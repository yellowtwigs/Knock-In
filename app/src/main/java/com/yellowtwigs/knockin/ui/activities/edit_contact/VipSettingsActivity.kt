package com.yellowtwigs.knockin.ui.activities.edit_contact

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.controller.activity.ManageNotificationActivity
import com.yellowtwigs.knockin.controller.activity.PremiumActivity
import com.yellowtwigs.knockin.databinding.ActivityVipSettingsBinding
import com.yellowtwigs.knockin.model.*
import com.yellowtwigs.knockin.model.ModelDB.*

class VipSettingsActivity : AppCompatActivity() {

    //region ========================================== Var or Val ==========================================

    private var alarmSound: MediaPlayer? = null

    private var funkySoundBought: Boolean = false
    private var jazzySoundBought: Boolean = true
    private var relaxSoundBought: Boolean = false
    private var customSoundBought: Boolean = false

    private var jazzyToClose = false
    private var funkyToClose = false
    private var relaxToClose = false
    private var uploadToClose = false

    private lateinit var alarmTonePreferences: SharedPreferences

    private var numberDefault = 1
    private lateinit var binding: ActivityVipSettingsBinding

    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //region ======================================== Theme Dark ========================================

        val sharedThemePreferences = getSharedPreferences("Knockin_Theme", Context.MODE_PRIVATE)
        if (sharedThemePreferences.getBoolean("darkTheme", false)) {
            setTheme(R.style.AppThemeDark)
        } else {
            setTheme(R.style.AppTheme)
        }

        //endregion

        binding = ActivityVipSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        alarmTonePreferences = getSharedPreferences("Alarm_Custom_Tone", Context.MODE_PRIVATE)

        val jazzySoundPreferences = getSharedPreferences("Jazzy_Sound_Bought", Context.MODE_PRIVATE)
        jazzySoundBought = jazzySoundPreferences.getBoolean("Jazzy_Sound_Bought", false)
//        jazzySoundBought = true

        val relaxSoundPreferences = getSharedPreferences("Relax_Sound_Bought", Context.MODE_PRIVATE)
        relaxSoundBought = relaxSoundPreferences.getBoolean("Relax_Sound_Bought", false)

        val funkySoundPreferences = getSharedPreferences("Funky_Sound_Bought", Context.MODE_PRIVATE)
        funkySoundBought = funkySoundPreferences.getBoolean("Funky_Sound_Bought", false)

        val customSoundPreferences =
            getSharedPreferences("Custom_Sound_Bought", Context.MODE_PRIVATE)
        customSoundBought = customSoundPreferences.getBoolean("Custom_Sound_Bought", false)

        //region ========================================== Intent ==========================================

        val contactId = intent.getIntExtra("ContactId", 0)
        val contactList = ContactManager(this)
        val contact = contactList.getContactById(contactId)

        //endregion

        val contactName = "${contact?.contactDB?.firstName} ${contact?.contactDB?.lastName}"
        binding.contactName.text = contactName

        if (contact?.contactDB?.notificationSound != null) {
            numberDefault = contact.contactDB?.notificationSound!!
        }
        Log.i("alarmTone", "Notif Sound : ${contact?.contactDB?.notificationSound}")

        ringToneLayoutClosed()
        refreshChecked()
        checkIfUserBoughtCustomSound()

        //region ======================================== Listeners =========================================

