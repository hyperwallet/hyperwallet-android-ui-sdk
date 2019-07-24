package com.hyperwallet.android.ui.transfer.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod;
import com.hyperwallet.android.ui.common.view.ToolbarEventListener;
import com.hyperwallet.android.ui.transfer.R;

import java.util.List;

public class ListTransferDestinationAdapter extends RecyclerView.Adapter<DestinationViewHolder> implements
        ListTransferDestinationFragment.DestinationItemClickListener {

    private List<HyperwalletTransferMethod> mDestinations;
    private ListTransferDestinationFragment.DestinationItemClickListener mDestinationItemClickListener;
    private String mSelectedCurrencyName;
    private ToolbarEventListener mToolbarEventListener;

    ListTransferDestinationAdapter(List<String> mDestinations, final String selectedCurrencyName,
            final ListTransferDestinationFragment.DestinationItemClickListener destinationItemClickListener,
            final ToolbarEventListener toolbarEventListener) {
        mSelectedCurrencyName = selectedCurrencyName;
        mDestinationItemClickListener = destinationItemClickListener;
        mToolbarEventListener = toolbarEventListener;
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
        String currencyName = mDestinations.get(position);
        holder.bind(currencyName);
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
    public void onDestinationItemClicked(int position) {

    }
}
