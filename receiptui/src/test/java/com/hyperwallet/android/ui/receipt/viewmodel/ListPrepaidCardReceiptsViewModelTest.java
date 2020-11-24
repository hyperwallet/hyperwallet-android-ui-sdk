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
public class ListPrepaidCardReceiptsViewModelTest {

    private ListPrepaidCardReceiptsViewModel mListPrepaidCardReceiptsViewModel;
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
        mListPrepaidCardReceiptsViewModel = new ListPrepaidCardReceiptsViewModel(mPrepaidCardReceiptRepository);
    }

    @Test
    public void testIsLoadingData_returnsLiveData() {
        assertThat(mPrepaidCardReceiptRepository.isLoading(), is(notNullValue()));
    }

    @Test
    public void testGetReceiptsList_returnsLiveData() {
        assertThat(mListPrepaidCardReceiptsViewModel.receipts(), is(notNullValue()));
    }

    @Test
    public void testGetDetailNavigation_returnsLiveData() {
        assertThat(mListPrepaidCardReceiptsViewModel.getDetailNavigation(), is(notNullValue()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testListPrepaidCardReceiptViewModelFactory_throwsExceptionOnInvalidClassArgument() {
        mListReceiptsViewModelFactory.create(ReceiptDetailViewModel.class);
    }

    @Test
    public void testListLoadPrepaidCardReceiptsViewModel() {
        verify(mPrepaidCardReceiptRepository, never()).loadPrepaidCardReceipts();
    }

    @Test
    public void testInit_verifyInitializedMultiple() {
        mListPrepaidCardReceiptsViewModel.init();
        verify(mPrepaidCardReceiptRepository).loadPrepaidCardReceipts();
        // call again. multiple calls to init should only register 1 call to repository
        mListPrepaidCardReceiptsViewModel.init();
        verify(mPrepaidCardReceiptRepository, times(2)).loadPrepaidCardReceipts();
    }

    @Test
    public void testRetry_loadInitial() {
        mListPrepaidCardReceiptsViewModel.retry();
        verify(mPrepaidCardReceiptRepository).retryLoadReceipt();
    }
}
