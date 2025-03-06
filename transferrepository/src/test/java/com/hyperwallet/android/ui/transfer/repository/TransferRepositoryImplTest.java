package com.hyperwallet.android.ui.transfer.repository;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import static com.hyperwallet.android.model.StatusTransition.StatusDefinition.QUOTED;
import static com.hyperwallet.android.model.StatusTransition.StatusDefinition.SCHEDULED;

import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.exception.HyperwalletException;
import com.hyperwallet.android.listener.HyperwalletListener;
import com.hyperwallet.android.model.Error;
import com.hyperwallet.android.model.Errors;
import com.hyperwallet.android.model.StatusTransition;
import com.hyperwallet.android.model.TypeReference;
import com.hyperwallet.android.model.transfer.Transfer;
import com.hyperwallet.android.ui.testutils.rule.HyperwalletExternalResourceManager;
import com.hyperwallet.android.util.JsonUtils;

import org.hamcrest.Matchers;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;

import java.util.Date;
import java.util.UUID;

@RunWith(RobolectricTestRunner.class)
public class TransferRepositoryImplTest {

    @Rule
    public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule
    public HyperwalletExternalResourceManager mResourceManager = new HyperwalletExternalResourceManager();
    @Mock
    private Hyperwallet mHyperwallet;
    @Mock
    private TransferRepository.CreateTransferCallback mCreateTransferCallback;
    @Mock
    private TransferRepository.ScheduleTransferCallback mScheduleTransferCallback;

    @Captor
    private ArgumentCaptor<StatusTransition> mStatusTransitionArgumentCaptor;
    @Captor
    private ArgumentCaptor<Transfer> mCreateTransferArgumentCaptor;
    @Captor
    private ArgumentCaptor<Errors> mErrorsArgumentCaptor;

    @Spy
    private TransferRepositoryImpl mTransferRepository;

    @Before
    public void setUp() {
        doReturn(mHyperwallet).when(mTransferRepository).getHyperwallet();
    }

