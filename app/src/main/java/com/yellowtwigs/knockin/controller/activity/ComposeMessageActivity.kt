package com.yellowtwigs.knockin.controller.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.widget.Toolbar
import android.telephony.SmsManager
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.Toast
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.controller.MessageListAdapter
import com.yellowtwigs.knockin.model.Message
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList

/**
 * La Classe qui permet de composer un message à un numero rentré
 * @author Florian Striebel, Kenzy Suon
 */
class ComposeMessageActivity : AppCompatActivity() {

    //region ========================================== Var or Val ==========================================

    private var compose_message_MessageEditText: EditText? = null
    private var compose_message_PhoneNumberEditText: EditText? = null

    private var compose_message_Attachement: ImageView? = null
    private var compose_message_Attachement_Blue: ImageView? = null
    private var compose_message_send_Button: ImageView? = null

    private var compose_message_ListViewMessage: ListView? = null

    private var compose_message_layout_Attachement: ConstraintLayout? = null

    private var compose_message_phone_number: String? = null
    private var compose_message_phone_property: String? = null

    private var compose_message_listOfMessage = ArrayList<Message>()

    private val SEND_SMS_PERMISSION_REQUEST_CODE = 1
    private val MY_PERMISSIONS_REQUEST_RECEIVE_SMS = 0

    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compose_message)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECEIVE_SMS)) {

            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECEIVE_SMS), MY_PERMISSIONS_REQUEST_RECEIVE_SMS)
            }
        }

        //region ========================================== Toolbar =========================================

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar.setHomeAsUpIndicator(R.drawable.ic_left_arrow)

        //endregion

        //region ====================================== FindViewById() ======================================

        compose_message_MessageEditText = findViewById(R.id.compose_message_chatbox)
        compose_message_PhoneNumberEditText = findViewById(R.id.compose_message_phone_number_edittext)
        compose_message_Attachement = findViewById(R.id.compose_message_attachement)
        compose_message_Attachement_Blue = findViewById(R.id.compose_message_attachement_blue)
        compose_message_send_Button = findViewById(R.id.compose_message_chatbox_send)
        compose_message_send_Button!!.isEnabled = false
        compose_message_layout_Attachement = findViewById(R.id.compose_message_layout_attachement)

        compose_message_ListViewMessage = findViewById(R.id.compose_message_list_view_message)

        //endregion


        val intent = intent
        if (intent.getStringExtra("ContactPhoneNumber")!!.isNotEmpty()) {
            compose_message_phone_number = intent.getStringExtra("ContactPhoneNumber")
            compose_message_PhoneNumberEditText!!.setText(compose_message_phone_number)
        }

        val tmp = intent.getStringExtra("ContactPhoneNumber")
        if (tmp != null) {
            compose_message_phone_number = tmp
            compose_message_phone_property = " "
        }

        compose_message_PhoneNumberEditText!!.setText(compose_message_phone_number)

        if (checkPermission(Manifest.permission.SEND_SMS)) {
            compose_message_send_Button!!.isEnabled = true
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), SEND_SMS_PERMISSION_REQUEST_CODE)
        }
        ///problem with editText he needs delay to be editable with keyboard
        val inputMM = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        compose_message_MessageEditText!!.postDelayed({
            compose_message_MessageEditText!!.requestFocus()
            inputMM.showSoftInput(compose_message_MessageEditText, InputMethodManager.SHOW_IMPLICIT)
        }, 100)
        //inputMM.showSoftInput(compose_message_MessageEditText, InputMethodManager.SHOW_IMPLICIT)


        //region ==================================== SetOnClickListener ====================================

        compose_message_Attachement!!.setOnClickListener{
            compose_message_layout_Attachement!!.visibility = View.VISIBLE
            compose_message_Attachement_Blue!!.visibility = View.VISIBLE
            compose_message_Attachement!!.visibility = View.INVISIBLE
            compose_message_Attachement!!.isClickable = false
            compose_message_Attachement_Blue!!.isClickable = true
        }

        compose_message_Attachement_Blue!!.setOnClickListener {
            compose_message_layout_Attachement!!.visibility = View.GONE
            compose_message_Attachement!!.visibility = View.VISIBLE
            compose_message_Attachement_Blue!!.visibility = View.INVISIBLE
            compose_message_Attachement!!.isClickable = true
            compose_message_Attachement_Blue!!.isClickable = false
        }

        compose_message_send_Button!!.setOnClickListener {
            val msg = compose_message_MessageEditText!!.text.toString()
            val phoneNumb = compose_message_PhoneNumberEditText!!.text.toString()

            val date = SimpleDateFormat("dd/M/yyyy")
            val hour = SimpleDateFormat("hh:mm:ss")
            val currentDate = date.format(Date())
            val currentHour = hour.format(Date())

            if (!TextUtils.isEmpty(msg) && !TextUtils.isEmpty(phoneNumb)) {
                if (checkPermission(Manifest.permission.SEND_SMS)) {
                    val smsManager = SmsManager.getDefault()
                    smsManager.sendTextMessage(phoneNumb, null, msg, null, null)

                    val message = Message(msg, true, "", 0, currentDate, currentHour)

                    compose_message_listOfMessage.add(message)

                    compose_message_ListViewMessage!!.adapter = MessageListAdapter(this, compose_message_listOfMessage)

                    compose_message_MessageEditText!!.text.clear()
                    Toast.makeText(this, getString(R.string.compose_message_toast_send), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ComposeMessageActivity,getString(R.string.compose_message_toast_permission_denied), Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this@ComposeMessageActivity, getString(R.string.compose_message_toast_miss_phone), Toast.LENGTH_SHORT).show()
            }
        }

        //endregion
    }

    //region ========================================== Functions ===========================================

    // Intent to return to the MainActivity
    @SuppressLint("ShowToast")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            R.id.menu_compose_message_phone_call -> {
                if (isValidPhone(compose_message_PhoneNumberEditText!!.text.toString())) {
                    phoneCall(compose_message_PhoneNumberEditText!!.text.toString())
                } else {
                    Toast.makeText(this, "Enter a phone number valid", Toast.LENGTH_SHORT)
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_compose_message, menu)
        return true
    }

    private fun checkPermission(permission: String): Boolean {
        val checkPermission = ContextCompat.checkSelfPermission(this, permission)
        return checkPermission == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            SEND_SMS_PERMISSION_REQUEST_CODE -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                compose_message_send_Button!!.isEnabled = true
            }
            MY_PERMISSIONS_REQUEST_RECEIVE_SMS -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Thank You for permitting !", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Can't do anything until you permit me !", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("ShowToast")
    private fun phoneCall(phoneNumberEntered: String) {
        if (!TextUtils.isEmpty(phoneNumberEntered)) {
            if (isValidPhone(phoneNumberEntered)) {
                val dial = "tel:$phoneNumberEntered"
                startActivity(Intent(Intent.ACTION_DIAL, Uri.parse(dial)))
            } else {
                Toast.makeText(this, "Enter a phone number valid", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Enter a phone number", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isValidPhone(phone: String): Boolean {
        val expression = "^(?:(?:\\+|00)33[\\s.-]{0,3}(?:\\(0\\)[\\s.-]{0,3})?|0)[1-9](?:(?:[\\s.-]?\\d{2}){4}|\\d{2}(?:[\\s.-]?\\d{3}){2})\$"
        val pattern = Pattern.compile(expression)
        val matcher = pattern.matcher(phone)
        return matcher.matches()
    }

    //endregion
}
