package com.hyperwallet.android.ui.testutils.espresso;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.ViewAssertion;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;

public class RecyclerViewCountAssertion implements ViewAssertion {
    private final int mCount;

    public RecyclerViewCountAssertion(int count) {
        this.mCount = count;
    }

    @Override
    public void check(View view, NoMatchingViewException noViewFoundException) {
        if (noViewFoundException != null) {
            throw noViewFoundException;
        }

        RecyclerView recyclerView = (RecyclerView) view;
        RecyclerView.Adapter adapter = recyclerView.getAdapter();

        MatcherAssert.assertThat(adapter.getItemCount(), CoreMatchers.is(mCount));
    }
}
