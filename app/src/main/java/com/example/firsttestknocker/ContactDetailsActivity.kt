@file:Suppress("NAME_SHADOWING")

package com.example.firsttestknocker

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
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

import java.io.File
import java.io.IOException
import java.net.URI
import java.text.SimpleDateFormat
import java.util.Date

import android.os.Environment.getExternalStoragePublicDirectory
import android.text.TextUtils
import android.util.Base64
import android.view.LayoutInflater
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import kotlinx.android.synthetic.main.alert_dialog_phone_number.view.*

class ContactDetailsActivity : AppCompatActivity() {

    private var contact_details_FirstName: TextView? = null
    private var contact_details_LastName: TextView? = null
    private var contact_details_PhoneNumber: TextView? = null
    private var contact_details_PhoneNumber_Property: TextView? = null
    private var contact_details_Mail_Property: TextView? = null
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
    private var contact_details_phone_property: String? = null
    private var contact_details_mail: String? = null
    private var contact_details_mail_property: String? = null
    private var contact_details_rounded_image: Int = 0
    private var contact_details_image64: String? = null
    private var contact_details_priority: Int = 1

    // Database && Thread
    private var contact_details_ContactsDatabase: ContactsRoomDatabase? = null
    private lateinit var contact_details_mDbWorkerThread: DbWorkerThread

