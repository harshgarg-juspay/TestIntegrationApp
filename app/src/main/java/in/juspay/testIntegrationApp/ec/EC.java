package in.juspay.testIntegrationApp.ec;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
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
import in.juspay.testIntegrationApp.R;
import in.juspay.testIntegrationApp.UiUtils;

public class EC extends AppCompatActivity {
    private LinearLayout initiateLayout;
    private LinearLayout processLayout;
    private SharedPreferences preferences;
    private JSONObject signaturePayload;
    private JSONObject initiatePayload;

    private String initiateSignature;

    private boolean isSignaturePayloadSigned;
    private boolean isInitiateDone;
    private JSONObject initiateResult;


    // Variables for process
    private JSONObject orderDetails;
    private JSONObject processPayload;


    private String orderId;
    private String processSignature;

    private boolean isOrderIDGenerated;
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
        CardView signInitCard = findViewById(R.id.signInitCard);
        signInitCard.setVisibility(View.GONE);
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

    public void startProcessActivity(View view) throws JSONException {
        if (isInitiateDone) {
            Objects.requireNonNull(getSupportActionBar()).setTitle(UiUtils.getWhiteText("Process"));
            initiateLayout.setVisibility(View.GONE);
            processLayout.setVisibility(View.VISIBLE);
            prepareProcess();
        } else {
            Snackbar.make(view, "Please Complete Initiate", Snackbar.LENGTH_SHORT).show();
        }

    }

    public void processJuspaySdk(View view) {
        hyperServices.process(processObject);
    }

    public void showProcessInput(View view) throws JSONException {
        UiUtils.showMessageInModal(this, "Process Input", processObject.toString(4));
    }

    public void prepareProcess() throws JSONException {
        ArrayList<String> urls = new ArrayList<String>();
        urls.add("juspay.n");
        JSONObject payload = new JSONObject();
        payload.put("action","startJuspaySafe");
        payload.put("orderId","123123");
        payload.put("url","www.google.com");
        payload.put("endUrls",new JSONArray(urls));
        processObject.put("payload",payload);
        processObject.put("requestId", new Random().nextInt(10000) + "");
        processObject.put("service","in.juspay.ec");
    }

}
