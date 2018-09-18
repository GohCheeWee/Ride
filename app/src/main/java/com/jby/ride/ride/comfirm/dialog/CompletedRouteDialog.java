package com.jby.ride.ride.comfirm.dialog;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


import com.jby.ride.R;

import static com.jby.ride.ride.comfirm.ConfirmRideFragment.UPDATE_CONFIRMED_RIDE_REQUEST;

public class CompletedRouteDialog extends DialogFragment {
    View rootView;
    Button completedRouteDialogButton;
    CompletedRouteDialogCallBack completedRouteDialogCallBack;

    public CompletedRouteDialog() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_confirm_ride_complete_route_dialog, container);
        objectInitialize();
        completedRouteDialogCallBack = (CompletedRouteDialogCallBack) getActivity();
        return rootView;
    }

    private void objectInitialize() {

        completedRouteDialogButton = (Button)rootView.findViewById(R.id.completed_route_dialog_start_button);

        completedRouteDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                completedRouteDialogCallBack.updateConfirmRideListView(UPDATE_CONFIRMED_RIDE_REQUEST);
                dismiss();
            }
        });
    }
    @Override
    public void onStart() {
        super.onStart();
        Dialog d = getDialog();
        if (d != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            d.getWindow().setLayout(width, height);
            d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    public interface CompletedRouteDialogCallBack {
        void updateConfirmRideListView(int requestCode);
    }
}