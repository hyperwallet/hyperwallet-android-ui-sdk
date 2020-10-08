package com.hyperwallet.android.ui.transfermethod.repository;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import static com.hyperwallet.android.ExceptionMapper.EC_UNEXPECTED_EXCEPTION;
import static com.hyperwallet.android.model.StatusTransition.StatusDefinition.ACTIVATED;
import static com.hyperwallet.android.model.StatusTransition.StatusDefinition.DE_ACTIVATED;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.BANK_ACCOUNT_ID;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.BANK_NAME;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.STATUS;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.TOKEN;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.TRANSFER_METHOD_COUNTRY;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.TRANSFER_METHOD_CURRENCY;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.TYPE;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodTypes.PREPAID_CARD;
import static com.hyperwallet.android.util.HttpMethod.GET;

import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.exception.HyperwalletException;
import com.hyperwallet.android.listener.HyperwalletListener;
import com.hyperwallet.android.model.Error;
import com.hyperwallet.android.model.Errors;
import com.hyperwallet.android.model.StatusTransition;
import com.hyperwallet.android.model.TypeReference;
import com.hyperwallet.android.model.balance.PrepaidCardBalanceQueryParam;
import com.hyperwallet.android.model.graphql.keyed.TransferMethodConfigurationKeyResult;
import com.hyperwallet.android.model.graphql.query.TransferMethodConfigurationKeysQuery;
import com.hyperwallet.android.model.paging.PageList;
import com.hyperwallet.android.model.transfer.Transfer;
import com.hyperwallet.android.model.transfermethod.BankAccount;
import com.hyperwallet.android.model.transfermethod.BankCard;
import com.hyperwallet.android.model.transfermethod.PayPalAccount;
import com.hyperwallet.android.model.transfermethod.PrepaidCard;
import com.hyperwallet.android.model.transfermethod.PrepaidCardQueryParam;
import com.hyperwallet.android.model.transfermethod.TransferMethod;
import com.hyperwallet.android.model.transfermethod.TransferMethodQueryParam;
import com.hyperwallet.android.ui.testutils.rule.HyperwalletExternalResourceManager;
import com.hyperwallet.android.util.DateUtil;
import com.hyperwallet.android.util.JsonUtils;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;

import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.mockwebserver.RecordedRequest;

@RunWith(RobolectricTestRunner.class)
public class PrepaidCardRepositoryImplTest {

    @Rule
    public ExpectedException mThrown = ExpectedException.none();
    @Rule
    public MockitoRule mMockito = MockitoJUnit.rule();
    @Spy
    PrepaidCardRepositoryImpl mPrepaidCardRepository;
    @Mock
    private Hyperwallet mHyperwallet;
    @Rule
    public HyperwalletExternalResourceManager mResourceManager = new HyperwalletExternalResourceManager();
    @Mock
    private PrepaidCardRepository.LoadPrepaidCardsCallback mLoadPrepaidCardListCallback;
    @Mock
    private PrepaidCardRepository.LoadPrepaidCardCallback mLoadPrepaidCardCallback;
    @Captor
    private ArgumentCaptor<Errors> mErrorsArgumentCaptor;
    @Captor
    private ArgumentCaptor<PrepaidCard> mPrepaidCardArgumentCaptor;
    @Captor
    private ArgumentCaptor<List<PrepaidCard>> mPrepaidCardListArgumentCaptor;

    @Before
    public void setup() {
        doReturn(mHyperwallet).when(mPrepaidCardRepository).getHyperwallet();
    }

