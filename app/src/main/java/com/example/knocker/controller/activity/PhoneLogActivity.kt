package com.example.knocker.controller.activity

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.CallLog
import android.text.TextUtils
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.regex.Pattern
import com.example.knocker.R
import com.example.knocker.model.PhoneLog
import java.security.AccessController.getContext
import java.util.ArrayList

/**
 * La Classe qui permet d'afficher la liste des appels reçu
 * @author Kenzy Suon
 */
class PhoneLogActivity : AppCompatActivity() {

    //region ========================================== Var or Val ==========================================

    private val MAKE_CALL_PERMISSION_REQUEST_CODE = 1
    private val PERMISSIONS_REQUEST_READ_CALL_LOG = 100

    private var main_BottomNavigationView: BottomNavigationView? = null
    private var phone_log_IncomingCallButton: FloatingActionButton? = null
    private var phone_log_SendMessage: ImageView? = null

    private var phone_log_ButtonOpen: FloatingActionButton? = null
    private var phone_log_CallLayout: ConstraintLayout? = null
    private var phone_log_KeyboardView: TableLayout? = null
    private var phone_log_EditTextLayout: ConstraintLayout? = null
    private var phone_log_PhoneNumberEditText: EditText? = null
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
    private var link_socials_networks_Messenger: ImageView? = null
    private var link_socials_networks_Instagram: ImageView? = null
    private var link_socials_networks_Facebook: ImageView? = null
    private var link_socials_networks_Whatsapp: ImageView? = null
    private var link_socials_networks_Youtube: ImageView? = null
    private var link_socials_networks_Gmail: ImageView? = null
    private var link_socials_networks_Spotify: ImageView? = null
    private var link_socials_networks_Telegram: ImageView? = null
    private var link_socials_networks_Outlook: ImageView? = null
    private var link_socials_networks_Skype: ImageView? = null
    private var link_socials_networks_Linkedin: ImageView? = null
    private var link_socials_networks_Twitter: ImageView? = null

    private var phone_log_CallBackSpace: ImageView? = null
    private var phone_log_ButtonAddContact: ImageView? = null

