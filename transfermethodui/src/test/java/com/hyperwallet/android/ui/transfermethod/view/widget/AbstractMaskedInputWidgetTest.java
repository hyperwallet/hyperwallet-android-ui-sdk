package com.hyperwallet.android.ui.transfermethod.view.widget;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hyperwallet.android.model.graphql.field.HyperwalletField;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class AbstractMaskedInputWidgetTest {

    private TestInputWidget mTestInputWidget;

    @Test
    public void testFormatToDisplay() {
        // json object
        // json.setMask(new Pattern("#"))
//        HyperwalletField field = new HyperwalletField( json );
//        mTestInputWidget = new TestInputWidget(json, null, null, null);

        mTestInputWidget = new TestInputWidget(null, null, null, null);

        String forDisplay = mTestInputWidget.formatToDisplay("12");
        assertThat(forDisplay, is("1"));
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