package com.yellowtwigs.knockin.controller.activity

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.*
import com.yellowtwigs.knockin.controller.adapter.MyProductAdapter
import com.yellowtwigs.knockin.R

class PremiumActivity : AppCompatActivity(), PurchasesUpdatedListener {

    private var billingClient: BillingClient? = null
    private var recyclerProduct: RecyclerView? = null
    private var sharedProductIsBoughtPreferences: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_premium)

        setupBillingClient()

        sharedProductIsBoughtPreferences = this.getSharedPreferences("IsBought", Context.MODE_PRIVATE)

        //View
        recyclerProduct = findViewById(R.id.recycler_product)
        recyclerProduct!!.setHasFixedSize(true)
        recyclerProduct!!.layoutManager = LinearLayoutManager(this)

        //Event
        if (billingClient!!.isReady) {
            val list = listOf("contacts_vip_unlimited") + listOf("custom_notifications_sound")
            val params = SkuDetailsParams.newBuilder()
                    .setSkusList(list)
                    .setType(BillingClient.SkuType.INAPP)
                    .build()

            billingClient!!.querySkuDetailsAsync(params) { billingResult, skuDetailsList ->
                if (billingResult!!.responseCode == BillingClient.BillingResponseCode.OK) {
                    loadProductToRecyclerView(skuDetailsList)
                } else {
                    Toast.makeText(this@PremiumActivity, "Cannot query product", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this@PremiumActivity, "Billing client not ready", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this@PremiumActivity, "Success to connect Billing", Toast.LENGTH_SHORT).show()
                else
                    Toast.makeText(this@PremiumActivity, "" + billingResult.responseCode, Toast.LENGTH_SHORT).show()
            }

            override fun onBillingServiceDisconnected() {
                Toast.makeText(this@PremiumActivity, "You are disconnected from Billing", Toast.LENGTH_SHORT).show()
            }
        })
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
            Toast.makeText(this, "Purchases item: " + purchases.size, Toast.LENGTH_LONG).show()
            Toast.makeText(this, "Purchases item: " + purchases[0], Toast.LENGTH_LONG).show()

            val edit = sharedProductIsBoughtPreferences!!.edit()
            edit.putBoolean("IsBought", true)
            edit.apply()
        }
    }
}