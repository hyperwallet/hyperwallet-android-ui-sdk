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
