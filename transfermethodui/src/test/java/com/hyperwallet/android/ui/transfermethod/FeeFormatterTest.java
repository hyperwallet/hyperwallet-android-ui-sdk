package com.hyperwallet.android.ui.transfermethod;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.res.Resources;

import com.hyperwallet.android.model.graphql.Fee;
import com.hyperwallet.android.model.graphql.ProcessingTime;
import com.hyperwallet.android.ui.R;
import com.hyperwallet.android.ui.testutils.rule.HyperwalletExternalResourceManager;
import com.hyperwallet.android.ui.transfermethod.view.FeeFormatter;

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
import java.util.Collections;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
public class FeeFormatterTest {

    @Rule
    public MockitoRule mockito = MockitoJUnit.rule();
    @Rule
    public HyperwalletExternalResourceManager externalResourceManager = new HyperwalletExternalResourceManager();
    @Captor
    private ArgumentCaptor<Integer> resourceIdCaptor;
    @Captor
    private ArgumentCaptor<Object> formatterArgumentCapture;
    @Mock
    private Context context;
    @Mock
    private Resources resources;
    private Fee mFlatFee;
    private JSONObject mJSONObject;
    @Mock
    private FeeFormatter mFeeFormatter;

    @Before
    public void setUp() throws JSONException {
        mJSONObject = new JSONObject(externalResourceManager.getResourceContent("fee_information.json"));
        mFlatFee = new Fee(mJSONObject.getJSONObject("FEE_ONE").getJSONArray("nodes")
                .getJSONObject(0));
        when(context.getResources()).thenReturn(resources);
    }

    @Test
    public void testGetFormattedFee_returnsFlatFormattedFee() {
        FeeFormatter.getFormattedFee(context, Arrays.asList(mFlatFee));
        ArgumentCaptor<Integer> resourceIdCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Object[]> formatterArgumentCapture = ArgumentCaptor.forClass(Object[].class);
        verify(resources).getString(resourceIdCaptor.capture(), formatterArgumentCapture.capture());
        Integer resourceIdCaptorValue = resourceIdCaptor.getValue();
        assertThat(resourceIdCaptorValue, is(R.string.fee_flat_formatter));
        Object[] argumentList = formatterArgumentCapture.getValue();
        assertThat(argumentList.length, is(2));
    }

    @Test
    public void testGetFormattedFee_returnsPercentFormattedFeeWithMinAndMax() {
        Fee fee = new Fee(mJSONObject.optJSONObject("FEE_TWO").optJSONArray("nodes")
                .optJSONObject(0));

        FeeFormatter.getFormattedFee(context, Arrays.asList(fee));
        ArgumentCaptor<Integer> resourceIdCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Object[]> formatterArgumentCapture = ArgumentCaptor.forClass(Object[].class);

        verify(resources).getString(resourceIdCaptor.capture(), formatterArgumentCapture.capture());
        int resourceIdCaptorValue = resourceIdCaptor.getValue();
        Object[] argumentList = formatterArgumentCapture.getValue();
        assertThat(resourceIdCaptorValue, is(R.string.fee_percent_formatter));
        assertThat(argumentList.length, is(4));
    }

    @Test
    public void testGetFormattedFee_returnsPercentFormattedFeeWithMinOnly() {
        Fee fee = new Fee(mJSONObject.optJSONObject("FEE_THREE").optJSONArray("nodes")
                .optJSONObject(0));

        FeeFormatter.getFormattedFee(context, Arrays.asList(fee));
        ArgumentCaptor<Integer> resourceIdCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Object[]> formatterArgumentCapture = ArgumentCaptor.forClass(Object[].class);
        verify(resources).getString(resourceIdCaptor.capture(), formatterArgumentCapture.capture());
        int resourceIdCaptorValue = resourceIdCaptor.getValue();
        Object[] argumentList = formatterArgumentCapture.getValue();
        assertThat(resourceIdCaptorValue, is(R.string.fee_percent_only_min_formatter));
        assertThat(argumentList.length, is(3));
    }

    @Test
    public void testGetFormattedFee_returnsPercentFormattedFeeWithMaxOnly() {
        Fee fee = new Fee(mJSONObject.optJSONObject("FEE_FOUR").optJSONArray("nodes")
                .optJSONObject(0));
        FeeFormatter.getFormattedFee(context, Arrays.asList(fee));
        ArgumentCaptor<Integer> resourceIdCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Object[]> formatterArgumentCapture = ArgumentCaptor.forClass(Object[].class);
        verify(resources).getString(resourceIdCaptor.capture(), formatterArgumentCapture.capture());
        int resourceIdCaptorValue = resourceIdCaptor.getValue();
        Object[] argumentList = formatterArgumentCapture.getValue();
        assertThat(resourceIdCaptorValue, is(R.string.fee_percent_only_max_formatter));
        assertThat(argumentList.length, is(3));
    }

