package com.example.firsttestknocker

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.PackageManager
import android.net.Uri
import android.support.v4.content.ContextCompat.startActivity
import android.widget.Toast

object ContactGesture {

    fun putContactIntent (contact :Contacts,context: Context, classToSend : Class<*>):Intent{
        val intent = Intent( context,classToSend)
        intent.putExtra("ContactFirstName", contact.firstName)
        intent.putExtra("ContactLastName", contact.lastName)
        intent.putExtra("ContactPhoneNumber", contact.phoneNumber)
        intent.putExtra("ContactMail", contact.mail)
        intent.putExtra("ContactImage", contact.profilePicture)
        intent.putExtra("ContactId", contact.id)
        intent.putExtra("ContactPriority", contact.contactPriority)
        return intent
    }
    fun openWhatsapp(contact: CharSequence,context: Context) {
        val url = "https://api.whatsapp.com/send?phone=$contact"
        try {
            val pm = context.getPackageManager()
            pm.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES)
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            context.startActivity(i)
        } catch (e: PackageManager.NameNotFoundException) {
            Toast.makeText(context, "Whatsapp app not installed in your phone", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }

    }
    fun openMessenger(id:String, context: Context) {
        try {
            val intent =Intent(Intent.ACTION_VIEW, Uri.parse("https://www.messenger.com/t/" + id));
            context.startActivity(intent);
        } catch (e: ActivityNotFoundException) {
            val intent =Intent(Intent.ACTION_VIEW, Uri.parse("https://www.messenger.com/t/" + id));
            context.startActivity(intent);
        }
    }
    fun openGmail(context: Context) {
        val i = context.packageManager.getLaunchIntentForPackage("com.google.android.gm")
        i!!.flags = FLAG_ACTIVITY_NEW_TASK
        context.startActivity(i)
    }

}