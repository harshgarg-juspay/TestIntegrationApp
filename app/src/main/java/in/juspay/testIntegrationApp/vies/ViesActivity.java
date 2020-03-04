package in.juspay.testIntegrationApp.vies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;

import in.juspay.hypersdk.core.PaymentConstants;
import in.juspay.hypersdk.data.JuspayResponseHandler;
import in.juspay.hypersdk.ui.HyperPaymentsCallbackAdapter;
import in.juspay.hypersdk.ui.JuspayPaymentsCallback;
import in.juspay.hypersdk.ui.JuspayWebView;
import in.juspay.services.HyperServices;
import in.juspay.testIntegrationApp.R;
import in.juspay.testIntegrationApp.SignatureAPI;
import in.juspay.testIntegrationApp.UiUtils;
import in.juspay.testIntegrationApp.paymentPage.Payload;
import in.juspay.testIntegrationApp.paymentPage.PaymentsActivity;

import static in.juspay.hypersdk.utils.GPayUtils.LOG_TAG;

public class ViesActivity extends AppCompatActivity {

    private LinearLayout initiateLayout;
    private LinearLayout processLayout;
    private Button getCustInfo;
    private Spinner service_type;
    private CardView signOrderCard;

    private SharedPreferences sharedPreferences;
    private HyperServices hyperServices;

    // Variables for process
    private JSONObject orderDetails;
    private JSONObject processPayload;


