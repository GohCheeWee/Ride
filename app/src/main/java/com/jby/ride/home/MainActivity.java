package com.jby.ride.home;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.jby.ride.Object.AddressObject;
import com.jby.ride.Object.MarkerObject;
import com.jby.ride.Object.PolylineObject;
import com.jby.ride.R;
import com.jby.ride.home.dialog.InvalidTimeDialog;
import com.jby.ride.home.dialog.NoteDialog;
import com.jby.ride.others.CustomMarker;
import com.jby.ride.others.dialog.DriverFoundDialog;
import com.jby.ride.others.dialog.DriverIsOtwDialog;
import com.jby.ride.others.FetchAddressIntentService;
import com.jby.ride.others.LocationConstants;
import com.jby.ride.others.dialog.DriverPickUpDialog;
import com.jby.ride.others.dialog.LocationRequestDialog;
import com.jby.ride.others.dialog.RatingDialog;
import com.jby.ride.others.SquareHeightLinearLayout;
import com.jby.ride.profile.ProfileActivity;
import com.jby.ride.registration.LoginActivity;
import com.jby.ride.ride.RideActivity;
import com.jby.ride.ride.comfirm.startRoute.StartRouteActivity;
import com.jby.ride.shareObject.AnimationUtility;
import com.jby.ride.shareObject.ApiDataObject;
import com.jby.ride.shareObject.ApiManager;
import com.jby.ride.shareObject.AsyncTaskManager;
import com.jby.ride.sharePreference.SharedPreferenceManager;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.jby.ride.others.MyFireBaseMessagingService.NotificationBroadCast;

public class MainActivity extends AppCompatActivity implements NavigationView.
        OnNavigationItemSelectedListener, View.OnClickListener,
        OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener,
        NoteDialog.NoteDialogCallBack, InvalidTimeDialog.InvalidTimeDialogCallBack,
        DriverFoundDialog.DriverFoundDialogCallBack, PolylineObject.PolyLineCallBack,
        RatingDialog.RatingDialogCallBack, DriverIsOtwDialog.DriverIsOtwDialogCallBack,
        DriverPickUpDialog.DriverPickUpDialogCallBack, LocationRequestDialog.LocationRequestDialogCallBack {

    private DrawerLayout mainActivityDrawerLayout;
    private ActionBarDrawerToggle mainActivityActionBarDrawerToggle;
    private NavigationView mainActivityNavigationView;
    private SquareHeightLinearLayout actionBarMenu;
    private TextView mainActivityUsername, mainActivityEmail;
    private CircleImageView mainActivityProfilePicture;
    private LinearLayout mainActivityNavHeaderLayout;
    private RelativeLayout mainActivityMainLayout;
    //    map setting
    private MapFragment map;
    private GoogleMap googleMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private MarkerOptions routeMarker = new MarkerOptions();
    ArrayList<MarkerObject> routeArray = new ArrayList<>();
    //    address setting
    protected Location mLastLocation;
    private AddressResultReceiver mResultReceiver;
    FusedLocationProviderClient fusedLocationProviderClient;
    private static int PICK_UP_POINT_REQUEST = 1;
    private static int DROP_OFF_POINT_REQUEST = 2;
    private int requestCode;
    private boolean placePicker = false;
    private LatLng currentLocation;
    private boolean dropOffPoint = false;
    //    booking setting
    private TextView mainActivityPickUpPoint, mainActivityDropOffPoint;
    private ImageView mainActivityDropOffIcon;
    private Button mainActivityBook;
    //    advance booing setting
    private LinearLayout mainActivityDate, mainActivityTime, mainActivityPayment, mainActivityNote;
    private LinearLayout mainActivityAdvanceSettingLayout;
    private RelativeLayout mainActivityPriceLayout;
    private TextView mainActivityTextViewDate, mainActivityTextViewTime, mainActivityTextViewPayment;
    private TextView mainActivityTextViewRemark, mainActivityTextViewPrice;
    private TextView mainActivityEstimateDuration, mainActivityEstimateDistance;
    //    floating button purpose
    private RelativeLayout mainActivityFloatingButtonLayout;
    private ImageView mainActivityFloatingButton;
    private TextView mainActivityPendingRideCounter;
    //    fare calculation setting
    public static double minFare = 4.00;
    public static double farePerDistance = 0.25;
    public static double minDistance = 5.0;
    private double finalFare = 4;
    //    advance booking time & date setting
    Calendar mCurrentTime;
    TimePickerDialog timePicker;
    DatePickerDialog datePicker;
    Calendar calendar;
    private String date = "-";
    private String time = "-";
    private String note = "-";
    private int hour, minute;
    private boolean selected = false;
    //    asyncTask
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;
    //    dialog
    DialogFragment dialogFragment;
    Bundle bundle;
    FragmentManager fm;
    //    logout request
    public static int LOGOUT_REQUEST = 300;
    public static int UPDATE_PROFILE_REQUEST = 301;
    public static int UPDATE_COUNTER_REQUEST = 304;
    public static String TAG = "MainActivity";
    //location permission purpose
    public static int LOCATION_REQUEST = 500;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        objectInitialize();
        objectSetting();
    }

    private void objectInitialize() {
        mainActivityDrawerLayout = (DrawerLayout) findViewById(R.id.activity_main);
        mainActivityActionBarDrawerToggle = new ActionBarDrawerToggle(this, mainActivityDrawerLayout, R.string.activity_main_open, R.string.activity_main_close);
        mainActivityNavigationView = (NavigationView) findViewById(R.id.nv);
        actionBarMenu = (SquareHeightLinearLayout) findViewById(R.id.actionbar_menu);
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.activity_main_google_map));

        View headerView = mainActivityNavigationView.getHeaderView(0);
        mainActivityUsername = (TextView) headerView.findViewById(R.id.activity_main_username);
        mainActivityEmail = (TextView) headerView.findViewById(R.id.activity_main_email);
        mainActivityNavHeaderLayout = (LinearLayout) headerView.findViewById(R.id.activity_main_nav_header);
        mainActivityProfilePicture = (CircleImageView) headerView.findViewById(R.id.activity_main_profile_picture);

        mainActivityMainLayout = (RelativeLayout) findViewById(R.id.activity_main_layout);
