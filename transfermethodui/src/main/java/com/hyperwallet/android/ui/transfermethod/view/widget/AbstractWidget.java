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

import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hyperwallet.android.model.graphql.field.Field;

public abstract class AbstractWidget {

    private static final String LABEL_SUFFIX = "Label";

    protected final View mDefaultFocusView;
    protected final String mDefaultValue;
    protected final Field mField;
    protected final WidgetEventListener mListener;
    protected int mBottomViewId = 0;
    protected WidgetInputState mWidgetInputState;
    public boolean isEdited = false;

    public AbstractWidget(@Nullable Field field, @NonNull WidgetEventListener listener,
                          @Nullable String defaultValue, @NonNull View defaultFocusView) {
        mField = field;
        mListener = listener;
        mDefaultValue = defaultValue;
        mDefaultFocusView = defaultFocusView;
        mWidgetInputState = new WidgetInputState(getName());
    }

    public abstract View getView(@NonNull final ViewGroup viewGroup);

    public String getName() {
        return mField == null ? "" : mField.getName();
    }

    public abstract String getValue();

    public abstract void showValidationError(String errorMessage);

    public void showValidationError() {
        getErrorMessage();
    }

    public boolean isValid() {
        if (mField == null) {
            return true;
        } else if (!isEdited && mField.isFieldValueMasked()) {
            return true;
        }
        return !isInvalidEmptyValue() && !isInvalidLength() && !isInvalidRegex();
    }

    @NonNull
    public WidgetInputState getWidgetInputState() {
        return mWidgetInputState;
    }

    public void setWidgetInputState(@NonNull WidgetInputState widgetInputState) {
        mWidgetInputState = widgetInputState;
    }

    protected boolean isInvalidRegex() {
        boolean hasRegex = mField != null && mField.getRegularExpression() != null
                && mField.getRegularExpression().trim().length() > 0;
        return mField != null && hasRegex && !isValueEmpty() && !getValue().matches(mField.getRegularExpression());
    }

    private boolean isValueEmpty() {
        return getValue() == null || getValue().trim().length() == 0;
    }

    protected boolean isInvalidLength() {
        return !isValueEmpty() && (getValue().length() < mField.getMinLength()
                || getValue().length() > mField.getMaxLength());
    }

    protected boolean isInvalidEmptyValue() {
        return mField.isRequired() && isValueEmpty();
    }

    public String getErrorMessage() {
        if (mField == null || mField.getValidationMessage() == null) {
            return null;
        }

        if (isInvalidEmptyValue()) {
            return mField.getValidationMessage().getEmpty();
        }

        if (isInvalidLength()) {
            return mField.getValidationMessage().getLength();
        }

        if (isInvalidRegex()) {
            return mField.getValidationMessage().getPattern();
        }

        return null;
    }

    protected void appendLayout(View v, boolean matchParentWidth) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                matchParentWidth ? ViewGroup.LayoutParams.MATCH_PARENT : ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        if (mBottomViewId != 0) {
            params.addRule(RelativeLayout.BELOW, mBottomViewId);
        }

        v.setLayoutParams(params);
        mBottomViewId = v.getId();
    }

    protected void setIdFromFieldName(View view) {
        final String fieldName = mField == null ? "" : mField.getName();
        if (fieldName.isEmpty()) {
            view.setId(View.generateViewId());
        } else {
            view.setId(getIdByResourceName(fieldName, view));
        }
    }

    protected void setIdFromFieldLabel(View view) {
        final String fieldName = mField == null ? "" : mField.getName();
        if (fieldName.isEmpty()) {
            view.setId(View.generateViewId());
        } else {
            view.setId(getIdByResourceName(fieldName + LABEL_SUFFIX, view));
        }
    }

    private int getIdByResourceName(@NonNull final String fieldName, @NonNull final View view) {
        final int idFromResource = view.getContext().getResources().getIdentifier(fieldName, "id",
                view.getContext().getPackageName());

        return idFromResource == 0 ? View.generateViewId() : idFromResource;
    }

    protected class DefaultKeyListener implements View.OnKeyListener {
        final View mFocusView;
        final View mClearFocusView;

        public DefaultKeyListener(View focusView, View clearFocusView) {
            mFocusView = focusView;
            mClearFocusView = clearFocusView;
        }

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            isEdited = true;
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                    case KeyEvent.KEYCODE_ENTER:
                        mFocusView.requestFocus();
                        mClearFocusView.clearFocus();
                        return true;
                    default:
                        break;
                }
            }
            return false;
        }
    }
}