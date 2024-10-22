package com.yellowtwigs.knockin.utils

import android.content.Context
import android.provider.Telephony

object NotificationsGesture {
    private const val FACEBOOK_PACKAGE = "com.facebook.katana"
    private const val MESSENGER_PACKAGE = "com.facebook.orca"
    private const val WHATSAPP_PACKAGE = "com.whatsapp"

    private const val GMAIL_PACKAGE = "com.google.android.gm"
    private const val OUTLOOK_PACKAGE = "com.microsoft.office.outlook"

    private const val MESSAGE_PACKAGE = "com.google.android.apps.messaging"
    private const val XIAOMI_MESSAGE_PACKAGE = "com.android.mms"
    private const val MESSAGE_SAMSUNG_PACKAGE = "com.samsung.android.messaging"

    private const val SIGNAL_PACKAGE = "org.thoughtcrime.securesms"
    private const val TELEGRAM_PACKAGE = "org.telegram.messenger"
    private const val INSTAGRAM_PACKAGE = "com.instagram.android"
    private const val DISCORD_PACKAGE = "com.discord"
    private const val TIKTOK_PACKAGE = "com.zhiliaoapp.musically"
    private const val SNAPCHAT_PACKAGE = "com.snapchat.android"
    private const val REDDIT_PACKAGE = "com.reddit.frontpage"
    private const val MESSAGES_PACKAGE = "com.chating.messages.chat.fun"
    private const val VIBER_PACKAGE = "com.viber.voip"

    private const val YOUTUBE_PACKAGE = "com.google.android.youtube"
    private const val GOOGLE_PACKAGE = "com.google.android.googlequicksearchbox"
    private const val SCREEN_RECORDER = "com.huawei.screenrecorder"

    fun convertPackageToString(packageName: String, context: Context): String {
        when (packageName) {
            FACEBOOK_PACKAGE -> return "Facebook"
            MESSENGER_PACKAGE -> return "Messenger"
            WHATSAPP_PACKAGE -> return "WhatsApp"
            GMAIL_PACKAGE -> return "Gmail"
            OUTLOOK_PACKAGE -> return "Outlook"

            MESSAGE_PACKAGE -> return "Message"
            XIAOMI_MESSAGE_PACKAGE -> return "Message"
            MESSAGE_SAMSUNG_PACKAGE -> return "Message"
            MESSAGES_PACKAGE -> return "Message"
            Telephony.Sms.getDefaultSmsPackage(context) -> return "Message"

            SIGNAL_PACKAGE -> return "Signal"
            TELEGRAM_PACKAGE -> return "Telegram"
            INSTAGRAM_PACKAGE -> return "Instagram"
            DISCORD_PACKAGE -> return "Discord"
            TIKTOK_PACKAGE -> return "Tiktok"
            SNAPCHAT_PACKAGE -> return "Snapchat"
            REDDIT_PACKAGE -> return "Reddit"
            VIBER_PACKAGE -> return "Viber"
            YOUTUBE_PACKAGE -> return "YouTube"
            GOOGLE_PACKAGE -> return "Google"
            SCREEN_RECORDER -> return "Screen Recorder"
            else -> return ""
        }
    }

//    fun convertPackageToBackgroundPackage(packageName: String, context: Context): Int {
//        when (packageName) {
//            FACEBOOK_PACKAGE -> return R.drawable.rounded_notification_background_facebook
//            MESSENGER_PACKAGE -> return R.drawable.rounded_notification_background_facebook
//            WHATSAPP_PACKAGE -> return R.drawable.rounded_notification_background_whatsapp
//            GMAIL_PACKAGE -> return R.drawable.rounded_notification_background_gmail
//            OUTLOOK_PACKAGE -> return R.drawable.rounded_notification_background_outlook
//
//            MESSAGE_PACKAGE -> return R.drawable.rounded_notification_background_facebook
//            XIAOMI_MESSAGE_PACKAGE -> return R.drawable.rounded_notification_background_xiaomi
//            MESSAGE_SAMSUNG_PACKAGE -> return R.drawable.rounded_notification_background_facebook
//            MESSAGES_PACKAGE -> return R.drawable.rounded_notification_background_facebook
//            Telephony.Sms.getDefaultSmsPackage(context) -> return R.drawable.rounded_notification_background_knock_in
//
//            SIGNAL_PACKAGE -> return R.drawable.rounded_notification_background_outlook
//            TELEGRAM_PACKAGE -> return R.drawable.rounded_notification_background_telegram
//            INSTAGRAM_PACKAGE -> return R.drawable.rounded_notification_background_instagram
//            DISCORD_PACKAGE -> return R.drawable.rounded_notification_background_discord
//            TIKTOK_PACKAGE -> return R.drawable.rounded_notification_background_tiktok
//            SNAPCHAT_PACKAGE -> return R.drawable.rounded_notification_background_snapchat
//            REDDIT_PACKAGE -> return R.drawable.rounded_notification_background_reddit
//            VIBER_PACKAGE -> return R.drawable.rounded_notification_background_viber
//            YOUTUBE_PACKAGE -> return R.drawable.rounded_notification_background_youtube
//            GOOGLE_PACKAGE -> return R.drawable.rounded_notification_background_google
//            else -> return R.drawable.rounded_notification_background_knock_in
//        }
//    }

