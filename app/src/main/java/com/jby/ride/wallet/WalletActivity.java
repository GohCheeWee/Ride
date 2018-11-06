package com.jby.ride.wallet;

import android.content.Context;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jby.ride.R;
import com.jby.ride.shareObject.AnimationUtility;
import com.jby.ride.shareObject.ApiDataObject;
import com.jby.ride.shareObject.ApiManager;
import com.jby.ride.shareObject.AsyncTaskManager;
import com.jby.ride.sharePreference.SharedPreferenceManager;
import com.jby.ride.wallet.bankCard.BankCardDialog;
import com.jby.ride.wallet.topUp.TopUpDialog;
import com.jby.ride.wallet.transaction.TransactionDialog;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class WalletActivity extends AppCompatActivity implements View.OnClickListener,
        TopUpDialog.TopUpDialogCallBack {
    private ImageView walletActivityBackButton;
    private CardView walletActivityTopUpButton, walletActivityHistoryButton, walletActivityBankCardButton;
    private TextView walletActivityBalance;
    private AppBarLayout walletActivityToolBarLayout;
    private NestedScrollView walletActivityNestedScrollView;
    private ProgressBar walletActivityProgressBar;
    private Handler handler;
    //dialog purpose
    private FragmentManager fm;
    private DialogFragment dialogFragment;
    //balance
    private String currentBalance = "";
    //prevent two time effect
    private boolean isLoad = false;
    //    asyncTask
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);
        objectInitialize();
        objectSetting();
    }

    private void objectInitialize() {
        walletActivityBackButton = findViewById(R.id.activity_wallet_back_button);

        walletActivityTopUpButton = findViewById(R.id.activity_wallet_top_up_button);
        walletActivityHistoryButton = findViewById(R.id.activity_wallet_history_button);
        walletActivityBankCardButton = findViewById(R.id.activity_wallet_bank_card_button);

        walletActivityBalance = findViewById(R.id.activity_wallet_balance);

        walletActivityToolBarLayout = findViewById(R.id.activity_wallet_tool_bar_layout);
        walletActivityNestedScrollView = findViewById(R.id.activity_wallet_scroll_view);
        walletActivityProgressBar = findViewById(R.id.activity_wallet_progress_bar);
        handler = new Handler();
        fm = getSupportFragmentManager();
    }

    private void objectSetting() {
        walletActivityBackButton.setOnClickListener(this);
        walletActivityTopUpButton.setOnClickListener(this);
        walletActivityHistoryButton.setOnClickListener(this);
        walletActivityBankCardButton.setOnClickListener(this);

        getCurrentBalanceSetup();
    }

    private void setUpView(){
        walletActivityProgressBar.setVisibility(View.GONE);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                new AnimationUtility().layoutSwipeDownIn(WalletActivity.this, walletActivityToolBarLayout);
                new AnimationUtility().fadeInVisible(WalletActivity.this, walletActivityNestedScrollView);
            }
        },100);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.activity_wallet_back_button:
                finish();
                break;
            case R.id.activity_wallet_top_up_button:
                view.setEnabled(false);
                openTopUpDialog();
                break;
            case R.id.activity_wallet_history_button:
                openTransactionDialog();
                break;
            case R.id.activity_wallet_bank_card_button:
                dialogFragment = new BankCardDialog();
                dialogFragment.show(fm, "");
                break;
        }
    }

    public void getCurrentBalanceSetup(){
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getCurrentBalance();
            }
        },200);
    }

    private void getCurrentBalance(){
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("user_id", SharedPreferenceManager.getUserID(this)));
        apiDataObjectArrayList.add(new ApiDataObject("balance","1"));

        asyncTaskManager = new AsyncTaskManager(
                this,
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
                        currentBalance = jsonObjectLoginResponse.getJSONObject("balance").getString("balance");
                        if(currentBalance.equals("null"))
                            currentBalance = "0.00";
                        else
                            currentBalance += ".00";
                        walletActivityBalance.setText(currentBalance);
                    }
                    else if(jsonObjectLoginResponse.getString("status").equals("2")){
                        showSnackBar("Something Went Wrong!");
                    }
                }
                else {
                    Toast.makeText(this, "Network Error!", Toast.LENGTH_SHORT).show();
                }

            } catch (InterruptedException e) {
                Toast.makeText(this, "Interrupted Exception!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (ExecutionException e) {
                Toast.makeText(this, "Execution Exception!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (JSONException e) {
                Toast.makeText(this, "JSON Exception!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (TimeoutException e) {
                Toast.makeText(this, "Connection Time Out!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
        if(!isLoad)
            setUpView();
        isLoad = true;
    }

    private void openTopUpDialog(){
        DialogFragment dialogFragment = new TopUpDialog();
        dialogFragment.show(fm, "");

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                walletActivityTopUpButton.setEnabled(true);
            }
        },200);
    }

    private void openTransactionDialog(){
        DialogFragment dialogFragment = new TransactionDialog();

        dialogFragment.show(fm, "");
    }

    //    snackBar setting
    public void showSnackBar(String message) {
        final Snackbar snackbar = Snackbar.make(walletActivityNestedScrollView, message, Snackbar.LENGTH_SHORT);
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
    public void checkingBalance(String currentBalance) {

    }

    @Override
    public void setCashAsPayment() {

    }

    @Override
    public void closeKeyBoard(){
        final InputMethodManager imm = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm != null)
            imm.hideSoftInputFromWindow(walletActivityNestedScrollView.getWindowToken(), 0);
    }
}
