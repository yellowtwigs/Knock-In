package com.example.knocker.controller.activity.firstLaunch

import android.Manifest
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.app.ActivityCompat
import com.example.knocker.R
import com.example.knocker.controller.activity.MainActivity
import com.example.knocker.controller.activity.MultiSelectActivity
import com.example.knocker.model.ContactList
import com.example.knocker.model.DbWorkerThread
import com.google.android.material.button.MaterialButton

class StartActivity : AppCompatActivity() {

    //region ========================================== Val or Var ==========================================

    private var start_activity_ImportContacts: MaterialButton? = null
    private var start_activity_ActivateNotifications: MaterialButton? = null
    private var start_activity_AuthorizeSuperposition: MaterialButton? = null
    private var start_activity_Permissions: MaterialButton? = null

    private var start_activity_Next: MaterialButton? = null

    private var start_activity_ImportContactsLoading: ProgressBar? = null
    private var start_activity_ActivateNotificationsLoading: ProgressBar? = null
    private var start_activity_AuthorizeSuperpositionLoading: ProgressBar? = null
    private var start_activity_PermissionsLoading: ProgressBar? = null

    private var start_activity_ImportContactsCheck: AppCompatImageView? = null
    private var start_activity_ActivateNotificationsCheck: AppCompatImageView? = null
    private var start_activity_AuthorizeSuperpositionCheck: AppCompatImageView? = null
    private var start_activity_PermissionsCheck: AppCompatImageView? = null

    private lateinit var start_activity_mDbWorkerThread: DbWorkerThread

    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_activity)

        //region ========================================== FindViewById ==========================================

        start_activity_ImportContacts = findViewById(R.id.start_activity_import_contacts_button)
        start_activity_ActivateNotifications = findViewById(R.id.start_activity_activate_notifications_button)
        start_activity_AuthorizeSuperposition = findViewById(R.id.start_activity_superposition_button)
        start_activity_Permissions = findViewById(R.id.start_activity_permissions_button)

        start_activity_Next = findViewById(R.id.start_activity_next)

        start_activity_ImportContactsLoading = findViewById(R.id.start_activity_import_contacts_loading)
        start_activity_ActivateNotificationsLoading = findViewById(R.id.start_activity_activate_notifications_loading)
        start_activity_AuthorizeSuperpositionLoading = findViewById(R.id.start_activity_superposition_loading)
        start_activity_PermissionsLoading = findViewById(R.id.start_activity_permissions_loading)

        start_activity_ImportContactsCheck = findViewById(R.id.start_activity_import_contacts_check)
        start_activity_ActivateNotificationsCheck = findViewById(R.id.start_activity_activate_notifications_check)
        start_activity_AuthorizeSuperpositionCheck = findViewById(R.id.start_activity_superposition_check)
        start_activity_PermissionsCheck = findViewById(R.id.start_activity_permissions_check)

        //endregion

        //region ========================================== WorkerThread ==========================================

        start_activity_mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        start_activity_mDbWorkerThread.start()

        //endregion

        //region ========================================== Listeners ==========================================

        start_activity_ImportContacts!!.setOnClickListener {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), ImportContactsActivity.REQUEST_CODE_READ_CONTACT)
            start_activity_ImportContacts!!.visibility = View.GONE
            start_activity_ImportContactsLoading!!.visibility = View.VISIBLE

            val SPLASH_DISPLAY_LENGHT = 3000

            Handler().postDelayed({
                start_activity_ImportContactsLoading!!.visibility = View.GONE
                start_activity_ImportContactsCheck!!.visibility = View.VISIBLE

            }, SPLASH_DISPLAY_LENGHT.toLong())
        }

        start_activity_ActivateNotifications!!.setOnClickListener {
            activateNotificationsClick()
            start_activity_ActivateNotifications!!.visibility = View.GONE
            start_activity_ActivateNotificationsLoading!!.visibility = View.VISIBLE

            val SPLASH_DISPLAY_LENGHT = 3000

            Handler().postDelayed({
                start_activity_ActivateNotificationsLoading!!.visibility = View.GONE
                start_activity_ActivateNotificationsCheck!!.visibility = View.VISIBLE

            }, SPLASH_DISPLAY_LENGHT.toLong())
        }

        start_activity_AuthorizeSuperposition!!.setOnClickListener {
            val intentPermission = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivity(intentPermission)
            start_activity_AuthorizeSuperposition!!.visibility = View.GONE
            start_activity_AuthorizeSuperpositionLoading!!.visibility = View.VISIBLE

            val SPLASH_DISPLAY_LENGHT = 3000

            Handler().postDelayed({
                start_activity_AuthorizeSuperpositionLoading!!.visibility = View.GONE
                start_activity_AuthorizeSuperpositionCheck!!.visibility = View.VISIBLE

            }, SPLASH_DISPLAY_LENGHT.toLong())
        }

        start_activity_Permissions!!.setOnClickListener {
            val arraylistPermission = ArrayList<String>()
            arraylistPermission.add(Manifest.permission.SEND_SMS)
            arraylistPermission.add(Manifest.permission.CALL_PHONE)
            ActivityCompat.requestPermissions(this, arraylistPermission.toArray(arrayOfNulls<String>(arraylistPermission.size)), 2)
            start_activity_Permissions!!.visibility = View.GONE
            start_activity_PermissionsLoading!!.visibility = View.VISIBLE

            val SPLASH_DISPLAY_LENGHT = 3000

            Handler().postDelayed({
                start_activity_PermissionsLoading!!.visibility = View.GONE
                start_activity_PermissionsCheck!!.visibility = View.VISIBLE

            }, SPLASH_DISPLAY_LENGHT.toLong())
        }

        start_activity_Next!!.setOnClickListener {
            startActivity(Intent(this@StartActivity, MultiSelectActivity::class.java))
        }

        //endregion
    }

    //region ========================================== Functions ==========================================

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_READ_CONTACT) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.import_contacts_toast, Toast.LENGTH_LONG).show()
                val sync = Runnable {
                    ContactList(this).getAllContacsInfoSync(contentResolver)
                }
                start_activity_mDbWorkerThread.postTask(sync)
            }
        }
    }

    private fun activateNotificationsClick() {
        startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
        val intentFilter = IntentFilter()
        intentFilter.addAction("com.example.knocker.notificationExemple")
    }

    companion object {
        const val REQUEST_CODE_READ_CONTACT = 2
    }

    //endregion
}
