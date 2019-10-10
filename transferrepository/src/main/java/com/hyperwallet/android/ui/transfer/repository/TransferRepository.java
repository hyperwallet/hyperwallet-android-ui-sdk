/*
 * Copyright 2019 Hyperwallet
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including
 * without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO
 * EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE
 * USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */
package com.hyperwallet.android.ui.transfer.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hyperwallet.android.model.HyperwalletErrors;
import com.hyperwallet.android.model.StatusTransition;
import com.hyperwallet.android.model.transfer.Transfer;

/**
 * Transfer Repository
 */
public interface TransferRepository {

    /**
     * Create Transfer
     *
     * @param transfer Transfer object {@link Transfer}
     * @param callback Callback object invoked after processing {@link CreateTransferCallback}
     */
    void createTransfer(@NonNull final Transfer transfer, @NonNull final CreateTransferCallback callback);

    /**
     * Schedule Transfer
     *
     * @param transfer Transfer object {@link Transfer}
     * @param callback Callback object invoked after processing {@link ScheduleTransferCallback}
     */
    void scheduleTransfer(@NonNull final Transfer transfer, @NonNull final ScheduleTransferCallback callback);


    /**
     * Create Transfer Callback
     */
    interface CreateTransferCallback {

        /**
         * Callback method when Transfer is created successfully.
         *
         * @param transfer Transfer object {@link Transfer}
         */
        void onTransferCreated(@Nullable final Transfer transfer);

        /**
         * Callback method when error occur on Create Transfer action.
         *
         * @param errors Transfer error representation {@link HyperwalletErrors}
         */
        void onError(@NonNull final HyperwalletErrors errors);
    }

    /**
     * Schedule Transfer Callback
     */
    interface ScheduleTransferCallback {

        /**
         * Callback method when Transfer is scheduled successfully.
         *
         * @param statusTransition Transfer object {@link StatusTransition}
         */
        void onTransferScheduled(@Nullable final StatusTransition statusTransition);

        /**
         * Callback method when error occur on Create Transfer action.
         *
         * @param errors Transfer error representation {@link HyperwalletErrors}
         */
        void onError(@NonNull final HyperwalletErrors errors);
    }
}
