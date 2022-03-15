package com.yellowtwigs.knockin.ui.groups

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.model.data.ContactDB
import com.yellowtwigs.knockin.model.data.ContactWithAllInformation
import com.yellowtwigs.knockin.model.data.LinkContactGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yellowtwigs.knockin.databinding.ActivityAddContactToGroupBinding
import com.yellowtwigs.knockin.model.data.GroupWithContact
import com.yellowtwigs.knockin.ui.edit_contact.ContactDetailsViewModel
import com.yellowtwigs.knockin.utils.EveryActivityUtils.checkThemePreferences
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Activité qui nous permet d'ajouter des contacts a un groupe précis
 * @author Ryan Granet
 */
@AndroidEntryPoint
class AddContactToGroupActivity : AppCompatActivity() {

    private lateinit var addContactToGroupAdapter: AddContactToGroupAdapter

    val selectContact: MutableList<ContactDB> = mutableListOf()
    private var listOfItemSelected: ArrayList<ContactWithAllInformation> = ArrayList()

    private var groupId: Int = 0

    private lateinit var binding: ActivityAddContactToGroupBinding

    private val groupsViewModel: GroupsViewModel by viewModels()
    private val contactDetailsViewModel: ContactDetailsViewModel by viewModels()
    private val linkContactGroupViewModel: LinkContactGroupViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkThemePreferences(this)
        binding = ActivityAddContactToGroupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //region ========================================= Toolbar ==========================================

        setSupportActionBar(binding.toolbar)
        val actionbar = supportActionBar
        actionbar?.setDisplayHomeAsUpEnabled(true)
        actionbar?.setHomeAsUpIndicator(R.drawable.ic_close)
        actionbar?.title = getString(R.string.add_contact_to_group_toolbar_title)

        //endregion

        initRecyclerView()
    }

    private fun initRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@AddContactToGroupActivity)
            addContactToGroupAdapter = AddContactToGroupAdapter(this@AddContactToGroupActivity)
            getGroupFromViewModel()
            adapter = addContactToGroupAdapter
        }
    }

    private fun getGroupFromViewModel() {
        groupsViewModel.groupWithContactLiveData.observe(this) { groupWithContact ->
            addContactToGroupAdapter.submitList(groupWithContact.getListContact(this))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.toolbar_menu_validation, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                refreshActivity()
            }
            R.id.nav_validate -> {
                if (selectContact.isEmpty()) {
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

    private fun addToGroup(listContact: List<ContactDB>, groupId: Int) {
        groupsViewModel.getGroupById(groupId).observe(this) { groupDb ->
            if (groupDb.name == "Favorites" || groupDb.name == "Favoris") {
                addToFavorite()
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            listContact.forEach {
                linkContactGroupViewModel.insert(LinkContactGroup(groupId, it.id!!))
            }
        }
    }

    private fun addToFavorite() {
//        var counter = 0
//
//        while (counter < addContactToGroupAdapter.allSelectContact.size) {
//            val contact = contactsDatabase?.contactsDao()
//                ?.getContact(addContactToGroupAdapter.allSelectContact[counter].id!!)
//            contact!!.setIsFavorite(contactsDatabase)
//
//            counter++
//        }
    }

    /**
     *récupère tous les contact qui ne sont pas dans le groupe
     * @param groupId [Int] //id du groupe dont on veut ajouté des contact
     * @return [List<ContactWithAllInformation>]
     */
//    private fun getContactNotInGroup(groupId: Int): List<ContactWithAllInformation> {
//        val allInGroup = mutableListOf<ContactWithAllInformation>()
//        val groupMember = contactsDatabase!!.contactsDao().getContactForGroup(groupId)
//        val allContact = contactsDatabase!!.contactsDao().sortContactByFirstNameAZ()
//        allContact.forEach { all ->
//            groupMember.forEach {
//                if (all.contactDB!!.id == it.contactDB!!.id) {
//                    allInGroup.add(all)
//                }
//            }
//        }
//        return allContact.minus(allInGroup)
//    }

    /**
     * Retour vers l'activité groupManager
     */
    private fun refreshActivity() {
        startActivity(
            Intent(
                this@AddContactToGroupActivity,
                GroupManagerActivity::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        )
        finish()
    }
}