package com.yellowtwigs.knockin.ui.contacts.list

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.ActivityContactsListBinding
import com.yellowtwigs.knockin.ui.HelpActivity
import com.yellowtwigs.knockin.ui.in_app.PremiumActivity
import com.yellowtwigs.knockin.ui.notifications.history.NotificationsHistoryActivity
import com.yellowtwigs.knockin.ui.notifications.history.NotificationsListFragment
import com.yellowtwigs.knockin.ui.settings.ManageMyScreenActivity
import com.yellowtwigs.knockin.ui.notifications.settings.NotificationsSettingsActivity
import com.yellowtwigs.knockin.ui.teleworking.TeleworkingActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ContactsListActivity : AppCompatActivity() {

    private val contactsListViewModel: ContactsListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //region ======================================== Theme Dark ========================================

        val sharedThemePreferences = getSharedPreferences("Knockin_Theme", Context.MODE_PRIVATE)
        if (sharedThemePreferences.getBoolean("darkTheme", false)) {
            setTheme(R.style.AppThemeDark)
        } else {
            setTheme(R.style.AppTheme)
        }

        //endregion

        val binding = ActivityContactsListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar(binding)
        setupRecyclerView(binding)

        setupBottomNavigationView(binding)
        setupDrawerLayout(binding)
    }

    //region =========================================== TOOLBAR ============================================

    private fun setupToolbar(binding: ActivityContactsListBinding) {
        setSupportActionBar(binding.toolbarMenu)
        val actionbar = supportActionBar
        actionbar?.title = ""

        binding.toolbarSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(query: CharSequence?, p1: Int, p2: Int, p3: Int) {
                contactsListViewModel.setSearchTextChanged(query.toString())
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.contacts_toolbar_menu, menu)

        val sortByPreferences = getSharedPreferences("Sort_By_Preferences", Context.MODE_PRIVATE)

        when (sortByPreferences.getInt("Sort_By_Preferences", R.id.sort_by_priority)) {
            R.id.sort_by_first_name -> {
                menu.findItem(R.id.sort_by_first_name).isChecked = true
                contactsListViewModel.setSortedBy(R.id.sort_by_first_name)
            }
            R.id.sort_by_last_name -> {
                menu.findItem(R.id.sort_by_last_name).isChecked = true
                contactsListViewModel.setSortedBy(R.id.sort_by_last_name)
            }
            R.id.sort_by_priority -> {
                menu.findItem(R.id.sort_by_priority).isChecked = true
                contactsListViewModel.setSortedBy(R.id.sort_by_priority)
            }
            R.id.sort_by_favorite -> {
                menu.findItem(R.id.sort_by_favorite).isChecked = true
                contactsListViewModel.setSortedBy(R.id.sort_by_favorite)
            }
            else -> {
                menu.findItem(R.id.sort_by_priority).isChecked = true
                contactsListViewModel.setSortedBy(R.id.sort_by_priority)
            }
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val sortByPreferences = getSharedPreferences("Sort_By_Preferences", Context.MODE_PRIVATE)

        when (item.itemId) {
            R.id.sort_by_first_name -> {
                val editSortBy = sortByPreferences.edit()
                editSortBy.putInt("Sort_By_Preferences", R.id.sort_by_first_name)
                editSortBy.apply()

                item.isChecked = !item.isChecked
                contactsListViewModel.setSortedBy(R.id.sort_by_first_name)
                return true
            }
            R.id.sort_by_last_name -> {
                val editSortBy = sortByPreferences.edit()
                editSortBy.putInt("Sort_By_Preferences", R.id.sort_by_last_name)
                editSortBy.apply()
                item.isChecked = !item.isChecked
                contactsListViewModel.setSortedBy(R.id.sort_by_last_name)
                return true
            }
            R.id.sort_by_priority -> {
                val editSortBy = sortByPreferences.edit()
                editSortBy.putInt("Sort_By_Preferences", R.id.sort_by_priority)
                editSortBy.apply()
                item.isChecked = !item.isChecked
                contactsListViewModel.setSortedBy(R.id.sort_by_priority)
                return true
            }
            R.id.sort_by_favorite -> {
                val editSortBy = sortByPreferences.edit()
                editSortBy.putInt("Sort_By_Preferences", R.id.sort_by_favorite)
                editSortBy.apply()
                item.isChecked = !item.isChecked
                contactsListViewModel.setSortedBy(R.id.sort_by_favorite)
                return true
            }
            R.id.sms_filter -> {
                item.isChecked = !item.isChecked
                contactsListViewModel.setFilterBy(R.id.sms_filter)
                return true
            }
            R.id.mail_filter -> {
                item.isChecked = !item.isChecked
                contactsListViewModel.setFilterBy(R.id.mail_filter)
                return true
            }
            R.id.whatsapp_filter -> {
                item.isChecked = !item.isChecked
                contactsListViewModel.setFilterBy(R.id.whatsapp_filter)
                return true
            }
            R.id.messenger_filter -> {
                item.isChecked = !item.isChecked
                contactsListViewModel.setFilterBy(R.id.messenger_filter)
                return true
            }
            R.id.signal_filter -> {
                item.isChecked = !item.isChecked
                contactsListViewModel.setFilterBy(R.id.signal_filter)
                return true
            }
            R.id.telegram_filter -> {
                item.isChecked = !item.isChecked
                contactsListViewModel.setFilterBy(R.id.telegram_filter)
                return true
            }
            else -> {
            }
        }

        return super.onOptionsItemSelected(item)
    }

    //endregion

    //region ======================================== DRAWER LAYOUT =========================================

    private fun setupDrawerLayout(binding: ActivityContactsListBinding) {
        binding.drawerLayout.apply {
            val menu = binding.navView.menu
            val navItem = menu.findItem(R.id.nav_home)
            navItem.isChecked = true
            menu.getItem(0).isChecked = true

            binding.navView.setNavigationItemSelectedListener { menuItem ->
                hideKeyboard()
                closeDrawers()
                when (menuItem.itemId) {
                    R.id.nav_home -> {}
                    R.id.nav_notifications -> startActivity(
                        Intent(this@ContactsListActivity, NotificationsSettingsActivity::class.java)
                    )
//                    R.id.nav_messenger -> startActivity(
//                        Intent(this@MainActivity, Mess::class.java)
//                    )
                    R.id.navigation_teleworking -> startActivity(
                        Intent(this@ContactsListActivity, TeleworkingActivity::class.java)
                    )
                    R.id.nav_in_app -> startActivity(
                        Intent(this@ContactsListActivity, PremiumActivity::class.java)
                    )
                    R.id.nav_manage_screen -> startActivity(
                        Intent(this@ContactsListActivity, ManageMyScreenActivity::class.java)
                    )
                    R.id.nav_help -> startActivity(
                        Intent(this@ContactsListActivity, HelpActivity::class.java)
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

    private fun setupRecyclerView(binding: ActivityContactsListBinding) {
        val contactsListAdapter = ContactsListAdapter(this) { id ->
            hideKeyboard()
        }

        binding.recyclerView.apply {
            contactsListViewModel.getAllContacts().observe(this@ContactsListActivity) { contacts ->
//                CoroutineScope(Dispatchers.).launch {
                contactsListAdapter.submitList(contacts)
//                }
                smoothScrollToPosition(0)
            }
            adapter = contactsListAdapter
            layoutManager = LinearLayoutManager(context)
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    contactsListAdapter.setIsScrolling(true)
                    super.onScrollStateChanged(recyclerView, newState)
                }
            })
        }
    }

    private fun setupBottomNavigationView(binding: ActivityContactsListBinding) {
        binding.bottomNavigation.menu.getItem(0).isChecked = true
        binding.bottomNavigation.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_groups -> {
//                    startActivity(
//                        Intent(
//                            this@ContactsListActivity,
//                            GroupManagerActivity::class.java
//                        ).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
//                    )
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_notifcations -> {
                    startActivity(
                        Intent(
                            this@ContactsListActivity,
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

    //endregion

    private fun hideKeyboard() {
        val view = this.currentFocus
        view?.let { v ->
            val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }
}