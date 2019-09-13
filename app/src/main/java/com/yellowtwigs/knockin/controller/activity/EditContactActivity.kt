package com.yellowtwigs.knockin.controller.activity

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import android.text.TextWatcher
import android.util.Base64
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.controller.CircularImageView
import com.yellowtwigs.knockin.controller.ContactIconeAdapter
import com.yellowtwigs.knockin.controller.GroupEditAdapter
import com.yellowtwigs.knockin.controller.activity.group.GroupManagerActivity
import com.yellowtwigs.knockin.model.*
import com.yellowtwigs.knockin.model.ModelDB.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import java.io.ByteArrayOutputStream
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * La Classe qui permet d'éditer un contact choisi
 * @author Florian Striebel, Kenzy Suon, Ryan Granet
 */
class EditContactActivity : AppCompatActivity() {

    //region ========================================== Var or Val ==========================================

    private var gestionnaireContacts: ContactManager? = null

    private var edit_contact_ParentLayout: ConstraintLayout? = null

    private var edit_contact_FirstName: TextInputLayout? = null
    private var edit_contact_LastName: TextInputLayout? = null
    private var edit_contact_PhoneNumber: TextInputLayout? = null
    private var edit_contact_FixNumber: TextInputLayout? = null
    private var edit_contact_Mail: TextInputLayout? = null

    private var edit_contact_RoundedImageView: CircularImageView? = null
    private var edit_contact_Priority: Spinner? = null
    private var edit_contact_Priority_explain: TextView? = null
    private var edit_contact_Phone_Property: Spinner? = null
    private var edit_contact_Fix_Property: Spinner? = null
    private var edit_contact_Mail_Property: Spinner? = null
    private var edit_contact_AddFieldButton: Button? = null

    private var edit_contact_Return: AppCompatImageView? = null
    private var edit_contact_DeleteContact: AppCompatImageView? = null
    private var edit_contact_AddContactToFavorite: AppCompatImageView? = null
    private var edit_contact_RemoveContactFromFavorite: AppCompatImageView? = null
    private var edit_contact_Validate: AppCompatImageView? = null

    private var groupId: Long = 0
    private var listContact: ArrayList<ContactDB?> = ArrayList()

    private var edit_contact_id: Int? = null
    private var edit_contact_first_name: String = ""
    private var edit_contact_last_name: String = ""
    private var edit_contact_phone_number: String = ""
    private var edit_contact_phone_property: String = ""
    private var edit_contact_fix_number: String = ""
    private var edit_contact_fix_property: String = ""
    private var edit_contact_mail_property: String = ""
    private var edit_contact_mail: String = ""
    private var edit_contact_rounded_image: Int = 0
    private var edit_contact_image64: String = ""
    private var edit_contact_priority: Int = 1

    // Database && Thread
    private var edit_contact_ContactsDatabase: ContactsRoomDatabase? = null
    private lateinit var edit_contact_mDbWorkerThread: DbWorkerThread

    private var imageUri: Uri? = null
    private var SELECT_FILE: Int? = 0
    private val IMAGE_CAPTURE_CODE = 1001
    private var edit_contact_imgString: String? = null

    private var edit_contact_imgStringChanged = false

    private var havePhone: Boolean = false
    private var haveSecondPhone: Boolean = false
    private var haveMail: Boolean = false

    private var recyclerGroup: RecyclerView? = null

    private var fromGroupActivity = false

    private var isChanged = false
    private var editInAndroid = false
    private var editInGoogle = false

    private var isFavorite: Boolean? = null
    private var isFavoriteChanged: Boolean? = null

    private var position: Int? = null

    //endregion

