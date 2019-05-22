package com.example.knocker.controller

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import android.widget.ListView
import com.example.knocker.model.DbWorkerThread
import com.example.knocker.model.ModelDB.NotificationDB
import com.example.knocker.R
import com.example.knocker.model.ContactsRoomDatabase

class NotificationHistoryActivity : AppCompatActivity() {
    private var contact_details_NotificationsDatabase: ContactsRoomDatabase? = null
    private lateinit var contact_details_mDbWorkerThread: DbWorkerThread
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_history)
        // on init WorkerThread
        val toolbar = findViewById<Toolbar>(R.id.toolbar_notification_history)
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar.setHomeAsUpIndicator(R.drawable.ic_left_arrow)
        actionbar.title = "NotificationDB log"

        contact_details_mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        contact_details_mDbWorkerThread.start()
        //on get la base de données
        contact_details_NotificationsDatabase = ContactsRoomDatabase.getDatabase(this)
        val list=contact_details_NotificationsDatabase?.notificationsDao()?.getAllnotifications() as ArrayList<NotificationDB>
        println("voici la liste"+list)
        val adapter = NotificationHistoryAdapterActivity(this, list)
        val listviews = findViewById<ListView>(R.id.listView_notification_history)
        listviews.adapter= adapter
    }
}
