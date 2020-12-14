package com.hyperwallet.android.ui.transfermethod.repository;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class TransferMethodRepositoryFactoryTest {
    @Test
    public void testGetInstance_verifyRepositoriesInitialized() {
        TransferMethodRepositoryFactory repositoryFactory = TransferMethodRepositoryFactory.getInstance();
        assertThat(repositoryFactory, is(notNullValue()));
        assertThat(repositoryFactory.getTransferMethodRepository(), is(notNullValue()));
        assertThat(repositoryFactory.getTransferMethodConfigurationRepository(), is(notNullValue()));
        assertThat(repositoryFactory.getUpdateTransferMethodConfigurationRepository(), is(notNullValue()));
    }

    @Test
    public void testClearInstance_verifyRepositoryCleared() {
        TransferMethodRepositoryFactory repositoryFactory = TransferMethodRepositoryFactory.getInstance();

        TransferMethodRepository transferMethodRepository = repositoryFactory.getTransferMethodRepository();
        TransferMethodConfigurationRepository configurationRepository =
                repositoryFactory.getTransferMethodConfigurationRepository();
        TransferMethodUpdateConfigurationRepository transferMethodUpdateConfigurationRepository =
                repositoryFactory.getUpdateTransferMethodConfigurationRepository();
        TransferMethodRepositoryFactory currentRepositoryFactory = TransferMethodRepositoryFactory.getInstance();
        assertThat(repositoryFactory, is(currentRepositoryFactory));
        assertThat(transferMethodRepository, is(currentRepositoryFactory.getTransferMethodRepository()));
        assertThat(configurationRepository, is(currentRepositoryFactory.getTransferMethodConfigurationRepository()));
        assertThat(transferMethodUpdateConfigurationRepository,is(currentRepositoryFactory.getUpdateTransferMethodConfigurationRepository()));

        TransferMethodRepositoryFactory.clearInstance();

        TransferMethodRepositoryFactory anotherRepositoryFactory = TransferMethodRepositoryFactory.getInstance();
        assertThat(repositoryFactory, is(not(anotherRepositoryFactory)));
        assertThat(transferMethodRepository, is(not(anotherRepositoryFactory.getTransferMethodRepository())));
        assertThat(configurationRepository,
                is(not(anotherRepositoryFactory.getTransferMethodConfigurationRepository())));
        assertThat(transferMethodUpdateConfigurationRepository,is(not(anotherRepositoryFactory.getUpdateTransferMethodConfigurationRepository())));
    }
}