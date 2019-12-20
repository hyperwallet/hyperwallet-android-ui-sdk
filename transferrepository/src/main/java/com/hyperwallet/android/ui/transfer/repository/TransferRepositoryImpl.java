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

import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.exception.HyperwalletException;
import com.hyperwallet.android.listener.HyperwalletListener;
import com.hyperwallet.android.model.StatusTransition;
import com.hyperwallet.android.model.transfer.Transfer;
import com.hyperwallet.android.ui.common.repository.EspressoIdlingResource;

/**
 * Transfer Repository Implementation
 */
public class TransferRepositoryImpl implements TransferRepository {

    private Handler mHandler = new Handler();

    /**
     * @see TransferRepository#createTransfer(Transfer, CreateTransferCallback)
     */
    @Override
    public void createTransfer(@NonNull final Transfer transfer, @NonNull final CreateTransferCallback callback) {
        EspressoIdlingResource.increment();
        getHyperwallet().createTransfer(transfer, new HyperwalletListener<Transfer>() {
            @Override
            public void onSuccess(@Nullable Transfer result) {
                callback.onTransferCreated(result);
                EspressoIdlingResource.decrement();
            }

            @Override
            public void onFailure(HyperwalletException exception) {
                callback.onError(exception.getErrors());
                EspressoIdlingResource.decrement();
            }

            @Override
            public Handler getHandler() {
                return mHandler;
            }
        });
    }

    /**
     * @see TransferRepository#scheduleTransfer(Transfer, ScheduleTransferCallback)
     */
    @Override
    public void scheduleTransfer(@NonNull final Transfer transfer, @NonNull final ScheduleTransferCallback callback) {
        EspressoIdlingResource.increment();
        getHyperwallet().scheduleTransfer(transfer.getToken(), transfer.getNotes(),
                new HyperwalletListener<StatusTransition>() {
                    @Override
                    public void onSuccess(@Nullable StatusTransition result) {
                        callback.onTransferScheduled(result);
                        EspressoIdlingResource.decrement();
                    }

                    @Override
                    public void onFailure(HyperwalletException exception) {
                        callback.onError(exception.getErrors());
                        EspressoIdlingResource.decrement();
                    }

                    @Override
                    public Handler getHandler() {
                        return mHandler;
                    }
                });
    }

    Hyperwallet getHyperwallet() {
        return Hyperwallet.getDefault();
    }
}
