package com.hyperwallet.android.ui.receipt.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.hyperwallet.android.model.HyperwalletErrors;
import com.hyperwallet.android.model.receipt.Receipt;
import com.hyperwallet.android.ui.common.repository.Event;

public class PrepaidCardReceiptRepositoryImpl implements PrepaidCardReceiptRepository {

    private static final int PAGE_SIZE = 10;
    private static final int INITIAL_LOAD_SIZE = 20;

    private final PrepaidCardReceiptDataSourceFactory mDataSourceFactory;
    private final LiveData<PrepaidCardReceiptDataSource> mReceiptDataSourceLiveData;
    private LiveData<Event<HyperwalletErrors>> mErrorsLiveData;
    private LiveData<Boolean> mIsFetchingData;
    private LiveData<PagedList<Receipt>> mReceiptsLiveData;

    public PrepaidCardReceiptRepositoryImpl(@NonNull final String token) {
        mDataSourceFactory = new PrepaidCardReceiptDataSourceFactory(token);
        mReceiptDataSourceLiveData = mDataSourceFactory.getPrepaidCardReceiptDataSource();
    }

    /**
     * @see PrepaidCardReceiptRepository#loadPrepaidCardReceipts()
     */
    @Override
    public LiveData<PagedList<Receipt>> loadPrepaidCardReceipts() {
        if (mReceiptsLiveData == null) {
            PagedList.Config config = new PagedList.Config.Builder()
                    .setPageSize(PAGE_SIZE)
                    .setEnablePlaceholders(true)
                    .setInitialLoadSizeHint(INITIAL_LOAD_SIZE)
                    .build();
            mReceiptsLiveData = new LivePagedListBuilder<>(mDataSourceFactory, config).build();
        }
        return mReceiptsLiveData;
    }

    /**
     * @see PrepaidCardReceiptRepository#isLoading()
     */
    @Override
    public LiveData<Boolean> isLoading() {
        if (mIsFetchingData == null) {
            mIsFetchingData = mReceiptDataSourceLiveData.getValue().isFetchingData();
        }
        return mIsFetchingData;
    }

    /**
     * @see PrepaidCardReceiptRepository#getErrors()
     */
    @Override
    public LiveData<Event<HyperwalletErrors>> getErrors() {
        if (mErrorsLiveData == null) {
            mErrorsLiveData = mReceiptDataSourceLiveData.getValue().getErrors();
        }
        return mErrorsLiveData;
    }

    /**
     * @see PrepaidCardReceiptRepository#retryLoadReceipt()
     */
    @Override
    public void retryLoadReceipt() {
        if (mReceiptDataSourceLiveData.getValue() != null) {
            mReceiptDataSourceLiveData.getValue().retry();
        }
    }
}
