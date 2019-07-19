package com.hyperwallet.android.ui.transfermethod;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
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
import static org.hamcrest.Matchers.not;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.concurrent.TimeUnit.SECONDS;

import static com.hyperwallet.android.model.transfermethod.HyperwalletBankAccount.Purpose.SAVINGS;
import static com.hyperwallet.android.ui.transfermethod.util.EspressoUtils.hasEmptyText;
import static com.hyperwallet.android.ui.transfermethod.util.EspressoUtils.hasErrorText;
import static com.hyperwallet.android.ui.transfermethod.util.EspressoUtils.hasNoErrorText;
import static com.hyperwallet.android.ui.transfermethod.util.EspressoUtils.nestedScrollTo;
import static com.hyperwallet.android.ui.transfermethod.util.EspressoUtils.withHint;

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
import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod;
import com.hyperwallet.android.ui.R;
import com.hyperwallet.android.ui.common.repository.EspressoIdlingResource;
import com.hyperwallet.android.ui.transfermethod.repository.TransferMethodRepositoryFactory;
import com.hyperwallet.android.ui.transfermethod.rule.HyperwalletExternalResourceManager;
import com.hyperwallet.android.ui.transfermethod.rule.HyperwalletMockWebServer;
import com.hyperwallet.android.ui.transfermethod.util.RecyclerViewCountAssertion;
import com.hyperwallet.android.ui.transfermethod.util.TestAuthenticationProvider;
import com.hyperwallet.android.ui.transfermethod.view.AddTransferMethodActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

