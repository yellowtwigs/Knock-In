package com.yellowtwigs.knockin.ui.contacts.multi_channel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.ActivityMultiChannelBinding
import com.yellowtwigs.knockin.ui.contacts.list.ContactsListActivity
import com.yellowtwigs.knockin.utils.ContactGesture
import com.yellowtwigs.knockin.utils.EveryActivityUtils.checkTheme
import dagger.hilt.android.AndroidEntryPoint
import java.net.URLEncoder

@AndroidEntryPoint
class MultiChannelActivity : AppCompatActivity() {

    private var twoChannels = false
    private var tripleChannels = false

    private var sendValidate = false
    private var sendValidateFromMail = false

    private var openMail = false
    private var openWhatsapp = false

    private var listOfPhoneNumbers = arrayListOf<Pair<Int, String>>()
    private var listOfMails = arrayListOf<Pair<Int, String>>()
    private var listOfHasWhatsapp = arrayListOf<Pair<Int, String>>()

    private val multiChannelViewModel: MultiChannelViewModel by viewModels()
    private lateinit var binding: ActivityMultiChannelBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkTheme(this)

        binding = ActivityMultiChannelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupListOfContacts(multiChannelViewModel)
        sendMessageClick()
    }

    private fun sendMessageClick() {
        binding.apply {
            sendMessage.setOnClickListener {
                val hasSms = listOfPhoneNumbers.size != 0
                val hasWhatsapp = listOfHasWhatsapp.size != 0
                val hasEmail = listOfMails.size != 0

                val newListOfPhoneNumbers = arrayListOf<String>()

                listOfPhoneNumbers.map { pair ->
                    newListOfPhoneNumbers.add(pair.second)
                }

                sendMessage(messageEditText, hasSms, hasWhatsapp, hasEmail, newListOfPhoneNumbers)
            }
        }
    }

    private fun sendMessage(
        messageEditText: AppCompatEditText,
        hasSms: Boolean,
        hasWhatsapp: Boolean,
        hasEmail: Boolean,
        newListOfPhoneNumbers: ArrayList<String>
    ) {
        if (messageEditText.text.toString() != "") {
            if (!hasSms && !hasWhatsapp && !hasEmail) {
                Toast.makeText(
                    this@MultiChannelActivity, getString(R.string.multi_channel_list_of_channel_selected_empty), Toast.LENGTH_LONG
                ).show()
                sendValidate = false
            } else {
                if (hasSms && !hasWhatsapp && !hasEmail) {
                    multiChannelSendMessage(
                        newListOfPhoneNumbers, messageEditText.text.toString()
                    )
                    sendValidate = true
                }
                if (hasWhatsapp && !hasSms && !hasEmail) {
                    multiChannelSendMessageWhatsapp(messageEditText.text.toString())
                    sendValidate = true
                }
                if (hasEmail && !hasSms && !hasWhatsapp) {
                    val mails = arrayListOf<String>()
                    listOfMails.forEach {
                        mails.add(it.second)
                    }
                    multiChannelMailClick(
                        mails, messageEditText.text.toString()
                    )
                    sendValidate = true
                } // Validate

                // Two Channels
                if (hasSms && hasWhatsapp && !hasEmail) {
                    multiChannelSendMessage(
                        newListOfPhoneNumbers, messageEditText.text.toString()
                    )
                    openWhatsapp = true
                    twoChannels = true
                } // Validate
                if (hasSms && hasEmail && !hasWhatsapp) {
                    multiChannelSendMessage(
                        newListOfPhoneNumbers, messageEditText.text.toString()
                    )
                    openMail = true
                    twoChannels = true
                } // Validate
                if (hasWhatsapp && hasEmail && !hasSms) {
                    multiChannelSendMessageWhatsapp(messageEditText.text.toString())
                    openMail = true
                    twoChannels = true
                } // Validate

                if (hasSms && hasWhatsapp && hasEmail) {
                    multiChannelSendMessage(
                        newListOfPhoneNumbers, messageEditText.text.toString()
                    )

                    openWhatsapp = true
                    openMail = false
                    tripleChannels = true
                }
            }

            hideKeyboard()

        } else {
            Toast.makeText(
                this@MultiChannelActivity, getString(R.string.multi_channel_empty_field), Toast.LENGTH_SHORT
            ).show()
            hideKeyboard()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        val actionbar = supportActionBar
        actionbar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.ic_left_arrow)
            it.title = "Multi Channel"
        }
    }

    private fun setupListOfContacts(
        multiChannelViewModel: MultiChannelViewModel
    ) {
        val listOfContactSelected = intent.getIntegerArrayListExtra("contacts")

        listOfContactSelected?.let {
            multiChannelViewModel.getContactsByIds(it).observe(this@MultiChannelActivity) { contacts ->
                val multiChannelListAdapter =
                    MultiChannelListAdapter(this@MultiChannelActivity, { id, image, firstPhoneNumber, secondPhoneNumber ->

                        if (firstPhoneNumber.flag == 2 && secondPhoneNumber.flag == 2) {
                            MaterialAlertDialogBuilder(
                                this@MultiChannelActivity, R.style.AlertDialog
                            ).setBackground(getDrawable(R.color.backgroundColor)).setTitle("")
                                .setMessage(getString(R.string.two_numbers_dialog_message))
                                .setPositiveButton("${ContactGesture.transformPhoneNumberWithSpinnerToFlag(firstPhoneNumber)} : ${firstPhoneNumber.phoneNumber}") { _, _ ->
                                    itemSelectedPhoneNumber(id, image, firstPhoneNumber.phoneNumber)
                                }.setNegativeButton("${ContactGesture.transformPhoneNumberWithSpinnerToFlag(secondPhoneNumber)} : ${secondPhoneNumber.phoneNumber}") { dialog, _ ->
                                    itemSelectedPhoneNumber(id, image, secondPhoneNumber.phoneNumber)
                                    dialog.cancel()
                                    dialog.dismiss()
                                }.show()
                        } else if (firstPhoneNumber.flag == 2 && secondPhoneNumber.flag == null) {
                            itemSelectedPhoneNumber(id, image, firstPhoneNumber.phoneNumber)
                        } else if (firstPhoneNumber.flag == null && secondPhoneNumber.flag == 2) {
                            itemSelectedPhoneNumber(id, image, secondPhoneNumber.phoneNumber)
                        } else if (firstPhoneNumber.flag != 2 && secondPhoneNumber.flag == null) {
                            MaterialAlertDialogBuilder(
                                this@MultiChannelActivity, R.style.AlertDialog
                            ).setBackground(getDrawable(R.color.backgroundColor)).setTitle(getString(R.string.not_mobile_flag_title))
                                .setMessage(getString(R.string.multi_channel_not_mobile_flag))
                                .setPositiveButton(getString(R.string.alert_dialog_yes)) { _, _ ->
                                    itemSelectedPhoneNumber(id, image, firstPhoneNumber.phoneNumber)
                                }.setNegativeButton(getString(R.string.alert_dialog_no)) { dialog, _ ->
                                    dialog.cancel()
                                    dialog.dismiss()
                                }.show()
                        } else if (firstPhoneNumber.flag == null && secondPhoneNumber.flag != 2) {
                            MaterialAlertDialogBuilder(
                                this@MultiChannelActivity, R.style.AlertDialog
                            ).setBackground(getDrawable(R.color.backgroundColor)).setTitle(getString(R.string.not_mobile_flag_title))
                                .setMessage(getString(R.string.multi_channel_not_mobile_flag))
                                .setPositiveButton(getString(R.string.alert_dialog_yes)) { _, _ ->
                                    itemSelectedPhoneNumber(id, image, secondPhoneNumber.phoneNumber)
                                }.setNegativeButton(getString(R.string.alert_dialog_no)) { dialog, _ ->
                                    dialog.cancel()
                                    dialog.dismiss()
                                }.show()
                        } else if (firstPhoneNumber.flag != 2 && secondPhoneNumber.flag != 2) {
                            MaterialAlertDialogBuilder(
                                this@MultiChannelActivity, R.style.AlertDialog
                            ).setBackground(getDrawable(R.color.backgroundColor)).setTitle("")
                                .setMessage(getString(R.string.two_numbers_dialog_message))
                                .setPositiveButton("${ContactGesture.transformPhoneNumberWithSpinnerToFlag(firstPhoneNumber)} : ${firstPhoneNumber.phoneNumber}") { _, _ ->
                                    itemSelectedPhoneNumber(id, image, firstPhoneNumber.phoneNumber)
                                }.setNegativeButton("${ContactGesture.transformPhoneNumberWithSpinnerToFlag(secondPhoneNumber)} : ${secondPhoneNumber.phoneNumber}") { dialog, _ ->
                                    itemSelectedPhoneNumber(id, image, secondPhoneNumber.phoneNumber)
                                    dialog.cancel()
                                    dialog.dismiss()
                                }.show()
                        } else if (firstPhoneNumber.flag == 2 && secondPhoneNumber.flag != 2) {
                            MaterialAlertDialogBuilder(
                                this@MultiChannelActivity, R.style.AlertDialog
                            ).setBackground(getDrawable(R.color.backgroundColor)).setTitle("")
                                .setMessage(getString(R.string.two_numbers_dialog_message))
                                .setPositiveButton("${ContactGesture.transformPhoneNumberWithSpinnerToFlag(firstPhoneNumber)} : ${firstPhoneNumber.phoneNumber}") { _, _ ->
                                    itemSelectedPhoneNumber(id, image, firstPhoneNumber.phoneNumber)
                                }.setNegativeButton("${ContactGesture.transformPhoneNumberWithSpinnerToFlag(secondPhoneNumber)} : ${secondPhoneNumber.phoneNumber}") { dialog, _ ->
                                    itemSelectedPhoneNumber(id, image, secondPhoneNumber.phoneNumber)
                                    dialog.cancel()
                                    dialog.dismiss()
                                }.show()
                        } else if (firstPhoneNumber.flag != 2 && secondPhoneNumber.flag == 2) {
                            MaterialAlertDialogBuilder(
                                this@MultiChannelActivity, R.style.AlertDialog
                            ).setBackground(getDrawable(R.color.backgroundColor)).setTitle("")
                                .setMessage(getString(R.string.two_numbers_dialog_message))
                                .setPositiveButton("${ContactGesture.transformPhoneNumberWithSpinnerToFlag(firstPhoneNumber)} : ${firstPhoneNumber.phoneNumber}") { _, _ ->
                                    itemSelectedPhoneNumber(id, image, firstPhoneNumber.phoneNumber)
                                }.setNegativeButton("${ContactGesture.transformPhoneNumberWithSpinnerToFlag(secondPhoneNumber)} : ${secondPhoneNumber.phoneNumber}") { dialog, _ ->
                                    itemSelectedPhoneNumber(id, image, secondPhoneNumber.phoneNumber)
                                    dialog.cancel()
                                    dialog.dismiss()
                                }.show()
                        } else {

                        }

                    }, { id, image, mail ->

                        itemSelectedMail(id, image, mail)

                    }, { id, image, firstPhoneNumber, secondPhoneNumber ->
                        itemSelectedWhatsapp(id, image, firstPhoneNumber)
                    })

                binding.recyclerView.apply {
                    multiChannelListAdapter.submitList(null)
                    multiChannelListAdapter.submitList(contacts)
                    adapter = multiChannelListAdapter
                    layoutManager = LinearLayoutManager(context)
                }
            }
        }
    }

    private fun itemSelectedPhoneNumber(id: Int, image: AppCompatImageView, phoneNumber: String) {
        if (listOfPhoneNumbers.contains(Pair(id, phoneNumber))) {
            listOfPhoneNumbers.remove(Pair(id, phoneNumber))

            image.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources, R.drawable.ic_micon, null
                )
            )
        } else {
            listOfPhoneNumbers.add(Pair(id, phoneNumber))
            image.setImageResource(R.drawable.ic_item_selected)
        }
    }

    private fun itemSelectedMail(id: Int, image: AppCompatImageView, email: String) {
        if (listOfMails.contains(Pair(id, email))) {
            listOfMails.remove(Pair(id, email))

            image.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources, R.drawable.ic_circular_gmail, null
                )
            )
        } else {
            listOfMails.add(Pair(id, email))
            image.setImageResource(R.drawable.ic_item_selected)
        }
    }

    private fun itemSelectedWhatsapp(id: Int, image: AppCompatImageView, phoneNumber: String) {
        if (listOfHasWhatsapp.contains(Pair(id, phoneNumber))) {
            listOfHasWhatsapp.remove(Pair(id, phoneNumber))

            image.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources, R.drawable.ic_circular_whatsapp, null
                )
            )
        } else {
            listOfHasWhatsapp.add(Pair(id, phoneNumber))
            image.setImageResource(R.drawable.ic_item_selected)
        }
    }

    //region ======================================= Functions ==============================================

    private fun refreshActivity() {
        if (intent.getBooleanExtra("fromMainToMultiChannel", false)) {
            startActivity(
                Intent(this@MultiChannelActivity, ContactsListActivity::class.java).addFlags(
                    Intent.FLAG_ACTIVITY_NO_ANIMATION
                )
            )
            hideKeyboard()
            finish()
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
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
        this.currentFocus?.let { v ->
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
            multiChannelSendMessageWhatsapp(binding.messageEditText.text.toString())
        }

        if (openMail) {
            val mails = arrayListOf<String>()
            listOfMails.forEach {
                mails.add(it.second)
            }
            multiChannelMailClick(
                mails, binding.messageEditText.text.toString()
            )
        }
    }

    override fun onBackPressed() {
        if (intent.getBooleanExtra("fromMainToMultiChannel", false)) {
            refreshActivity()
        } else {
            super.onBackPressed()
        }
    }

    //endregion
}