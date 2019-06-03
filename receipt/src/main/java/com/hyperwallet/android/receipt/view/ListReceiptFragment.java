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
package com.hyperwallet.android.receipt.view;

import static com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod.TransferMethodFields.CREATED_ON;
import static com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod.TransferMethodFields.TOKEN;
import static com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod.TransferMethodFields.TRANSFER_METHOD_CURRENCY;

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

import com.hyperwallet.android.common.util.DateUtility;
import com.hyperwallet.android.model.HyperwalletError;
import com.hyperwallet.android.model.HyperwalletErrors;
import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod;
import com.hyperwallet.android.receipt.R;
import com.hyperwallet.android.receipt.viewmodel.ListReceiptViewModel;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

//TODO change HyperwalletTransferMethod to HyperwalletReceipts whenever core is available
public class ListReceiptFragment extends Fragment {

    private static final String HEADER_DATE_FORMAT = "MMMM yyyy";
    private static final String CAPTION_DATE_FORMAT = "MMMM dd, yyyy";
    private View mProgressBar;
    private RecyclerView mListReceiptsView;
    private ListReceiptViewModel mListReceiptViewModel;
    private ListReceiptAdapter mListReceiptAdapter;
    private OnLoadReceiptErrorCallback mOnLoadReceiptErrorCallback;

    /**
     * Please don't use this constructor this is reserved for Android Core Framework
     *
     * @see {@link ListReceiptFragment#newInstance()} instead.
     */
    public ListReceiptFragment() {
        setRetainInstance(true);
    }

