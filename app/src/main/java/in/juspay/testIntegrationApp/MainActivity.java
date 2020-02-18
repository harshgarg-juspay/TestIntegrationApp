package in.juspay.testIntegrationApp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import in.juspay.godel.PaymentActivity;

public class MainActivity extends AppCompatActivity {

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

        Payload.setDefaultsIfNotPresent(getSharedPreferences(Payload.PayloadConstants.SHARED_PREF_KEY, MODE_PRIVATE));

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
        PaymentActivity.preFetch(this, Payload.PayloadConstants.clientId, Payload.PayloadConstants.betaAssets);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
