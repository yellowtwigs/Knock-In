package com.yellowtwigs.knockin.ui.statistics.dashboard

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.ActivityDashboardBinding
import com.yellowtwigs.knockin.ui.HelpActivity
import com.yellowtwigs.knockin.ui.contacts.list.ContactsListActivity
import com.yellowtwigs.knockin.ui.notifications.settings.NotificationsSettingsActivity
import com.yellowtwigs.knockin.ui.premium.PremiumActivity
import com.yellowtwigs.knockin.ui.settings.ManageMyScreenActivity
import com.yellowtwigs.knockin.ui.statistics.daily_statistics.DailyStatisticsActivity
import com.yellowtwigs.knockin.ui.teleworking.TeleworkingActivity
import com.yellowtwigs.knockin.utils.EveryActivityUtils.checkTheme
import com.yellowtwigs.knockin.utils.EveryActivityUtils.hideKeyboard
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class DashboardActivity : AppCompatActivity(), OnChartValueSelectedListener {

    private val dashboardViewModel: DashboardViewModel by viewModels()

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var dataPieChart: PieChart

    private var isThemeDark = false

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

        setupToolbar()
        setupDrawerLayout()
        setupDataToView()
        setupSpinner()

        dataPieChart.setOnChartValueSelectedListener(this)

        binding.goToDailyPerformance.setOnClickListener {
            startActivity(Intent(this@DashboardActivity, DailyStatisticsActivity::class.java).putExtra("FromSender", true))
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
            R.id.item_help -> {
//                startActivity(Intent(this@DashboardActivity, DailyStatisticsActivity::class.java).putExtra("FromSender", true))
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

    private fun setupSpinner() {
        val colorList = resources.getStringArray(R.array.messaging_apps_arrays)
        val colorsSpinnerAdapter = ArrayAdapter(this@DashboardActivity, R.layout.spinner_item, colorList)
        binding.messagingAppsSpinner.adapter = colorsSpinnerAdapter

        binding.messagingAppsSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                dashboardViewModel.updateSpinnerSelectedItem(position)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }
    }

    private fun setupDataToView() {
        dashboardViewModel.dashboardViewStateLiveData.observe(this) {
            setupPieChart(it.list)
            binding.allNotificationsTitle.text = it.notificationsTitle
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

//        seekBarX.setProgress(4);
//        seekBarY.setProgress(10);

        dataPieChart.animateY(100, Easing.EaseInOutQuad)
        // chart.spin(2000, 0, 360);

        val l = dataPieChart.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        l.orientation = Legend.LegendOrientation.VERTICAL
        l.setDrawInside(true)
        l.xEntrySpace = 7f
        l.yEntrySpace = 0f
        l.yOffset = 0f

        if (isThemeDark) {
            dataPieChart.setHoleColor(Color.TRANSPARENT)
            dataPieChart.setEntryLabelColor(Color.WHITE)
        } else {
            dataPieChart.setHoleColor(Color.TRANSPARENT)
            dataPieChart.setEntryLabelColor(Color.WHITE)
        }
        dataPieChart.setEntryLabelTextSize(12f)

        setupDataToPieChart(list)
    }

    private fun setupDataToPieChart(list: List<PieChartDataViewState>) {
        val colors: ArrayList<Int> = ArrayList()
        val entries: List<PieEntry> = list.map {
            colors.add(it.color)
            PieEntry(1f, "${it.platform} : ${it.number}", resources.getDrawable(R.drawable.ic_speedometer_strong_green))
        }

        val dataSet = PieDataSet(entries, "")
        dataSet.setDrawIcons(false)
        dataSet.sliceSpace = 30f
        dataSet.selectionShift = 25F
        dataSet.colors = colors

        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter())
        data.setValueTextSize(12f)

        if (isThemeDark) {
            data.setValueTextColor(Color.WHITE)
        } else {
            data.setValueTextColor(Color.WHITE)
        }

        dataPieChart.data = data

        dataPieChart.highlightValues(null)
        dataPieChart.invalidate()
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
    }

    override fun onNothingSelected() {
    }
}