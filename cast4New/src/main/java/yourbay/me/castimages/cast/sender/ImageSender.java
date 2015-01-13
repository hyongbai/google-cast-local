package yourbay.me.castimages.cast.sender;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ram on 15/1/7.
 */
public class ImageSender extends MediaSender {
    @Override
    protected MediaInfo generateMediaInfo(String uri, String title) {
        String token = "Bearer ya29.-gAjDNPoAVoYFZ1-xkj_Hd9L82MUzD2PBnIPGMO6OcdvIyawWD1DSV68pP43AbN6h9xucMl6ukAZIw";
        MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_PHOTO);
        mediaMetadata.putString(MediaMetadata.KEY_TITLE, title);
        mediaMetadata.putString("Authorization", token);
        JSONObject jo = new JSONObject();
        try {
            jo.put("Authorization", token);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        MediaInfo mediaInfo = new MediaInfo.Builder(uri)
                .setContentType("image/*")
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setMetadata(mediaMetadata)
//                .setCustomData(jo)
                .build();
        return mediaInfo;
    }
}
