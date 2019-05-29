package com.hyperwallet.android.transfermethod.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.pressImeActionButton;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
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
public class BankCardTest {

    private static final String VALID_CARD_NUMBER = "4895142232120006";
    private static final String CARD_NUMBER_MASKED = "************0006";
    private static final String WRONG_LENGTH_CARD_NUMBER = "489514223212";
    private static final String NOT_VALID_CARD_NUMBER = "0101010101010101";
    private static final String VALID_EXPIRATION_DATE = "1020";
    private static final String VALID_EXPIRATION_DATE_FORMATTED = "10/20";
    private static final String INVALID_PATTERN_EXPIRATION_DATE = "1100";
    private static final String VALID_CVV = "022";
    private static final String CARD_NUMBER_LABEL = "Card Number";
    private static final String EXPIRY_DATE_LABEL = "Expiry Date";
    private static final String CVV_LABEL = "CVV (Card Security Code)";

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
                    intent.putExtra("TRANSFER_METHOD_TYPE", "BANK_CARD");
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
                .getResourceContent("successful_tmc_fields_bank_card_response.json")).mock();
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
                matches(withText(R.string.title_add_bank_card)));

        onView(withId(R.id.cardNumber)).check(matches(isDisplayed()));
        onView(withId(R.id.cardNumberLabel)).check(matches(isDisplayed()));
        onView(withId(R.id.cardNumberLabel)).check(matches(withHint(CARD_NUMBER_LABEL)));
        onView(withId(R.id.dateOfExpiry)).check(matches(isDisplayed()));
        onView(withId(R.id.dateOfExpiryLabel)).check(matches(isDisplayed()));
        onView(withId(R.id.dateOfExpiryLabel)).check(matches(withHint(EXPIRY_DATE_LABEL)));
        onView(withId(R.id.cvv)).check(matches(isDisplayed()));
        onView(withId(R.id.cvvLabel)).check(matches(isDisplayed()));
        onView(withId(R.id.cvvLabel)).check(matches(withHint(CVV_LABEL)));

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
        onView(withId(R.id.add_transfer_method_fee_value)).check(matches(withText("USD 1.75")));

        //TODO: Uncomment when processing time node is implemented
//        onView(withId(R.id.add_transfer_method_processing_label)).check(
//                matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
//        onView(withId(R.id.add_transfer_method_processing_label)).check(
//                matches(withText(R.string.add_transfer_method_processing_time_label)));
//        onView(withId(R.id.add_transfer_method_fee_value)).check(matches(withText("1 - 2 Business Days")));
    }

    @Test
    public void testAddTransferMethod_returnsTokenOnBankCardCreation() throws InterruptedException {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_CREATED).withBody(sResourceManager
                .getResourceContent("bank_card_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        final CountDownLatch gate = new CountDownLatch(1);
        final BroadcastReceiver br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                gate.countDown();

                HyperwalletTransferMethod transferMethod = intent.getParcelableExtra(
                        "hyperwallet-local-broadcast-payload");
                assertThat("Card number is incorrect", transferMethod.getField(
                        HyperwalletTransferMethod.TransferMethodFields.CARD_NUMBER), is(CARD_NUMBER_MASKED));
                assertThat("Expiry date is incorrect", transferMethod.getField(
                        HyperwalletTransferMethod.TransferMethodFields.DATE_OF_EXPIRY), is("2020-10"));
            }
        };

        LocalBroadcastManager.getInstance(mActivityTestRule.getActivity().getApplicationContext())
                .registerReceiver(br, new IntentFilter("ACTION_HYPERWALLET_TRANSFER_METHOD_ADDED"));

        onView(withId(R.id.cardNumber)).perform(typeText(VALID_CARD_NUMBER)).perform(closeSoftKeyboard());
        onView(withId(R.id.dateOfExpiry)).perform(typeText(VALID_EXPIRATION_DATE)).perform(closeSoftKeyboard());

        onView(withId(R.id.dateOfExpiry)).check(matches(withText(VALID_EXPIRATION_DATE_FORMATTED)));

        onView(withId(R.id.cvv)).perform(typeText(VALID_CVV)).perform(closeSoftKeyboard());
        onView(withId(R.id.add_transfer_method_button)).perform(nestedScrollTo(), click());

        gate.await(5, SECONDS);

        assertThat("Result code is incorrect", mActivityTestRule.getActivityResult().getResultCode(),
                is(Activity.RESULT_OK));

        LocalBroadcastManager.getInstance(mActivityTestRule.getActivity().getApplicationContext()).unregisterReceiver(
                br);
        assertThat("Action is not broadcasted", gate.getCount(), is(0L));
    }

    @Test
    public void testAddTransferMethod_returnsErrorOnInvalidPattern() {
        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.dateOfExpiry))
                .perform(typeText(INVALID_PATTERN_EXPIRATION_DATE))
                .perform(closeSoftKeyboard(), pressImeActionButton());

        onView(withId(R.id.dateOfExpiryLabel)).check(matches(hasErrorText("Expiry Date is invalid.")));
    }

    @Test
    public void testAddTransferMethod_returnsErrorOnInvalidPresence() {
        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.cardNumber)).perform(click()).perform(closeSoftKeyboard());
        onView(withId(R.id.dateOfExpiry)).perform(click()).perform(closeSoftKeyboard());
        onView(withId(R.id.cvv)).perform(click()).perform(closeSoftKeyboard());

        onView(withId(R.id.add_transfer_method_button)).perform(nestedScrollTo(), click());

        onView(withId(R.id.cardNumberLabel))
                .check(matches(hasErrorText("You must provide a value for this field")));
        onView(withId(R.id.dateOfExpiryLabel))
                .check(matches(hasErrorText("You must provide a value for this field")));
        onView(withId(R.id.cvvLabel))
                .check(matches(hasErrorText("You must provide a value for this field")));
    }

    @Test
    public void testAddTransferMethod_returnsErrorOnInvalidLength() {
        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.cardNumber)).perform(typeText(WRONG_LENGTH_CARD_NUMBER)).perform(closeSoftKeyboard());
        onView(withId(R.id.add_transfer_method_button)).perform(nestedScrollTo(), click());

        onView(withId(R.id.cardNumberLabel)).check(
                matches(hasErrorText("The minimum length of this field is 13 and maximum length is 19.")));
    }

    @Test
    public void testAddTransferMethod_returnsErrorOnInvalidCardNumber() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_BAD_REQUEST).withBody(sResourceManager
                .getResourceContent("error_bank_card_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.cardNumber)).perform(typeText(NOT_VALID_CARD_NUMBER)).perform(closeSoftKeyboard());
        onView(withId(R.id.dateOfExpiry)).perform(typeText(VALID_EXPIRATION_DATE)).perform(closeSoftKeyboard());
        onView(withId(R.id.cvv)).perform(typeText(VALID_CVV)).perform(closeSoftKeyboard());
        onView(withId(R.id.add_transfer_method_button)).perform(nestedScrollTo(), click());

        onView(withId(R.id.cardNumberLabel)).check(matches(hasErrorText(
                "The card cannot be registered - Please contact your issuer or the bank for further information.")));
    }

}
