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
import android.widget.Toast

class AddNewContactActivity : AppCompatActivity() {

    private var add_new_contact_FirstName: TextView? = null
    private var add_new_contact_LastName: TextView? = null
    private var add_new_contact_PhoneNumber: TextView? = null
    private var add_new_contact_RoundedImageView: RoundedImageView? = null
    // Database && Thread
    private var main_ContactsDatabase: ContactsRoomDatabase? = null
    private lateinit var main_mDbWorkerThread: DbWorkerThread

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_contact)

        // on init WorkerThread
        main_mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        main_mDbWorkerThread.start()

        //on get la base de données
        main_ContactsDatabase = ContactsRoomDatabase.getDatabase(this)

        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar.setHomeAsUpIndicator(R.drawable.ic_left_arrow)
        actionbar.title = "Ajouter un nouveau contact"

        // Find View By Id
        add_new_contact_FirstName = findViewById(R.id.add_new_contact_first_name_id)
        add_new_contact_LastName = findViewById(R.id.add_new_contact_last_name_id)
        add_new_contact_PhoneNumber = findViewById(R.id.add_new_contact_phone_number_id)
        add_new_contact_RoundedImageView = findViewById(R.id.add_new_contact_rounded_image_view_id)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                if (add_new_contact_FirstName!!.text.toString().isEmpty() || add_new_contact_LastName!!.text.toString().isEmpty()) {
                    Toast.makeText(this, "Les champs nom et prénom ne peuvent pas être vide !", Toast.LENGTH_SHORT).show()
                } else {
                        val intent = Intent(this@AddNewContactActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                }
            }
            R.id.nav_validate -> if (add_new_contact_FirstName!!.text.toString().isEmpty() || add_new_contact_LastName!!.text.toString().isEmpty()) {
                Toast.makeText(this, "Les champs nom et prénom ne peuvent pas être vide !", Toast.LENGTH_SHORT).show()
            } else {
            val printContacts = Runnable {
                val contactData = Contacts(null,add_new_contact_FirstName!!.text.toString(),add_new_contact_LastName!!.text.toString(),add_new_contact_PhoneNumber!!.text.toString(), R.drawable.ryan,R.drawable.aquarius)
                println(contactData)
                main_ContactsDatabase?.contactsDao()?.insert(contactData)
                val intent = Intent(this@AddNewContactActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
                main_mDbWorkerThread.postTask(printContacts)
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
