package com.hyperwallet.android.ui.transfermethod.view.widget;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

@RunWith(JUnitParamsRunner.class)
public class ExpireDateUtilsTest {
    private final ExpireDateUtils helper = new ExpireDateUtils();

    @Test
    @Parameters(method = "parametersToTestValidDate")
    public void testIsInvalidDate(String inputDate, Boolean isValid) {
        assertThat(helper.isInvalidDate(inputDate), is(isValid));
    }

    @Test
    @Parameters(method = "parametersToTestConvertDateToServerFormat")
    public void testConvertDateToServerFormat(String inputData, String formattedDate) {
        assertThat(helper.convertDateToServerFormat(inputData), is(formattedDate));
    }

    @Test
    @Parameters(method = "parametersToTestConvertDateFromServerFormat")
    public void testConvertDateFromServerFormat(String inputData, String formattedDate) {
        assertThat(helper.convertDateFromServerFormat(inputData), is(formattedDate));
    }

    @Test
    @Parameters(method = "parametersMonth")
    public void testInputValidMonths(String inputData, boolean expected) {
        assertThat(helper.isValidMonth(inputData), is(expected));
    }

    private Collection<Object[]> parametersToTestValidDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM");
        String currentDate = simpleDateFormat.format(calendar.getTime());
        calendar.add(Calendar.MONTH, 1);
        String currentDateNextMonth = simpleDateFormat.format(calendar.getTime());
        calendar.add(Calendar.YEAR, 1);
        String futureDate = simpleDateFormat.format(calendar.getTime());
        calendar.add(Calendar.YEAR, 10);
        String invalidFutureDate = simpleDateFormat.format(calendar.getTime());
        calendar.add(Calendar.YEAR, -20);
        String invalidPastDate = simpleDateFormat.format(calendar.getTime());
        return Arrays.asList(new Object[][]{
                {invalidPastDate, true},
                {currentDate, true},
                {invalidFutureDate, true},
                {futureDate, false},
                {currentDateNextMonth, false}
        });
    }

    private Collection<Object[]> parametersToTestConvertDateFromServerFormat() {
        return Arrays.asList(new Object[][]{
                {"", ""},
                {"20-0", "0"},
                {"20-01", "01/"},
                {"20-1", "1"},
                {"20-5", "05/"},
                {"203-12", "12/3"},
                {"2034-5", "05/34"},
                {"2034-12", "12/34"}
        });
    }

    private Collection<Object[]> parametersToTestConvertDateToServerFormat() {
        return Arrays.asList(new Object[][]{
                {"", ""},
                {"0", "20-0"},
                {"01/", "20-01"},
                {"12/3", "203-12"},
                {"12/34", "2034-12"}
        });
    }

    private Collection<Object[]> parametersMonth() {
        return Arrays.asList(new Object[][]{
                {"", false},
                {"0", false},
                {"01", true},
                {"06", true},
                {"12", true},
                {"13", false},
                {"55", false},
                {"99", false}
        });
    }
}
