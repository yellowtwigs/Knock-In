package com.yellowtwigs.knockin.ui.first_launch

import android.Manifest
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
import android.provider.Telephony
import android.text.TextUtils
import android.util.Log
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
import androidx.core.view.isVisible
import com.android.billingclient.api.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.ui.contacts.MainActivity
import com.yellowtwigs.knockin.model.ContactManager
import com.yellowtwigs.knockin.model.DbWorkerThread
import com.yellowtwigs.knockin.model.data.ContactDB
import com.yellowtwigs.knockin.model.data.ContactDetailDB
import java.net.InetAddress
import java.net.UnknownHostException

/**
 * Activité qui nous permet d'importer nos contacts et accepter toutes les autorisations liées aux notifications appel et message
 * @author Florian Striebel, Kenzy Suon
 */
class StartActivity : AppCompatActivity(), PurchasesUpdatedListener {

    //region ========================================== Val or Var ==========================================

    private var importContactsButton: MaterialButton? = null
    private var importContactsLayout: RelativeLayout? = null
    private var importContactsLoading: ProgressBar? = null
    private var importContactsCheck: AppCompatImageView? = null

    private var activateNotificationsButton: MaterialButton? = null
    private var activateNotificationsLayout: RelativeLayout? = null
    private var activateNotificationsLoading: ProgressBar? = null
    private var activateNotificationsCheck: AppCompatImageView? = null

    private var superpositionButton: MaterialButton? = null
    private var superpositionLayout: RelativeLayout? = null
    private var superpositionLoading: ProgressBar? = null
    private var superpositionCheck: AppCompatImageView? = null

    private var permissionsButton: MaterialButton? = null
    private var permissionsLoading: ProgressBar? = null
    private var permissionsLayout: RelativeLayout? = null
    private var permissionsCheck: AppCompatImageView? = null

    private var startActivityNext: MaterialButton? = null
    private var startActivitySkip: MaterialButton? = null

    private var billingClient: BillingClient? = null
    private var sharedFunkySoundPreferences: SharedPreferences? = null
    private var sharedJazzySoundPreferences: SharedPreferences? = null
    private var sharedRelaxationSoundPreferences: SharedPreferences? = null
    private var sharedContactsUnlimitedPreferences: SharedPreferences? = null
    private var sharedCustomSoundPreferences: SharedPreferences? = null

    private lateinit var workerThread: DbWorkerThread
    private var activityNotificationVisible = false

    private var activitySuperpositionVisible = false
    private var clickSuperpositionButton = false

    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView()

        val listApp = getAppOnPhone()

        sharedFunkySoundPreferences =
            getSharedPreferences("Funky_Sound_Bought", Context.MODE_PRIVATE)
        sharedJazzySoundPreferences =
            getSharedPreferences("Jazzy_Sound_Bought", Context.MODE_PRIVATE)
        sharedRelaxationSoundPreferences =
            getSharedPreferences("Relax_Sound_Bought", Context.MODE_PRIVATE)
        sharedContactsUnlimitedPreferences =
            getSharedPreferences("Contacts_Unlimited_Bought", Context.MODE_PRIVATE)
        sharedCustomSoundPreferences =
            getSharedPreferences("Custom_Sound_Bought", Context.MODE_PRIVATE)

        setupBillingClient()

        //region ======================================= FindViewById =======================================

        importContactsButton = findViewById(R.id.import_contacts_button)
        activateNotificationsButton =
            findViewById(R.id.activate_notifications_button)
        superpositionButton = findViewById(R.id.superposition_button)
        permissionsButton = findViewById(R.id.permissions_button)

        importContactsLayout = findViewById(R.id.start_activity_import_contacts_layout)
        activateNotificationsLayout =
            findViewById(R.id.start_activity_notifications_layout)
        superpositionLayout =
            findViewById(R.id.start_activity_superposition_layout)
        permissionsLayout = findViewById(R.id.start_activity_permissions_layout)

