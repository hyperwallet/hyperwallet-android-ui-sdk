package com.hyperwallet.android.transfermethod.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
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

import static com.hyperwallet.android.util.EspressoUtils.hasErrorText;
import static com.hyperwallet.android.util.EspressoUtils.nestedScrollTo;
import static com.hyperwallet.android.util.EspressoUtils.withHint;

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

import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.hyperwallet_ui.R;
import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod;
import com.hyperwallet.android.rule.HyperwalletExternalResourceManager;
import com.hyperwallet.android.rule.HyperwalletMockWebServer;
import com.hyperwallet.android.ui.repository.RepositoryFactory;
import com.hyperwallet.android.ui.transfermethod.AddTransferMethodActivity;
import com.hyperwallet.android.common.util.EspressoIdlingResource;
import com.hyperwallet.android.util.TestAuthenticationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

@RunWith(AndroidJUnit4.class)
public class PayPalTest {

    @ClassRule
    public static HyperwalletExternalResourceManager sResourceManager = new HyperwalletExternalResourceManager();
    @Rule
    public HyperwalletMockWebServer mMockWebServer = new HyperwalletMockWebServer(8080);
    @Rule
    public ActivityTestRule<AddTransferMethodActivity> mActivityTestRule =
            new ActivityTestRule<AddTransferMethodActivity>(AddTransferMethodActivity.class, true, false) {
                @Override
                protected Intent getActivityIntent() {
                    Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                            AddTransferMethodActivity.class);
                    intent.putExtra("TRANSFER_METHOD_TYPE", "PAYPAL_ACCOUNT");
                    intent.putExtra("TRANSFER_METHOD_COUNTRY", "US");
                    intent.putExtra("TRANSFER_METHOD_CURRENCY", "USD");
                    intent.putExtra("TRANSFER_METHOD_PROFILE_TYPE", "INDIVIDUAL");
                    return intent;
                }
            };

    @Before
    public void setup() {
        Hyperwallet.getInstance(new TestAuthenticationProvider());

        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("authentication_token_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("successful_tmc_fields_paypal_response.json")).mock();
    }

    @After
    public void cleanup() {
        RepositoryFactory.clearInstance();
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
    public void testAddTransferMethod_displaysElementsOnTmcResponse() {
        mActivityTestRule.launchActivity(null);

        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar)))).check(
                matches(withText(R.string.paypal_account)));

        onView(withId(R.id.email)).check(matches(isDisplayed()));
        onView(withId(R.id.emailLabel)).check(matches(isDisplayed()));
        onView(withId(R.id.emailLabel)).check(matches(withHint("Email")));

        onView(withId(R.id.add_transfer_method_button)).perform(nestedScrollTo()).check(
                matches(withText(R.string.button_create_transfer_method)));
    }

    @Test
    public void testAddTransferMethod_displaysFeeElementsOnTmcResponse() {
        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.add_transfer_method_static_container)).check(
                matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));

        onView(withId(R.id.add_transfer_method_fee_label)).check(
                matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(withId(R.id.add_transfer_method_fee_label)).check(
                matches(withText(R.string.add_transfer_method_fee_label)));
        onView(withId(R.id.add_transfer_method_fee_value)).check(matches(withText("USD 0.25")));

        //TODO: Uncomment when processing time node is implemented
//        onView(withId(R.id.add_transfer_method_processing_label)).check(
//                matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
//        onView(withId(R.id.add_transfer_method_processing_label)).check(
//                matches(withText(R.string.add_transfer_method_processing_time_label)));
//        onView(withId(R.id.add_transfer_method_fee_value)).check(matches(withText("IMMEDIATE")));

    }

    @Test
    public void testAddTransferMethod_returnsTokenOnPaypalAccountCreation() throws InterruptedException {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_CREATED).withBody(sResourceManager
                .getResourceContent("paypal_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        final CountDownLatch gate = new CountDownLatch(1);
        final BroadcastReceiver br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                gate.countDown();

                HyperwalletTransferMethod transferMethod = intent.getParcelableExtra(
                        "hyperwallet-local-broadcast-payload");
                assertThat("Bank Account Id is incorrect", transferMethod.getField(
                        HyperwalletTransferMethod.TransferMethodFields.EMAIL), is("sunshine.carreiro@hyperwallet.com"));
            }
        };

        LocalBroadcastManager.getInstance(mActivityTestRule.getActivity().getApplicationContext())
                .registerReceiver(br, new IntentFilter("ACTION_HYPERWALLET_TRANSFER_METHOD_ADDED"));

        onView(withId(R.id.email))
                .perform(typeText("sunshine.carreiro@hyperwallet.com"))
                .perform(closeSoftKeyboard());

        onView(withId(R.id.add_transfer_method_button)).perform(nestedScrollTo(), click());

        assertThat("Result code is incorrect",
                mActivityTestRule.getActivityResult().getResultCode(), is(Activity.RESULT_OK));

        gate.await(5, SECONDS);
        LocalBroadcastManager.getInstance(mActivityTestRule.getActivity().getApplicationContext()).unregisterReceiver(
                br);
        assertThat("Action is not broadcasted", gate.getCount(), is(0L));
    }

    @Test
    public void testAddTransferMethod_returnsErrorOnInvalidPattern() {
        mActivityTestRule.launchActivity(null);
        // Number input should not allow non numeric values
        onView(withId(R.id.email)).perform(typeText("abc1test"));
        onView(withId(R.id.add_transfer_method_button)).perform(nestedScrollTo(), click());

        onView(withId(R.id.emailLabel))
                .check(matches(hasErrorText("is invalid length or format.")));
    }

    @Test
    public void testAddTransferMethod_returnsErrorOnInvalidPresence() {
        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.add_transfer_method_button)).perform(nestedScrollTo(), click());

        onView(withId(R.id.emailLabel))
                .check(matches(hasErrorText("You must provide a value for this field")));
    }

    @Test
    public void testAddTransferMethod_displaysErrorOnInvalidEmailAddress() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_BAD_REQUEST).withBody(sResourceManager
                .getResourceContent("paypal_invalid_email_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.email))
                .perform(typeText("invalidEmail@gmail.com")).perform(closeSoftKeyboard());
        onView(withId(R.id.add_transfer_method_button)).perform(nestedScrollTo(), click());

        // check dialog content
        onView(withText(R.string.error_dialog_title)).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withText(containsString(
                "PayPal transfer method email address should be same as profile email address.")))
                .inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withId(android.R.id.button1)).check(matches(withText(R.string.close_button_label)));
        onView(withId(android.R.id.button1)).perform(click());

        // should display the add tm form
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar)))).check(
                matches(withText(R.string.paypal_account)));

        // connectivity dialog should be dismissed and does not exist in ui
        onView(withText(R.string.error_dialog_title)).check(doesNotExist());
    }

}
