package com.yellowtwigs.knockin.controller.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.SkuDetails
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.controller.IProductClickListener
import com.yellowtwigs.knockin.controller.activity.PremiumActivity
import com.yellowtwigs.knockin.databinding.LayoutProductItemBinding

class MyProductAdapter(
    private val cxt: PremiumActivity,
    private val billingClient: BillingClient
) : ListAdapter<SkuDetails, MyProductAdapter.ViewHolder>(SkuDetailsComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            LayoutProductItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val skuDetails = getItem(position)
        holder.onBind(skuDetails)
    }

    inner class ViewHolder(private val binding: LayoutProductItemBinding) :
        RecyclerView.ViewHolder(binding.root), IProductClickListener {
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

                val unlimitedPref =
                    cxt.getSharedPreferences("Contacts_Unlimited_Bought", modePrivate)
                val contactsUnlimitedBought =
                    unlimitedPref.getBoolean("Contacts_Unlimited_Bought", false)

                val productName = skuDetails.title
                val text = productName.split("\\(").toTypedArray()
                productItemName.text = text[0]

                checkProductNameContains(productName, jazzySoundBought)
                checkProductNameContains(productName, funkySoundBought)
                checkProductNameContains(productName, relaxationSoundBought)
                checkProductNameContains(productName, customSoundBought)
                checkProductNameContains(productName, contactsUnlimitedBought)

                if (productName.contains("Contacts") || productName.contains("Contatti") ||
                    productName.contains("Contactos")
                ) {
                    productItemImage.setImageResource(R.drawable.ic_circular_vip_icon)
                } else if (productName.contains("Jazzy") || productName.contains(
                        "jazzy"
                    )
                ) {
                    productItemImage.setImageResource(R.drawable.ic_circular_jazz_trumpet)
                } else if (productName.contains("Funky") || productName.contains(
                        "funky"
                    )
                ) {
                    productItemImage.setImageResource(R.drawable.ic_circular_music_icon)
                } else if (productName.contains("Relaxation") || productName.contains(
                        "Relajación"
                    ) || productName.contains("Relajación")
                ) {
                    productItemImage.setImageResource(R.drawable.ic_circular_relax)
                } else if (productName.contains("Custom") || productName.contains(
                        "custom"
                    )
                ) {
                    productItemImage.setImageResource(R.drawable.ic_circular_music_icon)
                }
                productItemBuyImage.icon = ResourcesCompat.getDrawable(
                    cxt.resources,
                    R.drawable.ic_buying_in_app,
                    null
                )

                setIProductClickListener { _: View?, _: Int ->
                    val billingFlowParams = BillingFlowParams.newBuilder()
                        .setSkuDetails(skuDetails)
                        .build()
                    billingClient.launchBillingFlow(cxt, billingFlowParams)
                }

                productItemBuyImage.setOnClickListener {
                    iProductClickListener?.onProductClickListener(it, adapterPosition)
                }
            }
        }

        private fun changeBackgroundToDarkGrey() {
            binding.apply {
                productItemBuyImage.apply {
                    setBackgroundColor(
                        ResourcesCompat.getColor(
                            cxt.resources, R.color.textColorDarkGrey,
                            null
                        )
                    )
                    isEnabled = false
                }
            }
        }

        private fun checkProductNameContains(productName: String, sharedPref: Boolean) {
            with(productName) {
                when {
                    contains("Contacts") && sharedPref -> {
                        changeBackgroundToDarkGrey()
                    }
                    contains("Jazzy") && sharedPref -> {
                        changeBackgroundToDarkGrey()
                    }
                    contains("Funky") && sharedPref -> {
                        changeBackgroundToDarkGrey()
                    }
                    contains("Relaxation") && sharedPref -> {
                        changeBackgroundToDarkGrey()
                    }
                    contains("Custom") && sharedPref -> {
                        changeBackgroundToDarkGrey()
                    }
                }
            }
        }

        override fun onProductClickListener(view: View?, position: Int) {

        }
    }

    class SkuDetailsComparator : DiffUtil.ItemCallback<SkuDetails>() {
        override fun areItemsTheSame(oldItem: SkuDetails, newItem: SkuDetails): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: SkuDetails, newItem: SkuDetails): Boolean {
            return oldItem.description == newItem.description &&
                    oldItem.iconUrl == newItem.iconUrl &&
                    oldItem.price == newItem.price &&
                    oldItem.title == newItem.title &&
                    oldItem.type == newItem.type
        }

    }
}