package com.hyperwallet.android.common.repository;

import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;

import com.hyperwallet.android.model.HyperwalletErrors;
import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod;


public interface ReceiptRepository {

    LiveData<PagedList<HyperwalletTransferMethod>> getReceiptList();

    LiveData<Boolean> isFetchingReceiptList();

    LiveData<HyperwalletErrors> getErrors();

    void retry();

}
