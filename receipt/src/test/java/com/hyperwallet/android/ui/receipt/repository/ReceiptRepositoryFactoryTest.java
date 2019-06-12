package com.hyperwallet.android.ui.receipt.repository;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ReceiptRepositoryFactoryTest {

    @Test
    public void testGetInstance_verifyRepositoriesInitialized() {
        // test
        ReceiptRepositoryFactory factory = ReceiptRepositoryFactory.getInstance();
        assertThat(factory, is(notNullValue()));
        assertThat(factory.getReceiptRepository(), is(notNullValue()));

        ReceiptRepositoryFactory factory2 = ReceiptRepositoryFactory.getInstance();
        assertThat(factory, is(factory2));
        assertThat(factory.getReceiptRepository(), is(factory2.getReceiptRepository()));
    }

    @Test
    public void testClearInstance_verifyRepositoriesCleared() {
        ReceiptRepositoryFactory factory = ReceiptRepositoryFactory.getInstance();
        assertThat(factory, is(notNullValue()));
        assertThat(factory.getReceiptRepository(), is(notNullValue()));

        // test clear
        ReceiptRepositoryFactory.clearInstance();
        ReceiptRepositoryFactory factory2 = ReceiptRepositoryFactory.getInstance();
        assertThat(factory, is(not(factory2)));
        assertThat(factory.getReceiptRepository(), is(not(factory2.getReceiptRepository())));
    }
}
