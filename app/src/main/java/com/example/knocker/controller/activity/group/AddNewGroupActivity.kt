package com.example.knocker.controller.activity.group

import android.content.Context
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
import com.example.knocker.model.ContactsRoomDatabase
import com.example.knocker.model.ModelDB.ContactDB
import com.example.knocker.model.ModelDB.ContactWithAllInformation
import com.example.knocker.model.ModelDB.GroupDB
import com.example.knocker.model.ModelDB.LinkContactGroup

class AddNewGroupActivity : AppCompatActivity() {

    //region ========================================== Val or Var ==========================================

    private var contactsDatabase: ContactsRoomDatabase? = null
    private var addNewGroupListView: ListView? = null
    private var createGroupAdapter: AddContactToGroupAdapter? = null
    private var addNewGroupName:TextView? = null

    //endregion

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

        setContentView(R.layout.activity_add_new_group)
        addNewGroupListView = findViewById(R.id.add_new_group_list_view)
        addNewGroupName = findViewById(R.id.add_new_group_name)
        contactsDatabase = ContactsRoomDatabase.getDatabase(this)

        //region ========================================= Toolbar ==========================================

        val toolbar = findViewById<Toolbar>(R.id.add_new_group_toolbar)
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar.setHomeAsUpIndicator(R.drawable.ic_close)
        actionbar.title = ""

        //endregion


        var allContact = contactsDatabase!!.contactsDao().getContactAllInfo()
        createGroupAdapter = AddContactToGroupAdapter(this, allContact)
        addNewGroupListView!!.adapter = createGroupAdapter
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
                if(addNewGroupName!!.text.isNotEmpty()) {
                    if (createGroupAdapter!!.allSelectContact.isNotEmpty()){
                        addToGroup(createGroupAdapter!!.allSelectContact,addNewGroupName!!.text.toString())
                        refreshActivity()
                    }else{
                        Toast.makeText(this, getString(R.string.add_new_group_toast_no_contact_selected), Toast.LENGTH_LONG).show()
                    }
                }else{
                    Toast.makeText(this, getString(R.string.add_new_group_toast_empty_field), Toast.LENGTH_LONG).show()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun addToGroup(listContact: List<ContactDB>, name: String) {
        val group= GroupDB(null,name,"")
        println("list is $listContact")
        val groupId=contactsDatabase!!.GroupsDao().insert(group)
            listContact.forEach {
                val link = LinkContactGroup(groupId!!.toInt(), it.id!!)
                println("contact db id"+contactsDatabase!!.LinkContactGroupDao().insert(link))
            }

    }

    private fun refreshActivity() {
        startActivity(Intent(this@AddNewGroupActivity, GroupManagerActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
        finish()
    }

    override fun onBackPressed() {
    }

    private fun getContactNotInGroup(groupId: Int): List<ContactWithAllInformation> {
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