        startActivityNext = findViewById(R.id.start_activity_next)
        startActivitySkip = findViewById(R.id.start_activity_skip)
        importContactsLoading =
            findViewById(R.id.import_contacts_loading)
        activateNotificationsLoading =
            findViewById(R.id.start_activity_activate_notifications_loading)
        superpositionLoading =
            findViewById(R.id.superposition_loading)
        permissionsLoading = findViewById(R.id.permissions_loading)

        importContactsCheck = findViewById(R.id.import_contacts_check)
        activateNotificationsCheck =
            findViewById(R.id.activate_notifications_check)
        superpositionCheck =
            findViewById(R.id.superposition_check)
        permissionsCheck = findViewById(R.id.permissions_check)


        val start_activity_layout = findViewById<ConstraintLayout>(R.id.start_activity_layout)
        val start_activity_video_Layout =
            findViewById<RelativeLayout>(R.id.start_activity_video_layout)
        val videoview = findViewById<VideoView>(R.id.start_activity_video)
        val webview: WebView = findViewById(R.id.start_activity_webview)
        val video_skip = findViewById<MaterialButton>(R.id.video_skip)

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
            val sizeTest = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                150f,
                resources.displayMetrics
            )
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
                    println(importContactsButton!!.textSize)
                    importContactsButton!!.textSize = 10f
                    activateNotificationsButton!!.textSize = 10f
                    superpositionButton!!.textSize = 10f
                    permissionsButton!!.textSize = 10f
                    println(importContactsButton!!.textSize)
                    println("////////////////////////////")
                    webview.visibility = View.VISIBLE
                    webview.loadUrl("https://www.yellowtwigs.com/germany")
                }
                "in" -> {
                    importContactsButton!!.textSize = 9f
                    activateNotificationsButton!!.textSize = 9f
                    superpositionButton!!.textSize = 9f
                    permissionsButton!!.textSize = 9f
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
                    importContactsButton!!.textSize = 7f
                    activateNotificationsButton!!.textSize = 7f
                    superpositionButton!!.textSize = 7f
                    permissionsButton!!.textSize = 7f
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
            val sizeTest = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                150f,
                resources.displayMetrics
            )
            videoview.layoutParams.height = layoutSize.y - sizeTest.toInt()
            webview.visibility = View.INVISIBLE
            videoview.visibility = View.VISIBLE
            val uri = Uri.parse("android.resource://" + packageName + "/" + R.raw.in_app_video_en)
            videoview.setVideoURI(uri)
            videoview.start()
        }

        val sharedPreferences: SharedPreferences =
            getSharedPreferences("Knockin_preferences", Context.MODE_PRIVATE)
        val edit: SharedPreferences.Editor = sharedPreferences.edit()
        edit.putBoolean("popupNotif", true)
        edit.apply()

        val mediaController = MediaController(this)
        videoview.setMediaController(mediaController)
        mediaController.setAnchorView(videoview)

        videoview.setOnCompletionListener {
            start_activity_video_Layout.visibility = View.INVISIBLE
            video_skip.visibility = View.INVISIBLE
            start_activity_layout.visibility = View.VISIBLE
            videoview.stopPlayback()
        }

        //endregion

        //region ======================================= WorkerThread =======================================

        workerThread = DbWorkerThread("dbWorkerThread")
        workerThread.start()

        //endregion

        if (checkIfGoEdition()) {
            activateNotificationsLayout?.visibility = View.GONE
            superpositionLayout?.visibility = View.GONE

            MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                .setBackground(getDrawable(R.color.backgroundColor))
                .setMessage(getString(R.string.start_activity_go_edition_message))
                .setPositiveButton(R.string.start_activity_go_edition_positive_button) { _, _ ->
                }
                .show()
        }

        Log.i("Telephony", "${Telephony.Sms.getDefaultSmsPackage(this)}")

        //region ======================================== Listeners =========================================

        video_skip.setOnClickListener {
            webview.removeAllViews()
            webview.clearCache(true)
            webview.destroy()
            start_activity_video_Layout.visibility = View.INVISIBLE
            video_skip.visibility = View.INVISIBLE
            start_activity_layout.visibility = View.VISIBLE
            videoview.stopPlayback()

            Toast.makeText(
                this,
                getString(R.string.start_activity_video_tutorial_skip_text),
                Toast.LENGTH_LONG
            ).show()
        }

        importContactsButton?.setOnClickListener {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_CONTACTS),
                ImportContactsActivity.REQUEST_CODE_READ_CONTACT
            )
            importContactsButton!!.visibility = View.INVISIBLE

            val displayLoading = Runnable {
                importContactsLoading!!.visibility = View.VISIBLE
            }
            runOnUiThread(displayLoading)

            if (listApp.contains("com.whatsapp")) {
                val importWhatsappSharedPreferences: SharedPreferences =
                    getSharedPreferences("importWhatsappPreferences", Context.MODE_PRIVATE)
                val edit: SharedPreferences.Editor = importWhatsappSharedPreferences.edit()
                edit.putBoolean("importWhatsappPreferences", true)
                edit.apply()
            }
        }

        activateNotificationsButton?.setOnClickListener {
            activateNotificationsClick()
            activateNotificationsButton?.visibility = View.INVISIBLE
            activateNotificationsLoading?.visibility = View.VISIBLE

            val SPLASH_DISPLAY_LENGHT = 2000

            val displayLoading = Runnable {
                activateNotificationsLoading!!.visibility = View.VISIBLE
            }
            runOnUiThread(displayLoading)
            //Ici nous créons un thread qui vérifie en boucle si nous sommes revenu dans Knockin une fois revenu alors il affiche l'image de validation(Image_validate) ou le bouton demandant d'autoriser
            val verifiedNotification = Thread {
                activityNotificationVisible = false
                while (!activityNotificationVisible) {
                }
                if (isNotificationServiceEnabled()) {
                    val displayLoading = Runnable {
                        //start_activity_ActivateNotificationsLoading!!.visibility = View.INVISIBLE
                        //start_activity_ActivateNotificationsCheck!!.visibility = View.VISIBLE
                        Handler().postDelayed({
                            activateNotificationsLoading!!.visibility = View.INVISIBLE
                            activateNotificationsCheck!!.visibility = View.VISIBLE
                            val sharedPreferences: SharedPreferences =
                                getSharedPreferences("Knockin_preferences", Context.MODE_PRIVATE)
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
                        activateNotificationsLoading!!.visibility = View.INVISIBLE
                        activateNotificationsButton!!.visibility = View.VISIBLE
                    }
                    runOnUiThread(displayLoading)
                }
            }
            verifiedNotification.start()
        }

        superpositionButton?.setOnClickListener {
            if (clickSuperpositionButton) {
                verifiedOverlaySettings()
            } else {
                clickSuperpositionButton = true
                openOverlaySettings()
            }
        }

        permissionsButton?.setOnClickListener {
            val arraylistPermission = ArrayList<String>()
            arraylistPermission.add(Manifest.permission.SEND_SMS)
            arraylistPermission.add(Manifest.permission.CALL_PHONE)
            ActivityCompat.requestPermissions(
                this,
                arraylistPermission.toArray(arrayOfNulls<String>(arraylistPermission.size)),
                REQUEST_CODE_SMS_AND_CALL
            )
            permissionsButton!!.visibility = View.INVISIBLE
            permissionsLoading!!.visibility = View.VISIBLE
            allIsCheckedGOEdition()
        }

        startActivityNext?.setOnClickListener {
            if (!checkIfGoEdition()) {
                buildMultiSelectAlertDialog()
            } else {
                val intent = Intent(this@StartActivity, MainActivity::class.java)
                intent.putExtra("fromStartActivity", true)
                startActivity(intent)
                finish()
            }
        }

        startActivitySkip?.setOnClickListener {
            if (!checkIfGoEdition()) {
                if (importContactsCheck?.isVisible == true) {
                    buildMultiSelectAlertDialog()
                } else {
                    buildLeaveAlertDialog()
                }
            } else {
                val intent = Intent(this@StartActivity, MainActivity::class.java)
                intent.putExtra("fromStartActivity", true)
                startActivity(intent)
                finish()
            }
        }

        //endregion
    }

    //region ========================================== Functions ==========================================

    private fun openOverlaySettings() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S) {
            superpositionButton?.visibility = View.INVISIBLE
            if (checkAndroid6()) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
            }
            val displayLoading = Runnable {
                superpositionLoading?.visibility = View.VISIBLE
            }
            runOnUiThread(displayLoading)
        }
    }

    private fun verifiedOverlaySettings() {
        val verifiedSuperposition = Thread {
            while (!activitySuperpositionVisible) {
            }
            if (checkAndroid6() && Settings.canDrawOverlays(this) || checkAndroid8()) {
                val displayLoading = Runnable {
                    Handler().postDelayed({
                        superpositionLoading?.visibility =
                            View.INVISIBLE
                        superpositionCheck?.visibility = View.VISIBLE
                        val sharedPreferences: SharedPreferences = getSharedPreferences(
                            "Knockin_preferences",
                            Context.MODE_PRIVATE
                        )
                        val edit: SharedPreferences.Editor = sharedPreferences.edit()
                        edit.putBoolean("popupNotif", true)
                        edit.apply()
                        allIsChecked()

                    }, 3000)
                }
                runOnUiThread(displayLoading)
            } else {
                val displayLoading = Runnable {
                    superpositionLoading?.visibility = View.INVISIBLE
                    superpositionButton?.visibility = View.VISIBLE
                }
                runOnUiThread(displayLoading)
            }
        }
        verifiedSuperposition.start()
    }

    private fun checkAndroid6(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }

    private fun checkAndroid8(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
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

    private fun setupBillingClient() {
        billingClient = BillingClient.newBuilder(this)
            .setListener(this)
            .enablePendingPurchases()
            .build()
        connectToGooglePlayBilling()
    }

    private fun connectToGooglePlayBilling() {
        billingClient?.startConnection(object : BillingClientStateListener {
            /**
             * Called to notify that setup is complete.
             *
             * @param billingResult The [BillingResult] which returns the status of the setup process.
             */
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    querySkuDetails()
                } else {
                    Log.i("playBilling", "Fail")
                }
            }

            override fun onBillingServiceDisconnected() {
                connectToGooglePlayBilling()
            }
        })
    }

    private fun querySkuDetails() {
        val skuList = ArrayList<String>()
        skuList.add("contacts_vip_unlimited")
        skuList.add("notifications_vip_funk_theme")
        skuList.add("notifications_vip_jazz_theme")
        skuList.add("notifications_vip_relaxation_theme")
        skuList.add("notifications_vip_custom_tones_theme")

        val params = SkuDetailsParams.newBuilder()
            .setSkusList(skuList)
            .setType(BillingClient.SkuType.INAPP)

        billingClient?.querySkuDetailsAsync(
            params.build()
        ) { _, skuDetailsList ->

            billingClient?.queryPurchasesAsync(
                BillingClient.SkuType.INAPP
            ) { _, listOfPurchases ->
                if (listOfPurchases.isNotEmpty()) {
                    for (purchase in listOfPurchases) {
                        when {
                            purchase.originalJson.contains("notifications_vip_funk_theme") -> {
                                val edit =
                                    sharedFunkySoundPreferences?.edit()
                                edit?.putBoolean("Funky_Sound_Bought", true)
                                edit?.apply()
                            }
                            purchase.originalJson.contains("notifications_vip_jazz_theme") -> {
                                val edit =
                                    sharedJazzySoundPreferences?.edit()
                                edit?.putBoolean("Jazzy_Sound_Bought", true)
                                edit?.apply()
                            }
                            purchase.originalJson.contains("notifications_vip_relaxation_theme") -> {
                                val edit =
                                    sharedRelaxationSoundPreferences?.edit()
                                edit?.putBoolean("Relax_Sound_Bought", true)
                                edit?.apply()
                            }
                            purchase.originalJson.contains("contacts_vip_unlimited") -> {
                                val edit =
                                    sharedContactsUnlimitedPreferences?.edit()
                                edit?.putBoolean(
                                    "Contacts_Unlimited_Bought",
                                    true
                                )
                                edit?.apply()
                            }
                            purchase.originalJson.contains("notifications_vip_custom_tones_theme") -> {
                                val edit =
                                    sharedCustomSoundPreferences?.edit()
                                edit?.putBoolean("Custom_Sound_Bought", true)
                                edit?.apply()
                            }
                        }
                    }
                }
            }
        }
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

    /**
     *Méthode appellé par le système lorsque l'utilisateur a accepté ou refuser une demande de permission
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_READ_CONTACT) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.import_contacts_toast, Toast.LENGTH_LONG).show()
                val sync = Runnable {
                    ContactManager(this).getAllContacsInfoSync(contentResolver)

                    val sharedPreferencesSync =
                        getSharedPreferences("save_last_sync", Context.MODE_PRIVATE)
                    var index = 1
                    var stringSet = listOf<String>()
                    if (sharedPreferencesSync.getStringSet(index.toString(), null) != null)
                        stringSet =
                            sharedPreferencesSync.getStringSet(index.toString(), null)!!.sorted()
                    arrayListOf<Pair<ContactDB, List<ContactDetailDB>>>()
                    while (sharedPreferencesSync.getStringSet(
                            index.toString(),
                            null
                        ) != null && stringSet.isNotEmpty()
                    ) {
                        stringSet =
                            sharedPreferencesSync.getStringSet(index.toString(), null)!!.sorted()
                        index++
                    }

                    val runnable = Runnable {
                        importContactsLoading!!.visibility = View.INVISIBLE
                        importContactsCheck?.visibility = View.VISIBLE
                        allIsChecked()
                        allIsCheckedGOEdition()
                    }
                    runOnUiThread(runnable)
                }
                workerThread.postTask(sync)
            } else {
                importContactsLoading!!.visibility = View.INVISIBLE
                importContactsButton!!.visibility = View.VISIBLE
            }
        }

        if (REQUEST_CODE_SMS_AND_CALL == requestCode) {

            permissionsLoading!!.visibility = View.INVISIBLE
            permissionsCheck!!.visibility = View.VISIBLE
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
                val sharedPreferences: SharedPreferences =
                    getSharedPreferences("Knockin_preferences", Context.MODE_PRIVATE)
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
        val message = if (importContactsButton?.visibility == View.VISIBLE) {
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
                val sharedPreferences: SharedPreferences =
                    getSharedPreferences("Knockin_preferences", Context.MODE_PRIVATE)
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
    }

    /**
     * Si toutes les autorisations sont validées et que les contacts ont fini d'être charger alors nous changeons le bouton passer pour un bouton suivant
     */
    private fun allIsChecked() {
        if (activateNotificationsCheck!!.visibility == View.VISIBLE &&
            importContactsCheck!!.visibility == View.VISIBLE
        ) {
            startActivityNext?.visibility = View.VISIBLE
            startActivitySkip?.visibility = View.GONE
        }
    }

    private fun allIsCheckedGOEdition() {
        if (importContactsCheck!!.visibility == View.VISIBLE &&
            permissionsCheck!!.visibility == View.VISIBLE
        ) {
            startActivityNext?.visibility = View.VISIBLE
            startActivitySkip?.visibility = View.GONE
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

    override fun onPurchasesUpdated(p0: BillingResult, p1: MutableList<Purchase>?) {

    }

    //endregion

    //region =========================================== Lifecycle ==========================================

    override fun onRestart() {
        super.onRestart()
        activityNotificationVisible = true
        activitySuperpositionVisible = true

        if (clickSuperpositionButton)
            verifiedOverlaySettings()
    }

    //endregion
}

private class WebChromeClientCustomPoster : WebChromeClient() {
    override fun getDefaultVideoPoster(): Bitmap? {
        return Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
    }
}