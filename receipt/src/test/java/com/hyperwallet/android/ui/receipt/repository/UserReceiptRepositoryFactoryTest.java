package com.hyperwallet.android.ui.receipt.repository;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class UserReceiptRepositoryFactoryTest {

    @Test
    public void testGetInstance_verifyRepositoriesInitialized() {
        // test
        UserReceiptRepositoryFactory factory = UserReceiptRepositoryFactory.getInstance();
        assertThat(factory, is(notNullValue()));
        assertThat(factory.getUserReceiptRepository(), is(notNullValue()));

        UserReceiptRepositoryFactory factory2 = UserReceiptRepositoryFactory.getInstance();
        assertThat(factory, is(factory2));
        assertThat(factory.getUserReceiptRepository(), is(factory2.getUserReceiptRepository()));
    }

    @Test
    public void testClearInstance_verifyRepositoriesCleared() {
        UserReceiptRepositoryFactory factory = UserReceiptRepositoryFactory.getInstance();
        assertThat(factory, is(notNullValue()));
        assertThat(factory.getUserReceiptRepository(), is(notNullValue()));

        // test clear
        UserReceiptRepositoryFactory.clearInstance();
        UserReceiptRepositoryFactory factory2 = UserReceiptRepositoryFactory.getInstance();
        assertThat(factory, is(not(factory2)));
        assertThat(factory.getUserReceiptRepository(), is(not(factory2.getUserReceiptRepository())));
    }
}
