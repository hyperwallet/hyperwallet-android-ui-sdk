package com.hyperwallet.android.ui.transfermethod.repository.poc.analytics.eventparam;

import androidx.annotation.NonNull;

import java.util.Map;

public class SdkInfoEventParam extends EventParamDecorator {


    private String mSdkVersion; //sdk_version -> does it make sense?

    private String mEnvironment; //hyperwallet_environment

    private String mSdkType; //product - ui sdk / core sdk

    private String mComponent; //component constant hyperwallet


    public SdkInfoEventParam(@NonNull final EventParam eventParam) {
        super(eventParam);
    }

    @Override
    public Map<String, Object> getEventParams() {
        return null;
    }
}
