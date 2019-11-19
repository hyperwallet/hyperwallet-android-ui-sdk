package com.hyperwallet.android.ui.transfermethod.insight;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.withChild;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import static java.net.HttpURLConnection.HTTP_OK;

import android.content.Context;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.rule.ActivityTestRule;

import com.hyperwallet.android.ui.R;
import com.hyperwallet.android.ui.common.insight.HyperwalletInsight;
import com.hyperwallet.android.ui.common.repository.EspressoIdlingResource;
import com.hyperwallet.android.ui.common.util.PageGroups;
import com.hyperwallet.android.ui.testutils.rule.HyperwalletExternalResourceManager;
import com.hyperwallet.android.ui.testutils.rule.HyperwalletMockWebServer;
import com.hyperwallet.android.ui.testutils.rule.HyperwalletSdkRule;
import com.hyperwallet.android.ui.transfermethod.repository.TransferMethodRepositoryFactory;
import com.hyperwallet.android.ui.transfermethod.rule.HyperwalletInsightMockRule;
import com.hyperwallet.android.ui.transfermethod.view.SelectTransferMethodActivity;
import com.hyperwallet.android.ui.user.repository.UserRepositoryFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.Map;

public class SelectTransferMethodInsightTest {
    @ClassRule
    public static HyperwalletExternalResourceManager sResourceManager = new HyperwalletExternalResourceManager();

    @Rule
    public HyperwalletSdkRule mHyperwalletSdkRule = new HyperwalletSdkRule();
    @Rule
    public HyperwalletInsightMockRule mHyperwalletInsightMockRule = new HyperwalletInsightMockRule();
    @Rule
    public HyperwalletMockWebServer mMockWebServer = new HyperwalletMockWebServer(8080);
    @Rule
    public ActivityTestRule<SelectTransferMethodActivity> mActivityTestRule =
            new ActivityTestRule<>(SelectTransferMethodActivity.class, true, false);
    @Rule
    public MockitoRule mMockito = MockitoJUnit.rule();
    @Captor
    private ArgumentCaptor<Map<String, String>> mParamsCaptor;

