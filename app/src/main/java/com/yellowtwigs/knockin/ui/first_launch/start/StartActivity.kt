package com.yellowtwigs.knockin.ui.first_launch.start

import android.Manifest
import android.app.ActivityManager
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.android.billingclient.api.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.ActivityStartActivityBinding
import com.yellowtwigs.knockin.ui.first_launch.ImportContactsActivity
import com.yellowtwigs.knockin.ui.first_launch.MultiSelectActivity
import com.yellowtwigs.knockin.utils.ContactGesture.isWhatsappInstalled
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.collections.ArrayList
import kotlin.math.abs

@AndroidEntryPoint
class StartActivity : AppCompatActivity(), PurchasesUpdatedListener {

    //region ========================================== Val or Var ==========================================

    private var activateNotificationsButton: MaterialButton? = null
    private var activateNotificationsLayout: RelativeLayout? = null
    private var activateNotificationsLoading: ProgressBar? = null
    private var activateNotificationsCheck: AppCompatImageView? = null

    private var superpositionButton: MaterialButton? = null
    private var superpositionLayout: RelativeLayout? = null
    private var superpositionLoading: ProgressBar? = null
    private var superpositionCheck: AppCompatImageView? = null

    private var startActivityNext: MaterialButton? = null
    private var startActivitySkip: MaterialButton? = null

    private var activityNotificationVisible = false

    private var activitySuperpositionVisible = false
    private var clickSuperpositionButton = false

    private lateinit var radioButton1: AppCompatRadioButton
    private lateinit var radioButton2: AppCompatRadioButton
    private lateinit var radioButton3: AppCompatRadioButton
    private lateinit var radioButton4: AppCompatRadioButton

    //endregion

    val importContactsViewModel: ImportContactsViewModel by viewModels()

    private lateinit var binding: ActivityStartActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStartActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBillingClient()

        //region ======================================= FindViewById =======================================

        activateNotificationsButton =
            findViewById(R.id.activate_notifications_button)
        superpositionButton = findViewById(R.id.superposition_button)

        activateNotificationsLayout =
            findViewById(R.id.start_activity_notifications_layout)
        superpositionLayout =
            findViewById(R.id.start_activity_superposition_layout)

        startActivityNext = findViewById(R.id.start_activity_next)
        startActivitySkip = findViewById(R.id.start_activity_skip)
        activateNotificationsLoading =
            findViewById(R.id.start_activity_activate_notifications_loading)
        superpositionLoading =
            findViewById(R.id.superposition_loading)
        activateNotificationsCheck =
            findViewById(R.id.activate_notifications_check)
        superpositionCheck =
            findViewById(R.id.superposition_check)

        radioButton1 = findViewById(R.id.radio_button_1)
        radioButton2 = findViewById(R.id.radio_button_2)
        radioButton3 = findViewById(R.id.radio_button_3)
        radioButton4 = findViewById(R.id.radio_button_4)

        setSliderContainer()

        val start_activity_layout = findViewById<ConstraintLayout>(R.id.start_activity_layout)
        val video_skip = findViewById<MaterialButton>(R.id.video_skip)

        val layoutSize = Point()
        val displayScreen = windowManager.defaultDisplay
        displayScreen.getRealSize(layoutSize)

        val sharedPreferences: SharedPreferences =
            getSharedPreferences("Knockin_preferences", Context.MODE_PRIVATE)
        val edit: SharedPreferences.Editor = sharedPreferences.edit()
        edit.putBoolean("popupNotif", true)
        edit.apply()

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


        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_CONTACTS),
            ImportContactsActivity.REQUEST_CODE_READ_CONTACT
        )

        val arraylistPermission = ArrayList<String>()
        arraylistPermission.add(Manifest.permission.SEND_SMS)
        arraylistPermission.add(Manifest.permission.CALL_PHONE)
        ActivityCompat.requestPermissions(
            this,
            arraylistPermission.toArray(arrayOfNulls<String>(arraylistPermission.size)),
            REQUEST_CODE_SMS_AND_CALL
        )

        //region ======================================== Listeners =========================================

        video_skip.setOnClickListener {
            findViewById<ViewPager2>(R.id.view_pager).visibility = View.INVISIBLE
            video_skip.visibility = View.INVISIBLE
            start_activity_layout.visibility = View.VISIBLE
            radioButton1.visibility = View.INVISIBLE
            radioButton2.visibility = View.INVISIBLE
            radioButton3.visibility = View.INVISIBLE
            radioButton4.visibility = View.INVISIBLE

            Toast.makeText(
                this,
                getString(R.string.start_activity_video_tutorial_skip_text),
                Toast.LENGTH_LONG
            ).show()
        }


        val importWhatsappPreferences =
            getSharedPreferences("importWhatsappPreferences", Context.MODE_PRIVATE)
        importWhatsappPreferences.edit()
            .putBoolean("importWhatsappPreferences", isWhatsappInstalled(this))
        importWhatsappPreferences.edit().apply()

