package com.example.knocker.controller.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.knocker.R
import com.example.knocker.controller.activity.group.GroupManagerActivity
import com.google.android.material.navigation.NavigationView

/**
 * La Classe qui permet d'afficher les informations,la FAQ, le contact et les conditions de knocker
 * @author Kenzy Suon
 */
@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class HelpActivity : AppCompatActivity() {

    var help_activity_FAQ: ConstraintLayout? = null
    var help_activity_ContactUs: ConstraintLayout? = null
    var help_activity_Terms: ConstraintLayout? = null
    var help_activity_Infos: ConstraintLayout? = null
    var help_activity_DrawerLayout: DrawerLayout? = null

    @SuppressLint("IntentReset")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedThemePreferences = getSharedPreferences("Knocker_Theme", Context.MODE_PRIVATE)
        if (sharedThemePreferences.getBoolean("darkTheme", false)) {
            setTheme(R.style.AppThemeDark)
        } else {
            setTheme(R.style.AppTheme)
        }
        setContentView(R.layout.activity_help)

        //region ========================================== Toolbar =========================================

        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar.setHomeAsUpIndicator(R.drawable.ic_open_drawer)

        //endregion

        //region ======================================= FindViewById =======================================

        help_activity_FAQ = findViewById(R.id.help_activity_tutorial)
        help_activity_ContactUs = findViewById(R.id.help_activity_contact_us_id)
        help_activity_Terms = findViewById(R.id.help_activity_terms_id)
        help_activity_Infos = findViewById(R.id.help_activity_infos_id)

        //endregion

        //region ======================================= DrawerLayout =======================================

        // Drawerlayout
        help_activity_DrawerLayout = findViewById(R.id.drawer_layout)

        val navigationView = findViewById<NavigationView>(R.id.phone_log_nav_view)
        val menu = navigationView.menu
        val nav_item = menu.findItem(R.id.nav_notif_config)
        nav_item.isChecked = true

        navigationView!!.menu.getItem(3).isChecked = true

        navigationView.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            help_activity_DrawerLayout!!.closeDrawers()

            when (menuItem.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this@HelpActivity, MainActivity::class.java))
                }
                R.id.nav_groups -> startActivity(Intent(this@HelpActivity, GroupManagerActivity::class.java))
                R.id.nav_informations -> startActivity(Intent(this@HelpActivity, EditInformationsActivity::class.java))
                R.id.nav_notif_config -> startActivity(Intent(this@HelpActivity, ManageNotificationActivity::class.java))
                R.id.nav_screen_config -> startActivity(Intent(this@HelpActivity, ManageMyScreenActivity::class.java))
                R.id.nav_data_access -> {
                }
                R.id.nav_knockons -> startActivity(Intent(this@HelpActivity, ManageKnockonsActivity::class.java))
                R.id.nav_statistics -> {
                }
                R.id.nav_help -> startActivity(Intent(this@HelpActivity, HelpActivity::class.java))
            }

            val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
            drawer.closeDrawer(GravityCompat.START)
            true
        }

        //endregion

        //region ==================================== SetOnClickListener ====================================

        val onClick = View.OnClickListener {
            if (it.id == help_activity_FAQ!!.id) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.yellowtwigs.com/tutorial")))
            }
            if (it.id == help_activity_ContactUs!!.id) {
                val intent = Intent(Intent.ACTION_SEND)
                intent.data = Uri.parse("mailto:")
                intent.type = "text/html";
                intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.contact_mail)))
                intent.putExtra(Intent.EXTRA_SUBJECT, "")
                intent.putExtra(Intent.EXTRA_TEXT, "")
                println("intent " + intent.extras.toString())
                startActivity(Intent.createChooser(intent, getString(R.string.help_contact_us_intent)))
            }
            if (it.id == help_activity_Terms!!.id) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.yellowtwigs.com/contrat-de-licence-utilisateur-fina")))
            }
            if (it.id == help_activity_Infos!!.id) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.yellowtwigs.com")))
            }
        }
        help_activity_ContactUs!!.setOnClickListener(onClick)
        help_activity_Infos!!.setOnClickListener(onClick)
        help_activity_FAQ!!.setOnClickListener(onClick)
        help_activity_Terms!!.setOnClickListener(onClick)

        //endregion
    }

    // Intent to return to the MainActivity
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                help_activity_DrawerLayout!!.openDrawer(GravityCompat.START)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
