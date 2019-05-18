package com.hyperwallet.android.transaction_history.view;

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
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hyperwallet.android.hyperwallet_transactionhistory.R;
import com.hyperwallet.android.model.HyperwalletError;
import com.hyperwallet.android.model.HyperwalletErrors;
import com.hyperwallet.android.model.HyperwalletTransferMethod;
import com.hyperwallet.android.transaction_history.viewmodel.ListTransactionHistoryViewModel;

import java.util.ArrayList;
import java.util.List;

public class ListTransactionHistoryFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private ListTransactionHistoryAdapter mListTransactionHistoryAdapter;
    private ListTransactionHistoryViewModel mListTransactionHistoryViewModel;
    private OnLoadTransactionHistoryNetworkErrorCallback mOnLoadTransactionHistoryNetworkErrorCallback;

    public static ListTransactionHistoryFragment newInstance() {
        return new ListTransactionHistoryFragment();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListTransactionHistoryViewModel = ViewModelProviders.of(getActivity()).get(ListTransactionHistoryViewModel.class);

        mListTransactionHistoryAdapter = new ListTransactionHistoryAdapter(new ArrayList<HyperwalletTransferMethod>());
        mListTransactionHistoryViewModel.loadTransactionHistory().observe(this, new Observer<List<HyperwalletTransferMethod>>() {
            @Override
            public void onChanged(List<HyperwalletTransferMethod> hyperwalletTransferMethods) {
                mListTransactionHistoryAdapter.replaceData(hyperwalletTransferMethods);
            }
        });

        mListTransactionHistoryViewModel.getTransactionHistoryError().observe(this, new Observer<HyperwalletErrors>() {
            @Override
            public void onChanged(HyperwalletErrors hyperwalletErrors) {
                if (hyperwalletErrors != null) {
                    mOnLoadTransactionHistoryNetworkErrorCallback.showErrorsLoadTransactionHistory(hyperwalletErrors.getErrors());
                }
            }
        });


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
        mRecyclerView = view.findViewById(R.id.list_transaction_history_item);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mListTransactionHistoryAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        setupScrollListener();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mListTransactionHistoryViewModel.removeObservers(this);
    }

    private void setupScrollListener() {
        final LinearLayoutManager layoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {

                if (dy > 0) {
                    int totalItemCount = layoutManager.getItemCount();
                    int visibleItemCount = layoutManager.getChildCount();
                    int lastVisibleItem = layoutManager.findLastVisibleItemPosition();
                    if ( (visibleItemCount + lastVisibleItem) >= totalItemCount) {

                        mListTransactionHistoryViewModel.loadTransferMethods(visibleItemCount, lastVisibleItem, totalItemCount);
                    }
                }
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                System.out.println("");
            }
        });
    }



    interface OnLoadTransactionHistoryNetworkErrorCallback {

        void showErrorsLoadTransactionHistory(@NonNull final List<HyperwalletError> errors);
    }


    private static class ListTransactionHistoryAdapter extends RecyclerView.Adapter<ListTransactionHistoryAdapter.ViewHolder> {
        private List<HyperwalletTransferMethod> mTransferMethodList;

        ListTransactionHistoryAdapter(final List<HyperwalletTransferMethod> transferMethodList) {
            mTransferMethodList = transferMethodList;
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
            final HyperwalletTransferMethod transferMethod = mTransferMethodList.get(position);
            viewHolder.bind(transferMethod);
        }

        @Override
        public int getItemCount() {
            return mTransferMethodList.size();
        }

        void replaceData(List<HyperwalletTransferMethod> transferMethodList) {
            mTransferMethodList = transferMethodList;
            notifyDataSetChanged();
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
