package com.example.knocker.controller.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import com.example.knocker.R
import com.example.knocker.controller.ContactListViewAdapter
import com.example.knocker.model.ContactList
import com.example.knocker.model.ModelDB.ContactWithAllInformation
import android.telephony.SmsManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class MultiChannelActivity : AppCompatActivity() {

    //region ========================================== Val or Var ==========================================

    private var multi_channel_Listview: ListView? = null

    private var intent_listOfContactSelected: ArrayList<Int> = ArrayList()

    private var multi_channel_listOfContactSelected: ArrayList<ContactWithAllInformation?> = ArrayList()

    private var gestionnaireContacts: ContactList? = null

    private var multi_channel_listViewAdapter: ContactListViewAdapter? = null

    private var multi_channel_SendMessageEditText: AppCompatEditText? = null
    private var multi_channel_SendMessageButton: AppCompatImageView? = null

    private val SEND_SMS_PERMISSION_REQUEST_CODE = 1
    private val MY_PERMISSIONS_REQUEST_RECEIVE_SMS = 0

    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multi_channel)

        intent_listOfContactSelected = intent.getIntegerArrayListExtra("ListContactsSelected")

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECEIVE_SMS)) {
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECEIVE_SMS), MY_PERMISSIONS_REQUEST_RECEIVE_SMS)
            }
        }

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

        gestionnaireContacts = ContactList(this.applicationContext)
        gestionnaireContacts!!.sortContactByFirstNameAZ()

        val iterator = (0 until intent_listOfContactSelected.size).iterator()

        for (i in iterator) {
            multi_channel_listOfContactSelected.add(gestionnaireContacts!!.getContactById(intent_listOfContactSelected[i]))
        }

        multi_channel_listViewAdapter = ContactListViewAdapter(this, multi_channel_listOfContactSelected, false)

        multi_channel_Listview!!.adapter = multi_channel_listViewAdapter


        multi_channel_SendMessageButton!!.setOnClickListener {
            if (multi_channel_SendMessageEditText!!.text.toString() != "") {
                if (multi_channel_listViewAdapter!!.listOfNumberSelected.size != 0) {
                    if (checkPermission(Manifest.permission.SEND_SMS)) {
                        multiChannelSendMessage(multi_channel_listViewAdapter!!.listOfNumberSelected, multi_channel_SendMessageEditText!!.text.toString())
                    } else {
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), SEND_SMS_PERMISSION_REQUEST_CODE)
                        Toast.makeText(this, "No Permission", Toast.LENGTH_SHORT).show()
                    }
                }

                if (multi_channel_listViewAdapter!!.listOfMailSelected.size != 0) {
                    multiChannelMailClick(multi_channel_listViewAdapter!!.listOfMailSelected, multi_channel_SendMessageEditText!!.text.toString())
                }

                refreshActivity()
            } else {
                Toast.makeText(this, "Votre message ne doit pas être vide", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //region ======================================= Functions ==============================================

    private fun refreshActivity() {
        multi_channel_SendMessageEditText!!.text!!.clear()
        multi_channel_listViewAdapter = ContactListViewAdapter(this, multi_channel_listOfContactSelected, true)
        multi_channel_Listview!!.adapter = multi_channel_listViewAdapter
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                startActivity(Intent(this@MultiChannelActivity, MainActivity::class.java))
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
//        var numbers = "" + listOfPhoneNumber[0]
        for (i in 0 until listOfPhoneNumber.size) {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(listOfPhoneNumber[i], null, msg, null, null)
        }
        Toast.makeText(applicationContext, "Message Sent",
                Toast.LENGTH_LONG).show()
    }

    private fun multiChannelMailClick(listOfMail: ArrayList<String>, msg: String) {

//        val contact = listOfMail.toArray(arrayOfNulls<String>(listOfMail.size))
//        val emailIntent = Intent(Intent.ACTION_SEND)
//
//        emailIntent.type = "text/plain"
//        emailIntent.putExtra(Intent.EXTRA_EMAIL, contact)
//        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Send from Knocker")
//        emailIntent.putExtra(Intent.EXTRA_TEXT, msg)
//
//        try {
//            startActivity(Intent.createChooser(emailIntent, "Send email using..."))
//        } catch (ex: ActivityNotFoundException) {
//            Toast.makeText(this, "No email clients installed.", Toast.LENGTH_SHORT).show()
//        }

        val intent = Intent(Intent.ACTION_SEND)
        val contact = listOfMail.toArray(arrayOfNulls<String>(listOfMail.size))
        intent.putExtra(Intent.EXTRA_EMAIL, contact)
        intent.data = Uri.parse("mailto:")
        intent.type = "text/plain"
      //  intent.putExtra(Intent.EXTRA_SUBJECT, "Send from Knocker")
        intent.putExtra(Intent.EXTRA_TEXT, msg)

        startActivity(intent)
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