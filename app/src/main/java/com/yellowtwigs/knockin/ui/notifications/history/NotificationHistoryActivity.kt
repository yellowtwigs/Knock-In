package com.yellowtwigs.knockin.ui.notifications.history

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Telephony
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.ActivityNotificationHistoryBinding
import com.yellowtwigs.knockin.ui.group.GroupManagerActivity
import com.yellowtwigs.knockin.model.ContactManager
import com.yellowtwigs.knockin.model.ContactsRoomDatabase
import com.yellowtwigs.knockin.model.DbWorkerThread
import com.yellowtwigs.knockin.model.dao.NotificationsDao
import com.yellowtwigs.knockin.model.data.NotificationDB
import com.yellowtwigs.knockin.ui.CockpitActivity
import com.yellowtwigs.knockin.ui.HelpActivity
import com.yellowtwigs.knockin.ui.settings.ManageMyScreenActivity
import com.yellowtwigs.knockin.ui.contacts.MainActivity
import com.yellowtwigs.knockin.ui.in_app.PremiumActivity
import com.yellowtwigs.knockin.ui.notifications.NotificationListener
import com.yellowtwigs.knockin.ui.settings.ManageNotificationActivity
import com.yellowtwigs.knockin.ui.teleworking.TeleworkingActivity
import com.yellowtwigs.knockin.utils.ContactGesture.goToOutlook
import com.yellowtwigs.knockin.utils.ContactGesture.goToSignal
import com.yellowtwigs.knockin.utils.ContactGesture.goToTelegram
import com.yellowtwigs.knockin.utils.ContactGesture.openMessenger
import com.yellowtwigs.knockin.utils.ContactGesture.openWhatsapp
import kotlin.collections.ArrayList

/**
 * La Classe qui permet d'afficher l'historique des notifications
 * @author Florian Striebel
 */
class NotificationHistoryActivity : AppCompatActivity() {

    //region ========================================== Val or Var ==========================================

    private var notification_history_ToolbarMultiSelectModeLayout: RelativeLayout? = null
    private var notification_history_ToolbarMultiSelectModeTitle: TextView? = null

    private val listOfItemSelected = ArrayList<NotificationDB>()
    private val listOfNotificationsDB = arrayListOf<NotificationDB>()

    private var fromPopup: Boolean = false
    private var firstClick: Boolean = true
    private var multiSelectMode: Boolean = false

    private var numberForPermission = ""
    private val MAKE_CALL_PERMISSION_REQUEST_CODE = 1

    private val mOnNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_contacts -> {
                    startActivity(
                        Intent(
                            this@NotificationHistoryActivity,
                            MainActivity::class.java
                        ).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    )
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_teleworking -> {
                    startActivity(
                        Intent(
                            this@NotificationHistoryActivity,
                            TeleworkingActivity::class.java
                        ).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    )
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_groups -> {
                    startActivity(
                        Intent(
                            this@NotificationHistoryActivity,
                            GroupManagerActivity::class.java
                        ).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    )
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_notifcations -> {
                }
                R.id.navigation_cockpit -> {
                    startActivity(
                        Intent(
                            this@NotificationHistoryActivity,
                            CockpitActivity::class.java
                        ).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    )
                    return@OnNavigationItemSelectedListener true
                }

            }
            false
        }

    private lateinit var database: ContactsRoomDatabase
    private lateinit var dao: NotificationsDao
    private lateinit var notificationsAdapter: NotificationsListAdapter
    private lateinit var binding: ActivityNotificationHistoryBinding
    private lateinit var sharedPreferences: SharedPreferences

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

        binding = ActivityNotificationHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //region ====================================== FindViewById =======================================

        notification_history_ToolbarMultiSelectModeLayout =
            findViewById(R.id.toolbar_mode_multi_select)
        notification_history_ToolbarMultiSelectModeTitle =
            findViewById(R.id.notification_history_toolbar_multi_select_mode_tv)

        //endregion

        DbWorkerThread("dbWorkerThread").start()
        ContactsRoomDatabase.getDatabase(this)?.let {
            database = it
            dao = it.notificationsDao()
        }

        sharedPreferences = getSharedPreferences("Notification_tri", Context.MODE_PRIVATE)

        setupToolbar()
        setupDrawerLayout()
        setupBottomNavigationView()

        updateFilter()

        swipeRefreshActivity()

        //region ======================================== Listeners =========================================

        binding.apply {
            floatingActionButton.setOnClickListener {
                MaterialAlertDialogBuilder(this@NotificationHistoryActivity, R.style.AlertDialog)
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
                refreshActivity()
            }

            searchBar.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    s?.let { searchBarText ->
                        setupNotificationsList(filterWithSearchBar(searchBarText.toString()))
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                }
            })

