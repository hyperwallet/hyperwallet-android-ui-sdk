package com.hyperwallet.android.ui.transfermethod.insight;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import static java.net.HttpURLConnection.HTTP_OK;

import static com.hyperwallet.android.ui.testutils.util.EspressoUtils.nestedScrollTo;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.rule.ActivityTestRule;

import com.hyperwallet.android.ui.R;
import com.hyperwallet.android.ui.common.insight.HyperwalletInsight;
import com.hyperwallet.android.ui.common.repository.EspressoIdlingResource;
import com.hyperwallet.android.ui.common.util.PageGroups;
import com.hyperwallet.android.ui.testutils.rule.HyperwalletExternalResourceManager;
import com.hyperwallet.android.ui.testutils.rule.HyperwalletMockWebServer;
import com.hyperwallet.android.ui.transfermethod.repository.TransferMethodRepositoryFactory;
import com.hyperwallet.android.ui.transfermethod.rule.HyperwalletInsightMockRule;
import com.hyperwallet.android.ui.transfermethod.view.AddTransferMethodActivity;
import com.hyperwallet.android.ui.user.repository.UserRepositoryFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.Map;

public class BankCardInsightTest {
    private static final String INVALID_PATTERN_EXPIRATION_DATE = "1100";
    private static final String VALID_CARD_NUMBER = "4895142232120006";
    private static final String VALID_EXPIRATION_DATE = "1021";
    private static final String VALID_CVV = "022";

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
    @Rule
    public MockitoRule mMockito = MockitoJUnit.rule();
    @Captor
    private ArgumentCaptor<Map<String, String>> mParamsCaptor;

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
        UserRepositoryFactory.clearInstance();
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.getIdlingResource());
    }

    @Test
    public void testAddTransferMethod_verifyInsightEventCreatedOnAddTransferMethodLoad() {
        mActivityTestRule.launchActivity(null);

        verify(mHyperwalletInsightMockRule.getInsight(),
                timeout(5000).times(1)).trackImpression(any(Context.class),
                eq(AddTransferMethodActivity.TAG),
                eq(PageGroups.TRANSFER_METHOD),
                mParamsCaptor.capture());

        assertThat(mParamsCaptor.getValue().get("hyperwallet_ea_country"), is("US"));
        assertThat(mParamsCaptor.getValue().get("hyperwallet_ea_currency"), is("USD"));
        assertThat(mParamsCaptor.getValue().get("hyperwallet_ea_type"), is("BANK_CARD"));
        assertThat(mParamsCaptor.getValue().get("hyperwallet_ea_profile_type"), is("INDIVIDUAL"));

    }

    @Test
    public void testAddTransferMethod_verifyInsightEventCreatedOnClickCreateTransferMethod() {
        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.add_transfer_method_button)).perform(nestedScrollTo(), click());

        verify(mHyperwalletInsightMockRule.getInsight(),
                timeout(5000).times(1)).trackClick(any(Context.class),
                eq(AddTransferMethodActivity.TAG),
                eq(PageGroups.TRANSFER_METHOD),
                eq(HyperwalletInsight.LINK_SELECT_TRANSFER_METHOD_CREATE),
                mParamsCaptor.capture());

        assertThat(mParamsCaptor.getValue().get("hyperwallet_ea_country"), is("US"));
        assertThat(mParamsCaptor.getValue().get("hyperwallet_ea_currency"), is("USD"));
        assertThat(mParamsCaptor.getValue().get("hyperwallet_ea_type"), is("BANK_CARD"));
        assertThat(mParamsCaptor.getValue().get("hyperwallet_ea_profile_type"), is("INDIVIDUAL"));
    }

    @Test
    public void testAddTransferMethod_verifyInsightEventCreatedOnValidationErrorExpiryDate() {
        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.cardNumber)).perform(nestedScrollTo(), replaceText(VALID_CARD_NUMBER));
        onView(withId(R.id.dateOfExpiry)).perform(nestedScrollTo(), replaceText(INVALID_PATTERN_EXPIRATION_DATE));
        onView(withId(R.id.cvv)).perform(nestedScrollTo(), replaceText(VALID_CVV));

        onView(withId(R.id.add_transfer_method_button)).perform(nestedScrollTo(), click());

        verify(mHyperwalletInsightMockRule.getInsight(),
                timeout(5000).times(1)).trackError(any(Context.class),
                eq(AddTransferMethodActivity.TAG),
                eq(PageGroups.TRANSFER_METHOD),
                mParamsCaptor.capture());

        assertThat(mParamsCaptor.getValue().get("error_type"), is("FORM"));
        assertThat(mParamsCaptor.getValue().get("error_message"), is("Expiration Date is invalid."));
        assertThat(mParamsCaptor.getValue().get("erfd"), is("dateOfExpiry"));
        assertThat(mParamsCaptor.getValue().get("hyperwallet_ea_country"), is("US"));
        assertThat(mParamsCaptor.getValue().get("hyperwallet_ea_currency"), is("USD"));
        assertThat(mParamsCaptor.getValue().get("hyperwallet_ea_type"), is("BANK_CARD"));
        assertThat(mParamsCaptor.getValue().get("hyperwallet_ea_profile_type"), is("INDIVIDUAL"));
    }

    @Test
    public void testAddTransferMethod_verifyInsightEventCreatedOnAddTransferMethodSuccessful() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("bank_card_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.cardNumber)).perform(nestedScrollTo(), replaceText(VALID_CARD_NUMBER));
        onView(withId(R.id.dateOfExpiry)).perform(nestedScrollTo(), replaceText(VALID_EXPIRATION_DATE));
        onView(withId(R.id.cvv)).perform(nestedScrollTo(), replaceText(VALID_CVV));

        onView(withId(R.id.add_transfer_method_button)).perform(nestedScrollTo(), click());

        verify(mHyperwalletInsightMockRule.getInsight(),
                timeout(7000).times(2)).trackImpression(any(Context.class),
                eq(AddTransferMethodActivity.TAG),
                eq(PageGroups.TRANSFER_METHOD),
                mParamsCaptor.capture());

        assertThat(mParamsCaptor.getValue().get("goal"), is("transfer-method-created"));
        assertThat(mParamsCaptor.getValue().get("hyperwallet_ea_country"), is("US"));
        assertThat(mParamsCaptor.getValue().get("hyperwallet_ea_currency"), is("USD"));
        assertThat(mParamsCaptor.getValue().get("hyperwallet_ea_type"), is("BANK_CARD"));
        assertThat(mParamsCaptor.getValue().get("hyperwallet_ea_profile_type"), is("INDIVIDUAL"));
    }
}