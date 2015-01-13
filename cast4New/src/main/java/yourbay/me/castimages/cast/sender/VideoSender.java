package yourbay.me.castimages.cast.sender;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;

/**
 * Created by ram on 15/1/7.
 */
public class VideoSender extends MediaSender {

    @Override
    public MediaInfo generateMediaInfo(String uri, String title) {
        MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
        mediaMetadata.putString(MediaMetadata.KEY_TITLE, title);
        MediaInfo mediaInfo = new MediaInfo.Builder(
                "http://your.server.com/video.mp4")
                .setContentType("video/mp4")
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setMetadata(mediaMetadata)
                .build();
        return mediaInfo;
    }
}
