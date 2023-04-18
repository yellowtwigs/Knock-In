package com.yellowtwigs.knockin.ui.dashboard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.yellowtwigs.knockin.databinding.ActivityDailyStatisticsBinding

class DailyStatisticsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDailyStatisticsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDailyStatisticsBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}