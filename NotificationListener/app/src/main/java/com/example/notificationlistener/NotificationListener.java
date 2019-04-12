package com.example.notificationlistener;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

import java.util.zip.Inflater;

public class NotificationListener extends NotificationListenerService {
    public static String TAG= NotificationListener.class.getSimpleName();

    public void onNotificationPosted(StatusBarNotification sbn) {

        Intent intent = new Intent("com.example.testnotifiacation.notificationExemple");
        StatusBarParcelable sbp= new StatusBarParcelable(sbn);
        intent.putExtra("statusBar", sbp);
        sendBroadcast(intent);
        final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        this.cancelNotification(sbn.getKey());
        if(appNotifiable(sbp) ) {
            Log.i(TAG, "ID:" + sbn.getId());
            Log.i(TAG, "Posted by:" + sbn.getPackageName());
            Log.i(TAG, "tickerText:" + sbn.getNotification().tickerText);

            for (String key : sbn.getNotification().extras.keySet()) {
                Log.i(TAG, key + "=" + sbn.getNotification().extras.get(key));
            }
            Intent i = new Intent();
            i.setClass(this, NotificationPopUpActivity.class);
            i.putExtra("statusBar", sbp);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            System.out.println("launch the activity");
        }else{
        }

    }
    public boolean appNotifiable(StatusBarParcelable sbp){
        return !sbp.getStatusBarNotificationInfo().get("android.title").equals("Chat heads active") &&
                !sbp.getStatusBarNotificationInfo().get("android.title").equals("Messenger") &&
                !sbp.getStatusBarNotificationInfo().get("android.title").equals("android") &&
                !sbp.getAppNotifier().equals("com.google.android.gms") &&
                !sbp.getAppNotifier().equals("com.android.providers.downloads")&&
                !sbp.getAppNotifier().equals("com.samsung.android.da.daagent")&&
                !sbp.getAppNotifier().equals("com.android.vending");
    }


}
