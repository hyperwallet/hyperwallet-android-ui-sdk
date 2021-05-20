package com.hyperwallet.android.ui.common.util;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;
import java.util.Map;

@Config
@RunWith(RobolectricTestRunner.class)
public class CurrencyParserTest {
    private Map<String, String> currenciesMap = new HashMap<String, String>() {
        {
            put("ALL", "ALL1,000,000.00");         // Albania Currency
            put("ARS", "ARS1,000,000.00");      // Argentina Currency
            put("AMD", "AMD1,000,000.00");       // Armenia Currency
            put("AUD", "AUD1,000,000.00");      // Australia Currency
            put("BDT", "BDT1,000,000.00");      // Bangladesh Currency
            put("BRL", "BRL1,000,000.00");      // Brazil Currency
            put("BGN", "BGN1,000,000.00");      // Bulgaria Currency
            put("KHR", "KHR1,000,000.00");      // Cambodia Currency
            put("CAD", "CAD1,000,000.00");      // Canada Currency
            put("CLP", "CLP1,000,000");         // Chile Currency
            put("CNY", "CNY1,000,000.00");      // China Currency
            put("COP", "COP1,000,000.00");         // Colombia Currency
            put("HRK", "HRK1,000,000.00");      // Croatia Currency
            put("CZK", "CZK1,000,000.00");      // Czech Republic Currency
            put("DKK", "DKK1,000,000.00");      // Denmark Currency
            put("EGP", "EGP1,000,000.00");      // Egypt Currency
            put("EUR", "EUR1,000,000.00");      // Austria Currency
            put("HKD", "HKD1,000,000.00");      // Hong Kong Currency
            put("HUF", "HUF1,000,000.00");      // Hungary Currency
            put("INR", "INR1,000,000.00");       // India Currency
            put("IDR", "IDR1,000,000");         // Indonesia Currency
            put("JMD", "JMD1,000,000.00");      // Jamaica Currency
            put("JPY", "JPY1,000,000");         // Japan Currency
            put("JOD", "JOD1,000,000.00");     // Jordan Currency
            put("KZT", "KZT1,000,000.00");      // Kazakhstan Currency
            put("KES", "KES1,000,000.00");      // Kenya Currency
            put("LAK", "LAK1,000,000.00");         // Laos Currency
            put("MYR", "MYR1,000,000.00");      // Malaysia Currency
            put("MXN", "MXN1,000,000.00");      // Mexico Currency
            put("MAD", "MAD1,000,000.00");      // Morocco Currency
            put("ILS", "ILS1,000,000.00");      // Israel Currency
            put("TWD", "TWD1,000,000");      // Taiwan Currency
            put("TRY", "TRY1,000,000.00");      // Turkey Currency
            put("NZD", "NZD1,000,000.00");      // New Zealand Currency
            put("NGN", "NGN1,000,000.00");      // Nigeria Currency
            put("NOK", "NOK1,000,000.00");      // Norway Currency
            put("PKR", "PKR1,000,000.00");         // Pakistan Currency
            put("PEN", "PEN1,000,000.00");      // Peru Currency
            put("PHP", "PHP1,000,000.00");      // Philippines Currency
            put("PLN", "PLN1,000,000.00");      // Poland Currency
            put("GBP", "GBP1,000,000.00");      // Isle of Man
            put("RON", "RON1,000,000.00");      // Romania Currency
            put("RUB", "RUB1,000,000.00");      // Russia Currency
            put("RSD", "RSD1,000,000.00");         // Serbia Currency
            put("SGD", "SGD1,000,000.00");      // Singapore Currency
            put("ZAR", "ZAR1,000,000.00");      // South Africa Currency
            put("KRW", "KRW1,000,000");         // South Korea Currency
            put("LKR", "LKR1,000,000.00");      // Sri Lanka Currency
            put("SEK", "SEK1,000,000.00");      // Sweden Currency
            put("CHF", "CHF1,000,000.00");      // Switzerland Currency
            put("THB", "THB1,000,000.00");      // Thailand Currency
            put("TND", "TND1,000,000.000");     // Tunisia Currency
            put("AED", "AED1,000,000.00");      // United Arab Emirates Currency
            put("UGX", "UGX1,000,000");         // Uganda Currency
            put("USD", "$1,000,000.00");      // United States Currency
            put("VND", "VND1,000,000.00");         // Vietnam Currency
        }
    };

    @Test
    public void testAllCurrencyFormats() {
        String amount = "1000000";

        for (Map.Entry<String, String> currencyMap : currenciesMap.entrySet()) {
            Context context = ApplicationProvider.getApplicationContext();
            String currency = CurrencyParser.getInstance(context).formatCurrency(currencyMap.getKey(), amount);
            assertThat(currency, is(currencyMap.getValue()));
        }
    }

    @Test
    public void testCurrencyFormatWithSymbol() {
        String amount = "1000000";
            Context context = ApplicationProvider.getApplicationContext();
            String currency = CurrencyParser.getInstance(context).formatCurrencyWithSymbol("USD", amount);
            assertThat(currency, is("$1,000,000.00"));
    }
    @Test
    public void testGetNumberOfFractionDigits_ThreeDigitDecimal(){
        Context context = ApplicationProvider.getApplicationContext();
        int noOfDigits = CurrencyParser.getInstance(context).getNumberOfFractionDigits("TND");
        assertThat(noOfDigits, is(3));
    }

    @Test
    public void testGetCurrency() {
        Context context = ApplicationProvider.getApplicationContext();
        CurrencyDetails currencyDetails = CurrencyParser.getInstance(context).getCurrency("TND");
        assertThat(currencyDetails.getSymbol(), is("د.ت"));
        assertThat(currencyDetails.getDecimals(),is(3));
    }

    @Test
    public void testGetNumberOfFractionDigits_NoDigitDecimal(){
        Context context = ApplicationProvider.getApplicationContext();
        int noOfDigits = CurrencyParser.getInstance(context).getNumberOfFractionDigits("JPY");
        assertThat(noOfDigits, is(0));
    }

    @Test
    public void testFXRateWithFourDecimal()
    {
        assertThat("1.2346",is(CurrencyParser.getValueWithTruncateDecimals("1.234567",4)));
        assertThat("1.234",is(CurrencyParser.getValueWithTruncateDecimals("1.234",4)));
        assertThat("1.0001",is(CurrencyParser.getValueWithTruncateDecimals("1.000056",4)));
        assertThat("1",is(CurrencyParser.getValueWithTruncateDecimals("1",4)));
        assertThat("",is(CurrencyParser.getValueWithTruncateDecimals(null,4)));
        assertThat("1.235",is(CurrencyParser.getValueWithTruncateDecimals("1.234567",3)));
        assertThat("1.23",is(CurrencyParser.getValueWithTruncateDecimals("1.234567",2)));
        assertThat("1.2",is(CurrencyParser.getValueWithTruncateDecimals("1.234567",1)));
    }
}
