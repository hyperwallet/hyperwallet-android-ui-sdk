package com.hyperwallet.android.ui.transfermethod;

import android.app.Instrumentation;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.Root;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.hyperwallet.android.model.StatusTransition;
import com.hyperwallet.android.ui.R;
import com.hyperwallet.android.ui.common.repository.EspressoIdlingResource;
import com.hyperwallet.android.ui.common.view.error.DefaultErrorDialogFragment;
import com.hyperwallet.android.ui.testutils.rule.HyperwalletExternalResourceManager;
import com.hyperwallet.android.ui.testutils.rule.HyperwalletMockWebServer;
import com.hyperwallet.android.ui.testutils.util.RecyclerViewCountAssertion;
import com.hyperwallet.android.ui.transfermethod.repository.TransferMethodRepositoryFactory;
import com.hyperwallet.android.ui.transfermethod.rule.HyperwalletInsightMockRule;
import com.hyperwallet.android.ui.transfermethod.view.AddTransferMethodActivity;
import com.hyperwallet.android.ui.transfermethod.view.ListTransferMethodActivity;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.hasSibling;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.hyperwallet.android.model.StatusTransition.StatusDefinition.DE_ACTIVATED;
import static com.hyperwallet.android.ui.common.view.error.DefaultErrorDialogFragment.RESULT_ERROR;
import static com.hyperwallet.android.ui.testutils.util.EspressoUtils.atPosition;
import static com.hyperwallet.android.ui.testutils.util.EspressoUtils.nestedScrollTo;
import static com.hyperwallet.android.ui.testutils.util.EspressoUtils.withDrawable;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
public class EditTransferMethodTest {

    @ClassRule
    public static HyperwalletExternalResourceManager sResourceManager = new HyperwalletExternalResourceManager();
    @Rule
    public HyperwalletInsightMockRule mHyperwalletInsightMockRule = new HyperwalletInsightMockRule();
    @Rule
    public HyperwalletMockWebServer mMockWebServer = new HyperwalletMockWebServer(8080);
    @Rule
    public ActivityTestRule<ListTransferMethodActivity> mActivityTestRule =
            new ActivityTestRule<>(ListTransferMethodActivity.class, true, false);

    @Before
    public void setup() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("authentication_token_response.json")).mock();
        IdlingRegistry.getInstance().register(EspressoIdlingResource.getIdlingResource());
    }

    @After
    public void cleanup() {
        TransferMethodRepositoryFactory.clearInstance();
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.getIdlingResource());
    }


    @Test
    public void testUpdateTransferMethodFragment_verifyUpdateBankAccountTransferMethod() throws InterruptedException {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("transfer_method_list_single_bank_account_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("transfer_method_update_bankacount_response.json")).mock();


        final CountDownLatch gate = new CountDownLatch(1);
        final BroadcastReceiver br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                gate.countDown();

                StatusTransition statusTransition = intent.getParcelableExtra(
                        "hyperwallet-local-broadcast-payload");
                assertThat("Transition is not valid", statusTransition.getTransition(), is(DE_ACTIVATED));
            }
        };

        // run test
        mActivityTestRule.launchActivity(null);
        LocalBroadcastManager.getInstance(mActivityTestRule.getActivity().getApplicationContext())
                .registerReceiver(br, new IntentFilter("ACTION_HYPERWALLET_TRANSFER_METHOD_DEACTIVATED"));

        // assert
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()));
        onView(withId(R.id.toolbar))
                .check(matches(
                        hasDescendant(withText(R.string.mobileTransferMethodsHeader))));
        onView(withId(R.id.fab)).check(matches(isDisplayed()));

        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.bank_account_font_icon)))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.bank_account)))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withText("United States")))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withText(getEndingIn("1332"))))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withDrawable(R.drawable.ic_three_dots_16dp)))));

        onView(allOf(instanceOf(ImageButton.class), hasSibling(withText(R.string.bank_account)))).perform(click())
                .inRoot(Matchers.<Root>instanceOf(MenuItem.class));


