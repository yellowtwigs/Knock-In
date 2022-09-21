package com.yellowtwigs.knockin.ui.edit_contact

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
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
import com.google.android.material.textfield.TextInputLayout
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.ActivityEditContactBinding
import com.yellowtwigs.knockin.model.database.data.ContactDB
import com.yellowtwigs.knockin.ui.contacts.list.ContactsListActivity
import com.yellowtwigs.knockin.utils.Converter.base64ToBitmap
import com.yellowtwigs.knockin.utils.EveryActivityUtils.hideKeyboard
import com.yellowtwigs.knockin.utils.InitContactsForListAdapter.InitContactAdapter.contactPriorityBorder
import com.yellowtwigs.knockin.utils.RandomDefaultImage.randomDefaultImage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class EditContactActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditContactBinding

    private val editContactViewModel: EditContactViewModel by viewModels()

    private var isChanged = false
    private var editInAndroid = false
    private var editInGoogle = false

    private var imageUri: Uri? = null
    private var SELECT_FILE = 0
    private val IMAGE_CAPTURE_CODE = 1001

    private var isFavorite = 0

    private lateinit var currentContact: ContactDB

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

        binding = ActivityEditContactBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        val sharedNumberOfContactsVIPPreferences = getSharedPreferences("nb_Contacts_VIP", Context.MODE_PRIVATE)
