package com.example.knocker.controller

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
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Base64
import android.view.*
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import com.example.knocker.*
import com.example.knocker.model.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.layout_notification_pop_up.*
import com.example.knocker.model.ModelDB.ContactWithAllInformation
import kotlinx.android.synthetic.main.knocker_infos.view.*
import java.io.ByteArrayOutputStream
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class EditContactActivity : AppCompatActivity() {

    //region ========================================== Var or Val ==========================================

    private var edit_contact_ParentLayout: ConstraintLayout? = null

    private var edit_contact_FirstName: TextInputLayout? = null
    private var edit_contact_LastName: TextInputLayout? = null
    private var edit_contact_PhoneNumber: TextInputLayout? = null
    private var edit_contact_Mail: TextInputLayout? = null


    private var edit_contact_FirstNameEditText: EditText? = null


    private var edit_contact_RoundedImageView: ImageView? = null
    private var edit_contact_Priority: Spinner? = null
    private var edit_contact_Priority_explain: TextView? = null
    private var edit_contact_Phone_Property: Spinner? = null
    private var edit_contact_Mail_Property: Spinner? = null
    private var edit_contact_AddFieldButton: Button? = null

    private var customAdapterEditText: CustomAdapterEditText? = null

    private var edit_contact_id: Int? = null
    private var edit_contact_first_name: String = ""
    private var edit_contact_last_name: String = ""
    private var edit_contact_phone_number: String = ""
    private var edit_contact_phone_property: String = ""
    private var edit_contact_mail_property: String = ""
    private var edit_contact_mail: String = ""
    private var edit_contact_rounded_image: Int = 0
    private var edit_contact_image64: String = ""
    private var edit_contact_priority: Int = 1

    // Database && Thread
    private var edit_contact_ContactsDatabase: ContactsRoomDatabase? = null
    private lateinit var edit_contact_mDbWorkerThread: DbWorkerThread

    private var isChanged = false

    var imageUri: Uri? = null
    private var SELECT_FILE: Int? = 0
    private val IMAGE_CAPTURE_CODE = 1001
    private var edit_contact_imgString: String? = null;

    //endregion

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        //endregion

        //region ======================================= FindViewByID =======================================

        edit_contact_ParentLayout = findViewById(R.id.edit_contact_parent_layout)
        edit_contact_FirstName = findViewById(R.id.edit_contact_first_name_id)
        edit_contact_LastName = findViewById(R.id.edit_contact_last_name_id)
        edit_contact_PhoneNumber = findViewById(R.id.edit_contact_phone_number_id)
        edit_contact_RoundedImageView = findViewById(R.id.edit_contact_rounded_image_view_id)
        edit_contact_Mail = findViewById(R.id.edit_contact_mail_id)
        edit_contact_Mail_Property = findViewById(R.id.edit_contact_mail_spinner_id)
        edit_contact_Priority = findViewById(R.id.edit_contact_priority)
        edit_contact_Phone_Property = findViewById(R.id.edit_contact_phone_number_spinner)
        edit_contact_Priority_explain = findViewById(R.id.edit_contact_priority_explain)
        edit_contact_AddFieldButton= findViewById(R.id.edit_contact_add_field_button)

        edit_contact_AddFieldButton = findViewById(R.id.edit_contact_add_field_button)

        if (edit_contact_ContactsDatabase?.contactsDao()?.getContact(edit_contact_id!!.toInt()) == null) {
            var contactList: List<ContactWithAllInformation>?
            val contactString = FakeContact.loadJSONFromAsset(this)
            contactList = FakeContact.buildList(contactString)


            var contact = FakeContact.getContactId(edit_contact_id!!, contactList)!!
            edit_contact_first_name = contact.contactDB!!.firstName
            edit_contact_last_name = contact.contactDB!!.lastName
            var tmpPhone = contact.contactDetailList!!.get(0)
            edit_contact_phone_number = NumberAndMailDB.numDBAndMailDBtoDisplay(tmpPhone.content)
            edit_contact_phone_property = NumberAndMailDB.extractStringFromNumber(tmpPhone.content)
            var tmpMail = contact.contactDetailList!!.get(1)
            edit_contact_mail = NumberAndMailDB.numDBAndMailDBtoDisplay(tmpMail.content)
            edit_contact_mail_property = NumberAndMailDB.extractStringFromNumber(tmpMail.content)
            edit_contact_priority = contact.contactDB!!.contactPriority
            edit_contact_image64 = contact.contactDB!!.profilePicture64
            edit_contact_RoundedImageView!!.setImageBitmap(base64ToBitmap(edit_contact_image64.toString()))
        } else {

            val executorService: ExecutorService = Executors.newFixedThreadPool(1)
            val callDb = Callable { edit_contact_ContactsDatabase!!.contactsDao().getContact(edit_contact_id!!) }
            val result = executorService.submit(callDb)
            val contact: ContactWithAllInformation = result.get()
            edit_contact_first_name = contact.contactDB!!.firstName
            edit_contact_last_name = contact.contactDB!!.lastName
            edit_contact_priority = contact.contactDB!!.contactPriority
            //TODO :enlever code Dupliquer

            if (contact.contactDetailList!!.size == 0) {
                edit_contact_phone_property = "Mobile"
                edit_contact_phone_number = ""
                edit_contact_mail = ""
                edit_contact_mail_property = "Bureau"
            } else {
                var tmpPhone = contact.contactDetailList!!.get(0)
                edit_contact_phone_number = NumberAndMailDB.numDBAndMailDBtoDisplay(tmpPhone.content)
                edit_contact_phone_property = NumberAndMailDB.extractStringFromNumber(tmpPhone.content)
                edit_contact_mail = ""
                edit_contact_mail_property = ""
                if (contact.contactDetailList!!.size == 2) {
                    var tmpMail = contact.contactDetailList!!.get(1)
                    edit_contact_mail = NumberAndMailDB.numDBAndMailDBtoDisplay(tmpMail.content)
                    edit_contact_mail_property = NumberAndMailDB.extractStringFromNumber(tmpMail.content)
                }
            }

            val id = edit_contact_id
            val contactDB = edit_contact_ContactsDatabase?.contactsDao()?.getContact(id!!.toInt())
            edit_contact_image64 = contactDB!!.contactDB!!.profilePicture64
            if (edit_contact_image64 == "") {
                println(" contact detail ======= " + edit_contact_rounded_image)
                edit_contact_RoundedImageView!!.setImageResource(edit_contact_rounded_image)
            } else {
                println(" contact detail ======= " + edit_contact_image64)
                val image64 = edit_contact_image64
                edit_contact_RoundedImageView!!.setImageBitmap(base64ToBitmap(image64))
            }
        }

        //region ========================================= Toolbar ==========================================

        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar.setHomeAsUpIndicator(R.drawable.ic_cross)
        actionbar.title = "Editer le contact"

        // Set Resources from MainActivity to ContactDetailsActivity

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        edit_contact_FirstName!!.editText!!.setText(edit_contact_first_name)
        edit_contact_LastName!!.editText!!.setText(edit_contact_last_name)
        edit_contact_PhoneNumber!!.editText!!.setText(edit_contact_phone_number)
        edit_contact_Mail!!.editText!!.setText(edit_contact_mail)
        edit_contact_Mail_Property!!.setSelection(getPosItemSpinner(edit_contact_mail_property!!, edit_contact_Mail_Property!!))
        edit_contact_Phone_Property!!.setSelection(getPosItemSpinner(edit_contact_phone_property!!, edit_contact_Phone_Property!!))

        textChanged(edit_contact_FirstName, edit_contact_FirstName!!.editText!!.text?.toString())
        textChanged(edit_contact_LastName, edit_contact_LastName!!.editText!!.text?.toString())
        textChanged(edit_contact_PhoneNumber, edit_contact_PhoneNumber!!.editText!!.text?.toString())
        textChanged(edit_contact_Mail, edit_contact_Mail!!.editText!!.text?.toString())

        edit_contact_RoundedImageView!!.setOnClickListener {
            selectImage()
        }

        edit_contact_AddFieldButton!!.setOnClickListener {
            val inflater: LayoutInflater = this.layoutInflater
            val alertView: View = inflater.inflate(R.layout.alert_dialog_add_field, null)

            val alert_dialog_AddFieldListView = alertView.findViewById<ListView>(R.id.alert_dialog_add_field_list_view)
            val alert_dialog_FieldAdded = findViewById<ListView>(R.id.edit_contact_field_added)

            alert_dialog_AddFieldListView.adapter = ArrayAdapter(this@EditContactActivity, R.layout.list_item_add_fields_layout, R.id.list_item_add_fields_text, getListOfFields())
            alert_dialog_AddFieldListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->

                customAdapterEditText = CustomAdapterEditText(this@EditContactActivity, getListOfEditText(position))
                alert_dialog_FieldAdded.adapter = customAdapterEditText
            }

            MaterialAlertDialogBuilder(this)
                    .setTitle("Ajouter un champ")
                    .setView(alertView)
                    .show()
        }

        // drop list
        val priority_list = arrayOf(0, 1, 2)
        val array_adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, priority_list)
        array_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        edit_contact_Priority!!.adapter = array_adapter
        println("edit contact prio === " + edit_contact_priority)
        edit_contact_Priority!!.setSelection(edit_contact_priority)
        edit_contact_Priority!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0) {
                    edit_contact_Priority_explain!!.text = getString(R.string.add_new_contact_priority0)
                } else if (position == 1) {
                    edit_contact_Priority_explain!!.text = getString(R.string.add_new_contact_priority1)
                } else if (position == 2) {
                    edit_contact_Priority_explain!!.text = getString(R.string.add_new_contact_priority2)

                    if (Build.VERSION.SDK_INT >= 23) {
                        if (!Settings.canDrawOverlays(applicationContext)) {
                            val alertDialog = OverlayAlertDialog()
                            alertDialog.show()

                        }
                    }
                }
            }
        }
    }

    private fun getListOfFields(): ArrayList<String> {
        val edit_contact_ListOfFields = ArrayList<String>()

        edit_contact_ListOfFields.add("Phone Number")
        edit_contact_ListOfFields.add("Email Address")
        edit_contact_ListOfFields.add("Facebook Account")
        edit_contact_ListOfFields.add("Whatsapp Account")
        edit_contact_ListOfFields.add("Skype Account")
        edit_contact_ListOfFields.add("Instagram Account")
        edit_contact_ListOfFields.add("Telegram Account")
        edit_contact_ListOfFields.add("Address")
        edit_contact_ListOfFields.add("Nickname")
        edit_contact_ListOfFields.add("Site Web")
        edit_contact_ListOfFields.add("Anniversary")
        edit_contact_ListOfFields.add("Relationship")

        return edit_contact_ListOfFields;
    }

    private fun getListOfEditText(position: Int): ArrayList<EditTextModel> {
        val edit_contact_ListOfEditText = ArrayList<EditTextModel>()
        val edit_contact_ListOfFields = getListOfFields()

        val editModel = EditTextModel()
        editModel.editTextValue = edit_contact_ListOfFields[position]
        edit_contact_ListOfEditText.add(editModel)

        return edit_contact_ListOfEditText;
    }

    private fun getPosItemSpinner(item: String, spinner: Spinner): Int {
        val tailleSpinner: Int = spinner.adapter.count
        println("taille spinner" + tailleSpinner)
        for (x in 0 until tailleSpinner) {
            if (spinner.getItemAtPosition(x).equals(item)) {
                return x
            }
        }
        return 0;
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (isChanged) {
                    var alertDialog = AlertDialog.Builder(this)
                    alertDialog.setTitle("Attention")
                    alertDialog.setMessage("Vous risquez de perdre toutes vos modifications, voulez vous vraiment continuer ?")

                    alertDialog.setPositiveButton("Oui") { _, _ ->


                        finish()
                    }

                    alertDialog.setNegativeButton("Non") { _, _ ->
                    }
                    alertDialog.show()
                } else {
                    finish()
                }
            }
            R.id.nav_validate -> {
                val editContact = Runnable {
                    if (edit_contact_FirstName!!.editText!!.toString() != "" || edit_contact_LastName!!.editText!!.toString() != "") {
                        val spinnerPhoneChar = NumberAndMailDB.convertSpinnerStringToChar(edit_contact_Phone_Property!!.selectedItem.toString())
                        val spinnerMailChar = NumberAndMailDB.convertSpinnerStringToChar(edit_contact_Mail_Property!!.selectedItem.toString())
                        var contact = edit_contact_ContactsDatabase?.contactsDao()?.getContact(edit_contact_id!!)
                        for (i in 0..contact!!.contactDetailList!!.size - 1) {
                            if (i == 0) {
                                edit_contact_ContactsDatabase!!.contactDetailsDao().updateContactDetailById(contact!!.contactDetailList!!.get(i).id!!, "" + edit_contact_PhoneNumber!!.textView.text + spinnerPhoneChar)
                            } else if (i == 1) {
                                edit_contact_ContactsDatabase!!.contactDetailsDao().updateContactDetailById(contact!!.contactDetailList!!.get(i).id!!, "" + edit_contact_Mail!!.textView.text + spinnerMailChar)
                            }

                        }//TODO change for the listView

                        if (edit_contact_imgString != null) {
                            edit_contact_ContactsDatabase?.contactsDao()?.updateContactById(edit_contact_id!!.toInt(), edit_contact_FirstName!!.editText!!.text.toString(), edit_contact_LastName!!.editText!!.text.toString(), edit_contact_rounded_image, edit_contact_imgString!!, edit_contact_Priority!!.selectedItem.toString().toInt()) //edit contact rounded maybe not work

                        } else {
                            edit_contact_ContactsDatabase?.contactsDao()?.updateContactByIdWithoutPic(edit_contact_id!!.toInt(), edit_contact_FirstName!!.editText!!.text.toString(), edit_contact_LastName!!.editText!!.text.toString(), edit_contact_rounded_image, edit_contact_Priority!!.selectedItem.toString().toInt())
                        }
                        contact = edit_contact_ContactsDatabase?.contactsDao()?.getContact(edit_contact_id!!)!!
                        println("modify on contact " + contact.contactDB)
                        val intent = Intent(this@EditContactActivity, ContactDetailsActivity::class.java)

                        intent.putExtra("ContactId", edit_contact_id!!)

                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Nom ou Prénom requis", Toast.LENGTH_LONG).show()
                    }
                }
                edit_contact_mDbWorkerThread.postTask(editContact)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_add_contact, menu)
        return true
    }

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

        val items = arrayOf<CharSequence>("Camera", "Gallery", "Cancel")
        //            ActionBar.DisplayOptions[]
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add Image")
        builder.setItems(items) { dialog, i ->
            if (items[i] == "Camera") {
                openCamera()

            } else if (items[i] == "Gallery") {

                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                intent.type = "image/*"
                startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE!!)

            } else if (items[i] == "Cancel") {
                dialog.dismiss()
            }
        }
        builder.show()
    }

    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
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
            if (cursor != null) {
                cursor.close()
            }
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == IMAGE_CAPTURE_CODE) {
                println("image URI = " + imageUri)

                val matrix = Matrix()
                val exif = ExifInterface(getRealPathFromUri(this, imageUri!!));
                val rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                val rotationInDegrees = exifToDegrees(rotation);
                matrix.postRotate(rotationInDegrees.toFloat())
                var bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.width / 10, bitmap.height / 10, true)
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                edit_contact_RoundedImageView!!.setImageBitmap(bitmap)
                edit_contact_imgString = bitmap.bitmapToBase64()

            } else if (requestCode == SELECT_FILE) {
                val matrix = Matrix()
                val selectedImageUri = data!!.data
                val exif = ExifInterface(getRealPathFromUri(this, selectedImageUri!!));
                val rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                val rotationInDegrees = exifToDegrees(rotation);
                matrix.postRotate(rotationInDegrees.toFloat())
                var bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)
                bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.width / 10, bitmap.height / 10, true)

                //matrix.postRotate(90f)
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                edit_contact_RoundedImageView!!.setImageBitmap(bitmap)
                edit_contact_imgString = bitmap.bitmapToBase64()
            }
        }
    }

    private fun exifToDegrees(exifOrientation: Int): Int {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270
        }
        return 0
    }

    private fun Bitmap.bitmapToBase64(): String {
        val baos = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.PNG, 100, baos)
        val imageBytes = baos.toByteArray()

        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    private fun base64ToBitmap(base64: String): Bitmap {
        val imageBytes = Base64.decode(base64, 0)
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    private fun addField() {
        val editText = EditText(this)
        val edit_contact_MiddleName = TextInputLayout(this@EditContactActivity)

        val paramsEditText = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)

        val paramsTextLayout = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                R.dimen.edit_text_height)