            deleteIcon.setOnClickListener {
                MaterialAlertDialogBuilder(this@NotificationHistoryActivity, R.style.AlertDialog)
                    .setTitle(getString(R.string.notification_history_delete_notifications))
                    .setMessage(getString(R.string.notification_history_delete_notifications_confirmation))
                    .setPositiveButton(R.string.edit_contact_validate) { _, _ ->
                        listOfItemSelected.forEach { notification ->
                            notification.id?.let { id ->
                                dao.deleteNotificationById(id)
                            }
                        }
                        listOfItemSelected.clear()
                        refreshActivity()
                        finish()
                        overridePendingTransition(0, 0)
                        startActivity(intent)
                        overridePendingTransition(0, 0)
                    }
                    .setNegativeButton(R.string.delete_contact_from_group_cancel) { _, _ -> }
                    .show()
            }
        }

        //endregion
    }

    //region ============================================ Setup =============================================

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        val actionbar = supportActionBar
        binding.toolbar.overflowIcon =
            AppCompatResources.getDrawable(this, R.drawable.ic_toolbar_menu)
        actionbar?.let {
            it.title = ""
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.ic_open_drawer)
            it.setBackgroundDrawable(ColorDrawable(Color.parseColor("#ffffff")))
        }
    }

    private fun setupDrawerLayout() {
        binding.navView.apply {
            val navItem = menu.findItem(R.id.nav_manage_screen)
            navItem.isChecked = true
            menu.getItem(0).isChecked = true
            setNavigationItemSelectedListener { menuItem ->
                menuItem.isChecked = true
                binding.drawerLayout.closeDrawers()

                when (menuItem.itemId) {
                    R.id.nav_home -> startActivity(
                        Intent(
                            this@NotificationHistoryActivity,
                            MainActivity::class.java
                        )
                    )
                    R.id.nav_notif_config -> startActivity(
                        Intent(
                            this@NotificationHistoryActivity,
                            ManageNotificationActivity::class.java
                        )
                    )
                    R.id.navigation_teleworking -> startActivity(
                        Intent(
                            this@NotificationHistoryActivity,
                            TeleworkingActivity::class.java
                        )
                    )
                    R.id.nav_manage_screen -> startActivity(
                        Intent(
                            this@NotificationHistoryActivity,
                            ManageMyScreenActivity::class.java
                        )
                    )
                    R.id.nav_in_app -> startActivity(
                        Intent(
                            this@NotificationHistoryActivity,
                            PremiumActivity::class.java
                        )
                    )
                    R.id.nav_help -> startActivity(
                        Intent(
                            this@NotificationHistoryActivity,
                            HelpActivity::class.java
                        )
                    )
                }

                binding.drawerLayout.closeDrawer(GravityCompat.START)
                true
            }
        }
    }

    private fun setupBottomNavigationView() {
        binding.navigation.menu.getItem(2).isChecked = true
        binding.navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    private fun setupNotificationsList(notifications: List<NotificationDB>) {
        notificationsAdapter = NotificationsListAdapter(this)

        listOfNotificationsDB.clear()
        listOfNotificationsDB.addAll(notifications)

        binding.recyclerView.apply {
            this.adapter = notificationsAdapter
            notificationsAdapter.submitList(notifications)
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@NotificationHistoryActivity)
            (layoutManager as LinearLayoutManager).scrollToPosition(0)
            recycledViewPool.setMaxRecycledViews(0, 0)
        }
        binding.recyclerView.setItemViewCacheSize(50)
    }

    //endregion

    //region ============================================ Filter ============================================

    private fun filterWithSearchBar(searchBarText: String): ArrayList<NotificationDB> {
        val notifications = arrayListOf<NotificationDB>()

        if (sharedPreferences.getBoolean("filter_by_msg_apps", true)) {
            if (searchBarText.isEmpty() || searchBarText == "") {
                notifications.addAll(dao.getAllNotifications().filter {
                    isMessagingApp(it.platform)
                })
            } else {
                notifications.addAll(dao.getAllNotifications().filter {
                    isMessagingApp(it.platform)
                }.filter {
                    it.title.contains(searchBarText) || it.description.contains(searchBarText)
                })
            }
        } else {
            notifications.addAll(dao.getAllNotifications().filter {
                it.title.contains(searchBarText) || it.description.contains(searchBarText)
            })
        }

        return notifications
    }

    private fun sortByDate(): List<NotificationDB> {
        val notifications = arrayListOf<NotificationDB>()

        if (sharedPreferences.getBoolean("filter_by_msg_apps", true)) {
            notifications.addAll(dao.getAllNotifications().filter {
                isMessagingApp(it.platform)
            })
        } else {
            notifications.addAll(dao.getAllNotifications())
        }

        Log.i("notifications", "${sharedPreferences.getString("tri", "date")}")
        return notifications.sortedByDescending { it.id }
    }

    private fun sortByPriority(): List<NotificationDB> {
        val notifications = arrayListOf<NotificationDB>()

        val listTmp =
            database.notificationsDao().getContactWithPriority0And2() as MutableList<NotificationDB>
        val listTmp2 = mutableListOf<NotificationDB>()

        if (sharedPreferences.getBoolean("filter_by_msg_apps", true)) {
            notifications.addAll(dao.getAllNotifications().filter {
                isMessagingApp(it.platform)
            })
        } else {
            notifications.addAll(dao.getAllNotifications())
        }

        listTmp2.addAll(notifications)
        listTmp2.removeAll(listTmp)
        listTmp.addAll(firstContactPriority0(listTmp).coerceAtLeast(0), listTmp2)
        notifications.removeAll(notifications)
        notifications.addAll(listTmp)

        return notifications
    }

    private fun sortByContact(): List<NotificationDB> {
        val notifications = arrayListOf<NotificationDB>()

        if (sharedPreferences.getBoolean("filter_by_msg_apps", true)) {
            notifications.addAll(dao.getNotifSortByContact().filter {
                isMessagingApp(it.platform)
            })
        } else {
            notifications.addAll(dao.getNotifSortByContact())
        }

//        listNotif.retainAll(notification_history_ListOfNotificationDB)

        return notifications
    }

    private fun updateFilter() {
        when {
            sharedPreferences.getString("tri", "date") == "date" -> {
                setupNotificationsList(sortByDate())
            }
            sharedPreferences.getString("tri", "date") == "priority" -> {
                setupNotificationsList(sortByPriority())
            }
            sharedPreferences.getString("tri", "date") == "contact" -> {
                setupNotificationsList(sortByContact())
            }
            else -> {
                setupNotificationsList(sortByDate())
            }
        }
    }

