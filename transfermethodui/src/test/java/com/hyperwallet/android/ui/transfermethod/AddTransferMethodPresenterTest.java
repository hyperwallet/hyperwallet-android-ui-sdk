package com.hyperwallet.android.ui.transfermethod;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static com.hyperwallet.android.model.user.User.ProfileTypes.INDIVIDUAL;

import com.hyperwallet.android.model.Error;
import com.hyperwallet.android.model.Errors;
import com.hyperwallet.android.model.graphql.Fee;
import com.hyperwallet.android.model.graphql.HyperwalletTransferMethodConfigurationField;
import com.hyperwallet.android.model.graphql.ProcessingTime;
import com.hyperwallet.android.model.graphql.field.FieldGroup;
import com.hyperwallet.android.model.graphql.field.TransferMethodConfiguration;
import com.hyperwallet.android.model.transfermethod.BankAccount;
import com.hyperwallet.android.model.transfermethod.TransferMethod;
import com.hyperwallet.android.ui.testutils.rule.HyperwalletExternalResourceManager;
import com.hyperwallet.android.ui.transfermethod.repository.TransferMethodConfigurationRepository;
import com.hyperwallet.android.ui.transfermethod.repository.TransferMethodRepository;
import com.hyperwallet.android.ui.transfermethod.view.AddTransferMethodContract;
import com.hyperwallet.android.ui.transfermethod.view.AddTransferMethodPresenter;

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
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(RobolectricTestRunner.class)
public class AddTransferMethodPresenterTest {

    private final static CountDownLatch LATCH = new CountDownLatch(1);
    private final static int AWAIT_TIME_MS = 50;

    private final Errors errors = createErrors();
    private final BankAccount bankAccount = new BankAccount
            .Builder("CA", "CAD", "3423423432")
            .build();
    @Rule
    public MockitoRule mockito = MockitoJUnit.rule();
    @Rule
    public HyperwalletExternalResourceManager externalResourceManager = new HyperwalletExternalResourceManager();
    private AddTransferMethodPresenter presenter;
    @Mock
    private TransferMethodConfigurationRepository tmcRepository;
    @Mock
    private TransferMethodRepository tmRepository;
    @Mock
    private AddTransferMethodContract.View view;
    @Captor
    private ArgumentCaptor<List<FieldGroup>> fieldArgumentCaptor;
    @Captor
    private ArgumentCaptor<List<Error>> mErrorListArgumentCaptor;

    @Before
    public void setUp() {
        presenter = new AddTransferMethodPresenter(view, tmcRepository, tmRepository);
    }

