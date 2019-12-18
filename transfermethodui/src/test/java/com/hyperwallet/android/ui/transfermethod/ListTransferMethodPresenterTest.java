/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Hyperwallet Systems Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.hyperwallet.android.ui.transfermethod;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.BANK_ACCOUNT_ID;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.TRANSFER_METHOD_COUNTRY;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.TRANSFER_METHOD_CURRENCY;

import com.hyperwallet.android.model.Error;
import com.hyperwallet.android.model.Errors;
import com.hyperwallet.android.model.StatusTransition;
import com.hyperwallet.android.model.transfermethod.BankAccount;
import com.hyperwallet.android.model.transfermethod.TransferMethod;
import com.hyperwallet.android.ui.transfermethod.repository.TransferMethodRepository;
import com.hyperwallet.android.ui.transfermethod.view.ListTransferMethodContract;
import com.hyperwallet.android.ui.transfermethod.view.ListTransferMethodPresenter;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;

public class ListTransferMethodPresenterTest {

    private final Errors errors = createErrors();
    private final BankAccount bankAccount = new BankAccount
            .Builder("CA", "CAD", "3423423432")
            .build();
    private final StatusTransition statusTransition = new StatusTransition.Builder()
            .transition(StatusTransition.StatusDefinition.DE_ACTIVATED)
            .build();
    @Rule
    public MockitoRule mMockito = MockitoJUnit.rule();
    @Mock
    private TransferMethodRepository mTransferMethodRepository;
    @Mock
    private ListTransferMethodContract.View mView;
    @Captor
    private ArgumentCaptor<List<Error>> mListArgumentErrorCaptor;
    @Captor
    private ArgumentCaptor<List<TransferMethod>> mListArgumentTransferMethodCaptor;
    private ListTransferMethodPresenter presenter;

    @Before
    public void setUp() {
        presenter = new ListTransferMethodPresenter(mTransferMethodRepository, mView);
    }

