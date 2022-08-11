package com.yellowtwigs.knockin.ui.contacts

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
import com.yellowtwigs.knockin.model.ContactManager
import com.yellowtwigs.knockin.model.data.ContactWithAllInformation
import com.yellowtwigs.knockin.ui.contacts.list.MainActivity
import com.yellowtwigs.knockin.ui.group.list.GroupManagerActivity
import java.net.URLEncoder


class MultiChannelActivity : AppCompatActivity() {

    //region ========================================== Val or Var ==========================================

    private var appsListView: ListView? = null

    private var intent_listOfContactSelected: ArrayList<Int> = ArrayList()

    private var listOfContactSelected = arrayListOf<ContactWithAllInformation?>()

    private lateinit var contactManager: ContactManager
    private lateinit var viewAdapter: ContactListViewAdapter
    private lateinit var sendMessageEditText: AppCompatEditText
    private lateinit var sendMessageButton: AppCompatImageView

    private var twoChannels = false
    private var tripleChannels = false

    private var sendValidate = false
    private var sendValidateFromMail = false

    private var openMail = false
    private var openWhatsapp = false

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

        //region ========================================== Toolbar =========================================

        val toolbar = findViewById<Toolbar>(R.id.multi_channel_toolbar)
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar
        actionbar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.ic_left_arrow)
            it.title = "Multi Channel"
        }

        //endregion

        //region ======================================= FindViewById =======================================

        appsListView = findViewById(R.id.multi_channel_list_of_contacts_selected)
        sendMessageEditText = findViewById(R.id.multi_channel_chatbox)
        sendMessageButton = findViewById(R.id.multi_channel_chatbox_send)

        //endregion

        //region ================================= GetContactByIdFromIntent =================================

        intent_listOfContactSelected =
            intent.getIntegerArrayListExtra("ListContactsSelected") as ArrayList<Int>

        contactManager = ContactManager(this.applicationContext)
        contactManager.sortContactByFirstNameAZ()

        val iterator = (0 until intent_listOfContactSelected.size).iterator()

        for (i in iterator) {
            listOfContactSelected.add(contactManager.getContactById(intent_listOfContactSelected[i]))
        }

        //endregion

        //region ================================== ContactListViewAdapter ==================================

        viewAdapter = ContactListViewAdapter(this, listOfContactSelected)
        appsListView?.adapter = viewAdapter

        //endregion

        //region ======================================== Listeners =========================================

        sendMessageButton.setOnClickListener {
            val hasSms = viewAdapter.listOfNumberSelected?.size != 0
            val hasWhatsapp = viewAdapter.listOfWhatsappSelected?.size != 0
            val hasEmail = viewAdapter.listOfMailSelected?.size != 0

            if (sendMessageEditText.text.toString() != "") {
                if (!hasSms && !hasWhatsapp && !hasEmail) {
                    Toast.makeText(
                        this,
                        getString(R.string.multi_channel_list_of_channel_selected_empty),
                        Toast.LENGTH_LONG
                    ).show()
                    sendValidate = false
                } else {
                    // Mono Channel
                    if (hasSms && !hasWhatsapp && !hasEmail) {
                        multiChannelSendMessage(
                            viewAdapter.listOfNumberSelected,
                            sendMessageEditText.text.toString()
                        )
                        sendValidate = true
                    } // Validate
                    if (hasWhatsapp && !hasSms && !hasEmail) {
                        multiChannelSendMessageWhatsapp(sendMessageEditText.text.toString())
                        sendValidate = true
                    } // Validate
                    if (hasEmail && !hasSms && !hasWhatsapp) {
                        multiChannelMailClick(
                            viewAdapter.listOfMailSelected,
                            sendMessageEditText.text.toString()
                        )
                        sendValidate = true
                    } // Validate

                    // Two Channels
                    if (hasSms && hasWhatsapp && !hasEmail) {
                        multiChannelSendMessage(
                            viewAdapter.listOfNumberSelected,
                            sendMessageEditText.text.toString()
                        )
                        openWhatsapp = true
                        twoChannels = true
                    } // Validate
                    if (hasSms && hasEmail && !hasWhatsapp) {
                        multiChannelSendMessage(
                            viewAdapter.listOfNumberSelected,
                            sendMessageEditText.text.toString()
                        )
                        openMail = true
                        twoChannels = true
                    } // Validate
                    if (hasWhatsapp && hasEmail && !hasSms) {
                        multiChannelSendMessageWhatsapp(sendMessageEditText.text.toString())
                        openMail = true
                        twoChannels = true
                    } // Validate

                    if (hasSms && hasWhatsapp && hasEmail) {
                        multiChannelSendMessage(
                            viewAdapter.listOfNumberSelected,
                            sendMessageEditText.text.toString()
                        )

                        openWhatsapp = true
                        openMail = false
                        tripleChannels = true
                    }
                }

                hideKeyboard()

            } else {
                Toast.makeText(
                    this,
                    getString(R.string.multi_channel_empty_field),
                    Toast.LENGTH_SHORT
                ).show()
                hideKeyboard()
            }
        }

        //endregion
    }

    //region ======================================= Functions ==============================================

    private fun refreshActivity() {
        if (intent.getBooleanExtra("fromMainToMultiChannel", false)) {
            startActivity(
                Intent(this@MultiChannelActivity, MainActivity::class.java).addFlags(
                    Intent.FLAG_ACTIVITY_NO_ANIMATION
                )
            )
            hideKeyboard()
            finish()
        } else {
            startActivity(
                Intent(
                    this@MultiChannelActivity,
                    GroupManagerActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            )
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

    private fun multiChannelSendMessage(listOfPhoneNumber: ArrayList<String>, msg: String) {
        var message = "smsto:" + listOfPhoneNumber[0]
        for (i in 0 until listOfPhoneNumber.size) {
            message += ";" + listOfPhoneNumber[i]
        }

        sendValidate = twoChannels

        startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse(message)).putExtra("sms_body", msg))
    }

    private fun multiChannelSendMessageWhatsapp(msg: String) {
        val i = Intent(Intent.ACTION_VIEW)

        openWhatsapp = false
        openMail = tripleChannels
        sendValidate = twoChannels

        try {
            val url = "https://api.whatsapp.com/send?text=" + URLEncoder.encode(msg, "UTF-8")
            i.setPackage("com.whatsapp")
            i.data = Uri.parse(url)
            if (i.resolveActivity(packageManager) != null) {
                startActivity(i)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun multiChannelMailClick(listOfMail: ArrayList<String>, msg: String) {
        val intent = Intent(Intent.ACTION_SEND)
        val contact = listOfMail.toArray(arrayOfNulls<String>(listOfMail.size))
        intent.putExtra(Intent.EXTRA_EMAIL, contact)
        intent.data = Uri.parse("mailto:")
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, msg)

        openMail = false
        sendValidate = false

        if (twoChannels) {
            sendValidateFromMail = twoChannels
        } else if (tripleChannels) {
            sendValidateFromMail = tripleChannels
        }

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

        if (sendValidateFromMail) {
            sendValidate = true
        }

        if (openWhatsapp) {
            multiChannelSendMessageWhatsapp(sendMessageEditText.text.toString())
        }

        if (openMail) {
            viewAdapter.listOfMailSelected?.let {
                multiChannelMailClick(
                    it,
                    sendMessageEditText.text.toString()
                )
            }
        }
    }

    override fun onBackPressed() {
        refreshActivity()
    }

    //endregion
}