//        booking setting
        mainActivityPickUpPoint = (TextView) findViewById(R.id.activity_main_pick_up);
        mainActivityDropOffPoint = (TextView) findViewById(R.id.activity_main_drop_off);
        mainActivityBook = (Button) findViewById(R.id.activity_main_book);
        mainActivityDropOffIcon = (ImageView) findViewById(R.id.activity_main_drop_off_icon);
//        advance booking setting
        mainActivityTime = (LinearLayout) findViewById(R.id.activity_main_time);
        mainActivityDate = (LinearLayout) findViewById(R.id.activity_main_date);
        mainActivityPayment = (LinearLayout) findViewById(R.id.activity_main_payment);
        mainActivityNote = (LinearLayout) findViewById(R.id.activity_main_remark);
        mainActivityAdvanceSettingLayout = (LinearLayout) findViewById(R.id.activity_main_advance_setting);
        mainActivityPriceLayout = (RelativeLayout) findViewById(R.id.activity_main_price_layout);
//        textview
        mainActivityTextViewDate = (TextView) findViewById(R.id.activity_main_date_text_view);
        mainActivityTextViewTime = (TextView) findViewById(R.id.activity_main_time_text_view);
        mainActivityTextViewPayment = (TextView) findViewById(R.id.activity_main_payment_text_view);
        mainActivityTextViewRemark = (TextView) findViewById(R.id.activity_main_remark_text_view);
        mainActivityTextViewPrice = (TextView) findViewById(R.id.activity_main_price);
        mainActivityEstimateDistance = (TextView) findViewById(R.id.activity_main_estimate_distance);
        mainActivityEstimateDuration = (TextView) findViewById(R.id.activity_main_estimate_duration);
//        floating layout
        mainActivityFloatingButtonLayout = (RelativeLayout) findViewById(R.id.activity_main_floating_button_layout);
        mainActivityFloatingButton = (ImageView) findViewById(R.id.activity_main_floating_button);
        mainActivityPendingRideCounter = (TextView) findViewById(R.id.activity_main_counter);

