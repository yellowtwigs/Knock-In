package com.yellowtwigs.knockin.ui.statistics.reward

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.ActivityRewardBinding
import com.yellowtwigs.knockin.repositories.firebase.FirebaseViewModel
import com.yellowtwigs.knockin.premium.PremiumActivity
import com.yellowtwigs.knockin.ui.statistics.daily_statistics.DailyStatisticsActivity
import com.yellowtwigs.knockin.utils.EveryActivityUtils.checkTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RewardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRewardBinding

    private var sharedFunkySoundPreferences: SharedPreferences? = null
    private var sharedJazzySoundPreferences: SharedPreferences? = null
    private var sharedRelaxationSoundPreferences: SharedPreferences? = null
    private var sharedContactsUnlimitedPreferences: SharedPreferences? = null
    private var sharedCustomSoundPreferences: SharedPreferences? = null
    private var appsSupportPref: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRewardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkTheme(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        sharedFunkySoundPreferences = getSharedPreferences("Funky_Sound_Bought", Context.MODE_PRIVATE)
        sharedJazzySoundPreferences = getSharedPreferences("Jazzy_Sound_Bought", Context.MODE_PRIVATE)
        sharedRelaxationSoundPreferences = getSharedPreferences("Relax_Sound_Bought", Context.MODE_PRIVATE)
        sharedContactsUnlimitedPreferences = getSharedPreferences("Contacts_Unlimited_Bought", Context.MODE_PRIVATE)
        sharedCustomSoundPreferences = getSharedPreferences("Custom_Sound_Bought", Context.MODE_PRIVATE)
        appsSupportPref = getSharedPreferences("Apps_Support_Bought", Context.MODE_PRIVATE)

        val modePrivate = Context.MODE_PRIVATE
        val jazzyPref = getSharedPreferences("Jazzy_Sound_Bought", modePrivate)
        val funkyPref = getSharedPreferences("Funky_Sound_Bought", modePrivate)
        val relaxPref = getSharedPreferences("Relax_Sound_Bought", modePrivate)
        val appsSupportPref = getSharedPreferences("Apps_Support_Bought", modePrivate)
        val unlimitedPref = getSharedPreferences("Contacts_Unlimited_Bought", modePrivate)

        val jazzySoundBought = jazzyPref.getBoolean("Jazzy_Sound_Bought", false)
        val funkySoundBought = funkyPref.getBoolean("Funky_Sound_Bought", false)
        val relaxationSoundBought = relaxPref.getBoolean("Relax_Sound_Bought", false)
        val appsSupportBought = appsSupportPref.getBoolean("Apps_Support_Bought", false)
        val contactsUnlimitedBought = unlimitedPref.getBoolean("Contacts_Unlimited_Bought", false)

        val USER_POINT = "USER_POINT"
        val sharedPreferences: SharedPreferences = application.getSharedPreferences(USER_POINT, Context.MODE_PRIVATE)
        val points = sharedPreferences.getInt(USER_POINT, 0)
        binding.losingMessage.isVisible = points <= 50
        binding.redeemPointsLayout.visibility = if (points >= 50) View.VISIBLE else View.INVISIBLE
        binding.redeemPoints50.isEnabled = points >= 50 && !jazzySoundBought || !funkySoundBought || !relaxationSoundBought
        binding.redeemPoints200.isEnabled = points >= 200 && !appsSupportBought || !contactsUnlimitedBought
        binding.redeemPoints50.setOnClickListener {
            val editor = sharedPreferences.edit()
            editor.putInt(USER_POINT, points - 50)
            editor.apply()

            Toast.makeText(this, getString(R.string.reward_points_left, points - 50), Toast.LENGTH_LONG).show()

            binding.jazzyButton.isVisible = true
            binding.jazzyButton.isEnabled = !jazzySoundBought
            binding.funkyButton.isVisible = true
            binding.funkyButton.isEnabled = !funkySoundBought
            binding.relaxButton.isVisible = true
            binding.relaxButton.isEnabled = !relaxationSoundBought

            binding.redeemPoints50.isEnabled = false
        }
        binding.redeemPoints200.setOnClickListener {
            val editor = sharedPreferences.edit()
            editor.putInt(USER_POINT, points - 200)
            editor.apply()

            Toast.makeText(this, getString(R.string.reward_points_left, points - 200), Toast.LENGTH_LONG).show()

            binding.messagingVipButton.isVisible = true
            binding.messagingVipButton.isEnabled = !appsSupportBought
            binding.contactsButton.isVisible = true
            binding.contactsButton.isEnabled = !contactsUnlimitedBought

            binding.redeemPoints200.isEnabled = false
        }

        val listener = View.OnClickListener {
            when (it.id) {
                binding.jazzyButton.id -> {
                    MaterialAlertDialogBuilder(
                        this@RewardActivity, R.style.AlertDialog
                    ).setTitle(getString(R.string.promotion_of_store_products))
                        .setMessage(getString(R.string.product_discount_alert_dialog_message) + " ${getString(R.string.jazzy_ringtones)}")
                        .setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
                            startActivity(Intent(this@RewardActivity, PremiumActivity::class.java))
                        }.show()
                }

                binding.funkyButton.id -> {
                    MaterialAlertDialogBuilder(
                        this@RewardActivity, R.style.AlertDialog
                    ).setTitle(getString(R.string.promotion_of_store_products))
                        .setMessage(getString(R.string.product_discount_alert_dialog_message) + " ${getString(R.string.funky_ringtones)}")
                        .setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
                            startActivity(Intent(this@RewardActivity, PremiumActivity::class.java))
                        }.show()
                }

                binding.relaxButton.id -> {
                    MaterialAlertDialogBuilder(
                        this@RewardActivity, R.style.AlertDialog
                    ).setTitle(getString(R.string.promotion_of_store_products))
                        .setMessage(getString(R.string.product_discount_alert_dialog_message) + " ${getString(R.string.relaxation_personalization_pack)}")
                        .setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
                            startActivity(Intent(this@RewardActivity, PremiumActivity::class.java))
                        }.show()
                }

                binding.messagingVipButton.id -> {
                    MaterialAlertDialogBuilder(
                        this@RewardActivity, R.style.AlertDialog
                    ).setTitle(getString(R.string.promotion_of_store_products))
                        .setMessage(getString(R.string.product_discount_alert_dialog_message) + " ${getString(R.string.support_all_messaging_apps)}")
                        .setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
                            startActivity(Intent(this@RewardActivity, PremiumActivity::class.java))
                        }.show()
                }

                binding.contactsButton.id -> {
                    MaterialAlertDialogBuilder(
                        this@RewardActivity, R.style.AlertDialog
                    ).setTitle(getString(R.string.promotion_of_store_products))
                        .setMessage(getString(R.string.product_discount_alert_dialog_message) + " ${getString(R.string.unlimited_vip_contacts)}")
                        .setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
                            startActivity(Intent(this@RewardActivity, PremiumActivity::class.java))
                        }.show()
                }

                else -> {
                }
            }
        }

        binding.jazzyButton.setOnClickListener(listener)
        binding.funkyButton.setOnClickListener(listener)
        binding.relaxButton.setOnClickListener(listener)
        binding.messagingVipButton.setOnClickListener(listener)
        binding.contactsButton.setOnClickListener(listener)
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