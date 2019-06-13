package com.hyperwallet.android.ui.transfermethod;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import static com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod.TransferMethodTypes.BANK_ACCOUNT;
import static com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod.TransferMethodTypes.BANK_CARD;
import static com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod.TransferMethodTypes.PAPER_CHECK;
import static com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod.TransferMethodTypes.PAYPAL_ACCOUNT;

import android.content.Context;

import com.hyperwallet.android.hyperwallet_ui.R;
import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod;
import com.hyperwallet.android.ui.rule.HyperwalletExternalResourceManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class TransferMethodSecondLinePresenterTest {
    private TransferMethodSecondLinePresenter mPresenter;

    @Rule
    public HyperwalletExternalResourceManager mExternalResourceManager = new HyperwalletExternalResourceManager();

    @Before
    public void setUp() {
        mPresenter = new TransferMethodSecondLinePresenter();
    }

    @Test
    public void testGetSecondLine_returnsCachedSecondLine() {
        TransferMethodSecondLine actualStrategy = mPresenter.getSecondLinePresenter(
                HyperwalletTransferMethod.TransferMethodTypes.WIRE_ACCOUNT);
        TransferMethodSecondLine candidateStrategy = mPresenter.getSecondLinePresenter(BANK_ACCOUNT);
        assertThat(actualStrategy, is(candidateStrategy));
    }

    @Test
    public void testGetSecondLine_returnsSecondLine() {
        TransferMethodSecondLine strategy1 = mPresenter.getSecondLinePresenter(BANK_CARD);
        TransferMethodSecondLine strategy2 = mPresenter.getSecondLinePresenter(PAYPAL_ACCOUNT);
        assertThat(strategy1, not(strategy2));
    }

    @Test
    public void testGetSecondLine_returnsEndingOn() {
        TransferMethodSecondLine strategy = mPresenter.getSecondLinePresenter(BANK_CARD);
        assertThat(strategy, is(strategy));

    }

    @Test
    public void testGetSecondLine_returnsPayPalSecondLine() throws JSONException {
        Context context = mock(Context.class);
        String json = mExternalResourceManager.getResourceContent("paypal_response.json");
        JSONObject htmJsonObject = new JSONObject(json);
        HyperwalletTransferMethod transferMethod = new HyperwalletTransferMethod(htmJsonObject);

        TransferMethodSecondLine strategy = mPresenter.getSecondLinePresenter(PAYPAL_ACCOUNT);
        String actual = strategy.getText(context, transferMethod);
        assertThat(strategy, instanceOf(PayPalAccountSecondLine.class));
        assertThat(actual, is("sunshine.carreiro@hyperwallet.com"));
    }

    @Test
    public void testGetSecondLine_returnsCardSecondLine() throws JSONException {
        Context context = mock(Context.class);
        String json = mExternalResourceManager.getResourceContent("bank_card_response.json");
        JSONObject htmJsonObject = new JSONObject(json);
        HyperwalletTransferMethod transferMethod = new HyperwalletTransferMethod(htmJsonObject);
        TransferMethodSecondLine strategy = mPresenter.getSecondLinePresenter(BANK_CARD);
        strategy.getText(context, transferMethod);

        assertThat(strategy, instanceOf(CardSecondLine.class));
        verify(context).getString(eq(R.string.transfer_method_list_item_description), eq("0006"));
    }

    @Test
    public void testGetSecondLine_returnsAccountSecondLine() throws JSONException {
        Context context = mock(Context.class);
        String json = mExternalResourceManager.getResourceContent("bank_account_response.json");
        JSONObject htmJsonObject = new JSONObject(json);
        HyperwalletTransferMethod transferMethod = new HyperwalletTransferMethod(htmJsonObject);
        TransferMethodSecondLine strategy = mPresenter.getSecondLinePresenter(BANK_ACCOUNT);
        strategy.getText(context, transferMethod);

        assertThat(strategy, instanceOf(AccountSecondLine.class));
        verify(context).getString(eq(R.string.transfer_method_list_item_description), eq("0254"));
    }

    @Test
    public void testGetSecondLine_returnsPaperCheckSecondLine() throws JSONException {
        Context context = mock(Context.class);
        String json = mExternalResourceManager.getResourceContent("paper_check_response.json");
        JSONObject htmJsonObject = new JSONObject(json);
        HyperwalletTransferMethod transferMethod = new HyperwalletTransferMethod(htmJsonObject);
        TransferMethodSecondLine strategy = mPresenter.getSecondLinePresenter(PAPER_CHECK);
        String actual = strategy.getText(context, transferMethod);

        assertThat(strategy, instanceOf(TransferMethodSecondLine.class));
        assertThat(actual, is(""));
        verify(context, never()).getString(eq(R.string.transfer_method_list_item_description), anyString());
    }
}