//        Assert both icons
        onView(withDrawable(R.drawable.ic_trash)).check(matches(isDisplayed()));
        onView(withText(R.string.edit)).check(matches(isDisplayed())).perform(click());

        onView(withId(R.id.branchIdLabel)).check(matches(isDisplayed()));

        onView(withId(R.id.branchId)).perform(nestedScrollTo()).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
                .check(matches(withText("021000021")));

               onView(ViewMatchers.withId(R.id.branchIdLabel))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

              onView(ViewMatchers.withId(R.id.bankAccountId))
                .perform(nestedScrollTo())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
                .check(matches(withText("****")));

                onView(ViewMatchers.withId(R.id.bankAccountIdLabel))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        // ACCOUNT HOLDER INFO
           onView(
                Matchers.allOf(
                        ViewMatchers.withId(R.id.section_header_title),
                        ViewMatchers.withText(R.string.account_holder)
                )
        )
                .perform(nestedScrollTo())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

    onView(ViewMatchers.withId(R.id.firstName))
                .perform(nestedScrollTo())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
                .check(matches(withText("Android Mobile")));

        onView(ViewMatchers.withId(R.id.firstNameLabel))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        onView(ViewMatchers.withId(R.id.middleName))
                .perform(nestedScrollTo())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
                .check(matches(withText("mobile-qa")));

       onView(ViewMatchers.withId(R.id.middleNameLabel))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        onView(ViewMatchers.withId(R.id.lastName))
                .perform(nestedScrollTo())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
                .check(matches(withText("UITest")));

         onView(ViewMatchers.withId(R.id.lastNameLabel))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        // ADDRESS
        onView(
                Matchers.allOf(
                        ViewMatchers.withId(R.id.section_header_title),
                        ViewMatchers.withText(R.string.address)
                )
        ).perform(nestedScrollTo())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        onView(ViewMatchers.withId(R.id.country))
                .perform(nestedScrollTo())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
                .check(matches(withText("Canada")));

        onView(ViewMatchers.withId(R.id.countryLabel))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        onView(ViewMatchers.withId(R.id.stateProvince))
                .perform(nestedScrollTo())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
                .check(matches(withText("BC")));

        onView(ViewMatchers.withId(R.id.stateProvinceLabel))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        onView(ViewMatchers.withId(R.id.addressLine1))
                .perform(nestedScrollTo())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
                .check(matches(withText("475 howe st")));

        onView(ViewMatchers.withId(R.id.addressLine1Label))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        onView(ViewMatchers.withId(R.id.city))
                .perform(nestedScrollTo())
                .check(matches(withText("vancouver")))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        onView(ViewMatchers.withId(R.id.cityLabel))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

       onView(ViewMatchers.withId(R.id.postalCode))
                .perform(nestedScrollTo())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
                .check(matches(withText("V6Z1L2")));

        onView(ViewMatchers.withId(R.id.postalCodeLabel))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

    }

    @Test
    public void testUpdateTransferMethodFragment_verifyUpdateBankcardTransferMethod() {

        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("transfer_method_list_bankcard_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("transfer_method_update_bankcard_response.json")).mock();


        final CountDownLatch gate = new CountDownLatch(1);
        final BroadcastReceiver br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                gate.countDown();

                StatusTransition statusTransition = intent.getParcelableExtra(
                        "hyperwallet-local-broadcast-payload");
                assertThat("Transition is not valid", statusTransition.getTransition(), is(DE_ACTIVATED));
            }
        };

        // run test
        mActivityTestRule.launchActivity(null);
        LocalBroadcastManager.getInstance(mActivityTestRule.getActivity().getApplicationContext())
                .registerReceiver(br, new IntentFilter("ACTION_HYPERWALLET_TRANSFER_METHOD_DEACTIVATED"));

        // assert
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()));
        onView(withId(R.id.toolbar))
                .check(matches(
                        hasDescendant(withText(R.string.mobileTransferMethodsHeader))));
        onView(withId(R.id.fab)).check(matches(isDisplayed()));

        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.bank_card_font_icon)))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.bank_card)))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withText("Debit Card")))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withText(getEndingIn("0006"))))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withDrawable(R.drawable.ic_three_dots_16dp)))));

        onView(allOf(instanceOf(ImageButton.class), hasSibling(withText(R.string.bank_card)))).perform(click())
                .inRoot(Matchers.<Root>instanceOf(MenuItem.class));

