@file:Suppress("NAME_SHADOWING")
package com.example.firsttestknocker

import android.Manifest
import android.app.Activity
import android.content.Intent
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

class ContactDetailsActivity : AppCompatActivity() {

    private var contact_details_FirstName: TextView? = null
    private var contact_details_LastName: TextView? = null
    private var contact_details_PhoneNumber: TextView? = null
    private var contact_details_Mail: TextView? = null
    private var contact_details_RoundedImageView: RoundedImageView? = null
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

        // Find View By Id
        contact_details_FirstName = findViewById(R.id.contact_details_first_name_id)
        contact_details_LastName = findViewById(R.id.contact_details_last_name_id)
        contact_details_PhoneNumber = findViewById(R.id.contact_details_phone_number_text_id)
        contact_details_Mail = findViewById(R.id.contact_details_mail_id)
        contact_details_RoundedImageView = findViewById(R.id.contact_details_rounded_image_view_id)
        contactImage_BackgroundImage = findViewById(R.id.contact_details_background_image_id)

        contact_details_phone_number_RelativeLayout = findViewById(R.id.contact_details_phone_number_relative_layout_id)
        contact_details_messenger_RelativeLayout = findViewById(R.id.contact_details_messenger_relative_layout_id)
        contact_details_whatsapp_RelativeLayout = findViewById(R.id.contact_details_whatsapp_relative_layout_id)
        contact_details_instagram_RelativeLayout = findViewById(R.id.contact_details_instagram_relative_layout_id)
        contact_details_mail_RelativeLayout = findViewById(R.id.contact_details_mail_relative_layout_id)


        // Floating Button
        contact_details_FloatingButtonOpen = findViewById(R.id.contact_details_floating_button_open_id)
        contact_details_FloatingButtonEdit = findViewById(R.id.contact_details_floating_button_edit_id)
        contact_details_FloatingButtonDelete = findViewById(R.id.contact_details_floating_button_delete_id)
        contact_details_FloatingButtonOpenAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.fab_open)
        contact_details_FloatingButtonCloseAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.fab_close)
        contact_details_FloatingButtonClockWiserAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.rotate_clockwiser)
        contact_details_FloatingButtonAntiClockWiserAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.rotate_anticlockwiser)

        if (contact_details_phone_number != null) {
            contact_details_phone_number_RelativeLayout!!.visibility = View.VISIBLE
        }

        if (contact_details_mail != null) {
            contact_details_mail_RelativeLayout!!.visibility = View.VISIBLE
        }

        // Set Resources from MainActivity to ContactDetailsActivity
        contact_details_FirstName!!.text = contact_details_first_name
        contact_details_LastName!!.text = contact_details_last_name
        contact_details_PhoneNumber!!.text = contact_details_phone_number
        contact_details_RoundedImageView!!.setImageResource(contact_details_rounded_image)

        contact_details_FloatingButtonOpen!!.setOnClickListener {
            if (isOpen) {
                onFloatingClickBack()
                isOpen = false
            } else {
                onFloatingClick()
                isOpen = true
            }
        }

        contact_details_FloatingButtonEdit!!.setOnClickListener {

            val intent = Intent(this@ContactDetailsActivity, EditContactActivity::class.java)

            intent.putExtra("ContactFirstName", contact_details_first_name)
            intent.putExtra("ContactLastName", contact_details_last_name)
            intent.putExtra("ContactPhoneNumber", contact_details_phone_number)
            intent.putExtra("ContactImage", contact_details_rounded_image)
            intent.putExtra("ContactId", contact_details_id!!)

            startActivity(intent)
        }

        contact_details_FloatingButtonDelete!!.setOnClickListener {
            val deleteContact = Runnable {
                contact_details_ContactsDatabase?.contactsDao()?.deleteContactById(contact_details_id!!.toInt())
            }
            contact_details_mDbWorkerThread.postTask(deleteContact)
        }
    }

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


    fun onFloatingClickBack() {
        contact_details_FloatingButtonDelete!!.startAnimation(contact_details_FloatingButtonCloseAnimation)
        contact_details_FloatingButtonEdit!!.startAnimation(contact_details_FloatingButtonCloseAnimation)
        contact_details_FloatingButtonOpen!!.startAnimation(contact_details_FloatingButtonAntiClockWiserAnimation)

        contact_details_FloatingButtonEdit!!.isClickable = false
        contact_details_FloatingButtonDelete!!.isClickable = false
    }

    fun onFloatingClick() {
        contact_details_FloatingButtonDelete!!.startAnimation(contact_details_FloatingButtonOpenAnimation)
        contact_details_FloatingButtonEdit!!.startAnimation(contact_details_FloatingButtonOpenAnimation)
        contact_details_FloatingButtonOpen!!.startAnimation(contact_details_FloatingButtonClockWiserAnimation)

        contact_details_FloatingButtonEdit!!.isClickable = true
        contact_details_FloatingButtonDelete!!.isClickable = true
    }
}
