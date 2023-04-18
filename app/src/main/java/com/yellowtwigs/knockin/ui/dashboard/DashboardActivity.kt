package com.yellowtwigs.knockin.ui.dashboard

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.ActivityDashboardBinding
import com.yellowtwigs.knockin.ui.HelpActivity
import com.yellowtwigs.knockin.ui.contacts.list.ContactsListActivity
import com.yellowtwigs.knockin.ui.notifications.settings.NotificationsSettingsActivity
import com.yellowtwigs.knockin.ui.premium.PremiumActivity
import com.yellowtwigs.knockin.ui.teleworking.TeleworkingActivity
import com.yellowtwigs.knockin.utils.EveryActivityUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DashboardActivity : AppCompatActivity() {

    private val dashboardViewModel: DashboardViewModel by viewModels()

    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar(binding)
        setupDrawerLayout(binding)
        setupDataToView(binding)
    }

    //region =============================================================== TOOLBAR ================================================================

    private fun setupToolbar(binding: ActivityDashboardBinding) {
        setSupportActionBar(binding.toolbar)
        val actionbar = supportActionBar
        actionbar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
//                binding.drawerLayout.openDrawer(GravityCompat.START)
                return true
            }
            R.id.item_help -> {
//                startActivity(Intent(this@DashboardActivity, DailyStatisticsActivity::class.java))
//                MaterialAlertDialogBuilder(this, R.style.AlertDialog)
//                    .setTitle(R.string.help)
//                    .setMessage(resources.getString(R.string.statistics_help_msg))
//                    .show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.toolbar_menu_help, menu)
        return super.onCreateOptionsMenu(menu)
    }

    //endregion

    //region ================================================================ DRAWER ================================================================

    private fun setupDrawerLayout(binding: ActivityDashboardBinding) {
//        val itemLayout = findViewById<ConstraintLayout>(R.id.teleworking_item)
//        val itemText = findViewById<AppCompatTextView>(R.id.teleworking_item_text)
//
//        itemText.text = "${getString(R.string.teleworking)} ${getString(R.string.left_drawer_settings)}"
//
//        itemLayout.setOnClickListener {
//            startActivity(Intent(this@DashboardActivity, TeleworkingActivity::class.java))
//        }


    }

    //endregion

    private fun setupDataToView(binding: ActivityDashboardBinding) {
        dashboardViewModel.dashboardViewStateLiveData.observe(this) {
            binding.allMessagingContent.text = it?.allMessagingNumbers
            binding.dailyMessagingContent.text = it?.messagingNumbersDaily
            binding.weeklyMessagingContent.text = it?.messagingNumbersWeekly
            binding.monthlyMessagingContent.text = it?.messagingNumbersMonthly

            binding.allVipContent.text = it?.allMessagingNumbers
            binding.dailyVipContent.text = it?.messagingNumbersDaily
            binding.weeklyVipContent.text = it?.messagingNumbersWeekly
            binding.monthlyVipContent.text = it?.messagingNumbersMonthly
        }

        binding.messagingSpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                dashboardViewModel.updateSpinnerSelectedItem(position)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }
    }
}