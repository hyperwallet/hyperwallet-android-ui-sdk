package com.hyperwallet.android.ui.receipt;


import android.app.Application;

import java.util.Objects;

import leakcanary.AppWatcher;
import leakcanary.LeakCanary;

public class HyperwalletInstrumentedTestApplication extends Application {

    @Override
    public void onCreate() {

        super.onCreate();
        if (Objects.isNull(LeakCanary.getConfig())) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        installLeakCanary();
    }


    protected void installLeakCanary() {
        AppWatcher.INSTANCE.manualInstall(this);
    }

}