package com.yellowtwigs.knockin.controller.activity.group

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.model.ContactsRoomDatabase
import com.yellowtwigs.knockin.model.ModelDB.ContactDB
import com.yellowtwigs.knockin.model.ModelDB.ContactWithAllInformation
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Activité qui nous permet de supprimmer les contact d'un groupe
 */
class DeleteContactFromGroupActivity : AppCompatActivity() {

    //region ========================================= Var or Val ===========================================

    private var contactsDatabase: ContactsRoomDatabase? = null
    private var deleteContactFromGroupListView: ListView? = null
    private var deleteContactFromGroupAdapter: AddContactToGroupAdapter? = null
    private var groupId: Int = 0
    private var listIsEmpty = false

    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //region ======================================== Theme Dark ========================================

        val sharedThemePreferences = getSharedPreferences("Knockin_Theme", Context.MODE_PRIVATE)
        if (sharedThemePreferences.getBoolean("darkTheme", false)) {
            setTheme(R.style.AppThemeDark)
        } else {
            setTheme(R.style.AppTheme)
        }

        //endregion

        setContentView(R.layout.activity_delete_contact_from_group)
        deleteContactFromGroupListView = findViewById(R.id.delete_contact_from_group_listview)
        contactsDatabase = ContactsRoomDatabase.getDatabase(this)

        //region ========================================= Toolbar ==========================================

        val toolbar = findViewById<Toolbar>(R.id.delete_contact_from_group_toolbar)
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar.setHomeAsUpIndicator(R.drawable.ic_close)
        actionbar.title = getString(R.string.delete_contact_from_group_toolbar_title)

        //endregion

        groupId = intent.getIntExtra("GroupId", 0)
        var allContactInGroup = listOf<ContactWithAllInformation>()
        if (groupId != 0)
            allContactInGroup = getContactInGroup(groupId)
        deleteContactFromGroupAdapter = AddContactToGroupAdapter(this, allContactInGroup)
        deleteContactFromGroupListView!!.adapter = deleteContactFromGroupAdapter
    }

    //region ========================================= Toolbar ==========================================

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.toolbar_menu_validation, menu)
        return true
    }

    private fun refreshActivity() {
        startActivity(Intent(this@DeleteContactFromGroupActivity, GroupManagerActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                refreshActivity()
            }
            R.id.nav_validate -> {
                deleteFromGroup(deleteContactFromGroupAdapter!!.allSelectContact, groupId)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Supprime du groupe les contacts présent dans la liste
     * @param listContact [List<ContactDB>]
     * @param groupId [Int]
     */
    private fun deleteFromGroup(listContact: List<ContactDB>, groupId: Int) {

        if (contactsDatabase?.GroupsDao()!!.getGroup(groupId).name == "Favorites" || contactsDatabase?.GroupsDao()!!.getGroup(groupId).name == "Favoris") {
            removeFromFavorite()
        }

        var message: String
        when {
            listContact.isEmpty() -> {
                listIsEmpty = true
                message = resources.getString(R.string.delete_contact_0_contact)
            }
            listContact.size == 1 -> message = String.format(resources.getString(R.string.delete_contact_from_group), " " + listContact[0].firstName + " " + listContact[0].lastName + " ")
            else -> {
                message = String.format(resources.getString(R.string.delete_contact_from_group), "") + " :"
                listContact.forEach {
                    message += "\n- " + it.firstName + " " + it.lastName
                }
            }
        }

        if (listIsEmpty) {
            MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                    .setTitle(R.string.delete_contact_from_group_title)
                    .setMessage(message)
                    .setNegativeButton(R.string.delete_contact_from_group_cancel) { _, _ -> }.show()
        } else {
            MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                    .setTitle(R.string.delete_contact_from_group_title)
                    .setMessage(message)
                    .setPositiveButton(R.string.edit_contact_validate) { _, _ ->
                        listContact.forEach {
                            contactsDatabase!!.LinkContactGroupDao().deleteContactIngroup(it.id!!, groupId)
                        }
                        var counter = 0

                        while (counter < contactsDatabase?.GroupsDao()!!.getAllGroupsByNameAZ().size) {
//                            if (contactsDatabase?.GroupsDao()!!.getAllGroupsByNameAZ()[counter].groupDB!!.name == "Favorites" || contactsDatabase?.GroupsDao()!!.getAllGroupsByNameAZ()[counter].groupDB!!.name == "Favoris") {
                            if (contactsDatabase?.GroupsDao()!!.getAllGroupsByNameAZ()[counter].getListContact(this).isEmpty()) {
                                contactsDatabase?.GroupsDao()!!.deleteGroupById(contactsDatabase?.GroupsDao()!!.getAllGroupsByNameAZ()[counter].groupDB!!.id!!.toInt())
                                break
                            }
//                            }
                            counter++
                        }

                        startActivity(Intent(this, GroupManagerActivity::class.java))
                    }.setNegativeButton(R.string.delete_contact_from_group_cancel) { _, _ -> }.show()
        }

    }

    /**
     * Enlever un contact du groupe favoris lui envleve aussi l'attribut favoris
     */
    private fun removeFromFavorite() {
        var counter = 0

        while (counter < deleteContactFromGroupAdapter!!.allSelectContact.size) {
            val contact = contactsDatabase?.contactsDao()?.getContact(deleteContactFromGroupAdapter!!.allSelectContact[counter].id!!)
            contact!!.setIsNotFavorite(contactsDatabase)

            counter++
        }
    }

    /**
     * Récupère tous les contact dans le group
     * @param groupId [Int]
     */
    private fun getContactInGroup(groupId: Int): List<ContactWithAllInformation> {
        return contactsDatabase!!.contactsDao().getContactForGroup(groupId)
    }

    //endregion
}
