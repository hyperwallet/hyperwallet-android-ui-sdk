package com.hyperwallet.android.common.repository;

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
import com.hyperwallet.android.model.HyperwalletStatusTransition;
import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod;
import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethodPagination;
import com.hyperwallet.android.model.paging.HyperwalletPageList;

public class ReceiptDataSource extends PageKeyedDataSource<Integer, HyperwalletTransferMethod> {

    private HyperwalletTransferMethodPagination mHyperwalletTransferMethodPagination;
    private MutableLiveData<Boolean> mIsFetchingData = new MutableLiveData<>();
    private MutableLiveData<HyperwalletErrors> mErrors = new MutableLiveData<>();


    //todo the call backs and their parameters will not be necessary when using kotlin. we can store the function in a variable
    private LoadInitialCallback<Integer, HyperwalletTransferMethod> mLoadInitialCallback;
    private LoadInitialParams<Integer> mLoadInitialParams;
    private LoadCallback<Integer, HyperwalletTransferMethod> mLoadAfterCallback;
    private LoadParams<Integer> mLoadAfterParams;



    public ReceiptDataSource() {
        super();
        mHyperwalletTransferMethodPagination = new HyperwalletTransferMethodPagination();
        mHyperwalletTransferMethodPagination.setStatus(HyperwalletStatusTransition.StatusDefinition.DE_ACTIVATED);
    }

    @Override
    public void loadInitial(@NonNull final LoadInitialParams<Integer> params,
            @NonNull final LoadInitialCallback<Integer, HyperwalletTransferMethod> callback) {

        mLoadInitialCallback = callback;
        mLoadInitialParams = params;

        mIsFetchingData.postValue(Boolean.TRUE);
        mHyperwalletTransferMethodPagination.setLimit(params.requestedLoadSize);
        getHyperwallet().listTransferMethods(mHyperwalletTransferMethodPagination,
                new HyperwalletListener<HyperwalletPageList<HyperwalletTransferMethod>>() {
                    @Override
                    public void onSuccess(@Nullable HyperwalletPageList<HyperwalletTransferMethod> result) {
                        mIsFetchingData.postValue(Boolean.FALSE);
                        mErrors.postValue(null);
                        int next = result.getLimit() + result.getOffset();
                        int previous = 0;
                        callback.onResult(result.getDataList(), previous, next);
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
                        return null;
                    }
                });
    }

    @Override
    public void loadBefore(@NonNull final LoadParams<Integer> params,
            @NonNull final LoadCallback<Integer, HyperwalletTransferMethod> callback) {

    }

    @Override
    public void loadAfter(@NonNull final LoadParams<Integer> params,
            @NonNull final LoadCallback<Integer, HyperwalletTransferMethod> callback) {

        mLoadAfterCallback = callback;
        mLoadAfterParams = params;

        mIsFetchingData.postValue(Boolean.TRUE);
        mHyperwalletTransferMethodPagination.setLimit(params.requestedLoadSize);
        mHyperwalletTransferMethodPagination.setOffset(params.key);
        getHyperwallet().listTransferMethods(mHyperwalletTransferMethodPagination,
                new HyperwalletListener<HyperwalletPageList<HyperwalletTransferMethod>>() {
                    @Override
                    public void onSuccess(@Nullable HyperwalletPageList<HyperwalletTransferMethod> result) {
                        mIsFetchingData.postValue(Boolean.FALSE);
                        mErrors.postValue(null);
                        int next = result.getLimit() + result.getOffset();
                        callback.onResult(result.getDataList(), next);
                        mLoadAfterCallback = null;
                        mLoadAfterParams = null;
                    }

                    @Override
                    public void onFailure(HyperwalletException exception) {
                        mIsFetchingData.postValue(Boolean.FALSE);
                        mErrors.postValue(exception.getHyperwalletErrors());
                    }

                    @Override
                    public Handler getHandler() {
                        return null;
                    }
                });

    }

    public void retry() {
        if (mLoadInitialCallback != null) {
            loadInitial(mLoadInitialParams, mLoadInitialCallback);
        } else {
            loadAfter(mLoadAfterParams, mLoadAfterCallback);
        }
    }

    public LiveData<Boolean> isFetchingData() {
        return mIsFetchingData;
    }

    public LiveData<HyperwalletErrors> getErrors() {
        return mErrors;
    }

    Hyperwallet getHyperwallet() {
        return Hyperwallet.getDefault();
    }
}
