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

    static String getInputFormat(@NonNull final String value,
            @NonNull final String formatTemplate, final int insertIndex) {
        StringBuilder formattedStringBuilder = new StringBuilder();
        int indexToInsert = insertIndex;
        int valueIndex = 0;
        for (int i = 0; i < value.length(); i++) {
            char token = formatTemplate.charAt(indexToInsert);
            switch (token) {
                case NUMBER_TOKEN:
                    if (Character.isDigit(value.charAt(i))) {
                        formattedStringBuilder.append(value.charAt(valueIndex));
                        valueIndex++;
                    }
                    break;
                case TEXT_TOKEN:
                    if (Character.isLetter(value.charAt(i))) {
                        formattedStringBuilder.append(value.charAt(valueIndex));
                        valueIndex++;
                    }
                    break;
                default: // append token
                    formattedStringBuilder.append(token);
            }
            indexToInsert++;
        }
        return formattedStringBuilder.toString();
    }
}
