package com.yellowtwigs.knockin.ui.main

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.navigation.NavigationView
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.ActivityMainBinding
import com.yellowtwigs.knockin.ui.HelpActivity
import com.yellowtwigs.knockin.ui.contacts.Main2Activity
import com.yellowtwigs.knockin.ui.in_app.PremiumActivity
import com.yellowtwigs.knockin.ui.settings.ManageMyScreenActivity
import com.yellowtwigs.knockin.ui.settings.ManageNotificationActivity
import com.yellowtwigs.knockin.ui.settings.SettingsActivity
import com.yellowtwigs.knockin.ui.teleworking.TeleworkingActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    lateinit var navController: NavController

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

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        setupDrawerLayout(binding)
    }

    private fun setupDrawerLayout(binding: ActivityMainBinding) {
        binding.drawerLayout.apply {
            val menu = binding.navView.menu
            val navItem = menu.findItem(R.id.nav_home)
            navItem.isChecked = true

            binding.navView.setNavigationItemSelectedListener { menuItem ->
                closeDrawers()
                when (menuItem.itemId) {
                    R.id.nav_home -> {}
//                    R.id.nav_notif_config -> startActivity(
//                        Intent(
//                            this@Main2Activity,
//                            ManageNotificationActivity::class.java
//                        )
//                    )
//                    R.id.navigation_teleworking -> startActivity(
//                        Intent(
//                            this@Main2Activity,
//                            TeleworkingActivity::class.java
//                        )
//                    )
//                    R.id.nav_settings -> startActivity(
//                        Intent(
//                            this@Main2Activity,
//                            SettingsActivity::class.java
//                        )
//                    )
//                    R.id.nav_in_app -> startActivity(
//                        Intent(
//                            this@Main2Activity,
//                            PremiumActivity::class.java
//                        )
//                    )
//                    R.id.nav_manage_screen -> startActivity(
//                        Intent(
//                            this@Main2Activity,
//                            ManageMyScreenActivity::class.java
//                        )
//                    )
//                    R.id.nav_help -> startActivity(Intent(this@Main2Activity, HelpActivity::class.java))
                }

                true
            }
        }
    }
}