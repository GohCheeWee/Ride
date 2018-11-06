package com.jby.ride.wallet.bankCard;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jby.ride.R;
import com.jby.ride.others.CustomListView;
import com.jby.ride.others.SquareHeightLinearLayout;
import com.jby.ride.shareObject.ApiDataObject;
import com.jby.ride.shareObject.ApiManager;
import com.jby.ride.shareObject.AsyncTaskManager;
import com.jby.ride.sharePreference.SharedPreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class BankCardDialog extends DialogFragment implements SwipeRefreshLayout.OnRefreshListener,
        AbsListView.OnScrollListener, CustomListView.OnDetectScrollListener, View.OnClickListener, AddBankCardDialog.AddBankCardDialogCallBack,
        AdapterView.OnItemClickListener, SetDefaultCardDialog.SetDefaultCardDialogCallBack {

    View rootView;
    //actionbar
    private SquareHeightLinearLayout actionBarMenuIcon, actionBarCloseIcon;
    private TextView actionBarTitle;

    private CustomListView bankCardListView;
    private BankCardDialogAdapter bankCardDialogAdapter;
    private View bankCardDialogListViewFooter;
    private ArrayList<BankCardDialogObject> bankCardDialogObjectArrayList;
    private Button bankCardDialogAddCardButton;
    private RelativeLayout bankCardDialogNotFoundDialog;

    private SwipeRefreshLayout bankCardDialogSwipeRefreshLayout;
    private ProgressBar bankCardDialogProgressBar;
    //paging purpose
    private int page = 1;
    //    check background process is running or not
    private boolean isLoading = true;
    //    detect is scroll down or not
    private boolean isScroll = false;
    //    if finish load then become true
    boolean finishLoadAll = false;

    //    asyncTask
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;
    Handler handler;
    //checking this dialog is opened from where
    private String fromDialog = "";
    //interface
    public BankCardDialogCallBack bankCardDialogCallBack;

    public BankCardDialog() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.bank_card_dialog, container);
        objectInitialize();
        objectSetting();
        return rootView;
    }

    private void objectInitialize() {
        //actionBar
        actionBarMenuIcon = rootView.findViewById(R.id.actionbar_menu);
        actionBarCloseIcon = rootView.findViewById(R.id.actionbar_close);
        actionBarTitle = rootView.findViewById(R.id.actionBar_title);

        bankCardDialogSwipeRefreshLayout = rootView.findViewById(R.id.bank_card_dialog_swipe_layout);
        bankCardDialogProgressBar = rootView.findViewById(R.id.bank_card_dialog_progress_bar);
        bankCardDialogAddCardButton = rootView.findViewById(R.id.bank_card_dialog_add_card_button);
        bankCardDialogNotFoundDialog = rootView.findViewById(R.id.bank_card_dialog_not_found_layout);

        bankCardDialogListViewFooter = ((LayoutInflater) Objects.requireNonNull(Objects.requireNonNull(getActivity()).getSystemService(Context.LAYOUT_INFLATER_SERVICE))).inflate(R.layout.list_view_footer, null, false);
        bankCardListView = rootView.findViewById(R.id.bank_card_dialog_list_view);
        bankCardDialogObjectArrayList = new ArrayList<>();

        handler = new Handler();

        bankCardDialogCallBack = (BankCardDialogCallBack)getParentFragment();
    }

    private void objectSetting() {
        actionBarMenuIcon.setVisibility(View.GONE);
        actionBarCloseIcon.setVisibility(View.VISIBLE);
        actionBarCloseIcon.setOnClickListener(this);
        actionBarTitle.setText(R.string.bank_card_dialog_title);

        Bundle bundle = getArguments();
        if(bundle != null){
            fromDialog = bundle.getString("from_dialog");
        }
        bankCardDialogAdapter = new BankCardDialogAdapter(getActivity(), bankCardDialogObjectArrayList, fromDialog);

        bankCardDialogSwipeRefreshLayout.setOnRefreshListener(this);
        bankCardDialogSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.red));

        bankCardListView.setOnDetectScrollListener(this);
        bankCardListView.setOnScrollListener(this);
        bankCardListView.setOnItemClickListener(this);
        bankCardListView.setAdapter(bankCardDialogAdapter);
        bankCardDialogAddCardButton.setOnClickListener(this);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fetchAllBankCard();
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
    public void onDestroyView() {
        super.onDestroyView();
        page = 1;
        finishLoadAll = false;
    }

    private void fetchAllBankCard(){
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("user_id", SharedPreferenceManager.getUserID(getActivity())));
        apiDataObjectArrayList.add(new ApiDataObject("page", String.valueOf(page)));

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
                        JSONArray jsonArray = jsonObjectLoginResponse.getJSONArray("value").getJSONObject(0).getJSONArray("bank_card");
                        for(int i = 0; i < jsonArray.length(); i++){
                            bankCardDialogObjectArrayList.add(new BankCardDialogObject(
                                    jsonArray.getJSONObject(i).getString("provider"),
                                    jsonArray.getJSONObject(i).getString("type"),
                                    jsonArray.getJSONObject(i).getString("card_number"),
                                    jsonArray.getJSONObject(i).getString("id"),
                                    jsonArray.getJSONObject(i).getString("status")
                            ));
                        }
                        bankCardDialogAdapter.notifyDataSetChanged();
                    }
                    else if(jsonObjectLoginResponse.getString("status").equals("2")){
//                        if array list containing value mean data is finished load.
                        if(bankCardDialogObjectArrayList.size() > 0)
                            finishLoadAll = true;
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
        isLoading = false;
        bankCardListView.removeFooterView(bankCardDialogListViewFooter);
        setUpView();
    }

    @Override
    public void onRefresh() {
        page = 1;
        finishLoadAll = false;

        bankCardDialogObjectArrayList.clear();
        bankCardDialogAdapter.notifyDataSetChanged();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                bankCardDialogSwipeRefreshLayout.setRefreshing(false);
                fetchAllBankCard();
            }
        },50);
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {

    }

    @Override
    public void onScroll(AbsListView absListView, int firstVisibleItem, final int visibleItemCount, final int totalItemCount) {

        final int position = firstVisibleItem+visibleItemCount;

        if(!finishLoadAll){
            // Check if bottom has been reached
            if (position >= totalItemCount && totalItemCount > 0 && !isLoading && isScroll) {
                bankCardListView.addFooterView(bankCardDialogListViewFooter);
                isLoading = true;
                page++;
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        fetchAllBankCard();
                    }
                },200);
            }
        }
    }

    @Override
    public void onUpScrolling() {
        isScroll = false;
    }

    @Override
    public void onDownScrolling() {
        isScroll = true;
    }

    private void setUpView() {
        bankCardDialogProgressBar.setVisibility(View.GONE);

        if(bankCardDialogObjectArrayList.size() > 0)
        {
            bankCardDialogNotFoundDialog.setVisibility(View.GONE);
            bankCardListView.setVisibility(View.VISIBLE);
        }
        else{
            bankCardDialogNotFoundDialog.setVisibility(View.VISIBLE);
            bankCardListView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.bank_card_dialog_add_card_button:
                DialogFragment dialogFragment = new AddBankCardDialog();
                dialogFragment.show(this.getChildFragmentManager(), "dialog_fragment");
                break;
            case R.id.actionbar_close:
                dismiss();
                break;
        }
    }

    //    snackBar setting
    public void showSnackBar(String message) {
        final Snackbar snackbar = Snackbar.make(bankCardDialogSwipeRefreshLayout, message, Snackbar.LENGTH_SHORT);
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
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if(fromDialog.equals("")){
            DialogFragment dialogFragment = new SetDefaultCardDialog();
            FragmentManager fragmentManager = getChildFragmentManager();
            Bundle bundle = new Bundle();

            bundle.putString("card_id", bankCardDialogObjectArrayList.get(i).getId());
            dialogFragment.setArguments(bundle);
            dialogFragment.show(fragmentManager, "");
        }
        else{
            String cardNumber = bankCardDialogObjectArrayList.get(i).getCardNumber();
            String provider = bankCardDialogObjectArrayList.get(i).getProvider();
            bankCardDialogCallBack.setBankCardDetail(cardNumber, provider);
            dismiss();
        }
    }

    public void closeKeyBoard(){
        Objects.requireNonNull(Objects.requireNonNull(getDialog()).getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public void setBankCardDetail(String cardNumber, String provider) {

    }

    public interface BankCardDialogCallBack{
        void setBankCardDetail(String card_number, String provider);
    }
}