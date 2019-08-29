package com.yellowtwigs.knocker.controller.activity.group

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import androidx.appcompat.widget.Toolbar
import com.yellowtwigs.knocker.R
import com.yellowtwigs.knocker.model.ContactsRoomDatabase
import com.yellowtwigs.knocker.model.ModelDB.ContactDB
import com.yellowtwigs.knocker.model.ModelDB.ContactWithAllInformation
import com.yellowtwigs.knocker.model.ModelDB.LinkContactGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Activité qui nous permet d'ajouter des contacts a un groupe précis
 * @author Ryan Granet
 */
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

    /**
     * Pour le menu de l'activité nous lui affectons la ressource menu [menu_toolbar_validate]
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_toolbar_validate, menu)
        return true
    }

    /**
     * lorsqu'un élément du menu à été selectionner cette méthode est appelé par le système
     * @return [Boolean]
     */
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

    /**
     * Ajoute la liste des contacts sélectionné au groupe
     * @param listContact [List<ContactDB>]
     * @param groupId [Int]
     */
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
    /**
     * Pour le groupe favoris ont ajoute les contact au groupes favoris puis on leurs set la valeur favoris
     */
    private fun addToFavorite() {
        var counter = 0

        while (counter < addContactToGroupAdapter!!.allSelectContact.size) {
            val contact = contactsDatabase?.contactsDao()?.getContact(addContactToGroupAdapter!!.allSelectContact[counter].id!!)
            contact!!.setIsFavorite(contactsDatabase)

            counter++
        }
    }

    //endregion

    /**
     *récupère tous les contact qui ne sont pas dans le groupe
     * @param groupId [Int] //id du groupe dont on veut ajouté des contact
     * @return [List<ContactWithAllInformation>]
     */
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

    /**
     * Retour vers l'activité groupManager
     */
    private fun refreshActivity() {
        startActivity(Intent(this@AddContactToGroupActivity, GroupManagerActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
        finish()
    }
}