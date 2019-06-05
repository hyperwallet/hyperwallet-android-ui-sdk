package com.hyperwallet.android.common.repository;

import androidx.annotation.NonNull;

import com.hyperwallet.android.listener.HyperwalletListener;
import com.hyperwallet.android.model.paging.HyperwalletPageList;
import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod;
import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethodPagination;

public interface ReceiptProvider {

    void loadReceipts(@NonNull final HyperwalletTransferMethodPagination pagination,
            @NonNull final HyperwalletListener<HyperwalletPageList<HyperwalletTransferMethod>> listener);

    String getReceiptSourceToken();

}
