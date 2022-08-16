package com.yellowtwigs.knockin.ui.first_launch.first_vip_selection

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yellowtwigs.knockin.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yellowtwigs.knockin.ui.in_app.PremiumActivity
import com.yellowtwigs.knockin.databinding.ActivityMultiSelectBinding
import com.yellowtwigs.knockin.ui.contacts.list.ContactsListViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FirstVipSelectionActivity : AppCompatActivity() {

    //region ========================================= Var or Val ===========================================

    private val cxt = this
    private lateinit var binding: ActivityMultiSelectBinding
    private lateinit var firstVipSelectionAdapter: FirstVipSelectionAdapter

    private lateinit var numberOfContactsVIPref: SharedPreferences
    private lateinit var contactsUnlimitedPref: SharedPreferences

    private var contactsUnlimitedBought = true

    private var firstClick = true
    private var tooMuch = false

    private val contactsViewModel: ContactsListViewModel by viewModels()

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

        loadRecyclerView()

//        alreadyVip()

        binding.multiSelectTextView.text = String.format(
            applicationContext.resources.getString(R.string.multi_select_nb_contact),
            firstVipSelectionAdapter.listContactSelect.size
        )

//        if (Resources.getSystem().configuration.locale.language == "ar") {
//            binding.multiSelectTextView.text =
//                "${listItemSelect.size} ${getString(R.string.multi_select_nb_contact)}"
//        } else {
//            binding.multiSelectTextView.text = String.format(
//                applicationContext.resources.getString(R.string.multi_select_nb_contact),
//                listItemSelect.size
//            )
//        }
    }

    private fun loadRecyclerView() {
        firstVipSelectionAdapter = FirstVipSelectionAdapter(this, contactsUnlimitedBought) { position ->
            firstVipSelectionAdapter.itemSelected(position)

//            contactManager?.contactList?.get(position)
//                ?.let { selectedItem(listItemSelect, it) }

            if (tooMuch && !contactsUnlimitedBought) {
                MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                    .setTitle(getString(R.string.in_app_popup_nb_vip_max_title))
                    .setMessage(getString(R.string.in_app_popup_nb_vip_max_message))
                    .setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
                        startActivity(
                            Intent(
                                this@FirstVipSelectionActivity,
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
                        "${firstVipSelectionAdapter.listContactSelect.size} ${getString(R.string.multi_select_nb_contact)}"
                } else {
                    binding.multiSelectTextView.text = String.format(
                        applicationContext.resources.getString(R.string.multi_select_nb_contact),
                        firstVipSelectionAdapter.listContactSelect.size
                    )
                }

//                val edit: SharedPreferences.Editor = numberOfContactsVIPref.edit()
//                edit.putInt("nb_Contacts_VIP", listItemSelect.size)
//                edit.apply()
            }
        }
        binding.multiSelectRecyclerView.apply {
            adapter = firstVipSelectionAdapter


            contactsViewModel.getAllContacts().observe(this@FirstVipSelectionActivity, Observer { contacts ->
                firstVipSelectionAdapter.submitList(contacts)
            })

            setHasFixedSize(true)
            layoutManager = GridLayoutManager(cxt, 4, RecyclerView.VERTICAL, false)
        }
    }

//    private fun alreadyVip() {
//        for (contact in contactManager?.contactList!!) {
//            if (contact.contactDB?.contactPriority == 2) {
//                listItemSelect.add(contact)
//                multiSelectAdapter.listContactSelect.add(contact)
//            }
//        }
//    }

//    private fun overlayAlertDialog(contactList: ArrayList<ContactWithAllInformation>): MaterialAlertDialogBuilder {
//        var message: String
//
//        if (contactList.size == 0) {
//            message =
//                applicationContext.resources.getString(R.string.multi_select_alert_dialog_0_contact)
//        } else if (contactList.size == 1) {
//            message = String.format(
//                applicationContext.resources.getString(R.string.multi_select_alert_dialog_nb_contact),
//                contactList.size,
//                getString(R.string.multi_select_contact)
//            )
//            if (contactList.size == 1) {
//                val contact = contactList[0]
//                message += "\n- " + contact.contactDB!!.firstName + " " + contact.contactDB!!.lastName
//            }
//        } else {
//            message = String.format(
//                applicationContext.resources.getString(R.string.multi_select_alert_dialog_nb_contact),
//                contactList.size,
//                getString(R.string.multi_select_contacts)
//            )
//            for (contact in contactList) {
//                message += "\n- " + contact.contactDB!!.firstName + " " + contact.contactDB!!.lastName
//            }
//        }
//
//        return MaterialAlertDialogBuilder(this, R.style.AlertDialog)
//            .setTitle("Knock In")
//            .setMessage(message + "\n" + applicationContext.resources.getString(R.string.multi_select_validate_selection))
//            .setBackground(getDrawable(R.color.backgroundColor))
//            .setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
//                setPriorityList()
//                startActivity(
//                    Intent(
//                        this@MultiSelectActivity, Main2Activity::class.java
//                    ).putExtra("fromStartActivity", true)
//                )
//                finish()
//            }
//            .setNegativeButton(R.string.alert_dialog_no) { _, _ ->
//            }
//    }

//    private fun setPriorityList() {
//        CoroutineScope(Dispatchers.IO).launch {
//            val contactList = multiSelectAdapter.listContactSelect
//            val contactManag = ContactManager(contactList, applicationContext)
//            if (contactList.isNotEmpty()) {
//                contactManag.setToContactInListPriority(2)
//            }
//        }
//    }

//    fun selectedItem(
//        listItemSelect: ArrayList<ContactWithAllInformation>,
//        contact: ContactWithAllInformation
//    ) {
//        if (listItemSelect.isEmpty() && firstClick) {
//            listItemSelect.add(contact)
//            firstClick = false
//        } else {
//            if (listItemSelect.contains(contact)) {
//                listItemSelect.remove(contact)
//                tooMuch = false
//                if (listItemSelect.isEmpty())
//                    firstClick = true
//            } else {
//                if (listItemSelect.size == 5) {
//                    tooMuch = true
//                } else {
//                    listItemSelect.add(contact)
//                }
//            }
//        }
//    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.toolbar_menu_select_vip, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_skip -> {
//                val intent = Intent(this@MultiSelectActivity, Main2Activity::class.java)
//                intent.putExtra("fromStartActivity", true)
//                startActivity(intent)
                finish()
            }
            R.id.nav_validate -> {
//                overlayAlertDialog(multiSelectAdapter.listContactSelect).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {

    }
}