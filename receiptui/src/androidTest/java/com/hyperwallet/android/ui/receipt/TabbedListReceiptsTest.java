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
import com.hyperwallet.android.ui.receipt.view.ListPrepaidCardReceiptActivity;
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
    private TimeZone mDefaultTimeZone;

    private String monthLabel1 = "June 2019";
    private String cadCurrencySymbol = "CA$";



    @Before
    public void setup() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("authentication_token_response.json")).mock();

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

    @Test
    public void testPrimaryPrepaidCardActive() {
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
                .check(matches(atPosition(0, hasDescendant(withText(monthLabel1)))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(0,
                hasDescendant(withText(com.hyperwallet.android.ui.receipt.R.string.credit)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.deposit)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0, hasDescendant(withText(cadCurrencySymbol + "15.00")))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0, hasDescendant(withText("June 6, 2019")))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(0, hasDescendant(withText("CAD")))));

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