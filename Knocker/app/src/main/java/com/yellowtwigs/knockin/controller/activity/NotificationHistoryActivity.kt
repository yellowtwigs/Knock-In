package com.yellowtwigs.knockin.controller.activity

import android.Manifest
import android.content.*
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.telephony.SmsManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.facebook.internal.Utility.arrayList
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.controller.NotificationListener
import com.yellowtwigs.knockin.controller.NotificationsHistoryRecyclerViewAdapter
import com.yellowtwigs.knockin.controller.activity.group.GroupManagerActivity
import com.yellowtwigs.knockin.model.ContactManager
import com.yellowtwigs.knockin.model.ContactsRoomDatabase
import com.yellowtwigs.knockin.model.DbWorkerThread
import com.yellowtwigs.knockin.model.ModelDB.ContactWithAllInformation
import com.yellowtwigs.knockin.model.ModelDB.NotificationDB


/**
 * La Classe qui permet d'afficher l'historique des notifications
 * @author Florian Striebel
 */
class NotificationHistoryActivity : AppCompatActivity() {

    //region ========================================== Val or Var ==========================================

    private var notification_history_DrawerLayout: DrawerLayout? = null
    private var notification_BottomNavigationView: BottomNavigationView? = null
    private var notification_history_ToolbarLayout: ConstraintLayout? = null

    private var notification_Search_TextView: TextView? = null
    private var notification_history_ToolbarMultiSelectModeLayout: RelativeLayout? = null
    private var notification_history_ToolbarMultiSelectModeClose: AppCompatImageView? = null
    private var notification_history_ToolbarMultiSelectModeDelete: AppCompatImageView? = null
    private var notification_history_ToolbarMultiSelectModeTitle: TextView? = null
    private var notification_history_floating_action_button: FloatingActionButton? = null
    private var notification_history_SwipeRefreshLayout: SwipeRefreshLayout? = null

    private var notification_recyclerview_Adapter: NotificationsHistoryRecyclerViewAdapter? = null
    private var notification_history_RecyclerView: RecyclerView? = null

    private var notification_history_NotificationsDatabase: ContactsRoomDatabase? = null
    private lateinit var notification_history_mDbWorkerThread: DbWorkerThread

    private val listOfItemSelected = ArrayList<NotificationDB>()
    private val notification_history_ListOfNotificationDB = arrayList<NotificationDB>()

    private var fromPopup: Boolean = false

    private var firstClick: Boolean = true
    private var multiSelectMode: Boolean = false

    private var numberForPermission = ""
    private val MAKE_CALL_PERMISSION_REQUEST_CODE = 1
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->

