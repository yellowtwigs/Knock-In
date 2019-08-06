package com.example.knocker.controller.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.TranslateAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.knocker.R
import com.example.knocker.controller.activity.group.GroupActivity
import com.example.knocker.controller.activity.group.GroupManagerActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import java.util.ArrayList

/**
 * La Classe qui permet d'afficher la liste des appels reçu
 * @author Kenzy Suon & Ryan Granet
 */
class PhoneLogActivity : AppCompatActivity() {

    //region ========================================== Var or Val ==========================================

    private val MAKE_CALL_PERMISSION_REQUEST_CODE = 1
    /*private val PERMISSIONS_REQUEST_READ_CALL_LOG = 100*/

    private var numberForPermission = ""

    private var phone_log_DrawerLayout: DrawerLayout? = null

    private var main_BottomNavigationView: BottomNavigationView? = null
    private var phone_log_IncomingCallButton: FloatingActionButton? = null
    private var phone_log_SendMessage: ImageView? = null

    private var phone_log_ButtonOpen: FloatingActionButton? = null
    private var phone_log_CallLayout: ConstraintLayout? = null
    private var phone_log_KeyboardView: TableLayout? = null
    private var phone_log_EditTextLayout: ConstraintLayout? = null
    private var phone_log_PhoneNumberEditText: AppCompatEditText? = null
    private var phone_log_ButtonClose: ImageView? = null

    // Keyboard Pad
    private var phone_log_CallKeyboard_1: RelativeLayout? = null
    private var phone_log_CallKeyboard_2: RelativeLayout? = null
    private var phone_log_CallKeyboard_3: RelativeLayout? = null
    private var phone_log_CallKeyboard_4: RelativeLayout? = null
    private var phone_log_CallKeyboard_5: RelativeLayout? = null
    private var phone_log_CallKeyboard_6: RelativeLayout? = null
    private var phone_log_CallKeyboard_7: RelativeLayout? = null
    private var phone_log_CallKeyboard_8: RelativeLayout? = null
    private var phone_log_CallKeyboard_9: RelativeLayout? = null
    private var phone_log_CallKeyboard_Star: RelativeLayout? = null
    private var phone_log_CallKeyboard_0: RelativeLayout? = null
    private var phone_log_CallKeyboard_Sharp: RelativeLayout? = null

    //social network
    private var link_socials_networks_Messenger: AppCompatImageView? = null
    private var link_socials_networks_Instagram: AppCompatImageView? = null
    private var link_socials_networks_Facebook: AppCompatImageView? = null
    private var link_socials_networks_Whatsapp: AppCompatImageView? = null
    private var link_socials_networks_Youtube: AppCompatImageView? = null
    private var link_socials_networks_Gmail: AppCompatImageView? = null
    private var link_socials_networks_Snapchat: AppCompatImageView? = null
    private var link_socials_networks_Telegram: AppCompatImageView? = null
    private var link_socials_networks_Outlook: AppCompatImageView? = null
    private var link_socials_networks_Skype: AppCompatImageView? = null
    private var link_socials_networks_Linkedin: AppCompatImageView? = null
    private var link_socials_networks_Twitter: AppCompatImageView? = null

    private var phone_log_CallBackSpace: ImageView? = null
    private var phone_log_ButtonAddContact: ImageView? = null

