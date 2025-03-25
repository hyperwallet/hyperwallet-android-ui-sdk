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
import android.content.Intent;
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

import com.hyperwallet.android.model.Error;
import com.hyperwallet.android.model.Errors;
import com.hyperwallet.android.model.receipt.Receipt;
import com.hyperwallet.android.ui.common.repository.Event;
import com.hyperwallet.android.ui.common.util.CurrencyParser;
import com.hyperwallet.android.ui.common.util.DateUtils;
import com.hyperwallet.android.ui.common.view.OneClickListener;
import com.hyperwallet.android.ui.receipt.R;
import com.hyperwallet.android.ui.receipt.repository.PrepaidCardReceiptRepositoryImpl;
import com.hyperwallet.android.ui.receipt.repository.UserReceiptRepositoryImpl;
import com.hyperwallet.android.ui.receipt.viewmodel.ListReceiptsViewModel;
import com.hyperwallet.android.ui.receipt.viewmodel.ListUserReceiptsViewModel;

import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ListReceiptsFragment extends Fragment {

    private static final String SHOULD_SHOW_NO_TRANSACTION_PLACEHOLDER = "SHOULD_SHOW_NO_TRANSACTION_PLACEHOLDER";
    private static final String ARGUMENT_PREPAID_CARD_TOKEN = "ARGUMENT_PREPAID_CARD_TOKEN";

    private String mPrepaidCardToken = "";
    private ListReceiptsAdapter mListReceiptsAdapter;
    private RecyclerView mListReceiptsView;
    private ListReceiptsViewModel mReceiptViewModel;
    private View mProgressBar;
    private View mEmptyTransactionPlaceholder;
    private TextView mEmptyTransactionTextView;
    private Boolean mShouldShowNoTransactionPlaceholder = Boolean.FALSE;
    private Boolean mLockScreenToPortrait = false;
    private ListReceiptsFragmentCallback callback;

    static ListReceiptsFragment newInstance(boolean lockScreenToPortrait) {
        ListReceiptsFragment listReceiptsFragment = new ListReceiptsFragment();
        listReceiptsFragment.mLockScreenToPortrait = lockScreenToPortrait;
        return listReceiptsFragment;
    }

    static ListReceiptsFragment newInstance(String prepaidCardToken, boolean lockScreenToPortrait) {
        ListReceiptsFragment fragment = new ListReceiptsFragment();
        Bundle internalState = new Bundle();
        internalState.putString(ARGUMENT_PREPAID_CARD_TOKEN, prepaidCardToken);
        fragment.setArguments(internalState);
        fragment.mPrepaidCardToken = prepaidCardToken;
        fragment.mLockScreenToPortrait = lockScreenToPortrait;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mReceiptViewModel = ViewModelProviders.of(this,
                new ListReceiptsViewModel.ListReceiptsViewModelFactory(mPrepaidCardToken,
                        new UserReceiptRepositoryImpl(),
                        new PrepaidCardReceiptRepositoryImpl(mPrepaidCardToken)))
                .get(ListReceiptsViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_receipts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mEmptyTransactionPlaceholder = view.findViewById(R.id.empty_transaction_list_view);
        mEmptyTransactionTextView = view.findViewById(R.id.text_noTransaction);
        mListReceiptsView = view.findViewById(R.id.list_receipts);
        mProgressBar = view.findViewById(R.id.list_receipt_progress_bar);
        mListReceiptsAdapter = new ListReceiptsAdapter(mReceiptViewModel, new ListReceiptsItemDiffCallback());
        mListReceiptsView.setHasFixedSize(true);
        mListReceiptsView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mListReceiptsView.setAdapter(mListReceiptsAdapter);
        registerObservers();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        callback = (ListReceiptsFragmentCallback) context;
        super.onAttach(context);
    }

    public interface ListReceiptsFragmentCallback {
        void onHandleError(List<Error> errors);
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
        outState.putString(ARGUMENT_PREPAID_CARD_TOKEN, mPrepaidCardToken);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mShouldShowNoTransactionPlaceholder = savedInstanceState.getBoolean(SHOULD_SHOW_NO_TRANSACTION_PLACEHOLDER);
        }
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            mPrepaidCardToken = savedInstanceState.getString(ARGUMENT_PREPAID_CARD_TOKEN);
        } else if (getArguments() != null) {
            mPrepaidCardToken = getArguments().getString(ARGUMENT_PREPAID_CARD_TOKEN);
        }
    }

    private void registerObservers() {
        mReceiptViewModel.receipts().observe(getViewLifecycleOwner(), new Observer<PagedList<Receipt>>() {
            @Override
            public void onChanged(final PagedList<Receipt> receipts) {
                mListReceiptsAdapter.submitList(receipts);
            }
        });

        mReceiptViewModel.isLoading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean loading) {
                if (loading) {
                    mEmptyTransactionPlaceholder.setVisibility(View.GONE);
                    mProgressBar.setVisibility(View.VISIBLE);
                } else {
                    mProgressBar.setVisibility(View.GONE);
                    mShouldShowNoTransactionPlaceholder = (Objects.requireNonNull(mReceiptViewModel.receipts().getValue()).size() > 0) ? Boolean.FALSE : Boolean.TRUE;
                    if (mShouldShowNoTransactionPlaceholder) {
                        if (mReceiptViewModel instanceof ListUserReceiptsViewModel) {
                            mEmptyTransactionTextView.setText(R.string.mobileNoTransactionsUser);
                        } else {
                            mEmptyTransactionTextView.setText(R.string.mobileNoTransactionsPrepaidCard);
                        }
                        mEmptyTransactionPlaceholder.setVisibility(View.VISIBLE);
                        mListReceiptsView.setVisibility(View.GONE);
                    } else {
                        mEmptyTransactionPlaceholder.setVisibility(View.GONE);
                        mListReceiptsView.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        mReceiptViewModel.errors().observe(getViewLifecycleOwner(), new Observer<Event<Errors>>() {
            @Override
            public void onChanged(Event<Errors> errorsEvent) {
                if (errorsEvent != null && !errorsEvent.isContentConsumed()) {
                    callback.onHandleError(errorsEvent.getContent().getErrors());
                }
            }
        });

        mReceiptViewModel.getDetailNavigation().observe(getViewLifecycleOwner(), new Observer<Event<Receipt>>() {
            @Override
            public void onChanged(@NonNull final Event<Receipt> event) {
                navigate(event);
            }
        });
    }

    private void navigate(@NonNull final Event<Receipt> event) {
        if (!event.isContentConsumed()) {
            Intent intent = new Intent(getActivity(), ReceiptDetailActivity.class);

            intent.putExtra(ReceiptDetailActivity.EXTRA_RECEIPT, event.getContent());
            intent.putExtra(ReceiptDetailActivity.EXTRA_LOCK_SCREEN_ORIENTATION_TO_PORTRAIT, mLockScreenToPortrait);
            startActivity(intent);
        }
    }

    void retry() {
        mReceiptViewModel.retry();
    }

    private static class ListReceiptsItemDiffCallback extends DiffUtil.ItemCallback<Receipt> {

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

    private static class ListReceiptsAdapter
            extends PagedListAdapter<Receipt, ListReceiptsAdapter.ReceiptItemViewHolder> {

        private static final int HEADER_VIEW_TYPE = 1;
        private static final int DATA_VIEW_TYPE = 0;
        private final ListReceiptsViewModel mReceiptViewModel;

        ListReceiptsAdapter(@NonNull final ListReceiptsViewModel receiptViewModel,
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
        public ReceiptItemViewHolder onCreateViewHolder(final @NonNull ViewGroup viewGroup, int viewType) {
            LayoutInflater layout = LayoutInflater.from(viewGroup.getContext());

            if (viewType == HEADER_VIEW_TYPE) {
                View headerView = layout.inflate(R.layout.item_receipt_with_header, viewGroup, false);
                return new ReceiptItemViewHolderWithHeader(mReceiptViewModel, headerView);
            }
            View dataView = layout.inflate(R.layout.item_receipt, viewGroup, false);
            return new ReceiptItemViewHolder(dataView);
        }

        @Override
        public void onBindViewHolder(@NonNull final ReceiptItemViewHolder holder, final int position) {
            final Receipt receipt = getItem(position);
            if (receipt != null) {
                holder.bind(receipt);
            }
        }

        class ReceiptItemViewHolder extends RecyclerView.ViewHolder {

            ReceiptItemViewHolder(@NonNull final View item) {
                super(item);
            }

            void bind(@NonNull final Receipt receipt) {
                itemView.setOnClickListener(new OneClickListener() {
                    @Override
                    public void onOneClick(View v) {
                        mReceiptViewModel.setDetailNavigation(receipt);
                    }
                });

                TextView transactionTypeIcon = itemView.findViewById(R.id.transaction_type_icon);
                TextView transactionTitle = itemView.findViewById(R.id.transaction_title);
                TextView transactionDate = itemView.findViewById(R.id.transaction_date);
                TextView transactionAmount = itemView.findViewById(R.id.transaction_amount);
                TextView transactionCurrency = itemView.findViewById(R.id.transaction_currency);

                String currencySymbol = Currency.getInstance(receipt.getCurrency()).getSymbol(Locale.getDefault());

                if (CREDIT.equals(receipt.getEntry())) {
                    transactionAmount.setTextColor(transactionAmount.getContext()
                            .getResources().getColor(R.color.positiveColor));
                    transactionAmount.setText(
                            CurrencyParser.getInstance(itemView.getContext()).formatCurrencyWithSymbol(receipt.getCurrency(),
                                    receipt.getAmount()));
                    transactionTypeIcon.setTextColor(transactionTypeIcon.getContext()
                            .getResources().getColor(R.color.positiveColor));
                    transactionTypeIcon.setText(transactionTypeIcon.getContext().getText(R.string.credit));
                } else if (DEBIT.equals(receipt.getEntry())) {
                    transactionAmount.setTextColor(transactionAmount.getContext()
                            .getResources().getColor(R.color.negativeColor));
                    transactionTypeIcon.setTextColor(transactionTypeIcon.getContext()
                            .getResources().getColor(R.color.negativeColor));
                    transactionAmount.setText(transactionAmount.getContext().getString(R.string.debit_sign,
                            CurrencyParser.getInstance(itemView.getContext()).formatCurrencyWithSymbol(receipt.getCurrency(),
                                    receipt.getAmount())));
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

        class ReceiptItemViewHolderWithHeader extends ReceiptItemViewHolder {

            private final TextView mTransactionHeaderText;

            ReceiptItemViewHolderWithHeader(@NonNull final ListReceiptsViewModel receiptViewModel,
                    @NonNull final View item) {
                super(item);
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
