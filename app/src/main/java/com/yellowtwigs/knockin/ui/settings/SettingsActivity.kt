package com.yellowtwigs.knockin.ui.settings

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
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
import com.yellowtwigs.knockin.ui.HelpActivity
import com.yellowtwigs.knockin.ui.contacts.list.Main2Activity
import com.yellowtwigs.knockin.ui.in_app.PremiumActivity
import com.yellowtwigs.knockin.ui.teleworking.TeleworkingActivity

class SettingsActivity : AppCompatActivity() {

    //region ========================================== Val or Var ==========================================

    private var drawerLayout: DrawerLayout? = null

    private var callPopup: Boolean = true
    private var isBought: Boolean = false
//    private var searchbarPos: Boolean = false

    private var settings_CallPopupSwitch: Switch? = null
//    private var settings_SearchBarPosSwitch: Switch? = null

    private var settings_PermissionsPhoneLayout: RelativeLayout? = null
    private var settings_PermissionsPhoneMaterialButton: MaterialButton? = null
    private var settings_PermissionsPhoneLoading: ProgressBar? = null
    private var settings_PermissionsPhoneChecked: AppCompatImageView? = null

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

        val sharedProductIsBoughtPreferences = this.getSharedPreferences("IsBought", Context.MODE_PRIVATE)
        isBought = sharedProductIsBoughtPreferences.getBoolean("IsBought", true)

        val sharedPreferencePopup = getSharedPreferences("Phone_call", Context.MODE_PRIVATE)
        callPopup = sharedPreferencePopup.getBoolean("popup", true)

//        searchbarPos = sharedPreferencePopup.getBoolean("searchbarPos", false)

        //region ======================================= FindViewById =======================================

        settings_CallPopupSwitch = findViewById(R.id.settings_call_popup_switch)
//        settings_SearchBarPosSwitch = findViewById(R.id.settings_searchbar_pos_switch)

        settings_PermissionsPhoneLayout = findViewById(R.id.settings_permissions_phone_layout)

        settings_PermissionsPhoneMaterialButton = findViewById(R.id.settings_permissions_phone_button)
        settings_PermissionsPhoneLoading = findViewById(R.id.settings_permissions_phone_loading)
        settings_PermissionsPhoneChecked = findViewById(R.id.settings_permissions_phone_check)
        val phoneCallAlertTv = findViewById<TextView>(R.id.settings_permissions_phone_subtitle)

        phoneCallAlertTv.text = String.format(getString(R.string.permissions_subtitle), getString(R.string.app_name))

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
            drawerLayout?.closeDrawers()

            when (menuItem.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this@SettingsActivity, Main2Activity::class.java))
                }
                R.id.navigation_teleworking -> startActivity(
                    Intent(
                        this@SettingsActivity,
                        TeleworkingActivity::class.java
                    )
                )
                R.id.nav_notif_config -> startActivity(Intent(this@SettingsActivity, ManageNotificationActivity::class.java))
                R.id.nav_manage_screen -> startActivity(Intent(this@SettingsActivity, ManageMyScreenActivity::class.java))
                R.id.nav_in_app -> startActivity(Intent(this@SettingsActivity, PremiumActivity::class.java))
                R.id.nav_help -> startActivity(Intent(this@SettingsActivity, HelpActivity::class.java))
            }

            true
        }

        //endregion

        if (checkPermission(Manifest.permission.SEND_SMS) || checkPermission(Manifest.permission.CALL_PHONE)) {
            settings_PermissionsPhoneLayout!!.visibility = View.GONE
        } else {
            settings_PermissionsPhoneLayout!!.visibility = View.VISIBLE
        }

        if (callPopup) {
            settings_CallPopupSwitch!!.isChecked = true
        }

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

        settings_PermissionsPhoneMaterialButton!!.setOnClickListener {
            val arraylistPermission = ArrayList<String>()
//            arraylistPermission.add(Manifest.permission.SEND_SMS)
            arraylistPermission.add(Manifest.permission.CALL_PHONE)
            ActivityCompat.requestPermissions(this, arraylistPermission.toArray(arrayOfNulls<String>(arraylistPermission.size)), REQUEST_CODE_SMS_AND_CALL)
            settings_PermissionsPhoneMaterialButton!!.visibility = View.INVISIBLE
            settings_PermissionsPhoneLoading!!.visibility = View.VISIBLE
        }

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

    companion object {
        const val REQUEST_CODE_SMS_AND_CALL = 5
    }
}
