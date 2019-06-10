package com.hyperwallet.android.ui.transfermethod;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod.TransferMethodTypes.BANK_CARD;

import android.content.Context;
import android.content.res.Resources;

import com.hyperwallet.android.hyperwallet.ui.R;
import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod;
import com.hyperwallet.android.ui.rule.HyperwalletExternalResourceManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
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
        HyperwalletTransferMethod hyperwalletTransferMethod = new HyperwalletTransferMethod(htmJsonObject);
        when(mContext.getString(R.string.bank_card)).thenReturn("My card");
        String transferMethodName = TransferMethodUtils.getTransferMethodName(mContext, hyperwalletTransferMethod);
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
        String identifier = HyperwalletTransferMethod.TransferMethodTypes.WIRE_ACCOUNT.toLowerCase(Locale.ROOT);
        when(mResources.getIdentifier(eq(identifier), eq("string"), anyString())).thenReturn(R.string.wire_account);
        when(mContext.getString(R.string.wire_account)).thenReturn("wire acc");

        String stringResource = TransferMethodUtils.getStringResourceByName(mContext,
                HyperwalletTransferMethod.TransferMethodTypes.WIRE_ACCOUNT);
        assertThat(stringResource, is("wire acc"));
        verify(mResources, times(1)).getIdentifier(anyString(), anyString(), anyString());
        verify(mContext, times(1)).getString(anyInt());
    }

    @Test
    public void testGetStringResourceByName_returnsDefaultValue() {
        String identifier = HyperwalletTransferMethod.TransferMethodTypes.BANK_ACCOUNT.toLowerCase(Locale.ROOT);
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
        String identifier = HyperwalletTransferMethod.TransferMethodTypes.PAPER_CHECK.toLowerCase(Locale.ROOT)
                + "_font_icon";
        when(mResources.getIdentifier(eq(identifier), eq("string"), anyString())).thenReturn(
                R.string.paper_check_font_icon);
        when(mContext.getString(R.string.paper_check_font_icon)).thenReturn("paper icon");

        String stringResource = TransferMethodUtils.getStringFontIcon(mContext,
                HyperwalletTransferMethod.TransferMethodTypes.PAPER_CHECK);
        assertThat(stringResource, is("paper icon"));
        verify(mResources, times(1)).getIdentifier(anyString(), anyString(), anyString());
        verify(mContext, times(1)).getString(anyInt());
    }

    @Test
    public void testGetStringFontIcon_returnsDefaultValue() {
        String identifier = HyperwalletTransferMethod.TransferMethodTypes.BANK_ACCOUNT.toLowerCase(Locale.ROOT)
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
}
