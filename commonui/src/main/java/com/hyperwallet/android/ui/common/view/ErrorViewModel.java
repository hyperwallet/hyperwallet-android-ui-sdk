package com.hyperwallet.android.ui.common.view;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hyperwallet.android.model.Error;
import com.hyperwallet.android.ui.common.repository.Event;


import java.util.List;

public class ErrorViewModel extends ViewModel {

    private MutableLiveData<Event<List<Error>>> mErrors = new MutableLiveData<>();
    private MutableLiveData<Event<Void>> mRetryAction = new MutableLiveData<>();

    public void postErrors(@NonNull final List<Error> errors) {
        mErrors.setValue(new Event<>(errors));
    }

    public LiveData<Event<List<Error>>> getErrors() {
        return mErrors;
    }

    public MutableLiveData<Event<Void>> getRetryAction() {
        return mRetryAction;
    }


    public void postRetryAction() {
        mRetryAction.postValue(new Event<Void>(null));
    }

}
