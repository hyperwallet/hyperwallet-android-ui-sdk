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
package com.hyperwallet.android.ui.transfer.view;

import static com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod.TransferMethodFields.COUNTRY;
import static com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod.TransferMethodFields.TYPE;
import static com.hyperwallet.android.ui.common.util.TransferMethodUtils.getTransferMethodDetail;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod;
import com.hyperwallet.android.ui.transfer.R;

public class DestinationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private final TextView mTitle;
    private final TextView mTransferDestinationCountry;
    private final TextView mTransferDestinationIdentification;
    private final ImageView mIcon;
    private final OnItemClickListener mOnItemClickListener;

    DestinationViewHolder(@NonNull final View itemView,
            @NonNull final OnItemClickListener itemClickListener) {
        super(itemView);
        itemView.setOnClickListener(this);

        mTitle = itemView.findViewById(R.id.title);
        mTransferDestinationCountry = itemView.findViewById(R.id.description_1);
        mTransferDestinationIdentification = itemView.findViewById(R.id.description_2);
        mIcon = itemView.findViewById(R.id.item_selected_image);
        mOnItemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {
        int position = getAdapterPosition();
        mOnItemClickListener.onItemClick(position);
    }

    void bind(HyperwalletTransferMethod destination, boolean selected) {
        String type = destination.getField(TYPE);
        itemView.setOnClickListener(this);
        mTitle.setText(type);
        mTransferDestinationCountry.setText(destination.getField(COUNTRY));
        mTransferDestinationIdentification.setText(
                getTransferMethodDetail(mTransferDestinationIdentification.getContext(), destination, type));
        if (selected) {
            mIcon.setVisibility(View.VISIBLE);
        } else {
            mIcon.setVisibility(View.GONE);
        }
    }

    void recycle() {
        itemView.setOnClickListener(null);
    }
}
