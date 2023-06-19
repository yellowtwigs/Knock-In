package com.yellowtwigs.knockin.ui.statistics.daily_statistics

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.yellowtwigs.knockin.databinding.ActivityDailyStatisticsBinding
import com.yellowtwigs.knockin.databinding.ActivityDailyStatisticsUnder1200Binding
import com.yellowtwigs.knockin.databinding.ActivityDailyStatisticsUnder1500Binding
import com.yellowtwigs.knockin.ui.notifications.history.NotificationsHistoryActivity
import com.yellowtwigs.knockin.utils.EveryActivityUtils.checkTheme
import com.yellowtwigs.knockin.utils.EveryActivityUtils.hideKeyboard
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DailyStatisticsActivity : AppCompatActivity() {

    private lateinit var activityDailyStatisticsBinding: ActivityDailyStatisticsBinding
    private lateinit var activityDailyStatisticsUnder1200Binding: ActivityDailyStatisticsUnder1200Binding
    private lateinit var activityDailyStatisticsUnder1500Binding: ActivityDailyStatisticsUnder1500Binding

    private val dailyStatisticsViewModel: DailyStatisticsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkTheme(this)
        hideKeyboard(this)

        val deviceHeight = getDeviceHeight(this)

        if (deviceHeight in 1251..1599) {
            activityDailyStatisticsUnder1500Binding = ActivityDailyStatisticsUnder1500Binding.inflate(layoutInflater)
            setContentView(activityDailyStatisticsUnder1500Binding.root)

            if (!intent.getBooleanExtra("FromSender", false)) {
                startActivity(Intent(this@DailyStatisticsActivity, NotificationsHistoryActivity::class.java))
            }

            activityDailyStatisticsUnder1500Binding.toDashboardButton.setOnClickListener {
                startActivity(Intent(this@DailyStatisticsActivity, NotificationsHistoryActivity::class.java))
            }

            bindingDataToViewFromViewModelUnder1500()
        } else if (deviceHeight < 1250) {
            activityDailyStatisticsUnder1200Binding = ActivityDailyStatisticsUnder1200Binding.inflate(layoutInflater)
            setContentView(activityDailyStatisticsUnder1200Binding.root)

            if (!intent.getBooleanExtra("FromSender", false)) {
                startActivity(Intent(this@DailyStatisticsActivity, NotificationsHistoryActivity::class.java))
            }

            activityDailyStatisticsUnder1200Binding.toDashboardButton.setOnClickListener {
                startActivity(Intent(this@DailyStatisticsActivity, NotificationsHistoryActivity::class.java))
            }

            bindingDataToViewFromViewModelUnder1200()
        } else {
            activityDailyStatisticsBinding = ActivityDailyStatisticsBinding.inflate(layoutInflater)
            setContentView(activityDailyStatisticsBinding.root)

            if (!intent.getBooleanExtra("FromSender", false)) {
                startActivity(Intent(this@DailyStatisticsActivity, NotificationsHistoryActivity::class.java))
                finish()
            }

            activityDailyStatisticsBinding.toDashboardButton.setOnClickListener {
                startActivity(Intent(this@DailyStatisticsActivity, NotificationsHistoryActivity::class.java))
                finish()
            }

            bindingDataToViewFromViewModel()
        }
    }

    private fun bindingDataToViewFromViewModelUnder1200() {
        dailyStatisticsViewModel.dailyStatisticsViewStateLiveData.observe(this) {
            activityDailyStatisticsUnder1200Binding.performanceIcon.setImageResource(it.icon)
            activityDailyStatisticsUnder1200Binding.notificationsUnprocessed.text = it.numberOfNotificationsUnprocessed
            activityDailyStatisticsUnder1200Binding.notificationsVip.text = it.numberOfNotificationsVip
            activityDailyStatisticsUnder1200Binding.adviceMessageContent.text = it.adviceMessage
        }
    }

    private fun bindingDataToViewFromViewModelUnder1500() {
        dailyStatisticsViewModel.dailyStatisticsViewStateLiveData.observe(this) {
            activityDailyStatisticsUnder1500Binding.performanceIcon.setImageResource(it.icon)
            activityDailyStatisticsUnder1500Binding.notificationsUnprocessed.text = it.numberOfNotificationsUnprocessed
            activityDailyStatisticsUnder1500Binding.notificationsVip.text = it.numberOfNotificationsVip
            activityDailyStatisticsUnder1500Binding.adviceMessageContent.text = it.adviceMessage
        }
    }

    private fun bindingDataToViewFromViewModel() {
        dailyStatisticsViewModel.dailyStatisticsViewStateLiveData.observe(this) {
            activityDailyStatisticsBinding.performanceIcon.setImageResource(it.icon)
            activityDailyStatisticsBinding.notificationsUnprocessed.text = it.numberOfNotificationsUnprocessed
            activityDailyStatisticsBinding.notificationsVip.text = it.numberOfNotificationsVip
            activityDailyStatisticsBinding.adviceMessageContent.text = it.adviceMessage
        }
    }

    fun getDeviceHeight(context: Context): Int {
        val displayMetrics = DisplayMetrics()
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }

    fun getDeviceWidth(context: Context): Int {
        val displayMetrics = DisplayMetrics()
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.widthPixels
    }
}