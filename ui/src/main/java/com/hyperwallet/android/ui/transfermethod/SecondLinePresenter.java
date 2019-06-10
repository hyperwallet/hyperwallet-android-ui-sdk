package com.hyperwallet.android.ui.transfermethod;


interface SecondLinePresenter {
    TransferMethodSecondLineStrategy obtainSecondLineStrategy(String type);
}
