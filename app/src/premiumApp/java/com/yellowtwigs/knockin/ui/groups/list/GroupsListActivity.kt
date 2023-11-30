package com.yellowtwigs.knockin.ui.groups.list

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.net.Uri
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.yellowtwigs.knockin.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yellowtwigs.knockin.databinding.ActivityGroupsListBinding
import com.yellowtwigs.knockin.ui.HelpActivity
import com.yellowtwigs.knockin.ui.add_edit_contact.edit.PhoneNumberWithSpinner
import com.yellowtwigs.knockin.ui.cockpit.CockpitActivity
import com.yellowtwigs.knockin.ui.contacts.list.ContactsListActivity
import com.yellowtwigs.knockin.ui.settings.ManageMyScreenActivity
import com.yellowtwigs.knockin.ui.contacts.multi_channel.MultiChannelActivity
import com.yellowtwigs.knockin.ui.first_launch.start.ImportContactsViewModel
import com.yellowtwigs.knockin.ui.groups.manage_group.ManageGroupActivity
import com.yellowtwigs.knockin.ui.groups.list.section.SectionGroupsListAdapter
import com.yellowtwigs.knockin.ui.groups.list.section.SectionGroupsListAdapter.Companion.isSectionClicked
import com.yellowtwigs.knockin.ui.groups.list.section.SectionGroupsListAdapter.Companion.listOfEmails
import com.yellowtwigs.knockin.ui.groups.list.section.SectionGroupsListAdapter.Companion.listOfHasEmail
import com.yellowtwigs.knockin.ui.groups.list.section.SectionGroupsListAdapter.Companion.listOfHasSms
import com.yellowtwigs.knockin.ui.groups.list.section.SectionGroupsListAdapter.Companion.listOfHasWhatsapp
import com.yellowtwigs.knockin.ui.groups.list.section.SectionGroupsListAdapter.Companion.listOfIds
import com.yellowtwigs.knockin.ui.groups.list.section.SectionGroupsListAdapter.Companion.listOfItemSelected
import com.yellowtwigs.knockin.ui.groups.list.section.SectionGroupsListAdapter.Companion.listOfPhoneNumbers
import com.yellowtwigs.knockin.premium.PremiumActivity
import com.yellowtwigs.knockin.ui.notifications.history.NotificationsHistoryActivity
import com.yellowtwigs.knockin.ui.notifications.settings.NotificationsSettingsActivity
import com.yellowtwigs.knockin.ui.statistics.dashboard.DashboardActivity
import com.yellowtwigs.knockin.ui.teleworking.TeleworkingActivity
import com.yellowtwigs.knockin.utils.EveryActivityUtils
import com.yellowtwigs.knockin.utils.EveryActivityUtils.checkIfGoEdition
import com.yellowtwigs.knockin.utils.EveryActivityUtils.checkTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.net.URLEncoder

@AndroidEntryPoint
class GroupsListActivity : AppCompatActivity() {

    private val groupsListViewModel: GroupsListViewModel by viewModels()
    private val importContactsViewModel: ImportContactsViewModel by viewModels()
    var modeMultiSelect = false

    private lateinit var sectionGroupsListAdapter: SectionGroupsListAdapter
    private lateinit var binding: ActivityGroupsListBinding

    private var fromOtherApp = false

    companion object {
        var listOfSectionsSelected = arrayListOf<Int>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkTheme(this)
        binding = ActivityGroupsListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupDrawerLayout()
        setupBottomNavigationView()
        setupGroupsList()
        setupFloatingButtons()
        floatingButtonsClick()
    }

