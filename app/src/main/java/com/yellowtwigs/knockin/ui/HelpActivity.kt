package com.yellowtwigs.knockin.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.RelativeLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.yellowtwigs.knockin.R
import com.google.android.material.navigation.NavigationView
import com.yellowtwigs.knockin.ui.contacts.list.ContactsListActivity
import com.yellowtwigs.knockin.ui.first_launch.start.ImportContactsViewModel
import com.yellowtwigs.knockin.ui.premium.PremiumActivity
import com.yellowtwigs.knockin.ui.settings.ManageMyScreenActivity
import com.yellowtwigs.knockin.ui.notifications.settings.NotificationsSettingsActivity
import com.yellowtwigs.knockin.ui.statistics.dashboard.DashboardActivity
import com.yellowtwigs.knockin.ui.teleworking.TeleworkingActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HelpActivity : AppCompatActivity(), SensorEventListener {

    //region ========================================== Val or Var ==========================================

    private var helpActivityVideoTutorial: RelativeLayout? = null
    private var helpActivityWebsiteTutorial: RelativeLayout? = null
    private var helpActivityContactUs: RelativeLayout? = null
    private var helpActivityTerms: RelativeLayout? = null
    private var helpActivityInfos: RelativeLayout? = null
    private var help_activity_BubblesNotifications: RelativeLayout? = null
    private var help_activity_DrawerLayout: DrawerLayout? = null
    private var sensorManager: SensorManager? = null

    private var helpActivityLayout: ConstraintLayout? = null
    private var helpActivityWebView: WebView? = null

    private val importContactsViewModel: ImportContactsViewModel by viewModels()

    val webViewClient: WebViewClient = object : WebViewClient() {

    }

    //endregion

    @SuppressLint("IntentReset")
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

        setContentView(R.layout.activity_help)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT

        //region ========================================== Toolbar =========================================

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar
        actionbar?.setDisplayHomeAsUpEnabled(true)
        actionbar?.setHomeAsUpIndicator(R.drawable.ic_open_drawer)

        //endregion

        //region ========================================== Sensor Manager =========================================

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensors = sensorManager?.getSensorList(Sensor.TYPE_ALL)
        if (sensors != null) {
            for (sensor in sensors) {
                Log.i("DEBUG", sensor.name + " --- " + sensor.vendor)
            }
        }

        //endregion

        //region ======================================= FindViewById =======================================

        helpActivityVideoTutorial = findViewById(R.id.help_activity_tutorial_video)
        helpActivityWebsiteTutorial = findViewById(R.id.help_activity_tutorial_website)
        help_activity_BubblesNotifications = findViewById(R.id.help_activity_bubbles_notifications)
        helpActivityContactUs = findViewById(R.id.help_activity_contact_us_id)
        helpActivityTerms = findViewById(R.id.help_activity_terms_id)
        helpActivityInfos = findViewById(R.id.help_activity_infos_id)

        helpActivityLayout = findViewById(R.id.help_activity_tutorial_layout)
        helpActivityWebView = findViewById(R.id.help_activity_web_view)
        helpActivityWebView?.webViewClient = webViewClient

        val settings = helpActivityWebView?.settings
        settings?.javaScriptEnabled = true

        //endregion

        //region ======================================= DrawerLayout =======================================

        help_activity_DrawerLayout = findViewById(R.id.help_drawer_layout)

        val navigationView = findViewById<NavigationView>(R.id.help_nav_view)
        val menu = navigationView.menu
        val navItem = menu.findItem(R.id.nav_help)
        navItem.isChecked = true

        navigationView.setNavigationItemSelectedListener { menuItem ->
            if (menuItem.itemId != R.id.nav_sync_contact && menuItem.itemId != R.id.nav_invite_friend) {
                menuItem.isChecked = true
            }
            help_activity_DrawerLayout?.closeDrawers()

            when (menuItem.itemId) {
                R.id.nav_home -> {
                    startActivity(
                        Intent(
                            this@HelpActivity,
                            ContactsListActivity::class.java
                        )
                    )
                }
                R.id.nav_dashboard -> startActivity(Intent(this@HelpActivity, DashboardActivity::class.java))
                R.id.nav_notifications -> startActivity(
                    Intent(
                        this@HelpActivity,
                        NotificationsSettingsActivity::class.java
                    )
                )
                R.id.nav_teleworking -> startActivity(
                    Intent(this@HelpActivity, TeleworkingActivity::class.java)
                )
                R.id.nav_manage_screen -> startActivity(
                    Intent(
                        this@HelpActivity,
                        ManageMyScreenActivity::class.java
                    )
                )
                R.id.nav_help -> startActivity(Intent(this@HelpActivity, HelpActivity::class.java))
                R.id.nav_in_app -> startActivity(
                    Intent(
                        this@HelpActivity,
                        PremiumActivity::class.java
                    )
                )
                R.id.nav_sync_contact -> {
                    importContacts()
                }
                R.id.nav_invite_friend -> {
                    val intent = Intent(Intent.ACTION_SEND)
                    val messageString =
                        resources.getString(R.string.invite_friend_text) + " \n" + resources.getString(
                            R.string.location_on_playstore
                        )
                    intent.putExtra(Intent.EXTRA_TEXT, messageString)
                    intent.type = "text/plain"
                    val messageIntent = Intent.createChooser(intent, null)
                    startActivity(messageIntent)
                }
            }

            help_activity_DrawerLayout?.closeDrawer(GravityCompat.START)
            true
        }

        //endregion

        //region ==================================== SetOnClickListener ====================================

        val onClick = View.OnClickListener {
            if (it.id == helpActivityWebsiteTutorial?.id) {
//                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.help_contact_us_link))))
                helpActivityLayout?.visibility = View.GONE
                helpActivityWebView?.visibility = View.VISIBLE
                helpActivityWebView?.loadUrl(getString(R.string.help_contact_us_link))
            }
            if (it.id == help_activity_BubblesNotifications?.id) {
//                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.help_bubbles_link))))
                helpActivityLayout?.visibility = View.GONE
                helpActivityWebView?.visibility = View.VISIBLE
                helpActivityWebView?.loadUrl(getString(R.string.help_bubbles_link))
            }
            if (it.id == helpActivityContactUs?.id) {
                val intent = Intent(Intent.ACTION_SEND)
                intent.data = Uri.parse("mailto:")
                intent.type = "text/html"
                intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.contact_mail)))
                intent.putExtra(Intent.EXTRA_SUBJECT, "")
                intent.putExtra(Intent.EXTRA_TEXT, "")
                startActivity(
                    Intent.createChooser(
                        intent,
                        getString(R.string.help_contact_us_intent)
                    )
                )
            }
            if (it.id == helpActivityTerms?.id) {
//                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.help_contact_us_eula))))
                helpActivityLayout?.visibility = View.GONE
                helpActivityWebView?.visibility = View.VISIBLE
                helpActivityWebView?.loadUrl(getString(R.string.help_contact_us_eula))
            }
            if (it.id == helpActivityInfos?.id) {
//                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.yellowtwigs.com")))
                helpActivityLayout?.visibility = View.GONE
                helpActivityWebView?.visibility = View.VISIBLE
                helpActivityWebView?.loadUrl("https://www.yellowtwigs.com")
            }
            if (it.id == helpActivityVideoTutorial?.id) {
//                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.help_app_video_link))))
                helpActivityLayout?.visibility = View.GONE
                helpActivityWebView?.visibility = View.VISIBLE
                helpActivityWebView?.loadUrl(getString(R.string.help_app_video_link))
            }
        }
        helpActivityContactUs?.setOnClickListener(onClick)
        helpActivityInfos?.setOnClickListener(onClick)
        helpActivityWebsiteTutorial?.setOnClickListener(onClick)
        help_activity_BubblesNotifications?.setOnClickListener(onClick)
        helpActivityTerms?.setOnClickListener(onClick)
        helpActivityVideoTutorial?.setOnClickListener(onClick)


        //endregion
    }

    private fun importContacts() {
        CoroutineScope(Dispatchers.Default).launch {
            importContactsViewModel.syncAllContactsInDatabase(contentResolver)
        }
    }

    //region ========================================== Override ============================================

    override fun onBackPressed() {
        if (helpActivityWebView?.visibility == View.VISIBLE) {
            startActivity(Intent(this@HelpActivity, HelpActivity::class.java))
        } else {
            super.onBackPressed()
        }
    }

    //endregion

    //region ========================================== Functions ===========================================

    override fun onResume() {
        super.onResume()
        helpActivityLayout?.visibility = View.VISIBLE
        helpActivityWebView?.visibility = View.GONE
//        sensorManager?.registerListener(this., sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER))
    }

    override fun onPause() {
        super.onPause()
    }

    /**
     * Called when there is a new sensor event.  Note that "on changed"
     * is somewhat of a misnomer, as this will also be called if we have a
     * new reading from a sensor with the exact same sensor values (but a
     * newer timestamp).
     *
     *
     * See [SensorManager][android.hardware.SensorManager]
     * for details on possible sensor types.
     *
     * See also [SensorEvent][android.hardware.SensorEvent].
     *
     *
     * **NOTE:** The application doesn't own the
     * [event][android.hardware.SensorEvent]
     * object passed as a parameter and therefore cannot hold on to it.
     * The object may be part of an internal pool and may be reused by
     * the framework.
     *
     * @param event the [SensorEvent][android.hardware.SensorEvent].
     */
    override fun onSensorChanged(event: SensorEvent?) {
        event?.apply {
            val x = values?.get(0)
            val y = values?.get(1)
            val z = values?.get(2)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                help_activity_DrawerLayout?.openDrawer(GravityCompat.START)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //endregion
}
