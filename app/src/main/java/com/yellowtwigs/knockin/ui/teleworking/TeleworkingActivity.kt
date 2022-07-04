package com.yellowtwigs.knockin.ui.teleworking

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.SwitchCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.ActivityTeleworkingBinding
import com.yellowtwigs.knockin.ui.CockpitActivity
import com.yellowtwigs.knockin.ui.contacts.MainActivity
import com.yellowtwigs.knockin.ui.group.GroupManagerActivity
import com.yellowtwigs.knockin.ui.notifications.history.NotificationHistoryActivity

class TeleworkingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeleworkingBinding

    private val mOnNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_contacts -> {
                    startActivity(
                        Intent(
                            this@TeleworkingActivity,
                            MainActivity::class.java
                        ).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    )
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_groups -> {
                    startActivity(
                        Intent(
                            this@TeleworkingActivity,
                            GroupManagerActivity::class.java
                        ).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    )
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_notifcations -> {
                    startActivity(
                        Intent(
                            this@TeleworkingActivity,
                            NotificationHistoryActivity::class.java
                        ).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    )
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_cockpit -> {
                    startActivity(
                        Intent(this@TeleworkingActivity, CockpitActivity::class.java).addFlags(
                            Intent.FLAG_ACTIVITY_NO_ANIMATION
                        )
                    )
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }

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

        binding = ActivityTeleworkingBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //endregion

        //region ================================ Call Popup from LeftDrawer ================================

        val sharedPreferencePopup = getSharedPreferences("Phone_call", Context.MODE_PRIVATE)
        val settings_CallPopupSwitch = findViewById<SwitchCompat>(R.id.settings_call_popup_switch)

        if (sharedPreferencePopup.getBoolean("popup", true)) {
            settings_CallPopupSwitch?.isChecked = true
        }

        //endregion

        binding.apply {
            navigation.menu.getItem(2)?.isChecked = true
            navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        }

        if (sharedThemePreferences.getBoolean("darkTheme", false)) {
//            settings_left_drawer_ThemeSwitch?.isChecked = true
        }

    }
}