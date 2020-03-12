package com.yellowtwigs.knockin.controller.activity.firstLaunch

import android.Manifest
import android.annotation.SuppressLint
import android.content.*
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.app.ActivityCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.model.ContactManager
import com.yellowtwigs.knockin.model.DbWorkerThread
import com.yellowtwigs.knockin.model.ModelDB.ContactDB
import com.yellowtwigs.knockin.model.ModelDB.ContactDetailDB
import kotlinx.android.synthetic.main.activity_start_activity.*
import android.content.ComponentName
import com.yellowtwigs.knockin.controller.activity.MainActivity


/**
 * Activité qui nous permet d'importer nos contacts et accepter toutes les autorisations liées aux notifications appel et message
 * @author Florian Striebel, Kenzy Suon
 */
class StartActivity : AppCompatActivity() {

    //region ========================================== Val or Var ==========================================

    private var start_activity_ImportContacts: MaterialButton? = null
    private var start_activity_ActivateNotifications: MaterialButton? = null
    private var start_activity_AuthorizeSuperposition: MaterialButton? = null
    private var start_activity_Permissions: MaterialButton? = null

    private var start_activity_Next: MaterialButton? = null
    private var start_activity_Skip: MaterialButton? = null
    private var start_activity_ImportContactsLoading: ProgressBar? = null
    private var start_activity_ActivateNotificationsLoading: ProgressBar? = null
    private var start_activity_AuthorizeSuperpositionLoading: ProgressBar? = null
    private var start_activity_PermissionsLoading: ProgressBar? = null

    private var start_activity_ImportContactsCheck: AppCompatImageView? = null
    private var start_activity_ActivateNotificationsCheck: AppCompatImageView? = null
    private var start_activity_AuthorizeSuperpositionCheck: AppCompatImageView? = null
    private var start_activity_PermissionsCheck: AppCompatImageView? = null

    private lateinit var start_activity_mDbWorkerThread: DbWorkerThread
    private var activityVisible = false

    //endregion

    @SuppressLint("ObsoleteSdkInt")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val height = size.y
        // Nous permet d'avoir un écran adapté a différente taille d'écran
        when {
            height > 2500 -> setContentView(R.layout.activity_start_activity_bigger)
            height in 2000..2499 -> setContentView(R.layout.activity_start_activity)
            height in 1200..2101 -> setContentView(R.layout.activity_start_activity)
            height < 1200 -> setContentView(R.layout.activity_start_activity)
        }

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT

        val listApp = getAppOnPhone()

        //region ========================================== FindViewById ==========================================

        start_activity_ImportContacts = findViewById(R.id.start_activity_import_contacts_button)
        start_activity_ActivateNotifications = findViewById(R.id.start_activity_activate_notifications_button)
        start_activity_AuthorizeSuperposition = findViewById(R.id.start_activity_superposition_button)
        start_activity_Permissions = findViewById(R.id.start_activity_permissions_button)

        start_activity_Next = findViewById(R.id.start_activity_next)
        start_activity_Skip = findViewById(R.id.start_activity_skip)
        start_activity_ImportContactsLoading = findViewById(R.id.start_activity_import_contacts_loading)
        start_activity_ActivateNotificationsLoading = findViewById(R.id.start_activity_activate_notifications_loading)
        start_activity_AuthorizeSuperpositionLoading = findViewById(R.id.start_activity_superposition_loading)
        start_activity_PermissionsLoading = findViewById(R.id.start_activity_permissions_loading)

        start_activity_ImportContactsCheck = findViewById(R.id.start_activity_import_contacts_check)
        start_activity_ActivateNotificationsCheck = findViewById(R.id.start_activity_activate_notifications_check)
        start_activity_AuthorizeSuperpositionCheck = findViewById(R.id.start_activity_superposition_check)
        start_activity_PermissionsCheck = findViewById(R.id.start_activity_permissions_check)

        //endregion

        //region ========================================== WorkerThread ==========================================

        start_activity_mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        start_activity_mDbWorkerThread.start()

        //endregion

        //region ========================================== Listeners ==========================================

