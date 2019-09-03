package com.yellowtwigs.knocker.controller.activity.firstLaunch

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.yellowtwigs.knocker.R
import com.yellowtwigs.knocker.model.ContactManager
import com.yellowtwigs.knocker.model.DbWorkerThread
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Activité Qui Nous permet d'importer nos contact pour les buildVariant Contacter et Converser
 * @author Florian Striebel, Kenzy Suon
 */
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

        import_contacts_LongText!!.setText(String.format(getString(R.string.import_contacts_long_text),getString(R.string.app_name)))
        main_mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        main_mDbWorkerThread.start()


        //region Listener

        //Lors du click sur synchroniser alors nous demandons l'autorisation d'accéder aux contact
        import_contacts_accept_button!!.setOnClickListener {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), REQUEST_CODE_READ_CONTACT)
        }
        // Lors du click sur passer alors nous affichons une alert dialog pour nous prévenir de la possibilité de synchroniser plus tard
        import_contacts_not_accept_button!!.setOnClickListener {
            overlayAlertDialog()!!.show()
        }
        //endregion
    }
    /*
     *Méthode appellé par le système lorsque l'utilisateur a accepté ou refuser une demande de permission
     *
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_READ_CONTACT) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {//Si on a accès au contact on les synchronise sinon on affiche un message pour dire à l'utilisateur qu'il pourra synchroniser plus tard
                Toast.makeText(this, R.string.import_contacts_toast, Toast.LENGTH_LONG).show()
                import_contacts_accept_button!!.visibility = View.GONE
                import_contacts_not_accept_button!!.visibility = View.GONE
                import_contacts_LongText!!.visibility = View.GONE
                import_contacts_Title!!.visibility = View.GONE
                main_loadingPanel!!.visibility = View.VISIBLE
                val sync = Runnable {
                    ContactManager(this).getAllContacsInfoSync(contentResolver)
                    intentToTutorial()
                }
                main_mDbWorkerThread.postTask(sync)
            } else {
                overlayAlertDialog()!!.show()
            }
        }
    }
    /**
     *méthode qui lance  l'activity Tutorial
     */
    fun intentToTutorial() {
        val intent = Intent(this@ImportContactsActivity, TutorialActivity::class.java)
        intent.putExtra("fromImportContact", true)
        startActivity(intent)
        finish()
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
    override fun onBackPressed() {

    }


    companion object {
        const val REQUEST_CODE_READ_CONTACT = 2
    }
}
