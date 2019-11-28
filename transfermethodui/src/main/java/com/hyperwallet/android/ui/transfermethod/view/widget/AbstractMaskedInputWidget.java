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
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hyperwallet.android.model.graphql.field.HyperwalletField;
import com.hyperwallet.android.model.graphql.field.Mask;

public abstract class AbstractMaskedInputWidget extends AbstractWidget {
    private static final char NUMBER_TOKEN = '#';
    private static final char TEXT_TOKEN = '@';

    private InputFilter[] mInputFilters;  // TODO delete
    private WidgetInputFilter mWidgetInputFilter;  // TODO delete

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
        if (mField != null && mField.getMask() != null) {
            // format
            String pattern = mField.getMask().getPattern(apiValue);
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

    // TODO delete inner class
    private static final class WidgetInputFilter implements InputFilter {

        private final Mask mMask;

        private WidgetInputFilter(@Nullable final Mask maskField) {
            mMask = maskField;
        }

        @Override
        public CharSequence filter(CharSequence input, int start, int end, Spanned dest, int dstart, int dend) {

            if (input.toString() == "") {
                Log.d("filter input", "(empty string)/backspace");
            } else {
                Log.d("filter input", input.toString());
            }
            Log.d("filter start", String.valueOf(start));
            Log.d("filter end", String.valueOf(end));
            Log.d("filter dest", dest.toString());
            Log.d("filter dstart", String.valueOf(dstart));
            Log.d("filter dend", String.valueOf(dend));

            if (mMask != null) {
//                if (!TextUtils.isEmpty(input)) {
                String textToFormat = dest.toString().trim().isEmpty() ? input.toString() : dest.toString();
                String formatPattern = mMask.getPattern(textToFormat);

                String displayed = input.toString() + dest.toString();
                if (dest.length() < formatPattern.length()) {
                    if (!isValueFormatted(displayed, formatPattern)) {
                        Log.d("filter return3", formatInput(input.toString(), formatPattern, dend));
                        return formatInput(input.toString(), formatPattern, dend);
                    } else {
                        String return2Value = (input.length() > formatPattern.length() ?
                                input.subSequence(0, formatPattern.length()) : input).toString();
                        if (return2Value == "") {
                            return2Value = "- ";
                            Log.d("filter return2", "(empty return value)");
                            return return2Value;
                        } else {
                            Log.d("filter return2", return2Value);
                        }

                        return input.length() > formatPattern.length() ?
                                input.subSequence(0, formatPattern.length()) : input;
                    }
                }
//                }
                Log.d("filter return5", "(empty string)");
                return null;
            } else {
                Log.d("filter return4", input.toString());
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