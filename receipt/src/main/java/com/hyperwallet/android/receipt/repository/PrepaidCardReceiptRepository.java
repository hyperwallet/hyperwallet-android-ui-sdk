package com.hyperwallet.android.receipt.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;

import com.hyperwallet.android.common.viewmodel.Event;
import com.hyperwallet.android.model.HyperwalletErrors;
import com.hyperwallet.android.model.receipt.Receipt;

public interface PrepaidCardReceiptRepository {


    LiveData<PagedList<Receipt>> getPrepaidCardReceipts();

    LiveData<Boolean> isLoading();

    LiveData<Event<HyperwalletErrors>> getErrors();

    void retryLoadPrepaidCardReceipt();

}
