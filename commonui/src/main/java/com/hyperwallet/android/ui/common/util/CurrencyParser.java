package com.hyperwallet.android.ui.common.util;

import static com.hyperwallet.android.model.transfer.Transfer.EMPTY_STRING;

import android.content.Context;

import androidx.annotation.VisibleForTesting;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

public class CurrencyParser {
    private static CurrencyParser instance;
    private static final String CURRENCY_LIST = "currency.json";
    private List<CurrencyDetails> currencyList;
    private static final String REGEX_REMOVE_EMPTY_SPACE = "^\\s+|\\s+$";


    private CurrencyParser(Context context) {
        currencyList = populateCurrencyList(readJSONFromAsset(context));
    }

    public static CurrencyParser getInstance(Context context) {
        if (instance == null) {
            instance = new CurrencyParser(context);
        }
        return instance;
    }

    private String readJSONFromAsset(Context context) {
        String json = null;
        try {
            InputStream is = context.getAssets().open(CURRENCY_LIST);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    private List<CurrencyDetails> populateCurrencyList(String currencyList) {
        List<CurrencyDetails> mCurrencyDetailsList = new ArrayList<>();

        try {
            JSONArray jsonArr = new JSONArray(currencyList);
            for (int i = 0; i < jsonArr.length(); i++) {
                JSONObject jsonObj = jsonArr.getJSONObject(i);
                CurrencyDetails currencyDetails = new CurrencyDetails();
                currencyDetails.setCurrencyCode(jsonObj.getString("currencycode"));
                currencyDetails.setSymbol(jsonObj.getString("symbol"));
                currencyDetails.setDecimals(jsonObj.getInt("decimals"));
                mCurrencyDetailsList.add(currencyDetails);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return mCurrencyDetailsList;
    }

    /**
     * Formats the currency as per currency code.
     *
     * @param currency Any currency symbol.
     * @param amount   Any valid number in decimal.
     * @return Returns the formatted number as per currency.
     */
    public String formatCurrency(String currency, String amount) {
        int numberOfFractions = getNumberOfFractionDigits(currency);
        NumberFormat format = NumberFormat.getCurrencyInstance();
        format.setMinimumFractionDigits(numberOfFractions);
        format.setCurrency(Currency.getInstance(currency));
        return format.format(Double.parseDouble(amount));
    }

    @VisibleForTesting
    int getNumberOfFractionDigits(String currencyCode) {
        for (CurrencyDetails list : currencyList) {
            if (list.getCurrencyCode().equals(currencyCode)) {
                return list.getDecimals();
            }
        }

        return 0;
    }

    /**
     * Formats the currency as per currency.json code.
     *
     * @param currency Any currency symbol.
     * @param amount   Any valid number in decimal.
     * @return Returns the formatted number as per currency.json.
     */

    public String formatCurrencyWithSymbol(String currency, String amount) {
        DecimalFormat currencyFormatter = (DecimalFormat) DecimalFormat.getCurrencyInstance();
        CurrencyDetails currencyDetails = getCurrency(currency);
        currencyFormatter.setMinimumFractionDigits(currencyDetails == null ? 0 : currencyDetails.getDecimals());
        currencyFormatter.setCurrency(Currency.getInstance(currency));
        DecimalFormatSymbols decimalFormatSymbols = currencyFormatter.getDecimalFormatSymbols();
        decimalFormatSymbols.setCurrencySymbol("");
        currencyFormatter.setDecimalFormatSymbols(decimalFormatSymbols);
        String formattedAmount = currencyFormatter.format(Double.parseDouble(amount)).replaceAll(REGEX_REMOVE_EMPTY_SPACE, EMPTY_STRING);
        return currencyDetails == null ? "" : currencyDetails.getSymbol() + formattedAmount;
    }

    public CurrencyDetails getCurrency(String currencyCode) {
        for (CurrencyDetails list : currencyList) {
            if (list.getCurrencyCode().equals(currencyCode)) {
                return list;
            }
        }
        return null;
    }

    /**
     * truncate decimals for given value
     *
     * @param value Any value in string.
     * @param noOfDecimals   number of decimal to be truncate.
     * @return Returns truncated decimal value.
     */
    public static String getValueWithTruncateDecimals(String value, int noOfDecimals) {
        if (value != null) {
            StringBuilder builder = new StringBuilder();
            String[] amount = value.split("\\.");
            if (amount.length == 2) {
                String wholeNumber = amount[0];
                String decimal = amount[1];
                if (decimal.length() > noOfDecimals) {
                    decimal = decimal.substring(0, noOfDecimals);
                }
                return builder.append(wholeNumber).append(".").append(decimal).toString();
            }
            return value;
        } else {
            return "";
        }
    }
}
