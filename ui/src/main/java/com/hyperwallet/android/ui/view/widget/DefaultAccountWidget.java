package com.hyperwallet.android.ui.view.widget;

import static com.hyperwallet.android.model.HyperwalletTransferMethod.TransferMethodFields.IS_DEFAULT_TRANSFER_METHOD;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hyperwallet.android.hyperwallet_ui.R;

public class DefaultAccountWidget extends AbstractWidget {

    private ViewGroup mContainer;
    private String mValue;

    public DefaultAccountWidget(@NonNull WidgetEventListener listener, @NonNull Context context,
            @Nullable String defaultValue, @NonNull View defaultFocusView) {
        super(null, listener, context, defaultValue, defaultFocusView);
    }

    @Override
    public String getName() {
        return IS_DEFAULT_TRANSFER_METHOD;
    }

    @Override
    public View getView() {
        if (mContainer == null) {
            mContainer = new RelativeLayout(mContext);
            // label
            TextView label = new TextView(mContext);
            label.setText(mContext.getResources().getText(R.string.default_account_label));

            label.setTextSize(mContext.getResources().getDimension(R.dimen.font_subtitle2));
            setIdFromFieldLabel(label);
            label.setTypeface(null, Typeface.BOLD);
            label.setTextColor(mContext.getResources().getColor(R.color.colorPrimary));

            appendLayout(label, true);
            mContainer.addView(label);

            // switch control
            Switch toggle = new Switch(mContext);
            setIdFromFieldName(toggle);
            toggle.setText(mContext.getResources().getText(R.string.default_account_sub_label));
            toggle.setTextSize(mContext.getResources().getDimension(R.dimen.font_subtitle2));
            if (mDefaultValue == null) {
                toggle.setChecked(true); // initial state
                mValue = Boolean.TRUE.toString();
            } else {
                mValue = mDefaultValue;
                toggle.setChecked(Boolean.parseBoolean(mDefaultValue));
            }
            toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        mValue = Boolean.TRUE.toString();
                    } else {
                        mValue = Boolean.FALSE.toString();
                    }
                    mListener.valueChanged();
                }
            });
            appendLayout(toggle, true);
            mContainer.addView(toggle);
        }
        return mContainer;
    }

    @Override
    public String getValue() {
        return mValue;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public String getErrorMessage() {
        return null;
    }

    @Override
    public void showValidationError(String errorMessage) {

    }
}
