package com.example.knocker.controller.activity.firstLaunch

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.knocker.R

class FirstLaunchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_launch)
        val buttonAccept:Button= findViewById(R.id.first_launch_accept_politique)
        val sharedFirstLaunch = getSharedPreferences("FirstLaunch", Context.MODE_PRIVATE)
        val edit= sharedFirstLaunch.edit()
        buttonAccept.setOnClickListener({
            edit.putBoolean("first_launch",false)
            edit.commit()

            startActivity(Intent(this@FirstLaunchActivity,SynchronyseContactActivity::class.java))
        })
    }
}
