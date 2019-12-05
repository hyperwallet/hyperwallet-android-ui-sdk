package com.hyperwallet.android.ui.transfermethod.view.widget;

import static org.hamcrest.MatcherAssert.assertThat;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hyperwallet.android.model.graphql.field.HyperwalletField;

import org.hamcrest.Matchers;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;

import java.util.Arrays;
import java.util.Collection;

import junitparams.Parameters;

@RunWith(RobolectricTestRunner.class)
public class AbstractMaskedInputWidgetRobolectricTest {

    @Mock
    private TestInputWidget mTestInputWidget;

    @Mock
    private AbstractMaskedInputWidget mAbstractMaskedInputWidget;

    public AbstractMaskedInputWidgetRobolectricTest() {
        mTestInputWidget = new TestInputWidget(null, null, null, null);
    }

    @Test
    @Parameters(method = "formatToApi")
    public void testFormatToApi() throws JSONException {
        Collection<Object[]> params = formatToApiParams();

        for (Object[] param : params) {
            String scrubRegex = (String) param[0];
            String input = (String) param[1];
            String output = (String) param[2];

            JSONObject jsonMask = new JSONObject();
            jsonMask.put("scrubRegex", scrubRegex);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("mask", jsonMask);

            HyperwalletField field = new HyperwalletField(jsonObject);

            mTestInputWidget = new TestInputWidget(field, null, null, null);

            String a1 = mTestInputWidget.formatToApi(input);
            assertThat(a1, Matchers.is(output));
        }
    }

    private Collection<Object[]> formatToApiParams() {
        return Arrays.asList(new Object[][]{
                {"[+()\\-\\s]", "+1 (604) 123-4567", "16041234567"},
                {"[\\+\\s\\@\\#]", "a@+#123", "a123"},
                {"[A\\s]", "B Apple A", "Bpple"},
        });
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