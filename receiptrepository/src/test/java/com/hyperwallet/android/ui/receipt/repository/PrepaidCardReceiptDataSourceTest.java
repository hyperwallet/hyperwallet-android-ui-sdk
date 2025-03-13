package com.hyperwallet.android.ui.receipt.repository;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import static com.hyperwallet.android.model.receipt.Receipt.Entries.CREDIT;
import static com.hyperwallet.android.model.receipt.Receipt.ReceiptTypes.ADJUSTMENT;
import static com.hyperwallet.android.model.receipt.Receipt.ReceiptTypes.DEPOSIT;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.paging.PageKeyedDataSource;

import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.exception.HyperwalletException;
import com.hyperwallet.android.listener.HyperwalletListener;
import com.hyperwallet.android.model.Error;
import com.hyperwallet.android.model.Errors;
import com.hyperwallet.android.model.paging.PageList;
import com.hyperwallet.android.model.receipt.Receipt;
import com.hyperwallet.android.model.receipt.ReceiptQueryParam;
import com.hyperwallet.android.ui.testutils.rule.HyperwalletExternalResourceManager;
import com.hyperwallet.android.util.DateUtil;

import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.junit.Assert;
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
import java.util.Date;
import java.util.List;
import java.util.Objects;

@RunWith(RobolectricTestRunner.class)
public class PrepaidCardReceiptDataSourceTest {

    // can't be mocked due to params.key is of type Integer and autoboxing will not work with null to 0
    private final PageKeyedDataSource.LoadParams<Date> mLoadAfterParams = new PageKeyedDataSource.LoadParams<>(
            new Date(), 10);
    @Rule
    public MockitoRule mMockito = MockitoJUnit.rule();

    @Rule
    public InstantTaskExecutorRule mInstantTaskExecutor= new InstantTaskExecutorRule();
    @Rule
    public HyperwalletExternalResourceManager mExternalResourceManager = new HyperwalletExternalResourceManager();
    @Mock
    private Hyperwallet mHyperwallet;
    @Mock
    private PageKeyedDataSource.LoadInitialParams<Date> mInitialParams;
    @Mock
    private PageKeyedDataSource.LoadInitialCallback<Date, Receipt> mInitialCallback;
    @Mock
    private PageKeyedDataSource.LoadCallback<Date, Receipt> mLoadAfterCallback;

    @Captor
    private ArgumentCaptor<List<Receipt>> mListArgumentCaptor;
    @Captor
    private ArgumentCaptor<Date> mPreviousCaptor;
    @Captor
    private ArgumentCaptor<Date> mNextCaptor;

    private PrepaidCardReceiptDataSource mPrepaidCardReceiptDataSource;

    @Before
    public void setUp() {
        mPrepaidCardReceiptDataSource = spy(new PrepaidCardReceiptDataSource("test-token"));
        doReturn(mHyperwallet).when(mPrepaidCardReceiptDataSource).getHyperwallet();
    }


