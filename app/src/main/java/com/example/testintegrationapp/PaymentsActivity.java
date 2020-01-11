package com.example.testintegrationapp;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

import in.juspay.godel.data.JuspayResponseHandler;
import in.juspay.godel.ui.HyperPaymentsCallbackAdapter;
import in.juspay.services.PaymentServices;

public class PaymentsActivity extends AppCompatActivity {

    // Variables for initiate
    private JSONObject signaturePayload;
    private JSONObject initiatePayload;

    private String initiateSignature;

    private boolean isSignaturePayloadSigned;
    private boolean isInitiateDone;
    private JSONObject initiateResult;

    private LinearLayout initiateLayout;


    // Variables for process
    private JSONObject orderDetails;
    private JSONObject processPayload;


    private String orderId;
    private String processSignature;

    private boolean isOrderIDGenerated;
    private boolean isOrderDetailsSigned;
    private boolean isProcessDone;
    private JSONObject processResult;

    private LinearLayout processLayout;

    // Payment services
    private PaymentServices juspayServices;
    private String requestId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payments);
        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setTitle(UiUtils.getWhiteText("Initiate"));
        WebView.setWebContentsDebuggingEnabled(true);

        generateSignaturePayload();

        initiateLayout = findViewById(R.id.initiateLayout);
        processLayout = findViewById(R.id.processLayout);

        initiateLayout.setVisibility(View.VISIBLE);
        processLayout.setVisibility(View.GONE);

        juspayServices = new PaymentServices(this);

        isSignaturePayloadSigned = false;
        isOrderIDGenerated = false;
        isOrderDetailsSigned = false;
        isInitiateDone = false;
        isProcessDone = false;
        requestId = Payload.generateRequestId();
        orderId = "";
        initiateSignature = "";
        initiateResult = new JSONObject();
        processSignature = "";
        processResult = new JSONObject();
    }

    // Initiate Functions

    public void generateSignaturePayload() {
        signaturePayload = Payload.generateSignaturePayload();
    }

    public void generateInitiatePayload() {
        initiatePayload = Payload.generateInitiatePayload(signaturePayload, initiateSignature);
    }

    public void signSignaturePayload(View view) {
        try {
            SignatureAPI signatureAPI = new SignatureAPI();
            initiateSignature = signatureAPI.execute(signaturePayload.toString()).get();
            isSignaturePayloadSigned = true;
            Toast.makeText(this, "Payload signed", Toast.LENGTH_SHORT).show();
            generateInitiatePayload();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void showInitiateSigningInput(View view) {
        try {
            UiUtils.showMessageInModal(this, "Signing Input", signaturePayload.toString(4));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void showInitiateSigningOutput(View view) {
        if (isSignaturePayloadSigned) {
            UiUtils.showMessageInModal(this, "Signing Output", initiateSignature);
        } else {
            Snackbar.make(view, "Please sign to see the output", Snackbar.LENGTH_SHORT).show();
        }
    }

    public void showSigningFAQ(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            UiUtils.launchInCustomTab(this, "signing");
        } else {
            UiUtils.openWebView(this, "signing");
        }
    }

    public void initiateJuspaySdk(View view) {
        try {
            if (isSignaturePayloadSigned) {
                JSONObject payload = Payload.getPaymentsPayload(requestId, initiatePayload);
                juspayServices.initiate(payload, new HyperPaymentsCallbackAdapter() {
                    @Override
                    public void onEvent(JSONObject data, JuspayResponseHandler juspayResponseHandler) {
                        Log.d("Inside OnEvent ", "initiate");
                        try {
                            String event = data.getString("event");
                            switch (event) {
                                case "initiate_result":
                                    isInitiateDone = true;
                                    initiateResult = data;
                                    Toast.makeText(PaymentsActivity.this, "Initiate Complete", Toast.LENGTH_SHORT).show();
                                    Log.wtf("initiate_result", data.toString());
                                    break;
                                case "process_result":
                                    isProcessDone = true;
                                    processResult = data;
                                    Objects.requireNonNull(getSupportActionBar()).show();
                                    processLayout.setVisibility(View.VISIBLE);
                                    Toast.makeText(PaymentsActivity.this, "Process Complete", Toast.LENGTH_SHORT).show();
                                    Log.wtf("process_result", data.toString());
                                    break;
                                default:
                                    Toast.makeText(PaymentsActivity.this, "Unknown Result", Toast.LENGTH_SHORT).show();
                                    UiUtils.showMessageInModal(PaymentsActivity.this, "Unknown Result", data.toString());
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
                Snackbar.make(view, "Please sign the payload", Snackbar.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showInitiateInput(View view) {
        try {
            if (isSignaturePayloadSigned) {
                JSONObject payload = Payload.getPaymentsPayload(requestId, initiatePayload);
                UiUtils.showMessageInModal(this, "Initiate Input", payload.toString(4));
            } else {
                Snackbar.make(view, "Sign the payload first", Snackbar.LENGTH_SHORT).show();
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            UiUtils.launchInCustomTab(this, "initiate");
        } else {
            UiUtils.openWebView(this, "initiate");
        }
    }

    // Process Functions

    public void generateOrderID(View view) {
        orderId = Payload.generateOrderId();
        isOrderIDGenerated = true;
        Toast.makeText(this, "Order ID Generated: " + orderId, Toast.LENGTH_LONG).show();
        generateOrderDetails();
    }

    public void showOrderIdFAQ(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            UiUtils.launchInCustomTab(this, "orderID");
        } else {
            UiUtils.openWebView(this, "orderID");
        }
    }

    public void generateOrderDetails() {
        orderDetails = Payload.generateOrderDetails(orderId);
    }

    public void signOrderDetails(View view) {
        if (isOrderIDGenerated) {
            try {
                SignatureAPI signatureAPI = new SignatureAPI();
                processSignature = signatureAPI.execute(orderDetails.toString()).get();
                isOrderDetailsSigned = true;
                Toast.makeText(this, "Signed Order Details", Toast.LENGTH_SHORT).show();
                generateProcessPayload();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            Snackbar.make(view, "Please generate an order id", Snackbar.LENGTH_SHORT).show();
        }
    }

    public void showProcessSigningInput(View view) {
        try {
            if (isOrderIDGenerated) {
                UiUtils.showMessageInModal(this, "Signing Input", orderDetails.toString(4));
            } else {
                Snackbar.make(view, "Generate an order id", Snackbar.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void showProcessSigningOutput(View view) {
        if (isOrderDetailsSigned) {
            UiUtils.showMessageInModal(this, "Signing Output", processSignature);
        } else {
            Snackbar.make(view, "Please sign to see the output", Snackbar.LENGTH_SHORT).show();
        }
    }

    public void generateProcessPayload() {
        processPayload = Payload.generateProcessPayload(orderId, orderDetails, processSignature);
    }

    public void showProcessInput(View view) {
        try {
            if (isOrderDetailsSigned) {
                JSONObject payload = Payload.getPaymentsPayload(requestId, processPayload);
                UiUtils.showMessageInModal(this, "Process Input", payload.toString(4));
            } else {
                Snackbar.make(view, "Please sign the order details first", Snackbar.LENGTH_SHORT).show();
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
        if (isOrderDetailsSigned) {
            JSONObject payload = Payload.getPaymentsPayload(requestId, processPayload);
            juspayServices.process(payload);
            Objects.requireNonNull(getSupportActionBar()).hide();
        } else {
            Snackbar.make(view, "Please sign the payload", Snackbar.LENGTH_SHORT).show();
        }
    }

    public void showProcessFAQ(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            UiUtils.launchInCustomTab(this, "process");
        } else {
            UiUtils.openWebView(this, "process");
        }
    }

    public void terminateJuspaySdk(View view) {
        juspayServices.terminate();
        Toast.makeText(this, "Juspay SDK terminated", Toast.LENGTH_LONG).show();

        isSignaturePayloadSigned = false;
        isInitiateDone = false;

        isOrderIDGenerated = false;
        isOrderDetailsSigned = false;
        isProcessDone = false;

        processLayout.setVisibility(View.GONE);
        initiateLayout.setVisibility(View.VISIBLE);
        Objects.requireNonNull(getSupportActionBar()).setTitle(UiUtils.getWhiteText("Initiate"));
    }

    public void showTerminateFAQ(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            UiUtils.launchInCustomTab(this, "terminate");
        } else {
            UiUtils.openWebView(this, "terminate");
        }
    }

    @Override
    public void onBackPressed() {
        boolean backPressHandled = juspayServices.onBackPressed();
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
            case android.R.id.home:
                onBackPressed();
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
