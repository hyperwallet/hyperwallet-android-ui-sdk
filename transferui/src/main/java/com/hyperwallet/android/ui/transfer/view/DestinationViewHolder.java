package com.hyperwallet.android.ui.transfer.view;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hyperwallet.android.ui.transfer.R;

public class DestinationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private final TextView mTitle;
    private final TextView mCountry;
    private final TextView mEnding;
    private final ImageView mItemSelectedImage;
    private final ListTransferDestinationFragment.DestinationItemClickListener
            mDestinationItemClickListener;

    DestinationViewHolder(@NonNull final View itemView,
            @NonNull final ListTransferDestinationFragment.DestinationItemClickListener destinationItemClickListener) {
        super(itemView);
        itemView.setOnClickListener(this);

        mTitle = itemView.findViewById(R.id.title);
        mCountry = itemView.findViewById(R.id.description_1);
        mEnding = itemView.findViewById(R.id.description_2);
        mItemSelectedImage = itemView.findViewById(R.id.item_selected_image);
        mDestinationItemClickListener = destinationItemClickListener;
    }

    @Override
    public void onClick(View v) {
        int position = getAdapterPosition();
        mDestinationItemClickListener.onDestinationItemClicked(position);
    }

    void bind(String currencyName) {
        itemView.setOnClickListener(this);
        mTitle.setText(currencyName);
        //mCountry.setText();
//        if (currencyName.equals(mSelectedCurrencyName)) {
//            mItemSelectedImage.setVisibility(View.VISIBLE);
//        } else {
//            mItemSelectedImage.setVisibility(View.GONE);
//        }
    }

    void recycle() {
        itemView.setOnClickListener(null);
    }
}
