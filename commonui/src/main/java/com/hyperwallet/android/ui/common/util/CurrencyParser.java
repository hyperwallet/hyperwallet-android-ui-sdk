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
    private final HashMap<String, LocalDetails> localeList = new HashMap<>();
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
            LocalDetails locale = localeList.get(currency);
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
        localeList.put("AED",new LocalDetails("en","AE"));
        localeList.put("ALL",new LocalDetails("en","AL"));
        localeList.put("AMD",new LocalDetails("hy","AM"));
        localeList.put("ARS",new LocalDetails("es","AR"));
        localeList.put("AUD",new LocalDetails("en","AU"));
        localeList.put("BAM",new LocalDetails("hr","BA"));
        localeList.put("BDT",new LocalDetails("en","BD"));
        localeList.put("BGN",new LocalDetails("bg","BG"));
        localeList.put("BHD",new LocalDetails("en","US"));
        localeList.put("BOB",new LocalDetails("qu","BO"));
        localeList.put("BRL",new LocalDetails("en","BR"));
        localeList.put("BWP",new LocalDetails("en","BW"));
        localeList.put("CAD",new LocalDetails("en","CA"));
        localeList.put("CHF",new LocalDetails("en","CH"));
        localeList.put("CLP",new LocalDetails("es","CL"));
        localeList.put("CNH",new LocalDetails("en","CN"));
        localeList.put("CNY",new LocalDetails("en","CN"));
        localeList.put("COP",new LocalDetails("es","CO"));
        localeList.put("CZK",new LocalDetails("cs","CZ"));
        localeList.put("DKK",new LocalDetails("en","DK"));
        localeList.put("EEK",new LocalDetails("en","US"));
        localeList.put("EGP",new LocalDetails("en","US"));
        localeList.put("ETB",new LocalDetails("so","ET"));
        localeList.put("EUR",new LocalDetails("es","EA"));
        localeList.put("FJD",new LocalDetails("en","FJ"));
        localeList.put("GBP",new LocalDetails("kw","GB"));
        localeList.put("GHS",new LocalDetails("ee","GH"));
        localeList.put("GMD",new LocalDetails("en","GM"));
        localeList.put("HKD",new LocalDetails("en","HK"));
        localeList.put("HRK",new LocalDetails("es","HR"));
        localeList.put("HUF",new LocalDetails("hu","HU"));
        localeList.put("IDR",new LocalDetails("jv","ID"));
        localeList.put("ILS",new LocalDetails("he","IL"));
        localeList.put("INR",new LocalDetails("en","IN"));
        localeList.put("ISK",new LocalDetails("en","US"));
        localeList.put("JMD",new LocalDetails("en","JM"));
        localeList.put("JOD",new LocalDetails("en","us"));
        localeList.put("JPY",new LocalDetails("en","JP"));
        localeList.put("KES",new LocalDetails("guz","KE"));
        localeList.put("KHR",new LocalDetails("km","KH"));
        localeList.put("KRW",new LocalDetails("en","KR"));
        localeList.put("KWD",new LocalDetails("en","US"));
        localeList.put("KZT",new LocalDetails("ru","KZ"));
        localeList.put("LAK",new LocalDetails("lo","LA"));
        localeList.put("LKR",new LocalDetails("ta","LK"));
        localeList.put("LSL",new LocalDetails("en","US"));
        localeList.put("MAD",new LocalDetails("zgh","MA"));
        localeList.put("MGA",new LocalDetails("en","MG"));
        localeList.put("MRU",new LocalDetails("ff","MR"));
        localeList.put("MUR",new LocalDetails("en","MU"));
        localeList.put("MWK",new LocalDetails("en","MW"));
        localeList.put("MXN",new LocalDetails("en","MX"));
        localeList.put("MYR",new LocalDetails("en","MY"));
        localeList.put("MZN",new LocalDetails("mgh","MZ"));
        localeList.put("NAD",new LocalDetails("af","NA"));
        localeList.put("NGN",new LocalDetails("en","NG"));
        localeList.put("NOK",new LocalDetails("nn","NO"));
        localeList.put("NPR",new LocalDetails("en","US"));
        localeList.put("NZD",new LocalDetails("en","PN"));
        localeList.put("OMR",new LocalDetails("ae","OM"));
        localeList.put("PEN",new LocalDetails("en","PE"));
        localeList.put("PGK",new LocalDetails("en","PG"));
        localeList.put("PHP",new LocalDetails("ceb","PH"));
        localeList.put("PKR",new LocalDetails("en","PK"));
        localeList.put("PLN",new LocalDetails("pl","PL"));
        localeList.put("QAR",new LocalDetails("en","US"));
        localeList.put("RON",new LocalDetails("ro","RO"));
        localeList.put("RSD",new LocalDetails("sr","Latn_RS"));
        localeList.put("RUB",new LocalDetails("ru","RU"));
        localeList.put("SBD",new LocalDetails("en","SB"));
        localeList.put("SEK",new LocalDetails("en","SE"));
        localeList.put("SGD",new LocalDetails("ta","SG"));
        localeList.put("SVG",new LocalDetails("en","US"));
        localeList.put("SZL",new LocalDetails("en","SZ"));
        localeList.put("THB",new LocalDetails("th","TH"));
        localeList.put("TND",new LocalDetails("ar","TN"));
        localeList.put("TOP",new LocalDetails("to","TO"));
        localeList.put("TRY",new LocalDetails("tr","TR"));
        localeList.put("TWD",new LocalDetails("zh","TW"));
        localeList.put("UGX",new LocalDetails("cgg","UG"));
        localeList.put("USD",new LocalDetails("es","US"));
        localeList.put("UYU",new LocalDetails("es","UY"));
        localeList.put("VND",new LocalDetails("vi","VN"));
        localeList.put("VUV",new LocalDetails("en","VU"));
        localeList.put("WST",new LocalDetails("en","WS"));
        localeList.put("XPF",new LocalDetails("fr","PF"));
        localeList.put("ZAR",new LocalDetails("en","ZA"));
        localeList.put("ZMW",new LocalDetails("en","ZM"));
    }

    public HashMap<String, LocalDetails> getLocaleList()
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
