package com.hyperwallet.android.ui.transfermethod.repository.poc.analytics.eventparam;

import androidx.annotation.NonNull;

import com.hyperwallet.android.ui.transfermethod.repository.poc.analytics.util.DeviceInfo;

import java.util.HashMap;
import java.util.Map;

public class DeviceInfoEventParam extends EventParamDecorator {

    private Map<String, Object> mParams = new HashMap<>();

    private DeviceInfo mInsightMetadata = DeviceInfo.getInstance();

    public DeviceInfoEventParam(@NonNull final EventParam eventParam) {
        super(eventParam);
        mParams.putAll(eventParam.getEventParams());
        mParams.put("sw", mInsightMetadata.getScreenWidth());
        mParams.put("sh", mInsightMetadata.getScreenHeight());

    }


    @Override
    public Map<String, Object> getEventParams() {
        return mParams;
    }


}
