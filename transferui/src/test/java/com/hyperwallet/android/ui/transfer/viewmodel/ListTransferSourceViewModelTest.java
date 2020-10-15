package com.hyperwallet.android.ui.transfer.viewmodel;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.TOKEN;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.TRANSFER_METHOD_COUNTRY;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.TRANSFER_METHOD_CURRENCY;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodTypes.PREPAID_CARD;

import androidx.lifecycle.ViewModel;

import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.HyperwalletAuthenticationTokenProvider;
import com.hyperwallet.android.model.transfermethod.TransferMethod;
import com.hyperwallet.android.ui.transfer.TransferSource;
import com.hyperwallet.android.ui.transfermethod.repository.TransferMethodRepositoryFactory;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ListTransferSourceViewModelTest {

    @Rule
    public final ExpectedException mThrown = ExpectedException.none();

    private ListTransferSourceViewModel mListTransferSourceViewModel;

    @Before
    public void initializedViewModel() {
        Hyperwallet.getInstance(mock(HyperwalletAuthenticationTokenProvider.class));
        ListTransferSourceViewModel.ListTransferSourceViewModelFactory factory =
                new ListTransferSourceViewModel.ListTransferSourceViewModelFactory();

        mListTransferSourceViewModel = spy(factory.create(ListTransferSourceViewModel.class));
    }

    @Test
    public void testGetTransferDestinationSection_returnsLiveData() {
        assertThat(mListTransferSourceViewModel.getTransferSourceSelection(), is(notNullValue()));
    }

    @Test
    public void testSelectTransferDestination_returnsTransferMethod() {
        TransferSource transferSource = new TransferSource();
        mListTransferSourceViewModel.selectedTransferSource(transferSource);
        assertThat(mListTransferSourceViewModel.getTransferSourceSelection().getValue().getContent(),
                is(transferSource));
    }

    @Test
    public void testSelectTransferSourceViewModelFactory_createSelectTransferSourceViewModelUnSuccessful() {
        class DummyViewModel extends ViewModel {
        }

        ListTransferSourceViewModel.ListTransferSourceViewModelFactory factory =
                new ListTransferSourceViewModel.ListTransferSourceViewModelFactory();
        mThrown.expect(IllegalArgumentException.class);
        mThrown.expectMessage(
                "Expecting ViewModel class: com.hyperwallet.android.ui.transfer.viewmodel"
                        + ".ListTransferSourceViewModel");
        factory.create(DummyViewModel.class);
    }

    @Test
    public void testSelectTransferSourceViewModelFactory_createSelectTransferSourceViewModelSuccessful() {
        ListTransferSourceViewModel.ListTransferSourceViewModelFactory factory =
                new ListTransferSourceViewModel.ListTransferSourceViewModelFactory();
        ListTransferSourceViewModel viewModel = factory.create(ListTransferSourceViewModel.class);
        assertThat(viewModel, is(notNullValue()));
    }

    @Test
    public void testLoadTransferSource_AndSetItAsSelected() {

        final TransferMethod transferMethod = new TransferMethod();
        transferMethod.setField(TOKEN, "trm-prepaid_card-test-token");
        transferMethod.setField(TRANSFER_METHOD_CURRENCY, "CAD");
        transferMethod.setField(TRANSFER_METHOD_COUNTRY, "CA");

        final TransferSource transferSource = new TransferSource();
        transferSource.setType(PREPAID_CARD);
        transferSource.setToken("trm-prepaid_card-test-token");
        transferSource.setIdentification(transferMethod);

        assertThat(mListTransferSourceViewModel.getTransferSourceSelection().getValue(), is(nullValue()));
        mListTransferSourceViewModel.selectedTransferSource(transferSource);

        assertThat(mListTransferSourceViewModel.getTransferSourceSelection().getValue(), is(notNullValue()));
        assertThat(mListTransferSourceViewModel.getTransferSourceSelection().getValue().getContent().getToken(),
                is("trm-prepaid_card-test-token"));
        assertThat(
                mListTransferSourceViewModel.getTransferSourceSelection().getValue().getContent().getType(),
                is(PREPAID_CARD));
        assertThat(
                mListTransferSourceViewModel.getTransferSourceSelection().getValue().getContent().getIdentification(),
                is(notNullValue()));

    }

}
