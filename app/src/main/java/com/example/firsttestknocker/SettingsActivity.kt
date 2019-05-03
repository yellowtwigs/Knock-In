package com.example.firsttestknocker

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.net.Uri
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatDelegate
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import kotlinx.android.synthetic.main.alert_dialog_phone_number.view.*

class SettingsActivity : AppCompatActivity() {

    private var button_three: Button? = null
    private var button_four: Button? = null
    private var button_five: Button? = null
    private var button_six: Button? = null
    private var activity_settings_switch_Theme: Switch? = null
    private var nbGrid: Int = 3
    private var knockerTheme: Boolean? = true

    private var edit_owner_RelativeLayout_settings: RelativeLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.DarkTheme)
        } else {
            setTheme(R.style.AppTheme)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT)

        val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
        nbGrid = sharedPreferences.getInt("gridview", 3)

        val sharedThemePreferences = getSharedPreferences("Knocker_Theme", Context.MODE_PRIVATE)
        knockerTheme = sharedThemePreferences.getBoolean("theme", true)

        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar.setHomeAsUpIndicator(R.drawable.ic_left_arrow)
        actionbar.title = "Settings"

        button_three = findViewById(R.id.activity_settings_three_column)
        button_four = findViewById(R.id.activity_settings_four_column)
        button_five = findViewById(R.id.activity_settings_five_column)
        button_six = findViewById(R.id.activity_settings_six_column)

        activity_settings_switch_Theme = findViewById(R.id.activity_settings_switch_theme)
        edit_owner_RelativeLayout_settings = findViewById(R.id.edit_owner_RelativeLayout_settings)

        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            activity_settings_switch_Theme!!.setChecked(true);
        }

        activity_settings_switch_Theme!!.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                val sharedThemePreferences: SharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
                val edit: SharedPreferences.Editor = sharedThemePreferences.edit()
                edit.putBoolean("ThemeLight", true)
                edit.putBoolean("ThemeDark", false)
                edit.apply()
                restartActivity()
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                val sharedThemePreferences = getSharedPreferences("Knocker_Theme", Context.MODE_PRIVATE)
                val edit: SharedPreferences.Editor = sharedThemePreferences.edit()
                edit.putBoolean("ThemeDark", true)
                edit.putBoolean("ThemeLight", false)
                edit.apply()
                restartActivity()
            }
        })

        edit_owner_RelativeLayout_settings!!.setOnClickListener {
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.alert_dialog_edit_owner, null)
            val mBuilder = AlertDialog.Builder(this)
                    .setView(mDialogView)
                    .setTitle(R.string.settings_owner_edit)
            val mAlertDialog = mBuilder.show()


        }

        button_three?.setOnClickListener {
            nbGrid = 3
        }

        button_four?.setOnClickListener {
            nbGrid = 4
        }

        button_five?.setOnClickListener {
            nbGrid = 5
        }

        button_six?.setOnClickListener {
            nbGrid = 6
        }

        val notificationRL = findViewById<View>(R.id.notification_RelativeLayout_settings) as RelativeLayout
        notificationRL.setOnClickListener {
            val notificationSettingsIntent = Intent(this@SettingsActivity, ManageNotificationActivity::class.java)
            startActivity(notificationSettingsIntent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val loginIntent = Intent(this@SettingsActivity, MainActivity::class.java)
        val sharedPreferences: SharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
        val edit: SharedPreferences.Editor = sharedPreferences.edit()
        edit.putInt("gridview", nbGrid)
        edit.apply()
        startActivity(loginIntent)
        finish()
        return super.onOptionsItemSelected(item)
    }

    fun restartActivity() {
        val intent = Intent(this@SettingsActivity, SettingsActivity::class.java)
        startActivity(intent)
        finish()
    }
}
