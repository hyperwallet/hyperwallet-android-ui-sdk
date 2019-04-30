package com.hyperwallet.android.ui.repository;

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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import static com.hyperwallet.android.model.HyperwalletStatusTransition.StatusDefinition.DE_ACTIVATED;
import static com.hyperwallet.android.model.HyperwalletTransferMethod.TransferMethodFields.BANK_ACCOUNT_ID;
import static com.hyperwallet.android.model.HyperwalletTransferMethod.TransferMethodFields.BANK_NAME;
import static com.hyperwallet.android.model.HyperwalletTransferMethod.TransferMethodFields.STATUS;
import static com.hyperwallet.android.model.HyperwalletTransferMethod.TransferMethodFields.TRANSFER_METHOD_COUNTRY;
import static com.hyperwallet.android.model.HyperwalletTransferMethod.TransferMethodFields.TRANSFER_METHOD_CURRENCY;
import static com.hyperwallet.android.model.HyperwalletTransferMethod.TransferMethodFields.TYPE;

import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.exception.HyperwalletException;
import com.hyperwallet.android.listener.HyperwalletListener;
import com.hyperwallet.android.model.HyperwalletBankAccount;
import com.hyperwallet.android.model.HyperwalletBankCard;
import com.hyperwallet.android.model.HyperwalletError;
import com.hyperwallet.android.model.HyperwalletErrors;
import com.hyperwallet.android.model.HyperwalletStatusTransition;
import com.hyperwallet.android.model.HyperwalletTransferMethod;
import com.hyperwallet.android.model.HyperwalletTransferMethodPagination;
import com.hyperwallet.android.model.paging.HyperwalletPageList;

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

import java.util.ArrayList;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
public class TransferMethodRepositoryImplTest {

    @Mock
    private Hyperwallet mHyperwallet;
    @Captor
    private ArgumentCaptor<HyperwalletErrors> mErrorsArgumentCaptor;
    @Captor
    private ArgumentCaptor<HyperwalletBankAccount> mBankAccountArgumentCaptor;
    @Captor
    private ArgumentCaptor<HyperwalletBankCard> mBankCardArgumentCaptor;
    @Captor
    private ArgumentCaptor<HyperwalletStatusTransition> mStatusTransitionArgumentCaptor;
    @Captor
    private ArgumentCaptor<List<HyperwalletTransferMethod>> mListTransferMethodCaptor;
    @Rule
    public ExpectedException mThrown = ExpectedException.none();
    @Rule
    public MockitoRule mMockito = MockitoJUnit.rule();
    @Spy
    TransferMethodRepositoryImpl mTransferMethodRepository;

    @Before
    public void setup() {
        doReturn(mHyperwallet).when(mTransferMethodRepository).getHyperwallet();
    }

