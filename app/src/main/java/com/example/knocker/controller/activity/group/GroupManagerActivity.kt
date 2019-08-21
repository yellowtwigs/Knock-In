package com.example.knocker.controller.activity.group

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.net.Uri
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.knocker.R
import com.example.knocker.controller.activity.*
import com.example.knocker.controller.activity.firstLaunch.TutorialActivity
import com.example.knocker.model.ContactManager
import com.example.knocker.model.ContactsRoomDatabase
import com.example.knocker.model.DbWorkerThread
import com.example.knocker.model.ModelDB.ContactWithAllInformation
import com.example.knocker.model.ModelDB.GroupWithContact
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView

/**
 * Activité qui nous affiche les groupes de contact sous forme de section
 * @author Florian Striebel
 */
class GroupManagerActivity : AppCompatActivity(), DrawerLayout.DrawerListener {


    //region ========================================= Val or Var ===========================================

    private var group_manager_DrawerLayout: DrawerLayout? = null
    private var group_manager_ContactsDatabase: ContactsRoomDatabase? = null
    private lateinit var group_mDbWorkerThread: DbWorkerThread

    private var group_BottomNavigationView: BottomNavigationView? = null
    private var group_manager_NavigationView: NavigationView? = null

    private var group_manager_FloatingButtonSMS: FloatingActionButton? = null
    private var group_manager_FloatingButtonSend: FloatingActionButton? = null
    private var group_manager_FloatingButtonMail: FloatingActionButton? = null

    private var group_manager_FloatingButtonAddNewGroup: FloatingActionButton? = null

    private var group_manager_ToolbarHelp: AppCompatImageView? = null

    private var group_manager_RecyclerView: RecyclerView? = null

    private var gestionnaireContacts: ContactManager? = null
    private var listOfItemSelected: ArrayList<ContactWithAllInformation> = ArrayList()

    private var firstClick: Boolean = true

    private var group_GridView: RecyclerView? = null
    private var adapaterItem: GroupAdapter? = null
    private var sectionAdapter: SectionGroupAdapter? = null