    private String orderId;
    private String sessionToken;
    private boolean isOrderIDGenerated;
    private boolean isProcessDone;
    private JSONObject processResult;
    private String requestId;
    private boolean isInitiateDone;
    private JSONObject initiateResult;
    private JSONObject initiatePayload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payments);

        WebView.setWebContentsDebuggingEnabled(true);

        sharedPreferences = getSharedPreferences(Payload.PayloadConstants.SHARED_PREF_KEY, MODE_PRIVATE);

        prepareUI();
        initializeParams();

        hyperServices = new HyperServices(this);
    }

    private void prepareUI() {
        initiateLayout = findViewById(R.id.initiateLayout);
        processLayout = findViewById(R.id.processLayout);

        initiateLayout.setVisibility(View.VISIBLE);
        processLayout.setVisibility(View.GONE);

        getCustInfo = findViewById(R.id.actionButton1);
        getCustInfo.setText("Get Customer Details");


        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(UiUtils.getWhiteText("Initiate"));
        }
    }

    private void initializeParams() {
        requestId = Payload.generateRequestId();

        isInitiateDone = false;
        initiateResult = new JSONObject();

        isOrderIDGenerated = false;
        orderId = "";
        isProcessDone = false;
        processResult = new JSONObject();
    }

    public void initiateJuspaySdk(View view) {
        Bundle viesBundle = new Bundle();


        JSONObject payload = ViesPayload.generateInitiatePayload(sharedPreferences);
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
                            Toast.makeText(ViesActivity.this, "Initiate Complete", Toast.LENGTH_SHORT).show();
                            Log.wtf("initiate_result", data.toString());
                            break;
                        case "process_result":
                            isProcessDone = true;
                            processResult = data;
                            Objects.requireNonNull(getSupportActionBar()).show();
                            processLayout.setVisibility(View.VISIBLE);
                            Toast.makeText(ViesActivity.this, "Process Complete", Toast.LENGTH_SHORT).show();
                            Log.wtf("process_result", data.toString());
                            break;
                        default:
                            Toast.makeText(ViesActivity.this, "Unknown Result", Toast.LENGTH_SHORT).show();
                            UiUtils.showMessageInModal(ViesActivity.this, "Unknown Result", data.toString());
                            Log.wtf(event, data.toString());
                            break;
                    }
                } catch (Exception e) {
                    Log.d("Came here", e.toString());
                    e.printStackTrace();
                }
            }
        });
    }
    //ActionButton1 for Getting customer Details.
    public void takeActionButton1(View view) {
        getCustomerInfo(view);
    }

    public void getCustomerInfo(View view) {
        new AsyncTask<Void, Void, JSONObject>() {
            @Override
            protected JSONObject doInBackground(Void... voids) {
                return Utils.getSessionApi(view.getContext());
            }

            @Override
            protected void onPostExecute(JSONObject response) {
                try {
                    Log.d("VIESActivity", response.toString(2));
                    sessionToken = response.getJSONObject("juspay").getString("client_auth_token");
                    Snackbar.make(view, "Customer Details Received.", Snackbar.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Snackbar.make(view, "Error on getting Customer Details", Snackbar.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }.execute();

    }

    public void startProcessActivity(View view) {
        if (isInitiateDone) {
            Objects.requireNonNull(getSupportActionBar()).setTitle(UiUtils.getWhiteText("Process"));
            initiateLayout.setVisibility(View.GONE);
            processLayout.setVisibility(View.VISIBLE);
            service_type = findViewById(R.id.serviceType);
            service_type.setVisibility(View.VISIBLE);
            signOrderCard = findViewById(R.id.signProcessCard);
            signOrderCard.setVisibility(View.GONE);

//            Add List view of vies options and call accordingly.

        } else {
            Snackbar.make(view, "Please Complete Initiate", Snackbar.LENGTH_SHORT).show();
        }

    }


    //Process Functions

    public void generateOrderID(View view) {
        orderId = Payload.generateOrderId();
        isOrderIDGenerated = true;
        Toast.makeText(this, "Order ID Generated: " + orderId, Toast.LENGTH_LONG).show();
        generateOrderDetails();
    }
    public void copyOrderID(View view) {
        if (isOrderIDGenerated) {
            UiUtils.copyToClipBoard(this, "OrderID", orderId);
            Toast.makeText(this, "OrderID copied to clipboard: " + orderId, Toast.LENGTH_SHORT).show();
        } else {
            Snackbar.make(view, "Generate an order id", Snackbar.LENGTH_SHORT).show();
        }
    }

    public void showOrderIdFAQ(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            UiUtils.launchInCustomTab(this, "orderID");
        } else {
            UiUtils.openWebView(this, "orderID");
        }
    }

    public void generateOrderDetails() {
        orderDetails = Payload.generateOrderDetails(sharedPreferences, orderId);
    }

    public void showProcessInput(View view) {
        try {
            if (isOrderIDGenerated) {
                JSONObject payload = Payload.getPaymentsPayload(sharedPreferences, requestId, processPayload);
                UiUtils.showMessageInModal(this, "Process Input", payload.toString(4));
            } else {
                Snackbar.make(view, "Please generate order ID", Snackbar.LENGTH_SHORT).show();
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

    public void processJuspaySdk(View view) {
        if (isOrderIDGenerated) {
            //Based on the selected vies service type. Generate payload and process
            service_type = findViewById(R.id.serviceType);

            String a = service_type.getSelectedItem().toString();
            switch (a) {
                case "ELIGIBILITY":
                    hyperServices.process(ViesPayload.getEligibilityPayload(sharedPreferences, requestId, view));
                    break;
                case "GET-MAX-AMOUNT":
                    hyperServices.process(ViesPayload.getMaxAmountPayload(sharedPreferences, requestId, view));
                    break;
                case "PAY":
//                    createOrder;
//                    hyperServices.process(ViesPayload.getPayPayload(sharedPreferences, requestId, view));
                    break;
                case "DISENROLL":
                    hyperServices.process(ViesPayload.getDeenrollCardPayload(sharedPreferences, requestId, view, sessionToken));
                    break;
                case "DELETE":
                    hyperServices.process(ViesPayload.getDeleteCardPayload(sharedPreferences, requestId, view));
                    break;
                default:

            }
//            Objects.requireNonNull(getSupportActionBar()).hide();
        } else {
            Snackbar.make(view, "Please generate orderID", Snackbar.LENGTH_SHORT).show();
        }
    }

}
