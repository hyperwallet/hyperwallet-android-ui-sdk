/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Hyperwallet Systems Inc.
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
package com.hyperwallet.android.ui.transfermethod;

import static com.hyperwallet.android.model.HyperwalletTransferMethod.TransferMethodFields.TRANSFER_METHOD_COUNTRY;
import static com.hyperwallet.android.model.HyperwalletTransferMethod.TransferMethodFields.TRANSFER_METHOD_CURRENCY;
import static com.hyperwallet.android.model.HyperwalletTransferMethod.TransferMethodFields.TYPE;
import static com.hyperwallet.android.model.HyperwalletTransferMethod.TransferMethodTypes.BANK_ACCOUNT;
import static com.hyperwallet.android.model.HyperwalletTransferMethod.TransferMethodTypes.BANK_CARD;
import static com.hyperwallet.android.model.HyperwalletTransferMethod.TransferMethodTypes.PAYPAL_ACCOUNT;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.hyperwallet.android.exception.HyperwalletException;
import com.hyperwallet.android.hyperwallet_ui.R;
import com.hyperwallet.android.model.HyperwalletBankAccount;
import com.hyperwallet.android.model.HyperwalletBankCard;
import com.hyperwallet.android.model.HyperwalletError;
import com.hyperwallet.android.model.HyperwalletTransferMethod;
import com.hyperwallet.android.model.PayPalAccount;
import com.hyperwallet.android.model.meta.Fee;
import com.hyperwallet.android.model.meta.HyperwalletField;
import com.hyperwallet.android.ui.HyperwalletLocalBroadcast;
import com.hyperwallet.android.ui.repository.RepositoryFactory;
import com.hyperwallet.android.ui.view.WidgetSelectionDialogFragment;
import com.hyperwallet.android.ui.view.widget.AbstractWidget;
import com.hyperwallet.android.ui.view.widget.WidgetEventListener;
import com.hyperwallet.android.ui.view.widget.WidgetFactory;
import com.hyperwallet.android.ui.view.widget.WidgetInputState;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

public class AddTransferMethodFragment extends Fragment implements WidgetEventListener, AddTransferMethodContract.View {

    public static final String TAG = AddTransferMethodFragment.class.getName();
    private static final String ARGUMENT_TRANSFER_METHOD_COUNTRY = "ARGUMENT_TRANSFER_METHOD_COUNTRY";
    private static final String ARGUMENT_TRANSFER_METHOD_CURRENCY = "ARGUMENT_TRANSFER_METHOD_CURRENCY";
    private static final String ARGUMENT_TRANSFER_METHOD_TYPE = "ARGUMENT_TRANSFER_METHOD_TYPE";
    private static final String ARGUMENT_SHOW_CREATE_PROGRESS_BAR = "ARGUMENT_SHOW_CREATE_PROGRESS_BAR";
    private static final String ARGUMENT_TRANSFER_METHOD = "ARGUMENT_TRANSFER_METHOD";
    private static final String ARGUMENT_WIDGET_STATE_MAP = "ARGUMENT_WIDGET_STATE_MAP";
    private static final boolean FORCE_UPDATE = false;

    private String mCountry;
    private View mCreateButtonProgressBar;
    private Button mCreateTransferMethodButton;
    private String mCurrency;
    private ViewGroup mDynamicContainer;
    private OnAddTransferMethodNetworkErrorCallback mOnAddTransferMethodNetworkErrorCallback;
    private OnLoadTransferMethodConfigurationFieldsNetworkErrorCallback
            mOnLoadTransferMethodConfigurationFieldsNetworkErrorCallback;
    private AddTransferMethodContract.Presenter mPresenter;
    private View mProgressBar;
    private boolean mShowCreateProgressBar;
    private String mTransferMethodType;
    private HyperwalletTransferMethod mTransferMethod;
    private HashMap<String, WidgetInputState> mWidgetInputStateHashMap;
    private TextView sectionHeaderTextView;

