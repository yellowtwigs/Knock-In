package com.yellowtwigs.knockin.ui.teleworking

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TimePicker
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
        }

        val itemLayout = findViewById<ConstraintLayout>(R.id.teleworking_item)
        val itemIcon = findViewById<AppCompatImageView>(R.id.teleworking_item_icon)
        val itemText = findViewById<AppCompatTextView>(R.id.teleworking_item_text)

        itemIcon.setImageResource(R.drawable.ic_teleworking_yellow)
        itemText.setTextColor(resources.getColor(R.color.colorPrimaryDark))
        itemText.text =
            "${getString(R.string.teleworking)} ${getString(R.string.left_drawer_settings)}"

        binding.vipContactsButton.setOnClickListener {
            startActivity(
                Intent(
                    this@TeleworkingActivity,
                    MultiSelectActivity::class.java
                ).putExtra("fromTeleworking", true)
            )
        }

        binding.teleworkingModeSwitch.isChecked =
            vipScheduleValueSharedPreferences.getBoolean("VipScheduleValue", false)

        changeSwitchCompatColor(binding.teleworkingModeSwitch)

        binding.teleworkingModeSwitch.setOnCheckedChangeListener { compoundButton, isChecked ->
            changeSwitchCompatColor(binding.teleworkingModeSwitch)
            if (isChecked) {
                binding.teleworkingModeSwitch.thumbTintList
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

            buildMaterialAlertDialogBuilder()
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

    private fun changeSwitchCompatColor(switchTheme: SwitchCompat) {
        val states = arrayOf(
            intArrayOf(-android.R.attr.state_checked),
            intArrayOf(android.R.attr.state_checked)
        )

        lateinit var thumbColors: IntArray
        lateinit var trackColors: IntArray

        val sharedThemePreferences = getSharedPreferences("Knockin_Theme", Context.MODE_PRIVATE)
        if (sharedThemePreferences.getBoolean("darkTheme", false)) {
            thumbColors = intArrayOf(
                Color.WHITE,
                Color.CYAN
            )

            trackColors = intArrayOf(
                Color.LTGRAY,
                Color.argb(120, 3, 214, 194)
            )
        } else {
            thumbColors = intArrayOf(
                Color.LTGRAY,
                Color.CYAN
            )

            trackColors = intArrayOf(
                Color.LTGRAY,
                Color.argb(120, 3, 214, 194)
            )
        }

        DrawableCompat.setTintList(
            DrawableCompat.wrap(switchTheme.thumbDrawable),
            ColorStateList(states, thumbColors)
        )
        DrawableCompat.setTintList(
            DrawableCompat.wrap(switchTheme.trackDrawable),
            ColorStateList(states, trackColors)
        )
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
            toolbarHelp.setOnClickListener {
                MaterialAlertDialogBuilder(this@TeleworkingActivity, R.style.AlertDialog)
                    .setTitle(getString(R.string.help))
                    .setMessage(getString(R.string.teleworking_help))
                    .show()
            }
        }
    }

    private fun setupDrawerLayout() {
        binding.apply {
            navView.setNavigationItemSelectedListener { menuItem ->

                drawerLayout.closeDrawers()
                when (menuItem.itemId) {
                    R.id.nav_home -> startActivity(
                        Intent(
                            this@TeleworkingActivity,
                            MainActivity::class.java
                        )
                    )
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
                    R.id.nav_help -> startActivity(
                        Intent(
                            this@TeleworkingActivity,
                            HelpActivity::class.java
                        )
                    )
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
            reminderHourEditText.setText(hourGetString(hour, minute))
            setReminderAlarm(hour, minute)
            reminderHourContent.setOnClickListener {
                val timePickerDialog =
                    TimePickerDialog(this@TeleworkingActivity, { _, h, m ->
                        val editor = sharedPreferences.edit()
                        editor.putInt("remindHour", h)
                        editor.putInt("remindMinute", m)
                        editor.apply()
                        reminderHourEditText.setText(hourGetString(h, m))
                        hour = h
                        minute = m
                        setReminderAlarm(hour, minute)
                    }, hour, minute, true)
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

    private fun buildMaterialAlertDialogBuilder() {
        MaterialAlertDialogBuilder(this, R.style.AlertDialog)
            .setTitle(getString(R.string.teleworking_alert_dialog_title))
            .setMessage(getString(R.string.teleworking_alert_dialog_subtitle))
            .setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
                startActivity(Intent(this@TeleworkingActivity, MainActivity::class.java))
            }
            .setNegativeButton(R.string.alert_dialog_no) { _, _ ->
            }
            .show()
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