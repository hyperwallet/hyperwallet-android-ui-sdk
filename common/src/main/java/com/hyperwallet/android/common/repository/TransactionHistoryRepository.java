package com.hyperwallet.android.common.repository;

import androidx.annotation.NonNull;

import com.hyperwallet.android.model.HyperwalletErrors;
import com.hyperwallet.android.model.HyperwalletTransferMethod;

import java.util.List;


public interface TransactionHistoryRepository {

    void loadTransactionList(@NonNull final LoadTransactionListCallback callback);

    void dispose();

    interface LoadTransactionListCallback {
        void onTransferMethodListLoaded(List<HyperwalletTransferMethod> transferMethods);
        void onError(HyperwalletErrors errors);
    }

}
