package com.hyperwallet.android.transaction_history.viewmodel;

public class Event<T> {

    private T mT;
    boolean mHasBeenHandled;

    public Event(T t) {
        mT = t;
    }


    public T getContent() {
        return mT;
    }

    public boolean hasBeenHandled() {
        return mHasBeenHandled;
    }

    public T getContentIfNotHandled() {
        if (mHasBeenHandled) {
            return null;
        } else {
            mHasBeenHandled = true;
            return mT;
        }
    }

    public T peekContent() {
        return mT;
    }

}
