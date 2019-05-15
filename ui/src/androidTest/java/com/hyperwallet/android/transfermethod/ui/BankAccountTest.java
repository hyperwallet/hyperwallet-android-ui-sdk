package com.hyperwallet.android.transfermethod.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.concurrent.TimeUnit.SECONDS;

import static com.hyperwallet.android.model.HyperwalletBankAccount.Purpose.SAVINGS;
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
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.hyperwallet_ui.R;
import com.hyperwallet.android.model.HyperwalletTransferMethod;
import com.hyperwallet.android.rule.HyperwalletExternalResourceManager;
import com.hyperwallet.android.rule.HyperwalletMockWebServer;
import com.hyperwallet.android.ui.repository.RepositoryFactory;
import com.hyperwallet.android.ui.transfermethod.AddTransferMethodActivity;
import com.hyperwallet.android.ui.util.EspressoIdlingResource;
import com.hyperwallet.android.util.RecyclerViewCountAssertion;
import com.hyperwallet.android.util.TestAuthenticationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

@RunWith(AndroidJUnit4.class)
public class BankAccountTest {

    private static final String ACCOUNT_NUMBER_LABEL = "Account Number";
    private static final String ROUTING_NUMBER_LABEL = "Routing Number";
    private static final String ACCOUNT_TYPE_LABEL = "Account Type";
    private static final String ACCOUNT_NUMBER = "8017110254";
    private static final String ROUTING_NUMBER = "211179539";
    private static final String INVALID_ROUTING_NUMBER = "211179531";

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
                    intent.putExtra("TRANSFER_METHOD_TYPE", "BANK_ACCOUNT");
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
                .getResourceContent("successful_tmc_fields_response.json")).mock();
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
                matches(withText(R.string.title_add_bank_account)));

        onView(withId(R.id.branchId)).check(matches(isDisplayed()));
        onView(withId(R.id.branchIdLabel)).check(matches(withHint(ROUTING_NUMBER_LABEL)));
        onView(withId(R.id.bankAccountId)).check(matches(isDisplayed()));
        onView(withId(R.id.bankAccountIdLabel)).check(matches(withHint(ACCOUNT_NUMBER_LABEL)));
        onView(withId(R.id.bankAccountPurpose)).check(matches(isDisplayed()));
        onView(withId(R.id.bankAccountPurposeLabel)).check(
                matches(withHint(ACCOUNT_TYPE_LABEL)));

        onView(withId(R.id.add_transfer_method_button)).perform(nestedScrollTo()).check(
                matches(withText(R.string.button_create_transfer_method)));
    }

    @Test
    public void testAddTransferMethod_displaysFeeElementsOnTmcResponse() {
        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.add_transfer_method_fee_label)).check(
                matches(withText(R.string.add_transfer_method_fee_label)));
        onView(withId(R.id.add_transfer_method_processing_label)).check(
                matches(withText(R.string.add_transfer_method_processing_time_label)));
        onView(withId(R.id.add_transfer_method_fee_value)).check(matches(withText("USD 2.00")));
        onView(withId(R.id.add_transfer_method_processing_time_value)).check(matches(withText("1-2 Business days")));
    }

    @Test
    public void testAddTransferMethod_returnsTokenOnBankAccountCreation() throws InterruptedException {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_CREATED).withBody(sResourceManager
                .getResourceContent("bank_account_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        final CountDownLatch gate = new CountDownLatch(1);
        final BroadcastReceiver br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                gate.countDown();

                HyperwalletTransferMethod transferMethod = intent.getParcelableExtra(
                        "hyperwallet-local-broadcast-payload");
                assertThat("Bank Account Id is incorrect", transferMethod.getField(
                        HyperwalletTransferMethod.TransferMethodFields.BANK_ACCOUNT_ID), is(ACCOUNT_NUMBER));
                assertThat("Branch Id is incorrect", transferMethod.getField(
                        HyperwalletTransferMethod.TransferMethodFields.BRANCH_ID), is(ROUTING_NUMBER));
                assertThat("Bank Account purpose is incorrect", transferMethod.getField(
                        HyperwalletTransferMethod.TransferMethodFields.BANK_ACCOUNT_PURPOSE), is(SAVINGS));
            }
        };

        LocalBroadcastManager.getInstance(mActivityTestRule.getActivity().getApplicationContext())
                .registerReceiver(br, new IntentFilter("ACTION_HYPERWALLET_TRANSFER_METHOD_ADDED"));

        onView(withId(R.id.branchId))
                .perform(typeText(ROUTING_NUMBER))
                .perform(closeSoftKeyboard());
        onView(withId(R.id.bankAccountId))
                .perform(typeText(ACCOUNT_NUMBER))
                .perform(closeSoftKeyboard());
        onView(withId(R.id.bankAccountPurpose)).perform(click());
        onView(withId(R.id.search_button)).check(doesNotExist());
        onView(withId(R.id.input_selection_list)).check(new RecyclerViewCountAssertion(2));
        onView(allOf(withId(R.id.select_name), withText("Savings"))).perform(click());

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
        onView(withId(R.id.branchId)).perform(typeText("a12-345"));
        onView(withId(R.id.branchId)).check(matches(withText("12345")));
    }

    @Test
    public void testAddTransferMethod_returnsErrorOnInvalidPresence() {
        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.branchId)).perform(click()).perform(closeSoftKeyboard());
        onView(withId(R.id.bankAccountId)).perform(click()).perform(closeSoftKeyboard());
        onView(withId(R.id.bankAccountPurpose)).perform(click());

        onView(allOf(withContentDescription(R.string.abc_action_bar_up_description),
                withParent(withId(R.id.input_selection_toolbar)))).perform(
                click());

        onView(withId(R.id.add_transfer_method_button)).perform(nestedScrollTo(), click());

        onView(withId(R.id.branchIdLabel))
                .check(matches(hasErrorText("You must provide a value for this field.")));
        onView(withId(R.id.bankAccountIdLabel))
                .check(matches(hasErrorText("You must provide a value for this field.")));
        onView(withId(R.id.bankAccountPurposeLabel))
                .check(matches(hasErrorText("You must provide a value for this field.")));
    }

    @Test
    public void testAddTransferMethod_returnsErrorOnInvalidLength() {
        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.branchId))
                .perform(typeText("2111795311"))
                .perform(closeSoftKeyboard());
        onView(withId(R.id.bankAccountId))
                .perform(typeText("1"))
                .perform(closeSoftKeyboard());

        onView(withId(R.id.add_transfer_method_button)).perform(nestedScrollTo(), click());

        onView(withId(R.id.branchIdLabel))
                .check(matches(hasErrorText("The exact length of this field is 9.")));
        onView(withId(R.id.bankAccountIdLabel))
                .check(matches(hasErrorText("The minimum length of this field is 4 and maximum length is 17.")));
    }

    @Test
    public void testAddTransferMethod_displaysErrorOnInvalidRoutingNumber() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_BAD_REQUEST).withBody(sResourceManager
                .getResourceContent("bank_account_invalid_routing_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.branchId))
                .perform(typeText(INVALID_ROUTING_NUMBER)).perform(closeSoftKeyboard());
        onView(withId(R.id.bankAccountId))
                .perform(typeText(ACCOUNT_NUMBER))
                .perform(closeSoftKeyboard());
        onView(withId(R.id.bankAccountPurpose)).perform(click());
        onView(allOf(withId(R.id.select_name), withText("Checking"))).perform(click());

        onView(withId(R.id.add_transfer_method_button)).perform(nestedScrollTo(), click());

        onView(withId(R.id.branchIdLabel))
                .check(matches(hasErrorText(
                        "Routing Number [" + INVALID_ROUTING_NUMBER
                                + "] is not valid. Please modify Routing Number to a valid ACH Routing Number of the "
                                + "branch of your bank.")));
    }

}
