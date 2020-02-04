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

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.hyperwallet.android.model.Errors;
import com.hyperwallet.android.model.receipt.Receipt;
import com.hyperwallet.android.ui.common.repository.Event;

/**
 * {@link UserReceiptRepository} implementation
 */
public class UserReceiptRepositoryImpl implements UserReceiptRepository {

    private static final int PAGE_SIZE = 10;
    private static final int INITIAL_LOAD_SIZE = 20;

    private final UserReceiptDataSourceFactory mDataSourceFactory;
    private final LiveData<UserReceiptDataSource> mReceiptDataSourceLiveData;
    private LiveData<PagedList<Receipt>> mReceiptsLiveData;
    private MutableLiveData<Boolean> mRefreshDataSource = new MutableLiveData<>();
    private Observer<UserReceiptDataSource> mReceiptDataSourceObserver = new Observer<UserReceiptDataSource>() {
        @Override
        public void onChanged(UserReceiptDataSource userReceiptDataSource) {
            mRefreshDataSource.postValue(Boolean.TRUE);
        }
    };

    public UserReceiptRepositoryImpl() {
        mDataSourceFactory = new UserReceiptDataSourceFactory();
        mReceiptDataSourceLiveData = mDataSourceFactory.getUserReceiptDataSource();
        mReceiptDataSourceLiveData.observeForever(mReceiptDataSourceObserver);
        mRefreshDataSource.postValue(Boolean.FALSE);
    }

    /**
     * @see UserReceiptRepository#loadUserReceipts()
     */
    @Override
    public LiveData<PagedList<Receipt>> loadUserReceipts() {
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
     * @see UserReceiptRepository#isLoading()
     */
    @Override
    public LiveData<Boolean> isLoading() {
        return Transformations.switchMap(mRefreshDataSource, new Function<Boolean, LiveData<Boolean>>() {
            @Override
            public LiveData<Boolean> apply(Boolean input) {
                return mReceiptDataSourceLiveData.getValue().isFetchingData();
            }
        });
    }

    /**
     * @see UserReceiptRepository#getErrors()
     */
    @Override
    public LiveData<Event<Errors>> getErrors() {
        return Transformations.switchMap(mRefreshDataSource, new Function<Boolean, LiveData<Event<Errors>>>() {
            @Override
            public LiveData<Event<Errors>> apply(Boolean input) {
                return mReceiptDataSourceLiveData.getValue().getErrors();
            }
        });
    }

    /**
     * @see UserReceiptRepository#retryLoadReceipt()
     */
    @Override
    public void retryLoadReceipt() {
        mReceiptDataSourceLiveData.getValue().retry();
    }

    @Override
    public void refresh() {
        mReceiptDataSourceLiveData.getValue().invalidate();
    }

    @Override
    public void cleanup() {
        mReceiptDataSourceLiveData.removeObserver(mReceiptDataSourceObserver);
    }
}
