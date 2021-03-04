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

import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodTypes.PREPAID_CARD;
import static com.hyperwallet.android.ui.common.view.TransferMethodUtils.getStringFontIcon;
import static com.hyperwallet.android.ui.common.view.TransferMethodUtils.getTransferMethodDetail;
import static com.hyperwallet.android.ui.common.view.TransferMethodUtils.getTransferMethodName;

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
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hyperwallet.android.ui.transfer.R;
import com.hyperwallet.android.ui.transfer.TransferSource;
import com.hyperwallet.android.ui.transfer.viewmodel.ListTransferSourceViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ListTransferSourceFragment extends DialogFragment {

    public static final String TAG = "HW:" + ListTransferSourceFragment.class.getSimpleName();
    public static final String ARGUMENT_SELECTED_TRANSFER_SOURCE_TOKEN = "SELECTED_TRANSFER_SOURCE_TOKEN";
    public static final String ARGUMENT_TRANSFER_SOURCE_LIST = "SELECTED_TRANSFER_SOURCE_LIST";
    private ListTransferSourceViewModel mListTransferSourceViewModel;
    private RecyclerView mRecyclerView;
    private ListTransferSourceAdapter mListTransferSourceAdapter;
    private String mActiveTransferSourceToken;
    private List<TransferSource> mTransferSourceList = new ArrayList<>();

    /**
     * Please don't use this constructor this is reserved for Android Core Framework
     *
     * @see #newInstance(String, ArrayList)
     */
    public ListTransferSourceFragment() {
    }

    static ListTransferSourceFragment newInstance(@NonNull final String activeTransferSourceToken,
            @NonNull final ArrayList<TransferSource> transferSourceList) {
        Bundle bundle = new Bundle();
        bundle.putString(ARGUMENT_SELECTED_TRANSFER_SOURCE_TOKEN, activeTransferSourceToken);
        bundle.putParcelableArrayList(ARGUMENT_TRANSFER_SOURCE_LIST, transferSourceList);
        ListTransferSourceFragment fragment = new ListTransferSourceFragment();
        fragment.mActiveTransferSourceToken = activeTransferSourceToken;
        fragment.mTransferSourceList = transferSourceList;
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListTransferSourceViewModel = ViewModelProviders.of(requireActivity()).get(
                ListTransferSourceViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_fragment_list_transfer_source, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = view.findViewById(R.id.transfer_source_selection_toolbar);
        toolbar.setTitle(R.string.mobileTransferFromHeader);
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
        mRecyclerView = view.findViewById(R.id.transfer_source_list);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        mActiveTransferSourceToken = getArguments().getString(ARGUMENT_SELECTED_TRANSFER_SOURCE_TOKEN);
        mListTransferSourceAdapter = new ListTransferSourceAdapter(mListTransferSourceViewModel, mTransferSourceList,
                mActiveTransferSourceToken);
        mRecyclerView.setAdapter(mListTransferSourceAdapter);
    }

    private void hideSoftKey(@NonNull View focusedView) {
        InputMethodManager inputMethodManager = (InputMethodManager) focusedView.getContext().getSystemService(
                Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
    }

    private void onView() {
        requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getActivity().getWindow().setStatusBarColor(
                    ContextCompat.getColor(getContext(), R.color.regularColorPrimary));
            getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    private static class ListTransferSourceAdapter extends RecyclerView.Adapter<TransferSourceViewHolder> {

        private List<TransferSource> mTransferSourceList;
        private final ListTransferSourceViewModel mViewModel;
        private final String mSelectedSource;

        ListTransferSourceAdapter(
                @NonNull final ListTransferSourceViewModel viewModel,
                @NonNull final List<TransferSource> transferSourceList, @NonNull final String selectedSource) {
            mViewModel = viewModel;
            mSelectedSource = selectedSource;
            mTransferSourceList = transferSourceList;
        }

        @NonNull
        @Override
        public TransferSourceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            View itemCurrencyView = layoutInflater.inflate(R.layout.item_transfer_destination, parent, false);

            return new TransferSourceViewHolder(itemCurrencyView, mViewModel);
        }

        @Override
        public void onBindViewHolder(@NonNull TransferSourceViewHolder holder, int position) {
            TransferSource source = mTransferSourceList.get(position);
            holder.bind(source, Objects.equals(source.getToken(), mSelectedSource));
        }

        @Override
        public int getItemCount() {
            return mTransferSourceList.size();
        }

        @Override
        public void onViewRecycled(@NonNull TransferSourceViewHolder holder) {
            holder.recycle();
        }

    }

    private static class TransferSourceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView mTitle;
        private final TextView mIcon;
        private final TextView mTransferSourceIdentification;
        private final ImageView mSelectedIcon;
        private final ListTransferSourceViewModel mViewModel;
        private TransferSource mSource;

        TransferSourceViewHolder(@NonNull final View itemView,
                @NonNull final ListTransferSourceViewModel viewModel) {
            super(itemView);

            mIcon = itemView.findViewById(R.id.icon);
            mTitle = itemView.findViewById(R.id.title);
            mTransferSourceIdentification = itemView.findViewById(R.id.description_1);
            itemView.findViewById(R.id.description_2).setVisibility(View.GONE);
            mSelectedIcon = itemView.findViewById(R.id.item_selected_image);
            mViewModel = viewModel;
        }

        @Override
        public void onClick(View v) {
            mViewModel.selectedTransferSource(mSource);
        }

        void bind(@NonNull final TransferSource source, final boolean selected) {
            mSource = source;
            if (source.getType().equals(PREPAID_CARD)) {
                mTitle.setText(getTransferMethodName(mTitle.getContext(), source.getType()));
                mIcon.setText(getStringFontIcon(mIcon.getContext(), source.getType()));
            } else {
                mTitle.setText(mTitle.getContext().getString(R.string.availableFunds));
                mIcon.setText(mIcon.getContext().getString(R.string.available_funds_font_icon));
            }
            mTransferSourceIdentification.setText(source.getIdentification() == null ? source.getCurrencyCodes()
                    : getTransferMethodDetail(mTransferSourceIdentification.getContext(),
                            source.getIdentification(), source.getType()));

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
