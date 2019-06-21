package com.example.knocker.controller.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.knocker.model.DbWorkerThread
import com.example.knocker.model.ModelDB.NotificationDB
import com.example.knocker.R
import com.example.knocker.controller.NotificationHistoryAdapterActivity
import com.example.knocker.controller.NotificationListener
import com.example.knocker.model.ContactsRoomDatabase
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

/**
 * La Classe qui permet d'afficher l'historique des notifications
 * @author Florian Striebel
 */
class  NotificationHistoryActivity : AppCompatActivity() {

    private var contact_details_NotificationsDatabase: ContactsRoomDatabase? = null
    private lateinit var contact_details_mDbWorkerThread: DbWorkerThread
    private var drawerLayout: DrawerLayout? = null
    private val list = mutableListOf<NotificationDB>()

    private var main_BottomNavigationView: BottomNavigationView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_history)
        // on init WorkerThread
        val toolbar= findViewById<Toolbar>(R.id.notif_toolbar)
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar
        actionbar!!.title=this.resources.getString(R.string.bottom_navigation_view_notify_history)
        actionbar.setDisplayHomeAsUpEnabled(true)
        actionbar.setHomeAsUpIndicator(R.drawable.ic_open_drawer)
        actionbar.setBackgroundDrawable( ColorDrawable(Color.parseColor("#ffffff")))

        drawerLayout = findViewById(R.id.notif_log_layout_id)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        val menu = navigationView.menu
        val nav_item = menu.findItem(R.id.nav_screen_config)
        nav_item.isChecked = true

        navigationView.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            drawerLayout!!.closeDrawers()

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

            val drawer = findViewById<DrawerLayout>(R.id.notif_log_layout_id)
            drawer.closeDrawer(GravityCompat.START)
            true
        }

        contact_details_mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        contact_details_mDbWorkerThread.start()
        //on get la base de données
        contact_details_NotificationsDatabase = ContactsRoomDatabase.getDatabase(this)
        val sharedPreferences = getSharedPreferences("Notification_tri", Context.MODE_PRIVATE)
        if (sharedPreferences.getBoolean("filtre_message", true)) {
            list.addAll(contact_details_NotificationsDatabase?.notificationsDao()?.getAllnotifications() as ArrayList<NotificationDB>)
            println("BALISE = "+list)
            val listTmp = mutableListOf<NotificationDB>()
            listTmp.addAll(list)
            listTmp.forEach {
                if(!isMessagingApp(it.platform)) {
                    list.remove(it)
                }
            }
        } else {
            list.addAll(contact_details_NotificationsDatabase?.notificationsDao()?.getAllnotifications() as ArrayList<NotificationDB>)

        }
        if(sharedPreferences.getString("tri","date").equals("date")){

            val adapter = NotificationHistoryAdapterActivity(this, list)
            val listviews = findViewById<ListView>(R.id.listView_notification_history)
            listviews.adapter = adapter
            println("taille list "+list.size+" content "+list.toString())
        }else if(sharedPreferences.getString("tri","date").equals("priorite")){

            var listTmp = contact_details_NotificationsDatabase?.notificationsDao()?.testPriority() as MutableList<NotificationDB>
            var listTmp2 = mutableListOf<NotificationDB>()
            listTmp2.addAll(list)
            listTmp= contact_details_NotificationsDatabase?.notificationsDao()?.testPriority() as MutableList<NotificationDB>
            listTmp2.removeAll(listTmp)
            println("TEEEEEEEST = "+listTmp2.size)
            listTmp.addAll(Math.max(firstContactPrio0(listTmp)-1,0),listTmp2)
            val adapter = NotificationHistoryAdapterActivity(this, listTmp)
            val listviews = findViewById<ListView>(R.id.listView_notification_history)
            listviews.adapter = adapter
        }else{
            println("thats a problem")
        }

        main_BottomNavigationView = findViewById(R.id.navigation)
        main_BottomNavigationView!!.menu.getItem(1).isChecked = true
        main_BottomNavigationView!!.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }
    fun firstContactPrio0(notifList:List<NotificationDB>):Int{
        for (i in 0..notifList.size-1){
            val contact= contact_details_NotificationsDatabase!!.contactsDao().getContact(notifList.get(i).idContact)
            if(contact.contactDB!!.contactPriority==0){
                return i
            }
        }
        return notifList.size
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater=menuInflater
        inflater.inflate(R.menu.menu_notification,menu)////////////////////////////////////////////////////
        val sharedPreferences = getSharedPreferences("Notification_tri", Context.MODE_PRIVATE)
        if(sharedPreferences.getString("tri","date").equals("date")){
            menu!!.findItem(R.id.notif_tri_par_date).setChecked(true)
        }else if(sharedPreferences.getString("tri","date").equals("priorite")){

            menu!!.findItem(R.id.notif_tri_par_priorite).setChecked(true)
        }
        if (!sharedPreferences.getBoolean("filtre_message", true)){
            menu!!.findItem(R.id.messagefilter).setChecked(false)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            android.R.id.home -> {
                drawerLayout!!.openDrawer(GravityCompat.START)
                return true
            }
            R.id.notif_tri_par_date ->{
                println("tri par priorité checked")
                val sharedPreferences = getSharedPreferences("Notification_tri", Context.MODE_PRIVATE)
                val editor =sharedPreferences.edit()
                editor.putString("tri","date")
                editor.commit()
                item.setChecked(true)
                this.recreate()
            }
            R.id.notif_tri_par_priorite ->{
                println("tri par priorité checked")
                val sharedPreferences = getSharedPreferences("Notification_tri", Context.MODE_PRIVATE)
                val editor =sharedPreferences.edit()
                editor.putString("tri","priorite")
                editor.commit()
                item.setChecked(true)
                this.recreate()
            }
            R.id.item_help ->{
                val alertDialogBuilder = android.app.AlertDialog.Builder(this)
                alertDialogBuilder.setMessage(this.resources.getString(R.string.help_history))
                alertDialogBuilder.show()
            }
            R.id.messagefilter -> {
                val sharedPreferences = getSharedPreferences("Notification_tri", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                if (item.isChecked){
                    editor.putBoolean("filtre_message", false)
                    item.setChecked(false)
                } else {
                    editor.putBoolean("filtre_message",true)
                    item.setChecked(true)
                }
                editor.apply()
                this.recreate()
            }
        }
        return super.onOptionsItemSelected(item)
    }
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->

        println("r.id ="+R.id.notif_tri_par_priorite+" item.id="+item.itemId+" ")
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
    private fun isMessagingApp(packageName: String): Boolean {
        if (packageName.equals(NotificationListener.FACEBOOK_PACKAGE)) {
            return true;
        } else if (packageName.equals(NotificationListener.MESSENGER_PACKAGE)) {
            return true;
        } else if (packageName.equals(NotificationListener.WATHSAPP_SERVICE)) {
            return true
        } else if (packageName.equals(NotificationListener.GMAIL_PACKAGE)) {
            return true
        } else if (packageName.equals(NotificationListener.MESSAGE_PACKAGE) || packageName.equals(NotificationListener.MESSAGE_SAMSUNG_PACKAGE)) {
            return true
        } else if (packageName.equals(NotificationListener.TELEGRAM_PACKAGE))
            return true
        return false
    }
}
