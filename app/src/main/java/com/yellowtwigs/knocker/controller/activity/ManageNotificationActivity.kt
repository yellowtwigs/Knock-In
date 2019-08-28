package com.yellowtwigs.knocker.controller.activity

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.widget.Toolbar
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.yellowtwigs.knocker.R
import com.yellowtwigs.knocker.controller.NotificationSender
import com.yellowtwigs.knocker.controller.activity.firstLaunch.MultiSelectActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_manage_notification.*
import java.util.*

/**
 * La Classe qui permet d'activer ou desactiver les notifications de knocker
 * @author Florian Striebel
 */
class ManageNotificationActivity : AppCompatActivity() {

    //region ========================================== Var or Val ==========================================

    private var drawerLayout: DrawerLayout? = null
    private var activityVisible: Boolean = false
    private var switchPopupNotif: Switch? = null
    private var switchservice: Switch? = null
    private var switchMaskNotif: Switch? = null
    private var isTrue = false
    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //region ======================================== Theme Dark ========================================

        val sharedThemePreferences = getSharedPreferences("Knocker_Theme", Context.MODE_PRIVATE)
        if (sharedThemePreferences.getBoolean("darkTheme", false)) {
            setTheme(R.style.AppThemeDark)
        } else {
            setTheme(R.style.AppTheme)
        }

        //endregion

        setContentView(R.layout.activity_manage_notification)

        val sharedPreferences: SharedPreferences = getSharedPreferences("Knocker_preferences", Context.MODE_PRIVATE)

        switchPopupNotif = this.findViewById<Switch>(R.id.switch_stop_popup)
        switchservice = this.findViewById<Switch>(R.id.switch_stop_service)
        switchMaskNotif = this.findViewById<Switch>(R.id.switch_manage_notif_prio_1)
        val switchReminder = this.findViewById<Switch>(R.id.switch_manage_notif_reminder)
        val remindHour = this.findViewById<TextView>(R.id.textView_heure)
        val viewHour = this.findViewById<ConstraintLayout>(R.id.modify_hour_Constariant)
        if(this.isNotificationServiceEnabled) {
            switchPopupNotif!!.isChecked = sharedPreferences.getBoolean("popupNotif", false)
            switchservice!!.isChecked = sharedPreferences.getBoolean("serviceNotif", false)
            switchMaskNotif!!.isChecked = sharedPreferences.getBoolean("mask_prio_1", false)
            switchReminder.isChecked = sharedPreferences.getBoolean("reminder", false)
        }else{

            switchPopupNotif!!.isChecked =false
            switchservice!!.isChecked = false
            switchMaskNotif!!.isChecked = false
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
                    startActivity(Intent(this@ManageNotificationActivity, MainActivity::class.java))
                }
                R.id.nav_informations -> startActivity(Intent(this@ManageNotificationActivity, EditInformationsActivity::class.java))
                R.id.nav_manage_screen -> startActivity(Intent(this@ManageNotificationActivity, ManageMyScreenActivity::class.java))
                R.id.nav_data_access -> startActivity(Intent(this@ManageNotificationActivity, ManageMyScreenActivity::class.java))
                R.id.nav_settings ->startActivity(Intent(this@ManageNotificationActivity, SettingsActivity::class.java))
                R.id.nav_knockons -> startActivity(Intent(this@ManageNotificationActivity, ManageKnockonsActivity::class.java))
                R.id.nav_statistics -> {
                }
                R.id.nav_help -> startActivity(Intent(this@ManageNotificationActivity, HelpActivity::class.java))
            }

            val drawer = findViewById<DrawerLayout>(R.id.drawer_layout_manage_notif)
            drawer.closeDrawer(GravityCompat.START)
            true
        }

        //endregion

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
        switchMaskNotif!!.setOnCheckedChangeListener { _, _ ->
            val edit: SharedPreferences.Editor = sharedPreferences.edit()
            if (switchMaskNotif!!.isChecked) {
                /*if (!isNotificationServiceEnabled) {
                   // buildNotificationServiceAlertDialog().show()
                }else{*/
                switchservice!!.isChecked = true
                edit.putBoolean("serviceNotif", true)
                edit.putBoolean("mask_prio_1", true)
                edit.apply()
                //}
            } else {
                edit.putBoolean("mask_prio_1", false)
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
                switchMaskNotif!!.isChecked = false
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
                    val sharedPreferences: SharedPreferences = getSharedPreferences("Knocker_preferences", Context.MODE_PRIVATE)
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
        intentFilter.addAction("com.example.knocker.notificationExemple")
        alertDialog.cancel()
        val thread = Thread {
            activityVisible = false
            while (!isNotificationServiceEnabled && !activityVisible) {
            }
            val sharedPreferences: SharedPreferences = getSharedPreferences("Knocker_preferences", Context.MODE_PRIVATE)
            val edit: SharedPreferences.Editor = sharedPreferences.edit()
            if (isNotificationServiceEnabled) {
                edit.putBoolean("serviceNotif", true)
                edit.putBoolean("mask_prio_1", switchMaskNotif!!.isChecked)
                edit.apply()

                isTrue = true
            } else {
                val runnable = Runnable {
                    switchMaskNotif!!.isChecked = false
                    switchPopupNotif!!.isChecked = false
                    switchservice!!.isChecked = false
                }
                runOnUiThread(runnable)
            }
        }
        thread.start()
    }

    private fun negativeAlertDialogButtonClick(alertDialog: androidx.appcompat.app.AlertDialog) {
        switchMaskNotif!!.isChecked = false
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
        intent.action = "NOTIFICAION_TIME"
        val pendingIntent = PendingIntent.getBroadcast(applicationContext, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmManager = getSystemService(ALARM_SERVICE) as (AlarmManager)
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent)

        //cancel previous app

    }

    override fun onResume() {
        super.onResume()
        activityVisible = true

        if (isTrue && manage_notif_prio_1_layout.visibility != View.GONE) {
            buildMultiSelectAlertDialog()
            isTrue = false
        }
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