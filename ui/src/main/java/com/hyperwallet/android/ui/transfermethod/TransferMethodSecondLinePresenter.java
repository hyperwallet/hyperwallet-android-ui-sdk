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
