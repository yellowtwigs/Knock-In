package com.yellowtwigs.knockin.controller.activity.firstLaunch

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.*
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Point
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.text.TextUtils
import android.util.TypedValue
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.controller.activity.MainActivity
import com.yellowtwigs.knockin.model.ContactManager
import com.yellowtwigs.knockin.model.DbWorkerThread
import com.yellowtwigs.knockin.model.ModelDB.ContactDB
import com.yellowtwigs.knockin.model.ModelDB.ContactDetailDB
import java.net.InetAddress
import java.net.UnknownHostException


/**
 * Activité qui nous permet d'importer nos contacts et accepter toutes les autorisations liées aux notifications appel et message
 * @author Florian Striebel, Kenzy Suon
 */
class StartActivity : AppCompatActivity() {

    //region ========================================== Val or Var ==========================================

    private var startActivityImportContacts: MaterialButton? = null
    private var startActivityActivateNotifications: MaterialButton? = null
    private var startActivityAuthorizeSuperposition: MaterialButton? = null
    private var startActivityPermissions: MaterialButton? = null

    private var startActivityImportContactsLayout: RelativeLayout? = null
    private var startActivityActivateNotificationsLayout: RelativeLayout? = null
    private var startActivityAuthorizeSuperpositionLayout: RelativeLayout? = null
    private var startActivityPermissionsLayout: RelativeLayout? = null

    private var startActivityNext: MaterialButton? = null
    private var startActivitySkip: MaterialButton? = null
    private var startActivityImportContactsLoading: ProgressBar? = null
    private var startActivityActivateNotificationsLoading: ProgressBar? = null
    private var startActivityAuthorizeSuperpositionLoading: ProgressBar? = null
    private var startActivityPermissionsLoading: ProgressBar? = null
    private var start_activity_import_contacts_loading: ProgressBar? = null

    private var startActivityImportContactsCheck: AppCompatImageView? = null
    private var startActivityActivateNotificationsCheck: AppCompatImageView? = null
    private var startActivityAuthorizeSuperpositionCheck: AppCompatImageView? = null
    private var startActivityPermissionsCheck: AppCompatImageView? = null

    private lateinit var start_activity_mDbWorkerThread: DbWorkerThread
    private var activityVisible = false

    //endregion

