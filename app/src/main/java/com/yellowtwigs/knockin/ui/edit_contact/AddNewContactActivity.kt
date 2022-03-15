package com.yellowtwigs.knockin.ui.edit_contact

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import androidx.appcompat.app.AlertDialog
import android.util.Base64
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yellowtwigs.knockin.*
import com.yellowtwigs.knockin.ui.CircularImageView
import com.yellowtwigs.knockin.model.*
import com.yellowtwigs.knockin.model.data.ContactDB
import com.yellowtwigs.knockin.model.data.ContactDetailDB
import com.yellowtwigs.knockin.model.data.GroupDB
import com.yellowtwigs.knockin.model.data.LinkContactGroup
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.yellowtwigs.knockin.ui.contacts.ContactsViewModel
import com.yellowtwigs.knockin.ui.contacts.MainActivity
import com.yellowtwigs.knockin.ui.groups.GroupsViewModel
import com.yellowtwigs.knockin.ui.groups.LinkContactGroupViewModel
import com.yellowtwigs.knockin.ui.in_app.PremiumActivity
import com.yellowtwigs.knockin.utils.Converter.bitmapToBase64
import com.yellowtwigs.knockin.utils.EveryActivityUtils.checkThemePreferences
import com.yellowtwigs.knockin.utils.RandomDefaultImage.randomDefaultImage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

/**
 * La Classe qui permet la création d'un nouveau contact
 * @author Florian Striebel, Kenzy Suon, Ryan Granet
 */
@AndroidEntryPoint
class AddNewContactActivity : AppCompatActivity() {

    //region ========================================== Var or Val ==========================================

    private var add_new_contact_FirstName: TextInputLayout? = null
    private var add_new_contact_LastName: TextInputLayout? = null
    private var add_new_contact_PhoneNumber: TextInputLayout? = null
    private var add_new_contact_fixNumber: TextInputLayout? = null
    private var add_new_contact_Email: TextInputLayout? = null
    private var add_new_contact_Mail_Identifier: TextInputLayout? = null
    private var identifierHelpIcon: AppCompatImageView? = null
    private var contactImage: CircularImageView? = null
    private var add_new_contact_Priority: Spinner? = null
    private var add_new_contact_PhoneProperty: Spinner? = null
    private var add_new_contact_MailProperty: Spinner? = null
    private var add_new_contact_PriorityExplain: TextView? = null
    private var gestionnaireContacts: ContactManager? = null
    private var avatar: Int = 0

    private var backButton: AppCompatImageView? = null
    private var favoriteButton: AppCompatImageView? = null
    private var unfavoriteButton: AppCompatImageView? = null
    private var saveContactButton: AppCompatImageView? = null

    private var imageUri: Uri? = null
    private val IMAGE_CAPTURE_CODE = 1001

    // Database && Thread
    private var add_new_contact_ContactsDatabase: ContactsDatabase? = null
    private lateinit var main_mDbWorkerThread: DbWorkerThread

    //private var REQUEST_CAMERA: Int? = 1
    private var SELECT_FILE: Int? = 0
    private var add_new_contact_ImgString: String? = ""

    private var isFavorite = 0
    private var isRetroFit: Boolean = false
    private var groupId: Long = 0
    private var listContact: ArrayList<ContactDB?> = ArrayList()

    private var contactsUnlimitedIsBought: Boolean? = null

    private val contactsViewModel: ContactsViewModel by viewModels()
    private val groupsViewModel: GroupsViewModel by viewModels()
    private val contactDetailsViewModel: ContactDetailsViewModel by viewModels()
    private val linkContactGroupViewModel: LinkContactGroupViewModel by viewModels()

    //endregion


    @SuppressLint("WrongThread")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkThemePreferences(this)

        setContentView(R.layout.activity_add_new_contact)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT

        // on init WorkerThread
        main_mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        main_mDbWorkerThread.start()

        val sharedNumberOfContactsVIPPreferences: SharedPreferences =
            getSharedPreferences("nb_Contacts_VIP", Context.MODE_PRIVATE)
        val nb_Contacts_VIP = sharedNumberOfContactsVIPPreferences.getInt("nb_Contacts_VIP", 0)

