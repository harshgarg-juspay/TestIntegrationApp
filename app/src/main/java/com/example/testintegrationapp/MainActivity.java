package com.example.testintegrationapp;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import java.util.Objects;

import in.juspay.godel.PaymentActivity;

public class MainActivity extends AppCompatActivity {

    private boolean isPrefetchDone;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.isPrefetchDone = false;

        Objects.requireNonNull(getSupportActionBar()).setTitle(UiUtils.getWhiteText("Home Page"));

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        WebView.setWebContentsDebuggingEnabled(true);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            UiUtils.launchInCustomTab(this, "prefetch");
        } else {
            UiUtils.openWebView(this, "prefetch");
        }
    }

//    public void showCaseDemo(View view){
//        try {
//            ViewTarget target = new ViewTarget(R.id.show_case_btn, this);
//            new ShowcaseView.Builder(this)
//                    .setStyle(R.style.CustomShowcaseTheme2)
//                    .setTarget(target)
//                    .setContentTitle("Juspay Function Caller")
//                    .setContentText("Use this button to do Prefetch, Initiate & Process Calls")
//                    .hideOnTouchOutside()
//                    .build();
//        } catch (Exception e){
//            e.printStackTrace();
//            Toast.makeText(this, "Error", Toast.LENGTH_LONG).show();
//        }
//    }

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
