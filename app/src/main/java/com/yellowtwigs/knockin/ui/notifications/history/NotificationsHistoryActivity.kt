package com.yellowtwigs.knockin.ui.notifications.history

import android.content.*
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.ActivityNotificationsHistoryBinding
import com.yellowtwigs.knockin.model.database.data.NotificationDB
import com.yellowtwigs.knockin.ui.cockpit.CockpitActivity
import com.yellowtwigs.knockin.ui.HelpActivity
import com.yellowtwigs.knockin.ui.contacts.list.ContactsListActivity
import com.yellowtwigs.knockin.ui.groups.list.GroupsListActivity
import com.yellowtwigs.knockin.ui.settings.ManageMyScreenActivity
import com.yellowtwigs.knockin.ui.in_app.PremiumActivity
import com.yellowtwigs.knockin.ui.notifications.settings.NotificationsSettingsActivity
import com.yellowtwigs.knockin.ui.teleworking.TeleworkingActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationsHistoryActivity : AppCompatActivity() {

    //region ========================================== Val or Var ==========================================

    private var notification_history_ToolbarMultiSelectModeLayout: RelativeLayout? = null
    private var notification_history_ToolbarMultiSelectModeTitle: TextView? = null

    private var fromPopup: Boolean = false
    private var firstClick: Boolean = true
    private var multiSelectMode: Boolean = false

    private var numberForPermission = ""
    private val MAKE_CALL_PERMISSION_REQUEST_CODE = 1

    private lateinit var notificationsAdapter: NotificationsListAdapter
    private lateinit var binding: ActivityNotificationsHistoryBinding
    private lateinit var sharedPreferences: SharedPreferences

    private val notificationsListViewModel: NotificationsListViewModel by viewModels()

    //endregion

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

        binding = ActivityNotificationsHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //region ====================================== FindViewById =======================================

        notification_history_ToolbarMultiSelectModeLayout =
            findViewById(R.id.toolbar_mode_multi_select)
        notification_history_ToolbarMultiSelectModeTitle =
            findViewById(R.id.notification_history_toolbar_multi_select_mode_tv)

        //endregion

        sharedPreferences = getSharedPreferences("Notification_tri", Context.MODE_PRIVATE)

        setupToolbar()
        setupDrawerLayout()
        setupBottomNavigationView()
        setupNotificationsList()

        //region ======================================== Listeners =========================================

        binding.apply {
            floatingActionButton.setOnClickListener {
                MaterialAlertDialogBuilder(this@NotificationsHistoryActivity, R.style.AlertDialog)
                    .setTitle(getString(R.string.notification_history_alert_dialog_title))
                    .setMessage(getString(R.string.notification_history_alert_dialog_text))
                    .setPositiveButton(R.string.notification_history_alert_dialog_delete_button) { _, wich -> deleteAllNotifications() }
                    .setNegativeButton(
                        R.string.notification_history_alert_dialog_cancel_button,
                        null
                    )
                    .setNeutralButton(R.string.notification_history_alert_dialog_delete_system_button) { _, _ -> deleteAllNotificationsSystem() }
                    .show()
            }

            closeIcon.setOnClickListener {
//                refreshActivity()
            }

            deleteIcon.setOnClickListener {
                MaterialAlertDialogBuilder(this@NotificationsHistoryActivity, R.style.AlertDialog)
                    .setTitle(getString(R.string.notification_history_delete_notifications))
                    .setMessage(getString(R.string.notification_history_delete_notifications_confirmation))
                    .setPositiveButton(R.string.edit_contact_validate) { _, _ ->
//                        listOfItemSelected.forEach { notification ->
//                            notification.id?.let { id ->
//                                dao.deleteNotificationById(id)
//                            }
//                        }
//                        listOfItemSelected.clear()
//                        refreshActivity()
//                        finish()
//                        overridePendingTransition(0, 0)
//                        startActivity(intent)
//                        overridePendingTransition(0, 0)
                    }
                    .setNegativeButton(R.string.delete_contact_from_group_cancel) { _, _ -> }
                    .show()
            }
        }

        //endregion
    }

    //region =========================================== TOOLBAR ============================================

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