//        paramsTextLayout.(0, 5, 0, 0)

        editText.maxLines = 1
        editText.inputType = InputType.TYPE_TEXT_VARIATION_PERSON_NAME
        editText.background = ContextCompat.getDrawable(this@EditContactActivity, R.drawable.custom_edit_text)
        editText.setTextColor(ContextCompat.getColor(this@EditContactActivity, R.color.textColor))

        edit_contact_MiddleName.id = R.id.edit_contact_middle_name_id
        edit_contact_MiddleName.hint = resources.getString(R.string.edit_contact_first_name)
        edit_contact_MiddleName.layoutParams = paramsTextLayout
        edit_contact_MiddleName.addView(editText, paramsEditText)

//        val constraintSet = ConstraintSet()
//        constraintSet.clone(edit_contact_ParentLayout);
//        constraintSet.connect(edit_contact_MiddleName.id, ConstraintSet.TOP, edit_contact_ParentLayout!!.id, ConstraintSet.TOP, 18);
//        constraintSet.connect(edit_contact_MiddleName.id, ConstraintSet.LEFT, edit_contact_ParentLayout!!.id, ConstraintSet.LEFT, 18);
//        constraintSet.connect(edit_contact_MiddleName.id, ConstraintSet.RIGHT, edit_contact_ParentLayout!!.id, ConstraintSet.RIGHT, 18);
//        constraintSet.connect(edit_contact_MiddleName.id, ConstraintSet.BOTTOM, edit_contact_ParentLayout!!.id, ConstraintSet.BOTTOM, 18);
//
//        constraintSet.applyTo(edit_contact_ParentLayout);

