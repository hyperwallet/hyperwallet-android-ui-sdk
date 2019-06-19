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
package com.hyperwallet.android.ui.receipt.repository;

import androidx.lifecycle.LiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.hyperwallet.android.model.HyperwalletErrors;
import com.hyperwallet.android.model.receipt.Receipt;
import com.hyperwallet.android.ui.common.viewmodel.Event;

/**
 * {@link UserReceiptRepository} implementation
 */
public class UserReceiptRepositoryImpl implements UserReceiptRepository {

    private static final int PAGE_SIZE = 10;
    private static final int INITIAL_LOAD_SIZE = 20;

    private final UserReceiptDataSourceFactory mDataSourceFactory;
    private final LiveData<UserReceiptDataSource> mReceiptDataSourceLiveData;
    private LiveData<Event<HyperwalletErrors>> mErrorsLiveData;
    private LiveData<Boolean> mIsFetchingData;
    private LiveData<PagedList<Receipt>> mReceiptsLiveData;

    UserReceiptRepositoryImpl() {
        mDataSourceFactory = new UserReceiptDataSourceFactory();
        mReceiptDataSourceLiveData = mDataSourceFactory.getUserReceiptDataSource();
    }

    /**
     * @see {@link UserReceiptRepository#loadReceipts()}
     */
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

    /**
     * @see {@link UserReceiptRepository#isLoading()}
     */
    @Override
    public LiveData<Boolean> isLoading() {
        if (mIsFetchingData == null) {
            mIsFetchingData = mReceiptDataSourceLiveData.getValue().isFetchingData();
        }
        return mIsFetchingData;
    }

    /**
     * @see {@link UserReceiptRepository#getErrors()}
     * */
    @Override
    public LiveData<Event<HyperwalletErrors>> getErrors() {
        if (mErrorsLiveData == null) {
            mErrorsLiveData = mReceiptDataSourceLiveData.getValue().getErrors();
        }
        return mErrorsLiveData;
    }

    /**
     * @see {@link UserReceiptRepository#retryLoadReceipt()}
     * */
    @Override
    public void retryLoadReceipt() {
        if (mReceiptDataSourceLiveData.getValue() != null) {
            mReceiptDataSourceLiveData.getValue().retry();
        }
    }
}
