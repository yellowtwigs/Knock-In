package com.yellowtwigs.knockin.controller.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*

import com.yellowtwigs.knockin.R
import com.google.android.material.navigation.NavigationView

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.yellowtwigs.knockin.controller.activity.group.GroupManagerActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * La Classe qui permet de changer le theme de l'application et de changer le nombre de contact par ligne
 * @author Florian Striebel, Kenzy Suon, Ryan Granet
 */
class ManageMyScreenActivity : AppCompatActivity() {

    //region ========================================== Val or Var ==========================================

    private var drawerLayout: DrawerLayout? = null
    private var tv_zero: AppCompatImageView? = null
    private var tv_one: AppCompatImageView? = null
    private var tv_three: AppCompatImageView? = null
    private var tv_four: AppCompatImageView? = null
    private var tv_five: AppCompatImageView? = null
    private var tv_six: AppCompatImageView? = null

    //  private var //tv_zero_group: ImageView? = null
    //   private var tv_one_group: ImageView? = null
    private var tv_three_group: AppCompatImageView? = null
    private var tv_four_group: AppCompatImageView? = null
    private var tv_five_group: AppCompatImageView? = null
    private var tv_six_group: AppCompatImageView? = null
    private var nbGrid: Int = 4
    private var nbGrid_group: Int = 4

    private var manage_theme_SwitchTheme: Switch? = null

    private var manage_screen_dissociate_cl: ConstraintLayout? = null
    private var manage_screen_dissociate_textView: TextView? = null
    private var manage_screen_dissociate: Boolean? = null
    private var manage_screen_dissociate_switch: Switch? = null

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

        val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
        nbGrid = sharedPreferences.getInt("gridview", 4)

        val sharedPreferences_group = getSharedPreferences("group", Context.MODE_PRIVATE)
        nbGrid_group = sharedPreferences_group.getInt("gridview", 4)

        manage_screen_dissociate = sharedPreferences.getBoolean("dissociate_screen", false)

        //region ======================================= FindViewById =======================================

        manage_screen_dissociate_cl = findViewById(R.id.activity_settings_change_nb_contact_group_cl)
        manage_screen_dissociate_textView = findViewById(R.id.activity_settings_change_nb_contact_group)
        manage_screen_dissociate_switch = findViewById(R.id.manage_multi_row_switch)

        tv_zero = findViewById(R.id.activity_settings_imageView_0_contact)
        tv_one = findViewById(R.id.activity_settings_imageView_1_contact)
        tv_three = findViewById(R.id.activity_settings_imageView_3_contact)
        tv_four = findViewById(R.id.activity_settings_imageView_4_contact)
        tv_five = findViewById(R.id.activity_settings_imageView_5_contact)
        tv_six = findViewById(R.id.activity_settings_imageView_6_contact)

        // //tv_zero_group = findViewById(R.id.activity_settings_imageView_0_contact_group)
        // tv_one_group = findViewById(R.id.activity_settings_imageView_1_contact_group)
        tv_three_group = findViewById(R.id.activity_settings_imageView_3_contact_group)
        tv_four_group = findViewById(R.id.activity_settings_imageView_4_contact_group)
        tv_five_group = findViewById(R.id.activity_settings_imageView_5_contact_group)
        tv_six_group = findViewById(R.id.activity_settings_imageView_6_contact_group)

        manage_theme_SwitchTheme = findViewById(R.id.manage_theme_switch)

        //endregion

        //region ===================================== SetImageResource =====================================

        tv_six!!.setImageResource(R.drawable.contactbyline6)
        tv_five!!.setImageResource(R.drawable.contactbyline5)
        tv_four!!.setImageResource(R.drawable.contactbyline4)
        tv_three!!.setImageResource(R.drawable.contactbyline3)
        tv_one!!.setImageResource(R.drawable.contactbyline1)
        tv_zero!!.setImageResource(R.drawable.contactbyline0)

