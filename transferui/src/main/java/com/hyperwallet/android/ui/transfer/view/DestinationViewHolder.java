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
    private final ListTransferDestinationFragment.DestinationItemClickListener
            mDestinationItemClickListener;

    DestinationViewHolder(@NonNull final View itemView,
            @NonNull final ListTransferDestinationFragment.DestinationItemClickListener destinationItemClickListener) {
        super(itemView);
        itemView.setOnClickListener(this);

        mTitle = itemView.findViewById(R.id.title);
        mTransferDestinationCountry = itemView.findViewById(R.id.description_1);
        mTransferDestinationIdentification = itemView.findViewById(R.id.description_2);
        mIcon = itemView.findViewById(R.id.item_selected_image);
        mDestinationItemClickListener = destinationItemClickListener;
    }

    @Override
    public void onClick(View v) {
        int position = getAdapterPosition();
        mDestinationItemClickListener.selectTransferDestination(position);
    }

    void bind(HyperwalletTransferMethod destination, boolean selected) {
        String type = destination.getField(TYPE);
        itemView.setOnClickListener(this);
        mTitle.setText(destination.getField(TYPE));
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
