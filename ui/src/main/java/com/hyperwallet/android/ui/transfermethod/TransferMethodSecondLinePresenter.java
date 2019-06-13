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

import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory instantiates instance of a {@link TransferMethodSecondLine} by a corresponding
 * {@link HyperwalletTransferMethod.TransferMethodType}
 */
final class TransferMethodSecondLinePresenter implements SecondLinePresenter {
    private Map<Class, TransferMethodSecondLine> mSecondLineStrategies = new HashMap<>(3);

    @Override
    @NonNull
    public TransferMethodSecondLine getSecondLinePresenter(
            @HyperwalletTransferMethod.TransferMethodType final String type) {
        if (type == null) {
            return TransferMethodSecondLine.DEFAULT;
        }

        switch (type) {
            case BANK_CARD:
            case PREPAID_CARD:
                if (!mSecondLineStrategies.containsKey(CardSecondLine.class)) {
                    mSecondLineStrategies.put(
                            CardSecondLine.class, new CardSecondLine());
                }

                //noinspection ConstantConditions
                return mSecondLineStrategies.get(CardSecondLine.class);
            case BANK_ACCOUNT:
            case WIRE_ACCOUNT:
                if (!mSecondLineStrategies.containsKey(AccountSecondLine.class)) {
                    mSecondLineStrategies.put(
                            AccountSecondLine.class, new AccountSecondLine());
                }

                //noinspection ConstantConditions
                return mSecondLineStrategies.get(AccountSecondLine.class);
            case PAYPAL_ACCOUNT:
                if (!mSecondLineStrategies.containsKey(PayPalAccountSecondLine.class)) {
                    mSecondLineStrategies.put(PayPalAccountSecondLine.class,
                            new PayPalAccountSecondLine());
                }

                //noinspection ConstantConditions
                return mSecondLineStrategies.get(PayPalAccountSecondLine.class);

            default:
                return TransferMethodSecondLine.DEFAULT;
        }
    }
}
