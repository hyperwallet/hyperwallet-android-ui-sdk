package com.hyperwallet.android.ui.testutils.util;

import android.view.View;

import androidx.core.widget.NestedScrollView;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.ScrollToAction;
import androidx.test.espresso.matcher.ViewMatchers;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

public class NestedScrollToAction implements ViewAction {
    private static final String TAG = ScrollToAction.class.getSimpleName();

    @SuppressWarnings("unchecked")
    @Override
    public Matcher<View> getConstraints() {
        return Matchers.allOf(
                ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE),
                ViewMatchers.isDescendantOfA(
                        Matchers.anyOf(ViewMatchers.isAssignableFrom(NestedScrollView.class))));
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
