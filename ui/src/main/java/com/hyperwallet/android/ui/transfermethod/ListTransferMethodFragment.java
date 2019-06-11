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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hyperwallet.android.common.view.HorizontalDividerItemDecorator;
import com.hyperwallet.android.hyperwallet_ui.R;
import com.hyperwallet.android.model.HyperwalletError;
import com.hyperwallet.android.model.HyperwalletStatusTransition;
import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod;
import com.hyperwallet.android.ui.HyperwalletLocalBroadcast;
import com.hyperwallet.android.ui.repository.RepositoryFactory;
import com.hyperwallet.android.ui.view.widget.OneClickListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ListTransferMethodFragment extends Fragment implements ListTransferMethodContract.View {

    static final String ARGUMENT_IS_TRANSFER_METHODS_RELOAD_NEEDED = "ARGUMENT_IS_TRANSFER_METHODS_RELOAD_NEEDED";

    private static final int LAST_FOUR_DIGIT = 4;
    private static final String ARGUMENT_TRANSFER_METHOD_LIST = "ARGUMENT_TRANSFER_METHOD_LIST";

    private View mEmptyListView;
    private ListTransferMethodAdapter mListTransferMethodAdapter;
    private ListTransferMethodContract.Presenter mPresenter;
    private View mProgressBar;
    private ArrayList<HyperwalletTransferMethod> mTransferMethodList;
    private OnAddNewTransferMethodSelected mOnAddNewTransferMethodSelected;
    private OnTransferMethodContextMenuDeletionSelected mOnTransferMethodContextMenuDeletionSelected;
    private OnDeactivateTransferMethodNetworkErrorCallback mOnDeactivateTransferMethodNetworkErrorCallback;
    private OnLoadTransferMethodNetworkErrorCallback mOnLoadTransferMethodNetworkErrorCallback;
    private boolean mIsTransferMethodsReloadNeeded;
    private RecyclerView recyclerView;

    /**
     * Please don't use this constructor this is reserved for Android Core Framework
     * use
     *
     * @see {@link ListTransferMethodFragment#newInstance()} instead.
     */
    public ListTransferMethodFragment() {
    }

    public static ListTransferMethodFragment newInstance() {
        ListTransferMethodFragment fragment = new ListTransferMethodFragment();
        fragment.mTransferMethodList = new ArrayList<>(1);

        Bundle internalState = new Bundle();
        internalState.putParcelableArrayList(ARGUMENT_TRANSFER_METHOD_LIST, fragment.mTransferMethodList);
        fragment.setArguments(internalState);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mOnAddNewTransferMethodSelected = (OnAddNewTransferMethodSelected) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement "
                    + OnAddNewTransferMethodSelected.class.getCanonicalName());
        }

        try {
            mOnTransferMethodContextMenuDeletionSelected = (OnTransferMethodContextMenuDeletionSelected) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement "
                    + OnTransferMethodContextMenuDeletionSelected.class.getCanonicalName());
        }

        try {
            mOnDeactivateTransferMethodNetworkErrorCallback = (OnDeactivateTransferMethodNetworkErrorCallback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement "
                    + OnDeactivateTransferMethodNetworkErrorCallback.class.getCanonicalName());
        }

        try {
            mOnLoadTransferMethodNetworkErrorCallback = (OnLoadTransferMethodNetworkErrorCallback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement "
                    + OnLoadTransferMethodNetworkErrorCallback.class.getCanonicalName());
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }


    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            mTransferMethodList = savedInstanceState.getParcelableArrayList(ARGUMENT_TRANSFER_METHOD_LIST);
            mIsTransferMethodsReloadNeeded = savedInstanceState.getBoolean(ARGUMENT_IS_TRANSFER_METHODS_RELOAD_NEEDED);
        } else {
            mTransferMethodList = getArguments().getParcelableArrayList(ARGUMENT_TRANSFER_METHOD_LIST);
            mIsTransferMethodsReloadNeeded = true;
        }
        mListTransferMethodAdapter = new ListTransferMethodAdapter(mTransferMethodList,
                mOnTransferMethodContextMenuDeletionSelected);

        recyclerView.setAdapter(mListTransferMethodAdapter);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_transfer_method, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mProgressBar = view.findViewById(R.id.list_transfer_method_progress_bar_layout);
        mEmptyListView = view.findViewById(R.id.empty_transfer_method_list_layout);
        Button button = view.findViewById(R.id.add_account_button);
        button.setOnClickListener(new OneClickListener() {
            @Override
            public void onOneClick(View v) {
                mOnAddNewTransferMethodSelected.showSelectTransferMethodView();
            }
        });

        recyclerView = view.findViewById(R.id.list_transfer_method_item);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new HorizontalDividerItemDecorator(requireContext(), false));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        RepositoryFactory factory = RepositoryFactory.getInstance();
        mPresenter = new ListTransferMethodPresenter(factory.getTransferMethodRepository(), this);
    }


    @Override
    public void onResume() {
        super.onResume();
        mIsTransferMethodsReloadNeeded = getArguments().getBoolean(ARGUMENT_IS_TRANSFER_METHODS_RELOAD_NEEDED, true);
        if (mIsTransferMethodsReloadNeeded) {
            getArguments().putBoolean(ARGUMENT_IS_TRANSFER_METHODS_RELOAD_NEEDED, false);
            mPresenter.loadTransferMethods();
        } else {
            displayTransferMethods(mTransferMethodList);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelableArrayList(ARGUMENT_TRANSFER_METHOD_LIST, mTransferMethodList);
        outState.putBoolean(ARGUMENT_IS_TRANSFER_METHODS_RELOAD_NEEDED, mIsTransferMethodsReloadNeeded);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void confirmTransferMethodDeactivation(@NonNull final HyperwalletTransferMethod transferMethod) {
        mPresenter.deactivateTransferMethod(transferMethod);
    }

    @Override
    public void displayTransferMethods(@Nullable final List<HyperwalletTransferMethod> transferMethodList) {
        if (transferMethodList != null && !transferMethodList.isEmpty()) {
            mTransferMethodList = new ArrayList<>(transferMethodList);
            initializeNonEmptyListView();
            mIsTransferMethodsReloadNeeded = false;
        } else {
            mTransferMethodList = new ArrayList<>(1);
            mEmptyListView.setVisibility(View.VISIBLE);
            mListTransferMethodAdapter.replaceData(mTransferMethodList);
        }
    }

    @Override
    public void notifyTransferMethodDeactivated(
            @NonNull final HyperwalletStatusTransition statusTransition) {
        Intent intent = HyperwalletLocalBroadcast.createBroadcastIntentTransferMethodDeactivated(
                statusTransition);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
        mPresenter.loadTransferMethods();
    }

    @Override
    public void showErrorListTransferMethods(@NonNull List<HyperwalletError> errors) {
        mOnLoadTransferMethodNetworkErrorCallback.showErrorsLoadTransferMethods(errors);
    }

    @Override
    public void showErrorDeactivateTransferMethod(@NonNull List<HyperwalletError> errors) {
        mOnDeactivateTransferMethodNetworkErrorCallback.showErrorsDeactivateTransferMethod(errors);
    }

    @Override
    public void initiateAddTransferMethodFlow() {
    }

    @Override
    public void initiateAddTransferMethodFlowResult() {
    }

    @Override
    public void showProgressBar() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgressBar() {
        mProgressBar.setVisibility(View.GONE);
    }

    private void initializeNonEmptyListView() {
        mEmptyListView.setVisibility(View.GONE);
        mListTransferMethodAdapter.replaceData(mTransferMethodList);
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void loadTransferMethods() {
        mPresenter.loadTransferMethods();
    }

    interface OnTransferMethodContextMenuDeletionSelected {

        void showConfirmationDialog(@NonNull final HyperwalletTransferMethod transferMethod);
    }

    interface OnAddNewTransferMethodSelected {

        void showSelectTransferMethodView();
    }

    interface OnDeactivateTransferMethodNetworkErrorCallback {

        void showErrorsDeactivateTransferMethod(@NonNull final List<HyperwalletError> errors);
    }

    interface OnLoadTransferMethodNetworkErrorCallback {

        void showErrorsLoadTransferMethods(@NonNull final List<HyperwalletError> errors);
    }

    private static class ListTransferMethodAdapter extends RecyclerView.Adapter<ListTransferMethodAdapter.ViewHolder> {
        private List<HyperwalletTransferMethod> mTransferMethodList;
        private OnTransferMethodContextMenuDeletionSelected mOnTransferMethodContextMenuDeletionSelected;

        ListTransferMethodAdapter(final List<HyperwalletTransferMethod> transferMethodList,
                final OnTransferMethodContextMenuDeletionSelected onTransferMethodContextMenuSelection) {
            mTransferMethodList = transferMethodList;
            mOnTransferMethodContextMenuDeletionSelected = onTransferMethodContextMenuSelection;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater layout = LayoutInflater.from(viewGroup.getContext());
            View itemViewLayout = layout.inflate(R.layout.item_transfer_method_type, viewGroup, false);
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
            return (transferIdentification.length() > LAST_FOUR_DIGIT
                    ? transferIdentification.substring(transferIdentification.length() - LAST_FOUR_DIGIT)
                    : transferIdentification);
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
            private final TextView mTransferMethodCountry;
            private final TextView mTransferMethodIdentification;
            private final TextView mIcon;
            private final ImageButton mImageButton;

            ViewHolder(@NonNull final View itemView) {
                super(itemView);
                mTitle = itemView.findViewById(R.id.transfer_method_type_title);
                mIcon = itemView.findViewById(R.id.transfer_method_type_icon);
                mTransferMethodCountry = itemView.findViewById(R.id.transfer_method_type_description_1);
                mTransferMethodIdentification = itemView.findViewById(R.id.transfer_method_type_description_2);
                mImageButton = itemView.findViewById(R.id.transfer_method_context_button);
            }


            void bind(@NonNull final HyperwalletTransferMethod transferMethod) {
                mTitle.setText(
                        getStringResourceByName(mTitle.getContext(), transferMethod.getField(TYPE)));

                Locale locale = new Locale.Builder().setRegion(
                        transferMethod.getField(TRANSFER_METHOD_COUNTRY)).build();
                mIcon.setText(getStringFontIcon(mIcon.getContext(), transferMethod.getField(TYPE)));
                mTransferMethodCountry.setText(locale.getDisplayName());
                mTransferMethodIdentification.setText(mTransferMethodIdentification
                        .getContext().getString(R.string.transfer_method_list_item_description,
                                getAccountIdentifier(transferMethod)));

                mImageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    @SuppressWarnings("RestrictedApi")
                    public void onClick(View v) {
                        PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
                        popupMenu.inflate(R.menu.transfer_method_context_menu);
                        popupMenu.setGravity(Gravity.END);
                        if (popupMenu.getMenu() instanceof MenuBuilder) {
                            MenuBuilder menuBuilder = (MenuBuilder) popupMenu.getMenu();
                            menuBuilder.setOptionalIconsVisible(true);
                        }
                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                if (item.getItemId() == R.id.remove_account_context_option) {
                                    mOnTransferMethodContextMenuDeletionSelected.showConfirmationDialog(transferMethod);
                                    return true;
                                }
                                return false;
                            }
                        });

                        popupMenu.show();
                    }
                });
            }

            void recycle() {
                mImageButton.setOnClickListener(null);
            }
        }
    }
}
