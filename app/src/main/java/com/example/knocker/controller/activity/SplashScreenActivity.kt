package com.example.knocker.controller.activity

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.knocker.R

/**
 * La Classe qui permet d'afficher le splashscreen lors du lancement de l'application
 * @author Kenzy Suon
 */
class SplashScreenActivity : AppCompatActivity() {

    private val SPLASH_DISPLAY_LENGHT = 0.1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT

        Handler().postDelayed({
            val loginIntent = Intent(this@SplashScreenActivity, MainActivity::class.java)
            startActivity(loginIntent)
            finish()
        }, SPLASH_DISPLAY_LENGHT.toLong())
    }
}
