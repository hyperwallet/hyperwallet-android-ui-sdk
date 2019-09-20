package com.hyperwallet.android.ui.transfermethod.repository.poc.analytics.eventparam;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ClickEventParam extends EventParam {

    private Map<String, Object> mEventParams = new HashMap<>();

    private ClickEventParam() {
        mEventParams.put("t", "cl");
    }

    public ClickEventParam(@NonNull final String page, @NonNull final String link, @NonNull final String group) {
        this();
        mEventParams.put("page_name", page);
        mEventParams.put("link_name", link);
        mEventParams.put("page_group", group);
    }


    @Override
    public Map<String, Object> getEventParams() {
        return new HashMap<>(mEventParams);
    }

}
