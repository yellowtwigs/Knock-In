package com.yellowtwigs.knockin.ui.add_edit_contact.edit

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
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
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.ActivityEditContactBinding
import com.yellowtwigs.knockin.model.database.data.ContactDB
import com.yellowtwigs.knockin.ui.add_edit_contact.IconAdapter
import com.yellowtwigs.knockin.ui.add_edit_contact.vip_settings.VipSettingsActivity
import com.yellowtwigs.knockin.ui.contacts.list.ContactsListActivity
import com.yellowtwigs.knockin.ui.groups.list.GroupsListActivity
import com.yellowtwigs.knockin.ui.premium.PremiumActivity
import com.yellowtwigs.knockin.utils.Converter
import com.yellowtwigs.knockin.utils.Converter.base64ToBitmap
import com.yellowtwigs.knockin.utils.EveryActivityUtils
import com.yellowtwigs.knockin.utils.EveryActivityUtils.hideKeyboard
import com.yellowtwigs.knockin.repositories.firebase.FirebaseViewModel
import com.yellowtwigs.knockin.ui.contacts.SingleContactViewState
import com.yellowtwigs.knockin.utils.InitContactsForListAdapter.InitContactAdapter.contactPriorityBorder
import com.yellowtwigs.knockin.utils.RandomDefaultImage.randomDefaultImage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.collections.ArrayList

