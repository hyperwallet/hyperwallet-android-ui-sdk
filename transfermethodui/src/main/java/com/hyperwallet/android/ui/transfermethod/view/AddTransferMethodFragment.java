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
package com.hyperwallet.android.ui.transfermethod.view;

import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.PROFILE_TYPE;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.TRANSFER_METHOD_COUNTRY;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.TRANSFER_METHOD_CURRENCY;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.TYPE;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodTypes.BANK_ACCOUNT;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodTypes.BANK_CARD;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodTypes.PAYPAL_ACCOUNT;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodTypes.VENMO_ACCOUNT;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodTypes.WIRE_ACCOUNT;
import static com.hyperwallet.android.ui.common.intent.HyperwalletIntent.EXTRA_TRANSFER_METHOD_ADDED;
import static com.hyperwallet.android.ui.transfermethod.TransferMethodLocalBroadcast.TransferMethodLocalBroadcastAction.ACTION_HYPERWALLET_TRANSFER_METHOD_ADDED;
import static com.hyperwallet.android.ui.transfermethod.view.FeeFormatter.isFeeAvailable;
import static com.hyperwallet.android.ui.transfermethod.view.FeeFormatter.isProcessingTimeAvailable;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.hyperwallet.android.exception.HyperwalletException;
import com.hyperwallet.android.model.Error;
import com.hyperwallet.android.model.graphql.Fee;
import com.hyperwallet.android.model.graphql.ProcessingTime;
import com.hyperwallet.android.model.graphql.field.Field;
import com.hyperwallet.android.model.graphql.field.FieldGroup;
import com.hyperwallet.android.model.transfermethod.BankAccount;
import com.hyperwallet.android.model.transfermethod.BankCard;
import com.hyperwallet.android.model.transfermethod.PayPalAccount;
import com.hyperwallet.android.model.transfermethod.TransferMethod;
import com.hyperwallet.android.model.transfermethod.VenmoAccount;
import com.hyperwallet.android.ui.R;
import com.hyperwallet.android.ui.common.insight.HyperwalletInsight;
import com.hyperwallet.android.ui.common.util.ErrorTypes;
import com.hyperwallet.android.ui.common.util.PageGroups;
import com.hyperwallet.android.ui.transfermethod.TransferMethodLocalBroadcast;
import com.hyperwallet.android.ui.transfermethod.repository.TransferMethodRepositoryFactory;
import com.hyperwallet.android.ui.transfermethod.view.widget.AbstractWidget;
import com.hyperwallet.android.ui.transfermethod.view.widget.DateChangedListener;
import com.hyperwallet.android.ui.transfermethod.view.widget.DateWidget;
import com.hyperwallet.android.ui.transfermethod.view.widget.WidgetEventListener;
import com.hyperwallet.android.ui.transfermethod.view.widget.WidgetFactory;
import com.hyperwallet.android.ui.transfermethod.view.widget.WidgetInputState;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

public class AddTransferMethodFragment extends Fragment implements WidgetEventListener, AddTransferMethodContract.View {

    public static final String TAG = AddTransferMethodActivity.TAG;

    private static final String ARGUMENT_TRANSFER_METHOD_COUNTRY = "ARGUMENT_TRANSFER_METHOD_COUNTRY";
    private static final String ARGUMENT_TRANSFER_METHOD_CURRENCY = "ARGUMENT_TRANSFER_METHOD_CURRENCY";
    private static final String ARGUMENT_TRANSFER_METHOD_TYPE = "ARGUMENT_TRANSFER_METHOD_TYPE";
    private static final String ARGUMENT_TRANSFER_METHOD_PROFILE_TYPE = "ARGUMENT_TRANSFER_METHOD_PROFILE_TYPE";
    private static final String ARGUMENT_SHOW_CREATE_PROGRESS_BAR = "ARGUMENT_SHOW_CREATE_PROGRESS_BAR";
    private static final String ARGUMENT_TRANSFER_METHOD = "ARGUMENT_TRANSFER_METHOD";
    private static final String ARGUMENT_WIDGET_STATE_MAP = "ARGUMENT_WIDGET_STATE_MAP";
    private static final boolean FORCE_UPDATE = false;
    private String mCountry;
    private View mCreateButtonProgressBar;
    private Button mCreateTransferMethodButton;
    private String mCurrency;
    private ViewGroup mDynamicContainer;
    private NestedScrollView mNestedScrollView;
    private OnAddTransferMethodNetworkErrorCallback mOnAddTransferMethodNetworkErrorCallback;
    private OnLoadTransferMethodConfigurationFieldsNetworkErrorCallback
            mOnLoadTransferMethodConfigurationFieldsNetworkErrorCallback;
    private AddTransferMethodContract.Presenter mPresenter;
    private View mProgressBar;
    private boolean mShowCreateProgressBar;
    private String mTransferMethodType;
    private TransferMethod mTransferMethod;
    private String mTransferMethodProfileType;
    private HashMap<String, WidgetInputState> mWidgetInputStateHashMap;

