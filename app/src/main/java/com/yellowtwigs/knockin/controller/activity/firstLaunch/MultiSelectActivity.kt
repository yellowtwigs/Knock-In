package com.yellowtwigs.knockin.controller.activity.firstLaunch

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
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
import com.yellowtwigs.knockin.controller.activity.ContactDetailsActivity
import com.yellowtwigs.knockin.controller.activity.MainActivity
import com.yellowtwigs.knockin.controller.activity.PremiumActivity
import com.yellowtwigs.knockin.controller.activity.group.GroupManagerActivity

/**
 * Activité qui nous permet de faire un multiSelect sur nos contact afin de les prioriser
 * @author Florian Striebel, Kenzy Suon
 */
class MultiSelectActivity : AppCompatActivity() {

    //region ========================================= Var or Val ===========================================

    private var multi_select_gridView: GridView? = null
    private var gestionnaireContact: ContactManager? = null
    private var multi_select_NumberOfContactsSelected: TextView? = null
    private var adapter: SelectContactAdapter? = null
    private var listItemSelect = ArrayList<ContactWithAllInformation>()

    private var firstClick = true

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
        multi_select_NumberOfContactsSelected = findViewById(R.id.multiSelect_Tv_nb_contact)
        gestionnaireContact = ContactManager(this)

        //endregion

        val sharedNumberOfContactsVIPPreferences: SharedPreferences = getSharedPreferences("nb_Contacts_VIP", Context.MODE_PRIVATE)

        val sharedAlarmNotifInAppPreferences: SharedPreferences = getSharedPreferences("Contacts_Unlimited_Bought", Context.MODE_PRIVATE)
        val contactsUnlimitedIsBought = sharedAlarmNotifInAppPreferences.getBoolean("Contacts_Unlimited_Bought", false)

        adapter = SelectContactAdapter(this, gestionnaireContact, 4)

        multi_select_NumberOfContactsSelected!!.text = String.format(applicationContext.resources.getString(R.string.multi_select_nb_contact), adapter!!.listContactSelect.size)
        multi_select_gridView!!.numColumns = 4
        multi_select_gridView!!.adapter = adapter

        if (Resources.getSystem().configuration.locale.language == "ar") {
            multi_select_NumberOfContactsSelected!!.text = "${adapter!!.listContactSelect.size} ${getString(R.string.multi_select_nb_contact)}"
        } else {
            multi_select_NumberOfContactsSelected!!.text = String.format(applicationContext.resources.getString(R.string.multi_select_nb_contact), adapter!!.listContactSelect.size)
        }

        //region ========================================= Listener =========================================

        //lors d'un click sur un item(Contact) de la gridview Nous l'ajoutons dans la liste des contact sélectionner puis nous modifions le texte du nombre de contact sélectionné
        multi_select_gridView!!.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->

            adapter!!.itemSelected(position)
            selectedItem(listItemSelect, gestionnaireContact!!.contactList[position])

            if (listItemSelect.size > 5 && firstClick && !contactsUnlimitedIsBought) {
                MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                        .setTitle(getString(R.string.in_app_popup_nb_vip_max_title))
                        .setMessage(getString(R.string.in_app_popup_nb_vip_max_message))
                        .setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
                            startActivity(Intent(this@MultiSelectActivity, PremiumActivity::class.java).putExtra("fromMultiSelectActivity", true))
                            firstClick = true
                        }
                        .setNegativeButton(R.string.alert_dialog_later) { _, _ ->
                        }
                        .show()
            } else {
                adapter!!.notifyDataSetChanged()

                if (Resources.getSystem().configuration.locale.language == "ar") {
                    multi_select_NumberOfContactsSelected!!.text = "${adapter!!.listContactSelect.size} ${getString(R.string.multi_select_nb_contact)}"
                } else {
                    multi_select_NumberOfContactsSelected!!.text = String.format(applicationContext.resources.getString(R.string.multi_select_nb_contact), adapter!!.listContactSelect.size)
                }

                val edit: SharedPreferences.Editor = sharedNumberOfContactsVIPPreferences.edit()
                edit.putInt("nb_Contacts_VIP", listItemSelect.size)
                edit.apply()
            }
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
            message = String.format(applicationContext.resources.getString(R.string.multi_select_alert_dialog_nb_contact), contactList.size, getString(R.string.multi_select_contact)) /////////
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
                .setTitle("Knock In")
                .setMessage(message + applicationContext.resources.getString(R.string.multi_select_validate_selection))
                .setBackground(getDrawable(R.color.backgroundColor))
                .setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
                    val gestionnaireContact = ContactManager(contactList, this)
                    if (contactList.isNotEmpty()) {
                        gestionnaireContact.setToContactInListPriority2()
                    }

                    MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                            .setTitle(getString(R.string.start_activity_title))
                            .setMessage(getString(R.string.start_activity_personnalize_alert) + applicationContext.resources.getString(R.string.personnalize_validate_selection))
                            .setPositiveButton(getString(R.string.alert_dialog_yes)) { _, _ ->
                                startActivity(Intent(this@MultiSelectActivity, PremiumActivity::class.java).putExtra("fromStartActivity", true))
                                finish()
                            }
                            .setNegativeButton(getString(R.string.alert_dialog_later)) { _, _ ->
                                startActivity(Intent(this@MultiSelectActivity, MainActivity::class.java).putExtra("fromStartActivity", true))
                                finish()
                            }
                            .show()

                }
                .setNegativeButton(R.string.alert_dialog_no) { _, _ ->
                }
    }

    fun selectedItem(listItemSelect: ArrayList<ContactWithAllInformation>, contact: ContactWithAllInformation) {
        if (listItemSelect.isEmpty()) {
            listItemSelect.add(contact)
        } else {
            if (listItemSelect.contains(contact)) {
                listItemSelect.remove(contact)
            } else {
                listItemSelect.add(contact)
            }
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
                val intent = Intent(this@MultiSelectActivity, MainActivity::class.java)
                intent.putExtra("fromStartActivity", true)
                startActivity(intent)
                finish()
            }
            R.id.nav_validate -> {
                overlayAlertDialog(adapter!!.listContactSelect!!).show()
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