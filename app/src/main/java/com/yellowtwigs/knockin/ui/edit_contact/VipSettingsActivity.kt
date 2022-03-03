package com.yellowtwigs.knockin.ui.edit_contact

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.Cursor
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.ActivityVipSettingsBinding
import com.yellowtwigs.knockin.model.ContactManager
import com.yellowtwigs.knockin.ui.in_app.PremiumActivity
import com.yellowtwigs.knockin.ui.settings.ManageNotificationActivity
import kotlinx.coroutines.*
import java.text.DateFormat
import java.util.*

class VipSettingsActivity : AppCompatActivity() {

    //region ========================================== Var or Val ==========================================

    private var alarmSound: MediaPlayer? = null

    private var funkySoundBought: Boolean = false
    private var jazzySoundBought: Boolean = false
    private var relaxSoundBought: Boolean = false

    private var jazzyToClose = true
    private var funkyToClose = true
    private var relaxToClose = true
    private var uploadToClose = true

    private var audioFile = ""
    private var fileId = ""

    private lateinit var alarmTonePreferences: SharedPreferences
    private lateinit var fileIdPreferences: SharedPreferences
    private lateinit var schedulePreferences: SharedPreferences

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
        fileIdPreferences = getSharedPreferences("File_Id", Context.MODE_PRIVATE)
        schedulePreferences = getSharedPreferences("Schedule_VIP", Context.MODE_PRIVATE)

        val jazzySoundPreferences = getSharedPreferences("Jazzy_Sound_Bought", Context.MODE_PRIVATE)
        jazzySoundBought = jazzySoundPreferences.getBoolean("Jazzy_Sound_Bought", true)

        val relaxSoundPreferences = getSharedPreferences("Relax_Sound_Bought", Context.MODE_PRIVATE)
        relaxSoundBought = relaxSoundPreferences.getBoolean("Relax_Sound_Bought", false)

        val funkySoundPreferences = getSharedPreferences("Funky_Sound_Bought", Context.MODE_PRIVATE)
        funkySoundBought = funkySoundPreferences.getBoolean("Funky_Sound_Bought", false)

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

        refreshChecked()
        checkIfUserBoughtCustomSound()
        ringToneLayoutClosed()

        //region ======================================== Listeners =========================================

        binding.apply {
            backIcon.setOnClickListener {
                backIconClick(contactId)
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

            //region ================================ Jazz Checkboxes ================================

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

            //endregion

            //region ================================ Funky Checkboxes ================================

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

            //endregion

            //region ================================ Relax Checkboxes ================================

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

            //endregion

            uploadButton.setOnClickListener {
                alarmSound?.stop()
                checkRuntimePermission()
            }

            if (numberDefault == -1) {
                audioFile = alarmTonePreferences.getString("Alarm_Custom_Tone", "").toString()
            }

            uploadCheckbox.isChecked = numberDefault == -1
            uploadCheckbox.isVisible = numberDefault == -1

            uploadCheckbox.setOnClickListener {
                uncheckBoxAll()
                uploadCheckbox.isChecked = true

                CoroutineScope(Dispatchers.Main).launch {
                    alarmSound?.stop()
                    alarmSound = MediaPlayer.create(this@VipSettingsActivity, Uri.parse(audioFile))
                    alarmSound?.start()
                    delay(15000)
                    alarmSound?.stop()
                }

                numberDefault = -1
            }
        }

        binding.apply {
            permanentRadioButton.setOnClickListener {
                permanentRadioButton.isChecked = true
                daytimeRadioButton.isChecked = false
                workweekRadioButton.isChecked = false
                scheduleMixRadioButton.isChecked = false

                val edit = schedulePreferences.edit()
                edit.putInt("Schedule_VIP", 1)
                edit.apply()
            }

            daytimeRadioButton.setOnClickListener {
                permanentRadioButton.isChecked = false
                daytimeRadioButton.isChecked = true
                workweekRadioButton.isChecked = false
                scheduleMixRadioButton.isChecked = false

                val edit = schedulePreferences.edit()
                edit.putInt("Schedule_VIP", 2)
                edit.apply()
            }

            workweekRadioButton.setOnClickListener {
                permanentRadioButton.isChecked = false
                daytimeRadioButton.isChecked = false
                workweekRadioButton.isChecked = true
                scheduleMixRadioButton.isChecked = false

                val edit = schedulePreferences.edit()
                edit.putInt("Schedule_VIP", 3)
                edit.apply()
            }

            scheduleMixRadioButton.setOnClickListener {
                permanentRadioButton.isChecked = false
                daytimeRadioButton.isChecked = false
                workweekRadioButton.isChecked = false
                scheduleMixRadioButton.isChecked = true

                val edit = schedulePreferences.edit()
                edit.putInt("Schedule_VIP", 4)
                edit.apply()
            }
        }

        binding.apply {
            when (schedulePreferences.getInt("Schedule_VIP", 1)) {
                1 -> {
                    permanentRadioButton.isChecked = true
                }
                2 -> {
                    daytimeRadioButton.isChecked = true
                }
                3 -> {
                    workweekRadioButton.isChecked = true
                }
                4 -> {
                    scheduleMixRadioButton.isChecked = true
                }
                else -> {
                    permanentRadioButton.isChecked = true
                }
            }
        }

        //endregion

        getAudioNameFromStorage(fileIdPreferences.getString("File_Id", ""))
    }