//        address setting
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mResultReceiver = new AddressResultReceiver(new Handler());
//        dialog
        fm = getSupportFragmentManager();
    }

    private void objectSetting() {
        mainActivityDrawerLayout.addDrawerListener(mainActivityActionBarDrawerToggle);
        mainActivityActionBarDrawerToggle.syncState();
        mainActivityNavigationView.setNavigationItemSelectedListener(this);
//        actionbar
        actionBarMenu.setOnClickListener(this);
//        booking setting
        mainActivityPickUpPoint.setOnClickListener(this);
        mainActivityDropOffPoint.setOnClickListener(this);
//      advance setting
        mainActivityTime.setOnClickListener(this);
        mainActivityDate.setOnClickListener(this);
        mainActivityPayment.setOnClickListener(this);
        mainActivityNote.setOnClickListener(this);
        mainActivityBook.setOnClickListener(this);
        mainActivityFloatingButton.setOnClickListener(this);
//    drawer
        mainActivityNavHeaderLayout.setOnClickListener(this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);

        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (checkLocationPermission()) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)  == PackageManager.PERMISSION_GRANTED) {
                        //Request location updates:
                        initializeSetting();
                    }
                }
            }
        },200);
    }
//if permission is granted then only run here
    private void initializeSetting() {

        checkLocationPermission();
        initializeMap();
        getUserInformation();
        getNumPendingRide();
        checkingDriverFoundStatus();
        checkingBundleValue();
        checkUserProfileGenderAndPhone();
    }

    private void checkingBundleValue() {
        bundle = getIntent().getExtras();
        if(bundle != null){
            if(bundle.getString("driver_ride_id") != null)
            {
                String driverRideId = bundle.getString("driver_ride_id");
                popOutDriverIsOnTheWayDialog(driverRideId);
            }
        }
    }


    //<-----------------------------------Navigation Drawer-------------------------------------------------------->
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mainActivityActionBarDrawerToggle.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        //calling the method displayselectedscreen and passing the id of selected menu
        displaySelectedScreen(item.getItemId());
        //make this method blank
        return true;
    }

    private void displaySelectedScreen(int itemId) {
        //creating fragment object
        Fragment fragment = null;

        //initializing the fragment object which is selected
        switch (itemId) {
            case R.id.ridePAy:
                break;
            case R.id.trip:
                Intent intent = new Intent(this, RideActivity.class);
                startActivity(intent);
                break;
            case R.id.emergency:
                break;
            case R.id.helpCenter:
                break;
            case R.id.driveWithRide:
                break;
        }
        mainActivityDrawerLayout.closeDrawer(GravityCompat.START);
    }

    private void getUserInformation() {
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
                    if (jsonObjectLoginResponse.getString("status").equals("1")) {
                        String username = jsonObjectLoginResponse.getJSONObject("value").getString("username");
                        String email = jsonObjectLoginResponse.getJSONObject("value").getString("email");
                        String profile_picture = jsonObjectLoginResponse.getJSONObject("value").getString("profile_picture");

                        mainActivityEmail.setText(email);
                        mainActivityUsername.setText(username);

                        if (profile_picture != null) {
                            if(!profile_picture.substring(0, 8).equals("https://"))
                                profile_picture = ProfileActivity.prefix + profile_picture;

                            Picasso.get()
                                    .load(profile_picture)
                                    .error(R.drawable.loading_gif)
                                    .into(mainActivityProfilePicture);
                        }

                        showSnackBar("Hi," + username);

                    } else if (jsonObjectLoginResponse.getString("status").equals("2")) {
                        Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                    }
                } else {
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
    public void showSnackBar(String message) {
        final Snackbar snackbar = Snackbar.make(mainActivityMainLayout, message, Snackbar.LENGTH_SHORT);
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
    //<-----------------------------------End of Navigation Drawer-------------------------------------------------------->

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.actionbar_menu:
                mainActivityDrawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.activity_main_pick_up:
                if (!placePicker) {
                    clickEffect(mainActivityPickUpPoint);
                    selectPoint(1);
                }
                break;
            case R.id.activity_main_drop_off:
                if (!placePicker) {
                    clickEffect(mainActivityDropOffPoint);
                    selectPoint(2);
                }
                break;
            case R.id.activity_main_payment:
                clickEffect(mainActivityPayment);
                break;
            case R.id.activity_main_time:
                clickEffect(mainActivityTime);
                selectTimeDialog();
                break;
            case R.id.activity_main_date:
                clickEffect(mainActivityDate);
                selectDateDialog();
                break;
            case R.id.activity_main_remark:
                clickEffect(mainActivityNote);
                openNote();
                break;
            case R.id.activity_main_book:
                if (!dropOffPoint)
                    selectPoint(2);
                else
                    createUserRide();
                break;
            case R.id.activity_main_nav_header:
                clickEffect(mainActivityProfilePicture);
                openProfileActivity(false);
                break;
            case R.id.activity_main_floating_button:
                Intent intent = new Intent(this, RideActivity.class);
                startActivityForResult(intent, UPDATE_COUNTER_REQUEST);
                break;
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
        Log.d(TAG, "Location: " + location);

//        assign value into current location then only add the marker
        currentLocation = latLng;
        fetchAddress();

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }
    //<-----------------------------------End of Map and  GPS setting-------------------------------------------------------->

    //<-----------------------------------pickup and drop off point setting-------------------------------------------------------->
//for getting the initial address from result receiver
    private void fetchAddress() {
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
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        mLastLocation = location;

                        // In some rare cases the location returned can be null
                        if (mLastLocation == null) {
                            return;
                        }

                        if (!Geocoder.isPresent()) {
                            Toast.makeText(MainActivity.this,
                                    "Geo not found",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        // Start service and update UI to reflect new location
                        startIntentService();
                    }
                });
    }
