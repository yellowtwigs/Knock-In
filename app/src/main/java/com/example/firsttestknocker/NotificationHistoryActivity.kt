package com.example.firsttestknocker

import android.app.Notification
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ListView

class NotificationHistoryActivity : AppCompatActivity() {
    private var contact_details_NotificationsDatabase: ContactsRoomDatabase? = null
    private lateinit var contact_details_mDbWorkerThread: DbWorkerThread
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_history)
        // on init WorkerThread
        contact_details_mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        contact_details_mDbWorkerThread.start()
        //on get la base de données
        contact_details_NotificationsDatabase = ContactsRoomDatabase.getDatabase(this)
        val list=contact_details_NotificationsDatabase?.notificationsDao()?.getAllnotifications() as ArrayList<Notifications>
        val adapter =NotificationHistoryAdapterActivity(this,list)
        val listviews = findViewById<ListView>(R.id.listView_notification_history)
        listviews.adapter= adapter
    }
}
