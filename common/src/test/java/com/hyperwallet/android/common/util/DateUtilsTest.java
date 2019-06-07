package com.hyperwallet.android.common.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.Locale;

public class DateUtilsTest {

    @Before
    public void configureLocale() {
        Locale.setDefault(Locale.US);
    }

    @Test
    public void testToDateFormat_returnsExpectedStringFormat() {
        String dateString = "2019-05-27";
        Date dateTarget = DateUtils.fromDateTimeString("2019-05-27T15:57:49");

        // test
        String targetDate = DateUtils.toDateFormat(dateTarget);
        assertThat(targetDate, is(notNullValue()));
        assertThat(targetDate, is(dateString));
    }

    @Test
    public void testToDateFormat_returnsExpectedStringFormatFromParameter() {
        String dateString = "November 2019";
        Date dateTarget = DateUtils.fromDateTimeString("2019-11-27T15:57:49");

        // test
        String targetDate = DateUtils.toDateFormat(dateTarget, "MMMM yyyy");
        assertThat(targetDate, is(notNullValue()));
        assertThat(targetDate, is(dateString));
    }

    @Test
    public void testToDateTimeFormat_returnsExpectedStringFormat() {
        String dateString = "2019-11-27T15:57:49";
        Date dateTarget = DateUtils.fromDateTimeString("2019-11-27T15:57:49");

        // test
        String targetDate = DateUtils.toDateTimeFormat(dateTarget);
        assertThat(targetDate, is(notNullValue()));
        assertThat(targetDate, is(dateString));
    }

    @Test
    public void testToDateTimeMillisFormat_returnsExpectedStringFormat() {
        String dateString = "2019-11-27T15:57:09.450";
        Date dateTarget = DateUtils.fromDateTimeString("2019-11-27T15:57:09.450", "yyyy-MM-dd'T'HH:mm:ss.SSS");

        // test
        String targetDate = DateUtils.toDateTimeMillisFormat(dateTarget);
        assertThat(targetDate, is(notNullValue()));
        assertThat(targetDate, is(dateString));
    }
}