    @Test
    public void testCreateTransferMethod_callsListenerWithBankAccountOnSuccess() {
        HyperwalletBankAccount bankAccount = new HyperwalletBankAccount
                .Builder("CA", "CAD", "3423423432")
                .build();

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[1];
                HyperwalletBankAccount returnedBank = new HyperwalletBankAccount
                        .Builder("CA", "CAD", "3423423432")
                        .bankName("Mock Bank Response")
                        .build();
                listener.onSuccess(returnedBank);
                return listener;
            }
        }).when(mHyperwallet).createBankAccount(any(HyperwalletBankAccount.class),
                ArgumentMatchers.<HyperwalletListener<HyperwalletBankAccount>>any());
        TransferMethodRepository.LoadTransferMethodCallback mockCallback = mock(
                TransferMethodRepository.LoadTransferMethodCallback.class);

        // test
        mTransferMethodRepository.createTransferMethod(bankAccount, mockCallback);

        verify(mockCallback).onTransferMethodLoaded(mBankAccountArgumentCaptor.capture());
        verify(mockCallback, never()).onError(any(HyperwalletErrors.class));

        HyperwalletBankAccount transferMethod = mBankAccountArgumentCaptor.getValue();
        assertThat(transferMethod, is(notNullValue()));
        assertThat(transferMethod.getField(TYPE), is(HyperwalletTransferMethod.TransferMethodTypes.BANK_ACCOUNT));
        assertThat(transferMethod.getField(BANK_NAME), is("Mock Bank Response"));
        assertThat(transferMethod.getField(TRANSFER_METHOD_COUNTRY), is("CA"));
        assertThat(transferMethod.getField(TRANSFER_METHOD_CURRENCY), is("CAD"));
        assertThat(transferMethod.getField(BANK_ACCOUNT_ID), is("3423423432"));
    }

    @Test
    public void testCreateTransferMethod_callsListenerWithErrorOnFailure() {
        HyperwalletBankAccount bankAccount = new HyperwalletBankAccount
                .Builder("US", "USD", "23432432")
                .build();

        final HyperwalletError error = new HyperwalletError("test message", "TEST_CODE");

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[1];
                List<HyperwalletError> errorList = new ArrayList<>();
                errorList.add(error);
                HyperwalletErrors errors = new HyperwalletErrors(errorList);
                HyperwalletException exception = new HyperwalletException(errors);
                listener.onFailure(exception);
                return listener;
            }
        }).when(mHyperwallet).createBankAccount(any(HyperwalletBankAccount.class),
                ArgumentMatchers.<HyperwalletListener<HyperwalletBankAccount>>any());
        TransferMethodRepository.LoadTransferMethodCallback mockCallback = mock(
                TransferMethodRepository.LoadTransferMethodCallback.class);

        // test
        mTransferMethodRepository.createTransferMethod(bankAccount, mockCallback);

        verify(mockCallback).onError(mErrorsArgumentCaptor.capture());
        verify(mockCallback, never()).onTransferMethodLoaded(any(HyperwalletTransferMethod.class));
        assertThat(mErrorsArgumentCaptor.getValue().getErrors(), hasItem(error));
    }

    @Test
    public void testLoadTransferMethod_userContextHasBankAccount() {
        HyperwalletBankAccount bankAccount = new HyperwalletBankAccount
                .Builder("CA", "CAD", "3423423432")
                .build();

        List<HyperwalletBankAccount> accounts = new ArrayList<>();
        accounts.add(bankAccount);
        final HyperwalletPageList<HyperwalletBankAccount> pageList = new HyperwalletPageList<>(accounts);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[1];
                listener.onSuccess(pageList);
                return listener;
            }
        }).when(mHyperwallet).listTransferMethods((HyperwalletTransferMethodPagination) any(),
                ArgumentMatchers.<HyperwalletListener<HyperwalletPageList<HyperwalletTransferMethod>>>any());
        TransferMethodRepository.LoadTransferMethodListCallback mockCallback = mock(
                TransferMethodRepository.LoadTransferMethodListCallback.class);

        // test
        mTransferMethodRepository.loadTransferMethod(mockCallback);

        verify(mockCallback).onTransferMethodListLoaded(mListTransferMethodCaptor.capture());
        verify(mockCallback, never()).onError(any(HyperwalletErrors.class));

        List<HyperwalletTransferMethod> transferMethods = mListTransferMethodCaptor.getValue();
        assertThat(transferMethods, hasSize(1));
        assertThat(transferMethods.get(0).getField(TRANSFER_METHOD_COUNTRY), is("CA"));
        assertThat(transferMethods.get(0).getField(TRANSFER_METHOD_CURRENCY), is("CAD"));
        assertThat(transferMethods.get(0).getField(BANK_ACCOUNT_ID), is("3423423432"));
    }

    @Test
    public void testLoadTransferMethod_userContextEmptyAccounts() {

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[1];
                listener.onSuccess(null);
                return listener;
            }
        }).when(mHyperwallet).listTransferMethods((HyperwalletTransferMethodPagination) any(),
                ArgumentMatchers.<HyperwalletListener<HyperwalletPageList<HyperwalletTransferMethod>>>any());
        TransferMethodRepository.LoadTransferMethodListCallback mockCallback = mock(
                TransferMethodRepository.LoadTransferMethodListCallback.class);

        // test
        mTransferMethodRepository.loadTransferMethod(mockCallback);

        verify(mockCallback).onTransferMethodListLoaded(mListTransferMethodCaptor.capture());
        verify(mockCallback, never()).onError(any(HyperwalletErrors.class));

        List<HyperwalletTransferMethod> transferMethods = mListTransferMethodCaptor.getValue();
        assertThat(transferMethods, is(nullValue()));
    }

    @Test
    public void testLoadTransferMethod_hasErrors() {

        final HyperwalletError error = new HyperwalletError("test message", "TEST_CODE");

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[1];
                List<HyperwalletError> errorList = new ArrayList<>();
                errorList.add(error);
                HyperwalletErrors errors = new HyperwalletErrors(errorList);
                listener.onFailure(new HyperwalletException(errors));
                return listener;
            }
        }).when(mHyperwallet).listTransferMethods((HyperwalletTransferMethodPagination) any(),
                ArgumentMatchers.<HyperwalletListener<HyperwalletPageList<HyperwalletTransferMethod>>>any());
        TransferMethodRepository.LoadTransferMethodListCallback mockCallback = mock(
                TransferMethodRepository.LoadTransferMethodListCallback.class);

        // test
        mTransferMethodRepository.loadTransferMethod(mockCallback);

        verify(mockCallback, never()).onTransferMethodListLoaded(ArgumentMatchers.<HyperwalletTransferMethod>anyList());
        verify(mockCallback).onError(mErrorsArgumentCaptor.capture());

        assertThat(mErrorsArgumentCaptor.getValue().getErrors(), hasItem(error));
    }

    @Test
    public void testDeactivateBankAccount_successfullyDeactivatedBankAccount() {
        HyperwalletBankAccount bankAccount = new HyperwalletBankAccount
                .Builder("CA", "CAD", "3423423432")
                .token("some token")
                .build();
        bankAccount.setField(STATUS, HyperwalletStatusTransition.StatusDefinition.ACTIVATED);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletStatusTransition statusTransition = new HyperwalletStatusTransition(DE_ACTIVATED);
                statusTransition.setNotes("Closing this account.");
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[2];
                listener.onSuccess(statusTransition);
                return listener;
            }
        }).when(mHyperwallet).deactivateBankAccount(anyString(), ArgumentMatchers.<String>isNull(),
                ArgumentMatchers.<HyperwalletListener<HyperwalletStatusTransition>>any());

        TransferMethodRepository.DeactivateTransferMethodCallback mockCallback = mock(
                TransferMethodRepository.DeactivateTransferMethodCallback.class);

        // test
        mTransferMethodRepository.deactivateTransferMethod(bankAccount, mockCallback);

        verify(mockCallback).onTransferMethodDeactivated(mStatusTransitionArgumentCaptor.capture());
        verify(mockCallback, never()).onError(any(HyperwalletErrors.class));

        HyperwalletStatusTransition statusTransition = mStatusTransitionArgumentCaptor.getValue();
        assertThat(statusTransition, is(notNullValue()));
        assertThat(statusTransition.getTransition(), is(DE_ACTIVATED));
        assertThat(statusTransition.getNotes(), is("Closing this account."));
    }

    @Test
    public void testDeactivateBankAccount_hasErrorOnDeactivate() {
        TransferMethodRepositoryImpl mTransferMethodRepository = spy(TransferMethodRepositoryImpl.class);
        doReturn(mHyperwallet).when(mTransferMethodRepository).getHyperwallet();

        HyperwalletBankAccount bankAccount = new HyperwalletBankAccount
                .Builder("CA", "CAD", "3423423432")
                .token("some token")
                .build();
        bankAccount.setField(STATUS, HyperwalletStatusTransition.StatusDefinition.ACTIVATED);
        final HyperwalletError error = new HyperwalletError("test message", "TEST_CODE");
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[2];
                List<HyperwalletError> errorList = new ArrayList<>();
                errorList.add(error);
                HyperwalletErrors errors = new HyperwalletErrors(errorList);
                listener.onFailure(new HyperwalletException(errors));
                return listener;
            }
        }).when(mHyperwallet).deactivateBankAccount(anyString(), ArgumentMatchers.<String>isNull(),
                ArgumentMatchers.<HyperwalletListener<HyperwalletStatusTransition>>any());
        TransferMethodRepository.DeactivateTransferMethodCallback mockCallback = mock(
                TransferMethodRepository.DeactivateTransferMethodCallback.class);

        // test
        mTransferMethodRepository.deactivateTransferMethod(bankAccount, mockCallback);

        verify(mockCallback, never()).onTransferMethodDeactivated(any(HyperwalletStatusTransition.class));
        verify(mockCallback).onError(mErrorsArgumentCaptor.capture());

        assertThat(mErrorsArgumentCaptor.getValue().getErrors(), hasItem(error));
    }

    @Test
    public void testDeactivateBankCard_successfullyDeactivatedBankCard() {
        HyperwalletBankCard bankCard = new HyperwalletBankCard
                .Builder("CA", "CAD", "1232345456784", "2019-05", "234")
                .token("some token")
                .build();
        bankCard.setField(STATUS, HyperwalletStatusTransition.StatusDefinition.ACTIVATED);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletStatusTransition statusTransition = new HyperwalletStatusTransition(DE_ACTIVATED);
                statusTransition.setNotes("Closing this account.");
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[2];
                listener.onSuccess(statusTransition);
                return listener;
            }
        }).when(mHyperwallet).deactivateBankCard(anyString(), ArgumentMatchers.<String>isNull(),
                ArgumentMatchers.<HyperwalletListener<HyperwalletStatusTransition>>any());

        TransferMethodRepository.DeactivateTransferMethodCallback mockCallback = mock(
                TransferMethodRepository.DeactivateTransferMethodCallback.class);

        // test
        mTransferMethodRepository.deactivateTransferMethod(bankCard, mockCallback);

        verify(mockCallback).onTransferMethodDeactivated(mStatusTransitionArgumentCaptor.capture());
        verify(mockCallback, never()).onError(any(HyperwalletErrors.class));

        HyperwalletStatusTransition statusTransition = mStatusTransitionArgumentCaptor.getValue();
        assertThat(statusTransition, is(notNullValue()));
        assertThat(statusTransition.getTransition(), is(DE_ACTIVATED));
        assertThat(statusTransition.getNotes(), is("Closing this account."));
    }

    @Test
    public void testDeactivateBankCard_hasErrorOnDeactivate() {
        TransferMethodRepositoryImpl mTransferMethodRepository = spy(TransferMethodRepositoryImpl.class);
        doReturn(mHyperwallet).when(mTransferMethodRepository).getHyperwallet();

        HyperwalletBankCard bankCard = new HyperwalletBankCard
                .Builder("CA", "CAD", "1232345456784", "2019-05", "234")
                .token("some token")
                .build();
        bankCard.setField(STATUS, HyperwalletStatusTransition.StatusDefinition.ACTIVATED);
        final HyperwalletError error = new HyperwalletError("test message", "TEST_CODE");
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[2];
                List<HyperwalletError> errorList = new ArrayList<>();
                errorList.add(error);
                HyperwalletErrors errors = new HyperwalletErrors(errorList);
                listener.onFailure(new HyperwalletException(errors));
                return listener;
            }
        }).when(mHyperwallet).deactivateBankCard(anyString(), ArgumentMatchers.<String>isNull(),
                ArgumentMatchers.<HyperwalletListener<HyperwalletStatusTransition>>any());
        TransferMethodRepository.DeactivateTransferMethodCallback mockCallback = mock(
                TransferMethodRepository.DeactivateTransferMethodCallback.class);

        // test
        mTransferMethodRepository.deactivateTransferMethod(bankCard, mockCallback);

        verify(mockCallback, never()).onTransferMethodDeactivated(any(HyperwalletStatusTransition.class));
        verify(mockCallback).onError(mErrorsArgumentCaptor.capture());

        assertThat(mErrorsArgumentCaptor.getValue().getErrors(), hasItem(error));
    }


    @Test
    public void testCreateTransferMethod_callsListenerWithBankCardOnSuccess() {
        HyperwalletBankCard bankCard = new HyperwalletBankCard
                .Builder("CA", "CAD", "1232345456784", "2019-05", "234")
                .build();

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[1];
                HyperwalletBankCard returnedBankCard = new HyperwalletBankCard
                        .Builder("CA", "CAD", "1232345456784", "2019-05", "234")
                        .cardBrand("Brand")
                        .cardType("cardType")
                        .build();
                listener.onSuccess(returnedBankCard);
                return listener;
            }
        }).when(mHyperwallet).createBankCard(any(HyperwalletBankCard.class),
                ArgumentMatchers.<HyperwalletListener<HyperwalletBankCard>>any());
        TransferMethodRepository.LoadTransferMethodCallback mockCallback = mock(
                TransferMethodRepository.LoadTransferMethodCallback.class);

        // test
        mTransferMethodRepository.createTransferMethod(bankCard, mockCallback);

        verify(mockCallback).onTransferMethodLoaded(mBankCardArgumentCaptor.capture());
        verify(mockCallback, never()).onError(any(HyperwalletErrors.class));

        HyperwalletBankCard transferMethod = mBankCardArgumentCaptor.getValue();
        assertThat(transferMethod, is(notNullValue()));
        assertThat(transferMethod.getField(TYPE), is(HyperwalletTransferMethod.TransferMethodTypes.BANK_CARD));
        assertThat(transferMethod.getField(HyperwalletTransferMethod.TransferMethodFields.CARD_BRAND), is("Brand"));
        assertThat(transferMethod.getField(HyperwalletTransferMethod.TransferMethodFields.CARD_TYPE), is("cardType"));
    }

    @Test
    public void testCreateTransferMethod_callsListenerWithErrorOnBankCardFailure() {
        HyperwalletBankCard bankCard = new HyperwalletBankCard
                .Builder("CA", "CAD", "1232345456784", "2019-05", "234")
                .build();


        final HyperwalletError error = new HyperwalletError("bank card test message", "BANK_CARD_TEST_CODE");

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[1];
                List<HyperwalletError> errorList = new ArrayList<>();
                errorList.add(error);
                HyperwalletErrors errors = new HyperwalletErrors(errorList);
                HyperwalletException exception = new HyperwalletException(errors);
                listener.onFailure(exception);
                return listener;
            }
        }).when(mHyperwallet).createBankCard(any(HyperwalletBankCard.class),
                ArgumentMatchers.<HyperwalletListener<HyperwalletBankCard>>any());
        TransferMethodRepository.LoadTransferMethodCallback mockCallback = mock(
                TransferMethodRepository.LoadTransferMethodCallback.class);

        // test
        mTransferMethodRepository.createTransferMethod(bankCard, mockCallback);

        verify(mockCallback).onError(mErrorsArgumentCaptor.capture());
        verify(mockCallback, never()).onTransferMethodLoaded(any(HyperwalletTransferMethod.class));
        assertThat(mErrorsArgumentCaptor.getValue().getErrors(), hasItem(error));
    }
}