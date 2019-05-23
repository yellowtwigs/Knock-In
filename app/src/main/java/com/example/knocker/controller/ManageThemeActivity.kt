package com.example.knocker.controller

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.CompoundButton
import android.widget.Switch
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import com.example.knocker.R


class ManageThemeActivity : AppCompatActivity() {

    private var manage_theme_SwitchTheme: Switch? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_theme)

        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar.setHomeAsUpIndicator(R.drawable.ic_left_arrow)
        actionbar.title = "Theme"

        manage_theme_SwitchTheme = findViewById(R.id.manage_theme_switch)

        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            manage_theme_SwitchTheme!!.isChecked = true
        }

        manage_theme_SwitchTheme!!.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                val sharedThemePreferences: SharedPreferences = getSharedPreferences("Knocker_Theme", Context.MODE_PRIVATE)
                val edit: SharedPreferences.Editor = sharedThemePreferences.edit()
                edit.putBoolean("theme", false)
                edit.apply()
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                val sharedThemePreferences = getSharedPreferences("Knocker_Theme", Context.MODE_PRIVATE)
                val edit: SharedPreferences.Editor = sharedThemePreferences.edit()
                edit.putBoolean("theme", true)
                edit.apply()
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        startActivity(Intent(this@ManageThemeActivity, MainActivity::class.java))
        finish()
        return super.onOptionsItemSelected(item)
    }
}
