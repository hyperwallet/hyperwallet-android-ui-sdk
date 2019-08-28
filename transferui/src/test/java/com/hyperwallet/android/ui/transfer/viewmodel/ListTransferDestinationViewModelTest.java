package com.hyperwallet.android.ui.transfer.viewmodel;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import static com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod.TransferMethodFields.TOKEN;
import static com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod.TransferMethodFields.TRANSFER_METHOD_COUNTRY;
import static com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod.TransferMethodFields.TRANSFER_METHOD_CURRENCY;

import androidx.lifecycle.ViewModel;

import com.google.common.collect.Lists;
import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.HyperwalletAuthenticationTokenProvider;
import com.hyperwallet.android.model.HyperwalletError;
import com.hyperwallet.android.model.HyperwalletErrors;
import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod;
import com.hyperwallet.android.ui.transfermethod.repository.TransferMethodRepository;
import com.hyperwallet.android.ui.transfermethod.repository.TransferMethodRepositoryFactory;
import com.hyperwallet.android.ui.transfermethod.repository.TransferMethodRepositoryImpl;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;

@RunWith(RobolectricTestRunner.class)
public class ListTransferDestinationViewModelTest {

    @Rule
    public final ExpectedException mThrown = ExpectedException.none();

    private ListTransferDestinationViewModel mModelToTest;
    private TransferMethodRepository transferMethodRepository;

