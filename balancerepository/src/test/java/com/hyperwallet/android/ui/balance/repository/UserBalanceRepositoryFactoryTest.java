package com.hyperwallet.android.ui.balance.repository;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.notNullValue;

import org.junit.Test;

public class UserBalanceRepositoryFactoryTest {

    @Test
    public void testGetInstance_verifyRepositoriesInitialized() {
        UserBalanceRepositoryFactory userBalanceRepositoryFactory = UserBalanceRepositoryFactory.getInstance();
        assertThat(userBalanceRepositoryFactory, is(notNullValue()));
        assertThat(userBalanceRepositoryFactory.getUserBalanceRepository(), is(notNullValue()));
    }

    @Test
    public void testClearInstance_verifyRepositoriesCleared() {
        UserBalanceRepositoryFactory userBalanceRepositoryFactory = UserBalanceRepositoryFactory.getInstance();

        UserBalanceRepository userConfigurationRepository = userBalanceRepositoryFactory.getUserBalanceRepository();

        UserBalanceRepositoryFactory currentRepositoryFactory = UserBalanceRepositoryFactory.getInstance();
        assertThat(userBalanceRepositoryFactory, is(currentRepositoryFactory));

        UserBalanceRepositoryFactory.clearInstance();

        UserBalanceRepositoryFactory anotherRepositoryFactory = UserBalanceRepositoryFactory.getInstance();
        assertThat(userBalanceRepositoryFactory, is(not(anotherRepositoryFactory)));
        assertThat(userConfigurationRepository, is(not(anotherRepositoryFactory.getUserBalanceRepository())));
    }
}
