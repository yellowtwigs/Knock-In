package com.yellowtwigs.knockin.ui.cockpit

import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.TranslateAnimation
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.ActivityCockpitBinding
import com.yellowtwigs.knockin.ui.HelpActivity
import com.yellowtwigs.knockin.ui.add_edit_contact.add.AddNewContactActivity
import com.yellowtwigs.knockin.ui.contacts.list.ContactsListActivity
import com.yellowtwigs.knockin.ui.groups.list.GroupsListActivity
import com.yellowtwigs.knockin.ui.in_app.PremiumActivity
import com.yellowtwigs.knockin.ui.notifications.history.NotificationsHistoryActivity
import com.yellowtwigs.knockin.ui.notifications.settings.NotificationsSettingsActivity
import com.yellowtwigs.knockin.ui.settings.ManageMyScreenActivity
import com.yellowtwigs.knockin.ui.teleworking.TeleworkingActivity
import com.yellowtwigs.knockin.utils.EveryActivityUtils
import com.yellowtwigs.knockin.utils.EveryActivityUtils.checkTheme
import com.yellowtwigs.knockin.utils.EveryActivityUtils.getAppOnPhone
import com.yellowtwigs.knockin.utils.EveryActivityUtils.hideKeyboard
import com.yellowtwigs.knockin.utils.EveryActivityUtils.setupTeleworkingItem
import com.yellowtwigs.knockin.utils.NotificationsGesture
import com.yellowtwigs.knockin.utils.NotificationsGesture.phoneCall

class CockpitActivity : AppCompatActivity() {

    //region ========================================== Var or Val ==========================================

    private val MAKE_CALL_PERMISSION_REQUEST_CODE = 1

    //endregion

    private lateinit var binding: ActivityCockpitBinding
    private lateinit var listApp: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkTheme(this)
        hideKeyboard(this)

        binding = ActivityCockpitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        listApp = getAppOnPhone(this)

        setupBottomNavigationView()
        setupToolbar()
        setupDrawerLayout()
        setupRecyclerView()

