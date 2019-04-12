@file:Suppress("NAME_SHADOWING")
package com.example.firsttestknocker

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.content.FileProvider
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView

import java.io.File
import java.io.IOException
import java.net.URI
import java.text.SimpleDateFormat
import java.util.Date

import android.os.Environment.getExternalStoragePublicDirectory
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast

class ContactDetailsActivity : AppCompatActivity() {

    private var contact_details_FirstName: TextView? = null
    private var contact_details_LastName: TextView? = null
    private var contact_details_PhoneNumber: TextView? = null
    private var contact_details_Mail: TextView? = null
    private var contact_details_RoundedImageView: ImageView? = null
    private var contactImage_BackgroundImage: ImageView? = null

    private var contact_details_FloatingButtonOpen: FloatingActionButton? = null
    private var contact_details_FloatingButtonEdit: FloatingActionButton? = null
    private var contact_details_FloatingButtonDelete: FloatingActionButton? = null

    private var contact_details_FloatingButtonOpenAnimation: Animation? = null
    private var contact_details_FloatingButtonCloseAnimation: Animation? = null
    private var contact_details_FloatingButtonClockWiserAnimation: Animation? = null
    private var contact_details_FloatingButtonAntiClockWiserAnimation: Animation? = null

    internal var isOpen = false

    private var contact_details_phone_number_RelativeLayout: RelativeLayout? = null
    private var contact_details_mail_RelativeLayout: RelativeLayout? = null
    private var contact_details_messenger_RelativeLayout: RelativeLayout? = null
    private var contact_details_whatsapp_RelativeLayout: RelativeLayout? = null
    private var contact_details_instagram_RelativeLayout: RelativeLayout? = null