    @Test
    public void testLoadTransferMethods_hasTransferMethodAccounts() {
        // When
        when(mView.isActive()).thenReturn(true);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodRepository.LoadTransferMethodListCallback callback =
                        (TransferMethodRepository.LoadTransferMethodListCallback) invocation.getArguments()[0];
                List<TransferMethod> list = new ArrayList<>();
                list.add(bankAccount);
                callback.onTransferMethodListLoaded(list);
                return callback;
            }
        }).when(mTransferMethodRepository).loadTransferMethods(any(
                TransferMethodRepository.LoadTransferMethodListCallback.class));

        // Then
        presenter.loadTransferMethods();

        verify(mTransferMethodRepository, atLeastOnce()).loadTransferMethods(
                any(TransferMethodRepository.LoadTransferMethodListCallback.class));
        verify(mTransferMethodRepository, never()).deactivateTransferMethod(any(TransferMethod.class),
                any(TransferMethodRepository.DeactivateTransferMethodCallback.class));
        verify(mTransferMethodRepository, never()).createTransferMethod(any(TransferMethod.class),
                any(TransferMethodRepository.LoadTransferMethodCallback.class));
        verify(mView, never()).showErrorDeactivateTransferMethod(ArgumentMatchers.<Error>anyList());
        verify(mView, never()).showErrorListTransferMethods(ArgumentMatchers.<Error>anyList());
        verify(mView, never()).notifyTransferMethodDeactivated(any(StatusTransition.class));
        verify(mView, never()).initiateAddTransferMethodFlow();
        verify(mView, never()).initiateAddTransferMethodFlowResult();
        verify(mView, atLeastOnce()).displayTransferMethods(mListArgumentTransferMethodCaptor.capture());

        List<TransferMethod> capturedList = mListArgumentTransferMethodCaptor.getValue();
        assertThat(capturedList, hasSize(1));
        assertThat(capturedList.get(0).getField(TRANSFER_METHOD_CURRENCY), is("CAD"));
        assertThat(capturedList.get(0).getField(TRANSFER_METHOD_COUNTRY), is("CA"));
        assertThat(capturedList.get(0).getField(BANK_ACCOUNT_ID), is("3423423432"));
    }

    @Test
    public void testLoadTransferMethods_hasTransferMethodAccountsInactive() {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodRepository.LoadTransferMethodListCallback callback =
                        (TransferMethodRepository.LoadTransferMethodListCallback) invocation.getArguments()[0];
                List<TransferMethod> bankAccounts = new ArrayList<>();
                bankAccounts.add(mock(BankAccount.class));
                callback.onTransferMethodListLoaded(bankAccounts);
                return callback;
            }
        }).when(mTransferMethodRepository).loadTransferMethods(any(
                TransferMethodRepository.LoadTransferMethodListCallback.class));

        // test
        when(mView.isActive()).thenReturn(false);
        presenter.loadTransferMethods();

        verify(mView, never()).showErrorDeactivateTransferMethod(ArgumentMatchers.<Error>anyList());
        verify(mView, never()).showErrorListTransferMethods(ArgumentMatchers.<Error>anyList());
        verify(mView, never()).notifyTransferMethodDeactivated(any(StatusTransition.class));
        verify(mView, never()).initiateAddTransferMethodFlow();
        verify(mView, never()).initiateAddTransferMethodFlowResult();
        verify(mView, never()).displayTransferMethods(mListArgumentTransferMethodCaptor.capture());
    }

    @Test
    public void testLoadTransferMethods_doesNotHaveTransferMethodAccounts() {
        // When
        when(mView.isActive()).thenReturn(true);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodRepository.LoadTransferMethodListCallback callback =
                        (TransferMethodRepository.LoadTransferMethodListCallback) invocation.getArguments()[0];
                callback.onTransferMethodListLoaded(null);
                return callback;
            }
        }).when(mTransferMethodRepository).loadTransferMethods(any(
                TransferMethodRepository.LoadTransferMethodListCallback.class));

        // Then
        presenter.loadTransferMethods();

        verify(mTransferMethodRepository, atLeastOnce()).loadTransferMethods(
                any(TransferMethodRepository.LoadTransferMethodListCallback.class));
        verify(mTransferMethodRepository, never()).deactivateTransferMethod(any(TransferMethod.class),
                any(TransferMethodRepository.DeactivateTransferMethodCallback.class));
        verify(mTransferMethodRepository, never()).createTransferMethod(any(TransferMethod.class),
                any(TransferMethodRepository.LoadTransferMethodCallback.class));
        verify(mView, never()).showErrorDeactivateTransferMethod(ArgumentMatchers.<Error>anyList());
        verify(mView, never()).showErrorListTransferMethods(ArgumentMatchers.<Error>anyList());
        verify(mView, never()).notifyTransferMethodDeactivated(any(StatusTransition.class));
        verify(mView, never()).initiateAddTransferMethodFlow();
        verify(mView, never()).initiateAddTransferMethodFlowResult();
        verify(mView, atLeastOnce()).displayTransferMethods(mListArgumentTransferMethodCaptor.capture());

        List<TransferMethod> capturedList = mListArgumentTransferMethodCaptor.getValue();
        assertThat(capturedList, is(nullValue()));
    }

    @Test
    public void testLoadTransferMethods_hasErrors() {
        // When
        when(mView.isActive()).thenReturn(true);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodRepository.LoadTransferMethodListCallback callback =
                        (TransferMethodRepository.LoadTransferMethodListCallback) invocation.getArguments()[0];
                callback.onError(errors);
                return callback;
            }
        }).when(mTransferMethodRepository).loadTransferMethods(any(
                TransferMethodRepository.LoadTransferMethodListCallback.class));

        // Then
        presenter.loadTransferMethods();

        verify(mTransferMethodRepository, atLeastOnce()).loadTransferMethods(
                any(TransferMethodRepository.LoadTransferMethodListCallback.class));
        verify(mTransferMethodRepository, never()).deactivateTransferMethod(any(TransferMethod.class),
                any(TransferMethodRepository.DeactivateTransferMethodCallback.class));
        verify(mTransferMethodRepository, never()).createTransferMethod(any(TransferMethod.class),
                any(TransferMethodRepository.LoadTransferMethodCallback.class));
        verify(mView, never()).displayTransferMethods(ArgumentMatchers.<TransferMethod>anyList());
        verify(mView, never()).notifyTransferMethodDeactivated(any(StatusTransition.class));
        verify(mView, never()).initiateAddTransferMethodFlow();
        verify(mView, never()).initiateAddTransferMethodFlowResult();
        verify(mView, atLeastOnce()).showErrorListTransferMethods(mListArgumentErrorCaptor.capture());

        List<Error> capturedList = mListArgumentErrorCaptor.getValue();
        assertThat(capturedList, hasSize(1));
        assertThat(capturedList, hasItem(errors.getErrors().get(0)));
    }

    @Test
    public void testLoadTransferMethods_hasErrorsInActivate() {
        // When
        when(mView.isActive()).thenReturn(false);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodRepository.LoadTransferMethodListCallback callback =
                        (TransferMethodRepository.LoadTransferMethodListCallback) invocation.getArguments()[0];
                Errors errors = mock(Errors.class);
                callback.onError(errors);
                return callback;
            }
        }).when(mTransferMethodRepository).loadTransferMethods(any(
                TransferMethodRepository.LoadTransferMethodListCallback.class));

        // Then
        presenter.loadTransferMethods();
        verify(mView, never()).hideProgressBar();
        verify(mView, never()).showErrorDeactivateTransferMethod(ArgumentMatchers.<Error>anyList());
        verify(mView, never()).showErrorListTransferMethods(ArgumentMatchers.<Error>anyList());
    }

    @Test
    public void testDeactivateTransferMethod_successful() {
        // When
        when(mView.isActive()).thenReturn(true);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodRepository.DeactivateTransferMethodCallback callback =
                        (TransferMethodRepository.DeactivateTransferMethodCallback) invocation.getArguments()[1];
                callback.onTransferMethodDeactivated(statusTransition);
                return callback;
            }
        }).when(mTransferMethodRepository).deactivateTransferMethod(any(TransferMethod.class),
                any(TransferMethodRepository.DeactivateTransferMethodCallback.class));

        // Then
        presenter.deactivateTransferMethod(bankAccount);

        verify(mTransferMethodRepository, atLeastOnce()).deactivateTransferMethod(any(TransferMethod.class),
                any(TransferMethodRepository.DeactivateTransferMethodCallback.class));
        verify(mTransferMethodRepository, never()).loadTransferMethods(
                any(TransferMethodRepository.LoadTransferMethodListCallback.class));
        verify(mTransferMethodRepository, never()).createTransferMethod(any(TransferMethod.class),
                any(TransferMethodRepository.LoadTransferMethodCallback.class));
        verify(mView, never()).displayTransferMethods(ArgumentMatchers.<TransferMethod>anyList());
        verify(mView, never()).initiateAddTransferMethodFlow();
        verify(mView, never()).initiateAddTransferMethodFlowResult();
        verify(mView, never()).showErrorDeactivateTransferMethod(ArgumentMatchers.<Error>anyList());
        verify(mView, never()).showErrorListTransferMethods(ArgumentMatchers.<Error>anyList());
        verify(mView, atLeastOnce()).notifyTransferMethodDeactivated(any(StatusTransition.class));
    }

    @Test
    public void testDeactivateTransferMethod_successfulInactive() {
        // When
        when(mView.isActive()).thenReturn(false);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodRepository.DeactivateTransferMethodCallback callback =
                        (TransferMethodRepository.DeactivateTransferMethodCallback) invocation.getArguments()[1];
                callback.onTransferMethodDeactivated(statusTransition);
                return callback;
            }
        }).when(mTransferMethodRepository).deactivateTransferMethod(any(TransferMethod.class),
                any(TransferMethodRepository.DeactivateTransferMethodCallback.class));

        // Then
        presenter.deactivateTransferMethod(bankAccount);
        verify(mView, never()).hideProgressBar();
        verify(mView, never()).displayTransferMethods(ArgumentMatchers.<TransferMethod>anyList());
        verify(mView, never()).initiateAddTransferMethodFlow();
        verify(mView, never()).initiateAddTransferMethodFlowResult();
        verify(mView, never()).showErrorDeactivateTransferMethod(ArgumentMatchers.<Error>anyList());
        verify(mView, never()).showErrorListTransferMethods(ArgumentMatchers.<Error>anyList());
        verify(mView, never()).notifyTransferMethodDeactivated(any(StatusTransition.class));
    }

    @Test
    public void testDeactivateTransferMethod_hasErrors() {
        // When
        when(mView.isActive()).thenReturn(true);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodRepository.DeactivateTransferMethodCallback callback =
                        (TransferMethodRepository.DeactivateTransferMethodCallback) invocation.getArguments()[1];
                callback.onError(errors);
                return callback;
            }
        }).when(mTransferMethodRepository).deactivateTransferMethod(any(TransferMethod.class),
                any(TransferMethodRepository.DeactivateTransferMethodCallback.class));

        // Then
        presenter.deactivateTransferMethod(bankAccount);

        verify(mTransferMethodRepository, atLeastOnce()).deactivateTransferMethod((TransferMethod) any(),
                any(TransferMethodRepository.DeactivateTransferMethodCallback.class));
        verify(mTransferMethodRepository, never()).loadTransferMethods(
                any(TransferMethodRepository.LoadTransferMethodListCallback.class));
        verify(mTransferMethodRepository, never()).createTransferMethod(any(TransferMethod.class),
                any(TransferMethodRepository.LoadTransferMethodCallback.class));
        verify(mView, never()).displayTransferMethods(ArgumentMatchers.<TransferMethod>anyList());
        verify(mView, never()).initiateAddTransferMethodFlow();
        verify(mView, never()).initiateAddTransferMethodFlowResult();
        verify(mView, never()).notifyTransferMethodDeactivated(any(StatusTransition.class));
        verify(mView, atLeastOnce()).showErrorDeactivateTransferMethod(mListArgumentErrorCaptor.capture());

        List<Error> capturedList = mListArgumentErrorCaptor.getValue();
        assertThat(capturedList, hasSize(1));
        assertThat(capturedList, hasItem(errors.getErrors().get(0)));
    }

    @Test
    public void testDeactivateTransferMethod_hasErrorsInactive() {
        // When
        when(mView.isActive()).thenReturn(false);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferMethodRepository.DeactivateTransferMethodCallback callback =
                        (TransferMethodRepository.DeactivateTransferMethodCallback) invocation.getArguments()[1];
                callback.onError(errors);
                return callback;
            }
        }).when(mTransferMethodRepository).deactivateTransferMethod(any(TransferMethod.class),
                any(TransferMethodRepository.DeactivateTransferMethodCallback.class));

        // Then
        presenter.deactivateTransferMethod(bankAccount);

        verify(mView, never()).hideProgressBar();
        verify(mView, never()).displayTransferMethods(ArgumentMatchers.<TransferMethod>anyList());
        verify(mView, never()).initiateAddTransferMethodFlow();
        verify(mView, never()).initiateAddTransferMethodFlowResult();
        verify(mView, never()).notifyTransferMethodDeactivated(any(StatusTransition.class));
        verify(mView, never()).showErrorDeactivateTransferMethod(ArgumentMatchers.<Error>anyList());
        verify(mView, never()).showErrorListTransferMethods(ArgumentMatchers.<Error>anyList());
    }

    private Errors createErrors() {
        List<Error> errors = new ArrayList<>();
        Error error = new Error("test message", "TEST_CODE");
        errors.add(error);
        return new Errors(errors);
    }
}