    //    private var phone_log_Calls: TextView? = null
    /*private var phone_log_CallsListView: ListView? = null*/

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_contacts -> {
                startActivity(Intent(this@PhoneLogActivity, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_groups -> {
                startActivity(Intent(this@PhoneLogActivity, GroupActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifcations -> {
                startActivity(Intent(this@PhoneLogActivity, NotificationHistoryActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_phone_keyboard -> {
                startActivity(Intent(this@PhoneLogActivity, PhoneLogActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    //endregion

    @SuppressLint("ShowToast", "Recycle", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedThemePreferences = getSharedPreferences("Knocker_Theme", Context.MODE_PRIVATE)
        if (sharedThemePreferences.getBoolean("darkTheme", false)) {
            setTheme(R.style.AppThemeDark)
        } else {
            setTheme(R.style.AppTheme)
        }
        setContentView(R.layout.activity_phone_log)
        hideKeyboard()
        val listApp = getAppOnPhone()

        //region ========================================== Toolbar =========================================

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar.run {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_open_drawer)
        }
        toolbar.setTitle(R.string.phone_log_toolbar_title)

        //endregion

        //region ======================================= FindViewById =======================================

        phone_log_IncomingCallButton = findViewById(R.id.phone_log_incoming_call_button)
        main_BottomNavigationView = findViewById(R.id.navigation)

        phone_log_ButtonOpen = findViewById(R.id.phone_log_button_open_id)
        phone_log_CallLayout = findViewById(R.id.phone_log_call_layout_id)
        phone_log_ButtonClose = findViewById(R.id.phone_log_button_close_id)

        phone_log_SendMessage = findViewById(R.id.phone_log_send_message)
        phone_log_KeyboardView = findViewById(R.id.phone_log_call_keyboard_view)
        phone_log_EditTextLayout = findViewById(R.id.phone_log_call_edit_text_layout)
        phone_log_PhoneNumberEditText = findViewById(R.id.phone_log_call_phone_number_edit_text)

        phone_log_CallKeyboard_1 = findViewById(R.id.phone_log_call_keyboard_1)
        phone_log_CallKeyboard_2 = findViewById(R.id.phone_log_call_keyboard_2)
        phone_log_CallKeyboard_3 = findViewById(R.id.phone_log_call_keyboard_3)
        phone_log_CallKeyboard_4 = findViewById(R.id.phone_log_call_keyboard_4)
        phone_log_CallKeyboard_5 = findViewById(R.id.phone_log_call_keyboard_5)
        phone_log_CallKeyboard_6 = findViewById(R.id.phone_log_call_keyboard_6)
        phone_log_CallKeyboard_7 = findViewById(R.id.phone_log_call_keyboard_7)
        phone_log_CallKeyboard_8 = findViewById(R.id.phone_log_call_keyboard_8)
        phone_log_CallKeyboard_9 = findViewById(R.id.phone_log_call_keyboard_9)
        phone_log_CallKeyboard_Star = findViewById(R.id.phone_log_call_keyboard_star)
        phone_log_CallKeyboard_0 = findViewById(R.id.phone_log_call_keyboard_0)
        phone_log_CallKeyboard_Sharp = findViewById(R.id.phone_log_call_keyboard_sharp)

        link_socials_networks_Messenger = findViewById(R.id.messenger_link_socials_networks)
        link_socials_networks_Instagram = findViewById(R.id.instagram_link_socials_networks)
        link_socials_networks_Facebook = findViewById(R.id.facebook_link_socials_networks)
        link_socials_networks_Youtube = findViewById(R.id.youtube_link_socials_networks)
        link_socials_networks_Gmail = findViewById(R.id.gmail_link_socials_networks)
        link_socials_networks_Snapchat = findViewById(R.id.snapchat_link_socials_networks)
        link_socials_networks_Telegram = findViewById(R.id.telegram_link_socials_networks)
        link_socials_networks_Outlook = findViewById(R.id.outlook_link_socials_networks)
        link_socials_networks_Skype = findViewById(R.id.skype_link_socials_networks)
        link_socials_networks_Linkedin = findViewById(R.id.linkedin_link_socials_networks)
        link_socials_networks_Twitter = findViewById(R.id.twitter_link_socials_networks)
        link_socials_networks_Whatsapp = findViewById(R.id.whatsapp_link_socials_networks)

        phone_log_CallBackSpace = findViewById(R.id.phone_log_call_back_space)
        phone_log_ButtonAddContact = findViewById(R.id.phone_log_button_add_contact)

        //endregion

        main_BottomNavigationView!!.menu.getItem(3).isChecked = true
        main_BottomNavigationView!!.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        if (phone_log_PhoneNumberEditText!!.text!!.isEmpty()) {
            phone_log_EditTextLayout!!.visibility = View.GONE
        } else {
            phone_log_EditTextLayout!!.visibility = View.VISIBLE
        }

        //region ======================================= DrawerLayout =======================================

        // Drawerlayout
        phone_log_DrawerLayout = findViewById(R.id.phone_log_drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.phone_log_nav_view)
        val menu = navigationView.menu
        val nav_item = menu.findItem(R.id.nav_home)
        nav_item.isChecked = true

        navigationView!!.menu.getItem(0).isChecked = true

        navigationView.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            phone_log_DrawerLayout!!.closeDrawers()

            when (menuItem.itemId) {
                R.id.nav_home -> startActivity(Intent(this@PhoneLogActivity, MainActivity::class.java))
                R.id.nav_groups -> startActivity(Intent(this@PhoneLogActivity, GroupManagerActivity::class.java))
                R.id.nav_informations -> startActivity(Intent(this@PhoneLogActivity, EditInformationsActivity::class.java))
                R.id.nav_notif_config -> startActivity(Intent(this@PhoneLogActivity, ManageNotificationActivity::class.java))
                R.id.nav_settings -> startActivity(Intent(this@PhoneLogActivity, SettingsActivity::class.java))
                R.id.nav_manage_screen -> startActivity(Intent(this@PhoneLogActivity, ManageMyScreenActivity::class.java))
                R.id.nav_data_access -> {
                }
                R.id.nav_knockons -> startActivity(Intent(this@PhoneLogActivity, ManageKnockonsActivity::class.java))
                R.id.nav_statistics -> {
                }
                R.id.nav_help -> startActivity(Intent(this@PhoneLogActivity, HelpActivity::class.java))
            }

            val drawer = findViewById<DrawerLayout>(R.id.phone_log_drawer_layout)
            drawer.closeDrawer(GravityCompat.START)
            true
        }

        //endregion

        //region ========================================== Listener ========================================

        if (!listApp.contains("com.facebook.katana")) {
            link_socials_networks_Messenger!!.setImageResource(R.drawable.ic_facebook_disable)
            link_socials_networks_Messenger!!.setOnClickListener { Toast.makeText(this, R.string.phone_log_toast_facebook_not_install, Toast.LENGTH_SHORT).show() }
        } else {
            link_socials_networks_Messenger!!.setOnClickListener { gotToFacebookPage("") }
        }

        if (!listApp.contains("com.instagram.android")) {
            link_socials_networks_Instagram!!.setImageResource(R.drawable.ic_instagram_disable)
            link_socials_networks_Instagram!!.setOnClickListener { Toast.makeText(this, R.string.phone_log_toast_instagram_not_install, Toast.LENGTH_SHORT).show() }
        } else {
            link_socials_networks_Instagram!!.setOnClickListener { goToInstagramPage() }
        }

        if (!listApp.contains("com.whatsapp")) {
            link_socials_networks_Whatsapp!!.setImageResource(R.drawable.ic_whatsapp_disable)
            link_socials_networks_Whatsapp!!.setOnClickListener { Toast.makeText(this, R.string.phone_log_toast_whatsapp_not_install, Toast.LENGTH_SHORT).show() }
        } else {
            link_socials_networks_Whatsapp!!.setOnClickListener { goToWhatsapp() }
        }

        if (!listApp.contains("com.facebook.orca")) {
            link_socials_networks_Facebook!!.setImageResource(R.drawable.ic_messenger_disable)
            link_socials_networks_Facebook!!.setOnClickListener { Toast.makeText(this, R.string.phone_log_toast_messenger_not_install, Toast.LENGTH_SHORT).show() }
        } else {
            link_socials_networks_Facebook!!.setOnClickListener { goToFacebook() }
        }

        if (!listApp.contains("com.google.android.youtube")) {
            link_socials_networks_Youtube!!.setImageResource(R.drawable.ic_youtube_disable)
            link_socials_networks_Youtube!!.setOnClickListener { Toast.makeText(this, R.string.phone_log_toast_youtube_not_install, Toast.LENGTH_SHORT).show() }
        } else {
            link_socials_networks_Youtube!!.setOnClickListener { goToYoutube() }
        }

        if (!listApp.contains("com.google.android.gm")) {
            link_socials_networks_Gmail!!.setImageResource(R.drawable.ic_gmail_disable)
            link_socials_networks_Gmail!!.setOnClickListener { Toast.makeText(this, R.string.phone_log_toast_gmail_not_install, Toast.LENGTH_SHORT).show() }
        } else {
            link_socials_networks_Gmail!!.setOnClickListener { goToGmail() }
        }

        if (!listApp.contains("com.snapchat.android")) {
            link_socials_networks_Snapchat!!.setImageResource(R.drawable.ic_snapchat_disable)
            link_socials_networks_Snapchat!!.setOnClickListener { Toast.makeText(this, R.string.phone_log_toast_snapchat_not_install, Toast.LENGTH_SHORT).show() }
        } else {
            link_socials_networks_Snapchat!!.setOnClickListener { goToSnapchat() }
        }

        if (!listApp.contains("org.telegram.messenger")) {
            link_socials_networks_Telegram!!.setImageResource(R.drawable.ic_telegram_disable)
            link_socials_networks_Telegram!!.setOnClickListener { Toast.makeText(this, R.string.phone_log_toast_telegram_not_install, Toast.LENGTH_SHORT).show() }
        } else {
            link_socials_networks_Telegram!!.setOnClickListener { goToTelegram() }
        }

        if (!listApp.contains("com.microsoft.office.outlook")) {
            link_socials_networks_Outlook!!.setImageResource(R.drawable.ic_outlook_disable)
            link_socials_networks_Outlook!!.setOnClickListener { Toast.makeText(this, R.string.phone_log_toast_outlook_not_install, Toast.LENGTH_SHORT).show() }
        } else {
            link_socials_networks_Outlook!!.setOnClickListener { goToOutlook() }
        }

        if (!listApp.contains("com.skype.raider")) {
            link_socials_networks_Skype!!.setImageResource(R.drawable.ic_skype_disable)
            link_socials_networks_Skype!!.setOnClickListener { Toast.makeText(this, R.string.phone_log_toast_skype_not_install, Toast.LENGTH_SHORT).show() }
        } else {
            link_socials_networks_Skype!!.setOnClickListener { goToSkype() }
        }

        if (!listApp.contains("com.linkedin.android")) {
            link_socials_networks_Linkedin!!.setImageResource(R.drawable.ic_linkedin_disable)
            link_socials_networks_Linkedin!!.setOnClickListener { Toast.makeText(this, R.string.phone_log_toast_linkedin_not_install, Toast.LENGTH_SHORT).show() }
        } else {
            link_socials_networks_Linkedin!!.setOnClickListener { goToLinkedin() }
        }

        if (!listApp.contains("com.twitter.android")) {
            link_socials_networks_Twitter!!.setImageResource(R.drawable.ic_twitter_disable)
            link_socials_networks_Twitter!!.setOnClickListener { Toast.makeText(this, R.string.phone_log_toast_twitter_not_install, Toast.LENGTH_SHORT).show() }
        } else {
            link_socials_networks_Twitter!!.setOnClickListener { goToTwitter() }
        }


        phone_log_IncomingCallButton!!.setOnClickListener {
            phoneCall(phone_log_PhoneNumberEditText!!.text.toString())
        }

        phone_log_SendMessage!!.setOnClickListener {
            if (phone_log_PhoneNumberEditText!!.text!!.isNotEmpty()) {
                val phone = phone_log_PhoneNumberEditText!!.text.toString()
                val i = Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", phone, null))
                startActivity(i)
            } else {
                Toast.makeText(this, R.string.phone_log_toast_phone_number_empty, Toast.LENGTH_SHORT).show()
            }
        }

        phone_log_ButtonAddContact!!.setOnClickListener {
            if (phone_log_PhoneNumberEditText!!.text!!.isNotEmpty()) {
                val intent = Intent(this@PhoneLogActivity, AddNewContactActivity::class.java)
                intent.putExtra("ContactPhoneNumber", phone_log_PhoneNumberEditText!!.text.toString())
                startActivity(intent)
            } else {
                Toast.makeText(this, R.string.phone_log_toast_phone_number_empty, Toast.LENGTH_SHORT).show()
            }
        }

        phone_log_ButtonOpen!!.setOnClickListener {

            val slideLeft = AnimationUtils.loadAnimation(this, R.anim.slide_left)

            slideUp(phone_log_EditTextLayout!!)
            slideUp(phone_log_KeyboardView!!)
            slideUp(phone_log_CallLayout!!)
            phone_log_ButtonOpen!!.startAnimation(slideLeft)

            phone_log_CallLayout!!.visibility = View.VISIBLE
            phone_log_KeyboardView!!.visibility = View.VISIBLE
            phone_log_ButtonOpen!!.visibility = View.GONE
        }

        phone_log_ButtonClose!!.setOnClickListener {

            val slideRight = AnimationUtils.loadAnimation(this, R.anim.slide_right)
            val slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down)

            phone_log_EditTextLayout!!.startAnimation(slideDown)
            phone_log_KeyboardView!!.startAnimation(slideDown)
            phone_log_CallLayout!!.startAnimation(slideDown)
            phone_log_ButtonOpen!!.startAnimation(slideRight)

            phone_log_CallLayout!!.visibility = View.GONE
            phone_log_KeyboardView!!.visibility = View.GONE
            phone_log_EditTextLayout!!.visibility = View.GONE
            phone_log_ButtonOpen!!.visibility = View.VISIBLE
        }

        //region ========================================== Keyboard ========================================

        phone_log_CallKeyboard_1!!.setOnClickListener {
            phone_log_EditTextLayout!!.visibility = View.VISIBLE
            phone_log_PhoneNumberEditText!!.setText(phone_log_PhoneNumberEditText!!.text.toString() + 1)
        }
        phone_log_CallKeyboard_2!!.setOnClickListener {
            phone_log_EditTextLayout!!.visibility = View.VISIBLE
            phone_log_PhoneNumberEditText!!.setText(phone_log_PhoneNumberEditText!!.text.toString() + 2)
        }
        phone_log_CallKeyboard_3!!.setOnClickListener {
            phone_log_EditTextLayout!!.visibility = View.VISIBLE
            phone_log_PhoneNumberEditText!!.setText(phone_log_PhoneNumberEditText!!.text.toString() + 3)
        }
        phone_log_CallKeyboard_4!!.setOnClickListener {
            phone_log_EditTextLayout!!.visibility = View.VISIBLE
            phone_log_PhoneNumberEditText!!.setText(phone_log_PhoneNumberEditText!!.text.toString() + 4)
        }
        phone_log_CallKeyboard_5!!.setOnClickListener {
            phone_log_EditTextLayout!!.visibility = View.VISIBLE
            phone_log_PhoneNumberEditText!!.setText(phone_log_PhoneNumberEditText!!.text.toString() + 5)
        }
        phone_log_CallKeyboard_6!!.setOnClickListener {
            phone_log_EditTextLayout!!.visibility = View.VISIBLE
            phone_log_PhoneNumberEditText!!.setText(phone_log_PhoneNumberEditText!!.text.toString() + 6)
        }
        phone_log_CallKeyboard_7!!.setOnClickListener {
            phone_log_EditTextLayout!!.visibility = View.VISIBLE
            phone_log_PhoneNumberEditText!!.setText(phone_log_PhoneNumberEditText!!.text.toString() + 7)
        }
        phone_log_CallKeyboard_8!!.setOnClickListener {
            phone_log_EditTextLayout!!.visibility = View.VISIBLE
            phone_log_PhoneNumberEditText!!.setText(phone_log_PhoneNumberEditText!!.text.toString() + 8)
        }
        phone_log_CallKeyboard_9!!.setOnClickListener {
            phone_log_EditTextLayout!!.visibility = View.VISIBLE
            phone_log_PhoneNumberEditText!!.setText(phone_log_PhoneNumberEditText!!.text.toString() + 9)
        }
        phone_log_CallKeyboard_Star!!.setOnClickListener {
            phone_log_EditTextLayout!!.visibility = View.VISIBLE
            phone_log_PhoneNumberEditText!!.setText(phone_log_PhoneNumberEditText!!.text.toString() + "*")
        }
        phone_log_CallKeyboard_0!!.setOnClickListener {
            phone_log_EditTextLayout!!.visibility = View.VISIBLE
            phone_log_PhoneNumberEditText!!.setText(phone_log_PhoneNumberEditText!!.text.toString() + 0)
        }
        phone_log_CallKeyboard_Sharp!!.setOnClickListener {
            phone_log_EditTextLayout!!.visibility = View.VISIBLE
            phone_log_PhoneNumberEditText!!.setText(phone_log_PhoneNumberEditText!!.text.toString() + "#")
        }
        phone_log_CallKeyboard_0!!.setOnLongClickListener {
            phone_log_EditTextLayout!!.visibility = View.VISIBLE
            phone_log_PhoneNumberEditText!!.setText(phone_log_PhoneNumberEditText!!.text.toString() + "+")
            true
        }
        phone_log_CallBackSpace!!.setOnClickListener {
            if (phone_log_PhoneNumberEditText!!.text!!.isNotEmpty()) {
                phone_log_PhoneNumberEditText!!.text!!.delete(
                        phone_log_PhoneNumberEditText!!.length() - 1,
                        phone_log_PhoneNumberEditText!!.length())
            }
        }

        phone_log_CallBackSpace!!.setOnLongClickListener {
            if (phone_log_PhoneNumberEditText!!.text!!.isNotEmpty()) {
                phone_log_PhoneNumberEditText!!.setText("")
            }
            true
        }
        phone_log_CallKeyboard_1!!.setOnLongClickListener {
            val telecomManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            if (ContextCompat.checkSelfPermission(this@PhoneLogActivity, Manifest.permission.READ_PHONE_STATE)
                    != PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this@PhoneLogActivity, arrayOf(Manifest.permission.READ_PHONE_STATE), 0)
            } else {
                val numberVoiceMail = telecomManager.voiceMailNumber
                val dial = "tel:$numberVoiceMail"
                startActivity(Intent(Intent.ACTION_DIAL, Uri.parse(dial)))
            }
            true
        }

        //endregion

        //endregion
    }

    //region ========================================== Functions ===========================================

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_help, menu)
        return super.onCreateOptionsMenu(menu)
    }

    @SuppressLint("ShowToast")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                phone_log_DrawerLayout!!.openDrawer(GravityCompat.START)
                hideKeyboard()
                return true
            }
            R.id.item_help -> {
                MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                        .setTitle(R.string.help)
                        .setMessage(this.resources.getString(R.string.help_phone_log))
                        .show()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // slide the view from below itself to the current position
    private fun slideUp(view: View) {
        val height = view.height.toFloat()
        val animate = TranslateAnimation(
                0F,                 // fromXDelta
                0F,                 // toXDelta
                height,  // fromYDelta
                0F)               // toYDelta
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

    private fun gotToFacebookPage(id: String) {
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
            startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://snapchat.com/")))
        }
    }

    private fun goToWhatsapp() {
        val i = packageManager.getLaunchIntentForPackage("com.whatsapp")
        try {
            startActivity(i)
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://whatsapp.com/")))
        }
    }

    private fun goToInstagramPage() {
        val uri = Uri.parse("https://www.instagram.com/")
        val likeIng = Intent(Intent.ACTION_VIEW, uri)

        likeIng.setPackage("com.instagram.android")

        try {
            startActivity(likeIng)
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://instagram.com/")))
        }
    }

    private fun goToFacebook() {
        val uri = Uri.parse("facebook:/newsfeed")
        val likeIng = Intent(Intent.ACTION_VIEW, uri)
        try {
            startActivity(likeIng)
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://facebook.com/")))
        }
    }

    private fun goToGmail() {
        val appIntent = Intent(Intent.ACTION_VIEW)
        appIntent.setClassName("com.google.android.gm", "com.google.android.gm.ConversationListActivityGmail")
        try {
            startActivity(appIntent)
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://gmail.com/")))
        }
    }

    private fun goToLinkedin() {
        /// don't work
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("linkedin://you"))
        try {
            startActivity(appIntent)
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://linkedin.com/")))
        }
    }

    private fun goToOutlook() {
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("ms-outlook://emails"))
        try {
            startActivity(appIntent)
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://outlook.com/")))
        }
    }

    private fun goToSkype() {
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("skype://skype"))
        try {
            startActivity(appIntent)
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://skype.com/")))
        }
    }

    private fun goToTelegram() {
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("tg://resolve"))
        try {
            startActivity(appIntent)
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://web.telegram.org/")))
        }
    }

    private fun goToTwitter() {
        val appIntent = Intent(Intent.ACTION_VIEW)
        appIntent.setClassName("com.twitter.android", "com.twitter.android")
        try {
            startActivity(appIntent)
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://twitter.com/")))
        }
    }

    private fun goToYoutube() {
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube"))
        try {
            startActivity(appIntent)
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://youtube.com/")))
        }
    }

    override fun onBackPressed() {
    }

    private fun hideKeyboard() {
        // Check if no view has focus:
        val view = this.currentFocus

        view?.let { v ->
            val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MAKE_CALL_PERMISSION_REQUEST_CODE -> if (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED) {
                phone_log_IncomingCallButton!!.isEnabled = true
            }
        }
    }

    private fun phoneCall(phoneNumber: String) {
        if (!TextUtils.isEmpty(phoneNumber)) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), MAKE_CALL_PERMISSION_REQUEST_CODE)
                numberForPermission = phoneNumber
            } else {
                if (numberForPermission.isEmpty()) {
                    startActivity(Intent(Intent.ACTION_CALL, Uri.fromParts("tel", phoneNumber, null)))
                } else {
                    startActivity(Intent(Intent.ACTION_CALL, Uri.fromParts("tel", phoneNumber, null)))
                    numberForPermission = ""
                }
            }
        } else {
            Toast.makeText(this, R.string.phone_log_toast_phone_number_empty, Toast.LENGTH_SHORT).show()
        }
    }

    //endregion
}