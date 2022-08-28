package com.yellowtwigs.knockin

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Point
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.yellowtwigs.knockin.ui.contacts.Main2Activity
import com.yellowtwigs.knockin.ui.first_launch.start.StartActivity

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
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT

        //region ======================================= First Launch =======================================

        val sharedFirstLaunch = getSharedPreferences("FirstLaunch", Context.MODE_PRIVATE)
        if (sharedFirstLaunch.getBoolean("First_Launch", false)) {
            startActivity(Intent(this@FirstLaunchActivity, Main2Activity::class.java))
            finish()
        }

        //endregion

        val buttonAccept: Button = findViewById(R.id.first_launch_accept_politique)
        val edit = sharedFirstLaunch.edit()
        val textView = findViewById<TextView>(R.id.first_launch_welcome)
        textView.text = String.format(
            getString(
                R.string.first_launch_welcome,
                getString(R.string.app_name)
            )
        )
        val textViewCLUF = findViewById<TextView>(R.id.first_launch_politique)
        textViewCLUF.movementMethod = LinkMovementMethod.getInstance()

        buttonAccept.setOnClickListener {
            edit.putBoolean("First_Launch", true)
            edit.apply()
            startActivity(Intent(this@FirstLaunchActivity, StartActivity::class.java))
            finish()
        }
    }

    override fun onBackPressed() {
    }
}