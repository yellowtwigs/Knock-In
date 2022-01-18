package com.yellowtwigs.knockin.controller.activity.group

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu.MenuStateChangeListener
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.controller.CircularImageView
import com.yellowtwigs.knockin.controller.activity.EditContactActivity
import com.yellowtwigs.knockin.controller.activity.MainActivity
import com.yellowtwigs.knockin.databinding.GridContactItemLayoutBinding
import com.yellowtwigs.knockin.databinding.ListContactItemLayoutBinding
import com.yellowtwigs.knockin.utils.ContactGesture.openWhatsapp
import com.yellowtwigs.knockin.model.ContactManager
import com.yellowtwigs.knockin.model.ContactsRoomDatabase.Companion.getDatabase
import com.yellowtwigs.knockin.model.DbWorkerThread
import com.yellowtwigs.knockin.model.ModelDB.ContactWithAllInformation
import com.yellowtwigs.knockin.ui.adapters.ContactGridAdapter
import com.yellowtwigs.knockin.utils.ContactGesture.callPhone
import com.yellowtwigs.knockin.utils.ContactGesture.isWhatsappInstalled
import com.yellowtwigs.knockin.utils.ConvertBitmap.base64ToBitmap
import com.yellowtwigs.knockin.utils.ConverterPhoneNumber.converter06To33
import com.yellowtwigs.knockin.utils.InitContactAdapter
import com.yellowtwigs.knockin.utils.InitContactAdapter.contactPriorityBorder
import com.yellowtwigs.knockin.utils.RandomDefaultImage.randomDefaultImage
import java.util.*

/**
 * Adapter qui nous permet d'afficher un contact dans une section
 *
 * @author Florian Striebel
 */
