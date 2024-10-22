package com.yellowtwigs.knockin.ui.settings

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.ui.HelpActivity
import com.yellowtwigs.knockin.ui.contacts.MainActivity
import com.yellowtwigs.knockin.ui.group.GroupManagerActivity
import com.yellowtwigs.knockin.ui.in_app.PremiumActivity
import com.yellowtwigs.knockin.ui.teleworking.TeleworkingActivity


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

        setContentView(R.layout.activity_manage_my_screen)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT

        //region ======================================= SharedPreferences =======================================

        val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
        val nbGrid = sharedPreferences.getInt("gridview", 1)

        val sharedPreferencesIsMultiColor =
            getSharedPreferences("IsMultiColor", Context.MODE_PRIVATE)
        var IsMultiColor = sharedPreferencesIsMultiColor.getInt("IsMultiColor", 0)

        val sharedPreferencesContactsColor =
            getSharedPreferences("ContactsColor", Context.MODE_PRIVATE)
        val contactsColor = sharedPreferencesContactsColor.getInt("contactsColor", 0)

        //endregion

        //region ======================================= FindViewById =======================================

        manageMyScreenImageContactByLine1 =
            findViewById(R.id.activity_manage_my_screen_image_contact_by_line1)
        manageMyScreenImageContactByLine4 =
            findViewById(R.id.activity_manage_my_screen_image_contact_by_line4)
        manageMyScreenImageContactByLine5 =
            findViewById(R.id.activity_manage_my_screen_image_contact_by_line5)

        manageMyScreenSpinnerSelectColor =
            findViewById(R.id.activity_manage_my_screen_change_contact_color_spinner)
        manageMyScreenButtonSelectColorLayout =
            findViewById(R.id.activity_manage_my_screen_change_contact_color_scroll_view)

        manageMyScreenColorContactBlueIndigo =
            findViewById(R.id.activity_manage_my_screen_color_blue_indigo)
        manageMyScreenColorContactGreenLime =
            findViewById(R.id.activity_manage_my_screen_color_green_lime)
        manageMyScreenColorContactPurpleGrape =
            findViewById(R.id.activity_manage_my_screen_color_purple_grape)
        manageMyScreenColorContactRed = findViewById(R.id.activity_manage_my_screen_color_red)
        manageMyScreenColorContactGrey = findViewById(R.id.activity_manage_my_screen_color_grey)
        manageMyScreenColorContactOrange = findViewById(R.id.activity_manage_my_screen_color_orange)
        manageMyScreenColorContactCyanTeal =
            findViewById(R.id.activity_manage_my_screen_color_cyan_teal)

        //endregion

        checkTheme(sharedThemePreferences)

        if (intent.getBooleanExtra("ChangeTheme", false)) {
            buildMaterialAlertDialogBuilder()
        }

        //region ===================================== SetImageResource =====================================

        val arrayImagesContactByLine1 = intArrayOf(
            R.drawable.ic_contact_by_line1_multi_color,
            R.drawable.ic_contact_by_line1_blue,
            R.drawable.ic_contact_by_line1_cyan,
            R.drawable.ic_contact_by_line1_green,
            R.drawable.ic_contact_by_line1_blue,
            R.drawable.ic_contact_by_line1_orange,
            R.drawable.ic_contact_by_line1_purple,
            R.drawable.ic_contact_by_line1_red
        )

        val arrayImagesContactByLine4 = intArrayOf(
            R.drawable.ic_contact_by_line4_multi_color,
            R.drawable.ic_contact_by_line4_blue,
            R.drawable.ic_contact_by_line4_cyan,
            R.drawable.ic_contact_by_line4_green,
            R.drawable.ic_contact_by_line4_blue,
            R.drawable.ic_contact_by_line4_orange,
            R.drawable.ic_contact_by_line4_purple,
            R.drawable.ic_contact_by_line4_red
        )

        val arrayImagesContactByLine5 = intArrayOf(
            R.drawable.ic_contact_by_line5_multi_color,
            R.drawable.ic_contact_by_line5_blue,
            R.drawable.ic_contact_by_line5_blue_om,
            R.drawable.ic_contact_by_line5_cyan,
            R.drawable.ic_contact_by_line5_green,
            R.drawable.ic_contact_by_line5_orange,
            R.drawable.ic_contact_by_line5_purple,
            R.drawable.ic_contact_by_line5_red
        )

        when (contactsColor) {
            0 -> {
                manageMyScreenColorContactBlueIndigo?.setBackgroundResource(R.drawable.border_selected_yellow)
                manageMyScreenColorContactGreenLime?.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactPurpleGrape?.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactRed?.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactGrey?.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactOrange?.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactCyanTeal?.setBackgroundResource(R.drawable.border_selected_grey)
            }
            1 -> {
                manageMyScreenColorContactBlueIndigo?.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactGreenLime?.setBackgroundResource(R.drawable.border_selected_yellow)
                manageMyScreenColorContactPurpleGrape?.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactRed?.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactGrey?.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactOrange?.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactCyanTeal?.setBackgroundResource(R.drawable.border_selected_grey)
            }
            2 -> {
                manageMyScreenColorContactBlueIndigo?.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactGreenLime?.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactPurpleGrape?.setBackgroundResource(R.drawable.border_selected_yellow)
                manageMyScreenColorContactRed?.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactGrey?.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactOrange?.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactCyanTeal?.setBackgroundResource(R.drawable.border_selected_grey)
            }
            3 -> {
                manageMyScreenColorContactBlueIndigo?.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactGreenLime?.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactPurpleGrape?.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactRed?.setBackgroundResource(R.drawable.border_selected_yellow)
                manageMyScreenColorContactGrey?.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactOrange?.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactCyanTeal?.setBackgroundResource(R.drawable.border_selected_grey)
            }
            4 -> {
                manageMyScreenColorContactBlueIndigo?.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactGreenLime?.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactPurpleGrape?.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactRed?.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactGrey?.setBackgroundResource(R.drawable.border_selected_yellow)
                manageMyScreenColorContactOrange?.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactCyanTeal?.setBackgroundResource(R.drawable.border_selected_grey)
            }
            5 -> {
                manageMyScreenColorContactBlueIndigo?.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactGreenLime?.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactPurpleGrape?.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactRed?.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactGrey?.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactOrange?.setBackgroundResource(R.drawable.border_selected_yellow)
                manageMyScreenColorContactCyanTeal?.setBackgroundResource(R.drawable.border_selected_grey)
            }
            6 -> {
                manageMyScreenColorContactBlueIndigo?.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactGreenLime?.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactPurpleGrape?.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactRed?.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactGrey?.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactOrange?.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenColorContactCyanTeal?.setBackgroundResource(R.drawable.border_selected_yellow)
            }
        }

        when (nbGrid) {
            1 -> {
                manageMyScreenImageContactByLine1?.setBackgroundResource(R.drawable.border_selected_yellow)
                manageMyScreenImageContactByLine4?.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenImageContactByLine5?.setBackgroundResource(R.drawable.border_selected_grey)
            }
            4 -> {
                manageMyScreenImageContactByLine1?.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenImageContactByLine4?.setBackgroundResource(R.drawable.border_selected_yellow)
                manageMyScreenImageContactByLine5?.setBackgroundResource(R.drawable.border_selected_grey)
            }
            5 -> {
                manageMyScreenImageContactByLine1?.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenImageContactByLine4?.setBackgroundResource(R.drawable.border_selected_grey)
                manageMyScreenImageContactByLine5?.setBackgroundResource(R.drawable.border_selected_yellow)
            }
        }

        manageMyScreenImageContactByLine1?.setImageDrawable(getDrawable(arrayImagesContactByLine1[0]))
        manageMyScreenImageContactByLine4?.setImageDrawable(getDrawable(arrayImagesContactByLine4[0]))
        manageMyScreenImageContactByLine5?.setImageDrawable(getDrawable(arrayImagesContactByLine5[0]))

        manageMyScreenColorContactBlueIndigo?.setImageDrawable(getDrawable(R.drawable.ic_user_blue_indigo1))
        manageMyScreenColorContactGreenLime?.setImageDrawable(getDrawable(R.drawable.ic_user_green_lime1))
        manageMyScreenColorContactPurpleGrape?.setImageDrawable(getDrawable(R.drawable.ic_user_purple_grape1))
        manageMyScreenColorContactRed?.setImageDrawable(getDrawable(R.drawable.ic_user_red))
        manageMyScreenColorContactGrey?.setImageDrawable(getDrawable(R.drawable.ic_user_grey1))
        manageMyScreenColorContactOrange?.setImageDrawable(getDrawable(R.drawable.ic_user_orange))
        manageMyScreenColorContactCyanTeal?.setImageDrawable(getDrawable(R.drawable.ic_user_cyan_teal))

        //endregion

        //region ========================================== Toolbar =========================================

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar
        actionbar?.setDisplayHomeAsUpEnabled(true)
        actionbar?.setHomeAsUpIndicator(R.drawable.ic_open_drawer)

        //endregion

        //region ======================================= DrawerLayout =======================================

        drawerLayout = findViewById(R.id.manage_my_screen_drawer_layout)

        val navigationView = findViewById<NavigationView>(R.id.manage_my_screen_nav_view)
        val menu = navigationView.menu
        menu.findItem(R.id.nav_manage_screen).isChecked = true

        val itemLayout = findViewById<ConstraintLayout>(R.id.teleworking_item)
        val itemText = findViewById<AppCompatTextView>(R.id.teleworking_item_text)

        itemText.text =
            "${getString(R.string.teleworking)} ${getString(R.string.left_drawer_settings)}"

        itemLayout.setOnClickListener {
            startActivity(
                Intent(
                    this@ManageMyScreenActivity,
                    TeleworkingActivity::class.java
                )
            )
        }

        navigationView.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            drawerLayout?.closeDrawers()

            when (menuItem.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this@ManageMyScreenActivity, MainActivity::class.java))
                }
                R.id.nav_notifications -> startActivity(
                    Intent(
                        this@ManageMyScreenActivity,
                        ManageNotificationActivity::class.java
                    )
                )
                R.id.nav_in_app -> startActivity(
                    Intent(
                        this@ManageMyScreenActivity,
                        PremiumActivity::class.java
                    )
                )
                R.id.nav_help -> startActivity(
                    Intent(
                        this@ManageMyScreenActivity,
                        HelpActivity::class.java
                    )
                )
            }

            drawerLayout?.closeDrawer(GravityCompat.START)
            true
        }

        //endregion

        //region ======================================== Listeners =========================================

        //region ======================================= ItemSelected =======================================

        manageMyScreenImageContactByLine1?.setOnClickListener {
            manageMyScreenImageContactByLine1?.setBackgroundResource(R.drawable.border_selected_yellow)
            manageMyScreenImageContactByLine4?.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenImageContactByLine5?.setBackgroundResource(R.drawable.border_selected_grey)

            val sharedPreferences: SharedPreferences =
                getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
            val edit: SharedPreferences.Editor = sharedPreferences.edit()
            edit.putInt("gridview", 1)
            edit.apply()

            buildMaterialAlertDialogBuilder()
        }

        manageMyScreenImageContactByLine4?.setOnClickListener {
            manageMyScreenImageContactByLine1?.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenImageContactByLine4?.setBackgroundResource(R.drawable.border_selected_yellow)
            manageMyScreenImageContactByLine5?.setBackgroundResource(R.drawable.border_selected_grey)

            val sharedPreferences: SharedPreferences =
                getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
            val edit: SharedPreferences.Editor = sharedPreferences.edit()
            edit.putInt("gridview", 4)
            edit.apply()

            buildMaterialAlertDialogBuilder()
        }

        manageMyScreenImageContactByLine5?.setOnClickListener {
            manageMyScreenImageContactByLine1?.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenImageContactByLine4?.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenImageContactByLine5?.setBackgroundResource(R.drawable.border_selected_yellow)

            val sharedPreferences: SharedPreferences =
                getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
            val edit: SharedPreferences.Editor = sharedPreferences.edit()
            edit.putInt("gridview", 5)
            edit.apply()

            buildMaterialAlertDialogBuilder()
        }

        val colorList = resources.getStringArray(R.array.manage_my_screen_spinner_array)
        val colorsSpinnerAdapter = ArrayAdapter(this, R.layout.spinner_item, colorList)
        manageMyScreenSpinnerSelectColor?.adapter = colorsSpinnerAdapter
        manageMyScreenSpinnerSelectColor?.setSelection(IsMultiColor)
        manageMyScreenSpinnerSelectColor?.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    when (position) {
                        0 -> {
                            if (IsMultiColor == 1) {
                                buildMaterialAlertDialogBuilder()
                            }

                            val edit: SharedPreferences.Editor =
                                sharedPreferencesIsMultiColor.edit()
                            edit.putInt("IsMultiColor", position)
                            edit.apply()
                            IsMultiColor = 0

                            manageMyScreenButtonSelectColorLayout?.visibility = View.INVISIBLE
                        }
                        1 -> {
                            val edit: SharedPreferences.Editor =
                                sharedPreferencesIsMultiColor.edit()
                            edit.putInt("IsMultiColor", position)
                            edit.apply()
                            IsMultiColor = 1

                            manageMyScreenButtonSelectColorLayout?.visibility = View.VISIBLE
                        }
                    }
                }
            }

        manageMyScreenColorContactBlueIndigo?.setOnClickListener {
            manageMyScreenColorContactBlueIndigo?.setBackgroundResource(R.drawable.border_selected_yellow)
            manageMyScreenColorContactGreenLime?.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactPurpleGrape?.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactRed?.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactGrey?.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactOrange?.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactCyanTeal?.setBackgroundResource(R.drawable.border_selected_grey)

            val edit: SharedPreferences.Editor = sharedPreferencesContactsColor.edit()
            edit.putInt("contactsColor", 0)
            edit.apply()

            buildMaterialAlertDialogBuilder()
        }

        manageMyScreenColorContactGreenLime?.setOnClickListener {

            manageMyScreenColorContactBlueIndigo?.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactGreenLime?.setBackgroundResource(R.drawable.border_selected_yellow)
            manageMyScreenColorContactPurpleGrape?.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactRed?.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactGrey?.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactOrange?.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactCyanTeal?.setBackgroundResource(R.drawable.border_selected_grey)

            val edit: SharedPreferences.Editor = sharedPreferencesContactsColor.edit()
            edit.putInt("contactsColor", 1)
            edit.apply()

            buildMaterialAlertDialogBuilder()
        }

        manageMyScreenColorContactPurpleGrape?.setOnClickListener {

            manageMyScreenColorContactBlueIndigo?.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactGreenLime?.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactPurpleGrape?.setBackgroundResource(R.drawable.border_selected_yellow)
            manageMyScreenColorContactRed?.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactGrey?.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactOrange?.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactCyanTeal?.setBackgroundResource(R.drawable.border_selected_grey)

            val edit: SharedPreferences.Editor = sharedPreferencesContactsColor.edit()
            edit.putInt("contactsColor", 2)
            edit.apply()

            buildMaterialAlertDialogBuilder()
        }

        manageMyScreenColorContactRed?.setOnClickListener {

            manageMyScreenColorContactBlueIndigo?.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactGreenLime?.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactPurpleGrape?.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactRed?.setBackgroundResource(R.drawable.border_selected_yellow)
            manageMyScreenColorContactGrey?.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactOrange?.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactCyanTeal?.setBackgroundResource(R.drawable.border_selected_grey)

            val edit: SharedPreferences.Editor = sharedPreferencesContactsColor.edit()
            edit.putInt("contactsColor", 3)
            edit.apply()

            buildMaterialAlertDialogBuilder()
        }

        manageMyScreenColorContactGrey?.setOnClickListener {

            manageMyScreenColorContactBlueIndigo?.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactGreenLime?.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactPurpleGrape?.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactRed?.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactGrey?.setBackgroundResource(R.drawable.border_selected_yellow)
            manageMyScreenColorContactOrange?.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactCyanTeal?.setBackgroundResource(R.drawable.border_selected_grey)

            val edit: SharedPreferences.Editor = sharedPreferencesContactsColor.edit()
            edit.putInt("contactsColor", 4)
            edit.apply()

            buildMaterialAlertDialogBuilder()
        }

        manageMyScreenColorContactOrange?.setOnClickListener {
            manageMyScreenColorContactBlueIndigo?.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactGreenLime?.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactPurpleGrape?.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactRed?.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactGrey?.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactOrange?.setBackgroundResource(R.drawable.border_selected_yellow)
            manageMyScreenColorContactCyanTeal?.setBackgroundResource(R.drawable.border_selected_grey)

            val edit: SharedPreferences.Editor = sharedPreferencesContactsColor.edit()
            edit.putInt("contactsColor", 5)
            edit.apply()

            buildMaterialAlertDialogBuilder()
        }

        manageMyScreenColorContactCyanTeal?.setOnClickListener {
            manageMyScreenColorContactBlueIndigo?.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactGreenLime?.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactPurpleGrape?.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactRed?.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactGrey?.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactOrange?.setBackgroundResource(R.drawable.border_selected_grey)
            manageMyScreenColorContactCyanTeal?.setBackgroundResource(R.drawable.border_selected_yellow)

            val edit: SharedPreferences.Editor = sharedPreferencesContactsColor.edit()
            edit.putInt("contactsColor", 6)
            edit.apply()

            buildMaterialAlertDialogBuilder()
        }

        //endregion

        // endregion
    }

    private fun checkTheme(sharedThemePreferences: SharedPreferences) {
        val switchTheme = findViewById<SwitchCompat>(R.id.switch_theme)
        switchTheme.isChecked = sharedThemePreferences.getBoolean("darkTheme", false)

        switchTheme.setOnCheckedChangeListener { compoundButton, isChecked ->
            changeSwitchCompatColor(switchTheme)
            val editTheme = sharedThemePreferences.edit()
            editTheme.putBoolean("darkTheme", isChecked)
            editTheme.apply()
            refreshActivity()
        }
    }

    private fun changeSwitchCompatColor(switchTheme: SwitchCompat) {
        val states = arrayOf(
            intArrayOf(-android.R.attr.state_checked),
            intArrayOf(android.R.attr.state_checked)
        )

        lateinit var thumbColors: IntArray
        lateinit var trackColors: IntArray

        val sharedThemePreferences = getSharedPreferences("Knockin_Theme", Context.MODE_PRIVATE)
        if (sharedThemePreferences.getBoolean("darkTheme", false)) {
            thumbColors = intArrayOf(
                Color.WHITE,
                Color.CYAN
            )

            trackColors = intArrayOf(
                Color.LTGRAY,
                Color.argb(120, 3, 214, 194)
            )
        } else {
            thumbColors = intArrayOf(
                Color.LTGRAY,
                Color.CYAN
            )

            trackColors = intArrayOf(
                Color.LTGRAY,
                Color.argb(120, 3, 214, 194)
            )
        }

        DrawableCompat.setTintList(
            DrawableCompat.wrap(switchTheme.thumbDrawable),
            ColorStateList(states, thumbColors)
        )
        DrawableCompat.setTintList(
            DrawableCompat.wrap(switchTheme.trackDrawable),
            ColorStateList(states, trackColors)
        )
    }

    //region ========================================== Functions ===========================================

    private fun refreshActivity() {
        startActivity(
            Intent(this@ManageMyScreenActivity, ManageMyScreenActivity::class.java).addFlags(
                Intent.FLAG_ACTIVITY_NO_ANIMATION
            ).putExtra("ChangeTheme", true)
        )
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.toolbar_menu_help, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                drawerLayout?.openDrawer(GravityCompat.START)
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
            .setTitle(getString(R.string.manage_my_screen_display_contacts_edit_alert_dialog_title))
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