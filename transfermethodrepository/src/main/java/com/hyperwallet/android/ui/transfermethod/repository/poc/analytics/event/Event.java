package com.hyperwallet.android.ui.transfermethod.repository.poc.analytics.event;

import androidx.annotation.NonNull;

import com.hyperwallet.android.ui.transfermethod.repository.poc.analytics.eventparam.EventParam;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class Event {

    private JSONObject mRoot = new JSONObject();
    private JSONArray mEventParams = new JSONArray();

    public Event(@NonNull final String visitorId, @NonNull final String visitId) {
        try {
            mRoot.put("channel", "MOBILE");
            addActor(visitorId, visitId);
            addHttpParams();
        } catch (JSONException e) {
            mRoot = null;
            throw new RuntimeException();
        }
    }


    public void putEventParam(EventParam eventParam) {
        JSONObject jsonObject = new JSONObject();;
        for (Map.Entry<String, Object> entry: eventParam.getEventParams().entrySet()) {
            try {
                jsonObject.put(entry.getKey(), entry.getValue());
            } catch (JSONException e) {
                e.printStackTrace(); //todo
            }
        }
        mEventParams.put(jsonObject);
    }


    private void addActor(@NonNull final String visitorId, @NonNull final String visitId) throws JSONException {
        JSONObject actor = new JSONObject();
        actor.put("tracking_visitor_id", visitorId);
        actor.put("tracking_visit_id", visitId);
        mRoot.put("actor", actor);
    }


    private void addHttpParams() throws JSONException {
        JSONObject httpParams = new JSONObject();
        httpParams.put("accept_charset", "UTF-8");
        httpParams.put("accept_lang", "en");
        httpParams.put("user_agent", System.getProperty("http.agent"));
        mRoot.put("http_params", httpParams);
    }


}