    @Test
    public void testLoadInitial_returnsReceipts() throws Exception {
        String json = mExternalResourceManager.getResourceContent("prepaid_card_receipt_list_response.json");
        JSONObject jsonObject = new JSONObject(json);
        final PageList<Receipt> response = new PageList<>(jsonObject, Receipt.class);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[2];
                listener.onSuccess(response);
                return listener;
            }
        }).when(mHyperwallet).listPrepaidCardReceipts(anyString(), any(ReceiptQueryParam.class),
                ArgumentMatchers.<HyperwalletListener<PageList<Receipt>>>any());

        // test
        mPrepaidCardReceiptDataSource.loadInitial(mInitialParams, mInitialCallback);

        // verify
        verify(mHyperwallet).listPrepaidCardReceipts(anyString(), any(ReceiptQueryParam.class),
                ArgumentMatchers.<HyperwalletListener<PageList<Receipt>>>any());
        verify(mInitialCallback).onResult(mListArgumentCaptor.capture(), mPreviousCaptor.capture(),
                mNextCaptor.capture());

        assertThat(mPreviousCaptor.getValue(), is(notNullValue()));
        assertThat(mNextCaptor.getValue(), is(nullValue()));

        // assert receipts information
        List<Receipt> receipts = mListArgumentCaptor.getValue();
        assertThat(receipts, Matchers.<Receipt>hasSize(7));
        assertThat(receipts.get(0).getJournalId(), is("FISVL_5240221"));
        assertThat(receipts.get(0).getType(), is(DEPOSIT));
        assertThat(receipts.get(0).getCreatedOn(), is("2019-06-06T22:48:51"));
        assertThat(receipts.get(0).getEntry(), is(CREDIT));
        assertThat(receipts.get(0).getDestinationToken(), is("test-fake-token"));
        assertThat(receipts.get(0).getAmount(), is("5.00"));
        assertThat(receipts.get(0).getCurrency(), is("USD"));
        assertThat(receipts.get(0).getDetails(), is(notNullValue()));
        assertThat(receipts.get(0).getDetails().getCardNumber(), is("************0673"));
        assertThat(receipts.get(6).getJournalId(), is("FISA_5240226"));
        assertThat(receipts.get(6).getType(), is(ADJUSTMENT));
        assertThat(receipts.get(6).getCreatedOn(), is("2019-02-21T23:55:17"));
        assertThat(receipts.get(6).getEntry(), is(CREDIT));
        assertThat(receipts.get(6).getSourceToken(), is("test-fake-token"));
        assertThat(receipts.get(6).getAmount(), is("9.92"));
        assertThat(receipts.get(6).getCurrency(), is("USD"));
        assertThat(receipts.get(6).getDetails(), is(notNullValue()));
        assertThat(receipts.get(6).getDetails().getCardNumber(), is("************0673"));

        assertThat(mPrepaidCardReceiptDataSource.getErrors().getValue(), is(Matchers.nullValue()));
        assertThat(mPrepaidCardReceiptDataSource.isFetchingData().getValue(), is(false));
    }

    @Test
    public void testLoadInitial_returnNoReceipt() {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[2];
                listener.onSuccess(null);
                return listener;
            }
        }).when(mHyperwallet).listPrepaidCardReceipts(anyString(), any(ReceiptQueryParam.class),
                ArgumentMatchers.<HyperwalletListener<PageList<Receipt>>>any());

        // test
        mPrepaidCardReceiptDataSource.loadInitial(mInitialParams, mInitialCallback);

        // verify
        verify(mHyperwallet).listPrepaidCardReceipts(anyString(), any(ReceiptQueryParam.class),
                ArgumentMatchers.<HyperwalletListener<PageList<Receipt>>>any());
        verify(mInitialCallback, never()).onResult(ArgumentMatchers.<Receipt>anyList(), any(Date.class),
                any(Date.class));

        // assert
        assertThat(mPrepaidCardReceiptDataSource.getErrors().getValue(), is(Matchers.nullValue()));
        assertThat(mPrepaidCardReceiptDataSource.isFetchingData().getValue(), is(false));
    }

    @Test
    public void testLoadInitial_withError() {
        final Error error = new Error("test message", "TEST_CODE");
        List<Error> errorList = new ArrayList<>();
        errorList.add(error);
        final Errors errors = new Errors(errorList);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[2];
                listener.onFailure(new HyperwalletException(errors));
                return listener;
            }
        }).when(mHyperwallet).listPrepaidCardReceipts(anyString(), any(ReceiptQueryParam.class),
                ArgumentMatchers.<HyperwalletListener<PageList<Receipt>>>any());

        // test
        mPrepaidCardReceiptDataSource.loadInitial(mInitialParams, mInitialCallback);

        // verify
        verify(mHyperwallet).listPrepaidCardReceipts(anyString(), any(ReceiptQueryParam.class),
                ArgumentMatchers.<HyperwalletListener<PageList<Receipt>>>any());
        verify(mInitialCallback, never()).onResult(ArgumentMatchers.<Receipt>anyList(), any(Date.class),
                any(Date.class));

        assertThat(mPrepaidCardReceiptDataSource.getErrors().getValue(), is(notNullValue()));
        assertThat(mPrepaidCardReceiptDataSource.getErrors().getValue().getContent().getErrors(),
                Matchers.<Error>hasSize(1));
        assertThat(mPrepaidCardReceiptDataSource.getErrors().getValue().getContent().getErrors().get(0).getCode(),
                is("TEST_CODE"));
        assertThat(mPrepaidCardReceiptDataSource.getErrors().getValue().getContent().getErrors().get(0).getMessage(),
                is("test message"));
        assertThat(mPrepaidCardReceiptDataSource.isFetchingData().getValue(), is(false));
    }

    @Test
    public void testRetry_loadInitial() {
        final Error error = new Error("test message", "TEST_CODE");
        List<Error> errorList = new ArrayList<>();
        errorList.add(error);
        final Errors errors = new Errors(errorList);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[2];
                listener.onFailure(new HyperwalletException(errors));
                return listener;
            }
        }).when(mHyperwallet).listPrepaidCardReceipts(anyString(), any(ReceiptQueryParam.class),
                ArgumentMatchers.<HyperwalletListener<PageList<Receipt>>>any());

        // test
        mPrepaidCardReceiptDataSource.loadInitial(mInitialParams, mInitialCallback);

        // verify
        verify(mHyperwallet).listPrepaidCardReceipts(anyString(), any(ReceiptQueryParam.class),
                ArgumentMatchers.<HyperwalletListener<PageList<Receipt>>>any());
        verify(mInitialCallback, never()).onResult(ArgumentMatchers.<Receipt>anyList(), any(Date.class),
                any(Date.class));

        // error occurred, this will save params and callback
        assertThat(mPrepaidCardReceiptDataSource.getErrors().getValue(), is(notNullValue()));

        // test retry
        mPrepaidCardReceiptDataSource.retry();

        // verify calls
        verify(mPrepaidCardReceiptDataSource, times(2)).loadInitial(
                ArgumentMatchers.<PageKeyedDataSource.LoadInitialParams<Date>>any(),
                ArgumentMatchers.<PageKeyedDataSource.LoadInitialCallback<Date, Receipt>>any());
        verify(mPrepaidCardReceiptDataSource, never()).loadAfter(
                ArgumentMatchers.<PageKeyedDataSource.LoadParams<Date>>any(),
                ArgumentMatchers.<PageKeyedDataSource.LoadCallback<Date, Receipt>>any());
    }

    @Test
    public void testLoadAfter_returnsReceipts() throws Exception {
        String json = mExternalResourceManager.getResourceContent("prepaid_card_receipt_list_response.json");
        JSONObject jsonObject = new JSONObject(json);
        final PageList<Receipt> response = new PageList<>(jsonObject, Receipt.class);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[2];
                listener.onSuccess(response);
                return listener;
            }
        }).when(mHyperwallet).listPrepaidCardReceipts(anyString(), any(ReceiptQueryParam.class),
                ArgumentMatchers.<HyperwalletListener<PageList<Receipt>>>any());

        // test
        mPrepaidCardReceiptDataSource.loadAfter(mLoadAfterParams, mLoadAfterCallback);

        // verify
        verify(mHyperwallet).listPrepaidCardReceipts(anyString(), any(ReceiptQueryParam.class),
                ArgumentMatchers.<HyperwalletListener<PageList<Receipt>>>any());
        verify(mLoadAfterCallback).onResult(mListArgumentCaptor.capture(), mNextCaptor.capture());

        // assert receipts information
        List<Receipt> receipts = mListArgumentCaptor.getValue();
        assertThat(receipts, Matchers.<Receipt>hasSize(7));
        assertThat(receipts.get(1).getJournalId(), is("FISVL_5240221"));
        assertThat(receipts.get(1).getType(), is(DEPOSIT));
        assertThat(receipts.get(1).getCreatedOn(), is("2019-06-06T22:48:51"));
        assertThat(receipts.get(1).getEntry(), is(CREDIT));
        assertThat(receipts.get(1).getDestinationToken(), is("test-fake-token"));
        assertThat(receipts.get(1).getAmount(), is("5.00"));
        assertThat(receipts.get(1).getCurrency(), is("USD"));
        assertThat(receipts.get(1).getDetails(), is(notNullValue()));
        assertThat(receipts.get(1).getDetails().getCardNumber(), is("************0673"));
        assertThat(receipts.get(5).getJournalId(), is("FISA_5240225"));
        assertThat(receipts.get(5).getType(), is(ADJUSTMENT));
        assertThat(receipts.get(5).getCreatedOn(), is("2019-02-23T23:55:17"));
        assertThat(receipts.get(5).getEntry(), is(CREDIT));
        assertThat(receipts.get(5).getSourceToken(), is("test-fake-token"));
        assertThat(receipts.get(5).getAmount(), is("3.90"));
        assertThat(receipts.get(5).getCurrency(), is("USD"));
        assertThat(receipts.get(5).getDetails(), is(notNullValue()));
        assertThat(receipts.get(5).getDetails().getCardNumber(), is("************0673"));

        assertThat(mNextCaptor.getValue(), is(DateUtil.fromDateTimeString(receipts.get(6).getCreatedOn())));
        assertThat(mPrepaidCardReceiptDataSource.getErrors().getValue(), is(Matchers.nullValue()));
        assertThat(mPrepaidCardReceiptDataSource.isFetchingData().getValue(), is(false));
    }

    @Test
    public void testLoadAfter_returnNoReceipt() {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[2];
                listener.onSuccess(null);
                return listener;
            }
        }).when(mHyperwallet).listPrepaidCardReceipts(anyString(), any(ReceiptQueryParam.class),
                ArgumentMatchers.<HyperwalletListener<PageList<Receipt>>>any());

        // test
        mPrepaidCardReceiptDataSource.loadAfter(mLoadAfterParams, mLoadAfterCallback);

        // verify
        verify(mHyperwallet).listPrepaidCardReceipts(anyString(), any(ReceiptQueryParam.class),
                ArgumentMatchers.<HyperwalletListener<PageList<Receipt>>>any());
        verify(mLoadAfterCallback, never()).onResult(ArgumentMatchers.<Receipt>anyList(), any(Date.class));

        // assert
        assertThat(mPrepaidCardReceiptDataSource.getErrors().getValue(), is(Matchers.nullValue()));
        assertThat(mPrepaidCardReceiptDataSource.isFetchingData().getValue(), is(false));
    }

    @Test
    public void testLoadAfter_withError() {
        final Error error = new Error("test message", "TEST_CODE");
        List<Error> errorList = new ArrayList<>();
        errorList.add(error);
        final Errors errors = new Errors(errorList);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[2];
                listener.onFailure(new HyperwalletException(errors));
                return listener;
            }
        }).when(mHyperwallet).listPrepaidCardReceipts(anyString(), any(ReceiptQueryParam.class),
                ArgumentMatchers.<HyperwalletListener<PageList<Receipt>>>any());

        // test
        mPrepaidCardReceiptDataSource.loadAfter(mLoadAfterParams, mLoadAfterCallback);

        // verify
        verify(mHyperwallet).listPrepaidCardReceipts(anyString(), any(ReceiptQueryParam.class),
                ArgumentMatchers.<HyperwalletListener<PageList<Receipt>>>any());
        verify(mLoadAfterCallback, never()).onResult(ArgumentMatchers.<Receipt>anyList(), any(Date.class));

        assertThat(mPrepaidCardReceiptDataSource.getErrors().getValue(), is(notNullValue()));
        assertThat(mPrepaidCardReceiptDataSource.getErrors().getValue().getContent().getErrors(),
                Matchers.<Error>hasSize(1));
        assertThat(mPrepaidCardReceiptDataSource.getErrors().getValue().getContent().getErrors().get(0).getCode(),
                is("TEST_CODE"));
        assertThat(mPrepaidCardReceiptDataSource.getErrors().getValue().getContent().getErrors().get(0).getMessage(),
                is("test message"));
        assertThat(mPrepaidCardReceiptDataSource.isFetchingData().getValue(), is(false));
    }

    @Test
    public void testRetry_loadAfter() {
        final Error error = new Error("test message", "TEST_CODE");
        List<Error> errorList = new ArrayList<>();
        errorList.add(error);
        final Errors errors = new Errors(errorList);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[2];
                listener.onFailure(new HyperwalletException(errors));
                return listener;
            }
        }).when(mHyperwallet).listPrepaidCardReceipts(anyString(), any(ReceiptQueryParam.class),
                ArgumentMatchers.<HyperwalletListener<PageList<Receipt>>>any());

        // test
        mPrepaidCardReceiptDataSource.loadAfter(mLoadAfterParams, mLoadAfterCallback);

        // verify
        verify(mHyperwallet).listPrepaidCardReceipts(anyString(), any(ReceiptQueryParam.class),
                ArgumentMatchers.<HyperwalletListener<PageList<Receipt>>>any());
        verify(mLoadAfterCallback, never()).onResult(ArgumentMatchers.<Receipt>anyList(), any(Date.class));

        // error occurred, this will save params and callback
        assertThat(mPrepaidCardReceiptDataSource.getErrors().getValue(), is(notNullValue()));

        // test retry
        mPrepaidCardReceiptDataSource.retry();

        // verify calls
        verify(mPrepaidCardReceiptDataSource, never()).loadInitial(
                ArgumentMatchers.<PageKeyedDataSource.LoadInitialParams<Date>>any(),
                ArgumentMatchers.<PageKeyedDataSource.LoadInitialCallback<Date, Receipt>>any());
        verify(mPrepaidCardReceiptDataSource, times(2)).loadAfter(
                ArgumentMatchers.<PageKeyedDataSource.LoadParams<Date>>any(),
                ArgumentMatchers.<PageKeyedDataSource.LoadCallback<Date, Receipt>>any());
    }

    @Test
    public void testGetNextDate_verifyDefaultValue() {
        Date date = mPrepaidCardReceiptDataSource.getNextDate(new PageList<Receipt>(null));
        assertThat(date, is(notNullValue()));
    }

    @Test
    public void testSortPrepaidCardReceiptsByDateAndDesc_returnsReceipts() throws Exception {
        String json = mExternalResourceManager.getResourceContent("prepaid_card_receipt_list_response.json");
        JSONObject jsonObject = new JSONObject(json);
        final PageList<Receipt> response = new PageList<>(jsonObject, Receipt.class);

        mPrepaidCardReceiptDataSource.sortPrepaidCardReceiptsByDateAndDesc(response);

        Assert.assertThat(response, Matchers.is(Matchers.notNullValue()));
        Assert.assertThat(response.getDataList(), Matchers.is(Matchers.notNullValue()));
        Assert.assertThat(response.getDataList().size(), Matchers.is(7));

        // assert receipts information
        List<Receipt> receipts = response.getDataList();
        assertThat(receipts, Matchers.<Receipt>hasSize(7));
        assertThat(receipts.get(0).getJournalId(), is("FISVL_5240221"));
        assertThat(receipts.get(0).getType(), is(DEPOSIT));
        assertThat(receipts.get(0).getCreatedOn(), is("2019-06-06T22:48:51"));
        assertThat(receipts.get(0).getEntry(), is(CREDIT));
        assertThat(receipts.get(0).getDestinationToken(), is("test-fake-token"));
        assertThat(receipts.get(0).getAmount(), is("5.00"));
        assertThat(receipts.get(0).getCurrency(), is("USD"));
        assertThat(receipts.get(0).getDetails(), is(notNullValue()));
        assertThat(receipts.get(0).getDetails().getCardNumber(), is("************0673"));
        assertThat(receipts.get(6).getJournalId(), is("FISA_5240226"));
        assertThat(receipts.get(6).getType(), is(ADJUSTMENT));
        assertThat(receipts.get(6).getCreatedOn(), is("2019-02-21T23:55:17"));
        assertThat(receipts.get(6).getEntry(), is(CREDIT));
        assertThat(receipts.get(6).getSourceToken(), is("test-fake-token"));
        assertThat(receipts.get(6).getAmount(), is("9.92"));
        assertThat(receipts.get(6).getCurrency(), is("USD"));
        assertThat(receipts.get(6).getDetails(), is(notNullValue()));
        assertThat(receipts.get(6).getDetails().getCardNumber(), is("************0673"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testListPrepaidCardReceiptViewModelFactory_throwsExceptionOnInvalidClassArgument() throws Exception {
        String json = mExternalResourceManager.getResourceContent("prepaid_card_receipt_list_response.json");
        JSONObject jsonObject = new JSONObject(json);
        final PageList<Receipt> response = new PageList<>(jsonObject, Receipt.class);

        List<Receipt> resultDataList = new ArrayList<>();
        resultDataList.add(null);
        resultDataList.add(null);
        mPrepaidCardReceiptDataSource.parseDate("");

        Assert.assertThat(response, Matchers.is(Matchers.notNullValue()));
        Assert.assertThat(resultDataList, Matchers.is(Matchers.notNullValue()));
        Assert.assertThat(resultDataList.size(), Matchers.is(7));
    }
}
