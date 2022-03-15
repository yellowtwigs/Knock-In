package com.yellowtwigs.knockin.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.Telephony
import android.util.Base64
import android.util.Log
import java.io.ByteArrayOutputStream

object Converter {
    fun convertPackageToString(packageName: String, context: Context): String {
//        Log.i("displayLayout", Telephony.Sms.getDefaultSmsPackage(context))
//        Log.i("displayLayout", packageName)
        if (packageName == "com.facebook.katana") {
            return "Facebook"
        } else if (packageName == "com.facebook.orca") {
            return "Messenger"
        } else if (packageName == "com.whatsapp") {
            return "WhatsApp"
        } else if (packageName == "com.google.android.gm") {
            return "Gmail"
        } else if (packageName == "com.google.android.apps.messaging" || packageName == "com.samsung.android.messaging" ||
            packageName == Telephony.Sms.getDefaultSmsPackage(context)
        ) {
            return "Message"
        }
        return ""
    }

    fun base64ToBitmap(base64: String): Bitmap {
        val decodedString = Base64.decode(base64, Base64.DEFAULT)
        val options = BitmapFactory.Options()
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size, options)
    }

    fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val imageBytes = byteArrayOutputStream.toByteArray()

        return Base64.encodeToString(imageBytes, Base64.DEFAULT)
    }

    fun converter06To33(phoneNumber: String): String {
        return if (phoneNumber[0] == '0') {
            "+33$phoneNumber"
        } else phoneNumber
    }
}