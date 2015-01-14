package yourbay.me.castimages.cast;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.util.Log;
import android.view.MenuItem;

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
    private boolean IS_DEBUG_MODE = true;
    private MediaRouter mMediaRouter;
    private MediaRouteSelector mMediaRouteSelector;
    private CastDevice mSelectedDevice;
    private CastRouterCallback mMediaRouterCallback = new CastRouterCallback();
    private GoogleApiClient mApiClient;
    private Cast.Listener mCastClientListener = new Cast.Listener() {
        @Override
        public void onApplicationStatusChanged() {
            if (IS_DEBUG_MODE) {
                Log.d(TAG, "onApplicationStatusChanged: " + (mApiClient != null ? Cast.CastApi.getApplicationStatus(mApiClient) : ""));
            }
        }

        @Override
        public void onVolumeChanged() {
            if (IS_DEBUG_MODE) {
                Log.d(TAG, "onVolumeChanged: " + (mApiClient != null ? Cast.CastApi.getVolume(mApiClient) : ""));
            }
        }

        @Override
        public void onApplicationDisconnected(int errorCode) {
            if (IS_DEBUG_MODE) {
                Log.d(TAG, "onApplicationDisconnected: " + errorCode);
            }
            teardown();
        }
    };
    private String mSessionId;
    private boolean mApplicationStarted;
    private boolean mWaitingForReconnect;
    private ConnectionCallbacks mConnectionCallbacks = new ConnectionCallbacks();
    private ConnectionFailedListener mConnectionFailedListener = new ConnectionFailedListener();
    private HelloWorldChannel mHelloWorldChannel;
    private Context mContext;

    public CastHelper(Context context) {
        mContext = context;
        mMediaRouter = MediaRouter.getInstance(context);
        mMediaRouteSelector = new MediaRouteSelector.Builder().addControlCategory(CastMediaControlIntent.categoryForCast(Consts.APP_ID)).build();
        if (IS_DEBUG_MODE) {
            Log.d(TAG, "onCreate " + mMediaRouter + " " + mMediaRouteSelector);
        }
    }

    private void onDeviceSelected() {
        if (IS_DEBUG_MODE) {
            Log.d(TAG, "onDeviceSelected");
        }
        Cast.CastOptions.Builder apiOptionsBuilder = Cast.CastOptions.builder(mSelectedDevice, mCastClientListener);
        mApiClient = new GoogleApiClient.Builder(mContext)//
                .addApi(Cast.API, apiOptionsBuilder.build())//
                .addConnectionCallbacks(mConnectionCallbacks)//
                .addOnConnectionFailedListener(mConnectionFailedListener)//
                .build();
        mApiClient.connect();
    }

    private void launchReceiverApp() {
        if (mApiClient == null) {
            return;
        }
        Cast.CastApi.launchApplication(mApiClient, Consts.APP_ID, true).setResultCallback(new ResultCallback<Cast.ApplicationConnectionResult>() {
            @Override
            public void onResult(Cast.ApplicationConnectionResult result) {
                Status status = result.getStatus();
                if (IS_DEBUG_MODE) {
                    Log.d(TAG, "launchReceiverApp	onResult=" + status.isSuccess() + " StatusCode=" + status.getStatusCode() + " StatusMessage=" + status.getStatusMessage());
                }
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
        if ((connectionHint != null) && connectionHint.getBoolean(Cast.EXTRA_APP_NO_LONGER_RUNNING)) {
            if (IS_DEBUG_MODE) {
                Log.d(TAG, "App  is no longer running");
            }
            teardown();
        } else {
            if (IS_DEBUG_MODE) {
                Log.d(TAG, "Re-create the custom message channel");
            }
            try {
                Cast.CastApi.setMessageReceivedCallbacks(mApiClient, mHelloWorldChannel.getNamespace(), mHelloWorldChannel);
            } catch (IOException e) {
                Log.e(TAG, "Exception while creating channel", e);
            }
        }
    }

    private void teardown() {
        if (IS_DEBUG_MODE) {
            Log.d(TAG, "teardown");
        }
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

    public void onCreateOptionsMenu(MenuItem item) {
        MediaRouteActionProvider mProvider = (MediaRouteActionProvider) MenuItemCompat.getActionProvider(item);
        mProvider.setRouteSelector(mMediaRouteSelector);
    }

    public void start() {
        mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback, MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN);
    }

    public void stop() {
        mMediaRouter.removeCallback(mMediaRouterCallback);
    }

    public GoogleApiClient getApiClient() {
        return mApiClient;
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
