package com.hyperwallet.android.ui.user.repository;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class UserRepositoryFactoryTest {

    @Test
    public void testGetInstance_verifyRepositoriesInitialized() {
        UserRepositoryFactory userRepositoryFactory = UserRepositoryFactory.getInstance();
        assertThat(userRepositoryFactory, is(notNullValue()));
        assertThat(userRepositoryFactory.getUserRepository(), is(notNullValue()));
    }

    @Test
    public void testClearInstance_verifyRepositoriesCleared() {
        UserRepositoryFactory userRepositoryFactory = UserRepositoryFactory.getInstance();

        UserRepository userConfigurationRepository = userRepositoryFactory.getUserRepository();

        UserRepositoryFactory currentRepositoryFactory = UserRepositoryFactory.getInstance();
        assertThat(userRepositoryFactory, is(currentRepositoryFactory));

        UserRepositoryFactory.clearInstance();

        UserRepositoryFactory anotherRepositoryFactory = UserRepositoryFactory.getInstance();
        assertThat(userRepositoryFactory, is(not(anotherRepositoryFactory)));
        assertThat(userConfigurationRepository, is(not(anotherRepositoryFactory.getUserRepository())));
    }
}
