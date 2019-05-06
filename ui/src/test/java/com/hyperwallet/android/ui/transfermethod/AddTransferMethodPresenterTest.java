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

import com.hyperwallet.android.model.HyperwalletBankAccount;
import com.hyperwallet.android.model.HyperwalletError;
import com.hyperwallet.android.model.HyperwalletErrors;
import com.hyperwallet.android.model.HyperwalletTransferMethod;
import com.hyperwallet.android.model.meta.HyperwalletFee;
import com.hyperwallet.android.model.meta.HyperwalletTransferMethodConfigurationField;
import com.hyperwallet.android.model.meta.field.HyperwalletFieldGroup;
import com.hyperwallet.android.model.meta.field.HyperwalletTransferMethodConfiguration;
import com.hyperwallet.android.ui.repository.TransferMethodConfigurationRepository;
import com.hyperwallet.android.ui.repository.TransferMethodRepository;
import com.hyperwallet.android.ui.rule.HyperwalletExternalResourceManager;

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
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(RobolectricTestRunner.class)
public class AddTransferMethodPresenterTest {

    private final static CountDownLatch LATCH = new CountDownLatch(1);
    private final static int AWAIT_TIME_MS = 50;

    private final HyperwalletErrors errors = createErrors();
    private final HyperwalletBankAccount bankAccount = new HyperwalletBankAccount
            .Builder("CA", "CAD", "3423423432")
            .build();
    private AddTransferMethodPresenter presenter;

