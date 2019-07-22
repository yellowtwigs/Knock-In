package com.example.knocker.controller.activity

import android.Manifest
import android.annotation.SuppressLint
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
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import android.util.Base64
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.knocker.*
import com.example.knocker.controller.CircularImageView
import com.example.knocker.model.*
import com.example.knocker.model.ModelDB.ContactDB
import com.example.knocker.model.ModelDB.ContactDetailDB
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import java.io.ByteArrayOutputStream

/**
 * La Classe qui permet la création d'un nouveau contact
 * @author Florian Striebel, Kenzy Suon, Ryan Granet
 */
@Suppress("DEPRECATION")
class AddNewContactActivity : AppCompatActivity() {

    //region ========================================== Var or Val ==========================================

    private var add_new_contact_FirstName: TextInputLayout? = null
    private var add_new_contact_LastName: TextInputLayout? = null
    private var add_new_contact_PhoneNumber: TextInputLayout? = null
    private var add_new_contact_Email: TextInputLayout? = null
    private var add_new_contact_RoundedImageView: CircularImageView? = null
    private var add_new_contact_Priority: Spinner? = null
    private var add_new_contact_PhoneProperty: Spinner? = null
    private var add_new_contact_MailProperty: Spinner? = null
    private var add_new_contact_PriorityExplain: TextView? = null
    private var gestionnaireContacts: ContactList? = null
    private var avatar: Int = 0

    var imageUri: Uri? = null
    private val IMAGE_CAPTURE_CODE = 1001

    // Database && Thread
    private var main_ContactsDatabase: ContactsRoomDatabase? = null
    private lateinit var main_mDbWorkerThread: DbWorkerThread

    //private var REQUEST_CAMERA: Int? = 1
    private var SELECT_FILE: Int? = 0
    var add_new_contact_ImgString: String? = "";

    //endregion


