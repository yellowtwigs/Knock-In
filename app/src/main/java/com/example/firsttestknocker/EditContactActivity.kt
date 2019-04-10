package com.example.firsttestknocker

import android.content.Intent
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.WindowManager
import android.widget.TextView

class EditContactActivity : AppCompatActivity() {

    private var edit_contact_FirstName: TextView? = null
    private var edit_contact_LastName: TextView? = null
    private var edit_contact_PhoneNumber: TextView? = null
    private var edit_contact_RoundedImageView: RoundedImageView? = null
    private var edit_contact_first_name: String? = null
    private var edit_contact_last_name: String? = null
    private var edit_contact_phone_number: String? = null
    private var edit_contact_rounded_image: Int = 0
    // Database && Thread
    private var main_ContactsDatabase: ContactsRoomDatabase? = null
    private lateinit var main_mDbWorkerThread: DbWorkerThread

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_contact)

        // on init WorkerThread
        main_mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        main_mDbWorkerThread.start()

        //on get la base de données
        main_ContactsDatabase = ContactsRoomDatabase.getDatabase(this)

        // Create the Intent, and get the data from the GridView
        val intent = intent
        edit_contact_first_name = intent.getStringExtra("ContactFirstName")
        edit_contact_last_name = intent.getStringExtra("ContactLastName")
        edit_contact_phone_number = intent.getStringExtra("ContactPhoneNumber")
        edit_contact_rounded_image = intent.getIntExtra("ContactImage", 1)

        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar.setHomeAsUpIndicator(R.drawable.ic_cross)
        actionbar.title = "Editer le contact"

        // Find View By Id
        edit_contact_FirstName = findViewById(R.id.edit_contact_first_name_id)
        edit_contact_LastName = findViewById(R.id.edit_contact_last_name_id)
        edit_contact_PhoneNumber = findViewById(R.id.edit_contact_phone_number_id)
        edit_contact_RoundedImageView = findViewById(R.id.edit_contact_rounded_image_view_id)

        // Set Resources from MainActivity to ContactDetailsActivity
        edit_contact_FirstName!!.text = edit_contact_first_name
        edit_contact_LastName!!.text = edit_contact_last_name
        edit_contact_PhoneNumber!!.text = edit_contact_phone_number
        edit_contact_RoundedImageView!!.setImageResource(edit_contact_rounded_image)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                // Update
                val editContact = Runnable {
                    val intent = Intent(this@EditContactActivity, ContactDetailsActivity::class.java)

                    intent.putExtra("ContactFirstName", edit_contact_first_name)
                    intent.putExtra("ContactLastName", edit_contact_last_name)
                    intent.putExtra("ContactPhoneNumber", edit_contact_phone_number)
                    intent.putExtra("ContactImage", edit_contact_rounded_image)

                    startActivity(intent)
                    finish()
                }
                main_mDbWorkerThread.postTask(editContact)
            }
            R.id.nav_validate -> {
                val intent = Intent(this@EditContactActivity, ContactDetailsActivity::class.java)
                intent.putExtra("ContactFirstName", edit_contact_first_name)
                intent.putExtra("ContactLastName", edit_contact_last_name)
                intent.putExtra("ContactPhoneNumber", edit_contact_phone_number)
                intent.putExtra("ContactImage", edit_contact_rounded_image)
                startActivity(intent)
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_add_contact, menu)
        return true
    }
}
