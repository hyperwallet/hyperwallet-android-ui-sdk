package com.hyperwallet.android.ui.transfer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isSelected;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;

import static java.net.HttpURLConnection.HTTP_OK;

import static com.hyperwallet.android.ui.common.intent.HyperwalletIntent.ACTION_SELECT_TRANSFER_METHOD;
import static com.hyperwallet.android.ui.testutils.util.EspressoUtils.atPosition;
import static com.hyperwallet.android.ui.testutils.util.EspressoUtils.nestedScrollTo;

import android.app.Activity;
import android.app.Instrumentation;
import android.widget.TextView;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import com.hyperwallet.android.ui.common.repository.EspressoIdlingResource;
import com.hyperwallet.android.ui.testutils.rule.HyperwalletExternalResourceManager;
import com.hyperwallet.android.ui.testutils.rule.HyperwalletMockWebServer;
import com.hyperwallet.android.ui.testutils.rule.HyperwalletSdkRule;
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

import java.util.concurrent.TimeUnit;

import okhttp3.mockwebserver.MockResponse;

@RunWith(AndroidJUnit4.class)
public class SelectDestinationTest {

    @ClassRule
    public static HyperwalletExternalResourceManager sResourceManager = new HyperwalletExternalResourceManager();
    @Rule
    public HyperwalletSdkRule mHyperwalletSdkRule = new HyperwalletSdkRule();
    @Rule
    public HyperwalletMockWebServer mMockWebServer = new HyperwalletMockWebServer(8080);
    @Rule
    public ActivityTestRule<CreateTransferActivity> mActivityTestRule =
            new ActivityTestRule<>(CreateTransferActivity.class, true, false);
    @Rule
    public IntentsTestRule<CreateTransferActivity> mIntentsTestRule =
            new IntentsTestRule<>(CreateTransferActivity.class, true, false);

    @Before
    public void setup() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("authentication_token_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("user_response.json")).mock();
        IdlingRegistry.getInstance().register(EspressoIdlingResource.getIdlingResource());
    }