    /**
     * Please do not use this to have instance of AddTransferMethodFragment this is reserved for android framework
     */
    public AddTransferMethodFragment() {
    }

    /**
     * Creates new instance of AddTransferMethodFragment this is the proper initialization of this class
     * since the default constructor is reserved for android framework when lifecycle is triggered.
     * The parameters in {@link AddTransferMethodFragment#newInstance(String, String, String, String)} is mandatory
     * and should be supplied with correct data or this fragment will not initialize properly.
     *
     * @param transferMethodCountry     the country selected when creating transfer method
     * @param transferMethodCurrency    the currency selected when creating transfer method
     * @param transferMethodType        the type of transfer method needed to create transfer method
     * @param transferMethodProfileType the type of transfer method profile needed to create transfer method
     */
    public static AddTransferMethodFragment newInstance(@NonNull String transferMethodCountry,
            @NonNull String transferMethodCurrency,
            @NonNull String transferMethodType,
            @NonNull String transferMethodProfileType) {
        AddTransferMethodFragment addTransferMethodFragment = new AddTransferMethodFragment();
        Bundle arguments = new Bundle();

        addTransferMethodFragment.mCountry = transferMethodCountry;
        addTransferMethodFragment.mTransferMethodType = transferMethodType;
        addTransferMethodFragment.mCurrency = transferMethodCurrency;
        addTransferMethodFragment.mTransferMethodProfileType = transferMethodProfileType;
        addTransferMethodFragment.mWidgetInputStateHashMap = new HashMap<>(1);
        addTransferMethodFragment.mTransferMethod = null;

        arguments.putString(ARGUMENT_TRANSFER_METHOD_COUNTRY, addTransferMethodFragment.mCountry);
        arguments.putString(ARGUMENT_TRANSFER_METHOD_CURRENCY, addTransferMethodFragment.mCurrency);
        arguments.putString(ARGUMENT_TRANSFER_METHOD_TYPE, addTransferMethodFragment.mTransferMethodType);
        arguments.putString(ARGUMENT_TRANSFER_METHOD_PROFILE_TYPE,
                addTransferMethodFragment.mTransferMethodProfileType);
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
        mNestedScrollView = view.findViewById(R.id.add_transfer_method_scroll_view);

        mCreateButtonProgressBar = view.findViewById(R.id.add_transfer_method_create_button_progress_bar);
        mProgressBar = view.findViewById(R.id.add_transfer_method_progress_bar_layout);
        mCreateTransferMethodButton = view.findViewById(R.id.add_transfer_method_button);

        mCreateTransferMethodButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        mCreateTransferMethodButton.setTextColor(getResources().getColor(R.color.regularColorPrimary));
        mCreateTransferMethodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HyperwalletInsight.getInstance().trackClick(requireContext(),
                        TAG, PageGroups.TRANSFER_METHOD,
                        HyperwalletInsight.LINK_SELECT_TRANSFER_METHOD_CREATE,
                        new HyperwalletInsight.TransferMethodParamsBuilder()
                                .country(mCountry)
                                .currency(mCurrency)
                                .type(mTransferMethodType)
                                .profileType(mTransferMethodProfileType)
                                .build());

                triggerSubmit();
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        TransferMethodRepositoryFactory factory = TransferMethodRepositoryFactory.getInstance();
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
            mTransferMethodProfileType = savedInstanceState.getString(ARGUMENT_TRANSFER_METHOD_PROFILE_TYPE);
            mShowCreateProgressBar = savedInstanceState.getBoolean(ARGUMENT_SHOW_CREATE_PROGRESS_BAR);
            mTransferMethod = savedInstanceState.getParcelable(ARGUMENT_TRANSFER_METHOD);
        } else { // same as AddTransferMethodFragment#newInstance
            mWidgetInputStateHashMap = (HashMap) getArguments().getSerializable(ARGUMENT_WIDGET_STATE_MAP);
            mCountry = getArguments().getString(ARGUMENT_TRANSFER_METHOD_COUNTRY);
            mCurrency = getArguments().getString(ARGUMENT_TRANSFER_METHOD_CURRENCY);
            mTransferMethodType = getArguments().getString(ARGUMENT_TRANSFER_METHOD_TYPE);
            mTransferMethodProfileType = getArguments().getString(ARGUMENT_TRANSFER_METHOD_PROFILE_TYPE);
            mTransferMethod = getArguments().getParcelable(ARGUMENT_TRANSFER_METHOD);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.loadTransferMethodConfigurationFields(FORCE_UPDATE, mCountry, mCurrency, mTransferMethodType,
                mTransferMethodProfileType);
    }

