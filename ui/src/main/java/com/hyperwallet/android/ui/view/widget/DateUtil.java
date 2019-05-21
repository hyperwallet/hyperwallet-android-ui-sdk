/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Hyperwallet Systems Inc.
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

package com.hyperwallet.android.ui.view.widget;

import android.text.format.DateFormat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class is used for manage and convert date {@link DateWidget}
 */
class DateUtil {

    private static final String TAG = DateUtil.class.getName();
    private static final String SERVER_DATE_PATTERN = "yyyy-MM-dd";
    private static final String WIDGET_DATE_PATTERN = "dd MMMM yyyy";
    private static final String DATE_PATTERN =
            "(\\d{4})-(0?[1-9]|1[012])-(0?[1-9]|[12][0-9]|3[01])";

    private final SimpleDateFormat mServerDateFormat = new SimpleDateFormat(SERVER_DATE_PATTERN, Locale.getDefault());
    private final SimpleDateFormat mWidgetDateFormat;

    DateUtil() {
        mWidgetDateFormat = new SimpleDateFormat(
                DateFormat.getBestDateTimePattern(Locale.getDefault(), WIDGET_DATE_PATTERN), Locale.getDefault());
    }

    /**
     * Convert Date from server format (yyyy-MM-dd) to DateWidget format build by BestDateTimePattern @see {@link
     * DateFormat}
     * or
     * (dd MMM yyyy)
     *
     * @param serverDate Date from server
     * @return String date in the format by BestDateTimePattern or dd MMM yyyy otherwise
     */
    @NonNull
    String convertDateFromServerToWidgetFormat(@Nullable final String serverDate) {
        if (isServerDateNotMatched(serverDate)) {
            return "";
        }
        try {
            final Calendar calendar = Calendar.getInstance();
            calendar.setTime(mServerDateFormat.parse(serverDate));
            return mWidgetDateFormat.format(calendar.getTime());
        } catch (NumberFormatException | ParseException | IndexOutOfBoundsException e) {
            Log.e(TAG, e.getMessage());
            return "";
        }
    }

    /**
     * Build Date in the server format (yyyy-MM-dd)
     *
     * @param year       the selected year
     * @param month      the selected month (0-11 for compatibility with {@link Calendar#MONTH})
     * @param dayOfMonth th selected day of the month (1-31, depending on month)
     * @return String date in the format (yyyy-MM-dd)
     */
    @NonNull
    String buildDateFromDateDialogToServerFormat(final int year, final int month, final int dayOfMonth) {
        final Calendar calendar = getCalendar(year, month, dayOfMonth);
        return mServerDateFormat.format(calendar.getTime());
    }

    /**
     * Build Date in the DateWidget format build by BestDateTimePattern @see{@link DateFormat} or (dd MMMM yyyy)
     *
     * @param year       the selected year
     * @param month      the selected month (0-11 for compatibility with {@link Calendar#MONTH})
     * @param dayOfMonth th selected day of the month (1-31, depending on month)
     * @return String date in the format @see{@link DateFormat} or (dd MMMM yyyy)
     */
    @NonNull
    String buildDateFromDateDialogToWidgetFormat(final int year, final int month, final int dayOfMonth) {
        final Calendar calendar = getCalendar(year, month, dayOfMonth);
        return mWidgetDateFormat.format(calendar.getTime());
    }

    /**
     * Convert Date from server format (yyyy-MM-dd) to Calendar
     *
     * @param serverDate Date from server
     * @return Calendar with date from server
     */
    @NonNull
    Calendar convertDateFromServerFormatToCalendar(@Nullable final String serverDate) {
        final Calendar calendar = Calendar.getInstance();
        if (isServerDateNotMatched(serverDate)) {
            return calendar;
        }

        try {
            calendar.setTime(mServerDateFormat.parse(serverDate));
        } catch (ParseException e) {
            Log.e(TAG, e.getMessage());
        }
        return calendar;
    }

    @NonNull
    private Calendar getCalendar(final int year, final int month, final int dayOfMonth) {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth, 0, 0, 0);
        return calendar;
    }

    private boolean isServerDateNotMatched(@Nullable final String serverDate) {
        if (serverDate == null) {
            return true;
        }
        final Matcher matcher = Pattern.compile(DATE_PATTERN).matcher(serverDate);
        return !matcher.matches();
    }
}
