package com.yellowtwigs.knockin.controller.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import com.facebook.*
import com.yellowtwigs.knockin.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.facebook.login.widget.LoginButton
import java.util.*
import com.facebook.login.LoginResult
import com.facebook.login.LoginManager
import com.facebook.ProfileTracker


open class MessengerActivity : AppCompatActivity() {

    //region ========================================== Var or Val ==========================================

    private var messengerDrawerLayout: DrawerLayout? = null

    private var messenger_ComposeMessageFloatingButton: FloatingActionButton? = null

    private var messenger_RecyclerView: RecyclerView? = null

    private var facebook_LoginButton: LoginButton? = null
    private val EMAIL = "email"
    private var callbackManager: CallbackManager? = null

    private var profileTracker : ProfileTracker? = null

    //endregion

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

        setContentView(R.layout.activity_messenger)

        //region ======================================= FindViewById =======================================

        messenger_ComposeMessageFloatingButton = findViewById(R.id.messenger_compose_message)
        messenger_RecyclerView = findViewById(R.id.messenger_recycler_view)

        facebook_LoginButton = findViewById(R.id.facebook_login_button)
        facebook_LoginButton!!.setReadPermissions(listOf(EMAIL))

        //endregion

        //region ========================================= Toolbar ==========================================

        val toolbar = findViewById<Toolbar>(R.id.messenger_toolbar)
        setSupportActionBar(toolbar)
        toolbar.overflowIcon = getDrawable(R.drawable.ic_toolbar_menu)
        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar.setHomeAsUpIndicator(R.drawable.ic_open_drawer)
        actionbar.title = ""
        actionbar.setBackgroundDrawable(ColorDrawable(Color.parseColor("#ffffff")))

        //endregion

        //region ====================================== Drawer Layout =======================================

        messengerDrawerLayout = findViewById(R.id.messenger_drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        val menu = navigationView.menu
        val navItem = menu.findItem(R.id.nav_home)
        navItem.isChecked = true
        val navSyncContact = menu.findItem(R.id.nav_sync_contact)
        navSyncContact.isVisible = true

        navigationView.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            messengerDrawerLayout!!.closeDrawers()

            when (menuItem.itemId) {
                R.id.nav_home -> startActivity(Intent(this@MessengerActivity, MessengerActivity::class.java))
                R.id.nav_notif_config -> startActivity(Intent(this@MessengerActivity, ManageNotificationActivity::class.java))
                R.id.nav_settings -> startActivity(Intent(this@MessengerActivity, SettingsActivity::class.java))
                R.id.nav_manage_screen -> startActivity(Intent(this@MessengerActivity, ManageMyScreenActivity::class.java))
                R.id.nav_knockons -> startActivity(Intent(this@MessengerActivity, ManageKnockonsActivity::class.java))
                R.id.nav_help -> startActivity(Intent(this@MessengerActivity, HelpActivity::class.java))
            }

            val drawer = findViewById<DrawerLayout>(R.id.messenger_drawer_layout)
            drawer.closeDrawer(GravityCompat.START)
            true
        }

        //endregion

        //region ========================================= Adapter ==========================================

//        messenger_RecyclerView.adapter = MessengerRecyclerViewAdapter(this, )

        //endregion

        //region ======================================== Listeners =========================================

        messenger_ComposeMessageFloatingButton!!.setOnClickListener {
            startActivity(Intent(this@MessengerActivity, ComposeMessageActivity::class.java))
        }

        //endregion

        //region ========================================= Facebook =========================================

        FacebookSdk.sdkInitialize(this.applicationContext);
        callbackManager = CallbackManager.Factory.create()
        val accessToken = AccessToken.getCurrentAccessToken()
        val isLoggedIn = accessToken != null && !accessToken.isExpired
        LoginManager.getInstance().logInWithReadPermissions(this, listOf("public_profile"));

        LoginManager.getInstance().registerCallback(callbackManager,
                object : FacebookCallback<LoginResult> {
                    override fun onSuccess(loginResult: LoginResult) {
                        Toast.makeText(this@MessengerActivity, "is connected", Toast.LENGTH_SHORT).show()
                    }

                    override fun onCancel() {
                        // App code
                    }

                    override fun onError(exception: FacebookException) {
                        // App code
                    }
                })
        val profile : Profile

        profileTracker = object : ProfileTracker() {
            override fun onCurrentProfileChanged(
                    oldProfile: Profile,
                    currentProfile: Profile) {
                // App code
            }
        }

        //endregion
    }

    //region ========================================== Functions ===========================================

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager!!.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        super.onDestroy()
        profileTracker!!.stopTracking()
    }

    //endregion
}