    @Test
    public void testCreateTransferMethod_returnsTokenOnSuccess() {
        // When
        when(view.isActive()).thenReturn(true);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodRepository.LoadTransferMethodCallback callback =
                        (TransferMethodRepository.LoadTransferMethodCallback) invocation.getArguments()[1];
                callback.onTransferMethodLoaded(bankAccount);
                return callback;
            }
        }).when(tmRepository).createTransferMethod(any(TransferMethod.class),
                any(TransferMethodRepository.LoadTransferMethodCallback.class));

        // Then
        presenter.createTransferMethod(bankAccount);

        verify(view, never()).showErrorLoadTransferMethodConfigurationFields(
                ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorAddTransferMethod(ArgumentMatchers.<Error>anyList());
        verify(view, atLeastOnce()).notifyTransferMethodAdded(any(BankAccount.class));
    }

    @Test
    public void testCreateTransferMethod_showsErrorOnFailure() {
        // When
        when(view.isActive()).thenReturn(true);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodRepository.LoadTransferMethodCallback callback =
                        (TransferMethodRepository.LoadTransferMethodCallback) invocation.getArguments()[1];
                callback.onError(errors);
                return callback;
            }
        }).when(tmRepository).createTransferMethod(any(TransferMethod.class),
                any(TransferMethodRepository.LoadTransferMethodCallback.class));

        // Then
        presenter.createTransferMethod(bankAccount);

        verify(view, atLeastOnce()).showErrorAddTransferMethod(mErrorListArgumentCaptor.capture());
        verify(view, never()).notifyTransferMethodAdded(any(TransferMethod.class));
        List<Error> capturedList = mErrorListArgumentCaptor.getValue();
        assertNotNull(capturedList);
        assertThat(capturedList, hasItems(errors.getErrors().get(0)));
    }

    @Test
    public void testLoadTransferMethodConfigurationFields_loadsTransferMethodFieldsIntoViewOnSuccess()
            throws Exception {
        // Given
        final HyperwalletTransferMethodConfigurationField result = mock(
                HyperwalletTransferMethodConfigurationField.class);

        JSONObject jsonObject = new JSONObject(
                externalResourceManager.getResourceContent("add_transfer_method_presenter_test.json"));
        final TransferMethodConfiguration configuration = new TransferMethodConfiguration(
                jsonObject);

        // When
        when(view.isActive()).thenReturn(true);
        when(result.getFields()).thenReturn(configuration);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodConfigurationRepository.LoadFieldsCallback callback =
                        (TransferMethodConfigurationRepository.LoadFieldsCallback) invocation.getArguments()[4];
                callback.onFieldsLoaded(result);
                return callback;
            }
        }).when(tmcRepository).getFields(anyString(), anyString(), anyString(), anyString(),
                any(TransferMethodConfigurationRepository.LoadFieldsCallback.class));

        // Then
        presenter.loadTransferMethodConfigurationFields(false, "CA", "CAD", "BANK_ACCOUNT", INDIVIDUAL);
        LATCH.await(AWAIT_TIME_MS, TimeUnit.MILLISECONDS);

        verify(view).showTransferMethodFields(fieldArgumentCaptor.capture());
        verify(view, never()).showErrorLoadTransferMethodConfigurationFields(
                ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorAddTransferMethod(ArgumentMatchers.<Error>anyList());
        List<FieldGroup> responseFields = fieldArgumentCaptor.getValue();
        assertThat(responseFields, hasItem(configuration.getFieldGroups().get(0)));
    }

    @Test
    public void testLoadTransferMethodConfigurationFields_showsErrorOnFailure() throws Exception {
        // When
        when(view.isActive()).thenReturn(true);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodConfigurationRepository.LoadFieldsCallback callback =
                        (TransferMethodConfigurationRepository.LoadFieldsCallback) invocation.getArguments()[4];
                callback.onError(errors);
                return callback;
            }
        }).when(tmcRepository).getFields(anyString(), anyString(), anyString(), anyString(),
                any(TransferMethodConfigurationRepository.LoadFieldsCallback.class));

        // Then
        presenter.loadTransferMethodConfigurationFields(false, "CA", "CAD", "BANK_ACCOUNT", INDIVIDUAL);
        LATCH.await(AWAIT_TIME_MS, TimeUnit.MILLISECONDS);

        verify(view, never()).showTransferMethodFields(ArgumentMatchers.<FieldGroup>anyList());
        verify(view, atLeastOnce()).showErrorLoadTransferMethodConfigurationFields(mErrorListArgumentCaptor.capture());
        assertThat(mErrorListArgumentCaptor.getValue(), hasItem(errors.getErrors().get(0)));
    }

    @Test
    public void testLoadTransferMethodConfigurationFields_updatesFieldsWhenForceUpdateIsTrue() {
        presenter.loadTransferMethodConfigurationFields(true, "CA", "CAD", "BANK_ACCOUNT", INDIVIDUAL);

        verify(tmcRepository, atLeastOnce()).refreshFields();
        verify(tmcRepository, atLeastOnce()).getFields(anyString(), anyString(), anyString(), anyString(),
                any(TransferMethodConfigurationRepository.LoadFieldsCallback.class));
        verify(view, never()).showTransferMethodFields(ArgumentMatchers.<FieldGroup>anyList());
        verify(view, never()).showErrorLoadTransferMethodConfigurationFields(
                ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorAddTransferMethod(ArgumentMatchers.<Error>anyList());
    }

    @Test
    public void testLoadTransferMethodConfigurationFields_loadsTransferMethodFieldsInactive() {
        // When
        when(view.isActive()).thenReturn(false);

        // Then
        presenter.loadTransferMethodConfigurationFields(true, "CA", "CAD", "BANK_ACCOUNT", INDIVIDUAL);

        verify(tmcRepository, atLeastOnce()).refreshFields();
        verify(tmcRepository, atLeastOnce()).getFields(anyString(), anyString(), anyString(), anyString(),
                any(TransferMethodConfigurationRepository.LoadFieldsCallback.class));
        verify(view, never()).hideProgressBar();
        verify(view, never()).showTransferMethodFields(ArgumentMatchers.<FieldGroup>anyList());
        verify(view, never()).showTransactionInformation(ArgumentMatchers.<Fee>anyList(),
                any(ProcessingTime.class));
    }

    @Test
    public void testLoadTransferMethodConfigurationFields_loadsTransferMethodFieldsErrorInactive()
            throws InterruptedException {
        // When
        when(view.isActive()).thenReturn(false);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodConfigurationRepository.LoadFieldsCallback callback =
                        (TransferMethodConfigurationRepository.LoadFieldsCallback) invocation.getArguments()[4];
                callback.onError(errors);
                return callback;
            }
        }).when(tmcRepository).getFields(anyString(), anyString(), anyString(), anyString(),
                any(TransferMethodConfigurationRepository.LoadFieldsCallback.class));

        // Then
        presenter.loadTransferMethodConfigurationFields(false, "CA", "CAD", "BANK_ACCOUNT", INDIVIDUAL);
        LATCH.await(AWAIT_TIME_MS, TimeUnit.MILLISECONDS);

        verify(view, never()).hideProgressBar();
        verify(view, never()).showErrorLoadTransferMethodConfigurationFields(
                ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorAddTransferMethod(ArgumentMatchers.<Error>anyList());
    }

    @Test
    public void testCreateTransferMethod_returnsTokenOnSuccessInactive() {
        // When
        when(view.isActive()).thenReturn(false);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodRepository.LoadTransferMethodCallback callback =
                        (TransferMethodRepository.LoadTransferMethodCallback) invocation.getArguments()[1];
                callback.onTransferMethodLoaded(bankAccount);
                return callback;
            }
        }).when(tmRepository).createTransferMethod(any(TransferMethod.class),
                any(TransferMethodRepository.LoadTransferMethodCallback.class));

        // Then
        presenter.createTransferMethod(bankAccount);

        verify(view, never()).hideCreateButtonProgressBar();
        verify(view, never()).notifyTransferMethodAdded(any(TransferMethod.class));
    }

    @Test
    public void testCreateTransferMethod_returnsTokenOnSuccessErrorInactive() {
        // When
        when(view.isActive()).thenReturn(false);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodRepository.LoadTransferMethodCallback callback =
                        (TransferMethodRepository.LoadTransferMethodCallback) invocation.getArguments()[1];
                callback.onError(errors);
                return callback;
            }
        }).when(tmRepository).createTransferMethod(any(TransferMethod.class),
                any(TransferMethodRepository.LoadTransferMethodCallback.class));

        // Then
        presenter.createTransferMethod(bankAccount);

        verify(view, never()).hideCreateButtonProgressBar();
        verify(view, never()).showInputErrors(ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorLoadTransferMethodConfigurationFields(
                ArgumentMatchers.<Error>anyList());
        verify(view, never()).showErrorAddTransferMethod(ArgumentMatchers.<Error>anyList());
    }

    @Test
    public void testHandleUnmappedFieldError_hasFieldErrorUnmapped() {
        Map<String, Object> widgetState = new HashMap<>();
        widgetState.put("fieldOne", new Object());
        List<Error> errorList = new ArrayList<Error>() {{
            add(new Error("Test Error", "fieldTwo", "ERROR_UNMAPPED_FIELD"));
            add(new Error("Test Error", "fieldThree", "ERROR_UNMAPPED_FIELD"));
        }};

        // test
        presenter.handleUnmappedFieldError(widgetState, errorList);

        verify(view, atLeastOnce()).showErrorAddTransferMethod(ArgumentMatchers.<Error>anyList());
    }

    @Test
    public void testHandleUnmappedFieldError_fieldsAreMapped() {
        Map<String, Object> widgetState = new HashMap<>();
        widgetState.put("fieldOne", new Object());
        List<Error> errorList = new ArrayList<Error>() {{
            add(new Error("Test Error", "fieldOne", "ERROR_UNMAPPED_FIELD"));
        }};

        // test
        presenter.handleUnmappedFieldError(widgetState, errorList);

        verify(view, never()).showErrorAddTransferMethod(ArgumentMatchers.<Error>anyList());
    }

    private Errors createErrors() {
        List<Error> errors = new ArrayList<>();
        Error error = new Error("test message", "TEST_CODE");
        errors.add(error);
        return new Errors(errors);
    }
}
