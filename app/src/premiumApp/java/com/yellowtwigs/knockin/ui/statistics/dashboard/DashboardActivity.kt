package com.yellowtwigs.knockin.ui.statistics.dashboard

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.ActivityDashboardBinding
import com.yellowtwigs.knockin.ui.HelpActivity
import com.yellowtwigs.knockin.ui.contacts.list.ContactsListActivity
import com.yellowtwigs.knockin.ui.notifications.settings.NotificationsSettingsActivity
import com.yellowtwigs.knockin.premium.PremiumActivity
import com.yellowtwigs.knockin.ui.settings.ManageMyScreenActivity
import com.yellowtwigs.knockin.ui.statistics.daily_statistics.DailyStatisticsActivity
import com.yellowtwigs.knockin.ui.teleworking.TeleworkingActivity
import com.yellowtwigs.knockin.utils.EveryActivityUtils.hideKeyboard
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@AndroidEntryPoint
class DashboardActivity : AppCompatActivity(), OnChartValueSelectedListener {

    private val dashboardViewModel: DashboardViewModel by viewModels()

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var dataPieChart: PieChart

    private var isThemeDark = false
    private var isVIP = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedThemePreferences = getSharedPreferences("Knockin_Theme", Context.MODE_PRIVATE)
        isThemeDark = sharedThemePreferences.getBoolean("darkTheme", false)
        if (sharedThemePreferences.getBoolean("darkTheme", false)) {
            setTheme(R.style.AppThemeDark)
        } else {
            setTheme(R.style.AppTheme)
        }
        hideKeyboard(this)

        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dataPieChart = binding.pieChartMessaging
        setupDataToView()
        showLoadingProgressBar()

        setupToolbar()
        setupDrawerLayout()

        dataPieChart.setOnChartValueSelectedListener(this)

        setupBottomNavigationView()

