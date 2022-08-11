package com.yellowtwigs.knockin.ui.settings

import android.annotation.SuppressLint
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
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.model.ContactManager
import com.yellowtwigs.knockin.model.ContactsRoomDatabase
import com.yellowtwigs.knockin.ui.HelpActivity
import com.yellowtwigs.knockin.ui.contacts.MainActivity
import com.yellowtwigs.knockin.ui.first_launch.MultiSelectActivity
import com.yellowtwigs.knockin.ui.in_app.PremiumActivity
import com.yellowtwigs.knockin.ui.notifications.NotificationSender
import com.yellowtwigs.knockin.ui.teleworking.TeleworkingActivity
import com.yellowtwigs.knockin.utils.EveryActivityUtils
import com.yellowtwigs.knockin.utils.EveryActivityUtils.checkThemePreferences
import java.util.*

/**
 * La Classe qui permet d'activer ou desactiver les notifications de Knockin
 * @author Florian Striebel
 */
class ManageNotificationActivity : AppCompatActivity() {

    //region ========================================== Var or Val ==========================================

    private var drawerLayout: DrawerLayout? = null
    private var activityVisible: Boolean = false
    private var switchPopupNotif: SwitchCompat? = null
    private var switchservice: SwitchCompat? = null
    private var isTrue = false

    private var settings_NotificationMessagesAlarmSound: MediaPlayer? = null
    private var settings_ChooseNotifSoundTitle: TextView? = null
    private var settings_ChooseNotifSoundLayout: ConstraintLayout? = null

    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //region ======================================== Theme Dark ========================================

        val sharedThemePreferences = getSharedPreferences("Knockin_Theme", Context.MODE_PRIVATE)
        checkThemePreferences(this)

        //endregion


        setContentView(R.layout.activity_manage_notification)

        val sharedPreferences = getSharedPreferences("Knockin_preferences", Context.MODE_PRIVATE)

        //region ======================================= FindViewById =======================================

        switchPopupNotif = this.findViewById(R.id.switch_stop_popup)
        switchservice = this.findViewById(R.id.switch_stop_service)
        val switchReminder = this.findViewById<SwitchCompat>(R.id.switch_manage_notif_reminder)
        val remindHour = this.findViewById<TextView>(R.id.textView_heure)
        val viewHour = this.findViewById<ConstraintLayout>(R.id.modify_hour_Constariant)

        settings_ChooseNotifSoundTitle = findViewById(R.id.settings_choose_notif_sound_title)
        settings_ChooseNotifSoundLayout = findViewById(R.id.settings_choose_notif_sound_layout)

        val settings_left_drawer_ThemeSwitch =
            findViewById<SwitchCompat>(R.id.settings_left_drawer_theme_switch)

        if (sharedThemePreferences.getBoolean("darkTheme", false)) {
            settings_left_drawer_ThemeSwitch!!.isChecked = true
        }

        //region ================================ Call Popup from LeftDrawer ================================

        val sharedPreferencePopup = getSharedPreferences("Phone_call", Context.MODE_PRIVATE)
        val settings_CallPopupSwitch = findViewById<SwitchCompat>(R.id.settings_call_popup_switch)

        if (sharedPreferencePopup.getBoolean("popup", true)) {
            settings_CallPopupSwitch!!.isChecked = true
        }

        //endregion

        //endregion

        if (this.isNotificationServiceEnabled) {
            switchPopupNotif?.isChecked = sharedPreferences.getBoolean("popupNotif", false)
            switchservice?.isChecked = sharedPreferences.getBoolean("serviceNotif", false)
            switchReminder.isChecked = sharedPreferences.getBoolean("reminder", false)
        } else {
            switchPopupNotif?.isChecked = false
            switchservice?.isChecked = false
            switchReminder.isChecked = false
        }
        if (!switchReminder.isChecked) {
            viewHour.isEnabled = false
            viewHour.background = getDrawable(R.color.greyColor)
        }
        var hour = sharedPreferences.getInt("remindHour", 18)
        var minute = sharedPreferences.getInt("remindMinute", 0)

        remindHour.text = hourGetstring(hour, minute)
        setReminderAlarm(hour, minute)

        //region ========================================== Toolbar =========================================

        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar
        actionbar?.setDisplayHomeAsUpEnabled(true)
        actionbar?.setHomeAsUpIndicator(R.drawable.ic_open_drawer)

        //endregion

        //region ======================================= DrawerLayout =======================================

        drawerLayout = findViewById(R.id.drawer_layout_manage_notif)

        val navigationView = findViewById<NavigationView>(R.id.nav_view_manage_notif)
        val menu = navigationView.menu
        val navItem = menu.findItem(R.id.nav_notif_config)
        navItem.isChecked = true

        navigationView.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            drawerLayout?.closeDrawers()

