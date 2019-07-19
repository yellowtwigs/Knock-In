package com.example.knocker.controller.activity.firstLaunch

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import com.example.knocker.R
import com.example.knocker.controller.activity.MainActivity
import com.example.knocker.model.ContactList
import com.example.knocker.model.DbWorkerThread


class SynchronizeContactActivity : AppCompatActivity() {

    private var main_loadingPanel: RelativeLayout? = null
    private lateinit var main_mDbWorkerThread: DbWorkerThread
    private var synchronise_contact_accept_button: Button? = null
    private var synchronise_contact_not_accept_button: Button? = null
    private var textView2: TextView? = null
    private var synchronise_contact_title: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_synchronize_contact)
        synchronise_contact_accept_button = findViewById(R.id.synchronise_contact_accept_button)
        synchronise_contact_not_accept_button = findViewById(R.id.synchronise_contact_not_accept_button)
        main_loadingPanel = findViewById(R.id.loadingPanel)
        textView2 = findViewById(R.id.textView2)
        synchronise_contact_title = findViewById(R.id.synchronise_contact_title)


        main_mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        main_mDbWorkerThread.start()

        synchronise_contact_accept_button!!.setOnClickListener {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), REQUEST_CODE_READ_CONTACT)
        }
        synchronise_contact_not_accept_button!!.setOnClickListener {
            overlayAlertDialog().show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_READ_CONTACT) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.synchronise_contact_synchronise_toast, Toast.LENGTH_LONG).show()
                synchronise_contact_accept_button!!.visibility = View.GONE
                synchronise_contact_not_accept_button!!.visibility = View.GONE
                textView2!!.visibility = View.GONE
                synchronise_contact_title!!.visibility = View.GONE
                main_loadingPanel!!.visibility = View.VISIBLE
                val sync = Runnable {
                    ContactList(this).getAllContacsInfoSync(contentResolver)
                    startActivity(Intent(this@SynchronizeContactActivity, MainActivity::class.java))
                    finish()
                }
                main_mDbWorkerThread.postTask(sync)
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
            startActivity(Intent(this@SynchronizeContactActivity, MainActivity::class.java))
        }

        return alertDialogBuilder.create()
    }

    override fun onBackPressed() {

    }

    companion object {
        const val REQUEST_CODE_READ_CONTACT = 99
    }
}
