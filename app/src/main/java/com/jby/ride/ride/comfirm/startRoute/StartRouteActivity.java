package com.jby.ride.ride.comfirm.startRoute;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.jby.ride.R;
import com.jby.ride.others.SquareHeightLinearLayout;
import com.jby.ride.ride.comfirm.dialog.CompletedRouteDialog;
import com.jby.ride.shareObject.ApiDataObject;
import com.jby.ride.shareObject.ApiManager;
import com.jby.ride.shareObject.AsyncTaskManager;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.jby.ride.others.MyFireBaseMessagingService.NotificationBroadCast;

public class StartRouteActivity extends AppCompatActivity implements View.OnClickListener,
        OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener,
        CompletedRouteDialog.CompletedRouteDialogCallBack{
    private SquareHeightLinearLayout actionBarMenuIcon, actionBarCloseIcon, actionBarLogout;
    private TextView actionBarTitle;

    private MapFragment map;
    private TextView startRouteActivityStatus, startRouteActivityCarPlate,  startRouteActivityCarModel;
    private TextView  startRouteActivityDriverName;
    private ImageView  startRouteActivityCall,  startRouteActivityMessage;
    private CircleImageView  startRouteActivityDriverProfilePicture;

    private String matchedRideId;
    private String driverPhoneNumber;
//    update purpose
    private int requestCode;
    //    path
    private static String profile_prefix = "http://188.166.186.198/~cheewee/ride/frontend/driver/profile/driver_profile_picture/";
    //    asyncTask
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;
    Handler handler;
    //    map setting
    private GoogleMap googleMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
//    fragment purpose
    private DialogFragment dialogFragment;
    private FragmentManager fm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_route);
        objectInitialize();
        objectSetting();
    }

    private void objectInitialize() {
        actionBarMenuIcon = (SquareHeightLinearLayout)findViewById(R.id.actionbar_menu);
        actionBarCloseIcon = (SquareHeightLinearLayout)findViewById(R.id.actionbar_close);
        actionBarLogout = (SquareHeightLinearLayout)findViewById(R.id.actionbar_logout);
        actionBarTitle = (TextView)findViewById(R.id.actionBar_title);

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.activity_start_route_google_map));

        startRouteActivityStatus = (TextView)findViewById(R.id.activity_start_route_status);
        startRouteActivityCarPlate = (TextView)findViewById(R.id.activity_start_route_car_plate);
        startRouteActivityCarModel = (TextView)findViewById(R.id.activity_start_route_car_model);
        startRouteActivityDriverName = (TextView)findViewById(R.id.acitvity_start_route_driver_name);

        startRouteActivityCall = (ImageView)findViewById(R.id.activity_start_route_call_icon);
        startRouteActivityMessage = (ImageView)findViewById(R.id.activity_start_route_message_icon);

        startRouteActivityStatus = (TextView)findViewById(R.id.activity_start_route_status);
        startRouteActivityDriverProfilePicture = (CircleImageView)findViewById(R.id.activity_start_route_driver_icon);
        handler = new Handler();
        fm = getSupportFragmentManager();
    }

    private void objectSetting() {
        //        actionBar
        actionBarMenuIcon.setVisibility(View.GONE);
        actionBarCloseIcon.setVisibility(View.VISIBLE);
        actionBarTitle.setText(R.string.start_route_activity);

        actionBarCloseIcon.setOnClickListener(this);
        startRouteActivityCall.setOnClickListener(this);
        startRouteActivityMessage.setOnClickListener(this);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            matchedRideId = bundle.getString("match_ride_id");
        }

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getDriverDetail();
            }
        },200);
        initializeMap();
    }

    //<-----------------------------------Map and  GPS setting-------------------------------------------------------->
    public void initializeMap() {
        map.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
//                if permission is granted then get my location
                buildGoogleApiClient();
                googleMap.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
        }

