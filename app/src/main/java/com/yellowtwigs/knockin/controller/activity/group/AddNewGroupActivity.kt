package com.yellowtwigs.knockin.controller.activity.group

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
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.model.ContactsRoomDatabase
import com.yellowtwigs.knockin.model.ModelDB.ContactDB
import com.yellowtwigs.knockin.model.ModelDB.GroupDB
import com.yellowtwigs.knockin.model.ModelDB.LinkContactGroup

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
                    if (createGroupAdapter!!.allSelectContact.isNotEmpty()) {//Et qu'il y a des contact dans le groupe
                        addToGroup(createGroupAdapter!!.allSelectContact, addNewGroupName!!.text.toString())
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

    /**
     * crée un groupe et ajoute les contact de la liste a celui-ci
     * @param listContact [List<ContactDB>]
     * @param name [String]
     */
    private fun addToGroup(listContact: List<ContactDB>, name: String) {
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
            println("list is $listContact")
            val groupId = contactsDatabase!!.GroupsDao().insert(group)
            listContact.forEach {
                val link = LinkContactGroup(groupId!!.toInt(), it.id!!)
                println("contact db id" + contactsDatabase!!.LinkContactGroupDao().insert(link))
            }

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
