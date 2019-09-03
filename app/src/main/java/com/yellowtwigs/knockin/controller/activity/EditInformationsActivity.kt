package com.yellowtwigs.knockin.controller.activity

import android.Manifest
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
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

import com.yellowtwigs.knockin.R

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.yellowtwigs.knockin.controller.CircularImageView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.io.ByteArrayOutputStream

/**
 * La Classe qui permet d'éditer son propre profil
 * @author Kenzy Suon
 */
class EditInformationsActivity : AppCompatActivity() {

    //region ========================================== Var or Val ==========================================

    private var drawerLayout: DrawerLayout? = null
    private var edit_information_layout: ConstraintLayout? = null

    private var edit_informations_ProfileImage: CircularImageView? = null
    private var edit_informations_NameLayout: ConstraintLayout? = null
    private var edit_informations_MailLayout: ConstraintLayout? = null
    private var edit_informations_PhoneNumberLayout: ConstraintLayout? = null


    private var edit_informations_ModeEditionActivated: ImageView? = null
    private var edit_informations_ModeEditionDesactivated: ImageView? = null


    private var edit_informations_EditName: TextInputLayout? = null
    private var edit_informations_EditMail: TextInputLayout? = null
    private var edit_informations_EditPhoneNumber: TextInputLayout? = null

    private var edit_informations_InputNumber: TextInputEditText? = null
    private var edit_informations_InputEmail: TextInputEditText? = null
    private var edit_informations_InputName: TextInputEditText? = null

    private var edit_informations_NameText: TextView? = null
    private var edit_informations_MailText: TextView? = null
    private var edit_informations_PhoneNumberText: TextView? = null

    //private var REQUEST_CAMERA: Int? = 1
    private var SELECT_FILE: Int? = 0
    private var add_new_contact_ImgString: String? = ""

    private var imageUri: Uri? = null
    private val IMAGE_CAPTURE_CODE = 1001

    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedThemePreferences = getSharedPreferences("Knocker_Theme", Context.MODE_PRIVATE)
        if(sharedThemePreferences.getBoolean("darkTheme",false)){
            setTheme(R.style.AppThemeDark)
        }else{
            setTheme(R.style.AppTheme)
        }
        setContentView(R.layout.activity_edit_informations)

        //region ========================================== Toolbar =========================================

        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar.setHomeAsUpIndicator(R.drawable.ic_open_drawer)

        //endregion

        //region ======================================= DrawerLayout =======================================

        // Drawerlayout
        drawerLayout = findViewById(R.id.drawer_layout)

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        val menu = navigationView.menu
        val navItem = menu.findItem(R.id.nav_informations)
        navItem.isChecked = true

        navigationView!!.menu.getItem(1).isChecked = true

        navigationView.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            drawerLayout!!.closeDrawers()

            when (menuItem.itemId) {

                R.id.nav_home -> {
                    startActivity(Intent(this@EditInformationsActivity, MainActivity::class.java))
                }
                R.id.nav_notif_config -> startActivity(Intent(this@EditInformationsActivity, ManageNotificationActivity::class.java))
                R.id.nav_settings -> startActivity(Intent(this@EditInformationsActivity, SettingsActivity::class.java))
                R.id.nav_manage_screen -> startActivity(Intent(this@EditInformationsActivity, ManageMyScreenActivity::class.java))
                R.id.nav_data_access -> {
                }
                R.id.nav_knockons -> startActivity(Intent(this@EditInformationsActivity, ManageKnockonsActivity::class.java))
                R.id.nav_statistics -> {
                }
                R.id.nav_help -> startActivity(Intent(this@EditInformationsActivity, HelpActivity::class.java))
            }