@AndroidEntryPoint
class EditContactActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditContactBinding

    private val editContactViewModel: EditContactViewModel by viewModels()

    private var isChanged = false

    private var fromGroups = false
    private var editInAndroid = false
    private var editInGoogle = false
    private var isRetroFit = false

    private var numberOfContactsVIP = 0
    private var contactsUnlimitedIsBought = false

    private var imageUri: Uri? = null
    private val REQUEST_CODE_GALLERY = 1
    private val REQUEST_CODE_CAMERA = 2

    private var isFavorite = 0

    private var contactImageString = ""
    private var contactImageStringIsChanged = false

    private lateinit var currentContact: SingleContactViewState

    private var currentPhoneNumber1 = ""

    private var fromVipSettings = false

    private var notificationTone = ""
    private var notificationSound = R.raw.sms_ring
    private var isCustomSound = 0
    private var vipScheduleValue = 1
    private var hourLimit = ""
    private var audioFileName = ""
    private var fromVipSettingsDataChanged = false

    private val takePictureCallback = registerForActivityResult(ActivityResultContracts.TakePicture()) { successful ->
        if (successful) {
            imageUri?.let { uri ->
                Log.i("GetPhoto", "uri : $uri")
                val matrix = Matrix()
                val exif = ExifInterface(getRealPathFromUri(this, uri))
                val rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                val rotationInDegrees = exifToDegrees(rotation)
                matrix.postRotate(rotationInDegrees.toFloat())

                val options = BitmapFactory.Options().apply {
                    inSampleSize = 10
                }

                val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(uri), null, options)
                bitmap?.let {
                    val rotatedBitmap = Bitmap.createBitmap(it, 0, 0, it.width, it.height, matrix, true)

                    val outputStream = ByteArrayOutputStream()
                    rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    val base64String = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)

                    binding.contactImage.setImageBitmap(rotatedBitmap)
                    contactImageString = base64String
                    contactImageStringIsChanged = true
                }


//                val matrix = Matrix()
//                val exif = ExifInterface(getRealPathFromUri(this, uri))
//                val rotation = exif.getAttributeInt(
//                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
//                )
//                val rotationInDegrees = exifToDegrees(rotation)
//                matrix.postRotate(rotationInDegrees.toFloat())
//
//                var bitmap = uriToBitmap(uri)
//
//                Log.i("GetPhoto", "bitmap : $bitmap")
//                bitmap?.let {
//                    bitmap = Bitmap.createScaledBitmap(it, it.width / 10, it.height / 10, true)
//                    bitmap = Bitmap.createBitmap(it, 0, 0, it.width, it.height, matrix, true)
//
//                    binding.contactImage.setImageBitmap(it)
//                    contactImageString = Converter.bitmapToBase64(it)
//                    contactImageStringIsChanged = true
//                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        EveryActivityUtils.checkTheme(this)

        binding = ActivityEditContactBinding.inflate(layoutInflater)
        setContentView(binding.root)

        numberOfContactsVIP = getSharedPreferences("nb_Contacts_VIP", Context.MODE_PRIVATE).getInt("nb_Contacts_VIP", 0)

        contactsUnlimitedIsBought =
            getSharedPreferences("Contacts_Unlimited_Bought", Context.MODE_PRIVATE).getBoolean("Contacts_Unlimited_Bought", false)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        val id = intent.getIntExtra("ContactId", 1)

        fromVipSettings = intent.getBooleanExtra("fromVipSettings", false)

        fromGroups = intent.getBooleanExtra("FromGroups", false)
        bindingAllDataFromUserId(id)
        actionOnClickListener()
    }

    //region =============================================================== SETUP UI ===============================================================

    private fun bindingAllDataFromUserId(id: Int) {
        editContactViewModel.getSingleContactViewStateById(id).observe(this) { contact ->
            contact?.let {
                currentContact = it
                binding.apply {
                    if (it.profilePicture64 == "") {
                        contactImage.setImageResource(
                            randomDefaultImage(
                                contact.profilePicture, this@EditContactActivity, "Get"
                            )
                        )
                    } else {
                        contactImageString = contact.profilePicture64
                        contactImage.setImageBitmap(base64ToBitmap(contact.profilePicture64))
                    }

                    contactPriorityBorder(it.priority, contactImage, this@EditContactActivity)

                    firstNameInput.editText?.setText(it.firstName)
                    lastNameInput.editText?.setText(it.lastName)

                    if (it.listOfMails.isNotEmpty()) {
                        mailInput.editText?.setText(contact.listOfMails[0])
                    }

                    mailIdInput.editText?.setText(it.mail_name)
                    messengerIdInput.editText?.setText(it.messengerId)

                    setupPriority(it.priority)
                    currentPhoneNumber1 = it.firstPhoneNumber
                    binding.firstPhoneNumberContent.setText(it.firstPhoneNumber)
                    binding.firstNumberFlag.text = it.firstPhoneNumberFlag

                    isFavorite = it.isFavorite
                    if (it.isFavorite == 1) {
                        isFavorite = 1
                        favoriteContact.visibility = View.INVISIBLE
                        favoriteContact2.visibility = View.VISIBLE
                    } else {
                        isFavorite = 0
                        favoriteContact.visibility = View.VISIBLE
                        favoriteContact2.visibility = View.GONE
                    }
                }
            }

            if (intent.getBooleanExtra("hasChanged", false)) {
                binding.apply {
                    firstNameInput.editText?.setText(intent.getStringExtra("FirstName"))
                    lastNameInput.editText?.setText(intent.getStringExtra("Lastname"))

                    if (intent.getStringExtra("FirstPhoneNumber")?.contains(":") == true) {
                        firstPhoneNumberContent.setText(intent.getStringExtra("FirstPhoneNumber")?.split(":")?.get(1))
                    }

                    mailInput.editText?.setText(intent.getStringExtra("Mail"))
                    mailIdInput.editText?.setText(intent.getStringExtra("MailId"))
                    messengerIdInput.editText?.setText(intent.getStringExtra("MessengerId"))

                    if (intent.getIntExtra("isFavorite", 0) == 1) {
                        isFavorite = 1
                        favoriteContact.visibility = View.INVISIBLE
                        favoriteContact2.visibility = View.VISIBLE
                    } else {
                        isFavorite = 0
                        favoriteContact.visibility = View.VISIBLE
                        favoriteContact2.visibility = View.GONE
                    }
                    intent.putExtra("isFavorite", intent.getBooleanExtra("isFavorite", false))

                    setupPriority(2)
                }
            }
            if (fromVipSettings) {
                notificationSound = intent.getIntExtra("notification_Sound", 0)
                notificationTone = intent.getStringExtra("notificationTone").toString()
                isCustomSound = intent.getIntExtra("isCustomSound", 0)
                vipScheduleValue = intent.getIntExtra("vipScheduleValue", 1)
                hourLimit = intent.getStringExtra("hourLimit").toString()
                audioFileName = intent.getStringExtra("audioFileName").toString()
                fromVipSettingsDataChanged = intent.getBooleanExtra("vipSettingsHasChanged", false)
            }
        }
    }

    private fun setupPriority(contactPriority: Int) {
        val priorityArray = arrayOf(getString(R.string.priority_0_title), "Standard", "VIP")
        val arrayAdapter = ArrayAdapter(this, R.layout.spinner_item, priorityArray)
        binding.apply {
            prioritySpinner.adapter = arrayAdapter
            prioritySpinner.setSelection(contactPriority)
            prioritySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    changePriorityUI(position)
                }
            }

            contactImage.setBetweenBorderColor(resources.getColor(R.color.lightColor))
        }
    }

    private fun changePriorityUI(position: Int) {
        when (position) {
            0 -> {
                binding.priorityExplain.text = getString(R.string.priority_0_subtitle)
                binding.contactImage.setBorderColor(
                    resources.getColor(
                        R.color.priorityZeroColor, null
                    )
                )
                binding.vipSettingsIcon.visibility = View.GONE
                binding.vipSettingsText.visibility = View.GONE
            }

            1 -> {
                binding.priorityExplain.text = getString(R.string.priority_0_subtitle)
                binding.contactImage.setBorderColor(
                    resources.getColor(
                        R.color.priorityOneColor, null
                    )
                )
                binding.vipSettingsIcon.visibility = View.GONE
                binding.vipSettingsText.visibility = View.GONE
            }

            2 -> {
                binding.priorityExplain.text = getString(R.string.priority_2_subtitle)
                binding.contactImage.setBorderColor(
                    resources.getColor(
                        R.color.priorityTwoColor, null
                    )
                )

                binding.vipSettingsIcon.isVisible = numberOfContactsVIP <= 5
                binding.vipSettingsText.isVisible = numberOfContactsVIP <= 5

                if (numberOfContactsVIP > 4 && currentContact.priority != binding.prioritySpinner.selectedItemPosition && !contactsUnlimitedIsBought) {
                    MaterialAlertDialogBuilder(
                        this@EditContactActivity, R.style.AlertDialog
                    ).setTitle(getString(R.string.in_app_popup_nb_vip_max_message))
                        .setMessage(getString(R.string.in_app_popup_nb_vip_max_message))
                        .setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
                            startActivity(
                                Intent(
                                    this@EditContactActivity, PremiumActivity::class.java
                                )
                            )
                            finish()
                        }.setNegativeButton(R.string.alert_dialog_later) { _, _ ->
                        }.show()
                }
                if (numberOfContactsVIP > 5 && contactsUnlimitedIsBought) {
                    binding.vipSettingsIcon.isVisible = true
                    binding.vipSettingsText.isVisible = true
                }
            }
        }
    }

    //endregion

    //region ============================================================ ACTION LISTENER ===========================================================

    private fun actionOnClickListener() {
        binding.apply {
            returnIcon.setOnClickListener {
                onBackPressed()
            }
            deleteContact.setOnClickListener {
                MaterialAlertDialogBuilder(
                    this@EditContactActivity, R.style.AlertDialog
                ).setTitle(getString(R.string.edit_contact_delete_contact))
                    .setMessage(getString(R.string.edit_contact_delete_contact_message))
                    .setPositiveButton(getString(R.string.edit_contact_validate)) { _, _ ->
                        CoroutineScope(Dispatchers.IO).launch {
                            withContext(Dispatchers.Main) {
                                if (currentContact.priority == 2) {
                                    val edit = getSharedPreferences(
                                        "nb_Contacts_VIP", Context.MODE_PRIVATE
                                    ).edit()
                                    edit.putInt("nb_Contacts_VIP", numberOfContactsVIP - 1)
                                    edit.apply()
                                }
                                goToContactsOrGroups()
                            }

                            editContactViewModel.deleteContactById(currentContact.id)
                        }
                    }.setNegativeButton(getString(R.string.edit_contact_cancel)) { _, _ ->
                    }.show()
            }
            favoriteContact.setOnClickListener {
                if (isFavorite == 1) {
                    isFavorite = 0
                    favoriteContact.visibility = View.VISIBLE
                    favoriteContact2.visibility = View.GONE
                } else {
                    isFavorite = 1
                    favoriteContact.visibility = View.INVISIBLE
                    favoriteContact2.visibility = View.VISIBLE
                }
            }
            favoriteContact2.setOnClickListener {
                if (isFavorite == 1) {
                    isFavorite = 0
                    favoriteContact.visibility = View.VISIBLE
                    favoriteContact2.visibility = View.GONE
                } else {
                    isFavorite = 1
                    favoriteContact.visibility = View.INVISIBLE
                    favoriteContact2.visibility = View.VISIBLE
                }
            }
            phoneNumberInformations.setOnClickListener {
                MaterialAlertDialogBuilder(
                    this@EditContactActivity, R.style.AlertDialog
                ).setTitle(getString(R.string.add_new_contact_phone_number)).setMessage(getString(R.string.handle_one_phone_number_msg))
                    .setPositiveButton(R.string.start_activity_go_edition_positive_button) { _, _ ->
                    }.show()
            }
            validate.setOnClickListener {
                hideKeyboard(this@EditContactActivity)
                if (checkIfADataWasChanged() && !checkIfFieldsAreEmpty()) {
                    retrofitMaterialDialog()
                } else {
                    if (fromVipSettingsDataChanged) {
                        CoroutineScope(Dispatchers.IO).launch {
                            updateUser()
                            if (isFavorite != currentContact.isFavorite) {
                                updateFavorite()
                            }
                        }
                        goToContactsOrGroups()
                    } else {
                        goToContactsOrGroups()
                    }
                }
            }

            // Helper
            mailIdHelp.setOnClickListener {
                MaterialAlertDialogBuilder(
                    this@EditContactActivity, R.style.AlertDialog
                ).setTitle(getString(R.string.add_new_contact_mail_identifier)).setView(R.layout.alert_dialog_mail_identifier_help)
                    .setMessage(getString(R.string.add_new_contact_mail_identifier_help)).show()
            }
            messengerIdHelp.setOnClickListener {
                MaterialAlertDialogBuilder(
                    this@EditContactActivity, R.style.AlertDialog
                ).setTitle(getString(R.string.messenger_identifier_title)).setView(R.layout.alert_dialog_messenger_identifier_help)
                    .setMessage(getString(R.string.messenger_identifier_message)).show()
            }

            // Contact Data
            contactImage.setOnClickListener {
                selectImage()
            }

            if (EveryActivityUtils.checkIfGoEdition(this@EditContactActivity)) {
                binding.vipSettingsIcon.isVisible = false
                binding.vipSettingsText.isVisible = false
                binding.prioritySpinner.isVisible = false
                binding.priorityExplain.isVisible = false
                binding.messengerIdLayout.isVisible = false
                binding.mailIdLayout.isVisible = false
            }
            // Settings
            vipSettingsIcon.setOnClickListener {
                if (checkIfADataWasChanged()) {
                    val intentToVipSettings = Intent(this@EditContactActivity, VipSettingsActivity::class.java)
                    intentToVipSettings.apply {
                        putExtra("ContactId", currentContact.id)
                        putExtra("hasChanged", true)
                        putExtra("FirstName", firstNameInput.editText?.text.toString())
                        putExtra("Lastname", lastNameInput.editText?.text.toString())
                        putExtra("FirstPhoneNumber", "${firstPhoneNumberContent.text?.toString()}")
                        putExtra("SecondPhoneNumber", "${firstPhoneNumberContent.text?.toString()}")
                        putExtra("Mail", mailInput.editText?.text.toString())
                        putExtra("MailId", mailIdInput.editText?.text.toString())
                        putExtra("MessengerId", messengerIdInput.editText?.text.toString())
                        putExtra("Priority", prioritySpinner.selectedItemPosition)
                        putExtra("isFavorite", isFavorite)
                    }
                    startActivity(intentToVipSettings)
                } else {
                    val intentToVipSettings = Intent(this@EditContactActivity, VipSettingsActivity::class.java)
                    intentToVipSettings.putExtra("ContactId", currentContact.id)
                    intentToVipSettings.putExtra("hasChanged", false)
                    startActivity(intentToVipSettings)
                }
            }
        }
    }

    //endregion

    //region ================================================================ UPDATE ================================================================

    private suspend fun updateUser() {
        val fullName = if (isStringTotallyEmpty(binding.firstNameInput.editText?.text.toString())) {
            binding.lastNameInput.editText?.text.toString()
        } else {
            if (isStringTotallyEmpty(binding.lastNameInput.editText?.text.toString())) {
                binding.firstNameInput.editText?.text.toString()
            } else {
                "${binding.firstNameInput.editText?.text.toString()} ${binding.lastNameInput.editText?.text.toString()}"
            }
        }

        val listOfPhoneNumbers = arrayListOf("${binding.firstPhoneNumberContent.text?.toString()}")

        currentContact.androidId?.toString()?.let {

            if (binding.prioritySpinner.selectedItemPosition == 2) {
                setSendToVoicemailFlag(it, contentResolver, 0)
            } else {
                setSendToVoicemailFlag(it, contentResolver, 1)
            }
        }

        val contact = ContactDB(
            currentContact.id,
            currentContact.androidId,
            fullName,
            binding.firstNameInput.editText?.text.toString(),
            binding.lastNameInput.editText?.text.toString(),
            currentContact.profilePicture,
            contactImageString,
            listOfPhoneNumbers,
            arrayListOf(binding.mailInput.editText?.text.toString()),
            binding.mailIdInput.editText?.text.toString(),
            binding.prioritySpinner.selectedItemPosition,
            isFavorite,
            binding.messengerIdInput.editText?.text.toString(),
            currentContact.listOfMessagingApps,
            notificationTone,
            notificationSound,
            isCustomSound,
            vipScheduleValue,
            hourLimit,
            audioFileName
        )

        editContactViewModel.updateContact(contact)
    }

    private fun setSendToVoicemailFlag(contactId: String, resolver: ContentResolver, isVoiceMail: Int) {
        val contentValues = ContentValues()
        contentValues.put(ContactsContract.Contacts.SEND_TO_VOICEMAIL, 0)

        val where = ContactsContract.Contacts._ID + " = ?"
        val whereArgs = arrayOf(contactId)

        resolver.update(ContactsContract.Contacts.CONTENT_URI, contentValues, where, whereArgs)
    }

    private fun isStringTotallyEmpty(value: String): Boolean {
        return value.isEmpty() || value.isBlank() || value == ""
    }

    private suspend fun updateFavorite() {
        editContactViewModel.updateFavorite(currentContact.id.toString())
    }

    private fun retrofitMaterialDialog() {
        MaterialAlertDialogBuilder(this, R.style.AlertDialog).setTitle(R.string.edit_contact_alert_dialog_sync_contact_title)
            .setMessage(R.string.edit_contact_alert_dialog_sync_contact_message).setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
                editInAndroid = true
                editInGoogle = true

                addNewUserToAndroidContacts()
                CoroutineScope(Dispatchers.IO).launch {
                    updateUser()
                    if (isFavorite != currentContact.isFavorite) {
                        updateFavorite()
                    }
                }
            }.setNegativeButton(R.string.alert_dialog_no) { _, _ ->
                CoroutineScope(Dispatchers.IO).launch {
                    updateUser()
                    if (isFavorite != currentContact.isFavorite) {
                        updateFavorite()
                    }
                }

                goToContactsOrGroups()
            }.show()
    }

    private fun addNewUserToAndroidContacts() {
        val intentInsertEdit = Intent(Intent.ACTION_INSERT_OR_EDIT).apply {
            type = ContactsContract.Contacts.CONTENT_ITEM_TYPE

            putExtra(
                ContactsContract.Intents.Insert.NAME,
                binding.firstNameInput.editText?.text.toString() + " " + binding.lastNameInput.editText?.text.toString(),
            )
            putExtra(
                ContactsContract.Intents.Insert.EMAIL, binding.mailInput.editText?.text.toString()
            )
            putExtra(
                ContactsContract.Intents.Insert.EMAIL_TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK
            )
            putExtra(
                ContactsContract.Contacts.Photo.PHOTO, contactImageString
            )

            putExtra(ContactsContract.Intents.Insert.PHONE, binding.firstPhoneNumberContent.text?.toString())
            putExtra(
                ContactsContract.Intents.Insert.EXTRA_DATA_SET, binding.messengerIdInput.editText?.text.toString()
            )
        }
        isRetroFit = true
        startActivity(intentInsertEdit)
    }

    //endregion

    //region ================================================================ UTILS =================================================================

    private fun goToContactsOrGroups() {
        if (fromGroups) {
            startActivity(Intent(this@EditContactActivity, GroupsListActivity::class.java))
        } else {
            startActivity(Intent(this@EditContactActivity, ContactsListActivity::class.java))
        }
        finish()
    }

    private fun checkIfADataWasChanged(): Boolean {
        binding.apply {
            return firstNameInput.editText?.text.toString() != currentContact.firstName || lastNameInput.editText?.text.toString() != currentContact.lastName || currentPhoneNumber1 != firstPhoneNumberContent.text?.toString() || mailInput.editText?.text.toString() != currentContact.listOfMails[0] || isFavorite != currentContact.isFavorite || mailIdInput.editText?.text.toString() != currentContact.mail_name || contactImageStringIsChanged || messengerIdInput.editText?.text.toString() != currentContact.messengerId || prioritySpinner.selectedItemPosition != currentContact.priority
        }
    }

    private fun checkIfFieldsAreEmpty(): Boolean {
        binding.apply {
            return isStringTotallyEmpty(firstNameInput.editText?.text.toString()) && isStringTotallyEmpty(lastNameInput.editText?.text.toString()) || isStringTotallyEmpty(
                firstPhoneNumberContent.text.toString()
            )
        }
    }

    override fun onBackPressed() {
        hideKeyboard(this)
        if (checkIfADataWasChanged() || fromVipSettingsDataChanged) {
            MaterialAlertDialogBuilder(this, R.style.AlertDialog).setTitle(R.string.edit_contact_alert_dialog_cancel_title)
                .setMessage(R.string.edit_contact_alert_dialog_cancel_message).setBackground(
                    ResourcesCompat.getDrawable(
                        resources, R.color.backgroundColor, null
                    )
                ).setPositiveButton(getString(R.string.alert_dialog_yes)) { _, _ ->
                    goToContactsOrGroups()
                }.setNegativeButton(getString(R.string.alert_dialog_no)) { _, _ ->
                }.show()
        } else {
            goToContactsOrGroups()
        }
    }

    private fun selectImage() {
        val builderBottom = BottomSheetDialog(this)
        builderBottom.apply {
            setContentView(R.layout.alert_dialog_select_contact_picture_layout)
            val gallery = findViewById<ConstraintLayout>(R.id.select_contact_picture_gallery_layout)
            val camera = findViewById<ConstraintLayout>(R.id.select_contact_picture_camera_layout)
            val recyclerView = findViewById<RecyclerView>(R.id.select_contact_picture_recycler_view)
            val layoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.HORIZONTAL, false)
            recyclerView?.layoutManager = layoutManager

            val adapter = IconAdapter(this@EditContactActivity, this)
            recyclerView?.adapter = adapter
            gallery?.setOnClickListener {
                if (ActivityCompat.checkSelfPermission(
                        this@EditContactActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this@EditContactActivity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE_GALLERY
                    )
                    builderBottom.dismiss()
                } else {
                    openGallery()
                    builderBottom.dismiss()
                }
            }
            camera?.setOnClickListener {
                if (ActivityCompat.checkSelfPermission(
                        this@EditContactActivity, Manifest.permission.CAMERA
                    ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                        this@EditContactActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    val arrayListPermission = ArrayList<String>()
                    arrayListPermission.add(Manifest.permission.CAMERA)
                    arrayListPermission.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    ActivityCompat.requestPermissions(
                        this@EditContactActivity,
                        arrayListPermission.toArray(arrayOfNulls<String>(arrayListPermission.size)),
                        REQUEST_CODE_CAMERA
                    )
                    builderBottom.dismiss()
                } else {
                    openCamera()
                    builderBottom.dismiss()
                }
            }
            builderBottom.show()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(
            Intent.createChooser(intent, this@EditContactActivity.getString(R.string.add_new_contact_intent_title)),
            REQUEST_CODE_GALLERY
        )
    }

    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, R.string.edit_contact_camera_open_title)
        values.put(MediaStore.Images.Media.DESCRIPTION, R.string.edit_contact_camera_open_description)
        imageUri = null
        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        try {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            startActivityForResult(cameraIntent, REQUEST_CODE_CAMERA)
        } catch (e: Exception) {
            Log.i("PHOTO_URI", "Exception : $e")
        }
    }

    private fun getRealPathFromUri(context: Context, contentUri: Uri): String {
        var cursor: Cursor? = null
        try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = context.contentResolver.query(contentUri, proj, null, null, null)
            val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor?.moveToFirst()
            return cursor?.getString(columnIndex!!)!!
        } finally {
            cursor?.close()
        }
    }

    fun addContactIcon(bitmap: Bitmap) {
        binding.contactImage.setImageBitmap(bitmap)
        contactImageString = Converter.bitmapToBase64(bitmap)
        contactImageStringIsChanged = true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_GALLERY) {
            openGallery()
        } else if (requestCode == REQUEST_CODE_CAMERA) {
            openCamera()
        }
    }

    @Deprecated("Deprecated in Java")
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_GALLERY) {
                CoroutineScope(Dispatchers.IO).launch {
                    data?.data?.let {
                        imageUri = it
                        val matrix = Matrix()
                        val exif = ExifInterface(getRealPathFromUri(this@EditContactActivity, it))
                        val rotation = exif.getAttributeInt(
                            ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
                        )
                        val rotationInDegrees = exifToDegrees(rotation)
                        matrix.postRotate(rotationInDegrees.toFloat())

                        var bitmap = MediaStore.Images.Media.getBitmap(contentResolver, it)
                        bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.width / 10, bitmap.height / 10, true)
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

                        withContext(Dispatchers.Main) {
                            binding.contactImage.setImageURI(it)
                        }
                        contactImageString = Converter.bitmapToBase64(bitmap)
                        contactImageStringIsChanged = true
                    }
                }
            } else if (requestCode == REQUEST_CODE_CAMERA) {
                CoroutineScope(Dispatchers.IO).launch {
                    imageUri?.let { uri ->
                        val matrix = Matrix()
                        val exif = ExifInterface(getRealPathFromUri(this@EditContactActivity, uri))
                        val rotation = exif.getAttributeInt(
                            ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
                        )
                        val rotationInDegrees = exifToDegrees(rotation)
                        matrix.postRotate(rotationInDegrees.toFloat())
                        var bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                        bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.width / 10, bitmap.height / 10, true)
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

                        withContext(Dispatchers.Main) {
                            binding.contactImage.setImageURI(uri)
                        }

                        Log.i("PHOTO_URI", "Camera - binding.contactImage.setImageURI(uri) : ${binding.contactImage.setImageURI(uri)}")

                        contactImageString = Converter.bitmapToBase64(bitmap)
                        contactImageStringIsChanged = true
                    }
                }
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

    //endregion

    override fun onRestart() {
        super.onRestart()
        if (isRetroFit) {
            goToContactsOrGroups()
        }
    }

    override fun onResume() {
        super.onResume()
        if (editInGoogle) {
            goToContactsOrGroups()
        }
    }
}