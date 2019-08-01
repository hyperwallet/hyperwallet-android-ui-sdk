package com.hyperwallet.android.ui.transfer.rule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.isSelected;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.Matchers.not;

import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.net.HttpURLConnection.HTTP_OK;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.ui.common.repository.EspressoIdlingResource;
import com.hyperwallet.android.ui.transfer.R;
import com.hyperwallet.android.ui.transfer.repository.TransferRepositoryFactory;
import com.hyperwallet.android.ui.transfer.rule.rule.HyperwalletMockWebServer;
import com.hyperwallet.android.ui.transfer.rule.util.TestAuthenticationProvider;
import com.hyperwallet.android.ui.transfer.view.CreateTransferActivity;
import com.hyperwallet.android.ui.user.repository.UserRepositoryFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class TransferUserFundsTest {

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
    public void testTransferFunds_verifyTransferButtonDisabledWhenAmountNotSet() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("transfer_method_list_single_bank_account_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("create_transfer_quote_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.add_transfer_destination)).check(matches(not(isDisplayed())));
        onView(withId(R.id.transfer_destination)).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_destination_icon)).check(matches(withText(R.string.bank_account_font_icon)));
        onView(withId(R.id.transfer_destination_title)).check(matches(withText(R.string.bank_account)));
        onView(withId(R.id.transfer_destination_selection)).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_destination_description_1)).check(matches(withText("United States")));
        onView(withId(R.id.transfer_destination_description_2)).check(matches(withText("Ending on 7267")));

        onView(withId(R.id.transfer_amount)).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_amount)).check(matches(withHint("Amount")));
        onView(withId(R.id.transfer_amount_currency)).check(matches(withText("USD")));
        onView(withId(R.id.transfer_all_funds_label)).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_all_funds_label)).check(matches(withText(R.string.transfer_all_funds_label)));

        //Check that the toggle is disabled by default
        onView(withId(R.id.switchButton)).check(matches(isDisplayed()));
        onView(withId(R.id.switchButton)).check(matches(not(isSelected())));
        onView(withId(R.id.transfer_summary)).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_summary)).check(matches(withText("Available for Transfer: 98.00 USD")));

        onView(withId(R.id.transfer_notes)).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_notes)).check(matches(withHint("Description")));
        onView(withText(R.string.transfer_notes_additional_info_label)).check(matches(isDisplayed()));

        onView(withId(R.id.transfer_action_button)).check(matches(isDisplayed()));
        onView(withId(R.id.transfer_action_button)).check(matches(not(isEnabled())));
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
        onView(withId(R.id.transfer_action_button)).check(matches(not(isEnabled())));
    }

    @Test
    public void testTransferFunds_createTransferWithFX() {

    }

    @Test
    public void testTransferFunds_createTransferWithNoFX() {
    }

    @Test
    public void testTransferFunds_createTransferWithNotes() {
    }

    @Test
    public void testTransferFunds_createTransferWithAllFunds() {
    }

    @Test
    public void testTransferFunds_createTransferLimitError() {
    }

    @Test
    public void testTransferFunds_createTransferInsufficientFundsError() {
    }

    @Test
    public void testTransferFunds_createTransferMinimumAmountError() {
    }

    @Test
    public void testTransferFunds_createTransferInvalidSourceError() {
    }

    @Test
    public void testTransferFunds_createTransferConnectionError() {
    }

}
