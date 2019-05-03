/*
 * Copyright 2018 Hyperwallet
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

package com.hyperwallet.android.ui.transfermethod;

import static com.hyperwallet.android.ui.transfermethod.TransferMethodUtils.getStringFontIcon;
import static com.hyperwallet.android.ui.transfermethod.TransferMethodUtils.getStringResourceByName;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hyperwallet.android.hyperwallet_ui.R;
import com.hyperwallet.android.model.HyperwalletError;
import com.hyperwallet.android.ui.repository.RepositoryFactory;
import com.hyperwallet.android.ui.view.CountrySelectionDialogFragment;
import com.hyperwallet.android.ui.view.CurrencySelectionDialogFragment;
import com.hyperwallet.android.ui.view.HorizontalDividerItemDecorator;
import com.hyperwallet.android.ui.view.widget.OneClickListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

public class SelectTransferMethodFragment extends Fragment implements SelectTransferMethodContract.View {

    private static final String ARGUMENT_COUNTRY_CODE_SELECTED = "ARGUMENT_COUNTRY_CODE_SELECTED";
    private static final String ARGUMENT_CURRENCY_CODE_SELECTED = "ARGUMENT_CURRENCY_CODE_SELECTED";
    private static final String DEFAULT_COUNTRY_CODE = "US";
    private static final String DEFAULT_CURRENCY_CODE = "USD";
    private static final boolean FORCE_UPDATE = false;
    private static final String TAG = SelectTransferMethodFragment.class.getName();

    private TextView mCountryValue;
    private TextView mCurrencyValue;
    private OnLoadCountrySelectionNetworkErrorCallback mOnLoadCountrySelectionNetworkErrorCallback;
    private OnLoadCurrencyConfigurationNetworkErrorCallback mOnLoadCurrencyConfigurationNetworkErrorCallback;
    private OnLoadCurrencySelectionNetworkErrorCallback mOnLoadCurrencySelectionNetworkErrorCallback;
    private OnLoadTransferMethodConfigurationKeysNetworkErrorCallback
            mOnLoadTransferMethodConfigurationKeysNetworkErrorCallback;
    private OnLoadTransferMethodTypeNetworkErrorCallback mOnLoadTransferMethodTypeNetworkErrorCallback;
    private SelectTransferMethodContract.Presenter mPresenter;
    private View mProgressBar;
    private RecyclerView mRecyclerView;
    private String mSelectedCountryCode;
    private String mSelectedCurrencyCode;
    private TransferMethodTypesAdapter mTransferMethodTypesAdapter;

    public SelectTransferMethodFragment() {
    }

    public static SelectTransferMethodFragment newInstance() {
        SelectTransferMethodFragment selectTransferMethodFragment = new SelectTransferMethodFragment();
        selectTransferMethodFragment.mSelectedCountryCode = DEFAULT_COUNTRY_CODE;
        selectTransferMethodFragment.mSelectedCurrencyCode = DEFAULT_CURRENCY_CODE;

        Bundle arguments = new Bundle();
        arguments.putString(ARGUMENT_COUNTRY_CODE_SELECTED, selectTransferMethodFragment.mSelectedCountryCode);
        arguments.putString(ARGUMENT_CURRENCY_CODE_SELECTED, selectTransferMethodFragment.mSelectedCurrencyCode);
        selectTransferMethodFragment.setArguments(arguments);
        return selectTransferMethodFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mOnLoadTransferMethodConfigurationKeysNetworkErrorCallback =
                    (OnLoadTransferMethodConfigurationKeysNetworkErrorCallback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement "
                    + OnLoadTransferMethodConfigurationKeysNetworkErrorCallback.class.getCanonicalName());
        }

        try {
            mOnLoadCurrencyConfigurationNetworkErrorCallback =
                    (OnLoadCurrencyConfigurationNetworkErrorCallback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement "
                    + OnLoadCurrencyConfigurationNetworkErrorCallback.class.getCanonicalName());
        }

        try {
            mOnLoadTransferMethodTypeNetworkErrorCallback = (OnLoadTransferMethodTypeNetworkErrorCallback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement "
                    + OnLoadTransferMethodTypeNetworkErrorCallback.class.getCanonicalName());
        }

        try {
            mOnLoadCountrySelectionNetworkErrorCallback = (OnLoadCountrySelectionNetworkErrorCallback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement "
                    + OnLoadCountrySelectionNetworkErrorCallback.class.getCanonicalName());
        }

        try {
            mOnLoadCurrencySelectionNetworkErrorCallback = (OnLoadCurrencySelectionNetworkErrorCallback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement "
                    + OnLoadCurrencySelectionNetworkErrorCallback.class.getCanonicalName());
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_select_transfer_method, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mProgressBar = view.findViewById(R.id.select_transfer_method_progress_bar);
        mCountryValue = view.findViewById(R.id.select_transfer_method_country_value);
        mCurrencyValue = view.findViewById(R.id.select_transfer_method_currency_value);

        View countrySelectionContainer = view.findViewById(R.id.select_transfer_method_country_container);
        countrySelectionContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.loadCountrySelection(mSelectedCountryCode);
            }
        });

        View currencySelectionContainer = view.findViewById(R.id.select_transfer_method_currency_container);
        currencySelectionContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.loadCurrencySelection(mSelectedCountryCode, mSelectedCurrencyCode);
            }
        });

        mTransferMethodTypesAdapter = new TransferMethodTypesAdapter(new ArrayList<TransferMethodSelectionItem>(),
                new TransferMethodSelectionItemListener() {
                    @Override
                    public void onTransferMethodSelected(TransferMethodSelectionItem transferMethodType) {
                        mPresenter.openAddTransferMethod(mSelectedCountryCode, mSelectedCurrencyCode,
                                transferMethodType.getTransferMethodType());
                    }
                });

        mRecyclerView = view.findViewById(R.id.select_transfer_method_types_list);
        mRecyclerView.setAdapter(mTransferMethodTypesAdapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.addItemDecoration(new HorizontalDividerItemDecorator(getContext(), true));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        RepositoryFactory factory = RepositoryFactory.getInstance();
        mPresenter = new SelectTransferMethodPresenter(this,
                factory.getTransferMethodConfigurationRepository(),
                factory.getUserRepository());
    }


    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            mSelectedCurrencyCode = savedInstanceState.getString(ARGUMENT_CURRENCY_CODE_SELECTED);
            mSelectedCountryCode = savedInstanceState.getString(ARGUMENT_COUNTRY_CODE_SELECTED);
        } else {
            mSelectedCurrencyCode = getArguments().getString(ARGUMENT_CURRENCY_CODE_SELECTED);
            mSelectedCountryCode = getArguments().getString(ARGUMENT_COUNTRY_CODE_SELECTED);
        }
        mCountryValue.setText(getCountryDisplay(mSelectedCountryCode));
        mCurrencyValue.setText(mSelectedCurrencyCode);
        mPresenter.loadTransferMethodConfigurationKeys(false, mSelectedCountryCode, mSelectedCurrencyCode);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(ARGUMENT_COUNTRY_CODE_SELECTED, mSelectedCountryCode);
        outState.putString(ARGUMENT_CURRENCY_CODE_SELECTED, mSelectedCurrencyCode);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void showTransferMethodCurrency(@NonNull String currencyCode) {
        mSelectedCurrencyCode = currencyCode;
        mCurrencyValue.setText(currencyCode);
    }

    @Override
    public void showTransferMethodCountry(@NonNull String countryCode) {
        mSelectedCountryCode = countryCode;
        mCountryValue.setText(getCountryDisplay(countryCode));
    }

    @Override
    public void showTransferMethodTypes(@NonNull List<TransferMethodSelectionItem> transferMethodTypes) {
        mTransferMethodTypesAdapter.replaceData(transferMethodTypes);
    }

    public void showCountryCode(final String countryCode) {
        mSelectedCountryCode = countryCode;
        mPresenter.loadCurrency(FORCE_UPDATE, countryCode);
    }

    public void showCurrencyCode(final String currencyCode) {
        mSelectedCurrencyCode = currencyCode;
        mPresenter.loadTransferMethodTypes(FORCE_UPDATE, mSelectedCountryCode, currencyCode);
    }

    @Override
    public void showProgressBar() {
        mRecyclerView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgressBar() {
        mRecyclerView.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void showCountrySelectionDialog(@NonNull final TreeMap<String, String> countryNameCodeMap,
            @NonNull final String selectedCountryName) {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.popBackStack(CountrySelectionDialogFragment.TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        CountrySelectionDialogFragment fragment =
                CountrySelectionDialogFragment.newInstance(countryNameCodeMap, selectedCountryName);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.replace(android.R.id.content, fragment, CountrySelectionDialogFragment.TAG);
        fragmentTransaction.addToBackStack(CountrySelectionDialogFragment.TAG);
        fragmentTransaction.commit();
    }

    @Override
    public void showCurrencySelectionDialog(@NonNull final TreeMap<String, String> currencyNameCodeMap,
            @NonNull final String selectedCurrencyName) {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.popBackStack(CurrencySelectionDialogFragment.TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        CurrencySelectionDialogFragment fragment =
                CurrencySelectionDialogFragment.newInstance(currencyNameCodeMap, selectedCurrencyName);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.replace(android.R.id.content, fragment, CurrencySelectionDialogFragment.TAG);
        fragmentTransaction.addToBackStack(CurrencySelectionDialogFragment.TAG);
        fragmentTransaction.commit();
    }

    @Override
    public void showAddTransferMethod(String country, String currency, String transferMethodType) {
        Intent intent = new Intent(getActivity(), AddTransferMethodActivity.class);
        intent.putExtra(AddTransferMethodActivity.EXTRA_TRANSFER_METHOD_COUNTRY, country);
        intent.putExtra(AddTransferMethodActivity.EXTRA_TRANSFER_METHOD_CURRENCY, currency);
        intent.putExtra(AddTransferMethodActivity.EXTRA_TRANSFER_METHOD_TYPE, transferMethodType);
        getActivity().startActivityForResult(intent, AddTransferMethodActivity.REQUEST_CODE);
    }

    @Override
    public void showErrorLoadTransferMethodConfigurationKeys(@NonNull final List<HyperwalletError> errors) {
        mOnLoadTransferMethodConfigurationKeysNetworkErrorCallback.showErrorsLoadTransferMethodConfigurationKeys(
                errors);
    }

    @Override
    public void showErrorLoadCurrency(@NonNull final List<HyperwalletError> errors) {
        mOnLoadCurrencyConfigurationNetworkErrorCallback.showErrorsLoadCurrencyConfiguration(errors);
    }

    @Override
    public void showErrorLoadTransferMethodTypes(@NonNull final List<HyperwalletError> errors) {
        mOnLoadTransferMethodTypeNetworkErrorCallback.showErrorsLoadTransferMethodTypes(errors);
    }

    @Override
    public void showErrorLoadCountrySelection(@NonNull final List<HyperwalletError> errors) {
        mOnLoadCountrySelectionNetworkErrorCallback.showErrorsLoadCountrySelection(errors);
    }

    @Override
    public void showErrorLoadCurrencySelection(@NonNull final List<HyperwalletError> errors) {
        mOnLoadCurrencySelectionNetworkErrorCallback.showErrorsLoadCurrencySelection(errors);
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void reloadCurrency() {
        mPresenter.loadCurrency(FORCE_UPDATE, mSelectedCountryCode);
    }

    @Override
    public void reloadTransferMethodConfigurationKeys() {
        mPresenter.loadTransferMethodConfigurationKeys(FORCE_UPDATE, mSelectedCountryCode, mSelectedCurrencyCode);
    }

    @Override
    public void reloadTransferMethodTypes() {
        mPresenter.loadTransferMethodTypes(FORCE_UPDATE, mSelectedCountryCode, mSelectedCurrencyCode);
    }

    @Override
    public void reloadCountrySelection() {
        mPresenter.loadCountrySelection(mSelectedCountryCode);
    }

    @Override
    public void reloadCurrencySelection() {
        mPresenter.loadCurrencySelection(mSelectedCountryCode, mSelectedCurrencyCode);
    }

    public interface TransferMethodSelectionItemListener {

        void onTransferMethodSelected(TransferMethodSelectionItem transferMethodType);
    }

    interface OnLoadTransferMethodConfigurationKeysNetworkErrorCallback {

        void showErrorsLoadTransferMethodConfigurationKeys(@NonNull final List<HyperwalletError> errors);
    }

    interface OnLoadCurrencyConfigurationNetworkErrorCallback {

        void showErrorsLoadCurrencyConfiguration(@NonNull final List<HyperwalletError> errors);
    }

    interface OnLoadTransferMethodTypeNetworkErrorCallback {

        void showErrorsLoadTransferMethodTypes(@NonNull final List<HyperwalletError> errors);
    }

    interface OnLoadCountrySelectionNetworkErrorCallback {

        void showErrorsLoadCountrySelection(@NonNull final List<HyperwalletError> errors);
    }

    interface OnLoadCurrencySelectionNetworkErrorCallback {

        void showErrorsLoadCurrencySelection(@NonNull final List<HyperwalletError> errors);
    }

    private String getCountryDisplay(@NonNull final String countryCode) {
        Locale locale = new Locale.Builder().setRegion(countryCode).build();
        return locale.getDisplayName();
    }

    private static class TransferMethodTypesAdapter extends
            RecyclerView.Adapter<TransferMethodTypesAdapter.ViewHolder> {

        private List<TransferMethodSelectionItem> mTransferMethodTypes;
        private TransferMethodSelectionItemListener mTransferMethodSelectionItemListener;

        TransferMethodTypesAdapter(@NonNull final List<TransferMethodSelectionItem> transferMethodTypes,
                @NonNull final TransferMethodSelectionItemListener transferMethodSelectionItemListener) {
            mTransferMethodTypes = transferMethodTypes;
            mTransferMethodSelectionItemListener = transferMethodSelectionItemListener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            Context context = viewGroup.getContext();
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            View transferMethodTypeView = layoutInflater.inflate(R.layout.item_transfer_method_type, viewGroup, false);

            return new ViewHolder(transferMethodTypeView, mTransferMethodSelectionItemListener);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
            TransferMethodSelectionItem selectionItem = mTransferMethodTypes.get(position);
            viewHolder.bind(selectionItem);
        }

        @Override
        public int getItemCount() {
            return mTransferMethodTypes.size();
        }

        @Override
        public void onViewRecycled(@NonNull ViewHolder holder) {
            holder.recycle();
        }

        void replaceData(List<TransferMethodSelectionItem> transferMethodTypes) {
            mTransferMethodTypes = transferMethodTypes;
            notifyDataSetChanged();
        }

        TransferMethodSelectionItem getItem(int position) {
            return mTransferMethodTypes.get(position);
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView mTitle;
            final TextView mDescriptionProcessingTime;
            final TextView mDescriptionFees;
            final TextView mIcon;
            final TransferMethodSelectionItemListener mTransferMethodSelectionItemListener;

            ViewHolder(@NonNull final View itemView,
                    @NonNull final TransferMethodSelectionItemListener transferMethodSelectionItemListener) {
                super(itemView);
                mTitle = itemView.findViewById(R.id.transfer_method_type_title);
                mIcon = itemView.findViewById(R.id.transfer_method_type_icon);
                mDescriptionFees = itemView.findViewById(R.id.transfer_method_type_description_1);
                mDescriptionProcessingTime = itemView.findViewById(R.id.transfer_method_type_description_2);
                mTransferMethodSelectionItemListener = transferMethodSelectionItemListener;
                itemView.findViewById(R.id.transfer_method_context_button).setVisibility(View.GONE);
            }

            @SuppressLint("StringFormatInvalid")
            void bind(TransferMethodSelectionItem selectionItem) {
                mTitle.setText(
                        getStringResourceByName(mTitle.getContext(), selectionItem.getTransferMethodType()));
                mIcon.setText(
                        getStringFontIcon(mIcon.getContext(), selectionItem.getTransferMethodType()));

                if (selectionItem.getFees() != null && !selectionItem.getFees().isEmpty()) {
                    String formattedFee = FeeFormatter.getFormattedFee(mTitle.getContext(), selectionItem.getFees());
                    mDescriptionFees.setText(mDescriptionFees.getContext()
                            .getString(R.string.select_transfer_method_item_fee_information, formattedFee));
                } else {
                    mDescriptionFees.setVisibility(View.GONE);
                }

                if (!TextUtils.isEmpty(selectionItem.getProcessingTime())) {
                    mDescriptionProcessingTime.setText(mDescriptionProcessingTime.getContext()
                            .getString(R.string.select_transfer_method_item_processing_time_information,
                                    selectionItem.getProcessingTime()));
                } else {
                    mDescriptionProcessingTime.setVisibility(View.INVISIBLE);
                }

                itemView.setOnClickListener(new OneClickListener() {

                    @Override
                    public void onOneClick(View v) {
                        handleOneClick();
                    }
                });
            }

            void recycle() {
                itemView.setOnClickListener(null);
            }

            private void handleOneClick() {
                int position = getAdapterPosition();
                TransferMethodSelectionItem transferMethodType = getItem(position);
                mTransferMethodSelectionItemListener.onTransferMethodSelected(transferMethodType);
            }
        }
    }
}
