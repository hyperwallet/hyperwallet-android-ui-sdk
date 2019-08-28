package com.hyperwallet.android.ui.transfermethod.repository.poc.analytics;

import java.util.HashMap;
import java.util.Map;

public class ClickEventParam extends EventParam {

    private Map<String, Object> mEventParams = new HashMap<>();

    public ClickEventParam() {
        mEventParams.put("t", "cl");
    }


    @Override
    public Map<String, Object> getEventParams() {
        return null;
    }

}
