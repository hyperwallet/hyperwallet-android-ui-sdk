package com.hyperwallet.android.ui.transfermethod.rule;

import static org.mockito.Mockito.mock;

import com.hyperwallet.android.ui.common.insight.HyperwalletInsight;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class HyperwalletInsightMockRule extends TestWatcher {

    private HyperwalletInsight mHyperwalletInsight;

    @Override
    protected void starting(Description description) {
        super.starting(description);
        mHyperwalletInsight = mock(HyperwalletInsight.class);
        HyperwalletInsight.setInstance(mHyperwalletInsight);
    }

    @Override
    protected void finished(Description description) {
        super.finished(description);
        mHyperwalletInsight = null;
        HyperwalletInsight.setInstance(null);
    }

    public HyperwalletInsight getInsight() {
        return mHyperwalletInsight;
    }
}
