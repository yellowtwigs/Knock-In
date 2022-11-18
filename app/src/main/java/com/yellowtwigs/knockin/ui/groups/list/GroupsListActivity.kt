package com.yellowtwigs.knockin.ui.groups.list

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.net.Uri
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.yellowtwigs.knockin.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.yellowtwigs.knockin.databinding.ActivityGroupsListBinding
import com.yellowtwigs.knockin.ui.HelpActivity
import com.yellowtwigs.knockin.ui.settings.ManageMyScreenActivity
import com.yellowtwigs.knockin.ui.contacts.list.ContactsListActivity
import com.yellowtwigs.knockin.ui.groups.manage_group.ManageGroupActivity
import com.yellowtwigs.knockin.ui.groups.list.section.SectionGroupsListAdapter
import com.yellowtwigs.knockin.ui.in_app.PremiumActivity
import com.yellowtwigs.knockin.ui.notifications.history.NotificationsHistoryActivity
import com.yellowtwigs.knockin.ui.notifications.settings.NotificationsSettingsActivity
import com.yellowtwigs.knockin.ui.teleworking.TeleworkingActivity
import com.yellowtwigs.knockin.utils.EveryActivityUtils
import com.yellowtwigs.knockin.utils.EveryActivityUtils.checkTheme
import com.yellowtwigs.knockin.utils.EveryActivityUtils.setupTeleworkingItem
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GroupsListActivity : AppCompatActivity() {

    private val groupsListViewModel: GroupsListViewModel by viewModels()

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
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.yellowtwigs.com/aide-en-ligne-groupes")
                )
                startActivity(browserIntent)
            } else {
                val browserIntent =
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://www.yellowtwigs.com/help-groups"))
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

            setupTeleworkingItem(binding.navView, this@GroupsListActivity)

            binding.navView.setNavigationItemSelectedListener { menuItem ->
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

    //endregion

    //region =========================================== SETUP UI ===========================================

    private fun setupBottomNavigationView(binding: ActivityGroupsListBinding) {
        binding.navigation.menu.getItem(1).isChecked = true
        binding.navigation.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_contacts -> {
                    startActivity(
                        Intent(
                            this@GroupsListActivity,
                            ContactsListActivity::class.java
                        ).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    )
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_notifcations -> {
                    startActivity(
                        Intent(
                            this@GroupsListActivity,
                            NotificationsHistoryActivity::class.java
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
        val sectionGroupsListAdapter = SectionGroupsListAdapter(this, packageManager) { id ->
            CoroutineScope(Dispatchers.Default).launch {
                groupsListViewModel.deleteGroupById(id)
            }
        }

        binding.recyclerView.apply {
            groupsListViewModel.getAllGroups()
                .observe(this@GroupsListActivity) { groups ->
                    sectionGroupsListAdapter.submitList(groups)
                }
            adapter = sectionGroupsListAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    //endregion

    //region ========================================= Functions ============================================

    fun refreshActivity() {
        startActivity(Intent(this@GroupsListActivity, GroupsListActivity::class.java))
    }

    //endregion
}