//        val nb_Contacts_VIP = sharedNumberOfContactsVIPPreferences.getInt("nb_Contacts_VIP", 0)
//        val sharedAlarmNotifInAppPreferences: SharedPreferences =
//            getSharedPreferences("Contacts_Unlimited_Bought", Context.MODE_PRIVATE)
//        contactsUnlimitedIsBought =
//            sharedAlarmNotifInAppPreferences.getBoolean("Contacts_Unlimited_Bought", false)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        val id = intent.getIntExtra("contactId", 1)
        bindingAllDataFromUserId(id)
        actionOnClickListener()
    }

    //region =========================================== SETUP UI ===========================================

    private fun bindingAllDataFromUserId(id: Int) {
        editContactViewModel.getContactById(id).observe(this) { contact ->
            contact?.let {
                currentContact = it
                binding.apply {
                    if (it.profilePicture64 == "") {
                        contactImage.setImageResource(
                            randomDefaultImage(
                                contact.profilePicture,
                                this@EditContactActivity,
                                "Get"
                            )
                        )
                    } else {
                        contactImage.setImageBitmap(base64ToBitmap(contact.profilePicture64))
                    }

                    contactPriorityBorder(it.priority, contactImage, this@EditContactActivity)

                    firstNameInput.editText?.setText(it.firstName)

                    lastNameInput.editText?.setText(it.lastName)

                    if (it.listOfPhoneNumbers.isNotEmpty()) {
                        phoneNumberInput.editText?.setText(contact.listOfPhoneNumbers[0])

                        if (contact.listOfPhoneNumbers.size > 1) {
                            phoneNumberFixInput.editText?.setText(contact.listOfPhoneNumbers[1])
                        }
                    }

                    if (it.listOfPhoneNumbers.isNotEmpty()) {
                        mailInput.editText?.setText(contact.listOfMails[0])
                    }

                    mailIdInput.editText?.setText(it.mail_name)
                    messengerIdInput.editText?.setText(it.messengerId)

                    setupPriority(it.priority)

                    isFavorite = it.isFavorite
                    if (it.isFavorite == 1) {
                        favoriteContact.visibility = View.VISIBLE
                        favoriteContactShine.visibility = View.INVISIBLE
                    } else {
                        favoriteContact.visibility = View.INVISIBLE
                        favoriteContactShine.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun setupPriority(contactPriority: Int) {
        val priorityArray = arrayOf(getString(R.string.priority_0_title), "Standard", "VIP")
        val arrayAdapter = ArrayAdapter(this, R.layout.spinner_item, priorityArray)
        binding.apply {
            prioritySpinner.adapter = arrayAdapter
            prioritySpinner.setSelection(contactPriority)
            prioritySpinner.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) {
                    }

                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
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
                        R.color.priorityZeroColor,
                        null
                    )
                )
//                vipSettingsIcon?.visibility = View.GONE
//                contactVipSettingsText.visibility = View.GONE
            }
            1 -> {
                binding.priorityExplain.text = getString(R.string.priority_0_subtitle)
                binding.contactImage.setBorderColor(
                    resources.getColor(
                        R.color.priorityOneColor,
                        null
                    )
                )
//                vipSettingsIcon?.visibility = View.GONE
//                contactVipSettingsText.visibility = View.GONE
            }
            2 -> {
                binding.priorityExplain.text = getString(R.string.priority_2_subtitle)
                binding.contactImage.setBorderColor(
                    resources.getColor(
                        R.color.priorityTwoColor,
                        null
                    )
                )
//                vipSettingsIcon?.isVisible = nb_Contacts_VIP <= 5
//                contactVipSettingsText.isVisible = nb_Contacts_VIP <= 5
//                if (nb_Contacts_VIP > 4 &&
//                    edit_contact_priority != prioritySpinner?.selectedItemPosition &&
//                    contactsUnlimitedIsBought == false
//                ) {
//                    MaterialAlertDialogBuilder(
//                        this@EditContactDetailsActivity,
//                        R.style.AlertDialog
//                    )
//                        .setTitle(getString(R.string.in_app_popup_nb_vip_max_message))
//                        .setMessage(getString(R.string.in_app_popup_nb_vip_max_message))
//                        .setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
//                            startActivity(
//                                Intent(
//                                    this@EditContactDetailsActivity,
//                                    PremiumActivity::class.java
//                                )
//                            )
//                            finish()
//                        }
//                        .setNegativeButton(R.string.alert_dialog_later) { _, _ ->
//                        }
//                        .show()
//                }
//                if (nb_Contacts_VIP > 5 && contactsUnlimitedIsBought == true) {
//                    vipSettingsIcon?.isVisible = true
//                    contactVipSettingsText.isVisible = true
//                }
            }
        }
    }

    //endregion

    //region ======================================== ACTION LISTENER =======================================

    private fun actionOnClickListener() {
        binding.apply {
            // Toolbar
            returnIcon.setOnClickListener {
                onBackPressed()
            }
            deleteContact.setOnClickListener {
                MaterialAlertDialogBuilder(this@EditContactActivity, R.style.AlertDialog)
                    .setTitle(getString(R.string.edit_contact_delete_contact))
                    .setMessage(getString(R.string.edit_contact_delete_contact_message))
                    .setPositiveButton(getString(R.string.edit_contact_validate)) { _, _ ->
                        CoroutineScope(Dispatchers.IO).launch {
                            editContactViewModel.deleteContact(currentContact)

                            withContext(Dispatchers.Main) {
//                                if (edit_contact_priority == 2) {
//                                    val edit: SharedPreferences.Editor =
//                                        sharedNumberOfContactsVIPPreferences.edit()
//                                    edit.putInt("nb_Contacts_VIP", nb_Contacts_VIP - 1)
//                                    edit.apply()
//                                }
                                goToContactsListActivity()
                            }
                        }
                    }
                    .setNegativeButton(getString(R.string.edit_contact_cancel)) { _, _ ->
                    }
                    .show()
            }
            favoriteContact.setOnClickListener {
                isFavorite = 1
                favoriteContact.isVisible = isFavorite != 1
                favoriteContactShine.isVisible = isFavorite == 1
            }
            favoriteContactShine.setOnClickListener {
                isFavorite = 0
                favoriteContact.isVisible = isFavorite != 1
                favoriteContactShine.isVisible = isFavorite == 1
            }
            validate.setOnClickListener {
                hideKeyboard(this@EditContactActivity)

                CoroutineScope(Dispatchers.IO).launch {
                    updateUser()

                    withContext(Dispatchers.Main) {
                        goToContactsListActivity()
                    }
                }
            }

            // Helper
            mailIdHelp.setOnClickListener {
                MaterialAlertDialogBuilder(this@EditContactActivity, R.style.AlertDialog)
                    .setTitle(getString(R.string.add_new_contact_mail_identifier))
                    .setView(R.layout.alert_dialog_mail_identifier_help)
                    .setMessage(getString(R.string.add_new_contact_mail_identifier_help))
                    .show()
            }
            messengerIdHelp.setOnClickListener {
                MaterialAlertDialogBuilder(this@EditContactActivity, R.style.AlertDialog)
                    .setTitle(getString(R.string.messenger_identifier_title))
                    .setView(R.layout.alert_dialog_messenger_identifier_help)
                    .setMessage(getString(R.string.messenger_identifier_message))
                    .show()
            }

            // Contact Data
            contactImage.setOnClickListener {
                selectImage()
            }

            // Settings

        }
    }

    //endregion

    //region ============================================ UPDATE ============================================

    private suspend fun updateUser() {
        editContactViewModel.updateContact(
            ContactDB(
                currentContact.id,
                binding.firstNameInput.editText?.text.toString(),
                binding.lastNameInput.editText?.text.toString(),
                currentContact.profilePicture,
                currentContact.profilePicture64,
                arrayListOf(
                    binding.phoneNumberInput.editText?.text.toString(),
                    binding.phoneNumberFixInput.editText?.text.toString()
                ),
                arrayListOf(binding.mailIdInput.editText?.text.toString()),
                binding.mailIdInput.editText?.text.toString(),
                binding.prioritySpinner.selectedItemPosition,
                isFavorite,
                binding.messengerIdInput.editText?.text.toString(),
                currentContact.listOfMessagingApps,
                currentContact.notificationTone,
                currentContact.notificationSound,
                currentContact.isCustomSound,
                currentContact.vipSchedule,
                currentContact.hourLimitForNotification,
                currentContact.audioFileName
            )
        )
    }

    //endregion

    //region ============================================ UTILS =============================================

    private fun goToContactsListActivity() {
        startActivity(Intent(this@EditContactActivity, ContactsListActivity::class.java))
        finish()
    }

    private fun checkIfADataWasChanged(): Boolean {
        binding.apply {
            return firstNameInput.editText?.text.toString() != currentContact.firstName ||
                    lastNameInput.editText?.text.toString() != currentContact.lastName ||
                    phoneNumberInput.editText?.text.toString() != currentContact.listOfPhoneNumbers[0] ||
//                    phoneNumberFixInput.editText?.text.toString() != currentContact.firstName ||
                    mailInput.editText?.text.toString() != currentContact.listOfMails[0] ||
                    isFavorite != currentContact.isFavorite ||
//                    edit_contact_imgStringChanged ||
                    mailIdInput.editText?.text.toString() != currentContact.mail_name ||
                    messengerIdInput.editText?.text.toString() != currentContact.messengerId ||
                    prioritySpinner.selectedItemPosition != currentContact.priority
        }
    }

    override fun onBackPressed() {
        hideKeyboard(this)
        if (checkIfADataWasChanged()) {
            MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                .setTitle(R.string.edit_contact_alert_dialog_cancel_title)
                .setMessage(R.string.edit_contact_alert_dialog_cancel_message)
                .setBackground(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.color.backgroundColor,
                        null
                    )
                )
                .setPositiveButton(getString(R.string.alert_dialog_yes)) { _, _ ->
                    goToContactsListActivity()
                }
                .setNegativeButton(getString(R.string.alert_dialog_no)) { _, _ ->
                }
                .show()
        } else {
            goToContactsListActivity()
        }
    }

    private fun selectImage() {
        val builderBottom = BottomSheetDialog(this)
        builderBottom.apply {
            setContentView(R.layout.alert_dialog_select_contact_picture_layout)
            val gallery = findViewById<ConstraintLayout>(R.id.select_contact_picture_gallery_layout)
            val camera = findViewById<ConstraintLayout>(R.id.select_contact_picture_camera_layout)
            val recyclerView = findViewById<RecyclerView>(R.id.select_contact_picture_recycler_view)
            val layoutManager =
                LinearLayoutManager(applicationContext, LinearLayoutManager.HORIZONTAL, false)
            recyclerView?.layoutManager = layoutManager

            val adapter = ContactIconeAdapter(this@EditContactActivity)
            recyclerView?.adapter = adapter
            gallery?.setOnClickListener {
                if (ActivityCompat.checkSelfPermission(
                        this@EditContactActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this@EditContactActivity,
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
                            this@EditContactActivity.getString(R.string.add_new_contact_intent_title)
                        ), SELECT_FILE
                    )
                    builderBottom.dismiss()
                }
            }
            camera?.setOnClickListener {
                if (ActivityCompat.checkSelfPermission(
                        this@EditContactActivity,
                        Manifest.permission.CAMERA
                    ) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(
                        this@EditContactActivity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    val arrayListPermission = ArrayList<String>()
                    arrayListPermission.add(Manifest.permission.CAMERA)
                    arrayListPermission.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    ActivityCompat.requestPermissions(
                        this@EditContactActivity,
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
            val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor?.moveToFirst()
            return cursor?.getString(columnIndex!!)!!
        } finally {
            cursor?.close()
        }
    }

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
                binding.contactImage.setImageBitmap(bitmap)
//                edit_contact_imgString = bitmap.bitmapToBase64()
//                edit_contact_imgStringChanged = true
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
                binding.contactImage.setImageBitmap(bitmap)
//                edit_contact_imgString = bitmap.bitmapToBase64()
//                edit_contact_imgStringChanged = true
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
}