    var touchHelper: ItemTouchHelper? = null

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_contacts -> {
                startActivity(Intent(this@GroupManagerActivity, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifcations -> {
                startActivity(Intent(this@GroupManagerActivity, NotificationHistoryActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_phone_keyboard -> {
                startActivity(Intent(this@GroupManagerActivity, PhoneLogActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

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

        val group_manager_OpenDrawer = findViewById<AppCompatImageView>(R.id.group_manager_open_drawer)

        //endregion

        //region ====================================== FindViewById ========================================

        group_manager_DrawerLayout = findViewById(R.id.group_manager_drawer_layout)
        group_manager_RecyclerView = findViewById(R.id.group_manager_recycler_view)
        group_manager_NavigationView = findViewById(R.id.group_manager_nav_view)

        group_manager_FloatingButtonSMS = findViewById(R.id.group_manager_floating_button_sms)
        group_manager_FloatingButtonSend = findViewById(R.id.group_manager_floating_button_send_id)
        group_manager_FloatingButtonMail = findViewById(R.id.group_manager_floating_button_gmail)
        group_manager_FloatingButtonAddNewGroup = findViewById(R.id.group_manager_floating_button_add)

        group_GridView = findViewById(R.id.group_manager_recycler_view)

        group_BottomNavigationView = findViewById(R.id.navigation)

        group_manager_ToolbarHelp = findViewById(R.id.group_manager_toolbar_help)

        //endregion

        //region ======================================= Navigation =========================================

        group_BottomNavigationView!!.menu.getItem(1).isChecked = true
        group_BottomNavigationView!!.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        val menu = group_manager_NavigationView!!.menu
        val navItem = menu.findItem(R.id.nav_home)
        navItem.isChecked = true
        val navSyncContact = menu.findItem(R.id.nav_sync_contact)
        navSyncContact.isVisible = true

        group_manager_NavigationView!!.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            group_manager_DrawerLayout!!.closeDrawers()

            when (menuItem.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this@GroupManagerActivity, MainActivity::class.java))
                }
                R.id.nav_informations -> startActivity(Intent(this@GroupManagerActivity, EditInformationsActivity::class.java))
                R.id.nav_notif_config -> startActivity(Intent(this@GroupManagerActivity, ManageNotificationActivity::class.java))
                R.id.nav_manage_screen -> startActivity(Intent(this@GroupManagerActivity, ManageMyScreenActivity::class.java))
                R.id.nav_settings -> startActivity(Intent(this@GroupManagerActivity, SettingsActivity::class.java))
                R.id.nav_data_access -> {
                }
                R.id.nav_knockons -> startActivity(Intent(this@GroupManagerActivity, ManageKnockonsActivity::class.java))
                R.id.nav_statistics -> {
                }
                R.id.nav_help -> startActivity(Intent(this@GroupManagerActivity, HelpActivity::class.java))
            }

            val drawer = findViewById<DrawerLayout>(R.id.group_manager_drawer_layout)
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
        val sharedPreferences = getSharedPreferences("group", Context.MODE_PRIVATE)
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

        gestionnaireContacts = ContactManager(this)
        gestionnaireContacts!!.contactList = listContactGroup
        if (len >= 3) {
            adapaterItem = GroupAdapter(this, gestionnaireContacts!!, len)
            group_manager_RecyclerView!!.layoutManager = GridLayoutManager(this, len)
        } else {
            adapaterItem = GroupAdapter(this, gestionnaireContacts!!, 4)
            group_manager_RecyclerView!!.layoutManager = GridLayoutManager(this, 4)
        }
        val sectionList = arrayOfNulls<SectionGroupAdapter.Section>(sections.size)
        sectionAdapter = SectionGroupAdapter(this, R.layout.group_manager_recycler_adapter_section, group_manager_RecyclerView, adapaterItem)
        sectionAdapter!!.setSections(sections.toArray(sectionList))
        println("taille list group " + listContactGroup.size)
        group_manager_RecyclerView!!.adapter = sectionAdapter

        //endregion

        //region ======================================= Listeners ==========================================

        group_manager_OpenDrawer.setOnClickListener {
            group_manager_DrawerLayout!!.openDrawer(GravityCompat.START)
        }

        group_manager_ToolbarHelp!!.setOnClickListener {
            val intentToTuto = Intent(this@GroupManagerActivity, TutorialActivity::class.java)
            intentToTuto.putExtra("fromGroupManagerActivity", true)
            startActivity(intentToTuto)
            finish()
        }

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
        group_manager_FloatingButtonAddNewGroup!!.setOnClickListener {
            val intent = Intent(this@GroupManagerActivity, AddNewGroupActivity::class.java)
            startActivity(intent)
        }

        group_manager_RecyclerView!!.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            adapaterItem!!.closeMenu()
        }

        touchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
            override fun onMove(
                    p0: RecyclerView,
                    p1: RecyclerView.ViewHolder,
                    p2: RecyclerView.ViewHolder
            ): Boolean {
                val sourcePosition = p1.adapterPosition
                val targetPosition = p2.adapterPosition
                //adapaterItem!!.contactManager.contactList.add(targetPosition,adapaterItem!!.getItem(sourcePosition))
                println("Start Position$sourcePosition")
                println("last Position$targetPosition")
                return true
            }

            override fun onSwiped(p0: RecyclerView.ViewHolder, p1: Int) {
            }
        })

        touchHelper!!.attachToRecyclerView(group_manager_RecyclerView!!)

        //endregion
    }

    //region ========================================= Functions ============================================

    fun longRecyclerItemClick(position: Int, secondClickLibelle: Boolean, fromLibelleClick: Boolean) {
        if (!secondClickLibelle) {
            if (listOfItemSelected.contains(gestionnaireContacts!!.contactList[position])) {
                listOfItemSelected.remove(gestionnaireContacts!!.contactList[position])

                if (listOfItemSelected.size == 0) {
                    group_manager_FloatingButtonSMS!!.visibility = View.GONE
                    group_manager_FloatingButtonMail!!.visibility = View.GONE
                    group_manager_FloatingButtonSend!!.visibility = View.GONE
                }

            } else {
                listOfItemSelected.add(gestionnaireContacts!!.contactList[position])
                verifiedContactsChannel(listOfItemSelected)
            }

            if (fromLibelleClick) {
                firstClick = true
            }
        } else {
            listOfItemSelected.clear()
            group_manager_FloatingButtonSMS!!.visibility = View.GONE
            group_manager_FloatingButtonMail!!.visibility = View.GONE
            group_manager_FloatingButtonSend!!.visibility = View.GONE
        }


        if (listOfItemSelected.size == 1 && firstClick) {
            Toast.makeText(this, R.string.main_toast_multi_select_actived, Toast.LENGTH_LONG).show()
            firstClick = false
        } else if (listOfItemSelected.size == 0) {
            Toast.makeText(this, R.string.main_toast_multi_select_deactived, Toast.LENGTH_LONG).show()
            firstClick = true
        }
    }

    fun gridLongItemClick(position: Int) {
        if (listOfItemSelected.contains(
                        gestionnaireContacts!!.contactList.get(position))) {
            listOfItemSelected.remove(
                    gestionnaireContacts!!.contactList.get(position))
        } else {
            listOfItemSelected.add(
                    gestionnaireContacts!!.contactList.get(position))
            verifiedContactsChannel(listOfItemSelected)
        }

        if (listOfItemSelected.size == 1 && firstClick) {
            Toast.makeText(this, R.string.main_toast_multi_select_actived, Toast.LENGTH_LONG).show()
            firstClick = false
        } else if (listOfItemSelected.size == 0) {
            Toast.makeText(this, R.string.main_toast_multi_select_deactived, Toast.LENGTH_LONG).show()
            group_manager_FloatingButtonSend!!.visibility = View.GONE
            group_manager_FloatingButtonMail!!.visibility = View.GONE
            group_manager_FloatingButtonSMS!!.visibility = View.GONE
            group_manager_FloatingButtonAddNewGroup!!.visibility = View.GONE
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
        group_manager_FloatingButtonAddNewGroup!!.visibility = View.GONE
        group_manager_FloatingButtonSend!!.visibility = View.VISIBLE
        //var i = 2
        val metrics = DisplayMetrics()
        this.windowManager.defaultDisplay.getMetrics(metrics)
        val margin = (0.2
                * metrics.densityDpi).toInt()
        println("metric smartphone" + metrics.densityDpi)
        if (allContactsHavePhoneNumber) {
            group_manager_FloatingButtonSMS!!.visibility = View.VISIBLE
            //    i++
        } else {
            println("false phoneNumber")
            group_manager_FloatingButtonSMS!!.visibility = View.GONE
        }
        if (allContactsHaveMail) {
            group_manager_FloatingButtonMail!!.visibility = View.VISIBLE
            val params: ViewGroup.MarginLayoutParams = group_manager_FloatingButtonMail!!.layoutParams as ViewGroup.MarginLayoutParams
            params.bottomMargin = margin
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
        val sharedPreferences = getSharedPreferences("group", Context.MODE_PRIVATE)
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
        gestionnaireContacts = ContactManager(this)
        gestionnaireContacts!!.contactList = listContactGroup
        if (len >= 3) {
            adapaterItem = GroupAdapter(this, gestionnaireContacts!!, len)
            group_manager_RecyclerView!!.layoutManager = GridLayoutManager(this, len)
        } else {
            adapaterItem = GroupAdapter(this, gestionnaireContacts!!, 4)
            group_manager_RecyclerView!!.layoutManager = GridLayoutManager(this, 4)
        }
        val sectionList = arrayOfNulls<SectionGroupAdapter.Section>(sections.size)
        val sectionAdapter = SectionGroupAdapter(this, R.layout.group_manager_recycler_adapter_section, group_manager_RecyclerView, adapaterItem)
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

    override fun onDrawerStateChanged(newState: Int) {
    }

    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
        adapaterItem!!.closeMenu()
    }

    override fun onDrawerClosed(drawerView: View) {
    }

    override fun onDrawerOpened(drawerView: View) {
        adapaterItem!!.closeMenu()
    }


    //endregion
}