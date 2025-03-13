package com.hyperwallet.android.ui.transfermethod.repository;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static com.hyperwallet.android.model.user.User.ProfileTypes.INDIVIDUAL;

import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.exception.HyperwalletException;
import com.hyperwallet.android.listener.HyperwalletListener;
import com.hyperwallet.android.model.Error;
import com.hyperwallet.android.model.Errors;
import com.hyperwallet.android.model.TypeReference;
import com.hyperwallet.android.model.graphql.HyperwalletTransferMethodConfigurationField;
import com.hyperwallet.android.model.graphql.HyperwalletTransferMethodConfigurationKey;
import com.hyperwallet.android.model.graphql.field.FieldGroup;
import com.hyperwallet.android.model.graphql.field.TransferMethodConfiguration;
import com.hyperwallet.android.model.graphql.field.TransferMethodConfigurationFieldResult;
import com.hyperwallet.android.model.graphql.keyed.Country;
import com.hyperwallet.android.model.graphql.keyed.Currency;
import com.hyperwallet.android.model.graphql.keyed.TransferMethodConfigurationKeyResult;
import com.hyperwallet.android.model.graphql.query.TransferMethodConfigurationFieldQuery;
import com.hyperwallet.android.model.graphql.query.TransferMethodConfigurationKeysQuery;
import com.hyperwallet.android.ui.testutils.rule.HyperwalletExternalResourceManager;
import com.hyperwallet.android.util.JsonUtils;

import org.hamcrest.collection.IsEmptyCollection;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@RunWith(RobolectricTestRunner.class)
public class TransferMethodConfigurationRepositoryImplTest {
    private static final String COUNTRY = "COUNTRY";
    private static final String CURRENCY = "USD";
    private static final String TRANSFER_METHOD_TYPE = "BANK_ACCOUNT";
    @Rule
    public HyperwalletExternalResourceManager externalResourceManager = new HyperwalletExternalResourceManager();
    @Rule
    public MockitoRule mockito = MockitoJUnit.rule();
    @Mock
    private TransferMethodConfigurationRepository.LoadKeysCallback loadKeysCallback;
    @Mock
    private TransferMethodConfigurationRepository.LoadFieldsCallback loadFieldsCallback;
    @Captor
    private ArgumentCaptor<HyperwalletTransferMethodConfigurationKey> keyResultArgumentCaptor;
    @Captor
    private ArgumentCaptor<HyperwalletTransferMethodConfigurationField> fieldResultArgumentCaptor;
    @Captor
    private ArgumentCaptor<Errors> mErrorsArgumentCaptor;
    @Mock
    private Hyperwallet mHyperwallet;
    @Mock
    private HashMap<FieldMapKey, HyperwalletTransferMethodConfigurationField> mFieldsMap;
    @Spy
    @InjectMocks
    private TransferMethodConfigurationRepositoryImpl mTransferMethodConfigurationRepositoryImplMock;

    @Before
    public void setUp() {
        doReturn(mHyperwallet).when(mTransferMethodConfigurationRepositoryImplMock).getHyperwallet();
    }

