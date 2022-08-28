package com.yellowtwigs.knockin.ui.first_launch.start

import android.Manifest
import android.app.ActivityManager
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.android.billingclient.api.*
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.ActivityStartActivityBinding
import com.yellowtwigs.knockin.ui.first_launch.first_vip_selection.FirstVipSelectionActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.collections.ArrayList
import kotlin.math.abs

@AndroidEntryPoint
class StartActivity : AppCompatActivity(), PurchasesUpdatedListener {

    //region ========================================== Val or Var ==========================================

    private lateinit var radioButton1: AppCompatRadioButton
    private lateinit var radioButton2: AppCompatRadioButton
    private lateinit var radioButton3: AppCompatRadioButton
    private lateinit var radioButton4: AppCompatRadioButton

    private var currentPosition = 0

    //endregion

    private val importContactsViewModel: ImportContactsViewModel by viewModels()

    private lateinit var binding: ActivityStartActivityBinding
    private lateinit var importContactPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStartActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBillingClient()

        importContactPreferences = getSharedPreferences("Import_Contact", Context.MODE_PRIVATE)

        //region ======================================= FindViewById =======================================

        radioButton1 = findViewById(R.id.radio_button_1)
        radioButton2 = findViewById(R.id.radio_button_2)
        radioButton3 = findViewById(R.id.radio_button_3)
        radioButton4 = findViewById(R.id.radio_button_4)

        setSliderContainer()

        //endregion

        val arraylistPermission = ArrayList<String>()
        arraylistPermission.add(Manifest.permission.READ_CONTACTS)
        arraylistPermission.add(Manifest.permission.SEND_SMS)
        arraylistPermission.add(Manifest.permission.CALL_PHONE)

        if (!importContactPreferences.getBoolean("Import_Contact", false)) {
            ActivityCompat.requestPermissions(
                this,
                arraylistPermission.toArray(arrayOfNulls<String>(arraylistPermission.size)),
                ALL_PERMISSIONS
            )
        }

        binding.activateButton.setOnClickListener {
            when (currentPosition) {
                0 -> {
                    activateNotificationsClick()
                }
                1 -> {
                    openOverlaySettings()
                }
                2 -> {
                    val sharedFirstLaunch = getSharedPreferences("First_Launch", Context.MODE_PRIVATE)
                    val edit = sharedFirstLaunch.edit()
                    edit.putBoolean("First_Launch", true)
                    edit.apply()
                    startActivity(Intent(this, FirstVipSelectionActivity::class.java))
                    finish()
                }
            }
        }
    }

    //region ========================================== Functions ===========================================

    private fun setSliderContainer() {
//        if (checkIfGoEdition()) {
//
//            MaterialAlertDialogBuilder(this, R.style.AlertDialog)
//                .setBackground(getDrawable(R.color.backgroundColor))
//                .setMessage(getString(R.string.start_activity_go_edition_message))
//                .setPositiveButton(R.string.start_activity_go_edition_positive_button) { _, _ ->
//                }
//                .show()
//        }

        val viewPager = findViewById<ViewPager2>(R.id.view_pager)
        val sliderItems = arrayListOf<SliderItem>()
        sliderItems.add(SliderItem(R.drawable.carrousel_4))
        sliderItems.add(SliderItem(R.drawable.carrousel_3))
        sliderItems.add(SliderItem(R.drawable.carrousel_1))

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

            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)

                    currentPosition = position

                    when (position) {
                        0 -> {
                            binding.title.text =
                                getString(R.string.start_activity_notification_title)
                            binding.subtitle.text =
                                getString(R.string.start_activity_notification_subtitle)

                            radioButton1.isChecked = true
                            radioButton2.isChecked = false
                            radioButton3.isChecked = false
                            radioButton4.isChecked = false
                        }
                        1 -> {
                            binding.title.text = getString(R.string.superposition_title)
                            binding.subtitle.text = getString(R.string.superposition_subtitle)

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
                            binding.title.text = "VIP"
                            binding.subtitle.text = ""
                            binding.activateButton.text = "Next"

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
        val sharedPreferences = getSharedPreferences("Overlay_Preferences", Context.MODE_PRIVATE)
        val edit: SharedPreferences.Editor = sharedPreferences.edit()
        edit.putBoolean("Overlay_Preferences", true)
        edit.apply()

        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivity(intent)
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

        if (requestCode == ALL_PERMISSIONS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                CoroutineScope(Dispatchers.Main).launch {
                    importContactsViewModel.syncAllContactsInDatabase(contentResolver)
                }

                val edit = importContactPreferences.edit()
                edit.putBoolean("Import_Contact", true)
                edit.apply()

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
    }

    override fun onBackPressed() {
    }

    private fun activateNotificationsClick() {
        startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
        val intentFilter = IntentFilter()
        intentFilter.addAction("com.yellowtwigs.Knockin.notificationExemple")
    }

    companion object {
        const val ALL_PERMISSIONS = 1
    }

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
}