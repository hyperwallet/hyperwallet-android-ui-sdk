package com.hyperwallet.android.ui.common;


import android.app.Application;

import com.squareup.leakcanary.InstrumentationLeakDetector;
import com.squareup.leakcanary.LeakCanary;

public class HyperwalletInstrumentedTestApplication extends Application {

    @Override
    public void onCreate() {

        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        installLeakCanary();
    }


    protected void installLeakCanary() {

        InstrumentationLeakDetector.instrumentationRefWatcher(this)
                .buildAndInstall();

    }

}