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
import com.yellowtwigs.knockin.ui.first_launch.start.ImportContactsViewModel
import com.yellowtwigs.knockin.ui.groups.list.GroupsListActivity
import com.yellowtwigs.knockin.ui.settings.ManageMyScreenActivity
import com.yellowtwigs.knockin.ui.premium.PremiumActivity
import com.yellowtwigs.knockin.ui.notifications.settings.NotificationsSettingsActivity
import com.yellowtwigs.knockin.utils.EveryActivityUtils.checkTheme
import com.yellowtwigs.knockin.utils.EveryActivityUtils.setupTeleworkingItem
import com.yellowtwigs.knockin.repositories.firebase.FirebaseViewModel
import com.yellowtwigs.knockin.utils.NotificationsGesture.convertPackageNameToGoToWithContact
import com.yellowtwigs.knockin.utils.SaveUserIdToFirebase.saveUserIdToFirebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NotificationsHistoryActivity : AppCompatActivity() {

    //region ========================================== Val or Var ==========================================

    private var notification_history_ToolbarMultiSelectModeLayout: RelativeLayout? = null
    private var notification_history_ToolbarMultiSelectModeTitle: TextView? = null

    private var fromPopup: Boolean = false

    private lateinit var notificationsAdapter: NotificationsListAdapter
    private lateinit var binding: ActivityNotificationsHistoryBinding
    private lateinit var sharedPreferences: SharedPreferences

    private val notificationsListViewModel: NotificationsListViewModel by viewModels()
    private val importContactsViewModel: ImportContactsViewModel by viewModels()
    private val firebaseViewModel: FirebaseViewModel by viewModels()

    private lateinit var userIdPreferences: SharedPreferences

    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkTheme(this)

        binding = ActivityNotificationsHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("Notification_tri", Context.MODE_PRIVATE)

        setupToolbar()
        setupDrawerLayout()
        setupBottomNavigationView()
        setupNotificationsList()

        userIdPreferences = getSharedPreferences("User_Id", Context.MODE_PRIVATE)

        saveUserIdToFirebase(userIdPreferences, firebaseViewModel, "Enter the NotificationsHistoryActivity")

        //region ======================================== Listeners =========================================

        binding.apply {
            floatingActionButton.setOnClickListener {
                MaterialAlertDialogBuilder(
                    this@NotificationsHistoryActivity, R.style.AlertDialog
                ).setTitle(getString(R.string.notification_history_alert_dialog_title))
                    .setMessage(getString(R.string.notification_history_alert_dialog_text))
                    .setPositiveButton(R.string.notification_history_alert_dialog_delete_button) { _, wich -> deleteAllNotifications() }
                    .setNegativeButton(
                        R.string.notification_history_alert_dialog_cancel_button, null
                    )
                    .setNeutralButton(R.string.notification_history_alert_dialog_delete_system_button) { _, _ -> deleteAllNotificationsSystem() }
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
            R.id.discord_filter -> {
                editorFilterBy.putInt("Notifications_Filter_By", R.id.discord_filter)
                editorFilterBy.apply()
                item.isChecked = true
                notificationsListViewModel.setFilterBy(R.id.discord_filter)
            }
            R.id.viber_filter -> {
                editorFilterBy.putInt("Notifications_Filter_By", R.id.viber_filter)
                editorFilterBy.apply()
                item.isChecked = true
                notificationsListViewModel.setFilterBy(R.id.viber_filter)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //endregion

    //region ======================================== DRAWER LAYOUT =========================================

    private fun setupDrawerLayout() {
        setupTeleworkingItem(binding.navView, this)

        binding.navView.apply {
            menu.getItem(0).isChecked = true
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
                    R.id.nav_manage_screen -> startActivity(
                        Intent(
                            this@NotificationsHistoryActivity, ManageMyScreenActivity::class.java
                        )
                    )
                    R.id.nav_in_app -> startActivity(
                        Intent(
                            this@NotificationsHistoryActivity, PremiumActivity::class.java
                        )
                    )
                    R.id.nav_help -> startActivity(
                        Intent(
                            this@NotificationsHistoryActivity, HelpActivity::class.java
                        )
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

                binding.drawerLayout.closeDrawer(GravityCompat.START)
                true
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
        notificationsAdapter = NotificationsListAdapter(this) { phoneNumber, platform ->
            convertPackageNameToGoToWithContact(platform, phoneNumber, this)
        }

        binding.recyclerView.apply {
            this.adapter = notificationsAdapter

            notificationsListViewModel.getAllNotifications().observe(
                this@NotificationsHistoryActivity
            ) { notifications ->
                notificationsAdapter.submitList(null)
                notificationsAdapter.submitList(notifications)
//                scrollToPosition(0)
//                (layoutManager as LinearLayoutManager).scrollToPosition(0)
            }
            layoutManager = LinearLayoutManager(this@NotificationsHistoryActivity)
            recycledViewPool.setMaxRecycledViews(0, 0)
//            setItemViewCacheSize(50)
            val itemTouchHelper = ItemTouchHelper(SwipeDeleteHistory(notificationsAdapter))
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
                notification.priority,
                notification.phoneNumber,
                notification.mail,
                "",
                notification.isSystem
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

    private fun deleteAllNotificationsSystem() {
        notificationsListViewModel.deleteAllSystemNotifications()

    }

    private fun deleteAllNotifications() {
        notificationsListViewModel.deleteAllNotifications()
    }
}