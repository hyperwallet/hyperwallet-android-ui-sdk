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
package com.hyperwallet.android.ui.transfermethod;

import static com.hyperwallet.android.ui.common.intent.HyperwalletIntent.HYPERWALLET_LOCAL_BROADCAST_PAYLOAD_KEY;
import static com.hyperwallet.android.ui.transfermethod.TransferMethodLocalBroadcast.TransferMethodLocalBroadcastAction.ACTION_HYPERWALLET_TRANSFER_METHOD_ADDED;
import static com.hyperwallet.android.ui.transfermethod.TransferMethodLocalBroadcast.TransferMethodLocalBroadcastAction.ACTION_HYPERWALLET_TRANSFER_METHOD_DEACTIVATED;

import android.content.Intent;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.StringDef;

import com.hyperwallet.android.model.StatusTransition;
import com.hyperwallet.android.model.transfermethod.TransferMethod;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class TransferMethodLocalBroadcast {

    public static Intent createBroadcastIntentTransferMethodAdded(
            @NonNull final TransferMethod transferMethod) {
        return createBroadcastIntent(transferMethod,
                ACTION_HYPERWALLET_TRANSFER_METHOD_ADDED);
    }

    public static Intent createBroadcastIntentTransferMethodDeactivated(
            @NonNull final StatusTransition StatusTransition) {
        return createBroadcastIntent(StatusTransition,
                ACTION_HYPERWALLET_TRANSFER_METHOD_DEACTIVATED);
    }

    private static Intent createBroadcastIntent(@NonNull final Parcelable parcelable,
            @NonNull final @TransferMethodLocalBroadcastActionType String action) {
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
    public @interface TransferMethodLocalBroadcastActionType {
    }

    public final class TransferMethodLocalBroadcastAction {

        private TransferMethodLocalBroadcastAction() {}
        public static final String ACTION_HYPERWALLET_TRANSFER_METHOD_ADDED =
                "ACTION_HYPERWALLET_TRANSFER_METHOD_ADDED";
        public static final String ACTION_HYPERWALLET_TRANSFER_METHOD_DEACTIVATED =
                "ACTION_HYPERWALLET_TRANSFER_METHOD_DEACTIVATED";
    }
}
