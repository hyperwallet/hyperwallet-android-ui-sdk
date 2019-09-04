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

package com.hyperwallet.android.ui.transfermethod.view.widget;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Size;

import java.util.Calendar;

/**
 * Class is used for manage and convert card expire date {@link ExpiryDateWidget}
 */
class ExpireDateUtils {

    static final char ONE_CHAR = '1';
    static final int MAX_INPUT_LENGTH = 5;
    static final String SEPARATOR = "/";
    static final char ZERO_CHAR = '0';
    private static final int VALID_PERIOD_IN_YEARS = 10;
    private static final String SERVER_DATE_FORMAT = "20%s-%s";
    private static final String SERVER_SEPARATOR = "-";
    private static final String VIEW_DATE_FORMAT = "%s/%s";
    private static final String ZERO = "0";

    private final Calendar mUpperValidDate = Calendar.getInstance();

    ExpireDateUtils() {
        mUpperValidDate.add(Calendar.YEAR, VALID_PERIOD_IN_YEARS);
    }

    /**
     * Check if input is invalid date
     *
     * @param inputDate date
     * @return <code>true<code/>if input data is invalid
     */
    boolean isInvalidDate(@NonNull final String inputDate) {
        try {
            Calendar inputCalendar = getInputDate(inputDate);
            return !mUpperValidDate.after(inputCalendar) || !Calendar.getInstance().before(inputCalendar);
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    /**
     * Convert Card Expire Date (MM/YY) from ExpireDateWidget to server format (yyyy-MM)
     *
     * @param inputDate Card Expire Date from view
     * @return date in the server format (yyyy-MM)
     */
    String convertDateToServerFormat(@NonNull final String inputDate) {
        if (inputDate == null || inputDate.isEmpty()) {
            return "";
        }
        String[] splitDate = inputDate.split(SEPARATOR);
        String years = splitDate.length == 2 ? splitDate[1] : "";
        return String.format(SERVER_DATE_FORMAT, years, splitDate[0]);
    }

    /**
     * Convert Card Expire Date with server format (yyyy-MM)  to the view format (MM/YY)
     *
     * @param inputDate Card Expire Date from server
     * @return date in the MM/YY format
     */
    String convertDateFromServerFormat(@NonNull final String inputDate) {
        if (inputDate == null || inputDate.isEmpty()) {
            return "";
        }
        String[] splitDate = inputDate.split(SERVER_SEPARATOR);
        String month = getMonthFromServer(splitDate);
        String year = splitDate[0].length() > 2 ? splitDate[0].substring(2) : "";
        return month.length() == 1 && year.isEmpty() ? month :
                String.format(VIEW_DATE_FORMAT, month, year);
    }

    int getCursorPosition(int newLength, int editStart, int editAdd) {
        int newPosition, gapsJumped = 0;
        boolean skipBack = false;

        if (editAdd == 0 && editStart == 3) {
            // editAdd can only be 0 if we are deleting,
            // so we need to check whether or not to skip backwards one space
            skipBack = true;
        }

        if (editStart <= 2 && editStart + editAdd >= 2) {
            gapsJumped = 1;
        }

        newPosition = editStart + editAdd + gapsJumped;
        if (skipBack && newPosition > 0) {
            newPosition--;
        }

        int normalPosition = newPosition <= newLength ? newPosition : newLength;
        return Math.min(MAX_INPUT_LENGTH, normalPosition);
    }

    @NonNull
    String[] getDateParts(@NonNull @Size(max = 4) final String input) {
        String[] parts = new String[2];
        if (input.length() >= 2) {
            parts[0] = input.substring(0, 2);
            parts[1] = input.substring(2);
        } else {
            parts[0] = input;
            parts[1] = "";
        }
        return parts;
    }

    boolean isValidMonth(@Nullable final String monthString) {
        if (monthString == null) {
            return false;
        }

        try {
            int monthInt = Integer.parseInt(monthString);
            return monthInt > Calendar.JANUARY && monthInt <= Calendar.UNDECIMBER;
        } catch (NumberFormatException numEx) {
            return false;
        }
    }

    // create Date from input to compare with current Date
    private Calendar getInputDate(@NonNull final String input) throws IllegalArgumentException {
        Calendar inputCalendar = Calendar.getInstance();
        inputCalendar.set(Calendar.MONTH, getInputMonth(input));
        inputCalendar.set(Calendar.YEAR, getInputYear(input));
        return inputCalendar;
    }

    // get month from input Date
    private int getInputMonth(@NonNull final String input) throws IllegalArgumentException {
        String monthString = input.substring(5);
        if (isValidMonth(monthString)) {
            return Integer.valueOf(monthString) - 1;
        }
        throw new IllegalArgumentException("Invalid month input:"+input);
    }

    // get year from input Date
    private int getInputYear(String input) {
        return Integer.valueOf(input.substring(0, 4));
    }

    //get month from server month part
    private String getMonthFromServer(String[] splitDate) {
        return splitDate.length != 2 ? "" :
                splitDate[1].length() == 1 && Integer.parseInt(splitDate[1]) > 1 ?
                        ZERO.concat(splitDate[1]) : splitDate[1];
    }
}