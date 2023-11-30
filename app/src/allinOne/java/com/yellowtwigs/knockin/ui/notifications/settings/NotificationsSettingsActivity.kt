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
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.ActivityNotificationsSettingsBinding
import com.yellowtwigs.knockin.ui.HelpActivity
import com.yellowtwigs.knockin.ui.first_launch.first_vip_selection.FirstVipSelectionActivity
import com.yellowtwigs.knockin.ui.contacts.list.ContactsListActivity
import com.yellowtwigs.knockin.ui.first_launch.start.ImportContactsViewModel
import com.yellowtwigs.knockin.ui.notifications.sender.NotificationSender
import com.yellowtwigs.knockin.ui.settings.ManageMyScreenActivity
import com.yellowtwigs.knockin.ui.statistics.dashboard.DashboardActivity
import com.yellowtwigs.knockin.ui.teleworking.TeleworkingActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class NotificationsSettingsActivity : AppCompatActivity() {

    //region ========================================== Var or Val ==========================================

    private var settings_NotificationMessagesAlarmSound: MediaPlayer? = null

    private lateinit var binding: ActivityNotificationsSettingsBinding
    private lateinit var sharedThemePreferences: SharedPreferences

    private val notificationsSettingsViewModel: NotificationsSettingsViewModel by viewModels()
    private val importContactsViewModel: ImportContactsViewModel by viewModels()

    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedThemePreferences = getSharedPreferences("Knockin_Theme", Context.MODE_PRIVATE)
        if (sharedThemePreferences.getBoolean("darkTheme", false)) {
            setTheme(R.style.AppThemeDark)
        } else {
            setTheme(R.style.AppTheme)
        }

        binding = ActivityNotificationsSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupDrawerLayout()
        setupSwitchToPriority1To0()
        setupSwitchAllContactsEnabled()
        vipReselection()
        setupCheckBoxes()
        setupReminderAlarm()

        binding.activateOverlay.isChecked = Settings.canDrawOverlays(this@NotificationsSettingsActivity)

        binding.activateOverlay.setOnCheckedChangeListener { compoundButton, isChecked ->
            openOverlaySettings()
        }

        binding.activateNotification.isChecked = isNotificationServiceEnabled()
        binding.activateNotification.setOnCheckedChangeListener { compoundButton, isChecked ->
            activateNotificationsClick()
        }
    }

    override fun onResume() {
        super.onResume()

        binding.activateOverlay.isChecked = Settings.canDrawOverlays(this@NotificationsSettingsActivity)
        binding.activateNotification.isChecked = isNotificationServiceEnabled()
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
            if (menuItem.itemId != R.id.nav_sync_contact && menuItem.itemId != R.id.nav_invite_friend) {
                menuItem.isChecked = true
            }
            binding.drawerLayout.closeDrawers()

            if (settings_NotificationMessagesAlarmSound != null) {
                settings_NotificationMessagesAlarmSound?.stop()
            }

            when (menuItem.itemId) {
                R.id.nav_home -> {
                    startActivity(
                        Intent(
                            this@NotificationsSettingsActivity, ContactsListActivity::class.java
                        )
                    )
                }
                R.id.nav_dashboard -> startActivity(
                    Intent(this@NotificationsSettingsActivity, DashboardActivity::class.java)
                )
                R.id.nav_teleworking -> startActivity(
                    Intent(this@NotificationsSettingsActivity, TeleworkingActivity::class.java)
                )
                R.id.nav_manage_screen -> {
                    startActivity(
                        Intent(
                            this@NotificationsSettingsActivity, ManageMyScreenActivity::class.java
                        )
                    )
                }
                R.id.nav_help -> {
                    startActivity(
                        Intent(
                            this@NotificationsSettingsActivity, HelpActivity::class.java
                        )
                    )
                }
                R.id.nav_sync_contact -> {
                    importContacts()
                }
                R.id.nav_invite_friend -> {
                    val intent = Intent(Intent.ACTION_SEND)
                    val messageString = resources.getString(R.string.invite_friend_text) + " \n" + resources.getString(
                        R.string.location_on_playstore
                    )
                    intent.putExtra(Intent.EXTRA_TEXT, messageString)
                    intent.type = "text/plain"
                    val messageIntent = Intent.createChooser(intent, null)
                    startActivity(messageIntent)
                }
            }

            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun importContacts() {
        CoroutineScope(Dispatchers.Main).launch {
            importContactsViewModel.syncAllContactsInDatabase(contentResolver)
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
                        Intent.ACTION_VIEW, Uri.parse("https://www.yellowtwigs.com/aide-gestion-des-notifications")
                    )
                    startActivity(browserIntent)
                } else {
                    val browserIntent = Intent(
                        Intent.ACTION_VIEW, Uri.parse("https://www.yellowtwigs.com/help-notification-management")
                    )
                    startActivity(browserIntent)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //endregion

    private fun activateButtonIsClickable(isClickable: Boolean, button: AppCompatButton) {
        button.isEnabled = isClickable

        if (isClickable) {
            button.setBackgroundColor(
                ResourcesCompat.getColor(
                    resources, R.color.colorPrimary, null
                )
            )
        } else {
            button.setBackgroundColor(
                ResourcesCompat.getColor(
                    resources, R.color.greyColor, null
                )
            )
        }
    }

    private fun openOverlaySettings() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")
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

    private fun setupSwitchAllContactsEnabled() {
        val name = "switchAllContactsEnabledChecked"
        val voiceCallAllEnabledSwitchChecked = getSharedPreferences(name, Context.MODE_PRIVATE)
        val voiceCallAllEnabledSwitch = findViewById<SwitchCompat>(R.id.voice_call_all_enabled_switch)
        voiceCallAllEnabledSwitch.isChecked = voiceCallAllEnabledSwitchChecked.getBoolean(name, false)
        voiceCallAllEnabledSwitch.setOnCheckedChangeListener { button, isChecked ->
            if (isChecked) {
                notificationsSettingsViewModel.disabledAllPhoneCallContacts(contentResolver)
                val edit = voiceCallAllEnabledSwitchChecked.edit()
                edit.putBoolean(name, true)
                edit.apply()
            } else {
                notificationsSettingsViewModel.enabledAllPhoneCallContacts(contentResolver)
                val edit = voiceCallAllEnabledSwitchChecked.edit()
                edit.putBoolean(name, false)
                edit.apply()
            }
        }
    }

    private fun vipReselection() {
        val multiSelectVipButton = findViewById<AppCompatImageView>(R.id.vip_multi_select_button)
        multiSelectVipButton.setOnClickListener {
            startActivity(
                Intent(
                    this@NotificationsSettingsActivity, FirstVipSelectionActivity::class.java
                ).putExtra("fromSettings", true)
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

    private fun isNotificationServiceEnabled(): Boolean {
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

    //region =========================================== Reminder ===========================================

    private fun setupReminderAlarm() {
        val sharedPreferences = getSharedPreferences("Notifications_Reminder", Context.MODE_PRIVATE)

        binding.apply {
            switchReminder.isChecked = sharedPreferences.getBoolean("reminder", false)

            if (!switchReminder.isChecked) {
                editReminderHourLayout.isEnabled = false
                editReminderHourLayout.background = AppCompatResources.getDrawable(
                    this@NotificationsSettingsActivity, R.color.greyColor
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
                    this@NotificationsSettingsActivity, { _, h, m ->
                        Log.i("GetReminderTime", "hour : $h")
                        Log.i("GetReminderTime", "minutes : $m")
                        val editor = sharedPreferences.edit()
                        editor.putInt("remindHour", h)
                        editor.putInt("remindMinute", m)
                        editor.apply()
                        reminderHourContent.text = hourGetString(h, m)
                        hour = h
                        minute = m
                        setReminderAlarm(hour, minute)
                    }, hour, minute, true
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
            applicationContext, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarmManager = getSystemService(ALARM_SERVICE) as (AlarmManager)
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent
        )
    }

    //endregion

    companion object {
        const val PERMISSION_CODE = 111
    }
}