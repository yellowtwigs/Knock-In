package com.example.firsttestknocker

import android.content.Intent
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.WindowManager
import android.widget.TextView
import android.text.Editable
import android.text.TextWatcher
import android.widget.ImageView
import android.widget.Toast


class EditContactActivity : AppCompatActivity() {

    private var edit_contact_FirstName: TextView? = null
    private var edit_contact_LastName: TextView? = null
    private var edit_contact_PhoneNumber: TextView? = null
    private var edit_contact_Mail: TextView? = null
    private var edit_contact_RoundedImageView: ImageView? = null

    private var edit_contact_id: Long? = null
    private var edit_contact_first_name: String? = null
    private var edit_contact_last_name: String? = null
    private var edit_contact_phone_number: String? = null
    private var edit_contact_mail: String? = null
    private var edit_contact_rounded_image: Int = 0
    // Database && Thread
    private var edit_contact_ContactsDatabase: ContactsRoomDatabase? = null
    private lateinit var edit_contact_mDbWorkerThread: DbWorkerThread

    private var isChanged = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_contact)

        // on init WorkerThread
        edit_contact_mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        edit_contact_mDbWorkerThread.start()

        //on get la base de données
        edit_contact_ContactsDatabase = ContactsRoomDatabase.getDatabase(this)

        // Create the Intent, and get the data from the GridView
        val intent = intent
//        val idContact =
        edit_contact_id = intent.getLongExtra("ContactId", 1)
        edit_contact_first_name = intent.getStringExtra("ContactFirstName")
        edit_contact_last_name = intent.getStringExtra("ContactLastName")
        edit_contact_phone_number = intent.getStringExtra("ContactPhoneNumber")
        edit_contact_mail = intent.getStringExtra("ContactMail")
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
        edit_contact_Mail = findViewById(R.id.edit_contact_mail_id)

        // Set Resources from MainActivity to ContactDetailsActivity
        edit_contact_FirstName!!.text = edit_contact_first_name
        edit_contact_LastName!!.text = edit_contact_last_name
        edit_contact_PhoneNumber!!.text = edit_contact_phone_number
        edit_contact_Mail!!.text = edit_contact_mail
        edit_contact_RoundedImageView!!.setImageResource(edit_contact_rounded_image)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        textChanged(edit_contact_FirstName, edit_contact_first_name)
        textChanged(edit_contact_LastName, edit_contact_last_name)
        textChanged(edit_contact_PhoneNumber, edit_contact_phone_number)
        textChanged(edit_contact_Mail, edit_contact_mail)

//        val customDialogClass = CustomDialogClass(this@EditContactActivity)
////                    customDialogClass.setText("Attention, vous risquez de perdre toutes vos modifications, voulez vous vraiment continuer ?")
//        customDialogClass.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (isChanged) {
                    var alertDialog = AlertDialog.Builder(this)
                    alertDialog.setTitle("Attention")
                    alertDialog.setMessage("Vous risquez de perdre toutes vos modifications, voulez vous vraiment continuer ?")

                    alertDialog.setPositiveButton("Oui", { _, _ ->

                        val intent = Intent(this@EditContactActivity, ContactDetailsActivity::class.java)
                        intent.putExtra("ContactFirstName", edit_contact_first_name)
                        intent.putExtra("ContactLastName", edit_contact_last_name)
                        intent.putExtra("ContactPhoneNumber", edit_contact_phone_number)
                        intent.putExtra("ContactImage", edit_contact_rounded_image)
                        intent.putExtra("ContactMail", edit_contact_mail)

                        startActivity(intent)
                        finish()
                    })

                    alertDialog.setNegativeButton("Non", { _, _ ->
                    })
                    alertDialog.show()
                } else {
                    val intent = Intent(this@EditContactActivity, ContactDetailsActivity::class.java)
                    intent.putExtra("ContactFirstName", edit_contact_first_name)
                    intent.putExtra("ContactLastName", edit_contact_last_name)
                    intent.putExtra("ContactPhoneNumber", edit_contact_phone_number)
                    intent.putExtra("ContactImage", edit_contact_rounded_image)
                    intent.putExtra("ContactMail", edit_contact_mail)

                    startActivity(intent)
                    finish()
                }
            }
            R.id.nav_validate -> {
                val editContact = Runnable {
                    edit_contact_ContactsDatabase?.contactsDao()?.updateContactById(edit_contact_id!!.toInt(), edit_contact_FirstName!!.text.toString(), edit_contact_LastName!!.text.toString(), edit_contact_PhoneNumber!!.text.toString(), "", edit_contact_rounded_image) //edit contact rounded maybe not work
                    val intent = Intent(this@EditContactActivity, ContactDetailsActivity::class.java)

                    intent.putExtra("ContactFirstName", edit_contact_FirstName!!.text)
                    intent.putExtra("ContactLastName", edit_contact_LastName!!.text)
                    intent.putExtra("ContactPhoneNumber", edit_contact_PhoneNumber!!.text)
                    intent.putExtra("ContactImage", edit_contact_RoundedImageView!!.id)
                    intent.putExtra("ContactMail", edit_contact_Mail!!.text)

                    startActivity(intent)
                    finish()
                }
                edit_contact_mDbWorkerThread.postTask(editContact)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_add_contact, menu)
        return true
    }

    fun textChanged(textView : TextView?, txt : String?)
    {
        textView!!.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
                isChanged = textView.text != txt
            }

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {

            }
        })
    }
}
