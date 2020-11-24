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
import com.hyperwallet.android.ui.receipt.repository.UserReceiptRepository;

public class ListUserReceiptsViewModel extends ListReceiptsViewModel {

    private MutableLiveData<Event<Errors>> mErrorEvent = new MutableLiveData<>();
    private Observer<Event<Errors>> mErrorEventObserver;
    private UserReceiptRepository mUserReceiptRepository;
    private MutableLiveData<Event<Receipt>> mDetailNavigation = new MutableLiveData<>();

    ListUserReceiptsViewModel(@NonNull final UserReceiptRepository userReceiptRepository) {
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
        // load initial receipts
        mUserReceiptRepository.loadUserReceipts();
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
        mUserReceiptRepository.getErrors().removeObserver(mErrorEventObserver);
        mUserReceiptRepository.cleanup();
        mUserReceiptRepository = null;
    }
}

