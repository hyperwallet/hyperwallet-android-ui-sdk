package com.hyperwallet.android.ui.receipt.repository;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;

import com.hyperwallet.android.model.Errors;
import com.hyperwallet.android.model.receipt.Receipt;
import com.hyperwallet.android.ui.common.repository.Event;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class PrepaidCardReceiptRepositoryImplTest {


    private PrepaidCardReceiptRepository mPrepaidCardReceiptRepository = new PrepaidCardReceiptRepositoryImpl(
            "test-fake-token");

    @Test
    public void testLoadPrepaidCardReceipts_returnsLiveData() {
        LiveData<PagedList<Receipt>> result = mPrepaidCardReceiptRepository.loadPrepaidCardReceipts();
        assertThat(result, is(notNullValue()));
        LiveData<PagedList<Receipt>> result2 = mPrepaidCardReceiptRepository.loadPrepaidCardReceipts();
        assertSame(result, result2);
    }

    @Test
    public void testLoadPrepaidCardReceipts_liveDataSingleInstantiation() {
        LiveData<PagedList<Receipt>> result = mPrepaidCardReceiptRepository.loadPrepaidCardReceipts();
        LiveData<PagedList<Receipt>> result2 = mPrepaidCardReceiptRepository.loadPrepaidCardReceipts();
        assertSame(result, result2);
    }

    @Test
    public void testIsLoading_returnsLiveData() {
        LiveData<Boolean> result = mPrepaidCardReceiptRepository.isLoading();
        assertThat(result, is(notNullValue()));
    }

    @Test
    public void testIsLoading_liveDataInstanceSwap() {
        LiveData<Boolean> result = mPrepaidCardReceiptRepository.isLoading();
        LiveData<Boolean> result2 = mPrepaidCardReceiptRepository.isLoading();
        assertNotSame(result, result2);

    }

    @Test
    public void testGetErrors_returnsLiveData() {
        LiveData<Event<Errors>> result = mPrepaidCardReceiptRepository.getErrors();
        assertThat(result, is(notNullValue()));
    }

    @Test
    public void testGetErrors_liveDataInstanceSwap() {
        LiveData<Event<Errors>> result = mPrepaidCardReceiptRepository.getErrors();
        LiveData<Event<Errors>> result2 = mPrepaidCardReceiptRepository.getErrors();
        assertNotSame(result, result2);
    }
}