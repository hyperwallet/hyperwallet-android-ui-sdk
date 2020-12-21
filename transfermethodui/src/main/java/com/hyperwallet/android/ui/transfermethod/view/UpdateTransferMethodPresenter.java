package com.hyperwallet.android.ui.transfermethod.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hyperwallet.android.model.Error;
import com.hyperwallet.android.model.Errors;
import com.hyperwallet.android.model.graphql.HyperwalletTransferMethodConfigurationField;
import com.hyperwallet.android.model.transfermethod.TransferMethod;
import com.hyperwallet.android.ui.R;
import com.hyperwallet.android.ui.transfermethod.repository.TransferMethodRepository;
import com.hyperwallet.android.ui.transfermethod.repository.TransferMethodUpdateConfigurationRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UpdateTransferMethodPresenter implements UpdateTransferMethodContract.Presenter {
    private static final String ERROR_UNMAPPED_FIELD = "ERROR_UNMAPPED_FIELD";
    private final TransferMethodUpdateConfigurationRepository mTransferMethodUpdateConfigurationRepository;
    private final TransferMethodRepository mTransferMethodRepository;
    private final UpdateTransferMethodContract.View mView;

    public UpdateTransferMethodPresenter(UpdateTransferMethodContract.View view,
            TransferMethodUpdateConfigurationRepository transferMethodUpdateConfigurationRepository,
            TransferMethodRepository transferMethodRepository) {
        mView = view;
        mTransferMethodUpdateConfigurationRepository = transferMethodUpdateConfigurationRepository;
        mTransferMethodRepository = transferMethodRepository;
    }

    @Override
    public void loadTransferMethodConfigurationFields(boolean forceUpdate, @NonNull String transferMethodType,
            @NonNull final String transferMethodToken) {
        mView.showProgressBar();

        if (forceUpdate) {
            mTransferMethodUpdateConfigurationRepository.refreshFields();
        }

        mTransferMethodUpdateConfigurationRepository.getFields(transferMethodToken,
                new TransferMethodUpdateConfigurationRepository.LoadFieldsCallback() {
                    @Override
                    public void onFieldsLoaded(@Nullable HyperwalletTransferMethodConfigurationField field) {
                        if (!mView.isActive()) {
                            return;
                        }

                        mView.hideProgressBar();
                        mView.showTransferMethodFields(field);
                        // there can be multiple fees when we have flat fee + percentage fees
                        mView.showTransactionInformation(field.getFees(), field.getProcessingTime());
                    }

                    @Override
                    public void onError(@NonNull Errors errors) {
                        if (!mView.isActive()) {
                            return;
                        }
                        mView.hideProgressBar();
                        mView.showErrorLoadTransferMethodConfigurationFields(errors.getErrors());
                    }
                });
    }

    @Override
    public void updateTransferMethod(@NonNull TransferMethod transferMethod) {
        mView.showUpdateButtonProgressBar();
        mTransferMethodRepository.updateTransferMethod(transferMethod,
                new TransferMethodRepository.LoadTransferMethodCallback() {
                    @Override
                    public void onTransferMethodLoaded(TransferMethod transferMethod) {

                        if (!mView.isActive()) {
                            return;
                        }
                        mView.hideUpdateButtonProgressBar();
                        mView.notifyTransferMethodUpdated(transferMethod);
                    }

                    @Override
                    public void onError(Errors errors) {
                        if (!mView.isActive()) {
                            return;
                        }

                        mView.hideUpdateButtonProgressBar();
                        if (errors.containsInputError()) {
                            mView.showInputErrors(errors.getErrors());
                        } else {
                            mView.showErrorUpdateTransferMethod(errors.getErrors());
                        }
                    }
                });
    }

    @Override
    public void handleUnmappedFieldError(@NonNull Map<String, ?> fieldSet, @NonNull List<Error> errors) {
        for (Error error : errors) {
            if (fieldSet.get(error.getFieldName()) == null) {
                List<Error> errorList = new ArrayList<Error>() {{
                    add(new Error(R.string.error_unmapped_field, ERROR_UNMAPPED_FIELD));
                }};
                mView.showErrorUpdateTransferMethod(errorList);
                return;
            }
        }
    }
}
