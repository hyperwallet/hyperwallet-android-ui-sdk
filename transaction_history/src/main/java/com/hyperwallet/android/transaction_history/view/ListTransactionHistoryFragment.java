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
import com.hyperwallet.android.model.HyperwalletError;
import com.hyperwallet.android.model.HyperwalletErrors;
import com.hyperwallet.android.model.HyperwalletTransferMethod;
import com.hyperwallet.android.transaction_history.viewmodel.ListReceiptViewModel;

import java.util.List;

public class ListTransactionHistoryFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    private ListTransactionHistoryAdapter mListTransactionHistoryAdapter;
    private ListReceiptViewModel mListReceiptViewModel;
    private OnLoadTransactionHistoryNetworkErrorCallback mOnLoadTransactionHistoryNetworkErrorCallback;

    public static ListTransactionHistoryFragment newInstance() {
        return new ListTransactionHistoryFragment();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListReceiptViewModel = ViewModelProviders.of(getActivity()).get(
                ListReceiptViewModel.class);
    }

    public void retry() {
        mListReceiptViewModel.retry();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mOnLoadTransactionHistoryNetworkErrorCallback = (OnLoadTransactionHistoryNetworkErrorCallback) getActivity();
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
        mListTransactionHistoryAdapter = new ListTransactionHistoryAdapter(new HyperwalletTransferMethodDiffCallBack());
        mProgressBar = view.findViewById(R.id.list_transfer_method_progress_bar);
        mRecyclerView = view.findViewById(R.id.list_transaction_history_item);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mListTransactionHistoryAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        registerObservers();
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

    private void registerObservers() {
        mListReceiptViewModel.getRecipeList().observe(this, new Observer<PagedList<HyperwalletTransferMethod>>() {
            @Override
            public void onChanged(PagedList<HyperwalletTransferMethod> hyperwalletTransferMethods) {
                mListTransactionHistoryAdapter.submitList(hyperwalletTransferMethods);
            }
        });

        mListReceiptViewModel.getReceiptListError().observe(this,
                new Observer<HyperwalletErrors>() {
                    @Override
                    public void onChanged(HyperwalletErrors hyperwalletErrors) {
                        if (hyperwalletErrors != null) {
                            mOnLoadTransactionHistoryNetworkErrorCallback.showErrorsLoadTransactionHistory(
                                    hyperwalletErrors.getErrors());
                        }
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

    interface OnLoadTransactionHistoryNetworkErrorCallback {

        void showErrorsLoadTransactionHistory(@NonNull final List<HyperwalletError> errors);
    }


    private static class ListTransactionHistoryAdapter extends
            PagedListAdapter<HyperwalletTransferMethod, ListTransactionHistoryAdapter.ViewHolder> {

        ListTransactionHistoryAdapter(
                @NonNull DiffUtil.ItemCallback<HyperwalletTransferMethod> diffCallback) {
            super(diffCallback);
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater layout = LayoutInflater.from(viewGroup.getContext());
            View itemViewLayout = layout.inflate(R.layout.item_transaction_history_item, viewGroup, false);
            return new ViewHolder(itemViewLayout);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
            final HyperwalletTransferMethod transferMethod = getItem(position);
            viewHolder.bind(transferMethod);
        }


        class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView mTitle;

            ViewHolder(@NonNull final View itemView) {
                super(itemView);
                mTitle = itemView.findViewById(R.id.transaction_history_id);
            }

            void bind(@NonNull final HyperwalletTransferMethod transferMethod) {
                mTitle.setText(transferMethod.getField(HyperwalletTransferMethod.TransferMethodFields.TOKEN));
            }

        }
    }
}
