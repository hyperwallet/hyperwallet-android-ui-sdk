package com.hyperwallet.android.receipt.view;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hyperwallet.android.common.view.HorizontalDividerItemDecorator;

public class ReceiptItemDividerDecorator extends HorizontalDividerItemDecorator {

    ReceiptItemDividerDecorator(@NonNull final Context context, final boolean withHeaderDivider) {
        super(context, withHeaderDivider);
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
                if (child instanceof LinearLayout) { // header found
                    // peek if next is a header then draw line from beginning
                    if (parent.getChildAt(i + 1) != null
                            && parent.getChildAt(i + 1) instanceof LinearLayout) {
                        left = 0;
                    } else {
                        left = ((ViewGroup) ((ViewGroup) child).getChildAt(1)).getChildAt(1).getLeft();
                    }
                } else { // non header
                    // peek if next is a header then draw line from beginning
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
