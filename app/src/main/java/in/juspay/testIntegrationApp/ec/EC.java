package in.juspay.testIntegrationApp.ec;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

import in.juspay.hypersdk.core.PaymentConstants;
import in.juspay.hypersdk.data.JuspayResponseHandler;
import in.juspay.hypersdk.ui.HyperPaymentsCallbackAdapter;
import in.juspay.services.HyperServices;
import in.juspay.testIntegrationApp.Payload;
import in.juspay.testIntegrationApp.PaymentsActivity;
import in.juspay.testIntegrationApp.R;
import in.juspay.testIntegrationApp.SettingsActivity;
import in.juspay.testIntegrationApp.UiUtils;
import in.juspay.testIntegrationApp.serverCalls.JuspayHTTPResponse;

import static in.juspay.testIntegrationApp.serverCalls.Api.generateOrder;

public class EC extends AppCompatActivity {
    private static final String LOG_TAG = "EC_ACTIVITY";

    private static final int SETTINGS_ACTIVITY_REQ_CODE = 421;
    private LinearLayout initiateLayout;
    private LinearLayout processLayout;
    private SharedPreferences preferences;
    private JSONObject signaturePayload;
    private JSONObject initiatePayload;

    private String initiateSignature;

    private boolean isOrderCreateDone = false;
    private boolean isInitiateDone;
    private JSONObject initiateResult;


    // Variables for process
    private JSONObject orderDetails;
    private JSONObject processPayload;


    private String orderId;
    private String processSignature;

    private boolean isOrderIDGenerated = false;
    private boolean isOrderDetailsSigned;
    private boolean isProcessDone;
    private JSONObject processResult;