    @Test
    public void testGetFormattedFee_returnsPercentFormattedFeeWithoutMinAndMax() {
        Fee fee = new Fee(mJSONObject.optJSONObject("FEE_FIVE").optJSONArray("nodes")
                .optJSONObject(0));

        FeeFormatter.getFormattedFee(context, Arrays.asList(fee));
        ArgumentCaptor<Integer> resourceIdCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Object[]> formatterArgumentCapture = ArgumentCaptor.forClass(Object[].class);
        verify(resources).getString(resourceIdCaptor.capture(), formatterArgumentCapture.capture());
        int resourceIdCaptorValue = resourceIdCaptor.getValue();
        Object[] argumentList = formatterArgumentCapture.getValue();
        assertThat(resourceIdCaptorValue, is(R.string.fee_percent_no_min_and_max_formatter));
        assertThat(argumentList.length, is(1));
    }

    @Test
    public void testGetFormattedFee_returnsPercentAndFlatFormattedFeeWithMinAndMax() {
        Fee percentFee = new Fee(mJSONObject.optJSONObject("FEE_SIX").optJSONArray("nodes")
                .optJSONObject(0));

        FeeFormatter.getFormattedFee(context, Arrays.asList(mFlatFee, percentFee));
        ArgumentCaptor<Integer> resourceIdCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Object[]> formatterArgumentCapture = ArgumentCaptor.forClass(Object[].class);
        verify(resources).getString(resourceIdCaptor.capture(), formatterArgumentCapture.capture());
        int resourceIdCaptorValue = resourceIdCaptor.getValue();
        Object[] argumentList = formatterArgumentCapture.getValue();
        assertThat(resourceIdCaptorValue, is(R.string.fee_mix_formatter));
        assertThat(argumentList.length, is(5));
    }

    @Test
    public void testGetFormattedFee_returnsPercentAndFlatFormattedFeeWithMinOnly() {
        Fee percentFee = new Fee(mJSONObject.optJSONObject("FEE_SEVEN").optJSONArray("nodes")
                .optJSONObject(0));

        FeeFormatter.getFormattedFee(context, Arrays.asList(mFlatFee, percentFee));
        ArgumentCaptor<Integer> resourceIdCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Object[]> formatterArgumentCapture = ArgumentCaptor.forClass(Object[].class);
        verify(resources).getString(resourceIdCaptor.capture(), formatterArgumentCapture.capture());
        int resourceIdCaptorValue = resourceIdCaptor.getValue();
        Object[] argumentList = formatterArgumentCapture.getValue();
        assertThat(resourceIdCaptorValue, is(R.string.fee_mix_only_min_formatter));
        assertThat(argumentList.length, is(4));
    }

    @Test
    public void testGetFormattedFee_returnsFlatNoFee() {
        Fee flatFee = new Fee(mJSONObject.optJSONObject("FEE_NINE").optJSONArray("nodes").optJSONObject(0));
        FeeFormatter.getFormattedFee(context, Arrays.asList(flatFee));
        verify(resources).getString(resourceIdCaptor.capture());
        int resourceIdCaptorValue = resourceIdCaptor.getValue();
        assertThat(resourceIdCaptorValue, is(R.string.noFee));
    }

    @Test
    public void testGetFormattedFee_returnsPercentNoFee() {
        Fee percentFee = new Fee(mJSONObject.optJSONObject("FEE_EIGHT").optJSONArray("nodes").optJSONObject(0));
        FeeFormatter.getFormattedFee(context, Arrays.asList(percentFee));
        verify(resources).getString(resourceIdCaptor.capture());
        int resourceIdCaptorValue = resourceIdCaptor.getValue();
        assertThat(resourceIdCaptorValue, is(R.string.noFee));
    }

    @Test
    public void testGetFormattedFee_returnsMixedNoFee() {
        Fee flatFee = new Fee(mJSONObject.optJSONObject("FEE_EIGHT").optJSONArray("nodes").optJSONObject(0));
        Fee percentFee = new Fee(mJSONObject.optJSONObject("FEE_NINE").optJSONArray("nodes").optJSONObject(0));

        FeeFormatter.getFormattedFee(context, Arrays.asList(flatFee,percentFee));
        verify(resources).getString(resourceIdCaptor.capture());
        int resourceIdCaptorValue = resourceIdCaptor.getValue();
        assertThat(resourceIdCaptorValue, is(R.string.noFee));
    }

