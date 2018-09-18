package com.jby.ride.registration;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jby.ride.R;
import com.jby.ride.shareObject.ApiDataObject;
import com.jby.ride.shareObject.ApiManager;
import com.jby.ride.shareObject.AsyncTaskManager;
import com.jby.ride.shareObject.SomethingMissingDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener{
    private EditText registerActivityUsername, registerActivityEmail, registerActivityPassword, registerActivityMobile;
    private TextView registerActivityMale, registerActivityFemale;
    private String gender = "default";
    private LinearLayout registerActivityMainLayout;
    Handler handler;
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;
    //    dialog
    DialogFragment dialogFragment;
    FragmentManager fm;


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        objectInitialize();
        objectSetting();
    }

    private void objectInitialize() {
        registerActivityUsername = (EditText)findViewById(R.id.activity_register_username);
        registerActivityEmail = (EditText)findViewById(R.id.activity_register_email);
        registerActivityPassword = (EditText)findViewById(R.id.activity_register_password);
        registerActivityMobile = (EditText)findViewById(R.id.activity_register_mobile);

        registerActivityMale = (TextView)findViewById(R.id.activity_register_male);
        registerActivityFemale = (TextView)findViewById(R.id.activity_register_female);

        registerActivityMainLayout = (LinearLayout)findViewById(R.id.activity_register_main_layout);

        handler = new Handler();
        fm = getSupportFragmentManager();

    }

    private void objectSetting() {
        registerActivityMale.setOnClickListener(this);
        registerActivityFemale.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.activity_register_male:
                genderSetting(true, false);
                setGender("Male");
                break;

            case R.id.activity_register_female:
                genderSetting(false, true);
                setGender("Female");
                break;
        }
    }
//for gender purpose
    private void genderSetting(boolean male, boolean female){
       if(male)
           registerActivityMale.setTextColor(getResources().getColor(R.color.blue));
       else
           registerActivityMale.setTextColor(getResources().getColor(R.color.tranparent_white));

       if(female)
           registerActivityFemale.setTextColor(getResources().getColor(R.color.red));
       else
           registerActivityFemale.setTextColor(getResources().getColor(R.color.tranparent_white));
    }

    public String getGender(){
        return gender;
    }

    private void setGender(String gender){
        this.gender = gender;
    }
//    end of gender purpose

//    sign up
    public void checkingInput(View view){
        final String username = registerActivityUsername.getText().toString().trim();
        final String email = registerActivityEmail.getText().toString().trim();
        final String password = registerActivityPassword.getText().toString().trim();
        final String mobile = registerActivityMobile.getText().toString().trim();
        final String gender = getGender();

        String passwordFormat = "^(.{6,20})";
        String mobileFormat = "^(.{9,13})";
        closeKeyBoard();

        if(!username.equals("") && !email.equals("") && !password.equals("") && !mobile.equals("") && !gender.equals("default")){

            if(!email.matches(android.util.Patterns.EMAIL_ADDRESS.pattern()))
                showSnackBar("Invalid Email");

            else if(!password.matches(passwordFormat))
                showSnackBar("Password is too short");

            else if(!mobile.matches(mobileFormat))
                showSnackBar("Invalid number");

            else
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        signUp(username, password, email, mobile, gender);
                    }
                },200);
        }
        else
            alertDialog();
    }
    public void signUp(String username, String password, String email, String mobile, String gender){
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("username", username));
        apiDataObjectArrayList.add(new ApiDataObject("password", password));
        apiDataObjectArrayList.add(new ApiDataObject("email", email));
        apiDataObjectArrayList.add(new ApiDataObject("phone", mobile));
        apiDataObjectArrayList.add(new ApiDataObject("gender", gender));

        asyncTaskManager = new AsyncTaskManager(
                this,
                new ApiManager().register,
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
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(RegisterActivity.this, "Sign Up Successful!", Toast.LENGTH_SHORT).show();
                                backTologinActivity();
                            }
                        },200);
                    }
                    else if(jsonObjectLoginResponse.getString("status").equals("2"))
                        showSnackBar("Email is already existed");

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
//    end of sign up

//    snackBar setting
    private void showSnackBar(String message){
        final Snackbar snackbar = Snackbar.make(registerActivityMainLayout, message, Snackbar.LENGTH_SHORT);
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

//    intent setting
    private void backTologinActivity(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

//    hide keyboard
    public void closeKeyBoard(){
        View view = getCurrentFocus();
        final InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm != null && view != null)
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}
