package com.yellowtwigs.knockin.ui.add_edit_contact.vip_settings

import android.Manifest
import android.app.TimePickerDialog
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
import android.widget.TimePicker
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
import com.yellowtwigs.knockin.model.database.data.ContactDB
import com.yellowtwigs.knockin.ui.add_edit_contact.edit.EditContactActivity
import com.yellowtwigs.knockin.ui.add_edit_contact.edit.EditContactViewModel
import com.yellowtwigs.knockin.ui.premium.PremiumActivity
import com.yellowtwigs.knockin.ui.notifications.settings.NotificationsSettingsActivity
import com.yellowtwigs.knockin.utils.EveryActivityUtils.checkTheme
import com.yellowtwigs.knockin.repositories.firebase.FirebaseViewModel
import com.yellowtwigs.knockin.ui.add_edit_contact.edit.EditContactViewState
import com.yellowtwigs.knockin.utils.SaveUserIdToFirebase.saveUserIdToFirebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*

@AndroidEntryPoint
class VipSettingsActivity : AppCompatActivity() {

    //region ========================================== Var or Val ==========================================

    private val editContactViewModel: EditContactViewModel by viewModels()
    private val firebaseViewModel: FirebaseViewModel by viewModels()

    private lateinit var userIdPreferences: SharedPreferences

    private var alarmSound: MediaPlayer? = null

    private var funkySoundBought: Boolean = false
    private var jazzySoundBought: Boolean = false
    private var relaxSoundBought: Boolean = false

    private var jazzyToClose = true
    private var funkyToClose = true
    private var relaxToClose = true
    private var uploadToClose = true

    private var audioFile = ""
    private var currentAudioFile = ""

    private var notificationTone = ""
    private var currentNotificationTone = ""

    private var notificationSound = R.raw.sms_ring
    private var currentNotificationSound = R.raw.sms_ring

    private var hourLimit = ""
    private var currentHourLimit = ""

    private var vipScheduleValue = 1
    private var currentVipScheduleValue = 1

    private var isCustomSound = false
    private var currentIsCustomSound = false

    private lateinit var permissionsPref: SharedPreferences
    private lateinit var binding: ActivityVipSettingsBinding

    private lateinit var currentViewState: EditContactViewState

    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkTheme(this)

        binding = ActivityVipSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        permissionsPref = getSharedPreferences("PermissionsPreferences", Context.MODE_PRIVATE)

        val jazzySoundPreferences = getSharedPreferences("Jazzy_Sound_Bought", Context.MODE_PRIVATE)
        jazzySoundBought = jazzySoundPreferences.getBoolean("Jazzy_Sound_Bought", false)

        val relaxSoundPreferences = getSharedPreferences("Relax_Sound_Bought", Context.MODE_PRIVATE)
        relaxSoundBought = relaxSoundPreferences.getBoolean("Relax_Sound_Bought", false)

        val funkySoundPreferences = getSharedPreferences("Funky_Sound_Bought", Context.MODE_PRIVATE)
        funkySoundBought = funkySoundPreferences.getBoolean("Funky_Sound_Bought", false)

