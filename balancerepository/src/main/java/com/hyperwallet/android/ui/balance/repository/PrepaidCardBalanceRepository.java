package com.hyperwallet.android.ui.balance.repository;

import androidx.annotation.NonNull;

import com.hyperwallet.android.model.Errors;
import com.hyperwallet.android.model.balance.Balance;

import java.util.List;

public interface PrepaidCardBalanceRepository {
    /**
     * Load prepaid card balances
     */
    void loadPrepaidCardBalances(String prepaidCardToken, @NonNull final LoadPrepaidCardBalanceCallback callback);

    /**
     * Load prepaid card balance callback
     */
    interface LoadPrepaidCardBalanceCallback {
        /**
         * On prepaid card balance list loaded
         */
        void onPrepaidCardBalanceLoaded(@NonNull final List<Balance> balances);

        /**
         * On error
         */
        void onError(@NonNull final Errors errors);
    }
}
