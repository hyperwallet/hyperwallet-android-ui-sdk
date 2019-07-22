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

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hyperwallet.android.exception.HyperwalletException;
import com.hyperwallet.android.model.graphql.field.EDataType;
import com.hyperwallet.android.model.graphql.field.HyperwalletField;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class WidgetFactory {

    private static final HashMap<EDataType, Class> WIDGET_MAP_DEFINITION = new HashMap<EDataType, Class>() {{
        put(EDataType.TEXT, TextWidget.class);
        put(EDataType.SELECTION, SelectionWidget.class);
        put(EDataType.PHONE, PhoneWidget.class);
        put(EDataType.NUMBER, NumberWidget.class);
        put(EDataType.DATE, DateWidget.class);
        put(EDataType.EXPIRY_DATE, ExpiryDateWidget.class);
    }};

    @SuppressWarnings("unchecked")
    public static AbstractWidget newWidget(@NonNull HyperwalletField field, @NonNull WidgetEventListener listener,
            @Nullable String defaultValue, @NonNull View view) throws HyperwalletException {
        try {
            if (WIDGET_MAP_DEFINITION.containsKey(field.getDataType())) {
                return (AbstractWidget) WIDGET_MAP_DEFINITION.get(field.getDataType())
                        .getConstructor(HyperwalletField.class, WidgetEventListener.class, String.class, View.class)
                        .newInstance(field, listener, defaultValue, view);
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new HyperwalletException(e);
        }

        return new TextWidget(field, listener, defaultValue, view);
    }
}
