/*
 * The MIT License (MIT)
 * Copyright (c) 2019 Hyperwallet Systems Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.hyperwallet.android.common.util;

import androidx.annotation.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class DateUtility {

    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    private static final String DATE_TIME_FORMAT_MILLISECONDS = "yyyy-MM-dd'T'HH:mm:ss.SSS";

    /**
     * date format: yyyy-MM-dd
     */
    public static String toDateFormat(@NonNull Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        return dateFormat.format(date);
    }

    /**
     * @param date   Date object
     * @param format specify desired format of date
     * @return formatted date string
     */
    public static String toDateFormat(@NonNull final Date date, @NonNull final String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
        return dateFormat.format(date);
    }

    /**
     * date format: yyyy-MM-dd'T'HH:mm:ss
     */
    public static String toDateTimeFormat(@NonNull final Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault());
        return dateFormat.format(date);
    }

    /**
     * date format: yyyy-MM-dd'T'HH:mm:ss.SSS
     */
    public static String toDateTimeMillisFormat(@NonNull final Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_TIME_FORMAT_MILLISECONDS, Locale.getDefault());
        return dateFormat.format(date);
    }

    /**
     * date format: yyyy-MM-dd'T'HH:mm:ss
     */
    public static Date fromDateTimeString(@NonNull final String dateString) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault());
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            throw new IllegalArgumentException("An exception occurred when attempting to parse " +
                    "the date " + dateString, e);
        }
    }


    /**
     * date format: yyyy-MM-dd'T'HH:mm:ss
     */
    public static Date fromDateTimeString(@NonNull final String dateString, @NonNull final String format) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            throw new IllegalArgumentException("An exception occurred when attempting to parse " +
                    "the date " + dateString, e);
        }
    }
}
