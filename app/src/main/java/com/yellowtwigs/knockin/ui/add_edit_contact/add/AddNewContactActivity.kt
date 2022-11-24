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
import com.yellowtwigs.knockin.ui.CircularImageView
import com.yellowtwigs.knockin.ui.contacts.list.ContactsListActivity
import com.yellowtwigs.knockin.ui.add_edit_contact.IconAdapter
import com.yellowtwigs.knockin.ui.add_edit_contact.edit.EditContactViewModel
import com.yellowtwigs.knockin.ui.in_app.PremiumActivity
import com.yellowtwigs.knockin.utils.Converter.bitmapToBase64
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
    private var SELECT_FILE = 0
    private val IMAGE_CAPTURE_CODE = 1001

    private var isFavorite = 0
    private var contact: ContactDB? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkTheme(this)

        binding = ActivityEditContactBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.deleteContact.visibility = View.GONE

        numberOfContactsVIP = getSharedPreferences("nb_Contacts_VIP", Context.MODE_PRIVATE)
            .getInt("nb_Contacts_VIP", 0)

        contactsUnlimitedIsBought =
            getSharedPreferences("Contacts_Unlimited_Bought", Context.MODE_PRIVATE)
                .getBoolean("Contacts_Unlimited_Bought", false)

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
                randomDefaultImage(0, this, "Create"),
                this,
                "Get"
            )
        )
    }

    private fun setupPriority() {
        val priorityArray = arrayOf(getString(R.string.priority_0_title), "Standard", "VIP")
        val arrayAdapter = ArrayAdapter(this, R.layout.spinner_item, priorityArray)
        binding.apply {
            prioritySpinner.adapter = arrayAdapter
            prioritySpinner.setSelection(1)
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
                binding.vipSettingsIcon.visibility = View.GONE
                binding.vipSettingsText.visibility = View.GONE
            }
            1 -> {
                binding.priorityExplain.text = getString(R.string.priority_0_subtitle)
                binding.contactImage.setBorderColor(
                    resources.getColor(
                        R.color.priorityOneColor,
                        null
                    )
                )
                binding.vipSettingsIcon.visibility = View.GONE
                binding.vipSettingsText.visibility = View.GONE
            }
            2 -> {
                binding.priorityExplain.text = getString(R.string.priority_2_subtitle)
                binding.contactImage.setBorderColor(
                    resources.getColor(
                        R.color.priorityTwoColor,
                        null
                    )
                )

                binding.vipSettingsIcon.isVisible = numberOfContactsVIP <= 5
                binding.vipSettingsText.isVisible = numberOfContactsVIP <= 5

                if (numberOfContactsVIP > 4 && !contactsUnlimitedIsBought) {
                    MaterialAlertDialogBuilder(
                        this@AddNewContactActivity,
                        R.style.AlertDialog
                    )
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
                    favoriteContact.setImageDrawable(
                        ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.ic_star_selector,
                            null
                        )
                    )
                } else {
                    isFavorite = 1
                    favoriteContact.setImageDrawable(
                        ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.ic_star_shine,
                            null
                        )
                    )
                }
            }
            validate.setOnClickListener {
                hideKeyboard(this@AddNewContactActivity)
                contact = ContactDB(
                    0,
                    "${binding.firstNameInput.editText?.text.toString()} ${binding.lastNameInput.editText?.text.toString()}",
                    binding.firstNameInput.editText?.text.toString(),
                    binding.lastNameInput.editText?.text.toString(),
                    avatar,
                    contactImageString,
                    arrayListOf(
                        binding.phoneNumberInput.editText?.text.toString(),
                        binding.phoneNumberFixInput.editText?.text.toString()
                    ),
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

                CoroutineScope(Dispatchers.Default).launch {
                    if (editContactViewModel.checkDuplicateContact(contact!!)) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@AddNewContactActivity,
                                getString(R.string.add_new_contact_alert_dialog_message),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        retrofitMaterialDialog()
                    }
                }
            }

            mailIdHelp.setOnClickListener {
                MaterialAlertDialogBuilder(this@AddNewContactActivity, R.style.AlertDialog)
                    .setTitle(getString(R.string.add_new_contact_mail_identifier))
                    .setView(R.layout.alert_dialog_mail_identifier_help)
                    .setMessage(getString(R.string.add_new_contact_mail_identifier_help))
                    .show()
            }
            messengerIdHelp.setOnClickListener {
                MaterialAlertDialogBuilder(this@AddNewContactActivity, R.style.AlertDialog)
                    .setTitle(getString(R.string.messenger_identifier_title))
                    .setView(R.layout.alert_dialog_messenger_identifier_help)
                    .setMessage(getString(R.string.messenger_identifier_message))
                    .show()
            }

            contactImage.setOnClickListener {
                selectImage()
            }

            // Settings

        }
    }

    //endregion

    //region ============================================ UPDATE ============================================

    private suspend fun updateFavorite() {
        editContactViewModel.updateFavorite("${binding.firstNameInput.editText?.text.toString()} ${binding.lastNameInput.editText?.text.toString()}")
    }

    private fun retrofitMaterialDialog() {
        MaterialAlertDialogBuilder(this, R.style.AlertDialog)
            .setTitle(R.string.edit_contact_alert_dialog_sync_contact_title)
            .setMessage(R.string.edit_contact_alert_dialog_sync_contact_message)
            .setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
                editInAndroid = true
                editInGoogle = true

                CoroutineScope(Dispatchers.IO).launch {
                    editContactViewModel.addNewContact(contact!!)

                    if (isFavorite != 0) {
                        updateFavorite()
                    }
                    withContext(Dispatchers.Main) {
                        addNewUserToAndroidContacts()
                    }
                }
            }
            .setNegativeButton(R.string.alert_dialog_no) { _, _ ->
                CoroutineScope(Dispatchers.IO).launch {
                    editContactViewModel.addNewContact(contact!!)
                }

                goToContactsListActivity()
            }
            .show()
    }

    private fun addNewUserToAndroidContacts() {
        val intent = Intent(Intent.ACTION_INSERT_OR_EDIT).apply {
            type = ContactsContract.RawContacts.CONTENT_TYPE
        }
        binding.apply {
            intent.apply {
                putExtra(ContactsContract.Contacts.Photo.PHOTO_URI, imageUri)

                putExtra(
                    ContactsContract.Intents.Insert.NAME,
                    binding.firstNameInput.editText?.text.toString() + " " +
                            binding.lastNameInput.editText?.text.toString(),
                )

                putExtra(
                    ContactsContract.Intents.Insert.EMAIL,
                    binding.mailInput.editText?.text.toString()
                )
                putExtra(
                    ContactsContract.Intents.Insert.EMAIL_TYPE,
                    ContactsContract.CommonDataKinds.Email.TYPE_WORK
                )
                putExtra(
                    ContactsContract.Intents.Insert.PHONE,
                    binding.phoneNumberInput.editText?.text.toString()
                )
                putExtra(
                    ContactsContract.Intents.Insert.PHONE_TYPE,
                    ContactsContract.CommonDataKinds.Phone.TYPE_HOME
                )
                putExtra(
                    ContactsContract.Intents.Insert.SECONDARY_PHONE,
                    binding.phoneNumberFixInput.editText?.text.toString()
                )
                putExtra(
                    ContactsContract.Intents.Insert.EXTRA_DATA_SET,
                    binding.messengerIdInput.editText?.text.toString()
                )
            }
        }
        isRetroFit = true
        startActivity(intent)
    }

    //endregion

    //region ============================================ UTILS =============================================

    private fun goToContactsListActivity() {
        startActivity(Intent(this@AddNewContactActivity, ContactsListActivity::class.java))
        finish()
    }

    private fun checkIfADataWasChanged(): Boolean {
        binding.apply {
            return firstNameInput.editText?.text.toString().isNotEmpty() ||
                    lastNameInput.editText?.text.toString().isNotEmpty() ||
                    phoneNumberInput.editText?.text.toString().isNotEmpty() ||
                    phoneNumberFixInput.editText?.text.toString().isNotEmpty() ||
                    mailInput.editText?.text.toString().isNotEmpty() ||
                    mailIdInput.editText?.text.toString().isNotEmpty() ||
                    messengerIdInput.editText?.text.toString().isNotEmpty() ||
                    contactImageStringIsChanged
        }
    }

    private fun customOnBackPressed() {
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

    override fun onBackPressed() {
        customOnBackPressed()
    }

    //endregion

    //region ============================================ Camera =============================================

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

            gallery?.let {
                galleryClick(it, builderBottom)
            }
            camera?.let {
                cameraClick(it, builderBottom)
            }

            val adapter = IconAdapter(this@AddNewContactActivity)
            recyclerView?.adapter = adapter
            builderBottom.show()
        }
    }

    private fun galleryClick(gallery: ConstraintLayout, builderBottom: BottomSheetDialog) {
        gallery.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    this@AddNewContactActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this@AddNewContactActivity,
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
                        this@AddNewContactActivity.getString(R.string.add_new_contact_intent_title)
                    ), SELECT_FILE
                )
                builderBottom.dismiss()
            }
        }
    }

    private fun cameraClick(camera: ConstraintLayout, builderBottom: BottomSheetDialog) {
        camera.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    this@AddNewContactActivity,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    this@AddNewContactActivity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                val arrayListPermission = ArrayList<String>()
                arrayListPermission.add(Manifest.permission.CAMERA)
                arrayListPermission.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ActivityCompat.requestPermissions(
                    this@AddNewContactActivity,
                    arrayListPermission.toArray(arrayOfNulls<String>(arrayListPermission.size)),
                    2
                )
                builderBottom.dismiss()
            } else {
                openCamera()
                builderBottom.dismiss()
            }
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

    private fun exifToDegrees(exifOrientation: Int): Int {
        return when (exifOrientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    }

    fun addContactIcon(bitmap: Bitmap) {
        Log.i("PHOTO_URI", "bitmap : $bitmap")
        binding.contactImage.setImageBitmap(bitmap)
        contactImageString = bitmapToBase64(bitmap)
        contactImageStringIsChanged = true
    }

    //endregion

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == IMAGE_CAPTURE_CODE) {
                Log.i("PHOTO_URI", "IMAGE_CAPTURE_CODE")

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
                contactImageString = bitmapToBase64(bitmap)
                contactImageStringIsChanged = true
            } else if (requestCode == SELECT_FILE) {
                Log.i("PHOTO_URI", "SELECT_FILE")
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
                contactImageString = bitmapToBase64(bitmap)
            }
        }
    }

    override fun onRestart() {
        super.onRestart()
        if (isRetroFit) {
            goToContactsListActivity()
        }
    }
}