/*
 * The MIT License (MIT)
 * Copyright (c) 2019 Hyperwallet Systems Inc.
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
package com.hyperwallet.android.ui.receipt.view;

import static android.text.format.DateUtils.FORMAT_SHOW_DATE;
import static android.text.format.DateUtils.FORMAT_SHOW_YEAR;
import static android.text.format.DateUtils.formatDateTime;

import static com.hyperwallet.android.model.receipt.Receipt.Entries.CREDIT;
import static com.hyperwallet.android.model.receipt.Receipt.Entries.DEBIT;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.hyperwallet.android.model.receipt.Receipt;
import com.hyperwallet.android.ui.common.util.DateUtils;
import com.hyperwallet.android.ui.receipt.R;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.Locale;

final class ReceiptViewUtil {

    static final String AMOUNT_FORMAT = "###0.00";
    static final String DETAIL_TIMEZONE = "zzz";

    void setTransactionView(@NonNull final Receipt receipt, @NonNull final View view) {
        TextView transactionTypeIcon = view.findViewById(R.id.transaction_type_icon);
        TextView transactionTitle = view.findViewById(R.id.transaction_title);
        TextView transactionDate = view.findViewById(R.id.transaction_date);
        TextView transactionAmount = view.findViewById(R.id.transaction_amount);
        TextView transactionCurrency = view.findViewById(R.id.transaction_currency);
        DecimalFormat decimalFormat = new DecimalFormat(AMOUNT_FORMAT);
        double amount = Double.parseDouble(receipt.getAmount());
        String formattedAmount = decimalFormat.format(amount);

        if (CREDIT.equals(receipt.getEntry())) {
            transactionAmount.setTextColor(transactionAmount.getContext()
                    .getResources().getColor(R.color.positiveColor));
            transactionAmount.setText(transactionAmount.getContext()
                    .getString(R.string.credit_sign, formattedAmount));
            transactionTypeIcon.setTextColor(transactionTypeIcon.getContext()
                    .getResources().getColor(R.color.positiveColor));
            transactionTypeIcon.setBackground(transactionTypeIcon.getContext()
                    .getDrawable(R.drawable.circle_positive));
            transactionTypeIcon.setText(transactionTypeIcon.getContext().getText(R.string.credit));
        } else if (DEBIT.equals(receipt.getEntry())) {
            transactionAmount.setTextColor(transactionAmount.getContext()
                    .getResources().getColor(R.color.colorAccent));
            transactionAmount.setText(transactionAmount.getContext()
                    .getString(R.string.debit_sign, formattedAmount));
            transactionTypeIcon.setTextColor(transactionTypeIcon.getContext()
                    .getResources().getColor(R.color.colorAccent));
            transactionTypeIcon.setBackground(transactionTypeIcon.getContext()
                    .getDrawable(R.drawable.circle_negative));
            transactionTypeIcon.setText(transactionTypeIcon.getContext().getText(R.string.debit));
        }

        transactionCurrency.setText(receipt.getCurrency());
        transactionTitle.setText(getTransactionTitle(receipt.getType(), transactionTitle.getContext()));
        Date date = DateUtils.fromDateTimeString(receipt.getCreatedOn());
        transactionDate.setText(formatDateTime(view.getContext(), date.getTime(),
                FORMAT_SHOW_DATE | FORMAT_SHOW_YEAR));
    }

    String getTransactionTitle(@NonNull final String receiptType, @NonNull final Context context) {
        String showTitle = context.getResources().getString(R.string.unknown_type);
        int resourceId = context.getResources().getIdentifier(receiptType.toLowerCase(Locale.ROOT), "string",
                context.getPackageName());
        if (resourceId != 0) {
            showTitle = context.getResources().getString(resourceId);
        }

        return showTitle;
    }
}
