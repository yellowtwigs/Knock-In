package com.yellowtwigs.knockin.ui.statistics.daily_statistics

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.yellowtwigs.knockin.databinding.ActivityDailyStatisticsBinding
import com.yellowtwigs.knockin.ui.notifications.history.NotificationsHistoryActivity
import com.yellowtwigs.knockin.ui.statistics.dashboard.DashboardActivity
import com.yellowtwigs.knockin.ui.statistics.dashboard.DashboardViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DailyStatisticsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDailyStatisticsBinding
    private val dailyStatisticsViewModel: DailyStatisticsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDailyStatisticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!intent.getBooleanExtra("FromSender", false)) {
            startActivity(Intent(this@DailyStatisticsActivity, NotificationsHistoryActivity::class.java))
        }

        binding.toDashboardButton.setOnClickListener {
            startActivity(Intent(this@DailyStatisticsActivity, NotificationsHistoryActivity::class.java))
        }

        bindingDataToViewFromViewModel()
    }

    private fun bindingDataToViewFromViewModel() {
        dailyStatisticsViewModel.dailyStatisticsViewStateLiveData.observe(this) {
            binding.performanceIcon.setImageResource(it.icon)
            binding.notificationsUnprocessed.text = it.numberOfNotificationsUnprocessed
            binding.notificationsVip.text = it.numberOfNotificationsVip
            binding.adviceMessageContent.text = it.adviceMessage
        }
    }
}