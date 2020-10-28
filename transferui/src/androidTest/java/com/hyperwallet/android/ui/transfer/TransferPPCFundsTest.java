package com.hyperwallet.android.ui.transfer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
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

import static com.hyperwallet.android.ui.common.intent.HyperwalletIntent.ACTION_SELECT_TRANSFER_METHOD;
import static com.hyperwallet.android.ui.testutils.util.EspressoUtils.atPosition;
import static com.hyperwallet.android.ui.testutils.util.EspressoUtils.hasErrorText;
import static com.hyperwallet.android.ui.testutils.util.EspressoUtils.nestedScrollTo;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.hyperwallet.android.model.StatusTransition;
import com.hyperwallet.android.ui.common.repository.EspressoIdlingResource;
import com.hyperwallet.android.ui.testutils.rule.HyperwalletExternalResourceManager;
import com.hyperwallet.android.ui.testutils.rule.HyperwalletMockWebServer;
import com.hyperwallet.android.ui.testutils.rule.HyperwalletSdkRule;
import com.hyperwallet.android.ui.testutils.util.RecyclerViewCountAssertion;
import com.hyperwallet.android.ui.transfer.repository.TransferRepositoryFactory;
import com.hyperwallet.android.ui.transfer.rule.HyperwalletSdkMockRule;
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

import okhttp3.mockwebserver.MockResponse;

@RunWith(AndroidJUnit4.class)
public class TransferPPCFundsTest {

    @ClassRule
    public static HyperwalletExternalResourceManager sResourceManager = new HyperwalletExternalResourceManager();

    @Rule
    public HyperwalletSdkMockRule mHyperwalletMockRule = new HyperwalletSdkMockRule();