//start intent to get initilza
    protected void startIntentService() {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(LocationConstants.RECEIVER, mResultReceiver);
        intent.putExtra(LocationConstants.LOCATION_DATA_EXTRA, mLastLocation);
        startService(intent);
    }

    //after received then call fetchAddress
    class AddressResultReceiver extends ResultReceiver {
        private AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            if (resultData == null) {
                return;
            }

            // Display the address string
            // or an error message sent from the intent service.

            ArrayList<AddressObject> addressList;
            addressList = resultData.getParcelableArrayList(LocationConstants.RESULT_DATA_KEY);
            if (addressList == null) {
                addressList = null;
            }
            // Show a toast message if an address was found.
            if (resultCode == LocationConstants.SUCCESS_RESULT) {
                Log.i(TAG, "MainActivity" + getString(R.string.activity_main_address_found));
                if(addressList!= null)
                    setInitialPickUpPoint(addressList);
            }

        }
    }
// update initial pickup point after received it
    private void setInitialPickUpPoint(ArrayList<AddressObject> result){
        String streetName = result.get(0).getStreetName();
        String address = result.get(0).getFullAddress();
        mainActivityPickUpPoint.setText(streetName);

//        setting up initial marker
        routeArray.add(0, new MarkerObject(currentLocation, streetName, address));
        routeMarker.position(routeArray.get(0).getLocation());
        routeMarker.title(routeArray.get(0).getLocationName());
        routeMarker.icon(BitmapDescriptorFactory.fromBitmap(new CustomMarker(this).getMarkerBitmapFromView(R.drawable.activity_main_startpoint)));
        googleMap.addMarker(routeMarker);
    }
//    place picker && reset final fare = 4.00
    private void selectPoint(int point){
        placePicker = true;
        finalFare = 4.00;
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

        if(point == 1)
            requestCode = PICK_UP_POINT_REQUEST;
        else
            requestCode = DROP_OFF_POINT_REQUEST;
        try {
            startActivityForResult(builder.build(this), requestCode);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }
// result from place picker
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_UP_POINT_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                updateLocation(place);
            }
        }
        else if(requestCode == DROP_OFF_POINT_REQUEST){
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                updateLocation(place);
                dropOffPoint = true;
            }
        }
        else if(resultCode == LOGOUT_REQUEST){
            logOut();
        }
        else if(resultCode == UPDATE_PROFILE_REQUEST){
            getUserInformation();
        }
        else if(resultCode == UPDATE_COUNTER_REQUEST){
            getNumPendingRide();
        }

        placePicker = false;
    }
