package com.hyperwallet.android.transaction_history.viewmodel;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hyperwallet.android.common.repository.RepositoryFactory;
import com.hyperwallet.android.common.repository.TransactionHistoryRepository;
import com.hyperwallet.android.model.HyperwalletErrors;
import com.hyperwallet.android.model.HyperwalletStatusTransition;
import com.hyperwallet.android.model.HyperwalletTransferMethod;
import com.hyperwallet.android.model.HyperwalletTransferMethodPagination;

import java.util.ArrayList;
import java.util.List;

public class ListTransactionHistoryViewModel extends ViewModel {

    private HyperwalletTransferMethodPagination mHyperwalletTransferMethodPagination;
    private MutableLiveData<List<HyperwalletTransferMethod>> mTransferMethods;
    private MutableLiveData<HyperwalletErrors> mTransferMethodErrors;
    private TransactionHistoryRepository mTransactionHistoryRepository;

    private List<HyperwalletTransferMethod> mBackingTransferMethodList = new ArrayList<>();

    public ListTransactionHistoryViewModel() {
        mTransactionHistoryRepository = RepositoryFactory.getInstance().getTransactionHistoryRepository();
        mHyperwalletTransferMethodPagination = new HyperwalletTransferMethodPagination();
        mHyperwalletTransferMethodPagination.setStatus(HyperwalletStatusTransition.StatusDefinition.DE_ACTIVATED);
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


    public void loadTransferMethods(int visibleItemCount, int lastVisibleItem, int totalItemCount) {
        System.out.println(visibleItemCount);
        System.out.println(lastVisibleItem);
        System.out.println(totalItemCount);
        loadTransferMethods();
    }


    public void removeObservers(LifecycleOwner lifecycleOwner) {
        mTransferMethods.removeObservers(lifecycleOwner);
        mTransferMethodErrors.removeObservers(lifecycleOwner);
    }

    private void loadTransferMethods() {

        mTransactionHistoryRepository.loadTransactionList(
                new TransactionHistoryRepository.LoadTransactionListCallback() {
                    @Override
                    public void onTransferMethodListLoaded(List<HyperwalletTransferMethod> transferMethods) {
                        if (transferMethods != null) {
                            mBackingTransferMethodList.addAll(transferMethods);
                            mTransferMethods.postValue(mBackingTransferMethodList);
                            mTransferMethodErrors.postValue(null);
                        } else {
                            mTransferMethods.postValue(new ArrayList<HyperwalletTransferMethod>());
                        }
                    }

                    @Override
                    public void onError(HyperwalletErrors errors) {
                        mTransferMethodErrors.postValue(errors);
                    }
                });

    }


    @Override
    protected void onCleared() {
        super.onCleared();
        mTransactionHistoryRepository.dispose();
        mTransactionHistoryRepository = null;
    }



}
