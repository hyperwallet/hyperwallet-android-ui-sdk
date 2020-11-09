package com.hyperwallet.android.ui.common.util;

import java.text.NumberFormat;
import java.util.Currency;

/**
 * Common UI SDK utility class for formatting the currency.
 */
public class Utils {

    /**
     * Formats the currency as per currency code.
     *
     * @param currency Any currency symbol.
     * @param amount   Any valid number in decimal.
     * @return Returns the formatted number as per currency.
     */
    public static String formatCurrency(String currency, String amount) {
        NumberFormat format = NumberFormat.getCurrencyInstance();
        format.setCurrency(Currency.getInstance(currency));
        return format.format(Double.parseDouble(amount));
    }
}