        binding.apply {
            backIcon.setOnClickListener {
                backIconClick(contactId)
            }
            uploadButton.setOnClickListener {
                checkRuntimePermission()

                uncheckBoxAll()
            }
            jazzySoundLayout.setOnClickListener {
                openCloseAllJazzy(jazzyToClose)
            }
            funkySoundLayout.setOnClickListener {
                openCloseAllFunky(funkyToClose)
            }
            relaxSoundLayout.setOnClickListener {
                openCloseAllRelax(relaxToClose)
            }
            uploadCustomSoundLayout.setOnClickListener {
                openCloseUpload(uploadToClose)
            }

            // Jazz Checkboxes
            moaninCheckbox.setOnClickListener {
                uncheckBoxAll()
                moaninCheckbox.isChecked = true
                playAlarmSound(R.raw.moanin_jazz)

                if (jazzySoundBought) {
                    saveAlarmToneChoose(R.raw.moanin_jazz)
                } else {
                    alertDialogBuySound()
                }
            }
            blueBossaCheckbox.setOnClickListener {
                stopAlarmSound()
                uncheckBoxAll()
                blueBossaCheckbox.isChecked = true

                if (jazzySoundBought) {
                    playAlarmSound(R.raw.blue_bossa)
                    saveAlarmToneChoose(R.raw.blue_bossa)
                } else {
                    alertDialogBuySound()
                }
            }
            caravanCheckbox.setOnClickListener {
                stopAlarmSound()
                uncheckBoxAll()
                caravanCheckbox.isChecked = true

                if (jazzySoundBought) {
                    playAlarmSound(R.raw.caravan)
                    saveAlarmToneChoose(R.raw.caravan)
                } else {
                    alertDialogBuySound()
                }
            }
            dolphinDanceCheckbox.setOnClickListener {
                stopAlarmSound()
                uncheckBoxAll()
                dolphinDanceCheckbox.isChecked = true

                if (jazzySoundBought) {
                    playAlarmSound(R.raw.dolphin_dance)
                    saveAlarmToneChoose(R.raw.dolphin_dance)
                } else {
                    alertDialogBuySound()
                }
            }
            autumnLeavesCheckbox.setOnClickListener {
                stopAlarmSound()
                uncheckBoxAll()
                autumnLeavesCheckbox.isChecked = true

                if (jazzySoundBought) {
                    playAlarmSound(R.raw.autumn_leaves)
                    saveAlarmToneChoose(R.raw.autumn_leaves)
                } else {
                    alertDialogBuySound()
                }
            }
            freddieFreeloaderCheckbox.setOnClickListener {
                stopAlarmSound()
                uncheckBoxAll()
                freddieFreeloaderCheckbox.isChecked = true

                if (jazzySoundBought) {
                    playAlarmSound(R.raw.freddie_freeloader)
                    saveAlarmToneChoose(R.raw.freddie_freeloader)
                } else {
                    alertDialogBuySound()
                }
            }

            // Funky Checkboxes
            slapCheckbox.setOnClickListener {
                uncheckBoxAll()
                slapCheckbox.isChecked = true
                playAlarmSound(R.raw.bass_slap)

                if (funkySoundBought) {
                    saveAlarmToneChoose(R.raw.bass_slap)
                } else {
                    alertDialogBuySound()
                }
            }
            offTheCurveCheckbox.setOnClickListener {
                uncheckBoxAll()
                offTheCurveCheckbox.isChecked = true

                if (funkySoundBought) {
                    playAlarmSound(R.raw.off_the_curve_groove)
                    saveAlarmToneChoose(R.raw.off_the_curve_groove)
                } else {
                    alertDialogBuySound()
                }
            }
            funkYallCheckbox.setOnClickListener {
                uncheckBoxAll()
                funkYallCheckbox.isChecked = true

                if (funkySoundBought) {
                    playAlarmSound(R.raw.funk_yall)
                    saveAlarmToneChoose(R.raw.funk_yall)
                } else {
                    alertDialogBuySound()
                }
            }
            keyboardFunkyToneCheckbox.setOnClickListener {
                uncheckBoxAll()
                keyboardFunkyToneCheckbox.isChecked = true

                if (funkySoundBought) {
                    playAlarmSound(R.raw.keyboard_funky_tone)
                    saveAlarmToneChoose(R.raw.keyboard_funky_tone)
                } else {
                    alertDialogBuySound()
                }
            }
            uCantHoldNoGrooveCheckbox.setOnClickListener {
                uncheckBoxAll()
                uCantHoldNoGrooveCheckbox.isChecked = true

                if (funkySoundBought) {
                    playAlarmSound(R.raw.u_cant_hold_no_groove)
                    saveAlarmToneChoose(R.raw.u_cant_hold_no_groove)
                } else {
                    alertDialogBuySound()
                }
            }
            coldSweatCheckbox.setOnClickListener {
                uncheckBoxAll()
                coldSweatCheckbox.isChecked = true

                if (funkySoundBought) {
                    playAlarmSound(R.raw.cold_sweat)
                    saveAlarmToneChoose(R.raw.cold_sweat)
                } else {
                    alertDialogBuySound()
                }
            }

            // Relax Checkboxes
            xyloRelaxCheckbox.setOnClickListener {
                uncheckBoxAll()
                xyloRelaxCheckbox.isChecked = true
                playAlarmSound(R.raw.xylophone_tone)

                if (relaxSoundBought) {
                    saveAlarmToneChoose(R.raw.xylophone_tone)
                } else {
                    alertDialogBuySound()
                }
            }
            guitarRelaxCheckbox.setOnClickListener {
                uncheckBoxAll()
                guitarRelaxCheckbox.isChecked = true

                if (relaxSoundBought) {
                    playAlarmSound(R.raw.beautiful_chords_progression)
                    saveAlarmToneChoose(R.raw.beautiful_chords_progression)
                } else {
                    alertDialogBuySound()
                }
            }
            gravityCheckbox.setOnClickListener {
                uncheckBoxAll()
                gravityCheckbox.isChecked = true

                if (relaxSoundBought) {
                    playAlarmSound(R.raw.gravity)
                    saveAlarmToneChoose(R.raw.gravity)
                } else {
                    alertDialogBuySound()
                }
            }
            slowDancingCheckbox.setOnClickListener {
                uncheckBoxAll()
                slowDancingCheckbox.isChecked = true

                if (relaxSoundBought) {
                    playAlarmSound(R.raw.slow_dancing)
                    saveAlarmToneChoose(R.raw.slow_dancing)
                } else {
                    alertDialogBuySound()
                }
            }
            scorpionThemeCheckbox.setOnClickListener {
                uncheckBoxAll()
                scorpionThemeCheckbox.isChecked = true

                if (relaxSoundBought) {
                    playAlarmSound(R.raw.fade_to_black)
                    saveAlarmToneChoose(R.raw.fade_to_black)
                } else {
                    alertDialogBuySound()
                }
            }
            interstellarThemeCheckbox.setOnClickListener {
                uncheckBoxAll()
                interstellarThemeCheckbox.isChecked = true

                if (relaxSoundBought) {
                    playAlarmSound(R.raw.interstellar_main_theme)
                    saveAlarmToneChoose(R.raw.interstellar_main_theme)
                } else {
                    alertDialogBuySound()
                }
            }
        }

