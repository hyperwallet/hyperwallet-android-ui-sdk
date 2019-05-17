package com.hyperwallet.android.ui.view.widget;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

@RunWith(JUnitParamsRunner.class)
public class DateUtilTest {
    private final DateUtil helper = new DateUtil();

    @Test
    @Parameters(method = "parametersToTestConvertDateFromServerFormatToWidgetFormat")
    public void testParseDateFromServerToWidget(String serverDate, String widgetDate) {
        assertThat(helper.convertDateFromServerToWidgetFormat(serverDate), is(widgetDate));
    }

    @Test
    @Parameters(method = "parametersToTestConvertDateFromServerToCalendar")
    public void testParseDateFromServerToCalendar(String serverDate, Calendar widgetDate) {
        assertThat(helper.convertDateFromServerFormatToCalendar(serverDate).getTime().toString(),
                is(widgetDate.getTime().toString()));
    }

    @Test
    @Parameters(method = "parametersToTestConvertDateFromDialogToWidgetFormat")
    public void testParseDateFromDialogToWidgetFormat(final int year, final int month, final int dayOfMonth,
            String widgetDate) {
        assertThat(helper.buildDateFromDateDialogToWidgetFormat(year, month, dayOfMonth), is(widgetDate));
    }


    @Test
    @Parameters(method = "parametersToTestConvertDateFromDialogToServerFormat")
    public void testParseDateFromDialogToServerFormat(final int year, final int month, final int dayOfMonth,
            String widgetDate) {
        assertThat(helper.buildDateFromDateDialogToServerFormat(year, month, dayOfMonth), is(widgetDate));
    }

    private Collection<Object[]> parametersToTestConvertDateFromServerFormatToWidgetFormat() {
        return Arrays.asList(new Object[][]{
                {null, ""},
                {"", ""},
                {"0", ""},
                {"1990-01", ""},
                {"1990-03-111", ""},
                {"10-20-1", ""},
                {"190-02-1", ""},
                {"2190-13-1", ""},
                {"2190-00-1", ""},
                {"2190-01-00", ""},
                {"2190-01-0", ""},
                {"2190-01-32", ""},
                {"2005-05-23", "23-May-2005"},
                {"1999-10-01", "01-Oct-1999"}
        });
    }

    private Collection<Object[]> parametersToTestConvertDateFromServerToCalendar() {
        final Calendar mayCalendar = Calendar.getInstance();
        mayCalendar.set(2005, 4, 23, 0, 0, 0);
        return Arrays.asList(new Object[][]{
                {null, Calendar.getInstance()},
                {"", Calendar.getInstance()},
                {"0", Calendar.getInstance()},
                {"1990-01", Calendar.getInstance()},
                {"1990-03-111", Calendar.getInstance()},
                {"10-20-1", Calendar.getInstance()},
                {"190-02-1", Calendar.getInstance()},
                {"2190-13-1", Calendar.getInstance()},
                {"2190-00-1", Calendar.getInstance()},
                {"2190-01-00", Calendar.getInstance()},
                {"2190-01-0", Calendar.getInstance()},
                {"2190-01-32", Calendar.getInstance()},
                {"2005-05-23", mayCalendar}
        });
    }

    private Collection<Object[]> parametersToTestConvertDateFromDialogToWidgetFormat() {
        return Arrays.asList(new Object[][]{
                {1900, 0, 1, "01-Jan-1900"},
                {2001, 10, 13, "13-Nov-2001"}
        });
    }

    private Collection<Object[]> parametersToTestConvertDateFromDialogToServerFormat() {
        return Arrays.asList(new Object[][]{
                {1900, 0, 1, "1900-01-01"},
                {2000, 11, 31, "2000-12-31"},
                {2001, 10, 13, "2001-11-13"}
        });
    }
}