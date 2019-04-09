package com.example.notificationlistener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

public class NotificationListener extends NotificationListenerService {
    public static String TAG= NotificationListener.class.getSimpleName();
    private NotifReceiver notifier;
    public void onCreate(){
        super.onCreate();
        System.out.println("test ");
        notifier= new NotifReceiver();
        IntentFilter filter= new IntentFilter();
        filter.addAction("com.example.testnotifiacation.NOTIFICATION_LISTENER_SERVICE");
        registerReceiver(notifier,filter);
    }
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.i(TAG, "ID:" + sbn.getId());
        Log.i(TAG, "Posted by:" + sbn.getPackageName());
        Log.i(TAG, "tickerText:" + sbn.getNotification().tickerText);

        for (String key : sbn.getNotification().extras.keySet()) {
            Log.i(TAG, key + "=" + sbn.getNotification().toString());
        }
    }
    public class NotifReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {

            System.out.println("test ");
            if(intent.getStringExtra("command").equals("list")){
                Intent i1 = new  Intent("com.example.testnotifiacation.NOTIFICATION_LISTENER_EXAMPLE");
                i1.putExtra("notification_event","=====================");
                sendBroadcast(i1);
                int i=1;
                System.out.println("nombre de notification"+ NotificationListener.this.getActiveNotifications());
                for (StatusBarNotification sbn : NotificationListener.this.getActiveNotifications()) {
                    Intent i2 = new  Intent("com.example.testnotifiacation.NOTIFICATION_LISTENER_EXAMPLE");
                    i2.putExtra("notification_event",i +" " + sbn.getPackageName() + "n");
                    sendBroadcast(i2);
                    i++;
                }
                Intent i3 = new  Intent("com.example.testnotifiacation.NOTIFICATION_LISTENER_EXAMPLE");
                i3.putExtra("notification_event","===== Notification List ====");
                sendBroadcast(i3);

            }

        }
    }

}
