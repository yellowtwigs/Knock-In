package com.yellowtwigs.knockin.ui.contacts

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PorterDuff
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.TranslateAnimation
import android.widget.HorizontalScrollView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.ui.CircularImageView
import com.yellowtwigs.knockin.ui.edit_contact.EditContactDetailsActivity
import com.yellowtwigs.knockin.ui.group.GroupManagerActivity
import com.yellowtwigs.knockin.utils.ContactGesture.openWhatsapp
import com.yellowtwigs.knockin.model.ContactManager
import com.yellowtwigs.knockin.model.data.ContactWithAllInformation
import com.yellowtwigs.knockin.utils.ContactGesture
import com.yellowtwigs.knockin.utils.ContactGesture.callPhone
import com.yellowtwigs.knockin.utils.ContactGesture.goToSignal
import com.yellowtwigs.knockin.utils.ContactGesture.goToTelegram
import com.yellowtwigs.knockin.utils.ContactGesture.isWhatsappInstalled
import com.yellowtwigs.knockin.utils.Converter.base64ToBitmap
import com.yellowtwigs.knockin.utils.EveryActivityUtils.getAppOnPhone
import com.yellowtwigs.knockin.utils.RandomDefaultImage.randomDefaultImage
import java.sql.DriverManager
import java.util.*
import kotlin.collections.ArrayList

/**
 * La Classe qui permet de remplir la RecyclerView avec les bon éléments
 *
 * @author Florian Striebel, Kenzy Suon, Ryan Granet
 */
