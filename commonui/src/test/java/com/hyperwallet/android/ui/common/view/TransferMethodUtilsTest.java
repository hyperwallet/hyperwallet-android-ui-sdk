package com.hyperwallet.android.ui.common.view;

import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodTypes.VENMO_ACCOUNT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodTypes.BANK_ACCOUNT;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodTypes.BANK_CARD;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodTypes.PAPER_CHECK;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodTypes.PAYPAL_ACCOUNT;
import static com.hyperwallet.android.ui.common.view.TransferMethodUtils.getTransferMethodDetail;

import android.content.Context;
import android.content.res.Resources;

import com.hyperwallet.android.model.transfermethod.BankAccount;
import com.hyperwallet.android.model.transfermethod.BankCard;
import com.hyperwallet.android.model.transfermethod.PaperCheck;
import com.hyperwallet.android.model.transfermethod.PayPalAccount;
import com.hyperwallet.android.model.transfermethod.TransferMethod;
import com.hyperwallet.android.model.transfermethod.VenmoAccount;
import com.hyperwallet.android.ui.common.R;
import com.hyperwallet.android.ui.testutils.rule.HyperwalletExternalResourceManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RobolectricTestRunner;

import java.util.Locale;

@RunWith(RobolectricTestRunner.class)
public class TransferMethodUtilsTest {
    @Rule
    public HyperwalletExternalResourceManager mExternalResourceManager = new HyperwalletExternalResourceManager();
    @Rule
    public MockitoRule mockito = MockitoJUnit.rule();

    @Mock
    Context mContext;
    @Mock
    Resources mResources;

    @Before
    public void setup() {
        when(mContext.getResources()).thenReturn(mResources);
        when(mContext.getPackageName()).thenReturn("package");
    }

    @Test
    public void testGetTransferMethodName_returnsCorrectNameForName() {
        when(mContext.getString(R.string.bank_card)).thenReturn("My bank card");
        String transferMethodName = TransferMethodUtils.getTransferMethodName(mContext, BANK_CARD);
        assertThat(transferMethodName, is("My bank card"));
        verify(mContext, times(1)).getString(anyInt());
    }

    @Test
    public void testGetTransferMethodName_returnsCorrectNameForType() throws JSONException {
        String json = mExternalResourceManager.getResourceContent("bank_card_response.json");
        JSONObject htmJsonObject = new JSONObject(json);
        TransferMethod transferMethod = new TransferMethod(htmJsonObject);
        when(mContext.getString(R.string.bank_card)).thenReturn("My card");
        String transferMethodName = TransferMethodUtils.getTransferMethodName(mContext, transferMethod);
        assertThat(transferMethodName, is("My card"));
        verify(mContext, times(1)).getString(anyInt());
    }

    @Test
    public void testGetTransferMethodName_returnsDefaultValue() {
        when(mContext.getString(R.string.not_translated_in_braces)).thenReturn("(not translated)");
        String transferMethodName = TransferMethodUtils.getTransferMethodName(mContext, "some custom name");
        assertThat(transferMethodName, is("some custom name(not translated)"));
        verify(mContext, times(1)).getString(anyInt());
    }

    @Test
    public void testGetStringResourceByName_returnsCorrectString() {
        String identifier = TransferMethod.TransferMethodTypes.WIRE_ACCOUNT.toLowerCase(Locale.ROOT);
        when(mResources.getIdentifier(eq(identifier), eq("string"), anyString())).thenReturn(R.string.wire_account);
        when(mContext.getString(R.string.wire_account)).thenReturn("wire acc");

        String stringResource = TransferMethodUtils.getStringResourceByName(mContext,
                TransferMethod.TransferMethodTypes.WIRE_ACCOUNT);
        assertThat(stringResource, is("wire acc"));
        verify(mResources, times(1)).getIdentifier(anyString(), anyString(), anyString());
        verify(mContext, times(1)).getString(anyInt());
    }

    @Test
    public void testGetStringResourceByName_returnsDefaultValue() {
        String identifier = TransferMethod.TransferMethodTypes.BANK_ACCOUNT.toLowerCase(Locale.ROOT);
        when(mResources.getIdentifier(eq(identifier), eq("string"), anyString())).thenReturn(R.string.bank_account);
        when(mContext.getString(R.string.bank_account)).thenReturn("bank acc");
        when(mResources.getIdentifier(eq("some type"), eq("string"), anyString())).thenReturn(0);

        String stringResource = TransferMethodUtils.getStringResourceByName(mContext,
                "some type");
        assertThat(stringResource, is("bank acc"));
        verify(mResources, times(2)).getIdentifier(anyString(), anyString(), anyString());
        verify(mContext, times(1)).getString(anyInt());
    }

