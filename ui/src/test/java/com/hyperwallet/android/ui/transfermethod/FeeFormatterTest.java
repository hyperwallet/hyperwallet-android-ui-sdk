package com.hyperwallet.android.ui.transfermethod;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.res.Resources;

import com.hyperwallet.android.hyperwallet_ui.R;
import com.hyperwallet.android.model.meta.HyperwalletFee;
import com.hyperwallet.android.ui.rule.HyperwalletExternalResourceManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RobolectricTestRunner;

import java.util.Arrays;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
public class FeeFormatterTest {

    @Rule
    public MockitoRule mockito = MockitoJUnit.rule();
    @Captor
    private ArgumentCaptor<Integer> resourceIdCaptor;
    @Captor
    private ArgumentCaptor<Object> formatterArgumentCapture;
    @Mock
    private Context context;
    @Mock
    private Resources resources;
    @Rule
    public HyperwalletExternalResourceManager externalResourceManager = new HyperwalletExternalResourceManager();

    private HyperwalletFee mFlatFee;
    private JSONObject mJSONObject;

    @Before
    public void setUp() throws JSONException {
        mJSONObject = new JSONObject(externalResourceManager.getResourceContent("fee_information.json"));
        mFlatFee = new HyperwalletFee(mJSONObject.getJSONObject("FEE_ONE").getJSONArray("nodes")
                .getJSONObject(0));
        when(context.getResources()).thenReturn(resources);
    }

    @Test
    public void testGetFormattedFee_returnsFlatFormattedFee() {
        FeeFormatter.getFormattedFee(context, Arrays.asList(mFlatFee));

        verify(resources).getString(resourceIdCaptor.capture(), formatterArgumentCapture.capture());
        int resourceIdCaptorValue = resourceIdCaptor.getValue();
        List<Object> argumentList = formatterArgumentCapture.getAllValues();
        assertThat(resourceIdCaptorValue, is(R.string.fee_flat_formatter));
        assertThat(argumentList.size(), is(2));
    }

    @Test
    public void testGetFormattedFee_returnsPercentFormattedFeeWithMinAndMax() {
        HyperwalletFee fee = new HyperwalletFee(mJSONObject.optJSONObject("FEE_TWO").optJSONArray("nodes")
                .optJSONObject(0));

        FeeFormatter.getFormattedFee(context, Arrays.asList(fee));

        verify(resources).getString(resourceIdCaptor.capture(), formatterArgumentCapture.capture());
        int resourceIdCaptorValue = resourceIdCaptor.getValue();
        List<Object> argumentList = formatterArgumentCapture.getAllValues();
        assertThat(resourceIdCaptorValue, is(R.string.fee_percent_formatter));
        assertThat(argumentList.size(), is(4));
    }

    @Test
    public void testGetFormattedFee_returnsPercentFormattedFeeWithMinOnly() {
        HyperwalletFee fee = new HyperwalletFee(mJSONObject.optJSONObject("FEE_THREE").optJSONArray("nodes")
                .optJSONObject(0));

        FeeFormatter.getFormattedFee(context, Arrays.asList(fee));

        verify(resources).getString(resourceIdCaptor.capture(), formatterArgumentCapture.capture());
        int resourceIdCaptorValue = resourceIdCaptor.getValue();
        List<Object> argumentList = formatterArgumentCapture.getAllValues();
        assertThat(resourceIdCaptorValue, is(R.string.fee_percent_only_min_formatter));
        assertThat(argumentList.size(), is(3));
    }

    @Test
    public void testGetFormattedFee_returnsPercentFormattedFeeWithMaxOnly() {
        HyperwalletFee fee = new HyperwalletFee(mJSONObject.optJSONObject("FEE_FOUR").optJSONArray("nodes")
                .optJSONObject(0));
        FeeFormatter.getFormattedFee(context, Arrays.asList(fee));

        verify(resources).getString(resourceIdCaptor.capture(), formatterArgumentCapture.capture());
        int resourceIdCaptorValue = resourceIdCaptor.getValue();
        List<Object> argumentList = formatterArgumentCapture.getAllValues();
        assertThat(resourceIdCaptorValue, is(R.string.fee_percent_only_max_formatter));
        assertThat(argumentList.size(), is(3));
    }

    @Test
    public void testGetFormattedFee_returnsPercentFormattedFeeWithoutMinAndMax() {
        HyperwalletFee fee = new HyperwalletFee(mJSONObject.optJSONObject("FEE_FIVE").optJSONArray("nodes")
                .optJSONObject(0));

        FeeFormatter.getFormattedFee(context, Arrays.asList(fee));

        verify(resources).getString(resourceIdCaptor.capture(), formatterArgumentCapture.capture());
        int resourceIdCaptorValue = resourceIdCaptor.getValue();
        List<Object> argumentList = formatterArgumentCapture.getAllValues();
        assertThat(resourceIdCaptorValue, is(R.string.fee_percent_no_min_and_max_formatter));
        assertThat(argumentList.size(), is(1));
    }

    @Test
    public void testGetFormattedFee_returnsPercentAndFlatFormattedFeeWithMinAndMax() {
        HyperwalletFee percentFee = new HyperwalletFee(mJSONObject.optJSONObject("FEE_SIX").optJSONArray("nodes")
                .optJSONObject(0));

        FeeFormatter.getFormattedFee(context, Arrays.asList(mFlatFee, percentFee));

        verify(resources).getString(resourceIdCaptor.capture(), formatterArgumentCapture.capture());
        int resourceIdCaptorValue = resourceIdCaptor.getValue();
        List<Object> argumentList = formatterArgumentCapture.getAllValues();
        assertThat(resourceIdCaptorValue, is(R.string.fee_mix_formatter));
        assertThat(argumentList.size(), is(5));
    }

    @Test
    public void testGetFormattedFee_returnsPercentAndFlatFormattedFeeWithMinOnly() {
        HyperwalletFee percentFee = new HyperwalletFee(mJSONObject.optJSONObject("FEE_SEVEN").optJSONArray("nodes")
                .optJSONObject(0));

        FeeFormatter.getFormattedFee(context, Arrays.asList(mFlatFee, percentFee));

        verify(resources).getString(resourceIdCaptor.capture(), formatterArgumentCapture.capture());
        int resourceIdCaptorValue = resourceIdCaptor.getValue();
        List<Object> argumentList = formatterArgumentCapture.getAllValues();
        assertThat(resourceIdCaptorValue, is(R.string.fee_mix_only_min_formatter));
        assertThat(argumentList.size(), is(4));
    }
}
