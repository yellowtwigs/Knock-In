package com.yellowtwigs.knockin.controller.adapter

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
import com.yellowtwigs.knockin.controller.IProductClickListener
import com.yellowtwigs.knockin.controller.activity.PremiumActivity
import com.yellowtwigs.knockin.databinding.LayoutProductItemBinding

class MyProductAdapter(
    private val activity: PremiumActivity,
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
                val sharedNotifJazzySoundInAppPreferences =
                    activity.getSharedPreferences(
                        "Notif_Jazzy_Sound_IsBought",
                        Context.MODE_PRIVATE
                    )
                val notifJazzySoundIsBought =
                    sharedNotifJazzySoundInAppPreferences.getBoolean(
                        "Notif_Jazzy_Sound_IsBought",
                        false
                    )
                val sharedNotifFunkySoundInAppPreferences =
                    activity.getSharedPreferences(
                        "Notif_Funky_Sound_IsBought",
                        Context.MODE_PRIVATE
                    )
                val notifFunkySoundIsBought =
                    sharedNotifFunkySoundInAppPreferences.getBoolean(
                        "Notif_Funky_Sound_IsBought",
                        false
                    )
                val sharedNotifRelaxationSoundInAppPreferences = activity.getSharedPreferences(
                    "Notif_Relaxation_Sound_IsBought",
                    Context.MODE_PRIVATE
                )
                val notifRelaxationSoundIsBought =
                    sharedNotifRelaxationSoundInAppPreferences.getBoolean(
                        "Notif_Relaxation_Sound_IsBought",
                        false
                    )
                val sharedAlarmNotifInAppPreferences = activity.getSharedPreferences(
                    "Alarm_Contacts_Unlimited_IsBought",
                    Context.MODE_PRIVATE
                )
                val contactsUnlimitedIsBought =
                    sharedAlarmNotifInAppPreferences.getBoolean(
                        "Alarm_Contacts_Unlimited_IsBought",
                        false
                    )
                val sharedNotifCustomSoundInAppPreferences = activity.getSharedPreferences(
                    "Notif_Custom_Sound_IsBought",
                    Context.MODE_PRIVATE
                )
                val notifCustomSoundIsBought =
                    sharedNotifCustomSoundInAppPreferences.getBoolean(
                        "Notif_Custom_Sound_IsBought",
                        false
                    )

                val productName = skuDetails.title
                val text = productName.split("\\(").toTypedArray()
                productItemName.text = text[0]

                //        myViewHolder.myProductPrice.setText(skuDetailsList.get(i).getPrice());
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
                    activity.resources,
                    R.drawable.ic_buying_in_app,
                    null
                )

                with (productName) {
                    when{
                        contains("Contacts") && contactsUnlimitedIsBought -> {
                            changeBackgroundToDarkGrey()
                        }
                        contains("Jazzy") && notifJazzySoundIsBought -> {
                            changeBackgroundToDarkGrey()
                        }
                        contains("Funky") && notifFunkySoundIsBought -> {
                            changeBackgroundToDarkGrey()
                        }
                        contains("Relaxation") && notifRelaxationSoundIsBought -> {
                            changeBackgroundToDarkGrey()
                        }
                        contains("Custom") && notifCustomSoundIsBought -> {
                            changeBackgroundToDarkGrey()
                        }
                    }
                }

                setIProductClickListener { _: View?, _: Int ->
                    val billingFlowParams = BillingFlowParams.newBuilder()
                        .setSkuDetails(skuDetails)
                        .build()
                    billingClient.launchBillingFlow(activity, billingFlowParams)
                }

                productItemBuyImage.setOnClickListener {
                    iProductClickListener?.onProductClickListener(it, adapterPosition)
                }
            }
        }

        fun changeBackgroundToDarkGrey() {
            binding.apply {
                productItemBuyImage.apply {
                    setBackgroundColor(
                        ResourcesCompat.getColor(
                            activity.resources, R.color.textColorDarkGrey,
                            null
                        )
                    )
                    isEnabled = false
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