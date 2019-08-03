package com.hyperwallet.android.ui.transfer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isSelected;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;

import static java.net.HttpURLConnection.HTTP_OK;

import static com.hyperwallet.android.ui.testutils.util.EspressoUtils.atPosition;

import android.widget.TextView;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import com.hyperwallet.android.Hyperwallet;
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

@RunWith(AndroidJUnit4.class)
public class SelectDestinationTest {

    @ClassRule
    public static HyperwalletExternalResourceManager sResourceManager = new HyperwalletExternalResourceManager();
    @Rule
    public HyperwalletMockWebServer mMockWebServer = new HyperwalletMockWebServer(8080);
    @Rule
    public ActivityTestRule<CreateTransferActivity> mActivityTestRule =
            new ActivityTestRule<>(CreateTransferActivity.class, true, false);

    @Before
    public void setup() {
        Hyperwallet.getInstance(new TestAuthenticationProvider());

        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("authentication_token_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("user_response.json")).mock();
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
    public void testSelectDestination_verifyActiveExternalAccountsDisplayed() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("transfer_method_list_single_bank_account_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("create_transfer_quote_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("transfer_method_list_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.add_transfer_destination)).check(matches(not(isDisplayed())));
        onView(withId(R.id.transfer_destination)).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_destination_icon)).check(matches(withText(R.string.bank_account_font_icon)));
        onView(withId(R.id.transfer_destination_title)).check(matches(withText(R.string.bank_account)));
        onView(withId(R.id.transfer_destination_selection)).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_destination_description_1)).check(matches(withText("United States")));
        onView(withId(R.id.transfer_destination_description_2)).check(matches(withText("Ending on 7267")));

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
    }

    @Test
    public void testSelectDestination_verifyDestinationUpdatedUponAddingNewExternalAccount() {
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
                .getResourceContent("create_transfer_paypal_quote_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.add_transfer_destination)).check(matches(not(isDisplayed())));
        onView(withId(R.id.transfer_destination)).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_destination_icon)).check(matches(withText(R.string.bank_account_font_icon)));
        onView(withId(R.id.transfer_destination_title)).check(matches(withText(R.string.bank_account)));
        onView(withId(R.id.transfer_destination_selection)).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_destination_description_1)).check(matches(withText("United States")));
        onView(withId(R.id.transfer_destination_description_2)).check(matches(withText("Ending on 7267")));

        onView(withId(R.id.transfer_destination_title)).perform(click());

        onView(withId(R.id.transfer_destination_list)).perform(RecyclerViewActions.actionOnItemAtPosition(5, click()));
        onView(withId(R.id.transfer_destination)).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_destination_icon)).check(matches(withText(R.string.paypal_account_font_icon)));
        onView(withId(R.id.transfer_destination_title)).check(matches(withText(R.string.paypal_account)));
        onView(withId(R.id.transfer_destination_selection)).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_destination_description_1)).check(matches(withText("United States")));
        onView(withId(R.id.transfer_destination_description_2)).check(matches(withText("honey.thigpen@ukbuilder.com")));

        onView(withId(R.id.transfer_amount)).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_amount)).check(matches(withHint("Amount")));
        onView(withId(R.id.transfer_amount_currency)).check(matches(withText("USD")));

        //Check that the toggle is disabled by default
        onView(withId(R.id.switchButton)).check(matches(isDisplayed()));
        onView(withId(R.id.switchButton)).check(matches(not(isSelected())));
        onView(withId(R.id.transfer_summary)).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_summary)).check(matches(withText("Available for Transfer: 100.00 USD")));
    }

}
