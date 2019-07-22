package com.example.knocker.controller.activity.group

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.graphics.Color
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.knocker.R
import com.example.knocker.controller.activity.*
import com.example.knocker.model.ContactsRoomDatabase
import com.example.knocker.model.DbWorkerThread
import com.example.knocker.model.ModelDB.ContactWithAllInformation
import com.example.knocker.model.ModelDB.GroupWithContact
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView

class GroupManagerActivity:AppCompatActivity(){

    private var group_DrawerLayout: DrawerLayout? = null
    private var group_ContactsDatabase: ContactsRoomDatabase? = null
    private lateinit var group_mDbWorkerThread: DbWorkerThread

    private var group_NavigationView: NavigationView? = null

    private var recycler: RecyclerView? = null

    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
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

        group_DrawerLayout = findViewById(R.id.group_drawer_layout)
        recycler = findViewById(R.id.group_list_view_id)
        group_NavigationView = findViewById(R.id.nav_view)

        //endregion

        recycler!!.setHasFixedSize(true)

        //region Navigation

        val menu = group_NavigationView!!.menu
        val nav_item = menu.findItem(R.id.nav_home)
        nav_item.isChecked = true
        val nav_sync_contact = menu.findItem(R.id.nav_sync_contact)
        nav_sync_contact.isVisible = true

        group_NavigationView!!.menu.getItem(1).isChecked = true

        group_NavigationView!!.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            group_DrawerLayout!!.closeDrawers()

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

        group_ContactsDatabase = ContactsRoomDatabase.getDatabase(this)

        group_mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        group_mDbWorkerThread.start()
        val group: ArrayList<GroupWithContact> = ArrayList()
        group.addAll(group_ContactsDatabase!!.GroupsDao().getAllGroupsByNameAZ())
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

        val adapter: GroupAdapter
        if (len >= 3) {
            adapter = GroupAdapter(this, listContactGroup, len)
            recycler!!.layoutManager = GridLayoutManager(this, len)
        } else {
            adapter = GroupAdapter(this, listContactGroup, 4)
            recycler!!.layoutManager = GridLayoutManager(this, 4)
        }
        val sectionList = arrayOfNulls<SectionGroupAdapter.Section>(sections.size)
        val sectionAdapter = SectionGroupAdapter(this, R.layout.recycler_adapter_section, recycler, adapter)
        sectionAdapter.setSections(sections.toArray(sectionList))
        println("taille list group " + listContactGroup.size)
        // val adapter= GroupListViewAdapter(group,this,len)
        recycler!!.adapter = sectionAdapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_help, menu)
        return super.onCreateOptionsMenu(menu)
    }

    @SuppressLint("ShowToast")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                group_DrawerLayout!!.openDrawer(GravityCompat.START)
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

}