        //tv_zero_group!!.setImageResource(R.drawable.contactbyline0)
        //tv_one_group!!.setImageResource(R.drawable.contactbyline1)
        tv_three_group!!.setImageResource(R.drawable.contactbyline3)
        tv_four_group!!.setImageResource(R.drawable.contactbyline4)
        tv_five_group!!.setImageResource(R.drawable.contactbyline5)
        tv_six_group!!.setImageResource(R.drawable.contactbyline6)

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
//                R.id.nav_in_app -> startActivity(Intent(this@ManageMyScreenActivity, PremiumActivity::class.java))
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

        if (manage_screen_dissociate!!) {
            manage_screen_dissociate_textView!!.visibility = View.VISIBLE
            manage_screen_dissociate_cl!!.visibility = View.VISIBLE
        }
        manage_screen_dissociate_switch!!.isChecked = manage_screen_dissociate!!

        if (sharedThemePreferences.getBoolean("darkTheme", false)) {
            manage_theme_SwitchTheme!!.isChecked = true
        }

        manage_screen_dissociate_switch?.setOnCheckedChangeListener { _, isChecked ->
            val editor = sharedPreferences.edit()
            editor.putBoolean("dissociate_screen", isChecked)
            editor.apply()
            manage_screen_dissociate = isChecked
            if (isChecked) {
                manage_screen_dissociate_textView!!.visibility = View.VISIBLE
                manage_screen_dissociate_cl!!.visibility = View.VISIBLE
            } else {

                manage_screen_dissociate_textView!!.visibility = View.GONE
                manage_screen_dissociate_cl!!.visibility = View.GONE
            }
        }

        //region ==================================== SetOnClickListener ====================================

