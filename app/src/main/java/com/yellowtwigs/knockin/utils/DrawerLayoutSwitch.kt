package com.yellowtwigs.knockin.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.widget.SwitchCompat
import com.yellowtwigs.knockin.R

object DrawerLayoutSwitch {
    fun callPopupSwitch(switchCompat: SwitchCompat, activity: Activity) {
        switchCompat.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val sharedCallPopupPreferences: SharedPreferences =
                    activity.getSharedPreferences("Phone_call", Context.MODE_PRIVATE)
                val edit: SharedPreferences.Editor = sharedCallPopupPreferences.edit()
                edit.putBoolean("popup", true)
                edit.apply()
            } else {
                val sharedCallPopupPreferences: SharedPreferences =
                    activity.getSharedPreferences("Phone_call", Context.MODE_PRIVATE)
                val edit: SharedPreferences.Editor = sharedCallPopupPreferences.edit()
                edit.putBoolean("popup", false)
                edit.apply()
            }
        }
    }

    fun themeSwitch(
        switchCompat: SwitchCompat,
        activity: Activity,
        sharedThemePreferences: SharedPreferences
    ) {
        switchCompat.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                activity.setTheme(R.style.AppThemeDark)
//                main_constraintLayout!!.setBackgroundResource(R.drawable.dark_background)
                val edit: SharedPreferences.Editor = sharedThemePreferences.edit()
                edit.putBoolean("darkTheme", true)
                edit.apply()
                activity.startActivity(Intent(activity, activity::class.java))
            } else {
                activity.setTheme(R.style.AppTheme)
//                main_constraintLayout!!.setBackgroundResource(R.drawable.mr_white_blur_background)
                val edit: SharedPreferences.Editor = sharedThemePreferences.edit()
                edit.putBoolean("darkTheme", false)
                edit.apply()
                activity.startActivity(Intent(activity, activity::class.java))
            }
        }
    }

    fun checkSwitchFromLeftDrawer(cxt: Activity, sharedThemePreferences: SharedPreferences) {
        val sharedPreferencePopup = cxt.getSharedPreferences("Phone_call", Context.MODE_PRIVATE)
        val callPopupSwitch = cxt.findViewById<SwitchCompat>(R.id.settings_call_popup_switch)
        val themeSwitch = cxt.findViewById<SwitchCompat>(R.id.settings_left_drawer_theme_switch)

        if (sharedThemePreferences.getBoolean("darkTheme", false)) {
            themeSwitch.isChecked = true
        }

        if (sharedPreferencePopup.getBoolean("popup", true)) {
            callPopupSwitch.isChecked = true
        }

        callPopupSwitch(callPopupSwitch, cxt)
        themeSwitch(themeSwitch, cxt, sharedThemePreferences)
    }
}