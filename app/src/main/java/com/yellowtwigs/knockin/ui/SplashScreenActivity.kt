package com.yellowtwigs.knockin.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Point
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import com.yellowtwigs.knockin.FirstLaunchActivity
import com.yellowtwigs.knockin.R

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {

    //region ========================================= Val or Var ===========================================

    private var splashScreenActivityAppNameCenter: TextView? = null
    private var splashScreenActivityAppNameDown: TextView? = null
    private var splashScreenActivityYellowTwigs: TextView? = null
    private var splashScreenActivitySubtitle: TextView? = null

    private var splashScreenActivityAppIcon: AppCompatImageView? = null

    private val SPLASH_DISPLAY_LENGHT = 1450
    private val SPLASH_DISPLAY_LENGHT_ANIMATION = 1000
    private val SPLASH_DISPLAY_LENGHT_INTENT = 3000

    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //region ======================================== Theme Dark ========================================

        //On affecte le Thème light ou le dark en fonction de ce que l'utilisateur à choisi
        //Ce thème est enregistré dans une sharedPreferences c'est un fichier android qui est sauvegardé par l'application
        val sharedThemePreferences = getSharedPreferences("Knockin_Theme", Context.MODE_PRIVATE)
        if (sharedThemePreferences.getBoolean("darkTheme", false)) {
            setTheme(R.style.AppThemeDark)
        } else {
            setTheme(R.style.AppTheme)
        }

        //endregion

        setContentView()

        //region ======================================== Animation =========================================

        splashScreenActivityAppNameCenter = findViewById(R.id.splashscreen_activity_app_name_center)
        splashScreenActivityAppNameDown = findViewById(R.id.splashscreen_activity_app_name_down)
        splashScreenActivityYellowTwigs = findViewById(R.id.splashscreen_activity_yellowtwigs)
        splashScreenActivitySubtitle = findViewById(R.id.splashscreen_activity_subtitle)
        splashScreenActivityAppIcon = findViewById(R.id.splashscreen_activity_app_icon)

        //endregion

        val sharedFromSplashScreen = getSharedPreferences("fromSplashScreen", Context.MODE_PRIVATE)
        sharedFromSplashScreen.getBoolean("fromSplashScreen", false)
        val edit = sharedFromSplashScreen.edit()
        edit.putBoolean("fromSplashScreen", true)
        edit.apply()

        //region ======================================== Animation =========================================

        val slideDown = AnimationUtils.loadAnimation(
            this,
            R.anim.slide_to_down
        )

        val reappartion = AnimationUtils.loadAnimation(
            this,
            R.anim.reapparrition
        )

        Handler().postDelayed({
            splashScreenActivityAppNameCenter!!.startAnimation(slideDown)
        }, SPLASH_DISPLAY_LENGHT_ANIMATION.toLong())


        Handler().postDelayed({
            splashScreenActivityAppNameCenter!!.visibility = View.INVISIBLE
            splashScreenActivityAppNameDown!!.visibility = View.VISIBLE

            splashScreenActivityAppIcon!!.startAnimation(reappartion)
            splashScreenActivityAppIcon!!.visibility = View.VISIBLE
            splashScreenActivityYellowTwigs!!.startAnimation(reappartion)
            splashScreenActivityYellowTwigs!!.visibility = View.VISIBLE
            splashScreenActivitySubtitle!!.startAnimation(reappartion)
            splashScreenActivitySubtitle!!.visibility = View.VISIBLE
        }, SPLASH_DISPLAY_LENGHT.toLong())


        Handler().postDelayed({
            startActivity(Intent(this@SplashScreenActivity, FirstLaunchActivity::class.java))
            finish()
        }, SPLASH_DISPLAY_LENGHT_INTENT.toLong())

        //endregion
    }

    private fun setContentView() {
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val height = size.y

        when {
            height > 2500 -> setContentView(R.layout.activity_splash_screen)
            height in 1800..2499 -> setContentView(R.layout.activity_splash_screen)
            height in 1100..1799 -> setContentView(R.layout.activity_splash_screen_smaller_screen)
            height < 1099 -> setContentView(R.layout.activity_splash_screen_mini_screen)
        }
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
    }
}