package com.yellowtwigs.knockin.controller.activity.group

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Build
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.model.ContactsRoomDatabase
import com.yellowtwigs.knockin.model.ContactsRoomDatabase.Companion.getDatabase
import com.yellowtwigs.knockin.model.DbWorkerThread
import java.util.*

class SectionGroupAdapter(private val mContext: Context, private val mSectionResourceId: Int, recyclerView: RecyclerView,
                          private val mBaseAdapter: RecyclerView.Adapter<*>, len: Int) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), PopupMenu.OnMenuItemClickListener {
    private var mValid = true
    private val mSections = SparseArray<Section?>()
    private var color = 0

    class SectionViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
        var titleTv: TextView
        var gmailIV: ImageView
        var smsIV: ImageView
        var menu: ImageView
        var holderName: RelativeLayout

        init {
            titleTv = view.findViewById(R.id.section_text)
            gmailIV = view.findViewById(R.id.section_gmail_imageview)
            smsIV = view.findViewById(R.id.section_sms_imageview)
            menu = view.findViewById(R.id.section_more_imageView)
            holderName = view.findViewById(R.id.recycler_group_name_relative)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, typeView: Int): RecyclerView.ViewHolder {
        return if (typeView == SECTION_TYPE) {
            val view = LayoutInflater.from(mContext).inflate(mSectionResourceId, parent, false)
            view.setBackgroundResource(R.drawable.recycler_section)
            SectionViewHolder(view)
        } else {
            mBaseAdapter.onCreateViewHolder(parent, typeView - 1)
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onBindViewHolder(sectionViewHolder: RecyclerView.ViewHolder, position: Int) {
        if (isSectionHeaderPosition(position)) {
            var i = position + 1
            println("position section$position")
            val groupName = mSections[position]!!.title.toString()
            if (groupName == "Favorites") {
                (sectionViewHolder as SectionViewHolder).titleTv.setText(R.string.group_favorites)
            } else {
                (sectionViewHolder as SectionViewHolder).titleTv.text = mSections[position]!!.title
            }
            while (!isSectionHeaderPosition(i) && i < itemCount) {
                val contact = (mBaseAdapter as GroupAdapter).getItem(sectionedPositionToPosition(i))
                println("contact " + contact.contactDB + " de la section " + position)
                i++
                if (contact.getFirstMail().isEmpty()) {
                    println("contact " + contact.contactDB + "n'as pas de mail " + contact.getFirstMail())
                    sectionViewHolder.gmailIV.visibility = View.GONE
                }
                if (contact.getFirstPhoneNumber().isEmpty()) {
                    println("contact " + contact.contactDB + "n'as pas de num " + contact.getFirstPhoneNumber())
                    sectionViewHolder.smsIV.visibility = View.GONE
                }
            }
            val roundedLayout = mContext.getDrawable(R.drawable.rounded_button_color_grey)
            val contactsDatabase = getDatabase(mContext)
            assert(roundedLayout != null)
            assert(contactsDatabase != null)

//            roundedLayout.setColorFilter(contactsDatabase.GroupsDao().getGroup(mSections.get(position).getIdGroup().intValue()).randomColorGroup(mContext), PorterDuff.Mode.MULTIPLY);
            roundedLayout!!.setColorFilter(contactsDatabase!!.GroupsDao().getGroup(mSections[position]!!.idGroup.toInt()).section_color, PorterDuff.Mode.MULTIPLY)
            sectionViewHolder.holderName.background = roundedLayout
            sectionViewHolder.gmailIV.setOnClickListener { v: View? ->
                var i1 = position + 1
                val groupMail = ArrayList<String>()
                while (!isSectionHeaderPosition(i1) && i1 < itemCount) {
                    val contact = (mBaseAdapter as GroupAdapter).getItem(sectionedPositionToPosition(i1))
                    groupMail.add(contact.getFirstMail())
                    i1++
                }
                monoChannelMailClick(groupMail)
            }
            sectionViewHolder.smsIV.setOnClickListener { v: View? ->
                var i12 = position + 1
                val groupSms = ArrayList<String>()
                while (!isSectionHeaderPosition(i12) && i12 < itemCount) {
                    val contact = (mBaseAdapter as GroupAdapter).getItem(sectionedPositionToPosition(i12))
                    groupSms.add(contact.getFirstPhoneNumber())
                    i12++
                }
                monoChannelSmsClick(groupSms)
            }
            sectionViewHolder.menu.setOnClickListener { v: View? ->
                println("BUTTON CLICK")
                val popupMenu = PopupMenu(mContext, v)
                popupMenu.inflate(R.menu.section_menu_group_manager)
                popupMenu.setOnMenuItemClickListener { item: MenuItem ->
                    println("VALUES = " + item.itemId)
                    println("ok = " + R.id.menu_group_add_contacts)
                    println("ok = " + R.id.menu_group_delete_contacts)
                    println("ok = " + R.id.menu_group_delete_group)
                    when (item.itemId) {
                        R.id.menu_group_edit_group -> {
                            val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                            @SuppressLint("InflateParams") val alertView = inflater.inflate(R.layout.alert_dialog_edit_group, null, true)
                            val edit_group_name_EditText: AppCompatEditText = alertView.findViewById(R.id.manager_group_edit_group_view_edit)
                            val edit_group_name_AlertDialogTitle = alertView.findViewById<TextView>(R.id.manager_group_edit_group_alert_dialog_title)
                            val edit_group_name_RedTag: AppCompatImageView = alertView.findViewById(R.id.manager_group_edit_group_color_red)
                            val edit_group_name_BlueTag: AppCompatImageView = alertView.findViewById(R.id.manager_group_edit_group_color_blue)
                            val edit_group_name_GreenTag: AppCompatImageView = alertView.findViewById(R.id.manager_group_edit_group_color_green)
                            val edit_group_name_YellowTag: AppCompatImageView = alertView.findViewById(R.id.manager_group_edit_group_color_yellow)
                            val edit_group_name_OrangeTag: AppCompatImageView = alertView.findViewById(R.id.manager_group_edit_group_color_orange)
                            val edit_group_name_PurpleTag: AppCompatImageView = alertView.findViewById(R.id.manager_group_edit_group_color_purple)
                            if (groupName == "Favorites") {
                                edit_group_name_AlertDialogTitle.text = (mContext.getString(R.string.manager_group_edit_group_alert_dialog_title) + " "
                                        + mContext.getString(R.string.group_favorites))
                                edit_group_name_EditText.setText(mContext.getString(R.string.group_favorites))
                            } else {
                                edit_group_name_AlertDialogTitle.text = (mContext.getString(R.string.manager_group_edit_group_alert_dialog_title) + " "
                                        + contactsDatabase.GroupsDao().getGroup(mSections[position]!!.idGroup.toInt()).name)
                                edit_group_name_EditText.setText(contactsDatabase.GroupsDao().getGroup(mSections[position]!!.idGroup.toInt()).name)
                            }
                            if (contactsDatabase.GroupsDao().getGroup(mSections[position]!!.idGroup.toInt()).section_color == R.color.red_tag_group) {
                                edit_group_name_RedTag.setImageResource(R.drawable.border_selected_yellow)
                            } else if (contactsDatabase.GroupsDao().getGroup(mSections[position]!!.idGroup.toInt()).section_color == R.color.blue_tag_group) {
                                edit_group_name_BlueTag.setImageResource(R.drawable.border_selected_yellow)
                            } else if (contactsDatabase.GroupsDao().getGroup(mSections[position]!!.idGroup.toInt()).section_color == R.color.green_tag_group) {
                                edit_group_name_GreenTag.setImageResource(R.drawable.border_selected_yellow)
                            } else if (contactsDatabase.GroupsDao().getGroup(mSections[position]!!.idGroup.toInt()).section_color == R.color.yellow_tag_group) {
                                edit_group_name_YellowTag.setImageResource(R.drawable.border_selected_yellow)
                            } else if (contactsDatabase.GroupsDao().getGroup(mSections[position]!!.idGroup.toInt()).section_color == R.color.orange_tag_group) {
                                edit_group_name_OrangeTag.setImageResource(R.drawable.border_selected_yellow)
                            } else if (contactsDatabase.GroupsDao().getGroup(mSections[position]!!.idGroup.toInt()).section_color == R.color.purple_tag_group) {
                                edit_group_name_PurpleTag.setImageResource(R.drawable.border_selected_yellow)
                            }
                            edit_group_name_RedTag.setOnClickListener { v1: View? ->
                                edit_group_name_RedTag.setImageResource(R.drawable.border_selected_yellow)
                                edit_group_name_BlueTag.setImageResource(android.R.color.transparent)
                                edit_group_name_GreenTag.setImageResource(android.R.color.transparent)
                                edit_group_name_YellowTag.setImageResource(android.R.color.transparent)
                                edit_group_name_OrangeTag.setImageResource(android.R.color.transparent)
                                edit_group_name_PurpleTag.setImageResource(android.R.color.transparent)
                                color = mContext.getColor(R.color.red_tag_group)
                            }
                            edit_group_name_BlueTag.setOnClickListener { v1: View? ->
                                edit_group_name_BlueTag.setImageResource(R.drawable.border_selected_yellow)
                                edit_group_name_RedTag.setImageResource(android.R.color.transparent)
                                edit_group_name_GreenTag.setImageResource(android.R.color.transparent)
                                edit_group_name_YellowTag.setImageResource(android.R.color.transparent)
                                edit_group_name_OrangeTag.setImageResource(android.R.color.transparent)
                                edit_group_name_PurpleTag.setImageResource(android.R.color.transparent)
                                color = mContext.getColor(R.color.blue_tag_group)
                            }
                            edit_group_name_GreenTag.setOnClickListener { v1: View? ->
                                edit_group_name_GreenTag.setImageResource(R.drawable.border_selected_yellow)
                                edit_group_name_RedTag.setImageResource(android.R.color.transparent)
                                edit_group_name_BlueTag.setImageResource(android.R.color.transparent)
                                edit_group_name_YellowTag.setImageResource(android.R.color.transparent)
                                edit_group_name_OrangeTag.setImageResource(android.R.color.transparent)
                                edit_group_name_PurpleTag.setImageResource(android.R.color.transparent)
                                color = mContext.getColor(R.color.green_tag_group)
                            }
                            edit_group_name_YellowTag.setOnClickListener { v1: View? ->
                                edit_group_name_YellowTag.setImageResource(R.drawable.border_selected_yellow)
                                edit_group_name_RedTag.setImageResource(android.R.color.transparent)
                                edit_group_name_GreenTag.setImageResource(android.R.color.transparent)
                                edit_group_name_BlueTag.setImageResource(android.R.color.transparent)
                                edit_group_name_OrangeTag.setImageResource(android.R.color.transparent)
                                edit_group_name_PurpleTag.setImageResource(android.R.color.transparent)
                                color = mContext.getColor(R.color.yellow_tag_group)
                            }
                            edit_group_name_OrangeTag.setOnClickListener { v1: View? ->
                                edit_group_name_OrangeTag.setImageResource(R.drawable.border_selected_yellow)
                                edit_group_name_RedTag.setImageResource(android.R.color.transparent)
                                edit_group_name_GreenTag.setImageResource(android.R.color.transparent)
                                edit_group_name_YellowTag.setImageResource(android.R.color.transparent)
                                edit_group_name_BlueTag.setImageResource(android.R.color.transparent)
                                edit_group_name_PurpleTag.setImageResource(android.R.color.transparent)
                                color = mContext.getColor(R.color.orange_tag_group)
                            }
                            edit_group_name_PurpleTag.setOnClickListener { v1: View? ->
                                edit_group_name_PurpleTag.setImageResource(R.drawable.border_selected_yellow)
                                edit_group_name_RedTag.setImageResource(android.R.color.transparent)
                                edit_group_name_GreenTag.setImageResource(android.R.color.transparent)
                                edit_group_name_YellowTag.setImageResource(android.R.color.transparent)
                                edit_group_name_OrangeTag.setImageResource(android.R.color.transparent)
                                edit_group_name_BlueTag.setImageResource(android.R.color.transparent)
                                color = mContext.getColor(R.color.purple_tag_group)
                            }
                            MaterialAlertDialogBuilder(mContext, R.style.AlertDialog)
                                    .setView(alertView)
                                    .setPositiveButton(R.string.alert_dialog_validate) { dialog: DialogInterface?, which: Int ->
                                        println("Name : " + Objects.requireNonNull(edit_group_name_EditText.text).toString())
                                        println("Name : " + contactsDatabase.GroupsDao().getGroup(mSections[position]!!.idGroup.toInt()).name)
                                        println("Color : $color")
                                        if (contactsDatabase.GroupsDao().getGroup(mSections[position]!!.idGroup.toInt()).name == "Favorites") {
                                            Toast.makeText(mContext, "Vous ne pouvez pas modifier le nom du groupe Favorites", Toast.LENGTH_LONG).show()
                                        } else {
                                            if (edit_group_name_EditText.text.toString() == "") {
                                                edit_group_name_EditText.setText(contactsDatabase.GroupsDao().getGroup(mSections[position]!!.idGroup.toInt()).name)
                                            }
                                            contactsDatabase.GroupsDao().updateGroupNameById(mSections[position]!!.idGroup.toInt(), edit_group_name_EditText.text.toString())
                                            //Toast.makeText(mContext, "Vous avez modifié le nom de votre groupe", Toast.LENGTH_LONG).show();
                                        }
                                        if (color == 0) {
                                            val r = Random()
                                            val n = r.nextInt(7)
                                            when (n) {
                                                0 -> {
                                                    color = mContext.getColor(R.color.red_tag_group)
                                                    color = mContext.getColor(R.color.blue_tag_group)
                                                    color = mContext.getColor(R.color.green_tag_group)
                                                    color = mContext.getColor(R.color.orange_tag_group)
                                                    color = mContext.getColor(R.color.yellow_tag_group)
                                                    color = mContext.getColor(R.color.purple_tag_group)
                                                    color = mContext.getColor(R.color.red_tag_group)
                                                    color = mContext.getColor(R.color.blue_tag_group)
                                                }
                                                1 -> {
                                                    color = mContext.getColor(R.color.blue_tag_group)
                                                    color = mContext.getColor(R.color.green_tag_group)
                                                    color = mContext.getColor(R.color.orange_tag_group)
                                                    color = mContext.getColor(R.color.yellow_tag_group)
                                                    color = mContext.getColor(R.color.purple_tag_group)
                                                    color = mContext.getColor(R.color.red_tag_group)
                                                    color = mContext.getColor(R.color.blue_tag_group)
                                                }
                                                2 -> {
                                                    color = mContext.getColor(R.color.green_tag_group)
                                                    color = mContext.getColor(R.color.orange_tag_group)
                                                    color = mContext.getColor(R.color.yellow_tag_group)
                                                    color = mContext.getColor(R.color.purple_tag_group)
                                                    color = mContext.getColor(R.color.red_tag_group)
                                                    color = mContext.getColor(R.color.blue_tag_group)
                                                }
                                                3 -> {
                                                    color = mContext.getColor(R.color.orange_tag_group)
                                                    color = mContext.getColor(R.color.yellow_tag_group)
                                                    color = mContext.getColor(R.color.purple_tag_group)
                                                    color = mContext.getColor(R.color.red_tag_group)
                                                    color = mContext.getColor(R.color.blue_tag_group)
                                                }
                                                4 -> {
                                                    color = mContext.getColor(R.color.yellow_tag_group)
                                                    color = mContext.getColor(R.color.purple_tag_group)
                                                    color = mContext.getColor(R.color.red_tag_group)
                                                    color = mContext.getColor(R.color.blue_tag_group)
                                                }
                                                5 -> {
                                                    color = mContext.getColor(R.color.purple_tag_group)
                                                    color = mContext.getColor(R.color.red_tag_group)
                                                    color = mContext.getColor(R.color.blue_tag_group)
                                                }
                                                6 -> {
                                                    color = mContext.getColor(R.color.red_tag_group)
                                                    color = mContext.getColor(R.color.blue_tag_group)
                                                }
                                                else -> color = mContext.getColor(R.color.blue_tag_group)
                                            }
                                        }
                                        contactsDatabase.GroupsDao().updateGroupSectionColorById(mSections[position]!!.idGroup.toInt(), color)
                                        mContext.startActivity(Intent(mContext, GroupManagerActivity::class.java))
                                    }
                                    .setNegativeButton(R.string.alert_dialog_cancel) { dialog: DialogInterface?, which: Int -> }
                                    .show()
                        }
                        R.id.menu_group_add_contacts -> {
                            println("add contact")
                            val intentAddContacts = Intent(mContext, AddContactToGroupActivity::class.java)
                            intentAddContacts.putExtra("GroupId", mSections[position]!!.idGroup.toInt())
                            mContext.startActivity(intentAddContacts)
                        }
                        R.id.menu_group_delete_contacts -> {
                            val intentDelete = Intent(mContext, DeleteContactFromGroupActivity::class.java)
                            intentDelete.putExtra("GroupId", mSections[position]!!.idGroup.toInt())
                            mContext.startActivity(intentDelete)
                            println("delete contact")
                        }
                        R.id.menu_group_delete_group -> {
                            val contactsDatabase1: ContactsRoomDatabase?
                            val mDbWorkerThread: DbWorkerThread
                            mDbWorkerThread = DbWorkerThread("dbWorkerThread")
                            mDbWorkerThread.start()
                            contactsDatabase1 = getDatabase(mContext)
                            assert(contactsDatabase1 != null)
                            alertDialog(mSections[position]!!.idGroup.toInt(), contactsDatabase1)
                        }
                        else -> println("always in default")
                    }
                    true
                }
                popupMenu.show()
            }
            sectionViewHolder.holderName.setOnClickListener { v: View? -> (mBaseAdapter as GroupAdapter).SetGroupClick(getGroupPosition(position)) }
        } else {
            // System.out.println("position non section"+position);
            mBaseAdapter.onBindViewHolder(sectionViewHolder, sectionedPositionToPosition(position))
            //System.out.println("contact "+((GroupAdapter)mBaseAdapter).getItem(sectionedPositionToPosition(position)).getContactDB()+ " position "+position);
        }
        val list = ArrayList<Int>()
        for (i in 0 until itemCount) {
            if (isSectionHeaderPosition(i)) {
                list.add(sectionedPositionToPosition(i + 1))
            }
        }
        (mBaseAdapter as GroupAdapter).setSectionPos(list)
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        return false
    }

    override fun getItemViewType(position: Int): Int {
        return if (isSectionHeaderPosition(position)) SECTION_TYPE else mBaseAdapter.getItemViewType(sectionedPositionToPosition(position)) + 1
    }

    class Section(var firstPosition: Int, var title: CharSequence, var idGroup: Long) {
        var sectionedPosition = 0

    }

    fun setSections(sections: Array<Section>) {
        mSections.clear()
        Arrays.sort(sections) { o: Section, o1: Section -> Integer.compare(o.firstPosition, o1.firstPosition) }
        var offset = 0 // offset positions for the headers we're adding
        for (section in sections) {
            section.sectionedPosition = section.firstPosition + offset
            mSections.append(section.sectionedPosition, section)
            ++offset
        }
        notifyDataSetChanged()
    }

    fun positionToSectionedPosition(position: Int): Int {
        var offset = 0
        for (i in 0 until mSections.size()) {
            if (mSections.valueAt(i)!!.firstPosition > position) {
                break
            }
            ++offset
        }
        return position + offset
    }

    fun getPositionSection(position: Int): Int {
        for (i in position downTo 1) {
            if (isSectionHeaderPosition(i)) {
                return i
            }
        }
        return 0
    }

    fun getGroupPosition(position: Int): Int {
        var nbGroup = 0
        for (i in position downTo 1) {
            if (isSectionHeaderPosition(i)) {
                nbGroup++
            }
        }
        return nbGroup
    }

    private fun sectionedPositionToPosition(sectionedPosition: Int): Int {
        if (isSectionHeaderPosition(sectionedPosition)) {
            return RecyclerView.NO_POSITION
        }
        var offset = 0
        for (i in 0 until mSections.size()) {
            if (mSections.valueAt(i)!!.sectionedPosition > sectionedPosition) {
                break
            }
            --offset
        }
        return sectionedPosition + offset
    }

    private fun isSectionHeaderPosition(position: Int): Boolean {
        return mSections[position] != null
    }

    override fun getItemId(position: Int): Long {
        return if (isSectionHeaderPosition(position)) (Int.MAX_VALUE - mSections.indexOfKey(position)).toLong() else mBaseAdapter.getItemId(sectionedPositionToPosition(position))
    }

    override fun getItemCount(): Int {
        return if (mValid) mBaseAdapter.itemCount + mSections.size() else 0
    }

    private fun monoChannelSmsClick(listOfPhoneNumber: ArrayList<String>) {
        var message = "smsto:" + listOfPhoneNumber[0]
        for (i in listOfPhoneNumber.indices) {
            message += ";" + listOfPhoneNumber[i]
        }
        mContext.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse(message)))
    }

    @SuppressLint("IntentReset")
    private fun monoChannelMailClick(listOfMail: ArrayList<String>) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_EMAIL, listOfMail.toTypedArray())
        intent.data = Uri.parse("mailto:")
        intent.type = "message/rfc822"
        intent.putExtra(Intent.EXTRA_SUBJECT, "")
        intent.putExtra(Intent.EXTRA_TEXT, "")
        mContext.startActivity(intent)
    }

    private fun alertDialog(idGroup: Int, contactsDatabase: ContactsRoomDatabase?) {
        MaterialAlertDialogBuilder(mContext, R.style.AlertDialog)
                .setTitle(R.string.section_alert_delete_group_title)
                .setMessage(String.format(contactsDatabase!!.GroupsDao().getGroup(idGroup).name, R.string.section_alert_delete_group_message))
                .setPositiveButton(android.R.string.yes
                ) { dialog: DialogInterface?, id: Int ->
                    var counter = 0
                    while (counter < contactsDatabase.GroupsDao().getAllGroupsByNameAZ().size) {
                        if (Objects.requireNonNull(contactsDatabase.GroupsDao().getAllGroupsByNameAZ()[counter].groupDB).name == "Favorites") {
                            var secondCounter = 0
                            while (secondCounter < contactsDatabase.GroupsDao().getAllGroupsByNameAZ()[counter].getListContact(mContext).size) {
                                contactsDatabase.GroupsDao().getAllGroupsByNameAZ()[counter].getListContact(mContext)[secondCounter].setIsNotFavorite(contactsDatabase)
                                secondCounter++
                            }
                            break
                        }
                        counter++
                    }
                    contactsDatabase.GroupsDao().deleteGroupById(idGroup)
                    if (mContext is GroupManagerActivity) mContext.refreshList()
                }
                .setNegativeButton(android.R.string.no, null)
                .show()
    }

    companion object {
        private const val SECTION_TYPE = 0
    }

    init {

//        color = mContext.getColor(R.color.)
        mBaseAdapter.registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onChanged() {
                mValid = mBaseAdapter.itemCount > 0
                notifyDataSetChanged()
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                mValid = mBaseAdapter.itemCount > 0
                notifyItemRangeChanged(positionStart, itemCount)
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                mValid = mBaseAdapter.itemCount > 0
                notifyItemRangeInserted(positionStart, itemCount)
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                mValid = mBaseAdapter.itemCount > 0
                notifyItemRangeRemoved(positionStart, itemCount)
            }
        })
        if (len >= 4) {
            val gridLayoutManager = (recyclerView.layoutManager as GridLayoutManager?)!!
            gridLayoutManager.spanSizeLookup = object : SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (isSectionHeaderPosition(position)) gridLayoutManager.spanCount else 1
                }
            }
        } else {
//            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) (recyclerView.getLayoutManager());
//            assert linearLayoutManager != null;
//            linearLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
//                @Override
//                public int getSpanSize(int position) {
//                    return (isSectionHeaderPosition(position)) ? linearLayoutManager.getSpanCount() : 1;
//                }
//            });
        }
    }
}