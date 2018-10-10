package com.jby.ride.ride.past;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jby.ride.R;
import java.util.ArrayList;

public class PastRideAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<PastRideObject> pastRideObjectArrayList;

    public PastRideAdapter(Context context, ArrayList<PastRideObject> pastRideObjectArrayList){

        this.context = context;
        this.pastRideObjectArrayList = pastRideObjectArrayList;

    }
    @Override
    public int getCount() {
        return pastRideObjectArrayList.size();
    }

    @Override
    public PastRideObject getItem(int i) {
        return pastRideObjectArrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null){
            view = View.inflate(this.context, R.layout.fragment_past_ride_list_view_item, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        PastRideObject object = getItem(i);
        String fare = "RM " + object.getFare();
        String dateTime = object.getDate() + " " + object.getTime();

        viewHolder.fare.setText(fare);
        viewHolder.pickup.setText(object.getPickUpPoint());
        viewHolder.dropOff.setText(object.getDropOffPoint());
        viewHolder.dateTiime.setText(dateTime);

        return view;
    }

    private static class ViewHolder{
        private TextView  fare, pickup, dropOff, dateTiime;

        ViewHolder (View view){
            fare = (TextView)view.findViewById(R.id.fragment_past_ride_list_view_item_fare);
            pickup = (TextView)view.findViewById(R.id.fragment_past_ride_list_view_item_pick_up);
            dropOff = (TextView)view.findViewById(R.id.fragment_past_ride_list_view_item_drop_off);
            dateTiime = (TextView)view.findViewById(R.id.fragment_past_ride_list_view_item_data_time);
        }
    }
}
