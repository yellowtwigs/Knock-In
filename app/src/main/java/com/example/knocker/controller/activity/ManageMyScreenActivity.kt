package com.example.knocker.controller.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*

import com.example.knocker.R
import com.google.android.material.navigation.NavigationView

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout

/**
 * La Classe qui permet de changer le theme de l'application et de changer le nombre de contact par ligne
 * @author Florian Striebel, Kenzy Suon, Ryan Granet
 */
class ManageMyScreenActivity : AppCompatActivity() {

    private var drawerLayout: DrawerLayout? = null
    private var tv_one: ImageView? = null
    private var tv_three: ImageView? = null
    private var tv_four: ImageView? = null
    private var tv_five: ImageView? = null
    private var tv_six: ImageView? = null
    private var nbGrid: Int = 3
    private var callPopup: Boolean = true
    private var searchbarPos: Boolean = false

    private var manage_theme_SwitchTheme: Switch? = null
    private var manage_call_popup_switch: Switch? = null
    private var manage_searchbar_pos: Switch? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_my_screen)

        val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
        nbGrid = sharedPreferences.getInt("gridview", 3)
        val sharedPreferencePopup = getSharedPreferences("Phone_call", Context.MODE_PRIVATE)
        callPopup = sharedPreferencePopup.getBoolean("popup", true)
        tv_one = findViewById(R.id.activity_settings_imageView_1_contact)
        searchbarPos = sharedPreferencePopup.getBoolean("searchbarPos", false)

        tv_three = findViewById(R.id.activity_settings_imageView_3_contact)
        tv_four = findViewById(R.id.activity_settings_imageView_4_contact)
        tv_five = findViewById(R.id.activity_settings_imageView_5_contact)
        tv_six = findViewById(R.id.activity_settings_imageView_6_contact)
        tv_six!!.setImageResource(R.drawable.contactbyline6)
        tv_five!!.setImageResource(R.drawable.contactbyline5)
        tv_four!!.setImageResource(R.drawable.contactbyline4)
        tv_three!!.setImageResource(R.drawable.contactbyline3)
        tv_one!!.setImageResource(R.drawable.contactbyline1)


        manage_theme_SwitchTheme = findViewById(R.id.manage_theme_switch)
        manage_call_popup_switch = findViewById(R.id.manage_call_popup_switch)
        manage_searchbar_pos = findViewById(R.id.manage_searchbar_pos_switch)

        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            manage_theme_SwitchTheme!!.isChecked = true
        }
        if (callPopup) {
            manage_call_popup_switch!!.isChecked = true
        }
        if (searchbarPos) {
            manage_searchbar_pos!!.isChecked = true
        }
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
        val menu = navigationView.menu
        val nav_item = menu.findItem(R.id.nav_screen_config)
        nav_item.isChecked = true

        navigationView.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            drawerLayout!!.closeDrawers()

            when (menuItem.itemId) {
                R.id.nav_address_book -> {
                    startActivity(Intent(this@ManageMyScreenActivity, MainActivity::class.java))
                }
                R.id.nav_informations -> startActivity(Intent(this@ManageMyScreenActivity, EditInformationsActivity::class.java))
                R.id.nav_notif_config -> startActivity(Intent(this@ManageMyScreenActivity, ManageNotificationActivity::class.java))
                R.id.nav_screen_config -> {
                }
                R.id.nav_data_access -> {
                }
                R.id.nav_knockons -> startActivity(Intent(this@ManageMyScreenActivity, ManageKnockonsActivity::class.java))
                R.id.nav_statistics -> {
                }
                R.id.nav_help -> startActivity(Intent(this@ManageMyScreenActivity, HelpActivity::class.java))
            }

            val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
            drawer.closeDrawer(GravityCompat.START)
            true
        }
        //endregion

        //region ==================================== SetOnClickListener ====================================

        if (nbGrid == 1) {
            tv_one!!.setBackgroundResource(R.drawable.border_selected_image_view)
        } else if (nbGrid == 3) {
            tv_three!!.setBackgroundResource(R.drawable.border_selected_image_view)
        } else if (nbGrid == 4) {
            tv_four!!.setBackgroundResource(R.drawable.border_selected_image_view)
        } else if (nbGrid == 5) {
            tv_five!!.setBackgroundResource(R.drawable.border_selected_image_view)
        } else if (nbGrid == 6) {
            tv_six!!.setBackgroundResource(R.drawable.border_selected_image_view)
        }
        tv_one?.setOnClickListener {
            nbGrid = 1
            tv_three!!.background = null
            tv_four!!.background = null
            tv_five!!.background = null
            tv_six!!.background = null
            changeGridColumn()
        }
        tv_three?.setOnClickListener {
            nbGrid = 3
            val mes = String.format(resources.getString(R.string.manage_my_screen_toast), nbGrid)
            Toast.makeText(applicationContext, mes, Toast.LENGTH_SHORT).show()
            tv_one!!.background = null
            tv_four!!.background = null
            tv_five!!.background = null
            tv_six!!.background = null
            tv_three!!.setBackgroundResource(R.drawable.border_selected_image_view)
            changeGridColumn()
        }

        tv_four?.setOnClickListener {
            nbGrid = 4
            val mes = String.format(resources.getString(R.string.manage_my_screen_toast), nbGrid)
            Toast.makeText(applicationContext, mes, Toast.LENGTH_SHORT).show()
            tv_one!!.background = null
            tv_three!!.background = null
            tv_five!!.background = null
            tv_six!!.background = null
            tv_four!!.setBackgroundResource(R.drawable.border_selected_image_view)
            changeGridColumn()
        }

        tv_five?.setOnClickListener {
            nbGrid = 5
            val mes = String.format(resources.getString(R.string.manage_my_screen_toast), nbGrid)
            Toast.makeText(applicationContext, mes, Toast.LENGTH_SHORT).show()
            tv_one!!.background = null
            tv_three!!.background = null
            tv_four!!.background = null
            tv_six!!.background = null
            tv_five!!.setBackgroundResource(R.drawable.border_selected_image_view)
            changeGridColumn()
        }

        tv_six?.setOnClickListener {
            nbGrid = 6
            val mes = String.format(resources.getString(R.string.manage_my_screen_toast), nbGrid)
            Toast.makeText(applicationContext, mes, Toast.LENGTH_SHORT).show()
            tv_one!!.background = null
            tv_three!!.background = null
            tv_four!!.background = null
            tv_five!!.background = null
            tv_six!!.setBackgroundResource(R.drawable.border_selected_image_view)
            changeGridColumn()
        }

        manage_theme_SwitchTheme!!.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                val sharedThemePreferences: SharedPreferences = getSharedPreferences("Knocker_Theme", Context.MODE_PRIVATE)
                val edit: SharedPreferences.Editor = sharedThemePreferences.edit()
                edit.putBoolean("theme", false)
                edit.apply()
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                val sharedThemePreferences = getSharedPreferences("Knocker_Theme", Context.MODE_PRIVATE)
                val edit: SharedPreferences.Editor = sharedThemePreferences.edit()
                edit.putBoolean("theme", true)
                edit.apply()
            }
        })

        manage_call_popup_switch!!.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val sharedThemePreferences: SharedPreferences = getSharedPreferences("Phone_call", Context.MODE_PRIVATE)
                val edit: SharedPreferences.Editor = sharedThemePreferences.edit()
                edit.putBoolean("popup", true)
                edit.apply()
            } else {
                val sharedThemePreferences: SharedPreferences = getSharedPreferences("Phone_call", Context.MODE_PRIVATE)
                val edit: SharedPreferences.Editor = sharedThemePreferences.edit()
                edit.putBoolean("popup", false)
                edit.apply()
            }
        })

        manage_searchbar_pos!!.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val sharedThemePreferences: SharedPreferences = getSharedPreferences("Phone_call", Context.MODE_PRIVATE)
                val edit: SharedPreferences.Editor = sharedThemePreferences.edit()
                edit.putBoolean("searchbarPos", true)
                edit.apply()
            } else {
                val sharedThemePreferences: SharedPreferences = getSharedPreferences("Phone_call", Context.MODE_PRIVATE)
                val edit: SharedPreferences.Editor = sharedThemePreferences.edit()
                edit.putBoolean("searchbarPos", false)
                edit.apply()
            }
        })
        // endregion
    }
    //region ========================================== Functions ===========================================

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_help, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                drawerLayout!!.openDrawer(GravityCompat.START)
                return true
            }
            R.id.item_help -> {
                val alertDialogBuilder = android.app.AlertDialog.Builder(this)
                alertDialogBuilder.setMessage(this.resources.getString(R.string.help_screen_manager))
                alertDialogBuilder.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun changeGridColumn() {
        val loginIntent = Intent(this@ManageMyScreenActivity, MainActivity::class.java)
        val sharedPreferences: SharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
        val edit: SharedPreferences.Editor = sharedPreferences.edit()
        edit.putInt("gridview", nbGrid)
        edit.apply()
        startActivity(loginIntent)
        finish()
    }

    //endregion
}
