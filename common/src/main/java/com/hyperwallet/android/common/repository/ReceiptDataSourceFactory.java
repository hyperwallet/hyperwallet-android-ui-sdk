package com.hyperwallet.android.common.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;


public class ReceiptDataSourceFactory extends DataSource.Factory {

    private final DataSource mDataSource;
    private final MutableLiveData<ReceiptDataSource> mDataSourceMutableLiveData;

    public ReceiptDataSourceFactory() {
        super();
        mDataSource = new ReceiptDataSource();
        mDataSourceMutableLiveData = new MutableLiveData<>();
        mDataSourceMutableLiveData.postValue((ReceiptDataSource)mDataSource);
        mDataSourceMutableLiveData.setValue((ReceiptDataSource)mDataSource);
    }

    public LiveData<ReceiptDataSource> getReceiptDataSource() {
        return mDataSourceMutableLiveData;
    }

    @NonNull
    @Override
    public DataSource create() {
        return mDataSource;
    }

}
