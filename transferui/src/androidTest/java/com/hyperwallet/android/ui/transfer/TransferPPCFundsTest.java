package com.hyperwallet.android.ui.transfer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.isSelected;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.concurrent.TimeUnit.SECONDS;

import static com.hyperwallet.android.ui.testutils.util.EspressoUtils.atPosition;
import static com.hyperwallet.android.ui.testutils.util.EspressoUtils.hasErrorText;
import static com.hyperwallet.android.ui.testutils.util.EspressoUtils.nestedScrollTo;
import static com.hyperwallet.android.ui.testutils.util.EspressoUtils.withHint;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.model.StatusTransition;
import com.hyperwallet.android.ui.common.repository.EspressoIdlingResource;
import com.hyperwallet.android.ui.testutils.TestAuthenticationProvider;
import com.hyperwallet.android.ui.testutils.rule.HyperwalletExternalResourceManager;
import com.hyperwallet.android.ui.testutils.rule.HyperwalletMockWebServer;
import com.hyperwallet.android.ui.testutils.util.RecyclerViewCountAssertion;
import com.hyperwallet.android.ui.transfer.repository.TransferRepositoryFactory;
import com.hyperwallet.android.ui.transfer.view.CreateTransferActivity;
import com.hyperwallet.android.ui.user.repository.UserRepositoryFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import okhttp3.mockwebserver.MockResponse;

@RunWith(AndroidJUnit4.class)
public class TransferPPCFundsTest {

