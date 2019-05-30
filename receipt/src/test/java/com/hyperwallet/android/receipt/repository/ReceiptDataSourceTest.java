package com.hyperwallet.android.receipt.repository;

import static org.mockito.Mockito.doReturn;

import com.hyperwallet.android.Hyperwallet;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ReceiptDataSourceTest {

    @Rule
    public MockitoRule mMockito = MockitoJUnit.rule();
    @Spy
    @InjectMocks
    private ReceiptDataSource mReceiptDataSource;
    @Mock
    private Hyperwallet mHyperwallet;

    @Before
    public void setUp() {
        doReturn(mHyperwallet).when(mReceiptDataSource).getHyperwallet();
    }

    @Test
    public void testLoadInitial_returnsReceipts() {
        // 1) create receipt list to return
        // 2) initialize paging
        // 3) mock hyperwallet behavior to return objects
        // 4) mock params
        // 5) mock callback

        // test

        // verify receipt list return
        // verify error is not invoked
        // verify mock params
        // verify mock callback
        // assert receipts information
    }

    @Test
    public void testLoadInitial_returnNoReceipt() {
        // 1) mock hyperwallet behavior to return null to simulate 204
        // 2) mock params
        // 3) mock callback

        // test

        // verify receipt list return
        // verify error is not invoked
        // verify mock params
        // verify mock callback
        // assert receipts information
    }

    @Test
    public void testLoadInitial_withError() {
        // 1) Initialize error list response
        // 2) mock params
        // 3) mock callback

        // test

        // verify receipt is not returned
        // verify error is invoked
        // verify mock params
        // verify mock callback
        // assert errors information
    }

    @Test
    public void testRetry_LoadInitial() {
        // 1) Initialize error list response
        // 2) mock params
        // 3) mock callback

        // test load initial
        // test retry

        // verify receipt is not returned
        // verify error is invoked twice
        // verify mock params twice
        // verify mock callback twice

        // assert errors information
    }

    @Test
    public void testLoadAfter_returnsReceipts() {
        // 1) create receipt list to return
        // 2) initialize paging
        // 3) mock hyperwallet behavior to return objects
        // 4) mock params
        // 5) mock callback

        // test

        // verify receipt list return
        // verify error is not invoked
        // verify mock params
        // verify mock callback
        // assert receipts information
    }

    @Test
    public void testLoadAfter_returnNoReceipt() {
        // 1) mock hyperwallet behavior to return null to simulate 204
        // 2) mock params
        // 3) mock callback

        // test

        // verify receipt list return
        // verify error is not invoked
        // verify mock params
        // verify mock callback
        // assert receipts information
    }

    @Test
    public void testLoadAfter_withError() {
        // 1) Initialize error list response
        // 2) mock params
        // 3) mock callback

        // test

        // verify receipt is not returned
        // verify error is invoked
        // verify mock params
        // verify mock callback
        // assert errors information
    }

    @Test
    public void testRetry_LoadAfter() {
        // 1) Initialize error list response
        // 2) mock params
        // 3) mock callback

        // test load initial
        // test retry

        // verify receipt is not returned
        // verify error is invoked twice
        // verify mock params twice
        // verify mock callback twice

        // assert errors information
    }
}
