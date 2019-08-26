package com.hyperwallet.android.ui.transfermethod.repository.poc.analytics;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public class DeviceEventParam extends EventParamDecorator {

    private Map<String, Object> mParams = new HashMap<>();


    public DeviceEventParam(@NonNull final EventParam eventParam) {
        mParams.putAll(eventParam.getEventParams());
    }


    @Override
    public Map<String, Object> getEventParams() {
        return mParams;
    }


}
