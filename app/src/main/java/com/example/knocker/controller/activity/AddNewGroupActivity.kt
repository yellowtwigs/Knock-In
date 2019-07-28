package com.example.knocker.controller.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.example.knocker.R
import com.example.knocker.controller.activity.group.AddContactToGroupAdapter
import com.example.knocker.controller.activity.group.GroupManagerActivity
import com.example.knocker.model.ContactsRoomDatabase
import com.example.knocker.model.ModelDB.ContactDB
import com.example.knocker.model.ModelDB.ContactWithAllInformation
import com.example.knocker.model.ModelDB.GroupDB
import com.example.knocker.model.ModelDB.LinkContactGroup
import java.time.Duration

class AddNewGroupActivity : AppCompatActivity() {
    private var contactsDatabase: ContactsRoomDatabase? = null
    private var main_ListView: ListView? = null
    private var createGroupAdapter: AddContactToGroupAdapter? = null
    private var groupName:TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_group)
        main_ListView = findViewById(R.id.list_view_id)
        groupName = findViewById(R.id.add_group_name)
        contactsDatabase = ContactsRoomDatabase.getDatabase(this)

        val toolbar = findViewById<Toolbar>(R.id.group_toolbar)
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar.setHomeAsUpIndicator(R.drawable.ic_cross)


        var allContact = contactsDatabase!!.contactsDao().getContactAllInfo()
        createGroupAdapter = AddContactToGroupAdapter(this, allContact)
        main_ListView!!.adapter = createGroupAdapter
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_toolbar_validate, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
            R.id.nav_validate -> {
                if(!groupName!!.text.isEmpty()) {
                    if (!createGroupAdapter!!.allSelectContact.isEmpty()){
                        addToGroup(createGroupAdapter!!.allSelectContact,groupName!!.text.toString())
                        startActivity(Intent(this, GroupManagerActivity::class.java))
                    }else{
                        Toast.makeText(this, getString(R.string.add_new_contact_toast_nb_contact), Toast.LENGTH_LONG).show()
                    }
                }else{
                    Toast.makeText(this, getString(R.string.add_new_contact_toast_group_name), Toast.LENGTH_LONG).show()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun addToGroup(listContact: List<ContactDB>, name: String) {
        val group= GroupDB(null,name,"")
        val groupId=contactsDatabase!!.GroupsDao().insert(group)
            listContact.forEach {
                val link = LinkContactGroup(groupId!!.toInt(), it.id!!)
                contactsDatabase!!.LinkContactGroupDao().insert(link)
            }

    }

    private fun getContactNotInGroupe(groupId: Int): List<ContactWithAllInformation> {
        val allInGroup = mutableListOf<ContactWithAllInformation>()
        val groupMember = contactsDatabase!!.contactsDao().getContactForGroup(groupId)
        val allContact = contactsDatabase!!.contactsDao().getContactAllInfo()
        allContact.forEach { all ->
            groupMember.forEach {
                if (all.contactDB!!.id == it.contactDB!!.id) {
                    allInGroup.add(all)
                }
            }
        }
        return allContact.minus(allInGroup)
    }
}
