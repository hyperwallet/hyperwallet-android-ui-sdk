package com.hyperwallet.android.ui.view.widget;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;

@RunWith(RobolectricTestRunner.class)
public class DateUtilTest {
    private final DateUtil mDateUtil = new DateUtil();

    @Test
    public void testParseDateFromServerToWidget() {
        String serverDate;
        String widgetDate;
        Collection<Object[]> inputParamList = buildParamsDateFromServerToWidget();
        for (Object[] item : inputParamList) {
            serverDate = (String) item[0];
            widgetDate = (String) item[1];
            assertThat(mDateUtil.convertDateFromServerToWidgetFormat(serverDate), is(widgetDate));
        }
    }

    @Test
    public void testParseDateFromServerToCalendar() {
        String serverDate;
        Calendar widgetDate;
        Collection<Object[]> inputParamList = buildParamsDateFromServerToCalendar();
        for (Object[] item : inputParamList) {
            serverDate = (String) item[0];
            widgetDate = (Calendar) item[1];
            assertThat(mDateUtil.convertDateFromServerFormatToCalendar(serverDate).getTime().toString(),
                    is(widgetDate.getTime().toString()));
        }
    }


    @Test
    public void testParseDateFromDialogToWidgetFormat() {
        String widgetDate;
        int year;
        int month;
        int dayOfMonth;
        Collection<Object[]> inputParamList = buildParamsFromDialogToWidget();
        for (Object[] item : inputParamList) {
            year = (int) item[0];
            month = (int) item[1];
            dayOfMonth = (int) item[2];
            widgetDate = (String) item[3];
            assertThat(mDateUtil.buildDateFromDateDialogToWidgetFormat(year, month, dayOfMonth), is(widgetDate));
        }
    }

    @Test
    public void testParseDateFromDialogToServerFormat() {
        String widgetDate;
        int year;
        int month;
        int dayOfMonth;
        Collection<Object[]> inputParamList = buildParamsFromDialogToServer();
        for (Object[] item : inputParamList) {
            year = (int) item[0];
            month = (int) item[1];
            dayOfMonth = (int) item[2];
            widgetDate = (String) item[3];
            assertThat(mDateUtil.buildDateFromDateDialogToServerFormat(year, month, dayOfMonth), is(widgetDate));
        }
    }

    private Collection<Object[]> buildParamsDateFromServerToWidget() {
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
                {"2005-05-23", "23 May 2005"}
        });
    }

    private Collection<Object[]> buildParamsDateFromServerToCalendar() {
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


    private Collection<Object[]> buildParamsFromDialogToWidget() {
        return Arrays.asList(new Object[][]{
                {1900, 0, 1, "01 January 1900"},
                {2001, 10, 13, "13 November 2001"}
        });
    }

    private Collection<Object[]> buildParamsFromDialogToServer() {
        return Arrays.asList(new Object[][]{
                {1900, 0, 1, "1900-01-01"},
                {2000, 11, 31, "2000-12-31"},
                {2001, 10, 13, "2001-11-13"}
        });
    }
}