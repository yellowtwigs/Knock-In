package com.yellowtwigs.knockin.ui.edit_contact

import android.Manifest
import android.annotation.SuppressLint
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
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.ui.CircularImageView
import com.yellowtwigs.knockin.ui.groups.GroupEditAdapter
import com.yellowtwigs.knockin.ui.contacts.MainActivity
import com.yellowtwigs.knockin.ui.in_app.PremiumActivity
import com.yellowtwigs.knockin.ui.groups.GroupManagerActivity
import com.yellowtwigs.knockin.model.*
import com.yellowtwigs.knockin.model.data.*
import com.yellowtwigs.knockin.ui.contacts.ContactsViewModel
import com.yellowtwigs.knockin.ui.groups.GroupsViewModel
import com.yellowtwigs.knockin.ui.groups.LinkContactGroupViewModel
import com.yellowtwigs.knockin.utils.EveryActivityUtils.checkThemePreferences
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

/**
 * La Classe qui permet d'éditer un contact choisi
 * @author Florian Striebel, Kenzy Suon, Ryan Granet
 */
@AndroidEntryPoint
class EditContactDetailsActivity : AppCompatActivity() {

    //region ========================================== Var or Val ==========================================

    private var contactManager: ContactManager? = null

    private var edit_contact_ParentLayout: ConstraintLayout? = null

    private var firstNameInput: TextInputLayout? = null
    private var lastNameInput: TextInputLayout? = null
    private var phoneNumberInput: TextInputLayout? = null
    private var fixNumberInput: TextInputLayout? = null
    private var contact_vip_Settings: TextView? = null
    private var vipSettingsIcon: AppCompatImageView? = null

    private var mailInput: TextInputLayout? = null
    private var edit_contact_Mail_Name: TextInputLayout? = null
    private var edit_contact_Mail_Identifier_Help: AppCompatImageView? = null

    private var edit_contact_RoundedImageView: CircularImageView? = null
    private var prioritySpinner: Spinner? = null
    private var edit_contact_Priority_explain: TextView? = null
    private var phonePropertySpinner: Spinner? = null
    private var fixPropertySpinner: Spinner? = null
    private var mailPropertySpinner: Spinner? = null

    private var edit_contact_Return: AppCompatImageView? = null
    private var edit_contact_DeleteContact: AppCompatImageView? = null

    private var addToFavorite: AppCompatImageView? = null
    private var removeFavorite: AppCompatImageView? = null
    private var saveContact: AppCompatImageView? = null

    private var groupId: Long = 0
    private var listContact: ArrayList<ContactDB?> = ArrayList()

    private var contactId = 1
    private var firstName: String = ""
    private var lastName: String = ""
    private var phoneNumber: String = ""
    private var phoneProperty: String = ""
    private var fixNumber: String = ""
    private var edit_contact_fix_property: String = ""
    private var edit_contact_mail_property: String = ""
    private var edit_contact_mail: String = ""

    private var edit_contact_mail_name: String = ""
    private var edit_contact_rounded_image: Int = 0
    private var edit_contact_image64: String = ""
    private var edit_contact_priority: Int = 1

    private var alarmTone = 1

    private var edit_contact_GroupConstraintLayout: ConstraintLayout? = null

    // Database && Thread
    private var edit_contact_ContactsDatabase: ContactsDatabase? = null
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

    private var isFavorite = 0
    private var isFavoriteChanged = 0
    private var contactsUnlimitedIsBought: Boolean? = null

    private var position: Int? = null

    private var currentSound = ""
    private var isCustomSound = 0
    private var vipScheduleValue = 1
    private var hourLimit = ""
    private lateinit var currentContact: ContactWithAllInformation

    private val contactsViewModel: ContactsViewModel by viewModels()
    private val groupsViewModel: GroupsViewModel by viewModels()
    private val contactDetailsViewModel: ContactDetailsViewModel by viewModels()
    private val linkContactGroupViewModel: LinkContactGroupViewModel by viewModels()

    //endregion

