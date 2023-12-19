package com.yellowtwigs.knockin.utils

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ContentProviderOperation
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import android.util.Patterns
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.yellowtwigs.knockin.model.database.data.ContactDB
import com.yellowtwigs.knockin.ui.add_edit_contact.edit.PhoneNumberWithFlag
import com.yellowtwigs.knockin.ui.add_edit_contact.edit.PhoneNumberWithSpinner
import com.yellowtwigs.knockin.ui.contacts.contact_selected.ContactSelectedWithAppsActivity
import com.yellowtwigs.knockin.ui.contacts.list.ContactsListActivity
import com.yellowtwigs.knockin.ui.contacts.list.ContactsListActivity.Companion.PHONE_CALL_REQUEST_CODE
import com.yellowtwigs.knockin.ui.contacts.list.ContactsListViewState
import com.yellowtwigs.knockin.utils.Converter.converter06To33
import java.sql.DriverManager
import java.util.*

object ContactGesture {

    fun updateContact(context: Context, rawContactId: Int?, firstName: String, lastName: String, lastPhoneNumber: String, phoneNumber: String?, email:
    String?) {
        Log.i("UpdateContact", "rawContactId : $rawContactId")
        Log.i("UpdateContact", "firstName : $firstName")
        Log.i("UpdateContact", "lastName : $lastName")
        Log.i("UpdateContact", "phoneNumber : $phoneNumber")
        Log.i("UpdateContact", "email : $email")

        phoneNumber?.let { phoneNumberNotNull ->
            email?.let { mailNotNull ->
                updateNameAndNumber(
                        context,
                        lastPhoneNumber,
                        firstName,
                        lastName,
                        phoneNumberNotNull,
                        mailNotNull,
                )
            }
        }
    }

    private val DATA_COLS = arrayOf(
            ContactsContract.Data.MIMETYPE,
            ContactsContract.Data.DATA1,
            ContactsContract.Data.CONTACT_ID
    )

    private fun updateNameAndNumber(context: Context, number: String, newFirstname: String, newLastname: String, newNumber: String, newEmail: String): Boolean {

        getContactId(context, number)?.let { contactId ->
            val nameWhere = "${DATA_COLS[0]} = '${ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE}' AND ${DATA_COLS[2]} = ?"
            val nameArgs = arrayOf(contactId)

            val firstnameOperation = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(nameWhere, nameArgs)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, newFirstname)
                    .build()

            val lastnameOperation = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(nameWhere, nameArgs)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, newLastname)
                    .build()

            val numberWhere = "${DATA_COLS[0]} = '${ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE}' AND ${DATA_COLS[1]} = ?"
            val numberArgs = arrayOf(number)

            val numberOperation = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(numberWhere, numberArgs)
                    .withValue(DATA_COLS[1], newNumber)
                    .build()

            val emailWhere = "${DATA_COLS[0]} = '${ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE}' AND ${DATA_COLS[2]} = ?"
            val emailArgs = arrayOf(contactId)

            val emailOperation = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(emailWhere, emailArgs)
                    .withValue(ContactsContract.CommonDataKinds.Email.ADDRESS, newEmail)
                    .build()

            val operations = arrayListOf(firstnameOperation, lastnameOperation, numberOperation, emailOperation)

            try {
                val results = context.contentResolver.applyBatch(ContactsContract.AUTHORITY, operations)

                for (result in results) {
                    Log.d("Update Result", result.toString())
                }

                return true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return false
    }

    fun getContactId(context: Context, number: String): String? {
        val cursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(ContactsContract.CommonDataKinds.Phone.CONTACT_ID, ContactsContract.CommonDataKinds.Phone.NUMBER),
                "${ContactsContract.CommonDataKinds.Phone.NUMBER}=?",
                arrayOf(number),
                null
        )

        if (cursor == null || cursor.count == 0) return null

        cursor.moveToFirst()

