package com.yellowtwigs.knockin.controller.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.ui.adapters.ContactListViewAdapter
import com.yellowtwigs.knockin.controller.activity.group.GroupManagerActivity
import com.yellowtwigs.knockin.model.ContactManager
import com.yellowtwigs.knockin.model.ModelDB.ContactWithAllInformation

class MultiChannelActivity : AppCompatActivity() {

    //region ========================================== Val or Var ==========================================

    private var multi_channel_Listview: ListView? = null

    private var intent_listOfContactSelected: ArrayList<Int> = ArrayList()

    private var multi_channel_listOfContactSelected: ArrayList<ContactWithAllInformation?> = ArrayList()

    private var gestionnaireContacts: ContactManager? = null

    private var multi_channel_listViewAdapter: ContactListViewAdapter? = null

    private var multi_channel_SendMessageEditText: AppCompatEditText? = null
    private var multi_channel_SendMessageButton: AppCompatImageView? = null

    private val SEND_SMS_PERMISSION_REQUEST_CODE = 1
    private val MY_PERMISSIONS_REQUEST_RECEIVE_SMS = 0

    private var sendValidate = false
    private var multiChannelActivity = false

    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //region ======================================== Theme Dark ========================================

        val sharedThemePreferences = getSharedPreferences("Knockin_Theme", Context.MODE_PRIVATE)
        if (sharedThemePreferences.getBoolean("darkTheme", false)) {
            setTheme(R.style.AppThemeDark)
        } else {
            setTheme(R.style.AppTheme)
        }

        //endregion

        setContentView(R.layout.activity_multi_channel)

//        askForSMSPermissions()

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

        intent_listOfContactSelected = intent.getIntegerArrayListExtra("ListContactsSelected") as ArrayList<Int>

        gestionnaireContacts = ContactManager(this.applicationContext)
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

        multi_channel_SendMessageButton!!.setOnClickListener  {
            if (multi_channel_SendMessageEditText!!.text.toString() != "") {
                if (multi_channel_listViewAdapter!!.listOfNumberSelected.size != 0 && multi_channel_listViewAdapter!!.listOfMailSelected.size != 0) {
                    multiChannelSendMessage(multi_channel_listViewAdapter!!.listOfNumberSelected, multi_channel_SendMessageEditText!!.text.toString())

                    multiChannelActivity = true

                } else if (multi_channel_listViewAdapter!!.listOfNumberSelected.size != 0 && multi_channel_listViewAdapter!!.listOfMailSelected.size == 0) {
                    multiChannelSendMessage(multi_channel_listViewAdapter!!.listOfNumberSelected, multi_channel_SendMessageEditText!!.text.toString())

                    sendValidate = true

                } else if (multi_channel_listViewAdapter!!.listOfMailSelected.size != 0 && multi_channel_listViewAdapter!!.listOfNumberSelected.size == 0) {
                    multiChannelMailClick(multi_channel_listViewAdapter!!.listOfMailSelected, multi_channel_SendMessageEditText!!.text.toString())
                }

                if (multi_channel_listViewAdapter!!.listOfMailSelected.size == 0 && multi_channel_listViewAdapter!!.listOfNumberSelected.size == 0) {
                    Toast.makeText(this, getString(R.string.multi_channel_list_of_channel_selected_empty), Toast.LENGTH_LONG).show()
                    sendValidate = false
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
        if (intent.getBooleanExtra("fromMainToMultiChannel", false)) {
            startActivity(Intent(this@MultiChannelActivity, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
            hideKeyboard()
            finish()
        } else {
            startActivity(Intent(this@MultiChannelActivity, GroupManagerActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
            hideKeyboard()
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                refreshActivity()
            }
        }
        return super.onOptionsItemSelected(item)
    }

//    private fun checkPermission(permission: String): Boolean {
//        val checkPermission = ContextCompat.checkSelfPermission(this, permission)
//        return checkPermission == PackageManager.PERMISSION_GRANTED
//    }

//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
//        when (requestCode) {
//            SEND_SMS_PERMISSION_REQUEST_CODE -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                multi_channel_SendMessageButton!!.isEnabled = true
//            }
//            MY_PERMISSIONS_REQUEST_RECEIVE_SMS -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(this, "Thank You for permitting !", Toast.LENGTH_SHORT).show()
//            } else {
//                Toast.makeText(this, "Can't do anything until you permit me !", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }

//    private fun askForSMSPermissions() {
//        if (!checkPermission(Manifest.permission.SEND_SMS)) {
//            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), SEND_SMS_PERMISSION_REQUEST_CODE)
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
//                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECEIVE_SMS)) {
//                } else {
//                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECEIVE_SMS), MY_PERMISSIONS_REQUEST_RECEIVE_SMS)
//                }
//            }
//        } else {
//        }
//    }

    /**
     * On ouvre l'application de SMS avec tous les contacts saisis lors du multiselect
     * @param listOfPhoneNumber [ArrayList<String>]
     */
    private fun multiChannelSendMessage(listOfPhoneNumber: ArrayList<String>, msg: String) {

        var message = "smsto:" + listOfPhoneNumber[0]
        for (i in 0 until listOfPhoneNumber.size) {
            message += ";" + listOfPhoneNumber[i]
        }
        startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse(message)).putExtra("sms_body", msg))
    }

//    private fun multiChannelSendMessage(listOfPhoneNumber: ArrayList<String>, msg: String) {
//        for (i in 0 until listOfPhoneNumber.size) {
//            val smsManager = SmsManager.getDefault()
//            smsManager.sendTextMessage(listOfPhoneNumber[i], null, msg, null, null)
//        }
//        Toast.makeText(applicationContext, getString(R.string.multi_channel_message_sent),
//                Toast.LENGTH_LONG).show()
//    }

    private fun multiChannelMailClick(listOfMail: ArrayList<String>, msg: String) {
        val intent = Intent(Intent.ACTION_SEND)
        val contact = listOfMail.toArray(arrayOfNulls<String>(listOfMail.size))
        intent.putExtra(Intent.EXTRA_EMAIL, contact)
        intent.data = Uri.parse("mailto:")
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, msg)

        sendValidate = true
        multiChannelActivity = false

        startActivity(intent)
    }

    private fun hideKeyboard() {
        val view = this.currentFocus

        view?.let { v ->
            val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }

    override fun onResume() {
        super.onResume()

        if (sendValidate) {
            refreshActivity()
        }

        if (multiChannelActivity) {
            multiChannelMailClick(multi_channel_listViewAdapter!!.listOfMailSelected, multi_channel_SendMessageEditText!!.text.toString())
        }
    }

    override fun onBackPressed() {
        refreshActivity()
    }

    //endregion
}