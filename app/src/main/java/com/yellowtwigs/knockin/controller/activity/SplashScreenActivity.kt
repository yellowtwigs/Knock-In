package com.yellowtwigs.knockin.controller.activity

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import com.yellowtwigs.knockin.FirstLaunchActivity
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.controller.activity.firstLaunch.StartActivity

class SplashScreenActivity : AppCompatActivity() {

    //region ========================================= Val or Var ===========================================

    private var splashScreenActivityAppNameCenter: TextView? = null
    private var splashScreenActivityAppNameDown: TextView? = null
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

        setContentView(R.layout.activity_splash_screen)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT

        //region ======================================== Animation =========================================

        splashScreenActivityAppNameCenter = findViewById(R.id.splashscreen_activity_app_name_center)
        splashScreenActivityAppNameDown = findViewById(R.id.splashscreen_activity_app_name_down)
        splashScreenActivitySubtitle = findViewById(R.id.splashscreen_activity_subtitle)
        splashScreenActivityAppIcon = findViewById(R.id.splashscreen_activity_app_icon)

        //endregion

        //region ======================================== Animation =========================================

        val slideDown = AnimationUtils.loadAnimation(this,
                R.anim.slide_to_down
        )

        val reappartion = AnimationUtils.loadAnimation(this,
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
            splashScreenActivitySubtitle!!.startAnimation(reappartion)
            splashScreenActivitySubtitle!!.visibility = View.VISIBLE
        }, SPLASH_DISPLAY_LENGHT.toLong())


        Handler().postDelayed({
            startActivity(Intent(this@SplashScreenActivity, MainActivity::class.java))
            finish()
        }, SPLASH_DISPLAY_LENGHT_INTENT.toLong())

        //endregion
    }
}