    private var contact_details_id: Long? = null
    private var contact_details_first_name: String? = null
    private var contact_details_last_name: String? = null
    private var contact_details_phone_number: String? = null
    private var contact_details_mail: String? = null
    private var contact_details_rounded_image: Int = 0
    // Database && Thread
    private var contact_details_ContactsDatabase: ContactsRoomDatabase? = null
    private lateinit var contact_details_mDbWorkerThread: DbWorkerThread

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_details)

        // Create the Intent, and get the data from the GridView
        val intent = intent
        contact_details_first_name = intent.getStringExtra("ContactFirstName")
        contact_details_last_name = intent.getStringExtra("ContactLastName")
        contact_details_phone_number = intent.getStringExtra("ContactPhoneNumber")
        contact_details_mail = intent.getStringExtra("ContactMail")
        contact_details_rounded_image = intent.getIntExtra("ContactImage", 1)
        contact_details_id = intent.getLongExtra("ContactId", 1)

        // on init WorkerThread
        contact_details_mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        contact_details_mDbWorkerThread.start()

        //on get la base de données
        contact_details_ContactsDatabase = ContactsRoomDatabase.getDatabase(this)

        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar.setHomeAsUpIndicator(R.drawable.ic_left_arrow)
        actionbar.setTitle("Détails du contact " + contact_details_first_name!!)

        // Contact's informations, link between Layout and the Activity
        contact_details_FirstName = findViewById(R.id.contact_details_first_name_id)
        contact_details_LastName = findViewById(R.id.contact_details_last_name_id)
        contact_details_PhoneNumber = findViewById(R.id.contact_details_phone_number_text_id)
        contact_details_Mail = findViewById(R.id.contact_details_mail_id)
        contact_details_RoundedImageView = findViewById(R.id.contact_details_rounded_image_view_id)
        contactImage_BackgroundImage = findViewById(R.id.contact_details_background_image_id)

        // RelativeLayout to link with other SM's apps, link between Layout and the Activity
        contact_details_phone_number_RelativeLayout = findViewById(R.id.contact_details_phone_number_relative_layout_id)
        contact_details_messenger_RelativeLayout = findViewById(R.id.contact_details_messenger_relative_layout_id)
        contact_details_whatsapp_RelativeLayout = findViewById(R.id.contact_details_whatsapp_relative_layout_id)
        contact_details_instagram_RelativeLayout = findViewById(R.id.contact_details_instagram_relative_layout_id)
        contact_details_mail_RelativeLayout = findViewById(R.id.contact_details_mail_relative_layout_id)

        // Floating Button link between Layout and the Activity
        contact_details_FloatingButtonOpen = findViewById(R.id.contact_details_floating_button_open_id)
        contact_details_FloatingButtonEdit = findViewById(R.id.contact_details_floating_button_edit_id)
        contact_details_FloatingButtonDelete = findViewById(R.id.contact_details_floating_button_delete_id)
        contact_details_FloatingButtonOpenAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.fab_open)
        contact_details_FloatingButtonCloseAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.fab_close)
        contact_details_FloatingButtonClockWiserAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.rotate_clockwiser)
        contact_details_FloatingButtonAntiClockWiserAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.rotate_anticlockwiser)


        if (!contact_details_phone_number.isNullOrEmpty()) {
            contact_details_phone_number_RelativeLayout!!.visibility = View.VISIBLE
            contact_details_whatsapp_RelativeLayout!!.visibility = View.VISIBLE
        }
        if (!contact_details_mail.isNullOrEmpty()) {
            contact_details_mail_RelativeLayout!!.visibility = View.VISIBLE
        }

        // Set Resources from MainActivity to ContactDetailsActivity
        contact_details_FirstName!!.text = contact_details_first_name
        contact_details_LastName!!.text = contact_details_last_name
        contact_details_PhoneNumber!!.text = contact_details_phone_number
        contact_details_RoundedImageView!!.setImageResource(contact_details_rounded_image)
        contact_details_Mail!!.text = contact_details_mail

        // The click for the animation
        contact_details_FloatingButtonOpen!!.setOnClickListener {
            if (isOpen) {
                onFloatingClickBack()
                isOpen = false
            } else {
                onFloatingClick()
                isOpen = true
            }
        }

        // Link to Whatsapp contact chat
        contact_details_whatsapp_RelativeLayout!!.setOnClickListener {
            onWhatsappClick(contact_details_PhoneNumber!!.text)
        }

        // Floating button, edit a contact
        contact_details_FloatingButtonEdit!!.setOnClickListener {

            val intent = Intent(this@ContactDetailsActivity, EditContactActivity::class.java)

            // Creation of a intent to transfer data's contact from ContactDetails to EditContact
            intent.putExtra("ContactFirstName", contact_details_first_name)
            intent.putExtra("ContactLastName", contact_details_last_name)
            intent.putExtra("ContactPhoneNumber", contact_details_phone_number)
            intent.putExtra("ContactImage", contact_details_rounded_image)
            intent.putExtra("ContactId", contact_details_id!!)
            intent.putExtra("ContactMail", contact_details_mail!!)

            startActivity(intent)
        }

        // Floating button, detete a contact
        contact_details_FloatingButtonDelete!!.setOnClickListener {
            //crée une pop up de confirmation avant de supprimer un contact
            val builder = AlertDialog.Builder(this)
            builder.setTitle("SUPPRIMER CONTACT")
            builder.setMessage("Voulez vous vraiment supprimer ce contact ?")
            builder.setPositiveButton("OUI") { dialog, which ->
                val deleteContact = Runnable {
                    contact_details_ContactsDatabase?.contactsDao()?.deleteContactById(contact_details_id!!.toInt())

                    val intent = Intent(this@ContactDetailsActivity, MainActivity::class.java)
                    intent.putExtra("isDelete", true);
                    startActivity(intent)
                }
                contact_details_mDbWorkerThread.postTask(deleteContact)
            }
            builder.setNegativeButton("NON") { dialog, which ->
            //annule la suppression
            }
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }
    }

    // Intent to return to the MainActivity
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                val loginIntent = Intent(this@ContactDetailsActivity, MainActivity::class.java)
                startActivity(loginIntent)
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // Animation for the Floating Button
    fun onFloatingClickBack() {
        contact_details_FloatingButtonDelete!!.startAnimation(contact_details_FloatingButtonCloseAnimation)
        contact_details_FloatingButtonEdit!!.startAnimation(contact_details_FloatingButtonCloseAnimation)
        contact_details_FloatingButtonOpen!!.startAnimation(contact_details_FloatingButtonAntiClockWiserAnimation)

        contact_details_FloatingButtonEdit!!.isClickable = false
        contact_details_FloatingButtonDelete!!.isClickable = false
    }

    // Animation for the Floating Button
    fun onFloatingClick() {
        contact_details_FloatingButtonDelete!!.startAnimation(contact_details_FloatingButtonOpenAnimation)
        contact_details_FloatingButtonEdit!!.startAnimation(contact_details_FloatingButtonOpenAnimation)
        contact_details_FloatingButtonOpen!!.startAnimation(contact_details_FloatingButtonClockWiserAnimation)

        contact_details_FloatingButtonEdit!!.isClickable = true
        contact_details_FloatingButtonDelete!!.isClickable = true
    }

    // Link to Whatsapp
    fun onWhatsappClick(contact : CharSequence){
        val url = "https://api.whatsapp.com/send?phone=$contact"
        try {
            val pm = this.getPackageManager()
            pm.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES)
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        } catch (e: PackageManager.NameNotFoundException) {
            Toast.makeText(this, "Whatsapp app not installed in your phone", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }

    }
}