    private fun setupFloatingButtons() {
        binding.addNewGroup.setOnClickListener {
            val intent = Intent(this@GroupsListActivity, ManageGroupActivity::class.java)
            restartActivity()
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        if (fromOtherApp) {
            refreshActivity()
        }
    }

    //region =========================================== TOOLBAR ============================================

    private fun setupToolbar() {
        binding.help.setOnClickListener {
            if (Resources.getSystem().configuration.locale.language == "fr") {
                val browserIntent = Intent(
                    Intent.ACTION_VIEW, Uri.parse("https://www.yellowtwigs.com/aide-en-ligne-groupes")
                )
                startActivity(browserIntent)
            } else {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.yellowtwigs.com/help-groups"))
                startActivity(browserIntent)
            }
        }
    }

    //endregion

    //region ======================================== DRAWER LAYOUT =========================================

    private fun setupDrawerLayout() {
        binding.drawerLayout.apply {
            val menu = binding.navView.menu
            menu.findItem(R.id.nav_home).isChecked = true

            if (checkIfGoEdition(this@GroupsListActivity)) {
                menu.findItem(R.id.nav_in_app).isVisible = false
                menu.findItem(R.id.nav_notifications).isVisible = false
                menu.findItem(R.id.nav_teleworking).isVisible = false
                menu.findItem(R.id.nav_dashboard).isVisible = false
            }

            binding.navView.setNavigationItemSelectedListener { menuItem ->
                if (menuItem.itemId != R.id.nav_sync_contact && menuItem.itemId != R.id.nav_invite_friend) {
                    menuItem.isChecked = true
                }
                closeDrawers()
                when (menuItem.itemId) {
                    R.id.nav_notifications -> {
                        startActivity(
                            Intent(this@GroupsListActivity, NotificationsSettingsActivity::class.java)
                        )
                        restartActivity()
                    }
                    R.id.nav_teleworking -> startActivity(
                        Intent(this@GroupsListActivity, TeleworkingActivity::class.java)
                    )
                    R.id.nav_dashboard -> startActivity(
                        Intent(this@GroupsListActivity, DashboardActivity::class.java)
                    )
                    R.id.nav_in_app -> {
                        startActivity(
                            Intent(this@GroupsListActivity, PremiumActivity::class.java)
                        )
                        restartActivity()
                    }
                    R.id.nav_manage_screen -> {
                        startActivity(
                            Intent(this@GroupsListActivity, ManageMyScreenActivity::class.java)
                        )
                        restartActivity()
                    }
                    R.id.nav_help -> {
                        startActivity(
                            Intent(this@GroupsListActivity, HelpActivity::class.java)
                        )
                        restartActivity()
                    }
                    R.id.nav_sync_contact -> {
                        importContacts()
                        restartActivity()
                    }
                    R.id.nav_invite_friend -> {
                        val intent = Intent(Intent.ACTION_SEND)
                        val messageString = resources.getString(R.string.invite_friend_text) + " \n" + resources.getString(
                            R.string.location_on_playstore
                        )
                        intent.putExtra(Intent.EXTRA_TEXT, messageString)
                        intent.type = "text/plain"
                        val messageIntent = Intent.createChooser(intent, null)
                        startActivity(messageIntent)
                        restartActivity()
                    }
                }

                true
            }

            binding.openDrawer.setOnClickListener {
                if (isOpen) {
                    closeDrawer(GravityCompat.START)
                } else {
                    openDrawer(GravityCompat.START)
                }
            }
        }
    }

    private fun importContacts() {
        CoroutineScope(Dispatchers.Main).launch {
            importContactsViewModel.syncAllContactsInDatabase(contentResolver)
        }
    }

    //endregion

    //region =========================================== SETUP UI ===========================================

    private fun setupBottomNavigationView() {
        binding.navigation.menu.getItem(1).isChecked = true
        if (checkIfGoEdition(this@GroupsListActivity)) {
            binding.navigation.menu.getItem(2).isVisible = false
        }
        binding.navigation.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_contacts -> {
                    startActivity(
                        Intent(
                            this@GroupsListActivity, ContactsListActivity::class.java
                        ).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    )
                    restartActivity()
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_notifcations -> {
                    startActivity(
                        Intent(
                            this@GroupsListActivity, NotificationsHistoryActivity::class.java
                        ).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    )
                    restartActivity()
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_cockpit -> {
                    startActivity(
                        Intent(
                            this@GroupsListActivity, CockpitActivity::class.java
                        ).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    )
                    restartActivity()
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        })
    }

    private fun setupGroupsList() {
        sectionGroupsListAdapter = SectionGroupsListAdapter(this, { id ->
            CoroutineScope(Dispatchers.Default).launch {
                groupsListViewModel.deleteGroupById(id)
            }
        }, { id ->
            setupFloatingButtonVisibility()
        })

        binding.recyclerView.apply {
            groupsListViewModel.getAllGroups().observe(this@GroupsListActivity) { groups ->
                sectionGroupsListAdapter.submitList(groups)
            }
            adapter = sectionGroupsListAdapter
            layoutManager = LinearLayoutManager(context)
            setItemViewCacheSize(500)
        }
    }

    //endregion

    //region ========================================= MULTI SELECT =========================================

    private fun setupFloatingButtonVisibility() {
        binding.apply {
            smsButton.isVisible = listOfItemSelected.isNotEmpty() && !listOfHasSms.contains(false) && listOfHasSms.isNotEmpty()
            gmailButton.isVisible = listOfItemSelected.isNotEmpty() && !listOfHasEmail.contains(false) && listOfHasEmail.isNotEmpty()
            whatsappButton.isVisible =
                listOfItemSelected.isNotEmpty() && !listOfHasWhatsapp.contains(false) && listOfHasWhatsapp.isNotEmpty()
            buttonMultichannel.isVisible = listOfItemSelected.isNotEmpty()
            modeMultiSelect = listOfItemSelected.isNotEmpty()
        }
    }

    private fun floatingButtonsClick() {
        binding.apply {
            buttonMultichannel.setOnClickListener {
                startActivity(
                    Intent(
                        this@GroupsListActivity, MultiChannelActivity::class.java
                    ).putIntegerArrayListExtra(
                        "contacts", listOfIds
                    )
                )
                fromOtherApp = false
            }

            whatsappButton.setOnClickListener {
                multiChannelSendMessageWhatsapp()
                fromOtherApp = true
            }
            smsButton.setOnClickListener {
                if (listOfPhoneNumbers.isNotEmpty()) {
                    monoChannelSmsClick(listOfPhoneNumbers)
                    fromOtherApp = true
                }
            }
            gmailButton.setOnClickListener {
                monoChannelMailClick(listOfEmails)
                fromOtherApp = true
            }
        }
    }

    private fun monoChannelSmsClick(listOfPhoneNumber: ArrayList<PhoneNumberWithSpinner>) {
        var message = "smsto:" + listOfPhoneNumber[0].phoneNumber
        for (i in 0 until listOfPhoneNumber.size) {
            message += ";" + listOfPhoneNumber[i].phoneNumber
        }
        startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse(message)))
    }

