package com.example.knocker.model;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import java.util.Objects;

public class MyReceiver extends BroadcastReceiver {

    private static final String SMS_RECEIVED = MyReceiver.class.getSimpleName();
    private static final String pdu_type = "pdus";
    private static final String TAG = "SmsBroadcastReceiver";
    private String msg, phoneNumber = "";

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onReceive(Context context, Intent intent) {

        // Get the SMS message.
        Bundle bundle = intent.getExtras();
        SmsMessage[] msgs;
        String strMessage = "";
        assert bundle != null;
        String format = bundle.getString("format");
        // Retrieve the SMS message received.
        Object[] pdus = (Object[]) bundle.get(pdu_type);
        if (pdus != null) {
            // Check the Android version.
            boolean isVersionM =
                    (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M);
            // Fill the msgs array.
            msgs = new SmsMessage[pdus.length];
            for (int i = 0; i < msgs.length; i++) {
                // Check Android version and use appropriate createFromPdu.
                if (isVersionM) {
                    // If Android version M or newer:
                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
                } else {
                    // If Android version L or older:
                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                }
                // Build the message to show.
                strMessage += "SMS from " + msgs[i].getOriginatingAddress();
                strMessage += " :" + msgs[i].getMessageBody() + "\n";
                // Log and display the SMS message.
                Log.d(TAG, "onReceive: " + strMessage);
                Toast.makeText(context, strMessage, Toast.LENGTH_LONG).show();

//
//        Log.i(TAG, "Intent Received : " + intent.getAction());
//
//        if (Objects.equals(intent.getAction(), SMS_RECEIVED)) {
//            Bundle dataBundle = intent.getExtras();
//
//            if (dataBundle != null) {
//                Object[] mypdu = (Object[]) dataBundle.get("mypdu");
//                assert mypdu != null;
//                final SmsMessage[] message = new SmsMessage[mypdu.length];
//
//                for (int i = 0; i < mypdu.length; i++) {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                        String format = dataBundle.getString("format");
//                        message[i] = SmsMessage.createFromPdu((byte[]) mypdu[i], format);
//                    } else {
//                        message[i] = SmsMessage.createFromPdu((byte[]) mypdu[i]);
//                    }
//                    msg = message[i].getMessageBody();
//                    phoneNumber = message[i].getOriginatingAddress();
//                }
//                Toast.makeText(context, "Message: " + msg + "\n Number: " + phoneNumber, Toast.LENGTH_LONG).show();
            }
        }
    }
}