        //Lors du click sur synchroniser alors nous demandons l'autorisation d'accéder aux contact puis nous affichons un loading
        start_activity_ImportContacts!!.setOnClickListener {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), ImportContactsActivity.REQUEST_CODE_READ_CONTACT)
            start_activity_ImportContacts!!.visibility = View.INVISIBLE

            val displayLoading = Runnable {
                start_activity_ImportContactsLoading!!.visibility = View.VISIBLE
            }
            runOnUiThread(displayLoading)

            if (listApp.contains("com.whatsapp")) {
                val importWhatsappSharedPreferences: SharedPreferences = getSharedPreferences("importWhatsappPreferences", Context.MODE_PRIVATE)
                val edit: SharedPreferences.Editor = importWhatsappSharedPreferences.edit()
                edit.putBoolean("importWhatsappPreferences", true)
                edit.apply()
            }
        }

        //Lors du click sur activateNotification nous demandont l'autorisation d'accès au notification
        start_activity_ActivateNotifications!!.setOnClickListener {
            activateNotificationsClick()
            start_activity_ActivateNotifications!!.visibility = View.INVISIBLE
            start_activity_ActivateNotificationsLoading!!.visibility = View.VISIBLE

            val SPLASH_DISPLAY_LENGHT = 2000

            val displayLoading = Runnable {
                start_activity_ActivateNotificationsLoading!!.visibility = View.VISIBLE
            }
            runOnUiThread(displayLoading)
            //Ici nous créons un thread qui vérifie en boucle si nous sommes revenu dans Knockin une fois revenu alors il affiche l'image de validation(Image_validate) ou le bouton demandant d'autoriser
            val verifiedNotification = Thread {
                activityVisible = false
                while (!activityVisible) {
                }
                if (isNotificationServiceEnabled()) {
                    //     Handler().postDelayed({

                    val displayLoading = Runnable {
                        //start_activity_ActivateNotificationsLoading!!.visibility = View.INVISIBLE
                        //start_activity_ActivateNotificationsCheck!!.visibility = View.VISIBLE
                        Handler().postDelayed({
                            start_activity_ActivateNotificationsLoading!!.visibility = View.INVISIBLE
                            start_activity_ActivateNotificationsCheck!!.visibility = View.VISIBLE
                            val sharedPreferences: SharedPreferences = getSharedPreferences("Knockin_preferences", Context.MODE_PRIVATE)
                            val edit: SharedPreferences.Editor = sharedPreferences.edit()
                            edit.putBoolean("serviceNotif", true)
                            edit.apply()
                            allIsChecked()
                        }, SPLASH_DISPLAY_LENGHT.toLong())
                    }
                    runOnUiThread(displayLoading)
                } else {
                    val displayLoading = Runnable {
                        start_activity_ActivateNotificationsLoading!!.visibility = View.INVISIBLE
                        start_activity_ActivateNotifications!!.visibility = View.VISIBLE
                    }
                    runOnUiThread(displayLoading)
                }
            }
            verifiedNotification.start()
        }

        //Lors du click sur activateNotification nous demandont l'autorisation de superposition des écrans
        start_activity_AuthorizeSuperposition!!.setOnClickListener {

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {

                start_activity_AuthorizeSuperposition!!.visibility = View.INVISIBLE

                val SPLASH_DISPLAY_LENGHT = 3000
                //si nous sommes sous l'api 24 alors nous n'avons pas besoin de l'autorisation est nous validons directement
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:$packageName"))
                    startActivity(intent)
                }
                val displayLoading = Runnable {
                    start_activity_AuthorizeSuperpositionLoading!!.visibility = View.VISIBLE
                }
                runOnUiThread(displayLoading)
                //Ici nous créons un thread qui vérifie en boucle si nous sommes revenu dans Knockin une fois revenu alors il affiche l'image de validation(Image_validate) ou le bouton demandant d'autoriser
                val verifiedSuperposition = Thread {
                    activityVisible = false
                    while (!activityVisible) {
                    }
                    println("NotificationService" + isNotificationServiceEnabled() + " activity visible" + activityVisible)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
                        println("into before delayed")
                        //     Handler().postDelayed({


                        val displayLoading = Runnable {
                            Handler().postDelayed({
                                start_activity_AuthorizeSuperpositionLoading!!.visibility = View.INVISIBLE
                                start_activity_AuthorizeSuperpositionCheck!!.visibility = View.VISIBLE
                                val sharedPreferences: SharedPreferences = getSharedPreferences("Knockin_preferences", Context.MODE_PRIVATE)
                                val edit: SharedPreferences.Editor = sharedPreferences.edit()
                                edit.putBoolean("popupNotif", true)
                                edit.apply()
                                allIsChecked()

                            }, SPLASH_DISPLAY_LENGHT.toLong())
                        }
                        runOnUiThread(displayLoading)

                        //  }, SPLASH_DISPLAY_LENGHT.toLong())
                    } else {
                        val displayLoading = Runnable {
                            start_activity_AuthorizeSuperpositionLoading!!.visibility = View.INVISIBLE
                            start_activity_AuthorizeSuperposition!!.visibility = View.VISIBLE
                        }
                        runOnUiThread(displayLoading)
                    }
                }
                verifiedSuperposition.start()
            } else {
                Toast.makeText(this, getString(R.string.start_activity_superposition_not_allowed_message), Toast.LENGTH_LONG).show()
            }
        }

        //Lors du click sur activateNotification nous demandont l'autorisation des appels et des SMS
        start_activity_Permissions!!.setOnClickListener {
            val arraylistPermission = ArrayList<String>()
            arraylistPermission.add(Manifest.permission.SEND_SMS)
            arraylistPermission.add(Manifest.permission.CALL_PHONE)
            ActivityCompat.requestPermissions(this, arraylistPermission.toArray(arrayOfNulls<String>(arraylistPermission.size)), REQUEST_CODE_SMS_AND_CALL)
            start_activity_Permissions!!.visibility = View.INVISIBLE
            start_activity_PermissionsLoading!!.visibility = View.VISIBLE
        }

        //Bouton qui apparait lorsque tout les autorisation ont un check. Lors du click affichage d'un alertDialog d'information
        start_activity_Next!!.setOnClickListener {
            buildMultiSelectAlertDialog()
        }

        //lors du click affichage d'un message de prévention
        start_activity_Skip!!.setOnClickListener {
            //            start_activity_ImportContacts!!.visibility = View.INVISIBLE
            buildMultiSelectAlertDialog()
            buildLeaveAlertDialog()
        }


        //endregion
    }

    //region ========================================== Functions ==========================================

