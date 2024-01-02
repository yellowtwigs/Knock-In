package com.yellowtwigs.knockin.ui.notifications.history

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.ActivityNotificationsHistoryBinding
import com.yellowtwigs.knockin.model.database.data.NotificationDB
import com.yellowtwigs.knockin.repositories.firebase.FirebaseViewModel
import com.yellowtwigs.knockin.ui.HelpActivity
import com.yellowtwigs.knockin.ui.cockpit.CockpitActivity
import com.yellowtwigs.knockin.ui.contacts.list.ContactsListActivity
import com.yellowtwigs.knockin.ui.first_launch.start.ImportContactsViewModel
import com.yellowtwigs.knockin.ui.groups.list.GroupsListActivity
import com.yellowtwigs.knockin.ui.notifications.settings.NotificationsSettingsActivity
import com.yellowtwigs.knockin.ui.settings.ManageMyScreenActivity
import com.yellowtwigs.knockin.ui.statistics.dashboard.DashboardActivity
import com.yellowtwigs.knockin.ui.teleworking.TeleworkingActivity
import com.yellowtwigs.knockin.utils.EveryActivityUtils
import com.yellowtwigs.knockin.utils.EveryActivityUtils.checkTheme
import com.yellowtwigs.knockin.utils.NotificationsGesture.convertPackageNameToGoToWithContact
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NotificationsHistoryActivity : AppCompatActivity() {

    companion object {
        var currentPosition = 1
    }

    //region ========================================== Val or Var ==========================================

    private var fromPopup: Boolean = false

    private lateinit var notificationsAdapter: NotificationsListAdapter
    private lateinit var binding: ActivityNotificationsHistoryBinding
    private lateinit var sharedPreferences: SharedPreferences

    private val notificationsListViewModel: NotificationsListViewModel by viewModels()
    private val importContactsViewModel: ImportContactsViewModel by viewModels()

    private var multiSelectMode = false
    private var firstClick = true
    private val listOfIdsSelected = arrayListOf<Int>()

    private val currentList = arrayListOf<NotificationsListViewState>()
    private val duplicates = arrayListOf<NotificationsListViewState>()

    //endregion

    private lateinit var importContactPreferences: SharedPreferences

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions[Manifest.permission.WRITE_CONTACTS] == true && permissions[Manifest.permission.READ_CONTACTS] == true) {
            CoroutineScope(Dispatchers.Main).launch {
                importContactsViewModel.syncAllContactsInDatabase(contentResolver)
            }

            importContactPreferences = getSharedPreferences("Import_Contact", Context.MODE_PRIVATE)
            val edit = importContactPreferences.edit()
            edit.putBoolean("Import_Contact", true)
            edit.apply()
        }
    }

    override fun onStop() {
        super.onStop()
        currentPosition = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkTheme(this)

        binding = ActivityNotificationsHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) {
//            hideKeyboard(this)
//            binding.toolbarSearch.isEnabled = false
//            binding.toolbarSearch.setOnClickListener {
//                binding.toolbarSearch.requestFocus()
//                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//                imm.showSoftInput(binding.toolbarSearch, InputMethodManager.SHOW_IMPLICIT)
//            }
//        }

        sharedPreferences = getSharedPreferences("Notification_tri", Context.MODE_PRIVATE)

        setupToolbar()
        setupDrawerLayout()
        setupBottomNavigationView()
        setupNotificationsList()

        //region ============================================================ LISTENERS  ============================================================

        binding.apply {
            floatingActionButton.setOnClickListener {
                MaterialAlertDialogBuilder(
                    this@NotificationsHistoryActivity, R.style.AlertDialog
                ).setTitle(getString(R.string.notification_history_alert_dialog_title))
                    .setMessage(getString(R.string.notification_history_alert_dialog_text))
                    .setPositiveButton(R.string.notification_history_alert_dialog_delete_button) { _, _ -> deleteAllNotifications() }
                    .setNegativeButton(
                        R.string.notification_history_alert_dialog_cancel_button, null
                    )
                    .setNeutralButton(R.string.notification_history_alert_dialog_delete_system_button) { _, _ -> deleteAllNotificationsSystem() }
                    .show()
            }
            deleteNotifications.setOnClickListener {
                Log.i("DeleteNotification", "listOfIdsSelected : $listOfIdsSelected")
                deleteListOfSelectedNotifications()
            }
        }

        //endregion
    }

    //region ==================================================================== TOOLBAR =====================================================================

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarMenu)
        val actionbar = supportActionBar
        actionbar?.title = ""

        binding.toolbarSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(query: CharSequence?, p1: Int, p2: Int, p3: Int) {
                notificationsListViewModel.setSearchTextChanged(query.toString())
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })
        binding.openDrawer.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.history_toolbar_menu, menu)

        val sortByPreferences = getSharedPreferences("Notifications_Sort_By", Context.MODE_PRIVATE)
        val filterByPreferences = getSharedPreferences("Notifications_Filter_By", Context.MODE_PRIVATE)

        when (sortByPreferences.getInt("Notifications_Sort_By", R.id.sort_by_date)) {
            R.id.sort_by_date -> {
                menu.findItem(R.id.sort_by_date)?.isChecked = true
                notificationsListViewModel.setSortedBy(R.id.sort_by_date)
            }

            R.id.sort_by_contact -> {
                menu.findItem(R.id.sort_by_contact)?.isChecked = true
                notificationsListViewModel.setSortedBy(R.id.sort_by_contact)
            }

            R.id.notifications_sort_by_priority -> {
                menu.findItem(R.id.notifications_sort_by_priority)?.isChecked = true
                notificationsListViewModel.setSortedBy(R.id.notifications_sort_by_priority)
            }

            else -> {
                menu.findItem(R.id.sort_by_date)?.isChecked = true
                notificationsListViewModel.setSortedBy(R.id.sort_by_date)
            }
        }

        when (filterByPreferences.getInt("Notifications_Filter_By", R.id.filter_by_msg_apps)) {
            R.id.filter_by_all -> {
                menu.findItem(R.id.filter_by_all)?.isChecked = true
                notificationsListViewModel.setFilterBy(R.id.filter_by_all)
            }

            R.id.filter_by_msg_apps -> {
                menu.findItem(R.id.filter_by_msg_apps)?.isChecked = true
                notificationsListViewModel.setFilterBy(R.id.filter_by_msg_apps)
            }

            R.id.filter_by_social_media -> {
                menu.findItem(R.id.filter_by_social_media)?.isChecked = true
                notificationsListViewModel.setFilterBy(R.id.filter_by_social_media)
            }

            R.id.call_filter -> {
                menu.findItem(R.id.call_filter)?.isChecked = true
                notificationsListViewModel.setFilterBy(R.id.call_filter)
            }
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val sortByPreferences = getSharedPreferences("Notifications_Sort_By", Context.MODE_PRIVATE)
        val filterByPreferences = getSharedPreferences("Notifications_Filter_By", Context.MODE_PRIVATE)

        val editorSortBy = sortByPreferences.edit()
        val editorFilterBy = filterByPreferences.edit()

        when (item.itemId) {
            R.id.sort_by_date -> {
                editorSortBy.putInt("Notifications_Sort_By", R.id.sort_by_date)
                editorSortBy.apply()
                item.isChecked = true
                notificationsListViewModel.setSortedBy(R.id.sort_by_date)
            }

            R.id.notifications_sort_by_priority -> {
                editorSortBy.putInt("Notifications_Sort_By", R.id.notifications_sort_by_priority)
                editorSortBy.apply()
                item.isChecked = true
                notificationsListViewModel.setSortedBy(R.id.notifications_sort_by_priority)
            }

            R.id.sort_by_contact -> {
                editorSortBy.putInt("Notifications_Sort_By", R.id.sort_by_contact)
                editorSortBy.apply()
                item.isChecked = true
                notificationsListViewModel.setSortedBy(R.id.sort_by_contact)
            }

            R.id.item_help -> {
                if (Resources.getSystem().configuration.locale.language == "fr") {
                    val browserIntent = Intent(
                        Intent.ACTION_VIEW, Uri.parse("https://www.yellowtwigs.com/aide-en-ligne-historique")
                    )
                    startActivity(browserIntent)
                } else {
                    val browserIntent = Intent(
                        Intent.ACTION_VIEW, Uri.parse("https://www.yellowtwigs.com/help-history")
                    )
                    startActivity(browserIntent)
                }
                finish()
            }

            R.id.filter_by_all -> {
                editorFilterBy.putInt("Notifications_Filter_By", R.id.filter_by_all)
                editorFilterBy.apply()
                item.isChecked = true
                notificationsListViewModel.setFilterBy(R.id.filter_by_all)
            }

            R.id.filter_by_social_media -> {
                editorFilterBy.putInt("Notifications_Filter_By", R.id.filter_by_social_media)
                editorFilterBy.apply()
                item.isChecked = true
                notificationsListViewModel.setFilterBy(R.id.filter_by_social_media)
            }

            R.id.filter_by_msg_apps -> {
                editorFilterBy.putInt("Notifications_Filter_By", R.id.filter_by_msg_apps)
                editorFilterBy.apply()
                item.isChecked = true
                notificationsListViewModel.setFilterBy(R.id.filter_by_msg_apps)
            }

            R.id.sms_filter -> {
                editorFilterBy.putInt("Notifications_Filter_By", R.id.sms_filter)
                editorFilterBy.apply()
                item.isChecked = true
                notificationsListViewModel.setFilterBy(R.id.sms_filter)
            }

            R.id.call_filter -> {
                editorFilterBy.putInt("Notifications_Filter_By", R.id.call_filter)
                editorFilterBy.apply()
                item.isChecked = true
                notificationsListViewModel.setFilterBy(R.id.call_filter)
            }

            R.id.mail_filter -> {
                editorFilterBy.putInt("Notifications_Filter_By", R.id.mail_filter)
                editorFilterBy.apply()
                item.isChecked = true
                notificationsListViewModel.setFilterBy(R.id.mail_filter)
            }

            R.id.whatsapp_filter -> {
                editorFilterBy.putInt("Notifications_Filter_By", R.id.whatsapp_filter)
                editorFilterBy.apply()
                item.isChecked = true
                notificationsListViewModel.setFilterBy(R.id.whatsapp_filter)
            }

            R.id.facebook_filter -> {
                editorFilterBy.putInt("Notifications_Filter_By", R.id.facebook_filter)
                editorFilterBy.apply()
                item.isChecked = true
                notificationsListViewModel.setFilterBy(R.id.facebook_filter)
            }

            R.id.signal_filter -> {
                editorFilterBy.putInt("Notifications_Filter_By", R.id.signal_filter)
                editorFilterBy.apply()
                item.isChecked = true
                notificationsListViewModel.setFilterBy(R.id.signal_filter)
            }

            R.id.telegram_filter -> {
                editorFilterBy.putInt("Notifications_Filter_By", R.id.telegram_filter)
                editorFilterBy.apply()
                item.isChecked = true
                notificationsListViewModel.setFilterBy(R.id.telegram_filter)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //endregion

    //region ================================================================= DRAWER LAYOUT ==================================================================

    private fun setupDrawerLayout() {
        binding.navView.apply {
            val menu = binding.navView.menu
            menu.findItem(R.id.nav_home).isChecked = true

            if (EveryActivityUtils.checkIfGoEdition(this@NotificationsHistoryActivity)) {
                menu.findItem(R.id.nav_notifications).isVisible = false
                menu.findItem(R.id.nav_teleworking).isVisible = false
                menu.findItem(R.id.nav_dashboard).isVisible = false
            }

            setNavigationItemSelectedListener { menuItem ->
                if (menuItem.itemId != R.id.nav_sync_contact && menuItem.itemId != R.id.nav_invite_friend) {
                    menuItem.isChecked = true
                }
                binding.drawerLayout.closeDrawers()

                when (menuItem.itemId) {
                    R.id.nav_notifications -> startActivity(
                        Intent(
                            this@NotificationsHistoryActivity, NotificationsSettingsActivity::class.java
                        )
                    )

                    R.id.nav_teleworking -> startActivity(
                        Intent(this@NotificationsHistoryActivity, TeleworkingActivity::class.java)
                    )

                    R.id.nav_dashboard -> startActivity(
                        Intent(this@NotificationsHistoryActivity, DashboardActivity::class.java)
                    )

                    R.id.nav_manage_screen -> startActivity(
                        Intent(
                            this@NotificationsHistoryActivity, ManageMyScreenActivity::class.java
                        )
                    )

                    R.id.nav_help -> startActivity(
                        Intent(
                            this@NotificationsHistoryActivity, HelpActivity::class.java
                        )
                    )

                    R.id.nav_sync_contact -> {
                        requestPermissionLauncher.launch(arrayOf(Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CONTACTS))
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

                binding.drawerLayout.closeDrawer(GravityCompat.START)
                true
            }
        }
    }

    private fun importContacts() {
        CoroutineScope(Dispatchers.Main).launch {
            importContactsViewModel.syncAllContactsInDatabase(contentResolver)
        }
    }

    //endregion

    //region ==================================================================== SETUP UI ====================================================================

    private fun setupBottomNavigationView() {
        binding.navigation.menu.getItem(2).isChecked = true
        binding.navigation.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_contacts -> {
                    startActivity(
                        Intent(
                            this@NotificationsHistoryActivity, ContactsListActivity::class.java
                        ).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    )
                    return@OnNavigationItemSelectedListener true
                }

                R.id.navigation_groups -> {
                    startActivity(
                        Intent(
                            this@NotificationsHistoryActivity, GroupsListActivity::class.java
                        ).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    )
                    return@OnNavigationItemSelectedListener true
                }

                R.id.navigation_cockpit -> {
                    startActivity(
                        Intent(
                            this@NotificationsHistoryActivity, CockpitActivity::class.java
                        ).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    )
                    return@OnNavigationItemSelectedListener true
                }

            }
            false
        })
    }

    private fun setupNotificationsList() {
        notificationsAdapter = NotificationsListAdapter(this) { notification, phoneNumber, platform ->
            if (platform == "com.yellowtwigs.Knockin.notification") {
                startActivity(Intent(this@NotificationsHistoryActivity, DashboardActivity::class.java))
            } else {
                convertPackageNameToGoToWithContact(platform, phoneNumber, this)
            }

            notificationsListViewModel.updateNotification(
                NotificationDB(
                    notification.id,
                    notification.title,
                    notification.contactName,
                    notification.description,
                    notification.platform,
                    notification.timestamp,
                    1,
                    notification.idContact,
                    notification.priority,
                    notification.phoneNumber,
                    notification.mail,
                    "",
                    notification.isSystem
                )
            )
        }

        binding.recyclerView.adapter = notificationsAdapter
        val linearLayoutManager = LinearLayoutManager(this@NotificationsHistoryActivity)
        binding.recyclerView.layoutManager = linearLayoutManager

        notificationsListViewModel.notificationsListViewStateLiveData.observe(this@NotificationsHistoryActivity) { notificationsHistoryViewState ->
            currentList.addAll(notificationsHistoryViewState.list)
            duplicates.addAll(notificationsHistoryViewState.duplicates)
            notificationsAdapter.submitList(null)
            notificationsAdapter.submitList(notificationsHistoryViewState.list)
            binding.recyclerView.smoothScrollToPosition(currentPosition)
        }
        binding.recyclerView.recycledViewPool.setMaxRecycledViews(0, 0)
        binding.recyclerView.setItemViewCacheSize(250)
        val itemTouchHelper = ItemTouchHelper(SwipeDeleteHistory(notificationsAdapter))
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }

    //endregion

    //region ============================================================== RECYCLER VIEW CLICK ===============================================================

    fun recyclerLongClick(id: Int) {
        if (listOfIdsSelected.contains(id)) {
            listOfIdsSelected.remove(id)
        } else {
            listOfIdsSelected.add(id)
        }

        if (listOfIdsSelected.size == 1 && firstClick) {
            firstClick = false
            multiSelectMode = true
            binding.floatingActionButton.visibility = View.INVISIBLE
            binding.toolbarMultiSelect.visibility = View.VISIBLE
            binding.toolbar.visibility = View.INVISIBLE

        } else if (listOfIdsSelected.size == 0) {
            binding.toolbarMultiSelect.visibility = View.GONE
            binding.toolbar.visibility = View.VISIBLE

            firstClick = true
        }

        if (listOfIdsSelected.size == 1) {
            binding.toolbarText.text = listOfIdsSelected.size.toString() + " " + getString(R.string.main_toast_multi_select_mode_selected)
        } else if (listOfIdsSelected.size > 1) {
            binding.toolbarText.text =
                listOfIdsSelected.size.toString() + " " + getString(R.string.main_toast_multi_select_mode_selected_more_than_one)
        }
    }

    //endregion

    fun deleteItem(notification: NotificationsListViewState, layoutPosition: Int) {
        currentPosition = layoutPosition
        notificationsListViewModel.deleteNotification(
            NotificationDB(
                notification.id,
                notification.title,
                notification.contactName,
                notification.description,
                notification.platform,
                notification.timestamp,
                0,
                notification.idContact,
                notification.priority,
                notification.phoneNumber,
                notification.mail,
                "",
                notification.isSystem
            )
        )
    }

    private fun deleteListOfSelectedNotifications() {
        if (duplicates.isNotEmpty()) {
            duplicates.map { notification ->
                notificationsListViewModel.deleteNotification(
                    NotificationDB(
                        id = notification.id,
                        notification.title,
                        notification.contactName,
                        notification.description,
                        notification.platform,
                        notification.timestamp,
                        isCancellable = 0,
                        notification.idContact,
                        notification.priority,
                        notification.phoneNumber,
                        notification.mail,
                        "",
                        notification.isSystem
                    )
                )
            }
        }

        listOfIdsSelected.map { id ->
            val test = currentList.find { it.id == id }
            test?.let {
                notificationsListViewModel.deleteNotification(
                    NotificationDB(
                        id = test.id,
                        test.title,
                        test.contactName,
                        test.description,
                        test.platform,
                        test.timestamp,
                        isCancellable = 0,
                        test.idContact,
                        test.priority,
                        test.phoneNumber,
                        test.mail,
                        "",
                        test.isSystem
                    )
                )
            }
        }

        CoroutineScope(Dispatchers.Main).launch {
            binding.pieChartLoading.isVisible = true
            binding.recyclerView.isVisible = false
            delay(3000L)
            binding.pieChartLoading.isVisible = false
            binding.recyclerView.isVisible = true
        }

        multiSelectMode = false
        firstClick = true
        listOfIdsSelected.clear()
        binding.toolbarMultiSelect.visibility = View.GONE
        binding.toolbar.visibility = View.VISIBLE
        binding.floatingActionButton.visibility = View.VISIBLE
        notificationsAdapter.clearAll()
    }

    override fun onResume() {
        super.onResume()

        if (fromPopup) {
        }
    }

    private fun checkPermission(permission: String): Boolean {
        val checkPermission = ContextCompat.checkSelfPermission(this, permission)
        return checkPermission == PackageManager.PERMISSION_GRANTED
    }

    private fun deleteAllNotificationsSystem() {
        notificationsListViewModel.deleteAllSystemNotifications()

    }

    private fun deleteAllNotifications() {
        notificationsListViewModel.deleteAllNotifications()
    }
}