package com.yellowtwigs.knockin.ui.statistics.dashboard

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.MPPointF
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.ActivityDashboardBinding
import com.yellowtwigs.knockin.ui.statistics.daily_statistics.DailyStatisticsActivity
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class DashboardActivity : AppCompatActivity(), OnChartValueSelectedListener {

    private val dashboardViewModel: DashboardViewModel by viewModels()

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var dataPieChart: PieChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dataPieChart = binding.pieChartMessaging

        setupToolbar()
        setupDrawerLayout()
        setupDataToView()

        dataPieChart.setOnChartValueSelectedListener(this)
    }

    //region =============================================================== TOOLBAR ================================================================

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        val actionbar = supportActionBar
        actionbar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            R.id.item_help -> {
                startActivity(Intent(this@DashboardActivity, DailyStatisticsActivity::class.java))
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

    private fun setupDrawerLayout() {
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

    private fun setupDataToView() {
        dashboardViewModel.dashboardViewStateLiveData.observe(this) {
            setupPieChart(it.list)
            binding.allNotificationsTitle.setText(it.notificationsTitle)
        }

        binding.messagingAppsSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                dashboardViewModel.updateSpinnerSelectedItem(position)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }
    }

    private fun setupPieChart(list: List<PieChartDataViewState>) {
        dataPieChart.setUsePercentValues(true)
        dataPieChart.description.isEnabled = false
        dataPieChart.setExtraOffsets(5F, 5F, 5F, 5F)

        dataPieChart.dragDecelerationFrictionCoef = 0.95f

        dataPieChart.isDrawHoleEnabled = true
        dataPieChart.setHoleColor(Color.WHITE)

//        dataPieChart.setTransparentCircleColor(Color.WHITE)
//        dataPieChart.setTransparentCircleAlpha(110)

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
        l.setDrawInside(false)
        l.xEntrySpace = 7f
        l.yEntrySpace = 0f
        l.yOffset = 0f

        dataPieChart.setEntryLabelColor(Color.WHITE)
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
        dataSet.sliceSpace = 1f
//        dataSet.
        dataSet.selectionShift = 20F

//        for (c in TEST_COLORS) colors.add(c)
//        for (c in JOYFUL_COLORS) colors.add(c)
//        for (c in COLORFUL_COLORS) colors.add(c)
//        for (c in LIBERTY_COLORS) colors.add(c)
//        for (c in PASTEL_COLORS) colors.add(c)

//        colors.add(ColorTemplate.getHoloBlue())
        dataSet.colors = colors
        //dataSet.setSelectionShift(0f);
        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter())
        data.setValueTextSize(12f)
        data.setValueTextColor(Color.BLACK)
        dataPieChart.data = data

        dataPieChart.highlightValues(null)
        dataPieChart.invalidate()
    }

    val LIBERTY_COLORS = intArrayOf(
        Color.rgb(207, 248, 246), Color.rgb(148, 212, 212), Color.rgb(136, 180, 187), Color.rgb(118, 174, 175), Color.rgb(42, 109, 130)
    )
    val JOYFUL_COLORS = intArrayOf(
        Color.rgb(217, 80, 138), Color.rgb(254, 149, 7), Color.rgb(254, 247, 120), Color.rgb(106, 167, 134), Color.rgb(53, 194, 209)
    )
    val PASTEL_COLORS = intArrayOf(
        Color.rgb(64, 89, 128), Color.rgb(149, 165, 124), Color.rgb(217, 184, 162), Color.rgb(191, 134, 134), Color.rgb(179, 48, 80)
    )
    val COLORFUL_COLORS = intArrayOf(
        Color.rgb(193, 37, 82), Color.rgb(255, 102, 0), Color.rgb(245, 199, 0), Color.rgb(106, 150, 31), Color.rgb(179, 100, 53)
    )
    val TEST_COLORS = intArrayOf(Color.rgb(0, 0, 0))
    val VORDIPLOM_COLORS = intArrayOf(
        Color.rgb(255, 255, 255), Color.rgb(255, 247, 140), Color.rgb(255, 208, 140), Color.rgb(140, 234, 255), Color.rgb(255, 140, 157)
    )

    override fun onValueSelected(e: Entry?, h: Highlight?) {
    }

    override fun onNothingSelected() {
    }
}