//        activateNotificationsButton?.setOnClickListener {
//            activateNotificationsClick()
//            activateNotificationsButton?.visibility = View.INVISIBLE
//            activateNotificationsLoading?.visibility = View.VISIBLE
//
//            val SPLASH_DISPLAY_LENGHT = 2000
//
//            val displayLoading = Runnable {
//                activateNotificationsLoading?.visibility = View.VISIBLE
//            }
//            runOnUiThread(displayLoading)
//            val verifiedNotification = Thread {
//                activityNotificationVisible = false
//                while (!activityNotificationVisible) {
//                }
//                if (isNotificationServiceEnabled()) {
//                    val displayLoading = Runnable {
//                        Handler().postDelayed({
//                            activateNotificationsLoading?.visibility = View.INVISIBLE
//                            activateNotificationsCheck?.visibility = View.VISIBLE
//                            val sharedPreferences: SharedPreferences =
//                                getSharedPreferences("Knockin_preferences", Context.MODE_PRIVATE)
//                            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
//                                val edit: SharedPreferences.Editor = sharedPreferences.edit()
//                                edit.putBoolean("serviceNotif", true)
//                                edit.putBoolean("popupNotif", true)
//                                edit.apply()
//                            } else {
//                                val edit: SharedPreferences.Editor = sharedPreferences.edit()
//                                edit.putBoolean("serviceNotif", true)
//                                edit.putBoolean("popupNotif", false)
//                                edit.apply()
//                            }
//                            allIsChecked()
//                        }, SPLASH_DISPLAY_LENGHT.toLong())
//                    }
//                    runOnUiThread(displayLoading)
//                } else {
//                    val displayLoading = Runnable {
//                        activateNotificationsLoading?.visibility = View.INVISIBLE
//                        activateNotificationsButton?.visibility = View.VISIBLE
//                    }
//                    runOnUiThread(displayLoading)
//                }
//            }
//            verifiedNotification.start()
//        }

        superpositionButton?.setOnClickListener {
            if (clickSuperpositionButton) {
                verifiedOverlaySettings()
            } else {
                clickSuperpositionButton = true
                openOverlaySettings()
            }
        }

//        startActivityNext?.setOnClickListener {
//            if (!checkIfGoEdition()) {
//                buildMultiSelectAlertDialog()
//            } else {
//                val intent = Intent(this@StartActivity, Main2Activity::class.java)
//                intent.putExtra("fromStartActivity", true)
//                startActivity(intent)
//                finish()
//            }
//        }

