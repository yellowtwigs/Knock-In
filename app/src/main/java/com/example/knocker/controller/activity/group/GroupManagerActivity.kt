package com.example.knocker.controller.activity.group

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.graphics.Color
import android.net.Uri
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.knocker.R
import com.example.knocker.controller.activity.*
import com.example.knocker.model.ContactList
import com.example.knocker.model.ContactsRoomDatabase
import com.example.knocker.model.DbWorkerThread
import com.example.knocker.model.ModelDB.ContactWithAllInformation
import com.example.knocker.model.ModelDB.GroupWithContact
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView

class GroupManagerActivity : AppCompatActivity() {

    //region ========================================= Val or Var ===========================================

    private var group_manager_DrawerLayout: DrawerLayout? = null
    private var group_manager_ContactsDatabase: ContactsRoomDatabase? = null
    private lateinit var group_mDbWorkerThread: DbWorkerThread

    private var group_manager_NavigationView: NavigationView? = null

    private var group_manager_FloatingButtonSMS: FloatingActionButton? = null
    private var group_manager_FloatingButtonSend: FloatingActionButton? = null
    private var group_manager_FloatingButtonMail: FloatingActionButton? = null

    private var group_manager_RecyclerView: RecyclerView? = null

    private var gestionnaireContacts: ContactList? = null
    private var listOfItemSelected: ArrayList<ContactWithAllInformation> = ArrayList()

    private var firstClick: Boolean = true

    private var floating_add_new_group:FloatingActionButton?= null
    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {

        //region ======================================== Theme Dark ========================================

        val sharedThemePreferences = getSharedPreferences("Knocker_Theme", Context.MODE_PRIVATE)
        if (sharedThemePreferences.getBoolean("darkTheme", false)) {
            setTheme(R.style.AppThemeDark)
        } else {
            setTheme(R.style.AppTheme)
        }

        //endregion

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_manager)

        //region ========================================= Toolbar ==========================================

