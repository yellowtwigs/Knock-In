package com.yellowtwigs.knockin.controller.activity.firstLaunch

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.ui.adapters.MultiSelectAdapter
import com.yellowtwigs.knockin.model.ContactManager
import com.yellowtwigs.knockin.model.ModelDB.ContactWithAllInformation
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yellowtwigs.knockin.controller.activity.MainActivity
import com.yellowtwigs.knockin.controller.activity.PremiumActivity
import com.yellowtwigs.knockin.databinding.ActivityMultiSelectBinding

/**
 * Activité qui nous permet de faire un multiSelect sur nos contact afin de les prioriser
 * @author Florian Striebel, Kenzy Suon
 */
class MultiSelectActivity : AppCompatActivity() {

    //region ========================================= Var or Val ===========================================

    private var contactManager: ContactManager? = null
    private var listItemSelect = ArrayList<ContactWithAllInformation>()

    private val cxt = this
    private lateinit var binding: ActivityMultiSelectBinding
    private lateinit var multiSelectAdapter: MultiSelectAdapter

    private lateinit var numberOfContactsVIPref: SharedPreferences
    private lateinit var contactsUnlimitedPref: SharedPreferences

    private var contactsUnlimitedBought = true
    private var firstClick = true
    private var tooMuch = false

    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMultiSelectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //region ========================================= Toolbar ==========================================

        setSupportActionBar(binding.toolbar)
        val actionbar = supportActionBar
        actionbar?.setDisplayHomeAsUpEnabled(false)
        actionbar?.title = getString(R.string.multi_select_title)

        //endregion

        //region ===================================== SharedPreferences ====================================

        numberOfContactsVIPref = getSharedPreferences("nb_Contacts_VIP", Context.MODE_PRIVATE)
        contactsUnlimitedPref =
            getSharedPreferences("Contacts_Unlimited_Bought", Context.MODE_PRIVATE)
        contactsUnlimitedBought =
            contactsUnlimitedPref.getBoolean("Contacts_Unlimited_Bought", false)

        //endregion

        contactManager = ContactManager(this)
        loadRecyclerView()

        binding.multiSelectTextView.text = String.format(
            applicationContext.resources.getString(R.string.multi_select_nb_contact),
            multiSelectAdapter.listContactSelect.size
        )

        if (Resources.getSystem().configuration.locale.language == "ar") {
            binding.multiSelectTextView.text =
                "${multiSelectAdapter.listContactSelect.size} ${getString(R.string.multi_select_nb_contact)}"
        } else {
            binding.multiSelectTextView.text = String.format(
                applicationContext.resources.getString(R.string.multi_select_nb_contact),
                multiSelectAdapter.listContactSelect.size
            )
        }
    }

    private fun loadRecyclerView() {
        multiSelectAdapter = MultiSelectAdapter(this, 4, contactsUnlimitedBought) { position ->
            multiSelectAdapter.itemSelected(position)

            contactManager?.contactList?.get(position)
                ?.let { selectedItem(listItemSelect, it) }

            if (tooMuch && !contactsUnlimitedBought) {
                MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                    .setTitle(getString(R.string.in_app_popup_nb_vip_max_title))
                    .setMessage(getString(R.string.in_app_popup_nb_vip_max_message))
                    .setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
                        startActivity(
                            Intent(
                                this@MultiSelectActivity,
                                PremiumActivity::class.java
                            ).putExtra("fromMultiSelectActivity", true)
                        )
                    }
                    .setNegativeButton(R.string.alert_dialog_later) { _, _ ->
                    }
                    .show()
            } else {
                if (Resources.getSystem().configuration.locale.language == "ar") {
                    binding.multiSelectTextView.text =
                        "${multiSelectAdapter.listContactSelect.size} ${getString(R.string.multi_select_nb_contact)}"
                } else {
                    binding.multiSelectTextView.text = String.format(
                        applicationContext.resources.getString(R.string.multi_select_nb_contact),
                        multiSelectAdapter.listContactSelect.size
                    )
                }

                val edit: SharedPreferences.Editor = numberOfContactsVIPref.edit()
                edit.putInt("nb_Contacts_VIP", listItemSelect.size)
                edit.apply()
            }
        }
        multiSelectAdapter.submitList(null)
        multiSelectAdapter.submitList(contactManager?.contactList)
        binding.multiSelectRecyclerView.apply {
            adapter = multiSelectAdapter
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(cxt, 4, RecyclerView.VERTICAL, false)
        }

        binding.multiSelectRecyclerView

        multiSelectAdapter.notifyDataSetChanged()
    }

    /**
     * Initialise une alertDialog pour prévenir l'utilisateur quelles sont les contact qui vont être prioriser
     * @param contactList [ArrayList<ContactWithAllInformation>] liste des contact sélectionner
     * @return alertDialog [MaterialAlertDialog]
     */
    private fun overlayAlertDialog(contactList: ArrayList<ContactWithAllInformation>): MaterialAlertDialogBuilder {
        var message: String

        if (contactList.size == 0) {
            message =
                applicationContext.resources.getString(R.string.multi_select_alert_dialog_0_contact)
        } else if (contactList.size == 1) {
            message = String.format(
                applicationContext.resources.getString(R.string.multi_select_alert_dialog_nb_contact),
                contactList.size,
                getString(R.string.multi_select_contact)
            )
            if (contactList.size == 1) {
                val contact = contactList[0]
                message += "\n- " + contact.contactDB!!.firstName + " " + contact.contactDB!!.lastName
            }
        } else {
            message = String.format(
                applicationContext.resources.getString(R.string.multi_select_alert_dialog_nb_contact),
                contactList.size,
                getString(R.string.multi_select_contacts)
            )
            for (contact in contactList) {
                message += "\n- " + contact.contactDB!!.firstName + " " + contact.contactDB!!.lastName
            }
        }

        return MaterialAlertDialogBuilder(this, R.style.AlertDialog)
            .setTitle("Knock In")
            .setMessage(message + applicationContext.resources.getString(R.string.multi_select_validate_selection))
            .setBackground(getDrawable(R.color.backgroundColor))
            .setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
                val contactManag = ContactManager(contactList, this)
                if (contactList.isNotEmpty()) {
                    contactManag.setToContactInListPriority2()
                }
                startActivity(
                    Intent(
                        this@MultiSelectActivity,
                        MainActivity::class.java
                    ).putExtra("fromStartActivity", true)
                )
                finish()
            }
            .setNegativeButton(R.string.alert_dialog_no) { _, _ ->
            }
    }

    fun selectedItem(
        listItemSelect: ArrayList<ContactWithAllInformation>,
        contact: ContactWithAllInformation
    ) {
        if (listItemSelect.isEmpty() && firstClick) {
            listItemSelect.add(contact)
            firstClick = false
        } else {
            if (listItemSelect.contains(contact)) {
                listItemSelect.remove(contact)
                tooMuch = false
                if (listItemSelect.isEmpty())
                    firstClick = true
            } else {
                if (listItemSelect.size == 5) {
                    tooMuch = true
                } else {
                    listItemSelect.add(contact)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.toolbar_menu_select_vip, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_skip -> {
                val intent = Intent(this@MultiSelectActivity, MainActivity::class.java)
                intent.putExtra("fromStartActivity", true)
                startActivity(intent)
                finish()
            }
            R.id.nav_validate -> {
                overlayAlertDialog(multiSelectAdapter.listContactSelect).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {

    }
}