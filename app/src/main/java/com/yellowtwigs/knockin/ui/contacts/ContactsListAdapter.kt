package com.yellowtwigs.knockin.ui.contacts

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.TranslateAnimation
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.ItemContactBinding
import com.yellowtwigs.knockin.ui.CircularImageView
import com.yellowtwigs.knockin.ui.edit_contact.EditContactDetailsActivity
import com.yellowtwigs.knockin.ui.groups.GroupManagerActivity
import com.yellowtwigs.knockin.utils.ContactGesture.openWhatsapp
import com.yellowtwigs.knockin.model.data.ContactWithAllInformation
import com.yellowtwigs.knockin.utils.ContactGesture.callPhone
import com.yellowtwigs.knockin.utils.ContactGesture.whatsappIsNotInstalled
import com.yellowtwigs.knockin.utils.Converter.base64ToBitmap
import com.yellowtwigs.knockin.utils.InitContactsForListAdapter
import com.yellowtwigs.knockin.utils.RandomDefaultImage.randomDefaultImage
import java.sql.DriverManager
import java.util.*

/**
 * La Classe qui permet de remplir la RecyclerView avec les bon éléments
 *
 * @author Florian Striebel, Kenzy Suon, Ryan Granet
 */
class ContactsListAdapter(private val context: Context) :
    ListAdapter<ContactWithAllInformation, ContactsListAdapter.ViewHolder>(
        ContactWithAllInformationComparator()
    ) {
    private var modeMultiSelect = false
    private var lastClick = false
    private var lastSelectMenuLen1: ConstraintLayout?

    var listOfItemSelected = ArrayList<ContactWithAllInformation>()
    var phonePermission = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemContactBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(getItem(position), context)
    }

    private fun slideUp(view: View?) {
        view!!.visibility = View.VISIBLE
        val animate = TranslateAnimation(
            0.toFloat(),  // fromXDelta
            0.toFloat(),  // toXDelta
            view.height.toFloat(),  // fromYDelta
            0.toFloat()
        ) // toYDelta
        animate.duration = 500
        animate.fillAfter = true
        view.startAnimation(animate)
    }

    inner class ViewHolder(private val binding: ItemContactBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(contactAllInfo: ContactWithAllInformation, context: Context) {
            val contact = contactAllInfo.contactDB

            if (contact != null) {
                InitContactsForListAdapter.InitContactAdapter.contactPriorityBorder(
                    contact,
                    binding.contactImage,
                    context
                )

                if (contact.profilePicture64 != "") {
                    val bitmap = base64ToBitmap(contact.profilePicture64)
                    binding.contactImage.setImageBitmap(bitmap)
                } else {
                    binding.contactImage.setImageResource(
                        randomDefaultImage(
                            contact.profilePicture,
                            context
                        )
                    )
                }

                binding.contactName.text = contact.firstName + " " + contact.lastName

                binding.apply {
                    if (modeMultiSelect) {
                        if (listOfItemSelected.contains(contactAllInfo)) {
                            binding.contactImage.setImageResource(R.drawable.ic_item_selected)
                        }
                    }

                    if (whatsappIsNotInstalled(context)) {
                        whatsappLayout.visibility = View.GONE
                    } else {
                        whatsappLayout.visibility = View.VISIBLE
                    }
                    if (contactAllInfo.getFirstMail().isEmpty()) {
                        mailLayout.visibility = View.GONE
                    } else {
                        mailLayout.visibility = View.VISIBLE
                    }
                    Log.i("phoneNumber", "${contactAllInfo.getFirstPhoneNumber()}")
                    if (contactAllInfo.getFirstPhoneNumber().isEmpty()) {
                        callLayout.visibility = View.GONE
                        smsLayout.visibility = View.GONE
                    } else {
                        callLayout.visibility = View.VISIBLE
                        smsLayout.visibility = View.VISIBLE
                    }

                    val listener = View.OnClickListener { v: View ->
                        when (v.id) {
                            smsLayout.id -> {
                                val phone = contactAllInfo.getFirstPhoneNumber()
                                context.startActivity(
                                    Intent(
                                        Intent.ACTION_SENDTO,
                                        Uri.fromParts("sms", phone, null)
                                    )
                                )
                            }
                            callLayout.id -> {
                                callPhone(contactAllInfo.getFirstPhoneNumber(), context)
                            }
                            whatsappLayout.id -> {
                                openWhatsapp(contactAllInfo.getFirstPhoneNumber(), context)
                            }
                            mailLayout.id -> {
                                val mail = getItem(position).getFirstMail()
                                val intent = Intent(Intent.ACTION_SENDTO)
                                intent.data = Uri.parse("mailto:")
                                intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(mail))
                                intent.putExtra(Intent.EXTRA_SUBJECT, "")
                                intent.putExtra(Intent.EXTRA_TEXT, "")
                                DriverManager.println(
                                    "intent " + Objects.requireNonNull(intent.extras).toString()
                                )
                                context.startActivity(intent)
                            }
                            editLayout.id -> {
                                val intent = Intent(context, EditContactDetailsActivity::class.java)
                                intent.putExtra("ContactId", contact.id)
                                context.startActivity(intent)
                            }
                            layoutRoot.id -> {
                                menu.isVisible = !menu.isVisible
                            }
                        }
                    }

//                    val longClick = OnLongClickListener { v: View? ->
//                        if (listOfItemSelected.size == 0 && len == 1 && holder.constraintLayoutMenu != null) {
//                            holder.constraintLayoutMenu!!.visibility = View.GONE
//                        }
//                        view?.tag = holder
//
//                        val contactDB = getItem(position).contactDB
//                        if (listOfItemSelected.contains(getItem(position))) {
//                            listOfItemSelected.remove(getItem(position))
//                            if (contactDB?.profilePicture64 != "") {
//                                val bitmap = contactDB?.profilePicture64?.let { base64ToBitmap(it) }
//                                holder.contactRoundedImageView.setImageBitmap(bitmap)
//                            } else {
//                                holder.contactRoundedImageView.setImageResource(
//                                    randomDefaultImage(
//                                        contactDB.profilePicture,
//                                        context
//                                    )
//                                )
//                            }
//                        } else {
//                            listOfItemSelected.add(getItem(position))
//                            if (context is GroupManagerActivity && len == 0) {
//                            } else {
//                                holder.contactRoundedImageView.setImageResource(R.drawable.ic_item_selected)
//                            }
//                        }
//                        if (context is MainActivity) {
//                            context.recyclerMultiSelectItemClick(position)
//                        }
//                        if (listOfItemSelected.size > 0) {
//                            modeMultiSelect = true
//                            lastClick = false
//                        } else {
//                            modeMultiSelect = false
//                            lastClick = true
//                        }
//                        modeMultiSelect
//                    }
//                    val listItemClick = View.OnClickListener { v: View? ->
//                        if (modeMultiSelect) {
//                            if (listOfItemSelected.size == 0 && len == 1 && holder.constraintLayoutMenu != null) {
//                                holder.constraintLayoutMenu!!.visibility = View.GONE
//                            }
//                            view?.tag = holder
//                            if (listOfItemSelected.contains(getItem(position))) {
//                                listOfItemSelected.remove(getItem(position))
//                                if (contactDB?.profilePicture64 != "") {
//                                    val bitmap =
//                                        contactDB?.profilePicture64?.let { base64ToBitmap(it) }
//                                    holder.contactRoundedImageView.setImageBitmap(bitmap)
//                                } else {
//                                    holder.contactRoundedImageView.setImageResource(
//                                        randomDefaultImage(
//                                            contactDB.profilePicture,
//                                            context
//                                        )
//                                    )
//                                }
//                            } else {
//                                listOfItemSelected.add(getItem(position))
//                                if (context is GroupManagerActivity && len == 0) {
//                                } else {
//                                    holder.contactRoundedImageView.setImageResource(R.drawable.ic_item_selected)
//                                }
//                            }
//                            if (context is MainActivity) {
//                                context.recyclerMultiSelectItemClick(position)
//                            }
//                            if (listOfItemSelected.size > 0) {
//                                modeMultiSelect = true
//                                lastClick = false
//                            } else {
//                                modeMultiSelect = false
//                                lastClick = true
//                            }
//                        } else {
//                            if (lastClick) {
//                                lastClick = false
//                            } else {
//                                if (len == 1) {
//                                    if (holder.constraintLayoutMenu != null) {
//                                        if (holder.constraintLayoutMenu!!.visibility == View.GONE) {
//                                            holder.constraintLayoutMenu!!.visibility = View.VISIBLE
//                                            slideUp(holder.constraintLayoutMenu)
//                                            if (lastSelectMenuLen1 != null) lastSelectMenuLen1!!.visibility =
//                                                View.GONE
//                                            lastSelectMenuLen1 = holder.constraintLayoutMenu
//                                        } else {
//                                            holder.constraintLayoutMenu!!.visibility = View.GONE
//                                            val slideDown =
//                                                AnimationUtils.loadAnimation(
//                                                    context,
//                                                    R.anim.slide_down
//                                                )
//                                            holder.constraintLayoutMenu!!.startAnimation(slideDown)
//                                            lastSelectMenuLen1 = null
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }

                    mailLayout.setOnClickListener(listener)
                    whatsappLayout.setOnClickListener(listener)
                    callLayout.setOnClickListener(listener)
                    smsLayout.setOnClickListener(listener)
                    editLayout.setOnClickListener(listener)
                    layoutRoot.setOnClickListener(listener)
                    callLayout.setOnLongClickListener { v: View? ->
                        val phoneNumber =
                            contactAllInfo.getSecondPhoneNumber(contactAllInfo.getFirstPhoneNumber())
                        if (phoneNumber.isNotEmpty()) {
                            callPhone(phoneNumber, context)
                        }
                        true
                    }

                    if (contact.favorite == 1) {
                        favoriteIcon.visibility = View.VISIBLE
                    } else {
                        favoriteIcon.visibility = View.INVISIBLE
                    }
                }
            }
        }
    }

    init {
        lastSelectMenuLen1 = null
    }

    class ContactWithAllInformationComparator : DiffUtil.ItemCallback<ContactWithAllInformation>() {
        override fun areItemsTheSame(
            oldItem: ContactWithAllInformation,
            newItem: ContactWithAllInformation
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: ContactWithAllInformation,
            newItem: ContactWithAllInformation
        ): Boolean {
            return oldItem.contactDB == newItem.contactDB &&
                    oldItem.contactDetailList == newItem.contactDetailList
        }
    }
}