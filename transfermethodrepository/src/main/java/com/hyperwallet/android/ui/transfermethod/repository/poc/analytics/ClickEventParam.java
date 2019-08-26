package com.hyperwallet.android.ui.transfermethod.repository.poc.analytics;

import org.json.JSONException;
import org.json.JSONObject;

public class ClickEventParam extends EventParam {

    private JSONObject mClickEventParams;

    public ClickEventParam() throws JSONException {
        mClickEventParams = new JSONObject();
        mClickEventParams.put("t", "cl");
    }


    @Override
    public JSONObject getEventParams() {
        return mClickEventParams;
    }

}