class ContactRecyclerViewAdapter(
    private val context: Context,
    private var contactManager: ContactManager,
    private val len: Int
) : RecyclerView.Adapter<ContactRecyclerViewAdapter.ContactViewHolder>() {
    private val listContacts: List<ContactWithAllInformation>
    private var view: View? = null
    private var modeMultiSelect = false
    private var lastClick = false
    private var lastSelectMenuLen1: HorizontalScrollView?
    private lateinit var listApp: ArrayList<String>

    var listOfItemSelected = ArrayList<ContactWithAllInformation>()
    var phonePermission = ""
        private set

    fun getItem(position: Int): ContactWithAllInformation {
        return listContacts[position]
    }

    fun setGestionnaireContact(gestionnaireContact: ContactManager) {
        contactManager = gestionnaireContact
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        listApp = getAppOnPhone(context as MainActivity)
        view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_contact_item_layout, parent, false)
        return ContactViewHolder(view!!)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = getItem(position).contactDB!!
        when (contact.contactPriority) {
            0 -> {
                holder.contactRoundedImageView.setBorderColor(
                    context.resources.getColor(
                        R.color.priorityZeroColor,
                        null
                    )
                )
            }
            1 -> {
                holder.contactRoundedImageView.setBorderColor(
                    context.resources.getColor(
                        R.color.transparentColor,
                        null
                    )
                )
            }
            2 -> {
                holder.contactRoundedImageView.setBorderColor(
                    context.resources.getColor(
                        R.color.priorityTwoColor,
                        null
                    )
                )
            }
        }
        if (contact.profilePicture64 != "") {
            val bitmap = base64ToBitmap(contact.profilePicture64)
            holder.contactRoundedImageView.setImageBitmap(bitmap)
        } else {
            holder.contactRoundedImageView.setImageResource(
                randomDefaultImage(
                    contact.profilePicture,
                    context
                )
            )
        }
        val contactName = contact.firstName + " " + contact.lastName
        holder.contactFirstNameView.text = contactName
        val firstGroup = getItem(position).getFirstGroup(context)
        if (context is GroupManagerActivity) {
            if (len == 0) {
                holder.contactRoundedImageView.visibility = View.INVISIBLE
            }
        }
        if (firstGroup == null) {
            val sharedThemePreferences =
                context.getSharedPreferences("Knockin_Theme", Context.MODE_PRIVATE)
            if (sharedThemePreferences.getBoolean("darkTheme", false)) {
                val roundedLayout = context.getDrawable(R.drawable.rounded_rectangle_group)
                roundedLayout!!.setColorFilter(
                    context.resources.getColor(
                        R.color.backgroundColorDark,
                        null
                    ), PorterDuff.Mode.MULTIPLY
                )
            } else {
                val roundedLayout = context.getDrawable(R.drawable.rounded_rectangle_group)
                roundedLayout!!.setColorFilter(
                    context.resources.getColor(
                        R.color.backgroundColor,
                        null
                    ), PorterDuff.Mode.MULTIPLY
                )
            }
        } else {
            val roundedLayout = context.getDrawable(R.drawable.rounded_rectangle_group)!!
            roundedLayout.setColorFilter(firstGroup.section_color, PorterDuff.Mode.MULTIPLY)
        }
        if (modeMultiSelect) {
            if (listOfItemSelected.contains(contactManager.contactList[position])) {
                if (context is GroupManagerActivity && len == 0) {
                } else {
                    holder.contactRoundedImageView.setImageResource(R.drawable.ic_item_selected)
                }
            }
        }
        val listener = View.OnClickListener { v: View ->
            when (v.id) {
                holder.smsCl.id -> {
                    val phone = getItem(position).getFirstPhoneNumber()
                    context.startActivity(
                        Intent(
                            Intent.ACTION_SENDTO,
                            Uri.fromParts("sms", phone, null)
                        )
                    )
                }
                holder.callCl.id -> {
                    callPhone(getItem(position).getFirstPhoneNumber(), context)
                }
                holder.messengerCl.id -> {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://www.messenger.com/t/" + getItem(position).getMessengerID())
                    )
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }
                holder.whatsappCl.id -> {
                    val contactWithAllInformation = getItem(position)
                    openWhatsapp(contactWithAllInformation.getFirstPhoneNumber(), context)
                }
                holder.mailCl.id -> {
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
                holder.signalCl.id -> {
                    goToSignal(context as Activity)
                }
                holder.telegramCl.id -> {
                    goToTelegram(context, getItem(position).getFirstPhoneNumber())
                }
            }
            if (len == 1) {
                if (v.id == holder.editCl?.id) {
                    val intent = Intent(context, EditContactDetailsActivity::class.java)
                    intent.putExtra("ContactId", contact.id)
                    intent.putExtra("position", position)
                    context.startActivity(intent)
                }
            }
        }
        val longClick = OnLongClickListener { v: View? ->
            if (listOfItemSelected.size == 0 && len == 1) {
                holder.constraintLayoutMenu.visibility = View.GONE
            }
            view!!.tag = holder
            val contactDB = contactManager.contactList[position].contactDB!!
            if (listOfItemSelected.contains(contactManager.contactList[position])) {
                listOfItemSelected.remove(contactManager.contactList[position])
                if (contactDB.profilePicture64 != "") {
                    val bitmap = base64ToBitmap(contactDB.profilePicture64)
                    holder.contactRoundedImageView.setImageBitmap(bitmap)
                } else {
                    holder.contactRoundedImageView.setImageResource(
                        randomDefaultImage(
                            contactDB.profilePicture,
                            context
                        )
                    )
                }
            } else {
                listOfItemSelected.add(contactManager.contactList[position])
                if (context is GroupManagerActivity && len == 0) {
                } else {
                    holder.contactRoundedImageView.setImageResource(R.drawable.ic_item_selected)
                }
            }
            if (context is MainActivity) {
                context.recyclerMultiSelectItemClick(position)
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
                if (listOfItemSelected.size == 0 && len == 1 && holder.constraintLayoutMenu != null) {
                    holder.constraintLayoutMenu.visibility = View.GONE
                }
                view?.tag = holder
                val contactDB = contactManager.contactList[position].contactDB!!
                if (listOfItemSelected.contains(contactManager.contactList[position])) {
                    listOfItemSelected.remove(contactManager.contactList[position])
                    if (contactDB.profilePicture64 != "") {
                        val bitmap = base64ToBitmap(contactDB.profilePicture64)
                        holder.contactRoundedImageView.setImageBitmap(bitmap)
                    } else {
                        holder.contactRoundedImageView.setImageResource(
                            randomDefaultImage(
                                contactDB.profilePicture,
                                context
                            )
                        )
                    }
                } else {
                    listOfItemSelected.add(contactManager.contactList[position])
                    if (context is GroupManagerActivity && len == 0) {
                    } else {
                        holder.contactRoundedImageView.setImageResource(R.drawable.ic_item_selected)
                    }
                }
                if (context is MainActivity) {
                    context.recyclerMultiSelectItemClick(position)
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
                    Log.i("cpt", "${contact.firstName + " " + contact.lastName}")

                    if (holder.constraintLayoutMenu.visibility == View.GONE) {
                        val slideUp = AnimationUtils.loadAnimation(context, R.anim.slide_up)
                        holder.constraintLayoutMenu.startAnimation(slideUp)
                        holder.constraintLayoutMenu.visibility = View.VISIBLE
                        if (lastSelectMenuLen1 != null) lastSelectMenuLen1?.visibility = View.GONE
                        lastSelectMenuLen1 = holder.constraintLayoutMenu
                    } else {
                        holder.constraintLayoutMenu.visibility = View.GONE
                        lastSelectMenuLen1 = null
                    }
                }
            }
        }
        var cpt = 1

        val appsSupportPref =
            context.getSharedPreferences("Apps_Support_Bought", Context.MODE_PRIVATE)

        if (isWhatsappInstalled(context) && contact.hasWhatsapp == 1) {
            cpt += 1
            holder.whatsappCl.visibility = View.VISIBLE
        } else {
            holder.whatsappCl.visibility = View.GONE
        }

        if (listApp.contains("org.thoughtcrime.securesms") && contact.hasSignal == 1) {
            cpt += 1
            holder.signalCl.visibility = View.VISIBLE

            if (appsSupportPref?.getBoolean("Apps_Support_Bought", false) == false) {
                holder.signalIv.setImageResource(R.drawable.ic_signal_disable)
            }
        } else {
            holder.signalCl.visibility = View.GONE
        }

        if (listApp.contains("org.telegram.messenger") && contact.hasTelegram == 1) {
            cpt += 1
            holder.telegramCl.visibility = View.VISIBLE

            if (appsSupportPref?.getBoolean("Apps_Support_Bought", false) == false) {
                holder.telegramIv.setImageResource(R.drawable.ic_telegram_disable)
            }
        } else {
            holder.telegramCl.visibility = View.GONE
        }

        if (getItem(position).getFirstMail().isEmpty()) {
            holder.mailCl.visibility = View.GONE
        } else {
            cpt += 1
            holder.mailCl.visibility = View.VISIBLE
        }
        if (getItem(position).getFirstPhoneNumber().isEmpty()) {
            holder.callCl.visibility = View.GONE
            holder.smsCl.visibility = View.GONE
        } else {
            cpt += 2
            holder.callCl.visibility = View.VISIBLE
            holder.smsCl.visibility = View.VISIBLE
        }

        if (getItem(position).getMessengerID().isEmpty()) {
            holder.messengerCl.visibility = View.GONE
        } else {
            cpt += 1
            holder.messengerCl.visibility = View.VISIBLE

            if (appsSupportPref?.getBoolean("Apps_Support_Bought", false) == false) {
                holder.messengerIv.setImageResource(R.drawable.ic_messenger_disable)
            }
        }

        if (holder.constraintLayout != null) {
            holder.constraintLayout?.setOnLongClickListener(longClick)
            holder.constraintLayout?.setOnClickListener(listItemClick)
        }
        holder.mailCl.setOnClickListener(listener)
        holder.whatsappCl.setOnClickListener(listener)
        holder.callCl.setOnClickListener(listener)
        holder.smsCl.setOnClickListener(listener)
        holder.messengerCl.setOnClickListener(listener)
        holder.signalCl.setOnClickListener(listener)
        holder.callCl.setOnLongClickListener {
            val phoneNumber =
                getItem(position).getSecondPhoneNumber(getItem(position).getFirstPhoneNumber())
            if (phoneNumber.isNotEmpty()) {
                callPhone(phoneNumber, context)
            }
            true
        }
        if (holder.editCl != null) {
            holder.editCl?.setOnClickListener(listener)
        }
        if (contact.favorite == 1) {
            holder.listContactItemFavoriteShine.visibility = View.VISIBLE
        } else {
            holder.listContactItemFavoriteShine.visibility = View.GONE
        }

        val param = holder.constraintLayoutMenu.layoutParams as ViewGroup.MarginLayoutParams

        holder.constraintLayoutMenu.scrollBarFadeDuration = 20000
        holder.constraintLayoutMenu.scrollBarSize = 25
        holder.constraintLayoutMenu.scrollBarStyle
        holder.constraintLayoutMenu.scrollBarStyle = View.SCROLLBARS_OUTSIDE_OVERLAY
        when (cpt) {
            3 -> {
                param.setMargins(230, 0, 0, 0)
                holder.constraintLayoutMenu.layoutParams = param
            }
            4 -> {
                param.setMargins(150, 0, 0, 0)
                holder.constraintLayoutMenu.layoutParams = param
            }
            5 -> {
                param.setMargins(50, 0, 0, 0)
                holder.constraintLayoutMenu.layoutParams = param
            }
            6 -> {
                param.setMargins(0, 0, 0, 0)
                holder.constraintLayoutMenu.layoutParams = param
            }
        }
    }

    override fun getItemId(position: Int): Long {
        return listContacts.size.toLong()
    }

    override fun getItemCount(): Int {
        return listContacts.size
    }

    class ContactViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var contactFirstNameView: TextView =
            view.findViewById(R.id.list_contact_item_contactFirstName)
        var root: ConstraintLayout = view.findViewById(R.id.root)
        var constraintLayout: RelativeLayout? = view.findViewById(R.id.list_contact_item_layout)
        var constraintLayoutMenu: HorizontalScrollView =
            view.findViewById(R.id.list_contact_item_menu)
        var listContactItemFavoriteShine: AppCompatImageView =
            view.findViewById(R.id.list_contact_item_favorite_shine)
        var contactRoundedImageView: CircularImageView =
            view.findViewById(R.id.list_contact_item_contactRoundedImageView)
        var callCl: RelativeLayout = view.findViewById(R.id.list_contact_item_constraint_call)
        var smsCl: RelativeLayout = view.findViewById(R.id.list_contact_item_constraint_sms)
        var whatsappCl: RelativeLayout =
            view.findViewById(R.id.list_contact_item_constraint_whatsapp)
        var mailCl: RelativeLayout = view.findViewById(R.id.list_contact_item_constraint_mail)
        var editCl: RelativeLayout? = view.findViewById(R.id.list_contact_item_constraint_edit)
        var messengerCl: RelativeLayout =
            view.findViewById(R.id.list_contact_item_constraint_messenger)
        var messengerIv: AppCompatImageView = view.findViewById(R.id.messenger_iv)
        var signalCl: RelativeLayout = view.findViewById(R.id.list_contact_item_constraint_signal)
        var signalIv: AppCompatImageView = view.findViewById(R.id.signal_iv)
        var telegramCl: RelativeLayout =
            view.findViewById(R.id.list_contact_item_constraint_telegram)
        var telegramIv: AppCompatImageView = view.findViewById(R.id.telegram_iv)
    }

    init {
        listContacts = contactManager.contactList
        lastSelectMenuLen1 = null
    }
}