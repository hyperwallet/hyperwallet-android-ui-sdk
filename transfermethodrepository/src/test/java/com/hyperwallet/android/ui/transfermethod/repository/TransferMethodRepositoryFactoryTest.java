package com.hyperwallet.android.ui.transfermethod.repository;


import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class TransferMethodRepositoryFactoryTest {
    @Test
    public void testGetInstance_verifyRepositoryInitialized() {
        TransferMethodRepositoryFactory repositoryFactory = TransferMethodRepositoryFactory.getInstance();
        assertThat(repositoryFactory, is(notNullValue()));
        assertThat(repositoryFactory.getTransferMethodRepository(), is(notNullValue()));
    }

    @Test
    public void testClearInstance_verifyRepositoryCleared() {
        TransferMethodRepositoryFactory repositoryFactory = TransferMethodRepositoryFactory.getInstance();

        TransferMethodRepository transferMethodRepository = repositoryFactory.getTransferMethodRepository();
        TransferMethodRepositoryFactory currentRepositoryFactory = TransferMethodRepositoryFactory.getInstance();
        assertThat(repositoryFactory, is(currentRepositoryFactory));
        assertThat(transferMethodRepository, is(currentRepositoryFactory.getTransferMethodRepository()));

        TransferMethodRepositoryFactory.clearInstance();

        TransferMethodRepositoryFactory anotherRepositoryFactory = TransferMethodRepositoryFactory.getInstance();
        assertThat(repositoryFactory, is(not(anotherRepositoryFactory)));
        assertThat(transferMethodRepository, is(not(anotherRepositoryFactory.getTransferMethodRepository())));
    }
}