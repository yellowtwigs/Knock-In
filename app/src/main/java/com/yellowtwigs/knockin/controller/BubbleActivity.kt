package com.yellowtwigs.knockin.controller

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
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
        val notificationPopupRecyclerView = popupView!!.findViewById<RecyclerView>(R.id.notification_popup_recycler_view)
        notificationPopupRecyclerView!!.layoutManager = LinearLayoutManager(applicationContext)
        adapterNotification = NotifPopupRecyclerViewAdapter(applicationContext, notifications, windowManager, popupView, null, true)
        notificationPopupRecyclerView.adapter = adapterNotification
        val itemTouchHelper = ItemTouchHelper(SwipeToDeleteCallback(adapterNotification))
        itemTouchHelper.attachToRecyclerView(notificationPopupRecyclerView)
        setContentView(popupView)
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
        println("STARTOOOOOOOOOOOO")
    }

    override fun onPause() {
        super.onPause()
        println("PAUUUUUUUUSEEEEEEEEEE")
    }

    override fun onDestroy() {
        super.onDestroy()
        bubble_activity_ContactsDatabase!!.VipNotificationsDao().deleteAllVipNotifications()
        bubble_activity_ContactsDatabase!!.VipSbnDao().deleteAllSbn()
        val sharedPreferences: SharedPreferences = getSharedPreferences("Knockin_preferences", Context.MODE_PRIVATE)
        val edit: SharedPreferences.Editor = sharedPreferences.edit()
        edit.putBoolean("first_notif", true)
        edit.putBoolean("view", false)
        edit.apply()
    }
}