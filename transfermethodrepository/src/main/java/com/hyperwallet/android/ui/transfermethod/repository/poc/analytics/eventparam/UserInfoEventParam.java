package com.hyperwallet.android.ui.transfermethod.repository.poc.analytics.eventparam;

import androidx.annotation.NonNull;

import java.util.Map;

public class UserInfoEventParam extends EventParamDecorator {


    public UserInfoEventParam(@NonNull final EventParam eventParam) {
        super(eventParam);
    }

    @Override
    public Map<String, Object> getEventParams() {
        return null;
    }
}
