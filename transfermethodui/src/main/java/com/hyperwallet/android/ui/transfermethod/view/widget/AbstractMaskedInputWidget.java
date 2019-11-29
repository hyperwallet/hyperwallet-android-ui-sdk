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

import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hyperwallet.android.model.graphql.field.HyperwalletField;

public abstract class AbstractMaskedInputWidget extends AbstractWidget {
    private static final char NUMBER_TOKEN = '#';
    private static final char TEXT_TOKEN = '@';
    private static final char LETTER_OR_NUMBER_TOKEN = '*';

    public AbstractMaskedInputWidget(@Nullable HyperwalletField field, @NonNull WidgetEventListener listener,
            @Nullable String defaultValue, @NonNull View defaultFocusView) {
        super(field, listener, defaultValue, defaultFocusView);
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
     *
     * TODO switch to private if possible, currently set as package for easier access to write unit tests
     */
    String format(@NonNull final String apiValue, @NonNull final String pattern) {
        if (apiValue == null || apiValue.length() == 0 || pattern == null || pattern.length() == 0) {
            return "";
        }
        StringBuilder formattedValue = new StringBuilder();
        String extraTokens = "";
        int patternIndex = 0;
        int textIndex = 0;

        while (true) {
            char token = pattern.charAt(patternIndex);
            char textChar = apiValue.charAt(textIndex);

            switch (token) {
                case NUMBER_TOKEN:
                    if (Character.isDigit(textChar)) {
                        if (extraTokens.length() > 0) {
                            formattedValue.append(extraTokens);
                            extraTokens = "";
                        }
                        formattedValue.append(textChar);
                        patternIndex++;
                    }
                    textIndex++;
                    break;
                case TEXT_TOKEN:
                    if (Character.isLetter(textChar)) {
                        if (extraTokens.length() > 0) {
                            formattedValue.append(extraTokens);
                            extraTokens = "";
                        }
                        formattedValue.append(textChar);
                        patternIndex++;
                    }
                    textIndex++;
                    break;
                case LETTER_OR_NUMBER_TOKEN:
                    if (Character.isDigit(textChar) || Character.isLetter(textChar)) {
                        if (extraTokens.length() > 0) {
                            formattedValue.append(extraTokens);
                            extraTokens = "";
                        }
                        formattedValue.append(textChar);
                        patternIndex++;
                    }
                    textIndex++;
                    break;
                default:
                    extraTokens += token;
                    patternIndex++;
            }
            if (textIndex >= apiValue.length() || patternIndex >= pattern.length()) {
                break;
            }
        }
        return formattedValue.toString();
    }
}