package com.yellowtwigs.knockin.controller.activity

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.telephony.SmsManager
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import com.baoyz.swipemenulistview.SwipeMenu
import com.baoyz.swipemenulistview.SwipeMenuCreator
import com.baoyz.swipemenulistview.SwipeMenuItem
import com.baoyz.swipemenulistview.SwipeMenuListView
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.controller.NotificationsHistoryListViewAdapter
import com.yellowtwigs.knockin.controller.NotificationListener
import com.yellowtwigs.knockin.controller.activity.firstLaunch.TutorialActivity
import com.yellowtwigs.knockin.controller.activity.group.GroupManagerActivity
import com.yellowtwigs.knockin.model.ContactManager
import com.yellowtwigs.knockin.model.ContactsRoomDatabase
import com.yellowtwigs.knockin.model.DbWorkerThread
import com.yellowtwigs.knockin.model.ModelDB.ContactWithAllInformation
import com.yellowtwigs.knockin.model.ModelDB.NotificationDB
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView


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

    private var notification_Adapter: NotificationsHistoryListViewAdapter? = null
    private var notification_history_ListView: SwipeMenuListView? = null

    private var notification_Adapter: NotificationsHistoryRecyclerViewAdapter? = null
    private var notification_history_RecyclerView: RecyclerView? = null

    private var notification_history_NotificationsDatabase: ContactsRoomDatabase? = null
    private lateinit var notification_history_mDbWorkerThread: DbWorkerThread

    private val notification_history_ListOfNotificationDB = mutableListOf<NotificationDB>()
    private var fromPopup: Boolean = false

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

        setContentView(R.layout.activity_notification_history)

        //region ====================================== FindViewById =======================================

        notification_history_ToolbarLayout = findViewById(R.id.notification_history_toolbar_layout)
        notification_Search_TextView = findViewById(R.id.notification_history_search_bar)

        notification_history_ToolbarMultiSelectModeLayout = findViewById(R.id.notification_history_toolbar_multi_select_mode_layout)
        notification_history_ToolbarMultiSelectModeClose = findViewById(R.id.notification_history_toolbar_multi_select_mode_close)
        notification_history_ToolbarMultiSelectModeDelete = findViewById(R.id.notification_history_toolbar_multi_select_mode_delete)

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

            val drawer = findViewById<DrawerLayout>(R.id.notification_history_drawer_layout)
            drawer.closeDrawer(GravityCompat.START)
            true
        }

        //endregion

        //region ====================================== BottomNavigation =======================================

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

        notification_history_ListView!!.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val gestionnaireContacts = ContactManager(this.applicationContext)

            val contact = gestionnaireContacts.getContact(notification_history_ListOfNotificationDB[position].contactName)

            when (notification_history_ListOfNotificationDB[position].platform) {
                "com.whatsapp" -> {
                    if (contact != null) {
                        openWhatsapp(contact.getFirstPhoneNumber())
                    }
                }

                "com.google.android.gm" -> openGmail(this, gestionnaireContacts.getContact(notification_history_ListOfNotificationDB[position].contactName))

                "com.facebook.katana" -> goToFacebook()

                "com.facebook.orca" -> openMessenger("", this)

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

        notification_history_ListView!!.onItemLongClickListener = AdapterView.OnItemLongClickListener { _, _, position, _ ->

            val gestionnaireContacts = ContactManager(this.applicationContext)

            val contact = gestionnaireContacts.getContact(notification_history_ListOfNotificationDB[position].contactName)

            val platform = notification_history_ListOfNotificationDB[position].platform
            var alertView: View? = null

            if (platform == "com.whatsapp" || platform == "com.google.android.apps.messaging"
                    || platform == "com.android.mms" || platform == "com.samsung.android.messaging") {
                val inflater: LayoutInflater = this.layoutInflater
                alertView = inflater.inflate(R.layout.alert_dialog_notif_details_messaging, null)

                var alert_dialog_notif_details_PlatformImageView: AppCompatImageView? = null
                var alert_dialog_notif_details_Title: TextView? = null
                var alert_dialog_notif_details_Message: TextView? = null

                var alert_dialog_notif_details_EditText: AppCompatEditText? = null
                var alert_dialog_notif_details_Send: AppCompatImageView? = null

                alert_dialog_notif_details_PlatformImageView = alertView!!.findViewById(R.id.alert_dialog_notif_details_messaging_platform_image)
                alert_dialog_notif_details_Title = alertView.findViewById(R.id.alert_dialog_notif_details_messaging_platform_name)
                alert_dialog_notif_details_Message = alertView.findViewById(R.id.alert_dialog_notif_details_messaging_content)
                alert_dialog_notif_details_EditText = alertView.findViewById(R.id.alert_dialog_notif_details_messaging_edit_text)
                alert_dialog_notif_details_Send = alertView.findViewById(R.id.alert_dialog_notif_details_messaging_send)

                when (notification_history_ListOfNotificationDB[position].platform) {

                    "com.whatsapp" -> alert_dialog_notif_details_PlatformImageView.setImageResource(R.drawable.ic_circular_whatsapp)

                    "com.google.android.gm" -> alert_dialog_notif_details_PlatformImageView.setImageResource(R.drawable.ic_gmail)

                    "com.facebook.katana" -> alert_dialog_notif_details_PlatformImageView.setImageResource(R.drawable.ic_messenger_circle_menu)

                    "com.facebook.orca" -> alert_dialog_notif_details_PlatformImageView.setImageResource(R.drawable.ic_facebook)

                    "com.google.android.apps.messaging", "com.android.mms", "com.samsung.android.messaging" -> alert_dialog_notif_details_PlatformImageView!!.setImageResource(R.drawable.ic_sms)

                    "com.instagram.android" -> alert_dialog_notif_details_PlatformImageView.setImageResource(R.drawable.ic_instagram)

                    "com.microsoft.office.outlook" -> alert_dialog_notif_details_PlatformImageView.setImageResource(R.drawable.ic_outlook)

                    "com.twitter.android" -> alert_dialog_notif_details_PlatformImageView.setImageResource(R.drawable.ic_twitter)

                    "com.skype.raider" -> alert_dialog_notif_details_PlatformImageView.setImageResource(R.drawable.ic_skype)

                    "com.linkedin.android" -> alert_dialog_notif_details_PlatformImageView.setImageResource(R.drawable.ic_linkedin)
                }

                alert_dialog_notif_details_Title!!.text = notification_history_ListOfNotificationDB[position].contactName
                alert_dialog_notif_details_Message!!.text = notification_history_ListOfNotificationDB[position].description

                MaterialAlertDialogBuilder(this)
                        .setView(alertView)
                        .show()

                alert_dialog_notif_details_Send?.setOnClickListener {

                    when (notification_history_ListOfNotificationDB[position].platform) {
                        "com.whatsapp" -> {
                            if (alert_dialog_notif_details_EditText!!.text!!.isNotEmpty()) {
                                if (contact != null) {
                                    sendMessageWithWhatsapp(contact.getFirstPhoneNumber(), alert_dialog_notif_details_EditText.text.toString())
                                }
                            } else {
                                Toast.makeText(this, R.string.multi_channel_empty_field, Toast.LENGTH_SHORT).show()
                            }
                        }

                        "com.google.android.gm" -> {
                            if (alert_dialog_notif_details_EditText!!.text!!.isNotEmpty()) {
                                if (contact != null) {
                                    openGmail(this, gestionnaireContacts.getContact(notification_history_ListOfNotificationDB[position].contactName))
                                }
                            } else {
                                Toast.makeText(this, R.string.multi_channel_empty_field, Toast.LENGTH_SHORT).show()
                            }
                        }

                        "com.google.android.apps.messaging", "com.android.mms", "com.samsung.android.messaging" -> {

                            if (checkPermission(Manifest.permission.SEND_SMS)) {
                                if (alert_dialog_notif_details_EditText!!.text!!.isNotEmpty()) {
                                    if (contact != null) {
                                        sendMessageWithAndroidMessage(contact.getFirstPhoneNumber(), alert_dialog_notif_details_EditText.text.toString())
                                    } else {
                                        sendMessageWithAndroidMessage(notification_history_ListOfNotificationDB[position].contactName, alert_dialog_notif_details_EditText.text.toString())
                                    }
                                } else {
                                    Toast.makeText(this, R.string.multi_channel_empty_field, Toast.LENGTH_SHORT).show()
                                }
                                refreshActivity()
                            } else {
                                //TODO In english
                                Toast.makeText(this, "Vous n'avez pas autorisé l'envoi de SMS via Knockin", Toast.LENGTH_LONG).show()

                                if (contact != null) {
                                    openSms(contact.getFirstPhoneNumber(), alert_dialog_notif_details_EditText!!.text.toString())
                                } else {
                                    openSms(notification_history_ListOfNotificationDB[position].contactName, alert_dialog_notif_details_EditText!!.text.toString())
                                }
                            }
                        }
                    }
                }
            }

            true
        }

        notification_Search_TextView!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateFilter()
            }
        })

        //endregion
    }

    //region ========================================== Functions ==========================================

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

    override fun onResume() {
        super.onResume()

        if (fromPopup) {
            refreshActivity()
        }
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
                startActivity(Intent(this@NotificationHistoryActivity, TutorialActivity::class.java).putExtra("fromNotificationHistoryActivity", true))
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
            NotificationListener.MESSAGE_PACKAGE, NotificationListener.MESSAGE_SAMSUNG_PACKAGE -> true
            NotificationListener.TELEGRAM_PACKAGE -> true
            else -> false
        }
    }

    /**
     * Met à jour la liste de contact en fonction de changement tri ou de filtre
     */
    private fun updateFilter() {

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

                notification_Adapter = NotificationsHistoryListViewAdapter(this, notification_history_ListOfNotificationDB)
                notification_history_ListView = findViewById(R.id.listView_notification_history)
                notification_history_ListView!!.adapter = notification_Adapter
                swipeMenuCreator(notification_history_ListView!!)
            }
            sharedPreferences.getString("tri", "date") == "priorite" -> {

                val listTmp: MutableList<NotificationDB> = notification_history_NotificationsDatabase?.notificationsDao()?.getContactWithPriority0And2() as MutableList<NotificationDB>
                val listTmp2 = mutableListOf<NotificationDB>()

                listTmp2.addAll(notification_history_ListOfNotificationDB)
                listTmp2.removeAll(listTmp)

                listTmp.addAll(firstContactPrio0(listTmp).coerceAtLeast(0), listTmp2)
                notification_history_ListOfNotificationDB.removeAll(notification_history_ListOfNotificationDB)
                notification_history_ListOfNotificationDB.addAll(listTmp)

                notification_Adapter = NotificationsHistoryListViewAdapter(this, notification_history_ListOfNotificationDB)
                notification_history_ListView = findViewById(R.id.listView_notification_history)
                notification_history_ListView!!.adapter = notification_Adapter

                swipeMenuCreator(notification_history_ListView!!)
            }
            sharedPreferences.getString("tri", "date") == "contact" -> {

                val listNotif: ArrayList<NotificationDB> = arrayListOf()
                listNotif.addAll(notification_history_NotificationsDatabase!!.notificationsDao().getNotifSortByContact())
                listNotif.retainAll(notification_history_ListOfNotificationDB)
                notification_history_ListOfNotificationDB.clear()
                notification_history_ListOfNotificationDB.addAll(listNotif)
                notification_Adapter = NotificationsHistoryListViewAdapter(this, notification_history_ListOfNotificationDB)
                notification_history_ListView = findViewById(R.id.listView_notification_history)
                notification_history_ListView!!.adapter = notification_Adapter

                swipeMenuCreator(notification_history_ListView!!)
            }
            else -> println("thats a problem test")

        }
        notification_history_ListView!!.isScrollingCacheEnabled = false
    }

    fun swipeMenuCreator(listView: SwipeMenuListView) {
        val creator = SwipeMenuCreator {

            val deleteItem = SwipeMenuItem(applicationContext)
            deleteItem.setIcon(R.drawable.ic_swipe_delete)
            deleteItem.width = convertDipToPixels(50F)
            it.addMenuItem(deleteItem)
        }

        listView.setMenuCreator(creator)

        listView.setOnMenuItemClickListener { position: Int, _: SwipeMenu?, index: Int ->
            when (index) {
                0 -> {
                    notification_history_NotificationsDatabase!!.notificationsDao().deleteNotificationById(notification_history_ListOfNotificationDB[position].id!!)
                    notification_history_ListOfNotificationDB.removeAt(position)
                    notification_Adapter!!.updateList(notification_history_ListOfNotificationDB)
                    notification_Adapter!!.notifyDataSetChanged()
                    refreshActivity()
                }
            }
            true
        }
    }

    fun convertDipToPixels(dips: Float): Int {
        return (dips * applicationContext.resources.displayMetrics.density + 0.5F).toInt()
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
}