    //region ========================================== Functions ===========================================

    private fun checkIfUserBoughtCustomSound() {
        binding.apply {
            uploadButton.isVisible = true
            uploadSoundPath.isVisible = true
            uploadSongsLayout.isVisible = true
            uploadCustomSoundLayout.isVisible = true
        }
    }

    private fun backIconClick(id: Int) {
        stopAlarmSound()
        val intentBack = Intent(this, EditContactDetailsActivity::class.java)
        fillIntent(intentBack, id)
        if (numberDefault != 1) {
            intentBack.putExtra("AlarmTone", numberDefault)
        }

        if (binding.uploadCheckbox.isChecked) {
            val edit = alarmTonePreferences.edit()
            edit.putString("Alarm_Custom_Tone", audioFile)
            edit.apply()
        }

        startActivity(intentBack)
    }

    private fun fillIntent(intentBack: Intent, id: Int) {
        intentBack.apply {
            putExtra("ContactId", id)

            if (intent.getBooleanExtra("hasChanged", false)) {
                putExtra("FirstName", intent.getStringExtra("FirstName"))
                putExtra("Lastname", intent.getStringExtra("Lastname"))
                putExtra("PhoneNumber", intent.getStringExtra("PhoneNumber"))
                putExtra("FixNumber", intent.getStringExtra("FixNumber"))
                putExtra("Mail", intent.getStringExtra("Mail"))
                putExtra("MailId", intent.getStringExtra("MailId"))
                putExtra("Priority", intent.getIntExtra("Priority", 0))
                putExtra("isFavorite", intent.getBooleanExtra("isFavorite", false))
                putExtra("hasChanged", intent.getBooleanExtra("hasChanged", false))
            }
        }
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

    private fun stopAlarmSound() {
        alarmSound?.stop()
    }

    private fun playAlarmSound(id: Int) {
        stopAlarmSound()
        alarmSound = MediaPlayer.create(this@VipSettingsActivity, id)
        alarmSound?.start()
    }

    private fun saveAlarmToneChoose(id: Int) {
        val edit: SharedPreferences.Editor = alarmTonePreferences.edit()
        edit.putString("Alarm_Custom_Tone", "")
        edit.apply()

        numberDefault = id
    }

    //region ======================================= CheckboxGesture ========================================

    private fun uncheckBoxAll() {
        uncheckBoxAllJazzy()
        uncheckBoxAllFunky()
        uncheckBoxAllRelax()
        binding.uploadCheckbox.isChecked = false
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
                else -> {
                    uploadCheckbox.isChecked = true
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
        openCloseUpload(uploadToClose)
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
            uploadToClose = !uploadToClose
        }
    }

    //endregion

    private fun getTones() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "audio/*"
        startActivityForResult(Intent.createChooser(intent, "Title"), 89)
//        loadAudioFiles()
    }

    private fun getAudioNameFromStorage(audioId: String?) {
        CoroutineScope(Dispatchers.IO).launch {
            val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0"
            val sortOrder = MediaStore.Audio.Media.TITLE + " ASC"
            val cursor: Cursor? = contentResolver.query(
                uri, null, selection, null,
                sortOrder
            )
            if (cursor != null && cursor.moveToFirst()) {
                val id: Int = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
                val title: Int = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                do {
                    val audioFileId = cursor.getLong(id)
                    if (audioFileId.toString() == audioId) {

                        audioFile = cursor.getString(23)

                        withContext(Dispatchers.Main) {
                            binding.uploadSoundPath.isVisible = true
                            binding.uploadSoundPath.text = cursor.getString(title)
                        }
                        break
                    }

                } while (cursor.moveToNext())
            }
        }
    }

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
                    getTones()
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun checkRuntimePermission() {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            requestPermissions(permissions, ManageNotificationActivity.PERMISSION_CODE)
        } else {
            getTones()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 89 && resultCode == RESULT_OK) {
            if (data?.data != null) {
                fileId = data.data?.lastPathSegment?.split(":")?.get(1).toString()

                val edit = fileIdPreferences.edit()
                edit.putString("File_Id", fileId)
                edit.apply()

                getAudioNameFromStorage(fileId)
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