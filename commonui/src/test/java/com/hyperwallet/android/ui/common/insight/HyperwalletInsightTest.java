package com.hyperwallet.android.ui.common.insight;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class HyperwalletInsightTest {

    @Test
    public void testGetInstance() {
        HyperwalletInsight hyperwalletInsight1 = HyperwalletInsight.getInstance();
        HyperwalletInsight hyperwalletInsight2 = HyperwalletInsight.getInstance();

        assertThat(hyperwalletInsight1, is(notNullValue()));
        assertThat(hyperwalletInsight1, is(hyperwalletInsight2));
    }
}
