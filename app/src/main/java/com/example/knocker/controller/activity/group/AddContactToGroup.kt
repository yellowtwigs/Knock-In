package com.example.knocker.controller.activity.group

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import androidx.appcompat.widget.Toolbar
import com.example.knocker.R
import com.example.knocker.model.ContactsRoomDatabase
import com.example.knocker.model.ModelDB.ContactWithAllInformation

class AddContactToGroup : AppCompatActivity() {

    private var contactsDatabase: ContactsRoomDatabase? = null
    private var main_ListView: ListView? = null
    private var addContactToGroupAdapter: AddContactToGroupAdapter? = null

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

        val groupId = intent.getIntExtra("GroupId", 0)
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
                println("VALIDATION")
            }
        }
    return super.onOptionsItemSelected(item)
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
