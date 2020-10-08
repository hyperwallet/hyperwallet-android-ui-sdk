package com.hyperwallet.android.ui.transfer.viewmodel;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.TOKEN;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.TRANSFER_METHOD_COUNTRY;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.TRANSFER_METHOD_CURRENCY;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodTypes.BANK_ACCOUNT;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodTypes.PREPAID_CARD;

import androidx.lifecycle.ViewModel;

import com.google.common.collect.Lists;
import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.HyperwalletAuthenticationTokenProvider;
import com.hyperwallet.android.model.Error;
import com.hyperwallet.android.model.Errors;
import com.hyperwallet.android.model.transfermethod.TransferMethod;
import com.hyperwallet.android.ui.transfer.TransferSourceWrapper;
import com.hyperwallet.android.ui.transfermethod.repository.TransferMethodRepository;
import com.hyperwallet.android.ui.transfermethod.repository.TransferMethodRepositoryFactory;
import com.hyperwallet.android.ui.transfermethod.repository.TransferMethodRepositoryImpl;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;

@RunWith(RobolectricTestRunner.class)
public class ListSourceDestinationViewModelTest {

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
        TransferSourceWrapper transferSourceWrapper = new TransferSourceWrapper();
        mListTransferSourceViewModel.selectedTransferSource(transferSourceWrapper);
        assertThat(mListTransferSourceViewModel.getTransferSourceSelection().getValue().getContent(),
                is(transferSourceWrapper));
    }

    @Test
    public void testLoadTransferSource_AndSetItAsSelected() {

        final TransferMethod transferMethod = new TransferMethod();
        transferMethod.setField(TOKEN, "trm-prepaid_card-test-token");
        transferMethod.setField(TRANSFER_METHOD_CURRENCY, "CAD");
        transferMethod.setField(TRANSFER_METHOD_COUNTRY, "CA");

        final TransferSourceWrapper transferSourceWrapper = new TransferSourceWrapper();
        transferSourceWrapper.setType(PREPAID_CARD);
        transferSourceWrapper.setToken("trm-prepaid_card-test-token");
        transferSourceWrapper.setIdentification(transferMethod);

        assertThat(mListTransferSourceViewModel.getTransferSourceSelection().getValue(), is(nullValue()));
        mListTransferSourceViewModel.selectedTransferSource(transferSourceWrapper);

        assertThat(mListTransferSourceViewModel.getTransferSourceSelection().getValue(), is(notNullValue()));
        assertThat(mListTransferSourceViewModel.getTransferSourceSelection().getValue().getContent().getToken(),
                is("trm-prepaid_card-test-token"));
        assertThat(
                mListTransferSourceViewModel.getTransferSourceSelection().getValue().getContent().getType(),
                is(PREPAID_CARD));
        assertThat(mListTransferSourceViewModel.getTransferSourceSelection().getValue().getContent().getIdentification(),
                is(notNullValue()));

    }

}
