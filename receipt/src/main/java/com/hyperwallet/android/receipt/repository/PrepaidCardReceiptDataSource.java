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

import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.PageKeyedDataSource;

import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.common.viewmodel.Event;
import com.hyperwallet.android.exception.HyperwalletException;
import com.hyperwallet.android.listener.HyperwalletListener;
import com.hyperwallet.android.model.HyperwalletErrors;
import com.hyperwallet.android.model.QueryParam;
import com.hyperwallet.android.model.paging.HyperwalletPageList;
import com.hyperwallet.android.model.receipt.Receipt;
import com.hyperwallet.android.util.DateUtil;

import java.util.Calendar;
import java.util.Date;

public class PrepaidCardReceiptDataSource extends PageKeyedDataSource<Date, Receipt> {

    private static final int YEAR_BEFORE_NOW = -1;
    private final Calendar mCalendarYearBeforeNow;
    private final MutableLiveData<Event<HyperwalletErrors>> mErrors = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mIsFetchingData = new MutableLiveData<>();
    private PageKeyedDataSource.LoadInitialCallback<Date, Receipt> mLoadInitialCallback;
    private PageKeyedDataSource.LoadInitialParams<Date> mLoadInitialParams;
    private PageKeyedDataSource.LoadCallback<Date, Receipt> mLoadAfterCallback;
    private PageKeyedDataSource.LoadParams<Date> mLoadAfterParams;

    private String mPrepaidCardToken;

    PrepaidCardReceiptDataSource(@NonNull final String prepaidCardToken) {
        super();
        mPrepaidCardToken = prepaidCardToken;
        mCalendarYearBeforeNow = Calendar.getInstance();
        mCalendarYearBeforeNow.add(Calendar.YEAR, YEAR_BEFORE_NOW);
    }

    @Override
    public void loadInitial(@NonNull final LoadInitialParams<Date> params,
            @NonNull final LoadInitialCallback<Date, Receipt> callback) {
        mLoadInitialCallback = callback;
        mLoadInitialParams = params;
        mIsFetchingData.postValue(Boolean.TRUE);

        QueryParam queryParam = new QueryParam.Builder()
                .createdAfter(mCalendarYearBeforeNow.getTime()).build();

        getHyperwallet().listPrepaidCardReceipts(mPrepaidCardToken, queryParam,
                new HyperwalletListener<HyperwalletPageList<Receipt>>() {
                    @Override
                    public void onSuccess(@Nullable HyperwalletPageList<Receipt> result) {
                        mIsFetchingData.postValue(Boolean.FALSE);
                        mErrors.postValue(null);

                        if (result != null) {
                            Date previous = mCalendarYearBeforeNow.getTime();
                            Date next = null;
                            callback.onResult(result.getDataList(), previous, next);
                        }
                        // reset
                        mLoadInitialCallback = null;
                        mLoadInitialParams = null;
                    }

                    @Override
                    public void onFailure(HyperwalletException exception) {
                        mIsFetchingData.postValue(Boolean.FALSE);
                        mErrors.postValue(new Event<>(exception.getHyperwalletErrors()));
                    }

                    @Override
                    public Handler getHandler() {
                        return null;
                    }
                });
    }

    @Override
    public void loadBefore(@NonNull LoadParams<Date> params,
            @NonNull LoadCallback<Date, Receipt> callback) {
    }

    @Override
    public void loadAfter(@NonNull LoadParams<Date> params,
            final @NonNull LoadCallback<Date, Receipt> callback) {
        mLoadInitialCallback = null;
        mLoadInitialParams = null;
        mLoadAfterCallback = callback;
        mLoadAfterParams = params;
        mIsFetchingData.postValue(Boolean.TRUE);

        QueryParam queryParam = new QueryParam.Builder()
                .createdAfter(params.key)
                .limit(params.requestedLoadSize).build();

        getHyperwallet().listPrepaidCardReceipts("", queryParam,
                new HyperwalletListener<HyperwalletPageList<Receipt>>() {
                    @Override
                    public void onSuccess(@Nullable HyperwalletPageList<Receipt> result) {
                        mIsFetchingData.postValue(Boolean.FALSE);
                        mErrors.postValue(null);

                        if (result != null) {
                            Date next = getNextDate(result);
                            callback.onResult(result.getDataList(), next);
                        }

                        // reset
                        mLoadAfterCallback = null;
                        mLoadAfterParams = null;
                    }

                    @Override
                    public void onFailure(HyperwalletException exception) {
                        mIsFetchingData.postValue(Boolean.FALSE);
                        mErrors.postValue(new Event<>(exception.getHyperwalletErrors()));
                    }

                    @Override
                    public Handler getHandler() {
                        return null;
                    }
                });
    }


    private Date getNextDate(@Nullable final HyperwalletPageList<Receipt> result) {
        if (result != null && result.getDataList() != null && result.getDataList().size() > 0) {
            return DateUtil.fromDateTimeString(result.getDataList().get(result.getDataList().size() -1).getCreatedOn());
        }
        return new Date();
    }


    public LiveData<Boolean> isFetchingData() {
        return mIsFetchingData;
    }

    public LiveData<Event<HyperwalletErrors>> getErrors() {
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
