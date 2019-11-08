package com.hyperwallet.android.ui.transfermethod;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static junit.framework.TestCase.assertEquals;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_OK;

import static com.hyperwallet.android.ui.common.view.error.DefaultErrorDialogFragment.RESULT_ERROR;
import static com.hyperwallet.android.ui.testutils.util.EspressoUtils.nestedScrollTo;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.insight.InsightEventTag;
import com.hyperwallet.android.ui.R;
import com.hyperwallet.android.ui.common.insight.HyperwalletInsight;
import com.hyperwallet.android.ui.common.repository.EspressoIdlingResource;
import com.hyperwallet.android.ui.common.view.error.DefaultErrorDialogFragment;
import com.hyperwallet.android.ui.testutils.TestAuthenticationProvider;
import com.hyperwallet.android.ui.testutils.rule.HyperwalletExternalResourceManager;
import com.hyperwallet.android.ui.testutils.rule.HyperwalletMockWebServer;
import com.hyperwallet.android.ui.transfermethod.repository.TransferMethodRepositoryFactory;
import com.hyperwallet.android.ui.transfermethod.view.AddTransferMethodActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.IOException;
import java.util.Map;

@RunWith(AndroidJUnit4.class)
public class AddTransferMethodTest {

    private static final String ACCOUNT_NUMBER = "8017110254";
    private static final String ROUTING_NUMBER = "211179539";
    private static final String TRANSFER_METHOD_BANK_TYPE = "BANK_ACCOUNT";
    private static final String TRANSFER_METHOD_WIRE_TYPE = "WIRE_ACCOUNT";
    private static final String TRANSFER_METHOD_PAYPAL_TYPE = "PAYPAL_ACCOUNT";
    private static final String TRANSFER_METHOD_CARD_TYPE = "BANK_CARD";
    private static final String TRANSFER_METHOD_COUNTRY = "US";
    private static final String TRANSFER_METHOD_CURRENCY = "USD";
    private static final String TRANSFER_METHOD_PROFILE_TYPE = "INDIVIDUAL";

    @ClassRule
    public static HyperwalletExternalResourceManager sResourceManager = new HyperwalletExternalResourceManager();
    @Rule
    public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule
    public HyperwalletMockWebServer mMockWebServer = new HyperwalletMockWebServer(8080);
    @Rule
    public ActivityTestRule<AddTransferMethodActivity> mActivityTestRule =
            new ActivityTestRule<AddTransferMethodActivity>(AddTransferMethodActivity.class, true, false);

    @Captor
    ArgumentCaptor<String> pageNameCaptor;
    @Captor
    ArgumentCaptor<String> pageGroupCaptor;
    @Captor
    ArgumentCaptor<Map<String, String>> mapImpressionCaptor;

    @Mock
    private HyperwalletInsight mHyperwalletInsight;

