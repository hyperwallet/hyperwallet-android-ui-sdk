package com.hyperwallet.android.ui.transfermethod;

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

import com.hyperwallet.android.model.HyperwalletError;
import com.hyperwallet.android.model.HyperwalletErrors;
import com.hyperwallet.android.model.HyperwalletUser;
import com.hyperwallet.android.model.meta.TransferMethodConfigurationResult;
import com.hyperwallet.android.ui.repository.TransferMethodConfigurationRepository;
import com.hyperwallet.android.ui.repository.TransferMethodConfigurationRepositoryImpl;
import com.hyperwallet.android.ui.repository.UserRepository;
import com.hyperwallet.android.ui.repository.UserRepositoryImpl;
import com.hyperwallet.android.ui.rule.HyperwalletExternalResourceManager;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

@RunWith(RobolectricTestRunner.class)
public class SelectTransferMethodPresenterTest {
    private static final String COUNTRY = "CA";
    private static final String CURRENCY = "CAD";
    private static final String BANK_ACCOUNT = "BANK_ACCOUNT";

    @Mock
    private SelectTransferMethodContract.View view;
    @Mock
    private TransferMethodConfigurationRepositoryImpl mTransferMethodConfigurationRepository;
    @Mock
    private UserRepositoryImpl mUserRepository;
    @Rule
    public HyperwalletExternalResourceManager externalResourceManager = new HyperwalletExternalResourceManager();

    private TransferMethodConfigurationResult mResult;
    private HyperwalletUser mUser;
    private SelectTransferMethodPresenter selectTransferMethodPresenter;
    private final HyperwalletErrors errors = createErrors();

