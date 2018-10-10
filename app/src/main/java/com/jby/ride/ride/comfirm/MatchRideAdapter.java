package com.jby.ride.ride.comfirm;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jby.ride.R;
import com.jby.ride.ride.comfirm.object.OtherRiderInCarObject;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.jby.ride.profile.ProfileActivity.prefix;

public class MatchRideAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<ConfirmRideObject> confirmRideObjectArrayList;
    private MatchRideAdapterCallBack matchRideAdapterCallBack;

    public MatchRideAdapter(Context context, ArrayList<ConfirmRideObject> confirmRideObjectArrayList, MatchRideAdapterCallBack matchRideAdapterCallBack){

        this.context = context;
        this.confirmRideObjectArrayList = confirmRideObjectArrayList;
        this.matchRideAdapterCallBack = matchRideAdapterCallBack;

    }
    @Override
    public int getCount() {
        return confirmRideObjectArrayList.size();
    }

    @Override
    public ConfirmRideObject getItem(int i) {
        return confirmRideObjectArrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        final ViewHolder viewHolder;
        if (view == null){
            view = View.inflate(this.context, R.layout.activity_match_ride_list_view_item, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        ConfirmRideObject object = getItem(i);
        String fare = "RM " + object.getFare();
        String date = object.getDate() + " " + object.getTime();
        String status = object.getStatus();
        String paymentMethod = object.getPaymentMethod();
        String gender = object.getDriverGender();
        String profilePicture = object.getDriverImage();
        String carPlate = object.getDriver_plate();
        String carBrand = object.getDriver_brand();
        String carModel = object.getDriver_model();
        final ArrayList<OtherRiderInCarObject> otherRiderInCarObjectArrayList = object.getOtherRiderInCarObjectArrayList();
        //    path
        String profile_prefix = "http://188.166.186.198/~cheewee/ride/frontend/driver/profile/driver_profile_picture/";
        String vehicleDetail = carPlate + " . " + carBrand + " " + carModel;

        if(paymentMethod.equals("1"))
            paymentMethod = "Cash";
        else
            paymentMethod = "RidePay";

        if(!status.equals("2")){
            viewHolder.trackRoute.setTextColor(context.getResources().getColor(R.color.red));
            viewHolder.trackRoute.setEnabled(true);
        }
        else{
            viewHolder.trackRoute.setTextColor(context.getResources().getColor(R.color.disable_color));
            viewHolder.trackRoute.setEnabled(false);
        }

        switch(status){
            case "2":
                status = context.getResources().getString(R.string.match_ride_activity_status_not_start);
                break;
            case "3":
                status = context.getResources().getString(R.string.match_ride_activity_status_coming);
                break;
            case "4":
                status = context.getResources().getString(R.string.match_ride_activity_status_arrived);
                break;
            case "5":
                status = context.getResources().getString(R.string.match_ride_activity_status_destination);
                break;
        }

        if(gender.equals("1"))
            viewHolder.driver_gender.setImageDrawable(context.getResources().getDrawable(R.drawable.activity_profile_male_icon));
        else
            viewHolder.driver_gender.setImageDrawable(context.getResources().getDrawable(R.drawable.activity_profile_female_icon));

        if(profilePicture != null){
            profilePicture = profile_prefix + profilePicture;

            Picasso.get()
                    .load(profilePicture)
                    .error(R.drawable.loading_gif)
                    .into(viewHolder.driver);
        }
        else{
            viewHolder.driver.setImageDrawable(context.getResources().getDrawable(R.drawable.driver_found_dialog_driver_icon));
        }
        // for other ride image icon purpose
        if(otherRiderInCarObjectArrayList.size() > 0){
            viewHolder.otherRiderLayout.setVisibility(View.VISIBLE);
            viewHolder.labelOtherRider.setVisibility(View.VISIBLE);

            for(int j = 0 ; j < otherRiderInCarObjectArrayList.size(); j++){
                final CircleImageView image = new CircleImageView(context);
                String otherRiderProfilePictureUrl = prefix + otherRiderInCarObjectArrayList.get(i).getProfile_picture();

                //calculate image view size based on device width
                ViewTreeObserver viewTreeObserver = viewHolder.otherRiderLayout.getViewTreeObserver();
                if (viewTreeObserver.isAlive()) {
                    viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            viewHolder.otherRiderLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            int parentLayoutWidth = viewHolder.otherRiderLayout.getWidth();

                            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams)image.getLayoutParams();
                            layoutParams.width = parentLayoutWidth/8;
                            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                            image.setLayoutParams(layoutParams);
                        }
                    });
                }
                viewHolder.otherRiderLayout.addView(image);
                Picasso.get()
                        .load(otherRiderProfilePictureUrl)
                        .error(R.drawable.loading_gif)
                        .into(image);
            }
        }
        else {
            viewHolder.otherRiderLayout.setVisibility(View.GONE);
            viewHolder.labelOtherRider.setVisibility(View.GONE);
        }


        viewHolder.fare.setText(fare);
        viewHolder.pickup.setText(object.getPickUpPoint());
        viewHolder.dropoff.setText(object.getDropOffPoint());
        viewHolder.date.setText(date);
        viewHolder.payment_method.setText(paymentMethod);
        viewHolder.note.setText(object.getNote());
        viewHolder.status.setText(status);
        viewHolder.driver_name.setText(object.getDriverName());
        viewHolder.driver_vehicle.setText(vehicleDetail);

        viewHolder.trackRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickEffect(view);
                matchRideAdapterCallBack.trackMyRoute(i);
            }
        });

        viewHolder.otherRiderLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickEffect(view);
                matchRideAdapterCallBack.viewAllRiderDialog(otherRiderInCarObjectArrayList);
            }
        });
        return view;
    }

    public interface MatchRideAdapterCallBack{
        void trackMyRoute(int position);
        void viewAllRiderDialog(ArrayList<OtherRiderInCarObject> otherRiderInCarObjectArrayList);
    }
    //click effect
    public void clickEffect(View view){
        Animation animation1 = new AlphaAnimation(0.3f, 1.0f);
        animation1.setDuration(500);
        view.startAnimation(animation1);
    }

    private static class ViewHolder{
        private TextView status, fare, pickup, dropoff, date, payment_method, note, driver_name, driver_vehicle;
        private TextView trackRoute, labelOtherRider;
        private CircleImageView driver;
        private ImageView driver_gender;
        private LinearLayout otherRiderLayout;
        ViewHolder (View view){
            status = (TextView)view.findViewById(R.id.activity_match_ride_list_view_status);
            fare = (TextView)view.findViewById(R.id.activity_match_ride_list_view_fare);
            pickup = (TextView)view.findViewById(R.id.activity_match_ride_list_view_pick_up_point);
            dropoff = (TextView)view.findViewById(R.id.activity_match_ride_list_view_drop_off_point);
            date = (TextView)view.findViewById(R.id.activity_match_ride_list_view_date);
            payment_method = (TextView)view.findViewById(R.id.activity_match_ride_list_view_payment_method);
            note = (TextView)view.findViewById(R.id.activity_match_ride_list_view_note);

            trackRoute = (TextView)view.findViewById(R.id.activity_match_ride_list_view_detail);

            driver_name = (TextView)view.findViewById(R.id.activity_match_ride_list_view_driver_name);
            driver_vehicle = (TextView)view.findViewById(R.id.match_rider_detail_list_view_driver_vehicle);

            driver = (CircleImageView)view.findViewById(R.id.activity_match_ride_list_view_driver);
            driver_gender = (ImageView)view.findViewById(R.id.activity_match_ride_list_view_driver_gender_icon);

            otherRiderLayout= view.findViewById(R.id.activity_match_ride_list_view_item_other_rider_layout);
            labelOtherRider = view.findViewById(R.id.activity_match_ride_list_view_item_label_other_rider);
        }
    }
}
