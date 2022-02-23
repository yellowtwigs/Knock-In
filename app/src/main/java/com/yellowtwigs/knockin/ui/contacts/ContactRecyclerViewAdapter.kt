package com.yellowtwigs.knockin.ui.adapters

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PorterDuff
import android.net.Uri
import android.util.Base64
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.controller.CircularImageView
import com.yellowtwigs.knockin.ui.edit_contact.EditContactDetailsActivity
import com.yellowtwigs.knockin.controller.activity.MainActivity
import com.yellowtwigs.knockin.ui.group.GroupManagerActivity
import com.yellowtwigs.knockin.utils.ContactGesture.openWhatsapp
import com.yellowtwigs.knockin.model.ContactManager
import com.yellowtwigs.knockin.model.ModelDB.ContactWithAllInformation
import java.sql.DriverManager
import java.util.*

/**
 * La Classe qui permet de remplir la RecyclerView avec les bon éléments
 *
 * @author Florian Striebel, Kenzy Suon, Ryan Granet
 */
class ContactRecyclerViewAdapter(
    private val context: Context,
    private var gestionnaireContacts: ContactManager,
    private val len: Int
) : RecyclerView.Adapter<ContactRecyclerViewAdapter.ContactViewHolder>() {
    private val listContacts: List<ContactWithAllInformation>
    private var view: View? = null
    private var modeMultiSelect = false
    private var lastClick = false
    private var lastSelectMenuLen1: ConstraintLayout?

    var listOfItemSelected = ArrayList<ContactWithAllInformation>()
    var phonePermission = ""
        private set

    fun getItem(position: Int): ContactWithAllInformation {
        return listContacts[position]
    }

    fun setGestionnaireContact(gestionnaireContact: ContactManager) {
        gestionnaireContacts = gestionnaireContact
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_contact_item_layout, parent, false)
        return ContactViewHolder(view!!)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = getItem(position).contactDB!!
        if (len == 0) {
            if (contact.contactPriority == 0) {
            } else if (contact.contactPriority == 1) {
                val sharedPreferences =
                    context.getSharedPreferences("Knockin_Theme", Context.MODE_PRIVATE)
                if (sharedPreferences.getBoolean("darkTheme", false)) {
                } else {
                    holder.contactFirstNameView.setTextColor(
                        context.resources.getColor(
                            R.color.textColorDark,
                            null
                        )
                    )
                }
            } else if (contact.contactPriority == 2) {
                holder.contactFirstNameView.setTextColor(
                    context.resources.getColor(
                        R.color.colorPrimaryDark,
                        null
                    )
                )
            }
        } else {
            if (contact.contactPriority == 0) {
                holder.contactRoundedImageView.setBorderColor(
                    context.resources.getColor(
                        R.color.priorityZeroColor,
                        null
                    )
                )
            } else if (contact.contactPriority == 1) {
                holder.contactRoundedImageView.setBorderColor(
                    context.resources.getColor(
                        R.color.transparentColor,
                        null
                    )
                )
            } else if (contact.contactPriority == 2) {
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
            holder.contactRoundedImageView.setImageResource(randomDefaultImage(contact.profilePicture))
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
            //Drawable roundedLayout = context.getDrawable(R.drawable.rounded_rectangle_group);
            //roundedLayout.setColorFilter(Color.parseColor("#f0f0f0"), PorterDuff.Mode.MULTIPLY);
            //holder.groupWordingConstraint.setBackground(roundedLayout);
        } else {
            val roundedLayout = context.getDrawable(R.drawable.rounded_rectangle_group)!!
            //            roundedLayout.setColorFilter(firstGroup.randomColorGroup(this.context), PorterDuff.Mode.MULTIPLY);
            roundedLayout.setColorFilter(firstGroup.section_color, PorterDuff.Mode.MULTIPLY)
        }
        if (modeMultiSelect) {
            if (listOfItemSelected.contains(gestionnaireContacts.contactList[position])) {
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
                    callPhone(getItem(position).getFirstPhoneNumber())
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
            }
            if (len == 1) {
                if (v.id == holder.editCl!!.id) {
                    val intent = Intent(context, EditContactDetailsActivity::class.java)
                    intent.putExtra("ContactId", contact.id)
                    intent.putExtra("position", position)
                    context.startActivity(intent)
                }
            }
        }
        val longClick = OnLongClickListener { v: View? ->
            if (listOfItemSelected.size == 0 && len == 1 && holder.constraintLayoutMenu != null) {
                holder.constraintLayoutMenu!!.visibility = View.GONE
            }
            view!!.tag = holder
            val contactDB = gestionnaireContacts.contactList[position].contactDB!!
            if (listOfItemSelected.contains(gestionnaireContacts.contactList[position])) {
                listOfItemSelected.remove(gestionnaireContacts.contactList[position])
                if (contactDB.profilePicture64 != "") {
                    val bitmap = base64ToBitmap(contactDB.profilePicture64)
                    holder.contactRoundedImageView.setImageBitmap(bitmap)
                } else {
                    holder.contactRoundedImageView.setImageResource(randomDefaultImage(contactDB.profilePicture))
                }
            } else {
                listOfItemSelected.add(gestionnaireContacts.contactList[position])
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
                    holder.constraintLayoutMenu!!.visibility = View.GONE
                }
                view!!.tag = holder
                val contactDB = gestionnaireContacts.contactList[position].contactDB!!
                if (listOfItemSelected.contains(gestionnaireContacts.contactList[position])) {
                    listOfItemSelected.remove(gestionnaireContacts.contactList[position])
                    if (contactDB.profilePicture64 != "") {
                        val bitmap = base64ToBitmap(contactDB.profilePicture64)
                        holder.contactRoundedImageView.setImageBitmap(bitmap)
                    } else {
                        holder.contactRoundedImageView.setImageResource(randomDefaultImage(contactDB.profilePicture))
                    }
                } else {
                    listOfItemSelected.add(gestionnaireContacts.contactList[position])
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
                    if (len == 1) {
                        if (holder.constraintLayoutMenu != null) {
                            if (holder.constraintLayoutMenu!!.visibility == View.GONE) {
                                holder.constraintLayoutMenu!!.visibility = View.VISIBLE
                                slideUp(holder.constraintLayoutMenu)
                                if (lastSelectMenuLen1 != null) lastSelectMenuLen1!!.visibility =
                                    View.GONE
                                lastSelectMenuLen1 = holder.constraintLayoutMenu
                            } else {
                                holder.constraintLayoutMenu!!.visibility = View.GONE
                                val slideDown =
                                    AnimationUtils.loadAnimation(context, R.anim.slide_down)
                                holder.constraintLayoutMenu!!.startAnimation(slideDown)
                                lastSelectMenuLen1 = null
                            }
                        }
                    }
                }
            }
        }
        if (len == 0) {
            if (whatsappIsNotInstalled() || contact.hasWhatsapp == 0) {
                holder.whatsappCl.visibility = View.INVISIBLE
            } else {
                holder.whatsappCl.visibility = View.VISIBLE
            }
            if (getItem(position).getFirstMail().isEmpty()) {
                holder.mailCl.visibility = View.INVISIBLE
            } else {
                holder.mailCl.visibility = View.VISIBLE
            }
            if (getItem(position).getFirstPhoneNumber().isEmpty()) {
                holder.callCl.visibility = View.INVISIBLE
                holder.smsCl.visibility = View.INVISIBLE
            } else {
                holder.callCl.visibility = View.VISIBLE
                holder.smsCl.visibility = View.VISIBLE
            }
        }
        if (len == 1) {

            Log.i("Whatsapp", "Whatsapp : ${contact.hasWhatsapp}")
            Log.i("Whatsapp", "Whatsapp : ${whatsappIsNotInstalled()}")
            if (whatsappIsNotInstalled() && contact.hasWhatsapp == 0) {
                holder.whatsappCl.visibility = View.GONE
            } else {
                holder.whatsappCl.visibility = View.VISIBLE
            }
            if (getItem(position).getFirstMail().isEmpty()) {
                holder.mailCl.visibility = View.GONE
            } else {
                holder.mailCl.visibility = View.VISIBLE
            }
            if (getItem(position).getFirstPhoneNumber().isEmpty()) {
                holder.callCl.visibility = View.GONE
                holder.smsCl.visibility = View.GONE
            } else {
                holder.callCl.visibility = View.VISIBLE
                holder.smsCl.visibility = View.VISIBLE
            }
        }
        if (holder.constraintLayout != null) {
            holder.constraintLayout!!.setOnLongClickListener(longClick)
            holder.constraintLayout!!.setOnClickListener(listItemClick)
        }
        holder.mailCl.setOnClickListener(listener)
        holder.whatsappCl.setOnClickListener(listener)
        holder.callCl.setOnClickListener(listener)
        holder.smsCl.setOnClickListener(listener)
        holder.callCl.setOnLongClickListener { v: View? ->
            val phoneNumber =
                getItem(position).getSecondPhoneNumber(getItem(position).getFirstPhoneNumber())
            if (!phoneNumber.isEmpty()) {
                callPhone(phoneNumber)
            }
            true
        }
        if (holder.editCl != null) {
            holder.editCl!!.setOnClickListener(listener)
        }
        if (contact.favorite == 1) {
            holder.listContactItemFavoriteShine.visibility = View.VISIBLE
        } else {
            holder.listContactItemFavoriteShine.visibility = View.GONE
        }
    }

    override fun getItemId(position: Int): Long {
        return listContacts.size.toLong()
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    override fun getItemCount(): Int {
        return listContacts.size
    }

    //    private String converter06To33(String phoneNumber) {
    //        if (phoneNumber.charAt(0) == '0') {
    //            return "+33" + phoneNumber;
    //        }
    //        return phoneNumber;
    //    }
    private fun randomDefaultImage(avatarId: Int): Int {
        val sharedPreferencesIsMultiColor =
            context.getSharedPreferences("IsMultiColor", Context.MODE_PRIVATE)
        val multiColor = sharedPreferencesIsMultiColor.getInt("isMultiColor", 0)
        val sharedPreferencesContactsColor =
            context.getSharedPreferences("ContactsColor", Context.MODE_PRIVATE)
        val contactsColorPosition = sharedPreferencesContactsColor.getInt("contactsColor", 0)
        return if (multiColor == 0) {
            when (avatarId) {
                0 -> R.drawable.ic_user_purple
                1 -> R.drawable.ic_user_blue
                2 -> R.drawable.ic_user_cyan_teal
                3 -> R.drawable.ic_user_green
                4 -> R.drawable.ic_user_om
                5 -> R.drawable.ic_user_orange
                6 -> R.drawable.ic_user_red
                else -> R.drawable.ic_user_blue
            }
        } else {
            when (contactsColorPosition) {
                0 -> when (avatarId) {
                    0 -> R.drawable.ic_user_blue
                    1 -> R.drawable.ic_user_blue_indigo1
                    2 -> R.drawable.ic_user_blue_indigo2
                    3 -> R.drawable.ic_user_blue_indigo3
                    4 -> R.drawable.ic_user_blue_indigo4
                    5 -> R.drawable.ic_user_blue_indigo5
                    6 -> R.drawable.ic_user_blue_indigo6
                    else -> R.drawable.ic_user_om
                }
                1 -> when (avatarId) {
                    0 -> R.drawable.ic_user_green
                    1 -> R.drawable.ic_user_green_lime1
                    2 -> R.drawable.ic_user_green_lime2
                    3 -> R.drawable.ic_user_green_lime3
                    4 -> R.drawable.ic_user_green_lime4
                    5 -> R.drawable.ic_user_green_lime5
                    else -> R.drawable.ic_user_green_lime6
                }
                2 -> when (avatarId) {
                    0 -> R.drawable.ic_user_purple
                    1 -> R.drawable.ic_user_purple_grape1
                    2 -> R.drawable.ic_user_purple_grape2
                    3 -> R.drawable.ic_user_purple_grape3
                    4 -> R.drawable.ic_user_purple_grape4
                    5 -> R.drawable.ic_user_purple_grape5
                    else -> R.drawable.ic_user_purple
                }
                3 -> when (avatarId) {
                    0 -> R.drawable.ic_user_red
                    1 -> R.drawable.ic_user_red1
                    2 -> R.drawable.ic_user_red2
                    3 -> R.drawable.ic_user_red3
                    4 -> R.drawable.ic_user_red4
                    5 -> R.drawable.ic_user_red5
                    else -> R.drawable.ic_user_red
                }
                4 -> when (avatarId) {
                    0 -> R.drawable.ic_user_grey
                    1 -> R.drawable.ic_user_grey1
                    2 -> R.drawable.ic_user_grey2
                    3 -> R.drawable.ic_user_grey3
                    4 -> R.drawable.ic_user_grey4
                    else -> R.drawable.ic_user_grey1
                }
                5 -> when (avatarId) {
                    0 -> R.drawable.ic_user_orange
                    1 -> R.drawable.ic_user_orange1
                    2 -> R.drawable.ic_user_orange2
                    3 -> R.drawable.ic_user_orange3
                    4 -> R.drawable.ic_user_orange4
                    else -> R.drawable.ic_user_orange3
                }
                6 -> when (avatarId) {
                    0 -> R.drawable.ic_user_cyan_teal
                    1 -> R.drawable.ic_user_cyan_teal1
                    2 -> R.drawable.ic_user_cyan_teal2
                    3 -> R.drawable.ic_user_cyan_teal3
                    4 -> R.drawable.ic_user_cyan_teal4
                    else -> R.drawable.ic_user_cyan_teal
                }
                else -> when (avatarId) {
                    0 -> R.drawable.ic_user_purple
                    1 -> R.drawable.ic_user_blue
                    2 -> R.drawable.ic_user_cyan_teal
                    3 -> R.drawable.ic_user_green
                    4 -> R.drawable.ic_user_om
                    5 -> R.drawable.ic_user_orange
                    6 -> R.drawable.ic_user_red
                    else -> R.drawable.ic_user_blue
                }
            }
        }
    }

    private fun base64ToBitmap(base64: String): Bitmap {
        val decodedString = Base64.decode(base64, Base64.DEFAULT)
        val options = BitmapFactory.Options()
        //options.inSampleSize = 2;
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size, options)
    }

    fun callPhone(phoneNumber: String) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CALL_PHONE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val PERMISSION_CALL_RESULT = 1
            ActivityCompat.requestPermissions(
                (context as Activity),
                arrayOf(Manifest.permission.CALL_PHONE),
                PERMISSION_CALL_RESULT
            )
            phonePermission = phoneNumber
        } else {
            //Intent intent=new Intent(Intent.ACTION_CALL);
            //intent.setData(Uri.parse(getItem(position).getFirstPhoneNumber()));
            val sharedPreferences = context.getSharedPreferences("Phone_call", Context.MODE_PRIVATE)
            val popup = sharedPreferences.getBoolean("popup", true)
            if (popup && phonePermission.isEmpty()) {
                AlertDialog.Builder(context)
                    .setTitle(R.string.main_contact_grid_title)
                    .setMessage(R.string.main_contact_grid_message)
                    .setPositiveButton(android.R.string.yes) { dialog: DialogInterface?, id: Int ->
                        context.startActivity(
                            Intent(Intent.ACTION_CALL, Uri.fromParts("tel", phoneNumber, null))
                        )
                    }
                    .setNegativeButton(android.R.string.no, null)
                    .show()
            } else {
                context.startActivity(
                    Intent(
                        Intent.ACTION_CALL,
                        Uri.fromParts("tel", phoneNumber, null)
                    )
                )
                phonePermission = ""
            }
        }
    } //code duplicate à mettre dans contactAllInfo

    private fun whatsappIsNotInstalled(): Boolean {
        val pm = context.packageManager
        return try {
            pm.getApplicationInfo("com.whatsapp", 0)
            false
        } catch (e: Exception) {
            true
        }
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

    class ContactViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var contactFirstNameView: TextView =
            view.findViewById(R.id.list_contact_item_contactFirstName)
        var constraintLayout: RelativeLayout? = view.findViewById(R.id.list_contact_item_layout)
        var constraintLayoutMenu: ConstraintLayout? = view.findViewById(R.id.list_contact_item_menu)
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
    }

    init {
        listContacts = gestionnaireContacts.contactList
        lastSelectMenuLen1 = null
    }
}