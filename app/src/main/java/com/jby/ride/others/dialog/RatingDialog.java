package com.jby.ride.others.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jby.ride.R;
import com.jby.ride.shareObject.ApiDataObject;
import com.jby.ride.shareObject.ApiManager;
import com.jby.ride.shareObject.AsyncTaskManager;
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

public class RatingDialog extends DialogFragment implements View.OnClickListener, RatingBar.OnRatingBarChangeListener {
    View rootView;
    private CircleImageView ratingDialogDriverProfilePicture;
    private ImageView ratingDialogCancelButton;
    private TextView ratingDialogDate, ratingDialogRatingStatus, ratingDialogPickUpAddress, ratingDialogDropOffAddress;
    private TextView ratingDialogFare;
    private EditText ratingDialogComment;
    private Button ratingDialogSubmitButton;
    private RatingBar ratingDialogRatingBar;
    private LinearLayout ratingDialogParentLayout, ratingDialogProgressBarLayout;
    //    path
    private static String profile_prefix = "http://188.166.186.198/~cheewee/ride/frontend/driver/profile/driver_profile_picture/";
    //    async purpose
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;
    private Handler handler;
    RatingDialogCallBack ratingDialogCallBack;
    private String matchRideId;

    public RatingDialog() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.rating_dialog, container);
        objectInitialize();
        objectSetting();
        ratingDialogCallBack = (RatingDialogCallBack) getActivity();
        return rootView;
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
    public void onDismiss(DialogInterface dialog) {
        ratingDialogCallBack.refresh();
        super.onDismiss(dialog);
    }

    private void objectInitialize() {
        ratingDialogDriverProfilePicture = rootView.findViewById(R.id.rating_dialog_user_icon);

        ratingDialogCancelButton = rootView.findViewById(R.id.rating_dialog_cancel_button);

        ratingDialogDate = rootView.findViewById(R.id.rating_dialog_date);
        ratingDialogRatingStatus = rootView.findViewById(R.id.rating_dialog_rating_status);
        ratingDialogPickUpAddress = rootView.findViewById(R.id.rating_dialog_pick_up_point);
        ratingDialogDropOffAddress = rootView.findViewById(R.id.rating_dialog_drop_off_point);
        ratingDialogFare = rootView.findViewById(R.id.rating_dialog_fare);

        ratingDialogComment = rootView.findViewById(R.id.rating_dialog_comment);
        ratingDialogSubmitButton = rootView.findViewById(R.id.rating_dialog_submit_button);
        ratingDialogRatingBar = rootView.findViewById(R.id.rating_dialog_rating_bar);

        ratingDialogParentLayout = rootView.findViewById(R.id.rating_dialog_parent_layout);
        ratingDialogProgressBarLayout = rootView.findViewById(R.id.rating_dialog_progress_bar_layout);
        handler = new Handler();

    }

    private void objectSetting() {
        Bundle bundle = getArguments();
        if(bundle != null)
            matchRideId = bundle.getString("match_ride_id");

        ratingDialogRatingBar.setOnRatingBarChangeListener(this);
        ratingDialogSubmitButton.setOnClickListener(this);
        ratingDialogCancelButton.setOnClickListener(this);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getCompletedRideDetail();
            }
        }, 100);
    }


    public void getCompletedRideDetail(){
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("match_ride_id", matchRideId));

        asyncTaskManager = new AsyncTaskManager(
                getActivity(),
                new ApiManager().rating,
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
                        JSONArray jsonArray = jsonObjectLoginResponse.getJSONArray("value").getJSONObject(0).getJSONArray("completeRide");
                        String driverName = jsonArray.getJSONObject(0).getString("username");
                        String driverProfilePicture = jsonArray.getJSONObject(0).getString("profile_pic");
                        String pickUpAddress = jsonArray.getJSONObject(0).getString("pick_up_address");
                        String dropOffAddress = jsonArray.getJSONObject(0).getString("drop_off_address");
                        String fare = jsonArray.getJSONObject(0).getString("fare");
                        String date = jsonArray.getJSONObject(0).getString("updated_at");

                        setUpInitializeView(driverName, driverProfilePicture, pickUpAddress, dropOffAddress, fare, date);
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
    }

    public void storeRating(){
        String  rating = String.valueOf(ratingDialogRatingBar.getRating());
        String comment = ratingDialogComment.getText().toString();

        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("match_ride_id", "171"));
        apiDataObjectArrayList.add(new ApiDataObject("rating", rating));
        apiDataObjectArrayList.add(new ApiDataObject("comment", comment));

        asyncTaskManager = new AsyncTaskManager(
                getActivity(),
                new ApiManager().rating,
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
                        ratingDialogCallBack.showSnackBar("Thank you for your rating :)");
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
    }

    private void setUpInitializeView(String driverName, String driverProfilePicture, String pickUpAddress, String dropOffAddress, String fare, String date) {
        String ratingStatus = "How was the service about " + driverName ;
        fare = "RM " + fare;

        ratingDialogPickUpAddress.setText(pickUpAddress);
        ratingDialogDropOffAddress.setText(dropOffAddress);
        ratingDialogFare.setText(fare);
        ratingDialogDate.setText(date);
        ratingDialogRatingStatus.setText(ratingStatus);

        if(!driverProfilePicture.equals("")){
            driverProfilePicture = profile_prefix + driverProfilePicture;
            Picasso.get()
                    .load(driverProfilePicture)
                    .error(R.drawable.loading_gif)
                    .into(ratingDialogDriverProfilePicture);
        }
        else
            ratingDialogDriverProfilePicture.setImageDrawable(getResources().getDrawable(R.drawable.driver_found_dialog_driver_icon));

        ratingDialogProgressBarLayout.setVisibility(View.GONE);
        ratingDialogParentLayout.setVisibility(View.VISIBLE);
    }

    //    snackBar setting
    private void showSnackBar(String message) {
        final Snackbar snackbar = Snackbar.make(ratingDialogParentLayout, message, Snackbar.LENGTH_SHORT);
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
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.rating_dialog_submit_button:
                showProgressBar();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        storeRating();
                    }
                },200);
                break;
            case R.id.rating_dialog_cancel_button:
                dismiss();
                break;
        }
    }

    private void showProgressBar() {
        ratingDialogProgressBarLayout.setVisibility(View.VISIBLE);
        ratingDialogProgressBarLayout.setBackgroundColor(Objects.requireNonNull(getActivity()).getResources().getColor(R.color.transparent));
    }

    @Override
    public void onRatingChanged(RatingBar ratingBar, float rating, boolean b) {
        if(rating < 1.0){
            ratingBar.setRating(1.0f);
            rating = 1.0f;
        }
        setRatingStatus(rating);
        ratingDialogSubmitButton.setEnabled(true);
        ratingDialogSubmitButton.setBackground(Objects.requireNonNull(getActivity()).getResources().getDrawable(R.drawable.activity_login_custom_button));
    }

    private void setRatingStatus(float rating){
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

        ratingDialogRatingStatus.setText(ratingStatus);
    }

    public interface RatingDialogCallBack {
        void showSnackBar(String message);
        void refresh();
    }
}