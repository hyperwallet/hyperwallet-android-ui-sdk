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
package com.hyperwallet.android.ui.view.widget;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputLayout;
import com.hyperwallet.android.hyperwallet_ui.R;
import com.hyperwallet.android.model.meta.HyperwalletField;

public class PhoneWidget extends AbstractWidget {
    private ViewGroup mContainer;
    private String mValue = "";
    private TextInputLayout mTextInputLayout;

    public PhoneWidget(@NonNull HyperwalletField field, @NonNull WidgetEventListener listener, @NonNull Context context,
            @Nullable String defaultValue, @NonNull View defaultFocusView) {
        super(field, listener, context, defaultValue, defaultFocusView);
        mValue = defaultValue;
    }

    @Override
    public View getView() {
        if (mContainer == null) {
            mContainer = new RelativeLayout(mContext);
            mTextInputLayout = new TextInputLayout(
                    new ContextThemeWrapper(mContext, R.style.Widget_Hyperwallet_TextInputLayout));
            // input control
            final EditText editText = new EditText(
                    new ContextThemeWrapper(mContext, R.style.Widget_Hyperwallet_TextInputEditText));
            mTextInputLayout.addView(editText);
            mTextInputLayout.setHint(mField.getLabel());
            setIdFromFieldName(editText);

            editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus) {
                        mValue = ((EditText) v).getText().toString();
                        mListener.valueChanged();
                    } else {
                        mListener.widgetFocused(PhoneWidget.this.getName());
                    }
                }
            });
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (before != count) {
                        mValue = s.toString();
                        mListener.saveTextChanged(getName(), getValue());
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            editText.setInputType(InputType.TYPE_CLASS_PHONE);
            editText.setOnKeyListener(new DefaultKeyListener(mDefaultFocusView, editText));
            editText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_ACTION_NEXT);
            editText.setText(mDefaultValue);
            editText.setTag(mField.getName());
            appendLayout(mTextInputLayout, true);
            mContainer.addView(mTextInputLayout);
        }

        return mContainer;
    }

    @Override
    public String getValue() {
        return mValue;
    }

    @Override
    public void showValidationError(String errorMessage) {
        mTextInputLayout.setError(errorMessage);
    }
}
