package com.jby.ride.wallet.bankCard;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;

import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.jby.ride.R;
import com.jby.ride.others.SquareHeightLinearLayout;
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


public class AddBankCardDialog extends DialogFragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener,
        DialogInterface.OnDismissListener {
    View rootView;
    //actionbar
    private SquareHeightLinearLayout actionBarMenuIcon, actionBarCloseIcon;
    private TextView actionBarTitle;

    private EditText addBankCardDialogCardNumber, addBankCardDialogExpiry, addBankCardDialogCVV;
    private ImageView addBankCardDialogProviderIcon;
    private SwitchCompat addBankCardDialogSetCardAsDefault;
    private Button addBankCardDialogSave;
    private LinearLayout addBankCardParentLayout;

    private String firstTwoDigits = "";
    private String primary = "0";
    private String provider;
    //checking this dialog is opened from where
    private String fromDialog = "";

    //    asyncTask
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;
    Handler handler;
    //interface
    AddBankCardDialogCallBack addBankCardDialogCallBack;
    public AddBankCardDialog() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.add_bank_card_dialog, container);
        objectInitialize();
        objectSetting();
        return rootView;
    }

    private void objectInitialize() {
        //actionBar
        actionBarMenuIcon = rootView.findViewById(R.id.actionbar_menu);
        actionBarCloseIcon = rootView.findViewById(R.id.actionbar_close);
        actionBarTitle = rootView.findViewById(R.id.actionBar_title);

        addBankCardDialogCardNumber = rootView.findViewById(R.id.add_bank_card_dialog_card_number);
        addBankCardDialogExpiry = rootView.findViewById(R.id.add_bank_card_dialog_expiry);
        addBankCardDialogCVV = rootView.findViewById(R.id.add_bank_card_dialog_cvv);
        addBankCardDialogSetCardAsDefault = rootView.findViewById(R.id.add_bank_card_dialog_set_as_default_button);
        addBankCardDialogProviderIcon = rootView.findViewById(R.id.add_bank_card_dialog_card_provider_icon);
        addBankCardDialogSave = rootView.findViewById(R.id.add_bank_card_dialog_save_button);
        addBankCardParentLayout = rootView.findViewById(R.id.add_bank_card_dialog_parent_layout);

        addBankCardDialogCallBack = (AddBankCardDialogCallBack) getParentFragment();
    }

    private void objectSetting() {
        actionBarMenuIcon.setVisibility(View.GONE);
        actionBarCloseIcon.setVisibility(View.VISIBLE);
        actionBarTitle.setText(R.string.add_bank_card_dialog_title);
        actionBarCloseIcon.setOnClickListener(this);

        addBankCardDialogSave.setOnClickListener(this);
        addBankCardDialogSetCardAsDefault.setOnCheckedChangeListener(this);

        addBankCardDialogCardNumber.addTextChangedListener(new MyTextWatcher(addBankCardDialogCardNumber));
        addBankCardDialogExpiry.addTextChangedListener(new MyTextWatcher(addBankCardDialogExpiry));
        addBankCardDialogCVV.addTextChangedListener(new MyTextWatcher(addBankCardDialogCVV));

        handler = new Handler();
        Bundle bundle = getArguments();
        if(bundle != null){
            fromDialog = bundle.getString("from_dialog");
            addBankCardDialogSetCardAsDefault.setChecked(true);
            addBankCardDialogSetCardAsDefault.setClickable(false);
        }
        showKeyBoard();
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
            d.setOnDismissListener(this);
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        addBankCardDialogCallBack.closeKeyBoard();
        super.onDismiss(dialog);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.actionbar_close:
                dismiss();
                break;
            case R.id.add_bank_card_dialog_save_button:
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        addNewBankCard();
                    }
                },200);
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if(b)
            primary = "1";
        else
            primary = "0";
    }


    private class MyTextWatcher implements TextWatcher {

        private View view;
        private static final char space = ' ';

        private MyTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int start , int count, int after) {

        }

        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

        }

        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.add_bank_card_dialog_card_number:
                    if(editable.length() != 19){
                        autoSpacingForBankCardNumber(editable);
                        autoSetupCardProvider(editable);
                    }
                    else
                        addBankCardDialogExpiry.requestFocus();
                    break;
                case R.id.add_bank_card_dialog_expiry:
                    if(editable.length() != 5)
                        setSlashForExpiry(editable);
                    else
                        addBankCardDialogCVV.requestFocus();
                    break;
            }
            checkingStatus();
        }

        private void checkingStatus() {
            String cardNumber = addBankCardDialogCardNumber.getText().toString().trim();
            String expiry = addBankCardDialogExpiry.getText().toString().trim();
            String cvv = addBankCardDialogCVV.getText().toString().trim();

            if(cardNumber.length() == 19 && expiry.length() == 5 && cvv.length() == 3 && !provider.equals("Unknown")){
                addBankCardDialogSave.setBackground(Objects.requireNonNull(getActivity()).getResources().getDrawable(R.drawable.activity_login_custom_button));
                addBankCardDialogSave.setEnabled(true);
            }
            else
                addBankCardDialogSave.setEnabled(false);
        }

        private void autoSpacingForBankCardNumber(Editable editable){
            String initial = editable.toString();
            // remove all non-digits characters
            String processed = initial.replaceAll("\\D", "");
            // insert a space after all groups of 4 digits that are followed by another digit
            processed = processed.replaceAll("(\\d{4})(?=\\d)", "$1 ");
            //  there before setting
            if (!initial.equals(processed)) {
                // set the value
                editable.replace(0, initial.length(), processed);
            }
        }
        private void autoSetupCardProvider(Editable editable){
            if(editable.length() >= 4){
                firstTwoDigits =  editable.toString().substring (0,2);
                addBankCardDialogProviderIcon.setImageDrawable(cardProvider(Integer.valueOf(firstTwoDigits)));
            }
            else{
                addBankCardDialogProviderIcon.setImageDrawable(getResources().getDrawable(R.drawable.bank_card_dialog_default_card_icon));
                provider = "Unknown";
            }

        }

        private Drawable cardProvider(int cardNumber){
            Drawable drawable;
            if(cardNumber >= 51 && cardNumber <= 55){
                drawable = Objects.requireNonNull(getActivity()).getResources().getDrawable(R.drawable.fragment_pending_ride_detail_payment_method);
                provider = "Master Card";
            }
            else if(cardNumber >= 60 && cardNumber <= 65)
            {
                drawable = Objects.requireNonNull(getActivity()).getResources().getDrawable(R.drawable.bank_card_dialog_discover_network_icon);
                provider = "Discover Network";
            }
            else if(cardNumber >= 34 && cardNumber <= 37)
            {
                drawable = Objects.requireNonNull(getActivity()).getResources().getDrawable(R.drawable.bank_card_dialog_american_icon);
                provider = "American Express";
            }
            else if(cardNumber >= 40 && cardNumber <= 49)
            {
                drawable = Objects.requireNonNull(getActivity()).getResources().getDrawable(R.drawable.bank_card_dialog_visa_icon);
                provider = "VISA";
            }
            else{
                drawable = Objects.requireNonNull(getActivity()).getResources().getDrawable(R.drawable.bank_card_dialog_default_card_icon);
                provider = "Unknown";
            }
            return drawable;
        }

        private void setSlashForExpiry(Editable editable){
            if(editable.length() == 2)
                editable.append("/");
        }
    }

    private void addNewBankCard(){
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("user_id", SharedPreferenceManager.getUserID(getActivity())));
        apiDataObjectArrayList.add(new ApiDataObject("card_number", addBankCardDialogCardNumber.getText().toString().trim()));
        apiDataObjectArrayList.add(new ApiDataObject("expire_date", addBankCardDialogExpiry.getText().toString().trim()));
        apiDataObjectArrayList.add(new ApiDataObject("cvv", addBankCardDialogCVV.getText().toString().trim()));
        apiDataObjectArrayList.add(new ApiDataObject("type", "1"));
        apiDataObjectArrayList.add(new ApiDataObject("provider", provider));
        apiDataObjectArrayList.add(new ApiDataObject("status", primary));

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
                        checkingOpenFromWhere();

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

    private void checkingOpenFromWhere() {
        if(fromDialog.equals("")){
            addBankCardDialogCallBack.onRefresh();
            addBankCardDialogCallBack.showSnackBar("Save Successfully");
            dismiss();
        }
        else
        {
            String cardNumber = addBankCardDialogCardNumber.getText().toString().trim();
            addBankCardDialogCallBack.setBankCardDetail(cardNumber, provider);
            dismiss();
        }
    }

    //    snackBar setting
    public void showSnackBar() {
        final Snackbar snackbar = Snackbar.make(addBankCardParentLayout, "Something Went Wrong :(", Snackbar.LENGTH_SHORT);
        snackbar.setActionTextColor(getResources().getColor(R.color.blue));
        snackbar.setAction("Dismiss", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }

    public void showKeyBoard(){
        final InputMethodManager imm = (InputMethodManager)Objects.requireNonNull(getActivity()).getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm != null)
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public interface AddBankCardDialogCallBack{
        void showSnackBar(String message);
        void onRefresh();
        void closeKeyBoard();
        void setBankCardDetail(String cardNumber, String provider);
    }
}