//    update location after getting result from place picker
    private void updateLocation(Place place){
        String point = String.format("%s", place.getName());
        String address = String.format("%s", place.getAddress());
        LatLng latLng = place.getLatLng();

        MarkerOptions options;
        googleMap.clear();

        if(requestCode == 1)
        {
            mainActivityPickUpPoint.setText(point);
//            setup pick up point into route array
            routeArray.set(0, new MarkerObject(latLng, point, address));
        }
        else{
            mainActivityDropOffPoint.setText(point);
//            setup drop off marker
            if(routeArray.size() >= 2)
                routeArray.set(1, new MarkerObject(latLng, point, address));
            else
                routeArray.add(1, new MarkerObject(latLng, point, address));

            checkingDropOffPoint();
        }

        for(int i = 0; i<= routeArray.size()-1; i++){
            options = new MarkerOptions();
            options.position(routeArray.get(i).getLocation());
            options.title(routeArray.get(i).getLocationName());

//            for marker setting
            if(i == 0)
//                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                options.icon(BitmapDescriptorFactory.fromBitmap(new CustomMarker(this).getMarkerBitmapFromView(R.drawable.activity_main_startpoint)));
            else
                options.icon(BitmapDescriptorFactory.fromBitmap(new CustomMarker(this).getMarkerBitmapFromView(R.drawable.activity_main_endpoint)));

            //            focus on selected point
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(routeArray.get(i).getLocation()));
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(16));
            googleMap.addMarker(options);
        }

        if(routeArray.size() == 2){
/*            final String url = getRequestDirection(routeArray.get(0).getLocation(), routeArray.get(1).getLocation());
            requestDirection(url);*/
            new PolylineObject(this,
                    routeArray.get(0).getLocation(),
                    routeArray.get(1).getLocation(),
                    this).requestDirection();
        }
    }

    @Override
    public void setPolylineOptions(PolylineOptions polylineOptions) {
        if(polylineOptions != null){
            googleMap.addPolyline(polylineOptions);
        }
    }

    @Override
    public void setDistanceAndDuration(String distance, String duration) {
        setDistanceDuration(duration, distance);
        calculateFare(distance);
    }

    @Override
    public void dismiss() {
        requestCameraFocus();
    }

    //    setting after gettting drop off point
    private void checkingDropOffPoint(){
        if (!mainActivityDropOffPoint.getText().equals("")){
//            time setting
            mCurrentTime = Calendar.getInstance();
//            time
            hour = setTimeInto12HourFormat(mCurrentTime.get(Calendar.HOUR_OF_DAY));
            minute = mCurrentTime.get(Calendar.MINUTE);
            if(minute + 10 >= 60)
            {
                minute = minute + 10 - 60;
                hour++;
            }
//            date
            int dayOfMonth = mCurrentTime.get(Calendar.DAY_OF_MONTH);
            SimpleDateFormat monthFormat = new SimpleDateFormat("MM", Locale.getDefault());
            String month = monthFormat.format(mCurrentTime.getTime());
//            format
            String time_format = "%02d:%02d";
            String hourStatus = setAmOrPm(hour);

            time = String.format(Locale.getDefault(),time_format, hour, minute) +" "+hourStatus;
            date = dayOfMonth + "/" + month;

            mainActivityDropOffIcon.setImageDrawable(getDrawable(R.drawable.activity_main_endpoint));
            mainActivityBook.setText(R.string.activity_main_book);
            mainActivityTextViewTime.setText(time);
            mainActivityTextViewDate.setText(date);

            new AnimationUtility().fadeInVisible(this, mainActivityAdvanceSettingLayout);
            new AnimationUtility().fadeInVisible(this, mainActivityPriceLayout);
        }
    }

    private void requestCameraFocus(){
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for(int i = 0; i<routeArray.size(); i++)
            builder.include(routeArray.get(i).getLocation());
//
        LatLngBounds bounds = builder.build();
//
        int padding = 100; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        googleMap.animateCamera(cu);

    }

    private void setDistanceDuration(String duration, String distance){
        mainActivityEstimateDuration.setText(duration);
        mainActivityEstimateDistance.setText(distance);
    }