    static ListReceiptFragment newInstance() {
        ListReceiptFragment fragment = new ListReceiptFragment();
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mOnLoadReceiptErrorCallback = (OnLoadReceiptErrorCallback) requireContext();
        } catch (ClassCastException e) {
            throw new ClassCastException(requireActivity().toString() + " must implement "
                    + OnLoadReceiptErrorCallback.class.getCanonicalName());
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListReceiptViewModel = ViewModelProviders.of(requireActivity()).get(
                ListReceiptViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list_receipt_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mProgressBar = view.findViewById(R.id.list_receipt_progress_bar);
        mListReceiptAdapter = new ListReceiptAdapter(new ListReceiptItemDiffCallback());
        mListReceiptsView = view.findViewById(R.id.list_receipts);
        mListReceiptsView.setHasFixedSize(true);
        mListReceiptsView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mListReceiptsView.setAdapter(mListReceiptAdapter);
        registerObservers();
    }

    private void registerObservers() {
        mListReceiptViewModel.getReceiptList().observe(this, new Observer<PagedList<HyperwalletTransferMethod>>() {
            @Override
            public void onChanged(PagedList<HyperwalletTransferMethod> transferMethods) {
                mListReceiptAdapter.submitList(transferMethods);
            }
        });

        mListReceiptViewModel.getReceiptErrors().observe(this, new Observer<HyperwalletErrors>() {
            @Override
            public void onChanged(HyperwalletErrors hyperwalletErrors) {
                if (hyperwalletErrors != null) { // we need to check this since we are posting null
                    mOnLoadReceiptErrorCallback.showErrorOnLoadReceipt(hyperwalletErrors.getErrors());
                }
            }
        });

        mListReceiptViewModel.isLoadingData().observe(this, new Observer<Boolean>() {
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

    void retry() {
        mListReceiptViewModel.retryLoadReceipts();
    }

    interface OnLoadReceiptErrorCallback {

        void showErrorOnLoadReceipt(@NonNull final List<HyperwalletError> errors);
    }

    private static class ListReceiptItemDiffCallback extends DiffUtil.ItemCallback<HyperwalletTransferMethod> {

        @Override
        public boolean areItemsTheSame(@NonNull HyperwalletTransferMethod oldItem,
                @NonNull HyperwalletTransferMethod newItem) {
            //TODO Receipts: {journalId, type, entry}
            return oldItem.getField(TOKEN).equals(newItem.getField(TOKEN));
        }

        @Override
        public boolean areContentsTheSame(@NonNull HyperwalletTransferMethod oldItem,
                @NonNull HyperwalletTransferMethod newItem) {
            //TODO check if contents are the same in this case check each receipt fields against each other
            // Receipts: {journalId, type, entry}
            return oldItem.getField(TOKEN).equals(newItem.getField(TOKEN));
        }
    }

    private static class ListReceiptAdapter
            extends PagedListAdapter<HyperwalletTransferMethod, ListReceiptAdapter.ReceiptViewHolder> {

        static final int HEADER_VIEW_TYPE = 1;
        static final int DATA_VIEW_TYPE = 0;

        ListReceiptAdapter(@NonNull final DiffUtil.ItemCallback<HyperwalletTransferMethod> diffCallback) {
            super(diffCallback);
        }

        @Override
        public int getItemViewType(int position) {
            if (position != 0) {
                HyperwalletTransferMethod previous = getItem(position - 1);
                HyperwalletTransferMethod current = getItem(position);
                if (isDataViewType(previous, current)) {
                    return DATA_VIEW_TYPE;
                }
            }
            return HEADER_VIEW_TYPE;
        }

        boolean isDataViewType(@NonNull final HyperwalletTransferMethod previous,
                @NonNull final HyperwalletTransferMethod current) {

            Calendar prev = Calendar.getInstance();
            prev.setTime(DateUtility.fromDateTimeString(previous.getField(CREATED_ON)));
            Calendar curr = Calendar.getInstance();
            curr.setTime(DateUtility.fromDateTimeString(current.getField(CREATED_ON)));

            return prev.get(Calendar.MONTH) == curr.get(Calendar.MONTH)
                    && prev.get(Calendar.YEAR) == curr.get(Calendar.YEAR);
        }

        @NonNull
        @Override
        public ReceiptViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            LayoutInflater layout = LayoutInflater.from(viewGroup.getContext());

            if (viewType == HEADER_VIEW_TYPE) {
                View headerView = layout.inflate(R.layout.item_receipt_with_header, viewGroup, false);
                return new ReceiptViewHolderWithHeader(headerView);
            }

            View dataView = layout.inflate(R.layout.item_receipt, viewGroup, false);
            return new ReceiptViewHolder(dataView);
        }

        @Override
        public void onBindViewHolder(@NonNull ReceiptViewHolder holder, int position) {
            final HyperwalletTransferMethod transferMethod = getItem(position);
            if (transferMethod != null) {
                holder.bind(transferMethod);
            }
        }

        class ReceiptViewHolder extends RecyclerView.ViewHolder {

            private static final int MAX_CHARACTERS_FIRST_LINE = 38;
            private static final String ELLIPSIS = "...";
            private static final String SPACER = " ";
            private final TextView mTransactionAmount;
            private final TextView mTransactionCurrency;
            private final TextView mTransactionDate;
            private final TextView mTransactionTitle;
            private final TextView mTransactionTypeIcon;

            //TODO remove this after converting to Receipts model
            private final Random amount = new Random(100);

            ReceiptViewHolder(@NonNull final View item) {
                super(item);
                mTransactionAmount = item.findViewById(R.id.transaction_amount);
                mTransactionCurrency = item.findViewById(R.id.transaction_currency);
                mTransactionDate = item.findViewById(R.id.transaction_date);
                mTransactionTitle = item.findViewById(R.id.transaction_title);
                mTransactionTypeIcon = item.findViewById(R.id.transaction_type_icon);
            }

            void bind(@NonNull final HyperwalletTransferMethod transferMethod) {
                int nextAmount = amount.nextInt();
                Locale locale = Locale.getDefault();
                if (nextAmount < 0) {//TODO replace this with debit or credit
                    mTransactionAmount.setTextColor(mTransactionAmount.getContext()
                            .getResources().getColor(R.color.colorAccent));
                    mTransactionAmount.setText(String.format(locale, "- %d.00", Math.abs(nextAmount)));

                    mTransactionTypeIcon.setTextColor(mTransactionTypeIcon.getContext()
                            .getResources().getColor(R.color.colorAccent));
                    mTransactionTypeIcon.setBackground(mTransactionTypeIcon.getContext()
                            .getDrawable(R.drawable.circle_negative));
                    mTransactionTypeIcon.setText(mTransactionTypeIcon.getContext().getText(R.string.debit));
                } else {
                    mTransactionAmount.setTextColor(mTransactionAmount.getContext()
                            .getResources().getColor(R.color.positiveColor));
                    mTransactionAmount.setText(String.format(locale, "+ %d.00", nextAmount));

                    mTransactionTypeIcon.setTextColor(mTransactionTypeIcon.getContext()
                            .getResources().getColor(R.color.positiveColor));
                    mTransactionTypeIcon.setBackground(mTransactionTypeIcon.getContext()
                            .getDrawable(R.drawable.circle_positive));
                    mTransactionTypeIcon.setText(mTransactionTypeIcon.getContext().getText(R.string.credit));
                }

                mTransactionCurrency.setText(transferMethod.getField(TRANSFER_METHOD_CURRENCY));
                mTransactionTitle.setText(getTransactionTitle(transferMethod.getField(TOKEN),
                        mTransactionAmount.getText().length(), mTransactionTitle.getContext()));
                mTransactionDate.setText(DateUtility.toDateFormat(
                        DateUtility.fromDateTimeString(transferMethod.getField(CREATED_ON)), CAPTION_DATE_FORMAT));
            }

            CharSequence getTransactionTitle(@NonNull final CharSequence title, final int numberOfCharsAlreadyUsed,
                    @NonNull final Context context) {

                if (!context.getResources().getBoolean(R.bool.isLandscape)
                        && (title.length() + numberOfCharsAlreadyUsed) >= MAX_CHARACTERS_FIRST_LINE) {
                    int allowedCharsLength = MAX_CHARACTERS_FIRST_LINE -
                            numberOfCharsAlreadyUsed - ELLIPSIS.length() - SPACER.length();
                    StringBuilder builder = new StringBuilder(title.subSequence(0, allowedCharsLength));
                    builder.append(ELLIPSIS);
                    return builder.toString();
                }
                return title;
            }
        }

        class ReceiptViewHolderWithHeader extends ReceiptViewHolder {

            private final TextView mTransactionHeaderText;

            ReceiptViewHolderWithHeader(@NonNull final View item) {
                super(item);
                mTransactionHeaderText = item.findViewById(R.id.item_date_header_title);
            }

            @Override
            void bind(@NonNull final HyperwalletTransferMethod transferMethod) {
                super.bind(transferMethod);
                mTransactionHeaderText.setText(DateUtility.toDateFormat(
                        DateUtility.fromDateTimeString(transferMethod.getField(CREATED_ON)), HEADER_DATE_FORMAT));
            }
        }
    }
}
