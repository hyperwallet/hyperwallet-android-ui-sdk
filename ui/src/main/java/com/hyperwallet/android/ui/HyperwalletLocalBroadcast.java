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
package com.hyperwallet.android.ui;

import static com.hyperwallet.android.ui.HyperwalletLocalBroadcast.HyperwalletLocalBroadcastAction.ACTION_HYPERWALLET_TRANSFER_METHOD_ADDED;
import static com.hyperwallet.android.ui.HyperwalletLocalBroadcast.HyperwalletLocalBroadcastAction.ACTION_HYPERWALLET_TRANSFER_METHOD_DEACTIVATED;

import android.content.Intent;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.StringDef;

import com.hyperwallet.android.model.HyperwalletStatusTransition;
import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class HyperwalletLocalBroadcast {

    private static final String HYPERWALLET_LOCAL_BROADCAST_PAYLOAD_KEY = "hyperwallet-local-broadcast-payload";

    public static Intent createBroadcastIntentTransferMethodAdded(
            @NonNull final HyperwalletTransferMethod transferMethod) {
        return createBroadcastIntent(transferMethod,
                ACTION_HYPERWALLET_TRANSFER_METHOD_ADDED);
    }

    public static Intent createBroadcastIntentTransferMethodDeactivated(
            @NonNull final HyperwalletStatusTransition hyperwalletStatusTransition) {
        return createBroadcastIntent(hyperwalletStatusTransition,
                ACTION_HYPERWALLET_TRANSFER_METHOD_DEACTIVATED);
    }

    private static Intent createBroadcastIntent(@NonNull final Parcelable parcelable,
            @NonNull final @HyperwalletLocalBroadcastActionType String action) {
        final Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra(HYPERWALLET_LOCAL_BROADCAST_PAYLOAD_KEY, parcelable);
        return intent;
    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            ACTION_HYPERWALLET_TRANSFER_METHOD_ADDED,
            ACTION_HYPERWALLET_TRANSFER_METHOD_DEACTIVATED
    })
    public @interface HyperwalletLocalBroadcastActionType {
    }

    public final class HyperwalletLocalBroadcastAction {
        public static final String ACTION_HYPERWALLET_TRANSFER_METHOD_ADDED =
                "ACTION_HYPERWALLET_TRANSFER_METHOD_ADDED";
        public static final String ACTION_HYPERWALLET_TRANSFER_METHOD_DEACTIVATED =
                "ACTION_HYPERWALLET_TRANSFER_METHOD_DEACTIVATED";
    }
}
