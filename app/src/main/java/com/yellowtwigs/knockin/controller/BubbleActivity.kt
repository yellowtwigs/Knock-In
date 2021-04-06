package com.yellowtwigs.knockin.controller

import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.model.ContactsRoomDatabase
import com.yellowtwigs.knockin.model.ModelDB.VipSbnDB
import com.yellowtwigs.knockin.model.StatusBarParcelable
import java.util.HashMap
import java.util.ArrayList

class BubbleActivity : AppCompatActivity() {

    private var bubble_activity_ContactsDatabase: ContactsRoomDatabase? = null
    private var adapterNotification: NotifPopupRecyclerViewAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("ONNNNNNNNNCREATTTTTTTTTTEEEEEEEEE")
    }

    fun getSbnInfo(vipSbnList: List<VipSbnDB>): HashMap<String, Any?>{
        val sbnInfo = HashMap<String, Any?>()
        for (item in vipSbnList) {
            sbnInfo[item.sbnKey] = item.sbnValue
        }
        return (sbnInfo)
    }

    fun getVipSbnKeys(vipSbnList: List<VipSbnDB>):  ArrayList<String>{
        val vipSbnKeys = ArrayList<String>()
        for (item in vipSbnList) {
            vipSbnKeys.add(item.sbnKey)
        }
        return (vipSbnKeys)
    }

    override fun onResume() {
        super.onResume()
        println("RESUMEEEEEEE")
    }

    override fun onStart() {
        super.onStart()
        bubble_activity_ContactsDatabase = ContactsRoomDatabase.getDatabase(this)
        val notifications: ArrayList<StatusBarParcelable> = ArrayList()
        //var allSbnVip : ArrayList<List<VipSbnDB>>? = null
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.layout_notification_pop_up, null)
        val vipNotificationList = bubble_activity_ContactsDatabase!!.VipNotificationsDao().getAllVipNotificationsById()
        for (vipNotif in vipNotificationList) {
            val vipSbnList = bubble_activity_ContactsDatabase!!.VipSbnDao().getSbnWithNotifId(vipNotif.id.toString())
            val statusBarNotificationInfo = getSbnInfo(vipSbnList)
            val vipSbnKeys: ArrayList<String> = getVipSbnKeys(vipSbnList)
            println(vipNotif.id!!)
            println(vipSbnKeys.size)
            println(vipNotif.appNotifier)
            println(vipSbnKeys)
            println(statusBarNotificationInfo)
            val sbp = StatusBarParcelable(vipNotif.id!!, vipNotif.listSize, vipNotif.appNotifier, vipSbnKeys, statusBarNotificationInfo)
            notifications.add(sbp)
        }
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val imgClose = popupView.findViewById<View>(R.id.notification_popup_close) as AppCompatImageView
        imgClose.visibility = View.GONE
        val notificationPopupRecyclerView = popupView!!.findViewById<RecyclerView>(R.id.notification_popup_recycler_view)
        notificationPopupRecyclerView!!.layoutManager = LinearLayoutManager(applicationContext)
        adapterNotification = NotifPopupRecyclerViewAdapter(this, notifications, windowManager, popupView, null, true)
        notificationPopupRecyclerView.adapter = adapterNotification
        val itemTouchHelper = ItemTouchHelper(SwipeToDeleteCallback(adapterNotification))
        itemTouchHelper.attachToRecyclerView(notificationPopupRecyclerView)
        setContentView(popupView)
        println("STARTOOOOOOOOOOOO")
    }

    fun reduceBubble() {
        onBackPressed()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun closeBubble() {
        println("finitoooooo")
        val notificationManager = this.getSystemService(NotificationManager::class.java)
        notificationManager?.deleteNotificationChannel("notifications")
        bubble_activity_ContactsDatabase!!.VipNotificationsDao().deleteAllVipNotifications()
        bubble_activity_ContactsDatabase!!.VipSbnDao().deleteAllSbn()
        val sharedPreferences: SharedPreferences = getSharedPreferences("Knockin_preferences", Context.MODE_PRIVATE)
        val edit: SharedPreferences.Editor = sharedPreferences.edit()
        edit.putBoolean("first_notif", true)
        edit.putBoolean("view", false)
        edit.apply()
        finishAndRemoveTask()
    }

    override fun onPause() {
        super.onPause()
        println("PAUUUUUUUUSEEEEEEEEEE")
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onDestroy() {
        super.onDestroy()
        println("DESTROYEEEEEEE")
        val notificationManager = this.getSystemService(NotificationManager::class.java)
        notificationManager?.deleteNotificationChannel("notifications")
        bubble_activity_ContactsDatabase!!.VipNotificationsDao().deleteAllVipNotifications()
        bubble_activity_ContactsDatabase!!.VipSbnDao().deleteAllSbn()
        val sharedPreferences: SharedPreferences = getSharedPreferences("Knockin_preferences", Context.MODE_PRIVATE)
        val edit: SharedPreferences.Editor = sharedPreferences.edit()
        edit.putBoolean("first_notif", true)
        edit.putBoolean("view", false)
        edit.apply()
        println("END DESTROYYYY")
        finishAndRemoveTask()
    }
}