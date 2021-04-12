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

package com.hyperwallet.android.ui.transfermethod.view;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hyperwallet.android.model.graphql.Fee;
import com.hyperwallet.android.model.graphql.ProcessingTime;
import com.hyperwallet.android.ui.R;

import java.util.Currency;
import java.util.List;
import java.util.Locale;

public class FeeFormatter {

    public static String getFormattedFee(@NonNull final Context context, @NonNull final List<Fee> fees) {
        String formattedString = context.getResources().getString(R.string.noFee);
        System.out.println(" fees size " + fees.size());
        if (fees.size() == 1) {
            formattedString = getSingleFormattedFee(context, fees, formattedString);
        } else {
            formattedString = getMixFormattedFee(context, fees, formattedString);
        }
        return formattedString;
    }

    private static String getSingleFormattedFee(@NonNull Context context, @NonNull List<Fee> fees,
            String formattedString) {
        Fee fee = fees.get(0);
        if (Fee.FeeRate.FLAT.equals(fee.getFeeRateType())) {
            System.out.println("Flat fee value " + fee.getValue());
            if (!isValidFee(fee.getValue())) {
                return formattedString;
            }
            formattedString = context.getResources().getString(R.string.fee_flat_formatter,
                    Currency.getInstance(fee.getCurrency()).getSymbol(Locale.getDefault()), fee.getValue());
        } else if (Fee.FeeRate.PERCENT.equals(fee.getFeeRateType())) {
            System.out.println("Percentage fee value " + fee.getValue());
            formattedString = getPercentFormattedFee(context, fee, formattedString);
        }
        System.out.println("result in single" + formattedString);
        return formattedString;
    }

    // we expect at the most 2 fees and in that case one should be flat and other percent
    // which will be formatted to USD 3.00 + 3% (Min: USD 1.00, Max: USD 15.00)
    private static String getMixFormattedFee(@NonNull Context context, @NonNull List<Fee> fees,
            String formattedString) {
        Fee flatFee = null;
        Fee percentFee = null;
        for (Fee fee : fees) {
            if (Fee.FeeRate.FLAT.equals(fee.getFeeRateType())) {
                flatFee = fee;
            } else if (Fee.FeeRate.PERCENT.equals(fee.getFeeRateType())) {
                percentFee = fee;
            }
        }
        if (flatFee != null && percentFee != null) {
            System.out.println("mixed formatted value flatFee " + flatFee.getValue() + " percent fee " + percentFee.getValue());
            String minimumAmount = percentFee.getMin();
            String maximumAmount = percentFee.getMax();

            if (!isValidFee(flatFee.getValue()) && !isValidFee(percentFee.getValue())) {
                return formattedString;
            } else if (maximumAmount.isEmpty() && minimumAmount.isEmpty() && !isValidFee(percentFee.getValue())) {
                formattedString = context.getResources().getString(R.string.fee_flat_formatter,
                        Currency.getInstance(flatFee.getCurrency()).getSymbol(Locale.getDefault()), flatFee.getValue());
            } else if (maximumAmount.isEmpty() && minimumAmount.isEmpty() && !isValidFee(flatFee.getValue())) {
                formattedString = context.getResources().getString(R.string.fee_percent_no_min_and_max_formatter,
                        percentFee.getValue());
            } else if (maximumAmount.isEmpty() && minimumAmount.isEmpty()) {
                formattedString = context.getResources().getString(R.string.fee_mix_no_min_and_max_formatter,
                        Currency.getInstance(flatFee.getCurrency()).getSymbol(Locale.getDefault()),
                        flatFee.getValue(), percentFee.getValue());
            } else if (maximumAmount.isEmpty() && !isValidFee(percentFee.getValue())) {
                formattedString = context.getResources().getString(R.string.fee_flat_formatter,
                        Currency.getInstance(flatFee.getCurrency()).getSymbol(Locale.getDefault()), flatFee.getValue());
            } else if (maximumAmount.isEmpty() && !isValidFee(flatFee.getValue())) {
                formattedString = context.getResources().getString(R.string.fee_percent_only_max_formatter,
                        percentFee.getValue(),
                        percentFee.getCurrency(), maximumAmount);
            } else if (maximumAmount.isEmpty()) {
                formattedString = context.getResources().getString(R.string.fee_mix_only_min_formatter,
                        Currency.getInstance(flatFee.getCurrency()).getSymbol(Locale.getDefault()),
                        flatFee.getValue(), percentFee.getValue(), minimumAmount);
            } else if (minimumAmount.isEmpty() && !isValidFee(percentFee.getValue())) {
                formattedString = context.getResources().getString(R.string.fee_flat_formatter,
                        Currency.getInstance(flatFee.getCurrency()).getSymbol(Locale.getDefault()), flatFee.getValue());
            } else if (minimumAmount.isEmpty() && !isValidFee(flatFee.getValue())) {
                formattedString = context.getResources().getString(R.string.fee_percent_only_max_formatter,
                        percentFee.getValue(),
                        percentFee.getCurrency(), maximumAmount);
            } else if (minimumAmount.isEmpty()) {
                formattedString = context.getResources().getString(R.string.fee_mix_only_max_formatter,
                        Currency.getInstance(flatFee.getCurrency()).getSymbol(Locale.getDefault()),
                        flatFee.getValue(), percentFee.getValue(), maximumAmount);
            } else {
                formattedString = context.getResources().getString(R.string.fee_mix_formatter,
                        Currency.getInstance(flatFee.getCurrency()).getSymbol(Locale.getDefault()),
                        flatFee.getValue(), percentFee.getValue(), minimumAmount, maximumAmount);
            }
        }
        System.out.println("result in mixed " + formattedString);
        return formattedString;
    }


    private static String getPercentFormattedFee(@NonNull final Context context, @NonNull final Fee fee,
            String formattedString) {
        String formattedFee;
        String minimumAmount = fee.getMin();
        String maximumAmount = fee.getMax();

        if (!isValidFee(fee.getValue())) {
            return formattedString;
        }
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


    protected static boolean isFeeAvailable(@Nullable final List<?> fees) {
        return fees != null && !fees.isEmpty();
    }

    protected static boolean isProcessingTimeAvailable(@Nullable final ProcessingTime processingTime) {
        return processingTime != null && !TextUtils.isEmpty(processingTime.getValue());
    }

    protected static boolean isZeroFeeAvailable(@NonNull final Context context, String fee) {
        return fee.contains(context.getResources().getString(R.string.noFee));
    }

    protected static boolean isValidFee(String fee) {
        if (TextUtils.isEmpty(fee)) {
            return false;
        } else {
            return Double.parseDouble(fee) != 0.0;
        }
    }
}