//    fun openSMSappChooser() {
//        val packageManager = packageManager
//        val componentName = ComponentName(this, StartActivity.class)
//        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
//
//        val selector = Intent(Intent.ACTION_MAIN)
//        selector.addCategory(Intent.CATEGORY_APP_MESSAGING)
//        selector.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//        startActivity(selector)
//
//        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP)
//    }

    /**
     *Méthode appellé par le système lorsque l'utilisateur a accepté ou refuser une demande de permission
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_READ_CONTACT) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.import_contacts_toast, Toast.LENGTH_LONG).show()
                val sync = Runnable {
                    ContactManager(this).getAllContacsInfoSync(contentResolver)

                    val sharedPreferencesSync = getSharedPreferences("save_last_sync", Context.MODE_PRIVATE)
                    var index = 1
                    var stringSet = listOf<String>()
                    if (sharedPreferencesSync.getStringSet(index.toString(), null) != null)
                        stringSet = sharedPreferencesSync.getStringSet(index.toString(), null)!!.sorted()
                    arrayListOf<Pair<ContactDB, List<ContactDetailDB>>>()
                    while (sharedPreferencesSync.getStringSet(index.toString(), null) != null && stringSet.isNotEmpty()) {
                        stringSet = sharedPreferencesSync.getStringSet(index.toString(), null)!!.sorted()
                        index++
                    }

                    val runnable = Runnable {
                        start_activity_ImportContactsLoading!!.visibility = View.INVISIBLE
                        start_activity_ImportContactsCheck!!.visibility = View.VISIBLE
                        allIsChecked()
                    }
                    runOnUiThread(runnable)
                }
                start_activity_mDbWorkerThread.postTask(sync)
            } else {
                start_activity_ImportContactsLoading!!.visibility = View.INVISIBLE
                start_activity_ImportContacts!!.visibility = View.VISIBLE
            }
        }

        if (REQUEST_CODE_SMS_AND_CALL == requestCode) {

            start_activity_PermissionsLoading!!.visibility = View.INVISIBLE
            start_activity_PermissionsCheck!!.visibility = View.VISIBLE
        }
        allIsChecked()
    }

    /**
     * Réécriture de la méthode onBackPressed lorsque nous appuyant sur le boutton retour du téléphone rien n'est fait
     */
    override fun onBackPressed() {
    }

    /**
     * Lance l'activité d'autorisation des notification
     */
    private fun activateNotificationsClick() {
        startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
        val intentFilter = IntentFilter()
        intentFilter.addAction("com.yellowtwigs.Knockin.notificationExemple")
    }

    companion object {
        const val REQUEST_CODE_READ_CONTACT = 2
        const val REQUEST_CODE_SMS_AND_CALL = 5
    }

    /**
     * Demande à l'utilisateur si celui-ci désire choisir ses contacts prioritaires
     * @return [AlertDialog]
     */
    private fun buildMultiSelectAlertDialog(): AlertDialog {
        return MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                .setBackground(getDrawable(R.color.backgroundColor))
                .setTitle(getString(R.string.notification_alert_dialog_title))
                .setMessage(getString(R.string.notification_alert_dialog_message))
                .setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
                    startActivity(Intent(this@StartActivity, MultiSelectActivity::class.java))
                    val sharedPreferences: SharedPreferences = getSharedPreferences("Knockin_preferences", Context.MODE_PRIVATE)
                    val edit: SharedPreferences.Editor = sharedPreferences.edit()
                    edit.putBoolean("view", true)
                    edit.apply()
                    closeContextMenu()
                    finish()
                }
                .setNegativeButton(R.string.alert_dialog_later)
                { _, _ ->
                    closeContextMenu()
                    val intent = Intent(this@StartActivity, MainActivity::class.java)
                    intent.putExtra("fromStartActivity", true)
                    startActivity(intent)
                    finish()
                }
                .show()
    }

    /**
     * Permet à l'utilisateur de passer les demandes d'autorisations
     */
    private fun buildLeaveAlertDialog(): AlertDialog {
        val message = if (start_activity_import_contacts_loading.visibility == View.VISIBLE) {//vérifie que le téléphone ne charge pas les contacts sinon celui-ci prévient l'utilisateur que ces contact ne seront pas tous chargés
            getString(R.string.start_activity_skip_alert_dialog_message_importation)
        } else {
            getString(R.string.start_activity_skip_alert_dialog_message)
        }

        return MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                .setBackground(getDrawable(R.color.backgroundColor))
                .setTitle(getString(R.string.start_activity_skip_alert_dialog_title))
                .setMessage(message)
                .setPositiveButton(R.string.start_activity_skip_alert_dialog_positive_button) { _, _ ->
                    val intent = Intent(this@StartActivity, MainActivity::class.java)
                    intent.putExtra("fromStartActivity", true)
                    startActivity(intent)
                    val sharedPreferences: SharedPreferences = getSharedPreferences("Knockin_preferences", Context.MODE_PRIVATE)
                    val edit: SharedPreferences.Editor = sharedPreferences.edit()
                    edit.putBoolean("view", true)
                    edit.apply()
                    closeContextMenu()
                }
                .setNegativeButton(R.string.alert_dialog_cancel)
                { _, _ ->
                    closeContextMenu()
                }
                .show()
    }

    /**
     * Vérifie que nous avons l'autorisation de récupérer les notifications
     * @return  [Boolean]
     */
    private fun isNotificationServiceEnabled(): Boolean {
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
    }//TODO enlever duplicate code

    //ActivityVisible nous permet de savoir lorsque nous retournons dans cette activité
    override fun onStart() {
        super.onStart()
        activityVisible = true
    }

    override fun onResume() {
        super.onResume()
        activityVisible = true
    }

    /**
     * Si toutes les autorisations sont validées et que les contacts ont fini d'être charger alors nous changeons le bouton passer pour un bouton suivant
     */
    private fun allIsChecked() {
        if (start_activity_ActivateNotificationsCheck!!.visibility == View.VISIBLE &&
                start_activity_ImportContactsCheck!!.visibility == View.VISIBLE) {
            start_activity_Next!!.visibility = View.VISIBLE
        }
    }

    private fun getAppOnPhone(): java.util.ArrayList<String> {
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        val resolveInfoList = packageManager.queryIntentActivities(intent, 0)
        val packageNameList = java.util.ArrayList<String>()
        for (resolveInfo in resolveInfoList) {
            val activityInfo = resolveInfo.activityInfo
            packageNameList.add(activityInfo.applicationInfo.packageName)
        }
        return packageNameList
    }

    //endregion
}