    // Payment services
    private HyperServices hyperServices;
    private String requestId;
    private String signURL;
    private boolean isGodel = false;
    JSONObject merchantConfig;
    JSONObject customerConfig;
    JSONObject object = new JSONObject();
    JSONObject processObject = new JSONObject();
    JSONObject orderResponse = new JSONObject();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payments);

        isGodel = getIntent().getExtras().getBoolean("isGodel",false);

        WebView.setWebContentsDebuggingEnabled(true);

        preferences = getSharedPreferences(Payload.PayloadConstants.SHARED_PREF_KEY, MODE_PRIVATE);

        prepareUI();
        try {
            prepareInitiatePayload();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        hyperServices = new HyperServices(this);
    }

    public void initiateJuspaySdk(View view){
        initiateECSdk();
    }


    private void prepareUI() {
        initiateLayout = findViewById(R.id.initiateLayout);
        processLayout = findViewById(R.id.processLayout);

        initiateLayout.setVisibility(View.VISIBLE);
        processLayout.setVisibility(View.GONE);

        CardView signInitCard = findViewById(R.id.signInitCard);
        signInitCard.setVisibility(View.GONE);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(UiUtils.getWhiteText("Initiate"));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void prepareInitiatePayload() throws JSONException {
        merchantConfig = new JSONObject();
        merchantConfig.put(PaymentConstants.SERVICE,"in.juspay.ec");
        merchantConfig.put(PaymentConstants.MERCHANT_ID,"bb_instant");
        merchantConfig.put(PaymentConstants.CLIENT_ID,"bb_instant");
        merchantConfig.put(PaymentConstants.ENV,"sandbox");

        customerConfig = new JSONObject();
        customerConfig.put(PaymentConstants.CUSTOMER_ID,"123123123");

        hyperServices = new HyperServices(this);


        object.put("requestId",new Random().nextInt(10000)+"");
        object.put("betaAssets",true);
        object.put(PaymentConstants.SERVICE,merchantConfig.getString(PaymentConstants.SERVICE));
        JSONObject payload = new JSONObject();

        payload.put("action","initiate");
        payload.put("merchantId", merchantConfig.getString(PaymentConstants.MERCHANT_ID));
        payload.put("clientId", merchantConfig.getString(PaymentConstants.CLIENT_ID));
        payload.put("customerId", customerConfig.getString(PaymentConstants.CUSTOMER_ID));
        payload.put(PaymentConstants.ENV, merchantConfig.getString(PaymentConstants.ENV));
        object.put(PaymentConstants.PAYLOAD,payload);
    }


    public void initiateECSdk(){
        hyperServices.initiate(object, new HyperPaymentsCallbackAdapter() {
            @Override
            public void onEvent(JSONObject data, JuspayResponseHandler handler) {
                try {
                    String event = data.getString("event");
                    switch (event) {
                        case "initiate_result":
                            isInitiateDone = true;
                            initiateResult = data;
                            Toast.makeText(EC.this, "Initiate Complete", Toast.LENGTH_SHORT).show();
                            Log.wtf("initiate_result", data.toString());
                            break;
                        case "process_result":
                            isProcessDone = true;
                            processResult = data;
                            Objects.requireNonNull(getSupportActionBar()).show();
                            processLayout.setVisibility(View.VISIBLE);
                            Toast.makeText(EC.this, "Process Complete", Toast.LENGTH_SHORT).show();
                            Log.wtf("process_result", data.toString());
                            break;
                        default:
                            Toast.makeText(EC.this, "Unknown Result", Toast.LENGTH_SHORT).show();
                            UiUtils.showMessageInModal(EC.this, "Unknown Result", data.toString());
                            Log.wtf(event, data.toString());
                            break;
                    }
                } catch (Exception e) {
                    Log.d("Came here", e.toString());
                    e.printStackTrace();
                }

                Log.d("EC-ACTIVITY", "onEvent: " + data.toString());
            }
        });
    }

    public void showInitiateInput(View view) {
        try {
            UiUtils.showMessageInModal(this, "Signing Input", object.toString(4));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void showInitiateOutput(View view) {
        try {
            if (isInitiateDone) {
                UiUtils.showMessageInModal(this, "Signing Output", initiateResult.toString(4));
            }else{
                Snackbar.make(view, "Please Complete Initiate", Snackbar.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void startProcessActivity(View view) {
        if (isInitiateDone) {
            Objects.requireNonNull(getSupportActionBar()).setTitle(UiUtils.getWhiteText("Process"));
            initiateLayout.setVisibility(View.GONE);

//            CardView signProcessCard = findViewById(R.id.signProcessCard);
//            signProcessCard.setVisibility(View.GONE);
            CardView createOrderCard = findViewById(R.id.createOrderCard);
            createOrderCard.setVisibility(View.VISIBLE);

            processLayout.setVisibility(View.VISIBLE);

        } else {
            Snackbar.make(view, "Please Complete Initiate", Snackbar.LENGTH_SHORT).show();
        }

    }

    public void showCreateOrderFAQ(View view){

    }

    public void generateOrderID(View view) {
        orderId = Payload.generateOrderId();
        isOrderIDGenerated = true;
        Toast.makeText(this, "Order ID Generated: " + orderId, Toast.LENGTH_LONG).show();
        generateOrderDetails();
    }

    public void generateOrderDetails() {
        orderDetails = Payload.generateOrderDetails(preferences, orderId);
        isOrderIDGenerated = true;
    }

    public void copyOrderID(View view) {
        if (isOrderIDGenerated) {
            UiUtils.copyToClipBoard(this, "OrderID", orderId);
            Toast.makeText(this, "OrderID copied to clipboard: " + orderId, Toast.LENGTH_SHORT).show();
        } else {
            Snackbar.make(view, "Generate an order id", Snackbar.LENGTH_SHORT).show();
        }
    }
    public void createOrder(View view) {
        if (isOrderIDGenerated) {
            prepareProcess(view);
        } else {
            Snackbar.make(view, "Generate an order id", Snackbar.LENGTH_SHORT).show();
        }
    }

    public void showOrderCreateInput(View view){
        if (isOrderIDGenerated) {
            UiUtils.showMessageInModal(this, "orderCreate output", orderDetails.toString());
        } else {
            Snackbar.make(view, "Generate an order id", Snackbar.LENGTH_SHORT).show();
        }

    }

    public void showOrderCreateOutput(View view){
        if (isOrderCreateDone) {
            UiUtils.showMessageInModal(this, "orderCreate output", orderResponse.toString());
        } else {
            Snackbar.make(view, "Please create order to see the output", Snackbar.LENGTH_SHORT).show();
        }

    }

    public void processJuspaySdk(View view) {
        if(isOrderCreateDone) {
            hyperServices.process(processObject);
        }else {
            Snackbar.make(view, "Please create order to process", Snackbar.LENGTH_SHORT).show();
        }
    }

    public void showProcessInput(View view) throws JSONException {
        if(isOrderCreateDone) {
            UiUtils.showMessageInModal(this, "Process Input", processObject.toString(4));
        }else{
            Snackbar.make(view, "Please create order to get process input", Snackbar.LENGTH_SHORT).show();
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

    public void prepareProcess(View view) {
        new Thread() {

            @Override
            public void run() {
                JuspayHTTPResponse response = generateOrder(orderDetails, "8BA7D6345A7475C92B1D7194F61F9A", "sandbox");
                ArrayList<String> urls = new ArrayList<>();
                urls.add("sandbox.juspay.in/end");
                JSONObject payload = new JSONObject();
                try {
                    orderResponse = new JSONObject(response.responsePayload);
                    payload.put("action", "startJuspaySafe");
                    payload.put("orderId", "123123");
                    payload.put("url", orderResponse.getJSONObject("payment_links").getString("iframe" ));
                    payload.put("endUrls", new JSONArray(urls));
                    processObject.put("payload", payload);
                    processObject.put("requestId", new Random().nextInt(10000) + "");
                    processObject.put("service", "in.juspay.ec");
                    isOrderCreateDone = true;
                    Snackbar.make(view,"order created",Snackbar.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.start();
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
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivityForResult(intent, SETTINGS_ACTIVITY_REQ_CODE, new Bundle());
                return true;
            case android.R.id.home:
                onBackPressed();
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
