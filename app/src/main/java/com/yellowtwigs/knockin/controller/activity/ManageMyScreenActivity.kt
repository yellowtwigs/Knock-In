package com.yellowtwigs.knockin.controller.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.Point
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.yellowtwigs.knockin.R

/**
 * La Classe qui permet de changer le theme de l'application et de changer le nombre de contact par ligne
 * @author Florian Striebel, Kenzy Suon, Ryan Granet
 */
class ManageMyScreenActivity : AppCompatActivity() {

    //region ========================================== Val or Var ==========================================

    private var drawerLayout: DrawerLayout? = null

    private var manageMyScreenImageContactByLine1: AppCompatImageView? = null
    private var manageMyScreenImageContactByLine4: AppCompatImageView? = null
    private var manageMyScreenImageContactByLine5: AppCompatImageView? = null

    private var manageMyScreenColorContactBlueIndigo: AppCompatImageView? = null
    private var manageMyScreenColorContactGreenLime: AppCompatImageView? = null
    private var manageMyScreenColorContactPurpleGrape: AppCompatImageView? = null
    private var manageMyScreenColorContactRed: AppCompatImageView? = null
    private var manageMyScreenColorContactGrey: AppCompatImageView? = null
    private var manageMyScreenColorContactOrange: AppCompatImageView? = null
    private var manageMyScreenColorContactCyanTeal: AppCompatImageView? = null

    private var manageMyScreenSpinnerSelectColor: Spinner? = null
    private var manageMyScreenButtonSelectColorLayout: HorizontalScrollView? = null

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

        setContentView()

        //region ======================================= SharedPreferences =======================================

        val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
        val nbGrid = sharedPreferences.getInt("gridview", 1)

        val sharedPreferencesIsMultiColor = getSharedPreferences("IsMultiColor", Context.MODE_PRIVATE)
        var isMultiColor = sharedPreferencesIsMultiColor.getInt("isMultiColor", 0)

        val sharedPreferencesContactsColor = getSharedPreferences("ContactsColor", Context.MODE_PRIVATE)
        val contactsColor = sharedPreferencesContactsColor.getInt("contactsColor", 0)

        //endregion

        //region ======================================= FindViewById =======================================

        manageMyScreenImageContactByLine1 = findViewById(R.id.activity_manage_my_screen_image_contact_by_line1)
        manageMyScreenImageContactByLine4 = findViewById(R.id.activity_manage_my_screen_image_contact_by_line4)
        manageMyScreenImageContactByLine5 = findViewById(R.id.activity_manage_my_screen_image_contact_by_line5)

        manageMyScreenSpinnerSelectColor = findViewById(R.id.activity_manage_my_screen_change_contact_color_spinner)
        manageMyScreenButtonSelectColorLayout = findViewById(R.id.activity_manage_my_screen_change_contact_color_scroll_view)

        manageMyScreenColorContactBlueIndigo = findViewById(R.id.activity_manage_my_screen_color_blue_indigo)
        manageMyScreenColorContactGreenLime = findViewById(R.id.activity_manage_my_screen_color_green_lime)
        manageMyScreenColorContactPurpleGrape = findViewById(R.id.activity_manage_my_screen_color_purple_grape)
        manageMyScreenColorContactRed = findViewById(R.id.activity_manage_my_screen_color_red)
        manageMyScreenColorContactGrey = findViewById(R.id.activity_manage_my_screen_color_grey)
        manageMyScreenColorContactOrange = findViewById(R.id.activity_manage_my_screen_color_orange)
        manageMyScreenColorContactCyanTeal = findViewById(R.id.activity_manage_my_screen_color_cyan_teal)

        val main_SettingsLeftDrawerLayout = findViewById<RelativeLayout>(R.id.settings_left_drawer_layout)

        val settings_left_drawer_ThemeSwitch = findViewById<Switch>(R.id.settings_left_drawer_theme_switch)

        if (sharedThemePreferences.getBoolean("darkTheme", false)) {
            settings_left_drawer_ThemeSwitch!!.isChecked = true
//            manage_my_screen_Layout!!.setBackgroundResource(R.drawable.dark_background)
        }

        //region ================================ Call Popup from LeftDrawer ================================

        val sharedPreferencePopup = getSharedPreferences("Phone_call", Context.MODE_PRIVATE)
        val settings_CallPopupSwitch = findViewById<Switch>(R.id.settings_call_popup_switch)

        if (sharedPreferencePopup.getBoolean("popup", true)) {
            settings_CallPopupSwitch!!.isChecked = true
        }

