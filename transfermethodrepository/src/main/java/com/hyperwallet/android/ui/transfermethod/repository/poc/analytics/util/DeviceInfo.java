package com.hyperwallet.android.ui.transfermethod.repository.poc.analytics.util;

import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;

import androidx.annotation.NonNull;

public class DeviceInfo {

    private static DeviceInfo sInstance;

    private static final String sUserAgent = System.getProperty("http.agent");
    private static final String sDeviceName = Build.MANUFACTURER + " " + Build.MODEL;

    private int mScreenHeight;
    private int mScreenWidth;


    private DeviceInfo() {}


    public static synchronized DeviceInfo getInstance() {
        if (sInstance == null) {
            sInstance = new DeviceInfo();
        }
        return sInstance;
    }


    public synchronized void computeDeviceInfo(@NonNull final Context context) {
        mScreenWidth = calculateScreenWidth(context);
        mScreenHeight = calculateScreenHeight(context);
    }



    public int getScreenWidth() {
        return mScreenWidth;
    }

    public int getScreenHeight() {
        return mScreenHeight;
    }

    public String getUserAgent() {
        return sUserAgent;
    }

    public String getDeviceName() {
        return sDeviceName;
    }

    private int calculateScreenWidth(@NonNull final Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int widthInDP = context.getResources().getConfiguration().screenWidthDp;
        return dpToPx(displayMetrics, widthInDP);
    }

    private int calculateScreenHeight(@NonNull final Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int heightInDP = context.getResources().getConfiguration().screenHeightDp;
        return dpToPx(displayMetrics, heightInDP);
    }

    private int dpToPx(@NonNull final DisplayMetrics displayMetrics, int dp) {
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

}
