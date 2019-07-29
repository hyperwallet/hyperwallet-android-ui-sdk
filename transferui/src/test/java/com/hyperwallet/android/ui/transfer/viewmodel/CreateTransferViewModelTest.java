package com.hyperwallet.android.ui.transfer.viewmodel;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.spy;

import static java.net.HttpURLConnection.HTTP_OK;

import android.content.Intent;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProviders;

import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.ui.transfer.repository.TransferRepositoryFactory;
import com.hyperwallet.android.ui.transfer.rule.HyperwalletExternalResourceManager;
import com.hyperwallet.android.ui.transfer.rule.HyperwalletMockWebServer;
import com.hyperwallet.android.ui.transfer.util.TestAuthenticationProvider;
import com.hyperwallet.android.ui.transfer.view.CreateTransferActivity;
import com.hyperwallet.android.ui.transfermethod.repository.TransferMethodRepositoryFactory;
import com.hyperwallet.android.ui.user.repository.UserRepositoryFactory;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class CreateTransferViewModelTest {

    @Rule
    public HyperwalletExternalResourceManager mResourceManager = new HyperwalletExternalResourceManager();
    @Rule
    public HyperwalletMockWebServer mMockWebServer = new HyperwalletMockWebServer(8080);
    @Rule
    public ExpectedException mExpectedException = ExpectedException.none();

    @Before
    public void setup() {
        Hyperwallet.getInstance(new TestAuthenticationProvider());
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(mResourceManager
                .getResourceContent("authentication_token_response.json")).mock();
    }

    @Test
    public void testCreateTransferViewModel_initializeWithFundingSource() {
        String ppcToken = "trm-ppc-token";
        Intent intent = new Intent();
        intent.putExtra(CreateTransferActivity.EXTRA_TRANSFER_SOURCE_TOKEN, ppcToken);

        CreateTransferActivity activity = Robolectric.buildActivity(CreateTransferActivity.class, intent).setup().get();
        CreateTransferViewModel model = spy(ViewModelProviders.of(activity).get(CreateTransferViewModel.class));

        assertThat(model, is(notNullValue()));
        assertThat(model.isTransferAllAvailableFunds().getValue(), is(false));
        assertThat(model.isCreateQuoteLoading().getValue(), is(false));
    }

    @Test
    public void testCreateTransferViewModel_initializeWithoutFundingSource() {
        CreateTransferActivity activity = Robolectric.buildActivity(CreateTransferActivity.class).setup().get();
        CreateTransferViewModel model = spy(ViewModelProviders.of(activity).get(CreateTransferViewModel.class));

        assertThat(model, is(notNullValue()));
        assertThat(model.isTransferAllAvailableFunds().getValue(), is(false));
        assertThat(model.isCreateQuoteLoading().getValue(), is(false));
    }

    @Test
    public void testCreateTransferViewModelFactory_createCreateTransferViewModelUnSuccessful() {
        CreateTransferViewModel.CreateTransferViewModelFactory factory =
                new CreateTransferViewModel.CreateTransferViewModelFactory(
                        TransferRepositoryFactory.getInstance().getTransferRepository(),
                        TransferMethodRepositoryFactory.getInstance().getTransferMethodRepository(),
                        UserRepositoryFactory.getInstance().getUserRepository());
        mExpectedException.expect(IllegalArgumentException.class);
        mExpectedException.expectMessage(
                "Expecting ViewModel class: com.hyperwallet.android.ui.transfer.viewmodel.CreateTransferViewModel");
        factory.create(FakeModel.class);
    }

    @Test
    public void testCreateTransferViewModelFactory_createCreateTransferViewModelSuccessful() {
        CreateTransferViewModel.CreateTransferViewModelFactory factory =
                new CreateTransferViewModel.CreateTransferViewModelFactory(
                        TransferRepositoryFactory.getInstance().getTransferRepository(),
                        TransferMethodRepositoryFactory.getInstance().getTransferMethodRepository(),
                        UserRepositoryFactory.getInstance().getUserRepository());
        CreateTransferViewModel viewModel = factory.create(CreateTransferViewModel.class);
        assertThat(viewModel, is(notNullValue()));
    }

    class FakeModel extends ViewModel {
    }
}
