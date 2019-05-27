/*
 * Copyright 2018 Hyperwallet
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

package com.hyperwallet.android.ui.transfermethod;

import android.content.Context;

import androidx.annotation.NonNull;

import com.hyperwallet.android.hyperwallet_ui.R;
import com.hyperwallet.android.model.graphql.HyperwalletFee;

import java.util.List;

public class FeeFormatter {

    public static String getFormattedFee(@NonNull final Context context, @NonNull final List<HyperwalletFee> fees) {
        String formattedString = context.getResources().getString(R.string.unknown);
        if (fees.size() == 1) {
            formattedString = getSingleFormattedFee(context, fees, formattedString);
        } else {
            formattedString = getMixFormattedFee(context, fees, formattedString);
        }
        return formattedString;
    }

    private static String getSingleFormattedFee(@NonNull Context context, @NonNull List<HyperwalletFee> fees,
            String formattedString) {
        HyperwalletFee fee = fees.get(0);
        if (HyperwalletFee.FeeRate.FLAT.equals(fee.getFeeRateType())) {
            formattedString = context.getResources().getString(R.string.fee_flat_formatter, fee.getCurrency(),
                    fee.getValue());
        } else if (HyperwalletFee.FeeRate.PERCENT.equals(fee.getFeeRateType())) {
            formattedString = getPercentFormattedFee(context, fee);
        }
        return formattedString;
    }

    // we expect at the most 2 fees and in that case one should be flat and other percent
    // which will be formatted to USD 3.00 + 3% (Min: USD 1.00, Max: USD 15.00)
    private static String getMixFormattedFee(@NonNull Context context, @NonNull List<HyperwalletFee> fees,
            String formattedString) {
        HyperwalletFee flatFee = null;
        HyperwalletFee percentFee = null;
        for (HyperwalletFee fee : fees) {
            if (HyperwalletFee.FeeRate.FLAT.equals(fee.getFeeRateType())) {
                flatFee = fee;
            } else if (HyperwalletFee.FeeRate.PERCENT.equals(fee.getFeeRateType())) {
                percentFee = fee;
            }
        }
        if (flatFee != null && percentFee != null) {
            String minimumAmount = percentFee.getMin();
            String maximumAmount = percentFee.getMax();
            if (maximumAmount.isEmpty() && minimumAmount.isEmpty()) {
                formattedString = context.getResources().getString(R.string.fee_mix_no_min_and_max_formatter,
                        flatFee.getCurrency(), flatFee.getValue(), percentFee.getValue());
            } else if (maximumAmount.isEmpty()) {
                formattedString = context.getResources().getString(R.string.fee_mix_only_min_formatter,
                        flatFee.getCurrency(), flatFee.getValue(), percentFee.getValue(), minimumAmount);
            } else if (minimumAmount.isEmpty()) {
                formattedString = context.getResources().getString(R.string.fee_mix_only_max_formatter,
                        flatFee.getCurrency(), flatFee.getValue(), percentFee.getValue(), maximumAmount);
            } else {
                formattedString = context.getResources().getString(R.string.fee_mix_formatter, flatFee.getCurrency(),
                        flatFee.getValue(), percentFee.getValue(), minimumAmount, maximumAmount);
            }
        }
        return formattedString;
    }


    private static String getPercentFormattedFee(@NonNull final Context context, @NonNull final HyperwalletFee fee) {
        String formattedFee;
        String minimumAmount = fee.getMin();
        String maximumAmount = fee.getMax();
        if (maximumAmount.isEmpty() && minimumAmount.isEmpty()) {
            formattedFee = context.getResources().getString(R.string.fee_percent_no_min_and_max_formatter,
                    fee.getValue());
        } else if (maximumAmount.isEmpty()) {
            formattedFee = context.getResources().getString(R.string.fee_percent_only_min_formatter, fee.getValue(),
                    fee.getCurrency(), minimumAmount);
        } else if (minimumAmount.isEmpty()) {
            formattedFee = context.getResources().getString(R.string.fee_percent_only_max_formatter, fee.getValue(),
                    fee.getCurrency(), maximumAmount);
        } else {
            formattedFee = context.getResources().getString(R.string.fee_percent_formatter, fee.getValue(),
                    fee.getCurrency(), minimumAmount, maximumAmount);
        }
        return formattedFee;
    }
}
