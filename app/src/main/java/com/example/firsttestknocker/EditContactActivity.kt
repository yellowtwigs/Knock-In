package com.example.firsttestknocker

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.*
import java.io.ByteArrayOutputStream


class EditContactActivity : AppCompatActivity() {

    private var edit_contact_FirstName: TextView? = null
    private var edit_contact_LastName: TextView? = null
    private var edit_contact_PhoneNumber: TextView? = null
    private var edit_contact_Mail: TextView? = null
    private var edit_contact_RoundedImageView: ImageView? = null
    private var edit_contact_Priority: Spinner? = null
    private var edit_contact_Priority_explain: TextView? = null
    private var edit_contact_Phone_Property: Spinner? = null
    private var edit_contact_Mail_Property : Spinner? = null

    private var edit_contact_id: Long? = null
    private var edit_contact_first_name: String? = null
    private var edit_contact_last_name: String? = null
    private var edit_contact_phone_number: String? = null
    private var edit_contact_phone_property: String? = null
    private var edit_contact_mail_property: String? = null
    private var edit_contact_mail: String? = null
    private var edit_contact_rounded_image: Int = 0
    private var edit_contact_image64: String? = null
    private var edit_contact_priority: Int = 1

    // Database && Thread
    private var edit_contact_ContactsDatabase: ContactsRoomDatabase? = null
    private lateinit var edit_contact_mDbWorkerThread: DbWorkerThread

    private var isChanged = false

