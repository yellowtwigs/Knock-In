package com.yellowtwigs.knockin.controller.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.controller.activity.firstLaunch.MultiSelectActivity
import com.yellowtwigs.knockin.controller.adapter.MyProductAdapter

class PremiumActivity : AppCompatActivity(), PurchasesUpdatedListener {

    //region ========================================= Var or Val ===========================================

    private var drawerLayout: DrawerLayout? = null

    private var billingClient: BillingClient? = null
    private var recyclerProduct: RecyclerView? = null
    private var premium_activity_ToolbarLoadProducts: MaterialButton? = null
    private var sharedProductNotifFunkySoundPreferences: SharedPreferences? = null
    private var sharedProductNotifJazzySoundPreferences: SharedPreferences? = null
    private var sharedProductContactsUnlimitedPreferences: SharedPreferences? = null

    private var premium_activity_ToolbarLayout: RelativeLayout? = null
    private var premium_activity_ToolbarOpenDrawer: AppCompatImageView? = null

    private var premium_activity_ToolbarLayoutFromMultiSelect: RelativeLayout? = null
    private var premium_activity_ToolbarBackOnPressed: AppCompatImageView? = null

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

        setContentView(R.layout.activity_premium)

        setupBillingClient()

        val fromMultiSelectActivity = intent.getBooleanExtra("fromMultiSelectActivity", false)

        sharedProductNotifFunkySoundPreferences = this.getSharedPreferences("Notif_Funky_Sound_IsBought", Context.MODE_PRIVATE)
        sharedProductNotifJazzySoundPreferences = this.getSharedPreferences("Notif_Jazzy_Sound_IsBought", Context.MODE_PRIVATE)
        sharedProductNotifJazzySoundPreferences = this.getSharedPreferences("Notif_Relaxation_Sound_IsBought", Context.MODE_PRIVATE)
        sharedProductContactsUnlimitedPreferences = this.getSharedPreferences("Alarm_Contacts_Unlimited_IsBought", Context.MODE_PRIVATE)

        premium_activity_ToolbarLayout = findViewById(R.id.premium_activity_toolbar_layout)
        premium_activity_ToolbarOpenDrawer = findViewById(R.id.premium_activity_toolbar_open_drawer)

        premium_activity_ToolbarLayoutFromMultiSelect = findViewById(R.id.premium_activity_toolbar_layout)
        premium_activity_ToolbarBackOnPressed = findViewById(R.id.premium_activity_toolbar_from_multi_select_back)

        premium_activity_ToolbarLoadProducts = findViewById(R.id.premium_activity_toolbar_load_products)

        if (fromMultiSelectActivity) {
            premium_activity_ToolbarLayout!!.visibility = View.GONE
            premium_activity_ToolbarLayoutFromMultiSelect!!.visibility = View.VISIBLE
        } else {
            premium_activity_ToolbarLayout!!.visibility = View.VISIBLE
            premium_activity_ToolbarLayoutFromMultiSelect!!.visibility = View.GONE
        }

        premium_activity_ToolbarLayout!!.visibility = View.VISIBLE


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
                R.id.nav_informations -> startActivity(Intent(this@PremiumActivity, EditInformationsActivity::class.java))
                R.id.nav_notif_config -> startActivity(Intent(this@PremiumActivity, ManageNotificationActivity::class.java))
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
        recyclerProduct!!.setHasFixedSize(true)
        recyclerProduct!!.layoutManager = LinearLayoutManager(this)

