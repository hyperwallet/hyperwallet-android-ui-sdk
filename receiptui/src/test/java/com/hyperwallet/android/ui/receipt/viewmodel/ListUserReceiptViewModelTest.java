package com.hyperwallet.android.ui.receipt.viewmodel;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import com.hyperwallet.android.ui.receipt.repository.UserReceiptRepository;
import com.hyperwallet.android.ui.receipt.repository.UserReceiptRepositoryImpl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ListUserReceiptViewModelTest {

    private ReceiptViewModel mReceiptViewModelToTest;
    private ListUserReceiptViewModel.ListReceiptViewModelFactory mListReceiptViewModelfactoryToTest;

    @Before
    public void initializedViewModel() {
        UserReceiptRepository userReceiptRepository = new UserReceiptRepositoryImpl();
        mListReceiptViewModelfactoryToTest = new ListUserReceiptViewModel.
                ListReceiptViewModelFactory(userReceiptRepository);
        mReceiptViewModelToTest = mListReceiptViewModelfactoryToTest.create(ReceiptViewModel.class);
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
    public void testListReceiptViewModelFactory_throwsExceptionOnInvalidClassArgument() {
        mListReceiptViewModelfactoryToTest.create(ListPrepaidCardReceiptViewModel.class);
    }
}