    @SuppressLint("InflateParams", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //region ======================================== Theme Dark ========================================

        val sharedThemePreferences = getSharedPreferences("Knockin_Theme", Context.MODE_PRIVATE)
        if (sharedThemePreferences.getBoolean("darkTheme", false)) {
            setTheme(R.style.AppThemeDark)
        } else {
            setTheme(R.style.AppTheme)
        }

        //endregion

        setContentView(R.layout.activity_edit_contact)

        // on init WorkerThread
        edit_contact_mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        edit_contact_mDbWorkerThread.start()

        //on get la base de données
        edit_contact_ContactsDatabase = ContactsRoomDatabase.getDatabase(this)

        //region ========================================== Intent ==========================================

        // Create the Intent, and get the data from the GridView

        val intent = intent
        edit_contact_id = intent.getIntExtra("ContactId", 1)
        position = intent.getIntExtra("position", 0)
        fromGroupActivity = intent.getBooleanExtra("fromGroupActivity", false)
        gestionnaireContacts = ContactManager(this.applicationContext)

        //endregion

        //region ======================================= FindViewById =======================================

        edit_contact_ParentLayout = findViewById(R.id.edit_contact_parent_layout)
        edit_contact_FirstName = findViewById(R.id.edit_contact_first_name_id)
        edit_contact_LastName = findViewById(R.id.edit_contact_last_name_id)
        edit_contact_PhoneNumber = findViewById(R.id.edit_contact_phone_number_id)
        edit_contact_FixNumber = findViewById(R.id.edit_contact_phone_number_fix_id)
        edit_contact_RoundedImageView = findViewById(R.id.edit_contact_rounded_image_view_id)
        edit_contact_Mail = findViewById(R.id.edit_contact_mail_id)
        edit_contact_Mail_Property = findViewById(R.id.edit_contact_mail_spinner_id)
        edit_contact_Priority = findViewById(R.id.edit_contact_priority)
        edit_contact_Phone_Property = findViewById(R.id.edit_contact_phone_number_spinner)
        edit_contact_Fix_Property = findViewById(R.id.edit_contact_phone_number_spinner_fix)
        edit_contact_Priority_explain = findViewById(R.id.edit_contact_priority_explain)
        edit_contact_AddFieldButton = findViewById(R.id.edit_contact_add_field_button)

        recyclerGroup = findViewById(R.id.edit_contact_recycler)

        edit_contact_Return = findViewById(R.id.edit_contact_return)
        edit_contact_DeleteContact = findViewById(R.id.edit_contact_delete)
        edit_contact_AddContactToFavorite = findViewById(R.id.edit_contact_favorite)
        edit_contact_RemoveContactFromFavorite = findViewById(R.id.edit_contact_favorite_shine)
        edit_contact_Validate = findViewById(R.id.edit_contact_edit_contact)

        //endregion

        //disable keyboard

        edit_contact_ParentLayout!!.setOnTouchListener { _, _ ->
            val view = this@EditContactActivity.currentFocus
            val imm = this@EditContactActivity.getSystemService(
                    Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (view != null) {
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
            true
        }

        //edit_contact_AddFieldButton = findViewById(R.id.edit_contact_add_field_button)

        //TODO wash the code
        if (edit_contact_ContactsDatabase?.contactsDao()?.getContact(edit_contact_id!!.toInt()) == null) {

            val contactList = ContactManager(this)
            val contact = contactList.getContactById(edit_contact_id!!)!!
            edit_contact_first_name = contact.contactDB!!.firstName
            edit_contact_last_name = contact.contactDB!!.lastName
            val tmpPhone = contact.contactDetailList!![0]
            edit_contact_phone_number = tmpPhone.content
            edit_contact_phone_property = tmpPhone.tag
            val tmpMail = contact.contactDetailList!![1]
            edit_contact_mail = tmpMail.content
            edit_contact_mail_property = tmpMail.tag
            edit_contact_priority = contact.contactDB!!.contactPriority
            edit_contact_image64 = contact.contactDB!!.profilePicture64
            edit_contact_RoundedImageView!!.setImageBitmap(base64ToBitmap(edit_contact_image64))
        } else {

            val executorService: ExecutorService = Executors.newFixedThreadPool(1)
            val callDb = Callable { edit_contact_ContactsDatabase!!.contactsDao().getContact(edit_contact_id!!) }
            val result = executorService.submit(callDb)
            val contact: ContactWithAllInformation = result.get()
            edit_contact_first_name = contact.contactDB!!.firstName
            edit_contact_last_name = contact.contactDB!!.lastName
            edit_contact_priority = contact.contactDB!!.contactPriority
            edit_contact_rounded_image = gestionnaireContacts!!.randomDefaultImage(contact.contactDB!!.profilePicture, "Get")
            //TODO :enlever code Dupliquer

            edit_contact_phone_property = getString(R.string.edit_contact_phone_number_mobile)
            edit_contact_fix_number = getString(R.string.edit_contact_phone_number_home)
            edit_contact_phone_number = ""
            edit_contact_mail = ""
            edit_contact_mail_property = getString(R.string.edit_contact_mail_mobile)

            val tagPhone = contact.getPhoneNumberTag()
            val phoneNumber = contact.getFirstPhoneNumber()
            edit_contact_phone_number = phoneNumber
            edit_contact_phone_property = tagPhone
            println("phone property of number $phoneNumber is $tagPhone")
            val tagFix = contact.getSecondPhoneTag(phoneNumber)
            val fixNumber = contact.getSecondPhoneNumber(phoneNumber)
            edit_contact_fix_number = fixNumber
            edit_contact_fix_property = tagFix
            val tagMail = contact.getMailTag()
            val mail = contact.getFirstMail()
            edit_contact_mail = mail
            edit_contact_mail_property = tagMail

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

            val id = edit_contact_id
            val contactDB = edit_contact_ContactsDatabase?.contactsDao()?.getContact(id!!.toInt())
            edit_contact_image64 = contactDB!!.contactDB!!.profilePicture64
            if (edit_contact_image64 == "") {
                edit_contact_RoundedImageView!!.setImageResource(edit_contact_rounded_image)
            } else {
                val image64 = edit_contact_image64
                edit_contact_RoundedImageView!!.setImageBitmap(base64ToBitmap(image64))
            }
            when (edit_contact_priority) {
                0 -> {
                    edit_contact_RoundedImageView!!.setBorderColor(resources.getColor(R.color.priorityZeroColor, null))
                    edit_contact_RoundedImageView!!.setBetweenBorderColor(resources.getColor(R.color.lightColor, null))
                }
                1 -> {
                    edit_contact_RoundedImageView!!.setBorderColor(resources.getColor(R.color.priorityOneColor, null))
                    edit_contact_RoundedImageView!!.setBetweenBorderColor(resources.getColor(R.color.lightColor, null))
                }
                2 -> {
                    edit_contact_RoundedImageView!!.setBorderColor(resources.getColor(R.color.priorityTwoColor, null))
                    edit_contact_RoundedImageView!!.setBetweenBorderColor(resources.getColor(R.color.lightColor, null))
                }
            }
        }

        //region ===================================== SetViewDataField =====================================

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        edit_contact_FirstName!!.editText!!.setText(edit_contact_first_name)
        edit_contact_LastName!!.editText!!.setText(edit_contact_last_name)
        edit_contact_PhoneNumber!!.editText!!.setText(edit_contact_phone_number)
        edit_contact_FixNumber!!.editText!!.setText(edit_contact_fix_number)
        edit_contact_Mail!!.editText!!.setText(edit_contact_mail)
        edit_contact_Mail_Property!!.setSelection(getPosItemSpinner(edit_contact_mail_property, edit_contact_Mail_Property!!))
        edit_contact_Phone_Property!!.setSelection(getPosItemSpinner(edit_contact_phone_property, edit_contact_Phone_Property!!))
        edit_contact_Fix_Property!!.setSelection(getPosItemSpinner(edit_contact_fix_property, edit_contact_Fix_Property!!))
        textChanged(edit_contact_FirstName, edit_contact_FirstName!!.editText!!.text?.toString())
        textChanged(edit_contact_LastName, edit_contact_LastName!!.editText!!.text?.toString())
        textChanged(edit_contact_PhoneNumber, edit_contact_PhoneNumber!!.editText!!.text?.toString())
        textChanged(edit_contact_Mail, edit_contact_Mail!!.editText!!.text?.toString())

        //endregion

        //region ======================================== Favorites =========================================

        val contactList = ContactManager(this)
        val contact = contactList.getContactById(edit_contact_id!!)!!

        if (contact.contactDB!!.favorite == 1) {
            edit_contact_RemoveContactFromFavorite!!.visibility = View.VISIBLE
            edit_contact_AddContactToFavorite!!.visibility = View.INVISIBLE

            isFavorite = true
            isFavoriteChanged = true

        } else if (contact.contactDB!!.favorite == 0) {
            edit_contact_AddContactToFavorite!!.visibility = View.VISIBLE
            edit_contact_RemoveContactFromFavorite!!.visibility = View.INVISIBLE

            isFavorite = false
            isFavoriteChanged = false
        }

        //endregion

        //region ========================================== Tags ============================================

        val phoneTagList = resources.getStringArray(R.array.edit_contact_phone_number_arrays)
        val adapterPhoneTagList = ArrayAdapter(this, R.layout.spinner_item, phoneTagList)
        //array_adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)

        val mailTagList = resources.getStringArray(R.array.edit_contact_mail_arrays)
        val adapterMailTagList = ArrayAdapter(this, R.layout.spinner_item, mailTagList)
        //array_adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)

        edit_contact_Mail_Property!!.adapter = adapterMailTagList
        edit_contact_Phone_Property!!.adapter = adapterPhoneTagList
        edit_contact_Fix_Property!!.adapter = adapterPhoneTagList

        //endregion

        //region ======================================== Priority ==========================================

        val priority_list = arrayOf(getString(R.string.add_new_contact_priority_0), "Standard", "VIP")
        val priority_adapter = ArrayAdapter(this, R.layout.spinner_item, priority_list)
        //array_adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        edit_contact_Priority!!.adapter = priority_adapter
        //println("edit contact prio === " + edit_contact_priority)
        edit_contact_Priority!!.setSelection(edit_contact_priority)
        edit_contact_Priority!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> {
                        edit_contact_Priority_explain!!.text = getString(R.string.add_new_contact_priority0)
                        edit_contact_RoundedImageView!!.visibility = View.GONE
                        edit_contact_RoundedImageView!!.setBorderColor(resources.getColor(R.color.priorityZeroColor, null))
                        edit_contact_RoundedImageView!!.setBetweenBorderColor(resources.getColor(R.color.lightColor))
                        edit_contact_RoundedImageView!!.visibility = View.VISIBLE
                    }
                    1 -> {
                        edit_contact_Priority_explain!!.text = getString(R.string.add_new_contact_priority1)
                        edit_contact_RoundedImageView!!.visibility = View.GONE
                        edit_contact_RoundedImageView!!.setBorderColor(resources.getColor(R.color.priorityOneColor))
                        edit_contact_RoundedImageView!!.setBetweenBorderColor(resources.getColor(R.color.lightColor))
                        edit_contact_RoundedImageView!!.visibility = View.VISIBLE
                    }
                    2 -> {
                        edit_contact_Priority_explain!!.text = getString(R.string.add_new_contact_priority2)
                        edit_contact_RoundedImageView!!.visibility = View.GONE
                        edit_contact_RoundedImageView!!.setBorderColor(resources.getColor(R.color.priorityTwoColor))
                        edit_contact_RoundedImageView!!.setBetweenBorderColor(resources.getColor(R.color.lightColor))
                        edit_contact_RoundedImageView!!.visibility = View.VISIBLE
                    }
                }
            }
        }