            val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
            drawer.closeDrawer(GravityCompat.START)
            true
        }
        //endregion

        //region ======================================= FindViewById =======================================

        edit_informations_ProfileImage = findViewById(R.id.edit_informations_profile_image)
        edit_informations_NameLayout = findViewById(R.id.edit_informations_name_layout)
        edit_informations_PhoneNumberLayout = findViewById(R.id.edit_informations_mail_layout)
        edit_informations_MailLayout = findViewById(R.id.edit_informations_phone_number_layout)

        edit_informations_ModeEditionActivated = findViewById(R.id.edit_informations_mode_edition_activated)
        edit_informations_ModeEditionDesactivated = findViewById(R.id.edit_informations_mode_edition_desactivated)

        edit_informations_EditName = findViewById(R.id.edit_informations_edit_name)
        edit_informations_EditMail = findViewById(R.id.edit_informations_edit_mail)
        edit_informations_EditPhoneNumber = findViewById(R.id.edit_informations_edit_phone_number)

        edit_informations_InputNumber = findViewById(R.id.edit_informations_input_number)
        edit_informations_InputEmail = findViewById(R.id.edit_informations_input_email)
        edit_informations_InputName = findViewById(R.id.edit_informations_input_name)

        edit_informations_MailText = findViewById(R.id.edit_informations_email_text)
        edit_informations_NameText = findViewById(R.id.edit_informations_name_text)
        edit_informations_PhoneNumberText = findViewById(R.id.edit_informations_number_text)
        edit_information_layout = findViewById(R.id.my_informations_layout_id)
        //endregion

        //region ========================================= Listener =========================================

        edit_informations_ProfileImage!!.setOnClickListener {
            selectImage()
        }

        edit_informations_ModeEditionActivated!!.setOnClickListener {
            modeEditionActivated()
        }

        edit_informations_ModeEditionDesactivated!!.setOnClickListener {
            modeEditionDesactivated()
        }

        // endregion
        // disable keyboard
        edit_information_layout!!.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                val view = this@EditInformationsActivity.currentFocus
                val imm = this@EditInformationsActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                if (view != null) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0)
                }
                return true
            }
        })
        //
        val sharedPreferenceInfo = getSharedPreferences("My_info", Context.MODE_PRIVATE)
        edit_informations_NameText!!.setText(sharedPreferenceInfo.getString("name", "Nom"))
        edit_informations_MailText!!.setText(sharedPreferenceInfo.getString("email", "Email"))
        edit_informations_PhoneNumberText!!.setText(sharedPreferenceInfo.getString("number", "Numéro de téléphone"))
        val photo = sharedPreferenceInfo.getString("photo", "")
        if (photo != null && photo != "") {
            edit_informations_ProfileImage!!.setImageBitmap(base64ToBitmap(photo))
        }
    }
    //region ========================================== Functions ===========================================

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.toolbar_menu_help, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun selectImage() {

        val items = arrayOf<CharSequence>(getString(R.string.edit_contact_alert_dialog_photo_camera)
                , getString(R.string.edit_contact_alert_dialog_photo_galery)
                , getString(R.string.edit_contact_alert_dialog_photo_cancel))
        //            ActionBar.DisplayOptions[]
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle( R.string.edit_contact_alert_dialog_photo_title)
        builder.setItems(items) { dialog, i ->
            if (items[i] == getString(R.string.edit_contact_alert_dialog_photo_camera)) {
                openCamera()

            } else if (items[i] ==getString(R.string.edit_contact_alert_dialog_photo_galery)) {

                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                intent.type = "image/*"
                startActivityForResult(Intent.createChooser(intent,  getString(R.string.edit_contact_intent_title)), SELECT_FILE!!)

            } else if (items[i] == getString(R.string.edit_contact_alert_dialog_photo_cancel)) {
                dialog.dismiss()
            }
        }
        builder.show()
    }//TODO : enlever code duplicate avec edit et add new contact

    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, R.string.edit_contact_camera_open_title)
        values.put(MediaStore.Images.Media.DESCRIPTION, R.string.edit_contact_camera_open_description)
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

                val matrix = Matrix()
                val exif = ExifInterface(getRealPathFromUri(this, imageUri!!))
                val rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                val rotationInDegrees = exifToDegrees(rotation)
                matrix.postRotate(rotationInDegrees.toFloat())
                var bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.width / 10, bitmap.height / 10, true)
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                edit_informations_ProfileImage!!.setImageBitmap(bitmap)
                add_new_contact_ImgString = bitmapToBase64(bitmap)
                val sharedPreferenceInfo = getSharedPreferences("My_info", Context.MODE_PRIVATE)
                val edit: SharedPreferences.Editor = sharedPreferenceInfo.edit()
                edit.putString("photo", add_new_contact_ImgString)
                edit.apply()

            } else if (requestCode == SELECT_FILE) {
                val matrix = Matrix()
                val selectedImageUri = data!!.data
                val exif = ExifInterface(getRealPathFromUri(this, selectedImageUri!!))
                val rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                val rotationInDegrees = exifToDegrees(rotation)
                matrix.postRotate(rotationInDegrees.toFloat())
                var bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)
                bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.width / 10, bitmap.height / 10, true)
