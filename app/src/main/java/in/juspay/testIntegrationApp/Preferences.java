package in.juspay.testIntegrationApp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class Preferences {
    private static final String PREFS_KEY = "demoAppPrefs";
    public static final String SHARED_PREF_KEY = "Configurations";

    public static ArrayList<String> merchantIdList;
    public static String merchantId = "flipkart_visa";
    public static String clientId = "flipkart_visa";
    public static boolean betaAssets = true;
    public static String testMode= "true";
    public static String apiVersion = "v1";
    //v2 order specific fields
    public static String merchantKeyId = "3164";
    public static String privateKey = "";
    final public static String signatureURL = "https://dry-cliffs-89916.herokuapp.com/sign-hyper-beta";
    public static String environment = "sandbox";
    //v1 order specific fields
    public static String juspayApiKey = "";

    //VIES specific fields
    public static String appId = "in.juspay.amazonpay"; //package name sent to visa server in vies
    public static String safetyNetApiKey = "";
    public static String useLocalEligibility = "true";

    //General order create specific fields
    public static String customerId = "cst_bwgmuiumgfjjblrc";
    public static String mobileNumber = "9876543210";
    final public static String firstName = "Test";
    final public static String lastName = "User";
    final public static String emailAddress = "test@juspay.in";
    final public static String mandateOption = "OPTIONAL";
    final public static String mandateMaxAmount = "1.0";
    final public static String amount = "1.0";
    final public static String returnUrl = "https://sandbox.juspay.in/end"; //-
    final public static String language = "english";
    public static String gwRefId = "";

    final public static String initAction = "initiate";
    final public static String processAction = "paymentPage";
    final public static String service = "in.juspay.hyperpay";
    public static JSONArray services;


    @SuppressLint("HardwareIds")
    public static String getAndroidId(Context ctx) {
        return Settings.Secure.getString(ctx.getContentResolver(), "android_id");
    }

    public static void setDefaultsIfNotPresent(SharedPreferences preferences) {
        SharedPreferences.Editor editor = preferences.edit();

        if (!preferences.contains("clientIdPrefetch")) {
            editor.putString("clientIdPrefetch", clientId);
        }
        if (!preferences.contains("betaAssetsPrefetch")) {
            editor.putBoolean("betaAssetsPrefetch", betaAssets);
        }

        if (!preferences.contains("firstName")) {
            editor.putString("firstName", firstName);
        }
        if (!preferences.contains("lastName")) {
            editor.putString("lastName", lastName);
        }
        if (!preferences.contains("mobileNumber")) {
            editor.putString("mobileNumber", mobileNumber);
        }
        if (!preferences.contains("emailAddress")) {
            editor.putString("emailAddress", emailAddress);
        }
        if (!preferences.contains("customerId")) {
            editor.putString("customerId", customerId);
        }
        if (!preferences.contains("amount")) {
            editor.putString("amount", amount);
        }
        if (!preferences.contains("language")) {
            editor.putString("language", language);
        }

        if (!preferences.contains("mandateOption")) {
            editor.putString("mandateOption", mandateOption);
        }
        if (!preferences.contains("mandateMaxAmount")) {
            editor.putString("mandateMaxAmount", mandateMaxAmount);
        }

        if (!preferences.contains("merchantId")) {
            editor.putString("merchantId", merchantId);
        }
        if (!preferences.contains("clientId")) {
            editor.putString("clientId", clientId);
        }
        if (!preferences.contains("service")) {
            editor.putString("service", service);
        }
        if (!preferences.contains("merchantKeyId")) {
            editor.putString("merchantKeyId", merchantKeyId);
        }
        if (!preferences.contains("signatureURL")) {
            editor.putString("signatureURL", signatureURL);
        }
        if (!preferences.contains("environment")) {
            editor.putString("environment", environment);
        }
        if (!preferences.contains("action")) {
            editor.putString("action", processAction);
        }
        if (!preferences.contains("betaAssets")) {
            editor.putBoolean("betaAssets", betaAssets);
        }

        editor.apply();
    }

    public static void updatePreferences(JSONObject conf) {
        try {
            Iterator<?> keys = conf.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                switch (key){
                    case "clientId":
                        clientId = conf.get(key).toString();
                        break;
                    case "apiVersion":
                        apiVersion = conf.get(key).toString();
                        break;
                    case "sandbox":
                        environment = "sandbox";
                        setEnvDetails(conf.toString(), "sandbox");
                        break;
                    case "production":
                        environment = "production";
                        setEnvDetails(conf.toString(), "production");
                        break;
                    case "betaAssets":
                        betaAssets = conf.getBoolean(key);
                        break;
                    case "services":
                        services = conf.getJSONArray(key);
                        break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static void setEnvDetails (String str, String env) {
        try {
            JSONObject merchantConf = new JSONObject(str);
            JSONObject conf = merchantConf.getJSONObject(env);
            Iterator<?> keys = conf.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                switch (key) {
                    case "apiKey":
                        juspayApiKey = conf.get(key).toString();
                        break;
                    case "merchantKeyId":
                        merchantKeyId = conf.get(key).toString();
                        break;
                    case "privateKey":
                        privateKey = conf.get(key).toString();
                    case "safetyNetApiKey":
                        safetyNetApiKey = conf.get(key).toString();
                        break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    static void readPrefs(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE);

        try {
            JSONObject json = new JSONObject(preferences.getString(PREFS_KEY, "{}"));

            merchantId = json.optString("merchantId", merchantId);
            clientId = merchantId + "_android";
            safetyNetApiKey = json.optString("safetyNetApiKey", safetyNetApiKey);
            juspayApiKey = json.optString("juspayApiKey", juspayApiKey);
            appId = json.optString("appId", appId);
            gwRefId = json.optString("gwRefId", gwRefId);
            environment = json.optString("environment", environment);
            testMode = json.optString("test_mode", testMode);
            useLocalEligibility = json.optString("useLocalEligibility", useLocalEligibility);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    //Before passing this list of params to other activity,
    static void writePrefs(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE);
        JSONObject json = new JSONObject();

        try {
            json.put("merchantId", merchantId);
            json.put("safetyNetApiKey", safetyNetApiKey);
            json.put("juspayApiKey", juspayApiKey);
            json.put("appId", appId);
            json.put("gwRefId", gwRefId);
            json.put("environment", environment);
            json.put("test_mode", testMode);
            json.put("useLocalEligibility", useLocalEligibility);

            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(PREFS_KEY, json.toString());
            editor.apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
