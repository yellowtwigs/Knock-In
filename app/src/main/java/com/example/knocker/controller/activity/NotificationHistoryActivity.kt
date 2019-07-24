package com.example.knocker.controller.activity

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.knocker.R
import com.example.knocker.controller.NotificationHistoryAdapter
import com.example.knocker.controller.NotificationListener
import com.example.knocker.controller.activity.group.GroupActivity
import com.example.knocker.controller.activity.group.GroupManagerActivity
import com.example.knocker.model.ContactList
import com.example.knocker.model.ContactsRoomDatabase
import com.example.knocker.model.DbWorkerThread
import com.example.knocker.model.ModelDB.ContactWithAllInformation
import com.example.knocker.model.ModelDB.NotificationDB
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
    private var notification_Search_TextView: TextView? = null
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->

        when (item.itemId) {
            R.id.navigation_contacts -> {
                startActivity(Intent(this@NotificationHistoryActivity, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_groups -> {
                startActivity(Intent(this@NotificationHistoryActivity, GroupActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifcations -> {
            }
            R.id.navigation_phone_keyboard -> {
                startActivity(Intent(this@NotificationHistoryActivity, PhoneLogActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
                return@OnNavigationItemSelectedListener true
            }

        }
        false
    }

    private var notification_history_NotificationsDatabase: ContactsRoomDatabase? = null
    private lateinit var notification_history_mDbWorkerThread: DbWorkerThread

    private val notification_history_ListOfNotificationDB = mutableListOf<NotificationDB>()

    private var notification_history_ListView: ListView? = null


    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedThemePreferences = getSharedPreferences("Knocker_Theme", Context.MODE_PRIVATE)
        if (sharedThemePreferences.getBoolean("darkTheme", false)) {
            setTheme(R.style.AppThemeDark)
        } else {
            setTheme(R.style.AppTheme)
        }
        setContentView(R.layout.activity_notification_history)
        notification_Search_TextView = findViewById(R.id.notification_history_search_bar)
        notification_BottomNavigationView = findViewById(R.id.navigation)
        notification_BottomNavigationView!!.menu.getItem(2).isChecked = true
        notification_BottomNavigationView!!.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        //region ========================================= Toolbar ==========================================

        val toolbar = findViewById<Toolbar>(R.id.notification_history_toolbar)
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar
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
        val nav_item = menu.findItem(R.id.nav_screen_config)
        nav_item.isChecked = true
        navigationView!!.menu.getItem(0).isChecked = true

        navigationView.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            notification_history_DrawerLayout!!.closeDrawers()

            when (menuItem.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                }
                R.id.nav_groups -> startActivity(Intent(this@NotificationHistoryActivity, GroupManagerActivity::class.java))
                R.id.nav_informations -> startActivity(Intent(this, EditInformationsActivity::class.java))
                R.id.nav_notif_config -> startActivity(Intent(this, ManageNotificationActivity::class.java))
                R.id.nav_screen_config -> startActivity(Intent(this, ManageMyScreenActivity::class.java))
                R.id.nav_data_access -> {
                }
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

        //region ====================================== Worker Thread =======================================

        notification_history_mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        notification_history_mDbWorkerThread.start()

        //endregion

        //on get la base de données
        notification_history_NotificationsDatabase = ContactsRoomDatabase.getDatabase(this)
        updateFilter()

        //region ======================================== Listeners =========================================

        notification_history_ListView!!.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val gestionnaireContacts = ContactList(this.applicationContext)

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
                    println("into messaging")
                    if (contact != null) {
                        val intent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("sms", contact.getFirstPhoneNumber(), null))
                        startActivity(intent)
                    } else {
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

        notification_Search_TextView!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateFilter()
            }
        }

        )
        //endregion
    }

    //region ========================================== Functions ==========================================

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
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.messenger.com/t/" + id))
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.messenger.com/t/" + id))
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

    private fun firstContactPrio0(notifList: List<NotificationDB>): Int {
        for (i in 0..notifList.size - 1) {
            val contact = notification_history_NotificationsDatabase!!.contactsDao().getContact(notifList.get(i).idContact)
            if (contact.contactDB!!.contactPriority == 0) {
                return i
            }
        }
        return notifList.size
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_notifications, menu)
        val sharedPreferences = getSharedPreferences("Notification_tri", Context.MODE_PRIVATE)
        if (sharedPreferences.getString("tri", "date") == "date") {
            menu!!.findItem(R.id.notif_tri_par_date).setChecked(true)
        } else if (sharedPreferences.getString("tri", "date") == "priorite") {

            menu!!.findItem(R.id.notif_tri_par_priorite).setChecked(true)
        } else if (sharedPreferences.getBoolean("filtre_message", true)) {
            menu!!.findItem(R.id.notif_tri_par_contact)
        }
        if (!sharedPreferences.getBoolean("filtre_message", true)) {
            menu!!.findItem(R.id.messagefilter).setChecked(false)
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
                println("tri par priorité checked")
                val sharedPreferences = getSharedPreferences("Notification_tri", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString("tri", "date")
                editor.apply()
                item.setChecked(true)
                updateFilter()
                // this.recreate()
            }
            R.id.notif_tri_par_priorite -> {
                println("tri par priorité checked")
                val sharedPreferences = getSharedPreferences("Notification_tri", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString("tri", "priorite")
                editor.apply()
                item.isChecked = true

                updateFilter()
                //   this.recreate()
            }
            R.id.notif_tri_par_contact -> {
                val sharedPreferences = getSharedPreferences("Notification_tri", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString("tri", "contact")
                editor.apply()
                updateFilter()
            }
            R.id.item_help -> {
                MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.help)
                        .setBackground(getDrawable(R.color.backgroundColor))
                        .setMessage(this.resources.getString(R.string.help_notification_history))
                        .show()
            }
            R.id.messagefilter -> {
                val sharedPreferences = getSharedPreferences("Notification_tri", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                if (item.isChecked) {
                    editor.putBoolean("filtre_message", false)
                    item.setChecked(false)
                } else {
                    editor.putBoolean("filtre_message", true)
                    item.setChecked(true)
                }
                editor.apply()
                //  this.recreate()
            }
        }
        updateFilter()
        return super.onOptionsItemSelected(item)
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

    private fun updateFilter() {

        val sharedPreferences = getSharedPreferences("Notification_tri", Context.MODE_PRIVATE)
        if (sharedPreferences.getBoolean("filtre_message", true)) {
            notification_history_ListOfNotificationDB.removeAll(notification_history_ListOfNotificationDB)
            notification_history_ListOfNotificationDB.addAll(notification_history_NotificationsDatabase?.notificationsDao()?.getAllnotifications() as ArrayList<NotificationDB>)

            val listTmp = mutableListOf<NotificationDB>()
            listTmp.addAll(notification_history_ListOfNotificationDB)

            val stringSearch = notification_Search_TextView!!.text.toString().toLowerCase()
            listTmp.addAll(notification_history_ListOfNotificationDB)
            if (!stringSearch.isEmpty()) {
                val regex = (".*(" + stringSearch + ").*").toRegex()
                listTmp.forEach {
                    if (!(it.contactName.toLowerCase().matches(regex) || it.description.toLowerCase().matches(regex)) || !isMessagingApp(it.platform)) {
                        notification_history_ListOfNotificationDB.remove(it)
                    }
                }
            } else {
                listTmp.forEach {
                    if (!isMessagingApp(it.platform)) {
                        notification_history_ListOfNotificationDB.remove(it)
                    }
                }
            }

        } else {
            notification_history_ListOfNotificationDB.removeAll(notification_history_ListOfNotificationDB)
            notification_history_ListOfNotificationDB.addAll(notification_history_NotificationsDatabase?.notificationsDao()?.getAllnotifications() as ArrayList<NotificationDB>)
            val listTmp = mutableListOf<NotificationDB>()
            val stringSearch = notification_Search_TextView!!.text.toString().toLowerCase()
            listTmp.addAll(notification_history_ListOfNotificationDB)
            if (!stringSearch.isEmpty()) {
                val regex = (".*" + stringSearch + ".*").toRegex()
                listTmp.forEach {
                    if (!it.contactName.toLowerCase().matches(regex) && !it.description.toLowerCase().matches(regex)) {
                        notification_history_ListOfNotificationDB.remove(it)
                    }
                }
            }
        }

        when {
            sharedPreferences.getString("tri", "date") == "date" -> {

                val adapter = NotificationHistoryAdapter(this, notification_history_ListOfNotificationDB)
                notification_history_ListView = findViewById(R.id.listView_notification_history)
                notification_history_ListView!!.adapter = adapter

            }
            sharedPreferences.getString("tri", "date") == "priorite" -> {

                val listTmp: MutableList<NotificationDB> = notification_history_NotificationsDatabase?.notificationsDao()?.getContactWithPriority0And2() as MutableList<NotificationDB>
                val listTmp2 = mutableListOf<NotificationDB>()

                listTmp2.addAll(notification_history_ListOfNotificationDB)
                listTmp2.removeAll(listTmp)

                listTmp.addAll(Math.max(firstContactPrio0(listTmp), 0), listTmp2)
                notification_history_ListOfNotificationDB.removeAll(notification_history_ListOfNotificationDB)
                notification_history_ListOfNotificationDB.addAll(listTmp)
                val adapter = NotificationHistoryAdapter(this, notification_history_ListOfNotificationDB)
                notification_history_ListView = findViewById(R.id.listView_notification_history)
                notification_history_ListView!!.adapter = adapter

            }
            sharedPreferences.getString("tri", "date") == "contact" -> {

                val listNotif: ArrayList<NotificationDB> = arrayListOf()
                listNotif.addAll(notification_history_NotificationsDatabase!!.notificationsDao().getNotifSortByContact())
                listNotif.retainAll(notification_history_ListOfNotificationDB)
                val adapter = NotificationHistoryAdapter(this, listNotif)
                notification_history_ListView = findViewById(R.id.listView_notification_history)
                notification_history_ListView!!.adapter = adapter
            }
            else -> println("thats a problem test")

        }
    }
    //endregion
}