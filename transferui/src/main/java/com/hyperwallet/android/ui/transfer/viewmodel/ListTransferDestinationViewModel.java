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

import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.TYPE;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodTypes.PREPAID_CARD;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.hyperwallet.android.model.Errors;
import com.hyperwallet.android.model.transfermethod.TransferMethod;
import com.hyperwallet.android.ui.common.repository.Event;
import com.hyperwallet.android.ui.transfermethod.repository.TransferMethodRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

/**
 * List Transfer Destination ViewModel
 */
public class ListTransferDestinationViewModel extends ViewModel {

    private final MutableLiveData<List<TransferMethod>> mTransferDestinationList = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mIsLoading = new MutableLiveData<>();
    private final MutableLiveData<Event<TransferMethod>> mSelectedTransferDestination =
            new MutableLiveData<>();
    private final MutableLiveData<Event<Errors>> mTransferDestinationError = new MutableLiveData<>();
    private final TransferMethodRepository mTransferMethodRepository;

    private boolean mIsInitialized;
    private boolean mIsSourcePrepaidCard;

    ListTransferDestinationViewModel(@NonNull final TransferMethodRepository repository) {
        mTransferMethodRepository = repository;
    }

    public void init() {
        if (!mIsInitialized) {
            mIsInitialized = true;
            loadTransferDestinationList();
        }
    }

    public void setIsSourcePrepaidCard(boolean isSourcePrepaidCard) {
        mIsSourcePrepaidCard = isSourcePrepaidCard;
    }

    public void selectedTransferDestination(@NonNull final TransferMethod transferMethod) {
        mSelectedTransferDestination.postValue(new Event<>(transferMethod));
    }

    public LiveData<List<TransferMethod>> getTransferDestinationList() {
        return mTransferDestinationList;
    }

    public LiveData<Event<TransferMethod>> getSelectedTransferDestination() {
        return mSelectedTransferDestination;
    }

    public LiveData<Event<Errors>> getTransferDestinationError() {
        return mTransferDestinationError;
    }

    public LiveData<Boolean> isLoading() {
        return mIsLoading;
    }

    public void loadNewlyAddedTransferDestination() {
        mIsLoading.postValue(Boolean.TRUE);
        mTransferMethodRepository.loadTransferMethods(new TransferMethodRepository.LoadTransferMethodListCallback() {
            @Override
            public void onTransferMethodListLoaded(List<TransferMethod> transferMethods) {
                if (transferMethods != null && !transferMethods.isEmpty()) {
                    mTransferDestinationList.postValue(transferMethods);
                    mSelectedTransferDestination.postValue(new Event<>(transferMethods.get(0)));
                } else {
                    mTransferDestinationList.setValue(new ArrayList<TransferMethod>());
                }
                mIsLoading.postValue(Boolean.FALSE);
            }

            @Override
            public void onError(Errors errors) {
                mIsLoading.postValue(Boolean.FALSE);
                mTransferDestinationError.postValue(new Event<>(errors));
            }
        });
    }

    public void loadTransferDestinationList() {
        mIsLoading.postValue(Boolean.TRUE);
        mTransferMethodRepository.loadTransferMethods(new TransferMethodRepository.LoadTransferMethodListCallback() {
            @Override
            public void onTransferMethodListLoaded(List<TransferMethod> transferMethods) {
                if (transferMethods != null) {
                    if (mIsSourcePrepaidCard) {
                        ListIterator<TransferMethod> transferMethod = transferMethods.listIterator();
                        while (transferMethod.hasNext()) {
                            if (Objects.equals(transferMethod.next().getField(TYPE), PREPAID_CARD)) {
                                transferMethod.remove();
                            }
                        }
                    }
                    mTransferDestinationList.postValue(transferMethods);
                } else {
                    mTransferDestinationList.setValue(new ArrayList<TransferMethod>());
                }
                mIsLoading.postValue(Boolean.FALSE);
            }

            @Override
            public void onError(Errors errors) {
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