        //endregion

        //region ========================================== Groups ==========================================

        val layoutMananger = LinearLayoutManager(applicationContext, LinearLayoutManager.HORIZONTAL, false)
        recyclerGroup!!.layoutManager = layoutMananger
        //edit_contact_ContactsDatabase.contactsDao()
        val executorService: ExecutorService = Executors.newFixedThreadPool(1)
        val callDbGroup = Callable { edit_contact_ContactsDatabase!!.GroupsDao().getGroupForContact(edit_contact_id!!) }
        val resultGroup = executorService.submit(callDbGroup)
        val listGroup: ArrayList<GroupDB> = ArrayList()
        listGroup.addAll(resultGroup.get())

        val callDBContact = Callable { edit_contact_ContactsDatabase!!.contactsDao().getContact(edit_contact_id!!) }
        val resultContact = executorService.submit(callDBContact)
        val adapter = GroupEditAdapter(this, listGroup, resultContact.get())
        recyclerGroup!!.adapter = adapter

        //endregion

        //region ======================================== Listeners =========================================

        edit_contact_Return!!.setOnClickListener {
            onBackPressed()
        }

        edit_contact_DeleteContact!!.setOnClickListener {
            deleteContact()
        }

        edit_contact_AddContactToFavorite!!.setOnClickListener {
            edit_contact_AddContactToFavorite!!.visibility = View.INVISIBLE
            edit_contact_RemoveContactFromFavorite!!.visibility = View.VISIBLE

            isFavorite = true
        }