        //endregion

        //endregion

        //region ===================================== SetImageResource =====================================

        val arrayImagesContactByLine1 = intArrayOf(R.drawable.ic_contact_by_line1_multi_color,
                R.drawable.ic_contact_by_line1_blue, R.drawable.ic_contact_by_line1_cyan, R.drawable.ic_contact_by_line1_green,
                R.drawable.ic_contact_by_line1_blue, R.drawable.ic_contact_by_line1_orange, R.drawable.ic_contact_by_line1_purple,
                R.drawable.ic_contact_by_line1_red)

        val arrayImagesContactByLine4 = intArrayOf(R.drawable.ic_contact_by_line4_multi_color,
                R.drawable.ic_contact_by_line4_blue, R.drawable.ic_contact_by_line4_cyan, R.drawable.ic_contact_by_line4_green,
                R.drawable.ic_contact_by_line4_blue, R.drawable.ic_contact_by_line4_orange, R.drawable.ic_contact_by_line4_purple,
                R.drawable.ic_contact_by_line4_red)

        val arrayImagesContactByLine5 = intArrayOf(R.drawable.ic_contact_by_line5_multi_color,
                R.drawable.ic_contact_by_line5_blue, R.drawable.ic_contact_by_line5_blue_om, R.drawable.ic_contact_by_line5_cyan,
                R.drawable.ic_contact_by_line5_green, R.drawable.ic_contact_by_line5_orange, R.drawable.ic_contact_by_line5_purple,
                R.drawable.ic_contact_by_line5_red)

        when (contactsColor) {
            0 -> {
                manageMyScreenColorContactBlueIndigo!!.setBackgroundResource(R.drawable.border_selected_yellow)
                manageMyScreenColorContactGreenLime!!.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactPurpleGrape!!.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactRed!!.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactGrey!!.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactOrange!!.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactCyanTeal!!.setBackgroundResource(R.drawable.border_selected_grey)
            }
            1 -> {
                manageMyScreenColorContactBlueIndigo!!.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactGreenLime!!.setBackgroundResource(R.drawable.border_selected_yellow)
                manageMyScreenColorContactPurpleGrape!!.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactRed!!.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactGrey!!.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactOrange!!.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactCyanTeal!!.setBackgroundResource(R.drawable.border_selected_grey)
            }
            2 -> {
                manageMyScreenColorContactBlueIndigo!!.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactGreenLime!!.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactPurpleGrape!!.setBackgroundResource(R.drawable.border_selected_yellow)
                manageMyScreenColorContactRed!!.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactGrey!!.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactOrange!!.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactCyanTeal!!.setBackgroundResource(R.drawable.border_selected_grey)
            }
            3 -> {
                manageMyScreenColorContactBlueIndigo!!.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactGreenLime!!.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactPurpleGrape!!.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactRed!!.setBackgroundResource(R.drawable.border_selected_yellow)
                manageMyScreenColorContactGrey!!.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactOrange!!.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactCyanTeal!!.setBackgroundResource(R.drawable.border_selected_grey)
            }
            4 -> {
                manageMyScreenColorContactBlueIndigo!!.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactGreenLime!!.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactPurpleGrape!!.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactRed!!.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactGrey!!.setBackgroundResource(R.drawable.border_selected_yellow)
                manageMyScreenColorContactOrange!!.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactCyanTeal!!.setBackgroundResource(R.drawable.border_selected_grey)
            }
            5 -> {
                manageMyScreenColorContactBlueIndigo!!.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactGreenLime!!.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactPurpleGrape!!.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactRed!!.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactGrey!!.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactOrange!!.setBackgroundResource(R.drawable.border_selected_yellow)
                manageMyScreenColorContactCyanTeal!!.setBackgroundResource(R.drawable.border_selected_grey)
            }
            6 -> {
                manageMyScreenColorContactBlueIndigo!!.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactGreenLime!!.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactPurpleGrape!!.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactRed!!.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactGrey!!.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactOrange!!.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactCyanTeal!!.setBackgroundResource(R.drawable.border_selected_yellow)
            }
        }

        when (nbGrid) {
            1 -> {
                manageMyScreenImageContactByLine1!!.setBackgroundResource(R.drawable.border_selected_yellow)
                manageMyScreenImageContactByLine4!!.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenImageContactByLine5!!.setBackgroundResource(R.drawable.border_selected_grey)
            }
            4 -> {
                manageMyScreenImageContactByLine1!!.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenImageContactByLine4!!.setBackgroundResource(R.drawable.border_selected_yellow)
                manageMyScreenImageContactByLine5!!.setBackgroundResource(R.drawable.border_selected_grey)
            }
            5 -> {
                manageMyScreenImageContactByLine1!!.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenImageContactByLine4!!.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenImageContactByLine5!!.setBackgroundResource(R.drawable.border_selected_yellow)
            }
        }