    /**
     * Please do not use this to have instance of AddTransferMethodFragment this is reserved for android framework
     */
    public AddTransferMethodFragment() {
    }

    /**
     * Creates new instance of AddTransferMethodFragment this is the proper initialization of this class
     * since the default constructor is reserved for android framework when lifecycle is triggered.
     * The parameters in {@link AddTransferMethodFragment#newInstance(String, String, String)} is mandatory
     * and should be supplied with correct data or this fragment will not initialize properly.
     *
     * @param transferMethodCountry  the country selected when creating transfer method
     * @param transferMethodCurrency the currency selected when creating transfer method
     * @param transferMethodType     the type of transfer method needed to create transfer method
     */
    public static AddTransferMethodFragment newInstance(@NonNull String transferMethodCountry,
            @NonNull String transferMethodCurrency,
            @NonNull String transferMethodType) {
        AddTransferMethodFragment addTransferMethodFragment = new AddTransferMethodFragment();
        Bundle arguments = new Bundle();

        addTransferMethodFragment.mCountry = transferMethodCountry;
        addTransferMethodFragment.mTransferMethodType = transferMethodType;
        addTransferMethodFragment.mCurrency = transferMethodCurrency;
        addTransferMethodFragment.mWidgetInputStateHashMap = new HashMap<>(1);
        addTransferMethodFragment.mTransferMethod = null;

        arguments.putString(ARGUMENT_TRANSFER_METHOD_COUNTRY, addTransferMethodFragment.mCountry);
        arguments.putString(ARGUMENT_TRANSFER_METHOD_CURRENCY, addTransferMethodFragment.mCurrency);
        arguments.putString(ARGUMENT_TRANSFER_METHOD_TYPE, addTransferMethodFragment.mTransferMethodType);
        arguments.putParcelable(ARGUMENT_TRANSFER_METHOD, addTransferMethodFragment.mTransferMethod);
        arguments.putSerializable(ARGUMENT_WIDGET_STATE_MAP, addTransferMethodFragment.mWidgetInputStateHashMap);
        addTransferMethodFragment.setArguments(arguments);

        return addTransferMethodFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mOnAddTransferMethodNetworkErrorCallback = (OnAddTransferMethodNetworkErrorCallback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement "
                    + OnAddTransferMethodNetworkErrorCallback.class.getCanonicalName());
        }

        try {
            mOnLoadTransferMethodConfigurationFieldsNetworkErrorCallback =
                    (OnLoadTransferMethodConfigurationFieldsNetworkErrorCallback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement "
                    + OnLoadTransferMethodConfigurationFieldsNetworkErrorCallback.class.getCanonicalName());
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
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_transfer_method, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDynamicContainer = view.findViewById(R.id.add_transfer_method_dynamic_container);

        sectionHeaderTextView = view.findViewById(R.id.account_information_section_header);

        mCreateButtonProgressBar = view.findViewById(R.id.add_transfer_method_create_button_progress_bar);
        mProgressBar = view.findViewById(R.id.add_transfer_method_progress_bar_layout);

        mCreateTransferMethodButton = view.findViewById(R.id.add_transfer_method_button);
        mCreateTransferMethodButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        mCreateTransferMethodButton.setTextColor(getResources().getColor(R.color.regularColorPrimary));
        mCreateTransferMethodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                triggerSubmit();
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        RepositoryFactory factory = RepositoryFactory.getInstance();
        mPresenter = new AddTransferMethodPresenter(this,
                factory.getTransferMethodConfigurationRepository(),
                factory.getTransferMethodRepository());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null) {
            mWidgetInputStateHashMap = (HashMap) savedInstanceState.getSerializable(ARGUMENT_WIDGET_STATE_MAP);
            mCountry = savedInstanceState.getString(ARGUMENT_TRANSFER_METHOD_COUNTRY);
            mCurrency = savedInstanceState.getString(ARGUMENT_TRANSFER_METHOD_CURRENCY);
            mTransferMethodType = savedInstanceState.getString(ARGUMENT_TRANSFER_METHOD_TYPE);
            mShowCreateProgressBar = savedInstanceState.getBoolean(ARGUMENT_SHOW_CREATE_PROGRESS_BAR);
            mTransferMethod = savedInstanceState.getParcelable(ARGUMENT_TRANSFER_METHOD);
        } else { // same as AddTransferMethodFragment#newInstance
            mWidgetInputStateHashMap = (HashMap) getArguments().getSerializable(ARGUMENT_WIDGET_STATE_MAP);
            mCountry = getArguments().getString(ARGUMENT_TRANSFER_METHOD_COUNTRY);
            mCurrency = getArguments().getString(ARGUMENT_TRANSFER_METHOD_CURRENCY);
            mTransferMethodType = getArguments().getString(ARGUMENT_TRANSFER_METHOD_TYPE);
            mTransferMethod = getArguments().getParcelable(ARGUMENT_TRANSFER_METHOD);
        }

