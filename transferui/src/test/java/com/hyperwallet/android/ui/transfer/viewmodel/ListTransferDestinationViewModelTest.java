package com.hyperwallet.android.ui.transfer.viewmodel;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.TOKEN;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.TRANSFER_METHOD_COUNTRY;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.TRANSFER_METHOD_CURRENCY;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.ViewModel;

import com.google.common.collect.Lists;
import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.HyperwalletAuthenticationTokenProvider;
import com.hyperwallet.android.exception.HyperwalletException;
import com.hyperwallet.android.model.Error;
import com.hyperwallet.android.model.Errors;
import com.hyperwallet.android.model.paging.PageList;
import com.hyperwallet.android.model.transfermethod.PrepaidCard;
import com.hyperwallet.android.model.transfermethod.TransferMethod;
import com.hyperwallet.android.ui.testutils.rule.HyperwalletExternalResourceManager;
import com.hyperwallet.android.ui.transfermethod.repository.TransferMethodRepository;
import com.hyperwallet.android.ui.transfermethod.repository.TransferMethodRepositoryFactory;
import com.hyperwallet.android.ui.transfermethod.repository.TransferMethodRepositoryImpl;

import org.hamcrest.Matchers;
import org.json.JSONException;
import org.json.JSONObject;
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
    @Rule
    public InstantTaskExecutorRule mInstantTaskExecutor= new InstantTaskExecutorRule();
    private ListTransferDestinationViewModel mListTransferDestinationViewModel;
    private TransferMethodRepository mTransferMethodRepository;

    @Rule
    public HyperwalletExternalResourceManager mResourceManager = new HyperwalletExternalResourceManager();

    @Before
    public void initializedViewModel() throws JSONException, HyperwalletException {
        Hyperwallet.getInstance(mock(HyperwalletAuthenticationTokenProvider.class));
        mTransferMethodRepository = mock(TransferMethodRepositoryImpl.class);
        ListTransferDestinationViewModel.ListTransferDestinationViewModelFactory factory =
                new ListTransferDestinationViewModel.ListTransferDestinationViewModelFactory(mTransferMethodRepository);

        String responseJson = mResourceManager.getResourceContent("transfer_method_list_response.json");
        JSONObject jsonObject = new JSONObject(responseJson);
        final PageList<TransferMethod> transferList = new PageList<>(jsonObject, TransferMethod.class);
        transferList.getDataList();

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodRepository.LoadTransferMethodListCallback callback = invocation.getArgument(0);
                callback.onTransferMethodListLoaded(transferList.getDataList());
                return callback;
            }
        }).when(mTransferMethodRepository).loadTransferMethods(ArgumentMatchers.any(
                TransferMethodRepository.LoadTransferMethodListCallback.class));

        mListTransferDestinationViewModel = spy(factory.create(ListTransferDestinationViewModel.class));
        mListTransferDestinationViewModel.setIsSourcePrepaidCard(true);
    }

    @Test
    public void testGetTransferDestinationList_returnsLiveData() {
        mListTransferDestinationViewModel.init();
        assertThat(mListTransferDestinationViewModel.getTransferDestinationList(), is(notNullValue()));
        assertThat(mListTransferDestinationViewModel.getTransferDestinationList().getValue(),
                Matchers.<TransferMethod>hasSize(5));
    }

    @Test
    public void testGetTransferDestinationList_returnsLiveDataNotFound() {
        ListTransferDestinationViewModel.ListTransferDestinationViewModelFactory factory =
                new ListTransferDestinationViewModel.ListTransferDestinationViewModelFactory(mTransferMethodRepository);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodRepository.LoadTransferMethodListCallback callback = invocation.getArgument(0);
                callback.onTransferMethodListLoaded(null);
                return callback;
            }
        }).when(mTransferMethodRepository).loadTransferMethods(ArgumentMatchers.any(
                TransferMethodRepository.LoadTransferMethodListCallback.class));

        ListTransferDestinationViewModel viewModel = factory.create(ListTransferDestinationViewModel.class);
        viewModel.init();

        assertThat(viewModel.getTransferDestinationList(), is(notNullValue()));
        assertThat(viewModel.getTransferDestinationList().getValue(), Matchers.<TransferMethod>hasSize(0));
    }

    @Test
    public void testGetTransferDestinationSection_returnsLiveData() {
        assertThat(mListTransferDestinationViewModel.getSelectedTransferDestination(), is(notNullValue()));
    }

    @Test
    public void testGetTransferDestinationError_returnsLiveData() {
        assertThat(mListTransferDestinationViewModel.getTransferDestinationError(), is(notNullValue()));
    }

    @Test
    public void testSelectTransferDestination_returnsTransferMethod() {
        TransferMethod transferMethod = new TransferMethod();
        mListTransferDestinationViewModel.selectedTransferDestination(transferMethod);
        assertThat(mListTransferDestinationViewModel.getSelectedTransferDestination().getValue().getContent(),
                is(transferMethod));
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
        final TransferMethod transferMethod = new TransferMethod();
        transferMethod.setField(TOKEN, "trm-bank-test-token");
        transferMethod.setField(TRANSFER_METHOD_CURRENCY, "CAD");
        transferMethod.setField(TRANSFER_METHOD_COUNTRY, "CA");

        ListTransferDestinationViewModel.ListTransferDestinationViewModelFactory factory =
                new ListTransferDestinationViewModel.ListTransferDestinationViewModelFactory(mTransferMethodRepository);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodRepository.LoadTransferMethodListCallback callback = invocation.getArgument(0);
                callback.onTransferMethodListLoaded(Lists.newArrayList(transferMethod));
                return callback;
            }
        }).when(mTransferMethodRepository).loadTransferMethods(ArgumentMatchers.any(
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

        assertThat(viewModel.getTransferDestinationList().getValue(), Matchers.<TransferMethod>hasSize(1));
    }

    @Test
    public void testLoadNewlyAddedTransferDestination_loadsEmptyTransferDestination() {
        ListTransferDestinationViewModel.ListTransferDestinationViewModelFactory factory =
                new ListTransferDestinationViewModel.ListTransferDestinationViewModelFactory(mTransferMethodRepository);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodRepository.LoadTransferMethodListCallback callback = invocation.getArgument(0);
                callback.onTransferMethodListLoaded(new ArrayList<TransferMethod>());
                return callback;
            }
        }).when(mTransferMethodRepository).loadTransferMethods(ArgumentMatchers.any(
                TransferMethodRepository.LoadTransferMethodListCallback.class));

        ListTransferDestinationViewModel viewModel = factory.create(ListTransferDestinationViewModel.class);

        // no selection
        assertThat(viewModel.getSelectedTransferDestination().getValue(), is(nullValue()));

        // test
        viewModel.loadNewlyAddedTransferDestination();

        // no selection
        assertThat(viewModel.getSelectedTransferDestination().getValue(), is(nullValue()));
        assertThat(viewModel.getTransferDestinationList().getValue(), Matchers.<TransferMethod>hasSize(0));
    }

    @Test
    public void testLoadNewlyAddedTransferDestination_errorOnLoadingTransferDestinationList() {

        ListTransferDestinationViewModel.ListTransferDestinationViewModelFactory factory =
                new ListTransferDestinationViewModel.ListTransferDestinationViewModelFactory(mTransferMethodRepository);

        doAnswer(new Answer() {
            short execution = 0;

            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodRepository.LoadTransferMethodListCallback callback = invocation.getArgument(0);
                if (execution++ == 1) {
                    Error error = new Error("test error", "TEST_ERROR_CODE");
                    Errors errors = new Errors(Lists.newArrayList(error));
                    callback.onError(errors);
                    return callback;
                }
                callback.onTransferMethodListLoaded(new ArrayList<TransferMethod>());
                return callback;
            }
        }).when(mTransferMethodRepository).loadTransferMethods(ArgumentMatchers.any(
                TransferMethodRepository.LoadTransferMethodListCallback.class));

        // during creation of view model `loadTransferDestinationList` is called first
        ListTransferDestinationViewModel viewModel = factory.create(ListTransferDestinationViewModel.class);
        viewModel.init();
        // no selection
        assertThat(viewModel.getSelectedTransferDestination().getValue(), is(nullValue()));
        // no error
        assertThat(viewModel.getTransferDestinationError().getValue(), is(nullValue()));

        // test
        viewModel.loadNewlyAddedTransferDestination();

        assertThat(viewModel.getTransferDestinationError().getValue(), is(notNullValue()));
        assertThat(viewModel.getTransferDestinationError().getValue().getContent().getErrors(),
                Matchers.<Error>hasSize(1));
        assertThat(viewModel.getTransferDestinationError().getValue().getContent().getErrors().get(0).getCode(),
                is("TEST_ERROR_CODE"));
        assertThat(viewModel.getTransferDestinationError().getValue().getContent().getErrors().get(0).getMessage(),
                is("test error"));
        assertThat(viewModel.getSelectedTransferDestination().getValue(), is(nullValue()));
        assertThat(viewModel.getTransferDestinationList().getValue(), Matchers.<TransferMethod>hasSize(0));
        assertThat(viewModel.isLoading().getValue(), is(false));
    }

    @Test
    public void testLoadTransferDestinationList_errorOnLoadingTransferDestinationList() {
        ListTransferDestinationViewModel.ListTransferDestinationViewModelFactory factory =
                new ListTransferDestinationViewModel.ListTransferDestinationViewModelFactory(mTransferMethodRepository);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodRepository.LoadTransferMethodListCallback callback = invocation.getArgument(0);
                Error error = new Error("test error", "TEST_ERROR_CODE");
                Errors errors = new Errors(Lists.newArrayList(error));
                callback.onError(errors);
                return callback;
            }
        }).when(mTransferMethodRepository).loadTransferMethods(ArgumentMatchers.any(
                TransferMethodRepository.LoadTransferMethodListCallback.class));

        ListTransferDestinationViewModel viewModel = factory.create(ListTransferDestinationViewModel.class);
        viewModel.init();

        assertThat(viewModel.getSelectedTransferDestination().getValue(), is(nullValue()));
        assertThat(viewModel.getTransferDestinationList().getValue(), is(nullValue()));
        assertThat(viewModel.isLoading().getValue(), is(false));
    }


    @Test
    public void testInit_verifyInitializedOnce() {
        mListTransferDestinationViewModel.init();
        verify(mListTransferDestinationViewModel).loadTransferDestinationList();

        // call again. multiple calls to init should only register 1 call to repository
        mListTransferDestinationViewModel.init();
        verify(mListTransferDestinationViewModel).loadTransferDestinationList();

        verify(mTransferMethodRepository).loadTransferMethods(
                any(TransferMethodRepository.LoadTransferMethodListCallback.class));
    }
}
