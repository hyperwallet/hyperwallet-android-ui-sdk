package com.hyperwallet.android.ui.transfer.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod;
import com.hyperwallet.android.ui.transfer.R;

import java.util.List;
import java.util.Objects;

public class ListTransferDestinationAdapter extends RecyclerView.Adapter<DestinationViewHolder> implements
        ListTransferDestinationFragment.DestinationItemClickListener {

    private List<HyperwalletTransferMethod> mDestinations;
    private ListTransferDestinationFragment.DestinationItemClickListener mDestinationItemClickListener;
    private HyperwalletTransferMethod mSelectedDestination;

    ListTransferDestinationAdapter(@NonNull List<HyperwalletTransferMethod> destinations,
            final HyperwalletTransferMethod selectedDestination,
            final ListTransferDestinationFragment.DestinationItemClickListener destinationItemClickListener) {
        mDestinations = destinations;
        mSelectedDestination = selectedDestination;
        mDestinationItemClickListener = destinationItemClickListener;
    }

    @NonNull
    @Override
    public DestinationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View itemCurrencyView = layoutInflater.inflate(R.layout.item_destination, parent, false);

        return new DestinationViewHolder(itemCurrencyView, mDestinationItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull DestinationViewHolder holder, int position) {
        HyperwalletTransferMethod destination = mDestinations.get(position);
        holder.bind(destination, Objects.equals(destination, mSelectedDestination));
    }

    @Override
    public int getItemCount() {
        return mDestinations.size();
    }

    @Override
    public void onViewRecycled(@NonNull DestinationViewHolder holder) {
        holder.recycle();
    }

    @Override
    public void selectTransferDestination(int position) {

    }

    public void replaceData(List<HyperwalletTransferMethod> destinations) {
        mDestinations.addAll(destinations);
        notifyDataSetChanged();
    }
}