//        Assert both icons
        onView(withDrawable(R.drawable.ic_trash)).check(matches(isDisplayed()));
        onView(withText(R.string.edit)).check(matches(isDisplayed())).perform(click());

        onView(withId(R.id.cardNumber)).check(matches(isDisplayed()));

        onView(ViewMatchers.withId(R.id.cardNumber))
                .perform(nestedScrollTo())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
                .check(matches(withText("****0006")));

        onView(ViewMatchers.withId(R.id.dateOfExpiry))
                .perform(nestedScrollTo())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
                .check(matches(withText("10/24")));

        // cvv has no value when loaded for update
        onView(ViewMatchers.withId(R.id.cvv)).perform(nestedScrollTo())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        onView(ViewMatchers.withId(R.id.cardNumber))
                .perform(nestedScrollTo(), ViewActions.replaceText("1111222233334444"));

        onView(ViewMatchers.withId(R.id.dateOfExpiry))
                .perform(nestedScrollTo(), ViewActions.replaceText("12/25"));

        onView(ViewMatchers.withId(R.id.cvv))
                .perform(nestedScrollTo(), ViewActions.replaceText("321"));

        // update transfer method
        onView(ViewMatchers.withId(R.id.update_transfer_method_button))
                .perform(nestedScrollTo(), ViewActions.click());
    }

    @Test
    public void testUpdateTransferMethodFragment_verifyWireTransferMethod() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("transfer_method_list_wireaccount_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("transfer_method_update_wireaccount_response.json")).mock();


        final CountDownLatch gate = new CountDownLatch(1);
        final BroadcastReceiver br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                gate.countDown();

                StatusTransition statusTransition = intent.getParcelableExtra(
                        "hyperwallet-local-broadcast-payload");
                assertThat("Transition is not valid", statusTransition.getTransition(), is(DE_ACTIVATED));
            }
        };

        // run test
        mActivityTestRule.launchActivity(null);
        LocalBroadcastManager.getInstance(mActivityTestRule.getActivity().getApplicationContext())
                .registerReceiver(br, new IntentFilter("ACTION_HYPERWALLET_TRANSFER_METHOD_DEACTIVATED"));

        // assert
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()));
        onView(withId(R.id.toolbar))
                .check(matches(
                        hasDescendant(withText(R.string.mobileTransferMethodsHeader))));
        onView(withId(R.id.fab)).check(matches(isDisplayed()));

        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.wire_account_font_icon)))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.wire_account)))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withText("Wire Transfer Account")))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withText(getEndingIn("8888"))))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withDrawable(R.drawable.ic_three_dots_16dp)))));

        onView(allOf(instanceOf(ImageButton.class), hasSibling(withText(R.string.wire_account)))).perform(click())
                .inRoot(Matchers.<Root>instanceOf(MenuItem.class));

        //        Assert both icons
        onView(withDrawable(R.drawable.ic_trash)).check(matches(isDisplayed()));
        onView(withText(R.string.edit)).check(matches(isDisplayed())).perform(click());

        onView(withId(R.id.bankAccountId)).check(matches(isDisplayed()));


        onView(withId(R.id.bankAccountId))
                .perform(nestedScrollTo(),ViewActions.replaceText("1234567890"));

        onView(withId(R.id.bankId))
                .perform(nestedScrollTo(),ViewActions.replaceText("ACMTCAXX"));

        // update transfer method
        onView(ViewMatchers.withId(R.id.update_transfer_method_button))
                .perform(nestedScrollTo(), ViewActions.click());
    }

    @Test
    public void testUpdateTransferMethodFragment_verifyUpdatePaypalTransferMethod() {

        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("transfer_method_list_paypal_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("transfer_method_update_paypal_response.json")).mock();


        final CountDownLatch gate = new CountDownLatch(1);
        final BroadcastReceiver br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                gate.countDown();

                StatusTransition statusTransition = intent.getParcelableExtra(
                        "hyperwallet-local-broadcast-payload");
                assertThat("Transition is not valid", statusTransition.getTransition(), is(DE_ACTIVATED));
            }
        };

        // run test
        mActivityTestRule.launchActivity(null);
        LocalBroadcastManager.getInstance(mActivityTestRule.getActivity().getApplicationContext())
                .registerReceiver(br, new IntentFilter("ACTION_HYPERWALLET_TRANSFER_METHOD_DEACTIVATED"));

        // assert
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()));
        onView(withId(R.id.toolbar))
                .check(matches(
                        hasDescendant(withText(R.string.mobileTransferMethodsHeader))));
        onView(withId(R.id.fab)).check(matches(isDisplayed()));

        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.paypal_account_font_icon)))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.paypal_account)))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withText("PayPal Account")))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withDrawable(R.drawable.ic_three_dots_16dp)))));

        onView(allOf(instanceOf(ImageButton.class), hasSibling(withText(R.string.paypal_account)))).perform(click())
                .inRoot(Matchers.<Root>instanceOf(MenuItem.class));

        //        Assert both icons
        onView(withDrawable(R.drawable.ic_trash)).check(matches(isDisplayed()));
        onView(withText(R.string.edit)).check(matches(isDisplayed())).perform(click());

        onView(withId(R.id.email)).check(matches(isDisplayed()));

        onView(withId(R.id.email)).perform(nestedScrollTo())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
                .check(matches(withText("hello@hw.com")));

        onView(withId(R.id.emailLabel)).perform(nestedScrollTo())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        onView(ViewMatchers.withId(R.id.email))
                .perform(nestedScrollTo(), ViewActions.replaceText("update@test.com"));

        // update transfer method
        onView(ViewMatchers.withId(R.id.update_transfer_method_button))
                .perform(nestedScrollTo(), ViewActions.click());

    }

    @Test
    public void testUpdateTransferMethodFragment_verifyUpdatePaperCheckTransferMethod() {

        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("transfer_method_list_paper_check_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("transfer_method_update_papercheck_response.json")).mock();


        final CountDownLatch gate = new CountDownLatch(1);
        final BroadcastReceiver br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                gate.countDown();

                StatusTransition statusTransition = intent.getParcelableExtra(
                        "hyperwallet-local-broadcast-payload");
                assertThat("Transition is not valid", statusTransition.getTransition(), is(DE_ACTIVATED));
            }
        };

        // run test
        mActivityTestRule.launchActivity(null);
        LocalBroadcastManager.getInstance(mActivityTestRule.getActivity().getApplicationContext())
                .registerReceiver(br, new IntentFilter("ACTION_HYPERWALLET_TRANSFER_METHOD_DEACTIVATED"));

        // assert
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()));
        onView(withId(R.id.toolbar))
                .check(matches(
                        hasDescendant(withText(R.string.mobileTransferMethodsHeader))));
        onView(withId(R.id.fab)).check(matches(isDisplayed()));

        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.paper_check_font_icon)))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.paper_check)))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withText("Paper Check")))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withDrawable(R.drawable.ic_three_dots_16dp)))));

        onView(allOf(instanceOf(ImageButton.class), hasSibling(withText(R.string.paper_check)))).perform(click())
                .inRoot(Matchers.<Root>instanceOf(MenuItem.class));

        //        Assert both icons
        onView(withDrawable(R.drawable.ic_trash)).check(matches(isDisplayed()));
        onView(withText(R.string.edit)).check(matches(isDisplayed())).perform(click());


        onView(withId(com.hyperwallet.android.ui.R.id.shippingMethod))
                .check(ViewAssertions.matches(ViewMatchers.withText("Standard Mail")));

        onView(withId(com.hyperwallet.android.ui.R.id.country))
                .check(ViewAssertions.matches(ViewMatchers.withText("Canada")));

        onView(withId(com.hyperwallet.android.ui.R.id.stateProvince))
                .check(ViewAssertions.matches(ViewMatchers.withText("BC")));

        onView(withId(com.hyperwallet.android.ui.R.id.addressLine1))
                .check(ViewAssertions.matches(ViewMatchers.withText("475 howe st")));

        onView(withId(com.hyperwallet.android.ui.R.id.addressLine2))
                .check(ViewAssertions.matches(ViewMatchers.withText("")));

        onView(withId(com.hyperwallet.android.ui.R.id.city))
                .check(ViewAssertions.matches(ViewMatchers.withText("vancouver")));

        onView(withId(com.hyperwallet.android.ui.R.id.postalCode))
                .check(ViewAssertions.matches(ViewMatchers.withText("V6Z1L2")));

        onView(ViewMatchers.withId(R.id.city))
                .perform(nestedScrollTo(), ViewActions.replaceText("UpdateCityTest"));

        // update transfer method
        onView(ViewMatchers.withId(R.id.update_transfer_method_button))
                .perform(nestedScrollTo(), ViewActions.click());
    }

    @Test
    public void testUpdateTransferMethodFragment_verifyUpdateVenmoTransferMethod() {

        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("transfer_method_list_venmo_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("transfer_method_update_venmo_response.json")).mock();


        final CountDownLatch gate = new CountDownLatch(1);
        final BroadcastReceiver br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                gate.countDown();

                StatusTransition statusTransition = intent.getParcelableExtra(
                        "hyperwallet-local-broadcast-payload");
                assertThat("Transition is not valid", statusTransition.getTransition(), is(DE_ACTIVATED));
            }
        };

        // run test
        mActivityTestRule.launchActivity(null);
        LocalBroadcastManager.getInstance(mActivityTestRule.getActivity().getApplicationContext())
                .registerReceiver(br, new IntentFilter("ACTION_HYPERWALLET_TRANSFER_METHOD_DEACTIVATED"));

        // assert
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()));
        onView(withId(R.id.toolbar))
                .check(matches(
                        hasDescendant(withText(R.string.mobileTransferMethodsHeader))));
        onView(withId(R.id.fab)).check(matches(isDisplayed()));

        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.venmo_account_font_icon)))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.venmo_account)))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withText("Venmo Account")))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withText(getEndingIn("1234"))))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withDrawable(R.drawable.ic_three_dots_16dp)))));

        onView(allOf(instanceOf(ImageButton.class), hasSibling(withText(R.string.venmo_account)))).perform(click())
                .inRoot(Matchers.<Root>instanceOf(MenuItem.class));

        //        Assert both icons
        onView(withDrawable(R.drawable.ic_trash)).check(matches(isDisplayed()));
        onView(withText(R.string.edit)).check(matches(isDisplayed())).perform(click());

        onView(withId(R.id.accountIdLabel))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        onView(ViewMatchers.withId(R.id.accountIdLabel))
                .check(ViewAssertions.matches(ViewMatchers.hasDescendant(ViewMatchers.withText("5555555555"))));

        onView(ViewMatchers.withId(R.id.accountId))
                .perform(nestedScrollTo(), ViewActions.replaceText("1234567890"));

        // update transfer method
        onView(ViewMatchers.withId(R.id.update_transfer_method_button))
                .perform(nestedScrollTo(), ViewActions.click());
    }

    private String getEndingIn(String ending) {
        return String.format(InstrumentationRegistry.getInstrumentation().getTargetContext()
                .getString(R.string.endingIn), ending);
    }

}
