/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Hyperwallet Systems Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.hyperwallet.android.ui.receipt;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.HyperwalletAuthenticationTokenProvider;
import com.hyperwallet.android.ui.receipt.view.ListPrepaidCardReceiptActivity;
import com.hyperwallet.android.ui.receipt.view.ListUserReceiptActivity;

public final class HyperwalletReceiptUi {

    private static HyperwalletReceiptUi sInstance;

    private HyperwalletReceiptUi() {
    }

    /**
     * @param authenticationTokenProvider An implementation of the {@link HyperwalletAuthenticationTokenProvider}
     * @return Returns a newly created HyperwalletTransferMethodUi that can be used to get Intents to launch different
     * activities.
     */
    public static synchronized HyperwalletReceiptUi getInstance(
            @NonNull final HyperwalletAuthenticationTokenProvider authenticationTokenProvider) {
        if (sInstance == null) {
            sInstance = new HyperwalletReceiptUi();
            Hyperwallet.getInstance(authenticationTokenProvider);
        }
        return sInstance;
    }

    /**
     * @param context A Context of the application consuming this Intent.
     * @return an Intent with the data necessary to launch the {@link ListUserReceiptActivity}
     */
    public Intent getIntentListUserReceiptActivity(@NonNull final Context context) {
        return new Intent(context, ListUserReceiptActivity.class);
    }

    /**
     * @param context A Context of the application consuming this Intent.
     * @return an Intent with the data necessary to launch the {@link ListPrepaidCardReceiptActivity}
     */
    public Intent getIntentListPrepaidCardReceiptActivity(@NonNull final Context context, @NonNull final String token) {
        Intent intent = new Intent(context, ListPrepaidCardReceiptActivity.class);
        intent.putExtra(ListPrepaidCardReceiptActivity.EXTRA_PREPAID_CARD_TOKEN, token);
        return intent;
    }

    public static void clearInstance() {
        sInstance = null;
        Hyperwallet.clearInstance();
    }
}
