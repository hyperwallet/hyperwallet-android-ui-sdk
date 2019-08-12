package com.hyperwallet.android.ui.transfer.viewmodel;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

import static com.hyperwallet.android.model.transfer.Transfer.TransferStatuses.QUOTED;
import static com.hyperwallet.android.model.transfer.Transfer.TransferStatuses.SCHEDULED;
import static com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod.TransferMethodFields.TOKEN;
import static com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod.TransferMethodFields.TRANSFER_METHOD_COUNTRY;
import static com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod.TransferMethodFields.TRANSFER_METHOD_CURRENCY;

import androidx.lifecycle.ViewModel;

import com.hyperwallet.android.model.HyperwalletError;
import com.hyperwallet.android.model.HyperwalletErrors;
import com.hyperwallet.android.model.StatusTransition;
import com.hyperwallet.android.model.TypeReference;
import com.hyperwallet.android.model.transfer.Transfer;
import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod;
import com.hyperwallet.android.ui.testutils.rule.HyperwalletExternalResourceManager;
import com.hyperwallet.android.ui.transfer.repository.TransferRepository;
import com.hyperwallet.android.util.JsonUtils;

import org.hamcrest.Matchers;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;

import java.util.Date;

@RunWith(RobolectricTestRunner.class)
public class ScheduleTransferViewModelTest {

    @Rule
    public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule
    public HyperwalletExternalResourceManager mResourceManager = new HyperwalletExternalResourceManager();
    @Rule
    public ExpectedException mExpectedException = ExpectedException.none();

    @Mock
    private TransferRepository mTransferRepository;

    private ScheduleTransferViewModel mScheduleTransferViewModel;

    @Before
    public void setup() {
        // setting defaults
        Transfer transfer = new Transfer.Builder()
                .token("trf-transfer-token")
                .status(QUOTED)
                .createdOn(new Date())
                .clientTransferID("ClientId1234122")
                .sourceToken("usr-source-token")
                .sourceCurrency("CAD")
                .destinationToken("trm-bank-token")
                .destinationAmount("123.23")
                .destinationCurrency("CAD")
                .memo("Create quote test notes")
                .build();

        HyperwalletTransferMethod transferMethod = new HyperwalletTransferMethod();
        transferMethod.setField(TOKEN, "trm-canadian-bank-token");
        transferMethod.setField(TRANSFER_METHOD_CURRENCY, "CAD");
        transferMethod.setField(TRANSFER_METHOD_COUNTRY, "CA");

        ScheduleTransferViewModel.ScheduleTransferViewModelFactory factory =
                new ScheduleTransferViewModel.ScheduleTransferViewModelFactory(mTransferRepository);
        mScheduleTransferViewModel = factory.create(ScheduleTransferViewModel.class);
        mScheduleTransferViewModel.setTransfer(transfer);
        mScheduleTransferViewModel.setTransferDestination(transferMethod);
    }

    @Test
    public void testGetTransfer_returnsTransfer() {
        assertThat(mScheduleTransferViewModel.getTransfer(), is(notNullValue()));
        assertThat(mScheduleTransferViewModel.getTransfer().getToken(), is("trf-transfer-token"));
        assertThat(mScheduleTransferViewModel.getTransfer().getClientTransferId(), is("ClientId1234122"));
        assertThat(mScheduleTransferViewModel.getTransfer().getStatus(), is(QUOTED));
    }

    @Test
    public void testGetTransferDestination_returnsTransferDestination() {
        assertThat(mScheduleTransferViewModel.getTransferDestination(), is(notNullValue()));
        assertThat(mScheduleTransferViewModel.getTransferDestination().getField(TOKEN), is("trm-canadian-bank-token"));
        assertThat(mScheduleTransferViewModel.getTransferDestination().getField(TRANSFER_METHOD_COUNTRY), is("CA"));
        assertThat(mScheduleTransferViewModel.getTransferDestination().getField(TRANSFER_METHOD_CURRENCY), is("CAD"));
    }

    @Test
    public void testGetTransferStatusTransitionError_returnsLiveData() {
        assertThat(mScheduleTransferViewModel.getTransferStatusTransition(), is(notNullValue()));
        assertThat(mScheduleTransferViewModel.getTransferStatusTransition().getValue(), is(nullValue()));
    }

