package com.yellowtwigs.knockin.ui.call

import android.app.Activity
import android.app.role.RoleManager
import android.content.Intent
import android.os.Build
import android.telecom.TelecomManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {

    /**
     * Event that can be received in every activity that extends [BaseActivity]
     */
}

@RequiresApi(Build.VERSION_CODES.Q)
fun Activity.startCallScreeningPermissionScreen(requestId: Int) {
    val roleManager = this.getSystemService(AppCompatActivity.ROLE_SERVICE) as RoleManager
    val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
    this.startActivityForResult(intent, requestId)
}

fun Activity.hasDialerCapability(): Boolean {
    val telecomManager = getSystemService(AppCompatActivity.TELECOM_SERVICE) as TelecomManager
    return packageName.equals(telecomManager.defaultDialerPackage)
}

fun Activity.startSelectDialerScreen(requestId: Int) {
    if (this.hasDialerCapability()) return
    val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
        .putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
    startActivityForResult(intent, requestId)
}