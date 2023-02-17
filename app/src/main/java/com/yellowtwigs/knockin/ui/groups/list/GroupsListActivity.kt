package com.yellowtwigs.knockin.ui.groups.list

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.net.Uri
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.yellowtwigs.knockin.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.yellowtwigs.knockin.databinding.ActivityContactsListBinding
import com.yellowtwigs.knockin.databinding.ActivityGroupsListBinding
import com.yellowtwigs.knockin.ui.HelpActivity
import com.yellowtwigs.knockin.ui.contacts.list.ContactsListActivity
import com.yellowtwigs.knockin.ui.settings.ManageMyScreenActivity
import com.yellowtwigs.knockin.ui.contacts.multi_channel.MultiChannelActivity
import com.yellowtwigs.knockin.ui.first_launch.start.ImportContactsViewModel
import com.yellowtwigs.knockin.ui.groups.manage_group.ManageGroupActivity
import com.yellowtwigs.knockin.ui.groups.list.section.SectionGroupsListAdapter
import com.yellowtwigs.knockin.ui.groups.list.section.SectionGroupsListAdapter.Companion.listOfEmails
import com.yellowtwigs.knockin.ui.groups.list.section.SectionGroupsListAdapter.Companion.listOfHasEmail
import com.yellowtwigs.knockin.ui.groups.list.section.SectionGroupsListAdapter.Companion.listOfHasSms
import com.yellowtwigs.knockin.ui.groups.list.section.SectionGroupsListAdapter.Companion.listOfHasWhatsapp
import com.yellowtwigs.knockin.ui.groups.list.section.SectionGroupsListAdapter.Companion.listOfIds
import com.yellowtwigs.knockin.ui.groups.list.section.SectionGroupsListAdapter.Companion.listOfPhoneNumbers
import com.yellowtwigs.knockin.ui.premium.PremiumActivity
import com.yellowtwigs.knockin.ui.notifications.history.NotificationsHistoryActivity
import com.yellowtwigs.knockin.ui.notifications.settings.NotificationsSettingsActivity
import com.yellowtwigs.knockin.utils.EveryActivityUtils.checkTheme
import com.yellowtwigs.knockin.utils.EveryActivityUtils.setupTeleworkingItem
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URLEncoder

@AndroidEntryPoint
class GroupsListActivity : AppCompatActivity() {

    private val groupsListViewModel: GroupsListViewModel by viewModels()
    private val importContactsViewModel: ImportContactsViewModel by viewModels()
    var modeMultiSelect = false

    private lateinit var sectionGroupsListAdapter: SectionGroupsListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkTheme(this)

        val binding = ActivityGroupsListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar(binding)
        setupDrawerLayout(binding)
        setupBottomNavigationView(binding)
        setupGroupsList(binding)
        setupFloatingButtons(binding)
        floatingButtonsClick(binding)
        setupFloatingButtonVisibility(binding)
    }

    private fun setupFloatingButtons(binding: ActivityGroupsListBinding) {
        binding.addNewGroup.setOnClickListener {
            val intent = Intent(this@GroupsListActivity, ManageGroupActivity::class.java)
            startActivity(intent)
        }
    }

    //region =========================================== TOOLBAR ============================================

    private fun setupToolbar(binding: ActivityGroupsListBinding) {
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

    private fun setupDrawerLayout(binding: ActivityGroupsListBinding) {
        binding.drawerLayout.apply {
            val menu = binding.navView.menu
            val navItem = menu.findItem(R.id.nav_home)
            navItem.isChecked = true
            menu.getItem(0).isChecked = true

            setupTeleworkingItem(binding.drawerLayout, this@GroupsListActivity)

            binding.navView.setNavigationItemSelectedListener { menuItem ->
                if (menuItem.itemId != R.id.nav_sync_contact && menuItem.itemId != R.id.nav_invite_friend) {
                    menuItem.isChecked = true
                }
                closeDrawers()

                when (menuItem.itemId) {
                    R.id.nav_notifications -> startActivity(
                        Intent(this@GroupsListActivity, NotificationsSettingsActivity::class.java)
                    )
                    R.id.nav_in_app -> startActivity(
                        Intent(this@GroupsListActivity, PremiumActivity::class.java)
                    )
                    R.id.nav_manage_screen -> startActivity(
                        Intent(this@GroupsListActivity, ManageMyScreenActivity::class.java)
                    )
                    R.id.nav_help -> startActivity(
                        Intent(this@GroupsListActivity, HelpActivity::class.java)
                    )
                    R.id.nav_sync_contact -> {
                        importContacts()
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
        CoroutineScope(Dispatchers.Default).launch {
            importContactsViewModel.syncAllContactsInDatabase(contentResolver)
        }
    }

    //endregion

    //region =========================================== SETUP UI ===========================================

    private fun setupBottomNavigationView(binding: ActivityGroupsListBinding) {
        binding.navigation.menu.getItem(1).isChecked = true
        binding.navigation.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_contacts -> {
                    startActivity(
                        Intent(
                            this@GroupsListActivity, ContactsListActivity::class.java
                        ).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    )
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_notifcations -> {
                    startActivity(
                        Intent(
                            this@GroupsListActivity, NotificationsHistoryActivity::class.java
                        ).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    )
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_cockpit -> {
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        })
    }

    private fun setupGroupsList(binding: ActivityGroupsListBinding) {
        sectionGroupsListAdapter = SectionGroupsListAdapter(this, { id ->
            CoroutineScope(Dispatchers.Default).launch {
                groupsListViewModel.deleteGroupById(id)
            }
        }, { id ->
            setupFloatingButtonVisibility(binding)
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

    private fun setupFloatingButtonVisibility(binding: ActivityGroupsListBinding) {
        binding.apply {
            smsButton.isVisible = listOfIds.isNotEmpty() && !listOfHasSms.contains(false)
            gmailButton.isVisible = listOfIds.isNotEmpty() && !listOfHasEmail.contains(false)
            whatsappButton.isVisible = listOfIds.isNotEmpty() && !listOfHasWhatsapp.contains(false)
            buttonMultichannel.isVisible = listOfIds.isNotEmpty()
        }
    }

    private fun floatingButtonsClick(binding: ActivityGroupsListBinding) {
        binding.apply {
            buttonMultichannel.setOnClickListener {
                startActivity(
                    Intent(
                        this@GroupsListActivity, MultiChannelActivity::class.java
                    ).putIntegerArrayListExtra(
                        "contacts", listOfIds
                    )
                )
                finish()
            }

            whatsappButton.setOnClickListener {
                multiChannelSendMessageWhatsapp()
            }
            smsButton.setOnClickListener {
                monoChannelSmsClick(listOfPhoneNumbers)
            }
            gmailButton.setOnClickListener {
                monoChannelMailClick(listOfEmails)
            }
        }
    }

    private fun monoChannelSmsClick(listOfPhoneNumber: ArrayList<String>) {
        var message = "smsto:" + listOfPhoneNumber[0]
        for (i in 0 until listOfPhoneNumber.size) {
            message += ";" + listOfPhoneNumber[i]
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

    fun refreshActivity() {
        startActivity(Intent(this@GroupsListActivity, GroupsListActivity::class.java))
    }

    //endregion
}