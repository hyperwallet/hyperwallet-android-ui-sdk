package com.hyperwallet.android.ui.common.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.BoundedMatcher;

import com.google.android.material.textfield.TextInputLayout;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.Objects;

public class EspressoUtils {

    public static Matcher<View> withHint(final String expectedHint) {
        return new TypeSafeMatcher<View>() {

            @Override
            public boolean matchesSafely(View view) {
                if (!(view instanceof TextInputLayout)) {
                    return false;
                }

                String hint = Objects.toString(((TextInputLayout) view).getHint());
                return expectedHint.equals(hint);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(expectedHint);
            }
        };
    }

    public static Matcher<View> hasErrorText(final String expectedErrorMessage) {
        return new TypeSafeMatcher<View>() {

            @Override
            public boolean matchesSafely(View view) {
                if (!(view instanceof TextInputLayout)) {
                    return false;
                }

                String errorMessage = Objects.toString(((TextInputLayout) view).getError());
                return expectedErrorMessage.equals(errorMessage);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(expectedErrorMessage);
            }
        };
    }

    public static Matcher<View> hasErrorText(final int resourceId) {
        return new TypeSafeMatcher<View>() {

            @Override
            public boolean matchesSafely(View view) {
                if (!(view instanceof TextInputLayout)) {
                    return false;
                }
                String expectedErrorMessage = view.getResources().getString(resourceId);
                String errorMessage = Objects.toString(((TextInputLayout) view).getError());

                return expectedErrorMessage.equals(errorMessage);
            }

            @Override
            public void describeTo(Description description) {
            }
        };
    }

    public static Matcher<View> withDrawable(final int resourceId) {
        return new TypeSafeMatcher<View>() {

            @Override
            public boolean matchesSafely(View view) {
                if (!(view instanceof ImageView)) {
                    return false;
                }

                Drawable drawable = ((ImageView) view).getDrawable();
                if (drawable == null) {
                    return false;
                }
                Drawable expectedDrawable = view.getContext().getResources().getDrawable(resourceId);

                Bitmap bitmap = getBitmap(drawable);
                Bitmap expectedBitmap = getBitmap(expectedDrawable);

                return bitmap.sameAs(expectedBitmap);
            }

            @Override
            public void describeTo(Description description) {
            }
        };
    }

    public static Matcher<View> atPosition(final int position, @NonNull final Matcher<View> matcher) {
        return new BoundedMatcher<View, RecyclerView>(RecyclerView.class) {

            @Override
            protected boolean matchesSafely(final RecyclerView view) {
                RecyclerView.ViewHolder viewHolder = view.findViewHolderForAdapterPosition(position);

                if (viewHolder == null) {
                    return false;
                }

                return matcher.matches(viewHolder.itemView);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("has item at position " + position + ": ");
                matcher.describeTo(description);
            }
        };
    }

    public static ViewAction nestedScrollTo() {
        return ViewActions.actionWithAssertions(new NestedScrollToAction());
    }

    private static Bitmap getBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public static Matcher<View> hasNoErrorText() {
        return new TypeSafeMatcher<View>() {

            @Override
            public boolean matchesSafely(View view) {
                if (!(view instanceof TextInputLayout)) {
                    return false;
                }
                return ((TextInputLayout) view).getError() == null;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("has no error text: ");
            }
        };
    }

    public static Matcher<View> hasEmptyText() {
        return new TypeSafeMatcher<View>() {

            @Override
            public boolean matchesSafely(View view) {
                if (!(view instanceof EditText)) {
                    return false;
                }
                String text = ((EditText) view).getText().toString();

                return text.isEmpty();
            }

            @Override
            public void describeTo(Description description) {
            }
        };
    }
}

