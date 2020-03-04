package in.juspay.testIntegrationApp.vies;

import android.content.SharedPreferences;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;

import in.juspay.hypersdk.core.PaymentConstants;
import in.juspay.testIntegrationApp.paymentPage.Payload;
import in.juspay.vies.Card;

public class ViesPayload {

    public static JSONObject generateInitiatePayload(SharedPreferences sharedPreferences) {
        JSONObject initiatePayload = new JSONObject();
        JSONObject viesFields = new JSONObject();
        try {
            initiatePayload.put(PaymentConstants.SERVICE, PaymentConstants.VIES_SERVICE);
            initiatePayload.put("requestId", Payload.generateRequestId());
            initiatePayload.put("betaAssets", sharedPreferences.getBoolean(PaymentConstants.BETA_ASSETS, Payload.PayloadConstants.betaAssets));
            viesFields.put("action", Payload.PayloadConstants.initAction);
            viesFields.put(PaymentConstants.MERCHANT_ID, sharedPreferences.getString(PaymentConstants.MERCHANT_ID, Payload.PayloadConstants.merchantId));
            viesFields.put(PaymentConstants.CLIENT_ID, sharedPreferences.getString(PaymentConstants.CLIENT_ID, Payload.PayloadConstants.clientId));
            viesFields.put(PaymentConstants.CUSTOMER_ID, sharedPreferences.getString(PaymentConstants.CUSTOMER_ID, Payload.PayloadConstants.customerId));
            viesFields.put(PaymentConstants.ENV, sharedPreferences.getString(PaymentConstants.ENV, Payload.PayloadConstants.environment));

            //VIES flow specific payload fields
            viesFields.put(PaymentConstants.TEST_MODE, sharedPreferences.getString(PaymentConstants.TEST_MODE, ViesPayload.PayloadConstants.testMode));
            viesFields.put(PaymentConstants.PACKAGE_NAME, sharedPreferences.getString(PaymentConstants.PACKAGE_NAME, ViesPayload.PayloadConstants.packageName));
            viesFields.put(PaymentConstants.SAFETYNET_API_KEY, sharedPreferences.getString(PaymentConstants.SAFETYNET_API_KEY, ViesPayload.PayloadConstants.safetyNetApiKey));
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
            request.put(PaymentConstants.SERVICE, PaymentConstants.VIES_SERVICE);
            request.put("requestId", Payload.generateRequestId());

            payload.put("action", "VIES_ELIGIBILITY");
            payload.put("amount", "1.00");

            card.setMaskedCard("40012****1212");
            card.setAlias("abcd");
            card.setBin("400012");

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
            request.put(PaymentConstants.SERVICE, PaymentConstants.VIES_SERVICE);
            request.put("requestId", Payload.generateRequestId());

            payload.put("action", "VIES_GET_MAX_AMOUNT");
            payload.put(PaymentConstants.SERVICE, PaymentConstants.VIES_SERVICE);//TODO: Need to check vies, why it is required.

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
            request.put(PaymentConstants.SERVICE, PaymentConstants.VIES_SERVICE);
            request.put("requestId", Payload.generateRequestId());

            card.setMaskedCard("40012****1212");
            card.setAlias("abcd");
            card.setBin("400012");

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
            request.put(PaymentConstants.SERVICE, PaymentConstants.VIES_SERVICE);
            request.put("requestId", Payload.generateRequestId());

            payload.put("action", "VIES_DELETE_CARD");
            card.setMaskedCard("40012****1212");
            card.setAlias("abcd");
            card.setBin("400012");

            payload.put("card", card.toJSON());
            request.put("payload", payload);
        } catch(Exception e){
            e.printStackTrace();
        }
        return  request;
    }



    public abstract class PayloadConstants {

        final public static String service = "in.juspay.vies";
        final public static String testMode = "false";
        final public static String packageName = "in.juspay.amazonpay";
        final public static String safetyNetApiKey = "AIzaSyCdQ4GoFpbcpc4uJy6PfKjtsSRM6yjiKWI";


    }
}
