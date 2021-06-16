package com.hyperwallet.android.ui.transfermethod;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import static com.hyperwallet.android.ExceptionMapper.EC_UNEXPECTED_EXCEPTION;

import com.hyperwallet.android.model.Error;
import com.hyperwallet.android.model.Errors;
import com.hyperwallet.android.model.graphql.HyperwalletTransferMethodConfigurationKey;
import com.hyperwallet.android.model.graphql.keyed.TransferMethodConfigurationKeyResult;
import com.hyperwallet.android.model.transfermethod.TransferMethod;
import com.hyperwallet.android.model.user.User;
import com.hyperwallet.android.ui.testutils.rule.HyperwalletExternalResourceManager;
import com.hyperwallet.android.ui.transfermethod.repository.TransferMethodConfigurationRepository;
import com.hyperwallet.android.ui.transfermethod.repository.TransferMethodConfigurationRepositoryImpl;
import com.hyperwallet.android.ui.transfermethod.view.SelectTransferMethodContract;
import com.hyperwallet.android.ui.transfermethod.view.SelectTransferMethodPresenter;
import com.hyperwallet.android.ui.transfermethod.view.TransferMethodSelectionItem;
import com.hyperwallet.android.ui.user.repository.UserRepository;
import com.hyperwallet.android.ui.user.repository.UserRepositoryImpl;

import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

@RunWith(RobolectricTestRunner.class)
public class SelectTransferMethodPresenterTest {

    private final Errors errors = createErrors();
    @Rule
    public HyperwalletExternalResourceManager externalResourceManager = new HyperwalletExternalResourceManager();
    @Mock
    private SelectTransferMethodContract.View view;
    @Mock
    private TransferMethodConfigurationRepositoryImpl mTransferMethodConfigurationRepository;
    @Mock
    private UserRepositoryImpl mUserRepository;
    @Captor
    private ArgumentCaptor<List<Error>> mErrorCaptor;

    private HyperwalletTransferMethodConfigurationKey mResult;
    private HyperwalletTransferMethodConfigurationKey mPartialResult;
    private HyperwalletTransferMethodConfigurationKey mFeeAndProcessingTimeResult;
    private User mUser;
    private SelectTransferMethodPresenter selectTransferMethodPresenter;

    @Before
    public void initialize() throws Exception {
        initMocks(this);
        String responseBody = externalResourceManager.getResourceContent("successful_tmc_keys_response.json");
        final JSONObject jsonObject = new JSONObject(responseBody);
        mResult = new TransferMethodConfigurationKeyResult(jsonObject);

        String partialResponseBody = externalResourceManager.getResourceContent(
                "partial_success_tmc_keys_response.json");
        mPartialResult = new TransferMethodConfigurationKeyResult(new JSONObject(partialResponseBody));

        String userResponseBody = externalResourceManager.getResourceContent("user_response.json");
        final JSONObject userJsonObject = new JSONObject(userResponseBody);
        mUser = new User(userJsonObject);

        String feeAndProcessingTimeResponseBody = externalResourceManager.getResourceContent("successful_tmc_keys_fee_processing_time_response.json");
        final JSONObject feeAndProcessingTimeJsonObject = new JSONObject(feeAndProcessingTimeResponseBody);
        mFeeAndProcessingTimeResult = new TransferMethodConfigurationKeyResult(feeAndProcessingTimeJsonObject);

        selectTransferMethodPresenter = new SelectTransferMethodPresenter(view, mTransferMethodConfigurationRepository,
                mUserRepository);
    }

