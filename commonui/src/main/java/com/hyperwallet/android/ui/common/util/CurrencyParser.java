package com.hyperwallet.android.ui.common.util;

import android.content.Context;

import androidx.annotation.VisibleForTesting;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class CurrencyParser {
    private static CurrencyParser instance;
    private static final String CURRENCY_LIST = "currency.json";
    private final List<CurrencyDetails> currencyList;
    private final HashMap<String, LocaleDetails> localeList = new HashMap<>();
    private static final String REGEX_REMOVE_EMPTY_SPACE = "^\\s+|\\s+$";


    private CurrencyParser(Context context) {
        currencyList = populateCurrencyList(readJSONFromAsset(context));
        setLocaleList();
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
        DecimalFormat currencyFormatter;
        if(localeList.containsKey(currency)) {
            LocaleDetails locale = localeList.get(currency);
            currencyFormatter = (DecimalFormat) DecimalFormat.getCurrencyInstance((new Locale(locale.getLanguage(),locale.getCountryCode())));
        }else {
            currencyFormatter = (DecimalFormat) DecimalFormat.getCurrencyInstance();
        }
        CurrencyDetails currencyDetails = getCurrency(currency);
        currencyFormatter.setMinimumFractionDigits(currencyDetails == null ? 0 : currencyDetails.getDecimals());
        currencyFormatter.setCurrency(Currency.getInstance(currency));
        DecimalFormatSymbols decimalFormatSymbols = currencyFormatter.getDecimalFormatSymbols();
        decimalFormatSymbols.setCurrencySymbol("");
        currencyFormatter.setDecimalFormatSymbols(decimalFormatSymbols);
        String formattedAmount = currencyFormatter.format(Double.parseDouble(amount));
        return currencyDetails == null ? "" : currencyDetails.getSymbol() + formattedAmount.trim();
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
     *
     */
    public void setLocaleList() {
        localeList.clear();
        localeList.put("AED",new LocaleDetails("en","AE"));
        localeList.put("ALL",new LocaleDetails("en","US"));
        localeList.put("AMD",new LocaleDetails("hy","AM"));
        localeList.put("ARS",new LocaleDetails("es","AR"));
        localeList.put("AUD",new LocaleDetails("en","AU"));
        localeList.put("BAM",new LocaleDetails("hr","BA"));
        localeList.put("BDT",new LocaleDetails("en","BD"));
        localeList.put("BGN",new LocaleDetails("bg","BG"));
        localeList.put("BHD",new LocaleDetails("en","US"));
        localeList.put("BOB",new LocaleDetails("qu","BO"));
        localeList.put("BRL",new LocaleDetails("en","BR"));
        localeList.put("BWP",new LocaleDetails("en","BW"));
        localeList.put("CAD",new LocaleDetails("en","CA"));
        localeList.put("CHF",new LocaleDetails("en","CH"));
        localeList.put("CLP",new LocaleDetails("es","CL"));
        localeList.put("CNH",new LocaleDetails("en","CN"));
        localeList.put("CNY",new LocaleDetails("en","CN"));
        localeList.put("COP",new LocaleDetails("es","CO"));
        localeList.put("CZK",new LocaleDetails("cs","CZ"));
        localeList.put("DKK",new LocaleDetails("en","DK"));
        localeList.put("EEK",new LocaleDetails("en","US"));
        localeList.put("EGP",new LocaleDetails("en","US"));
        localeList.put("ETB",new LocaleDetails("so","ET"));
        localeList.put("EUR",new LocaleDetails("es","EA"));
        localeList.put("FJD",new LocaleDetails("en","FJ"));
        localeList.put("GBP",new LocaleDetails("kw","GB"));
        localeList.put("GHS",new LocaleDetails("ee","GH"));
        localeList.put("GMD",new LocaleDetails("en","GM"));
        localeList.put("HKD",new LocaleDetails("en","HK"));
        localeList.put("HRK",new LocaleDetails("es","HR"));
        localeList.put("HUF",new LocaleDetails("hu","HU"));
        localeList.put("IDR",new LocaleDetails("jv","ID"));
        localeList.put("ILS",new LocaleDetails("he","IL"));
        localeList.put("INR",new LocaleDetails("en","IN"));
        localeList.put("ISK",new LocaleDetails("en","US"));
        localeList.put("JMD",new LocaleDetails("en","JM"));
        localeList.put("JOD",new LocaleDetails("en","us"));
        localeList.put("JPY",new LocaleDetails("en","JP"));
        localeList.put("KES",new LocaleDetails("guz","KE"));
        localeList.put("KHR",new LocaleDetails("km","KH"));
        localeList.put("KRW",new LocaleDetails("en","KR"));
        localeList.put("KWD",new LocaleDetails("en","US"));
        localeList.put("KZT",new LocaleDetails("ru","KZ"));
        localeList.put("LAK",new LocaleDetails("lo","LA"));
        localeList.put("LKR",new LocaleDetails("ta","LK"));
        localeList.put("LSL",new LocaleDetails("en","US"));
        localeList.put("MAD",new LocaleDetails("zgh","MA"));
        localeList.put("MGA",new LocaleDetails("en","MG"));
        localeList.put("MRU",new LocaleDetails("ff","MR"));
        localeList.put("MUR",new LocaleDetails("en","MU"));
        localeList.put("MWK",new LocaleDetails("en","MW"));
        localeList.put("MXN",new LocaleDetails("en","MX"));
        localeList.put("MYR",new LocaleDetails("en","MY"));
        localeList.put("MZN",new LocaleDetails("mgh","MZ"));
        localeList.put("NAD",new LocaleDetails("af","NA"));
        localeList.put("NGN",new LocaleDetails("en","NG"));
        localeList.put("NOK",new LocaleDetails("nn","NO"));
        localeList.put("NPR",new LocaleDetails("en","US"));
        localeList.put("NZD",new LocaleDetails("en","PN"));
        localeList.put("OMR",new LocaleDetails("ae","OM"));
        localeList.put("PEN",new LocaleDetails("en","PE"));
        localeList.put("PGK",new LocaleDetails("en","PG"));
        localeList.put("PHP",new LocaleDetails("ceb","PH"));
        localeList.put("PKR",new LocaleDetails("en","PK"));
        localeList.put("PLN",new LocaleDetails("pl","PL"));
        localeList.put("QAR",new LocaleDetails("en","US"));
        localeList.put("RON",new LocaleDetails("ro","RO"));
        localeList.put("RSD",new LocaleDetails("sr","Latn_RS"));
        localeList.put("RUB",new LocaleDetails("ru","RU"));
        localeList.put("SBD",new LocaleDetails("en","SB"));
        localeList.put("SEK",new LocaleDetails("en","SE"));
        localeList.put("SGD",new LocaleDetails("ta","SG"));
        localeList.put("SVG",new LocaleDetails("en","US"));
        localeList.put("SZL",new LocaleDetails("en","SZ"));
        localeList.put("THB",new LocaleDetails("th","TH"));
        localeList.put("TND",new LocaleDetails("en","TN"));
        localeList.put("TOP",new LocaleDetails("to","TO"));
        localeList.put("TRY",new LocaleDetails("tr","TR"));
        localeList.put("TWD",new LocaleDetails("zh","TW"));
        localeList.put("UGX",new LocaleDetails("cgg","UG"));
        localeList.put("USD",new LocaleDetails("es","US"));
        localeList.put("UYU",new LocaleDetails("es","UY"));
        localeList.put("VND",new LocaleDetails("vi","VN"));
        localeList.put("VUV",new LocaleDetails("en","VU"));
        localeList.put("WST",new LocaleDetails("en","WS"));
        localeList.put("XPF",new LocaleDetails("fr","PF"));
        localeList.put("ZAR",new LocaleDetails("en","ZA"));
        localeList.put("ZMW",new LocaleDetails("en","ZM"));
    }

    public HashMap<String, LocaleDetails> getLocaleList()
    {
        return localeList;
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
            NumberFormat nf = NumberFormat.getNumberInstance();
            nf.setMaximumFractionDigits(noOfDecimals);
            nf.setRoundingMode(RoundingMode.HALF_UP);
            double amount = Double.parseDouble(value);
            return nf.format(amount);
        } else {
            return "";
        }
    }
}
