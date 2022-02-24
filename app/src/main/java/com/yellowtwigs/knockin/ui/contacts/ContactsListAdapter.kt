package com.yellowtwigs.knockin.ui.contacts

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.TranslateAnimation
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.ItemContactBinding
import com.yellowtwigs.knockin.models.data.Contact
import com.yellowtwigs.knockin.ui.edit_contact.EditContactDetailsActivity
import com.yellowtwigs.knockin.ui.group.GroupManagerActivity
import com.yellowtwigs.knockin.utils.ContactGesture.openWhatsapp
import com.yellowtwigs.knockin.utils.Converter.base64ToBitmap
import com.yellowtwigs.knockin.utils.RandomDefaultImage.randomDefaultImage
import com.yellowtwigs.knockin.utils.SetupContactsList.contactPriorityBorder
import java.sql.DriverManager
import java.util.*

/**
 * La Classe qui permet de remplir la RecyclerView avec les bon éléments
 *
 * @author Florian Striebel, Kenzy Suon, Ryan Granet
 */
class ContactsListAdapter(
    private val cxt: Context,
    private val fragment: ContactsListFragment,
    private val onClickedCallback: (Int) -> Unit
) : ListAdapter<Contact, ContactsListAdapter.ViewHolder>(ContactComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemContactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(getItem(position), fragment, cxt, onClickedCallback)
    }

    inner class ViewHolder(private val binding: ItemContactBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(
            contact: Contact,
            fragment: ContactsListFragment,
            cxt: Context,
            onClicked: (Int) -> Unit
        ) {
            binding.apply {
                contactPriorityBorder(contact.priority, contactImage, cxt)
                if (contact.profilePicture64 != "") {
                    val bitmap = base64ToBitmap(contact.profilePicture64)
                    contactImage.setImageBitmap(bitmap)
                } else {
                    contactImage.setImageResource(
                        randomDefaultImage(
                            contact.profilePicture,
                            cxt
                        )
                    )
                }

                contactName.text = "${contact.firstName} ${contact.lastName}"

                root.setOnClickListener {
                    menu.isVisible = !menu.isVisible
                }

//                root.setOnLongClickListener {
//
//                    return@setOnLongClickListener
//                }

                smsIcon.setOnClickListener {
                    fragment.openSms(contact.id)
                }
                callIcon.setOnClickListener {
                    fragment.phoneCall(contact.id)
                }
                whatsappIcon.setOnClickListener {
                    fragment.phoneCall(contact.id)
                }
                mailIcon.setOnClickListener {
                    fragment.phoneCall(contact.id)
                }

                editIcon.setOnClickListener {
                    onClicked(contact.id)
                }

//                val longClick = OnLongClickListener { v: View? ->
//                    if (listOfItemSelected.size == 0 && len == 1 && holder.constraintLayoutMenu != null) {
//                        holder.constraintLayoutMenu!!.visibility = View.GONE
//                    }
//                    view!!.tag = holder
//                    val contactDB = gestionnaireContacts.contactList[position].contact!!
//                    if (listOfItemSelected.contains(gestionnaireContacts.contactList[position])) {
//                        listOfItemSelected.remove(gestionnaireContacts.contactList[position])
//                        if (contactDB.profilePicture64 != "") {
//                            val bitmap = base64ToBitmap(contactDB.profilePicture64)
//                            holder.contactRoundedImageView.setImageBitmap(bitmap)
//                        } else {
//                            holder.contactRoundedImageView.setImageResource(
//                                randomDefaultImage(
//                                    contactDB.profilePicture
//                                )
//                            )
//                        }
//                    } else {
//                        listOfItemSelected.add(gestionnaireContacts.contactList[position])
//                        if (context is GroupManagerActivity && len == 0) {
//                        } else {
//                            holder.contactRoundedImageView.setImageResource(R.drawable.ic_item_selected)
//                        }
//                    }
//                    if (context is ContactListActivity) {
//                        context.recyclerMultiSelectItemClick(position)
//                    }
//                    if (listOfItemSelected.size > 0) {
//                        modeMultiSelect = true
//                        lastClick = false
//                    } else {
//                        modeMultiSelect = false
//                        lastClick = true
//                    }
//                    modeMultiSelect
//                }
//                val listItemClick = View.OnClickListener { v: View? ->
//                    if (modeMultiSelect) {
//                        if (listOfItemSelected.size == 0 && len == 1 && holder.constraintLayoutMenu != null) {
//                            holder.constraintLayoutMenu!!.visibility = View.GONE
//                        }
//                        view!!.tag = holder
//                        val contactDB = gestionnaireContacts.contactList[position].contact!!
//                        if (listOfItemSelected.contains(gestionnaireContacts.contactList[position])) {
//                            listOfItemSelected.remove(gestionnaireContacts.contactList[position])
//                            if (contactDB.profilePicture64 != "") {
//                                val bitmap = base64ToBitmap(contactDB.profilePicture64)
//                                holder.contactRoundedImageView.setImageBitmap(bitmap)
//                            } else {
//                                holder.contactRoundedImageView.setImageResource(
//                                    randomDefaultImage(
//                                        contactDB.profilePicture
//                                    )
//                                )
//                            }
//                        } else {
//                            listOfItemSelected.add(gestionnaireContacts.contactList[position])
//                            if (context is GroupManagerActivity && len == 0) {
//                            } else {
//                                holder.contactRoundedImageView.setImageResource(R.drawable.ic_item_selected)
//                            }
//                        }
//                        if (context is ContactListActivity) {
//                            context.recyclerMultiSelectItemClick(position)
//                        }
//                        if (listOfItemSelected.size > 0) {
//                            modeMultiSelect = true
//                            lastClick = false
//                        } else {
//                            modeMultiSelect = false
//                            lastClick = true
//                        }
//                    } else {
//                        if (lastClick) {
//                            lastClick = false
//                        } else {
//                            if (len == 1) {
//                                if (holder.constraintLayoutMenu != null) {
//                                    if (holder.constraintLayoutMenu!!.visibility == View.GONE) {
//                                        holder.constraintLayoutMenu!!.visibility = View.VISIBLE
//                                        slideUp(holder.constraintLayoutMenu)
//                                        if (lastSelectMenuLen1 != null) lastSelectMenuLen1!!.visibility =
//                                            View.GONE
//                                        lastSelectMenuLen1 = holder.constraintLayoutMenu
//                                    } else {
//                                        holder.constraintLayoutMenu!!.visibility = View.GONE
//                                        val slideDown =
//                                            AnimationUtils.loadAnimation(
//                                                context,
//                                                R.anim.slide_down
//                                            )
//                                        holder.constraintLayoutMenu!!.startAnimation(slideDown)
//                                        lastSelectMenuLen1 = null
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//                if (len == 1) {
//                    if (whatsappIsNotInstalled() && contact.hasWhatsapp == 0) {
//                        holder.whatsappCl.visibility = View.GONE
//                    } else {
//                        holder.whatsappCl.visibility = View.VISIBLE
//                    }
//                    if (getItem(position).getFirstMail().isEmpty()) {
//                        holder.mailCl.visibility = View.GONE
//                    } else {
//                        holder.mailCl.visibility = View.VISIBLE
//                    }
//                    if (getItem(position).getFirstPhoneNumber().isEmpty()) {
//                        holder.callCl.visibility = View.GONE
//                        holder.smsCl.visibility = View.GONE
//                    } else {
//                        holder.callCl.visibility = View.VISIBLE
//                        holder.smsCl.visibility = View.VISIBLE
//                    }
//                }
//                if (holder.constraintLayout != null) {
//                    holder.constraintLayout!!.setOnLongClickListener(longClick)
//                    holder.constraintLayout!!.setOnClickListener(listItemClick)
//                }
//                holder.callCl.setOnLongClickListener { v: View? ->
//                    val phoneNumber =
//                        getItem(position).getSecondPhoneNumber(getItem(position).getFirstPhoneNumber())
//                    if (!phoneNumber.isEmpty()) {
//                        callPhone(phoneNumber)
//                    }
//                    true
//                }
//                if (holder.editCl != null) {
//                    holder.editCl!!.setOnClickListener(listener)
//                }
//                if (contact.favorite == 1) {
//                    holder.listContactItemFavoriteShine.visibility = View.VISIBLE
//                } else {
//                    holder.listContactItemFavoriteShine.visibility = View.GONE
//                }
            }
        }

//        private fun slideUp(view: View?) {
//            view?.visibility = View.VISIBLE
//            val animate = TranslateAnimation(
//                0.toFloat(),  // fromXDelta
//                0.toFloat(),  // toXDelta
//                view.height.toFloat(),  // fromYDelta
//                0.toFloat()
//            ) // toYDelta
//            animate.duration = 500
//            animate.fillAfter = true
//            view.startAnimation(animate)
//        }
    }

    class ContactComparator : DiffUtil.ItemCallback<Contact>() {
        override fun areItemsTheSame(oldItem: Contact, newItem: Contact) = oldItem == newItem
        override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem.id == newItem.id &&
                    oldItem.firstName == newItem.firstName &&
                    oldItem.lastName == newItem.lastName &&
                    oldItem.phoneNumber == newItem.phoneNumber &&
                    oldItem.mail == newItem.mail &&
                    oldItem.mailId == newItem.mailId &&
                    oldItem.priority == newItem.priority
        }
    }
}