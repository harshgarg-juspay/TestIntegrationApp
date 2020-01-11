package com.example.testintegrationapp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

public class Payload {

    public static String getTimeStamp() {
        return Long.toString(System.currentTimeMillis());
    }

    public static JSONObject generateSignaturePayload() {
        JSONObject signaturePayload = new JSONObject();
        try {
            signaturePayload.put("first_name", PayloadConstants.firstName);
            signaturePayload.put("last_name", PayloadConstants.lastName);
            signaturePayload.put("mobile_number", PayloadConstants.mobileNumber);
            signaturePayload.put("email_address", PayloadConstants.emailAddress);
            signaturePayload.put("customer_id", PayloadConstants.customerId);
            signaturePayload.put("time_stamp", getTimeStamp());
            signaturePayload.put("merchant_id", PayloadConstants.merchantId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return signaturePayload;
    }

    public static JSONObject generateInitiatePayload(JSONObject signaturePayload, String signature) {
        JSONObject initiatePayload = new JSONObject();
        try {
            initiatePayload.put("action", PayloadConstants.init_action);
            initiatePayload.put("clientId", PayloadConstants.clientId);
            initiatePayload.put("merchantKeyId", PayloadConstants.merchantKeyId);
            initiatePayload.put("signaturePayload", signaturePayload.toString());
            initiatePayload.put("signature", signature);
            initiatePayload.put("environment", PayloadConstants.environment);
            initiatePayload.put("betaAssets", PayloadConstants.betaAssets);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return initiatePayload;
    }

    public static JSONObject generateOrderDetails(String orderId) {
        JSONObject orderDetails = new JSONObject();
        try {
            orderDetails.put("order_id", orderId);
            orderDetails.put("first_name", PayloadConstants.firstName);
            orderDetails.put("last_name", PayloadConstants.lastName);
            orderDetails.put("mobile_number", PayloadConstants.mobileNumber);
            orderDetails.put("email_address", PayloadConstants.emailAddress);
            orderDetails.put("customer_id", PayloadConstants.customerId);
            orderDetails.put("time_stamp", getTimeStamp());
            orderDetails.put("merchant_id", PayloadConstants.merchantId);
            orderDetails.put("amount", PayloadConstants.amount);
            orderDetails.put("return_url", PayloadConstants.returnUrl);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return orderDetails;
    }

    public static JSONObject generateProcessPayload(String orderId, JSONObject orderDetails, String signature) {
        JSONObject processPayload = new JSONObject();
        try {
            processPayload.put("action", PayloadConstants.process_action);
            processPayload.put("merchantId", PayloadConstants.merchantId);
            processPayload.put("clientId", PayloadConstants.clientId);
            processPayload.put("orderId", orderId);
            processPayload.put("amount", PayloadConstants.amount);
            processPayload.put("customerId", PayloadConstants.customerId);
            processPayload.put("customerMobile", PayloadConstants.mobileNumber);

            ArrayList<String> endUrlArr = new ArrayList<>(Arrays.asList(".*sandbox.juspay.in\\/thankyou.*", ".*sandbox.juspay.in\\/end.*", ".*localhost.*", ".*api.juspay.in\\/end.*"));
            JSONArray endUrls = new JSONArray(endUrlArr);

            processPayload.put("endUrls", endUrls);

            processPayload.put("merchantKeyId", PayloadConstants.merchantKeyId);
            processPayload.put("orderDetails", orderDetails.toString());
            processPayload.put("signature", signature);
            processPayload.put("language", PayloadConstants.language);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return processPayload;
    }

    public static JSONObject getPaymentsPayload(String requestId, JSONObject payload) {
        JSONObject paymentsPayload = new JSONObject();
        try {
            paymentsPayload.put("requestId", requestId);
            paymentsPayload.put("service", PayloadConstants.service);
            paymentsPayload.put("payload", payload);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return paymentsPayload;
    }

    public static String generateRequestId() {
        return "X" + (long) (Math.random() * 10000000000L);
    }

    public static String generateOrderId() {
        return "R" + (long) (Math.random() * 10000000000L);
    }

    public abstract class PayloadConstants {

        final public static String service = "in.juspay.hyperpay";

        final public static String mobileNumber = "9876543210";
        final public static String clientId = "idea_android";
        final public static String firstName = "Test";
        final public static String lastName = "User";
        final public static String emailAddress = "test@juspay.in";
        final public static String customerId = "9876543210";
        final public static String merchantId = "idea_preprod";

        final public static String init_action = "initiate";
        final public static String process_action = "quick_pay";
        final public static String merchantKeyId = "2980";
        final public static String environment = "sandbox";

        final public static String amount = "1.0";
        final public static String returnUrl = "https://sandbox.juspay.in/end";
        final public static String language = "english";

        final public static String signatureURL = "https://dry-cliffs-89916.herokuapp.com/sign-idea?payload=";

        final public static boolean betaAssets = true;

    }
}
