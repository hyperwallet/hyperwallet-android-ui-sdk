package com.hyperwallet.android.common.repository;

import androidx.annotation.NonNull;

import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.listener.HyperwalletListener;
import com.hyperwallet.android.model.paging.HyperwalletPageList;
import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod;
import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethodPagination;

public class PrepaidCardReceiptProvider implements ReceiptProvider {

    private final String mReceiptSourceToken;

    public PrepaidCardReceiptProvider(@NonNull final String receiptSourceToken) {
        mReceiptSourceToken = receiptSourceToken;
    }

    @Override
    public void loadReceipts(@NonNull HyperwalletTransferMethodPagination pagination,
            @NonNull HyperwalletListener<HyperwalletPageList<HyperwalletTransferMethod>> listener) {

        //there will be a method that takes in the string as parameter - we would invoke getReceiptSourceToken() then
        Hyperwallet.getDefault().listTransferMethods(pagination, listener);
    }

    @Override
    public String getReceiptSourceToken() {
        return mReceiptSourceToken;
    }

}
