package com.yellowtwigs.knockin.controller.activity

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.*
import android.media.MediaPlayer
import android.media.session.MediaController
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import androidx.appcompat.widget.Toolbar
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.controller.NotificationSender
import com.yellowtwigs.knockin.controller.activity.firstLaunch.MultiSelectActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_manage_notification.*
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * La Classe qui permet d'activer ou desactiver les notifications de Knockin
 * @author Florian Striebel
 */
class ManageNotificationActivity : AppCompatActivity(), SeekBar.OnSeekBarChangeListener {

    //region ========================================== Var or Val ==========================================

    private var drawerLayout: DrawerLayout? = null
    private var activityVisible: Boolean = false
    private var switchPopupNotif: Switch? = null
    private var switchservice: Switch? = null
    private var isTrue = false

    var handler = Handler()

    private var settings_NotifSoundSlapLayout: RelativeLayout? = null
    private var settings_MediaController: android.widget.MediaController? = null
    //    private var settings_NotifSoundXyloLayout: RelativeLayout? = null
//    private var settings_NotifSoundKeyboardLayout: RelativeLayout? = null
//    private var settings_NotifSoundGuitarLayout: RelativeLayout? = null
//    private var settings_NotifSoundDrumLayout: RelativeLayout? = null
//    private var settings_NotifSoundSaxLayout: RelativeLayout? = null
    private var settings_NotificationSoundCurrTimeText: TextView? = null
    private var settings_NotificationSoundMaxTimeText: TextView? = null
    private var settings_NotificationSoundSlapPlayButton: AppCompatImageButton? = null
    private var settings_NotificationSoundSlapStopButton: AppCompatImageButton? = null
    private var settings_NotificationSoundSlapSeekbar: SeekBar? = null

    private var settings_NotificationMessagesAlarmSound: MediaPlayer? = null
    private var settings_ChooseNotifSoundTitle: TextView? = null
    private var settings_ChooseNotifSoundLayout: ConstraintLayout? = null

    private var settings_ChooseNotifSoundLayoutOpenClose: RelativeLayout? = null
    private var settings_ChooseNotifSoundImageOpen: AppCompatImageView? = null
    private var settings_ChooseNotifSoundImageClose: AppCompatImageView? = null

    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //region ======================================== Theme Dark ========================================

        val sharedThemePreferences = getSharedPreferences("Knockin_Theme", Context.MODE_PRIVATE)
        if (sharedThemePreferences.getBoolean("darkTheme", false)) {
            setTheme(R.style.AppThemeDark)
        } else {
            setTheme(R.style.AppTheme)
        }

        //endregion

        setContentView(R.layout.activity_manage_notification)

        val sharedPreferences: SharedPreferences = getSharedPreferences("Knockin_preferences", Context.MODE_PRIVATE)
        val sharedAlarmNotifTonePreferences: SharedPreferences = getSharedPreferences("Alarm_Notif_Tone", Context.MODE_PRIVATE)

        switchPopupNotif = this.findViewById(R.id.switch_stop_popup)
        switchservice = this.findViewById(R.id.switch_stop_service)
        val switchReminder = this.findViewById<Switch>(R.id.switch_manage_notif_reminder)
        val remindHour = this.findViewById<TextView>(R.id.textView_heure)
        val viewHour = this.findViewById<ConstraintLayout>(R.id.modify_hour_Constariant)

        settings_MediaController = MediaController(this)

        settings_NotifSoundSlapLayout = findViewById(R.id.settings_notif_sound_slap_layout)
//        settings_NotifSoundSlapMediaController = findViewById(R.id.settings_notif_sound_slap_media)
//        settings_NotifSoundXyloLayout = findViewById(R.id.settings_notif_sound_xylo_layout)
//        settings_NotifSoundKeyboardLayout = findViewById(R.id.settings_notif_sound_keyboard_layout)
//        settings_NotifSoundGuitarLayout = findViewById(R.id.settings_notif_sound_guitar_layout)
//        settings_NotifSoundDrumLayout = findViewById(R.id.settings_notif_sound_drum_layout)
//        settings_NotifSoundSaxLayout = findViewById(R.id.settings_notif_sound_sax_layout)
        settings_NotificationSoundSlapSeekbar = findViewById(R.id.settings_notif_sound_slap_seekbar)
        settings_NotificationSoundSlapSeekbar!!.max = 100
        settings_NotificationSoundSlapSeekbar!!.progress = 0
        settings_NotificationSoundSlapSeekbar!!.setOnSeekBarChangeListener(this)

        settings_NotificationSoundCurrTimeText = findViewById(R.id.settings_notif_sound_slap_current_time)
        settings_NotificationSoundMaxTimeText = findViewById(R.id.settings_notif_sound_slap_max_time)

