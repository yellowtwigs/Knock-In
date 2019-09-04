package com.yellowtwigs.knockin.controller.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import com.yellowtwigs.knockin.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView

class MessengerActivity : AppCompatActivity() {

    //region ========================================== Var or Val ==========================================

    private var messengerDrawerLayout: DrawerLayout? = null

    private var messenger_ComposeMessageFloatingButton: FloatingActionButton? = null

    private var messenger_RecyclerView: RecyclerView? = null

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

        //region ======================================= Adapter =======================================

//        messenger_RecyclerView.adapter = MessengerRecyclerViewAdapter(this, )

        //endregion

        //region ======================================== Listeners =========================================

        messenger_ComposeMessageFloatingButton!!.setOnClickListener {
            startActivity(Intent(this@MessengerActivity, ComposeMessageActivity::class.java))
        }

        //endregion
    }

    //region ========================================== Functions ===========================================


    //endregion
}
