package com.yellowtwigs.knockin.controller.activity

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.*
import android.graphics.Point
import android.media.MediaPlayer
import android.media.session.MediaController
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.controller.NotificationSender
import com.yellowtwigs.knockin.controller.activity.firstLaunch.MultiSelectActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.yellowtwigs.knockin.controller.activity.group.GroupManagerActivity
import kotlinx.android.synthetic.main.activity_manage_notification.*
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * La Classe qui permet d'activer ou desactiver les notifications de Knockin
 * @author Florian Striebel
 */
class ManageNotificationActivity : AppCompatActivity() {

    //region ========================================== Var or Val ==========================================

    private var drawerLayout: DrawerLayout? = null
    private var activityVisible: Boolean = false
    private var switchPopupNotif: Switch? = null
    private var switchservice: Switch? = null
    private var isTrue = false

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

    private var numberDefault: Int = 0

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
        numberDefault = sharedAlarmNotifTonePreferences.getInt("Alarm_Notif_Tone", R.raw.sms_ring)

        val sharedNotifJazzySoundInAppPreferences: SharedPreferences = getSharedPreferences("Notif_Jazzy_Sound_IsBought", Context.MODE_PRIVATE)
        notifJazzySoundIsBought = sharedNotifJazzySoundInAppPreferences.getBoolean("Notif_Jazzy_Sound_IsBought", false)

        val sharedNotifRelaxationSoundInAppPreferences: SharedPreferences = getSharedPreferences("Notif_Relaxation_Sound_IsBought", Context.MODE_PRIVATE)
        notifRelaxationSoundIsBought = sharedNotifRelaxationSoundInAppPreferences.getBoolean("Notif_Relaxation_Sound_IsBought", false)

        val sharedNotifFunkySoundInAppPreferences: SharedPreferences = getSharedPreferences("Notif_Funky_Sound_IsBought", Context.MODE_PRIVATE)
        notifFunkySoundIsBought = sharedNotifFunkySoundInAppPreferences.getBoolean("Notif_Funky_Sound_IsBought", false)

        //region ======================================= FindViewById =======================================

        switchPopupNotif = this.findViewById(R.id.switch_stop_popup)
        switchservice = this.findViewById(R.id.switch_stop_service)
        val switchReminder = this.findViewById<Switch>(R.id.switch_manage_notif_reminder)
        val remindHour = this.findViewById<TextView>(R.id.textView_heure)
        val viewHour = this.findViewById<ConstraintLayout>(R.id.modify_hour_Constariant)

        //region Default Sound

        settings_ChooseNotifDefaultSoundLayoutOpenClose = findViewById(R.id.settings_choose_default_sound_layout)
        settings_ChooseNotifDefaultSoundImageOpen = findViewById(R.id.settings_choose_notif_default_sound_image_open)
        settings_ChooseNotifDefaultSoundImageClose = findViewById(R.id.settings_choose_notif_default_sound_image_close)

        settings_NotifNoSoundLayout = findViewById(R.id.settings_notif_no_sound_layout)
        settings_NotifNoSoundCheckbox = findViewById(R.id.settings_notif_no_sound_checkbox)

        settings_NotifSoundKnockinLayout = findViewById(R.id.settings_notif_sound_knockin_layout)
        settings_NotifSoundKnockinCheckbox = findViewById(R.id.settings_notif_sound_knockin_checkbox)

        settings_NotifSoundXyloLayout = findViewById(R.id.settings_notif_sound_xylo_layout)
        settings_NotifSoundXyloCheckbox = findViewById(R.id.settings_notif_sound_xylo_checkbox)

        //endregion

        //region Jazzy Sound

        settings_ChooseNotifJazzySoundLayoutOpenClose = findViewById(R.id.settings_choose_jazzy_sound_layout)
        settings_ChooseNotifJazzySoundImageOpen = findViewById(R.id.settings_choose_notif_jazzy_sound_image_open)
        settings_ChooseNotifJazzySoundImageClose = findViewById(R.id.settings_choose_notif_jazzy_sound_image_close)

        settings_NotifSoundMoaninLayout = findViewById(R.id.settings_notif_sound_moanin_layout)
        settings_NotifSoundMoaninCheckbox = findViewById(R.id.settings_notif_sound_moanin_checkbox)

        settings_NotifSoundBlueBossaLayout = findViewById(R.id.settings_notif_sound_blue_bossa_layout)
        settings_NotifSoundBlueBossaCheckbox = findViewById(R.id.settings_notif_sound_blue_bossa_checkbox)

        settings_NotifSoundCaravanLayout = findViewById(R.id.settings_notif_sound_caravan_layout)
        settings_NotifSoundCaravanCheckbox = findViewById(R.id.settings_notif_sound_caravan_checkbox)

        settings_NotifSoundAutumnLeavesLayout = findViewById(R.id.settings_notif_sound_autumn_leaves_layout)
        settings_NotifSoundAutumnLeavesCheckbox = findViewById(R.id.settings_notif_sound_autumn_leaves_checkbox)

        settings_NotifSoundDolphinDanceLayout = findViewById(R.id.settings_notif_sound_dolphin_dance_layout)
        settings_NotifSoundDolphinDanceCheckbox = findViewById(R.id.settings_notif_sound_dolphin_dance_checkbox)

        settings_NotifSoundFreddieFreeloaderLayout = findViewById(R.id.settings_notif_sound_freddie_freeloader_layout)
        settings_NotifSoundFreddieFreeloaderCheckbox = findViewById(R.id.settings_notif_sound_freddie_freeloader_checkbox)

        //endregion

        //region Funky Sound

        settings_ChooseNotifFunkySoundLayoutOpenClose = findViewById(R.id.settings_choose_funky_sound_layout)
        settings_ChooseNotifFunkySoundImageOpen = findViewById(R.id.settings_choose_notif_funky_sound_image_open)
        settings_ChooseNotifFunkySoundImageClose = findViewById(R.id.settings_choose_notif_funky_sound_image_close)

        settings_NotifSoundSlapLayout = findViewById(R.id.settings_notif_sound_slap_layout)
        settings_NotifSoundSlapCheckbox = findViewById(R.id.settings_notif_sound_slap_checkbox)

        settings_NotifSoundOffTheCurveLayout = findViewById(R.id.settings_notif_sound_off_the_curve_layout)
        settings_NotifSoundOffTheCurveCheckbox = findViewById(R.id.settings_notif_sound_off_the_curve_checkbox)

        settings_NotifSoundFunkYallLayout = findViewById(R.id.settings_notif_sound_funk_yall_layout)
        settings_NotifSoundFunkYallCheckbox = findViewById(R.id.settings_notif_sound_funk_yall_checkbox)

        settings_NotifSoundKeyboardFunkyToneLayout = findViewById(R.id.settings_notif_sound_keyboard_funky_tone_layout)
        settings_NotifSoundKeyboardFunkyToneCheckbox = findViewById(R.id.settings_notif_sound_keyboard_funky_tone_checkbox)

        settings_NotifSoundUCantHoldNoGrooveLayout = findViewById(R.id.settings_notif_sound_u_cant_hold_no_groove_layout)
        settings_NotifSoundUCantHoldNoGrooveCheckbox = findViewById(R.id.settings_notif_sound_u_cant_hold_no_groove_checkbox)

        settings_NotifSoundColdSweatLayout = findViewById(R.id.settings_notif_sound_cold_sweat_layout)
        settings_NotifSoundColdSweatCheckbox = findViewById(R.id.settings_notif_sound_cold_sweat_checkbox)

        //endregion

        //region Relaxation Sound

