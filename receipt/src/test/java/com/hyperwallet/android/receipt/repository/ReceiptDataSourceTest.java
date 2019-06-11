package com.hyperwallet.android.receipt.repository;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import static com.hyperwallet.android.model.receipt.Receipt.Entries.CREDIT;
import static com.hyperwallet.android.model.receipt.Receipt.Entries.DEBIT;
import static com.hyperwallet.android.model.receipt.Receipt.ReceiptTypes.PAYMENT;
import static com.hyperwallet.android.model.receipt.Receipt.ReceiptTypes.TRANSFER_TO_BANK_ACCOUNT;

import androidx.paging.PageKeyedDataSource;

import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.exception.HyperwalletException;
import com.hyperwallet.android.listener.HyperwalletListener;
import com.hyperwallet.android.model.HyperwalletError;
import com.hyperwallet.android.model.HyperwalletErrors;
import com.hyperwallet.android.model.paging.HyperwalletPageList;
import com.hyperwallet.android.model.receipt.Receipt;
import com.hyperwallet.android.model.receipt.ReceiptQueryParam;
import com.hyperwallet.android.rule.HyperwalletExternalResourceManager;

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
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
public class ReceiptDataSourceTest {

    @Rule
    public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule
    public HyperwalletExternalResourceManager mExternalResourceManager = new HyperwalletExternalResourceManager();

    @Mock
    private Hyperwallet mHyperwallet;
    @Mock
    private PageKeyedDataSource.LoadInitialParams<Integer> mInitialParams;
    @Mock
    private PageKeyedDataSource.LoadInitialCallback<Integer, Receipt> mInitialCallback;
    // can't be mocked due to params.key is of type Integer and autoboxing will not work with null to 0
    private final PageKeyedDataSource.LoadParams<Integer> mLoadAfterParams =
            new PageKeyedDataSource.LoadParams<>(10, 10);
    @Mock
    private PageKeyedDataSource.LoadCallback<Integer, Receipt> mLoadAfterCallback;

    @Captor
    private ArgumentCaptor<List<Receipt>> mListArgumentCaptor;
    @Captor
    private ArgumentCaptor<Integer> mPreviousCaptor;
    @Captor
    private ArgumentCaptor<Integer> mNextCaptor;

    @Spy
    private ReceiptDataSource mReceiptDataSource;

    @Before
    public void setUp() {
        doReturn(mHyperwallet).when(mReceiptDataSource).getHyperwallet();
    }

