package com.hyperwallet.android.ui.transfer.viewmodel;


import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod;
import com.hyperwallet.android.ui.transfermethod.repository.TransferMethodRepository;
import com.hyperwallet.android.ui.transfermethod.repository.TransferMethodRepositoryImpl;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ListTransferDestinationViewModelTest {

    private ListTransferDestinationViewModel mModelToTest;
    private TransferMethodRepository transferMethodRepository;
    @Rule
    public final ExpectedException mThrown = ExpectedException.none();

    @Before
    public void initializedViewModel() {
        transferMethodRepository = mock(TransferMethodRepositoryImpl.class);
        ListTransferDestinationViewModel.ListTransferDestinationViewModelFactory factory =
                new ListTransferDestinationViewModel.ListTransferDestinationViewModelFactory(transferMethodRepository);

        mModelToTest = factory.create(ListTransferDestinationViewModel.class);
    }

    @Test
    public void testGetTransferDestinationList_returnsLiveData() {
        assertThat(mModelToTest.getTransferDestinationList(), is(notNullValue()));
    }

    @Test
    public void testGetTransferDestinationSection_returnsLiveData() {
        assertThat(mModelToTest.getTransferDestinationSection(), is(notNullValue()));
    }

    @Test
    public void testGetTransferDestinationError_returnsLiveData() {
        assertThat(mModelToTest.getTransferDestinationError(), is(notNullValue()));
    }

    @Test
    public void testInitialization_callsRepository() {
        verify(transferMethodRepository).loadTransferMethods(
                any(TransferMethodRepository.LoadTransferMethodListCallback.class));
    }

    @Test
    public void testSelectTransferDestination_callRepository() {
        HyperwalletTransferMethod transferMethod = new HyperwalletTransferMethod();
        mModelToTest.selectTransferDestination(transferMethod);
        assertThat(mModelToTest.getTransferDestinationSection().getValue().getContent(), is(transferMethod));
    }
}