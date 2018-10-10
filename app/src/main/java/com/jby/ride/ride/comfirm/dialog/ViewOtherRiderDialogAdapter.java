package com.jby.ride.ride.comfirm.dialog;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jby.ride.R;
import com.jby.ride.ride.comfirm.object.OtherRiderInCarObject;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.jby.ride.profile.ProfileActivity.prefix;

public class ViewOtherRiderDialogAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<OtherRiderInCarObject> otherRiderInCarObjectArrayList;

    public ViewOtherRiderDialogAdapter(Context context, ArrayList<OtherRiderInCarObject> otherRiderInCarObjectArrayList){

        this.context = context;
        this.otherRiderInCarObjectArrayList = otherRiderInCarObjectArrayList;

    }
    @Override
    public int getCount() {
        return otherRiderInCarObjectArrayList.size();
    }

    @Override
    public OtherRiderInCarObject getItem(int i) {
        return otherRiderInCarObjectArrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null){
            view = View.inflate(this.context, R.layout.fragment_confirm_ride_view_other_rider_dialog_list_view_item, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        OtherRiderInCarObject object = getItem(i);

        String profilePictureUrl = object.getProfile_picture();
        if(!profilePictureUrl.substring(0, 8).equals("https://")){
            profilePictureUrl = prefix + object.getProfile_picture();
        }
        viewHolder.username.setText(object.getUsername());
        viewHolder.gender.setText(object.getGender());

        Picasso.get()
                .load(profilePictureUrl)
                .error(R.drawable.loading_gif)
                .into(viewHolder.profile_picture);
        return view;
    }

    private static class ViewHolder{
        private TextView username, gender;
        private CircleImageView profile_picture;

        ViewHolder (View view){
            username = (TextView)view.findViewById(R.id.view_other_rider_dialog_list_view_user_name);
            gender = (TextView)view.findViewById(R.id.view_other_rider_dialog_list_view_gender);
            profile_picture = view.findViewById(R.id.view_other_rider_dialog_list_view_user_profile_picture);

        }
    }
}
