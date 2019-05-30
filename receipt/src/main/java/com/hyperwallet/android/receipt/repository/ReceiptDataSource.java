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

import static com.hyperwallet.android.model.HyperwalletStatusTransition.StatusDefinition.ACTIVATED;
import static com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethodPagination.TransferMethodSortable.DESCENDANT_CREATE_ON;

import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.PageKeyedDataSource;

import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.exception.HyperwalletException;
import com.hyperwallet.android.listener.HyperwalletListener;
import com.hyperwallet.android.model.HyperwalletErrors;
import com.hyperwallet.android.model.paging.HyperwalletPageList;
import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod;
import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethodPagination;

public class ReceiptDataSource extends PageKeyedDataSource<Integer, HyperwalletTransferMethod> {

    private final HyperwalletTransferMethodPagination mReceiptPagination;
    private final MutableLiveData<Boolean> mIsFetchingData = new MutableLiveData<>();
    private final MutableLiveData<HyperwalletErrors> mErrors = new MutableLiveData<>();
    private final Handler mHandler = new Handler();

    private LoadInitialCallback<Integer, HyperwalletTransferMethod> mLoadInitialCallback;
    private LoadInitialParams<Integer> mLoadInitialParams;
    private LoadCallback<Integer, HyperwalletTransferMethod> mLoadAfterCallback;
    private LoadParams<Integer> mLoadAfterParams;

    ReceiptDataSource() {
        super();
        mReceiptPagination = new HyperwalletTransferMethodPagination();
        mReceiptPagination.setStatus(ACTIVATED);
        mReceiptPagination.setSortBy(DESCENDANT_CREATE_ON);
    }

    @Override
    public void loadInitial(@NonNull final LoadInitialParams<Integer> params,
            @NonNull final LoadInitialCallback<Integer, HyperwalletTransferMethod> callback) {
        mLoadInitialCallback = callback;
        mLoadInitialParams = params;
        mIsFetchingData.postValue(Boolean.TRUE);

        mReceiptPagination.setLimit(params.requestedLoadSize);
        getHyperwallet().listTransferMethods(mReceiptPagination,
                new HyperwalletListener<HyperwalletPageList<HyperwalletTransferMethod>>() {
                    @Override
                    public void onSuccess(@Nullable HyperwalletPageList<HyperwalletTransferMethod> result) {
                        mIsFetchingData.postValue(Boolean.FALSE);
                        mErrors.postValue(null);

                        if (result != null) {
                            int next = result.getLimit() + result.getOffset();
                            int previous = 0;
                            callback.onResult(result.getDataList(), previous, next);
                        }

                        // reset
                        mLoadInitialCallback = null;
                        mLoadInitialParams = null;
                    }

                    @Override
                    public void onFailure(HyperwalletException exception) {
                        mIsFetchingData.postValue(Boolean.FALSE);
                        mErrors.postValue(exception.getHyperwalletErrors());
                    }

                    @Override
                    public Handler getHandler() {
                        return mHandler;
                    }
                });
    }

    @Override
    public void loadBefore(@NonNull LoadParams<Integer> params,
            @NonNull LoadCallback<Integer, HyperwalletTransferMethod> callback) {
    }

    @Override
    public void loadAfter(@NonNull LoadParams<Integer> params,
            final @NonNull LoadCallback<Integer, HyperwalletTransferMethod> callback) {
        mLoadAfterCallback = callback;
        mLoadAfterParams = params;

        mIsFetchingData.postValue(Boolean.TRUE);

        mReceiptPagination.setLimit(params.requestedLoadSize);
        mReceiptPagination.setOffset(params.key);
        getHyperwallet().listTransferMethods(mReceiptPagination,
                new HyperwalletListener<HyperwalletPageList<HyperwalletTransferMethod>>() {
                    @Override
                    public void onSuccess(@Nullable HyperwalletPageList<HyperwalletTransferMethod> result) {
                        mIsFetchingData.postValue(Boolean.FALSE);
                        mErrors.postValue(null);

                        if (result != null) {
                            int next = result.getLimit() + result.getOffset();
                            callback.onResult(result.getDataList(), next);
                        }

                        // reset
                        mLoadInitialCallback = null;
                        mLoadInitialParams = null;
                    }

                    @Override
                    public void onFailure(HyperwalletException exception) {
                        mIsFetchingData.postValue(Boolean.FALSE);
                        mErrors.postValue(exception.getHyperwalletErrors());
                    }

                    @Override
                    public Handler getHandler() {
                        return mHandler;
                    }
                });
    }

    void retry() {
        if (mLoadInitialCallback != null) {
            loadInitial(mLoadInitialParams, mLoadInitialCallback);
        } else if (mLoadAfterCallback != null) {
            loadAfter(mLoadAfterParams, mLoadAfterCallback);
        }
    }

    LiveData<Boolean> isFetchingData() {
        return mIsFetchingData;
    }

    public LiveData<HyperwalletErrors> getErrors() {
        return mErrors;
    }

    Hyperwallet getHyperwallet() {
        return Hyperwallet.getDefault();
    }
}
