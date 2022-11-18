package com.yellowtwigs.knockin.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import com.yellowtwigs.knockin.FirstLaunchActivity
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.ui.contacts.MainActivity

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {

    //region ========================================= Val or Var ===========================================

    private var splashScreenActivityAppNameCenter: TextView? = null
    private var splashScreenActivityAppNameDown: TextView? = null
    private var splashScreenActivityYellowTwigs: TextView? = null
    private var splashScreenActivitySubtitle: TextView? = null

    private var splashScreenActivityAppIcon: AppCompatImageView? = null

    private val SPLASH_DISPLAY_LENGHT = 965
    private val SPLASH_DISPLAY_LENGHT_ANIMATION = 665
    private val SPLASH_DISPLAY_LENGHT_INTENT = 2000

    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //region ======================================== Theme Dark ========================================

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
            splashScreenActivityAppNameCenter?.startAnimation(slideDown)
        }, SPLASH_DISPLAY_LENGHT_ANIMATION.toLong())


        Handler().postDelayed({
            splashScreenActivityAppNameCenter?.visibility = View.INVISIBLE
            splashScreenActivityAppNameDown?.visibility = View.VISIBLE

            splashScreenActivityAppIcon?.startAnimation(reappartion)
            splashScreenActivityAppIcon?.visibility = View.VISIBLE
            splashScreenActivityYellowTwigs?.startAnimation(reappartion)
            splashScreenActivityYellowTwigs?.visibility = View.VISIBLE
            splashScreenActivitySubtitle?.startAnimation(reappartion)
            splashScreenActivitySubtitle?.visibility = View.VISIBLE
        }, SPLASH_DISPLAY_LENGHT.toLong())

        Handler().postDelayed({
            val sharedFirstLaunch = getSharedPreferences("First_Launch", Context.MODE_PRIVATE)
            if (sharedFirstLaunch.getBoolean("First_Launch", false)) {
                startActivity(Intent(this@SplashScreenActivity, MainActivity::class.java))
            } else {
                startActivity(Intent(this@SplashScreenActivity, FirstLaunchActivity::class.java))
            }
            finish()
        }, SPLASH_DISPLAY_LENGHT_INTENT.toLong())

        //endregion
    }

    private fun setContentView() {
        setContentView(R.layout.activity_splash_screen)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
    }
}