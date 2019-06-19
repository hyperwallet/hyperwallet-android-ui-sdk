package com.hyperwallet.android.ui.receipt.util;

import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;

import android.view.View;

import androidx.core.widget.NestedScrollView;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.ScrollToAction;
import androidx.test.espresso.matcher.ViewMatchers;

import org.hamcrest.Matcher;

public class NestedScrollToAction implements ViewAction {
    private static final String TAG = ScrollToAction.class.getSimpleName();

    @SuppressWarnings("unchecked")
    @Override
    public Matcher<View> getConstraints() {
        return allOf(
                withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE),
                isDescendantOfA(
                        anyOf(isAssignableFrom(NestedScrollView.class))));
    }

    @Override
    public void perform(UiController uiController, View view) {
        new ScrollToAction().perform(uiController, view);
    }

    @Override
    public String getDescription() {
        return "scroll to";
    }
}
