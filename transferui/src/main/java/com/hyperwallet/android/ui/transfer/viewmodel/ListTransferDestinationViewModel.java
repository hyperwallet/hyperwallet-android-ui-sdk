package com.hyperwallet.android.ui.transfer.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.hyperwallet.android.model.HyperwalletErrors;
import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod;
import com.hyperwallet.android.ui.common.repository.Event;
import com.hyperwallet.android.ui.transfermethod.repository.TransferMethodRepository;

import java.util.List;

public class ListTransferDestinationViewModel extends ViewModel {

    private final MutableLiveData<List<HyperwalletTransferMethod>> mTransferDestinationList = new MutableLiveData<>();
    private final MutableLiveData<Event<HyperwalletTransferMethod>> mTransferDestinationSection =
            new MutableLiveData<>();
    private final MutableLiveData<Event<HyperwalletErrors>> mTransferDestinationError = new MutableLiveData<>();
    private final TransferMethodRepository mTransferMethodRepository;

    public ListTransferDestinationViewModel(
            @NonNull final TransferMethodRepository transferMethodRepository) {
        mTransferMethodRepository = transferMethodRepository;
        loadTransferDestinationList();
    }

    public void selectTransferDestination(HyperwalletTransferMethod transferMethod) {
        mTransferDestinationSection.postValue(new Event<>(transferMethod));
    }

    private void loadTransferDestinationList() {
        mTransferMethodRepository.loadTransferMethods(new TransferMethodRepository.LoadTransferMethodListCallback() {
            @Override
            public void onTransferMethodListLoaded(List<HyperwalletTransferMethod> transferMethods) {
                mTransferDestinationList.postValue(transferMethods);
            }

            @Override
            public void onError(HyperwalletErrors errors) {
                mTransferDestinationError.postValue(new Event<>(errors));
            }
        });
    }

    public MutableLiveData<List<HyperwalletTransferMethod>> getTransferDestinationList() {
        return mTransferDestinationList;
    }

    public MutableLiveData<Event<HyperwalletTransferMethod>> getTransferDestinationSection() {
        return mTransferDestinationSection;
    }

    public MutableLiveData<Event<HyperwalletErrors>> getTransferDestinationError() {
        return mTransferDestinationError;
    }

    /**
     * @return live data of loading information
     */
    public LiveData<Boolean> isLoadingData() {
        return mTransferMethodRepository.isLoadingData();
    }

    public static class ListTransferDestinationViewModelFactory implements ViewModelProvider.Factory {

        private final TransferMethodRepository mTransferMethodRepository;

        public ListTransferDestinationViewModelFactory(
                @NonNull final TransferMethodRepository transferMethodRepository) {
            mTransferMethodRepository = transferMethodRepository;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(ListTransferDestinationViewModel.class)) {
                return (T) new ListTransferDestinationViewModel(mTransferMethodRepository);
            }
            throw new IllegalArgumentException(
                    "Expecting ViewModel class: " + ListTransferDestinationViewModel.class.getName());
        }
    }

}
