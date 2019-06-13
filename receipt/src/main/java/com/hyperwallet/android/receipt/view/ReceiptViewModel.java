package com.hyperwallet.android.receipt.view;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.PagedList;

import com.hyperwallet.android.common.viewmodel.Event;
import com.hyperwallet.android.model.HyperwalletError;
import com.hyperwallet.android.model.receipt.Receipt;

import java.util.List;

public abstract class ReceiptViewModel extends ViewModel {

    public abstract LiveData<Event<List<HyperwalletError>>> getReceiptErrors();

    public abstract void retryLoadReceipts();

    public abstract LiveData<Boolean> isLoadingData();

    public abstract  LiveData<PagedList<Receipt>> getReceiptList();

}
