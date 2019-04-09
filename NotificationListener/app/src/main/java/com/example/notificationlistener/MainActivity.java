package com.example.notificationlistener;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private TextView txtview;
    private NotificationReceiver notificationReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtview= findViewById(R.id.txtView);
        if(!isNotificationServiceEnabled()){
            AlertDialog alertDialog = buildNotificationServiceAlertDialog();
            alertDialog.show();
        }
        notificationReceiver = new NotificationReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.example.testnotifiacation.Notification");
        registerReceiver(notificationReceiver,filter);
        System.out.println("go to intent");
        Intent i = new Intent("com.example.testnotifiacation.Notification");
        i.putExtra("command","list");
        System.out.println("send intent");
        sendBroadcast(i);
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver(notificationReceiver);
    }
    private boolean isNotificationServiceEnabled(){
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(),
                "enabled_notification_listeners");
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private AlertDialog buildNotificationServiceAlertDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("NotificationService");
        alertDialogBuilder.setMessage("vous voulez vous autouriser knocker à acceder a vos notifications");
        alertDialogBuilder.setPositiveButton("yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                    }
                });
        alertDialogBuilder.setNegativeButton("no",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // If you choose to not enable the notification listener
                        // the app. will not work as expected
                    }
                });
        return(alertDialogBuilder.create());
    }
    class NotificationReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent){
            String listNotification = intent.getStringExtra("notification_event")+"\n"+txtview.getText();
            txtview.setText(listNotification);
        }
    }
}
