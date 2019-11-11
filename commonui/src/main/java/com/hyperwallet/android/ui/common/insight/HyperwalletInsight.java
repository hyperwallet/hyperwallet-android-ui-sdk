/*
 * Copyright 2019 Hyperwallet
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */
package com.hyperwallet.android.ui.common.insight;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.hyperwallet.android.Configuration;
import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.HyperwalletAuthenticationTokenProvider;
import com.hyperwallet.android.exception.HyperwalletException;
import com.hyperwallet.android.insight.Insight;
import com.hyperwallet.android.insight.InsightEventTag;
import com.hyperwallet.android.insight.collect.ErrorInfo;
import com.hyperwallet.android.listener.HyperwalletListener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Used for gathering the data necessary for the Insights analytics.
 */
public class HyperwalletInsight {

    private static final int MAX_THREAD_POOL = 2;
    private static HyperwalletInsight sHyperwalletInsight;
    private final Executor mExecutor;

    private HyperwalletInsight() {
        mExecutor = Executors.newFixedThreadPool(MAX_THREAD_POOL);
    }

    /**
     * Returns instance of HyperwalletInsight or initializes it if it is null.
     *
     * @return singleton instance of HyperwalletInsight
     */
    public static synchronized HyperwalletInsight getInstance() {
        if (sHyperwalletInsight == null) {
            sHyperwalletInsight = new HyperwalletInsight();
        }
        return sHyperwalletInsight;
    }

    @VisibleForTesting
    public void setInstance(HyperwalletInsight hyperwalletInsight) {
        sHyperwalletInsight = hyperwalletInsight;
    }

    /**
     * Initializes the Insight library using the given parameters.
     *
     * @param context       the context using Insight
     * @param configuration Configuration object containing information about the session
     */
    public void initialize(@NonNull final Context context, @NonNull final Configuration configuration) {
        final String sdkVersion = com.hyperwallet.android.ui.common.BuildConfig.VERSION_NAME;

        Insight.initialize(context, configuration.getEnvironment(), configuration.getProgramToken(), sdkVersion,
                configuration.getInsightApiUrl(), configuration.getUserToken());
    }