    @Test
    public void testGetStringFontIcon_returnsCorrectString() {
        String identifier = TransferMethod.TransferMethodTypes.PAPER_CHECK.toLowerCase(Locale.ROOT)
                + "_font_icon";
        when(mResources.getIdentifier(eq(identifier), eq("string"), anyString())).thenReturn(
                R.string.paper_check_font_icon);
        when(mContext.getString(R.string.paper_check_font_icon)).thenReturn("paper icon");

        String stringResource = TransferMethodUtils.getStringFontIcon(mContext,
                TransferMethod.TransferMethodTypes.PAPER_CHECK);
        assertThat(stringResource, is("paper icon"));
        verify(mResources, times(1)).getIdentifier(anyString(), anyString(), anyString());
        verify(mContext, times(1)).getString(anyInt());
    }

    @Test
    public void testGetStringFontIcon_returnsDefaultValue() {
        String identifier = TransferMethod.TransferMethodTypes.BANK_ACCOUNT.toLowerCase(Locale.ROOT)
                + "_font_icon";
        when(mResources.getIdentifier(eq(identifier), eq("string"), anyString())).thenReturn(
                R.string.bank_account_font_icon);
        when(mResources.getIdentifier(eq("some type"), eq("string"), anyString())).thenReturn(0);
        when(mContext.getString(R.string.bank_account_font_icon)).thenReturn("bank icon");

        String stringResource = TransferMethodUtils.getStringFontIcon(mContext,
                "some type");

        assertThat(stringResource, is("bank icon"));
        verify(mResources, times(2)).getIdentifier(anyString(), anyString(), anyString());
        verify(mContext, times(1)).getString(anyInt());
    }

    @Test
    public void getTransferMethodDetail_returnsPayPalDetails() {
        TransferMethod transferMethod = new PayPalAccount.Builder().email(
                "sunshine.carreiro@hyperwallet.com").build();

        when(mContext.getString(ArgumentMatchers.eq(R.string.to),
                eq("sunshine.carreiro@hyperwallet.com"))).thenReturn(
                "to sunshine.carreiro@hyperwallet.com");

        String actual = getTransferMethodDetail(mContext, transferMethod, PAYPAL_ACCOUNT);
        assertThat(actual, is("to sunshine.carreiro@hyperwallet.com"));
    }

    @Test
    public void getTransferMethodDetail_returnsVenmoDetails() {
        TransferMethod transferMethod = new VenmoAccount.Builder()
                .accountId("1234567898").build();

        when(mContext.getString(ArgumentMatchers.eq(R.string.endingIn),
                eq("7898"))).thenReturn("Ending in 7898");

        String actual = getTransferMethodDetail(mContext, transferMethod, VENMO_ACCOUNT);
        assertThat(actual, is("Ending in 7898"));
    }

    @Test
    public void getTransferMethodDetail_returnsCardDetails() {
        TransferMethod transferMethod = new BankCard.Builder().cardNumber(
                "************0006").build();

        when(mContext.getString(ArgumentMatchers.eq(R.string.endingIn),
                eq("0006"))).thenReturn(
                "Ending on 0006");
        String actual = getTransferMethodDetail(mContext, transferMethod, BANK_CARD);
        assertThat(actual, is("Ending on 0006"));
    }

    @Test
    public void getTransferMethodDetail_returnsBankAccountDetails() {
        TransferMethod transferMethod = new BankAccount.Builder().bankAccountId(
                "8017110254").build();

        when(mContext.getString(ArgumentMatchers.eq(R.string.endingIn),
                eq("0254"))).thenReturn(
                "Ending on 0254");
        String actual = getTransferMethodDetail(mContext, transferMethod, BANK_ACCOUNT);
        assertThat(actual, is("Ending on 0254"));
    }

    @Test
    public void getTransferMethodDetail_returnsPaperCheckDetails() {
        TransferMethod transferMethod = new PaperCheck.Builder().transferMethodCurrency("USD").transferMethodCountry("CA").build();
        assertThat(transferMethod.getField(TransferMethod.TransferMethodFields.TRANSFER_METHOD_COUNTRY), is("CA"));
        assertThat(transferMethod.getField(TransferMethod.TransferMethodFields.TRANSFER_METHOD_CURRENCY), is("USD"));
        assertThat(transferMethod.getField(TransferMethod.TransferMethodFields.TYPE), is("PAPER_CHECK"));
    }
}
