package com.yellowtwigs.knockin.controller.activity.firstLaunch

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.widget.Toolbar
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.controller.SelectContactAdapter
import com.yellowtwigs.knockin.model.ContactManager
import com.yellowtwigs.knockin.model.ModelDB.ContactWithAllInformation
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Activité qui nous permet de faire un multiSelect sur nos contact afin de les prioriser
 * @author Florian Striebel, Kenzy Suon
 */
class MultiSelectActivity : AppCompatActivity() {

    //region ========================================= Var or Val ===========================================

    private var multi_select_gridView: GridView? = null
    private var gestionnaireContact: ContactManager? = null
    private var multi_select_textView: TextView? = null
    private var adapter: SelectContactAdapter? = null
    private var listItemSelect: ArrayList<ContactWithAllInformation>? = null

    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multi_select)

        //region ========================================= Toolbar ==========================================

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(false)
        actionbar.title = getString(R.string.multi_select_title)

        //endregion

        //region ====================================== FindViewById ========================================

        multi_select_gridView = findViewById(R.id.multiSelect_gridView)
        multi_select_textView = findViewById(R.id.multiSelect_Tv_nb_contact)
        gestionnaireContact = ContactManager(this)

        //endregion

        adapter = SelectContactAdapter(this, gestionnaireContact, 4)

        multi_select_textView!!.text = String.format(applicationContext.resources.getString(R.string.multi_select_nb_contact), adapter!!.listContactSelect.size)
        multi_select_gridView!!.numColumns = 4
        multi_select_gridView!!.adapter = adapter

        multi_select_textView!!.text = String.format(applicationContext.resources.getString(R.string.multi_select_nb_contact), adapter!!.listContactSelect.size)

        //region =================================== listener============================================

        //lors d'un click sur un item(Contact) de la gridview Nous l'ajoutons dans la liste des contact sélectionner puis nous modifions le texte du nombre de contact sélectionné
        multi_select_gridView!!.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            adapter!!.itemSelected(position)
            adapter!!.notifyDataSetChanged()
            multi_select_textView!!.text = String.format(applicationContext.resources.getString(R.string.multi_select_nb_contact), adapter!!.listContactSelect.size)
        }
        //endregion
    }

    /**
     * Initialise une alertDialog pour prévenir l'utilisateur quelles sont les contact qui vont être prioriser
     * @param contactList [ArrayList<ContactWithAllInformation>] liste des contact sélectionner
     * @return alertDialog [MaterialAlertDialog]
     */
    private fun overlayAlertDialog(contactList: ArrayList<ContactWithAllInformation>): MaterialAlertDialogBuilder {

        var message: String

        if (contactList.size == 0) {
            message = applicationContext.resources.getString(R.string.multi_select_alert_dialog_0_contact)
        } else if (contactList.size == 1) {
            message = String.format(applicationContext.resources.getString(R.string.multi_select_alert_dialog_nb_contact), contactList.size, getString(R.string.multi_select_contact))
            if (contactList.size == 1) {
                val contact = contactList[0]
                message += "\n- " + contact.contactDB!!.firstName + " " + contact.contactDB!!.lastName
            }
        } else {
            message = String.format(applicationContext.resources.getString(R.string.multi_select_alert_dialog_nb_contact), contactList.size, getString(R.string.multi_select_contacts))
            for (contact in contactList) {
                message += "\n- " + contact.contactDB!!.firstName + " " + contact.contactDB!!.lastName
            }
        }

        return MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                .setTitle("Knockin")
                .setMessage(message + applicationContext.resources.getString(R.string.multi_select_validate_selection))
                .setBackground(getDrawable(R.color.backgroundColor))
                .setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
                    val gestionnaireContact = ContactManager(contactList, this)
                    if (contactList.isNotEmpty()) {
                        gestionnaireContact.setToContactInListPriority2()
                    }
                    val intent = Intent(this@MultiSelectActivity, TutorialActivity::class.java)
                    intent.putExtra("fromStartActivity", true)
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton(R.string.alert_dialog_no) { _, _ ->
                }
    }

    /**
     * Pour le menu de l'activité nous affectons la ressource menu [menu_toolbar_validate_skip]
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.toolbar_menu_select_vip, menu)
        return true
    }

    /**
     * lorsqu'un élément du menu à été selectionner cette méthode est appelé par le système
     * @return [Boolean]
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_skip -> {
                val intent = Intent(this@MultiSelectActivity, TutorialActivity::class.java)
                intent.putExtra("fromStartActivity", true)
                startActivity(intent)
                finish()
            }
            R.id.nav_validate -> {

                listItemSelect = adapter!!.listContactSelect
                overlayAlertDialog(listItemSelect!!).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Réécriture de la méthode onBackPressed lorsque nous appuyant sur le boutton retour du téléphone rien n'est fait
     */
    override fun onBackPressed() {

    }
}