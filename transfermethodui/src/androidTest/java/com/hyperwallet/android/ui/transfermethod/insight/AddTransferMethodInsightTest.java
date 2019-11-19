package com.hyperwallet.android.ui.transfermethod.insight;

import static androidx.test.espresso.matcher.ViewMatchers.assertThat;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import static java.net.HttpURLConnection.HTTP_OK;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.rule.ActivityTestRule;

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

public class AddTransferMethodInsightTest {
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
                    intent.putExtra("TRANSFER_METHOD_TYPE", "BANK_ACCOUNT");
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
                .getResourceContent("successful_tmc_fields_bank_account_response.json")).mock();
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
        assertThat(mParamsCaptor.getValue().get("hyperwallet_ea_type"), is("BANK_ACCOUNT"));
        assertThat(mParamsCaptor.getValue().get("hyperwallet_ea_profile_type"), is("INDIVIDUAL"));

    }


}
