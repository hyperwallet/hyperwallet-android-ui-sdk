package com.hyperwallet.android.ui.transfermethod.view.widget;

import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hyperwallet.android.model.graphql.field.HyperwalletField;
import com.hyperwallet.android.model.graphql.field.HyperwalletMaskField;

public abstract class AbstractMaskedInputWidget extends AbstractWidget {

    private InputFilter[] mInputFilter;
    static final char NUMBER_TOKEN = '#';
    static final char TEXT_TOKEN = '@';

    public AbstractMaskedInputWidget(
            @Nullable HyperwalletField field,
            @NonNull WidgetEventListener listener,
            @Nullable String defaultValue,
            @NonNull View defaultFocusView) {
        super(field, listener, defaultValue, defaultFocusView);

        if (mField.getHyperwalletMaskField() != null) {
            mInputFilter = new InputFilter[]{new WidgetInputFilter(mField.getHyperwalletMaskField())};
        }

    }

    public InputFilter[] getInputFilter() {
        return mInputFilter;
    }


    protected String formatToApi(@NonNull final String value) {
        if (mField.getHyperwalletMaskField() != null) {
            return value.replaceAll(mField.getHyperwalletMaskField().getScrubRegex(), "");
        }
        return value;
    }


    protected String formatToDisplay(@NonNull final String value) {
        if (mField.getHyperwalletMaskField().containsConditionalPattern()) {
            // format
            String formatTemplate = mField.getHyperwalletMaskField().getConditionalPattern(value);

            if (!TextUtils.isEmpty(formatTemplate)) {
                return format(value, formatTemplate);
            } else {
                return value;
            }
        }
        return value;
    }



    private String format(@NonNull final String value, @NonNull final String formatTemplate) {
        StringBuilder formattedValue = new StringBuilder();
        int valueIndex = 0;
        for (int i = 0; i < formatTemplate.length(); i++) {
            if (valueIndex == value.length()) {
                break;
            }

            char token = formatTemplate.charAt(i);
            switch (token) {
                case NUMBER_TOKEN:
                    if (Character.isDigit(value.charAt(valueIndex))) {
                        formattedValue.append(value.charAt(valueIndex));
                        valueIndex++;
                    }
                    break;
                case TEXT_TOKEN:
                    if (Character.isLetter(value.charAt(valueIndex))) {
                        formattedValue.append(value.charAt(valueIndex));
                        valueIndex++;
                    }
                    break;
                default:
                    formattedValue.append(token);
            }
        }
        return formattedValue.toString();
    }




    private static class WidgetInputFilter implements InputFilter {

        private final HyperwalletMaskField mHyperwalletMaskField;
        private boolean hasMasking;

        protected WidgetInputFilter(@Nullable final HyperwalletMaskField maskField) {
            mHyperwalletMaskField = maskField;
            hasMasking = mHyperwalletMaskField != null;
        }

        @Override
        public CharSequence filter(CharSequence input, int start, int end, Spanned dest, int dstart, int dend) {
            if (hasMasking) {
                if (!TextUtils.isEmpty(input)) {
                    String textToFormat = dest.toString().trim().isEmpty() ? input.toString() : dest.toString();
                    String formatPattern = mHyperwalletMaskField.getConditionalPattern(textToFormat);

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


        private boolean isValidCharacter(final char c, final char token) {
            return Character.isLetter(c) || Character.isDigit(c) || c == token;
        }
    }


}
