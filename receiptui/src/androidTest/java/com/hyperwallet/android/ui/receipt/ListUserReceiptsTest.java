package com.hyperwallet.android.ui.receipt;

import static android.text.format.DateUtils.FORMAT_ABBREV_WEEKDAY;
import static android.text.format.DateUtils.FORMAT_SHOW_DATE;
import static android.text.format.DateUtils.FORMAT_SHOW_TIME;
import static android.text.format.DateUtils.FORMAT_SHOW_WEEKDAY;
import static android.text.format.DateUtils.FORMAT_SHOW_YEAR;
import static android.text.format.DateUtils.formatDateTime;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;

import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.net.HttpURLConnection.HTTP_OK;

import static com.hyperwallet.android.ui.testutils.util.EspressoUtils.atPosition;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.ui.common.repository.EspressoIdlingResource;
import com.hyperwallet.android.ui.common.util.DateUtils;
import com.hyperwallet.android.ui.receipt.view.ListUserReceiptActivity;
import com.hyperwallet.android.ui.testutils.TestAuthenticationProvider;
import com.hyperwallet.android.ui.testutils.rule.HyperwalletExternalResourceManager;
import com.hyperwallet.android.ui.testutils.rule.HyperwalletMockWebServer;
import com.hyperwallet.android.ui.testutils.util.RecyclerViewCountAssertion;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.mockwebserver.MockResponse;

@RunWith(AndroidJUnit4.class)
public class ListUserReceiptsTest {

    @ClassRule
    public static HyperwalletExternalResourceManager sResourceManager = new HyperwalletExternalResourceManager();
    @Rule
    public HyperwalletMockWebServer mMockWebServer = new HyperwalletMockWebServer(8080);
    @Rule
    public ActivityTestRule<ListUserReceiptActivity> mActivityTestRule =
            new ActivityTestRule<>(ListUserReceiptActivity.class, true, false);

    @Before
    public void setup() {
        Hyperwallet.getInstance(new TestAuthenticationProvider());

        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("authentication_token_response.json")).mock();

