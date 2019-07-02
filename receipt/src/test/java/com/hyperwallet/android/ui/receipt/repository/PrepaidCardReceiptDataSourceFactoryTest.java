package com.hyperwallet.android.ui.receipt.repository;

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
public class PrepaidCardReceiptDataSourceFactoryTest {

    @Test
    public void testGetReceiptDataSource_returnsLiveDataReceiptSource() {
        // initialize
        PrepaidCardReceiptDataSourceFactory dataSourceFactory = new PrepaidCardReceiptDataSourceFactory("token");
        // test
        LiveData<PrepaidCardReceiptDataSource> liveData = dataSourceFactory.getPrepaidCardReceiptDataSource();
        // assert
        assertThat(liveData, is(notNullValue()));
    }

    @Test
    public void testCreate_returnsDataSource() {
        // initialize
        PrepaidCardReceiptDataSourceFactory dataSourceFactory = new PrepaidCardReceiptDataSourceFactory("token");
        // test
        DataSource dataSource = dataSourceFactory.create();
        // assert
        assertThat(dataSource, is(notNullValue()));
        assertThat(dataSource, CoreMatchers.<DataSource>instanceOf(PrepaidCardReceiptDataSource.class));
    }
}
