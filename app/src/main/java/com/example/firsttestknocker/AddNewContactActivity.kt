package com.example.firsttestknocker

import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AlertDialog
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import java.io.ByteArrayOutputStream

class AddNewContactActivity : AppCompatActivity() {

    private var add_new_contact_FirstName: EditText? = null
    private var add_new_contact_LastName: EditText? = null
    private var add_new_contact_PhoneNumber: EditText? = null
    private var add_new_contact_Email: EditText? = null
    private var add_new_contact_RoundedImageView: ImageView? = null
    // Database && Thread
    private var main_ContactsDatabase: ContactsRoomDatabase? = null
    private lateinit var main_mDbWorkerThread: DbWorkerThread

    private var REQUEST_CAMERA: Int? = 1
    private var SELECT_FILE: Int? = 0
    var add_new_contact_imgString: String? = null;

    @SuppressLint("WrongThread")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_contact)

        // on init WorkerThread
        main_mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        main_mDbWorkerThread.start()

        //on get la base de données
        main_ContactsDatabase = ContactsRoomDatabase.getDatabase(this)

        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar.setHomeAsUpIndicator(R.drawable.ic_left_arrow)
        actionbar.title = "Ajouter un nouveau contact"

        // Find View By Id
        add_new_contact_FirstName = findViewById(R.id.add_new_contact_first_name_id)
        add_new_contact_LastName = findViewById(R.id.add_new_contact_last_name_id)
        add_new_contact_PhoneNumber = findViewById(R.id.add_new_contact_phone_number_id)
        add_new_contact_Email = findViewById(R.id.add_new_contact_mail_id)
        add_new_contact_RoundedImageView = findViewById(R.id.add_new_contact_rounded_image_view_id)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        add_new_contact_RoundedImageView!!.setOnClickListener {
            SelectImage()
        }

        add_new_contact_imgString = imageToBase64(add_new_contact_RoundedImageView!!)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (isEmptyField(add_new_contact_FirstName) && isEmptyField(add_new_contact_LastName) && isEmptyField(add_new_contact_PhoneNumber) && isEmptyField(add_new_contact_Email)) {
                    val intent = Intent(this@AddNewContactActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    var alertDialog = AlertDialog.Builder(this)
                    alertDialog.setTitle("Attention")
                    alertDialog.setMessage("Vous risquez de perdre toutes vos champs, voulez vous vraiment continuer ?")

                    alertDialog.setPositiveButton("Oui", { _, _ ->

                        val intent = Intent(this@AddNewContactActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    })

                    alertDialog.setNegativeButton("Non", { _, _ ->
                    })
                    alertDialog.show()
                }
            }
            R.id.nav_validate -> if (isEmptyField(add_new_contact_FirstName)) {
                Toast.makeText(this, "Le champ prénom ne peut pas être vide !", Toast.LENGTH_SHORT).show()
            } else {
                if (isValidMobile(add_new_contact_PhoneNumber!!.text.toString())) {
                    val printContacts = Runnable {
                        val contactData = Contacts(null, add_new_contact_FirstName!!.text.toString(), add_new_contact_LastName!!.text.toString(), add_new_contact_PhoneNumber!!.text.toString(), add_new_contact_Email!!.text.toString(), R.drawable.img_avatar, R.drawable.aquarius)
                        println(contactData)
                        main_ContactsDatabase?.contactsDao()?.insert(contactData)
                        val intent = Intent(this@AddNewContactActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    main_mDbWorkerThread.postTask(printContacts)
                } else {
                    Toast.makeText(this, "Votre numéro de téléphone n'est pas valide !", Toast.LENGTH_SHORT).show()
                }
            }

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_add_contact, menu)
        return true
    }

    fun isValidMobile(phone: String): Boolean {
        return android.util.Patterns.PHONE.matcher(phone).matches()
    }

    fun isEmptyField(field: EditText?): Boolean {
        return field!!.text.toString().isEmpty()
    }

    fun isValidMail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun SelectImage() {

        val items = arrayOf<CharSequence>("Camera", "Gallery", "Cancel")
        //            ActionBar.DisplayOptions[]

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add Image")
        builder.setItems(items) { dialog, i ->
            if (items[i] == "Camera") {

                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(intent, REQUEST_CAMERA!!)

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

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {

                val bundle = data!!.extras
                val bitmap = bundle!!.get("data") as Bitmap
                add_new_contact_RoundedImageView!!.setImageBitmap(bitmap)

            } else if (requestCode == SELECT_FILE) {
                val selectedImageUri = data!!.data
                add_new_contact_RoundedImageView!!.setImageURI(selectedImageUri)
            }
        }
    }

    fun imageToBase64(img : ImageView) : String
    {
        val baos = ByteArrayOutputStream()
        val bitmap = BitmapFactory.decodeResource(resources, img!!.id)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageBytes = baos.toByteArray()
        val imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT)

        return imageString;
    }
}
