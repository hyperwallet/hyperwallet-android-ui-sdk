package com.hyperwallet.android.ui.transfermethod.repository;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class RepositoryFactoryTest {

    @Test
    public void testGetInstance_verifyRepositoriesInitialized() {
        RepositoryFactory repositoryFactory = RepositoryFactory.getInstance();
        assertThat(repositoryFactory, is(notNullValue()));
        assertThat(repositoryFactory.getTransferMethodConfigurationRepository(), is(notNullValue()));
        assertThat(repositoryFactory.getTransferMethodRepository(), is(notNullValue()));
    }

    @Test
    public void testClearInstance_verifyRepositoriesCleared() {
        RepositoryFactory repositoryFactory = RepositoryFactory.getInstance();

        TransferMethodConfigurationRepository configurationRepository =
                repositoryFactory.getTransferMethodConfigurationRepository();
        TransferMethodRepository transferMethodRepository = repositoryFactory.getTransferMethodRepository();
        RepositoryFactory currentRepositoryFactory = RepositoryFactory.getInstance();
        assertThat(repositoryFactory, is(currentRepositoryFactory));
        assertThat(configurationRepository, is(currentRepositoryFactory.getTransferMethodConfigurationRepository()));
        assertThat(transferMethodRepository, is(currentRepositoryFactory.getTransferMethodRepository()));

        RepositoryFactory.clearInstance();

        RepositoryFactory anotherRepositoryFactory = RepositoryFactory.getInstance();
        assertThat(repositoryFactory, is(not(anotherRepositoryFactory)));
        assertThat(configurationRepository,
                is(not(anotherRepositoryFactory.getTransferMethodConfigurationRepository())));
        assertThat(transferMethodRepository, is(not(anotherRepositoryFactory.getTransferMethodRepository())));
    }
}