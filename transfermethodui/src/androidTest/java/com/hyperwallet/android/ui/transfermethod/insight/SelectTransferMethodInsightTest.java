package com.hyperwallet.android.ui.transfermethod.insight;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import static java.net.HttpURLConnection.HTTP_OK;

import android.content.Context;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.rule.ActivityTestRule;

import com.hyperwallet.android.ui.R;
import com.hyperwallet.android.ui.common.insight.HyperwalletInsight;
import com.hyperwallet.android.ui.common.repository.EspressoIdlingResource;
import com.hyperwallet.android.ui.testutils.rule.HyperwalletExternalResourceManager;
import com.hyperwallet.android.ui.testutils.rule.HyperwalletMockWebServer;
import com.hyperwallet.android.ui.testutils.rule.HyperwalletTestRule;
import com.hyperwallet.android.ui.transfermethod.repository.TransferMethodRepositoryFactory;
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
    public HyperwalletTestRule mHyperwalletTestRule = new HyperwalletTestRule();
    @Rule
    public HyperwalletMockWebServer mMockWebServer = new HyperwalletMockWebServer(8080);
    @Rule
    public ActivityTestRule<SelectTransferMethodActivity> mActivityTestRule =
            new ActivityTestRule<>(SelectTransferMethodActivity.class, true, false);
    @Rule
    public MockitoRule mMockito = MockitoJUnit.rule();
    @Captor
    private ArgumentCaptor<Map<String, String>> mParamsCaptor;

    private HyperwalletInsight mHyperwalletInsight;

    @Before
    public void setup() {
        mHyperwalletInsight = mock(HyperwalletInsight.class);
        HyperwalletInsight.setInstance(mHyperwalletInsight);

        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("authentication_token_response.json")).mock();
    }

    @After
    public void cleanup() {
        TransferMethodRepositoryFactory.clearInstance();
        UserRepositoryFactory.clearInstance();
        mHyperwalletInsight = null;
        HyperwalletInsight.setInstance(null);
    }

    @Before
    public void registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.getIdlingResource());
    }

    @After
    public void unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.getIdlingResource());
    }

    @Test
    public void testSelectTransferMethod_verifyInsightEventCreatedOnTransferMethodOptionsLoad() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("user_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("successful_tmc_keys_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        verify(mHyperwalletInsight, times(1)).trackImpression(any(Context.class),
                eq(HyperwalletInsight.PAGE_TRANSFER_METHOD_SELECT),
                eq(HyperwalletInsight.TRANSFER_METHOD_GROUP),
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

        verify(mHyperwalletInsight, times(1)).trackImpression(any(Context.class),
                eq(HyperwalletInsight.PAGE_TRANSFER_METHOD_SELECT),
                eq(HyperwalletInsight.TRANSFER_METHOD_GROUP),
                mParamsCaptor.capture());

        assertThat(mParamsCaptor.getValue().get("hyperwallet_ea_country"), is("US"));
        assertThat(mParamsCaptor.getValue().get("hyperwallet_ea_currency"), is("USD"));

        onView(withId(R.id.select_transfer_method_country_value)).perform(click());
        onView(allOf(withId(R.id.country_name), withText("Canada"))).perform(click());

        verify(mHyperwalletInsight, times(2)).trackImpression(any(Context.class),
                eq(HyperwalletInsight.PAGE_TRANSFER_METHOD_SELECT),
                eq(HyperwalletInsight.TRANSFER_METHOD_GROUP),
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

        verify(mHyperwalletInsight, times(1)).trackImpression(any(Context.class),
                eq(HyperwalletInsight.PAGE_TRANSFER_METHOD_SELECT),
                eq(HyperwalletInsight.TRANSFER_METHOD_GROUP),
                mParamsCaptor.capture());

        assertThat(mParamsCaptor.getValue().get("hyperwallet_ea_country"), is("US"));
        assertThat(mParamsCaptor.getValue().get("hyperwallet_ea_currency"), is("USD"));

        onView(withId(R.id.select_transfer_method_currency_value)).perform(click());
        onView(allOf(withId(R.id.currency_name), withText("United States Dollar"))).perform(click());

        verify(mHyperwalletInsight, times(2)).trackImpression(any(Context.class),
                eq(HyperwalletInsight.PAGE_TRANSFER_METHOD_SELECT),
                eq(HyperwalletInsight.TRANSFER_METHOD_GROUP),
                mParamsCaptor.capture());

        assertThat(mParamsCaptor.getValue().get("hyperwallet_ea_country"), is("US"));
        assertThat(mParamsCaptor.getValue().get("hyperwallet_ea_currency"), is("USD"));
    }
}