        val contactId = intent.getIntExtra("ContactId", 0)
        editContactViewModel.getContactById(contactId).observe(this) { viewState ->
            currentViewState = viewState
            binding.contactName.text = "${viewState.firstName} ${viewState.lastName}"

            if (viewState.notificationSound != R.raw.sms_ring) {
                notificationSound = viewState.notificationSound
                currentNotificationSound = viewState.notificationSound
            }

            isCustomSound = viewState.isCustomSound == 1
            currentIsCustomSound = viewState.isCustomSound == 1

            vipScheduleValue = viewState.vipSchedule
            currentVipScheduleValue = viewState.vipSchedule

            hourLimit = viewState.hourLimitForNotification
            currentHourLimit = viewState.hourLimitForNotification

            audioFile = viewState.audioFileName
            currentAudioFile = viewState.audioFileName

            notificationTone = viewState.notificationTone
            currentNotificationTone = viewState.notificationTone

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
                    onClickFirstCheckbox(moaninCheckbox, R.raw.moanin_jazz, jazzySoundBought)
                }
                blueBossaCheckbox.setOnClickListener {
                    onClickCheckbox(blueBossaCheckbox, R.raw.blue_bossa, jazzySoundBought)
                }
                caravanCheckbox.setOnClickListener {
                    onClickCheckbox(caravanCheckbox, R.raw.caravan, jazzySoundBought)
                }
                dolphinDanceCheckbox.setOnClickListener {
                    onClickCheckbox(
                        dolphinDanceCheckbox, R.raw.dolphin_dance, jazzySoundBought
                    )
                }
                autumnLeavesCheckbox.setOnClickListener {
                    onClickCheckbox(
                        autumnLeavesCheckbox, R.raw.autumn_leaves, jazzySoundBought
                    )
                }
                freddieFreeloaderCheckbox.setOnClickListener {
                    onClickCheckbox(
                        freddieFreeloaderCheckbox, R.raw.freddie_freeloader, jazzySoundBought
                    )
                }

                //endregion

                //region =================================== Funky Checkboxes ===================================

                slapCheckbox.setOnClickListener {
                    onClickFirstCheckbox(slapCheckbox, R.raw.bass_slap, funkySoundBought)
                }
                offTheCurveCheckbox.setOnClickListener {
                    onClickCheckbox(
                        offTheCurveCheckbox, R.raw.off_the_curve_groove, funkySoundBought
                    )
                }
                funkYallCheckbox.setOnClickListener {
                    onClickCheckbox(funkYallCheckbox, R.raw.funk_yall, funkySoundBought)
                }
                keyboardFunkyToneCheckbox.setOnClickListener {
                    onClickCheckbox(
                        keyboardFunkyToneCheckbox, R.raw.keyboard_funky_tone, funkySoundBought
                    )
                }
                uCantHoldNoGrooveCheckbox.setOnClickListener {
                    onClickCheckbox(
                        uCantHoldNoGrooveCheckbox, R.raw.u_cant_hold_no_groove, funkySoundBought
                    )
                }
                coldSweatCheckbox.setOnClickListener {
                    onClickCheckbox(coldSweatCheckbox, R.raw.cold_sweat, funkySoundBought)
                }

                //endregion

                //region =================================== Relax Checkboxes ===================================

                xyloRelaxCheckbox.setOnClickListener {
                    onClickFirstCheckbox(
                        xyloRelaxCheckbox, R.raw.xylophone_tone, relaxSoundBought
                    )
                }
                guitarRelaxCheckbox.setOnClickListener {
                    onClickCheckbox(
                        guitarRelaxCheckbox, R.raw.beautiful_chords_progression, relaxSoundBought
                    )
                }
                gravityCheckbox.setOnClickListener {
                    onClickCheckbox(gravityCheckbox, R.raw.gravity, relaxSoundBought)
                }
                slowDancingCheckbox.setOnClickListener {
                    onClickCheckbox(
                        slowDancingCheckbox, R.raw.slow_dancing, relaxSoundBought
                    )
                }
                scorpionThemeCheckbox.setOnClickListener {
                    onClickCheckbox(
                        scorpionThemeCheckbox, R.raw.fade_to_black, relaxSoundBought
                    )
                }
                interstellarThemeCheckbox.setOnClickListener {
                    onClickCheckbox(
                        interstellarThemeCheckbox, R.raw.interstellar_main_theme, relaxSoundBought
                    )
                }

                //endregion

                uploadButton.setOnClickListener {
                    alarmSound?.stop()
                    checkRuntimePermission()
                }

                uploadCheckbox.setOnClickListener {
                    uncheckBoxAll()
                    uploadCheckbox.isChecked = true
                    isCustomSound = true

                    CoroutineScope(Dispatchers.Main).launch {
                        alarmSound?.stop()
                        alarmSound = MediaPlayer.create(this@VipSettingsActivity, Uri.parse(notificationTone))
                        alarmSound?.start()
                        delay(15000)
                        alarmSound?.stop()
                    }
                }

