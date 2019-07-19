package com.hyperwallet.android.ui.transfermethod;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.hasSibling;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.concurrent.TimeUnit.SECONDS;

import static com.hyperwallet.android.model.HyperwalletStatusTransition.StatusDefinition.DE_ACTIVATED;
import static com.hyperwallet.android.ui.util.EspressoUtils.atPosition;
import static com.hyperwallet.android.ui.util.EspressoUtils.withDrawable;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.test.espresso.Root;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.model.HyperwalletStatusTransition;
import com.hyperwallet.android.ui.R;
import com.hyperwallet.android.ui.rule.HyperwalletExternalResourceManager;
import com.hyperwallet.android.ui.rule.HyperwalletMockWebServer;
import com.hyperwallet.android.ui.transfermethod.repository.TransferMethodRepositoryFactory;
import com.hyperwallet.android.ui.util.RecyclerViewCountAssertion;
import com.hyperwallet.android.ui.util.TestAuthenticationProvider;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

@RunWith(AndroidJUnit4.class)
public class ListTransferMethodTest {

    @ClassRule
    public static HyperwalletExternalResourceManager sResourceManager = new HyperwalletExternalResourceManager();
    @Rule
    public HyperwalletMockWebServer mMockWebServer = new HyperwalletMockWebServer(8080);
    @Rule
    public ActivityTestRule<ListTransferMethodActivity> mActivityTestRule =
            new ActivityTestRule<>(ListTransferMethodActivity.class, true, false);

