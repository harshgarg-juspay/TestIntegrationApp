package in.juspay.testIntegrationApp.paymentPage;

import android.content.SharedPreferences;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;


import in.juspay.testIntegrationApp.Preferences;

public class Payload {

    public static String getTimeStamp() {
        return Long.toString(System.currentTimeMillis());
    }

    public static JSONObject generateSignaturePayload(SharedPreferences preferences) {
        JSONObject signaturePayload = new JSONObject();
        try {
            signaturePayload.put("first_name", preferences.getString("firstName", Preferences.firstName));
            signaturePayload.put("last_name", preferences.getString("lastName", Preferences.lastName));
            signaturePayload.put("mobile_number", preferences.getString("mobileNumber", Preferences.mobileNumber));
            signaturePayload.put("email_address", preferences.getString("emailAddress", Preferences.emailAddress));
            signaturePayload.put("customer_id", preferences.getString("customerId", Preferences.customerId));
            signaturePayload.put("timestamp", getTimeStamp());
            signaturePayload.put("merchant_id", preferences.getString("merchantId", Preferences.merchantId));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return signaturePayload;
    }

    public static JSONObject generateInitiatePayload(SharedPreferences preferences, JSONObject signaturePayload, String signature) {
        JSONObject initiatePayload = new JSONObject();
        try {
            initiatePayload.put("action", Preferences.initAction);
            initiatePayload.put("clientId", preferences.getString("clientId", Preferences.clientId));
            initiatePayload.put("merchantKeyId", preferences.getString("merchantKeyId", Preferences.merchantKeyId));
            initiatePayload.put("signaturePayload", signaturePayload.toString());
            initiatePayload.put("signature", signature);
            initiatePayload.put("environment", preferences.getString("environment", Preferences.environment));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return initiatePayload;
    }

    public static JSONObject generateOrderDetails(SharedPreferences preferences, String orderId) {
        JSONObject orderDetails = new JSONObject();
        try {
            orderDetails.put("order_id", orderId);
            orderDetails.put("first_name", preferences.getString("firstName", Preferences.firstName));
            orderDetails.put("last_name", preferences.getString("lastName", Preferences.lastName));
            orderDetails.put("mobile_number", preferences.getString("mobileNumber", Preferences.mobileNumber));
            orderDetails.put("email_address", preferences.getString("emailAddress", Preferences.emailAddress));
            orderDetails.put("customer_id", preferences.getString("customerId", Preferences.customerId));
            orderDetails.put("timestamp", getTimeStamp());
            orderDetails.put("merchant_id", preferences.getString("merchantId", Preferences.merchantId));
            orderDetails.put("amount", preferences.getString("amount", Preferences.amount));
            String mandateType = preferences.getString("mandateOption", Preferences.mandateOption);
            if (!mandateType.equals("None")) {
                orderDetails.put("options.create_mandate", mandateType);
                orderDetails.put("mandate_max_amount", preferences.getString("mandateMaxAmount", Preferences.mandateMaxAmount));
                orderDetails.put("metadata.PAYTM_V2:SUBSCRIPTION_EXPIRY_DATE", "2020-12-30");
                orderDetails.put("metadata.PAYTM_V2:SUBSCRIPTION_FREQUENCY_UNIT", "MONTH");
                orderDetails.put("metadata.PAYTM_V2:SUBSCRIPTION_FREQUENCY", "2");
                orderDetails.put("metadata.PAYTM_V2:SUBSCRIPTION_GRACE_DAYS", "0");

                Date c = Calendar.getInstance().getTime();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                String formattedDate = df.format(c);
                orderDetails.put("metadata.PAYTM_V2:SUBSCRIPTION_START_DATE", formattedDate);
            }
            orderDetails.put("return_url", Preferences.returnUrl);
            String desc =  "Get pro for Rs. 0.33/mo for 3 months";
            orderDetails.put("description", desc);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return orderDetails;
    }

    public static JSONObject generateProcessPayload(SharedPreferences preferences, String orderId, JSONObject orderDetails, String signature) {
        JSONObject processPayload = new JSONObject();
        try {
            processPayload.put("action", preferences.getString("action", Preferences.processAction));
            processPayload.put("merchantId", preferences.getString("merchantId", Preferences.merchantId));
            processPayload.put("clientId", preferences.getString("clientId", Preferences.clientId));
            processPayload.put("orderId", orderId);
            processPayload.put("amount", preferences.getString("amount", Preferences.amount));
            processPayload.put("customerId", preferences.getString("customerId", Preferences.customerId));
            processPayload.put("customerMobile", preferences.getString("mobileNumber", Preferences.mobileNumber));

            ArrayList<String> endUrlArr = new ArrayList<>(Arrays.asList(".*sandbox.juspay.in\\/thankyou.*", ".*sandbox.juspay.in\\/end.*", ".*localhost.*", ".*api.juspay.in\\/end.*"));
            JSONArray endUrls = new JSONArray(endUrlArr);

            processPayload.put("endUrls", endUrls);

            processPayload.put("merchantKeyId",preferences.getString("merchantKeyId", Preferences.merchantKeyId));
            processPayload.put("orderDetails", orderDetails.toString());
            processPayload.put("signature", signature);
            processPayload.put("language", preferences.getString("language", Preferences.language));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return processPayload;
    }

    public static JSONObject getPaymentsPayload(SharedPreferences preferences, String requestId, JSONObject payload) {
        JSONObject paymentsPayload = new JSONObject();
        try {
            paymentsPayload.put("requestId", requestId);
            paymentsPayload.put("service", preferences.getString("service", Preferences.service));
            paymentsPayload.put("payload", payload);
            paymentsPayload.put("betaAssets", preferences.getBoolean("betaAssets", Preferences.betaAssets));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return paymentsPayload;
    }

    public static String generateRequestId() {
        String[] uuid = UUID.randomUUID().toString().split("-");
        for (int i = 0; i < uuid.length; i++) {
            if (i % 2 != 0) {
                uuid[i] = uuid[i].toUpperCase();
            }
        }
        return TextUtils.join("-", uuid);
    }

    public static String generateOrderId() {
        return "R" + (long) (Math.random() * 10000000000L);
    }

}

