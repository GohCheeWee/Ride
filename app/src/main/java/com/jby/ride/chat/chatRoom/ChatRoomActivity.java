package com.jby.ride.chat.chatRoom;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jby.ride.R;
import com.jby.ride.database.DbChat;
import com.jby.ride.others.SquareHeightLinearLayout;
import com.jby.ride.shareObject.ApiDataObject;
import com.jby.ride.shareObject.ApiManager;
import com.jby.ride.shareObject.AsyncTaskManager;
import com.jby.ride.sharePreference.SharedPreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.jby.ride.chat.ChatActivity.UPDATE_CHAT_LIST;
import static com.jby.ride.others.MyFireBaseMessagingService.NotificationBroadCast;
import static com.jby.ride.others.dialog.RiderProfileDialog.prefix;

public class ChatRoomActivity extends AppCompatActivity implements ExpandableListView.OnGroupCollapseListener, View.OnClickListener,
        TextWatcher {
    //actionbar
    private SquareHeightLinearLayout actionBarMenuIcon, actionBarCloseIcon;
    private TextView actionBarTitle;

    private ExpandableListView chatRoomDialogListView;
    private ArrayList<ChatRoomGroupObject> chatRoomGroupObjectArrayList;
    private ChatRoomAdapter chatRoomAdapter;

    private SquareHeightLinearLayout chatRoomSendButton;
    private ImageView chatRoomSendIcon;
    private EditText chatRoomMessage;
    private ProgressBar chatRoomProgressBar;

    private String roomID;
    private DbChat dbChat;

    //    asyncTask
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;
    Handler handler;
    //tone
    private MediaPlayer mp;

    public ChatRoomActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chat_room);
        objectInitialize();
        objectSetting();
    }

    private void objectInitialize() {
        actionBarMenuIcon = findViewById(R.id.actionbar_menu);
        actionBarCloseIcon =  findViewById(R.id.actionbar_close);
        actionBarTitle = findViewById(R.id.actionBar_title);

        chatRoomDialogListView = findViewById(R.id.chat_room_dialog_list_view);
        chatRoomProgressBar = findViewById(R.id.chat_room_dialog_progress_bar);

        chatRoomMessage = findViewById(R.id.chat_room_dialog_message_field);
        chatRoomSendButton = findViewById(R.id.chat_room_dialog_send_button);
        chatRoomSendIcon = findViewById(R.id.chat_room_dialog_send_button_icon);

        chatRoomGroupObjectArrayList = new ArrayList<>();
        chatRoomAdapter = new ChatRoomAdapter(this, chatRoomGroupObjectArrayList);
        mp = MediaPlayer.create(this, R.raw.tone);
        dbChat = new DbChat(this);
        handler = new Handler();
    }

    private void objectSetting() {
        actionBarMenuIcon.setVisibility(View.GONE);
        actionBarCloseIcon.setVisibility(View.VISIBLE);
        actionBarCloseIcon.setOnClickListener(this);

        chatRoomDialogListView.setAdapter(chatRoomAdapter);
        chatRoomDialogListView.setOnGroupCollapseListener(this);
        chatRoomDialogListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);

        chatRoomMessage.setOnClickListener(this);
        chatRoomMessage.addTextChangedListener(this);
        chatRoomSendButton.setOnClickListener(this);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            roomID = bundle.getString("driver_ride_id");
            String roomTitle = "RIDE" + roomID;
            actionBarTitle.setText(roomTitle);
            //create chat if haven't created
            dbChat.createChat(roomID);
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getAllChat(roomID, false);
            }
        },200);
    }

    private void getAllChat(String roomID, boolean oneMessage){
        try {
            JSONArray jsonArray;
                //fetch all chat
                if(!oneMessage) jsonArray = dbChat.fetchAllChat(roomID).getJSONArray("ChatRoomObject");
                //fetch only the new message added
                else jsonArray = dbChat.fetchNewAddedChat(roomID).getJSONArray("ChatRoomObject");
            for(int i = 0; i< jsonArray.length(); i++){
                int position = -1;
                String date = jsonArray.getJSONObject(i).getString("sendDate");

                for(int j = 0 ; j < chatRoomGroupObjectArrayList.size(); j++){
                    //if found mean that same child record under the same date
                    if (chatRoomGroupObjectArrayList.get(j).getSendDate().equals(dateSubString(date))){
                        position = j;
                        break;
                    }
                }
                //mean this date is never added yet so create a new group view
                if(position == -1){
                    chatRoomGroupObjectArrayList.add(new ChatRoomGroupObject(dateSubString(date), setChildArray(jsonArray.getJSONObject(i))));
                }
                // if the same date(group view) is added then add the child item
                else
                    chatRoomGroupObjectArrayList.get(position).getChatRoomChildObjectArrayList().add(setChildObject(jsonArray.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        chatRoomAdapter.notifyDataSetChanged();
        setUpView();
    }

    private ChatRoomChildObject setChildObject(JSONObject jsonObject){
        ChatRoomChildObject object = null;
        try {
            object = new ChatRoomChildObject(
                    jsonObject.getString("id"),
                    jsonObject.getString("senderID"),
                    jsonObject.getString("senderName"),
                    jsonObject.getString("senderProfilePicture"),
                    jsonObject.getString("message"),
                    jsonObject.getString("status"),
                    jsonObject.getString("sendFrom"),
                    jsonObject.getString("sendDate")
            );

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }

    private ArrayList<ChatRoomChildObject> setChildArray(JSONObject jsonObject){
        ArrayList<ChatRoomChildObject> chatRoomChildObjectArrayList = new ArrayList<>();
        try {
            chatRoomChildObjectArrayList.add(new ChatRoomChildObject(
                    jsonObject.getString("id"),
                    jsonObject.getString("senderID"),
                    jsonObject.getString("senderName"),
                    jsonObject.getString("senderProfilePicture"),
                    jsonObject.getString("message"),
                    jsonObject.getString("status"),
                    jsonObject.getString("sendFrom"),
                    jsonObject.getString("sendDate")
            ));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return chatRoomChildObjectArrayList;
    }

    private String dateSubString(String date){
        return date.substring(0 , 10);
    }

    @Override
    public void onGroupCollapse(int i) {
        chatRoomDialogListView.expandGroup(i);
    }

    private void setUpView() {
        if(chatRoomGroupObjectArrayList.size() > 0){
            for(int i = 0; i < chatRoomGroupObjectArrayList.size(); i++){
                chatRoomDialogListView.expandGroup(i);
            }
        }
        scrollToDown();
        chatRoomProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.actionbar_close:
                finish();
                break;
            case R.id.chat_room_dialog_send_button:
                sendChat();
                break;
            case R.id.chat_room_dialog_message_field:
                scrollToDown();
                break;
        }
    }

    private void scrollToDown(){
        int size = chatRoomGroupObjectArrayList.size();
        if(size > 0)
        chatRoomDialogListView.setSelection(chatRoomAdapter.getChildrenCount(size-1) - 1);
    }
    /*-----------------------------------------------------------------send mesage-------------------------------------------------------------*/
    public void sendChat(){
        final String message = chatRoomMessage.getText().toString();
        String userID = SharedPreferenceManager.getUserID(this);
        String profilePicture = SharedPreferenceManager.getUserProfilePic(this);
        String timeStamp = String.valueOf(android.text.format.DateFormat.format("yyyy-MM-dd HH:mm:ss", new java.util.Date()));

        long storeChat = dbChat.storeChatDetail(roomID, message, "You", userID, profilePicture,  "text", timeStamp, "rider", timeStamp, "1");
        if(storeChat != -1){
            getAllChat(roomID, true);
            chatRoomMessage.setText("");
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    sendToReceiver(message);
                }
            },200);
        }
    }

    private void sendToReceiver(String message){
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("driver_ride_id", roomID));
        apiDataObjectArrayList.add(new ApiDataObject("user_id", SharedPreferenceManager.getUserID(this)));
        apiDataObjectArrayList.add(new ApiDataObject("message", message));
        apiDataObjectArrayList.add(new ApiDataObject("message_type", "text"));

        asyncTaskManager = new AsyncTaskManager(
                this,
                new ApiManager().chat,
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
                        Log.d("","");
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
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        if(editable.toString().trim().equals("")){
            chatRoomSendIcon.setImageDrawable(getResources().getDrawable(R.drawable.chat_room_dialog_disable_send_icon));
            chatRoomSendButton.setEnabled(false);
        }
        else{
            chatRoomSendIcon.setImageDrawable(getResources().getDrawable(R.drawable.activity_chat_send_icon));
            chatRoomSendButton.setEnabled(true);
        }
    }
    /*---------------------------------------------------------------broadcast-----------------------------------------*/

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    @Override
    public void onStart() {
        super.onStart();
//        getParent().getWindow().setWindowAnimations(R.style.dialog_up_down);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(NotificationBroadCast));
    }


    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String id = intent.getStringExtra("id");
            switch(id){
                case "9":
                    addNewChat(intent.getStringExtra("message_content"));
                    break;
            }
        }
    };

    private void addNewChat(String message_content){
        try {
            JSONArray jsonArray = new JSONArray(message_content);
            for(int i = 0; i< jsonArray.length(); i++){
                int position = -1;
                String date = jsonArray.getJSONObject(i).getString("sendDate");

                for(int j = 0 ; j < chatRoomGroupObjectArrayList.size(); j++){
                    //if found mean that same child record under the same date
                    if (chatRoomGroupObjectArrayList.get(j).getSendDate().equals(dateSubString(date))){
                        position = j;
                        break;
                    }
                }
                //mean this date is never added yet so create a new group view
                if(position == -1){
                    chatRoomGroupObjectArrayList.add(new ChatRoomGroupObject(dateSubString(date), setChildArray(jsonArray.getJSONObject(i))));
                }
                // if the same date(group view) is added then add the child item
                else
                    chatRoomGroupObjectArrayList.get(position).getChatRoomChildObjectArrayList().add(setChildObject(jsonArray.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        chatRoomAdapter.notifyDataSetChanged();
        //if first received the message then expand the group
        if(chatRoomGroupObjectArrayList.size() == 1)
            chatRoomDialogListView.expandGroup(0);
        //play tone
        mp.start();
    }
    /*-------------------------------------------------------------------end of broadcast-----------------------------*/

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(UPDATE_CHAT_LIST);
    }
}