    @After
    public void cleanup() {
        UserRepositoryFactory.clearInstance();
        TransferRepositoryFactory.clearInstance();
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.getIdlingResource());
    }

    @Test
    public void testSelectDestination_verifyActiveExternalAccountsDisplayed() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("transfer_method_list_single_bank_account_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("create_transfer_quote_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("transfer_method_list_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.add_transfer_destination)).check(matches(not(isDisplayed())));
        onView(withId(R.id.transfer_destination)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_destination_icon)).check(matches(withText(R.string.bank_account_font_icon)));
        onView(withId(R.id.transfer_destination_title)).check(matches(withText(R.string.bank_account)));
        onView(withId(R.id.transfer_destination_selection)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_destination_description_1)).check(matches(withText("United States")));
        onView(withId(R.id.transfer_destination_description_2)).check(matches(withText("Ending on 0616")));

        onView(withId(R.id.transfer_destination_title)).perform(click());

        onView(allOf(instanceOf(TextView.class),
                withParent(withId(R.id.transfer_destination_selection_toolbar)))).check(
                matches(withText(R.string.transfer_destination)));

        onView(withId(R.id.transfer_destination_list)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.bank_account_font_icon)))));
        onView(withId(R.id.transfer_destination_list)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.bank_account)))));
        onView(withId(R.id.transfer_destination_list)).check(
                matches(atPosition(0, hasDescendant(withText("United States")))));
        onView(withId(R.id.transfer_destination_list)).check(
                matches(atPosition(0, hasDescendant(withText("Ending on 1332")))));

        onView(withId(R.id.transfer_destination_list)).check(
                matches(atPosition(1, hasDescendant(withText(R.string.bank_card_font_icon)))));
        onView(withId(R.id.transfer_destination_list)).check(
                matches(atPosition(1, hasDescendant(withText(R.string.bank_card)))));
        onView(withId(R.id.transfer_destination_list)).check(
                matches(atPosition(1, hasDescendant(withText("United States")))));
        onView(withId(R.id.transfer_destination_list)).check(
                matches(atPosition(1, hasDescendant(withText("Ending on 0006")))));

        onView(withId(R.id.transfer_destination_list)).check(
                matches(atPosition(2, hasDescendant(withText(R.string.wire_account_font_icon)))));
        onView(withId(R.id.transfer_destination_list)).check(
                matches(atPosition(2, hasDescendant(withText(R.string.wire_account)))));
        onView(withId(R.id.transfer_destination_list)).check(
                matches(atPosition(2, hasDescendant(withText("United States")))));
        onView(withId(R.id.transfer_destination_list)).check(
                matches(atPosition(2, hasDescendant(withText("Ending on 8337")))));

        onView(withId(R.id.transfer_destination_list)).check(
                matches(atPosition(3, hasDescendant(withText(R.string.paper_check_font_icon)))));
        onView(withId(R.id.transfer_destination_list)).check(
                matches(atPosition(3, hasDescendant(withText(R.string.paper_check)))));
        onView(withId(R.id.transfer_destination_list)).check(matches(atPosition(3, hasDescendant(withText("Canada")))));
        onView(withId(R.id.transfer_destination_list)).check(
                matches(atPosition(3, hasDescendant(withText("")))));

        onView(withId(R.id.transfer_destination_list)).check(
                matches(atPosition(4, hasDescendant(withText(R.string.prepaid_card_font_icon)))));
        onView(withId(R.id.transfer_destination_list)).check(
                matches(atPosition(4, hasDescendant(withText(R.string.prepaid_card)))));
        onView(withId(R.id.transfer_destination_list)).check(matches(atPosition(4, hasDescendant(withText("Canada")))));
        onView(withId(R.id.transfer_destination_list)).check(
                matches(atPosition(4, hasDescendant(withText("Ending on 3187")))));

        onView(withId(R.id.transfer_destination_list)).check(
                matches(atPosition(5, hasDescendant(withText(R.string.paypal_account_font_icon)))));
        onView(withId(R.id.transfer_destination_list)).check(
                matches(atPosition(5, hasDescendant(withText(R.string.paypal_account)))));
        onView(withId(R.id.transfer_destination_list)).check(
                matches(atPosition(5, hasDescendant(withText("United States")))));
        onView(withId(R.id.transfer_destination_list)).check(
                matches(atPosition(5, hasDescendant(withText("honey.thigpen@ukbuilder.com")))));

        onView(withId(R.id.transfer_destination_list)).check(new RecyclerViewCountAssertion(6));

    }

    @Test
    public void testSelectDestination_verifyDestinationNotUpdatedWhenClickingBackButton() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("transfer_method_list_single_bank_account_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("create_transfer_quote_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("transfer_method_list_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.add_transfer_destination)).check(matches(not(isDisplayed())));
        onView(withId(R.id.transfer_destination)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_destination_icon)).check(matches(withText(R.string.bank_account_font_icon)));
        onView(withId(R.id.transfer_destination_title)).check(matches(withText(R.string.bank_account)));
        onView(withId(R.id.transfer_destination_selection)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_destination_description_1)).check(matches(withText("United States")));
        onView(withId(R.id.transfer_destination_description_2)).check(matches(withText("Ending on 0616")));

        onView(withId(R.id.transfer_summary)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_summary)).check(matches(withText("Available for Transfer: 998.00 USD")));

        onView(withId(R.id.transfer_destination_title)).perform(click());

        onView(allOf(instanceOf(TextView.class),
                withParent(withId(R.id.transfer_destination_selection_toolbar)))).check(
                matches(withText(R.string.transfer_destination)));

        onView(withContentDescription(R.string.abc_action_bar_up_description)).perform(click());

        onView(withId(R.id.add_transfer_destination)).check(matches(not(isDisplayed())));
        onView(withId(R.id.transfer_destination)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_destination_icon)).check(matches(withText(R.string.bank_account_font_icon)));
        onView(withId(R.id.transfer_destination_title)).check(matches(withText(R.string.bank_account)));
        onView(withId(R.id.transfer_destination_selection)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_destination_description_1)).check(matches(withText("United States")));
        onView(withId(R.id.transfer_destination_description_2)).check(matches(withText("Ending on 0616")));

        onView(withId(R.id.transfer_summary)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_summary)).check(matches(withText("Available for Transfer: 998.00 USD")));
    }

    @Test
    public void testSelectDestination_verifyDestinationUpdatedUponAddingNewExternalAccount() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("transfer_method_list_single_bank_account_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("create_transfer_quote_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("transfer_method_list_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("transfer_method_list_added_bank_account_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("create_transfer_quote_fx_response.json")).mock();

        mIntentsTestRule.launchActivity(null);

        Instrumentation.ActivityResult result =
                new Instrumentation.ActivityResult(Activity.RESULT_OK, null);
        intending(hasAction(ACTION_SELECT_TRANSFER_METHOD)).respondWith(result);

        onView(withId(R.id.transfer_destination_title)).perform(click());
        onView(allOf(instanceOf(TextView.class),
                withParent(withId(R.id.transfer_destination_selection_toolbar)))).check(
                matches(withText(R.string.transfer_destination)));
        onView(withId(R.id.create_transfer_method_fab)).perform(click());
        intended(hasAction(ACTION_SELECT_TRANSFER_METHOD));

        onView(withId(R.id.add_transfer_destination)).check(matches(not(isDisplayed())));
        onView(withId(R.id.transfer_destination)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_destination_icon)).check(matches(withText(R.string.bank_account_font_icon)));
        onView(withId(R.id.transfer_destination_title)).check(matches(withText(R.string.bank_account)));
        onView(withId(R.id.transfer_destination_selection)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_destination_description_1)).check(matches(withText("Canada")));
        onView(withId(R.id.transfer_destination_description_2)).check(matches(withText("Ending on 5121")));

        onView(withId(R.id.transfer_summary)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_summary)).check(matches(withText("Available for Transfer: 1,157.40 CAD")));

    }

    @Test
    public void testSelectDestination_verifyDestinationUpdatedUponSelection() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("transfer_method_list_single_bank_account_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("create_transfer_quote_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("transfer_method_list_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("create_transfer_quote_paypal_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.add_transfer_destination)).check(matches(not(isDisplayed())));
        onView(withId(R.id.transfer_destination)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_destination_icon)).check(matches(withText(R.string.bank_account_font_icon)));
        onView(withId(R.id.transfer_destination_title)).check(matches(withText(R.string.bank_account)));
        onView(withId(R.id.transfer_destination_selection)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_destination_description_1)).check(matches(withText("United States")));
        onView(withId(R.id.transfer_destination_description_2)).check(matches(withText("Ending on 0616")));

        onView(withId(R.id.transfer_summary)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_summary)).check(matches(withText("Available for Transfer: 998.00 USD")));

        onView(withId(R.id.transfer_destination_title)).perform(click());

        onView(withId(R.id.transfer_destination_list)).perform(RecyclerViewActions.actionOnItemAtPosition(5, click()));
        onView(withId(R.id.transfer_destination)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_destination_icon)).check(matches(withText(R.string.paypal_account_font_icon)));
        onView(withId(R.id.transfer_destination_title)).check(matches(withText(R.string.paypal_account)));
        onView(withId(R.id.transfer_destination_selection)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_destination_description_1)).check(matches(withText("United States")));
        onView(withId(R.id.transfer_destination_description_2)).check(matches(withText("honey.thigpen@ukbuilder.com")));

        onView(withId(R.id.transfer_amount)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_amount)).check(matches(withText("")));
        onView(withId(R.id.transfer_amount_currency)).check(matches(withText("USD")));

        //Check that the toggle is disabled by default
        onView(withId(R.id.switchButton)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.switchButton)).check(matches(not(isSelected())));
        onView(withId(R.id.transfer_summary)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_summary)).check(matches(withText("Available for Transfer: 1000.00 USD")));

        onView(withId(R.id.transfer_notes)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_notes)).check(matches(withText("")));
    }

    @Test
    public void testSelectDestination_listTransferMethodsConnectionError() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("transfer_method_list_single_bank_account_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("create_transfer_quote_response.json")).mock();
        mMockWebServer.getServer().enqueue(new MockResponse().setResponseCode(HTTP_OK).setBody(sResourceManager
                .getResourceContent("transfer_method_list_response.json")).setBodyDelay(10500, TimeUnit
                .MILLISECONDS));
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("transfer_method_list_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.add_transfer_destination)).check(matches(not(isDisplayed())));
        onView(withId(R.id.transfer_destination)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_destination_icon)).check(matches(withText(R.string.bank_account_font_icon)));
        onView(withId(R.id.transfer_destination_title)).check(matches(withText(R.string.bank_account)));
        onView(withId(R.id.transfer_destination_selection)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_destination_description_1)).check(matches(withText("United States")));
        onView(withId(R.id.transfer_destination_description_2)).check(matches(withText("Ending on 0616")));

        onView(withId(R.id.transfer_destination_title)).perform(click());

        onView(withText(R.string.error_dialog_connectivity_title)).check(matches(isDisplayed()));
        onView(withText(R.string.io_exception)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(android.R.id.button1)).check(matches(withText(R.string.try_again_button_label)));
        onView(withId(android.R.id.button2)).check(matches(withText(R.string.cancel_button_label)));

        onView(withId(android.R.id.button1)).perform(click());
        onView(withText(R.string.error_dialog_connectivity_title)).check(doesNotExist());

        onView(allOf(instanceOf(TextView.class),
                withParent(withId(R.id.transfer_destination_selection_toolbar)))).check(
                matches(withText(R.string.transfer_destination)));

        onView(withId(R.id.transfer_destination_list)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.bank_account_font_icon)))));
        onView(withId(R.id.transfer_destination_list)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.bank_account)))));
        onView(withId(R.id.transfer_destination_list)).check(
                matches(atPosition(0, hasDescendant(withText("United States")))));
        onView(withId(R.id.transfer_destination_list)).check(
                matches(atPosition(0, hasDescendant(withText("Ending on 1332")))));

        onView(withId(R.id.transfer_destination_list)).check(
                matches(atPosition(1, hasDescendant(withText(R.string.bank_card_font_icon)))));
        onView(withId(R.id.transfer_destination_list)).check(
                matches(atPosition(1, hasDescendant(withText(R.string.bank_card)))));
        onView(withId(R.id.transfer_destination_list)).check(
                matches(atPosition(1, hasDescendant(withText("United States")))));
        onView(withId(R.id.transfer_destination_list)).check(
                matches(atPosition(1, hasDescendant(withText("Ending on 0006")))));

        onView(withId(R.id.transfer_destination_list)).check(
                matches(atPosition(2, hasDescendant(withText(R.string.wire_account_font_icon)))));
        onView(withId(R.id.transfer_destination_list)).check(
                matches(atPosition(2, hasDescendant(withText(R.string.wire_account)))));
        onView(withId(R.id.transfer_destination_list)).check(
                matches(atPosition(2, hasDescendant(withText("United States")))));
        onView(withId(R.id.transfer_destination_list)).check(
                matches(atPosition(2, hasDescendant(withText("Ending on 8337")))));

        onView(withId(R.id.transfer_destination_list)).check(
                matches(atPosition(3, hasDescendant(withText(R.string.paper_check_font_icon)))));
        onView(withId(R.id.transfer_destination_list)).check(
                matches(atPosition(3, hasDescendant(withText(R.string.paper_check)))));
        onView(withId(R.id.transfer_destination_list)).check(matches(atPosition(3, hasDescendant(withText("Canada")))));
        onView(withId(R.id.transfer_destination_list)).check(
                matches(atPosition(3, hasDescendant(withText("")))));

        onView(withId(R.id.transfer_destination_list)).check(
                matches(atPosition(4, hasDescendant(withText(R.string.prepaid_card_font_icon)))));
        onView(withId(R.id.transfer_destination_list)).check(
                matches(atPosition(4, hasDescendant(withText(R.string.prepaid_card)))));
        onView(withId(R.id.transfer_destination_list)).check(matches(atPosition(4, hasDescendant(withText("Canada")))));
        onView(withId(R.id.transfer_destination_list)).check(
                matches(atPosition(4, hasDescendant(withText("Ending on 3187")))));

        onView(withId(R.id.transfer_destination_list)).check(
                matches(atPosition(5, hasDescendant(withText(R.string.paypal_account_font_icon)))));
        onView(withId(R.id.transfer_destination_list)).check(
                matches(atPosition(5, hasDescendant(withText(R.string.paypal_account)))));
        onView(withId(R.id.transfer_destination_list)).check(
                matches(atPosition(5, hasDescendant(withText("United States")))));
        onView(withId(R.id.transfer_destination_list)).check(
                matches(atPosition(5, hasDescendant(withText("honey.thigpen@ukbuilder.com")))));

        onView(withId(R.id.transfer_destination_list)).check(new RecyclerViewCountAssertion(6));

    }

}
