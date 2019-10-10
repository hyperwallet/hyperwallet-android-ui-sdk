/*
 * The MIT License (MIT)
 * Copyright (c) 2019 Hyperwallet Systems Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
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

import java.util.ArrayList;
import java.util.List;

/**
 * List Transfer Destination ViewModel
 */
public class ListTransferDestinationViewModel extends ViewModel {

    private final MutableLiveData<List<HyperwalletTransferMethod>> mTransferDestinationList = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mIsLoading = new MutableLiveData<>();
    private final MutableLiveData<Event<HyperwalletTransferMethod>> mSelectedTransferDestination =
            new MutableLiveData<>();
    private final MutableLiveData<Event<HyperwalletErrors>> mTransferDestinationError = new MutableLiveData<>();
    private final TransferMethodRepository mTransferMethodRepository;

    ListTransferDestinationViewModel(@NonNull final TransferMethodRepository repository) {
        mTransferMethodRepository = repository;
        loadTransferDestinationList();
    }

    public void selectedTransferDestination(@NonNull final HyperwalletTransferMethod transferMethod) {
        mSelectedTransferDestination.postValue(new Event<>(transferMethod));
    }

    public LiveData<List<HyperwalletTransferMethod>> getTransferDestinationList() {
        return mTransferDestinationList;
    }

    public LiveData<Event<HyperwalletTransferMethod>> getSelectedTransferDestination() {
        return mSelectedTransferDestination;
    }

    public LiveData<Event<HyperwalletErrors>> getTransferDestinationError() {
        return mTransferDestinationError;
    }

    public LiveData<Boolean> isLoading() {
        return mIsLoading;
    }

    public void loadNewlyAddedTransferDestination() {
        mIsLoading.postValue(Boolean.TRUE);
        mTransferMethodRepository.loadTransferMethods(new TransferMethodRepository.LoadTransferMethodListCallback() {
            @Override
            public void onTransferMethodListLoaded(List<HyperwalletTransferMethod> transferMethods) {
                if (transferMethods != null && !transferMethods.isEmpty()) {
                    mTransferDestinationList.postValue(transferMethods);
                    mSelectedTransferDestination.postValue(new Event<>(transferMethods.get(0)));
                } else {
                    mTransferDestinationList.setValue(new ArrayList<HyperwalletTransferMethod>());
                }
                mIsLoading.postValue(Boolean.FALSE);
            }

            @Override
            public void onError(HyperwalletErrors errors) {
                mIsLoading.postValue(Boolean.FALSE);
                mTransferDestinationError.postValue(new Event<>(errors));
            }
        });
    }

    public void loadTransferDestinationList() {
        mIsLoading.postValue(Boolean.TRUE);
        mTransferMethodRepository.loadTransferMethods(new TransferMethodRepository.LoadTransferMethodListCallback() {
            @Override
            public void onTransferMethodListLoaded(List<HyperwalletTransferMethod> transferMethods) {
                if (transferMethods != null) {
                    mTransferDestinationList.postValue(transferMethods);
                } else {
                    mTransferDestinationList.setValue(new ArrayList<HyperwalletTransferMethod>());
                }
                mIsLoading.postValue(Boolean.FALSE);
            }

            @Override
            public void onError(HyperwalletErrors errors) {
                mIsLoading.postValue(Boolean.FALSE);
                mTransferDestinationError.postValue(new Event<>(errors));
            }
        });
    }

    public static class ListTransferDestinationViewModelFactory implements ViewModelProvider.Factory {

        private final TransferMethodRepository mTransferMethodRepository;

        public ListTransferDestinationViewModelFactory(@NonNull final TransferMethodRepository repository) {
            mTransferMethodRepository = repository;
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
