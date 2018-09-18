package com.jby.ride.profile;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jby.ride.R;

import java.util.Objects;

public class UploadImageDialog extends DialogFragment implements View.OnClickListener {
    TextView uploadImageDialogCamera;
    TextView uploadImageDialogGallery;
    UploadImageDialogListener uploadImageDialogListener;
    LinearLayout uploadImageDialogMainLayout;
    View rootView;

    public UploadImageDialog() {
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_profile_upload_photo_dialog, container);
        objectInitialize();
        objectSetting();
        uploadImageDialogListener = (UploadImageDialogListener) getActivity();
        return rootView;
    }

    private void objectInitialize() {
        uploadImageDialogCamera = (TextView) rootView.findViewById(R.id.activity_profile_upload_photo_new_photo);
        uploadImageDialogGallery = (TextView) rootView.findViewById(R.id.activity_profile_upload_photo_gallery);
        uploadImageDialogMainLayout = (LinearLayout)rootView.findViewById(R.id.activity_upload_photo_dialog_main_layout);
    }

    private void objectSetting() {
        uploadImageDialogCamera.setOnClickListener(this);
        uploadImageDialogGallery.setOnClickListener(this);
        uploadImageDialogMainLayout.setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog d = getDialog();
        if (d!=null){
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            Objects.requireNonNull(d.getWindow()).setLayout(width, height);
            d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.activity_profile_upload_photo_new_photo:
                uploadImageDialogListener.checkCapturePermission();
                dismiss();
                break;

            case R.id.activity_profile_upload_photo_gallery:
                uploadImageDialogListener.checkGalleryPermission();
                dismiss();
                break;

            case R.id.activity_upload_photo_dialog_main_layout:
                dismiss();
                break;
        }
    }

    public interface UploadImageDialogListener {
        void checkGalleryPermission();
        void checkCapturePermission();
    }
}

