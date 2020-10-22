package com.hyperwallet.android.ui.receipt;

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

import static java.lang.Thread.sleep;
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
import androidx.test.espresso.Espresso;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import com.hyperwallet.android.ui.common.repository.EspressoIdlingResource;
import com.hyperwallet.android.ui.receipt.view.ListPrepaidCardReceiptActivity;
import com.hyperwallet.android.ui.receipt.view.TabbedListReceiptsActivity;
import com.hyperwallet.android.ui.testutils.rule.HyperwalletExternalResourceManager;
import com.hyperwallet.android.ui.testutils.rule.HyperwalletMockWebServer;
import com.hyperwallet.android.ui.testutils.rule.HyperwalletSdkRule;
import com.hyperwallet.android.ui.testutils.util.RecyclerViewCountAssertion;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import okhttp3.mockwebserver.MockResponse;

@RunWith(AndroidJUnit4.class)
public class TabbedListReceiptsTest {

    private final String MASK = "\u0020\u2022\u2022\u2022\u2022\u0020";
    private final String VISA = "Visa";

    @ClassRule
    public static HyperwalletExternalResourceManager sResourceManager = new HyperwalletExternalResourceManager();
    @Rule
    public HyperwalletSdkRule mHyperwalletSdkRule = new HyperwalletSdkRule();
    @Rule
    public HyperwalletMockWebServer mMockWebServer = new HyperwalletMockWebServer(8080);
    @Rule
    public ActivityTestRule<TabbedListReceiptsActivity> mActivityTestRule =
            new ActivityTestRule<TabbedListReceiptsActivity>(TabbedListReceiptsActivity.class, true, false) {
                @Override
                protected Intent getActivityIntent() {
                    Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                            TabbedListReceiptsActivity.class);
                    // intent.putExtra(EXTRA_PREPAID_CARD_TOKEN, "trm-test-token");
                    return intent;
                }
            };

    private TimeZone mDefaultTimeZone;

    private String monthLabel1 = "June 2019";
    private String cadCurrencySymbol = "CA$";
    private String usdCurrencySymbol = "$";

    @Before
    public void setup() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("authentication_token_walletmodel_response.json")).mock();
        mDefaultTimeZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("US/Pacific"));
        setLocale(Locale.US);
        IdlingRegistry.getInstance().register(EspressoIdlingResource.getIdlingResource());
    }

    @After
    public void cleanup() {
        TimeZone.setDefault(mDefaultTimeZone);
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.getIdlingResource());
    }

    /*Given user has Available funds and Primary PPC and Available funds has no transactions
    When user selects the "Transaction" tab
    Then user can see the tabs for Available funds and Place holder text
    */
    @Test
    public void testListReceiptFragment_verifyAvailbleFundsEmptyReceipt() {

        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_NO_CONTENT).withBody("").mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("prepaidcard/prepaidcard_primarycard_only_response.json")).mock();

        // run test
        mActivityTestRule.launchActivity(null);

        // assert
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText(R.string.title_activity_receipt_list)));

        // Tab navigation need to implement to validate transaction screen
        onView(withText(R.string.mobileNoTransactionsUser)).check(matches(isDisplayed()));
    }

    /*
 Given user has Available funds and Primary PPC and PPC has no transactions
 When user selects the "Transaction" tab
 Then user can see the tabs for PPC and Place holder text
 */
    @Test
    public void testListReceiptFragment_verifyPrimaryPPCEmptyReceipt() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("receipt_list_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("prepaidcard/prepaidcard_primarycard_only_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_NO_CONTENT).withBody("").mock();

        // run test
        mActivityTestRule.launchActivity(null);

        // assert
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText(R.string.title_activity_receipt_list)));
        //onView(withId(R.id.list_receipts)).check(matches(isDisplayed()));

        String ppcTabTitle = VISA + MASK + "9285";
        Espresso.onView(
                allOf(
                        ViewMatchers.withText(ppcTabTitle),
                        ViewMatchers.isDescendantOfA(ViewMatchers.withId(R.id.tab_layout))
                )
        ).perform(
                ViewActions.scrollTo()
        ).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(
                allOf(
                        ViewMatchers.withText(ppcTabTitle),
                        ViewMatchers.isDescendantOfA(ViewMatchers.withId(R.id.tab_layout))
                )
        ).perform(ViewActions.click());

        // Tab navigation need to implement to validate transaction screen
        onView(withText(R.string.mobileNoTransactionsPrepaidCard)).check(matches(isDisplayed()));
    }

    /*
Given user has Available funds and Primary PPC and Available funds has receipts
When user selects the "Transaction" tab
Then user can see the tabs for Available funds and receipts
*/
    @Test
    public void testListReceiptFragment_verifyAvailbleFundsReceipt() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("receipt_list_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("prepaidcard/prepaidcard_primarycard_only_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("prepaidcard/prepaidcard_receipts_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_NO_CONTENT).withBody("").mock();

        // run test
        mActivityTestRule.launchActivity(null);

        // assert
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText(R.string.title_activity_receipt_list)));

        // Assert receipts
        Espresso.onView(
                allOf(
                        ViewMatchers.withText("June 20, 2020"),
                        ViewMatchers.hasSibling(ViewMatchers.withText("USD"))
                )
        ).check(ViewAssertions.matches(ViewMatchers.isCompletelyDisplayed()));

        Espresso.onView(
                allOf(
                        ViewMatchers.withText(R.string.prepaid_card_sale),
                        ViewMatchers.hasSibling(ViewMatchers.withText("-"+ usdCurrencySymbol + "10.00"))
                )
        ).check(ViewAssertions.matches(ViewMatchers.isCompletelyDisplayed()));
    }

    /*
 Given user has Available funds and Primary PPC
 When user selects the "Transaction" tab
 Then user can see the tabs for Primary PPC and receipts
 */
    @Test
    public void testListReceiptFragment_verifyPrimaryPPCReceipt() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("receipt_list_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("prepaidcard/prepaidcard_secondary_response2.json")).mock();

        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("prepaidcard/prepaidcard_secondary_receipts_response.json")).mock();

        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("prepaidcard/prepaidcard_receipts_response.json")).mock();

        // run test
        mActivityTestRule.launchActivity(null);

        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText(R.string.title_activity_receipt_list)));

        String ppcTabTitle = VISA + MASK + "8884";
        Espresso.onView(
                allOf(
                        ViewMatchers.withText(ppcTabTitle),
                        ViewMatchers.isDescendantOfA(ViewMatchers.withId(R.id.tab_layout))
                )
        ).perform(
                ViewActions.scrollTo()
        ).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(
                allOf(
                        ViewMatchers.withText(ppcTabTitle),
                        ViewMatchers.isDescendantOfA(ViewMatchers.withId(R.id.tab_layout))
                )
        ).perform(ViewActions.click());

        // Assert receipts
        Espresso.onView(
                allOf(
                        ViewMatchers.withText("September 20, 2020"),
                        ViewMatchers.hasSibling(ViewMatchers.withText("USD"))
                )
        ).check(ViewAssertions.matches(ViewMatchers.isCompletelyDisplayed()));

        Espresso.onView(
                allOf(
                        ViewMatchers.withText(R.string.adjustment),
                        ViewMatchers.hasSibling(ViewMatchers.withText(usdCurrencySymbol + "6.90"))
                )
        ).check(ViewAssertions.matches(ViewMatchers.isCompletelyDisplayed()));

    }

    /*
     Given user has Available funds and Secondary PPC
     When user selects the "Transaction" tab
     Then user can see the tabs for Secondary PPC
     */
    @Test
    public void testListReceiptFragment_verifySecondaryPPCTabs() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("receipt_list_response.json")).mock();

        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("prepaidcard/prepaidcard_secondary_response2.json")).mock();

        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("prepaidcard/prepaidcard_secondary_receipts_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("prepaidcard/prepaidcard_receipts_response.json")).mock();

        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_NO_CONTENT).withBody("").mock();

        // run test
        mActivityTestRule.launchActivity(null);

        // assert
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText(R.string.title_activity_receipt_list)));

        String secondaryPPCTabTitle = VISA + MASK + "9285";
        Espresso.onView(
                allOf(
                        ViewMatchers.withText(secondaryPPCTabTitle),
                        ViewMatchers.isDescendantOfA(ViewMatchers.withId(R.id.tab_layout))
                )
        ).perform(
                ViewActions.scrollTo()
        ).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(
                allOf(
                        ViewMatchers.withText(secondaryPPCTabTitle),
                        ViewMatchers.isDescendantOfA(ViewMatchers.withId(R.id.tab_layout))
                )
        ).perform(ViewActions.click());

        // assert the empty string
        onView(withText(R.string.mobileNoTransactionsPrepaidCard)).check(matches(isDisplayed()));
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