        setLocale(Locale.US);
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
    public void testListReceipt_userHasMultipleTransactions() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("receipt_list_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_NO_CONTENT).withBody("").mock();

        // run test
        mActivityTestRule.launchActivity(null);

        // assert
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText(R.string.title_activity_receipt_list)));
        onView(withId(R.id.list_receipts)).check(matches(isDisplayed()));

        onView(withId(R.id.list_receipts))
                .check(matches(atPosition(0, hasDescendant(withText("June 2019")))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(0,
                hasDescendant(withText(com.hyperwallet.android.ui.receipt.R.string.credit)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.payment)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0, hasDescendant(withText("+ 20.00")))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0, hasDescendant(withText("June 7, 2019")))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(0, hasDescendant(withText("USD")))));

        onView(withId(R.id.list_receipts)).check(matches(atPosition(1,
                hasDescendant(withText(com.hyperwallet.android.ui.receipt.R.string.credit)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(1, hasDescendant(withText(R.string.payment)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(1, hasDescendant(withText("+ 25.00")))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(1, hasDescendant(withText("June 2, 2019")))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(1, hasDescendant(withText("CAD")))));

        onView(withId(R.id.list_receipts)).check(matches(atPosition(2,
                hasDescendant(withText(com.hyperwallet.android.ui.receipt.R.string.debit)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(2, hasDescendant(withText(R.string.card_activation_fee)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(2, hasDescendant(withText("- 1.95")))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(2, hasDescendant(withText("June 1, 2019")))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(2, hasDescendant(withText("USD")))));

        onView(withId(R.id.list_receipts))
                .check(matches(atPosition(3, hasDescendant(withText("December 2018")))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(3,
                hasDescendant(withText(com.hyperwallet.android.ui.receipt.R.string.debit)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(3, hasDescendant(withText(R.string.transfer_to_prepaid_card)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(3, hasDescendant(withText("- 18.05")))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(3, hasDescendant(withText("December 1, 2018")))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(3, hasDescendant(withText("USD")))));

        onView(withId(R.id.list_receipts)).check(new RecyclerViewCountAssertion(4));
    }

    @Test
    public void testListReceipt_userHasCreditTransaction() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("receipt_credit_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_NO_CONTENT).withBody("").mock();

        // run test
        mActivityTestRule.launchActivity(null);

        // assert
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText(R.string.title_activity_receipt_list)));
        onView(withId(R.id.list_receipts)).check(matches(isDisplayed()));

        onView(withId(R.id.list_receipts))
                .check(matches(atPosition(0, hasDescendant(withText("June 2019")))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(0,
                hasDescendant(withText(com.hyperwallet.android.ui.receipt.R.string.credit)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.payment)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0, hasDescendant(withText("+ 25.00")))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0, hasDescendant(withText("June 2, 2019")))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(0, hasDescendant(withText("CAD")))));

        onView(withId(R.id.list_receipts)).check(new RecyclerViewCountAssertion(1));
    }

    @Test
    public void testListReceipt_userHasDebitTransaction() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("receipt_debit_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_NO_CONTENT).withBody("").mock();

        // run test
        mActivityTestRule.launchActivity(null);

        // assert
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText(R.string.title_activity_receipt_list)));
        onView(withId(R.id.list_receipts)).check(matches(isDisplayed()));

        onView(withId(R.id.list_receipts))
                .check(matches(atPosition(0, hasDescendant(withText("May 2019")))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(0,
                hasDescendant(withText(com.hyperwallet.android.ui.receipt.R.string.debit)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.transfer_to_prepaid_card)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0, hasDescendant(withText("- 18.05")))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0, hasDescendant(withText("May 2, 2019")))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(0, hasDescendant(withText("USD")))));

        onView(withId(R.id.list_receipts)).check(new RecyclerViewCountAssertion(1));
    }

    @Test
    public void testListReceipt_userHasUnknownTransactionType() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("receipt_unknown_type_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_NO_CONTENT).withBody("").mock();

        // run test
        mActivityTestRule.launchActivity(null);

        // assert
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText(R.string.title_activity_receipt_list)));
        onView(withId(R.id.list_receipts)).check(matches(isDisplayed()));

        onView(withId(R.id.list_receipts))
                .check(matches(atPosition(0, hasDescendant(withText("June 2019")))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(0,
                hasDescendant(withText(com.hyperwallet.android.ui.receipt.R.string.credit)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.unknown_type)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0, hasDescendant(withText("+ 25.00")))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0, hasDescendant(withText("June 2, 2019")))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(0, hasDescendant(withText("CAD")))));

        onView(withId(R.id.list_receipts)).check(new RecyclerViewCountAssertion(1));
    }

    @Test
    public void testListReceipt_userHasNoTransactions() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_NO_CONTENT).withBody("").mock();

        // run test
        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.toolbar)).check(matches(isDisplayed()));
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText(R.string.title_activity_receipt_list)));
        //todo: check empty view when it will be ready
    }

    @Test
    public void testListReceipt_clickTransactionDisplaysDetails() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("receipt_list_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_NO_CONTENT).withBody("").mock();

        // run test
        mActivityTestRule.launchActivity(null);

        // assert
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText(R.string.title_activity_receipt_list)));
        onView(withId(R.id.list_receipts)).check(matches(isDisplayed()));

        onView(withId(R.id.list_receipts)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        onView(withId(R.id.transaction_header_text)).check(matches(withText(R.string.transaction_header_text)));
        onView(withId(R.id.transaction_type_icon)).check(matches(withText(R.string.credit)));
        onView(withId(R.id.transaction_title)).check(matches(withText(R.string.payment)));
        onView(withId(R.id.transaction_amount)).check(matches(withText("+ 20.00")));
        onView(withId(R.id.transaction_currency)).check(matches(withText("USD")));
        onView(withId(R.id.transaction_date)).check(matches(withText("June 7, 2019")));

        onView(withId(R.id.receipt_details_header_label)).check(matches(withText(R.string.receipt_header_label)));
        onView(withId(R.id.receipt_id_label)).check(matches(withText(R.string.journalId)));
        onView(withId(R.id.receipt_id_value)).check(matches(withText("3051579")));
        onView(withId(R.id.date_label)).check(matches(withText(R.string.createdOn)));

        Date date = DateUtils.fromDateTimeString("2019-06-07T17:08:58");
        String timezone = DateUtils.toDateFormat(date, "zzz");
        String text = mActivityTestRule.getActivity().getApplicationContext().getString(
                R.string.concat_string_view_format,
                formatDateTime(mActivityTestRule.getActivity().getApplicationContext(), date.getTime(),
                        FORMAT_SHOW_DATE | FORMAT_SHOW_TIME | FORMAT_SHOW_YEAR
                                | FORMAT_SHOW_WEEKDAY | FORMAT_ABBREV_WEEKDAY), timezone);
        onView(withId(R.id.date_value)).check(matches(withText(text)));

        onView(withId(R.id.client_id_label)).check(matches(withText(R.string.clientPaymentId)));
        onView(withId(R.id.client_id_value)).check(matches(withText("8OxXefx5")));
        onView(withId(R.id.charity_label)).check(matches(withText(R.string.charityName)));
        onView(withId(R.id.charity_value)).check(matches(withText("Sample Charity")));
        onView(withId(R.id.check_number_label)).check(matches(withText(R.string.checkNumber)));
        onView(withId(R.id.check_number_value)).check(matches(withText("Sample Check Number")));
        onView(withId(R.id.website_label)).check(matches(withText(R.string.website)));
        onView(withId(R.id.website_value)).check(matches(withText("https://api.sandbox.hyperwallet.com")));
        onView(withText("A Person")).check(doesNotExist());

        onView(withId(R.id.receipt_notes_header_label)).check(matches(withText(R.string.notes)));
        onView(withId(R.id.notes_value)).check(
                matches(withText("Sample payment for the period of June 15th, 2019 to July 23, 2019")));

        onView(withId(R.id.details_header_text)).check(matches(withText(R.string.fee_details_header_text)));
        onView(withId(R.id.details_amount_label)).check(matches(withText(R.string.details_amount_label)));
        onView(withId(R.id.details_amount_value)).check(matches(withText("20.00 USD")));
        onView(withId(R.id.details_fee_label)).check(matches(withText(R.string.fee_label)));
        onView(withId(R.id.details_fee_value)).check(matches(withText("2.25 USD")));
        onView(withId(R.id.details_transfer_amount_label)).check(matches(withText(R.string.transfer_amount_label)));
        onView(withId(R.id.details_transfer_amount_value)).check(matches(withText("17.75 USD")));
    }

    @Test
    public void testListReceipt_clickTransactionDisplaysDetailsWithoutFees() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("receipt_list_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_NO_CONTENT).withBody("").mock();

        // run test
        mActivityTestRule.launchActivity(null);

        // assert
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText(R.string.title_activity_receipt_list)));
        onView(withId(R.id.list_receipts)).check(matches(isDisplayed()));

        onView(withId(R.id.list_receipts)).perform(RecyclerViewActions.actionOnItemAtPosition(3, click()));

        onView(withId(R.id.transaction_header_text)).check(matches(withText(R.string.transaction_header_text)));
        onView(withId(R.id.transaction_type_icon)).check(matches(withText(R.string.debit)));
        onView(withId(R.id.transaction_title)).check(matches(withText(R.string.transfer_to_prepaid_card)));
        onView(withId(R.id.transaction_amount)).check(matches(withText("- 18.05")));
        onView(withId(R.id.transaction_currency)).check(matches(withText("USD")));
        onView(withId(R.id.transaction_date)).check(matches(withText("December 1, 2018")));

        onView(withId(R.id.receipt_details_header_label)).check(matches(withText(R.string.receipt_header_label)));
        onView(withId(R.id.receipt_id_label)).check(matches(withText(R.string.journalId)));
        onView(withId(R.id.receipt_id_value)).check(matches(withText("3051590")));
        onView(withId(R.id.date_label)).check(matches(withText(R.string.createdOn)));

        Date date = DateUtils.fromDateTimeString("2018-12-01T17:12:18");
        String timezone = DateUtils.toDateFormat(date, "zzz");
        String text = mActivityTestRule.getActivity().getApplicationContext().getString(
                R.string.concat_string_view_format,
                formatDateTime(mActivityTestRule.getActivity().getApplicationContext(), date.getTime(),
                        FORMAT_SHOW_DATE | FORMAT_SHOW_TIME | FORMAT_SHOW_YEAR
                                | FORMAT_SHOW_WEEKDAY | FORMAT_ABBREV_WEEKDAY), timezone);
        onView(withId(R.id.date_value)).check(matches(withText(text)));

        onView(withId(R.id.client_id_label)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(R.id.client_id_value)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(R.id.charity_label)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(R.id.charity_value)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(R.id.check_number_label)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(R.id.check_number_value)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(R.id.website_label)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(R.id.website_value)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));

        onView(withId(R.id.receipt_notes_information)).check(
                matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(R.id.receipt_notes_header_label)).check(
                matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(R.id.notes_value)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }

    @Test
    public void testListReceipt_verifyTransactionsLoadedUponScrolling() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("receipt_list_paged_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("receipt_list_paged_second_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("receipt_list_paged_third_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("receipt_list_paged_last_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_NO_CONTENT).withBody("").mock();

        // run test
        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.toolbar)).check(matches(isDisplayed()));
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText(R.string.title_activity_receipt_list)));

        onView(withId(R.id.list_receipts)).check(new RecyclerViewCountAssertion(20));
        onView(withId(R.id.list_receipts)).perform(RecyclerViewActions.scrollToPosition(10));
        onView(withId(R.id.list_receipts)).check(new RecyclerViewCountAssertion(30));
        onView(withId(R.id.list_receipts)).perform(RecyclerViewActions.scrollToPosition(20));
        onView(withId(R.id.list_receipts)).check(new RecyclerViewCountAssertion(40));
        onView(withId(R.id.list_receipts)).perform(RecyclerViewActions.scrollToPosition(30));
        onView(withId(R.id.list_receipts)).check(new RecyclerViewCountAssertion(50));

        // verify that when the list reaches the end no additional data is loaded
        onView(withId(R.id.list_receipts)).perform(RecyclerViewActions.scrollToPosition(40));
        onView(withId(R.id.list_receipts)).check(new RecyclerViewCountAssertion(50));
        onView(withId(R.id.list_receipts)).perform(RecyclerViewActions.scrollToPosition(50));
        onView(withId(R.id.list_receipts)).check(new RecyclerViewCountAssertion(50));
    }

    @Test
    public void testListReceipt_checkDateTextOnLocaleChange() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("receipt_debit_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_NO_CONTENT).withBody("").mock();

        setLocale(Locale.ITALY);
        // run test
        mActivityTestRule.launchActivity(null);
        // assert
        onView(withId(R.id.list_receipts))
                .check(matches(atPosition(0, hasDescendant(withText("maggio 2019")))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(0, hasDescendant(withText("2 maggio 2019")))));
    }

    @Test
    public void testListReceipt_displaysNetworkErrorDialogOnConnectionTimeout() {
        mMockWebServer.getServer().enqueue(new MockResponse().setResponseCode(HTTP_OK).setBody(sResourceManager
                .getResourceContent("receipt_debit_response.json")).setBodyDelay(10500, TimeUnit.MILLISECONDS));
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("receipt_debit_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_NO_CONTENT).withBody("").mock();

        mActivityTestRule.launchActivity(null);

        onView(withText(R.string.error_dialog_connectivity_title)).check(matches(isDisplayed()));
        onView(withText(R.string.io_exception)).check(matches(isDisplayed()));
        onView(withId(android.R.id.button1)).check(matches(withText(R.string.try_again_button_label)));
        onView(withId(android.R.id.button2)).check(matches(withText(R.string.cancel_button_label)));

        // retry button clicked
        onView(withId(android.R.id.button1)).perform(click());
        onView(withText(R.string.error_dialog_connectivity_title)).check(doesNotExist());

        // assert
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText(R.string.title_activity_receipt_list)));
        onView(withId(R.id.list_receipts)).check(matches(isDisplayed()));

        onView(withId(R.id.list_receipts))
                .check(matches(atPosition(0, hasDescendant(withText("May 2019")))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(0,
                hasDescendant(withText(com.hyperwallet.android.ui.receipt.R.string.debit)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.transfer_to_prepaid_card)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0, hasDescendant(withText("- 18.05")))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0, hasDescendant(withText("May 2, 2019")))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(0, hasDescendant(withText("USD")))));

        onView(withId(R.id.list_receipts)).check(new RecyclerViewCountAssertion(1));
    }

    private void setLocale(Locale locale) {
        Context context = ApplicationProvider.getApplicationContext();
        Locale.setDefault(locale);
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.JELLY_BEAN) {
            configuration.setLocale(locale);
        } else {
            configuration.locale = locale;
        }
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
    }
}
