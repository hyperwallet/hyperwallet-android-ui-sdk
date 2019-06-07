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
import androidx.annotation.VisibleForTesting;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Common HW-SDK UI Date Utility class, that will assist on safe presentation of date whatever the mobile device setting
 * is set Locale, Timezone and etc... that dictates how that dates are being presented
 */
public final class DateUtils {

    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    private static final String DATE_TIME_FORMAT_MILLISECONDS = "yyyy-MM-dd'T'HH:mm:ss.SSS";

    private DateUtils() {
    }

    /**
     * Creates a string date format: <code>yyyy-MM-dd</code>
     *
     * @param date Date object
     * @return string date in <code>yyyy-MM-dd</code> format
     */
    public static String toDateFormat(@NonNull final Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getDefault());
        return dateFormat.format(date);
    }

    /**
     * Creates a string date in specified format
     *
     * @param date   Date object
     * @param format specify desired format of date
     * @return formatted date string based on format specified
     */
    public static String toDateFormat(@NonNull final Date date, @NonNull final String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getDefault());
        return dateFormat.format(date);
    }

    /**
     * Creates a string date format
     *
     * @param date Date object
     * @return formatted string in <code>yyyy-MM-dd'T'HH:mm:ss</code> format
     */
    public static String toDateTimeFormat(@NonNull final Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getDefault());
        return dateFormat.format(date);
    }

    /**
     * Creates a string date format
     *
     * @param date Date object
     * @return formatted string in <code>yyyy-MM-dd'T'HH:mm:ss.SSS</code> format
     */
    public static String toDateTimeMillisFormat(@NonNull final Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_TIME_FORMAT_MILLISECONDS, Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getDefault());
        return dateFormat.format(date);
    }

    /**
     * Creates a Date object from string date
     *
     * @param dateString String date
     * @return date Date object
     * @throws IllegalArgumentException when string is un-parsable
     */
    public static Date fromDateTimeString(@NonNull final String dateString) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault());
            dateFormat.setTimeZone(TimeZone.getDefault());
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            throw new IllegalArgumentException("An exception occurred when attempting to parse " +
                    "the date " + dateString, e);
        }
    }

    @VisibleForTesting
    static Date fromDateTimeString(@NonNull final String dateString, @NonNull final String format) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
            dateFormat.setTimeZone(TimeZone.getDefault());
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            throw new IllegalArgumentException("An exception occurred when attempting to parse " +
                    "the date " + dateString, e);
        }
    }
}
