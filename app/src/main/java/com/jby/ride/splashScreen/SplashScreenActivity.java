package com.jby.ride.splashScreen;

import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.stetho.Stetho;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.jby.ride.R;
import com.jby.ride.home.MainActivity;
import com.jby.ride.registration.LoginActivity;
import com.jby.ride.shareObject.AnimationUtility;
import com.jby.ride.shareObject.ApiDataObject;
import com.jby.ride.shareObject.ApiManager;
import com.jby.ride.shareObject.AsyncTaskManager;
import com.jby.ride.sharePreference.SharedPreferenceManager;
import com.jby.ride.wallet.WalletActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SplashScreenActivity extends AppCompatActivity implements View.OnClickListener,
        GoogleApiClient.OnConnectionFailedListener{
    private RelativeLayout splashScreenSignInWithGoogle, splashScreenMainLayout, splashScreenButtonLayout;
    private TextView splashScreenSignInWithAccount;
    private View splashScreenBackGroundFilter;

    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;
    private Handler handler;

    public static int LOGIN_SUCCESSFULLY = 23;
    //google login
    private static final int RC_SIGN_IN = 1001;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        objectInitialize();
        objectSetting();
    }

    private void objectInitialize() {
        splashScreenSignInWithGoogle = findViewById(R.id.activity_splash_screen_google_login_button);
        splashScreenSignInWithAccount = findViewById(R.id.activity_splash_screen_login_with_account_button);

        splashScreenButtonLayout = findViewById(R.id.activity_splash_screen_button_layout);
        splashScreenBackGroundFilter = findViewById(R.id.activity_splash_screen_background_filter);

        splashScreenMainLayout =  findViewById(R.id.activity_splash_screen_main_layout);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        handler = new Handler();
        Stetho.initializeWithDefaults(this);
    }

    private void objectSetting() {
        splashScreenSignInWithGoogle.setOnClickListener(this);
        splashScreenSignInWithAccount.setOnClickListener(this);

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener( this,  new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String newToken = instanceIdResult.getToken();
                SharedPreferenceManager.setDeviceToken(SplashScreenActivity.this, newToken);
                Log.e("newToken",newToken);
            }
        });
        //checking whether is from log out or not
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            boolean signUp = bundle.getBoolean("sign_out");
            //if really from sign out then skipped the splash screen
            if(signUp)
                checkingUserID();
            else
                showSplashScreen();
        }else
            showSplashScreen();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.activity_splash_screen_google_login_button:
                googleSignIn();
                break;
            case R.id.activity_splash_screen_login_with_account_button:
                Intent intent = new Intent(this, LoginActivity.class);
                startActivityForResult(intent, LOGIN_SUCCESSFULLY);
                break;
        }
    }

    /*-----------------------------------------------------------------------google login purpose-------------------------------------------------------*/
    private void googleSignIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        showSnackBar("Something Went Wrong :(");
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            if (acct != null) {
                String googleUserImage = "";
                String googleUserName = acct.getDisplayName();
                String googleUserEmail = acct.getEmail();
                if(acct.getPhotoUrl() != null)
                    googleUserImage = String.valueOf(acct.getPhotoUrl());
                signInWithGoogle(googleUserEmail, googleUserName, googleUserImage);
            }
        }else{
            showSnackBar("Something Went Wrong :(");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
        else if (resultCode == LOGIN_SUCCESSFULLY){
            loginSuccess();
        }
    }

    public void signInWithGoogle(String email, String username, String imagePath){
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("username", username));
        apiDataObjectArrayList.add(new ApiDataObject("email", email));
        apiDataObjectArrayList.add(new ApiDataObject("google", "1"));
        apiDataObjectArrayList.add(new ApiDataObject("image_path", imagePath));

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
    }

    //    if login then redirect
    private void checkingUserID(){
        if(!SharedPreferenceManager.getUserID(this).equals("default")){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        else{
            new AnimationUtility().fastFadeInVisible(this, splashScreenBackGroundFilter);
            new AnimationUtility().layoutSwipeUpIn(this, splashScreenButtonLayout);
        }
    }

    private void showSplashScreen(){
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkingUserID();
            }
        },2000);
    }

    private void loginSuccess(){
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    //    snackBar setting
    private void showSnackBar(String message){
        final Snackbar snackbar = Snackbar.make(splashScreenMainLayout, message, Snackbar.LENGTH_SHORT);
        snackbar.setAction("Dismiss", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }
}
