package com.hyperwallet.android.ui.transfermethod.repository.poc.analytics;

import androidx.annotation.NonNull;


import com.hyperwallet.android.model.HyperwalletErrors;
import com.hyperwallet.android.ui.transfermethod.repository.poc.analytics.event.Event;
import com.hyperwallet.android.ui.transfermethod.repository.poc.analytics.eventparam.ClickEventParam;
import com.hyperwallet.android.ui.transfermethod.repository.poc.analytics.eventparam.DeviceInfoEventParam;
import com.hyperwallet.android.ui.transfermethod.repository.poc.analytics.eventparam.ErrorEventParam;
import com.hyperwallet.android.ui.transfermethod.repository.poc.analytics.eventparam.EventParam;

import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class EventTracker {

    private final Executor mExecutor;
    private final String mUserToken;
    private final String mVisitId;


    public EventTracker(@NonNull String userToken) {
        mUserToken = userToken;
        mVisitId = UUID.randomUUID().toString();
        mExecutor = Executors.newSingleThreadExecutor();
    }


    public void trackClick(@NonNull final String page, @NonNull final String link, @NonNull final String group) {
        processEvent(new ClickEventParam(page, link, group));
    }


    public void trackError(@NonNull final HyperwalletErrors errors) {

        processEvent(new ErrorEventParam(errors.getErrors().get(0)));

    }


    private void processEvent(@NonNull final EventParam eventParam) {

        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Event event = new Event(mUserToken, mVisitId);
                EventParam deviceEventParam = new DeviceInfoEventParam(eventParam); //todo
                event.putEventParam(deviceEventParam);
                persistEvent(event);
            }
        });


    }


    private void persistEvent(@NonNull final Event event) {
        //todo decide if we need a class to handle it or if we use the repository here
    }

}
