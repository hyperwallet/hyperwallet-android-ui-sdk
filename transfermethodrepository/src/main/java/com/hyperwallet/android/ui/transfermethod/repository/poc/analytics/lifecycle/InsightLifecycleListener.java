package com.hyperwallet.android.ui.transfermethod.repository.poc.analytics.lifecycle;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.hyperwallet.android.ui.transfermethod.repository.poc.analytics.util.DeviceInfo;

/*
    Implementing Application.ActivityLifecycleCallbacks instead of LifecycleObserver because we

 */

public class InsightLifecycleListener implements LifecycleObserver {

    private DeviceInfo mInsightMetadata = DeviceInfo.getInstance();
    private Context mContext;

    public InsightLifecycleListener(@NonNull final Context context) {
        mContext = context;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume() {
        mInsightMetadata.computeDeviceInfo(mContext);
    }


    public void dispose() {
        mContext = null;
    }


}
