package com.example.knocker.controller.activity.firstLaunch

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.knocker.R
import com.example.knocker.model.ContactList


class SynchronizeContactActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_synchronize_contact)
        val synchronise: Button = findViewById(R.id.synchronise_contact_accept_button)
        val notSynchronise: Button = findViewById(R.id.synchronise_contact_not_accept_button)

        synchronise.setOnClickListener {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), REQUEST_CODE_READ_CONTACT)
        }
        notSynchronise.setOnClickListener {
            overlayAlertDialog().show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_READ_CONTACT) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.synchronise_contact_synchronise_toast, Toast.LENGTH_LONG).show()
                ContactList(this).getAllContacsInfoSync(contentResolver)
                startActivity(Intent(this@SynchronizeContactActivity, AcceptNotificationActivity::class.java))
                finish()
            } else {
                overlayAlertDialog().show()
            }
        }
    }

    private fun overlayAlertDialog(): android.app.AlertDialog {
        val alertDialogBuilder = android.app.AlertDialog.Builder(this)
        alertDialogBuilder.setTitle(applicationContext.resources.getString(R.string.app_name))
        alertDialogBuilder.setMessage(applicationContext.resources.getString(R.string.synchronise_contact_alert_dialog))
        alertDialogBuilder.setPositiveButton("ok"
        ) { _, _ ->
            startActivity(Intent(this@SynchronizeContactActivity, AcceptNotificationActivity::class.java))
        }

        return alertDialogBuilder.create()
    }

    override fun onBackPressed() {

    }

    companion object {
        const val REQUEST_CODE_READ_CONTACT = 99
    }
}