        settings_NotificationSoundSlapPlayButton = findViewById(R.id.settings_notif_sound_slap_play)
        settings_NotificationSoundSlapStopButton = findViewById(R.id.settings_notif_sound_slap_stop)

        settings_ChooseNotifSoundTitle = findViewById(R.id.settings_choose_notif_sound_title)
        settings_ChooseNotifSoundLayout = findViewById(R.id.settings_choose_notif_sound_layout)

        settings_ChooseNotifSoundLayoutOpenClose = findViewById(R.id.settings_choose_notif_sound_layout_open_close)
        settings_ChooseNotifSoundImageOpen = findViewById(R.id.settings_choose_notif_sound_image_open)
        settings_ChooseNotifSoundImageClose = findViewById(R.id.settings_choose_notif_sound_image_close)

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

        when (sharedAlarmNotifTonePreferences.getInt("Alarm_Notif_Tone", 1)) {
            R.raw.bass_slap -> {
//                settings_NotifSoundSlapLayout!!.background = getDrawable(R.drawable.border_selected_image_view)
            }
//            R.raw.xylophone_tone -> {
//                settings_NotifSoundXyloLayout!!.background = getDrawable(R.drawable.border_selected_image_view)
//            }
//            R.raw.piano_sms -> {
//                settings_NotifSoundKeyboardLayout!!.background = getDrawable(R.drawable.border_selected_image_view)
//            }
//            R.raw.electric_blues -> {
//                settings_NotifSoundGuitarLayout!!.background = getDrawable(R.drawable.border_selected_image_view)
//            }
//            R.raw.caravan -> {
//                settings_NotifSoundDrumLayout!!.background = getDrawable(R.drawable.border_selected_image_view)
//            }
//            R.raw.sax_sms -> {
//                settings_NotifSoundSaxLayout!!.background = getDrawable(R.drawable.border_selected_image_view)
//            }
        }

//        settings_NotifSoundSlapMediaController!!.setMediaPlayer(MediaPlayer.create(this, R.raw.bass_slap).)

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

        //region ======================================== Ring Tones ========================================

        settings_NotificationSoundSlapPlayButton!!.setOnClickListener {
            audioPlay()
        }

        settings_NotificationSoundSlapStopButton!!.setOnClickListener {
            audioStop()
        }

        settings_ChooseNotifSoundLayoutOpenClose!!.setOnClickListener {
            if (settings_ChooseNotifSoundImageClose!!.visibility == View.GONE) {
                settings_ChooseNotifSoundLayout!!.visibility = View.GONE
                settings_ChooseNotifSoundImageClose!!.visibility = View.VISIBLE
                settings_ChooseNotifSoundImageOpen!!.visibility = View.GONE
            } else if (settings_ChooseNotifSoundImageOpen!!.visibility == View.GONE) {
                settings_ChooseNotifSoundLayout!!.visibility = View.VISIBLE
                settings_ChooseNotifSoundImageOpen!!.visibility = View.VISIBLE
                settings_ChooseNotifSoundImageClose!!.visibility = View.GONE
                val slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down)
                settings_ChooseNotifSoundLayout!!.startAnimation(slideDown)
            }
        }

//        settings_NotifSoundSlapLayout!!.setOnClickListener {
//            if (settings_NotificationMessagesAlarmSound != null) {
//                settings_NotificationMessagesAlarmSound!!.stop()
//            }
//            settings_NotificationMessagesAlarmSound = MediaPlayer.create(this, R.raw.bass_slap)
//            settings_NotificationMessagesAlarmSound!!.start()
//
////            settings_NotifSoundSlapLayout!!.background = getDrawable(R.drawable.border_selected_image_view)
////            settings_NotifSoundXyloLayout!!.background = getDrawable(R.drawable.background_layout_selector)
////            settings_NotifSoundKeyboardLayout!!.background = getDrawable(R.drawable.background_layout_selector)
////            settings_NotifSoundGuitarLayout!!.background = getDrawable(R.drawable.background_layout_selector)
////            settings_NotifSoundDrumLayout!!.background = getDrawable(R.drawable.background_layout_selector)
////            settings_NotifSoundSaxLayout!!.background = getDrawable(R.drawable.background_layout_selector)
//
//            val edit: SharedPreferences.Editor = sharedAlarmNotifTonePreferences.edit()
//            edit.putInt("Alarm_Notif_Tone", R.raw.bass_slap)
//            edit.apply()
//        }

