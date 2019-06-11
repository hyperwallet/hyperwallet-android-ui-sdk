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

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.hyperwallet.android.common.viewmodel.Event;
import com.hyperwallet.android.model.HyperwalletErrors;
import com.hyperwallet.android.model.receipt.Receipt;

public class ReceiptRepositoryImpl implements ReceiptRepository {

    private static final int PAGE_SIZE = 10;
    private static final int INITIAL_LOAD_SIZE = 20;

    private final ReceiptDataSourceFactory mDataSourceFactory;
    private final LiveData<ReceiptDataSource> mReceiptDataSourceLiveData;
    private LiveData<Event<HyperwalletErrors>> mErrorsLiveData;
    private LiveData<Boolean> mIsFetchingData;
    private LiveData<PagedList<Receipt>> mReceiptsLiveData;

    ReceiptRepositoryImpl(@NonNull final String token) {
        mDataSourceFactory = new ReceiptDataSourceFactory(token);
        mReceiptDataSourceLiveData = mDataSourceFactory.getReceiptDataSource();
    }

    @Override
    public LiveData<PagedList<Receipt>> loadReceipts() {
        if (mReceiptsLiveData == null) {
            PagedList.Config config = new PagedList.Config.Builder()
                    .setPageSize(PAGE_SIZE)
                    .setEnablePlaceholders(true)
                    .setInitialLoadSizeHint(INITIAL_LOAD_SIZE)
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
    public LiveData<Event<HyperwalletErrors>> getErrors() {
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
