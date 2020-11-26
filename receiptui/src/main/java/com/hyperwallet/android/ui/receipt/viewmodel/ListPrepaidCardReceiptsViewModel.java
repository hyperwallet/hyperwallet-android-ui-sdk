package com.hyperwallet.android.ui.receipt.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.paging.PagedList;

import com.hyperwallet.android.model.Errors;
import com.hyperwallet.android.model.receipt.Receipt;
import com.hyperwallet.android.ui.common.repository.Event;
import com.hyperwallet.android.ui.receipt.repository.PrepaidCardReceiptRepository;

public class ListPrepaidCardReceiptsViewModel extends ListReceiptsViewModel {

    private MutableLiveData<Event<Errors>> mErrorEvent = new MutableLiveData<>();
    private Observer<Event<Errors>> mErrorEventObserver;
    private PrepaidCardReceiptRepository mPrepaidCardReceiptRepository;
    private MutableLiveData<Event<Receipt>> mDetailNavigation = new MutableLiveData<>();

    ListPrepaidCardReceiptsViewModel(@NonNull final PrepaidCardReceiptRepository receiptRepository) {
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
        mPrepaidCardReceiptRepository.loadPrepaidCardReceipts();
    }

    @Override
    public void refresh() {
        mPrepaidCardReceiptRepository.refresh();
    }

    @Override
    public void retry() {
        mPrepaidCardReceiptRepository.retryLoadReceipt();
    }

    @Override
    public LiveData<Event<Receipt>> getDetailNavigation() {
        return mDetailNavigation;
    }

    @Override
    public void setDetailNavigation(@NonNull final Receipt receipt) {
        mDetailNavigation.postValue(new Event<>(receipt));
    }

    /**
     * @see ViewModel#onCleared()
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        mPrepaidCardReceiptRepository.getErrors().removeObserver(mErrorEventObserver);
        mPrepaidCardReceiptRepository.cleanup();
    }
}
