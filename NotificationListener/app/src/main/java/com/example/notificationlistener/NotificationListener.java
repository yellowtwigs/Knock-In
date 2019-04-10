package com.example.notificationlistener;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

public class NotificationListener extends NotificationListenerService {
    public static String TAG= NotificationListener.class.getSimpleName();

    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.i(TAG, "ID:" + sbn.getId());
        Log.i(TAG, "Posted by:" + sbn.getPackageName());
        Log.i(TAG, "tickerText:" + sbn.getNotification().tickerText);

        for (String key : sbn.getNotification().extras.keySet()) {
            Log.i(TAG, key + "=" + sbn.getNotification().extras.get(key));
        }
        Intent intent = new Intent("com.example.testnotifiacation.notificationExemple");
        StatusBarParcelable sbp= new StatusBarParcelable(sbn);
        intent.putExtra("statusBar", sbp);
        sendBroadcast(intent);
        final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        this.cancelNotification(sbn.getKey());
    }


}
