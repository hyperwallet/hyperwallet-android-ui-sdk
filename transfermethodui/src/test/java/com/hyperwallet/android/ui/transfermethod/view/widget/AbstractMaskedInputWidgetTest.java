package com.hyperwallet.android.ui.transfermethod.view.widget;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hyperwallet.android.model.graphql.field.HyperwalletField;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collection;

import junitparams.FileParameters;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

@RunWith(JUnitParamsRunner.class)
public class AbstractMaskedInputWidgetTest {

    private TestInputWidget mTestInputWidget;

    public AbstractMaskedInputWidgetTest() throws JSONException {
        mTestInputWidget = new TestInputWidget(null, null, null, null);
    }

    @Test
    @FileParameters("src/test/resources/formatter_mask_test_scenarios.csv")
    public void testFormatToDisplay_usingExcelData(String description, String pattern, String scenario,
            String inputValue, String formattedValue) {
        pattern = pattern.replace("\\\\", "\\");
        assertThat("Description: " + description + "; Scenario: " + scenario + "; Pattern:" + pattern + ";InputValue:"
                        + inputValue,
                mTestInputWidget.format(inputValue, pattern), is(formattedValue));
    }

    @Test
    @Parameters(method = "maskTestCaseScenarios")
    public void testFormatToDisplay_fromExcelData(String description, String pattern, String scenario,
            String inputValue, String formattedValue) {
        assertThat("Description: " + description + "; Scenario: " + scenario + "; Pattern:" + pattern + ";InputValue:"
                        + inputValue,
                mTestInputWidget.format(inputValue, pattern), is(formattedValue));
    }