        when (item.itemId) {
            R.id.navigation_contacts -> {
                startActivity(Intent(this@NotificationHistoryActivity, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_groups -> {
                startActivity(Intent(this@NotificationHistoryActivity, GroupManagerActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifcations -> {
            }
            R.id.navigation_cockpit -> {
                startActivity(Intent(this@NotificationHistoryActivity, CockpitActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
                return@OnNavigationItemSelectedListener true
            }

        }
        false
    }

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

        setContentView()

        //region ====================================== FindViewById =======================================

        notification_history_SwipeRefreshLayout = findViewById(R.id.notification_history_swipe_refresh_layout)
        notification_history_ToolbarLayout = findViewById(R.id.notification_history_toolbar_layout)
        notification_Search_TextView = findViewById(R.id.notification_history_search_bar)

        notification_history_ToolbarMultiSelectModeLayout = findViewById(R.id.notification_history_toolbar_multi_select_mode_layout)
        notification_history_ToolbarMultiSelectModeClose = findViewById(R.id.notification_history_toolbar_multi_select_mode_close)
        notification_history_ToolbarMultiSelectModeDelete = findViewById(R.id.notification_history_toolbar_multi_select_mode_delete)
        notification_history_ToolbarMultiSelectModeTitle = findViewById(R.id.notification_history_toolbar_multi_select_mode_tv)
        notification_history_floating_action_button = findViewById(R.id.notification_history_floating_action_button)
        notification_history_RecyclerView = findViewById(R.id.notification_history_recycler_view)

        val settings_left_drawer_ThemeSwitch = findViewById<Switch>(R.id.settings_left_drawer_theme_switch)
        if (sharedThemePreferences.getBoolean("darkTheme", false)) {
            settings_left_drawer_ThemeSwitch!!.isChecked = true
//            notification_history_MainLayout!!.setBackgroundResource(R.drawable.dark_background)
        }

        if (sharedThemePreferences.getBoolean("darkTheme", false)) {
            settings_left_drawer_ThemeSwitch.isChecked = true
//            notification_history_MainLayout!!.setBackgroundResource(R.drawable.dark_background)
        }///
        notification_history_floating_action_button!!.setOnClickListener {
            MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                    .setTitle(getString(R.string.notification_history_alert_dialog_title))
                    .setMessage(getString(R.string.notification_history_alert_dialog_text))
                    .setPositiveButton(R.string.notification_history_alert_dialog_delete_button) { _, wich -> deleteAllNotif() }
                    .setNegativeButton(R.string.notification_history_alert_dialog_cancel_button, null)
                    .setNeutralButton(R.string.notification_history_alert_dialog_delete_system_button) { _, _ -> deleteAllNotifSystem() }
                    .show()
        }

        //region ================================ Call Popup from LeftDrawer ================================

        val sharedPreferencePopup = getSharedPreferences("Phone_call", Context.MODE_PRIVATE)
        val settings_CallPopupSwitch = findViewById<Switch>(R.id.settings_call_popup_switch)

        if (sharedPreferencePopup.getBoolean("popup", true)) {
            settings_CallPopupSwitch!!.isChecked = true
        }

        //endregion

        //endregion

        //region ========================================= Toolbar ==========================================

        val toolbar = findViewById<Toolbar>(R.id.notification_history_toolbar)
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar
        toolbar.overflowIcon = getDrawable(R.drawable.ic_toolbar_menu)
        actionbar!!.run {
            actionbar.title = ""
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_open_drawer)
            setBackgroundDrawable(ColorDrawable(Color.parseColor("#ffffff")))
        }

        //endregion

        //region ====================================== Drawer Layout =======================================

        notification_history_DrawerLayout = findViewById(R.id.notification_history_drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        val menu = navigationView.menu
        val navItem = menu.findItem(R.id.nav_manage_screen)
        navItem.isChecked = true
        navigationView!!.menu.getItem(0).isChecked = true

        navigationView.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            notification_history_DrawerLayout!!.closeDrawers()

            when (menuItem.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                }
                R.id.nav_informations -> startActivity(Intent(this, EditInformationsActivity::class.java))
                R.id.nav_notif_config -> startActivity(Intent(this, ManageNotificationActivity::class.java))
                R.id.nav_settings -> startActivity(Intent(this@NotificationHistoryActivity, SettingsActivity::class.java))
                R.id.nav_manage_screen -> startActivity(Intent(this, ManageMyScreenActivity::class.java))
                R.id.nav_data_access -> {
                }
                R.id.nav_in_app -> startActivity(Intent(this@NotificationHistoryActivity, PremiumActivity::class.java))
                R.id.nav_knockons -> startActivity(Intent(this, ManageKnockonsActivity::class.java))
                R.id.nav_statistics -> {
                }
                R.id.nav_help -> startActivity(Intent(this, HelpActivity::class.java))
            }

            val drawer = notification_history_DrawerLayout
            drawer!!.closeDrawer(GravityCompat.START)
            true
        }

        //endregion

        //region ==================================== Bottom Navigation =====================================

        notification_BottomNavigationView = findViewById(R.id.navigation)
        notification_BottomNavigationView!!.menu.getItem(2).isChecked = true
        notification_BottomNavigationView!!.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        //endregion

        //region ====================================== Worker Thread =======================================

        notification_history_mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        notification_history_mDbWorkerThread.start()

        //endregion

        //on get la base de données
        notification_history_NotificationsDatabase = ContactsRoomDatabase.getDatabase(this)
        updateFilter()

        //region ======================================== Listeners =========================================

        notification_history_SwipeRefreshLayout!!.setOnRefreshListener {
            swipeRefreshActivity()
        }

        settings_CallPopupSwitch!!.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val sharedCallPopupPreferences: SharedPreferences = getSharedPreferences("Phone_call", Context.MODE_PRIVATE)
                val edit: SharedPreferences.Editor = sharedCallPopupPreferences.edit()
                edit.putBoolean("popup", true)
                edit.apply()
            } else {
                val sharedCallPopupPreferences: SharedPreferences = getSharedPreferences("Phone_call", Context.MODE_PRIVATE)
                val edit: SharedPreferences.Editor = sharedCallPopupPreferences.edit()
                edit.putBoolean("popup", false)
                edit.apply()
            }
        }

        settings_left_drawer_ThemeSwitch!!.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                setTheme(R.style.AppThemeDark)
//                notification_history_MainLayout!!.setBackgroundResource(R.drawable.dark_background)
                val edit: SharedPreferences.Editor = sharedThemePreferences.edit()
                edit.putBoolean("darkTheme", true)
                edit.apply()
                startActivity(Intent(this@NotificationHistoryActivity, NotificationHistoryActivity::class.java))
            } else {
                setTheme(R.style.AppTheme)
//                notification_history_MainLayout!!.setBackgroundResource(R.drawable.mr_white_blur_background)
                val edit: SharedPreferences.Editor = sharedThemePreferences.edit()
                edit.putBoolean("darkTheme", false)
                edit.apply()
                startActivity(Intent(this@NotificationHistoryActivity, NotificationHistoryActivity::class.java))
            }
        }

        notification_history_ToolbarMultiSelectModeClose!!.setOnClickListener {
            refreshActivity()
        }

        notification_Search_TextView!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateFilter()
            }
        })

        notification_history_ToolbarMultiSelectModeDelete!!.setOnClickListener {
            MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                    .setTitle(getString(R.string.notification_history_delete_notifications))
                    .setMessage(getString(R.string.notification_history_delete_notifications_confirmation))
                    .setPositiveButton(R.string.edit_contact_validate) { _, _ ->
                        listOfItemSelected.forEach {
                            notification_history_NotificationsDatabase!!.notificationsDao().deleteNotificationById(it.id!!)
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

        //endregion
    }

    //region ========================================== Functions ===========================================

    private fun setContentView() {
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val height = size.y

        when {
            height > 2500 -> setContentView(R.layout.activity_notification_history)
            height in 1800..2499 -> setContentView(R.layout.activity_notification_history)
            height in 1100..1799 -> setContentView(R.layout.activity_notification_history_smaller_screen)
            height < 1099 -> setContentView(R.layout.activity_notification_history_mini_screen)
        }
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
    }

    private fun swipeRefreshActivity() {
        val handler = Handler()
        handler.postDelayed({ notification_history_SwipeRefreshLayout!!.isRefreshing = false }, 1000)
        refreshActivity()
    }

    /**
     * Met à jour la liste de contact en fonction de changement tri ou de filtre
     */
    fun updateFilter() {

        val sharedPreferences = getSharedPreferences("Notification_tri", Context.MODE_PRIVATE)

        if (sharedPreferences.getBoolean("filtre_message", true)) {

            notification_history_ListOfNotificationDB.removeAll(notification_history_ListOfNotificationDB)
            val stringSearch = notification_Search_TextView!!.text.toString().toLowerCase()
            if (stringSearch.isEmpty()) {
                notification_history_ListOfNotificationDB.addAll(notification_history_NotificationsDatabase?.notificationsDao()?.getAllNotifications() as ArrayList<NotificationDB>)
            } else {
                notification_history_ListOfNotificationDB.addAll(notification_history_NotificationsDatabase?.notificationsDao()?.getNotificationFiltered(stringSearch) as ArrayList<NotificationDB>)
                println("notification list after request" + notification_history_NotificationsDatabase?.notificationsDao()?.getNotificationFiltered(stringSearch))
            }
            val listTmp = mutableListOf<NotificationDB>()
            listTmp.addAll(notification_history_ListOfNotificationDB)
            listTmp.forEach {
                if (!isMessagingApp(it.platform)) {
                    notification_history_ListOfNotificationDB.remove(it)
                }
            }
        } else {
            notification_history_ListOfNotificationDB.removeAll(notification_history_ListOfNotificationDB)
            notification_history_ListOfNotificationDB.addAll(notification_history_NotificationsDatabase?.notificationsDao()?.getAllNotifications() as ArrayList<NotificationDB>)
            val listTmp = mutableListOf<NotificationDB>()
            val stringSearch = notification_Search_TextView!!.text.toString().toLowerCase()
            listTmp.addAll(notification_history_ListOfNotificationDB)
            if (stringSearch.isNotEmpty()) {
                val regex = (".*$stringSearch.*").toRegex()
                listTmp.forEach {
                    if (!it.contactName.toLowerCase().matches(regex) && !it.description.toLowerCase().matches(regex)) {
                        notification_history_ListOfNotificationDB.remove(it)
                    }
                }
            }
        }

        when {
            sharedPreferences.getString("tri", "date") == "date" -> {
                val listTmp = mutableListOf<NotificationDB>()

                for (i in 0 until notification_history_ListOfNotificationDB.size) {
                    listTmp.add(notification_history_ListOfNotificationDB[i])
                }

                initRecyclerView()
            }
            sharedPreferences.getString("tri", "date") == "priorite" -> {
                val listTmp: MutableList<NotificationDB> = notification_history_NotificationsDatabase?.notificationsDao()?.getContactWithPriority0And2() as MutableList<NotificationDB>
                val listTmp2 = mutableListOf<NotificationDB>()
                listTmp2.addAll(notification_history_ListOfNotificationDB)
                listTmp2.removeAll(listTmp)
                listTmp.addAll(firstContactPrio0(listTmp).coerceAtLeast(0), listTmp2)
                notification_history_ListOfNotificationDB.removeAll(notification_history_ListOfNotificationDB)
                notification_history_ListOfNotificationDB.addAll(listTmp)

                initRecyclerView()
            }
            sharedPreferences.getString("tri", "date") == "contact" -> {
                val listNotif: ArrayList<NotificationDB> = arrayListOf()
                listNotif.addAll(notification_history_NotificationsDatabase!!.notificationsDao().getNotifSortByContact())
                listNotif.retainAll(notification_history_ListOfNotificationDB)
                initRecyclerView()
            }
        }
    }

    private fun initRecyclerView() {
        notification_recyclerview_Adapter = NotificationsHistoryRecyclerViewAdapter(this, notification_history_ListOfNotificationDB)
        notification_history_RecyclerView!!.adapter = notification_recyclerview_Adapter
        notification_history_RecyclerView!!.layoutManager = LinearLayoutManager(this)
        notification_recyclerview_Adapter!!.notifyDataSetChanged()
        notification_history_RecyclerView!!.recycledViewPool.setMaxRecycledViews(0, 0)
        notification_history_RecyclerView!!.layoutManager!!.scrollToPosition(0)
    }

    private fun updateFilterMultiSelect() {

        val sharedPreferences = getSharedPreferences("Notification_tri", Context.MODE_PRIVATE)
        if (sharedPreferences.getBoolean("filtre_message", true)) {

            notification_history_ListOfNotificationDB.removeAll(notification_history_ListOfNotificationDB)
            val stringSearch = notification_Search_TextView!!.text.toString().toLowerCase()
            if (stringSearch.isEmpty()) {
                notification_history_ListOfNotificationDB.addAll(notification_history_NotificationsDatabase?.notificationsDao()?.getAllNotifications() as ArrayList<NotificationDB>)
            } else {
                notification_history_ListOfNotificationDB.addAll(notification_history_NotificationsDatabase?.notificationsDao()?.getNotificationFiltered(stringSearch) as ArrayList<NotificationDB>)
                println("notification list after request" + notification_history_NotificationsDatabase?.notificationsDao()?.getNotificationFiltered(stringSearch))
            }
            val listTmp = mutableListOf<NotificationDB>()
            listTmp.addAll(notification_history_ListOfNotificationDB)
            listTmp.forEach {
                if (!isMessagingApp(it.platform)) {
                    notification_history_ListOfNotificationDB.remove(it)
                }
            }
        } else {
            notification_history_ListOfNotificationDB.removeAll(notification_history_ListOfNotificationDB)
            notification_history_ListOfNotificationDB.addAll(notification_history_NotificationsDatabase?.notificationsDao()?.getAllNotifications() as ArrayList<NotificationDB>)
            val listTmp = mutableListOf<NotificationDB>()
            val stringSearch = notification_Search_TextView!!.text.toString().toLowerCase()
            listTmp.addAll(notification_history_ListOfNotificationDB)
            if (stringSearch.isNotEmpty()) {
                val regex = (".*$stringSearch.*").toRegex()
                listTmp.forEach {
                    if (!it.contactName.toLowerCase().matches(regex) && !it.description.toLowerCase().matches(regex)) {
                        notification_history_ListOfNotificationDB.remove(it)
                    }
                }
            }
        }
    }

    //region ======================================== recyclerClick =========================================

    fun recyclerSimpleClick(position: Int) {

        val gestionnaireContacts = ContactManager(this.applicationContext)

        val contact = gestionnaireContacts.getContact(notification_history_ListOfNotificationDB[position].contactName)

        when (notification_history_ListOfNotificationDB[position].platform) {
            "com.whatsapp" -> {
                if (contact != null) {
                    openWhatsapp(contact.getFirstPhoneNumber())
                } else {
                    openWhatsapp()
                }
            }

            "com.android.incallui" -> {
                phoneCall(notification_history_ListOfNotificationDB[position].contactName)
            }

            "com.google.android.gm" -> openGmail(this, gestionnaireContacts.getContact(notification_history_ListOfNotificationDB[position].contactName))

            "com.facebook.katana" -> goToFacebook()

            "com.facebook.orca" -> {
                if (contact != null) {
                    openMessenger(contact.getMessengerID(), this)
                } else {
                    openMessenger("", this)
                }
            }

            "com.google.android.apps.messaging", "com.android.mms", "com.samsung.android.messaging" -> {
                if (contact != null) {
                    val intent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("sms", contact.getFirstPhoneNumber(), null))
                    startActivity(intent)
                } else {
                    val intent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("sms", notification_history_ListOfNotificationDB[position].contactName, null))
                    startActivity(intent)
                }
                val sendIntent = Intent(Intent.ACTION_VIEW)
                sendIntent.data = Uri.parse("sms:")
            }

            "com.instagram.android" -> goToInstagramPage()

            "com.microsoft.office.outlook" -> goToOutlook()

            "com.twitter.android" -> goToTwitter()

            "com.skype.raider" -> goToSkype()

            "com.linkedin.android" -> goToLinkedin()
        }
    }

    fun recyclerLongClick(position: Int) {

        updateFilterMultiSelect()

        if (listOfItemSelected.contains(notification_history_ListOfNotificationDB[position])) {
            listOfItemSelected.remove(notification_history_ListOfNotificationDB[position])
        } else {
            listOfItemSelected.add(notification_history_ListOfNotificationDB[position])
        }

        val i = listOfItemSelected.size

        if (listOfItemSelected.size == 1 && firstClick) {
            Toast.makeText(this, R.string.main_toast_multi_select_actived, Toast.LENGTH_SHORT).show()
            firstClick = false
            multiSelectMode = true
            notification_history_ToolbarMultiSelectModeLayout!!.visibility = View.VISIBLE
            notification_history_ToolbarLayout!!.visibility = View.INVISIBLE

        } else if (listOfItemSelected.size == 0) {
            Toast.makeText(this, R.string.main_toast_multi_select_deactived, Toast.LENGTH_SHORT).show()

            notification_history_ToolbarMultiSelectModeLayout!!.visibility = View.GONE
            notification_history_ToolbarLayout!!.visibility = View.VISIBLE

            firstClick = true
        }

        if (listOfItemSelected.size == 1) {
            notification_history_ToolbarMultiSelectModeTitle!!.text = i.toString() + " " + getString(R.string.main_toast_multi_select_mode_selected)
        } else if (listOfItemSelected.size > 1) {
            notification_history_ToolbarMultiSelectModeTitle!!.text = i.toString() + " " + getString(R.string.main_toast_multi_select_mode_selected_more_than_one)
        }
    }

    //endregion

    private fun sendMail(addressMail: String, msg: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_EMAIL, addressMail)
        intent.data = Uri.parse("mailto:")
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, msg)

        startActivity(intent)
    }

    private fun sendMessageWithWhatsapp(phoneNumber: String, msg: String) {

        val intent = Intent(Intent.ACTION_VIEW)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        val message = "phone=" + converter06To33(phoneNumber)
        intent.data = Uri.parse("http://api.whatsapp.com/send?phone=$message&text=$msg")

        startActivity(intent)
    }

    private fun appIsInstalled(): Boolean {
        val pm = getPackageManager()
        return try {
            pm.getApplicationInfo("com.whatsapp", 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun openSms(phoneNumber: String, message: String) {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("sms", phoneNumber, null))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtra("sms_body", message)

        startActivity(intent)
    }

    //region open app TODO supprimer toutes ces méthodes useless pour la plupart des application
    private fun goToSkype() {
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("skype://skype"))
        try {
            startActivity(appIntent)
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://skype.com/")))
        }
    }

    private fun goToLinkedin() {
        /// don't work
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("linkedin://you"))
        try {
            startActivity(appIntent)
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://linkedin.com/")))
        }
    }

    private fun goToTwitter() {
        val appIntent = Intent(Intent.ACTION_VIEW)
        appIntent.setClassName("com.twitter.android", "com.twitter.android")
        try {
            startActivity(appIntent)
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://twitter.com/")))
        }
    }

    private fun goToOutlook() {
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("ms-outlook://emails"))
        try {
            startActivity(appIntent)
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://outlook.com/")))
        }
    }

    private fun goToFacebook() {
        val uri = Uri.parse("facebook:/newsfeed")
        val likeIng = Intent(Intent.ACTION_VIEW, uri)
        try {
            startActivity(likeIng)
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://facebook.com/")))
        }
    }

    private fun goToInstagramPage() {
        val uri = Uri.parse("https://www.instagram.com/")
        val likeIng = Intent(Intent.ACTION_VIEW, uri)

        likeIng.setPackage("com.instagram.android")

        try {
            startActivity(likeIng)
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://instagram.com/")))
        }
    }

    private fun openMessenger(id: String, context: Context) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.messenger.com/t/$id"))
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.messenger.com/t/$id"))
            context.startActivity(intent)
        }
    }

    private fun openGmail(context: Context, contact: ContactWithAllInformation?) {
        val i = context.packageManager.getLaunchIntentForPackage("com.google.android.gm")
        i!!.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(i)
    }

    private fun converter06To33(phoneNumber: String): String {
        return if (phoneNumber[0].toString() == "0") {
            val phoneNumberConvert = "+33" + phoneNumber.substring(0)
            phoneNumberConvert
        } else {
            phoneNumber
        }
    }

    private fun openWhatsapp(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        val message = "phone=" + converter06To33(phoneNumber)
        intent.data = Uri.parse("http://api.whatsapp.com/send?phone=$message")

        startActivity(intent)
    }

    private fun openWhatsapp() {
        val i = packageManager.getLaunchIntentForPackage("com.whatsapp")
        try {
            startActivity(i)
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://whatsapp.com/")))
        }
    }

    private fun phoneCall(phoneNumber: String) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), MAKE_CALL_PERMISSION_REQUEST_CODE)
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

    override fun onResume() {
        super.onResume()

        if (fromPopup) {
            refreshActivity()
        }
    }

