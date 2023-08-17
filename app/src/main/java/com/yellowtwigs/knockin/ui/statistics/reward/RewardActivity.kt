package com.yellowtwigs.knockin.ui.statistics.reward

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.yellowtwigs.knockin.databinding.ActivityRewardBinding
import com.yellowtwigs.knockin.ui.statistics.daily_statistics.DailyStatisticsActivity
import com.yellowtwigs.knockin.utils.EveryActivityUtils.checkTheme

class RewardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRewardBinding
    private var isLayoutVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRewardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkTheme(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val USER_POINT = "USER_POINT"
        val sharedPreferences: SharedPreferences = application.getSharedPreferences(USER_POINT, Context.MODE_PRIVATE)

        binding.redeemPointsLayout.isVisible = sharedPreferences.getInt(USER_POINT, 0) >= 50

        binding.fakeButton.setOnClickListener {
            binding.redeemPointsLayout.isVisible = !isLayoutVisible
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            startActivity(Intent(this@RewardActivity, DailyStatisticsActivity::class.java))
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}