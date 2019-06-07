package com.hyperwallet.android.receipt.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ReceiptItemDividerDecorator extends RecyclerView.ItemDecoration {

    private final Drawable mHorizontalItemDivider;
    private final int mDefaultPadding;

    public ReceiptItemDividerDecorator(@NonNull final Context context, final boolean withHeaderDivider) {
        mHorizontalItemDivider = context.getResources().getDrawable(
                com.hyperwallet.android.common.R.drawable.horizontal_divider, null);
        // get dp from dimension configuration
        if (withHeaderDivider) {
            mDefaultPadding = (int) (context.getResources().getDimension(
                    com.hyperwallet.android.common.R.dimen.default_padding)
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

                if (childCount > 1) { // draw bottom
                    if (child instanceof LinearLayout) { // receipt header
                        left = ((ViewGroup) ((ViewGroup) child).getChildAt(1)).getChildAt(1).getLeft();
                    } else { // receipt item
                        left = ((ViewGroup) child).getChildAt(1).getLeft();
                    }
                }
                top = child.getBottom() + params.bottomMargin;
            } else if (i == parent.getChildCount() - 1) { // draw bottom
                top = child.getBottom() + params.bottomMargin;
            } else { //draw middle
                if (child instanceof LinearLayout) {
                    left = ((ViewGroup) ((ViewGroup) child).getChildAt(1)).getChildAt(1).getLeft();
                } else {
                    // peek if its a header then draw line from beginning
                    if (parent.getChildAt(i + 1) != null
                            && parent.getChildAt(i + 1) instanceof LinearLayout) {
                        left = 0;
                    } else {
                        left = ((ViewGroup) child).getChildAt(1).getLeft();
                    }
                }
                top = child.getBottom() + params.bottomMargin;
            }
            bottom = top + mHorizontalItemDivider.getIntrinsicHeight();
            mHorizontalItemDivider.setBounds(left, top, right, bottom);
            mHorizontalItemDivider.draw(c);
        }
    }
}
