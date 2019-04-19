package com.example.firsttestknocker

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
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
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import java.io.ByteArrayOutputStream


class EditContactActivity : AppCompatActivity() {

    private var edit_contact_FirstName: TextView? = null
    private var edit_contact_LastName: TextView? = null
    private var edit_contact_PhoneNumber: TextView? = null
    private var edit_contact_Mail: TextView? = null
    private var edit_contact_RoundedImageView: ImageView? = null

    private var edit_contact_id: Long? = null
    private var edit_contact_first_name: String? = null
    private var edit_contact_last_name: String? = null
    private var edit_contact_phone_number: String? = null
    private var edit_contact_mail: String? = null
    private var edit_contact_rounded_image: Int = 0
    private var edit_contact_image64: String? = null

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
//        val idContact =
        edit_contact_id = intent.getLongExtra("ContactId", 1)
        edit_contact_first_name = intent.getStringExtra("ContactFirstName")
        edit_contact_last_name = intent.getStringExtra("ContactLastName")
        edit_contact_phone_number = intent.getStringExtra("ContactPhoneNumber")
        edit_contact_mail = intent.getStringExtra("ContactMail")
        edit_contact_rounded_image = intent.getIntExtra("ContactImage", 1)

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

        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar.setHomeAsUpIndicator(R.drawable.ic_cross)
        actionbar.title = "Editer le contact"

        // Find View By Id
        edit_contact_FirstName = findViewById(R.id.edit_contact_first_name_id)
        edit_contact_LastName = findViewById(R.id.edit_contact_last_name_id)
        edit_contact_PhoneNumber = findViewById(R.id.edit_contact_phone_number_id)
        edit_contact_RoundedImageView = findViewById(R.id.edit_contact_rounded_image_view_id)
        edit_contact_Mail = findViewById(R.id.edit_contact_mail_id)

        // Set Resources from MainActivity to ContactDetailsActivity
        edit_contact_FirstName!!.text = edit_contact_first_name
        edit_contact_LastName!!.text = edit_contact_last_name
        edit_contact_PhoneNumber!!.text = edit_contact_phone_number
        edit_contact_Mail!!.text = edit_contact_mail
        edit_contact_RoundedImageView!!.setImageResource(edit_contact_rounded_image)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        textChanged(edit_contact_FirstName, edit_contact_first_name)
        textChanged(edit_contact_LastName, edit_contact_last_name)
        textChanged(edit_contact_PhoneNumber, edit_contact_phone_number)
        textChanged(edit_contact_Mail, edit_contact_mail)

        edit_contact_RoundedImageView!!.setOnClickListener {
            SelectImage()
//            add_new_contact_imgString = imageToBase64(add_new_contact_RoundedImageView!!)
        }
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
                        intent.putExtra("ContactPhoneNumber", edit_contact_phone_number)
                        intent.putExtra("ContactImage", edit_contact_rounded_image)
                        intent.putExtra("ContactMail", edit_contact_mail)

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
                    intent.putExtra("ContactPhoneNumber", edit_contact_phone_number)
                    intent.putExtra("ContactImage", edit_contact_rounded_image)
                    intent.putExtra("ContactMail", edit_contact_mail)

                    startActivity(intent)
                    finish()
                }
            }
            R.id.nav_validate -> {
                val editContact = Runnable {
                    if (edit_contact_imgString != null) {
                        edit_contact_ContactsDatabase?.contactsDao()?.updateContactById(edit_contact_id!!.toInt(), edit_contact_FirstName!!.text.toString(), edit_contact_LastName!!.text.toString(), edit_contact_PhoneNumber!!.text.toString(), "", edit_contact_rounded_image, edit_contact_imgString!!) //edit contact rounded maybe not work
                    } else {

                    }
                    val intent = Intent(this@EditContactActivity, ContactDetailsActivity::class.java)

                    intent.putExtra("ContactId", edit_contact_id)
                    intent.putExtra("ContactFirstName", edit_contact_FirstName!!.text.toString())
                    intent.putExtra("ContactLastName", edit_contact_LastName!!.text.toString())
                    intent.putExtra("ContactPhoneNumber", edit_contact_PhoneNumber!!.text.toString())
                    intent.putExtra("ContactImage", edit_contact_RoundedImageView!!.id)
                    intent.putExtra("ContactMail", edit_contact_Mail!!.text.toString())


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
        println("image URI inside = " + imageUri)
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == IMAGE_CAPTURE_CODE) {
                println("image URI = " + imageUri)
                var bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                bitmap = Bitmap.createScaledBitmap(bitmap,250,200,true)
                val matrix = Matrix()
                matrix.postRotate(90f)
                bitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.width,bitmap.height,matrix, true )
                edit_contact_RoundedImageView!!.setImageBitmap(bitmap)
                edit_contact_imgString = bitmapToBase64(bitmap)

            } else if (requestCode == SELECT_FILE) {
                val selectedImageUri = data!!.data
                var bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)
                bitmap = Bitmap.createScaledBitmap(bitmap,250,200,true)
                val matrix = Matrix()
                matrix.postRotate(90f)
                bitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.width,bitmap.height,matrix, true )
                edit_contact_RoundedImageView!!.setImageBitmap(bitmap)
                edit_contact_imgString = bitmapToBase64(bitmap)
            }
        }
    }

    fun bitmapToBase64(bitmap: Bitmap) : String {
        val baos = ByteArrayOutputStream()
        //val bitmap = BitmapFactory.decodeResource(resources, img.id)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val imageBytes = baos.toByteArray()

        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    fun base64ToBitmap(base64: String) : Bitmap {
        val imageBytes = Base64.decode(base64,0)
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }
}
