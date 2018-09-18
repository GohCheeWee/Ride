package com.jby.ride.ride.pending.pendingDetail;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
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
import com.jby.ride.Object.MarkerObject;
import com.jby.ride.R;
import com.jby.ride.others.CustomMarker;
import com.jby.ride.others.DirectionsParser;
import com.jby.ride.others.SquareHeightLinearLayout;
import com.jby.ride.ride.pending.pendingDetail.dialog.DeleteDialog;
import com.jby.ride.shareObject.ApiDataObject;
import com.jby.ride.shareObject.ApiManager;
import com.jby.ride.shareObject.AsyncTaskManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.jby.ride.ride.RideActivity.DELETE_PENDING_RIDE;

public class PendingRideDetailActivity extends AppCompatActivity implements OnMapReadyCallback,
        View.OnClickListener, DeleteDialog.DeleteDialogCallBack{
    private SquareHeightLinearLayout actionBarMenuIcon, actionBarCloseIcon, actionBarLogout;
    private TextView actionBarTitle;

    private EditText pendingRideDetailActivityTime;
    private EditText pendingRideDetailActivityFare, pendingRideDetailActivityPaymentMethod;
    private EditText pendingRideDetailActivityNote;
    private TextView pendingRideDetailActivityStatus, pendingRideDetailActivityPickUp, pendingRideDetailActivityDropOff;
    private CircleImageView pendingRideDetailActivityDriverProfile;
    private Button pendingRideDetailActivityEdit, pendingRideDetailActivityDelete;
//    map setting
    private MapFragment pendingRideDetailActivityMap;
    ArrayList<MarkerObject> routeArray = new ArrayList<>();
    private GoogleMap googleMap;
    private String stringPickUpPoint, stringDropOffPoint;
    static String stringPickUpAddress = "Pick up point";
    static String stringDropOffAddress = "Drop off Point";
    private MarkerOptions options;
    private String rideID;

    //    asyncTask
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;
    Handler handler;
    //    action hanlding purpose
    private int requestCode = -302;
    private FragmentManager fm;
    private DialogFragment dialogFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_pending_ride_detail);
        objectInitialize();
        objectSetting();

    }

    private void objectInitialize() {
        actionBarMenuIcon = (SquareHeightLinearLayout)findViewById(R.id.actionbar_menu);
        actionBarCloseIcon = (SquareHeightLinearLayout)findViewById(R.id.actionbar_close);
        actionBarLogout = (SquareHeightLinearLayout)findViewById(R.id.actionbar_logout);
        actionBarTitle = (TextView)findViewById(R.id.actionBar_title);

        pendingRideDetailActivityPickUp = (TextView) findViewById(R.id.fragment_pending_ride_detail_pick_up);
        pendingRideDetailActivityDropOff = (TextView)findViewById(R.id.fragment_pending_ride_detail_drop_off);
        pendingRideDetailActivityTime = (EditText)findViewById(R.id.fragment_pending_ride_detail_time);
        pendingRideDetailActivityFare = (EditText)findViewById(R.id.fragment_pending_ride_detail_fare);
        pendingRideDetailActivityPaymentMethod = (EditText)findViewById(R.id.fragment_pending_ride_detail_payment_method);
        pendingRideDetailActivityDriverProfile = (CircleImageView)findViewById(R.id.fragment_pending_ride_detail_driver_profile);
        pendingRideDetailActivityNote = (EditText)findViewById(R.id.fragment_pending_ride_detail_note);
        pendingRideDetailActivityStatus = (TextView)findViewById(R.id.fragment_pending_ride_detail_status);
        pendingRideDetailActivityMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.fragment_pending_ride_detail_map));

        pendingRideDetailActivityDelete = (Button)findViewById(R.id.fragment_pending_ride_detail_delete);
        pendingRideDetailActivityEdit = (Button)findViewById(R.id.fragment_pending_ride_detail_edit);

        handler = new Handler();
        fm = getSupportFragmentManager();
    }


    private void objectSetting() {
        //        actionBar
        actionBarTitle.setText(R.string.edit_profile_dialog_title);
        actionBarMenuIcon.setVisibility(View.GONE);
        actionBarCloseIcon.setVisibility(View.VISIBLE);
        actionBarTitle.setText(R.string.fragment_pending_ride_detail_title);

        actionBarCloseIcon.setOnClickListener(this);
        actionBarLogout.setOnClickListener(this);

        pendingRideDetailActivityDelete.setOnClickListener(this);
        pendingRideDetailActivityEdit.setOnClickListener(this);

        pendingRideDetailActivityPickUp.setSelected(true);
        pendingRideDetailActivityDropOff.setSelected(true);

        getExtraValue();
    }
