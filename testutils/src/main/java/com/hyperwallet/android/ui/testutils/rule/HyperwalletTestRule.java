package com.hyperwallet.android.ui.testutils.rule;

import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.ui.testutils.TestAuthenticationProvider;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class HyperwalletTestRule extends TestWatcher {

    @Override
    protected void starting(Description description) {
        super.starting(description);
        Hyperwallet.getInstance(new TestAuthenticationProvider());
    }

    @Override
    protected void finished(Description description) {
        super.finished(description);
        Hyperwallet.clearInstance();
    }
}
