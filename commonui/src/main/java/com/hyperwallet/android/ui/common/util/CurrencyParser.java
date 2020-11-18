package com.hyperwallet.android.ui.common.util;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

public class CurrencyParser {
    private static CurrencyParser instance;
    private static final String CURRENCY_LIST = "currency.json";
    private List<CurrencyDetails> currencyList;

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

    private int getNumberOfFractionDigits(String currencyCode) {
        for (CurrencyDetails list : currencyList) {
            if (list.getCurrencyCode().equals(currencyCode)) {
                return list.getDecimals();
            }
        }

        return 0;
    }
}
