package com.jby.ride.profile;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jby.ride.BuildConfig;
import com.jby.ride.R;
import com.jby.ride.home.MainActivity;
import com.jby.ride.others.SquareHeightLinearLayout;
import com.jby.ride.shareObject.ApiDataObject;
import com.jby.ride.shareObject.ApiManager;
import com.jby.ride.shareObject.AsyncTaskManager;
import com.jby.ride.sharePreference.SharedPreferenceManager;
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

import static com.theartofdev.edmodo.cropper.CropImageView.CropShape.OVAL;
import static com.theartofdev.edmodo.cropper.CropImageView.Guidelines.ON_TOUCH;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener, UploadImageDialog.UploadImageDialogListener {
    //    actionBar
    private SquareHeightLinearLayout actionBarMenuIcon, actionBarCloseIcon, actionBarLogout;
    private TextView actionBarTitle;

    //    dialog
    private CircleImageView editProfileDialogPicture;
    private ImageView editProfileGender;
    private TextView editProfileDialogLabelUsername, editProfileDialogRating, editProfileDialogReview;
    private TextView editProfileDialogLabelCancellation;
    private EditText editTextProfileDialogUsername, editTextProfileDialogMobile, editTextProfileDialogEmail;
    private Button editTextProfileDialogSaveButton;
    private LinearLayout editTextProfileMainLayout;

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
        actionBarMenuIcon = (SquareHeightLinearLayout)findViewById(R.id.actionbar_menu);
        actionBarCloseIcon = (SquareHeightLinearLayout)findViewById(R.id.actionbar_close);
        actionBarLogout = (SquareHeightLinearLayout)findViewById(R.id.actionbar_logout);
        actionBarTitle = (TextView)findViewById(R.id.actionBar_title);

        editProfileDialogPicture = (CircleImageView) findViewById(R.id.edit_profile_dialog_profile_picture);
        editProfileGender = (ImageView) findViewById(R.id.activity_profile_gender_icon);

        editProfileDialogLabelUsername = (TextView) findViewById(R.id.edit_profile_dialog_profile_username);
        editProfileDialogRating = (TextView) findViewById(R.id.edit_profile_dialog_profile_rating);
        editProfileDialogReview = (TextView) findViewById(R.id.edit_profile_dialog_profile_review);
        editProfileDialogLabelCancellation = (TextView) findViewById(R.id.edit_profile_dialog_profile_cancellation);

        editTextProfileDialogUsername = (EditText) findViewById(R.id.edit_profile_dialog_profile_edit_text_username);
        editTextProfileDialogMobile = (EditText) findViewById(R.id.edit_profile_dialog_profile_edit_text_mobile);
        editTextProfileDialogEmail = (EditText) findViewById(R.id.edit_profile_dialog_profile_edit_text_email);

        editTextProfileMainLayout = (LinearLayout)findViewById(R.id.activity_profile_main_layout);

        editTextProfileDialogSaveButton = (Button) findViewById(R.id.edit_profile_dialog_save_button);
        fm = getSupportFragmentManager();
        handler = new Handler();
    }

    private void objectSetting() {
//        actionBar
        actionBarTitle.setText(R.string.edit_profile_dialog_title);
        actionBarMenuIcon.setVisibility(View.GONE);
        actionBarCloseIcon.setVisibility(View.VISIBLE);
        actionBarLogout.setVisibility(View.VISIBLE);

        actionBarCloseIcon.setOnClickListener(this);
        actionBarLogout.setOnClickListener(this);

        editProfileDialogPicture.setOnClickListener(this);
        editTextProfileDialogSaveButton.setOnClickListener(this);

        getUserDetail();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.actionbar_close:
                onBackPressed();
                break;
            case R.id.actionbar_logout:
                requestCode = MainActivity.LOGOUT_REQUEST;
                onBackPressed();
                break;
            case R.id.edit_profile_dialog_profile_picture:
                dialogFragment = new UploadImageDialog();
                dialogFragment.show(fm, "");
                break;
            case R.id.edit_profile_dialog_save_button:
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        saveProfile();
                    }
                },200);
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
                        setUpUserDetail(username, email, gender, phone, profile_picture);

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

    private void setUpUserDetail(String username, String email, String gender, String phone, String profile_picture) {
        editProfileDialogLabelUsername.setText(username);
        editTextProfileDialogUsername.setText(username);

        editTextProfileDialogEmail.setText(email);
        editTextProfileDialogMobile.setText(phone);

        if(profile_picture != null){
            profile_picture = prefix + profile_picture;

            Picasso.get()
                    .load(profile_picture)
                    .error(R.drawable.loading_gif)
                    .into(editProfileDialogPicture);
        }

        if(gender.equals("Male"))
            editProfileGender.setImageDrawable(getResources().getDrawable(R.drawable.activity_profile_male_icon));
        else
            editProfileGender.setImageDrawable(getResources().getDrawable(R.drawable.activity_profile_female_icon));

    }

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

    private void saveProfile(){
        String username = editTextProfileDialogUsername.getText().toString();
        String phone = editTextProfileDialogMobile.getText().toString();
        String userID = SharedPreferenceManager.getUserID(this);

        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("user_id", userID));
        apiDataObjectArrayList.add(new ApiDataObject("username", username));
        apiDataObjectArrayList.add(new ApiDataObject("phone", phone));

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
                        requestCode = MainActivity.UPDATE_PROFILE_REQUEST;
                        showSnackBar();
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

    //    snackBar setting
    private void showSnackBar(){
        final Snackbar snackbar = Snackbar.make(editTextProfileMainLayout, "Updated Successful", Snackbar.LENGTH_SHORT);
        snackbar.setAction("Dismiss", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(requestCode, intent);
        super.onBackPressed();
    }

}
