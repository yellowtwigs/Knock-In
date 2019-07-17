package com.example.knocker.model

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SmsMessage
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ListView
import android.widget.Toast

import com.example.knocker.R
import com.example.knocker.controller.MessageListAdapter

import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar

@Suppress("DEPRECATION")
class MyReceiver : BroadcastReceiver() {

    private val compose_message_listOfMessage = ArrayList<Message>()

    private var composeMessageView: View? = null

    @SuppressLint("InflateParams", "RtlHardcoded")
    @TargetApi(Build.VERSION_CODES.M)
    override fun onReceive(context: Context, intent: Intent) {

        // Get the SMS message.
        val bundle = intent.extras
        val msgs: Array<SmsMessage?>

        assert(bundle != null)
        val format = bundle!!.getString("format")

        // Retrieve the SMS message received.
        val pdus = bundle.get(pdu_type) as Array<*>

        // Check the Android version.
        val isVersionM = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

        // Fill the msgs array.
        msgs = arrayOfNulls(pdus.size)

        for (i in msgs.indices) {
            // Check Android version and use appropriate createFromPdu.
            if (isVersionM) {
                // If Android version M or newer:
                msgs[i] = SmsMessage.createFromPdu(pdus[i] as ByteArray, format)
            } else {
                // If Android version L or older:
                msgs[i] = SmsMessage.createFromPdu(pdus[i] as ByteArray)
            }

            // Build the message to show.
            val phoneNumber = msgs[i]!!.originatingAddress
            val msg = msgs[i]!!.messageBody + "\n"

            // Log and display the SMS message.
            Log.d(TAG, "onReceive: $msg")

            Toast.makeText(context, msg + " from : " + phoneNumber, Toast.LENGTH_LONG).show();

//            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
//            composeMessageView = inflater.inflate(R.layout.activity_compose_message, null)
//
//            val c = Calendar.getInstance().time
//
//            val date = SimpleDateFormat("dd-MMM-yyyy")
//            val hour = SimpleDateFormat("hh:mm:ss")
//            val currentDate = date.format(c)
//            val currentHour = hour.format(c)
//
//            val messageClass = Message(msg, false, phoneNumber, 0, currentDate, currentHour)
//
//            compose_message_listOfMessage.add(messageClass)
//
//            callComposeMessageLayout(context, composeMessageView)
        }
    }

    private fun callComposeMessageLayout(context: Context, view: View?) {
        val compose_message_ListViewMessage = view!!.findViewById<ListView>(R.id.compose_message_list_view_message)
        compose_message_ListViewMessage.adapter = MessageListAdapter(context, compose_message_listOfMessage)
    }

    companion object {

        private val SMS_RECEIVED = MyReceiver::class.java.simpleName
        private val pdu_type = "pdus"
        private val TAG = "SmsBroadcastReceiver"
    }
}