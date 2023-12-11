package com.yellowtwigs.knockin.ui.statistics.daily_statistics

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.MenuItem
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.ActivityDailyStatisticsBinding
import com.yellowtwigs.knockin.databinding.ActivityDailyStatisticsUnder1200Binding
import com.yellowtwigs.knockin.databinding.ActivityDailyStatisticsUnder1500Binding
import com.yellowtwigs.knockin.ui.HelpActivity
import com.yellowtwigs.knockin.ui.contacts.list.ContactsListActivity
import com.yellowtwigs.knockin.ui.notifications.settings.NotificationsSettingsActivity
import com.yellowtwigs.knockin.premium.PremiumActivity
import com.yellowtwigs.knockin.ui.settings.ManageMyScreenActivity
import com.yellowtwigs.knockin.ui.statistics.dashboard.DashboardActivity
import com.yellowtwigs.knockin.ui.statistics.reward.RewardActivity
import com.yellowtwigs.knockin.ui.teleworking.TeleworkingActivity
import com.yellowtwigs.knockin.utils.EveryActivityUtils.checkTheme
import com.yellowtwigs.knockin.utils.EveryActivityUtils.hideKeyboard
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DailyStatisticsActivity : AppCompatActivity() {

    private lateinit var activityDailyStatisticsBinding: ActivityDailyStatisticsBinding
    private lateinit var activityDailyStatisticsUnder1200Binding: ActivityDailyStatisticsUnder1200Binding
    private lateinit var activityDailyStatisticsUnder1500Binding: ActivityDailyStatisticsUnder1500Binding

    private val USER_POINT = "USER_POINT"

    private val dailyStatisticsViewModel: DailyStatisticsViewModel by viewModels()
    private var deviceHeight = 0

    companion object {
        fun navigate(context: Context): Intent {
            return Intent(context, DailyStatisticsActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkTheme(this, packageName, contentResolver)
        hideKeyboard(this)
        deviceHeight = getDeviceHeight(this)

        if (deviceHeight in 1250..1699) {
            activityDailyStatisticsUnder1500Binding = ActivityDailyStatisticsUnder1500Binding.inflate(layoutInflater)
            setContentView(activityDailyStatisticsUnder1500Binding.root)

            setupToolbarUnder1500()
            setupDrawerLayoutUnder1500()
            setupBottomNavigationViewUnder1500()
            bindingDataToViewFromViewModelUnder1500()
        } else if (deviceHeight < 1250) {
            activityDailyStatisticsUnder1200Binding = ActivityDailyStatisticsUnder1200Binding.inflate(layoutInflater)
            setContentView(activityDailyStatisticsUnder1200Binding.root)

            setupToolbarUnder1200()
            setupDrawerLayoutUnder1200()
            setupBottomNavigationViewUnder1200()
            bindingDataToViewFromViewModelUnder1200()
        } else {
            activityDailyStatisticsBinding = ActivityDailyStatisticsBinding.inflate(layoutInflater)
            setContentView(activityDailyStatisticsBinding.root)

            setupToolbar()
            setupDrawerLayout()
            setupBottomNavigationView()
            bindingDataToViewFromViewModel()
        }
    }

    private fun getDeviceHeight(context: Context): Int {
        val displayMetrics = DisplayMetrics()
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (deviceHeight in 1250..1699) {
                    if (activityDailyStatisticsUnder1500Binding.drawerLayout.isOpen) {
                        activityDailyStatisticsUnder1500Binding.drawerLayout.closeDrawer(GravityCompat.START)
                    } else {
                        activityDailyStatisticsUnder1500Binding.drawerLayout.openDrawer(GravityCompat.START)
                    }
                } else if (deviceHeight < 1250) {
                    if (activityDailyStatisticsUnder1200Binding.drawerLayout.isOpen) {
                        activityDailyStatisticsUnder1200Binding.drawerLayout.closeDrawer(GravityCompat.START)
                    } else {
                        activityDailyStatisticsUnder1200Binding.drawerLayout.openDrawer(GravityCompat.START)
                    }
                } else {
                    if (activityDailyStatisticsBinding.drawerLayout.isOpen) {
                        activityDailyStatisticsBinding.drawerLayout.closeDrawer(GravityCompat.START)
                    } else {
                        activityDailyStatisticsBinding.drawerLayout.openDrawer(GravityCompat.START)
                    }
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //region ======================================== 1200 =========================================

    private fun bindingDataToViewFromViewModelUnder1200() {
        dailyStatisticsViewModel.dailyStatisticsViewStateLiveData.observe(this) {
            activityDailyStatisticsUnder1200Binding.adviceMessageContent.text = it.adviceMessage
        }

        val sharedPreferences: SharedPreferences = application.getSharedPreferences(USER_POINT, Context.MODE_PRIVATE)
        activityDailyStatisticsUnder1200Binding.numberPointsText.text = "${sharedPreferences.getInt(USER_POINT, 0)}"
        activityDailyStatisticsUnder1200Binding.rewardButton.setOnClickListener {
            startActivity(Intent(this@DailyStatisticsActivity, RewardActivity::class.java))
        }
    }

    private fun setupToolbarUnder1200() {
        setSupportActionBar(activityDailyStatisticsUnder1200Binding.toolbar)
        val actionbar = supportActionBar
        actionbar?.setDisplayHomeAsUpEnabled(true)
        actionbar?.title = getString(R.string.left_drawer_statistics)
        actionbar?.setHomeAsUpIndicator(R.drawable.ic_open_drawer)
    }

    private fun setupDrawerLayoutUnder1200() {
        activityDailyStatisticsUnder1200Binding.drawerLayout.apply {
            val menu = activityDailyStatisticsUnder1200Binding.navView.menu
            menu.findItem(R.id.nav_dashboard).isChecked = true

            val navSyncContact = menu.findItem(R.id.nav_sync_contact)
            navSyncContact.isVisible = false

            val navInviteFriend = menu.findItem(R.id.nav_invite_friend)
            navInviteFriend.isVisible = true

            activityDailyStatisticsUnder1200Binding.navView.setNavigationItemSelectedListener { menuItem ->
                if (menuItem.itemId != R.id.nav_sync_contact && menuItem.itemId != R.id.nav_invite_friend) {
                    menuItem.isChecked = true
                }
                hideKeyboard(this@DailyStatisticsActivity)
                closeDrawer(GravityCompat.START)

                when (menuItem.itemId) {
                    R.id.nav_home -> startActivity(
                        Intent(this@DailyStatisticsActivity, ContactsListActivity::class.java)
                    )
                    R.id.nav_notifications -> startActivity(
                        Intent(this@DailyStatisticsActivity, NotificationsSettingsActivity::class.java)
                    )
                    R.id.nav_teleworking -> startActivity(
                        Intent(this@DailyStatisticsActivity, TeleworkingActivity::class.java)
                    )
                    R.id.nav_in_app -> startActivity(
                        Intent(this@DailyStatisticsActivity, PremiumActivity::class.java)
                    )
                    R.id.nav_manage_screen -> startActivity(
                        Intent(this@DailyStatisticsActivity, ManageMyScreenActivity::class.java)
                    )
                    R.id.nav_help -> startActivity(Intent(this@DailyStatisticsActivity, HelpActivity::class.java))
                    R.id.nav_invite_friend -> {
                        val intent = Intent(Intent.ACTION_SEND)
                        val messageString = resources.getString(R.string.invite_friend_text) + " \n" + resources.getString(
                            R.string.location_on_playstore
                        )
                        intent.putExtra(Intent.EXTRA_TEXT, messageString)
                        intent.type = "text/plain"
                        val messageIntent = Intent.createChooser(intent, null)
                        startActivity(messageIntent)
                    }
                }
                true
            }
        }
    }

    private fun setupBottomNavigationViewUnder1200() {
        activityDailyStatisticsUnder1200Binding.bottomNavigation.menu.getItem(1).isChecked = true
        activityDailyStatisticsUnder1200Binding.bottomNavigation.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_dashboard -> {
                    startActivity(
                        Intent(
                            this@DailyStatisticsActivity, DashboardActivity::class.java
                        ).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    )
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        })
    }

    //endregion

    //region ======================================== 1500 =========================================

    private fun bindingDataToViewFromViewModelUnder1500() {
        dailyStatisticsViewModel.dailyStatisticsViewStateLiveData.observe(this) {
            activityDailyStatisticsUnder1500Binding.adviceMessageContent.text = it.adviceMessage
        }

        val sharedPreferences: SharedPreferences = application.getSharedPreferences(USER_POINT, Context.MODE_PRIVATE)
        activityDailyStatisticsUnder1500Binding.numberPointsText.text = "${sharedPreferences.getInt(USER_POINT, 0)}"
        activityDailyStatisticsUnder1500Binding.rewardButton.setOnClickListener {
            startActivity(Intent(this@DailyStatisticsActivity, RewardActivity::class.java))
        }
    }

    private fun setupToolbarUnder1500() {
        setSupportActionBar(activityDailyStatisticsUnder1500Binding.toolbar)
        val actionbar = supportActionBar
        actionbar?.setDisplayHomeAsUpEnabled(true)
        actionbar?.title = getString(R.string.left_drawer_statistics)
        actionbar?.setHomeAsUpIndicator(R.drawable.ic_open_drawer)
    }

    private fun setupDrawerLayoutUnder1500() {
        activityDailyStatisticsUnder1500Binding.drawerLayout.apply {
            val menu = activityDailyStatisticsUnder1500Binding.navView.menu
            menu.findItem(R.id.nav_dashboard).isChecked = true

            val navSyncContact = menu.findItem(R.id.nav_sync_contact)
            navSyncContact.isVisible = false

            val navInviteFriend = menu.findItem(R.id.nav_invite_friend)
            navInviteFriend.isVisible = true

            activityDailyStatisticsUnder1500Binding.navView.setNavigationItemSelectedListener { menuItem ->
                if (menuItem.itemId != R.id.nav_sync_contact && menuItem.itemId != R.id.nav_invite_friend) {
                    menuItem.isChecked = true
                }
                hideKeyboard(this@DailyStatisticsActivity)
                closeDrawer(GravityCompat.START)

                when (menuItem.itemId) {
                    R.id.nav_home -> startActivity(
                        Intent(this@DailyStatisticsActivity, ContactsListActivity::class.java)
                    )
                    R.id.nav_notifications -> startActivity(
                        Intent(this@DailyStatisticsActivity, NotificationsSettingsActivity::class.java)
                    )
                    R.id.nav_teleworking -> startActivity(
                        Intent(this@DailyStatisticsActivity, TeleworkingActivity::class.java)
                    )
                    R.id.nav_in_app -> startActivity(
                        Intent(this@DailyStatisticsActivity, PremiumActivity::class.java)
                    )
                    R.id.nav_manage_screen -> startActivity(
                        Intent(this@DailyStatisticsActivity, ManageMyScreenActivity::class.java)
                    )
                    R.id.nav_help -> startActivity(Intent(this@DailyStatisticsActivity, HelpActivity::class.java))
                    R.id.nav_invite_friend -> {
                        val intent = Intent(Intent.ACTION_SEND)
                        val messageString = resources.getString(R.string.invite_friend_text) + " \n" + resources.getString(
                            R.string.location_on_playstore
                        )
                        intent.putExtra(Intent.EXTRA_TEXT, messageString)
                        intent.type = "text/plain"
                        val messageIntent = Intent.createChooser(intent, null)
                        startActivity(messageIntent)
                    }
                }
                true
            }
        }
    }

    private fun setupBottomNavigationViewUnder1500() {
        activityDailyStatisticsUnder1500Binding.bottomNavigation.menu.getItem(1).isChecked = true
        activityDailyStatisticsUnder1500Binding.bottomNavigation.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_dashboard -> {
                    startActivity(
                        Intent(
                            this@DailyStatisticsActivity, DashboardActivity::class.java
                        ).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    )
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        })
    }

    //endregion

    //region ======================================== +1800 =========================================

    private fun bindingDataToViewFromViewModel() {
        dailyStatisticsViewModel.dailyStatisticsViewStateLiveData.observe(this) {
            activityDailyStatisticsBinding.adviceMessageContent.text = it.adviceMessage
        }

        val sharedPreferences: SharedPreferences = application.getSharedPreferences(USER_POINT, Context.MODE_PRIVATE)
        activityDailyStatisticsBinding.numberPointsText.text = "${sharedPreferences.getInt(USER_POINT, 0)}"
        activityDailyStatisticsBinding.rewardedButton.setOnClickListener {
            startActivity(Intent(this@DailyStatisticsActivity, RewardActivity::class.java))
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(activityDailyStatisticsBinding.toolbar)
        val actionbar = supportActionBar
        actionbar?.setDisplayHomeAsUpEnabled(true)
        actionbar?.title = getString(R.string.left_drawer_statistics)
        actionbar?.setHomeAsUpIndicator(R.drawable.ic_open_drawer)
    }

    private fun setupDrawerLayout() {
        activityDailyStatisticsBinding.drawerLayout.apply {
            val menu = activityDailyStatisticsBinding.navView.menu
            menu.findItem(R.id.nav_dashboard).isChecked = true

            val navSyncContact = menu.findItem(R.id.nav_sync_contact)
            navSyncContact.isVisible = false

            val navInviteFriend = menu.findItem(R.id.nav_invite_friend)
            navInviteFriend.isVisible = true

            activityDailyStatisticsBinding.navView.setNavigationItemSelectedListener { menuItem ->
                if (menuItem.itemId != R.id.nav_sync_contact && menuItem.itemId != R.id.nav_invite_friend) {
                    menuItem.isChecked = true
                }
                hideKeyboard(this@DailyStatisticsActivity)
                closeDrawer(GravityCompat.START)

                when (menuItem.itemId) {
                    R.id.nav_home -> startActivity(
                        Intent(this@DailyStatisticsActivity, ContactsListActivity::class.java)
                    )
                    R.id.nav_notifications -> startActivity(
                        Intent(this@DailyStatisticsActivity, NotificationsSettingsActivity::class.java)
                    )
                    R.id.nav_teleworking -> startActivity(
                        Intent(this@DailyStatisticsActivity, TeleworkingActivity::class.java)
                    )
                    R.id.nav_in_app -> startActivity(
                        Intent(this@DailyStatisticsActivity, PremiumActivity::class.java)
                    )
                    R.id.nav_manage_screen -> startActivity(
                        Intent(this@DailyStatisticsActivity, ManageMyScreenActivity::class.java)
                    )
                    R.id.nav_help -> startActivity(Intent(this@DailyStatisticsActivity, HelpActivity::class.java))
                    R.id.nav_invite_friend -> {
                        val intent = Intent(Intent.ACTION_SEND)
                        val messageString = resources.getString(R.string.invite_friend_text) + " \n" + resources.getString(
                            R.string.location_on_playstore
                        )
                        intent.putExtra(Intent.EXTRA_TEXT, messageString)
                        intent.type = "text/plain"
                        val messageIntent = Intent.createChooser(intent, null)
                        startActivity(messageIntent)
                    }
                }
                true
            }
        }
    }

    private fun setupBottomNavigationView() {
        activityDailyStatisticsBinding.bottomNavigation.menu.getItem(1).isChecked = true
        activityDailyStatisticsBinding.bottomNavigation.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_dashboard -> {
                    startActivity(
                        Intent(
                            this@DailyStatisticsActivity, DashboardActivity::class.java
                        ).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    )
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        })
    }

    //endregion
}