    private fun monoChannelMailClick(listOfMail: ArrayList<String>) {
        val contact = listOfMail.toArray(arrayOfNulls<String>(listOfMail.size))
        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(
            Intent.EXTRA_EMAIL, contact
        )
        intent.data = Uri.parse("mailto:")
        intent.type = "message/rfc822"
        intent.putExtra(Intent.EXTRA_SUBJECT, "")
        intent.putExtra(Intent.EXTRA_TEXT, "")
        startActivity(intent)
    }

    private fun multiChannelSendMessageWhatsapp() {
        val i = Intent(Intent.ACTION_VIEW)

        try {
            val url = "https://api.whatsapp.com/send?text=" + URLEncoder.encode(".", "UTF-8")
            i.setPackage("com.whatsapp")
            i.data = Uri.parse(url)
            if (i.resolveActivity(packageManager) != null) {
                startActivity(i)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //endregion

    //region ========================================= FUNCTIONS ============================================

    private fun restartActivity() {
        listOfItemSelected.clear()
        listOfIds.clear()
        listOfHasSms.clear()
        listOfHasEmail.clear()
        listOfHasWhatsapp.clear()
        listOfSectionsSelected.clear()
        isSectionClicked = false
        setupFloatingButtonVisibility()
    }

    fun refreshActivity() {
        startActivity(Intent(this@GroupsListActivity, GroupsListActivity::class.java))
        restartActivity()
        finish()
    }

    //endregion
}