    private Collection<Object[]> maskTestCaseScenarios() {
        return Arrays.asList(new Object[][]{
                {"Single number", "#", "1", "1", "1"},
                {"Single number", "#", "2", "11", "1"},
                {"Single number", "#", "3", "a", ""},
                {"Single number", "#", "4", "a1", "1"},
                {"Single number", "#", "5", "", ""},

                {"Single number", "##", "1", "11", "11"},
                {"Single number", "##", "2", "1111", "11"},
                {"Single number", "##", "3", "aa11", "11"},
                {"Single number", "##", "4", "1a1a", "11"},
                {"Single number", "##", "5", "", ""},

                {"Numbers with delimiter in between", "##-##", "1", "11", "11"},
                {"Numbers with delimiter in between", "##-##", "2", "1111", "11-11"},
                {"Numbers with delimiter in between", "##-##", "3", "aa11aa11", "11-11"},
                {"Numbers with delimiter in between", "##-##", "4", "11-11", "11-11"},
                {"Numbers with delimiter in between", "##-##", "5", "aa-aa-1111", "11-11"},

                {"Numbers with prefix and delimiter in between", "A##B##", "1", "11", "A11"},
                {"Numbers with prefix and delimiter in between", "A##B##", "2", "111", "A11B1"},
                {"Numbers with prefix and delimiter in between", "A##B##", "3", "A11B11", "A11B11"},
                {"Numbers with prefix and delimiter in between", "A##B##", "4", "AAA11BBB11", "A11B11"},
                {"Numbers with prefix and delimiter in between", "A##B##", "5", "", ""},

                {"Numbers with escape", "##\\###", "1", "11", "11#"},
                {"Numbers with escape", "##\\###", "2", "1111", "11#11"},
                {"Numbers with escape", "##\\###", "3", "aa11aa11", "11#11"},
                {"Numbers with escape", "##\\###", "4", "11-11", "11#11"},
                {"Numbers with escape", "##\\###", "5", "aa-aa-1111", "11#11"},

                {"Single letter", "@", "1", "a", "a"},
                {"Single letter", "@", "2", "aa", "a"},
                {"Single letter", "@", "3", "1", ""},
                {"Single letter", "@", "4", "1a", "a"},
                {"Single letter", "@", "5", "", ""},

                {"Double Letters", "@@", "1", "aa", "aa"},
                {"Double Letters", "@@", "2", "aaaa", "aa"},
                {"Double Letters", "@@", "3", "11aa", "aa"},
                {"Double Letters", "@@", "4", "a1a1", "aa"},
                {"Double Letters", "@@", "5", "", ""},

                {"Letters with delimiter in between", "@@-@@", "1", "aa", "aa"},
                {"Letters with delimiter in between", "@@-@@", "2", "aaaa", "aa-aa"},
                {"Letters with delimiter in between", "@@-@@", "3", "11aa11aa", "aa-aa"},
                {"Letters with delimiter in between", "@@-@@", "4", "aa-aa", "aa-aa"},
                {"Letters with delimiter in between", "@@-@@", "5", "11-11-aaaa", "aa-aa"},

                {"Lettetrs with prefix and delimiter in between", "1@@2@@", "1", "aa", "1aa"},
                {"Lettetrs with prefix and delimiter in between", "1@@2@@", "2", "aaa", "1aa2a"},
                {"Lettetrs with prefix and delimiter in between", "1@@2@@", "3", "1aa2aa", "1aa2aa"},
                {"Lettetrs with prefix and delimiter in between", "1@@2@@", "4", "111aa222bb", "1aa2bb"},
                {"Lettetrs with prefix and delimiter in between", "1@@2@@", "5", "", ""},

                {"Letters with escape", "@@\\@@@", "1", "aa", "aa@"},
                {"Letters with escape", "@@\\@@@", "2", "aaaa", "aa@aa"},
                {"Letters with escape", "@@\\@@@", "3", "11aa11aa", "aa@aa"},
                {"Letters with escape", "@@\\@@@", "4", "aa-aa", "aa@aa"},
                {"Letters with escape", "@@\\@@@", "5", "11-11-aaaa", "aa@aa"},

                {"Single character", "*", "1", "1", "1"},
                {"Single character", "*", "2", "11", "1"},
                {"Single character", "*", "3", "a", "a"},
                {"Single character", "*", "4", "a1", "a"},
                {"Single character", "*", "5", "", ""},

                {"Double characters", "**", "1", "aa", "aa"},
                {"Double characters", "**", "2", "aaaa", "aa"},
                {"Double characters", "**", "3", "11aa", "11"},
                {"Double characters", "**", "4", "a1a1", "a1"},
                {"Double characters", "**", "5", "", ""},

                {"Characters with delimiter in between", "**-**", "1", "11", "11"},
                {"Characters with delimiter in between", "**-**", "2", "1111", "11-11"},
                {"Characters with delimiter in between", "**-**", "3", "aa11aa11", "aa-11"},
                {"Characters with delimiter in between", "**-**", "4", "11-11", "11-11"},
                {"Characters with delimiter in between", "**-**", "5", "aa-aa-1111", "aa-aa"},
                {"Characters with delimiter in between", "**-**", "6", "11-", "11"},

                {"Characters with prefix and delimiter in between", "1**A**", "1", "aa", "1aa"},
                {"Characters with prefix and delimiter in between", "1**A**", "2", "aaa", "1aaAa"},
                {"Characters with prefix and delimiter in between", "1**A**", "3", "1aa2aa", "1aaA2a"},
                {"Characters with prefix and delimiter in between", "1**A**", "4", "111aa222bb", "111Aaa"},
                {"Characters with prefix and delimiter in between", "1**A**", "5", "", ""},

                {"Characters with escape", "**\\***", "1", "11", "11*"},
                {"Characters with escape", "**\\***", "2", "1111", "11*11"},
                {"Characters with escape", "**\\***", "3", "aa11aa11", "aa*11"},
                {"Characters with escape", "**\\***", "4", "11-NOV", "11*-N"},
                {"Characters with escape", "**\\***", "5", "aa-aa-1111", "aa*-a"},

                {"Combined - Single", "#@*", "1", "aaa", ""},
                {"Combined - Single", "#@*", "2", "111", "1"},
                {"Combined - Single", "#@*", "3", "1ab", "1ab"},
                {"Combined - Single", "#@*", "4", "ba1", "1"},
                {"Combined - Single", "#@*", "5", "1ab1", "1ab"},

                {"Combined - Double", "#@*#@*", "1", "aaaaaa", ""},
                {"Combined - Double", "#@*#@*", "2", "111111", "1"},
                {"Combined - Double", "#@*#@*", "3", "1a11a1", "1a11a1"},
                {"Combined - Double", "#@*#@*", "4", "a1aa1a", "1aa1a"},
                {"Combined - Double", "#@*#@*", "5", "-1a-", "1a-"},

                {"Combined - With delimiter in between", "#@*-@#*", "1", "aaaaaa", ""},
                {"Combined - With delimiter in between", "#@*-@#*", "2", "111111", "1"},
                {"Combined - With delimiter in between", "#@*-@#*", "3", "1a11a1", "1a1-a1"},
                {"Combined - With delimiter in between", "#@*-@#*", "4", "-12ab-12ab", "1ab-a"},
                {"Combined - With delimiter in between", "#@*-@#*", "5", "", ""},

                {"Combined - With prefix and delimiter in between", "^#@*-@#*", "1", "aaaaaa", ""},
                {"Combined - With prefix and delimiter in between", "^#@*-@#*", "2", "111111", "^1"},
                {"Combined - With prefix and delimiter in between", "^#@*-@#*", "3", "1a11a1", "^1a1-a1"},
                {"Combined - With prefix and delimiter in between", "^#@*-@#*", "4", "-12ab-12ab", "^1ab-a"},
                {"Combined - With prefix and delimiter in between", "^#@*-@#*", "5", "", ""},

                {"Combined - With escape", "\\@@#*\\#@#*\\*@#*", "1", "aaaaaa", "@a"},
                {"Combined - With escape", "\\@@#*\\#@#*\\*@#*", "2", "111111", "@"},
                {"Combined - With escape", "\\@@#*\\#@#*\\*@#*", "3", "a1aa1a", "@a1a#a1a*"},
                {"Combined - With escape", "\\@@#*\\#@#*\\*@#*", "4", "@a1a#a1a*a1a", "@a1a#a1a*a1a"},

                {"formatted post code", "@#@ #@#", "1", "V1B2N3", "V1B 2N3"},

                {"formatted CVV", "###", "1", "A123", "123"},

                {"formatted visa debit card number", "#### #### #### ####", "1", "4123567891234567",
                        "4123 5678 9123 4567"},

                {"formatted amex debit card number", "#### ###### #####", "1", "347356789134567", "3473 567891 34567"},

                {"Prefix with characters", "Hello: @@@@@", "1", "Hello: abcde", "Hello: abcde"}
        });
    }