        when (nbGrid) {
            0 -> tv_zero!!.setBackgroundResource(R.drawable.border_selected_image_view)
            1 -> tv_one!!.setBackgroundResource(R.drawable.border_selected_image_view)
            3 -> tv_three!!.setBackgroundResource(R.drawable.border_selected_image_view)
            4 -> tv_four!!.setBackgroundResource(R.drawable.border_selected_image_view)
            5 -> tv_five!!.setBackgroundResource(R.drawable.border_selected_image_view)
            6 -> tv_six!!.setBackgroundResource(R.drawable.border_selected_image_view)
        }
        when (nbGrid_group) {
            // 0 ->tv_zero_group!!.setBackgroundResource(R.drawable.border_selected_image_view)
            //   1 -> tv_one_group!!.setBackgroundResource(R.drawable.border_selected_image_view)
            3 -> tv_three_group!!.setBackgroundResource(R.drawable.border_selected_image_view)
            4 -> tv_four_group!!.setBackgroundResource(R.drawable.border_selected_image_view)
            5 -> tv_five_group!!.setBackgroundResource(R.drawable.border_selected_image_view)
            6 -> tv_six_group!!.setBackgroundResource(R.drawable.border_selected_image_view)
        }
        tv_zero?.setOnClickListener {
            nbGrid = 0
            val mes = String.format(resources.getString(R.string.manage_my_screen_toast_list_smaller), nbGrid)
            Toast.makeText(applicationContext, mes, Toast.LENGTH_SHORT).show()
            tv_zero!!.setBackgroundResource(R.drawable.border_selected_image_view)
            tv_one!!.background = null
            tv_three!!.background = null
            tv_four!!.background = null
            tv_five!!.background = null
            tv_six!!.background = null
            if (!manage_screen_dissociate!!) {
                changeGridColumnHomeAndGroup()
            } else {
                changeGridColumn()
            }
        }
        tv_one?.setOnClickListener {
            nbGrid = 1
            val mes = String.format(resources.getString(R.string.manage_my_screen_toast_list), nbGrid)
            Toast.makeText(applicationContext, mes, Toast.LENGTH_SHORT).show()
            tv_one!!.setBackgroundResource(R.drawable.border_selected_image_view)
            tv_zero!!.background = null
            tv_three!!.background = null
            tv_four!!.background = null
            tv_five!!.background = null
            tv_six!!.background = null
            if (!manage_screen_dissociate!!) {
                changeGridColumnHomeAndGroup()
            } else {
                changeGridColumn()
            }
        }
        tv_three?.setOnClickListener {
            nbGrid = 3
            val mes = String.format(resources.getString(R.string.manage_my_screen_toast_grid), nbGrid)
            Toast.makeText(applicationContext, mes, Toast.LENGTH_SHORT).show()
            tv_zero!!.background = null
            tv_one!!.background = null
            tv_four!!.background = null
            tv_five!!.background = null
            tv_three!!.setBackgroundResource(R.drawable.border_selected_image_view)
            tv_six!!.background = null
            if (!manage_screen_dissociate!!) {
                changeGridColumnHomeAndGroup()
            } else {
                changeGridColumn()
            }
        }
        tv_four?.setOnClickListener {
            nbGrid = 4
            val mes = String.format(resources.getString(R.string.manage_my_screen_toast_grid), nbGrid)
            Toast.makeText(applicationContext, mes, Toast.LENGTH_SHORT).show()
            tv_zero!!.background = null
            tv_one!!.background = null
            tv_three!!.background = null
            tv_five!!.background = null
            tv_six!!.background = null
            tv_four!!.setBackgroundResource(R.drawable.border_selected_image_view)
            if (!manage_screen_dissociate!!) {
                changeGridColumnHomeAndGroup()
            } else {
                changeGridColumn()
            }
        }
        tv_five?.setOnClickListener {
            nbGrid = 5
            val mes = String.format(resources.getString(R.string.manage_my_screen_toast_grid), nbGrid)
            Toast.makeText(applicationContext, mes, Toast.LENGTH_SHORT).show()
            tv_zero!!.background = null
            tv_one!!.background = null
            tv_three!!.background = null
            tv_four!!.background = null
            tv_six!!.background = null
            tv_five!!.setBackgroundResource(R.drawable.border_selected_image_view)
            if (!manage_screen_dissociate!!) {
                changeGridColumnHomeAndGroup()
            } else {
                changeGridColumn()
            }
        }
        tv_six?.setOnClickListener {
            nbGrid = 6
            val mes = String.format(resources.getString(R.string.manage_my_screen_toast_grid), nbGrid)
            Toast.makeText(applicationContext, mes, Toast.LENGTH_SHORT).show()
            tv_zero!!.background = null
            tv_one!!.background = null
            tv_three!!.background = null
            tv_four!!.background = null
            tv_five!!.background = null
            tv_six!!.setBackgroundResource(R.drawable.border_selected_image_view)
            if (!manage_screen_dissociate!!) {
                changeGridColumnHomeAndGroup()
            } else {
                changeGridColumn()
            }
        }
        /*tv_zero_group?.setOnClickListener {
            nbGrid = 0
            val mes = String.format(resources.getString(R.string.manage_my_screen_toast_list_smaller), nbGrid)
            Toast.makeText(applicationContext, mes, Toast.LENGTH_SHORT).show()
            //tv_zero_group!!.setBackgroundResource(R.drawable.border_selected_image_view)
            tv_one_group!!.background = null
            tv_three_group!!.background = null
            tv_four_group!!.background = null
            tv_five_group!!.background = null
            tv_six_group!!.background = null
            changeGridColumnGroup()
        }*/
        /*tv_one_group?.setOnClickListener {
            nbGrid = 1
            val mes = String.format(resources.getString(R.string.manage_my_screen_toast_list), nbGrid)
            Toast.makeText(applicationContext, mes, Toast.LENGTH_SHORT).show()
            tv_one_group!!.setBackgroundResource(R.drawable.border_selected_image_view)
            //tv_zero_group!!.background = null
            tv_three_group!!.background = null
            tv_four_group!!.background = null
            tv_five_group!!.background = null
            tv_six_group!!.background = null
            changeGridColumnGroup()
        }*/
        tv_three_group?.setOnClickListener {
            nbGrid = 3
            val mes = String.format(resources.getString(R.string.manage_my_screen_toast_grid), nbGrid)
            Toast.makeText(applicationContext, mes, Toast.LENGTH_SHORT).show()
            //tv_zero_group!!.background = null
            // tv_one_group!!.background = null
            tv_four_group!!.background = null
            tv_five_group!!.background = null
            tv_three_group!!.setBackgroundResource(R.drawable.border_selected_image_view)
            tv_six_group!!.background = null
            changeGridColumnGroup()
        }
        tv_four_group?.setOnClickListener {
            nbGrid = 4
            val mes = String.format(resources.getString(R.string.manage_my_screen_toast_grid), nbGrid)
            Toast.makeText(applicationContext, mes, Toast.LENGTH_SHORT).show()
            //tv_zero_group!!.background = null
            //  tv_one_group!!.background = null
            tv_three_group!!.background = null
            tv_five_group!!.background = null
            tv_six_group!!.background = null
            tv_four_group!!.setBackgroundResource(R.drawable.border_selected_image_view)
            changeGridColumnGroup()
        }
        tv_five_group?.setOnClickListener {
            nbGrid = 5
            val mes = String.format(resources.getString(R.string.manage_my_screen_toast_grid), nbGrid)
            Toast.makeText(applicationContext, mes, Toast.LENGTH_SHORT).show()
            //tv_zero_group!!.background = null
            // tv_one_group!!.background = null
            tv_three_group!!.background = null
            tv_four_group!!.background = null
            tv_six_group!!.background = null
            tv_five_group!!.setBackgroundResource(R.drawable.border_selected_image_view)
            changeGridColumnGroup()
        }
        tv_six_group?.setOnClickListener {
            nbGrid = 6
            val mes = String.format(resources.getString(R.string.manage_my_screen_toast_grid), nbGrid)
            Toast.makeText(applicationContext, mes, Toast.LENGTH_SHORT).show()
            //tv_zero_group!!.background = null
            //tv_one_group!!.background = null
            tv_three_group!!.background = null
            tv_four_group!!.background = null
            tv_five_group!!.background = null
            tv_six_group!!.setBackgroundResource(R.drawable.border_selected_image_view)
            changeGridColumnGroup()
        }

