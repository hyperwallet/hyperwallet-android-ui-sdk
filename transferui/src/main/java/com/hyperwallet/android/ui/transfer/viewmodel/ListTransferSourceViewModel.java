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

import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.TOKEN;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.TRANSFER_METHOD_CURRENCY;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.hyperwallet.android.model.Error;
import com.hyperwallet.android.model.Errors;
import com.hyperwallet.android.model.transfer.Transfer;
import com.hyperwallet.android.model.transfermethod.PrepaidCard;
import com.hyperwallet.android.model.transfermethod.TransferMethod;
import com.hyperwallet.android.ui.common.repository.Event;
import com.hyperwallet.android.ui.transfer.TransferSourceWrapper;
import com.hyperwallet.android.ui.transfer.repository.TransferRepository;
import com.hyperwallet.android.ui.transfermethod.repository.PrepaidCardRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * List Transfer Source ViewModel
 */
public class ListTransferSourceViewModel extends ViewModel {
    private final MutableLiveData<Event<TransferSourceWrapper>> mSelectedTransferSource =
            new MutableLiveData<>();
    private final MutableLiveData<Event<Errors>> mTransferSourceError = new MutableLiveData<>();
    private final PrepaidCardRepository mPrepaidCardRepository;
    private final MutableLiveData<Boolean> mIsLoading = new MutableLiveData<>();
    private final MutableLiveData<List<TransferSourceWrapper>> mTransferSourceList = new MutableLiveData<>();

    private boolean mIsInitialized;

    ListTransferSourceViewModel(@NonNull final PrepaidCardRepository repository) {
        mPrepaidCardRepository = repository;
    }

    public void init() {
        if (!mIsInitialized) {
            mIsInitialized = true;
            loadTransferSourceList();
        }
    }

    public void selectedTransferSource(@NonNull final TransferSourceWrapper source) {
        mSelectedTransferSource.postValue(new Event<>(source));
    }

    public LiveData<Event<TransferSourceWrapper>> getTransferSourceSelection() {
        return mSelectedTransferSource;
    }

    public LiveData<Event<Errors>> getTransferSourceError() {
        return mTransferSourceError;
    }

    public LiveData<List<TransferSourceWrapper>> getTransferSourceList() {
        return mTransferSourceList;
    }

    public LiveData<Boolean> isLoading() {
        return mIsLoading;
    }

    public void loadTransferSourceList() {
        mIsLoading.postValue(Boolean.TRUE);
        mPrepaidCardRepository.loadPrepaidCards(new PrepaidCardRepository.LoadPrepaidCardsCallback() {
            @Override
            public void onPrepaidCardLoaded(@Nullable List<PrepaidCard> prepaidCardList) {
                mIsLoading.postValue(Boolean.FALSE);
                List<TransferSourceWrapper> sources = new ArrayList<>();
                sources.add(new TransferSourceWrapper("Available Funds", "$ 100.00 usd", "",
                        TransferMethod.TransferMethodTypes.BANK_ACCOUNT, "usr-13b1fa70-2f5c-47c2-b9f0-f9678cc21601"));
                if (prepaidCardList != null) {
                    for (PrepaidCard prepaidCard : prepaidCardList) {
                        sources.add(new TransferSourceWrapper(prepaidCard.getType(), "$ 100.00 usd",
                                prepaidCard.getCardNumber(), TransferMethod.TransferMethodTypes.PREPAID_CARD,
                                prepaidCard.getField(TOKEN)));
                    }
                }
                mTransferSourceList.postValue(sources);
                mIsLoading.postValue(Boolean.FALSE);
            }

            @Override
            public void onError(@NonNull Errors errors) {
                mIsLoading.postValue(Boolean.FALSE);
                mTransferSourceError.postValue(new Event<>(errors));
            }
        });
    }

    public static class ListTransferSourceViewModelFactory implements ViewModelProvider.Factory {

        private final PrepaidCardRepository mPrepaidCardRepository;

        public ListTransferSourceViewModelFactory(@NonNull final PrepaidCardRepository repository) {
            mPrepaidCardRepository = repository;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(ListTransferSourceViewModel.class)) {
                return (T) new ListTransferSourceViewModel(mPrepaidCardRepository);
            }
            throw new IllegalArgumentException(
                    "Expecting ViewModel class: " + ListTransferSourceViewModel.class.getName());
        }
    }
}
