package com.yellowtwigs.knockin.controller.activity.group

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.util.Base64
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
import androidx.recyclerview.widget.RecyclerView
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu.MenuStateChangeListener
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.controller.CircularImageView
import com.yellowtwigs.knockin.ui.edit_contact.EditContactDetailsActivity
import com.yellowtwigs.knockin.utils.ContactGesture.openWhatsapp
import com.yellowtwigs.knockin.model.ContactManager
import com.yellowtwigs.knockin.model.ContactsRoomDatabase.Companion.getDatabase
import com.yellowtwigs.knockin.model.DbWorkerThread
import com.yellowtwigs.knockin.model.ModelDB.ContactWithAllInformation
import java.util.*

/**
 * Adapter qui nous permet d'afficher un contact dans une section
 *
 * @author Florian Striebel
 */
class GroupAdapter(private val context: Context,
                   /**
                    * renvoi le contact manager de l'adapter
                    *
                    * @return [ContactManager]
                    */
                   private val contactManager: ContactManager, private val len: Int) : RecyclerView.Adapter<GroupAdapter.ViewHolder>(), MenuStateChangeListener {
    private var selectMenu: FloatingActionMenu? = null
    private val listCircularMenu = ArrayList<FloatingActionMenu>()
    private var numberForPermission = ""
    private var modeMultiSelect = false
    private var secondClick = false

    /**
     * renvoie la list des contact qui sont multiselecté
     *
     * @return [ArrayList<ContactWithAllInformation>]
    </ContactWithAllInformation> */
    val listOfItemSelected = ArrayList<ContactWithAllInformation>()
    private var sectionPos: ArrayList<Int>
    private var heightWidthImage = 0
    private var view: View? = null
    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: List<Any>) {
        super.onBindViewHolder(holder, position, payloads)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        view = if (len >= 4) {
            LayoutInflater.from(context).inflate(R.layout.grid_contact_item_layout, parent, false)
        } else {
            LayoutInflater.from(context).inflate(R.layout.list_contact_item_layout, parent, false)
        }
        val holder = ViewHolder(view!!)
        heightWidthImage = holder.contactRoundedImageView!!.layoutParams.height
        return holder
    }

    /**
     * Gère les affichages par contact
     *
     * @param holder
     * @param position
     */
    @SuppressLint("ResourceType")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val height = heightWidthImage
        val width = heightWidthImage
        println(" layout params height $height width $width")
        val layoutParamsTV = holder.contactFirstNameView!!.layoutParams as RelativeLayout.LayoutParams
        val layoutParamsIV = if (len != 1) {
            holder.contactRoundedImageView!!.layoutParams as ConstraintLayout.LayoutParams
        } else {
            holder.contactRoundedImageView!!.layoutParams as RelativeLayout.LayoutParams
        }

        when (len) {
            1 -> {
                holder.contactRoundedImageView!!.layoutParams.height = (heightWidthImage - heightWidthImage * 0.05).toInt()
                holder.contactRoundedImageView!!.layoutParams.width = (heightWidthImage - heightWidthImage * 0.05).toInt()
                layoutParamsTV.topMargin = 30
                layoutParamsIV.topMargin = 10
            }
            3 -> {
                holder.contactRoundedImageView!!.layoutParams.height = (heightWidthImage - heightWidthImage * 0.05).toInt()
                holder.contactRoundedImageView!!.layoutParams.width = (heightWidthImage - heightWidthImage * 0.05).toInt()
                layoutParamsTV.topMargin = 30
                layoutParamsIV.topMargin = 10
            }
            4 -> {
                holder.contactRoundedImageView!!.layoutParams.height = (heightWidthImage - heightWidthImage * 0.25).toInt()
                holder.contactRoundedImageView!!.layoutParams.width = (heightWidthImage - heightWidthImage * 0.25).toInt()
                layoutParamsTV.topMargin = 10
                layoutParamsIV.topMargin = 10
            }
            5 -> {
                holder.contactRoundedImageView!!.layoutParams.height = (heightWidthImage - heightWidthImage * 0.40).toInt()
                holder.contactRoundedImageView!!.layoutParams.width = (heightWidthImage - heightWidthImage * 0.40).toInt()
                layoutParamsTV.topMargin = 0
                layoutParamsIV.topMargin = 0
            }
            6 -> {
                holder.contactRoundedImageView!!.layoutParams.height = (heightWidthImage - heightWidthImage * 0.50).toInt()
                holder.contactRoundedImageView!!.layoutParams.width = (heightWidthImage - heightWidthImage * 0.50).toInt()
                layoutParamsTV.topMargin = 0
                layoutParamsIV.topMargin = 0
            }
        }
        val contact = contactManager.contactList[position].contactDB!!
        if (contact.contactPriority == 0) {
            holder.contactRoundedImageView!!.setBorderColor(context.resources.getColor(R.color.priorityZeroColor, null))
        } else if (contact.contactPriority == 1) {
            holder.contactRoundedImageView!!.setBorderColor(context.resources.getColor(R.color.transparentColor, null))
        } else if (contact.contactPriority == 2) {
            holder.contactRoundedImageView!!.setBorderColor(context.resources.getColor(R.color.priorityTwoColor, null))
        }
        if (modeMultiSelect && listOfItemSelected.contains(contactManager.contactList[position])) {
            holder.contactRoundedImageView!!.setImageResource(R.drawable.ic_item_selected)
        } else {
            if (contact.profilePicture64 != "") {
                val bitmap = base64ToBitmap(contact.profilePicture64)
                holder.contactRoundedImageView!!.setImageBitmap(bitmap)
            } else {
                holder.contactRoundedImageView!!.setImageResource(randomDefaultImage(contact.profilePicture)) //////////////
            }
        }
        //region set libellé group
        // ContactsRoomDatabase main_ContactsDatabase=ContactsRoomDatabase.Companion.getDatabase(context);
        //DbWorkerThread main_mDbWorkerThread=new DbWorkerThread("dbWorkerThread");
        //main_mDbWorkerThread.start() ;
        //List<GroupDB> listDB=main_ContactsDatabase.GroupsDao().getGroupForContact(contact.getId());
        //endregion
        // getItem(position).getFirstGroup(context);
        var firstname = contact.firstName
        var lastName = contact.lastName
        var group = ""

        when (len) {
            1 -> {
                val spanFistName: Spannable = SpannableString(firstname)
                spanFistName.setSpan(RelativeSizeSpan(0.95f), 0, firstname.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                holder.contactFirstNameView!!.text = spanFistName
                val spanLastName: Spannable = SpannableString(lastName)
                spanLastName.setSpan(RelativeSizeSpan(0.95f), 0, lastName.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                holder.contactLastNameView!!.text = spanLastName
            }
            3 -> {
                val spanFistName: Spannable = SpannableString(firstname)
                spanFistName.setSpan(RelativeSizeSpan(0.95f), 0, firstname.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                holder.contactFirstNameView!!.text = spanFistName
                val spanLastName: Spannable = SpannableString(lastName)
                spanLastName.setSpan(RelativeSizeSpan(0.95f), 0, lastName.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                holder.contactLastNameView!!.text = spanLastName
            }
            4 -> {
                if (contact.firstName.length > 12) firstname = contact.firstName.substring(0, 10) + ".."
                val spanFistName: Spannable = SpannableString(firstname)
                spanFistName.setSpan(RelativeSizeSpan(0.95f), 0, firstname.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                holder.contactFirstNameView!!.text = spanFistName
                if (contact.lastName.length > 12) lastName = contact.lastName.substring(0, 10) + ".."
                val spanLastName: Spannable = SpannableString(lastName)
                spanLastName.setSpan(RelativeSizeSpan(0.95f), 0, lastName.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                holder.contactLastNameView!!.text = spanLastName
                if (group.length > 9) group = group.substring(0, 8) + ".."
                spanLastName.setSpan(RelativeSizeSpan(0.95f), 0, lastName.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            5 -> {
                if (contact.firstName.length > 11) firstname = contact.firstName.substring(0, 9) + ".."
                holder.contactFirstNameView!!.text = firstname
                val span: Spannable = SpannableString(holder.contactFirstNameView!!.text)
                span.setSpan(RelativeSizeSpan(0.9f), 0, firstname.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                holder.contactFirstNameView!!.text = span
                //holder.contactFirstNameView.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary, null));
                if (contact.lastName.length > 11) lastName = contact.lastName.substring(0, 9) + ".."
                val spanLastName: Spannable = SpannableString(lastName)
                spanLastName.setSpan(RelativeSizeSpan(0.9f), 0, lastName.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                holder.contactLastNameView!!.text = spanLastName
            }
        }

        /*   if (!contact.getProfilePicture64().equals("")) {
            Bitmap bitmap = base64ToBitmap(contact.getProfilePicture64());
            holder.contactRoundedImageView.setImageBitmap(bitmap);
        } else {
            holder.contactRoundedImageView.setImageResource(randomDefaultImage(contact.getProfilePicture())); //////////////
        }*/
        //region circular menu

        //final ImageView buttonMessenger = new ImageView(context);
        val buttonCall = ImageView(context)
        val buttonWhatsApp = ImageView(context)
        val buttonSMS = ImageView(context)
        val buttonEdit = ImageView(context)
        val buttonMail = ImageView(context)

        //  buttonMessenger.setId(0);
        buttonCall.id = 1
        buttonSMS.id = 2
        buttonWhatsApp.id = 3
        buttonEdit.id = 4
        buttonMail.id = 5

        //buttonMessenger.setImageDrawable(iconMessenger);
        buttonCall.setImageResource(R.drawable.ic_google_call)
        buttonWhatsApp.setImageResource(R.drawable.ic_circular_whatsapp)
        buttonSMS.setImageResource(R.drawable.ic_sms_selector)
        buttonEdit.setImageResource(R.drawable.ic_circular_edit)
        buttonMail.setImageResource(R.drawable.ic_circular_mail)
        val builderIcon = SubActionButton.Builder(context as Activity)
        builderIcon.setBackgroundDrawable(context.getDrawable(R.drawable.ic_circular))
        builderIcon.setContentView(buttonCall)
        val startAngle: Int
        val endAngle: Int
        when {
            (position - getSectionnedPosition(position)) % len == 0 -> {
                println("position vaut " + position + " modulo" + len + " vaut" + position % len)
                startAngle = 90
                endAngle = -90
            }
            (position - getSectionnedPosition(position)) % len == len - 1 -> {
                println("position vaut " + position + " modulo" + len + " vaut" + position % len)
                startAngle = 90
                endAngle = 270
            }
            else -> {
                println("position vaut " + position + " modulo" + len + " vaut" + position % len)
                startAngle = 0
                endAngle = -180
            }
        }
        val metrics = DisplayMetrics()
        context.windowManager.defaultDisplay.getMetrics(metrics)
        val diametreBoutton = (0.35 * metrics.densityDpi).toInt()
        val radiusMenu = (0.45 * metrics.densityDpi).toInt()
        val border = (0.0625 * metrics.densityDpi).toInt()
        val layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        layoutParams.setMargins(5, 5, 5, 5)
        val builder = FloatingActionMenu.Builder(context)
                .setStartAngle(startAngle)
                .setEndAngle(endAngle)
                .setRadius(radiusMenu)
                .addSubActionView(builderIcon.setContentView(buttonEdit, layoutParams).build(), diametreBoutton, diametreBoutton)
                .attachTo(holder.contactRoundedImageView)
                .setStateChangeListener(this)
                .disableAnimations()
        if (appIsInstalled() && getItem(position).getFirstPhoneNumber() != "") {
            builder.addSubActionView(builderIcon.setContentView(buttonWhatsApp, layoutParams).build(), diametreBoutton, diametreBoutton)
        }
        if (getItem(position).getFirstMail() != "") {
            builder.addSubActionView(builderIcon.setContentView(buttonMail, layoutParams).build(), diametreBoutton, diametreBoutton)
        }
        if (getItem(position).getFirstPhoneNumber() != "") {
            builder.addSubActionView(builderIcon.setContentView(buttonSMS, layoutParams).build(), diametreBoutton, diametreBoutton)
                    .addSubActionView(builderIcon.setContentView(buttonCall, layoutParams).build(), diametreBoutton, diametreBoutton)
        }

        /* if( appIsInstalled( "com.facebook.orca")){
            builder.addSubActionView(builderIcon.setContentView(buttonMessenger,layoutParams).build(),diametreBoutton,diametreBoutton);
        }*/
        val quickMenu = builder.build()
        listCircularMenu.add(quickMenu)
        //  quickMenu.addSubActionView(builderIcon.setContentView(buttonSMS,layoutParams).build(),diametreBoutton,diametreBoutton)
        val buttonListener = View.OnClickListener { v: View ->

            /* if (v.getId() == buttonMessenger.getId()) {
                try {
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.messenger.com/t/" + "")));
                } catch (ActivityNotFoundException e) {
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.messenger.com/t/" + "")));
                }
            } else*/
            when (v.id) {
                buttonWhatsApp.id -> {
                    val contactWithAllInformation = getItem(position)
                    openWhatsapp(converter06To33(contactWithAllInformation.getFirstPhoneNumber()), context)
                }
                buttonEdit.id -> {
                    val intent = Intent(context, EditContactDetailsActivity::class.java)
                    intent.putExtra("ContactId", contact.id)
                    intent.putExtra("fromGroupActivity", true)
                    context.startActivity(intent)
                }
                buttonCall.id -> {
                    callPhone(getItem(position).getFirstPhoneNumber())
                }
                buttonSMS.id -> {
                    val phone = getItem(position).getFirstPhoneNumber()
                    val i = Intent(Intent.ACTION_SENDTO, Uri.fromParts("sms", phone, null))
                    context.startActivity(i)
                }
                buttonMail.id -> {
                    val mail = getItem(position).getFirstMail()
                    val intent = Intent(Intent.ACTION_SENDTO)
                    intent.data = Uri.parse("mailto:")
                    //intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(mail))
                    intent.putExtra(Intent.EXTRA_SUBJECT, "")
                    intent.putExtra(Intent.EXTRA_TEXT, "")
                    println("intent " + Objects.requireNonNull(intent.extras).toString())
                    context.startActivity(intent)
                }
            }
            selectMenu!!.close(false)
        }
        val gridlongClick = OnLongClickListener { v: View ->
            if (!modeMultiSelect) {
                v.tag = holder
                assert(contact != null)
                holder.contactFirstNameView!!.text = contact.firstName
                if (listOfItemSelected.contains(contactManager.contactList[position])) {
                    listOfItemSelected.remove(contactManager.contactList[position])
                    if (contact.profilePicture64 != "") {
                        val bitmap = base64ToBitmap(contact.profilePicture64)
                        holder.contactRoundedImageView!!.setImageBitmap(bitmap)
                    } else {
                        listOfItemSelected.add(contactManager.contactList[position])
                        holder.contactRoundedImageView!!.setImageResource(R.drawable.ic_item_selected)
                        notifyDataSetChanged()
                    }
                } else {
                    listOfItemSelected.add(contactManager.contactList[position])
                    holder.contactRoundedImageView!!.setImageResource(R.drawable.ic_item_selected)
                    notifyDataSetChanged()
                }
                closeMenu()
                (context as GroupManagerActivity).gridLongItemClick(position)
                modeMultiSelect = true
            }
            true
        }
        val gridItemClick = View.OnClickListener { v: View? ->
            if (modeMultiSelect) {
                if (listOfItemSelected.contains(contactManager.contactList[position])) {
                    listOfItemSelected.remove(contactManager.contactList[position])
                    if (contact.profilePicture64 != "") {
                        val bitmap = base64ToBitmap(contact.profilePicture64)
                        holder.contactRoundedImageView!!.setImageBitmap(bitmap)
                    } else {
                        holder.contactRoundedImageView!!.setImageResource(randomDefaultImage(contact.profilePicture))
                    }
                    if (listOfItemSelected.isEmpty()) {
                        modeMultiSelect = false
                    }
                    notifyDataSetChanged()
                } else {
                    listOfItemSelected.add(contactManager.contactList[position])
                    holder.contactRoundedImageView!!.setImageResource(R.drawable.ic_item_selected)
                    notifyDataSetChanged()
                }
                (context as GroupManagerActivity).gridLongItemClick(position)
            } else {
                if (quickMenu.isOpen) {
                    quickMenu.close(false)
                } else {
                    quickMenu.open(false)
                }
            }
        }
        buttonCall.setOnLongClickListener {
            val phoneNumber = getItem(position).getSecondPhoneNumber(getItem(position).getFirstPhoneNumber())
            if (phoneNumber.isNotEmpty()) {
                callPhone(phoneNumber)
            }
            true
        }

        holder.contactRoundedImageView!!.setOnLongClickListener(gridlongClick)
        holder.contactRoundedImageView!!.setOnClickListener(gridItemClick)
        //buttonMessenger.setOnClickListener(buttonListener);
        buttonWhatsApp.setOnClickListener(buttonListener)
        buttonCall.setOnClickListener(buttonListener)
        buttonSMS.setOnClickListener(buttonListener)
        buttonEdit.setOnClickListener(buttonListener)
        buttonMail.setOnClickListener(buttonListener)
    }

    /**
     * renvoie si l'utilisateur est en train de faire un multiselect
     *
     * @return [Boolean]
     */
    fun multiSelectMode(): Boolean {
        return modeMultiSelect
    }

    /**
     * écoute quand un menuCirculaire à été ouvert et ferme l'ancien menuCirculaire ouvert
     *
     * @param floatingActionMenu //menu qui est ouvert
     */
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
        if (selectMenu != null) selectMenu!!.close(true)
    }

    /**
     * appelle le numéro de téléphone passé en paramètre
     *
     * @param phoneNumber
     */
    fun callPhone(phoneNumber: String) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            val PERMISSION_CALL_RESULT = 1
            ActivityCompat.requestPermissions((context as Activity), arrayOf(Manifest.permission.CALL_PHONE), PERMISSION_CALL_RESULT)
            numberForPermission = phoneNumber
        } else {
            val sharedPreferences = context.getSharedPreferences("Phone_call", Context.MODE_PRIVATE)
            val popup = sharedPreferences.getBoolean("popup", true)
            if (popup && numberForPermission.isEmpty()) {
                AlertDialog.Builder(context)
                        .setTitle(R.string.main_contact_grid_title)
                        .setMessage(R.string.main_contact_grid_message)
                        .setPositiveButton(android.R.string.yes) { dialog: DialogInterface?, id: Int -> context.startActivity(Intent(Intent.ACTION_CALL, Uri.fromParts("tel", phoneNumber, null))) }
                        .setNegativeButton(android.R.string.no, null)
                        .show()
            } else {
                context.startActivity(Intent(Intent.ACTION_CALL, Uri.fromParts("tel", phoneNumber, null)))
                numberForPermission = ""
            }
        }
    }

    /**
     * gère la multiselection d'un groupe par le click sur son libéllé
     *
     * @param position
     */
    fun SetGroupClick(position: Int) {
        val main_mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        main_mDbWorkerThread.start()
        println("list contact grid size" + contactManager.contactList.size)
        val listContactOnGroup = putGroupContactInItemSelected(position)
        if (!secondClick) {
            if (!modeMultiSelect) {
                modeMultiSelect = true
                for (i in listContactOnGroup.indices) {
                    if (!listOfItemSelected.contains(listContactOnGroup[i])) {
                        val positionItem = contactManager.contactList.indexOf(listContactOnGroup[i])
                        (context as GroupManagerActivity).recyclerMultiSelectItemClick(positionItem, secondClick, true)
                        listOfItemSelected.add(contactManager.contactList[positionItem])
                    }
                }
                secondClick = true
                notifyDataSetChanged()
            }
        } else {
            for (i in listContactOnGroup.indices) {
                if (listOfItemSelected.contains(listContactOnGroup[i])) {
                    val positionItem = contactManager.contactList.indexOf(listContactOnGroup[i])
                    (context as GroupManagerActivity).recyclerMultiSelectItemClick(positionItem, secondClick, true)
                    listOfItemSelected.remove(contactManager.contactList[positionItem])
                }
            }
            if (listOfItemSelected.size == 0) {
                secondClick = false
                modeMultiSelect = false
            }
            notifyDataSetChanged()
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var contactFirstNameView: TextView? = null
        var contactLastNameView: TextView? = null
        var contactRoundedImageView: CircularImageView? = null
        var open: Boolean? = null

        init {
            if (len >= 4) {
                contactRoundedImageView = view.findViewById(R.id.contactRoundedImageView)
                contactFirstNameView = view.findViewById(R.id.grid_adapter_contactFirstName)
                contactLastNameView = view.findViewById(R.id.grid_adapter_contactLastName)
                heightWidthImage = contactRoundedImageView!!.height
            } else {
                contactRoundedImageView = view.findViewById(R.id.list_contact_item_contactRoundedImageView)
                contactFirstNameView = view.findViewById(R.id.list_contact_item_contactFirstName)
                contactLastNameView = view.findViewById(R.id.list_contact_item_contactLastName)
                heightWidthImage = contactRoundedImageView!!.height
                open = false
            }
        } /*        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }*/
    }

    /**
     * Récupère le contact à la position [position]
     *
     * @param position [Int]
     * @return [ContactWithAllInformation]
     */
    fun getItem(position: Int): ContactWithAllInformation {
        return contactManager.contactList[position]
    }

    /**
     * Supression de la liste l'item à la position [position]
     *
     * @param position [Int]
     */
    fun removeItem(position: Int) {
        contactManager.contactList.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun getItemCount(): Int {
        return contactManager.contactList.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    /**
     * rajoute un indicatif au numéro
     *
     * @param phoneNumber [String]
     * @return [String]
     */
    private fun converter06To33(phoneNumber: String): String {
        return if (phoneNumber[0] == '0') {
            "+33$phoneNumber"
        } else phoneNumber
    }

    /**
     * Renvoie l'image du contact sous forme de ressource
     *
     * @param avatarId [Int]
     * @return [Int]
     */
    private fun randomDefaultImage(avatarId: Int): Int {
        val sharedPreferencesIsMultiColor = context.getSharedPreferences("IsMultiColor", Context.MODE_PRIVATE)
        val multiColor = sharedPreferencesIsMultiColor.getInt("isMultiColor", 0)
        val sharedPreferencesContactsColor = context.getSharedPreferences("ContactsColor", Context.MODE_PRIVATE)
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

    /**
     * Convertit les image de base64 en Bitmap
     *
     * @param base64
     * @return
     */
    private fun base64ToBitmap(base64: String): Bitmap {
        val decodedString = Base64.decode(base64, Base64.DEFAULT)
        val options = BitmapFactory.Options()
        //options.inSampleSize = 2;
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size, options)
    } //TODO enlever code duplicate

    /**
     * vérifie que le téléphone possède l'application whatsApp
     *
     * @return [Boolean]
     */
    private fun appIsInstalled(): Boolean {
        val pm = context.packageManager
        return try {
            pm.getApplicationInfo("com.whatsapp", 0)
            true
        } catch (e: Exception) {
            false
        }
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
        val contactsDatabase = getDatabase(context)
        val main_mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        main_mDbWorkerThread.start()
        val group = contactsDatabase!!.GroupsDao().getAllGroupsByNameAZ()[position]
        return group.getListContact(context)
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
}