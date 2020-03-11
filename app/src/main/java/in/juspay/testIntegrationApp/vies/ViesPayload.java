package in.juspay.testIntegrationApp.vies;

import android.content.SharedPreferences;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;

import in.juspay.hypersdk.core.PaymentConstants;
import in.juspay.testIntegrationApp.Preferences;
import in.juspay.testIntegrationApp.paymentPage.Payload;
import in.juspay.vies.Card;

public class ViesPayload {

    public static JSONObject generateInitiatePayload(SharedPreferences sharedPreferences) {
        JSONObject initiatePayload = new JSONObject();
        JSONObject viesFields = new JSONObject();
        try {
            initiatePayload.put(PaymentConstants.SERVICE, "in.juspay.vies");
            initiatePayload.put("requestId", Payload.generateRequestId());
            initiatePayload.put("betaAssets", sharedPreferences.getBoolean(PaymentConstants.BETA_ASSETS, Preferences.betaAssets));
            viesFields.put("action", Preferences.initAction);
            viesFields.put(PaymentConstants.MERCHANT_ID, sharedPreferences.getString(PaymentConstants.MERCHANT_ID, Preferences.merchantId));
            viesFields.put(PaymentConstants.CLIENT_ID, sharedPreferences.getString(PaymentConstants.CLIENT_ID, Preferences.clientId));
            viesFields.put(PaymentConstants.CUSTOMER_ID, sharedPreferences.getString(PaymentConstants.CUSTOMER_ID, Preferences.customerId));
            viesFields.put(PaymentConstants.ENV, sharedPreferences.getString(PaymentConstants.ENV, Preferences.environment));

            //VIES flow specific payload fields
            viesFields.put(PaymentConstants.TEST_MODE, sharedPreferences.getString(PaymentConstants.TEST_MODE, Preferences.testMode));
            viesFields.put(PaymentConstants.PACKAGE_NAME, sharedPreferences.getString(PaymentConstants.PACKAGE_NAME, Preferences.appId));
            viesFields.put(PaymentConstants.SAFETYNET_API_KEY, sharedPreferences.getString(PaymentConstants.SAFETYNET_API_KEY, Preferences.safetyNetApiKey));
            initiatePayload.put("payload", viesFields);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return initiatePayload;
    }

    public static JSONObject getEligibilityPayload(SharedPreferences sharedPreferences, String requestId, View view) {
        JSONObject request = new JSONObject();
        JSONObject payload = new JSONObject();
        JSONArray cardArray = new JSONArray();
        Card card = new Card();
        try {
            request.put(PaymentConstants.SERVICE, "in.juspay.vies");
            request.put("requestId", Payload.generateRequestId());

            payload.put("action", "VIES_ELIGIBILITY");
            payload.put("amount", "1.00");

            card.setMaskedCard("4012****1212");
            card.setAlias("abcd");
            card.setBin("401200");

            JSONObject cardObj = card.toJSON();
            cardArray.put(cardObj);
            payload.put("cards",cardArray);
            request.put("payload", payload);
        } catch(Exception e){
            e.printStackTrace();
        }
        return  request;
    }


    public static JSONObject getMaxAmountPayload(SharedPreferences sharedPreferences, String requestId, View view) {
        JSONObject request = new JSONObject();
        JSONObject payload = new JSONObject();
        try {
            request.put(PaymentConstants.SERVICE, "in.juspay.vies");
            request.put("requestId", Payload.generateRequestId());

            payload.put("action", "VIES_GET_MAX_AMOUNT");
            payload.put(PaymentConstants.SERVICE, "in.juspay.vies");//TODO: Need to check vies, why it is required.

            request.put("payload", payload);
        } catch(Exception e){
            e.printStackTrace();
        }
        return  request;
    }

    public static JSONObject getDeenrollCardPayload(SharedPreferences sharedPreferences, String requestId, View view, String sessionToken) {
        JSONObject request = new JSONObject();
        JSONObject payload = new JSONObject();
        Card card = new Card();
        try {
            request.put(PaymentConstants.SERVICE, "in.juspay.vies");
            request.put("requestId", Payload.generateRequestId());

            card.setMaskedCard("4012****1212");
            card.setAlias("abcd");
            card.setBin("401200");

            payload.put("action", "VIES_DISENROLL");
            payload.put("card", card.toJSON());
            payload.put("session_token", sessionToken);
            request.put("payload", payload);
        } catch(Exception e){
            e.printStackTrace();
        }
        return  request;
    }

    public static JSONObject getDeleteCardPayload(SharedPreferences sharedPreferences, String requestId, View view) {
        JSONObject request = new JSONObject();
        JSONObject payload = new JSONObject();
        Card card = new Card();
        try {
            request.put(PaymentConstants.SERVICE, "in.juspay.vies");
            request.put("requestId", Payload.generateRequestId());

            payload.put("action", "VIES_DELETE_CARD");
            card.setMaskedCard("4012****1212");
            card.setAlias("abcd");
            card.setBin("401200");

            payload.put("card", card.toJSON());
            request.put("payload", payload);
        } catch(Exception e){
            e.printStackTrace();
        }
        return  request;
    }

    public static JSONObject getPayPayload(SharedPreferences sharedPreferences, String requestId, View view, JSONObject txn) {
        JSONObject request = new JSONObject();
        JSONObject payload = new JSONObject();
        Card card = new Card();
        try {
            request.put(PaymentConstants.SERVICE, "in.juspay.vies");
            request.put("requestId", Payload.generateRequestId());

            payload.put("action", "VIES_DELETE_CARD");
            card.setMaskedCard("4012****1212");
            card.setAlias("abcd");
            card.setBin("401200");

            payload.put("card", card.toJSON());


            ArrayList<String> urls = new ArrayList<String>();
            urls.add("juspay.in/end");

            payload.put("action", "VIES_PAY");
            payload.put("amount", "1.00");
            payload.put("card", card.toJSON());
            payload.put("juspay_txn_resp", txn);
//            request.put("merchant_root_view", String.valueOf(R.id.mainScreenView));
            payload.put("end_urls_regexes", new JSONArray(urls));

            // TODO : Add this to use safety net.
            payload.put("safetynet_api_key", Preferences.safetyNetApiKey);


            request.put("payload", payload);
        } catch(Exception e){
            e.printStackTrace();
        }
        return  request;
    }

}
