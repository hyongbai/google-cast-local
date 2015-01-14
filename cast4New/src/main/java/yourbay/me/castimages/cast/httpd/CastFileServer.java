package yourbay.me.castimages.cast.httpd;

import android.text.TextUtils;

import org.apache.http.conn.util.InetAddressUtils;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by ram on 15/1/14.
 */
public class CastFileServer extends FileServer {

    public CastFileServer() {
        super();
    }

    public CastFileServer(int port) {
        super(port);
    }

    public static final String getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> infos = NetworkInterface
                    .getNetworkInterfaces();
            while (infos.hasMoreElements()) {
                NetworkInterface niFace = infos.nextElement();
                Enumeration<InetAddress> enumIpAddr = niFace.getInetAddresses();
                while (enumIpAddr.hasMoreElements()) {
                    InetAddress mInetAddress = enumIpAddr.nextElement();
                    if (!mInetAddress.isLoopbackAddress()
                            && InetAddressUtils.isIPv4Address(mInetAddress
                            .getHostAddress())) {
                        return mInetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String cropFilePath(String uri) {
        //String divider = new StringBuilder(":").append(serverPort).toString();
        return uri.trim().replace(File.separatorChar, '/');
    }

    public final String generateUri(String localPath) {
        if (TextUtils.isEmpty(localPath)) {
            return null;
        }
        return new StringBuilder("http://")
                .append(getLocalIpAddress())//
                .append(":")//
                .append(getServerPort())//
                .append(localPath)//
                .toString();
    }
}
