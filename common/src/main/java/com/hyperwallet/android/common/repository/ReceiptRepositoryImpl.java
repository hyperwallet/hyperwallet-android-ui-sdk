package com.hyperwallet.android.common.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.hyperwallet.android.model.HyperwalletErrors;
import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod;

public class ReceiptRepositoryImpl implements ReceiptRepository {

    private final ReceiptDataSourceFactory mReceiptDataSourceFactory;
    private LiveData<ReceiptDataSource> mReceiptDataSource;
    private LiveData<PagedList<HyperwalletTransferMethod>> mReceipts;
    private LiveData<Boolean> mIsFetchingData;
    private LiveData<HyperwalletErrors> mHyperwalletErrors;


    public ReceiptRepositoryImpl(@NonNull final String receiptSourceToken) {
        mReceiptDataSourceFactory = new ReceiptDataSourceFactory(receiptSourceToken);
        mReceiptDataSource = mReceiptDataSourceFactory.getReceiptDataSource();
    }


    @Override
    public LiveData<Boolean> isFetchingReceiptList() {
        if (mIsFetchingData == null) {
            mIsFetchingData = mReceiptDataSource.getValue().isFetchingData();
        }
        return mIsFetchingData;
    }

    @Override
    public void retry() {
        if (mReceiptDataSource.getValue() != null) {
            mReceiptDataSource.getValue().retry();
        }

    }

    @Override
    public LiveData<HyperwalletErrors> getErrors() {
        if (mHyperwalletErrors == null) {
            mHyperwalletErrors = mReceiptDataSource.getValue().getErrors();
        }
        return mHyperwalletErrors;
    }

    @Override
    public LiveData<PagedList<HyperwalletTransferMethod>> getReceiptList() {
        if (mReceipts == null) {
            PagedList.Config config = new PagedList.Config.Builder()
                    .setPageSize(10)
                    .setEnablePlaceholders(false)
                    .setInitialLoadSizeHint(20)
                    .build();
            mReceipts = new LivePagedListBuilder<>(mReceiptDataSourceFactory, config).build();
        }
        return mReceipts;
    }

}
