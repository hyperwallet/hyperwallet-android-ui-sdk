package com.hyperwallet.android.transaction_history.viewmodel;

import androidx.lifecycle.ViewModel;

import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod;

public class ReceiptDetailViewModel extends ViewModel {

    private HyperwalletTransferMethod mHyperwalletTransferMethod;

    public HyperwalletTransferMethod getHyperwalletTransferMethod() {
        return mHyperwalletTransferMethod;
    }


    public void setHyperwalletTransferMethod(HyperwalletTransferMethod transferMethod) {
        mHyperwalletTransferMethod = transferMethod;
    }

}