    @Mock
    private TransferMethodConfigurationRepository tmcRepository;
    @Mock
    private TransferMethodRepository tmRepository;
    @Mock
    private AddTransferMethodContract.View view;
    @Captor
    private ArgumentCaptor<List<HyperwalletFieldGroup>> fieldArgumentCaptor;
    @Captor
    private ArgumentCaptor<List<HyperwalletError>> mErrorListArgumentCaptor;
    @Rule
    public MockitoRule mockito = MockitoJUnit.rule();
    @Rule
    public HyperwalletExternalResourceManager externalResourceManager = new HyperwalletExternalResourceManager();

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
        }).when(tmRepository).createTransferMethod(any(HyperwalletTransferMethod.class),
                any(TransferMethodRepository.LoadTransferMethodCallback.class));

        // Then
        presenter.createTransferMethod(bankAccount);

        verify(view, never()).showErrorLoadTransferMethodConfigurationFields(
                ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showErrorAddTransferMethod(ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, atLeastOnce()).notifyTransferMethodAdded(any(HyperwalletBankAccount.class));
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
        }).when(tmRepository).createTransferMethod(any(HyperwalletTransferMethod.class),
                any(TransferMethodRepository.LoadTransferMethodCallback.class));

        // Then
        presenter.createTransferMethod(bankAccount);

        verify(view, atLeastOnce()).showErrorAddTransferMethod(mErrorListArgumentCaptor.capture());
        verify(view, never()).notifyTransferMethodAdded(any(HyperwalletTransferMethod.class));
        List<HyperwalletError> capturedList = mErrorListArgumentCaptor.getValue();
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
        final HyperwalletTransferMethodConfiguration configuration = new HyperwalletTransferMethodConfiguration(
                jsonObject);

        // When
        when(view.isActive()).thenReturn(true);
        when(result.getFields()).thenReturn(configuration);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodConfigurationRepository.LoadFieldsCallback callback =
                        (TransferMethodConfigurationRepository.LoadFieldsCallback) invocation.getArguments()[3];
                callback.onFieldsLoaded(result, "");
                return callback;
            }
        }).when(tmcRepository).getFields(anyString(), anyString(), anyString(),
                any(TransferMethodConfigurationRepository.LoadFieldsCallback.class));

        // Then
        presenter.loadTransferMethodConfigurationFields(false, "CA", "CAD", "BANK_ACCOUNT");
        LATCH.await(AWAIT_TIME_MS, TimeUnit.MILLISECONDS);

        verify(view).showTransferMethodFields(fieldArgumentCaptor.capture());
        verify(view, never()).showErrorLoadTransferMethodConfigurationFields(
                ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showErrorAddTransferMethod(ArgumentMatchers.<HyperwalletError>anyList());
        List<HyperwalletFieldGroup> responseFields = fieldArgumentCaptor.getValue();
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
                        (TransferMethodConfigurationRepository.LoadFieldsCallback) invocation.getArguments()[3];
                callback.onError(errors);
                return callback;
            }
        }).when(tmcRepository).getFields(anyString(), anyString(), anyString(),
                any(TransferMethodConfigurationRepository.LoadFieldsCallback.class));

        // Then
        presenter.loadTransferMethodConfigurationFields(false, "CA", "CAD", "BANK_ACCOUNT");
        LATCH.await(AWAIT_TIME_MS, TimeUnit.MILLISECONDS);

        verify(view, never()).showTransferMethodFields(ArgumentMatchers.<HyperwalletFieldGroup>anyList());
        verify(view, atLeastOnce()).showErrorLoadTransferMethodConfigurationFields(mErrorListArgumentCaptor.capture());
        assertThat(mErrorListArgumentCaptor.getValue(), hasItem(errors.getErrors().get(0)));
    }

    @Test
    public void testLoadTransferMethodConfigurationFields_updatesFieldsWhenForceUpdateIsTrue() {
        presenter.loadTransferMethodConfigurationFields(true, "CA", "CAD", "BANK_ACCOUNT");

        verify(tmcRepository, atLeastOnce()).refreshFields();
        verify(tmcRepository, atLeastOnce()).getFields(anyString(), anyString(), anyString(),
                any(TransferMethodConfigurationRepository.LoadFieldsCallback.class));
        verify(view, never()).showTransferMethodFields(ArgumentMatchers.<HyperwalletFieldGroup>anyList());
        verify(view, never()).showErrorLoadTransferMethodConfigurationFields(
                ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showErrorAddTransferMethod(ArgumentMatchers.<HyperwalletError>anyList());
    }

    @Test
    public void testLoadTransferMethodConfigurationFields_loadsTransferMethodFieldsInactive() {
        // When
        when(view.isActive()).thenReturn(false);

        // Then
        presenter.loadTransferMethodConfigurationFields(true, "CA", "CAD", "BANK_ACCOUNT");

        verify(tmcRepository, atLeastOnce()).refreshFields();
        verify(tmcRepository, atLeastOnce()).getFields(anyString(), anyString(), anyString(),
                any(TransferMethodConfigurationRepository.LoadFieldsCallback.class));
        verify(view, never()).hideProgressBar();
        verify(view, never()).showTransferMethodFields(ArgumentMatchers.<HyperwalletFieldGroup>anyList());
        verify(view, never()).showTransactionInformation(ArgumentMatchers.<HyperwalletFee>anyList(), anyString());
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
                        (TransferMethodConfigurationRepository.LoadFieldsCallback) invocation.getArguments()[3];
                callback.onError(errors);
                return callback;
            }
        }).when(tmcRepository).getFields(anyString(), anyString(), anyString(),
                any(TransferMethodConfigurationRepository.LoadFieldsCallback.class));

        // Then
        presenter.loadTransferMethodConfigurationFields(false, "CA", "CAD", "BANK_ACCOUNT");
        LATCH.await(AWAIT_TIME_MS, TimeUnit.MILLISECONDS);

        verify(view, never()).hideProgressBar();
        verify(view, never()).showErrorLoadTransferMethodConfigurationFields(
                ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showErrorAddTransferMethod(ArgumentMatchers.<HyperwalletError>anyList());
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
        }).when(tmRepository).createTransferMethod(any(HyperwalletTransferMethod.class),
                any(TransferMethodRepository.LoadTransferMethodCallback.class));

        // Then
        presenter.createTransferMethod(bankAccount);

        verify(view, never()).hideCreateButtonProgressBar();
        verify(view, never()).notifyTransferMethodAdded(any(HyperwalletTransferMethod.class));
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
        }).when(tmRepository).createTransferMethod(any(HyperwalletTransferMethod.class),
                any(TransferMethodRepository.LoadTransferMethodCallback.class));

        // Then
        presenter.createTransferMethod(bankAccount);

        verify(view, never()).hideCreateButtonProgressBar();
        verify(view, never()).showInputErrors(ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showErrorLoadTransferMethodConfigurationFields(
                ArgumentMatchers.<HyperwalletError>anyList());
        verify(view, never()).showErrorAddTransferMethod(ArgumentMatchers.<HyperwalletError>anyList());
    }

    private HyperwalletErrors createErrors() {
        List<HyperwalletError> errors = new ArrayList<>();
        HyperwalletError error = new HyperwalletError("test message", "TEST_CODE");
        errors.add(error);
        return new HyperwalletErrors(errors);
    }
}
