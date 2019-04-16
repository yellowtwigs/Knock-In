package com.example.notificationlistener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;

public class MainActivity extends Activity {
    private TextView txtView;
    //private StatusbarReceiver sbr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
       if(!isNotificationServiceEnabled()) {

           AlertDialog alertDialog = buildNotificationServiceAlertDialog();
           alertDialog.show();
       }
        if(!Settings.canDrawOverlays(this)){
            AlertDialog alertDialog = OverlayAlertDialog();
            alertDialog.show();
        }
        if(isNotificationServiceEnabled() && Settings.canDrawOverlays(this)){

           IntentFilter intentFilter = new IntentFilter();
           intentFilter.addAction("com.example.testnotifiacation.notificationExemple");
           finish();

       }

          // sbr = new StatusbarReceiver();

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
        alertDialogBuilder.setTitle("Knocker");
        alertDialogBuilder.setMessage("vous voulez vous autouriser knocker à acceder a vos notifications");
        alertDialogBuilder.setPositiveButton("yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                        IntentFilter intentFilter = new IntentFilter();
                        intentFilter.addAction("com.example.testnotifiacation.notificationExemple");
                        //registerReceiver(sbr, intentFilter);
                        if(isNotificationServiceEnabled()) {
                        }

                    }
                });
        alertDialogBuilder.setNegativeButton("no",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        return(alertDialogBuilder.create());
    }
    private AlertDialog OverlayAlertDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Knocker");
        alertDialogBuilder.setMessage("vous voulez vous autouriser knocker à afficher certaines notifications sur l'affichage d'autre application");
        alertDialogBuilder.setPositiveButton("yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intentPermission =new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"+getPackageName()));
                        startActivity(intentPermission);
                    }
                });
        alertDialogBuilder.setNegativeButton("no",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        return(alertDialogBuilder.create());
    }
   /* public class StatusbarReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context,Intent intent){

            StatusBarParcelable sbp= intent.getParcelableExtra("statusBar");
            String textNotif = "notification id: "+sbp.getId()+ " envoyé par "+sbp.getAppNotifier()+" l'expediteur du message est "+sbp.getStatusBarNotificationInfo().get("android.title")+" voici le contenu de ce message" + sbp.getStatusBarNotificationInfo().get("android.text")+"\n" +txtView.getText();
            txtView.setText(textNotif);
    }
    }*/
}
