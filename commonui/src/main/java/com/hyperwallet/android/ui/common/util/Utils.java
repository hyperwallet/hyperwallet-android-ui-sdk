package com.hyperwallet.android.ui.common.util;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

/**
 * Common UI SDK utility class for formatting the currency.
 */
public class Utils {

    /**
     * Currencies without Fraction
     */
    private static final List<String> NODECIMAL_CURRENCY = new ArrayList<String>() {{
        add("JPY");
        add("ALL");
        add("COP");
        add("SGD");
        add("PKR");
        add("LAK");
        add("IDR");
        add("RSD");
        add("VND");
        add("CLP");
        add("UGX");
    }};

    /**
     * Currencies with three Fraction
     */
    private static final List<String> THREE_DECIMAL_CURRENCY = new ArrayList<String>() {{
        add("JOD");
        add("TND");
    }};

    /**
     * Currencies with three Fraction
     */
    private static final List<String> SINGLE_DECIMAL_CURRENCY = new ArrayList<String>() {{
        add("AMD");
    }};

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

        if (NODECIMAL_CURRENCY.contains(currency)) {
            format.setMaximumFractionDigits(0);
        } else if (THREE_DECIMAL_CURRENCY.contains(currency)) {
            format.setMinimumFractionDigits(3);
        } else if (SINGLE_DECIMAL_CURRENCY.contains(currency)) {
            format.setMinimumFractionDigits(1);
        }
        return format.format(Double.parseDouble(amount));
    }
}