                permanentRadioButton.setOnClickListener {
                    vipScheduleValue = 1
                    permanentRadioButton.isChecked = true
                    daytimeRadioButton.isChecked = false
                    workweekRadioButton.isChecked = false
                    scheduleCustomRadioButton.isChecked = false

                    startTime.isVisible = false
                    endTime.isVisible = false
                    startTimeEditText.isVisible = false
                    endTimeEditText.isVisible = false
                }

                daytimeRadioButton.setOnClickListener {
                    vipScheduleValue = 2
                    permanentRadioButton.isChecked = false
                    daytimeRadioButton.isChecked = true
                    workweekRadioButton.isChecked = false
                    scheduleCustomRadioButton.isChecked = false

                    startTime.isVisible = true
                    endTime.isVisible = true
                    startTimeEditText.isVisible = true
                    endTimeEditText.isVisible = true
                }

                workweekRadioButton.setOnClickListener {
                    vipScheduleValue = 3
                    permanentRadioButton.isChecked = false
                    daytimeRadioButton.isChecked = false
                    workweekRadioButton.isChecked = true
                    scheduleCustomRadioButton.isChecked = false

                    startTime.isVisible = false
                    endTime.isVisible = false
                    startTimeEditText.isVisible = false
                    endTimeEditText.isVisible = false
                }

