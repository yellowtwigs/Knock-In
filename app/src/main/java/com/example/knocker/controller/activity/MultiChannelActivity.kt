package com.example.knocker.controller.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.telephony.SmsManager
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.ListView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.knocker.R
import com.example.knocker.controller.ContactListViewAdapter
import com.example.knocker.model.ContactList
import com.example.knocker.model.ModelDB.ContactWithAllInformation

class MultiChannelActivity : AppCompatActivity() {

    //region ========================================== Val or Var ==========================================

    private var multi_channel_Listview: ListView? = null

    private var intent_listOfContactSelected: ArrayList<Int> = ArrayList()

    private var multi_channel_listOfContactSelected: ArrayList<ContactWithAllInformation?> = ArrayList()

    private var gestionnaireContacts: ContactList? = null

    private var multi_channel_listViewAdapter: ContactListViewAdapter? = null

    private var multi_channel_SendMessageEditText: AppCompatEditText? = null
    private var multi_channel_SendMessageButton: RelativeLayout? = null

    private val SEND_SMS_PERMISSION_REQUEST_CODE = 1
    private val MY_PERMISSIONS_REQUEST_RECEIVE_SMS = 0
    private val SPLASH_DISPLAY_LENGHT = 10000

    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multi_channel)

        //region ========================================== Toolbar =========================================

        val toolbar = findViewById<Toolbar>(R.id.multi_channel_toolbar)
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar.setHomeAsUpIndicator(R.drawable.ic_left_arrow)
        actionbar.title = "Multi Channel"

        //endregion

        //region ======================================= FindViewById =======================================

        multi_channel_Listview = findViewById(R.id.multi_channel_list_of_contacts_selected)
        multi_channel_SendMessageEditText = findViewById(R.id.multi_channel_chatbox)
        multi_channel_SendMessageButton = findViewById(R.id.multi_channel_chatbox_send)

        //endregion

        //region ================================= GetContactByIdFromIntent =================================

        intent_listOfContactSelected = intent.getIntegerArrayListExtra("ListContactsSelected")

        gestionnaireContacts = ContactList(this.applicationContext)
        gestionnaireContacts!!.sortContactByFirstNameAZ()

        val iterator = (0 until intent_listOfContactSelected.size).iterator()

        for (i in iterator) {
            multi_channel_listOfContactSelected.add(gestionnaireContacts!!.getContactById(intent_listOfContactSelected[i]))
        }

        //endregion

        //region ================================== ContactListViewAdapter ==================================

        multi_channel_listViewAdapter = ContactListViewAdapter(this, multi_channel_listOfContactSelected)
        multi_channel_Listview!!.adapter = multi_channel_listViewAdapter

        //endregion

        //region ======================================== Listeners =========================================

        multi_channel_SendMessageButton!!.setOnClickListener {
            var sendValidate = false
            var sendValidateWithDelay = false
            if (multi_channel_SendMessageEditText!!.text.toString() != "") {
                if (multi_channel_listViewAdapter!!.listOfNumberSelected.size != 0) {
                    if (checkPermission(Manifest.permission.SEND_SMS)) {
                        multiChannelSendMessage(multi_channel_listViewAdapter!!.listOfNumberSelected, multi_channel_SendMessageEditText!!.text.toString())
                        sendValidate = true
                    } else {
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), SEND_SMS_PERMISSION_REQUEST_CODE)
                        Toast.makeText(this, getString(R.string.multi_channel_no_permission), Toast.LENGTH_SHORT).show()

                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECEIVE_SMS)) {
                            } else {
                                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECEIVE_SMS), MY_PERMISSIONS_REQUEST_RECEIVE_SMS)
                            }
                        }
                    }
                }

                if (multi_channel_listViewAdapter!!.listOfMailSelected.size != 0) {
                    multiChannelMailClick(multi_channel_listViewAdapter!!.listOfMailSelected, multi_channel_SendMessageEditText!!.text.toString())
                    sendValidate = false
                    sendValidateWithDelay = true
                }

                if (multi_channel_listViewAdapter!!.listOfMailSelected.size == 0 && multi_channel_listViewAdapter!!.listOfNumberSelected.size == 0) {
                    Toast.makeText(this, getString(R.string.multi_channel_list_of_channel_selected_empty), Toast.LENGTH_SHORT).show()
                    sendValidate = false
                }

                if (sendValidate) {
                    refreshActivity()
                } else if (sendValidateWithDelay) {
                    refreshActivityWithDelay()
                }
                hideKeyboard()
            } else {
                Toast.makeText(this, getString(R.string.multi_channel_empty_field), Toast.LENGTH_SHORT).show()
                hideKeyboard()
            }
        }

        //endregion
    }

    //region ======================================= Functions ==============================================

    private fun refreshActivity() {
        startActivity(Intent(this@MultiChannelActivity, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
        finish()
    }

    private fun refreshActivityWithDelay() {
        Handler().postDelayed({
            startActivity(Intent(this@MultiChannelActivity, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
            finish()
        }, SPLASH_DISPLAY_LENGHT.toLong())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                startActivity(Intent(this@MultiChannelActivity, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
                finish()
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
            SEND_SMS_PERMISSION_REQUEST_CODE -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                multi_channel_SendMessageButton!!.isEnabled = true
            }
            MY_PERMISSIONS_REQUEST_RECEIVE_SMS -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Thank You for permitting !", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Can't do anything until you permit me !", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun multiChannelSendMessage(listOfPhoneNumber: ArrayList<String>, msg: String) {
        for (i in 0 until listOfPhoneNumber.size) {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(listOfPhoneNumber[i], null, msg, null, null)
        }
        Toast.makeText(applicationContext, getString(R.string.multi_channel_message_sent),
                Toast.LENGTH_LONG).show()
    }

    private fun multiChannelMailClick(listOfMail: ArrayList<String>, msg: String) {
        val intent = Intent(Intent.ACTION_SEND)
        val contact = listOfMail.toArray(arrayOfNulls<String>(listOfMail.size))
        intent.putExtra(Intent.EXTRA_EMAIL, contact)
        intent.data = Uri.parse("mailto:")
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, msg)

        startActivity(intent)
    }

    private fun hideKeyboard() {
        // Check if no view has focus:
        val view = this.currentFocus

        view?.let { v ->
            val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }

    /*private fun multiChannelWhatsapp(listOfPhoneNumber: ArrayList<String>, msg: String) {

        val intent = Intent(Intent.ACTION_VIEW)

        var message = "phone=" + converter06To33(listOfPhoneNumber[0])
        for (i in 1 until listOfPhoneNumber.size) {
            message += "," + converter06To33(listOfPhoneNumber[i])
        }

        for (i in 0 until listOfPhoneNumber.size) {
            intent.data = Uri.parse("http://api.whatsapp.com/send?phone=$message&text=$msg")
        }
        startActivity(intent)
    }*/

    /*private fun converter06To33(phoneNumber: String): String {
        return if (phoneNumber[0] == '0') {
            "+33$phoneNumber"
        } else phoneNumber
    }*/

    //endregion
}