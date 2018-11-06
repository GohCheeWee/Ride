package com.jby.ride.profile;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.jby.ride.BuildConfig;
import com.jby.ride.R;
import com.jby.ride.home.MainActivity;
import com.jby.ride.others.dialog.DriverFoundDialog;
import com.jby.ride.others.dialog.DriverIsOtwDialog;
import com.jby.ride.others.dialog.RatingDialog;
import com.jby.ride.ride.RideActivity;
import com.jby.ride.ride.comfirm.startRoute.StartRouteActivity;
import com.jby.ride.shareObject.ApiDataObject;
import com.jby.ride.shareObject.ApiManager;
import com.jby.ride.shareObject.AsyncTaskManager;
import com.jby.ride.sharePreference.SharedPreferenceManager;
import com.jgabrielfreitas.core.BlurImageView;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.jby.ride.home.MainActivity.LOGOUT_REQUEST;
import static com.jby.ride.others.MyFireBaseMessagingService.NotificationBroadCast;
import static com.theartofdev.edmodo.cropper.CropImageView.CropShape.OVAL;
import static com.theartofdev.edmodo.cropper.CropImageView.Guidelines.ON_TOUCH;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener, UploadImageDialog.UploadImageDialogListener,
        DriverFoundDialog.DriverFoundDialogCallBack, RatingDialog.RatingDialogCallBack, DriverIsOtwDialog.DriverIsOtwDialogCallBack,
        View.OnFocusChangeListener, GoogleApiClient.OnConnectionFailedListener {
    //    dialog
    private CircleImageView editProfileDialogPicture;
    private BlurImageView editProfileBackgroundImage;
    private ImageView editProfileBackButton, editProfileClearMobileButton, editProfileLogOutButton;
    private TextView editProfileGenderMale, editProfileGenderFemale;
    private TextView editProfileNumCompleteRide;
    private TextView editProfileDialogLabelUsername, editProfileDialogEditProfileButton;
    private EditText editTextProfileDialogMobile, editTextProfileDialogEmail;
    private LinearLayout editTextProfileMainLayout;
    private String gender = null;
    private boolean isUpdate = true;
//    path
    public static String prefix = "http://188.166.186.198/~cheewee/ride/frontend/user/profile/profile_picture/";

    //    asyncTask
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;
    Handler handler;

//    upload image purpose
    public static final int MY_READ_PERMISSION_REQUEST_CODE = 1;
    public static final int PICK_IMAGE_REQUEST = 2;
    public static final int CAMERA_REQUEST = 10;
    public static final int REQUEST_ACCESS_CAMERA = 3;
    private String newPhotoUrl;
    private Uri photoUri, croppedUrl;
    private String imageCode = null;
//    google sign purpose
    private GoogleApiClient mGoogleApiClient;

    //    dialog
    DialogFragment dialogFragment;
    Bundle bundle;
    FragmentManager fm;
//    action hanlding purpose
    private int requestCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        objectInitialize();
        objectSetting();
    }

    private void objectInitialize() {
        editProfileDialogEditProfileButton = findViewById(R.id.activity_profile_edit_profile_button);

        editProfileDialogPicture = (CircleImageView) findViewById(R.id.edit_profile_dialog_profile_picture);
        editProfileBackButton = findViewById(R.id.activity_profile_cancel_button);
        editProfileLogOutButton = findViewById(R.id.activity_profile_log_out_button);
        editProfileBackgroundImage = findViewById(R.id.edit_profile_dialog_background_picture);
        editProfileDialogLabelUsername = (TextView) findViewById(R.id.edit_profile_dialog_profile_username);

        editTextProfileDialogMobile = (EditText) findViewById(R.id.edit_profile_dialog_profile_edit_text_mobile);
        editProfileClearMobileButton = findViewById(R.id.activity_profile_clear_phone_button);
        editTextProfileDialogEmail = (EditText) findViewById(R.id.edit_profile_dialog_profile_edit_text_email);
        editProfileGenderMale = findViewById(R.id.activity_profile_gender_male);
        editProfileGenderFemale = findViewById(R.id.activity_profile_gender_female);
        editProfileNumCompleteRide = findViewById(R.id.edit_profile_dialog_profile_num_complete_ride);

        editTextProfileMainLayout = (LinearLayout)findViewById(R.id.activity_profile_main_layout);

        fm = getSupportFragmentManager();
        handler = new Handler();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    private void objectSetting() {
        if(getIntent().getExtras() != null){
            boolean requestUpdate = getIntent().getBooleanExtra("requestUpdate", false);
            if(requestUpdate){
                //for handle on back press purpose
                isUpdate = false;
                showSnackBar("Please update your profile");
            }
        }
        editProfileBackButton.setOnClickListener(this);
        editProfileDialogPicture.setOnClickListener(this);
        editProfileDialogEditProfileButton.setOnClickListener(this);
        editProfileLogOutButton.setOnClickListener(this);
        editProfileGenderMale.setOnClickListener(this);
        editProfileGenderFemale.setOnClickListener(this);
        editProfileClearMobileButton.setOnClickListener(this);
        editProfileDialogLabelUsername.setOnFocusChangeListener(this);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getUserDetail();
            }
        },200);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.activity_profile_cancel_button:
                onBackPressed();
                break;
            case R.id.activity_profile_log_out_button:
                logOutSetting();
                break;
            case R.id.edit_profile_dialog_profile_picture:
                dialogFragment = new UploadImageDialog();
                dialogFragment.show(fm, "");
                break;
            case R.id.activity_profile_edit_profile_button:
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        checkingInput();
                    }
                },200);
                break;
            case R.id.activity_profile_gender_male:
                gender = "Male";
                genderSetting(gender);
                break;
            case R.id.activity_profile_gender_female:
                gender = "Female";
                genderSetting(gender);
                break;
            case R.id.activity_profile_clear_phone_button:
                editTextProfileDialogMobile.setText("");
                break;
        }
    }

    private void getUserDetail(){

        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("user_id", SharedPreferenceManager.getUserID(this)));
        asyncTaskManager = new AsyncTaskManager(
                this,
                new ApiManager().userProfile,
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
                        String username = jsonObjectLoginResponse.getJSONObject("value").getString("username");
                        String email = jsonObjectLoginResponse.getJSONObject("value").getString("email");
                        String gender = jsonObjectLoginResponse.getJSONObject("value").getString("gender");
                        String phone = jsonObjectLoginResponse.getJSONObject("value").getString("phone");
                        String profile_picture = jsonObjectLoginResponse.getJSONObject("value").getString("profile_picture");
                        String completeRide = jsonObjectLoginResponse.getString("num_complete_ride");
                        setUpUserDetail(username, email, gender, phone, profile_picture, completeRide);

                    }
                    else if(jsonObjectLoginResponse.getString("status").equals("2")){
                        Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show();
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

    private void setUpUserDetail(String username, String email, String gender, String phone, String profile_picture, String completeRide) {
        editProfileDialogLabelUsername.setText(username);

        editTextProfileDialogEmail.setText(email);
        editTextProfileDialogMobile.setText(phone);
        editProfileNumCompleteRide.setText(completeRide);
        if(!profile_picture.equals("")){
            if(!profile_picture.substring(0, 8).equals("https://"))
                profile_picture = prefix + profile_picture;

            Picasso.get()
                    .load(profile_picture)
                    .error(R.drawable.acitivty_main_user)
                    .into(editProfileDialogPicture);

            Picasso.get()
                    .load(profile_picture)
                    .error(R.drawable.acitivty_main_user)
                    .into(editProfileBackgroundImage);

            if(editProfileBackgroundImage.getDrawable() != null)
                editProfileBackgroundImage.setBlur(5);
        }

        this.gender = gender;
        genderSetting(gender);
    }

    private void genderSetting(String gender){
        if(gender.equals("Male")){
            editProfileGenderMale.setTextColor(getResources().getColor(R.color.blue));
            editProfileGenderFemale.setTextColor(getResources().getColor(R.color.disable_color));
        }
        else{
            editProfileGenderFemale.setTextColor(getResources().getColor(R.color.red));
            editProfileGenderMale.setTextColor(getResources().getColor(R.color.disable_color));
        }
    }

/*--------------------------------------------------------------camera and gellery setup purpose----------------------------------------------*/
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_READ_PERMISSION_REQUEST_CODE
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else if (requestCode == REQUEST_ACCESS_CAMERA
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCameraApp();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST) {
            if (data != null && resultCode == RESULT_OK) {
//                getting photo from gallery
                photoUri = data.getData();
                cropImage(photoUri);
            }

        } else if (requestCode == CAMERA_REQUEST) {
            if (resultCode == RESULT_OK) {
//                getting image from camera
                photoUri = Uri.parse(newPhotoUrl);

                cropImage(photoUri);
            }
        }
        else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                croppedUrl = result.getUri();
                try
                {
                    Bitmap croppedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), croppedUrl);
                    editProfileDialogPicture.setImageBitmap(croppedImage);
                    editProfileBackgroundImage.setImageBitmap(croppedImage);

                    imageCode = encodeToBase64(getResizedBitmap(croppedImage, 500));

                }
                catch (Exception e)
                {
                    //handle exception
                }
            }
        }
        else {
            Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show();
        }
    }

    public void checkGalleryPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_READ_PERMISSION_REQUEST_CODE);
            } else {
                openGallery();
            }
        }
    }

    public void checkCapturePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCameraApp();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.CAMERA,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE}
                        , REQUEST_ACCESS_CAMERA);
            } else {
                openCameraApp();
            }
        }
    }

    private void openGallery() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMinCropWindowSize(250,250)
                .setAspectRatio(1, 1)
                .setActivityTitle("Profile")
                .setCropShape(OVAL)
                .setAutoZoomEnabled(false)
                .setActivityMenuIconColor(R.color.red)
                .start(this);
    }

    private void openCameraApp() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile;
            try {
                photoFile = createImageFile();

                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(this,  BuildConfig.APPLICATION_ID + ".provider", createImageFile());
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, CAMERA_REQUEST);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
//create file for storing
    private File createImageFile() throws IOException {
        String timeStamp = String.valueOf(android.text.format.DateFormat.format("yyyyMMMdd_HHmmss", new java.util.Date()));

        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), "Camera");
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        newPhotoUrl = "file:" + image.getAbsolutePath();
        return image;
    }
