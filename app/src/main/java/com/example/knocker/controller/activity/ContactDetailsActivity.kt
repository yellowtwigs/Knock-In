package com.example.knocker.controller.activity

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.knocker.R
import com.example.knocker.controller.CircularImageView
import com.example.knocker.controller.GroupEditAdapter
import com.example.knocker.controller.activity.group.GroupActivity
import com.example.knocker.controller.activity.group.GroupManagerActivity
import com.example.knocker.model.*
import com.example.knocker.model.ModelDB.ContactDetailDB
import com.example.knocker.model.ModelDB.ContactWithAllInformation
import com.example.knocker.model.ModelDB.GroupDB
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import java.io.ByteArrayOutputStream
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * La Classe qui permet d'éditer un contact choisi
 * @author Florian Striebel, Kenzy Suon
 */
class ContactDetailsActivity : AppCompatActivity() {

    //region ========================================== Var or Val ==========================================

    private var gestionnaireContacts: ContactList? = null

    private var contact_details_RoundedImageView: CircularImageView? = null
    private var contact_details_ContactName: TextView? = null
    private var contact_details_PhoneNumber: TextView? = null
    private var contact_details_FixNumber: TextView? = null
    private var contact_details_Mail: TextView? = null

    private var contact_details_PhoneCall: RelativeLayout? = null
    private var contact_details_SendSMS: RelativeLayout? = null
    private var contact_details_Whatsapp: RelativeLayout? = null
    private var contact_details_SendMail: RelativeLayout? = null

    private var contact_details_RecyclerGroup: RecyclerView? = null

    private var contact_details_Return: AppCompatImageView? = null
    private var contact_details_DeleteContact: AppCompatImageView? = null
    private var contact_details_AddContactToFavorite: AppCompatImageView? = null
    private var contact_details_RemoveContactFromFavorite: AppCompatImageView? = null
    private var contact_details_EditContact: AppCompatImageView? = null

    private var contact_details_ContactId: Int? = null
    private var contact_details_first_name: String = ""
    private var contact_details_last_name: String = ""
    private var contact_details_phone_number: String = ""
    private var contact_details_fix_number: String = ""
    private var contact_details_mail: String = ""
    private var contact_details_rounded_image: Int = 0
    private var contact_details_image64: String = ""

    // Database && Thread
    private var contact_details_ContactsDatabase: ContactsRoomDatabase? = null
    private lateinit var contact_details_mDbWorkerThread: DbWorkerThread

    private var havePhone: Boolean = false
    private var haveSecondPhone: Boolean = false
    private var haveMail: Boolean = false

    private var fromGroupActivity = false

    private var isFavorite: Boolean? = null

    //endregion

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //region ======================================== Theme Dark ========================================

        val sharedThemePreferences = getSharedPreferences("Knocker_Theme", Context.MODE_PRIVATE)
        if (sharedThemePreferences.getBoolean("darkTheme", false)) {
            setTheme(R.style.AppThemeDark)
        } else {
            setTheme(R.style.AppTheme)
        }

        //endregion

        val sharedFavoritePreferences = getSharedPreferences("Knocker_Contact_Favorite", Context.MODE_PRIVATE)
        isFavorite = sharedFavoritePreferences.getBoolean("contactFavorite", false)

        setContentView(R.layout.activity_contact_details)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT

        //region ==================================== WorkerThread / BDD ====================================

        // on init WorkerThread
        contact_details_mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        contact_details_mDbWorkerThread.start()


        //on get la base de données
        contact_details_ContactsDatabase = ContactsRoomDatabase.getDatabase(this)

        gestionnaireContacts = ContactList(this.applicationContext)

        //endregion

        //region ======================================= FindViewById =======================================

        contact_details_ContactName = findViewById(R.id.contact_details_contact_name)
//        contact_details_PhoneNumber = findViewById(R.id.edit_contact_phone_number_id)
//        contact_details_FixNumber = findViewById(R.id.edit_contact_phone_number_fix_id)
        contact_details_RoundedImageView = findViewById(R.id.contact_details_rounded_image_view)
        contact_details_Mail = findViewById(R.id.edit_contact_mail_id)
        contact_details_RecyclerGroup = findViewById(R.id.contact_details_recycler_group)

        //endregion

        //region ========================================= Toolbar ==========================================

        contact_details_Return = findViewById(R.id.contact_details_return)
        contact_details_DeleteContact = findViewById(R.id.contact_details_delete)
        contact_details_AddContactToFavorite = findViewById(R.id.contact_details_favorite)
        contact_details_RemoveContactFromFavorite = findViewById(R.id.contact_details_favorite_shine)
        contact_details_EditContact = findViewById(R.id.contact_details_edit_contact)

        //endregion

        //region ==================================== Intent / Set Text =====================================

        val intent = intent
        contact_details_ContactId = intent.getIntExtra("ContactId", 1)
        fromGroupActivity = intent.getBooleanExtra("fromGroupActivity", false)


