package com.example.testintegrationapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Html;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class UiUtils {

    private static final String BASE_URL = "https://hyperwidget-ppconfig.herokuapp.com/faq/";

    public static void showMessageInModal(Context cont, String header, String message) {
        new MaterialAlertDialogBuilder(cont)
                .setTitle(header)
                .setMessage(message)
                .setPositiveButton("Ok", null)
                .show();
    }

    public static void generateCards(Context cont, Drawable image) {

        MaterialCardView materialCard = new MaterialCardView(cont);
        materialCard.setMinimumHeight(80);
        materialCard.setUseCompatPadding(true);

        LinearLayout childLayout = new LinearLayout(cont);
        childLayout.setOrientation(LinearLayout.HORIZONTAL);
        childLayout.setGravity(Gravity.CENTER_VERTICAL);

        ImageView img = new ImageView(cont);
        img.setImageDrawable(image);

        TextView message = new TextView(cont);
        message.setText("Item");
        message.setGravity(Gravity.CENTER);
        message.setTextSize(32);

        childLayout.addView(img);
        childLayout.addView(message);

        materialCard.addView(childLayout);

    }

    public static CharSequence getColoredText(String text, String color) {
        return Html.fromHtml("<font color='" + color + "'>" + text + "</font>");
    }

    public static CharSequence getWhiteText(String text) {
        return getColoredText(text, "#ffffff");
    }

    public static void openWebView(Context context, String path) {
        Intent i = new Intent(context, WebViewActivity.class);
        i.putExtra("path", path);
        context.startActivity(i);
    }

    public static void launchInCustomTab(Context context, String path) {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimary));
        CustomTabsIntent customTabsIntent = builder.build();
        Uri url = Uri.parse(BASE_URL + path);
        customTabsIntent.launchUrl(context, url);
    }
}