        settings_ChooseNotifRelaxationSoundLayoutOpenClose = findViewById(R.id.settings_choose_relaxation_sound_layout)
        settings_ChooseNotifRelaxationSoundImageOpen = findViewById(R.id.settings_choose_notif_relaxation_sound_image_open)
        settings_ChooseNotifRelaxationSoundImageClose = findViewById(R.id.settings_choose_notif_relaxation_sound_image_close)

        settings_NotifSoundAcousticGuitarLayout = findViewById(R.id.settings_notif_sound_guitar_relax_layout)
        settings_NotifSoundAcousticGuitarCheckbox = findViewById(R.id.settings_notif_sound_guitar_relax_checkbox)

        settings_NotifSoundRelaxToneLayout = findViewById(R.id.settings_notif_sound_xylo_relax_layout)
        settings_NotifSoundRelaxToneCheckbox = findViewById(R.id.settings_notif_sound_xylo_relax_checkbox)

        settings_NotifSoundGravityLayout = findViewById(R.id.settings_notif_sound_gravity_layout)
        settings_NotifSoundGravityCheckbox = findViewById(R.id.settings_notif_sound_gravity_checkbox)

        settings_NotifSoundSlowDancingLayout = findViewById(R.id.settings_notif_sound_slow_dancing_layout)
        settings_NotifSoundSlowDancingCheckbox = findViewById(R.id.settings_notif_sound_slow_dancing_checkbox)

        settings_NotifSoundScorpionThemeLayout = findViewById(R.id.settings_notif_sound_scorpion_theme_layout)
        settings_NotifSoundScorpionThemeCheckbox = findViewById(R.id.settings_notif_sound_scorpion_theme_checkbox)

        settings_NotifSoundFirstStepLayout = findViewById(R.id.settings_notif_sound_interstellar_theme_layout)
        settings_NotifSoundFirstStepCheckbox = findViewById(R.id.settings_notif_sound_interstellar_theme_checkbox)

        //endregion

        settings_ChooseNotifSoundTitle = findViewById(R.id.settings_choose_notif_sound_title)
        settings_ChooseNotifSoundLayout = findViewById(R.id.settings_choose_notif_sound_layout)

        val group_manager_MainLayout = findViewById<LinearLayoutCompat>(R.id.manage_my_notif_layout_id)

        val settings_left_drawer_ThemeSwitch = findViewById<Switch>(R.id.settings_left_drawer_theme_switch)

        if (sharedThemePreferences.getBoolean("darkTheme", false)) {
            settings_left_drawer_ThemeSwitch!!.isChecked = true
//            group_manager_MainLayout!!.setBackgroundResource(R.drawable.dark_background)
        }

        val main_SettingsLeftDrawerLayout = findViewById<RelativeLayout>(R.id.settings_left_drawer_layout)

        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val height = size.y
        when {
            height in 1501..2101 -> {
            }
            height < 1500 -> {
                val params = main_SettingsLeftDrawerLayout.layoutParams
                params.height = 325
                main_SettingsLeftDrawerLayout.layoutParams = params
            }
        }

        //region ================================ Call Popup from LeftDrawer ================================

        val sharedPreferencePopup = getSharedPreferences("Phone_call", Context.MODE_PRIVATE)
        val settings_CallPopupSwitch = findViewById<Switch>(R.id.settings_call_popup_switch)

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

        //region ======================================== Ring Tones ========================================

        //region OpenClose

        settings_ChooseNotifDefaultSoundLayoutOpenClose!!.setOnClickListener {
            if (settings_NotifSoundKnockinLayout!!.visibility == View.VISIBLE &&
                    settings_NotifSoundXyloLayout!!.visibility == View.VISIBLE) {
                settings_NotifSoundKnockinLayout!!.visibility = View.GONE
                settings_NotifSoundXyloLayout!!.visibility = View.GONE
                settings_NotifNoSoundLayout!!.visibility = View.GONE

                settings_ChooseNotifDefaultSoundImageOpen!!.visibility = View.GONE
                settings_ChooseNotifDefaultSoundImageClose!!.visibility = View.VISIBLE
            } else {
                settings_NotifNoSoundLayout!!.visibility = View.VISIBLE
                settings_NotifSoundKnockinLayout!!.visibility = View.VISIBLE
                settings_NotifSoundXyloLayout!!.visibility = View.VISIBLE

                settings_ChooseNotifDefaultSoundImageOpen!!.visibility = View.VISIBLE
                settings_ChooseNotifDefaultSoundImageClose!!.visibility = View.GONE
            }
        }

        settings_ChooseNotifJazzySoundLayoutOpenClose!!.setOnClickListener {
            if (settings_NotifSoundMoaninLayout!!.visibility == View.VISIBLE &&
                    settings_NotifSoundBlueBossaLayout!!.visibility == View.VISIBLE &&
                    settings_NotifSoundCaravanLayout!!.visibility == View.VISIBLE &&
                    settings_NotifSoundDolphinDanceLayout!!.visibility == View.VISIBLE &&
                    settings_NotifSoundAutumnLeavesLayout!!.visibility == View.VISIBLE &&
                    settings_NotifSoundFreddieFreeloaderLayout!!.visibility == View.VISIBLE) {

                settings_NotifSoundMoaninLayout!!.visibility = View.GONE
                settings_NotifSoundBlueBossaLayout!!.visibility = View.GONE
                settings_NotifSoundCaravanLayout!!.visibility = View.GONE
                settings_NotifSoundDolphinDanceLayout!!.visibility = View.GONE
                settings_NotifSoundAutumnLeavesLayout!!.visibility = View.GONE
                settings_NotifSoundFreddieFreeloaderLayout!!.visibility = View.GONE

                settings_ChooseNotifJazzySoundImageOpen!!.visibility = View.GONE
                settings_ChooseNotifJazzySoundImageClose!!.visibility = View.VISIBLE
            } else {
                settings_NotifSoundMoaninLayout!!.visibility = View.VISIBLE
                settings_NotifSoundBlueBossaLayout!!.visibility = View.VISIBLE
                settings_NotifSoundCaravanLayout!!.visibility = View.VISIBLE
                settings_NotifSoundDolphinDanceLayout!!.visibility = View.VISIBLE
                settings_NotifSoundAutumnLeavesLayout!!.visibility = View.VISIBLE
                settings_NotifSoundFreddieFreeloaderLayout!!.visibility = View.VISIBLE

                settings_ChooseNotifJazzySoundImageOpen!!.visibility = View.VISIBLE
                settings_ChooseNotifJazzySoundImageClose!!.visibility = View.GONE
            }
        }

        settings_ChooseNotifFunkySoundLayoutOpenClose!!.setOnClickListener {
            if (settings_NotifSoundSlapLayout!!.visibility == View.VISIBLE &&
                    settings_NotifSoundOffTheCurveLayout!!.visibility == View.VISIBLE &&
                    settings_NotifSoundFunkYallLayout!!.visibility == View.VISIBLE &&
                    settings_NotifSoundKeyboardFunkyToneLayout!!.visibility == View.VISIBLE &&
                    settings_NotifSoundUCantHoldNoGrooveLayout!!.visibility == View.VISIBLE &&
                    settings_NotifSoundColdSweatLayout!!.visibility == View.VISIBLE) {

                settings_NotifSoundSlapLayout!!.visibility = View.GONE
                settings_NotifSoundOffTheCurveLayout!!.visibility = View.GONE
                settings_NotifSoundFunkYallLayout!!.visibility = View.GONE
                settings_NotifSoundKeyboardFunkyToneLayout!!.visibility = View.GONE
                settings_NotifSoundUCantHoldNoGrooveLayout!!.visibility = View.GONE
                settings_NotifSoundColdSweatLayout!!.visibility = View.GONE

                settings_ChooseNotifFunkySoundImageOpen!!.visibility = View.GONE
                settings_ChooseNotifFunkySoundImageClose!!.visibility = View.VISIBLE
            } else {
                settings_NotifSoundSlapLayout!!.visibility = View.VISIBLE
                settings_NotifSoundOffTheCurveLayout!!.visibility = View.VISIBLE
                settings_NotifSoundFunkYallLayout!!.visibility = View.VISIBLE
                settings_NotifSoundKeyboardFunkyToneLayout!!.visibility = View.VISIBLE
                settings_NotifSoundUCantHoldNoGrooveLayout!!.visibility = View.VISIBLE
                settings_NotifSoundColdSweatLayout!!.visibility = View.VISIBLE

                settings_ChooseNotifFunkySoundImageOpen!!.visibility = View.VISIBLE
                settings_ChooseNotifFunkySoundImageClose!!.visibility = View.GONE
            }
        }

