package com.yellowtwigs.knockin.ui.adapters

import android.app.Activity
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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.controller.activity.EditContactActivity
import com.yellowtwigs.knockin.controller.activity.MainActivity
import com.yellowtwigs.knockin.controller.activity.group.GroupManagerActivity
import com.yellowtwigs.knockin.databinding.ListContactItemLayoutBinding
import com.yellowtwigs.knockin.model.ModelDB.ContactWithAllInformation
import com.yellowtwigs.knockin.utils.ContactGesture.callPhone
import com.yellowtwigs.knockin.utils.ContactGesture.isWhatsappInstalled
import com.yellowtwigs.knockin.utils.ContactGesture.openWhatsapp
import com.yellowtwigs.knockin.utils.ConvertBitmap.base64ToBitmap
import com.yellowtwigs.knockin.utils.InitContactAdapter.contactPriorityBorder
import com.yellowtwigs.knockin.utils.RandomDefaultImage.randomDefaultImage
import java.sql.DriverManager
import java.util.*

/**
 * La Classe qui permet de remplir la RecyclerView avec les bon éléments
 *
 * @author Florian Striebel, Kenzy Suon, Ryan Granet
 */
class ContactListAdapter(
    private val cxt: Activity, private val len: Int
) : ListAdapter<ContactWithAllInformation, ContactListAdapter.ViewHolder>(
    ContactWithAllInformationComparator()
) {

    private var modeMultiSelect = false
    private var lastClick = false
    private var lastSelectMenuLen1: ConstraintLayout? = null

    var listOfItemSelected = ArrayList<ContactWithAllInformation>()
    var phonePermission = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ListContactItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = getItem(position)
        Log.i("recyclerView", "${contact}")
        holder.onBind(contact, position)
    }

    inner class ViewHolder(private val binding: ListContactItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind(contactWithAllInformation: ContactWithAllInformation, position: Int) {
            binding.apply {
                val contact = contactWithAllInformation.contactDB
                Log.i("recyclerView", "${contact}")
                if (contact != null) {
                    contactPriorityBorder(contact, contactImage, cxt)

                    if (contact.profilePicture64 != "") {
                        val bitmap = base64ToBitmap(contact.profilePicture64)
                        contactImage.setImageBitmap(bitmap)
                    } else {
                        contactImage.setImageResource(randomDefaultImage(contact.profilePicture, cxt))
                    }

                    firstName.text = "${contact.firstName} ${contact.lastName}"
                    val firstGroup = contactWithAllInformation.getFirstGroup(cxt)
                    if (cxt is GroupManagerActivity) {
                        if (len == 0) {
                            contactImage.visibility = View.INVISIBLE
                        }
                    }

                    if (firstGroup == null) {
                        val theme = cxt.getSharedPreferences("Knockin_Theme", Context.MODE_PRIVATE)
                        if (theme.getBoolean("darkTheme", false)) {
                            val roundedLayout = cxt.getDrawable(R.drawable.rounded_rectangle_group)
                            roundedLayout?.setColorFilter(
                                ResourcesCompat.getColor(
                                    cxt.resources,
                                    R.color.backgroundColorDark,
                                    null
                                ), PorterDuff.Mode.MULTIPLY
                            )
                        } else {
                            val roundedLayout = cxt.getDrawable(R.drawable.rounded_rectangle_group)
                            roundedLayout?.setColorFilter(
                                ResourcesCompat.getColor(
                                    cxt.resources,
                                    R.color.backgroundColor,
                                    null
                                ), PorterDuff.Mode.MULTIPLY
                            )
                        }
                    } else {
                        val roundedLayout = cxt.getDrawable(R.drawable.rounded_rectangle_group)
                        roundedLayout?.setColorFilter(
                            firstGroup.section_color,
                            PorterDuff.Mode.MULTIPLY
                        )
                    }

                    if (modeMultiSelect) {
                        if (listOfItemSelected.contains(contactWithAllInformation)) {
                            if (cxt is GroupManagerActivity && len == 0) {
                            } else {
                                contactImage.setImageResource(R.drawable.ic_item_selected)
                            }
                        }
                    }

                    val listener = View.OnClickListener { v: View ->
                        when (v.id) {
                            this.listContactItemConstraintSms.id -> {
                                val phone = getItem(position).getFirstPhoneNumber()
                                cxt.startActivity(
                                    Intent(
                                        Intent.ACTION_SENDTO,
                                        Uri.fromParts("sms", phone, null)
                                    )
                                )
                            }
                            this.listContactItemConstraintCall.id -> {
                                callPhone(contactWithAllInformation.getFirstPhoneNumber(), cxt)
                            }
                            this.listContactItemConstraintWhatsapp.id -> {
                                openWhatsapp(
                                    contactWithAllInformation.getFirstPhoneNumber(),
                                    cxt
                                )
                            }
                            this.listContactItemConstraintMail.id -> {
                                val mail = getItem(position).getFirstMail()
                                val intent = Intent(Intent.ACTION_SENDTO)
                                intent.data = Uri.parse("mailto:")
                                intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(mail))
                                intent.putExtra(Intent.EXTRA_SUBJECT, "")
                                intent.putExtra(Intent.EXTRA_TEXT, "")
                                DriverManager.println(
                                    "intent " + Objects.requireNonNull(intent.extras).toString()
                                )
                                cxt.startActivity(intent)
                            }
                        }

                        if (len == 1) {
                            if (v.id == this.listContactItemConstraintEdit.id) {
                                val intent = Intent(cxt, EditContactActivity::class.java)
                                intent.putExtra("ContactId", contact.id)
                                intent.putExtra("position", position)
                                cxt.startActivity(intent)
                            }
                        }
                    }

                    val longClick = OnLongClickListener { v: View? ->
                        if (listOfItemSelected.size == 0 && len == 1) {
                            this.listContactItemMenu.visibility = View.GONE
                        }
                        if (listOfItemSelected.contains(contactWithAllInformation)) {
                            listOfItemSelected.remove(contactWithAllInformation)
                            if (contact.profilePicture64 != "") {
                                val bitmap = base64ToBitmap(contact.profilePicture64)
                                contactImage.setImageBitmap(bitmap)
                            } else {
                                contactImage.setImageResource(
                                    randomDefaultImage(
                                        contact.profilePicture, cxt
                                    )
                                )
                            }
                        } else {
                            listOfItemSelected.add(contactWithAllInformation)
                            if (cxt is GroupManagerActivity && len == 0) {
                            } else {
                                contactImage.setImageResource(R.drawable.ic_item_selected)
                            }
                        }
                        if (cxt is MainActivity) {
                            cxt.recyclerMultiSelectItemClick(position)
                        }
                        if (listOfItemSelected.size > 0) {
                            modeMultiSelect = true
                            lastClick = false
                        } else {
                            modeMultiSelect = false
                            lastClick = true
                        }
                        modeMultiSelect
                    }
                    val listItemClick = View.OnClickListener { v: View? ->
                        if (modeMultiSelect) {
                            if (listOfItemSelected.size == 0 && len == 1) {
                                this.listContactItemMenu.visibility = View.GONE
                            }
                            if (listOfItemSelected.contains(contactWithAllInformation)) {
                                listOfItemSelected.remove(contactWithAllInformation)
                                if (contact.profilePicture64 != "") {
                                    val bitmap = base64ToBitmap(contact.profilePicture64)
                                    contactImage.setImageBitmap(bitmap)
                                } else {
                                    contactImage.setImageResource(
                                        randomDefaultImage(
                                            contact.profilePicture, cxt
                                        )
                                    )
                                }
                            } else {
                                listOfItemSelected.add(contactWithAllInformation)
                                if (cxt is GroupManagerActivity && len == 0) {
                                } else {
                                    contactImage.setImageResource(R.drawable.ic_item_selected)
                                }
                            }
                            if (cxt is MainActivity) {
                                cxt.recyclerMultiSelectItemClick(position)
                            }
                            if (listOfItemSelected.size > 0) {
                                modeMultiSelect = true
                                lastClick = false
                            } else {
                                modeMultiSelect = false
                                lastClick = true
                            }
                        } else {
                            if (lastClick) {
                                lastClick = false
                            } else {
                                if (len == 1) {
                                    if (this.listContactItemMenu != null) {
                                        if (this.listContactItemMenu.visibility == View.GONE) {
                                            this.listContactItemMenu.visibility = View.VISIBLE
                                            slideUp(this.listContactItemMenu)
                                            if (lastSelectMenuLen1 != null) lastSelectMenuLen1!!.visibility =
                                                View.GONE
                                            lastSelectMenuLen1 = this.listContactItemMenu
                                        } else {
                                            this.listContactItemMenu.visibility = View.GONE
                                            val slideDown =
                                                AnimationUtils.loadAnimation(
                                                    cxt,
                                                    R.anim.slide_down
                                                )
                                            this.listContactItemMenu.startAnimation(slideDown)
                                            lastSelectMenuLen1 = null
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (len == 1) {
                        if (!isWhatsappInstalled(cxt) || contact.hasWhatsapp == 0) {
                            this.listContactItemConstraintWhatsapp.visibility = View.GONE
                        } else {
                            this.listContactItemConstraintWhatsapp.visibility = View.VISIBLE
                        }
                        if (getItem(position).getFirstMail().isEmpty()) {
                            this.listContactItemConstraintMail.visibility = View.GONE
                        } else {
                            this.listContactItemConstraintMail.visibility = View.VISIBLE
                        }
                        if (getItem(position).getFirstPhoneNumber().isEmpty()) {
                            this.listContactItemConstraintCall.visibility = View.GONE
                            this.listContactItemConstraintSms.visibility = View.GONE
                        } else {
                            this.listContactItemConstraintCall.visibility = View.VISIBLE
                            this.listContactItemConstraintSms.visibility = View.VISIBLE
                        }
                    }

                    root.setOnLongClickListener(longClick)
                    root.setOnClickListener(listItemClick)

                    this.listContactItemConstraintMail.setOnClickListener(listener)
                    this.listContactItemConstraintWhatsapp.setOnClickListener(listener)
                    this.listContactItemConstraintCall.setOnClickListener(listener)
                    this.listContactItemConstraintSms.setOnClickListener(listener)
                    this.listContactItemConstraintEdit.setOnClickListener(listener)
                    this.listContactItemConstraintCall.setOnLongClickListener { v: View? ->
                        val phoneNumber =
                            getItem(position).getSecondPhoneNumber(getItem(position).getFirstPhoneNumber())
                        if (phoneNumber.isNotEmpty()) {
                            callPhone(phoneNumber, cxt)
                        }
                        true
                    }

                    if (contact.favorite == 1) {
                        this.favoriteShine.visibility = View.VISIBLE
                    } else {
                        this.favoriteShine.visibility = View.GONE
                    }
                }
            }
        }
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

    private fun slideUp(view: View?) {
        view?.visibility = View.VISIBLE
        val animate = view?.height?.toFloat()?.let {
            TranslateAnimation(
                0.toFloat(),  // fromXDelta
                0.toFloat(),  // toXDelta
                it,  // fromYDelta
                0.toFloat()
            )
        } // toYDelta
        animate?.duration = 500
        animate?.fillAfter = true
        view?.startAnimation(animate)
    }
}