//    private fun setupToolbar() {
//        setSupportActionBar(binding.toolbar)
//        val actionbar = supportActionBar
//        binding.toolbar.overflowIcon =
//            AppCompatResources.getDrawable(this, R.drawable.ic_toolbar_menu)
//        actionbar?.let {
//            it.title = ""
//            it.setDisplayHomeAsUpEnabled(true)
//            it.setHomeAsUpIndicator(R.drawable.ic_open_drawer)
//            it.setBackgroundDrawable(ColorDrawable(Color.parseColor("#ffffff")))
//        }
//    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.history_toolbar_menu, menu)

        val sortByPreferences = getSharedPreferences("Notifications_Sort_By", Context.MODE_PRIVATE)
        val filterByPreferences =
            getSharedPreferences("Notifications_Filter_By", Context.MODE_PRIVATE)

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
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val sortByPreferences = getSharedPreferences("Notifications_Sort_By", Context.MODE_PRIVATE)
        val filterByPreferences =
            getSharedPreferences("Notifications_Filter_By", Context.MODE_PRIVATE)

        val editorSortBy = sortByPreferences.edit()
        val editorFilterBy = filterByPreferences.edit()

        when (item.itemId) {
            R.id.sort_by_date -> {
                editorSortBy.putInt("Notifications_Sort_By", R.id.sort_by_date)
                editorSortBy.apply()
                item.isChecked = true
                notificationsListViewModel.setSortedBy(R.id.sort_by_date)
            }
            R.id.sort_by_priority -> {
                editorSortBy.putInt("Notifications_Sort_By", R.id.sort_by_priority)
                editorSortBy.apply()
                item.isChecked = true
                notificationsListViewModel.setSortedBy(R.id.sort_by_priority)
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
                        Intent.ACTION_VIEW,
                        Uri.parse("https://www.yellowtwigs.com/aide-en-ligne-historique")
                    )
                    startActivity(browserIntent)
                } else {
                    val browserIntent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://www.yellowtwigs.com/help-history")
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
            R.id.filter_by_msg_apps -> {
                editorFilterBy.putInt("Notifications_Filter_By", R.id.filter_by_msg_apps)
                editorFilterBy.apply()
                item.isChecked = true
                notificationsListViewModel.setFilterBy(R.id.filter_by_msg_apps)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //endregion

    //region ======================================== DRAWER LAYOUT =========================================

    private fun setupDrawerLayout() {
        binding.navView.apply {
            val navItem = menu.findItem(R.id.nav_manage_screen)
            navItem.isChecked = true
            menu.getItem(0).isChecked = true
            setNavigationItemSelectedListener { menuItem ->
                menuItem.isChecked = true
                binding.drawerLayout.closeDrawers()

                val itemLayout = findViewById<ConstraintLayout>(R.id.teleworking_item)
                val itemText = findViewById<AppCompatTextView>(R.id.teleworking_item_text)

                itemText.text =
                    "${getString(R.string.teleworking)} ${getString(R.string.left_drawer_settings)}"

                itemLayout.setOnClickListener {
                    startActivity(
                        Intent(
                            this@NotificationsHistoryActivity,
                            TeleworkingActivity::class.java
                        )
                    )
                }

                when (menuItem.itemId) {
                    R.id.nav_notifications -> startActivity(
                        Intent(
                            this@NotificationsHistoryActivity,
                            NotificationsSettingsActivity::class.java
                        )
                    )
                    R.id.nav_manage_screen -> startActivity(
                        Intent(
                            this@NotificationsHistoryActivity,
                            ManageMyScreenActivity::class.java
                        )
                    )
                    R.id.nav_in_app -> startActivity(
                        Intent(
                            this@NotificationsHistoryActivity,
                            PremiumActivity::class.java
                        )
                    )
                    R.id.nav_help -> startActivity(
                        Intent(
                            this@NotificationsHistoryActivity,
                            HelpActivity::class.java
                        )
                    )
                }

                binding.drawerLayout.closeDrawer(GravityCompat.START)
                true
            }
        }
    }

    //endregion

    //region =========================================== SETUP UI ===========================================

    private fun setupBottomNavigationView() {
        binding.navigation.menu.getItem(2).isChecked = true
        binding.navigation.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_contacts -> {
                    startActivity(
                        Intent(
                            this@NotificationsHistoryActivity,
                            ContactsListActivity::class.java
                        ).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    )
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_groups -> {
                    startActivity(
                        Intent(
                            this@NotificationsHistoryActivity,
                            GroupsListActivity::class.java
                        ).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    )
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_cockpit -> {
                    startActivity(
                        Intent(
                            this@NotificationsHistoryActivity,
                            CockpitActivity::class.java
                        ).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    )
                    return@OnNavigationItemSelectedListener true
                }

            }
            false
        })
    }

    private fun setupNotificationsList() {
        notificationsAdapter = NotificationsListAdapter(this)

        binding.recyclerView.apply {
            this.adapter = notificationsAdapter

            notificationsListViewModel.getAllNotifications().observe(
                this@NotificationsHistoryActivity
            ) { notifications ->
                notificationsAdapter.submitList(notifications)
//                scrollToPosition(0)
//                (layoutManager as LinearLayoutManager).scrollToPosition(0)
            }
            layoutManager = LinearLayoutManager(this@NotificationsHistoryActivity)
            recycledViewPool.setMaxRecycledViews(0, 0)
//            setItemViewCacheSize(50)
            val itemTouchHelper = ItemTouchHelper(SwipeToDeleteCallback(notificationsAdapter))
            itemTouchHelper.attachToRecyclerView(this)
        }
    }

    //endregion

//    //region ======================================== recyclerClick =========================================
//
//    fun recyclerSimpleClick(position: Int) {
//        val contactManager = ContactManager(this.applicationContext)
//
//        val contact = contactManager.getContact(listOfNotificationsDB[position].contactName)
//
//        when (listOfNotificationsDB[position].platform) {
//            "com.whatsapp" -> {
//                if (contact != null) {
//                    openWhatsapp(contact.getFirstPhoneNumber(), this)
//                } else {
//                    openWhatsapp(this)
//                }
//            }
//
//            "com.android.incallui" -> {
//                phoneCall(listOfNotificationsDB[position].contactName)
//            }
//
//            "com.google.android.gm" -> openGmail(this)
//
//            "com.facebook.katana" -> goToFacebook()
//
//            "org.telegram.messenger" -> {
//                if (contact != null) {
//                    goToTelegram(this, contact.getFirstPhoneNumber())
//                } else {
//                    goToTelegram(this)
//                }
//            }
//
//            "com.facebook.orca" -> {
//                if (contact != null) {
//                    openMessenger(contact.getMessengerID(), this)
//                } else {
//                    openMessenger("", this)
//                }
//            }
//
//            "com.google.android.apps.messaging", "com.android.mms", "com.samsung.android.messaging", "com.contapps.android" -> {
//                if (contact != null) {
//                    val intent = Intent(
//                        Intent.ACTION_SENDTO,
//                        Uri.fromParts("sms", contact.getFirstPhoneNumber(), null)
//                    )
//                    startActivity(intent)
//                } else {
//                    val intent = Intent(
//                        Intent.ACTION_SENDTO,
//                        Uri.fromParts(
//                            "sms",
//                            listOfNotificationsDB[position].contactName,
//                            null
//                        )
//                    )
//                    startActivity(intent)
//                }
//                val sendIntent = Intent(Intent.ACTION_VIEW)
//                sendIntent.data = Uri.parse("sms:")
//            }
//
//            "com.instagram.android" -> goToInstagramPage()
//
//            "com.microsoft.office.outlook" -> goToOutlook(this)
//
//            "com.twitter.android" -> goToTwitter()
//
//            "org.thoughtcrime.securesms" -> goToSignal(this)
//
//            "com.skype.raider" -> goToSkype()
//
//            "com.linkedin.android" -> goToLinkedin()
//        }
//    }
//
//    fun recyclerLongClick(position: Int) {
//        if (listOfItemSelected.contains(listOfNotificationsDB[position])) {
//            listOfItemSelected.remove(listOfNotificationsDB[position])
//        } else {
//            listOfItemSelected.add(listOfNotificationsDB[position])
//        }
//
//        val i = listOfItemSelected.size
//
//        binding.apply {
//            if (listOfItemSelected.size == 1 && firstClick) {
//                Toast.makeText(
//                    this@NotificationHistoryActivity,
//                    R.string.main_toast_multi_select_actived,
//                    Toast.LENGTH_SHORT
//                ).show()
//                firstClick = false
//                multiSelectMode = true
//                toolbarModeMultiSelect.visibility = View.VISIBLE
//                toolbarLayout.visibility = View.INVISIBLE
//
//            } else if (listOfItemSelected.size == 0) {
//                Toast.makeText(
//                    this@NotificationHistoryActivity,
//                    R.string.main_toast_multi_select_deactived,
//                    Toast.LENGTH_SHORT
//                )
//                    .show()
//
//                toolbarModeMultiSelect.visibility = View.GONE
//                toolbarLayout.visibility = View.VISIBLE
//
//                firstClick = true
//            }
//        }
//
//        if (listOfItemSelected.size == 1) {
//            notification_history_ToolbarMultiSelectModeTitle!!.text =
//                i.toString() + " " + getString(R.string.main_toast_multi_select_mode_selected)
//        } else if (listOfItemSelected.size > 1) {
//            notification_history_ToolbarMultiSelectModeTitle!!.text =
//                i.toString() + " " + getString(R.string.main_toast_multi_select_mode_selected_more_than_one)
//        }
//    }
//
//    //endregion

    fun deleteItem(notification: NotificationsListViewState) {
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
                notification.priority
            )
        )
    }

    override fun onResume() {
        super.onResume()

        if (fromPopup) {
//            refreshActivity()
        }
    }

    private fun checkPermission(permission: String): Boolean {
        val checkPermission = ContextCompat.checkSelfPermission(this, permission)
        return checkPermission == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Suppression de toutes les notifications systèmes
     */
    private fun deleteAllNotificationsSystem() {
//        val listTmp = mutableListOf<NotificationDB>()
//        listTmp.addAll(listOfNotificationsDB)
//        listTmp.forEach {
//            if (!isMessagingApp(it.platform)) {
//                it.id?.let { it1 ->
//                    dao.deleteNotificationById(it1)
//                }
//            }
//        }
    }

    /**
     * Suppression de toutes les notifications
     */
    private fun deleteAllNotifications() {
//        dao.deleteAllNotification()
//        refreshActivity()
    }
}