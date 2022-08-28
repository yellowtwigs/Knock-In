package com.yellowtwigs.knockin.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.TranslateAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TableLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.ui.edit_contact.AddNewContactActivity
import com.yellowtwigs.knockin.ui.group.list.GroupManagerActivity
import com.yellowtwigs.knockin.ui.in_app.PremiumActivity
import com.yellowtwigs.knockin.ui.settings.ManageMyScreenActivity
import com.yellowtwigs.knockin.ui.notifications.settings.NotificationsSettingsActivity
import com.yellowtwigs.knockin.ui.teleworking.TeleworkingActivity
import com.yellowtwigs.knockin.utils.ContactGesture.goToOutlook
import com.yellowtwigs.knockin.utils.ContactGesture.goToSignal
import com.yellowtwigs.knockin.utils.ContactGesture.goToTelegram

/**
 * La Classe qui permet d'afficher la liste des appels reçu
 * @author Kenzy Suon & Ryan Granet
 */
class CockpitActivity : AppCompatActivity() {

    //region ========================================== Var or Val ==========================================

    private val MAKE_CALL_PERMISSION_REQUEST_CODE = 1

    private var numberForPermission = ""

    private var cockpit_DrawerLayout: DrawerLayout? = null

    private var bottomNavigationView: BottomNavigationView? = null
    private var cockpit_IncomingCallButton: FloatingActionButton? = null
    private var cockpit_SendMessage: ImageView? = null

    private var cockpit_ButtonOpen: FloatingActionButton? = null
    private var cockpit_CallLayout: ConstraintLayout? = null
    private var cockpit_KeyboardView: TableLayout? = null
    private var cockpit_EditTextLayout: ConstraintLayout? = null
    private var cockpit_PhoneNumberEditText: AppCompatEditText? = null
    private var cockpit_ButtonClose: ImageView? = null

    // Keyboard Pad
    private var cockpit_CallKeyboard_1: RelativeLayout? = null
    private var cockpit_CallKeyboard_2: RelativeLayout? = null
    private var cockpit_CallKeyboard_3: RelativeLayout? = null
    private var cockpit_CallKeyboard_4: RelativeLayout? = null
    private var cockpit_CallKeyboard_5: RelativeLayout? = null
    private var cockpit_CallKeyboard_6: RelativeLayout? = null
    private var cockpit_CallKeyboard_7: RelativeLayout? = null
    private var cockpit_CallKeyboard_8: RelativeLayout? = null
    private var cockpit_CallKeyboard_9: RelativeLayout? = null
    private var cockpit_CallKeyboard_Star: RelativeLayout? = null
    private var cockpit_CallKeyboard_0: RelativeLayout? = null
    private var cockpit_CallKeyboard_Sharp: RelativeLayout? = null

    //social network
    private var link_socials_networks_Facebook: AppCompatImageView? = null
    private var link_socials_networks_Messenger: AppCompatImageView? = null
    private var link_socials_networks_Instagram: AppCompatImageView? = null
    private var link_socials_networks_Whatsapp: AppCompatImageView? = null
    private var link_socials_networks_Gmail: AppCompatImageView? = null
    private var link_socials_networks_Snapchat: AppCompatImageView? = null
    private var link_socials_networks_Telegram: AppCompatImageView? = null
    private var link_socials_networks_Outlook: AppCompatImageView? = null
    private var link_socials_networks_Skype: AppCompatImageView? = null
    private var link_socials_networks_Linkedin: AppCompatImageView? = null
    private var link_socials_networks_Twitter: AppCompatImageView? = null
    private var signalIcon: AppCompatImageView? = null

    private var cockpit_CallBackSpace: ImageView? = null
    private var cockpit_ButtonAddContact: ImageView? = null

