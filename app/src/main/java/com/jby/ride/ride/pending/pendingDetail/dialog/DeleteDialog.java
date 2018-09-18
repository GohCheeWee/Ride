package com.jby.ride.ride.pending.pendingDetail.dialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.jby.ride.R;

public class DeleteDialog extends DialogFragment {
    View rootView;
    private Button deleteDialogDelete;
    private TextView deleteDialogCancel;
    DeleteDialogCallBack deleteDialogCallBack;

    public DeleteDialog() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activtiy_pending_ride_detail_delete_dialog, container);
        objectInitialize();
        deleteDialogCallBack = (DeleteDialogCallBack) getActivity();
        return rootView;
    }

    private void objectInitialize() {

        deleteDialogDelete = (Button)rootView.findViewById(R.id.activity_pending_ride_detail_delete_dialog_delete);
        deleteDialogCancel = (TextView)rootView.findViewById(R.id.activity_pending_ride_detail_delete_dialog_cancel);

        deleteDialogDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteDialogCallBack.deleteRide();
                dismiss();
            }
        });

        deleteDialogCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

    public interface DeleteDialogCallBack {
        void deleteRide();
    }

}