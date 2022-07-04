package com.yellowtwigs.knockin.ui.teleworking

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.ActivityTeleworkingBinding
import com.yellowtwigs.knockin.model.ContactManager
import com.yellowtwigs.knockin.model.ContactsRoomDatabase
import com.yellowtwigs.knockin.model.DbWorkerThread
import com.yellowtwigs.knockin.model.dao.NotificationsDao
import com.yellowtwigs.knockin.ui.CockpitActivity
import com.yellowtwigs.knockin.ui.contacts.MainActivity
import com.yellowtwigs.knockin.ui.group.GroupManagerActivity
import com.yellowtwigs.knockin.ui.notifications.NotificationSender
import com.yellowtwigs.knockin.ui.notifications.history.NotificationHistoryActivity
import com.yellowtwigs.knockin.utils.EveryActivityUtils.checkThemePreferences
import java.util.*

class TeleworkingActivity : AppCompatActivity() {

    private lateinit var database: ContactsRoomDatabase
    private lateinit var dao: NotificationsDao

    private val mOnNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_contacts -> {
                    startActivity(
                        Intent(
                            this@TeleworkingActivity,
                            MainActivity::class.java
                        ).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    )
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_groups -> {
                    startActivity(
                        Intent(
                            this@TeleworkingActivity,
                            GroupManagerActivity::class.java
                        ).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    )
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_notifcations -> {
                    startActivity(
                        Intent(
                            this@TeleworkingActivity,
                            NotificationHistoryActivity::class.java
                        ).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    )
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_cockpit -> {
                    startActivity(
                        Intent(this@TeleworkingActivity, CockpitActivity::class.java).addFlags(
                            Intent.FLAG_ACTIVITY_NO_ANIMATION
                        )
                    )
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkThemePreferences(this)
        val binding = ActivityTeleworkingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDatabase()
        setupContactList(binding)
        setupReminder(binding)

        binding.apply {
            navigation.menu.getItem(2)?.isChecked = true
            navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        }
    }

    private fun setupDatabase() {
        DbWorkerThread("dbWorkerThread").start()
        ContactsRoomDatabase.getDatabase(this)?.let {
            database = it
            dao = it.notificationsDao()
        }
    }

    private fun setupContactList(binding: ActivityTeleworkingBinding) {
        val teleworkingContactsListAdapter = TeleworkingContactsListAdapter(this)

        binding.vipContacts.apply {
            val contactList =
                ContactManager(this@TeleworkingActivity).contactList.filter { contact ->
                    contact.contactDB?.contactPriority == 2
                }.sortedBy {
                    it.contactDB?.firstName + it.contactDB?.lastName
                }

            this.adapter = teleworkingContactsListAdapter
            teleworkingContactsListAdapter.submitList(contactList)
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(this@TeleworkingActivity, 4)
            recycledViewPool.setMaxRecycledViews(0, 0)
        }
    }

    private fun setupReminder(binding: ActivityTeleworkingBinding) {
        val sharedPreferences = getSharedPreferences("Knockin_preferences", Context.MODE_PRIVATE)

        var hour = sharedPreferences.getInt("remindHour", 18)
        var minute = sharedPreferences.getInt("remindMinute", 0)

        binding.apply {
            reminderHourContent.text = hourGetString(hour, minute)
            setReminderAlarm(hour, minute)
            editReminderHourLayout.setOnClickListener {
                val timePickerDialog = TimePickerDialog(this@TeleworkingActivity, TimePickerDialog.OnTimeSetListener(
                    function = { _, h, m ->
                        val editor = sharedPreferences.edit()
                        editor.putInt("remindHour", h)
                        editor.putInt("remindMinute", m)
                        editor.apply()
                        reminderHourContent.text = hourGetString(h, m)
                        hour = h
                        minute = m
                        setReminderAlarm(hour, minute)
                    }
                ), hour, minute, true)
                timePickerDialog.show()
            }
        }
    }

    private fun hourGetString(hour: Int, minute: Int): String {
        var textRemind = ""
        if (hour < 10) {
            textRemind += "0$hour"
        } else {
            textRemind += hour
        }
        textRemind += ":"
        if (minute < 10) {
            textRemind += "0$minute"
        } else {
            textRemind += minute
        }
        println(textRemind)
        return textRemind
    }

    private fun setReminderAlarm(hour: Int, minute: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        val intent = Intent(applicationContext, NotificationSender::class.java)
        intent.action = "NOTIFICATION_TIME"
        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            100,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarmManager = getSystemService(ALARM_SERVICE) as (AlarmManager)
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )

        //cancel previous app
    }

//    private fun notificationsSchedule(binding: ActivityTeleworkingBinding){
//        if (currentContact.contactDB?.hourLimitForNotification.toString().contains("to")) {
//            binding.startTimeEditText.setText(
//                convertTimeToStartTime(
//                    currentContact.contactDB?.hourLimitForNotification.toString()
//                )
//            )
//
//            binding.endTimeEditText.setText(
//                convertTimeToEndTime(
//                    currentContact.contactDB?.hourLimitForNotification.toString()
//                )
//            )
//        }
//    }
}