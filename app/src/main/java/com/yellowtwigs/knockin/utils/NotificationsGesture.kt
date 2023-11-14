package com.yellowtwigs.knockin.utils

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Telephony
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.ui.cockpit.CockpitActivity
import com.yellowtwigs.knockin.utils.ContactGesture.goToOutlook
import com.yellowtwigs.knockin.utils.ContactGesture.goToSignal
import com.yellowtwigs.knockin.utils.ContactGesture.goToTelegram
import com.yellowtwigs.knockin.utils.ContactGesture.openMessenger
import com.yellowtwigs.knockin.utils.ContactGesture.openWhatsapp

object NotificationsGesture {
    const val FACEBOOK_PACKAGE = "com.facebook.katana"
    const val FACEBOOK_APP_NAME = "Facebook"

    const val MESSENGER_PACKAGE = "com.facebook.orca"
    const val MESSENGER_APP_NAME = "Messenger"

    const val WHATSAPP_PACKAGE = "com.whatsapp"
    const val WHATSAPP_APP_NAME = "WhatsApp"

    const val GMAIL_PACKAGE = "com.google.android.gm"
    const val GMAIL_APP_NAME = "Gmail"

    const val OUTLOOK_PACKAGE = "com.microsoft.office.outlook"
    const val OUTLOOK_APP_NAME = "Outlook"

    const val MESSAGE_APP_NAME = "SMS"
    const val MESSAGE_PACKAGE = "com.google.android.apps.messaging"
    const val XIAOMI_MESSAGE_PACKAGE = "com.android.mms"
    const val MESSAGE_SAMSUNG_PACKAGE = "com.samsung.android.messaging"
    const val MESSAGES_PACKAGE = "com.chating.messages.chat.fun"
    private const val MESSAGES_GO_PACKAGE = "com.messagesgo.messanger"

    const val CALLS_APP_NAME = "Call"
    const val HUAWEI_SYSTEM_MANAGER_PACKAGE = "com.huawei.systemmanager"
    const val ANDROID_CONTACTS_PACKAGE = "com.android.contacts"
    const val ANDROID_DIALER_PACKAGE = "com.android.dialer"
    const val ANDROID_PHONE_PACKAGE = "com.android.phone"
    const val SAMSUNG_CONTACTS_PACKAGE = "com.samsung.android.contacts"
    const val HUAWEI_CONTACTS_PACKAGE = "com.huawei.contacts"
    const val TRUECALLER_PACKAGE = "com.truecaller"

    const val SIGNAL_PACKAGE = "org.thoughtcrime.securesms"
    const val SIGNAL_APP_NAME = "Signal"

    const val TELEGRAM_PACKAGE = "org.telegram.messenger"
    const val TELEGRAM_APP_NAME = "Telegram"

    const val KNOCKIN_PACKAGE = "com.yellowtwigs.Knockin.notification"
    const val KNOCKIN_NAME = "Knockin"

    const val INSTAGRAM_PACKAGE = "com.instagram.android"
    private const val DISCORD_PACKAGE = "com.discord"
    private const val TIKTOK_PACKAGE = "com.zhiliaoapp.musically"
    const val SNAPCHAT_PACKAGE = "com.snapchat.android"
    const val SKYPE_PACKAGE = "com.skype.raider"
    private const val REDDIT_PACKAGE = "com.reddit.frontpage"
    private const val VIBER_PACKAGE = "com.viber.voip"

    private const val YOUTUBE_PACKAGE = "com.google.android.youtube"
    private const val GOOGLE_PACKAGE = "com.google.android.googlequicksearchbox"
    private const val SCREEN_RECORDER = "com.huawei.screenrecorder"

    const val LINKEDIN_PACKAGE = "com.linkedin.android"
    const val LINKEDIN_NAME = "Linkedin"

    const val TWITTER_PACKAGE = "com.twitter.android"
    const val TWITTER_APP_NAME = "Twitter"

