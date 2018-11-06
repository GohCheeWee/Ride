package com.jby.ride.registration;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jby.ride.R;
import com.jby.ride.shareObject.ApiDataObject;
import com.jby.ride.shareObject.ApiManager;
import com.jby.ride.shareObject.AsyncTaskManager;
import com.jby.ride.shareObject.SomethingMissingDialog;
import com.jby.ride.sharePreference.SharedPreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.jby.ride.splashScreen.SplashScreenActivity.LOGIN_SUCCESSFULLY;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{
    private EditText loginActivityEmail, loginActivityPassword;
    private TextView loginActivityForgotPassword;
    private ImageView loginActivityShowPassword, loginActivityCancelEmail;
    private LinearLayout loginActivityMainLayout;
    private ProgressBar loginActivityProgressBar;
    private boolean show = true;
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;
    private Handler handler;
//    dialog
    DialogFragment dialogFragment;
    FragmentManager fm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        objectInitialize();
        objectSetting();
    }

    private void objectInitialize() {
        loginActivityEmail = (EditText)findViewById(R.id.activity_login_email);
        loginActivityPassword = (EditText)findViewById(R.id.activity_login_password);

        loginActivityForgotPassword = (TextView)findViewById(R.id.activity_login_forgot_password);

        loginActivityShowPassword = (ImageView)findViewById(R.id.activity_login_show_password);
        loginActivityCancelEmail = (ImageView)findViewById(R.id.activity_login_cancel_email);

        loginActivityMainLayout = (LinearLayout)findViewById(R.id.activity_login_main_layout);
        loginActivityProgressBar = (ProgressBar)findViewById(R.id.login_activity_progress_bar);

        handler = new Handler();
        fm = getSupportFragmentManager();

    }

    private void objectSetting() {
        loginActivityShowPassword.setOnClickListener(this);
        loginActivityCancelEmail.setOnClickListener(this);
    }

    public void signUp(View view){
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    public void closeKeyBoard(){
        View view = getCurrentFocus();
        final InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm != null && view != null)
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.activity_login_show_password:
                showPasswordSetting();
                break;
            case R.id.activity_login_cancel_email:
                loginActivityEmail.setText("");
                break;
        }
    }

//    show/ hide password setting
    private void showPasswordSetting(){
        if(show){
            loginActivityShowPassword.setImageDrawable(getResources().getDrawable(R.drawable.activity_login_hide_icon));
            loginActivityPassword.setTransformationMethod(null);
            show = false;
        }
        else{
            loginActivityShowPassword.setImageDrawable(getResources().getDrawable(R.drawable.activity_login_show_icon));
            loginActivityPassword.setTransformationMethod(new PasswordTransformationMethod());
            show = true;
        }
    }

//    sign in setting
    public void checking(View v){
        loginActivityProgressBar.setVisibility(View.VISIBLE);
        final String email = loginActivityEmail.getText().toString().trim();
        final String password = loginActivityPassword.getText().toString().trim();
        closeKeyBoard();

        if(!email.equals("") && !password.equals(""))
        {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    signIn(email, password);
                }
            },200);
        }
        else
            alertDialog();

    }

    public void signIn(String email, String password){
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("password", password));
        apiDataObjectArrayList.add(new ApiDataObject("email", email));

        asyncTaskManager = new AsyncTaskManager(
                this,
                new ApiManager().login,
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
                    if (jsonObjectLoginResponse.getString("status").equals("1")) {
//                        setup user detail
                        whenLoginSuccessful(jsonObjectLoginResponse);

                    }
                    else if(jsonObjectLoginResponse.getString("status").equals("2")){
                        showSnackBar("Invalid email or password");
                        loginActivityProgressBar.setVisibility(View.GONE);
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
    }

    public void whenLoginSuccessful(JSONObject jsonObject){
        try {
            String userID = jsonObject.getString("user_id");
            String userName = jsonObject.getString("username");
            SharedPreferenceManager.setUserID(this, userID);
            SharedPreferenceManager.setUserName(this, userName);
            registerRiderToken();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void registerRiderToken(){
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("user_id", SharedPreferenceManager.getUserID(this)));
        apiDataObjectArrayList.add(new ApiDataObject("token", SharedPreferenceManager.getDeviceToken(this)));

        asyncTaskManager = new AsyncTaskManager(
                this,
                new ApiManager().registerToken,
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
                    if (jsonObjectLoginResponse.getString("status").equals("1")) {
//                        setup user detail
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                              loginSuccess();
                            }
                        },200);
                    }
                    else if(jsonObjectLoginResponse.getString("status").equals("2"))
                        showSnackBar("Invalid email or password");

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
        loginActivityProgressBar.setVisibility(View.GONE);
    }

    //    snackBar setting
    private void showSnackBar(String message){
        final Snackbar snackbar = Snackbar.make(loginActivityMainLayout, message, Snackbar.LENGTH_SHORT);
        snackbar.setAction("Dismiss", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }

//    dialogSetting
    private void alertDialog(){
        dialogFragment = new SomethingMissingDialog();
        dialogFragment.show(fm, "");
    }

    private void loginSuccess(){
        setResult(LOGIN_SUCCESSFULLY);
        onBackPressed();
    }
}