            when (menuItem.itemId) {
                R.id.nav_home -> {
                    if (settings_NotificationMessagesAlarmSound != null) {
                        settings_NotificationMessagesAlarmSound!!.stop()
                    }
                    startActivity(Intent(this@ManageNotificationActivity, MainActivity::class.java))
                }
                R.id.nav_manage_screen -> {

                    if (settings_NotificationMessagesAlarmSound != null) {
                        settings_NotificationMessagesAlarmSound!!.stop()
                    }
                    startActivity(
                        Intent(
                            this@ManageNotificationActivity,
                            ManageMyScreenActivity::class.java
                        )
                    )
                }
                R.id.navigation_teleworking -> startActivity(
                    Intent(
                        this@ManageNotificationActivity,
                        TeleworkingActivity::class.java
                    )
                )
                R.id.nav_settings -> {

                    if (settings_NotificationMessagesAlarmSound != null) {
                        settings_NotificationMessagesAlarmSound!!.stop()
                    }
                    startActivity(
                        Intent(
                            this@ManageNotificationActivity,
                            SettingsActivity::class.java
                        )
                    )
                }
                R.id.nav_in_app -> {

                    if (settings_NotificationMessagesAlarmSound != null) {
                        settings_NotificationMessagesAlarmSound!!.stop()
                    }
                    startActivity(
                        Intent(
                            this@ManageNotificationActivity,
                            PremiumActivity::class.java
                        )
                    )
                }
                R.id.nav_help -> {

                    if (settings_NotificationMessagesAlarmSound != null) {
                        settings_NotificationMessagesAlarmSound!!.stop()
                    }
                    startActivity(Intent(this@ManageNotificationActivity, HelpActivity::class.java))
                }
            }

            drawerLayout?.closeDrawer(GravityCompat.START)
            true
        }

        //endregion

        //region ======================================== Listeners =========================================

        settings_CallPopupSwitch?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val sharedCallPopupPreferences: SharedPreferences =
                    getSharedPreferences("Phone_call", Context.MODE_PRIVATE)
                val edit: SharedPreferences.Editor = sharedCallPopupPreferences.edit()
                edit.putBoolean("popup", true)
                edit.apply()
            } else {
                val sharedCallPopupPreferences: SharedPreferences =
                    getSharedPreferences("Phone_call", Context.MODE_PRIVATE)
                val edit: SharedPreferences.Editor = sharedCallPopupPreferences.edit()
                edit.putBoolean("popup", false)
                edit.apply()
            }
        }

        settings_left_drawer_ThemeSwitch?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                setTheme(R.style.AppThemeDark)