        Locale locale = new Locale.Builder().setRegion(mCountry).build();
        sectionHeaderTextView.setText(requireContext().getResources()
                .getString(R.string.account_information_section_header, locale.getDisplayName(), mCurrency));
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.loadTransferMethodConfigurationFields(FORCE_UPDATE, mCountry, mCurrency, mTransferMethodType);
    }

    @Override
    public void showErrorAddTransferMethod(@NonNull final List<HyperwalletError> errors) {
        mOnAddTransferMethodNetworkErrorCallback.showErrorsAddTransferMethod(errors);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putSerializable(ARGUMENT_WIDGET_STATE_MAP, mWidgetInputStateHashMap);
        outState.putString(ARGUMENT_TRANSFER_METHOD_COUNTRY, mCountry);
        outState.putString(ARGUMENT_TRANSFER_METHOD_CURRENCY, mCurrency);
        outState.putString(ARGUMENT_TRANSFER_METHOD_TYPE, mTransferMethodType);
        outState.putBoolean(ARGUMENT_SHOW_CREATE_PROGRESS_BAR, mShowCreateProgressBar);
        outState.putParcelable(ARGUMENT_TRANSFER_METHOD, mTransferMethod);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void showErrorLoadTransferMethodConfigurationFields(@NonNull final List<HyperwalletError> errors) {
        mOnLoadTransferMethodConfigurationFieldsNetworkErrorCallback.showErrorsLoadTransferMethodConfigurationFields(
                errors);
    }

    @Override
    public void retryAddTransferMethod() {
        mPresenter.createTransferMethod(mTransferMethod);
    }

    @Override
    public void reloadTransferMethodConfigurationFields() {
        mPresenter.loadTransferMethodConfigurationFields(FORCE_UPDATE, mCountry, mCurrency, mTransferMethodType);
    }

    @Override
    public void valueChanged() {
        performValidation(false);
    }

    @Override
    public void widgetFocused(@NonNull final String fieldName) {
        WidgetInputState widgetInputState = mWidgetInputStateHashMap.get(fieldName);
        widgetInputState.setHasFocused(true);
    }

    @Override
    public void saveTextChanged(@NonNull final String fieldName, @NonNull final String value) {
        WidgetInputState inputState = mWidgetInputStateHashMap.get(fieldName);
        if (inputState.hasApiError()) {
            String oldValue = inputState.getValue();
            if (!TextUtils.isEmpty(oldValue) && !oldValue.equals(value)) {
                inputState.setHasApiError(false);
            }
        }
        inputState.setValue(value);
    }

    @Override
    public void notifyTransferMethodAdded(@NonNull final HyperwalletTransferMethod transferMethod) {
        Intent intent = HyperwalletLocalBroadcast.createBroadcastIntentTransferMethodAdded(transferMethod);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
        getActivity().setResult(Activity.RESULT_OK);
        getActivity().finish();
    }

    @Override
    public void showTransferMethodFields(@NonNull final List<HyperwalletField> fields) {
        mDynamicContainer.removeAllViews();
        int previousView = 0;
        try {
            for (final HyperwalletField field : fields) {
                AbstractWidget widget = WidgetFactory
                        .newWidget(field, this, getContext(),
                                mWidgetInputStateHashMap.containsKey(field.getName()) ?
                                        mWidgetInputStateHashMap.get(field.getName()).getValue() : "",
                                mCreateTransferMethodButton);
                if (mWidgetInputStateHashMap.isEmpty() || !mWidgetInputStateHashMap.containsKey(widget.getName())) {
                    mWidgetInputStateHashMap.put(widget.getName(), widget.getWidgetInputState());
                }

                View widgetView = widget.getView();
                widgetView.setTag(widget);
                previousView = placeBelow(widgetView, previousView, true);
                final String error = mWidgetInputStateHashMap.get(widget.getName()).getErrorMessage();
                widget.showValidationError(error);
                mDynamicContainer.addView(widgetView);
            }

            if (mShowCreateProgressBar) {
                setVisibleAndDisableFields();
            }
        } catch (HyperwalletException e) {
            throw new IllegalStateException("Widget initialization error: " + e.getMessage());
        }
    }

    @Override
    public void showTransactionInformation(@NonNull final List<Fee> fees, @Nullable final String processingTime) {
        View header = getView().findViewById(R.id.add_transfer_method_static_container_header);
        View container = getView().findViewById(R.id.add_transfer_method_static_container);
        View feeLabel = getView().findViewById(R.id.add_transfer_method_fee_label);
        TextView feeValue = getView().findViewById(R.id.add_transfer_method_fee_value);
        View processingTimeLabel = getView().findViewById(R.id.add_transfer_method_processing_label);
        TextView processingTimeValue = getView().findViewById(R.id.add_transfer_method_processing_time_value);

        int defaultMargin = (int) requireContext().getResources().getDimension(R.dimen.default_margin);

        if (!fees.isEmpty()) {
            String formattedFee = FeeFormatter.getFormattedFee(requireContext(), fees);
            feeValue.setText(formattedFee);
            feeLabel.setVisibility(View.VISIBLE);
            feeValue.setVisibility(View.VISIBLE);
        } else {
            feeLabel.setVisibility(View.GONE);
            feeValue.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(processingTime)) {
            processingTimeValue.setText(processingTime);
            processingTimeLabel.setVisibility(View.VISIBLE);
            processingTimeValue.setVisibility(View.VISIBLE);
            if (feeValue.getVisibility() == View.GONE) {
                // we need to set new margin parameters when top views are not visible
                RelativeLayout.LayoutParams labelLayout =
                        (RelativeLayout.LayoutParams) processingTimeLabel.getLayoutParams();
                RelativeLayout.LayoutParams valueLayout =
                        (RelativeLayout.LayoutParams) processingTimeValue.getLayoutParams();
                labelLayout.setMargins(defaultMargin, defaultMargin, 0, defaultMargin);
                valueLayout.setMargins(defaultMargin, defaultMargin, 0, 0);
            }
        } else {
            if (feeValue.getVisibility() == View.VISIBLE) {
                // add margin to fees when only fee is shown
                RelativeLayout.LayoutParams labelLayout = (RelativeLayout.LayoutParams) feeLabel.getLayoutParams();
                RelativeLayout.LayoutParams valueLayout = (RelativeLayout.LayoutParams) feeValue.getLayoutParams();
                labelLayout.setMargins(defaultMargin, defaultMargin, 0, defaultMargin);
                valueLayout.setMargins(defaultMargin, defaultMargin, 0, 0);
            }
            processingTimeLabel.setVisibility(View.GONE);
            processingTimeValue.setVisibility(View.GONE);
        }

        if (feeValue.getVisibility() == View.VISIBLE || processingTimeValue.getVisibility() == View.VISIBLE) {
            header.setVisibility(View.VISIBLE);
            container.setVisibility(View.VISIBLE);
        } else {
            header.setVisibility(View.GONE);
            container.setVisibility(View.GONE);
        }
    }

    @Override
    public void showCreateButtonProgressBar() {
        mShowCreateProgressBar = true;
        setVisibleAndDisableFields();
    }

    @Override
    public void hideCreateButtonProgressBar() {
        mShowCreateProgressBar = false;
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        mCreateButtonProgressBar.setVisibility(View.GONE);
        mCreateTransferMethodButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        mCreateTransferMethodButton.setTextColor(getResources().getColor(R.color.regularColorPrimary));
    }

    @Override
    public void showProgressBar() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgressBar() {
        mProgressBar.setVisibility(View.GONE);
        mCreateTransferMethodButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void showInputErrors(List<HyperwalletError> errors) {
        boolean focusSet = false;
        for (HyperwalletError error : errors) {
            for (int i = 0; i < mDynamicContainer.getChildCount(); i++) {
                View view = mDynamicContainer.getChildAt(i);
                if (view.getTag() instanceof AbstractWidget) {
                    AbstractWidget widget = (AbstractWidget) view.getTag();
                    if (widget.getName().equals(error.getFieldName())) {
                        if (!focusSet) {
                            widget.getView().requestFocus();
                            focusSet = true;
                        }
                        widget.showValidationError(error.getMessage());
                        WidgetInputState widgetInputState = mWidgetInputStateHashMap.get(widget.getName());
                        widgetInputState.setErrorMessage(error.getMessage());
                        widgetInputState.setHasApiError(true);
                    }
                }
            }
        }
    }

    @Override
    public void openWidgetSelectionFragmentDialog(@NonNull final TreeMap<String, String> nameValueMap,
            @NonNull final String selectedName, @NonNull final String fieldLabel, @NonNull final String fieldName) {
        String selectedLabel = selectedName;
        if (TextUtils.isEmpty(selectedLabel)) {
            selectedLabel = mWidgetInputStateHashMap.get(fieldName).getSelectedName();
        } else {
            mWidgetInputStateHashMap.get(fieldName).setSelectedName(selectedLabel);
        }

        getFragmentManager().popBackStack(WidgetSelectionDialogFragment.TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        WidgetSelectionDialogFragment widgetSelectionDialogFragment = WidgetSelectionDialogFragment
                .newInstance(nameValueMap, selectedLabel, fieldLabel, fieldName);

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.replace(android.R.id.content, widgetSelectionDialogFragment,
                WidgetSelectionDialogFragment.TAG);
        fragmentTransaction.addToBackStack(WidgetSelectionDialogFragment.TAG);
        fragmentTransaction.commit();
    }

    interface OnLoadTransferMethodConfigurationFieldsNetworkErrorCallback {
        void showErrorsLoadTransferMethodConfigurationFields(@NonNull final List<HyperwalletError> errors);
    }

    interface OnAddTransferMethodNetworkErrorCallback {
        void showErrorsAddTransferMethod(@NonNull final List<HyperwalletError> errors);
    }

    private void triggerSubmit() {
        if (performValidation(true)) {
            switch (mTransferMethodType) {
                case BANK_ACCOUNT:
                    mTransferMethod = new HyperwalletBankAccount.Builder()
                            .transferMethodCountry(mCountry)
                            .transferMethodCurrency(mCurrency)
                            .build();
                    break;
                case BANK_CARD:
                    mTransferMethod = new HyperwalletBankCard.Builder().
                            transferMethodCountry(mCountry).transferMethodCurrency(mCurrency).build();
                    break;
                case PAYPAL_ACCOUNT:
                    mTransferMethod = new PayPalAccount.Builder()
                            .transferMethodCountry(mCountry)
                            .transferMethodCurrency(mCurrency)
                            .build();
                    break;
                default:
                    mTransferMethod = new HyperwalletTransferMethod();
                    mTransferMethod.setField(TRANSFER_METHOD_COUNTRY, mCountry);
                    mTransferMethod.setField(TRANSFER_METHOD_CURRENCY, mCurrency);
                    mTransferMethod.setField(TYPE, mTransferMethodType);
            }

            for (int i = 0; i < mDynamicContainer.getChildCount(); i++) {
                View view = mDynamicContainer.getChildAt(i);
                if (view.getTag() instanceof AbstractWidget) {
                    AbstractWidget widget = (AbstractWidget) view.getTag();
                    mTransferMethod.setField(widget.getName(), widget.getValue());
                }
            }
            mPresenter.createTransferMethod(mTransferMethod);
        }
    }

    protected void onWidgetSelectionItemClicked(@NonNull final String selectedValue, @NonNull final String fieldName) {
        for (int i = 0; i < mDynamicContainer.getChildCount(); i++) {
            View view = mDynamicContainer.getChildAt(i);
            if (view.getTag() instanceof WidgetSelectionDialogFragment.WidgetSelectionItemType) {
                AbstractWidget widget = (AbstractWidget) view.getTag();
                if (fieldName.equals(widget.getName())) {
                    ((WidgetSelectionDialogFragment.WidgetSelectionItemType) view.getTag())
                            .onWidgetSelectionItemClicked(selectedValue);
                    return;
                }
            }
        }
    }

    private void setVisibleAndDisableFields() {
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        mCreateButtonProgressBar.setVisibility(View.VISIBLE);
        mCreateTransferMethodButton.setBackgroundColor(getResources().getColor(R.color.colorSecondaryDark));
    }

    private int placeBelow(View v, int previousId, boolean matchParentWidth) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                matchParentWidth ? ViewGroup.LayoutParams.MATCH_PARENT : ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        if (previousId != 0) {
            params.addRule(RelativeLayout.BELOW, previousId);
        }

        int margin = (int) (getContext().getResources().getDimension(R.dimen.default_margin)
                / getContext().getResources().getDisplayMetrics().density);

        params.setMargins(margin, margin, margin, margin);
        v.setId(View.generateViewId());
        v.setLayoutParams(params);

        return v.getId();
    }

    private boolean performValidation(boolean bypassFocusCheck) {
        boolean valid = true;
        // this is added since some phones triggers the create button but the widgets are not yet initialized
        boolean hasWidget = false;
        for (int i = 0; i < mDynamicContainer.getChildCount(); i++) {
            View v = mDynamicContainer.getChildAt(i);
            if (v.getTag() instanceof AbstractWidget) {
                hasWidget = true;
                AbstractWidget widget = (AbstractWidget) v.getTag();
                WidgetInputState widgetInputState = mWidgetInputStateHashMap.get(widget.getName());
                widgetInputState.setValue(widget.getValue());
                if (bypassFocusCheck || widgetInputState.hasFocused()) {
                    if (widget.isValid()) {
                        if (!widgetInputState.hasApiError()) {
                            widgetInputState.setErrorMessage(null);
                            widget.showValidationError(null);
                        }
                    } else {
                        valid = false;
                        widget.showValidationError(widget.getErrorMessage());
                        widgetInputState.setErrorMessage(widget.getErrorMessage());
                        widgetInputState.setHasApiError(false);
                    }
                }
            }
        }
        return valid && hasWidget && haveAllWidgetsReceivedFocus();
    }

    private boolean haveAllWidgetsReceivedFocus() {
        for (String key : mWidgetInputStateHashMap.keySet()) {
            if (!mWidgetInputStateHashMap.get(key).hasFocused()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }
}