    @Before
    public void setup() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("authentication_token_response.json")).mock();
        IdlingRegistry.getInstance().register(EspressoIdlingResource.getIdlingResource());
    }

    @After
    public void cleanup() {
        TransferMethodRepositoryFactory.clearInstance();
        UserRepositoryFactory.clearInstance();
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.getIdlingResource());
    }

    @Test
    public void testSelectTransferMethod_verifyInsightEventCreatedOnTransferMethodOptionsLoad() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("user_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("successful_tmc_keys_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        verify(mHyperwalletInsightMockRule.getInsight(),
                timeout(5000).times(1)).trackImpression(any(Context.class),
                eq(SelectTransferMethodActivity.TAG),
                eq(PageGroups.TRANSFER_METHOD),
                mParamsCaptor.capture());

        assertThat(mParamsCaptor.getValue().get("hyperwallet_ea_country"), is("US"));
        assertThat(mParamsCaptor.getValue().get("hyperwallet_ea_currency"), is("USD"));
    }

    @Test
    public void testSelectTransferMethod_verifyInsightEventCreatedOnCountrySelection() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("user_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("successful_tmc_keys_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        verify(mHyperwalletInsightMockRule.getInsight(),
                times(1)).trackImpression(any(Context.class),
                eq(SelectTransferMethodActivity.TAG),
                eq(PageGroups.TRANSFER_METHOD),
                mParamsCaptor.capture());

        assertThat(mParamsCaptor.getValue().get("hyperwallet_ea_country"), is("US"));
        assertThat(mParamsCaptor.getValue().get("hyperwallet_ea_currency"), is("USD"));

        onView(withId(R.id.select_transfer_method_country_value)).perform(click());
        onView(allOf(withId(R.id.country_name), withText("Canada"))).perform(click());

        verify(mHyperwalletInsightMockRule.getInsight(),
                timeout(5000).times(1)).trackClick(any(Context.class),
                eq(SelectTransferMethodActivity.TAG),
                eq(PageGroups.TRANSFER_METHOD),
                eq(HyperwalletInsight.LINK_SELECT_TRANSFER_METHOD_COUNTRY),
                mParamsCaptor.capture());

        assertThat(mParamsCaptor.getValue().get("hyperwallet_ea_country"), is("CA"));

        verify(mHyperwalletInsightMockRule.getInsight(),
                timeout(5000).times(2)).trackImpression(any(Context.class),
                eq(SelectTransferMethodActivity.TAG),
                eq(PageGroups.TRANSFER_METHOD),
                mParamsCaptor.capture());

        assertThat(mParamsCaptor.getValue().get("hyperwallet_ea_country"), is("CA"));
        assertThat(mParamsCaptor.getValue().get("hyperwallet_ea_currency"), is("CAD"));
    }

    @Test
    public void testSelectTransferMethod_verifyInsightEventCreatedOnCurrencySelection() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("user_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("successful_tmc_keys_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        verify(mHyperwalletInsightMockRule.getInsight(),
                timeout(5000).times(1)).trackImpression(any(Context.class),
                eq(SelectTransferMethodActivity.TAG),
                eq(PageGroups.TRANSFER_METHOD),
                mParamsCaptor.capture());

        assertThat(mParamsCaptor.getValue().get("hyperwallet_ea_country"), is("US"));
        assertThat(mParamsCaptor.getValue().get("hyperwallet_ea_currency"), is("USD"));

        onView(withId(R.id.select_transfer_method_currency_value)).perform(click());
        onView(allOf(withId(R.id.currency_name), withText("United States Dollar"))).perform(click());

        verify(mHyperwalletInsightMockRule.getInsight(),
                timeout(5000).times(1)).trackClick(any(Context.class),
                eq(SelectTransferMethodActivity.TAG),
                eq(PageGroups.TRANSFER_METHOD),
                eq(HyperwalletInsight.LINK_SELECT_TRANSFER_METHOD_CURRENCY),
                mParamsCaptor.capture());

        assertThat(mParamsCaptor.getValue().get("hyperwallet_ea_currency"), is("USD"));

        verify(mHyperwalletInsightMockRule.getInsight(),
                timeout(5000).times(2)).trackImpression(any(Context.class),
                eq(SelectTransferMethodActivity.TAG),
                eq(PageGroups.TRANSFER_METHOD),
                mParamsCaptor.capture());

        assertThat(mParamsCaptor.getValue().get("hyperwallet_ea_country"), is("US"));
        assertThat(mParamsCaptor.getValue().get("hyperwallet_ea_currency"), is("USD"));
    }

    @Test
    public void testSelectTransferMethod_verifyInsightEventCreatedOnTransferMethodSelected() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("user_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("successful_tmc_keys_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("successful_tmc_fields_bank_account_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.select_transfer_method_types_list))
                .perform(RecyclerViewActions.actionOnItem(withChild(withText(R.string.bank_account)), click()));

        verify(mHyperwalletInsightMockRule.getInsight(),
                timeout(5000).times(1)).trackClick(any(Context.class),
                eq(SelectTransferMethodActivity.TAG),
                eq(PageGroups.TRANSFER_METHOD),
                eq(HyperwalletInsight.LINK_SELECT_TRANSFER_METHOD_SELECT),
                mParamsCaptor.capture());

        assertThat(mParamsCaptor.getValue().get("hyperwallet_ea_country"), is("US"));
        assertThat(mParamsCaptor.getValue().get("hyperwallet_ea_currency"), is("USD"));
        assertThat(mParamsCaptor.getValue().get("hyperwallet_ea_type"), is("BANK_ACCOUNT"));
    }

    @Test
    public void testSelectTransferMethod_verifyInsightEventCreatedOnTransferMethodSelectedPaypal() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("user_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("successful_tmc_keys_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("successful_tmc_fields_paypal_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.select_transfer_method_types_list))
                .perform(RecyclerViewActions.actionOnItem(withChild(withText(R.string.paypal_account)), click()));

        verify(mHyperwalletInsightMockRule.getInsight(),
                timeout(5000).times(1)).trackClick(any(Context.class),
                eq(SelectTransferMethodActivity.TAG),
                eq(PageGroups.TRANSFER_METHOD),
                eq(HyperwalletInsight.LINK_SELECT_TRANSFER_METHOD_SELECT),
                mParamsCaptor.capture());

        assertThat(mParamsCaptor.getValue().get("hyperwallet_ea_country"), is("US"));
        assertThat(mParamsCaptor.getValue().get("hyperwallet_ea_currency"), is("USD"));
        assertThat(mParamsCaptor.getValue().get("hyperwallet_ea_type"), is("PAYPAL_ACCOUNT"));
    }
}
