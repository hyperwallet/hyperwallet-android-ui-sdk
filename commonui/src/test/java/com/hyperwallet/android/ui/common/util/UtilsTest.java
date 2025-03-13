package com.hyperwallet.android.ui.common.util;

import static org.hamcrest.CoreMatchers.is;
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
public class UtilsTest {
    private Map<String, String> currenciesMap = new HashMap<String, String>() {
        {
            put("ALL", "L1,000,000.00");         // Albania Currency
            put("ARS", "$1.000.000,00");      // Argentina Currency
            put("AMD", "֏1,000,000.00");       // Armenia Currency
            put("AUD", "A$1,000,000.00");      // Australia Currency
            put("BDT", "Tk1,000,000.00");      // Bangladesh Currency
            put("BRL", "R$1,000,000.00");      // Brazil Currency
            put("BGN", "лв.1 000 000,00");      // Bulgaria Currency
            put("KHR", "៛1,000,000.00");      // Cambodia Currency
            put("CAD", "$1,000,000.00");      // Canada Currency
            put("CLP", "$1.000.000");         // Chile Currency
            put("CNY", "¥1,000,000.00");      // China Currency
            put("COP", "$1.000.000,00");         // Colombia Currency
            put("HRK", "kn1.000.000,00");      // Croatia Currency
            put("CZK", "Kč1 000 000,00");      // Czech Republic Currency
            put("DKK", "kr1,000,000.00");      // Denmark Currency
            put("EGP", "E£1,000,000.00");      // Egypt Currency
            put("EUR", "€1.000.000,00");      // Austria Currency
            put("HKD", "HK$1,000,000.00");      // Hong Kong Currency
            put("HUF", "Ft1 000 000,00");      // Hungary Currency
            put("INR", "₹1,000,000.00");       // India Currency
            put("IDR", "rp1,000,000");         // Indonesia Currency
            put("JMD", "$1,000,000.00");      // Jamaica Currency
            put("JPY", "¥1,000,000");         // Japan Currency
            put("JOD", "د.ا1,000,000.00");     // Jordan Currency
            put("KZT", "₸1 000 000,00");      // Kazakhstan Currency
            put("KES", "KSh1,000,000.00");      // Kenya Currency
            put("LAK", "₭1,000,000.00");         // Laos Currency
            put("MYR", "RM1,000,000.00");      // Malaysia Currency
            put("MXN", "$1,000,000.00");      // Mexico Currency
            put("MAD", "د.م.1,000,000.00");      // Morocco Currency
            put("ILS", "₪1,000,000.00");      // Israel Currency
            put("TWD", "NT$1,000,000");      // Taiwan Currency
            put("TRY", "TL1.000.000,00");      // Turkey Currency
            put("NZD", "NZ$1,000,000.00");      // New Zealand Currency
            put("NGN", "₦1,000,000.00");      // Nigeria Currency
            put("NOK", "kr1 000 000,00");      // Norway Currency
            put("PKR", "Rs1,000,000.00");         // Pakistan Currency
            put("PEN", "S/.1,000,000.00");      // Peru Currency
            put("PHP", "₱1,000,000.00");      // Philippines Currency
            put("PLN", "zł1 000 000,00");      // Poland Currency
            put("GBP", "£1,000,000.00");      // Isle of Man
            put("RON", "lei1.000.000,00");      // Romania Currency
            put("RUB", "руб1 000 000,00");      // Russia Currency
            put("RSD", "Дин.1.000.000,00");         // Serbia Currency
            put("SGD", "S$1,000,000.00");      // Singapore Currency
            put("ZAR", "R1,000,000.00");      // South Africa Currency
            put("KRW", "₩1,000,000");         // South Korea Currency
            put("LKR", "රු1,000,000.00");      // Sri Lanka Currency
            put("SEK", "kr1,000,000.00");      // Sweden Currency
            put("CHF", "1,000,000.00");      // Switzerland Currency
            put("THB", "฿1,000,000.00");      // Thailand Currency
            put("TND", "د.ت1,000,000.000");     // Tunisia Currency
            put("AED", "د.إ1,000,000.00");      // United Arab Emirates Currency
            put("UGX", "USh1,000,000");         // Uganda Currency
            put("USD", "$1,000,000.00");      // United States Currency
            put("VND", "₫1.000.000,00");         // Vietnam Currency
        }
    };

    @Test
    public void testAllCurrencyFormats() {
//        String amount = "1000000";
//
//        for (Map.Entry<String, String> currencyMap : currenciesMap.entrySet()) {
//            Context context = ApplicationProvider.getApplicationContext();
//            String currency = CurrencyParser.getInstance(context).formatCurrencyWithSymbol(currencyMap.getKey(), amount);
//            assertThat(currency.trim(), is(currencyMap.getValue()));
//
//        }
   }
}