    @Before
    public void setup() {
        Hyperwallet.getInstance(new TestAuthenticationProvider());
        HyperwalletInsight.setInstance(mHyperwalletInsight);

        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("authentication_token_response.json")).mock();

    }

    @After
    public void cleanup() {
        TransferMethodRepositoryFactory.clearInstance();
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
    public void testAddTransferMethod_accountDetailsHiddenOnEmptyFeeAndProcessingResponse() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("successful_tmc_fields_empty_details_response.json")).mock();

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                AddTransferMethodActivity.class);
        intent.putExtra("TRANSFER_METHOD_TYPE", TRANSFER_METHOD_BANK_TYPE);
        intent.putExtra("TRANSFER_METHOD_COUNTRY", TRANSFER_METHOD_COUNTRY);
        intent.putExtra("TRANSFER_METHOD_CURRENCY", TRANSFER_METHOD_CURRENCY);
        intent.putExtra("TRANSFER_METHOD_PROFILE_TYPE", TRANSFER_METHOD_PROFILE_TYPE);

        mActivityTestRule.launchActivity(intent);

        onView(withId(R.id.add_transfer_method_static_container)).check(
                matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(R.id.add_transfer_method_fee_label)).check(
                matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(R.id.add_transfer_method_processing_label)).check(
                matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(R.id.add_transfer_method_fee_value)).check(
                matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(R.id.add_transfer_method_processing_time_value)).check(
                matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }

    @Test
    public void testAddTransferMethod_displaysErrorDialogOnDuplicateAccountFailure() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("successful_tmc_fields_bank_account_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_BAD_REQUEST).withBody(sResourceManager
                .getResourceContent("bank_account_duplicate_routing_response.json")).mock();

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                AddTransferMethodActivity.class);
        intent.putExtra("TRANSFER_METHOD_TYPE", TRANSFER_METHOD_BANK_TYPE);
        intent.putExtra("TRANSFER_METHOD_COUNTRY", TRANSFER_METHOD_COUNTRY);
        intent.putExtra("TRANSFER_METHOD_CURRENCY", TRANSFER_METHOD_CURRENCY);
        intent.putExtra("TRANSFER_METHOD_PROFILE_TYPE", TRANSFER_METHOD_PROFILE_TYPE);

        mActivityTestRule.launchActivity(intent);

        onView(withId(R.id.branchId)).perform(nestedScrollTo(), replaceText(ROUTING_NUMBER));
        onView(withId(R.id.bankAccountId)).perform(nestedScrollTo(), replaceText(ACCOUNT_NUMBER));
        onView(withId(R.id.bankAccountPurpose)).perform(nestedScrollTo(), click());
        onView(allOf(withId(R.id.select_name), withText("Savings"))).perform(click());
        onView(withId(R.id.add_transfer_method_button)).perform(nestedScrollTo(), click());

        // check dialog content
        onView(withText(R.string.error_dialog_title)).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withText(containsString(
                "The account information you provided is already registered. Based on the external account "
                        + "configuration duplications are not allowed.")))
                .inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withId(android.R.id.button1)).check(matches(withText(R.string.close_button_label)));
        onView(withId(android.R.id.button1)).perform(click());

        // should display the add tm form
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar)))).check(
                matches(withText(R.string.title_add_bank_account)));

        // connectivity dialog should be dismissed and does not exist in ui
        onView(withText(R.string.error_dialog_title)).check(doesNotExist());
    }

    @Test
    public void testAddTransferMethod_displaysUnexpectedErrorDialogOnException() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("successful_tmc_fields_bank_account_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_BAD_REQUEST).withBody(sResourceManager
                .getResourceContent("invalid_json_response.json")).mock();

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                AddTransferMethodActivity.class);
        intent.putExtra("TRANSFER_METHOD_TYPE", TRANSFER_METHOD_BANK_TYPE);
        intent.putExtra("TRANSFER_METHOD_COUNTRY", TRANSFER_METHOD_COUNTRY);
        intent.putExtra("TRANSFER_METHOD_CURRENCY", TRANSFER_METHOD_CURRENCY);
        intent.putExtra("TRANSFER_METHOD_PROFILE_TYPE", TRANSFER_METHOD_PROFILE_TYPE);

        mActivityTestRule.launchActivity(intent);

        verify(mHyperwalletInsight, atLeastOnce()).trackImpression(any(Context.class), pageNameCaptor.capture(),
                pageGroupCaptor.capture(),
                mapImpressionCaptor.capture());

        assertEquals("transfer-method:add:collect-transfer-method-information", pageNameCaptor.getValue());
        assertEquals("transfer-method", pageGroupCaptor.getValue());
        assertEquals(4, mapImpressionCaptor.getValue().size());
        assertEquals("US",
                mapImpressionCaptor.getValue().get(InsightEventTag.InsightEventTagEventParams.TRANSFER_METHOD_COUNTRY));
        assertEquals("USD", mapImpressionCaptor.getValue().get(
                InsightEventTag.InsightEventTagEventParams.TRANSFER_METHOD_CURRENCY));
        assertEquals("BANK_ACCOUNT",
                mapImpressionCaptor.getValue().get(InsightEventTag.InsightEventTagEventParams.TRANSFER_METHOD_TYPE));
        assertEquals("INDIVIDUAL", mapImpressionCaptor.getValue().get(
                InsightEventTag.InsightEventTagEventParams.TRANSFER_METHOD_PROFILE_TYPE));

        onView(withId(R.id.branchId)).perform(nestedScrollTo(), replaceText(ROUTING_NUMBER));
        onView(withId(R.id.bankAccountId)).perform(nestedScrollTo(), replaceText(ACCOUNT_NUMBER));
        onView(withId(R.id.bankAccountPurpose)).perform(nestedScrollTo(), click());
        onView(allOf(withId(R.id.select_name), withText("Savings"))).perform(click());

        onView(withId(R.id.add_transfer_method_button)).perform(nestedScrollTo(), click());

        // check dialog content
        onView(withText(R.string.error_dialog_unexpected_title)).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withText(R.string.unexpected_exception)).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withId(android.R.id.button1)).check(matches(withText(R.string.close_button_label)));
        onView(withId(android.R.id.button1)).perform(click());

        // verify activity is finished
        assertThat("Result code is incorrect",
                mActivityTestRule.getActivityResult().getResultCode(), is(RESULT_ERROR));
    }

    @Test
    public void testAddTransferMethod_displaysNetworkErrorDialogOnConnectionTimeout() throws IOException {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("successful_tmc_fields_bank_account_response.json")).mock();

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                AddTransferMethodActivity.class);
        intent.putExtra("TRANSFER_METHOD_TYPE", TRANSFER_METHOD_BANK_TYPE);
        intent.putExtra("TRANSFER_METHOD_COUNTRY", TRANSFER_METHOD_COUNTRY);
        intent.putExtra("TRANSFER_METHOD_CURRENCY", TRANSFER_METHOD_CURRENCY);
        intent.putExtra("TRANSFER_METHOD_PROFILE_TYPE", TRANSFER_METHOD_PROFILE_TYPE);

        mActivityTestRule.launchActivity(intent);

        onView(withId(R.id.branchId)).perform(nestedScrollTo(), replaceText(ROUTING_NUMBER));
        onView(withId(R.id.bankAccountId)).perform(nestedScrollTo(), replaceText(ACCOUNT_NUMBER));
        onView(withId(R.id.bankAccountPurpose)).perform(nestedScrollTo(), click());
        onView(allOf(withId(R.id.select_name), withText("Savings"))).perform(click());

        // initiate test
        mMockWebServer.getServer().shutdown();

        onView(withId(R.id.add_transfer_method_button)).perform(nestedScrollTo(), click());
        // by default screen is in portrait mode

        // assert error dialog information exist in portrait mode
        onView(withText(R.string.error_dialog_connectivity_title)).check(matches(isDisplayed()));
        onView(withText(R.string.io_exception)).check(matches(isDisplayed()));
        onView(withId(android.R.id.button1)).check(matches(withText(R.string.try_again_button_label)));
        onView(withId(android.R.id.button2)).check(matches(withText(R.string.cancel_button_label)));

        // retry button clicked
        onView(withId(android.R.id.button1)).perform(click());

        // should still display connectivity issue
        onView(withText(R.string.error_dialog_connectivity_title)).check(matches(isDisplayed()));
        onView(withText(R.string.io_exception)).check(matches(isDisplayed()));
        onView(withId(android.R.id.button1)).check(matches(withText(R.string.try_again_button_label)));
        onView(withId(android.R.id.button2)).check(matches(withText(R.string.cancel_button_label)));

        // cancel button clicked
        onView(withId(android.R.id.button2)).perform(click());

        Instrumentation.ActivityResult result = mActivityTestRule.getActivityResult();
        assertThat(result.getResultCode(), is(DefaultErrorDialogFragment.RESULT_ERROR));
        assertThat(mActivityTestRule.getActivity().isFinishing(), is(true));
    }

    @Test
    public void testAddTransferMethod_VerifyEventWhenTransferMethodScreenSuccessfullyLoadedForPayPalAccount() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("successful_tmc_fields_paypal_response_for_insight_check.json")).mock();

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                AddTransferMethodActivity.class);
        intent.putExtra("TRANSFER_METHOD_TYPE", TRANSFER_METHOD_PAYPAL_TYPE);
        intent.putExtra("TRANSFER_METHOD_COUNTRY", TRANSFER_METHOD_COUNTRY);
        intent.putExtra("TRANSFER_METHOD_CURRENCY", TRANSFER_METHOD_CURRENCY);
        intent.putExtra("TRANSFER_METHOD_PROFILE_TYPE", TRANSFER_METHOD_PROFILE_TYPE);

        mActivityTestRule.launchActivity(intent);

        verify(mHyperwalletInsight, atLeastOnce()).trackImpression(any(Context.class), pageNameCaptor.capture(),
                pageGroupCaptor.capture(),
                mapImpressionCaptor.capture());

        assertEquals("transfer-method:add:collect-transfer-method-information", pageNameCaptor.getValue());
        assertEquals("transfer-method", pageGroupCaptor.getValue());
        assertEquals(4, mapImpressionCaptor.getValue().size());
        assertEquals("US",
                mapImpressionCaptor.getValue().get(InsightEventTag.InsightEventTagEventParams.TRANSFER_METHOD_COUNTRY));
        assertEquals("USD", mapImpressionCaptor.getValue().get(
                InsightEventTag.InsightEventTagEventParams.TRANSFER_METHOD_CURRENCY));
        assertEquals("PAYPAL_ACCOUNT",
                mapImpressionCaptor.getValue().get(InsightEventTag.InsightEventTagEventParams.TRANSFER_METHOD_TYPE));
        assertEquals("INDIVIDUAL", mapImpressionCaptor.getValue().get(
                InsightEventTag.InsightEventTagEventParams.TRANSFER_METHOD_PROFILE_TYPE));
    }

    @Test
    public void testAddTransferMethod_VerifyEventWhenTransferMethodScreenSuccessfullyLoadedForCardAccount() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("successful_tmc_fields_bank_card_response_for_insight_Check.json")).mock();

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                AddTransferMethodActivity.class);
        intent.putExtra("TRANSFER_METHOD_TYPE", TRANSFER_METHOD_CARD_TYPE);
        intent.putExtra("TRANSFER_METHOD_COUNTRY", TRANSFER_METHOD_COUNTRY);
        intent.putExtra("TRANSFER_METHOD_CURRENCY", TRANSFER_METHOD_CURRENCY);
        intent.putExtra("TRANSFER_METHOD_PROFILE_TYPE", TRANSFER_METHOD_PROFILE_TYPE);

        mActivityTestRule.launchActivity(intent);

        verify(mHyperwalletInsight, atLeastOnce()).trackImpression(any(Context.class), pageNameCaptor.capture(),
                pageGroupCaptor.capture(),
                mapImpressionCaptor.capture());

        assertEquals("transfer-method:add:collect-transfer-method-information", pageNameCaptor.getValue());
        assertEquals("transfer-method", pageGroupCaptor.getValue());
        assertEquals(4, mapImpressionCaptor.getValue().size());
        assertEquals("US",
                mapImpressionCaptor.getValue().get(InsightEventTag.InsightEventTagEventParams.TRANSFER_METHOD_COUNTRY));
        assertEquals("USD", mapImpressionCaptor.getValue().get(
                InsightEventTag.InsightEventTagEventParams.TRANSFER_METHOD_CURRENCY));
        assertEquals("BANK_CARD",
                mapImpressionCaptor.getValue().get(InsightEventTag.InsightEventTagEventParams.TRANSFER_METHOD_TYPE));
        assertEquals("INDIVIDUAL", mapImpressionCaptor.getValue().get(
                InsightEventTag.InsightEventTagEventParams.TRANSFER_METHOD_PROFILE_TYPE));
    }

    @Test
    public void testAddTransferMethod_VerifyEventWhenTransferMethodScreenSuccessfullyLoadedForWireAccount() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("successful_tmc_fields_wireaccount_response_for_insight_check.json")).mock();

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                AddTransferMethodActivity.class);
        intent.putExtra("TRANSFER_METHOD_TYPE", TRANSFER_METHOD_WIRE_TYPE);
        intent.putExtra("TRANSFER_METHOD_COUNTRY", TRANSFER_METHOD_COUNTRY);
        intent.putExtra("TRANSFER_METHOD_CURRENCY", TRANSFER_METHOD_CURRENCY);
        intent.putExtra("TRANSFER_METHOD_PROFILE_TYPE", TRANSFER_METHOD_PROFILE_TYPE);

        mActivityTestRule.launchActivity(intent);

        verify(mHyperwalletInsight, atLeastOnce()).trackImpression(any(Context.class), pageNameCaptor.capture(),
                pageGroupCaptor.capture(),
                mapImpressionCaptor.capture());

        assertEquals("transfer-method:add:collect-transfer-method-information", pageNameCaptor.getValue());
        assertEquals("transfer-method", pageGroupCaptor.getValue());
        assertEquals(4, mapImpressionCaptor.getValue().size());
        assertEquals("US",
                mapImpressionCaptor.getValue().get(InsightEventTag.InsightEventTagEventParams.TRANSFER_METHOD_COUNTRY));
        assertEquals("USD", mapImpressionCaptor.getValue().get(
                InsightEventTag.InsightEventTagEventParams.TRANSFER_METHOD_CURRENCY));
        assertEquals("WIRE_ACCOUNT",
                mapImpressionCaptor.getValue().get(InsightEventTag.InsightEventTagEventParams.TRANSFER_METHOD_TYPE));
        assertEquals("INDIVIDUAL", mapImpressionCaptor.getValue().get(
                InsightEventTag.InsightEventTagEventParams.TRANSFER_METHOD_PROFILE_TYPE));
    }
}
