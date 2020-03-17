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

package com.hyperwallet.android.ui.transfer;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.HyperwalletAuthenticationTokenProvider;
import com.hyperwallet.android.ui.transfer.repository.TransferRepositoryFactory;
import com.hyperwallet.android.ui.transfer.view.CreateTransferActivity;

/* Class responsible for initializing the Hyperwallet Transfer UI SDK. It contains methods to interact with the
 * activities and fragments used to interact with the Hyperwallet platform
 */
public final class HyperwalletTransferUi {

    private static HyperwalletTransferUi sInstance;

    private HyperwalletTransferUi() {
    }


    /**
     * @param authenticationTokenProvider An implementation of the {@link HyperwalletAuthenticationTokenProvider}
     * @return Returns a newly created HyperwalletTransferUi that can be used to get Intents to launch different
     * activities.
     */
    public static synchronized HyperwalletTransferUi getInstance(
            @NonNull final HyperwalletAuthenticationTokenProvider authenticationTokenProvider) {
        if (sInstance == null) {
            sInstance = new HyperwalletTransferUi();
            Hyperwallet.getInstance(authenticationTokenProvider);
        }
        return sInstance;
    }

    public Intent getIntentCreateTransfer(@NonNull final Context context) {
        return new Intent(context, CreateTransferActivity.class);
    }

    public Intent getIntentCreateTransfer(@NonNull final Context context, @NonNull final String sourceToken) {
        Intent intent = new Intent(context, CreateTransferActivity.class);
        intent.putExtra(CreateTransferActivity.EXTRA_TRANSFER_SOURCE_TOKEN, sourceToken);
        return intent;
    }

    public static void clearInstance() {
        sInstance = null;
        Hyperwallet.clearInstance();
        TransferRepositoryFactory.clearInstance();
    }
}
