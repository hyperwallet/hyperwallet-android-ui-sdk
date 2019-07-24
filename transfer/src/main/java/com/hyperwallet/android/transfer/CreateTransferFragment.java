package com.hyperwallet.android.transfer;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.hyperwallet.android.model.HyperwalletErrors;
import com.hyperwallet.android.model.transfer.Transfer;
import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod;
import com.hyperwallet.android.ui.common.viewmodel.Event;

import java.util.Locale;

public class CreateTransferFragment extends Fragment {

    private CreateTransferViewModel mCreateTransferViewModel;
    private TextView mTransferDestinationIcon;
    private TextView mTransferDestinationTitle;
    private TextView mTransferDestinationDescription;
    private TextView mTransferDestinationCountry;
    private TextView mAvailableFunds;
    private EditText mTransferAmount;
    private CheckBox mTransferAllFunds;
    private Button mNext;



    public static CreateTransferFragment newInstance() {
        return new CreateTransferFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCreateTransferViewModel = ViewModelProviders.of(requireActivity()).get(CreateTransferViewModel.class);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_transfer, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        registerTransferDestinationObserver(view);
        registerAvailableFundsObserver(view);
        registerTransferAmountObserver(view);
        mNext = view.findViewById(R.id.btn_next);
        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCreateTransferViewModel.createTransfer(mTransferAmount.getText().toString(), "notes here");
            }
        });
    }


    void retry() {
        mCreateTransferViewModel.retry(); //we let the view model decide what needs to be retried based on its state
    }

    private void registerTransferDestinationObserver(@NonNull final View view) {
        mTransferDestinationIcon = view.findViewById(R.id.transfer_destination_icon);
        mTransferDestinationTitle = view.findViewById(R.id.transfer_destination_title);
        mTransferDestinationCountry = view.findViewById(R.id.transfer_destination_description_1);
        mTransferDestinationDescription = view.findViewById(R.id.transfer_destination_description_2);

        mCreateTransferViewModel.getTransferDestination().observe(this, new Observer<HyperwalletTransferMethod>() {
            @Override
            public void onChanged(HyperwalletTransferMethod transferMethod) {

                //TODO move transfer method utils to common
                String icon = TransferMethodUtils.getStringFontIcon(CreateTransferFragment.this.requireContext(),
                        transferMethod.getField(HyperwalletTransferMethod.TransferMethodFields.TYPE));
                mTransferDestinationIcon.setText(icon);

                String title = TransferMethodUtils.getTransferMethodName(CreateTransferFragment.this.requireContext(),
                        transferMethod);
                mTransferDestinationTitle.setText(title);

                String description = TransferMethodUtils.getTransferMethodDetail(
                        CreateTransferFragment.this.requireContext(), transferMethod,
                        transferMethod.getField(HyperwalletTransferMethod.TransferMethodFields.TYPE));
                mTransferDestinationDescription.setText(description);

                Locale locale = new Locale.Builder().setRegion(transferMethod.getField(
                        HyperwalletTransferMethod.TransferMethodFields.TRANSFER_METHOD_COUNTRY)).build();
                mTransferDestinationCountry.setText(locale.getDisplayName());

            }
        });
    }


    private void registerAvailableFundsObserver(@NonNull final View view) {
        mAvailableFunds = view.findViewById(R.id.available_funds);
        mCreateTransferViewModel.getQuoteAvailableFunds().observe(this, new Observer<Transfer>() {
            @Override
            public void onChanged(Transfer transfer) {
                mAvailableFunds.setText(getString(R.string.available_funds, transfer.getDestinationAmount(), transfer.getDestinationCurrency()));
            }
        });
    }


    private void registerTransferAmountObserver(@NonNull final View view) {
        mTransferAmount = view.findViewById(R.id.transfer_amount);
        mTransferAllFunds = view.findViewById(R.id.cb_transfer_all_funds);
        mTransferAllFunds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = ((CheckBox) v).isChecked();
                mCreateTransferViewModel.setTransferAllAvailableFunds(checked);
            }
        });
        mCreateTransferViewModel.getTransferAllAvailableFunds().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean transferAllFunds) {
                if (transferAllFunds) {
                    mTransferAmount.setEnabled(!transferAllFunds);
                    mTransferAmount.setText(mCreateTransferViewModel.getQuoteAvailableFunds().getValue().getDestinationAmount());
                } else {
                    mTransferAmount.setEnabled(!transferAllFunds);
                    mTransferAmount.setText("");
                }
            }
        });
    }

}