        binding.notificationsUnprocessedContent.setOnClickListener {
            if (isVIP) {
                isVIP = false
                dashboardViewModel.changeNewOrVipNotifications(isVIP)
                binding.notificationsUnprocessedContent.setBackgroundColor(resources.getColor(R.color.darkGreyColor, resources.newTheme()))
                binding.notificationsVipContent.setBackgroundColor(resources.getColor(R.color.darkGreyColorDark, resources.newTheme()))
            }
        }
        binding.notificationsVipContent.setOnClickListener {
            if (!isVIP) {
                isVIP = true
                dashboardViewModel.changeNewOrVipNotifications(isVIP)
                binding.notificationsUnprocessedContent.setBackgroundColor(
                    resources.getColor(
                        R.color.darkGreyColorDark, resources.newTheme()
                    )
                )
                binding.notificationsVipContent.setBackgroundColor(resources.getColor(R.color.darkGreyColor, resources.newTheme()))
            }
        }
    }

    //region =============================================================== TOOLBAR ================================================================

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        val actionbar = supportActionBar
        actionbar?.setDisplayHomeAsUpEnabled(true)
        actionbar?.title = getString(R.string.left_drawer_statistics)
        actionbar?.setHomeAsUpIndicator(R.drawable.ic_open_drawer)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (binding.drawerLayout.isOpen) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    binding.drawerLayout.openDrawer(GravityCompat.START)
                }
                return true
            }

            R.id.item_daily -> {
                dashboardViewModel.changeDailyWeeklyMonthly(R.id.item_daily)
                showLoadingProgressBar()
                item.isChecked = true
            }

            R.id.item_weekly -> {
                dashboardViewModel.changeDailyWeeklyMonthly(R.id.item_weekly)
                showLoadingProgressBar()
                item.isChecked = true
            }

            R.id.item_monthly -> {
                dashboardViewModel.changeDailyWeeklyMonthly(R.id.item_monthly)
                showLoadingProgressBar()
                item.isChecked = true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.toolbar_menu_dashboard, menu)
        return super.onCreateOptionsMenu(menu)
    }

    //endregion

    private fun showLoadingProgressBar() {
        CoroutineScope(Dispatchers.Main).launch {
            binding.pieChartLoading.isVisible = true
            binding.allNotificationsTitle.isVisible = false
            binding.pieChartMessaging.isVisible = false
            delay(3500L)

            binding.pieChartLoading.isVisible = false
            binding.allNotificationsTitle.isVisible = true
            binding.pieChartMessaging.isVisible = true
        }
    }

    //region ======================================== DRAWER LAYOUT =========================================

    private fun setupDrawerLayout() {
        binding.drawerLayout.apply {
            val menu = binding.navView.menu
            menu.findItem(R.id.nav_dashboard).isChecked = true

            val navSyncContact = menu.findItem(R.id.nav_sync_contact)
            navSyncContact.isVisible = false

            val navInviteFriend = menu.findItem(R.id.nav_invite_friend)
            navInviteFriend.isVisible = true

            binding.navView.setNavigationItemSelectedListener { menuItem ->
                if (menuItem.itemId != R.id.nav_sync_contact && menuItem.itemId != R.id.nav_invite_friend) {
                    menuItem.isChecked = true
                }
                hideKeyboard(this@DashboardActivity)
                closeDrawer(GravityCompat.START)

                when (menuItem.itemId) {
                    R.id.nav_home -> startActivity(
                        Intent(this@DashboardActivity, ContactsListActivity::class.java)
                    )

                    R.id.nav_notifications -> startActivity(
                        Intent(this@DashboardActivity, NotificationsSettingsActivity::class.java)
                    )

                    R.id.nav_teleworking -> startActivity(
                        Intent(this@DashboardActivity, TeleworkingActivity::class.java)
                    )

                    R.id.nav_in_app -> startActivity(
                        Intent(this@DashboardActivity, PremiumActivity::class.java)
                    )

                    R.id.nav_manage_screen -> startActivity(
                        Intent(this@DashboardActivity, ManageMyScreenActivity::class.java)
                    )

                    R.id.nav_help -> startActivity(Intent(this@DashboardActivity, HelpActivity::class.java))
                    R.id.nav_dashboard -> startActivity(Intent(this@DashboardActivity, DashboardActivity::class.java))
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

    //endregion

    private fun setupBottomNavigationView() {
        binding.bottomNavigation.menu.getItem(0).isChecked = true
        binding.bottomNavigation.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_daily_stats -> {
                    startActivity(Intent(this@DashboardActivity, DailyStatisticsActivity::class.java))
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        })
    }

    private fun setupDataToView() {
        dashboardViewModel.dashboardViewStateLiveData.observe(this) {
            setupPieChart(it.list)
            binding.performanceIcon.setImageResource(it.icon)
            binding.allNotificationsTitle.text = it.notificationsTitle
            binding.notificationsUnprocessedContent.setText(it.numberOfNotificationsUnprocessed)
            binding.notificationsVipContent.setText(it.numberOfNotificationsVip)
        }
    }

    private fun setupPieChart(list: List<PieChartDataViewState>) {
        dataPieChart.setUsePercentValues(true)
        dataPieChart.description.isEnabled = false
        dataPieChart.setExtraOffsets(5F, 5F, 5F, 5F)

        dataPieChart.dragDecelerationFrictionCoef = 0.95f

        dataPieChart.isDrawHoleEnabled = true

        dataPieChart.holeRadius = 58f
        dataPieChart.transparentCircleRadius = 61f

        dataPieChart.setDrawCenterText(true)

        dataPieChart.rotationAngle = 0F
        dataPieChart.isRotationEnabled = true
        dataPieChart.isHighlightPerTapEnabled = true

        dataPieChart.setOnChartValueSelectedListener(this)

        dataPieChart.animateY(100, Easing.EaseInOutQuad)

        val l = dataPieChart.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        l.orientation = Legend.LegendOrientation.VERTICAL
        l.setDrawInside(true)
        l.xEntrySpace = 7f
        l.yEntrySpace = 0f
        l.yOffset = 0f

        dataPieChart.setHoleColor(Color.TRANSPARENT)
        dataPieChart.setEntryLabelColor(Color.TRANSPARENT)

        if (isThemeDark) {
            dataPieChart.legend.textColor = Color.WHITE
        } else {
            dataPieChart.legend.textColor = Color.BLACK
        }
        dataPieChart.setEntryLabelTextSize(12f)

        setupDataToPieChart(list)
    }

    private fun setupDataToPieChart(list: List<PieChartDataViewState>) {
        val colors: ArrayList<Int> = ArrayList()
        val entries: List<PieEntry> = list.map {
            colors.add(it.color)
            PieEntry(it.number.toFloat(), "${it.platform} : ${it.number}", resources.getDrawable(R.drawable.ic_speedometer_strong_green))
        }

        val dataSet = PieDataSet(entries, "")
        dataSet.setDrawIcons(false)
        dataSet.sliceSpace = 30f
        dataSet.selectionShift = 25F
        dataSet.colors = colors

        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter())
        data.setValueTextSize(12f)
//        data.setValueTextColor(Color.rgb(159, 48, 255))
        data.setValueTextColor(Color.TRANSPARENT)

//        if (isThemeDark) {
//            data.setValueTextColor(Color.WHITE)
//        } else {
//            data.setValueTextColor(Color.WHITE)
//        }

        dataPieChart.data = data
        dataPieChart.setEntryLabelColor(Color.TRANSPARENT)
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
    }

    override fun onNothingSelected() {
    }
}