    @ClassRule
    public static HyperwalletExternalResourceManager sResourceManager = new HyperwalletExternalResourceManager();
    @Rule
    public HyperwalletMockWebServer mMockWebServer = new HyperwalletMockWebServer(8080);
    @Rule
    public ActivityTestRule<CreateTransferActivity> mActivityTestRule =
            new ActivityTestRule<CreateTransferActivity>(CreateTransferActivity.class, true, false) {
                @Override
                protected Intent getActivityIntent() {
                    Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                            CreateTransferActivity.class);
                    intent.putExtra("TRANSFER_SOURCE_TOKEN", "trm-2beee55a-f2af-4cd3-a952-2375fb871597");
                    return intent;
                }
            };

    @Before
    public void setup() {
        Hyperwallet.getInstance(new TestAuthenticationProvider());

        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("authentication_token_response.json")).mock();
    }

    @After
    public void cleanup() {
        UserRepositoryFactory.clearInstance();
        TransferRepositoryFactory.clearInstance();
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
    public void testTransferFunds_verifyTransferScreen() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("transfer_method_list_single_bank_account_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("ppc/create_transfer_quote_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.add_transfer_destination)).check(matches(not(isDisplayed())));
        onView(withId(R.id.transfer_destination)).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_destination_icon)).check(matches(withText(R.string.bank_account_font_icon)));
        onView(withId(R.id.transfer_destination_title)).check(matches(withText(R.string.bank_account)));
        onView(withId(R.id.transfer_destination_selection)).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_destination_description_1)).check(matches(withText("United States")));
        onView(withId(R.id.transfer_destination_description_2)).check(matches(withText("Ending on 0616")));

        onView(withId(R.id.transfer_amount)).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_amount_layout)).check(matches(withHint("Amount")));
        onView(withId(R.id.transfer_amount_currency)).check(matches(withText("USD")));
        onView(withId(R.id.transfer_all_funds_label)).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_all_funds_label)).check(matches(withText(R.string.transfer_all_funds_label)));

        //Check that the toggle is disabled by default
        onView(withId(R.id.switchButton)).check(matches(isDisplayed()));
        onView(withId(R.id.switchButton)).check(matches(not(isSelected())));
        onView(withId(R.id.transfer_summary)).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_summary)).check(matches(withText("Available for Transfer: 998.00 USD")));

        onView(withId(R.id.transfer_notes)).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_notes_layout)).check(matches(withHint("Description")));
        onView(withText(R.string.transfer_notes_additional_info_label)).check(matches(isDisplayed()));

        onView(withId(R.id.transfer_action_button)).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_action_button)).check(matches(isEnabled()));
    }

    @Test
    public void testTransferFunds_verifyAddDestinationDisplayedWhenUserHasNoExternalAccounts() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_NO_CONTENT).withBody("").mock();

        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.add_transfer_destination)).check(matches(isDisplayed()));
        onView(withId(R.id.add_transfer_destination_icon)).check(matches(withText(R.string.add_text)));
        onView(withId(R.id.add_transfer_destination_title)).check(matches(withText(R.string.add_transfer_label)));
        onView(withId(R.id.add_transfer_destination_description_1)).check(
                matches(withText(R.string.add_transfer_description_1)));
        onView(withId(R.id.add_transfer_destination_description_2)).check(
                matches(withText(R.string.add_transfer_description_2)));

        onView(withId(R.id.transfer_summary)).check(matches(not(isDisplayed())));
        onView(withId(R.id.transfer_action_button)).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_action_button)).check(matches(isEnabled()));
    }

    @Test
    public void testTransferFunds_createTransferWithFX() throws InterruptedException {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("transfer_method_list_single_bank_account_cad_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("ppc/create_transfer_quote_fx_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("ppc/create_transfer_fx_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("schedule_transfer_success_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        final CountDownLatch gate = new CountDownLatch(1);
        final BroadcastReceiver br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                gate.countDown();

                StatusTransition transition = intent.getParcelableExtra(
                        "hyperwallet-local-broadcast-payload");
                assertThat("Token is incorrect", transition.getToken(), is("sts-2157d925-90c9-407b-a9d6-24a0d9dacfb6"));
                assertThat("To Status is incorrect", transition.getToStatus(), is("SCHEDULED"));
                assertThat("From Status is incorrect", transition.getFromStatus(), is("QUOTED"));
                assertThat("Transition is incorrect", transition.getTransition(), is("SCHEDULED"));
                assertThat("Created on is incorrect", transition.getCreatedOn(), is("2019-08-12T17:39:35"));

            }
        };

        LocalBroadcastManager.getInstance(mActivityTestRule.getActivity().getApplicationContext())
                .registerReceiver(br, new IntentFilter("ACTION_HYPERWALLET_TRANSFER_SCHEDULED"));

        onView(withId(R.id.add_transfer_destination)).check(matches(not(isDisplayed())));
        onView(withId(R.id.transfer_destination)).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_destination_icon)).check(matches(withText(R.string.bank_account_font_icon)));
        onView(withId(R.id.transfer_destination_title)).check(matches(withText(R.string.bank_account)));
        onView(withId(R.id.transfer_destination_selection)).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_destination_description_1)).check(matches(withText("Canada")));
        onView(withId(R.id.transfer_destination_description_2)).check(matches(withText("Ending on 5121")));

        onView(withId(R.id.transfer_summary)).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_summary)).check(matches(withText("Available for Transfer: 1,157.40 CAD")));

        onView(withId(R.id.transfer_amount)).perform(replaceText("150.00"));
        onView(withId(R.id.transfer_action_button)).perform(nestedScrollTo(), click());

        onView(withId(R.id.list_foreign_exchange)).check(matches(isDisplayed()));
        onView(withId(R.id.list_foreign_exchange)).check(new RecyclerViewCountAssertion(1));
        onView(withId(R.id.list_foreign_exchange)).check(
                matches(atPosition(0, hasDescendant(
                        allOf(withId(R.id.sell_label), withText(R.string.foreign_exchange_sell_label))))));
        onView(withId(R.id.list_foreign_exchange)).check(
                matches(atPosition(0, hasDescendant(allOf(withId(R.id.sell_value), withText("117.87 USD"))))));
        onView(withId(R.id.list_foreign_exchange)).check(
                matches(atPosition(0,
                        hasDescendant(allOf(withId(R.id.buy_label), withText(R.string.foreign_exchange_buy_label))))));
        onView(withId(R.id.list_foreign_exchange)).check(
                matches(atPosition(0, hasDescendant(allOf(withId(R.id.buy_value), withText("152.20 CAD"))))));
        onView(withId(R.id.list_foreign_exchange)).check(
                matches(atPosition(0, hasDescendant(
                        allOf(withId(R.id.exchange_rate_label), withText(R.string.foreign_exchange_rate_label))))));
        onView(withId(R.id.list_foreign_exchange)).check(
                matches(atPosition(0,
                        hasDescendant(allOf(withId(R.id.exchange_rate_value), withText("1 USD = 1.291253 CAD"))))));
        onView(withId(R.id.amount_label)).check(matches(withText(R.string.summary_amount_label)));
        onView(withId(R.id.amount_value)).check(matches(withText("152.20 CAD")));
        onView(withId(R.id.fee_label)).check(matches(withText(R.string.summary_amount_fee_label)));
        onView(withId(R.id.fee_value)).check(matches(withText("2.20 CAD")));
        onView(withId(R.id.transfer_label)).check(matches(withText(R.string.summary_amount_transfer_label)));
        onView(withId(R.id.transfer_value)).check(matches(withText("150.00 CAD")));
        onView(withId(R.id.notes_container)).check(matches(not(isDisplayed())));
        onView(withId(R.id.notes_value)).check(matches(not(isDisplayed())));

        onView(withId(R.id.transfer_confirm_button)).perform(nestedScrollTo(), click());

        assertThat("Result code is incorrect",
                mActivityTestRule.getActivityResult().getResultCode(), is(Activity.RESULT_OK));

        gate.await(5, SECONDS);
        LocalBroadcastManager.getInstance(mActivityTestRule.getActivity().getApplicationContext()).unregisterReceiver(
                br);
        assertThat("Action is not broadcasted", gate.getCount(), is(0L));
    }

    @Test
    public void testTransferFunds_createTransferWithNoFX() throws InterruptedException {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("transfer_method_list_single_bank_account_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("ppc/create_transfer_quote_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("ppc/create_transfer_no_fx_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("schedule_transfer_success_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        final CountDownLatch gate = new CountDownLatch(1);
        final BroadcastReceiver br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                gate.countDown();

                StatusTransition transition = intent.getParcelableExtra(
                        "hyperwallet-local-broadcast-payload");
                assertThat("Token is incorrect", transition.getToken(), is("sts-2157d925-90c9-407b-a9d6-24a0d9dacfb6"));
                assertThat("To Status is incorrect", transition.getToStatus(), is("SCHEDULED"));
                assertThat("From Status is incorrect", transition.getFromStatus(), is("QUOTED"));
                assertThat("Transition is incorrect", transition.getTransition(), is("SCHEDULED"));
                assertThat("Created on is incorrect", transition.getCreatedOn(), is("2019-08-12T17:39:35"));
            }
        };

        LocalBroadcastManager.getInstance(mActivityTestRule.getActivity().getApplicationContext())
                .registerReceiver(br, new IntentFilter("ACTION_HYPERWALLET_TRANSFER_SCHEDULED"));

        onView(withId(R.id.add_transfer_destination)).check(matches(not(isDisplayed())));
        onView(withId(R.id.transfer_destination)).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_destination_icon)).check(matches(withText(R.string.bank_account_font_icon)));
        onView(withId(R.id.transfer_destination_title)).check(matches(withText(R.string.bank_account)));
        onView(withId(R.id.transfer_destination_selection)).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_destination_description_1)).check(matches(withText("United States")));
        onView(withId(R.id.transfer_destination_description_2)).check(matches(withText("Ending on 0616")));

        onView(withId(R.id.transfer_summary)).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_summary)).check(matches(withText("Available for Transfer: 998.00 USD")));

        onView(withId(R.id.transfer_amount)).perform(replaceText("100.00"));
        onView(withId(R.id.transfer_action_button)).perform(nestedScrollTo(), click());

        onView(withId(R.id.list_foreign_exchange)).check(matches(not(isDisplayed())));
        onView(withId(R.id.amount_label)).check(matches(withText(R.string.summary_amount_label)));
        onView(withId(R.id.amount_value)).check(matches(withText("102.00 USD")));
        onView(withId(R.id.fee_label)).check(matches(withText(R.string.summary_amount_fee_label)));
        onView(withId(R.id.fee_value)).check(matches(withText("2.00 USD")));
        onView(withId(R.id.transfer_label)).check(matches(withText(R.string.summary_amount_transfer_label)));
        onView(withId(R.id.transfer_value)).check(matches(withText("100.00 USD")));
        onView(withId(R.id.notes_container)).check(matches(not(isDisplayed())));
        onView(withId(R.id.notes_value)).check(matches(not(isDisplayed())));

        onView(withId(R.id.transfer_confirm_button)).perform(nestedScrollTo(), click());

        assertThat("Result code is incorrect",
                mActivityTestRule.getActivityResult().getResultCode(), is(Activity.RESULT_OK));

        gate.await(5, SECONDS);
        LocalBroadcastManager.getInstance(mActivityTestRule.getActivity().getApplicationContext()).unregisterReceiver(
                br);
        assertThat("Action is not broadcasted", gate.getCount(), is(0L));
    }

    @Test
    public void testTransferFunds_createTransferWithNotes() throws InterruptedException {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("transfer_method_list_single_bank_account_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("ppc/create_transfer_quote_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("ppc/create_transfer_no_fx_notes_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("schedule_transfer_success_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        final CountDownLatch gate = new CountDownLatch(1);
        final BroadcastReceiver br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                gate.countDown();

                StatusTransition transition = intent.getParcelableExtra(
                        "hyperwallet-local-broadcast-payload");
                assertThat("Token is incorrect", transition.getToken(), is("sts-2157d925-90c9-407b-a9d6-24a0d9dacfb6"));
                assertThat("To Status is incorrect", transition.getToStatus(), is("SCHEDULED"));
                assertThat("From Status is incorrect", transition.getFromStatus(), is("QUOTED"));
                assertThat("Transition is incorrect", transition.getTransition(), is("SCHEDULED"));
                assertThat("Created on is incorrect", transition.getCreatedOn(), is("2019-08-12T17:39:35"));
            }
        };

        LocalBroadcastManager.getInstance(mActivityTestRule.getActivity().getApplicationContext())
                .registerReceiver(br, new IntentFilter("ACTION_HYPERWALLET_TRANSFER_SCHEDULED"));

        onView(withId(R.id.add_transfer_destination)).check(matches(not(isDisplayed())));
        onView(withId(R.id.transfer_destination)).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_destination_icon)).check(matches(withText(R.string.bank_account_font_icon)));
        onView(withId(R.id.transfer_destination_title)).check(matches(withText(R.string.bank_account)));
        onView(withId(R.id.transfer_destination_selection)).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_destination_description_1)).check(matches(withText("United States")));
        onView(withId(R.id.transfer_destination_description_2)).check(matches(withText("Ending on 0616")));

        onView(withId(R.id.transfer_summary)).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_summary)).check(matches(withText("Available for Transfer: 998.00 USD")));

        onView(withId(R.id.transfer_amount)).perform(replaceText("100.00"));
        onView(withId(R.id.transfer_notes)).perform(replaceText("QA Automation Test"));

        onView(withId(R.id.transfer_action_button)).perform(nestedScrollTo(), click());

        onView(withId(R.id.list_foreign_exchange)).check(matches(not(isDisplayed())));
        onView(withId(R.id.amount_label)).check(matches(withText(R.string.summary_amount_label)));
        onView(withId(R.id.amount_value)).check(matches(withText("102.00 USD")));
        onView(withId(R.id.fee_label)).check(matches(withText(R.string.summary_amount_fee_label)));
        onView(withId(R.id.fee_value)).check(matches(withText("2.00 USD")));
        onView(withId(R.id.transfer_label)).check(matches(withText(R.string.summary_amount_transfer_label)));
        onView(withId(R.id.transfer_value)).check(matches(withText("100.00 USD")));
        onView(withId(R.id.notes_container)).check(matches(isDisplayed()));
        onView(withId(R.id.notes_value)).check(matches(withText("Transfer funds test")));

        onView(withId(R.id.transfer_confirm_button)).perform(nestedScrollTo(), click());

        assertThat("Result code is incorrect",
                mActivityTestRule.getActivityResult().getResultCode(), is(Activity.RESULT_OK));

        gate.await(5, SECONDS);
        LocalBroadcastManager.getInstance(mActivityTestRule.getActivity().getApplicationContext()).unregisterReceiver(
                br);
        assertThat("Action is not broadcasted", gate.getCount(), is(0L));
    }

    @Test
    public void testTransferFunds_createTransferWithEmptyFees() throws InterruptedException {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("transfer_method_list_single_bank_account_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("ppc/create_transfer_quote_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("ppc/create_transfer_no_fees_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("schedule_transfer_success_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        final CountDownLatch gate = new CountDownLatch(1);
        final BroadcastReceiver br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                gate.countDown();

                StatusTransition transition = intent.getParcelableExtra(
                        "hyperwallet-local-broadcast-payload");
                assertThat("Token is incorrect", transition.getToken(), is("sts-2157d925-90c9-407b-a9d6-24a0d9dacfb6"));
                assertThat("To Status is incorrect", transition.getToStatus(), is("SCHEDULED"));
                assertThat("From Status is incorrect", transition.getFromStatus(), is("QUOTED"));
                assertThat("Transition is incorrect", transition.getTransition(), is("SCHEDULED"));
                assertThat("Created on is incorrect", transition.getCreatedOn(), is("2019-08-12T17:39:35"));
            }
        };

        LocalBroadcastManager.getInstance(mActivityTestRule.getActivity().getApplicationContext())
                .registerReceiver(br, new IntentFilter("ACTION_HYPERWALLET_TRANSFER_SCHEDULED"));

        onView(withId(R.id.add_transfer_destination)).check(matches(not(isDisplayed())));
        onView(withId(R.id.transfer_destination)).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_destination_icon)).check(matches(withText(R.string.bank_account_font_icon)));
        onView(withId(R.id.transfer_destination_title)).check(matches(withText(R.string.bank_account)));
        onView(withId(R.id.transfer_destination_selection)).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_destination_description_1)).check(matches(withText("United States")));
        onView(withId(R.id.transfer_destination_description_2)).check(matches(withText("Ending on 0616")));

        onView(withId(R.id.transfer_summary)).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_summary)).check(matches(withText("Available for Transfer: 998.00 USD")));

        onView(withId(R.id.transfer_amount)).perform(replaceText("100.00"));
        onView(withId(R.id.transfer_notes)).perform(replaceText("QA Automation Test"));

        onView(withId(R.id.transfer_action_button)).perform(nestedScrollTo(), click());

        onView(withId(R.id.list_foreign_exchange)).check(matches(not(isDisplayed())));
        onView(withId(R.id.amount_label)).check(matches(withText(R.string.summary_amount_label)));
        onView(withId(R.id.amount_value)).check(matches(withText("100.00 USD")));
        onView(withId(R.id.fee_label)).check(matches(not(isDisplayed())));
        onView(withId(R.id.fee_value)).check(matches(not(isDisplayed())));
        onView(withId(R.id.transfer_label)).check(matches(not(isDisplayed())));
        onView(withId(R.id.transfer_value)).check(matches(not(isDisplayed())));
        onView(withId(R.id.notes_container)).check(matches(not(isDisplayed())));
        onView(withId(R.id.notes_value)).check(matches(not(isDisplayed())));

        onView(withId(R.id.transfer_confirm_button)).perform(nestedScrollTo(), click());

        assertThat("Result code is incorrect",
                mActivityTestRule.getActivityResult().getResultCode(), is(Activity.RESULT_OK));

        gate.await(5, SECONDS);
        LocalBroadcastManager.getInstance(mActivityTestRule.getActivity().getApplicationContext()).unregisterReceiver(
                br);
        assertThat("Action is not broadcasted", gate.getCount(), is(0L));
    }

    @Test
    public void testTransferFunds_createTransferWithAllFunds() throws InterruptedException {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("transfer_method_list_single_bank_account_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("ppc/create_transfer_quote_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("ppc/create_transfer_all_funds_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("schedule_transfer_success_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        final CountDownLatch gate = new CountDownLatch(1);
        final BroadcastReceiver br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                gate.countDown();

                StatusTransition transition = intent.getParcelableExtra(
                        "hyperwallet-local-broadcast-payload");
                assertThat("Token is incorrect", transition.getToken(), is("sts-2157d925-90c9-407b-a9d6-24a0d9dacfb6"));
                assertThat("To Status is incorrect", transition.getToStatus(), is("SCHEDULED"));
                assertThat("From Status is incorrect", transition.getFromStatus(), is("QUOTED"));
                assertThat("Transition is incorrect", transition.getTransition(), is("SCHEDULED"));
                assertThat("Created on is incorrect", transition.getCreatedOn(), is("2019-08-12T17:39:35"));
            }
        };

        LocalBroadcastManager.getInstance(mActivityTestRule.getActivity().getApplicationContext())
                .registerReceiver(br, new IntentFilter("ACTION_HYPERWALLET_TRANSFER_SCHEDULED"));

        onView(withId(R.id.add_transfer_destination)).check(matches(not(isDisplayed())));
        onView(withId(R.id.transfer_destination)).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_destination_icon)).check(matches(withText(R.string.bank_account_font_icon)));
        onView(withId(R.id.transfer_destination_title)).check(matches(withText(R.string.bank_account)));
        onView(withId(R.id.transfer_destination_selection)).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_destination_description_1)).check(matches(withText("United States")));
        onView(withId(R.id.transfer_destination_description_2)).check(matches(withText("Ending on 0616")));

        onView(withId(R.id.switchButton)).perform(nestedScrollTo(), click());
        onView(withId(R.id.transfer_amount)).check(matches(withText("998.00")));
        onView(withId(R.id.transfer_amount)).check(matches(not(isEnabled())));
        onView(withId(R.id.transfer_notes)).perform(replaceText("Transfer all funds test"));

        onView(withId(R.id.transfer_action_button)).perform(nestedScrollTo(), click());

        onView(withId(R.id.list_foreign_exchange)).check(matches(not(isDisplayed())));

        onView(withId(R.id.amount_label)).check(matches(withText(R.string.summary_amount_label)));
        onView(withId(R.id.amount_value)).check(matches(withText("1,000.00 USD")));
        onView(withId(R.id.fee_label)).check(matches(withText(R.string.summary_amount_fee_label)));
        onView(withId(R.id.fee_value)).check(matches(withText("2.00 USD")));
        onView(withId(R.id.transfer_label)).check(matches(withText(R.string.summary_amount_transfer_label)));
        onView(withId(R.id.transfer_value)).check(matches(withText("998.00 USD")));

        onView(withId(R.id.notes_container)).perform(nestedScrollTo());
        onView(withId(R.id.notes_container)).check(matches(isDisplayed()));
        onView(withId(R.id.notes_value)).check(matches(withText("Transfer funds test")));

        onView(withId(R.id.transfer_confirm_button)).perform(nestedScrollTo(), click());

        assertThat("Result code is incorrect",
                mActivityTestRule.getActivityResult().getResultCode(), is(Activity.RESULT_OK));

        gate.await(5, SECONDS);
        LocalBroadcastManager.getInstance(mActivityTestRule.getActivity().getApplicationContext()).unregisterReceiver(
                br);
        assertThat("Action is not broadcasted", gate.getCount(), is(0L));
    }

    @Test
    public void testTransferFunds_createTransferAmountNotSetError() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("transfer_method_list_single_bank_account_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("ppc/create_transfer_quote_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.transfer_action_button)).perform(nestedScrollTo(), click());
        onView(withId(R.id.transfer_amount_layout)).check(matches(hasErrorText(R.string.validation_amount_required)));
    }

    @Test
    public void testTransferFunds_createTransferInvalidAmountError() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("transfer_method_list_single_bank_account_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("ppc/create_transfer_quote_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_BAD_REQUEST).withBody(sResourceManager
                .getResourceContent("errors/create_transfer_error_invalid_amount_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.transfer_amount)).perform(replaceText("100.00"));
        onView(withId(R.id.transfer_action_button)).perform(nestedScrollTo(), click());

        onView(withId(R.id.transfer_amount_layout)).check(matches(hasErrorText("Invalid amount.")));
    }

    @Test
    public void testTransferFunds_createTransferDestinationNotSetError() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_NO_CONTENT).withBody("").mock();

        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.transfer_amount)).perform(replaceText("100.00"));
        onView(withId(R.id.transfer_action_button)).perform(nestedScrollTo(), click());

        onView(withId(R.id.transfer_destination_error)).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_destination_error)).check(
                matches(withText(R.string.validation_destination_required)));
    }

    @Test
    public void testTransferFunds_createTransferLimitError() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("transfer_method_list_single_bank_account_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("ppc/create_transfer_quote_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_BAD_REQUEST).withBody(sResourceManager
                .getResourceContent("errors/create_transfer_error_limit_exceeded_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.transfer_amount)).perform(replaceText("100000.00"));
        onView(withId(R.id.transfer_action_button)).perform(nestedScrollTo(), click());

        onView(withId(R.id.alertTitle)).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withId(R.id.alertTitle)).check(matches(withText(R.string.error_dialog_title)));
        onView(withText(
                "Your attempted transaction has exceeded the approved payout limit; please contact HyperWallet Pay "
                        + "for further assistance."))
                .inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withId(android.R.id.button1)).check(matches(withText(R.string.close_button_label)));
        onView(withId(android.R.id.button1)).perform(click());
    }

    @Test
    public void testTransferFunds_createTransferInsufficientFundsError() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("transfer_method_list_single_bank_account_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("ppc/create_transfer_quote_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_BAD_REQUEST).withBody(sResourceManager
                .getResourceContent("errors/create_transfer_error_insufficient_funds_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.transfer_amount)).perform(replaceText("5000.00"));
        onView(withId(R.id.transfer_action_button)).perform(nestedScrollTo(), click());

        onView(withId(R.id.alertTitle)).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withId(R.id.alertTitle)).check(matches(withText(R.string.error_dialog_title)));
        onView(withText("You do not have enough funds in any single currency to complete this transfer."))
                .inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withId(android.R.id.button1)).check(matches(withText(R.string.close_button_label)));
        onView(withId(android.R.id.button1)).perform(click());
    }

    @Test
    public void testTransferFunds_createTransferMinimumAmountError() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("transfer_method_list_single_bank_account_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("ppc/create_transfer_quote_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_BAD_REQUEST).withBody(sResourceManager
                .getResourceContent("errors/create_transfer_error_limit_subceeded_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.transfer_amount)).perform(replaceText("5000.00"));
        onView(withId(R.id.transfer_action_button)).perform(nestedScrollTo(), click());

        onView(withId(R.id.alertTitle)).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withId(R.id.alertTitle)).check(matches(withText(R.string.error_dialog_title)));
        onView(withText("Requested transfer amount $0.01, is below the transaction limit of $1.00."))
                .inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withId(android.R.id.button1)).check(matches(withText(R.string.close_button_label)));
        onView(withId(android.R.id.button1)).perform(click());

    }

    @Test
    public void testTransferFunds_createTransferInvalidSourceError() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("transfer_method_list_single_bank_account_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("ppc/create_transfer_quote_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_BAD_REQUEST).withBody(sResourceManager
                .getResourceContent("errors/create_transfer_error_invalid_wallet_status_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.transfer_amount)).perform(replaceText("100.00"));
        onView(withId(R.id.transfer_action_button)).perform(nestedScrollTo(), click());

        onView(withId(R.id.alertTitle)).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withId(R.id.alertTitle)).check(matches(withText(R.string.error_dialog_title)));
        onView(withText("The account status does not allow the requested action."))
                .inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withId(android.R.id.button1)).check(matches(withText(R.string.close_button_label)));
        onView(withId(android.R.id.button1)).perform(click());
    }

    @Test
    public void testTransferFunds_createTransferConnectionError() throws TimeoutException, InterruptedException {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("transfer_method_list_single_bank_account_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("ppc/create_transfer_quote_response.json")).mock();
        mMockWebServer.getServer().enqueue(new MockResponse().setResponseCode(HTTP_OK).setBody(sResourceManager
                .getResourceContent("ppc/create_transfer_no_fx_response.json")).setBodyDelay(10500, TimeUnit
                .MILLISECONDS));
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("ppc/create_transfer_no_fx_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.transfer_amount)).perform(replaceText("100.00"));
        onView(withId(R.id.transfer_action_button)).perform(nestedScrollTo(), click());

        onView(withText(R.string.error_dialog_connectivity_title)).check(matches(isDisplayed()));
        onView(withText(R.string.io_exception)).check(matches(isDisplayed()));
        onView(withId(android.R.id.button1)).check(matches(withText(R.string.try_again_button_label)));
        onView(withId(android.R.id.button2)).check(matches(withText(R.string.cancel_button_label)));

        onView(withId(android.R.id.button1)).perform(click());
        onView(withText(R.string.error_dialog_connectivity_title)).check(doesNotExist());

        onView(withId(R.id.amount_label)).check(matches(withText(R.string.summary_amount_label)));
        onView(withId(R.id.amount_value)).check(matches(withText("102.00 USD")));
        onView(withId(R.id.fee_label)).check(matches(withText(R.string.summary_amount_fee_label)));
        onView(withId(R.id.fee_value)).check(matches(withText("2.00 USD")));
        onView(withId(R.id.transfer_label)).check(matches(withText(R.string.summary_amount_transfer_label)));
        onView(withId(R.id.transfer_value)).check(matches(withText("100.00 USD")));
    }

    @Test
    public void testTransferFunds_createTransferConfirmationConnectionError()
            throws InterruptedException, TimeoutException {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("transfer_method_list_single_bank_account_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("ppc/create_transfer_quote_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("ppc/create_transfer_no_fx_response.json")).mock();
        mMockWebServer.getServer().enqueue(new MockResponse().setResponseCode(HTTP_OK).setBody(sResourceManager
                .getResourceContent("schedule_transfer_success_response.json")).setBodyDelay(10500, TimeUnit
                .MILLISECONDS));
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("schedule_transfer_success_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        final CountDownLatch gate = new CountDownLatch(1);
        final BroadcastReceiver br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                gate.countDown();

                StatusTransition transition = intent.getParcelableExtra(
                        "hyperwallet-local-broadcast-payload");
                assertThat("Token is incorrect", transition.getToken(), is("sts-2157d925-90c9-407b-a9d6-24a0d9dacfb6"));
                assertThat("To Status is incorrect", transition.getToStatus(), is("SCHEDULED"));
                assertThat("From Status is incorrect", transition.getFromStatus(), is("QUOTED"));
                assertThat("Transition is incorrect", transition.getTransition(), is("SCHEDULED"));
                assertThat("Created on is incorrect", transition.getCreatedOn(), is("2019-08-12T17:39:35"));
            }
        };

        LocalBroadcastManager.getInstance(mActivityTestRule.getActivity().getApplicationContext())
                .registerReceiver(br, new IntentFilter("ACTION_HYPERWALLET_TRANSFER_SCHEDULED"));

        onView(withId(R.id.transfer_amount)).perform(replaceText("100.00"));
        onView(withId(R.id.transfer_action_button)).perform(nestedScrollTo(), click());

        onView(withId(R.id.amount_label)).check(matches(withText(R.string.summary_amount_label)));
        onView(withId(R.id.amount_value)).check(matches(withText("102.00 USD")));
        onView(withId(R.id.fee_label)).check(matches(withText(R.string.summary_amount_fee_label)));
        onView(withId(R.id.fee_value)).check(matches(withText("2.00 USD")));
        onView(withId(R.id.transfer_label)).check(matches(withText(R.string.summary_amount_transfer_label)));
        onView(withId(R.id.transfer_value)).check(matches(withText("100.00 USD")));
        onView(withId(R.id.notes_container)).check(matches(not(isDisplayed())));
        onView(withId(R.id.notes_value)).check(matches(not(isDisplayed())));

        onView(withId(R.id.transfer_confirm_button)).perform(nestedScrollTo(), click());

        onView(withText(R.string.error_dialog_connectivity_title)).check(matches(isDisplayed()));
        onView(withText(R.string.io_exception)).check(matches(isDisplayed()));
        onView(withId(android.R.id.button1)).check(matches(withText(R.string.try_again_button_label)));
        onView(withId(android.R.id.button2)).check(matches(withText(R.string.cancel_button_label)));

        onView(withId(android.R.id.button1)).perform(click());

        assertThat("Result code is incorrect",
                mActivityTestRule.getActivityResult().getResultCode(), is(Activity.RESULT_OK));

        gate.await(5, SECONDS);
        LocalBroadcastManager.getInstance(mActivityTestRule.getActivity().getApplicationContext()).unregisterReceiver(
                br);
        assertThat("Action is not broadcasted", gate.getCount(), is(0L));
    }

}
