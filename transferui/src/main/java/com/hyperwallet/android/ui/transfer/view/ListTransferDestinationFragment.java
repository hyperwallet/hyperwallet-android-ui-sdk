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

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod;
import com.hyperwallet.android.ui.common.repository.Event;
import com.hyperwallet.android.ui.common.view.OnItemClickListener;
import com.hyperwallet.android.ui.common.view.ToolbarEventListener;
import com.hyperwallet.android.ui.transfer.R;
import com.hyperwallet.android.ui.transfer.viewmodel.ListTransferDestinationViewModel;
import com.hyperwallet.android.ui.transfermethod.repository.TransferMethodRepositoryFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ListTransferDestinationFragment extends DialogFragment implements ToolbarEventListener,
        SelectDestinationItemClickListener {

    public static final String TAG = ListTransferDestinationFragment.class.getSimpleName();
    private static final String ARGUMENT_SELECTED_TRANSFER_DESTINATION = "ARGUMENT_SELECTED_TRANSFER_DESTINATION";

    private ListTransferDestinationAdapter mAdapter;
    private SelectDestinationItemClickListener mSelectDestinationItemClickListener;
    private HyperwalletTransferMethod mSelectedDestination;
    private RecyclerView mRecyclerView;
    private ListTransferDestinationViewModel mListTransferDestinationViewModel;
    private View mProgressBar;

    public static ListTransferDestinationFragment newInstance(HyperwalletTransferMethod selectedDestination) {

        ListTransferDestinationFragment
                listTransferDestinationDialogFragment = new ListTransferDestinationFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelable(ARGUMENT_SELECTED_TRANSFER_DESTINATION,
                selectedDestination);
        listTransferDestinationDialogFragment.setArguments(bundle);

        return listTransferDestinationDialogFragment;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListTransferDestinationViewModel = ViewModelProviders.of(this, new ListTransferDestinationViewModel
                .ListTransferDestinationViewModelFactory(
                TransferMethodRepositoryFactory.getInstance().getTransferMethodRepository())).get(
                ListTransferDestinationViewModel.class);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mSelectDestinationItemClickListener = (SelectDestinationItemClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement "
                    + SelectDestinationItemClickListener.class.getCanonicalName());
        }
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_transfer_destination, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Toolbar toolbar = view.findViewById(R.id.destination_selection_toolbar);
        toolbar.setTitle(R.string.destination);
        toolbar.setNavigationIcon(R.drawable.ic_close_14dp);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKey(v);
                onClose();
                getFragmentManager().popBackStack(TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                dismiss();
            }
        });

        onView();
        mRecyclerView = view.findViewById(R.id.destination_selection_list);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mProgressBar = view.findViewById(R.id.progress_bar_layout);

        registerObservers();
    }

    private void registerObservers() {
        mListTransferDestinationViewModel.getTransferDestinationList().observe(getViewLifecycleOwner(),
                new Observer<List<HyperwalletTransferMethod>>() {
                    @Override
                    public void onChanged(List<HyperwalletTransferMethod> destinations) {
                        mAdapter.replaceData(destinations);
                    }
                });

        mListTransferDestinationViewModel.isLoadingData().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean loading) {
                if (loading) {
                    mProgressBar.setVisibility(View.VISIBLE);
                } else {
                    mProgressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null) {
            mSelectedDestination = savedInstanceState.getParcelable(ARGUMENT_SELECTED_TRANSFER_DESTINATION);
        } else {
            mSelectedDestination = getArguments().getParcelable(ARGUMENT_SELECTED_TRANSFER_DESTINATION);
        }

        mAdapter = new ListTransferDestinationAdapter(mSelectedDestination,
                mSelectDestinationItemClickListener);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelable(ARGUMENT_SELECTED_TRANSFER_DESTINATION, mSelectedDestination);
        super.onSaveInstanceState(outState);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(getActivity());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClose() {
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
        getActivity().getWindow().getDecorView().setSystemUiVisibility(0);
    }

    @Override
    public void onView() {
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getActivity().getWindow().setStatusBarColor(
                    ContextCompat.getColor(getContext(), R.color.regularColorPrimary));
            getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    @Override
    public void hideSoftKey(@NonNull View focusedView) {
        InputMethodManager inputMethodManager = (InputMethodManager) focusedView.getContext().getSystemService(
                Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
    }

    @Override
    public void selectTransferDestination(HyperwalletTransferMethod destination) {
        mListTransferDestinationViewModel.getTransferDestinationSection().setValue(new Event<>(destination));
    }

    private class ListTransferDestinationAdapter extends RecyclerView.Adapter<DestinationViewHolder> implements
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

    private class DestinationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
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
}