    /**
     * Initialize insight in BG mode
     *
     * @param context  Application context
     * @param provider Hyperwallet authentication provider
     */
    public void initialize(@NonNull final Context context,
            @NonNull final HyperwalletAuthenticationTokenProvider provider) {
        final String sdkVersion = com.hyperwallet.android.ui.common.BuildConfig.VERSION_NAME;

        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Hyperwallet.getInstance(provider, new HyperwalletListener<Configuration>() {
                    @Override
                    public void onSuccess(@Nullable Configuration configuration) {
                        if (configuration != null) {
                            Insight.initialize(context, configuration.getEnvironment(), configuration.getProgramToken(),
                                    sdkVersion, configuration.getInsightApiUrl(), configuration.getUserToken());
                        }
                    }

                    @Override
                    public void onFailure(HyperwalletException exception) {
                        // do nothing
                    }

                    @Override
                    public Handler getHandler() {
                        return null;
                    }
                });
            }
        });
    }

    /**
     * Used to track an impression by a user. Typical example is when a user lands on a specific page.
     *
     * @param context   Context where the tracking is happening
     * @param pageName  an arbituary pageName the user is currently viewing as defined by the app
     * @param pageGroup an arbituary pageGroup the user is currently viewing as defined by the app
     * @param params    additional information that can be added to make the tracking event more useful
     */
    public void trackImpression(@NonNull final Context context, @NonNull final String pageName,
            @NonNull final String pageGroup, @NonNull final Map<String, String> params) {

        if (Insight.getInsightTracker().isInitialized()) {
            Insight.getInsightTracker().trackImpression(context, pageName, pageGroup, params);

        } else {
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    Hyperwallet.getDefault().getConfiguration(new HyperwalletListener<Configuration>() {
                        @Override
                        public void onSuccess(@Nullable Configuration configuration) {
                            if (configuration != null) {
                                HyperwalletInsight.getInstance().initialize(context, configuration);
                                Insight.getInsightTracker().trackImpression(context, pageName, pageGroup, params);
                            }
                        }

                        @Override
                        public void onFailure(HyperwalletException exception) {
                            // do nothing
                        }

                        @Override
                        public Handler getHandler() {
                            return null;
                        }
                    });
                }
            });
        }
    }

    /**
     * Used to track an intentional interaction by the user. Typically for interacting with a UI element such as Buttons
     * and Spinners
     *
     * @param context   Context where the tracking is happening
     * @param pageName  an arbituary pageName the user is currently viewing as defined by the app
     * @param pageGroup an arbituary pageGroup the user is currently viewing as defined by the app
     * @param link      the link the user is currently viewing as defined by the app
     * @param params    additional information that can be added to make the tracking event more useful
     */
    public void trackClick(@NonNull final Context context, @NonNull final String pageName,
            @NonNull final String pageGroup, @NonNull final String link, @NonNull final Map<String, String> params) {

        if (Insight.getInsightTracker().isInitialized()) {
            Insight.getInsightTracker().trackImpression(context, pageName, pageGroup, params);

        } else {
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    Hyperwallet.getDefault().getConfiguration(new HyperwalletListener<Configuration>() {
                        @Override
                        public void onSuccess(@Nullable Configuration configuration) {
                            if (configuration != null) {
                                HyperwalletInsight.getInstance().initialize(context, configuration);
                                Insight.getInsightTracker().trackImpression(context, pageName, pageGroup, params);
                            }
                        }

                        @Override
                        public void onFailure(HyperwalletException exception) {
                            // do nothing
                        }

                        @Override
                        public Handler getHandler() {
                            return null;
                        }
                    });
                }
            });
        }
    }

    /**
     * Used to track an error received by the user. This can be used for, and not limited to: system errors, caught
     * exceptions, or validation input errors.
     *
     * @param context   Context where the tracking is happening
     * @param pageName  an arbituary pageName the user is currently viewing as defined by the app
     * @param pageGroup an arbituary pageGroup the user is currently viewing as defined by the app
     * @param errorInfo additional error information that can be added to make the tracking event more useful
     */
    public void trackError(@NonNull final Context context, @NonNull final String pageName,
            @NonNull final String pageGroup, @NonNull final ErrorInfo errorInfo) {

        if (Insight.getInsightTracker().isInitialized()) {
            Insight.getInsightTracker().trackError(context, pageName, pageGroup, errorInfo);

        } else {
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    Hyperwallet.getDefault().getConfiguration(new HyperwalletListener<Configuration>() {
                        @Override
                        public void onSuccess(@Nullable Configuration configuration) {
                            if (configuration != null) {
                                HyperwalletInsight.getInstance().initialize(context, configuration);
                                Insight.getInsightTracker().trackError(context, pageName, pageGroup, errorInfo);
                            }
                        }

                        @Override
                        public void onFailure(HyperwalletException exception) {
                            // do nothing
                        }

                        @Override
                        public Handler getHandler() {
                            return null;
                        }
                    });
                }
            });
        }
    }

    public static class TransferParamsBuilder {

        private Map<String, String> mParams = new HashMap<>();

        public TransferParamsBuilder setTransferMethodType(@NonNull final String transferMethodType) {
            if (!TextUtils.isEmpty(transferMethodType)) {
                mParams.put(InsightEventTag.InsightEventTagEventParams.TRANSFER_METHOD_TYPE, transferMethodType);
            }
            return this;
        }

        public TransferParamsBuilder setTransferMethodProfileType(@NonNull final String transferMethodProfileType) {
            if (!TextUtils.isEmpty(transferMethodProfileType)) {
                mParams.put(InsightEventTag.InsightEventTagEventParams.TRANSFER_METHOD_PROFILE_TYPE,
                        transferMethodProfileType);
            }
            return this;
        }

        public TransferParamsBuilder setTransferMethodCountry(@NonNull final String transferMethodCountry) {
            if (!TextUtils.isEmpty(transferMethodCountry)) {
                mParams.put(InsightEventTag.InsightEventTagEventParams.TRANSFER_METHOD_COUNTRY, transferMethodCountry);
            }
            return this;
        }

        public TransferParamsBuilder setTransferMethodCurrency(@NonNull final String transferMethodCurrency) {
            if (!TextUtils.isEmpty(transferMethodCurrency)) {
                mParams.put(InsightEventTag.InsightEventTagEventParams.TRANSFER_METHOD_CURRENCY,
                        transferMethodCurrency);
            }
            return this;
        }

        public Map<String, String> build() {
            return mParams;
        }
    }
}
