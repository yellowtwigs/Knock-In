package com.yellowtwigs.knockin.ui.edit_contact

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.SQLException
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.ActivityVipSettingsBinding
import com.yellowtwigs.knockin.model.data.ContactWithAllInformation
import com.yellowtwigs.knockin.ui.contacts.ContactsViewModel
import com.yellowtwigs.knockin.ui.in_app.PremiumActivity
import com.yellowtwigs.knockin.ui.settings.ManageNotificationActivity
import com.yellowtwigs.knockin.utils.EveryActivityUtils.checkThemePreferences
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*

@AndroidEntryPoint
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
    private var currentSound = ""
    private var isCustomSound = false
    private var vipScheduleValue = 1
    private var hourLimit = ""

    private lateinit var permissionsPreferences: SharedPreferences

    private var numberDefault = 1
    private lateinit var binding: ActivityVipSettingsBinding

    private lateinit var currentContact: ContactWithAllInformation

    private val contactsViewModel: ContactsViewModel by viewModels()

    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkThemePreferences(this)

        binding = ActivityVipSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        permissionsPreferences =
            getSharedPreferences("PermissionsPreferences", Context.MODE_PRIVATE)

        val jazzySoundPreferences = getSharedPreferences("Jazzy_Sound_Bought", Context.MODE_PRIVATE)
