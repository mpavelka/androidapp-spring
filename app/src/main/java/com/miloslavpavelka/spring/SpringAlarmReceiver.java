package com.miloslavpavelka.spring;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

/**
 * Created by mpavelka on 02/07/2018.
 */


public class SpringAlarmReceiver extends BroadcastReceiver {
    public static final String TAG = "SpringAlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Received a broadcast");
        showNotification(context);
    }

    private void showNotification(Context context) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setLights(Color.BLUE, 500, 500)
                .setVibrate(new long[]{500,500,500,500,500,500,500,500,500})
                .setSound(Uri.parse("android.resource://"+context.getPackageName()+"/"+R.raw.pourwater))
                .setSmallIcon(R.drawable.h2o_smile)
                .setContentTitle("Go get a glass of water!")
                .setContentText("Your water deficit today is more than 250ml.")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Your water deficit today is more than 250ml. You should go and get some water!"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(1, mBuilder.build());
    }
}
