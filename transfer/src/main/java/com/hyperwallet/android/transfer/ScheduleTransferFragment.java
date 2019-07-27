package com.hyperwallet.android.transfer;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.hyperwallet.android.model.StatusTransition;
import com.hyperwallet.android.ui.common.viewmodel.Event;

public class ScheduleTransferFragment extends Fragment {

    private ScheduleTransferViewModel mScheduleTransferViewModel;

    private TextView mTransferSource;
    private TextView mTransferDestination;
    private Button mButton;

    public static ScheduleTransferFragment newInstance() {
        ScheduleTransferFragment fragment = new ScheduleTransferFragment();
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScheduleTransferViewModel = ViewModelProviders.of(requireActivity()).get(ScheduleTransferViewModel.class);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_schedule_transfer, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTransferSource = view.findViewById(R.id.tv_transfer_source);
        mTransferDestination = view.findViewById(R.id.tv_transfer_destination);
        mButton = view.findViewById(R.id.btn_confirm);

        registerObservers();

        mTransferSource.setText(mScheduleTransferViewModel.getTransfer().getSourceToken());
        mTransferDestination.setText(mScheduleTransferViewModel.getTransfer().getDestinationToken());
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mScheduleTransferViewModel.scheduleTransfer();
            }
        });
    }


    void retry() {
        mScheduleTransferViewModel.scheduleTransfer();
    }

    private void registerObservers() {
        mScheduleTransferViewModel.getTransferStatusTransition().observe(this, new Observer<Event<StatusTransition>>() {
            @Override
            public void onChanged(Event<StatusTransition> statusTransitionEvent) {
                if (!statusTransitionEvent.isContentConsumed()) {
                    //todo broadcast
                    statusTransitionEvent.getContent();
                    requireActivity().finishActivity(Activity.RESULT_OK);
                }
            }
        });
    }

}
