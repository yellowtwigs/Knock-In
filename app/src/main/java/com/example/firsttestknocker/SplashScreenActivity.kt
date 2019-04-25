package com.example.firsttestknocker

import android.content.Intent
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.os.Bundle

class SplashScreenActivity : AppCompatActivity() {

    private val SPLASH_DISPLAY_LENGHT = 2000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        //        getSupportActionBar().hide();

        Handler().postDelayed({
            val loginIntent = Intent(this@SplashScreenActivity, MainActivity::class.java)
            startActivity(loginIntent)
            finish()
        }, SPLASH_DISPLAY_LENGHT.toLong())
    }
}
