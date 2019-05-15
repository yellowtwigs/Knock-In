package com.example.knocker.controller

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.example.knocker.R

class SplashScreenActivity : AppCompatActivity() {

    private val SPLASH_DISPLAY_LENGHT = 2000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT)

        Handler().postDelayed({
            val loginIntent = Intent(this@SplashScreenActivity, MainActivity::class.java)
            startActivity(loginIntent)
            finish()
        }, SPLASH_DISPLAY_LENGHT.toLong())
    }
}