//                var matrix = Matrix()
//                matrix.postRotate(90f)
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                edit_informations_ProfileImage!!.setImageBitmap(bitmap)
                add_new_contact_ImgString = bitmapToBase64(bitmap)
                val sharedPreferenceInfo = getSharedPreferences("My_info", Context.MODE_PRIVATE)
                val edit: SharedPreferences.Editor = sharedPreferenceInfo.edit()
                edit.putString("photo", add_new_contact_ImgString)
                edit.apply()
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

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val imageBytes = baos.toByteArray()

        return Base64.encodeToString(imageBytes, Base64.DEFAULT)
    }

    private fun base64ToBitmap(base64: String): Bitmap {
        val imageBytes = Base64.decode(base64, 0)
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                drawerLayout!!.openDrawer(GravityCompat.START)
                hideKeyboard()
                return true
            }
            R.id.item_help -> {
                MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                        .setTitle(R.string.help)
                        .setMessage(this.resources.getString(R.string.help_my_informations))
                        .show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun modeEditionActivated() {
        val sharedPreferenceInfo = getSharedPreferences("My_info", Context.MODE_PRIVATE)
        edit_informations_InputName!!.setText(sharedPreferenceInfo.getString("name", ""))
        edit_informations_NameLayout!!.visibility = View.GONE
        edit_informations_EditName!!.visibility = View.VISIBLE
        edit_informations_InputEmail!!.setText(sharedPreferenceInfo.getString("email", ""))
        edit_informations_MailLayout!!.visibility = View.GONE
        edit_informations_EditMail!!.visibility = View.VISIBLE
        edit_informations_InputNumber!!.setText(sharedPreferenceInfo.getString("number", ""))
        edit_informations_PhoneNumberLayout!!.visibility = View.GONE
        edit_informations_EditPhoneNumber!!.visibility = View.VISIBLE
        edit_informations_ModeEditionDesactivated!!.visibility = View.VISIBLE
        edit_informations_ModeEditionActivated!!.visibility = View.GONE


        hideKeyboard()
        Toast.makeText(this@EditInformationsActivity, R.string.edit_information_toast_enable, Toast.LENGTH_SHORT).show()
    }

    private fun modeEditionDesactivated() {
        val sharedPreferenceInfo = getSharedPreferences("My_info", Context.MODE_PRIVATE)
        val edit: SharedPreferences.Editor = sharedPreferenceInfo.edit()
        edit.putString("name", edit_informations_InputName!!.text.toString())
        edit.putString("email", edit_informations_InputEmail!!.text.toString())
        edit.putString("number", edit_informations_InputNumber!!.text.toString())
        edit.apply()
/*
        val name = sharedPreferenceInfo.getString("name", "Nom")
        val email = sharedPreferenceInfo.getString("email", "Email")
        val number = sharedPreferenceInfo.getString("number", "Numéro de téléphone")
        edit_informations_NameText!!.setText(name)
        edit_informations_NameLayout!!.visibility = View.VISIBLE
        edit_informations_EditName!!.visibility = View.GONE
        edit_informations_MailText!!.setText(email)
        edit_informations_MailLayout!!.visibility = View.VISIBLE
        edit_informations_EditMail!!.visibility = View.GONE
        edit_informations_PhoneNumberText!!.setText(number)
        edit_informations_PhoneNumberLayout!!.visibility = View.VISIBLE
        edit_informations_EditPhoneNumber!!.visibility = View.GONE
        edit_informations_ModeEditionActivated!!.visibility = View.VISIBLE
        edit_informations_ModeEditionDesactivated!!.visibility = View.GONE
*/
        hideKeyboard()
        Toast.makeText(this@EditInformationsActivity, R.string.edit_information_toast_disable, Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun hideKeyboard() {
        // Check if no view has focus:
        val view = this.currentFocus

        view?.let { v ->
            val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }

    //endregion
}
