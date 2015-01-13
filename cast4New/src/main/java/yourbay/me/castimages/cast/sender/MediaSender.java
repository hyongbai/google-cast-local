package yourbay.me.castimages.cast.sender;

import android.util.Log;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.RemoteMediaPlayer;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;

/**
 * Created by ram on 15/1/7.
 */
public abstract class MediaSender implements RemoteMediaPlayer.OnStatusUpdatedListener, RemoteMediaPlayer.OnMetadataUpdatedListener, ResultCallback {
    private final static String TAG = "MediaSender";
    private RemoteMediaPlayer mRemoteMediaPlayer;
    private GoogleApiClient mApiClient;
    private RemoteMediaPlayer.OnStatusUpdatedListener onStatusUpdatedListener;
    private RemoteMediaPlayer.OnMetadataUpdatedListener onMetadataUpdatedListener;
    private ResultCallback resultCallback;

    public MediaSender() {
        mRemoteMediaPlayer = new RemoteMediaPlayer();
        mRemoteMediaPlayer.setOnStatusUpdatedListener(this);
        mRemoteMediaPlayer.setOnMetadataUpdatedListener(this);
    }

    @Override
    public void onStatusUpdated() {
        if (onStatusUpdatedListener != null) {
            onStatusUpdatedListener.onStatusUpdated();
        }
    }

    @Override
    public void onMetadataUpdated() {
        if (onMetadataUpdatedListener != null) {
            onMetadataUpdatedListener.onMetadataUpdated();
        }
    }


    @Override
    public void onResult(Result result) {
        if (resultCallback != null) {
            resultCallback.onResult(result);
        }
        if (result.getStatus().isSuccess()) {
            Log.d(TAG, "Media loaded successfully" + result.getStatus().getStatusMessage());
        }
    }

    public void setApiClient(GoogleApiClient apiClient) {
        this.mApiClient = apiClient;
    }

    public void cast(String uri, String title) {
        if (mApiClient == null) {
            return;
        }
        MediaInfo info = generateMediaInfo(uri, title);
        if (info == null) {
            return;
        }
        load(info);
    }

    protected abstract MediaInfo generateMediaInfo(String uri, String title);

    protected void load(MediaInfo info) {
        try {
            mRemoteMediaPlayer.load(mApiClient, info, true).setResultCallback(this);
        } catch (IllegalStateException e) {
            Log.e(TAG, "Problem occurred with media during loading", e);
        } catch (Exception e) {
            Log.e(TAG, "Problem opening media during loading", e);
        }
    }


    public void setOnStatusUpdatedListener(RemoteMediaPlayer.OnStatusUpdatedListener onStatusUpdatedListener) {
        this.onStatusUpdatedListener = onStatusUpdatedListener;
    }

    public void setOnMetadataUpdatedListener(RemoteMediaPlayer.OnMetadataUpdatedListener onMetadataUpdatedListener) {
        this.onMetadataUpdatedListener = onMetadataUpdatedListener;
    }

    public void setResultCallback(ResultCallback resultCallback) {
        this.resultCallback = resultCallback;
    }
}
