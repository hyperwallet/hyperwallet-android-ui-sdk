package com.hyperwallet.android.ui.transfermethod;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.res.Resources;

import com.hyperwallet.android.hyperwallet_ui.R;
import com.hyperwallet.android.model.meta.Fee;

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
    private ArgumentCaptor<Object> formatterArugmentCapture;
    @Mock
    private Context context;
    @Mock
    private Resources resources;

    private final Fee flatFee = new Fee("", null, "USD", "FLAT", "3.00", null, null);

    @Before
    public void setUp() {
        when(context.getResources()).thenReturn(resources);
    }

    @Test
    public void testGetFormattedFee_returnsFlatFormattedFee() {
        FeeFormatter.getFormattedFee(context, Arrays.asList(flatFee));

        verify(resources).getString(resourceIdCaptor.capture(), formatterArugmentCapture.capture());
        int resourceIdCaptorValue = resourceIdCaptor.getValue();
        List<Object> argumentList = formatterArugmentCapture.getAllValues();
        assertThat(resourceIdCaptorValue, is(R.string.fee_flat_formatter));
        assertThat(argumentList.size(), is(2));
    }

    @Test
    public void testGetFormattedFee_returnsPercentFormattedFeeWithMinAndMax() {
        Fee fee = new Fee("", null, "USD", "PERCENT", "3", "4.00", "10.00");

        FeeFormatter.getFormattedFee(context, Arrays.asList(fee));

        verify(resources).getString(resourceIdCaptor.capture(), formatterArugmentCapture.capture());
        int resourceIdCaptorValue = resourceIdCaptor.getValue();
        List<Object> argumentList = formatterArugmentCapture.getAllValues();
        assertThat(resourceIdCaptorValue, is(R.string.fee_percent_formatter));
        assertThat(argumentList.size(), is(4));
    }

    @Test
    public void testGetFormattedFee_returnsPercentFormattedFeeWithMinOnly() {
        Fee fee = new Fee("", null, "USD", "PERCENT", "3", "4.00", "");

        FeeFormatter.getFormattedFee(context, Arrays.asList(fee));

        verify(resources).getString(resourceIdCaptor.capture(), formatterArugmentCapture.capture());
        int resourceIdCaptorValue = resourceIdCaptor.getValue();
        List<Object> argumentList = formatterArugmentCapture.getAllValues();
        assertThat(resourceIdCaptorValue, is(R.string.fee_percent_only_min_formatter));
        assertThat(argumentList.size(), is(3));
    }

    @Test
    public void testGetFormattedFee_returnsPercentFormattedFeeWithMaxOnly() {
        Fee fee = new Fee("", null, "USD", "PERCENT", "3", "", "10.00");

        FeeFormatter.getFormattedFee(context, Arrays.asList(fee));

        verify(resources).getString(resourceIdCaptor.capture(), formatterArugmentCapture.capture());
        int resourceIdCaptorValue = resourceIdCaptor.getValue();
        List<Object> argumentList = formatterArugmentCapture.getAllValues();
        assertThat(resourceIdCaptorValue, is(R.string.fee_percent_only_max_formatter));
        assertThat(argumentList.size(), is(3));
    }

    @Test
    public void testGetFormattedFee_returnsPercentFormattedFeeWithoutMinAndMax() {
        Fee fee = new Fee("", null, "USD", "PERCENT", "3", "", "");

        FeeFormatter.getFormattedFee(context, Arrays.asList(fee));

        verify(resources).getString(resourceIdCaptor.capture(), formatterArugmentCapture.capture());
        int resourceIdCaptorValue = resourceIdCaptor.getValue();
        List<Object> argumentList = formatterArugmentCapture.getAllValues();
        assertThat(resourceIdCaptorValue, is(R.string.fee_percent_no_min_and_max_formatter));
        assertThat(argumentList.size(), is(1));
    }

    @Test
    public void testGetFormattedFee_returnsPercentAndFlatFormattedFeeWithMinAndMax() {
        Fee percentFee = new Fee("", null, "USD", "PERCENT", "3", "4.00", "10.00");

        FeeFormatter.getFormattedFee(context, Arrays.asList(flatFee, percentFee));

        verify(resources).getString(resourceIdCaptor.capture(), formatterArugmentCapture.capture());
        int resourceIdCaptorValue = resourceIdCaptor.getValue();
        List<Object> argumentList = formatterArugmentCapture.getAllValues();
        assertThat(resourceIdCaptorValue, is(R.string.fee_mix_formatter));
        assertThat(argumentList.size(), is(5));
    }

    @Test
    public void testGetFormattedFee_returnsPercentAndFlatFormattedFeeWithMinOnly() {
        Fee percentFee = new Fee("", null, "USD", "PERCENT", "3.00", "10.00", "");

        FeeFormatter.getFormattedFee(context, Arrays.asList(flatFee, percentFee));

        verify(resources).getString(resourceIdCaptor.capture(), formatterArugmentCapture.capture());
        int resourceIdCaptorValue = resourceIdCaptor.getValue();
        List<Object> argumentList = formatterArugmentCapture.getAllValues();
        assertThat(resourceIdCaptorValue, is(R.string.fee_mix_only_min_formatter));
        assertThat(argumentList.size(), is(4));
    }
}