//resize image
    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }
//encode
    public static String encodeToBase64(Bitmap image) {
        ByteArrayOutputStream bAOS = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, bAOS);
        byte[] b = bAOS.toByteArray();
        return Base64.encodeToString(b,Base64.DEFAULT);
    }

    private void cropImage(Uri photo){
        CropImage.activity(photo)
                .setMinCropWindowSize(250,250)
                .setAspectRatio(1, 1)
                .setActivityTitle("Profile")
                .setCropShape(OVAL)
                .setGuidelines(ON_TOUCH)
                .setAutoZoomEnabled(false)
                .setActivityMenuIconColor(R.color.red)
                .start(this);
    }
    private void checkingInput(){
        String username = editProfileDialogLabelUsername.getText().toString();
        String phone = editTextProfileDialogMobile.getText().toString();
        if(!username.equals("") && !phone.equals("") && gender != null)
            saveProfile(username, phone);
        else
            showSnackBar("Sorry, every field above is require :)");
    }
    private void saveProfile(String username, String phone){
        String userID = SharedPreferenceManager.getUserID(this);

        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("user_id", userID));
        apiDataObjectArrayList.add(new ApiDataObject("username", username));
        apiDataObjectArrayList.add(new ApiDataObject("phone", phone));
        apiDataObjectArrayList.add(new ApiDataObject("gender", gender));

        if(imageCode != null){
            String timeStamp = String.valueOf(android.text.format.DateFormat.format("yyyyMMMdd_HHmmss", new java.util.Date()));
            String image_name = userID + timeStamp + ".jpg";
            apiDataObjectArrayList.add(new ApiDataObject("profile_picture_name", image_name));
            apiDataObjectArrayList.add(new ApiDataObject("profile_picture", imageCode));
        }

        asyncTaskManager = new AsyncTaskManager(
                this,
                new ApiManager().userProfile,
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
                        isUpdate = true;
                        requestCode = MainActivity.UPDATE_PROFILE_REQUEST;
                        showSnackBar("Update Successfully");
                    }
                    else if(jsonObjectLoginResponse.getString("status").equals("2")){
                        Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onBackPressed() {
        String phone = editTextProfileDialogMobile.getText().toString().trim();
        if(gender != null && !phone.equals("") && isUpdate){
            Intent intent = new Intent();
            setResult(requestCode, intent);
            super.onBackPressed();
        }
        else
            showSnackBar("Please update your profile");
    }
/*-------------------------------------------------------------when ride is accepted by driver-----------------------------------------------------*/
    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(NotificationBroadCast));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String id = intent.getStringExtra("id");
            String matchRideId;
            switch(id){
                case "3":
                    matchRideId = intent.getStringExtra("match_ride_id");
                    popOutRatingDialog(matchRideId);
                    break;
                case "4":
                    matchRideId = intent.getStringExtra("match_ride_id");
                    popOutDriverFoundDialog(matchRideId);
                    break;
                case "5":
                    String driverRideID = intent.getStringExtra("driver_ride_id");
                    popOutDriverIsOnTheWayDialog(driverRideID);
                    break;
            }
        }
    };
    private void popOutDriverFoundDialog(String matchRideId){
        bundle = new Bundle();
        bundle.putString("match_ride_id", matchRideId);

        dialogFragment = new DriverFoundDialog();
        dialogFragment.setArguments(bundle);
        dialogFragment.show(fm, "");
    }

    @Override
    public void acceptDriver() {
        Intent intent = new Intent(this, RideActivity.class);
        intent.putExtra("accept", "Accept");
        startActivity(intent);
    }

    private void popOutRatingDialog(String matchRideId){
        Bundle bundle = new Bundle();
        bundle.putString("match_ride_id", matchRideId);

        DialogFragment dialogFragment = new RatingDialog();
        dialogFragment.setArguments(bundle);
        dialogFragment.show(fm, "");
    }

    private void popOutDriverIsOnTheWayDialog(String driverRideID){
        Bundle bundle = new Bundle();
        bundle.putString("driver_ride_id", driverRideID);

        DialogFragment dialogFragment = new DriverIsOtwDialog();
        dialogFragment.setArguments(bundle);
        dialogFragment.show(fm, "");
    }

    @Override
    public void redirectToStartRideActivity(String matchRideId) {
        Bundle bundle = new Bundle();
        bundle.putString("match_ride_id", matchRideId);
        startActivity(new Intent(this, StartRouteActivity.class).putExtras(bundle));
    }

    @Override
    public void showSnackBar(String message) {
        final Snackbar snackbar = Snackbar.make(editTextProfileMainLayout, message, Snackbar.LENGTH_SHORT);
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
    public void refresh() {

    }

    @Override
    public void onFocusChange(View view, boolean b) {
        switch (view.getId()){
            case R.id.edit_profile_dialog_profile_username:
                if(b)
                    editProfileDialogLabelUsername.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                else
                    editProfileDialogLabelUsername.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.transparent)));
                break;

        }
    }
    /*-------------------------------------------------------------end of acceted ride by driver----------------------------------------------------------*/
    /*-----------------------------------------------------------------sign out setting---------------------------------------------------------------*/

    private void logOutSetting(){
        googleSignOut();
        requestCode = LOGOUT_REQUEST;
        onBackPressed();
    }
    private void googleSignOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        showSnackBar("Something Went Wrong. :(");
    }
}
