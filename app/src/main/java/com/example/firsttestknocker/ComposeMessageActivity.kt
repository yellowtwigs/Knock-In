package com.example.firsttestknocker

import android.Manifest
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatDelegate
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.telephony.SmsManager
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast

class ComposeMessageActivity : AppCompatActivity() {

    private var compose_message_MessageEditText: EditText? = null
    private var compose_message_PhoneNumberEditText: EditText? = null

    private var compose_message_Attachement: ImageView? = null
    private var compose_message_Attachement_Blue: ImageView? = null
    private var compose_message_send_Button: ImageView? = null

    private var compose_message_layout_Attachement: ConstraintLayout? = null
    private var compose_message_RecyclerView: RecyclerView? = null

    private var compose_message_phone_number: String? = null
    private var compose_message_phone_property: String? = null

    private val SEND_SMS_PERMISSION_REQUEST_CODE = 111


    override fun onCreate(savedInstanceState: Bundle?) {
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.DarkTheme)
        } else {
            setTheme(R.style.AppTheme)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compose_message)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT)
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar.setHomeAsUpIndicator(R.drawable.ic_left_arrow)
        actionbar.title = "Envoyer un sms"

        compose_message_MessageEditText = findViewById(R.id.compose_message_chatbox)
        compose_message_PhoneNumberEditText = findViewById(R.id.compose_message_phone_number_edittext)
        compose_message_Attachement = findViewById(R.id.compose_message_attachement)
        compose_message_Attachement_Blue = findViewById(R.id.compose_message_attachement_blue)
        compose_message_send_Button = findViewById(R.id.compose_message_chatbox_send)
        compose_message_send_Button!!.isEnabled = false
        compose_message_layout_Attachement = findViewById(R.id.compose_message_layout_attachement)

        var tmp = intent.getStringExtra("ContactPhoneNumber")
        println("tmp " + tmp);
        if(tmp != null){
            compose_message_phone_number = NumberAndMailDB.numDBAndMailDBtoDisplay(tmp)
            compose_message_phone_property = NumberAndMailDB.extractStringFromNumber(tmp)
        }

        compose_message_PhoneNumberEditText!!.setText(compose_message_phone_number)

        compose_message_Attachement!!.setOnClickListener(View.OnClickListener {
            compose_message_layout_Attachement!!.visibility = View.VISIBLE
            compose_message_Attachement_Blue!!.visibility = View.VISIBLE
            compose_message_Attachement!!.visibility = View.GONE
        })

        compose_message_Attachement_Blue!!.setOnClickListener(View.OnClickListener {
            compose_message_layout_Attachement!!.visibility = View.GONE
            compose_message_Attachement!!.visibility = View.VISIBLE
            compose_message_Attachement_Blue!!.visibility = View.GONE
        })

        if (checkPermission(Manifest.permission.SEND_SMS)) {
            compose_message_send_Button!!.isEnabled = true
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), SEND_SMS_PERMISSION_REQUEST_CODE)
        }

        compose_message_send_Button!!.setOnClickListener(View.OnClickListener {
            val msg = compose_message_MessageEditText!!.text.toString()
            val phoneNumb = compose_message_PhoneNumberEditText!!.text.toString()

            if (!TextUtils.isEmpty(msg) && !TextUtils.isEmpty(phoneNumb)) {
                if (checkPermission(Manifest.permission.SEND_SMS)) {
                    val smsManager = SmsManager.getDefault()
                    smsManager.sendTextMessage(phoneNumb, null, msg, null, null)
                    compose_message_MessageEditText!!.getText().clear()
                    Toast.makeText(this, "Message envoyé", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ComposeMessageActivity, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this@ComposeMessageActivity, "Enter a message and a phone number", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Intent to return to the MainActivity
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun checkPermission(permission: String): Boolean {
        val checkPermission = ContextCompat.checkSelfPermission(this, permission)
        return checkPermission == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {

            SEND_SMS_PERMISSION_REQUEST_CODE -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                compose_message_send_Button!!.isEnabled = true
            }
        }
    }
}
