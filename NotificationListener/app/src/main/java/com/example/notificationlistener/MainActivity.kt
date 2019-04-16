package com.example.notificationlistener

import android.app.Activity
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Window
import android.widget.TextView

class MainActivity : Activity() {
    private val txtView: TextView? = null

    private val isNotificationServiceEnabled: Boolean
        get() {
            val pkgName = packageName
            val str = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
            if (!TextUtils.isEmpty(str)) {
                val names = str.split(":")
                for (i in names.indices) {
                    val cn = ComponentName.unflattenFromString(names[i])
                    if (cn != null) {
                        if (TextUtils.equals(pkgName, cn.packageName)) {
                            return true
                        }
                    }
                }
            }
            return false
        }

    //private StatusbarReceiver sbr;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("onPopUp")
        setContentView(R.layout.activity_main)
        if (!isNotificationServiceEnabled) {

            val alertDialog = buildNotificationServiceAlertDialog()
            alertDialog.show()
        }
        if (!Settings.canDrawOverlays(this)) {
            val alertDialog = OverlayAlertDialog()
            alertDialog.show()
        }
        if (isNotificationServiceEnabled && Settings.canDrawOverlays(this)) {

            val intentFilter = IntentFilter()
            intentFilter.addAction("com.example.testnotifiacation.notificationExemple")
            finish()

        }

        // sbr = new StatusbarReceiver();

    }

    private fun buildNotificationServiceAlertDialog(): AlertDialog {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Knocker")
        alertDialogBuilder.setMessage("vous voulez vous autouriser knocker à acceder a vos notifications")
        alertDialogBuilder.setPositiveButton("yes"
        ) { dialog, id ->
            startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
            val intentFilter = IntentFilter()
            intentFilter.addAction("com.example.testnotifiacation.notificationExemple")
            //registerReceiver(sbr, intentFilter);
            if (isNotificationServiceEnabled) {
            }
        }
        alertDialogBuilder.setNegativeButton("no"
        ) { dialog, id -> }
        return alertDialogBuilder.create()
    }

    private fun OverlayAlertDialog(): AlertDialog {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Knocker")
        alertDialogBuilder.setMessage("vous voulez vous autouriser knocker à afficher certaines notifications sur l'affichage d'autre application")
        alertDialogBuilder.setPositiveButton("yes"
        ) { dialog, id ->
            val intentPermission = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivity(intentPermission)
        }
        alertDialogBuilder.setNegativeButton("no"
        ) { dialog, id -> }
        return alertDialogBuilder.create()
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