    //    private var phone_log_Calls: TextView? = null
    private var phone_log_CallsListView: ListView? = null

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_phone_book -> {
                startActivity(Intent(this@PhoneLogActivity, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_groups -> {
            }
            R.id.navigation_notifcations -> {
                startActivity(Intent(this@PhoneLogActivity, NotificationHistoryActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
                return@OnNavigationItemSelectedListener true
            }
//            R.id.navigation_socials_networks -> {
//                startActivity(Intent(this@PhoneLogActivity, SocialsNetworksLinksActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
//                return@OnNavigationItemSelectedListener true
//            }
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
        setContentView(R.layout.activity_phone_log)
        hideKeyboard()

        //region ========================================== Toolbar =========================================

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
//        setSupportActionBar(toolbar)
//        val actionbar = supportActionBar
//        supportActionBar!!.setDisplayShowTitleEnabled(false);
//        actionbar!!.setDisplayHomeAsUpEnabled(true)
        //toolbar.title = "Phone Log" //////////////

        //endregion

        //region ======================================= FindViewById =======================================

        phone_log_IncomingCallButton = findViewById(R.id.phone_log_incoming_call_button)
        phone_log_SendMessage = findViewById(R.id.phone_log_send_message)
        main_BottomNavigationView = findViewById(R.id.navigation)

//        phone_log_Calls = findViewById(R.id.phone_log_calls)

        phone_log_ButtonOpen = findViewById(R.id.phone_log_button_open_id)
        phone_log_CallLayout = findViewById(R.id.phone_log_call_layout_id)
        phone_log_KeyboardView = findViewById(R.id.phone_log_call_keyboard_view)
        phone_log_EditTextLayout = findViewById(R.id.phone_log_call_edit_text_layout)
        phone_log_PhoneNumberEditText = findViewById(R.id.phone_log_call_phone_number_edit_text)
        phone_log_ButtonClose = findViewById(R.id.phone_log_button_close_id)

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
        link_socials_networks_Spotify = findViewById(R.id.spotify_link_socials_networks)
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

        if (phone_log_PhoneNumberEditText!!.text.isEmpty()) {
            phone_log_EditTextLayout!!.visibility = View.GONE
        } else {
            phone_log_EditTextLayout!!.visibility = View.VISIBLE
        }

        //region ========================================= Phone Log ========================================

        showPhoneLog()

        //endregion
        val listApp = getAppOnPhone()
        //region ========================================== Listener ========================================
        if (!listApp.contains("com.facebook.katana")) {
            link_socials_networks_Messenger!!.setImageResource(R.drawable.ic_facebook_disable)
            link_socials_networks_Messenger!!.setOnClickListener { Toast.makeText(this, "Facebook n\'est pas installé", Toast.LENGTH_SHORT).show() }
        } else {
            link_socials_networks_Messenger!!.setOnClickListener { gotToFacebookPage("") }
        }

        if (!listApp.contains("com.instagram.android")) {
            link_socials_networks_Instagram!!.setImageResource(R.drawable.ic_instagram_disable)
            link_socials_networks_Instagram!!.setOnClickListener { Toast.makeText(this, "Instagram n\'est pas installé", Toast.LENGTH_SHORT).show() }
        } else {
            link_socials_networks_Instagram!!.setOnClickListener { goToInstagramPage() }
        }

        if (!listApp.contains("com.whatsapp")) {
            link_socials_networks_Whatsapp!!.setImageResource(R.drawable.ic_whatsapp_disable)
            link_socials_networks_Whatsapp!!.setOnClickListener { Toast.makeText(this, "Whatsapp n\'est pas installé", Toast.LENGTH_SHORT).show() }
        } else {
            link_socials_networks_Whatsapp!!.setOnClickListener { goToWhatsapp() }
        }

        if (!listApp.contains("com.facebook.orca")) {
            link_socials_networks_Facebook!!.setImageResource(R.drawable.ic_messenger_disable)
            link_socials_networks_Facebook!!.setOnClickListener { Toast.makeText(this, "Facebook Messenger n\'est pas installé", Toast.LENGTH_SHORT).show() }
        } else {
            link_socials_networks_Facebook!!.setOnClickListener { goToFacebook() }
        }

        if (!listApp.contains("com.google.android.youtube")) {
            link_socials_networks_Youtube!!.setImageResource(R.drawable.ic_youtube_disable)
            link_socials_networks_Youtube!!.setOnClickListener { Toast.makeText(this, "Youtube n\'est pas installé", Toast.LENGTH_SHORT).show() }
        } else {
            link_socials_networks_Youtube!!.setOnClickListener { goToYoutube() }
        }

        if (!listApp.contains("com.google.android.gm")) {
            link_socials_networks_Gmail!!.setImageResource(R.drawable.ic_gmail_disable)
            link_socials_networks_Gmail!!.setOnClickListener { Toast.makeText(this, "Gmail n\'est pas installé", Toast.LENGTH_SHORT).show() }
        } else {
            link_socials_networks_Gmail!!.setOnClickListener { goToGmail() }
        }

        if (!listApp.contains("com.spotify.music")) {
            link_socials_networks_Spotify!!.setImageResource(R.drawable.ic_spotify_disable)
            link_socials_networks_Spotify!!.setOnClickListener { Toast.makeText(this, "Spotify n\'est pas installé", Toast.LENGTH_SHORT).show() }
        } else {
            link_socials_networks_Spotify!!.setOnClickListener { goToSpotify() }
        }

        if (!listApp.contains("org.telegram.messenger")) {
            link_socials_networks_Telegram!!.setImageResource(R.drawable.ic_telegram_disable)
            link_socials_networks_Telegram!!.setOnClickListener { Toast.makeText(this, "Telegram n\'est pas installé", Toast.LENGTH_SHORT).show() }
        } else {
            link_socials_networks_Telegram!!.setOnClickListener { goToTelegram() }
        }

        if (!listApp.contains("com.microsoft.office.outlook")) {
            link_socials_networks_Outlook!!.setImageResource(R.drawable.ic_outlook_disable)
            link_socials_networks_Outlook!!.setOnClickListener { Toast.makeText(this, "Outlook n\'est pas installé", Toast.LENGTH_SHORT).show() }
        } else {
            link_socials_networks_Outlook!!.setOnClickListener { goToOutlook() }
        }

        if (!listApp.contains("com.skype.raider")) {
            link_socials_networks_Skype!!.setImageResource(R.drawable.ic_skype_disable)
            link_socials_networks_Skype!!.setOnClickListener { Toast.makeText(this, "Skype n\'est pas installé", Toast.LENGTH_SHORT).show() }
        } else {
            link_socials_networks_Skype!!.setOnClickListener { goToSkype() }
        }

        if (!listApp.contains("com.linkedin.android")) {
            link_socials_networks_Linkedin!!.setImageResource(R.drawable.ic_linkedin_disable)
            link_socials_networks_Linkedin!!.setOnClickListener { Toast.makeText(this, "Linkedin n\'est pas installé", Toast.LENGTH_SHORT).show() }
        } else {
            link_socials_networks_Linkedin!!.setOnClickListener { goToLinkedin() }
        }

        if (!listApp.contains("com.twitter.android")) {
            link_socials_networks_Twitter!!.setImageResource(R.drawable.ic_twitter_disable)
            link_socials_networks_Twitter!!.setOnClickListener { Toast.makeText(this, "Twitter n\'est pas installé", Toast.LENGTH_SHORT).show() }
        } else {
            link_socials_networks_Twitter!!.setOnClickListener { goToTwitter() }
        }


        phone_log_IncomingCallButton!!.setOnClickListener {
            phoneCall(phone_log_PhoneNumberEditText!!.text.toString())
        }

        phone_log_SendMessage!!.setOnClickListener {
            if (phone_log_PhoneNumberEditText!!.text.isNotEmpty()) {
                val phone = phone_log_PhoneNumberEditText!!.text.toString()
                val i = Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", phone, null));
                startActivity(i)
            } else {
                Toast.makeText(this, "Enter a phone number please", Toast.LENGTH_SHORT).show()
            }
        }

        phone_log_ButtonOpen!!.setOnClickListener {

            phone_log_CallLayout!!.visibility = View.VISIBLE
            phone_log_KeyboardView!!.visibility = View.VISIBLE
            phone_log_ButtonOpen!!.visibility = View.GONE

            val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)
            val slideLeft = AnimationUtils.loadAnimation(this, R.anim.slide_left)

            phone_log_EditTextLayout!!.startAnimation(slideUp)
            phone_log_KeyboardView!!.startAnimation(slideUp)
            phone_log_CallLayout!!.startAnimation(slideUp)
            phone_log_ButtonOpen!!.startAnimation(slideLeft)
        }

        phone_log_ButtonClose!!.setOnClickListener {

            phone_log_CallLayout!!.visibility = View.GONE
            phone_log_KeyboardView!!.visibility = View.GONE
            phone_log_EditTextLayout!!.visibility = View.GONE
            phone_log_ButtonOpen!!.visibility = View.VISIBLE

            val slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down)
            val slideRight = AnimationUtils.loadAnimation(this, R.anim.slide_right)

            phone_log_EditTextLayout!!.startAnimation(slideDown)
            phone_log_KeyboardView!!.startAnimation(slideDown)
            phone_log_CallLayout!!.startAnimation(slideDown)
            phone_log_ButtonOpen!!.startAnimation(slideRight)
        }

        phone_log_ButtonAddContact!!.setOnClickListener {
            if (phone_log_PhoneNumberEditText!!.text.isNotEmpty()) {
                val intent = Intent(this@PhoneLogActivity, AddNewContactActivity::class.java)
                intent.putExtra("ContactPhoneNumber", phone_log_PhoneNumberEditText!!.text.toString())
                startActivity(intent)
            } else {
                Toast.makeText(this, "Enter a phone number please", Toast.LENGTH_SHORT).show()
            }
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

        phone_log_CallBackSpace!!.setOnClickListener {
            if (phone_log_PhoneNumberEditText!!.text.isNotEmpty()) {
                phone_log_PhoneNumberEditText!!.text.delete(
                        phone_log_PhoneNumberEditText!!.length() - 1,
                        phone_log_PhoneNumberEditText!!.length())
            }
        }

        phone_log_CallBackSpace!!.setOnLongClickListener {
            if (phone_log_PhoneNumberEditText!!.text.isNotEmpty()) {
                phone_log_PhoneNumberEditText!!.setText("")
            }
            true
        }

        //endregion

        //endregion
    }

    //region ========================================== Functions ===========================================

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
        val appIntent = Intent(Intent.ACTION_VIEW);
        appIntent.setClassName("com.google.android.gm", "com.google.android.gm.ConversationListActivityGmail");
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

    private fun goToSpotify() {
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("spotify://spotify"))
        try {
            startActivity(appIntent)
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://spotify.com/")))
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

    private fun showPhoneLog() {
//        phone_log_CallsListView!!.adapter = PhoneLogListAdapter(this@PhoneLogActivity, getListPhoneCalls())
    }

    private fun showListPhoneCalls() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(android.Manifest.permission.READ_CALL_LOG) != PERMISSION_GRANTED) {
            requestPermissions(Array<String?>(1) { android.Manifest.permission.READ_CALL_LOG }, PERMISSIONS_REQUEST_READ_CALL_LOG)
        } else {
            Toast.makeText(this, "Bla bla bla", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("Recycle")
    private fun getListPhoneCalls(): MutableList<PhoneLog> {
        checkPermission()
        val listOfPhoneCallDetails: MutableList<PhoneLog> = mutableListOf()

        val contentUri = Uri.parse("content://call_log/calls")
        val cursor = this.contentResolver.query(contentUri, null, null, null, null)

        cursor!!.moveToFirst()

        val num = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER))
        var name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME))
        val duration = cursor.getString(cursor.getColumnIndex(CallLog.Calls.DURATION))
        val type = Integer.parseInt(cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE)))
        var callType = ""
        val date = cursor.getString(cursor.getColumnIndex(CallLog.Calls.DATE))

