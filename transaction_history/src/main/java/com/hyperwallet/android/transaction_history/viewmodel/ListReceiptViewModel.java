package com.hyperwallet.android.transaction_history.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagedList;

import com.hyperwallet.android.common.repository.ReceiptRepository;
import com.hyperwallet.android.model.HyperwalletErrors;
import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod;

public class ListReceiptViewModel extends ViewModel {

    private LiveData<PagedList<HyperwalletTransferMethod>> mTransferMethods;
    private LiveData<HyperwalletErrors> mTransferMethodErrors;
    private LiveData<Boolean> mDisplayLoading;
    private ReceiptRepository mReceiptRepository;

    public ListReceiptViewModel(@NonNull final ReceiptRepository repository) {
        mReceiptRepository = repository;
        mTransferMethods = mReceiptRepository.getReceiptList();
    }


    public LiveData<HyperwalletErrors> getReceiptListError() {
        if (mTransferMethodErrors == null) {
            mTransferMethodErrors = mReceiptRepository.getErrors();
        }
        return mTransferMethodErrors;
    }

    public LiveData<Boolean> isLoadingData() {
        if (mDisplayLoading == null) {
            mDisplayLoading = mReceiptRepository.isFetchingReceiptList();
        }
        return mDisplayLoading;
    }

    public LiveData<PagedList<HyperwalletTransferMethod>> getRecipeList() {
        return mTransferMethods;
    }

    public void retry() {
        mReceiptRepository.retry();
    }


    @Override
    protected void onCleared() {
        super.onCleared();
        mReceiptRepository = null;
    }

    public static class ListReceiptViewModelFactory implements ViewModelProvider.Factory {

        private final ReceiptRepository mRepository;

        public ListReceiptViewModelFactory(@NonNull final ReceiptRepository repository) {
            mRepository = repository;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(ListReceiptViewModel.class)) {
                return (T) new ListReceiptViewModel(mRepository);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");


        }
    }
}
