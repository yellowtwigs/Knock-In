package com.example.knocker.controller.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ListView
import com.example.knocker.model.DbWorkerThread
import com.example.knocker.model.ModelDB.NotificationDB
import com.example.knocker.R
import com.example.knocker.model.ContactsRoomDatabase
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * La Classe qui permet d'afficher l'historique des notifications
 * @author Florian Striebel
 */
class NotificationHistoryActivity : AppCompatActivity() {

    private var contact_details_NotificationsDatabase: ContactsRoomDatabase? = null
    private lateinit var contact_details_mDbWorkerThread: DbWorkerThread

    private var main_BottomNavigationView: BottomNavigationView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_history)
        // on init WorkerThread

        contact_details_mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        contact_details_mDbWorkerThread.start()

        //on get la base de données
        contact_details_NotificationsDatabase = ContactsRoomDatabase.getDatabase(this)

        val list = contact_details_NotificationsDatabase?.notificationsDao()?.getAllnotifications() as ArrayList<NotificationDB>
        val adapter = NotificationHistoryAdapterActivity(this, list)
        val listviews = findViewById<ListView>(R.id.listView_notification_history)
        listviews.adapter = adapter

        main_BottomNavigationView = findViewById(R.id.navigation)
        main_BottomNavigationView!!.menu.getItem(1).isChecked = true
        main_BottomNavigationView!!.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_phone_book -> {
                startActivity(Intent(this@NotificationHistoryActivity, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_groups -> {
            }
            R.id.navigation_notifcations -> {
            }
            R.id.navigation_socials_networks -> {
                startActivity(Intent(this@NotificationHistoryActivity, SocialsNetworksLinksActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_phone_keyboard -> {
                startActivity(Intent(this@NotificationHistoryActivity, PhoneLogActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }
}
