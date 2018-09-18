package com.jby.ride.others;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.jby.ride.home.MainActivity;
import com.jby.ride.profile.ProfileActivity;
import com.jby.ride.ride.RideActivity;
import com.jby.ride.sharePreference.SharedPreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import static com.jby.ride.home.MainActivity.TAG;

public class MyFireBaseMessagingService extends FirebaseMessagingService {
    public static String NotificationBroadCast = "notificationBroadCast";
    private LocalBroadcastManager broadcaster;

    @Override
    public void onCreate() {
        broadcaster = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (remoteMessage.getData().size() > 0) {
            Log.e(TAG, "Data Payload: " + remoteMessage.getData().toString());
            try {
                JSONObject json = new JSONObject(remoteMessage.getData().toString());
                sendPushNotification(json);

            } catch (Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage());
            }
        }
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);

    }

    private void sendPushNotification(JSONObject json) {
        try {
            JSONObject data = json.getJSONObject("data");
            String title = data.getString("title");
            String message = data.getString("message");
            String imageUrl = data.getString("image");
            String matchRideId = data.getString("match_ride_id");
            Intent activityIntent;
            Bundle bundle = new Bundle();


            if(message.equals("Your ride have been accepted!")){
                SharedPreferenceManager.setMatchRideID(getApplicationContext(), matchRideId);
                sendBroadCastToActivity(matchRideId);

                activityIntent = new Intent(getApplicationContext(), MainActivity.class);
                bundle.putString("match_ride_id", matchRideId);
            }
            else if(message.equals("Yay! You have finished your journey~")){
                sendBroadCastToActivityWhenFinishedRide();
                activityIntent = new Intent(getApplicationContext(), RideActivity.class);

            }
            else if(message.equals("Your driver is on the way")){
                activityIntent = new Intent(getApplicationContext(), RideActivity.class);
            }
            else{
                activityIntent = new Intent(getApplicationContext(), MainActivity.class);
            }
            activityIntent.putExtras(bundle);


            //            notification channel
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationChannelHelper notificationChannelHelper = new NotificationChannelHelper(getApplicationContext());
                NotificationCompat.Builder builder = notificationChannelHelper.getChannel1Notification(title, message);
                notificationChannelHelper.getManager().notify(1, builder.build());

            }else{
                //            notification
                CustomNotificationManager mNotificationManager = new CustomNotificationManager(getApplicationContext());
                if(imageUrl.equals("null")){
                    mNotificationManager.showSmallNotification(title, message, activityIntent);
                }else{
                    mNotificationManager.showBigNotification(title, message, imageUrl, activityIntent);
                }
            }

        } catch (JSONException e) {
            Log.e(TAG, "Json Exception: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

    private void sendBroadCastToActivity(String matchRideId){
        Intent intent = new Intent(NotificationBroadCast);
        intent.putExtra("match_ride_id", matchRideId);
        broadcaster.sendBroadcast(intent);
    }

    private void sendBroadCastToActivityWhenFinishedRide(){
        Intent intent = new Intent(NotificationBroadCast);
//        intent.putExtra("match_ride_id", matchRideId);
        broadcaster.sendBroadcast(intent);
    }

}
