package com.example.knocker.controller.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.example.knocker.R

/**
 * TO DELETE ???
 * @author Florian Striebel, Kenzy Suon, Ryan Granet
 */
class SettingsActivity : AppCompatActivity() {

    private var tv_three: ImageView? = null
    private var tv_four: ImageView? = null
    private var tv_five: ImageView? = null
    private var tv_six: ImageView? = null
    private var nbGrid: Int = 3
    private var knockerTheme: Boolean? = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT

        val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
        nbGrid = sharedPreferences.getInt("gridview", 3)

        val sharedThemePreferences = getSharedPreferences("Knocker_Theme", Context.MODE_PRIVATE)
        knockerTheme = sharedThemePreferences.getBoolean("theme", true)

        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar.setHomeAsUpIndicator(R.drawable.ic_left_arrow)
        actionbar.title = "Settings"

        tv_three = findViewById(R.id.activity_settings_imageView_3_contact)
        tv_four = findViewById(R.id.activity_settings_imageView_4_contact)
        tv_five = findViewById(R.id.activity_settings_imageView_5_contact)
        tv_six = findViewById(R.id.activity_settings_imageView_6_contact)
        tv_six!!.setImageResource(R.drawable.contactbyline6)
        tv_five!!.setImageResource(R.drawable.contactbyline5)
        tv_four!!.setImageResource(R.drawable.contactbyline4)
        tv_three!!.setImageResource(R.drawable.contactbyline3)


        if(nbGrid==3){
            tv_three!!.setBackgroundResource(R.drawable.border_imageview)
        }else if(nbGrid==4){
            tv_four!!.setBackgroundResource(R.drawable.border_imageview)
        }else if(nbGrid==5){
            tv_five!!.setBackgroundResource(R.drawable.border_imageview)
        }else if(nbGrid==6){
            tv_six!!.setBackgroundResource(R.drawable.border_imageview)
        }
        tv_three?.setOnClickListener {
            nbGrid = 3
           // val mes= String.format(resources.getString(R.string.settings_toast),nbGrid)
           // Toast.makeText(applicationContext,mes,Toast.LENGTH_SHORT).show()
            tv_four!!.background=null
            tv_five!!.background=null
            tv_six!!.background=null
            tv_three!!.setBackgroundResource(R.drawable.border_imageview)

        }

        tv_four?.setOnClickListener {
            nbGrid = 4
           // val mes= String.format(resources.getString(R.string.settings_toast),nbGrid)
           // Toast.makeText(applicationContext,mes,Toast.LENGTH_SHORT).show()
            tv_three!!.background=null
            tv_five!!.background=null
            tv_six!!.background=null
            tv_four!!.setBackgroundResource(R.drawable.border_imageview)
        }

        tv_five?.setOnClickListener {
            nbGrid = 5
           // val mes= String.format(resources.getString(R.string.settings_toast),nbGrid)
           // Toast.makeText(applicationContext,mes,Toast.LENGTH_SHORT).show()
            tv_three!!.background=null
            tv_four!!.background=null
            tv_six!!.background=null
            tv_five!!.setBackgroundResource(R.drawable.border_imageview)
        }

        tv_six?.setOnClickListener {
            nbGrid = 6
            //val mes= String.format(resources.getString(R.string.settings_toast),nbGrid)
            //Toast.makeText(applicationContext,mes,Toast.LENGTH_SHORT).show()
            tv_three!!.background=null
            tv_four!!.background=null
            tv_five!!.background=null
            tv_six!!.setBackgroundResource(R.drawable.border_imageview)
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
        val edit: SharedPreferences.Editor = sharedPreferences.edit()
        edit.putInt("gridview", nbGrid)
        edit.apply()
        startActivity(loginIntent)
        finish()
        return super.onOptionsItemSelected(item)
    }
}