        val sharedAlarmNotifInAppPreferences: SharedPreferences =
            getSharedPreferences("Contacts_Unlimited_Bought", Context.MODE_PRIVATE)
        contactsUnlimitedIsBought =
            sharedAlarmNotifInAppPreferences.getBoolean("Contacts_Unlimited_Bought", false)

        //on get la base de données
        add_new_contact_ContactsDatabase = ContactsDatabase.getDatabase(this)
        gestionnaireContacts = ContactManager(this.applicationContext)

        //region ========================================== Toolbar =========================================

        backButton = findViewById(R.id.add_new_contact_return)
        favoriteButton = findViewById(R.id.add_new_contact_favorite)
        unfavoriteButton =
            findViewById(R.id.add_new_contact_favorite_shine)
        saveContactButton = findViewById(R.id.add_new_contact_validate)

        //endregion

        //region ======================================= FindViewById =======================================

        add_new_contact_FirstName = findViewById(R.id.add_new_contact_first_name_id)
        add_new_contact_LastName = findViewById(R.id.add_new_contact_last_name_id)
        add_new_contact_PhoneNumber = findViewById(R.id.add_new_contact_phone_number_id)
        add_new_contact_fixNumber = findViewById(R.id.add_new_contact_phone_number_fix_id)
        add_new_contact_Email = findViewById(R.id.add_new_contact_mail_id)
        add_new_contact_Mail_Identifier = findViewById(R.id.add_new_contact_mail_id_edit_text)
        contactImage = findViewById(R.id.add_new_contact_rounded_image_view_id)
        add_new_contact_Priority = findViewById(R.id.add_new_contact_priority)
        add_new_contact_PhoneProperty = findViewById(R.id.add_new_contact_phone_number_spinner)
        add_new_contact_MailProperty = findViewById(R.id.add_new_contact_mail_spinner_id)
        add_new_contact_PriorityExplain = findViewById(R.id.add_new_contact_priority_explain)

        identifierHelpIcon = findViewById(R.id.add_new_contact_mail_id_help)

        val add_new_contact_layout: ConstraintLayout = findViewById(R.id.add_new_contact_layout)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        //endregion

        if (intent != null) {
            if (intent.getStringExtra("numberFromCockpit") != null) {
                val add_new_contact_phone_number = intent.getStringExtra("numberFromCockpit")
                add_new_contact_PhoneNumber?.editText?.setText(add_new_contact_phone_number)
            }
        }

        avatar = randomDefaultImage(0, this, "Create")
        contactImage?.setImageResource(randomDefaultImage(avatar, this, "Get"))

        //region ==================================== SetOnClickListener ====================================

