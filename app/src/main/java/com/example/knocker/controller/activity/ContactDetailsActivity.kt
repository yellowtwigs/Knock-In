package com.example.knocker.controller.activity

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.*
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
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
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
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.knocker.R
import com.example.knocker.controller.CircularImageView
import com.example.knocker.controller.GroupEditAdapter
import com.example.knocker.controller.activity.group.AddContactToGroupAdapter
import com.example.knocker.controller.activity.group.GroupActivity
import com.example.knocker.controller.activity.group.GroupManagerActivity
import com.example.knocker.model.*
import com.example.knocker.model.ModelDB.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.activity_contact_details.*
import java.io.ByteArrayOutputStream
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.collections.ArrayList

/**
 * La Classe qui permet d'éditer un contact choisi
 * @author Florian Striebel, Kenzy Suon
 */
class ContactDetailsActivity : AppCompatActivity() {

    //region ========================================== Var or Val ==========================================

    private var gestionnaireContacts: ContactList? = null

    private var contact_details_RoundedImageView: CircularImageView? = null
    private var contact_details_ContactName: TextView? = null
    private var contact_details_Mail: TextView? = null

    private var contact_details_PhoneCall: RelativeLayout? = null
    private var contact_details_SendSMS: RelativeLayout? = null
    private var contact_details_Whatsapp: RelativeLayout? = null
    private var contact_details_SendMail: RelativeLayout? = null

    private var contact_details_RecyclerGroup: RecyclerView? = null

    private var contact_details_PhoneNumberLayout: RelativeLayout? = null
    private var contact_details_PhoneNumber_text: TextView? = null

    private var contact_details_FixNumberLayout: RelativeLayout? = null
    private var contact_details_FixNumber_text: TextView? = null

    private var contact_details_EmailLayout: RelativeLayout? = null
    private var contact_details_Email_text: TextView? = null

    private var contact_details_VocalCallWhatsappLayout: RelativeLayout? = null
    private var contact_details_VocalCallWhatsapp_text: TextView? = null

    private var contact_details_VideoCallWhatsappLayout: RelativeLayout? = null
    private var contact_details_VideoCallWhatsapp_text: TextView? = null

    private var contact_details_MessageWhatsappLayout: RelativeLayout? = null
    private var contact_details_MessageWhatsapp_text: TextView? = null

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

    private var groupId: Long = 0

    private var listContact: ArrayList<ContactDB?> = ArrayList()

    private var contact: ContactWithAllInformation? = null

    private val MAKE_CALL_PERMISSION_REQUEST_CODE = 1

    private var numberForPermission = ""

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
        contact_details_RoundedImageView = findViewById(R.id.contact_details_rounded_image_view)
        contact_details_Mail = findViewById(R.id.edit_contact_mail_id)

        contact_details_PhoneCall = findViewById(R.id.contact_details_phone_call_layout)
        contact_details_SendSMS = findViewById(R.id.contact_details_sms_layout)
        contact_details_Whatsapp = findViewById(R.id.contact_details_whatsapp_layout)
        contact_details_SendMail = findViewById(R.id.contact_details_mail_layout)

//        contact_details_PhoneNumberLayout = findViewById(R.id.edit_contact_phone_number_id)
//        contact_details_FixNumber = findViewById(R.id.edit_contact_phone_number_fix_id)

        contact_details_PhoneNumberLayout = findViewById(R.id.contact_details_phone_number_layout)
        contact_details_FixNumberLayout = findViewById(R.id.contact_details_phone_number_fix_layout)
        contact_details_EmailLayout = findViewById(R.id.contact_details_mail_relative_layout)
        contact_details_VocalCallWhatsappLayout = findViewById(R.id.contact_details_vocal_call_whatsapp_relative_layout)
        contact_details_VideoCallWhatsappLayout = findViewById(R.id.contact_details_video_call_whatsapp_relative_layout)
        contact_details_MessageWhatsappLayout = findViewById(R.id.contact_details_message_whatsapp_relative_layout)