    fun isMessagingApp(packageName: String, context: Context): Boolean {
        return when (packageName) {
            FACEBOOK_PACKAGE -> true
            MESSENGER_PACKAGE -> true
            WHATSAPP_PACKAGE -> true
            GMAIL_PACKAGE -> true
            OUTLOOK_PACKAGE -> true
            MESSAGE_PACKAGE, XIAOMI_MESSAGE_PACKAGE, MESSAGE_SAMSUNG_PACKAGE -> true
            Telephony.Sms.getDefaultSmsPackage(context) -> true
            SIGNAL_PACKAGE -> true
            TELEGRAM_PACKAGE -> true
            INSTAGRAM_PACKAGE -> true
            DISCORD_PACKAGE -> true
            TIKTOK_PACKAGE -> true
            SNAPCHAT_PACKAGE -> true
            REDDIT_PACKAGE -> true
            MESSAGES_PACKAGE -> true
            VIBER_PACKAGE -> true
            else -> false
        }
    }

//    //region ============================================= goTo =============================================
//
//    private fun goToSkype() {
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
//
//    private fun goToLinkedin() {
//        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("linkedin://you"))
//        try {
//            startActivity(appIntent)
//        } catch (e: ActivityNotFoundException) {
//            startActivity(
//                Intent(
//                    Intent.ACTION_VIEW,
//                    Uri.parse("https://linkedin.com/")
//                )
//            )
//        }
//    }
//
//    private fun goToTwitter() {
//        val appIntent = Intent(Intent.ACTION_VIEW)
//        appIntent.setClassName("com.twitter.android", "com.twitter.android")
//        try {
//            startActivity(appIntent)
//        } catch (e: ActivityNotFoundException) {
//            startActivity(
//                Intent(
//                    Intent.ACTION_VIEW,
//                    Uri.parse("https://twitter.com/")
//                )
//            )
//        }
//    }
//
//    private fun goToFacebook() {
//        val uri = Uri.parse("facebook:/newsfeed")
//        val likeIng = Intent(Intent.ACTION_VIEW, uri)
//        try {
//            startActivity(likeIng)
//        } catch (e: ActivityNotFoundException) {
//            startActivity(
//                Intent(
//                    Intent.ACTION_VIEW,
//                    Uri.parse("http://facebook.com/")
//                )
//            )
//        }
//    }
//
//    private fun goToInstagramPage() {
//        val uri = Uri.parse("https://www.instagram.com/")
//        val likeIng = Intent(Intent.ACTION_VIEW, uri)
//
//        likeIng.setPackage("com.instagram.android")
//
//        try {
//            startActivity(likeIng)
//        } catch (e: ActivityNotFoundException) {
//            startActivity(
//                Intent(
//                    Intent.ACTION_VIEW,
//                    Uri.parse("https://instagram.com/")
//                )
//            )
//        }
//    }
//
//    private fun openGmail(context: Context) {
//        val intent = context.packageManager.getLaunchIntentForPackage("com.google.android.gm")
//        intent?.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//        context.startActivity(intent)
//    }
//
//    private fun phoneCall(phoneNumber: String) {
//        if (ContextCompat.checkSelfPermission(
//                this,
//                Manifest.permission.CALL_PHONE
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            ActivityCompat.requestPermissions(
//                this,
//                arrayOf(Manifest.permission.CALL_PHONE),
//                MAKE_CALL_PERMISSION_REQUEST_CODE
//            )
//            numberForPermission = phoneNumber
//        } else {
//            if (numberForPermission.isEmpty()) {
//                startActivity(Intent(Intent.ACTION_CALL, Uri.fromParts("tel", phoneNumber, null)))
//            } else {
//                startActivity(Intent(Intent.ACTION_CALL, Uri.fromParts("tel", phoneNumber, null)))
//                numberForPermission = ""
//            }
//        }
//    }
//
//    //endregion
}