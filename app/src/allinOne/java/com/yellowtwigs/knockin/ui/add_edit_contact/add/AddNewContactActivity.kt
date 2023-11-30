package com.yellowtwigs.knockin.ui.add_edit_contact.add

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
import android.provider.ContactsContract
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
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
import com.yellowtwigs.knockin.ui.contacts.list.ContactsListActivity
import com.yellowtwigs.knockin.ui.add_edit_contact.IconAdapter
import com.yellowtwigs.knockin.ui.add_edit_contact.edit.EditContactViewModel
import com.yellowtwigs.knockin.utils.Converter
import com.yellowtwigs.knockin.utils.Converter.bitmapToBase64
import com.yellowtwigs.knockin.utils.EveryActivityUtils
import com.yellowtwigs.knockin.utils.EveryActivityUtils.checkTheme
import com.yellowtwigs.knockin.utils.EveryActivityUtils.hideKeyboard
import com.yellowtwigs.knockin.utils.RandomDefaultImage.randomDefaultImage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class AddNewContactActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditContactBinding

    private val editContactViewModel: EditContactViewModel by viewModels()

    private var isChanged = false
    private var editInAndroid = false
    private var editInGoogle = false
    private var isRetroFit = false

    private var numberOfContactsVIP = 0
    private var contactsUnlimitedIsBought = false

    private var contactImageString = ""
    private var contactImageStringIsChanged = false

    private var avatar = 0

    private var imageUri: Uri? = null
    private val REQUEST_CODE_GALLERY = 1
    private val REQUEST_CODE_CAMERA = 2

    private var isFavorite = 0
    private var contact: ContactDB? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkTheme(this)

        binding = ActivityEditContactBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.deleteContact.visibility = View.GONE

        numberOfContactsVIP = getSharedPreferences("nb_Contacts_VIP", Context.MODE_PRIVATE).getInt("nb_Contacts_VIP", 0)

        contactsUnlimitedIsBought =
            getSharedPreferences("Contacts_Unlimited_Bought", Context.MODE_PRIVATE).getBoolean("Contacts_Unlimited_Bought", false)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        setupUi()
        actionOnClickListener()
        setupPriority()
    }

    //region =========================================== SETUP UI ===========================================

    private fun setupUi() {
        avatar = randomDefaultImage(0, this, "Create")
        binding.contactImage.setImageResource(
            randomDefaultImage(
                randomDefaultImage(0, this, "Create"), this, "Get"
            )
        )
    }

    private fun setupPriority() {
        val priorityArray = arrayOf(getString(R.string.priority_0_title), "Standard", "VIP")
        val arrayAdapter = ArrayAdapter(this, R.layout.spinner_item, priorityArray)
        binding.apply {
            prioritySpinner.adapter = arrayAdapter
            prioritySpinner.setSelection(1)
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

                if (numberOfContactsVIP > 4 && !contactsUnlimitedIsBought) {
                    MaterialAlertDialogBuilder(
                        this@AddNewContactActivity, R.style.AlertDialog
                    ).setTitle(getString(R.string.in_app_popup_nb_vip_max_message))
                        .setMessage(getString(R.string.in_app_popup_nb_vip_max_message))
                        .setPositiveButton(R.string.alert_dialog_yes) { _, _ ->

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

    //region ======================================== ACTION LISTENER =======================================

    private fun actionOnClickListener() {
        binding.apply {
            // Toolbar
            returnIcon.setOnClickListener {
                customOnBackPressed()
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
                    this@AddNewContactActivity, R.style.AlertDialog
                ).setTitle(getString(R.string.add_new_contact_phone_number)).setMessage(getString(R.string.handle_one_phone_number_msg))
                    .setPositiveButton(R.string.start_activity_go_edition_positive_button) { _, _ ->
                    }.show()
            }
            validate.setOnClickListener {
                hideKeyboard(this@AddNewContactActivity)

                if (isStringTotallyEmpty(binding.firstNameInput.editText?.text.toString()) && isStringTotallyEmpty(binding.lastNameInput.editText?.text.toString())) {
                    Toast.makeText(this@AddNewContactActivity, "You should make a name for this contact", Toast.LENGTH_SHORT).show()
                } else {
                    if (isStringTotallyEmpty(binding.firstPhoneNumberContent.text.toString())) {
                        Toast.makeText(this@AddNewContactActivity, "You should add at least one phone number", Toast.LENGTH_SHORT).show()
                    } else {
                        val listOfPhoneNumbers = listOf(binding.firstPhoneNumberContent.text?.toString() ?: "")

                        val fullName = if (isStringTotallyEmpty(binding.firstNameInput.editText?.text.toString())) {
                            binding.lastNameInput.editText?.text.toString()
                        } else {
                            if (isStringTotallyEmpty(binding.lastNameInput.editText?.text.toString())) {
                                binding.firstNameInput.editText?.text.toString()
                            } else {
                                "${binding.firstNameInput.editText?.text.toString()} ${binding.lastNameInput.editText?.text.toString()}"
                            }
                        }

                        contact = ContactDB(
                            0,
                            null,
                            fullName,
                            binding.firstNameInput.editText?.text.toString(),
                            binding.lastNameInput.editText?.text.toString(),
                            avatar,
                            contactImageString,
                            listOfPhoneNumbers,
                            arrayListOf(binding.mailInput.editText?.text.toString()),
                            binding.mailIdInput.editText?.text.toString(),
                            binding.prioritySpinner.selectedItemPosition,
                            isFavorite,
                            binding.messengerIdInput.editText?.text.toString(),
                            arrayListOf(),
                            "",
                            R.raw.sms_ring,
                            1,
                            0,
                            "",
                            ""
                        )

                        CoroutineScope(Dispatchers.Main).launch {
                            if (editContactViewModel.checkDuplicateContact(contact!!)) {
                                Toast.makeText(
                                    this@AddNewContactActivity, getString(R.string.add_new_contact_alert_dialog_message), Toast.LENGTH_LONG
                                ).show()
                            } else {
                                retrofitMaterialDialog()
                            }
                        }
                    }
                }
            }

            mailIdHelp.setOnClickListener {
                MaterialAlertDialogBuilder(
                    this@AddNewContactActivity, R.style.AlertDialog
                ).setTitle(getString(R.string.add_new_contact_mail_identifier)).setView(R.layout.alert_dialog_mail_identifier_help)
                    .setMessage(getString(R.string.add_new_contact_mail_identifier_help)).show()
            }
            messengerIdHelp.setOnClickListener {
                MaterialAlertDialogBuilder(
                    this@AddNewContactActivity, R.style.AlertDialog
                ).setTitle(getString(R.string.messenger_identifier_title)).setView(R.layout.alert_dialog_messenger_identifier_help)
                    .setMessage(getString(R.string.messenger_identifier_message)).show()
            }

            contactImage.setOnClickListener {
                selectImage()
            }

            // Settings
            if (EveryActivityUtils.checkIfGoEdition(this@AddNewContactActivity)) {
                binding.vipSettingsIcon.isVisible = false
                binding.vipSettingsText.isVisible = false
                binding.prioritySpinner.isVisible = false
                binding.priorityExplain.isVisible = false
                binding.messengerIdLayout.isVisible = false
                binding.mailIdLayout.isVisible = false
            }
        }
    }

    //endregion

    //region ============================================ UPDATE ============================================

    private suspend fun updateFavorite() {
        editContactViewModel.updateFavorite("${binding.firstNameInput.editText?.text.toString()} ${binding.lastNameInput.editText?.text.toString()}")
    }

    private fun retrofitMaterialDialog() {
        MaterialAlertDialogBuilder(this, R.style.AlertDialog).setTitle(R.string.edit_contact_alert_dialog_sync_contact_title)
            .setMessage(R.string.edit_contact_alert_dialog_sync_contact_message).setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
                editInAndroid = true
                editInGoogle = true

                addNewUserToAndroidContacts()

                CoroutineScope(Dispatchers.IO).launch {
                    editContactViewModel.addNewContact(contact!!)

                    if (isFavorite != 0) {
                        updateFavorite()
                    }
                }
            }.setNegativeButton(R.string.alert_dialog_no) { _, _ ->
                CoroutineScope(Dispatchers.IO).launch {
                    editContactViewModel.addNewContact(contact!!)
                }

                goToContactsListActivity()
            }.show()
    }

    private fun addNewUserToAndroidContacts() {
        val intent = Intent(Intent.ACTION_INSERT_OR_EDIT).apply {
            type = ContactsContract.RawContacts.CONTENT_ITEM_TYPE

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
            putExtra(ContactsContract.Contacts.Photo.PHOTO, contactImageString)
            putExtra(
                ContactsContract.Intents.Insert.PHONE, binding.firstPhoneNumberContent.text?.toString()
            )
            putExtra(
                ContactsContract.Intents.Insert.EXTRA_DATA_SET, binding.messengerIdInput.editText?.text.toString()
            )
        }

        isRetroFit = true
        startActivity(intent)
    }

    //endregion

    //region ============================================ UTILS =============================================

    private fun isStringTotallyEmpty(value: String): Boolean {
        return value.isEmpty() || value.isBlank() || value == ""
    }

    private fun goToContactsListActivity() {
        startActivity(Intent(this@AddNewContactActivity, ContactsListActivity::class.java))
        finish()
    }

    private fun checkIfDataAreEmpty(): Boolean {
        binding.apply {
            return firstNameInput.editText?.text.toString().isNotEmpty() || lastNameInput.editText?.text.toString()
                .isNotEmpty() || firstPhoneNumberContent.text?.toString()?.isNotEmpty() == true || mailInput.editText?.text.toString()
                .isNotEmpty() || mailIdInput.editText?.text.toString().isNotEmpty() || messengerIdInput.editText?.text.toString()
                .isNotEmpty() || contactImageStringIsChanged
        }
    }

    private fun customOnBackPressed() {
        hideKeyboard(this)
        if (checkIfDataAreEmpty()) {
            MaterialAlertDialogBuilder(this, R.style.AlertDialog).setTitle(R.string.edit_contact_alert_dialog_cancel_title)
                .setMessage(R.string.edit_contact_alert_dialog_cancel_message).setBackground(
                    ResourcesCompat.getDrawable(
                        resources, R.color.backgroundColor, null
                    )
                ).setPositiveButton(getString(R.string.alert_dialog_yes)) { _, _ ->
                    goToContactsListActivity()
                }.setNegativeButton(getString(R.string.alert_dialog_no)) { _, _ ->
                }.show()
        } else {
            goToContactsListActivity()
        }
    }

    override fun onBackPressed() {
        customOnBackPressed()
    }

    //endregion

    private fun selectImage() {
        val builderBottom = BottomSheetDialog(this)
        builderBottom.apply {
            setContentView(R.layout.alert_dialog_select_contact_picture_layout)
            val gallery = findViewById<ConstraintLayout>(R.id.select_contact_picture_gallery_layout)
            val camera = findViewById<ConstraintLayout>(R.id.select_contact_picture_camera_layout)
            val recyclerView = findViewById<RecyclerView>(R.id.select_contact_picture_recycler_view)
            val layoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.HORIZONTAL, false)
            recyclerView?.layoutManager = layoutManager

            val adapter = IconAdapter(this@AddNewContactActivity, this)
            recyclerView?.adapter = adapter
            gallery?.setOnClickListener {
                if (ActivityCompat.checkSelfPermission(
                        this@AddNewContactActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this@AddNewContactActivity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE_GALLERY
                    )
                    builderBottom.dismiss()
                } else {
                    openGallery()
                    builderBottom.dismiss()
                }
            }
            camera?.setOnClickListener {
                if (ActivityCompat.checkSelfPermission(
                        this@AddNewContactActivity, Manifest.permission.CAMERA
                    ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                        this@AddNewContactActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    val arrayListPermission = ArrayList<String>()
                    arrayListPermission.add(Manifest.permission.CAMERA)
                    arrayListPermission.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    ActivityCompat.requestPermissions(
                        this@AddNewContactActivity,
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
            Intent.createChooser(intent, this@AddNewContactActivity.getString(R.string.add_new_contact_intent_title)), REQUEST_CODE_GALLERY
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
                        val exif = ExifInterface(getRealPathFromUri(this@AddNewContactActivity, it))
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
                        contactImageString = bitmapToBase64(bitmap)
                        contactImageStringIsChanged = true
                    }
                }
            } else if (requestCode == REQUEST_CODE_CAMERA) {
                CoroutineScope(Dispatchers.IO).launch {
                    imageUri?.let { uri ->
                        val matrix = Matrix()
                        val exif = ExifInterface(getRealPathFromUri(this@AddNewContactActivity, uri))
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

    override fun onRestart() {
        super.onRestart()
        if (isRetroFit) {
            goToContactsListActivity()
        }
    }
}