    private val mOnNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_contacts -> {

                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_groups -> {
                    startActivity(
                        Intent(
                            this@CockpitActivity,
                            GroupManagerActivity::class.java
                        ).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    )
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_teleworking -> {
                    startActivity(
                        Intent(
                            this@CockpitActivity,
                            TeleworkingActivity::class.java
                        ).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    )
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_notifcations -> {

                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_cockpit -> {
                }
            }
            false
        }

    //endregion

    @SuppressLint("ShowToast", "Recycle", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //region ======================================== Theme Dark ========================================

        val sharedThemePreferences = getSharedPreferences("Knockin_Theme", Context.MODE_PRIVATE)
        if (sharedThemePreferences.getBoolean("darkTheme", false)) {
            setTheme(R.style.AppThemeDark)
        } else {
            setTheme(R.style.AppTheme)
        }

        //endregion

        setContentView(R.layout.activity_cockpit)
        hideKeyboard()
        val listApp = getAppOnPhone()

        //region ========================================== Toolbar =========================================

        val cockpitOpenDrawer = findViewById<AppCompatImageView>(R.id.cockpit_open_drawer)
        val cockpitHelp = findViewById<AppCompatImageView>(R.id.cockpit_toolbar_help)

        //endregion

        //region ======================================= FindViewById =======================================

        cockpit_IncomingCallButton = findViewById(R.id.cockpit_incoming_call_button)
        bottomNavigationView = findViewById(R.id.navigation)

        cockpit_ButtonOpen = findViewById(R.id.cockpit_button_open_id)
        cockpit_CallLayout = findViewById(R.id.cockpit_call_layout_id)
        cockpit_ButtonClose = findViewById(R.id.cockpit_button_close_id)

        cockpit_SendMessage = findViewById(R.id.cockpit_send_message)
        cockpit_KeyboardView = findViewById(R.id.cockpit_call_keyboard_view)
        cockpit_EditTextLayout = findViewById(R.id.cockpit_call_edit_text_layout)
        cockpit_PhoneNumberEditText = findViewById(R.id.cockpit_call_phone_number_edit_text)

        cockpit_CallKeyboard_1 = findViewById(R.id.cockpit_call_keyboard_1)
        cockpit_CallKeyboard_2 = findViewById(R.id.cockpit_call_keyboard_2)
        cockpit_CallKeyboard_3 = findViewById(R.id.cockpit_call_keyboard_3)
        cockpit_CallKeyboard_4 = findViewById(R.id.cockpit_call_keyboard_4)
        cockpit_CallKeyboard_5 = findViewById(R.id.cockpit_call_keyboard_5)
        cockpit_CallKeyboard_6 = findViewById(R.id.cockpit_call_keyboard_6)
        cockpit_CallKeyboard_7 = findViewById(R.id.cockpit_call_keyboard_7)
        cockpit_CallKeyboard_8 = findViewById(R.id.cockpit_call_keyboard_8)
        cockpit_CallKeyboard_9 = findViewById(R.id.cockpit_call_keyboard_9)
        cockpit_CallKeyboard_Star = findViewById(R.id.cockpit_call_keyboard_star)
        cockpit_CallKeyboard_0 = findViewById(R.id.cockpit_call_keyboard_0)
        cockpit_CallKeyboard_Sharp = findViewById(R.id.cockpit_call_keyboard_sharp)

        link_socials_networks_Facebook = findViewById(R.id.facebook_link_socials_networks)
        link_socials_networks_Messenger = findViewById(R.id.messenger_link_socials_networks)
        link_socials_networks_Instagram = findViewById(R.id.instagram_link_socials_networks)
        link_socials_networks_Gmail = findViewById(R.id.gmail_link_socials_networks)
        link_socials_networks_Snapchat = findViewById(R.id.snapchat_link_socials_networks)
        link_socials_networks_Telegram = findViewById(R.id.telegram_link_socials_networks)
        link_socials_networks_Outlook = findViewById(R.id.outlook_link_socials_networks)
        link_socials_networks_Skype = findViewById(R.id.skype_link_socials_networks)
        link_socials_networks_Linkedin = findViewById(R.id.linkedin_link_socials_networks)
        link_socials_networks_Twitter = findViewById(R.id.twitter_link_socials_networks)
        link_socials_networks_Whatsapp = findViewById(R.id.whatsapp_link_socials_networks)
        signalIcon = findViewById(R.id.signal_icon)

        cockpit_CallBackSpace = findViewById(R.id.cockpit_call_back_space)
        cockpit_ButtonAddContact = findViewById(R.id.cockpit_button_add_contact)

        val settings_left_drawer_ThemeSwitch =
            findViewById<SwitchCompat>(R.id.settings_left_drawer_theme_switch)

        //endregion

        if (sharedThemePreferences.getBoolean("darkTheme", false)) {
            settings_left_drawer_ThemeSwitch?.isChecked = true
//            group_manager_MainLayout?.setBackgroundResource(R.drawable.dark_background)
        }

        //region ================================ Call Popup from LeftDrawer ================================

        val sharedPreferencePopup = getSharedPreferences("Phone_call", Context.MODE_PRIVATE)
        val settings_CallPopupSwitch = findViewById<SwitchCompat>(R.id.settings_call_popup_switch)

        if (sharedPreferencePopup.getBoolean("popup", true)) {
            settings_CallPopupSwitch?.isChecked = true
        }

        //endregion

        bottomNavigationView?.menu?.getItem(3)?.isChecked = true
        bottomNavigationView?.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        if (cockpit_PhoneNumberEditText?.text?.isEmpty() == true) {
            cockpit_EditTextLayout?.visibility = View.GONE
        } else {
            cockpit_EditTextLayout?.visibility = View.VISIBLE
        }

        //region ======================================= DrawerLayout =======================================

        cockpit_DrawerLayout = findViewById(R.id.cockpit_drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.cockpit_nav_view)
        val menu = navigationView.menu
        val navItem = menu.findItem(R.id.nav_home)
        navItem.isChecked = true

        navigationView?.menu?.getItem(0)?.isChecked = true

        navigationView.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            cockpit_DrawerLayout?.closeDrawers()

            when (menuItem.itemId) {
                R.id.nav_notifications -> startActivity(
                    Intent(
                        this@CockpitActivity,
                        NotificationsSettingsActivity::class.java
                    )
                )
                R.id.navigation_teleworking -> startActivity(
                    Intent(
                        this@CockpitActivity,
                        TeleworkingActivity::class.java
                    )
                )
                R.id.nav_in_app -> startActivity(
                    Intent(
                        this@CockpitActivity,
                        PremiumActivity::class.java
                    )
                )
                R.id.nav_manage_screen -> startActivity(
                    Intent(
                        this@CockpitActivity,
                        ManageMyScreenActivity::class.java
                    )
                )
                R.id.nav_help -> startActivity(
                    Intent(
                        this@CockpitActivity,
                        HelpActivity::class.java
                    )
                )
            }

            cockpit_DrawerLayout?.closeDrawer(GravityCompat.START)
            true
        }

        //endregion

        //region ========================================== Listener ========================================

        cockpitOpenDrawer.setOnClickListener {
            cockpit_DrawerLayout?.openDrawer(GravityCompat.START)
            hideKeyboard()

        }
        cockpitHelp.setOnClickListener {
            if (Resources.getSystem().configuration.locale.language == "fr") {
                val browserIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.yellowtwigs.com/aide-en-ligne-cockpit")
                )
                startActivity(browserIntent)
            } else {
                val browserIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.yellowtwigs.com/help-cockpit")
                )
                startActivity(browserIntent)
            }
        }

