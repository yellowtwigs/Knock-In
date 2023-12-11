package com.yellowtwigs.knockin.utils

import android.app.Activity
import android.app.ActivityManager
import android.app.NotificationManager
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.TextUtils
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.yellowtwigs.knockin.R

object EveryActivityUtils {

    fun isNotificationServiceEnabled(packageName: String, contentResolver: ContentResolver): Boolean {
        val str = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        if (!TextUtils.isEmpty(str)) {
            val names = str.split(":")
            for (i in names.indices) {
                val cn = ComponentName.unflattenFromString(names[i])
                if (cn != null) {
                    if (TextUtils.equals(packageName, cn.packageName)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun checkIfDoNotDisturbActivated(cxt: Activity) {
        val mNotificationManager = cxt.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
        val name = "DoNotDisturb"
        val voiceCallAllEnabledSwitchChecked = cxt.getSharedPreferences(name, Context.MODE_PRIVATE)
        if (mNotificationManager.currentInterruptionFilter == 2) {
            val policy = NotificationManager.Policy(
                NotificationManager.Policy.PRIORITY_CATEGORY_CALLS,
                NotificationManager.Policy.PRIORITY_SENDERS_STARRED,
                NotificationManager.Policy.PRIORITY_SENDERS_STARRED
            )
            mNotificationManager.notificationPolicy = policy

            val edit = voiceCallAllEnabledSwitchChecked.edit()
            edit.putBoolean(name, true)
            edit.apply()
        } else {
            val edit = voiceCallAllEnabledSwitchChecked.edit()
            edit.putBoolean(name, false)
            edit.apply()

            val policy = NotificationManager.Policy(
                NotificationManager.Policy.PRIORITY_SENDERS_ANY,
                NotificationManager.Policy.PRIORITY_SENDERS_ANY,
                NotificationManager.Policy.PRIORITY_SENDERS_ANY
            )
            mNotificationManager.notificationPolicy = policy
        }
    }

    fun checkIfGoEdition(cxt: Activity): Boolean {
        val am = cxt.getSystemService(AppCompatActivity.ACTIVITY_SERVICE) as ActivityManager
        return am.isLowRamDevice
    }

    fun checkTheme(cxt: Activity, packageName: String, contentResolver: ContentResolver) {

        if (isNotificationServiceEnabled(packageName, contentResolver)) {
            checkIfDoNotDisturbActivated(cxt)
        }

        val sharedThemePreferences = cxt.getSharedPreferences("Knockin_Theme", Context.MODE_PRIVATE)
        if (sharedThemePreferences.getBoolean("darkTheme", false)) {
            cxt.setTheme(R.style.AppThemeDark)
        } else {
            cxt.setTheme(R.style.AppTheme)
        }
    }

    fun getAppOnPhone(cxt: Activity): ArrayList<String> {
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        val resolveInfoList = cxt.packageManager.queryIntentActivities(intent, 0)
        val packageNameList = ArrayList<String>()
        for (resolveInfo in resolveInfoList) {
            val activityInfo = resolveInfo.activityInfo
            packageNameList.add(activityInfo.applicationInfo.packageName)
        }
        return packageNameList
    }

    fun hideKeyboard(cxt: Activity) {
        val view = cxt.currentFocus
        view?.let { v ->
            val imm = cxt.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }

//    fun setupTeleworkingItem(drawerLayout: DrawerLayout, cxt: Activity) {
//        val itemLayout = cxt.findViewById<ConstraintLayout>(R.id.teleworking_item)
//        val itemText = cxt.findViewById<AppCompatTextView>(R.id.teleworking_item_text)
//
//        itemText.text = "${cxt.getString(R.string.teleworking)} ${cxt.getString(R.string.left_drawer_settings)}"
//
//        itemLayout.setOnClickListener {
//            drawerLayout.closeDrawer(GravityCompat.START)
//            cxt.startActivity(Intent(cxt, TeleworkingActivity::class.java))
//        }
//    }
}