package com.jby.ride.wallet.bankCard;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jby.ride.R;
import com.jby.ride.shareObject.ApiDataObject;
import com.jby.ride.shareObject.ApiManager;
import com.jby.ride.shareObject.AsyncTaskManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SetDefaultCardDialog extends DialogFragment implements View.OnClickListener {
    View rootView;
    private Button setDefaultCardDialogConfirmButton;
    private TextView setDefaultCardDialogCancelButton;
    private RelativeLayout setDefaultCardDialogParentLayout;
    //    asyncTask
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;
    Handler handler;
    private String cardID;

    SetDefaultCardDialogCallBack setDefaultCardDialogCallBack;
    public SetDefaultCardDialog() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.set_default_bank_card_dialog, container);
        objectInitialize();
        objectSetting();
        return rootView;
    }

    private void objectInitialize() {
        setDefaultCardDialogConfirmButton = rootView.findViewById(R.id.set_default_bank_card_dialog_confirm_button);
        setDefaultCardDialogCancelButton = rootView.findViewById(R.id.set_default_bank_card_dialog_cancel_button);
        setDefaultCardDialogParentLayout = rootView.findViewById(R.id.set_default_bank_card_dialog_parent_layout);
        setDefaultCardDialogCallBack = (SetDefaultCardDialogCallBack) getParentFragment();
        handler = new Handler();
    }


    private void objectSetting(){
        setDefaultCardDialogConfirmButton.setOnClickListener(this);
        setDefaultCardDialogCancelButton.setOnClickListener(this);

        Bundle bundle = getArguments();
        if(bundle != null){
            cardID = bundle.getString("card_id");
        }
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
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.set_default_bank_card_dialog_confirm_button:
                setCardAsDefault();
                break;
            case R.id.set_default_bank_card_dialog_cancel_button:
                dismiss();
                break;
        }
    }

    private void setCardAsDefault(){
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("card_id", cardID));
        apiDataObjectArrayList.add(new ApiDataObject("default","1"));
        asyncTaskManager = new AsyncTaskManager(
                getActivity(),
                new ApiManager().bankCard,
                new ApiManager().getResultParameter(
                        "",
                        new ApiManager().setData(apiDataObjectArrayList),
                        ""
                )
        );
        asyncTaskManager.execute();

        if (!asyncTaskManager.isCancelled()) {
            try {
                jsonObjectLoginResponse = asyncTaskManager.get(30000, TimeUnit.MILLISECONDS);

                if (jsonObjectLoginResponse != null) {
                    if(jsonObjectLoginResponse.getString("status").equals("1")){
                       setDefaultCardDialogCallBack.onRefresh();
                       setDefaultCardDialogCallBack.showSnackBar("Primary card is changed");
                       dismiss();

                    }
                    else if(jsonObjectLoginResponse.getString("status").equals("2")){
                        showSnackBar();
                    }
                }
                else {
                    Toast.makeText(getActivity(), "Network Error!", Toast.LENGTH_SHORT).show();
                }

            } catch (InterruptedException e) {
                Toast.makeText(getActivity(), "Interrupted Exception!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (ExecutionException e) {
                Toast.makeText(getActivity(), "Execution Exception!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (JSONException e) {
                Toast.makeText(getActivity(), "JSON Exception!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (TimeoutException e) {
                Toast.makeText(getActivity(), "Connection Time Out!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    //    snackBar setting
    public void showSnackBar() {
        final Snackbar snackbar = Snackbar.make(setDefaultCardDialogParentLayout, "Something Went Wrong :(", Snackbar.LENGTH_SHORT);
        snackbar.setActionTextColor(getResources().getColor(R.color.blue));
        snackbar.setAction("Dismiss", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }

    public interface SetDefaultCardDialogCallBack{
        void onRefresh();
        void showSnackBar(String message);
    }
}