//        startActivitySkip?.setOnClickListener {
//            if (!checkIfGoEdition()) {
//                if (importContactsCheck?.isVisible == true) {
//                    buildMultiSelectAlertDialog()
//                } else {
//                    buildLeaveAlertDialog()
//                }
//            } else {
//                val intent = Intent(this@StartActivity, Main2Activity::class.java)
//                intent.putExtra("fromStartActivity", true)
//                startActivity(intent)
//                finish()
//            }
//        }

        //endregion
    }

    //region ========================================== Functions ==========================================

    private fun setSliderContainer() {
        val viewPager = findViewById<ViewPager2>(R.id.view_pager)
        val sliderItems = arrayListOf<SliderItem>()
        sliderItems.add(SliderItem(R.drawable.carrousel_1))
        sliderItems.add(SliderItem(R.drawable.carrousel_2))
        sliderItems.add(SliderItem(R.drawable.carrousel_3))
        sliderItems.add(SliderItem(R.drawable.carrousel_4))
        val sliderAdapter = SliderAdapter(sliderItems)

        viewPager.apply {
            adapter = sliderAdapter
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 3
            getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER

            val compositePageTransformer = CompositePageTransformer()
            compositePageTransformer.addTransformer(MarginPageTransformer(30))
            compositePageTransformer.addTransformer { page, position ->
                val r = 1 - abs(position)
                page.scaleY = 0.85f + r * 0.25f
            }

            setPageTransformer(compositePageTransformer)

            var currentPosition = 0

            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)

                    when (position) {
                        0 -> {
                            radioButton1.isChecked = true
                            radioButton2.isChecked = false
                            radioButton3.isChecked = false
                            radioButton4.isChecked = false
                        }
                        1 -> {
                            radioButton1.isChecked = false
                            radioButton2.isChecked = true
                            radioButton3.isChecked = false
                            radioButton4.isChecked = false
                        }
                        2 -> {
                            radioButton1.isChecked = false
                            radioButton2.isChecked = false
                            radioButton3.isChecked = true
                            radioButton4.isChecked = false
                        }
                        3 -> {
                            radioButton1.isChecked = false
                            radioButton2.isChecked = false
                            radioButton3.isChecked = false
                            radioButton4.isChecked = true
                        }
                    }
                }
            })
        }

    }

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

    //region =========================================== BILLING ============================================

    private fun setupBillingClient() {
        val billingClient = BillingClient.newBuilder(this)
            .setListener(this)
            .enablePendingPurchases()
            .build()
        connectToGooglePlayBilling(billingClient)
    }

    private fun connectToGooglePlayBilling(billingClient: BillingClient) {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    querySkuDetails(billingClient)
                } else {
                    Log.i("playBilling", "Fail")
                }
            }

            override fun onBillingServiceDisconnected() {
                connectToGooglePlayBilling(billingClient)
            }
        })
    }

    private fun querySkuDetails(billingClient: BillingClient) {
        val skuList = ArrayList<String>()
        skuList.add("contacts_vip_unlimited")
        skuList.add("notifications_vip_funk_theme")
        skuList.add("notifications_vip_jazz_theme")
        skuList.add("notifications_vip_relaxation_theme")
        skuList.add("additional_applications_support")

        val params = SkuDetailsParams.newBuilder()
            .setSkusList(skuList)
            .setType(BillingClient.SkuType.INAPP)

        billingClient.querySkuDetailsAsync(
            params.build()
        ) { _, _ ->
            billingClient.queryPurchasesAsync(
                BillingClient.SkuType.INAPP
            ) { _, listOfPurchases ->
                if (listOfPurchases.isNotEmpty()) {
                    fillSharedPreferencesWithListPurchases(listOfPurchases)
                }
            }
        }
    }

    private fun fillSharedPreferencesWithListPurchases(listOfPurchases: MutableList<Purchase>) {
        for (purchase in listOfPurchases) {
            purchase.originalJson.apply {
                when {
                    contains("notifications_vip_funk_theme") -> {
                        sharedPreferencesConfiguration("Funky_Sound_Bought")
                    }
                    contains("notifications_vip_jazz_theme") -> {
                        sharedPreferencesConfiguration("Jazzy_Sound_Bought")
                    }
                    contains("notifications_vip_relaxation_theme") -> {
                        sharedPreferencesConfiguration("Relax_Sound_Bought")
                    }
                    contains("contacts_vip_unlimited") -> {
                        sharedPreferencesConfiguration("Contacts_Unlimited_Bought")
                    }
                    contains("additional_applications_support") -> {
                        sharedPreferencesConfiguration("Apps_Support_Bought")
                    }
                }
            }
        }
    }

    private fun sharedPreferencesConfiguration(sharedPreferencesName: String) {
        val private = Context.MODE_PRIVATE

        val sharedPreferences = getSharedPreferences(sharedPreferencesName, private)
        val edit = sharedPreferences.edit()
        edit.putBoolean(sharedPreferencesName, true)
        edit.apply()
    }

    //endregion

    private fun checkIfGoEdition(): Boolean {
        val am = baseContext.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        return am.isLowRamDevice
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_READ_CONTACT) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                CoroutineScope(Dispatchers.IO).launch {
                    importContactsViewModel.syncAllContactsInDatabase(contentResolver)
                }
