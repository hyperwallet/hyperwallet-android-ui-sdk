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

import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.PageKeyedDataSource;

import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.exception.HyperwalletException;
import com.hyperwallet.android.listener.HyperwalletListener;
import com.hyperwallet.android.model.Errors;
import com.hyperwallet.android.model.paging.PageList;
import com.hyperwallet.android.model.receipt.Receipt;
import com.hyperwallet.android.model.receipt.ReceiptQueryParam;
import com.hyperwallet.android.ui.common.repository.EspressoIdlingResource;
import com.hyperwallet.android.ui.common.repository.Event;

import java.util.Calendar;

/**
 * UserReceiptDataSource mediates communication to HW API Platform particularly on
 * Receipts Users V3 API
 */
public class UserReceiptDataSource extends PageKeyedDataSource<Integer, Receipt> {

    private static final int YEAR_BEFORE_NOW = -1;
    private final Calendar mCalendarYearBeforeNow;
    private final MutableLiveData<Event<Errors>> mErrors = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mIsFetchingData = new MutableLiveData<>();
    private LoadInitialCallback<Integer, Receipt> mLoadInitialCallback;
    private LoadInitialParams<Integer> mLoadInitialParams;
    private LoadCallback<Integer, Receipt> mLoadAfterCallback;
    private LoadParams<Integer> mLoadAfterParams;

    UserReceiptDataSource() {
        super();
        mCalendarYearBeforeNow = Calendar.getInstance();
        mCalendarYearBeforeNow.add(Calendar.YEAR, YEAR_BEFORE_NOW);
    }

    /**
     * @see PageKeyedDataSource#loadInitial(LoadInitialParams, LoadInitialCallback)
     */
    @Override
    public void loadInitial(@NonNull final LoadInitialParams<Integer> params,
            @NonNull final LoadInitialCallback<Integer, Receipt> callback) {
        mLoadInitialCallback = callback;
        mLoadInitialParams = params;
        mIsFetchingData.postValue(Boolean.TRUE);

        ReceiptQueryParam queryParam = new ReceiptQueryParam.Builder()
                .createdAfter(mCalendarYearBeforeNow.getTime())
                .limit(params.requestedLoadSize)
                .sortByCreatedOnDesc().build();

        EspressoIdlingResource.increment();
        getHyperwallet().listUserReceipts(queryParam,
                new HyperwalletListener<PageList<Receipt>>() {
                    @Override
                    public void onSuccess(@Nullable PageList<Receipt> result) {
                        mErrors.postValue(null);

                        if (result != null) {
                            int next = result.getLimit() + result.getOffset();
                            int previous = 0;
                            callback.onResult(result.getDataList(), previous, next);
                        }
                        // reset
                        mIsFetchingData.postValue(Boolean.FALSE);
                        mLoadInitialCallback = null;
                        mLoadInitialParams = null;
                        EspressoIdlingResource.decrement();
                    }

                    @Override
                    public void onFailure(HyperwalletException exception) {
                        mIsFetchingData.postValue(Boolean.FALSE);
                        mErrors.postValue(new Event<>(exception.getErrors()));
                        EspressoIdlingResource.decrement();
                    }

                    @Override
                    public Handler getHandler() {
                        return null;
                    }
                });
    }

    /**
     * Unused in this case
     *
     * @see PageKeyedDataSource#loadBefore(LoadParams, LoadCallback)
     */
    @Override
    public void loadBefore(@NonNull final LoadParams<Integer> params,
            @NonNull final LoadCallback<Integer, Receipt> callback) {
    }

    /**
     * @see PageKeyedDataSource#loadAfter(LoadParams, LoadCallback)
     */
    @Override
    public void loadAfter(@NonNull final LoadParams<Integer> params,
            final @NonNull LoadCallback<Integer, Receipt> callback) {
        mLoadInitialCallback = null;
        mLoadInitialParams = null;
        mLoadAfterCallback = callback;
        mLoadAfterParams = params;
        mIsFetchingData.postValue(Boolean.TRUE);

        ReceiptQueryParam queryParam = new ReceiptQueryParam.Builder()
                .createdAfter(mCalendarYearBeforeNow.getTime())
                .limit(params.requestedLoadSize)
                .offset(params.key)
                .sortByCreatedOnDesc().build();

        EspressoIdlingResource.increment();
        getHyperwallet().listUserReceipts(queryParam,
                new HyperwalletListener<PageList<Receipt>>() {
                    @Override
                    public void onSuccess(@Nullable PageList<Receipt> result) {
                        mIsFetchingData.postValue(Boolean.FALSE);
                        mErrors.postValue(null);

                        if (result != null) {
                            int next = result.getLimit() + result.getOffset();
                            callback.onResult(result.getDataList(), next);
                        }

                        // reset
                        mLoadAfterCallback = null;
                        mLoadAfterParams = null;
                        EspressoIdlingResource.decrement();
                    }

                    @Override
                    public void onFailure(HyperwalletException exception) {
                        mIsFetchingData.postValue(Boolean.FALSE);
                        mErrors.postValue(new Event<>(exception.getErrors()));
                        EspressoIdlingResource.decrement();
                    }

                    @Override
                    public Handler getHandler() {
                        return null;
                    }
                });
    }

    /**
     * Facilitates retry when network is down; any error that we can have a retry operation
     */
    void retry() {
        if (mLoadInitialCallback != null) {
            loadInitial(mLoadInitialParams, mLoadInitialCallback);
        } else if (mLoadAfterCallback != null) {
            loadAfter(mLoadAfterParams, mLoadAfterCallback);
        }
    }

    /**
     * Retrieve reference of Hyperwallet errors inorder for consumers to observe on data changes
     *
     * @return Live event data of {@link Errors}
     */
    public LiveData<Event<Errors>> getErrors() {
        return mErrors;
    }

    LiveData<Boolean> isFetchingData() {
        return mIsFetchingData;
    }

    Hyperwallet getHyperwallet() {
        return Hyperwallet.getDefault();
    }
}
