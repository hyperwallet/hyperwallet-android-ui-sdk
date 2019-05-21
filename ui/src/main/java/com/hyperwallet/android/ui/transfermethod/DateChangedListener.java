package com.hyperwallet.android.ui.transfermethod;

public interface DateChangedListener {
    void onUpdate(int resultYear, int resultMonth, int resultDayOfMonth);

    void onCancel();
}
