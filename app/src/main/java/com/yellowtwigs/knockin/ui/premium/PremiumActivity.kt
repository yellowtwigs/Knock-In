package com.yellowtwigs.knockin.ui.premium

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.billingclient.api.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.ui.HelpActivity
import com.yellowtwigs.knockin.ui.settings.ManageMyScreenActivity
import com.yellowtwigs.knockin.ui.first_launch.first_vip_selection.FirstVipSelectionActivity
import com.yellowtwigs.knockin.databinding.ActivityPremiumBinding
import com.yellowtwigs.knockin.ui.contacts.list.ContactsListActivity
import com.yellowtwigs.knockin.ui.first_launch.start.ImportContactsViewModel
import com.yellowtwigs.knockin.ui.notifications.settings.NotificationsSettingsActivity
import com.yellowtwigs.knockin.ui.teleworking.TeleworkingActivity
import com.yellowtwigs.knockin.repositories.firebase.FirebaseViewModel
import com.yellowtwigs.knockin.ui.statistics.dashboard.DashboardActivity
import com.yellowtwigs.knockin.utils.SaveUserIdToFirebase.saveUserIdToFirebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class PremiumActivity : AppCompatActivity(), PurchasesUpdatedListener {

    //region ========================================= Var or Val ===========================================

    private var acknowledgePurchaseResponseListener: AcknowledgePurchaseResponseListener =
        AcknowledgePurchaseResponseListener { println("Purchase acknowledged") }

    private var sharedFunkySoundPreferences: SharedPreferences? = null
    private var sharedJazzySoundPreferences: SharedPreferences? = null
    private var sharedRelaxationSoundPreferences: SharedPreferences? = null
    private var sharedContactsUnlimitedPreferences: SharedPreferences? = null
    private var sharedCustomSoundPreferences: SharedPreferences? = null
    private var appsSupportPref: SharedPreferences? = null

    private var fromManageNotification = false

    private val activity = this

    private lateinit var binding: ActivityPremiumBinding
    private lateinit var billingClient: BillingClient
    private lateinit var myProductAdapter: MyProductAdapter
    private lateinit var params: SkuDetailsParams.Builder

    private val importContactsViewModel: ImportContactsViewModel by viewModels()
    private val firebaseViewModel: FirebaseViewModel by viewModels()

    private lateinit var userIdPreferences: SharedPreferences

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

        userIdPreferences = getSharedPreferences("User_Id", Context.MODE_PRIVATE)

        saveUserIdToFirebase(userIdPreferences, firebaseViewModel, "Enter the Premium Activity")

        fromManageNotification = intent.getBooleanExtra("fromManageNotification", false)

        sharedFunkySoundPreferences = getSharedPreferences("Funky_Sound_Bought", Context.MODE_PRIVATE)
        sharedJazzySoundPreferences = getSharedPreferences("Jazzy_Sound_Bought", Context.MODE_PRIVATE)
        sharedRelaxationSoundPreferences = getSharedPreferences("Relax_Sound_Bought", Context.MODE_PRIVATE)
        sharedContactsUnlimitedPreferences = getSharedPreferences("Contacts_Unlimited_Bought", Context.MODE_PRIVATE)
        sharedCustomSoundPreferences = getSharedPreferences("Custom_Sound_Bought", Context.MODE_PRIVATE)
        appsSupportPref = getSharedPreferences("Apps_Support_Bought", Context.MODE_PRIVATE)

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

            val menu = navViewPremium.menu
            val navItem = menu.findItem(R.id.nav_in_app)
            navItem.isChecked = true

            navViewPremium.setNavigationItemSelectedListener { menuItem ->
                if (menuItem.itemId != R.id.nav_sync_contact && menuItem.itemId != R.id.nav_invite_friend) {
                    menuItem.isChecked = true
                }
                drawerLayout.closeDrawers()

                when (menuItem.itemId) {
                    R.id.nav_home -> {
                        startActivity(
                            Intent(
                                this@PremiumActivity, ContactsListActivity::class.java
                            )
                        )
                    }
                    R.id.nav_notifications -> startActivity(
                        Intent(
                            this@PremiumActivity, NotificationsSettingsActivity::class.java
                        )
                    )
                    R.id.nav_teleworking -> startActivity(
                        Intent(this@PremiumActivity, TeleworkingActivity::class.java)
                    )
                    R.id.nav_dashboard -> startActivity(
                        Intent(this@PremiumActivity, DashboardActivity::class.java)
                    )
                    R.id.nav_manage_screen -> {
                        startActivity(
                            Intent(
                                this@PremiumActivity, ManageMyScreenActivity::class.java
                            )
                        )
                    }
                    R.id.nav_help -> {
                        startActivity(Intent(this@PremiumActivity, HelpActivity::class.java))
                    }
                    R.id.nav_sync_contact -> {
                        importContacts()
                    }
                    R.id.nav_invite_friend -> {
                        val intent = Intent(Intent.ACTION_SEND)
                        val messageString = resources.getString(R.string.invite_friend_text) + " \n" + resources.getString(
                            R.string.location_on_playstore
                        )
                        intent.putExtra(Intent.EXTRA_TEXT, messageString)
                        intent.type = "text/plain"
                        val messageIntent = Intent.createChooser(intent, null)
                        startActivity(messageIntent)
                    }
                }

                true
            }
        }
    }

    private fun importContacts() {
        CoroutineScope(Dispatchers.Default).launch {
            importContactsViewModel.syncAllContactsInDatabase(contentResolver)
        }
    }

    private fun toolbarClick() {
        binding.apply {
            openDrawer.setOnClickListener {
                drawerLayout.openDrawer(GravityCompat.START)
            }

            backPressedIcon.setOnClickListener {
                startActivity(Intent(this@PremiumActivity, FirstVipSelectionActivity::class.java))
                finish()
            }

        }
    }

    private fun loadProductToRecyclerView(skuDetailsList: List<SkuDetails>) {
        binding.recyclerProduct.apply {
            layoutManager = LinearLayoutManager(activity)
            myProductAdapter = MyProductAdapter(activity, billingClient) {
                saveUserIdToFirebase(userIdPreferences, firebaseViewModel, it)
            }
            adapter = myProductAdapter
            myProductAdapter.submitList(skuDetailsList)
        }
    }

    private fun setupBillingClient() {
        billingClient = BillingClient.newBuilder(this).setListener(this).enablePendingPurchases().build()

        connectToGooglePlayBilling()
    }

    private fun connectToGooglePlayBilling() {
        try {
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
        } catch (e: Exception) {
            Log.e("Exception", "$e")
        }
    }

    private fun querySkuDetails() {
        val skuList = ArrayList<String>()
        skuList.add("contacts_vip_unlimited")
        skuList.add("notifications_vip_funk_theme")
        skuList.add("notifications_vip_jazz_theme")
        skuList.add("notifications_vip_relaxation_theme")
        skuList.add("additional_applications_support")

        params = SkuDetailsParams.newBuilder().setSkusList(skuList).setType(BillingClient.SkuType.INAPP)

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
                    if (purchases[0].purchaseState == Purchase.PurchaseState.PURCHASED) if (!purchases[0].isAcknowledged) {
                        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchases[0].purchaseToken)
                        billingClient.acknowledgePurchase(
                            acknowledgePurchaseParams.build(), acknowledgePurchaseResponseListener
                        )
                        Toast.makeText(
                            this, getString(R.string.in_app_purchase_made_message), Toast.LENGTH_SHORT
                        ).show()
                        val edit = sharedFunkySoundPreferences?.edit()
                        edit?.putBoolean("Funky_Sound_Bought", true)
                        edit?.apply()
                        backToManageNotifAfterBuying()
                    }
                }
                purchases[0].originalJson.contains("notifications_vip_jazz_theme") -> {
                    if (purchases[0].purchaseState == Purchase.PurchaseState.PURCHASED) if (!purchases[0].isAcknowledged) {
                        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchases[0].purchaseToken)
                        billingClient.acknowledgePurchase(
                            acknowledgePurchaseParams.build(), acknowledgePurchaseResponseListener
                        )
                        Toast.makeText(
                            this, getString(R.string.in_app_purchase_made_message), Toast.LENGTH_SHORT
                        ).show()
                        val edit = sharedJazzySoundPreferences?.edit()
                        edit?.putBoolean("Jazzy_Sound_Bought", true)
                        edit?.apply()
                        backToManageNotifAfterBuying()
                    }
                }
                purchases[0].originalJson.contains("notifications_vip_relaxation_theme") -> {
                    if (purchases[0].purchaseState == Purchase.PurchaseState.PURCHASED) if (!purchases[0].isAcknowledged) {
                        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchases[0].purchaseToken)
                        billingClient.acknowledgePurchase(
                            acknowledgePurchaseParams.build(), acknowledgePurchaseResponseListener
                        )
                        Toast.makeText(
                            this, getString(R.string.in_app_purchase_made_message), Toast.LENGTH_SHORT
                        ).show()
                        val edit = sharedRelaxationSoundPreferences!!.edit()
                        edit.putBoolean("Relax_Sound_Bought", true)
                        edit.apply()
                        backToManageNotifAfterBuying()
                    }
                }
                purchases[0].originalJson.contains("contacts_vip_unlimited") -> {
                    if (purchases[0].purchaseState == Purchase.PurchaseState.PURCHASED) if (!purchases[0].isAcknowledged) {
                        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchases[0].purchaseToken)
                        billingClient.acknowledgePurchase(
                            acknowledgePurchaseParams.build(), acknowledgePurchaseResponseListener
                        )
                        Toast.makeText(
                            this, getString(R.string.in_app_purchase_made_message), Toast.LENGTH_SHORT
                        ).show()
                        val edit = sharedContactsUnlimitedPreferences!!.edit()
                        edit.putBoolean("Contacts_Unlimited_Bought", true)
                        edit.apply()
                    }
                }
                purchases[0].originalJson.contains("notifications_vip_custom_tones_theme") -> {
                    if (purchases[0].purchaseState == Purchase.PurchaseState.PURCHASED) if (!purchases[0].isAcknowledged) {
                        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchases[0].purchaseToken)
                        billingClient.acknowledgePurchase(
                            acknowledgePurchaseParams.build(), acknowledgePurchaseResponseListener
                        )
                        Toast.makeText(
                            this, getString(R.string.in_app_purchase_made_message), Toast.LENGTH_SHORT
                        ).show()
                        val edit = sharedCustomSoundPreferences!!.edit()
                        edit.putBoolean("Custom_Sound_Bought", true)
                        edit.apply()
                        backToManageNotifAfterBuying()
                    }
                }
                purchases[0].originalJson.contains("additional_applications_support") -> {
                    if (purchases[0].purchaseState == Purchase.PurchaseState.PURCHASED) if (!purchases[0].isAcknowledged) {
                        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchases[0].purchaseToken)
                        billingClient.acknowledgePurchase(
                            acknowledgePurchaseParams.build(), acknowledgePurchaseResponseListener
                        )
                        Toast.makeText(
                            this, getString(R.string.in_app_purchase_made_message), Toast.LENGTH_SHORT
                        ).show()
                        val edit = appsSupportPref?.edit()
                        edit?.putBoolean("Apps_Support_Bought", true)
                        edit?.apply()
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
            MaterialAlertDialogBuilder(this, R.style.AlertDialog).setTitle(getString(R.string.in_app_purchase_made_message))
                .setMessage(getString(R.string.in_app_shop_return_to_notif)).setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
                    startActivity(
                        Intent(
                            this@PremiumActivity, NotificationsSettingsActivity::class.java
                        ).putExtra("fromMultiSelectActivity", true)
                    )
                }.setNegativeButton(R.string.alert_dialog_later) { _, _ ->
                }.show()
        }
    }

    //endregion

}