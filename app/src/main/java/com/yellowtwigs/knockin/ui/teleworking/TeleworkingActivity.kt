package com.yellowtwigs.knockin.ui.teleworking

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TimePicker
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.navigation.NavigationView
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.ActivityTeleworkingBinding
import com.yellowtwigs.knockin.model.ContactManager
import com.yellowtwigs.knockin.model.ContactsRoomDatabase
import com.yellowtwigs.knockin.model.DbWorkerThread
import com.yellowtwigs.knockin.model.dao.NotificationsDao
import com.yellowtwigs.knockin.ui.HelpActivity
import com.yellowtwigs.knockin.ui.contacts.MainActivity
import com.yellowtwigs.knockin.ui.first_launch.MultiSelectActivity
import com.yellowtwigs.knockin.ui.in_app.PremiumActivity
import com.yellowtwigs.knockin.ui.notifications.NotificationSender
import com.yellowtwigs.knockin.ui.settings.ManageMyScreenActivity
import com.yellowtwigs.knockin.ui.settings.ManageNotificationActivity
import com.yellowtwigs.knockin.utils.EveryActivityUtils.checkThemePreferences
import java.util.*

class TeleworkingActivity : AppCompatActivity() {

    private lateinit var database: ContactsRoomDatabase
    private lateinit var dao: NotificationsDao
    private lateinit var binding: ActivityTeleworkingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkThemePreferences(this)
        binding = ActivityTeleworkingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDatabase()
        setupContactList(binding)
        setupReminder(binding)
        setupToolbar(binding)
        editTextTimePicker(binding)

        val vipScheduleValueSharedPreferences =
            getSharedPreferences("VipScheduleValue", Context.MODE_PRIVATE)

        binding.apply {
            navView.menu.getItem(4).isChecked = true
        }

        binding.vipContactsButton.setOnClickListener {
            startActivity(Intent(this@TeleworkingActivity, MultiSelectActivity::class.java).putExtra("fromTeleworking", true))
        }

        binding.teleworkingModeSwitch.isChecked =
            vipScheduleValueSharedPreferences.getBoolean("VipScheduleValue", false)

        binding.teleworkingModeSwitch.setOnCheckedChangeListener { compoundButton, isChecked ->
            if (isChecked) {
                val contactManager = ContactManager(this.applicationContext)
                for (contact in contactManager.contactList) {
                    if (contact.contactDB?.contactPriority == 1) {
                        contact.setPriority(ContactsRoomDatabase.getDatabase(this), 0)
                    }
                }
                val edit = vipScheduleValueSharedPreferences.edit()
                edit.putBoolean("VipScheduleValue", true)
                edit.apply()
            } else {
                val contactManager = ContactManager(this.applicationContext)
                for (contact in contactManager.contactList) {
                    if (contact.contactDB?.contactPriority == 0) {
                        contact.setPriority(ContactsRoomDatabase.getDatabase(this), 1)
                    }
                }
                val edit = vipScheduleValueSharedPreferences.edit()
                edit.putBoolean("VipScheduleValue", false)
                edit.apply()
            }
        }

        setupDrawerLayout()

        val notificationsHour = getSharedPreferences(
            "TeleworkingReminder", Context.MODE_PRIVATE
        ).getString("TeleworkingReminder", "")

        if (notificationsHour?.contains("to") == true) {
            binding.startTimeEditText.setText(convertTimeToStartTime(notificationsHour))
            binding.endTimeEditText.setText(convertTimeToEndTime(notificationsHour))
        }
    }

    override fun onStop() {
        val teleworkingReminderValueSharedPreferences =
            getSharedPreferences("TeleworkingReminder", Context.MODE_PRIVATE)
        val teleworkingEditor = teleworkingReminderValueSharedPreferences.edit()
        teleworkingEditor.putString(
            "TeleworkingReminder", convertStartAndEndTimeToOneString(
                binding.startTimeEditText.text.toString(),
                binding.endTimeEditText.text.toString()
            )
        )

        teleworkingEditor.apply()
        super.onStop()
    }

    private fun setupToolbar(binding: ActivityTeleworkingBinding) {
        binding.apply {
            openDrawer.setOnClickListener {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }
    }

    private fun setupDrawerLayout() {
        binding.apply {
            navView.setNavigationItemSelectedListener { menuItem ->
                navView.menu.findItem(R.id.navigation_teleworking).isChecked = true
                drawerLayout.closeDrawers()
                when (menuItem.itemId) {
                    R.id.nav_home -> startActivity(Intent(this@TeleworkingActivity, MainActivity::class.java))
                    R.id.nav_notifications -> startActivity(
                        Intent(
                            this@TeleworkingActivity,
                            ManageNotificationActivity::class.java
                        )
                    )
                    R.id.nav_in_app -> startActivity(
                        Intent(
                            this@TeleworkingActivity,
                            PremiumActivity::class.java
                        )
                    )
                    R.id.nav_manage_screen -> startActivity(
                        Intent(
                            this@TeleworkingActivity,
                            ManageMyScreenActivity::class.java
                        )
                    )
                    R.id.nav_help -> startActivity(Intent(this@TeleworkingActivity, HelpActivity::class.java))
                }

                true
            }
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

    //region ============================================= Date =============================================

    private fun setupReminder(binding: ActivityTeleworkingBinding) {
        val sharedPreferences = getSharedPreferences("Knockin_preferences", Context.MODE_PRIVATE)

        var hour = sharedPreferences.getInt("remindHour", 18)
        var minute = sharedPreferences.getInt("remindMinute", 0)

        binding.apply {
            reminderHourContent.text = hourGetString(hour, minute)
            setReminderAlarm(hour, minute)
            editReminderHourLayout.setOnClickListener {
                val timePickerDialog =
                    TimePickerDialog(this@TeleworkingActivity, TimePickerDialog.OnTimeSetListener(
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
    }

    private fun editTextTimePicker(binding: ActivityTeleworkingBinding) {
        binding.startTimeEditText.setOnClickListener { view ->
            val timePickerDialog = TimePickerDialog(
                this,
                { timePicker: TimePicker?, hourOfDay: Int, minutes: Int ->
                    if (minutes < 10) {
                        binding.startTimeEditText.setText(
                            hourOfDay.toString() + "h0" + minutes
                        )
                    } else {
                        binding.startTimeEditText.setText(
                            hourOfDay.toString() + "h" + minutes
                        )
                    }

                }, 0, 0, true
            )
            timePickerDialog.show()
        }
        binding.endTimeEditText.setOnClickListener { view ->
            val timePickerDialog = TimePickerDialog(
                this,
                { timePicker: TimePicker?, hourOfDay: Int, minutes: Int ->
                    if (minutes < 10) {
                        binding.endTimeEditText.setText(
                            hourOfDay.toString() + "h0" + minutes
                        )
                    } else {
                        binding.endTimeEditText.setText(
                            hourOfDay.toString() + "h" + minutes
                        )
                    }
                }, 0, 0, true
            )
            timePickerDialog.show()
        }
    }

    private fun convertStartAndEndTimeToOneString(startTime: String, endTime: String): String {
        return "$startTime to $endTime"
    }

    private fun convertTimeToStartTime(time: String): String {
        val parts = time.split(" to").toTypedArray()
        return parts[0]
    }

    private fun convertTimeToEndTime(time: String): String {
        val parts = time.split("to ").toTypedArray()
        return parts[1]
    }

    //endregion

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