package com.yellowtwigs.knockin.ui.first_launch

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.app.ActivityCompat
import com.google.android.material.button.MaterialButton
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.models.DbWorkerThread
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yellowtwigs.knockin.ui.contacts.ContactListActivity
import com.yellowtwigs.knockin.models.data.Contact

/**
 * Activité Qui Nous permet d'importer nos contact pour les buildVariant Contacter et Converser
 * @author Florian Striebel, Kenzy Suon
 */
class ImportContactsActivity : AppCompatActivity() {

    //region ========================================== Val or Var ==========================================

    private lateinit var main_mDbWorkerThread: DbWorkerThread

    private var import_contacts_activity_ImportContacts: MaterialButton? = null
    private var import_contacts_activity_Permissions: MaterialButton? = null
    private var import_contacts_activity_ImportContactsLoading: ProgressBar? = null
    private var import_contacts_activity_PermissionsLoading: ProgressBar? = null

    private var import_contacts_activity_Next: MaterialButton? = null
    private var import_contacts_activity_Skip: MaterialButton? = null

    private var import_contacts_ImportContactsCheck: AppCompatImageView? = null
    private var start_activity_PermissionsCheck: AppCompatImageView? = null
    private var import_contacts_activity_import_contacts_check: AppCompatImageView? = null

    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_import_contacts)

        val listApp = getAppOnPhone()

        //region ========================================== FindViewById ==========================================

        import_contacts_activity_ImportContacts = findViewById(R.id.import_contacts_activity_import_contacts_button)
        import_contacts_activity_Permissions = findViewById(R.id.import_contacts_activity_permissions_button)
        import_contacts_activity_ImportContactsLoading = findViewById(R.id.import_contacts_activity_import_contacts_loading)
        import_contacts_activity_PermissionsLoading = findViewById(R.id.import_contacts_activity_permissions_loading)

        import_contacts_activity_Next = findViewById(R.id.import_contacts_activity_next)
        import_contacts_activity_Skip = findViewById(R.id.import_contacts_activity_skip)

        import_contacts_ImportContactsCheck = findViewById(R.id.import_contacts_activity_import_contacts_check)
        start_activity_PermissionsCheck = findViewById(R.id.import_contacts_activity_permissions_check)
        import_contacts_activity_import_contacts_check = findViewById(R.id.import_contacts_activity_import_contacts_check)

        //endregion

        main_mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        main_mDbWorkerThread.start()


        //region Listener

        import_contacts_activity_ImportContacts!!.setOnClickListener {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), REQUEST_CODE_READ_CONTACT)
            import_contacts_activity_ImportContacts!!.visibility = View.INVISIBLE

            val displayLoading = Runnable {
                import_contacts_activity_ImportContactsLoading!!.visibility = View.VISIBLE
            }
            runOnUiThread(displayLoading)

            if (listApp.contains("com.whatsapp")) {
                val importWhatsappSharedPreferences: SharedPreferences = getSharedPreferences("importWhatsappPreferences", Context.MODE_PRIVATE)
                val edit: SharedPreferences.Editor = importWhatsappSharedPreferences.edit()
                edit.putBoolean("importWhatsappPreferences", true)
                edit.apply()
            }
        }

        import_contacts_activity_Permissions!!.setOnClickListener {
            val arraylistPermission = ArrayList<String>()
            arraylistPermission.add(Manifest.permission.SEND_SMS)
            arraylistPermission.add(Manifest.permission.CALL_PHONE)
            ActivityCompat.requestPermissions(this, arraylistPermission.toArray(arrayOfNulls<String>(arraylistPermission.size)), REQUEST_CODE_SMS_AND_CALL)
            import_contacts_activity_Permissions!!.visibility = View.INVISIBLE
            import_contacts_activity_PermissionsLoading!!.visibility = View.VISIBLE
        }

        //Bouton qui apparait lorsque tout les autorisation ont un check. Lors du click affichage d'un alertDialog d'information
        import_contacts_activity_Next!!.setOnClickListener {
            val intent = Intent(this@ImportContactsActivity, ContactListActivity::class.java)
            intent.putExtra("fromStartActivity", true)
            startActivity(intent)
            finish()
        }

        //lors du click affichage d'un message de prévention
        import_contacts_activity_Skip!!.setOnClickListener {
            //            start_activity_ImportContacts!!.visibility = View.INVISIBLE
//            buildMultiSelectAlertDialog()
            buildLeaveAlertDialog()
        }

        //endregion
    }

    /**
     *Méthode appellé par le système lorsque l'utilisateur a accepté ou refuser une demande de permission
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_READ_CONTACT) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.import_contacts_toast, Toast.LENGTH_LONG).show()
                val sync = Runnable {
                    ContactManager(this).getAllContacsInfoSync(contentResolver)

                    val sharedPreferencesSync = getSharedPreferences("save_last_sync", Context.MODE_PRIVATE)
                    var index = 1
                    var stringSet = listOf<String>()
                    if (sharedPreferencesSync.getStringSet(index.toString(), null) != null)
                        stringSet = sharedPreferencesSync.getStringSet(index.toString(), null)!!.sorted()
                    arrayListOf<Pair<Contact, List<ContactDetailDB>>>()
                    while (sharedPreferencesSync.getStringSet(index.toString(), null) != null && stringSet.isNotEmpty()) {
                        stringSet = sharedPreferencesSync.getStringSet(index.toString(), null)!!.sorted()
                        index++
                    }

                    val runnable = Runnable {
                        import_contacts_activity_ImportContactsLoading!!.visibility = View.INVISIBLE
                        import_contacts_activity_import_contacts_check!!.visibility = View.VISIBLE
                        allIsChecked()
                    }
                    runOnUiThread(runnable)
                }
                main_mDbWorkerThread.postTask(sync)
            } else {
                import_contacts_activity_ImportContactsLoading!!.visibility = View.INVISIBLE
                import_contacts_activity_ImportContacts!!.visibility = View.VISIBLE
            }
        }

        if (REQUEST_CODE_SMS_AND_CALL == requestCode) {

            import_contacts_activity_PermissionsLoading!!.visibility = View.INVISIBLE
            import_contacts_activity_import_contacts_check!!.visibility = View.VISIBLE
        }
        allIsChecked()
    }

    /**
     *méthode qui lance  l'activity Tutorial
     */
    fun intentToTutorial() {
        val intent = Intent(this@ImportContactsActivity, ContactListActivity::class.java)
        intent.putExtra("fromImportContact", true)
        startActivity(intent)
        finish()
    }

    /**
     * Si toutes les autorisations sont validées et que les contacts ont fini d'être charger alors nous changeons le bouton passer pour un bouton suivant
     */
    private fun allIsChecked() {
        if (import_contacts_ImportContactsCheck!!.visibility == View.VISIBLE) {
            import_contacts_activity_Next!!.visibility = View.VISIBLE
        }
    }

    /**
     * Initialise une alertDialog pour prévenir l'utilisateur que celui-ci pourra autoriser l'accès au contact plus tard
     * @return alertDialog [AlertDialog]
     */
    private fun overlayAlertDialog(): AlertDialog? {

        return MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                .setTitle(applicationContext.resources.getString(R.string.app_name))
                .setMessage(applicationContext.resources.getString(R.string.import_contacts_alert_dialog))
                .setPositiveButton("Ok"
                ) { _, _ ->
                    intentToTutorial()
                }
                .show()
    }

    /**
     * Réécriture de la méthode onBackPressed lorsque nous appuyant sur le boutton retour du téléphone rien n'est fait
     */
    override fun onBackPressed() {}

    companion object {
        const val REQUEST_CODE_READ_CONTACT = 2
        const val REQUEST_CODE_SMS_AND_CALL = 5
    }

    /**
     * Demande à l'utilisateur si celui-ci désire choisir ses contacts prioritaires
     * @return [AlertDialog]
     */
    private fun buildMultiSelectAlertDialog(): AlertDialog {
        return MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                .setBackground(getDrawable(R.color.backgroundColor))
                .setTitle(applicationContext.resources.getString(R.string.app_name))
                .setMessage(applicationContext.resources.getString(R.string.import_contacts_alert_dialog))
                .setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
                    intentToTutorial()
                }
                .setNegativeButton(R.string.alert_dialog_later)
                { _, _ ->
                    closeContextMenu()
                    val intent = Intent(this@ImportContactsActivity, ContactListActivity::class.java)
                    intent.putExtra("fromStartActivity", true)
                    startActivity(intent)
                    finish()
                }
                .show()
    }

    /**
     * Permet à l'utilisateur de passer les demandes d'autorisations
     */
    private fun buildLeaveAlertDialog(): AlertDialog {
        val message = if (import_contacts_activity_ImportContactsLoading!!.visibility == View.VISIBLE) {//vérifie que le téléphone ne charge pas les contacts sinon celui-ci prévient l'utilisateur que ces contact ne seront pas tous chargés
            getString(R.string.start_activity_skip_alert_dialog_message_importation)
        } else {
            getString(R.string.start_activity_skip_alert_dialog_message)
        }

        return MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                .setBackground(getDrawable(R.color.backgroundColor))
                .setTitle(getString(R.string.start_activity_skip_alert_dialog_title))
                .setMessage(message)
                .setPositiveButton(R.string.start_activity_skip_alert_dialog_positive_button) { _, _ ->
                    val intent = Intent(this@ImportContactsActivity, ContactListActivity::class.java)
                    intent.putExtra("fromStartActivity", true)
                    startActivity(intent)
                    val sharedPreferences: SharedPreferences = getSharedPreferences("Knockin_preferences", Context.MODE_PRIVATE)
                    val edit: SharedPreferences.Editor = sharedPreferences.edit()
                    edit.putBoolean("view", true)
                    edit.apply()
                    closeContextMenu()
                }
                .setNegativeButton(R.string.alert_dialog_cancel)
                { _, _ ->
                    closeContextMenu()
                }
                .show()
    }

    private fun getAppOnPhone(): java.util.ArrayList<String> {
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        val resolveInfoList = packageManager.queryIntentActivities(intent, 0)
        val packageNameList = java.util.ArrayList<String>()
        for (resolveInfo in resolveInfoList) {
            val activityInfo = resolveInfo.activityInfo
            packageNameList.add(activityInfo.applicationInfo.packageName)
        }
        return packageNameList
    }
}
