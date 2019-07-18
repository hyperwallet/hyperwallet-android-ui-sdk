package com.hyperwallet.android.ui.transfermethod;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.hasSibling;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withChild;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;

import static java.net.HttpURLConnection.HTTP_OK;

import static com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod.TransferMethodTypes.BANK_ACCOUNT;
import static com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod.TransferMethodTypes.BANK_CARD;
import static com.hyperwallet.android.model.user.HyperwalletUser.ProfileTypes.BUSINESS;
import static com.hyperwallet.android.model.user.HyperwalletUser.ProfileTypes.INDIVIDUAL;
import static com.hyperwallet.android.ui.transfermethod.view.AddTransferMethodActivity.EXTRA_TRANSFER_METHOD_COUNTRY;
import static com.hyperwallet.android.ui.transfermethod.view.AddTransferMethodActivity.EXTRA_TRANSFER_METHOD_CURRENCY;
import static com.hyperwallet.android.ui.transfermethod.view.AddTransferMethodActivity.EXTRA_TRANSFER_METHOD_PROFILE_TYPE;
import static com.hyperwallet.android.ui.transfermethod.view.AddTransferMethodActivity.EXTRA_TRANSFER_METHOD_TYPE;
import static com.hyperwallet.android.ui.transfermethod.util.EspressoUtils.atPosition;
import static com.hyperwallet.android.ui.transfermethod.util.EspressoUtils.withDrawable;

