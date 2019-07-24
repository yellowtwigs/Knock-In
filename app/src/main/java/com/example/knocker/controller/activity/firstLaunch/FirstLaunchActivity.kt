package com.example.knocker.controller.activity.firstLaunch

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.widget.Button
import android.widget.TextView
import com.example.knocker.R

class FirstLaunchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_launch)

        val buttonAccept: Button = findViewById(R.id.first_launch_accept_politique)
        val sharedFirstLaunch = getSharedPreferences("FirstLaunch", Context.MODE_PRIVATE)
        val edit = sharedFirstLaunch.edit()
        val textViewCLUF = findViewById<TextView>(R.id.first_launch_politique)
        textViewCLUF.movementMethod = LinkMovementMethod.getInstance()
        //textViewCLUF.setText(Html.fromHtml(this.getString(R.string.first_launch_confidentiality)))

        buttonAccept.setOnClickListener {
            edit.putBoolean("first_launch", false)
            edit.apply()
            startActivity(Intent(this@FirstLaunchActivity, SynchronizeContactActivity::class.java))
            finish()
        }
    }

    override fun onBackPressed() {
    }
}
