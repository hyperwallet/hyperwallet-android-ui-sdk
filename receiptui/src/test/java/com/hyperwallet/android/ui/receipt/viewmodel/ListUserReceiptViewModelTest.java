package com.hyperwallet.android.ui.receipt.viewmodel;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.hyperwallet.android.ui.receipt.repository.UserReceiptRepository;
import com.hyperwallet.android.ui.receipt.repository.UserReceiptRepositoryImpl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ListUserReceiptViewModelTest {

    private ReceiptViewModel mReceiptViewModel;
    private ListUserReceiptViewModel.ListReceiptViewModelFactory mListReceiptViewModelFactory;
    private UserReceiptRepository mUserReceiptRepository;

    @Before
    public void initializedViewModel() {
        mUserReceiptRepository = spy(new UserReceiptRepositoryImpl());
        mListReceiptViewModelFactory = new ListUserReceiptViewModel.ListReceiptViewModelFactory(
                mUserReceiptRepository);
        mReceiptViewModel = mListReceiptViewModelFactory.create(ReceiptViewModel.class);
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
    public void testListReceiptViewModelFactory_throwsExceptionOnInvalidClassArgument() {
        mListReceiptViewModelFactory.create(ListPrepaidCardReceiptViewModel.class);
    }

    @Test
    public void testListUserReceiptViewModel() {
        verify(mUserReceiptRepository, never()).loadUserReceipts();
    }

    @Test
    public void testInit_verifyInitializedOnce() {
        mReceiptViewModel.init();
        verify(mUserReceiptRepository).loadUserReceipts();
        // call again. multiple calls to init should only register 1 call to repository
        mReceiptViewModel.init();
        verify(mUserReceiptRepository).loadUserReceipts();
    }
}