package com.yellowtwigs.knockin.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.ui.teleworking.TeleworkingActivity

object EveryActivityUtils {

    fun checkTheme(cxt: Activity) {
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

    fun setupTeleworkingItem(drawerLayout: DrawerLayout, cxt: Activity) {
        val itemLayout = cxt.findViewById<ConstraintLayout>(R.id.teleworking_item)
        val itemText = cxt.findViewById<AppCompatTextView>(R.id.teleworking_item_text)

        itemText.text = "${cxt.getString(R.string.teleworking)} ${cxt.getString(R.string.left_drawer_settings)}"

        itemLayout.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            cxt.startActivity(Intent(cxt, TeleworkingActivity::class.java))
        }
    }
}