    @Rule
    public HyperwalletMockWebServer mMockWebServer = new HyperwalletMockWebServer(8080);
    @Rule
    public ActivityTestRule<CreateTransferActivity> mActivityTestRule =
            new ActivityTestRule<CreateTransferActivity>(CreateTransferActivity.class, true, false) {
                @Override
                protected Intent getActivityIntent() {
                    Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                            CreateTransferActivity.class);
                    intent.putExtra("TRANSFER_SOURCE_TOKEN", "trm-fake-token");
                    return intent;
                }
            };
    @Rule
    public IntentsTestRule<CreateTransferActivity> mActivityIntentsTestRule =
            new IntentsTestRule<CreateTransferActivity>(CreateTransferActivity.class, true, false) {
                @Override
                protected Intent getActivityIntent() {
                    Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                            CreateTransferActivity.class);
                    intent.putExtra("TRANSFER_SOURCE_TOKEN", "trm-fake-token");
                    return intent;
                }
            };

    @Before
    public void setup() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("authentication_token_wallet_model_response.json")).mock();
        IdlingRegistry.getInstance().register(EspressoIdlingResource.getIdlingResource());

    }

    @After
    public void cleanup() {
        UserRepositoryFactory.clearInstance();
        TransferRepositoryFactory.clearInstance();
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.getIdlingResource());
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
                .getResourceContent("ppc/prepaid_card_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("transfer_method_list_single_bank_account_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("ppc/create_transfer_quote_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.add_transfer_destination)).check(matches(not(isDisplayed())));
        onView(withId(R.id.transfer_destination)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_destination_icon)).check(matches(withText(R.string.bank_account_font_icon)));
        onView(withId(R.id.transfer_destination_title)).check(matches(withText(R.string.bank_account)));
        onView(withId(R.id.transfer_destination_description_1)).check(matches(withText("United States")));
        onView(withId(R.id.transfer_destination_description_2)).check(matches(withText("ending in 0616")));

        onView(withId(R.id.transfer_amount)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_amount_currency)).check(matches(withText("USD")));

        //Check that the toggle is disabled by default
        onView(withId(R.id.transfer_all_funds)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_all_funds)).check(matches(not(isSelected())));
        onView(withId(R.id.transfer_summary)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        String availableFundUSD = getAvailableFund("998.00", "USD");
        onView(withId(R.id.transfer_summary)).check(matches(withText(availableFundUSD)));

        onView(withId(R.id.transfer_action_button)).perform(nestedScrollTo());
        onView(withId(R.id.transfer_notes)).perform(nestedScrollTo()).check(matches(isDisplayed()));

        onView(withId(R.id.transfer_action_button)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_action_button)).check(matches(isEnabled()));
    }

    @Test
    public void testTransferFunds_verifyDestinationUpdatedAfterAddingNewExternalAccount() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("ppc/prepaid_card_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_NO_CONTENT).withBody("").mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("create_transfer_quote_response.json")).mock();

        mActivityIntentsTestRule.launchActivity(null);

        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, null);
        intending(hasAction(ACTION_SELECT_TRANSFER_METHOD)).respondWith(result);

        onView(withId(R.id.add_transfer_destination)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.add_transfer_destination_icon)).check(matches(withText(R.string.add_text)));
        onView(withId(R.id.add_transfer_destination_title)).check(matches(withText(R.string.mobileAddTransferMethod)));

        onView(withId(R.id.add_transfer_destination)).perform(click());
        intended(hasAction(ACTION_SELECT_TRANSFER_METHOD));

        onView(withId(R.id.add_transfer_destination)).check(matches(not(isDisplayed())));
        onView(withId(R.id.transfer_destination)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_destination_icon)).check(matches(withText(R.string.bank_account_font_icon)));
        onView(withId(R.id.transfer_destination_title)).check(matches(withText(R.string.bank_account)));
        onView(withId(R.id.transfer_destination_description_1)).check(matches(withText("United States")));
        onView(withId(R.id.transfer_destination_description_2)).check(matches(withText("ending in 0616")));

        onView(withId(R.id.transfer_amount)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_amount_currency)).check(matches(withText("USD")));

        //Check that the toggle is disabled by default
        onView(withId(R.id.transfer_all_funds)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_all_funds)).check(matches(not(isSelected())));
        onView(withId(R.id.transfer_summary)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        String availableFundUSD = getAvailableFund("998.00", "USD");
        onView(withId(R.id.transfer_summary)).check(matches(withText(availableFundUSD)));

        onView(withId(R.id.transfer_action_button)).check(matches(isEnabled()));
    }

    @Test
    public void testTransferFunds_verifyAddDestinationDisplayedWhenUserHasNoExternalAccounts() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("ppc/prepaid_card_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_NO_CONTENT).withBody("").mock();

        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.add_transfer_destination)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.add_transfer_destination_icon)).check(matches(withText(R.string.add_text)));
        onView(withId(R.id.add_transfer_destination_title)).check(matches(withText(R.string.mobileAddTransferMethod)));

        onView(withId(R.id.transfer_summary)).check(matches(not(isDisplayed())));
        onView(withId(R.id.transfer_action_button)).perform(nestedScrollTo());
        onView(withId(R.id.transfer_action_button)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_action_button)).check(matches(isEnabled()));
    }

    @Test
    public void testTransferFunds_createTransferWithFX() throws InterruptedException {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("ppc/prepaid_card_response.json")).mock();
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
                assertThat("Token is incorrect", transition.getToken(), is("sts-token"));
                assertThat("To Status is incorrect", transition.getToStatus(), is("SCHEDULED"));
                assertThat("From Status is incorrect", transition.getFromStatus(), is("QUOTED"));
                assertThat("Transition is incorrect", transition.getTransition(), is("SCHEDULED"));
                assertThat("Created on is incorrect", transition.getCreatedOn(), is("2019-08-12T17:39:35"));

            }
        };

        LocalBroadcastManager.getInstance(mActivityTestRule.getActivity().getApplicationContext())
                .registerReceiver(br, new IntentFilter("ACTION_HYPERWALLET_TRANSFER_SCHEDULED"));

        onView(withId(R.id.add_transfer_destination)).check(matches(not(isDisplayed())));
        onView(withId(R.id.transfer_destination)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_destination_icon)).check(matches(withText(R.string.bank_account_font_icon)));
        onView(withId(R.id.transfer_destination_title)).check(matches(withText(R.string.bank_account)));
        onView(withId(R.id.transfer_destination_description_1)).check(matches(withText("Canada")));
        onView(withId(R.id.transfer_destination_description_2)).check(matches(withText("ending in 5121")));

        onView(withId(R.id.transfer_summary)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        String availableFundCAD =  getAvailableFund("1,157.40", "CAD");
        onView(withId(R.id.transfer_summary)).check(matches(withText(availableFundCAD)));

        onView(withId(R.id.transfer_amount)).perform(nestedScrollTo(), replaceText("150.00"));
        onView(withId(R.id.transfer_action_button)).perform(nestedScrollTo(), click());

        onView(withId(R.id.list_foreign_exchange)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.list_foreign_exchange)).check(new RecyclerViewCountAssertion(1));
        onView(withId(R.id.list_foreign_exchange)).check(
                matches(atPosition(0, hasDescendant(
                        allOf(withId(R.id.sell_label), withText(R.string.mobileFXsell))))));
        onView(withId(R.id.list_foreign_exchange)).check(
                matches(atPosition(0, hasDescendant(allOf(withId(R.id.sell_value), withText("117.87 USD"))))));
        onView(withId(R.id.list_foreign_exchange)).check(
                matches(atPosition(0,
                        hasDescendant(allOf(withId(R.id.buy_label), withText(R.string.mobileFXbuy))))));
        onView(withId(R.id.list_foreign_exchange)).check(
                matches(atPosition(0, hasDescendant(allOf(withId(R.id.buy_value), withText("152.20 CAD"))))));
        onView(withId(R.id.list_foreign_exchange)).check(
                matches(atPosition(0, hasDescendant(
                        allOf(withId(R.id.exchange_rate_label), withText(R.string.mobileFXRateLabel))))));
        onView(withId(R.id.list_foreign_exchange)).check(
                matches(atPosition(0,
                        hasDescendant(allOf(withId(R.id.exchange_rate_value), withText("1 USD = 1.291253 CAD"))))));
        onView(withId(R.id.amount_label)).check(matches(withText(R.string.mobileConfirmDetailsAmount)));
        onView(withId(R.id.amount_value)).check(matches(withText("152.20 CAD")));
        onView(withId(R.id.fee_label)).check(matches(withText(R.string.mobileConfirmDetailsFee)));
        onView(withId(R.id.fee_value)).check(matches(withText("2.20 CAD")));
        onView(withId(R.id.transfer_label)).check(matches(withText(R.string.mobileConfirmDetailsTotal)));
        onView(withId(R.id.transfer_value)).check(matches(withText("150.00 CAD")));
        onView(withId(R.id.exchange_rate_warning_container)).check(matches(not(isDisplayed())));
        onView(withId(R.id.exchange_rate_warning)).check(matches(not(isDisplayed())));
        onView(withId(R.id.notes_container)).check(matches(not(isDisplayed())));
        onView(withId(R.id.notes_value)).check(matches(not(isDisplayed())));

        onView(withId(R.id.transfer_confirm_button)).perform(nestedScrollTo(), click());

        gate.await(5, SECONDS);
        LocalBroadcastManager.getInstance(mActivityTestRule.getActivity().getApplicationContext()).unregisterReceiver(
                br);
        assertThat("Action is not broadcasted", gate.getCount(), is(0L));

        verifyTransferConfirmationDialog("Bank Account");
    }

    @Test
    public void testTransferFunds_createTransferWithNoFX() throws InterruptedException {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("ppc/prepaid_card_response.json")).mock();
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
                assertThat("Token is incorrect", transition.getToken(), is("sts-token"));
                assertThat("To Status is incorrect", transition.getToStatus(), is("SCHEDULED"));
                assertThat("From Status is incorrect", transition.getFromStatus(), is("QUOTED"));
                assertThat("Transition is incorrect", transition.getTransition(), is("SCHEDULED"));
                assertThat("Created on is incorrect", transition.getCreatedOn(), is("2019-08-12T17:39:35"));
            }
        };

        LocalBroadcastManager.getInstance(mActivityTestRule.getActivity().getApplicationContext())
                .registerReceiver(br, new IntentFilter("ACTION_HYPERWALLET_TRANSFER_SCHEDULED"));

        onView(withId(R.id.add_transfer_destination)).check(matches(not(isDisplayed())));
        onView(withId(R.id.transfer_destination)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_destination_icon)).check(matches(withText(R.string.bank_account_font_icon)));
        onView(withId(R.id.transfer_destination_title)).check(matches(withText(R.string.bank_account)));
        onView(withId(R.id.transfer_destination_description_1)).check(matches(withText("United States")));
        onView(withId(R.id.transfer_destination_description_2)).check(matches(withText("ending in 0616")));

        onView(withId(R.id.transfer_summary)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        String availableFundUSD = String.format(InstrumentationRegistry.getInstrumentation().getTargetContext()
                .getString(R.string.mobileAvailableBalance), "998.00" , "USD");
        onView(withId(R.id.transfer_summary)).check(matches(withText(availableFundUSD)));

        onView(withId(R.id.transfer_amount)).perform(nestedScrollTo(), replaceText("100.00"));
        onView(withId(R.id.transfer_action_button)).perform(nestedScrollTo(), click());

        onView(withId(R.id.list_foreign_exchange)).check(matches(not(isDisplayed())));
        onView(withId(R.id.amount_label)).check(matches(withText(R.string.mobileConfirmDetailsAmount)));
        onView(withId(R.id.amount_value)).check(matches(withText("102.00 USD")));
        onView(withId(R.id.fee_label)).check(matches(withText(R.string.mobileConfirmDetailsFee)));
        onView(withId(R.id.fee_value)).check(matches(withText("2.00 USD")));
        onView(withId(R.id.transfer_label)).check(matches(withText(R.string.mobileConfirmDetailsTotal)));
        onView(withId(R.id.transfer_value)).check(matches(withText("100.00 USD")));
        onView(withId(R.id.exchange_rate_warning_container)).check(matches(not(isDisplayed())));
        onView(withId(R.id.exchange_rate_warning)).check(matches(not(isDisplayed())));
        onView(withId(R.id.notes_container)).check(matches(not(isDisplayed())));
        onView(withId(R.id.notes_value)).check(matches(not(isDisplayed())));

        onView(withId(R.id.transfer_confirm_button)).perform(nestedScrollTo(), click());

        gate.await(5, SECONDS);
        LocalBroadcastManager.getInstance(mActivityTestRule.getActivity().getApplicationContext()).unregisterReceiver(
                br);
        assertThat("Action is not broadcasted", gate.getCount(), is(0L));

        verifyTransferConfirmationDialog("Bank Account");
    }

    @Test
    public void testTransferFunds_createTransferWithNotes() throws InterruptedException {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("ppc/prepaid_card_response.json")).mock();
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
                assertThat("Token is incorrect", transition.getToken(), is("sts-token"));
                assertThat("To Status is incorrect", transition.getToStatus(), is("SCHEDULED"));
                assertThat("From Status is incorrect", transition.getFromStatus(), is("QUOTED"));
                assertThat("Transition is incorrect", transition.getTransition(), is("SCHEDULED"));
                assertThat("Created on is incorrect", transition.getCreatedOn(), is("2019-08-12T17:39:35"));
            }
        };

        LocalBroadcastManager.getInstance(mActivityTestRule.getActivity().getApplicationContext())
                .registerReceiver(br, new IntentFilter("ACTION_HYPERWALLET_TRANSFER_SCHEDULED"));

        onView(withId(R.id.add_transfer_destination)).check(matches(not(isDisplayed())));
        onView(withId(R.id.transfer_destination)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_destination_icon)).check(matches(withText(R.string.bank_account_font_icon)));
        onView(withId(R.id.transfer_destination_title)).check(matches(withText(R.string.bank_account)));
        onView(withId(R.id.transfer_destination_description_1)).check(matches(withText("United States")));
        onView(withId(R.id.transfer_destination_description_2)).check(matches(withText("ending in 0616")));

        onView(withId(R.id.transfer_summary)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        String availableFundUSD = getAvailableFund("998.00", "USD");
        onView(withId(R.id.transfer_summary)).check(matches(withText(availableFundUSD)));

        onView(withId(R.id.transfer_amount)).perform(nestedScrollTo(), replaceText("100.00"));
        onView(withId(R.id.transfer_notes)).perform(nestedScrollTo(), replaceText("QA Automation Test"));

        onView(withId(R.id.transfer_action_button)).perform(nestedScrollTo(), click());

        onView(withId(R.id.list_foreign_exchange)).check(matches(not(isDisplayed())));
        onView(withId(R.id.amount_label)).check(matches(withText(R.string.mobileConfirmDetailsAmount)));
        onView(withId(R.id.amount_value)).check(matches(withText("102.00 USD")));
        onView(withId(R.id.fee_label)).check(matches(withText(R.string.mobileConfirmDetailsFee)));
        onView(withId(R.id.fee_value)).check(matches(withText("2.00 USD")));
        onView(withId(R.id.transfer_label)).check(matches(withText(R.string.mobileConfirmDetailsTotal)));
        onView(withId(R.id.transfer_value)).check(matches(withText("100.00 USD")));
        onView(withId(R.id.exchange_rate_warning_container)).check(matches(not(isDisplayed())));
        onView(withId(R.id.exchange_rate_warning)).check(matches(not(isDisplayed())));
        onView(withId(R.id.notes_container)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.notes_value)).check(matches(withText("Transfer funds test")));

        onView(withId(R.id.transfer_confirm_button)).perform(nestedScrollTo(), click());