        //Event
        premium_activity_ToolbarLoadProducts!!.setOnClickListener {
            if (billingClient!!.isReady) {
                val list = listOf("contacts_vip_unlimited") + listOf("notifications_vip_funk_theme")+ listOf("notifications_vip_jazz_theme") +
                        listOf("notifications_vip_relaxation_theme")
                val params = SkuDetailsParams.newBuilder()
                        .setSkusList(list)
                        .setType(BillingClient.SkuType.INAPP)
                        .build()

                billingClient!!.querySkuDetailsAsync(params) { billingResult, skuDetailsList ->
                    if (billingResult!!.responseCode == BillingClient.BillingResponseCode.OK) {
                        loadProductToRecyclerView(skuDetailsList)
                        premium_activity_ToolbarLoadProducts!!.visibility = View.GONE
                    } else {
//                        Toast.makeText(this@PremiumActivity, "Cannot query product", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
//                Toast.makeText(this@PremiumActivity, "Billing client not ready", Toast.LENGTH_SHORT).show()
            }

        }

        premium_activity_ToolbarOpenDrawer!!.setOnClickListener {
            drawerLayout!!.openDrawer(GravityCompat.START)
        }

        premium_activity_ToolbarBackOnPressed!!.setOnClickListener {
            startActivity(Intent(this@PremiumActivity, MultiSelectActivity::class.java))
            finish()
        }
    }

    private fun loadProductToRecyclerView(skuDetailsList: List<SkuDetails>) {
        val adapter = MyProductAdapter(this, skuDetailsList, billingClient)
        recyclerProduct!!.adapter = adapter
    }

    private fun setupBillingClient() {
        billingClient = BillingClient.newBuilder(this)
                .enablePendingPurchases()
                .setListener(this)
                .build()

        billingClient!!.startConnection(object : BillingClientStateListener {
            /**
             * Called to notify that setup is complete.
             *
             * @param billingResult The [BillingResult] which returns the status of the setup process.
             */
            override fun onBillingSetupFinished(billingResult: BillingResult?) {
                if (billingResult!!.responseCode == BillingClient.BillingResponseCode.OK)
//                    Toast.makeText(this@PremiumActivity, "Success to connect Billing", Toast.LENGTH_SHORT).show()
                else
                    Toast.makeText(this@PremiumActivity, "" + billingResult.responseCode, Toast.LENGTH_SHORT).show()
            }

            override fun onBillingServiceDisconnected() {
//                Toast.makeText(this@PremiumActivity, "You are disconnected from Billing", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun refreshActivity() {
        startActivity(Intent(this@PremiumActivity, PremiumActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
        finish()
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
    override fun onPurchasesUpdated(billingResult: BillingResult?, purchases: MutableList<Purchase>?) {
        if (purchases != null) {

            when {
                purchases[0].originalJson.contains("notifications_vip_funk_theme") -> {
                    Toast.makeText(this, getString(R.string.in_app_purchase_made_message), Toast.LENGTH_SHORT).show()
                    val edit = sharedProductNotifFunkySoundPreferences!!.edit()
                    edit.putBoolean("Notif_Funky_Sound_IsBought", true)
                    edit.apply()
                    refreshActivity()
                }
                purchases[0].originalJson.contains("notifications_vip_jazz_theme") -> {
                    Toast.makeText(this, getString(R.string.in_app_purchase_made_message), Toast.LENGTH_SHORT).show()
                    val edit = sharedProductNotifJazzySoundPreferences!!.edit()
                    edit.putBoolean("Notif_Jazzy_Sound_IsBought", true)
                    edit.apply()
                    refreshActivity()
                }
                purchases[0].originalJson.contains("notifications_vip_relaxation_theme") -> {
                    Toast.makeText(this, getString(R.string.in_app_purchase_made_message), Toast.LENGTH_SHORT).show()
                    val edit = sharedProductNotifJazzySoundPreferences!!.edit()
                    edit.putBoolean("Notif_Relaxation_Sound_IsBought", true)
                    edit.apply()
                    refreshActivity()
                }
                purchases[0].originalJson.contains("contacts_vip_unlimited") -> {
                    Toast.makeText(this, getString(R.string.in_app_purchase_made_message), Toast.LENGTH_SHORT).show()
                    val edit = sharedProductContactsUnlimitedPreferences!!.edit()
                    edit.putBoolean("Alarm_Contacts_Unlimited_IsBought", true)
                    edit.apply()
                    refreshActivity()
                }
            }
        }
    }
}