        settings_ChooseNotifRelaxationSoundLayoutOpenClose!!.setOnClickListener {
            if (settings_NotifSoundAcousticGuitarLayout!!.visibility == View.VISIBLE &&
                    settings_NotifSoundGravityLayout!!.visibility == View.VISIBLE &&
                    settings_NotifSoundSlowDancingLayout!!.visibility == View.VISIBLE &&
                    settings_NotifSoundScorpionThemeLayout!!.visibility == View.VISIBLE &&
                    settings_NotifSoundFirstStepLayout!!.visibility == View.VISIBLE &&
                    settings_NotifSoundRelaxToneLayout!!.visibility == View.VISIBLE) {

                settings_NotifSoundAcousticGuitarLayout!!.visibility = View.GONE
                settings_NotifSoundGravityLayout!!.visibility = View.GONE
                settings_NotifSoundSlowDancingLayout!!.visibility = View.GONE
                settings_NotifSoundScorpionThemeLayout!!.visibility = View.GONE
                settings_NotifSoundFirstStepLayout!!.visibility = View.GONE
                settings_NotifSoundRelaxToneLayout!!.visibility = View.GONE

                settings_ChooseNotifRelaxationSoundImageOpen!!.visibility = View.GONE
                settings_ChooseNotifRelaxationSoundImageClose!!.visibility = View.VISIBLE
            } else {
                settings_NotifSoundAcousticGuitarLayout!!.visibility = View.VISIBLE
                settings_NotifSoundGravityLayout!!.visibility = View.VISIBLE
                settings_NotifSoundSlowDancingLayout!!.visibility = View.VISIBLE
                settings_NotifSoundScorpionThemeLayout!!.visibility = View.VISIBLE
                settings_NotifSoundFirstStepLayout!!.visibility = View.VISIBLE
                settings_NotifSoundRelaxToneLayout!!.visibility = View.VISIBLE

                settings_ChooseNotifRelaxationSoundImageOpen!!.visibility = View.VISIBLE
                settings_ChooseNotifRelaxationSoundImageClose!!.visibility = View.GONE
            }
        }

        //endregion

        refreshChecked()
        ringToneLayoutClosed()

        //region Default

        settings_NotifNoSoundCheckbox!!.setOnClickListener {
            if (settings_NotificationMessagesAlarmSound != null) {
                settings_NotificationMessagesAlarmSound!!.stop()
            }

            if (settings_NotifNoSoundCheckbox!!.isChecked) {

//                settings_NotifNoSoundCheckbox!!.isChecked = false
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

                val edit: SharedPreferences.Editor = sharedAlarmNotifTonePreferences.edit()
                edit.putInt("Alarm_Notif_Tone", 1)
                edit.apply()
            }
        }

        settings_NotifSoundKnockinCheckbox!!.setOnClickListener {
            if (settings_NotificationMessagesAlarmSound != null) {
                settings_NotificationMessagesAlarmSound!!.stop()
            }

            if (settings_NotifSoundKnockinCheckbox!!.isChecked) {
                settings_NotificationMessagesAlarmSound = MediaPlayer.create(this, R.raw.sms_ring)
                settings_NotificationMessagesAlarmSound!!.start()

                settings_NotifNoSoundCheckbox!!.isChecked = false
//                settings_NotifSoundKnockinCheckbox!!.isChecked = false
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


                val edit: SharedPreferences.Editor = sharedAlarmNotifTonePreferences.edit()
                edit.putInt("Alarm_Notif_Tone", R.raw.sms_ring)
                edit.apply()
            }
        }

        settings_NotifSoundXyloCheckbox!!.setOnClickListener {
            if (settings_NotificationMessagesAlarmSound != null) {
                settings_NotificationMessagesAlarmSound!!.stop()
            }

            if (settings_NotifSoundXyloCheckbox!!.isChecked) {
                settings_NotificationMessagesAlarmSound = MediaPlayer.create(this, R.raw.xylophone_tone)
                settings_NotificationMessagesAlarmSound!!.start()

                settings_NotifNoSoundCheckbox!!.isChecked = false
                settings_NotifSoundKnockinCheckbox!!.isChecked = false
//                settings_NotifSoundXyloCheckbox!!.isChecked = false

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

                val edit: SharedPreferences.Editor = sharedAlarmNotifTonePreferences.edit()
                edit.putInt("Alarm_Notif_Tone", R.raw.xylophone_tone)
                edit.apply()
            }
        }

        //endregion

        //region Jazzy

