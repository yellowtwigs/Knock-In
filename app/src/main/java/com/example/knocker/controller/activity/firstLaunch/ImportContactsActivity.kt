package com.example.knocker.controller.activity.firstLaunch

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.example.knocker.R
import com.example.knocker.controller.activity.MainActivity
import com.example.knocker.model.ContactList
import com.example.knocker.model.DbWorkerThread
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class ImportContactsActivity : AppCompatActivity() {

    private var main_loadingPanel: RelativeLayout? = null
    private lateinit var main_mDbWorkerThread: DbWorkerThread
    private var import_contacts_accept_button: Button? = null
    private var import_contacts_not_accept_button: Button? = null
    private var import_contacts_LongText: TextView? = null
    private var import_contacts_Title: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_import_contacts)
        import_contacts_accept_button = findViewById(R.id.import_contacts_accept_button)
        import_contacts_not_accept_button = findViewById(R.id.import_contacts_not_accept_button)
        main_loadingPanel = findViewById(R.id.loadingPanel)
        import_contacts_LongText = findViewById(R.id.import_contacts_long_text)
        import_contacts_Title = findViewById(R.id.import_contacts_title)


        main_mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        main_mDbWorkerThread.start()

        import_contacts_accept_button!!.setOnClickListener {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), REQUEST_CODE_READ_CONTACT)
        }
        import_contacts_not_accept_button!!.setOnClickListener {
            overlayAlertDialog()!!.show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_READ_CONTACT) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.import_contacts_toast, Toast.LENGTH_LONG).show()
                import_contacts_accept_button!!.visibility = View.GONE
                import_contacts_not_accept_button!!.visibility = View.GONE
                import_contacts_LongText!!.visibility = View.GONE
                import_contacts_Title!!.visibility = View.GONE
                main_loadingPanel!!.visibility = View.VISIBLE
                val sync = Runnable {
                    ContactList(this).getAllContacsInfoSync(contentResolver)
                    intentToMain()
                }
                main_mDbWorkerThread.postTask(sync)
            } else {
                overlayAlertDialog()!!.show()
            }
        }
    }

    fun intentToMain() {
        val intent = Intent(this@ImportContactsActivity, MainActivity::class.java)
        intent.putExtra("fromImportContact", true)
        startActivity(intent)
        finish()
    }

    private fun overlayAlertDialog(): AlertDialog? {

        return MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                .setTitle(applicationContext.resources.getString(R.string.app_name))
                .setMessage(applicationContext.resources.getString(R.string.import_contacts_alert_dialog))
                .setPositiveButton("Ok"
                ) { _, _ ->
                    intentToMain()
                }
                .show()
    }

    override fun onBackPressed() {

    }

    companion object {
        const val REQUEST_CODE_READ_CONTACT = 2
    }
}
