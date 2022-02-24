package com.yellowtwigs.knockin.utils

import android.content.Context
import com.yellowtwigs.knockin.R

object AppTheme {

    fun checkTheme(context: Context) {
        val sharedThemePreferences =
            context.getSharedPreferences("Knockin_Theme", Context.MODE_PRIVATE)
        if (sharedThemePreferences.getBoolean("darkTheme", false)) {
            context.setTheme(R.style.AppThemeDark)
        } else {
            context.setTheme(R.style.AppTheme)
        }
    }
}