        contact_details_PhoneNumber_text = findViewById(R.id.contact_details_phone_number)
        contact_details_FixNumber_text = findViewById(R.id.contact_details_phone_number_fix)
        contact_details_Email_text = findViewById(R.id.contact_details_mail_relative_layout_mail)
        contact_details_VideoCallWhatsapp_text = findViewById(R.id.contact_details_video_call_whatsapp_relative_layout_phone_number)
        contact_details_VocalCallWhatsapp_text = findViewById(R.id.contact_details_vocal_call_whatsapp_relative_layout_phone_number)
        contact_details_MessageWhatsapp_text = findViewById(R.id.contact_details_message_whatsapp_relative_layout_phone_number)

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
            contact = contactList.getContactById(contact_details_ContactId!!)!!
            contact_details_first_name = contact!!.contactDB!!.firstName
            contact_details_last_name = contact!!.contactDB!!.lastName
            val tmpPhone = contact!!.contactDetailList!![0]
            contact_details_phone_number = tmpPhone.content
            val tmpMail = contact!!.contactDetailList!![1]
            contact_details_mail = tmpMail.content
            contact_details_image64 = contact!!.contactDB!!.profilePicture64
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
            if (mail != "") {
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

        //region ========================================= Others ===========================================

        val contactList = ContactList(this)
        val contact = contactList.getContactById(contact_details_ContactId!!)!!

        if (contact.contactDB!!.favorite == 1) {
            contact_details_RemoveContactFromFavorite!!.visibility = View.VISIBLE
            contact_details_AddContactToFavorite!!.visibility = View.INVISIBLE
        } else if (contact.contactDB!!.favorite == 0) {
            contact_details_AddContactToFavorite!!.visibility = View.VISIBLE
            contact_details_RemoveContactFromFavorite!!.visibility = View.INVISIBLE
        }

        if (contact_details_phone_number != "") {
            contact_details_PhoneCall!!.visibility = View.VISIBLE
            contact_details_SendSMS!!.visibility = View.VISIBLE
            contact_details_PhoneNumberLayout!!.visibility = View.VISIBLE

            contact_details_PhoneNumber_text!!.text = contact_details_phone_number
        }

        if (contact_details_fix_number != "") {
            contact_details_PhoneCall!!.visibility = View.VISIBLE
            contact_details_FixNumberLayout!!.visibility = View.VISIBLE

            contact_details_FixNumber_text!!.text = contact_details_fix_number
        }


//        if (contact_details_phone_number != "" && appIsInstalled()) {
        if (contact_details_phone_number != "") {
            contact_details_Whatsapp!!.visibility = View.VISIBLE
            contact_details_VideoCallWhatsappLayout!!.visibility = View.VISIBLE
            contact_details_VocalCallWhatsappLayout!!.visibility = View.VISIBLE
            contact_details_MessageWhatsappLayout!!.visibility = View.VISIBLE

            contact_details_VideoCallWhatsapp_text!!.text = "Appel vocal du " + converter06To33(contact_details_phone_number)
            contact_details_VocalCallWhatsapp_text!!.text = "Appel vidéo du " + converter06To33(contact_details_phone_number)
            contact_details_MessageWhatsapp_text!!.text = "Message au " + converter06To33(contact_details_phone_number)
        }

        if (contact_details_phone_number == "" && contact_details_fix_number != "") {
            contact_details_PhoneCall!!.visibility = View.VISIBLE
            contact_details_SendSMS!!.visibility = View.GONE
            contact_details_PhoneNumberLayout!!.visibility = View.GONE
            contact_details_FixNumberLayout!!.visibility = View.VISIBLE
            contact_details_VideoCallWhatsappLayout!!.visibility = View.GONE
            contact_details_VocalCallWhatsappLayout!!.visibility = View.GONE
            contact_details_MessageWhatsappLayout!!.visibility = View.GONE

            contact_details_PhoneNumber_text!!.text = contact_details_phone_number
            contact_details_fix_number
        }

        if (contact_details_mail != "") {
            contact_details_SendMail!!.visibility = View.VISIBLE
            contact_details_EmailLayout!!.visibility = View.VISIBLE

            contact_details_Email_text!!.text = contact_details_mail
        }

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
            val intentToEditContact = Intent(this@ContactDetailsActivity, EditContactActivity::class.java)
//            intentToEditContact.putExtra("isFavorite", isFavorite)
            intentToEditContact.putExtra("ContactId", contact_details_ContactId!!)
            startActivity(intentToEditContact)
        }