//        setButtonPosition();
        googleMap.setMyLocationEnabled(true);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    //
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(50);
        mLocationRequest.setFastestInterval(50);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (mGoogleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(16));
        Log.d("StartRouteActivity", "Location: " + location);

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    //<-----------------------------------End of Map and  GPS setting-------------------------------------------------------->
    /*----------------------------------------------------call purpose-------------------------------------------------------*/
    public static final int MY_PERMISSIONS_REQUEST_PHONE_CALL = 10;

    public boolean checkPhoneCallPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CALL_PHONE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                ActivityCompat.requestPermissions(StartRouteActivity.this,
                        new String[]{Manifest.permission.CALL_PHONE}, MY_PERMISSIONS_REQUEST_PHONE_CALL);

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CALL_PHONE},
                        MY_PERMISSIONS_REQUEST_PHONE_CALL);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_PHONE_CALL: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.CALL_PHONE)
                            == PackageManager.PERMISSION_GRANTED) {
                        //Request location updates:
                        onCall();
                    }
                } else {
                    Toast.makeText(this, "Unable to make a phone call with permission!", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public void phoneCallPermission() {
        if (checkPhoneCallPermission()) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CALL_PHONE)
                    == PackageManager.PERMISSION_GRANTED) {
                onCall();
            }
        }
    }

    public void onCall() {
        String phoneNo = driverPhoneNumber;
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:"+phoneNo));    //this is the phone number calling

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            //request permission from user if the app hasn't got the required permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE},   //request specific permission from user
                    MY_PERMISSIONS_REQUEST_PHONE_CALL);
        }else {     //have got permission
            try{
                startActivity(callIntent);  //call activity and make phone call
            }
            catch (android.content.ActivityNotFoundException ex){
                Toast.makeText(this,"Invalid Number",Toast.LENGTH_SHORT).show();
            }
        }
    }

    /*----------------------------------------------------end of call purpose-------------------------------------------------------*/
    private void getDriverDetail(){

        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("match_ride_id", matchedRideId));
        apiDataObjectArrayList.add(new ApiDataObject("getMyComingDriver", "1"));
        asyncTaskManager = new AsyncTaskManager(
                this,
                new ApiManager().confirmRide,
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
                        JSONArray jsonArray = jsonObjectLoginResponse.getJSONArray("value").getJSONObject(0).getJSONArray("driver");
                        String driver_name = jsonArray.getJSONObject(0).getString("username");
                        String phone = jsonArray.getJSONObject(0).getString("phone");
                        String profile_pic = jsonArray.getJSONObject(0).getString("profile_pic");
                        String car_plate = jsonArray.getJSONObject(0).getString("car_plate");
                        String car_brand = jsonArray.getJSONObject(0).getString("car_brand");
                        String car_model = jsonArray.getJSONObject(0).getString("car_model");
                        String status = jsonArray.getJSONObject(0).getString("status");

                        setUpView(driver_name, phone, profile_pic, car_plate, car_brand, car_model, status);
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

    private void setUpView(String driver_name, String phone, String profile_pic, String car_plate, String car_brand, String car_model, String status) {
        profile_pic = profile_prefix + profile_pic;
        car_model = car_brand + " "+ car_model;
        driverPhoneNumber = phone;

        switch (status){
            case "3":
                status = getResources().getString(R.string.match_ride_activity_status_coming);
                break;
            case "4":
                status = getResources().getString(R.string.match_ride_activity_status_arrived);
                break;
            case "5":
                status = getResources().getString(R.string.match_ride_activity_status_destination);
                break;
        }
        startRouteActivityCarModel.setText(car_model);
        startRouteActivityStatus.setText(status);
        startRouteActivityCarPlate.setText(car_plate);
        startRouteActivityDriverName.setText(driver_name);
        Picasso.get()
                .load(profile_pic)
                .error(R.drawable.loading_gif)
                .into(startRouteActivityDriverProfilePicture);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.actionbar_close:
                onBackPressed();
                break;
            case R.id.activity_start_route_call_icon:
                phoneCallPermission();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        setResult(requestCode);
        super.onBackPressed();
    }

    @Override
    public void updateConfirmRideListView(int requestCode) {
        this.requestCode = requestCode;
        onBackPressed();
    }

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
            popUpCompletedRouteDialog();
        }
    };

    private void popUpCompletedRouteDialog(){
        dialogFragment = new CompletedRouteDialog();
        dialogFragment.show(fm, "");
    }

    //convert String pick up into LatLong
    private LatLng getPickUpLatLong(String stringPickUpLocation){

        stringPickUpLocation = stringPickUpLocation.substring(10, stringPickUpLocation.length()-1);
        String[] pickUpLatLong =  stringPickUpLocation.split(",");
        double latitude = Double.parseDouble(pickUpLatLong[0]);
        double longitude = Double.parseDouble(pickUpLatLong[1]);
        return new LatLng(latitude, longitude);
    }
}
