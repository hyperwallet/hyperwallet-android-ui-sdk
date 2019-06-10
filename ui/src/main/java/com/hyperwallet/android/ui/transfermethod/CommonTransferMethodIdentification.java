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

import static com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod.TransferMethodFields.BANK_ACCOUNT_ID;
import static com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod.TransferMethodFields.CARD_NUMBER;
import static com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod.TransferMethodFields.TYPE;
import static com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod.TransferMethodTypes.BANK_ACCOUNT;
import static com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod.TransferMethodTypes.BANK_CARD;
import static com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod.TransferMethodTypes.PREPAID_CARD;
import static com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod.TransferMethodTypes.WIRE_ACCOUNT;

import android.content.Context;

import androidx.annotation.NonNull;

import com.hyperwallet.android.hyperwallet_ui.R;
import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod;


public class CommonTransferMethodIdentification implements TransferMethodIdentificationStrategy {
    private static final int LAST_FOUR_DIGIT = 4;

    @NonNull
    @Override
    public String getIdentificationText(@NonNull Context context,
            @NonNull HyperwalletTransferMethod transferMethod) {

        return context.getString(R.string.transfer_method_list_item_description,
                getAccountIdentifier(transferMethod));
    }

    private String getAccountIdentifier(@NonNull final HyperwalletTransferMethod transferMethod) {
        String transferIdentification = "";
        switch (transferMethod.getField(TYPE)) {
            case BANK_ACCOUNT:
            case WIRE_ACCOUNT:
                transferIdentification = transferMethod.getField(BANK_ACCOUNT_ID);
                break;
            case BANK_CARD:
            case PREPAID_CARD:
                transferIdentification = transferMethod.getField(CARD_NUMBER);
                break;
            default: // none for paper check
        }
        return (transferIdentification.length() > LAST_FOUR_DIGIT
                ? transferIdentification.substring(transferIdentification.length() - LAST_FOUR_DIGIT)
                : transferIdentification);
    }
}
