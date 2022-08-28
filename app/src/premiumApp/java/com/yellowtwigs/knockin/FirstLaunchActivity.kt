package com.yellowtwigs.knockin

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.yellowtwigs.knockin.ui.first_launch.start.StartActivity

class FirstLaunchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_launch)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT

        val buttonAccept: Button = findViewById(R.id.first_launch_accept_politique)
        val textView = findViewById<TextView>(R.id.first_launch_welcome)
//        textView.setText(
//            String.format(
//                getString(
//                    R.string.first_launch_welcome,
//                    getString(R.string.app_name)
//                )
//            )
//        )
        val textViewCLUF = findViewById<TextView>(R.id.first_launch_politique)
        textViewCLUF.movementMethod = LinkMovementMethod.getInstance()

        buttonAccept.setOnClickListener {
            startActivity(Intent(this@FirstLaunchActivity, StartActivity::class.java))
            finish()
        }
    }

    override fun onBackPressed() {
    }
}