    @Test
    public void testFormatToDisplay() throws JSONException {
        JSONObject jsonMask = new JSONObject();
        jsonMask.put("scrubRegex", "\\s");
        jsonMask.put("defaultPattern", "#### #### #### ####");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("mask", jsonMask);

        HyperwalletField field = new HyperwalletField(jsonObject);
        mTestInputWidget = new TestInputWidget(field, null, null, null);

        String displayValue = mTestInputWidget.formatToDisplay("1234567887654321");
        assertThat(displayValue, is("1234 5678 8765 4321"));
    }

    @Test
    @Parameters(method = "formatToApi")
    public void testFormatToApi(String scrubRegex, String input, String output) throws JSONException {
        JSONObject jsonMask = new JSONObject();
        jsonMask.put("scrubRegex", scrubRegex);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("mask", jsonMask);

        HyperwalletField field = new HyperwalletField(jsonObject);
        mTestInputWidget = new TestInputWidget(field, null, null, null);

        String a1 = mTestInputWidget.formatToApi(input);
        assertThat(a1, is(output));
    }

    private Collection<Object[]> formatToApi() {
        return Arrays.asList(new Object[][]{
                {"\\s", "604 123 4567", "6041234567"},
                {"\\-", "604-123-4567", "6041234567"},
                {"[+()\\-\\s]", "+1 (604) 123-4567", "16041234567"},
                {"[A]", "AabcdeA", "abcde"},
        });
    }

    @Test
    @Parameters(method = "edgeCases")
    public void testFormatToDisplay_edgeCases(String description, String pattern, String scenario,
            String inputValue, String formattedValue) {
        assertThat("Description: " + description + "; Scenario: " + scenario,
                mTestInputWidget.format(inputValue, pattern), is(formattedValue));
    }

    private Collection<Object[]> edgeCases() {
        return Arrays.asList(new Object[][]{
                {"Ending suffix", "###-##-", "1", "123456", "123-45"},
                {"Ending suffix", "###-##-9", "2", "123456", "123-45"},
                {"Ending suffix", "###-##9", "3", "123456", "123-45"},
                {"Ending suffix", "###-##9", "4", "123456", "123-45"},

                {"Backslash", "##\\\\##9", "1", "12345", "12\\34"},
                {"Backslash", "##\\\\\\\\##9", "2", "12345", "12\\\\34"},
                {"Backslash", "\\\\##9##", "3", "12345", "\\12934"},
                {"Backslash", "\\\\\\\\##9##", "4", "12345", "\\\\12934"},

                // * should only accept [0-9A-Za-z]
                {"Character Set", "@@-##-**-**", "1", "12你好ééab你好éé12你好éé", "ab-12"},

                {"Phone", "+# (###) ###-####", "1", "16046332234", "+1 (604) 633-2234"},
                {"Phone", "+# (###) ###-####", "2", "1604", "+1 (604"},
                {"Phone", "+# (###) ###-####", "3", "16046", "+1 (604) 6"},
                {"Phone", "+# (###) ###-####", "3", "1abc", "+1"},
                {"Phone", "+# (###) ###-####", "4", "abc", ""},
        });
    }

