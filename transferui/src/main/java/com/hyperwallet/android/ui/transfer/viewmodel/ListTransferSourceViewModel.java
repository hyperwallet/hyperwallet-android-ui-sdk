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

    private boolean mIsInitialized;

    ListTransferSourceViewModel() {
    }

    public void init() {
        if (!mIsInitialized) {
            mIsInitialized = true;
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

    public static class ListTransferSourceViewModelFactory implements ViewModelProvider.Factory {


        public ListTransferSourceViewModelFactory() {

        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(ListTransferSourceViewModel.class)) {
                return (T) new ListTransferSourceViewModel();
            }
            throw new IllegalArgumentException(
                    "Expecting ViewModel class: " + ListTransferSourceViewModel.class.getName());
        }
    }
}