    @SuppressLint("ObsoleteSdkInt", "SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView()

        val listApp = getAppOnPhone()

        //region ======================================= FindViewById =======================================

        startActivityImportContacts = findViewById(R.id.start_activity_import_contacts_button)
        startActivityActivateNotifications = findViewById(R.id.start_activity_activate_notifications_button)
        startActivityAuthorizeSuperposition = findViewById(R.id.start_activity_superposition_button)
        startActivityPermissions = findViewById(R.id.start_activity_permissions_button)

        startActivityImportContactsLayout = findViewById(R.id.start_activity_import_contacts_layout)
        startActivityActivateNotificationsLayout = findViewById(R.id.start_activity_notifications_layout)
        startActivityAuthorizeSuperpositionLayout = findViewById(R.id.start_activity_superposition_layout)
        startActivityPermissionsLayout = findViewById(R.id.start_activity_permissions_layout)

        startActivityNext = findViewById(R.id.start_activity_next)
        startActivitySkip = findViewById(R.id.start_activity_skip)
        startActivityImportContactsLoading = findViewById(R.id.start_activity_import_contacts_loading)
        startActivityActivateNotificationsLoading = findViewById(R.id.start_activity_activate_notifications_loading)
        startActivityAuthorizeSuperpositionLoading = findViewById(R.id.start_activity_superposition_loading)
        startActivityPermissionsLoading = findViewById(R.id.start_activity_permissions_loading)
        start_activity_import_contacts_loading = findViewById(R.id.start_activity_import_contacts_loading)

        startActivityImportContactsCheck = findViewById(R.id.start_activity_import_contacts_check)
        startActivityActivateNotificationsCheck = findViewById(R.id.start_activity_activate_notifications_check)
        startActivityAuthorizeSuperpositionCheck = findViewById(R.id.start_activity_superposition_check)
        startActivityPermissionsCheck = findViewById(R.id.start_activity_permissions_check)


        val start_activity_layout = findViewById<ConstraintLayout>(R.id.start_activity_layout)
        val start_activity_video_Layout = findViewById<RelativeLayout>(R.id.start_activity_video_layout)
        val videoview = findViewById<VideoView>(R.id.start_activity_video)
        val webview: WebView = findViewById(R.id.start_activity_webview)
        val start_activity_video_Skip = findViewById<MaterialButton>(R.id.start_activity_video_skip)

        val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        var isWifiConn = false
        var isMobileConn = false
        connMgr.allNetworks.forEach { network ->
            connMgr.getNetworkInfo(network)?.apply {
                if (type == ConnectivityManager.TYPE_WIFI) {
                    isWifiConn = isWifiConn or isConnected
                }
                if (type == ConnectivityManager.TYPE_MOBILE) {
                    isMobileConn = isMobileConn or isConnected
                }
            }
        }
        val layoutSize = Point()
        val displayScreen = windowManager.defaultDisplay
        displayScreen.getRealSize(layoutSize)
        if (isWifiConn || isMobileConn) {
            val sizeTest = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150f, resources.displayMetrics)
            webview.layoutParams.height = layoutSize.y - sizeTest.toInt()
            webview.settings.loadWithOverviewMode = true
            webview.settings.useWideViewPort = true
            webview.settings.javaScriptEnabled = true
            webview.webChromeClient = WebChromeClientCustomPoster()
            //videoview.visibility = View.INVISIBLE

            when (Resources.getSystem().configuration.locale.language) {
                "fr" -> {
                    webview.visibility = View.VISIBLE
                    webview.loadUrl("https://www.yellowtwigs.com/france")
                }
                "de" -> {
                    println("////////////////////////////")
                    println(startActivityImportContacts!!.textSize)
                    startActivityImportContacts!!.textSize = 10f
                    startActivityActivateNotifications!!.textSize = 10f
                    startActivityAuthorizeSuperposition!!.textSize = 10f
                    startActivityPermissions!!.textSize = 10f
                    println(startActivityImportContacts!!.textSize)
                    println("////////////////////////////")
                    webview.visibility = View.VISIBLE
                    webview.loadUrl("https://www.yellowtwigs.com/germany")
                }
                "in" -> {
                    startActivityImportContacts!!.textSize = 9f
                    startActivityActivateNotifications!!.textSize = 9f
                    startActivityAuthorizeSuperposition!!.textSize = 9f
                    startActivityPermissions!!.textSize = 9f
                    webview.visibility = View.VISIBLE
                    webview.loadUrl("https://www.yellowtwigs.com/indonesia")
                }
                "vi" -> {
                    webview.visibility = View.VISIBLE
                    webview.loadUrl("https://www.yellowtwigs.com/vietnam")
                }
                "it" -> {
                    webview.visibility = View.VISIBLE
                    webview.loadUrl("https://www.yellowtwigs.com/italy")
                }
                "es" -> {
                    webview.visibility = View.VISIBLE
                    webview.loadUrl("https://www.yellowtwigs.com/spain")
                }
                "pt" -> {
                    webview.visibility = View.VISIBLE
                    webview.loadUrl("https://www.yellowtwigs.com/portugal")
                }
                "ar" -> {
                    webview.visibility = View.VISIBLE
                    webview.loadUrl("https://www.yellowtwigs.com/arabic")
                }
                "ru" -> {
                    startActivityImportContacts!!.textSize = 7f
                    startActivityActivateNotifications!!.textSize = 7f
                    startActivityAuthorizeSuperposition!!.textSize = 7f
                    startActivityPermissions!!.textSize = 7f
                    webview.visibility = View.VISIBLE
                    webview.loadUrl("https://www.yellowtwigs.com/russia")
                }
                "tr" -> {
                    webview.visibility = View.VISIBLE
                    webview.loadUrl("https://www.yellowtwigs.com/turkey")
                }
            }
        }
        if (webview.visibility == View.GONE) {
            val sizeTest = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150f, resources.displayMetrics)
            videoview.layoutParams.height = layoutSize.y - sizeTest.toInt()
            webview.visibility = View.INVISIBLE
            videoview.visibility = View.VISIBLE
            val uri = Uri.parse("android.resource://" + packageName + "/" + R.raw.in_app_video_en)
            videoview.setVideoURI(uri)
            videoview.start()
        }

        val sharedPreferences: SharedPreferences = getSharedPreferences("Knockin_preferences", Context.MODE_PRIVATE)
        val edit: SharedPreferences.Editor = sharedPreferences.edit()
        edit.putBoolean("popupNotif", true)
        edit.apply()

        val mediaController = MediaController(this)
        videoview.setMediaController(mediaController)
        mediaController.setAnchorView(videoview)

        videoview.setOnCompletionListener {
            start_activity_video_Layout.visibility = View.INVISIBLE
            start_activity_video_Skip.visibility = View.INVISIBLE
            start_activity_layout.visibility = View.VISIBLE
            videoview.stopPlayback()
        }

        //endregion

        //region ======================================= WorkerThread =======================================

        start_activity_mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        start_activity_mDbWorkerThread.start()

        //endregion

        if (checkIfGoEdition()) {
            startActivityActivateNotificationsLayout!!.visibility = View.GONE
            startActivityAuthorizeSuperpositionLayout!!.visibility = View.GONE

            MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                    .setBackground(getDrawable(R.color.backgroundColor))
                    .setMessage(getString(R.string.start_activity_go_edition_message))
                    .setPositiveButton(R.string.start_activity_go_edition_positive_button) { _, _ ->
                    }
                    .show()
        }