//                group_manager_MainLayout!!.setBackgroundResource(R.drawable.dark_background)
                val edit: SharedPreferences.Editor = sharedThemePreferences.edit()
                edit.putBoolean("darkTheme", true)
                edit.apply()
                startActivity(
                    Intent(
                        this@ManageNotificationActivity,
                        ManageNotificationActivity::class.java
                    )
                )
            } else {

                setTheme(R.style.AppTheme)
//                group_manager_MainLayout!!.setBackgroundResource(R.drawable.mr_white_blur_background)
                val edit: SharedPreferences.Editor = sharedThemePreferences.edit()
                edit.putBoolean("darkTheme", false)
                edit.apply()
                startActivity(
                    Intent(
                        this@ManageNotificationActivity,
                        ManageNotificationActivity::class.java
                    )
                )
            }
        }

        switchPopupNotif?.setOnCheckedChangeListener { _, _ ->
            val edit: SharedPreferences.Editor = sharedPreferences.edit()
            if (switchPopupNotif!!.isChecked) {
                /*if (!isNotificationServiceEnabled) {
                   // buildNotificationServiceAlertDialog().show()
                }*/
                switchservice!!.isChecked = true
                edit.putBoolean("serviceNotif", true)
                edit.putBoolean("popupNotif", true)
                edit.apply()
            } else {

                edit.putBoolean("popupNotif", false)
                edit.apply()
            }
        }

        switchservice?.setOnCheckedChangeListener { _, _ ->
            val edit: SharedPreferences.Editor = sharedPreferences.edit()
            if (switchservice!!.isChecked) {
                if (!isNotificationServiceEnabled) {
                    buildNotificationServiceAlertDialog().show()
                } else {
                    edit.putBoolean("serviceNotif", true)
                    edit.apply()
                }
            } else {

                switchPopupNotif!!.isChecked = false
                edit.putBoolean("serviceNotif", false)
                edit.putBoolean("popupNotif", false)
                edit.putBoolean("mask_prio_1", false)
                edit.apply()
            }
        }

        switchReminder.setOnCheckedChangeListener { _, _ ->
            val edit = sharedPreferences.edit()
            if (switchReminder.isChecked) {
                edit.putBoolean("reminder", true)
                viewHour.isEnabled = true
                if (sharedThemePreferences.getBoolean("darkTheme", false)) {
                    viewHour.background = getDrawable(R.color.backgroundColorDark)
                } else {
                    viewHour.background = getDrawable(R.color.backgroundColor)
                }
            } else {
                edit.putBoolean("reminder", false)
                viewHour.isEnabled = false
                viewHour.background = getDrawable(R.color.greyColor)
            }
            edit.apply()
        }

        viewHour.setOnClickListener {
            val timePickerDialog = TimePickerDialog(this, TimePickerDialog.OnTimeSetListener(
                function = { _, h, m ->
                    val editor = sharedPreferences.edit()
                    editor.putInt("remindHour", h)
                    editor.putInt("remindMinute", m)
                    editor.apply()
                    remindHour.text = hourGetstring(h, m)
                    hour = h
                    minute = m
                    setReminderAlarm(hour, minute)
                }
            ), hour, minute, true)
            timePickerDialog.show()
        }

        //endregion

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

        val switch1To0Checked = getSharedPreferences("switch1To0Checked", Context.MODE_PRIVATE)
        val switch1To0 = findViewById<SwitchCompat>(R.id.vip_0_switch)
        switch1To0.isChecked = switch1To0Checked.getBoolean("switch1To0Checked", false)
        switch1To0.setOnCheckedChangeListener { button, isChecked ->
            if (isChecked) {
                val contactManager = ContactManager(this.applicationContext)
                for (contact in contactManager.contactList) {
                    if (contact.contactDB?.contactPriority == 1) {
                        contact.setPriority(ContactsRoomDatabase.getDatabase(this), 0)
                    }
                }
                val edit = switch1To0Checked.edit()
                edit.putBoolean("switch1To0Checked", true)
                edit.apply()
            } else {
                val contactManager = ContactManager(this.applicationContext)
                for (contact in contactManager.contactList) {
                    if (contact.contactDB?.contactPriority == 0) {
                        contact.setPriority(ContactsRoomDatabase.getDatabase(this), 1)
                    }
                }
                val edit = switch1To0Checked.edit()
                edit.putBoolean("switch1To0Checked", false)
                edit.apply()
            }
        }

        val multiSelectVipButton = findViewById<AppCompatImageView>(R.id.vip_multi_select_button)
        multiSelectVipButton.setOnClickListener {
            startActivity(Intent(this@ManageNotificationActivity, MultiSelectActivity::class.java))
        }
    }

    companion object {
        const val PERMISSION_CODE = 111
    }

    //region ========================================== Functions =========================================

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.toolbar_menu_help, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                drawerLayout!!.openDrawer(GravityCompat.START)
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

    @SuppressLint("InflateParams")
    private fun buildNotificationServiceAlertDialog(): androidx.appcompat.app.AlertDialog {
        val inflater: LayoutInflater = this.layoutInflater
        val alertView: View = inflater.inflate(R.layout.alert_dialog_catch_notification, null)
        val alertDialog = MaterialAlertDialogBuilder(this, R.style.AlertDialog)
            .setView(alertView)
            .show()

        val manage_notif_ButtonAlertDialogAllow =
            alertView.findViewById<Button>(R.id.alert_dialog_catch_notification_button_allow_it)
        manage_notif_ButtonAlertDialogAllow.setOnClickListener {
            positiveAlertDialogButtonClick(
                alertDialog
            )
        }

        val manage_notif_ButtonAlertDialogDismiss =
            alertView.findViewById<Button>(R.id.alert_dialog_catch_notification_button_dismiss)
        manage_notif_ButtonAlertDialogDismiss.setOnClickListener {
            negativeAlertDialogButtonClick(
                alertDialog
            )
        }

        return alertDialog
    }

    private fun positiveAlertDialogButtonClick(alertDialog: androidx.appcompat.app.AlertDialog) {
        startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
        val intentFilter = IntentFilter()
        intentFilter.addAction("com.yellowtwigs.Knockin.notificationExemple")
        alertDialog.cancel()
        val thread = Thread {
            activityVisible = false
            while (!isNotificationServiceEnabled && !activityVisible) {
            }
            val sharedPreferences: SharedPreferences =
                getSharedPreferences("Knockin_preferences", Context.MODE_PRIVATE)
            val edit: SharedPreferences.Editor = sharedPreferences.edit()
            if (isNotificationServiceEnabled) {
                edit.putBoolean("serviceNotif", true)
                edit.apply()

                isTrue = true
            } else {
                val runnable = Runnable {
                    switchPopupNotif!!.isChecked = false
                    switchservice!!.isChecked = false
                }
                runOnUiThread(runnable)
            }
        }
        thread.start()
    }

    private fun negativeAlertDialogButtonClick(alertDialog: androidx.appcompat.app.AlertDialog) {
        switchPopupNotif?.isChecked = false
        switchservice?.isChecked = false
        alertDialog.cancel()
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

    private fun hourGetstring(hour: Int, minute: Int): String {
        var textRemind = ""
        if (hour < 10) {
            textRemind += "0" + hour
        } else {
            textRemind += hour
        }
        textRemind += ":"
        if (minute < 10) {
            textRemind += "0" + minute
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

    fun goToPremiumActivity() {
        startActivity(
            Intent(this@ManageNotificationActivity, PremiumActivity::class.java).putExtra(
                "fromManageNotification",
                true
            )
        )
        finish()
    }

    override fun onResume() {
        super.onResume()
        activityVisible = true
    }

    override fun onPause() {
        super.onPause()
        activityVisible = false
    }

    override fun onStart() {
        super.onStart()
        activityVisible = true
    }

    //endregion
}