    // Alert Dialog SMS & Phone Call
    private val SEND_SMS_PERMISSION_REQUEST_CODE = 111

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_details)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT)

        // on init WorkerThread
        contact_details_mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        contact_details_mDbWorkerThread.start()

        //on get la base de données
        contact_details_ContactsDatabase = ContactsRoomDatabase.getDatabase(this)

        // Contact's informations, link between Layout and the Activity
        contact_details_FirstName = findViewById(R.id.contact_details_first_name_id)
        contact_details_LastName = findViewById(R.id.contact_details_last_name_id)
        contact_details_PhoneNumber = findViewById(R.id.contact_details_phone_number_text_id)
        contact_details_PhoneNumber_Property = findViewById(R.id.contact_details_phone_property_text_id)
        contact_details_Mail = findViewById(R.id.contact_details_mail_id)
        contact_details_RoundedImageView = findViewById(R.id.contact_details_rounded_image_view_id)
        contactImage_BackgroundImage = findViewById(R.id.contact_details_background_image_id)
        contact_details_Mail_Property = findViewById(R.id.contact_details_mail_property_id)


        // Create the Intent, and get the data from the GridView
        val intent = intent
        contact_details_first_name = intent.getStringExtra("ContactFirstName")
        contact_details_last_name = intent.getStringExtra("ContactLastName")
        var tmp = intent.getStringExtra("ContactPhoneNumber")
        println("tmp " + tmp);
        contact_details_phone_number = NumberAndMailDB.numDBAndMailDBtoDisplay(tmp)
        contact_details_phone_property = NumberAndMailDB.extractStringFromNumber(tmp)
        tmp = intent.getStringExtra("ContactMail")
        contact_details_mail = NumberAndMailDB.numDBAndMailDBtoDisplay(tmp)
        contact_details_mail_property = NumberAndMailDB.extractStringFromNumber(tmp)
        contact_details_rounded_image = intent.getIntExtra("ContactImage", 1)
        contact_details_id = intent.getLongExtra("ContactId", 1)
        contact_details_priority = intent.getIntExtra("ContactPriority", 1)


        val getimage64 = Runnable {
            val id = contact_details_id
            val contact = contact_details_ContactsDatabase?.contactsDao()?.getContact(id!!.toInt())
            contact_details_image64 = contact!!.profilePicture64
            if (contact_details_image64 == "") {
                println(" contact detail ======= " + contact_details_rounded_image)
                contact_details_RoundedImageView!!.setImageResource(contact_details_rounded_image)
            } else {
                val image64 = contact_details_image64
                contact_details_RoundedImageView!!.setImageBitmap(base64ToBitmap(image64!!))
            }
        }
        contact_details_mDbWorkerThread.postTask(getimage64)

        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar.setHomeAsUpIndicator(R.drawable.ic_left_arrow)
        println("contact name = " + contact_details_first_name)
        println("contact last name = " + contact_details_last_name)
        println("contact image = " + contact_details_rounded_image)
        actionbar.setTitle("Détails du contact " + contact_details_first_name!!)

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
        contact_details_PhoneNumber_Property!!.text = contact_details_phone_property
        contact_details_Mail!!.text = contact_details_mail
        contact_details_Mail_Property!!.text = contact_details_mail_property

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
            ContactGesture.openWhatsapp(contact_details_PhoneNumber!!.text,this)
        }

        // Floating button, edit a contact
        contact_details_FloatingButtonEdit!!.setOnClickListener {

            val intent = Intent(this@ContactDetailsActivity, EditContactActivity::class.java)

            println("imaaage befor send to edit = " + contact_details_rounded_image)
            // Creation of a intent to transfer data's contact from ContactDetails to EditContact
            intent.putExtra("ContactFirstName", contact_details_first_name)
            intent.putExtra("ContactLastName", contact_details_last_name)
            intent.putExtra("ContactPhoneNumber", contact_details_phone_number + NumberAndMailDB.convertSpinnerStringToChar(contact_details_phone_property!!))
            intent.putExtra("ContactImage", contact_details_rounded_image)
            intent.putExtra("ContactId", contact_details_id!!)
            intent.putExtra("ContactMail", contact_details_mail!! + NumberAndMailDB.convertSpinnerStringToChar(contact_details_mail_property!!))
            intent.putExtra("ContactPriority", contact_details_priority)

            startActivity(intent)
        }

        contact_details_phone_number_RelativeLayout!!.setOnClickListener {
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.alert_dialog_phone_number, null)
            val mBuilder = AlertDialog.Builder(this)
                    .setView(mDialogView)
                    .setTitle("Phone Call or SMS")
            val mAlertDialog = mBuilder.show()

            mDialogView.alert_dialog_phone_call_id!!.setOnClickListener {
                if (!TextUtils.isEmpty(contact_details_phone_number)) {
                    val dial = "tel:$contact_details_phone_number"
                    startActivity(Intent(Intent.ACTION_DIAL, Uri.parse(dial)))
                    mBuilder.setCancelable(true)
                } else {
                    Toast.makeText(this@ContactDetailsActivity, "Enter a phone number", Toast.LENGTH_SHORT).show()
                }
            }

            mDialogView.alert_dialog_sms_id!!.setOnClickListener {
                val intent = Intent(this@ContactDetailsActivity, ComposeMessageActivity::class.java)

                println("image befor send to edit = " + contact_details_rounded_image)
                // Creation of a intent to transfer data's contact from ContactDetails to EditContact
                intent.putExtra("ContactFirstName", contact_details_first_name)
                intent.putExtra("ContactLastName", contact_details_last_name)
                intent.putExtra("ContactPhoneNumber", contact_details_phone_number + NumberAndMailDB.convertSpinnerStringToChar(contact_details_phone_property!!))
                intent.putExtra("ContactImage", contact_details_rounded_image)
                intent.putExtra("ContactId", contact_details_id!!)
                intent.putExtra("ContactMail", contact_details_mail!! + NumberAndMailDB.convertSpinnerStringToChar(contact_details_mail_property!!))
                intent.putExtra("ContactPriority", contact_details_priority)

                startActivity(intent)

                mBuilder.setCancelable(true)
            }
        }

        // Floating button, detete a contact
        contact_details_FloatingButtonDelete!!.setOnClickListener {
            //crée une pop up de confirmation avant de supprimer un contact
            val builder = AlertDialog.Builder(this)
            println("ouioiuioiuioiuioiuiou + =" + contact_details_image64)
            builder.setTitle("SUPPRIMER CONTACT")
            builder.setMessage("Voulez vous vraiment supprimer ce contact ?")
            builder.setPositiveButton("OUI") { _, _ ->
                val deleteContact = Runnable {
                    contact_details_ContactsDatabase?.contactsDao()?.deleteContactById(contact_details_id!!.toInt())

                    val intent = Intent(this@ContactDetailsActivity, MainActivity::class.java)
                    intent.putExtra("isDelete", true);
                    startActivity(intent)
                }
                contact_details_mDbWorkerThread.postTask(deleteContact)
            }
            builder.setNegativeButton("NON") { _, _ ->
                //annule la suppression
            }
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }
    }

    fun base64ToBitmap(base64: String): Bitmap {
        val imageBytes = Base64.decode(base64, 0)
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
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
 /*   fun onWhatsappClick(contact: CharSequence) {
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

    }*/
}
