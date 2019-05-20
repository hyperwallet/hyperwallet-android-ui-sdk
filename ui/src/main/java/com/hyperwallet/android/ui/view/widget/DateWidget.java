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

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.DatePicker;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputLayout;
import com.hyperwallet.android.hyperwallet_ui.R;
import com.hyperwallet.android.model.meta.field.HyperwalletField;

import java.util.Calendar;

public class DateWidget extends AbstractWidget implements DatePickerDialog.OnDateSetListener {

    private final DateUtil mDateUtil;
    private ViewGroup mContainer;
    private String mValue;
    private TextInputLayout mTextInputLayout;
    private EditText mEditText;

    public DateWidget(@NonNull HyperwalletField field, @NonNull WidgetEventListener listener,
            @Nullable String defaultValue, @NonNull View defaultFocusView) {
        super(field, listener, defaultValue, defaultFocusView);
        mDateUtil = new DateUtil();
        mValue = defaultValue;
    }

    @Override
    public View getView(@NonNull final ViewGroup viewGroup) {
        if (mContainer == null) {
            mContainer = (ViewGroup) LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.item_widget_layout, viewGroup, false);
            setIdFromFieldLabel(mContainer);
            mContainer.setFocusable(true);
            mContainer.setFocusableInTouchMode(false);

            // input control
            mTextInputLayout = new TextInputLayout(new ContextThemeWrapper(viewGroup.getContext(),
                    mField.isEditable() ? R.style.Widget_Hyperwallet_TextInputLayout
                            : R.style.Widget_Hyperwallet_TextInputLayout_Disabled));
            final EditText editText = new EditText(
                    new ContextThemeWrapper(viewGroup.getContext(), R.style.Widget_Hyperwallet_TextInputEditText));
            if (!TextUtils.isEmpty(mDefaultValue)) {
                mEditText.setText(mDateUtil.convertDateFromServerToWidgetFormat(mDefaultValue));
            }
            setIdFromFieldLabel(mTextInputLayout);

            mEditText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
            mEditText.setKeyListener(null);
            mEditText.setFocusableInTouchMode(false);
            mEditText.setFocusable(false);
            setIdFromFieldName(mEditText);
            mTextInputLayout.setHint(mField.getLabel());
            mTextInputLayout.addView(mEditText);

            if (mField.isEditable()) {
                mEditText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDateSelectDialog();
                    }
                });
            }

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

    private void showDateSelectDialog() {
        Calendar calendar = mDateUtil.convertDateFromServerFormatToCalendar(mValue);
        DatePickerDialog datePickerDialog = new DatePickerDialog(mEditText.getRootView().getContext(), this,
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                mEditText.getContext().getString(R.string.cancel_button_label),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == DialogInterface.BUTTON_NEGATIVE) {
                            if (isValid()) {
                                mTextInputLayout.setError(null);
                            }
                        }
                    }
                });
        datePickerDialog.show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        if (isValid()) {
            mTextInputLayout.setError(null);
        }

        mValue = mDateUtil.buildDateFromDateDialogToServerFormat(year, month, dayOfMonth);
        mEditText.setText(mDateUtil.buildDateFromDateDialogToWidgetFormat(year, month, dayOfMonth));
        if (isValid()) {
            mTextInputLayout.setError(null);
        }
        mListener.valueChanged();
    }
}