import android.widget.TextView;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.ui.R;
import com.hyperwallet.android.ui.common.util.EspressoIdlingResource;
import com.hyperwallet.android.ui.transfermethod.repository.RepositoryFactory;
import com.hyperwallet.android.ui.transfermethod.rule.HyperwalletExternalResourceManager;
import com.hyperwallet.android.ui.transfermethod.rule.HyperwalletMockWebServer;
import com.hyperwallet.android.ui.transfermethod.view.SelectTransferMethodActivity;
import com.hyperwallet.android.ui.transfermethod.util.RecyclerViewCountAssertion;
import com.hyperwallet.android.ui.transfermethod.util.TestAuthenticationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SelectTransferMethodTest {

    @ClassRule
    public static HyperwalletExternalResourceManager sResourceManager = new HyperwalletExternalResourceManager();
    @Rule
    public HyperwalletMockWebServer mMockWebServer = new HyperwalletMockWebServer(8080);
    @Rule
    public ActivityTestRule<SelectTransferMethodActivity> mActivityTestRule =
            new ActivityTestRule<>(SelectTransferMethodActivity.class, true, false);
    @Rule
    public IntentsTestRule<SelectTransferMethodActivity> mIntentsTestRule =
            new IntentsTestRule<>(SelectTransferMethodActivity.class, true, false);

    @Before
    public void setup() {
        Hyperwallet.getInstance(new TestAuthenticationProvider());

        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("authentication_token_response.json")).mock();
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
    public void testSelectTransferMethod_verifyCorrectLabelsDisplayed() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("user_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("successful_tmc_keys_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.toolbar)).check(matches(isDisplayed()));
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar)))).check(
                matches(withText(R.string.activity_select_transfer_method_title)));

        onView(withId(R.id.select_transfer_method_country_label)).check(
                matches(withText(R.string.select_transfer_method_country)));
        onView(withId(R.id.select_transfer_method_country_value)).check(matches(withText("United States")));
        onView(allOf(hasSibling(withId(R.id.select_transfer_method_country_value)),
                withDrawable(R.drawable.ic_keyboard_arrow_right_12dp))).check(matches(isDisplayed()));

        onView(withId(R.id.select_transfer_method_currency_label)).check(
                matches(withText(R.string.select_transfer_method_currency)));
        onView(withId(R.id.select_transfer_method_currency_value)).check(matches(withText("USD")));
        onView(allOf(hasSibling(withId(R.id.select_transfer_method_currency_value)),
                withDrawable(R.drawable.ic_keyboard_arrow_right_12dp))).check(matches(isDisplayed()));

    }

    @Test
    public void testSelectTransferMethod_verifyCountrySelectionList() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("user_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("successful_tmc_keys_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.select_transfer_method_country_value)).perform(click());

        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.country_selection_toolbar)))).check(
                matches(withText(R.string.select_transfer_method_country)));
        onView(withId(R.id.search_button)).check(doesNotExist());
        onView(withId(R.id.country_selection_list)).check(new RecyclerViewCountAssertion(5));
        onView(allOf(withId(R.id.country_name), withText("Canada"))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.country_name), withText("Croatia"))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.country_name), withText("Mexico"))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.country_name), withText("United Kingdom"))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.country_name), withText("United States"))).check(matches(isDisplayed()));

        onView(allOf(withId(R.id.country_item_selected_image),
                hasSibling(allOf(withId(R.id.country_name), withText("United States"))))).check(matches(isDisplayed()));

        onView(allOf(withId(R.id.country_name), withText("Canada"))).perform(click());
        onView(withId(R.id.select_transfer_method_country_value)).check(matches(withText("Canada")));
    }

    @Test
    public void testSelectTransferMethod_verifyCountrySelectionSearch() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("user_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("successful_tmc_keys_large_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.select_transfer_method_country_value)).perform(click());

        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.country_selection_toolbar)))).check(
                matches(withText(R.string.select_transfer_method_country)));
        onView(withId(R.id.search_button)).perform(click());
        onView(withId(R.id.search_src_text)).perform(typeText("United States"));
        onView(withId(R.id.country_selection_list)).check(new RecyclerViewCountAssertion(1));
        onView(allOf(withId(R.id.country_name), withText("United States"))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.country_name), withText("United States"))).perform(click());
        onView(withId(R.id.select_transfer_method_country_value)).check(matches(withText("United States")));
    }

    @Test
    public void testSelectTransferMethod_verifyCurrencySelectionList() {

        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("user_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("successful_tmc_keys_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.select_transfer_method_country_value)).perform(click());
        onView(allOf(withId(R.id.country_name), withText("United States"))).perform(click());

        onView(withId(R.id.select_transfer_method_currency_value)).perform(click());
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.currency_selection_toolbar)))).check(
                matches(withText(R.string.select_transfer_method_currency)));
        onView(withId(R.id.search_button)).check(doesNotExist());
        onView(withId(R.id.currency_selection_list)).check(new RecyclerViewCountAssertion(1));
        onView(allOf(withId(R.id.currency_name), withText("United States Dollar"))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.currency_item_selected_image),
                hasSibling(allOf(withId(R.id.currency_name), withText("United States Dollar"))))).check(
                matches(isDisplayed()));

        onView(allOf(withId(R.id.currency_name), withText("United States Dollar"))).perform(click());
        onView(withId(R.id.select_transfer_method_currency_value)).check(matches(withText("USD")));
    }

    @Test
    public void testSelectTransferMethod_verifyTransferMethodsList() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("user_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("successful_tmc_keys_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.select_transfer_method_country_value)).check(matches(withText("United States")));
        onView(withId(R.id.select_transfer_method_currency_value)).check(matches(withText("USD")));
        onView(withId(R.id.select_transfer_method_types_list)).check(new RecyclerViewCountAssertion(4));

        onView(withId(R.id.select_transfer_method_types_list)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.bank_account_font_icon)))));
        onView(withId(R.id.select_transfer_method_types_list)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.bank_account)))));
        onView(withId(R.id.select_transfer_method_types_list)).check(
                matches(atPosition(0, hasDescendant(withText("Transaction Fee: USD 2.00")))));
        onView(withId(R.id.select_transfer_method_types_list)).check(
                matches(atPosition(0, hasDescendant(withText("Processing Time: 1-2 Business days")))));

        onView(withId(R.id.select_transfer_method_types_list)).check(
                matches(atPosition(1, hasDescendant(withText(R.string.bank_card_font_icon)))));
        onView(withId(R.id.select_transfer_method_types_list)).check(
                matches(atPosition(1, hasDescendant(withText(R.string.bank_card)))));
        onView(withId(R.id.select_transfer_method_types_list)).check(
                matches(atPosition(1, hasDescendant(withText("Transaction Fee: USD 1.75")))));
        onView(withId(R.id.select_transfer_method_types_list)).check(
                matches(atPosition(1, hasDescendant(withText("Processing Time: IMMEDIATE")))));

        onView(withId(R.id.select_transfer_method_types_list)).check(
                matches(atPosition(2, hasDescendant(withText(R.string.wire_account_font_icon)))));
        onView(withId(R.id.select_transfer_method_types_list)).check(
                matches(atPosition(2, hasDescendant(withText(R.string.wire_account)))));
        onView(withId(R.id.select_transfer_method_types_list)).check(
                matches(atPosition(2, hasDescendant(withText("Transaction Fee: USD 20.00")))));
        onView(withId(R.id.select_transfer_method_types_list)).check(
                matches(atPosition(2, hasDescendant(withText("Processing Time: 1-3 Business days")))));

        onView(withId(R.id.select_transfer_method_types_list)).check(
                matches(atPosition(3, hasDescendant(withText(R.string.paypal_account_font_icon)))));
        onView(withId(R.id.select_transfer_method_types_list)).check(
                matches(atPosition(3, hasDescendant(withText(R.string.paypal_account)))));
        onView(withId(R.id.select_transfer_method_types_list)).check(
                matches(atPosition(3, hasDescendant(withText("Transaction Fee: USD 0.25")))));
        onView(withId(R.id.select_transfer_method_types_list)).check(
                matches(atPosition(3, hasDescendant(withText("Processing Time: IMMEDIATE")))));

    }

    @Test
    public void testSelectTransferMethod_verifyTransferMethodsListEmptyFee() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("user_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("successful_tmc_keys_empty_fee_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.select_transfer_method_country_value)).check(matches(withText("United States")));
        onView(withId(R.id.select_transfer_method_currency_value)).check(matches(withText("USD")));
        onView(withId(R.id.select_transfer_method_types_list)).check(new RecyclerViewCountAssertion(1));

        onView(withId(R.id.select_transfer_method_types_list)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.bank_account_font_icon)))));
        onView(withId(R.id.select_transfer_method_types_list)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.bank_account)))));
        onView(withId(R.id.select_transfer_method_types_list)).check(
                matches(atPosition(0, hasDescendant(withText("Processing Time: 1-2 Business days")))));
        onView(withId(R.id.transfer_method_type_description_1)).check(
                matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));

    }

    @Test
    public void testSelectTransferMethod_verifyTransferMethodsListEmptyProcessing() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("user_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("successful_tmc_keys_empty_processing_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.select_transfer_method_country_value)).check(matches(withText("United States")));
        onView(withId(R.id.select_transfer_method_currency_value)).check(matches(withText("USD")));
        onView(withId(R.id.select_transfer_method_types_list)).check(new RecyclerViewCountAssertion(1));

        onView(withId(R.id.select_transfer_method_types_list)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.bank_account_font_icon)))));
        onView(withId(R.id.select_transfer_method_types_list)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.bank_account)))));
        onView(withId(R.id.select_transfer_method_types_list)).check(
                matches(atPosition(0, hasDescendant(withText("Transaction Fee: USD 2.00")))));
        onView(withId(R.id.transfer_method_type_description_2)).check(
                matches(withEffectiveVisibility(ViewMatchers.Visibility.INVISIBLE)));

    }

    @Test
    public void testSelectTransferMethod_verifyTransferMethodsListUpdatedOnSelectionChange() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("user_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("successful_tmc_keys_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.select_transfer_method_country_value)).check(matches(withText("United States")));
        onView(withId(R.id.select_transfer_method_currency_value)).check(matches(withText("USD")));
        onView(withId(R.id.select_transfer_method_types_list)).check(new RecyclerViewCountAssertion(4));

        onView(withId(R.id.select_transfer_method_country_value)).perform(click());
        onView(allOf(withId(R.id.country_name), withText("Canada"))).perform(click());

        onView(withId(R.id.select_transfer_method_types_list)).check(new RecyclerViewCountAssertion(2));
        onView(withId(R.id.select_transfer_method_types_list)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.bank_account_font_icon)))));
        onView(withId(R.id.select_transfer_method_types_list)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.bank_account)))));
        onView(withId(R.id.select_transfer_method_types_list)).check(
                matches(atPosition(0, hasDescendant(withText("Transaction Fee: CAD 2.20")))));
        onView(withId(R.id.select_transfer_method_types_list)).check(
                matches(atPosition(0, hasDescendant(withText("Processing Time: 1-2 Business days")))));

        onView(withId(R.id.select_transfer_method_types_list)).check(
                matches(atPosition(1, hasDescendant(withText(R.string.paypal_account_font_icon)))));
        onView(withId(R.id.select_transfer_method_types_list)).check(
                matches(atPosition(1, hasDescendant(withText(R.string.paypal_account)))));
        onView(withId(R.id.select_transfer_method_types_list)).check(
                matches(atPosition(1, hasDescendant(withText("Transaction Fee: CAD 0.25")))));
        onView(withId(R.id.select_transfer_method_types_list)).check(
                matches(atPosition(1, hasDescendant(withText("Processing Time: IMMEDIATE")))));

    }

    @Test
    public void testSelectTransferMethod_verifyIntentIndividualUser() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("user_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("successful_tmc_keys_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("successful_tmc_fields_bank_account_response.json")).mock();

        mIntentsTestRule.launchActivity(null);

        onView(withId(R.id.select_transfer_method_types_list))
                .perform(RecyclerViewActions.actionOnItem(withChild(withText(R.string.bank_account)), click()));
        intended(hasExtra(EXTRA_TRANSFER_METHOD_PROFILE_TYPE, INDIVIDUAL));
        intended(hasExtra(EXTRA_TRANSFER_METHOD_COUNTRY, "US"));
        intended(hasExtra(EXTRA_TRANSFER_METHOD_CURRENCY, "USD"));
        intended(hasExtra(EXTRA_TRANSFER_METHOD_TYPE, BANK_ACCOUNT));
    }

    @Test
    public void testSelectTransferMethod_verifyIntentBusinessUser() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("user_business_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("successful_tmc_keys_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("successful_tmc_fields_bank_account_response.json")).mock();

        mIntentsTestRule.launchActivity(null);

        onView(withId(R.id.select_transfer_method_types_list))
                .perform(RecyclerViewActions.actionOnItem(withChild(withText(R.string.bank_card)), click()));
        intended(hasExtra(EXTRA_TRANSFER_METHOD_PROFILE_TYPE, BUSINESS));
        intended(hasExtra(EXTRA_TRANSFER_METHOD_COUNTRY, "US"));
        intended(hasExtra(EXTRA_TRANSFER_METHOD_CURRENCY, "USD"));
        intended(hasExtra(EXTRA_TRANSFER_METHOD_TYPE, BANK_CARD));
    }

    @Test
    public void testSelectTransferMethod_clickBankAccountOpensAddTransferMethodUi() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("user_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("successful_tmc_keys_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("successful_tmc_fields_bank_account_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.select_transfer_method_types_list))
                .perform(RecyclerViewActions.actionOnItem(withChild(withText(R.string.bank_account)), click()));
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar)))).check(
                matches(withText(R.string.title_add_bank_account)));
    }

    @Test
    public void testSelectTransferMethod_clickBankCardOpensAddTransferMethodUi() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("user_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("successful_tmc_keys_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("successful_tmc_fields_bank_card_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.select_transfer_method_types_list))
                .perform(RecyclerViewActions.actionOnItem(withChild(withText(R.string.bank_card)), click()));
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar)))).check(
                matches(withText(R.string.title_add_bank_card)));
    }

    @Test
    public void testSelectTransferMethod_verifyThatCountryIsFromUserProfile() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("user_ca_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("successful_tmc_keys_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.select_transfer_method_country_value)).check(matches(withText("Canada")));
        onView(withId(R.id.select_transfer_method_currency_value)).check(matches(withText("CAD")));
    }

    @Test
    public void testSelectTransferMethod_verifyDefaultsToUSWhenUserProfileCountryIsNotConfigured() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("user_not_configured_country_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("successful_tmc_keys_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.select_transfer_method_country_value)).check(matches(withText("United States")));
        onView(withId(R.id.select_transfer_method_currency_value)).check(matches(withText("USD")));
    }

    @Test
    public void testSelectTransferMethod_verifyDefaultsToUSWhenUserProfileDoesNotHaveCountry() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("user_no_country_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("successful_tmc_keys_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.select_transfer_method_country_value)).check(matches(withText("United States")));
        onView(withId(R.id.select_transfer_method_currency_value)).check(matches(withText("USD")));
    }
}
