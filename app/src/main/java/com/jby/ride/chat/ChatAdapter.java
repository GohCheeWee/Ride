package com.jby.ride.chat;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jby.ride.R;
import com.jby.ride.others.SquareHeightLinearLayout;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.jby.ride.profile.ProfileActivity.prefix;
import static com.jby.ride.shareObject.ApiManager.profile_prefix;

public class ChatAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<ChatObject> chatObjectArrayList;

    public ChatAdapter(Context context, ArrayList<ChatObject> chatObjectArrayList){

        this.context = context;
        this.chatObjectArrayList = chatObjectArrayList;

    }
    @Override
    public int getCount() {
        return chatObjectArrayList.size();
    }

    @Override
    public ChatObject getItem(int i) {
        return chatObjectArrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null){
            view = View.inflate(this.context, R.layout.activity_chat_list_view_item, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        ChatObject object = getItem(i);
        String groupName = "RIDE" + object.getId();
        String status = object.getStatus();
        String sender = object.getUsername() + ": ";

        viewHolder.message.setText(object.getMessage());
        viewHolder.message.setTextColor(getTextColorByStatus(status));

        viewHolder.status.setVisibility(status.equals("0")? View.VISIBLE : View.GONE );

        viewHolder.groupName.setText(groupName);
        viewHolder.date.setText(getDate(object.getSendDate()));

        viewHolder.sender.setText(sender);
        viewHolder.sender.setTextColor(getTextColorByStatus(status));
        if(object.getProfilePicture().length() > 0)
        Picasso.get()
                .load(imageUrl(object.getSendFrom(), object.getProfilePicture()))
                .centerCrop()
                .fit()
                .error(R.drawable.loading_gif)
                .into(viewHolder.icon);

        return view;
    }

    private String getDate(String dateTime){
        String currentDate = String.valueOf(android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date()));
        String date = dateTime.substring(0, dateTime.length() - 9);

        if(currentDate.equals(date)){
            return dateTime.substring(10, dateTime.length()-3);
        }
        else
            return date;
    }

    private int getTextColorByStatus(String status){
        if(status.equals("0")) return context.getResources().getColor(R.color.black);
        else return context.getResources().getColor(R.color.transparent_black);
    }

    private String imageUrl(String sendFrom, String profilePicture){
        if(sendFrom.equals("rider")){
            if(!profilePicture.equals("")){
                if(!profilePicture.substring(0, 8).equals("https://"))
                    profilePicture = prefix + profilePicture;
            }
            return profilePicture;
        }
        else{
            return profile_prefix + profilePicture;
        }
    }

    private static class ViewHolder{
        private TextView  message, groupName, date, sender;
        private CircleImageView icon;
        private SquareHeightLinearLayout status;

        ViewHolder (View view){
            message = view.findViewById(R.id.activity_chat_list_view_item_message);
            sender = view.findViewById(R.id.activity_chat_list_view_item_sender);
            groupName = view.findViewById(R.id.activity_chat_list_view_item_group_name);
            date = view.findViewById(R.id.activity_chat_list_view_item_date);
            status = view.findViewById(R.id.activity_chat_list_view_status_icon);
            icon = view.findViewById(R.id.activity_chat_list_view_item_chat_room_icon);
        }
    }
}