//    private fun updateFilterMultiSelect() {
//        val sharedPreferences = getSharedPreferences("Notification_tri", Context.MODE_PRIVATE)
//        if (sharedPreferences.getBoolean("filter_by_msg_apps", true)) {
//
//            listOfNotificationsDB.clear()
//
//            val stringSearch = binding.searchBar.text.toString().toLowerCase()
//            if (stringSearch.isEmpty()) {
//                notification_history_ListOfNotificationDB.addAll(
//                    database.notificationsDao().getAllNotifications() as ArrayList<NotificationDB>
//                )
//            } else {
//                notification_history_ListOfNotificationDB.addAll(
//                    database?.notificationsDao()
//                        ?.getNotificationFiltered(stringSearch) as ArrayList<NotificationDB>
//                )
//            }
//            val listTmp = mutableListOf<NotificationDB>()
//            listTmp.addAll(notification_history_ListOfNotificationDB)
//            listTmp.forEach {
//                if (!isMessagingApp(it.platform)) {
//                    notification_history_ListOfNotificationDB.remove(it)
//                }
//            }
//        } else {
//            notification_history_ListOfNotificationDB.removeAll(
//                notification_history_ListOfNotificationDB
//            )
//            notification_history_ListOfNotificationDB.addAll(
//                database?.notificationsDao()
//                    ?.getAllNotifications() as ArrayList<NotificationDB>
//            )
//            val listTmp = mutableListOf<NotificationDB>()
//            val stringSearch = notification_Search_TextView!!.text.toString().toLowerCase()
//            listTmp.addAll(notification_history_ListOfNotificationDB)
//            if (stringSearch.isNotEmpty()) {
//                val regex = (".*$stringSearch.*").toRegex()
//                listTmp.forEach {
//                    if (!it.contactName.toLowerCase()
//                            .matches(regex) && !it.description.toLowerCase().matches(regex)
//                    ) {
//                        notification_history_ListOfNotificationDB.remove(it)
//                    }
//                }
//            }
//        }
//    }

    //endregion

    private fun swipeRefreshActivity() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            val handler = Handler()
            handler.postDelayed(
                { binding.swipeRefreshLayout.isRefreshing = false },
                1000
            )
            refreshActivity()
        }
    }

    //region ======================================== recyclerClick =========================================

    fun recyclerSimpleClick(position: Int) {
        val contactManager = ContactManager(this.applicationContext)

        val contact = contactManager.getContact(listOfNotificationsDB[position].contactName)

        when (listOfNotificationsDB[position].platform) {
            "com.whatsapp" -> {
                if (contact != null) {
                    openWhatsapp(contact.getFirstPhoneNumber(), this)
                } else {
                    openWhatsapp(this)
                }
            }

            "com.android.incallui" -> {
                phoneCall(listOfNotificationsDB[position].contactName)
            }

            "com.google.android.gm" -> openGmail(this)

            "com.facebook.katana" -> goToFacebook()

            "org.telegram.messenger" -> {
                if (contact != null) {
                    goToTelegram(this, contact.getFirstPhoneNumber())
                } else {
                    goToTelegram(this)
                }
            }

            "com.facebook.orca" -> {
                if (contact != null) {
                    openMessenger(contact.getMessengerID(), this)
                } else {
                    openMessenger("", this)
                }
            }

            "com.google.android.apps.messaging", "com.android.mms", "com.samsung.android.messaging", "com.contapps.android" -> {
                if (contact != null) {
                    val intent = Intent(
                        Intent.ACTION_SENDTO,
                        Uri.fromParts("sms", contact.getFirstPhoneNumber(), null)
                    )
                    startActivity(intent)
                } else {
                    val intent = Intent(
                        Intent.ACTION_SENDTO,
                        Uri.fromParts(
                            "sms",
                            listOfNotificationsDB[position].contactName,
                            null
                        )
                    )
                    startActivity(intent)
                }
                val sendIntent = Intent(Intent.ACTION_VIEW)
                sendIntent.data = Uri.parse("sms:")
            }

            "com.instagram.android" -> goToInstagramPage()

            "com.microsoft.office.outlook" -> goToOutlook(this)

            "com.twitter.android" -> goToTwitter()

            "org.thoughtcrime.securesms" -> goToSignal(this)

            "com.skype.raider" -> goToSkype()

            "com.linkedin.android" -> goToLinkedin()
        }
    }

    fun recyclerLongClick(position: Int) {

        if (listOfItemSelected.contains(listOfNotificationsDB[position])) {
            listOfItemSelected.remove(listOfNotificationsDB[position])
        } else {
            listOfItemSelected.add(listOfNotificationsDB[position])
        }

        val i = listOfItemSelected.size

        binding.apply {
            if (listOfItemSelected.size == 1 && firstClick) {
                Toast.makeText(
                    this@NotificationHistoryActivity,
                    R.string.main_toast_multi_select_actived,
                    Toast.LENGTH_SHORT
                ).show()
                firstClick = false
                multiSelectMode = true
                toolbarModeMultiSelect.visibility = View.VISIBLE
                toolbarLayout.visibility = View.INVISIBLE

            } else if (listOfItemSelected.size == 0) {
                Toast.makeText(
                    this@NotificationHistoryActivity,
                    R.string.main_toast_multi_select_deactived,
                    Toast.LENGTH_SHORT
                )
                    .show()

                toolbarModeMultiSelect.visibility = View.GONE
                toolbarLayout.visibility = View.VISIBLE

                firstClick = true
            }
        }

        if (listOfItemSelected.size == 1) {
            notification_history_ToolbarMultiSelectModeTitle!!.text =
                i.toString() + " " + getString(R.string.main_toast_multi_select_mode_selected)
        } else if (listOfItemSelected.size > 1) {
            notification_history_ToolbarMultiSelectModeTitle!!.text =
                i.toString() + " " + getString(R.string.main_toast_multi_select_mode_selected_more_than_one)
        }
    }

    //endregion

    //region ============================================= goTo =============================================

    private fun goToSkype() {
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("skype://skype"))
        try {
            startActivity(appIntent)
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://skype.com/")
                )
            )
        }
    }

    private fun goToLinkedin() {
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("linkedin://you"))
        try {
            startActivity(appIntent)
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://linkedin.com/")
                )
            )
        }
    }

    private fun goToTwitter() {
        val appIntent = Intent(Intent.ACTION_VIEW)
        appIntent.setClassName("com.twitter.android", "com.twitter.android")
        try {
            startActivity(appIntent)
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://twitter.com/")
                )
            )
        }
    }

    private fun goToFacebook() {
        val uri = Uri.parse("facebook:/newsfeed")
        val likeIng = Intent(Intent.ACTION_VIEW, uri)
        try {
            startActivity(likeIng)
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://facebook.com/")
                )
            )
        }
    }

    private fun goToInstagramPage() {
        val uri = Uri.parse("https://www.instagram.com/")
        val likeIng = Intent(Intent.ACTION_VIEW, uri)

        likeIng.setPackage("com.instagram.android")

        try {
            startActivity(likeIng)
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://instagram.com/")
                )
            )
        }
    }

    private fun openGmail(context: Context) {
        val intent = context.packageManager.getLaunchIntentForPackage("com.google.android.gm")
        intent?.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    private fun phoneCall(phoneNumber: String) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CALL_PHONE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CALL_PHONE),
                MAKE_CALL_PERMISSION_REQUEST_CODE
            )
            numberForPermission = phoneNumber
        } else {
            if (numberForPermission.isEmpty()) {
                startActivity(Intent(Intent.ACTION_CALL, Uri.fromParts("tel", phoneNumber, null)))
            } else {
                startActivity(Intent(Intent.ACTION_CALL, Uri.fromParts("tel", phoneNumber, null)))
                numberForPermission = ""
            }
        }
    }

    //endregion

    override fun onResume() {
        super.onResume()

        if (fromPopup) {
            refreshActivity()
        }
    }

    private fun firstContactPriority0(notifications: List<NotificationDB>): Int {
        if (notifications.size > 1) {
            var i = notifications.size - 1
            while (i > 0) {
                val contact = database.contactsDao()
                    .getContact(notifications[i].idContact)
                if (contact.contactDB!!.contactPriority != 0) {
                    return i + 1
                }
                i--
            }
        }
        return 0
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.toolbar_menu_filter_notif_history, menu)
        val sharedPreferences = getSharedPreferences("Notification_tri", Context.MODE_PRIVATE)
        when {
            sharedPreferences.getString(
                "tri",
                "date"
            ) == "date" -> menu.findItem(R.id.sort_by_date)?.isChecked = true
            sharedPreferences.getString(
                "tri",
                "date"
            ) == "priority" -> menu.findItem(R.id.sort_by_priority)?.isChecked = true
            else -> menu.findItem(R.id.sort_by_contact)?.isChecked = true
        }
        if (!sharedPreferences.getBoolean("filter_by_msg_apps", true)) {
            menu.findItem(R.id.messagefilter)?.isChecked = false
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val sharedPreferences = getSharedPreferences("Notification_tri", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        when (item.itemId) {
            android.R.id.home -> {
                binding.drawerLayout.openDrawer(GravityCompat.START)
                return true
            }
            R.id.sort_by_date -> {
                editor.putString("tri", "date")
                editor.apply()
                item.isChecked = true
            }
            R.id.sort_by_priority -> {
                editor.putString("tri", "priority")
                editor.apply()
                item.isChecked = true
            }
            R.id.sort_by_contact -> {
                editor.putString("tri", "contact")
                editor.apply()
                item.isChecked = true
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
            R.id.messagefilter -> {
                if (item.isChecked) {
                    editor.putBoolean("filter_by_msg_apps", false)
                    item.isChecked = false
                } else {
                    editor.putBoolean("filter_by_msg_apps", true)
                    item.isChecked = true
                }
                editor.apply()
            }
        }
        updateFilter()
        return super.onOptionsItemSelected(item)
    }

    private fun checkPermission(permission: String): Boolean {
        val checkPermission = ContextCompat.checkSelfPermission(this, permission)
        return checkPermission == PackageManager.PERMISSION_GRANTED
    }

    private fun isMessagingApp(packageName: String): Boolean {
        return when (packageName) {
            NotificationListener.FACEBOOK_PACKAGE -> true
            NotificationListener.MESSENGER_PACKAGE -> true
            NotificationListener.WHATSAPP_SERVICE -> true
            NotificationListener.GMAIL_PACKAGE -> true
            NotificationListener.OUTLOOK_PACKAGE -> true
            NotificationListener.SIGNAL_PACKAGE -> true
            NotificationListener.MESSAGE_PACKAGE, NotificationListener.MESSAGE_SAMSUNG_PACKAGE, NotificationListener.XIAOMI_MESSAGE_PACKAGE -> true
            Telephony.Sms.getDefaultSmsPackage(this) -> true
            NotificationListener.TELEGRAM_PACKAGE -> true
            NotificationListener.INSTAGRAM_PACKAGE -> true
            else -> false
        }
    }

    private fun refreshActivity() {
        notificationsAdapter.clearAll()
        startActivity(
            Intent(
                this@NotificationHistoryActivity,
                NotificationHistoryActivity::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        )
        finish()
    }

    /**
     * Suppression de toutes les notifications systèmes
     */
    private fun deleteAllNotificationsSystem() {
        val listTmp = mutableListOf<NotificationDB>()
        listTmp.addAll(listOfNotificationsDB)
        listTmp.forEach {
            if (!isMessagingApp(it.platform)) {
                it.id?.let { it1 ->
                    dao.deleteNotificationById(it1)
                }
            }
        }
        refreshActivity()
    }

    /**
     * Suppression de toutes les notifications
     */
    private fun deleteAllNotifications() {
        dao.deleteAllNotification()
        refreshActivity()
    }
}