package com.hyperwallet.android.ui.receipt.repository;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertTrue;

import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;

import com.hyperwallet.android.model.Errors;
import com.hyperwallet.android.model.receipt.Receipt;
import com.hyperwallet.android.ui.common.repository.Event;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class UserReceiptRepositoryImplTest {


    private UserReceiptRepository mUserReceiptRepository = new UserReceiptRepositoryImpl();

    @Test
    public void testLoadUserReceipts_returnsLiveData() {
        LiveData<PagedList<Receipt>> result = mUserReceiptRepository.loadUserReceipts();
        assertThat(result, is(notNullValue()));
        LiveData<PagedList<Receipt>> result2 = mUserReceiptRepository.loadUserReceipts();
        assertTrue(result == result2);
    }

    @Test
    public void testLoadUserReceipts_liveDataSingleInstantiation() {
        LiveData<PagedList<Receipt>> result = mUserReceiptRepository.loadUserReceipts();
        LiveData<PagedList<Receipt>> result2 = mUserReceiptRepository.loadUserReceipts();
        assertTrue(result == result2);
    }

    @Test
    public void testIsLoading_returnsLiveData() {
        LiveData<Boolean> result = mUserReceiptRepository.isLoading();
        assertThat(result, is(notNullValue()));
    }

    @Test
    public void testIsLoading_liveDataSingleInstantiation() {
        LiveData<Boolean> result = mUserReceiptRepository.isLoading();
        LiveData<Boolean> result2 = mUserReceiptRepository.isLoading();
        assertTrue(result == result2);

    }

    @Test
    public void testGetErrors_returnsLiveData() {
        LiveData<Event<Errors>> result = mUserReceiptRepository.getErrors();
        assertThat(result, is(notNullValue()));
    }

    @Test
    public void testGetErrors_liveDataSingleInstantiation() {
        LiveData<Event<Errors>> result = mUserReceiptRepository.getErrors();
        LiveData<Event<Errors>> result2 = mUserReceiptRepository.getErrors();
        assertTrue(result == result2);
    }


}