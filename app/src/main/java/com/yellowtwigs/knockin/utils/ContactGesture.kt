package com.yellowtwigs.knockin.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.utils.Converter.converter06To33

/**
 * L'objet qui permet d'ouvrir messenger, whatsapp et gmail
 * @author Florian Striebel, Kenzy Suon
 */
object ContactGesture {

    /*fun putContactIntent(contact: ContactWithAllInformation, context: Context, classToSend: Class<*>): Intent {
        *//*
        intent.putExtra("ContactFirstName", contact.firstName)
        intent.putExtra("ContactLastName", contact.lastName)
        intent.putExtra("ContactPhoneNumber",phoneNumber)
        intent.putExtra("ContactMail",mail)
        intent.putExtra("ContactImage", contact.profilePicture)
        intent.putExtra("ContactId", contact.id)
        intent.putExtra("ContactPriority", contact.contactPriority)
      *//*  return Intent(context, classToSend)
    }*/
    fun isWhatsappInstalled(context: Context): Boolean {
        val pm = context.packageManager
        return try {
            pm.getApplicationInfo("com.whatsapp", 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun openWhatsapp(contact: CharSequence, context: Context) {
        val url = "https://api.whatsapp.com/send?phone=$contact"
        try {
            val pm = context.packageManager
            pm.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES)
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            context.startActivity(i)
        } catch (e: PackageManager.NameNotFoundException) {
            Toast.makeText(context, "Whatsapp app not installed in your phone", Toast.LENGTH_SHORT)
                .show()
            e.printStackTrace()
        }
    }

    fun sendMessageWithWhatsapp(phoneNumber: String, msg: String, activity: Activity) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        val message = "phone=" + converter06To33(phoneNumber)
        intent.data = Uri.parse("http://api.whatsapp.com/send?phone=$message&text=$msg")

        activity.startActivity(intent)
    }

    fun whatsappIsNotInstalled(context: Context): Boolean {
        val pm = context.packageManager
        return try {
            pm.getApplicationInfo("com.whatsapp", 0)
            false
        } catch (e: Exception) {
            true
        }
    }

    fun callPhone(phoneNumber: String, context: Context) {
        var numberForPermission = ""

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CALL_PHONE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val PERMISSION_CALL_RESULT = 1
            ActivityCompat.requestPermissions(
                (context as Activity),
                arrayOf(Manifest.permission.CALL_PHONE),
                PERMISSION_CALL_RESULT
            )
            numberForPermission = phoneNumber
        } else {
            val sharedPreferences = context.getSharedPreferences("Phone_call", Context.MODE_PRIVATE)
            val popup = sharedPreferences.getBoolean("popup", true)
            if (popup && numberForPermission.isEmpty()) {
                AlertDialog.Builder(context)
                    .setTitle(R.string.main_contact_grid_title)
                    .setMessage(R.string.main_contact_grid_message)
                    .setPositiveButton(android.R.string.yes) { dialog: DialogInterface?, id: Int ->
                        context.startActivity(
                            Intent(Intent.ACTION_CALL, Uri.fromParts("tel", phoneNumber, null))
                        )
                    }
                    .setNegativeButton(android.R.string.no, null)
                    .show()
            } else {
                context.startActivity(
                    Intent(
                        Intent.ACTION_CALL,
                        Uri.fromParts("tel", phoneNumber, null)
                    )
                )
                numberForPermission = ""
            }
        }
    }

    private fun openSmsWithMessage(phoneNumber: String, message: String, context: Context) {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("sms", phoneNumber, null))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtra("sms_body", message)

        context.startActivity(intent)
    }


    fun openSmsNoMessage(phoneNumber: String, context: Context) {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("sms", phoneNumber, null))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        context.startActivity(intent)
        (context as Activity).finish()
    }

    /*fun openMessenger(id: String, context: Context) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.messenger.com/t/" + id))
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.messenger.com/t/" + id))
            context.startActivity(intent)
        }
    }*/

    /*fun openGmail(context: Context) {
        val i = context.packageManager.getLaunchIntentForPackage("com.google.android.gm")
        i!!.flags = FLAG_ACTIVITY_NEW_TASK
        context.startActivity(i)
    }*/

}