        contact_details_PhoneCall!!.setOnClickListener {
            phoneCall(contact_details_phone_number)
        }

        contact_details_SendSMS!!.setOnClickListener {
            val i = Intent(Intent.ACTION_SENDTO, Uri.fromParts("sms", contact_details_phone_number, null))
            i.putExtra("fromKnocker", "envoyé depuis Knocker")
            startActivity(i)
        }

        contact_details_Whatsapp!!.setOnClickListener {
            ContactGesture.openWhatsapp(converter06To33(contact_details_phone_number), this)
        }

        contact_details_MessageWhatsappLayout!!.setOnClickListener {
            ContactGesture.openWhatsapp(converter06To33(contact_details_phone_number), this)
        }

        contact_details_VocalCallWhatsappLayout!!.setOnClickListener {
            whatsappCall()
        }

        contact_details_SendMail!!.setOnClickListener {
            val intentSendMailTo = Intent(Intent.ACTION_SENDTO)
            intentSendMailTo.data = Uri.parse("mailto:")
            intentSendMailTo.putExtra(Intent.EXTRA_EMAIL, arrayOf(contact_details_mail))
            intentSendMailTo.putExtra(Intent.EXTRA_SUBJECT, "")
            intentSendMailTo.putExtra(Intent.EXTRA_TEXT, "Envoyé depuis Knocker")
            startActivity(intentSendMailTo)
        }

        contact_details_PhoneNumberLayout!!.setOnClickListener {
            phoneCall(contact_details_phone_number)
        }

        contact_details_FixNumberLayout!!.setOnClickListener {
            phoneCall(contact_details_fix_number)
        }

        contact_details_EmailLayout!!.setOnClickListener {
            val intentSendMailTo = Intent(Intent.ACTION_SENDTO)
            intentSendMailTo.data = Uri.parse("mailto:")
            intentSendMailTo.putExtra(Intent.EXTRA_EMAIL, arrayOf(contact_details_mail))
            intentSendMailTo.putExtra(Intent.EXTRA_SUBJECT, "")
            intentSendMailTo.putExtra(Intent.EXTRA_TEXT, "Envoyé depuis Knocker")
            startActivity(intentSendMailTo)
        }

