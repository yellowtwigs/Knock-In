package com.yellowtwigs.knockin.ui.statistics.daily_statistics

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.yellowtwigs.knockin.databinding.ActivityDailyStatisticsBinding
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

        bindingDataToViewFromViewModel()
    }

    private fun bindingDataToViewFromViewModel() {
        dailyStatisticsViewModel.dailyStatisticsViewStateLiveData.observe(this) {
            binding.performanceIcon.setImageResource(it.icon)
            binding.notificationsTotal.text = it.numberOfNotificationsTotal
            binding.notificationsVip.text = it.numberOfNotificationsVip
            binding.adviceMessage.text = it.adviceMessage
        }
    }
}