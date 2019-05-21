package com.hyperwallet.android.common.repository;

import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.exception.HyperwalletException;
import com.hyperwallet.android.listener.HyperwalletListener;
import com.hyperwallet.android.model.HyperwalletStatusTransition;
import com.hyperwallet.android.model.HyperwalletTransferMethod;
import com.hyperwallet.android.model.HyperwalletTransferMethodPagination;
import com.hyperwallet.android.model.paging.HyperwalletPageList;

public class TransactionHistoryRepositoryImpl implements TransactionHistoryRepository {

    private HyperwalletTransferMethodPagination mHyperwalletTransferMethodPagination;
    private LoadTransactionListCallback mLoadTransactionListCallback;

    public TransactionHistoryRepositoryImpl() {
        mHyperwalletTransferMethodPagination = new HyperwalletTransferMethodPagination();
        mHyperwalletTransferMethodPagination.setStatus(HyperwalletStatusTransition.StatusDefinition.DE_ACTIVATED);
    }

    Hyperwallet getHyperwallet() {
        return Hyperwallet.getDefault();
    }

    @Override
    public void loadTransactionList(@NonNull final LoadTransactionListCallback callback) {
        mLoadTransactionListCallback = callback;
        getHyperwallet().listTransferMethods(mHyperwalletTransferMethodPagination,
                new HyperwalletListener<HyperwalletPageList<HyperwalletTransferMethod>>() {
                    @Override
                    public void onSuccess(@Nullable HyperwalletPageList<HyperwalletTransferMethod> result) {
                        if (result != null && mLoadTransactionListCallback != null) {
                            updatePagination(result);
                            mLoadTransactionListCallback.onTransferMethodListLoaded(result.getDataList());
                        }
                    }

                    @Override
                    public void onFailure(HyperwalletException exception) {
                        if (mLoadTransactionListCallback != null) {
                            mLoadTransactionListCallback.onError(exception.getHyperwalletErrors());
                        }

                    }

                    @Override
                    public Handler getHandler() {
                        return null;
                    }
                });
    }

    @Override
    public void dispose() {
        mLoadTransactionListCallback = null;
        mHyperwalletTransferMethodPagination = new HyperwalletTransferMethodPagination();
        mHyperwalletTransferMethodPagination.setStatus(HyperwalletStatusTransition.StatusDefinition.DE_ACTIVATED);
    }

    private void updatePagination(@NonNull final HyperwalletPageList<HyperwalletTransferMethod> result) {
        if (result.getNextPageLink() == null) {
            return;
        }
        int limit = result.getLimit();
        int offset = mHyperwalletTransferMethodPagination.getOffset() +  mHyperwalletTransferMethodPagination.getLimit();
        mHyperwalletTransferMethodPagination.setLimit(limit);
        mHyperwalletTransferMethodPagination.setOffset(offset);
    }

}
