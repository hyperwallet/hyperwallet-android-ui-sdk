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

import androidx.annotation.NonNull;

import java.util.Map;

/**
 * Used for gathering the data necessary for the Insights analytics.
 */
public class HyperwalletInsight {
    private static HyperwalletInsight sHyperwalletInsight;

    private HyperwalletInsight() {
    }

    public static synchronized HyperwalletInsight getInstance() {
        if (sHyperwalletInsight == null) {
            sHyperwalletInsight = new HyperwalletInsight();
        }
        return sHyperwalletInsight;
    }

    public void trackClick(@NonNull final Context context, @NonNull final String pageName,
            @NonNull final String pageGroup, String link, Map<String, String> params) {

    }

    public void trackImpression(@NonNull final Context context, @NonNull final String pageName,
            @NonNull final String pageGroup, @NonNull final Map<String, String> params) {

    }
}
