package com.yellowtwigs.knockin.ui.contacts

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu.MenuStateChangeListener
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.model.ContactManager
import com.yellowtwigs.knockin.model.data.ContactWithAllInformation
import com.yellowtwigs.knockin.ui.CircularImageView
import com.yellowtwigs.knockin.ui.contacts.contact_selected.ContactSelectedWithAppsActivity
import com.yellowtwigs.knockin.ui.contacts.list.Main2Activity
import com.yellowtwigs.knockin.ui.group.list.GroupManagerActivity
import com.yellowtwigs.knockin.utils.Converter.base64ToBitmap
import com.yellowtwigs.knockin.utils.EveryActivityUtils.getAppOnPhone
import com.yellowtwigs.knockin.utils.RandomDefaultImage.randomDefaultImage

/**
 * La Classe qui permet de remplir la convertView avec les bon éléments
 *
 * @author Florian Striebel, Kenzy Suon, Ryan Granet
 */
class ContactGridViewAdapter(
    private val context: Context,
    private var contactManager: ContactManager,
    private val len: Int
) : RecyclerView.Adapter<ContactGridViewAdapter.ViewHolder>(), MenuStateChangeListener {
    var selectMenu: FloatingActionMenu? = null
        private set
    var phonePermission = ""
        private set
    private var modeMultiSelect = false
    private var heightWidthImage = 0
    private lateinit var listApp: ArrayList<String>

    var listOfItemSelected = ArrayList<ContactWithAllInformation>()

    fun setContactManager(contactManager: ContactManager) {
        this.contactManager = contactManager
    }

    fun getItem(position: Int): ContactWithAllInformation {
        return contactManager.contactList[position]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        listApp = getAppOnPhone(context as Activity)
        val view =
            LayoutInflater.from(context).inflate(R.layout.grid_contact_item_layout, parent, false)
        val holder = ViewHolder(view)
        heightWidthImage = holder.contactRoundedImageView.layoutParams.height
        return holder
    }

    @SuppressLint("ResourceType")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = contactManager.contactList[position].contactDB
        val layoutParamsTV = holder.contactFirstNameView.layoutParams as RelativeLayout.LayoutParams
        val layoutParamsIV =
            holder.contactRoundedImageView.layoutParams as ConstraintLayout.LayoutParams
        if (!modeMultiSelect || !listOfItemSelected.contains(contactManager.contactList[position])) {
            assert(contact != null)
            if (contact!!.profilePicture64 != "") {
                val bitmap = base64ToBitmap(contact.profilePicture64)
                holder.contactRoundedImageView.setImageBitmap(bitmap)
            } else {
                holder.contactRoundedImageView.setImageResource(
                    randomDefaultImage(
                        contact.profilePicture,
                        context,
                        "Get"
                    )
                )
            }
        } else {
            holder.contactRoundedImageView.setImageResource(R.drawable.ic_item_selected)
        }
        if (len == 4) {
            holder.contactRoundedImageView.layoutParams.height =
                (heightWidthImage - heightWidthImage * 0.25).toInt()
            holder.contactRoundedImageView.layoutParams.width =
                (heightWidthImage - heightWidthImage * 0.25).toInt()
            layoutParamsTV.topMargin = 10
            layoutParamsIV.topMargin = 10
        } else if (len == 5) {
            holder.contactRoundedImageView.layoutParams.height =
                (heightWidthImage - heightWidthImage * 0.40).toInt()
            holder.contactRoundedImageView.layoutParams.width =
                (heightWidthImage - heightWidthImage * 0.40).toInt()
            layoutParamsTV.topMargin = 0
            layoutParamsIV.topMargin = 0
        }
        when (contact?.contactPriority) {
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
        var firstname = contact?.firstName
        var lastName = contact?.lastName
        if (len == 5) {
            if (contact?.firstName!!.length > 11) firstname =
                contact.firstName.substring(0, 9) + ".."
            holder.contactFirstNameView.text = firstname
            val span: Spannable = SpannableString(holder.contactFirstNameView.text)
            span.setSpan(
                RelativeSizeSpan(0.9f),
                0,
                firstname?.length!!,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            holder.contactFirstNameView.text = span
            //holder.contactFirstNameView.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary, null));
            if (contact.lastName.length > 11) lastName = contact.lastName.substring(0, 9) + ".."
            val spanLastName: Spannable = SpannableString(lastName)
            spanLastName.setSpan(
                RelativeSizeSpan(0.9f),
                0,
                lastName!!.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            holder.contactLastNameView.text = spanLastName
        } else if (len == 4) {
            if (contact?.firstName?.length!! > 12) firstname =
                contact.firstName.substring(0, 10) + ".."
            val spanFistName: Spannable = SpannableString(firstname)
            spanFistName.setSpan(
                RelativeSizeSpan(0.95f),
                0,
                firstname!!.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            holder.contactFirstNameView.text = spanFistName
            if (contact.lastName.length > 12) lastName = contact.lastName.substring(0, 10) + ".."
            val spanLastName: Spannable = SpannableString(lastName)
            spanLastName.setSpan(
                RelativeSizeSpan(0.95f),
                0,
                lastName!!.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            holder.contactLastNameView.text = spanLastName
        }
        if (firstname?.isEmpty() == true) {
            holder.contactFirstNameView.visibility = View.GONE
        }

        if (contact?.favorite == 1) {
            holder.gridAdapterFavoriteShine.visibility = View.VISIBLE
        } else {
            holder.gridAdapterFavoriteShine.visibility = View.GONE
        }

        val gridlongClick = OnLongClickListener { v: View? ->
            if (!modeMultiSelect) {
                val firstPosVis: Int
                closeMenu()
                modeMultiSelect = true
                listOfItemSelected.add(contactManager.contactList[position])
                firstPosVis = 0
                if (context is Main2Activity) {
                    context.gridMultiSelectItemClick(position)
                } else {
                    (context as GroupManagerActivity).gridMultiSelectItemClick(
                        len,
                        position,
                        firstPosVis
                    )
                }
                holder.contactRoundedImageView.setImageResource(R.drawable.ic_item_selected)
            }
            true
        }
        val gridItemClick = View.OnClickListener { v: View? ->
            if (modeMultiSelect) {
                if (listOfItemSelected.contains(contactManager.contactList[position])) {
                    listOfItemSelected.remove(contactManager.contactList[position])
                    if (contact?.profilePicture64 != "") {
                        val bitmap = base64ToBitmap(contact?.profilePicture64.toString())
                        holder.contactRoundedImageView.setImageBitmap(bitmap)
                    } else {
                        holder.contactRoundedImageView.setImageResource(
                            randomDefaultImage(
                                contact.profilePicture,
                                context,
                                "Get"
                            )
                        )
                    }
                    if (listOfItemSelected.isEmpty()) {
                        modeMultiSelect = false
                    }
                } else {
                    listOfItemSelected.add(contactManager.contactList[position])
                    holder.contactRoundedImageView.setImageResource(R.drawable.ic_item_selected)
                }
                (context as Main2Activity).gridMultiSelectItemClick(position)
            } else {
                (context as Main2Activity).startActivity(
                    Intent(
                        context,
                        ContactSelectedWithAppsActivity::class.java
                    ).putExtra("id", getItem(position).getContactId())
                )
            }
        }
        holder.gridContactItemLayout.setOnLongClickListener(gridlongClick)
        holder.contactRoundedImageView.setOnLongClickListener(gridlongClick)
        holder.gridContactItemLayout.setOnClickListener(gridItemClick)
        holder.contactRoundedImageView.setOnClickListener(gridItemClick)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount(): Int {
        return contactManager.contactList.size
    }

    override fun onMenuOpened(floatingActionMenu: FloatingActionMenu) {
        if (selectMenu != null) {
            selectMenu?.close(false)
        }
        if (multiSelectMode()) {
            floatingActionMenu.close(false)
        }
        selectMenu = floatingActionMenu
    }

    /**
     * @param floatingActionMenu
     */
    override fun onMenuClosed(floatingActionMenu: FloatingActionMenu) {
        selectMenu = null
    }

    /**
     * Ferme le menu qui est ouvert
     */
    fun closeMenu() {
        if (selectMenu != null) selectMenu!!.close(true)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var contactFirstNameView: TextView
        var contactLastNameView: TextView
        var contactRoundedImageView: CircularImageView
        var gridAdapterFavoriteShine: AppCompatImageView
        var gridContactItemLayout: ConstraintLayout

        init {
            contactFirstNameView = view.findViewById(R.id.grid_adapter_contactFirstName)
            contactLastNameView = view.findViewById(R.id.grid_adapter_contactLastName)
            gridContactItemLayout = view.findViewById(R.id.grid_contact_item_layout)
            contactRoundedImageView = view.findViewById(R.id.contactRoundedImageView)
            gridAdapterFavoriteShine = view.findViewById(R.id.grid_adapter_favorite_shine)
            //            heightWidthImage = holder.contactRoundedImageView.getLayoutParams().height;
        }
    }

    fun multiSelectMode(): Boolean {
        return modeMultiSelect
    }

}