class GroupAdapter(
    private val cxt: Activity, private val len: Int
) : ListAdapter<ContactWithAllInformation, GroupAdapter.ViewHolder>(
    ContactWithAllInformationComparator()
), MenuStateChangeListener {

    private var selectMenu: FloatingActionMenu? = null
    private val listCircularMenu = ArrayList<FloatingActionMenu>()
    private var modeMultiSelect = false
    private var secondClick = false
    val listOfItemSelected = ArrayList<ContactWithAllInformation>()
    private var sectionPos: ArrayList<Int>
    val menuStateChangeListener = this

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = if (len >= 4) {
            GridContactItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        } else {
            ListContactItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        }
        return ViewHolder(binding)
    }

    /**
     * Gère les affichages par contact
     *
     * @param holder
     * @param position
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = getItem(position)
        holder.onBind(contact, position)
    }

    /**
     * renvoie si l'utilisateur est en train de faire un multiselect
     *
     * @return [Boolean]
     */
    fun multiSelectMode(): Boolean {
        return modeMultiSelect
    }

    fun customGetItem(position: Int): ContactWithAllInformation {
        return currentList[position]
    }

    /**
     * écoute quand un menuCirculaire à été ouvert et ferme l'ancien menuCirculaire ouvert
     *
     * @param floatingActionMenu //menu qui est ouvert
     */
    override fun onMenuOpened(floatingActionMenu: FloatingActionMenu) {
        selectMenu?.close(false)
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

    /**
     * gère la multiselection d'un groupe par le click sur son libéllé
     *
     * @param position
     */
    fun setGroupClick(position: Int) {
        val main_mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        main_mDbWorkerThread.start()
        val listContactOnGroup = putGroupContactInItemSelected(position)
        if (!secondClick) {
            if (!modeMultiSelect) {
                modeMultiSelect = true
                for (i in listContactOnGroup.indices) {
                    if (!listOfItemSelected.contains(listContactOnGroup[i])) {
                        val positionItem = currentList.indexOf(listContactOnGroup[i])
                        (cxt as GroupManagerActivity).recyclerMultiSelectItemClick(
                            positionItem,
                            secondClick,
                            true
                        )
                        listOfItemSelected.add(currentList[positionItem])
                    }
                }
                secondClick = true
            }
        } else {
            for (i in listContactOnGroup.indices) {
                if (listOfItemSelected.contains(listContactOnGroup[i])) {
                    val positionItem = currentList.indexOf(listContactOnGroup[i])
                    (cxt as GroupManagerActivity).recyclerMultiSelectItemClick(
                        positionItem,
                        secondClick,
                        true
                    )
                    listOfItemSelected.remove(currentList[positionItem])
                }
            }
            if (listOfItemSelected.size == 0) {
                secondClick = false
                modeMultiSelect = false
            }
        }
    }

    /**
     * Supression de la liste l'item à la position [position]
     *
     * @param position [Int]
     */
    fun removeItem(position: Int) {
        currentList.removeAt(position)
        notifyItemRemoved(position)
    }

    /**
     * setter sectionPos
     *
     * @param position ArrayList<Integer>
    </Integer> */
    fun setSectionPos(position: ArrayList<Int>) {
        sectionPos = position
    }

    /**
     * Retourne la position de la section dont fait partie le contact
     *
     * @param position [Int]
     * @return [Int]
     */
    private fun getSectionnedPosition(position: Int): Int {
        for (i in sectionPos.size - 1 downTo 1) {
            if (sectionPos[i] <= position) {
                return sectionPos[i]
            }
        }
        return 0
    }

    /**
     * Ajout des contact faisant partie du groupe dans une list
     *
     * @param position
     * @return [List<ContactWithAllInformation>]
    </ContactWithAllInformation> */
    private fun putGroupContactInItemSelected(position: Int): List<ContactWithAllInformation> {
        val contactsDatabase = getDatabase(cxt)
        val main_mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        main_mDbWorkerThread.start()
        val group = contactsDatabase!!.GroupsDao().getAllGroupsByNameAZ()[position]
        return group.getListContact(cxt)
    }

    /**
     * Constructeur de GroupAdapter
     *
     * @param context        [Context]
     * @param contactManager [ContactManager]
     * @param len            [Integer]
     */
    init {
        sectionPos = ArrayList()
    }

    inner class ViewHolder(private val binding: ViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(contactWithAllInformation: ContactWithAllInformation, position: Int) {
            val contact = contactWithAllInformation.contactDB
            if (contact != null) {
                if (len == 1) {
                    val listBinding = (binding as ListContactItemLayoutBinding)
                    listBinding.apply {
                        InitContactAdapter.initContact(
                            contact,
                            firstName,
                            lastName,
                            contactImage,
                            len
                        )

                        contactPriorityBorder(contact, contactImage, cxt)

                        if (modeMultiSelect && listOfItemSelected.contains(contactWithAllInformation)) {
                            contactImage.setImageResource(R.drawable.ic_item_selected)
                        } else {
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
                        }

                        selectMenu?.let {
                            InitContactAdapter.initMenuButton(
                                contactImage,
                                position,
                                contactWithAllInformation,
                                cxt,
                                len,
                                listCircularMenu,
                                listOfItemSelected,
                                it,
                                modeMultiSelect,
                                listBinding,
                                menuStateChangeListener
                            )
                        }
                    }
                } else {
                    val gridBinding = (binding as GridContactItemLayoutBinding)
                    gridBinding.apply {
                        InitContactAdapter.initContact(
                            contact,
                            contactFirstName,
                            contactLastName,
                            contactImage,
                            len
                        )

                        contactPriorityBorder(contact, contactImage, cxt)

                        if (modeMultiSelect && listOfItemSelected.contains(contactWithAllInformation)) {
                            contactImage.setImageResource(R.drawable.ic_item_selected)
                        } else {
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
                        }

                        selectMenu?.let {
                            InitContactAdapter.initMenuButton(
                                contactImage,
                                position,
                                contactWithAllInformation,
                                cxt,
                                len,
                                listCircularMenu,
                                listOfItemSelected,
                                it,
                                modeMultiSelect,
                                gridBinding,
                                menuStateChangeListener
                            )
                        }
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
}