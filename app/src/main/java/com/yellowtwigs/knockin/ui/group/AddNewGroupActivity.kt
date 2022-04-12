package com.yellowtwigs.knockin.ui.group

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.model.ContactManager
import com.yellowtwigs.knockin.model.ContactsRoomDatabase
import com.yellowtwigs.knockin.model.data.ContactDB
import com.yellowtwigs.knockin.model.data.ContactWithAllInformation
import com.yellowtwigs.knockin.model.data.GroupDB
import com.yellowtwigs.knockin.model.data.LinkContactGroup

/**
 * Activité qui nous permet de créer un groupe
 * @author Florian Striebel
 */
class AddNewGroupActivity : AppCompatActivity() {

    //region ========================================== Val or Var ==========================================

    private var contactsDatabase: ContactsRoomDatabase? = null
    private var addNewGroupListView: ListView? = null
    private var createGroupAdapter: AddContactToGroupAdapter? = null
    private var addNewGroupName: TextView? = null

    private var listOfItemSelected: ArrayList<ContactWithAllInformation> = ArrayList()

    private var main_GridView: RecyclerView? = null
    private var gridViewAdapter: CreateGroupGridViewAdapter? = null
    private var main_RecyclerView: RecyclerView? = null
    private var test: Int = 0
    private var listtest: Int = 0
    private var recyclerViewAdapter: CreateGroupListViewAdapter? = null

    private var gestionnaireContacts: ContactManager? = null
    private val selectContact: MutableList<ContactDB>? = mutableListOf()

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

        val position = intent.getIntExtra("position", 0)
        //endregion
        /////////////////////////////
        gestionnaireContacts = ContactManager(this.applicationContext)
        main_GridView = findViewById(R.id.add_contact_to_group_grid_view_id)
        main_RecyclerView = findViewById(R.id.add_contact_to_group_recycler_view_id)

        val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
        val len = sharedPreferences.getInt("gridview", 1)

        //Vérification du mode d'affichage si c'est 1 ou inférieur alors l'affichage est sous forme de liste
        // sinon il sera sous forme de gridView

        if (len <= 1) {
            main_GridView!!.visibility = View.GONE
            main_RecyclerView!!.visibility = View.VISIBLE
        } else {
            main_GridView!!.visibility = View.VISIBLE
            main_RecyclerView!!.visibility = View.GONE
        }

        if (main_GridView!!.visibility != View.GONE) {

            gridViewAdapter = CreateGroupGridViewAdapter(this, gestionnaireContacts!!, len, null)
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
                    null
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

        val allContact = contactsDatabase!!.contactsDao().sortContactByFirstNameAZ()
        createGroupAdapter = AddContactToGroupAdapter(this, allContact)
        addNewGroupListView!!.adapter = createGroupAdapter
    }

    /**
     * Pour le menu de l'activité nous affectons la ressource menu [menu_toolbar_validate_skip]
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.toolbar_menu_validation, menu)
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
            R.id.nav_validate -> {//Si l'utilisateur clique sur validé ont crée le groupe avec les contact séléctionner dans l'adapter
                if (addNewGroupName!!.text.isNotEmpty()) {//Avant de vréer le groupe on vérifie que celui-ci à un nom
                    if (selectContact!!.isNotEmpty()) {//Et qu'il y a des contact dans le groupe
                        addToGroup(selectContact, addNewGroupName!!.text.toString())
                    } else {
                        Toast.makeText(this, getString(R.string.add_new_group_toast_no_contact_selected), Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this, getString(R.string.add_new_group_toast_empty_field), Toast.LENGTH_LONG).show()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun gridMultiSelectItemClick(position: Int) { ///// duplicata à changer vite
        if (listOfItemSelected.contains(gestionnaireContacts!!.contactList[position])) {
            listOfItemSelected.remove(gestionnaireContacts!!.contactList[position])
            selectContact!!.remove(gestionnaireContacts!!.contactList[position].contactDB!!)
        } else {
            listtest++
            listOfItemSelected.add(gestionnaireContacts!!.contactList[position])
            //
            var contact = gestionnaireContacts!!.contactList[position].contactDB!!
            selectContact!!.add(gestionnaireContacts!!.contactList[position].contactDB!!)
        }
    }

    fun listMultiSelectItemClick(position: Int) {
        if (listOfItemSelected.contains(gestionnaireContacts!!.contactList[position])) {
            listOfItemSelected.remove(gestionnaireContacts!!.contactList[position])

            selectContact!!.remove(gestionnaireContacts!!.contactList[position].contactDB!!)
        } else {
            test++
            listOfItemSelected.add(gestionnaireContacts!!.contactList[position])

            var contact = gestionnaireContacts!!.contactList[position].contactDB!!
            selectContact!!.add(gestionnaireContacts!!.contactList[position].contactDB!!)
        }
    }

    /**
     * crée un groupe et ajoute les contact de la liste a celui-ci
     * @param listContact [List<ContactDB>]
     * @param name [String]
     */
    private fun addToGroup(listContact: List<ContactDB>?, name: String) {
        val group = GroupDB(null, name, "", -500138) // création de l'objet groupe
        var counter = 0
        var alreadyExist = false

        while (counter < contactsDatabase?.GroupsDao()!!.getAllGroupsByNameAZ().size) { //Nous vérifions que le nom de groupe ne correspond à aucun autre groupe
            if (name == contactsDatabase?.GroupsDao()!!.getAllGroupsByNameAZ()[counter].groupDB!!.name) {
                alreadyExist = true
                break
            }
            counter++
        }

        if (alreadyExist) {//Si il existe Nous prévenons l'utilisateur sinon nous créons le groupe
            Toast.makeText(this, "Ce groupe existe déjà", Toast.LENGTH_LONG).show()
        } else {
            val groupId = contactsDatabase!!.GroupsDao().insert(group)
            listContact!!.forEach {
                val link = LinkContactGroup(groupId!!.toInt(), it.id!!)
                println("contact db id" + contactsDatabase!!.LinkContactGroupDao().insert(link))
            }
            println(contactsDatabase!!.GroupsDao().getAllGroupsByNameAZ())
            refreshActivity()
        }
    }

    private fun refreshActivity() {
        startActivity(Intent(this@AddNewGroupActivity, GroupManagerActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
        finish()
    }

    override fun onBackPressed() {
    }

}
