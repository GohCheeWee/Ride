package com.jby.ride.home.dialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.jby.ride.R;

public class BalanceNotEnoughDialog extends DialogFragment implements View.OnClickListener {
    View rootView;
    private Button balanceNotEnoughDialogTopUpButton;
    private TextView balanceNotEnoughDialogSelectCashButton;
    BalanceNotEnoughDialogCallBack balanceNotEnoughDialogCallBack;
    boolean selectCash = false;

    public BalanceNotEnoughDialog() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.balance_not_enough_dialog, container);
        objectInitialize();
        objectSetting();
        balanceNotEnoughDialogCallBack = (BalanceNotEnoughDialogCallBack) getActivity();
        return rootView;
    }

    private void objectInitialize() {

        balanceNotEnoughDialogTopUpButton = rootView.findViewById(R.id.balance_not_enough_dialog_top_up_button);
        balanceNotEnoughDialogSelectCashButton = rootView.findViewById(R.id.balance_not_enough_dialog_select_cash_button);
    }
    private void objectSetting(){
        balanceNotEnoughDialogTopUpButton.setOnClickListener(this);
        balanceNotEnoughDialogSelectCashButton.setOnClickListener(this);
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
            case R.id.balance_not_enough_dialog_top_up_button:
                balanceNotEnoughDialogCallBack.topUp();
                selectCash = false;
                dismiss();
                break;
            case R.id.balance_not_enough_dialog_select_cash_button:
                selectCash = true;
                dismiss();
                break;
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if(selectCash)
            balanceNotEnoughDialogCallBack.setCashAsPayment();
        super.onDismiss(dialog);
    }

    public interface BalanceNotEnoughDialogCallBack {
        void setCashAsPayment();
        void topUp();
    }

}