//        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
//            MaterialAlertDialogBuilder(this, R.style.AlertDialog)
//                    .setBackground(getDrawable(R.color.backgroundColor))
//                    .setMessage(getString(R.string.start_activity_superposition_not_allowed_message_11))
//                    .setPositiveButton(R.string.start_activity_go_edition_positive_button) { _, _ ->
//                    }
//                    .show()
//
//        }

        //region ======================================== Listeners =========================================

        start_activity_video_Skip.setOnClickListener {
            webview.removeAllViews()
            webview.clearCache(true)
            webview.destroy()
            start_activity_video_Layout.visibility = View.INVISIBLE
            start_activity_video_Skip.visibility = View.INVISIBLE
            start_activity_layout.visibility = View.VISIBLE
            videoview.stopPlayback()

            Toast.makeText(this, getString(R.string.start_activity_video_tutorial_skip_text), Toast.LENGTH_LONG).show()
        }

        //Lors du click sur synchroniser alors nous demandons l'autorisation d'accéder aux contact puis nous affichons un loading
        startActivityImportContacts!!.setOnClickListener {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), ImportContactsActivity.REQUEST_CODE_READ_CONTACT)
            startActivityImportContacts!!.visibility = View.INVISIBLE

            val displayLoading = Runnable {
                startActivityImportContactsLoading!!.visibility = View.VISIBLE
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
        startActivityActivateNotifications!!.setOnClickListener {
            activateNotificationsClick()
            startActivityActivateNotifications!!.visibility = View.INVISIBLE
            startActivityActivateNotificationsLoading!!.visibility = View.VISIBLE

            val SPLASH_DISPLAY_LENGHT = 2000

            val displayLoading = Runnable {
                startActivityActivateNotificationsLoading!!.visibility = View.VISIBLE
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
                            startActivityActivateNotificationsLoading!!.visibility = View.INVISIBLE
                            startActivityActivateNotificationsCheck!!.visibility = View.VISIBLE
                            val sharedPreferences: SharedPreferences = getSharedPreferences("Knockin_preferences", Context.MODE_PRIVATE)
                            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                                val edit: SharedPreferences.Editor = sharedPreferences.edit()
                                edit.putBoolean("serviceNotif", true)
                                edit.putBoolean("popupNotif", true)
                                edit.apply()
                            } else {
                                val edit: SharedPreferences.Editor = sharedPreferences.edit()
                                edit.putBoolean("serviceNotif", true)
                                edit.putBoolean("popupNotif", false)
                                edit.apply()
                            }
                            allIsChecked()
                        }, SPLASH_DISPLAY_LENGHT.toLong())
                    }
                    runOnUiThread(displayLoading)
                } else {
                    val displayLoading = Runnable {
                        startActivityActivateNotificationsLoading!!.visibility = View.INVISIBLE
                        startActivityActivateNotifications!!.visibility = View.VISIBLE
                    }
                    runOnUiThread(displayLoading)
                }
            }
            verifiedNotification.start()
        }

        //Lors du click sur activateNotification nous demandont l'autorisation de superposition des écrans
        startActivityAuthorizeSuperposition!!.setOnClickListener {

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {

                startActivityAuthorizeSuperposition!!.visibility = View.INVISIBLE

                val SPLASH_DISPLAY_LENGHT = 3000
                //si nous sommes sous l'api 24 alors nous n'avons pas besoin de l'autorisation est nous validons directement
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:$packageName"))
                    startActivity(intent)
                }
                val displayLoading = Runnable {
                    startActivityAuthorizeSuperpositionLoading!!.visibility = View.VISIBLE
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
                                startActivityAuthorizeSuperpositionLoading!!.visibility = View.INVISIBLE
                                startActivityAuthorizeSuperpositionCheck!!.visibility = View.VISIBLE
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
                            startActivityAuthorizeSuperpositionLoading!!.visibility = View.INVISIBLE
                            startActivityAuthorizeSuperposition!!.visibility = View.VISIBLE
                        }
                        runOnUiThread(displayLoading)
                    }
                }
                verifiedSuperposition.start()
            } else {
            }
        }

        //Lors du click sur activateNotification nous demandont l'autorisation des appels et des SMS
        startActivityPermissions!!.setOnClickListener {
            val arraylistPermission = ArrayList<String>()
            arraylistPermission.add(Manifest.permission.SEND_SMS)
            arraylistPermission.add(Manifest.permission.CALL_PHONE)
            ActivityCompat.requestPermissions(this, arraylistPermission.toArray(arrayOfNulls<String>(arraylistPermission.size)), REQUEST_CODE_SMS_AND_CALL)
            startActivityPermissions!!.visibility = View.INVISIBLE
            startActivityPermissionsLoading!!.visibility = View.VISIBLE
            allIsCheckedGOEdition()
        }

        //Bouton qui apparait lorsque tout les autorisation ont un check. Lors du click affichage d'un alertDialog d'information
        startActivityNext!!.setOnClickListener {
            if (!checkIfGoEdition()) {
                buildMultiSelectAlertDialog()
            } else {
                val intent = Intent(this@StartActivity, MainActivity::class.java)
                intent.putExtra("fromStartActivity", true)
                startActivity(intent)
                finish()
            }
        }

        //lors du click affichage d'un message de prévention
        startActivitySkip!!.setOnClickListener {
            //            startActivityImportContacts!!!!.visibility = View.INVISIBLE
            if (!checkIfGoEdition()) {
                buildMultiSelectAlertDialog()
                buildLeaveAlertDialog()
            } else {
                val intent = Intent(this@StartActivity, MainActivity::class.java)
                intent.putExtra("fromStartActivity", true)
                startActivity(intent)
                finish()
            }
        }


        //endregion
    }

    private fun setContentView() {
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val height = size.y

        when {
            height > 2500 -> setContentView(R.layout.activity_start_activity)
            height in 1800..2499 -> setContentView(R.layout.activity_start_activity)
            height in 1100..1799 -> setContentView(R.layout.activity_start_activity_smaller_screen)
            height < 1099 -> setContentView(R.layout.activity_start_activity_mini_screen)
        }
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
    }

    private fun checkIfGoEdition(): Boolean {
        val am = baseContext.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        return am.isLowRamDevice
    }

    private fun isNetworkConnected(): Boolean {
        try {
            val address: InetAddress = InetAddress.getByName("www.google.com");
            return !address.equals("")
        } catch (e: UnknownHostException) {
            println("OI OI OI")
        }
        return false
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
                        startActivityImportContactsLoading!!.visibility = View.INVISIBLE
                        startActivityImportContactsCheck!!.visibility = View.VISIBLE
                        allIsChecked()
                        allIsCheckedGOEdition()
                    }
                    runOnUiThread(runnable)
                }
                start_activity_mDbWorkerThread.postTask(sync)
            } else {
                startActivityImportContactsLoading!!.visibility = View.INVISIBLE
                startActivityImportContacts!!.visibility = View.VISIBLE
            }
        }

        if (REQUEST_CODE_SMS_AND_CALL == requestCode) {

            startActivityPermissionsLoading!!.visibility = View.INVISIBLE
            startActivityPermissionsCheck!!.visibility = View.VISIBLE
        }
        allIsChecked()
        allIsCheckedGOEdition()
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
        val message = if (start_activity_import_contacts_loading?.visibility == View.VISIBLE) {
            //vérifie que le téléphone ne charge pas les contacts sinon celui-ci prévient l'utilisateur que ces contact ne seront pas tous chargés
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
        if (startActivityActivateNotificationsCheck!!.visibility == View.VISIBLE &&
                startActivityImportContactsCheck!!.visibility == View.VISIBLE) {
            startActivityNext!!.visibility = View.VISIBLE
            startActivitySkip!!.visibility = View.GONE
        }
    }

    private fun allIsCheckedGOEdition() {
        if (startActivityImportContactsCheck!!.visibility == View.VISIBLE &&
                startActivityPermissionsCheck!!.visibility == View.VISIBLE) {
            startActivityNext!!.visibility = View.VISIBLE
            startActivitySkip!!.visibility = View.GONE
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

//permet de supprimer l'icon grise au lancement d'une video dans la webview
private class WebChromeClientCustomPoster : WebChromeClient() {
    override fun getDefaultVideoPoster(): Bitmap? {
        return Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
    }
}