        //endregion
    }

    //region ========================================== Functions ===========================================

    private fun checkIfUserBoughtCustomSound() {
        binding.apply {
            uploadButton.isVisible = customSoundBought
            uploadSongsLayout.isVisible = customSoundBought
            uploadCustomSoundLayout.isVisible = customSoundBought
        }
    }

    private fun backIconClick(id: Int) {
        stopAlarmSound()
        val intent = Intent(this, EditContactDetailsActivity::class.java)
        intent.putExtra("ContactId", id)
        if (numberDefault != 1) {
            intent.putExtra("AlarmTone", numberDefault)
        }
        startActivity(intent)
    }

    private fun changeIconOpenLayout(img: AppCompatImageView, toClose: Boolean) {
        if (toClose) {
            img.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_bottom_arrow,
                    null
                )
            )
        } else {
            img.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_top_arrow,
                    null
                )
            )
        }
    }

    private fun alertDialogBuySound() {
        MaterialAlertDialogBuilder(this, R.style.AlertDialog)
            .setTitle(getString(R.string.in_app_popup_tone_available_title))
            .setMessage(getString(R.string.in_app_popup_tone_available_message))
            .setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
                goToPremiumActivity()
            }
            .setNegativeButton(R.string.alert_dialog_later) { _, _ ->
                refreshChecked()
            }
            .show()
    }

    private fun getTones() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "audio/*"
        startActivityForResult(Intent.createChooser(intent, "Title"), 89)
    }

    private fun stopAlarmSound() {
        Log.i("alarmTone", "stop")
        alarmSound?.stop()
    }

    private fun playAlarmSound(id: Int) {
        stopAlarmSound()
        alarmSound = MediaPlayer.create(this@VipSettingsActivity, id)
        alarmSound?.start()
    }

    private fun saveAlarmToneChoose(id: Int) {
        val edit: SharedPreferences.Editor = alarmTonePreferences.edit()
        edit.putString("Alarm_Custom_Tone", null)
        edit.apply()

        numberDefault = id
    }

    //region ======================================= CheckboxGesture ========================================

    private fun uncheckBoxAll() {
        uncheckBoxAllJazzy()
        uncheckBoxAllFunky()
        uncheckBoxAllRelax()
    }

    private fun uncheckBoxAllJazzy() {
        binding.apply {
            moaninCheckbox.isChecked = false
            blueBossaCheckbox.isChecked = false
            caravanCheckbox.isChecked = false
            dolphinDanceCheckbox.isChecked = false
            autumnLeavesCheckbox.isChecked = false
            freddieFreeloaderCheckbox.isChecked = false
        }
    }

    private fun uncheckBoxAllFunky() {
        binding.apply {
            slapCheckbox.isChecked = false
            offTheCurveCheckbox.isChecked = false
            funkYallCheckbox.isChecked = false
            keyboardFunkyToneCheckbox.isChecked = false
            uCantHoldNoGrooveCheckbox.isChecked = false
            coldSweatCheckbox.isChecked = false
        }
    }

    private fun uncheckBoxAllRelax() {
        binding.apply {
            guitarRelaxCheckbox.isChecked = false
            gravityCheckbox.isChecked = false
            slowDancingCheckbox.isChecked = false
            scorpionThemeCheckbox.isChecked = false
            interstellarThemeCheckbox.isChecked = false
            xyloRelaxCheckbox.isChecked = false
        }
    }

    private fun refreshChecked() {
        stopAlarmSound()
        uncheckBoxAll()
        binding.apply {
            when (numberDefault) {
                1 -> {}
                R.raw.moanin_jazz -> {
                    moaninCheckbox.isChecked = true
                }
                R.raw.blue_bossa -> {
                    blueBossaCheckbox.isChecked = true
                }
                R.raw.caravan -> {
                    caravanCheckbox.isChecked = true
                }
                R.raw.dolphin_dance -> {
                    dolphinDanceCheckbox.isChecked = true
                }
                R.raw.autumn_leaves -> {
                    autumnLeavesCheckbox.isChecked = true
                }
                R.raw.freddie_freeloader -> {
                    freddieFreeloaderCheckbox.isChecked = true
                }
                R.raw.bass_slap -> {
                    slapCheckbox.isChecked = true
                }
                R.raw.funk_yall -> {
                    funkYallCheckbox.isChecked = true
                }
                R.raw.off_the_curve_groove -> {
                    offTheCurveCheckbox.isChecked = true
                }
                R.raw.keyboard_funky_tone -> {
                    keyboardFunkyToneCheckbox.isChecked = true
                }
                R.raw.u_cant_hold_no_groove -> {
                    uCantHoldNoGrooveCheckbox.isChecked = true
                }
                R.raw.cold_sweat -> {
                    coldSweatCheckbox.isChecked = true
                }
                R.raw.beautiful_chords_progression -> {
                    guitarRelaxCheckbox.isChecked = true
                }
                R.raw.gravity -> {
                    gravityCheckbox.isChecked = true
                }
                R.raw.fade_to_black -> {
                    scorpionThemeCheckbox.isChecked = true
                }
                R.raw.slow_dancing -> {
                    slowDancingCheckbox.isChecked = true
                }
                R.raw.relax_sms -> {
                    xyloRelaxCheckbox.isChecked = true
                }
                R.raw.interstellar_main_theme -> {
                    interstellarThemeCheckbox.isChecked = true
                }
            }
        }
    }

    //endregion

    //region ======================================= OpenCloseLayout ========================================

    private fun ringToneLayoutClosed() {
        openCloseAllJazzy(jazzyToClose)
        openCloseAllFunky(funkyToClose)
        openCloseAllRelax(relaxToClose)
    }

    private fun openCloseAllJazzy(isOpen: Boolean) {
        binding.apply {
            moaninLayout.isVisible = !isOpen
            blueBossaLayout.isVisible = !isOpen
            caravanLayout.isVisible = !isOpen
            dolphinDanceLayout.isVisible = !isOpen
            autumnLeavesLayout.isVisible = !isOpen
            freddieFreeloaderLayout.isVisible = !isOpen

            changeIconOpenLayout(jazzyCloseIcon, isOpen)

            jazzyToClose = !jazzyToClose
        }
    }

    private fun openCloseAllFunky(isOpen: Boolean) {
        binding.apply {
            slapLayout.isVisible = !isOpen
            offTheCurveLayout.isVisible = !isOpen
            funkYallLayout.isVisible = !isOpen
            keyboardFunkyToneLayout.isVisible = !isOpen
            uCantHoldNoGrooveLayout.isVisible = !isOpen
            coldSweatLayout.isVisible = !isOpen

            changeIconOpenLayout(funkySoundCloseIcon, isOpen)

            funkyToClose = !funkyToClose
        }
    }

    private fun openCloseAllRelax(isOpen: Boolean) {
        binding.apply {
            guitarRelaxLayout.isVisible = !isOpen
            gravityLayout.isVisible = !isOpen
            slowDancingLayout.isVisible = !isOpen
            scorpionThemeLayout.isVisible = !isOpen
            interstellarThemeLayout.isVisible = !isOpen
            xyloRelaxLayout.isVisible = !isOpen

            changeIconOpenLayout(relaxSoundIcon, isOpen)

            relaxToClose = !relaxToClose
        }
    }

    private fun openCloseUpload(isOpen: Boolean) {
        binding.apply {
            uploadSongsLayout.isVisible = !isOpen
            changeIconOpenLayout(uploadCloseIcon, isOpen)
            relaxToClose = !relaxToClose
        }
    }

    //endregion

    //check if you have permission or not
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            ManageNotificationActivity.PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission from popup granted
                    getTones()
                } else {
                    //permission from popup denied
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun checkRuntimePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                requestPermissions(permissions, ManageNotificationActivity.PERMISSION_CODE)
            } else {
                getTones()
            }
        } else {
            getTones()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 89 && resultCode == RESULT_OK) {
            if (data?.data != null) {
                val audioFileUri = data.data
                // use uri to get path
                val path = audioFileUri?.path
//                jazzyUploadSoundPath.text = "From :$path"
                val alarmTonePreferences: SharedPreferences =
                    getSharedPreferences("Alarm_Custom_Tone", Context.MODE_PRIVATE)
                val edit: SharedPreferences.Editor = alarmTonePreferences.edit()
                edit.putString("Alarm_Custom_Tone", audioFileUri.toString())
                edit.apply()
            }
        }
    }

    override fun onBackPressed() {
        stopAlarmSound()
        super.onBackPressed()
    }

    private fun goToPremiumActivity() {
        startActivity(
            Intent(
                this@VipSettingsActivity,
                PremiumActivity::class.java
            ).putExtra("fromManageNotification", true)
        )
        finish()
    }

    //endregion
}