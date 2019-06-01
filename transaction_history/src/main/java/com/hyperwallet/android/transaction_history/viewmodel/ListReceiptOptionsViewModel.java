package com.hyperwallet.android.transaction_history.viewmodel;

import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.exception.HyperwalletException;
import com.hyperwallet.android.listener.HyperwalletListener;
import com.hyperwallet.android.model.paging.HyperwalletPageList;
import com.hyperwallet.android.model.transfermethod.HyperwalletBankAccountPagination;
import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod;

public class ListReceiptOptionsViewModel extends ViewModel {

    private MutableLiveData<HyperwalletPageList<HyperwalletTransferMethod>> mTransferMethodLiveData;

    public ListReceiptOptionsViewModel() {
        mTransferMethodLiveData = new MutableLiveData<>();
        loadPrepaidCards();
    }

    public LiveData<HyperwalletPageList<HyperwalletTransferMethod>> getPrepaidCards(){
        return mTransferMethodLiveData;
    }

    private void loadPrepaidCards() {
        Hyperwallet.getDefault().listTransferMethods(new HyperwalletBankAccountPagination(),
                new HyperwalletListener<HyperwalletPageList<HyperwalletTransferMethod>>() {
                    @Override
                    public void onSuccess(@Nullable HyperwalletPageList<HyperwalletTransferMethod> result) {
                        mTransferMethodLiveData.postValue(result);
                    }

                    @Override
                    public void onFailure(HyperwalletException exception) {

                    }

                    @Override
                    public Handler getHandler() {
                        return null;
                    }
                });
    }



}
