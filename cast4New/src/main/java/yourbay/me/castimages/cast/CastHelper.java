package yourbay.me.castimages.cast;


import android.content.Context;
import android.os.Bundle;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.util.Log;

import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import java.io.IOException;

/**
 * Created by ram on 15/1/7.
 */
public class CastHelper {

    private final static String TAG = "CastHelper";
    private MediaRouter mMediaRouter;
    private MediaRouteSelector mMediaRouteSelector;
    private CastDevice mSelectedDevice;
    private CastRouterCallback mMediaRouterCallback = new CastRouterCallback();
    private GoogleApiClient mApiClient;
    private String mSessionId;
    private boolean mApplicationStarted;
    private boolean mWaitingForReconnect;
    private ConnectionCallbacks mConnectionCallbacks = new ConnectionCallbacks();
    private ConnectionFailedListener mConnectionFailedListener = new ConnectionFailedListener();
    private HelloWorldChannel mHelloWorldChannel;
    private Context mContext;
    private Cast.Listener mCastClientListener = new Cast.Listener() {
        @Override
        public void onApplicationStatusChanged() {
            if (mApiClient != null) {
                Log.d(TAG, "onApplicationStatusChanged: " + Cast.CastApi.getApplicationStatus(mApiClient));
            }
        }

        @Override
        public void onVolumeChanged() {
            if (mApiClient != null) {
                Log.d(TAG, "onVolumeChanged: " + Cast.CastApi.getVolume(mApiClient));
            }
        }

        @Override
        public void onApplicationDisconnected(int errorCode) {
            Log.d(TAG, "onApplicationDisconnected: " + errorCode);
            teardown();
        }
    };

    private void onDeviceSelected() {
        Log.d(TAG, "onDeviceSelected");
        Cast.CastOptions.Builder apiOptionsBuilder = Cast.CastOptions.builder(mSelectedDevice, mCastClientListener);
        mApiClient = new GoogleApiClient.Builder(mContext)//
                .addApi(Cast.API, apiOptionsBuilder.build())//
                .addConnectionCallbacks(mConnectionCallbacks)//
                .addOnConnectionFailedListener(mConnectionFailedListener)//
                .build();
        mApiClient.connect();
    }

    private void launchReceiverApp() {
        Log.d(TAG, "launchReceiverApp   mApiClient=" + (mApiClient != null));
        if (mApiClient == null) {
            return;
        }
        Cast.CastApi.launchApplication(mApiClient, Consts.APP_ID, true).setResultCallback(new ResultCallback<Cast.ApplicationConnectionResult>() {
            @Override
            public void onResult(Cast.ApplicationConnectionResult result) {
                Status status = result.getStatus();
                Log.d(TAG, "launchReceiverApp	onResult=" + status.isSuccess() + " StatusCode=" + status.getStatusCode() + " StatusMessage=" + status.getStatusMessage());
                if (status.isSuccess()) {
                    mApplicationStarted = true;
                    mHelloWorldChannel = new HelloWorldChannel();
                    try {
                        Cast.CastApi.setMessageReceivedCallbacks(mApiClient, mHelloWorldChannel.getNamespace(), mHelloWorldChannel);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Exception while creating channel", e);
                    }
                } else {
                    teardown();
                }
            }
        });
    }

    private void reconnectReceiverApp(Bundle connectionHint) {
        Log.d(TAG, "reconnectReceiverApp");
        if ((connectionHint != null) && connectionHint.getBoolean(Cast.EXTRA_APP_NO_LONGER_RUNNING)) {
            Log.d(TAG, "App  is no longer running");
            teardown();
        } else {
            Log.d(TAG, "Re-create the custom message channel");
            try {
                Cast.CastApi.setMessageReceivedCallbacks(mApiClient, mHelloWorldChannel.getNamespace(), mHelloWorldChannel);
            } catch (IOException e) {
                Log.e(TAG, "Exception while creating channel", e);
            }
        }
    }

    private void teardown() {
        Log.d(TAG, "teardown");
        if (mApiClient != null) {
            if (mApplicationStarted) {
                if (mApiClient.isConnected() || mApiClient.isConnecting()) {
                    try {
                        Cast.CastApi.stopApplication(mApiClient, mSessionId);
                        if (mHelloWorldChannel != null) {
                            Cast.CastApi.removeMessageReceivedCallbacks(mApiClient, mHelloWorldChannel.getNamespace());
                            mHelloWorldChannel = null;
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Exception while removing channel", e);
                    }
                    mApiClient.disconnect();
                }
                mApplicationStarted = false;
            }
            mApiClient = null;
        }
        mSelectedDevice = null;
        mWaitingForReconnect = false;
    }

    public void onCreate(Context context) {
        mContext = context;
        mMediaRouter = MediaRouter.getInstance(context);
        mMediaRouteSelector = new MediaRouteSelector.Builder().addControlCategory(CastMediaControlIntent.categoryForCast(Consts.APP_ID)).build();
        Log.d(TAG, "onCreate " + mMediaRouter + " " + mMediaRouteSelector);
    }

    public void onResume() {
        mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback, MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN);
    }

    public void onPause() {
        mMediaRouter.removeCallback(mMediaRouterCallback);
    }

    public GoogleApiClient getApiClient(){
        return mApiClient;
    }

    public MediaRouteSelector getSelector() {
        return mMediaRouteSelector;
    }

    private class CastRouterCallback extends MediaRouter.Callback {

        @Override
        public void onRouteSelected(MediaRouter router, MediaRouter.RouteInfo info) {
            mSelectedDevice = CastDevice.getFromBundle(info.getExtras());
            onDeviceSelected();
        }

        @Override
        public void onRouteUnselected(MediaRouter router, MediaRouter.RouteInfo info) {
            teardown();
            mSelectedDevice = null;
        }
    }

    private class ConnectionCallbacks implements GoogleApiClient.ConnectionCallbacks {
        @Override
        public void onConnected(Bundle connectionHint) {
            Log.d(TAG, "ConnectionCallbacks onConnected: " + connectionHint);
            if (mWaitingForReconnect) {
                mWaitingForReconnect = false;
                reconnectReceiverApp(connectionHint);
            } else {
                launchReceiverApp();
            }
        }

        @Override
        public void onConnectionSuspended(int cause) {
            mWaitingForReconnect = true;
        }
    }

    private class ConnectionFailedListener implements GoogleApiClient.OnConnectionFailedListener {
        @Override
        public void onConnectionFailed(ConnectionResult result) {
            teardown();
        }
    }

    private class HelloWorldChannel implements Cast.MessageReceivedCallback {
        public String getNamespace() {
            return "urn:x-cast:com.example.custom";
        }

        @Override
        public void onMessageReceived(CastDevice castDevice, String namespace, String message) {
            Log.d(TAG, "onMessageReceived: " + message);
        }
    }
}