    @SuppressLint("WrongThread")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedThemePreferences = getSharedPreferences("Knocker_Theme", Context.MODE_PRIVATE)
        if (sharedThemePreferences.getBoolean("darkTheme", false)) {
            setTheme(R.style.AppThemeDark)
        } else {
            setTheme(R.style.AppTheme)
        }
        setContentView(R.layout.activity_add_new_contact)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT)

        // on init WorkerThread
        main_mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        main_mDbWorkerThread.start()

        //on get la base de données
        main_ContactsDatabase = ContactsRoomDatabase.getDatabase(this)
        gestionnaireContacts = ContactList(this.applicationContext)

        //region ========================================== Toolbar =========================================

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar.setHomeAsUpIndicator(R.drawable.ic_left_arrow)
        actionbar.title = "Ajouter un nouveau contact"

        //endregion

        //region ======================================= FindViewById =======================================

        add_new_contact_FirstName = findViewById(R.id.add_new_contact_first_name_id)
        add_new_contact_LastName = findViewById(R.id.add_new_contact_last_name_id)
        add_new_contact_PhoneNumber = findViewById(R.id.add_new_contact_phone_number_id)
        add_new_contact_Email = findViewById(R.id.add_new_contact_mail_id)
        add_new_contact_RoundedImageView = findViewById(R.id.add_new_contact_rounded_image_view_id)
        add_new_contact_Priority = findViewById(R.id.add_new_contact_priority)
        add_new_contact_PhoneProperty = findViewById(R.id.add_new_contact_phone_number_spinner)
        add_new_contact_MailProperty = findViewById(R.id.add_new_contact_mail_spinner_id)
        add_new_contact_PriorityExplain = findViewById(R.id.add_new_contact_priority_explain)
        val add_new_contact_layout: ConstraintLayout = findViewById(R.id.add_new_contact_layout)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        //endregion

        if (intent != null &&
                intent.getStringExtra("ContactPhoneNumber") != null) {
            val add_new_contact_phone_number = intent.getStringExtra("ContactPhoneNumber")
            add_new_contact_PhoneNumber!!.editText!!.setText(add_new_contact_phone_number)
        }

        avatar = gestionnaireContacts!!.randomDefaultImage(0, "Create")
        add_new_contact_RoundedImageView!!.setImageResource(gestionnaireContacts!!.randomDefaultImage(avatar, "Get"))

        //region ==================================== SetOnClickListener ====================================

        add_new_contact_RoundedImageView!!.setOnClickListener {
            SelectImage()
        }

        //endregion

        // drop list
        val priority_list = arrayOf(0, 1, 2)
        val array_adapter = ArrayAdapter(this, R.layout.spinner_dropdown_item, priority_list)
        array_adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)

        //disable keyboard window
        add_new_contact_layout.setOnTouchListener { v, event ->
            val view = this@AddNewContactActivity.currentFocus
            val imm = this@AddNewContactActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (view != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0)
            }
            true
        }
        //
        add_new_contact_Priority!!.adapter = array_adapter
        add_new_contact_Priority!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                println("position equals" + position)
                if (position == 0) {
                    add_new_contact_PriorityExplain!!.setText(getString(R.string.add_new_contact_priority0))
                    add_new_contact_RoundedImageView!!.visibility = View.GONE
                    add_new_contact_RoundedImageView!!.setBorderColor(resources.getColor(R.color.priorityZeroColor))
                    add_new_contact_RoundedImageView!!.setBetweenBorderColor(resources.getColor(R.color.lightColor))
                    add_new_contact_RoundedImageView!!.visibility = View.VISIBLE
                } else if (position == 1) {
                    add_new_contact_PriorityExplain!!.setText(getString(R.string.add_new_contact_priority1))
                    add_new_contact_RoundedImageView!!.visibility = View.GONE
                    add_new_contact_RoundedImageView!!.setBorderColor(resources.getColor(R.color.priorityOneColor))
                    add_new_contact_RoundedImageView!!.setBetweenBorderColor(resources.getColor(R.color.lightColor))
                    add_new_contact_RoundedImageView!!.visibility = View.VISIBLE
                } else if (position == 2) {
                    add_new_contact_PriorityExplain!!.setText(getString(R.string.add_new_contact_priority2))
                    add_new_contact_RoundedImageView!!.visibility = View.GONE
                    add_new_contact_RoundedImageView!!.setBorderColor(resources.getColor(R.color.priorityTwoColor))
                    add_new_contact_RoundedImageView!!.setBetweenBorderColor(resources.getColor(R.color.lightColor))
                    add_new_contact_RoundedImageView!!.visibility = View.VISIBLE
                }
                println("selected item equals" + add_new_contact_Priority!!.selectedItemPosition)
            }
        }
        add_new_contact_Priority!!.setSelection(1)
        add_new_contact_RoundedImageView!!.setBorderColor(resources.getColor(R.color.priorityOneColor))
        add_new_contact_RoundedImageView!!.setBetweenBorderColor(resources.getColor(R.color.lightColor))
        println("selected item equals" + add_new_contact_Priority!!.selectedItemPosition)


        val phoneTagList = resources.getStringArray(R.array.add_new_contact_phone_number_arrays)
        val adapterPhoneTagList = ArrayAdapter(this, R.layout.spinner_item, phoneTagList)
        array_adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        add_new_contact_PhoneProperty!!.adapter = adapterPhoneTagList

        val mailTagList = resources.getStringArray(R.array.add_new_contact_mail_arrays)
        val adapterMailTagList = ArrayAdapter(this, R.layout.spinner_item, mailTagList)
        array_adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        add_new_contact_MailProperty!!.adapter = adapterMailTagList
        android.R.layout.simple_spinner_item
    }

    //region ========================================== Functions ===========================================

    //demmande de confirmation de la création d'un contact en double
    private fun confirmationDuplicate(contactData: ContactDB) {

        MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                .setTitle(R.string.add_new_contact_alert_dialog_title)
                .setMessage(R.string.add_new_contact_alert_dialog_message)
                .setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
                    main_ContactsDatabase?.contactsDao()?.insert(contactData)
                    val intent = Intent(this@AddNewContactActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .setBackground(getDrawable(R.color.backgroundColor))
                .setNegativeButton(R.string.alert_dialog_no) { _, _ ->
                }
                .show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (isEmptyField(add_new_contact_FirstName) && isEmptyField(add_new_contact_LastName) && isEmptyField(add_new_contact_PhoneNumber) && isEmptyField(add_new_contact_Email)) {
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
            R.id.nav_validate -> if (isEmptyField(add_new_contact_FirstName)) {

                MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                        .setTitle(R.string.add_new_contact_alert_dialog_title)
                        .setMessage(R.string.add_new_contact_alert_dialog_message)
                        .setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
                        }
                        .setBackground(getDrawable(R.color.backgroundColor))
                        .setNegativeButton(R.string.alert_dialog_no) { _, _ ->
                        }
                        .show()


                Toast.makeText(this, R.string.add_new_contact_toast, Toast.LENGTH_SHORT).show()
            } else {
                //if (isValidMobile(add_new_contact_PhoneNumber!!.text.toString())) {
                val printContacts = Runnable {
                    //check si un contact porte deja ce prénom et nom puis l'ajoute si il y a aucun doublon
                    val spinnerChar = NumberAndMailDB.convertSpinnerStringToChar(add_new_contact_PhoneProperty!!.selectedItem.toString(), this)
                    val mailSpinnerChar = NumberAndMailDB.convertSpinnerMailStringToChar(add_new_contact_MailProperty!!.selectedItem.toString(), add_new_contact_Email!!.editText!!.text.toString(), this)
                    val contactData = ContactDB(null,
                            add_new_contact_FirstName!!.editText!!.text.toString(),
                            add_new_contact_LastName!!.editText!!.text.toString(),
                            avatar, add_new_contact_Priority!!.selectedItem.toString().toInt(),
                            add_new_contact_ImgString!!)
                    println(contactData)
                    var isDuplicate = false
                    val allcontacts = main_ContactsDatabase?.contactsDao()?.getAllContacts()
                    allcontacts?.forEach { contactsDB ->
                        if (contactsDB.firstName == contactData.firstName && contactsDB.lastName == contactData.lastName)
                            isDuplicate = true
                    }

                    if (isDuplicate == false) {
                        main_ContactsDatabase?.contactsDao()?.insert(contactData)
                        val listContacts: List<ContactDB>? = main_ContactsDatabase?.contactsDao()!!.getAllContacts()
                        val contact: ContactDB? = getContact(contactData.firstName + " " + contactData.lastName, listContacts)
                        var contactDetailDB: ContactDetailDB
                        if (add_new_contact_PhoneNumber!!.editText!!.text.toString() != "") {
                            contactDetailDB = ContactDetailDB(null, contact?.id, "" + add_new_contact_PhoneNumber!!.editText!!.text.toString(), "phone", spinnerChar, 0)
                            main_ContactsDatabase?.contactDetailsDao()?.insert(contactDetailDB)
                        }
                        if (add_new_contact_Email!!.editText!!.text.toString() != "") {
                            contactDetailDB = ContactDetailDB(null, contact?.id, "" + add_new_contact_Email!!.editText!!.text.toString(), "mail", mailSpinnerChar, 1)
                            main_ContactsDatabase?.contactDetailsDao()?.insert(contactDetailDB)
                        }

                        // println("test" + main_ContactsDatabase?.contactDetailsDao()?.getAllpropertiesEditContact())

                        val intent = Intent(this@AddNewContactActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        confirmationDuplicate(contactData)
                    }
                }
                main_mDbWorkerThread.postTask(printContacts)
                //} else {
                //   Toast.makeText(this, "Votre numéro de téléphone n'est pas valide !", Toast.LENGTH_SHORT).show()
                //}
            }

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_toolbar_validate, menu)
        return true
    }

    private fun isEmptyField(field: TextInputLayout?): Boolean {
        return field!!.editText!!.text.toString().isEmpty()
    }

    private fun SelectImage() {

        val items = arrayOf<CharSequence>(getString(R.string.add_new_contact_alert_dialog_photo_camera)
                , getString(R.string.add_new_contact_alert_dialog_photo_galery)
                , getString(R.string.add_new_contact_alert_dialog_photo_cancel))
        //            ActionBar.DisplayOptions[]

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.add_new_contact_alert_dialog_photo_title)
        builder.setItems(items) { dialog, i ->
            if (items[i] == this.getString(R.string.add_new_contact_alert_dialog_photo_camera)) {
                openCamera()

            } else if (items[i] == this.getString(R.string.add_new_contact_alert_dialog_photo_galery)) {

                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                intent.type = "image/*"
                startActivityForResult(Intent.createChooser(intent, this.getString(R.string.add_new_contact_intent_title)), SELECT_FILE!!)

            } else if (items[i] == this.getString(R.string.add_new_contact_alert_dialog_photo_cancel)) {
                dialog.dismiss()
            }
        }
        builder.show()
    }

    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, getString(R.string.add_new_contact_camera_open_title))
        values.put(MediaStore.Images.Media.DESCRIPTION, getString(R.string.add_new_contact_camera_open_description))
        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE)
    }

    fun getRealPathFromUri(context: Context, contentUri: Uri): String {
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

                val matrix = Matrix()
                val exif = ExifInterface(getRealPathFromUri(this, imageUri!!));
                val rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                val rotationInDegrees = exifToDegrees(rotation);
                matrix.postRotate(rotationInDegrees.toFloat())
                var bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.width / 10, bitmap.height / 10, true)
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                add_new_contact_RoundedImageView!!.setImageBitmap(bitmap)
                add_new_contact_ImgString = bitmapToBase64(bitmap)
                println("convert to = " + base64ToBitmap(bitmapToBase64(bitmap)))
                println("is the same ? result: " + bitmap.sameAs(base64ToBitmap(bitmapToBase64(bitmap))))

            } else if (requestCode == SELECT_FILE) {
                val matrix = Matrix()
                val selectedImageUri = data!!.data
                val exif = ExifInterface(getRealPathFromUri(this, selectedImageUri!!))
                val rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                val rotationInDegrees = exifToDegrees(rotation);
                matrix.postRotate(rotationInDegrees.toFloat())
                var bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)
                bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.width / 10, bitmap.height / 10, true)
//                var matrix = Matrix()
//                matrix.postRotate(90f)
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                add_new_contact_RoundedImageView!!.setImageBitmap(bitmap)
                add_new_contact_ImgString = bitmapToBase64(bitmap)
            }
        }
    }

    fun exifToDegrees(exifOrientation: Int): Int {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270
        }
        return 0
    }

    fun bitmapToBase64(bitmap: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val imageBytes = baos.toByteArray()

        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    fun base64ToBitmap(base64: String): Bitmap {
        val imageBytes = Base64.decode(base64, 0)
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    //endregion

    fun getContact(name: String, listContact: List<ContactDB>?): ContactDB? {

        if (name.contains(" ")) {
            listContact!!.forEach { dbContact ->

                //                println("contact "+dbContact+ "différent de name"+name)
                if (dbContact.firstName + " " + dbContact.lastName == name) {
                    return dbContact
                }
            }
        } else {
            listContact!!.forEach { dbContact ->
                if (dbContact.firstName == name && dbContact.lastName == "" || dbContact.firstName == "" && dbContact.lastName == name) {
                    return dbContact
                }
            }
        }
        return null
    }//TODO : trouver une place pour toutes les méthodes des contacts

}
