package com.hyperwallet.android.ui.transfer.viewmodel;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;

import static java.net.HttpURLConnection.HTTP_OK;

import static com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod.TransferMethodFields.TOKEN;
import static com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod.TransferMethodFields.TRANSFER_METHOD_CURRENCY;

import android.content.Intent;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProviders;

import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.model.HyperwalletError;
import com.hyperwallet.android.model.HyperwalletErrors;
import com.hyperwallet.android.model.TypeReference;
import com.hyperwallet.android.model.transfer.Transfer;
import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod;
import com.hyperwallet.android.model.user.HyperwalletUser;
import com.hyperwallet.android.ui.transfer.repository.TransferRepository;
import com.hyperwallet.android.ui.transfer.repository.TransferRepositoryFactory;
import com.hyperwallet.android.ui.transfer.rule.HyperwalletExternalResourceManager;
import com.hyperwallet.android.ui.transfer.rule.HyperwalletMockWebServer;
import com.hyperwallet.android.ui.transfer.util.TestAuthenticationProvider;
import com.hyperwallet.android.ui.transfer.view.CreateTransferActivity;
import com.hyperwallet.android.ui.transfermethod.repository.TransferMethodRepository;
import com.hyperwallet.android.ui.transfermethod.repository.TransferMethodRepositoryFactory;
import com.hyperwallet.android.ui.user.repository.UserRepository;
import com.hyperwallet.android.ui.user.repository.UserRepositoryFactory;
import com.hyperwallet.android.util.JsonUtils;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.stubbing.Answer;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.util.Date;

@RunWith(RobolectricTestRunner.class)
public class CreateTransferViewModelTest {

    @Rule
    public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule
    public HyperwalletExternalResourceManager mResourceManager = new HyperwalletExternalResourceManager();
    @Rule
    public HyperwalletMockWebServer mMockWebServer = new HyperwalletMockWebServer(8080);
    @Rule
    public ExpectedException mExpectedException = ExpectedException.none();

    @Mock
    private TransferMethodRepository mTransferMethodRepository;
    @Mock
    private UserRepository mUserRepository;
    @Mock
    private TransferRepository mTransferRepository;

    private Transfer mTransfer;

