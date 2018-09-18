package com.jby.ride.others;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;

import com.jby.ride.R;

public class NotificationChannelHelper extends ContextWrapper {
    public static String NotificationChannel = "Ride";
    public static String NotificationName = "Channel 1";
    private NotificationManager notificationManager;
    private Context context;

    public NotificationChannelHelper(Context base) {
        super(base);
        context = base;
        createChannel1();
    }

    public void createChannel1(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NotificationChannel, NotificationName, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.enableVibration(true);
            notificationChannel.setLightColor(R.color.red);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            getManager().createNotificationChannel(notificationChannel);
        }
    }

    public NotificationManager getManager(){
        if(notificationManager == null)
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        return notificationManager;
    }

    public NotificationCompat.Builder getChannel1Notification(String title, String message){
        long[] pattern = {500,500,500,500,500};

        return new NotificationCompat.Builder(getApplicationContext(), NotificationChannel)
                .setSmallIcon(R.mipmap.ic_launcher).setTicker(title).setWhen(0)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setContentText(message)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setShowWhen(true)
                .setPriority(Notification.PRIORITY_MAX)
                .setVibrate(pattern)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
    }
}
