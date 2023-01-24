package com.yellowtwigs.knockin.ui.teleworking

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TimePicker
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.ActivityTeleworkingBinding
import com.yellowtwigs.knockin.ui.HelpActivity
import com.yellowtwigs.knockin.ui.contacts.contact_selected.ContactSelectedWithAppsActivity
import com.yellowtwigs.knockin.ui.first_launch.first_vip_selection.FirstVipSelectionActivity
import com.yellowtwigs.knockin.ui.premium.PremiumActivity
import com.yellowtwigs.knockin.ui.contacts.list.ContactsListActivity
import com.yellowtwigs.knockin.ui.notifications.NotificationSender
import com.yellowtwigs.knockin.ui.settings.ManageMyScreenActivity
import com.yellowtwigs.knockin.ui.notifications.settings.NotificationsSettingsActivity
import com.yellowtwigs.knockin.utils.EveryActivityUtils.checkTheme
import com.yellowtwigs.knockin.utils.FirebaseViewModel
import com.yellowtwigs.knockin.utils.SaveUserIdToFirebase.saveUserIdToFirebase
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class TeleworkingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeleworkingBinding
    private val firebaseViewModel: FirebaseViewModel by viewModels()

    private lateinit var userIdPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkTheme(this)

        val viewModel: TeleworkingViewModel by viewModels()
        binding = ActivityTeleworkingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userIdPreferences = getSharedPreferences("User_Id", Context.MODE_PRIVATE)

        saveUserIdToFirebase(userIdPreferences, firebaseViewModel, "Enter the Teleworking Activity")

        setupContactList(viewModel)
        setupReminder()
        setupToolbar()
        editTextTimePicker()

        val vipScheduleValueSharedPreferences =
            getSharedPreferences("VipScheduleValue", Context.MODE_PRIVATE)

        binding.vipContactsButton.setOnClickListener {
            startActivity(Intent(this@TeleworkingActivity, FirstVipSelectionActivity::class.java))
        }

        binding.teleworkingModeSwitch.isChecked =
            vipScheduleValueSharedPreferences.getBoolean("VipScheduleValue", false)

        changeSwitchCompatColor(binding.teleworkingModeSwitch)

        binding.teleworkingModeSwitch.setOnCheckedChangeListener { compoundButton, isChecked ->
            changeSwitchCompatColor(binding.teleworkingModeSwitch)
            if (isChecked) {
                binding.teleworkingModeSwitch.thumbTintList

                viewModel.updateContactsPriority(0)
                val edit = vipScheduleValueSharedPreferences.edit()
                edit.putBoolean("VipScheduleValue", true)
                edit.apply()
            } else {
                viewModel.updateContactsPriority(1)
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

    //region ==================================== TOOLBAR / DRAWER LAYOUT ===================================

    private fun setupToolbar() {
        binding.apply {
            openDrawer.setOnClickListener {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }
    }

    private fun setupDrawerLayout() {
        binding.apply {
            val itemIcon = findViewById<AppCompatImageView>(R.id.teleworking_item_icon)
            val itemText = findViewById<AppCompatTextView>(R.id.teleworking_item_text)

            itemIcon.setImageResource(R.drawable.ic_teleworking_yellow)
            itemText.text =
                "${getString(R.string.teleworking)} ${getString(R.string.left_drawer_settings)}"
            itemText.setTextColor(resources.getColor(R.color.colorPrimaryDark))

            navView.setNavigationItemSelectedListener { menuItem ->
                if (menuItem.itemId != R.id.nav_sync_contact && menuItem.itemId != R.id.nav_invite_friend) {
                    menuItem.isChecked = true
                }
                drawerLayout.closeDrawers()

                when (menuItem.itemId) {
                    R.id.nav_home -> startActivity(
                        Intent(
                            this@TeleworkingActivity,
                            ContactsListActivity::class.java
                        )
                    )
                    R.id.nav_notifications -> startActivity(
                        Intent(
                            this@TeleworkingActivity,
                            NotificationsSettingsActivity::class.java
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

    //endregion

    private fun setupContactList(viewModel: TeleworkingViewModel) {
        var len =
            getSharedPreferences("Gridview_column", Context.MODE_PRIVATE).getInt("gridview", 4)

        if (len == 1) len = 4

        val teleworkingContactsListAdapter = TeleworkingContactsListAdapter(this) { id ->
            startActivity(
                Intent(
                    this@TeleworkingActivity,
                    ContactSelectedWithAppsActivity::class.java
                ).putExtra(
                    "ContactId",
                    id
                )
            )
        }

        binding.vipContacts.apply {
            viewModel.liveData.observe(this@TeleworkingActivity) { contacts ->
                adapter = teleworkingContactsListAdapter
                teleworkingContactsListAdapter.submitList(contacts.sortedBy {
                    if (it.firstName == "" || it.firstName == " " || it.firstName.isBlank() || it.firstName.isEmpty()) {
                        it.lastName.uppercase()
                    } else {
                        it.firstName.uppercase()
                    }
                })
                setHasFixedSize(true)
                layoutManager = GridLayoutManager(this@TeleworkingActivity, len)
                recycledViewPool.setMaxRecycledViews(0, 0)
            }
        }
    }

    //region ============================================= Date =============================================

    private fun setupReminder() {
        val sharedPreferences = getSharedPreferences("Knockin_preferences", Context.MODE_PRIVATE)

        var hour = sharedPreferences.getInt("remindHour", 18)
        var minute = sharedPreferences.getInt("remindMinute", 0)

        binding.apply {
            reminderHourEditText.setText(hourGetString(hour, minute))
            setReminderAlarm(hour, minute)
            reminderHourEditText.setOnClickListener {
                val timePickerDialog =
                    TimePickerDialog(this@TeleworkingActivity, TimePickerDialog.OnTimeSetListener(
                        function = { _, h, m ->
                            val editor = sharedPreferences.edit()
                            editor.putInt("remindHour", h)
                            editor.putInt("remindMinute", m)
                            editor.apply()
                            reminderHourEditText.setText(hourGetString(h, m))
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

    private fun editTextTimePicker() {
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
}