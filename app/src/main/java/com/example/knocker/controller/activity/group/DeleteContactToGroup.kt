package com.example.knocker.controller.activity.group

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

class DeleteContactToGroup : AppCompatActivity() {

    private var contactsDatabase: ContactsRoomDatabase? = null
    private var delete_ListView: ListView? = null
    private var deleteContactToGroupAdapter: AddContactToGroupAdapter? = null
    private var groupId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delete_contact_to_group)
        delete_ListView = findViewById(R.id.delete_contact_listview)
        contactsDatabase = ContactsRoomDatabase.getDatabase(this)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar.setHomeAsUpIndicator(R.drawable.ic_cross)

        groupId = intent.getIntExtra("GroupId", 0)
        var allContactInGroup = listOf<ContactWithAllInformation>()
        if (groupId != 0)
            allContactInGroup = getContactInGroupe(groupId)
        deleteContactToGroupAdapter = AddContactToGroupAdapter(this, allContactInGroup)
        delete_ListView!!.adapter = deleteContactToGroupAdapter
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_toolbar_validate, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_validate -> {
                deleteToGroup(deleteContactToGroupAdapter!!.allSelectContact, groupId)
               //
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun deleteToGroup(listContact: List<ContactDB>, groupId: Int) {
        var message=""
        if(listContact.size==0){
            message= resources.getString(R.string.delete_contact_0_contact)
        }else if(listContact.size==1){
            message= String.format(resources.getString(R.string.delete_contact)," "+listContact.get(0).firstName+" "+listContact.get(0).lastName+" ")
                    //resources.getString(R.string.delete_contact)+" "+listContact.get(0).firstName+" "+listContact.get(0).lastName+
        }else{
            message= String.format(resources.getString(R.string.delete_contact),"")+" :"
            listContact.forEach {
                message+="\n- "+it.firstName+" "+it.lastName
            }
        }
        MaterialAlertDialogBuilder(this)
                .setTitle("Suppression de contact")
                .setMessage(message)
                .setPositiveButton(R.string.edit_contact_validate) { dialog, which ->
                    listContact.forEach {
                        val link = LinkContactGroup(groupId, it.id!!)
                        contactsDatabase!!.LinkContactGroupDao().deleteContactIngroup(it.id!!,groupId)
                        startActivity(Intent(this, GroupManagerActivity::class.java))
                    }
                }.setNegativeButton("annuler") { dialog, which -> }.show()

    }

    private fun getContactInGroupe(groupId: Int): List<ContactWithAllInformation> {
       return contactsDatabase!!.contactsDao().getContactForGroup(groupId)
    }
}
