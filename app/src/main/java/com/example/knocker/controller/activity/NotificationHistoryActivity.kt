package com.example.knocker.controller.activity

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.knocker.model.DbWorkerThread
import com.example.knocker.model.ModelDB.NotificationDB
import com.example.knocker.R
import com.example.knocker.controller.NotificationHistoryAdapterActivity
import com.example.knocker.controller.NotificationListener
import com.example.knocker.model.ContactList
import com.example.knocker.model.ContactsRoomDatabase
import com.example.knocker.model.ModelDB.ContactWithAllInformation
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

/**
 * La Classe qui permet d'afficher l'historique des notifications
 * @author Florian Striebel
 */
class NotificationHistoryActivity : AppCompatActivity() {

    //region ========================================== Val or Var ==========================================

    private var notification_history_DrawerLayout: DrawerLayout? = null
    private var main_BottomNavigationView: BottomNavigationView? = null
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->

        println("r.id =" + R.id.notif_tri_par_priorite + " item.id=" + item.itemId + " ")
        when (item.itemId) {
            R.id.navigation_phone_book -> {
                startActivity(Intent(this@NotificationHistoryActivity, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_groups -> {
            }
            R.id.navigation_notifcations -> {
            }
//            R.id.navigation_socials_networks -> {
//                startActivity(Intent(this@NotificationHistoryActivity, SocialsNetworksLinksActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
//                return@OnNavigationItemSelectedListener true
//            }
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
        setContentView(R.layout.activity_notification_history)

        main_BottomNavigationView = findViewById(R.id.navigation)
        main_BottomNavigationView!!.menu.getItem(1).isChecked = true
        main_BottomNavigationView!!.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        //region ========================================= Toolbar ==========================================

        val toolbar = findViewById<Toolbar>(R.id.notification_history_toolbar)
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar
        actionbar!!.title = this.resources.getString(R.string.bottom_navigation_view_notify_history)
        actionbar.run {
            actionbar.title = resources.getString(R.string.bottom_navigation_view_notify_history)
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
                R.id.nav_address_book -> {
                    startActivity(Intent(this, MainActivity::class.java))
                }
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

            val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
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
        val sharedPreferences = getSharedPreferences("Notification_tri", Context.MODE_PRIVATE)
        if (sharedPreferences.getBoolean("filtre_message", true)) {
            notification_history_ListOfNotificationDB.addAll(notification_history_NotificationsDatabase?.notificationsDao()?.getAllnotifications() as ArrayList<NotificationDB>)

            println("BALISE = $notification_history_ListOfNotificationDB")

            val listTmp = mutableListOf<NotificationDB>()

            listTmp.addAll(notification_history_ListOfNotificationDB)
            listTmp.forEach {
                if (!isMessagingApp(it.platform)) {
                    notification_history_ListOfNotificationDB.remove(it)
                }
            }
        } else {
            notification_history_ListOfNotificationDB.addAll(notification_history_NotificationsDatabase?.notificationsDao()?.getAllnotifications() as ArrayList<NotificationDB>)
        }

        if (sharedPreferences.getString("tri", "date") == "date") {

            val adapter = NotificationHistoryAdapterActivity(this, notification_history_ListOfNotificationDB)
            notification_history_ListView = findViewById(R.id.listView_notification_history)
            notification_history_ListView!!.adapter = adapter

            println("taille list " + notification_history_ListOfNotificationDB.size + " content " + notification_history_ListOfNotificationDB.toString())

        } else if (sharedPreferences.getString("tri", "date") == "priorite") {

            val listTmp: MutableList<NotificationDB> = notification_history_NotificationsDatabase?.notificationsDao()?.testPriority() as MutableList<NotificationDB>
            val listTmp2 = mutableListOf<NotificationDB>()

            listTmp2.addAll(notification_history_ListOfNotificationDB)
            listTmp2.removeAll(listTmp)
            println("TEEEEEEEST = " + listTmp2.size)

            listTmp.addAll(Math.max(firstContactPrio0(listTmp) - 1, 0), listTmp2)

            val adapter = NotificationHistoryAdapterActivity(this, listTmp)
            notification_history_ListView = findViewById(R.id.listView_notification_history)
            notification_history_ListView!!.adapter = adapter

        } else {
            println("thats a problem")
        }

        notification_history_ListView!!.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val gestionnaireContacts = ContactList(this.applicationContext)
            val iterator = (1 until gestionnaireContacts.contacts.size).iterator()


            when {
                notification_history_ListOfNotificationDB[position].platform == "com.whatsapp" -> iterator.forEach {
                    if (notification_history_ListOfNotificationDB[position].contactName == gestionnaireContacts.contacts[iterator.nextInt()].contactDB!!.firstName) {
                        openWhatsapp(converter06To33(gestionnaireContacts.contacts[iterator.nextInt()].getPhoneNumber()), baseContext)
                    }
                }
                notification_history_ListOfNotificationDB[position].platform == "com.google.android.gm" -> iterator.forEach { _ ->
                    openGmail(this)
                }
                notification_history_ListOfNotificationDB[position].platform == "com.facebook.katana" -> iterator.forEach { _ ->
                    goToFacebook()
                }
                notification_history_ListOfNotificationDB[position].platform == "com.facebook.orca" -> iterator.forEach { _ ->
                    openMessenger("", this)
                }
                notification_history_ListOfNotificationDB[position].platform == "com.google.android.apps.messaging" -> iterator.forEach { _ ->
                }
                notification_history_ListOfNotificationDB[position].platform == "com.instagram.android" -> iterator.forEach { _ ->
                    goToInstagramPage()
                }
                notification_history_ListOfNotificationDB[position].platform == "com.twitter.android" -> iterator.forEach { _ ->
                    goToTwitter()
                }
            }
        }
    }

    //region ========================================== Functions ==========================================

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

    fun openMessenger(id: String, context: Context) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.messenger.com/t/" + id));
            context.startActivity(intent);
        } catch (e: ActivityNotFoundException) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.messenger.com/t/" + id));
            context.startActivity(intent);
        }
    }

