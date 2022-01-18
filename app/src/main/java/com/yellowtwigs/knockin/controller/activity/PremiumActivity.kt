package com.yellowtwigs.knockin.controller.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.Point
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.controller.activity.firstLaunch.MultiSelectActivity
import com.yellowtwigs.knockin.ui.adapters.MyProductAdapter
import com.yellowtwigs.knockin.utils.DrawerLayoutSwitch
import kotlinx.coroutines.*

class PremiumActivity : AppCompatActivity(), PurchasesUpdatedListener {

    //region ========================================= Var or Val ===========================================

    private var drawerLayout: DrawerLayout? = null

    private var billingClient: BillingClient? = null

    private var acknowledgePurchaseResponseListener: AcknowledgePurchaseResponseListener =
        AcknowledgePurchaseResponseListener { println("Purchase acknowledged") }

    private var recyclerProduct: RecyclerView? = null
    private var myProductAdapter: MyProductAdapter? = null

    private var sharedFunkySoundPreferences: SharedPreferences? = null
    private var sharedJazzySoundPreferences: SharedPreferences? = null
    private var sharedRelaxationSoundPreferences: SharedPreferences? = null
    private var sharedContactsUnlimitedPreferences: SharedPreferences? = null
    private var sharedCustomSoundPreferences: SharedPreferences? = null

    private var premium_activity_ToolbarLayout: RelativeLayout? = null
    private var premium_activity_ToolbarOpenDrawer: AppCompatImageView? = null

    private var premium_activity_ToolbarLayoutFromMultiSelect: RelativeLayout? = null
    private var premium_activity_ToolbarBackOnPressed: AppCompatImageView? = null

    private var fromManageNotification = false

    private val activity = this

    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //region ======================================== Theme Dark ========================================

        val sharedThemePreferences = getSharedPreferences("Knockin_Theme", Context.MODE_PRIVATE)
        if (sharedThemePreferences.getBoolean("darkTheme", false)) {
            setTheme(R.style.AppThemeDark)
        } else {
            setTheme(R.style.AppTheme)
        }

        //endregion

        val fromMultiSelectActivity = intent.getBooleanExtra("fromMultiSelectActivity", false)
        fromManageNotification = intent.getBooleanExtra("fromManageNotification", false)

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

        setContentView()
        setupBillingClient()

        premium_activity_ToolbarLayout = findViewById(R.id.premium_activity_toolbar_layout)
        premium_activity_ToolbarOpenDrawer = findViewById(R.id.premium_activity_toolbar_open_drawer)

        premium_activity_ToolbarLayoutFromMultiSelect = premium_activity_ToolbarLayout
        premium_activity_ToolbarBackOnPressed =
            findViewById(R.id.premium_activity_toolbar_from_multi_select_back)

        if (fromMultiSelectActivity) {
            premium_activity_ToolbarLayout!!.visibility = View.GONE
            premium_activity_ToolbarLayoutFromMultiSelect!!.visibility = View.VISIBLE
        } else {
            premium_activity_ToolbarLayout!!.visibility = View.VISIBLE
            premium_activity_ToolbarLayoutFromMultiSelect!!.visibility = View.GONE
        }

        premium_activity_ToolbarLayout!!.visibility = View.VISIBLE

        //region ================================ Call Popup from LeftDrawer ================================

        val sharedPreferencePopup = getSharedPreferences("Phone_call", Context.MODE_PRIVATE)
        val callPopupSwitch = findViewById<SwitchCompat>(R.id.settings_call_popup_switch)
        val themeSwitch = findViewById<SwitchCompat>(R.id.settings_left_drawer_theme_switch)

        if (sharedThemePreferences.getBoolean("darkTheme", false)) {
            themeSwitch.isChecked = true
        }

        if (sharedPreferencePopup.getBoolean("popup", true)) {
            callPopupSwitch.isChecked = true
        }

        //endregion

        //region ======================================= DrawerLayout =======================================

        drawerLayout = findViewById(R.id.premium_activity_drawer_layout)

        val navigationView = findViewById<NavigationView>(R.id.nav_view_premium)
        val menu = navigationView.menu
        val navItem = menu.findItem(R.id.nav_in_app)
        navItem.isChecked = true

        navigationView.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            drawerLayout!!.closeDrawers()

