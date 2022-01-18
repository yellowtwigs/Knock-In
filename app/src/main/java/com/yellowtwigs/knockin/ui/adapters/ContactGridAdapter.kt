package com.yellowtwigs.knockin.ui.adapters

import android.Manifest
import com.yellowtwigs.knockin.utils.ContactGesture.openWhatsapp
import androidx.recyclerview.widget.RecyclerView
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu.MenuStateChangeListener
import android.view.LayoutInflater
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu
import com.yellowtwigs.knockin.model.ModelDB.ContactWithAllInformation
import android.view.ViewGroup
import com.yellowtwigs.knockin.R
import android.graphics.Bitmap
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.util.DisplayMetrics
import android.widget.FrameLayout
import android.content.Intent
import com.yellowtwigs.knockin.controller.activity.EditContactActivity
import com.yellowtwigs.knockin.controller.activity.group.GroupManagerActivity
import android.view.View.OnLongClickListener
import com.yellowtwigs.knockin.controller.activity.MainActivity
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import android.content.DialogInterface
import android.net.Uri
import android.view.View
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.yellowtwigs.knockin.controller.CircularImageView
import com.yellowtwigs.knockin.databinding.GridContactItemLayoutBinding
import com.yellowtwigs.knockin.utils.ConvertBitmap.base64ToBitmap
import com.yellowtwigs.knockin.utils.ConverterPhoneNumber.converter06To33
import com.yellowtwigs.knockin.utils.InitContactAdapter
import com.yellowtwigs.knockin.utils.InitContactAdapter.contactPriorityBorder
import com.yellowtwigs.knockin.utils.InitContactAdapter.initMenuButton
import com.yellowtwigs.knockin.utils.RandomDefaultImage.randomDefaultImage
import java.lang.Exception
import java.util.*

/**
 * La Classe qui permet de remplir la convertView avec les bon éléments
 *
 * @author Florian Striebel, Kenzy Suon, Ryan Granet
 */
class ContactGridAdapter(
    private val cxt: Activity, private val len: Int
) : ListAdapter<ContactWithAllInformation, ContactGridAdapter.ViewHolder>(
    ContactWithAllInformationComparator()
), MenuStateChangeListener {

    private val listCircularMenu = ArrayList<FloatingActionMenu>()
    var selectMenu: FloatingActionMenu? = null
    var phonePermission = ""
    private var modeMultiSelect = false
    val menuStateChangeListener = this
    var listOfItemSelected = ArrayList<ContactWithAllInformation>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            GridContactItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = getItem(position)
        holder.onBind(contact, position)
    }

    inner class ViewHolder(private val binding: GridContactItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(contactWithAllInformation: ContactWithAllInformation, position: Int) {
            binding.apply {
                val contact = contactWithAllInformation.contactDB
                if (contact != null) {
                    InitContactAdapter.initContact(
                        contact,
                        contactFirstName,
                        contactLastName,
                        contactImage,
                        len
                    )

                    contactPriorityBorder(contact, contactImage, cxt)

                    if (!modeMultiSelect || !listOfItemSelected.contains(contactWithAllInformation)) {
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
                    } else {
                        contactImage.setImageResource(R.drawable.ic_item_selected)
                    }

                    contactPriorityBorder(contact, contactImage, cxt)
                    if (contact.favorite == 1) {
                        favoriteShine.visibility = View.VISIBLE
                    } else {
                        favoriteShine.visibility = View.GONE
                    }

                    selectMenu?.let {
                        initMenuButton(contactImage, position, contactWithAllInformation, cxt, len, listCircularMenu,
                            listOfItemSelected,
                            it, modeMultiSelect, binding, menuStateChangeListener)
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

    fun callPhone(phoneNumber: String) {
        if (ContextCompat.checkSelfPermission(
                cxt,
                Manifest.permission.CALL_PHONE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val PERMISSION_CALL_RESULT = 1
            ActivityCompat.requestPermissions(
                (cxt as Activity),
                arrayOf(Manifest.permission.CALL_PHONE),
                PERMISSION_CALL_RESULT
            )
            phonePermission = phoneNumber
        } else {
            val sharedPreferences = cxt.getSharedPreferences("Phone_call", Context.MODE_PRIVATE)
            val popup = sharedPreferences.getBoolean("popup", true)
            if (popup && phonePermission.isEmpty()) {
                AlertDialog.Builder(cxt)
                    .setTitle(R.string.main_contact_grid_title)
                    .setMessage(R.string.main_contact_grid_message)
                    .setPositiveButton(android.R.string.yes) { dialog: DialogInterface?, id: Int ->
                        cxt.startActivity(
                            Intent(Intent.ACTION_CALL, Uri.fromParts("tel", phoneNumber, null))
                        )
                    }
                    .setNegativeButton(android.R.string.no, null)
                    .show()
            } else {
                cxt.startActivity(
                    Intent(
                        Intent.ACTION_CALL,
                        Uri.fromParts("tel", phoneNumber, null)
                    )
                )
                phonePermission = ""
            }
        }
    }

    override fun onMenuOpened(floatingActionMenu: FloatingActionMenu) {
        println("menu select")
        if (selectMenu != null) {
            selectMenu!!.close(false)
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
        println("menu close")
        selectMenu = null
    }

    /**
     * Ferme le menu qui est ouvert
     */
    fun closeMenu() {
        if (selectMenu != null) selectMenu?.close(true)
    }

    fun multiSelectMode(): Boolean {
        return modeMultiSelect
    }
}