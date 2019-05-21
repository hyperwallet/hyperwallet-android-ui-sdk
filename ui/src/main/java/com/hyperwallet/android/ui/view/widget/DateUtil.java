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

import android.text.TextUtils;
import android.text.format.DateFormat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Class is used for manage and convert date {@link DateWidget}
 */
final class DateUtil {

    private static final String SERVER_DATE_PATTERN = "yyyy-MM-dd";
    private static final String WIDGET_DATE_PATTERN = "dd MMMM yyyy";

    private final SimpleDateFormat mServerDateFormat = new SimpleDateFormat(SERVER_DATE_PATTERN, Locale.getDefault());
    private final SimpleDateFormat mWidgetDateFormat;

    DateUtil() {
        mWidgetDateFormat = new SimpleDateFormat(
                DateFormat.getBestDateTimePattern(Locale.getDefault(), WIDGET_DATE_PATTERN), Locale.getDefault());
    }

    /**
     * Convert Date from server format (yyyy-MM-dd) to DateWidget format build by BestDateTimePattern @see {@link
     * DateFormat}  or (dd MMMM yyyy)
     *
     * @param serverDate Date from server
     * @return String date in the format by BestDateTimePattern or dd MMMM yyyy otherwise
     */
    @NonNull
    String convertDateFromServerToWidgetFormat(@Nullable final String serverDate) {
        if (isServerDateNotValid(serverDate)) {
            return "";
        }
        try {
            final Calendar calendar = Calendar.getInstance();
            calendar.setTime(mServerDateFormat.parse(serverDate));
            return mWidgetDateFormat.format(calendar.getTime());
        } catch (NumberFormatException | ParseException | IndexOutOfBoundsException e) {
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
        if (isServerDateNotValid(serverDate)) {
            return calendar;
        }

        try {
            calendar.setTime(mServerDateFormat.parse(serverDate));
            return calendar;
        } catch (ParseException e) {
            return calendar;
        }
    }

    @NonNull
    private Calendar getCalendar(final int year, final int month, final int dayOfMonth) {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth, 0, 0, 0);
        return calendar;
    }

    private boolean isServerDateNotValid(@Nullable final String serverDate) {
        Date date = null;
        if (!TextUtils.isEmpty(serverDate)) {
            SimpleDateFormat formatter = new SimpleDateFormat(SERVER_DATE_PATTERN, Locale.getDefault());
            formatter.setLenient(Boolean.FALSE); // make it strict
            try {
                date = formatter.parse(serverDate);
            } catch (ParseException e) {
                date = null;
            }
        }

        return date == null;
    }
}
