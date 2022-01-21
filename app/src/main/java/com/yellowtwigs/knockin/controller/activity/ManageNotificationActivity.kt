package com.yellowtwigs.knockin.controller.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.*
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Point
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.controller.NotificationSender
import com.yellowtwigs.knockin.controller.activity.firstLaunch.MultiSelectActivity
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
    //private lateinit var filePath: Uri

    //region Default Sound

    private var settings_ChooseNotifDefaultSoundLayoutOpenClose: RelativeLayout? = null
    private var settings_ChooseNotifDefaultSoundImageOpen: AppCompatImageView? = null
    private var settings_ChooseNotifDefaultSoundImageClose: AppCompatImageView? = null

    private var settings_NotifNoSoundLayout: RelativeLayout? = null
    private var settings_NotifNoSoundCheckbox: CheckBox? = null

    private var settings_NotifSoundKnockinLayout: RelativeLayout? = null
    private var settings_NotifSoundKnockinCheckbox: CheckBox? = null

    private var settings_NotifSoundXyloLayout: RelativeLayout? = null
    private var settings_NotifSoundXyloCheckbox: CheckBox? = null

    //endregion
/*
    //region personal tones

    private var settings_ChooseNotifPersonalSoundLayoutOpenClose: RelativeLayout? = null
    private var settings_ChooseNotifPersonalSoundImageOpen: AppCompatImageView? = null
    private var settings_ChooseNotifPersonalSoundImageClose: AppCompatImageView? = null

    private var settings_NotifPersonalSoundLayout: RelativeLayout? = null
    lateinit var txtpath: TextView

    //endregion2

 */

    //Schedule

    private var settings_ChooseNotifScheduleLayoutOpenClose: RelativeLayout? = null
    private var settings_ChooseNotifScheduleImageOpen: AppCompatImageView? = null
    private var settings_ChooseNotifScheduleImageClose: AppCompatImageView? = null

    private var settings_Relative1: RelativeLayout? = null

    //end

    //region Jazzy Sound

    private var settings_ChooseNotifJazzySoundLayoutOpenClose: RelativeLayout? = null
    private var settings_ChooseNotifJazzySoundImageOpen: AppCompatImageView? = null
    private var settings_ChooseNotifJazzySoundImageClose: AppCompatImageView? = null

    private var settings_NotifSoundMoaninLayout: RelativeLayout? = null
    private var settings_NotifSoundMoaninCheckbox: CheckBox? = null

    private var settings_NotifSoundBlueBossaLayout: RelativeLayout? = null
    private var settings_NotifSoundBlueBossaCheckbox: CheckBox? = null

    private var settings_NotifSoundAutumnLeavesLayout: RelativeLayout? = null
    private var settings_NotifSoundAutumnLeavesCheckbox: CheckBox? = null

    private var settings_NotifSoundDolphinDanceLayout: RelativeLayout? = null
    private var settings_NotifSoundDolphinDanceCheckbox: CheckBox? = null

    private var settings_NotifSoundFreddieFreeloaderLayout: RelativeLayout? = null
    private var settings_NotifSoundFreddieFreeloaderCheckbox: CheckBox? = null

    private var settings_NotifSoundCaravanLayout: RelativeLayout? = null
    private var settings_NotifSoundCaravanCheckbox: CheckBox? = null

    //endregion

    //region Funky Sound

    private var settings_ChooseNotifFunkySoundLayoutOpenClose: RelativeLayout? = null
    private var settings_ChooseNotifFunkySoundImageOpen: AppCompatImageView? = null
    private var settings_ChooseNotifFunkySoundImageClose: AppCompatImageView? = null

    private var settings_NotifSoundSlapLayout: RelativeLayout? = null
    private var settings_NotifSoundSlapCheckbox: CheckBox? = null

    private var settings_NotifSoundOffTheCurveLayout: RelativeLayout? = null
    private var settings_NotifSoundOffTheCurveCheckbox: CheckBox? = null

    private var settings_NotifSoundKeyboardFunkyToneLayout: RelativeLayout? = null
    private var settings_NotifSoundKeyboardFunkyToneCheckbox: CheckBox? = null

    private var settings_NotifSoundUCantHoldNoGrooveLayout: RelativeLayout? = null
    private var settings_NotifSoundUCantHoldNoGrooveCheckbox: CheckBox? = null

    private var settings_NotifSoundColdSweatLayout: RelativeLayout? = null
    private var settings_NotifSoundColdSweatCheckbox: CheckBox? = null

    private var settings_NotifSoundFunkYallLayout: RelativeLayout? = null
    private var settings_NotifSoundFunkYallCheckbox: CheckBox? = null

    //endregion

    //region Relaxation Sound

    private var settings_ChooseNotifRelaxationSoundLayoutOpenClose: RelativeLayout? = null
    private var settings_ChooseNotifRelaxationSoundImageOpen: AppCompatImageView? = null
    private var settings_ChooseNotifRelaxationSoundImageClose: AppCompatImageView? = null

    private var settings_NotifSoundAcousticGuitarLayout: RelativeLayout? = null
    private var settings_NotifSoundAcousticGuitarCheckbox: CheckBox? = null

    private var settings_NotifSoundRelaxToneLayout: RelativeLayout? = null
    private var settings_NotifSoundRelaxToneCheckbox: CheckBox? = null

    private var settings_NotifSoundGravityLayout: RelativeLayout? = null
    private var settings_NotifSoundGravityCheckbox: CheckBox? = null

    private var settings_NotifSoundSlowDancingLayout: RelativeLayout? = null
    private var settings_NotifSoundSlowDancingCheckbox: CheckBox? = null

    private var settings_NotifSoundScorpionThemeLayout: RelativeLayout? = null
    private var settings_NotifSoundScorpionThemeCheckbox: CheckBox? = null

    private var settings_NotifSoundFirstStepLayout: RelativeLayout? = null
    private var settings_NotifSoundFirstStepCheckbox: CheckBox? = null

    //endregion

    private var settings_NotificationMessagesAlarmSound: MediaPlayer? = null
    private var settings_ChooseNotifSoundTitle: TextView? = null
    private var settings_ChooseNotifSoundLayout: ConstraintLayout? = null

    private var notifFunkySoundIsBought: Boolean = false
    private var notifJazzySoundIsBought: Boolean = false
    private var notifRelaxationSoundIsBought: Boolean = false
    private var notifCustomSoundIsBought: Boolean = false

    private var numberDefault: Int = 0

    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications_vip_ringtone_layout)

        //region ======================================== Theme Dark ========================================

        val sharedThemePreferences = getSharedPreferences("Knockin_Theme", Context.MODE_PRIVATE)
        if (sharedThemePreferences.getBoolean("darkTheme", false)) {
            setTheme(R.style.AppThemeDark)
        } else {
            setTheme(R.style.AppTheme)
        }
        //endregion
        setContentView()
        /*
        //get the list of  Ringtones
        var UploadButton= findViewById(R.id.UploadButton) as Button
        txtpath=findViewById(R.id.Txtpath)
        UploadButton.setOnClickListener{ //Intent to select Ringtone.
            //check runtime permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                    //permission denied
                    val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                    //show popup to request runtime permission
                    requestPermissions(permissions, PERMISSION_CODE)
                } else {
                    getTones()
                }
            } else {
                getTones()
            }
            if(txtpath!=null){
                settings_NotifNoSoundCheckbox!!.isChecked = false
                settings_NotifSoundKnockinCheckbox!!.isChecked = false
                settings_NotifSoundXyloCheckbox!!.isChecked = false

                settings_NotifSoundMoaninCheckbox!!.isChecked = false
                settings_NotifSoundBlueBossaCheckbox!!.isChecked = false
                settings_NotifSoundCaravanCheckbox!!.isChecked = false
                settings_NotifSoundDolphinDanceCheckbox!!.isChecked = false
                settings_NotifSoundAutumnLeavesCheckbox!!.isChecked = false
                settings_NotifSoundFreddieFreeloaderCheckbox!!.isChecked = false

                settings_NotifSoundSlapCheckbox!!.isChecked = false
                settings_NotifSoundOffTheCurveCheckbox!!.isChecked = false
                settings_NotifSoundFunkYallCheckbox!!.isChecked = false
                settings_NotifSoundKeyboardFunkyToneCheckbox!!.isChecked = false
                settings_NotifSoundUCantHoldNoGrooveCheckbox!!.isChecked = false
                settings_NotifSoundColdSweatCheckbox!!.isChecked = false

                settings_NotifSoundAcousticGuitarCheckbox!!.isChecked = false
                settings_NotifSoundGravityCheckbox!!.isChecked = false
                settings_NotifSoundSlowDancingCheckbox!!.isChecked = false
                settings_NotifSoundScorpionThemeCheckbox!!.isChecked = false
                settings_NotifSoundFirstStepCheckbox!!.isChecked = false
                settings_NotifSoundRelaxToneCheckbox!!.isChecked = false
            }
        }
        //end
         */

        val sharedPreferences: SharedPreferences = getSharedPreferences("Knockin_preferences", Context.MODE_PRIVATE)
        val sharedAlarmNotifTonePreferences: SharedPreferences = getSharedPreferences("Alarm_Tone", Context.MODE_PRIVATE)
        numberDefault = sharedAlarmNotifTonePreferences.getInt("Alarm_Tone", R.raw.sms_ring)

        val sharedNotifJazzySoundInAppPreferences: SharedPreferences = getSharedPreferences("Jazzy_Sound_Bought", Context.MODE_PRIVATE)
        notifJazzySoundIsBought = sharedNotifJazzySoundInAppPreferences.getBoolean("Jazzy_Sound_Bought", false)

        val sharedNotifRelaxationSoundInAppPreferences: SharedPreferences = getSharedPreferences("Relax_Sound_Bought", Context.MODE_PRIVATE)
        notifRelaxationSoundIsBought = sharedNotifRelaxationSoundInAppPreferences.getBoolean("Relax_Sound_Bought", false)

        val sharedNotifFunkySoundInAppPreferences: SharedPreferences = getSharedPreferences("Funky_Sound_Bought", Context.MODE_PRIVATE)
        notifFunkySoundIsBought = sharedNotifFunkySoundInAppPreferences.getBoolean("Funky_Sound_Bought", false)

    //    val sharedNotifCustomSoundInAppPreferences: SharedPreferences = getSharedPreferences("Custom_Sound_Bought", Context.MODE_PRIVATE)
    //    notifCustomSoundIsBought = sharedNotifCustomSoundInAppPreferences.getBoolean("Custom_Sound_Bought", false)


        //region ======================================= FindViewById =======================================

        switchPopupNotif = this.findViewById(R.id.switch_stop_popup)
        switchservice = this.findViewById(R.id.switch_stop_service)
        val switchReminder = this.findViewById<SwitchCompat>(R.id.switch_manage_notif_reminder)
        val remindHour = this.findViewById<TextView>(R.id.textView_heure)
        val viewHour = this.findViewById<ConstraintLayout>(R.id.modify_hour_Constariant)

        settings_ChooseNotifSoundTitle = findViewById(R.id.settings_choose_notif_sound_title)
        settings_ChooseNotifSoundLayout = findViewById(R.id.settings_choose_notif_sound_layout)

        val group_manager_MainLayout = findViewById<LinearLayoutCompat>(R.id.manage_my_notif_layout_id)

        val settings_left_drawer_ThemeSwitch = findViewById<SwitchCompat>(R.id.settings_left_drawer_theme_switch)

        if (sharedThemePreferences.getBoolean("darkTheme", false)) {
            settings_left_drawer_ThemeSwitch!!.isChecked = true
        }

        val main_SettingsLeftDrawerLayout = findViewById<RelativeLayout>(R.id.settings_left_drawer_layout)

        //region ================================ Call Popup from LeftDrawer ================================

        val sharedPreferencePopup = getSharedPreferences("Phone_call", Context.MODE_PRIVATE)
        val settings_CallPopupSwitch = findViewById<SwitchCompat>(R.id.settings_call_popup_switch)

        if (sharedPreferencePopup.getBoolean("popup", true)) {
            settings_CallPopupSwitch!!.isChecked = true
        }

        //endregion

        //endregion

        if (this.isNotificationServiceEnabled) {
            switchPopupNotif!!.isChecked = sharedPreferences.getBoolean("popupNotif", false)
            switchservice!!.isChecked = sharedPreferences.getBoolean("serviceNotif", false)
            switchReminder.isChecked = sharedPreferences.getBoolean("reminder", false)
        } else {
            switchPopupNotif!!.isChecked = false
            switchservice!!.isChecked = false
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
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar.setHomeAsUpIndicator(R.drawable.ic_open_drawer)

        //endregion

        //region ======================================= DrawerLayout =======================================

        drawerLayout = findViewById(R.id.drawer_layout_manage_notif)

        val navigationView = findViewById<NavigationView>(R.id.nav_view_manage_notif)
        val menu = navigationView.menu
        val navItem = menu.findItem(R.id.nav_notif_config)
        navItem.isChecked = true

        navigationView.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            drawerLayout!!.closeDrawers()

            when (menuItem.itemId) {
                R.id.nav_home -> {
                    if (settings_NotificationMessagesAlarmSound != null) {
                        settings_NotificationMessagesAlarmSound!!.stop()
                    }
                    startActivity(Intent(this@ManageNotificationActivity, MainActivity::class.java))
                }
                R.id.nav_informations -> startActivity(Intent(this@ManageNotificationActivity, EditInformationsActivity::class.java))
                R.id.nav_manage_screen -> {

                    if (settings_NotificationMessagesAlarmSound != null) {
                        settings_NotificationMessagesAlarmSound!!.stop()
                    }
                    startActivity(Intent(this@ManageNotificationActivity, ManageMyScreenActivity::class.java))
                }
                R.id.nav_settings -> {

                    if (settings_NotificationMessagesAlarmSound != null) {
                        settings_NotificationMessagesAlarmSound!!.stop()
                    }
                    startActivity(Intent(this@ManageNotificationActivity, SettingsActivity::class.java))
                }
                R.id.nav_in_app -> {

                    if (settings_NotificationMessagesAlarmSound != null) {
                        settings_NotificationMessagesAlarmSound!!.stop()
                    }
                    startActivity(Intent(this@ManageNotificationActivity, PremiumActivity::class.java))
                }
                R.id.nav_statistics -> {
                }
                R.id.nav_help -> {

                    if (settings_NotificationMessagesAlarmSound != null) {
                        settings_NotificationMessagesAlarmSound!!.stop()
                    }
                    startActivity(Intent(this@ManageNotificationActivity, HelpActivity::class.java))
                }
            }

            val drawer = findViewById<DrawerLayout>(R.id.drawer_layout_manage_notif)
            drawer.closeDrawer(GravityCompat.START)
            true
        }

        //endregion

        //region ======================================== Listeners =========================================

        settings_CallPopupSwitch!!.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val sharedCallPopupPreferences: SharedPreferences = getSharedPreferences("Phone_call", Context.MODE_PRIVATE)
                val edit: SharedPreferences.Editor = sharedCallPopupPreferences.edit()
                edit.putBoolean("popup", true)
                edit.apply()
            } else {
                val sharedCallPopupPreferences: SharedPreferences = getSharedPreferences("Phone_call", Context.MODE_PRIVATE)
                val edit: SharedPreferences.Editor = sharedCallPopupPreferences.edit()
                edit.putBoolean("popup", false)
                edit.apply()
            }
        }

        settings_left_drawer_ThemeSwitch!!.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {

                setTheme(R.style.AppThemeDark)
//                group_manager_MainLayout!!.setBackgroundResource(R.drawable.dark_background)
                val edit: SharedPreferences.Editor = sharedThemePreferences.edit()
                edit.putBoolean("darkTheme", true)
                edit.apply()
                startActivity(Intent(this@ManageNotificationActivity, ManageNotificationActivity::class.java))
            } else {

                setTheme(R.style.AppTheme)
//                group_manager_MainLayout!!.setBackgroundResource(R.drawable.mr_white_blur_background)
                val edit: SharedPreferences.Editor = sharedThemePreferences.edit()
                edit.putBoolean("darkTheme", false)
                edit.apply()
                startActivity(Intent(this@ManageNotificationActivity, ManageNotificationActivity::class.java))
            }
        }

        switchPopupNotif!!.setOnCheckedChangeListener { _, _ ->
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

        switchservice!!.setOnCheckedChangeListener { _, _ ->
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

    }
companion object {
    const val PERMISSION_CODE = 111
}

    //region ========================================== Functions =========================================

    private fun setContentView() {
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val height = size.y

        when {
            height > 2500 -> setContentView(R.layout.activity_manage_notification)
            height in 1800..2499 -> setContentView(R.layout.activity_manage_notification)
            height in 1100..1799 -> setContentView(R.layout.activity_manage_notification_smaller_screen)
            height < 1099 -> setContentView(R.layout.activity_manage_notification_mini_screen)
        }
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
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
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.yellowtwigs.com/aide-gestion-des-notifications"))
                    startActivity(browserIntent)
                } else {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.yellowtwigs.com/help-notification-management"))
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

        val manage_notif_ButtonAlertDialogAllow = alertView.findViewById<Button>(R.id.alert_dialog_catch_notification_button_allow_it)
        manage_notif_ButtonAlertDialogAllow.setOnClickListener { positiveAlertDialogButtonClick(alertDialog) }

        val manage_notif_ButtonAlertDialogDismiss = alertView.findViewById<Button>(R.id.alert_dialog_catch_notification_button_dismiss)
        manage_notif_ButtonAlertDialogDismiss.setOnClickListener { negativeAlertDialogButtonClick(alertDialog) }

        return alertDialog
    }

    private fun buildMultiSelectAlertDialog(): androidx.appcompat.app.AlertDialog {
        val alertDialog = MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                .setBackground(getDrawable(R.color.backgroundColor))
                .setTitle(getString(R.string.notification_alert_dialog_title))
                .setMessage(getString(R.string.notification_alert_dialog_message))
                .setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
                    startActivity(Intent(this@ManageNotificationActivity, MultiSelectActivity::class.java))
                    val sharedPreferences: SharedPreferences = getSharedPreferences("Knockin_preferences", Context.MODE_PRIVATE)
                    val edit: SharedPreferences.Editor = sharedPreferences.edit()
                    edit.putBoolean("view", true)
                    edit.apply()
                    closeContextMenu()
                }
                .setNegativeButton(R.string.alert_dialog_later)
                { _, _ ->
                    closeContextMenu()

                }
                .show()


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
            val sharedPreferences: SharedPreferences = getSharedPreferences("Knockin_preferences", Context.MODE_PRIVATE)
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
        switchPopupNotif!!.isChecked = false
        switchservice!!.isChecked = false
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
        val pendingIntent = PendingIntent.getBroadcast(applicationContext, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmManager = getSystemService(ALARM_SERVICE) as (AlarmManager)
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent)

        //cancel previous app
    }

    fun goToPremiumActivity(){
        startActivity(Intent(this@ManageNotificationActivity, PremiumActivity::class.java).putExtra("fromManageNotification", true))
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