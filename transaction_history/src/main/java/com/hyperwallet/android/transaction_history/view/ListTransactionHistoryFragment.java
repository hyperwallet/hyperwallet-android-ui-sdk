package com.hyperwallet.android.transaction_history.view;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hyperwallet.android.hyperwallet_transactionhistory.R;
import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod;
import com.hyperwallet.android.transaction_history.viewmodel.ListReceiptViewModel;
import com.hyperwallet.android.util.DateUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ListTransactionHistoryFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    private ListTransactionHistoryAdapter mListTransactionHistoryAdapter;
    private ListReceiptViewModel mListReceiptViewModel;
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy MM");
    private String mReceiptSourceToken; //todo save this parameter


    public static ListTransactionHistoryFragment newInstance(@NonNull final String receiptSourceToken) {
        ListTransactionHistoryFragment fragment = new ListTransactionHistoryFragment();
        fragment.mReceiptSourceToken = receiptSourceToken;
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListReceiptViewModel = ViewModelProviders.of(getActivity()).get(mReceiptSourceToken, ListReceiptViewModel.class);
    }

    public void retry() {
        mListReceiptViewModel.retry();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_transaction_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mListTransactionHistoryAdapter = new ListTransactionHistoryAdapter(mListReceiptViewModel, new HyperwalletTransferMethodDiffCallBack());
        mProgressBar = view.findViewById(R.id.list_transfer_method_progress_bar);
        mRecyclerView = view.findViewById(R.id.list_transaction_history_item);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mListTransactionHistoryAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        registerObservers();
    }



    private void registerObservers() {
        mListReceiptViewModel.getRecipeList().observe(this, new Observer<PagedList<HyperwalletTransferMethod>>() {
            @Override
            public void onChanged(PagedList<HyperwalletTransferMethod> hyperwalletTransferMethods) {
                mListTransactionHistoryAdapter.submitList(hyperwalletTransferMethods);
            }
        });
        mListReceiptViewModel.isLoadingData().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isLoading) {
                if (isLoading) {
                    mProgressBar.setVisibility(View.VISIBLE);
                } else {
                    mProgressBar.setVisibility(View.GONE);
                }
            }
        });
    }


    private static class HyperwalletTransferMethodDiffCallBack extends
            DiffUtil.ItemCallback<HyperwalletTransferMethod> {

        @Override
        public boolean areItemsTheSame(@NonNull HyperwalletTransferMethod oldItem,
                @NonNull HyperwalletTransferMethod newItem) {

            return oldItem.getField(HyperwalletTransferMethod.TransferMethodFields.TOKEN).equals(newItem.getField(
                    HyperwalletTransferMethod.TransferMethodFields.TOKEN));

        }

        @Override
        public boolean areContentsTheSame(@NonNull HyperwalletTransferMethod oldItem,
                @NonNull HyperwalletTransferMethod newItem) {
            return oldItem.getField(HyperwalletTransferMethod.TransferMethodFields.TOKEN).equals(newItem.getField(
                    HyperwalletTransferMethod.TransferMethodFields.TOKEN));
        }
    }


    private static class ListTransactionHistoryAdapter extends
            PagedListAdapter<HyperwalletTransferMethod, ListTransactionHistoryAdapter.ViewHolder> {

        private ListReceiptViewModel mListReceiptViewModel;

        ListTransactionHistoryAdapter(ListReceiptViewModel listReceiptViewModel,
                @NonNull DiffUtil.ItemCallback<HyperwalletTransferMethod> diffCallback) {
            super(diffCallback);
            mListReceiptViewModel = listReceiptViewModel;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater layout = LayoutInflater.from(viewGroup.getContext());
            if (i == 0) {
                View itemViewLayout = layout.inflate(R.layout.item_transaction_history_item, viewGroup, false);
                return new ViewHolder(itemViewLayout);
            } else {
                View itemViewLayout = layout.inflate(R.layout.item_transaction_history_item_header, viewGroup, false);
                return new ViewHolderHeader(itemViewLayout);
            }
        }

        @Override
        public int getItemViewType(int position) {

            if (position != 0) {
                HyperwalletTransferMethod previous = getItem(position-1);
                HyperwalletTransferMethod current = getItem(position);
                if (shouldGroup(previous, current)) {
                    return 1;
                }
            }
            return 0;

        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
            final HyperwalletTransferMethod transferMethod = getItem(position);
            viewHolder.bind(transferMethod);
        }

        @Override
        public void onViewRecycled(@NonNull ViewHolder holder) {
            super.onViewRecycled(holder);
            holder.mTitle.setOnClickListener(null);
        }

        boolean shouldGroup(HyperwalletTransferMethod previous, HyperwalletTransferMethod current) {
            Date previousDate = DateUtil.fromDateTimeString(previous.getField(HyperwalletTransferMethod.TransferMethodFields.CREATED_ON));
            Date currentDate = DateUtil.fromDateTimeString(current.getField(HyperwalletTransferMethod.TransferMethodFields.CREATED_ON));
            return previousDate.getMonth() != currentDate.getMonth() || previousDate.getYear() !=  currentDate.getYear();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private TextView mTitle;

            ViewHolder(@NonNull final View itemView) {
                super(itemView);
            }

            void bind(@NonNull final HyperwalletTransferMethod transferMethod) {
                mTitle = itemView.findViewById(R.id.transaction_history_id);
                mTitle.setText(transferMethod.getField(HyperwalletTransferMethod.TransferMethodFields.TOKEN));
                mTitle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListReceiptViewModel.navigateToDetail(transferMethod);
                    }
                });
            }
        }

        class ViewHolderHeader extends ViewHolder {
            private final TextView mTitle;
            private final TextView mHeader;

            ViewHolderHeader(@NonNull final View itemView) {
                super(itemView);
                mTitle = itemView.findViewById(R.id.transaction_history_id2);
                mHeader = itemView.findViewById(R.id.header);
            }

            void bind(@NonNull final HyperwalletTransferMethod transferMethod) {
                mTitle.setText(transferMethod.getField(HyperwalletTransferMethod.TransferMethodFields.TOKEN));
                Date date = DateUtil.fromDateTimeString(transferMethod.getField(HyperwalletTransferMethod.TransferMethodFields.CREATED_ON));
                mHeader.setText(DATE_FORMAT.format(date));
                mTitle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListReceiptViewModel.navigateToDetail(transferMethod);
                    }
                });
            }
        }
    }



}
