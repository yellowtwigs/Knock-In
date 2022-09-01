package com.yellowtwigs.knockin.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.widget.SwitchCompat
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.ui.contacts.MainActivity

object EveryActivityUtils {
    fun checkThemePreferences(cxt: Activity) {
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
}