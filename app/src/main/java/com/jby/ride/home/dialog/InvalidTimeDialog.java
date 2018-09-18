package com.jby.ride.home.dialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.jby.ride.R;

public class InvalidTimeDialog extends DialogFragment {
    View rootView;
    public Button invalidTimeDialog;
    InvalidTimeDialogCallBack invalidTimeDialogCallBack;
    String note;

    public InvalidTimeDialog() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_main_invalid_time_dialog, container);
        objectInitialize();
        invalidTimeDialogCallBack = (InvalidTimeDialogCallBack) getActivity();
        return rootView;
    }

    private void objectInitialize() {

        invalidTimeDialog = (Button)rootView.findViewById(R.id.activity_main_invalid_time_dialog_button);

        invalidTimeDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                invalidTimeDialogCallBack.selectTimeDialog();
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

    public interface InvalidTimeDialogCallBack {
        void selectTimeDialog();
    }

}