        settings_NotifSoundMoaninCheckbox!!.setOnClickListener {
            if (settings_NotificationMessagesAlarmSound != null) {
                settings_NotificationMessagesAlarmSound!!.stop()
            }

            if (settings_NotifSoundMoaninCheckbox!!.isChecked) {
                settings_NotificationMessagesAlarmSound = MediaPlayer.create(this, R.raw.moanin_jazz)
                settings_NotificationMessagesAlarmSound!!.start()

                settings_NotifNoSoundCheckbox!!.isChecked = false
                settings_NotifSoundKnockinCheckbox!!.isChecked = false
                settings_NotifSoundXyloCheckbox!!.isChecked = false

//                settings_NotifSoundMoaninCheckbox!!.isChecked = false
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

                if (notifJazzySoundIsBought) {
                    val edit: SharedPreferences.Editor = sharedAlarmNotifTonePreferences.edit()
                    edit.putInt("Alarm_Notif_Tone", R.raw.moanin_jazz)
                    edit.apply()
                } else {
                    MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                            .setTitle(getString(R.string.in_app_popup_tone_available_title))
                            .setMessage(getString(R.string.in_app_popup_tone_available_message))
                            .setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
                                goToPremiumActivity()
                            }
                            .setNegativeButton(R.string.alert_dialog_later) { _, _ ->
                                refreshChecked()
                            }
                            .show()
                }
            }
        }

        settings_NotifSoundBlueBossaCheckbox!!.setOnClickListener {
            if (settings_NotificationMessagesAlarmSound != null) {
                settings_NotificationMessagesAlarmSound!!.stop()
            }

            if (settings_NotifSoundBlueBossaCheckbox!!.isChecked) {

                settings_NotifNoSoundCheckbox!!.isChecked = false
                settings_NotifSoundKnockinCheckbox!!.isChecked = false
                settings_NotifSoundXyloCheckbox!!.isChecked = false

                settings_NotifSoundMoaninCheckbox!!.isChecked = false
//                settings_NotifSoundBlueBossaCheckbox!!.isChecked = false
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

                if (notifJazzySoundIsBought) {

                    settings_NotificationMessagesAlarmSound = MediaPlayer.create(this, R.raw.blue_bossa)
                    settings_NotificationMessagesAlarmSound!!.start()

                    val edit: SharedPreferences.Editor = sharedAlarmNotifTonePreferences.edit()
                    edit.putInt("Alarm_Notif_Tone", R.raw.blue_bossa)
                    edit.apply()
                } else {
                    MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                            .setTitle(getString(R.string.in_app_popup_tone_available_title))
                            .setMessage(getString(R.string.in_app_popup_tone_available_message))
                            .setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
                                goToPremiumActivity()
                            }
                            .setNegativeButton(R.string.alert_dialog_later) { _, _ ->
                                refreshChecked()
                            }
                            .show()
                }
            }
        }

        settings_NotifSoundCaravanCheckbox!!.setOnClickListener {
            if (settings_NotificationMessagesAlarmSound != null) {
                settings_NotificationMessagesAlarmSound!!.stop()
            }

            if (settings_NotifSoundCaravanCheckbox!!.isChecked) {

                settings_NotifNoSoundCheckbox!!.isChecked = false
                settings_NotifSoundKnockinCheckbox!!.isChecked = false
                settings_NotifSoundXyloCheckbox!!.isChecked = false

                settings_NotifSoundMoaninCheckbox!!.isChecked = false
                settings_NotifSoundBlueBossaCheckbox!!.isChecked = false
//                settings_NotifSoundCaravanCheckbox!!.isChecked = false
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

                if (notifJazzySoundIsBought) {

                    settings_NotificationMessagesAlarmSound = MediaPlayer.create(this, R.raw.caravan)
                    settings_NotificationMessagesAlarmSound!!.start()

                    val edit: SharedPreferences.Editor = sharedAlarmNotifTonePreferences.edit()
                    edit.putInt("Alarm_Notif_Tone", R.raw.caravan)
                    edit.apply()
                } else {
                    MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                            .setTitle(getString(R.string.in_app_popup_tone_available_title))
                            .setMessage(getString(R.string.in_app_popup_tone_available_message))
                            .setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
                                goToPremiumActivity()
                            }
                            .setNegativeButton(R.string.alert_dialog_later) { _, _ ->
                                refreshChecked()
                            }
                            .show()
                }
            }
        }

        settings_NotifSoundDolphinDanceCheckbox!!.setOnClickListener {
            if (settings_NotificationMessagesAlarmSound != null) {
                settings_NotificationMessagesAlarmSound!!.stop()
            }

            if (settings_NotifSoundDolphinDanceCheckbox!!.isChecked) {

                settings_NotifNoSoundCheckbox!!.isChecked = false
                settings_NotifSoundKnockinCheckbox!!.isChecked = false
                settings_NotifSoundXyloCheckbox!!.isChecked = false

                settings_NotifSoundMoaninCheckbox!!.isChecked = false
                settings_NotifSoundBlueBossaCheckbox!!.isChecked = false
                settings_NotifSoundCaravanCheckbox!!.isChecked = false
//                settings_NotifSoundDolphinDanceCheckbox!!.isChecked = false
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

                if (notifJazzySoundIsBought) {

                    settings_NotificationMessagesAlarmSound = MediaPlayer.create(this, R.raw.dolphin_dance)
                    settings_NotificationMessagesAlarmSound!!.start()

                    val edit: SharedPreferences.Editor = sharedAlarmNotifTonePreferences.edit()
                    edit.putInt("Alarm_Notif_Tone", R.raw.dolphin_dance)
                    edit.apply()
                } else {
                    MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                            .setTitle(getString(R.string.in_app_popup_tone_available_title))
                            .setMessage(getString(R.string.in_app_popup_tone_available_message))
                            .setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
                                goToPremiumActivity()
                            }
                            .setNegativeButton(R.string.alert_dialog_later) { _, _ ->
                                refreshChecked()
                            }
                            .show()
                }
            }
        }

        settings_NotifSoundAutumnLeavesCheckbox!!.setOnClickListener {
            if (settings_NotificationMessagesAlarmSound != null) {
                settings_NotificationMessagesAlarmSound!!.stop()
            }

            if (settings_NotifSoundAutumnLeavesCheckbox!!.isChecked) {

                settings_NotifNoSoundCheckbox!!.isChecked = false
                settings_NotifSoundKnockinCheckbox!!.isChecked = false
                settings_NotifSoundXyloCheckbox!!.isChecked = false

                settings_NotifSoundMoaninCheckbox!!.isChecked = false
                settings_NotifSoundBlueBossaCheckbox!!.isChecked = false
                settings_NotifSoundCaravanCheckbox!!.isChecked = false
                settings_NotifSoundDolphinDanceCheckbox!!.isChecked = false
//                settings_NotifSoundAutumnLeavesCheckbox!!.isChecked = false
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

                if (notifJazzySoundIsBought) {

                    settings_NotificationMessagesAlarmSound = MediaPlayer.create(this, R.raw.autumn_leaves)
                    settings_NotificationMessagesAlarmSound!!.start()

                    val edit: SharedPreferences.Editor = sharedAlarmNotifTonePreferences.edit()
                    edit.putInt("Alarm_Notif_Tone", R.raw.autumn_leaves)
                    edit.apply()
                } else {
                    MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                            .setTitle(getString(R.string.in_app_popup_tone_available_title))
                            .setMessage(getString(R.string.in_app_popup_tone_available_message))
                            .setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
                                goToPremiumActivity()
                            }
                            .setNegativeButton(R.string.alert_dialog_later) { _, _ ->
                                refreshChecked()
                            }
                            .show()
                }
            }
        }

        settings_NotifSoundFreddieFreeloaderCheckbox!!.setOnClickListener {
            if (settings_NotificationMessagesAlarmSound != null) {
                settings_NotificationMessagesAlarmSound!!.stop()
            }

            if (settings_NotifSoundFreddieFreeloaderCheckbox!!.isChecked) {

                settings_NotifNoSoundCheckbox!!.isChecked = false
                settings_NotifSoundKnockinCheckbox!!.isChecked = false
                settings_NotifSoundXyloCheckbox!!.isChecked = false

                settings_NotifSoundMoaninCheckbox!!.isChecked = false
                settings_NotifSoundBlueBossaCheckbox!!.isChecked = false
                settings_NotifSoundCaravanCheckbox!!.isChecked = false
                settings_NotifSoundDolphinDanceCheckbox!!.isChecked = false
                settings_NotifSoundAutumnLeavesCheckbox!!.isChecked = false
//                settings_NotifSoundFreddieFreeloaderCheckbox!!.isChecked = false

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

                if (notifJazzySoundIsBought) {

                    settings_NotificationMessagesAlarmSound = MediaPlayer.create(this, R.raw.freddie_freeloader)
                    settings_NotificationMessagesAlarmSound!!.start()

                    val edit: SharedPreferences.Editor = sharedAlarmNotifTonePreferences.edit()
                    edit.putInt("Alarm_Notif_Tone", R.raw.freddie_freeloader)
                    edit.apply()
                } else {
                    MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                            .setTitle(getString(R.string.in_app_popup_tone_available_title))
                            .setMessage(getString(R.string.in_app_popup_tone_available_message))
                            .setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
                                goToPremiumActivity()
                            }
                            .setNegativeButton(R.string.alert_dialog_later) { _, _ ->
                                refreshChecked()
                            }
                            .show()
                }
            }
        }

        //endregion

        //region Funky

        settings_NotifSoundSlapCheckbox!!.setOnClickListener {
            if (settings_NotificationMessagesAlarmSound != null) {
                settings_NotificationMessagesAlarmSound!!.stop()
            }

            if (settings_NotifSoundSlapCheckbox!!.isChecked) {
                settings_NotificationMessagesAlarmSound = MediaPlayer.create(this, R.raw.bass_slap)
                settings_NotificationMessagesAlarmSound!!.start()

                settings_NotifNoSoundCheckbox!!.isChecked = false
                settings_NotifSoundKnockinCheckbox!!.isChecked = false
                settings_NotifSoundXyloCheckbox!!.isChecked = false

                settings_NotifSoundMoaninCheckbox!!.isChecked = false
                settings_NotifSoundBlueBossaCheckbox!!.isChecked = false
                settings_NotifSoundCaravanCheckbox!!.isChecked = false
                settings_NotifSoundDolphinDanceCheckbox!!.isChecked = false
                settings_NotifSoundAutumnLeavesCheckbox!!.isChecked = false
                settings_NotifSoundFreddieFreeloaderCheckbox!!.isChecked = false

//                settings_NotifSoundSlapCheckbox!!.isChecked = false
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

                if (notifFunkySoundIsBought) {
                    val edit: SharedPreferences.Editor = sharedAlarmNotifTonePreferences.edit()
                    edit.putInt("Alarm_Notif_Tone", R.raw.bass_slap)
                    edit.apply()
                } else {
                    MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                            .setTitle(getString(R.string.in_app_popup_tone_available_title))
                            .setMessage(getString(R.string.in_app_popup_tone_available_message))
                            .setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
                                goToPremiumActivity()
                            }
                            .setNegativeButton(R.string.alert_dialog_later) { _, _ ->
                                refreshChecked()
                            }
                            .show()
                }
            }
        }

        settings_NotifSoundOffTheCurveCheckbox!!.setOnClickListener {
            if (settings_NotificationMessagesAlarmSound != null) {
                settings_NotificationMessagesAlarmSound!!.stop()
            }

            if (settings_NotifSoundOffTheCurveCheckbox!!.isChecked) {

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
//                settings_NotifSoundOffTheCurveCheckbox!!.isChecked = false
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

                if (notifFunkySoundIsBought) {
                    settings_NotificationMessagesAlarmSound = MediaPlayer.create(this, R.raw.off_the_curve_groove)
                    settings_NotificationMessagesAlarmSound!!.start()

                    val edit: SharedPreferences.Editor = sharedAlarmNotifTonePreferences.edit()
                    edit.putInt("Alarm_Notif_Tone", R.raw.off_the_curve_groove)
                    edit.apply()

                } else {
                    MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                            .setTitle(getString(R.string.in_app_popup_tone_available_title))
                            .setMessage(getString(R.string.in_app_popup_tone_available_message))
                            .setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
                                goToPremiumActivity()
                            }
                            .setNegativeButton(R.string.alert_dialog_later) { _, _ ->
                                refreshChecked()
                            }
                            .show()
                }
            }
        }

        settings_NotifSoundFunkYallCheckbox!!.setOnClickListener {
            if (settings_NotificationMessagesAlarmSound != null) {
                settings_NotificationMessagesAlarmSound!!.stop()
            }

            if (settings_NotifSoundFunkYallCheckbox!!.isChecked) {

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
//                settings_NotifSoundFunkYallCheckbox!!.isChecked = false
                settings_NotifSoundKeyboardFunkyToneCheckbox!!.isChecked = false
                settings_NotifSoundUCantHoldNoGrooveCheckbox!!.isChecked = false
                settings_NotifSoundColdSweatCheckbox!!.isChecked = false

                settings_NotifSoundAcousticGuitarCheckbox!!.isChecked = false
                settings_NotifSoundGravityCheckbox!!.isChecked = false
                settings_NotifSoundSlowDancingCheckbox!!.isChecked = false
                settings_NotifSoundScorpionThemeCheckbox!!.isChecked = false
                settings_NotifSoundFirstStepCheckbox!!.isChecked = false
                settings_NotifSoundRelaxToneCheckbox!!.isChecked = false

                if (notifFunkySoundIsBought) {

                    settings_NotificationMessagesAlarmSound = MediaPlayer.create(this, R.raw.funk_yall)
                    settings_NotificationMessagesAlarmSound!!.start()

                    val edit: SharedPreferences.Editor = sharedAlarmNotifTonePreferences.edit()
                    edit.putInt("Alarm_Notif_Tone", R.raw.off_the_curve_groove)
                    edit.apply()
                } else {
                    MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                            .setTitle(getString(R.string.in_app_popup_tone_available_title))
                            .setMessage(getString(R.string.in_app_popup_tone_available_message))
                            .setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
                                goToPremiumActivity()
                            }
                            .setNegativeButton(R.string.alert_dialog_later) { _, _ ->
                                refreshChecked()
                            }
                            .show()
                }
            }
        }

        settings_NotifSoundKeyboardFunkyToneCheckbox!!.setOnClickListener {
            if (settings_NotificationMessagesAlarmSound != null) {
                settings_NotificationMessagesAlarmSound!!.stop()
            }

            if (settings_NotifSoundKeyboardFunkyToneCheckbox!!.isChecked) {

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
//                settings_NotifSoundKeyboardFunkyToneCheckbox!!.isChecked = false
                settings_NotifSoundUCantHoldNoGrooveCheckbox!!.isChecked = false
                settings_NotifSoundColdSweatCheckbox!!.isChecked = false

                settings_NotifSoundAcousticGuitarCheckbox!!.isChecked = false
                settings_NotifSoundGravityCheckbox!!.isChecked = false
                settings_NotifSoundSlowDancingCheckbox!!.isChecked = false
                settings_NotifSoundScorpionThemeCheckbox!!.isChecked = false
                settings_NotifSoundFirstStepCheckbox!!.isChecked = false
                settings_NotifSoundRelaxToneCheckbox!!.isChecked = false

                if (notifFunkySoundIsBought) {

                    settings_NotificationMessagesAlarmSound = MediaPlayer.create(this, R.raw.keyboard_funky_tone)
                    settings_NotificationMessagesAlarmSound!!.start()

                    val edit: SharedPreferences.Editor = sharedAlarmNotifTonePreferences.edit()
                    edit.putInt("Alarm_Notif_Tone", R.raw.keyboard_funky_tone)
                    edit.apply()
                } else {
                    MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                            .setTitle(getString(R.string.in_app_popup_tone_available_title))
                            .setMessage(getString(R.string.in_app_popup_tone_available_message))
                            .setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
                                goToPremiumActivity()
                            }
                            .setNegativeButton(R.string.alert_dialog_later) { _, _ ->
                                refreshChecked()
                            }
                            .show()
                }
            }
        }

        settings_NotifSoundUCantHoldNoGrooveCheckbox!!.setOnClickListener {
            if (settings_NotificationMessagesAlarmSound != null) {
                settings_NotificationMessagesAlarmSound!!.stop()
            }

            if (settings_NotifSoundUCantHoldNoGrooveCheckbox!!.isChecked) {

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
//                settings_NotifSoundUCantHoldNoGrooveCheckbox!!.isChecked = false
                settings_NotifSoundColdSweatCheckbox!!.isChecked = false

                settings_NotifSoundAcousticGuitarCheckbox!!.isChecked = false
                settings_NotifSoundGravityCheckbox!!.isChecked = false
                settings_NotifSoundSlowDancingCheckbox!!.isChecked = false
                settings_NotifSoundScorpionThemeCheckbox!!.isChecked = false
                settings_NotifSoundFirstStepCheckbox!!.isChecked = false
                settings_NotifSoundRelaxToneCheckbox!!.isChecked = false

                if (notifFunkySoundIsBought) {

                    settings_NotificationMessagesAlarmSound = MediaPlayer.create(this, R.raw.u_cant_hold_no_groove)
                    settings_NotificationMessagesAlarmSound!!.start()

                    val edit: SharedPreferences.Editor = sharedAlarmNotifTonePreferences.edit()
                    edit.putInt("Alarm_Notif_Tone", R.raw.u_cant_hold_no_groove)
                    edit.apply()
                } else {
                    MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                            .setTitle(getString(R.string.in_app_popup_tone_available_title))
                            .setMessage(getString(R.string.in_app_popup_tone_available_message))
                            .setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
                                goToPremiumActivity()
                            }
                            .setNegativeButton(R.string.alert_dialog_later) { _, _ ->
                                refreshChecked()
                            }
                            .show()
                }
            }
        }

        settings_NotifSoundColdSweatCheckbox!!.setOnClickListener {
            if (settings_NotificationMessagesAlarmSound != null) {
                settings_NotificationMessagesAlarmSound!!.stop()
            }

            if (settings_NotifSoundColdSweatCheckbox!!.isChecked) {

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

                settings_NotifSoundAcousticGuitarCheckbox!!.isChecked = false
                settings_NotifSoundGravityCheckbox!!.isChecked = false
                settings_NotifSoundSlowDancingCheckbox!!.isChecked = false
                settings_NotifSoundScorpionThemeCheckbox!!.isChecked = false
                settings_NotifSoundFirstStepCheckbox!!.isChecked = false
                settings_NotifSoundRelaxToneCheckbox!!.isChecked = false

                if (notifFunkySoundIsBought) {

                    settings_NotificationMessagesAlarmSound = MediaPlayer.create(this, R.raw.cold_sweat)
                    settings_NotificationMessagesAlarmSound!!.start()

                    val edit: SharedPreferences.Editor = sharedAlarmNotifTonePreferences.edit()
                    edit.putInt("Alarm_Notif_Tone", R.raw.cold_sweat)
                    edit.apply()
                } else {
                    MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                            .setTitle(getString(R.string.in_app_popup_tone_available_title))
                            .setMessage(getString(R.string.in_app_popup_tone_available_message))
                            .setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
                                goToPremiumActivity()
                            }
                            .setNegativeButton(R.string.alert_dialog_later) { _, _ ->
                                refreshChecked()
                            }
                            .show()
                }
            }
        }

        //endregion

        //region Relaxation

        settings_NotifSoundAcousticGuitarCheckbox!!.setOnClickListener {
            if (settings_NotificationMessagesAlarmSound != null) {
                settings_NotificationMessagesAlarmSound!!.stop()
            }

            if (settings_NotifSoundAcousticGuitarCheckbox!!.isChecked) {

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

//                settings_NotifSoundAcousticGuitarCheckbox!!.isChecked = false
                settings_NotifSoundGravityCheckbox!!.isChecked = false
                settings_NotifSoundSlowDancingCheckbox!!.isChecked = false
                settings_NotifSoundScorpionThemeCheckbox!!.isChecked = false
                settings_NotifSoundFirstStepCheckbox!!.isChecked = false
                settings_NotifSoundRelaxToneCheckbox!!.isChecked = false

                if (notifRelaxationSoundIsBought) {
                    settings_NotificationMessagesAlarmSound = MediaPlayer.create(this, R.raw.beautiful_chords_progression)
                    settings_NotificationMessagesAlarmSound!!.start()

                    val edit: SharedPreferences.Editor = sharedAlarmNotifTonePreferences.edit()
                    edit.putInt("Alarm_Notif_Tone", R.raw.beautiful_chords_progression)
                    edit.apply()
                } else {
                    MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                            .setTitle(getString(R.string.in_app_popup_tone_available_title))
                            .setMessage(getString(R.string.in_app_popup_tone_available_message))
                            .setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
                                goToPremiumActivity()
                            }
                            .setNegativeButton(R.string.alert_dialog_later) { _, _ ->
                                refreshChecked()
                            }
                            .show()
                }
            }
        }

        settings_NotifSoundGravityCheckbox!!.setOnClickListener {
            if (settings_NotificationMessagesAlarmSound != null) {
                settings_NotificationMessagesAlarmSound!!.stop()
            }

            if (settings_NotifSoundGravityCheckbox!!.isChecked) {

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
//                settings_NotifSoundGravityCheckbox!!.isChecked = false
                settings_NotifSoundSlowDancingCheckbox!!.isChecked = false
                settings_NotifSoundScorpionThemeCheckbox!!.isChecked = false
                settings_NotifSoundFirstStepCheckbox!!.isChecked = false
                settings_NotifSoundRelaxToneCheckbox!!.isChecked = false

                if (notifRelaxationSoundIsBought) {
                    settings_NotificationMessagesAlarmSound = MediaPlayer.create(this, R.raw.gravity)
                    settings_NotificationMessagesAlarmSound!!.start()

                    val edit: SharedPreferences.Editor = sharedAlarmNotifTonePreferences.edit()
                    edit.putInt("Alarm_Notif_Tone", R.raw.gravity)
                    edit.apply()

                } else {
                    MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                            .setTitle(getString(R.string.in_app_popup_tone_available_title))
                            .setMessage(getString(R.string.in_app_popup_tone_available_message))
                            .setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
                                goToPremiumActivity()
                            }
                            .setNegativeButton(R.string.alert_dialog_later) { _, _ ->
                                refreshChecked()
                            }
                            .show()
                }
            }
        }

        settings_NotifSoundSlowDancingCheckbox!!.setOnClickListener {
            if (settings_NotificationMessagesAlarmSound != null) {
                settings_NotificationMessagesAlarmSound!!.stop()
            }

            if (settings_NotifSoundSlowDancingCheckbox!!.isChecked) {

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
//                settings_NotifSoundSlowDancingCheckbox!!.isChecked = false
                settings_NotifSoundScorpionThemeCheckbox!!.isChecked = false
                settings_NotifSoundFirstStepCheckbox!!.isChecked = false
                settings_NotifSoundRelaxToneCheckbox!!.isChecked = false

                if (notifRelaxationSoundIsBought) {

                    settings_NotificationMessagesAlarmSound = MediaPlayer.create(this, R.raw.slow_dancing)
                    settings_NotificationMessagesAlarmSound!!.start()

                    val edit: SharedPreferences.Editor = sharedAlarmNotifTonePreferences.edit()
                    edit.putInt("Alarm_Notif_Tone", R.raw.slow_dancing)
                    edit.apply()
                } else {
                    MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                            .setTitle(getString(R.string.in_app_popup_tone_available_title))
                            .setMessage(getString(R.string.in_app_popup_tone_available_message))
                            .setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
                                goToPremiumActivity()
                            }
                            .setNegativeButton(R.string.alert_dialog_later) { _, _ ->
                                refreshChecked()
                            }
                            .show()
                }
            }
        }

        settings_NotifSoundScorpionThemeCheckbox!!.setOnClickListener {
            if (settings_NotificationMessagesAlarmSound != null) {
                settings_NotificationMessagesAlarmSound!!.stop()
            }

            if (settings_NotifSoundScorpionThemeCheckbox!!.isChecked) {

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
//                settings_NotifSoundScorpionThemeCheckbox!!.isChecked = false
                settings_NotifSoundFirstStepCheckbox!!.isChecked = false
                settings_NotifSoundRelaxToneCheckbox!!.isChecked = false

                if (notifRelaxationSoundIsBought) {

                    settings_NotificationMessagesAlarmSound = MediaPlayer.create(this, R.raw.fade_to_black)
                    settings_NotificationMessagesAlarmSound!!.start()

                    val edit: SharedPreferences.Editor = sharedAlarmNotifTonePreferences.edit()
                    edit.putInt("Alarm_Notif_Tone", R.raw.fade_to_black)
                    edit.apply()
                } else {
                    MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                            .setTitle(getString(R.string.in_app_popup_tone_available_title))
                            .setMessage(getString(R.string.in_app_popup_tone_available_message))
                            .setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
                                goToPremiumActivity()
                            }
                            .setNegativeButton(R.string.alert_dialog_later) { _, _ ->
                                refreshChecked()
                            }
                            .show()
                }
            }
        }

        settings_NotifSoundFirstStepCheckbox!!.setOnClickListener {
            if (settings_NotificationMessagesAlarmSound != null) {
                settings_NotificationMessagesAlarmSound!!.stop()
            }

            if (settings_NotifSoundFirstStepCheckbox!!.isChecked) {

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
//                settings_NotifSoundFirstStepCheckbox!!.isChecked = false
                settings_NotifSoundRelaxToneCheckbox!!.isChecked = false

                if (notifRelaxationSoundIsBought) {

                    settings_NotificationMessagesAlarmSound = MediaPlayer.create(this, R.raw.interstellar_main_theme)
                    settings_NotificationMessagesAlarmSound!!.start()

                    val edit: SharedPreferences.Editor = sharedAlarmNotifTonePreferences.edit()
                    edit.putInt("Alarm_Notif_Tone", R.raw.interstellar_main_theme)
                    edit.apply()
                } else {
                    MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                            .setTitle(getString(R.string.in_app_popup_tone_available_title))
                            .setMessage(getString(R.string.in_app_popup_tone_available_message))
                            .setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
                                goToPremiumActivity()
                            }
                            .setNegativeButton(R.string.alert_dialog_later) { _, _ ->
                                refreshChecked()
                            }
                            .show()
                }
            }
        }

        settings_NotifSoundRelaxToneCheckbox!!.setOnClickListener {
            if (settings_NotificationMessagesAlarmSound != null) {
                settings_NotificationMessagesAlarmSound!!.stop()
            }

            if (settings_NotifSoundRelaxToneCheckbox!!.isChecked) {

                settings_NotificationMessagesAlarmSound = MediaPlayer.create(this, R.raw.relax_sms)
                settings_NotificationMessagesAlarmSound!!.start()

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
//                settings_NotifSoundRelaxToneCheckbox!!.isChecked = false

                if (notifRelaxationSoundIsBought) {

                    settings_NotificationMessagesAlarmSound = MediaPlayer.create(this, R.raw.relax_sms)
                    settings_NotificationMessagesAlarmSound!!.start()

                    val edit: SharedPreferences.Editor = sharedAlarmNotifTonePreferences.edit()
                    edit.putInt("Alarm_Notif_Tone", R.raw.relax_sms)
                    edit.apply()
                } else {
                    MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                            .setTitle(getString(R.string.in_app_popup_tone_available_title))
                            .setMessage(getString(R.string.in_app_popup_tone_available_message))
                            .setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
                                goToPremiumActivity()
                            }
                            .setNegativeButton(R.string.alert_dialog_later) { _, _ ->
                                refreshChecked()
                            }
                            .show()
                }
            }
        }

        //endregion

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

    fun refreshChecked() {

        if (settings_NotificationMessagesAlarmSound != null) {
            settings_NotificationMessagesAlarmSound!!.stop()
        }

        when (numberDefault) {
            1 -> {
                settings_NotifNoSoundCheckbox!!.isChecked = true
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
            R.raw.sms_ring -> {
                settings_NotifNoSoundCheckbox!!.isChecked = false
                settings_NotifSoundKnockinCheckbox!!.isChecked = true
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
            R.raw.xylophone_tone -> {
                settings_NotifNoSoundCheckbox!!.isChecked = false
                settings_NotifSoundKnockinCheckbox!!.isChecked = false
                settings_NotifSoundXyloCheckbox!!.isChecked = true

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
            R.raw.moanin_jazz -> {
                settings_NotifNoSoundCheckbox!!.isChecked = false
                settings_NotifSoundKnockinCheckbox!!.isChecked = false
                settings_NotifSoundXyloCheckbox!!.isChecked = false

                settings_NotifSoundMoaninCheckbox!!.isChecked = true
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
            R.raw.blue_bossa -> {
                settings_NotifNoSoundCheckbox!!.isChecked = false
                settings_NotifSoundKnockinCheckbox!!.isChecked = false
                settings_NotifSoundXyloCheckbox!!.isChecked = false

                settings_NotifSoundMoaninCheckbox!!.isChecked = false
                settings_NotifSoundBlueBossaCheckbox!!.isChecked = true
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
            R.raw.caravan -> {
                settings_NotifNoSoundCheckbox!!.isChecked = false
                settings_NotifSoundKnockinCheckbox!!.isChecked = false
                settings_NotifSoundXyloCheckbox!!.isChecked = false

                settings_NotifSoundMoaninCheckbox!!.isChecked = false
                settings_NotifSoundBlueBossaCheckbox!!.isChecked = false
                settings_NotifSoundCaravanCheckbox!!.isChecked = true
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
            R.raw.dolphin_dance -> {
                settings_NotifNoSoundCheckbox!!.isChecked = false
                settings_NotifSoundKnockinCheckbox!!.isChecked = false
                settings_NotifSoundXyloCheckbox!!.isChecked = false

                settings_NotifSoundMoaninCheckbox!!.isChecked = false
                settings_NotifSoundBlueBossaCheckbox!!.isChecked = false
                settings_NotifSoundCaravanCheckbox!!.isChecked = false
                settings_NotifSoundDolphinDanceCheckbox!!.isChecked = true
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
            R.raw.autumn_leaves -> {
                settings_NotifNoSoundCheckbox!!.isChecked = false
                settings_NotifSoundKnockinCheckbox!!.isChecked = false
                settings_NotifSoundXyloCheckbox!!.isChecked = false

                settings_NotifSoundMoaninCheckbox!!.isChecked = false
                settings_NotifSoundBlueBossaCheckbox!!.isChecked = false
                settings_NotifSoundCaravanCheckbox!!.isChecked = false
                settings_NotifSoundDolphinDanceCheckbox!!.isChecked = false
                settings_NotifSoundAutumnLeavesCheckbox!!.isChecked = true
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
            R.raw.freddie_freeloader -> {
                settings_NotifNoSoundCheckbox!!.isChecked = false
                settings_NotifSoundKnockinCheckbox!!.isChecked = false
                settings_NotifSoundXyloCheckbox!!.isChecked = false

                settings_NotifSoundMoaninCheckbox!!.isChecked = false
                settings_NotifSoundBlueBossaCheckbox!!.isChecked = false
                settings_NotifSoundCaravanCheckbox!!.isChecked = false
                settings_NotifSoundDolphinDanceCheckbox!!.isChecked = false
                settings_NotifSoundAutumnLeavesCheckbox!!.isChecked = false
                settings_NotifSoundFreddieFreeloaderCheckbox!!.isChecked = true

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
            R.raw.bass_slap -> {

                settings_NotifNoSoundCheckbox!!.isChecked = false
                settings_NotifSoundKnockinCheckbox!!.isChecked = false
                settings_NotifSoundXyloCheckbox!!.isChecked = false

                settings_NotifSoundMoaninCheckbox!!.isChecked = false
                settings_NotifSoundBlueBossaCheckbox!!.isChecked = false
                settings_NotifSoundCaravanCheckbox!!.isChecked = false
                settings_NotifSoundDolphinDanceCheckbox!!.isChecked = false
                settings_NotifSoundAutumnLeavesCheckbox!!.isChecked = false
                settings_NotifSoundFreddieFreeloaderCheckbox!!.isChecked = false

                settings_NotifSoundSlapCheckbox!!.isChecked = true
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
            R.raw.funk_yall -> {

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
                settings_NotifSoundFunkYallCheckbox!!.isChecked = true
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
            R.raw.off_the_curve_groove -> {

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
                settings_NotifSoundOffTheCurveCheckbox!!.isChecked = true
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
            R.raw.keyboard_funky_tone -> {

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
                settings_NotifSoundKeyboardFunkyToneCheckbox!!.isChecked = true
                settings_NotifSoundUCantHoldNoGrooveCheckbox!!.isChecked = false
                settings_NotifSoundColdSweatCheckbox!!.isChecked = false

                settings_NotifSoundAcousticGuitarCheckbox!!.isChecked = false
                settings_NotifSoundGravityCheckbox!!.isChecked = false
                settings_NotifSoundSlowDancingCheckbox!!.isChecked = false
                settings_NotifSoundScorpionThemeCheckbox!!.isChecked = false
                settings_NotifSoundFirstStepCheckbox!!.isChecked = false
                settings_NotifSoundRelaxToneCheckbox!!.isChecked = false
            }
            R.raw.u_cant_hold_no_groove -> {

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
                settings_NotifSoundUCantHoldNoGrooveCheckbox!!.isChecked = true
                settings_NotifSoundColdSweatCheckbox!!.isChecked = false

                settings_NotifSoundAcousticGuitarCheckbox!!.isChecked = false
                settings_NotifSoundGravityCheckbox!!.isChecked = false
                settings_NotifSoundSlowDancingCheckbox!!.isChecked = false
                settings_NotifSoundScorpionThemeCheckbox!!.isChecked = false
                settings_NotifSoundFirstStepCheckbox!!.isChecked = false
                settings_NotifSoundRelaxToneCheckbox!!.isChecked = false
            }
            R.raw.cold_sweat -> {

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
                settings_NotifSoundColdSweatCheckbox!!.isChecked = true

                settings_NotifSoundAcousticGuitarCheckbox!!.isChecked = false
                settings_NotifSoundGravityCheckbox!!.isChecked = false
                settings_NotifSoundSlowDancingCheckbox!!.isChecked = false
                settings_NotifSoundScorpionThemeCheckbox!!.isChecked = false
                settings_NotifSoundFirstStepCheckbox!!.isChecked = false
                settings_NotifSoundRelaxToneCheckbox!!.isChecked = false
            }
            R.raw.beautiful_chords_progression -> {

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

                settings_NotifSoundAcousticGuitarCheckbox!!.isChecked = true
                settings_NotifSoundGravityCheckbox!!.isChecked = false
                settings_NotifSoundSlowDancingCheckbox!!.isChecked = false
                settings_NotifSoundScorpionThemeCheckbox!!.isChecked = false
                settings_NotifSoundFirstStepCheckbox!!.isChecked = false
                settings_NotifSoundRelaxToneCheckbox!!.isChecked = false
            }
            R.raw.gravity -> {

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
                settings_NotifSoundGravityCheckbox!!.isChecked = true
                settings_NotifSoundSlowDancingCheckbox!!.isChecked = false
                settings_NotifSoundScorpionThemeCheckbox!!.isChecked = false
                settings_NotifSoundFirstStepCheckbox!!.isChecked = false
                settings_NotifSoundRelaxToneCheckbox!!.isChecked = false
            }
            R.raw.fade_to_black -> {

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
                settings_NotifSoundScorpionThemeCheckbox!!.isChecked = true
                settings_NotifSoundFirstStepCheckbox!!.isChecked = false
                settings_NotifSoundRelaxToneCheckbox!!.isChecked = false
            }
            R.raw.slow_dancing -> {

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
                settings_NotifSoundSlowDancingCheckbox!!.isChecked = true
                settings_NotifSoundScorpionThemeCheckbox!!.isChecked = false
                settings_NotifSoundFirstStepCheckbox!!.isChecked = false
                settings_NotifSoundRelaxToneCheckbox!!.isChecked = false
            }
            R.raw.relax_sms -> {

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
                settings_NotifSoundOffTheCurveCheckbox!!.isChecked = true
                settings_NotifSoundFunkYallCheckbox!!.isChecked = false
                settings_NotifSoundKeyboardFunkyToneCheckbox!!.isChecked = false
                settings_NotifSoundUCantHoldNoGrooveCheckbox!!.isChecked = false
                settings_NotifSoundColdSweatCheckbox!!.isChecked = false

                settings_NotifSoundAcousticGuitarCheckbox!!.isChecked = false
                settings_NotifSoundGravityCheckbox!!.isChecked = false
                settings_NotifSoundSlowDancingCheckbox!!.isChecked = false
                settings_NotifSoundScorpionThemeCheckbox!!.isChecked = false
                settings_NotifSoundFirstStepCheckbox!!.isChecked = false
                settings_NotifSoundRelaxToneCheckbox!!.isChecked = true
            }
            R.raw.interstellar_main_theme -> {

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
                settings_NotifSoundFirstStepCheckbox!!.isChecked = true
                settings_NotifSoundRelaxToneCheckbox!!.isChecked = false
            }
        }
    }

    fun ringToneLayoutClosed() {
        //region OpenClose

        settings_NotifSoundKnockinLayout!!.visibility = View.GONE
        settings_NotifSoundXyloLayout!!.visibility = View.GONE
        settings_NotifNoSoundLayout!!.visibility = View.GONE

        settings_ChooseNotifDefaultSoundImageOpen!!.visibility = View.GONE
        settings_ChooseNotifDefaultSoundImageClose!!.visibility = View.VISIBLE

        settings_NotifSoundMoaninLayout!!.visibility = View.GONE
        settings_NotifSoundBlueBossaLayout!!.visibility = View.GONE
        settings_NotifSoundCaravanLayout!!.visibility = View.GONE
        settings_NotifSoundDolphinDanceLayout!!.visibility = View.GONE
        settings_NotifSoundAutumnLeavesLayout!!.visibility = View.GONE
        settings_NotifSoundFreddieFreeloaderLayout!!.visibility = View.GONE

        settings_ChooseNotifJazzySoundImageOpen!!.visibility = View.GONE
        settings_ChooseNotifJazzySoundImageClose!!.visibility = View.VISIBLE

        settings_NotifSoundSlapLayout!!.visibility = View.GONE
        settings_NotifSoundOffTheCurveLayout!!.visibility = View.GONE
        settings_NotifSoundFunkYallLayout!!.visibility = View.GONE
        settings_NotifSoundKeyboardFunkyToneLayout!!.visibility = View.GONE
        settings_NotifSoundUCantHoldNoGrooveLayout!!.visibility = View.GONE
        settings_NotifSoundColdSweatLayout!!.visibility = View.GONE

        settings_ChooseNotifFunkySoundImageOpen!!.visibility = View.GONE
        settings_ChooseNotifFunkySoundImageClose!!.visibility = View.VISIBLE

        settings_NotifSoundAcousticGuitarLayout!!.visibility = View.GONE
        settings_NotifSoundGravityLayout!!.visibility = View.GONE
        settings_NotifSoundSlowDancingLayout!!.visibility = View.GONE
        settings_NotifSoundScorpionThemeLayout!!.visibility = View.GONE
        settings_NotifSoundFirstStepLayout!!.visibility = View.GONE
        settings_NotifSoundRelaxToneLayout!!.visibility = View.GONE

        settings_ChooseNotifRelaxationSoundImageOpen!!.visibility = View.GONE
        settings_ChooseNotifRelaxationSoundImageClose!!.visibility = View.VISIBLE

        //endregion
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