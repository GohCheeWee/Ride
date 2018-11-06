package com.jby.ride.wallet.topUp;

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
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jby.ride.R;
import com.jby.ride.others.SquareHeightLinearLayout;
import com.jby.ride.shareObject.ApiDataObject;
import com.jby.ride.shareObject.ApiManager;
import com.jby.ride.shareObject.AsyncTaskManager;
import com.jby.ride.sharePreference.SharedPreferenceManager;
import com.jby.ride.wallet.bankCard.AddBankCardDialog;
import com.jby.ride.wallet.bankCard.BankCardDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TopUpDialog extends DialogFragment implements View.OnClickListener, TextWatcher,
        BankCardDialog.BankCardDialogCallBack, AddBankCardDialog.AddBankCardDialogCallBack {

    View rootView;
    //actionbar
    private SquareHeightLinearLayout actionBarMenuIcon, actionBarCloseIcon;
    private TextView actionBarTitle;
    private EditText topUpDialogAmount;
    private Button topUpDialogNextButton;
    private TextView topUpDialogBankCard;
    private TextView topUpDialogLabelMaximum;
    private ImageView topupDialogProviderIcon;
    private static final double maximumAmount = 200.00;
    private String provider;
    boolean buttonClickable;
    //checking is open from mainactivity or not
    private boolean isFromMainActivity = false;
    private boolean paymentIsMadeSuccessful = false;
    //    asyncTask
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;
    Handler handler;
    //interface
    public TopUpDialogCallBack topUpDialogCallBack;
    public TopUpDialog() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.top_up_dialog, container);
        objectInitialize();
        objectSetting();
        return rootView;
    }

    private void objectInitialize() {
        //actionBar
        actionBarMenuIcon = rootView.findViewById(R.id.actionbar_menu);
        actionBarCloseIcon = rootView.findViewById(R.id.actionbar_close);
        actionBarTitle = rootView.findViewById(R.id.actionBar_title);

        topUpDialogAmount = rootView.findViewById(R.id.top_up_dialog_amount);
        topUpDialogNextButton = rootView.findViewById(R.id.top_up_dialog_next_button);
        topUpDialogBankCard = rootView.findViewById(R.id.top_up_dialog_bank_card);
        topupDialogProviderIcon = rootView.findViewById(R.id.top_up_dialog_provider_icon);
        topUpDialogLabelMaximum = rootView.findViewById(R.id.top_up_dialog_label_maximum);
        handler = new Handler();
        topUpDialogCallBack = (TopUpDialogCallBack) getActivity();
    }

    private void objectSetting() {
        actionBarMenuIcon.setVisibility(View.GONE);
        actionBarCloseIcon.setVisibility(View.VISIBLE);
        actionBarCloseIcon.setOnClickListener(this);
        actionBarTitle.setText(R.string.top_up_dialog_title);

        topUpDialogNextButton.setOnClickListener(this);
        topUpDialogAmount.addTextChangedListener(this);
        topUpDialogBankCard.setOnClickListener(this);

        Bundle bundle = getArguments();
        if(bundle != null){
            //checking is open from main activity or not
            isFromMainActivity = bundle.getBoolean("fromMainActivity");
        }
        showKeyBoard();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkBankCardAvailability();
            }
        },200);
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
    public void onDismiss(DialogInterface dialog) {
        if(isFromMainActivity){
            if(!paymentIsMadeSuccessful)
                topUpDialogCallBack.setCashAsPayment();

            topUpDialogCallBack.closeKeyBoard();
        }
        else
            closeKeyBoard();
        super.onDismiss(dialog);
    }

    @Override
    public void onClick(View view) {
        view.setEnabled(false);
        switch (view.getId()){
            case R.id.top_up_dialog_bank_card:
                closeKeyBoard();
                openBankCardDialog();
                break;
            case R.id.actionbar_close:
                dismiss();
                break;
            case R.id.top_up_dialog_next_button:
                checkingInput();
                break;
        }
    }

    private void checkBankCardAvailability(){
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("user_id", SharedPreferenceManager.getUserID(getActivity())));

        asyncTaskManager = new AsyncTaskManager(
                getActivity(),
                new ApiManager().transaction,
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
                        String cardNumber = jsonObjectLoginResponse.getJSONObject("card_detail").getString("card_number");
                        String provider = jsonObjectLoginResponse.getJSONObject("card_detail").getString("provider");

                        if(cardNumber != null && provider != null)
                            setBankCardDetail(cardNumber, provider);
                    }
                    else if(jsonObjectLoginResponse.getString("status").equals("3")){
                        topUpDialogBankCard.setText(R.string.bank_card_dialog_label_add_bank_card);
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

    private void checkingInput(){
        double finalAmount = Double.valueOf(topUpDialogAmount.getText().toString().trim());
        if(finalAmount > 0)
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    topUp();
                }
            },200);
        else
            showSnackBar("Minimum amount is RM 1");
    }
    private void topUp(){
        String account = topUpDialogBankCard.getText().toString().trim();
        String amount =  topUpDialogAmount.getText().toString().trim() + ".00";
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("user_id", SharedPreferenceManager.getUserID(getActivity())));
        apiDataObjectArrayList.add(new ApiDataObject("amount", amount));
        apiDataObjectArrayList.add(new ApiDataObject("account", account));
        apiDataObjectArrayList.add(new ApiDataObject("provider", provider));
        apiDataObjectArrayList.add(new ApiDataObject("top_up","1"));


        asyncTaskManager = new AsyncTaskManager(
                getActivity(),
                new ApiManager().transaction,
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
                        //checking whether payment is make successful or not
                        paymentIsMadeSuccessful = true;
                        //if opened from main activity, after top up check again the amount weather is enough or not
                        if(isFromMainActivity)
                            topUpDialogCallBack.checkingBalance(amount);
                        else
                            topUpDialogCallBack.getCurrentBalanceSetup();

                        topUpDialogCallBack.showSnackBar("Top-up Successful");
                        dismiss();
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
        topUpDialogNextButton.setEnabled(true);
    }

    private void openBankCardDialog(){
        String bankCard = topUpDialogBankCard.getText().toString().trim();
        DialogFragment dialogFragment;
        //if bank card is added already
        if(!bankCard.equals("Add a Bank Card"))
            dialogFragment = new BankCardDialog();
        //if no any bank card is added yet
        else
            dialogFragment = new AddBankCardDialog();

        Bundle bundle = new Bundle();
        bundle.putString("from_dialog", "top_up");

        dialogFragment.setArguments(bundle);
        FragmentManager fm = getChildFragmentManager();
        dialogFragment.show(fm, "");

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                topUpDialogBankCard.setEnabled(true);
            }
        },200);

    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        if(editable.length() > 0){
            double amount = Double.valueOf(editable.toString());
            if(amount > maximumAmount){
                topUpDialogLabelMaximum.setTextColor(getResources().getColor(R.color.red));
                topUpDialogLabelMaximum.setText(R.string.top_up_dialog_label_when_reach_maximum);
                buttonClickable = false;
            }
            else{
                topUpDialogLabelMaximum.setTextColor(getResources().getColor(R.color.transparent_black));
                topUpDialogLabelMaximum.setText(R.string.top_up_dialog_label_user_limit);
                buttonClickable = true;
            }
        }
        else
        {
            topUpDialogLabelMaximum.setTextColor(getResources().getColor(R.color.transparent_black));
            topUpDialogLabelMaximum.setText(R.string.top_up_dialog_label_user_limit);
            buttonClickable = false;
        }
        checkingButtonStatus();
    }

    private Drawable cardProvider(String provider){
        Drawable drawable;
        switch (provider){
            case "Master Card":
                drawable = getResources().getDrawable(R.drawable.fragment_pending_ride_detail_payment_method);
                break;
            case "Discover Network":
                drawable = getResources().getDrawable(R.drawable.bank_card_dialog_discover_network_icon);
                break;
            case "American Express":
                drawable = getResources().getDrawable(R.drawable.bank_card_dialog_american_icon);
                break;
            case "VISA":
                drawable = getResources().getDrawable(R.drawable.bank_card_dialog_visa_icon);
                break;
            default:
                drawable = getResources().getDrawable(R.drawable.bank_card_dialog_default_card_icon);
        }
        return drawable;
    }

    @Override
    public void setBankCardDetail(String cardNumber, String provider) {
        if (cardNumber != null && provider != null) {
            this.provider = provider;
            cardNumber = "**** **** **** " + cardNumber.substring(cardNumber.length()-4);
            topupDialogProviderIcon.setImageDrawable(cardProvider(provider));

        }
        topUpDialogBankCard.setText(cardNumber);
        topUpDialogAmount.requestFocus();
        checkingButtonStatus();
    }

    private void checkingButtonStatus() {
        //button setting
        if(buttonClickable && !topUpDialogBankCard.getText().toString().equals("Add a Bank Card")){
            topUpDialogNextButton.setBackground(getResources().getDrawable(R.drawable.activity_login_custom_button));
            topUpDialogNextButton.setEnabled(true);
        }
        else{
            topUpDialogNextButton.setBackground(getResources().getDrawable(R.drawable.rating_dialog_custom_disable_button));
            topUpDialogNextButton.setEnabled(false);
        }
    }

    public void showKeyBoard(){
        final InputMethodManager imm = (InputMethodManager)Objects.requireNonNull(getActivity()).getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm != null)
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
    }

    public void closeKeyBoard(){
            Objects.requireNonNull(getActivity()).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    //    snackBar setting
    public void showSnackBar(String message) {
        final Snackbar snackbar = Snackbar.make(Objects.requireNonNull(getView()), message, Snackbar.LENGTH_SHORT);
        snackbar.setActionTextColor(getResources().getColor(R.color.blue));
        snackbar.setAction("Dismiss", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }

    @Override
    public void onRefresh() {

    }

    public interface TopUpDialogCallBack{
        void getCurrentBalanceSetup();
        void showSnackBar(String message);
        void checkingBalance(String currentBalance);
        void setCashAsPayment();
        void closeKeyBoard();
    }
}