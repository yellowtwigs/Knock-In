//package com.yellowtwigs.knockin.ui.groups.list
//
//import android.Manifest
//import android.app.Activity
//import android.app.AlertDialog
//import android.content.Context
//import android.content.DialogInterface
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.net.Uri
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import androidx.recyclerview.widget.RecyclerView
//import com.yellowtwigs.knockin.R
//import java.util.*
//
//class GroupAdapter(private val context: Context) :
//
//
//    RecyclerView.Adapter<GroupAdapter.ViewHolder>() {
//
//
//    private var numberForPermission = ""
//    private var modeMultiSelect = false
//    private var secondClick = false
//
//    private var sectionPos: ArrayList<Int>
//    private var heightWidthImage = 0
//    private var view: View? = null
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: List<Any>) {
//        super.onBindViewHolder(holder, position, payloads)
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        view = if (len >= 4) {
//            LayoutInflater.from(context).inflate(R.layout.grid_contact_item_layout, parent, false)
//        } else {
//            LayoutInflater.from(context).inflate(R.layout.item_contact_list, parent, false)
//        }
//        val holder = ViewHolder(view!!)
//        heightWidthImage = holder.contactRoundedImageView!!.layoutParams.height
//        return holder
//    }
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
////        val layoutParamsTV =
////            holder.contactFirstNameView?.layoutParams as RelativeLayout.LayoutParams
////        val layoutParamsIV = if (len != 1) {
////            holder.contactRoundedImageView?.layoutParams as ConstraintLayout.LayoutParams
////        } else {
////            holder.contactRoundedImageView?.layoutParams as RelativeLayout.LayoutParams
////        }
////
////        when (len) {
////            1 -> {
////                holder.contactRoundedImageView!!.layoutParams.height =
////                    (heightWidthImage - heightWidthImage * 0.05).toInt()
////                holder.contactRoundedImageView!!.layoutParams.width =
////                    (heightWidthImage - heightWidthImage * 0.05).toInt()
////                layoutParamsTV.topMargin = 30
////                layoutParamsIV.topMargin = 10
////            }
////            4 -> {
////                holder.contactRoundedImageView?.layoutParams?.height =
////                    (heightWidthImage - heightWidthImage * 0.25).toInt()
////                holder.contactRoundedImageView?.layoutParams?.width =
////                    (heightWidthImage - heightWidthImage * 0.25).toInt()
////                layoutParamsTV.topMargin = 10
////                layoutParamsIV.topMargin = 10
////            }
////            5 -> {
////                holder.contactRoundedImageView?.layoutParams?.height =
////                    (heightWidthImage - heightWidthImage * 0.40).toInt()
////                holder.contactRoundedImageView?.layoutParams?.width =
////                    (heightWidthImage - heightWidthImage * 0.40).toInt()
////                layoutParamsTV.topMargin = 0
////                layoutParamsIV.topMargin = 0
////            }
////        }
////        val contact = contactManager.contactList[position].contactDB!!
////        when (contact.contactPriority) {
////            0 -> {
////                holder.contactRoundedImageView?.setBorderColor(
////                    context.resources.getColor(
////                        R.color.priorityZeroColor,
////                        null
////                    )
////                )
////            }
////            1 -> {
////                holder.contactRoundedImageView?.setBorderColor(
////                    context.resources.getColor(
////                        R.color.transparentColor,
////                        null
////                    )
////                )
////            }
////            2 -> {
////                holder.contactRoundedImageView?.setBorderColor(
////                    context.resources.getColor(
////                        R.color.priorityTwoColor,
////                        null
////                    )
////                )
////            }
////        }
////        if (modeMultiSelect && listOfItemSelected.contains(contactManager.contactList[position])) {
////            holder.contactRoundedImageView!!.setImageResource(R.drawable.ic_item_selected)
////        } else {
////            if (contact.profilePicture64 != "") {
////                val bitmap = base64ToBitmap(contact.profilePicture64)
////                holder.contactRoundedImageView!!.setImageBitmap(bitmap)
////            } else {
////                holder.contactRoundedImageView!!.setImageResource(randomDefaultImage(contact.profilePicture)) //////////////
////            }
////        }
////        var firstname = contact.firstName
////        var lastName = contact.lastName
////        var group = ""
////
////        when (len) {
////            1 -> {
////                val spanFistName: Spannable = SpannableString(firstname)
////                spanFistName.setSpan(
////                    RelativeSizeSpan(0.95f),
////                    0,
////                    firstname.length,
////                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
////                )
////                holder.contactFirstNameView!!.text = spanFistName
////                val spanLastName: Spannable = SpannableString(lastName)
////                spanLastName.setSpan(
////                    RelativeSizeSpan(0.95f),
////                    0,
////                    lastName.length,
////                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
////                )
////                holder.contactLastNameView!!.text = spanLastName
////            }
////            4 -> {
////                if (contact.firstName.length > 12) firstname =
////                    contact.firstName.substring(0, 10) + ".."
////                val spanFistName: Spannable = SpannableString(firstname)
////                spanFistName.setSpan(
////                    RelativeSizeSpan(0.95f),
////                    0,
////                    firstname.length,
////                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
////                )
////                holder.contactFirstNameView!!.text = spanFistName
////                if (contact.lastName.length > 12) lastName =
////                    contact.lastName.substring(0, 10) + ".."
////                val spanLastName: Spannable = SpannableString(lastName)
////                spanLastName.setSpan(
////                    RelativeSizeSpan(0.95f),
////                    0,
////                    lastName.length,
////                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
////                )
////                holder.contactLastNameView!!.text = spanLastName
////                if (group.length > 9) group = group.substring(0, 8) + ".."
////                spanLastName.setSpan(
////                    RelativeSizeSpan(0.95f),
////                    0,
////                    lastName.length,
////                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
////                )
////            }
////            5 -> {
////                if (contact.firstName.length > 11) firstname =
////                    contact.firstName.substring(0, 9) + ".."
////                holder.contactFirstNameView!!.text = firstname
////                val span: Spannable = SpannableString(holder.contactFirstNameView!!.text)
////                span.setSpan(
////                    RelativeSizeSpan(0.9f),
////                    0,
////                    firstname.length,
////                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
////                )
////                holder.contactFirstNameView!!.text = span
////                if (contact.lastName.length > 11) lastName = contact.lastName.substring(0, 9) + ".."
////                val spanLastName: Spannable = SpannableString(lastName)
////                spanLastName.setSpan(
////                    RelativeSizeSpan(0.9f),
////                    0,
////                    lastName.length,
////                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
////                )
////                holder.contactLastNameView!!.text = spanLastName
////            }
////        }
////
////        val gridlongClick = OnLongClickListener { v: View ->
////            if (!modeMultiSelect) {
////                v.tag = holder
////                assert(contact != null)
////                holder.contactFirstNameView!!.text = contact.firstName
////                if (listOfItemSelected.contains(contactManager.contactList[position])) {
////                    listOfItemSelected.remove(contactManager.contactList[position])
////                    if (contact.profilePicture64 != "") {
////                        val bitmap = base64ToBitmap(contact.profilePicture64)
////                        holder.contactRoundedImageView!!.setImageBitmap(bitmap)
////                    } else {
////                        listOfItemSelected.add(contactManager.contactList[position])
////                        holder.contactRoundedImageView!!.setImageResource(R.drawable.ic_item_selected)
////                        notifyDataSetChanged()
////                    }
////                } else {
////                    listOfItemSelected.add(contactManager.contactList[position])
////                    holder.contactRoundedImageView!!.setImageResource(R.drawable.ic_item_selected)
////                    notifyDataSetChanged()
////                }
////                closeMenu()
////                (context as GroupManagerActivity).gridLongItemClick(position)
////                modeMultiSelect = true
////            }
////            true
////        }
////        val gridItemClick = View.OnClickListener { v: View? ->
////            if (modeMultiSelect) {
////                if (listOfItemSelected.contains(contactManager.contactList[position])) {
////                    listOfItemSelected.remove(contactManager.contactList[position])
////                    if (contact.profilePicture64 != "") {
////                        val bitmap = base64ToBitmap(contact.profilePicture64)
////                        holder.contactRoundedImageView!!.setImageBitmap(bitmap)
////                    } else {
////                        holder.contactRoundedImageView!!.setImageResource(randomDefaultImage(contact.profilePicture))
////                    }
////                    if (listOfItemSelected.isEmpty()) {
////                        modeMultiSelect = false
////                    }
////                    notifyDataSetChanged()
////                } else {
////                    listOfItemSelected.add(contactManager.contactList[position])
////                    holder.contactRoundedImageView!!.setImageResource(R.drawable.ic_item_selected)
////                    notifyDataSetChanged()
////                }
////                (context as GroupManagerActivity).gridLongItemClick(position)
////            } else {
////                (context as GroupManagerActivity).startActivity(
////                    Intent(
////                        context,
////                        ContactSelectedWithAppsActivity::class.java
////                    ).putExtra("id", getItem(position).getContactId())
////                )
////            }
////        }
////
////        holder.contactRoundedImageView?.setOnLongClickListener(gridlongClick)
////        holder.contactRoundedImageView?.setOnClickListener(gridItemClick)
//    }
//
//    fun multiSelectMode(): Boolean {
//        return modeMultiSelect
//    }
//
//    fun callPhone(phoneNumber: String) {
//        if (ContextCompat.checkSelfPermission(
//                context,
//                Manifest.permission.CALL_PHONE
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            val PERMISSION_CALL_RESULT = 1
//            ActivityCompat.requestPermissions(
//                (context as Activity),
//                arrayOf(Manifest.permission.CALL_PHONE),
//                PERMISSION_CALL_RESULT
//            )
//            numberForPermission = phoneNumber
//        } else {
//            val sharedPreferences = context.getSharedPreferences("Phone_call", Context.MODE_PRIVATE)
//            val popup = sharedPreferences.getBoolean("popup", true)
//            if (popup && numberForPermission.isEmpty()) {
//                AlertDialog.Builder(context)
//                    .setTitle(R.string.main_contact_grid_title)
//                    .setMessage(R.string.main_contact_grid_message)
//                    .setPositiveButton(android.R.string.yes) { dialog: DialogInterface?, id: Int ->
//                        context.startActivity(
//                            Intent(Intent.ACTION_CALL, Uri.fromParts("tel", phoneNumber, null))
//                        )
//                    }
//                    .setNegativeButton(android.R.string.no, null)
//                    .show()
//            } else {
//                context.startActivity(
//                    Intent(
//                        Intent.ACTION_CALL,
//                        Uri.fromParts("tel", phoneNumber, null)
//                    )
//                )
//                numberForPermission = ""
//            }
//        }
//    }
//
//    /**
//     * gère la multiselection d'un groupe par le click sur son libéllé
//     *
//     * @param position
//     */
////    fun SetGroupClick(position: Int) {
////        val main_mDbWorkerThread = DbWorkerThread("dbWorkerThread")
////        main_mDbWorkerThread.start()
////        println("list contact grid size" + contactManager.contactList.size)
////        val listContactOnGroup = putGroupContactInItemSelected(position)
////        if (!secondClick) {
////            if (!modeMultiSelect) {
////                modeMultiSelect = true
////                for (i in listContactOnGroup.indices) {
////                    if (!listOfItemSelected.contains(listContactOnGroup[i])) {
////                        val positionItem = contactManager.contactList.indexOf(listContactOnGroup[i])
////                        (context as GroupManagerActivity).recyclerMultiSelectItemClick(
////                            positionItem,
////                            secondClick,
////                            true
////                        )
////                        listOfItemSelected.add(contactManager.contactList[positionItem])
////                    }
////                }
////                secondClick = true
////                notifyDataSetChanged()
////            }
////        } else {
////            for (i in listContactOnGroup.indices) {
////                if (listOfItemSelected.contains(listContactOnGroup[i])) {
////                    val positionItem = contactManager.contactList.indexOf(listContactOnGroup[i])
////                    (context as GroupManagerActivity).recyclerMultiSelectItemClick(
////                        positionItem,
////                        secondClick,
////                        true
////                    )
////                    listOfItemSelected.remove(contactManager.contactList[positionItem])
////                }
////            }
////            if (listOfItemSelected.size == 0) {
////                secondClick = false
////                modeMultiSelect = false
////            }
////            notifyDataSetChanged()
////        }
////    }
//
////    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
////        var contactFirstNameView: TextView? = null
////        var contactLastNameView: TextView? = null
////        var contactRoundedImageView: CircularImageView? = null
////        var open: Boolean? = null
////
////        init {
////            if (len >= 4) {
////                contactRoundedImageView = view.findViewById(R.id.contactRoundedImageView)
////                contactFirstNameView = view.findViewById(R.id.grid_adapter_contactFirstName)
////                contactLastNameView = view.findViewById(R.id.grid_adapter_contactLastName)
////                heightWidthImage = contactRoundedImageView!!.height
////            } else {
////                contactRoundedImageView =
////                    view.findViewById(R.id.list_contact_item_contactRoundedImageView)
////                contactFirstNameView = view.findViewById(R.id.list_contact_item_contactFirstName)
////                contactLastNameView = view.findViewById(R.id.list_contact_item_contactLastName)
////                heightWidthImage = contactRoundedImageView!!.height
////                open = false
////            }
////        } /*        @Override
////        public void onClick(View view) {
////            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
////        }*/
////    }
//
////    fun getItem(position: Int): ContactDB {
////        return contactManager.contactList[position]
////    }
//
////    fun removeItem(position: Int) {
////        contactManager.contactList.removeAt(position)
////        notifyItemRemoved(position)
////    }
//
//    override fun getItemCount(): Int {
//        return 1
////        return contactManager.contactList.size
//    }
//
//    override fun getItemId(position: Int): Long {
//        return position.toLong()
//    }
//
//    override fun getItemViewType(position: Int): Int {
//        return position
//    }
//
//    /**
//     * setter sectionPos
//     *
//     * @param position ArrayList<Integer>
//    </Integer> */
//    fun setSectionPos(position: ArrayList<Int>) {
//        sectionPos = position
//    }
//
//    /**
//     * Retourne la position de la section dont fait partie le contact
//     *
//     * @param position [Int]
//     * @return [Int]
//     */
//    private fun getSectionnedPosition(position: Int): Int {
//        for (i in sectionPos.size - 1 downTo 1) {
//            if (sectionPos[i] <= position) {
//                return sectionPos[i]
//            }
//        }
//        return 0
//    }
//
////    private fun putGroupContactInItemSelected(position: Int): List<ContactWithAllInformation> {
////        val contactsDatabase = getDatabase(context)
////        val main_mDbWorkerThread = DbWorkerThread("dbWorkerThread")
////        main_mDbWorkerThread.start()
////        val group = contactsDatabase!!.GroupsDao().getAllGroupsByNameAZ()[position]
////        return group.getListContact(context)
////    }
//
//    /**
//     * Constructeur de GroupAdapter
//     *
//     * @param context        [Context]
//     * @param contactManager [ContactManager]
//     * @param len            [Integer]
//     */
//    init {
//        sectionPos = ArrayList()
//    }
//}