    @Before
    public void initializedViewModel() {
        Hyperwallet.getInstance(mock(HyperwalletAuthenticationTokenProvider.class));
        transferMethodRepository = mock(TransferMethodRepositoryImpl.class);
        ListTransferDestinationViewModel.ListTransferDestinationViewModelFactory factory =
                new ListTransferDestinationViewModel.ListTransferDestinationViewModelFactory(transferMethodRepository);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodRepository.LoadTransferMethodListCallback callback = invocation.getArgument(0);
                callback.onTransferMethodListLoaded(new ArrayList<HyperwalletTransferMethod>());
                return callback;
            }
        }).when(transferMethodRepository).loadTransferMethods(ArgumentMatchers.any(
                TransferMethodRepository.LoadTransferMethodListCallback.class));

        mModelToTest = factory.create(ListTransferDestinationViewModel.class);
    }

    @Test
    public void testGetTransferDestinationList_returnsLiveData() {
        assertThat(mModelToTest.getTransferDestinationList(), is(notNullValue()));
        assertThat(mModelToTest.getTransferDestinationList().getValue(),
                Matchers.<HyperwalletTransferMethod>hasSize(0));
    }

    @Test
    public void testGetTransferDestinationList_returnsLiveDataNotFound() {
        ListTransferDestinationViewModel.ListTransferDestinationViewModelFactory factory =
                new ListTransferDestinationViewModel.ListTransferDestinationViewModelFactory(transferMethodRepository);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodRepository.LoadTransferMethodListCallback callback = invocation.getArgument(0);
                callback.onTransferMethodListLoaded(null);
                return callback;
            }
        }).when(transferMethodRepository).loadTransferMethods(ArgumentMatchers.any(
                TransferMethodRepository.LoadTransferMethodListCallback.class));

        ListTransferDestinationViewModel viewModel = factory.create(ListTransferDestinationViewModel.class);

        assertThat(viewModel.getTransferDestinationList(), is(notNullValue()));
        assertThat(viewModel.getTransferDestinationList().getValue(), Matchers.<HyperwalletTransferMethod>hasSize(0));
    }

    @Test
    public void testGetTransferDestinationSection_returnsLiveData() {
        assertThat(mModelToTest.getSelectedTransferDestination(), is(notNullValue()));
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
    public void testSelectTransferDestination_callsRepository() {
        HyperwalletTransferMethod transferMethod = new HyperwalletTransferMethod();
        mModelToTest.selectedTransferDestination(transferMethod);
        assertThat(mModelToTest.getSelectedTransferDestination().getValue().getContent(), is(transferMethod));
    }

    @Test
    public void testSelectTransferDestinationViewModelFactory_createSelectTransferDestinationViewModelUnSuccessful() {
        class DummyViewModel extends ViewModel {
        }

        ListTransferDestinationViewModel.ListTransferDestinationViewModelFactory factory =
                new ListTransferDestinationViewModel.ListTransferDestinationViewModelFactory(
                        TransferMethodRepositoryFactory.getInstance().getTransferMethodRepository());
        mThrown.expect(IllegalArgumentException.class);
        mThrown.expectMessage(
                "Expecting ViewModel class: com.hyperwallet.android.ui.transfer.viewmodel"
                        + ".ListTransferDestinationViewModel");
        factory.create(DummyViewModel.class);
    }

    @Test
    public void testSelectTransferDestinationViewModelFactory_createSelectTransferDestinationViewModelSuccessful() {
        ListTransferDestinationViewModel.ListTransferDestinationViewModelFactory factory =
                new ListTransferDestinationViewModel.ListTransferDestinationViewModelFactory(
                        TransferMethodRepositoryFactory.getInstance().getTransferMethodRepository());
        ListTransferDestinationViewModel viewModel = factory.create(ListTransferDestinationViewModel.class);
        assertThat(viewModel, is(notNullValue()));
    }

    @Test
    public void testLoadNewlyAddedTransferDestination_loadsNewlyAddedTransferDestinationAndSetItAsSelected() {
        final HyperwalletTransferMethod transferMethod = new HyperwalletTransferMethod();
        transferMethod.setField(TOKEN, "trm-bank-test-token");
        transferMethod.setField(TRANSFER_METHOD_CURRENCY, "CAD");
        transferMethod.setField(TRANSFER_METHOD_COUNTRY, "CA");

        TransferMethodRepository repository = mock(TransferMethodRepositoryImpl.class);
        ListTransferDestinationViewModel.ListTransferDestinationViewModelFactory factory =
                new ListTransferDestinationViewModel.ListTransferDestinationViewModelFactory(repository);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodRepository.LoadTransferMethodListCallback callback = invocation.getArgument(0);
                callback.onTransferMethodListLoaded(Lists.newArrayList(transferMethod));
                return callback;
            }
        }).when(repository).loadTransferMethods(ArgumentMatchers.any(
                TransferMethodRepository.LoadTransferMethodListCallback.class));

        ListTransferDestinationViewModel viewModel = factory.create(ListTransferDestinationViewModel.class);

        // no selection
        assertThat(viewModel.getSelectedTransferDestination().getValue(), is(nullValue()));

        // test
        viewModel.loadNewlyAddedTransferDestination();

        assertThat(viewModel.getSelectedTransferDestination().getValue(), is(notNullValue()));
        assertThat(viewModel.getSelectedTransferDestination().getValue().getContent().getField(TOKEN),
                is("trm-bank-test-token"));
        assertThat(
                viewModel.getSelectedTransferDestination().getValue().getContent().getField(TRANSFER_METHOD_CURRENCY),
                is("CAD"));
        assertThat(viewModel.getSelectedTransferDestination().getValue().getContent().getField(TRANSFER_METHOD_COUNTRY),
                is("CA"));

        assertThat(viewModel.getTransferDestinationList().getValue(), Matchers.<HyperwalletTransferMethod>hasSize(1));
    }

    @Test
    public void testLoadNewlyAddedTransferDestination_loadsEmptyTransferDestination() {
        TransferMethodRepository repository = mock(TransferMethodRepositoryImpl.class);
        ListTransferDestinationViewModel.ListTransferDestinationViewModelFactory factory =
                new ListTransferDestinationViewModel.ListTransferDestinationViewModelFactory(repository);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodRepository.LoadTransferMethodListCallback callback = invocation.getArgument(0);
                callback.onTransferMethodListLoaded(new ArrayList<HyperwalletTransferMethod>());
                return callback;
            }
        }).when(repository).loadTransferMethods(ArgumentMatchers.any(
                TransferMethodRepository.LoadTransferMethodListCallback.class));

        ListTransferDestinationViewModel viewModel = factory.create(ListTransferDestinationViewModel.class);

        // no selection
        assertThat(viewModel.getSelectedTransferDestination().getValue(), is(nullValue()));

        // test
        viewModel.loadNewlyAddedTransferDestination();

        // no selection
        assertThat(viewModel.getSelectedTransferDestination().getValue(), is(nullValue()));
        assertThat(viewModel.getTransferDestinationList().getValue(), Matchers.<HyperwalletTransferMethod>hasSize(0));
    }

    @Test
    public void testLoadNewlyAddedTransferDestination_errorOnLoadingTransferDestinationList() {

        TransferMethodRepository repository = mock(TransferMethodRepositoryImpl.class);
        ListTransferDestinationViewModel.ListTransferDestinationViewModelFactory factory =
                new ListTransferDestinationViewModel.ListTransferDestinationViewModelFactory(repository);

        doAnswer(new Answer() {
            short execution = 0;

            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodRepository.LoadTransferMethodListCallback callback = invocation.getArgument(0);
                if (execution++ == 1) {
                    HyperwalletError error = new HyperwalletError("test error", "TEST_ERROR_CODE");
                    HyperwalletErrors errors = new HyperwalletErrors(Lists.newArrayList(error));
                    callback.onError(errors);
                    return callback;
                }
                callback.onTransferMethodListLoaded(new ArrayList<HyperwalletTransferMethod>());
                return callback;
            }
        }).when(repository).loadTransferMethods(ArgumentMatchers.any(
                TransferMethodRepository.LoadTransferMethodListCallback.class));

        // during creation of view model `loadTransferDestinationList` is called first
        ListTransferDestinationViewModel viewModel = factory.create(ListTransferDestinationViewModel.class);
        // no selection
        assertThat(viewModel.getSelectedTransferDestination().getValue(), is(nullValue()));
        // no error
        assertThat(viewModel.getTransferDestinationError().getValue(), is(nullValue()));

        // test
        viewModel.loadNewlyAddedTransferDestination();

        assertThat(viewModel.getTransferDestinationError().getValue(), is(notNullValue()));
        assertThat(viewModel.getTransferDestinationError().getValue().getContent().getErrors(),
                Matchers.<HyperwalletError>hasSize(1));
        assertThat(viewModel.getTransferDestinationError().getValue().getContent().getErrors().get(0).getCode(),
                is("TEST_ERROR_CODE"));
        assertThat(viewModel.getTransferDestinationError().getValue().getContent().getErrors().get(0).getMessage(),
                is("test error"));
        assertThat(viewModel.getSelectedTransferDestination().getValue(), is(nullValue()));
        assertThat(viewModel.getTransferDestinationList().getValue(), Matchers.<HyperwalletTransferMethod>hasSize(0));
        assertThat(viewModel.isLoading().getValue(), is(false));
    }

    @Test
    public void testLoadTransferDestinationList_errorOnLoadingTransferDestinationList() {
        TransferMethodRepository repository = mock(TransferMethodRepositoryImpl.class);
        ListTransferDestinationViewModel.ListTransferDestinationViewModelFactory factory =
                new ListTransferDestinationViewModel.ListTransferDestinationViewModelFactory(repository);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodRepository.LoadTransferMethodListCallback callback = invocation.getArgument(0);
                HyperwalletError error = new HyperwalletError("test error", "TEST_ERROR_CODE");
                HyperwalletErrors errors = new HyperwalletErrors(Lists.newArrayList(error));
                callback.onError(errors);
                return callback;
            }
        }).when(repository).loadTransferMethods(ArgumentMatchers.any(
                TransferMethodRepository.LoadTransferMethodListCallback.class));

        ListTransferDestinationViewModel viewModel = factory.create(ListTransferDestinationViewModel.class);

        assertThat(viewModel.getSelectedTransferDestination().getValue(), is(nullValue()));
        assertThat(viewModel.getTransferDestinationList().getValue(), is(nullValue()));
        assertThat(viewModel.isLoading().getValue(), is(false));
    }
}
