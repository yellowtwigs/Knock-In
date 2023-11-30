package com.yellowtwigs.knockin.premium

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.SkuDetails
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.LayoutProductItemBinding

class MyProductAdapter(
    private val cxt: PremiumActivity,
    private val billingClient: BillingClient,
    private val onClickedCallback: (String) -> Unit,
) : ListAdapter<SkuDetails, MyProductAdapter.ViewHolder>(SkuDetailsComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LayoutProductItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val skuDetails = getItem(position)
        holder.onBind(skuDetails)
    }

    inner class ViewHolder(private val binding: LayoutProductItemBinding) : RecyclerView.ViewHolder(binding.root), IProductClickListener {
        var iProductClickListener: IProductClickListener? = null

        @JvmName("setIProductClickListener1")
        fun setIProductClickListener(iProductClick: IProductClickListener?) {
            iProductClickListener = iProductClick
        }

        fun onBind(skuDetails: SkuDetails) {
            binding.apply {
                val modePrivate = Context.MODE_PRIVATE
                val jazzyPref = cxt.getSharedPreferences("Jazzy_Sound_Bought", modePrivate)
                val jazzySoundBought = jazzyPref.getBoolean("Jazzy_Sound_Bought", false)

                val funkyPref = cxt.getSharedPreferences("Funky_Sound_Bought", modePrivate)
                val funkySoundBought = funkyPref.getBoolean("Funky_Sound_Bought", false)

                val relaxPref = cxt.getSharedPreferences("Relax_Sound_Bought", modePrivate)
                val relaxationSoundBought = relaxPref.getBoolean("Relax_Sound_Bought", false)

                val customPref = cxt.getSharedPreferences("Custom_Sound_Bought", modePrivate)
                val customSoundBought = customPref.getBoolean("Custom_Sound_Bought", false)

                val appsSupportPref = cxt.getSharedPreferences("Apps_Support_Bought", modePrivate)
                val appsSupportBought = appsSupportPref.getBoolean("Apps_Support_Bought", false)

                val unlimitedPref = cxt.getSharedPreferences("Contacts_Unlimited_Bought", modePrivate)
                val contactsUnlimitedBought = unlimitedPref.getBoolean("Contacts_Unlimited_Bought", false)

                val codeFakePref = cxt.getSharedPreferences("Produits_Fake_Bought", modePrivate)
                val codeFakeBought = codeFakePref.getBoolean("Produits_Fake_Bought", false)

                val skuDetailsTitle = skuDetails.title
                val titleTransformed = skuDetailsTitle.split("\\(").toTypedArray()
                val productName = titleTransformed[0].split("(").toTypedArray()[0]
                productItemName.text = productName

                with(productName) {
                    when {
                        contains("Jazzy") || contains("jazzy") || contains("Джазовый") -> {
                            productItemImage.setImageResource(R.drawable.ic_circular_jazz_trumpet)

                            if (jazzySoundBought) {
                                changeBackgroundToDarkGrey()
                            }
                        }

                        contains("Funky") || contains("funky") || contains("Веселый") -> {
                            productItemImage.setImageResource(R.drawable.ic_circular_music_icon)

                            if (funkySoundBought) {
                                changeBackgroundToDarkGrey()
                            }
                        }

                        contains("Relaxation") || contains("Relajación") || contains("relaxation") || contains("relajación") || contains("Entspannungs") || contains(
                            "Relax"
                        ) || contains("Relaxamento") || contains("Расслабление") -> {
                            productItemImage.setImageResource(R.drawable.ic_circular_relax)

                            if (relaxationSoundBought) {
                                changeBackgroundToDarkGrey()
                            }
                        }

                        contains("Custom") || contains("custom") -> {
                            productItemImage.setImageResource(R.drawable.ic_circular_music_icon)

                            if (customSoundBought) {
                                changeBackgroundToDarkGrey()
                            }
                        }

                        contains("VIP") || contains("vip") -> {
                            productItemImage.setImageResource(R.drawable.ic_circular_vip_icon)

                            if (contactsUnlimitedBought) {
                                changeBackgroundToDarkGrey()
                            }
                        }

                        contains("Support") || contains("Supporta") || contains("Suporta") || contains("Поддержка") || contains("Unterstützt") || contains(
                            "support"
                        ) -> {
                            productItemImage.setImageResource(R.drawable.ic_social_media)

                            if (appsSupportBought) {
                                changeBackgroundToDarkGrey()
                            }
                        }

                        contains("Test") || contains("Promo") -> {
                            productItemImage.setImageResource(R.drawable.ic_speedometer_light_green)

                            if (codeFakeBought) {
                                changeBackgroundToDarkGrey()
                            }
                        }
                    }
                }

                productItemBuyImage.icon = ResourcesCompat.getDrawable(
                    cxt.resources, R.drawable.ic_buying_in_app, null
                )

                setIProductClickListener { _: View?, _: Int ->
                    val billingFlowParams = BillingFlowParams.newBuilder().setSkuDetails(skuDetails).build()
                    billingClient.launchBillingFlow(cxt, billingFlowParams)
                }

                productItemBuyImage.setOnClickListener {
                    onClickedCallback("Try to buy $productName")
                    iProductClickListener?.onProductClickListener(it, adapterPosition)
                }

                root.setOnClickListener {
                    Toast.makeText(cxt, skuDetails.description + " " + skuDetails.price, Toast.LENGTH_LONG).show()
                }
            }
        }

        private fun changeBackgroundToDarkGrey() {
            binding.apply {
                productItemBuyImage.apply {
                    setBackgroundColor(
                        ResourcesCompat.getColor(
                            cxt.resources, R.color.textColorDarkGrey, null
                        )
                    )
                    isEnabled = false
                }
            }
        }

        override fun onProductClickListener(view: View?, position: Int) {}
    }

    class SkuDetailsComparator : DiffUtil.ItemCallback<SkuDetails>() {
        override fun areItemsTheSame(oldItem: SkuDetails, newItem: SkuDetails): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: SkuDetails, newItem: SkuDetails): Boolean {
            return oldItem.description == newItem.description && oldItem.iconUrl == newItem.iconUrl && oldItem.price == newItem.price && oldItem.title == newItem.title && oldItem.type == newItem.type
        }
    }
}