package com.hyperwallet.android.ui.receipt.viewmodel;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.hyperwallet.android.ui.receipt.repository.PrepaidCardReceiptRepository;
import com.hyperwallet.android.ui.receipt.repository.PrepaidCardReceiptRepositoryImpl;
import com.hyperwallet.android.ui.receipt.repository.UserReceiptRepository;
import com.hyperwallet.android.ui.receipt.repository.UserReceiptRepositoryImpl;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ListUserReceiptsViewModelTest {

    private ListUserReceiptsViewModel mListUserReceiptsViewModel;
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
        mListUserReceiptsViewModel = new ListUserReceiptsViewModel(mUserReceiptRepository);
    }

    @Test
    public void testIsLoadingData_returnsLiveData() {
        assertThat(mListUserReceiptsViewModel.isLoading(), is(notNullValue()));
    }

    @Test
    public void testGetReceiptList_returnsLiveData() {
        assertThat(mListUserReceiptsViewModel.receipts(), is(notNullValue()));
    }

    @Test
    public void testGetDetailNavigation_returnsLiveData() {
        MatcherAssert.assertThat(mListUserReceiptsViewModel.getDetailNavigation(),
                CoreMatchers.is(CoreMatchers.notNullValue()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testListPrepaidCardReceiptViewModelFactory_throwsExceptionOnInvalidClassArgument() {
        mListReceiptsViewModelFactory.create(ReceiptDetailViewModel.class);
    }

    @Test
    public void testListUserReceiptViewModel() {
        verify(mUserReceiptRepository, never()).loadUserReceipts();
    }

    @Test
    public void testInit_verifyInitializedOnce() {
        mListUserReceiptsViewModel.init();
        verify(mUserReceiptRepository).loadUserReceipts();
        // call again. multiple calls to init should only register 1 call to repository
        mListUserReceiptsViewModel.init();
        verify(mUserReceiptRepository).loadUserReceipts();
    }

    @Test
    public void testRetry_loadInitial() {
        mListUserReceiptsViewModel.retry();
        verify(mUserReceiptRepository).retryLoadReceipt();
    }
}