    @SuppressLint("InflateParams", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkThemePreferences(this)

        setContentView(R.layout.activity_edit_contact_details)

        edit_contact_mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        edit_contact_mDbWorkerThread.start()

        //on get la base de données
        edit_contact_ContactsDatabase = ContactsDatabase.getDatabase(this)

        val sharedNumberOfContactsVIPPreferences: SharedPreferences =
            getSharedPreferences("nb_Contacts_VIP", Context.MODE_PRIVATE)
        val nb_Contacts_VIP = sharedNumberOfContactsVIPPreferences.getInt("nb_Contacts_VIP", 0)

        val sharedAlarmNotifInAppPreferences: SharedPreferences =
            getSharedPreferences("Contacts_Unlimited_Bought", Context.MODE_PRIVATE)
        contactsUnlimitedIsBought =
            sharedAlarmNotifInAppPreferences.getBoolean("Contacts_Unlimited_Bought", false)

        //region ========================================== Intent ==========================================

        contactId = intent.getIntExtra("ContactId", 1)
        alarmTone = intent.getIntExtra("AlarmTone", 1)
        position = intent.getIntExtra("position", 0)
        fromGroupActivity = intent.getBooleanExtra("fromGroupActivity", false)
        contactManager = ContactManager(this.applicationContext)

        //endregion

        //region ======================================= FindViewById =======================================

        edit_contact_ParentLayout = findViewById(R.id.edit_contact_parent_layout)
        firstNameInput = findViewById(R.id.edit_contact_first_name_id)
        lastNameInput = findViewById(R.id.edit_contact_last_name_id)
        contact_vip_Settings = findViewById(R.id.contact_vip_settings)
        vipSettingsIcon = findViewById(R.id.edit_contact_vip_settings)

        phoneNumberInput = findViewById(R.id.edit_contact_phone_number_id)
        fixNumberInput = findViewById(R.id.edit_contact_phone_number_fix_id)
        edit_contact_RoundedImageView = findViewById(R.id.edit_contact_rounded_image_view_id)
        mailInput = findViewById(R.id.edit_contact_mail_id)
        mailPropertySpinner = findViewById(R.id.edit_contact_mail_spinner_id)
        edit_contact_Mail_Name = findViewById(R.id.edit_contact_mail_id_edit_text)
        edit_contact_Mail_Identifier_Help = findViewById(R.id.edit_contact_mail_id_help)
        prioritySpinner = findViewById(R.id.edit_contact_priority)
        phonePropertySpinner = findViewById(R.id.edit_contact_phone_number_spinner)
        fixPropertySpinner = findViewById(R.id.edit_contact_phone_number_spinner_fix)
        edit_contact_Priority_explain = findViewById(R.id.edit_contact_priority_explain)

        recyclerGroup = findViewById(R.id.edit_contact_recycler)
        edit_contact_GroupConstraintLayout = findViewById(R.id.edit_contact_group_constraint_layout)

        edit_contact_Return = findViewById(R.id.edit_contact_return) // 1531651456
        edit_contact_DeleteContact = findViewById(R.id.edit_contact_delete) // 1531651455574546
        addToFavorite = findViewById(R.id.edit_contact_favorite)
        removeFavorite = findViewById(R.id.edit_contact_favorite_shine)
        saveContact = findViewById(R.id.edit_contact_edit_contact)

        //endregion

        //disable keyboard

        edit_contact_ParentLayout?.setOnTouchListener { _, _ ->
            val view = this@EditContactDetailsActivity.currentFocus
            val imm = this@EditContactDetailsActivity.getSystemService(
                Context.INPUT_METHOD_SERVICE
            ) as InputMethodManager
            if (view != null) {
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
            true
        }

        contactsViewModel.getContact(contactId).observe(this) { contact ->
            if (contact != null) {
                currentContact = contact
                firstName = contact.contactDB?.firstName.toString()
                lastName = contact.contactDB?.lastName.toString()
                val tmpPhone = contact.contactDetailList?.get(0)
                phoneNumber = tmpPhone?.content.toString()
                phoneProperty = tmpPhone?.tag.toString()
                val tmpMail = contact.contactDetailList?.get(1)
                edit_contact_mail = tmpMail?.content.toString()
                edit_contact_mail_property = tmpMail?.tag.toString()
                edit_contact_priority = contact.contactDB?.contactPriority!!
                edit_contact_mail_name = contact.contactDB?.mail_name.toString()
                edit_contact_image64 = contact.contactDB?.profilePicture64.toString()
                edit_contact_RoundedImageView?.setImageBitmap(base64ToBitmap(edit_contact_image64))

                removeFavorite?.isVisible = contact.contactDB?.favorite == 1
                addToFavorite?.isVisible = contact.contactDB?.favorite == 0
                isFavorite = contact.contactDB?.favorite!!
                isFavoriteChanged = contact.contactDB?.favorite!!

//            if (edit_contact_ContactsDatabase?.contactsDao()
//                    ?.getContact(contactId?.toInt()) == null
//            ) {
//            } else {
//                val executorService: ExecutorService = Executors.newFixedThreadPool(1)
//                val callDb = Callable {
//                    edit_contact_ContactsDatabase!!.contactsDao().getContact(contactId!!)
//                }
//                val result = executorService.submit(callDb)
//                val contact: ContactWithAllInformation = result.get()
//                firstName = contact.contactDB!!.firstName
//                lastName = contact.contactDB!!.lastName
//                edit_contact_priority = contact.contactDB!!.contactPriority
//                edit_contact_rounded_image =
//                    contactManager!!.randomDefaultImage(contact.contactDB!!.profilePicture, "Get")
//
//                edit_contact_mail_name = contact.contactDB!!.mail_name
//
//                phoneProperty = getString(R.string.edit_contact_phone_number_mobile)
//                fixNumber = getString(R.string.edit_contact_phone_number_home)
//                phoneNumber = ""
//                edit_contact_mail = ""
//                edit_contact_mail_property = getString(R.string.edit_contact_mail_mobile)
//
//                val tagPhone = contact.getPhoneNumberTag()
//                val phoneNumber = contact.getFirstPhoneNumber()
//                this.phoneNumber = phoneNumber
//                phoneProperty = tagPhone
//                val tagFix = contact.getSecondPhoneTag(phoneNumber)
//                val fixNumber = contact.getSecondPhoneNumber(phoneNumber)
//                this.fixNumber = fixNumber
//                edit_contact_fix_property = tagFix
//                val tagMail = contact.getMailTag()
//                val mail = contact.getFirstMail()
//                edit_contact_mail = mail
//                edit_contact_mail_property = tagMail
//
//                if (phoneNumber != "") {
//                    havePhone = true
//                }
//                if (fixNumber != "") {
//                    haveSecondPhone = true
//                }
//                if (mail != "") {
//                    haveMail = true
//                }
//
//                val id = contactId
//                val contactDB =
//                    edit_contact_ContactsDatabase?.contactsDao()?.getContact(id!!.toInt())
//                edit_contact_image64 = contactDB!!.contactDB!!.profilePicture64
//                if (edit_contact_image64 == "") {
//                    edit_contact_RoundedImageView!!.setImageResource(edit_contact_rounded_image)
//                } else {
//                    val image64 = edit_contact_image64
//                    edit_contact_RoundedImageView!!.setImageBitmap(base64ToBitmap(image64))
//                }
//
//                contactPriorityBorder(contact.contactDB!!, edit_contact_RoundedImageView!!, this)
//            }
            }
        }

        //region ===================================== SetViewDataField =====================================

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        firstNameInput?.editText?.setText(firstName)
        lastNameInput?.editText?.setText(lastName)
        phoneNumberInput?.editText?.setText(phoneNumber)
        fixNumberInput?.editText?.setText(fixNumber)
        mailInput?.editText?.setText(edit_contact_mail)
        edit_contact_Mail_Name?.editText?.setText(edit_contact_mail_name)
        mailPropertySpinner?.setSelection(
            getPosItemSpinner(
                edit_contact_mail_property,
                mailPropertySpinner!!
            )
        )
        phonePropertySpinner?.setSelection(
            getPosItemSpinner(
                phoneProperty,
                phonePropertySpinner!!
            )
        )
        fixPropertySpinner?.setSelection(
            getPosItemSpinner(
                edit_contact_fix_property,
                fixPropertySpinner!!
            )
        )
        textChanged(firstNameInput, firstNameInput?.editText?.text?.toString())
        textChanged(lastNameInput, lastNameInput?.editText?.text?.toString())
        textChanged(phoneNumberInput, phoneNumberInput?.editText?.text?.toString())
        textChanged(mailInput, mailInput?.editText?.text?.toString())
        textChanged(edit_contact_Mail_Name, edit_contact_Mail_Name?.editText?.text?.toString())

        //endregion

        if (intent.getBooleanExtra("hasChanged", false)) {
            firstNameInput?.editText?.setText(intent.getStringExtra("FirstName"))
            lastNameInput?.editText?.setText(intent.getStringExtra("Lastname"))
            phoneNumberInput?.editText?.setText(intent.getStringExtra("PhoneNumber"))
            fixNumberInput?.editText?.setText(intent.getStringExtra("FixNumber"))
            mailInput?.editText?.setText(intent.getStringExtra("Mail"))
            edit_contact_Mail_Name?.editText?.setText(intent.getStringExtra("MailId"))

            currentSound = intent.getStringExtra("currentSound").toString()

            removeFavorite?.isVisible = intent.getIntExtra("isFavorite", 0) == 1
            addToFavorite?.isVisible = intent.getIntExtra("isFavorite", 0) != 1
            isFavorite = intent.getIntExtra("isFavorite", 0)
            isFavoriteChanged = intent.getIntExtra("isFavorite", 0)
            isCustomSound = intent.getIntExtra("isCustomSound", 0)
            vipScheduleValue = intent.getIntExtra("vipScheduleValue", 1)
            hourLimit = intent.getStringExtra("hourLimit").toString()

            edit_contact_priority = 2
        }

        //region ========================================== Tags ============================================

        val phoneTagList = resources.getStringArray(R.array.edit_contact_phone_number_arrays)
        val adapterPhoneTagList = ArrayAdapter(this, R.layout.spinner_item, phoneTagList)

        val mailTagList = resources.getStringArray(R.array.edit_contact_mail_arrays)
        val adapterMailTagList = ArrayAdapter(this, R.layout.spinner_item, mailTagList)

        mailPropertySpinner?.adapter = adapterMailTagList
        phonePropertySpinner?.adapter = adapterPhoneTagList
        fixPropertySpinner?.adapter = adapterPhoneTagList

        //endregion

        //region ======================================== Priority ==========================================

        val priorityList =
            arrayOf(getString(R.string.add_new_contact_priority_0), "Standard", "VIP")
        val priority_adapter = ArrayAdapter(this, R.layout.spinner_item, priorityList)

        prioritySpinner?.adapter = priority_adapter
        prioritySpinner?.setSelection(edit_contact_priority)
        prioritySpinner?.onItemSelectedListener =
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
                            edit_contact_Priority_explain?.text =
                                getString(R.string.add_new_contact_priority0)
                            edit_contact_RoundedImageView?.visibility = View.GONE
                            edit_contact_RoundedImageView?.setBorderColor(
                                resources.getColor(
                                    R.color.priorityZeroColor,
                                    null
                                )
                            )
                            edit_contact_RoundedImageView?.setBetweenBorderColor(
                                resources.getColor(
                                    R.color.lightColor
                                )
                            )
                            edit_contact_RoundedImageView?.visibility = View.VISIBLE
                            vipSettingsIcon?.visibility = View.GONE
                            contact_vip_Settings?.visibility = View.GONE
                        }
                        1 -> {
                            edit_contact_Priority_explain?.text =
                                getString(R.string.add_new_contact_priority1)
                            edit_contact_RoundedImageView?.visibility = View.GONE
                            edit_contact_RoundedImageView?.setBorderColor(resources.getColor(R.color.priorityOneColor))
                            edit_contact_RoundedImageView?.setBetweenBorderColor(
                                resources.getColor(
                                    R.color.lightColor
                                )
                            )
                            edit_contact_RoundedImageView?.visibility = View.VISIBLE
                            vipSettingsIcon?.visibility = View.GONE
                            contact_vip_Settings?.visibility = View.GONE

                        }
                        2 -> {
                            edit_contact_Priority_explain?.text =
                                getString(R.string.add_new_contact_priority2)
                            edit_contact_RoundedImageView?.visibility = View.GONE
                            edit_contact_RoundedImageView?.setBorderColor(resources.getColor(R.color.priorityTwoColor))
                            edit_contact_RoundedImageView?.setBetweenBorderColor(
                                resources.getColor(
                                    R.color.lightColor
                                )
                            )
                            edit_contact_RoundedImageView?.visibility = View.VISIBLE

                            vipSettingsIcon?.isVisible = nb_Contacts_VIP <= 5
                            contact_vip_Settings?.isVisible = nb_Contacts_VIP <= 5

                            if (nb_Contacts_VIP > 4 &&
                                edit_contact_priority != prioritySpinner?.selectedItemPosition &&
                                contactsUnlimitedIsBought == false
                            ) {
                                MaterialAlertDialogBuilder(
                                    this@EditContactDetailsActivity,
                                    R.style.AlertDialog
                                )
                                    .setTitle(getString(R.string.in_app_popup_nb_vip_max_message))
                                    .setMessage(getString(R.string.in_app_popup_nb_vip_max_message))
                                    .setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
                                        startActivity(
                                            Intent(
                                                this@EditContactDetailsActivity,
                                                PremiumActivity::class.java
                                            )
                                        )
                                        finish()
                                    }
                                    .setNegativeButton(R.string.alert_dialog_later) { _, _ ->
                                    }
                                    .show()
                            }

                            if (nb_Contacts_VIP > 5 && contactsUnlimitedIsBought == true) {
                                vipSettingsIcon?.isVisible = true
                                contact_vip_Settings?.isVisible = true
                            }
                        }
                    }
                }
            }

        //endregion

        //region ========================================== Groups ==========================================

        CoroutineScope(Dispatchers.IO).launch {
            groupsViewModel.getGroupsForContact(contactId)
                .observe(this@EditContactDetailsActivity) { groupsDb ->
                    val groups = arrayListOf<GroupDB>()
                    groups.addAll(groupsDb)

                    val adapter = GroupEditAdapter(
                        this@EditContactDetailsActivity, groups, currentContact
                    )
                    CoroutineScope(Dispatchers.Main).launch {
                        recyclerGroup?.adapter = adapter
                        recyclerGroup?.layoutManager = LinearLayoutManager(
                            applicationContext,
                            LinearLayoutManager.HORIZONTAL,
                            false
                        )
                        if (groups.size > 0) {
                            edit_contact_GroupConstraintLayout?.visibility = View.VISIBLE
                        }
                    }
                }
        }

        //endregion

        //region ======================================== Listeners =========================================

        edit_contact_Return?.setOnClickListener {
            onBackPressed()
        }

        edit_contact_DeleteContact?.setOnClickListener {
            MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                .setTitle(getString(R.string.edit_contact_delete_contact))
                .setMessage(getString(R.string.edit_contact_delete_contact_message))
                .setPositiveButton(getString(R.string.edit_contact_validate)) { _, _ ->
                    CoroutineScope(Dispatchers.IO).launch {
                        currentContact.contactDB?.let { contactDb ->
                            contactsViewModel.deleteContact(
                                contactDb
                            )
                        }
                    }
                    val mainIntent =
                        Intent(this@EditContactDetailsActivity, MainActivity::class.java)
                    mainIntent.putExtra("isDelete", true)

                    if (edit_contact_priority == 2) {
                        val edit: SharedPreferences.Editor =
                            sharedNumberOfContactsVIPPreferences.edit()
                        edit.putInt("nb_Contacts_VIP", nb_Contacts_VIP - 1)
                        edit.apply()
                    }

                    startActivity(mainIntent)
                    finish()
                }
                .setNegativeButton(getString(R.string.edit_contact_cancel)) { _, _ ->
                }
                .show()
        }

        vipSettingsIcon?.setOnClickListener {
            if (checkIfADataWasChanged()) {
                val intentToVipSettings = Intent(this, VipSettingsActivity::class.java)
                contactsViewModel.setContactLiveData(currentContact)
                intentToVipSettings.apply {
                    putExtra("hasChanged", true)
                    putExtra("FirstName", firstNameInput?.editText?.text.toString())
                    putExtra("Lastname", lastNameInput?.editText?.text.toString())
                    putExtra("PhoneNumber", phoneNumberInput?.editText?.text.toString())
                    putExtra("FixNumber", fixNumberInput?.editText?.text.toString())
                    putExtra("Mail", mailInput?.editText?.text.toString())
                    putExtra("MailId", edit_contact_Mail_Name?.editText?.text.toString())
                    putExtra("Priority", prioritySpinner?.selectedItemPosition)
                    putExtra("isFavorite", isFavorite)
                }
                startActivity(intentToVipSettings)
            } else {
                val intentToVipSettings = Intent(this, VipSettingsActivity::class.java)
                intentToVipSettings.putExtra("ContactId", currentContact.getContactId())
                intentToVipSettings.putExtra("hasChanged", false)
                startActivity(intentToVipSettings)
            }
        }

        addToFavorite?.setOnClickListener {
            addToFavorite?.visibility = View.INVISIBLE
            removeFavorite?.visibility = View.VISIBLE

            isFavorite = 1
        }

        removeFavorite?.setOnClickListener {
            removeFavorite?.visibility = View.INVISIBLE
            addToFavorite?.visibility = View.VISIBLE

            isFavorite = 0
        }

        saveContact?.setOnClickListener {
            hideKeyboard()
            if (firstNameInput?.editText?.text.toString().isEmpty()) {
                Toast.makeText(
                    this,
                    getString(R.string.add_new_contact_first_name_empty_field),
                    Toast.LENGTH_LONG
                ).show()

            } else {
                if (checkIfADataWasChanged()) {
                    if (nb_Contacts_VIP > 4 && edit_contact_priority != prioritySpinner!!.selectedItemPosition && prioritySpinner!!.selectedItemPosition == 2 && contactsUnlimitedIsBought == false) {
                        MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                            .setTitle(getString(R.string.in_app_popup_nb_vip_max_message))
                            .setMessage(getString(R.string.in_app_popup_nb_vip_max_message))
                            .setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
                                startActivity(
                                    Intent(
                                        this@EditContactDetailsActivity,
                                        PremiumActivity::class.java
                                    )
                                )
                                finish()
                            }
                            .setNegativeButton(R.string.alert_dialog_later) { _, _ ->
                            }
                            .show()
                    } else {
                        if (edit_contact_priority != prioritySpinner!!.selectedItemPosition && prioritySpinner!!.selectedItemPosition == 2) {
                            val edit: SharedPreferences.Editor =
                                sharedNumberOfContactsVIPPreferences.edit()
                            edit.putInt("nb_Contacts_VIP", nb_Contacts_VIP + 1)
                            edit.apply()
                        }

                        if (edit_contact_priority != prioritySpinner!!.selectedItemPosition && edit_contact_priority == 2) {
                            val edit: SharedPreferences.Editor =
                                sharedNumberOfContactsVIPPreferences.edit()
                            edit.putInt("nb_Contacts_VIP", nb_Contacts_VIP - 1)
                            edit.apply()
                        }

                        MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                            .setTitle(R.string.edit_contact_alert_dialog_sync_contact_title)
                            .setMessage(R.string.edit_contact_alert_dialog_sync_contact_message)
                            .setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
                                editContactValidation()
                                editInAndroid = true
                                editInGoogle = true

                                val intentInsertEdit = Intent(Intent.ACTION_INSERT_OR_EDIT).apply {
                                    type = ContactsContract.Contacts.CONTENT_ITEM_TYPE

                                    putExtra(
                                        ContactsContract.Intents.Insert.NAME,
                                        firstNameInput?.editText!!.text.toString() + " " + lastNameInput?.editText!!.text.toString()
                                    )

                                    putExtra(
                                        ContactsContract.Intents.Insert.EMAIL,
                                        mailInput?.editText!!.text.toString()
                                    )
                                    putExtra(
                                        ContactsContract.Intents.Insert.EMAIL_TYPE,
                                        ContactsContract.CommonDataKinds.Email.TYPE_WORK
                                    )
                                    putExtra(
                                        ContactsContract.Contacts.Photo.PHOTO,
                                        edit_contact_image64
                                    )
                                    putExtra(
                                        ContactsContract.Intents.ATTACH_IMAGE,
                                        ContactsContract.CommonDataKinds.Email.TYPE_WORK
                                    )
                                    putExtra(
                                        ContactsContract.Intents.Insert.PHONE,
                                        phoneNumberInput?.editText!!.text.toString()
                                    )

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
                                    startActivity(
                                        Intent(
                                            this@EditContactDetailsActivity,
                                            GroupManagerActivity::class.java
                                        )
                                    )
                                    finish()
                                } else {
                                    startActivity(
                                        Intent(
                                            this@EditContactDetailsActivity,
                                            MainActivity::class.java
                                        ).putExtra("position", position!!)
                                    )
                                    finish()
                                }
                            }
                            .show()
                    }
                } else if (edit_contact_priority != prioritySpinner?.selectedItemPosition) {

                    if (nb_Contacts_VIP > 4 && prioritySpinner!!.selectedItemPosition == 2 && contactsUnlimitedIsBought == false) {
                        MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                            .setTitle(getString(R.string.in_app_popup_nb_vip_max_title))
                            .setMessage(getString(R.string.in_app_popup_nb_vip_max_message))
                            .setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
                                startActivity(
                                    Intent(
                                        this@EditContactDetailsActivity,
                                        PremiumActivity::class.java
                                    )
                                )
                                finish()
                            }
                            .setNegativeButton(R.string.alert_dialog_later) { _, _ ->
                            }
                            .show()
                    } else {
                        if (prioritySpinner?.selectedItemPosition == 2) {
                            val edit: SharedPreferences.Editor =
                                sharedNumberOfContactsVIPPreferences.edit()
                            edit.putInt("nb_Contacts_VIP", nb_Contacts_VIP + 1)
                            edit.apply()
                        } else if (edit_contact_priority
                            != prioritySpinner?.selectedItemPosition && edit_contact_priority == 2
                        ) {
                            val edit: SharedPreferences.Editor =
                                sharedNumberOfContactsVIPPreferences.edit()
                            edit.putInt("nb_Contacts_VIP", nb_Contacts_VIP - 1)
                            edit.apply()
                        }
                        editContactValidation()
                        if (fromGroupActivity) {
                            startActivity(
                                Intent(
                                    this@EditContactDetailsActivity,
                                    GroupManagerActivity::class.java
                                )
                            )
                            finish()
                        } else {
                            startActivity(
                                Intent(
                                    this@EditContactDetailsActivity,
                                    MainActivity::class.java
                                ).putExtra("position", position!!)
                            )
                            finish()
                        }
                    }
                } else {

                    if (fromGroupActivity) {
                        startActivity(
                            Intent(
                                this@EditContactDetailsActivity,
                                GroupManagerActivity::class.java
                            )
                        )
                        finish()
                    } else {
                        startActivity(
                            Intent(
                                this@EditContactDetailsActivity,
                                MainActivity::class.java
                            ).putExtra("position", position!!)
                        )
                        finish()
                    }
                }
            }
        }

        edit_contact_RoundedImageView?.setOnClickListener {
            selectImage()
        }

        //endregion
    }

    //region ========================================== Functions ===========================================

    //region ========================================== Lifecycle ===========================================

    override fun onRestart() {
        super.onRestart()
        if (editInAndroid) {
            if (fromGroupActivity) {
                startActivity(
                    Intent(
                        this@EditContactDetailsActivity,
                        GroupManagerActivity::class.java
                    ).putExtra("ContactId", contactId)
                )
                finish()
            } else {
                startActivity(
                    Intent(
                        this@EditContactDetailsActivity,
                        MainActivity::class.java
                    ).putExtra("ContactId", contactId).putExtra("position", position)
                )
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (editInGoogle) {
            if (fromGroupActivity) {
                startActivity(
                    Intent(
                        this@EditContactDetailsActivity,
                        GroupManagerActivity::class.java
                    ).putExtra("ContactId", contactId)
                )
                finish()
            } else {
                startActivity(
                    Intent(
                        this@EditContactDetailsActivity,
                        MainActivity::class.java
                    ).putExtra("ContactId", contactId).putExtra("position", position)
                )
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
                        startActivity(
                            Intent(
                                this@EditContactDetailsActivity,
                                GroupManagerActivity::class.java
                            )
                        )
                    } else {
                        startActivity(
                            Intent(
                                this@EditContactDetailsActivity,
                                MainActivity::class.java
                            ).putExtra("position", position)
                        )
                    }
                    finish()
                }
                .setNegativeButton(getString(R.string.alert_dialog_no)) { _, _ ->
                }
                .show()
        } else {
            if (fromGroupActivity) {
                startActivity(
                    Intent(
                        this@EditContactDetailsActivity,
                        GroupManagerActivity::class.java
                    )
                )
            } else {
                startActivity(
                    Intent(
                        this@EditContactDetailsActivity,
                        MainActivity::class.java
                    ).putExtra("position", position)
                )
            }
            finish()
        }
        hideKeyboard()
    }

    //endregion

    //region =========================================== CheckIf ============================================

    private fun checkIfADataWasChanged(): Boolean {
        return firstNameInput?.editText?.text.toString() != firstName ||
                lastNameInput?.editText?.text.toString() != lastName ||
                phoneNumberInput?.editText?.text.toString() != phoneNumber ||
                fixNumberInput?.editText?.text.toString() != fixNumber ||
                mailInput?.editText?.text.toString() != edit_contact_mail ||
                isFavorite != isFavoriteChanged || edit_contact_imgStringChanged ||
                edit_contact_Mail_Name?.editText?.text.toString() != edit_contact_mail_name ||
                prioritySpinner?.selectedItemPosition != edit_contact_priority
    }

    private fun checkIfNameFieldsAreNotEmpty(): Boolean {
        return firstNameInput?.editText?.toString() != "" ||
                firstNameInput?.editText?.toString()?.isNotBlank() == true ||
                firstNameInput?.editText?.toString()?.isNotEmpty() == true ||
                lastNameInput?.editText?.toString() != "" ||
                lastNameInput?.editText?.toString()?.isNotBlank() == true ||
                lastNameInput?.editText?.toString()?.isNotEmpty() == true
    }

    //endregion

    private fun createContactDetail(
        content: String, type: String, tag: String, fieldPosition: Int
    ) {
        val detail = ContactDetailDB(
            null, currentContact.getContactId(),
            content,
            type,
            tag,
            fieldPosition
        )
        contactDetailsViewModel.insert(detail)
    }

    private fun updateContactDetail(inputText: String, counter: Int): Int {
        return if (inputText.isNotEmpty() || inputText.isNotBlank() || inputText != "") {
            contactDetailsViewModel.updateContactDetail(
                currentContact.contactDetailList?.get(
                    counter
                )!!
            )
            currentContact.contactDetailList?.size!!
        } else {
            contactDetailsViewModel.deleteDetail(currentContact.contactDetailList?.get(counter)!!)
            currentContact.contactDetailList?.size!!
        }
    }

    private fun editContactDetailsInit() {
        val spinnerPhoneChar = NumberAndMailDB.convertSpinnerStringToChar(
            phonePropertySpinner?.selectedItem.toString(),
            this@EditContactDetailsActivity
        )
        val spinnerMailChar = NumberAndMailDB.convertSpinnerStringToChar(
            mailPropertySpinner?.selectedItem.toString(),
            this@EditContactDetailsActivity
        )

        val spinnerFixChar = NumberAndMailDB.convertSpinnerStringToChar(
            fixPropertySpinner?.selectedItem.toString(),
            this@EditContactDetailsActivity
        )

        val nbDetail = currentContact.contactDetailList?.size?.minus(1)

        if (havePhone) {
            val firstNumber = currentContact.getFirstPhoneNumber()
            var counter = 0
            while (counter < currentContact.contactDetailList?.size!!) {
                if (currentContact.contactDetailList?.get(counter)?.content == (firstNumber)) {
                    counter =
                        updateContactDetail(phoneNumberInput?.editText?.text.toString(), counter)
                }
                counter++
            }

            if (!haveMail && mailInput?.editText?.text.toString() != "") {
                createContactDetail(
                    mailInput?.editText?.text.toString(),
                    "mail",
                    spinnerMailChar,
                    2
                )
            }
            if (!haveSecondPhone && mailInput?.editText?.text.toString() != "") {
                createContactDetail(
                    fixNumberInput?.editText?.text.toString(),
                    "phone",
                    spinnerFixChar,
                    1
                )
            }

        } else if (!havePhone && phoneNumberInput?.editText?.text.toString() != "") {
            createContactDetail(
                phoneNumberInput?.editText?.text.toString(),
                "phone",
                spinnerPhoneChar,
                0
            )
        }

        if (haveSecondPhone) {
            val secondNumber =
                currentContact.getSecondPhoneNumber(currentContact.getFirstPhoneNumber())
            var counter = 0
            while (counter < currentContact.contactDetailList?.size!!) {
                if (currentContact.contactDetailList?.get(counter)?.content == secondNumber) {
                    counter =
                        updateContactDetail(fixNumberInput?.editText?.text.toString(), counter)
                }
                counter++
            }
        } else if (!haveSecondPhone && fixNumberInput?.editText?.text.toString() != "") {
            createContactDetail(
                fixNumberInput?.editText?.text.toString(),
                "phone",
                spinnerFixChar,
                1
            )
        }

        if (haveMail) {
            val firstMail = currentContact.getFirstMail()
            var counter = 0
            while (counter < currentContact.contactDetailList?.size!!) {
                if (currentContact.contactDetailList?.get(counter)?.content == firstMail) {
                    counter = updateContactDetail(mailInput?.editText?.text.toString(), counter)
                }
                counter++
            }
        } else if (mailInput?.editText?.text.toString() != "") {
            createContactDetail(
                mailInput?.editText?.text.toString(),
                "mail",
                spinnerMailChar,
                2
            )
        }

        if (nbDetail == -1 && mailInput?.editText?.text.toString() != "") {
            createContactDetail(
                mailInput?.editText?.text.toString(),
                "mail",
                spinnerMailChar,
                2
            )
        }
        if (nbDetail == -1 && phoneNumberInput?.editText?.text.toString() != "") {
            createContactDetail(
                phoneNumberInput?.editText?.text.toString(),
                "phone",
                spinnerPhoneChar,
                0
            )
        }
        if (nbDetail == -1 && fixNumberInput?.editText?.text.toString() != "") {
            createContactDetail(
                fixNumberInput?.editText?.text.toString(),
                "phone",
                spinnerPhoneChar,
                1
            )
        }
    }

    private fun editContactValidation() {
        CoroutineScope(Dispatchers.IO).launch {
            if (checkIfNameFieldsAreNotEmpty()) {
                editContactDetailsInit()

                if (isFavoriteChanged != isFavorite) {
                    if (isFavorite == 1) {
                        addToFavorite()
                    } else {
                        removeFromFavorite()
                    }
                }

                contactsViewModel.updateContact(
                    ContactDB(
                        contactId,
                        firstNameInput?.editText?.text.toString(),
                        lastNameInput?.editText?.text.toString(),
                        edit_contact_Mail_Name?.editText?.text.toString(),
                        0,
                        prioritySpinner?.selectedItemPosition!!,
                        edit_contact_imgString.toString(),
                        isFavorite,
                        "",
                        currentContact.contactDB?.hasWhatsapp!!,
                        currentSound,
                        isCustomSound,
                        vipScheduleValue,
                        hourLimit
                    )
                )
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@EditContactDetailsActivity,
                        R.string.edit_contact_toast,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun getPosItemSpinner(item: String, spinner: Spinner): Int {
        val tailleSpinner: Int = spinner.adapter.count
        for (x in 0 until tailleSpinner) {
            if (spinner.getItemAtPosition(x)
                    .toString() == NumberAndMailDB.convertStringToSpinnerString(item, this)
            ) {
                return x
            } else {
                println(
                    spinner.getItemAtPosition(x)
                        .toString() + "est diférent de " + NumberAndMailDB.convertStringToSpinnerString(
                        item,
                        this
                    )
                )
            }
        }
        return 0
    }

    //region ========================================== Favorites ===========================================

    private fun addToFavorite() {
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

        listContact.add(currentContact.contactDB)

        if (alreadyExist) {
            currentContact.contactDB?.id?.let { addContactToGroup(it, groupId) }
        } else {
            createGroup(listContact)
        }
    }

    private fun removeFromFavorite() {
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

        listContact.remove(currentContact.contactDB)
        removeContactFromGroup(contactId, groupId)

        groupsViewModel.getAllGroups().observe(this) { groups ->
            for (group in groups) {
                if (group.groupDB?.name == "Favorites") {
                    if (group.getListContact(this).isEmpty()) {
                        groupsViewModel.deleteGroup(group)
                        break
                    }
                }
            }
        }
    }

    //endregion

    //region =========================================== Groups =============================================

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

    private fun textChanged(textInput: TextInputLayout?, txt: String?) {
        textInput!!.editText!!.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
                isChanged = textInput.editText.toString() != txt
            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {

            }
        })
    }

    private fun selectImage() {
        val builderBottom = BottomSheetDialog(this)
        builderBottom.setContentView(R.layout.alert_dialog_select_contact_picture_layout)
        val gallery =
            builderBottom.findViewById<ConstraintLayout>(R.id.select_contact_picture_gallery_layout)
        val camera =
            builderBottom.findViewById<ConstraintLayout>(R.id.select_contact_picture_camera_layout)
        val recyclerView =
            builderBottom.findViewById<RecyclerView>(R.id.select_contact_picture_recycler_view)
        val layoutManager =
            LinearLayoutManager(applicationContext, LinearLayoutManager.HORIZONTAL, false)
        recyclerView!!.layoutManager = layoutManager
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
                ) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                val arrayListPermission = ArrayList<String>()
                arrayListPermission.add(Manifest.permission.CAMERA)
                arrayListPermission.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ActivityCompat.requestPermissions(
                    this,
                    arrayListPermission.toArray(arrayOfNulls<String>(arrayListPermission.size)),
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

    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, R.string.edit_contact_camera_open_title)
        values.put(
            MediaStore.Images.Media.DESCRIPTION,
            R.string.edit_contact_camera_open_description
        )
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
            val columnIndex = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            return cursor.getString(columnIndex)
        } finally {
            cursor?.close()
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == IMAGE_CAPTURE_CODE) {

                val matrix = Matrix()
                //check la rotation de base du téléphone pour l'appliquer à la photo pour qu'elle soit tout le temps dans le bon sens
                val exif = ExifInterface(getRealPathFromUri(this, imageUri!!))
                val rotation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
                val rotationInDegrees = exifToDegrees(rotation)
                matrix.postRotate(rotationInDegrees.toFloat())
                //convert l'imageUri en bitmap
                var bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                bitmap =
                    Bitmap.createScaledBitmap(bitmap, bitmap.width / 10, bitmap.height / 10, true)
                bitmap =
                    Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                //set l'image et la convertit en base64 pour la mettre plus tard dans la DB
                edit_contact_RoundedImageView!!.setImageBitmap(bitmap)
                edit_contact_imgString = bitmap.bitmapToBase64()
                edit_contact_imgStringChanged = true

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

    fun addContactIcon(bitmap: Bitmap) {
        edit_contact_RoundedImageView!!.setImageBitmap(bitmap)
        edit_contact_imgString = bitmap.bitmapToBase64()
        edit_contact_imgStringChanged = true
    }

    private fun hideKeyboard() {
        val view = this.currentFocus
        view?.let { v ->
            val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
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

    //endregion
}