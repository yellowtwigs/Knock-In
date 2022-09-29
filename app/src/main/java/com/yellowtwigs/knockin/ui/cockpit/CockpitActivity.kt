package com.yellowtwigs.knockin.ui.cockpit

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
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
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.ActivityCockpitBinding
import com.yellowtwigs.knockin.ui.HelpActivity
import com.yellowtwigs.knockin.ui.contacts.list.ContactsListActivity
import com.yellowtwigs.knockin.ui.groups.list.GroupsListActivity
import com.yellowtwigs.knockin.ui.in_app.PremiumActivity
import com.yellowtwigs.knockin.ui.notifications.history.NotificationsHistoryActivity
import com.yellowtwigs.knockin.ui.settings.ManageMyScreenActivity
import com.yellowtwigs.knockin.ui.notifications.settings.NotificationsSettingsActivity
import com.yellowtwigs.knockin.ui.teleworking.TeleworkingActivity
import com.yellowtwigs.knockin.utils.ContactGesture.goToOutlook
import com.yellowtwigs.knockin.utils.ContactGesture.goToSignal
import com.yellowtwigs.knockin.utils.ContactGesture.goToTelegram

class CockpitActivity : AppCompatActivity() {

    //region ========================================== Var or Val ==========================================

    private val MAKE_CALL_PERMISSION_REQUEST_CODE = 1

    private var numberForPermission = ""

    private var cockpit_IncomingCallButton: FloatingActionButton? = null
    private var cockpit_SendMessage: ImageView? = null

    private var cockpit_ButtonOpen: FloatingActionButton? = null
    private var cockpit_CallLayout: ConstraintLayout? = null
    private var cockpit_KeyboardView: TableLayout? = null
    private var cockpit_EditTextLayout: ConstraintLayout? = null
    private var cockpit_PhoneNumberEditText: AppCompatEditText? = null
    private var cockpit_ButtonClose: ImageView? = null

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

    private var cockpit_CallBackSpace: ImageView? = null
    private var cockpit_ButtonAddContact: ImageView? = null

    //endregion

    private lateinit var binding: ActivityCockpitBinding
    private lateinit var listApp: ArrayList<String>

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

        binding = ActivityCockpitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        hideKeyboard()

        listApp = getAppOnPhone()

        setupBottomNavigationView()
        setupToolbar()
        setupDrawerLayout()
        setupRecyclerView()

        //region ======================================= FindViewById =======================================

        cockpit_IncomingCallButton = findViewById(R.id.cockpit_incoming_call_button)

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

        cockpit_CallBackSpace = findViewById(R.id.cockpit_call_back_space)
        cockpit_ButtonAddContact = findViewById(R.id.cockpit_button_add_contact)

        //endregion

        if (cockpit_PhoneNumberEditText?.text?.isEmpty() == true) {
            cockpit_EditTextLayout?.visibility = View.GONE
        } else {
            cockpit_EditTextLayout?.visibility = View.VISIBLE
        }


        //region ========================================== Listener ========================================

