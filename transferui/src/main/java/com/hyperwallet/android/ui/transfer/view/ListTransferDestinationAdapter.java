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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod;
import com.hyperwallet.android.ui.transfer.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ListTransferDestinationAdapter extends RecyclerView.Adapter<DestinationViewHolder> implements
        OnItemClickListener {

    private final List<HyperwalletTransferMethod> mDestinations = new ArrayList<>();
    private final SelectDestinationItemClickListener
            mSelectDestinationItemClickListener;
    private final HyperwalletTransferMethod mSelectedDestination;

    ListTransferDestinationAdapter(
            final HyperwalletTransferMethod selectedDestination,
            @NonNull final SelectDestinationItemClickListener selectDestinationItemClickListener) {
        mSelectedDestination = selectedDestination;
        mSelectDestinationItemClickListener = selectDestinationItemClickListener;
    }

    @NonNull
    @Override
    public DestinationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View itemCurrencyView = layoutInflater.inflate(R.layout.item_destination, parent, false);

        return new DestinationViewHolder(itemCurrencyView, this);
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

    public void replaceData(List<HyperwalletTransferMethod> destinations) {
        mDestinations.addAll(destinations);
        notifyDataSetChanged();
    }

    @Override
    public void onItemClick(int position) {
        mSelectDestinationItemClickListener.selectTransferDestination(mDestinations.get(position));
    }
}
