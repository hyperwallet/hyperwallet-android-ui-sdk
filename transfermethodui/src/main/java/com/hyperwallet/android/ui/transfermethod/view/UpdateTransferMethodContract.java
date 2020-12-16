package com.hyperwallet.android.ui.transfermethod.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hyperwallet.android.model.Error;
import com.hyperwallet.android.model.graphql.Fee;
import com.hyperwallet.android.model.graphql.HyperwalletTransferMethodConfigurationField;
import com.hyperwallet.android.model.graphql.ProcessingTime;
import com.hyperwallet.android.model.graphql.field.FieldGroup;
import com.hyperwallet.android.model.transfermethod.TransferMethod;

import java.util.List;
import java.util.Map;

/**
 * View and Presenter Contract for Updating Transfer Method
 */
public class UpdateTransferMethodContract {

    interface View {

        void notifyTransferMethodUpdated(@NonNull final TransferMethod transferMethod);

        void showErrorUpdateTransferMethod(@NonNull final List<Error> errors);

        void showErrorLoadTransferMethodConfigurationFields(@NonNull final List<Error> errors);

        void showTransferMethodFields(@NonNull final List<FieldGroup> fields);

        void showTransferMethodFields(@NonNull final HyperwalletTransferMethodConfigurationField field);

        void showTransactionInformation(@NonNull final List<Fee> fees,
                @Nullable final ProcessingTime processingTime);

        void showUpdateButtonProgressBar();

        void hideUpdateButtonProgressBar();

        void showProgressBar();

        void hideProgressBar();

        void showInputErrors(@NonNull final List<Error> errors);

        /**
         * Check the state of a View
         *
         * @return true when View is added to Container
         */
        boolean isActive();

        void retryUpdateTransferMethod();

        void reloadTransferMethodConfigurationFields();
    }

    interface Presenter {

        void updateTransferMethod(@NonNull TransferMethod transferMethod);

        void loadTransferMethodConfigurationFields(boolean forceUpdate, @NonNull final String transferMethodType, @NonNull final String transferMethodToken);

        void handleUnmappedFieldError(@NonNull final Map<String, ?> fieldSet,
                @NonNull final List<Error> errors);
    }
}
