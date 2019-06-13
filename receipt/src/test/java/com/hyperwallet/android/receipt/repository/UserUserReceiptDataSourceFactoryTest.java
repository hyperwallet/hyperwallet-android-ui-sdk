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
public class UserUserReceiptDataSourceFactoryTest {

    @Test
    public void testGetReceiptDataSource_returnsLiveDataReceiptSource() {
        // initialize
        UserReceiptDataSourceFactory dataSourceFactory = new UserReceiptDataSourceFactory();
        // test
        LiveData<UserReceiptDataSource> liveData = dataSourceFactory.getReceiptDataSource();
        // assert
        assertThat(liveData, is(notNullValue()));
    }

    @Test
    public void testCreate_returnsDataSource() {
        // initialize
        UserReceiptDataSourceFactory dataSourceFactory = new UserReceiptDataSourceFactory();
        // test
        DataSource dataSource = dataSourceFactory.create();
        // assert
        assertThat(dataSource, is(notNullValue()));
        assertThat(dataSource, CoreMatchers.<DataSource>instanceOf(UserReceiptDataSource.class));
    }
}
