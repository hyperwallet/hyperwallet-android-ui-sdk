package com.hyperwallet.android.ui.transfermethod;

import static com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod.TransferMethodFields.BANK_ACCOUNT_ID;
import static com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod.TransferMethodFields.CARD_NUMBER;
import static com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod.TransferMethodFields.TRANSFER_METHOD_COUNTRY;
import static com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod.TransferMethodFields.TYPE;
import static com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod.TransferMethodTypes.BANK_ACCOUNT;
import static com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod.TransferMethodTypes.BANK_CARD;
import static com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod.TransferMethodTypes.PREPAID_CARD;
import static com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod.TransferMethodTypes.WIRE_ACCOUNT;
import static com.hyperwallet.android.ui.transfermethod.TransferMethodUtils.getStringFontIcon;
import static com.hyperwallet.android.ui.transfermethod.TransferMethodUtils.getStringResourceByName;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod;
import com.hyperwallet.android.ui.R;
import com.hyperwallet.android.ui.common.view.HorizontalDividerItemDecorator;
import com.hyperwallet.android.ui.viewmodel.ListTransferDestinationViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ListTransferDestinationFragment extends Fragment {

    private RecyclerView recyclerView;
    private View mProgressBar;
    private ListTransferDestinationViewModel mTransferMethodSelectorViewModel;
    private ListTransferDestinationAdapter mListTransferMethodSelectorAdapter;

    public static ListTransferDestinationFragment newInstance() {
        return new ListTransferDestinationFragment();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTransferMethodSelectorViewModel = ViewModelProviders.of(requireActivity()).get(ListTransferDestinationViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_transfer_method_selector, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mProgressBar = view.findViewById(R.id.list_transfer_method_selector_progress_bar);
        recyclerView = view.findViewById(R.id.list_transfer_method_selector_item);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new HorizontalDividerItemDecorator(requireContext(), false));
        mListTransferMethodSelectorAdapter = new ListTransferDestinationAdapter(new ArrayList<HyperwalletTransferMethod>(), mTransferMethodSelectorViewModel);
        recyclerView.setAdapter(mListTransferMethodSelectorAdapter);
        mTransferMethodSelectorViewModel.getTransferDestinationList().observe(this,
                new Observer<List<HyperwalletTransferMethod>>() {
                    @Override
                    public void onChanged(List<HyperwalletTransferMethod> transferMethods) {
                        mListTransferMethodSelectorAdapter.replaceData(transferMethods);
                    }
                });

    }



    private static class ListTransferDestinationAdapter extends RecyclerView.Adapter<ListTransferDestinationAdapter.ViewHolder> {

        private List<HyperwalletTransferMethod> mTransferMethodList;
        private ListTransferDestinationViewModel mTransferMethodSelectorViewModel;

        ListTransferDestinationAdapter(@NonNull final List<HyperwalletTransferMethod> transferMethodList,
                @NonNull final ListTransferDestinationViewModel transferMethodSelectorViewModel) {
            mTransferMethodList = transferMethodList;
            mTransferMethodSelectorViewModel = transferMethodSelectorViewModel;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater layout = LayoutInflater.from(viewGroup.getContext());
            View itemViewLayout = layout.inflate(R.layout.item_transfer_method_selector, viewGroup, false);
            return new ViewHolder(itemViewLayout);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
            final HyperwalletTransferMethod transferMethod = mTransferMethodList.get(position);
            viewHolder.bind(transferMethod);
        }

        @Override
        public void onViewRecycled(@NonNull ViewHolder holder) {
            holder.recycle();
        }

        void replaceData(@NonNull final List<HyperwalletTransferMethod> transferMethods) {
            notifyDataSetChanged();
            mTransferMethodList = transferMethods;
        }

        private String getAccountIdentifier(HyperwalletTransferMethod transferMethod) {
            String transferIdentification = "";
            switch (transferMethod.getField(TYPE)) {
                case BANK_ACCOUNT:
                case WIRE_ACCOUNT:
                    transferIdentification = transferMethod.getField(BANK_ACCOUNT_ID);
                    break;
                case BANK_CARD:
                case PREPAID_CARD:
                    transferIdentification = transferMethod.getField(CARD_NUMBER);
                    break;
                default: // none for paper check
            }
            return (transferIdentification.length() > 4
                    ? transferIdentification.substring(transferIdentification.length() - 4)
                    : transferIdentification);
        }

        @Override
        public int getItemCount() {
            return mTransferMethodList.size();
        }


        class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView mTitle;
            private final TextView mTransferMethodCountry;
            private final TextView mTransferMethodIdentification;
            private final TextView mIcon;

            ViewHolder(@NonNull final View itemView) {
                super(itemView);
                mTitle = itemView.findViewById(R.id.transfer_method_type_title);
                mIcon = itemView.findViewById(R.id.transfer_method_type_icon);
                mTransferMethodCountry = itemView.findViewById(R.id.transfer_method_type_description_1);
                mTransferMethodIdentification = itemView.findViewById(R.id.transfer_method_type_description_2);

            }


            void bind(@NonNull final HyperwalletTransferMethod transferMethod) {
                String transferMethodType = transferMethod.getField(TYPE);
                mTitle.setText(getStringResourceByName(mTitle.getContext(), transferMethodType));

                Locale locale = new Locale.Builder().setRegion(transferMethod.getField(TRANSFER_METHOD_COUNTRY)).build();
                mIcon.setText(getStringFontIcon(mIcon.getContext(), transferMethodType));
                mTransferMethodCountry.setText(locale.getDisplayName());
                mTransferMethodIdentification.setText(mTransferMethodIdentification
                        .getContext().getString(R.string.transfer_method_list_item_description,
                                getAccountIdentifier(transferMethod)));
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mTransferMethodSelectorViewModel.selectTransferMethod(transferMethod);
                    }
                });

            }

            void recycle() {
                itemView.setOnClickListener(null);
            }


        }
    }

}
