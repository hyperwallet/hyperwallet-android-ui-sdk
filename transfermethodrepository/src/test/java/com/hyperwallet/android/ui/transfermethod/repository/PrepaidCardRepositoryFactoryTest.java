package com.hyperwallet.android.ui.transfermethod.repository;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class PrepaidCardRepositoryFactoryTest {
    @Test
    public void testGetInstance_verifyRepositoriesInitialized() {
        PrepaidCardRepositoryFactory repositoryFactory = PrepaidCardRepositoryFactory.getInstance();
        assertThat(repositoryFactory, is(notNullValue()));
        assertThat(repositoryFactory.getPrepaidCardRepository(), is(notNullValue()));
    }

    @Test
    public void testClearInstance_verifyRepositoryCleared() {
        PrepaidCardRepositoryFactory repositoryFactory = PrepaidCardRepositoryFactory.getInstance();

        PrepaidCardRepository prepaidCardRepository = repositoryFactory.getPrepaidCardRepository();
        PrepaidCardRepositoryFactory currentRepositoryFactory = PrepaidCardRepositoryFactory.getInstance();
        assertThat(repositoryFactory, is(currentRepositoryFactory));
        assertThat(prepaidCardRepository, is(currentRepositoryFactory.getPrepaidCardRepository()));

        PrepaidCardRepositoryFactory.clearInstance();

        PrepaidCardRepositoryFactory anotherRepositoryFactory = PrepaidCardRepositoryFactory.getInstance();
        assertThat(repositoryFactory, is(not(anotherRepositoryFactory)));
        assertThat(prepaidCardRepository, is(not(anotherRepositoryFactory.getPrepaidCardRepository())));
    }
}