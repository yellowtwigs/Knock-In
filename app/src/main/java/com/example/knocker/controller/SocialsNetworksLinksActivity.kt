package com.example.knocker.controller

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils

import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout

import com.example.knocker.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

/**
 * La Classe qui permet d'éditer un contact choisi
 * @author Kenzy Suon
 */
class SocialsNetworksLinksActivity : AppCompatActivity() {

    //region ========================================== Var or Val ==========================================

    // Show on the Main Layout
    private var drawerLayout: DrawerLayout? = null
    private var main_BottomNavigationView: BottomNavigationView? = null

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


    private var my_knocker: RelativeLayout? = null

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_phone_book -> {
                startActivity(Intent(this@SocialsNetworksLinksActivity, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_groups -> {
            }
            R.id.navigation_notifcations -> {
                startActivity(Intent(this@SocialsNetworksLinksActivity, NotificationHistoryActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
            }
            R.id.navigation_socials_networks -> {
            }
            R.id.navigation_phone_keyboard -> {
                startActivity(Intent(this@SocialsNetworksLinksActivity, PhoneLogActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_socials_networks_links)

        //region ======================================= FindViewById =======================================

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


        main_BottomNavigationView = findViewById(R.id.navigation)
        main_BottomNavigationView!!.menu.getItem(3).isChecked = true

        //endregion

        //region ========================================= Listener =========================================

        main_BottomNavigationView!!.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        link_socials_networks_Messenger!!.setOnClickListener { gotToFacebookPage("") }

        link_socials_networks_Instagram!!.setOnClickListener {
            goToInstagramPage()
        }

        link_socials_networks_Whatsapp!!.setOnClickListener {
            goToWhatsapp()
        }

        //endregion

        //region ========================================== Toolbar =========================================

        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar.setHomeAsUpIndicator(R.drawable.ic_open_drawer)
        actionbar.title = "Socials Networks"

        //endregion

        //region ======================================= DrawerLayout =======================================

        // Drawerlayout
        drawerLayout = findViewById(R.id.drawer_layout)

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        val headerView = navigationView.getHeaderView(0);
        my_knocker = headerView.findViewById(R.id.my_knocker)

        my_knocker!!.setOnClickListener {
        }
        navigationView.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            drawerLayout!!.closeDrawers()

            val id = menuItem.itemId

            when (id) {
                R.id.nav_informations -> startActivity(Intent(this@SocialsNetworksLinksActivity, EditInformationsActivity::class.java))
                R.id.nav_notif_config -> startActivity(Intent(this@SocialsNetworksLinksActivity, ManageNotificationActivity::class.java))
                R.id.nav_screen_config -> startActivity(Intent(this@SocialsNetworksLinksActivity, ManageMyScreenActivity::class.java))
                R.id.nav_data_access -> {
                }
                R.id.nav_knockons -> startActivity(Intent(this@SocialsNetworksLinksActivity, ManageKnockonsActivity::class.java))
                R.id.nav_statistics -> {
                }
                R.id.nav_help -> startActivity(Intent(this@SocialsNetworksLinksActivity, HelpActivity::class.java))
            }

            val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
            drawer.closeDrawer(GravityCompat.START)
            true
        }

        //endregion

    }
    //region ========================================== Functions =========================================

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                drawerLayout!!.openDrawer(GravityCompat.START)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
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
        val uri = Uri.parse("smsto: " + "12345")
        val i = Intent(Intent.ACTION_SENDTO, uri)
        i.setPackage("com.whatsapp")
        startActivity(i)
    }

    private fun goToInstagramPage() {
        val uri = Uri.parse("http://instagram.com/_u/therock/")
        val likeIng = Intent(Intent.ACTION_VIEW, uri)

        likeIng.setPackage("com.instagram.android")

        try {
            startActivity(likeIng)
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://instagram.com/")))
        }

    }

    //endregion
}