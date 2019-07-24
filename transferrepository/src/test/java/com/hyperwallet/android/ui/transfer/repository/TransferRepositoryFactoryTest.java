package com.hyperwallet.android.ui.transfer.repository;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class TransferRepositoryFactoryTest {

    @Test
    public void testGetInstance_verifyRepositoryInitialized() {
        TransferRepositoryFactory transferRepositoryFactory = TransferRepositoryFactory.getInstance();
        assertThat(transferRepositoryFactory, is(notNullValue()));
        assertThat(transferRepositoryFactory.getTransferRepository(), is(notNullValue()));
    }

    @Test
    public void testClearInstance_verifyRepositoryCleared() {
        TransferRepositoryFactory transferRepositoryFactory = TransferRepositoryFactory.getInstance();
        TransferRepository transferRepository = transferRepositoryFactory.getTransferRepository();

        TransferRepositoryFactory transferRepositoryFactorySame = TransferRepositoryFactory.getInstance();
        TransferRepository transferRepositorySame = transferRepositoryFactorySame.getTransferRepository();

        assertThat(transferRepositoryFactory, is(transferRepositoryFactory));
        assertThat(transferRepository, is(transferRepositorySame));

        TransferRepositoryFactory.clearInstance();

        transferRepositoryFactory = TransferRepositoryFactory.getInstance();
        transferRepository = transferRepositoryFactory.getTransferRepository();

        assertThat(transferRepositoryFactory, is(not(transferRepositoryFactorySame)));
        assertThat(transferRepository, is(not(transferRepositorySame)));
    }
}
