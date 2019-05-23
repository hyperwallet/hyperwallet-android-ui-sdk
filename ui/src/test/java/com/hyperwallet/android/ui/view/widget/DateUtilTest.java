package com.hyperwallet.android.ui.view.widget;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
public class DateUtilTest {
    private final DateUtil mDateUtil = new DateUtil();


    @Test
    public void testParseIncorrectDateFromServerToWidget() throws Exception {
        final List<String> inputParamList = buildParamsDateFromServerToWidget();
        for (String serverDate : inputParamList) {
            boolean isIncorrect = false;
            try {
                mDateUtil.convertDateFromServerToWidgetFormat(serverDate);
            } catch (DateParseException e) {
                isIncorrect = true;
            }
            assertThat(isIncorrect, is(true));
        }
    }

    @Test
    public void testParseCorrectDateFromServerToWidget() throws Exception {
        String serverDate = "2005-05-23";
        String widgetDate = "23 May 2005";
        assertThat(mDateUtil.convertDateFromServerToWidgetFormat(serverDate), is(widgetDate));
        assertThat(mDateUtil.convertDateFromServerToWidgetFormat(""), is(""));
        assertThat(mDateUtil.convertDateFromServerToWidgetFormat(null), is(""));
    }

    @Test
    public void testParseIncorrectDateFromServerToCalendar() throws Exception {
        List<String> inputParamList = buildParamsDateFromServerToCalendar();
        for (String serverDate : inputParamList) {
            boolean isIncorrect = false;
            try {
                mDateUtil.convertDateFromServerFormatToCalendar(serverDate).getTime();
            } catch (DateParseException e) {
                isIncorrect = true;
            }
            assertThat(isIncorrect, is(true));
        }
    }

    @Test
    public void testParseCorrectDateFromServerToCalendar() throws DateParseException {
        assertThat(mDateUtil.convertDateFromServerFormatToCalendar(null).getTime().toString(),
                is(Calendar.getInstance().getTime().toString()));
        assertThat(mDateUtil.convertDateFromServerFormatToCalendar("").getTime().toString(),
                is(Calendar.getInstance().getTime().toString()));
        String serverDate = "2005-05-23";
        final Calendar mayCalendar = Calendar.getInstance();
        mayCalendar.set(2005, 4, 23, 0, 0, 0);
        assertThat(mDateUtil.convertDateFromServerFormatToCalendar(serverDate).getTime().toString(),
                is(mayCalendar.getTime().toString()));
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

    private List<String> buildParamsDateFromServerToWidget() {
        return Arrays.asList(
                "0",
                "1990-01",
                "1990-03-111",
                "10-20-1",
                "2190-13-1",
                "2190-00-1",
                "2190-01-00",
                "2190-01-0",
                "2190-01-32"
        );
    }

    private List<String> buildParamsDateFromServerToCalendar() {
        return Arrays.asList(
                "0",
                "1990-01",
                "1990-03-111",
                "10-20-1",
                "19-1102-1",
                "2190-13-1",
                "2190-00-1",
                "2190-01-00",
                "2190-01-0"
        );
    }

    private Collection<Object[]> buildParamsFromDialogToServer() {
        return Arrays.asList(new Object[][]{
                {1900, 0, 1, "1900-01-01"},
                {2000, 11, 31, "2000-12-31"},
                {2001, 10, 13, "2001-11-13"}
        });
    }
}