//    calculatation fare
    private void calculateFare(String distance){
       String unit = distance.substring(distance.length() - 2);
       DecimalFormat form = new DecimalFormat("0");

       if(unit.equals("km")){
           String plainDistance = distance.substring(0, distance.length() - 2);
           plainDistance = plainDistance.replace(",", "");
           double finalDistance = Double.parseDouble(plainDistance);

           if(finalDistance > minDistance){
               double extraDistance = finalDistance - minDistance;
               finalFare = (extraDistance * farePerDistance) + minFare;
               setFare(form.format(finalFare));
           }
           else{
               setFare(form.format(finalFare));
           }
       }
       else{
           setFare(form.format(finalFare));
       }
    }
//    set fare
    private void setFare(String fare){
        fare = "RM " + fare;
        mainActivityTextViewPrice.setText(fare);
        mainActivityPriceLayout.setVisibility(View.VISIBLE);
    }

    public void setNote(String note){
        this.note = note;
        mainActivityTextViewRemark.setText(note);
    }

    public void openNote(){
        dialogFragment = new NoteDialog();
        bundle = new Bundle();
        String note = mainActivityTextViewRemark.getText().toString();

        if(!note.equals("Write your note here..."))
            bundle.putString("note", note);
        dialogFragment.setArguments(bundle);
        dialogFragment.show(fm, "");
    }
    //<-----------------------------------End of pickup and drop off point setting-------------------------------------------------------->

    //<-----------------------------------Time setting-------------------------------------------------------->
    private void selectDateDialog(){
        mCurrentTime = Calendar.getInstance();
        int dayOfMonth = mCurrentTime.get(Calendar.DAY_OF_MONTH);
        int month = mCurrentTime.get(Calendar.MONTH);
        int year = mCurrentTime.get(Calendar.YEAR);

        datePicker = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        calendar = Calendar.getInstance();
                        calendar.set(year,monthOfYear, dayOfMonth);

                        SimpleDateFormat monthFormat = new SimpleDateFormat("MM", Locale.getDefault());
                        SimpleDateFormat dayFormat = new SimpleDateFormat("dd", Locale.getDefault());

                        String strDate = monthFormat.format(calendar.getTime());
                        String day = dayFormat.format(calendar.getTime());
                        date = day + "/" + strDate;

                        mainActivityTextViewDate.setText(date);

                    }
                }, year, month, dayOfMonth);
        datePicker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
