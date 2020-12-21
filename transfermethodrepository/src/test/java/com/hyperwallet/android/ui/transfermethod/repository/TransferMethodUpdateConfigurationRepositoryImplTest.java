package com.hyperwallet.android.ui.transfermethod.repository;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
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
import com.hyperwallet.android.model.Error;
import com.hyperwallet.android.model.Errors;
import com.hyperwallet.android.model.TypeReference;
import com.hyperwallet.android.model.graphql.HyperwalletTransferMethodConfigurationField;
import com.hyperwallet.android.model.graphql.field.FieldGroup;
import com.hyperwallet.android.model.graphql.field.TransferMethodConfiguration;
import com.hyperwallet.android.model.graphql.field.TransferMethodConfigurationFieldResult;
import com.hyperwallet.android.model.graphql.field.TransferMethodUpdateConfigurationFieldResult;
import com.hyperwallet.android.model.graphql.query.TransferMethodUpdateConfigurationFieldQuery;
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

@RunWith(RobolectricTestRunner.class)
public class TransferMethodUpdateConfigurationRepositoryImplTest {

    private static final String TRANSFER_TOKEN = "trm-fake";
    @Rule
    public HyperwalletExternalResourceManager externalResourceManager = new HyperwalletExternalResourceManager();
    @Rule
    public MockitoRule mockito = MockitoJUnit.rule();
    @Mock
    private TransferMethodUpdateConfigurationRepository.LoadFieldsCallback loadFieldsCallback;
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
    private TransferMethodUpdateConfigurationRepositoryImpl mTransferMethodUpdateConfigurationRepositoryImplMock;

    @Before
    public void setUp() {
        doReturn(mHyperwallet).when(mTransferMethodUpdateConfigurationRepositoryImplMock).getHyperwallet();
    }

    @Test
    public void testGetFields_callsListenerWithFieldResultOnSuccess()
            throws NoSuchMethodException, InstantiationException, IllegalAccessException, JSONException,
            InvocationTargetException {
        String responseBody = externalResourceManager.getResourceContent(
                "successful_tmc_update_field_bank_account_response.json");
        final TransferMethodUpdateConfigurationFieldResult result = JsonUtils.fromJsonString(responseBody,
                new TypeReference<TransferMethodUpdateConfigurationFieldResult>() {
                });
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[1];
                listener.onSuccess(result);
                return listener;
            }
        }).when(mHyperwallet).retrieveUpdateTransferMethodConfigurationFields(
                ArgumentMatchers.<TransferMethodUpdateConfigurationFieldQuery>any(),
                ArgumentMatchers.<HyperwalletListener<HyperwalletTransferMethodConfigurationField>>any());

        mTransferMethodUpdateConfigurationRepositoryImplMock.getFields(TRANSFER_TOKEN,loadFieldsCallback);

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
        }).when(mHyperwallet).retrieveUpdateTransferMethodConfigurationFields(
                ArgumentMatchers.<TransferMethodUpdateConfigurationFieldQuery>any(),
                ArgumentMatchers.<HyperwalletListener<HyperwalletTransferMethodConfigurationField>>any());


        mTransferMethodUpdateConfigurationRepositoryImplMock.getFields(TRANSFER_TOKEN, loadFieldsCallback);

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
    public void testGetFields_callsListenerWithFieldResultFromCacheWhenNotNull() throws Exception {
        String responseBody = externalResourceManager.getResourceContent(
                "successful_tmc_update_field_bank_account_response.json");
        final TransferMethodUpdateConfigurationFieldResult result = JsonUtils.fromJsonString(responseBody,
                new TypeReference<TransferMethodUpdateConfigurationFieldResult>() {
                });

        FieldMapKey fieldMapKey = new FieldMapKey();
        when(mFieldsMap.get(fieldMapKey)).thenReturn(result);

        mTransferMethodUpdateConfigurationRepositoryImplMock.getFields(TRANSFER_TOKEN, loadFieldsCallback);
        verify(loadFieldsCallback, never()).onError(any(Errors.class));
    }

    @Test
    public void testRefreshFields_clearsFieldMapWhenNotEmpty() throws Exception {
        String responseBody = externalResourceManager.getResourceContent(
                "successful_tmc_update_field_bank_account_response.json");
        JSONObject jsonObject = new JSONObject(responseBody);
        FieldMapKey fieldMapKey = new FieldMapKey();
        HashMap<FieldMapKey, HyperwalletTransferMethodConfigurationField> fieldMap = new HashMap<>();
        fieldMap.put(fieldMapKey, new TransferMethodUpdateConfigurationFieldResult(jsonObject));
        TransferMethodConfigurationRepositoryImpl repositoryWithCache = new TransferMethodConfigurationRepositoryImpl(
                null, null, fieldMap);
        repositoryWithCache.refreshFields();
        assertTrue(fieldMap.isEmpty());
    }
}
