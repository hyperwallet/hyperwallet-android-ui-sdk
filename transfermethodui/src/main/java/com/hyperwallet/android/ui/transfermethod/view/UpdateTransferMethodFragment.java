package com.hyperwallet.android.ui.transfermethod.view;

import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.TYPE;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodTypes.BANK_ACCOUNT;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodTypes.BANK_CARD;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodTypes.PAPER_CHECK;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodTypes.PAYPAL_ACCOUNT;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodTypes.VENMO_ACCOUNT;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodTypes.WIRE_ACCOUNT;
import static com.hyperwallet.android.ui.common.intent.HyperwalletIntent.EXTRA_TRANSFER_METHOD_UPDATED;
import static com.hyperwallet.android.ui.transfermethod.TransferMethodLocalBroadcast.TransferMethodLocalBroadcastAction.ACTION_HYPERWALLET_TRANSFER_METHOD_UPDATED;
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
import com.hyperwallet.android.model.graphql.HyperwalletTransferMethodConfigurationField;
import com.hyperwallet.android.model.graphql.ProcessingTime;
import com.hyperwallet.android.model.graphql.field.Field;
import com.hyperwallet.android.model.graphql.field.FieldGroup;
import com.hyperwallet.android.model.transfermethod.BankAccount;
import com.hyperwallet.android.model.transfermethod.BankCard;
import com.hyperwallet.android.model.transfermethod.PaperCheck;
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

