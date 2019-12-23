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

import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputLayout;
import com.hyperwallet.android.model.graphql.field.Field;
import com.hyperwallet.android.ui.R;

public class TextWidget extends AbstractMaskedInputWidget {

    public TextWidget(@NonNull Field field, @NonNull WidgetEventListener listener,
            @Nullable String defaultValue, @NonNull View defaultFocusView) {
        super(field, listener, defaultValue, defaultFocusView);
        mValue = defaultValue;
    }

    @Override
    public View getView(@NonNull final ViewGroup viewGroup) {
        if (mContainer == null) {
            mContainer = (ViewGroup) LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.item_widget_layout, viewGroup, false);
            mContainer.setTag(mField.getName());

            mTextInputLayout = new TextInputLayout(new ContextThemeWrapper(viewGroup.getContext(),
                    mField.isEditable() ? R.style.Widget_Hyperwallet_TextInputLayout
                            : R.style.Widget_Hyperwallet_TextInputLayout_Disabled));

            final EditText editText = new EditText(
                    new ContextThemeWrapper(viewGroup.getContext(), R.style.Widget_Hyperwallet_TextInputEditText));
            editText.setTextColor(viewGroup.getContext().getResources().getColor(R.color.regularColorSecondary));
            editText.setEnabled(mField.isEditable());

            mTextInputLayout.setHint(mField.getLabel());
            setIdFromFieldLabel(mTextInputLayout);
            setIdFromFieldName(editText);
            editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus) {
                        String input = ((EditText) v).getText().toString();
                        mValue = formatToApi(input);
                        mListener.valueChanged(TextWidget.this);
                    } else {
                        mListener.widgetFocused(TextWidget.this.getName());
                    }
                }
            });
            editText.addTextChangedListener(new TextWatcher() {

                boolean ignoreChange;

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (ignoreChange) {
                        ignoreChange = false;
                        return;
                    }

                    if (!TextUtils.isEmpty(mValue)) {
                        String displayValue = formatToDisplay(mValue);
                        editText.setText(displayValue);
                        editText.setSelection(displayValue.length());
                    }
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (ignoreChange) return;

                    if (before != count) {
                        mValue = formatToApi(s.toString());
                        mListener.saveTextChanged(getName(), getValue());
                    } else {
                        ignoreChange = true;
                    }
                }
            });

            editText.setText(TextUtils.isEmpty(mDefaultValue) ? mField.getValue() : mDefaultValue);
            editText.setInputType(InputType.TYPE_CLASS_TEXT);
//            if (mField.getMask() != null) {
//                InputFilter[] filters = new InputFilter[]{new InputFilter() {
//                    @Override
//                    public CharSequence filter(CharSequence source,
//                            int start, int end, Spanned dest,
//                            int dstart, int dend) {
//
//                        if (source.equals("")) {
//                            // this means backspace is triggered
//                            return "";
//                        }
//                        return formatToDisplay(insertString(dest.toString(), source.toString(), dstart, dend));
//                    }
//
//                    private String insertString(final String dest, final String toInsert, int start, int end) {
//                        // last
//                        if (start != 0 && end == dest.length()) {
//                            return dest + toInsert;
//                        }
//                        // middle
//                        else if (start != 0 && start == end) {
//                            return dest.substring(0, start - 1) + toInsert + dest.substring(end);
//                        }
//
//                        // start
//                        return toInsert + dest;
//                    }
//                }};
//                editText.setFilters(filters);
//            }
            editText.setOnKeyListener(new DefaultKeyListener(mDefaultFocusView, editText));
            editText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_ACTION_NEXT);

            mTextInputLayout.addView(editText);
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
