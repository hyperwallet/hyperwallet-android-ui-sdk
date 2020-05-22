package com.hyperwallet.android.ui.receipt.viewmodel;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.hyperwallet.android.ui.receipt.repository.PrepaidCardReceiptRepository;
import com.hyperwallet.android.ui.receipt.repository.PrepaidCardReceiptRepositoryImpl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ListPrepaidCardReceiptViewModelTest {

    private ReceiptViewModel mReceiptViewModel;
    private ListPrepaidCardReceiptViewModel.ListPrepaidCardReceiptViewModelFactory
            mListPrepaidCardReceiptViewModelFactory;
    private PrepaidCardReceiptRepository mPrepaidCardReceiptRepository;

    @Before
    public void initializedViewModel() {
        mPrepaidCardReceiptRepository = spy(new PrepaidCardReceiptRepositoryImpl("trm-ppc-token"));
        mListPrepaidCardReceiptViewModelFactory =
                new ListPrepaidCardReceiptViewModel.ListPrepaidCardReceiptViewModelFactory(
                        mPrepaidCardReceiptRepository);
        mReceiptViewModel = mListPrepaidCardReceiptViewModelFactory.create(ReceiptViewModel.class);
    }

    @Test
    public void testIsLoadingData_returnsLiveData() {
        assertThat(mReceiptViewModel.isLoadingData(), is(notNullValue()));
    }

    @Test
    public void testGetReceiptErrors_returnsLiveData() {
        assertThat(mReceiptViewModel.getReceiptErrors(), is(notNullValue()));
    }

    @Test
    public void testGetReceiptList_returnsLiveData() {
        assertThat(mReceiptViewModel.getReceiptList(), is(notNullValue()));
    }

    @Test
    public void testGetDetailNavigation_returnsLiveData() {
        assertThat(mReceiptViewModel.getDetailNavigation(), is(notNullValue()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testListPrepaidCardReceiptViewModelFactory_throwsExceptionOnInvalidClassArgument() {
        mListPrepaidCardReceiptViewModelFactory.create(ReceiptDetailViewModel.class);
    }

    @Test
    public void testListPrepaidCardReceiptViewModel() {
        verify(mPrepaidCardReceiptRepository, never()).loadPrepaidCardReceipts();
    }

    @Test
    public void testInit_verifyInitializedOnce() {
        mReceiptViewModel.init();
        verify(mPrepaidCardReceiptRepository).loadPrepaidCardReceipts();
        // call again. multiple calls to init should only register 1 call to repository
        mReceiptViewModel.init();
        verify(mPrepaidCardReceiptRepository).loadPrepaidCardReceipts();
    }

    @Test
    public void testRetry_loadInitial() {
        mReceiptViewModel.retry();
        verify(mPrepaidCardReceiptRepository).retryLoadReceipt();
    }
}