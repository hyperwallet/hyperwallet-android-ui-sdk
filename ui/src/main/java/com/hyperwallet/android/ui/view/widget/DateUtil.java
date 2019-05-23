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
public final class DateUtil {

    private static final String SERVER_DATE_PATTERN = "yyyy-MM-dd";
    private static final String WIDGET_DATE_PATTERN = "dd MMMM yyyy";

    private final SimpleDateFormat mServerDateFormat = new SimpleDateFormat(SERVER_DATE_PATTERN, Locale.getDefault());
    private final SimpleDateFormat mWidgetDateFormat;

    public DateUtil() {
        mWidgetDateFormat = new SimpleDateFormat(
                DateFormat.getBestDateTimePattern(Locale.getDefault(), WIDGET_DATE_PATTERN), Locale.getDefault());
    }

    /**
     * Convert Date from server format (yyyy-MM-dd) to DateWidget format build by BestDateTimePattern @see {@link
     * DateFormat}  or (dd MMMM yyyy)
     *
     * @param serverDate Date from server
     * @return String date in the format by BestDateTimePattern or dd MMMM yyyy otherwise
     * @throws DateParseException unable to convert server date to widget format
     */
    @NonNull
    String convertDateFromServerToWidgetFormat(@Nullable final String serverDate) throws DateParseException {
        try {
            if (isServerDateValid(serverDate)) {
                final Calendar calendar = Calendar.getInstance();
                calendar.setTime(mServerDateFormat.parse(serverDate));
                return mWidgetDateFormat.format(calendar.getTime());
            }
        } catch (NumberFormatException | ParseException | IndexOutOfBoundsException e) {
            throw new DateParseException("Unable to convert date from server to widget format", e.getCause());
        }
        return "";
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
    public String buildDateFromDateDialogToServerFormat(final int year, final int month, final int dayOfMonth) {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth);
        return mServerDateFormat.format(calendar.getTime());
    }

    /**
     * Convert Date from server format (yyyy-MM-dd) to Calendar
     *
     * @param serverDate Date from server
     * @return Calendar with date from server
     * @throws DateParseException unable to convert server date to calendar format
     */
    @NonNull
    Calendar convertDateFromServerFormatToCalendar(@Nullable final String serverDate) throws DateParseException {
        final Calendar calendar = Calendar.getInstance();
        try {
            if (isServerDateValid(serverDate)) {
                calendar.setTime(mServerDateFormat.parse(serverDate));
            }
        } catch (ParseException e) {
            throw new DateParseException("Unable to convert server date to calendar format", e.getCause());
        }
        return calendar;
    }

    private boolean isServerDateValid(@Nullable final String serverDate) throws DateParseException {
        Date date = null;
        if (!TextUtils.isEmpty(serverDate)) {
            SimpleDateFormat formatter = new SimpleDateFormat(SERVER_DATE_PATTERN, Locale.getDefault());
            formatter.setLenient(Boolean.FALSE); // make it strict
            try {
                date = formatter.parse(serverDate);
            } catch (ParseException e) {
                throw new DateParseException("Unable to parse server date to date format", e.getCause());
            }
        }
        return date != null;
    }
}