package com.hyperwallet.android.receipt.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.PageKeyedDataSource;

import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.model.HyperwalletErrors;
import com.hyperwallet.android.model.receipt.Receipt;

import java.util.Date;

abstract class ReceiptDataSource<Key, Value> extends PageKeyedDataSource<Key, Value> {

    final MutableLiveData<HyperwalletErrors> mErrors = new MutableLiveData<>();
    final MutableLiveData<Boolean> mIsFetchingData = new MutableLiveData<>();
    LoadInitialCallback<Key, Value> mLoadInitialCallback;
    LoadInitialParams<Key> mLoadInitialParams;
    LoadCallback<Key, Value> mLoadAfterCallback;
    LoadParams<Key> mLoadAfterParams;

    public LiveData<Boolean> isFetchingData() {
        return mIsFetchingData;
    }

    public LiveData<HyperwalletErrors> getErrors() {
        return mErrors;
    }

    void retry() {
        if (mLoadInitialCallback != null) {
            loadInitial(mLoadInitialParams, mLoadInitialCallback);
        } else if (mLoadAfterCallback != null) {
            loadAfter(mLoadAfterParams, mLoadAfterCallback);
        }
    }

    Hyperwallet getHyperwallet() {
        return Hyperwallet.getDefault();
    }

}
