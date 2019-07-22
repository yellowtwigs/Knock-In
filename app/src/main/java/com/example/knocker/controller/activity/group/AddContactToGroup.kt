package com.example.knocker.controller.activity.group

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import androidx.appcompat.widget.Toolbar
import com.example.knocker.R
import com.example.knocker.controller.activity.NotificationHistoryActivity
import com.example.knocker.model.ContactsRoomDatabase
import com.example.knocker.model.ModelDB.ContactDB
import com.example.knocker.model.ModelDB.ContactWithAllInformation
import com.example.knocker.model.ModelDB.LinkContactGroup

class AddContactToGroup : AppCompatActivity() {

    private var contactsDatabase: ContactsRoomDatabase? = null
    private var main_ListView: ListView? = null
    private var addContactToGroupAdapter: AddContactToGroupAdapter? = null
    private var groupId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_contact_to_group)
        main_ListView = findViewById(R.id.list_view_id)
        contactsDatabase = ContactsRoomDatabase.getDatabase(this)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar.setHomeAsUpIndicator(R.drawable.ic_cross)

        groupId = intent.getIntExtra("GroupId", 0)
        var allContactNotInGroup = listOf<ContactWithAllInformation>()
        if (groupId != 0)
            allContactNotInGroup = getContactNotInGroupe(groupId)
        addContactToGroupAdapter = AddContactToGroupAdapter(this, allContactNotInGroup)
        main_ListView!!.adapter = addContactToGroupAdapter
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_toolbar_validate, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_validate -> {
                addToGroup(addContactToGroupAdapter!!.allSelectContact, groupId)
                startActivity(Intent(this, GroupManagerActivity::class.java))
            }
        }
    return super.onOptionsItemSelected(item)
}

    fun addToGroup(listContact: List<ContactDB>, groupId: Int) {
        listContact.forEach {
            val link = LinkContactGroup(groupId, it.id!!)
            contactsDatabase!!.LinkContactGroupDao().insert(link)
        }
    }

    fun getContactNotInGroupe(groupId: Int): List<ContactWithAllInformation> {
        val allInGroup = mutableListOf<ContactWithAllInformation>()
        val groupMember = contactsDatabase!!.contactsDao().getContactForGroup(groupId)
        val allContact = contactsDatabase!!.contactsDao().getContactAllInfo()
        allContact.forEach {all ->
            groupMember.forEach {
                if (all.contactDB!!.id == it.contactDB!!.id) {
                    allInGroup.add(all)
                }
            }
        }
        return allContact.minus(allInGroup)
    }
}