//                            edit_contact_MiddleName.requestLayout()
        edit_contact_ParentLayout!!.addView(edit_contact_MiddleName)
    }

    //TODO: modifier l'alert dialog en ajoutant une vue pour le rendre joli.
    private fun OverlayAlertDialog(): android.app.AlertDialog {
        val alertDialogBuilder = android.app.AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Knocker")
        alertDialogBuilder.setMessage("vous voulez vous autouriser knocker à afficher des notifications directement sur d'autre application")
        alertDialogBuilder.setPositiveButton("oui"
        ) { _, _ ->
            val intentPermission = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivity(intentPermission)
            val sharedPreferences = getSharedPreferences("Knocker_preferences", Context.MODE_PRIVATE)
            val edit: SharedPreferences.Editor = sharedPreferences.edit()
            edit.putBoolean("popupNotif", true)//quand la personne autorise l'affichage par dessus d'autre application nous l'enregistrons
            edit.putBoolean("serviceNotif", false)
            edit.apply()

        }
        alertDialogBuilder.setNegativeButton("non"
        ) { _, _ ->
            val sharedPreferences = getSharedPreferences("Knocker_preferences", Context.MODE_PRIVATE)
            val edit: SharedPreferences.Editor = sharedPreferences.edit()
            edit.putBoolean("popupNotif", false)//quand la personne autorise l'affichage par dessus d'autre application nous l'enregistrons
            edit.putBoolean("serviceNotif", true)
            edit.commit()
        }
        return alertDialogBuilder.create()
    }
}
