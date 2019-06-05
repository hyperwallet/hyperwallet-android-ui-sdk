package com.hyperwallet.android.common.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;

import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.listener.HyperwalletListener;
import com.hyperwallet.android.model.paging.HyperwalletPageList;
import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod;
import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethodPagination;


public class ReceiptDataSourceFactory extends DataSource.Factory {

    private final DataSource mDataSource;
    private final MutableLiveData<ReceiptDataSource> mDataSourceMutableLiveData;

    public ReceiptDataSourceFactory(@NonNull final String receiptSourceToken) {
        super();
        mDataSource = new ReceiptDataSource(getReceiptProvider(receiptSourceToken));
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


    private static ReceiptProvider getReceiptProvider(@NonNull final String receiptSourceToken) {
        if (receiptSourceToken.startsWith("usr-")) {
            return new UserReceiptProvider(receiptSourceToken);
        } else if (receiptSourceToken.startsWith("trm-")) {
            return new PrepaidCardReceiptProvider(receiptSourceToken);
        } else {
            throw new IllegalArgumentException("Receipt source not supported " + receiptSourceToken);
        }
    }

}