        settings_CallPopupSwitch?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val sharedCallPopupPreferences: SharedPreferences =
                    getSharedPreferences("Phone_call", Context.MODE_PRIVATE)
                val edit: SharedPreferences.Editor = sharedCallPopupPreferences.edit()
                edit.putBoolean("popup", true)
                edit.apply()
            } else {
                val sharedCallPopupPreferences: SharedPreferences =
                    getSharedPreferences("Phone_call", Context.MODE_PRIVATE)
                val edit: SharedPreferences.Editor = sharedCallPopupPreferences.edit()
                edit.putBoolean("popup", false)
                edit.apply()
            }
        }

        settings_left_drawer_ThemeSwitch?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                setTheme(R.style.AppThemeDark)
//                group_manager_MainLayout!!.setBackgroundResource(R.drawable.dark_background)
                val edit: SharedPreferences.Editor = sharedThemePreferences.edit()
                edit.putBoolean("darkTheme", true)
                edit.apply()
                startActivity(Intent(this@CockpitActivity, CockpitActivity::class.java))
            } else {
                setTheme(R.style.AppTheme)
//                group_manager_MainLayout!!.setBackgroundResource(R.drawable.mr_white_blur_background)
                val edit: SharedPreferences.Editor = sharedThemePreferences.edit()
                edit.putBoolean("darkTheme", false)
                edit.apply()
                startActivity(Intent(this@CockpitActivity, CockpitActivity::class.java))
            }
        }

        if (!listApp.contains("com.facebook.katana")) {
            link_socials_networks_Facebook?.setImageResource(R.drawable.ic_facebook_disable)
            link_socials_networks_Facebook?.setOnClickListener {
                Toast.makeText(
                    this,
                    R.string.cockpit_toast_facebook_not_install,
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            link_socials_networks_Facebook?.setOnClickListener { goToFacebook() }
        }

        if (!listApp.contains("com.facebook.orca")) {
            link_socials_networks_Messenger?.setImageResource(R.drawable.ic_messenger_disable)
            link_socials_networks_Messenger?.setOnClickListener {
                Toast.makeText(
                    this,
                    R.string.cockpit_toast_messenger_not_install,
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            link_socials_networks_Messenger?.setOnClickListener { openMessenger("") }
        }

        if (!listApp.contains("com.instagram.android")) {
            link_socials_networks_Instagram?.setImageResource(R.drawable.ic_instagram_disable)
            link_socials_networks_Instagram?.setOnClickListener {
                Toast.makeText(
                    this,
                    R.string.cockpit_toast_instagram_not_install,
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            link_socials_networks_Instagram?.setOnClickListener { goToInstagramPage() }
        }

        if (!listApp.contains("com.whatsapp")) {
            link_socials_networks_Whatsapp?.setImageResource(R.drawable.ic_whatsapp_disable)
            link_socials_networks_Whatsapp?.setOnClickListener {
                Toast.makeText(
                    this,
                    R.string.cockpit_toast_whatsapp_not_install,
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            link_socials_networks_Whatsapp?.setOnClickListener { goToWhatsapp() }
        }

        if (!listApp.contains("com.google.android.gm")) {
            link_socials_networks_Gmail?.setImageResource(R.drawable.ic_gmail_disable)
            link_socials_networks_Gmail?.setOnClickListener {
                Toast.makeText(
                    this,
                    R.string.cockpit_toast_gmail_not_install,
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            link_socials_networks_Gmail?.setOnClickListener { goToGmail() }
        }

        if (!listApp.contains("com.snapchat.android")) {
            link_socials_networks_Snapchat?.setImageResource(R.drawable.ic_snapchat_disable)
            link_socials_networks_Snapchat?.setOnClickListener {
                Toast.makeText(
                    this,
                    R.string.cockpit_toast_snapchat_not_install,
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            link_socials_networks_Snapchat?.setOnClickListener { goToSnapchat() }
        }

        if (!listApp.contains("org.telegram.messenger")) {
            link_socials_networks_Telegram?.setImageResource(R.drawable.ic_telegram_disable)
            link_socials_networks_Telegram?.setOnClickListener {
                Toast.makeText(
                    this,
                    R.string.cockpit_toast_telegram_not_install,
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            link_socials_networks_Telegram?.setOnClickListener {
                goToTelegram(this)
            }
        }

        if (!listApp.contains("com.microsoft.office.outlook")) {
            link_socials_networks_Outlook?.setImageResource(R.drawable.ic_outlook_disable)
            link_socials_networks_Outlook?.setOnClickListener {
                Toast.makeText(
                    this,
                    R.string.cockpit_toast_outlook_not_install,
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            link_socials_networks_Outlook?.setOnClickListener { goToOutlook(this@CockpitActivity) }
        }

        if (!listApp.contains("com.skype.raider")) {
            link_socials_networks_Skype?.setImageResource(R.drawable.ic_skype_disable)
            link_socials_networks_Skype?.setOnClickListener {
                Toast.makeText(
                    this,
                    R.string.cockpit_toast_skype_not_install,
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            link_socials_networks_Skype?.setOnClickListener { goToSkype() }
        }

        if (!listApp.contains("com.linkedin.android")) {
            link_socials_networks_Linkedin?.setImageResource(R.drawable.ic_linkedin_disable)
            link_socials_networks_Linkedin?.setOnClickListener {
                Toast.makeText(
                    this,
                    R.string.cockpit_toast_linkedin_not_install,
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            link_socials_networks_Linkedin?.setOnClickListener { goToLinkedin() }
        }

        if (!listApp.contains("com.twitter.android")) {
            link_socials_networks_Twitter?.setImageResource(R.drawable.ic_twitter_disable)
            link_socials_networks_Twitter?.setOnClickListener {
                Toast.makeText(
                    this,
                    R.string.cockpit_toast_twitter_not_install,
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            link_socials_networks_Twitter?.setOnClickListener { goToTwitter() }
        }

        if (!listApp.contains("org.thoughtcrime.securesms")) {
            signalIcon?.setImageResource(R.drawable.ic_signal_disable)
        } else {
            signalIcon?.setOnClickListener { goToSignal(this) }
        }

        cockpit_IncomingCallButton?.setOnClickListener {
            phoneCall(cockpit_PhoneNumberEditText?.text.toString())
        }

        cockpit_SendMessage?.setOnClickListener {
            if (cockpit_PhoneNumberEditText?.text?.isNotEmpty() == true) {
                val phone = cockpit_PhoneNumberEditText?.text.toString()
                val i = Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", phone, null))
                startActivity(i)
            } else {
                Toast.makeText(this, R.string.cockpit_toast_phone_number_empty, Toast.LENGTH_SHORT)
                    .show()
            }
        }

        cockpit_ButtonAddContact?.setOnClickListener {
            if (cockpit_PhoneNumberEditText?.text?.isNotEmpty() == true) {
                startActivity(
                    Intent(
                        this@CockpitActivity,
                        AddNewContactActivity::class.java
                    ).putExtra("ContactPhoneNumber", cockpit_PhoneNumberEditText!!.text.toString())
                )
            } else {
                Toast.makeText(this, R.string.cockpit_toast_phone_number_empty, Toast.LENGTH_SHORT)
                    .show()
            }
        }

        cockpit_ButtonOpen?.setOnClickListener {
            val slideLeft = AnimationUtils.loadAnimation(this, R.anim.slide_left)

            slideUp(cockpit_EditTextLayout!!)
            slideUp(cockpit_KeyboardView!!)
            slideUp(cockpit_CallLayout!!)
            cockpit_ButtonOpen?.startAnimation(slideLeft)

            cockpit_CallLayout?.visibility = View.VISIBLE
            cockpit_KeyboardView?.visibility = View.VISIBLE
            cockpit_ButtonOpen?.visibility = View.GONE
        }

        cockpit_ButtonClose?.setOnClickListener {
            val slideRight = AnimationUtils.loadAnimation(this, R.anim.slide_right)
            val slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down)

            cockpit_EditTextLayout?.startAnimation(slideDown)
            cockpit_KeyboardView?.startAnimation(slideDown)
            cockpit_CallLayout?.startAnimation(slideDown)
            cockpit_ButtonOpen?.startAnimation(slideRight)

            cockpit_CallLayout?.visibility = View.GONE
            cockpit_KeyboardView?.visibility = View.GONE
            cockpit_EditTextLayout?.visibility = View.GONE
            cockpit_ButtonOpen?.visibility = View.VISIBLE
        }

        //region ========================================== Keyboard ========================================

        cockpit_CallKeyboard_1?.setOnClickListener {
            cockpit_EditTextLayout?.visibility = View.VISIBLE
            cockpit_PhoneNumberEditText?.setText(cockpit_PhoneNumberEditText?.text.toString() + 1)
        }
        cockpit_CallKeyboard_2?.setOnClickListener {
            cockpit_EditTextLayout?.visibility = View.VISIBLE
            cockpit_PhoneNumberEditText?.setText(cockpit_PhoneNumberEditText?.text.toString() + 2)
        }
        cockpit_CallKeyboard_3?.setOnClickListener {
            cockpit_EditTextLayout?.visibility = View.VISIBLE
            cockpit_PhoneNumberEditText?.setText(cockpit_PhoneNumberEditText?.text.toString() + 3)
        }
        cockpit_CallKeyboard_4?.setOnClickListener {
            cockpit_EditTextLayout?.visibility = View.VISIBLE
            cockpit_PhoneNumberEditText?.setText(cockpit_PhoneNumberEditText?.text.toString() + 4)
        }
        cockpit_CallKeyboard_5?.setOnClickListener {
            cockpit_EditTextLayout?.visibility = View.VISIBLE
            cockpit_PhoneNumberEditText?.setText(cockpit_PhoneNumberEditText?.text.toString() + 5)
        }
        cockpit_CallKeyboard_6?.setOnClickListener {
            cockpit_EditTextLayout?.visibility = View.VISIBLE
            cockpit_PhoneNumberEditText?.setText(cockpit_PhoneNumberEditText?.text.toString() + 6)
        }
        cockpit_CallKeyboard_7?.setOnClickListener {
            cockpit_EditTextLayout?.visibility = View.VISIBLE
            cockpit_PhoneNumberEditText?.setText(cockpit_PhoneNumberEditText?.text.toString() + 7)
        }
        cockpit_CallKeyboard_8?.setOnClickListener {
            cockpit_EditTextLayout?.visibility = View.VISIBLE
            cockpit_PhoneNumberEditText?.setText(cockpit_PhoneNumberEditText?.text.toString() + 8)
        }
        cockpit_CallKeyboard_9?.setOnClickListener {
            cockpit_EditTextLayout?.visibility = View.VISIBLE
            cockpit_PhoneNumberEditText?.setText(cockpit_PhoneNumberEditText?.text.toString() + 9)
        }
        cockpit_CallKeyboard_Star?.setOnClickListener {
            cockpit_EditTextLayout?.visibility = View.VISIBLE
            cockpit_PhoneNumberEditText?.setText(cockpit_PhoneNumberEditText?.text.toString() + "*")
        }
        cockpit_CallKeyboard_0?.setOnClickListener {
            cockpit_EditTextLayout?.visibility = View.VISIBLE
            cockpit_PhoneNumberEditText?.setText(cockpit_PhoneNumberEditText?.text.toString() + 0)
        }
        cockpit_CallKeyboard_Sharp?.setOnClickListener {
            cockpit_EditTextLayout?.visibility = View.VISIBLE
            cockpit_PhoneNumberEditText?.setText(cockpit_PhoneNumberEditText?.text.toString() + "#")
        }
        cockpit_CallKeyboard_0?.setOnLongClickListener {
            cockpit_EditTextLayout?.visibility = View.VISIBLE
            cockpit_PhoneNumberEditText?.setText(cockpit_PhoneNumberEditText?.text.toString() + "+")
            true
        }
        cockpit_CallBackSpace?.setOnClickListener {
            if (cockpit_PhoneNumberEditText?.text?.isNotEmpty() == true) {
                cockpit_PhoneNumberEditText?.text?.delete(
                    cockpit_PhoneNumberEditText?.length()!! - 1,
                    cockpit_PhoneNumberEditText?.length()!!
                )
            }
        }

        cockpit_CallBackSpace?.setOnLongClickListener {
            if (cockpit_PhoneNumberEditText?.text?.isNotEmpty() == true) {
                cockpit_PhoneNumberEditText?.setText("")
            }
            true
        }

        //endregion

        //endregion
    }

    //region ========================================== Functions ===========================================

    private fun slideUp(view: View) {
        val height = view.height.toFloat()
        val animate = TranslateAnimation(
            0F,                 // fromXDelta
            0F,                 // toXDelta
            height,  // fromYDelta
            0F
        )               // toYDelta
        animate.duration = 500
        animate.fillAfter = true
        view.startAnimation(animate)
    }

    private fun getAppOnPhone(): ArrayList<String> {
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        val resolveInfoList = packageManager.queryIntentActivities(intent, 0)
        val packageNameList = ArrayList<String>()
        for (resolveInfo in resolveInfoList) {
            val activityInfo = resolveInfo.activityInfo
            packageNameList.add(activityInfo.applicationInfo.packageName)
        }
        return packageNameList
    }

    private fun openMessenger(id: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.messenger.com/t/$id"))
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.messenger.com/t/$id"))
            startActivity(intent)
        }
    }

    private fun goToSnapchat() {
        val i = packageManager.getLaunchIntentForPackage("com.snapchat.android")
        try {
            startActivity(i)
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://snapchat.com/")
                )
            )
        }
    }

    private fun goToWhatsapp() {
        val i = packageManager.getLaunchIntentForPackage("com.whatsapp")
        try {
            startActivity(i)
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://whatsapp.com/")
                )
            )
        }
    }

    private fun goToInstagramPage() {
        val uri = Uri.parse("https://www.instagram.com/")
        val likeIng = Intent(Intent.ACTION_VIEW, uri)

        likeIng.setPackage("com.instagram.android")

        try {
            startActivity(likeIng)
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://instagram.com/")
                )
            )
        }
    }

    private fun goToFacebook() {
        val uri = Uri.parse("facebook:/newsfeed")
        val likeIng = Intent(Intent.ACTION_VIEW, uri)
        try {
            startActivity(likeIng)
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://facebook.com/")
                )
            )
        }
    }

    private fun goToGmail() {
        val appIntent = Intent(Intent.ACTION_VIEW)
        appIntent.setClassName(
            "com.google.android.gm",
            "com.google.android.gm.ConversationListActivityGmail"
        )
        try {
            startActivity(appIntent)
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://gmail.com/")
                )
            )
        }
    }

    private fun goToLinkedin() {
        /// don't work
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("linkedin://you"))
        try {
            startActivity(appIntent)
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://linkedin.com/")
                )
            )
        }
    }

    private fun goToSkype() {
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("skype://skype"))
        try {
            startActivity(appIntent)
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://skype.com/")
                )
            )
        }
    }

    private fun goToTwitter() {
        val appIntent = Intent(Intent.ACTION_VIEW)
        appIntent.setClassName("com.twitter.android", "com.twitter.android")
        try {
            startActivity(appIntent)
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://twitter.com/")
                )
            )
        }
    }

    override fun onBackPressed() {
    }

    private fun hideKeyboard() {
        val view = this.currentFocus

        view?.let { v ->
            val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            MAKE_CALL_PERMISSION_REQUEST_CODE -> if (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED) {
                cockpit_IncomingCallButton?.isEnabled = true
            }
        }
    }

    private fun phoneCall(phoneNumber: String) {
        if (!TextUtils.isEmpty(phoneNumber)) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CALL_PHONE
                ) != PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CALL_PHONE),
                    MAKE_CALL_PERMISSION_REQUEST_CODE
                )
                numberForPermission = phoneNumber
            } else {
                if (numberForPermission.isEmpty()) {
                    startActivity(
                        Intent(
                            Intent.ACTION_CALL,
                            Uri.fromParts("tel", phoneNumber, null)
                        )
                    )
                } else {
                    startActivity(
                        Intent(
                            Intent.ACTION_CALL,
                            Uri.fromParts("tel", phoneNumber, null)
                        )
                    )
                    numberForPermission = ""
                }
            }
        } else {
            Toast.makeText(this, R.string.cockpit_toast_phone_number_empty, Toast.LENGTH_SHORT)
                .show()
        }
    }

    //endregion
}