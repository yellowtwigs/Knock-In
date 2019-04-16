package com.example.notificationlistener;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.zip.Inflater;

public class NotificationListener extends NotificationListenerService {
    public static String TAG= NotificationListener.class.getSimpleName();
    public static final String FACEBOOK_PACKAGE= "com.facebook.katana";
    public static final String MESSENGER_PACKAGE = "com.facebook.orca";
    public static final String WATHSAPP_SERVICE = "com.whatsapp";
    public static final String GMAIL_PACKAGE ="com.google.android.gm";
    public static final String MESSAGE_PACKAGE="";
    public void onNotificationPosted(StatusBarNotification sbn) {

        Intent intent = new Intent("com.example.testnotifiacation.notificationExemple");
        StatusBarParcelable sbp= new StatusBarParcelable(sbn);
        intent.putExtra("statusBar", sbp);
        sendBroadcast(intent);
        final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        this.cancelNotification(sbn.getKey());
        if(appNotifiable(sbp) ) {
            Log.i(TAG, "application notifier:" + sbn.getPackageName());
            Log.i(TAG, "tickerText:" + sbn.getNotification().tickerText);

            for (String key : sbn.getNotification().extras.keySet()) {
                Log.i(TAG, key + "=" + sbn.getNotification().extras.get(key));
            }

            if(Settings.canDrawOverlays(this)) {

                WindowManager.LayoutParams parameters = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                        PixelFormat.TRANSLUCENT);
                parameters.gravity = Gravity.RIGHT | Gravity.TOP;
                parameters.flags=WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
                WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                /*View popupView = inflater.inflate(R.layout.activity_notification_pop_up, null);
                notifLayout(sbp, popupView);

                windowManager.addView(popupView, parameters);*/
            }
        }

    }

   /* private void notifLayout(StatusBarParcelable sbp, final View view) {
        TextView expediteur =view.findViewById(R.id.expediteur2);
        TextView message=(TextView) view.findViewById(R.id.txtView2);
        expediteur.setText(sbp.getStatusBarNotificationInfo().get("android.title")+"");
        message.setText(sbp.getStatusBarNotificationInfo().get("android.text")+"");
        ConstraintLayout layout= (ConstraintLayout) view.findViewById(R.id.constraintLayout);
        ImageView imgPlat = (ImageView) view.findViewById(R.id.imageView3);
        imgPlat.setImageResource(getApplicationNotifier(sbp));
        layout.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        System.exit(0);
                    }
                }
        );
    }*/


    public boolean appNotifiable(StatusBarParcelable sbp){
        return !sbp.getStatusBarNotificationInfo().get("android.title").equals("Chat heads active") &&
                !sbp.getStatusBarNotificationInfo().get("android.title").equals("Messenger") &&
                !sbp.getStatusBarNotificationInfo().get("android.title").equals("android") &&
                !sbp.getAppNotifier().equals("com.google.android.gms") &&
                !sbp.getAppNotifier().equals("com.android.providers.downloads")&&
                !sbp.getAppNotifier().equals("com.samsung.android.da.daagent")&&
                !sbp.getAppNotifier().equals("com.android.vending") &&
                !sbp.getStatusBarNotificationInfo().get("android.text").equals(null) &&
                !sbp.getAppNotifier().equals("android");
    }

    private int getApplicationNotifier(StatusBarParcelable sbp) {

        if(sbp.getAppNotifier().equals(FACEBOOK_PACKAGE)|| sbp.getAppNotifier().equals(MESSENGER_PACKAGE)){
            return R.drawable.facebook;
        }else if(sbp.getAppNotifier().equals(GMAIL_PACKAGE)){
            return R.drawable.gmail;
        }else if(sbp.getAppNotifier().equals(WATHSAPP_SERVICE)){
            return R.drawable.download;
        }
        return R.drawable.sms;
    }
}
