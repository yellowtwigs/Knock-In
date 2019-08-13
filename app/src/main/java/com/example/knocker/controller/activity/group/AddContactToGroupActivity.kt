package com.example.knocker.controller.activity.group

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import androidx.appcompat.widget.Toolbar
import com.example.knocker.R
import com.example.knocker.model.ContactsRoomDatabase
import com.example.knocker.model.ModelDB.ContactDB
import com.example.knocker.model.ModelDB.ContactWithAllInformation
import com.example.knocker.model.ModelDB.LinkContactGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AddContactToGroupActivity : AppCompatActivity() {

    private var contactsDatabase: ContactsRoomDatabase? = null
    private var addContactToGroupListView: ListView? = null
    private var addContactToGroupAdapter: AddContactToGroupAdapter? = null
    private var groupId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //region ======================================== Theme Dark ========================================

        val sharedThemePreferences = getSharedPreferences("Knocker_Theme", Context.MODE_PRIVATE)
        if (sharedThemePreferences.getBoolean("darkTheme", false)) {
            setTheme(R.style.AppThemeDark)
        } else {
            setTheme(R.style.AppTheme)
        }

        //endregion

        setContentView(R.layout.activity_add_contact_to_group)

        addContactToGroupListView = findViewById(R.id.add_contact_to_group_listview)

        contactsDatabase = ContactsRoomDatabase.getDatabase(this)

        //region ========================================= Toolbar ==========================================

        val toolbar = findViewById<Toolbar>(R.id.add_contact_to_group_toolbar)
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar.setHomeAsUpIndicator(R.drawable.ic_close)
        actionbar.title = getString(R.string.add_contact_to_group_toolbar_title)

        //endregion

        groupId = intent.getIntExtra("GroupId", 0)
        var allContactNotInGroup = listOf<ContactWithAllInformation>()
        if (groupId != 0)
            allContactNotInGroup = getContactNotInGroup(groupId)
        addContactToGroupAdapter = AddContactToGroupAdapter(this, allContactNotInGroup)
        addContactToGroupListView!!.adapter = addContactToGroupAdapter
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_toolbar_validate, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                refreshActivity()
            }
            R.id.nav_validate -> {
                if (addContactToGroupAdapter!!.allSelectContact.isEmpty()) {
                    MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                            .setTitle(R.string.add_contact_to_group_alert_dialog_title)
                            .setMessage(getString(R.string.add_contact_to_group_alert_dialog_message))
                            .show()
                } else {
                    addToGroup(addContactToGroupAdapter!!.allSelectContact, groupId)
                    refreshActivity()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun addToGroup(listContact: List<ContactDB>, groupId: Int) {
        if (contactsDatabase?.GroupsDao()!!.getGroup(groupId).name == "Favorites" || contactsDatabase?.GroupsDao()!!.getGroup(groupId).name == "Favoris") {
            addToFavorite()
        }

        listContact.forEach {
            val link = LinkContactGroup(groupId, it.id!!)
            contactsDatabase!!.LinkContactGroupDao().insert(link)
        }
    }

    //region ========================================== Favorites ===========================================

    private fun addToFavorite() {
        var counter = 0

        while (counter < addContactToGroupAdapter!!.allSelectContact.size) {
            val contact = contactsDatabase?.contactsDao()?.getContact(addContactToGroupAdapter!!.allSelectContact[counter].id!!)
            contact!!.setIsFavorite(contactsDatabase)

            counter++
        }
    }

    //endregion

    private fun getContactNotInGroup(groupId: Int): List<ContactWithAllInformation> {
        val allInGroup = mutableListOf<ContactWithAllInformation>()
        val groupMember = contactsDatabase!!.contactsDao().getContactForGroup(groupId)
        val allContact = contactsDatabase!!.contactsDao().sortContactByFirstNameAZ()
        allContact.forEach { all ->
            groupMember.forEach {
                if (all.contactDB!!.id == it.contactDB!!.id) {
                    allInGroup.add(all)
                }
            }
        }
        return allContact.minus(allInGroup)
    }

    private fun refreshActivity() {
        startActivity(Intent(this@AddContactToGroupActivity, GroupManagerActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
        finish()
    }
}