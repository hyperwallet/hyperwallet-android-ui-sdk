package com.hyperwallet.android.ui.transfermethod;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.concurrent.TimeUnit.SECONDS;

import static com.hyperwallet.android.ui.testutils.util.EspressoUtils.hasEmptyText;
import static com.hyperwallet.android.ui.testutils.util.EspressoUtils.hasErrorText;
import static com.hyperwallet.android.ui.testutils.util.EspressoUtils.nestedScrollTo;
import static com.hyperwallet.android.ui.testutils.util.EspressoUtils.withHint;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.TextView;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import com.hyperwallet.android.model.transfermethod.TransferMethod;
import com.hyperwallet.android.ui.R;
import com.hyperwallet.android.ui.common.repository.EspressoIdlingResource;
import com.hyperwallet.android.ui.testutils.rule.HyperwalletExternalResourceManager;
import com.hyperwallet.android.ui.testutils.rule.HyperwalletMockWebServer;
import com.hyperwallet.android.ui.transfermethod.repository.TransferMethodRepositoryFactory;
import com.hyperwallet.android.ui.transfermethod.rule.HyperwalletInsightMockRule;
import com.hyperwallet.android.ui.transfermethod.view.AddTransferMethodActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

@RunWith(AndroidJUnit4.class)
public class PaperCheckTest {

    @ClassRule
    public static HyperwalletExternalResourceManager sResourceManager = new HyperwalletExternalResourceManager();
    @Rule
    public HyperwalletInsightMockRule mHyperwalletInsightMockRule = new HyperwalletInsightMockRule();
    @Rule
    public HyperwalletMockWebServer mMockWebServer = new HyperwalletMockWebServer(8080);
    @Rule
    public ActivityTestRule<AddTransferMethodActivity> mActivityTestRule =
            new ActivityTestRule<AddTransferMethodActivity>(AddTransferMethodActivity.class, true, false) {
                @Override
                protected Intent getActivityIntent() {
                    Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                            AddTransferMethodActivity.class);
                    intent.putExtra("TRANSFER_METHOD_TYPE", "PAPER_CHECK");
                    intent.putExtra("TRANSFER_METHOD_COUNTRY", "US");
                    intent.putExtra("TRANSFER_METHOD_CURRENCY", "USD");
                    intent.putExtra("TRANSFER_METHOD_PROFILE_TYPE", "INDIVIDUAL");
                    return intent;
                }
            };

    @Before
    public void setup() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("authentication_token_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("successful_tmc_fields_paper_check_response.json")).mock();
        IdlingRegistry.getInstance().register(EspressoIdlingResource.getIdlingResource());
    }

    @After
    public void cleanup() {
        TransferMethodRepositoryFactory.clearInstance();
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.getIdlingResource());
    }

    @Test
    public void testAddTransferMethod_displaysFeeElementsOnTmcResponse() {
        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.add_transfer_method_static_container)).check(
                matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));

        onView(withId(R.id.transfer_method_information)).check(
                matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));

        onView(withId(R.id.transfer_method_information)).check(
                matches(withText(R.string.mobileFeesAndProcessingTime)));

        onView(withId(R.id.add_transfer_method_information)).check(
                matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(withId(R.id.add_transfer_method_information)).check(
                matches(withText("$0.25 fee \u2022 5 - 7 Business days")));
    }

    @Test
    public void testAddTransferMethod_verifyDefaultValues() {
        mActivityTestRule.launchActivity(null);
        onView(withId(R.id.shippingMethod)).check(matches(withText("Standard Mail")));
        onView(withId(R.id.country)).check(matches(withText("United States")));
        onView(withId(R.id.stateProvince)).check(matches(withText("BC")));
        onView(withId(R.id.addressLine1)).check(matches(withText("950 Granville Street")));
        onView(withId(R.id.city)).check(matches(withText("Vancouver")));
        onView(withId(R.id.postalCode)).check(matches(withText("12345")));
    }

    @Test
    public void testAddTransferMethod_returnsErrorOnInvalidPattern() {
        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.postalCode)).perform(nestedScrollTo(), replaceText("123456"));

        onView(withId(R.id.add_transfer_method_button)).perform(nestedScrollTo(), click());

        onView(withId(R.id.postalCodeLabel))
                .check(matches(hasErrorText("is invalid length or format.")));
    }

    @Test
    public void testAddTransferMethod_returnsErrorOnInvalidPresence() {
        mActivityTestRule.launchActivity(null);
        onView(withId(R.id.postalCode)).perform(nestedScrollTo(), replaceText(""));

        onView(withId(R.id.add_transfer_method_button)).perform(nestedScrollTo(), click());

        onView(withId(R.id.postalCodeLabel))
                .check(matches(hasErrorText("You must provide a value for this field")));
    }

}