    @Before
    public void initialize() throws Exception {
        initMocks(this);

        String methodsResponseBody = externalResourceManager.getResourceContent("successful_tmc_keys_response.json");
        final JSONObject methodsJsonObject = new JSONObject(methodsResponseBody);
        mResult = new TransferMethodConfigurationResult(methodsJsonObject);

        String userResponseBody = externalResourceManager.getResourceContent("user_response.json");
        final JSONObject userJsonObject = new JSONObject(userResponseBody);
        mUser = new HyperwalletUser(userJsonObject);

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
        verify(view).showTransferMethodTypes(ArgumentMatchers.<TransferMethodSelectionItem>anyList());
        verify(view, never()).showErrorLoadTransferMethodConfigurationKeys(
                ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showErrorLoadCurrency(ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showErrorLoadTransferMethodTypes(ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showErrorLoadCountrySelection(ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showErrorLoadCurrencySelection(ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showAddTransferMethod(anyString(), anyString(), anyString());
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

        // Then
        selectTransferMethodPresenter.loadTransferMethodConfigurationKeys(false, "CA", "CAD");

        verify(view, never()).hideProgressBar();
        verify(view, never()).showTransferMethodTypes(ArgumentMatchers.<TransferMethodSelectionItem>anyList());
        verify(view, never()).showErrorLoadTransferMethodConfigurationKeys(
                ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showErrorLoadCurrency(ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showErrorLoadTransferMethodTypes(ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showErrorLoadCountrySelection(ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showErrorLoadCurrencySelection(ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showAddTransferMethod(anyString(), anyString(), anyString());
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
        verify(view, never()).showAddTransferMethod(anyString(), anyString(), anyString());
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
                UserRepository.LoadUserCallback userCallback =
                        (UserRepository.LoadUserCallback) invocation.getArguments()[0];
                userCallback.onUserLoaded(mUser);
                return userCallback;
            }
        }).when(mUserRepository).loadUser(any(
                UserRepository.LoadUserCallback.class));

        // Then
        selectTransferMethodPresenter.loadCurrency(false, "CA");

        verify(view).showTransferMethodCurrency("USD");
        verify(view).showTransferMethodTypes(ArgumentMatchers.<TransferMethodSelectionItem>anyList());
        verify(view, never()).showErrorLoadTransferMethodConfigurationKeys(
                ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showErrorLoadCurrency(ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showErrorLoadTransferMethodTypes(ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showErrorLoadCountrySelection(ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showErrorLoadCurrencySelection(ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showAddTransferMethod(anyString(), anyString(), anyString());
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
                ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showErrorLoadCurrency(ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showErrorLoadTransferMethodTypes(ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showErrorLoadCountrySelection(ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showErrorLoadCurrencySelection(ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showAddTransferMethod(anyString(), anyString(), anyString());
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
        verify(view, never()).showAddTransferMethod(anyString(), anyString(), anyString());
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
                UserRepository.LoadUserCallback userCallback =
                        (UserRepository.LoadUserCallback) invocation.getArguments()[0];
                userCallback.onUserLoaded(mUser);
                return userCallback;
            }
        }).when(mUserRepository).loadUser(any(
                UserRepository.LoadUserCallback.class));


        // Then
        selectTransferMethodPresenter.loadTransferMethodTypes(false, COUNTRY, CURRENCY);

        verify(view).showTransferMethodTypes(ArgumentMatchers.<TransferMethodSelectionItem>anyList());
        verify(view, never()).showErrorLoadTransferMethodConfigurationKeys(
                ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showErrorLoadCurrency(ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showErrorLoadTransferMethodTypes(ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showErrorLoadCountrySelection(ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showErrorLoadCurrencySelection(ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showAddTransferMethod(anyString(), anyString(), anyString());
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

        // Then
        selectTransferMethodPresenter.loadTransferMethodTypes(false, COUNTRY, CURRENCY);

        verify(view, never()).showTransferMethodTypes(ArgumentMatchers.<TransferMethodSelectionItem>anyList());
        verify(view, never()).showErrorLoadTransferMethodConfigurationKeys(
                ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showErrorLoadCurrency(ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showErrorLoadTransferMethodTypes(ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showErrorLoadCountrySelection(ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showErrorLoadCurrencySelection(ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showAddTransferMethod(anyString(), anyString(), anyString());
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
        verify(view, never()).showAddTransferMethod(anyString(), anyString(), anyString());
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

        // Then
        selectTransferMethodPresenter.loadTransferMethodTypes(false, "CA", "CAD");

        verify(view, never()).showTransferMethodTypes(ArgumentMatchers.<TransferMethodSelectionItem>anyList());
        verify(view, never()).showErrorLoadTransferMethodTypes(eq(errors.getErrors()));
        verify(view, never()).showAddTransferMethod(anyString(), anyString(), anyString());
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
        selectTransferMethodPresenter.openAddTransferMethod(COUNTRY, CURRENCY, BANK_ACCOUNT);

        verify(view).showAddTransferMethod(COUNTRY, CURRENCY, BANK_ACCOUNT);
        verify(view, never()).showErrorLoadTransferMethodConfigurationKeys(
                ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showErrorLoadCurrency(ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showErrorLoadTransferMethodTypes(ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showErrorLoadCountrySelection(ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showErrorLoadCurrencySelection(ArgumentMatchers.<HyperwalletError>anyList());
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
        selectTransferMethodPresenter.loadCountrySelection(COUNTRY);

        verify(view).showCountrySelectionDialog(any(TreeMap.class), anyString());
        verify(view, never()).showErrorLoadTransferMethodConfigurationKeys(
                ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showErrorLoadCurrency(ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showErrorLoadTransferMethodTypes(ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showErrorLoadCountrySelection(ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showErrorLoadCurrencySelection(ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showAddTransferMethod(anyString(), anyString(), anyString());
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
        selectTransferMethodPresenter.loadCountrySelection(COUNTRY);

        verify(view, never()).showCountrySelectionDialog(any(TreeMap.class), anyString());
        verify(view, never()).showErrorLoadTransferMethodConfigurationKeys(
                ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showErrorLoadCurrency(ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showErrorLoadTransferMethodTypes(ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showErrorLoadCountrySelection(ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showErrorLoadCurrencySelection(ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showAddTransferMethod(anyString(), anyString(), anyString());
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
        selectTransferMethodPresenter.loadCountrySelection(COUNTRY);

        verify(view, never()).showCountrySelectionDialog(any(TreeMap.class), anyString());
        verify(view).showErrorLoadCountrySelection(eq(errors.getErrors()));
        verify(view, never()).showAddTransferMethod(anyString(), anyString(), anyString());
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
        selectTransferMethodPresenter.loadCountrySelection(COUNTRY);

        verify(view, never()).showCountrySelectionDialog(any(TreeMap.class), anyString());
        verify(view, never()).showErrorLoadTransferMethodConfigurationKeys(
                ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showErrorLoadCurrency(ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showErrorLoadTransferMethodTypes(ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showErrorLoadCountrySelection(ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showErrorLoadCurrencySelection(ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showAddTransferMethod(anyString(), anyString(), anyString());
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
        selectTransferMethodPresenter.loadCurrencySelection(COUNTRY, CURRENCY);

        verify(view).showCurrencySelectionDialog(any(TreeMap.class), anyString());
        verify(view, never()).showErrorLoadTransferMethodConfigurationKeys(
                ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showErrorLoadCurrency(ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showErrorLoadTransferMethodTypes(ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showErrorLoadCountrySelection(ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showErrorLoadCurrencySelection(ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showAddTransferMethod(anyString(), anyString(), anyString());
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
        selectTransferMethodPresenter.loadCurrencySelection(COUNTRY, CURRENCY);

        verify(view, never()).showCurrencySelectionDialog(any(TreeMap.class), anyString());
        verify(view, never()).showErrorLoadTransferMethodConfigurationKeys(
                ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showErrorLoadCurrency(ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showErrorLoadTransferMethodTypes(ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showErrorLoadCountrySelection(ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showErrorLoadCurrencySelection(ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showAddTransferMethod(anyString(), anyString(), anyString());
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
        selectTransferMethodPresenter.loadCurrencySelection(COUNTRY, CURRENCY);

        verify(view, never()).showCurrencySelectionDialog(any(TreeMap.class), anyString());
        verify(view).showErrorLoadCurrencySelection(eq(errors.getErrors()));
        verify(view, never()).showAddTransferMethod(anyString(), anyString(), anyString());
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
        selectTransferMethodPresenter.loadCurrencySelection(COUNTRY, CURRENCY);

        verify(view, never()).showCurrencySelectionDialog(any(TreeMap.class), anyString());
        verify(view, never()).showErrorLoadTransferMethodConfigurationKeys(
                ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showErrorLoadCurrency(ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showErrorLoadTransferMethodTypes(ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showErrorLoadCountrySelection(ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showErrorLoadCurrencySelection(ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showAddTransferMethod(anyString(), anyString(), anyString());
    }

    private HyperwalletErrors createErrors() {
        List<HyperwalletError> errors = new ArrayList<>();
        HyperwalletError error = new HyperwalletError("test message", "TEST_CODE");
        errors.add(error);
        return new HyperwalletErrors(errors);
    }
}