//        settings_NotifSoundXyloLayout!!.setOnClickListener {
//            if (settings_NotificationMessagesAlarmSound != null) {
//                settings_NotificationMessagesAlarmSound!!.stop()
//            }
//            settings_NotificationMessagesAlarmSound = MediaPlayer.create(this, R.raw.xylophone_tone)
//            settings_NotificationMessagesAlarmSound!!.start()
//
//            settings_NotifSoundSlapLayout!!.background = getDrawable(R.drawable.background_layout_selector)
//            settings_NotifSoundXyloLayout!!.background = getDrawable(R.drawable.border_selected_image_view)
//            settings_NotifSoundKeyboardLayout!!.background = getDrawable(R.drawable.background_layout_selector)
//            settings_NotifSoundGuitarLayout!!.background = getDrawable(R.drawable.background_layout_selector)
//            settings_NotifSoundDrumLayout!!.background = getDrawable(R.drawable.background_layout_selector)
//            settings_NotifSoundSaxLayout!!.background = getDrawable(R.drawable.background_layout_selector)
//
//            val edit: SharedPreferences.Editor = sharedAlarmNotifTonePreferences.edit()
//            edit.putInt("Alarm_Notif_Tone", R.raw.xylophone_tone)
//            edit.apply()
//        }
//
//        settings_NotifSoundKeyboardLayout!!.setOnClickListener {
//            if (settings_NotificationMessagesAlarmSound != null) {
//                settings_NotificationMessagesAlarmSound!!.stop()
//            }
//            settings_NotificationMessagesAlarmSound = MediaPlayer.create(this, R.raw.piano_sms)
//            settings_NotificationMessagesAlarmSound!!.start()
//
//            settings_NotifSoundSlapLayout!!.background = getDrawable(R.drawable.background_layout_selector)
//            settings_NotifSoundXyloLayout!!.background = getDrawable(R.drawable.background_layout_selector)
//            settings_NotifSoundKeyboardLayout!!.background = getDrawable(R.drawable.border_selected_image_view)
//            settings_NotifSoundGuitarLayout!!.background = getDrawable(R.drawable.background_layout_selector)
//            settings_NotifSoundDrumLayout!!.background = getDrawable(R.drawable.background_layout_selector)
//            settings_NotifSoundSaxLayout!!.background = getDrawable(R.drawable.background_layout_selector)
//
//            val edit: SharedPreferences.Editor = sharedAlarmNotifTonePreferences.edit()
//            edit.putInt("Alarm_Notif_Tone", R.raw.piano_sms)
//            edit.apply()
//        }
//
//        settings_NotifSoundGuitarLayout!!.setOnClickListener {
//            if (settings_NotificationMessagesAlarmSound != null) {
//                settings_NotificationMessagesAlarmSound!!.stop()
//            }
//            settings_NotificationMessagesAlarmSound = MediaPlayer.create(this, R.raw.electric_blues)
//            settings_NotificationMessagesAlarmSound!!.start()
//
//            settings_NotifSoundSlapLayout!!.background = getDrawable(R.drawable.background_layout_selector)
//            settings_NotifSoundXyloLayout!!.background = getDrawable(R.drawable.background_layout_selector)
//            settings_NotifSoundKeyboardLayout!!.background = getDrawable(R.drawable.background_layout_selector)
//            settings_NotifSoundGuitarLayout!!.background = getDrawable(R.drawable.border_selected_image_view)
//            settings_NotifSoundDrumLayout!!.background = getDrawable(R.drawable.background_layout_selector)
//            settings_NotifSoundSaxLayout!!.background = getDrawable(R.drawable.background_layout_selector)
//
//            val edit: SharedPreferences.Editor = sharedAlarmNotifTonePreferences.edit()
//            edit.putInt("Alarm_Notif_Tone", R.raw.electric_blues)
//            edit.apply()
//        }
//
//        settings_NotifSoundDrumLayout!!.setOnClickListener {
//            if (settings_NotificationMessagesAlarmSound != null) {
//                settings_NotificationMessagesAlarmSound!!.stop()
//            }
//            settings_NotificationMessagesAlarmSound = MediaPlayer.create(this, R.raw.caravan)
//            settings_NotificationMessagesAlarmSound!!.start()
//
//            settings_NotifSoundSlapLayout!!.background = getDrawable(R.drawable.background_layout_selector)
//            settings_NotifSoundXyloLayout!!.background = getDrawable(R.drawable.background_layout_selector)
//            settings_NotifSoundKeyboardLayout!!.background = getDrawable(R.drawable.background_layout_selector)
//            settings_NotifSoundGuitarLayout!!.background = getDrawable(R.drawable.background_layout_selector)
//            settings_NotifSoundDrumLayout!!.background = getDrawable(R.drawable.border_selected_image_view)
//            settings_NotifSoundSaxLayout!!.background = getDrawable(R.drawable.background_layout_selector)
//
//            val edit: SharedPreferences.Editor = sharedAlarmNotifTonePreferences.edit()
//            edit.putInt("Alarm_Notif_Tone", R.raw.caravan)
//            edit.apply()
//        }
//
//        settings_NotifSoundSaxLayout!!.setOnClickListener {
//            if (settings_NotificationMessagesAlarmSound != null) {
//                settings_NotificationMessagesAlarmSound!!.stop()
//            }
//            settings_NotificationMessagesAlarmSound = MediaPlayer.create(this, R.raw.sax_sms)
//            settings_NotificationMessagesAlarmSound!!.start()
//
//            settings_NotifSoundSlapLayout!!.background = getDrawable(R.drawable.background_layout_selector)
//            settings_NotifSoundXyloLayout!!.background = getDrawable(R.drawable.background_layout_selector)
//            settings_NotifSoundKeyboardLayout!!.background = getDrawable(R.drawable.background_layout_selector)
//            settings_NotifSoundGuitarLayout!!.background = getDrawable(R.drawable.background_layout_selector)
//            settings_NotifSoundDrumLayout!!.background = getDrawable(R.drawable.background_layout_selector)
//            settings_NotifSoundSaxLayout!!.background = getDrawable(R.drawable.border_selected_image_view)
//
//            val edit: SharedPreferences.Editor = sharedAlarmNotifTonePreferences.edit()
//            edit.putInt("Alarm_Notif_Tone", R.raw.sax_sms)
//            edit.apply()
//        }

        //endregion
    }

    //region ========================================== Functions =========================================

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
                MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                        .setTitle(R.string.help)
                        .setBackground(getDrawable(R.color.backgroundColor))
                        .setMessage(this.resources.getString(R.string.help_notification_manager))
                        .show()
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
                .setNegativeButton(R.string.alert_dialog_no)
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

    fun milliSecondToString(ms: Int): String {
        var detik = TimeUnit.MILLISECONDS.toSeconds(ms.toLong())
        val menit = TimeUnit.SECONDS.toSeconds(detik)

        detik %= 60

        return "$menit : $detik"
    }

    inner class UpdateSeekBarProgressThread : Runnable {
        override fun run() {
            val currTime = settings_NotificationMessagesAlarmSound!!.currentPosition
            settings_NotificationSoundCurrTimeText!!.text = milliSecondToString(currTime)
            settings_NotificationSoundSlapSeekbar!!.progress = currTime

            if (currTime != settings_NotificationMessagesAlarmSound!!.duration) {
                handler.postDelayed(this, 50)
            }
        }
    }

    fun audioPlay() {
        settings_NotificationMessagesAlarmSound = MediaPlayer.create(this, R.raw.bass_slap)
        settings_NotificationSoundSlapSeekbar!!.max = settings_NotificationMessagesAlarmSound!!.duration
        settings_NotificationSoundMaxTimeText!!.text = milliSecondToString(settings_NotificationSoundSlapSeekbar!!.max)
        settings_NotificationSoundCurrTimeText!!.text = milliSecondToString(settings_NotificationMessagesAlarmSound!!.currentPosition)
        settings_NotificationSoundSlapSeekbar!!.progress = settings_NotificationMessagesAlarmSound!!.currentPosition
        settings_NotificationMessagesAlarmSound!!.start()

        val updateSeekBarThread = UpdateSeekBarProgressThread()
        handler.postDelayed(updateSeekBarThread, 50)
        settings_NotificationSoundSlapPlayButton!!.visibility = View.GONE
        settings_NotificationSoundSlapStopButton!!.visibility = View.VISIBLE
    }

    fun audioStop() {
        settings_NotificationMessagesAlarmSound!!.stop()
        settings_NotificationSoundSlapPlayButton!!.visibility = View.VISIBLE
        settings_NotificationSoundSlapStopButton!!.visibility = View.GONE
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


    /**
     * Notification that the progress level has changed. Clients can use the fromUser parameter
     * to distinguish user-initiated changes from those that occurred programmatically.
     *
     * @param seekBar The SeekBar whose progress has changed
     * @param progress The current progress level. This will be in the range min..max where min
     * and max were set by [ProgressBar.setMin] and
     * [ProgressBar.setMax], respectively. (The default values for
     * min is 0 and max is 100.)
     * @param fromUser True if the progress change was initiated by the user.
     */
    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
    }

    /**
     * Notification that the user has finished a touch gesture. Clients may want to use this
     * to re-enable advancing the seekbar.
     * @param seekBar The SeekBar in which the touch gesture began
     */
    override fun onStopTrackingTouch(seekBar: SeekBar?) {
    }

    /**
     * Notification that the user has started a touch gesture. Clients may want to use this
     * to disable advancing the seekbar.
     * @param seekBar The SeekBar in which the touch gesture began
     */
    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        seekBar?.progress?.let { settings_NotificationMessagesAlarmSound!!.seekTo(it) }
    }

    //endregion
}