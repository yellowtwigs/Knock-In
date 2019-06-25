package com.example.knocker.controller.activity.firstLaunch

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.example.knocker.R
import com.example.knocker.controller.activity.MainActivity
import com.example.knocker.model.ContactList
import com.example.knocker.model.ModelDB.ContactWithAllInformation
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MultiSelectActivity : AppCompatActivity() {

    var activityVisible: Boolean = true;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multi_select)
        val multi_select_gridView = findViewById<GridView>(R.id.multiSelect_gridView)
        val gestionnaireContact = ContactList(this)
        val multi_select_textView = findViewById<TextView>(R.id.multiSelect_Tv_nb_contact)
        val adapter = SelectContactAdapter(this, gestionnaireContact, 4)
        val multi_select_validate = findViewById<ImageView>(R.id.multiSelect_validate)
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(applicationContext)) {
                overlayAlertDialogPermission().show()

            }
        }
        multi_select_textView.text = String.format(applicationContext.resources.getString(R.string.multi_select_nb_contact), adapter.listContactSelect().size)
        multi_select_gridView.numColumns += 4
        multi_select_gridView.adapter = adapter
        for (contact in ContactList(this).contacts) {
            println("contact is " + contact.contactDB!!.lastName + " " + contact.contactDB!!.firstName)
        }

        multi_select_gridView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            adapter.itemSelected(position)
            adapter.notifyDataSetChanged()
            multi_select_textView.text = String.format(applicationContext.resources.getString(R.string.multi_select_nb_contact), adapter.listContactSelect().size)

            true
        }
        multi_select_validate.setOnClickListener {
            val listItemSelect = adapter.listContactSelect()
            overlayAlertDialog(listItemSelect).show()
        }
    }

    private fun overlayAlertDialogPermission(): androidx.appcompat.app.AlertDialog {
        val inflater: LayoutInflater = this.layoutInflater
        val alertView: View = inflater.inflate(R.layout.alert_dialog_multi_select, null)

        val manage_notif_ButtonAlertDialogAllow = alertView.findViewById<Button>(R.id.alert_dialog_multi_select_button_allow_it)
        manage_notif_ButtonAlertDialogAllow.setOnClickListener { positiveAlertDialogButtonClick() }

        val manage_notif_ButtonAlertDialogDismiss = alertView.findViewById<Button>(R.id.alert_dialog_multi_select_button_dismiss)
        manage_notif_ButtonAlertDialogDismiss.setOnClickListener { negativeAlertDialogButtonClick() }

        return MaterialAlertDialogBuilder(this)
                .setView(alertView)
                .show()
    }

    private fun positiveAlertDialogButtonClick() {
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
    }

    private fun negativeAlertDialogButtonClick() {
        val sharedPreferences = getSharedPreferences("Knocker_preferences", Context.MODE_PRIVATE)
        val edit: SharedPreferences.Editor = sharedPreferences.edit()
        edit.putBoolean("popupNotif", false)//quand la personne autorise l'affichage par dessus d'autre application nous l'enregistrons
        edit.putBoolean("serviceNotif", true)
        edit.commit()
    }

    private fun overlayAlertDialog(contactList: List<ContactWithAllInformation>): android.app.AlertDialog {
        val alertDialogBuilder = android.app.AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Knocker")
        var message = "vous venez de séléctionner " + contactList.size
        for (contact in contactList) {
            message += "\n- " + contact.contactDB!!.firstName + " " + contact.contactDB!!.lastName
        }
        alertDialogBuilder.setMessage(message + "\n voulez-vous valider votre séléction")
        alertDialogBuilder.setPositiveButton("oui"
        ) { _, _ ->
            val gestionnaireContact = ContactList(contactList, this)
            if (!contactList.isEmpty()) {
                gestionnaireContact.setToContactInListPriority2()
            }
            startActivity(Intent(this@MultiSelectActivity, MainActivity::class.java))
        }
        alertDialogBuilder.setNegativeButton("non"
        ) { _, _ ->
        }
        return alertDialogBuilder.create()
    }


    override fun onResume() {
        super.onResume()
        activityVisible = true
        println("test resume")
    }

    override fun onPause() {
        super.onPause()
        activityVisible = false
        println("test pause")
    }

    override fun onStart() {
        super.onStart()
        activityVisible = true
        println("test start")
    }

    override fun onBackPressed() {

    }
}