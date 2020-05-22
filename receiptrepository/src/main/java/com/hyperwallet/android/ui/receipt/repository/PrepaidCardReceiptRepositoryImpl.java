package com.hyperwallet.android.ui.receipt.repository;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.hyperwallet.android.model.Errors;
import com.hyperwallet.android.model.receipt.Receipt;
import com.hyperwallet.android.ui.common.repository.Event;

public class PrepaidCardReceiptRepositoryImpl implements PrepaidCardReceiptRepository {

    private static final int PAGE_SIZE = 10;
    private static final int INITIAL_LOAD_SIZE = 20;

    private final PrepaidCardReceiptDataSourceFactory mDataSourceFactory;
    private final LiveData<PrepaidCardReceiptDataSource> mReceiptDataSourceLiveData;
    private LiveData<PagedList<Receipt>> mReceiptsLiveData;
    private MutableLiveData<Boolean> mRefreshDataSource = new MutableLiveData<>();
    private Observer<PrepaidCardReceiptDataSource> mReceiptDataSourceObserver =
            new Observer<PrepaidCardReceiptDataSource>() {
                @Override
                public void onChanged(PrepaidCardReceiptDataSource dataSource) {
                    mRefreshDataSource.postValue(Boolean.TRUE);
                }
            };

    public PrepaidCardReceiptRepositoryImpl(@NonNull final String token) {
        mDataSourceFactory = new PrepaidCardReceiptDataSourceFactory(token);
        mReceiptDataSourceLiveData = mDataSourceFactory.getPrepaidCardReceiptDataSource();
        mReceiptDataSourceLiveData.observeForever(mReceiptDataSourceObserver);
        mRefreshDataSource.postValue(Boolean.FALSE);
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
        return Transformations.switchMap(mRefreshDataSource, new Function<Boolean, LiveData<Boolean>>() {
            @Override
            public LiveData<Boolean> apply(Boolean input) {
                return mReceiptDataSourceLiveData.getValue().isFetchingData();
            }
        });
    }

    /**
     * @see PrepaidCardReceiptRepository#getErrors()
     */
    @Override
    public LiveData<Event<Errors>> getErrors() {
        return Transformations.switchMap(mRefreshDataSource, new Function<Boolean, LiveData<Event<Errors>>>() {
            @Override
            public LiveData<Event<Errors>> apply(Boolean input) {
                return mReceiptDataSourceLiveData.getValue().getErrors();
            }
        });
    }

    /**
     * @see PrepaidCardReceiptRepository#retryLoadReceipt()
     */
    @Override
    public void retryLoadReceipt() {
        mReceiptDataSourceLiveData.getValue().retry();
    }

    @Override
    public void refresh() {
        mReceiptDataSourceLiveData.getValue().invalidate();
    }

    @Override
    public void cleanup() {
        mReceiptDataSourceLiveData.removeObserver(mReceiptDataSourceObserver);
    }
}
