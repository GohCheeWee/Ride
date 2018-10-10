package com.jby.ride.others;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.jby.ride.R;
import com.jby.ride.home.MainActivity;
import com.jby.ride.profile.ProfileActivity;
import com.jby.ride.ride.RideActivity;
import com.jby.ride.ride.comfirm.startRoute.StartRouteActivity;
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
            Log.d("haha", "HAHA: " + json);
            Intent activityIntent = null;
            String matchRideId, driverRideID;
            String message = null;
            Bundle bundle = new Bundle();
            boolean blankMessage = false;
            String title = data.getString("title");
            String id = data.getJSONArray("content").getJSONObject(0).getString("id");


            switch (id){
                case "1":
                    // driver on the way notification
                    driverRideID = data.getJSONArray("content").getJSONObject(0).getString("driver_ride_id");
                     message = data.getJSONArray("content").getJSONObject(0).getString("message");
                    activityIntent = new Intent(getApplicationContext(), MainActivity.class);
                    bundle.putString("driver_ride_id", driverRideID);

                    sendBroadCastToActivityWhenDriverIsComing(driverRideID);
                    break;
                case "2":
                    // driver arrived
                    matchRideId = data.getJSONArray("content").getJSONObject(0).getString("match_ride_id");
                    message = data.getJSONArray("content").getJSONObject(0).getString("message");
                    activityIntent = new Intent(getApplicationContext(), StartRouteActivity.class);
                    bundle.putString("match_ride_id", matchRideId);

                    sendBroadCastToActivityWhenDriverIsArrived(id);
                    break;
                case "3":
                    // finished journey
                    matchRideId = data.getJSONArray("content").getJSONObject(0).getString("match_ride_id");
                    message = data.getJSONArray("content").getJSONObject(0).getString("message");
                    activityIntent = new Intent(getApplicationContext(), RideActivity.class);

                    sendBroadCastToActivityWhenFinishedRide(matchRideId);
                    break;
                case "4":
                    // driver found
                    matchRideId = data.getJSONArray("content").getJSONObject(0).getString("match_ride_id");
                    message = data.getJSONArray("content").getJSONObject(0).getString("message");
                    //store match ride id avoid broadcast failed
                    SharedPreferenceManager.setMatchRideID(getApplicationContext(), matchRideId);
                    //notification action
                    activityIntent = new Intent(getApplicationContext(), MainActivity.class);
                    bundle.putString("match_ride_id", matchRideId);

                    sendBroadCastToActivity(matchRideId);
                    break;
                case "5":
                    // pick up
                    matchRideId = data.getJSONArray("content").getJSONObject(0).getString("match_ride_id");
                    message = data.getJSONArray("content").getJSONObject(0).getString("message");
                    activityIntent = new Intent(getApplicationContext(), StartRouteActivity.class);
                    bundle.putString("match_ride_id", matchRideId);
                    //rider is successfully pick up
                    sendBroadCastToActivityWhenDriverIsPickUp(id);
                    break;
                    default:
                        blankMessage = true;
                        break;
            }

            if(id.equals("6")){
                String longitude = data.getJSONArray("content").getJSONObject(0).getString("longitude");
                String latitude = data.getJSONArray("content").getJSONObject(0).getString("latitude");
                sendBroadCastToActivityWhenDriverLocationChange(longitude, latitude);
            }

            if(!blankMessage){
                lightOutScreen();
                //            notification channel
                activityIntent.putExtras(bundle);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    NotificationChannelHelper notificationChannelHelper = new NotificationChannelHelper(getApplicationContext());
                    NotificationCompat.Builder builder = notificationChannelHelper.getChannel1Notification(title, message);
                    notificationChannelHelper.getManager().notify(1, builder.build());

                }else{
                    //            notification < android 8.0
                    CustomNotificationManager mNotificationManager = new CustomNotificationManager(getApplicationContext());
                    mNotificationManager.showSmallNotification(title, message, activityIntent);
                }
            }


        } catch (JSONException e) {
            Log.e(TAG, "Json Exception: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

    //trip complete
    private void sendBroadCastToActivityWhenFinishedRide(String matchRideId){
        Intent intent = new Intent(NotificationBroadCast);
        intent.putExtra("match_ride_id", matchRideId);
        intent.putExtra("id", "3");
        broadcaster.sendBroadcast(intent);
    }

    //driver found purpose
    private void sendBroadCastToActivity(String matchRideId){
        Intent intent = new Intent(NotificationBroadCast);
        intent.putExtra("match_ride_id", matchRideId);
        intent.putExtra("id", "4");
        broadcaster.sendBroadcast(intent);
    }

    //driver otw
    private void sendBroadCastToActivityWhenDriverIsComing(String driverRideID){
        Intent intent = new Intent(NotificationBroadCast);
        intent.putExtra("driver_ride_id", driverRideID);
        intent.putExtra("id", "5");
        broadcaster.sendBroadcast(intent);
    }

    //driver arrived
    private void sendBroadCastToActivityWhenDriverIsArrived(String matchRideId){
        Intent intent = new Intent(NotificationBroadCast);
        intent.putExtra("match_ride_id", matchRideId);
        intent.putExtra("id", "6");
        broadcaster.sendBroadcast(intent);
    }

    //driver pick up
    private void sendBroadCastToActivityWhenDriverIsPickUp(String matchRideId){
        Intent intent = new Intent(NotificationBroadCast);
        intent.putExtra("match_ride_id", matchRideId);
        intent.putExtra("id", "7");
        broadcaster.sendBroadcast(intent);
    }

    //driver pick up
    private void sendBroadCastToActivityWhenDriverLocationChange(String longitude, String latitude){
        Intent intent = new Intent(NotificationBroadCast);
        intent.putExtra("longitude", longitude);
        intent.putExtra("latitude", latitude);
        intent.putExtra("id", "8");
        broadcaster.sendBroadcast(intent);
    }


    private void lightOutScreen(){
        PowerManager pm = (PowerManager)getApplication().getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = false;
        if (pm != null) {
            isScreenOn = pm.isInteractive();
        }
        if(!isScreenOn)
        {
            PowerManager.WakeLock wl = null;
            if (pm != null) {
                wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.ON_AFTER_RELEASE,
                        getApplication().getResources().getString(R.string.app_name));
                wl.acquire(10000);
                PowerManager.WakeLock wl_cpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,getApplication().getResources().getString(R.string.app_name));
                wl_cpu.acquire(10000);
            }
        }
    }

}