    @Before
    public void setup() {
        Hyperwallet.getInstance(new TestAuthenticationProvider());
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(mResourceManager
                .getResourceContent("authentication_token_response.json")).mock();

        mTransfer = new Transfer.Builder()
                .token("trf-transfer-token")
                .status(Transfer.TransferStatuses.QUOTED)
                .createdOn(new Date())
                .clientTransferID("ClientId1234122")
                .sourceToken("usr-source-token")
                .sourceCurrency("CAD")
                .destinationToken("trm-bank-token")
                .destinationAmount("123.23")
                .destinationCurrency("CAD")
                .memo("Create quote test notes")
                .build();

        final HyperwalletTransferMethod transferMethod = new HyperwalletTransferMethod();
        transferMethod.setField(TOKEN, "trm-bank-token");
        transferMethod.setField(TRANSFER_METHOD_CURRENCY, "CAD");

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferRepository.CreateTransferCallback callback = invocation.getArgument(1);
                callback.onTransferCreated(mTransfer);
                return callback;
            }
        }).when(mTransferRepository).createTransfer(any(Transfer.class),
                any(TransferRepository.CreateTransferCallback.class));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletUser user = new HyperwalletUser.Builder()
                        .token("usr-token-source")
                        .build();
                UserRepository.LoadUserCallback callback = invocation.getArgument(0);
                callback.onUserLoaded(user);
                return callback;
            }
        }).when(mUserRepository).loadUser(any(UserRepository.LoadUserCallback.class));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodRepository.LoadTransferMethodCallback callback = invocation.getArgument(0);
                callback.onTransferMethodLoaded(transferMethod);
                return callback;
            }
        }).when(mTransferMethodRepository).loadLatestTransferMethod(any(TransferMethodRepository
                .LoadTransferMethodCallback.class));
    }

    @Test
    public void testCreateTransferViewModel_initializeWithFundingSource() {
        String ppcToken = "trm-ppc-token";
        Intent intent = new Intent();
        intent.putExtra(CreateTransferActivity.EXTRA_TRANSFER_SOURCE_TOKEN, ppcToken);

        CreateTransferActivity activity = Robolectric.buildActivity(CreateTransferActivity.class, intent).setup().get();
        CreateTransferViewModel model = spy(ViewModelProviders.of(activity).get(CreateTransferViewModel.class));

        assertThat(model.isTransferAllAvailableFunds(), is(notNullValue()));
        assertThat(model.isTransferAllAvailableFunds().getValue(), is(false));
        assertThat(model.isCreateQuoteLoading().getValue(), is(false));
    }

    @Test
    public void testCreateTransferViewModel_initializeWithoutFundingSource() {
        CreateTransferActivity activity = Robolectric.buildActivity(CreateTransferActivity.class).setup().get();
        CreateTransferViewModel viewModel = spy(ViewModelProviders.of(activity).get(CreateTransferViewModel.class));

        assertThat(viewModel.isTransferAllAvailableFunds(), is(notNullValue()));
        assertThat(viewModel.isTransferAllAvailableFunds().getValue(), is(false));
        assertThat(viewModel.isCreateQuoteLoading().getValue(), is(false));
    }

    @Test
    public void testSetTransferAllAvailableFunds_verifyLiveDataIsUpdated() {
        CreateTransferActivity activity = Robolectric.buildActivity(CreateTransferActivity.class).setup().get();
        CreateTransferViewModel viewModel = spy(ViewModelProviders.of(activity).get(CreateTransferViewModel.class));

        // default is false
        assertThat(viewModel.isTransferAllAvailableFunds().getValue(), is(false));

        viewModel.setTransferAllAvailableFunds(true);
        assertThat(viewModel.isTransferAllAvailableFunds().getValue(), is(true));
    }

    @Test
    public void testTransferAmount_verifyUpdatedTransferAmount() {
        CreateTransferActivity activity = Robolectric.buildActivity(CreateTransferActivity.class).setup().get();
        CreateTransferViewModel viewModel = spy(ViewModelProviders.of(activity).get(CreateTransferViewModel.class));

        // default amount
        assertThat(viewModel.getTransferAmount().getValue(), is(nullValue()));

        viewModel.setTransferAmount("20.22");
        assertThat(viewModel.getTransferAmount().getValue(), is("20.22"));
    }

    @Test
    public void testTransferNotes_verifyUpdatedTransferNotes() {
        CreateTransferActivity activity = Robolectric.buildActivity(CreateTransferActivity.class).setup().get();
        CreateTransferViewModel viewModel = spy(ViewModelProviders.of(activity).get(CreateTransferViewModel.class));

        // default notes
        assertThat(viewModel.getTransferNotes().getValue(), is(nullValue()));

        viewModel.setTransferNotes("Test transfer note");
        assertThat(viewModel.getTransferNotes().getValue(), is("Test transfer note"));
    }

    @Test
    public void testCreateQuoteTransfer_Successful() {
        CreateTransferViewModel.CreateTransferViewModelFactory factory =
                new CreateTransferViewModel.CreateTransferViewModelFactory(mTransferRepository,
                        mTransferMethodRepository, mUserRepository);

        CreateTransferViewModel viewModel = factory.create(CreateTransferViewModel.class);

        viewModel.setTransferNotes("Create quote test notes");
        viewModel.setTransferAmount("123.23");

        // test
        viewModel.createQuoteTransfer();

        assertThat(viewModel.getQuoteErrors().getValue(), is(nullValue()));
        assertThat(viewModel.getQuoteTransfer().getValue(), is(notNullValue()));
        assertThat(viewModel.getQuoteTransfer().getValue().getSourceToken(), is(mTransfer.getSourceToken()));
        assertThat(viewModel.getQuoteTransfer().getValue().getToken(), is(mTransfer.getToken()));
        assertThat(viewModel.getQuoteTransfer().getValue().getCreatedOn(), is(mTransfer.getCreatedOn()));
        assertThat(viewModel.getQuoteTransfer().getValue().getClientTransferId(), is(mTransfer.getClientTransferId()));
        assertThat(viewModel.getQuoteTransfer().getValue().getSourceCurrency(), is(mTransfer.getSourceCurrency()));
        assertThat(viewModel.getQuoteTransfer().getValue().getDestinationToken(), is(mTransfer.getDestinationToken()));
        assertThat(viewModel.getQuoteTransfer().getValue().getDestinationAmount(),
                is(mTransfer.getDestinationAmount()));
        assertThat(viewModel.getQuoteTransfer().getValue().getDestinationCurrency(),
                is(mTransfer.getDestinationCurrency()));
        assertThat(viewModel.getQuoteTransfer().getValue().getMemo(), is(mTransfer.getMemo()));
    }

    @Test
    public void testCreateQuoteTransfer_Error() throws Exception {
        String errorResponse = mResourceManager.getResourceContent("transfer_error_response.json");
        final HyperwalletErrors errors = JsonUtils.fromJsonString(errorResponse,
                new TypeReference<HyperwalletErrors>() {
                });

        final HyperwalletTransferMethod transferMethod = new HyperwalletTransferMethod();
        transferMethod.setField(TOKEN, "trm-bank-token");
        transferMethod.setField(TRANSFER_METHOD_CURRENCY, "CAD");

        doAnswer(new Answer() {
            private int call = 0;

            @Override
            public Object answer(InvocationOnMock invocation) {
                if (++call == 1) { // 1st call
                    TransferRepository.CreateTransferCallback callback = invocation.getArgument(1);
                    callback.onTransferCreated(mTransfer);
                    return callback;
                }

                // 2nd call
                TransferRepository.CreateTransferCallback callback = invocation.getArgument(1);
                callback.onError(errors);
                return callback;

            }
        }).when(mTransferRepository).createTransfer(any(Transfer.class),
                any(TransferRepository.CreateTransferCallback.class));

        CreateTransferViewModel.CreateTransferViewModelFactory factory =
                new CreateTransferViewModel.CreateTransferViewModelFactory(mTransferRepository,
                        mTransferMethodRepository, mUserRepository);

        CreateTransferViewModel viewModel = factory.create(CreateTransferViewModel.class);
        viewModel.setTransferNotes("Create quote test notes");
        viewModel.setTransferAmount("123.23");

        // test
        viewModel.createQuoteTransfer();

        assertThat(viewModel.getQuoteErrors().getValue(), is(notNullValue()));
        assertThat(viewModel.getQuoteErrors().getValue().getContent().getErrors(),
                Matchers.<HyperwalletError>hasSize(1));
        assertThat(viewModel.getQuoteErrors().getValue().getContent().getErrors().get(0).getMessage(),
                is("The source token you provided doesn’t exist or is not a valid source."));
        assertThat(viewModel.getQuoteErrors().getValue().getContent().getErrors().get(0).getCode(),
                is("INVALID_SOURCE_TOKEN"));
    }

    @Test
    public void testCreateTransferViewModelFactory_createCreateTransferViewModelUnSuccessful() {
        CreateTransferViewModel.CreateTransferViewModelFactory factory =
                new CreateTransferViewModel.CreateTransferViewModelFactory(
                        TransferRepositoryFactory.getInstance().getTransferRepository(),
                        TransferMethodRepositoryFactory.getInstance().getTransferMethodRepository(),
                        UserRepositoryFactory.getInstance().getUserRepository());
        mExpectedException.expect(IllegalArgumentException.class);
        mExpectedException.expectMessage(
                "Expecting ViewModel class: com.hyperwallet.android.ui.transfer.viewmodel.CreateTransferViewModel");
        factory.create(FakeModel.class);
    }

    @Test
    public void testCreateTransferViewModelFactory_createCreateTransferViewModelSuccessful() {
        CreateTransferViewModel.CreateTransferViewModelFactory factory =
                new CreateTransferViewModel.CreateTransferViewModelFactory(
                        TransferRepositoryFactory.getInstance().getTransferRepository(),
                        TransferMethodRepositoryFactory.getInstance().getTransferMethodRepository(),
                        UserRepositoryFactory.getInstance().getUserRepository());
        CreateTransferViewModel viewModel = factory.create(CreateTransferViewModel.class);
        assertThat(viewModel, is(notNullValue()));
    }

    class FakeModel extends ViewModel {
    }
}
