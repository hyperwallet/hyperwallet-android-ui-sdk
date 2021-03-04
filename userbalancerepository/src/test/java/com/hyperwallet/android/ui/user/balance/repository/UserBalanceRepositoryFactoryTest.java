package com.hyperwallet.android.ui.user.balance.repository;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

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
