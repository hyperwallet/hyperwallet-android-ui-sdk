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
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;

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

    public DateWidget(@NonNull HyperwalletField field, @NonNull WidgetEventListener listener, @NonNull Context context,
            @Nullable String defaultValue, @NonNull View defaultFocusView) {
        super(field, listener, context, defaultValue, defaultFocusView);
        mDateUtil = new DateUtil();
        mValue = defaultValue;
    }

    @Override
    public View getView() {
        if (mContainer == null) {
            mContainer = new RelativeLayout(mContext);
            setIdFromFieldLabel(mContainer);
            mContainer.setFocusable(true);
            mContainer.setFocusableInTouchMode(false);
            // input control
            mTextInputLayout = new TextInputLayout(new ContextThemeWrapper(mContext,
                    mField.isEditable() ? R.style.Widget_Hyperwallet_TextInputLayout
                            : R.style.Widget_Hyperwallet_TextInputLayout_Disabled));
            mEditText = new EditText(
                    new ContextThemeWrapper(mContext, R.style.Widget_Hyperwallet_TextInputEditText));
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

            if (mField.isEditable()) {
                mEditText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDateSelectDialog();
                    }
                });
            }

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

    private void showDateSelectDialog() {
        Calendar calendar = mDateUtil.convertDateFromServerFormatToCalendar(mValue);
        DatePickerDialog datePickerDialog = new DatePickerDialog(mContext, this,
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, mContext.getString(R.string.cancel_button_label),
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