        edit_contact_RemoveContactFromFavorite!!.setOnClickListener {
            edit_contact_RemoveContactFromFavorite!!.visibility = View.INVISIBLE
            edit_contact_AddContactToFavorite!!.visibility = View.VISIBLE

            isFavorite = false
        }

        edit_contact_Validate!!.setOnClickListener {
            hideKeyboard()

            if (edit_contact_FirstName!!.editText!!.text.toString() != edit_contact_first_name ||
                    edit_contact_LastName!!.editText!!.text.toString() != edit_contact_last_name ||
                    edit_contact_PhoneNumber!!.editText!!.text.toString() != edit_contact_phone_number ||
                    edit_contact_FixNumber!!.editText!!.text.toString() != edit_contact_fix_number ||
                    edit_contact_Mail!!.editText!!.text.toString() != edit_contact_mail) {
                MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                        .setTitle(R.string.edit_contact_alert_dialog_sync_contact_title)
                        .setMessage(R.string.edit_contact_alert_dialog_sync_contact_message)
                        .setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
                            editContactValidation()
                            editInAndroid = true
                            editInGoogle = true

                            val intentInsertEdit = Intent(Intent.ACTION_INSERT_OR_EDIT).apply {
                                type = ContactsContract.Contacts.CONTENT_ITEM_TYPE

                                putExtra(ContactsContract.Intents.Insert.NAME, edit_contact_FirstName?.editText!!.text.toString() + " " + edit_contact_LastName?.editText!!.text.toString())

                                putExtra(ContactsContract.Intents.Insert.EMAIL, edit_contact_Mail?.editText!!.text.toString())
                                putExtra(
                                        ContactsContract.Intents.Insert.EMAIL_TYPE,
                                        ContactsContract.CommonDataKinds.Email.TYPE_WORK
                                )
                                putExtra(ContactsContract.Intents.Insert.PHONE, edit_contact_PhoneNumber?.editText!!.text.toString())
                                putExtra(
                                        ContactsContract.Intents.Insert.PHONE_TYPE,
                                        ContactsContract.CommonDataKinds.Phone.TYPE_WORK
                                )
                            }
                            startActivity(intentInsertEdit)
                        }
                        .setNegativeButton(R.string.alert_dialog_no) { _, _ ->
                            editContactValidation()
                            if (fromGroupActivity) {
                                startActivity(Intent(this@EditContactActivity, GroupManagerActivity::class.java))
                                finish()
                            } else {
                                startActivity(Intent(this@EditContactActivity, MainActivity::class.java).putExtra("position", position!!))
                                finish()
                            }
                        }
                        .show()
            } else if (
                    isFavorite != isFavoriteChanged || edit_contact_imgStringChanged ||
                    edit_contact_priority != edit_contact_Priority!!.selectedItemPosition) {
                editContactValidation()
                if (fromGroupActivity) {
                    startActivity(Intent(this@EditContactActivity, GroupManagerActivity::class.java))
                    finish()
                } else {
                    startActivity(Intent(this@EditContactActivity, MainActivity::class.java).putExtra("position", position!!))
                    finish()
                }
            } else {
                if (fromGroupActivity) {
                    startActivity(Intent(this@EditContactActivity, GroupManagerActivity::class.java))
                    finish()
                } else {
                    startActivity(Intent(this@EditContactActivity, MainActivity::class.java).putExtra("position", position!!))
                    finish()
                }
            }