//        jazzySoundBought = jazzySoundPreferences.getBoolean("Jazzy_Sound_Bought", false)
        jazzySoundBought = jazzySoundPreferences.getBoolean("Jazzy_Sound_Bought", true)

        val relaxSoundPreferences = getSharedPreferences("Relax_Sound_Bought", Context.MODE_PRIVATE)
        relaxSoundBought = relaxSoundPreferences.getBoolean("Relax_Sound_Bought", false)

        val funkySoundPreferences = getSharedPreferences("Funky_Sound_Bought", Context.MODE_PRIVATE)
        funkySoundBought = funkySoundPreferences.getBoolean("Funky_Sound_Bought", false)

        contactsViewModel.contactLiveData.observe(this) {
            currentContact = it
        }

        val contactName =
            "${currentContact.contactDB?.firstName} ${currentContact.contactDB?.lastName}"
        binding.contactName.text = contactName

        if (currentContact.contactDB?.notificationTone != null) {
            currentSound = currentContact.contactDB?.notificationTone!!
            isCustomSound = currentContact.contactDB?.isCustomSound == 1
        }

        vipScheduleValue = currentContact.contactDB?.vipSchedule!!

        refreshChecked()
        checkIfUserBoughtCustomSound()
        ringToneLayoutClosed()

        //region ======================================== Listeners =========================================

        binding.apply {
            backIcon.setOnClickListener {
                backIconClick()
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

            //region =================================== Jazz Checkboxes ====================================

            moaninCheckbox.setOnClickListener {
                onClickFirstCheckbox(moaninCheckbox, R.raw.moanin_jazz.toString(), jazzySoundBought)
            }
            blueBossaCheckbox.setOnClickListener {
                onClickCheckbox(blueBossaCheckbox, R.raw.blue_bossa.toString(), jazzySoundBought)
            }
            caravanCheckbox.setOnClickListener {
                onClickCheckbox(caravanCheckbox, R.raw.caravan.toString(), jazzySoundBought)
            }
            dolphinDanceCheckbox.setOnClickListener {
                onClickCheckbox(
                    dolphinDanceCheckbox,
                    R.raw.dolphin_dance.toString(),
                    jazzySoundBought
                )
            }
            autumnLeavesCheckbox.setOnClickListener {
                onClickCheckbox(
                    autumnLeavesCheckbox,
                    R.raw.autumn_leaves.toString(),
                    jazzySoundBought
                )
            }
            freddieFreeloaderCheckbox.setOnClickListener {
                onClickCheckbox(
                    freddieFreeloaderCheckbox,
                    R.raw.freddie_freeloader.toString(),
                    jazzySoundBought
                )
            }

            //endregion

            //region =================================== Funky Checkboxes ===================================

            slapCheckbox.setOnClickListener {
                onClickFirstCheckbox(slapCheckbox, R.raw.bass_slap.toString(), funkySoundBought)
            }
            offTheCurveCheckbox.setOnClickListener {
                onClickCheckbox(
                    offTheCurveCheckbox,
                    R.raw.off_the_curve_groove.toString(),
                    funkySoundBought
                )
            }
            funkYallCheckbox.setOnClickListener {
                onClickCheckbox(funkYallCheckbox, R.raw.funk_yall.toString(), funkySoundBought)
            }
            keyboardFunkyToneCheckbox.setOnClickListener {
                onClickCheckbox(
                    keyboardFunkyToneCheckbox,
                    R.raw.keyboard_funky_tone.toString(),
                    funkySoundBought
                )
            }
            uCantHoldNoGrooveCheckbox.setOnClickListener {
                onClickCheckbox(
                    uCantHoldNoGrooveCheckbox,
                    R.raw.u_cant_hold_no_groove.toString(),
                    funkySoundBought
                )
            }
            coldSweatCheckbox.setOnClickListener {
                onClickCheckbox(coldSweatCheckbox, R.raw.cold_sweat.toString(), funkySoundBought)
            }

            //endregion

            //region =================================== Relax Checkboxes ===================================

            xyloRelaxCheckbox.setOnClickListener {
                onClickFirstCheckbox(
                    xyloRelaxCheckbox,
                    R.raw.xylophone_tone.toString(),
                    relaxSoundBought
                )
            }
            guitarRelaxCheckbox.setOnClickListener {
                onClickCheckbox(
                    guitarRelaxCheckbox,
                    R.raw.beautiful_chords_progression.toString(),
                    relaxSoundBought
                )
            }
            gravityCheckbox.setOnClickListener {
                onClickCheckbox(gravityCheckbox, R.raw.gravity.toString(), relaxSoundBought)
            }
            slowDancingCheckbox.setOnClickListener {
                onClickCheckbox(
                    slowDancingCheckbox,
                    R.raw.slow_dancing.toString(),
                    relaxSoundBought
                )
            }
            scorpionThemeCheckbox.setOnClickListener {
                onClickCheckbox(
                    scorpionThemeCheckbox,
                    R.raw.fade_to_black.toString(),
                    relaxSoundBought
                )
            }
            interstellarThemeCheckbox.setOnClickListener {
                onClickCheckbox(
                    interstellarThemeCheckbox,
                    R.raw.interstellar_main_theme.toString(),
                    relaxSoundBought
                )
            }

            //endregion

            uploadCheckbox.isChecked = isCustomSound
            uploadCheckbox.isVisible = isCustomSound

            uploadButton.setOnClickListener {
                alarmSound?.stop()
                checkRuntimePermission()
            }

            uploadCheckbox.setOnClickListener {
                onClickCheckbox(uploadCheckbox, currentSound, true)
                uncheckBoxAll()
                uploadCheckbox.isChecked = true

                CoroutineScope(Dispatchers.Main).launch {
                    alarmSound?.stop()
                    alarmSound =
                        MediaPlayer.create(this@VipSettingsActivity, Uri.parse(currentSound))
                    alarmSound?.start()
                    delay(7000)
                    alarmSound?.stop()
                }
            }
            permanentRadioButton.setOnClickListener {
                vipScheduleValue = 1
                permanentRadioButton.isChecked = true
                daytimeRadioButton.isChecked = false
                workweekRadioButton.isChecked = false
                scheduleMixRadioButton.isChecked = false
            }

            daytimeRadioButton.setOnClickListener {
                vipScheduleValue = 2
                permanentRadioButton.isChecked = false
                daytimeRadioButton.isChecked = true
                workweekRadioButton.isChecked = false
                scheduleMixRadioButton.isChecked = false
            }

            workweekRadioButton.setOnClickListener {
                vipScheduleValue = 3
                permanentRadioButton.isChecked = false
                daytimeRadioButton.isChecked = false
                workweekRadioButton.isChecked = true
                scheduleMixRadioButton.isChecked = false
            }

            scheduleMixRadioButton.setOnClickListener {
                vipScheduleValue = 4
                permanentRadioButton.isChecked = false
                daytimeRadioButton.isChecked = false
                workweekRadioButton.isChecked = false
                scheduleMixRadioButton.isChecked = true
            }

            when (vipScheduleValue) {
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

        if (permissionsPreferences.getBoolean("permissionsPreferences", false) && isCustomSound) {
            getAudioNameFromStorage(currentSound)
        }
    }

    //region ========================================== Functions ===========================================

    private fun checkIfUserBoughtCustomSound() {
        binding.apply {
            val isBought = jazzySoundBought || relaxSoundBought || funkySoundBought
            uploadButton.isVisible = isBought
            uploadSoundPath.isVisible = isBought
            uploadSongsLayout.isVisible = isBought
            uploadCustomSoundLayout.isVisible = isBought
        }
    }

    private fun onClickFirstCheckbox(
        checkBox: AppCompatCheckBox, sound: String, isBought: Boolean
    ) {
        uncheckBoxAll()
        checkBox.isChecked = true
        currentSound = sound
        playAlarmSound()

        if (!isBought) {
            currentSound = currentContact.contactDB?.notificationTone.toString()
            alertDialogBuySound()
        }
    }

    private fun onClickCheckbox(checkBox: AppCompatCheckBox, sound: String, isBought: Boolean) {
        uncheckBoxAll()
        checkBox.isChecked = true

        if (isBought) {
            currentSound = sound
            playAlarmSound()
        } else {
            currentSound = currentContact.contactDB?.notificationTone.toString()
            alertDialogBuySound()
        }
    }

    private fun backIconClick() {
        alarmSound?.stop()
        val intentBack = Intent(this, EditContactDetailsActivity::class.java)
        fillIntent(intentBack)
        startActivity(intentBack)
    }

    private fun fillIntent(intentBack: Intent) {
        intentBack.apply {
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
                putExtra("currentSound", currentSound)
                if (isCustomSound) {
                    putExtra("isCustomSound", 1)
                } else {
                    putExtra("isCustomSound", 0)
                }
                putExtra("vipScheduleValue", vipScheduleValue)
                putExtra("hourLimit", hourLimit)
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

    private fun playAlarmSound() {
        CoroutineScope(Dispatchers.Main).launch {
            alarmSound?.stop()
            alarmSound = if (isCustomSound) {
                MediaPlayer.create(this@VipSettingsActivity, Uri.parse(currentSound))
            } else {
                MediaPlayer.create(this@VipSettingsActivity, currentSound.toInt())
            }
            alarmSound?.start()
            delay(7000)
            alarmSound?.stop()
        }
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
        alarmSound?.stop()
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
    }

    private fun getAudioNameFromStorage(audioId: String?) {
        CoroutineScope(Dispatchers.IO).launch {
            val uri = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }
            val selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0"
            val sortOrder = MediaStore.Audio.Media.TITLE + " ASC"
            val cursor = contentResolver.query(
                uri, null, selection, null,
                sortOrder
            )

            if (cursor != null && cursor.moveToFirst()) {
                val id = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
                val title = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)

                do {
                    val audioFileId = cursor.getLong(id)
                    if (audioFileId.toString() == audioId) {

                        for (i in 0..title) {
                            try {
                                if (cursor.getString(i)?.contains("storage") == true &&
                                    cursor.getString(i)?.contains(".mp3") == true
                                ) {
                                    currentSound = cursor.getString(i)
                                    break
                                }
                            } catch (e: SQLException) {
                                Log.i("audioFile", "$i : ${cursor.getBlob(i)}")
                            }
                        }

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
            val edit = permissionsPreferences.edit()
            edit.putBoolean("PermissionsPreferences", true)
            edit.apply()
            getTones()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 89 && resultCode == RESULT_OK) {
            if (data?.data != null) {
                audioFile = data.data.toString()
                binding.uploadSoundPath.isVisible = true
                binding.uploadSoundPath.text =
                    data.data?.lastPathSegment?.split(":")?.get(1).toString()

                binding.uploadCheckbox.isVisible = true

                getAudioNameFromStorage(audioFile)
            }
        }
    }

    override fun onBackPressed() {
        alarmSound?.stop()
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