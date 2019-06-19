/*
 * The MIT License (MIT)
 * Copyright (c) 2019 Hyperwallet Systems Inc.
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
package com.hyperwallet.android.ui.common.viewmodel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Class that represents {@link androidx.lifecycle.LiveData} event with content
 */
public class Event<T> {

    private final T mContent;
    private boolean mIsContentConsumed;

    public Event(@NonNull final T t) {
        mContent = t;
    }

    /**
     * @return content of this event, will also mark {@link Event#mIsContentConsumed} to <code>true</code>
     * that will also mean that {@link Event#getContentIfNotConsumed()} will also return <code>true</code>
     */
    @NonNull
    public T getContent() {
        mIsContentConsumed = true;
        return mContent;
    }

    /**
     * @return <code>true</code> if content assigned to event is already referenced
     * from {@link Event#getContent()}; <code>false</code> otherwise.
     */
    public boolean isContentConsumed() {
        return mIsContentConsumed;
    }

    /**
     * Retrieve assigned content based on if and only if content has not been referenced from {@link Event#getContent()}
     *
     * @return content if content is not yet consumed; otherwise null
     */
    @Nullable
    public T getContentIfNotConsumed() {
        if (!mIsContentConsumed) {
            mIsContentConsumed = true;
            return mContent;
        }
        return null;
    }
}
