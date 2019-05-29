package com.example.knocker.controller.activity

import android.annotation.SuppressLint
import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.widget.Toolbar
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.knocker.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView

/**
 * La Classe qui permet d'activer ou desactiver les notifications de knocker
 * @author Florian Striebel
 */
class ManageNotificationActivity : AppCompatActivity() {

    //region ========================================== Var or Val ==========================================

    // Show on the Main Layout
    private var drawerLayout: DrawerLayout? = null

    private var my_knocker: RelativeLayout? = null

    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_notification)

        val sharedPreferences: SharedPreferences = getSharedPreferences("Knocker_preferences", Context.MODE_PRIVATE)

        val switchPopupNotif = this.findViewById<Switch>(R.id.switch_stop_popup)
        val switchservice = this.findViewById<Switch>(R.id.switch_stop_service)

        switchPopupNotif.isChecked = sharedPreferences.getBoolean("popupNotif", false)
        switchservice.isChecked = sharedPreferences.getBoolean("serviceNotif", true)

        //region ========================================== Toolbar =========================================

        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar.setHomeAsUpIndicator(R.drawable.ic_open_drawer)
        actionbar.title = "Manage Notification"

        //endregion

        //region ======================================= DrawerLayout =======================================

        // Drawerlayout
        drawerLayout = findViewById(R.id.drawer_layout)

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        val headerView = navigationView.getHeaderView(0);
        my_knocker = headerView.findViewById(R.id.my_knocker)

        my_knocker!!.setOnClickListener {
        }
        navigationView.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            drawerLayout!!.closeDrawers()

            val id = menuItem.itemId

            when (id) {
                R.id.nav_informations -> startActivity(Intent(this@ManageNotificationActivity, EditInformationsActivity::class.java))
                R.id.nav_screen_config -> startActivity(Intent(this@ManageNotificationActivity, ManageMyScreenActivity::class.java))
                R.id.nav_data_access -> {
                }
                R.id.nav_knockons -> startActivity(Intent(this@ManageNotificationActivity, ManageKnockonsActivity::class.java))
                R.id.nav_statistics -> {
                }
                R.id.nav_help -> startActivity(Intent(this@ManageNotificationActivity, HelpActivity::class.java))
            }

            val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
            drawer.closeDrawer(GravityCompat.START)
            true
        }

        //endregion

        switchPopupNotif.setOnCheckedChangeListener { _, _ ->
            val edit: SharedPreferences.Editor = sharedPreferences.edit()
            if (switchPopupNotif.isChecked) {
                switchservice.setChecked(false)
                edit.remove("popupNotif")
                edit.remove("serviceNotif")
                edit.putBoolean("serviceNotif", false)
                edit.putBoolean("popupNotif", true)
                edit.apply()
                System.out.println("pop up true " + sharedPreferences.getBoolean("popupNotif", false))
            } else {
                edit.remove("popupNotif")
                edit.putBoolean("popupNotif", false)
                edit.apply()
                System.out.println("pop up false" + sharedPreferences.getBoolean("popupNotif", false))
            }
        }

        switchservice.setOnCheckedChangeListener { _, _ ->
            val edit: SharedPreferences.Editor = sharedPreferences.edit()
            if (switchservice.isChecked) {
                switchPopupNotif.setChecked(false)
                edit.remove("popupNotif")
                edit.remove("serviceNotif")
                edit.putBoolean("serviceNotif", true)
                edit.putBoolean("popupNotif", false)
                edit.apply()
                System.out.println("service economy true " + sharedPreferences.getBoolean("serviceNotif", true))
            } else {
                if (!isNotificationServiceEnabled) {
                    buildNotificationServiceAlertDialog().show()
                }
                edit.remove("serviceNotif")
                edit.putBoolean("serviceNotif", false)
                edit.commit()
                System.out.println("service economy false " + sharedPreferences.getBoolean("serviceNotif", true))
            }
        }
    }

    //region ========================================== Functions =========================================

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                drawerLayout!!.openDrawer(GravityCompat.START)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
    }

    @SuppressLint("InflateParams")
    private fun buildNotificationServiceAlertDialog(): androidx.appcompat.app.AlertDialog {
        val inflater: LayoutInflater = this.layoutInflater
        val alertView: View = inflater.inflate(R.layout.alert_dialog_notification, null)

        val manage_notif_ButtonAlertDialogAllow = alertView.findViewById<Button>(R.id.button_alert_dialog_allow_it)
        manage_notif_ButtonAlertDialogAllow.setOnClickListener { positiveFloatingDeleteButtonClick() }

        val manage_notif_ButtonAlertDialogDismiss = alertView.findViewById<Button>(R.id.tv_alert_dialog_dismiss)
        manage_notif_ButtonAlertDialogDismiss.setOnClickListener { negativeFloatingDeleteButtonClick() }

        return MaterialAlertDialogBuilder(this)
                .setView(alertView)
                .show()
    }

    private fun positiveFloatingDeleteButtonClick() {
        startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
        val intentFilter = IntentFilter()
        intentFilter.addAction("com.example.firsttestknocker.notificationExemple")
    }

    private fun negativeFloatingDeleteButtonClick() {

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

    //endregion
}