    @Before
    public void setup() {
        Hyperwallet.getInstance(new TestAuthenticationProvider());

        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("authentication_token_response.json")).mock();
    }

    @After
    public void cleanup() {
        TransferMethodRepositoryFactory.clearInstance();
    }

    @Test
    public void testListTransferMethod_userHasMultipleTransferMethods() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("transfer_method_list_response.json")).mock();

        // run test
        mActivityTestRule.launchActivity(null);

        // assert
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText(R.string.title_activity_list_transfer_method)));
        onView(withId(R.id.fab)).check(matches(isDisplayed()));

        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.bank_account_font_icon)))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.bank_account)))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withText("United States")))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withText("Ending on 1332")))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withDrawable(R.drawable.ic_three_dots_16dp)))));

        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(1, hasDescendant(withText(R.string.bank_card_font_icon)))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(1, hasDescendant(withText(R.string.bank_card)))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(1, hasDescendant(withText("United States")))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(1, hasDescendant(withText("Ending on 0006")))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(1, hasDescendant(withDrawable(R.drawable.ic_three_dots_16dp)))));

        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(2, hasDescendant(withText(R.string.wire_account_font_icon)))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(2, hasDescendant(withText(R.string.wire_account)))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(2, hasDescendant(withText("United States")))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(2, hasDescendant(withText("Ending on 8337")))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(2, hasDescendant(withDrawable(R.drawable.ic_three_dots_16dp)))));

        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(3, hasDescendant(withText(R.string.paper_check_font_icon)))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(3, hasDescendant(withText(R.string.paper_check)))));
        onView(withId(R.id.list_transfer_method_item)).check(matches(atPosition(3, hasDescendant(withText("Canada")))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(3, hasDescendant(withText("")))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(3, hasDescendant(withDrawable(R.drawable.ic_three_dots_16dp)))));

        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(4, hasDescendant(withText(R.string.prepaid_card_font_icon)))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(4, hasDescendant(withText(R.string.prepaid_card)))));
        onView(withId(R.id.list_transfer_method_item)).check(matches(atPosition(4, hasDescendant(withText("Canada")))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(4, hasDescendant(withText("Ending on 3187")))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(4, hasDescendant(withDrawable(R.drawable.ic_three_dots_16dp)))));

        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(5, hasDescendant(withText(R.string.paypal_account_font_icon)))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(5, hasDescendant(withText(R.string.paypal_account)))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(5, hasDescendant(withText("United States")))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(5, hasDescendant(withText("honey.thigpen@ukbuilder.com")))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(5, hasDescendant(withDrawable(R.drawable.ic_three_dots_16dp)))));

        onView(withId(R.id.list_transfer_method_item)).check(new RecyclerViewCountAssertion(6));

    }

    @Test
    public void testListTransferMethod_userHasSingleTransferMethod() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("transfer_method_list_single_bank_account_response.json")).mock();

        // run test
        mActivityTestRule.launchActivity(null);

        // assert
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()));
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText(R.string.title_activity_list_transfer_method)));
        onView(withId(R.id.fab)).check(matches(isDisplayed()));

        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.bank_account_font_icon)))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.bank_account)))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withText("United States")))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withText("Ending on 1332")))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withDrawable(R.drawable.ic_three_dots_16dp)))));
        onView(withId(R.id.list_transfer_method_item)).check(new RecyclerViewCountAssertion(1));

    }

    @Test
    public void testListTransferMethod_userHasNoTransferMethods() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_NO_CONTENT).withBody("").mock();

        // run test
        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.toolbar)).check(matches(isDisplayed()));
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText(R.string.title_activity_list_transfer_method)));
        onView(withId(R.id.fab)).check(matches(isDisplayed()));

        onView(withId(R.id.empty_transfer_method_list_layout)).check(matches(isDisplayed()));
        onView(withText(R.string.empty_list_transfer_method_information)).check(matches(isDisplayed()));

    }

    @Test
    public void testListTransferMethod_removeBankAccount() throws InterruptedException {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("transfer_method_list_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("transfer_method_deactivate_success.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("transfer_method_list_deleted_response.json")).mock();

        final CountDownLatch gate = new CountDownLatch(1);
        final BroadcastReceiver br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                gate.countDown();

                HyperwalletStatusTransition statusTransition = intent.getParcelableExtra(
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
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText(R.string.title_activity_list_transfer_method)));
        onView(withId(R.id.fab)).check(matches(isDisplayed()));

        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.bank_account_font_icon)))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.bank_account)))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withText("United States")))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withText("Ending on 1332")))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withDrawable(R.drawable.ic_three_dots_16dp)))));

        onView(allOf(instanceOf(ImageButton.class), hasSibling(withText(R.string.bank_account)))).perform(click())
                .inRoot(Matchers.<Root>instanceOf(MenuItem.class));
        onView(withDrawable(R.drawable.ic_trash)).check(matches(isDisplayed()));
        onView(withText(R.string.menu_remove_account)).check(matches(isDisplayed())).perform(click());

        // confirmation dialog is shown before deletion
        onView(withText(R.string.transfer_method_remove_confirmation_title)).check(matches(isDisplayed()));
        onView(withText(R.string.transfer_method_remove_confirmation)).check(matches(isDisplayed()));
        onView(withId(android.R.id.button1)).check(matches(withText(R.string.remove_button_label)));
        onView(withId(android.R.id.button2)).check(matches(withText(R.string.cancel_button_label)));

        onView(withId(android.R.id.button1)).perform(click());

        gate.await(5, SECONDS);
        LocalBroadcastManager.getInstance(mActivityTestRule.getActivity().getApplicationContext()).unregisterReceiver(
                br);
        assertThat("Action is not broadcasted", gate.getCount(), is(0L));

        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.bank_card_font_icon)))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.bank_card)))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withText("United States")))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withText("Ending on 0006")))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withDrawable(R.drawable.ic_three_dots_16dp)))));

        onView(withId(R.id.list_transfer_method_item)).check(new RecyclerViewCountAssertion(4));

    }

    @Test
    public void testListTransferMethod_removeBankCard() throws InterruptedException {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("transfer_method_list_single_bank_card_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("transfer_method_deactivate_success.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_NO_CONTENT).withBody("").mock();


        final CountDownLatch gate = new CountDownLatch(1);
        final BroadcastReceiver br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                gate.countDown();

                HyperwalletStatusTransition statusTransition = intent.getParcelableExtra(
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
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText(R.string.title_activity_list_transfer_method)));
        onView(withId(R.id.fab)).check(matches(isDisplayed()));

        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.bank_card_font_icon)))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.bank_card)))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withText("United States")))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withText("Ending on 0006")))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withDrawable(R.drawable.ic_three_dots_16dp)))));

        onView(allOf(instanceOf(ImageButton.class), hasSibling(withText(R.string.bank_card)))).perform(click())
                .inRoot(Matchers.<Root>instanceOf(MenuItem.class));
        onView(withDrawable(R.drawable.ic_trash)).check(matches(isDisplayed()));
        onView(withText(R.string.menu_remove_account)).check(matches(isDisplayed())).perform(click());

        // confirmation dialog is shown before deletion
        onView(withText(R.string.transfer_method_remove_confirmation_title)).check(matches(isDisplayed()));
        onView(withText(R.string.transfer_method_remove_confirmation)).check(matches(isDisplayed()));
        onView(withId(android.R.id.button1)).check(matches(withText(R.string.remove_button_label)));
        onView(withId(android.R.id.button2)).check(matches(withText(R.string.cancel_button_label)));

        onView(withId(android.R.id.button1)).perform(click());

        gate.await(5, SECONDS);
        LocalBroadcastManager.getInstance(mActivityTestRule.getActivity().getApplicationContext()).unregisterReceiver(
                br);
        assertThat("Action is not broadcasted", gate.getCount(), is(0L));

        onView(withId(R.id.empty_transfer_method_list_layout)).check(matches(isDisplayed()));
        onView(withText(R.string.empty_list_transfer_method_information)).check(matches(isDisplayed()));

    }

    @Test
    public void testListTransferMethod_removePayPalAccount() throws InterruptedException {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("transfer_method_list_single_paypal_account_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("transfer_method_deactivate_success.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_NO_CONTENT).withBody("").mock();


        final CountDownLatch gate = new CountDownLatch(1);
        final BroadcastReceiver br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                gate.countDown();

                HyperwalletStatusTransition statusTransition = intent.getParcelableExtra(
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
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText(R.string.title_activity_list_transfer_method)));
        onView(withId(R.id.fab)).check(matches(isDisplayed()));

        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.paypal_account_font_icon)))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.paypal_account)))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withText("United States")))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withDrawable(R.drawable.ic_three_dots_16dp)))));

        onView(allOf(instanceOf(ImageButton.class), hasSibling(withText(R.string.paypal_account)))).perform(click())
                .inRoot(Matchers.<Root>instanceOf(MenuItem.class));
        onView(withDrawable(R.drawable.ic_trash)).check(matches(isDisplayed()));
        onView(withText(R.string.menu_remove_account)).check(matches(isDisplayed())).perform(click());

        // confirmation dialog is shown before deletion
        onView(withText(R.string.transfer_method_remove_confirmation_title)).check(matches(isDisplayed()));
        onView(withText(R.string.transfer_method_remove_confirmation)).check(matches(isDisplayed()));
        onView(withId(android.R.id.button1)).check(matches(withText(R.string.remove_button_label)));
        onView(withId(android.R.id.button2)).check(matches(withText(R.string.cancel_button_label)));

        onView(withId(android.R.id.button1)).perform(click());

        gate.await(5, SECONDS);
        LocalBroadcastManager.getInstance(mActivityTestRule.getActivity().getApplicationContext()).unregisterReceiver(
                br);
        assertThat("Action is not broadcasted", gate.getCount(), is(0L));

        onView(withId(R.id.empty_transfer_method_list_layout)).check(matches(isDisplayed()));
        onView(withText(R.string.empty_list_transfer_method_information)).check(matches(isDisplayed()));

    }

    @Test
    public void testListTransferMethod_removeBankAccountClickCancel() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("transfer_method_list_response.json")).mock();

        // run test
        mActivityTestRule.launchActivity(null);
        // assert
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()));
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText(R.string.title_activity_list_transfer_method)));
        onView(withId(R.id.fab)).check(matches(isDisplayed()));

        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.bank_account_font_icon)))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.bank_account)))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withText("United States")))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withText("Ending on 1332")))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withDrawable(R.drawable.ic_three_dots_16dp)))));

        onView(allOf(instanceOf(ImageButton.class), hasSibling(withText(R.string.bank_account)))).perform(click())
                .inRoot(Matchers.<Root>instanceOf(MenuItem.class));
        onView(withDrawable(R.drawable.ic_trash)).check(matches(isDisplayed()));
        onView(withText(R.string.menu_remove_account)).check(matches(isDisplayed())).perform(click());

        // confirmation dialog is shown before deletion
        onView(withText(R.string.transfer_method_remove_confirmation_title)).check(matches(isDisplayed()));
        onView(withText(R.string.transfer_method_remove_confirmation)).check(matches(isDisplayed()));
        onView(withId(android.R.id.button1)).check(matches(withText(R.string.remove_button_label)));
        onView(withId(android.R.id.button2)).check(matches(withText(R.string.cancel_button_label)));

        onView(withId(android.R.id.button2)).perform(click());

        // confirmation dialog should be dismissed and does not exist in ui
        onView(withText(R.string.transfer_method_remove_confirmation_title)).check(doesNotExist());
        onView(withText(R.string.transfer_method_remove_confirmation)).check(doesNotExist());

        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.bank_account_font_icon)))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.bank_account)))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withText("United States")))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withText("Ending on 1332")))));
        onView(withId(R.id.list_transfer_method_item)).check(
                matches(atPosition(0, hasDescendant(withDrawable(R.drawable.ic_three_dots_16dp)))));
    }

}