//    getting value from pending ride activity
    private void getExtraValue(){
        Intent intent = getIntent();
        if(intent.getExtras() != null)
        {
            rideID = intent.getStringExtra("ride_id");
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    getPendingRideDetail();
                    initializeMap();
                }
            },200);
        }
    }

    private void getPendingRideDetail(){
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("id", rideID));

        asyncTaskManager = new AsyncTaskManager(
                this,
                new ApiManager().pendingRide,
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

                        String pickUpLocation = jsonObjectLoginResponse.getJSONObject("pending_ride_detail").getString("pick_up_location");
                        String dropOffLocation = jsonObjectLoginResponse.getJSONObject("pending_ride_detail").getString("drop_off_location");
                        String pickUpAddress = jsonObjectLoginResponse.getJSONObject("pending_ride_detail").getString("pick_up_address");
                        String dropOffAddress = jsonObjectLoginResponse.getJSONObject("pending_ride_detail").getString("drop_off_address");
                        String paymentMethod = jsonObjectLoginResponse.getJSONObject("pending_ride_detail").getString("payment_method");
                        String date = jsonObjectLoginResponse.getJSONObject("pending_ride_detail").getString("date");
                        String time = jsonObjectLoginResponse.getJSONObject("pending_ride_detail").getString("time");
                        String note = jsonObjectLoginResponse.getJSONObject("pending_ride_detail").getString("note");
                        String fare = jsonObjectLoginResponse.getJSONObject("pending_ride_detail").getString("fare");

                        setupView(pickUpLocation,dropOffLocation, pickUpAddress,dropOffAddress, paymentMethod, date, time, note, fare);

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
//    after getting data from server then setup the view
    private void setupView(String stringPickUpLocation, String stringDropOffLocation, String pickUpAddress, String dropOffAddress,
                           String paymentMethod, String date, String time, String note, String fare) {

        String rideTime = date + " " + time;
        String rideFare = "RM " + fare;

        if(paymentMethod.equals("1"))
            paymentMethod = "Cash";
        else
            paymentMethod = "RidePay";

        setStringPickUpPoint(stringPickUpLocation);
        setStringDropOffPoint(stringDropOffLocation);
        if(!note.equals("-"))
            pendingRideDetailActivityNote.setText(note);

        pendingRideDetailActivityPickUp.setText(pickUpAddress);
        pendingRideDetailActivityDropOff.setText(dropOffAddress);
        pendingRideDetailActivityPaymentMethod.setText(paymentMethod);
        pendingRideDetailActivityTime.setText(rideTime);
        pendingRideDetailActivityFare.setText(rideFare);
    }
    //convert String pick up into LatLong
    private LatLng getPickUpLatLong(String stringPickUpLocation){

        stringPickUpLocation = stringPickUpLocation.substring(10, stringPickUpLocation.length()-1);
        String[] pickUpLatLong =  stringPickUpLocation.split(",");
        double latitude = Double.parseDouble(pickUpLatLong[0]);
        double longitude = Double.parseDouble(pickUpLatLong[1]);
        return new LatLng(latitude, longitude);
    }
    //convert String drop off into LatLong
    private LatLng getDropOffLatLong(String stringDropOffLocation){

        stringDropOffLocation = stringDropOffLocation.substring(10, stringDropOffLocation.length()-1);
        String[] dropOffLatLong =  stringDropOffLocation.split(",");
        double latitude = Double.parseDouble(dropOffLatLong[0]);
        double longitude = Double.parseDouble(dropOffLatLong[1]);
        return new LatLng(latitude, longitude);
    }

    public void setStringPickUpPoint(String stringPickUpPoint) {
        this.stringPickUpPoint = stringPickUpPoint;
    }

    public void setStringDropOffPoint(String stringDropOffPoint) {
        this.stringDropOffPoint = stringDropOffPoint;
    }

//    map setting
    public void initializeMap() {
        pendingRideDetailActivityMap.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        setupMarker();
    }
//setup marker, route, zoom in camera
    private void setupMarker(){
        //        pick up point
        LatLng pickUpLocation = getPickUpLatLong(stringPickUpPoint);
        routeArray.add(new MarkerObject(pickUpLocation, stringPickUpAddress, stringPickUpAddress));

//        drop off
        LatLng dropOffLocation = getDropOffLatLong(stringDropOffPoint);
        routeArray.add(new MarkerObject(dropOffLocation, stringDropOffAddress, stringDropOffAddress));

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
            String url = getRequestDirection(routeArray.get(0).getLocation(), routeArray.get(1).getLocation());
            requestDirection(url);
            requestCameraFocus();
        }

    }
    private void requestCameraFocus(){
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for(int i = 0; i<routeArray.size(); i++)
            builder.include(routeArray.get(i).getLocation());
//
        LatLngBounds bounds = builder.build();
//
        int padding = 50; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        googleMap.animateCamera(cu);

    }

    // getting direction path
    private String getRequestDirection(LatLng origin, LatLng dest){
        String pickUpPoint = "origin=" + origin.latitude +  "," + origin.longitude;
        String dropOffPoint = "destination=" + dest.latitude +  "," + dest.longitude;
        String sensor = "sensor=false";
        String mode = "mode=driving";
        String param = pickUpPoint + "&" + dropOffPoint + "&" + sensor +  "&" + mode;
        String output = "json";
        return "https://maps.googleapis.com/maps/api/directions/" + output + "?" + param;
    }

    // request direction from api
    public void requestDirection(String url){

        asyncTaskManager = new AsyncTaskManager(
                this,
                url,
                new ApiManager().getResultParameter(
                        "", "", ""
                )
        );
        asyncTaskManager.execute();

        if (!asyncTaskManager.isCancelled()) {
            try {
                jsonObjectLoginResponse = asyncTaskManager.get(30000, TimeUnit.MILLISECONDS);
                if (jsonObjectLoginResponse != null) {
                    List<List<HashMap<String, String>>> routes;
                    DirectionsParser directionsParser = new DirectionsParser();
                    ArrayList<LatLng> points;
                    PolylineOptions polylineOptions = null;
                    String distance = "";
                    String duration = "";

                    routes = directionsParser.parse(jsonObjectLoginResponse);
                    // Traversing through all the routes
                    for(int i=0;i<routes.size();i++){
                        points = new ArrayList<LatLng>();
                        polylineOptions = new PolylineOptions();

                        // Fetching i-th route
                        List<HashMap<String, String>> path = routes.get(i);

                        // Fetching all the points in i-th route
                        for(int j=0;j<path.size();j++){
                            HashMap<String,String> point = path.get(j);

                            if(j==0){    // Get distance from the list
                                distance = (String)point.get("distance");
                                continue;
                            }else if(j==1){ // Get duration from the list
                                duration = (String)point.get("duration");
                                continue;
                            }

                            double lat = Double.parseDouble(point.get("lat"));
                            double lng = Double.parseDouble(point.get("lng"));
                            LatLng position = new LatLng(lat, lng);

                            points.add(position);
                        }
                        // Adding all the points in the route to LineOptions
                        polylineOptions.addAll(points);
                        polylineOptions.width(15);
                        polylineOptions.color(Color.BLACK);
                        polylineOptions.geodesic(true);
                    }

                    if(polylineOptions != null){
                        googleMap.addPolyline(polylineOptions);
//                        setDistanceDuration(duration, distance);
                    }
                }
                else{
                    Toast.makeText(this, "Network Error!", Toast.LENGTH_SHORT).show();
                }
            }catch (InterruptedException e) {
                Toast.makeText(this, "Interrupted Exception!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (ExecutionException e) {
                Toast.makeText(this, "Execution Exception!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }  catch (TimeoutException e) {
                Toast.makeText(this, "Connection Time Out!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.actionbar_close:
                onBackPressed();
                break;
            case R.id.fragment_pending_ride_detail_delete:
                dialogFragment = new DeleteDialog();
                dialogFragment.show(fm, "");
                break;
        }
    }

    public void deleteRide(){
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("id", rideID));
        apiDataObjectArrayList.add(new ApiDataObject("delete", "true"));

        asyncTaskManager = new AsyncTaskManager(
                this,
                new ApiManager().pendingRide,
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
                        requestCode = DELETE_PENDING_RIDE;
                        onBackPressed();
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
        Intent intent = new Intent();
        setResult(requestCode, intent);
        super.onBackPressed();
    }
}