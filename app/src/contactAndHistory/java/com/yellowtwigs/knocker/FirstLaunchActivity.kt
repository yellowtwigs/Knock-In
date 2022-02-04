package com.yellowtwigs.Knockin

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Point
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.yellowtwigs.Knockin.controller.activity.firstLaunch.ImportContactsActivity

class FirstLaunchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val height = size.y

        when {
            height > 2500 -> setContentView(R.layout.activity_first_launch_bigger)
            height in 2000..2499 -> setContentView(R.layout.activity_first_launch)
            height in 1100..1999 -> setContentView(R.layout.activity_first_launch_smaller_screen)
            height < 1100 -> setContentView(R.layout.activity_first_launch_mini_screen)
        }

        println(height)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT

        val buttonAccept: Button = findViewById(R.id.first_launch_accept_politique)
        val sharedFirstLaunch = getSharedPreferences("FirstLaunch", Context.MODE_PRIVATE)
        val edit = sharedFirstLaunch.edit()
        val textViewCLUF = findViewById<TextView>(R.id.first_launch_politique)
        textViewCLUF.movementMethod = LinkMovementMethod.getInstance()

        buttonAccept.setOnClickListener {
            edit.putBoolean("first_launch", true)
            edit.apply()
            startActivity(Intent(this@FirstLaunchActivity, ImportContactsActivity::class.java))
            finish()
        }
    }

    override fun onBackPressed() {
    }
}