    @Test
    public void testLoadInitial_returnsReceipts() throws Exception {
        String json = mExternalResourceManager.getResourceContent("receipt_list_date_grouping_response.json");
        JSONObject jsonObject = new JSONObject(json);
        final HyperwalletPageList<Receipt> response = new HyperwalletPageList<>(jsonObject, Receipt.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[1];
                listener.onSuccess(response);
                return listener;
            }
        }).when(mHyperwallet).listReceipts(any(ReceiptQueryParam.class),
                ArgumentMatchers.<HyperwalletListener<HyperwalletPageList<Receipt>>>any());

        // test
        mReceiptDataSource.loadInitial(mInitialParams, mInitialCallback);

        verify(mHyperwallet).listReceipts(any(ReceiptQueryParam.class),
                ArgumentMatchers.<HyperwalletListener<HyperwalletPageList<Receipt>>>any());
        verify(mInitialCallback).onResult(mListArgumentCaptor.capture(), mPreviousCaptor.capture(),
                mNextCaptor.capture());

        assertThat(mPreviousCaptor.getValue(), is(0));
        assertThat(mNextCaptor.getValue(), is(10));

        // assert receipts information
        List<Receipt> receipts = mListArgumentCaptor.getValue();
        assertThat(receipts, Matchers.<Receipt>hasSize(5));
        assertThat(receipts.get(0).getJournalId(), is("51660665"));
        assertThat(receipts.get(0).getType(), is(PAYMENT));
        assertThat(receipts.get(0).getEntry(), is(CREDIT));
        assertThat(receipts.get(0).getSourceToken(), is("act-b1f6dc28-e534-45f4-a661-3523f051f77a"));
        assertThat(receipts.get(0).getDestinationToken(), is("usr-b4e8ec34-52d8-4a81-9566-bdde1bd745b6"));
        assertThat(receipts.get(0).getAmount(), is("5000.00"));
        assertThat(receipts.get(0).getFee(), is("0.00"));
        assertThat(receipts.get(0).getCurrency(), is("USD"));
        assertThat(receipts.get(0).getDetails(), is(notNullValue()));
        assertThat(receipts.get(0).getDetails().getPayeeName(), is("Kevin Puckett"));
        assertThat(receipts.get(0).getDetails().getClientPaymentId(), is("trans-0001"));
        assertThat(receipts.get(1).getJournalId(), is("51660666"));
        assertThat(receipts.get(1).getType(), is(TRANSFER_TO_BANK_ACCOUNT));
        assertThat(receipts.get(1).getEntry(), is(DEBIT));
        assertThat(receipts.get(1).getSourceToken(), is("usr-b4e8ec34-52d8-4a81-9566-bdde1bd745b6"));
        assertThat(receipts.get(1).getDestinationToken(), is("trm-0a2ac589-2cae-4ed3-9b0b-658246a34687"));
        assertThat(receipts.get(1).getAmount(), is("10.25"));
        assertThat(receipts.get(1).getFee(), is("0.25"));
        assertThat(receipts.get(1).getCurrency(), is("USD"));
        assertThat(receipts.get(1).getDetails(), is(notNullValue()));
        assertThat(receipts.get(1).getDetails().getPayeeName(), is("Kevin Puckett"));
        assertThat(receipts.get(1).getDetails().getBankAccountId(), is("patzachery.mcclary@example.com"));

        assertThat(mReceiptDataSource.getErrors().getValue(), is(nullValue()));
        assertThat(mReceiptDataSource.isFetchingData().getValue(), is(false));
    }

    @Test
    public void testLoadInitial_returnNoReceipt() {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[1];
                listener.onSuccess(null);
                return listener;
            }
        }).when(mHyperwallet).listReceipts(any(ReceiptQueryParam.class),
                ArgumentMatchers.<HyperwalletListener<HyperwalletPageList<Receipt>>>any());

        // test
        mReceiptDataSource.loadInitial(mInitialParams, mInitialCallback);

        verify(mHyperwallet).listReceipts(any(ReceiptQueryParam.class),
                ArgumentMatchers.<HyperwalletListener<HyperwalletPageList<Receipt>>>any());
        verify(mInitialCallback, never()).onResult(ArgumentMatchers.<Receipt>anyList(), anyInt(), anyInt());

        assertThat(mReceiptDataSource.getErrors().getValue(), is(nullValue()));
        assertThat(mReceiptDataSource.isFetchingData().getValue(), is(false));
    }

    @Test
    public void testLoadInitial_withError() {
        final HyperwalletError error = new HyperwalletError("test message", "TEST_CODE");
        List<HyperwalletError> errorList = new ArrayList<>();
        errorList.add(error);
        final HyperwalletErrors errors = new HyperwalletErrors(errorList);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[1];
                listener.onFailure(new HyperwalletException(errors));
                return listener;
            }
        }).when(mHyperwallet).listReceipts(any(ReceiptQueryParam.class),
                ArgumentMatchers.<HyperwalletListener<HyperwalletPageList<Receipt>>>any());

        // test
        mReceiptDataSource.loadInitial(mInitialParams, mInitialCallback);

        verify(mHyperwallet).listReceipts(any(ReceiptQueryParam.class),
                ArgumentMatchers.<HyperwalletListener<HyperwalletPageList<Receipt>>>any());
        verify(mInitialCallback, never()).onResult(ArgumentMatchers.<Receipt>anyList(), anyInt(), anyInt());

        assertThat(mReceiptDataSource.getErrors().getValue(), is(notNullValue()));
        assertThat(mReceiptDataSource.getErrors().getValue().getContent().getErrors(),
                Matchers.<HyperwalletError>hasSize(1));
        assertThat(mReceiptDataSource.getErrors().getValue().getContent().getErrors().get(0).getCode(),
                is("TEST_CODE"));
        assertThat(mReceiptDataSource.getErrors().getValue().getContent().getErrors().get(0).getMessage(),
                is("test message"));
        assertThat(mReceiptDataSource.isFetchingData().getValue(), is(false));
    }

    @Test
    public void testRetry_loadInitial() {
        final HyperwalletError error = new HyperwalletError("test message", "TEST_CODE");
        List<HyperwalletError> errorList = new ArrayList<>();
        errorList.add(error);
        final HyperwalletErrors errors = new HyperwalletErrors(errorList);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[1];
                listener.onFailure(new HyperwalletException(errors));
                return listener;
            }
        }).when(mHyperwallet).listReceipts(any(ReceiptQueryParam.class),
                ArgumentMatchers.<HyperwalletListener<HyperwalletPageList<Receipt>>>any());

        // test
        mReceiptDataSource.loadInitial(mInitialParams, mInitialCallback);

        verify(mHyperwallet).listReceipts(any(ReceiptQueryParam.class),
                ArgumentMatchers.<HyperwalletListener<HyperwalletPageList<Receipt>>>any());
        verify(mInitialCallback, never()).onResult(ArgumentMatchers.<Receipt>anyList(), anyInt(), anyInt());

        // error occurred, this will save params and callback
        assertThat(mReceiptDataSource.getErrors().getValue(), is(notNullValue()));

        // test retry, saved params and callback will be used and no null pointer exception is thrown
        mReceiptDataSource.retry();

        // verify calls
        verify(mReceiptDataSource, times(2)).loadInitial(
                ArgumentMatchers.<PageKeyedDataSource.LoadInitialParams<Integer>>any(),
                ArgumentMatchers.<PageKeyedDataSource.LoadInitialCallback<Integer, Receipt>>any());
        verify(mReceiptDataSource, never()).loadAfter(
                ArgumentMatchers.<PageKeyedDataSource.LoadParams<Integer>>any(),
                ArgumentMatchers.<PageKeyedDataSource.LoadCallback<Integer, Receipt>>any());
    }

    @Test
    public void testLoadAfter_returnsReceipts() throws Exception {
        String json = mExternalResourceManager.getResourceContent("receipt_list_date_grouping_response.json");
        JSONObject jsonObject = new JSONObject(json);
        final HyperwalletPageList<Receipt> response = new HyperwalletPageList<>(jsonObject, Receipt.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[1];
                listener.onSuccess(response);
                return listener;
            }
        }).when(mHyperwallet).listReceipts(any(ReceiptQueryParam.class),
                ArgumentMatchers.<HyperwalletListener<HyperwalletPageList<Receipt>>>any());

        // test
        mReceiptDataSource.loadAfter(mLoadAfterParams, mLoadAfterCallback);

        verify(mHyperwallet).listReceipts(any(ReceiptQueryParam.class),
                ArgumentMatchers.<HyperwalletListener<HyperwalletPageList<Receipt>>>any());
        verify(mLoadAfterCallback).onResult(mListArgumentCaptor.capture(), mNextCaptor.capture());

        assertThat(mNextCaptor.getValue(), is(10));

        // assert receipts information
        List<Receipt> receipts = mListArgumentCaptor.getValue();
        assertThat(receipts, Matchers.<Receipt>hasSize(5));
        assertThat(receipts.get(3).getJournalId(), is("51660675"));
        assertThat(receipts.get(3).getType(), is(PAYMENT));
        assertThat(receipts.get(3).getEntry(), is(CREDIT));
        assertThat(receipts.get(3).getSourceToken(), is("act-b1f6dc28-e534-45f4-a661-3523f051f77a"));
        assertThat(receipts.get(3).getDestinationToken(), is("usr-b4e8ec34-52d8-4a81-9566-bdde1bd745b6"));
        assertThat(receipts.get(3).getAmount(), is("13.00"));
        assertThat(receipts.get(3).getFee(), is("0.00"));
        assertThat(receipts.get(3).getCurrency(), is("USD"));
        assertThat(receipts.get(3).getDetails(), is(notNullValue()));
        assertThat(receipts.get(3).getDetails().getPayeeName(), is("Kevin Puckett"));
        assertThat(receipts.get(3).getDetails().getClientPaymentId(), is("CSietnRJQQ0bscYkOoPJxNiTDiVALhjQ"));
        assertThat(receipts.get(4).getJournalId(), is("51660676"));
        assertThat(receipts.get(4).getType(), is(PAYMENT));
        assertThat(receipts.get(4).getEntry(), is(CREDIT));
        assertThat(receipts.get(4).getSourceToken(), is("act-b1f6dc28-e534-45f4-a661-3523f051f77a"));
        assertThat(receipts.get(4).getDestinationToken(), is("usr-b4e8ec34-52d8-4a81-9566-bdde1bd745b6"));
        assertThat(receipts.get(4).getAmount(), is("14.00"));
        assertThat(receipts.get(4).getFee(), is("0.00"));
        assertThat(receipts.get(4).getCurrency(), is("USD"));
        assertThat(receipts.get(4).getDetails(), is(notNullValue()));
        assertThat(receipts.get(4).getDetails().getPayeeName(), is("Kevin Puckett"));
        assertThat(receipts.get(4).getDetails().getClientPaymentId(), is("wUOdfLlJONacbdHlAHOAXQT7uwX7LTPy"));

        assertThat(mReceiptDataSource.getErrors().getValue(), is(nullValue()));
        assertThat(mReceiptDataSource.isFetchingData().getValue(), is(false));
    }

    @Test
    public void testLoadAfter_returnNoReceipt() {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[1];
                listener.onSuccess(null);
                return listener;
            }
        }).when(mHyperwallet).listReceipts(any(ReceiptQueryParam.class),
                ArgumentMatchers.<HyperwalletListener<HyperwalletPageList<Receipt>>>any());

        // test
        mReceiptDataSource.loadAfter(mLoadAfterParams, mLoadAfterCallback);

        verify(mHyperwallet).listReceipts(any(ReceiptQueryParam.class),
                ArgumentMatchers.<HyperwalletListener<HyperwalletPageList<Receipt>>>any());
        verify(mLoadAfterCallback, never()).onResult(ArgumentMatchers.<List<Receipt>>any(), anyInt());

        assertThat(mReceiptDataSource.getErrors().getValue(), is(nullValue()));
        assertThat(mReceiptDataSource.isFetchingData().getValue(), is(false));
    }

    @Test
    public void testLoadAfter_withError() {
        final HyperwalletError error = new HyperwalletError("test message load after", "LOAD_AFTER_CODE");
        List<HyperwalletError> errorList = new ArrayList<>();
        errorList.add(error);
        final HyperwalletErrors errors = new HyperwalletErrors(errorList);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[1];
                listener.onFailure(new HyperwalletException(errors));
                return listener;
            }
        }).when(mHyperwallet).listReceipts(any(ReceiptQueryParam.class),
                ArgumentMatchers.<HyperwalletListener<HyperwalletPageList<Receipt>>>any());

        // test
        mReceiptDataSource.loadAfter(mLoadAfterParams, mLoadAfterCallback);

        verify(mHyperwallet).listReceipts(any(ReceiptQueryParam.class),
                ArgumentMatchers.<HyperwalletListener<HyperwalletPageList<Receipt>>>any());
        verify(mLoadAfterCallback, never()).onResult(ArgumentMatchers.<Receipt>anyList(), anyInt());

        // error occurred, this will save params and callback
        assertThat(mReceiptDataSource.getErrors().getValue(), is(notNullValue()));
        assertThat(mReceiptDataSource.getErrors().getValue().getContent().getErrors(),
                Matchers.<HyperwalletError>hasSize(1));
        assertThat(mReceiptDataSource.getErrors().getValue().getContent().getErrors().get(0).getCode(),
                is("LOAD_AFTER_CODE"));
        assertThat(mReceiptDataSource.getErrors().getValue().getContent().getErrors().get(0).getMessage(),
                is("test message load after"));
        assertThat(mReceiptDataSource.isFetchingData().getValue(), is(false));
    }

    @Test
    public void testRetry_loadAfter() {
        final HyperwalletError error = new HyperwalletError("test message", "TEST_CODE");
        List<HyperwalletError> errorList = new ArrayList<>();
        errorList.add(error);
        final HyperwalletErrors errors = new HyperwalletErrors(errorList);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[1];
                listener.onFailure(new HyperwalletException(errors));
                return listener;
            }
        }).when(mHyperwallet).listReceipts(any(ReceiptQueryParam.class),
                ArgumentMatchers.<HyperwalletListener<HyperwalletPageList<Receipt>>>any());

        // test
        mReceiptDataSource.loadAfter(mLoadAfterParams, mLoadAfterCallback);

        verify(mHyperwallet).listReceipts(any(ReceiptQueryParam.class),
                ArgumentMatchers.<HyperwalletListener<HyperwalletPageList<Receipt>>>any());
        verify(mLoadAfterCallback, never()).onResult(ArgumentMatchers.<Receipt>anyList(), anyInt());

        // error occurred, this will save params and callback
        assertThat(mReceiptDataSource.getErrors().getValue(), is(notNullValue()));

        // test retry, saved params and callback will be used and no null pointer exception is thrown
        mReceiptDataSource.retry();

        // verify calls
        verify(mReceiptDataSource, times(2)).loadAfter(
                ArgumentMatchers.<PageKeyedDataSource.LoadParams<Integer>>any(),
                ArgumentMatchers.<PageKeyedDataSource.LoadCallback<Integer, Receipt>>any());
        verify(mReceiptDataSource, never()).loadInitial(
                ArgumentMatchers.<PageKeyedDataSource.LoadInitialParams<Integer>>any(),
                ArgumentMatchers.<PageKeyedDataSource.LoadInitialCallback<Integer, Receipt>>any());
    }
}