        manage_theme_SwitchTheme!!.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                setTheme(R.style.AppThemeDark)

                val edit: SharedPreferences.Editor = sharedThemePreferences.edit()
                edit.putBoolean("darkTheme", true)
                edit.apply()
                refreshActivity()
            } else {
                setTheme(R.style.AppTheme)

                val edit: SharedPreferences.Editor = sharedThemePreferences.edit()
                edit.putBoolean("darkTheme", false)
                edit.apply()

                refreshActivity()
            }
        }

        // endregion
    }
    //region ========================================== Functions ===========================================

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

    private fun changeGridColumn() {
        val loginIntent = Intent(this@ManageMyScreenActivity, MainActivity::class.java)
        val sharedPreferences: SharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
        val edit: SharedPreferences.Editor = sharedPreferences.edit()
        edit.putInt("gridview", nbGrid)
        edit.apply()
        startActivity(loginIntent)
        finish()
    }

    private fun changeGridColumnGroup() {
        val loginIntent = Intent(this@ManageMyScreenActivity, GroupManagerActivity::class.java)
        val sharedPreferencesGroup: SharedPreferences = getSharedPreferences("group", Context.MODE_PRIVATE)
        val editGroup: SharedPreferences.Editor = sharedPreferencesGroup.edit()
        editGroup.putInt("gridview", nbGrid)
        editGroup.apply()
        startActivity(loginIntent)
        finish()
    }


    private fun changeGridColumnHomeAndGroup() {
        val loginIntent = Intent(this@ManageMyScreenActivity, MainActivity::class.java)
        val sharedPreferences: SharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
        val edit: SharedPreferences.Editor = sharedPreferences.edit()
        edit.putInt("gridview", nbGrid)
        edit.apply()

        val sharedPreferencesGroup: SharedPreferences = getSharedPreferences("group", Context.MODE_PRIVATE)
        val editGroup: SharedPreferences.Editor = sharedPreferencesGroup.edit()
        editGroup.putInt("gridview", nbGrid)
        editGroup.apply()

        startActivity(loginIntent)
        finish()
    }
    //endregion
}
