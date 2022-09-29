package com.yellowtwigs.knockin.ui.notifications.settings

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.*
import android.content.res.Resources
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.ActivityNotificationsSettingsBinding
import com.yellowtwigs.knockin.ui.HelpActivity
import com.yellowtwigs.knockin.ui.first_launch.first_vip_selection.FirstVipSelectionActivity
import com.yellowtwigs.knockin.ui.in_app.PremiumActivity
import com.yellowtwigs.knockin.ui.contacts.list.ContactsListActivity
import com.yellowtwigs.knockin.ui.notifications.NotificationSender
import com.yellowtwigs.knockin.ui.settings.ManageMyScreenActivity
import com.yellowtwigs.knockin.ui.teleworking.TeleworkingActivity
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class NotificationsSettingsActivity : AppCompatActivity() {

    //region ========================================== Var or Val ==========================================

    private var settings_NotificationMessagesAlarmSound: MediaPlayer? = null

    private lateinit var binding: ActivityNotificationsSettingsBinding
    private lateinit var sharedThemePreferences: SharedPreferences

    private val notificationsSettingsViewModel: NotificationsSettingsViewModel by viewModels()

    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //region ======================================== Theme Dark ========================================

        sharedThemePreferences = getSharedPreferences("Knockin_Theme", Context.MODE_PRIVATE)

        //endregion

        binding = ActivityNotificationsSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupDrawerLayout()
        setupSwitchToPriority1To0()
        vipReselection()
        setupCheckBoxes()
        setupReminderAlarm()

        binding.activateOverlay.setOnClickListener {
            openOverlaySettings()
        }
        binding.activateNotification.setOnClickListener {
            activateNotificationsClick()
        }
    }

    //region ======================================= Drawer + Toolbar =======================================

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        val actionbar = supportActionBar
        actionbar?.setDisplayHomeAsUpEnabled(true)
        actionbar?.setHomeAsUpIndicator(R.drawable.ic_open_drawer)
    }

    private fun setupDrawerLayout() {
        val navigationView = findViewById<NavigationView>(R.id.nav_view_manage_notif)
        val menu = navigationView.menu
        val navItem = menu.findItem(R.id.nav_notifications)
        navItem.isChecked = true

        navigationView.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            binding.drawerLayout.closeDrawers()

            if (settings_NotificationMessagesAlarmSound != null) {
                settings_NotificationMessagesAlarmSound?.stop()
            }

            val itemLayout = findViewById<ConstraintLayout>(R.id.teleworking_item)
            val itemText = findViewById<AppCompatTextView>(R.id.teleworking_item_text)

            itemText.text =
                "${getString(R.string.teleworking)} ${getString(R.string.left_drawer_settings)}"

            itemLayout.setOnClickListener {
                startActivity(
                    Intent(
                        this@NotificationsSettingsActivity,
                        TeleworkingActivity::class.java
                    )
                )
            }

            when (menuItem.itemId) {
                R.id.nav_home -> {
                    startActivity(
                        Intent(
                            this@NotificationsSettingsActivity,
                            ContactsListActivity::class.java
                        )
                    )
                }
                R.id.nav_manage_screen -> {
                    startActivity(
                        Intent(
                            this@NotificationsSettingsActivity,
                            ManageMyScreenActivity::class.java
                        )
                    )
                }
                R.id.nav_in_app -> {
                    startActivity(
                        Intent(
                            this@NotificationsSettingsActivity,
                            PremiumActivity::class.java
                        )
                    )
                }
                R.id.nav_help -> {
                    startActivity(
                        Intent(
                            this@NotificationsSettingsActivity,
                            HelpActivity::class.java
                        )
                    )
                }
            }

            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.toolbar_menu_help, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                binding.drawerLayout.openDrawer(GravityCompat.START)
                return true
            }
            R.id.item_help -> {
                if (Resources.getSystem().configuration.locale.language == "fr") {
                    val browserIntent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://www.yellowtwigs.com/aide-gestion-des-notifications")
                    )
                    startActivity(browserIntent)
                } else {
                    val browserIntent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://www.yellowtwigs.com/help-notification-management")
                    )
                    startActivity(browserIntent)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //endregion

    private fun openOverlaySettings() {
        val sharedPreferences = getSharedPreferences("Overlay_Preferences", Context.MODE_PRIVATE)
        val edit: SharedPreferences.Editor = sharedPreferences.edit()
        edit.putBoolean("Overlay_Preferences", true)
        edit.apply()

        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivity(intent)
    }

    private fun activateNotificationsClick() {
        startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
        val intentFilter = IntentFilter()
        intentFilter.addAction("com.yellowtwigs.Knockin.notificationExemple")
    }

    private fun setupSwitchToPriority1To0() {
        val switch1To0Checked = getSharedPreferences("switch1To0Checked", Context.MODE_PRIVATE)
        val switch1To0 = findViewById<SwitchCompat>(R.id.vip_0_switch)
        switch1To0.isChecked = switch1To0Checked.getBoolean("switch1To0Checked", false)
        switch1To0.setOnCheckedChangeListener { button, isChecked ->
            if (isChecked) {
                notificationsSettingsViewModel.updateContactPriority1To0()
                val edit = switch1To0Checked.edit()
                edit.putBoolean("switch1To0Checked", true)
                edit.apply()
            } else {
                notificationsSettingsViewModel.updateContactPriority0To1()
                val edit = switch1To0Checked.edit()
                edit.putBoolean("switch1To0Checked", false)
                edit.apply()
            }
        }
    }

    private fun vipReselection() {
        val multiSelectVipButton = findViewById<AppCompatImageView>(R.id.vip_multi_select_button)
        multiSelectVipButton.setOnClickListener {
            startActivity(
                Intent(
                    this@NotificationsSettingsActivity,
                    FirstVipSelectionActivity::class.java
                )
            )
        }
    }

    private fun setupCheckBoxes() {
        val noSoundCheckbox = findViewById<AppCompatCheckBox>(R.id.no_sound_checkbox)
        val knockinCheckbox = findViewById<AppCompatCheckBox>(R.id.knockin_checkbox)

        val defaultSoundCheck = getSharedPreferences("defaultSoundCheck", Context.MODE_PRIVATE)
        knockinCheckbox.isChecked = defaultSoundCheck.getBoolean("defaultSoundCheck", true)
        noSoundCheckbox.isChecked = !knockinCheckbox.isChecked

        knockinCheckbox.setOnCheckedChangeListener { _, isChecked ->
            val edit = defaultSoundCheck.edit()
            edit.putBoolean("defaultSoundCheck", isChecked)
            edit.apply()
            noSoundCheckbox.isChecked = !knockinCheckbox.isChecked
        }
        noSoundCheckbox.setOnCheckedChangeListener { _, _ ->
            knockinCheckbox.isChecked = !noSoundCheckbox.isChecked
        }
    }

    private val isNotificationServiceEnabled: Boolean
        get() {
            val pkgName = packageName
            val str = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
            if (!TextUtils.isEmpty(str)) {
                val names = str.split(":")
                for (i in names.indices) {
                    val cn = ComponentName.unflattenFromString(names[i])
                    if (cn != null) {
                        if (TextUtils.equals(pkgName, cn.packageName)) {
                            return true
                        }
                    }
                }
            }
            return false
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
        return textRemind
    }

    private fun goToPremiumActivity() {
        startActivity(
            Intent(this@NotificationsSettingsActivity, PremiumActivity::class.java).putExtra(
                "fromManageNotification",
                true
            )
        )
        finish()
    }

    //region =========================================== Reminder ===========================================

    private fun setupReminderAlarm() {
        val sharedPreferences = getSharedPreferences("Notifications_Reminder", Context.MODE_PRIVATE)

        binding.apply {
            switchReminder.isChecked = sharedPreferences.getBoolean("reminder", false)

            if (!switchReminder.isChecked) {
                editReminderHourLayout.isEnabled = false
                editReminderHourLayout.background = AppCompatResources.getDrawable(
                    this@NotificationsSettingsActivity,
                    R.color.greyColor
                )
            }

            var hour = sharedPreferences.getInt("remindHour", 18)
            var minute = sharedPreferences.getInt("remindMinute", 0)

            reminderHourContent.text = hourGetString(hour, minute)
            setReminderAlarm(hour, minute)

            switchReminder.setOnCheckedChangeListener { _, _ ->
                val edit = sharedPreferences.edit()
                if (switchReminder.isChecked) {
                    edit.putBoolean("reminder", true)
                    editReminderHourLayout.isEnabled = true
                    if (sharedThemePreferences.getBoolean("darkTheme", false)) {
                        editReminderHourLayout.background = getDrawable(R.color.backgroundColorDark)
                    } else {
                        editReminderHourLayout.background = getDrawable(R.color.backgroundColor)
                    }
                } else {
                    edit.putBoolean("reminder", false)
                    editReminderHourLayout.isEnabled = false
                    editReminderHourLayout.background = getDrawable(R.color.greyColor)
                }
                edit.apply()
            }

            editReminderHourLayout.setOnClickListener {
                val timePickerDialog = TimePickerDialog(
                    this@NotificationsSettingsActivity,
                    { _, h, m ->
                        val editor = sharedPreferences.edit()
                        editor.putInt("remindHour", h)
                        editor.putInt("remindMinute", m)
                        editor.apply()
                        reminderHourContent.text = hourGetString(h, m)
                        hour = h
                        minute = m
                        setReminderAlarm(hour, minute)
                    },
                    hour,
                    minute,
                    true
                )
                timePickerDialog.show()
            }
        }
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

    //endregion

    companion object {
        const val PERMISSION_CODE = 111
    }
}