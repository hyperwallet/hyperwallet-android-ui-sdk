package com.hyperwallet.android.ui.repository;


import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.exception.HyperwalletException;
import com.hyperwallet.android.listener.HyperwalletListener;
import com.hyperwallet.android.model.HyperwalletError;
import com.hyperwallet.android.model.HyperwalletErrors;
import com.hyperwallet.android.model.TypeReference;
import com.hyperwallet.android.model.meta.HyperwalletField;
import com.hyperwallet.android.model.meta.HyperwalletTransferMethodConfigurationFieldResult;
import com.hyperwallet.android.model.meta.HyperwalletTransferMethodConfigurationKeyResult;
import com.hyperwallet.android.model.meta.TransferMethodConfigurationResult;
import com.hyperwallet.android.model.meta.query.HyperwalletTransferMethodConfigurationFieldQuery;
import com.hyperwallet.android.model.meta.query.HyperwalletTransferMethodConfigurationKeysQuery;
import com.hyperwallet.android.ui.rule.HyperwalletExternalResourceManager;
import com.hyperwallet.android.util.JsonUtils;

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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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
    private ArgumentCaptor<HyperwalletTransferMethodConfigurationKeyResult> keyResultArgumentCaptor;
    @Captor
    private ArgumentCaptor<HyperwalletTransferMethodConfigurationFieldResult> fieldResultArgumentCaptor;
    @Captor
    private ArgumentCaptor<HyperwalletErrors> mErrorsArgumentCaptor;
    @Mock
    private Hyperwallet mHyperwallet;
    @Mock
    private HashMap<FieldMapKey, HyperwalletTransferMethodConfigurationFieldResult> mFieldsMap;
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
        final HyperwalletTransferMethodConfigurationKeyResult result = JsonUtils.fromJsonString(responseBody,
                new TypeReference<TransferMethodConfigurationResult>() {
                });
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[1];
                listener.onSuccess(result);
                return listener;
            }
        }).when(mHyperwallet).retrieveTransferMethodConfigurationKeys(
                ArgumentMatchers.<HyperwalletTransferMethodConfigurationKeysQuery>any(),
                ArgumentMatchers.<HyperwalletListener<HyperwalletTransferMethodConfigurationKeyResult>>any());

        mTransferMethodConfigurationRepositoryImplMock.getKeys(loadKeysCallback);

        verify(loadKeysCallback).onKeysLoaded(keyResultArgumentCaptor.capture());
        verify(loadKeysCallback, never()).onError(any(HyperwalletErrors.class));

        HyperwalletTransferMethodConfigurationKeyResult transferMethodConfigurationKeyResult =
                keyResultArgumentCaptor.getValue();
        assertNotNull(transferMethodConfigurationKeyResult);
        List<String> countriesReturned = transferMethodConfigurationKeyResult.getCountries();
        assertNotNull(countriesReturned);
        assertThat(countriesReturned, is(Arrays.asList("CA", "US")));
        List<String> currenciesReturned = transferMethodConfigurationKeyResult.getCurrencies("US");
        assertNotNull(currenciesReturned);
        assertThat(currenciesReturned, is(Arrays.asList("USD")));
    }

    @Test
    public void testGetKeys_callsListenerWithErrorOnFailure()
            throws NoSuchMethodException, InstantiationException, IllegalAccessException,
            JSONException, InvocationTargetException {
        String responseBody = externalResourceManager.getResourceContent("error_tmc_keys_response.json");
        final HyperwalletErrors errors = JsonUtils.fromJsonString(responseBody,
                new TypeReference<HyperwalletErrors>() {
                });
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[1];
                listener.onFailure(new HyperwalletException(errors));
                return listener;
            }
        }).when(mHyperwallet).retrieveTransferMethodConfigurationKeys(
                ArgumentMatchers.<HyperwalletTransferMethodConfigurationKeysQuery>any(),
                ArgumentMatchers.<HyperwalletListener<HyperwalletTransferMethodConfigurationKeyResult>>any());

        mTransferMethodConfigurationRepositoryImplMock.getKeys(loadKeysCallback);

        verify(loadKeysCallback, never()).onKeysLoaded(any(HyperwalletTransferMethodConfigurationKeyResult.class));
        verify(loadKeysCallback).onError(mErrorsArgumentCaptor.capture());

        HyperwalletErrors hyperwalletErrors = mErrorsArgumentCaptor.getValue();
        assertNotNull(hyperwalletErrors);
        assertNotNull(hyperwalletErrors.getErrors());
        HyperwalletError inError = hyperwalletErrors.getErrors().get(0);
        assertThat(errors.getErrors().get(0), is(inError));
    }

    @Test
    public void testGetFields_callsListenerWithFieldResultOnSuccess()
            throws NoSuchMethodException, InstantiationException, IllegalAccessException, JSONException,
            InvocationTargetException {
        String responseBody = externalResourceManager.getResourceContent("successful_tmc_fields_response.json");
        final HyperwalletTransferMethodConfigurationFieldResult result = JsonUtils.fromJsonString(responseBody,
                new TypeReference<TransferMethodConfigurationResult>() {
                });
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[1];
                listener.onSuccess(result);
                return listener;
            }
        }).when(mHyperwallet).retrieveTransferMethodConfigurationFields(
                ArgumentMatchers.<HyperwalletTransferMethodConfigurationFieldQuery>any(),
                ArgumentMatchers.<HyperwalletListener<HyperwalletTransferMethodConfigurationFieldResult>>any());

        mTransferMethodConfigurationRepositoryImplMock.getFields(COUNTRY, CURRENCY, TRANSFER_METHOD_TYPE,
                loadFieldsCallback);

        verify(loadFieldsCallback).onFieldsLoaded(fieldResultArgumentCaptor.capture());
        verify(loadFieldsCallback, never()).onError(any(HyperwalletErrors.class));

        HyperwalletTransferMethodConfigurationFieldResult transferMethodConfigurationFieldResult =
                fieldResultArgumentCaptor.getValue();
        assertNotNull(transferMethodConfigurationFieldResult);
        List<HyperwalletField> fields = transferMethodConfigurationFieldResult.getFields();
        assertNotNull(fields);
        assertThat(fields.size(), is(3));
    }

    @Test
    public void testGetFields_callsListenerWithErrorResultOnFailure()
            throws NoSuchMethodException, InstantiationException, IllegalAccessException, JSONException,
            InvocationTargetException {
        String responseBody = externalResourceManager.getResourceContent("error_tmc_keys_response.json");
        final HyperwalletErrors errors = JsonUtils.fromJsonString(responseBody,
                new TypeReference<HyperwalletErrors>() {
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
                ArgumentMatchers.<HyperwalletTransferMethodConfigurationFieldQuery>any(),
                ArgumentMatchers.<HyperwalletListener<HyperwalletTransferMethodConfigurationFieldResult>>any());


        mTransferMethodConfigurationRepositoryImplMock.getFields(COUNTRY, CURRENCY, TRANSFER_METHOD_TYPE,
                loadFieldsCallback);

        verify(loadFieldsCallback, never()).onFieldsLoaded(
                any(HyperwalletTransferMethodConfigurationFieldResult.class));
        verify(loadFieldsCallback).onError(mErrorsArgumentCaptor.capture());

        HyperwalletErrors hyperwalletErrors = mErrorsArgumentCaptor.getValue();
        assertNotNull(hyperwalletErrors);
        List<HyperwalletError> inErrors = hyperwalletErrors.getErrors();
        assertNotNull(errors);
        HyperwalletError inError = inErrors.get(0);
        assertThat(errors.getErrors().get(0), is(inError));
    }

    @Test
    public void testGetKeys_callsListenerWithKeyResultFromCacheWhenNotNull() throws Exception {
        String responseBody = externalResourceManager.getResourceContent("successful_tmc_keys_response.json");
        final TransferMethodConfigurationResult transferMethodConfigurationResult = JsonUtils.fromJsonString(
                responseBody,
                new TypeReference<TransferMethodConfigurationResult>() {
                });
        TransferMethodConfigurationRepositoryImpl repositoryWithCache = spy(
                new TransferMethodConfigurationRepositoryImpl(null, transferMethodConfigurationResult,
                        new HashMap<FieldMapKey,
                                HyperwalletTransferMethodConfigurationFieldResult>()));
        repositoryWithCache.getKeys(loadKeysCallback);

        verify(repositoryWithCache, never()).getTransferMethodConfigurationKeyResult(
                any(TransferMethodConfigurationRepository.LoadKeysCallback.class));
        verify(loadKeysCallback).onKeysLoaded(keyResultArgumentCaptor.capture());
        verify(loadKeysCallback, never()).onError(any(HyperwalletErrors.class));
    }

    @Test
    public void testGetFields_callsListenerWithFieldResultFromCacheWhenNotNull() throws Exception {
        String responseBody = externalResourceManager.getResourceContent("successful_tmc_fields_response.json");
        final TransferMethodConfigurationResult result = JsonUtils.fromJsonString(responseBody,
                new TypeReference<TransferMethodConfigurationResult>() {
                });

        FieldMapKey fieldMapKey = new FieldMapKey(COUNTRY, CURRENCY, TRANSFER_METHOD_TYPE);
        when(mFieldsMap.get(fieldMapKey)).thenReturn(result);

        mTransferMethodConfigurationRepositoryImplMock.getFields(COUNTRY, CURRENCY, TRANSFER_METHOD_TYPE,
                loadFieldsCallback);

        verify(mTransferMethodConfigurationRepositoryImplMock, never()).getTransferMethodConfigurationFieldResult(
                any(String.class),
                any(String.class),
                any(String.class),
                any(TransferMethodConfigurationRepository.LoadFieldsCallback.class));
        verify(loadFieldsCallback).onFieldsLoaded(fieldResultArgumentCaptor.capture());
        verify(loadFieldsCallback, never()).onError(any(HyperwalletErrors.class));
    }

    @Test
    public void testRefreshFields_clearsFieldMapWhenNotEmpty() throws Exception {
        String responseBody = externalResourceManager.getResourceContent("successful_tmc_fields_response.json");
        JSONObject jsonObject = new JSONObject(responseBody);
        FieldMapKey fieldMapKey = new FieldMapKey(COUNTRY, CURRENCY, TRANSFER_METHOD_TYPE);
        HashMap<FieldMapKey, HyperwalletTransferMethodConfigurationFieldResult> fieldMap = new HashMap<>();
        fieldMap.put(fieldMapKey, new TransferMethodConfigurationResult(jsonObject));
        TransferMethodConfigurationRepositoryImpl repositoryWithCache = new TransferMethodConfigurationRepositoryImpl(
                null, null, fieldMap);
        repositoryWithCache.refreshFields();
        assertTrue(fieldMap.isEmpty());
    }
}
