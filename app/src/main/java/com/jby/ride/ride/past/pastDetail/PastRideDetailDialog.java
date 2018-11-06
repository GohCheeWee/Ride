package com.jby.ride.ride.past.pastDetail;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.jby.ride.R;
import com.jby.ride.others.SquareHeightLinearLayout;
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
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

import static com.jby.ride.shareObject.ApiManager.profile_prefix;


public class PastRideDetailDialog extends DialogFragment implements View.OnClickListener {
    View rootView;
    //actionbar
    private SquareHeightLinearLayout actionBarMenuIcon, actionBarCloseIcon;
    private TextView actionBarTitle;
    //scrollview
    private ScrollView pastRideDetailActivityScrollView;
    private ProgressBar pastRideDetailActivityProgressBar;
    private LinearLayout pastRideDetailActivityBookingNumberLayout;
    //driver detail
    private TextView pastRideDetailActivityBookingNumber, pastRideDetailActivityCar;
    private TextView pastRideDetailActivityDriverName;
    private CircleImageView pastRideDetailActivityDriverProfilePicture;
    //payment
    private TextView pastRideDetailActivityFare, pastRideDetailActivityPaymentMethod;
    //trip detail
    private TextView pastRideDetailActivityPickUpPoint, pastRideDetailActivityDropOffPoint;
    //rating
    private MaterialRatingBar pastRideDetailActivityRatingBar;
    private TextView pastRideDetailActivityRatingStatus;
    //comment
    private TextView pastRideDetailActivityComment;
    //    asyncTask
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;
    Handler handler;
    private String matchRideID;


