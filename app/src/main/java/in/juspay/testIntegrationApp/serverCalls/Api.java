package in.juspay.testIntegrationApp.serverCalls;

import android.annotation.SuppressLint;
import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import static in.juspay.hypersdk.utils.network.NetUtils.generateQueryString;

public class Api {
    private static String LOG_TAG = "API";

    public static JuspayHTTPResponse generateOrder(JSONObject jsonPayload, String apiKey, String env) {
        try {
            String auth = Base64.encodeToString((apiKey + ":").getBytes(), Base64.DEFAULT);
            String orderUrl = env.equalsIgnoreCase("prod") ? "https://api.juspay.in" : "https://sandbox.juspay.in";
            orderUrl = orderUrl + "/order/create";
            HttpsURLConnection connection = (HttpsURLConnection) (new URL(orderUrl).openConnection());

            connection.setSSLSocketFactory(new TLSSocketFactory());
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Basic " + auth);
            connection.setRequestProperty("version", "2018-07-01");
            connection.setDoOutput(true);

            Map<String, String> payload = new HashMap<>();
            Iterator<?> keys = jsonPayload.keys();

            while( keys.hasNext() ){
                String key = (String)keys.next();
                String value = jsonPayload.getString(key);
                payload.put(key, value);

            }
            OutputStream stream = connection.getOutputStream();
            stream.write(generateQueryString(payload).getBytes());
            JuspayHTTPResponse response = new JuspayHTTPResponse(connection);
            Log.d(LOG_TAG, "Order: " + response.responsePayload);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}


class TLSSocketFactory extends SSLSocketFactory {

    private SSLSocketFactory delegate;

    public TLSSocketFactory() throws KeyManagementException, NoSuchAlgorithmException {
        SSLContext context = SSLContext.getInstance("TLS");
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @SuppressLint("TrustAllX509TrustManager")
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            @SuppressLint("TrustAllX509TrustManager")
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }
        };
        context.init(null, trustAllCerts, new SecureRandom());
        delegate = context.getSocketFactory();
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return delegate.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return delegate.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket() throws IOException {
        return enableTLSOnSocket(delegate.createSocket());
    }

    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        return enableTLSOnSocket(delegate.createSocket(s, host, port, autoClose));
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        return enableTLSOnSocket(delegate.createSocket(host, port));
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
        return enableTLSOnSocket(delegate.createSocket(host, port, localHost, localPort));
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return enableTLSOnSocket(delegate.createSocket(host, port));
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return enableTLSOnSocket(delegate.createSocket(address, port, localAddress, localPort));
    }

    private Socket enableTLSOnSocket(Socket socket) {
        if(socket != null && (socket instanceof SSLSocket)) {
            ((SSLSocket)socket).setEnabledProtocols(new String[] {"TLSv1.1", "TLSv1.2"});
        }
        return socket;
    }

}