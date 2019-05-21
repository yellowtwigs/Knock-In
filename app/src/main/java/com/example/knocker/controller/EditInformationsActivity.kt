package com.example.knocker.controller

import android.content.Intent
import android.os.Bundle
import android.widget.RelativeLayout

import com.example.knocker.R

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class EditInformationsActivity : AppCompatActivity() {

    private var drawerLayout: DrawerLayout? = null
    private var my_knocker: RelativeLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_informations)

        //region ======================================= DrawerLayout =======================================

        // Drawerlayout
        drawerLayout = findViewById(R.id.drawer_layout)

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        val headerView = navigationView.getHeaderView(0);
        my_knocker = headerView.findViewById(R.id.my_knocker)

        my_knocker!!.setOnClickListener {
            startActivity(Intent(this@EditInformationsActivity, MainActivity::class.java))
        }

        navigationView.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            drawerLayout!!.closeDrawers()

            val id = menuItem.itemId

            if (id == R.id.nav_informations) {
                startActivity(Intent(this@EditInformationsActivity, EditInformationsActivity::class.java))
            } else if (id == R.id.nav_notif_config) {
                startActivity(Intent(this@EditInformationsActivity, ManageNotificationActivity::class.java))
            } else if (id == R.id.nav_screen_size) {
                startActivity(Intent(this@EditInformationsActivity, ManageScreenSizeActivity::class.java))
            } else if (id == R.id.nav_theme) {
                startActivity(Intent(this@EditInformationsActivity, ChatActivity::class.java))
            } else if (id == R.id.nav_data_access) {
                startActivity(Intent(this@EditInformationsActivity, ChatActivity::class.java))
            } else if (id == R.id.nav_knockons) {
                startActivity(Intent(this@EditInformationsActivity, NotificationHistoryActivity::class.java))
            } else if (id == R.id.nav_statistics) {
                startActivity(Intent(this@EditInformationsActivity, NotificationHistoryActivity::class.java))
            } else if (id == R.id.nav_help) {
                startActivity(Intent(this@EditInformationsActivity, NotificationHistoryActivity::class.java))
            }

            val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
            drawer.closeDrawer(GravityCompat.START)
            true
        }
        //endregion

        //region ==================================== SetOnClickListener ====================================

        my_knocker!!.setOnClickListener {
            startActivity(Intent(this@EditInformationsActivity, MainActivity::class.java))
        }

        // endregion
    }
}