    @Test
    public void testGetFormattedFee_returnsFlatFee() {
        Fee percentFee = new Fee(mJSONObject.optJSONObject("FEE_EIGHT").optJSONArray("nodes").optJSONObject(0));
        FeeFormatter.getFormattedFee(context, Arrays.asList(mFlatFee,percentFee));
        ArgumentCaptor<Integer> resourceIdCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Object[]> formatterArgumentCapture = ArgumentCaptor.forClass(Object[].class);
        verify(resources).getString(resourceIdCaptor.capture(), formatterArgumentCapture.capture());
        int resourceIdCaptorValue = resourceIdCaptor.getValue();
        Object[] argumentList = formatterArgumentCapture.getValue();
        assertThat(resourceIdCaptorValue, is(R.string.fee_flat_formatter));
        assertThat(argumentList.length, is(2));
    }

    @Test
    public void testGetFormattedFee_returnsFlatFeeWithMinAndMax() {
        Fee percentFee = new Fee(mJSONObject.optJSONObject("FEE_TEN").optJSONArray("nodes").optJSONObject(0));
        FeeFormatter.getFormattedFee(context, Arrays.asList(mFlatFee,percentFee));
        ArgumentCaptor<Integer> resourceIdCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Object[]> formatterArgumentCapture = ArgumentCaptor.forClass(Object[].class);

        verify(resources).getString(resourceIdCaptor.capture(), formatterArgumentCapture.capture());
        int resourceIdCaptorValue = resourceIdCaptor.getValue();
        Object[] argumentList = formatterArgumentCapture.getValue();
        assertThat(resourceIdCaptorValue, is(R.string.fee_flat_formatter));
        assertThat(argumentList.length, is(2));
    }

    @Test
    public void testGetFormattedFee_returnsFlatFeeWithMaxOnly() {
        Fee percentFee = new Fee(mJSONObject.optJSONObject("FEE_TWELVE").optJSONArray("nodes").optJSONObject(0));
        FeeFormatter.getFormattedFee(context, Arrays.asList(mFlatFee,percentFee));
        ArgumentCaptor<Integer> resourceIdCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Object[]> formatterArgumentCapture = ArgumentCaptor.forClass(Object[].class);
        verify(resources).getString(resourceIdCaptor.capture(), formatterArgumentCapture.capture());
        int resourceIdCaptorValue = resourceIdCaptor.getValue();
        Object[] argumentList = formatterArgumentCapture.getValue();
        assertThat(resourceIdCaptorValue, is(R.string.fee_flat_formatter));
        assertThat(argumentList.length, is(2));
    }

    @Test
    public void testGetFormattedFee_returnsFlatFeeWithMinOnly() {
        Fee percentFee = new Fee(mJSONObject.optJSONObject("FEE_ELEVEN").optJSONArray("nodes").optJSONObject(0));
        FeeFormatter.getFormattedFee(context, Arrays.asList(mFlatFee,percentFee));
        ArgumentCaptor<Integer> resourceIdCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Object[]> formatterArgumentCapture = ArgumentCaptor.forClass(Object[].class);
        verify(resources).getString(resourceIdCaptor.capture(), formatterArgumentCapture.capture());
        int resourceIdCaptorValue = resourceIdCaptor.getValue();
        Object[] argumentList = formatterArgumentCapture.getValue();
        assertThat(resourceIdCaptorValue, is(R.string.fee_flat_formatter));
        assertThat(argumentList.length, is(2));
    }

    @Test
    public void testGetFormattedFee_returnsPercentFeeWithMinAndMax() {
        Fee flatFee = new Fee(mJSONObject.optJSONObject("FEE_NINE").optJSONArray("nodes").optJSONObject(0));
        Fee percentFee = new Fee(mJSONObject.optJSONObject("FEE_TWO").optJSONArray("nodes").optJSONObject(0));
        FeeFormatter.getFormattedFee(context, Arrays.asList(flatFee,percentFee));
        ArgumentCaptor<Integer> resourceIdCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Object[]> formatterArgumentCapture = ArgumentCaptor.forClass(Object[].class);
        verify(resources).getString(resourceIdCaptor.capture(), formatterArgumentCapture.capture());
        int resourceIdCaptorValue = resourceIdCaptor.getValue();
        Object[] argumentList = formatterArgumentCapture.getValue();
        assertThat(resourceIdCaptorValue, is(R.string.fee_percent_formatter));
        assertThat(argumentList.length, is(4));
    }