            when (menuItem.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this@PremiumActivity, MainActivity::class.java))
                }
                R.id.nav_informations -> startActivity(
                    Intent(
                        this@PremiumActivity,
                        EditInformationsActivity::class.java
                    )
                )
                R.id.nav_notif_config -> startActivity(
                    Intent(
                        this@PremiumActivity,
                        ManageNotificationActivity::class.java
                    )
                )
                R.id.nav_manage_screen -> {
                    startActivity(Intent(this@PremiumActivity, ManageMyScreenActivity::class.java))
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this@PremiumActivity, SettingsActivity::class.java))
                }
                R.id.nav_help -> {
                    startActivity(Intent(this@PremiumActivity, HelpActivity::class.java))
                }
            }

            true
        }

        //endregion

        //View
        recyclerProduct = findViewById(R.id.recycler_product)

        //Initialize the Handler
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
        if (!isWifiConn && !isMobileConn) {
            val popup = AlertDialog.Builder(this@PremiumActivity);
            popup.setTitle(getString(R.string.popup_connection_title));
            popup.setMessage(getString(R.string.popup_connection_message))
            popup.setPositiveButton(getString(R.string.popup_connection_cancel)) { _, _ ->
                finish()
            }
            popup.setNegativeButton(getString(R.string.popup_connection_retry)) { _, _ ->
                finish();
                startActivity(intent)
            }
            popup.create().show()
        }

        premium_activity_ToolbarOpenDrawer?.setOnClickListener {
            drawerLayout?.openDrawer(GravityCompat.START)
        }

        premium_activity_ToolbarBackOnPressed?.setOnClickListener {
            startActivity(Intent(this@PremiumActivity, MultiSelectActivity::class.java))
            finish()
        }

        DrawerLayoutSwitch.callPopupSwitch(callPopupSwitch, activity)
        DrawerLayoutSwitch.themeSwitch(themeSwitch, activity, sharedThemePreferences)
    }

    private fun setContentView() {
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val height = size.y

        when {
            height > 2500 -> setContentView(R.layout.activity_premium)
            height in 1800..2499 -> setContentView(R.layout.activity_premium)
            height in 1100..1799 -> setContentView(R.layout.activity_premium_smaller_screen)
            height < 1099 -> setContentView(R.layout.activity_premium_mini_screen)
        }
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
    }

    private fun loadProductToRecyclerView(skuDetailsList: List<SkuDetails>) {
        myProductAdapter = billingClient?.let { MyProductAdapter(this, it) }
        myProductAdapter?.submitList(null)
        myProductAdapter?.submitList(skuDetailsList)
        recyclerProduct?.adapter = myProductAdapter
        recyclerProduct?.setHasFixedSize(true)
        recyclerProduct?.layoutManager = LinearLayoutManager(this)
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
//                val mutableList = mutableListOf<String>()
//                for (purchase in listOfPurchases) {
//                    mutableList.add("PURCHASE :: ${purchase.originalJson}")
//                }
//                sendMessageWithWhatsapp("0651740903", "$mutableList")

                    for (purchase in listOfPurchases) {
                        when {
                            purchase.originalJson.contains("notifications_vip_funk_theme") -> {
                                val edit =
                                    sharedFunkySoundPreferences!!.edit()
                                edit.putBoolean("Funky_Sound_Bought", true)
                                edit.apply()
                            }
                            purchase.originalJson.contains("notifications_vip_jazz_theme") -> {
                                val edit =
                                    sharedJazzySoundPreferences!!.edit()
                                edit.putBoolean("Jazzy_Sound_Bought", true)
                                edit.apply()
                            }
                            purchase.originalJson.contains("notifications_vip_relaxation_theme") -> {
                                val edit =
                                    sharedRelaxationSoundPreferences!!.edit()
                                edit.putBoolean("Relax_Sound_Bought", true)
                                edit.apply()
                            }
                            purchase.originalJson.contains("contacts_vip_unlimited") -> {
                                val edit =
                                    sharedContactsUnlimitedPreferences!!.edit()
                                edit.putBoolean(
                                    "Contacts_Unlimited_Bought",
                                    true
                                )
                                edit.apply()
                            }
                            purchase.originalJson.contains("notifications_vip_custom_tones_theme") -> {
                                val edit =
                                    sharedCustomSoundPreferences!!.edit()
                                edit.putBoolean("Custom_Sound_Bought", true)
                                edit.apply()
                            }
                        }
                    }
                }
            }
            if (skuDetailsList != null) {
                loadProductToRecyclerView(skuDetailsList)
            }
        }
    }


    /**
     * Implement this method to get notifications for purchases updates. Both purchases initiated by
     * your app and the ones initiated outside of your app will be reported here.
     *
     *
     * **Warning!** All purchases reported here must either be consumed or acknowledged. Failure
     * to either consume (via [BillingClient.consumeAsync]) or acknowledge (via [ ][BillingClient.acknowledgePurchase]) a purchase will result in that purchase being refunded.
     * Please refer to
     * https://developer.android.com/google/play/billing/billing_library_overview#acknowledge for more
     * details.
     *
     * @param billingResult BillingResult of the update.
     * @param purchases List of updated purchases if present.
     */
    override fun onPurchasesUpdated(p0: BillingResult, purchases: MutableList<Purchase>?) {
        if (purchases != null) {
            when {
                purchases[0].originalJson.contains("notifications_vip_funk_theme") -> {
                    if (purchases[0].purchaseState == Purchase.PurchaseState.PURCHASED)
                        if (!purchases[0].isAcknowledged) {
                            val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchases[0].purchaseToken)
                            billingClient!!.acknowledgePurchase(
                                acknowledgePurchaseParams.build(),
                                acknowledgePurchaseResponseListener
                            )
                            Toast.makeText(
                                this,
                                getString(R.string.in_app_purchase_made_message),
                                Toast.LENGTH_SHORT
                            ).show()
                            val edit = sharedFunkySoundPreferences?.edit()
                            edit?.putBoolean("Funky_Sound_Bought", true)
                            edit?.apply()
                            backToManageNotifAfterBuying()
                        }
                }
                purchases[0].originalJson.contains("notifications_vip_jazz_theme") -> {
                    if (purchases[0].purchaseState == Purchase.PurchaseState.PURCHASED)
                        if (!purchases[0].isAcknowledged) {
                            val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchases[0].purchaseToken)
                            billingClient!!.acknowledgePurchase(
                                acknowledgePurchaseParams.build(),
                                acknowledgePurchaseResponseListener
                            )
                            Toast.makeText(
                                this,
                                getString(R.string.in_app_purchase_made_message),
                                Toast.LENGTH_SHORT
                            ).show()
                            val edit = sharedJazzySoundPreferences?.edit()
                            edit?.putBoolean("Jazzy_Sound_Bought", true)
                            edit?.apply()
                            backToManageNotifAfterBuying()
                        }
                }
                purchases[0].originalJson.contains("notifications_vip_relaxation_theme") -> {
                    if (purchases[0].purchaseState == Purchase.PurchaseState.PURCHASED)
                        if (!purchases[0].isAcknowledged) {
                            val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchases[0].purchaseToken)
                            billingClient!!.acknowledgePurchase(
                                acknowledgePurchaseParams.build(),
                                acknowledgePurchaseResponseListener
                            )
                            Toast.makeText(
                                this,
                                getString(R.string.in_app_purchase_made_message),
                                Toast.LENGTH_SHORT
                            ).show()
                            val edit = sharedRelaxationSoundPreferences!!.edit()
                            edit.putBoolean("Relax_Sound_Bought", true)
                            edit.apply()
                            backToManageNotifAfterBuying()
                        }
                }
                purchases[0].originalJson.contains("contacts_vip_unlimited") -> {
                    if (purchases[0].purchaseState == Purchase.PurchaseState.PURCHASED)
                        if (!purchases[0].isAcknowledged) {
                            val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchases[0].purchaseToken)
                            billingClient!!.acknowledgePurchase(
                                acknowledgePurchaseParams.build(),
                                acknowledgePurchaseResponseListener
                            )
                            Toast.makeText(
                                this,
                                getString(R.string.in_app_purchase_made_message),
                                Toast.LENGTH_SHORT
                            ).show()
                            val edit = sharedContactsUnlimitedPreferences!!.edit()
                            edit.putBoolean("Contacts_Unlimited_Bought", true)
                            edit.apply()
                        }
                }
                purchases[0].originalJson.contains("notifications_vip_custom_tones_theme") -> {
                    if (purchases[0].purchaseState == Purchase.PurchaseState.PURCHASED)
                        if (!purchases[0].isAcknowledged) {
                            val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchases[0].purchaseToken)
                            billingClient!!.acknowledgePurchase(
                                acknowledgePurchaseParams.build(),
                                acknowledgePurchaseResponseListener
                            )
                            Toast.makeText(
                                this,
                                getString(R.string.in_app_purchase_made_message),
                                Toast.LENGTH_SHORT
                            ).show()
                            val edit = sharedCustomSoundPreferences!!.edit()
                            edit.putBoolean("Custom_Sound_Bought", true)
                            edit.apply()
                            backToManageNotifAfterBuying()
                        }
                }
            }
        } else {
//            startActivity(
//                Intent(
//                    this@PremiumActivity,
//                    MainActivity::class.java
//                ).putExtra("fromMultiSelectActivity", true)
//            )
        }
    }

    fun backToManageNotifAfterBuying() {
        if (fromManageNotification) {
            MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                .setTitle(getString(R.string.in_app_purchase_made_message))
                .setMessage(getString(R.string.in_app_shop_return_to_notif))
                .setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
                    startActivity(
                        Intent(
                            this@PremiumActivity,
                            ManageNotificationActivity::class.java
                        ).putExtra("fromMultiSelectActivity", true)
                    )
                }
                .setNegativeButton(R.string.alert_dialog_later) { _, _ ->
                }
                .show()
        }
    }
}