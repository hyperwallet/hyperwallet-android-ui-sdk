package com.hyperwallet.android.ui.testutils.util;


import static android.text.format.DateUtils.formatDateTime;

import android.content.Context;

import com.hyperwallet.android.ui.common.util.DateUtils;

import java.util.Date;

public class DateConversion {

    public static String convertDate(Context context, String dateString, int flag) {
        Date date = DateUtils.fromDateTimeString(dateString);
        return convertDate(context, date, flag);
    }

    public static String convertDate(Context context, Date date, int flag) {
        return formatDateTime(context, date.getTime(), flag);
    }
}