        val toolbar = findViewById<Toolbar>(R.id.group_toolbar)
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar
        actionbar!!.run {
            actionbar.title = ""
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_open_drawer)
            setBackgroundDrawable(ColorDrawable(Color.parseColor("#ffffff")))
        }

        //endregion

        //region ====================================== FindViewById ========================================

        group_manager_DrawerLayout = findViewById(R.id.group_drawer_layout)
        group_manager_RecyclerView = findViewById(R.id.group_list_view_id)
        group_manager_NavigationView = findViewById(R.id.nav_view)

        group_manager_FloatingButtonSMS = findViewById(R.id.group_manager_floating_button_sms)
        group_manager_FloatingButtonSend = findViewById(R.id.group_manager_floating_button_send_id)
        group_manager_FloatingButtonMail = findViewById(R.id.group_manager_floating_button_gmail)
        floating_add_new_group = findViewById(R.id.group_manager_floating_button_add)
        //endregion

        group_manager_RecyclerView!!.setHasFixedSize(true)

        //region ======================================= Navigation =========================================

        val menu = group_manager_NavigationView!!.menu
        val nav_item = menu.findItem(R.id.nav_home)
        nav_item.isChecked = true
        val nav_sync_contact = menu.findItem(R.id.nav_sync_contact)
        nav_sync_contact.isVisible = true

        group_manager_NavigationView!!.menu.getItem(1).isChecked = true

        group_manager_NavigationView!!.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            group_manager_DrawerLayout!!.closeDrawers()

            when (menuItem.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this@GroupManagerActivity, MainActivity::class.java))
                }
                R.id.nav_informations -> startActivity(Intent(this@GroupManagerActivity, EditInformationsActivity::class.java))
                R.id.nav_notif_config -> startActivity(Intent(this@GroupManagerActivity, ManageNotificationActivity::class.java))
                R.id.nav_screen_config -> startActivity(Intent(this@GroupManagerActivity, ManageMyScreenActivity::class.java))
                R.id.nav_data_access -> {
                }
                R.id.nav_knockons -> startActivity(Intent(this@GroupManagerActivity, ManageKnockonsActivity::class.java))
                R.id.nav_statistics -> {
                }
                R.id.nav_help -> startActivity(Intent(this@GroupManagerActivity, HelpActivity::class.java))
            }

            val drawer = findViewById<DrawerLayout>(R.id.group_drawer_layout)
            drawer.closeDrawer(GravityCompat.START)
            true
        }

        //endregion

        //region ===================================== WorkerThread =========================================

        group_manager_ContactsDatabase = ContactsRoomDatabase.getDatabase(this)

        group_mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        group_mDbWorkerThread.start()
        val group: ArrayList<GroupWithContact> = ArrayList()
        group.addAll(group_manager_ContactsDatabase!!.GroupsDao().getAllGroupsByNameAZ())
        println("group size" + group.size)
        for (aGroup in group)
            println("group content" + aGroup.ContactIdList)
        val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
        val len = sharedPreferences.getInt("gridview", 4)
        val listContactGroup: ArrayList<ContactWithAllInformation> = arrayListOf()
        val sections = ArrayList<SectionGroupAdapter.Section>()
        var position = 0
        for (i in group) {
            val list = i.getListContact(this)
            listContactGroup.addAll(list)
            sections.add(SectionGroupAdapter.Section(position, i.groupDB!!.name, i.groupDB!!.id))
            position += list.size
        }

        //endregion

        //region ======================================== Adapter ===========================================

        val adapter: GroupAdapter
        if (len >= 3) {
            adapter = GroupAdapter(this, listContactGroup, len)
            group_manager_RecyclerView!!.layoutManager = GridLayoutManager(this, len)
        } else {
            adapter = GroupAdapter(this, listContactGroup, 4)
            group_manager_RecyclerView!!.layoutManager = GridLayoutManager(this, 4)
        }
        val sectionList = arrayOfNulls<SectionGroupAdapter.Section>(sections.size)
        val sectionAdapter = SectionGroupAdapter(this, R.layout.recycler_adapter_section, group_manager_RecyclerView, adapter)
        sectionAdapter.setSections(sections.toArray(sectionList))
        println("taille list group " + listContactGroup.size)
        // val adapter= GroupListViewAdapter(group,this,len)
        group_manager_RecyclerView!!.adapter = sectionAdapter

        //endregion

        //region ======================================= Listeners ==========================================

        group_manager_FloatingButtonSend!!.setOnClickListener {
            val intent = Intent(this@GroupManagerActivity, MultiChannelActivity::class.java)
            val iterator: IntIterator?
            val listOfIdContactSelected: ArrayList<Int> = ArrayList()

            iterator = (0 until listOfItemSelected.size).iterator()

            for (i in iterator) {
                listOfIdContactSelected.add(listOfItemSelected[i].getContactId())
            }
            intent.putIntegerArrayListExtra("ListContactsSelected", listOfIdContactSelected)

            startActivity(intent)
            finish()
        }

        group_manager_FloatingButtonSMS!!.setOnClickListener {
            val iterator: IntIterator?
            val listOfPhoneNumberContactSelected: ArrayList<String> = ArrayList()

            iterator = (0 until listOfItemSelected.size).iterator()

            for (i in iterator) {
                listOfPhoneNumberContactSelected.add(listOfItemSelected[i].getFirstPhoneNumber())
            }
            monoChannelSmsClick(listOfPhoneNumberContactSelected)
        }

        group_manager_FloatingButtonMail!!.setOnClickListener {
            val iterator: IntIterator?
            val listOfMailContactSelected: ArrayList<String> = ArrayList()

            iterator = (0 until listOfItemSelected.size).iterator()

            for (i in iterator) {
                listOfMailContactSelected.add(listOfItemSelected[i].getFirstMail())
            }
            monoChannelMailClick(listOfMailContactSelected)
        }
        floating_add_new_group!!.setOnClickListener {
            val intent=Intent(this@GroupManagerActivity, AddNewGroupActivity::class.java)
            startActivity(intent)
        }

        //endregion
    }

    //region ========================================= Functions ============================================

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_help, menu)
        return super.onCreateOptionsMenu(menu)
    }

    @SuppressLint("ShowToast")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                group_manager_DrawerLayout!!.openDrawer(GravityCompat.START)
                return true
            }
            R.id.item_help -> {
                MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.help)
                        .setMessage(this.resources.getString(R.string.help_phone_log))
                        .show()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun gridLongItemClick(position: Int) {
        if (listOfItemSelected.contains(gestionnaireContacts!!.contacts[position])) {
            listOfItemSelected.remove(gestionnaireContacts!!.contacts[position])
        } else {
            listOfItemSelected.add(gestionnaireContacts!!.contacts[position])
            verifiedContactsChannel(listOfItemSelected)
        }

        if (listOfItemSelected.size == 1 && firstClick) {
            Toast.makeText(this, R.string.main_toast_multi_select_actived, Toast.LENGTH_SHORT).show()
            firstClick = false
        } else if (listOfItemSelected.size == 0) {
            Toast.makeText(this, R.string.main_toast_multi_select_deactived, Toast.LENGTH_SHORT).show()
            firstClick = true
        }
    }

    fun gridItemClick(position: Int) {
        if (listOfItemSelected.contains(gestionnaireContacts!!.contacts[position])) {
            listOfItemSelected.remove(gestionnaireContacts!!.contacts[position])
            verifiedContactsChannel(listOfItemSelected)
        } else {
            listOfItemSelected.add(gestionnaireContacts!!.contacts[position])
            verifiedContactsChannel(listOfItemSelected)
        }

        if (listOfItemSelected.size == 1 && firstClick) {
            Toast.makeText(this, R.string.main_toast_multi_select_actived, Toast.LENGTH_SHORT).show()
            firstClick = false
        } else if (listOfItemSelected.size == 0) {

            group_manager_FloatingButtonSend!!.visibility = View.GONE
            group_manager_FloatingButtonMail!!.visibility = View.GONE
            group_manager_FloatingButtonSMS!!.visibility = View.GONE

            Toast.makeText(this, R.string.main_toast_multi_select_deactived, Toast.LENGTH_SHORT).show()
            firstClick = true
        }
    }

    private fun verifiedContactsChannel(listOfItemSelected: ArrayList<ContactWithAllInformation>) {
        val iterator = (0 until listOfItemSelected.size).iterator()
        var allContactsHaveMail = true
        var allContactsHavePhoneNumber = true

        for (i in iterator) {
            if (listOfItemSelected[i].getFirstMail() == "") {
                allContactsHaveMail = false
            }

            if (listOfItemSelected[i].getFirstPhoneNumber() == "") {
                allContactsHavePhoneNumber = false
            }
        }
        var i = 2
        val metrics = DisplayMetrics()
        this.windowManager.defaultDisplay.getMetrics(metrics)
        val margin = (0.5 * metrics.densityDpi).toInt()
        println("metric smartphone" + metrics.densityDpi)
        if (allContactsHavePhoneNumber) {
            group_manager_FloatingButtonSMS!!.visibility = View.VISIBLE
            i++
        } else {
            println("false phoneNumber")
            group_manager_FloatingButtonSMS!!.visibility = View.GONE
        }
        if (allContactsHaveMail) {
            group_manager_FloatingButtonMail!!.visibility = View.VISIBLE
            val params: ViewGroup.MarginLayoutParams = group_manager_FloatingButtonMail!!.layoutParams as ViewGroup.MarginLayoutParams
            params.bottomMargin = margin * i
            group_manager_FloatingButtonMail!!.layoutParams = params
            println("height of floating mail" + group_manager_FloatingButtonMail!!.height)
        } else {
            println("false mail")
            group_manager_FloatingButtonMail!!.visibility = View.GONE
        }

    }

    fun refreshList() {
        group_manager_ContactsDatabase = ContactsRoomDatabase.getDatabase(this)
        val group: ArrayList<GroupWithContact> = ArrayList()
        group.addAll(group_manager_ContactsDatabase!!.GroupsDao().getAllGroupsByNameAZ())
        val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
        val len = sharedPreferences.getInt("gridview", 4)
        val listContactGroup: ArrayList<ContactWithAllInformation> = arrayListOf()
        val sections = ArrayList<SectionGroupAdapter.Section>()
        var position = 0
        for (i in group) {
            val list = i.getListContact(this)
            listContactGroup.addAll(list)
            sections.add(SectionGroupAdapter.Section(position, i.groupDB!!.name, i.groupDB!!.id))
            position += list.size
        }
        val adapter: GroupAdapter
        if (len >= 3) {
            adapter = GroupAdapter(this, listContactGroup, len)
            group_manager_RecyclerView!!.layoutManager = GridLayoutManager(this, len)
        } else {
            adapter = GroupAdapter(this, listContactGroup, 4)
            group_manager_RecyclerView!!.layoutManager = GridLayoutManager(this, 4)
        }
        val sectionList = arrayOfNulls<SectionGroupAdapter.Section>(sections.size)
        val sectionAdapter = SectionGroupAdapter(this, R.layout.recycler_adapter_section, group_manager_RecyclerView, adapter)
        sectionAdapter.setSections(sections.toArray(sectionList))
        group_manager_RecyclerView!!.adapter = sectionAdapter

    }

    private fun monoChannelSmsClick(listOfPhoneNumber: ArrayList<String>) {

        var message = "smsto:" + listOfPhoneNumber[0]
        for (i in 0 until listOfPhoneNumber.size) {
            message += ";" + listOfPhoneNumber[i]
        }
        startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse(message)))
    }

    private fun monoChannelMailClick(listOfMail: ArrayList<String>) {
        val contact = listOfMail.toArray(arrayOfNulls<String>(listOfMail.size))
        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_EMAIL, contact)/*listOfMail.toArray(new String[listOfMail.size()]*/
        intent.data = Uri.parse("mailto:")
        intent.type = "message/rfc822"
        intent.putExtra(Intent.EXTRA_SUBJECT, "")
        intent.putExtra(Intent.EXTRA_TEXT, "")
        startActivity(intent)
    }

    //endregion
}