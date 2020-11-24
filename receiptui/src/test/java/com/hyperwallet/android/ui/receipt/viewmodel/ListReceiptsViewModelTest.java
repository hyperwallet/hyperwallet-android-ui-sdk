package com.hyperwallet.android.ui.receipt.viewmodel;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.hyperwallet.android.ui.receipt.repository.PrepaidCardReceiptRepository;
import com.hyperwallet.android.ui.receipt.repository.PrepaidCardReceiptRepositoryImpl;
import com.hyperwallet.android.ui.receipt.repository.UserReceiptRepository;
import com.hyperwallet.android.ui.receipt.repository.UserReceiptRepositoryImpl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ListReceiptsViewModelTest {
    private ListReceiptsViewModel mListReceiptsViewModel;
    private PrepaidCardReceiptRepository mPrepaidCardReceiptRepository;
    private UserReceiptRepository mUserReceiptRepository;
    private ListReceiptsViewModel.ListReceiptsViewModelFactory mListReceiptsViewModelFactory;

    @Before
    public void initializedViewModel() {
        String prepaidToken = "trm-ppc-token";
        mPrepaidCardReceiptRepository = spy(new PrepaidCardReceiptRepositoryImpl(prepaidToken));
        mUserReceiptRepository = spy(new UserReceiptRepositoryImpl());
        mListReceiptsViewModelFactory = new ListReceiptsViewModel.ListReceiptsViewModelFactory(prepaidToken,
                mUserReceiptRepository, mPrepaidCardReceiptRepository);
        mListReceiptsViewModel = mListReceiptsViewModelFactory.create(ListReceiptsViewModel.class);
    }

    @Test
    public void testIsLoadingData_returnsLiveData() {
        assertThat(mListReceiptsViewModel.isLoading(), is(notNullValue()));
    }

    @Test
    public void testGetReceiptList_returnsLiveData() {
        assertThat(mListReceiptsViewModel.receipts(), is(notNullValue()));
    }

    @Test
    public void testGetDetailNavigation_returnsLiveData() {
        assertThat(mListReceiptsViewModel.getDetailNavigation(), is(notNullValue()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testListPrepaidCardReceiptViewModelFactory_throwsExceptionOnInvalidClassArgument() {
        mListReceiptsViewModelFactory.create(ReceiptDetailViewModel.class);
    }

    @Test
    public void testLoadPrepaidCardReceiptsViewModel() {
        verify(mPrepaidCardReceiptRepository, never()).loadPrepaidCardReceipts();
    }

    @Test
    public void testInit_verifyInitializedMultiple() {
        mListReceiptsViewModel.init();
        verify(mPrepaidCardReceiptRepository).loadPrepaidCardReceipts();
        // call again. multiple calls to init should only register 1 call to repository
        mListReceiptsViewModel.init();
        verify(mPrepaidCardReceiptRepository, times(2)).loadPrepaidCardReceipts();
    }

    @Test
    public void testRetry_loadInitial() {
        mListReceiptsViewModel.retry();
        verify(mPrepaidCardReceiptRepository).retryLoadReceipt();
    }
}