    @Test
    public void testCreateTransfer_onSuccess() throws JSONException {
        String responseJson = mResourceManager.getResourceContent("transfer_success_response.json");
        JSONObject jsonObject = new JSONObject(responseJson);
        final Transfer transfer = new Transfer(jsonObject);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = invocation.getArgument(1);
                listener.onSuccess(transfer);
                return listener;
            }
        }).when(mHyperwallet).createTransfer(any(Transfer.class), any(HyperwalletListener.class));

        Transfer transferRequest = new Transfer.Builder()
                .clientTransferID(UUID.randomUUID().toString())
                .createdOn(new Date())
                .destinationAmount("20.00")
                .destinationCurrency("USD")
                .destinationToken("trm-token")
                .sourceToken("usr-token")
                .build();

        // test
        mTransferRepository.createTransfer(transferRequest, mCreateTransferCallback);

        verify(mCreateTransferCallback).onTransferCreated(mCreateTransferArgumentCaptor.capture());
        verify(mCreateTransferCallback, never()).onError(any(Errors.class));

        Transfer capturedTransfer = mCreateTransferArgumentCaptor.getValue();
        assertThat(capturedTransfer, is(notNullValue()));
        assertThat(capturedTransfer.getStatus(), is(QUOTED));
        assertThat(capturedTransfer.getToken(), is("trf-token"));
        assertThat(capturedTransfer.getDestinationToken(), is("trm-token"));
        assertThat(capturedTransfer.getSourceToken(), is("usr-token"));
    }

    @Test
    public void testCreateTransfer_onFailure() throws Exception {
        String errorResponse = mResourceManager.getResourceContent("transfer_error_response.json");
        final Errors errors = JsonUtils.fromJsonString(errorResponse,
                new TypeReference<Errors>() {
                });

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = invocation.getArgument(1);
                listener.onFailure(new HyperwalletException(errors));
                return listener;
            }
        }).when(mHyperwallet).createTransfer(any(Transfer.class), any(HyperwalletListener.class));

        Transfer transferRequest = new Transfer.Builder()
                .clientTransferID(UUID.randomUUID().toString())
                .createdOn(new Date())
                .destinationAmount("20.00")
                .destinationCurrency("USD")
                .destinationToken("trm-token")
                .sourceToken("usr-token")
                .build();

        // test
        mTransferRepository.createTransfer(transferRequest, mCreateTransferCallback);

        verify(mCreateTransferCallback, never()).onTransferCreated(any(Transfer.class));
        verify(mCreateTransferCallback).onError(mErrorsArgumentCaptor.capture());

        Errors capturedErrors = mErrorsArgumentCaptor.getValue();
        assertThat(capturedErrors, is(notNullValue()));
        assertThat(capturedErrors.getErrors(), Matchers.<Error>hasSize(1));
        assertThat(capturedErrors.getErrors().get(0).getCode(), is("INVALID_SOURCE_TOKEN"));
        assertThat(capturedErrors.getErrors().get(0).getMessage(),
                is("The source token you provided doesn’t exist or is not a valid source."));
    }

    @Test
    public void testScheduledTransfer_onSuccess() throws Exception {
        String responseJson = mResourceManager.getResourceContent("transfer_scheduled_success_response.json");
        JSONObject jsonObject = new JSONObject(responseJson);
        final StatusTransition statusTransition = new StatusTransition(jsonObject);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = invocation.getArgument(2);
                listener.onSuccess(statusTransition);
                return listener;
            }
        }).when(mHyperwallet).scheduleTransfer(anyString(), anyString(), any(HyperwalletListener.class));

        Transfer transferRequest = new Transfer.Builder()
                .clientTransferID(UUID.randomUUID().toString())
                .createdOn(new Date())
                .destinationAmount("20.00")
                .destinationCurrency("USD")
                .destinationToken("trm-token")
                .sourceToken("usr-token")
                .token("trf-token")
                .notes("this is the notes")
                .build();

        // test
        mTransferRepository.scheduleTransfer(transferRequest, mScheduleTransferCallback);

        verify(mScheduleTransferCallback).onTransferScheduled(mStatusTransitionArgumentCaptor.capture());
        verify(mScheduleTransferCallback, never()).onError(any(Errors.class));

        StatusTransition scheduledTransition = mStatusTransitionArgumentCaptor.getValue();
        assertThat(scheduledTransition, is(notNullValue()));
        assertThat(scheduledTransition.getToken(), is("sts-token"));
        assertThat(scheduledTransition.getCreatedOn(), is("2019-07-23T15:20:17"));
        assertThat(scheduledTransition.getTransition(), is(SCHEDULED));
        assertThat(scheduledTransition.getFromStatus(), is(QUOTED));
        assertThat(scheduledTransition.getToStatus(), is(SCHEDULED));
        assertThat(scheduledTransition.getNotes(), is("Completing the Partial-Balance Transfer"));
    }

    @Test
    public void testScheduledTransfer_onFailure() throws Exception {
        String errorResponse = mResourceManager.getResourceContent("transfer_schedule_error_response.json");
        final Errors errors = JsonUtils.fromJsonString(errorResponse,
                new TypeReference<Errors>() {
                });

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = invocation.getArgument(2);
                listener.onFailure(new HyperwalletException(errors));
                return listener;
            }
        }).when(mHyperwallet).scheduleTransfer(anyString(), anyString(), any(HyperwalletListener.class));

        Transfer transferRequest = new Transfer.Builder()
                .clientTransferID(UUID.randomUUID().toString())
                .createdOn(new Date())
                .destinationAmount("20.00")
                .destinationCurrency("USD")
                .destinationToken("trm-token")
                .sourceToken("usr-token")
                .token("trf-token")
                .notes("this is the notes")
                .build();

        // test
        mTransferRepository.scheduleTransfer(transferRequest, mScheduleTransferCallback);

        verify(mScheduleTransferCallback, never()).onTransferScheduled(any(StatusTransition.class));
        verify(mScheduleTransferCallback).onError(mErrorsArgumentCaptor.capture());

        Errors capturedErrors = mErrorsArgumentCaptor.getValue();
        assertThat(capturedErrors, is(notNullValue()));
        assertThat(capturedErrors.getErrors(), Matchers.<Error>hasSize(1));
        assertThat(capturedErrors.getErrors().get(0).getCode(), is("INVALID_STATUS_TRANSITION"));
        assertThat(capturedErrors.getErrors().get(0).getMessage(), is("This status transition is not allowed."));
    }
}
