package com.hyperwallet.android.ui.transfermethod.view.widget;

import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.hyperwallet.android.model.graphql.field.HyperwalletMaskField;

public class WidgetInputFilter implements InputFilter {

    private final HyperwalletMaskField mHyperwalletMaskField;
    private boolean hasMasking;

    protected WidgetInputFilter(@Nullable final HyperwalletMaskField maskField) {
        mHyperwalletMaskField = maskField;
        hasMasking = mHyperwalletMaskField != null;
    }

    @Override
    public CharSequence filter(CharSequence input, int start, int end, Spanned dest, int dstart, int dend) {
        if (!TextUtils.isEmpty(input) && hasMasking) {
            String textToFormat = dest.toString().trim().isEmpty() ? input.toString() : dest.toString();
            String formatPattern = WidgetInputUtil.getFormatTemplate(textToFormat, mHyperwalletMaskField);

            if (dest.length() < formatPattern.length()) {
                return WidgetInputUtil.getInputFormat(input.toString(), formatPattern, dend);
            }
        }
        return "";
    }
}
