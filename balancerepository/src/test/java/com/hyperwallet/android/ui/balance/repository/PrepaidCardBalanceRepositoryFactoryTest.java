package com.hyperwallet.android.ui.balance.repository;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.Test;

public class PrepaidCardBalanceRepositoryFactoryTest {

    @Test
    public void testGetInstance_verifyRepositoriesInitialized() {
        PrepaidCardBalanceRepositoryFactory prepaidCardBalanceRepository =
                PrepaidCardBalanceRepositoryFactory.getInstance();
        assertThat(prepaidCardBalanceRepository, is(notNullValue()));
        assertThat(prepaidCardBalanceRepository.getPrepaidCardBalanceRepository(), is(notNullValue()));
    }

    @Test
    public void testClearInstance_verifyRepositoriesCleared() {
        PrepaidCardBalanceRepositoryFactory prepaidCardBalanceRepositoryFactory =
                PrepaidCardBalanceRepositoryFactory.getInstance();

        PrepaidCardBalanceRepository prepaidCardBalanceRepository =
                prepaidCardBalanceRepositoryFactory.getPrepaidCardBalanceRepository();

        PrepaidCardBalanceRepositoryFactory currentRepositoryFactory =
                PrepaidCardBalanceRepositoryFactory.getInstance();
        assertThat(prepaidCardBalanceRepositoryFactory, is(currentRepositoryFactory));

        PrepaidCardBalanceRepositoryFactory.clearInstance();

        PrepaidCardBalanceRepositoryFactory anotherRepositoryFactory =
                PrepaidCardBalanceRepositoryFactory.getInstance();
        assertThat(prepaidCardBalanceRepositoryFactory, is(not(anotherRepositoryFactory)));
        assertThat(prepaidCardBalanceRepository, is(not(anotherRepositoryFactory.getPrepaidCardBalanceRepository())));
    }
}
