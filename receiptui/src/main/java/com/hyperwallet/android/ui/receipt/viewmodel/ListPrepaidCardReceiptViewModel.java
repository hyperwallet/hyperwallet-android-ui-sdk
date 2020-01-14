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
package com.hyperwallet.android.ui.receipt.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagedList;

import com.hyperwallet.android.model.Errors;
import com.hyperwallet.android.model.receipt.Receipt;
import com.hyperwallet.android.ui.common.repository.Event;
import com.hyperwallet.android.ui.receipt.repository.PrepaidCardReceiptRepository;

public class ListPrepaidCardReceiptViewModel extends ReceiptViewModel {

    private MutableLiveData<Event<Errors>> mErrorEvent = new MutableLiveData<>();
    private MutableLiveData<Event<Receipt>> mDetailNavigation = new MutableLiveData<>();
    private Observer<Event<Errors>> mErrorEventObserver;
    private PrepaidCardReceiptRepository mPrepaidCardReceiptRepository;

    private boolean mIsInitialized;

    private ListPrepaidCardReceiptViewModel(@NonNull final PrepaidCardReceiptRepository receiptRepository) {
        mPrepaidCardReceiptRepository = receiptRepository;

        // register one time error event observer
        mErrorEventObserver = new Observer<Event<Errors>>() {
            @Override
            public void onChanged(Event<Errors> event) {
                mErrorEvent.postValue(event);
            }
        };
        mPrepaidCardReceiptRepository.getErrors().observeForever(mErrorEventObserver);
    }

    @Override
    public void init() {
        if (!mIsInitialized) {
            mIsInitialized = true;
            // load initial receipts
            mPrepaidCardReceiptRepository.loadPrepaidCardReceipts();
        }
    }

    /**
     * @see ReceiptViewModel#isLoadingData()
     */
    @Override
    public LiveData<Boolean> isLoadingData() {
        return mPrepaidCardReceiptRepository.isLoading();
    }

    /**
     * @see ReceiptViewModel#getReceiptErrors()
     */
    @Override
    public LiveData<Event<Errors>> getReceiptErrors() {
        return mErrorEvent;
    }

    /**
     * @see ReceiptViewModel#getReceiptList()
     */
    @Override
    public LiveData<PagedList<Receipt>> getReceiptList() {
        return mPrepaidCardReceiptRepository.loadPrepaidCardReceipts();
    }

    /**
     * @see ReceiptViewModel#retryLoadReceipts()
     */
    @Override
    public void retryLoadReceipts() {
        mPrepaidCardReceiptRepository.retryLoadReceipt();
    }

    /**
     * @see ReceiptViewModel#getDetailNavigation()
     */
    @Override
    public LiveData<Event<Receipt>> getDetailNavigation() {
        return mDetailNavigation;
    }

    /**
     * @see ReceiptViewModel#setDetailNavigation(Receipt)
     */
    @Override
    public void setDetailNavigation(@NonNull Receipt receipt) {
        mDetailNavigation.postValue(new Event<>(receipt));
    }

    /**
     * @see ViewModel#onCleared()
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        mPrepaidCardReceiptRepository.getErrors().removeObserver(mErrorEventObserver);
        mPrepaidCardReceiptRepository = null;
    }

    public static class ListPrepaidCardReceiptViewModelFactory implements ViewModelProvider.Factory {

        private final PrepaidCardReceiptRepository mPrepaidCardReceiptRepository;

        public ListPrepaidCardReceiptViewModelFactory(@NonNull final PrepaidCardReceiptRepository repository) {
            mPrepaidCardReceiptRepository = repository;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull final Class<T> modelClass) {
            if (modelClass.isAssignableFrom(ListPrepaidCardReceiptViewModel.class)) {
                return (T) new ListPrepaidCardReceiptViewModel(mPrepaidCardReceiptRepository);
            }
            throw new IllegalArgumentException(
                    "Expecting ViewModel class: " + ListPrepaidCardReceiptViewModel.class.getName());
        }
    }
}
