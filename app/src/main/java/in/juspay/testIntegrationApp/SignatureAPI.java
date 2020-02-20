package in.juspay.testIntegrationApp;

import android.os.AsyncTask;
import android.util.Log;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class SignatureAPI extends AsyncTask<String, Integer, String> {

    @Override
    protected String doInBackground(String... strings) {
        StringBuilder result = new StringBuilder();
        try {
            String orderUrl = strings[0] + "?payload=" + strings[1];
            Log.wtf("SignedByHarsh", orderUrl);

            HttpsURLConnection connection = (HttpsURLConnection) (new URL(orderUrl).openConnection());
            connection.setRequestMethod("GET");
            InputStream in = connection.getInputStream();
            InputStreamReader ir = new InputStreamReader(in);

            int data = ir.read();

            while (data != -1) {
                char curr = (char) data;
                result.append(curr);
                data = ir.read();
            }

            Log.wtf("SignedByHarsh", result.toString());
            return result.toString();
        } catch (Exception ignored) {
            return result.toString();
        }
    }
}
