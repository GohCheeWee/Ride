package com.jby.ride.others.dialog;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jby.ride.R;
import com.jby.ride.shareObject.ApiDataObject;
import com.jby.ride.shareObject.ApiManager;
import com.jby.ride.shareObject.AsyncTaskManager;
import com.jby.ride.sharePreference.SharedPreferenceManager;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.jby.ride.shareObject.ApiManager.car_prefix;
import static com.jby.ride.shareObject.ApiManager.profile_prefix;

public class DriverFoundDialog extends DialogFragment implements View.OnClickListener {
    View rootView;
    private TextView driverFoundDialogDriver, driverFoundDialogRating, driverFoundDialogCompletedRide;
    private TextView driverFoundDialogVehicleDetail, driverFoundDialogCancelButton;
    private CircleImageView driverFoundDialogProfilePicture;
    private ImageView driverFoundDialogBackgroundPicture, driverFoundDialogCancelIcon, driverFoundDialogGenderIcon;
    private Button driverFoundDialogConfirmButton;
    private ProgressBar driverFoundDialogProgressBar;
    private LinearLayout driverFoundDialogMainLayout, driverFoundDialogParentLayout;
    private RelativeLayout driverFoundDialogPictureParentLayout;

    //    async purpose
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;
    private Handler handler;
    private String matchRideID;


    DriverFoundDialogCallBack driverFoundDialogCallBack;
    String note;

