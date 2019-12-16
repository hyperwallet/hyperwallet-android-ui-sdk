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

import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.exception.HyperwalletException;
import com.hyperwallet.android.listener.HyperwalletListener;
import com.hyperwallet.android.model.Error;
import com.hyperwallet.android.model.Errors;
import com.hyperwallet.android.model.StatusTransition;
import com.hyperwallet.android.model.paging.PageList;
import com.hyperwallet.android.model.transfermethod.BankAccount;
import com.hyperwallet.android.model.transfermethod.BankCard;
import com.hyperwallet.android.model.transfermethod.TransferMethod;
import com.hyperwallet.android.model.transfermethod.TransferMethodQueryParam;
import com.hyperwallet.android.model.transfermethod.PayPalAccount;

import org.hamcrest.Matchers;
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

    @Rule
    public ExpectedException mThrown = ExpectedException.none();
    @Rule
    public MockitoRule mMockito = MockitoJUnit.rule();
    @Spy
    TransferMethodRepositoryImpl mTransferMethodRepository;
    @Mock
    private Hyperwallet mHyperwallet;
    @Mock
    private TransferMethodRepository.DeactivateTransferMethodCallback mDeactivateTransferMethodCallback;
    @Mock
    private TransferMethodRepository.LoadTransferMethodListCallback mLoadTransferMethodListCallback;
    @Mock
    private TransferMethodRepository.LoadTransferMethodCallback mLoadTransferMethodCallback;
    @Captor
    private ArgumentCaptor<Errors> mErrorsArgumentCaptor;
    @Captor
    private ArgumentCaptor<BankAccount> mBankAccountArgumentCaptor;
    @Captor
    private ArgumentCaptor<BankCard> mBankCardArgumentCaptor;
    @Captor
    private ArgumentCaptor<PayPalAccount> mPayPalAccountArgumentCaptor;
    @Captor
    private ArgumentCaptor<StatusTransition> mStatusTransitionArgumentCaptor;
    @Captor
    private ArgumentCaptor<List<TransferMethod>> mListTransferMethodCaptor;
    @Captor
    private ArgumentCaptor<TransferMethodQueryParam> mQueryParamCaptor;

    @Before
    public void setup() {
        doReturn(mHyperwallet).when(mTransferMethodRepository).getHyperwallet();
    }

    @Test
    public void testCreateTransferMethod_bankAccountWithSuccess() {
        BankAccount bankAccount = new BankAccount
                .Builder("CA", "CAD", "3423423432")
                .build();

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[1];
                BankAccount returnedBank = new BankAccount
                        .Builder("CA", "CAD", "3423423432")
                        .bankName("Mock Bank Response")
                        .build();
                listener.onSuccess(returnedBank);
                return listener;
            }
        }).when(mHyperwallet).createBankAccount(any(BankAccount.class),
                ArgumentMatchers.<HyperwalletListener<BankAccount>>any());

        // test
        mTransferMethodRepository.createTransferMethod(bankAccount, mLoadTransferMethodCallback);

        verify(mLoadTransferMethodCallback).onTransferMethodLoaded(mBankAccountArgumentCaptor.capture());
        verify(mLoadTransferMethodCallback, never()).onError(any(Errors.class));

        BankAccount transferMethod = mBankAccountArgumentCaptor.getValue();
        assertThat(transferMethod, is(notNullValue()));
        assertThat(transferMethod.getField(TYPE), is(TransferMethod.TransferMethodTypes.BANK_ACCOUNT));
        assertThat(transferMethod.getField(BANK_NAME), is("Mock Bank Response"));
        assertThat(transferMethod.getField(TRANSFER_METHOD_COUNTRY), is("CA"));
        assertThat(transferMethod.getField(TRANSFER_METHOD_CURRENCY), is("CAD"));
        assertThat(transferMethod.getField(BANK_ACCOUNT_ID), is("3423423432"));
    }

    @Test
    public void testCreateTransferMethod_bankAccountWithError() {
        BankAccount bankAccount = new BankAccount
                .Builder("US", "USD", "23432432")
                .build();

        final Error error = new Error("test message", "TEST_CODE");

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[1];
                List<Error> errorList = new ArrayList<>();
                errorList.add(error);
                Errors errors = new Errors(errorList);
                HyperwalletException exception = new HyperwalletException(errors);
                listener.onFailure(exception);
                return listener;
            }
        }).when(mHyperwallet).createBankAccount(any(BankAccount.class),
                ArgumentMatchers.<HyperwalletListener<BankAccount>>any());

        // test
        mTransferMethodRepository.createTransferMethod(bankAccount, mLoadTransferMethodCallback);

        verify(mLoadTransferMethodCallback).onError(mErrorsArgumentCaptor.capture());
        verify(mLoadTransferMethodCallback, never()).onTransferMethodLoaded(any(TransferMethod.class));
        assertThat(mErrorsArgumentCaptor.getValue().getErrors(), hasItem(error));
    }

    @Test
    public void testCreateTransferMethod_createTransferWithUnsupportedTransferType() {
        BankAccount bankAccount = new BankAccount
                .Builder("US", "USD", "23432432")
                .transferMethodType("UNKNOWN_TRANSFER_TYPE")
                .build();

        // test
        mTransferMethodRepository.createTransferMethod(bankAccount, mLoadTransferMethodCallback);

        verify(mLoadTransferMethodCallback).onError(mErrorsArgumentCaptor.capture());
        verify(mLoadTransferMethodCallback, never()).onTransferMethodLoaded(any(TransferMethod.class));
        assertThat(mErrorsArgumentCaptor.getValue().getErrors(), Matchers.<Error>hasSize(1));
        assertThat(mErrorsArgumentCaptor.getValue().getErrors().get(0).getMessageId(),
                is(R.string.error_unsupported_transfer_type));
        assertThat(mErrorsArgumentCaptor.getValue().getErrors().get(0).getCode(), is(EC_UNEXPECTED_EXCEPTION));
    }

    @Test
    public void testDeactivateTransferMethod_bankAccountWithSuccess() {
        BankAccount bankAccount = new BankAccount
                .Builder("CA", "CAD", "3423423432")
                .token("trm-854c4ec1-9161-49d6-92e2-b8d15aa4bf56")
                .build();
        bankAccount.setField(STATUS, StatusTransition.StatusDefinition.ACTIVATED);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                StatusTransition statusTransition = new StatusTransition.Builder()
                        .transition(DE_ACTIVATED)
                        .notes("Closing this account.").build();
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[2];
                listener.onSuccess(statusTransition);
                return listener;
            }
        }).when(mHyperwallet).deactivateBankAccount(anyString(), ArgumentMatchers.<String>isNull(),
                ArgumentMatchers.<HyperwalletListener<StatusTransition>>any());

        // test
        mTransferMethodRepository.deactivateTransferMethod(bankAccount, mDeactivateTransferMethodCallback);

        verify(mDeactivateTransferMethodCallback).onTransferMethodDeactivated(
                mStatusTransitionArgumentCaptor.capture());
        verify(mDeactivateTransferMethodCallback, never()).onError(any(Errors.class));

        StatusTransition statusTransition = mStatusTransitionArgumentCaptor.getValue();
        assertThat(statusTransition, is(notNullValue()));
        assertThat(statusTransition.getTransition(), is(DE_ACTIVATED));
        assertThat(statusTransition.getNotes(), is("Closing this account."));
    }

    @Test
    public void testDeactivateTransferMethod_bankAccountWithError() {
        BankAccount bankAccount = new BankAccount
                .Builder("CA", "CAD", "3423423432")
                .token("trm-854c4ec1-9161-49d6-92e2-b8d15aa4bf56")
                .build();
        bankAccount.setField(STATUS, StatusTransition.StatusDefinition.ACTIVATED);
        final Error error = new Error("test message", "TEST_CODE");
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[2];
                List<Error> errorList = new ArrayList<>();
                errorList.add(error);
                Errors errors = new Errors(errorList);
                listener.onFailure(new HyperwalletException(errors));
                return listener;
            }
        }).when(mHyperwallet).deactivateBankAccount(anyString(), ArgumentMatchers.<String>isNull(),
                ArgumentMatchers.<HyperwalletListener<StatusTransition>>any());

        // test
        mTransferMethodRepository.deactivateTransferMethod(bankAccount, mDeactivateTransferMethodCallback);

        verify(mDeactivateTransferMethodCallback, never()).onTransferMethodDeactivated(
                any(StatusTransition.class));
        verify(mDeactivateTransferMethodCallback).onError(mErrorsArgumentCaptor.capture());

        assertThat(mErrorsArgumentCaptor.getValue().getErrors(), hasItem(error));
    }

    @Test
    public void testDeactivateTransferMethod_bankCardWithSuccess() {
        BankCard bankCard = new BankCard
                .Builder("CA", "CAD", "1232345456784", "2019-05", "234")
                .token("trm-854c4ec1-9161-49d6-92e2-b8d15aa4bf56")
                .build();
        bankCard.setField(STATUS, StatusTransition.StatusDefinition.ACTIVATED);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                StatusTransition statusTransition = new StatusTransition.Builder()
                        .transition(DE_ACTIVATED)
                        .notes("Closing this account.").build();
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[2];
                listener.onSuccess(statusTransition);
                return listener;
            }
        }).when(mHyperwallet).deactivateBankCard(anyString(), ArgumentMatchers.<String>isNull(),
                ArgumentMatchers.<HyperwalletListener<StatusTransition>>any());

        // test
        mTransferMethodRepository.deactivateTransferMethod(bankCard, mDeactivateTransferMethodCallback);

        verify(mDeactivateTransferMethodCallback).onTransferMethodDeactivated(
                mStatusTransitionArgumentCaptor.capture());
        verify(mDeactivateTransferMethodCallback, never()).onError(any(Errors.class));

        StatusTransition statusTransition = mStatusTransitionArgumentCaptor.getValue();
        assertThat(statusTransition, is(notNullValue()));
        assertThat(statusTransition.getTransition(), is(DE_ACTIVATED));
        assertThat(statusTransition.getNotes(), is("Closing this account."));
    }

    @Test
    public void testDeactivateTransferMethod_bankCardWithError() {
        BankCard bankCard = new BankCard
                .Builder("CA", "CAD", "1232345456784", "2019-05", "234")
                .token("trm-854c4ec1-9161-49d6-92e2-b8d15aa4bf56")
                .build();
        bankCard.setField(STATUS, StatusTransition.StatusDefinition.ACTIVATED);
        final Error error = new Error("test message", "TEST_CODE");
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[2];
                List<Error> errorList = new ArrayList<>();
                errorList.add(error);
                Errors errors = new Errors(errorList);
                listener.onFailure(new HyperwalletException(errors));
                return listener;
            }
        }).when(mHyperwallet).deactivateBankCard(anyString(), ArgumentMatchers.<String>isNull(),
                ArgumentMatchers.<HyperwalletListener<StatusTransition>>any());

        // test
        mTransferMethodRepository.deactivateTransferMethod(bankCard, mDeactivateTransferMethodCallback);

        verify(mDeactivateTransferMethodCallback, never()).onTransferMethodDeactivated(
                any(StatusTransition.class));
        verify(mDeactivateTransferMethodCallback).onError(mErrorsArgumentCaptor.capture());

        assertThat(mErrorsArgumentCaptor.getValue().getErrors(), hasItem(error));
    }

    @Test
    public void testDeactivateTransferMethod_payPalAccountWithSuccess() {
        PayPalAccount payPalAccount = new PayPalAccount.Builder("US", "US", "jsmith4@hyperwallet.com")
                .token("trm-854c4ec1-9161-49d6-92e2-b8d15aa4bf56").build();
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                StatusTransition statusTransition = new StatusTransition.Builder()
                        .transition(DE_ACTIVATED)
                        .notes("Closing this account.").build();
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[2];
                listener.onSuccess(statusTransition);
                return listener;
            }
        }).when(mHyperwallet).deactivatePayPalAccount(anyString(), ArgumentMatchers.<String>isNull(),
                ArgumentMatchers.<HyperwalletListener<StatusTransition>>any());

        // test
        mTransferMethodRepository.deactivateTransferMethod(payPalAccount, mDeactivateTransferMethodCallback);

        verify(mDeactivateTransferMethodCallback).onTransferMethodDeactivated(
                mStatusTransitionArgumentCaptor.capture());
        verify(mDeactivateTransferMethodCallback, never()).onError(any(Errors.class));

        StatusTransition statusTransition = mStatusTransitionArgumentCaptor.getValue();
        assertThat(statusTransition, is(notNullValue()));
        assertThat(statusTransition.getTransition(), is(DE_ACTIVATED));
        assertThat(statusTransition.getNotes(), is("Closing this account."));
    }

    @Test
    public void testDeactivateTransferMethod_payPalAccountWithError() {
        PayPalAccount payPalAccount = new PayPalAccount.Builder("US", "US", "jsmith4@hyperwallet.com")
                .token("trm-854c4ec1-9161-49d6-92e2-b8d15aa4bf56").build();
        payPalAccount.setField(STATUS, StatusTransition.StatusDefinition.ACTIVATED);
        final Error error = new Error("test message", "TEST_CODE");
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[2];
                List<Error> errorList = new ArrayList<>();
                errorList.add(error);
                Errors errors = new Errors(errorList);
                listener.onFailure(new HyperwalletException(errors));
                return listener;
            }
        }).when(mHyperwallet).deactivatePayPalAccount(anyString(), ArgumentMatchers.<String>isNull(),
                ArgumentMatchers.<HyperwalletListener<StatusTransition>>any());

        // test
        mTransferMethodRepository.deactivateTransferMethod(payPalAccount, mDeactivateTransferMethodCallback);

        verify(mDeactivateTransferMethodCallback, never()).onTransferMethodDeactivated(
                any(StatusTransition.class));
        verify(mDeactivateTransferMethodCallback).onError(mErrorsArgumentCaptor.capture());

        assertThat(mErrorsArgumentCaptor.getValue().getErrors(), hasItem(error));
    }


    @Test
    public void testCreateTransferMethod_bankCardWithSuccess() {
        BankCard bankCard = new BankCard
                .Builder("CA", "CAD", "1232345456784", "2019-05", "234")
                .build();

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[1];
                BankCard returnedBankCard = new BankCard
                        .Builder("CA", "CAD", "1232345456784", "2019-05", "234")
                        .cardBrand("Brand")
                        .cardType("cardType")
                        .build();
                listener.onSuccess(returnedBankCard);
                return listener;
            }
        }).when(mHyperwallet).createBankCard(any(BankCard.class),
                ArgumentMatchers.<HyperwalletListener<BankCard>>any());

        // test
        mTransferMethodRepository.createTransferMethod(bankCard, mLoadTransferMethodCallback);

        verify(mLoadTransferMethodCallback).onTransferMethodLoaded(mBankCardArgumentCaptor.capture());
        verify(mLoadTransferMethodCallback, never()).onError(any(Errors.class));

        BankCard transferMethod = mBankCardArgumentCaptor.getValue();
        assertThat(transferMethod, is(notNullValue()));
        assertThat(transferMethod.getField(TYPE), is(TransferMethod.TransferMethodTypes.BANK_CARD));
        assertThat(transferMethod.getField(TransferMethod.TransferMethodFields.CARD_BRAND), is("Brand"));
        assertThat(transferMethod.getField(TransferMethod.TransferMethodFields.CARD_TYPE), is("cardType"));
    }

    @Test
    public void testCreateTransferMethod_bankCardWithError() {
        BankCard bankCard = new BankCard
                .Builder("CA", "CAD", "1232345456784", "2019-05", "234")
                .build();

        final Error error = new Error("bank card test message", "BANK_CARD_TEST_CODE");

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[1];
                List<Error> errorList = new ArrayList<>();
                errorList.add(error);
                Errors errors = new Errors(errorList);
                HyperwalletException exception = new HyperwalletException(errors);
                listener.onFailure(exception);
                return listener;
            }
        }).when(mHyperwallet).createBankCard(any(BankCard.class),
                ArgumentMatchers.<HyperwalletListener<BankCard>>any());

        // test
        mTransferMethodRepository.createTransferMethod(bankCard, mLoadTransferMethodCallback);

        verify(mLoadTransferMethodCallback).onError(mErrorsArgumentCaptor.capture());
        verify(mLoadTransferMethodCallback, never()).onTransferMethodLoaded(any(TransferMethod.class));
        assertThat(mErrorsArgumentCaptor.getValue().getErrors(), hasItem(error));
    }


    @Test
    public void testLoadTransferMethod_returnsBankAccount() {
        BankAccount bankAccount = new BankAccount
                .Builder("CA", "CAD", "3423423432")
                .build();

        List<BankAccount> accounts = new ArrayList<>();
        accounts.add(bankAccount);
        final PageList<BankAccount> pageList = new PageList<>(accounts);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[1];
                listener.onSuccess(pageList);
                return listener;
            }
        }).when(mHyperwallet).listTransferMethods((TransferMethodQueryParam) any(),
                ArgumentMatchers.<HyperwalletListener<PageList<TransferMethod>>>any());

        // test
        mTransferMethodRepository.loadTransferMethods(mLoadTransferMethodListCallback);

        verify(mLoadTransferMethodListCallback).onTransferMethodListLoaded(mListTransferMethodCaptor.capture());
        verify(mLoadTransferMethodListCallback, never()).onError(any(Errors.class));

        List<TransferMethod> transferMethods = mListTransferMethodCaptor.getValue();
        assertThat(transferMethods, hasSize(1));
        assertThat(transferMethods.get(0).getField(TRANSFER_METHOD_COUNTRY), is("CA"));
        assertThat(transferMethods.get(0).getField(TRANSFER_METHOD_CURRENCY), is("CAD"));
        assertThat(transferMethods.get(0).getField(BANK_ACCOUNT_ID), is("3423423432"));
    }

    @Test
    public void testLoadTransferMethod_returnsNoAccounts() {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[1];
                listener.onSuccess(null);
                return listener;
            }
        }).when(mHyperwallet).listTransferMethods((TransferMethodQueryParam) any(),
                ArgumentMatchers.<HyperwalletListener<PageList<TransferMethod>>>any());

        // test
        mTransferMethodRepository.loadTransferMethods(mLoadTransferMethodListCallback);

        verify(mLoadTransferMethodListCallback).onTransferMethodListLoaded(mListTransferMethodCaptor.capture());
        verify(mLoadTransferMethodListCallback, never()).onError(any(Errors.class));

        List<TransferMethod> transferMethods = mListTransferMethodCaptor.getValue();
        assertThat(transferMethods, is(nullValue()));
    }

    @Test
    public void testLoadTransferMethod_withError() {
        final Error error = new Error("test message", "TEST_CODE");

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[1];
                List<Error> errorList = new ArrayList<>();
                errorList.add(error);
                Errors errors = new Errors(errorList);
                listener.onFailure(new HyperwalletException(errors));
                return listener;
            }
        }).when(mHyperwallet).listTransferMethods((TransferMethodQueryParam) any(),
                ArgumentMatchers.<HyperwalletListener<PageList<TransferMethod>>>any());

        // test
        mTransferMethodRepository.loadTransferMethods(mLoadTransferMethodListCallback);

        verify(mLoadTransferMethodListCallback, never()).onTransferMethodListLoaded(
                ArgumentMatchers.<TransferMethod>anyList());
        verify(mLoadTransferMethodListCallback).onError(mErrorsArgumentCaptor.capture());

        assertThat(mErrorsArgumentCaptor.getValue().getErrors(), hasItem(error));
    }

    @Test
    public void testLoadLatestTransferMethod_returnsBankAccount() {
        List<BankAccount> accounts = new ArrayList<BankAccount>() {{
            add(new BankAccount
                    .Builder("CA", "CAD", "3423423432")
                    .build());
            add(new BankAccount
                    .Builder("US", "USD", "1231231222")
                    .build());
        }};

        final PageList<BankAccount> pageList = new PageList<>(accounts);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[1];
                listener.onSuccess(pageList);
                return listener;
            }
        }).when(mHyperwallet).listTransferMethods((TransferMethodQueryParam) any(),
                ArgumentMatchers.<HyperwalletListener<PageList<TransferMethod>>>any());

        // test
        mTransferMethodRepository.loadLatestTransferMethod(mLoadTransferMethodCallback);

        verify(mLoadTransferMethodCallback).onTransferMethodLoaded(mBankAccountArgumentCaptor.capture());
        verify(mLoadTransferMethodCallback, never()).onError(any(Errors.class));

        BankAccount transferMethod = mBankAccountArgumentCaptor.getValue();
        assertThat(transferMethod, is(notNullValue()));
        assertThat(transferMethod.getField(TRANSFER_METHOD_COUNTRY), is("CA"));
        assertThat(transferMethod.getField(TRANSFER_METHOD_CURRENCY), is("CAD"));
        assertThat(transferMethod.getField(BANK_ACCOUNT_ID), is("3423423432"));
    }

    @Test
    public void testLoadLatestTransferMethod_returnsNoAccounts() {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[1];
                listener.onSuccess(null);
                return listener;
            }
        }).when(mHyperwallet).listTransferMethods((TransferMethodQueryParam) any(),
                ArgumentMatchers.<HyperwalletListener<PageList<TransferMethod>>>any());

        // test
        mTransferMethodRepository.loadLatestTransferMethod(mLoadTransferMethodCallback);

        verify(mLoadTransferMethodCallback).onTransferMethodLoaded(mBankAccountArgumentCaptor.capture());
        verify(mLoadTransferMethodCallback, never()).onError(any(Errors.class));

        BankAccount transferMethod = mBankAccountArgumentCaptor.getValue();
        assertThat(transferMethod, is(nullValue()));
    }

    @Test
    public void testLoadLatestTransferMethod_withError() {
        final Error error = new Error("test message", "TEST_CODE");

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[1];
                List<Error> errorList = new ArrayList<>();
                errorList.add(error);
                Errors errors = new Errors(errorList);
                listener.onFailure(new HyperwalletException(errors));
                return listener;
            }
        }).when(mHyperwallet).listTransferMethods((TransferMethodQueryParam) any(),
                ArgumentMatchers.<HyperwalletListener<PageList<TransferMethod>>>any());

        // test
        mTransferMethodRepository.loadLatestTransferMethod(mLoadTransferMethodCallback);

        verify(mLoadTransferMethodCallback, never()).onTransferMethodLoaded(any(TransferMethod.class));
        verify(mLoadTransferMethodCallback).onError(mErrorsArgumentCaptor.capture());

        assertThat(mErrorsArgumentCaptor.getValue().getErrors(), hasItem(error));
    }

    @Test
    public void testCreateTransferMethod_payPalAccountWithSuccess() {
        // prepare
        final PayPalAccount returnedPayPalAccount = new PayPalAccount.Builder()
                .transferMethodCurrency("USD")
                .transferMethodCountry("US")
                .email("money@mail.com")
                .token("trm-token-1342242314")
                .build();

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[1];
                returnedPayPalAccount.setField(STATUS, ACTIVATED);
                listener.onSuccess(returnedPayPalAccount);
                return listener;
            }
        }).when(mHyperwallet).createPayPalAccount(any(PayPalAccount.class),
                ArgumentMatchers.<HyperwalletListener<PayPalAccount>>any());

        PayPalAccount parameter = new PayPalAccount.Builder().build();

        // test
        mTransferMethodRepository.createTransferMethod(parameter, mLoadTransferMethodCallback);

        // verify
        verify(mLoadTransferMethodCallback).onTransferMethodLoaded(mPayPalAccountArgumentCaptor.capture());
        verify(mLoadTransferMethodCallback, never()).onError(any(Errors.class));

        // assert
        PayPalAccount payPalAccount = mPayPalAccountArgumentCaptor.getValue();
        assertThat(payPalAccount, is(notNullValue()));
        assertThat(payPalAccount.getCountry(), is("US"));
        assertThat(payPalAccount.getCurrency(), is("USD"));
        assertThat(payPalAccount.getEmail(), is("money@mail.com"));
        assertThat(payPalAccount.getField(STATUS), is(ACTIVATED));
        assertThat(payPalAccount.getField(TOKEN), is("trm-token-1342242314"));
    }

    @Test
    public void testCreateTransferMethod_payPalAccountWithError() {
        // prepare
        final Error returnedError = new Error("PayPal test message", "PAYPAL_TEST_CODE");

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[1];

                List<Error> errorList = new ArrayList<>();
                errorList.add(returnedError);

                listener.onFailure(new HyperwalletException(new Errors(errorList)));
                return listener;
            }
        }).when(mHyperwallet).createPayPalAccount(any(PayPalAccount.class),
                ArgumentMatchers.<HyperwalletListener<PayPalAccount>>any());
        PayPalAccount parameter = new PayPalAccount.Builder().build();

        // test
        mTransferMethodRepository.createTransferMethod(parameter, mLoadTransferMethodCallback);

        // verify
        verify(mLoadTransferMethodCallback, never()).onTransferMethodLoaded(any(TransferMethod.class));
        verify(mLoadTransferMethodCallback).onError(mErrorsArgumentCaptor.capture());

        // assert
        assertThat(mErrorsArgumentCaptor.getValue().getErrors(), hasItem(returnedError));
    }

    @Test
    public void testCreateTransferMethod_wireAccountWithSuccess() {
        BankAccount bankAccount = new BankAccount
                .Builder("US", "USD", "1411413412")
                .transferMethodType(TransferMethod.TransferMethodTypes.WIRE_ACCOUNT)
                .build();

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[1];
                BankAccount returnedBank = new BankAccount
                        .Builder("US", "USD", "1411413412")
                        .bankName("Mock Bank Response")
                        .transferMethodType(TransferMethod.TransferMethodTypes.WIRE_ACCOUNT)
                        .build();
                listener.onSuccess(returnedBank);
                return listener;
            }
        }).when(mHyperwallet).createBankAccount(any(BankAccount.class),
                ArgumentMatchers.<HyperwalletListener<BankAccount>>any());

        // test
        mTransferMethodRepository.createTransferMethod(bankAccount, mLoadTransferMethodCallback);

        verify(mLoadTransferMethodCallback).onTransferMethodLoaded(mBankAccountArgumentCaptor.capture());
        verify(mLoadTransferMethodCallback, never()).onError(any(Errors.class));

        BankAccount transferMethod = mBankAccountArgumentCaptor.getValue();
        assertThat(transferMethod, is(notNullValue()));
        assertThat(transferMethod.getField(TYPE), is(TransferMethod.TransferMethodTypes.WIRE_ACCOUNT));
        assertThat(transferMethod.getField(BANK_NAME), is("Mock Bank Response"));
        assertThat(transferMethod.getField(TRANSFER_METHOD_COUNTRY), is("US"));
        assertThat(transferMethod.getField(TRANSFER_METHOD_CURRENCY), is("USD"));
        assertThat(transferMethod.getField(BANK_ACCOUNT_ID), is("1411413412"));
    }

    @Test
    public void testDeactivateTransferMethod_wireAccountWithSuccess() {
        BankAccount bankAccount = new BankAccount
                .Builder("CA", "CAD", "3423423432")
                .transferMethodType(TransferMethod.TransferMethodTypes.WIRE_ACCOUNT)
                .token("trm-123")
                .build();
        bankAccount.setField(STATUS, StatusTransition.StatusDefinition.ACTIVATED);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                StatusTransition statusTransition = new StatusTransition.Builder()
                        .transition(DE_ACTIVATED)
                        .notes("Closing this account.").build();
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[2];
                listener.onSuccess(statusTransition);
                return listener;
            }
        }).when(mHyperwallet).deactivateBankAccount(anyString(), ArgumentMatchers.<String>isNull(),
                ArgumentMatchers.<HyperwalletListener<StatusTransition>>any());

        // test
        mTransferMethodRepository.deactivateTransferMethod(bankAccount, mDeactivateTransferMethodCallback);

        verify(mDeactivateTransferMethodCallback).onTransferMethodDeactivated(
                mStatusTransitionArgumentCaptor.capture());
        verify(mDeactivateTransferMethodCallback, never()).onError(any(Errors.class));

        StatusTransition statusTransition = mStatusTransitionArgumentCaptor.getValue();
        assertThat(statusTransition, is(notNullValue()));
        assertThat(statusTransition.getTransition(), is(DE_ACTIVATED));
        assertThat(statusTransition.getNotes(), is("Closing this account."));
    }

    @Test
    public void testLoadTransferMethod_verifyDefaultQueryParams() {
        BankAccount bankAccount = new BankAccount
                .Builder("CA", "CAD", "3423423432")
                .build();
        List<BankAccount> accounts = new ArrayList<>();
        accounts.add(bankAccount);
        final PageList<BankAccount> pageList = new PageList<>(accounts);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[1];
                listener.onSuccess(pageList);
                return listener;
            }
        }).when(mHyperwallet).listTransferMethods(mQueryParamCaptor.capture(),
                ArgumentMatchers.<HyperwalletListener<PageList<TransferMethod>>>any());

        // test
        mTransferMethodRepository.loadTransferMethods(mLoadTransferMethodListCallback);
        assertThat(mQueryParamCaptor.getValue().getLimit(), is(100));
        assertThat(mQueryParamCaptor.getValue().getStatus(), is(ACTIVATED));
        assertThat(mQueryParamCaptor.getValue().getSortBy(), is("-createdOn"));
    }
}
