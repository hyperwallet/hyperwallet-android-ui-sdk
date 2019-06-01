package com.hyperwallet.android.transaction_history.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagedList;

import com.hyperwallet.android.common.repository.ReceiptRepository;
import com.hyperwallet.android.model.HyperwalletError;
import com.hyperwallet.android.model.HyperwalletErrors;
import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod;

import java.util.List;

public class ListReceiptViewModel extends ViewModel {

    private LiveData<PagedList<HyperwalletTransferMethod>> mTransferMethods;
    private LiveData<Boolean> mDisplayLoading;
    private ReceiptRepository mReceiptRepository;

    private MutableLiveData<Event<HyperwalletTransferMethod>> mDetailNavigation = new MutableLiveData<>();
    private MutableLiveData<Event<List<HyperwalletError>>> mErrors = new MutableLiveData<>();
    private Observer<HyperwalletErrors> mErrorsObserver;

    public ListReceiptViewModel(@NonNull final ReceiptRepository repository) {
        mReceiptRepository = repository;
        mTransferMethods = mReceiptRepository.getReceiptList();
        registerObservers();
    }


    private void registerObservers() {
        mErrorsObserver = new Observer<HyperwalletErrors>() {
            @Override
            public void onChanged(HyperwalletErrors hyperwalletErrors) {
                if (hyperwalletErrors != null) {
                    mErrors.setValue(new Event<>(hyperwalletErrors.getErrors()));
                }
            }
        };
        mReceiptRepository.getErrors().observeForever(mErrorsObserver);
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

    public void navigateToDetail(HyperwalletTransferMethod transferMethod) {
        mDetailNavigation.setValue(new Event<>(transferMethod));
    }


    public LiveData<Event<List<HyperwalletError>>> getErrors() {
        return mErrors;
    }


    public LiveData<Event<HyperwalletTransferMethod>> getDetailNavigation() {
        return mDetailNavigation;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mReceiptRepository.getErrors().removeObserver(mErrorsObserver);
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
