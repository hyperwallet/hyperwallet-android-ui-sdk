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
import com.hyperwallet.android.ui.receipt.repository.UserReceiptRepository;

public abstract class ListReceiptsViewModel extends ViewModel {
    /**
     * Initialize loading receipts
     */
    public abstract void init();

    /**
     * Refresh receipts
     */
    public abstract void refresh();

    /**
     * @return live data of isloading information
     */
    public abstract LiveData<Boolean> isLoading();

    /**
     * @return live data of receipt errors [Errors]
     */
    public abstract LiveData<Event<Errors>> errors();

    /**
     * @return paged live data of receipts [Receipt]
     */
    public abstract LiveData<PagedList<Receipt>> receipts();

    /**
     * Explicit invoke of load retry on receipts data
     */
    public abstract void retry();

    public static class ListUserReceiptsViewModel extends ListReceiptsViewModel {

        private MutableLiveData<Event<Errors>> mErrorEvent = new MutableLiveData<>();
        private Observer<Event<Errors>> mErrorEventObserver;
        private UserReceiptRepository mUserReceiptRepository;

        private boolean mIsInitialized;

        private ListUserReceiptsViewModel(@NonNull final UserReceiptRepository userReceiptRepository) {
            mUserReceiptRepository = userReceiptRepository;

            // register one time error event observer
            mErrorEventObserver = new Observer<Event<Errors>>() {
                @Override
                public void onChanged(Event<Errors> event) {
                    mErrorEvent.postValue(event);
                }
            };
            mUserReceiptRepository.getErrors().observeForever(mErrorEventObserver);
        }

        @Override
        public void init() {
            if (!mIsInitialized) {
                mIsInitialized = true;
                // load initial receipts
                mUserReceiptRepository.loadUserReceipts();
            }
        }

        @Override
        public void refresh() {
            mUserReceiptRepository.refresh();
        }

        @Override
        public LiveData<Boolean> isLoading() {
            return mUserReceiptRepository.isLoading();
        }

        @Override
        public LiveData<Event<Errors>> errors() {
            return mErrorEvent;
        }

        @Override
        public LiveData<PagedList<Receipt>> receipts() {
            return mUserReceiptRepository.loadUserReceipts();
        }

        @Override
        public void retry() {
            mUserReceiptRepository.retryLoadReceipt();
        }
    }

    public static class ListPrepaidCardReceiptsViewModel extends ListReceiptsViewModel {

        private MutableLiveData<Event<Errors>> mErrorEvent = new MutableLiveData<>();
        private Observer<Event<Errors>> mErrorEventObserver;
        private PrepaidCardReceiptRepository mPrepaidCardReceiptRepository;

        private boolean mIsInitialized;

        private ListPrepaidCardReceiptsViewModel(@NonNull final PrepaidCardReceiptRepository receiptRepository) {
            mPrepaidCardReceiptRepository = receiptRepository;

            mErrorEventObserver = new Observer<Event<Errors>>() {
                @Override
                public void onChanged(Event<Errors> event) {
                    mErrorEvent.postValue(event);
                }
            };
            mPrepaidCardReceiptRepository.getErrors().observeForever(mErrorEventObserver);
        }

        @Override
        public LiveData<Boolean> isLoading() {
            return mPrepaidCardReceiptRepository.isLoading();
        }

        @Override
        public LiveData<Event<Errors>> errors() {
            return mErrorEvent;
        }

        @Override
        public LiveData<PagedList<Receipt>> receipts() {
            return mPrepaidCardReceiptRepository.loadPrepaidCardReceipts();
        }

        @Override
        public void init() {
            if (!mIsInitialized) {
                mIsInitialized = true;
                mPrepaidCardReceiptRepository.loadPrepaidCardReceipts();
            }
        }

        @Override
        public void refresh() {
            mPrepaidCardReceiptRepository.refresh();
        }

        @Override
        public void retry() {
            mPrepaidCardReceiptRepository.retryLoadReceipt();
        }
    }

    public static class ListReceiptsViewModelFactory implements ViewModelProvider.Factory {
        private String prepaidCardToken;
        private UserReceiptRepository mUserReceiptRepository;
        private PrepaidCardReceiptRepository mPrepaidCardReceiptRepository;

        public ListReceiptsViewModelFactory(String prepaidCardToken,
                UserReceiptRepository userReceiptRepository,
                PrepaidCardReceiptRepository prepaidCardReceiptRepository) {
            this.prepaidCardToken = prepaidCardToken;
            mUserReceiptRepository = userReceiptRepository;
            mPrepaidCardReceiptRepository = prepaidCardReceiptRepository;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(ListReceiptsViewModel.class)) {
                if (prepaidCardToken.isEmpty()) {
                    return (T) new ListUserReceiptsViewModel(mUserReceiptRepository);
                }
                return (T) new ListPrepaidCardReceiptsViewModel(mPrepaidCardReceiptRepository);
            }

            throw new IllegalArgumentException(
                    "Expecting ViewModel class: " + ListReceiptsViewModel.class.getName());
        }
    }
}
