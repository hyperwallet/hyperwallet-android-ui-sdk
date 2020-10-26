package rule;

import android.os.Handler;

import androidx.annotation.Nullable;

import com.hyperwallet.android.Configuration;
import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.exception.HyperwalletException;
import com.hyperwallet.android.listener.HyperwalletListener;
import com.hyperwallet.android.ui.testutils.TestAuthenticationProvider;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class HyperwalletSdkMockRule extends TestWatcher {

    @Override
    protected void starting(Description description) {
        super.starting(description);
        Hyperwallet.getInstance(new TestAuthenticationProvider(), new HyperwalletListener<Configuration>() {
            @Override
            public void onSuccess(@Nullable Configuration result) {
            }
            @Override
            public void onFailure(HyperwalletException exception) {
            }
            @Override
            public Handler getHandler() {
                return null;
            }
        });
    }
    @Override
    protected void finished(Description description) {
        super.finished(description);
        Hyperwallet.clearInstance();
    }
}
