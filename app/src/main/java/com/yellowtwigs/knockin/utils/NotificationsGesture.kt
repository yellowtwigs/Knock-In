package com.yellowtwigs.knockin.utils

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Telephony
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.utils.ContactGesture.goToOutlook
import com.yellowtwigs.knockin.utils.ContactGesture.goToSignal
import com.yellowtwigs.knockin.utils.ContactGesture.goToTelegram
import com.yellowtwigs.knockin.utils.ContactGesture.openMessenger
import com.yellowtwigs.knockin.utils.ContactGesture.openWhatsapp

object NotificationsGesture {
    const val FACEBOOK_PACKAGE = "com.facebook.katana"
    const val MESSENGER_PACKAGE = "com.facebook.orca"
    const val WHATSAPP_PACKAGE = "com.whatsapp"

    const val GMAIL_PACKAGE = "com.google.android.gm"
    const val OUTLOOK_PACKAGE = "com.microsoft.office.outlook"

    private const val MESSAGE_PACKAGE = "com.google.android.apps.messaging"
    private const val XIAOMI_MESSAGE_PACKAGE = "com.android.mms"
    private const val MESSAGE_SAMSUNG_PACKAGE = "com.samsung.android.messaging"

    const val SIGNAL_PACKAGE = "org.thoughtcrime.securesms"
    const val TELEGRAM_PACKAGE = "org.telegram.messenger"
    const val INSTAGRAM_PACKAGE = "com.instagram.android"
    private const val DISCORD_PACKAGE = "com.discord"
    private const val TIKTOK_PACKAGE = "com.zhiliaoapp.musically"
    const val SNAPCHAT_PACKAGE = "com.snapchat.android"
    const val SKYPE_PACKAGE = "com.skype.raider"
    private const val REDDIT_PACKAGE = "com.reddit.frontpage"
    private const val MESSAGES_PACKAGE = "com.chating.messages.chat.fun"
    private const val VIBER_PACKAGE = "com.viber.voip"

    private const val YOUTUBE_PACKAGE = "com.google.android.youtube"
    private const val GOOGLE_PACKAGE = "com.google.android.googlequicksearchbox"
    private const val SCREEN_RECORDER = "com.huawei.screenrecorder"
    const val LINKEDIN_PACKAGE = "com.linkedin.android"
    const val TWITTER_PACKAGE = "com.twitter.android"

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

    fun convertPackageToBackgroundPackage(packageName: String, context: Context): Int {
        when (packageName) {
            FACEBOOK_PACKAGE -> return R.drawable.rounded_notification_background_facebook
            MESSENGER_PACKAGE -> return R.drawable.rounded_notification_background_facebook
            WHATSAPP_PACKAGE -> return R.drawable.rounded_notification_background_whatsapp
            GMAIL_PACKAGE -> return R.drawable.rounded_notification_background_gmail
            OUTLOOK_PACKAGE -> return R.drawable.rounded_notification_background_outlook

            MESSAGE_PACKAGE -> return R.drawable.rounded_notification_background_facebook
            XIAOMI_MESSAGE_PACKAGE -> return R.drawable.rounded_notification_background_xiaomi
            MESSAGE_SAMSUNG_PACKAGE -> return R.drawable.rounded_notification_background_facebook
            MESSAGES_PACKAGE -> return R.drawable.rounded_notification_background_facebook
            Telephony.Sms.getDefaultSmsPackage(context) -> return R.drawable.rounded_notification_background_knock_in

            SIGNAL_PACKAGE -> return R.drawable.rounded_notification_background_outlook
            TELEGRAM_PACKAGE -> return R.drawable.rounded_notification_background_telegram
            INSTAGRAM_PACKAGE -> return R.drawable.rounded_notification_background_instagram
            DISCORD_PACKAGE -> return R.drawable.rounded_notification_background_discord
            TIKTOK_PACKAGE -> return R.drawable.rounded_notification_background_tiktok
            SNAPCHAT_PACKAGE -> return R.drawable.rounded_notification_background_snapchat
            REDDIT_PACKAGE -> return R.drawable.rounded_notification_background_reddit
            VIBER_PACKAGE -> return R.drawable.rounded_notification_background_viber
            YOUTUBE_PACKAGE -> return R.drawable.rounded_notification_background_youtube
            GOOGLE_PACKAGE -> return R.drawable.rounded_notification_background_google
            else -> return R.drawable.rounded_notification_background_knock_in
        }
    }

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

