package com.hyperwallet.android.ui.receipt;

import static android.text.format.DateUtils.FORMAT_ABBREV_WEEKDAY;
import static android.text.format.DateUtils.FORMAT_NO_MONTH_DAY;
import static android.text.format.DateUtils.FORMAT_SHOW_DATE;
import static android.text.format.DateUtils.FORMAT_SHOW_TIME;
import static android.text.format.DateUtils.FORMAT_SHOW_WEEKDAY;
import static android.text.format.DateUtils.FORMAT_SHOW_YEAR;

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

import static com.hyperwallet.android.ui.receipt.view.ListPrepaidCardReceiptActivity.EXTRA_PREPAID_CARD_TOKEN;
import static com.hyperwallet.android.ui.testutils.util.EspressoUtils.atPosition;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import com.hyperwallet.android.ui.common.repository.EspressoIdlingResource;
import com.hyperwallet.android.ui.common.util.DateUtils;
import com.hyperwallet.android.ui.receipt.view.ListPrepaidCardReceiptActivity;
import com.hyperwallet.android.ui.testutils.rule.HyperwalletExternalResourceManager;
import com.hyperwallet.android.ui.testutils.rule.HyperwalletMockWebServer;
import com.hyperwallet.android.ui.testutils.rule.HyperwalletSdkRule;
import com.hyperwallet.android.ui.testutils.util.DateConversion;
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
public class ListPrepaidCardReceiptsTest {

