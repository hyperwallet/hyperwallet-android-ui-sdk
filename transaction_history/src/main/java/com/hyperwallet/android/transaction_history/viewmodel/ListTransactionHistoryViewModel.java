package com.hyperwallet.android.transaction_history.viewmodel;

import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.exception.HyperwalletException;
import com.hyperwallet.android.listener.HyperwalletListener;
import com.hyperwallet.android.model.HyperwalletError;
import com.hyperwallet.android.model.HyperwalletErrors;
import com.hyperwallet.android.model.HyperwalletTransferMethod;
import com.hyperwallet.android.model.HyperwalletTransferMethodPagination;
import com.hyperwallet.android.model.paging.HyperwalletPageList;

import java.util.ArrayList;
import java.util.List;

public class ListTransactionHistoryViewModel extends ViewModel {

    private HyperwalletTransferMethodPagination mHyperwalletTransferMethodPagination;
    private MutableLiveData<List<HyperwalletTransferMethod>> mTransferMethods;
    private MutableLiveData<HyperwalletErrors> mTransferMethodErrors;

    public ListTransactionHistoryViewModel() {

        mHyperwalletTransferMethodPagination = new HyperwalletTransferMethodPagination();
        mTransferMethodErrors = new MutableLiveData<>();
    }

    public LiveData<List<HyperwalletTransferMethod>> loadTransactionHistory() {
        if (mTransferMethods == null) {
            mTransferMethods = new MutableLiveData<>();
            loadTransferMethods();
        }
        return mTransferMethods;
    }

    public LiveData<HyperwalletErrors> getTransactionHistoryError() {
        return mTransferMethodErrors;
    }

    private void loadTransferMethods() {

        //todo use repository instead
        Hyperwallet.getDefault().listTransferMethods(mHyperwalletTransferMethodPagination,
                new HyperwalletListener<HyperwalletPageList<HyperwalletTransferMethod>>() {
                    @Override
                    public void onSuccess(@Nullable HyperwalletPageList<HyperwalletTransferMethod> result) {
                        if (result != null) {
//                            int limit = result.getNextPageLink().getLimit();
//                            int offset = result.getNextPageLink().getOffset();
//                            mHyperwalletTransferMethodPagination.setLimit(limit);
//                            mHyperwalletTransferMethodPagination.setOffset(offset);
                            mTransferMethods.postValue(result.getDataList());
                            mTransferMethodErrors.postValue(null);
                        } else {
                            mTransferMethods.postValue(new ArrayList<HyperwalletTransferMethod>());
                        }

                    }

                    @Override
                    public void onFailure(HyperwalletException exception) {
                        mTransferMethodErrors.postValue(exception.getHyperwalletErrors());
                    }

                    @Override
                    public Handler getHandler() {
                        return null;
                    }
                });
    }
}
