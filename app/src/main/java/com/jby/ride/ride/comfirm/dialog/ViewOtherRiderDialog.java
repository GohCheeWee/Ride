package com.jby.ride.ride.comfirm.dialog;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.jby.ride.R;
import com.jby.ride.others.CustomListView;
import com.jby.ride.others.dialog.RiderProfileDialog;
import com.jby.ride.others.SquareHeightLinearLayout;
import com.jby.ride.ride.comfirm.object.OtherRiderInCarObject;

import java.util.ArrayList;
import java.util.Objects;


public class ViewOtherRiderDialog extends DialogFragment implements View.OnClickListener, AdapterView.OnItemClickListener {
    View rootView;
    //    actionBar
    private SquareHeightLinearLayout actionBarMenuIcon, actionBarCloseIcon, actionBarLogout;
    private TextView actionBarTitle;
    ViewOtherRiderDialogAdapter viewOtherRiderDialogAdapter;
    private ArrayList<OtherRiderInCarObject> otherRiderInCarObjectArrayList;
    private CustomListView viewOtherRiderDialogListView;

    public ViewOtherRiderDialog() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_confirm_ride_view_other_rider_dialog, container);
        objectInitialize();
        objectSetting();
        return rootView;
    }


    private void objectInitialize() {
        actionBarMenuIcon = (SquareHeightLinearLayout)rootView.findViewById(R.id.actionbar_menu);
        actionBarCloseIcon = (SquareHeightLinearLayout)rootView.findViewById(R.id.actionbar_close);
        actionBarLogout = (SquareHeightLinearLayout)rootView.findViewById(R.id.actionbar_logout);
        actionBarTitle = (TextView)rootView.findViewById(R.id.actionBar_title);

        viewOtherRiderDialogListView = rootView.findViewById(R.id.view_other_rider_dialog_list_view);
        otherRiderInCarObjectArrayList = new ArrayList<>();
    }

    private void objectSetting() {
        Bundle bundle = getArguments();
        if(bundle != null){
            otherRiderInCarObjectArrayList = bundle.getParcelableArrayList("OtherRiderInCarArrayList");
        }

        actionBarMenuIcon.setVisibility(View.GONE);
        actionBarCloseIcon.setVisibility(View.VISIBLE);
        actionBarLogout.setVisibility(View.GONE);
        actionBarTitle.setText(R.string.activity_match_ride_list_view_item_label_other_rider);
        actionBarCloseIcon.setOnClickListener(this);

        viewOtherRiderDialogAdapter = new ViewOtherRiderDialogAdapter(getActivity(), otherRiderInCarObjectArrayList);
        viewOtherRiderDialogListView.setAdapter(viewOtherRiderDialogAdapter);
        viewOtherRiderDialogListView.setOnItemClickListener(this);
    }

    private void popUpUserProfileDialog(){

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
            d.getWindow().setWindowAnimations(R.style.dialog_up_down);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.actionbar_close:
                dismiss();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Bundle bundle = new Bundle();
        DialogFragment dialogFragment = new RiderProfileDialog();
        FragmentManager fm = Objects.requireNonNull(getActivity()).getSupportFragmentManager();

        bundle.putString("rider_id", otherRiderInCarObjectArrayList.get(i).getUser_id());
        bundle.putString("username", otherRiderInCarObjectArrayList.get(i).getUsername());
        bundle.putString("profile_pic", otherRiderInCarObjectArrayList.get(i).getProfile_picture());
        bundle.putString("gender", otherRiderInCarObjectArrayList.get(i).getGender());

        dialogFragment.setArguments(bundle);
        dialogFragment.show(fm, "");
    }
}