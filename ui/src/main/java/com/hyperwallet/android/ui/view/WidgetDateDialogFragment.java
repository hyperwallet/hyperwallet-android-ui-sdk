package com.hyperwallet.android.ui.view;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.hyperwallet.android.hyperwallet_ui.R;
import com.hyperwallet.android.ui.view.widget.DateUtil;

import java.util.Calendar;

public class WidgetDateDialogFragment extends DialogFragment {

    public static final String TAG = WidgetDateDialogFragment.class.getName();
    private static final String ARGUMENT_DATE = "ARGUMENT_DATE";
    private static final String ARGUMENT_FIELD_NAME = "ARGUMENT_FIELD_NAME";
    private OnSelectedDateCallback mOnSelectedDateCallback;
    private final DateUtil mDateUtil = new DateUtil();

    /**
     * Please do not use this to have instance of DateDialogFragment this is reserved for android framework
     */
    public WidgetDateDialogFragment() {
        setRetainInstance(true);
    }

    public static WidgetDateDialogFragment newInstance(final String date, @NonNull final String fieldName) {
        WidgetDateDialogFragment widgetDateDialogFragment = new WidgetDateDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARGUMENT_DATE, date);
        bundle.putString(ARGUMENT_FIELD_NAME, fieldName);
        widgetDateDialogFragment.setArguments(bundle);

        return widgetDateDialogFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mOnSelectedDateCallback = (OnSelectedDateCallback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement "
                    + OnSelectedDateCallback.class.getCanonicalName());
        }
    }

    public void show(@NonNull FragmentManager manager) {
        show(manager, TAG);
    }

    @Override
    public void onDestroyView() {
        Dialog dialog = getDialog();
        if (dialog != null && getRetainInstance()) {
            dialog.setDismissMessage(null);
        }
        super.onDestroyView();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle state) {

        String storedDate = getArguments() != null ? getArguments().getString(ARGUMENT_DATE) : "";
        final String fieldName = getArguments().getString(ARGUMENT_FIELD_NAME);
        Calendar calendar;
        try {
            calendar = mDateUtil.convertDateFromServerFormatToCalendar(storedDate);
        } catch (Exception e) {
            calendar = Calendar.getInstance();
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                R.style.Widget_Hyperwallet_DatePicker,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int resultYear, int resultMonth, int resultDayOfMonth) {
                        final String selectedDate = mDateUtil
                                .buildDateFromDateDialogToServerFormat(resultYear, resultMonth, resultDayOfMonth);
                        mOnSelectedDateCallback.setSelectedDateField(fieldName, selectedDate);
                    }
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                getString(R.string.cancel_button_label),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == DialogInterface.BUTTON_NEGATIVE) {
                            mOnSelectedDateCallback.setSelectedDateField(fieldName, null);
                        }
                    }
                });

        datePickerDialog.setCanceledOnTouchOutside(false);
        return datePickerDialog;
    }

    public interface OnSelectedDateCallback {
        void setSelectedDateField(@NonNull final String fieldName, final String selectedValue);
    }
}