        cockpit_IncomingCallButton?.setOnClickListener {
//            phoneCall(cockpit_PhoneNumberEditText?.text.toString())
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
//                startActivity(
//                    Intent(
//                        this@CockpitActivity,
//                        AddNewContactActivity::class.java
//                    ).putExtra("ContactPhoneNumber", cockpit_PhoneNumberEditText!!.text.toString())
//                )
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

    //region =========================================== TOOLBAR ============================================

    private fun setupToolbar() {
        binding.apply {
            openDrawer.setOnClickListener {
                drawerLayout.openDrawer(GravityCompat.START)
                hideKeyboard()
            }
        }

        binding.help.setOnClickListener {
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
    }

    //endregion

    //region ========================================= DRAWER LAYOUT ========================================

    private fun setupDrawerLayout() {
        val menu = binding.navView.menu
        menu.findItem(R.id.nav_home).isChecked = true

        binding.navView.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            binding.drawerLayout.closeDrawers()

            val itemLayout = findViewById<ConstraintLayout>(R.id.teleworking_item)
            val itemText = findViewById<AppCompatTextView>(R.id.teleworking_item_text)

            itemText.text =
                "${getString(R.string.teleworking)} ${getString(R.string.left_drawer_settings)}"

            itemLayout.setOnClickListener {
                startActivity(
                    Intent(
                        this@CockpitActivity,
                        TeleworkingActivity::class.java
                    )
                )
            }

            when (menuItem.itemId) {
                R.id.nav_home -> {
                    startActivity(
                        Intent(
                            this@CockpitActivity,
                            ContactsListActivity::class.java
                        )
                    )
                }
                R.id.nav_notifications -> startActivity(
                    Intent(
                        this@CockpitActivity,
                        NotificationsSettingsActivity::class.java
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

            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    //endregion

    //region =========================================== SETUP UI ===========================================

    private fun setupBottomNavigationView() {
        binding.navigation.apply {
            menu.getItem(3).isChecked = true
            setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.navigation_contacts -> {
                        startActivity(
                            Intent(
                                this@CockpitActivity,
                                ContactsListActivity::class.java
                            ).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                        )
                        return@OnNavigationItemSelectedListener true
                    }
                    R.id.navigation_groups -> {
                        startActivity(
                            Intent(
                                this@CockpitActivity,
                                GroupsListActivity::class.java
                            ).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                        )
                        return@OnNavigationItemSelectedListener true
                    }
                    R.id.navigation_notifcations -> {
                        startActivity(
                            Intent(
                                this@CockpitActivity,
                                NotificationsHistoryActivity::class.java
                            ).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                        )
                        return@OnNavigationItemSelectedListener true
                    }
                }
                false
            })
        }
    }

    private fun setupRecyclerView() {
        val listOfApps = arrayListOf<Int>()

        if (!listApp.contains("com.facebook.katana")) {
            listOfApps.add(R.drawable.ic_facebook_disable)
//            link_socials_networks_Facebook?.setOnClickListener {
//                Toast.makeText(
//                    this,
//                    R.string.cockpit_toast_facebook_not_install,
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
        } else {
            listOfApps.add(R.drawable.ic_facebook_selector)
//            link_socials_networks_Facebook?.setOnClickListener { goToFacebook() }
        }

        if (!listApp.contains("com.facebook.orca")) {
            listOfApps.add(R.drawable.ic_messenger_disable)
//            link_socials_networks_Messenger?.setOnClickListener {
//                Toast.makeText(
//                    this,
//                    R.string.cockpit_toast_messenger_not_install,
//                    Toast.LENGTH_SHORT
//                ).show()
        } else {
            listOfApps.add(R.drawable.ic_messenger_selector)
//            link_socials_networks_Messenger?.setOnClickListener { openMessenger("") }
        }

        if (!listApp.contains("com.instagram.android")) {
            listOfApps.add(R.drawable.ic_instagram_disable)
//            link_socials_networks_Instagram?.setOnClickListener {
//                Toast.makeText(
//                    this,
//                    R.string.cockpit_toast_instagram_not_install,
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
        } else {
            listOfApps.add(R.drawable.ic_instagram)
        }

        if (!listApp.contains("com.whatsapp")) {
            listOfApps.add(R.drawable.ic_whatsapp_disable)
//            link_socials_networks_Whatsapp?.setOnClickListener {
//                Toast.makeText(
//                    this,
//                    R.string.cockpit_toast_whatsapp_not_install,
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
        } else {
            listOfApps.add(R.drawable.ic_whatsapp)
//            link_socials_networks_Whatsapp?.setOnClickListener {
//                goToWhatsapp() }
        }

        if (!listApp.contains("com.google.android.gm")) {
            listOfApps.add(R.drawable.ic_gmail_disable)
//            link_socials_networks_Gmail?.setOnClickListener {
//                Toast.makeText(
//                    this,
//                    R.string.cockpit_toast_gmail_not_install,
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
        } else {
            listOfApps.add(R.drawable.ic_gmail)
//            link_socials_networks_Gmail?.setOnClickListener { goToGmail() }
        }

        if (!listApp.contains("com.snapchat.android")) {
            listOfApps.add(R.drawable.ic_snapchat_disable)
//            link_socials_networks_Snapchat?.setOnClickListener {
//                Toast.makeText(
//                    this,
//                    R.string.cockpit_toast_snapchat_not_install,
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
        } else {
            listOfApps.add(R.drawable.ic_snapchat)
//            link_socials_networks_Snapchat?.setOnClickListener { goToSnapchat() }
        }

        if (!listApp.contains("org.telegram.messenger")) {
            listOfApps.add(R.drawable.ic_telegram_disable)
//            link_socials_networks_Telegram?.setOnClickListener {
//                Toast.makeText(
//                    this,
//                    R.string.cockpit_toast_telegram_not_install,
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
        } else {
            listOfApps.add(R.drawable.ic_telegram)

//            link_socials_networks_Telegram?.setOnClickListener {
//                goToTelegram(this)
//            }
        }

        if (!listApp.contains("com.microsoft.office.outlook")) {
            listOfApps.add(R.drawable.ic_outlook_disable)
//            link_socials_networks_Outlook?.setOnClickListener {
//                Toast.makeText(
//                    this,
//                    R.string.cockpit_toast_outlook_not_install,
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
        } else {
            listOfApps.add(R.drawable.ic_outlook)
//            link_socials_networks_Outlook?.setOnClickListener { goToOutlook(this@CockpitActivity) }
        }

        if (!listApp.contains("com.skype.raider")) {
            listOfApps.add(R.drawable.ic_skype_disable)
//            link_socials_networks_Skype?.setOnClickListener {
//                Toast.makeText(
//                    this,
//                    R.string.cockpit_toast_skype_not_install,
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
        } else {
            listOfApps.add(R.drawable.ic_skype)
//            link_socials_networks_Skype?.setOnClickListener { goToSkype() }
        }

        if (!listApp.contains("com.linkedin.android")) {
            listOfApps.add(R.drawable.ic_linkedin_disable)
//            link_socials_networks_Linkedin?.setOnClickListener {
//                Toast.makeText(
//                    this,
//                    R.string.cockpit_toast_linkedin_not_install,
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
        } else {
            listOfApps.add(R.drawable.ic_linkedin)
//            link_socials_networks_Linkedin?.setOnClickListener { goToLinkedin() }
        }

        if (!listApp.contains("com.twitter.android")) {
            listOfApps.add(R.drawable.ic_twitter_disable)
//            link_socials_networks_Twitter?.setOnClickListener {
//                Toast.makeText(
//                    this,
//                    R.string.cockpit_toast_twitter_not_install,
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
        } else {
            listOfApps.add(R.drawable.ic_twitter)
//            link_socials_networks_Twitter?.setOnClickListener { goToTwitter() }
        }

        if (!listApp.contains("org.thoughtcrime.securesms")) {
            listOfApps.add(R.drawable.ic_signal_disable)
        } else {
            listOfApps.add(R.drawable.ic_signal)
//            signalIcon?.setOnClickListener { goToSignal(this) }
        }

        binding.recyclerView.apply {
            val cockpitListAdapter = CockpitListAdapter()
            cockpitListAdapter.submitList(listOfApps)
            adapter = cockpitListAdapter
            layoutManager = GridLayoutManager(context, 4)
        }
    }

    //endregion

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

    //endregion
}