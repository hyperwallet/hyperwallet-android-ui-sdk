package com.hyperwallet.android.ui.transfermethod.repository.poc.analytics;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.hyperwallet.android.model.HyperwalletError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ErrorEventParam extends EventParam {


    private static final String ERFD = "erfd";
    private static final String ERROR_CODE = "error_code";
    private static final String ERROR_MESSAGE = "error_message";
    private static final String ERROR_TYPE = "error_type";


    private static final String ERROR_TYPE_FORM = "FORM";
    private static final String ERROR_TYPE_API = "API";

    private JSONObject eventParam = new JSONObject();


    public ErrorEventParam(@NonNull final HyperwalletError error) throws JSONException {
        if (!TextUtils.isEmpty(error.getFieldName())) {
            eventParam.put(ERFD, error.getFieldName());
        }
        if (!TextUtils.isEmpty(error.getCode())) {
            eventParam.put(ERROR_CODE, error.getCode());
        }
        if (!TextUtils.isEmpty(error.getMessage())) {
            eventParam.put(ERROR_MESSAGE, error.getMessage());
        }

        //TODO expose a method to get stack trace -> if stack trace present, then we have error EXCEPTION
        eventParam.put(ERROR_TYPE, ERROR_TYPE_API);


    }


    public ErrorEventParam(@NonNull final String field, @NonNull final String errorCode,
            @NonNull final String errorMessage) throws JSONException {
        eventParam.put(ERFD, field);
        eventParam.put(ERROR_CODE, errorCode);
        eventParam.put(ERROR_MESSAGE, errorMessage);
        eventParam.put(ERROR_TYPE, ERROR_TYPE_FORM);
    }


    //we could return the eventParam object too
    public JSONObject toJsonObject() throws JSONException {

        return new JSONObject(eventParam.toString());
    }


    @Override
    public JSONObject getEventParams() {
        return eventParam;
    }

}