//        open time when closing
        datePicker.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                selectTimeDialog();
            }
        });
        datePicker.show();
    }

    public void selectTimeDialog(){
        mCurrentTime = Calendar.getInstance();
        hour = mCurrentTime.get(Calendar.HOUR_OF_DAY);
        minute = mCurrentTime.get(Calendar.MINUTE) + 10;

        timePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override

            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                calendar.set(Calendar.MINUTE, selectedMinute);

                String format = "%02d:%02d";
                String hourStatus = setAmOrPm(selectedHour);
                int editedHour = setTimeInto12HourFormat(selectedHour);
                time = String.format(Locale.getDefault(),format, editedHour, selectedMinute) +" "+hourStatus;
                mainActivityTextViewTime.setText(time);

                hour = selectedHour;
                minute = selectedMinute;
                selected = true;

            }
        }, hour, minute, false);
        timePicker.setTitle("Select Time");

        timePicker.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {

                SimpleDateFormat sdf = new SimpleDateFormat("HHmm", Locale.getDefault());
                int currentTime = Integer.valueOf(sdf.format(new Date()));
                int selectTime = Integer.valueOf(String.valueOf(hour) + String.valueOf( minute));

                if(selectTime - currentTime < 10 && selected){
                    dialogFragment = new InvalidTimeDialog();
                    dialogFragment.show(fm, "");
                }
            }
        });

        timePicker.show();

    }

    public String setAmOrPm(int time){
        String AM_PM = null ;
        if(time < 12) {
            AM_PM = "AM";
        } else {
            AM_PM = "PM";
        }
        return AM_PM;
    }

    public int setTimeInto12HourFormat(int time){
        if(time > 12)
            time = time - 12;
        else
            time = (time);
        return time;
    }
    //<-----------------------------------end of time setting-------------------------------------------------------->

    //<-----------------------------------booking setting-------------------------------------------------------->
    private void createUserRide(){
        DecimalFormat form = new DecimalFormat("0");
        String pickUpLocation = String.valueOf(routeArray.get(0).getLocation());
        String dropOffLocation = String.valueOf(routeArray.get(1).getLocation());
        String pickUpAddress = String.valueOf(routeArray.get(0).getLocationAddress());
        String dropOffAddress =  String.valueOf(routeArray.get(1).getLocationAddress());
        String date = mainActivityTextViewDate.getText().toString();
        String time = mainActivityTextViewTime.getText().toString();

        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("user_id", SharedPreferenceManager.getUserID(this)));
        apiDataObjectArrayList.add(new ApiDataObject("pick_up_location", pickUpLocation));
        apiDataObjectArrayList.add(new ApiDataObject("drop_off_location",dropOffLocation));
        apiDataObjectArrayList.add(new ApiDataObject("pick_up_address", pickUpAddress));
        apiDataObjectArrayList.add(new ApiDataObject("drop_off_address",dropOffAddress));
        apiDataObjectArrayList.add(new ApiDataObject("payment_method", "1"));
        apiDataObjectArrayList.add(new ApiDataObject("dateRide", date));
        apiDataObjectArrayList.add(new ApiDataObject("time", time));
        apiDataObjectArrayList.add(new ApiDataObject("note", note));
        apiDataObjectArrayList.add(new ApiDataObject("fare", form.format(finalFare)));

        asyncTaskManager = new AsyncTaskManager(
                this,
                new ApiManager().userCreateRide,
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
                        showSnackBar("Ride create successful");
                        resetAllToDefault();
                        getNumPendingRide();
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
    private void resetAllToDefault(){
        mainActivityDropOffIcon.setImageDrawable(getDrawable(R.drawable.activity_main_disable_endpoint));
        new AnimationUtility().fadeOutGone(this, mainActivityAdvanceSettingLayout);
        new AnimationUtility().fadeOutGone(this, mainActivityPriceLayout);
        mainActivityBook.setText(R.string.activty_main_book_button);

        mainActivityTextViewTime.setHint(getResources().getString(R.string.activity_main_time));
        mainActivityTextViewDate.setHint(getResources().getString(R.string.activity_main_time));
        mainActivityTextViewRemark.setHint(getResources().getString(R.string.activity_main_hint_note));

        routeArray.clear();
        googleMap.clear();
        time = "-";
        date = "-";
        note = "-";
        finalFare = 4.00;
        dropOffPoint = false;
        mainActivityDropOffPoint.setText("");

        fetchAddress();
    }
    //<-----------------------------------booking setting-------------------------------------------------------->

    //<-----------------------------------profile setting-------------------------------------------------------->
    private void openProfileActivity(boolean askForUpdate){
        Intent intent = new Intent(this, ProfileActivity.class);
        if(askForUpdate)
            intent.putExtra("requestUpdate", true);
        startActivityForResult(intent, LOGOUT_REQUEST);
    }
    //<-----------------------------------end of profile setting-------------------------------------------------------->

    //<-----------------------------------floating button setting-------------------------------------------------------->
    private void getNumPendingRide(){
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("user_id", SharedPreferenceManager.getUserID(this)));

        asyncTaskManager = new AsyncTaskManager(
                this,
                new ApiManager().numPendingRide,
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
                        String numPendingRide = jsonObjectLoginResponse.getString("num_pending_ride");
                        setMainActivityFloatingButtonLayoutVisibility(true);
                        setUpPendingRideCounter(numPendingRide);
                    }
                    else if(jsonObjectLoginResponse.getString("status").equals("2")){
                        setMainActivityFloatingButtonLayoutVisibility(false);
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

    private void setMainActivityFloatingButtonLayoutVisibility(boolean visible){
        if(visible)
            mainActivityFloatingButtonLayout.setVisibility(View.VISIBLE);
        else
            mainActivityFloatingButtonLayout.setVisibility(View.GONE);
    }

    private void setUpPendingRideCounter(String numPendingRide){
        mainActivityPendingRideCounter.setText(numPendingRide);
    }

    private void logOut(){
        SharedPreferenceManager.setUserID(this, "default");
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
    //click effect
    public void clickEffect(View view){
        Animation animation1 = new AlphaAnimation(0.3f, 1.0f);
        animation1.setDuration(500);
        view.startAnimation(animation1);
    }

    private void checkingDriverFoundStatus(){
        String matchRideId = SharedPreferenceManager.getMatchRideID(this);
        if(!matchRideId.equals("default"))
            popOutDriverFoundDialog(matchRideId);
    }
    /*--------------------------------------------------------------------------permission purpose----------------------------------------------*/
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_REQUEST && grantResults[0] == PackageManager.PERMISSION_GRANTED) initializeSetting();

        else checkLocationPermission();
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission. ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission. ACCESS_FINE_LOCATION)) {
                //open dialog
                openLocationRequestDialog();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission. ACCESS_FINE_LOCATION}, LOCATION_REQUEST);
            }
            return false;
        } else {
            return true;
        }
    }

    private void openLocationRequestDialog(){
        dialogFragment = new LocationRequestDialog();
        dialogFragment.show(fm, "");
    }

    /*-----------------------------------------------------------checking user profile purpose---------------------------------------------------------*/
    private void checkUserProfileGenderAndPhone(){

        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("user_id", SharedPreferenceManager.getUserID(this)));
        apiDataObjectArrayList.add(new ApiDataObject("check_user_profile_info", "1"));
        asyncTaskManager = new AsyncTaskManager(
                this,
                new ApiManager().userCreateRide,
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
                    if(jsonObjectLoginResponse.getString("status").equals("3")){
                        openProfileActivity(true);
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

    /*-----------------------------------------------------------------------broadcast purpose------------------------------------------------------*/
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
                    //rating
                case "3":
                    matchRideId = intent.getStringExtra("match_ride_id");
                    popOutRatingDialog(matchRideId);
                    break;
                    //driver found
                case "4":
                    matchRideId = intent.getStringExtra("match_ride_id");
                    popOutDriverFoundDialog(matchRideId);
                    break;
                    //otw
                case "5":
                    String driverRideID = intent.getStringExtra("driver_ride_id");
                    popOutDriverIsOnTheWayDialog(driverRideID);
                    break;
                    //pick up
                case "7":
                    matchRideId = intent.getStringExtra("match_ride_id");
                    popOutDriverPickUpDialog(matchRideId);
                    break;
            }
        }
    };

    @Override
    public void acceptDriver() {
        Intent intent = new Intent(this, RideActivity.class);
        intent.putExtra("accept", "Accept");
        startActivity(intent);
    }


    @Override
    public void redirectToStartRideActivity(String matchRideId) {
        bundle = new Bundle();
        bundle.putString("match_ride_id", matchRideId);
        startActivity(new Intent(this, StartRouteActivity.class).putExtras(bundle));
    }

    private void popOutDriverFoundDialog(String matchRideId){
        bundle = new Bundle();
        bundle.putString("match_ride_id", matchRideId);

        dialogFragment = new DriverFoundDialog();
        dialogFragment.setArguments(bundle);
        dialogFragment.show(fm, "");
    }

    private void popOutRatingDialog(String matchRideId){
        bundle = new Bundle();
        bundle.putString("match_ride_id", matchRideId);

        dialogFragment = new RatingDialog();
        dialogFragment.setArguments(bundle);
        dialogFragment.show(fm, "");
    }

    private void popOutDriverPickUpDialog(String matchRideId){
        bundle = new Bundle();
        bundle.putString("match_ride_id", matchRideId);

        dialogFragment = new DriverPickUpDialog();
        dialogFragment.setArguments(bundle);
        dialogFragment.show(fm, "");
    }

    private void popOutDriverIsOnTheWayDialog(String driverRideID){
        bundle = new Bundle();
        bundle.putString("driver_ride_id", driverRideID);

        dialogFragment = new DriverIsOtwDialog();
        dialogFragment.setArguments(bundle);
        dialogFragment.show(fm, "");
    }
}
