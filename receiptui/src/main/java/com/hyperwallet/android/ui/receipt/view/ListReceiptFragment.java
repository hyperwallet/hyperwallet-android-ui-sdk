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
package com.hyperwallet.android.ui.receipt.view;

import static android.text.format.DateUtils.FORMAT_NO_MONTH_DAY;
import static android.text.format.DateUtils.FORMAT_SHOW_DATE;
import static android.text.format.DateUtils.FORMAT_SHOW_YEAR;
import static android.text.format.DateUtils.formatDateTime;

import static com.hyperwallet.android.model.receipt.Receipt.Entries.CREDIT;
import static com.hyperwallet.android.model.receipt.Receipt.Entries.DEBIT;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hyperwallet.android.model.receipt.Receipt;
import com.hyperwallet.android.ui.common.util.DateUtils;
import com.hyperwallet.android.ui.common.view.OneClickListener;
import com.hyperwallet.android.ui.receipt.R;
import com.hyperwallet.android.ui.receipt.viewmodel.ReceiptViewModel;

import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class ListReceiptFragment extends Fragment {

    private static final String SHOULD_SHOW_NO_TRANSACTION_PLACEHOLDER = "SHOULD_SHOW_NO_TRANSACTION_PLACEHOLDER";

    private ListReceiptAdapter mListReceiptAdapter;
    private RecyclerView mListReceiptsView;
    private ReceiptViewModel mReceiptViewModel;
    private View mProgressBar;
    private View mEmptyTransactionPlaceholder;
    private Boolean mShouldShowNoTransactionPlaceholder = Boolean.TRUE;

    /**
     * Please don't use this constructor this is reserved for Android Core Framework
     *
     * @see #newInstance()
     */
    public ListReceiptFragment() {
        setRetainInstance(true);
    }

    static ListReceiptFragment newInstance() {
        ListReceiptFragment fragment = new ListReceiptFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mReceiptViewModel = ViewModelProviders.of(requireActivity()).get(
                ReceiptViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_receipt, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View transactionHeaderView = view.findViewById(R.id.transactions_header);
        if (getActivity() instanceof ListPrepaidCardReceiptActivity
                || getActivity() instanceof ListUserReceiptActivity) {
            transactionHeaderView.setVisibility(View.GONE);
        }

        mEmptyTransactionPlaceholder = view.findViewById(R.id.empty_transaction_list_view);
        mListReceiptsView = view.findViewById(R.id.list_receipts);
        mProgressBar = view.findViewById(R.id.list_receipt_progress_bar);
        mListReceiptAdapter = new ListReceiptAdapter(mReceiptViewModel, new ListReceiptItemDiffCallback());
        mListReceiptsView.setHasFixedSize(true);
        mListReceiptsView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mListReceiptsView.setAdapter(mListReceiptAdapter);
        registerObservers();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mReceiptViewModel.init();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean(SHOULD_SHOW_NO_TRANSACTION_PLACEHOLDER, mShouldShowNoTransactionPlaceholder);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mShouldShowNoTransactionPlaceholder = savedInstanceState.getBoolean(SHOULD_SHOW_NO_TRANSACTION_PLACEHOLDER);
        }
        super.onViewStateRestored(savedInstanceState);
    }

    private void registerObservers() {
        mReceiptViewModel.getReceiptList().observe(getViewLifecycleOwner(), new Observer<PagedList<Receipt>>() {
            @Override
            public void onChanged(final PagedList<Receipt> receipts) {
                mListReceiptAdapter.submitList(receipts);
                receipts.addWeakCallback(null, new PagedList.Callback() {
                    @Override
                    public void onChanged(int position, int count) {
                    }

                    @Override
                    public void onInserted(int position, int count) {
                        if (receipts.size() > 0) {
                            mShouldShowNoTransactionPlaceholder = Boolean.FALSE;
                        } else {
                            mShouldShowNoTransactionPlaceholder = Boolean.TRUE;
                        }
                    }

                    @Override
                    public void onRemoved(int position, int count) {
                    }
                });
            }
        });

        mReceiptViewModel.isLoadingData().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean loading) {
                if (loading) {
                    mEmptyTransactionPlaceholder.setVisibility(View.GONE);
                    mProgressBar.setVisibility(View.VISIBLE);
                } else {
                    mProgressBar.setVisibility(View.GONE);
                    if (mShouldShowNoTransactionPlaceholder) {
                        mEmptyTransactionPlaceholder.setVisibility(View.VISIBLE);
                        mListReceiptsView.setVisibility(View.GONE);
                    } else {
                        mEmptyTransactionPlaceholder.setVisibility(View.GONE);
                        mListReceiptsView.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    void retry() {
        mReceiptViewModel.retry();
    }

    private static class ListReceiptItemDiffCallback extends DiffUtil.ItemCallback<Receipt> {

        @Override
        public boolean areItemsTheSame(@NonNull final Receipt oldItem, @NonNull final Receipt newItem) {
            return oldItem.hashCode() == newItem.hashCode()
                    && Objects.equals(oldItem, newItem);
        }

        @Override
        public boolean areContentsTheSame(@NonNull final Receipt oldItem, @NonNull final Receipt newItem) {
            return oldItem.hashCode() == newItem.hashCode()
                    && Objects.equals(oldItem, newItem);
        }
    }

    private static class ListReceiptAdapter
            extends PagedListAdapter<Receipt, ListReceiptAdapter.ReceiptViewHolder> {

        static final String AMOUNT_FORMAT = "###0.00";
        private static final int HEADER_VIEW_TYPE = 1;
        private static final int DATA_VIEW_TYPE = 0;
        private final ReceiptViewModel mReceiptViewModel;

        ListReceiptAdapter(@NonNull final ReceiptViewModel receiptViewModel,
                @NonNull final DiffUtil.ItemCallback<Receipt> diffCallback) {
            super(diffCallback);
            mReceiptViewModel = receiptViewModel;
        }

        @Override
        public int getItemViewType(final int position) {
            if (position != 0) {
                Receipt previous = getItem(position - 1);
                Receipt current = getItem(position);
                if (isDataViewType(previous, current)) {
                    return DATA_VIEW_TYPE;
                }
            }
            return HEADER_VIEW_TYPE;
        }

        boolean isDataViewType(@NonNull final Receipt previous, @NonNull final Receipt current) {
            Calendar prev = Calendar.getInstance();
            prev.setTime(DateUtils.fromDateTimeString(previous.getCreatedOn()));
            Calendar curr = Calendar.getInstance();
            curr.setTime(DateUtils.fromDateTimeString(current.getCreatedOn()));

            return prev.get(Calendar.MONTH) == curr.get(Calendar.MONTH)
                    && prev.get(Calendar.YEAR) == curr.get(Calendar.YEAR);
        }

        @NonNull
        @Override
        public ReceiptViewHolder onCreateViewHolder(final @NonNull ViewGroup viewGroup, int viewType) {
            LayoutInflater layout = LayoutInflater.from(viewGroup.getContext());

            if (viewType == HEADER_VIEW_TYPE) {
                View headerView = layout.inflate(R.layout.item_receipt_with_header, viewGroup, false);
                return new ReceiptViewHolderWithHeader(mReceiptViewModel, headerView);
            }
            View dataView = layout.inflate(R.layout.item_receipt, viewGroup, false);
            return new ReceiptViewHolder(mReceiptViewModel, dataView);
        }

        @Override
        public void onBindViewHolder(@NonNull final ReceiptViewHolder holder, final int position) {
            final Receipt receipt = getItem(position);
            if (receipt != null) {
                holder.bind(receipt);
            }
        }

        class ReceiptViewHolder extends RecyclerView.ViewHolder {

            private ReceiptViewModel mListUserReceiptViewModel;
            private View mView;

            ReceiptViewHolder(@NonNull final ReceiptViewModel receiptViewModel,
                    @NonNull final View item) {
                super(item);
                mView = item.findViewById(R.id.receipt_item);
                mListUserReceiptViewModel = receiptViewModel;
            }

            void bind(@NonNull final Receipt receipt) {
                mView.setOnClickListener(new OneClickListener() {
                    @Override
                    public void onOneClick(View v) {
                        mListUserReceiptViewModel.setDetailNavigation(receipt);
                    }
                });

                // By design decision from hereon, this code is also repeated in ReceiptDetailFragment
                TextView transactionTypeIcon = itemView.findViewById(R.id.transaction_type_icon);
                TextView transactionTitle = itemView.findViewById(R.id.transaction_title);
                TextView transactionDate = itemView.findViewById(R.id.transaction_date);
                TextView transactionAmount = itemView.findViewById(R.id.transaction_amount);
                TextView transactionCurrency = itemView.findViewById(R.id.transaction_currency);

                String currencySymbol = Currency.getInstance(receipt.getCurrency()).getSymbol(Locale.getDefault());

                if (CREDIT.equals(receipt.getEntry())) {
                    transactionAmount.setTextColor(transactionAmount.getContext()
                            .getResources().getColor(R.color.positiveColor));
                    transactionAmount.setText(transactionAmount.getContext()
                            .getString(R.string.credit_sign, currencySymbol, receipt.getAmount()));
                    transactionTypeIcon.setTextColor(transactionTypeIcon.getContext()
                            .getResources().getColor(R.color.positiveColor));
                    transactionTypeIcon.setText(transactionTypeIcon.getContext().getText(R.string.credit));
                } else if (DEBIT.equals(receipt.getEntry())) {
                    transactionAmount.setTextColor(transactionAmount.getContext()
                            .getResources().getColor(R.color.negativeColor));
                    transactionTypeIcon.setTextColor(transactionTypeIcon.getContext()
                            .getResources().getColor(R.color.negativeColor));
                    transactionAmount.setText(transactionAmount.getContext()
                            .getString(R.string.debit_sign, currencySymbol, receipt.getAmount()));
                    transactionTypeIcon.setText(transactionTypeIcon.getContext().getText(R.string.debit));
                }

                transactionCurrency.setText(receipt.getCurrency());
                transactionTitle.setText(getTransactionTitle(receipt.getType(), transactionTitle.getContext()));
                Date date = DateUtils.fromDateTimeString(receipt.getCreatedOn());
                transactionDate.setText(formatDateTime(itemView.getContext(), date.getTime(),
                        FORMAT_SHOW_DATE | FORMAT_SHOW_YEAR));
            }

            String getTransactionTitle(@NonNull final String receiptType, @NonNull final Context context) {
                String showTitle = context.getResources().getString(R.string.unknown_type);
                int resourceId = context.getResources().getIdentifier(receiptType.toLowerCase(Locale.ROOT), "string",
                        context.getPackageName());
                if (resourceId != 0) {
                    showTitle = context.getResources().getString(resourceId);
                }

                return showTitle;
            }
        }

        class ReceiptViewHolderWithHeader extends ReceiptViewHolder {

            private final TextView mTransactionHeaderText;

            ReceiptViewHolderWithHeader(@NonNull final ReceiptViewModel receiptViewModel, @NonNull final View item) {
                super(receiptViewModel, item);
                mTransactionHeaderText = item.findViewById(R.id.item_date_header_title);
            }

            @Override
            void bind(@NonNull final Receipt receipt) {
                super.bind(receipt);
                Date date = DateUtils.fromDateTimeString(receipt.getCreatedOn());
                mTransactionHeaderText.setText(formatDateTime(mTransactionHeaderText.getContext(), date.getTime(),
                        FORMAT_SHOW_DATE | FORMAT_SHOW_YEAR | FORMAT_NO_MONTH_DAY));
            }
        }
    }
}