    @Test
    public void testGetPrepaidCard_returnsCard() throws JSONException {
        String responseJson = mResourceManager.getResourceContent("prepaid_card_response.json");
        JSONObject jsonObject = new JSONObject(responseJson);
        final PrepaidCard prepaidCard = new PrepaidCard(jsonObject);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = invocation.getArgument(1);
                listener.onSuccess(prepaidCard);
                return listener;
            }
        }).when(mHyperwallet).getPrepaidCard(ArgumentMatchers.<String>any(), any(HyperwalletListener.class));

        mPrepaidCardRepository.getPrepaidCard("trm-fake-token", mLoadPrepaidCardCallback);
        verify(mLoadPrepaidCardCallback).onPrepaidCardLoaded(mPrepaidCardArgumentCaptor.capture());
        verify(mLoadPrepaidCardCallback, never()).onError(any(Errors.class));

        PrepaidCard prepaidCardResponse = mPrepaidCardArgumentCaptor.getValue();
        assertThat(prepaidCardResponse, is(Matchers.notNullValue()));
        assertThat(prepaidCardResponse.getField(TransferMethod.TransferMethodFields.TOKEN),
                is("trm-fake-token"));
        assertThat(prepaidCardResponse.getType(), is("PREPAID_CARD"));
        assertThat(prepaidCardResponse.getStatus(), is("ACTIVATED"));
        assertThat(DateUtil.toDateTimeFormat(prepaidCardResponse.getCreatedOn()), is("2019-06-20T22:49:12"));
        assertThat(prepaidCardResponse.getTransferMethodCountry(), is("US"));
        assertThat(prepaidCardResponse.getTransferMethodCurrency(), is("USD"));
        assertThat(prepaidCardResponse.getCardType(), is("VIRTUAL"));
        assertThat(prepaidCardResponse.getCardPackage(), is("L1"));
        assertThat(prepaidCardResponse.getCardNumber(), is("************8766"));
        assertThat(prepaidCardResponse.getCardBrand(), is("VISA"));
        assertThat(prepaidCardResponse.getDateOfExpiry(), is("2023-06"));

    }

    @Test
    public void testGetPrepaidCard_error() {
        final Error returnedError = new Error("test message", "TEST_CODE");
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[1];

                List<Error> errorList = new ArrayList<>();
                errorList.add(returnedError);

                listener.onFailure(new HyperwalletException(new Errors(errorList)));
                return listener;
            }
        }).when(mHyperwallet).getPrepaidCard(ArgumentMatchers.<String>any(),
                any(HyperwalletListener.class));

        mPrepaidCardRepository.getPrepaidCard("trm-fake-token", mLoadPrepaidCardCallback);

        verify(mLoadPrepaidCardCallback, never()).onPrepaidCardLoaded(any(PrepaidCard.class));
        verify(mLoadPrepaidCardCallback).onError(mErrorsArgumentCaptor.capture());

        assertThat(mErrorsArgumentCaptor.getValue().getErrors(), hasItem(returnedError));
    }

    @Test
    public void testGetPrepaidCardList_returnsCard()
            throws JSONException, HyperwalletException {
        String responseJson = mResourceManager.getResourceContent("prepaid_cards_response.json");
        JSONObject jsonObject = new JSONObject(responseJson);
        final PageList<PrepaidCard> prepaidCardList = new PageList<>(jsonObject, PrepaidCard.class);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = invocation.getArgument(1);
                listener.onSuccess(prepaidCardList);
                return listener;
            }
        }).when(mHyperwallet).listPrepaidCards(ArgumentMatchers.<PrepaidCardQueryParam>any(),
                any(HyperwalletListener.class));
        mPrepaidCardRepository.loadPrepaidCards(mLoadPrepaidCardListCallback);
        verify(mPrepaidCardRepository).getHyperwallet();
        verify(mLoadPrepaidCardListCallback).onPrepaidCardListLoaded(mPrepaidCardListArgumentCaptor.capture());
        verify(mLoadPrepaidCardListCallback, never()).onError(any(Errors.class));

        List<PrepaidCard> prepaidCardResponse = mPrepaidCardListArgumentCaptor.getValue();

        PrepaidCard prepaidCard = prepaidCardResponse.get(0);
        assertThat(prepaidCard.getField(TOKEN), is("trm-fake-token"));
        assertThat(prepaidCard.getType(), is(PREPAID_CARD));
        assertThat(prepaidCard.getStatus(), is(ACTIVATED));
        assertThat(DateUtil.toDateTimeFormat(prepaidCard.getCreatedOn()), is("2019-06-20T22:49:12"));
        assertThat(prepaidCard.getTransferMethodCountry(), is("US"));
        assertThat(prepaidCard.getTransferMethodCurrency(), is("USD"));
        assertThat(prepaidCard.getCardType(), is("VIRTUAL"));
        assertThat(prepaidCard.getCardPackage(), is("L1"));
        assertThat(prepaidCard.getCardNumber(), is("************8766"));
        assertThat(prepaidCard.getCardBrand(), is("VISA"));
        assertThat(prepaidCard.getDateOfExpiry(), is("2023-06"));
        assertThat(prepaidCard.getField("verificationStatus"), is("VERIFIED"));

        PrepaidCard secondaryPrepaidCard = prepaidCardResponse.get(1);
        assertThat(secondaryPrepaidCard.getField(TOKEN), is("trm-fake-token2"));
        assertThat(secondaryPrepaidCard.getType(), is(PREPAID_CARD));
        assertThat(secondaryPrepaidCard.getStatus(), is(ACTIVATED));
        assertThat(DateUtil.toDateTimeFormat(secondaryPrepaidCard.getCreatedOn()), is("2019-06-20T22:59:12"));
        assertThat(secondaryPrepaidCard.getTransferMethodCountry(), is("US"));
        assertThat(secondaryPrepaidCard.getTransferMethodCurrency(), is("USD"));
        assertThat(secondaryPrepaidCard.getCardType(), is("VIRTUAL"));
        assertThat(secondaryPrepaidCard.getCardPackage(), is("L1"));
        assertThat(secondaryPrepaidCard.getCardNumber(), is("************8767"));
        assertThat(secondaryPrepaidCard.getCardBrand(), is("VISA"));
        assertThat(secondaryPrepaidCard.getDateOfExpiry(), is("2023-06"));
        assertThat(secondaryPrepaidCard.getPrimaryCardToken(), is("trm-fake-token"));
    }

    @Test
    public void testGetPrepaidCardList_error() {
        final Error returnedError = new Error("test message", "TEST_CODE");
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[1];

                List<Error> errorList = new ArrayList<>();
                errorList.add(returnedError);

                listener.onFailure(new HyperwalletException(new Errors(errorList)));
                return listener;
            }
        }).when(mHyperwallet).listPrepaidCards(ArgumentMatchers.<PrepaidCardQueryParam>any(),
                any(HyperwalletListener.class));

        mPrepaidCardRepository.loadPrepaidCards(mLoadPrepaidCardListCallback);

        verify(mLoadPrepaidCardListCallback, never()).onPrepaidCardListLoaded(
                (List<PrepaidCard>) any(PrepaidCard.class));
        verify(mLoadPrepaidCardListCallback).onError(mErrorsArgumentCaptor.capture());

        assertThat(mErrorsArgumentCaptor.getValue().getErrors(), hasItem(returnedError));
    }
}
