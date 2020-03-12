package in.juspay.testIntegrationApp.vies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import in.juspay.hypersdk.data.JuspayResponseHandler;
import in.juspay.hypersdk.ui.HyperPaymentsCallbackAdapter;
import in.juspay.services.HyperServices;
import in.juspay.testIntegrationApp.ConfigureActivity;
import in.juspay.testIntegrationApp.MainActivity;
import in.juspay.testIntegrationApp.Preferences;
import in.juspay.testIntegrationApp.R;
import in.juspay.testIntegrationApp.SettingsActivity;
import in.juspay.testIntegrationApp.UiUtils;
import in.juspay.testIntegrationApp.paymentPage.Payload;

public class ViesActivity extends AppCompatActivity {

    private static final int SETTINGS_ACTIVITY_REQ_CODE = 420;
    private LinearLayout initiateLayout;
    private LinearLayout processLayout;
    private Button getCustInfo;
    private Spinner service_type;
    private CardView signOrderCard;

    private SharedPreferences sharedPreferences;
    private JSONObject merchantConfig;
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

        sharedPreferences = getSharedPreferences(Preferences.SHARED_PREF_KEY, MODE_PRIVATE);
        try {
            merchantConfig = new JSONObject(this.getIntent().getStringExtra("merchantConfig"));
        } catch (JSONException e){
            e.printStackTrace();
        }
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
                    handleCreateTxnAndProcess(view);
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

    private void handleCreateTxnAndProcess(View view){
        new Thread() {
            @Override
            public void run() {
                JSONObject txn = new JSONObject();
                try {

                    txn = Utils.createTxnApi(view.getContext(), orderId, "1.00", "4000120000000045", "12", "2030","123", "abcdefgijk");
                } catch (Exception e){
                    e.printStackTrace();
                }
                hyperServices.process(ViesPayload.getPayPayload(sharedPreferences, requestId, view, txn));
            }
        }.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.configure:
                Intent intent = new Intent(ViesActivity.this, SettingsActivity.class);
                startActivityForResult(intent, SETTINGS_ACTIVITY_REQ_CODE, new Bundle());
                return true;
            case android.R.id.home:
                onBackPressed();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
