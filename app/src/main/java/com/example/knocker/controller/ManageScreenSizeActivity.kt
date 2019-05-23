package com.example.knocker.controller

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.RelativeLayout

import com.example.knocker.R
import com.google.android.material.navigation.NavigationView

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout

class ManageScreenSizeActivity : AppCompatActivity() {

    private var drawerLayout: DrawerLayout? = null
    private var my_knocker: RelativeLayout? = null
    private var tv_three: ImageView? = null
    private var tv_four: ImageView? = null
    private var tv_five: ImageView? = null
    private var tv_six: ImageView? = null
    private var nbGrid: Int = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_screen_size)

        tv_three = findViewById(R.id.activity_settings_imageView_3_contact)
        tv_four = findViewById(R.id.activity_settings_imageView_4_contact)
        tv_five = findViewById(R.id.activity_settings_imageView_5_contact)
        tv_six = findViewById(R.id.activity_settings_imageView_6_contact)
        tv_six!!.setImageResource(R.drawable.contactbyline6)
        tv_five!!.setImageResource(R.drawable.contactbyline5)
        tv_four!!.setImageResource(R.drawable.contactbyline4)
        tv_three!!.setImageResource(R.drawable.contactbyline3)

        //region ========================================== Toolbar =========================================

        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar.setHomeAsUpIndicator(R.drawable.ic_open_drawer)

        //endregion

        //region ======================================= DrawerLayout =======================================

        // Drawerlayout
        drawerLayout = findViewById(R.id.drawer_layout)

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        val headerView = navigationView.getHeaderView(0);
        my_knocker = headerView.findViewById(R.id.my_knocker)

        my_knocker!!.setOnClickListener {
            startActivity(Intent(this@ManageScreenSizeActivity, MainActivity::class.java))
        }
        navigationView.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            drawerLayout!!.closeDrawers()

            val id = menuItem.itemId

            if (id == R.id.nav_informations) {
                startActivity(Intent(this@ManageScreenSizeActivity, EditInformationsActivity::class.java))
            } else if (id == R.id.nav_notif_config) {
                startActivity(Intent(this@ManageScreenSizeActivity, ManageNotificationActivity::class.java))
            } else if (id == R.id.nav_screen_size) {
            } else if (id == R.id.nav_theme) {
                startActivity(Intent(this@ManageScreenSizeActivity, ManageThemeActivity::class.java))
            } else if (id == R.id.nav_data_access) {
            } else if (id == R.id.nav_knockons) {
                startActivity(Intent(this@ManageScreenSizeActivity, ManageKnockonsActivity::class.java))
            } else if (id == R.id.nav_statistics) {
            } else if (id == R.id.nav_help) {
                startActivity(Intent(this@ManageScreenSizeActivity, HelpActivity::class.java))
            }

            val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
            drawer.closeDrawer(GravityCompat.START)
            true
        }
        //endregion

        //region ==================================== SetOnClickListener ====================================

        my_knocker!!.setOnClickListener {
            startActivity(Intent(this@ManageScreenSizeActivity, MainActivity::class.java))
        }

        if (nbGrid == 3) {
            tv_three!!.setBackgroundResource(R.drawable.border_imageview)
        } else if (nbGrid == 4) {
            tv_four!!.setBackgroundResource(R.drawable.border_imageview)
        } else if (nbGrid == 5) {
            tv_five!!.setBackgroundResource(R.drawable.border_imageview)
        } else if (nbGrid == 6) {
            tv_six!!.setBackgroundResource(R.drawable.border_imageview)
        }
        tv_three?.setOnClickListener {
            nbGrid = 3
            // val mes= String.format(resources.getString(R.string.settings_toast),nbGrid)
            // Toast.makeText(applicationContext,mes,Toast.LENGTH_SHORT).show()
            tv_four!!.background = null
            tv_five!!.background = null
            tv_six!!.background = null
            tv_three!!.setBackgroundResource(R.drawable.border_imageview)

        }

        tv_four?.setOnClickListener {
            nbGrid = 4
            // val mes= String.format(resources.getString(R.string.settings_toast),nbGrid)
            // Toast.makeText(applicationContext,mes,Toast.LENGTH_SHORT).show()
            tv_three!!.background = null
            tv_five!!.background = null
            tv_six!!.background = null
            tv_four!!.setBackgroundResource(R.drawable.border_imageview)
        }

        tv_five?.setOnClickListener {
            nbGrid = 5
            // val mes= String.format(resources.getString(R.string.settings_toast),nbGrid)
            // Toast.makeText(applicationContext,mes,Toast.LENGTH_SHORT).show()
            tv_three!!.background = null
            tv_four!!.background = null
            tv_six!!.background = null
            tv_five!!.setBackgroundResource(R.drawable.border_imageview)
        }

        tv_six?.setOnClickListener {
            nbGrid = 6
            //val mes= String.format(resources.getString(R.string.settings_toast),nbGrid)
            //Toast.makeText(applicationContext,mes,Toast.LENGTH_SHORT).show()
            tv_three!!.background = null
            tv_four!!.background = null
            tv_five!!.background = null
            tv_six!!.setBackgroundResource(R.drawable.border_imageview)
        }


        // endregion
    }
    //region ========================================== Functions ===========================================

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                drawerLayout!!.openDrawer(GravityCompat.START)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //endregion
}
