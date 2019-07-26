package com.hyperwallet.android.ui.transfer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static java.net.HttpURLConnection.HTTP_OK;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.ui.common.repository.EspressoIdlingResource;
import com.hyperwallet.android.ui.transfer.rule.HyperwalletExternalResourceManager;
import com.hyperwallet.android.ui.transfer.rule.HyperwalletMockWebServer;
import com.hyperwallet.android.ui.transfer.util.TestAuthenticationProvider;
import com.hyperwallet.android.ui.transfer.view.ListTransferDestinationActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class ListTransferDestinationTest {

    @ClassRule
    public static HyperwalletExternalResourceManager sResourceManager = new HyperwalletExternalResourceManager();
    @Rule
    public HyperwalletMockWebServer mMockWebServer = new HyperwalletMockWebServer(8080);
    @Rule
    public ActivityTestRule<ListTransferDestinationActivity> mActivityTestRule =
            new ActivityTestRule<>(ListTransferDestinationActivity.class, true, false);
    @Rule
    public IntentsTestRule<ListTransferDestinationActivity> mIntentsTestRule =
            new IntentsTestRule<>(ListTransferDestinationActivity.class, true, false);

    @Before
    public void setup() {
        Hyperwallet.getInstance(new TestAuthenticationProvider());

        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("authentication_token_response.json")).mock();

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
    public void testSelectTransferMethod_verifyTransferMethodsListEmptyProcessing() {
//        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
//                .getResourceContent("user_response.json")).mock();
//
        mActivityTestRule.launchActivity(null);

//        onView(withId(R.id.select_transfer_method_currency_value)).check(matches(withText("USD")));
    }

}