            // Creates a new Intent to insert a contact

        }

        edit_contact_RoundedImageView!!.setOnClickListener {
            selectImage()
        }

        edit_contact_AddFieldButton!!.setOnClickListener {
            val inflater: LayoutInflater = this.layoutInflater
            val alertView: View = inflater.inflate(R.layout.alert_dialog_add_field, null)

//            val alert_dialog_AddFieldListView = alertView.findViewById<List>(R.id.alert_dialog_add_field_list_view)
//            val alert_dialog_FieldAdded = findViewById<ListView>(R.id.edit_contact_field_added)

//            alert_dialog_AddFieldListView.adapter = ArrayAdapter(this@EditContactActivity, R.layout.list_item_add_fields_layout, R.id.list_item_add_fields_text, getListOfFields())
//            alert_dialog_AddFieldListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
//
//                customAdapterEditText = CustomAdapterEditText(this@EditContactActivity, getListOfEditText(position))
//                alert_dialog_FieldAdded.adapter = customAdapterEditText
//            }

            MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                    .setTitle(R.string.edit_contact_add_field)
                    .setView(alertView)
                    .show()
        }

        //endregion
    }

    //region ========================================== Functions ===========================================

    fun editContactValidation() {
        val editContact = Runnable {
            if (edit_contact_FirstName!!.editText!!.toString() != "" || edit_contact_LastName!!.editText!!.toString() != "") {
                val spinnerPhoneChar = NumberAndMailDB.convertSpinnerStringToChar(edit_contact_Phone_Property!!.selectedItem.toString(), this)
                val spinnerMailChar = NumberAndMailDB.convertSpinnerStringToChar(edit_contact_Mail_Property!!.selectedItem.toString(), this)

                val spinnerFixChar = NumberAndMailDB.convertSpinnerStringToChar(edit_contact_Fix_Property!!.selectedItem.toString(), this)

                val contact = edit_contact_ContactsDatabase?.contactsDao()?.getContact(edit_contact_id!!)
                val nbDetail = contact!!.contactDetailList!!.size - 1

                if (isFavoriteChanged != isFavorite) {
                    if (isFavorite!!) {
                        addToFavorite()
                    } else {
                        removeFromFavorite()
                    }
                }

                //   for (i in 0..nbDetail) {
                if (havePhone) {
                    //println("------------il a un numéro ------")
                    //println("contact content number "+contact.contactDetailList!![i].content)
                    val firstNumber = contact.getFirstPhoneNumber()
                    var counter = 0
                    while (counter < contact.contactDetailList!!.size) {
                        if (contact.contactDetailList!![counter].content == (firstNumber)) {
                            counter = if (edit_contact_PhoneNumber!!.editText!!.text.toString() == "") {
                                edit_contact_ContactsDatabase!!.contactDetailsDao().deleteDetailById(contact.contactDetailList!![counter].id!!)
                                contact.contactDetailList!!.size
                            } else {
                                edit_contact_ContactsDatabase!!.contactDetailsDao().updateContactDetailById(contact.contactDetailList!![counter].id!!, edit_contact_PhoneNumber!!.editText!!.text.toString())
                                //println("condition = havemail ="+haveMail+" && text = "+edit_contact_Mail!!.editText!!.text.toString())
                                contact.contactDetailList!!.size
                            }
                        }
                        counter++
                    }
                    if (!haveMail && edit_contact_Mail!!.editText!!.text.toString() != "") {
                        //println("------------et à ajouter un mail------")
                        val detail = ContactDetailDB(null, contact.getContactId(), edit_contact_Mail!!.editText!!.text.toString(), "mail", spinnerMailChar, 2)
                        edit_contact_ContactsDatabase!!.contactDetailsDao().insert(detail)
                    } else {
                        println("have mail")
                    }
                    if (!haveSecondPhone && edit_contact_Mail!!.editText!!.text.toString() != "") {
                        val detail = ContactDetailDB(null, contact.getContactId(), edit_contact_FixNumber!!.editText!!.text.toString(), "phone", spinnerFixChar, 1)
                        edit_contact_ContactsDatabase!!.contactDetailsDao().insert(detail)
                    } else {
                        println("have second phone number")
                    }

                } else if (!havePhone && edit_contact_PhoneNumber!!.editText!!.text.toString() != "") {
                    //println("------------et à ajouter un numéro------")
                    val detail = ContactDetailDB(null, contact.getContactId(), edit_contact_PhoneNumber!!.editText!!.text.toString(), "phone", spinnerPhoneChar, 0)
                    edit_contact_ContactsDatabase!!.contactDetailsDao().insert(detail)
                }
                if (haveSecondPhone) {
                    val secondNumber = contact.getSecondPhoneNumber(contact.getFirstPhoneNumber())
                    var counter = 0
                    while (counter < contact.contactDetailList!!.size) {
                        if (contact.contactDetailList!![counter].content == secondNumber) {
                            counter = if (edit_contact_FixNumber!!.editText!!.text.toString() == "") {
                                edit_contact_ContactsDatabase!!.contactDetailsDao().deleteDetailById(contact.contactDetailList!![counter].id!!)
                                contact.contactDetailList!!.size
                            } else {
                                edit_contact_ContactsDatabase!!.contactDetailsDao().updateContactDetailById(contact.contactDetailList!![counter].id!!, "" + edit_contact_FixNumber!!.editText!!.text)
                                //println("condition = havemail ="+haveMail+" && text = "+edit_contact_Mail!!.editText!!.text.toString())
                                contact.contactDetailList!!.size
                            }
                        }
                        counter++
                    }


                } else if (!haveSecondPhone && edit_contact_FixNumber!!.editText!!.text.toString() != "") {
                    val detail = ContactDetailDB(null, contact.getContactId(), edit_contact_FixNumber!!.editText!!.text.toString(), "phone", spinnerFixChar, 1)
                    edit_contact_ContactsDatabase!!.contactDetailsDao().insert(detail)
                }
                if (haveMail) {
                    val firstMail = contact.getFirstMail()
                    var counter = 0
                    while (counter < contact.contactDetailList!!.size) {
                        if (contact.contactDetailList!![counter].content == firstMail) {
                            println("contact content mail" + contact.contactDetailList!![counter].content)
                            if (edit_contact_Mail!!.editText!!.text.toString() == "") {
                                edit_contact_ContactsDatabase!!.contactDetailsDao().deleteDetailById(contact.contactDetailList!!.get(counter).id!!)
                                counter = contact.contactDetailList!!.size
                            } else {
                                edit_contact_ContactsDatabase!!.contactDetailsDao().updateContactDetailById(contact.contactDetailList!![counter].id!!, "" + edit_contact_Mail!!.editText!!.text)
                                //println("condition = havemail ="+haveMail+" && text = "+edit_contact_Mail!!.editText!!.text.toString())
                                counter = contact.contactDetailList!!.size
                            }
                        } else {
                            println("contact content mail" + firstMail + "différent de" + contact.contactDetailList!!.get(counter).content + "r")
                        }
                        counter++
                    }
                } else if (edit_contact_Mail!!.editText!!.text.toString() != "") {
                    val detail = ContactDetailDB(null, contact.getContactId(), edit_contact_Mail!!.editText!!.text.toString(), "mail", spinnerMailChar, 2)
                    edit_contact_ContactsDatabase!!.contactDetailsDao().insert(detail)
                }
                // }//TODO change for the listView
                if (nbDetail == -1 && edit_contact_Mail!!.editText!!.text.toString() != "") {
                    val detail = ContactDetailDB(null, contact.getContactId(), edit_contact_Mail!!.editText!!.text.toString(), "mail", spinnerMailChar, 2)
                    edit_contact_ContactsDatabase!!.contactDetailsDao().insert(detail)
                }
                if (nbDetail == -1 && edit_contact_PhoneNumber!!.editText!!.text.toString() != "") {
                    val detail = ContactDetailDB(null, contact.getContactId(), edit_contact_PhoneNumber!!.editText!!.text.toString(), "phone", spinnerPhoneChar, 0)
                    edit_contact_ContactsDatabase!!.contactDetailsDao().insert(detail)
                }
                if (nbDetail == -1 && edit_contact_FixNumber!!.editText!!.text.toString() != "") {
                    val detail = ContactDetailDB(null, contact.getContactId(), edit_contact_FixNumber!!.editText!!.text.toString(), "phone", spinnerPhoneChar, 1)
                    edit_contact_ContactsDatabase!!.contactDetailsDao().insert(detail)
                }
                if (edit_contact_imgString != null) {

                    //println("edit contact rounded != null "+edit_contact_rounded_image )
                    edit_contact_ContactsDatabase?.contactsDao()?.updateContactById(edit_contact_id!!.toInt(), edit_contact_FirstName!!.editText!!.text.toString(), edit_contact_LastName!!.editText!!.text.toString(), edit_contact_imgString!!, edit_contact_Priority!!.selectedItemPosition) //edit contact rounded maybe not work

                } else {
                    //println("edit contact rounded == null "+edit_contact_rounded_image )
                    edit_contact_ContactsDatabase?.contactsDao()?.updateContactByIdWithoutPic(edit_contact_id!!.toInt(), edit_contact_FirstName!!.editText!!.text.toString(), edit_contact_LastName!!.editText!!.text.toString(), edit_contact_Priority!!.selectedItemPosition)
                }
                //println("modify on contact " + contact.contactDB)

            } else {
                Toast.makeText(this, R.string.edit_contact_toast, Toast.LENGTH_LONG).show()
            }
        }
        edit_contact_mDbWorkerThread.postTask(editContact)
    }

    override fun onRestart() {
        super.onRestart()
        if (editInAndroid) {
            if (fromGroupActivity) {
                startActivity(Intent(this@EditContactActivity, GroupManagerActivity::class.java).putExtra("ContactId", edit_contact_id!!))
                finish()
            } else {
                startActivity(Intent(this@EditContactActivity, MainActivity::class.java).putExtra("ContactId", edit_contact_id!!).putExtra("position", position))
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (editInGoogle) {
            if (fromGroupActivity) {
                startActivity(Intent(this@EditContactActivity, GroupManagerActivity::class.java).putExtra("ContactId", edit_contact_id!!))
                finish()
            } else {
                startActivity(Intent(this@EditContactActivity, MainActivity::class.java).putExtra("ContactId", edit_contact_id!!).putExtra("position", position))
                finish()
            }
        }
    }

    override fun onBackPressed() {
        if (isChanged || isFavoriteChanged != isFavorite) {
            MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                    .setTitle(R.string.edit_contact_alert_dialog_cancel_title)
                    .setMessage(R.string.edit_contact_alert_dialog_cancel_message)
                    .setBackground(getDrawable(R.color.backgroundColor))
                    .setPositiveButton(getString(R.string.alert_dialog_yes)) { _, _ ->
                        if (fromGroupActivity) {
                            startActivity(Intent(this@EditContactActivity, GroupManagerActivity::class.java))
                        } else {
                            startActivity(Intent(this@EditContactActivity, MainActivity::class.java).putExtra("position", position))
                        }
                        finish()
                    }
                    .setNegativeButton(getString(R.string.alert_dialog_no)) { _, _ ->
                    }
                    .show()
        } else {
            if (fromGroupActivity) {
                startActivity(Intent(this@EditContactActivity, GroupManagerActivity::class.java))
            } else {
                startActivity(Intent(this@EditContactActivity, MainActivity::class.java).putExtra("position", position))
            }
            finish()
        }
        hideKeyboard()
    }

    private fun deleteContact() {
        MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                .setTitle(getString(R.string.edit_contact_delete_contact))
                .setMessage(getString(R.string.edit_contact_delete_contact_message))
                .setPositiveButton(getString(R.string.edit_contact_validate)) { _, _ ->
                    edit_contact_ContactsDatabase!!.contactsDao().deleteContactById(edit_contact_id!!)
                    val mainIntent = Intent(this@EditContactActivity, MainActivity::class.java)
                    mainIntent.putExtra("isDelete", true)
                    startActivity(mainIntent)
                    finish()
                }
                .setNegativeButton(getString(R.string.edit_contact_cancel)) { _, _ ->
                }
                .show()
    }

    private fun hideKeyboard() {
        // Check if no view has focus:
        val view = this.currentFocus

        view?.let { v ->
            val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }

    private fun getPosItemSpinner(item: String, spinner: Spinner): Int {
        val tailleSpinner: Int = spinner.adapter.count
        //println("taille spinner" + tailleSpinner)
        for (x in 0 until tailleSpinner) {
            if (spinner.getItemAtPosition(x).toString() == NumberAndMailDB.convertStringToSpinnerString(item, this)) {
                return x
            } else {
                println(spinner.getItemAtPosition(x).toString() + "est diférent de " + NumberAndMailDB.convertStringToSpinnerString(item, this))
            }
        }
        return 0
    }

    //region ========================================== Favorites ===========================================

    private fun addToFavorite() {
        val contact = edit_contact_ContactsDatabase?.contactsDao()?.getContact(edit_contact_id!!)

        contact!!.setIsFavorite(edit_contact_ContactsDatabase)

        var counter = 0
        var alreadyExist = false

        while (counter < edit_contact_ContactsDatabase?.GroupsDao()!!.getAllGroupsByNameAZ().size) {
            if (edit_contact_ContactsDatabase?.GroupsDao()!!.getAllGroupsByNameAZ()[counter].groupDB!!.name == "Favorites") {
                groupId = edit_contact_ContactsDatabase?.GroupsDao()!!.getAllGroupsByNameAZ()[counter].groupDB!!.id!!
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
        val contact = edit_contact_ContactsDatabase?.contactsDao()?.getContact(edit_contact_id!!)

        contact!!.setIsNotFavorite(edit_contact_ContactsDatabase)

        var counter = 0

        while (counter < edit_contact_ContactsDatabase?.GroupsDao()!!.getAllGroupsByNameAZ().size) {
            if (edit_contact_ContactsDatabase?.GroupsDao()!!.getAllGroupsByNameAZ()[counter].groupDB!!.name == "Favorites") {
                groupId = edit_contact_ContactsDatabase?.GroupsDao()!!.getAllGroupsByNameAZ()[counter].groupDB!!.id!!
                break
            }
            counter++
        }

        listContact.remove(contact.contactDB)

        removeContactFromGroup(edit_contact_id!!, groupId)

        counter = 0

        while (counter < edit_contact_ContactsDatabase?.GroupsDao()!!.getAllGroupsByNameAZ().size) {
            if (edit_contact_ContactsDatabase?.GroupsDao()!!.getAllGroupsByNameAZ()[counter].getListContact(this).isEmpty()) {
                edit_contact_ContactsDatabase?.GroupsDao()!!.deleteGroupById(edit_contact_ContactsDatabase?.GroupsDao()!!.getAllGroupsByNameAZ()[counter].groupDB!!.id!!.toInt())
                break
            }
            counter++
        }
    }

    //endregion

    //region =========================================== Groups =============================================

    private fun createGroup(listContact: ArrayList<ContactDB?>, name: String) {
        val group = GroupDB(null, name, "", -500138)

        val groupId = edit_contact_ContactsDatabase!!.GroupsDao().insert(group)
        listContact.forEach {
            val link = LinkContactGroup(groupId!!.toInt(), it!!.id!!)
            println("contact db id" + edit_contact_ContactsDatabase!!.LinkContactGroupDao().insert(link))
        }
    }

    private fun addContactToGroup(listContact: ArrayList<ContactDB?>, groupId: Long?) {
        listContact.forEach {
            val link = LinkContactGroup(groupId!!.toInt(), it!!.id!!)
            edit_contact_ContactsDatabase!!.LinkContactGroupDao().insert(link)
        }
    }

    private fun removeContactFromGroup(contactId: Int, groupId: Long?) {
        edit_contact_ContactsDatabase!!.LinkContactGroupDao().deleteContactIngroup(contactId, groupId!!.toInt())

    }

    //endregion

    private fun textChanged(textInput: TextInputLayout?, txt: String?) {
        textInput!!.editText!!.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
                isChanged = textInput.editText.toString() != txt
            }

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {

            }
        })
    }

    private fun selectImage() {

        val builderBottom = BottomSheetDialog(this)
        builderBottom.setContentView(R.layout.alert_dialog_select_contact_picture_layout)
        val gallery = builderBottom.findViewById<ConstraintLayout>(R.id.select_contact_picture_gallery_layout)
        val camera = builderBottom.findViewById<ConstraintLayout>(R.id.select_contact_picture_camera_layout)
        val recyclerView = builderBottom.findViewById<RecyclerView>(R.id.select_contact_picture_recycler_view)
        val layoutMananger = LinearLayoutManager(applicationContext, LinearLayoutManager.HORIZONTAL, false)
        recyclerView!!.layoutManager = layoutMananger
        val adapter = ContactIconeAdapter(this)
        recyclerView.adapter = adapter
        gallery!!.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
                builderBottom.dismiss()
            } else {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                intent.type = "image/*"
                startActivityForResult(Intent.createChooser(intent, this.getString(R.string.add_new_contact_intent_title)), SELECT_FILE!!)
                builderBottom.dismiss()
            }
        }
        camera!!.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 2)
                builderBottom.dismiss()
            } else {
                openCamera()
                builderBottom.dismiss()
            }
        }
        builderBottom.show()
    }

    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, R.string.edit_contact_camera_open_title)
        values.put(MediaStore.Images.Media.DESCRIPTION, R.string.edit_contact_camera_open_description)
        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE)
    }

    private fun getRealPathFromUri(context: Context, contentUri: Uri): String {
        var cursor: Cursor? = null
        try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = context.contentResolver.query(contentUri, proj, null, null, null)
            val column_index = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            return cursor.getString(column_index)
        } finally {
            cursor?.close()
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == IMAGE_CAPTURE_CODE) {
                //println("image URI = " + imageUri)

                val matrix = Matrix()
                //check la rotation de base du téléphone pour l'appliquer à la photo pour qu'elle soit tout le temps dans le bon sens
                val exif = ExifInterface(getRealPathFromUri(this, imageUri!!))
                val rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                val rotationInDegrees = exifToDegrees(rotation)
                matrix.postRotate(rotationInDegrees.toFloat())
                //convert l'imageUri en bitmap
                var bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.width / 10, bitmap.height / 10, true)
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                //set l'image et la convertit en base64 pour la mettre plus tard dans la DB
                edit_contact_RoundedImageView!!.setImageBitmap(bitmap)
                edit_contact_imgString = bitmap.bitmapToBase64()
                edit_contact_imgStringChanged = true

            } else if (requestCode == SELECT_FILE) {
                val matrix = Matrix()
                val selectedImageUri = data!!.data
                val exif = ExifInterface(getRealPathFromUri(this, selectedImageUri!!))
                val rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                val rotationInDegrees = exifToDegrees(rotation)
                matrix.postRotate(rotationInDegrees.toFloat())

                var bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)
                bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.width / 10, bitmap.height / 10, true)
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

                edit_contact_RoundedImageView!!.setImageBitmap(bitmap)
                edit_contact_imgString = bitmap.bitmapToBase64()
                edit_contact_imgStringChanged = true
            }
        }
    }

    private fun exifToDegrees(exifOrientation: Int): Int {
        return when (exifOrientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    }

    private fun Bitmap.bitmapToBase64(): String {
        val baos = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.PNG, 100, baos)
        val imageBytes = baos.toByteArray()

        return Base64.encodeToString(imageBytes, Base64.DEFAULT)
    }

    private fun base64ToBitmap(base64: String): Bitmap {
        val imageBytes = Base64.decode(base64, 0)
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    //TODO: modifier l'alert dialog en ajoutant une vue pour le rendre joli.
    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(Build.VERSION_CODES.M)
    private fun overlayAlertDialog(): AlertDialog? {

        return MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                .setTitle(R.string.alert_dialog_overlay_title)
                .setBackground(getDrawable(R.color.backgroundColor))
                .setMessage(this.resources.getString(R.string.alert_dialog_overlay_message))
                .setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
                    val intentPermission = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                    startActivity(intentPermission)
                    val sharedPreferences = getSharedPreferences("Knockin_preferences", Context.MODE_PRIVATE)
                    val edit: SharedPreferences.Editor = sharedPreferences.edit()
                    edit.putBoolean("popupNotif", true)//quand la personne autorise l'affichage par dessus d'autre application nous l'enregistrons
                    edit.putBoolean("serviceNotif", false)
                    edit.apply()
                }
                .setNegativeButton(R.string.alert_dialog_no)
                { _, _ ->
                    val sharedPreferences = getSharedPreferences("Knockin_preferences", Context.MODE_PRIVATE)
                    val edit: SharedPreferences.Editor = sharedPreferences.edit()
                    edit.putBoolean("popupNotif", false)//quand la personne autorise l'affichage par dessus d'autre application nous l'enregistrons
                    edit.putBoolean("serviceNotif", true)
                    edit.apply()
                }
                .show()
    }

    fun addContactIcone(bitmap: Bitmap) {
        //  add_new_contact_ImgString

        edit_contact_RoundedImageView!!.setImageBitmap(bitmap)
        edit_contact_imgString = bitmap.bitmapToBase64()
        edit_contact_imgStringChanged = true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent, this.getString(R.string.add_new_contact_intent_title)), SELECT_FILE!!)
        } else if (requestCode == 2 && ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        }
    }

    //endregion
}