        //TODO wash the code
        if (contact_details_ContactsDatabase?.contactsDao()?.getContact(contact_details_ContactId!!.toInt()) == null) {

            val contactList = ContactList(this)
            val contact = contactList.getContactById(contact_details_ContactId!!)!!
            contact_details_first_name = contact.contactDB!!.firstName
            contact_details_last_name = contact.contactDB!!.lastName
            val tmpPhone = contact.contactDetailList!![0]
            contact_details_phone_number = tmpPhone.content
            val tmpMail = contact.contactDetailList!![1]
            contact_details_mail = tmpMail.content
            contact_details_image64 = contact.contactDB!!.profilePicture64
            contact_details_RoundedImageView!!.setImageBitmap(base64ToBitmap(contact_details_image64))
        } else {

            val executorService: ExecutorService = Executors.newFixedThreadPool(1)
            val callDb = Callable { contact_details_ContactsDatabase!!.contactsDao().getContact(contact_details_ContactId!!) }
            val result = executorService.submit(callDb)
            val contact: ContactWithAllInformation = result.get()
            contact_details_first_name = contact.contactDB!!.firstName
            contact_details_last_name = contact.contactDB!!.lastName
            contact_details_rounded_image = gestionnaireContacts!!.randomDefaultImage(contact.contactDB!!.profilePicture, "Get")
            //TODO :enlever code Dupliquer

            contact_details_fix_number = getString(R.string.edit_contact_phone_number_home)
            contact_details_phone_number = ""
            contact_details_mail = ""

            val phoneNumber = contact.getFirstPhoneNumber()
            contact_details_phone_number = phoneNumber
            val fixNumber = contact.getSecondPhoneNumber(phoneNumber)
            contact_details_fix_number = fixNumber
            val mail = contact.getFirstMail()
            contact_details_mail = mail

            println("fix number egale à $fixNumber")
            if (phoneNumber != "") {
                havePhone = true
            }
            if (fixNumber != "") {
                haveSecondPhone = true
            }
            if (mail != "") { ///// havemail toujour false
                haveMail = true
            }

            val id = contact_details_ContactId
            val contactDB = contact_details_ContactsDatabase?.contactsDao()?.getContact(id!!.toInt())
            contact_details_image64 = contactDB!!.contactDB!!.profilePicture64
            if (contact_details_image64 == "") {
                contact_details_RoundedImageView!!.setImageResource(contact_details_rounded_image)
            } else {
                val image64 = contact_details_image64
                contact_details_RoundedImageView!!.setImageBitmap(base64ToBitmap(image64))
            }
        }

        contact_details_ContactName!!.text = "$contact_details_first_name $contact_details_last_name"
//        contact_details_PhoneNumber!!.text = contact_details_phone_number
//        contact_details_FixNumber!!.text = contact_details_fix_number
//        contact_details_Mail!!.text = contact_details_mail

        //endregion

        //region ========================================= Groups ===========================================

        val layoutMananger = LinearLayoutManager(applicationContext, LinearLayoutManager.HORIZONTAL, false)
        contact_details_RecyclerGroup!!.layoutManager = layoutMananger
        val executorService: ExecutorService = Executors.newFixedThreadPool(1)
        val callDbGroup = Callable { contact_details_ContactsDatabase!!.GroupsDao().getGroupForContact(contact_details_ContactId!!) }
        val resultGroup = executorService.submit(callDbGroup)
        val listGroup: ArrayList<GroupDB> = ArrayList()
        listGroup.addAll(resultGroup.get())
        val callDBContact = Callable { contact_details_ContactsDatabase!!.contactsDao().getContact(contact_details_ContactId!!) }
        val resultContact = executorService.submit(callDBContact)

        val adapter = GroupEditAdapter(this, listGroup, resultContact.get())
        contact_details_RecyclerGroup!!.adapter = adapter

        //endregion

        //region ======================================== Listeners =========================================

        contact_details_Return!!.setOnClickListener {
            backOnPressed()
        }

        contact_details_DeleteContact!!.setOnClickListener {
            deleteContact()
        }

        contact_details_AddContactToFavorite!!.setOnClickListener {
            contact_details_AddContactToFavorite!!.visibility = View.INVISIBLE
            contact_details_RemoveContactFromFavorite!!.visibility = View.VISIBLE

            addToFavorite()
        }

        contact_details_RemoveContactFromFavorite!!.setOnClickListener {
            contact_details_RemoveContactFromFavorite!!.visibility = View.INVISIBLE
            contact_details_AddContactToFavorite!!.visibility = View.VISIBLE

            removeFromFavorite()
        }

        contact_details_EditContact!!.setOnClickListener {
            startActivity(Intent(this@ContactDetailsActivity, EditContactActivity::class.java))
        }

        //endregion

    }

    //region ========================================== Functions ===========================================

    private fun deleteContact() {
        contact_details_ContactsDatabase!!.contactsDao().deleteContactById(contact_details_ContactId!!)
        val mainIntent = Intent(this@ContactDetailsActivity, MainActivity::class.java)
        mainIntent.putExtra("isDelete", true)
        startActivity(mainIntent)
        finish()
    }

    private fun backOnPressed() {
        if (!fromGroupActivity) {
            startActivity(Intent(this@ContactDetailsActivity, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
            finish()
        } else {
            startActivity(Intent(this@ContactDetailsActivity, GroupManagerActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
            finish()
        }
    }

    private fun addToFavorite() {
        val sharedFavoritePreferences = getSharedPreferences("Knocker_Contact_Favorite", Context.MODE_PRIVATE)
        val edit: SharedPreferences.Editor = sharedFavoritePreferences.edit()
        edit.putBoolean("contactFavorite", true)
        edit.apply()
    }

    private fun removeFromFavorite() {
        val sharedFavoritePreferences = getSharedPreferences("Knocker_Contact_Favorite", Context.MODE_PRIVATE)
        val edit: SharedPreferences.Editor = sharedFavoritePreferences.edit()
        edit.putBoolean("contactFavorite", false)
        edit.apply()
    }


    private fun base64ToBitmap(base64: String): Bitmap {
        val imageBytes = Base64.decode(base64, 0)
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    override fun onBackPressed() {}

    //endregion
}