public class UpdateTransferMethodFragment extends Fragment implements WidgetEventListener,
        UpdateTransferMethodContract.View {


    public static final String TAG = UpdateTransferMethodActivity.TAG;

    private static final String ARGUMENT_TRANSFER_METHOD_TYPE = "ARGUMENT_TRANSFER_METHOD_TYPE";
    private static final String ARGUMENT_TRANSFER_METHOD_TOKEN = "ARGUMENT_TRANSFER_METHOD_TOKEN";
    private static final String ARGUMENT_SHOW_UPDATE_PROGRESS_BAR = "ARGUMENT_SHOW_UPDATE_PROGRESS_BAR";
    private static final String ARGUMENT_WIDGET_STATE_MAP = "ARGUMENT_WIDGET_STATE_MAP";
    private static final boolean FORCE_UPDATE = false;
    private View mUpdateButtonProgressBar;
    private Button mUpdateTransferMethodButton;
    private ViewGroup mDynamicContainer;
    private NestedScrollView mNestedScrollView;
    private OnUpdateTransferMethodNetworkErrorCallback mOnUpdateTransferMethodNetworkErrorCallback;
    private OnLoadTransferMethodConfigurationFieldsNetworkErrorCallback
            mOnLoadTransferMethodConfigurationFieldsNetworkErrorCallback;
    private UpdateTransferMethodContract.Presenter mPresenter;
    private View mProgressBar;
    private boolean mUpdateProgressBar;
    private String mTransferMethodType;
    private TransferMethod mTransferMethod;
    private String mTransferMethodToken;
    private HashMap<String, WidgetInputState> mWidgetInputStateHashMap;

    /**
     * Please do not use this to have instance of UpdateTransferMethodFragment this is reserved for android framework
     */
    public UpdateTransferMethodFragment() {
    }

    /**
     * Creates new instance of UpdateTransferMethodFragment this is the proper initialization of this class
     * since the default constructor is reserved for android framework when lifecycle is triggered.
     * The parameters in {@link UpdateTransferMethodFragment#newInstance(String, String)} is mandatory
     * and should be supplied with correct data or this fragment will not initialize properly.
     *
     * @param transferMethodToken the country selected when creating transfer method
     */
    public static UpdateTransferMethodFragment newInstance(@NonNull String transferMethodType,
            @NonNull String transferMethodToken) {
        UpdateTransferMethodFragment updateTransferMethodFragment = new UpdateTransferMethodFragment();
        Bundle arguments = new Bundle();

        updateTransferMethodFragment.mTransferMethodType = transferMethodType;
        updateTransferMethodFragment.mTransferMethodToken = transferMethodToken;
        updateTransferMethodFragment.mWidgetInputStateHashMap = new HashMap<>(1);
        updateTransferMethodFragment.mTransferMethod = null;
        arguments.putString(ARGUMENT_TRANSFER_METHOD_TYPE, transferMethodType);
        arguments.putString(ARGUMENT_TRANSFER_METHOD_TOKEN, transferMethodToken);
        arguments.putSerializable(ARGUMENT_WIDGET_STATE_MAP, updateTransferMethodFragment.mWidgetInputStateHashMap);
        updateTransferMethodFragment.setArguments(arguments);

        return updateTransferMethodFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mOnUpdateTransferMethodNetworkErrorCallback = (OnUpdateTransferMethodNetworkErrorCallback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement "
                    + OnUpdateTransferMethodNetworkErrorCallback.class.getCanonicalName());
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
        return inflater.inflate(R.layout.fragment_update_transfer_method, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDynamicContainer = view.findViewById(R.id.update_transfer_method_dynamic_container);
        mNestedScrollView = view.findViewById(R.id.update_transfer_method_scroll_view);

        mUpdateButtonProgressBar = view.findViewById(R.id.update_transfer_method_progress_bar);
        mProgressBar = view.findViewById(R.id.update_transfer_method_progress_bar_layout);
        mUpdateTransferMethodButton = view.findViewById(R.id.update_transfer_method_button);

        mUpdateTransferMethodButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        mUpdateTransferMethodButton.setTextColor(getResources().getColor(R.color.regularColorPrimary));
        mUpdateTransferMethodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HyperwalletInsight.getInstance().trackClick(requireContext(),
                        TAG, PageGroups.TRANSFER_METHOD,
                        HyperwalletInsight.LINK_SELECT_TRANSFER_METHOD_CREATE,
                        new HyperwalletInsight.TransferMethodParamsBuilder()
//                                .country(mCountry)
//                                .currency(mCurrency)
                                .type(mTransferMethodType)
//                                .profileType(mTransferMethodProfileType)
                                .build());

                triggerUpdate();
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        TransferMethodRepositoryFactory factory = TransferMethodRepositoryFactory.getInstance();
        mPresenter = new UpdateTransferMethodPresenter(this,
                factory.getUpdateTransferMethodConfigurationRepository(),
                factory.getTransferMethodRepository());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null) {
            mWidgetInputStateHashMap = (HashMap) savedInstanceState.getSerializable(ARGUMENT_WIDGET_STATE_MAP);
            mTransferMethodType = savedInstanceState.getString(ARGUMENT_TRANSFER_METHOD_TYPE);
            mTransferMethodToken = savedInstanceState.getString(ARGUMENT_TRANSFER_METHOD_TOKEN);
            mUpdateProgressBar = savedInstanceState.getBoolean(ARGUMENT_SHOW_UPDATE_PROGRESS_BAR);
            mTransferMethod = savedInstanceState.getParcelable(ARGUMENT_TRANSFER_METHOD_TOKEN);
        } else { // same as UpdateTransferMethodFragment#newInstance
            mWidgetInputStateHashMap = (HashMap) getArguments().getSerializable(ARGUMENT_WIDGET_STATE_MAP);
            mTransferMethodType = getArguments().getString(ARGUMENT_TRANSFER_METHOD_TYPE);
            mTransferMethodToken = getArguments().getString(ARGUMENT_TRANSFER_METHOD_TOKEN);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.loadTransferMethodConfigurationFields(FORCE_UPDATE, mTransferMethodType, mTransferMethodToken);
    }

    @Override
    public void showErrorUpdateTransferMethod(@NonNull final List<Error> errors) {
        mOnUpdateTransferMethodNetworkErrorCallback.showErrorsUpdateTransferMethod(errors);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putSerializable(ARGUMENT_WIDGET_STATE_MAP, mWidgetInputStateHashMap);
        outState.putString(ARGUMENT_TRANSFER_METHOD_TOKEN, mTransferMethodToken);
        outState.putBoolean(ARGUMENT_SHOW_UPDATE_PROGRESS_BAR, mUpdateProgressBar);
        super.onSaveInstanceState(outState);
    }

    private void triggerUpdate() {
        hideSoftKeys();
        if (performValidation()) {
            switch (mTransferMethodType) {
                case BANK_ACCOUNT:
                    mTransferMethod = new BankAccount.Builder()
                            .token(mTransferMethodToken)
                            .build();
                    break;
                case BANK_CARD:
                    mTransferMethod = new BankCard.Builder()
                            .token(mTransferMethodToken)
                            .build();
                    break;
                case PAYPAL_ACCOUNT:
                    mTransferMethod = new PayPalAccount.Builder()
                            .token(mTransferMethodToken)
                            .build();
                    break;
                case WIRE_ACCOUNT:
                    mTransferMethod = new BankAccount.Builder()
                            .token(mTransferMethodToken)
                            .transferMethodType(WIRE_ACCOUNT)
                            .build();
                    break;
                case VENMO_ACCOUNT:
                    mTransferMethod = new VenmoAccount.Builder()
                            .token(mTransferMethodToken)
                            .build();
                    break;
                case PAPER_CHECK:
                    mTransferMethod = new PaperCheck.Builder()
                            .token(mTransferMethodToken)
                            .build();
                    break;
                default:
                    mTransferMethod = new TransferMethod();
                    mTransferMethod.setField(TYPE, mTransferMethodType);
            }

            for (int i = 0; i < mDynamicContainer.getChildCount(); i++) {
                View view = mDynamicContainer.getChildAt(i);
                if (view.getTag() instanceof AbstractWidget) {
                    AbstractWidget widget = (AbstractWidget) view.getTag();
                    if (widget.isEdited) {
                        mTransferMethod.setField(widget.getName(), widget.getValue());
                    }
                }
            }

            mPresenter.updateTransferMethod(mTransferMethod);
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


    /**
     * Use this to perform validation on an entire form, typically used during form submission.
     *
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
//                                    .country(mCountry)
//                                    .currency(mCurrency)
                                    .type(mTransferMethodType)
//                                    .profileType(mTransferMethodProfileType)
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
    public void notifyTransferMethodUpdated(@NonNull TransferMethod transferMethod) {
        HyperwalletInsight.getInstance().trackImpression(requireContext(),
                TAG, PageGroups.TRANSFER_METHOD,
                new HyperwalletInsight.TransferMethodParamsBuilder()
                        .goal(HyperwalletInsight.TRANSFER_METHOD_GOAL)
//                        .country(mCountry)
//                        .currency(mCurrency)
                        .type(mTransferMethodType)
//                        .profileType(mTransferMethodProfileType)
                        .build());

        Intent intent = TransferMethodLocalBroadcast.createBroadcastIntentTransferMethodUpdated(
                transferMethod);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);

        Intent activityResult = new Intent();
        activityResult.setAction(ACTION_HYPERWALLET_TRANSFER_METHOD_UPDATED);
        activityResult.putExtra(EXTRA_TRANSFER_METHOD_UPDATED, transferMethod);
        getActivity().setResult(Activity.RESULT_OK, activityResult);
        getActivity().finish();
    }

    @Override
    public void showErrorLoadTransferMethodConfigurationFields(@NonNull List<Error> errors) {
        mOnLoadTransferMethodConfigurationFieldsNetworkErrorCallback.showErrorsLoadTransferMethodConfigurationFields(
                errors);
    }

    @Override
    public void showTransferMethodFields(@NonNull List<FieldGroup> fields) {
        mDynamicContainer.removeAllViews();

        try {
//            Locale locale = new Locale.Builder().setRegion(mCountry).build();
            Locale locale = Locale.getDefault();

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
                                    mUpdateTransferMethodButton);
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
//                            .country(mCountry)
//                            .currency(mCurrency)
                            .type(mTransferMethodType)
//                            .profileType(mTransferMethodProfileType)
                            .build());

            if (mUpdateProgressBar) {
                setVisibleAndDisableFields();
            }
        } catch (HyperwalletException e) {
            throw new IllegalStateException("Widget initialization error: " + e.getMessage());
        }
    }

    private String getSectionHeaderText(@NonNull final FieldGroup group, @NonNull final Locale locale) {
        if (FieldGroup.GroupTypes.ACCOUNT_INFORMATION.equals(group.getGroupName())) {
            return requireContext().getString(R.string.account_information,
                    locale.getDisplayName().toUpperCase(), "USD");
//                    locale.getDisplayName().toUpperCase(), mCurrency);
        }

        return requireContext().getString(requireContext().getResources()
                .getIdentifier(group.getGroupName().toLowerCase(Locale.ROOT), "string",
                        requireContext().getPackageName()));
    }

    @Override
    public void showTransactionInformation(@NonNull List<Fee> fees,
            @Nullable ProcessingTime processingTime) {
        View header = getView().findViewById(R.id.update_transfer_method_static_container_header);
        View container = getView().findViewById(R.id.update_transfer_method_static_container);
        TextView feeAndProcessingTime = getView().findViewById(R.id.update_transfer_method_information);

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
    public void showTransferMethodFields(@NonNull HyperwalletTransferMethodConfigurationField field) {

    }

    @Override
    public void showUpdateButtonProgressBar() {
        mUpdateProgressBar = true;
        setVisibleAndDisableFields();
    }

    private void setVisibleAndDisableFields() {
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        mUpdateButtonProgressBar.setVisibility(View.VISIBLE);
        mUpdateTransferMethodButton.setBackgroundColor(getResources().getColor(R.color.colorSecondaryDark));
    }

    @Override
    public void hideUpdateButtonProgressBar() {
        mUpdateProgressBar = false;
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        mUpdateButtonProgressBar.setVisibility(View.GONE);
        mUpdateTransferMethodButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        mUpdateTransferMethodButton.setTextColor(getResources().getColor(R.color.regularColorPrimary));
    }

    @Override
    public void showProgressBar() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgressBar() {
        mProgressBar.setVisibility(View.GONE);
        mUpdateTransferMethodButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void showInputErrors(@NonNull List<Error> errors) {
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
                    } else {
                        widget.showValidationError(null);
                        widgetInputState.setErrorMessage(null);
                    }
                }
            }
        }

        mPresenter.handleUnmappedFieldError(mWidgetInputStateHashMap, errors);
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void retryUpdateTransferMethod() {
        mPresenter.updateTransferMethod(mTransferMethod);
    }

    @Override
    public void reloadTransferMethodConfigurationFields() {
        mPresenter.loadTransferMethodConfigurationFields(FORCE_UPDATE, mTransferMethodType, mTransferMethodToken);
    }

    @Override
    public void valueChanged(@NonNull AbstractWidget widget) {
        isWidgetItemValid(widget);
    }

    @Override
    public boolean isWidgetSelectionFragmentDialogOpen() {
        return getFragmentManager().findFragmentByTag(WidgetSelectionDialogFragment.TAG) != null;
    }

    @Override
    public void openWidgetSelectionFragmentDialog(@NonNull TreeMap<String, String> nameValueMap,
            @NonNull String selectedName, @NonNull String fieldLabel, @NonNull String fieldName) {
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

    @Override
    public void widgetFocused(@NonNull String fieldName) {
        WidgetInputState widgetInputState = mWidgetInputStateHashMap.get(fieldName);
        widgetInputState.setHasFocused(true);
    }

    @Override
    public void saveTextChanged(@NonNull String fieldName, @NonNull String value) {
        WidgetInputState inputState = mWidgetInputStateHashMap.get(fieldName);
        if (inputState.hasApiError()) {
            String oldValue = inputState.getValue();
            if (!TextUtils.isEmpty(oldValue) && !oldValue.equals(value)) {
                inputState.setHasApiError(false);
            }
        }
        inputState.setValue(value);
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

    @Override
    public void openWidgetDateDialog(@Nullable String date, @NonNull String fieldName) {
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

    interface OnUpdateTransferMethodNetworkErrorCallback {
        void showErrorsUpdateTransferMethod(@NonNull final List<Error> errors);
    }
}