    @Override
    public void showErrorAddTransferMethod(@NonNull final List<Error> errors) {
        mOnAddTransferMethodNetworkErrorCallback.showErrorsAddTransferMethod(errors);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putSerializable(ARGUMENT_WIDGET_STATE_MAP, mWidgetInputStateHashMap);
        outState.putString(ARGUMENT_TRANSFER_METHOD_COUNTRY, mCountry);
        outState.putString(ARGUMENT_TRANSFER_METHOD_CURRENCY, mCurrency);
        outState.putString(ARGUMENT_TRANSFER_METHOD_TYPE, mTransferMethodType);
        outState.putString(ARGUMENT_TRANSFER_METHOD_PROFILE_TYPE, mTransferMethodProfileType);
        outState.putBoolean(ARGUMENT_SHOW_CREATE_PROGRESS_BAR, mShowCreateProgressBar);
        outState.putParcelable(ARGUMENT_TRANSFER_METHOD, mTransferMethod);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void showErrorLoadTransferMethodConfigurationFields(@NonNull final List<Error> errors) {
        mOnLoadTransferMethodConfigurationFieldsNetworkErrorCallback.showErrorsLoadTransferMethodConfigurationFields(
                errors);
    }

    @Override
    public void retryAddTransferMethod() {
        mPresenter.createTransferMethod(mTransferMethod);
    }

    @Override
    public void reloadTransferMethodConfigurationFields() {
        mPresenter.loadTransferMethodConfigurationFields(FORCE_UPDATE, mCountry, mCurrency, mTransferMethodType,
                mTransferMethodProfileType);
    }

    @Override
    public void valueChanged(@NonNull final AbstractWidget widget) {
        isWidgetItemValid(widget);
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
    public void notifyTransferMethodAdded(@NonNull final TransferMethod transferMethod) {
        HyperwalletInsight.getInstance().trackImpression(requireContext(),
                TAG, PageGroups.TRANSFER_METHOD,
                new HyperwalletInsight.TransferMethodParamsBuilder()
                        .goal(HyperwalletInsight.TRANSFER_METHOD_GOAL)
                        .country(mCountry)
                        .currency(mCurrency)
                        .type(mTransferMethodType)
                        .profileType(mTransferMethodProfileType)
                        .build());

        Intent intent = TransferMethodLocalBroadcast.createBroadcastIntentTransferMethodAdded(
                transferMethod);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);

        Intent activityResult = new Intent();
        activityResult.setAction(ACTION_HYPERWALLET_TRANSFER_METHOD_ADDED);
        activityResult.putExtra(EXTRA_TRANSFER_METHOD_ADDED, transferMethod);
        getActivity().setResult(Activity.RESULT_OK, activityResult);
        getActivity().finish();
    }

    @Override
    public void showTransferMethodFields(@NonNull final List<FieldGroup> fields) {
        mDynamicContainer.removeAllViews();

        try {
            Locale locale = new Locale.Builder().setRegion(mCountry).build();

            // group
            for (FieldGroup group : fields) {
                View sectionHeader = LayoutInflater.from(mDynamicContainer.getContext())
                        .inflate(R.layout.item_widget_section_header, mDynamicContainer, false);
                TextView sectionTitle = sectionHeader.findViewById(R.id.section_header_title);
                sectionTitle.setText(getSectionHeaderText(group, locale));
                sectionHeader.setId(View.generateViewId());
                mDynamicContainer.addView(sectionHeader);

                // group fields
                for (final Field field : group.getFields()) {
                    AbstractWidget widget = WidgetFactory
                            .newWidget(field, this, mWidgetInputStateHashMap.containsKey(field.getName()) ?
                                            mWidgetInputStateHashMap.get(field.getName()).getValue() : field.getValue(),
                                    mCreateTransferMethodButton);
                    if (mWidgetInputStateHashMap.isEmpty() || !mWidgetInputStateHashMap.containsKey(widget.getName())) {
                        mWidgetInputStateHashMap.put(widget.getName(), widget.getWidgetInputState());
                    }

                    View widgetView = widget.getView(mDynamicContainer);
                    widgetView.setTag(widget);
                    widgetView.setId(View.generateViewId());
                    final String error = mWidgetInputStateHashMap.get(widget.getName()).getErrorMessage();
                    widget.showValidationError(error);
                    mDynamicContainer.addView(widgetView);
                }
            }

            HyperwalletInsight.getInstance().trackImpression(requireContext(),
                    TAG, PageGroups.TRANSFER_METHOD,
                    new HyperwalletInsight.TransferMethodParamsBuilder()
                            .country(mCountry)
                            .currency(mCurrency)
                            .type(mTransferMethodType)
                            .profileType(mTransferMethodProfileType)
                            .build());

            if (mShowCreateProgressBar) {
                setVisibleAndDisableFields();
            }
        } catch (HyperwalletException e) {
            throw new IllegalStateException("Widget initialization error: " + e.getMessage());
        }
    }

    private String getSectionHeaderText(@NonNull final FieldGroup group, @NonNull final Locale locale) {
        if (FieldGroup.GroupTypes.ACCOUNT_INFORMATION.equals(group.getGroupName())) {
            return requireContext().getString(R.string.account_information,
                    locale.getDisplayName().toUpperCase(), mCurrency);
        }

        return requireContext().getString(requireContext().getResources()
                .getIdentifier(group.getGroupName().toLowerCase(Locale.ROOT), "string",
                        requireContext().getPackageName()));
    }

    @Override
    public void showTransactionInformation(@NonNull final List<Fee> fees,
            @Nullable final ProcessingTime processingTime) {
        View header = getView().findViewById(R.id.add_transfer_method_static_container_header);
        View container = getView().findViewById(R.id.add_transfer_method_static_container);
        TextView feeAndProcessingTime = getView().findViewById(R.id.add_transfer_method_information);

        if (isFeeAvailable(fees) && isProcessingTimeAvailable(processingTime)) {
            String formattedFee = FeeFormatter.getFormattedFee(header.getContext(), fees);
            feeAndProcessingTime.setVisibility(View.VISIBLE);
            feeAndProcessingTime.setText(
                    feeAndProcessingTime.getContext().getString(R.string.feeAndProcessingTimeInformation, formattedFee,
                            processingTime.getValue()));
        } else if (isFeeAvailable(fees) && !isProcessingTimeAvailable(processingTime)) {
            String formattedFee = FeeFormatter.getFormattedFee(header.getContext(), fees);
            feeAndProcessingTime.setVisibility(View.VISIBLE);
            feeAndProcessingTime.setText(
                    feeAndProcessingTime.getContext().getString(R.string.feeInformation, formattedFee));
        } else if (isProcessingTimeAvailable(processingTime) && !isFeeAvailable(fees)) {
            feeAndProcessingTime.setVisibility(View.VISIBLE);
            feeAndProcessingTime.setText(processingTime.getValue());
        } else {
            feeAndProcessingTime.setVisibility(View.GONE);
        }

        if (feeAndProcessingTime.getVisibility() == View.VISIBLE) {
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
    public void showInputErrors(@NonNull final List<Error> errors) {
        boolean focusSet = false;
        Context context = requireContext();
        Resources resources = context.getResources();
        int pixels = (int) (resources.getDimension(R.dimen.negative_padding) * resources.getDisplayMetrics().density);

        for (Error error : errors) {
            for (int i = 0; i < mDynamicContainer.getChildCount(); i++) {
                View view = mDynamicContainer.getChildAt(i);
                if (view.getTag() instanceof AbstractWidget) {
                    AbstractWidget widget = (AbstractWidget) view.getTag();
                    WidgetInputState widgetInputState = mWidgetInputStateHashMap.get(widget.getName());
                    if (widget.getName().equals(error.getFieldName())) {
                        if (!focusSet) {
                            mNestedScrollView.smoothScrollTo(0, view.getTop() - pixels);
                            focusSet = true;
                        }
                        HyperwalletInsight.getInstance().trackError(context,
                                TAG, PageGroups.TRANSFER_METHOD,
                                new HyperwalletInsight.ErrorParamsBuilder()
                                        .code(error.getCode())
                                        .message(error.getMessage())
                                        .fieldName(error.getFieldName())
                                        .type(ErrorTypes.API_ERROR)
                                        .build());

                        widget.showValidationError(null);
                        widgetInputState.setErrorMessage(null);
                        widget.showValidationError(error.getMessage());
                        widgetInputState.setErrorMessage(error.getMessage());
                        widgetInputState.setHasApiError(true);
                    }else{
                        widget.showValidationError(null);
                        widgetInputState.setErrorMessage(null);
                    }
                }
            }
        }

        mPresenter.handleUnmappedFieldError(mWidgetInputStateHashMap, errors);
    }

    @Override
    public boolean isWidgetSelectionFragmentDialogOpen() {
        return getFragmentManager().findFragmentByTag(WidgetSelectionDialogFragment.TAG) != null;
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

        if (!isWidgetSelectionFragmentDialogOpen()) {
            WidgetSelectionDialogFragment widgetSelectionDialogFragment = WidgetSelectionDialogFragment
                    .newInstance(nameValueMap, selectedLabel, fieldLabel, fieldName);

            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            fragmentTransaction.replace(android.R.id.content, widgetSelectionDialogFragment,
                    WidgetSelectionDialogFragment.TAG);
            fragmentTransaction.addToBackStack(WidgetSelectionDialogFragment.TAG);
            fragmentTransaction.commit();
        }
    }

    private void triggerSubmit() {
        hideSoftKeys();
        if (performValidation()) {
            switch (mTransferMethodType) {
                case BANK_ACCOUNT:
                    mTransferMethod = new BankAccount.Builder()
                            .transferMethodCountry(mCountry)
                            .transferMethodCurrency(mCurrency)
                            .build();
                    break;
                case BANK_CARD:
                    mTransferMethod = new BankCard.Builder().
                            transferMethodCountry(mCountry).transferMethodCurrency(mCurrency).build();
                    break;
                case PAYPAL_ACCOUNT:
                    mTransferMethod = new PayPalAccount.Builder()
                            .transferMethodCountry(mCountry)
                            .transferMethodCurrency(mCurrency)
                            .build();
                    break;
                case WIRE_ACCOUNT:
                    mTransferMethod = new BankAccount.Builder()
                            .transferMethodCountry(mCountry)
                            .transferMethodCurrency(mCurrency)
                            .transferMethodType(WIRE_ACCOUNT)
                            .build();
                    break;
                case VENMO_ACCOUNT:
                    mTransferMethod = new VenmoAccount.Builder()
                            .transferMethodCountry(mCountry)
                            .transferMethodCurrency(mCurrency)
                            .build();
                    break;
                default:
                    mTransferMethod = new TransferMethod();
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

            mTransferMethod.setField(PROFILE_TYPE, mTransferMethodProfileType);
            mPresenter.createTransferMethod(mTransferMethod);
        }
    }

    private void hideSoftKeys() {
        View view = requireActivity().getCurrentFocus();

        if (view != null) {
            view.clearFocus();
            InputMethodManager inputMethodManager = (InputMethodManager) view.getContext().getSystemService(
                    Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    void onWidgetSelectionItemClicked(@NonNull final String selectedValue, @NonNull final String fieldName) {
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

    /**
     * Use this to perform validation on an entire form, typically used during form submission.
     * @return true if the form is valid
     */
    private boolean performValidation() {
        boolean containsInvalidWidget = false;

        // this is added since some phones triggers the create button but the widgets are not yet initialized
        boolean hasWidget = false;
        Resources resources = requireContext().getResources();
        int pixels = (int) (resources.getDimension(R.dimen.negative_padding) * resources.getDisplayMetrics().density);

        for (int i = 0; i < mDynamicContainer.getChildCount(); i++) {
            View currentView = mDynamicContainer.getChildAt(i);
            if (currentView.getTag() instanceof AbstractWidget) {
                hasWidget = true;

                AbstractWidget widget = (AbstractWidget) currentView.getTag();
                WidgetInputState widgetInputState = mWidgetInputStateHashMap.get(widget.getName());
                widgetInputState.setValue(widget.getValue());

                if (!isWidgetItemValid(widget) && !containsInvalidWidget) {
                    containsInvalidWidget = true;
                    mNestedScrollView.smoothScrollTo(0, currentView.getTop() - pixels);
                }
            }
        }
        return hasWidget && !containsInvalidWidget;
    }

    /**
     * Use this to perform validation on a single widget item, typically used while the user is inputting data.
     *
     * @param widget the widget to validate
     * @return true if the input is valid
     */
    private boolean isWidgetItemValid(@NonNull final AbstractWidget widget) {
        boolean valid = true;
        Context context = requireContext();

        WidgetInputState widgetInputState = mWidgetInputStateHashMap.get(widget.getName());
        widgetInputState.setValue(widget.getValue());
        if (widget.isValid()) {
            if (!widgetInputState.hasApiError()) {
                widgetInputState.setErrorMessage(null);
                widget.showValidationError(null);
            }
        } else {
            HyperwalletInsight.getInstance().trackError(context,
                    TAG, PageGroups.TRANSFER_METHOD,
                    new HyperwalletInsight.ErrorParamsBuilder()
                            .message(widget.getErrorMessage())
                            .fieldName(widget.getName())
                            .type(ErrorTypes.FORM_ERROR)
                            .addAll(new HyperwalletInsight.TransferMethodParamsBuilder()
                                    .country(mCountry)
                                    .currency(mCurrency)
                                    .type(mTransferMethodType)
                                    .profileType(mTransferMethodProfileType)
                                    .build())
                            .build());

            valid = false;
            widget.showValidationError(null);
            widgetInputState.setErrorMessage(null);
            widget.showValidationError(widget.getErrorMessage());
            widgetInputState.setErrorMessage(widget.getErrorMessage());
            widgetInputState.setHasApiError(false);
        }
        return valid;
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void openWidgetDateDialog(@Nullable final String date, @NonNull final String fieldName) {
        if (getFragmentManager() != null) {
            WidgetDateDialogFragment dateDialogFragment = (WidgetDateDialogFragment)
                    getFragmentManager().findFragmentByTag(WidgetDateDialogFragment.TAG);

            if (dateDialogFragment == null) {
                dateDialogFragment = WidgetDateDialogFragment.newInstance(date, fieldName);
            }

            if (!dateDialogFragment.isAdded()) {
                dateDialogFragment.show(getFragmentManager());
            }
        }
    }

    void onDateSelected(@NonNull final String selectedValue, @NonNull final String fieldName) {
        for (int i = 0; i < mDynamicContainer.getChildCount(); i++) {
            View view = mDynamicContainer.getChildAt(i);
            if (view.getTag() instanceof DateWidget) {
                AbstractWidget widget = (AbstractWidget) view.getTag();
                if (fieldName.equals(widget.getName()) && widget instanceof DateChangedListener) {
                    ((DateChangedListener) view.getTag()).onUpdate(selectedValue);
                    return;
                }
            }
        }
    }

    interface OnLoadTransferMethodConfigurationFieldsNetworkErrorCallback {
        void showErrorsLoadTransferMethodConfigurationFields(@NonNull final List<Error> errors);
    }

    interface OnAddTransferMethodNetworkErrorCallback {
        void showErrorsAddTransferMethod(@NonNull final List<Error> errors);
    }
}
