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

import android.app.Activity;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputLayout;
import com.hyperwallet.android.model.graphql.field.HyperwalletField;
import com.hyperwallet.android.model.graphql.field.HyperwalletFieldSelectionOption;
import com.hyperwallet.android.ui.R;
import com.hyperwallet.android.ui.transfermethod.view.WidgetSelectionDialogFragment;

import java.util.Set;
import java.util.TreeMap;

public class SelectionWidget extends AbstractWidget implements WidgetSelectionDialogFragment.WidgetSelectionItemType {

    private ViewGroup mContainer;
    private EditText mEditText;
    private TreeMap<String, String> mSelectionNameValueMap;
    private TextInputLayout mTextInputLayout;
    private String mValue;

    public SelectionWidget(@NonNull HyperwalletField field, @NonNull WidgetEventListener listener,
            @Nullable String defaultValue, @NonNull View defaultFocusView) {
        super(field, listener, defaultValue, defaultFocusView);
        mValue = defaultValue;
        mSelectionNameValueMap = new TreeMap<>();
        if (field.getFieldSelectionOptions() != null) {
            for (HyperwalletFieldSelectionOption option : field.getFieldSelectionOptions()) {
                if (!TextUtils.isEmpty(option.getLabel())) {
                    mSelectionNameValueMap.put(option.getLabel(), option.getValue());
                }
            }
        }
    }

    @Override
    public View getView(@NonNull final ViewGroup viewGroup) {
        if (mContainer == null) {
            mContainer = (ViewGroup) LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.item_widget_layout, viewGroup, false);

            setIdFromFieldLabel(mContainer);
            mContainer.setFocusable(true);
            mContainer.setFocusableInTouchMode(true);

            mTextInputLayout = new TextInputLayout(new ContextThemeWrapper(viewGroup.getContext(),
                    mField.isEditable() ? R.style.Widget_Hyperwallet_TextInputLayout
                            : R.style.Widget_Hyperwallet_TextInputLayout_Disabled));
            mEditText = new EditText(
                    new ContextThemeWrapper(viewGroup.getContext(), R.style.Widget_Hyperwallet_TextInputEditText));
            mEditText.setTextColor(viewGroup.getContext().getResources().getColor(R.color.regularColorSecondary));

            mEditText.setText(
                    getKeyFromValue(TextUtils.isEmpty(mDefaultValue) ? mValue = mField.getValue() : mDefaultValue));
            setIdFromFieldLabel(mTextInputLayout);
            setIdFromFieldName(mEditText);

            mEditText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
            mEditText.setKeyListener(null);
            mEditText.setCompoundDrawablesWithIntrinsicBounds(null, null,
                    ContextCompat.getDrawable(viewGroup.getContext(), R.drawable.ic_keyboard_arrow_down_12dp), null);

            mEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        mListener.widgetFocused(SelectionWidget.this.getName());
                        hideSoftKey(v);
                        showSelectionFragmentDialog();
                    } else {
                        if (!mListener.isWidgetSelectionFragmentDialogOpen()) {
                            if (isValid()) {
                                mTextInputLayout.setError(null);
                            }
                            String label = ((EditText) v).getText().toString();
                            mValue = mSelectionNameValueMap.get(label);
                            mListener.valueChanged(SelectionWidget.this);
                        }
                    }
                }
            });

            if (mField.isEditable()) {
                mEditText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        hideSoftKey(v);
                        showSelectionFragmentDialog();
                    }
                });
            } else {
                mEditText.setEnabled(false);
            }

            mTextInputLayout.setHint(mField.getLabel());
            mTextInputLayout.addView(mEditText);
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

    @Override
    public void onWidgetSelectionItemClicked(@NonNull String selectedValue) {
        mValue = selectedValue;
        mListener.valueChanged(this);
        mEditText.setText(getKeyFromValue(selectedValue));
        mEditText.requestFocus();
    }

    private void hideSoftKey(@NonNull View focusedView) {
        InputMethodManager inputMethodManager = (InputMethodManager) focusedView.getContext().getSystemService(
                Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
    }

    private void showSelectionFragmentDialog() {
        String defaultSelected = TextUtils.isEmpty(mValue) ?
                TextUtils.isEmpty(mDefaultValue) ? getKeyFromValue(mField.getValue()) :
                        getKeyFromValue(mDefaultValue) : getKeyFromValue(mValue);
        mListener.openWidgetSelectionFragmentDialog(mSelectionNameValueMap, defaultSelected, mField.getLabel(),
                mField.getName());
    }

    private String getKeyFromValue(@NonNull String value) {
        Set<String> selections = mSelectionNameValueMap.keySet();
        for (String key : selections) {
            if (value.equals(mSelectionNameValueMap.get(key))) {
                return key;
            }
        }
        return "";
    }
}