        backButton?.setOnClickListener {

            if (isEmptyField(add_new_contact_FirstName) && isEmptyField(add_new_contact_LastName) && isEmptyField(
                    add_new_contact_PhoneNumber
                ) && isEmptyField(add_new_contact_fixNumber) && isEmptyField(add_new_contact_Email)
            ) {
                val intent = Intent(this@AddNewContactActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                val alertDialog = AlertDialog.Builder(this, R.style.AlertDialog)
                alertDialog.setTitle(applicationContext.resources.getString(R.string.add_new_contact_alert_dialog_cancel_title))
                alertDialog.setMessage(applicationContext.resources.getString(R.string.add_new_contact_alert_dialog_cancel_message))

                alertDialog.setPositiveButton(R.string.alert_dialog_yes) { _, _ ->

                    startActivity(Intent(this@AddNewContactActivity, MainActivity::class.java))
                    finish()
                }

                alertDialog.setNegativeButton(R.string.alert_dialog_no) { _, _ ->
                }
                alertDialog.show()
            }
        }

        identifierHelpIcon?.setOnClickListener {
            MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                .setTitle(getString(R.string.add_new_contact_mail_identifier))
                .setView(R.layout.alert_dialog_mail_identifier_help)
                .setMessage(getString(R.string.add_new_contact_mail_identifier_help))
                .show()
        }

        saveContactButton?.setOnClickListener {
            if (add_new_contact_FirstName?.editText?.text.toString().isEmpty()) {
                Toast.makeText(
                    this, getString(R.string.add_new_contact_first_name_empty_field),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                if (add_new_contact_Priority?.selectedItemPosition == 2) {
                    if (nb_Contacts_VIP > 4 && contactsUnlimitedIsBought == false) {
                        MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                            .setTitle(getString(R.string.in_app_popup_nb_vip_max_message))
                            .setMessage(getString(R.string.in_app_popup_nb_vip_max_message))
                            .setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
                                startActivity(
                                    Intent(
                                        this@AddNewContactActivity,
                                        PremiumActivity::class.java
                                    )
                                )
                                finish()
                            }
                            .setNegativeButton(R.string.alert_dialog_later) { _, _ ->
                            }
                            .show()
                    } else {
                        val edit: SharedPreferences.Editor =
                            sharedNumberOfContactsVIPPreferences.edit()
                        edit.putInt("nb_Contacts_VIP", nb_Contacts_VIP + 1)
                        edit.apply()

                        CoroutineScope(Dispatchers.IO).launch {
                            val spinnerChar = NumberAndMailDB.convertSpinnerStringToChar(
                                add_new_contact_PhoneProperty?.selectedItem.toString(),
                                this@AddNewContactActivity
                            )
                            val mailSpinnerChar = NumberAndMailDB.convertSpinnerMailStringToChar(
                                add_new_contact_MailProperty?.selectedItem.toString(),
                                add_new_contact_Email?.editText?.text.toString(),
                                this@AddNewContactActivity
                            )

                            val contactData = ContactDB(
                                null,
                                add_new_contact_FirstName?.editText?.text.toString(),
                                add_new_contact_LastName?.editText?.text.toString(),
                                add_new_contact_Mail_Identifier?.editText?.text.toString(),
                                avatar,
                                add_new_contact_Priority?.selectedItemPosition!!,
                                add_new_contact_ImgString.toString(),
                                isFavorite,
                                "",
                                0,
                                R.raw.sms_ring.toString(),
                                0,
                                1,
                                ""
                            )

                            if (!isDuplicate(contactData)) {
                                val contactId = this@AddNewContactActivity.contactsViewModel.insertContact(contactData)

                                var contactDetailDB: ContactDetailDB
                                if (add_new_contact_PhoneNumber?.editText?.text.toString() != "") {
                                    contactDetailDB = ContactDetailDB(
                                        null,
                                        contactId?.toInt(),
                                        "" +
                                                add_new_contact_PhoneNumber?.editText?.text.toString(),
                                        "phone",
                                        spinnerChar,
                                        0
                                    )
                                    add_new_contact_ContactsDatabase?.contactDetailsDao()
                                        ?.insert(contactDetailDB)
                                }
                                if (add_new_contact_fixNumber?.editText?.text.toString() != "") {
                                    contactDetailDB = ContactDetailDB(
                                        null,
                                        contactId?.toInt(),
                                        "" +
                                                add_new_contact_fixNumber?.editText?.text.toString(),
                                        "phone",
                                        spinnerChar,
                                        1
                                    )
                                    add_new_contact_ContactsDatabase?.contactDetailsDao()
                                        ?.insert(contactDetailDB)
                                }
                                if (add_new_contact_Email?.editText?.text.toString() != "") {
                                    contactDetailDB = ContactDetailDB(
                                        null,
                                        contactId?.toInt(),
                                        "" +
                                                add_new_contact_Email?.editText?.text.toString(),
                                        "mail",
                                        mailSpinnerChar,
                                        2
                                    )
                                    add_new_contact_ContactsDatabase?.contactDetailsDao()
                                        ?.insert(contactDetailDB)
                                }

                                val intent = Intent(ContactsContract.Intents.Insert.ACTION).apply {
                                    type = ContactsContract.RawContacts.CONTENT_TYPE
                                }
                                intent.apply {
                                    putExtra(
                                        ContactsContract.Intents.Insert.NAME,
                                        add_new_contact_FirstName?.editText?.text.toString()
                                                + " " + add_new_contact_LastName?.editText?.text.toString()
                                    )

                                    putExtra(
                                        ContactsContract.Intents.Insert.EMAIL,
                                        add_new_contact_Email?.editText?.text.toString()
                                    )
                                    putExtra(
                                        ContactsContract.Intents.Insert.EMAIL_TYPE,
                                        ContactsContract.CommonDataKinds.Email.TYPE_WORK
                                    )
                                    putExtra(
                                        ContactsContract.Contacts.Photo.PHOTO,
                                        add_new_contact_ImgString!!
                                    )
                                    putExtra(
                                        ContactsContract.Intents.Insert.PHONE,
                                        add_new_contact_PhoneNumber?.editText?.text.toString()
                                    )
                                    putExtra(
                                        ContactsContract.Intents.Insert.PHONE_TYPE,
                                        ContactsContract.CommonDataKinds.Phone.TYPE_WORK
                                    )
                                }
                                isRetroFit = true
                                startActivity(intent)
                            } else {
                                confirmationDuplicate(contactData)
                            }
                        }
                    }
                } else {
                    CoroutineScope(Dispatchers.IO).launch {
                        val spinnerChar = NumberAndMailDB.convertSpinnerStringToChar(
                            add_new_contact_PhoneProperty?.selectedItem.toString(),
                            this@AddNewContactActivity
                        )
                        val mailSpinnerChar = NumberAndMailDB.convertSpinnerMailStringToChar(
                            add_new_contact_MailProperty?.selectedItem.toString(),
                            add_new_contact_Email?.editText?.text.toString(),
                            this@AddNewContactActivity
                        )

                        val contactData = ContactDB(
                            null,
                            add_new_contact_FirstName?.editText?.text.toString(),
                            add_new_contact_LastName?.editText?.text.toString(),
                            add_new_contact_Mail_Identifier?.editText?.text.toString(),
                            avatar,
                            add_new_contact_Priority?.selectedItemPosition!!,
                            add_new_contact_ImgString!!,
                            isFavorite,
                            "",
                            0,
                            R.raw.sms_ring.toString(),
                            0,
                            1,
                            ""
                        )

                        if (!isDuplicate(contactData)) {
                            val contactId = this@AddNewContactActivity.contactsViewModel.insertContact(contactData)

                            var contactDetailDB: ContactDetailDB
                            if (add_new_contact_PhoneNumber?.editText?.text.toString() != "") {
                                contactDetailDB = ContactDetailDB(
                                    null,
                                    contactId?.toInt(),
                                    "" +
                                            add_new_contact_PhoneNumber?.editText?.text.toString(),
                                    "phone",
                                    spinnerChar,
                                    0
                                )
                                add_new_contact_ContactsDatabase?.contactDetailsDao()
                                    ?.insert(contactDetailDB)
                            }
                            if (add_new_contact_fixNumber?.editText?.text.toString() != "") {
                                contactDetailDB = ContactDetailDB(
                                    null,
                                    contactId?.toInt(),
                                    "" +
                                            add_new_contact_fixNumber?.editText?.text.toString(),
                                    "phone",
                                    spinnerChar,
                                    1
                                )
                                add_new_contact_ContactsDatabase?.contactDetailsDao()
                                    ?.insert(contactDetailDB)
                            }
                            if (add_new_contact_Email?.editText?.text.toString() != "") {
                                contactDetailDB = ContactDetailDB(
                                    null,
                                    contactId?.toInt(),
                                    "" +
                                            add_new_contact_Email?.editText?.text.toString(),
                                    "mail",
                                    mailSpinnerChar,
                                    2
                                )
                                add_new_contact_ContactsDatabase?.contactDetailsDao()
                                    ?.insert(contactDetailDB)
                            }

                            val intent = Intent(ContactsContract.Intents.Insert.ACTION).apply {
                                type = ContactsContract.RawContacts.CONTENT_TYPE
                            }
                            intent.apply {
                                putExtra(
                                    ContactsContract.Intents.Insert.NAME,
                                    add_new_contact_FirstName?.editText?.text.toString()
                                            + " " + add_new_contact_LastName?.editText?.text.toString()
                                )

                                putExtra(
                                    ContactsContract.Intents.Insert.EMAIL,
                                    add_new_contact_Email?.editText?.text.toString()
                                )
                                putExtra(
                                    ContactsContract.Intents.Insert.EMAIL_TYPE,
                                    ContactsContract.CommonDataKinds.Email.TYPE_WORK
                                )
                                putExtra(
                                    ContactsContract.Contacts.Photo.PHOTO,
                                    add_new_contact_ImgString!!
                                )
                                putExtra(
                                    ContactsContract.Intents.Insert.PHONE,
                                    add_new_contact_PhoneNumber?.editText?.text.toString()
                                )
                                putExtra(
                                    ContactsContract.Intents.Insert.PHONE_TYPE,
                                    ContactsContract.CommonDataKinds.Phone.TYPE_WORK
                                )
                            }
                            isRetroFit = true
                            startActivity(intent)
                        } else {
                            confirmationDuplicate(contactData)
                        }
                    }
                }
            }
        }

        contactImage?.setOnClickListener {
            selectImage()
        }

        favoriteButton?.setOnClickListener {
            favoriteButton?.visibility = View.INVISIBLE
            unfavoriteButton?.visibility = View.VISIBLE

            isFavorite = 1
        }

        unfavoriteButton?.setOnClickListener {
            favoriteButton?.visibility = View.VISIBLE
            unfavoriteButton?.visibility = View.INVISIBLE

            isFavorite = 0
        }

        add_new_contact_layout.setOnTouchListener { _, _ ->
            val view = this@AddNewContactActivity.currentFocus
            val imm =
                this@AddNewContactActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (view != null) {
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
            true
        }

        //endregion

        val priorityList =
            arrayOf(getString(R.string.add_new_contact_priority_0), "Standard", "VIP")
        val arrayAdapter = ArrayAdapter(this, R.layout.spinner_item, priorityList)
        add_new_contact_Priority?.adapter = arrayAdapter
        add_new_contact_Priority?.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    when (position) {
                        0 -> {
                            add_new_contact_PriorityExplain?.text =
                                getString(R.string.add_new_contact_priority0)
                            contactImage?.visibility = View.GONE
                            contactImage?.setBorderColor(
                                resources.getColor(
                                    R.color.priorityZeroColor,
                                    null
                                )
                            )
                            contactImage?.setBetweenBorderColor(
                                resources.getColor(
                                    R.color.lightColor,
                                    null
                                )
                            )
                            contactImage?.visibility = View.VISIBLE
                        }
                        1 -> {
                            add_new_contact_PriorityExplain?.text =
                                getString(R.string.add_new_contact_priority1)
                            contactImage?.visibility = View.GONE
                            contactImage?.setBorderColor(
                                resources.getColor(
                                    R.color.priorityOneColor,
                                    null
                                )
                            )
                            contactImage?.setBetweenBorderColor(
                                resources.getColor(
                                    R.color.lightColor,
                                    null
                                )
                            )
                            contactImage?.visibility = View.VISIBLE
                        }
                        2 -> {
                            add_new_contact_PriorityExplain?.text =
                                getString(R.string.add_new_contact_priority2)
                            contactImage?.visibility = View.GONE
                            contactImage?.setBorderColor(
                                resources.getColor(
                                    R.color.priorityTwoColor,
                                    null
                                )
                            )
                            contactImage?.setBetweenBorderColor(
                                resources.getColor(
                                    R.color.lightColor,
                                    null
                                )
                            )
                            contactImage?.visibility = View.VISIBLE
                        }
                    }
                }
            }