    @Test
    public void testIsScheduleTransferLoading_returnsLiveData() {
        assertThat(mScheduleTransferViewModel.isScheduleTransferLoading(), is(notNullValue()));
        assertThat(mScheduleTransferViewModel.isScheduleTransferLoading().getValue(), is(Boolean.FALSE));
    }

    @Test
    public void testScheduleTransfer_successful() throws JSONException {
        String commitResponse = mResourceManager.getResourceContent("commit_sts_response.json");
        final StatusTransition statusTransition = new StatusTransition(new JSONObject(commitResponse));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferRepository.ScheduleTransferCallback callback = invocation.getArgument(1);
                callback.onTransferScheduled(statusTransition);
                return callback;
            }
        }).when(mTransferRepository).scheduleTransfer(any(Transfer.class), any(
                TransferRepository.ScheduleTransferCallback.class));

        // test
        mScheduleTransferViewModel.scheduleTransfer();

        assertThat(mScheduleTransferViewModel.getTransferStatusTransition().getValue(), is(notNullValue()));
        assertThat(mScheduleTransferViewModel.getTransferStatusTransition().getValue().getToken(), is("sts-token"));
        assertThat(mScheduleTransferViewModel.getTransferStatusTransition().getValue().getCreatedOn(),
                is("2019-08-06T08:27:45"));
        assertThat(mScheduleTransferViewModel.getTransferStatusTransition().getValue().getTransition(), is(SCHEDULED));
        assertThat(mScheduleTransferViewModel.getTransferStatusTransition().getValue().getFromStatus(), is(QUOTED));
        assertThat(mScheduleTransferViewModel.getTransferStatusTransition().getValue().getToStatus(), is(SCHEDULED));
        assertThat(mScheduleTransferViewModel.getTransferStatusTransition().getValue().getNotes(),
                is("Completing the Partial-Balance Transfer"));
    }

    @Test
    public void testScheduleTransfer_unsuccessful() throws Exception {
        String errorResponse = mResourceManager.getResourceContent("commit_transfer_timeout.json");
        final HyperwalletErrors errors = JsonUtils.fromJsonString(errorResponse,
                new TypeReference<HyperwalletErrors>() {
                });

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                TransferRepository.ScheduleTransferCallback callback = invocation.getArgument(1);
                callback.onError(errors);
                return callback;
            }
        }).when(mTransferRepository).scheduleTransfer(any(Transfer.class), any(
                TransferRepository.ScheduleTransferCallback.class));

        // test
        mScheduleTransferViewModel.scheduleTransfer();

        assertThat(mScheduleTransferViewModel.getTransferStatusTransitionError().getValue(), is(notNullValue()));
        assertThat(mScheduleTransferViewModel.getTransferStatusTransitionError().getValue().getContent().getErrors(),
                Matchers.<HyperwalletError>hasSize(1));
        assertThat(mScheduleTransferViewModel.getTransferStatusTransitionError().getValue().getContent()
                .getErrors().get(0).getCode(), is("EXPIRED_TRANSFER"));
        assertThat(mScheduleTransferViewModel.getTransferStatusTransitionError().getValue().getContent()
                        .getErrors().get(0).getMessage(),
                is("The transfer request has expired on Mon Aug 12 16:33:10 PDT 2019. Please create a new transfer "
                        + "and commit it before 120 seconds."));
    }

    @Test
    public void testCreate_createScheduleTransferViewModelSuccess() {
        ScheduleTransferViewModel.ScheduleTransferViewModelFactory factory =
                new ScheduleTransferViewModel.ScheduleTransferViewModelFactory(mTransferRepository);
        ScheduleTransferViewModel scheduleTransferViewModel = factory.create(ScheduleTransferViewModel.class);
        assertThat(scheduleTransferViewModel, is(notNullValue()));
    }

    class FakeModel extends ViewModel {
    }

    @Test
    public void testCreate_createScheduleTransferViewModelUnSuccessful() {
        ScheduleTransferViewModel.ScheduleTransferViewModelFactory factory =
                new ScheduleTransferViewModel.ScheduleTransferViewModelFactory(mTransferRepository);

        mExpectedException.expect(IllegalArgumentException.class);
        mExpectedException.expectMessage(
                "Expecting ViewModel class: com.hyperwallet.android.ui.transfer.viewmodel.ScheduleTransferViewModel");

        factory.create(FakeModel.class);
    }
}
