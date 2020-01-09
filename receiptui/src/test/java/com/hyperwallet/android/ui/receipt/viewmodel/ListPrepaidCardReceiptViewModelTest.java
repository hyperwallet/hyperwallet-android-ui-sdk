package com.hyperwallet.android.ui.receipt.viewmodel;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.hyperwallet.android.ui.receipt.repository.PrepaidCardReceiptRepository;
import com.hyperwallet.android.ui.receipt.repository.PrepaidCardReceiptRepositoryImpl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ListPrepaidCardReceiptViewModelTest {

    private ReceiptViewModel mReceiptViewModelToTest;
    private ListPrepaidCardReceiptViewModel.ListPrepaidCardReceiptViewModelFactory
            mListPrepaidCardReceiptViewModelFactory;
    private PrepaidCardReceiptRepository mPrepaidCardReceiptRepositoryTest;

    @Before
    public void initializedViewModel() {
        mPrepaidCardReceiptRepositoryTest = spy(new PrepaidCardReceiptRepositoryImpl("trm-ppc-token"));
        mListPrepaidCardReceiptViewModelFactory =
                new ListPrepaidCardReceiptViewModel.ListPrepaidCardReceiptViewModelFactory(
                        mPrepaidCardReceiptRepositoryTest);
        mReceiptViewModelToTest = mListPrepaidCardReceiptViewModelFactory.create(ReceiptViewModel.class);
    }

    @Test
    public void testIsLoadingData_returnsLiveData() {
        assertThat(mReceiptViewModelToTest.isLoadingData(), is(notNullValue()));
    }

    @Test
    public void testGetReceiptErrors_returnsLiveData() {
        assertThat(mReceiptViewModelToTest.getReceiptErrors(), is(notNullValue()));
    }

    @Test
    public void testGetReceiptList_returnsLiveData() {
        assertThat(mReceiptViewModelToTest.getReceiptList(), is(notNullValue()));
    }

    @Test
    public void testGetDetailNavigation_returnsLiveData() {
        assertThat(mReceiptViewModelToTest.getDetailNavigation(), is(notNullValue()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testListPrepaidCardReceiptViewModelFactory_throwsExceptionOnInvalidClassArgument() {
        mListPrepaidCardReceiptViewModelFactory.create(ReceiptDetailViewModel.class);
    }

    @Test
    public void testListPrepaidCardReceiptViewModel() {
        verify(mPrepaidCardReceiptRepositoryTest, never()).loadPrepaidCardReceipts();
    }

    @Test
    public void testInit() {
        mReceiptViewModelToTest.init();
        verify(mPrepaidCardReceiptRepositoryTest, times(1)).loadPrepaidCardReceipts();
    }
}