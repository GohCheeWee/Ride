package com.jby.ride.wallet.transaction.transactionDetail;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jby.ride.R;
import com.jby.ride.others.SquareHeightLinearLayout;
import com.jby.ride.shareObject.ApiDataObject;
import com.jby.ride.shareObject.ApiManager;
import com.jby.ride.shareObject.AsyncTaskManager;
import com.jby.ride.sharePreference.SharedPreferenceManager;
import com.jby.ride.wallet.transaction.TransactionChildObject;
import com.jby.ride.wallet.transaction.TransactionExpandableAdapter;
import com.jby.ride.wallet.transaction.TransactionParentObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TransactionDetailDialog extends DialogFragment implements View.OnClickListener{

    View rootView;
    //actionbar
    private SquareHeightLinearLayout actionBarMenuIcon, actionBarCloseIcon;
    private TextView actionBarTitle;
    private TextView transactionDetailDialogLabel, transactionDetailDialogStatus, transactionDetailDialogAmount;
    private TextView transactionDetailDialogLabelMethod, transactionDetailDialogPaymentMethod, transactionDetailDialogTransactionID;
    private TextView transactionDetailDialogDate;
    private LinearLayout transactionDetailRidePayIcon;
    private ImageView transactionDetailDialogProviderIcon;
    private ProgressBar transactionDetailDialogProgressBar;
    private LinearLayout transactionDetailDialogParentLayout;
    private String transactionID;
    //    asyncTask
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;
    Handler handler;

    public TransactionDetailDialog() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.transaction_detail_dialog, container);
        objectInitialize();
        objectSetting();
        return rootView;
    }

    private void objectInitialize() {
        //actionBar
        actionBarMenuIcon = rootView.findViewById(R.id.actionbar_menu);
        actionBarCloseIcon = rootView.findViewById(R.id.actionbar_close);
        actionBarTitle = rootView.findViewById(R.id.actionBar_title);

        transactionDetailDialogLabel = rootView.findViewById(R.id.transaction_detail_dialog_transaction_label);
        transactionDetailDialogStatus = rootView.findViewById(R.id.transaction_detail_dialog_transaction_status);
        transactionDetailDialogAmount = rootView.findViewById(R.id.transaction_detail_dialog_amount);
        transactionDetailDialogLabelMethod = rootView.findViewById(R.id.transaction_detail_dialog_label_method);
        transactionDetailDialogPaymentMethod = rootView.findViewById(R.id.transaction_detail_dialog_method);
        transactionDetailDialogTransactionID = rootView.findViewById(R.id.transaction_detail_dialog_transaction_id);
        transactionDetailDialogDate = rootView.findViewById(R.id.transaction_detail_dialog_date);

        transactionDetailDialogProgressBar = rootView.findViewById(R.id.transaction_detail_dialog_progress_bar);
        transactionDetailDialogParentLayout = rootView.findViewById(R.id.transaction_detail_dialog_parent_layout);
        transactionDetailDialogProviderIcon = rootView.findViewById(R.id.transaction_detail_dialog_provider_icon);
        transactionDetailRidePayIcon = rootView.findViewById(R.id.transaction_detail_dialog_ridepay_icon);
        handler = new Handler();

    }

    private void objectSetting() {
        actionBarMenuIcon.setVisibility(View.GONE);
        actionBarCloseIcon.setVisibility(View.VISIBLE);
        actionBarCloseIcon.setOnClickListener(this);
        actionBarTitle.setText(R.string.transaction_detail_dialog_title);
        Bundle bundle = getArguments();
        if (bundle != null) {
            transactionID = bundle.getString("transaction_id");
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fetchTransactionDetail();
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
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.actionbar_close:
                dismiss();
                break;
        }
    }
    private void fetchTransactionDetail(){
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("transaction_id", transactionID));

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
                        JSONArray jsonArray = jsonObjectLoginResponse.getJSONArray("value").getJSONObject(0).getJSONArray("transaction_detail");
                        for(int i = 0; i < jsonArray.length(); i++){
                            String label = jsonArray.getJSONObject(0).getString("title");
                            String amount = jsonArray.getJSONObject(0).getString("amount");
                            String account = jsonArray.getJSONObject(0).getString("account");
                            String status = jsonArray.getJSONObject(0).getString("status");
                            String provider = jsonArray.getJSONObject(0).getString("provider");
                            String date = jsonArray.getJSONObject(0).getString("date");

                            setupView(label, amount, account, status, date, provider);
                        }
                    }
                    else if(jsonObjectLoginResponse.getString("status").equals("4")){
//                        if array list containing value mean data is finished load.
                        showSnackBar("Something Went Wrong!");
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

    private void setupView(String label, String amount, String account, String status, String date, String provider) {
        String transaction = String.valueOf(android.text.format.DateFormat.format("yyyymmddHHmmss", new java.util.Date())) + transactionID;
        transactionDetailDialogLabel.setText(label);
        transactionDetailDialogPaymentMethod.setText(account);
        transactionDetailDialogDate.setText(date);
        transactionDetailDialogTransactionID.setText(transaction);
//        transactionDetailDialogProviderIcon.setImageDrawable(cardProvider());

        if(label.equals("Top-Up")){
            transactionDetailDialogLabelMethod.setText(R.string.transaction_detail_dialog_label_top_up_method);
            transactionDetailDialogStatus.setText(R.string.transaction_detail_dialog_top_up_status);
            transactionDetailDialogAmount.setText(amount);

            transactionDetailDialogProviderIcon.setImageDrawable(cardProvider(provider));
            transactionDetailDialogProviderIcon.setVisibility(View.VISIBLE);
        }
        else{
            transactionDetailDialogStatus.setText(R.string.transaction_detail_dialog_label_earn_method);
            transactionDetailDialogAmount.setText(amountSubString(amount));
            transactionDetailDialogLabelMethod.setText(R.string.transaction_detail_dialog_label_received_method);
            transactionDetailDialogPaymentMethod.setText(status);

            transactionDetailRidePayIcon.setVisibility(View.VISIBLE);
        }
        transactionDetailDialogProgressBar.setVisibility(View.GONE);
        transactionDetailDialogParentLayout.setVisibility(View.VISIBLE);
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

    //    snackBar setting
    public void showSnackBar(String message) {
        final Snackbar snackbar = Snackbar.make(transactionDetailDialogParentLayout, message, Snackbar.LENGTH_SHORT);
        snackbar.setActionTextColor(getResources().getColor(R.color.blue));
        snackbar.setAction("Dismiss", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }

    private String amountSubString(String amount){
        return amount.substring(1, amount.length());
    }

}