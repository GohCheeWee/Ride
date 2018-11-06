package com.jby.ride.chat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.jby.ride.R;
import com.jby.ride.chat.chatRoom.ChatRoomActivity;
import com.jby.ride.database.DbChat;
import com.jby.ride.others.CustomListView;
import com.jby.ride.others.SquareHeightLinearLayout;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Objects;

import static com.jby.ride.others.MyFireBaseMessagingService.NotificationBroadCast;

public class ChatActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener,
        AdapterView.OnItemClickListener{
    //actionbar
    private SquareHeightLinearLayout actionBarMenuIcon, actionBarCloseIcon;
    private TextView actionBarTitle;

    SwipeRefreshLayout chatActivitySwipeLayout;
    CustomListView chatActivityListView;
    View chatActivityListViewFooter;
    ChatAdapter chatActivityAdapter;

    RelativeLayout chatActivityNotFoundLayout;
    ProgressBar chatActivityProgressBar;
    ArrayList<ChatObject> chatActivityObjectArrayList;
    private Handler handler;
    private DbChat dbChat;
    //for refresh purpose
    public static int UPDATE_CHAT_LIST = 305;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        objectInitialize();
        objectSetting();
    }

    private void objectInitialize() {
        actionBarMenuIcon = findViewById(R.id.actionbar_menu);
        actionBarCloseIcon = findViewById(R.id.actionbar_close);
        actionBarTitle = findViewById(R.id.actionBar_title);

        chatActivitySwipeLayout = findViewById(R.id.activity_chat_swipe_layout);
        chatActivityListView = findViewById(R.id.activity_chat_list_view);
        chatActivityListViewFooter = ((LayoutInflater) Objects.requireNonNull(getSystemService(Context.LAYOUT_INFLATER_SERVICE))).inflate(R.layout.list_view_footer, null, false);
        chatActivityNotFoundLayout = findViewById(R.id.activity_chat_not_found_layout);
        chatActivityProgressBar = findViewById(R.id.activity_chat_progress_bar);

        chatActivityObjectArrayList = new ArrayList<>();
        chatActivityAdapter = new ChatAdapter(this, chatActivityObjectArrayList);

        dbChat = new DbChat(this);
        handler = new Handler();
    }

    private void objectSetting() {
        actionBarMenuIcon.setVisibility(View.GONE);
        actionBarCloseIcon.setVisibility(View.VISIBLE);
        actionBarCloseIcon.setOnClickListener(this);
        actionBarTitle.setText(R.string.activity_chat_title);

        chatActivityListView.setAdapter(chatActivityAdapter);
        chatActivitySwipeLayout.setOnRefreshListener(this);
        chatActivitySwipeLayout.setColorSchemeColors(getResources().getColor(R.color.red));

        chatActivityListView.setOnItemClickListener(this);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fetchAllChat();
            }
        },200);

    }
    public void fetchAllChat(){
        try {
            JSONArray jsonArray = dbChat.fetchAllChatRoom().getJSONArray("ChatObject");
            for(int i = 0 ; i < jsonArray.length(); i++){
                chatActivityObjectArrayList.add(new ChatObject(
                        jsonArray.getJSONObject(i).getString("id"),
                        jsonArray.getJSONObject(i).getString("message"),
                        jsonArray.getJSONObject(i).getString("username"),
                        jsonArray.getJSONObject(i).getString("sendDate"),
                        jsonArray.getJSONObject(i).getString("profilePicture"),
                        jsonArray.getJSONObject(i).getString("status"),
                        jsonArray.getJSONObject(i).getString("sendFrom")
                ));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        chatActivityAdapter.notifyDataSetChanged();
        setUpView();
    }

    private void setUpView() {
        chatActivityProgressBar.setVisibility(View.GONE);

        if(chatActivityObjectArrayList.size() > 0)
        {
            chatActivityNotFoundLayout.setVisibility(View.GONE);
            chatActivityListView.setVisibility(View.VISIBLE);
        }
        else{
            chatActivityNotFoundLayout.setVisibility(View.VISIBLE);
            chatActivityListView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRefresh() {
        chatActivityObjectArrayList.clear();
        fetchAllChat();
        chatActivityAdapter.notifyDataSetChanged();
        chatActivitySwipeLayout.setRefreshing(false);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.actionbar_close:
                finish();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        String driverRideId = chatActivityObjectArrayList.get(i).getId();

        if (dbChat.updateChatStatusWhenClick(driverRideId)) {
            Intent intent = new Intent(this, ChatRoomActivity.class);
            Bundle bundle = new Bundle();

            bundle.putString("driver_ride_id", driverRideId);
            intent.putExtras(bundle);
            startActivityForResult(intent, UPDATE_CHAT_LIST);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(NotificationBroadCast));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
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
            chatActivityObjectArrayList.add(0, new ChatObject(
                    jsonArray.getJSONObject(0).getString("driver_ride_id"),
                    jsonArray.getJSONObject(0).getString("message"),
                    jsonArray.getJSONObject(0).getString("senderName"),
                    jsonArray.getJSONObject(0).getString("sendDate"),
                    jsonArray.getJSONObject(0).getString("senderProfilePicture"),
                    jsonArray.getJSONObject(0).getString("status"),
                    jsonArray.getJSONObject(0).getString("sendFrom")
            ));
            chatActivityAdapter.notifyDataSetChanged();
            setUpView();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == UPDATE_CHAT_LIST)
            onRefresh();
    }
}
