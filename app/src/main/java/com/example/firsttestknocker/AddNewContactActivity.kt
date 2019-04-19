package com.example.firsttestknocker

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.view.*
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
    var imageUri: Uri? = null
    private val IMAGE_CAPTURE_CODE = 1001

    // Database && Thread
    private var main_ContactsDatabase: ContactsRoomDatabase? = null
    private lateinit var main_mDbWorkerThread: DbWorkerThread

    //private var REQUEST_CAMERA: Int? = 1
    private var SELECT_FILE: Int? = 0
    var add_new_contact_imgString: String? = "";

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
//            add_new_contact_imgString = imageToBase64(add_new_contact_RoundedImageView!!)
        }
    }

    //demmande de confirmation de la création d'un contact en double
    private fun confirmationDuplicate(contactData: Contacts){
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("CONTACT DEJA EXISTANT !")
        builder.setMessage("Un contact porte déjà ce nom. L'enregistrer quand même sous ce nom ?")
        builder.setPositiveButton("OUI") { _, _ ->
            main_ContactsDatabase?.contactsDao()?.insert(contactData)
            val intent = Intent(this@AddNewContactActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        builder.setNegativeButton("NON") { _, _ ->
            //NON
        }
        val dialog: android.app.AlertDialog = builder.create()
        dialog.show()
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

                        //check si un contact porte deja ce prénom et nom puis l'ajoute si il y a aucun doublon
                        println("teeeeeeeeeessssssssssssstttttttttt = "+ add_new_contact_imgString!!)
                        val contactData = Contacts(null, add_new_contact_FirstName!!.text.toString(), add_new_contact_LastName!!.text.toString(), add_new_contact_PhoneNumber!!.text.toString(), add_new_contact_Email!!.text.toString(), R.drawable.img_avatar, R.drawable.aquarius, 0, add_new_contact_imgString!!)
                        println(contactData)
                        var isDuplicate = false
                        val allcontacts = main_ContactsDatabase?.contactsDao()?.getAllContacts()
                        allcontacts?.forEach { contactsDB ->
                            if (contactsDB.firstName == contactData.firstName && contactsDB.lastName == contactData.lastName)
                                isDuplicate = true
                        }
                        if (isDuplicate == false) {
                            main_ContactsDatabase?.contactsDao()?.insert(contactData)
                            val intent = Intent(this@AddNewContactActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            confirmationDuplicate(contactData)
                        }
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



    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == IMAGE_CAPTURE_CODE) {

                var bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                bitmap = Bitmap.createScaledBitmap(bitmap,250,200,true)
                var matrix = Matrix()
                matrix.postRotate(90f)
                bitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.width,bitmap.height,matrix, true )
                add_new_contact_RoundedImageView!!.setImageBitmap(bitmap)
                add_new_contact_imgString = bitmapToBase64(bitmap)
                println("convert to = " + base64ToBitmap(bitmapToBase64(bitmap)))
                println("is the same ? result: " + bitmap.sameAs(base64ToBitmap(bitmapToBase64(bitmap))))

            } else if (requestCode == SELECT_FILE) {
                val selectedImageUri = data!!.data
                var bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)
                bitmap = Bitmap.createScaledBitmap(bitmap,250,200,true)
                var matrix = Matrix()
                matrix.postRotate(90f)
                bitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.width,bitmap.height,matrix, true )
                add_new_contact_RoundedImageView!!.setImageBitmap(bitmap)
                add_new_contact_imgString = bitmapToBase64(bitmap)
            }
        }
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
