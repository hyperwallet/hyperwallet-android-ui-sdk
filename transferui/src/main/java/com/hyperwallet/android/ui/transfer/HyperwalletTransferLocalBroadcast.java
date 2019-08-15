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

import android.content.Intent;

import androidx.annotation.NonNull;

import com.hyperwallet.android.model.StatusTransition;

public class HyperwalletTransferLocalBroadcast {

    private static final String HYPERWALLET_LOCAL_BROADCAST_PAYLOAD_KEY = "hyperwallet-local-broadcast-payload";
    private static final String ACTION_HYPERWALLET_TRANSFER_SCHEDULED = "ACTION_HYPERWALLET_TRANSFER_SCHEDULED";

    public static Intent createBroadcastIntentTransferScheduled(@NonNull final StatusTransition statusTransition) {
        final Intent intent = new Intent();
        intent.setAction(ACTION_HYPERWALLET_TRANSFER_SCHEDULED);
        intent.putExtra(HYPERWALLET_LOCAL_BROADCAST_PAYLOAD_KEY, statusTransition);
        return intent;
    }
}
