package in.juspay.testIntegrationApp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import in.juspay.godel.PaymentActivity;

public class MainActivity extends AppCompatActivity {

    private static final int SETTINGS_ACTIVITY_REQ_CODE = 69;

    private SharedPreferences preferences;
    private boolean isPrefetchDone;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.isPrefetchDone = false;

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(UiUtils.getWhiteText("Home Page"));
        }

        WebView.setWebContentsDebuggingEnabled(true);

        preferences = getSharedPreferences(Payload.PayloadConstants.SHARED_PREF_KEY, MODE_PRIVATE);
        Payload.setDefaultsIfNotPresent(preferences);

    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", (dialog, which) -> MainActivity.super.onBackPressed())
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }


    public void startInitiateActivity(View view){
        if(isPrefetchDone) {
            Intent initiateIntent = new Intent(this, PaymentsActivity.class);
            startActivity(initiateIntent);
        } else {
            Snackbar.make(view, "Please Complete prefetch", Snackbar.LENGTH_SHORT).show();
        }
    }

    public void startPrefetch(View view){

        String clientId = preferences.getString("clientIdPrefetch", Payload.PayloadConstants.clientId);
        boolean useBetaAssets = preferences.getBoolean("betaAssetsPrefetch", Payload.PayloadConstants.betaAssets);

        PaymentActivity.preFetch(this, clientId, useBetaAssets);

        this.isPrefetchDone = true;
        Toast.makeText(this, "Prefetch Started", Toast.LENGTH_SHORT).show();
    }

    public void showPrefetchInput(View view){
        UiUtils.showMessageInModal(this, "Prefetch","boolean useBetaAssets = false;\nString clientId =  \"clientId\";\n\nPaymentActivity.preFetch(activity, clientId,  useBetaAssets);");
    }

    public void showPrefetchFAQ(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            UiUtils.launchInCustomTab(this, "prefetch");
        } else {
            UiUtils.openWebView(this, "prefetch");
        }
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
                Intent intent = new Intent(MainActivity.this, ConfigureActivity.class);
                startActivityForResult(intent, SETTINGS_ACTIVITY_REQ_CODE, new Bundle());
                return true;
            case android.R.id.home:
                onBackPressed();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == SETTINGS_ACTIVITY_REQ_CODE) {
            if (data != null) {
                if (data.hasExtra("changed") && data.getBooleanExtra("changed", false)) {
                    Toast.makeText(this, "Resetting due to change in parameters", Toast.LENGTH_SHORT).show();
                    isPrefetchDone = false;
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
