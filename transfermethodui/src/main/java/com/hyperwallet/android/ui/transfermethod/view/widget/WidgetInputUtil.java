package com.hyperwallet.android.ui.transfermethod.view.widget;

import androidx.annotation.NonNull;

import com.hyperwallet.android.model.graphql.field.HyperwalletConditionalPattern;
import com.hyperwallet.android.model.graphql.field.HyperwalletMaskField;

final class WidgetInputUtil {

    static final char NUMBER_TOKEN = '#';
    static final char TEXT_TOKEN = '@';

    static String getFormatTemplate(@NonNull final String value,
            @NonNull final HyperwalletMaskField maskField) {
        if (!maskField.getHyperwalletConditionalPatterns().isEmpty()) {
            for (HyperwalletConditionalPattern rule : maskField.getHyperwalletConditionalPatterns()) {
                if (value.matches(rule.getRegex())) {
                    return rule.getPattern();
                }
            }
        }
        return maskField.getDefaultPattern();
    }

    static String getFormattedString(@NonNull final String value, @NonNull final String formatTemplate) {
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

    static boolean isValueFormatted(@NonNull final String value, @NonNull final String template) {
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

    static String getInputFormat(@NonNull final String value,
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

    static boolean isValidCharacter(final char c, final char token) {
        return Character.isLetter(c) || Character.isDigit(c) || c == token;
    }
}
