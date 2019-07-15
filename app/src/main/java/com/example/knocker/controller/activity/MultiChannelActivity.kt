package com.example.knocker.controller.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import com.example.knocker.R
import com.example.knocker.controller.ContactListViewAdapter
import com.example.knocker.controller.activity.firstLaunch.SelectContactAdapter
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
    private var multi_channel_SendMessageButton: AppCompatImageView? = null

    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multi_channel)

        intent_listOfContactSelected = intent.getIntegerArrayListExtra("ListContactsSelected")

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

        multi_channel_listViewAdapter = ContactListViewAdapter(this, multi_channel_listOfContactSelected)

        multi_channel_Listview!!.adapter = multi_channel_listViewAdapter

        multi_channel_SendMessageButton!!.setOnClickListener {

            if (multi_channel_SendMessageEditText!!.text.toString() != "") {
                if (multi_channel_listViewAdapter!!.listOfNumberSelected != null) {
                    multiChannelSendMessage(multi_channel_listViewAdapter!!.listOfNumberSelected, multi_channel_SendMessageEditText!!.text.toString())
                }

                if (multi_channel_listViewAdapter!!.listOfMailSelected != null) {
                    //                    multiChannelSendMessage(listOfMailSelected, messageToSend);
                }
            }


        }
    }

    //region ================================ Functions =======================================

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                startActivity(Intent(this@MultiChannelActivity, MainActivity::class.java))
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun multiChannelSendMessage(listOfPhoneNumber: ArrayList<String>, message: String) {

        var numbers = "" + listOfPhoneNumber[0]
        for (i in 1 until listOfPhoneNumber.size) {
            numbers += ";" + listOfPhoneNumber[i]
        }

        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse(numbers))

        intent.putExtra("sms_body:", message)

        startActivity(intent)
    }

    //endregion
}
