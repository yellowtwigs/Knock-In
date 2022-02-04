package com.yellowtwigs.knockin.controller.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.billingclient.api.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.controller.activity.firstLaunch.MultiSelectActivity
import com.yellowtwigs.knockin.databinding.ActivityPremiumBinding
import com.yellowtwigs.knockin.ui.adapters.MyProductAdapter
import com.yellowtwigs.knockin.utils.DrawerLayoutSwitch.checkSwitchFromLeftDrawer
import kotlinx.coroutines.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class PremiumActivity : AppCompatActivity(), PurchasesUpdatedListener {

    //region ========================================= Var or Val ===========================================

    private var acknowledgePurchaseResponseListener: AcknowledgePurchaseResponseListener =
        AcknowledgePurchaseResponseListener { println("Purchase acknowledged") }

    private var sharedFunkySoundPreferences: SharedPreferences? = null
    private var sharedJazzySoundPreferences: SharedPreferences? = null
    private var sharedRelaxationSoundPreferences: SharedPreferences? = null
    private var sharedContactsUnlimitedPreferences: SharedPreferences? = null
    private var sharedCustomSoundPreferences: SharedPreferences? = null

    private var fromManageNotification = false

    private val activity = this

    private lateinit var binding: ActivityPremiumBinding
    private lateinit var billingClient: BillingClient
    private lateinit var myProductAdapter: MyProductAdapter
    private lateinit var params: SkuDetailsParams.Builder

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

        binding = ActivityPremiumBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupBillingClient()

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
        setupLeftDrawerLayout(sharedThemePreferences)
        toolbarClick()
    }

    //region ========================================== Functions ===========================================

    private fun setupLeftDrawerLayout(sharedThemePreferences: SharedPreferences) {
        binding.apply {
            if (intent.getBooleanExtra("fromMultiSelectActivity", false)) {
                premiumActivityToolbarLayout.visibility = View.GONE
                premiumActivityToolbarFromMultiSelectLayout.visibility = View.VISIBLE
            } else {
                premiumActivityToolbarLayout.visibility = View.VISIBLE
                premiumActivityToolbarFromMultiSelectLayout.visibility = View.GONE
            }
            checkSwitchFromLeftDrawer(activity, sharedThemePreferences)

            val menu = navViewPremium.menu
            val navItem = menu.findItem(R.id.nav_in_app)
            navItem.isChecked = true

            navViewPremium.setNavigationItemSelectedListener { menuItem ->
                menuItem.isChecked = true
                drawerLayout.closeDrawers()

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
                        startActivity(
                            Intent(
                                this@PremiumActivity,
                                ManageMyScreenActivity::class.java
                            )
                        )
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
        }
    }

    private fun toolbarClick() {
        binding.apply {
            openDrawer.setOnClickListener {
                drawerLayout.openDrawer(GravityCompat.START)
            }

            backPressedIcon.setOnClickListener {
                startActivity(Intent(this@PremiumActivity, MultiSelectActivity::class.java))
                finish()
            }

        }
    }

    private fun loadProductToRecyclerView(skuDetailsList: List<SkuDetails>) {
        binding.recyclerProduct.apply {
            layoutManager = LinearLayoutManager(activity)
            myProductAdapter = MyProductAdapter(activity, billingClient)
            adapter = myProductAdapter
            myProductAdapter.submitList(skuDetailsList)
        }
    }

    private fun getAllSkuDetails(skuDetailsList: List<SkuDetails>) {
//        CoroutineScope(Dispatchers.Main).launch {
//            launchProgressBarSpin(3000)
//        }
    }

    private suspend fun launchProgressBarSpin(time: Long) {
        binding.apply {
            progressBar.visibility = View.VISIBLE
            recyclerProduct.visibility = View.INVISIBLE
            delay(time)
            progressBar.visibility = View.INVISIBLE
            recyclerProduct.visibility = View.VISIBLE
        }
    }

    private fun setupBillingClient() {
        billingClient = BillingClient.newBuilder(this)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        connectToGooglePlayBilling()
    }

    private fun connectToGooglePlayBilling() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    querySkuDetails()
                } else {
                    connectToGooglePlayBilling()
                }
            }

            override fun onBillingServiceDisconnected() {
            }
        })
    }

    private fun querySkuDetails() {
        val skuList = ArrayList<String>()
        skuList.add("contacts_vip_unlimited")
        skuList.add("notifications_vip_funk_theme")
        skuList.add("notifications_vip_jazz_theme")
        skuList.add("notifications_vip_relaxation_theme")

        params = SkuDetailsParams.newBuilder()
            .setSkusList(skuList)
            .setType(BillingClient.SkuType.INAPP)

        billingClient.querySkuDetailsAsync(params.build()) { billingResult, skuDetailsList ->
            runOnUiThread {
                loadProductToRecyclerView(skuDetailsList!!)
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
                            billingClient.acknowledgePurchase(
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
                            billingClient.acknowledgePurchase(
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
                            billingClient.acknowledgePurchase(
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
                            billingClient.acknowledgePurchase(
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
                            billingClient.acknowledgePurchase(
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

    //endregion

}