        //endregion
    }

    //region ========================================== Functions ===========================================

    private fun deleteContact() {
        MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                .setTitle(getString(R.string.edit_contact_delete_contact))
                .setMessage(getString(R.string.edit_contact_delete_contact_message))
                .setPositiveButton("Remove") { _, _ ->
                    contact_details_ContactsDatabase!!.contactsDao().deleteContactById(contact_details_ContactId!!)
                    val mainIntent = Intent(this@ContactDetailsActivity, MainActivity::class.java)
                    mainIntent.putExtra("isDelete", true)
                    startActivity(mainIntent)
                    finish()
                }.show()
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

    //region ========================================== Favorites ===========================================

    private fun addToFavorite() {
        val contact = contact_details_ContactsDatabase?.contactsDao()?.getContact(contact_details_ContactId!!)

        contact!!.setIsFavorite(contact_details_ContactsDatabase)

        var counter = 0
        var alreadyExist = false

        while (counter < contact_details_ContactsDatabase?.GroupsDao()!!.getAllGroupsByNameAZ().size) {
            if (contact_details_ContactsDatabase?.GroupsDao()!!.getAllGroupsByNameAZ()[counter].groupDB!!.name == "Favorites") {
                groupId = contact_details_ContactsDatabase?.GroupsDao()!!.getAllGroupsByNameAZ()[counter].groupDB!!.id!!
                alreadyExist = true
                break
            }
            counter++
        }

        listContact.add(contact.contactDB)

        if (alreadyExist) {
            addContactToGroup(listContact, groupId)
        } else {
            createGroup(listContact, "Favorites")
        }
    }

    private fun removeFromFavorite() {
        val contact = contact_details_ContactsDatabase?.contactsDao()?.getContact(contact_details_ContactId!!)

        contact!!.setIsNotFavorite(contact_details_ContactsDatabase)

        var counter = 0

        while (counter < contact_details_ContactsDatabase?.GroupsDao()!!.getAllGroupsByNameAZ().size) {
            if (contact_details_ContactsDatabase?.GroupsDao()!!.getAllGroupsByNameAZ()[counter].groupDB!!.name == "Favorites") {
                groupId = contact_details_ContactsDatabase?.GroupsDao()!!.getAllGroupsByNameAZ()[counter].groupDB!!.id!!
                break
            }
            counter++
        }

        listContact.remove(contact.contactDB)

        removeContactFromGroup(contact_details_ContactId!!, groupId)

        counter = 0

        while (counter < contact_details_ContactsDatabase?.GroupsDao()!!.getAllGroupsByNameAZ().size) {
            if (contact_details_ContactsDatabase?.GroupsDao()!!.getAllGroupsByNameAZ()[counter].getListContact(this).isEmpty()) {
                contact_details_ContactsDatabase?.GroupsDao()!!.deleteGroupById(contact_details_ContactsDatabase?.GroupsDao()!!.getAllGroupsByNameAZ()[counter].groupDB!!.id!!.toInt())
                break
            }
            counter++
        }
    }

    //endregion

    //region =========================================== Groups =============================================

    private fun createGroup(listContact: ArrayList<ContactDB?>, name: String) {
        val group = GroupDB(null, name, "")

        val groupId = contact_details_ContactsDatabase!!.GroupsDao().insert(group)
        listContact.forEach {
            val link = LinkContactGroup(groupId!!.toInt(), it!!.id!!)
            println("contact db id" + contact_details_ContactsDatabase!!.LinkContactGroupDao().insert(link))
        }
    }

    private fun addContactToGroup(listContact: ArrayList<ContactDB?>, groupId: Long?) {
        listContact.forEach {
            val link = LinkContactGroup(groupId!!.toInt(), it!!.id!!)
            contact_details_ContactsDatabase!!.LinkContactGroupDao().insert(link)
        }
    }

    private fun removeContactFromGroup(contactId: Int, groupId: Long?) {
        contact_details_ContactsDatabase!!.LinkContactGroupDao().deleteContactIngroup(contactId, groupId!!.toInt())

    }

    //endregion

    private fun appIsInstalled(): Boolean {
        val pm = this.packageManager
        return try {
            pm.getApplicationInfo("com.whatsapp", 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun whatsappCall() {
        val resolver = contentResolver;
        val cursor = resolver.query(
                ContactsContract.Data.CONTENT_URI,
                null, null, null,
                ContactsContract.Contacts.DISPLAY_NAME);

        //Now read data from cursor like

        while (cursor.moveToNext()) {
            val _id = cursor.getLong(cursor.getColumnIndex(ContactsContract.Data._ID))
            val displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME))
            val mimeType = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.MIMETYPE))

            Log.d("Data", "$_id $displayName $mimeType")

            val intent = Intent()
            intent.action = Intent.ACTION_VIEW

            // the _ids you save goes here at the end of /data/12562
            intent.setDataAndType(Uri.parse("content://com.android.contacts/data/_id"),
                    "vnd.android.cursor.item/vnd.com.whatsapp.voip.call");
            intent.setPackage("com.whatsapp")

            startActivity(intent)
        }
    }

    private fun base64ToBitmap(base64: String): Bitmap {
        val imageBytes = Base64.decode(base64, 0)
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    override fun onBackPressed() {
        startActivity(Intent(this@ContactDetailsActivity, MainActivity::class.java))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MAKE_CALL_PERMISSION_REQUEST_CODE -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            }
        }
    }

    private fun converter06To33(phoneNumber: String): String {
        return if (phoneNumber[0] == '0') {
            "+33 $phoneNumber"
        } else phoneNumber
    }

    private fun phoneCall(phoneNumber: String) {
        if (!TextUtils.isEmpty(phoneNumber)) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), MAKE_CALL_PERMISSION_REQUEST_CODE)
                numberForPermission = phoneNumber
            } else {
                if (numberForPermission.isEmpty()) {
                    startActivity(Intent(Intent.ACTION_CALL, Uri.fromParts("tel", phoneNumber, null)))
                } else {
                    startActivity(Intent(Intent.ACTION_CALL, Uri.fromParts("tel", phoneNumber, null)))
                    numberForPermission = ""
                }
            }
        } else {
            Toast.makeText(this, R.string.phone_log_toast_phone_number_empty, Toast.LENGTH_SHORT).show()
        }
    }

    //endregion
}
