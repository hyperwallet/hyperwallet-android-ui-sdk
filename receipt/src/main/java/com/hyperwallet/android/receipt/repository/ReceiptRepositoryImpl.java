/*
 * The MIT License (MIT)
 * Copyright (c) 2019 Hyperwallet Systems Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.hyperwallet.android.receipt.repository;

import androidx.lifecycle.LiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.hyperwallet.android.model.HyperwalletErrors;
import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod;

public class ReceiptRepositoryImpl implements ReceiptRepository {

    private final ReceiptDataSourceFactory mDataSourceFactory;
    private final LiveData<ReceiptDataSource> mReceiptDataSourceLiveData;
    private LiveData<Boolean> mIsFetchingData;
    private LiveData<HyperwalletErrors> mErrorsLiveData;
    private LiveData<PagedList<HyperwalletTransferMethod>> mReceiptsLiveData;

    ReceiptRepositoryImpl() {
        mDataSourceFactory = new ReceiptDataSourceFactory();
        mReceiptDataSourceLiveData = mDataSourceFactory.getReceiptDataSource();
    }

    @Override
    public LiveData<PagedList<HyperwalletTransferMethod>> loadReceipts() {
        if (mReceiptsLiveData == null) {
            PagedList.Config config = new PagedList.Config.Builder()
                    .setPageSize(10)
                    .setEnablePlaceholders(true)
                    .setInitialLoadSizeHint(20)
                    .build();
            mReceiptsLiveData = new LivePagedListBuilder<>(mDataSourceFactory, config).build();
        }
        return mReceiptsLiveData;
    }

    @Override
    public LiveData<Boolean> isLoading() {
        if (mIsFetchingData == null) {
            mIsFetchingData = mReceiptDataSourceLiveData.getValue().isFetchingData();
        }
        return mIsFetchingData;
    }

    @Override
    public LiveData<HyperwalletErrors> getErrors() {
        if (mErrorsLiveData == null) {
            mErrorsLiveData = mReceiptDataSourceLiveData.getValue().getErrors();
        }
        return mErrorsLiveData;
    }

    @Override
    public void retryLoadReceipt() {
        if (mReceiptDataSourceLiveData.getValue() != null) {
            mReceiptDataSourceLiveData.getValue().retry();
        }
    }
}