    @Test
    public void testLoadTransferMethodConfigurationKeys_loadsKeysIntoViewOnSuccess() {
        // When
        when(view.isActive()).thenReturn(true);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodConfigurationRepository.LoadKeysCallback callback =
                        (TransferMethodConfigurationRepository.LoadKeysCallback) invocation.getArguments()[0];
                callback.onKeysLoaded(mResult);
                return callback;
            }
        }).when(mTransferMethodConfigurationRepository).getKeys(any(
                TransferMethodConfigurationRepository.LoadKeysCallback.class));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodConfigurationRepository.LoadKeysCallback callback =
                        (TransferMethodConfigurationRepository.LoadKeysCallback) invocation.getArguments()[2];
                callback.onKeysLoaded(mFeeAndProcessingTimeResult);
                return callback;
            }
        }).when(mTransferMethodConfigurationRepository).getFeeAndProcessingTime(anyString(), anyString(), any(
                TransferMethodConfigurationRepository.LoadKeysCallback.class));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                UserRepository.LoadUserCallback userCallback =
                        (UserRepository.LoadUserCallback) invocation.getArguments()[0];
                userCallback.onUserLoaded(mUser);
                return userCallback;
            }
        }).when(mUserRepository).loadUser(any(
                UserRepository.LoadUserCallback.class));

        // Then
        selectTransferMethodPresenter.loadTransferMethodConfigurationKeys(true, "CA", "CAD");

        verify(view).showTransferMethodCountry("CA");
        verify(view).showTransferMethodCurrency("CAD");
        verify(view).showTransferMethodTypes(ArgumentMatchers.<TransferMethodSelectionItem>anyList());
        verify(view, never()).showErrorLoadTransferMethodConfigurationKeys(
                ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadCurrency(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadTransferMethodTypes(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadCountrySelection(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadCurrencySelection(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showAddTransferMethod(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void testLoadTransferMethodConfigurationKeys_loadsFeeAndProcessingTimeKeysIntoViewOnError() {
        // When
        when(view.isActive()).thenReturn(true);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodConfigurationRepository.LoadKeysCallback callback =
                        (TransferMethodConfigurationRepository.LoadKeysCallback) invocation.getArguments()[0];
                callback.onKeysLoaded(mResult);
                return callback;
            }
        }).when(mTransferMethodConfigurationRepository).getKeys(any(
                TransferMethodConfigurationRepository.LoadKeysCallback.class));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodConfigurationRepository.LoadKeysCallback callback =
                        (TransferMethodConfigurationRepository.LoadKeysCallback) invocation.getArguments()[2];
                callback.onError(errors);
                return callback;
            }
        }).when(mTransferMethodConfigurationRepository).getFeeAndProcessingTime(anyString(), anyString(), any(
                TransferMethodConfigurationRepository.LoadKeysCallback.class));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                UserRepository.LoadUserCallback userCallback =
                        (UserRepository.LoadUserCallback) invocation.getArguments()[0];
                userCallback.onUserLoaded(mUser);
                return userCallback;
            }
        }).when(mUserRepository).loadUser(any(
                UserRepository.LoadUserCallback.class));

        // Then
        selectTransferMethodPresenter.loadTransferMethodConfigurationKeys(false, "CA", "CAD");

        verify(view).showTransferMethodCountry("CA");
        verify(view).showTransferMethodCurrency("CAD");
        verify(view, never()).showTransferMethodTypes(ArgumentMatchers.<TransferMethodSelectionItem>anyList());
        verify(view).showErrorLoadTransferMethodConfigurationKeys(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadCurrency(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadTransferMethodTypes(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadCountrySelection(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadCurrencySelection(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showAddTransferMethod(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void testLoadTransferMethodConfigurationKeys_loadsKeysIntoViewOnSuccessInactive() {
        // When
        when(view.isActive()).thenReturn(false);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodConfigurationRepository.LoadKeysCallback callback =
                        (TransferMethodConfigurationRepository.LoadKeysCallback) invocation.getArguments()[0];
                callback.onKeysLoaded(mResult);
                return callback;
            }
        }).when(mTransferMethodConfigurationRepository).getKeys(any(
                TransferMethodConfigurationRepository.LoadKeysCallback.class));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodConfigurationRepository.LoadKeysCallback callback =
                        (TransferMethodConfigurationRepository.LoadKeysCallback) invocation.getArguments()[2];
                callback.onKeysLoaded(mFeeAndProcessingTimeResult);
                return callback;
            }
        }).when(mTransferMethodConfigurationRepository).getFeeAndProcessingTime(anyString(), anyString(), any(
                TransferMethodConfigurationRepository.LoadKeysCallback.class));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                UserRepository.LoadUserCallback userCallback =
                        (UserRepository.LoadUserCallback) invocation.getArguments()[0];
                userCallback.onUserLoaded(mUser);
                return userCallback;
            }
        }).when(mUserRepository).loadUser(any(
                UserRepository.LoadUserCallback.class));

        // Then
        selectTransferMethodPresenter.loadTransferMethodConfigurationKeys(false, "CA", "CAD");

        verify(view, never()).hideProgressBar();
        verify(view, never()).showTransferMethodTypes(ArgumentMatchers.<TransferMethodSelectionItem>anyList());
        verify(view, never()).showErrorLoadTransferMethodConfigurationKeys(
                ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadCurrency(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadTransferMethodTypes(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadCountrySelection(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadCurrencySelection(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showAddTransferMethod(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void testLoadTransferMethodConfigurationKeys_loadsKeysIntoViewOnError() {
        // When
        when(view.isActive()).thenReturn(true);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodConfigurationRepository.LoadKeysCallback callback =
                        (TransferMethodConfigurationRepository.LoadKeysCallback) invocation.getArguments()[0];
                callback.onError(errors);
                return callback;
            }
        }).when(mTransferMethodConfigurationRepository).getKeys(any(
                TransferMethodConfigurationRepository.LoadKeysCallback.class));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodConfigurationRepository.LoadKeysCallback callback =
                        (TransferMethodConfigurationRepository.LoadKeysCallback) invocation.getArguments()[2];
                callback.onKeysLoaded(mFeeAndProcessingTimeResult);
                return callback;
            }
        }).when(mTransferMethodConfigurationRepository).getFeeAndProcessingTime(anyString(), anyString(), any(
                TransferMethodConfigurationRepository.LoadKeysCallback.class));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                UserRepository.LoadUserCallback userCallback =
                        (UserRepository.LoadUserCallback) invocation.getArguments()[0];
                userCallback.onUserLoaded(mUser);
                return userCallback;
            }
        }).when(mUserRepository).loadUser(any(
                UserRepository.LoadUserCallback.class));

        // Then
        selectTransferMethodPresenter.loadTransferMethodConfigurationKeys(false, "CA", "CAD");

        verify(view, never()).showTransferMethodTypes(ArgumentMatchers.<TransferMethodSelectionItem>anyList());
        verify(view).showErrorLoadTransferMethodConfigurationKeys(eq(errors.getErrors()));
        verify(view, never()).showAddTransferMethod(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void testLoadTransferMethodConfigurationKeys_loadsKeysIntoUserProfileCountryViewOnSuccess() {
        // When
        when(view.isActive()).thenReturn(true);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodConfigurationRepository.LoadKeysCallback callback =
                        (TransferMethodConfigurationRepository.LoadKeysCallback) invocation.getArguments()[0];
                callback.onKeysLoaded(mResult);
                return callback;
            }
        }).when(mTransferMethodConfigurationRepository).getKeys(any(
                TransferMethodConfigurationRepository.LoadKeysCallback.class));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodConfigurationRepository.LoadKeysCallback callback =
                        (TransferMethodConfigurationRepository.LoadKeysCallback) invocation.getArguments()[2];
                callback.onKeysLoaded(mFeeAndProcessingTimeResult);
                return callback;
            }
        }).when(mTransferMethodConfigurationRepository).getFeeAndProcessingTime(anyString(), anyString(), any(
                TransferMethodConfigurationRepository.LoadKeysCallback.class));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                UserRepository.LoadUserCallback userCallback =
                        (UserRepository.LoadUserCallback) invocation.getArguments()[0];
                userCallback.onUserLoaded(mUser);
                return userCallback;
            }
        }).when(mUserRepository).loadUser(any(
                UserRepository.LoadUserCallback.class));

        // Then
        selectTransferMethodPresenter.loadTransferMethodConfigurationKeys(false, null, null);

        verify(view).showTransferMethodCountry("US");
        verify(view).showTransferMethodCurrency("USD");
        verify(view).showTransferMethodTypes(ArgumentMatchers.<TransferMethodSelectionItem>anyList());
        verify(view, never()).showErrorLoadTransferMethodConfigurationKeys(
                ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadCurrency(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadTransferMethodTypes(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadCountrySelection(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadCurrencySelection(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showAddTransferMethod(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void testLoadTransferMethodConfigurationKeys_loadsKeysIntoUserProfileCountryViewOnError() {
        // When
        when(view.isActive()).thenReturn(true);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodConfigurationRepository.LoadKeysCallback callback =
                        (TransferMethodConfigurationRepository.LoadKeysCallback) invocation.getArguments()[0];
                callback.onKeysLoaded(mPartialResult);
                return callback;
            }
        }).when(mTransferMethodConfigurationRepository).getKeys(any(
                TransferMethodConfigurationRepository.LoadKeysCallback.class));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodConfigurationRepository.LoadKeysCallback callback =
                        (TransferMethodConfigurationRepository.LoadKeysCallback) invocation.getArguments()[2];
                callback.onKeysLoaded(mFeeAndProcessingTimeResult);
                return callback;
            }
        }).when(mTransferMethodConfigurationRepository).getFeeAndProcessingTime(anyString(), anyString(), any(
                TransferMethodConfigurationRepository.LoadKeysCallback.class));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                UserRepository.LoadUserCallback userCallback =
                        (UserRepository.LoadUserCallback) invocation.getArguments()[0];
                userCallback.onUserLoaded(mUser);
                return userCallback;
            }
        }).when(mUserRepository).loadUser(any(
                UserRepository.LoadUserCallback.class));

        // Then
        selectTransferMethodPresenter.loadTransferMethodConfigurationKeys(false, null, null);


        verify(view).showProgressBar();
        verify(view, times(2)).isActive();
        verify(view).hideProgressBar();
        verify(view).showErrorLoadTransferMethodConfigurationKeys(mErrorCaptor.capture());
        verify(view, never()).showTransferMethodCountry(anyString());
        verify(view, never()).showTransferMethodCurrency(anyString());
        verify(view, never()).showTransferMethodTypes(ArgumentMatchers.<TransferMethodSelectionItem>anyList());

        // Assert
        List<Error> errors = mErrorCaptor.getValue();
        assertThat(errors, Matchers.<Error>hasSize(1));
        assertThat(errors.get(0).getCode(), is(EC_UNEXPECTED_EXCEPTION));
        assertThat(errors.get(0).getMessage(), is("Can't get Currency based from Country: US"));
    }

    @Test
    public void testLoadCurrency_loadsCurrenciesIntoViewOnSuccess() {
        // When
        when(view.isActive()).thenReturn(true);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodConfigurationRepository.LoadKeysCallback callback =
                        (TransferMethodConfigurationRepository.LoadKeysCallback) invocation.getArguments()[0];

                callback.onKeysLoaded(mResult);
                return callback;
            }
        }).when(mTransferMethodConfigurationRepository).getKeys(any(
                TransferMethodConfigurationRepository.LoadKeysCallback.class));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodConfigurationRepository.LoadKeysCallback callback =
                        (TransferMethodConfigurationRepository.LoadKeysCallback) invocation.getArguments()[2];
                callback.onKeysLoaded(mFeeAndProcessingTimeResult);
                return callback;
            }
        }).when(mTransferMethodConfigurationRepository).getFeeAndProcessingTime(anyString(), anyString(), any(
                TransferMethodConfigurationRepository.LoadKeysCallback.class));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                UserRepository.LoadUserCallback userCallback =
                        (UserRepository.LoadUserCallback) invocation.getArguments()[0];
                userCallback.onUserLoaded(mUser);
                return userCallback;
            }
        }).when(mUserRepository).loadUser(any(
                UserRepository.LoadUserCallback.class));

        // Then
        selectTransferMethodPresenter.loadCurrency(false, "CA");

        verify(view).showTransferMethodCurrency("CAD");
       // verify(view).showTransferMethodTypes(ArgumentMatchers.<TransferMethodSelectionItem>anyList());
        verify(view, never()).showErrorLoadTransferMethodConfigurationKeys(
                ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadCurrency(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadTransferMethodTypes(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadCountrySelection(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadCurrencySelection(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showAddTransferMethod(anyString(), anyString(), anyString(), anyString());
        verify(view, atLeastOnce()).showTransferMethodCountry(anyString());
    }

    @Test
    public void testLoadCurrency_loadsCurrenciesIntoViewOnSuccessInactive() {
        // When
        when(view.isActive()).thenReturn(false);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodConfigurationRepository.LoadKeysCallback callback =
                        (TransferMethodConfigurationRepository.LoadKeysCallback) invocation.getArguments()[0];

                callback.onKeysLoaded(mResult);
                return callback;
            }
        }).when(mTransferMethodConfigurationRepository).getKeys(any(
                TransferMethodConfigurationRepository.LoadKeysCallback.class));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodConfigurationRepository.LoadKeysCallback callback =
                        (TransferMethodConfigurationRepository.LoadKeysCallback) invocation.getArguments()[2];
                callback.onKeysLoaded(mFeeAndProcessingTimeResult);
                return callback;
            }
        }).when(mTransferMethodConfigurationRepository).getFeeAndProcessingTime(anyString(), anyString(), any(
                TransferMethodConfigurationRepository.LoadKeysCallback.class));


        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                UserRepository.LoadUserCallback userCallback =
                        (UserRepository.LoadUserCallback) invocation.getArguments()[0];
                userCallback.onUserLoaded(mUser);
                return userCallback;
            }
        }).when(mUserRepository).loadUser(any(
                UserRepository.LoadUserCallback.class));

        // Then
        selectTransferMethodPresenter.loadCurrency(false, "CA");

        verify(view, never()).showTransferMethodTypes(ArgumentMatchers.<TransferMethodSelectionItem>anyList());
        verify(view, never()).showErrorLoadTransferMethodConfigurationKeys(
                ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadCurrency(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadTransferMethodTypes(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadCountrySelection(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadCurrencySelection(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showAddTransferMethod(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void testLoadCurrency_loadsCurrenciesWhenRefreshingKeys() {
        selectTransferMethodPresenter.loadCurrency(true, "CA");
        verify(mTransferMethodConfigurationRepository, times(1)).refreshKeys();
    }


    @Test
    public void testLoadCurrency_loadsCurrenciesIntoViewOnError() {
        // When
        when(view.isActive()).thenReturn(true);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodConfigurationRepository.LoadKeysCallback callback =
                        (TransferMethodConfigurationRepository.LoadKeysCallback) invocation.getArguments()[0];

                callback.onError(errors);
                return callback;
            }
        }).when(mTransferMethodConfigurationRepository).getKeys(any(
                TransferMethodConfigurationRepository.LoadKeysCallback.class));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodConfigurationRepository.LoadKeysCallback callback =
                        (TransferMethodConfigurationRepository.LoadKeysCallback) invocation.getArguments()[2];
                callback.onKeysLoaded(mFeeAndProcessingTimeResult);
                return callback;
            }
        }).when(mTransferMethodConfigurationRepository).getFeeAndProcessingTime(anyString(), anyString(), any(
                TransferMethodConfigurationRepository.LoadKeysCallback.class));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                UserRepository.LoadUserCallback userCallback =
                        (UserRepository.LoadUserCallback) invocation.getArguments()[0];
                userCallback.onUserLoaded(mUser);
                return userCallback;
            }
        }).when(mUserRepository).loadUser(any(
                UserRepository.LoadUserCallback.class));

        // Then
        selectTransferMethodPresenter.loadCurrency(false, "CA");

        verify(view, never()).showTransferMethodTypes(ArgumentMatchers.<TransferMethodSelectionItem>anyList());
        verify(view).showErrorLoadCurrency(eq(errors.getErrors()));
        verify(view, never()).showAddTransferMethod(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void testLoadTransferMethodTypes_loadsTypesIntoViewOnSuccess() {
        // When
        when(view.isActive()).thenReturn(true);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodConfigurationRepository.LoadKeysCallback callback =
                        (TransferMethodConfigurationRepository.LoadKeysCallback) invocation.getArguments()[0];

                callback.onKeysLoaded(mResult);
                return callback;
            }
        }).when(mTransferMethodConfigurationRepository).getKeys(any(
                TransferMethodConfigurationRepository.LoadKeysCallback.class));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodConfigurationRepository.LoadKeysCallback callback =
                        (TransferMethodConfigurationRepository.LoadKeysCallback) invocation.getArguments()[2];
                callback.onKeysLoaded(mFeeAndProcessingTimeResult);
                return callback;
            }
        }).when(mTransferMethodConfigurationRepository).getFeeAndProcessingTime(anyString(), anyString(), any(
                TransferMethodConfigurationRepository.LoadKeysCallback.class));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                UserRepository.LoadUserCallback userCallback =
                        (UserRepository.LoadUserCallback) invocation.getArguments()[0];
                userCallback.onUserLoaded(mUser);
                return userCallback;
            }
        }).when(mUserRepository).loadUser(any(
                UserRepository.LoadUserCallback.class));


        // Then
        selectTransferMethodPresenter.loadTransferMethodTypes(true, "CA", "CAD");

        verify(view).showTransferMethodTypes(ArgumentMatchers.<TransferMethodSelectionItem>anyList());
        verify(view, never()).showErrorLoadTransferMethodConfigurationKeys(
                ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadCurrency(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadTransferMethodTypes(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadCountrySelection(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadCurrencySelection(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showAddTransferMethod(anyString(), anyString(), anyString(), anyString());
        verify(view, atLeastOnce()).showTransferMethodCountry(anyString());
        verify(view, atLeastOnce()).showTransferMethodCurrency(anyString());
    }

    @Test
    public void testLoadTransferMethodTypes_loadsTypesIntoViewOnSuccessInactive() {
        // When
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodConfigurationRepository.LoadKeysCallback callback =
                        (TransferMethodConfigurationRepository.LoadKeysCallback) invocation.getArguments()[0];

                callback.onKeysLoaded(mResult);
                return callback;
            }
        }).when(mTransferMethodConfigurationRepository).getKeys(any(
                TransferMethodConfigurationRepository.LoadKeysCallback.class));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodConfigurationRepository.LoadKeysCallback callback =
                        (TransferMethodConfigurationRepository.LoadKeysCallback) invocation.getArguments()[2];
                callback.onKeysLoaded(mFeeAndProcessingTimeResult);
                return callback;
            }
        }).when(mTransferMethodConfigurationRepository).getFeeAndProcessingTime(anyString(), anyString(), any(
                TransferMethodConfigurationRepository.LoadKeysCallback.class));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                UserRepository.LoadUserCallback userCallback =
                        (UserRepository.LoadUserCallback) invocation.getArguments()[0];
                userCallback.onUserLoaded(mUser);
                return userCallback;
            }
        }).when(mUserRepository).loadUser(any(
                UserRepository.LoadUserCallback.class));


        // Then
        selectTransferMethodPresenter.loadTransferMethodTypes(false, "CA", "CAD");

        verify(view, never()).showTransferMethodTypes(ArgumentMatchers.<TransferMethodSelectionItem>anyList());
        verify(view, never()).showErrorLoadTransferMethodConfigurationKeys(
                ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadCurrency(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadTransferMethodTypes(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadCountrySelection(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadCurrencySelection(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showAddTransferMethod(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void testLoadTransferMethodTypes_loadsTypesIntoViewOnError() {
        // When
        when(view.isActive()).thenReturn(true);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodConfigurationRepository.LoadKeysCallback callback =
                        (TransferMethodConfigurationRepository.LoadKeysCallback) invocation.getArguments()[0];

                callback.onError(errors);
                return callback;
            }
        }).when(mTransferMethodConfigurationRepository).getKeys(any(
                TransferMethodConfigurationRepository.LoadKeysCallback.class));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodConfigurationRepository.LoadKeysCallback callback =
                        (TransferMethodConfigurationRepository.LoadKeysCallback) invocation.getArguments()[2];
                callback.onKeysLoaded(mFeeAndProcessingTimeResult);
                return callback;
            }
        }).when(mTransferMethodConfigurationRepository).getFeeAndProcessingTime(anyString(), anyString(), any(
                TransferMethodConfigurationRepository.LoadKeysCallback.class));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                UserRepository.LoadUserCallback userCallback =
                        (UserRepository.LoadUserCallback) invocation.getArguments()[0];
                userCallback.onUserLoaded(mUser);
                return userCallback;
            }
        }).when(mUserRepository).loadUser(any(
                UserRepository.LoadUserCallback.class));

        // Then
        selectTransferMethodPresenter.loadTransferMethodTypes(false, "CA", "CAD");

        verify(view, never()).showTransferMethodTypes(ArgumentMatchers.<TransferMethodSelectionItem>anyList());
        verify(view).showErrorLoadTransferMethodTypes(eq(errors.getErrors()));
        verify(view, never()).showAddTransferMethod(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void testLoadTransferMethodTypes_loadsTypesIntoViewOnErrorInActive() {
        // When
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodConfigurationRepository.LoadKeysCallback callback =
                        (TransferMethodConfigurationRepository.LoadKeysCallback) invocation.getArguments()[0];

                callback.onError(errors);
                return callback;
            }
        }).when(mTransferMethodConfigurationRepository).getKeys(any(
                TransferMethodConfigurationRepository.LoadKeysCallback.class));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodConfigurationRepository.LoadKeysCallback callback =
                        (TransferMethodConfigurationRepository.LoadKeysCallback) invocation.getArguments()[2];
                callback.onKeysLoaded(mFeeAndProcessingTimeResult);
                return callback;
            }
        }).when(mTransferMethodConfigurationRepository).getFeeAndProcessingTime(anyString(), anyString(), any(
                TransferMethodConfigurationRepository.LoadKeysCallback.class));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                UserRepository.LoadUserCallback userCallback =
                        (UserRepository.LoadUserCallback) invocation.getArguments()[0];
                userCallback.onUserLoaded(mUser);
                return userCallback;
            }
        }).when(mUserRepository).loadUser(any(
                UserRepository.LoadUserCallback.class));

        // Then
        selectTransferMethodPresenter.loadTransferMethodTypes(false, "CA", "CAD");

        verify(view, never()).showTransferMethodTypes(ArgumentMatchers.<TransferMethodSelectionItem>anyList());
        verify(view, never()).showErrorLoadTransferMethodTypes(eq(errors.getErrors()));
        verify(view, never()).showAddTransferMethod(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void testOpenAddTransferMethod_showsAddTransferMethodUi() {
        // When
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodConfigurationRepository.LoadKeysCallback callback =
                        (TransferMethodConfigurationRepository.LoadKeysCallback) invocation.getArguments()[0];

                callback.onKeysLoaded(mResult);
                return callback;
            }
        }).when(mTransferMethodConfigurationRepository).getKeys(any(TransferMethodConfigurationRepository.
                LoadKeysCallback.class));

        // Then
        selectTransferMethodPresenter.openAddTransferMethod("CA", "CAD",
                TransferMethod.TransferMethodTypes.BANK_ACCOUNT,
                User.ProfileTypes.INDIVIDUAL);

        verify(view).showAddTransferMethod("CA", "CAD", TransferMethod.TransferMethodTypes.BANK_ACCOUNT,
                User.ProfileTypes.INDIVIDUAL);
        verify(view, never()).showErrorLoadTransferMethodConfigurationKeys(
                ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadCurrency(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadTransferMethodTypes(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadCountrySelection(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadCurrencySelection(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showTransferMethodCountry(anyString());
        verify(view, never()).showTransferMethodCurrency(anyString());
        verify(view, never()).showTransferMethodTypes(ArgumentMatchers.<TransferMethodSelectionItem>anyList());
    }

    @Test
    public void testLoadTransferMethodTypes_loadCountrySelectionOnSuccess() {
        // When
        when(view.isActive()).thenReturn(true);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodConfigurationRepository.LoadKeysCallback callback =
                        (TransferMethodConfigurationRepository.LoadKeysCallback) invocation.getArguments()[0];

                callback.onKeysLoaded(mResult);
                return callback;
            }
        }).when(mTransferMethodConfigurationRepository).getKeys(any(
                TransferMethodConfigurationRepository.LoadKeysCallback.class));

        // Then
        selectTransferMethodPresenter.loadCountrySelection("CA");

        verify(view).showCountrySelectionDialog(any(TreeMap.class), anyString());
        verify(view, never()).showErrorLoadTransferMethodConfigurationKeys(
                ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadCurrency(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadTransferMethodTypes(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadCountrySelection(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadCurrencySelection(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showAddTransferMethod(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void testLoadTransferMethodTypes_loadCountrySelectionOnSuccessInactive() {
        // When
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodConfigurationRepository.LoadKeysCallback callback =
                        (TransferMethodConfigurationRepository.LoadKeysCallback) invocation.getArguments()[0];

                callback.onKeysLoaded(mResult);
                return callback;
            }
        }).when(mTransferMethodConfigurationRepository).getKeys(any(
                TransferMethodConfigurationRepository.LoadKeysCallback.class));

        // Then
        selectTransferMethodPresenter.loadCountrySelection("CA");

        verify(view, never()).showCountrySelectionDialog(any(TreeMap.class), anyString());
        verify(view, never()).showErrorLoadTransferMethodConfigurationKeys(
                ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadCurrency(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadTransferMethodTypes(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadCountrySelection(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadCurrencySelection(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showAddTransferMethod(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void testLoadTransferMethodTypes_loadCountrySelectionOnError() {
        // When
        when(view.isActive()).thenReturn(true);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodConfigurationRepository.LoadKeysCallback callback =
                        (TransferMethodConfigurationRepository.LoadKeysCallback) invocation.getArguments()[0];

                callback.onError(errors);
                return callback;
            }
        }).when(mTransferMethodConfigurationRepository).getKeys(any(
                TransferMethodConfigurationRepository.LoadKeysCallback.class));

        // Then
        selectTransferMethodPresenter.loadCountrySelection("CA");

        verify(view, never()).showCountrySelectionDialog(any(TreeMap.class), anyString());
        verify(view).showErrorLoadCountrySelection(eq(errors.getErrors()));
        verify(view, never()).showAddTransferMethod(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void testLoadTransferMethodTypes_loadCountrySelectionOnErrorInactive() {
        // When
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodConfigurationRepository.LoadKeysCallback callback =
                        (TransferMethodConfigurationRepository.LoadKeysCallback) invocation.getArguments()[0];

                callback.onError(errors);
                return callback;
            }
        }).when(mTransferMethodConfigurationRepository).getKeys(any(
                TransferMethodConfigurationRepository.LoadKeysCallback.class));

        // Then
        selectTransferMethodPresenter.loadCountrySelection("CA");

        verify(view, never()).showCountrySelectionDialog(any(TreeMap.class), anyString());
        verify(view, never()).showErrorLoadTransferMethodConfigurationKeys(
                ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadCurrency(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadTransferMethodTypes(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadCountrySelection(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadCurrencySelection(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showAddTransferMethod(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void testLoadTransferMethodTypes_loadCurrencySelectionOnSuccess() {
        // When
        when(view.isActive()).thenReturn(true);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodConfigurationRepository.LoadKeysCallback callback =
                        (TransferMethodConfigurationRepository.LoadKeysCallback) invocation.getArguments()[0];

                callback.onKeysLoaded(mResult);
                return callback;
            }
        }).when(mTransferMethodConfigurationRepository).getKeys(any(
                TransferMethodConfigurationRepository.LoadKeysCallback.class));

        // Then
        selectTransferMethodPresenter.loadCurrencySelection("CA", "CAD");

        verify(view).showCurrencySelectionDialog(any(TreeMap.class), anyString());
        verify(view, never()).showErrorLoadTransferMethodConfigurationKeys(
                ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadCurrency(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadTransferMethodTypes(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadCountrySelection(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadCurrencySelection(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showAddTransferMethod(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void testLoadTransferMethodTypes_loadCurrencySelectionOnSuccessInactive() {
        // When
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodConfigurationRepository.LoadKeysCallback callback =
                        (TransferMethodConfigurationRepository.LoadKeysCallback) invocation.getArguments()[0];

                callback.onKeysLoaded(mResult);
                return callback;
            }
        }).when(mTransferMethodConfigurationRepository).getKeys(any(
                TransferMethodConfigurationRepository.LoadKeysCallback.class));

        // Then
        selectTransferMethodPresenter.loadCurrencySelection("CA", "CAD");

        verify(view, never()).showCurrencySelectionDialog(any(TreeMap.class), anyString());
        verify(view, never()).showErrorLoadTransferMethodConfigurationKeys(
                ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadCurrency(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadTransferMethodTypes(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadCountrySelection(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadCurrencySelection(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showAddTransferMethod(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void testLoadTransferMethodTypes_loadCurrencySelectionOnError() {
        // When
        when(view.isActive()).thenReturn(true);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodConfigurationRepository.LoadKeysCallback callback =
                        (TransferMethodConfigurationRepository.LoadKeysCallback) invocation.getArguments()[0];

                callback.onError(errors);
                return callback;
            }
        }).when(mTransferMethodConfigurationRepository).getKeys(any(
                TransferMethodConfigurationRepository.LoadKeysCallback.class));

        // Then
        selectTransferMethodPresenter.loadCurrencySelection("CA", "CAD");

        verify(view, never()).showCurrencySelectionDialog(any(TreeMap.class), anyString());
        verify(view).showErrorLoadCurrencySelection(eq(errors.getErrors()));
        verify(view, never()).showAddTransferMethod(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void testLoadTransferMethodTypes_loadCurrencySelectionOnErrorInactive() {
        // When
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodConfigurationRepository.LoadKeysCallback callback =
                        (TransferMethodConfigurationRepository.LoadKeysCallback) invocation.getArguments()[0];

                callback.onError(errors);
                return callback;
            }
        }).when(mTransferMethodConfigurationRepository).getKeys(any(
                TransferMethodConfigurationRepository.LoadKeysCallback.class));

        // Then
        selectTransferMethodPresenter.loadCurrencySelection("CA", "CAD");

        verify(view, never()).showCurrencySelectionDialog(any(TreeMap.class), anyString());
        verify(view, never()).showErrorLoadTransferMethodConfigurationKeys(
                ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadCurrency(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadTransferMethodTypes(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadCountrySelection(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadCurrencySelection(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showAddTransferMethod(anyString(), anyString(), anyString(), anyString());
    }

    private Errors createErrors() {
        List<Error> errors = new ArrayList<>();
        Error error = new Error("test message", "TEST_CODE");
        errors.add(error);
        return new Errors(errors);
    }

    @Test
    public void testLoadMethods_whenLoadUserWithError_checkShowingErrors() {

        final Error error = new Error("test message", "TEST_CODE");
        List<Error> errorList = new ArrayList<>();
        errorList.add(error);
        final Errors errors = new Errors(errorList);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                UserRepository.LoadUserCallback userCallback =
                        (UserRepository.LoadUserCallback) invocation.getArguments()[0];
                userCallback.onError(errors);
                return userCallback;
            }
        }).when(mUserRepository).loadUser(any(
                UserRepository.LoadUserCallback.class));
        when(view.isActive()).thenReturn(false);
        // Then
        selectTransferMethodPresenter.loadTransferMethodConfigurationKeys(false, "CA", "CAD");

        verify(view, never()).showErrorLoadTransferMethodConfigurationKeys(
                ArgumentMatchers.<Error>anyList());

        selectTransferMethodPresenter.loadCurrency(false, "CA");
        verify(view, never()).showErrorLoadCurrency(ArgumentMatchers.<Error>anyList());

        selectTransferMethodPresenter.loadTransferMethodTypes(false, "CA", "CAD");
        verify(view, never()).showErrorLoadTransferMethodTypes(ArgumentMatchers.<Error>anyList());


        // When
        when(view.isActive()).thenReturn(true);

        // Then
        selectTransferMethodPresenter.loadTransferMethodConfigurationKeys(false, "CA", "CAD");

        verify(view).showErrorLoadTransferMethodConfigurationKeys(ArgumentMatchers.<Error>anyList());

        selectTransferMethodPresenter.loadCurrency(false, "CA");
        verify(view).showErrorLoadCurrency(ArgumentMatchers.<Error>anyList());

        selectTransferMethodPresenter.loadTransferMethodTypes(false, "CA", "CAD");
        verify(view).showErrorLoadTransferMethodTypes(ArgumentMatchers.<Error>anyList());
    }
}
