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

import static com.hyperwallet.android.ui.transfermethod.view.widget.ExpireDateUtils.MAX_INPUT_LENGTH;
import static com.hyperwallet.android.ui.transfermethod.view.widget.ExpireDateUtils.ONE_CHAR;
import static com.hyperwallet.android.ui.transfermethod.view.widget.ExpireDateUtils.SEPARATOR;
import static com.hyperwallet.android.ui.transfermethod.view.widget.ExpireDateUtils.ZERO_CHAR;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputLayout;
import com.hyperwallet.android.model.graphql.field.HyperwalletField;
import com.hyperwallet.android.ui.R;

public class ExpiryDateWidget extends AbstractWidget {
    private final ExpireDateUtils mExpireDateUtils;
    private ViewGroup mContainer;
    private TextInputLayout mTextInputLayout;
    private String mValue;
    private String mMessageInvalidDateLength;
    private String mMessageInvalidDate;

    public ExpiryDateWidget(@NonNull HyperwalletField field, @NonNull WidgetEventListener listener,
            @Nullable String defaultValue, @NonNull View defaultFocusView) {
        super(field, listener, defaultValue, defaultFocusView);
        mValue = defaultValue;
        mExpireDateUtils = new ExpireDateUtils();
    }

    @Override
    public View getView(@NonNull final ViewGroup viewGroup) {
        if (mContainer == null) {
            mContainer = (ViewGroup) LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.item_widget_layout, viewGroup, false);

            // initialize messaging
            mMessageInvalidDateLength = viewGroup.getContext().getResources()
                    .getString(R.string.error_exact_length_field, MAX_INPUT_LENGTH);
            mMessageInvalidDate = viewGroup.getContext().getResources()
                    .getString(R.string.error_invalid_expiry_date, mField.getLabel());

            // input control
            mTextInputLayout = new TextInputLayout(new ContextThemeWrapper(viewGroup.getContext(),
                    mField.isEditable() ? R.style.Widget_Hyperwallet_TextInputLayout
                            : R.style.Widget_Hyperwallet_TextInputLayout_Disabled));
            final EditText editText = new EditText(
                    new ContextThemeWrapper(viewGroup.getContext(), R.style.Widget_Hyperwallet_TextInputEditText));

            editText.setEnabled(mField.isEditable());
            setIdFromFieldLabel(mTextInputLayout);
            setIdFromFieldName(editText);
            editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus) {
                        mValue = mExpireDateUtils.convertDateToServerFormat(((EditText) v).getText().toString());
                        mListener.valueChanged(ExpiryDateWidget.this);
                    } else {
                        mListener.widgetFocused(ExpiryDateWidget.this.getName());
                        editText.setHint(editText.getText().toString().trim().isEmpty() ?
                                viewGroup.getContext().getResources().getString(R.string.api_expiry_date_format) : "");

                        InputMethodManager imm = (InputMethodManager)
                                viewGroup.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
                    }
                }
            });

            editText.addTextChangedListener(new TextWatcher() {
                boolean ignoreConcurrentChanges;
                int changeStart;
                int insertionSize;
                String[] dateParts = new String[2]; //{MM, YY}

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    if (ignoreConcurrentChanges) {
                        return;
                    }
                    changeStart = start;
                    insertionSize = after;
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (ignoreConcurrentChanges) {
                        return;
                    }

                    boolean inErrorState = false;

                    String input = s.toString().replaceAll(SEPARATOR, "");
                    // some phones have extra characters in softkey or using hard keyboard from phone
                    if (!input.isEmpty() && !TextUtils.isDigitsOnly(input)) {
                        if (input.length() > 2) {
                            input = input.substring(0, start - 1);
                        } else {
                            input = input.substring(0, start);
                        }
                    }

                    if (changeStart == 2 && insertionSize == 0 && input.length() == 2) {
                        // controls separator "/"
                        input = input.substring(0, 1);
                    } else if (changeStart == 0 && insertionSize == 1 && input.length() == 1) {
                        // controls first character either "0" | "1" other than that will be prefixed with "0"
                        char firstCharacter = input.charAt(0);
                        if (!(firstCharacter == ZERO_CHAR || firstCharacter == ONE_CHAR)) {
                            input = ZERO_CHAR + input;
                            insertionSize++;
                        }
                    }

                    dateParts = mExpireDateUtils.getDateParts(input);

                    if (!mExpireDateUtils.isValidMonth(dateParts[0])) {
                        inErrorState = true;
                    }

                    StringBuilder dateBuilder = new StringBuilder();
                    dateBuilder.append(dateParts[0]);
                    // dateParts[0] {MM}
                    if ((dateParts[0].length() == 2 && insertionSize > 0 && !inErrorState)
                            || input.length() > 2) {
                        dateBuilder.append("/");
                    }
                    // dateParts[1] {YY}
                    if (dateParts[1].length() > 2) {
                        dateParts[1] = dateParts[1].substring(0, 2);
                    }
                    dateBuilder.append(dateParts[1]);

                    String formattedDate = dateBuilder.toString();
                    int cursorPosition = mExpireDateUtils.getCursorPosition(formattedDate.length(), changeStart,
                            insertionSize);

                    ignoreConcurrentChanges = true;
                    editText.setText(formattedDate);
                    editText.setSelection(cursorPosition);

                    if (before != count) {
                        mValue = mExpireDateUtils.convertDateToServerFormat(formattedDate);
                        mListener.saveTextChanged(getName(), mValue);
                    }
                    ignoreConcurrentChanges = false;
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            editText.setInputType(InputType.TYPE_CLASS_DATETIME);
            editText.setHint(mField.getLabel());
            editText.setText(mExpireDateUtils.convertDateFromServerFormat(
                    TextUtils.isEmpty(mDefaultValue) ? mField.getValue() : mDefaultValue));

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

    @Override
    public String getErrorMessage() {
        if (mField == null || mField.getHyperwalletValidationMessage() == null) {
            return null;
        }
        if (isInvalidEmptyValue()) {
            return mField.getHyperwalletValidationMessage().getEmpty();
        }

        if (isInvalidLength()) {
            return mMessageInvalidDateLength;
        }

        if (mExpireDateUtils.isInvalidDate(mValue)) {
            return mMessageInvalidDate;
        }

        return null;
    }

    @Override
    protected boolean isInvalidRegex() {
        return mExpireDateUtils.isInvalidDate(mValue);
    }
}