    @Test
    public void testGetFormattedFee_returnsPercentFeeWithMinOnly() {
        Fee flatFee = new Fee(mJSONObject.optJSONObject("FEE_NINE").optJSONArray("nodes").optJSONObject(0));
        Fee percentFee = new Fee(mJSONObject.optJSONObject("FEE_THREE").optJSONArray("nodes").optJSONObject(0));
        FeeFormatter.getFormattedFee(context, Arrays.asList(flatFee,percentFee));
        ArgumentCaptor<Integer> resourceIdCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Object[]> formatterArgumentCapture = ArgumentCaptor.forClass(Object[].class);
        verify(resources).getString(resourceIdCaptor.capture(), formatterArgumentCapture.capture());
        int resourceIdCaptorValue = resourceIdCaptor.getValue();
        Object[] argumentList = formatterArgumentCapture.getValue();
        assertThat(resourceIdCaptorValue, is(R.string.fee_percent_only_min_formatter));
        assertThat(argumentList.length, is(3));
    }

    @Test
    public void testGetFormattedFee_returnsPercentFeeWithMaxOnly() {
        Fee flatFee = new Fee(mJSONObject.optJSONObject("FEE_NINE").optJSONArray("nodes").optJSONObject(0));
        Fee percentFee = new Fee(mJSONObject.optJSONObject("FEE_FOUR").optJSONArray("nodes").optJSONObject(0));
        FeeFormatter.getFormattedFee(context, Arrays.asList(flatFee,percentFee));
        ArgumentCaptor<Integer> resourceIdCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Object[]> formatterArgumentCapture = ArgumentCaptor.forClass(Object[].class);
        verify(resources).getString(resourceIdCaptor.capture(), formatterArgumentCapture.capture());
        int resourceIdCaptorValue = resourceIdCaptor.getValue();
        Object[] argumentList = formatterArgumentCapture.getValue();
        assertThat(resourceIdCaptorValue, is(R.string.fee_percent_only_max_formatter));
        assertThat(argumentList.length, is(3));
    }

    @Test
    public void testGetFormattedFeeAndProcessingTime_returnsFormattedFeeAndProcessingTimeWithNoFeeString() throws JSONException {

        JSONObject  mJSONObject = new JSONObject(externalResourceManager.getResourceContent("processing_time_information.json"));
        ProcessingTime mProcessingTime = new ProcessingTime(mJSONObject.getJSONObject("PROCESSING_TIME_ONE").getJSONArray("nodes").optJSONObject(0));

        when(FeeFormatter.getFormattedFee(context,Arrays.asList(mFlatFee))).thenReturn("No fee");
        when(context.getResources().getString(R.string.noFee)).thenReturn("No fee");
        when(context.getResources().getString(R.string.fee_flat_formatter)).thenReturn("$2.00");
        when(context.getResources().getString(R.string.processingTimeInformation,"1-2 Business days")).thenReturn(" \u2022 1-2 Business days");

        String formattedString = FeeFormatter.getFormattedFeeAndProcessingTime(context,Arrays.asList(mFlatFee),mProcessingTime);
        assertThat(formattedString , is("No fee \u2022 1-2 Business days"));
    }

    @Test
    public void testGetFormattedFeeAndProcessingTime_returnsFormattedFeeAndProcessingTimeWithFlatFeeString() throws JSONException {

        JSONObject  mJSONObject = new JSONObject(externalResourceManager.getResourceContent("processing_time_information.json"));
        ProcessingTime mProcessingTime = new ProcessingTime(mJSONObject.getJSONObject("PROCESSING_TIME_ONE").getJSONArray("nodes").optJSONObject(0));

        when(FeeFormatter.getFormattedFee(context,Arrays.asList(mFlatFee))).thenReturn("$3.00 fee");
        when(context.getResources().getString(R.string.noFee)).thenReturn("No fee");
        when(context.getResources().getString(R.string.feeInformation,"$3.00 fee")).thenReturn("$3.00 fee");
        when(context.getResources().getString(R.string.processingTimeInformation,"1-2 Business days")).thenReturn(" \u2022 1-2 Business days");

        String formattedString = FeeFormatter.getFormattedFeeAndProcessingTime(context,Arrays.asList(mFlatFee),mProcessingTime);
        assertThat(formattedString , is("$3.00 fee \u2022 1-2 Business days"));
    }
}