    public DriverFoundDialog() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.driver_found_dialog, container);
        objectInitialize();
        objectSetting();
        driverFoundDialogCallBack = (DriverFoundDialogCallBack) getActivity();
        return rootView;
    }

    private void objectInitialize() {

        driverFoundDialogDriver = (TextView)rootView.findViewById(R.id.driver_found_dialog_driver);
        driverFoundDialogRating = (TextView)rootView.findViewById(R.id.driver_found_dialog_rating);
        driverFoundDialogCompletedRide = (TextView)rootView.findViewById(R.id.driver_found_dialog_completed_ride);
        driverFoundDialogVehicleDetail = (TextView)rootView.findViewById(R.id.driver_found_dialog_vehicle_detail);
        driverFoundDialogCancelButton = (TextView)rootView.findViewById(R.id.driver_found_dialog_cancel_button);

        driverFoundDialogBackgroundPicture = (ImageView)rootView.findViewById(R.id.driver_found_dialog_background_picture);
        driverFoundDialogCancelIcon = (ImageView)rootView.findViewById(R.id.driver_found_dialog_cancel_icon);

        driverFoundDialogCancelIcon = (ImageView)rootView.findViewById(R.id.driver_found_dialog_cancel_icon);
        driverFoundDialogGenderIcon = (ImageView)rootView.findViewById(R.id.driver_found_dialog_profile_gender_icon);
        driverFoundDialogProfilePicture = (CircleImageView)rootView.findViewById(R.id.driver_found_dialog_profile_picture);

        driverFoundDialogConfirmButton = (Button)rootView.findViewById(R.id.driver_found_dialog_comfirm_button);

        driverFoundDialogProgressBar = (ProgressBar) rootView.findViewById(R.id.driver_found_dialog_progress_bar);
        driverFoundDialogMainLayout = (LinearLayout) rootView.findViewById(R.id.driver_found_dialog_main_layout);

        driverFoundDialogParentLayout = (LinearLayout)rootView.findViewById(R.id.driver_found_dialog_parent_layout);
        driverFoundDialogPictureParentLayout = rootView.findViewById(R.id.driver_found_dialog_picture_parent_layout);

        handler = new Handler();

    }

    private void objectSetting() {
        SharedPreferenceManager.setMatchRideID(getActivity(), "default");
        driverFoundDialogConfirmButton.setOnClickListener(this);
        driverFoundDialogCancelButton.setOnClickListener(this);
        driverFoundDialogCancelIcon.setOnClickListener(this);
        Bundle mArgs = getArguments();
        if (mArgs != null) {
            matchRideID = mArgs.getString("match_ride_id");
        }

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getDriverInformation();
            }
        },200);
        calcualateHeight();
    }

    public void getDriverInformation(){
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("match_ride_id", matchRideID));

        asyncTaskManager = new AsyncTaskManager(
                getActivity(),
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
                    if (jsonObjectLoginResponse.getString("status").equals("1")) {
                        JSONArray jsonArray = jsonObjectLoginResponse.getJSONArray("value").getJSONObject(0).getJSONArray("matched_driver_profile");
                        for(int i = 0; i < jsonArray.length(); i++){
                            String username = jsonArray.getJSONObject(i).getString("username");
                            String gender = jsonArray.getJSONObject(i).getString("gender");
                            String profile_pic = jsonArray.getJSONObject(i).getString("profile_pic");
                            String car_pic = jsonArray.getJSONObject(i).getString("car_pic");
                            String car_brand = jsonArray.getJSONObject(i).getString("car_brand");
                            String car_model = jsonArray.getJSONObject(i).getString("car_model");
                            String car_plate = jsonArray.getJSONObject(i).getString("car_plate");

                            setInitializeView(username, gender, profile_pic, car_pic, car_brand, car_model, car_plate);
                        }
                    }
                    else if(jsonObjectLoginResponse.getString("status").equals("2"))
                        showSnackBar("Something went wrong!");
                }
                else {
                    Toast.makeText(getActivity(), "Network Error!", Toast.LENGTH_SHORT).show();
                }
            } catch (InterruptedException e) {
                Toast.makeText(getActivity(), "Interrupted Exception!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (ExecutionException e) {
                Toast.makeText(getActivity(), "Execution Exception!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (JSONException e) {
                Toast.makeText(getActivity(), "JSON Exception!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (TimeoutException e) {
                Toast.makeText(getActivity(), "Connection Time Out!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
        driverFoundDialogProgressBar.setVisibility(View.GONE);
        driverFoundDialogMainLayout.setVisibility(View.VISIBLE);
    }

    public void acceptDriver(){
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("match_ride_id", matchRideID));
        apiDataObjectArrayList.add(new ApiDataObject("status", "2"));
        apiDataObjectArrayList.add(new ApiDataObject("accept", "1"));

        asyncTaskManager = new AsyncTaskManager(
                getActivity(),
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
                    if (jsonObjectLoginResponse.getString("status").equals("1")) {
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                driverFoundDialogCallBack.acceptDriver();
                            }
                        },200);
                        dismiss();
                    }
                    else if(jsonObjectLoginResponse.getString("status").equals("2"))
                        showSnackBar("Something went wrong!");
                }
                else {
                    Toast.makeText(getActivity(), "Network Error!", Toast.LENGTH_SHORT).show();
                }
            } catch (InterruptedException e) {
                Toast.makeText(getActivity(), "Interrupted Exception!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (ExecutionException e) {
                Toast.makeText(getActivity(), "Execution Exception!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (JSONException e) {
                Toast.makeText(getActivity(), "JSON Exception!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (TimeoutException e) {
                Toast.makeText(getActivity(), "Connection Time Out!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
        driverFoundDialogProgressBar.setVisibility(View.GONE);
    }

    public void rejectDriverRequest(){
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("match_ride_id", matchRideID));
        apiDataObjectArrayList.add(new ApiDataObject("reject", "1"));

        asyncTaskManager = new AsyncTaskManager(
                getActivity(),
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
                    if (jsonObjectLoginResponse.getString("status").equals("1")) {
                        dismiss();
                    }
                    else if(jsonObjectLoginResponse.getString("status").equals("2"))
                        showSnackBar("Something went wrong!");
                }
                else {
                    Toast.makeText(getActivity(), "Network Error!", Toast.LENGTH_SHORT).show();
                }
            } catch (InterruptedException e) {
                Toast.makeText(getActivity(), "Interrupted Exception!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (ExecutionException e) {
                Toast.makeText(getActivity(), "Execution Exception!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (JSONException e) {
                Toast.makeText(getActivity(), "JSON Exception!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (TimeoutException e) {
                Toast.makeText(getActivity(), "Connection Time Out!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
        driverFoundDialogProgressBar.setVisibility(View.GONE);
    }

    private void setInitializeView(String username, String gender, String profile_pic, String car_pic, String car_brand, String car_model, String car_plate) {
        driverFoundDialogDriver.setText(username);

        String vehicleDetail = car_plate + " . " + car_brand + " " + car_model;
        driverFoundDialogVehicleDetail.setText(vehicleDetail);

        if(gender.equals("1"))
            driverFoundDialogGenderIcon.setImageDrawable(getResources().getDrawable(R.drawable.activity_profile_male_icon));
        else
            driverFoundDialogGenderIcon.setImageDrawable(getResources().getDrawable(R.drawable.activity_profile_female_icon));

        if(!profile_pic.equals("")){
            profile_pic = profile_prefix + profile_pic;
            Picasso.get()
                    .load(profile_pic)
                    .error(R.drawable.loading_gif)
                    .into(driverFoundDialogProfilePicture);
        }
        else
            driverFoundDialogProfilePicture.setImageDrawable(getResources().getDrawable(R.drawable.driver_found_dialog_driver_icon));

        if (!car_pic.equals("")){
            car_pic = car_prefix + car_pic;
            Picasso.get()
                    .load(car_pic)
                    .error(R.drawable.loading_gif)
                    .fit()
                    .centerCrop()
                    .into(driverFoundDialogBackgroundPicture);
        }
        else
            driverFoundDialogBackgroundPicture.setImageDrawable(getResources().getDrawable(R.drawable.driver_found_dialog_car_icon));

    }

    //    snackBar setting
    private void showSnackBar(String message) {
        final Snackbar snackbar = Snackbar.make(driverFoundDialogBackgroundPicture, message, Snackbar.LENGTH_SHORT);
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
    public void onStart() {
        super.onStart();
        Dialog d = getDialog();
        if (d != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            d.getWindow().setLayout(width, height);
            d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            d.getWindow().setWindowAnimations(R.style.dialog_up_down);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id. driver_found_dialog_comfirm_button:
                driverFoundDialogProgressBar.setVisibility(View.VISIBLE);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        acceptDriver();
                    }
                },200);
                break;
            case R.id.driver_found_dialog_cancel_button:
                driverFoundDialogProgressBar.setVisibility(View.VISIBLE);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        rejectDriverRequest();
                    }
                },200);
                break;
            case R.id.driver_found_dialog_cancel_icon:
                driverFoundDialogProgressBar.setVisibility(View.VISIBLE);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        rejectDriverRequest();
                    }
                },200);
                break;
        }
    }

    private void calcualateHeight(){
        ViewTreeObserver viewTreeObserver = driverFoundDialogParentLayout.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {

                    driverFoundDialogParentLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    int parentLayoutHeight = driverFoundDialogParentLayout.getHeight();
                    int parentLayoutWidth = driverFoundDialogParentLayout.getWidth();

                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams)driverFoundDialogPictureParentLayout.getLayoutParams();
                    layoutParams.height = parentLayoutHeight/3;
                    layoutParams.width = parentLayoutWidth;

                    driverFoundDialogPictureParentLayout.setLayoutParams(layoutParams);

                }
            });
        }
    }

    public interface DriverFoundDialogCallBack {
        void acceptDriver();
    }
}