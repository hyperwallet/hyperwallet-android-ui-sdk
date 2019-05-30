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
package com.hyperwallet.android.receipt.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagedList;

import com.hyperwallet.android.model.HyperwalletErrors;
import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod;
import com.hyperwallet.android.receipt.repository.ReceiptRepository;

public class ListReceiptViewModel extends ViewModel {

    private final ReceiptRepository mReceiptRepository;

    public ListReceiptViewModel(@NonNull final ReceiptRepository receiptRepository) {
        mReceiptRepository = receiptRepository;
        // load initial receipts
        mReceiptRepository.loadReceipts();
    }

    public LiveData<Boolean> isLoadingData() {
        return mReceiptRepository.isLoading();
    }

    public LiveData<HyperwalletErrors> getReceiptErrors() {
        return mReceiptRepository.getErrors();
    }

    public LiveData<PagedList<HyperwalletTransferMethod>> getReceiptList() {
        return mReceiptRepository.loadReceipts();
    }

    public void retryLoadReceipts() {
        mReceiptRepository.retryLoadReceipt();
    }

    public static class ListReceiptViewModelFactory implements ViewModelProvider.Factory {

        private final ReceiptRepository mReceiptRepository;

        public ListReceiptViewModelFactory(@NonNull final ReceiptRepository receiptRepository) {
            mReceiptRepository = receiptRepository;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(ListReceiptViewModel.class)) {
                return (T) new ListReceiptViewModel(mReceiptRepository);
            }
            throw new IllegalArgumentException("Expecting ViewModel class: " + ListReceiptViewModel.class.getName());
        }
    }
}
