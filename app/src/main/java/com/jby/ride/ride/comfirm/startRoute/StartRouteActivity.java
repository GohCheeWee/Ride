package com.jby.ride.ride.comfirm.startRoute;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jby.ride.Object.MarkerObject;
import com.jby.ride.R;
import com.jby.ride.chat.chatRoom.ChatRoomActivity;
import com.jby.ride.database.DbChat;
import com.jby.ride.others.CustomMarker;
import com.jby.ride.others.SquareHeightLinearLayout;
import com.jby.ride.ride.RideActivity;
import com.jby.ride.ride.comfirm.dialog.CompletedRouteDialog;
import com.jby.ride.shareObject.AnimationUtility;
import com.jby.ride.shareObject.ApiDataObject;
import com.jby.ride.shareObject.ApiManager;
import com.jby.ride.shareObject.AsyncTaskManager;
import com.jby.ride.sharePreference.SharedPreferenceManager;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.jby.ride.others.MyFireBaseMessagingService.NotificationBroadCast;

public class StartRouteActivity extends AppCompatActivity implements View.OnClickListener,
        OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener,
        CompletedRouteDialog.CompletedRouteDialogCallBack {
    private SquareHeightLinearLayout actionBarMenuIcon, actionBarCloseIcon, actionBarLogout;
    private TextView actionBarTitle;

    private MapFragment map;
    private TextView startRouteActivityStatus, startRouteActivityCarPlate, startRouteActivityCarModel;
    private TextView startRouteActivityDriverName, startRouteActivivtyMovingText;
    private ImageView startRouteActivityCall, startRouteActivityMessage;
    private CircleImageView startRouteActivityDriverProfilePicture;

    private String matchedRideId, status, driverPhoneNumber;
    private String pickUpLocation, dropOffLocation;
    private String pickUpAddress, dropOffAddress;
    private Location currentLocation;
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
    private ArrayList<MarkerObject> markerObjectArrayList;
    //    fragment purpose
    private DialogFragment dialogFragment;
    private FragmentManager fm;
    //for chat purpose
    private String driverRideID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_route);
        objectInitialize();
        objectSetting();
    }

    private void objectInitialize() {
        actionBarMenuIcon = (SquareHeightLinearLayout) findViewById(R.id.actionbar_menu);
        actionBarCloseIcon = (SquareHeightLinearLayout) findViewById(R.id.actionbar_close);
        actionBarLogout = (SquareHeightLinearLayout) findViewById(R.id.actionbar_logout);
        actionBarTitle = (TextView) findViewById(R.id.actionBar_title);

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.activity_start_route_google_map));

        startRouteActivityStatus = (TextView) findViewById(R.id.activity_start_route_status);
        startRouteActivityCarPlate = (TextView) findViewById(R.id.activity_start_route_car_plate);
        startRouteActivityCarModel = (TextView) findViewById(R.id.activity_start_route_car_model);
        startRouteActivityDriverName = (TextView) findViewById(R.id.acitvity_start_route_driver_name);
        startRouteActivivtyMovingText = findViewById(R.id.activity_start_route_moving_text);

        startRouteActivityCall = (ImageView) findViewById(R.id.activity_start_route_call_icon);
        startRouteActivityMessage = (ImageView) findViewById(R.id.activity_start_route_message_icon);

        startRouteActivityStatus = (TextView) findViewById(R.id.activity_start_route_status);
        startRouteActivityDriverProfilePicture = (CircleImageView) findViewById(R.id.activity_start_route_driver_icon);
        handler = new Handler();
        fm = getSupportFragmentManager();
        markerObjectArrayList = new ArrayList<>();
    }

    private void objectSetting() {
        //        actionBar
        actionBarMenuIcon.setVisibility(View.GONE);
        actionBarCloseIcon.setVisibility(View.VISIBLE);
        actionBarTitle.setText(R.string.start_route_activity);

        actionBarCloseIcon.setOnClickListener(this);
        startRouteActivityCall.setOnClickListener(this);
        startRouteActivityMessage.setOnClickListener(this);
        startRouteActivivtyMovingText.setSelected(true);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            matchedRideId = bundle.getString("match_ride_id");
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getDriverRideId();
            }
        }, 200);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getDriverDetail();
            }
        }, 200);
    }

    private void getDriverRideId(){
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("match_ride_id", matchedRideId));

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
                        driverRideID = jsonObjectLoginResponse.getString("driver_ride_id");
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
                Toast.makeText(this,"Execution Exception!", Toast.LENGTH_SHORT).show();
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
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setSmallestDisplacement(1);
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
        if (mGoogleApiClient != null) {
            currentLocation = location;
            if (status.equals("5")) {
                setUpMarkerWhenGoingDestination();
            }
        }
    }


    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (mGoogleApiClient != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    //convert String pick up into LatLong
    private void setUpMarkerWhenDriveIsComing(String stringLatitude, String stringLongitude){
        double latitude = Double.parseDouble(stringLatitude);
        double longitude = Double.parseDouble(stringLongitude);
        LatLng driverLocation = new LatLng(latitude, longitude);
//            rider pick up point
            if (markerObjectArrayList.size() == 0)
                markerObjectArrayList.add(new MarkerObject(getLatLong(pickUpLocation), "", pickUpAddress));
            else
                markerObjectArrayList.set(0, new MarkerObject(getLatLong(pickUpLocation), "", pickUpAddress));

            //driver location
            if (markerObjectArrayList.size() == 1)
                markerObjectArrayList.add(new MarkerObject(driverLocation, "", startRouteActivityDriverName.getText().toString()));
            else

                markerObjectArrayList.set(1, new MarkerObject(driverLocation, "", startRouteActivityDriverName.getText().toString()));
            setUpMarker();

    }

    private void setUpMarkerWhenGoingDestination(){
        markerObjectArrayList.clear();
        double latitude = currentLocation.getLatitude();
        double longitude = currentLocation.getLongitude();
        LatLng currentLocation = new LatLng(latitude, longitude);
        //setup current point
        if (markerObjectArrayList.size() == 0)
            markerObjectArrayList.add(new MarkerObject(currentLocation, "", "My Location"));
        else
            markerObjectArrayList.set(0, new MarkerObject(currentLocation, "", "My  Location"));

        //setup drop off location
        if (markerObjectArrayList.size() == 1)
            markerObjectArrayList.add(new MarkerObject(getLatLong(dropOffLocation), "", dropOffAddress));
        else
            markerObjectArrayList.set(1, new MarkerObject(getLatLong(dropOffLocation), "", dropOffAddress));
        setUpMarker();
    }

    // update initial pickup point after received it
    private void setUpMarker(){
        googleMap.clear();
//        setting up initial marker
        MarkerOptions routeMarker = new MarkerOptions();
        //setting up marker information
//        getMarkerInformationByStatus();

        //set up all the marker
        for(int i = 0; i<markerObjectArrayList.size(); i++){
            routeMarker
                    .position(markerObjectArrayList.get(i).getLocation())
                    .title(markerObjectArrayList.get(i).getLocationAddress());
            //custom marker icon
            if(!status.equals("5")){
                if(i == 0) routeMarker.icon(BitmapDescriptorFactory.fromBitmap(new CustomMarker(this).getMarkerBitmapFromView(R.drawable.activity_main_startpoint)));
                else {
                    routeMarker.icon(BitmapDescriptorFactory.fromBitmap(new CustomMarker(this).getMarkerBitmapFromView(R.drawable.activity_start_car_icon)));
                }
            }
            else{
                if(i == 0) routeMarker.icon(BitmapDescriptorFactory.fromBitmap(new CustomMarker(this).getMarkerBitmapFromView(R.drawable.activity_start_car_icon)));
                else {
                    routeMarker.icon(BitmapDescriptorFactory.fromBitmap(new CustomMarker(this).getMarkerBitmapFromView(R.drawable.activity_main_endpoint)));
                }
            }

            googleMap.addMarker(routeMarker);
            requestCameraFocus();
        }
    }

    //convert String pick up into LatLong
    private LatLng getLatLong(String stringLocation){

        stringLocation = stringLocation.substring(10, stringLocation.length()-1);
        String[] location =  stringLocation.split(",");
        double latitude = Double.parseDouble(location[0]);
        double longitude = Double.parseDouble(location[1]);
        return new LatLng(latitude, longitude);
    }

    private void requestCameraFocus(){
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for(int i = 0; i<markerObjectArrayList.size(); i++)
            builder.include(markerObjectArrayList.get(i).getLocation());
//
        LatLngBounds bounds = builder.build();
//
        int padding = 100; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        googleMap.animateCamera(cu);

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
                        status = jsonArray.getJSONObject(0).getString("status");
                        pickUpLocation = jsonArray.getJSONObject(0).getString("pick_up_location");
                        dropOffLocation = jsonArray.getJSONObject(0).getString("drop_off_location");
                        pickUpAddress = jsonArray.getJSONObject(0).getString("pick_up_address");
                        dropOffAddress = jsonArray.getJSONObject(0).getString("drop_off_address");

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
        initializeMap();
    }

    private void setUpView(String driver_name, String phone, String profile_pic, String car_plate, String car_brand, String car_model, String status) {
        profile_pic = profile_prefix + profile_pic;
        car_model = car_brand + " "+ car_model;
        driverPhoneNumber = phone;


        startRouteActivityCarModel.setText(car_model);
        startRouteActivityCarPlate.setText(car_plate);
        startRouteActivityDriverName.setText(driver_name);
        Picasso.get()
                .load(profile_pic)
                .error(R.drawable.loading_gif)
                .into(startRouteActivityDriverProfilePicture);
        setUpStatus(status);
    }

    private void setUpStatus(String status){
        this.status = status;
        switch (status){
            case "3":
                status = getResources().getString(R.string.match_ride_activity_status_coming);
                setUpMovingText("otw");
                new AnimationUtility().fadeInVisible(this, startRouteActivivtyMovingText);
                break;
            case "4":
                status = getResources().getString(R.string.match_ride_activity_status_arrived);
                setUpMovingText("arrived");
                new AnimationUtility().fadeInVisible(this, startRouteActivivtyMovingText);
                break;
            case "5":
                status = getResources().getString(R.string.match_ride_activity_status_destination);
                new AnimationUtility().fadeOutGone(this, startRouteActivivtyMovingText);
                break;
        }
        startRouteActivityStatus.setText(status);
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
            case R.id.activity_start_route_message_icon:
                openChatRoom();
                break;
        }
    }

    private void openChatRoom(){
        if (new DbChat(getApplicationContext()).updateChatStatusWhenClick(driverRideID)) {
            Intent intent = new Intent(this, ChatRoomActivity.class);
            Bundle bundle = new Bundle();

            bundle.putString("driver_ride_id", driverRideID);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void popOutRatingDialogRequest(int requestCode) {
        Intent intent = new Intent(this, RideActivity.class);
        intent.putExtra("complete", "complete");
        intent.putExtra("match_ride_id", matchedRideId);
        startActivity(intent);
        finish();
    }
/*-----------------------------------------------------------------------------------broadcasr purpose--------------------------------------------------------*/
    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(NotificationBroadCast));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String id = intent.getStringExtra("id");
            switch(id){
                case "3":
                    popUpCompletedRouteDialog();
                    break;
                case "6":
//                    arrived
                    setUpStatus("4");
                    break;
                case "7":
                    setUpStatus("5");
                    setUpMarkerWhenGoingDestination();
                    break;
                case "8":
                    String longitude =  intent.getStringExtra("longitude");
                    String latitude =  intent.getStringExtra("latitude");
                    if(!status.equals("5") && status != null)
                        if(mGoogleApiClient != null) setUpMarkerWhenDriveIsComing(latitude, longitude);
                    break;
            }

        }
    };

    private void popUpCompletedRouteDialog(){
        dialogFragment = new CompletedRouteDialog();
        dialogFragment.show(fm, "");
    }

    private void setUpMovingText(String status){
        if(status.equals("arrived")){
            startRouteActivivtyMovingText.setText(R.string.activity_start_route_arrive_moving_text);
            startRouteActivivtyMovingText.setBackgroundColor(getResources().getColor(R.color.red));
        }
        else{
            startRouteActivivtyMovingText.setText(R.string.activity_start_route_otw_moving_text);
            startRouteActivivtyMovingText.setBackgroundColor(getResources().getColor(R.color.green));
        }
        startRouteActivivtyMovingText.setSelected(true);

    }
/*-------------------------------------------------------------------------------------end of broadcast---------------------------------------------------------*/
}
