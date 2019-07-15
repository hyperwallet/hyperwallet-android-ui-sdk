package com.hyperwallet.android.ui.receipt.viewmodel;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import com.hyperwallet.android.ui.receipt.repository.PrepaidCardReceiptRepository;
import com.hyperwallet.android.ui.receipt.repository.PrepaidCardReceiptRepositoryImpl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ListPrepaidCardReceiptViewModelTest {

    private ReceiptViewModel mReceiptViewModelToTest;

    @Before
    public void initializedViewModel() {
        PrepaidCardReceiptRepository prepaidCardReceiptRepository = new PrepaidCardReceiptRepositoryImpl(
                "trm-ppc-token");
        ListPrepaidCardReceiptViewModel.ListPrepaidCardReceiptViewModelFactory factory =
                new ListPrepaidCardReceiptViewModel.ListPrepaidCardReceiptViewModelFactory(
                        prepaidCardReceiptRepository);

        mReceiptViewModelToTest = factory.create(ReceiptViewModel.class);
    }

    @Test
    public void testIsLoadingData_liveDataNotNull() {
        assertThat(mReceiptViewModelToTest.isLoadingData(), is(notNullValue()));
    }

    @Test
    public void testGetReceiptErrors_liveDataNotNull() {
        assertThat(mReceiptViewModelToTest.getReceiptErrors(), is(notNullValue()));
    }

    @Test
    public void testGetReceiptList_liveDataNotNull() {
        assertThat(mReceiptViewModelToTest.getReceiptList(), is(notNullValue()));
    }

    @Test
    public void testGetDetailNavigation_liveDataIsNotNull() {
        assertThat(mReceiptViewModelToTest.getDetailNavigation(), is(notNullValue()));
    }
}
