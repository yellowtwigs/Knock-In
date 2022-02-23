package com.yellowtwigs.knockin.ui.group

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ListView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.model.ContactsRoomDatabase
import com.yellowtwigs.knockin.model.data.ContactDB
import com.yellowtwigs.knockin.model.data.ContactWithAllInformation
import com.yellowtwigs.knockin.model.data.LinkContactGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yellowtwigs.knockin.model.ContactManager

/**
 * Activité qui nous permet d'ajouter des contacts a un groupe précis
 * @author Ryan Granet
 */
class AddContactToGroupActivity : AppCompatActivity() {

    private var contactsDatabase: ContactsRoomDatabase? = null
    private var addContactToGroupListView: ListView? = null
    private var addContactToGroupAdapter: AddContactToGroupAdapter? = null

    private var main_GridView: RecyclerView? = null
    private var gridViewAdapter: CreateGroupGridViewAdapter? = null
    private var main_RecyclerView: RecyclerView? = null
    private var recyclerViewAdapter: CreateGroupListViewAdapter? = null
    private var gestionnaireContacts: ContactManager? = null

    private val selectContact: MutableList<ContactDB>? = mutableListOf()
    private var listOfItemSelected: ArrayList<ContactWithAllInformation> = ArrayList()

    private var groupId: Int = 0

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

        setContentView(R.layout.activity_add_contact_to_group)

        addContactToGroupListView = findViewById(R.id.add_contact_to_group_listview)

        gestionnaireContacts = ContactManager(this.applicationContext)
        main_GridView = findViewById(R.id.add_contact_to_group_grid_view_id)
        main_RecyclerView = findViewById(R.id.add_contact_to_group_recycler_view_id)

        contactsDatabase = ContactsRoomDatabase.getDatabase(this)

        //region ========================================= Toolbar ==========================================

        val toolbar = findViewById<Toolbar>(R.id.add_contact_to_group_toolbar)
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar.setHomeAsUpIndicator(R.drawable.ic_close)
        actionbar.title = getString(R.string.add_contact_to_group_toolbar_title)

        //endregion

        val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
        val len = sharedPreferences.getInt("gridview", 1)

        //Vérification du mode d'affichage si c'est 1 ou inférieur alors l'affichage est sous forme de liste
        // sinon il sera sous forme de gridView

        groupId = intent.getIntExtra("GroupId", 0)
        val position = intent.getIntExtra("position", 0)
        var allContactNotInGroup = listOf<ContactWithAllInformation>()
        if (groupId != 0)
            allContactNotInGroup = getContactNotInGroup(groupId)
        addContactToGroupAdapter = AddContactToGroupAdapter(this, allContactNotInGroup)
        addContactToGroupListView!!.adapter = addContactToGroupAdapter

        if (len <= 1) {
            main_GridView!!.visibility = View.GONE
            main_RecyclerView!!.visibility = View.VISIBLE
        } else {
            main_GridView!!.visibility = View.VISIBLE
            main_RecyclerView!!.visibility = View.GONE
        }

        if (main_GridView!!.visibility != View.GONE) {

            gridViewAdapter =
                CreateGroupGridViewAdapter(
                    this,
                    gestionnaireContacts!!,
                    len,
                    allContactNotInGroup
                )
            main_GridView!!.adapter = gridViewAdapter
            main_GridView!!.layoutManager = GridLayoutManager(this, len)
            main_GridView!!.recycledViewPool.setMaxRecycledViews(0, 0)

            val index = sharedPreferences.getInt("index", 0)
            val edit: SharedPreferences.Editor = sharedPreferences.edit()
            edit.apply()
        }
        if (main_RecyclerView!!.visibility != View.GONE) {
            recyclerViewAdapter =
                CreateGroupListViewAdapter(
                    this,
                    gestionnaireContacts,
                    len,
                    allContactNotInGroup
                )
            main_RecyclerView!!.adapter = recyclerViewAdapter
            main_RecyclerView!!.layoutManager = LinearLayoutManager(this)
            main_RecyclerView!!.recycledViewPool.setMaxRecycledViews(0, 0)

            if (position == 0) {
                val index = sharedPreferences.getInt("index", 0)
                val edit: SharedPreferences.Editor = sharedPreferences.edit()
                main_RecyclerView!!.scrollToPosition(index)
                edit.putInt("index", 0)
                edit.apply()
            } else {
                main_RecyclerView!!.layoutManager!!.scrollToPosition(position)
            }

        }


    }

    /**
     * Pour le menu de l'activité nous lui affectons la ressource menu [menu_toolbar_validate]
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.toolbar_menu_validation, menu)
        return true
    }

    fun multiSelectItemClick(position: Int) {
        if (listOfItemSelected.contains(gestionnaireContacts!!.contactList[position])) {
            listOfItemSelected.remove(gestionnaireContacts!!.contactList[position])
            selectContact!!.remove(gestionnaireContacts!!.contactList[position].contactDB!!)
        } else {
            listOfItemSelected.add(gestionnaireContacts!!.contactList[position])
            selectContact!!.add(gestionnaireContacts!!.contactList[position].contactDB!!)
        }
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
                if (selectContact!!.isEmpty()) {
                    MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                            .setTitle(R.string.add_contact_to_group_alert_dialog_title)
                            .setMessage(getString(R.string.add_contact_to_group_alert_dialog_message))
                            .show()
                } else {
                    addToGroup(selectContact, groupId)
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