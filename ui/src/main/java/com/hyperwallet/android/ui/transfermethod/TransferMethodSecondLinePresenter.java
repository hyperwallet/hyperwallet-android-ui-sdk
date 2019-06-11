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

import static com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod.TransferMethodTypes.BANK_ACCOUNT;
import static com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod.TransferMethodTypes.BANK_CARD;
import static com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod.TransferMethodTypes.PAYPAL_ACCOUNT;
import static com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod.TransferMethodTypes.PREPAID_CARD;
import static com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod.TransferMethodTypes.WIRE_ACCOUNT;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

final class TransferMethodSecondLinePresenter implements SecondLinePresenter {
    private Map<Class, TransferMethodSecondLineStrategy> mSecondLineStrategies = new HashMap<>(2);

    @Override
    @NonNull
    public TransferMethodSecondLineStrategy obtainSecondLineStrategy(String type) {
        if (type == null) {
            return TransferMethodSecondLineStrategy.DEFAULT;
        }

        switch (type) {
            case BANK_CARD:
            case PREPAID_CARD:
            case BANK_ACCOUNT:
            case WIRE_ACCOUNT:
                if (!mSecondLineStrategies.containsKey(TransferMethodSecondLine.class)) {
                    mSecondLineStrategies.put(TransferMethodSecondLine.class, new TransferMethodSecondLine());
                }

                //noinspection ConstantConditions
                return mSecondLineStrategies.get(TransferMethodSecondLine.class);
            case PAYPAL_ACCOUNT:
                if (!mSecondLineStrategies.containsKey(PayPalAccountTransferMethodSecondLine.class)) {
                    mSecondLineStrategies.put(PayPalAccountTransferMethodSecondLine.class,
                            new PayPalAccountTransferMethodSecondLine());
                }

                //noinspection ConstantConditions
                return mSecondLineStrategies.get(PayPalAccountTransferMethodSecondLine.class);

            default:
                return TransferMethodSecondLineStrategy.DEFAULT;
        }
    }
}
