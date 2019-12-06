package com.hyperwallet.android.ui.transfermethod;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
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

import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod;
import com.hyperwallet.android.ui.R;
import com.hyperwallet.android.ui.common.repository.EspressoIdlingResource;
import com.hyperwallet.android.ui.testutils.rule.HyperwalletExternalResourceManager;
import com.hyperwallet.android.ui.testutils.rule.HyperwalletMockWebServer;
import com.hyperwallet.android.ui.transfermethod.repository.TransferMethodRepositoryFactory;
import com.hyperwallet.android.ui.transfermethod.rule.HyperwalletInsightMockRule;
import com.hyperwallet.android.ui.transfermethod.view.AddTransferMethodActivity;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

import okhttp3.mockwebserver.RecordedRequest;

@RunWith(AndroidJUnit4.class)
public class BankCardTest {

    private static final String VALID_CARD_NUMBER = "4895142232120006";
    private static final String VALID_CARD_NUMBER_FORMATTED = "48951422 32120006";
    private static final String DEFAULT_CARD_NUMBER = "5910123444449850";
    private static final String DEFAULT_CARD_NUMBER_FORMATTED = "5910 1234 4444 9850";
    private static final String CARD_NUMBER_MASKED = "************0006";
    private static final String WRONG_LENGTH_CARD_NUMBER = "489514223212";
    private static final String NOT_VALID_CARD_NUMBER = "0101010101010101";
    private static final String VALID_EXPIRATION_DATE = "1020";
    private static final String VALID_EXPIRATION_DATE_FORMATTED = "10/20";
    private static final String INVALID_PATTERN_EXPIRATION_DATE = "1100";
    private static final String VALID_CVV = "022";
    private static final String CARD_NUMBER_LABEL = "Card Number";
    private static final String EXPIRY_DATE_LABEL = "Expiration Date";
    private static final String CVV_LABEL = "CVV (Card Security Code)";

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
                    intent.putExtra("TRANSFER_METHOD_TYPE", "BANK_CARD");
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
                .getResourceContent("successful_tmc_fields_bank_card_response.json")).mock();
        IdlingRegistry.getInstance().register(EspressoIdlingResource.getIdlingResource());
    }

    @After
    public void cleanup() {
        TransferMethodRepositoryFactory.clearInstance();
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.getIdlingResource());
    }

    @Test
    public void testAddTransferMethod_displaysElementsOnTmcResponse() {
        mActivityTestRule.launchActivity(null);

        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText(R.string.title_add_bank_card)));

        onView(allOf(withId(R.id.section_header_title), withText("Account Information - United States (USD)")))
                .perform(nestedScrollTo())
                .check(matches(isDisplayed()));
        onView(withId(R.id.cardNumber)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.cardNumberLabel)).check(matches(isDisplayed()));
        onView(withId(R.id.cardNumberLabel)).check(matches(withHint(CARD_NUMBER_LABEL)));
        onView(withId(R.id.dateOfExpiry)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.dateOfExpiryLabel)).check(matches(isDisplayed()));
        onView(withId(R.id.dateOfExpiryLabel)).check(matches(withHint(EXPIRY_DATE_LABEL)));
        onView(withId(R.id.cvv)).perform(nestedScrollTo()).check(matches(isDisplayed()));
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

        onView(withId(R.id.add_transfer_method_processing_label)).check(
                matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(withId(R.id.add_transfer_method_processing_label)).check(
                matches(withText(R.string.add_transfer_method_processing_time_label)));
        onView(withId(R.id.add_transfer_method_processing_time_value)).check(matches(withText("IMMEDIATE")));
    }

    @Test
    public void testAddTransferMethod_verifyDefaultValues() {
        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.cardNumber)).check(matches(hasEmptyText()));
        onView(withId(R.id.dateOfExpiry)).check(matches(hasEmptyText()));
        onView(withId(R.id.cvv)).check(matches(hasEmptyText()));
    }

    @Test
    public void testAddTransferMethod_verifyEditableFields() {
        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.cardNumber)).check(matches(isEnabled()));
        onView(withId(R.id.dateOfExpiry)).check(matches(isEnabled()));
        onView(withId(R.id.cvv)).check(matches(isEnabled()));
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

        onView(withId(R.id.cardNumber)).perform(nestedScrollTo(), replaceText(VALID_CARD_NUMBER));
        // Type text here instead to trigger auto-formatting
        onView(withId(R.id.dateOfExpiry)).perform(nestedScrollTo(), typeText(VALID_EXPIRATION_DATE));
        onView(withId(R.id.dateOfExpiry)).check(matches(withText(VALID_EXPIRATION_DATE_FORMATTED)));
        onView(withId(R.id.cvv)).perform(nestedScrollTo(), replaceText(VALID_CVV));
        onView(withId(R.id.add_transfer_method_button)).perform(nestedScrollTo(), click());

        gate.await(5, SECONDS);

        assertThat("Result code is incorrect", mActivityTestRule.getActivityResult().getResultCode(),
                is(Activity.RESULT_OK));

        LocalBroadcastManager.getInstance(
                mActivityTestRule.getActivity().getApplicationContext()).unregisterReceiver(br);
        assertThat("Action is not broadcasted", gate.getCount(), is(0L));
    }

    @Test
    public void testAddTransferMethod_returnsErrorOnInvalidPattern() {
        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.cardNumber)).perform(nestedScrollTo(), replaceText("abc12341234cb"));
        onView(withId(R.id.dateOfExpiry)).perform(nestedScrollTo(), replaceText(INVALID_PATTERN_EXPIRATION_DATE));
        onView(withId(R.id.cvv)).perform(nestedScrollTo(), replaceText("9-09"));

        onView(withId(R.id.add_transfer_method_button)).perform(nestedScrollTo(), click());

        onView(withId(R.id.cardNumberLabel)).check(matches(hasErrorText("is invalid length or format.")));
        onView(withId(R.id.dateOfExpiryLabel)).check(matches(hasErrorText("Expiry Date is invalid.")));
        onView(withId(R.id.cvvLabel)).check(matches(hasErrorText("is invalid length or format.")));
    }

    @Test
    public void testAddTransferMethod_returnsErrorOnInvalidPresence() {
        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.cardNumber)).perform(nestedScrollTo(), replaceText(""));
        onView(withId(R.id.dateOfExpiry)).perform(nestedScrollTo(), replaceText(""));
        onView(withId(R.id.cvv)).perform(nestedScrollTo(), replaceText(""));

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

        onView(withId(R.id.cardNumber)).perform(nestedScrollTo(), replaceText(WRONG_LENGTH_CARD_NUMBER));
        onView(withId(R.id.dateOfExpiry)).perform(nestedScrollTo(), replaceText("1"));
        onView(withId(R.id.cvv)).perform(nestedScrollTo(), replaceText("1"));

        onView(withId(R.id.add_transfer_method_button)).perform(nestedScrollTo(), click());

        onView(withId(R.id.cardNumberLabel)).check(
                matches(hasErrorText("The minimum length of this field is 13 and maximum length is 19.")));
        onView(withId(R.id.dateOfExpiryLabel)).check(
                matches(hasErrorText("The length of this field is exactly 5.")));
        onView(withId(R.id.cvvLabel)).check(
                matches(hasErrorText("The minimum length of this field is 3 and maximum length is 4.")));
    }

    @Test
    public void testAddTransferMethod_returnsErrorOnInvalidCardNumber() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_BAD_REQUEST).withBody(sResourceManager
                .getResourceContent("error_bank_card_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.cardNumber)).perform(nestedScrollTo(), replaceText(NOT_VALID_CARD_NUMBER));
        onView(withId(R.id.dateOfExpiry)).perform(nestedScrollTo(), replaceText(VALID_EXPIRATION_DATE));
        onView(withId(R.id.cvv)).perform(nestedScrollTo(), replaceText(VALID_CVV));
        onView(withId(R.id.add_transfer_method_button)).perform(nestedScrollTo(), click());

        onView(withId(R.id.cardNumberLabel)).check(matches(hasErrorText(
                "The card cannot be registered - Please contact your issuer or the bank for further information.")));
    }

    @Test
    public void testAddTransferMethod_verifyFieldFormatting() throws Exception {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_CREATED).withBody(sResourceManager
                .getResourceContent("bank_card_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.cardNumber)).perform(nestedScrollTo(), typeText(DEFAULT_CARD_NUMBER));
        onView(withId(R.id.cardNumber)).check(matches(withText(DEFAULT_CARD_NUMBER_FORMATTED)));
        onView(withId(R.id.dateOfExpiry)).perform(nestedScrollTo(), typeText(VALID_EXPIRATION_DATE));
        onView(withId(R.id.dateOfExpiry)).check(matches(withText(VALID_EXPIRATION_DATE_FORMATTED)));
        onView(withId(R.id.cvv)).perform(nestedScrollTo(), typeText("34459"));
        onView(withId(R.id.cvv)).check(matches(withText("344")));

        onView(withId(R.id.add_transfer_method_button)).perform(nestedScrollTo(), click());

        // Authentication Token request
        mMockWebServer.getRequest();
        // GraphQl Fields request
        mMockWebServer.getRequest();
        RecordedRequest createBankCardRequest = mMockWebServer.getRequest();
        JSONObject bankCard = new JSONObject(createBankCardRequest.getBody().readUtf8());

        assertThat("Card number is incorrect", bankCard.getString("cardNumber"), is(DEFAULT_CARD_NUMBER));
        assertThat("Date of expiry is incorrect", bankCard.getString("dateOfExpiry"), is("2020-10"));
        assertThat("CVV is incorrect", bankCard.getString("cvv"), is("344"));
    }

    @Test
    public void testAddTransferMethod_verifyCardNumberConditionalFormatting() throws Exception {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_CREATED).withBody(sResourceManager
                .getResourceContent("bank_card_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.cardNumber)).perform(nestedScrollTo(), typeText(VALID_CARD_NUMBER));
        onView(withId(R.id.cardNumber)).check(matches(withText(VALID_CARD_NUMBER_FORMATTED)));
        onView(withId(R.id.dateOfExpiry)).perform(nestedScrollTo(), typeText(VALID_EXPIRATION_DATE));
        onView(withId(R.id.dateOfExpiry)).check(matches(withText(VALID_EXPIRATION_DATE_FORMATTED)));
        onView(withId(R.id.cvv)).perform(nestedScrollTo(), replaceText(VALID_CVV));

        onView(withId(R.id.add_transfer_method_button)).perform(nestedScrollTo(), click());

        // Authentication Token request
        mMockWebServer.getRequest();
        // GraphQl Fields request
        mMockWebServer.getRequest();
        RecordedRequest createBankCardRequest = mMockWebServer.getRequest();
        JSONObject bankCard = new JSONObject(createBankCardRequest.getBody().readUtf8());

        assertThat("Card number is incorrect", bankCard.getString("cardNumber"), is(VALID_CARD_NUMBER));
    }

}
