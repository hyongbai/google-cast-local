package me.yourbay.cast4older;

import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.util.Log;
import android.view.Menu;

import com.google.cast.ApplicationChannel;
import com.google.cast.ApplicationMetadata;
import com.google.cast.ApplicationSession;
import com.google.cast.CastContext;
import com.google.cast.CastDevice;
import com.google.cast.Logger;
import com.google.cast.MediaRouteAdapter;
import com.google.cast.MediaRouteHelper;
import com.google.cast.MediaRouteStateChangeListener;
import com.google.cast.SessionError;

import java.io.IOException;


public class Cast4Activity extends ActionBarActivity implements MediaRouteAdapter {

    private static final String TAG = "Cast4Activity";
    private static final Logger sLog = new Logger(TAG, true);
    private MediaRouteSelector mMediaRouteSelector;
    private MediaRouter mMediaRouter;
    private MediaRouter.Callback mMediaRouterCallback;
    private CastContext mCastContext;

    private CastDevice mSelectedDevice;
    private ApplicationSession mSession;
    private SessionListener mSessionListener;


    private void endSession() {
        if (mSession == null) {
            return;
        }
//        if ((this.mSession != null) && (this.mSession.hasStarted()))
        try {
            if (this.mSession.hasChannel()) {
//                this.mGameMessageStream.leave();
            }
            this.mSession.endSession();
            return;
        } catch (IOException localIOException) {
            Log.e(TAG, "Failed to end the session.", localIOException);
            this.mSession = null;
        } catch (IllegalStateException localIllegalStateException) {
            Log.e(TAG, "Unable to end session.", localIllegalStateException);
            this.mSession = null;
        } finally {
            this.mSession = null;
        }
    }

    private void onRouteSelected(MediaRouter.RouteInfo paramRouteInfo) {
        Logger localLogger = sLog;
        Object[] arrayOfObject = new Object[1];
        arrayOfObject[0] = paramRouteInfo.getName();
        localLogger.d("onRouteSelected: %s", arrayOfObject);
        MediaRouteHelper.requestCastDeviceForRoute(paramRouteInfo);
    }

    private void onRouteUnselected(MediaRouter.RouteInfo paramRouteInfo) {
        Logger localLogger = sLog;
        Object[] arrayOfObject = new Object[1];
        arrayOfObject[0] = paramRouteInfo.getName();
        localLogger.d("onRouteUnselected: %s", arrayOfObject);
        setSelectedDevice(null);
    }

    private void setSelectedDevice(CastDevice paramCastDevice) {
        this.mSelectedDevice = paramCastDevice;
        if (this.mSelectedDevice != null) {
            this.mSession = new ApplicationSession(this.mCastContext, this.mSelectedDevice);
            this.mSession.setListener(mSessionListener);
        }
        while (true) {
            try {
                this.mSession.startSession("TicTacToe");
                return;
            } catch (IOException localIOException) {
                Log.e(TAG, "Failed to open a session", localIOException);
                continue;
            }
//            endSession();
//            this.mPlayerNameView.setText(null);
//            this.mInfoView.setText(2131034151);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cast4);
        mSessionListener = new SessionListener();
        this.mCastContext = new CastContext(getApplicationContext());
        MediaRouteHelper.registerMinimalMediaRouteProvider(mCastContext, this);

        this.mMediaRouter = MediaRouter.getInstance(getApplicationContext());
        this.mMediaRouteSelector = MediaRouteHelper.buildMediaRouteSelector("com.google.cast.CATEGORY_CAST", "TicTacToe", null);
        this.mMediaRouterCallback = new MediaRouterCallback();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_cast4, menu);
        ((MediaRouteActionProvider) MenuItemCompat.getActionProvider(menu.findItem(R.id.media_route_menu_item))).setRouteSelector(this.mMediaRouteSelector);
        return true;
    }

    public void onDestroy() {
        MediaRouteHelper.unregisterMediaRouteProvider(this.mCastContext);
        this.mCastContext.dispose();
        this.mCastContext = null;
        super.onDestroy();
    }

    public void onDeviceAvailable(CastDevice paramCastDevice, String paramString, MediaRouteStateChangeListener paramMediaRouteStateChangeListener) {
        Logger localLogger = sLog;
        Object[] arrayOfObject = new Object[2];
        arrayOfObject[0] = paramCastDevice;
        arrayOfObject[1] = paramString;
        localLogger.d("onDeviceAvailable: %s (route %s)", arrayOfObject);
        setSelectedDevice(paramCastDevice);
    }

    protected void onPause() {
        super.onPause();
        finish();
    }

    public void onSetVolume(double paramDouble) {
    }

    protected void onStart() {
        super.onStart();
        this.mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback, MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN);
    }

    protected void onStop() {
        endSession();
        this.mMediaRouter.removeCallback(this.mMediaRouterCallback);
        super.onStop();
    }

    public void onUpdateVolume(double paramDouble) {
    }

    private class SessionListener
            implements ApplicationSession.Listener {
        private SessionListener() {
        }

        public void onSessionEnded(SessionError paramSessionError) {
            Logger localLogger = sLog;
            Object[] arrayOfObject = new Object[1];
            arrayOfObject[0] = paramSessionError;
            localLogger.d("SessionListener.onEnded: %s", arrayOfObject);
        }

        public void onSessionStartFailed(SessionError paramSessionError) {
            Logger localLogger = sLog;
            Object[] arrayOfObject = new Object[1];
            arrayOfObject[0] = paramSessionError;
            localLogger.d("SessionListener.onStartFailed: %s", arrayOfObject);
        }

        public void onSessionStarted(ApplicationMetadata paramApplicationMetadata) {
            sLog.d("SessionListener.onStarted", new Object[0]);
//            GameActivity.this.mInfoView.setText(2131034141);
            ApplicationChannel localApplicationChannel = mSession.getChannel();
            if (localApplicationChannel == null)
                Log.w(TAG, "onStarted: channel is null");
//            while (true) {
//                return;
//                localApplicationChannel.attachMessageStream(mGameMessageStream);
//                GameActivity.this.mGameMessageStream.join("MyName");
//            }
        }
    }

    class MediaRouterCallback extends MediaRouter.Callback {

        private MediaRouterCallback() {
        }

        public void onRouteSelected(MediaRouter paramMediaRouter, MediaRouter.RouteInfo paramRouteInfo) {
            Logger localLogger = sLog;
            Object[] arrayOfObject = new Object[1];
            arrayOfObject[0] = paramRouteInfo;
            localLogger.d("onRouteSelected: %s", arrayOfObject);
            Cast4Activity.this.onRouteSelected(paramRouteInfo);
        }

        public void onRouteUnselected(MediaRouter paramMediaRouter, MediaRouter.RouteInfo paramRouteInfo) {
            Logger localLogger = sLog;
            Object[] arrayOfObject = new Object[1];
            arrayOfObject[0] = paramRouteInfo;
            localLogger.d("onRouteUnselected: %s", arrayOfObject);
            Cast4Activity.this.onRouteUnselected(paramRouteInfo);
        }
    }
}