        binding.apply {
            editTextLayout.isVisible = phoneNumberEditText.text?.isEmpty() == true

            //region ========================================== Listener ========================================

            callButton.setOnClickListener {
                phoneCall(
                    this@CockpitActivity,
                    phoneNumberEditText.text.toString(),
                    MAKE_CALL_PERMISSION_REQUEST_CODE
                )
            }

            sendMessage.setOnClickListener {
                if (phoneNumberEditText.text?.isNotEmpty() == true) {
                    val phone = phoneNumberEditText.text.toString()
                    val i = Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", phone, null))
                    startActivity(i)
                } else {
                    Toast.makeText(
                        this@CockpitActivity,
                        R.string.cockpit_toast_phone_number_empty,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }

            addContact.setOnClickListener {
                if (phoneNumberEditText.text?.isNotEmpty() == true) {
                    startActivity(
                        Intent(
                            this@CockpitActivity,
                            AddNewContactActivity::class.java
                        ).putExtra(
                            "ContactPhoneNumber",
                            phoneNumberEditText.text.toString()
                        )
                    )
                } else {
                    Toast.makeText(
                        this@CockpitActivity,
                        R.string.cockpit_toast_phone_number_empty,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }

            openFab.setOnClickListener {
                val slideLeft =
                    AnimationUtils.loadAnimation(this@CockpitActivity, R.anim.slide_left)

                slideUp(editTextLayout)
                slideUp(tableLayout)
                slideUp(callLayout)
                openFab.startAnimation(slideLeft)

                callLayout.visibility = View.VISIBLE
                tableLayout.visibility = View.VISIBLE
                openFab.visibility = View.GONE
            }

            closeFab.setOnClickListener {
                val slideRight =
                    AnimationUtils.loadAnimation(this@CockpitActivity, R.anim.slide_right)
                val slideDown =
                    AnimationUtils.loadAnimation(this@CockpitActivity, R.anim.slide_down)

                editTextLayout.startAnimation(slideDown)
                tableLayout.startAnimation(slideDown)
                callLayout.startAnimation(slideDown)
                openFab.startAnimation(slideRight)

                callLayout.visibility = View.GONE
                tableLayout.visibility = View.GONE
                editTextLayout.visibility = View.GONE
                openFab.visibility = View.VISIBLE
            }

            //region ========================================== Keyboard ========================================

            callKeyboardNumberClick(callKeyboard1, "1")
            callKeyboardNumberClick(callKeyboard2, "2")
            callKeyboardNumberClick(callKeyboard3, "3")
            callKeyboardNumberClick(callKeyboard4, "4")
            callKeyboardNumberClick(callKeyboard5, "5")
            callKeyboardNumberClick(callKeyboard6, "6")
            callKeyboardNumberClick(callKeyboard7, "7")
            callKeyboardNumberClick(callKeyboard8, "8")
            callKeyboardNumberClick(callKeyboard9, "9")
            callKeyboardNumberClick(callKeyboardStar, "*")
            callKeyboardNumberClick(callKeyboard0, "0")
            callKeyboardNumberClick(callKeyboardSharp, "#")

            backSpace.setOnClickListener {
                if (phoneNumberEditText.text?.isNotEmpty() == true) {
                    phoneNumberEditText.text?.delete(
                        phoneNumberEditText.length() - 1,
                        phoneNumberEditText.length()
                    )
                }
            }
            //endregion

            //endregion
        }
    }

    //region =========================================== TOOLBAR ============================================

    private fun setupToolbar() {
        binding.apply {
            openDrawer.setOnClickListener {
                drawerLayout.openDrawer(GravityCompat.START)
                hideKeyboard(this@CockpitActivity)
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

        setupTeleworkingItem(binding.navView, this)

        binding.navView.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            binding.drawerLayout.closeDrawers()

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
        val cockpitViewStateList = arrayListOf<CockpitViewState>()

        if (listApp.contains(NotificationsGesture.FACEBOOK_PACKAGE)) {
            cockpitViewStateList.add(
                CockpitViewState(
                    R.drawable.ic_facebook_selector,
                    NotificationsGesture.FACEBOOK_PACKAGE
                )
            )
        } else {
            cockpitViewStateList.add(
                CockpitViewState(
                    R.drawable.ic_facebook_disable,
                    NotificationsGesture.FACEBOOK_PACKAGE
                )
            )
        }

        if (listApp.contains(NotificationsGesture.MESSENGER_PACKAGE)) {
            cockpitViewStateList.add(
                CockpitViewState(
                    R.drawable.ic_messenger_selector,
                    NotificationsGesture.MESSENGER_PACKAGE
                )
            )
        } else {
            cockpitViewStateList.add(
                CockpitViewState(
                    R.drawable.ic_messenger_disable,
                    NotificationsGesture.MESSENGER_PACKAGE
                )
            )
        }

        if (listApp.contains(NotificationsGesture.WHATSAPP_PACKAGE)) {
            cockpitViewStateList.add(
                CockpitViewState(
                    R.drawable.ic_whatsapp,
                    NotificationsGesture.WHATSAPP_PACKAGE
                )
            )
        } else {
            cockpitViewStateList.add(
                CockpitViewState(
                    R.drawable.ic_whatsapp_disable,
                    NotificationsGesture.WHATSAPP_PACKAGE
                )
            )
        }

        if (listApp.contains(NotificationsGesture.GMAIL_PACKAGE)) {
            cockpitViewStateList.add(
                CockpitViewState(
                    R.drawable.ic_gmail,
                    NotificationsGesture.GMAIL_PACKAGE
                )
            )
        } else {
            cockpitViewStateList.add(
                CockpitViewState(
                    R.drawable.ic_gmail_disable,
                    NotificationsGesture.GMAIL_PACKAGE
                )
            )
        }

        if (listApp.contains(NotificationsGesture.OUTLOOK_PACKAGE)) {
            cockpitViewStateList.add(
                CockpitViewState(
                    R.drawable.ic_outlook,
                    NotificationsGesture.OUTLOOK_PACKAGE
                )
            )
        } else {
            cockpitViewStateList.add(
                CockpitViewState(
                    R.drawable.ic_outlook_disable,
                    NotificationsGesture.OUTLOOK_PACKAGE
                )
            )
        }

        if (listApp.contains(NotificationsGesture.SIGNAL_PACKAGE)) {
            cockpitViewStateList.add(
                CockpitViewState(
                    R.drawable.ic_signal,
                    NotificationsGesture.SIGNAL_PACKAGE
                )
            )
        } else {
            cockpitViewStateList.add(
                CockpitViewState(
                    R.drawable.ic_signal_disable,
                    NotificationsGesture.SIGNAL_PACKAGE
                )
            )
        }

        if (listApp.contains(NotificationsGesture.LINKEDIN_PACKAGE)) {
            cockpitViewStateList.add(
                CockpitViewState(
                    R.drawable.ic_linkedin,
                    NotificationsGesture.LINKEDIN_PACKAGE
                )
            )
        } else {
            cockpitViewStateList.add(
                CockpitViewState(
                    R.drawable.ic_linkedin_disable,
                    NotificationsGesture.LINKEDIN_PACKAGE
                )
            )
        }

        if (listApp.contains(NotificationsGesture.SKYPE_PACKAGE)) {
            cockpitViewStateList.add(
                CockpitViewState(
                    R.drawable.ic_skype,
                    NotificationsGesture.SKYPE_PACKAGE
                )
            )
        } else {
            cockpitViewStateList.add(
                CockpitViewState(
                    R.drawable.ic_skype_disable,
                    NotificationsGesture.SKYPE_PACKAGE
                )
            )
        }

        if (listApp.contains(NotificationsGesture.TELEGRAM_PACKAGE)) {
            cockpitViewStateList.add(
                CockpitViewState(
                    R.drawable.ic_telegram,
                    NotificationsGesture.TELEGRAM_PACKAGE
                )
            )
        } else {
            cockpitViewStateList.add(
                CockpitViewState(
                    R.drawable.ic_telegram_disable,
                    NotificationsGesture.TELEGRAM_PACKAGE
                )
            )
        }

        if (listApp.contains(NotificationsGesture.INSTAGRAM_PACKAGE)) {
            cockpitViewStateList.add(
                CockpitViewState(
                    R.drawable.ic_instagram,
                    NotificationsGesture.INSTAGRAM_PACKAGE
                )
            )
        } else {
            cockpitViewStateList.add(
                CockpitViewState(
                    R.drawable.ic_instagram_disable,
                    NotificationsGesture.INSTAGRAM_PACKAGE
                )
            )
        }

        if (listApp.contains(NotificationsGesture.TWITTER_PACKAGE)) {
            cockpitViewStateList.add(
                CockpitViewState(
                    R.drawable.ic_twitter,
                    NotificationsGesture.TWITTER_PACKAGE
                )
            )
        } else {
            cockpitViewStateList.add(
                CockpitViewState(
                    R.drawable.ic_twitter_disable,
                    NotificationsGesture.TWITTER_PACKAGE
                )
            )
        }

        if (listApp.contains(NotificationsGesture.SNAPCHAT_PACKAGE)) {
            cockpitViewStateList.add(
                CockpitViewState(
                    R.drawable.ic_snapchat,
                    NotificationsGesture.SNAPCHAT_PACKAGE
                )
            )
        } else {
            cockpitViewStateList.add(
                CockpitViewState(
                    R.drawable.ic_snapchat_disable,
                    NotificationsGesture.SNAPCHAT_PACKAGE
                )
            )
        }

        binding.recyclerView.apply {
            val cockpitListAdapter = CockpitListAdapter(this@CockpitActivity)
            cockpitListAdapter.submitList(cockpitViewStateList)
            adapter = cockpitListAdapter
            layoutManager = GridLayoutManager(context, 4)
        }
    }

    private fun callKeyboardNumberClick(callKeyboard: RelativeLayout, value: String) {
        callKeyboard.setOnClickListener {
            binding.apply {
                editTextLayout.visibility = View.VISIBLE
                phoneNumberEditText.setText(phoneNumberEditText.text.toString() + value)
            }
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

    override fun onBackPressed() {
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            MAKE_CALL_PERMISSION_REQUEST_CODE -> if (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED) {
                phoneCall(
                    this@CockpitActivity,
                    binding.phoneNumberEditText.text.toString(),
                    MAKE_CALL_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    //endregion
}