        manageMyScreenImageContactByLine1!!.setImageDrawable(getDrawable(arrayImagesContactByLine1[0]))
        manageMyScreenImageContactByLine4!!.setImageDrawable(getDrawable(arrayImagesContactByLine4[0]))
        manageMyScreenImageContactByLine5!!.setImageDrawable(getDrawable(arrayImagesContactByLine5[0]))

        manageMyScreenColorContactBlueIndigo!!.setImageDrawable(getDrawable(R.drawable.ic_user_blue_indigo1))
        manageMyScreenColorContactGreenLime!!.setImageDrawable(getDrawable(R.drawable.ic_user_green_lime1))
        manageMyScreenColorContactPurpleGrape!!.setImageDrawable(getDrawable(R.drawable.ic_user_purple_grape1))
        manageMyScreenColorContactRed!!.setImageDrawable(getDrawable(R.drawable.ic_user_red))
        manageMyScreenColorContactGrey!!.setImageDrawable(getDrawable(R.drawable.ic_user_grey1))
        manageMyScreenColorContactOrange!!.setImageDrawable(getDrawable(R.drawable.ic_user_orange))
        manageMyScreenColorContactCyanTeal!!.setImageDrawable(getDrawable(R.drawable.ic_user_cyan_teal))

        //endregion

        //region ========================================== Toolbar =========================================

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar.setHomeAsUpIndicator(R.drawable.ic_open_drawer)

        //endregion

        //region ======================================= DrawerLayout =======================================

        drawerLayout = findViewById(R.id.manage_my_screen_drawer_layout)

        val navigationView = findViewById<NavigationView>(R.id.manage_my_screen_nav_view)
        val menu = navigationView.menu
        val navItem = menu.findItem(R.id.nav_manage_screen)
        navItem.isChecked = true

        navigationView.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            drawerLayout!!.closeDrawers()

