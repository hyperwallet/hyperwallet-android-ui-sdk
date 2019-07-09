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
package com.hyperwallet.android.ui.repository;

import androidx.annotation.NonNull;

import com.hyperwallet.android.model.HyperwalletErrors;
import com.hyperwallet.android.model.StatusTransition;
import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod;

import java.util.List;

/**
 * Transfer Method Repository Contract
 */
public interface TransferMethodRepository {

    /**
     * Create transfer method
     *
     * @param transferMethod Transfer Method Representation
     * @param callback       @see {@link LoadTransferMethodCallback}
     */
    void createTransferMethod(@NonNull HyperwalletTransferMethod transferMethod,
            @NonNull LoadTransferMethodCallback callback);

    /**
     * Load transfer methods available associated with current context
     *
     * @param callback @see {@link LoadTransferMethodListCallback}
     */
    void loadTransferMethods(@NonNull LoadTransferMethodListCallback callback);

    /**
     * Deactivate transfer method specified.
     *
     * @param transferMethod transfer method to deactivate @see {@link HyperwalletTransferMethod}
     * @param callback       @see {@link DeactivateTransferMethodCallback}
     */
    void deactivateTransferMethod(@NonNull final HyperwalletTransferMethod transferMethod,
            @NonNull final DeactivateTransferMethodCallback callback);

    /**
     * Callback interface that responses to action when invoked to
     * Load Transfer Method information
     * <p>
     * When Transfer Method is properly loaded
     * {@link LoadTransferMethodCallback#onTransferMethodLoaded(HyperwalletTransferMethod)}
     * is invoked otherwise {@link LoadTransferMethodCallback#onError(HyperwalletErrors)}
     * is called to further log or show error information
     */
    interface LoadTransferMethodCallback {

        void onTransferMethodLoaded(HyperwalletTransferMethod transferMethod);

        void onError(HyperwalletErrors errors);
    }

    /**
     * Callback interface that responses to action when invoked to
     * Load transfer methods information
     */
    interface LoadTransferMethodListCallback {

        void onTransferMethodListLoaded(List<HyperwalletTransferMethod> transferMethods);

        void onError(HyperwalletErrors errors);
    }

    /**
     * Callback interface that responses to action when invoked to
     * Deactivate transfer methods information
     */
    interface DeactivateTransferMethodCallback {

        void onTransferMethodDeactivated(final @NonNull StatusTransition statusTransition);

        void onError(HyperwalletErrors errors);
    }
}