    @ClassRule
    public static HyperwalletExternalResourceManager sResourceManager = new HyperwalletExternalResourceManager();
    @Rule
    public HyperwalletSdkRule mHyperwalletSdkRule = new HyperwalletSdkRule();
    @Rule
    public HyperwalletMockWebServer mMockWebServer = new HyperwalletMockWebServer(8080);
    @Rule
    public ActivityTestRule<ListPrepaidCardReceiptActivity> mActivityTestRule =
            new ActivityTestRule<ListPrepaidCardReceiptActivity>(ListPrepaidCardReceiptActivity.class, true, false) {
                @Override
                protected Intent getActivityIntent() {
                    Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                            ListPrepaidCardReceiptActivity.class);
                    intent.putExtra(EXTRA_PREPAID_CARD_TOKEN, "trm-test-token");
                    return intent;
                }
            };

    @Before
    public void setup() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("authentication_token_response.json")).mock();

        setLocale(Locale.US);
        IdlingRegistry.getInstance().register(EspressoIdlingResource.getIdlingResource());
    }

    @After
    public void cleanup() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.getIdlingResource());
    }

    @Test
    public void testListPrepaidCardReceipt_userHasMultipleTransactions() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("prepaid_card_receipt_list_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_NO_CONTENT).withBody("").mock();

        // run test
        mActivityTestRule.launchActivity(null);

        // assert
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText(R.string.title_activity_receipt_list)));
        onView(withId(R.id.list_receipts)).check(matches(isDisplayed()));

        onView(withId(R.id.list_receipts))
                .check(matches(atPosition(0, hasDescendant(withText(
                        DateConversion.convertDate(mActivityTestRule.getActivity().getApplicationContext(),
                                "2019-06-06T22:48:41",
                                FORMAT_SHOW_DATE | FORMAT_SHOW_YEAR | FORMAT_NO_MONTH_DAY))))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(0,
                hasDescendant(withText(R.string.debit)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.prepaid_card_sale)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0, hasDescendant(withText("- 10.00")))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0,
                        hasDescendant(withText(
                                DateConversion.convertDate(mActivityTestRule.getActivity().getApplicationContext(),
                                        "2019-06-06T22:48:41",
                                        FORMAT_SHOW_DATE | FORMAT_SHOW_YEAR))))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(0, hasDescendant(withText("USD")))));

        onView(withId(R.id.list_receipts)).check(matches(atPosition(1,
                hasDescendant(withText(R.string.credit)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(1, hasDescendant(withText(R.string.deposit)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(1, hasDescendant(withText("+ 5.00")))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(1,
                        hasDescendant(withText(
                                DateConversion.convertDate(mActivityTestRule.getActivity().getApplicationContext(),
                                        "2019-06-06T22:48:51",
                                        FORMAT_SHOW_DATE | FORMAT_SHOW_YEAR))))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(1, hasDescendant(withText("USD")))));

        onView(withId(R.id.list_receipts)).check(matches(atPosition(2,
                hasDescendant(withText(R.string.debit)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(2, hasDescendant(withText(R.string.adjustment)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(2, hasDescendant(withText("- 8.90")))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(2,
                        hasDescendant(withText(
                                DateConversion.convertDate(mActivityTestRule.getActivity().getApplicationContext(),
                                        "2019-06-01T22:49:17",
                                        FORMAT_SHOW_DATE | FORMAT_SHOW_YEAR))))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(2, hasDescendant(withText("USD")))));

        onView(withId(R.id.list_receipts))
                .check(matches(atPosition(3, hasDescendant(withText(
                        DateConversion.convertDate(mActivityTestRule.getActivity().getApplicationContext(),
                                "2019-03-31T23:55:17",
                                FORMAT_SHOW_DATE | FORMAT_SHOW_YEAR | FORMAT_NO_MONTH_DAY))))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(3,
                hasDescendant(withText(R.string.debit)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(3, hasDescendant(withText(R.string.adjustment)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(3, hasDescendant(withText("- 7.90")))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(3,
                        hasDescendant(withText(
                                DateConversion.convertDate(mActivityTestRule.getActivity().getApplicationContext(),
                                        "2019-03-31T23:55:17",
                                        FORMAT_SHOW_DATE | FORMAT_SHOW_YEAR))))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(3, hasDescendant(withText("USD")))));

        onView(withId(R.id.list_receipts)).check(matches(atPosition(4,
                hasDescendant(withText(R.string.credit)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(4, hasDescendant(withText(R.string.adjustment)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(4, hasDescendant(withText("+ 6.90")))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(4,
                        hasDescendant(withText(
                                DateConversion.convertDate(mActivityTestRule.getActivity().getApplicationContext(),
                                        "2019-02-28T23:55:17",
                                        FORMAT_SHOW_DATE | FORMAT_SHOW_YEAR))))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(4, hasDescendant(withText("USD")))));

        onView(withId(R.id.list_receipts)).perform(RecyclerViewActions.scrollToPosition(5));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(5,
                hasDescendant(withText(R.string.credit)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(5, hasDescendant(withText(R.string.adjustment)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(5, hasDescendant(withText("+ 3.90")))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(5,
                        hasDescendant(withText(
                                DateConversion.convertDate(mActivityTestRule.getActivity().getApplicationContext(),
                                        "2019-02-23T23:55:17",
                                        FORMAT_SHOW_DATE | FORMAT_SHOW_YEAR))))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(5, hasDescendant(withText("USD")))));

        onView(withId(R.id.list_receipts)).perform(RecyclerViewActions.scrollToPosition(6));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(6,
                hasDescendant(withText(R.string.credit)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(6, hasDescendant(withText(R.string.adjustment)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(6, hasDescendant(withText("+ 9.92")))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(6,
                        hasDescendant(withText(
                                DateConversion.convertDate(mActivityTestRule.getActivity().getApplicationContext(),
                                        "2019-02-21T23:55:17",
                                        FORMAT_SHOW_DATE | FORMAT_SHOW_YEAR))))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(6, hasDescendant(withText("USD")))));

        onView(withId(R.id.list_receipts)).check(new RecyclerViewCountAssertion(7));
    }

    @Test
    public void testListPrepaidCardReceipt_userHasCreditTransaction() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("prepaid_card_receipt_credit_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_NO_CONTENT).withBody("").mock();

        // run test
        mActivityTestRule.launchActivity(null);

        // assert
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText(R.string.title_activity_receipt_list)));
        onView(withId(R.id.list_receipts)).check(matches(isDisplayed()));
        onView(withId(R.id.list_receipts))
                .check(matches(atPosition(0, hasDescendant(withText(
                        DateConversion.convertDate(mActivityTestRule.getActivity().getApplicationContext(),
                                "2019-06-06T22:48:41", FORMAT_SHOW_DATE | FORMAT_SHOW_YEAR | FORMAT_NO_MONTH_DAY))))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(0,
                hasDescendant(withText(com.hyperwallet.android.ui.receipt.R.string.credit)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.deposit)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0, hasDescendant(withText("+ 15.00")))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0,
                        hasDescendant(withText(
                                DateConversion.convertDate(mActivityTestRule.getActivity().getApplicationContext(),
                                        "2019-06-06T22:48:41",
                                        FORMAT_SHOW_DATE | FORMAT_SHOW_YEAR))))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(0, hasDescendant(withText("CAD")))));

        onView(withId(R.id.list_receipts)).check(new RecyclerViewCountAssertion(1));
    }

    @Test
    public void testListPrepaidCardReceipt_userHasDebitTransaction() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("prepaid_card_receipt_debit_response.json")).mock();
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
                hasDescendant(withText(com.hyperwallet.android.ui.receipt.R.string.debit)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.adjustment)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0, hasDescendant(withText("- 8.90")))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0,
                        hasDescendant(withText(
                                DateConversion.convertDate(mActivityTestRule.getActivity().getApplicationContext(),
                                        "2019-06-01T22:49:17",
                                        FORMAT_SHOW_DATE | FORMAT_SHOW_YEAR))))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(0, hasDescendant(withText("USD")))));

        onView(withId(R.id.list_receipts)).check(new RecyclerViewCountAssertion(1));
    }

    @Test
    public void testListPrepaidCardReceipt_userHasUnknownTransactionType() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("prepaid_card_receipt_unknown_type_response.json")).mock();
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
                matches(atPosition(0, hasDescendant(withText("+ 15.00")))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0,
                        hasDescendant(withText(
                                DateConversion.convertDate(mActivityTestRule.getActivity().getApplicationContext(),
                                        "2019-06-06T22:48:41",
                                        FORMAT_SHOW_DATE | FORMAT_SHOW_YEAR))))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(0, hasDescendant(withText("CAD")))));

        onView(withId(R.id.list_receipts)).check(new RecyclerViewCountAssertion(1));
    }

    @Test
    public void testListPrepaidCardReceipt_userHasNoTransactions() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_NO_CONTENT).withBody("").mock();

        // run test
        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.toolbar)).check(matches(isDisplayed()));
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText(R.string.title_activity_receipt_list)));
        //todo: check empty view when it will be ready
    }

    @Test
    public void testListPrepaidCardReceipt_checkDateTextOnLocaleChange() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("prepaid_card_receipt_debit_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_NO_CONTENT).withBody("").mock();

        setLocale(Locale.GERMAN);
        // run test
        mActivityTestRule.launchActivity(null);
        // assert
        onView(withId(R.id.list_receipts))
                .check(matches(atPosition(0, hasDescendant(withText("Juni 2019")))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(0,
                hasDescendant(withText(com.hyperwallet.android.ui.receipt.R.string.debit)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.adjustment)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0, hasDescendant(withText("- 8,90")))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0,
                        hasDescendant(withText(
                                DateConversion.convertDate(mActivityTestRule.getActivity().getApplicationContext(),
                                        "2019-06-01T22:49:17",
                                        FORMAT_SHOW_DATE | FORMAT_SHOW_YEAR))))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0, hasDescendant(withText("USD")))));

        onView(withId(R.id.list_receipts)).check(new RecyclerViewCountAssertion(1));
    }

    @Test
    public void testListPrepaidCardReceipt_displaysNetworkErrorDialogOnConnectionTimeout() {
        mMockWebServer.getServer().enqueue(new MockResponse().setResponseCode(HTTP_OK).setBody(sResourceManager
                .getResourceContent("prepaid_card_receipt_debit_response.json")).setBodyDelay(10500,
                TimeUnit.MILLISECONDS));
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("prepaid_card_receipt_debit_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_NO_CONTENT).withBody("").mock();

        mActivityTestRule.launchActivity(null);

        // assert error dialog information exist in portrait mode
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
                .check(matches(atPosition(0, hasDescendant(withText("June 2019")))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(0,
                hasDescendant(withText(com.hyperwallet.android.ui.receipt.R.string.debit)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.adjustment)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0, hasDescendant(withText("- 8.90")))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0,
                        hasDescendant(withText(
                                DateConversion.convertDate(mActivityTestRule.getActivity().getApplicationContext(),
                                        "2019-06-01T22:49:17",
                                        FORMAT_SHOW_DATE | FORMAT_SHOW_YEAR))))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(0, hasDescendant(withText("USD")))));

        onView(withId(R.id.list_receipts)).check(new RecyclerViewCountAssertion(1));
    }

    @Test
    public void testListPrepaidCardReceipt_clickTransactionDisplaysDetails() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("prepaid_card_receipt_list_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_NO_CONTENT).withBody("").mock();

        // run test
        mActivityTestRule.launchActivity(null);

        // assert
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText(R.string.title_activity_receipt_list)));
        onView(withId(R.id.list_receipts)).check(matches(isDisplayed()));

        onView(withId(R.id.list_receipts)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        onView(withId(R.id.transaction_header_text)).check(matches(withText(R.string.transaction_header_text)));
        onView(withId(R.id.transaction_type_icon)).check(matches(withText(R.string.debit)));
        onView(withId(R.id.transaction_title)).check(matches(withText(R.string.prepaid_card_sale)));
        onView(withId(R.id.transaction_amount)).check(matches(withText("- 10.00")));
        onView(withId(R.id.transaction_currency)).check(matches(withText("USD")));
        onView(withId(R.id.transaction_date)).check(
                matches(withText(DateConversion.convertDate(mActivityTestRule.getActivity().getApplicationContext(),
                        "2019-06-06T22:48:41",
                        FORMAT_SHOW_DATE | FORMAT_SHOW_YEAR))));

        onView(withId(R.id.receipt_details_header_label)).check(matches(withText(R.string.receipt_header_label)));
        onView(withId(R.id.receipt_id_label)).check(matches(withText(R.string.journalId)));
        onView(withId(R.id.receipt_id_value)).check(matches(withText("FISVL_5240220")));
        onView(withId(R.id.date_label)).check(matches(withText(R.string.createdOn)));

        Date date = DateUtils.fromDateTimeString("2019-06-06T22:48:41");
        String timezone = DateUtils.toDateFormat(date, "zzz");
        String text = mActivityTestRule.getActivity().getApplicationContext().getString(
                R.string.concat_string_view_format,
                DateConversion.convertDate(mActivityTestRule.getActivity().getApplicationContext(), date,
                        FORMAT_SHOW_DATE | FORMAT_SHOW_TIME | FORMAT_SHOW_YEAR
                                | FORMAT_SHOW_WEEKDAY | FORMAT_ABBREV_WEEKDAY), timezone);
        onView(withId(R.id.date_value)).check(matches(withText(text)));

        onView(withId(R.id.client_id_label)).check(matches(withText(R.string.clientPaymentId)));
        onView(withId(R.id.client_id_value)).check(matches(withText("AOxXefx9")));
        onView(withId(R.id.charity_label)).check(matches(withText(R.string.charityName)));
        onView(withId(R.id.charity_value)).check(matches(withText("Sample Charity")));
        onView(withId(R.id.check_number_label)).check(matches(withText(R.string.checkNumber)));
        onView(withId(R.id.check_number_value)).check(matches(withText("Sample Check Number")));
        onView(withId(R.id.website_label)).check(matches(withText(R.string.website)));
        onView(withId(R.id.website_value)).check(matches(withText("https://api.sandbox.hyperwallet.com")));
        onView(withText("A Person")).check(doesNotExist());

        onView(withId(R.id.receipt_notes_header_label)).check(matches(withText(R.string.notes)));
        onView(withId(R.id.notes_value)).check(
                matches(withText("Sample prepaid card payment for the period of June 15th, 2019 to July 23, 2019")));

        onView(withId(R.id.details_header_text)).check(matches(withText(R.string.fee_details_header_text)));
        onView(withId(R.id.details_amount_label)).check(matches(withText(R.string.details_amount_label)));
        onView(withId(R.id.details_amount_value)).check(matches(withText("10.00 USD")));
        onView(withId(R.id.details_fee_label)).check(matches(withText(R.string.fee_label)));
        onView(withId(R.id.details_fee_value)).check(matches(withText("3.00 USD")));
        onView(withId(R.id.details_transfer_amount_label)).check(matches(withText(R.string.transfer_amount_label)));
        onView(withId(R.id.details_transfer_amount_value)).check(matches(withText("7.00 USD")));
    }

    @Test
    public void testListPrepaidCardReceipt_clickTransactionDisplaysDetailsWithoutFees() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("prepaid_card_receipt_list_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_NO_CONTENT).withBody("").mock();

        // run test
        mActivityTestRule.launchActivity(null);

        // assert
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText(R.string.title_activity_receipt_list)));
        onView(withId(R.id.list_receipts)).check(matches(isDisplayed()));

        onView(withId(R.id.list_receipts)).perform(RecyclerViewActions.actionOnItemAtPosition(1, click()));

        onView(withId(R.id.transaction_header_text)).check(matches(withText(R.string.transaction_header_text)));
        onView(withId(R.id.transaction_type_icon)).check(matches(withText(R.string.credit)));
        onView(withId(R.id.transaction_title)).check(matches(withText(R.string.deposit)));
        onView(withId(R.id.transaction_amount)).check(matches(withText("+ 5.00")));
        onView(withId(R.id.transaction_currency)).check(matches(withText("USD")));
        onView(withId(R.id.transaction_date)).check(
                matches(withText(DateConversion.convertDate(mActivityTestRule.getActivity().getApplicationContext(),
                        "2019-06-06T22:48:51",
                        FORMAT_SHOW_DATE | FORMAT_SHOW_YEAR))));

        onView(withId(R.id.receipt_details_header_label)).check(matches(withText(R.string.receipt_header_label)));
        onView(withId(R.id.receipt_id_label)).check(matches(withText(R.string.journalId)));
        onView(withId(R.id.receipt_id_value)).check(matches(withText("FISVL_5240221")));
        onView(withId(R.id.date_label)).check(matches(withText(R.string.createdOn)));

        Date date = DateUtils.fromDateTimeString("2019-06-06T22:48:51");
        String timezone = DateUtils.toDateFormat(date, "zzz");
        String text = mActivityTestRule.getActivity().getApplicationContext().getString(
                R.string.concat_string_view_format,
                DateConversion.convertDate(mActivityTestRule.getActivity().getApplicationContext(), date,
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