        val id = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID))

        cursor.close()
        return id
    }

    fun transformContactDbToContactsListViewState(contact: ContactDB): ContactsListViewState {
        val fullName = if (contact.firstName.isEmpty() || contact.firstName.isBlank() || contact.firstName == " ") {
            contact.lastName.uppercase()
        } else if (contact.lastName.isEmpty() || contact.lastName.isBlank() || contact.lastName == " ") {
            contact.firstName.uppercase()
        } else {
            "${contact.firstName.uppercase()} ${contact.firstName.uppercase()}"
        }

        return ContactsListViewState(id = contact.id, fullName = fullName, firstName = contact.firstName, lastName = contact.lastName, profilePicture = contact.profilePicture, profilePicture64 = contact.profilePicture64, firstPhoneNumber = transformPhoneNumberToSinglePhoneNumberWithSpinner(contact.listOfPhoneNumbers, true), secondPhoneNumber = transformPhoneNumberToSinglePhoneNumberWithSpinner(contact.listOfPhoneNumbers, false), listOfMails = contact.listOfMails, priority = contact.priority, isFavorite = contact.isFavorite == 1, messengerId = contact.messengerId, hasWhatsapp = contact.listOfMessagingApps.contains("com.whatsapp"), hasTelegram = contact.listOfMessagingApps.contains("org.telegram.messenger"), hasSignal = contact.listOfMessagingApps.contains("org.thoughtcrime.securesms"))
    }

    fun handleContactWithMultiplePhoneNumbers(cxt: Context, phoneNumbers: List<PhoneNumberWithSpinner>, action: String, onClickedMultipleNumbers: (String, PhoneNumberWithSpinner, PhoneNumberWithSpinner) -> Unit, onNotMobileFlagClicked: (String, PhoneNumberWithSpinner, String) -> Unit, message: String) {

        if (phoneNumbers.size > 1) {
            onClickedMultipleNumbers(action, phoneNumbers[0], phoneNumbers[1])
        } else if (phoneNumbers.isNotEmpty()) {
            val type = phoneNumbers[0].flag
            val phoneNumber = phoneNumbers[0].phoneNumber

            if (cxt is ContactsListActivity) {
                when (action) {
                    "call" -> {
                        cxt.callPhone(phoneNumber)
                    }

                    "sms" -> {
                        if (type == 2) {
                            openSms(phoneNumber, cxt)
                        } else {
                            onNotMobileFlagClicked(action, phoneNumbers[0], "")
                        }
                    }

                    "send_whatsapp" -> {
                        if (type == 2) {
                            sendMessageWithWhatsapp(phoneNumber, message, cxt)
                        } else {
                            onNotMobileFlagClicked(action, phoneNumbers[0], message)
                        }
                    }

                    "send_message" -> {
                        if (type == 2) {
                            sendMessageWithAndroidMessage(phoneNumber, message, cxt)
                        } else {
                            onNotMobileFlagClicked(action, phoneNumbers[0], message)
                        }
                    }

                    "whatsapp" -> {
                        if (type == 2) {
                            openWhatsapp(converter06To33(phoneNumber), cxt)
                        } else {
                            onNotMobileFlagClicked(action, phoneNumbers[0], "")
                        }
                    }

                    "telegram" -> {
                        if (type == 2) {
                            goToTelegram(cxt, phoneNumber)
                        } else {
                            onNotMobileFlagClicked(action, phoneNumbers[0], "")
                        }
                    }
                }
            } else if (cxt is ContactSelectedWithAppsActivity) {
                when (action) {
                    "call" -> {
                        callPhone(phoneNumber, cxt)
                    }

                    "sms" -> {
                        if (type == 2) {
                            openSms(phoneNumber, cxt)
                        } else {
                            onNotMobileFlagClicked(action, phoneNumbers[0], "")
                        }
                    }

                    "send_whatsapp" -> {
                        if (type == 2) {
                            sendMessageWithWhatsapp(phoneNumber, message, cxt)
                        } else {
                            onNotMobileFlagClicked(action, phoneNumbers[0], message)
                        }
                    }

                    "send_message" -> {
                        if (type == 2) {
                            sendMessageWithAndroidMessage(phoneNumber, message, cxt)
                        } else {
                            onNotMobileFlagClicked(action, phoneNumbers[0], message)
                        }
                    }

                    "whatsapp" -> {
                        if (type == 2) {
                            openWhatsapp(converter06To33(phoneNumber), cxt)
                        } else {
                            onNotMobileFlagClicked(action, phoneNumbers[0], "")
                        }
                    }

                    "telegram" -> {
                        if (type == 2) {
                            goToTelegram(cxt, phoneNumber)
                        } else {
                            onNotMobileFlagClicked(action, phoneNumbers[0], "")
                        }
                    }
                }
            }
        }
    }

    fun transformPhoneNumberWithFlagToPhoneNumberWithSpinner(phoneNumbers: List<PhoneNumberWithFlag>): List<PhoneNumberWithSpinner> {
        return phoneNumbers.map {
            PhoneNumberWithSpinner(flag = transformPhoneNumberFlagStringToFlagNumber(it.flag), phoneNumber = it.phoneNumber)
        }
    }

    fun transformPhoneNumberToSinglePhoneNumberWithFlag(phoneNumbers: List<String>, firstPhoneNumber: Boolean): PhoneNumberWithFlag {
        return if (firstPhoneNumber && phoneNumbers.isNotEmpty()) {
            if (phoneNumbers[0].contains(":")) {
                PhoneNumberWithFlag(flag = transformPhoneNumberFlagNumberToFlagString(phoneNumbers[0].split(":")[0].toInt()), phoneNumber = phoneNumbers[0].split(":")[1])
            } else {
                PhoneNumberWithFlag(flag = "Mobile", phoneNumber = phoneNumbers[0])
            }
        } else if (phoneNumbers.size > 1) {
            if (phoneNumbers[1].contains(":")) {
                PhoneNumberWithFlag(flag = transformPhoneNumberFlagNumberToFlagString(phoneNumbers[1].split(":")[0].toInt()), phoneNumber = phoneNumbers[1].split(":")[1])
            } else {
                PhoneNumberWithFlag(flag = "Mobile", phoneNumber = phoneNumbers[1])
            }
        } else {
            PhoneNumberWithFlag(flag = "Mobile", phoneNumber = "")
        }
    }

    fun transformPhoneNumberToSinglePhoneNumberWithSpinner(phoneNumbers: List<String>, firstPhoneNumber: Boolean): PhoneNumberWithSpinner {
        return if (firstPhoneNumber && phoneNumbers.isNotEmpty()) {
            if (phoneNumbers[0].contains(":")) {
                if (phoneNumbers[0].split(":")[0] != "") {
                    PhoneNumberWithSpinner(flag = phoneNumbers[0].split(":")[0].toInt(), // 1 ==> 0
                            phoneNumber = phoneNumbers[0].split(":")[1])
                } else {
                    PhoneNumberWithSpinner(flag = 2, // 1 ==> 0
                            phoneNumber = phoneNumbers[0].split(":")[1])
                }
            } else {
                PhoneNumberWithSpinner(flag = 2, phoneNumber = phoneNumbers[0])
            }
        } else if (phoneNumbers.size > 1) {
            if (phoneNumbers[1].contains(":")) {
                if (phoneNumbers[1].split(":")[0] != "") {
                    PhoneNumberWithSpinner(flag = phoneNumbers[1].split(":")[0].toInt(), // 1 ==> 0
                            phoneNumber = phoneNumbers[1].split(":")[1])
                } else {
                    PhoneNumberWithSpinner(flag = 2, // 1 ==> 0
                            phoneNumber = phoneNumbers[1].split(":")[1])
                }
            } else {
                PhoneNumberWithSpinner(flag = 2, phoneNumber = phoneNumbers[1])
            }
        } else {
            PhoneNumberWithSpinner(flag = 2, phoneNumber = "")
        }
    }

    fun transformPhoneNumberToPhoneNumbersWithSpinner(phoneNumbers: List<String>): List<PhoneNumberWithSpinner> {
        return phoneNumbers.map { phoneNumber ->
            if (phoneNumber.contains(":")) {
                PhoneNumberWithSpinner(flag = phoneNumber.split(":")[0].toInt(), // 1 ==> 0
                        phoneNumber = phoneNumber.split(":")[1])
            } else {
                PhoneNumberWithSpinner(flag = 2, phoneNumber = phoneNumber)
            }
        }
    }

    fun transformPhoneNumberWithSpinnerToFlag(phoneNumberWithSpinner: PhoneNumberWithSpinner): String {
        return when (phoneNumberWithSpinner.flag) {
            1 -> "Home"
            2 -> "Mobile"
            3 -> "Work"
            4 -> "Work Fax"
            5 -> "Home Fax"
            6 -> "Pager"
            7 -> "Other"
            else -> "Other"
        }
    }

    fun transformPhoneNumberFlagNumberToFlagString(flag: Int): String {
        return when (flag) {
            1 -> "Home"
            2 -> "Mobile"
            3 -> "Work"
            4 -> "Work Fax"
            5 -> "Home Fax"
            6 -> "Pager"
            7 -> "Other"
            else -> "Other"
        }
    }

    fun transformPhoneNumberFlagStringToFlagNumber(flag: String): Int {
        return when (flag) {
            "Home" -> 1
            "Mobile" -> 2
            "Work" -> 3
            "Work Fax" -> 4
            "Home Fax" -> 5
            "Pager" -> 6
            "Other" -> 7
            else -> 7
        }
    }

    //region =========================================== WHATSAPP ===========================================

    fun isWhatsappInstalled(context: Context): Boolean {
        val pm = context.packageManager
        return try {
            pm.getApplicationInfo("com.whatsapp", 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun openWhatsapp(contact: String, context: Context) {
        Log.i("GoToWithContact", "contact : $contact")

        val url = "https://api.whatsapp.com/send?phone=${converter06To33(contact)}"

        val i = Intent(Intent.ACTION_VIEW)
        i.flags = FLAG_ACTIVITY_NEW_TASK
        i.data = Uri.parse(url)
        context.startActivity(i)
    }

    fun openWhatsapp(context: Context) {
        val i = context.packageManager.getLaunchIntentForPackage("com.whatsapp")
        i?.flags = FLAG_ACTIVITY_NEW_TASK
        try {
            context.startActivity(i)
        } catch (e: ActivityNotFoundException) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://whatsapp.com/")))
        }
    }

    fun sendMessageWithWhatsapp(phoneNumber: String, msg: String, context: Context) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.flags = FLAG_ACTIVITY_NEW_TASK
        val message = "phone=" + converter06To33(phoneNumber)
        intent.data = Uri.parse("http://api.whatsapp.com/send?phone=$message&text=$msg")

        context.startActivity(intent)
    }

    //endregion

    //region =========================================== TELEGRAM ===========================================

    fun isTelegramInstalled(context: Context): Boolean {
        val pm = context.packageManager
        return try {
            pm.getApplicationInfo("org.telegram.messenger", 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun goToTelegram(context: Context, phoneNumber: String) {
        val appIntent = if (phoneNumber == "") {
            Intent(Intent.ACTION_VIEW, Uri.parse("tg://resolve"))
        } else {
            Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/${
                converter06To33(phoneNumber).replace("\\s".toRegex(), "")
            }"))
        }
        try {
            appIntent.flags = FLAG_ACTIVITY_NEW_TASK
            appIntent.setPackage("org.telegram.messenger")
            context.startActivity(appIntent)
        } catch (e: ActivityNotFoundException) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://web.telegram.org/")))
        }
    }

    fun goToTelegram(context: Context) {
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("tg://resolve"))
        try {
            appIntent.flags = FLAG_ACTIVITY_NEW_TASK
            appIntent.putExtra("", "")
            appIntent.setPackage("org.telegram.messenger")
            context.startActivity(appIntent)
        } catch (e: ActivityNotFoundException) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://web.telegram.org/")))
        }
    }

    //endregion

    //region ============================================ SIGNAL ============================================

    fun isSignalInstalled(context: Context): Boolean {
        val pm = context.packageManager
        return try {
            pm.getApplicationInfo("org.thoughtcrime.securesms", 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun goToSignal(context: Context) {
        val appIntent = context.packageManager.getLaunchIntentForPackage("org.thoughtcrime.securesms")
        try {
            context.startActivity(appIntent)
        } catch (e: ActivityNotFoundException) {
            Log.i("resolveInfoList", "$e")
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://signal.org/")))
        }
    }

    //endregion

    //region ============================================ PHONE =============================================

    fun openSms(phoneNumber: String, context: Context) {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("sms", phoneNumber, null))
        intent.flags = FLAG_ACTIVITY_NEW_TASK

        context.startActivity(intent)
    }

    fun sendMessageWithAndroidMessage(phoneNumber: String, msg: String, context: Context) {
        val message = "smsto:$phoneNumber"
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse(message))
        intent.flags = FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent.putExtra("sms_body", msg))
    }

    fun callPhone(phoneNumber: String, context: Context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((context as Activity), arrayOf(Manifest.permission.CALL_PHONE), PHONE_CALL_REQUEST_CODE)
        } else {
            context.startActivity(Intent(Intent.ACTION_CALL, Uri.fromParts("tel", phoneNumber, null)))
        }
    }

    fun isPhoneNumber(phoneNumber: String): Boolean {
        val regex = "((?:\\+|00)[17][ \\-]?|(?:\\+|00)[1-9]\\d{0,2}[ \\-]?|(?:\\+|00)1-\\d{3}[ \\-]?)?(0\\d|\\([0-9]{3}\\)|[1-9]{0,3})(?:([ \\-][0-9]{2}){4}|((?:[0-9]{2}){4})|([ \\-][0-9]{3}[ \\-][0-9]{4})|([0-9]{7}))"
        return phoneNumber.matches(regex.toRegex())
    }

    fun isValidEmail(mail: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(mail).matches()
    }

    //endregion

    //region ========================================== MESSENGER ===========================================

    fun isMessengerInstalled(context: Context): Boolean {
        val pm = context.packageManager
        return try {
            pm.getApplicationInfo("com.facebook.orca", 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun openMessenger(id: String, context: Context) {
        try {
            val intent = if (id == "") {
                Intent(Intent.ACTION_VIEW, Uri.parse("https://www.messenger.com/t/"))
            } else {
                Intent(Intent.ACTION_VIEW, Uri.parse("https://www.messenger.com/t/$id"))
            }
            intent.flags = FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.messenger.com/t/$id"))
            context.startActivity(intent)
        }
    }


    //endregion

    //region ============================================= MAIL =============================================

    fun goToOutlook(context: Context) {
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("ms-outlook://emails"))
        try {
            context.startActivity(appIntent)
        } catch (e: ActivityNotFoundException) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://outlook.com/")))
        }
    }

    fun openMailApp(mail: String, context: Context) {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:")
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(mail))
        intent.putExtra(Intent.EXTRA_SUBJECT, "")
        intent.putExtra(Intent.EXTRA_TEXT, "")
        DriverManager.println("intent " + Objects.requireNonNull(intent.extras).toString())
        context.startActivity(intent)
    }

    fun goToGmail(context: Context) {
        val appIntent = Intent(Intent.ACTION_VIEW)
        appIntent.setClassName("com.google.android.gm", "com.google.android.gm.ConversationListActivityGmail")
        try {
            context.startActivity(appIntent)
        } catch (e: ActivityNotFoundException) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://gmail.com/")))
        }
    }

    fun sendMail(mail: String, subject: String, msg: String, context: Context) {
        val intent = Intent(Intent.ACTION_SEND)
        val mailSubject = "RE: $subject"
        intent.putExtra(Intent.EXTRA_EMAIL, mail)
        intent.data = Uri.parse("mailto:")
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, msg)
        intent.putExtra(Intent.EXTRA_SUBJECT, mailSubject)
        intent.flags = FLAG_ACTIVITY_NEW_TASK

        context.startActivity(intent)
    }

    //endregion

    //region ============================================ OTHERS ============================================

//    private fun goToSkype(context: Context) {
//        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("skype://skype"))
//        try {
//            startActivity(appIntent)
//        } catch (e: ActivityNotFoundException) {
//            startActivity(
//                Intent(
//                    Intent.ACTION_VIEW,
//                    Uri.parse("https://skype.com/")
//                )
//            )
//        }
//    }


    //endregion
}