        add_new_contact_Priority?.setSelection(1)
        contactImage?.setBorderColor(
            resources.getColor(
                R.color.priorityOneColor,
                null
            )
        )
        contactImage?.setBetweenBorderColor(
            resources.getColor(
                R.color.lightColor,
                null
            )
        )

        val phoneTagList = resources.getStringArray(R.array.add_new_contact_phone_number_arrays)
        val adapterPhoneTagList = ArrayAdapter(this, R.layout.spinner_item, phoneTagList)
//        array_adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        add_new_contact_PhoneProperty?.adapter = adapterPhoneTagList

        val mailTagList = resources.getStringArray(R.array.add_new_contact_mail_arrays)
        val adapterMailTagList = ArrayAdapter(this, R.layout.spinner_item, mailTagList)
//        array_adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        add_new_contact_MailProperty?.adapter = adapterMailTagList
        android.R.layout.simple_spinner_item
    }

    //region ========================================== Functions ===========================================

    private fun isDuplicate(contactData: ContactDB): Boolean {
        var isDuplicate = false
        this.contactsViewModel.allContacts.observe(this) { contactsDB ->
            for (contact in contactsDB) {
                if (contact.firstName == contactData.firstName && contact.lastName == contactData.lastName) {
                    isDuplicate = true
                    break
                }
            }
        }

        return isDuplicate
    }

    //region =========================================== Groups =============================================

    private fun addToFavorite(id: Int) {
        var alreadyExist = false

        groupsViewModel.getAllGroups().observe(this) { groups ->
            for (group in groups) {
                if (group.groupDB?.name == "Favorites") {
                    groupId = group.groupDB?.id!!
                    alreadyExist = true
                    break
                }
            }
        }

//        listContact.add(currentContact.contactDB)

        if (alreadyExist) {
            addContactToGroup(id, groupId)
        } else {
            createGroup(listContact)
        }
    }

    private fun createGroup(listContact: ArrayList<ContactDB?>) {
        CoroutineScope(Dispatchers.IO).launch {
            val groupId = groupsViewModel.insertGroup(GroupDB("Favorites", "", -500138))
            listContact.forEach { contactDb ->
                linkContactGroupViewModel.insert(LinkContactGroup(groupId.toInt(), contactDb?.id!!))
            }
        }
    }

    private fun addContactToGroup(contactId: Int, groupId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            linkContactGroupViewModel.insert(LinkContactGroup(groupId.toInt(), contactId))
        }
    }

    private fun removeContactFromGroup(contactId: Int, groupId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            linkContactGroupViewModel.deleteContactInGroup(contactId, groupId.toInt())
        }
    }

    //endregion

    /**
     *demande de confirmation de la création d'un contact en double
     * @param contactData [contactDB]
     */
    private fun confirmationDuplicate(contactData: ContactDB) {
        MaterialAlertDialogBuilder(this, R.style.AlertDialog)
            .setTitle(R.string.add_new_contact_alert_dialog_title)
            .setMessage(R.string.add_new_contact_alert_dialog_message)
            .setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
                CoroutineScope(Dispatchers.IO).launch {
                    this@AddNewContactActivity.contactsViewModel.insertContact(contactData)
                }
                val intent = Intent(this@AddNewContactActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            .setBackground(getDrawable(R.color.backgroundColor))
            .setNegativeButton(R.string.alert_dialog_no) { _, _ ->
            }
            .show()
    }

    /**
     *Retourne si le [TextInputLayout] passé en parametre n'a pas de texte
     * @param field [TextInputLayout]
     * @return [boolean]
     */
    private fun isEmptyField(field: TextInputLayout?): Boolean {
        return field!!.editText!!.text.toString().isEmpty()
    }

    /**
     * Lors du click sur l'image du contact  affichage du BottomSheetDialog contenant les option choix icone appareil photo ou gallerie
     */
    private fun selectImage() {
        val builderBottom = BottomSheetDialog(this)
        builderBottom.setContentView(R.layout.alert_dialog_select_contact_picture_layout)
        val gallery =
            builderBottom.findViewById<ConstraintLayout>(R.id.select_contact_picture_gallery_layout)
        val camera =
            builderBottom.findViewById<ConstraintLayout>(R.id.select_contact_picture_camera_layout)
        val recyclerView =
            builderBottom.findViewById<RecyclerView>(R.id.select_contact_picture_recycler_view)
        val layoutMananger =
            LinearLayoutManager(applicationContext, LinearLayoutManager.HORIZONTAL, false)

        recyclerView!!.layoutManager = layoutMananger

        val adapter =
            ContactIconeAdapter(this)
        recyclerView.adapter = adapter
        gallery!!.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1
                )
                builderBottom.dismiss()
            } else {
                val intent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                intent.type = "image/*"
                startActivityForResult(
                    Intent.createChooser(
                        intent,
                        this.getString(R.string.add_new_contact_intent_title)
                    ), SELECT_FILE!!
                )
                builderBottom.dismiss()
            }
        }
        camera!!.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    2
                )
                builderBottom.dismiss()
            } else {
                openCamera()
                builderBottom.dismiss()
            }
        }
        builderBottom.show()
    }

    /**
     * Ouvre l'appreil photo du téléphone
     */
    private fun openCamera() {
        val values = ContentValues()
        values.put(
            MediaStore.Images.Media.TITLE,
            getString(R.string.add_new_contact_camera_open_title)
        )
        values.put(
            MediaStore.Images.Media.DESCRIPTION,
            getString(R.string.add_new_contact_camera_open_description)
        )
        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE)
    }

    /**
     * Transforme l'Uri passer parametre en string contenant le path vers la ressource
     * @param context [Context]
     * @param  contentUri [Uri]
     * @return [String] //path
     */
    private fun getRealPathFromUri(context: Context, contentUri: Uri): String {
        var cursor: Cursor? = null
        try {
            val arrayOfMediaStore = arrayOf(MediaStore.Images.Media.DATA)
            cursor = context.contentResolver.query(contentUri, arrayOfMediaStore, null, null, null)
            val columnIndex = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            return cursor.getString(columnIndex)
        } finally {
            cursor?.close()
        }
    }

    /**
     * Récupère la photo prise depuis l'appreil ou la photo que le contact à sélectionner pour la rajouter au contact
     * @param requestCode [Int]
     * @param resultCode [Int]
     * @param data [Intent]
     */
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == IMAGE_CAPTURE_CODE) {

                val matrix = Matrix()
                val exif = ExifInterface(getRealPathFromUri(this, imageUri!!))
                val rotation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
                val rotationInDegrees = exifToDegrees(rotation)
                matrix.postRotate(rotationInDegrees.toFloat())
                var bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                bitmap =
                    Bitmap.createScaledBitmap(bitmap, bitmap.width / 10, bitmap.height / 10, true)
                bitmap =
                    Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                contactImage!!.setImageBitmap(bitmap)
                add_new_contact_ImgString = bitmapToBase64(bitmap)

            } else if (requestCode == SELECT_FILE) {
                val matrix = Matrix()
                val selectedImageUri = data!!.data
                val exif = ExifInterface(getRealPathFromUri(this, selectedImageUri!!))
                val rotation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
                val rotationInDegrees = exifToDegrees(rotation)
                matrix.postRotate(rotationInDegrees.toFloat())
                var bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)
                bitmap =
                    Bitmap.createScaledBitmap(bitmap, bitmap.width / 10, bitmap.height / 10, true)
                bitmap =
                    Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                contactImage!!.setImageBitmap(bitmap)
                add_new_contact_ImgString = bitmapToBase64(bitmap)
            }
        }
    }

    /**
     * retourne la rotation lors de la prise de photo
     * @param exifOrientation [Int]
     * @return [Int]
     */
    private fun exifToDegrees(exifOrientation: Int): Int {
        return when (exifOrientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    }

    /**
     * Méthode appelée par le système lorsque l'utilisateur accepte ou refuse une permission
     * Lorsque l'utilisateur autorise l'accès a ces fichiers nous ouvrons le dossier "Galerie"
     * Lorsque l'utilisateur autorise à l'appareil photo nous l'ouvrons
     * @param requestCode [Int]
     * @param permissions [Array<String>]
     * @param grantResults [IntArray]
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            startActivityForResult(
                Intent.createChooser(
                    intent,
                    this.getString(R.string.add_new_contact_intent_title)
                ), SELECT_FILE!!
            )
        } else if (requestCode == 2 && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openCamera()
        }
    }

    override fun onRestart() {
        super.onRestart()
        if (isRetroFit) {
            startActivity(Intent(this@AddNewContactActivity, MainActivity::class.java))
            finish()
        }
    }

    /**
     * Change l'image du contact par l'icone sélectionner par l'utilisateur
     * @param bitmap
     */
    fun addContactIcone(bitmap: Bitmap) {
        contactImage!!.setImageBitmap(bitmap)
        add_new_contact_ImgString = bitmapToBase64(bitmap)
    }

    //endregion
}