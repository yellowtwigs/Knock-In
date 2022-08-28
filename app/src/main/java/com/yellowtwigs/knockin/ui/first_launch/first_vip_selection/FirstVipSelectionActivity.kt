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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yellowtwigs.knockin.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yellowtwigs.knockin.databinding.ActivityFirstVipSelectionBinding
import com.yellowtwigs.knockin.ui.in_app.PremiumActivity
import com.yellowtwigs.knockin.ui.contacts.list.ContactsListActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FirstVipSelectionActivity : AppCompatActivity() {

    //region ========================================= Var or Val ===========================================

    private val cxt = this
    private lateinit var firstVipSelectionAdapter: FirstVipSelectionAdapter

    private lateinit var numberOfContactsVIPref: SharedPreferences
    private var contactsUnlimitedBought = false

    private var firstClick = true
    private var tooMuch = false

    private var listOfItemSelected = arrayListOf<Int>()
    private val viewModel: FirstVipSelectionViewModel by viewModels()

    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityFirstVipSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //region ===================================== SharedPreferences ====================================

        numberOfContactsVIPref = getSharedPreferences("nb_Contacts_VIP", Context.MODE_PRIVATE)

        contactsUnlimitedBought = getSharedPreferences(
            "Contacts_Unlimited_Bought",
            Context.MODE_PRIVATE
        ).getBoolean("Contacts_Unlimited_Bought", false)

        contactsUnlimitedBought = false


        //endregion

        setupToolbar(binding)
        setupRecyclerView(binding)

//        alreadyVip()

        binding.multiSelectTextView.text = String.format(
            applicationContext.resources.getString(R.string.multi_select_nb_contact),
            listOfItemSelected.size
        )

        if (Resources.getSystem().configuration.locale.language == "ar") {
            binding.multiSelectTextView.text =
                "${listOfItemSelected.size} ${getString(R.string.multi_select_nb_contact)}"
        } else {
            binding.multiSelectTextView.text = String.format(
                applicationContext.resources.getString(R.string.multi_select_nb_contact),
                listOfItemSelected.size
            )
        }
    }

    //region =========================================== TOOLBAR ============================================

    private fun setupToolbar(binding: ActivityFirstVipSelectionBinding) {
        setSupportActionBar(binding.toolbar)
        val actionbar = supportActionBar
        actionbar?.setDisplayHomeAsUpEnabled(false)
        actionbar?.title = getString(R.string.multi_select_title)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.toolbar_menu_select_vip, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_skip -> {
                val intent = Intent(this@FirstVipSelectionActivity, ContactsListActivity::class.java)
                intent.putExtra("fromStartActivity", true)
                startActivity(intent)
                finish()
            }
            R.id.nav_validate -> {
                setPriorityList()
                startActivity(
                    Intent(
                        this@FirstVipSelectionActivity, ContactsListActivity::class.java
                    ).putExtra("fromStartActivity", true)
                )
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //endregion

    //region =========================================== SETUP UI ===========================================

    private fun setupRecyclerView(binding: ActivityFirstVipSelectionBinding) {
        firstVipSelectionAdapter =
            FirstVipSelectionAdapter(this, listOfItemSelected, contactsUnlimitedBought) { id ->
                if (listOfItemSelected.isEmpty() && firstClick) {
                    listOfItemSelected.add(id)
                    firstClick = false
                } else {
                    if (listOfItemSelected.contains(id)) {
                        listOfItemSelected.remove(id)
                        tooMuch = false
                        if (listOfItemSelected.isEmpty())
                            firstClick = true
                    } else {
                        if (listOfItemSelected.size == 5) {
                            tooMuch = true
                        } else {
                            listOfItemSelected.add(id)
                        }
                    }
                }

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
                            "${listOfItemSelected.size} ${getString(R.string.multi_select_nb_contact)}"
                    } else {
                        binding.multiSelectTextView.text = String.format(
                            applicationContext.resources.getString(R.string.multi_select_nb_contact),
                            listOfItemSelected.size
                        )
                    }

//                val edit: SharedPreferences.Editor = numberOfContactsVIPref.edit()
//                edit.putInt("nb_Contacts_VIP", listItemSelect.size)
//                edit.apply()
                }
            }
        binding.multiSelectRecyclerView.apply {
            adapter = firstVipSelectionAdapter

            viewModel.getAllContacts().observe(this@FirstVipSelectionActivity) { contacts ->
                firstVipSelectionAdapter.submitList(contacts)
            }

            setHasFixedSize(true)
            layoutManager = GridLayoutManager(cxt, 4, RecyclerView.VERTICAL, false)
            setItemViewCacheSize(500)
        }
    }

    //endregion

//    private fun alreadyVip() {
//        for (contact in contactManager?.contactList!!) {
//            if (contact.contactDB?.contactPriority == 2) {
//                listItemSelect.add(contact)
//                multiSelectAdapter.listContactSelect.add(contact)
//            }
//        }
//    }

    private fun setPriorityList() {
        CoroutineScope(Dispatchers.IO).launch {
            viewModel.updateContact(listOfItemSelected)
        }
    }

    override fun onBackPressed() {

    }
}