    fun convertPackageToString(packageName: String, context: Context): String {
        when (packageName) {
            FACEBOOK_PACKAGE -> return FACEBOOK_APP_NAME
            MESSENGER_PACKAGE -> return MESSENGER_APP_NAME
            WHATSAPP_PACKAGE -> return WHATSAPP_APP_NAME
            GMAIL_PACKAGE -> return GMAIL_APP_NAME
            OUTLOOK_PACKAGE -> return OUTLOOK_APP_NAME

            MESSAGE_PACKAGE -> return MESSAGE_APP_NAME
            XIAOMI_MESSAGE_PACKAGE -> return MESSAGE_APP_NAME
            MESSAGE_SAMSUNG_PACKAGE -> return MESSAGE_APP_NAME
            MESSAGES_PACKAGE -> return MESSAGE_APP_NAME
            MESSAGES_GO_PACKAGE -> return MESSAGE_APP_NAME
            Telephony.Sms.getDefaultSmsPackage(context) -> return MESSAGE_APP_NAME

            SIGNAL_PACKAGE -> return SIGNAL_APP_NAME
            TELEGRAM_PACKAGE -> return TELEGRAM_APP_NAME
            INSTAGRAM_PACKAGE -> return "Instagram"
            DISCORD_PACKAGE -> return "Discord"
            TIKTOK_PACKAGE -> return "Tiktok"
            SNAPCHAT_PACKAGE -> return "Snapchat"
            VIBER_PACKAGE -> return "Viber"
            YOUTUBE_PACKAGE -> return "YouTube"
            LINKEDIN_PACKAGE -> return LINKEDIN_NAME
            SKYPE_PACKAGE -> return "Skype"
            REDDIT_PACKAGE -> return "Reddit"

            HUAWEI_SYSTEM_MANAGER_PACKAGE -> return CALLS_APP_NAME
            ANDROID_CONTACTS_PACKAGE -> return CALLS_APP_NAME
            ANDROID_DIALER_PACKAGE -> return CALLS_APP_NAME
            ANDROID_PHONE_PACKAGE -> return CALLS_APP_NAME
            SAMSUNG_CONTACTS_PACKAGE -> return CALLS_APP_NAME
            HUAWEI_CONTACTS_PACKAGE -> return CALLS_APP_NAME
            TRUECALLER_PACKAGE -> return CALLS_APP_NAME

            KNOCKIN_PACKAGE -> return KNOCKIN_NAME
            TWITTER_PACKAGE -> return TWITTER_APP_NAME
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
            TWITTER_PACKAGE -> return R.drawable.rounded_notification_background_outlook

            MESSAGE_PACKAGE -> return R.drawable.rounded_notification_background_facebook
            XIAOMI_MESSAGE_PACKAGE -> return R.drawable.rounded_notification_background_xiaomi
            MESSAGE_SAMSUNG_PACKAGE -> return R.drawable.rounded_notification_background_facebook
            MESSAGES_PACKAGE -> return R.drawable.rounded_notification_background_facebook
            MESSAGES_GO_PACKAGE -> return R.drawable.rounded_notification_background_facebook
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
            WHATSAPP_PACKAGE -> true
            GMAIL_PACKAGE -> true
            OUTLOOK_PACKAGE -> true
            MESSAGE_PACKAGE, XIAOMI_MESSAGE_PACKAGE, MESSAGE_SAMSUNG_PACKAGE, MESSAGES_GO_PACKAGE -> true
            Telephony.Sms.getDefaultSmsPackage(context) -> true
            KNOCKIN_PACKAGE -> true
            SIGNAL_PACKAGE -> true
            TELEGRAM_PACKAGE -> true
            MESSAGES_PACKAGE -> true
            VIBER_PACKAGE -> true
            else -> false
        }
    }

    fun isPhoneCall(packageName: String): Boolean {
        return when (packageName) {
            HUAWEI_SYSTEM_MANAGER_PACKAGE -> true
            ANDROID_CONTACTS_PACKAGE -> true
            ANDROID_DIALER_PACKAGE -> true
            ANDROID_PHONE_PACKAGE -> true
            SAMSUNG_CONTACTS_PACKAGE -> true
            HUAWEI_CONTACTS_PACKAGE -> true
            TRUECALLER_PACKAGE -> true
            else -> false
        }
    }

    fun isSocialMedia(packageName: String): Boolean {
        return when (packageName) {
            FACEBOOK_PACKAGE -> true
            KNOCKIN_PACKAGE -> true
            INSTAGRAM_PACKAGE -> true
            DISCORD_PACKAGE -> true
            TIKTOK_PACKAGE -> true
            SNAPCHAT_PACKAGE -> true
            REDDIT_PACKAGE -> true
            TWITTER_PACKAGE -> true
            LINKEDIN_PACKAGE -> true
            else -> false
        }
    }

    fun convertPackageNameToGoToWithContact(packageName: String, contact: String, context: Context) {
        when (packageName) {
            FACEBOOK_PACKAGE -> {
                goToFacebook(context)
            }

            MESSENGER_PACKAGE -> {
                openMessenger(contact, context)
            }

            WHATSAPP_PACKAGE -> {
                openWhatsapp(contact.replace(" ", ""), context)
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
                val intent = context.packageManager.getLaunchIntentForPackage("com.snapchat.android")
                intent?.let {
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(it)
                }
            }

            VIBER_PACKAGE -> {
                val intent = context.packageManager.getLaunchIntentForPackage("com.viber.voip")
                intent?.let {
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(it)
                }
            }

            DISCORD_PACKAGE -> {
                goToDiscord(context)
            }

            MESSAGE_PACKAGE, XIAOMI_MESSAGE_PACKAGE, MESSAGE_SAMSUNG_PACKAGE, MESSAGES_GO_PACKAGE, Telephony.Sms.getDefaultSmsPackage(
                context
            ) -> {
                openSms(contact, context)
            }

            else -> {
                val intent = context.packageManager.getLaunchIntentForPackage(packageName)
                intent?.let {
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(it)
                }
            }
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

            MESSAGE_PACKAGE, XIAOMI_MESSAGE_PACKAGE, MESSAGE_SAMSUNG_PACKAGE, MESSAGES_GO_PACKAGE, Telephony.Sms.getDefaultSmsPackage(
                context
            ) -> {
                openSms("", context)
            }
        }
    }

    //region ============================================= goTo =============================================

    fun openSms(phoneNumber: String, context: Context) {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("sms", phoneNumber, null))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        context.startActivity(intent)
    }

    private fun goToSkype(context: Context) {
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("skype://skype"))
        try {
            context.startActivity(appIntent)
        } catch (e: ActivityNotFoundException) {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW, Uri.parse("https://skype.com/")
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
                    Intent.ACTION_VIEW, Uri.parse("https://linkedin.com/")
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
                    Intent.ACTION_VIEW, Uri.parse("https://twitter.com/")
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
                    Intent.ACTION_VIEW, Uri.parse("http://facebook.com/")
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
                    Intent.ACTION_VIEW, Uri.parse("https://instagram.com/")
                )
            )
        }
    }

    fun openGmail(context: Context) {
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

    fun phoneCall(cxt: Context, phoneNumber: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(
                cxt, Manifest.permission.CALL_PHONE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                cxt as CockpitActivity, arrayOf(Manifest.permission.CALL_PHONE), requestCode
            )
        } else {
            cxt.startActivity(Intent(Intent.ACTION_CALL, Uri.fromParts("tel", phoneNumber, null)))
        }
    }

    //endregion
}