package com.example.firsttestknocker

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout

class SettingsActivity : AppCompatActivity() {

    private var drawerLayout: DrawerLayout? = null
    private var button_add: Button? = null
    private var button_delete: Button? = null
    private var nbGrid: Int = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
        nbGrid = sharedPreferences.getInt("gridview",3)
        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar.setHomeAsUpIndicator(R.drawable.ic_left_arrow)
        actionbar.title = "Settings"

        // Drawerlayout
        drawerLayout = findViewById(R.id.drawer_layout)
        button_add = findViewById(R.id.activity_settings_add_column)
        button_delete = findViewById(R.id.activity_settings_delete_column)

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            drawerLayout!!.closeDrawers()

            val id = menuItem.itemId

            if (id == R.id.nav_home) {
                val loginIntent = Intent(this@SettingsActivity, MainActivity::class.java)
                loginIntent.putExtra("nbGridview", nbGrid)
                startActivity(loginIntent)
            } else if (id == R.id.nav_settings) {
                val loginIntent = Intent(this@SettingsActivity, SettingsActivity::class.java)
                startActivity(loginIntent)
                finish()
            }

            val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
            drawer.closeDrawer(GravityCompat.START)
            true
        }

        button_add?.setOnClickListener {
            if (nbGrid < 6) {
                nbGrid += 1
            }
        }

        button_delete?.setOnClickListener{
            if (nbGrid > 3) {
                nbGrid -= 1
            }
        }

        val notificationRL = findViewById<View>(R.id.notification_RelativeLayout_settings) as RelativeLayout
        notificationRL.setOnClickListener {
            val notificationSettingsIntent = Intent(this@SettingsActivity, ManageNotificationActivity::class.java)
            startActivity(notificationSettingsIntent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val loginIntent = Intent(this@SettingsActivity, MainActivity::class.java)
        val sharedPreferences: SharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
        val edit : SharedPreferences.Editor = sharedPreferences.edit()
        edit.putInt("gridview", nbGrid)
        edit.apply()
        startActivity(loginIntent)
        finish()
        return super.onOptionsItemSelected(item)
    }
}