    @Test
    public void testGetKeys_callsListenerWithKeyResultOnSuccess()
            throws NoSuchMethodException, InstantiationException, IllegalAccessException,
            JSONException, InvocationTargetException {
        String responseBody = externalResourceManager.getResourceContent("successful_tmc_keys_response.json");
        final HyperwalletTransferMethodConfigurationKey result = JsonUtils.fromJsonString(responseBody,
                new TypeReference<TransferMethodConfigurationKeyResult>() {
                });
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[1];
                listener.onSuccess(result);
                return listener;
            }
        }).when(mHyperwallet).retrieveTransferMethodConfigurationKeys(
                ArgumentMatchers.<TransferMethodConfigurationKeysQuery>any(),
                ArgumentMatchers.<HyperwalletListener<HyperwalletTransferMethodConfigurationKey>>any());

        mTransferMethodConfigurationRepositoryImplMock.getKeys(loadKeysCallback);

        verify(loadKeysCallback).onKeysLoaded(keyResultArgumentCaptor.capture());
        verify(loadKeysCallback, never()).onError(any(Errors.class));

        HyperwalletTransferMethodConfigurationKey transferMethodConfigurationKeyResult =
                keyResultArgumentCaptor.getValue();
        assertNotNull(transferMethodConfigurationKeyResult);
        Set<Country> countriesReturned = transferMethodConfigurationKeyResult.getCountries();
        assertNotNull(countriesReturned);
        Set<Currency> currenciesReturned = transferMethodConfigurationKeyResult.getCurrencies("US");
        assertNotNull(currenciesReturned);
    }

    @Test
    public void testGetKeys_callsListenerWithErrorOnFailure()
            throws NoSuchMethodException, InstantiationException, IllegalAccessException,
            JSONException, InvocationTargetException {
        String responseBody = externalResourceManager.getResourceContent("error_tmc_keys_response.json");
        final Errors errors = JsonUtils.fromJsonString(responseBody,
                new TypeReference<Errors>() {
                });
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[1];
                listener.onFailure(new HyperwalletException(errors));
                return listener;
            }
        }).when(mHyperwallet).retrieveTransferMethodConfigurationKeys(
                ArgumentMatchers.<TransferMethodConfigurationKeysQuery>any(),
                ArgumentMatchers.<HyperwalletListener<HyperwalletTransferMethodConfigurationKey>>any());

        mTransferMethodConfigurationRepositoryImplMock.getKeys(loadKeysCallback);

        verify(loadKeysCallback, never()).onKeysLoaded(any(HyperwalletTransferMethodConfigurationKey.class));
        verify(loadKeysCallback).onError(mErrorsArgumentCaptor.capture());

        Errors errorsList = mErrorsArgumentCaptor.getValue();
        assertNotNull(errorsList);
        assertNotNull(errorsList.getErrors());
        Error inError = errorsList.getErrors().get(0);
        assertThat(errors.getErrors().get(0), is(inError));
    }

    @Test
    public void testGetFields_callsListenerWithFieldResultOnSuccess()
            throws NoSuchMethodException, InstantiationException, IllegalAccessException, JSONException,
            InvocationTargetException {
        String responseBody = externalResourceManager.getResourceContent(
                "successful_tmc_fields_bank_account_response.json");
        final TransferMethodConfigurationFieldResult result = JsonUtils.fromJsonString(responseBody,
                new TypeReference<TransferMethodConfigurationFieldResult>() {
                });
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[1];
                listener.onSuccess(result);
                return listener;
            }
        }).when(mHyperwallet).retrieveTransferMethodConfigurationFields(
                ArgumentMatchers.<TransferMethodConfigurationFieldQuery>any(),
                ArgumentMatchers.<HyperwalletListener<HyperwalletTransferMethodConfigurationField>>any());

        mTransferMethodConfigurationRepositoryImplMock.getFields(COUNTRY, CURRENCY, TRANSFER_METHOD_TYPE, INDIVIDUAL,
                loadFieldsCallback);

        verify(loadFieldsCallback).onFieldsLoaded(fieldResultArgumentCaptor.capture());
        verify(loadFieldsCallback, never()).onError(any(Errors.class));

        HyperwalletTransferMethodConfigurationField transferMethodConfigurationFieldResult =
                fieldResultArgumentCaptor.getValue();
        assertNotNull(transferMethodConfigurationFieldResult);
        TransferMethodConfiguration transferMethodConfiguration =
                transferMethodConfigurationFieldResult.getFields();
        assertThat(transferMethodConfiguration, is(notNullValue()));
        assertThat(transferMethodConfiguration.getFieldGroups(),
                is(not(IsEmptyCollection.<FieldGroup>empty())));
    }

    @Test
    public void testGetFields_callsListenerWithErrorResultOnFailure()
            throws NoSuchMethodException, InstantiationException, IllegalAccessException, JSONException,
            InvocationTargetException {
        String responseBody = externalResourceManager.getResourceContent("error_tmc_keys_response.json");
        final Errors errors = JsonUtils.fromJsonString(responseBody,
                new TypeReference<Errors>() {
                });
        final HyperwalletException exception = new HyperwalletException(errors);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[1];

                listener.onFailure(exception);
                return listener;
            }
        }).when(mHyperwallet).retrieveTransferMethodConfigurationFields(
                ArgumentMatchers.<TransferMethodConfigurationFieldQuery>any(),
                ArgumentMatchers.<HyperwalletListener<HyperwalletTransferMethodConfigurationField>>any());


        mTransferMethodConfigurationRepositoryImplMock.getFields(COUNTRY, CURRENCY, TRANSFER_METHOD_TYPE, INDIVIDUAL,
                loadFieldsCallback);

        verify(loadFieldsCallback, never()).onFieldsLoaded(any(HyperwalletTransferMethodConfigurationField.class));
        verify(loadFieldsCallback).onError(mErrorsArgumentCaptor.capture());

        Errors errorsList = mErrorsArgumentCaptor.getValue();
        assertNotNull(errorsList);
        List<Error> inErrors = errorsList.getErrors();
        assertNotNull(errors);
        Error inError = inErrors.get(0);
        assertThat(errors.getErrors().get(0), is(inError));
    }

    @Test
    public void testGetKeys_callsListenerWithKeyResultFromCacheWhenNotNull() throws Exception {
        String responseBody = externalResourceManager.getResourceContent("successful_tmc_keys_response.json");
        final HyperwalletTransferMethodConfigurationKey transferMethodConfigurationResult = JsonUtils.fromJsonString(
                responseBody,
                new TypeReference<TransferMethodConfigurationKeyResult>() {
                });
        TransferMethodConfigurationRepositoryImpl repositoryWithCache = spy(
                new TransferMethodConfigurationRepositoryImpl(null, transferMethodConfigurationResult,
                        new HashMap<FieldMapKey,
                                HyperwalletTransferMethodConfigurationField>()));
        repositoryWithCache.getKeys(loadKeysCallback);

        verify(repositoryWithCache, never()).getTransferMethodConfigurationKeyResult(
                any(TransferMethodConfigurationRepository.LoadKeysCallback.class));
        verify(loadKeysCallback).onKeysLoaded(keyResultArgumentCaptor.capture());
        verify(loadKeysCallback, never()).onError(any(Errors.class));
    }

    @Test
    public void testGetFields_callsListenerWithFieldResultFromCacheWhenNotNull() throws Exception {
        String responseBody = externalResourceManager.getResourceContent(
                "successful_tmc_fields_bank_account_response.json");
        final TransferMethodConfigurationFieldResult result = JsonUtils.fromJsonString(responseBody,
                new TypeReference<TransferMethodConfigurationFieldResult>() {
                });

        FieldMapKey fieldMapKey = new FieldMapKey(COUNTRY, CURRENCY, TRANSFER_METHOD_TYPE);
        when(mFieldsMap.get(fieldMapKey)).thenReturn(result);

        mTransferMethodConfigurationRepositoryImplMock.getFields(COUNTRY, CURRENCY, TRANSFER_METHOD_TYPE, INDIVIDUAL,
                loadFieldsCallback);

        verify(mTransferMethodConfigurationRepositoryImplMock, never()).getTransferMethodConfigurationFieldResult(
                any(String.class),
                any(String.class),
                any(String.class),
                any(String.class),
                any(TransferMethodConfigurationRepository.LoadFieldsCallback.class));
        verify(loadFieldsCallback).onFieldsLoaded(fieldResultArgumentCaptor.capture());
        verify(loadFieldsCallback, never()).onError(any(Errors.class));
    }

    @Test
    public void testRefreshFields_clearsFieldMapWhenNotEmpty() throws Exception {
        String responseBody = externalResourceManager.getResourceContent(
                "successful_tmc_fields_bank_account_response.json");
        JSONObject jsonObject = new JSONObject(responseBody);
        FieldMapKey fieldMapKey = new FieldMapKey(COUNTRY, CURRENCY, TRANSFER_METHOD_TYPE);
        HashMap<FieldMapKey, HyperwalletTransferMethodConfigurationField> fieldMap = new HashMap<>();
        fieldMap.put(fieldMapKey, new TransferMethodConfigurationFieldResult(jsonObject));
        TransferMethodConfigurationRepositoryImpl repositoryWithCache = new TransferMethodConfigurationRepositoryImpl(
                null, null, fieldMap);
        repositoryWithCache.refreshFields();
        assertTrue(fieldMap.isEmpty());
    }
}