    fun convertPackageNameToGoTo(packageName: String, context: Context) {
        when (packageName) {
            FACEBOOK_PACKAGE -> {
                goToFacebook(context)
            }
            MESSENGER_PACKAGE -> {
                openMessenger("", context)
            }
            WHATSAPP_PACKAGE -> {
                openWhatsapp(context)
            }
            GMAIL_PACKAGE -> {
                openGmail(context)
            }
            OUTLOOK_PACKAGE -> {
                goToOutlook(context)
            }
            SIGNAL_PACKAGE -> {
                goToSignal(context)
            }
            LINKEDIN_PACKAGE -> {
                goToLinkedin(context)
            }
            SKYPE_PACKAGE -> {
                goToSkype(context)
            }
            TELEGRAM_PACKAGE -> {
                goToTelegram(context)
            }
            INSTAGRAM_PACKAGE -> {
                goToInstagram(context)
            }
            TWITTER_PACKAGE -> {
                goToTwitter(context)
            }
            SNAPCHAT_PACKAGE -> {

            }
            VIBER_PACKAGE -> {

            }
            DISCORD_PACKAGE -> {
                goToDiscord(context)
            }
        }
    }

    //region ============================================= goTo =============================================

    private fun goToSkype(context: Context) {
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("skype://skype"))
        try {
            context.startActivity(appIntent)
        } catch (e: ActivityNotFoundException) {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://skype.com/")
                )
            )
        }
    }

    private fun goToLinkedin(context: Context) {
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("linkedin://you"))
        try {
            context.startActivity(appIntent)
        } catch (e: ActivityNotFoundException) {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://linkedin.com/")
                )
            )
        }
    }

    private fun goToTwitter(context: Context) {
        val appIntent = Intent(Intent.ACTION_VIEW)
        appIntent.setClassName("com.twitter.android", "com.twitter.android")
        try {
            context.startActivity(appIntent)
        } catch (e: ActivityNotFoundException) {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://twitter.com/")
                )
            )
        }
    }

    private fun goToFacebook(context: Context) {
        val uri = Uri.parse("facebook:/newsfeed")
        val likeIng = Intent(Intent.ACTION_VIEW, uri)
        try {
            context.startActivity(likeIng)
        } catch (e: ActivityNotFoundException) {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://facebook.com/")
                )
            )
        }
    }

    private fun goToInstagram(context: Context) {
        val uri = Uri.parse("https://www.instagram.com/")
        val likeIng = Intent(Intent.ACTION_VIEW, uri)

        likeIng.setPackage("com.instagram.android")

        try {
            context.startActivity(likeIng)
        } catch (e: ActivityNotFoundException) {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://instagram.com/")
                )
            )
        }
    }

    private fun openGmail(context: Context) {
        val intent = context.packageManager.getLaunchIntentForPackage("com.google.android.gm")
        intent?.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    private fun goToDiscord(context: Context) {
        val i = context.packageManager.getLaunchIntentForPackage("com.discord")
        try {
            context.startActivity(i)
        } catch (e: ActivityNotFoundException) {
        }
    }

    fun phoneCall(cxt: Activity, phoneNumber: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(
                cxt,
                Manifest.permission.CALL_PHONE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                cxt,
                arrayOf(Manifest.permission.CALL_PHONE),
                requestCode
            )
        } else {
            cxt.startActivity(Intent(Intent.ACTION_CALL, Uri.fromParts("tel", phoneNumber, null)))
        }
    }

    //endregion
}