                scheduleCustomRadioButton.setOnClickListener {
                    vipScheduleValue = 4
                    permanentRadioButton.isChecked = false
                    daytimeRadioButton.isChecked = false
                    workweekRadioButton.isChecked = false
                    scheduleCustomRadioButton.isChecked = true

                    startTime.isVisible = true
                    endTime.isVisible = true
                    startTimeEditText.isVisible = true
                    endTimeEditText.isVisible = true
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
                        scheduleCustomRadioButton.isChecked = true
                    }
                    else -> {
                        permanentRadioButton.isChecked = true
                    }
                }

                startTime.isVisible = scheduleCustomRadioButton.isChecked || daytimeRadioButton.isChecked
                endTime.isVisible = scheduleCustomRadioButton.isChecked || daytimeRadioButton.isChecked
                startTimeEditText.isVisible = scheduleCustomRadioButton.isChecked || daytimeRadioButton.isChecked
                endTimeEditText.isVisible = scheduleCustomRadioButton.isChecked || daytimeRadioButton.isChecked

                if (hourLimit.contains("to")) {
                    binding.startTimeEditText.setText(convertTimeToStartTime(hourLimit))
                    binding.endTimeEditText.setText(convertTimeToEndTime(hourLimit))
                }
            }

            //endregion

            editTextTimePicker()

            if (notificationTone.isNotBlank() && notificationTone.isNotEmpty() && notificationTone != "") {
                binding.apply {
                    uploadSoundPath.isVisible = true
                    uploadSoundPath.text = audioFile
                    uploadCheckbox.isVisible = true
                }
            }
        }

        userIdPreferences = getSharedPreferences("User_Id", Context.MODE_PRIVATE)

        saveUserIdToFirebase(userIdPreferences, firebaseViewModel, "Enter the VipSettingsActivity")
    }

    //region ========================================== Functions ===========================================

    private fun checkIfDataHasChanged(): Boolean {
        return notificationSound != currentNotificationSound || isCustomSound != currentIsCustomSound || vipScheduleValue != currentVipScheduleValue || hourLimit != currentHourLimit || audioFile != currentAudioFile || notificationTone != currentNotificationTone
    }

    //region ============================================= Date =============================================

    private fun editTextTimePicker() {
        binding.startTimeEditText.setOnClickListener { view ->
            val timePickerDialog = TimePickerDialog(
                this, { _: TimePicker?, hourOfDay: Int, minutes: Int ->
                    if (minutes < 10) {
                        binding.startTimeEditText.setText(
                            hourOfDay.toString() + "h0" + minutes
                        )
                    } else {
                        binding.startTimeEditText.setText(
                            hourOfDay.toString() + "h" + minutes
                        )
                    }

                }, 0, 0, true
            )
            timePickerDialog.show()
        }
        binding.endTimeEditText.setOnClickListener { view ->
            val timePickerDialog = TimePickerDialog(
                this, { timePicker: TimePicker?, hourOfDay: Int, minutes: Int ->
                    if (minutes < 10) {
                        binding.endTimeEditText.setText(
                            hourOfDay.toString() + "h0" + minutes
                        )
                    } else {
                        binding.endTimeEditText.setText(
                            hourOfDay.toString() + "h" + minutes
                        )
                    }
                }, 0, 0, true
            )
            timePickerDialog.show()
        }
    }

    private fun convertStartAndEndTimeToOneString(startTime: String, endTime: String): String {
        return "$startTime to $endTime"
    }

    private fun convertTimeToStartTime(time: String): String {
        val parts = time.split(" to").toTypedArray()
        return parts[0]
    }

    private fun convertTimeToEndTime(time: String): String {
        val parts = time.split("to ").toTypedArray()
        return parts[1]
    }

    //endregion

    private fun checkIfUserBoughtCustomSound() {
        binding.apply {
            val isBought = jazzySoundBought || relaxSoundBought || funkySoundBought
            uploadButton.isVisible = isBought
            uploadSoundPath.isVisible = isBought
            uploadSongsLayout.isVisible = isBought
            uploadCustomSoundLayout.isVisible = isBought
        }
    }

    private fun onClickFirstCheckbox(checkBox: AppCompatCheckBox, sound: Int, isBought: Boolean) {
        saveUserIdToFirebase(userIdPreferences, firebaseViewModel, "Click on Checkbox to buy song")

        uncheckBoxAll()
        checkBox.isChecked = true
        notificationSound = sound
        playAlarmSound()

        if (!isBought) {
            notificationSound = currentNotificationSound
            alertDialogBuySound()
        } else {
            isCustomSound = false
        }
    }

    private fun onClickCheckbox(checkBox: AppCompatCheckBox, sound: Int, isBought: Boolean) {
        saveUserIdToFirebase(userIdPreferences, firebaseViewModel, "Click on Checkbox to buy song")
        uncheckBoxAll()
        checkBox.isChecked = true

        if (isBought) {
            notificationSound = sound
            playAlarmSound()
            isCustomSound = false
        } else {
            notificationSound = currentNotificationSound
            alertDialogBuySound()
        }
    }

    private fun backIconClick() {
        alarmSound?.stop()
        val intentBack = Intent(this, EditContactActivity::class.java)

        fillIntent(intentBack, currentViewState.id)
        intentBack.putExtra("fromVipSettings", true)
        startActivity(intentBack)
        finish()
    }

    private fun fillIntent(intentBack: Intent, id: Int) {
        intentBack.apply {
            putExtra("ContactId", id)

            if (intent.getBooleanExtra("hasChanged", false)) {
                putExtra("FirstName", intent.getStringExtra("FirstName"))
                putExtra("Lastname", intent.getStringExtra("Lastname"))
                putExtra("FirstPhoneNumber", intent.getStringExtra("FirstPhoneNumber"))
                putExtra("SecondPhoneNumber", intent.getStringExtra("SecondPhoneNumber"))

                Log.i("VipSettingsViewState", "mail : ${intent.getStringExtra("Mail")}")

                putExtra("Mail", intent.getStringExtra("Mail"))
                putExtra("MailId", intent.getStringExtra("MailId"))
                putExtra("MessengerId", intent.getStringExtra("MessengerId"))
                putExtra("Priority", intent.getIntExtra("Priority", 0))
                putExtra("isFavorite", intent.getIntExtra("isFavorite", 0))
                putExtra("hasChanged", intent.getBooleanExtra("hasChanged", false))
            }
            putExtra("notificationTone", notificationTone)
            putExtra("notification_Sound", notificationSound)

            Log.i("fromVipSettings", "notificationSound : $notificationSound")
            if (isCustomSound) {
                putExtra("isCustomSound", 1)
            } else {
                putExtra("isCustomSound", 0)
            }
            putExtra("vipScheduleValue", vipScheduleValue)
            putExtra("audioFileName", binding.uploadSoundPath.text.toString())
            putExtra("vipSettingsHasChanged", checkIfDataHasChanged())

            if (binding.startTimeEditText.text.isNullOrBlank() || binding.endTimeEditText.text.isNullOrBlank()) {
            } else {
                hourLimit = convertStartAndEndTimeToOneString(
                    binding.startTimeEditText.text.toString(), binding.endTimeEditText.text.toString()
                )
                putExtra("hourLimit", hourLimit)
            }
        }
    }

    private fun changeIconOpenLayout(img: AppCompatImageView, toClose: Boolean) {
        if (toClose) {
            img.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources, R.drawable.ic_bottom_arrow, null
                )
            )
        } else {
            img.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources, R.drawable.ic_top_arrow, null
                )
            )
        }
    }

    private fun alertDialogBuySound() {
        MaterialAlertDialogBuilder(this, R.style.AlertDialog).setTitle(getString(R.string.in_app_popup_tone_available_title))
            .setMessage(getString(R.string.in_app_popup_tone_available_message)).setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
                goToPremiumActivity()
            }.setNegativeButton(R.string.alert_dialog_later) { _, _ ->
                refreshChecked()
            }.show()
    }

    private fun playAlarmSound() {
        CoroutineScope(Dispatchers.Main).launch {
            alarmSound?.stop()
            alarmSound = if (isCustomSound) {
                MediaPlayer.create(this@VipSettingsActivity, Uri.parse(notificationTone))
            } else {
                MediaPlayer.create(this@VipSettingsActivity, notificationSound)
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
            if (isCustomSound) {
                binding.uploadCheckbox.isChecked = true
            } else {
                when (notificationSound) {
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
                    }
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
            val cursor = contentResolver.query(uri, null, selection, null, sortOrder)
            if (cursor != null && cursor.moveToFirst()) {
                val id = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
                val title = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)

                do {
                    val audioFileId = cursor.getLong(id)
                    if (audioFileId.toString() == audioId) {

                        for (i in 0..title) {
                            try {
                                if (cursor.getString(i)?.contains("storage") == true && cursor.getString(i)?.contains(".mp3") == true) {
                                    withContext(Dispatchers.Main) {
                                        binding.uploadSoundPath.isVisible = true
                                        binding.uploadSoundPath.text = cursor.getString(title)
                                        notificationTone = cursor.getString(i)
                                    }
                                    break
                                }
                            } catch (e: SQLException) {
                                Log.i("audioFile", "$i : ${cursor.getBlob(i)}")
                            }
                        }
                        break
                    }

                } while (cursor.moveToNext())
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            NotificationsSettingsActivity.PERMISSION_CODE -> {
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
            requestPermissions(permissions, NotificationsSettingsActivity.PERMISSION_CODE)
        } else {
            val edit = permissionsPref.edit()
            edit.putBoolean("PermissionsPreferences", true)
            edit.apply()
            getTones()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 89 && resultCode == RESULT_OK) {
            if (data?.data != null) {
                notificationTone = data.data.toString()

                binding.uploadSoundPath.isVisible = true
                binding.uploadSoundPath.text = data.data?.lastPathSegment?.split(":")?.get(1).toString()

                binding.uploadCheckbox.isVisible = true

                getAudioNameFromStorage(data.data?.lastPathSegment?.split(":")?.get(1).toString())
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
                this@VipSettingsActivity, PremiumActivity::class.java
            ).putExtra("fromManageNotification", true)
        )
        finish()
    }

    //endregion
}