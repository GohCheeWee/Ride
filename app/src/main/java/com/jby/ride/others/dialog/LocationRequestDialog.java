package com.jby.ride.others.dialog;
import android.Manifest;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.jby.ride.R;
import java.util.Objects;


import static com.jby.ride.home.MainActivity.LOCATION_REQUEST;

public class LocationRequestDialog extends DialogFragment {
    View rootView;
    public Button locationRequestDialogEnableButton;
    LocationRequestDialogCallBack locationRequestDialogCallBack;
    private boolean isClick = false;
    public LocationRequestDialog() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.location_request_dialog, container);
        objectInitialize();
        objectSetting();

        return rootView;
    }

    private void objectInitialize() {
        locationRequestDialogEnableButton = rootView.findViewById(R.id.location_request_dialog_enable_button);

    }

    private void objectSetting() {
        locationRequestDialogCallBack = (LocationRequestDialogCallBack) getActivity();

        locationRequestDialogEnableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isClick = true;
                ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST);
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
            Objects.requireNonNull(d.getWindow()).setLayout(width, height);
            d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            d.getWindow().setWindowAnimations(R.style.dialog_up_down);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (!isClick)
            locationRequestDialogCallBack.checkLocationPermission();
    }

    public interface LocationRequestDialogCallBack {
        boolean checkLocationPermission();
    }

}