    var imageUri: Uri? = null
    private var REQUEST_CAMERA: Int? = 1
    private var SELECT_FILE: Int? = 0
    private val IMAGE_CAPTURE_CODE = 1001
    var edit_contact_imgString: String? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_contact)

        // on init WorkerThread
        edit_contact_mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        edit_contact_mDbWorkerThread.start()

        //on get la base de données
        edit_contact_ContactsDatabase = ContactsRoomDatabase.getDatabase(this)

        // Create the Intent, and get the data from the GridView
        val intent = intent
        edit_contact_id = intent.getLongExtra("ContactId", 1)
        edit_contact_first_name = intent.getStringExtra("ContactFirstName")
        edit_contact_last_name = intent.getStringExtra("ContactLastName")
        var tmp=intent.getStringExtra("ContactPhoneNumber")
        edit_contact_phone_number = NumberAndMailDB.numDBAndMailDBtoDisplay(tmp)
        edit_contact_phone_property = NumberAndMailDB.extractStringFromNumber(tmp)
        tmp= intent.getStringExtra("ContactMail")
        edit_contact_mail = NumberAndMailDB.numDBAndMailDBtoDisplay(tmp)
        edit_contact_mail_property= NumberAndMailDB.extractStringFromNumber(tmp)
        edit_contact_rounded_image = intent.getIntExtra("ContactImage", 1)
        edit_contact_priority = intent.getIntExtra("ContactPriority",1)

        // Find View By Id
        edit_contact_FirstName = findViewById(R.id.edit_contact_first_name_id)
        edit_contact_LastName = findViewById(R.id.edit_contact_last_name_id)
        edit_contact_PhoneNumber = findViewById(R.id.edit_contact_phone_number_id)
        edit_contact_RoundedImageView = findViewById(R.id.edit_contact_rounded_image_view_id)
        edit_contact_Mail = findViewById(R.id.edit_contact_mail_id)
        edit_contact_Mail_Property= findViewById(R.id.edit_contact_mail_spinner_id)
        edit_contact_Priority = findViewById(R.id.edit_contact_priority)
        edit_contact_Phone_Property = findViewById(R.id.add_new_contact_phone_number_spinner)
        edit_contact_Priority_explain = findViewById(R.id.edit_contact_priority_explain)
        if(edit_contact_ContactsDatabase?.contactsDao()?.getContact(edit_contact_id!!.toInt())==null)
        {
            var contactList: List<Contacts>?
            val contact= ListContact.loadJSONFromAsset(this)
            contactList= ListContact.construireListe(contact)
            for(contact in contactList){
                println("nom attendu :"+edit_contact_first_name+" "+edit_contact_last_name+" voici le nom de ce contact"+contact.firstName+" "+contact.lastName)
                if(edit_contact_first_name.equals(contact.firstName) && edit_contact_last_name.equals(contact.lastName)){
                    edit_contact_image64= contact.profilePicture64
                    edit_contact_RoundedImageView!!.setImageBitmap(base64ToBitmap(edit_contact_image64.toString()))
                    println("image set to image view")
                }
            }
        }else {
            val getimage64 = Runnable {
                val id = edit_contact_id
                val contact = edit_contact_ContactsDatabase?.contactsDao()?.getContact(id!!.toInt())
                edit_contact_image64 = contact!!.profilePicture64
                if (edit_contact_image64 == "") {
                    println(" contact detail ======= " + edit_contact_rounded_image)
                    edit_contact_RoundedImageView!!.setImageResource(edit_contact_rounded_image)
                } else {
                    println(" contact detail ======= " + edit_contact_image64)
                    val image64 = edit_contact_image64
                    edit_contact_RoundedImageView!!.setImageBitmap(base64ToBitmap(image64!!))
                }
            }
            edit_contact_mDbWorkerThread.postTask(getimage64)
        }
        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar.setHomeAsUpIndicator(R.drawable.ic_cross)
        actionbar.title = "Editer le contact"

        // Set Resources from MainActivity to ContactDetailsActivity
        edit_contact_FirstName!!.text = edit_contact_first_name
        edit_contact_LastName!!.text = edit_contact_last_name
        edit_contact_PhoneNumber!!.text = edit_contact_phone_number
        edit_contact_Phone_Property!!.setSelection(getPosItemSpinner(edit_contact_phone_property!!, edit_contact_Phone_Property!!))
        edit_contact_Mail!!.text = edit_contact_mail
        edit_contact_Mail_Property!!.setSelection(getPosItemSpinner(edit_contact_mail_property!!,edit_contact_Mail_Property!!))
        //edit_contact_RoundedImageView!!.setImageResource(edit_contact_rounded_image)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        textChanged(edit_contact_FirstName, edit_contact_first_name)
        textChanged(edit_contact_LastName, edit_contact_last_name)
        textChanged(edit_contact_PhoneNumber, edit_contact_phone_number)
        textChanged(edit_contact_Mail, edit_contact_mail)

        edit_contact_RoundedImageView!!.setOnClickListener {
            SelectImage()
        }

        // drop list
        val priority_list = arrayOf(0,1,2)
        val array_adapter = ArrayAdapter(this,android.R.layout.simple_spinner_item, priority_list)
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
                    edit_contact_Priority_explain!!.setText(getString(R.string.add_new_contact_priority0))
                } else if (position == 1) {
                    edit_contact_Priority_explain!!.setText(getString(R.string.add_new_contact_priority1))
                } else if (position == 2) {
                    edit_contact_Priority_explain!!.setText(getString(R.string.add_new_contact_priority2))
                }
            }
        }
    }
    private fun getPosItemSpinner(item: String, spinner:Spinner): Int {
        val tailleSpinner: Int= spinner.adapter.count
        println("taille spinner"+tailleSpinner)
            for(x in 0 until tailleSpinner) {
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

                    alertDialog.setPositiveButton("Oui", { _, _ ->

                        val intent = Intent(this@EditContactActivity, ContactDetailsActivity::class.java)
                        intent.putExtra("ContactId", edit_contact_id)
                        intent.putExtra("ContactFirstName", edit_contact_first_name)
                        intent.putExtra("ContactLastName", edit_contact_last_name)
                        intent.putExtra("ContactPhoneNumber", edit_contact_phone_number+NumberAndMailDB.convertSpinnerStringToChar(edit_contact_Phone_Property!!.selectedItem.toString()))
                        intent.putExtra("ContactImage", edit_contact_rounded_image)
                        intent.putExtra("ContactMail", edit_contact_mail+NumberAndMailDB.convertSpinnerMailStringToChar(edit_contact_Mail_Property!!.selectedItem.toString(),edit_contact_mail!!))
                        intent.putExtra("ContactPriority", edit_contact_priority)

                        startActivity(intent)
                        finish()
                    })

                    alertDialog.setNegativeButton("Non", { _, _ ->
                    })
                    alertDialog.show()
                } else {
                    val intent = Intent(this@EditContactActivity, ContactDetailsActivity::class.java)
                    intent.putExtra("ContactId", edit_contact_id)
                    intent.putExtra("ContactFirstName", edit_contact_first_name)
                    intent.putExtra("ContactLastName", edit_contact_last_name)
                    intent.putExtra("ContactPhoneNumber", edit_contact_phone_number+NumberAndMailDB.convertSpinnerStringToChar(edit_contact_Phone_Property!!.prompt.toString()))
                    intent.putExtra("ContactImage", edit_contact_rounded_image)
                    intent.putExtra("ContactMail", edit_contact_mail+NumberAndMailDB.convertSpinnerMailStringToChar(edit_contact_Mail_Property!!.selectedItem.toString(),edit_contact_mail!!))
                    intent.putExtra("ContactPriority", edit_contact_priority)

                    startActivity(intent)
                    finish()
                }
            }
            R.id.nav_validate -> {
                val editContact = Runnable {

                    val spinnerPhoneChar = NumberAndMailDB.convertSpinnerStringToChar(edit_contact_Phone_Property!!.selectedItem.toString())
                    val spinnerMailChar = NumberAndMailDB.convertSpinnerStringToChar(edit_contact_Mail_Property!!.selectedItem.toString())
                    if (edit_contact_imgString != null) {
                        edit_contact_ContactsDatabase?.contactsDao()?.updateContactById(edit_contact_id!!.toInt(), edit_contact_FirstName!!.text.toString(), edit_contact_LastName!!.text.toString(), edit_contact_PhoneNumber!!.text.toString()+spinnerPhoneChar, "", edit_contact_rounded_image, edit_contact_imgString!!, edit_contact_Priority!!.selectedItem.toString().toInt()) //edit contact rounded maybe not work
                    } else {
                        edit_contact_ContactsDatabase?.contactsDao()?.updateContactByIdWithoutPic(edit_contact_id!!.toInt(), edit_contact_FirstName!!.text.toString(), edit_contact_LastName!!.text.toString(), edit_contact_PhoneNumber!!.text.toString()+spinnerPhoneChar, "", edit_contact_rounded_image, edit_contact_Priority!!.selectedItem.toString().toInt())
                    }
                    val intent = Intent(this@EditContactActivity, ContactDetailsActivity::class.java)

                    intent.putExtra("ContactId", edit_contact_id)
                    intent.putExtra("ContactFirstName", edit_contact_FirstName!!.text.toString())
                    intent.putExtra("ContactLastName", edit_contact_LastName!!.text.toString())
                    intent.putExtra("ContactPhoneNumber", edit_contact_phone_number+spinnerPhoneChar)
                    intent.putExtra("ContactImage", edit_contact_rounded_image)
                    intent.putExtra("ContactMail", edit_contact_Mail!!.text.toString()+spinnerMailChar)
                    intent.putExtra("ContactPriority", edit_contact_Priority!!.selectedItem.toString().toInt())


                    startActivity(intent)
                    finish()
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

    fun textChanged(textView: TextView?, txt: String?) {
        textView!!.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
                isChanged = textView.text != txt
            }

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {

            }
        })
    }

    private fun SelectImage() {

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
                println("image URI = " + imageUri)

                val matrix = Matrix()
                val exif = ExifInterface(getRealPathFromUri(this,imageUri!!));
                val rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                val rotationInDegrees = exifToDegrees(rotation);
                matrix.postRotate(rotationInDegrees.toFloat())
                var bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                bitmap = Bitmap.createScaledBitmap(bitmap,bitmap.width/10,bitmap.height/10,true)
                bitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.width,bitmap.height,matrix, true )
                edit_contact_RoundedImageView!!.setImageBitmap(bitmap)
                edit_contact_imgString = bitmapToBase64(bitmap)

            } else if (requestCode == SELECT_FILE) {
                val matrix = Matrix()
                val selectedImageUri = data!!.data
                val exif = ExifInterface(getRealPathFromUri(this, selectedImageUri));
                val rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                val rotationInDegrees = exifToDegrees(rotation);
                matrix.postRotate(rotationInDegrees.toFloat())
                var bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)
                bitmap = Bitmap.createScaledBitmap(bitmap,bitmap.width/10,bitmap.height/10,true)

                //matrix.postRotate(90f)
                bitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.width,bitmap.height,matrix, true )
                edit_contact_RoundedImageView!!.setImageBitmap(bitmap)
                edit_contact_imgString = bitmapToBase64(bitmap)
            }
        }
    }

    fun exifToDegrees(exifOrientation: Int): Int {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) { return 90}
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) { return 180}
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) { return 270}
        return 0
    }

    fun bitmapToBase64(bitmap: Bitmap) : String {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val imageBytes = baos.toByteArray()

        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    fun base64ToBitmap(base64: String) : Bitmap {
        val imageBytes = Base64.decode(base64,0)
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }
}
