package com.jby.ride.chat.chatRoom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.jby.ride.R;
import com.jby.ride.sharePreference.SharedPreferenceManager;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.jby.ride.others.dialog.RiderProfileDialog.prefix;
import static com.jby.ride.shareObject.ApiManager.profile_prefix;

public class ChatRoomAdapter extends BaseExpandableListAdapter {
    private Context context;
    private ArrayList<ChatRoomGroupObject> chatRoomGroupObjectArrayList;
    private boolean isSendView = true;
    private SendViewHolder sendViewHolder;
    private ReceiveViewHolder receiveViewHolder;

    public ChatRoomAdapter(Context context, ArrayList<ChatRoomGroupObject> chatRoomGroupObjectArrayList){
        this.context = context;
        this.chatRoomGroupObjectArrayList = chatRoomGroupObjectArrayList;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
    /*-----------------------------------------------------------------------------PARENT VIEW-------------------------------------------------------------*/
    @SuppressLint("InflateParams")
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupViewHolder groupViewHolder;
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert layoutInflater != null;
            convertView = layoutInflater.inflate(R.layout.activity_chat_room_parent_list_view_item, null);
            groupViewHolder = new GroupViewHolder(convertView);
            convertView.setTag(groupViewHolder);

        }
        else
            groupViewHolder = (GroupViewHolder)convertView.getTag();

        ChatRoomGroupObject object = getGroup(groupPosition);
        groupViewHolder.date.setText(object.getSendDate());

        return convertView;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public ChatRoomGroupObject getGroup(int i) {
        return chatRoomGroupObjectArrayList.get(i);
    }

    private static class GroupViewHolder{
        TextView date;
        GroupViewHolder (View view){
            date = view.findViewById(R.id.chat_room_parent_list_view_item_date);
        }
    }

    @Override
    public int getGroupCount() {
        return chatRoomGroupObjectArrayList.size();
    }


    /*-----------------------------------------------------------------------END OF PARENT VIEW-------------------------------------------------------------*/
    /*---------------------------------------------------------------------------CHILD VIEW-------------------------------------------------------------------*/
    @SuppressLint("InflateParams")
    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View view, ViewGroup parent) {
        ChatRoomChildObject object = getChild(groupPosition, childPosition);
            //select view based on send or receive
            view = selectView(object.getSendFrom(), object.getSenderID());
            //check send view or receive view
            if(isSendView)
                //send object
                sendViewHolder.bindMessage(object);
            else
                //send object
                receiveViewHolder.bindMessage(object);
        return view;
    }

    private View selectView(String sendFrom, String senderId){
        LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        View view;

        if(sendFrom.equals("rider")){
            view = layoutInflater.inflate(R.layout.activity_chat_room_send_view, null);
            sendViewHolder = new SendViewHolder(view);
            view.setTag(sendViewHolder);
            isSendView = true;
        }
        else{
            view = layoutInflater.inflate(R.layout.activity_chat_room_receive_view, null);
            receiveViewHolder = new ReceiveViewHolder(view);
            view.setTag(receiveViewHolder);
            isSendView = false;
        }
        return view;
    }
    @Override
    public boolean isChildSelectable(int i, int i1) {
        return false;
    }

    @Override
    public int getChildrenCount(int i) {
        return chatRoomGroupObjectArrayList.get(i).getChatRoomChildObjectArrayList().size();
    }

    @Override
    public ChatRoomChildObject getChild(int groupPosition, int childPosition) {
        return chatRoomGroupObjectArrayList.get(groupPosition).getChatRoomChildObjectArrayList().get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    private static class SendViewHolder {
         TextView message, date;

        SendViewHolder (View view){
            message = view.findViewById(R.id.chat_room_dialog_send_view_message);
            date = view.findViewById(R.id.chat_room_dialog_send_view_date);
        }

        void bindMessage(ChatRoomChildObject object){
            message.setText(object.getMessage());
            date.setText(subStringTime(object.getSendDate()));
        }
    }

    private static class ReceiveViewHolder{
        TextView message, date;
        CircleImageView userIcon;

        ReceiveViewHolder (View view){
            message = view.findViewById(R.id.chat_room_dialog_receive_view_message);
            date = view.findViewById(R.id.chat_room_dialog_receive_view_date);
            userIcon = view.findViewById(R.id.chat_room_dialog_receive_view_user_icon);
        }

        private void bindMessage(ChatRoomChildObject object){
            message.setText(object.getMessage());
            date.setText(subStringTime(object.getSendDate()));
            if(object.getSenderProfilePicture().length() > 0)
            Picasso.get()
                    .load(imageUrl(object.getSendFrom(), object.getSenderProfilePicture()))
                    .centerCrop()
                    .fit()
                    .error(R.drawable.loading_gif)
                    .into(userIcon);
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
    }

    private static String subStringTime(String dateTime){
        return dateTime.substring(11, dateTime.length() - 3);
    }

    /*-----------------------------------------------------------------------------------END OF CHILD VIEW---------------------------------------------------------*/

    public void clickEffect(View view){
        Animation animation1 = new AlphaAnimation(0.3f, 1.0f);
        animation1.setDuration(500);
        view.startAnimation(animation1);
    }

}