            when (menuItem.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this@ManageMyScreenActivity, MainActivity::class.java))
                }
                R.id.nav_informations -> startActivity(Intent(this@ManageMyScreenActivity, EditInformationsActivity::class.java))
                R.id.nav_notif_config -> startActivity(Intent(this@ManageMyScreenActivity, ManageNotificationActivity::class.java))
                R.id.nav_settings -> startActivity(Intent(this@ManageMyScreenActivity, SettingsActivity::class.java))
                R.id.nav_in_app -> startActivity(Intent(this@ManageMyScreenActivity, PremiumActivity::class.java))
                R.id.nav_manage_screen -> {
                }
                R.id.nav_data_access -> {
                }
                R.id.nav_knockons -> startActivity(Intent(this@ManageMyScreenActivity, ManageKnockonsActivity::class.java))
                R.id.nav_statistics -> {
                }
                R.id.nav_help -> startActivity(Intent(this@ManageMyScreenActivity, HelpActivity::class.java))
            }

            val drawer = findViewById<DrawerLayout>(R.id.manage_my_screen_drawer_layout)
            drawer.closeDrawer(GravityCompat.START)
            true
        }

        //endregion

        //region ======================================== Listeners =========================================

        //region ======================================= ItemSelected =======================================

        manageMyScreenImageContactByLine1!!.setOnClickListener {
            manageMyScreenImageContactByLine1!!.setBackgroundResource(R.drawable.border_selected_yellow)
            manageMyScreenImageContactByLine4!!.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenImageContactByLine5!!.setBackgroundResource(R.drawable.border_selected_grey)

            val sharedPreferences: SharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
            val edit: SharedPreferences.Editor = sharedPreferences.edit()
            edit.putInt("gridview", 1)
            edit.apply()

            buildMaterialAlertDialogBuilder()
        }

        manageMyScreenImageContactByLine4!!.setOnClickListener {
            manageMyScreenImageContactByLine1!!.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenImageContactByLine4!!.setBackgroundResource(R.drawable.border_selected_yellow)
            manageMyScreenImageContactByLine5!!.setBackgroundResource(R.drawable.border_selected_grey)

            val sharedPreferences: SharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
            val edit: SharedPreferences.Editor = sharedPreferences.edit()
            edit.putInt("gridview", 4)
            edit.apply()

            buildMaterialAlertDialogBuilder()
        }

        manageMyScreenImageContactByLine5!!.setOnClickListener {
            manageMyScreenImageContactByLine1!!.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenImageContactByLine4!!.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenImageContactByLine5!!.setBackgroundResource(R.drawable.border_selected_yellow)

            val sharedPreferences: SharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
            val edit: SharedPreferences.Editor = sharedPreferences.edit()
            edit.putInt("gridview", 5)
            edit.apply()

            buildMaterialAlertDialogBuilder()
        }

        val colorList = resources.getStringArray(R.array.manage_my_screen_spinner_array)
        val colorsSpinnerAdapter = ArrayAdapter(this, R.layout.spinner_item, colorList)
        manageMyScreenSpinnerSelectColor!!.adapter = colorsSpinnerAdapter
        manageMyScreenSpinnerSelectColor!!.setSelection(isMultiColor)
        manageMyScreenSpinnerSelectColor!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> {
                        if (isMultiColor == 1) {
                            buildMaterialAlertDialogBuilder()
                        }

                        val edit: SharedPreferences.Editor = sharedPreferencesIsMultiColor.edit()
                        edit.putInt("isMultiColor", position)
                        edit.apply()
                        isMultiColor = 0

                        manageMyScreenButtonSelectColorLayout!!.visibility = View.INVISIBLE
                    }
                    1 -> {
                        val edit: SharedPreferences.Editor = sharedPreferencesIsMultiColor.edit()
                        edit.putInt("isMultiColor", position)
                        edit.apply()
                        isMultiColor = 1

                        manageMyScreenButtonSelectColorLayout!!.visibility = View.VISIBLE
                    }
                }
            }
        }

        manageMyScreenColorContactBlueIndigo!!.setOnClickListener {

            manageMyScreenColorContactBlueIndigo!!.setBackgroundResource(R.drawable.border_selected_yellow)
            manageMyScreenColorContactGreenLime!!.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactPurpleGrape!!.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactRed!!.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactGrey!!.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactOrange!!.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactCyanTeal!!.setBackgroundResource(R.drawable.border_selected_grey)

            val edit: SharedPreferences.Editor = sharedPreferencesContactsColor.edit()
            edit.putInt("contactsColor", 0)
            edit.apply()

            buildMaterialAlertDialogBuilder()
        }

        manageMyScreenColorContactGreenLime!!.setOnClickListener {

            manageMyScreenColorContactBlueIndigo!!.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactGreenLime!!.setBackgroundResource(R.drawable.border_selected_yellow)
            manageMyScreenColorContactPurpleGrape!!.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactRed!!.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactGrey!!.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactOrange!!.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactCyanTeal!!.setBackgroundResource(R.drawable.border_selected_grey)

            val edit: SharedPreferences.Editor = sharedPreferencesContactsColor.edit()
            edit.putInt("contactsColor", 1)
            edit.apply()

            buildMaterialAlertDialogBuilder()
        }

        manageMyScreenColorContactPurpleGrape!!.setOnClickListener {

            manageMyScreenColorContactBlueIndigo!!.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactGreenLime!!.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactPurpleGrape!!.setBackgroundResource(R.drawable.border_selected_yellow)
            manageMyScreenColorContactRed!!.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactGrey!!.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactOrange!!.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactCyanTeal!!.setBackgroundResource(R.drawable.border_selected_grey)

            val edit: SharedPreferences.Editor = sharedPreferencesContactsColor.edit()
            edit.putInt("contactsColor", 2)
            edit.apply()

            buildMaterialAlertDialogBuilder()
        }

        manageMyScreenColorContactRed!!.setOnClickListener {

            manageMyScreenColorContactBlueIndigo!!.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactGreenLime!!.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactPurpleGrape!!.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactRed!!.setBackgroundResource(R.drawable.border_selected_yellow)
            manageMyScreenColorContactGrey!!.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactOrange!!.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactCyanTeal!!.setBackgroundResource(R.drawable.border_selected_grey)

            val edit: SharedPreferences.Editor = sharedPreferencesContactsColor.edit()
            edit.putInt("contactsColor", 3)
            edit.apply()

            buildMaterialAlertDialogBuilder()
        }

        manageMyScreenColorContactGrey!!.setOnClickListener {

            manageMyScreenColorContactBlueIndigo!!.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactGreenLime!!.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactPurpleGrape!!.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactRed!!.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactGrey!!.setBackgroundResource(R.drawable.border_selected_yellow)
            manageMyScreenColorContactOrange!!.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactCyanTeal!!.setBackgroundResource(R.drawable.border_selected_grey)

            val edit: SharedPreferences.Editor = sharedPreferencesContactsColor.edit()
            edit.putInt("contactsColor", 4)
            edit.apply()

            buildMaterialAlertDialogBuilder()
        }

        manageMyScreenColorContactOrange!!.setOnClickListener {

            manageMyScreenColorContactBlueIndigo!!.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactGreenLime!!.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactPurpleGrape!!.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactRed!!.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactGrey!!.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactOrange!!.setBackgroundResource(R.drawable.border_selected_yellow)
            manageMyScreenColorContactCyanTeal!!.setBackgroundResource(R.drawable.border_selected_grey)

            val edit: SharedPreferences.Editor = sharedPreferencesContactsColor.edit()
            edit.putInt("contactsColor", 5)
            edit.apply()

            buildMaterialAlertDialogBuilder()
        }

        manageMyScreenColorContactCyanTeal!!.setOnClickListener {

            manageMyScreenColorContactBlueIndigo!!.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactGreenLime!!.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactPurpleGrape!!.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactRed!!.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactGrey!!.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactOrange!!.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactCyanTeal!!.setBackgroundResource(R.drawable.border_selected_yellow)

            val edit: SharedPreferences.Editor = sharedPreferencesContactsColor.edit()
            edit.putInt("contactsColor", 6)
            edit.apply()

            buildMaterialAlertDialogBuilder()
        }

        //endregion

        settings_CallPopupSwitch!!.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val sharedCallPopupPreferences: SharedPreferences = getSharedPreferences("Phone_call", Context.MODE_PRIVATE)
                val edit: SharedPreferences.Editor = sharedCallPopupPreferences.edit()
                edit.putBoolean("popup", true)
                edit.apply()
            } else {
                val sharedCallPopupPreferences: SharedPreferences = getSharedPreferences("Phone_call", Context.MODE_PRIVATE)
                val edit: SharedPreferences.Editor = sharedCallPopupPreferences.edit()
                edit.putBoolean("popup", false)
                edit.apply()
            }
        }

        settings_left_drawer_ThemeSwitch!!.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {

                setTheme(R.style.AppThemeDark)
//                manage_my_screen_Layout!!.setBackgroundResource(R.drawable.dark_background)
                val edit: SharedPreferences.Editor = sharedThemePreferences.edit()
                edit.putBoolean("darkTheme", true)
                edit.apply()
                startActivity(Intent(this@ManageMyScreenActivity, ManageMyScreenActivity::class.java))
            } else {

                setTheme(R.style.AppTheme)
//                manage_my_screen_Layout!!.setBackgroundResource(R.drawable.mr_white_blur_background)
                val edit: SharedPreferences.Editor = sharedThemePreferences.edit()
                edit.putBoolean("darkTheme", false)
                edit.apply()
                startActivity(Intent(this@ManageMyScreenActivity, ManageMyScreenActivity::class.java))
            }
        }

        // endregion
    }

    //region ========================================== Functions ===========================================

    private fun setContentView() {
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val height = size.y

        when {
            height > 2500 -> setContentView(R.layout.activity_manage_my_screen)
            height in 1800..2499 -> setContentView(R.layout.activity_manage_my_screen)
            height in 1100..1799 -> setContentView(R.layout.activity_manage_my_screen_smaller_screen)
            height < 1099 -> setContentView(R.layout.activity_manage_my_screen_mini_screen)
        }
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.toolbar_menu_help, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun refreshActivity() {
        val i = Intent(applicationContext, ManageMyScreenActivity::class.java)
        startActivity(i)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                drawerLayout!!.openDrawer(GravityCompat.START)
                return true
            }
            R.id.item_help -> {
                MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                        .setTitle(R.string.help)
                        .setMessage(this.resources.getString(R.string.help_manage_screen))
                        .show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun buildMaterialAlertDialogBuilder() {
        MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                .setTitle(getString(R.string.manage_my_screen_display_contacts_edit_alert_dialog_title)) // getString(R.string.main_alert_dialog_delete_contact_title)
                .setMessage(getString(R.string.manage_my_screen_display_contacts_edit_alert_dialog_message))
                .setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
                    startActivity(Intent(this@ManageMyScreenActivity, MainActivity::class.java))
                }
                .setNegativeButton(R.string.alert_dialog_no) { _, _ ->
                }
                .show()
    }

    //endregion
}