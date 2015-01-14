package yourbay.me.castimages.cast.httpd;

import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ram on 15/1/13.
 */
public abstract class FileServer extends NanoHTTPD {
    public final static int DEFAULT_SERVER_PORT = 8000;
    private static final String TAG = "FileServer";
    private static final String MIME_DEFAULT_BINARY = "application/octet-stream";
    private static final Map<String, String> MIME_TYPES = new HashMap<String, String>() {
        {
            put("gif", "image/gif");
            put("jpg", "image/jpeg");
            put("jpeg", "image/jpeg");
            put("png", "image/png");
            put("mp3", "audio/mpeg");
            put("m3u", "audio/mpeg-url");
            put("mp4", "video/mp4");
            put("ogv", "video/ogg");
            put("flv", "video/x-flv");
            put("mov", "video/quicktime");
        }
    };
    private int serverPort;

    public FileServer() {
        this(DEFAULT_SERVER_PORT);
    }

    public FileServer(int port) {
        this(null, port);
    }

    public FileServer(String hostname, int port) {
        super(hostname, port);
        this.serverPort = port;
    }

    public int getServerPort() {
        return serverPort;
    }

    @Override
    public Response serve(IHTTPSession session) {
        Map<String, String> headers = session.getHeaders();
        String uri = session.getUri();
        Log.d(TAG, "serve   uri=" + uri);
        return respond(headers, uri);
    }

    public abstract String cropFilePath(String uri);

    /**
     * xxx.xxx.xxx.xxx:xxxx/filePath
     */
    private Response respond(Map<String, String> headers, String uri) {
        final String FILE_PATH = cropFilePath(uri);
        String mimeTypeForFile = getMimeTypeForFile(FILE_PATH);
        Response response = serveFile(headers, FILE_PATH, mimeTypeForFile);
        return response != null ? response : createResponse(
                Response.Status.NOT_FOUND, MIME_PLAINTEXT,
                "Error 404, file not found.");
    }

    private String getMimeTypeForFile(String uri) {
        int dot = uri.lastIndexOf('.');
        String mime = null;
        if (dot >= 0) {
            mime = MIME_TYPES.get(uri.substring(dot + 1).toLowerCase());
        }
        return mime == null ? MIME_DEFAULT_BINARY : mime;
    }

    private Response serveFile(Map<String, String> header, String file, String mime) {
        Response res;
        try {
            InputStream cInputStream = new FileInputStream(file);
            long fileLen = cInputStream.available();
            String etag = Integer.toHexString((file + "" + fileLen).hashCode());
            long startFrom = 0;
            long endAt = -1;
            String range = header.get("range");
            if (range != null) {
                if (range.startsWith("bytes=")) {
                    range = range.substring("bytes=".length());
                    int minus = range.indexOf('-');
                    try {
                        if (minus > 0) {
                            startFrom = Long.parseLong(range
                                    .substring(0, minus));
                            endAt = Long.parseLong(range.substring(minus + 1));
                        }
                    } catch (NumberFormatException ignored) {
                        ignored.printStackTrace();
                    }
                }
            }

            if (range != null && startFrom >= 0) {
                if (startFrom >= fileLen) {
                    res = createResponse(Response.Status.RANGE_NOT_SATISFIABLE,
                            MIME_PLAINTEXT, "");
                    res.addHeader("Content-Range", "bytes 0-0/" + fileLen);
                    res.addHeader("ETag", etag);
                } else {
                    if (endAt < 0) {
                        endAt = fileLen - 1;
                    }
                    long newLen = endAt - startFrom + 1;
                    if (newLen < 0) {
                        newLen = 0;
                    }
                    final long dataLen = newLen;
                    cInputStream.skip(startFrom);
                    res = createResponse(Response.Status.PARTIAL_CONTENT, mime,
                            cInputStream);
                    res.addHeader("Content-Length", "" + dataLen);
                    res.addHeader("Content-Range", "bytes " + startFrom + "-"
                            + endAt + "/" + fileLen);
                    res.addHeader("ETag", etag);
                }
            } else {
                if (etag.equals(header.get("if-none-match")))
                    res = createResponse(Response.Status.NOT_MODIFIED, mime, "");
                else {
                    res = createResponse(Response.Status.OK, mime, cInputStream);
                    res.addHeader("Content-Length", "" + fileLen);
                    res.addHeader("ETag", etag);
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            res = createResponse(Response.Status.FORBIDDEN,
                    MIME_PLAINTEXT, "FORBIDDEN: Reading file failed.");
        }
        return res;
    }

    // Announce that the file server accepts partial content requests
    private Response createResponse(Response.Status status, String mimeType, InputStream message) {
        Response res = new Response(status, mimeType, message);
        res.addHeader("Accept-Ranges", "bytes");
        return res;
    }

    // Announce that the file server accepts partial content requests
    private Response createResponse(Response.Status status, String mimeType, String message) {
        Response res = new Response(status, mimeType, message);
        res.addHeader("Accept-Ranges", "bytes");
        return res;
    }
}
