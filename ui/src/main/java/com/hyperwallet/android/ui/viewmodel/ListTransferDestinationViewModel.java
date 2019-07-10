package com.hyperwallet.android.ui.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.hyperwallet.android.model.HyperwalletErrors;
import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod;
import com.hyperwallet.android.ui.common.viewmodel.Event;
import com.hyperwallet.android.ui.repository.TransferMethodRepository;

import java.util.List;

public class ListTransferDestinationViewModel extends ViewModel {

    private MutableLiveData<List<HyperwalletTransferMethod>> mTransferMethodList;
    private MutableLiveData<Event<HyperwalletTransferMethod>> mSelection;
    private TransferMethodRepository mTransferMethodRepository;

    public ListTransferDestinationViewModel(@NonNull final TransferMethodRepository transferMethodRepository) {
        mTransferMethodRepository = transferMethodRepository;
    }

    public LiveData<List<HyperwalletTransferMethod>> getTransferDestinationList() {
        if (mTransferMethodList == null) {
            mTransferMethodList = new MutableLiveData<>();
            loadTransferMethods();
        }
        return mTransferMethodList;
    }


    public LiveData<Event<HyperwalletTransferMethod>> getTransferMethodSelection() {
        if (mSelection == null) {
            mSelection = new MutableLiveData<>();
        }
        return mSelection;
    }


    public void selectTransferMethod(@NonNull final HyperwalletTransferMethod transferMethod) {
        mSelection.postValue(new Event<>(transferMethod));
    }

    private void loadTransferMethods() {
        mTransferMethodRepository.loadTransferMethods(new TransferMethodRepository.LoadTransferMethodListCallback() {
            @Override
            public void onTransferMethodListLoaded(List<HyperwalletTransferMethod> transferMethods) {
                mTransferMethodList.postValue(transferMethods);
            }

            @Override
            public void onError(HyperwalletErrors errors) {

            }
        });
    }


    public static class ListTransferDestinationViewModelFactory implements ViewModelProvider.Factory {

        private TransferMethodRepository mTransferMethodRepository;

        public ListTransferDestinationViewModelFactory(@NonNull final TransferMethodRepository transferMethodRepository) {
            mTransferMethodRepository = transferMethodRepository;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new ListTransferDestinationViewModel(mTransferMethodRepository);
        }
    }

}
