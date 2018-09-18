package com.jby.ride.ride.pending;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jby.ride.R;
import com.jby.ride.ride.pending.PendingRideObject;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class PendingRideAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<PendingRideObject> pendingRideObjectArrayList;

    public PendingRideAdapter(Context context, ArrayList<PendingRideObject> pendingRideObjectArrayList){

        this.context = context;
        this.pendingRideObjectArrayList = pendingRideObjectArrayList;

    }
    @Override
    public int getCount() {
        return pendingRideObjectArrayList.size();
    }

    @Override
    public PendingRideObject getItem(int i) {
        return pendingRideObjectArrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null){
            view = View.inflate(this.context, R.layout.fragment_pending_ride_list_view_layout, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        PendingRideObject object = getItem(i);
        String fare = "RM " + object.getFare();

        viewHolder.fare.setText(fare);
        viewHolder.pickup.setText(object.getPickUpPoint());
        viewHolder.dropoff.setText(object.getDropOffPoint());
        viewHolder.date.setText(object.getDate());
        viewHolder.time.setText(object.getTime());

        return view;
    }

    private static class ViewHolder{
        private TextView status, fare, pickup, dropoff, date, time;

        ViewHolder (View view){
            status = (TextView)view.findViewById(R.id.fragment_pending_ride_list_view_status);
            fare = (TextView)view.findViewById(R.id.fragment_pending_ride_list_view_fare);
            pickup = (TextView)view.findViewById(R.id.fragment_pending_ride_list_view_pickup_point);
            dropoff = (TextView)view.findViewById(R.id.fragment_pending_ride_list_view_dropoff);
            date = (TextView)view.findViewById(R.id.fragment_pending_ride_list_view_date);
            time = (TextView)view.findViewById(R.id.fragment_pending_ride_list_view_time);
        }
    }
}
