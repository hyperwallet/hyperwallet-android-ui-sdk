package com.hyperwallet.android.common.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Date;

@RunWith(JUnit4.class)
public class DateUtilityTest {

    @Test
    public void testToDateFormat_returnExpectedStringFormat() {
        String dateString = "2019-05-27";
        Date dateTarget = DateUtility.fromDateTimeString("2019-05-27T15:57:49");

        // test
        String targetDate = DateUtility.toDateFormat(dateTarget);
        assertThat(targetDate, is(notNullValue()));
        assertThat(targetDate, is(dateString));
    }

    @Test
    public void testToDateFormat_returnExpectedStringFormatFromParameter() {
        String dateString = "November 2019";
        Date dateTarget = DateUtility.fromDateTimeString("2019-11-27T15:57:49");

        // test
        String targetDate = DateUtility.toDateFormat(dateTarget, "MMMM yyyy");
        assertThat(targetDate, is(notNullValue()));
        assertThat(targetDate, is(dateString));
    }

    @Test
    public void testToDateTimeFormat_returnExpectedStringFormat() {
        String dateString = "2019-11-27T15:57:49";
        Date dateTarget = DateUtility.fromDateTimeString("2019-11-27T15:57:49");

        // test
        String targetDate = DateUtility.toDateTimeFormat(dateTarget);
        assertThat(targetDate, is(notNullValue()));
        assertThat(targetDate, is(dateString));
    }

    @Test
    public void testToDateTimeMillisFormat_returnExpectedStringFormat() {
        String dateString = "2019-11-27T15:57:09.450";
        Date dateTarget = DateUtility.fromDateTimeString("2019-11-27T15:57:09.450", "yyyy-MM-dd'T'HH:mm:ss.SSS");

        // test
        String targetDate = DateUtility.toDateTimeMillisFormat(dateTarget);
        assertThat(targetDate, is(notNullValue()));
        assertThat(targetDate, is(dateString));
    }
}
