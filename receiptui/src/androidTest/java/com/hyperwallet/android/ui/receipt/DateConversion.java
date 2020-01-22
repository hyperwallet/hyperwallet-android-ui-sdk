package com.hyperwallet.android.ui.receipt;

import static android.text.format.DateUtils.FORMAT_ABBREV_WEEKDAY;
import static android.text.format.DateUtils.FORMAT_SHOW_DATE;
import static android.text.format.DateUtils.FORMAT_SHOW_TIME;
import static android.text.format.DateUtils.FORMAT_SHOW_WEEKDAY;
import static android.text.format.DateUtils.FORMAT_SHOW_YEAR;
import static android.text.format.DateUtils.formatDateTime;

import androidx.test.core.app.ApplicationProvider;

import com.hyperwallet.android.ui.common.util.DateUtils;

import java.util.Date;

public class DateConversion {

    public static String dateConvertReceiptList(String dateString) {
        Date date = DateUtils.fromDateTimeString(dateString);
        return formatDateTime(ApplicationProvider.getApplicationContext(), date.getTime(),
                FORMAT_SHOW_DATE | FORMAT_SHOW_YEAR);
    }

    public static String dateConvertReceiptDetails(Date date) {
        return formatDateTime(ApplicationProvider.getApplicationContext(), date.getTime(),
                FORMAT_SHOW_DATE | FORMAT_SHOW_TIME | FORMAT_SHOW_YEAR
                        | FORMAT_SHOW_WEEKDAY | FORMAT_ABBREV_WEEKDAY);
    }
}