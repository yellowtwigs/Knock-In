package com.example.knocker.controller.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import com.example.knocker.R
import com.example.knocker.controller.SelectContactAdapter
import com.example.knocker.model.ContactList
import com.example.knocker.model.ModelDB.ContactWithAllInformation
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MultiSelectActivity : AppCompatActivity() {

    //region ========================================= Var or Val ===========================================

    private var multi_select_gridView: GridView? = null
    private var gestionnaireContact: ContactList? = null
    private var multi_select_textView: TextView? = null
    private var activityVisible: Boolean = true
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
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar.title = getString(R.string.multi_select_title)

        //endregion

        //region ====================================== FindViewById ========================================

        multi_select_gridView = findViewById(R.id.multiSelect_gridView)
        multi_select_textView = findViewById(R.id.multiSelect_Tv_nb_contact)
        gestionnaireContact = ContactList(this)

        //endregion

        adapter = SelectContactAdapter(this, gestionnaireContact, 4, true)

        multi_select_textView!!.text = String.format(applicationContext.resources.getString(R.string.multi_select_nb_contact), adapter!!.listContactSelect.size)
        multi_select_gridView!!.numColumns = 4
        multi_select_gridView!!.adapter = adapter

        multi_select_textView!!.text = String.format(applicationContext.resources.getString(R.string.multi_select_nb_contact), adapter!!.listContactSelect.size)

        multi_select_gridView!!.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            adapter!!.itemSelected(position)
            adapter!!.notifyDataSetChanged()
            multi_select_textView!!.text = String.format(applicationContext.resources.getString(R.string.multi_select_nb_contact), adapter!!.getListContactSelect().size)

            true
        }
    }

    private fun overlayAlertDialogPermission(): AlertDialog {
        val inflater: LayoutInflater = this.layoutInflater
        val alertView: View = inflater.inflate(R.layout.alert_dialog_multi_select, null)
        val alertDialog = MaterialAlertDialogBuilder(this)
                .setView(alertView)
                .show()
        val manage_notif_ButtonAlertDialogAllow = alertView.findViewById<Button>(R.id.alert_dialog_multi_select_button_allow_it)
        manage_notif_ButtonAlertDialogAllow.setOnClickListener { positiveAlertDialogButtonClick(alertDialog) }

        val manage_notif_ButtonAlertDialogDismiss = alertView.findViewById<Button>(R.id.alert_dialog_multi_select_button_dismiss)
        manage_notif_ButtonAlertDialogDismiss.setOnClickListener { negativeAlertDialogButtonClick(alertDialog) }

        return alertDialog
    }

    private fun positiveAlertDialogButtonClick(alertDialog: AlertDialog) {
        val intentPermission = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
        startActivity(intentPermission)
        val thread = Thread {
            activityVisible = false
            if (Build.VERSION.SDK_INT >= 23) {
                while (!Settings.canDrawOverlays(applicationContext) && !activityVisible) {
                }
                finish()
                val sharedPreferences = getSharedPreferences("Knocker_preferences", Context.MODE_PRIVATE)
                val edit: SharedPreferences.Editor = sharedPreferences.edit()
                edit.putBoolean("popupNotif", true)//quand la personne autorise l'affichage par dessus d'autre application nous l'enregistrons
                edit.apply()
                startActivity(Intent(this@MultiSelectActivity, MultiSelectActivity::class.java))
            }
        }
        thread.start()
        alertDialog.cancel()
    }

    private fun negativeAlertDialogButtonClick(alertDialog: AlertDialog) {
        alertDialog.cancel()
    }

    private fun overlayAlertDialog(contactList: ArrayList<ContactWithAllInformation>): android.app.AlertDialog {
        val alertDialogBuilder = android.app.AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Knocker")
        var message = ""
        if (contactList.size == 0) {
            message = applicationContext.resources.getString(R.string.multi_select_alert_dialog_0_contact)
        } else if (contactList.size == 1) {
            message = String.format(applicationContext.resources.getString(R.string.multi_select_alert_dialog_nb_contact), contactList.size, "contact :")
            if (contactList.size == 1) {
                val contact = contactList[0]
                message += "\n- " + contact.contactDB!!.firstName + " " + contact.contactDB!!.lastName
            }
        } else {
            message = String.format(applicationContext.resources.getString(R.string.multi_select_alert_dialog_nb_contact), contactList.size, "contacts :")
            for (contact in contactList) {
                message += "\n- " + contact.contactDB!!.firstName + " " + contact.contactDB!!.lastName
            }
        }
        alertDialogBuilder.setMessage(message + applicationContext.resources.getString(R.string.multi_select_validate_selection))
        alertDialogBuilder.setPositiveButton("oui"
        ) { _, _ ->
            val gestionnaireContact = ContactList(contactList, this)
            if (contactList.isNotEmpty()) {
                gestionnaireContact.setToContactInListPriority2()
            }
            startActivity(Intent(this@MultiSelectActivity, MainActivity::class.java))
        }
        alertDialogBuilder.setNegativeButton("non"
        ) { _, _ ->
        }
        return alertDialogBuilder.create()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_toolbar_validate, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_validate -> {
                if (Build.VERSION.SDK_INT >= 23) {
                    if (!Settings.canDrawOverlays(applicationContext)) {
                        overlayAlertDialogPermission().show()
                    } else {
                        val sharedPreferences = getSharedPreferences("Knocker_preferences", Context.MODE_PRIVATE)
                        val edit: SharedPreferences.Editor = sharedPreferences.edit()
                        edit.putBoolean("popupNotif", true)//quand la personne autorise l'affichage par dessus d'autre application nous l'enregistrons
                        edit.apply()
                    }
                }
                listItemSelect = adapter!!.listContactSelect
                overlayAlertDialog(listItemSelect!!).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        activityVisible = true
    }

    override fun onPause() {
        super.onPause()
        activityVisible = false
    }

    override fun onStart() {
        super.onStart()
        activityVisible = true
    }

    override fun onBackPressed() {

    }
}