//
//        assertThat("Result code is incorrect",
//                mActivityTestRule.getActivityResult().getResultCode(), is(Activity.RESULT_OK));

        gate.await(5, SECONDS);
        LocalBroadcastManager.getInstance(mActivityTestRule.getActivity().getApplicationContext()).unregisterReceiver(
                br);
        assertThat("Action is not broadcasted", gate.getCount(), is(0L));

        verifyTransferConfirmationDialog("Bank Account");
    }

    @Test
    public void testTransferFunds_createTransferWithEmptyFees() throws InterruptedException {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("ppc/prepaid_card_response.json")).mock();
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
                assertThat("Token is incorrect", transition.getToken(), is("sts-token"));
                assertThat("To Status is incorrect", transition.getToStatus(), is("SCHEDULED"));
                assertThat("From Status is incorrect", transition.getFromStatus(), is("QUOTED"));
                assertThat("Transition is incorrect", transition.getTransition(), is("SCHEDULED"));
                assertThat("Created on is incorrect", transition.getCreatedOn(), is("2019-08-12T17:39:35"));
            }
        };

        LocalBroadcastManager.getInstance(mActivityTestRule.getActivity().getApplicationContext())
                .registerReceiver(br, new IntentFilter("ACTION_HYPERWALLET_TRANSFER_SCHEDULED"));

        onView(withId(R.id.add_transfer_destination)).check(matches(not(isDisplayed())));
        onView(withId(R.id.transfer_destination)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_destination_icon)).check(matches(withText(R.string.bank_account_font_icon)));
        onView(withId(R.id.transfer_destination_title)).check(matches(withText(R.string.bank_account)));
        onView(withId(R.id.transfer_destination_description_1)).check(matches(withText("United States")));
        onView(withId(R.id.transfer_destination_description_2)).check(matches(withText("ending in 0616")));

        onView(withId(R.id.transfer_summary)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        String availableFundUSD = getAvailableFund("998.00","USD");
        onView(withId(R.id.transfer_summary)).check(matches(withText(availableFundUSD)));

        onView(withId(R.id.transfer_amount)).perform(nestedScrollTo(), replaceText("100.00"));
        onView(withId(R.id.transfer_notes)).perform(nestedScrollTo(), replaceText("QA Automation Test"));

        onView(withId(R.id.transfer_action_button)).perform(nestedScrollTo(), click());

        onView(withId(R.id.list_foreign_exchange)).check(matches(not(isDisplayed())));
        onView(withId(R.id.amount_label)).check(matches(withText(R.string.mobileConfirmDetailsAmount)));
        onView(withId(R.id.amount_value)).check(matches(withText("100.00 USD")));
        onView(withId(R.id.fee_label)).check(matches(not(isDisplayed())));
        onView(withId(R.id.fee_value)).check(matches(not(isDisplayed())));
        onView(withId(R.id.transfer_label)).check(matches(not(isDisplayed())));
        onView(withId(R.id.transfer_value)).check(matches(not(isDisplayed())));
        onView(withId(R.id.exchange_rate_warning_container)).check(matches(not(isDisplayed())));
        onView(withId(R.id.exchange_rate_warning)).check(matches(not(isDisplayed())));
        onView(withId(R.id.notes_container)).check(matches(not(isDisplayed())));
        onView(withId(R.id.notes_value)).check(matches(not(isDisplayed())));

        onView(withId(R.id.transfer_confirm_button)).perform(nestedScrollTo(), click());

        gate.await(5, SECONDS);
        LocalBroadcastManager.getInstance(mActivityTestRule.getActivity().getApplicationContext()).unregisterReceiver(
                br);
        assertThat("Action is not broadcasted", gate.getCount(), is(0L));

        verifyTransferConfirmationDialog("Bank Account");
    }

    @Test
    public void testTransferFunds_createTransferWithAllFunds() throws InterruptedException {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("ppc/prepaid_card_response.json")).mock();
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
                assertThat("Token is incorrect", transition.getToken(), is("sts-token"));
                assertThat("To Status is incorrect", transition.getToStatus(), is("SCHEDULED"));
                assertThat("From Status is incorrect", transition.getFromStatus(), is("QUOTED"));
                assertThat("Transition is incorrect", transition.getTransition(), is("SCHEDULED"));
                assertThat("Created on is incorrect", transition.getCreatedOn(), is("2019-08-12T17:39:35"));
            }
        };

        LocalBroadcastManager.getInstance(mActivityTestRule.getActivity().getApplicationContext())
                .registerReceiver(br, new IntentFilter("ACTION_HYPERWALLET_TRANSFER_SCHEDULED"));

        onView(withId(R.id.add_transfer_destination)).check(matches(not(isDisplayed())));
        onView(withId(R.id.transfer_destination)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_destination_icon)).check(matches(withText(R.string.bank_account_font_icon)));
        onView(withId(R.id.transfer_destination_title)).check(matches(withText(R.string.bank_account)));
        onView(withId(R.id.transfer_destination_description_1)).check(matches(withText("United States")));
        onView(withId(R.id.transfer_destination_description_2)).check(matches(withText("ending in 0616")));

        onView(withId(R.id.transfer_all_funds)).perform(nestedScrollTo(), click());
        onView(withId(R.id.transfer_amount)).check(matches(withText("998.00")));
        onView(withId(R.id.transfer_notes)).perform(nestedScrollTo(), replaceText("Transfer all funds test"));

        onView(withId(R.id.transfer_action_button)).perform(nestedScrollTo(), click());

        onView(withId(R.id.list_foreign_exchange)).check(matches(not(isDisplayed())));

        onView(withId(R.id.amount_label)).check(matches(withText(R.string.mobileConfirmDetailsAmount)));
        onView(withId(R.id.amount_value)).check(matches(withText("1,000.00 USD")));
        onView(withId(R.id.fee_label)).check(matches(withText(R.string.mobileConfirmDetailsFee)));
        onView(withId(R.id.fee_value)).check(matches(withText("2.00 USD")));
        onView(withId(R.id.transfer_label)).check(matches(withText(R.string.mobileConfirmDetailsTotal)));
        onView(withId(R.id.transfer_value)).check(matches(withText("998.00 USD")));
        onView(withId(R.id.exchange_rate_warning_container)).check(matches(not(isDisplayed())));
        onView(withId(R.id.exchange_rate_warning)).check(matches(not(isDisplayed())));
        onView(withId(R.id.notes_container)).perform(nestedScrollTo());
        onView(withId(R.id.notes_container)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.notes_value)).check(matches(withText("Transfer funds test")));

        onView(withId(R.id.transfer_confirm_button)).perform(nestedScrollTo(), click());

        gate.await(5, SECONDS);
        LocalBroadcastManager.getInstance(mActivityTestRule.getActivity().getApplicationContext()).unregisterReceiver(
                br);
        assertThat("Action is not broadcasted", gate.getCount(), is(0L));

        verifyTransferConfirmationDialog("Bank Account");
    }

    @Test
    public void testTransferFunds_createTransferAmountNotSetError() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("ppc/prepaid_card_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("transfer_method_list_single_bank_account_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("ppc/create_transfer_quote_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.transfer_action_button)).perform(nestedScrollTo(), click());
        onView(withId(R.id.transfer_amount_error)).check(matches(withText(R.string.transferAmountInvalid)));
    }

    @Test
    public void testTransferFunds_createTransferInvalidAmountError() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("ppc/prepaid_card_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("transfer_method_list_single_bank_account_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("ppc/create_transfer_quote_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_BAD_REQUEST).withBody(sResourceManager
                .getResourceContent("errors/create_transfer_error_invalid_amount_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.transfer_amount)).perform(nestedScrollTo(), replaceText("100.00"));
        onView(withId(R.id.transfer_action_button)).perform(nestedScrollTo(), click());

        //onView(withId(R.id.transfer_amount_layout)).check(matches(hasErrorText("Invalid amount.")));
        onView(withId(R.id.transfer_amount_error)).check(matches(withText("Invalid amount.")));
    }

    @Test
    public void testTransferFunds_createTransferDestinationNotSetError() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("ppc/prepaid_card_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_NO_CONTENT).withBody("").mock();

        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.transfer_amount)).perform(nestedScrollTo(), replaceText("100.00"));
        onView(withId(R.id.transfer_action_button)).perform(nestedScrollTo(), click());

        onView(withId(R.id.transfer_destination_error)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_destination_error)).check(
                matches(withText(R.string.noTransferMethodAdded)));
    }

    @Test
    public void testTransferFunds_createTransferLimitError() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("ppc/prepaid_card_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("transfer_method_list_single_bank_account_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("ppc/create_transfer_quote_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_BAD_REQUEST).withBody(sResourceManager
                .getResourceContent("errors/create_transfer_error_limit_exceeded_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.transfer_amount)).perform(nestedScrollTo(), replaceText("100000.00"));
        onView(withId(R.id.transfer_action_button)).perform(nestedScrollTo(), click());

        onView(withId(R.id.alertTitle)).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withId(R.id.alertTitle)).check(matches(withText(R.string.error_dialog_title)));
        onView(withText(
                "Your attempted transaction has exceeded the approved payout limit; please contact HyperWallet Pay "
                        + "for further assistance."))
                .inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withId(android.R.id.button1)).check(matches(withText(R.string.ok)));
        onView(withId(android.R.id.button1)).perform(click());
    }

    @Test
    public void testTransferFunds_createTransferInsufficientFundsError() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("ppc/prepaid_card_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("transfer_method_list_single_bank_account_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("ppc/create_transfer_quote_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_BAD_REQUEST).withBody(sResourceManager
                .getResourceContent("errors/create_transfer_error_insufficient_funds_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.transfer_amount)).perform(nestedScrollTo(), replaceText("5000.00"));
        onView(withId(R.id.transfer_action_button)).perform(nestedScrollTo(), click());

        onView(withId(R.id.alertTitle)).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withId(R.id.alertTitle)).check(matches(withText(R.string.error_dialog_title)));
        onView(withText("You do not have enough funds in any single currency to complete this transfer."))
                .inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withId(android.R.id.button1)).check(matches(withText(R.string.ok)));
        onView(withId(android.R.id.button1)).perform(click());
    }

    @Test
    public void testTransferFunds_createTransferMinimumAmountError() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("ppc/prepaid_card_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("transfer_method_list_single_bank_account_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("ppc/create_transfer_quote_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_BAD_REQUEST).withBody(sResourceManager
                .getResourceContent("errors/create_transfer_error_limit_subceeded_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.transfer_amount)).perform(nestedScrollTo(), replaceText("5000.00"));
        onView(withId(R.id.transfer_action_button)).perform(nestedScrollTo(), click());

        onView(withId(R.id.alertTitle)).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withId(R.id.alertTitle)).check(matches(withText(R.string.error_dialog_title)));
        onView(withText("Requested transfer amount $0.01, is below the transaction limit of $1.00."))
                .inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withId(android.R.id.button1)).check(matches(withText(R.string.ok)));
        onView(withId(android.R.id.button1)).perform(click());

    }

    @Test
    public void testTransferFunds_createTransferInvalidSourceError() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("ppc/prepaid_card_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("transfer_method_list_single_bank_account_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("ppc/create_transfer_quote_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_BAD_REQUEST).withBody(sResourceManager
                .getResourceContent("errors/create_transfer_error_invalid_wallet_status_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.transfer_amount)).perform(nestedScrollTo(), replaceText("100.00"));
        onView(withId(R.id.transfer_action_button)).perform(nestedScrollTo(), click());

        onView(withId(R.id.alertTitle)).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withId(R.id.alertTitle)).check(matches(withText(R.string.error_dialog_title)));
        onView(withText("The account status does not allow the requested action."))
                .inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withId(android.R.id.button1)).check(matches(withText(R.string.ok)));
        onView(withId(android.R.id.button1)).perform(click());
    }

    @Test
    public void testTransferFunds_createTransferConnectionError() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("ppc/prepaid_card_response.json")).mock();
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

        onView(withId(R.id.transfer_amount)).perform(nestedScrollTo(), replaceText("100.00"));
        onView(withId(R.id.transfer_action_button)).perform(nestedScrollTo(), click());

        onView(withText(R.string.error_dialog_connectivity_title)).check(matches(isDisplayed()));
        onView(withText(R.string.io_exception)).check(matches(isDisplayed()));
        onView(withId(android.R.id.button1)).check(matches(withText(R.string.try_again_button_label)));
        onView(withId(android.R.id.button2)).check(matches(withText(R.string.cancelButtonLabel)));

        onView(withId(android.R.id.button1)).perform(click());
        onView(withText(R.string.error_dialog_connectivity_title)).check(doesNotExist());

        onView(withId(R.id.amount_label)).check(matches(withText(R.string.mobileConfirmDetailsAmount)));
        onView(withId(R.id.amount_value)).check(matches(withText("102.00 USD")));
        onView(withId(R.id.fee_label)).check(matches(withText(R.string.mobileConfirmDetailsFee)));
        onView(withId(R.id.fee_value)).check(matches(withText("2.00 USD")));
        onView(withId(R.id.transfer_label)).check(matches(withText(R.string.mobileConfirmDetailsTotal)));
        onView(withId(R.id.transfer_value)).check(matches(withText("100.00 USD")));
    }

    @Test
    public void testTransferFunds_createTransferConfirmationConnectionError() throws InterruptedException {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("ppc/prepaid_card_response.json")).mock();
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
                assertThat("Token is incorrect", transition.getToken(), is("sts-token"));
                assertThat("To Status is incorrect", transition.getToStatus(), is("SCHEDULED"));
                assertThat("From Status is incorrect", transition.getFromStatus(), is("QUOTED"));
                assertThat("Transition is incorrect", transition.getTransition(), is("SCHEDULED"));
                assertThat("Created on is incorrect", transition.getCreatedOn(), is("2019-08-12T17:39:35"));
            }
        };

        LocalBroadcastManager.getInstance(mActivityTestRule.getActivity().getApplicationContext())
                .registerReceiver(br, new IntentFilter("ACTION_HYPERWALLET_TRANSFER_SCHEDULED"));

        onView(withId(R.id.transfer_amount)).perform(nestedScrollTo(), replaceText("100.00"));
        onView(withId(R.id.transfer_action_button)).perform(nestedScrollTo(), click());

        onView(withId(R.id.amount_label)).check(matches(withText(R.string.mobileConfirmDetailsAmount)));
        onView(withId(R.id.amount_value)).check(matches(withText("102.00 USD")));
        onView(withId(R.id.fee_label)).check(matches(withText(R.string.mobileConfirmDetailsFee)));
        onView(withId(R.id.fee_value)).check(matches(withText("2.00 USD")));
        onView(withId(R.id.transfer_label)).check(matches(withText(R.string.mobileConfirmDetailsTotal)));
        onView(withId(R.id.transfer_value)).check(matches(withText("100.00 USD")));
        onView(withId(R.id.notes_container)).check(matches(not(isDisplayed())));
        onView(withId(R.id.notes_value)).check(matches(not(isDisplayed())));

        onView(withId(R.id.transfer_confirm_button)).perform(nestedScrollTo(), click());

        onView(withText(R.string.error_dialog_connectivity_title)).check(matches(isDisplayed()));
        onView(withText(R.string.io_exception)).check(matches(isDisplayed()));
        onView(withId(android.R.id.button1)).check(matches(withText(R.string.try_again_button_label)));
        onView(withId(android.R.id.button2)).check(matches(withText(R.string.cancelButtonLabel)));

        // When click on Try again
        onView(withId(android.R.id.button1)).perform(click());

        gate.await(5, SECONDS);
        LocalBroadcastManager.getInstance(mActivityTestRule.getActivity().getApplicationContext()).unregisterReceiver(
                br);
        assertThat("Action is not broadcasted", gate.getCount(), is(0L));

        // Then assert the Success Dialog
        verifyTransferConfirmationDialog("Bank Account");
    }

    private void verifyTransferConfirmationDialog(String transferType) {
        // Your transfer is being processed
        onView(withText(R.string.mobileTransferSuccessMsg))
                .inRoot(RootMatchers.isDialog())
                .check(matches(ViewMatchers.isDisplayed()));
        // The funds are on the way to your Bank Account
        String detail = String.format(InstrumentationRegistry.getInstrumentation().getTargetContext()
                .getString(R.string.mobileTransferSuccessDetails),transferType);
        onView(withText(detail))
                .inRoot(RootMatchers.isDialog());
        // Done button
        onView(ViewMatchers.withId(android.R.id.button1))
                .check(matches(withText(R.string.doneButtonLabel)));
        onView(ViewMatchers.withId(android.R.id.button1)).perform(click());
    }

    private String getAvailableFund(String amount, String currency) {
        String availableFund = String.format(InstrumentationRegistry.getInstrumentation().getTargetContext()
                .getString(R.string.mobileAvailableBalance), amount , currency);
        return availableFund;
    }
}