//                Toast.makeText(this, R.string.import_contacts_toast, Toast.LENGTH_LONG).show()

//                val sync = Runnable {
//                    ContactManager(this).getAllContactsInfoSync(contentResolver)
//
//                    val sharedPreferencesSync =
//                        getSharedPreferences("save_last_sync", Context.MODE_PRIVATE)
//                    var index = 1
//                    var stringSet = listOf<String>()
//                    if (sharedPreferencesSync.getStringSet(index.toString(), null) != null)
//                        stringSet =
//                            sharedPreferencesSync.getStringSet(index.toString(), null)!!.sorted()
//                    arrayListOf<Pair<ContactDB, List<ContactDetailDB>>>()
//                    while (sharedPreferencesSync.getStringSet(
//                            index.toString(),
//                            null
//                        ) != null && stringSet.isNotEmpty()
//                    ) {
//                        stringSet =
//                            sharedPreferencesSync.getStringSet(index.toString(), null)!!.sorted()
//                        index++
//                    }
//
//                    val runnable = Runnable {
//                        importContactsLoading?.visibility = View.INVISIBLE
//                        importContactsCheck?.visibility = View.VISIBLE
//                        allIsChecked()
//                        allIsCheckedGOEdition()
//                    }
//                    runOnUiThread(runnable)
//                }
            }
        }

        if (REQUEST_CODE_SMS_AND_CALL == requestCode) {
        }
    }

    override fun onBackPressed() {
    }

    private fun activateNotificationsClick() {
        startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
        val intentFilter = IntentFilter()
        intentFilter.addAction("com.yellowtwigs.Knockin.notificationExemple")
    }

    companion object {
        const val REQUEST_CODE_READ_CONTACT = 2
        const val REQUEST_CODE_SMS_AND_CALL = 5
    }

//    private fun buildMultiSelectAlertDialog(): AlertDialog {
//        return MaterialAlertDialogBuilder(this, R.style.AlertDialog)
//            .setBackground(getDrawable(R.color.backgroundColor))
//            .setTitle(getString(R.string.notification_alert_dialog_title))
//            .setMessage(getString(R.string.notification_alert_dialog_message))
//            .setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
//                startActivity(Intent(this@StartActivity, MultiSelectActivity::class.java))
//                val sharedPreferences: SharedPreferences =
//                    getSharedPreferences("Knockin_preferences", Context.MODE_PRIVATE)
//                val edit: SharedPreferences.Editor = sharedPreferences.edit()
//                edit.putBoolean("view", true)
//                edit.apply()
//                closeContextMenu()
//                finish()
//            }
//            .setNegativeButton(R.string.alert_dialog_later)
//            { _, _ ->
//                closeContextMenu()
//                val intent = Intent(this@StartActivity, Main2Activity::class.java)
//                intent.putExtra("fromStartActivity", true)
//                startActivity(intent)
//                finish()
//            }
//            .show()
//    }

//    private fun buildLeaveAlertDialog(): AlertDialog {
//        val message = if (importContactsButton?.visibility == View.VISIBLE) {
//            getString(R.string.start_activity_skip_alert_dialog_message_importation)
//        } else {
//            getString(R.string.start_activity_skip_alert_dialog_message)
//        }
//
//        return MaterialAlertDialogBuilder(this, R.style.AlertDialog)
//            .setBackground(getDrawable(R.color.backgroundColor))
//            .setTitle(getString(R.string.start_activity_skip_alert_dialog_title))
//            .setMessage(message)
//            .setPositiveButton(R.string.start_activity_skip_alert_dialog_positive_button) { _, _ ->
//                val intent = Intent(this@StartActivity, Main2Activity::class.java)
//                intent.putExtra("fromStartActivity", true)
//                startActivity(intent)
//                val sharedPreferences: SharedPreferences =
//                    getSharedPreferences("Knockin_preferences", Context.MODE_PRIVATE)
//                val edit: SharedPreferences.Editor = sharedPreferences.edit()
//                edit.putBoolean("view", true)
//                edit.apply()
//                closeContextMenu()
//            }
//            .setNegativeButton(R.string.alert_dialog_cancel)
//            { _, _ ->
//                closeContextMenu()
//            }
//            .show()
//    }

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