//    private fun phoneCall() {
//        val i = packageManager.getLaunchIntentForPackage("com.android.incallui")
//        try {
//            startActivity(i)
//        } catch (e: ActivityNotFoundException) {
//        }
//    }

    //endregion

    private fun sendMessageWithAndroidMessage(phoneNumber: String, msg: String) {
        val smsManager = SmsManager.getDefault()
        smsManager.sendTextMessage(phoneNumber, null, msg, null, null)

        Toast.makeText(this, R.string.notif_adapter_message_sent,
                Toast.LENGTH_LONG).show()
    }

    private fun firstContactPrio0(notifList: List<NotificationDB>): Int {
        if (notifList.size > 1) {
            var i = notifList.size - 1
            while (i > 0) {
                val contact = notification_history_NotificationsDatabase!!.contactsDao().getContact(notifList[i].idContact)
                if (contact.contactDB!!.contactPriority != 0) {
                    return i + 1
                }
                i--
            }
        }
        return 0
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.toolbar_menu_filter_notif_history, menu)
        val sharedPreferences = getSharedPreferences("Notification_tri", Context.MODE_PRIVATE)
        when {
            sharedPreferences.getString("tri", "date") == "date" -> menu!!.findItem(R.id.notif_tri_par_date).isChecked = true
            sharedPreferences.getString("tri", "date") == "priorite" -> menu!!.findItem(R.id.notif_tri_par_priorite).isChecked = true
            else -> menu!!.findItem(R.id.notif_tri_par_contact).isChecked = true
        }
        if (!sharedPreferences.getBoolean("filtre_message", true)) {
            menu.findItem(R.id.messagefilter).isChecked = false
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                notification_history_DrawerLayout!!.openDrawer(GravityCompat.START)
                return true
            }
            R.id.notif_tri_par_date -> {
                val sharedPreferences = getSharedPreferences("Notification_tri", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString("tri", "date")
                editor.apply()
                item.isChecked = true
                updateFilter()
                // this.recreate()
            }
            R.id.notif_tri_par_priorite -> {
                val sharedPreferences = getSharedPreferences("Notification_tri", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString("tri", "priorite")
                editor.apply()
                item.isChecked = true

                updateFilter()
            }
            R.id.notif_tri_par_contact -> {
                val sharedPreferences = getSharedPreferences("Notification_tri", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString("tri", "contact")
                editor.apply()
                updateFilter()
                item.isChecked = true
            }
            R.id.item_help -> {
                if (Resources.getSystem().configuration.locale.language == "fr") {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.yellowtwigs.com/aide-en-ligne-historique"))
                    startActivity(browserIntent)
                } else {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.yellowtwigs.com/help-history"))
                    startActivity(browserIntent)
                }
                finish()
            }
            R.id.messagefilter -> {
                val sharedPreferences = getSharedPreferences("Notification_tri", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                if (item.isChecked) {
                    editor.putBoolean("filtre_message", false)
                    item.isChecked = false
                } else {
                    editor.putBoolean("filtre_message", true)
                    item.isChecked = true
                }
                editor.apply()
                //  this.recreate()
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
            NotificationListener.MESSAGE_PACKAGE, NotificationListener.MESSAGE_SAMSUNG_PACKAGE, NotificationListener.XIAOMI_MESSAGE_PACKAGE -> true
            NotificationListener.TELEGRAM_PACKAGE -> true
            NotificationListener.INSTAGRAM_PACKAGE -> true
            else -> false
        }
    }

    /*
        fun longNotifHistoryListItemClick(position: Int) {
            val notifSelected = notification_history_ListOfNotificationDB[position]
            if (listOfItemSelected.contains(notification_history_NotificationsDatabase!!.notificationsDao().getNotification(notifSelected.id!!))) {
                listOfItemSelected.remove(notification_history_NotificationsDatabase!!.notificationsDao().getNotification(notifSelected.id))
            } else {
                listOfItemSelected.add(notification_history_NotificationsDatabase!!.notificationsDao().getNotification(notifSelected.id))
            }

            if (listOfItemSelected.size == 1 && firstClick) {
                Toast.makeText(this, R.string.main_toast_multi_select_actived, Toast.LENGTH_LONG).show()
                firstClick = false
                multiSelectMode = true
                notification_history_ToolbarLayout!!.visibility = View.INVISIBLE
                notification_history_ToolbarMultiSelectModeLayout!!.visibility = View.VISIBLE

            } else if (listOfItemSelected.size == 0) {
                Toast.makeText(this, R.string.main_toast_multi_select_deactived, Toast.LENGTH_LONG).show()

                notification_history_ToolbarLayout!!.visibility = View.VISIBLE
                notification_history_ToolbarMultiSelectModeLayout!!.visibility = View.GONE

                firstClick = true
                multiSelectMode = false
            }
        }
    */
    private fun refreshActivity() {
        startActivity(Intent(this@NotificationHistoryActivity, NotificationHistoryActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
        finish()
    }

    //endregion

    /**
     * Suppression de toues les notifications systèmes
     */

    private fun deleteAllNotifSystem() {
        val listTmp = mutableListOf<NotificationDB>()
        listTmp.addAll(notification_history_ListOfNotificationDB)
        listTmp.forEach {
            if (!isMessagingApp(it.platform)) {
                it.id?.let { it1 -> notification_history_NotificationsDatabase?.notificationsDao()?.deleteNotificationById(it1) }
            }
        }
        refreshActivity()
    }

    /**
     * Suppression de toues les notifications
     */
    private fun deleteAllNotif() {
        notification_history_NotificationsDatabase?.notificationsDao()?.deleteAllNotification()
        refreshActivity()
    }
}

class WrapContentLinearLayoutManager(context: Context?) : LinearLayoutManager(context) {
    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        try {
            super.onLayoutChildren(recycler, state)
        } catch (e: IndexOutOfBoundsException) {
            Log.e("Error", "IndexOutOfBoundsException in Recycler View")
        }
    }
}

