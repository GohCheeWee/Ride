package com.jby.ride.others.dialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.jby.ride.R;
import com.jby.ride.shareObject.ApiDataObject;
import com.jby.ride.shareObject.ApiManager;
import com.jby.ride.shareObject.AsyncTaskManager;
import com.jby.ride.sharePreference.SharedPreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DriverPickUpDialog extends DialogFragment {
    View rootView;
    public Button driverPickUpDialogButton;
    DriverPickUpDialogCallBack driverPickUpDialogCallBack;
    String  matchRideId;

    public DriverPickUpDialog() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.driver_pick_up_dialog, container);
        objectInitialize();
        objectSetting();

        return rootView;
    }

    private void objectInitialize() {
        driverPickUpDialogButton = rootView.findViewById(R.id.driver_pick_up_dialog_button);

    }

    private void objectSetting() {
        if (getArguments() != null) {
            matchRideId = getArguments().getString("match_ride_id");
            driverPickUpDialogButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    driverPickUpDialogCallBack.redirectToStartRideActivity(matchRideId);
                    dismiss();
                }
            });
        }
        driverPickUpDialogCallBack = (DriverPickUpDialogCallBack) getActivity();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog d = getDialog();
        if (d != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            Objects.requireNonNull(d.getWindow()).setLayout(width, height);
            d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            d.getWindow().setWindowAnimations(R.style.dialog_up_down);
        }
    }
    public interface DriverPickUpDialogCallBack {
        void redirectToStartRideActivity(String matchRideId);
    }

}