/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Hyperwallet Systems Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.hyperwallet.android.ui.transfermethod.view.widget;

import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hyperwallet.android.model.graphql.field.ConditionalPattern;
import com.hyperwallet.android.model.graphql.field.HyperwalletField;
import com.hyperwallet.android.model.graphql.field.Mask;

import java.util.List;

public abstract class AbstractMaskedInputWidget extends AbstractWidget {
    private static final char NUMBER_TOKEN = '#';
    private static final char TEXT_TOKEN = '@';

    private InputFilter[] mInputFilters;
    private WidgetInputFilter mWidgetInputFilter;

    public AbstractMaskedInputWidget(@Nullable HyperwalletField field, @NonNull WidgetEventListener listener,
            @Nullable String defaultValue, @NonNull View defaultFocusView) {
        super(field, listener, defaultValue, defaultFocusView);

        // TODO if @Nullable HyperwalletField field
        // then below statement will fail if null is passed
        if (mField != null && mField.getMask() != null) {
            mInputFilters = new InputFilter[]{new WidgetInputFilter(mField.getMask())};
        }
    }

    public InputFilter[] getInputFilters() {
        return mInputFilters;
    }

    protected String formatToApi(@NonNull final String displayValue) {
        if (mField.getMask() != null) {
            return displayValue.replaceAll(mField.getMask().getScrubRegex(), "");
        }
        return displayValue;
    }


    protected String formatToDisplay(@NonNull final String apiValue) {
        if (mField != null && mField.getMask().containsConditionalPattern()) {
            // format
            String pattern = mField.getMask().getPattern(apiValue);

            List<ConditionalPattern> conditionalPatterns = mField.getMask().getConditionalPatterns();

            for (ConditionalPattern conditionalPattern : conditionalPatterns) {
                /* TODO this might already be handled in Mask.java

                if (conditionalPattern.getRegex().matches(value) == true)
                    pattern = conditionalPattern.getPattern();

                 */
            }
            if (!TextUtils.isEmpty(pattern)) {
                return format(apiValue, pattern);
            } else {
                return apiValue;
            }
        }
        return apiValue;
    }

    /**
     * Helper to formatToDisplay
     */
    private String format(@NonNull final String apiValue, @NonNull final String formatTemplate) {
        StringBuilder formattedValue = new StringBuilder();
        int valueIndex = 0;
        for (int i = 0; i < formatTemplate.length(); i++) {
            if (valueIndex == apiValue.length()) {
                break;
            }

            char token = formatTemplate.charAt(i);
            switch (token) {
                case NUMBER_TOKEN:
                    if (Character.isDigit(apiValue.charAt(valueIndex))) {
                        formattedValue.append(apiValue.charAt(valueIndex));
                        valueIndex++;
                    }
                    break;
                case TEXT_TOKEN:
                    if (Character.isLetter(apiValue.charAt(valueIndex))) {
                        formattedValue.append(apiValue.charAt(valueIndex));
                        valueIndex++;
                    }
                    break;
                default:
                    formattedValue.append(token);
            }
        }
        return formattedValue.toString();
    }

    private static final class WidgetInputFilter implements InputFilter {

        private final Mask mMask;

        private WidgetInputFilter(@Nullable final Mask maskField) {
            mMask = maskField;
        }

        @Override
        public CharSequence filter(CharSequence input, int start, int end, Spanned dest, int dstart, int dend) {
            if (mMask != null) {
                if (!TextUtils.isEmpty(input)) {
                    String textToFormat = dest.toString().trim().isEmpty() ? input.toString() : dest.toString();
                    String formatPattern = mMask.getPattern(textToFormat);

                    String displayed = input.toString() + dest.toString();
                    if (dest.length() < formatPattern.length()) {
                        if (!isValueFormatted(displayed, formatPattern)) {
                            return formatInput(input.toString(), formatPattern, dend);
                        } else {
                            return input.length() > formatPattern.length() ?
                                    input.subSequence(0, formatPattern.length()) : input;
                        }
                    }
                }
                return "";
            } else {
                return input;
            }
        }

        protected String formatInput(@NonNull final String value,
                @NonNull final String formatTemplate, final int insertIndex) {
            StringBuilder formattedStringBuilder = new StringBuilder();
            int valueIndex = 0;
            for (int i = insertIndex; i < formatTemplate.length(); i++) {
                if (valueIndex == value.length()) {
                    break;
                }

                if (!isValidCharacter(value.charAt(valueIndex), formatTemplate.charAt(i))) {
                    valueIndex++; //skip value
                    --i; //reset template back
                    continue;
                }

                char token = formatTemplate.charAt(i);
                switch (token) {
                    case NUMBER_TOKEN:
                        if (Character.isDigit(value.charAt(valueIndex))) {
                            formattedStringBuilder.append(value.charAt(valueIndex));
                            valueIndex++;
                        }
                        break;
                    case TEXT_TOKEN:
                        if (Character.isLetter(value.charAt(valueIndex))) {
                            formattedStringBuilder.append(value.charAt(valueIndex));
                            valueIndex++;
                        }
                        break;
                    default: // append token
                        formattedStringBuilder.append(token);
                }
            }
            return formattedStringBuilder.toString();
        }

        private boolean isValueFormatted(@NonNull final String value, @NonNull final String template) {
            int candidateFormattedValueIndex = 0;
            for (int i = 0; i < template.length(); i++) {
                if (candidateFormattedValueIndex == value.length()) {
                    break;
                }

                char token = template.charAt(i);
                switch (token) {
                    case NUMBER_TOKEN:
                        if (!Character.isDigit(value.charAt(candidateFormattedValueIndex))) {
                            return false;
                        }
                        break;
                    case TEXT_TOKEN:
                        if (!Character.isLetter(value.charAt(candidateFormattedValueIndex))) {
                            return false;
                        }
                        break;
                    default: // append token
                        if (value.charAt(candidateFormattedValueIndex) != token) {
                            return false;
                        }
                }
                candidateFormattedValueIndex++;
            }
            return true;
        }

        private boolean isValidCharacter(final char c, final char token) {
            return Character.isLetter(c) || Character.isDigit(c) || c == token;
        }
    }
}