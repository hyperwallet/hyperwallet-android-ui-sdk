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
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.hyperwallet.android.model.HyperwalletErrors;
import com.hyperwallet.android.model.StatusTransition;
import com.hyperwallet.android.model.transfer.Transfer;
import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod;
import com.hyperwallet.android.ui.common.repository.Event;
import com.hyperwallet.android.ui.transfer.repository.TransferRepository;

/**
 * Schedule Transfer View Model
 */
public class ScheduleTransferViewModel extends ViewModel {

    private Transfer mTransfer;
    private HyperwalletTransferMethod mTransferDestination;
    private TransferRepository mTransferRepository;
    private MutableLiveData<StatusTransition> mTransferStatusTransition = new MutableLiveData<>();
    private MutableLiveData<Event<HyperwalletErrors>> mTransferStatusTransitionError = new MutableLiveData<>();
    private MutableLiveData<Boolean> mIsScheduleTransferLoading = new MutableLiveData<>();

    ScheduleTransferViewModel(@NonNull final TransferRepository transferRepository) {
        mTransferRepository = transferRepository;
        mIsScheduleTransferLoading.setValue(Boolean.FALSE);
    }

    public void scheduleTransfer() {
        mIsScheduleTransferLoading.postValue(Boolean.TRUE);
        mTransferRepository.scheduleTransfer(mTransfer, new TransferRepository.ScheduleTransferCallback() {
            @Override
            public void onTransferScheduled(@Nullable StatusTransition statusTransition) {
                mIsScheduleTransferLoading.postValue(Boolean.FALSE);
                mTransferStatusTransition.postValue(statusTransition);
            }

            @Override
            public void onError(@NonNull HyperwalletErrors errors) {
                mIsScheduleTransferLoading.postValue(Boolean.FALSE);
            }
        });
    }

    public void setTransfer(@NonNull final Transfer transfer) {
        mTransfer = transfer;
    }

    public Transfer getTransfer() {
        return mTransfer;
    }

    public void setTransferDestination(@NonNull final HyperwalletTransferMethod transferDestination) {
        mTransferDestination = transferDestination;
    }

    public HyperwalletTransferMethod getTransferDestination() {
        return mTransferDestination;
    }

    public LiveData<StatusTransition> getTransferStatusTransition() {
        return mTransferStatusTransition;
    }

    public LiveData<Event<HyperwalletErrors>> getTransferStatusTransitionError() {
        return mTransferStatusTransitionError;
    }

    public LiveData<Boolean> isScheduleTransferLoading() {
        return mIsScheduleTransferLoading;
    }

    public static class ScheduleTransferViewModelFactory implements ViewModelProvider.Factory {

        private TransferRepository transferRepository;

        public ScheduleTransferViewModelFactory(@NonNull final TransferRepository transferRepository) {
            this.transferRepository = transferRepository;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new ScheduleTransferViewModel(transferRepository);
        }
    }
}