    fun openGmail(context: Context) {
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

    fun openWhatsapp(contact: CharSequence, context: Context) {
        val url = "https://api.whatsapp.com/send?phone=$contact"
        try {
            val pm = context.packageManager
            pm.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES)
            val i = Intent(Intent.ACTION_VIEW)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK;
            i.data = Uri.parse(url)
            context.startActivity(i)
        } catch (e: PackageManager.NameNotFoundException) {
            Toast.makeText(context, "Whatsapp app not installed in your phone", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    fun firstContactPrio0(notifList: List<NotificationDB>): Int {
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
        inflater.inflate(R.menu.menu_notification, menu)////////////////////////////////////////////////////
        val sharedPreferences = getSharedPreferences("Notification_tri", Context.MODE_PRIVATE)
        if (sharedPreferences.getString("tri", "date") == "date") {
            menu!!.findItem(R.id.notif_tri_par_date).setChecked(true)
        } else if (sharedPreferences.getString("tri", "date") == "priorite") {

            menu!!.findItem(R.id.notif_tri_par_priorite).setChecked(true)
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
                this.recreate()
            }
            R.id.notif_tri_par_priorite -> {
                println("tri par priorité checked")
                val sharedPreferences = getSharedPreferences("Notification_tri", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString("tri", "priorite")
                editor.apply()
                item.setChecked(true)
                this.recreate()
            }
            R.id.item_help -> {
                val alertDialogBuilder = android.app.AlertDialog.Builder(this)
                alertDialogBuilder.setMessage(this.resources.getString(R.string.help_history))
                alertDialogBuilder.show()
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
                this.recreate()
            }
        }
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

    //endregion
}