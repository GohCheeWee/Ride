package com.jby.ride.wallet.transaction;

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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
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
import com.jby.ride.wallet.bankCard.BankCardDialog;
import com.jby.ride.wallet.bankCard.BankCardDialogObject;
import com.jby.ride.wallet.transaction.transactionDetail.TransactionDetailDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TransactionDialog extends DialogFragment implements View.OnClickListener, TransactionExpandableAdapter.TransactionExpandableAdapterCallBack {

    View rootView;
    //actionbar
    private SquareHeightLinearLayout actionBarMenuIcon, actionBarCloseIcon;
    private TextView actionBarTitle;
    //expandable list
    private ExpandableListView transactionDialogExpandableListView;
    private TransactionExpandableAdapter transactionExpandableAdapter;
    private ArrayList<TransactionParentObject> transactionParentObjectArrayList;
    private ProgressBar transactionDialogProgressBar;
    private RelativeLayout transactionDialogNotFound;
    //    asyncTask
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;
    Handler handler;

    public TransactionDialog() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.transaction_dialog, container);
        objectInitialize();
        objectSetting();
        return rootView;
    }

    private void objectInitialize() {
        //actionBar
        actionBarMenuIcon = rootView.findViewById(R.id.actionbar_menu);
        actionBarCloseIcon = rootView.findViewById(R.id.actionbar_close);
        actionBarTitle = rootView.findViewById(R.id.actionBar_title);

        transactionDialogExpandableListView = rootView.findViewById(R.id.transaction_dialog_expandable_list);
        transactionDialogProgressBar = rootView.findViewById(R.id.transaction_dialog_progress_bar);
        transactionDialogNotFound = rootView.findViewById(R.id.transaction_dialog_not_found_layout);

        transactionParentObjectArrayList = new ArrayList<>();
        transactionExpandableAdapter = new TransactionExpandableAdapter(getActivity(), transactionParentObjectArrayList, this);
        handler = new Handler();

    }

    private void objectSetting() {
        actionBarMenuIcon.setVisibility(View.GONE);
        actionBarCloseIcon.setVisibility(View.VISIBLE);
        actionBarCloseIcon.setOnClickListener(this);
        actionBarTitle.setText(R.string.transation_dialog_title);

        transactionDialogExpandableListView.setAdapter(transactionExpandableAdapter);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fetchAllTransaction();
            }
        },200);

        transactionDialogExpandableListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int i) {
                transactionDialogExpandableListView.expandGroup(i);
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
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.actionbar_close:
                dismiss();
                break;
        }
    }
    private void fetchAllTransaction(){
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("user_id", SharedPreferenceManager.getUserID(getActivity())));
        apiDataObjectArrayList.add(new ApiDataObject("transaction", "1"));

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
                        JSONArray jsonArray = jsonObjectLoginResponse.getJSONArray("value").getJSONObject(0).getJSONArray("transaction");
                        for(int i = 0; i < jsonArray.length(); i++){
                            int position = -1;
                            String date = jsonArray.getJSONObject(i).getString("created_at");

                            for(int j = 0 ; j < transactionParentObjectArrayList.size(); j++){
                                //if found mean that same child record under the same date
                                if (transactionParentObjectArrayList.get(j).getDate().equals(dateSubString(date))){
                                    position = j;
                                    //stop looping
                                   break;
                                }
                            }
                            //mean this date is never added yet so create a new group view
                            if(position == -1){
                                transactionParentObjectArrayList.add(new TransactionParentObject(dateSubString(date), setChildArray(jsonArray.getJSONObject(i))));
                            }
                            // if the same date(group view) is added then add the child item
                            else{
                                transactionParentObjectArrayList.get(position).getTransactionChildObjectArrayList().add(setChildObject(jsonArray.getJSONObject(i)));
                            }
                        }
//                        Log.d("haha", "haha: " +transactionParentObjectArrayList.get(0).getTransactionChildObjectArrayList().get(17).amount);
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
        transactionExpandableAdapter.notifyDataSetChanged();
        setUpView();
    }

    private void setUpView() {
        if(transactionParentObjectArrayList.size() > 0){
            for(int i = 0; i < transactionParentObjectArrayList.size(); i++){
                transactionDialogExpandableListView.expandGroup(i);
            }
            transactionDialogNotFound.setVisibility(View.GONE);
            transactionDialogExpandableListView.setVisibility(View.VISIBLE);
        }
        else{
            transactionDialogNotFound.setVisibility(View.VISIBLE);
            transactionDialogExpandableListView.setVisibility(View.GONE);
        }
        transactionDialogProgressBar.setVisibility(View.GONE);

    }

    private TransactionChildObject setChildObject(JSONObject jsonObject){
       TransactionChildObject object = null;
        try {
            object = new TransactionChildObject(
                    jsonObject.getString("id"),
                    jsonObject.getString("amount"),
                    jsonObject.getString("title"),
                    jsonObject.getString("status")
            );

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }

    private ArrayList<TransactionChildObject> setChildArray(JSONObject jsonObject){
        ArrayList<TransactionChildObject> transactionChildObjectArrayList = new ArrayList<>();
        try {
            transactionChildObjectArrayList.add(new TransactionChildObject(
                    jsonObject.getString("id"),
                    jsonObject.getString("amount"),
                    jsonObject.getString("title"),
                    jsonObject.getString("status")
            ));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return transactionChildObjectArrayList;
    }

    private String dateSubString(String date){
        return date.substring(0 , 10);
    }

    //    snackBar setting
    public void showSnackBar(String message) {
        final Snackbar snackbar = Snackbar.make(transactionDialogExpandableListView, message, Snackbar.LENGTH_SHORT);
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
    public void openTransactionDetail(String id) {
        DialogFragment dialogFragment = new TransactionDetailDialog();
        FragmentManager fragmentManager = getChildFragmentManager();

        Bundle bundle = new Bundle();
        bundle.putString("transaction_id", id);

        dialogFragment.setArguments(bundle);
        dialogFragment.show(fragmentManager, "");
    }
}