        if (name == null) {
            name = num
        }

        when (type) {
            1 -> callType = "INCOMING_TYPE"
            2 -> callType = "OUTGOING_TYPE"
            3 -> callType = "MISSED_TYPE"
            4 -> callType = "VOICEMAIL_TYPE"
            5 -> callType = "REJECTED_TYPE"
            6 -> callType = "BLOCKED_TYPE"
        }

        while (cursor.moveToNext()) {
            listOfPhoneCallDetails.add(PhoneLog(num, name, duration, callType, date))
        }

        return listOfPhoneCallDetails
    }

    private fun checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_CALL_LOG) == PERMISSION_GRANTED) {
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MAKE_CALL_PERMISSION_REQUEST_CODE -> if (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED) {
                phone_log_IncomingCallButton!!.isEnabled = true
            }
            PERMISSIONS_REQUEST_READ_CALL_LOG -> if (grantResults[0] == PERMISSION_GRANTED) {
                showListPhoneCalls()
            } else {
                Toast.makeText(this, "Until you grant the permission, we canot display the names", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("ShowToast")
    private fun phoneCall(phoneNumberEntered: String) {
        if (!TextUtils.isEmpty(phoneNumberEntered)) {
            if (isValidPhone(phoneNumberEntered)) {
                val dial = "tel:$phoneNumberEntered"
                startActivity(Intent(Intent.ACTION_DIAL, Uri.parse(dial)))
            } else {
                Toast.makeText(this, "Enter a phone number valid", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Enter a phone number", Toast.LENGTH_SHORT).show()
        }
    }

    fun isValidPhone(phone: String): Boolean {
        val expression = "^(?:(?:\\+|00)33[\\s.-]{0,3}(?:\\(0\\)[\\s.-]{0,3})?|0)[1-9](?:(?:[\\s.-]?\\d{2}){4}|\\d{2}(?:[\\s.-]?\\d{3}){2})\$"
        val pattern = Pattern.compile(expression)
        val matcher = pattern.matcher(phone)
        return matcher.matches()
    }

    //endregion
}