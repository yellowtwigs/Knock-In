package com.yellowtwigs.knockin.controller.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.yellowtwigs.knockin.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {

    //region ========================================== Val or Var ==========================================

    private var drawerLayout: DrawerLayout? = null

    private var callPopup: Boolean = true
//    private var searchbarPos: Boolean = false

    private var settings_CallPopupSwitch: Switch? = null
//    private var settings_SearchBarPosSwitch: Switch? = null

    private var settings_PermissionsPhoneLayout: RelativeLayout? = null
    private var settings_PermissionsPhoneMaterialButton: MaterialButton? = null
    private var settings_PermissionsPhoneLoading: ProgressBar? = null
    private var settings_PermissionsPhoneChecked: AppCompatImageView? = null

    private var settings_NotifSoundSlapLayout: RelativeLayout? = null
    private var settings_NotifSoundXyloLayout: RelativeLayout? = null
    private var settings_NotifSoundKeyboardLayout: RelativeLayout? = null
    private var settings_NotifSoundGuitarLayout: RelativeLayout? = null
    private var settings_NotifSoundDrumLayout: RelativeLayout? = null
    private var settings_NotifSoundSaxLayout: RelativeLayout? = null

    private var settings_NotificationMessagesAlarmSound: MediaPlayer? = null

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

        setContentView(R.layout.activity_settings)

        PremiumButton.setOnClickListener {
            val intent = Intent(this,PremiumActivity::class.java)
            startActivity(intent)
        }

        val sharedPreferencePopup = getSharedPreferences("Phone_call", Context.MODE_PRIVATE)
        callPopup = sharedPreferencePopup.getBoolean("popup", true)

        val sharedAlarmNotifTonePreferences: SharedPreferences = getSharedPreferences("Alarm_Notif_Tone", Context.MODE_PRIVATE)

//        searchbarPos = sharedPreferencePopup.getBoolean("searchbarPos", false)

        //region ======================================= FindViewById =======================================

        settings_CallPopupSwitch = findViewById(R.id.settings_call_popup_switch)
//        settings_SearchBarPosSwitch = findViewById(R.id.settings_searchbar_pos_switch)

        settings_PermissionsPhoneLayout = findViewById(R.id.settings_permissions_phone_layout)

        settings_PermissionsPhoneMaterialButton = findViewById(R.id.settings_permissions_phone_button)
        settings_PermissionsPhoneLoading = findViewById(R.id.settings_permissions_phone_loading)
        settings_PermissionsPhoneChecked = findViewById(R.id.settings_permissions_phone_check)
        val phoneCallAlertTv = findViewById<TextView>(R.id.settings_permissions_phone_subtitle)

        phoneCallAlertTv.text = String.format(getString(R.string.start_activity_permission_subtitle), getString(R.string.app_name))

        settings_NotifSoundSlapLayout = findViewById(R.id.settings_notif_sound_slap_layout)
        settings_NotifSoundXyloLayout = findViewById(R.id.settings_notif_sound_xylo_layout)
        settings_NotifSoundKeyboardLayout = findViewById(R.id.settings_notif_sound_keyboard_layout)
        settings_NotifSoundGuitarLayout = findViewById(R.id.settings_notif_sound_guitar_layout)
        settings_NotifSoundDrumLayout = findViewById(R.id.settings_notif_sound_drum_layout)
        settings_NotifSoundSaxLayout = findViewById(R.id.settings_notif_sound_sax_layout)

        //endregion

        //region ========================================== Toolbar =========================================

        val toolbar = findViewById<Toolbar>(R.id.settings_toolbar)
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar.setHomeAsUpIndicator(R.drawable.ic_open_drawer)
        actionbar.title = getString(R.string.left_drawer_settings)

        //endregion

        //region ======================================= DrawerLayout =======================================

        drawerLayout = findViewById(R.id.settings_drawer_layout)

        val navigationView = findViewById<NavigationView>(R.id.settings_nav_view)
        val menu = navigationView.menu
        val navItem = menu.findItem(R.id.nav_settings)
        navItem.isChecked = true

        navigationView.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            drawerLayout!!.closeDrawers()

            when (menuItem.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this@SettingsActivity, MainActivity::class.java))
                }
                R.id.nav_informations -> startActivity(Intent(this@SettingsActivity, EditInformationsActivity::class.java))
                R.id.nav_notif_config -> startActivity(Intent(this@SettingsActivity, ManageNotificationActivity::class.java))
                R.id.nav_manage_screen -> startActivity(Intent(this@SettingsActivity, ManageMyScreenActivity::class.java))
                R.id.nav_data_access -> {
                }
                R.id.nav_knockons -> startActivity(Intent(this@SettingsActivity, ManageKnockonsActivity::class.java))
                R.id.nav_statistics -> {
                }
                R.id.nav_help -> startActivity(Intent(this@SettingsActivity, HelpActivity::class.java))
            }

            true
        }

        //endregion

        if (checkPermission(Manifest.permission.SEND_SMS) && checkPermission(Manifest.permission.CALL_PHONE)) {
            settings_PermissionsPhoneLayout!!.visibility = View.GONE
        } else {
            settings_PermissionsPhoneLayout!!.visibility = View.VISIBLE
        }

        if (callPopup) {
            settings_CallPopupSwitch!!.isChecked = true
        }

        when (sharedAlarmNotifTonePreferences.getInt("Alarm_Notif_Tone", 1)) {
            R.raw.bass_slap -> {
                settings_NotifSoundSlapLayout!!.background = getDrawable(R.drawable.border_selected_image_view)
            }
            R.raw.xylophone_tone -> {
                settings_NotifSoundXyloLayout!!.background = getDrawable(R.drawable.border_selected_image_view)
            }
            R.raw.piano_sms -> {
                settings_NotifSoundKeyboardLayout!!.background = getDrawable(R.drawable.border_selected_image_view)
            }
            R.raw.electric_blues -> {
                settings_NotifSoundGuitarLayout!!.background = getDrawable(R.drawable.border_selected_image_view)
            }
            R.raw.caravan -> {
                settings_NotifSoundDrumLayout!!.background = getDrawable(R.drawable.border_selected_image_view)
            }
            R.raw.sax_sms -> {
                settings_NotifSoundSaxLayout!!.background = getDrawable(R.drawable.border_selected_image_view)
            }
        }

        settings_CallPopupSwitch!!.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val sharedThemePreferences: SharedPreferences = getSharedPreferences("Phone_call", Context.MODE_PRIVATE)
                val edit: SharedPreferences.Editor = sharedThemePreferences.edit()
                edit.putBoolean("popup", true)
                edit.apply()
            } else {
                val sharedThemePreferences: SharedPreferences = getSharedPreferences("Phone_call", Context.MODE_PRIVATE)
                val edit: SharedPreferences.Editor = sharedThemePreferences.edit()
                edit.putBoolean("popup", false)
                edit.apply()
            }
        }

        settings_PermissionsPhoneMaterialButton!!.setOnClickListener {
            val arraylistPermission = ArrayList<String>()
//            arraylistPermission.add(Manifest.permission.SEND_SMS)
            arraylistPermission.add(Manifest.permission.CALL_PHONE)
            ActivityCompat.requestPermissions(this, arraylistPermission.toArray(arrayOfNulls<String>(arraylistPermission.size)), REQUEST_CODE_SMS_AND_CALL)
            settings_PermissionsPhoneMaterialButton!!.visibility = View.INVISIBLE
            settings_PermissionsPhoneLoading!!.visibility = View.VISIBLE
        }

        //region ======================================== Ring Tones ========================================

        settings_NotifSoundSlapLayout!!.setOnClickListener {
            if(settings_NotificationMessagesAlarmSound != null){
                settings_NotificationMessagesAlarmSound!!.stop()
            }
            settings_NotificationMessagesAlarmSound = MediaPlayer.create(this, R.raw.bass_slap)
            settings_NotificationMessagesAlarmSound!!.start()

            settings_NotifSoundSlapLayout!!.background = getDrawable(R.drawable.border_selected_image_view)
            settings_NotifSoundXyloLayout!!.background = getDrawable(R.drawable.background_layout_selector)
            settings_NotifSoundKeyboardLayout!!.background = getDrawable(R.drawable.background_layout_selector)
            settings_NotifSoundGuitarLayout!!.background = getDrawable(R.drawable.background_layout_selector)
            settings_NotifSoundDrumLayout!!.background = getDrawable(R.drawable.background_layout_selector)
            settings_NotifSoundSaxLayout!!.background = getDrawable(R.drawable.background_layout_selector)

            val edit: SharedPreferences.Editor = sharedAlarmNotifTonePreferences.edit()
            edit.putInt("Alarm_Notif_Tone", R.raw.bass_slap)
            edit.apply()
        }

        settings_NotifSoundXyloLayout!!.setOnClickListener {
            if(settings_NotificationMessagesAlarmSound != null){
                settings_NotificationMessagesAlarmSound!!.stop()
            }
            settings_NotificationMessagesAlarmSound = MediaPlayer.create(this, R.raw.xylophone_tone)
            settings_NotificationMessagesAlarmSound!!.start()

            settings_NotifSoundSlapLayout!!.background = getDrawable(R.drawable.background_layout_selector)
            settings_NotifSoundXyloLayout!!.background = getDrawable(R.drawable.border_selected_image_view)
            settings_NotifSoundKeyboardLayout!!.background = getDrawable(R.drawable.background_layout_selector)
            settings_NotifSoundGuitarLayout!!.background = getDrawable(R.drawable.background_layout_selector)
            settings_NotifSoundDrumLayout!!.background = getDrawable(R.drawable.background_layout_selector)
            settings_NotifSoundSaxLayout!!.background = getDrawable(R.drawable.background_layout_selector)

            val edit: SharedPreferences.Editor = sharedAlarmNotifTonePreferences.edit()
            edit.putInt("Alarm_Notif_Tone", R.raw.xylophone_tone)
            edit.apply()
        }

        settings_NotifSoundKeyboardLayout!!.setOnClickListener {
            if(settings_NotificationMessagesAlarmSound != null){
                settings_NotificationMessagesAlarmSound!!.stop()
            }
            settings_NotificationMessagesAlarmSound = MediaPlayer.create(this, R.raw.piano_sms)
            settings_NotificationMessagesAlarmSound!!.start()

            settings_NotifSoundSlapLayout!!.background = getDrawable(R.drawable.background_layout_selector)
            settings_NotifSoundXyloLayout!!.background = getDrawable(R.drawable.background_layout_selector)
            settings_NotifSoundKeyboardLayout!!.background = getDrawable(R.drawable.border_selected_image_view)
            settings_NotifSoundGuitarLayout!!.background = getDrawable(R.drawable.background_layout_selector)
            settings_NotifSoundDrumLayout!!.background = getDrawable(R.drawable.background_layout_selector)
            settings_NotifSoundSaxLayout!!.background = getDrawable(R.drawable.background_layout_selector)

            val edit: SharedPreferences.Editor = sharedAlarmNotifTonePreferences.edit()
            edit.putInt("Alarm_Notif_Tone", R.raw.piano_sms)
            edit.apply()
        }

        settings_NotifSoundGuitarLayout!!.setOnClickListener {
            if(settings_NotificationMessagesAlarmSound != null){
                settings_NotificationMessagesAlarmSound!!.stop()
            }
            settings_NotificationMessagesAlarmSound = MediaPlayer.create(this, R.raw.electric_blues)
            settings_NotificationMessagesAlarmSound!!.start()

            settings_NotifSoundSlapLayout!!.background = getDrawable(R.drawable.background_layout_selector)
            settings_NotifSoundXyloLayout!!.background = getDrawable(R.drawable.background_layout_selector)
            settings_NotifSoundKeyboardLayout!!.background = getDrawable(R.drawable.background_layout_selector)
            settings_NotifSoundGuitarLayout!!.background = getDrawable(R.drawable.border_selected_image_view)
            settings_NotifSoundDrumLayout!!.background = getDrawable(R.drawable.background_layout_selector)
            settings_NotifSoundSaxLayout!!.background = getDrawable(R.drawable.background_layout_selector)

            val edit: SharedPreferences.Editor = sharedAlarmNotifTonePreferences.edit()
            edit.putInt("Alarm_Notif_Tone", R.raw.electric_blues)
            edit.apply()
        }

        settings_NotifSoundDrumLayout!!.setOnClickListener {
            if(settings_NotificationMessagesAlarmSound != null){
                settings_NotificationMessagesAlarmSound!!.stop()
            }
            settings_NotificationMessagesAlarmSound = MediaPlayer.create(this, R.raw.caravan)
            settings_NotificationMessagesAlarmSound!!.start()

            settings_NotifSoundSlapLayout!!.background = getDrawable(R.drawable.background_layout_selector)
            settings_NotifSoundXyloLayout!!.background = getDrawable(R.drawable.background_layout_selector)
            settings_NotifSoundKeyboardLayout!!.background = getDrawable(R.drawable.background_layout_selector)
            settings_NotifSoundGuitarLayout!!.background = getDrawable(R.drawable.background_layout_selector)
            settings_NotifSoundDrumLayout!!.background = getDrawable(R.drawable.border_selected_image_view)
            settings_NotifSoundSaxLayout!!.background = getDrawable(R.drawable.background_layout_selector)

            val edit: SharedPreferences.Editor = sharedAlarmNotifTonePreferences.edit()
            edit.putInt("Alarm_Notif_Tone", R.raw.caravan)
            edit.apply()
        }

        settings_NotifSoundSaxLayout!!.setOnClickListener {
            if(settings_NotificationMessagesAlarmSound != null){
                settings_NotificationMessagesAlarmSound!!.stop()
            }
            settings_NotificationMessagesAlarmSound = MediaPlayer.create(this, R.raw.sax_sms)
            settings_NotificationMessagesAlarmSound!!.start()

            settings_NotifSoundSlapLayout!!.background = getDrawable(R.drawable.background_layout_selector)
            settings_NotifSoundXyloLayout!!.background = getDrawable(R.drawable.background_layout_selector)
            settings_NotifSoundKeyboardLayout!!.background = getDrawable(R.drawable.background_layout_selector)
            settings_NotifSoundGuitarLayout!!.background = getDrawable(R.drawable.background_layout_selector)
            settings_NotifSoundDrumLayout!!.background = getDrawable(R.drawable.background_layout_selector)
            settings_NotifSoundSaxLayout!!.background = getDrawable(R.drawable.border_selected_image_view)

            val edit: SharedPreferences.Editor = sharedAlarmNotifTonePreferences.edit()
            edit.putInt("Alarm_Notif_Tone", R.raw.sax_sms)
            edit.apply()
        }

        //endregion
    }

    @SuppressLint("ShowToast")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                drawerLayout!!.openDrawer(GravityCompat.START)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun checkPermission(permission: String): Boolean {
        val checkPermission = ContextCompat.checkSelfPermission(this, permission)
        return checkPermission == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (REQUEST_CODE_SMS_AND_CALL == requestCode && checkPermission(Manifest.permission.SEND_SMS) && checkPermission(Manifest.permission.CALL_PHONE)) {
            settings_PermissionsPhoneLoading!!.visibility = View.INVISIBLE
            settings_PermissionsPhoneChecked!!.visibility = View.VISIBLE
        } else {
            settings_PermissionsPhoneLoading!!.visibility = View.INVISIBLE
            settings_PermissionsPhoneMaterialButton!!.visibility = View.VISIBLE
        }
    }

    override fun onBackPressed() {
    }

    companion object {
        const val REQUEST_CODE_SMS_AND_CALL = 5
    }
}
