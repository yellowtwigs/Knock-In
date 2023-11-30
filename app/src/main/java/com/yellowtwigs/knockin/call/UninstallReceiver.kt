package com.yellowtwigs.knockin.call

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class UninstallIntentReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val packageNames = intent?.getStringArrayExtra("android.intent.extra.PACKAGES")

        if (packageNames != null) {
            for (packageName in packageNames) {
                Log.d("TestUninstall", "Package $packageName has been removed.")
                if (packageName != null && packageName == "com.yellowtwigs.knockin") {
                }
            }
        }
    }
}