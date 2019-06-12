package com.hyperwallet.android.common.view;

import android.os.SystemClock;
import android.view.View;

/**
 * Helper class that holds a time of the first click event and won't call onClick callback during a delay.
 */
public abstract class OneClickListener implements View.OnClickListener {
    private static final long CLICK_DELAY_MILLIS = 800L;
    private long mFirstOccurrenceClickTime;

    @Override
    public void onClick(View v) {
        if (SystemClock.elapsedRealtime() - mFirstOccurrenceClickTime < CLICK_DELAY_MILLIS) {
            return;
        }

        onOneClick(v);
        mFirstOccurrenceClickTime = SystemClock.elapsedRealtime();

    }

    /**
     * Handle click events with a delay. This callback is time aware and will not appear the second time during a delay.
     *
     * @param v View
     */
    public abstract void onOneClick(View v);
}