    public PastRideDetailDialog() {
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.past_ride_detail_dialog, container);
        objectInitialize();
        objectSetting();
        return rootView;
    }


    private void objectInitialize() {
        //actionBar
        actionBarMenuIcon = rootView.findViewById(R.id.actionbar_menu);
        actionBarCloseIcon = rootView.findViewById(R.id.actionbar_close);
        actionBarTitle = rootView.findViewById(R.id.actionBar_title);
        //scroll view
        pastRideDetailActivityScrollView = rootView.findViewById(R.id.activity_pass_ride_detail_scroll_view);
        pastRideDetailActivityProgressBar = rootView.findViewById(R.id.activity_past_ride_detail_progress_bar);
        pastRideDetailActivityBookingNumberLayout = rootView.findViewById(R.id.past_ride_detail_dialog_booking_num_layout);
        //driver info
        pastRideDetailActivityBookingNumber = rootView.findViewById(R.id.activity_past_ride_detail_booking_no);
        pastRideDetailActivityCar = rootView.findViewById(R.id.activity_past_ride_detail_driver_car_plate);
        pastRideDetailActivityDriverName = rootView.findViewById(R.id.activity_past_ride_detail_driver_name);
        pastRideDetailActivityDriverProfilePicture = rootView.findViewById(R.id.activity_past_ride_detail_driver_profile_picture);
        //payment
        pastRideDetailActivityFare = rootView.findViewById(R.id.activity_past_ride_detail_fare);
        pastRideDetailActivityPaymentMethod = rootView.findViewById(R.id.activity_past_ride_detail_payment_method);
        //trip detail
        pastRideDetailActivityPickUpPoint = rootView.findViewById(R.id.activity_past_ride_detail_pick_up_point);
        pastRideDetailActivityDropOffPoint = rootView.findViewById(R.id.activity_past_ride_detail_drop_off_point);
        //rating
        pastRideDetailActivityRatingBar = rootView.findViewById(R.id.activity_past_ride_detail_rating);
        pastRideDetailActivityRatingStatus = rootView.findViewById(R.id.activity_past_ride_detail_rating_status);
        //fare
        pastRideDetailActivityComment = rootView.findViewById(R.id.activity_past_ride_detail_comment);
        //scroll view
        pastRideDetailActivityScrollView = rootView.findViewById(R.id.activity_pass_ride_detail_scroll_view);
        pastRideDetailActivityProgressBar = rootView.findViewById(R.id.activity_past_ride_detail_progress_bar);
        handler = new Handler();
    }

    private void objectSetting() {
        Bundle bundle = getArguments();
        if(bundle != null){
          matchRideID = bundle.getString("match_ride_id");
        }

        actionBarMenuIcon.setVisibility(View.GONE);
        actionBarCloseIcon.setVisibility(View.VISIBLE);
        actionBarCloseIcon.setOnClickListener(this);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getPastRideDetail();
            }
        },200);

    }
    private void getPastRideDetail(){
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("match_ride_id", matchRideID));

        asyncTaskManager = new AsyncTaskManager(
                getActivity(),
                new ApiManager().pastRide,
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
                        JSONArray jsonArray = jsonObjectLoginResponse.getJSONArray("value").getJSONObject(0).getJSONArray("pastRideDetail");
                        String bookingID = jsonArray.getJSONObject(0).getString("id");
                        String pickUpAddress = jsonArray.getJSONObject(0).getString("pick_up_address");
                        String dropOffAddress = jsonArray.getJSONObject(0).getString("drop_off_address");
                        String date = jsonArray.getJSONObject(0).getString("date");
                        String time = jsonArray.getJSONObject(0).getString("time");
                        String fare = jsonArray.getJSONObject(0).getString("fare");
                        String paymentMethod = jsonArray.getJSONObject(0).getString("payment_method");
                        String driverName = jsonArray.getJSONObject(0).getString("username");
                        String driverProfilePicture = jsonArray.getJSONObject(0).getString("profile_pic");
                        String carPlate = jsonArray.getJSONObject(0).getString("car_plate");
                        String carBrand = jsonArray.getJSONObject(0).getString("car_brand");
                        String carModel = jsonArray.getJSONObject(0).getString("car_model");
                        String rating = jsonArray.getJSONObject(0).getString("rating");
                        String comment = jsonArray.getJSONObject(0).getString("comment");
                        setUpView(bookingID, pickUpAddress, dropOffAddress, date, time, fare, paymentMethod, driverName, driverProfilePicture, carPlate,
                                carBrand, carModel, rating, comment);

                    }
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
        viewSetting();
    }

    private void viewSetting() {
        pastRideDetailActivityProgressBar.setVisibility(View.GONE);
        pastRideDetailActivityScrollView.setVisibility(View.VISIBLE);
        pastRideDetailActivityBookingNumberLayout.setVisibility(View.VISIBLE);
    }

    private void setUpView(String bookingID, String pickUpAddress, String dropOffAddress, String date, String time, String fare, String paymentMethod, String driverName, String driverProfilePicture, String carPlate, String carBrand, String carModel, String rating, String comment) {
        bookingID = "RIDE" + bookingID;
        date = date + " " + time;
        carPlate = carPlate + " " + carBrand + " " + carModel;
        if(paymentMethod.equals("1"))
            paymentMethod = "Cash";
        else
            paymentMethod = "RidePay";

        if(!driverProfilePicture.equals("")){
            driverProfilePicture = profile_prefix + driverProfilePicture;

            Picasso.get()
                    .load(driverProfilePicture)
                    .error(R.drawable.loading_gif)
                    .into(pastRideDetailActivityDriverProfilePicture);
        }
        actionBarTitle.setText(date);
        pastRideDetailActivityBookingNumber.setText(bookingID);
        pastRideDetailActivityCar.setText(carPlate);
        pastRideDetailActivityDriverName.setText(driverName);

        pastRideDetailActivityFare.setText(fare);
        pastRideDetailActivityPaymentMethod.setText(paymentMethod);

        pastRideDetailActivityPickUpPoint.setText(pickUpAddress);
        pastRideDetailActivityDropOffPoint.setText(dropOffAddress);

        if(!rating.equals("null")){
            float ratingRate = Float.valueOf(rating);
            pastRideDetailActivityRatingBar.setRating(ratingRate);
            pastRideDetailActivityRatingStatus.setText(setRatingStatus(ratingRate));
        }

        if(!comment.equals("null"))
            pastRideDetailActivityComment.setText(comment);

    }

    private String setRatingStatus(float rating){
        String ratingStatus;
        if(rating <= 1.0)
            ratingStatus = "Terrible";
        else if(rating <= 2.0)
            ratingStatus = "Bad";
        else if(rating <= 3.0)
            ratingStatus = "Satisfied";
        else if(rating <= 4.0)
            ratingStatus = "Good";
        else
            ratingStatus = "Excellent";

        return ratingStatus;
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.actionbar_close:
                dismiss();
                break;
        }
    }
}