@RunWith(AndroidJUnit4.class)
public class BankAccountTest {

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
                .getResourceContent("successful_tmc_fields_bank_account_response.json")).mock();
    }

    @After
    public void cleanup() {
        TransferMethodRepositoryFactory.clearInstance();
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

        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText(R.string.title_add_bank_account)));

        onView(allOf(withId(R.id.section_header_title), withText("Account Information - United States (USD)")))
                .perform(nestedScrollTo())
                .check(matches(isDisplayed()));
        onView(withId(R.id.branchId)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.branchIdLabel)).check(matches(isDisplayed()));
        onView(withId(R.id.branchIdLabel)).check(matches(withHint("Routing Number")));
        onView(withId(R.id.bankAccountId)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.bankAccountIdLabel)).check(matches(isDisplayed()));
        onView(withId(R.id.bankAccountIdLabel)).check(matches(withHint("Account Number")));
        onView(withId(R.id.bankAccountPurpose)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.bankAccountPurposeLabel)).check(matches(isDisplayed()));
        onView(withId(R.id.bankAccountPurposeLabel)).check(matches(withHint("Account Type")));

        onView(allOf(withId(R.id.section_header_title), withText("Account Holder")))
                .perform(nestedScrollTo())
                .check(matches(isDisplayed()));
        onView(withId(R.id.firstName)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.firstNameLabel)).check(matches(isDisplayed()));
        onView(withId(R.id.firstNameLabel)).check(matches(withHint("First Name")));
        onView(withId(R.id.middleName)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.middleNameLabel)).check(matches(isDisplayed()));
        onView(withId(R.id.middleNameLabel)).check(matches(withHint("Middle Name")));
        onView(withId(R.id.lastName)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.lastNameLabel)).check(matches(isDisplayed()));
        onView(withId(R.id.lastNameLabel)).check(matches(withHint("Last Name")));
        onView(withId(R.id.dateOfBirth)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.dateOfBirthLabel)).check(matches(isDisplayed()));
        onView(withId(R.id.dateOfBirthLabel)).check(matches(withHint("Date of Birth")));

        onView(allOf(withId(R.id.section_header_title), withText("Contact Information")))
                .perform(nestedScrollTo())
                .check(matches(isDisplayed()));
        onView(withId(R.id.phoneNumber)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.phoneNumberLabel)).check(matches(isDisplayed()));
        onView(withId(R.id.phoneNumberLabel)).check(matches(withHint("Phone Number")));
        onView(withId(R.id.mobileNumber)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.mobileNumberLabel)).check(matches(isDisplayed()));
        onView(withId(R.id.mobileNumberLabel)).check(matches(withHint("Mobile Number")));

        onView(allOf(withId(R.id.section_header_title), withText("Address")))
                .perform(nestedScrollTo())
                .check(matches(isDisplayed()));
        onView(withId(R.id.country)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.countryLabel)).check(matches(isDisplayed()));
        onView(withId(R.id.countryLabel)).check(matches(withHint("Country")));
        onView(withId(R.id.stateProvince)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.stateProvinceLabel)).check(matches(isDisplayed()));
        onView(withId(R.id.stateProvinceLabel)).check(matches(withHint("State/Province")));
        onView(withId(R.id.addressLine1)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.addressLine1Label)).check(matches(isDisplayed()));
        onView(withId(R.id.addressLine1Label)).check(matches(withHint("Street")));
        onView(withId(R.id.city)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.cityLabel)).check(matches(isDisplayed()));
        onView(withId(R.id.cityLabel)).check(matches(withHint("City")));
        onView(withId(R.id.postalCode)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.postalCodeLabel)).check(matches(isDisplayed()));
        onView(withId(R.id.postalCodeLabel)).check(matches(withHint("Zip/Postal Code")));

        onView(withId(R.id.add_transfer_method_button))
                .perform(nestedScrollTo()).check(matches(withText(R.string.button_create_transfer_method)));
    }

    @Test
    public void testAddTransferMethod_displaysFeeElementsOnTmcResponse() {
        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.add_transfer_method_static_container))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));

        onView(withId(R.id.add_transfer_method_fee_label))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(withId(R.id.add_transfer_method_fee_label)).check(
                matches(withText(R.string.add_transfer_method_fee_label)));
        onView(withId(R.id.add_transfer_method_fee_value)).check(matches(withText("USD 2.00")));

        onView(withId(R.id.add_transfer_method_processing_label))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(withId(R.id.add_transfer_method_processing_label))
                .check(matches(withText(R.string.add_transfer_method_processing_time_label)));
        onView(withId(R.id.add_transfer_method_processing_time_value))
                .check(matches(withText("1-2 Business days")));
    }

    @Test
    public void testAddTransferMethod_verifyDefaultValues() {
        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.branchId)).check(matches(hasEmptyText()));
        onView(withId(R.id.bankAccountId)).check(matches(hasEmptyText()));
        onView(withId(R.id.bankAccountPurpose)).check(matches(hasEmptyText()));

        onView(withId(R.id.firstName)).check(matches(withText("Brody")));
        onView(withId(R.id.middleName)).check(matches(hasEmptyText()));
        onView(withId(R.id.lastName)).check(matches(withText("Nehru")));
        onView(withId(R.id.dateOfBirth)).check(matches(withText("January 01, 2000")));

        onView(withId(R.id.phoneNumber)).check(matches(withText("+1 604 6666666")));
        onView(withId(R.id.mobileNumber)).check(matches(withText("604 666 6666")));

        onView(withId(R.id.country)).check(matches(withText("Canada")));
        onView(withId(R.id.stateProvince)).check(matches(withText("BC")));
        onView(withId(R.id.addressLine1)).check(matches(withText("950 Granville Street")));
        onView(withId(R.id.city)).check(matches(withText("Vancouver")));
        onView(withId(R.id.postalCode)).check(matches(withText("V6Z1L2")));
    }

    @Test
    public void testAddTransferMethod_verifyEditableFields() {
        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.branchId)).check(matches(isEnabled()));
        onView(withId(R.id.bankAccountId)).check(matches(isEnabled()));
        onView(withId(R.id.bankAccountPurpose)).check(matches(isEnabled()));

        onView(withId(R.id.firstName)).check(matches(not(isEnabled())));
        onView(withId(R.id.middleName)).check(matches(isEnabled()));
        onView(withId(R.id.lastName)).check(matches(isEnabled()));
        onView(withId(R.id.dateOfBirth)).check(matches(isEnabled()));

        onView(withId(R.id.phoneNumber)).check(matches(isEnabled()));
        onView(withId(R.id.mobileNumber)).check(matches(isEnabled()));

        onView(withId(R.id.country)).check(matches(not(isEnabled())));
        onView(withId(R.id.stateProvince)).check(matches(isEnabled()));
        onView(withId(R.id.addressLine1)).check(matches(isEnabled()));
        onView(withId(R.id.city)).check(matches(isEnabled()));
        onView(withId(R.id.postalCode)).check(matches(isEnabled()));
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

                assertThat("First Name is incorrect", transferMethod.getField(
                        HyperwalletTransferMethod.TransferMethodFields.FIRST_NAME), is("Brody"));
                assertThat("Last Name is incorrect", transferMethod.getField(
                        HyperwalletTransferMethod.TransferMethodFields.LAST_NAME), is("Nehru"));
                assertThat("Date of birth is incorrect", transferMethod.getField(
                        HyperwalletTransferMethod.TransferMethodFields.DATE_OF_BIRTH), is("2000-01-01"));

                assertThat("Phone Number is incorrect", transferMethod.getField(
                        HyperwalletTransferMethod.TransferMethodFields.PHONE_NUMBER), is("+1 604 6666666"));
                assertThat("Mobile Number incorrect", transferMethod.getField(
                        HyperwalletTransferMethod.TransferMethodFields.MOBILE_NUMBER), is("604 666 6666"));
                assertThat("Country is incorrect", transferMethod.getField(
                        HyperwalletTransferMethod.TransferMethodFields.COUNTRY), is("CA"));
                assertThat("State Province is incorrect", transferMethod.getField(
                        HyperwalletTransferMethod.TransferMethodFields.STATE_PROVINCE), is("BC"));
                assertThat("Address is incorrect", transferMethod.getField(
                        HyperwalletTransferMethod.TransferMethodFields.ADDRESS_LINE_1), is("950 Granville Street"));
                assertThat("City is incorrect", transferMethod.getField(
                        HyperwalletTransferMethod.TransferMethodFields.CITY), is("Vancouver"));
                assertThat("Postal Code is incorrect", transferMethod.getField(
                        HyperwalletTransferMethod.TransferMethodFields.POSTAL_CODE), is("V6Z1L2"));
            }
        };

        LocalBroadcastManager.getInstance(mActivityTestRule.getActivity().getApplicationContext())
                .registerReceiver(br, new IntentFilter("ACTION_HYPERWALLET_TRANSFER_METHOD_ADDED"));

        onView(withId(R.id.branchId)).perform(nestedScrollTo(), replaceText(ROUTING_NUMBER));
        onView(withId(R.id.bankAccountId)).perform(nestedScrollTo(), replaceText(ACCOUNT_NUMBER));
        onView(withId(R.id.bankAccountPurpose)).perform(nestedScrollTo(), click());
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

        onView(withId(R.id.bankAccountId)).perform(nestedScrollTo(), replaceText("ewrd{123"));
        onView(withId(R.id.branchId)).perform(nestedScrollTo(), replaceText("{dfghfgh}"));
        onView(withId(R.id.firstName)).perform(nestedScrollTo(), replaceText("ewrd{1{2"));
        onView(withId(R.id.lastName)).perform(nestedScrollTo(), replaceText("ewrd{1{2345"));
        onView(withId(R.id.addressLine1)).perform(nestedScrollTo(), replaceText("950 {G}ranville Street"));
        onView(withId(R.id.city)).perform(nestedScrollTo(), replaceText("Vancouve{r}"));
        onView(withId(R.id.stateProvince)).perform(nestedScrollTo(), replaceText("df{r}"));
        onView(withId(R.id.postalCode)).perform(nestedScrollTo(), replaceText("df{r}"));

        onView(withId(R.id.add_transfer_method_button)).perform(nestedScrollTo(), click());

        onView(withId(R.id.branchIdLabel))
                .check(matches(hasErrorText("is invalid length or format.")));
        onView(withId(R.id.bankAccountIdLabel))
                .check(matches(hasErrorText("is invalid length or format.")));
        onView(withId(R.id.firstNameLabel))
                .check(matches(hasErrorText("is invalid length or format.")));
        onView(withId(R.id.lastNameLabel))
                .check(matches(hasErrorText("is invalid length or format.")));
        onView(withId(R.id.addressLine1Label))
                .check(matches(hasErrorText("is invalid length or format.")));
        onView(withId(R.id.cityLabel))
                .check(matches(hasErrorText("is invalid length or format.")));
        onView(withId(R.id.stateProvinceLabel))
                .check(matches(hasErrorText("is invalid length or format.")));
        onView(withId(R.id.postalCodeLabel))
                .check(matches(hasErrorText("is invalid length or format.")));
    }

    @Test
    public void testAddTransferMethod_returnsErrorOnInvalidPresence() {
        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.branchId)).perform(nestedScrollTo(), replaceText(""));
        onView(withId(R.id.bankAccountId)).perform(nestedScrollTo(), replaceText(""));
        onView(withId(R.id.bankAccountPurpose)).perform(nestedScrollTo(), replaceText(""));
        onView(withId(R.id.firstName)).perform(nestedScrollTo(), replaceText(""));
        onView(withId(R.id.middleName)).perform(nestedScrollTo(), replaceText(""));
        onView(withId(R.id.lastName)).perform(nestedScrollTo(), replaceText(""));
        onView(withId(R.id.dateOfBirth)).perform(nestedScrollTo(), replaceText(""));
        onView(withId(R.id.phoneNumber)).perform(nestedScrollTo(), replaceText(""));
        onView(withId(R.id.mobileNumber)).perform(nestedScrollTo(), replaceText(""));
        onView(withId(R.id.addressLine1)).perform(nestedScrollTo(), replaceText(""));
        onView(withId(R.id.city)).perform(nestedScrollTo(), replaceText(""));
        onView(withId(R.id.stateProvince)).perform(nestedScrollTo(), replaceText(""));
        onView(withId(R.id.postalCode)).perform(nestedScrollTo(), replaceText(""));

        onView(withId(R.id.add_transfer_method_button)).perform(nestedScrollTo(), click());

        onView(withId(R.id.branchIdLabel))
                .check(matches(hasErrorText("You must provide a value for this field")));
        onView(withId(R.id.bankAccountIdLabel))
                .check(matches(hasErrorText("You must provide a value for this field")));
        onView(withId(R.id.bankAccountPurposeLabel))
                .check(matches(hasErrorText("You must provide a value for this field")));
        onView(withId(R.id.firstNameLabel))
                .check(matches(hasErrorText("You must provide a value for this field")));
        onView(withId(R.id.lastNameLabel))
                .check(matches(hasErrorText("You must provide a value for this field")));
        onView(withId(R.id.addressLine1Label))
                .check(matches(hasErrorText("You must provide a value for this field")));
        onView(withId(R.id.cityLabel))
                .check(matches(hasErrorText("You must provide a value for this field")));
        onView(withId(R.id.stateProvinceLabel))
                .check(matches(hasErrorText("You must provide a value for this field")));
        onView(withId(R.id.postalCodeLabel))
                .check(matches(hasErrorText("You must provide a value for this field")));

        onView(withId(R.id.middleNameLabel)).check(matches(hasNoErrorText()));
        onView(withId(R.id.dateOfBirthLabel)).check(matches(hasNoErrorText()));
        onView(withId(R.id.phoneNumberLabel)).check(matches(hasNoErrorText()));
        onView(withId(R.id.mobileNumberLabel)).check(matches(hasNoErrorText()));
    }

    @Test
    public void testAddTransferMethod_returnsErrorOnInvalidLength() {
        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.branchId)).perform(nestedScrollTo(), replaceText("2111795311"));
        onView(withId(R.id.bankAccountId)).perform(nestedScrollTo(), replaceText("1"));

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

        onView(withId(R.id.branchId)).perform(nestedScrollTo(), replaceText(INVALID_ROUTING_NUMBER));
        onView(withId(R.id.bankAccountId)).perform(nestedScrollTo(), replaceText(ACCOUNT_NUMBER));
        onView(withId(R.id.bankAccountPurpose)).perform(nestedScrollTo(), click());
        onView(allOf(withId(R.id.select_name), withText("Checking"))).perform(click());

        onView(withId(R.id.add_transfer_method_button)).perform(nestedScrollTo(), click());

        onView(withId(R.id.branchIdLabel))
                .check(matches(hasErrorText(
                        "Routing Number [" + INVALID_ROUTING_NUMBER
                                + "] is not valid. Please modify Routing Number to a valid ACH Routing Number of the "
                                + "branch of your bank.")));
    }

}
