package com.yellowtwigs.knockin.ui.settings

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.GravityCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.ActivityManageMyScreenBinding
import com.yellowtwigs.knockin.ui.HelpActivity
import com.yellowtwigs.knockin.ui.contacts.list.ContactsListActivity
import com.yellowtwigs.knockin.ui.in_app.PremiumActivity
import com.yellowtwigs.knockin.ui.notifications.settings.NotificationsSettingsActivity
import com.yellowtwigs.knockin.ui.teleworking.TeleworkingActivity
import com.yellowtwigs.knockin.utils.EveryActivityUtils.checkTheme
import com.yellowtwigs.knockin.utils.EveryActivityUtils.setupTeleworkingItem

class ManageMyScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManageMyScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkTheme(this)
        binding = ActivityManageMyScreenBinding.inflate(layoutInflater)

        setContentView(binding.root)

        if (intent.getBooleanExtra("ChangeTheme", false)) {
            buildMaterialAlertDialogBuilder()
        }

        setupToolbar()
        setupDrawerLayout()
        setupContactsByLine()
        setupContactsColor()
        switchTheme()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        val actionbar = supportActionBar
        actionbar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.ic_open_drawer)
        }
    }

    private fun setupDrawerLayout() {
        binding.navigationView.menu.findItem(R.id.nav_manage_screen).isChecked = true

        setupTeleworkingItem(binding.navigationView, this)

        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            binding.drawerLayout.closeDrawers()

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

            when (menuItem.itemId) {
                R.id.nav_home -> {
                    startActivity(
                        Intent(
                            this@ManageMyScreenActivity,
                            ContactsListActivity::class.java
                        )
                    )
                }
                R.id.nav_notifications -> startActivity(
                    Intent(
                        this@ManageMyScreenActivity,
                        NotificationsSettingsActivity::class.java
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

            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun setupContactsByLine() {
        val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
        val nbGrid = sharedPreferences.getInt("gridview", 1)
        val edit = sharedPreferences.edit()

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

        binding.apply {
            when (nbGrid) {
                1 -> {
                    contactByLine1.setBackgroundResource(R.drawable.border_selected_yellow)
                    contactByLine4.setBackgroundResource(R.drawable.border_selected_grey)
                    contactByLine5.setBackgroundResource(R.drawable.border_selected_grey)
                }
                4 -> {
                    contactByLine1.setBackgroundResource(R.drawable.border_selected_grey)
                    contactByLine4.setBackgroundResource(R.drawable.border_selected_yellow)
                    contactByLine5.setBackgroundResource(R.drawable.border_selected_grey)
                }
                5 -> {
                    contactByLine1.setBackgroundResource(R.drawable.border_selected_grey)
                    contactByLine4.setBackgroundResource(R.drawable.border_selected_grey)
                    contactByLine5.setBackgroundResource(R.drawable.border_selected_yellow)
                }
            }

            contactByLine1.setImageDrawable(
                AppCompatResources.getDrawable(
                    this@ManageMyScreenActivity,
                    arrayImagesContactByLine1[0]
                )
            )
            contactByLine4.setImageDrawable(
                AppCompatResources.getDrawable(
                    this@ManageMyScreenActivity,
                    arrayImagesContactByLine4[0]
                )
            )
            contactByLine5.setImageDrawable(
                AppCompatResources.getDrawable(
                    this@ManageMyScreenActivity,
                    arrayImagesContactByLine5[0]
                )
            )

            contactByLine1.setOnClickListener {
                contactByLine1.setBackgroundResource(R.drawable.border_selected_yellow)
                contactByLine4.setBackgroundResource(R.drawable.border_selected_grey)
                contactByLine5.setBackgroundResource(R.drawable.border_selected_grey)

                edit.putInt("gridview", 1)
                edit.apply()

                buildMaterialAlertDialogBuilder()
            }

            contactByLine4.setOnClickListener {
                contactByLine1.setBackgroundResource(R.drawable.border_selected_grey)
                contactByLine4.setBackgroundResource(R.drawable.border_selected_yellow)
                contactByLine5.setBackgroundResource(R.drawable.border_selected_grey)

                edit.putInt("gridview", 4)
                edit.apply()

                buildMaterialAlertDialogBuilder()
            }

            contactByLine5.setOnClickListener {
                contactByLine1.setBackgroundResource(R.drawable.border_selected_grey)
                contactByLine4.setBackgroundResource(R.drawable.border_selected_grey)
                contactByLine5.setBackgroundResource(R.drawable.border_selected_yellow)

                edit.putInt("gridview", 5)
                edit.apply()

                buildMaterialAlertDialogBuilder()
            }

        }
    }

    private fun setupContactsColor() {
        val sharedPreferencesIsMultiColor =
            getSharedPreferences("IsMultiColor", Context.MODE_PRIVATE)
        var isMultiColor = sharedPreferencesIsMultiColor.getInt("IsMultiColor", 0)

        val sharedPreferencesContactsColor =
            getSharedPreferences("ContactsColor", Context.MODE_PRIVATE)
        val contactsColor = sharedPreferencesContactsColor.getInt("contactsColor", 0)

        binding.apply {
            when (contactsColor) {
                0 -> {
                    blueIndigo.setBackgroundResource(R.drawable.border_selected_yellow)
                    greenLime.setBackgroundResource(R.drawable.border_selected_grey)
                    purpleGrape.setBackgroundResource(R.drawable.border_selected_grey)
                    red.setBackgroundResource(R.drawable.border_selected_grey)
                    grey.setBackgroundResource(R.drawable.border_selected_grey)
                    orange.setBackgroundResource(R.drawable.border_selected_grey)
                    cyanTeal.setBackgroundResource(R.drawable.border_selected_grey)
                }
                1 -> {
                    blueIndigo.setBackgroundResource(R.drawable.border_selected_grey)
                    greenLime.setBackgroundResource(R.drawable.border_selected_yellow)
                    purpleGrape.setBackgroundResource(R.drawable.border_selected_grey)
                    red.setBackgroundResource(R.drawable.border_selected_grey)
                    grey.setBackgroundResource(R.drawable.border_selected_grey)
                    orange.setBackgroundResource(R.drawable.border_selected_grey)
                    cyanTeal.setBackgroundResource(R.drawable.border_selected_grey)
                }
                2 -> {
                    blueIndigo.setBackgroundResource(R.drawable.border_selected_grey)
                    greenLime.setBackgroundResource(R.drawable.border_selected_grey)
                    purpleGrape.setBackgroundResource(R.drawable.border_selected_yellow)
                    red.setBackgroundResource(R.drawable.border_selected_grey)
                    grey.setBackgroundResource(R.drawable.border_selected_grey)
                    orange.setBackgroundResource(R.drawable.border_selected_grey)
                    cyanTeal.setBackgroundResource(R.drawable.border_selected_grey)
                }
                3 -> {
                    blueIndigo.setBackgroundResource(R.drawable.border_selected_grey)
                    greenLime.setBackgroundResource(R.drawable.border_selected_grey)
                    purpleGrape.setBackgroundResource(R.drawable.border_selected_grey)
                    red.setBackgroundResource(R.drawable.border_selected_yellow)
                    grey.setBackgroundResource(R.drawable.border_selected_grey)
                    orange.setBackgroundResource(R.drawable.border_selected_grey)
                    cyanTeal.setBackgroundResource(R.drawable.border_selected_grey)
                }
                4 -> {
                    blueIndigo.setBackgroundResource(R.drawable.border_selected_grey)
                    greenLime.setBackgroundResource(R.drawable.border_selected_grey)
                    purpleGrape.setBackgroundResource(R.drawable.border_selected_grey)
                    red.setBackgroundResource(R.drawable.border_selected_grey)
                    grey.setBackgroundResource(R.drawable.border_selected_yellow)
                    orange.setBackgroundResource(R.drawable.border_selected_grey)
                    cyanTeal.setBackgroundResource(R.drawable.border_selected_grey)
                }
                5 -> {
                    blueIndigo.setBackgroundResource(R.drawable.border_selected_grey)
                    greenLime.setBackgroundResource(R.drawable.border_selected_grey)
                    purpleGrape.setBackgroundResource(R.drawable.border_selected_grey)
                    red.setBackgroundResource(R.drawable.border_selected_grey)
                    grey.setBackgroundResource(R.drawable.border_selected_grey)
                    orange.setBackgroundResource(R.drawable.border_selected_yellow)
                    cyanTeal.setBackgroundResource(R.drawable.border_selected_grey)
                }
                6 -> {
                    blueIndigo.setBackgroundResource(R.drawable.border_selected_grey)
                    greenLime.setBackgroundResource(R.drawable.border_selected_grey)
                    purpleGrape.setBackgroundResource(R.drawable.border_selected_grey)
                    red.setBackgroundResource(R.drawable.border_selected_grey)
                    grey.setBackgroundResource(R.drawable.border_selected_grey)
                    orange.setBackgroundResource(R.drawable.border_selected_grey)
                    cyanTeal.setBackgroundResource(R.drawable.border_selected_yellow)
                }
            }

            blueIndigo.setImageDrawable(
                AppCompatResources.getDrawable(
                    this@ManageMyScreenActivity,
                    R.drawable.ic_user_blue_indigo1
                )
            )
            greenLime.setImageDrawable(
                AppCompatResources.getDrawable(
                    this@ManageMyScreenActivity,
                    R.drawable.ic_user_green_lime1
                )
            )
            purpleGrape.setImageDrawable(
                AppCompatResources.getDrawable(
                    this@ManageMyScreenActivity,
                    R.drawable.ic_user_purple_grape1
                )
            )
            red.setImageDrawable(
                AppCompatResources.getDrawable(
                    this@ManageMyScreenActivity,
                    R.drawable.ic_user_red
                )
            )
            grey.setImageDrawable(
                AppCompatResources.getDrawable(
                    this@ManageMyScreenActivity,
                    R.drawable.ic_user_grey1
                )
            )
            orange.setImageDrawable(
                AppCompatResources.getDrawable(
                    this@ManageMyScreenActivity,
                    R.drawable.ic_user_orange
                )
            )
            cyanTeal.setImageDrawable(
                AppCompatResources.getDrawable(
                    this@ManageMyScreenActivity,
                    R.drawable.ic_user_cyan_teal
                )
            )

            val colorList = resources.getStringArray(R.array.manage_my_screen_spinner_array)
            val colorsSpinnerAdapter =
                ArrayAdapter(this@ManageMyScreenActivity, R.layout.spinner_item, colorList)
            spinnerColor.adapter = colorsSpinnerAdapter
            spinnerColor.setSelection(isMultiColor)
            spinnerColor.onItemSelectedListener =
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
                                if (isMultiColor == 1) {
                                    buildMaterialAlertDialogBuilder()
                                }

                                val edit: SharedPreferences.Editor =
                                    sharedPreferencesIsMultiColor.edit()
                                edit.putInt("IsMultiColor", position)
                                edit.apply()
                                isMultiColor = 0

                                binding.changeColorLayout.visibility = View.INVISIBLE
                            }
                            1 -> {
                                val edit: SharedPreferences.Editor =
                                    sharedPreferencesIsMultiColor.edit()
                                edit.putInt("IsMultiColor", position)
                                edit.apply()
                                isMultiColor = 1

                                binding.changeColorLayout.visibility = View.VISIBLE
                            }
                        }
                    }
                }

            blueIndigo.setOnClickListener {
                blueIndigo.setBackgroundResource(R.drawable.border_selected_yellow)
                greenLime.setBackgroundResource(R.drawable.border_selected_grey)
                purpleGrape.setBackgroundResource(R.drawable.border_selected_grey)
                red.setBackgroundResource(R.drawable.border_selected_grey)
                grey.setBackgroundResource(R.drawable.border_selected_grey)
                orange.setBackgroundResource(R.drawable.border_selected_grey)
                cyanTeal.setBackgroundResource(R.drawable.border_selected_grey)

                val edit: SharedPreferences.Editor = sharedPreferencesContactsColor.edit()
                edit.putInt("contactsColor", 0)
                edit.apply()

                buildMaterialAlertDialogBuilder()
            }

            greenLime.setOnClickListener {
                blueIndigo.setBackgroundResource(R.drawable.border_selected_grey)
                greenLime.setBackgroundResource(R.drawable.border_selected_yellow)
                purpleGrape.setBackgroundResource(R.drawable.border_selected_grey)
                red.setBackgroundResource(R.drawable.border_selected_grey)
                grey.setBackgroundResource(R.drawable.border_selected_grey)
                orange.setBackgroundResource(R.drawable.border_selected_grey)
                cyanTeal.setBackgroundResource(R.drawable.border_selected_grey)

                val edit: SharedPreferences.Editor = sharedPreferencesContactsColor.edit()
                edit.putInt("contactsColor", 1)
                edit.apply()

                buildMaterialAlertDialogBuilder()
            }

            purpleGrape.setOnClickListener {
                blueIndigo.setBackgroundResource(R.drawable.border_selected_grey)
                greenLime.setBackgroundResource(R.drawable.border_selected_grey)
                purpleGrape.setBackgroundResource(R.drawable.border_selected_yellow)
                red.setBackgroundResource(R.drawable.border_selected_grey)
                grey.setBackgroundResource(R.drawable.border_selected_grey)
                orange.setBackgroundResource(R.drawable.border_selected_grey)
                cyanTeal.setBackgroundResource(R.drawable.border_selected_grey)

                val edit: SharedPreferences.Editor = sharedPreferencesContactsColor.edit()
                edit.putInt("contactsColor", 2)
                edit.apply()

                buildMaterialAlertDialogBuilder()
            }

            red.setOnClickListener {
                blueIndigo.setBackgroundResource(R.drawable.border_selected_grey)
                greenLime.setBackgroundResource(R.drawable.border_selected_grey)
                purpleGrape.setBackgroundResource(R.drawable.border_selected_grey)
                red.setBackgroundResource(R.drawable.border_selected_yellow)
                grey.setBackgroundResource(R.drawable.border_selected_grey)
                orange.setBackgroundResource(R.drawable.border_selected_grey)
                cyanTeal.setBackgroundResource(R.drawable.border_selected_grey)

                val edit: SharedPreferences.Editor = sharedPreferencesContactsColor.edit()
                edit.putInt("contactsColor", 3)
                edit.apply()

                buildMaterialAlertDialogBuilder()
            }

            grey.setOnClickListener {
                blueIndigo.setBackgroundResource(R.drawable.border_selected_grey)
                greenLime.setBackgroundResource(R.drawable.border_selected_grey)
                purpleGrape.setBackgroundResource(R.drawable.border_selected_grey)
                red.setBackgroundResource(R.drawable.border_selected_grey)
                grey.setBackgroundResource(R.drawable.border_selected_yellow)
                orange.setBackgroundResource(R.drawable.border_selected_grey)
                cyanTeal.setBackgroundResource(R.drawable.border_selected_grey)

                val edit: SharedPreferences.Editor = sharedPreferencesContactsColor.edit()
                edit.putInt("contactsColor", 4)
                edit.apply()

                buildMaterialAlertDialogBuilder()
            }

            orange.setOnClickListener {
                blueIndigo.setBackgroundResource(R.drawable.border_selected_grey)
                greenLime.setBackgroundResource(R.drawable.border_selected_grey)
                purpleGrape.setBackgroundResource(R.drawable.border_selected_grey)
                red.setBackgroundResource(R.drawable.border_selected_grey)
                grey.setBackgroundResource(R.drawable.border_selected_grey)
                orange.setBackgroundResource(R.drawable.border_selected_yellow)
                cyanTeal.setBackgroundResource(R.drawable.border_selected_grey)

                val edit: SharedPreferences.Editor = sharedPreferencesContactsColor.edit()
                edit.putInt("contactsColor", 5)
                edit.apply()

                buildMaterialAlertDialogBuilder()
            }

            cyanTeal.setOnClickListener {
                blueIndigo.setBackgroundResource(R.drawable.border_selected_grey)
                greenLime.setBackgroundResource(R.drawable.border_selected_grey)
                purpleGrape.setBackgroundResource(R.drawable.border_selected_grey)
                red.setBackgroundResource(R.drawable.border_selected_grey)
                grey.setBackgroundResource(R.drawable.border_selected_grey)
                orange.setBackgroundResource(R.drawable.border_selected_grey)
                cyanTeal.setBackgroundResource(R.drawable.border_selected_yellow)

                val edit: SharedPreferences.Editor = sharedPreferencesContactsColor.edit()
                edit.putInt("contactsColor", 6)
                edit.apply()

                buildMaterialAlertDialogBuilder()
            }
        }
    }

    private fun switchTheme() {
        val sharedThemePreferences = getSharedPreferences("Knockin_Theme", Context.MODE_PRIVATE)
        val switchTheme = findViewById<SwitchCompat>(R.id.switch_theme)
        switchTheme.isChecked = sharedThemePreferences.getBoolean("darkTheme", false)

        if (sharedThemePreferences.getBoolean("darkTheme", false)) {
            switchTheme.isChecked = true
        }

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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.toolbar_menu_help, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun refreshActivity() {
        startActivity(
            Intent(this@ManageMyScreenActivity, ManageMyScreenActivity::class.java).addFlags(
                Intent.FLAG_ACTIVITY_NO_ANIMATION
            ).putExtra("ChangeTheme", true)
        )
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                binding.drawerLayout.openDrawer(GravityCompat.START)
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
                startActivity(Intent(this@ManageMyScreenActivity, ContactsListActivity::class.java))
            }
            .setNegativeButton(R.string.alert_dialog_no) { _, _ ->
            }
            .show()
    }

    //endregion
}