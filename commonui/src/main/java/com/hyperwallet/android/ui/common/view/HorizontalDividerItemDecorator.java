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
package com.hyperwallet.android.ui.common.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hyperwallet.android.ui.common.R;

public class HorizontalDividerItemDecorator extends RecyclerView.ItemDecoration {

    protected final Drawable mHorizontalItemDivider;
    protected final int mDefaultPadding;

    public HorizontalDividerItemDecorator(@NonNull final Context context, final boolean withHeaderDivider) {
        mHorizontalItemDivider = context.getResources().getDrawable(R.drawable.horizontal_divider, null);
        // get dp from dimension configuration
        if (withHeaderDivider) {
            mDefaultPadding = (int) (context.getResources().getDimension(R.dimen.default_padding)
                    / context.getResources().getDisplayMetrics().density) * 2;
        } else {
            mDefaultPadding = 0;
        }
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent,
            @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        int itemCount = state.getItemCount();

        final int itemPosition = parent.getChildAdapterPosition(view);

        // no position, leave it alone
        if (itemPosition == RecyclerView.NO_POSITION) {
            return;
        }

        if (itemPosition == 0) { // first item
            outRect.set(view.getPaddingLeft(), mDefaultPadding, view.getPaddingRight(), view.getPaddingBottom());
        } else if (itemCount > 0 && itemPosition == itemCount - 1) { // last item
            outRect.set(view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom());
        } else { // middle items
            outRect.set(view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom());
        }
    }

    @Override
    public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int right = parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            int left = 0;
            int top;
            int bottom;
            View child = parent.getChildAt(i);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            if (i == 0) { // first
                // draw top
                top = child.getTop() + params.topMargin;
                bottom = top + mHorizontalItemDivider.getIntrinsicHeight();
                mHorizontalItemDivider.setBounds(left, top, right, bottom);
                mHorizontalItemDivider.draw(c);

                // draw bottom
                if (childCount > 1) { // middle line style
                    left = ((ViewGroup) child).getChildAt(1).getLeft();
                }
                top = child.getBottom() + params.bottomMargin;
            } else if (i == parent.getChildCount() - 1) { // draw bottom
                top = child.getBottom() + params.bottomMargin;
            } else { //draw middle
                left = ((ViewGroup) child).getChildAt(1).getLeft();
                top = child.getBottom() + params.bottomMargin;
            }
            bottom = top + mHorizontalItemDivider.getIntrinsicHeight();
            mHorizontalItemDivider.setBounds(left, top, right, bottom);
            mHorizontalItemDivider.draw(c);
        }
    }
}
