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

import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.TOKEN;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.TRANSFER_METHOD_COUNTRY;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.TYPE;
import static com.hyperwallet.android.ui.common.view.TransferMethodUtils.getStringFontIcon;
import static com.hyperwallet.android.ui.common.view.TransferMethodUtils.getStringResourceByName;
import static com.hyperwallet.android.ui.common.view.TransferMethodUtils.getTransferMethodDetail;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hyperwallet.android.model.transfermethod.TransferMethod;
import com.hyperwallet.android.ui.transfer.R;
import com.hyperwallet.android.ui.transfer.viewmodel.ListTransferDestinationViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ListTransferDestinationFragment extends DialogFragment {

    public static final String TAG = "HW:" + ListTransferDestinationFragment.class.getSimpleName();
    public static final String ARGUMENT_SELECTED_TRANSFER_TOKEN = "SELECTED_TRANSFER_TOKEN";
    public static final String ARGUMENT_IS_SOURCE_PREPAID_CARD = "IS_SOURCE_PREPAID_CARD";
    private ListTransferDestinationViewModel mListTransferDestinationViewModel;
    private RecyclerView mRecyclerView;
    private ListTransferDestinationAdapter mListTransferDestinationAdapter;
    private View mProgressBar;
    private String mActiveTransferToken;
    private boolean mIsSourcePrepaidCard;

    /**
     * Please don't use this constructor this is reserved for Android Core Framework
     *
     * @see #newInstance(String, boolean)
     */
    public ListTransferDestinationFragment() {
    }

    static ListTransferDestinationFragment newInstance(@NonNull final String activeTransferToken,
            final boolean isSourcePrepaidCard) {
        Bundle bundle = new Bundle();
        bundle.putString(ARGUMENT_SELECTED_TRANSFER_TOKEN, activeTransferToken);
        bundle.putBoolean(ARGUMENT_IS_SOURCE_PREPAID_CARD, isSourcePrepaidCard);
        ListTransferDestinationFragment fragment = new ListTransferDestinationFragment();
        fragment.mActiveTransferToken = activeTransferToken;
        fragment.mIsSourcePrepaidCard = isSourcePrepaidCard;
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListTransferDestinationViewModel = ViewModelProviders.of(requireActivity()).get(
                ListTransferDestinationViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_fragment_list_transfer_destination, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = view.findViewById(R.id.transfer_destination_selection_toolbar);
        toolbar.setTitle(R.string.mobileTransferMethodsHeader);
        toolbar.setNavigationIcon(R.drawable.ic_close_14dp);

        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKey(v);
                getFragmentManager().popBackStack(TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                dismiss();
                requireActivity().finish();
            }
        });

        onView();
        mProgressBar = view.findViewById(R.id.progress_bar);
        mRecyclerView = view.findViewById(R.id.transfer_destination_list);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        registerObservers();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mListTransferDestinationViewModel.init();
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        mActiveTransferToken = getArguments().getString(ARGUMENT_SELECTED_TRANSFER_TOKEN);
        mIsSourcePrepaidCard = getArguments().getBoolean(ARGUMENT_IS_SOURCE_PREPAID_CARD);
        mListTransferDestinationViewModel.setIsSourcePrepaidCard(mIsSourcePrepaidCard);
        mListTransferDestinationAdapter = new ListTransferDestinationAdapter(mActiveTransferToken,
                mListTransferDestinationViewModel);
        mRecyclerView.setAdapter(mListTransferDestinationAdapter);
    }

    private void registerObservers() {
        mListTransferDestinationViewModel.getTransferDestinationList().observe(getViewLifecycleOwner(),
                new Observer<List<TransferMethod>>() {
                    @Override
                    public void onChanged(List<TransferMethod> transferMethods) {
                        if (mListTransferDestinationAdapter != null) {
                            mListTransferDestinationAdapter.replaceData(transferMethods);
                        }
                    }
                });

        mListTransferDestinationViewModel.isLoading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
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

    private void hideSoftKey(@NonNull View focusedView) {
        InputMethodManager inputMethodManager = (InputMethodManager) focusedView.getContext().getSystemService(
                Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
    }

    private void onView() {
       //do nothing
    }

    private static class ListTransferDestinationAdapter extends RecyclerView.Adapter<TransferDestinationViewHolder> {

        private final List<TransferMethod> mTransferDestinations = new ArrayList<>();
        private final ListTransferDestinationViewModel mViewModel;
        private final String mSelectedDestination;

        ListTransferDestinationAdapter(@NonNull final String selectedDestination,
                @NonNull final ListTransferDestinationViewModel viewModel) {
            mSelectedDestination = selectedDestination;
            mViewModel = viewModel;
        }

        @NonNull
        @Override
        public TransferDestinationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            View itemCurrencyView = layoutInflater.inflate(R.layout.item_transfer_destination, parent, false);

            return new TransferDestinationViewHolder(itemCurrencyView, mViewModel);
        }

        @Override
        public void onBindViewHolder(@NonNull TransferDestinationViewHolder holder, int position) {
            TransferMethod destination = mTransferDestinations.get(position);
            holder.bind(destination, Objects.equals(destination.getField(TOKEN), mSelectedDestination));
        }

        @Override
        public int getItemCount() {
            return mTransferDestinations.size();
        }

        @Override
        public void onViewRecycled(@NonNull TransferDestinationViewHolder holder) {
            holder.recycle();
        }

        void replaceData(@NonNull final List<TransferMethod> destinations) {
            mTransferDestinations.clear();
            mTransferDestinations.addAll(destinations);
            notifyDataSetChanged();
        }
    }

    private static class TransferDestinationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView mTitle;
        private final TextView mIcon;
        private final TextView mTransferDestinationCountry;
        private final TextView mTransferDestinationIdentification;
        private final ImageView mSelectedIcon;
        private final ListTransferDestinationViewModel mViewModel;
        private TransferMethod mTransferMethod;

        TransferDestinationViewHolder(@NonNull final View itemView,
                @NonNull final ListTransferDestinationViewModel viewModel) {
            super(itemView);

            mIcon = itemView.findViewById(R.id.icon);
            mTitle = itemView.findViewById(R.id.title);
            mTransferDestinationCountry = itemView.findViewById(R.id.description_1);
            mTransferDestinationIdentification = itemView.findViewById(R.id.description_2);
            mSelectedIcon = itemView.findViewById(R.id.item_selected_image);
            mViewModel = viewModel;
        }

        @Override
        public void onClick(View v) {
            mViewModel.selectedTransferDestination(mTransferMethod);
        }

        void bind(@NonNull final TransferMethod destination, final boolean selected) {
            mTransferMethod = destination;
            String type = destination.getField(TYPE);
            Locale locale = new Locale.Builder().setRegion(destination.getField(TRANSFER_METHOD_COUNTRY)).build();
            String transferId = getTransferMethodDetail(mTitle.getContext(), destination, type);

            mTitle.setText(getStringResourceByName(mTitle.getContext(), type));
            mIcon.setText(getStringFontIcon(mIcon.getContext(), type));
            mTransferDestinationCountry.setText(locale.getDisplayName());
            mTransferDestinationIdentification.setText(transferId);

            if (selected) {
                mSelectedIcon.setVisibility(View.VISIBLE);
            } else {
                mSelectedIcon.setVisibility(View.GONE);
            }
            itemView.setOnClickListener(this);
        }

        void recycle() {
            itemView.setOnClickListener(null);
        }
    }
}
