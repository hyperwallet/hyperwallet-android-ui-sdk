package com.hyperwallet.android.receipt.repository;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ReceiptDataSourceFactoryTest {

    @Test
    public void testGetReceiptDataSource_returnsLiveDataReceiptSource() {
        // initialize
        ReceiptDataSourceFactory dataSourceFactory = new ReceiptDataSourceFactory();
        // test
        LiveData<ReceiptDataSource> liveData = dataSourceFactory.getReceiptDataSource();
        // assert
        assertThat(liveData, is(notNullValue()));
    }

    @Test
    public void testCreate_returnsDataSource() {
        // initialize
        ReceiptDataSourceFactory dataSourceFactory = new ReceiptDataSourceFactory();
        // test
        DataSource dataSource = dataSourceFactory.create();
        // assert
        assertThat(dataSource, is(notNullValue()));
        assertThat(dataSource, CoreMatchers.<DataSource>instanceOf(ReceiptDataSource.class));
    }
}
