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

import static com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod.TransferMethodFields.CARD_NUMBER;

import android.content.Context;

import androidx.annotation.NonNull;

import com.hyperwallet.android.hyperwallet_ui.R;
import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod;

/**
 * Retrieves Card number from the {@link HyperwalletTransferMethod} field
 * and forms an identifier String of the last 4 digits.
 */
public class CardSecondLine implements TransferMethodSecondLine {
    private static final int LAST_FOUR_DIGIT = 4;

    @NonNull
    @Override
    public String getText(@NonNull final Context context,
            @NonNull final HyperwalletTransferMethod transferMethod) {

        return context.getString(R.string.transfer_method_list_item_description,
                getCardIdentifier(transferMethod));
    }

    private String getCardIdentifier(@NonNull final HyperwalletTransferMethod transferMethod) {
        final String transferIdentification = transferMethod.getField(CARD_NUMBER);

        return (transferIdentification.length() > LAST_FOUR_DIGIT
                ? transferIdentification.substring(transferIdentification.length() - LAST_FOUR_DIGIT)
                : transferIdentification);
    }


}
