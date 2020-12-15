package com.hyperwallet.android.ui.transfer.viewmodel;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import static java.net.HttpURLConnection.HTTP_OK;

import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.TOKEN;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.TRANSFER_METHOD_CURRENCY;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.TYPE;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodTypes.BANK_ACCOUNT;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodTypes.PREPAID_CARD;
import static com.hyperwallet.android.ui.common.intent.HyperwalletIntent.ERROR_SDK_MODULE_UNAVAILABLE;

import android.content.Intent;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProviders;

import com.hyperwallet.android.Configuration;
import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.exception.HyperwalletException;
import com.hyperwallet.android.listener.HyperwalletListener;
import com.hyperwallet.android.model.Error;
import com.hyperwallet.android.model.Errors;
import com.hyperwallet.android.model.TypeReference;
import com.hyperwallet.android.model.paging.PageList;
import com.hyperwallet.android.model.transfer.Transfer;
import com.hyperwallet.android.model.transfermethod.PrepaidCard;
import com.hyperwallet.android.model.transfermethod.TransferMethod;
import com.hyperwallet.android.model.user.User;
import com.hyperwallet.android.ui.common.view.ProgramModel;
import com.hyperwallet.android.ui.testutils.TestAuthenticationProvider;
import com.hyperwallet.android.ui.testutils.rule.HyperwalletExternalResourceManager;
import com.hyperwallet.android.ui.testutils.rule.HyperwalletMockWebServer;
import com.hyperwallet.android.ui.transfer.TransferSource;
import com.hyperwallet.android.ui.transfer.repository.TransferRepository;
import com.hyperwallet.android.ui.transfer.repository.TransferRepositoryFactory;
import com.hyperwallet.android.ui.transfer.view.CreateTransferActivity;
import com.hyperwallet.android.ui.transfermethod.repository.PrepaidCardRepository;
import com.hyperwallet.android.ui.transfermethod.repository.PrepaidCardRepositoryFactory;
import com.hyperwallet.android.ui.transfermethod.repository.TransferMethodRepository;
import com.hyperwallet.android.ui.transfermethod.repository.TransferMethodRepositoryFactory;
import com.hyperwallet.android.ui.user.repository.UserRepository;
import com.hyperwallet.android.ui.user.repository.UserRepositoryFactory;
import com.hyperwallet.android.util.JsonUtils;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.stubbing.Answer;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    @Mock
    private PrepaidCardRepository mPrepaidCardRepository;

    private Transfer mTransfer;
    private TransferSource mTransferSource;
    private TransferMethod mTransferMethod;
    private PrepaidCard mPrepaidCard;
    private List<PrepaidCard> mPrepaidCardList;

    @Mock
    private Hyperwallet mHyperwallet;

    private static Configuration mConfiguration;
    private static String mJwtToken;


    @Before
    public void setup() throws HyperwalletException, JSONException {
        Hyperwallet.getInstance(new TestAuthenticationProvider());
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(mResourceManager
                .getResourceContent("authentication_token_response.json")).mock();

        try {
            mJwtToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9."
                    +
                    "eyJzdWIiOiJ1c3ItdG9rZW4iLCJpYXQiOjE1Nzk3MzAzOTIsImV4cCI6MTY3OTczMDk5MiwiYXVkIjoidXNyLXRva2VuIiwiaXNzIjoicHJnLXRva2VuIiwicmVzdC11cmkiOiJodHRwOi"
                    +
                    "8vbG9jYWxob3N0OjgwODAvcmVzdC92My8iLCJncmFwaHFsLXVyaSI6Imh0dHA6Ly9sb2NhbGhvc3Q6ODA4MC9ncmFwaHFsIiwicHJvZ3JhbS1tb2RlbCI6IldBTExFVF9NT0RFTCJ9."
                    + "jpMquopCyBPT1n32OoZLu1PylxtTepuS_KrCZ-xLgDgy9JtcqMDy7zbiBDmj7AQqpGR9loaZq3YQDCjwp8Sy_Q";
            mConfiguration = new Configuration(mJwtToken);
        } catch (JSONException e) {
            fail("Unable to parse json response");
        }

        final String prepaidCardResponse = mResourceManager.getResourceContent("prepaid_card_response.json");
        mPrepaidCard = new PrepaidCard(prepaidCardResponse);

        String responseJson = mResourceManager.getResourceContent("prepaid_cards_response.json");
        JSONObject jsonObject = new JSONObject(responseJson);
        final PageList<PrepaidCard> prepaidCardList = new PageList<>(jsonObject, PrepaidCard.class);
        mPrepaidCardList = prepaidCardList.getDataList();


        final String transferListResponse = mResourceManager.getResourceContent("transfer_method_list_response.json");
        JSONObject jObject = new JSONObject(transferListResponse);
        final PageList<TransferMethod> transferMethodList = new PageList<>(jObject, TransferMethod.class);

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

        mTransferMethod = new TransferMethod();
        mTransferMethod.setField(TOKEN, "trm-bank-token");
        mTransferMethod.setField(TYPE, PREPAID_CARD);
        mTransferMethod.setField(TRANSFER_METHOD_CURRENCY, "CAD");


        mTransferSource = new TransferSource();
        mTransferSource.setToken("usr-fake");
        mTransferSource.setType(BANK_ACCOUNT);
        mTransferSource.setIdentification(null);


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
                User user = new User.Builder()
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
                callback.onTransferMethodLoaded(mTransferMethod);
                return callback;
            }
        }).when(mTransferMethodRepository).loadLatestTransferMethod(any(TransferMethodRepository
                .LoadTransferMethodCallback.class));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodRepository.LoadTransferMethodListCallback callback = invocation.getArgument(0);
                callback.onTransferMethodListLoaded(transferMethodList.getDataList());
                return callback;
            }
        }).when(mTransferMethodRepository).loadTransferMethods(any(TransferMethodRepository
                .LoadTransferMethodListCallback.class));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                PrepaidCardRepository.LoadPrepaidCardsCallback callback = invocation.getArgument(0);
                callback.onPrepaidCardListLoaded(mPrepaidCardList);
                return callback;
            }
        }).when(mPrepaidCardRepository).loadPrepaidCards(ArgumentMatchers.any(
                PrepaidCardRepository.LoadPrepaidCardsCallback.class));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                PrepaidCardRepository.LoadPrepaidCardCallback callback = invocation.getArgument(0);
                callback.onPrepaidCardLoaded(new PrepaidCard());
                return callback;
            }
        }).when(mPrepaidCardRepository).loadPrepaidCard(ArgumentMatchers.any(String.class), ArgumentMatchers.any(
                PrepaidCardRepository.LoadPrepaidCardCallback.class));


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
    public void testInit_withFundingSource_walletModel() {
        CreateTransferViewModel viewModel = spy(new CreateTransferViewModel.CreateTransferViewModelFactory(
                mTransfer.getToken(), mTransferRepository, mTransferMethodRepository, mUserRepository,
                mPrepaidCardRepository
        ).create(CreateTransferViewModel.class));
        viewModel.init("0");

        doReturn(ProgramModel.WALLET_MODEL).when(viewModel).getProgramModel();

        verify(viewModel, never()).loadTransferSource();

        assertThat(viewModel, is(notNullValue()));
        assertThat(viewModel.getProgramModel(), is(ProgramModel.WALLET_MODEL));
        assertThat(viewModel.isTransferAllAvailableFunds().getValue(), is(false));
        assertThat(viewModel.getTransferAmount().getValue(), is(nullValue()));
        assertThat(viewModel.getTransferNotes().getValue(), is(nullValue()));
        assertThat(viewModel.getTransferDestination().getValue().getField(TOKEN), is(mTransferMethod.getField(TOKEN)));
        assertThat(viewModel.getTransferDestination().getValue().getField(TRANSFER_METHOD_CURRENCY),
                is(mTransferMethod.getField(TRANSFER_METHOD_CURRENCY)));
        assertThat(viewModel.isLoading().getValue(), is(false));
        assertThat(viewModel.isCreateQuoteLoading().getValue(), is(false));
        assertThat(viewModel.getQuoteAvailableFunds().getValue().getToken(), is(mTransfer.getToken()));
        assertThat(viewModel.getQuoteAvailableFunds().getValue().getStatus(), is(mTransfer.getStatus()));
        assertThat(viewModel.getQuoteAvailableFunds().getValue().getClientTransferId(),
                is(mTransfer.getClientTransferId()));
        assertThat(viewModel.getQuoteAvailableFunds().getValue().getSourceToken(), is(mTransfer.getSourceToken()));
        assertThat(viewModel.getQuoteAvailableFunds().getValue().getSourceCurrency(),
                is(mTransfer.getSourceCurrency()));
        assertThat(viewModel.getQuoteAvailableFunds().getValue().getDestinationToken(),
                is(mTransfer.getDestinationToken()));
        assertThat(viewModel.getQuoteAvailableFunds().getValue().getDestinationAmount(),
                is(mTransfer.getDestinationAmount()));
        assertThat(viewModel.getQuoteAvailableFunds().getValue().getDestinationCurrency(),
                is(mTransfer.getDestinationCurrency()));
        assertThat(viewModel.getQuoteAvailableFunds().getValue().getMemo(), is(mTransfer.getMemo()));
        assertThat(viewModel.getCreateTransfer().getValue(), is(nullValue()));
        assertThat(viewModel.getShowFxRateChange().getValue(), is(false));
    }

    @Test
    public void testInit_withFundingSource_cardModel() {
        CreateTransferViewModel viewModel = spy(new CreateTransferViewModel.CreateTransferViewModelFactory(
                mTransfer.getToken(), mTransferRepository, mTransferMethodRepository, mUserRepository,
                mPrepaidCardRepository
        ).create(CreateTransferViewModel.class));
        viewModel.init("0");

        doReturn(ProgramModel.CARD_ONLY_MODEL).when(viewModel).getProgramModel();

        verify(viewModel, never()).loadTransferSource();
        assertThat(viewModel.getProgramModel(), is(ProgramModel.CARD_ONLY_MODEL));
        assertThat(viewModel, is(notNullValue()));
        assertThat(viewModel.isTransferAllAvailableFunds().getValue(), is(false));
        assertThat(viewModel.getTransferAmount().getValue(), is(nullValue()));
        assertThat(viewModel.getTransferNotes().getValue(), is(nullValue()));
        assertThat(viewModel.getTransferDestination().getValue().getField(TOKEN), is(mTransferMethod.getField(TOKEN)));
        assertThat(viewModel.getTransferDestination().getValue().getField(TRANSFER_METHOD_CURRENCY),
                is(mTransferMethod.getField(TRANSFER_METHOD_CURRENCY)));
        assertThat(viewModel.isLoading().getValue(), is(false));
        assertThat(viewModel.isCreateQuoteLoading().getValue(), is(false));
        assertThat(viewModel.getQuoteAvailableFunds().getValue().getToken(), is(mTransfer.getToken()));
        assertThat(viewModel.getQuoteAvailableFunds().getValue().getStatus(), is(mTransfer.getStatus()));
        assertThat(viewModel.getQuoteAvailableFunds().getValue().getClientTransferId(),
                is(mTransfer.getClientTransferId()));
        assertThat(viewModel.getQuoteAvailableFunds().getValue().getSourceToken(), is(mTransfer.getSourceToken()));
        assertThat(viewModel.getQuoteAvailableFunds().getValue().getSourceCurrency(),
                is(mTransfer.getSourceCurrency()));
        assertThat(viewModel.getQuoteAvailableFunds().getValue().getDestinationToken(),
                is(mTransfer.getDestinationToken()));
        assertThat(viewModel.getQuoteAvailableFunds().getValue().getDestinationAmount(),
                is(mTransfer.getDestinationAmount()));
        assertThat(viewModel.getQuoteAvailableFunds().getValue().getDestinationCurrency(),
                is(mTransfer.getDestinationCurrency()));
        assertThat(viewModel.getQuoteAvailableFunds().getValue().getMemo(), is(mTransfer.getMemo()));
        assertThat(viewModel.getCreateTransfer().getValue(), is(nullValue()));
        assertThat(viewModel.getShowFxRateChange().getValue(), is(false));
    }

    @Test
    public void testInit_withoutFundingSource_walletModel() {
        CreateTransferViewModel viewModel = spy(new CreateTransferViewModel.CreateTransferViewModelFactory(
                mTransferRepository, mTransferMethodRepository, mUserRepository, mPrepaidCardRepository
        ).create(CreateTransferViewModel.class));
        viewModel.init("0");
        doReturn(ProgramModel.WALLET_MODEL).when(viewModel).getProgramModel();
        verify(viewModel).loadTransferSource();

        assertThat(viewModel, is(notNullValue()));
        assertThat(viewModel.getProgramModel(), is(ProgramModel.WALLET_MODEL));
        assertThat(viewModel.isTransferAllAvailableFunds().getValue(), is(false));
        assertThat(viewModel.getTransferAmount().getValue(), is(nullValue()));
        assertThat(viewModel.getTransferNotes().getValue(), is(nullValue()));
        assertThat(viewModel.getTransferDestination().getValue().getField(TOKEN), is(mTransferMethod.getField(TOKEN)));
        assertThat(viewModel.getTransferDestination().getValue().getField(TRANSFER_METHOD_CURRENCY),
                is(mTransferMethod.getField(TRANSFER_METHOD_CURRENCY)));
        assertThat(viewModel.isLoading().getValue(), is(false));
        assertThat(viewModel.isCreateQuoteLoading().getValue(), is(false));
        assertThat(viewModel.getQuoteAvailableFunds().getValue().getToken(), is(mTransfer.getToken()));
        assertThat(viewModel.getQuoteAvailableFunds().getValue().getStatus(), is(mTransfer.getStatus()));
        assertThat(viewModel.getQuoteAvailableFunds().getValue().getClientTransferId(),
                is(mTransfer.getClientTransferId()));
        assertThat(viewModel.getQuoteAvailableFunds().getValue().getSourceToken(), is(mTransfer.getSourceToken()));
        assertThat(viewModel.getQuoteAvailableFunds().getValue().getSourceCurrency(),
                is(mTransfer.getSourceCurrency()));
        assertThat(viewModel.getQuoteAvailableFunds().getValue().getDestinationToken(),
                is(mTransfer.getDestinationToken()));
        assertThat(viewModel.getQuoteAvailableFunds().getValue().getDestinationAmount(),
                is(mTransfer.getDestinationAmount()));
        assertThat(viewModel.getQuoteAvailableFunds().getValue().getDestinationCurrency(),
                is(mTransfer.getDestinationCurrency()));
        assertThat(viewModel.getQuoteAvailableFunds().getValue().getMemo(), is(mTransfer.getMemo()));
        assertThat(viewModel.getCreateTransfer().getValue(), is(nullValue()));
        assertThat(viewModel.getShowFxRateChange().getValue(), is(false));
    }

    @Test
    public void testInit_withoutFundingSource_cardModel() {
        CreateTransferViewModel viewModel = spy(new CreateTransferViewModel.CreateTransferViewModelFactory(
                mTransferRepository, mTransferMethodRepository, mUserRepository, mPrepaidCardRepository
        ).create(CreateTransferViewModel.class));
        viewModel.init("0");
        doReturn(ProgramModel.CARD_ONLY_MODEL).when(viewModel).getProgramModel();
        verify(viewModel).loadPrepaidCardList(any(ArrayList.class));

        assertThat(viewModel, is(notNullValue()));
        assertThat(viewModel.isTransferAllAvailableFunds().getValue(), is(false));
        assertThat(viewModel.getProgramModel(), is(ProgramModel.CARD_ONLY_MODEL));
        assertThat(viewModel.getTransferAmount().getValue(), is(nullValue()));
        assertThat(viewModel.getTransferNotes().getValue(), is(nullValue()));
        assertThat(viewModel.getTransferDestination().getValue().getField(TOKEN), is(mTransferMethod.getField(TOKEN)));
        assertThat(viewModel.getTransferDestination().getValue().getField(TRANSFER_METHOD_CURRENCY),
                is(mTransferMethod.getField(TRANSFER_METHOD_CURRENCY)));
        assertThat(viewModel.isLoading().getValue(), is(false));
        assertThat(viewModel.isCreateQuoteLoading().getValue(), is(false));
        assertThat(viewModel.getQuoteAvailableFunds().getValue().getToken(), is(mTransfer.getToken()));
        assertThat(viewModel.getQuoteAvailableFunds().getValue().getStatus(), is(mTransfer.getStatus()));
        assertThat(viewModel.getQuoteAvailableFunds().getValue().getClientTransferId(),
                is(mTransfer.getClientTransferId()));
        assertThat(viewModel.getQuoteAvailableFunds().getValue().getSourceToken(), is(mTransfer.getSourceToken()));
        assertThat(viewModel.getQuoteAvailableFunds().getValue().getSourceCurrency(),
                is(mTransfer.getSourceCurrency()));
        assertThat(viewModel.getQuoteAvailableFunds().getValue().getDestinationToken(),
                is(mTransfer.getDestinationToken()));
        assertThat(viewModel.getQuoteAvailableFunds().getValue().getDestinationAmount(),
                is(mTransfer.getDestinationAmount()));
        assertThat(viewModel.getQuoteAvailableFunds().getValue().getDestinationCurrency(),
                is(mTransfer.getDestinationCurrency()));
        assertThat(viewModel.getQuoteAvailableFunds().getValue().getMemo(), is(mTransfer.getMemo()));
        assertThat(viewModel.getCreateTransfer().getValue(), is(nullValue()));
        assertThat(viewModel.getShowFxRateChange().getValue(), is(false));
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
        assertThat(viewModel.getTransferAmount().getValue(), is("0"));

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
    public void testCreateQuoteTransfer_isSuccessful() {
        CreateTransferViewModel.CreateTransferViewModelFactory factory =
                new CreateTransferViewModel.CreateTransferViewModelFactory(mTransferRepository,
                        mTransferMethodRepository, mUserRepository, mPrepaidCardRepository);

        CreateTransferViewModel viewModel = factory.create(CreateTransferViewModel.class);


        viewModel.setTransferNotes("Create quote test notes");
        viewModel.setTransferAmount("123.23");

        viewModel.init("0");
        // test
        viewModel.createTransfer();

        assertThat(viewModel.getCreateTransferError().getValue(), is(nullValue()));
        assertThat(viewModel.getCreateTransfer().getValue(), is(notNullValue()));
        assertThat(viewModel.getCreateTransfer().getValue().getContent().getSourceToken(),
                is(mTransfer.getSourceToken()));
        assertThat(viewModel.getCreateTransfer().getValue().getContent().getToken(), is(mTransfer.getToken()));
        assertThat(viewModel.getCreateTransfer().getValue().getContent().getCreatedOn(), is(mTransfer.getCreatedOn()));
        assertThat(viewModel.getCreateTransfer().getValue().getContent().getClientTransferId(),
                is(mTransfer.getClientTransferId()));
        assertThat(viewModel.getCreateTransfer().getValue().getContent().getSourceCurrency(),
                is(mTransfer.getSourceCurrency()));
        assertThat(viewModel.getCreateTransfer().getValue().getContent().getDestinationToken(),
                is(mTransfer.getDestinationToken()));
        assertThat(viewModel.getCreateTransfer().getValue().getContent().getDestinationAmount(),
                is(mTransfer.getDestinationAmount()));
        assertThat(viewModel.getCreateTransfer().getValue().getContent().getDestinationCurrency(),
                is(mTransfer.getDestinationCurrency()));
        assertThat(viewModel.getCreateTransfer().getValue().getContent().getMemo(), is(mTransfer.getMemo()));
        assertThat(viewModel.getShowFxRateChange().getValue(), is(false));
    }


    @Test
    public void testCreateQuoteTransfer_isSuccessfulWithAllAvailableFunds() {
        CreateTransferViewModel.CreateTransferViewModelFactory factory =
                new CreateTransferViewModel.CreateTransferViewModelFactory(mTransferRepository,
                        mTransferMethodRepository, mUserRepository, mPrepaidCardRepository);

        CreateTransferViewModel viewModel = factory.create(CreateTransferViewModel.class);

        viewModel.setTransferNotes("Create quote test notes");
        viewModel.setTransferAllAvailableFunds(true);
        viewModel.setTransferAmount("124.23");
        viewModel.init("0");
        viewModel.setGroupSeparator(",");
        viewModel.setDecimalSeparator(".");
        // test
        viewModel.createTransfer();

        assertThat(viewModel.getCreateTransferError().getValue(), is(nullValue()));
        assertThat(viewModel.getCreateTransfer().getValue(), is(notNullValue()));
        assertThat(viewModel.getCreateTransfer().getValue().getContent().getSourceToken(),
                is(mTransfer.getSourceToken()));
        assertThat(viewModel.getCreateTransfer().getValue().getContent().getToken(), is(mTransfer.getToken()));
        assertThat(viewModel.getCreateTransfer().getValue().getContent().getCreatedOn(), is(mTransfer.getCreatedOn()));
        assertThat(viewModel.getCreateTransfer().getValue().getContent().getClientTransferId(),
                is(mTransfer.getClientTransferId()));
        assertThat(viewModel.getCreateTransfer().getValue().getContent().getSourceCurrency(),
                is(mTransfer.getSourceCurrency()));
        assertThat(viewModel.getCreateTransfer().getValue().getContent().getDestinationToken(),
                is(mTransfer.getDestinationToken()));
        assertThat(viewModel.getCreateTransfer().getValue().getContent().getDestinationAmount(),
                is(mTransfer.getDestinationAmount()));
        assertThat(viewModel.getCreateTransfer().getValue().getContent().getDestinationCurrency(),
                is(mTransfer.getDestinationCurrency()));
        assertThat(viewModel.getCreateTransfer().getValue().getContent().getMemo(), is(mTransfer.getMemo()));
        assertThat(viewModel.getShowFxRateChange().getValue(), is(true));
    }

    @Test
    public void testCreateQuoteTransfer_hasGenericError() throws Exception {
        String errorResponse = mResourceManager.getResourceContent("errors/transfer_error_response.json");
        final Errors errors = JsonUtils.fromJsonString(errorResponse,
                new TypeReference<Errors>() {
                });

        final TransferMethod transferMethod = new TransferMethod();
        transferMethod.setField(TOKEN, "trm-bank-token");
        transferMethod.setField(TRANSFER_METHOD_CURRENCY, "CAD");

        CreateTransferViewModel.CreateTransferViewModelFactory factory =
                new CreateTransferViewModel.CreateTransferViewModelFactory(mTransferRepository,
                        mTransferMethodRepository, mUserRepository, mPrepaidCardRepository);

        CreateTransferViewModel viewModel = factory.create(CreateTransferViewModel.class);
        viewModel.setTransferAllAvailableFunds(true);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferRepository.CreateTransferCallback callback = invocation.getArgument(1);
                callback.onError(errors);
                return callback;

            }
        }).when(mTransferRepository).createTransfer(any(Transfer.class),
                any(TransferRepository.CreateTransferCallback.class));

        viewModel.init("0");
        // test
        viewModel.setQuoteAvailableTransferFunds(true);
        viewModel.createTransfer();

        assertThat(viewModel.getCreateTransferError().getValue(), is(notNullValue()));
        assertThat(viewModel.getCreateTransferError().getValue().getContent().getErrors(),
                Matchers.<Error>hasSize(1));
        assertThat(viewModel.getCreateTransferError().getValue().getContent().getErrors().get(0).getMessage(),
                is("The source token you provided doesn’t exist or is not a valid source."));
        assertThat(viewModel.getCreateTransferError().getValue().getContent().getErrors().get(0).getCode(),
                is("INVALID_SOURCE_TOKEN"));
    }

    @Test
    public void testCreateQuoteTransfer_hasInvalidAmountError() throws Exception {
        String errorResponse = mResourceManager.getResourceContent(
                "errors/create_transfer_error_invalid_amount_response.json");
        final Errors errors = JsonUtils.fromJsonString(errorResponse,
                new TypeReference<Errors>() {
                });

        final TransferMethod transferMethod = new TransferMethod();
        transferMethod.setField(TOKEN, "trm-bank-token");
        transferMethod.setField(TRANSFER_METHOD_CURRENCY, "CAD");

        CreateTransferViewModel.CreateTransferViewModelFactory factory =
                new CreateTransferViewModel.CreateTransferViewModelFactory(mTransferRepository,
                        mTransferMethodRepository, mUserRepository, mPrepaidCardRepository);

        CreateTransferViewModel viewModel = factory.create(CreateTransferViewModel.class);
        viewModel.setTransferAllAvailableFunds(true);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferRepository.CreateTransferCallback callback = invocation.getArgument(1);
                callback.onError(errors);
                return callback;

            }
        }).when(mTransferRepository).createTransfer(any(Transfer.class),
                any(TransferRepository.CreateTransferCallback.class));


        viewModel.init("0");
        // test
        viewModel.setQuoteAvailableTransferFunds(true);
        viewModel.createTransfer();

        assertThat(viewModel.getInvalidAmountError().getValue(), is(notNullValue()));
        assertThat(viewModel.getInvalidAmountError().getValue().getContent().getMessage(), is("Invalid amount."));
        assertThat(viewModel.getInvalidAmountError().getValue().getContent().getCode(), is("INVALID_AMOUNT"));
        assertThat(viewModel.getInvalidAmountError().getValue().getContent().getFieldName(), is("destinationAmount"));
    }

    @Test
    public void testCreateQuoteTransfer_hasInvalidDestinationError() throws Exception {
        String errorResponse = mResourceManager.getResourceContent("errors/transfer_destination_input_invalid.json");
        final Errors errors = JsonUtils.fromJsonString(errorResponse,
                new TypeReference<Errors>() {
                });

        final TransferMethod transferMethod = new TransferMethod();
        transferMethod.setField(TOKEN, "trm-bank-token");
        transferMethod.setField(TRANSFER_METHOD_CURRENCY, "CAD");

        CreateTransferViewModel.CreateTransferViewModelFactory factory =
                new CreateTransferViewModel.CreateTransferViewModelFactory(mTransferRepository,
                        mTransferMethodRepository, mUserRepository, mPrepaidCardRepository);

        CreateTransferViewModel viewModel = factory.create(CreateTransferViewModel.class);
        viewModel.setTransferAllAvailableFunds(true);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferRepository.CreateTransferCallback callback = invocation.getArgument(1);
                callback.onError(errors);
                return callback;
            }
        }).when(mTransferRepository).createTransfer(any(Transfer.class),
                any(TransferRepository.CreateTransferCallback.class));

        viewModel.init("0");
        // test
        viewModel.createTransfer();

        assertThat(viewModel.getInvalidDestinationError().getValue(), is(notNullValue()));
        assertThat(viewModel.getInvalidDestinationError().getValue().getContent().getMessage(),
                is("Invalid transfer destination."));
        assertThat(viewModel.getInvalidDestinationError().getValue().getContent().getCode(), is("INVALID_DESTINATION"));
        assertThat(viewModel.getInvalidDestinationError().getValue().getContent().getFieldName(),
                is("destinationToken"));
    }

    @Test
    public void testRetry_isTransferSourceTokenUnknown_walletModel() {
        CreateTransferViewModel.CreateTransferViewModelFactory factory =
                new CreateTransferViewModel.CreateTransferViewModelFactory(mTransferRepository,
                        mTransferMethodRepository, mUserRepository, mPrepaidCardRepository);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                UserRepository.LoadUserCallback callback = invocation.getArgument(0);
                callback.onError(null);
                return callback;
            }
        }).when(mUserRepository).loadUser(any(UserRepository.LoadUserCallback.class));

        // test
        CreateTransferViewModel viewModel = spy(factory.create(CreateTransferViewModel.class));
        doReturn(ProgramModel.WALLET_MODEL).when(viewModel).getProgramModel();
        viewModel.init("0");
        viewModel.retry();

        verify(mUserRepository, times(2)).loadUser(any(UserRepository.LoadUserCallback.class));
    }

    @Test
    public void testRetry_isTransferSourceTokenUnknown_cardModel() {
        CreateTransferViewModel.CreateTransferViewModelFactory factory =
                new CreateTransferViewModel.CreateTransferViewModelFactory(mTransferRepository,
                        mTransferMethodRepository, mUserRepository, mPrepaidCardRepository);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                PrepaidCardRepository.LoadPrepaidCardsCallback callback = invocation.getArgument(0);
                callback.onError(null);
                return callback;
            }
        }).when(mPrepaidCardRepository).loadPrepaidCards(any(PrepaidCardRepository.LoadPrepaidCardsCallback.class));

        // test
        CreateTransferViewModel viewModel = spy(factory.create(CreateTransferViewModel.class));
        doReturn(ProgramModel.CARD_ONLY_MODEL).when(viewModel).getProgramModel();
        viewModel.init("0");
        viewModel.retry();

        verify(mPrepaidCardRepository, times(2)).loadPrepaidCards(
                any(PrepaidCardRepository.LoadPrepaidCardsCallback.class));
    }

    @Test
    public void testRetry_isTransferDestinationUnknownOnError_walletModel() {
        CreateTransferViewModel.CreateTransferViewModelFactory factory =
                new CreateTransferViewModel.CreateTransferViewModelFactory(mTransferRepository,
                        mTransferMethodRepository, mUserRepository, mPrepaidCardRepository);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodRepository.LoadTransferMethodCallback callback = invocation.getArgument(0);
                callback.onError(null);
                return callback;
            }
        }).when(mTransferMethodRepository).loadLatestTransferMethod(any(TransferMethodRepository
                .LoadTransferMethodCallback.class));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodRepository.LoadTransferMethodListCallback callback = invocation.getArgument(0);
                callback.onError(null);
                return callback;
            }
        }).when(mTransferMethodRepository).loadTransferMethods(any(TransferMethodRepository
                .LoadTransferMethodListCallback.class));

        // test
        CreateTransferViewModel viewModel = spy(factory.create(CreateTransferViewModel.class));
        doReturn(ProgramModel.WALLET_MODEL).when(viewModel).getProgramModel();
        viewModel.init("0");
        viewModel.retry();

        verify(mUserRepository, times(1)).loadUser(any(UserRepository.LoadUserCallback.class));
        verify(mTransferMethodRepository, times(2)).loadLatestTransferMethod(any(TransferMethodRepository
                .LoadTransferMethodCallback.class));
    }

    @Test
    public void testRetry_isTransferDestinationUnknownOnError_cardModel() {
        CreateTransferViewModel.CreateTransferViewModelFactory factory =
                new CreateTransferViewModel.CreateTransferViewModelFactory(mTransferRepository,
                        mTransferMethodRepository, mUserRepository, mPrepaidCardRepository);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodRepository.LoadTransferMethodCallback callback = invocation.getArgument(0);
                callback.onError(null);
                return callback;
            }
        }).when(mTransferMethodRepository).loadLatestTransferMethod(any(TransferMethodRepository
                .LoadTransferMethodCallback.class));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodRepository.LoadTransferMethodListCallback callback = invocation.getArgument(0);
                callback.onError(null);
                return callback;
            }
        }).when(mTransferMethodRepository).loadTransferMethods(any(TransferMethodRepository
                .LoadTransferMethodListCallback.class));

        // test
        CreateTransferViewModel viewModel = spy(factory.create(CreateTransferViewModel.class));
        doReturn(ProgramModel.CARD_ONLY_MODEL).when(viewModel).getProgramModel();
        viewModel.init("0");
        viewModel.retry();

        verify(mTransferMethodRepository, times(2)).loadLatestTransferMethod(any(TransferMethodRepository
                .LoadTransferMethodCallback.class));
    }

    @Test
    public void testRetry_isTransferDestinationUnknownNotFound_walletModel() {
        CreateTransferViewModel.CreateTransferViewModelFactory factory =
                new CreateTransferViewModel.CreateTransferViewModelFactory(mTransferRepository,
                        mTransferMethodRepository, mUserRepository, mPrepaidCardRepository);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodRepository.LoadTransferMethodCallback callback = invocation.getArgument(0);
                callback.onTransferMethodLoaded(null);
                return callback;
            }
        }).when(mTransferMethodRepository).loadLatestTransferMethod(any(TransferMethodRepository
                .LoadTransferMethodCallback.class));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodRepository.LoadTransferMethodListCallback callback = invocation.getArgument(0);
                callback.onTransferMethodListLoaded(null);
                return callback;
            }
        }).when(mTransferMethodRepository).loadTransferMethods(any(TransferMethodRepository
                .LoadTransferMethodListCallback.class));

        // test
        CreateTransferViewModel viewModel = spy(factory.create(CreateTransferViewModel.class));
        doReturn(ProgramModel.WALLET_MODEL).when(viewModel).getProgramModel();
        viewModel.init("0");
        viewModel.retry();

        verify(mUserRepository, times(1)).loadUser(any(UserRepository.LoadUserCallback.class));
        verify(mTransferMethodRepository, times(2)).loadLatestTransferMethod(any(TransferMethodRepository
                .LoadTransferMethodCallback.class));
    }

    @Test
    public void testRetry_isTransferDestinationUnknownNotFound_cardModel() {
        CreateTransferViewModel.CreateTransferViewModelFactory factory =
                new CreateTransferViewModel.CreateTransferViewModelFactory(mTransferRepository,
                        mTransferMethodRepository, mUserRepository, mPrepaidCardRepository);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodRepository.LoadTransferMethodCallback callback = invocation.getArgument(0);
                callback.onTransferMethodLoaded(null);
                return callback;
            }
        }).when(mTransferMethodRepository).loadLatestTransferMethod(any(TransferMethodRepository
                .LoadTransferMethodCallback.class));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodRepository.LoadTransferMethodListCallback callback = invocation.getArgument(0);
                callback.onTransferMethodListLoaded(null);
                return callback;
            }
        }).when(mTransferMethodRepository).loadTransferMethods(any(TransferMethodRepository
                .LoadTransferMethodListCallback.class));

        // test
        CreateTransferViewModel viewModel = spy(factory.create(CreateTransferViewModel.class));
        doReturn(ProgramModel.CARD_ONLY_MODEL).when(viewModel).getProgramModel();
        assertThat(viewModel.getProgramModel(), is(ProgramModel.CARD_ONLY_MODEL));
        viewModel.init("0");
        viewModel.retry();

        verify(mTransferMethodRepository, times(2)).loadLatestTransferMethod(any(TransferMethodRepository
                .LoadTransferMethodCallback.class));
    }

    @Test
    public void testRetry_isQuoteInvalidWithQuoteObjectNull_walletModel() {
        CreateTransferViewModel.CreateTransferViewModelFactory factory =
                new CreateTransferViewModel.CreateTransferViewModelFactory(mTransferRepository,
                        mTransferMethodRepository, mUserRepository, mPrepaidCardRepository);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferRepository.CreateTransferCallback callback = invocation.getArgument(1);
                callback.onError(Errors.getEmptyInstance());
                return callback;
            }
        }).when(mTransferRepository).createTransfer(any(Transfer.class),
                any(TransferRepository.CreateTransferCallback.class));

        // test
        CreateTransferViewModel viewModel = spy(factory.create(CreateTransferViewModel.class));
        doReturn(ProgramModel.WALLET_MODEL).when(viewModel).getProgramModel();
        viewModel.init("0");
        viewModel.retry();

        verify(mUserRepository, times(1)).loadUser(any(UserRepository.LoadUserCallback.class));
        verify(mTransferMethodRepository, times(1)).loadLatestTransferMethod(any(TransferMethodRepository
                .LoadTransferMethodCallback.class));
        verify(mTransferRepository, times(2)).createTransfer(any(Transfer.class),
                any(TransferRepository.CreateTransferCallback.class));
        verify(mPrepaidCardRepository, times(1)).loadPrepaidCards(
                any(PrepaidCardRepository.LoadPrepaidCardsCallback.class));
    }


    @Test
    public void testRetry_isQuoteInvalidWithQuoteObjectNull_cardModel() {
        CreateTransferViewModel.CreateTransferViewModelFactory factory =
                new CreateTransferViewModel.CreateTransferViewModelFactory(mTransferRepository,
                        mTransferMethodRepository, mUserRepository, mPrepaidCardRepository);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferRepository.CreateTransferCallback callback = invocation.getArgument(1);
                callback.onError(Errors.getEmptyInstance());
                return callback;
            }
        }).when(mTransferRepository).createTransfer(any(Transfer.class),
                any(TransferRepository.CreateTransferCallback.class));

        // test
        CreateTransferViewModel viewModel = spy(factory.create(CreateTransferViewModel.class));
        doReturn(ProgramModel.CARD_ONLY_MODEL).when(viewModel).getProgramModel();
        viewModel.init("0");
        viewModel.retry();

        verify(mTransferMethodRepository, times(1)).loadLatestTransferMethod(any(TransferMethodRepository
                .LoadTransferMethodCallback.class));
        verify(mTransferRepository, times(2)).createTransfer(any(Transfer.class),
                any(TransferRepository.CreateTransferCallback.class));
        verify(mPrepaidCardRepository, times(1)).loadPrepaidCards(
                any(PrepaidCardRepository.LoadPrepaidCardsCallback.class));
    }

    @Test
    public void testRetry_isQuoteInvalidWithQuoteSourceNotValid_walletModel() {
        CreateTransferViewModel.CreateTransferViewModelFactory factory =
                new CreateTransferViewModel.CreateTransferViewModelFactory(mTransferRepository,
                        mTransferMethodRepository, mUserRepository, mPrepaidCardRepository);
        // test
        CreateTransferViewModel viewModel = spy(factory.create(CreateTransferViewModel.class));
        doReturn(ProgramModel.WALLET_MODEL).when(viewModel).getProgramModel();
        viewModel.init("0");
        viewModel.retry();

        verify(mUserRepository, times(1)).loadUser(any(UserRepository.LoadUserCallback.class));
        verify(mTransferMethodRepository, times(1)).loadLatestTransferMethod(any(TransferMethodRepository
                .LoadTransferMethodCallback.class));
        verify(mTransferRepository, times(2)).createTransfer(any(Transfer.class),
                any(TransferRepository.CreateTransferCallback.class));
    }

    @Test
    public void testRetry_isQuoteInvalidWithQuoteSourceNotValid_cardModel() {
        CreateTransferViewModel.CreateTransferViewModelFactory factory =
                new CreateTransferViewModel.CreateTransferViewModelFactory(mTransferRepository,
                        mTransferMethodRepository, mUserRepository, mPrepaidCardRepository);
        // test
        CreateTransferViewModel viewModel = spy(factory.create(CreateTransferViewModel.class));
        doReturn(ProgramModel.CARD_ONLY_MODEL).when(viewModel).getProgramModel();
        viewModel.init("0");
        viewModel.retry();

        verify(mTransferMethodRepository, times(1)).loadLatestTransferMethod(any(TransferMethodRepository
                .LoadTransferMethodCallback.class));
        verify(mTransferRepository, times(2)).createTransfer(any(Transfer.class),
                any(TransferRepository.CreateTransferCallback.class));
    }

    @Test
    public void testRetry_isQuoteInvalidWithQuoteDestinationNotValid_walletModel() {
        CreateTransferViewModel.CreateTransferViewModelFactory factory =
                new CreateTransferViewModel.CreateTransferViewModelFactory(mTransferRepository,
                        mTransferMethodRepository, mUserRepository, mPrepaidCardRepository);

        final Transfer transfer = new Transfer.Builder()
                .token("trf-transfer-token")
                .status(Transfer.TransferStatuses.QUOTED)
                .createdOn(new Date())
                .clientTransferID("ClientId1234122")
                .sourceToken("usr-token-source")
                .sourceCurrency("CAD")
                .destinationToken("trm-card-token")
                .destinationAmount("123.23")
                .destinationCurrency("CAD")
                .memo("Create quote test notes")
                .build();

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferRepository.CreateTransferCallback callback = invocation.getArgument(1);
                callback.onTransferCreated(transfer);
                return callback;
            }
        }).when(mTransferRepository).createTransfer(any(Transfer.class),
                any(TransferRepository.CreateTransferCallback.class));

        // test
        CreateTransferViewModel viewModel = spy(factory.create(CreateTransferViewModel.class));
        doReturn(ProgramModel.WALLET_MODEL).when(viewModel).getProgramModel();
        viewModel.init("0");
        viewModel.retry();

        verify(mUserRepository, times(1)).loadUser(any(UserRepository.LoadUserCallback.class));
        verify(mTransferMethodRepository, times(1)).loadLatestTransferMethod(any(TransferMethodRepository
                .LoadTransferMethodCallback.class));
        verify(mTransferRepository, times(2)).createTransfer(any(Transfer.class),
                any(TransferRepository.CreateTransferCallback.class));
    }

    @Test
    public void testRetry_isQuoteInvalidWithQuoteDestinationNotValid_cardModel() {
        CreateTransferViewModel.CreateTransferViewModelFactory factory =
                new CreateTransferViewModel.CreateTransferViewModelFactory(mTransferRepository,
                        mTransferMethodRepository, mUserRepository, mPrepaidCardRepository);

        final Transfer transfer = new Transfer.Builder()
                .token("trf-transfer-token")
                .status(Transfer.TransferStatuses.QUOTED)
                .createdOn(new Date())
                .clientTransferID("ClientId1234122")
                .sourceToken("usr-token-source")
                .sourceCurrency("CAD")
                .destinationToken("trm-card-token")
                .destinationAmount("123.23")
                .destinationCurrency("CAD")
                .memo("Create quote test notes")
                .build();

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferRepository.CreateTransferCallback callback = invocation.getArgument(1);
                callback.onTransferCreated(transfer);
                return callback;
            }
        }).when(mTransferRepository).createTransfer(any(Transfer.class),
                any(TransferRepository.CreateTransferCallback.class));

        // test
        CreateTransferViewModel viewModel = spy(factory.create(CreateTransferViewModel.class));
        doReturn(ProgramModel.CARD_ONLY_MODEL).when(viewModel).getProgramModel();
        viewModel.init("0");
        viewModel.retry();

        verify(mTransferMethodRepository, times(1)).loadLatestTransferMethod(any(TransferMethodRepository
                .LoadTransferMethodCallback.class));
        verify(mTransferRepository, times(2)).createTransfer(any(Transfer.class),
                any(TransferRepository.CreateTransferCallback.class));
    }

    @Test
    public void testRetry_isTransferAmountKnown_walletModel() {
        CreateTransferViewModel.CreateTransferViewModelFactory factory =
                new CreateTransferViewModel.CreateTransferViewModelFactory(mTransferRepository,
                        mTransferMethodRepository, mUserRepository, mPrepaidCardRepository);

        final Transfer transfer = new Transfer.Builder()
                .token("trf-transfer-token")
                .status(Transfer.TransferStatuses.QUOTED)
                .createdOn(new Date())
                .clientTransferID("ClientId1234122")
                .sourceToken("usr-token-source")
                .sourceCurrency("CAD")
                .destinationToken("trm-bank-token")
                .destinationAmount("123.23")
                .destinationCurrency("CAD")
                .memo("Create quote test notes")
                .build();

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferRepository.CreateTransferCallback callback = invocation.getArgument(1);
                callback.onTransferCreated(transfer);
                return callback;
            }
        }).when(mTransferRepository).createTransfer(any(Transfer.class),
                any(TransferRepository.CreateTransferCallback.class));

        // test
        CreateTransferViewModel viewModel = spy(factory.create(CreateTransferViewModel.class));
        doReturn(ProgramModel.WALLET_MODEL).when(viewModel).getProgramModel();
        viewModel.init("0");
        viewModel.setGroupSeparator(",");
        viewModel.setDecimalSeparator(".");
        viewModel.setTransferAmount("20.25");
        viewModel.retry();

        verify(mUserRepository, times(1)).loadUser(any(UserRepository.LoadUserCallback.class));
        verify(mTransferMethodRepository, times(1)).loadLatestTransferMethod(any(TransferMethodRepository
                .LoadTransferMethodCallback.class));
        verify(mTransferRepository, times(2)).createTransfer(any(Transfer.class),
                any(TransferRepository.CreateTransferCallback.class));
    }

    @Test
    public void testRetry_isTransferAmountKnown_cardModel() {
        CreateTransferViewModel.CreateTransferViewModelFactory factory =
                new CreateTransferViewModel.CreateTransferViewModelFactory(mTransferRepository,
                        mTransferMethodRepository, mUserRepository, mPrepaidCardRepository);

        final Transfer transfer = new Transfer.Builder()
                .token("trf-transfer-token")
                .status(Transfer.TransferStatuses.QUOTED)
                .createdOn(new Date())
                .clientTransferID("ClientId1234122")
                .sourceToken("usr-token-source")
                .sourceCurrency("CAD")
                .destinationToken("trm-bank-token")
                .destinationAmount("123.23")
                .destinationCurrency("CAD")
                .memo("Create quote test notes")
                .build();

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferRepository.CreateTransferCallback callback = invocation.getArgument(1);
                callback.onTransferCreated(transfer);
                return callback;
            }
        }).when(mTransferRepository).createTransfer(any(Transfer.class),
                any(TransferRepository.CreateTransferCallback.class));

        // test
        CreateTransferViewModel viewModel = spy(factory.create(CreateTransferViewModel.class));
        doReturn(ProgramModel.CARD_ONLY_MODEL).when(viewModel).getProgramModel();
        viewModel.init("0");
        viewModel.setTransferAmount("20.25");
        viewModel.retry();

        verify(mTransferMethodRepository, times(1)).loadLatestTransferMethod(any(TransferMethodRepository
                .LoadTransferMethodCallback.class));
        verify(mTransferRepository, times(2)).createTransfer(any(Transfer.class),
                any(TransferRepository.CreateTransferCallback.class));
    }

    @Test
    public void testNotifyModuleUnavailable_hasModuleUnavailableError() {
        CreateTransferActivity activity = Robolectric.buildActivity(CreateTransferActivity.class).setup().get();
        CreateTransferViewModel viewModel = spy(ViewModelProviders.of(activity).get(CreateTransferViewModel.class));

        // test
        viewModel.notifyModuleUnavailable();

        assertThat(viewModel.getModuleUnavailableError().getValue(), is(notNullValue()));
        assertThat(viewModel.getModuleUnavailableError().getValue().getContent().getErrors(),
                Matchers.<Error>hasSize(1));
        assertThat(viewModel.getModuleUnavailableError().getValue().getContent().getErrors().get(0).getCode(),
                is(ERROR_SDK_MODULE_UNAVAILABLE));
    }

    @Test
    public void testCreateTransferViewModelFactory_createCreateTransferViewModelUnSuccessful() {
        CreateTransferViewModel.CreateTransferViewModelFactory factory =
                new CreateTransferViewModel.CreateTransferViewModelFactory(
                        TransferRepositoryFactory.getInstance().getTransferRepository(),
                        TransferMethodRepositoryFactory.getInstance().getTransferMethodRepository(),
                        UserRepositoryFactory.getInstance().getUserRepository(),
                        PrepaidCardRepositoryFactory.getInstance().getPrepaidCardRepository());
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
                        UserRepositoryFactory.getInstance().getUserRepository(),
                        PrepaidCardRepositoryFactory.getInstance().getPrepaidCardRepository());
        CreateTransferViewModel viewModel = factory.create(CreateTransferViewModel.class);
        assertThat(viewModel, is(notNullValue()));
    }


    class FakeModel extends ViewModel {
    }

    @Test
    public void testRefresh_callsRefreshWithQuote() {
        CreateTransferViewModel.CreateTransferViewModelFactory factory =
                new CreateTransferViewModel.CreateTransferViewModelFactory(mTransferRepository,
                        mTransferMethodRepository, mUserRepository, mPrepaidCardRepository);

        // test
        CreateTransferViewModel viewModel = spy(factory.create(CreateTransferViewModel.class));
        doReturn(ProgramModel.CARD_ONLY_MODEL).when(viewModel).getProgramModel();
        viewModel.refresh("0");

        verify(mTransferRepository, times(1)).createTransfer(any(Transfer.class),
                any(TransferRepository.CreateTransferCallback.class));
    }

    @Test
    public void testRefresh_callsRefreshWithoutQuote() {
        CreateTransferViewModel.CreateTransferViewModelFactory factory =
                new CreateTransferViewModel.CreateTransferViewModelFactory(mTransferRepository,
                        mTransferMethodRepository, mUserRepository, mPrepaidCardRepository);

        // test
        CreateTransferViewModel viewModel = factory.create(CreateTransferViewModel.class);
        viewModel.refresh("0");

        assertThat(viewModel, is(notNullValue()));
        assertThat(viewModel.isTransferAllAvailableFunds().getValue(), is(false));
        verify(mTransferRepository, times(1)).createTransfer(any(Transfer.class),
                any(TransferRepository.CreateTransferCallback.class));
    }

    @Test
    public void testSortingPrepaidCard() throws JSONException, HyperwalletException {
        CreateTransferViewModel.CreateTransferViewModelFactory factory =
                new CreateTransferViewModel.CreateTransferViewModelFactory(mTransferRepository,
                        mTransferMethodRepository, mUserRepository, mPrepaidCardRepository);

        // test
        CreateTransferViewModel viewModel = factory.create(CreateTransferViewModel.class);
        viewModel.sortPrepaidCard(mPrepaidCardList);
        assertThat(mPrepaidCardList.get(0).getPrimaryCardToken(), is(nullValue()));
        assertThat(mPrepaidCardList.get(1).getPrimaryCardToken(), is("trm-fake-token"));

        String responseJson = mResourceManager.getResourceContent("prepaid_cards_primary_card_response.json");
        JSONObject jsonObject = new JSONObject(responseJson);
        final PageList<PrepaidCard> prepaidCardList = new PageList<>(jsonObject, PrepaidCard.class);
        List<PrepaidCard> prepaidCards = prepaidCardList.getDataList();

        // test
        viewModel.sortPrepaidCard(prepaidCards);
        assertThat(prepaidCards.get(0).getPrimaryCardToken(), is(nullValue()));
        assertThat(prepaidCards.get(1).getPrimaryCardToken(), is("trm-fake-token"));
    }

    @Test
    public void testTransferDestinationWithOutPPc() {
        CreateTransferViewModel.CreateTransferViewModelFactory factory =
                new CreateTransferViewModel.CreateTransferViewModelFactory(mTransferRepository,
                        mTransferMethodRepository, mUserRepository, mPrepaidCardRepository);

        // test
        CreateTransferViewModel viewModel = factory.create(CreateTransferViewModel.class);
        viewModel.loadTransferDestinationListWithoutPPC("trm-fake-token");
        assertThat(viewModel.getTransferDestination().getValue().getField(TYPE), is(BANK_ACCOUNT));

    }


    @Test
    public void testNotifySourceUnavailable_hasSourceUnavailableError() {
        CreateTransferActivity activity = Robolectric.buildActivity(CreateTransferActivity.class).setup().get();
        CreateTransferViewModel viewModel = spy(ViewModelProviders.of(activity).get(CreateTransferViewModel.class));

        // test
        viewModel.notifySourceUnavailable();

        assertThat(viewModel.getModuleUnavailableError().getValue(), is(notNullValue()));
        assertThat(viewModel.getModuleUnavailableError().getValue().getContent().getErrors(),
                Matchers.<Error>hasSize(1));
        assertThat(viewModel.getModuleUnavailableError().getValue().getContent().getErrors().get(0).getCode(),
                is(ERROR_SDK_MODULE_UNAVAILABLE));
    }

    @Test
    public void testTransferDestinationWithOutPPc_hasSucessWithEmpty() {
        CreateTransferViewModel.CreateTransferViewModelFactory factory =
                new CreateTransferViewModel.CreateTransferViewModelFactory(mTransferRepository,
                        mTransferMethodRepository, mUserRepository, mPrepaidCardRepository);

        // test
        CreateTransferViewModel viewModel = factory.create(CreateTransferViewModel.class);
        viewModel.loadTransferDestinationListWithoutPPC("trm-fake-token");
        assertThat(viewModel.getTransferDestination().getValue().getField(TYPE), is(BANK_ACCOUNT));

    }

    @Test
    public void testRetry_isTransferSourceTokenknown() {
        CreateTransferViewModel.CreateTransferViewModelFactory factory =
                new CreateTransferViewModel.CreateTransferViewModelFactory("usr-fake", mTransferRepository,
                        mTransferMethodRepository, mUserRepository, mPrepaidCardRepository);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                UserRepository.LoadUserCallback callback = invocation.getArgument(0);
                callback.onError(null);
                return callback;
            }
        }).when(mUserRepository).loadUser(any(UserRepository.LoadUserCallback.class));

        // test
        CreateTransferViewModel viewModel = spy(factory.create(CreateTransferViewModel.class));
        doReturn(ProgramModel.WALLET_MODEL).when(viewModel).getProgramModel();
        viewModel.init("0");
        viewModel.retry();

        verify(mUserRepository, times(1)).loadUser(any(UserRepository.LoadUserCallback.class));
    }

    @Test
    public void testRefreshTransferDestination_WithFunding() {
        CreateTransferViewModel.CreateTransferViewModelFactory factory =
                new CreateTransferViewModel.CreateTransferViewModelFactory("usr-fake", mTransferRepository,
                        mTransferMethodRepository, mUserRepository, mPrepaidCardRepository);
        CreateTransferViewModel viewModel = spy(factory.create(CreateTransferViewModel.class));
        viewModel.refreshTransferDestination();
        verify(mTransferMethodRepository, times(1)).loadLatestTransferMethod(any(TransferMethodRepository
                .LoadTransferMethodCallback.class));

    }

    @Test
    public void testRefreshTransferDestination_WithoutFunding() {
        CreateTransferViewModel.CreateTransferViewModelFactory factory =
                new CreateTransferViewModel.CreateTransferViewModelFactory(mTransferRepository,
                        mTransferMethodRepository, mUserRepository, mPrepaidCardRepository);
        CreateTransferViewModel viewModel = spy(factory.create(CreateTransferViewModel.class));
        doReturn(ProgramModel.WALLET_MODEL).when(viewModel).getProgramModel();
        viewModel.refreshTransferDestination();
        verify(mUserRepository, times(1)).loadUser(any(UserRepository.LoadUserCallback.class));
        verify(mTransferMethodRepository, times(1)).loadLatestTransferMethod(any(TransferMethodRepository
                .LoadTransferMethodCallback.class));
        verify(mPrepaidCardRepository, times(1)).loadPrepaidCards(
                any(PrepaidCardRepository.LoadPrepaidCardsCallback.class));
    }

    @Test
    public void testRemovedPrePaidCardFromTransferMethodList() {
        CreateTransferViewModel viewModel = spy(new CreateTransferViewModel.CreateTransferViewModelFactory(
                mTransfer.getToken(), mTransferRepository, mTransferMethodRepository, mUserRepository,
                mPrepaidCardRepository
        ).create(CreateTransferViewModel.class));
        viewModel.init("0");
        TransferSource source = new TransferSource();
        source.setType(PREPAID_CARD);
        viewModel.setSelectedTransferSource(source);
        doReturn(ProgramModel.WALLET_MODEL).when(viewModel).getProgramModel();
        verify(viewModel, never()).loadTransferSource();
        assertThat(viewModel.getProgramModel(), is(ProgramModel.WALLET_MODEL));
        assertThat(viewModel.getTransferSelectedSource().getValue().getType(), is(PREPAID_CARD));
    }

    @Test
    public void testCreateTransferViewModel_prepaidCardValue() {
        CreateTransferViewModel viewModel = spy(new CreateTransferViewModel.CreateTransferViewModelFactory(
                "trm-ppc-token", mTransferRepository, mTransferMethodRepository, mUserRepository,
                mPrepaidCardRepository
        ).create(CreateTransferViewModel.class));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                PrepaidCardRepository.LoadPrepaidCardCallback callback = invocation.getArgument(1);
                callback.onPrepaidCardLoaded(mPrepaidCard);
                return callback;
            }
        }).when(mPrepaidCardRepository).loadPrepaidCard(ArgumentMatchers.any(String.class), ArgumentMatchers.any(
                PrepaidCardRepository.LoadPrepaidCardCallback.class));


        viewModel.init("0");

        assertThat(viewModel.getTransferSelectedSource().getValue(), is(notNullValue()));
        assertThat(viewModel.getTransferSources().getValue(), is(notNullValue()));
        verify(mPrepaidCardRepository, times(1)).loadPrepaidCard(any(String.class),
                any(PrepaidCardRepository.LoadPrepaidCardCallback.class));
    }

    @Test
    public void testCreateTransferViewModel_prepaidCardValue_null() {
        CreateTransferViewModel viewModel = spy(new CreateTransferViewModel.CreateTransferViewModelFactory(
                "trm-ppc-token", mTransferRepository, mTransferMethodRepository, mUserRepository,
                mPrepaidCardRepository
        ).create(CreateTransferViewModel.class));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                PrepaidCardRepository.LoadPrepaidCardCallback callback = invocation.getArgument(1);
                callback.onPrepaidCardLoaded(null);
                return callback;
            }
        }).when(mPrepaidCardRepository).loadPrepaidCard(ArgumentMatchers.any(String.class), ArgumentMatchers.any(
                PrepaidCardRepository.LoadPrepaidCardCallback.class));


        viewModel.init("0");

        assertThat(viewModel.getTransferSelectedSource().getValue(), is(nullValue()));
        assertThat(viewModel.getTransferSources().getValue(), is(new ArrayList<TransferSource>()));
        verify(mPrepaidCardRepository, times(1)).loadPrepaidCard(any(String.class),
                any(PrepaidCardRepository.LoadPrepaidCardCallback.class));
    }

    @Test
    public void testCreateTransferViewModel_prepaidCardValue_error() {
        CreateTransferViewModel viewModel = spy(new CreateTransferViewModel.CreateTransferViewModelFactory(
                "trm-ppc-token", mTransferRepository, mTransferMethodRepository, mUserRepository,
                mPrepaidCardRepository
        ).create(CreateTransferViewModel.class));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                PrepaidCardRepository.LoadPrepaidCardCallback callback = invocation.getArgument(1);
                callback.onError(Errors.getEmptyInstance());
                return callback;
            }
        }).when(mPrepaidCardRepository).loadPrepaidCard(ArgumentMatchers.any(String.class), ArgumentMatchers.any(
                PrepaidCardRepository.LoadPrepaidCardCallback.class));

        viewModel.init("0");

        verify(mPrepaidCardRepository, times(1)).loadPrepaidCard(any(String.class),
                any(PrepaidCardRepository.LoadPrepaidCardCallback.class));
    }

    @Test
    public void testProgramModel() {
        CreateTransferViewModel viewModel = spy(
                new CreateTransferViewModel.CreateTransferViewModelFactory(mTransferRepository,
                        mTransferMethodRepository, mUserRepository,
                        mPrepaidCardRepository
                ).create(CreateTransferViewModel.class));
        doReturn(mHyperwallet).when(viewModel).getHyperwallet();
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArgument(0);
                listener.onSuccess(mConfiguration);
                return listener;
            }
        }).when(mHyperwallet).getConfiguration(any(HyperwalletListener.class));

        viewModel.getProgramModel();
        MatcherAssert.assertThat(mConfiguration, Matchers.is(Matchers.notNullValue()));
        MatcherAssert.assertThat(mConfiguration.getProgramModel(), Matchers.is("WALLET_MODEL"));
        assertThat(viewModel.getProgramModel(), is(notNullValue()));

    }
}
