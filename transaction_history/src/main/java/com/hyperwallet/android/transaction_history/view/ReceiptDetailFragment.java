package com.hyperwallet.android.transaction_history.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.hyperwallet.android.hyperwallet_transactionhistory.R;
import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod;
import com.hyperwallet.android.transaction_history.viewmodel.ReceiptDetailViewModel;

public class ReceiptDetailFragment extends Fragment {

    private ReceiptDetailViewModel mReceiptDetailViewModel;
    private TextView mTextView;

    public static ReceiptDetailFragment getInstance() {
        return new ReceiptDetailFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mReceiptDetailViewModel = ViewModelProviders.of(getActivity()).get(ReceiptDetailViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.receipt_detail_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTextView = view.findViewById(R.id.receipt_detail);
        mTextView.setText(mReceiptDetailViewModel.getHyperwalletTransferMethod().getField(
                HyperwalletTransferMethod.TransferMethodFields.TOKEN));
    }
}
