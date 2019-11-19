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
    private static final String SDK_VERSION = com.hyperwallet.android.ui.common.BuildConfig.VERSION_NAME;
    private static final String PRODUCT_VALUE = "hyperwallet-android-ui-sdk";
    private static final String PAGE_TECHNOLOGY_JAVA = "Java";

    public static final String LINK_SELECT_TRANSFER_METHOD_SELECT = "select-transfer-method";
    public static final String LINK_SELECT_TRANSFER_METHOD_CREATE = "create-transfer-method";
    public static final String LINK_SELECT_TRANSFER_METHOD_CURRENCY = "select-currency";
    public static final String LINK_SELECT_TRANSFER_METHOD_COUNTRY = "select-country";

    public static final String TRANSFER_METHOD_GOAL = "transfer-method-created";

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
    public static void setInstance(HyperwalletInsight hyperwalletInsight) {
        sHyperwalletInsight = hyperwalletInsight;
    }

    /**
     * Initializes the Insight library using the given parameters.
     *
     * @param context       the context using Insight
     * @param configuration Configuration object containing information about the session
     */
    public void initialize(@NonNull final Context context, @NonNull final Configuration configuration) {
        Insight.initialize(context, configuration.getEnvironment(), configuration.getProgramToken(), SDK_VERSION,
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
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Hyperwallet.getInstance(provider, new HyperwalletListener<Configuration>() {
                    @Override
                    public void onSuccess(@Nullable Configuration configuration) {
                        if (configuration != null) {
                            Insight.initialize(context, configuration.getEnvironment(), configuration.getProgramToken(),
                                    SDK_VERSION, configuration.getInsightApiUrl(), configuration.getUserToken());
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
            Insight.getInsightTracker().trackClick(context, pageName, pageGroup, link, params);

        } else {
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    Hyperwallet.getDefault().getConfiguration(new HyperwalletListener<Configuration>() {
                        @Override
                        public void onSuccess(@Nullable Configuration configuration) {
                            if (configuration != null) {
                                HyperwalletInsight.getInstance().initialize(context, configuration);
                                Insight.getInsightTracker().trackClick(context, pageName, pageGroup, link, params);
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
     * @param context      Context where the tracking is happening
     * @param pageName     an arbituary pageName the user is currently viewing as defined by the app
     * @param pageGroup    an arbituary pageGroup the user is currently viewing as defined by the app
     * @param errorInfoMap contains information regarding the error
     */
    public void trackError(@NonNull final Context context, @NonNull final String pageName,
            @NonNull final String pageGroup, @NonNull final Map<String, String> errorInfoMap) {

        final ErrorInfo errorInfo = new ErrorInfo.ErrorInfoBuilder()
                .type(errorInfoMap.get(InsightEventTag.InsightEventTagEventParams.ERROR_TYPE))
                .message(errorInfoMap.get(InsightEventTag.InsightEventTagEventParams.ERROR_MESSAGE))
                .code(errorInfoMap.get(InsightEventTag.InsightEventTagEventParams.ERROR_CODE))
                .field(errorInfoMap.get(InsightEventTag.InsightEventTagEventParams.ERROR_FIELD_NAME))
                .description(errorInfoMap.get(InsightEventTag.InsightEventTagEventParams.ERROR_DESCRIPTION))
                .build();

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

    public static final class TransferMethodParamsBuilder {
        final private Map<String, String> mParams;

        public TransferMethodParamsBuilder() {
            mParams = new HashMap<>(2);
            mParams.put(InsightEventTag.InsightEventTagEventParams.PRODUCT, PRODUCT_VALUE);
            mParams.put(InsightEventTag.InsightEventTagEventParams.PAGE_TECHNOLOGY, PAGE_TECHNOLOGY_JAVA);
        }

        public TransferMethodParamsBuilder type(@NonNull final String transferMethodType) {
            mParams.put(InsightEventTag.InsightEventTagEventParams.TRANSFER_METHOD_TYPE, transferMethodType);
            return this;
        }

        public TransferMethodParamsBuilder profileType(@NonNull final String transferMethodProfileType) {
            mParams.put(InsightEventTag.InsightEventTagEventParams.TRANSFER_METHOD_PROFILE_TYPE,
                    transferMethodProfileType);
            return this;
        }

        public TransferMethodParamsBuilder country(@NonNull final String transferMethodCountry) {
            mParams.put(InsightEventTag.InsightEventTagEventParams.TRANSFER_METHOD_COUNTRY, transferMethodCountry);
            return this;
        }

        public TransferMethodParamsBuilder currency(@NonNull final String transferMethodCurrency) {
            mParams.put(InsightEventTag.InsightEventTagEventParams.TRANSFER_METHOD_CURRENCY, transferMethodCurrency);
            return this;
        }

        public TransferMethodParamsBuilder pageTechnology(@NonNull final String pageTechnology) {
            mParams.put(InsightEventTag.InsightEventTagEventParams.PAGE_TECHNOLOGY, pageTechnology);
            return this;
        }

        public TransferMethodParamsBuilder goal(@NonNull final String transferMethodGoal) {
            mParams.put(InsightEventTag.InsightEventTagEventParams.GOAL, transferMethodGoal);
            return this;
        }

        public Map<String, String> build() {
            return mParams;
        }
    }

    public static final class ErrorParamsBuilder {
        final private Map<String, String> mParams;

        public ErrorParamsBuilder() {
            mParams = new HashMap<>(2);
            mParams.put(InsightEventTag.InsightEventTagEventParams.PRODUCT, PRODUCT_VALUE);
            mParams.put(InsightEventTag.InsightEventTagEventParams.PAGE_TECHNOLOGY, PAGE_TECHNOLOGY_JAVA);
        }

        public ErrorParamsBuilder code(@NonNull final String errorCode) {
            mParams.put(InsightEventTag.InsightEventTagEventParams.ERROR_CODE, errorCode);
            return this;
        }

        public ErrorParamsBuilder type(@NonNull final String errorType) {
            mParams.put(InsightEventTag.InsightEventTagEventParams.ERROR_TYPE, errorType);
            return this;
        }

        public ErrorParamsBuilder message(@NonNull final String errorMessage) {
            mParams.put(InsightEventTag.InsightEventTagEventParams.ERROR_MESSAGE, errorMessage);
            return this;
        }

        public ErrorParamsBuilder fieldName(@NonNull final String errorFieldName) {
            mParams.put(InsightEventTag.InsightEventTagEventParams.ERROR_FIELD_NAME, errorFieldName);
            return this;
        }

        public ErrorParamsBuilder description(@NonNull final String errorDescription) {
            mParams.put(InsightEventTag.InsightEventTagEventParams.ERROR_DESCRIPTION, errorDescription);
            return this;
        }

        public ErrorParamsBuilder pageTechnology(@NonNull final String pageTechnology) {
            mParams.put(InsightEventTag.InsightEventTagEventParams.PAGE_TECHNOLOGY, pageTechnology);
            return this;
        }

        public Map<String, String> build() {
            return mParams;
        }
    }
}
