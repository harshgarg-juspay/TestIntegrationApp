package in.juspay.testIntegrationApp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import javax.net.ssl.HttpsURLConnection;

import in.juspay.hypersdk.data.JuspayResponseHandler;
import in.juspay.hypersdk.ui.HyperPaymentsCallbackAdapter;
import in.juspay.services.HyperServices;

public class PaymentsActivityV1 extends AppCompatActivity {
    private static final int SETTINGS_ACTIVITY_REQ_CODE = 420;
    private SharedPreferences preferences;
    private ProgressDialog pd;

    // Variables for initiate
    private JSONObject initiatePayload;

    private String apiKey;
    private String clientAuthToken;

    private boolean isInitiateDone;
    private JSONObject initiateResult;

    private LinearLayout initiateLayout;

    // Variables for process
    private JSONObject processPayload;

    private String orderId;

    private boolean isClientAuthTokenGenerated;
    private boolean isOrderIDGenerated;
    private boolean isProcessDone;
    private JSONObject processResult;

    private LinearLayout processLayout;

    // Payment services
    private HyperServices hyperServices;
    private String requestId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payments_v1);

        createPD();
        WebView.setWebContentsDebuggingEnabled(true);

        preferences = getSharedPreferences(PayloadConstants.SHARED_PREF_KEY, MODE_PRIVATE);

        prepareUI();
        initializeParams();

        hyperServices = new HyperServices(this, findViewById(android.R.id.content));
    }

    private void prepareUI() {
        initiateLayout = findViewById(R.id.initiateLayout);
        processLayout = findViewById(R.id.processLayout);

        initiateLayout.setVisibility(View.VISIBLE);
        processLayout.setVisibility(View.GONE);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(UiUtils.getWhiteText("Initiate"));
        }
    }

    private void createPD() {
        pd = new ProgressDialog(this);
        pd.setMessage("Processing...");
        pd.setCancelable(false);
        ProgressBar progressBar = new ProgressBar(this);
    }

    private void showPD() {
        if (pd == null) createPD();
        pd.show();
    }

    private void hidePD() {
        if (pd == null) return;
        pd.cancel();
    }

    private void initializeParams() {
        requestId = Payload.generateRequestId();
        apiKey = preferences.getString("apiKey", PayloadConstants.apiKey);

        isInitiateDone = false;
        initiateResult = new JSONObject();

        isOrderIDGenerated = false;
        orderId = "";
        isProcessDone = false;
        processResult = new JSONObject();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void generateClientAuthToken(View view) {
        new CustomerApiCaller(view).execute();
    }

    public void showclientAuthTokenInput(View view) {
        UiUtils.showMessageInModal(this, "Client Auth Token Input", apiKey);
    }

    public void showclientAuthTokenOutput(View view) {
        if (isClientAuthTokenGenerated) {
            UiUtils.showMessageInModal(this, "Client Auth Token Output", clientAuthToken);
        } else {
            Snackbar.make(view, "Please generate Client Auth Token to see the output", Snackbar.LENGTH_SHORT).show();
        }
    }

    public void generateInitiatePayload() {
        initiatePayload = Payload.generateInitiatePayloadV1(preferences, clientAuthToken);
    }

    public void initiateJuspaySdk(View view) {
        try {
            if (isClientAuthTokenGenerated) {
                JSONObject payload = Payload.getPaymentsPayload(preferences, requestId, initiatePayload);
                hyperServices.initiate(payload, new HyperPaymentsCallbackAdapter() {
                    @Override
                    public void onEvent(JSONObject data, JuspayResponseHandler juspayResponseHandler) {
                        Log.d("Inside OnEvent ", "initiate");
                        try {
                            String event = data.getString("event");
                            switch (event) {
                                case "initiate_result":
                                    isInitiateDone = true;
                                    initiateResult = data;
                                    Snackbar.make(view, "Initiate Complete", Snackbar.LENGTH_SHORT).show();
                                    Log.wtf("initiate_result", data.toString());
                                    break;
                                case "process_result":
                                    isProcessDone = true;
                                    processResult = data;
                                    Objects.requireNonNull(getSupportActionBar()).show();
                                    processLayout.setVisibility(View.VISIBLE);
                                    Snackbar.make(view, "Process Complete", Snackbar.LENGTH_SHORT).show();
                                    Log.wtf("process_result", data.toString());
                                    break;
                                case "hide_loader":
                                    hidePD();
                                    break;
                                default:
                                    Snackbar.make(view, "Unknown Result", Snackbar.LENGTH_SHORT).show();
                                    UiUtils.showMessageInModal(PaymentsActivityV1.this, "Unknown Result", data.toString());
                                    Log.wtf(event, data.toString());
                                    break;
                            }
                        } catch (Exception e) {
                            Log.d("Came here", e.toString());
                            e.printStackTrace();
                        }
                    }
                });
            } else {
                Snackbar.make(view, "Please generate Client Auth Token first", Snackbar.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showInitiateInput(View view) {
        try {
            if (isClientAuthTokenGenerated) {
                JSONObject payload = Payload.getPaymentsPayload(preferences, requestId, initiatePayload);
                UiUtils.showMessageInModal(this, "Initiate Input", payload.toString(4));
            } else {
                Snackbar.make(view, "Please generate Client Auth Token first", Snackbar.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void showInitiateOutput(View view) {
        try {
            if (isInitiateDone) {
                UiUtils.showMessageInModal(this, "Initiate Result", initiateResult.toString(4));
            } else {
                Snackbar.make(view, "Initiate not completed yet!", Snackbar.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void startProcessActivity(View view) {
        if (isInitiateDone) {
            Objects.requireNonNull(getSupportActionBar()).setTitle(UiUtils.getWhiteText("Process"));
            initiateLayout.setVisibility(View.GONE);
            processLayout.setVisibility(View.VISIBLE);
        } else {
            Snackbar.make(view, "Please Complete Initiate", Snackbar.LENGTH_SHORT).show();
        }

    }

    public void showInitiateFAQ(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            UiUtils.launchInCustomTab(this, "initiate");
        } else {
            UiUtils.openWebView(this, "initiate");
        }
    }

    // Process Functions

    public void generateOrderID(View view) {
        orderId = Payload.generateOrderId();
        new OrderApiCaller().execute();
        isOrderIDGenerated = true;
        Snackbar.make(view, "Order ID Generated: " + orderId, Snackbar.LENGTH_SHORT).show();
        generateProcessPayload();
    }

    public void copyOrderID(View view) {
        if (isOrderIDGenerated) {
            UiUtils.copyToClipBoard(this, "OrderID", orderId);
            Snackbar.make(view, "OrderID copied to clipboard: " + orderId, Snackbar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(view, "Please generate an order id", Snackbar.LENGTH_SHORT).show();
        }
    }

    public void showOrderIdFAQ(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            UiUtils.launchInCustomTab(this, "orderID");
        } else {
            UiUtils.openWebView(this, "orderID");
        }
    }

    public void generateProcessPayload() {
        processPayload = Payload.generateProcessPayloadV1(preferences, orderId, clientAuthToken);
    }

    public void processJuspaySdk(View view) {
        if (isOrderIDGenerated) {
            showPD();
            JSONObject payload = Payload.getPaymentsPayload(preferences, requestId, processPayload);
            hyperServices.process(payload);
            Objects.requireNonNull(getSupportActionBar()).hide();
        } else {
            Snackbar.make(view, "Please generate Order ID", Snackbar.LENGTH_SHORT).show();
        }
    }

    public void showProcessInput(View view) {
        try {
            if (isOrderIDGenerated) {
                JSONObject payload = Payload.getPaymentsPayload(preferences, requestId, processPayload);
                UiUtils.showMessageInModal(this, "Process Input", payload.toString(4));
            } else {
                Snackbar.make(view, "Please generate Order ID", Snackbar.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void showProcessOutput(View view) {
        try {
            if (isProcessDone) {
                UiUtils.showMessageInModal(this, "Process Result", processResult.toString(4));
            } else {
                Snackbar.make(view, "Process not completed yet!", Snackbar.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void showProcessFAQ(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            UiUtils.launchInCustomTab(this, "process");
        } else {
            UiUtils.openWebView(this, "process");
        }
    }

    private void reset() {
        prepareUI();
        initializeParams();
    }

    public void terminateJuspaySdk(View view) {
        Snackbar.make(view,  "Juspay SDK terminated", Snackbar.LENGTH_SHORT).show();
        hyperServices.terminate();
        reset();
    }

    public void showTerminateFAQ(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            UiUtils.launchInCustomTab(this, "terminate");
        } else {
            UiUtils.openWebView(this, "terminate");
        }
    }

    @Override
    public void onBackPressed() {
        boolean backPressHandled = hyperServices.onBackPressed();
        if (!backPressHandled) {
            if (processLayout.getVisibility() == View.VISIBLE) {
                processLayout.setVisibility(View.GONE);
                initiateLayout.setVisibility(View.VISIBLE);
                Objects.requireNonNull(getSupportActionBar()).setTitle(UiUtils.getWhiteText("Initiate"));
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.configure:
                Intent intent = new Intent(PaymentsActivityV1.this, SettingsActivity.class);
                startActivityForResult(intent, SETTINGS_ACTIVITY_REQ_CODE, new Bundle());
                return true;
            case android.R.id.home:
                onBackPressed();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == SETTINGS_ACTIVITY_REQ_CODE) {
            if (data != null) {
                if (data.hasExtra("changed") && data.getBooleanExtra("changed", false)) {
                    Toast.makeText(this, "Resetting due to change in parameters", Toast.LENGTH_SHORT).show();
                    reset();
                    hyperServices.terminate();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    //API Calls

    public class CustomerApiCaller extends AsyncTask<URL, Integer, String> {

        View view;

        CustomerApiCaller(View view) {
            this.view = view;
        }

        protected String doInBackground(URL... urls) {
            String customerId = preferences.getString("customerId", PayloadConstants.customerId);
            String environment = preferences.getString("environment", PayloadConstants.environment).equals("sandbox") ? "sandbox" : "api";

            String url = "https://" + environment + ".juspay.in/customers/" + customerId + "?options.get_client_auth_token=true";

            try {
                URL httpsUrl = new URL(url);
                String auth = apiKey;

                HttpsURLConnection connection = (HttpsURLConnection)httpsUrl.openConnection();

                connection.setRequestMethod("GET");

                byte[] encodedAuth = auth.getBytes("UTF-8");

                connection.setRequestProperty("Authorization", "Basic " + new String(android.util.Base64.encode(encodedAuth, Base64.DEFAULT)));

                if(connection != null){
                    Log.e("connectionResponse "+ connection.getResponseCode(), "");
                    String resp = evaluateConnectionResponse(connection);
                    Log.e("final Response", resp);


                    for(int i=0; i<10; i++)
                        publishProgress(i*10);

                    return resp;
                }

            } catch (Exception e) {
                return e.toString();
            }

            return "error";
        }

        public String evaluateConnectionResponse(HttpsURLConnection connection){
            try {
                int responseCode = connection.getResponseCode();
                InputStreamReader responseReader;
                if ((responseCode < 200 || responseCode >= 300) && responseCode != 302) {
                    responseReader = new InputStreamReader(connection.getErrorStream());
                } else {
                    responseReader = new InputStreamReader(connection.getInputStream());
                }

                BufferedReader in = new BufferedReader(responseReader);
                StringBuilder response = new StringBuilder();

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }

                in.close();
                String responsePayload = response.toString();
                connection.disconnect();

                return responsePayload;

            } catch (Exception e){
                e.printStackTrace();
            }

            return "ErrorBoy";
        }

        protected void onPostExecute(String result) {
            try {
                Log.e("onPostExecute", result);
                JSONObject res = new JSONObject(result);

                Log.e("jsonObjCust", res.toString());

                String token = res.getJSONObject("juspay").getString("client_auth_token");
                Log.e("clientTokenFromJson", token);
                clientAuthToken = token;
                isClientAuthTokenGenerated = true;
                Snackbar.make(view, "Client Auth Token generated", Snackbar.LENGTH_SHORT).show();
                generateInitiatePayload();
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("e",""+e);
            }
        }

    }

    public class OrderApiCaller extends AsyncTask<URL, Integer, String> {

        public JSONObject generateOrderJSON(String orderId) {
            JSONObject obj = new JSONObject();

            try {
                obj.put("order_id", orderId);
                obj.put("amount", PayloadConstants.amount);
                obj.put("customer_id", PayloadConstants.customerId);
                obj.put("metadata.JUSPAY:gateway_reference_id", "vodafone");
                obj.put("metadata.LAZYPAY:gateway_reference_id", "vodafone");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.e("orderJson", obj.toString());
            return obj;
        }

        protected String doInBackground(URL... urls) {
            String environment = preferences.getString("environment", PayloadConstants.environment).equals("sandbox") ? "sandbox" : "api";
            String url = "https://" + environment + ".juspay.in/orders/";

            try {
                URL httpsUrl = new URL(url);
                String auth = apiKey;;

                HttpsURLConnection connection = (HttpsURLConnection)httpsUrl.openConnection();

                connection.setRequestMethod("POST");

                byte[] encodedAuth = auth.getBytes("UTF-8");

                JSONObject orderData = generateOrderJSON(orderId);

                connection.setRequestProperty("Authorization", "Basic " + new String(android.util.Base64.encode(encodedAuth, Base64.DEFAULT)));

                byte[] out = orderData.toString().getBytes(StandardCharsets.UTF_8);
                int length = out.length;

                connection.setFixedLengthStreamingMode(length);
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.connect();
                try(OutputStream os = connection.getOutputStream()) {
                    os.write(out);
                }

                if(connection != null){
                    Log.d("connectionRepsonse "+ connection.getResponseCode(), "");
                    String resp = evaluateConnectionResponse(connection);
                    Log.d("final Response", resp);

                    for(int i=0; i<10; i++)
                        publishProgress(i*10);

                    return resp;
                }

            } catch (Exception e) {
                return e.toString();
            }

            return "error";
        }

        public String evaluateConnectionResponse(HttpsURLConnection connection){
            try {
                int responseCode = connection.getResponseCode();
                InputStreamReader responseReader;
                if ((responseCode < 200 || responseCode >= 300) && responseCode != 302) {
                    responseReader = new InputStreamReader(connection.getErrorStream());
                } else {
                    responseReader = new InputStreamReader(connection.getInputStream());
                }

                BufferedReader in = new BufferedReader(responseReader);
                StringBuilder response = new StringBuilder();

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }

                in.close();
                String responsePayload = response.toString();
                connection.disconnect();

                return responsePayload;

            } catch (Exception e){
                e.printStackTrace();
            }
            return "ErrorBoy";
        }

        protected void onPostExecute(String result) {
            Log.d("onPostExecute", result);
        }

    }
}