    @Test
    public void testFormatToDisplay_edgeCases_TODO_delete_me() {
        // TODO temporary tests for most scenarios and edge cases, use values from excel sheet instead
        String test1 = mTestInputWidget.format("v23", "@#@-#@#");
        assertThat(test1, is("v2"));

        String test2 = mTestInputWidget.format("v5l", "@#@ #@#");
        assertThat(test2, is("v5l"));

        String test3 = mTestInputWidget.format("v5l3", "@#@-#@#");
        assertThat(test3, is("v5l-3"));

        String test4 = mTestInputWidget.format("v5l3c2", "@#@-#@#");
        assertThat(test4, is("v5l-3c2"));

        String num1 = mTestInputWidget.format("123456", "### ###");
        assertThat(num1, is("123 456"));

        String num2 = mTestInputWidget.format("123", "### ###");
        assertThat(num2, is("123"));

        String num3 = mTestInputWidget.format("123abc", "### ###");
        assertThat(num3, is("123"));

        String num4 = mTestInputWidget.format("123abc456", "### ###");
        assertThat(num4, is("123 456"));

        String word1 = mTestInputWidget.format("b2", "@@@");
        assertThat(word1, is("b"));

        String phone1 = mTestInputWidget.format("16046332234", "+# (###) ###-####");
        assertThat(phone1, is("+1 (604) 633-2234"));

        String star1 = mTestInputWidget.format("v5l", "***-***");
        assertThat(star1, is("v5l"));

        String star2 = mTestInputWidget.format("v5l3", "***-***");
        assertThat(star2, is("v5l-3"));

        String empty = mTestInputWidget.format("", "***-***");
        assertThat(empty, is(""));

        // below simulates if the user clicks keys one letter at a time
        String conflict1 = mTestInputWidget.format("9", "1**A**");
        assertThat(conflict1, is("19"));

        String conflict2 = mTestInputWidget.format("198", "1##A##");
        assertThat(conflict2, is("198"));

        String conflict3 = mTestInputWidget.format("1987", "1##A##");
        assertThat(conflict3, is("198A7"));

        String conflict4 = mTestInputWidget.format("198A76", "1##A##");
        assertThat(conflict4, is("198A76"));

        // simulates user pasting in values
        String paste1 = mTestInputWidget.format("9876", "1##A##");
        assertThat(paste1, is("198A76"));

        String paste2 = mTestInputWidget.format("198A76", "1##A##");
        assertThat(paste2, is("198A76"));

        String backslash1 = mTestInputWidget.format("1234", "##\\###");
        assertThat(backslash1, is("12#34"));

        String backslash2 = mTestInputWidget.format("aaaaaa", "\\@@#*\\#@#*\\*@#*");
        assertThat(backslash2, is("@a"));

//        String backslash3 = mTestInputWidget.format("111111", "\\@@#*\\#@#*\\*@#*");
//        assertThat(backslash3, is("@")); // Line 99 of excel, the excel is not correct. Correct answer should be:
//        (empty string)

//        String backslash4 = mTestInputWidget.format("a1aa1a", "\\@@#*\\#@#*\\*@#*");
//        assertThat(backslash4, is("@a1a#a1a*")); // Line 100 of excel, the excel is not correct. Correct answer
//        should be: @a1a#a1a

        String backslash5 = mTestInputWidget.format("@a1a#a1a*a1a", "\\@@#*\\#@#*\\*@#*");
        assertThat(backslash5, is("@a1a#a1a*a1a"));

        // we don't support ending suffix
        String backslash6 = mTestInputWidget.format("1234", "\\\\##\\\\##\\\\");
        assertThat(backslash6, is("\\12\\34"));

        String extra1 = mTestInputWidget.format("123_", "###_###");
        assertThat(extra1, is("123_"));

        String extra2 = mTestInputWidget.format("123-", "###-###");
        assertThat(extra2, is("123-"));
    }

    class TestInputWidget extends AbstractMaskedInputWidget {
        public TestInputWidget(@Nullable HyperwalletField field, @NonNull WidgetEventListener listener,
                @Nullable String defaultValue, @NonNull View defaultFocusView) {
            super(field, listener, defaultValue, defaultFocusView);
        }

        public View getView(@NonNull final ViewGroup viewGroup) {
            return null;
        }

        public String getValue() {
            return null;
        }

        public void showValidationError(String errorMessage) {
        }
    }
}