package com.yellowtwigs.knockin.controller.activity

import android.Manifest
import android.R.attr
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Base64
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.controller.CircularImageView
import com.yellowtwigs.knockin.controller.GroupEditAdapter
import com.yellowtwigs.knockin.controller.activity.group.GroupManagerActivity
import com.yellowtwigs.knockin.model.*
import com.yellowtwigs.knockin.model.ModelDB.*
import java.io.ByteArrayOutputStream
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * La Classe qui permet d'éditer un contact choisi
 * @author Florian Striebel, Kenzy Suon, Ryan Granet
 */
class EditContactVipSettingsActivity : AppCompatActivity() {
    companion object {
        const val PER = 110
    }

    //region ========================================== Var or Val ==========================================
    private lateinit var filePath: Uri

    private var gestionnaireContacts: ContactManager? = null
    private var edit_contact_ParentLayout: ConstraintLayout? = null
    private var edit_contact_RoundedImageView: CircularImageView? = null
    private var edit_contact_Name: TextView? = null

    private var edit_contact_Return: AppCompatImageView? = null
    private var edit_contact_DeleteContact: AppCompatImageView? = null
    private var edit_contact_AddContactToFavorite: AppCompatImageView? = null
    private var edit_contact_RemoveContactFromFavorite: AppCompatImageView? = null
    private var edit_contact_Validate: AppCompatImageView? = null

    private var groupId: Long = 0
    private var listContact: ArrayList<ContactDB?> = ArrayList()

    private var edit_contact_id: Int? = null
    private var edit_contact_first_name: String = ""
    private var edit_contact_last_name: String = ""
    private var edit_contact_phone_number: String = ""
    private var edit_contact_phone_property: String = ""
    private var edit_contact_fix_number: String = ""
    private var edit_contact_fix_property: String = ""
    private var edit_contact_mail_property: String = ""
    private var edit_contact_mail: String = ""
    private var edit_contact_mail_name: String = ""
    private var edit_contact_rounded_image: Int = 0
    private var edit_contact_image64: String = ""
    private var edit_contact_priority: Int = 1

    private var notification_tone: String = ""

    private var edit_contact_GroupConstraintLayout: ConstraintLayout? = null

    // Database && Thread
    private var edit_contact_ContactsDatabase: ContactsRoomDatabase? = null
    private lateinit var edit_contact_mDbWorkerThread: DbWorkerThread

    private var edit_contact_imgString: String? = null

    private var edit_contact_imgStringChanged = false

    private var havePhone: Boolean = false
    private var haveSecondPhone: Boolean = false
    private var haveMail: Boolean = false

    private var recyclerGroup: RecyclerView? = null

    private var fromGroupActivity = false

    private var isChanged = false
    private var editInAndroid = false
    private var editInGoogle = false

    private var isFavorite: Boolean? = null
    private var isFavoriteChanged: Boolean? = null
    private var contactsUnlimitedIsBought: Boolean? = null

    private var position: Int? = null

    //endregion

    //region personal tones

    private var settings_ChooseNotifPersonalSoundLayoutOpenClose: RelativeLayout? = null
    private var settings_ChooseNotifPersonalSoundImageOpen: AppCompatImageView? = null
    private var settings_ChooseNotifPersonalSoundImageClose: AppCompatImageView? = null

    private var settings_NotifPersonalSoundLayout: RelativeLayout? = null

    //endregion2

    //region Jazzy Sound

    private var settings_ChooseNotifJazzySoundLayoutOpenClose: RelativeLayout? = null
    private var settings_ChooseNotifJazzySoundImageOpen: AppCompatImageView? = null
    private var settings_ChooseNotifJazzySoundImageClose: AppCompatImageView? = null

    private var settings_NotifSoundMoaninLayout: RelativeLayout? = null
    private var settings_NotifSoundMoaninCheckbox: CheckBox? = null

    private var settings_NotifSoundBlueBossaLayout: RelativeLayout? = null
    private var settings_NotifSoundBlueBossaCheckbox: CheckBox? = null

    private var settings_NotifSoundAutumnLeavesLayout: RelativeLayout? = null
    private var settings_NotifSoundAutumnLeavesCheckbox: CheckBox? = null

    private var settings_NotifSoundDolphinDanceLayout: RelativeLayout? = null
    private var settings_NotifSoundDolphinDanceCheckbox: CheckBox? = null

    private var settings_NotifSoundFreddieFreeloaderLayout: RelativeLayout? = null
    private var settings_NotifSoundFreddieFreeloaderCheckbox: CheckBox? = null

    private var settings_NotifSoundCaravanLayout: RelativeLayout? = null
    private var settings_NotifSoundCaravanCheckbox: CheckBox? = null

    private var settings_NotifsoundJazzyUploadSoundLayout: RelativeLayout? = null
    lateinit var settings_NotifsoundJazzyUploadSoundPath: TextView


    //endregion

    //region Funky Sound

    private var settings_ChooseNotifFunkySoundLayoutOpenClose: RelativeLayout? = null
    private var settings_ChooseNotifFunkySoundImageOpen: AppCompatImageView? = null
    private var settings_ChooseNotifFunkySoundImageClose: AppCompatImageView? = null

    private var settings_NotifSoundSlapLayout: RelativeLayout? = null
    private var settings_NotifSoundSlapCheckbox: CheckBox? = null

    private var settings_NotifSoundOffTheCurveLayout: RelativeLayout? = null
    private var settings_NotifSoundOffTheCurveCheckbox: CheckBox? = null

    private var settings_NotifSoundKeyboardFunkyToneLayout: RelativeLayout? = null
    private var settings_NotifSoundKeyboardFunkyToneCheckbox: CheckBox? = null

    private var settings_NotifSoundUCantHoldNoGrooveLayout: RelativeLayout? = null
    private var settings_NotifSoundUCantHoldNoGrooveCheckbox: CheckBox? = null

    private var settings_NotifSoundColdSweatLayout: RelativeLayout? = null
    private var settings_NotifSoundColdSweatCheckbox: CheckBox? = null

    private var settings_NotifSoundFunkYallLayout: RelativeLayout? = null
    private var settings_NotifSoundFunkYallCheckbox: CheckBox? = null

    private var settings_NotifsoundFunkyUploadSoundLayout: RelativeLayout? = null
    lateinit var settings_NotifsoundFunkyUploadSoundPath: TextView
    //endregion

    //region Relaxation Sound

    private var settings_ChooseNotifRelaxationSoundLayoutOpenClose: RelativeLayout? = null
    private var settings_ChooseNotifRelaxationSoundImageOpen: AppCompatImageView? = null
    private var settings_ChooseNotifRelaxationSoundImageClose: AppCompatImageView? = null

    private var settings_NotifSoundAcousticGuitarLayout: RelativeLayout? = null
    private var settings_NotifSoundAcousticGuitarCheckbox: CheckBox? = null

    private var settings_NotifSoundRelaxToneLayout: RelativeLayout? = null
    private var settings_NotifSoundRelaxToneCheckbox: CheckBox? = null

    private var settings_NotifSoundGravityLayout: RelativeLayout? = null
    private var settings_NotifSoundGravityCheckbox: CheckBox? = null

    private var settings_NotifSoundSlowDancingLayout: RelativeLayout? = null
    private var settings_NotifSoundSlowDancingCheckbox: CheckBox? = null

    private var settings_NotifSoundScorpionThemeLayout: RelativeLayout? = null
    private var settings_NotifSoundScorpionThemeCheckbox: CheckBox? = null

    private var settings_NotifSoundFirstStepLayout: RelativeLayout? = null
    private var settings_NotifSoundFirstStepCheckbox: CheckBox? = null

    private var settings_NotifsoundRelaxUploadSoundLayout: RelativeLayout? = null
    lateinit var settings_NotifsoundRelaxUploadSoundPath: TextView

    //endregion
    private var settings_NotificationMessagesAlarmSound: MediaPlayer? = null
    private var settings_ChooseNotifSoundTitle: TextView? = null
    private var settings_ChooseNotifSoundLayout: ConstraintLayout? = null

    private var notifFunkySoundIsBought: Boolean = false
    private var notifJazzySoundIsBought: Boolean = true
    private var notifRelaxationSoundIsBought: Boolean = false
    private var notifCustomSoundIsBought: Boolean = false

    private var numberDefault: Int = 0

    //endregion
    val CONTACT_CHOOSER_ACTIVITY_CODE: Int = 73729

    @SuppressLint("InflateParams", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (notifJazzySoundIsBought == false && notifFunkySoundIsBought == false && notifRelaxationSoundIsBought == false) {
            MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                .setTitle(getString(R.string.in_app_popup_personalization_available_title))
                .setMessage(getString(R.string.in_app_popup_tone_available_message))
                .setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
                    goToPremiumActivity()
                }
                .setNegativeButton(R.string.alert_dialog_later) { _, _ ->
                    refreshChecked()
                }
                .show()
        } else {

        }
        //region ======================================== Theme Dark ========================================

        val sharedThemePreferences = getSharedPreferences("Knockin_Theme", Context.MODE_PRIVATE)
        if (sharedThemePreferences.getBoolean("darkTheme", false)) {
            setTheme(R.style.AppThemeDark)
        } else {
            setTheme(R.style.AppTheme)
        }

        //endregion
        setContentView(R.layout.activity_edit_contact_vip_settings)

        //get the list of  Ringtones
        settings_NotifsoundJazzyUploadSoundPath =
            findViewById(R.id.settings_notif_jazzy_upload_sound_path)
        settings_NotifsoundFunkyUploadSoundPath =
            findViewById(R.id.settings_notif_funky_upload_sound_path)
        settings_NotifsoundRelaxUploadSoundPath =
            findViewById(R.id.settings_notif_relax_upload_sound_path)


        var settings_NotifsoundJazzyUploadSoundButton =
            findViewById(R.id.settings_notif_jazzy_upload_sound_button) as Button
        settings_NotifsoundJazzyUploadSoundButton.setOnClickListener {
            checkRuntimePermission()

            //settings_NotifsoundFunkyUploadSoundPath?.setText("")
            settings_NotifSoundMoaninCheckbox!!.isChecked = false
            settings_NotifSoundBlueBossaCheckbox!!.isChecked = false
            settings_NotifSoundCaravanCheckbox!!.isChecked = false
            settings_NotifSoundDolphinDanceCheckbox!!.isChecked = false
            settings_NotifSoundAutumnLeavesCheckbox!!.isChecked = false
            settings_NotifSoundFreddieFreeloaderCheckbox!!.isChecked = false

            settings_NotifSoundSlapCheckbox!!.isChecked = false
            settings_NotifSoundOffTheCurveCheckbox!!.isChecked = false
            settings_NotifSoundFunkYallCheckbox!!.isChecked = false
            settings_NotifSoundKeyboardFunkyToneCheckbox!!.isChecked = false
            settings_NotifSoundUCantHoldNoGrooveCheckbox!!.isChecked = false
            settings_NotifSoundColdSweatCheckbox!!.isChecked = false

            settings_NotifSoundAcousticGuitarCheckbox!!.isChecked = false
            settings_NotifSoundGravityCheckbox!!.isChecked = false
            settings_NotifSoundSlowDancingCheckbox!!.isChecked = false
            settings_NotifSoundScorpionThemeCheckbox!!.isChecked = false
            settings_NotifSoundFirstStepCheckbox!!.isChecked = false
            settings_NotifSoundRelaxToneCheckbox!!.isChecked = false

        }
        var settings_NotifsoundFunkyUploadSoundButton =
            findViewById(R.id.settings_notif_funky_upload_sound_button) as Button
        settings_NotifsoundFunkyUploadSoundButton.setOnClickListener {
            checkRuntimePermission()
            settings_NotifSoundMoaninCheckbox!!.isChecked = false
            settings_NotifSoundBlueBossaCheckbox!!.isChecked = false
            settings_NotifSoundCaravanCheckbox!!.isChecked = false
            settings_NotifSoundDolphinDanceCheckbox!!.isChecked = false
            settings_NotifSoundAutumnLeavesCheckbox!!.isChecked = false
            settings_NotifSoundFreddieFreeloaderCheckbox!!.isChecked = false

            settings_NotifSoundSlapCheckbox!!.isChecked = false
            settings_NotifSoundOffTheCurveCheckbox!!.isChecked = false
            settings_NotifSoundFunkYallCheckbox!!.isChecked = false
            settings_NotifSoundKeyboardFunkyToneCheckbox!!.isChecked = false
            settings_NotifSoundUCantHoldNoGrooveCheckbox!!.isChecked = false
            settings_NotifSoundColdSweatCheckbox!!.isChecked = false

            settings_NotifSoundAcousticGuitarCheckbox!!.isChecked = false
            settings_NotifSoundGravityCheckbox!!.isChecked = false
            settings_NotifSoundSlowDancingCheckbox!!.isChecked = false
            settings_NotifSoundScorpionThemeCheckbox!!.isChecked = false
            settings_NotifSoundFirstStepCheckbox!!.isChecked = false
            settings_NotifSoundRelaxToneCheckbox!!.isChecked = false

        }
        var settings_NotifsoundRelaxUploadSoundButton =
            findViewById(R.id.settings_notif_relax_upload_sound_button) as Button
        settings_NotifsoundRelaxUploadSoundButton.setOnClickListener {
            checkRuntimePermission()

            settings_NotifSoundMoaninCheckbox!!.isChecked = false
            settings_NotifSoundBlueBossaCheckbox!!.isChecked = false
            settings_NotifSoundCaravanCheckbox!!.isChecked = false
            settings_NotifSoundDolphinDanceCheckbox!!.isChecked = false
            settings_NotifSoundAutumnLeavesCheckbox!!.isChecked = false
            settings_NotifSoundFreddieFreeloaderCheckbox!!.isChecked = false

            settings_NotifSoundSlapCheckbox!!.isChecked = false
            settings_NotifSoundOffTheCurveCheckbox!!.isChecked = false
            settings_NotifSoundFunkYallCheckbox!!.isChecked = false
            settings_NotifSoundKeyboardFunkyToneCheckbox!!.isChecked = false
            settings_NotifSoundUCantHoldNoGrooveCheckbox!!.isChecked = false
            settings_NotifSoundColdSweatCheckbox!!.isChecked = false

            settings_NotifSoundAcousticGuitarCheckbox!!.isChecked = false
            settings_NotifSoundGravityCheckbox!!.isChecked = false
            settings_NotifSoundSlowDancingCheckbox!!.isChecked = false
            settings_NotifSoundScorpionThemeCheckbox!!.isChecked = false
            settings_NotifSoundFirstStepCheckbox!!.isChecked = false
            settings_NotifSoundRelaxToneCheckbox!!.isChecked = false
        }

        //end

        val sharedPreferences: SharedPreferences =
            getSharedPreferences("Knockin_preferences", Context.MODE_PRIVATE)
        val sharedAlarmNotifTonePreferences: SharedPreferences =
            getSharedPreferences("Alarm_Notif_Tone", Context.MODE_PRIVATE)
        numberDefault = sharedAlarmNotifTonePreferences.getInt("Alarm_Notif_Tone", R.raw.sms_ring)

        val sharedNotifJazzySoundInAppPreferences: SharedPreferences =
            getSharedPreferences("Jazzy_Sound_Bought", Context.MODE_PRIVATE)
        notifJazzySoundIsBought =
            sharedNotifJazzySoundInAppPreferences.getBoolean("Jazzy_Sound_Bought", true)

        val sharedNotifRelaxationSoundInAppPreferences: SharedPreferences =
            getSharedPreferences("Relax_Sound_Bought", Context.MODE_PRIVATE)
        notifRelaxationSoundIsBought = sharedNotifRelaxationSoundInAppPreferences.getBoolean(
            "Relax_Sound_Bought",
            false
        )

        val sharedNotifFunkySoundInAppPreferences: SharedPreferences =
            getSharedPreferences("Funky_Sound_Bought", Context.MODE_PRIVATE)
        notifFunkySoundIsBought =
            sharedNotifFunkySoundInAppPreferences.getBoolean("Funky_Sound_Bought", false)

        val sharedNotifCustomSoundInAppPreferences: SharedPreferences =
            getSharedPreferences("Custom_Sound_Bought", Context.MODE_PRIVATE)
        notifCustomSoundIsBought =
            sharedNotifCustomSoundInAppPreferences.getBoolean("Custom_Sound_Bought", false)

        // on init WorkerThread
        edit_contact_mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        edit_contact_mDbWorkerThread.start()

        //on get la base de données
        edit_contact_ContactsDatabase = ContactsRoomDatabase.getDatabase(this)

        val sharedNumberOfContactsVIPPreferences: SharedPreferences =
            getSharedPreferences("nb_Contacts_VIP", Context.MODE_PRIVATE)
        val nb_Contacts_VIP = sharedNumberOfContactsVIPPreferences.getInt("nb_Contacts_VIP", 0)

        val sharedAlarmNotifInAppPreferences: SharedPreferences =
            getSharedPreferences("Contacts_Unlimited_Bought", Context.MODE_PRIVATE)
        contactsUnlimitedIsBought =
            sharedAlarmNotifInAppPreferences.getBoolean("Contacts_Unlimited_Bought", false)

        //region ========================================== Intent ==========================================

        // Create the Intent, and get the data from the GridView

        val intent = intent
        edit_contact_id = intent.getIntExtra("ContactId", 1)
        position = intent.getIntExtra("position", 0)
        fromGroupActivity = intent.getBooleanExtra("fromGroupActivity", false)
        gestionnaireContacts = ContactManager(this.applicationContext)

        //endregion

        //region ======================================= FindViewById =======================================

        edit_contact_ParentLayout = findViewById(R.id.edit_contact_parent_layout)
        edit_contact_Name = findViewById(R.id.edit_contact_name_id)
        edit_contact_RoundedImageView = findViewById(R.id.edit_contact_rounded_image_view_id)
        recyclerGroup = findViewById(R.id.edit_contact_recycler)
        edit_contact_GroupConstraintLayout = findViewById(R.id.edit_contact_group_constraint_layout)
        edit_contact_Return = findViewById(R.id.edit_contact_return) // 1531651456
        edit_contact_DeleteContact = findViewById(R.id.edit_contact_delete) // 1531651455574546
        edit_contact_AddContactToFavorite = findViewById(R.id.edit_contact_favorite)
        edit_contact_RemoveContactFromFavorite = findViewById(R.id.edit_contact_favorite_shine)
        edit_contact_Validate = findViewById(R.id.edit_contact_edit_contact)

        //region personal tones

        settings_ChooseNotifPersonalSoundLayoutOpenClose =
            findViewById(R.id.settings_choose_personally_tone)
        settings_ChooseNotifPersonalSoundImageOpen =
            findViewById(R.id.settings_choose_notif_personally_tone_sound_image_open)
        settings_ChooseNotifPersonalSoundImageClose =
            findViewById(R.id.settings_choose_notif_personally_tone_sound_image_close)
        settings_NotifPersonalSoundLayout = findViewById(R.id.settings_notif_upload_tone_layout)

        //endregion

        //region Jazzy Sound

        settings_ChooseNotifJazzySoundLayoutOpenClose =
            findViewById(R.id.settings_choose_jazzy_sound_layout)
        settings_ChooseNotifJazzySoundImageOpen =
            findViewById(R.id.settings_choose_notif_jazzy_sound_image_open)
        settings_ChooseNotifJazzySoundImageClose =
            findViewById(R.id.settings_choose_notif_jazzy_sound_image_close)

        settings_NotifSoundMoaninLayout = findViewById(R.id.settings_notif_sound_moanin_layout)
        settings_NotifSoundMoaninCheckbox = findViewById(R.id.settings_notif_sound_moanin_checkbox)

        settings_NotifSoundBlueBossaLayout =
            findViewById(R.id.settings_notif_sound_blue_bossa_layout)
        settings_NotifSoundBlueBossaCheckbox =
            findViewById(R.id.settings_notif_sound_blue_bossa_checkbox)

        settings_NotifSoundCaravanLayout = findViewById(R.id.settings_notif_sound_caravan_layout)
        settings_NotifSoundCaravanCheckbox =
            findViewById(R.id.settings_notif_sound_caravan_checkbox)

        settings_NotifSoundAutumnLeavesLayout =
            findViewById(R.id.settings_notif_sound_autumn_leaves_layout)
        settings_NotifSoundAutumnLeavesCheckbox =
            findViewById(R.id.settings_notif_sound_autumn_leaves_checkbox)

        settings_NotifSoundDolphinDanceLayout =
            findViewById(R.id.settings_notif_sound_dolphin_dance_layout)
        settings_NotifSoundDolphinDanceCheckbox =
            findViewById(R.id.settings_notif_sound_dolphin_dance_checkbox)

        settings_NotifSoundFreddieFreeloaderLayout =
            findViewById(R.id.settings_notif_sound_freddie_freeloader_layout)
        settings_NotifSoundFreddieFreeloaderCheckbox =
            findViewById(R.id.settings_notif_sound_freddie_freeloader_checkbox)

        settings_NotifsoundJazzyUploadSoundLayout =
            findViewById(R.id.settings_notif_jazzy_upload_sound_layout)


        //endregion

        //region Funky Sound

        settings_ChooseNotifFunkySoundLayoutOpenClose =
            findViewById(R.id.settings_choose_funky_sound_layout)
        settings_ChooseNotifFunkySoundImageOpen =
            findViewById(R.id.settings_choose_notif_funky_sound_image_open)
        settings_ChooseNotifFunkySoundImageClose =
            findViewById(R.id.settings_choose_notif_funky_sound_image_close)

        settings_NotifSoundSlapLayout = findViewById(R.id.settings_notif_sound_slap_layout)
        settings_NotifSoundSlapCheckbox = findViewById(R.id.settings_notif_sound_slap_checkbox)

        settings_NotifSoundOffTheCurveLayout =
            findViewById(R.id.settings_notif_sound_off_the_curve_layout)
        settings_NotifSoundOffTheCurveCheckbox =
            findViewById(R.id.settings_notif_sound_off_the_curve_checkbox)

        settings_NotifSoundFunkYallLayout = findViewById(R.id.settings_notif_sound_funk_yall_layout)
        settings_NotifSoundFunkYallCheckbox =
            findViewById(R.id.settings_notif_sound_funk_yall_checkbox)

        settings_NotifSoundKeyboardFunkyToneLayout =
            findViewById(R.id.settings_notif_sound_keyboard_funky_tone_layout)
        settings_NotifSoundKeyboardFunkyToneCheckbox =
            findViewById(R.id.settings_notif_sound_keyboard_funky_tone_checkbox)

        settings_NotifSoundUCantHoldNoGrooveLayout =
            findViewById(R.id.settings_notif_sound_u_cant_hold_no_groove_layout)
        settings_NotifSoundUCantHoldNoGrooveCheckbox =
            findViewById(R.id.settings_notif_sound_u_cant_hold_no_groove_checkbox)

        settings_NotifSoundColdSweatLayout =
            findViewById(R.id.settings_notif_sound_cold_sweat_layout)
        settings_NotifSoundColdSweatCheckbox =
            findViewById(R.id.settings_notif_sound_cold_sweat_checkbox)

        settings_NotifsoundFunkyUploadSoundLayout =
            findViewById(R.id.settings_notif_funky_upload_sound_layout)

        //endregion

        //region Relaxation Sound

        settings_ChooseNotifRelaxationSoundLayoutOpenClose =
            findViewById(R.id.settings_choose_relaxation_sound_layout)
        settings_ChooseNotifRelaxationSoundImageOpen =
            findViewById(R.id.settings_choose_notif_relaxation_sound_image_open)
        settings_ChooseNotifRelaxationSoundImageClose =
            findViewById(R.id.settings_choose_notif_relaxation_sound_image_close)

        settings_NotifSoundAcousticGuitarLayout =
            findViewById(R.id.settings_notif_sound_guitar_relax_layout)
        settings_NotifSoundAcousticGuitarCheckbox =
            findViewById(R.id.settings_notif_sound_guitar_relax_checkbox)

        settings_NotifSoundRelaxToneLayout =
            findViewById(R.id.settings_notif_sound_xylo_relax_layout)
        settings_NotifSoundRelaxToneCheckbox =
            findViewById(R.id.settings_notif_sound_xylo_relax_checkbox)

        settings_NotifSoundGravityLayout = findViewById(R.id.settings_notif_sound_gravity_layout)
        settings_NotifSoundGravityCheckbox =
            findViewById(R.id.settings_notif_sound_gravity_checkbox)

        settings_NotifSoundSlowDancingLayout =
            findViewById(R.id.settings_notif_sound_slow_dancing_layout)
        settings_NotifSoundSlowDancingCheckbox =
            findViewById(R.id.settings_notif_sound_slow_dancing_checkbox)

        settings_NotifSoundScorpionThemeLayout =
            findViewById(R.id.settings_notif_sound_scorpion_theme_layout)
        settings_NotifSoundScorpionThemeCheckbox =
            findViewById(R.id.settings_notif_sound_scorpion_theme_checkbox)

        settings_NotifSoundFirstStepLayout =
            findViewById(R.id.settings_notif_sound_interstellar_theme_layout)
        settings_NotifSoundFirstStepCheckbox =
            findViewById(R.id.settings_notif_sound_interstellar_theme_checkbox)

        settings_NotifsoundRelaxUploadSoundLayout =
            findViewById(R.id.settings_notif_relax_upload_sound_layout)


        //endregion
        settings_ChooseNotifSoundTitle = findViewById(R.id.settings_choose_notif_sound_title)
        settings_ChooseNotifSoundLayout = findViewById(R.id.settings_choose_notif_sound_layout)

        //endregion

        //disable keyboard

        edit_contact_ParentLayout!!.setOnTouchListener { _, _ ->
            val view = this@EditContactVipSettingsActivity.currentFocus
            val imm = this@EditContactVipSettingsActivity.getSystemService(
                Context.INPUT_METHOD_SERVICE
            ) as InputMethodManager
            if (view != null) {
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
            true
        }

        //edit_contact_AddFieldButton = findViewById(R.id.edit_contact_add_field_button)

        //TODO wash the code
        if (edit_contact_ContactsDatabase?.contactsDao()
                ?.getContact(edit_contact_id!!.toInt()) == null
        ) {

            val contactList = ContactManager(this)
            val contact = contactList.getContactById(edit_contact_id!!)!!
            edit_contact_first_name = contact.contactDB!!.firstName
            edit_contact_last_name = contact.contactDB!!.lastName
            edit_contact_priority = contact.contactDB!!.contactPriority
            val tmpPhone = contact.contactDetailList!![0]
            edit_contact_phone_number = tmpPhone.content
            edit_contact_phone_property = tmpPhone.tag
            val tmpMail = contact.contactDetailList!![1]
            edit_contact_mail = tmpMail.content
            edit_contact_mail_property = tmpMail.tag
//            edit_contact_messenger = contact.contactDB!!.messengerId
            edit_contact_mail_name = contact.contactDB!!.mail_name
            edit_contact_image64 = contact.contactDB!!.profilePicture64
            edit_contact_RoundedImageView!!.setImageBitmap(base64ToBitmap(edit_contact_image64))
        } else {

            val executorService: ExecutorService = Executors.newFixedThreadPool(1)
            val callDb = Callable {
                edit_contact_ContactsDatabase!!.contactsDao().getContact(edit_contact_id!!)
            }
            val result = executorService.submit(callDb)
            val contact: ContactWithAllInformation = result.get()
            notification_tone = contact.contactDB!!.notificationTone
            edit_contact_first_name = contact.contactDB!!.firstName
            edit_contact_last_name = contact.contactDB!!.lastName
            edit_contact_priority = contact.contactDB!!.contactPriority
            edit_contact_rounded_image =
                gestionnaireContacts!!.randomDefaultImage(contact.contactDB!!.profilePicture, "Get")

            edit_contact_mail_name = contact.contactDB!!.mail_name
            //TODO :enlever code Dupliquer

            edit_contact_phone_property = getString(R.string.edit_contact_phone_number_mobile)
            edit_contact_fix_number = getString(R.string.edit_contact_phone_number_home)
            edit_contact_phone_number = ""
            edit_contact_mail = ""
            edit_contact_mail_property = getString(R.string.edit_contact_mail_mobile)

            val tagPhone = contact.getPhoneNumberTag()
            val phoneNumber = contact.getFirstPhoneNumber()
            edit_contact_phone_number = phoneNumber
            edit_contact_phone_property = tagPhone
            println("phone property of number $phoneNumber is $tagPhone")
            val tagFix = contact.getSecondPhoneTag(phoneNumber)
            val fixNumber = contact.getSecondPhoneNumber(phoneNumber)
            edit_contact_fix_number = fixNumber
            edit_contact_fix_property = tagFix
            val tagMail = contact.getMailTag()
            val mail = contact.getFirstMail()
            edit_contact_mail = mail
            edit_contact_mail_property = tagMail

            println("fix number egale à $fixNumber")
            if (phoneNumber != "") {
                havePhone = true
            }
            if (fixNumber != "") {
                haveSecondPhone = true
            }
            if (mail != "") { ///// havemail toujour false
                haveMail = true
            }

            val id = edit_contact_id
            val contactDB = edit_contact_ContactsDatabase?.contactsDao()?.getContact(id!!.toInt())
            edit_contact_image64 = contactDB!!.contactDB!!.profilePicture64
            if (edit_contact_image64 == "") {
                edit_contact_RoundedImageView!!.setImageResource(edit_contact_rounded_image)
            } else {
                val image64 = edit_contact_image64
                edit_contact_RoundedImageView!!.setImageBitmap(base64ToBitmap(image64))
            }
            when (edit_contact_priority) {
                0 -> {
                    edit_contact_RoundedImageView!!.setBorderColor(
                        ResourcesCompat.getColor(
                            resources,
                            R.color.priorityZeroColor,
                            null
                        )
                    )
                    edit_contact_RoundedImageView!!.setBetweenBorderColor(
                        ResourcesCompat.getColor(
                            resources,
                            R.color.lightColor,
                            null
                        )
                    )
                }
                1 -> {
                    edit_contact_RoundedImageView!!.setBorderColor(
                        resources.getColor(
                            R.color.priorityOneColor,
                            null
                        )
                    )
                    edit_contact_RoundedImageView!!.setBetweenBorderColor(
                        resources.getColor(
                            R.color.lightColor,
                            null
                        )
                    )
                }
                2 -> {
                    edit_contact_RoundedImageView!!.setBorderColor(
                        resources.getColor(
                            R.color.priorityTwoColor,
                            null
                        )
                    )
                    edit_contact_RoundedImageView!!.setBetweenBorderColor(
                        resources.getColor(
                            R.color.lightColor,
                            null
                        )
                    )
                }
            }

        }

        //region ===================================== SetViewDataField =====================================

        edit_contact_Name!!.setText(edit_contact_first_name + " " + edit_contact_last_name)

        //endregion

        //region ======================================== Favorites =========================================

        val contactList = ContactManager(this)
        val contact = contactList.getContactById(edit_contact_id!!)!!
/*
        if (contact.contactDB!!.favorite == 1) {
            edit_contact_RemoveContactFromFavorite!!.visibility = View.VISIBLE
            edit_contact_AddContactToFavorite!!.visibility = View.INVISIBLE

            isFavorite = true
            isFavoriteChanged = true

        } else if (contact.contactDB!!.favorite == 0) {
            edit_contact_AddContactToFavorite!!.visibility = View.VISIBLE
            edit_contact_RemoveContactFromFavorite!!.visibility = View.INVISIBLE

            isFavorite = false
            isFavoriteChanged = false
        }
*/
        //endregion

        //region ========================================== Tags ============================================

        val phoneTagList = resources.getStringArray(R.array.edit_contact_phone_number_arrays)
        val adapterPhoneTagList = ArrayAdapter(this, R.layout.spinner_item, phoneTagList)
        //array_adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)

        val mailTagList = resources.getStringArray(R.array.edit_contact_mail_arrays)
        val adapterMailTagList = ArrayAdapter(this, R.layout.spinner_item, mailTagList)
        //array_adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)


        //endregion

        //region ======================================== Priority ==========================================

        val priority_list =
            arrayOf(getString(R.string.add_new_contact_priority_0), "Standard", "VIP")
        val priority_adapter = ArrayAdapter(this, R.layout.spinner_item, priority_list)


        //endregion

        //region ========================================== Groups ==========================================


        //edit_contact_ContactsDatabase.contactsDao()
        val executorService: ExecutorService = Executors.newFixedThreadPool(1)
        val callDbGroup = Callable {
            edit_contact_ContactsDatabase!!.GroupsDao().getGroupForContact(edit_contact_id!!)
        }
        val resultGroup = executorService.submit(callDbGroup)
        val listGroup: ArrayList<GroupDB> = ArrayList()
        listGroup.addAll(resultGroup.get())

        val callDBContact =
            Callable { edit_contact_ContactsDatabase!!.contactsDao().getContact(edit_contact_id!!) }
        val resultContact = executorService.submit(callDBContact)
        val adapter = GroupEditAdapter(this, listGroup, resultContact.get())

        if (listGroup.size > 0) {
            edit_contact_GroupConstraintLayout!!.visibility = View.VISIBLE
        }

        //endregion

        //region ======================================== Listeners =========================================

        edit_contact_Return!!.setOnClickListener {
            //  onBackPressed()
            val intent = Intent(this, EditContactActivity::class.java)
            intent.putExtra("ContactId", contact.getContactId())
            startActivity(intent)
        }
        edit_contact_DeleteContact!!.setOnClickListener {

            MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                .setTitle(getString(R.string.edit_contact_delete_contact))
                .setMessage(getString(R.string.edit_contact_delete_contact_message))
                .setPositiveButton(getString(R.string.edit_contact_validate)) { _, _ ->
                    edit_contact_ContactsDatabase!!.contactsDao()
                        .deleteContactById(edit_contact_id!!)
                    val mainIntent =
                        Intent(this@EditContactVipSettingsActivity, MainActivity::class.java)
                    mainIntent.putExtra("isDelete", true)

                    if (edit_contact_priority == 2) {
                        val edit: SharedPreferences.Editor =
                            sharedNumberOfContactsVIPPreferences.edit()
                        edit.putInt("nb_Contacts_VIP", nb_Contacts_VIP - 1)
                        edit.apply()
                    }

                    startActivity(mainIntent)
                    finish()
                }
                .setNegativeButton(getString(R.string.edit_contact_cancel)) { _, _ ->
                }
                .show()
        }
/*
        edit_contact_AddContactToFavorite!!.setOnClickListener {
            edit_contact_AddContactToFavorite!!.visibility = View.INVISIBLE
            edit_contact_RemoveContactFromFavorite!!.visibility = View.VISIBLE

            isFavorite = true
        }

        edit_contact_RemoveContactFromFavorite!!.setOnClickListener {
            edit_contact_RemoveContactFromFavorite!!.visibility = View.INVISIBLE
            edit_contact_AddContactToFavorite!!.visibility = View.VISIBLE

            isFavorite = false
        }
*/
        edit_contact_Validate!!.setOnClickListener {

        }


        //region ======================================== Ring Tones ========================================

        settings_NotifPersonalSoundLayout!!.visibility = View.GONE
        settings_ChooseNotifPersonalSoundImageOpen!!.visibility = View.GONE
        settings_ChooseNotifPersonalSoundImageClose!!.visibility = View.VISIBLE

        settings_ChooseNotifPersonalSoundLayoutOpenClose!!.setOnClickListener {
            if (settings_NotifPersonalSoundLayout!!.visibility == View.GONE) {

                settings_NotifPersonalSoundLayout!!.visibility = View.VISIBLE
                settings_ChooseNotifPersonalSoundImageOpen!!.visibility = View.VISIBLE
                settings_ChooseNotifPersonalSoundImageClose!!.visibility = View.GONE
            } else {
                settings_NotifPersonalSoundLayout!!.visibility = View.GONE

                settings_ChooseNotifPersonalSoundImageOpen!!.visibility = View.GONE
                settings_ChooseNotifPersonalSoundImageClose!!.visibility = View.VISIBLE
            }
        }

        settings_NotifSoundMoaninLayout!!.visibility = View.GONE
        settings_NotifSoundBlueBossaLayout!!.visibility = View.GONE
        settings_NotifSoundCaravanLayout!!.visibility = View.GONE
        settings_NotifSoundDolphinDanceLayout!!.visibility = View.GONE
        settings_NotifSoundAutumnLeavesLayout!!.visibility = View.GONE
        settings_NotifSoundFreddieFreeloaderLayout!!.visibility = View.GONE
        settings_NotifsoundJazzyUploadSoundLayout!!.visibility = View.GONE
        settings_ChooseNotifJazzySoundImageOpen!!.visibility = View.GONE
        settings_ChooseNotifJazzySoundImageClose!!.visibility = View.VISIBLE


        settings_ChooseNotifJazzySoundLayoutOpenClose!!.setOnClickListener {
            if (settings_NotifSoundMoaninLayout!!.visibility == View.GONE &&
                settings_NotifSoundBlueBossaLayout!!.visibility == View.GONE &&
                settings_NotifSoundCaravanLayout!!.visibility == View.GONE &&
                settings_NotifSoundDolphinDanceLayout!!.visibility == View.GONE &&
                settings_NotifSoundAutumnLeavesLayout!!.visibility == View.GONE &&
                settings_NotifSoundFreddieFreeloaderLayout!!.visibility == View.GONE &&
                settings_NotifsoundJazzyUploadSoundLayout!!.visibility == View.GONE
            ) {

                settings_NotifSoundMoaninLayout!!.visibility = View.VISIBLE
                settings_NotifSoundBlueBossaLayout!!.visibility = View.VISIBLE
                settings_NotifSoundCaravanLayout!!.visibility = View.VISIBLE
                settings_NotifSoundDolphinDanceLayout!!.visibility = View.VISIBLE
                settings_NotifSoundAutumnLeavesLayout!!.visibility = View.VISIBLE
                settings_NotifSoundFreddieFreeloaderLayout!!.visibility = View.VISIBLE
                settings_NotifsoundJazzyUploadSoundLayout!!.visibility = View.VISIBLE


                settings_ChooseNotifJazzySoundImageOpen!!.visibility = View.VISIBLE
                settings_ChooseNotifJazzySoundImageClose!!.visibility = View.GONE
            } else {
                settings_NotifSoundMoaninLayout!!.visibility = View.GONE
                settings_NotifSoundBlueBossaLayout!!.visibility = View.GONE
                settings_NotifSoundCaravanLayout!!.visibility = View.GONE
                settings_NotifSoundDolphinDanceLayout!!.visibility = View.GONE
                settings_NotifSoundAutumnLeavesLayout!!.visibility = View.GONE
                settings_NotifSoundFreddieFreeloaderLayout!!.visibility = View.GONE
                settings_NotifsoundJazzyUploadSoundLayout!!.visibility = View.GONE


                settings_ChooseNotifJazzySoundImageOpen!!.visibility = View.GONE
                settings_ChooseNotifJazzySoundImageClose!!.visibility = View.VISIBLE
            }
        }

        settings_NotifSoundSlapLayout!!.visibility = View.GONE
        settings_NotifSoundOffTheCurveLayout!!.visibility = View.GONE
        settings_NotifSoundFunkYallLayout!!.visibility = View.GONE
        settings_NotifSoundKeyboardFunkyToneLayout!!.visibility = View.GONE
        settings_NotifSoundUCantHoldNoGrooveLayout!!.visibility = View.GONE
        settings_NotifSoundColdSweatLayout!!.visibility = View.GONE
        settings_NotifsoundFunkyUploadSoundLayout!!.visibility = View.GONE


        settings_ChooseNotifFunkySoundImageOpen!!.visibility = View.GONE
        settings_ChooseNotifFunkySoundImageClose!!.visibility = View.VISIBLE

        settings_ChooseNotifFunkySoundLayoutOpenClose!!.setOnClickListener {
            if (settings_NotifSoundSlapLayout!!.visibility == View.GONE &&
                settings_NotifSoundOffTheCurveLayout!!.visibility == View.GONE &&
                settings_NotifSoundFunkYallLayout!!.visibility == View.GONE &&
                settings_NotifSoundKeyboardFunkyToneLayout!!.visibility == View.GONE &&
                settings_NotifSoundUCantHoldNoGrooveLayout!!.visibility == View.GONE &&
                settings_NotifSoundColdSweatLayout!!.visibility == View.GONE &&
                settings_NotifsoundFunkyUploadSoundLayout!!.visibility == View.GONE
            ) {

                settings_NotifSoundSlapLayout!!.visibility = View.VISIBLE
                settings_NotifSoundOffTheCurveLayout!!.visibility = View.VISIBLE
                settings_NotifSoundFunkYallLayout!!.visibility = View.VISIBLE
                settings_NotifSoundKeyboardFunkyToneLayout!!.visibility = View.VISIBLE
                settings_NotifSoundUCantHoldNoGrooveLayout!!.visibility = View.VISIBLE
                settings_NotifSoundColdSweatLayout!!.visibility = View.VISIBLE
                settings_NotifsoundFunkyUploadSoundLayout!!.visibility = View.VISIBLE


                settings_ChooseNotifFunkySoundImageOpen!!.visibility = View.VISIBLE
                settings_ChooseNotifFunkySoundImageClose!!.visibility = View.GONE
            } else {
                settings_NotifSoundSlapLayout!!.visibility = View.GONE
                settings_NotifSoundOffTheCurveLayout!!.visibility = View.GONE
                settings_NotifSoundFunkYallLayout!!.visibility = View.GONE
                settings_NotifSoundKeyboardFunkyToneLayout!!.visibility = View.GONE
                settings_NotifSoundUCantHoldNoGrooveLayout!!.visibility = View.GONE
                settings_NotifSoundColdSweatLayout!!.visibility = View.GONE
                settings_NotifsoundFunkyUploadSoundLayout!!.visibility = View.GONE


                settings_ChooseNotifFunkySoundImageOpen!!.visibility = View.GONE
                settings_ChooseNotifFunkySoundImageClose!!.visibility = View.VISIBLE
            }
        }

        settings_NotifSoundAcousticGuitarLayout!!.visibility = View.GONE
        settings_NotifSoundGravityLayout!!.visibility = View.GONE
        settings_NotifSoundSlowDancingLayout!!.visibility = View.GONE
        settings_NotifSoundScorpionThemeLayout!!.visibility = View.GONE
        settings_NotifSoundFirstStepLayout!!.visibility = View.GONE
        settings_NotifSoundRelaxToneLayout!!.visibility = View.GONE
        settings_NotifsoundRelaxUploadSoundLayout!!.visibility = View.GONE

        settings_ChooseNotifRelaxationSoundImageOpen!!.visibility = View.GONE
        settings_ChooseNotifRelaxationSoundImageClose!!.visibility = View.VISIBLE

        settings_ChooseNotifRelaxationSoundLayoutOpenClose!!.setOnClickListener {
            if (settings_NotifSoundAcousticGuitarLayout!!.visibility == View.GONE &&
                settings_NotifSoundGravityLayout!!.visibility == View.GONE &&
                settings_NotifSoundSlowDancingLayout!!.visibility == View.GONE &&
                settings_NotifSoundScorpionThemeLayout!!.visibility == View.GONE &&
                settings_NotifSoundFirstStepLayout!!.visibility == View.GONE &&
                settings_NotifSoundRelaxToneLayout!!.visibility == View.GONE &&
                settings_NotifsoundRelaxUploadSoundLayout!!.visibility == View.GONE
            ) {

                settings_NotifSoundAcousticGuitarLayout!!.visibility = View.VISIBLE
                settings_NotifSoundGravityLayout!!.visibility = View.VISIBLE
                settings_NotifSoundSlowDancingLayout!!.visibility = View.VISIBLE
                settings_NotifSoundScorpionThemeLayout!!.visibility = View.VISIBLE
                settings_NotifSoundFirstStepLayout!!.visibility = View.VISIBLE
                settings_NotifSoundRelaxToneLayout!!.visibility = View.VISIBLE
                settings_NotifsoundRelaxUploadSoundLayout!!.visibility = View.VISIBLE

                settings_ChooseNotifRelaxationSoundImageOpen!!.visibility = View.VISIBLE
                settings_ChooseNotifRelaxationSoundImageClose!!.visibility = View.GONE
            } else {
                settings_NotifSoundAcousticGuitarLayout!!.visibility = View.GONE
                settings_NotifSoundGravityLayout!!.visibility = View.GONE
                settings_NotifSoundSlowDancingLayout!!.visibility = View.GONE
                settings_NotifSoundScorpionThemeLayout!!.visibility = View.GONE
                settings_NotifSoundFirstStepLayout!!.visibility = View.GONE
                settings_NotifSoundRelaxToneLayout!!.visibility = View.GONE
                settings_NotifsoundRelaxUploadSoundLayout!!.visibility = View.GONE

                settings_ChooseNotifRelaxationSoundImageOpen!!.visibility = View.GONE
                settings_ChooseNotifRelaxationSoundImageClose!!.visibility = View.VISIBLE
            }
        }


        //endregion

        refreshChecked()
        ringToneLayoutClosed()
        //region Jazzy

        settings_NotifSoundMoaninCheckbox!!.setOnClickListener {
            if (settings_NotificationMessagesAlarmSound != null) {
                settings_NotificationMessagesAlarmSound!!.stop()
            }

            if (settings_NotifSoundMoaninCheckbox!!.isChecked) {
                settings_NotificationMessagesAlarmSound =
                    MediaPlayer.create(this, R.raw.moanin_jazz)
                settings_NotificationMessagesAlarmSound!!.start()


                settings_NotifSoundBlueBossaCheckbox!!.isChecked = false
                settings_NotifSoundCaravanCheckbox!!.isChecked = false
                settings_NotifSoundDolphinDanceCheckbox!!.isChecked = false
                settings_NotifSoundAutumnLeavesCheckbox!!.isChecked = false
                settings_NotifSoundFreddieFreeloaderCheckbox!!.isChecked = false
                settings_NotifsoundJazzyUploadSoundPath?.text = ""

                settings_NotifsoundFunkyUploadSoundPath?.text = ""
                settings_NotifSoundSlapCheckbox!!.isChecked = false
                settings_NotifSoundOffTheCurveCheckbox!!.isChecked = false
                settings_NotifSoundFunkYallCheckbox!!.isChecked = false
                settings_NotifSoundKeyboardFunkyToneCheckbox!!.isChecked = false
                settings_NotifSoundUCantHoldNoGrooveCheckbox!!.isChecked = false
                settings_NotifSoundColdSweatCheckbox!!.isChecked = false

                settings_NotifsoundRelaxUploadSoundPath.text = ""
                settings_NotifSoundAcousticGuitarCheckbox!!.isChecked = false
                settings_NotifSoundGravityCheckbox!!.isChecked = false
                settings_NotifSoundSlowDancingCheckbox!!.isChecked = false
                settings_NotifSoundScorpionThemeCheckbox!!.isChecked = false
                settings_NotifSoundFirstStepCheckbox!!.isChecked = false
                settings_NotifSoundRelaxToneCheckbox!!.isChecked = false

                if (notifJazzySoundIsBought) {
                    val edit: SharedPreferences.Editor = sharedAlarmNotifTonePreferences.edit()
                    edit.putString("Alarm_Custom_Notif_Tone", null)
                    edit.apply()
                    edit.putInt("Alarm_Notif_Tone", R.raw.moanin_jazz)
                    edit.apply()
                } else {
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
            }
        }

        settings_NotifSoundBlueBossaCheckbox!!.setOnClickListener {
            if (settings_NotificationMessagesAlarmSound != null) {
                settings_NotificationMessagesAlarmSound!!.stop()
            }

            if (settings_NotifSoundBlueBossaCheckbox!!.isChecked) {

                settings_NotifSoundMoaninCheckbox!!.isChecked = false
//                settings_NotifSoundBlueBossaCheckbox!!.isChecked = false
                settings_NotifSoundCaravanCheckbox!!.isChecked = false
                settings_NotifSoundDolphinDanceCheckbox!!.isChecked = false
                settings_NotifSoundAutumnLeavesCheckbox!!.isChecked = false
                settings_NotifSoundFreddieFreeloaderCheckbox!!.isChecked = false
                settings_NotifsoundJazzyUploadSoundPath?.setText("")

                settings_NotifsoundFunkyUploadSoundPath?.setText("")
                settings_NotifSoundSlapCheckbox!!.isChecked = false
                settings_NotifSoundOffTheCurveCheckbox!!.isChecked = false
                settings_NotifSoundFunkYallCheckbox!!.isChecked = false
                settings_NotifSoundKeyboardFunkyToneCheckbox!!.isChecked = false
                settings_NotifSoundUCantHoldNoGrooveCheckbox!!.isChecked = false
                settings_NotifSoundColdSweatCheckbox!!.isChecked = false

                settings_NotifsoundRelaxUploadSoundPath?.setText("")
                settings_NotifSoundAcousticGuitarCheckbox!!.isChecked = false
                settings_NotifSoundGravityCheckbox!!.isChecked = false
                settings_NotifSoundSlowDancingCheckbox!!.isChecked = false
                settings_NotifSoundScorpionThemeCheckbox!!.isChecked = false
                settings_NotifSoundFirstStepCheckbox!!.isChecked = false
                settings_NotifSoundRelaxToneCheckbox!!.isChecked = false

                if (notifJazzySoundIsBought) {

                    settings_NotificationMessagesAlarmSound =
                        MediaPlayer.create(this, R.raw.blue_bossa)
                    settings_NotificationMessagesAlarmSound!!.start()

                    val edit: SharedPreferences.Editor = sharedAlarmNotifTonePreferences.edit()
                    edit.putString("Alarm_Custom_Notif_Tone", null)
                    edit.apply()
                    edit.putInt("Alarm_Notif_Tone", R.raw.blue_bossa)
                    edit.apply()
                } else {
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
            }
        }

        settings_NotifSoundCaravanCheckbox!!.setOnClickListener {
            if (settings_NotificationMessagesAlarmSound != null) {
                settings_NotificationMessagesAlarmSound!!.stop()
            }

            if (settings_NotifSoundCaravanCheckbox!!.isChecked) {

                //settings_NotifNoSoundCheckbox!!.isChecked = false
                //settings_NotifSoundKnockinCheckbox!!.isChecked = false
                //settings_NotifSoundXyloCheckbox!!.isChecked = false

                settings_NotifSoundMoaninCheckbox!!.isChecked = false
                settings_NotifSoundBlueBossaCheckbox!!.isChecked = false
//                settings_NotifSoundCaravanCheckbox!!.isChecked = false
                settings_NotifSoundDolphinDanceCheckbox!!.isChecked = false
                settings_NotifSoundAutumnLeavesCheckbox!!.isChecked = false
                settings_NotifSoundFreddieFreeloaderCheckbox!!.isChecked = false
                settings_NotifsoundJazzyUploadSoundPath?.setText("")

                settings_NotifsoundFunkyUploadSoundPath?.setText("")
                settings_NotifSoundSlapCheckbox!!.isChecked = false
                settings_NotifSoundOffTheCurveCheckbox!!.isChecked = false
                settings_NotifSoundFunkYallCheckbox!!.isChecked = false
                settings_NotifSoundKeyboardFunkyToneCheckbox!!.isChecked = false
                settings_NotifSoundUCantHoldNoGrooveCheckbox!!.isChecked = false
                settings_NotifSoundColdSweatCheckbox!!.isChecked = false

                settings_NotifsoundRelaxUploadSoundPath?.setText("")
                settings_NotifSoundAcousticGuitarCheckbox!!.isChecked = false
                settings_NotifSoundGravityCheckbox!!.isChecked = false
                settings_NotifSoundSlowDancingCheckbox!!.isChecked = false
                settings_NotifSoundScorpionThemeCheckbox!!.isChecked = false
                settings_NotifSoundFirstStepCheckbox!!.isChecked = false
                settings_NotifSoundRelaxToneCheckbox!!.isChecked = false

                if (notifJazzySoundIsBought) {

                    settings_NotificationMessagesAlarmSound =
                        MediaPlayer.create(this, R.raw.caravan)
                    settings_NotificationMessagesAlarmSound!!.start()

                    val edit: SharedPreferences.Editor = sharedAlarmNotifTonePreferences.edit()
                    edit.putString("Alarm_Custom_Notif_Tone", null)
                    edit.apply()
                    edit.putInt("Alarm_Notif_Tone", R.raw.caravan)
                    edit.apply()
                } else {
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
            }
        }

        settings_NotifSoundDolphinDanceCheckbox!!.setOnClickListener {
            if (settings_NotificationMessagesAlarmSound != null) {
                settings_NotificationMessagesAlarmSound!!.stop()
            }

            if (settings_NotifSoundDolphinDanceCheckbox!!.isChecked) {

                //settings_NotifNoSoundCheckbox!!.isChecked = false
                //settings_NotifSoundKnockinCheckbox!!.isChecked = false
                //settings_NotifSoundXyloCheckbox!!.isChecked = false

                settings_NotifSoundMoaninCheckbox!!.isChecked = false
                settings_NotifSoundBlueBossaCheckbox!!.isChecked = false
                settings_NotifSoundCaravanCheckbox!!.isChecked = false
//                settings_NotifSoundDolphinDanceCheckbox!!.isChecked = false
                settings_NotifSoundAutumnLeavesCheckbox!!.isChecked = false
                settings_NotifSoundFreddieFreeloaderCheckbox!!.isChecked = false
                settings_NotifsoundJazzyUploadSoundPath?.setText("")

                settings_NotifsoundFunkyUploadSoundPath?.setText("")
                settings_NotifSoundSlapCheckbox!!.isChecked = false
                settings_NotifSoundOffTheCurveCheckbox!!.isChecked = false
                settings_NotifSoundFunkYallCheckbox!!.isChecked = false
                settings_NotifSoundKeyboardFunkyToneCheckbox!!.isChecked = false
                settings_NotifSoundUCantHoldNoGrooveCheckbox!!.isChecked = false
                settings_NotifSoundColdSweatCheckbox!!.isChecked = false

                settings_NotifsoundRelaxUploadSoundPath?.setText("")
                settings_NotifSoundAcousticGuitarCheckbox!!.isChecked = false
                settings_NotifSoundGravityCheckbox!!.isChecked = false
                settings_NotifSoundSlowDancingCheckbox!!.isChecked = false
                settings_NotifSoundScorpionThemeCheckbox!!.isChecked = false
                settings_NotifSoundFirstStepCheckbox!!.isChecked = false
                settings_NotifSoundRelaxToneCheckbox!!.isChecked = false

                if (notifJazzySoundIsBought) {

                    settings_NotificationMessagesAlarmSound =
                        MediaPlayer.create(this, R.raw.dolphin_dance)
                    settings_NotificationMessagesAlarmSound!!.start()

                    val edit: SharedPreferences.Editor = sharedAlarmNotifTonePreferences.edit()
                    edit.putString("Alarm_Custom_Notif_Tone", null)
                    edit.apply()
                    edit.putInt("Alarm_Notif_Tone", R.raw.dolphin_dance)
                    edit.apply()
                } else {
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
            }
        }

        settings_NotifSoundAutumnLeavesCheckbox!!.setOnClickListener {
            if (settings_NotificationMessagesAlarmSound != null) {
                settings_NotificationMessagesAlarmSound!!.stop()
            }

            if (settings_NotifSoundAutumnLeavesCheckbox!!.isChecked) {

                //settings_NotifNoSoundCheckbox!!.isChecked = false
                //settings_NotifSoundKnockinCheckbox!!.isChecked = false
                //settings_NotifSoundXyloCheckbox!!.isChecked = false

                settings_NotifSoundMoaninCheckbox!!.isChecked = false
                settings_NotifSoundBlueBossaCheckbox!!.isChecked = false
                settings_NotifSoundCaravanCheckbox!!.isChecked = false
                settings_NotifSoundDolphinDanceCheckbox!!.isChecked = false
//                settings_NotifSoundAutumnLeavesCheckbox!!.isChecked = false
                settings_NotifSoundFreddieFreeloaderCheckbox!!.isChecked = false
                settings_NotifsoundJazzyUploadSoundPath?.setText("")

                settings_NotifsoundFunkyUploadSoundPath?.setText("")

                settings_NotifSoundSlapCheckbox!!.isChecked = false
                settings_NotifSoundOffTheCurveCheckbox!!.isChecked = false
                settings_NotifSoundFunkYallCheckbox!!.isChecked = false
                settings_NotifSoundKeyboardFunkyToneCheckbox!!.isChecked = false
                settings_NotifSoundUCantHoldNoGrooveCheckbox!!.isChecked = false
                settings_NotifSoundColdSweatCheckbox!!.isChecked = false

                settings_NotifsoundRelaxUploadSoundPath?.setText("")
                settings_NotifSoundAcousticGuitarCheckbox!!.isChecked = false
                settings_NotifSoundGravityCheckbox!!.isChecked = false
                settings_NotifSoundSlowDancingCheckbox!!.isChecked = false
                settings_NotifSoundScorpionThemeCheckbox!!.isChecked = false
                settings_NotifSoundFirstStepCheckbox!!.isChecked = false
                settings_NotifSoundRelaxToneCheckbox!!.isChecked = false

                if (notifJazzySoundIsBought) {

                    settings_NotificationMessagesAlarmSound =
                        MediaPlayer.create(this, R.raw.autumn_leaves)
                    settings_NotificationMessagesAlarmSound!!.start()

                    val edit: SharedPreferences.Editor = sharedAlarmNotifTonePreferences.edit()
                    edit.putString("Alarm_Custom_Notif_Tone", null)
                    edit.apply()
                    edit.putInt("Alarm_Notif_Tone", R.raw.autumn_leaves)
                    edit.apply()
                } else {
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
            }
        }

        settings_NotifSoundFreddieFreeloaderCheckbox!!.setOnClickListener {
            if (settings_NotificationMessagesAlarmSound != null) {
                settings_NotificationMessagesAlarmSound!!.stop()
            }

            if (settings_NotifSoundFreddieFreeloaderCheckbox!!.isChecked) {

                //settings_NotifNoSoundCheckbox!!.isChecked = false
                //settings_NotifSoundKnockinCheckbox!!.isChecked = false
                //settings_NotifSoundXyloCheckbox!!.isChecked = false

                settings_NotifSoundMoaninCheckbox!!.isChecked = false
                settings_NotifSoundBlueBossaCheckbox!!.isChecked = false
                settings_NotifSoundCaravanCheckbox!!.isChecked = false
                settings_NotifSoundDolphinDanceCheckbox!!.isChecked = false
                settings_NotifSoundAutumnLeavesCheckbox!!.isChecked = false
//                settings_NotifSoundFreddieFreeloaderCheckbox!!.isChecked = false
                settings_NotifsoundJazzyUploadSoundPath?.setText("")

                settings_NotifsoundFunkyUploadSoundPath?.setText("")

                settings_NotifSoundSlapCheckbox!!.isChecked = false
                settings_NotifSoundOffTheCurveCheckbox!!.isChecked = false
                settings_NotifSoundFunkYallCheckbox!!.isChecked = false
                settings_NotifSoundKeyboardFunkyToneCheckbox!!.isChecked = false
                settings_NotifSoundUCantHoldNoGrooveCheckbox!!.isChecked = false
                settings_NotifSoundColdSweatCheckbox!!.isChecked = false

                settings_NotifsoundRelaxUploadSoundPath?.setText("")
                settings_NotifSoundAcousticGuitarCheckbox!!.isChecked = false
                settings_NotifSoundGravityCheckbox!!.isChecked = false
                settings_NotifSoundSlowDancingCheckbox!!.isChecked = false
                settings_NotifSoundScorpionThemeCheckbox!!.isChecked = false
                settings_NotifSoundFirstStepCheckbox!!.isChecked = false
                settings_NotifSoundRelaxToneCheckbox!!.isChecked = false

                if (notifJazzySoundIsBought) {

                    settings_NotificationMessagesAlarmSound =
                        MediaPlayer.create(this, R.raw.freddie_freeloader)
                    settings_NotificationMessagesAlarmSound!!.start()

                    val edit: SharedPreferences.Editor = sharedAlarmNotifTonePreferences.edit()
                    edit.putString("Alarm_Custom_Notif_Tone", null)
                    edit.apply()
                    edit.putInt("Alarm_Notif_Tone", R.raw.freddie_freeloader)
                    edit.apply()
                } else {
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
            }
        }

        //endregion

        //region Funky

        settings_NotifSoundSlapCheckbox!!.setOnClickListener {
            if (settings_NotificationMessagesAlarmSound != null) {
                settings_NotificationMessagesAlarmSound!!.stop()
            }

            if (settings_NotifSoundSlapCheckbox!!.isChecked) {
                settings_NotificationMessagesAlarmSound = MediaPlayer.create(this, R.raw.bass_slap)
                settings_NotificationMessagesAlarmSound!!.start()

                //settings_NotifNoSoundCheckbox!!.isChecked = false
                //settings_NotifSoundKnockinCheckbox!!.isChecked = false
                //settings_NotifSoundXyloCheckbox!!.isChecked = false

                settings_NotifSoundMoaninCheckbox!!.isChecked = false
                settings_NotifSoundBlueBossaCheckbox!!.isChecked = false
                settings_NotifSoundCaravanCheckbox!!.isChecked = false
                settings_NotifSoundDolphinDanceCheckbox!!.isChecked = false
                settings_NotifSoundAutumnLeavesCheckbox!!.isChecked = false
                settings_NotifSoundFreddieFreeloaderCheckbox!!.isChecked = false
                settings_NotifsoundJazzyUploadSoundPath?.setText("")

                settings_NotifsoundFunkyUploadSoundPath?.setText("")

//                settings_NotifSoundSlapCheckbox!!.isChecked = false
                settings_NotifSoundOffTheCurveCheckbox!!.isChecked = false
                settings_NotifSoundFunkYallCheckbox!!.isChecked = false
                settings_NotifSoundKeyboardFunkyToneCheckbox!!.isChecked = false
                settings_NotifSoundUCantHoldNoGrooveCheckbox!!.isChecked = false
                settings_NotifSoundColdSweatCheckbox!!.isChecked = false

                settings_NotifsoundRelaxUploadSoundPath?.setText("")
                settings_NotifSoundAcousticGuitarCheckbox!!.isChecked = false
                settings_NotifSoundGravityCheckbox!!.isChecked = false
                settings_NotifSoundSlowDancingCheckbox!!.isChecked = false
                settings_NotifSoundScorpionThemeCheckbox!!.isChecked = false
                settings_NotifSoundFirstStepCheckbox!!.isChecked = false
                settings_NotifSoundRelaxToneCheckbox!!.isChecked = false

                if (notifFunkySoundIsBought) {
                    val edit: SharedPreferences.Editor = sharedAlarmNotifTonePreferences.edit()
                    edit.putString("Alarm_Custom_Notif_Tone", null)
                    edit.apply()
                    edit.putInt("Alarm_Notif_Tone", R.raw.bass_slap)
                    edit.apply()
                } else {
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
            }
        }

        settings_NotifSoundOffTheCurveCheckbox!!.setOnClickListener {
            if (settings_NotificationMessagesAlarmSound != null) {
                settings_NotificationMessagesAlarmSound!!.stop()
            }

            if (settings_NotifSoundOffTheCurveCheckbox!!.isChecked) {

                //settings_NotifNoSoundCheckbox!!.isChecked = false
                //settings_NotifSoundKnockinCheckbox!!.isChecked = false
                //settings_NotifSoundXyloCheckbox!!.isChecked = false

                settings_NotifSoundMoaninCheckbox!!.isChecked = false
                settings_NotifSoundBlueBossaCheckbox!!.isChecked = false
                settings_NotifSoundCaravanCheckbox!!.isChecked = false
                settings_NotifSoundDolphinDanceCheckbox!!.isChecked = false
                settings_NotifSoundAutumnLeavesCheckbox!!.isChecked = false
                settings_NotifSoundFreddieFreeloaderCheckbox!!.isChecked = false
                settings_NotifsoundJazzyUploadSoundPath?.setText("")

                settings_NotifsoundFunkyUploadSoundPath?.setText("")

                settings_NotifSoundSlapCheckbox!!.isChecked = false
//                settings_NotifSoundOffTheCurveCheckbox!!.isChecked = false
                settings_NotifSoundFunkYallCheckbox!!.isChecked = false
                settings_NotifSoundKeyboardFunkyToneCheckbox!!.isChecked = false
                settings_NotifSoundUCantHoldNoGrooveCheckbox!!.isChecked = false
                settings_NotifSoundColdSweatCheckbox!!.isChecked = false

                settings_NotifsoundRelaxUploadSoundPath?.setText("")
                settings_NotifSoundAcousticGuitarCheckbox!!.isChecked = false
                settings_NotifSoundGravityCheckbox!!.isChecked = false
                settings_NotifSoundSlowDancingCheckbox!!.isChecked = false
                settings_NotifSoundScorpionThemeCheckbox!!.isChecked = false
                settings_NotifSoundFirstStepCheckbox!!.isChecked = false
                settings_NotifSoundRelaxToneCheckbox!!.isChecked = false

                if (notifFunkySoundIsBought) {
                    settings_NotificationMessagesAlarmSound =
                        MediaPlayer.create(this, R.raw.off_the_curve_groove)
                    settings_NotificationMessagesAlarmSound!!.start()

                    val edit: SharedPreferences.Editor = sharedAlarmNotifTonePreferences.edit()
                    edit.putString("Alarm_Custom_Notif_Tone", null)
                    edit.apply()
                    edit.putInt("Alarm_Notif_Tone", R.raw.off_the_curve_groove)
                    edit.apply()

                } else {
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
            }
        }

        settings_NotifSoundFunkYallCheckbox!!.setOnClickListener {
            if (settings_NotificationMessagesAlarmSound != null) {
                settings_NotificationMessagesAlarmSound!!.stop()
            }

            if (settings_NotifSoundFunkYallCheckbox!!.isChecked) {

                //settings_NotifNoSoundCheckbox!!.isChecked = false
                //settings_NotifSoundKnockinCheckbox!!.isChecked = false
                //settings_NotifSoundXyloCheckbox!!.isChecked = false

                settings_NotifSoundMoaninCheckbox!!.isChecked = false
                settings_NotifSoundBlueBossaCheckbox!!.isChecked = false
                settings_NotifSoundCaravanCheckbox!!.isChecked = false
                settings_NotifSoundDolphinDanceCheckbox!!.isChecked = false
                settings_NotifSoundAutumnLeavesCheckbox!!.isChecked = false
                settings_NotifSoundFreddieFreeloaderCheckbox!!.isChecked = false
                settings_NotifsoundJazzyUploadSoundPath?.setText("")

                settings_NotifsoundFunkyUploadSoundPath?.setText("")

                settings_NotifSoundSlapCheckbox!!.isChecked = false
                settings_NotifSoundOffTheCurveCheckbox!!.isChecked = false
//                settings_NotifSoundFunkYallCheckbox!!.isChecked = false
                settings_NotifSoundKeyboardFunkyToneCheckbox!!.isChecked = false
                settings_NotifSoundUCantHoldNoGrooveCheckbox!!.isChecked = false
                settings_NotifSoundColdSweatCheckbox!!.isChecked = false

                settings_NotifsoundRelaxUploadSoundPath?.setText("")
                settings_NotifSoundAcousticGuitarCheckbox!!.isChecked = false
                settings_NotifSoundGravityCheckbox!!.isChecked = false
                settings_NotifSoundSlowDancingCheckbox!!.isChecked = false
                settings_NotifSoundScorpionThemeCheckbox!!.isChecked = false
                settings_NotifSoundFirstStepCheckbox!!.isChecked = false
                settings_NotifSoundRelaxToneCheckbox!!.isChecked = false

                if (notifFunkySoundIsBought) {

                    settings_NotificationMessagesAlarmSound =
                        MediaPlayer.create(this, R.raw.funk_yall)
                    settings_NotificationMessagesAlarmSound!!.start()

                    val edit: SharedPreferences.Editor = sharedAlarmNotifTonePreferences.edit()
                    edit.putString("Alarm_Custom_Notif_Tone", null)
                    edit.apply()
                    edit.putInt("Alarm_Notif_Tone", R.raw.off_the_curve_groove)
                    edit.apply()
                } else {
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
            }
        }

        settings_NotifSoundKeyboardFunkyToneCheckbox!!.setOnClickListener {
            if (settings_NotificationMessagesAlarmSound != null) {
                settings_NotificationMessagesAlarmSound!!.stop()
            }

            if (settings_NotifSoundKeyboardFunkyToneCheckbox!!.isChecked) {

                //settings_NotifNoSoundCheckbox!!.isChecked = false
                //settings_NotifSoundKnockinCheckbox!!.isChecked = false
                //settings_NotifSoundXyloCheckbox!!.isChecked = false

                settings_NotifSoundMoaninCheckbox!!.isChecked = false
                settings_NotifSoundBlueBossaCheckbox!!.isChecked = false
                settings_NotifSoundCaravanCheckbox!!.isChecked = false
                settings_NotifSoundDolphinDanceCheckbox!!.isChecked = false
                settings_NotifSoundAutumnLeavesCheckbox!!.isChecked = false
                settings_NotifSoundFreddieFreeloaderCheckbox!!.isChecked = false
                settings_NotifsoundJazzyUploadSoundPath?.setText("")

                settings_NotifsoundFunkyUploadSoundPath?.setText("")

                settings_NotifSoundSlapCheckbox!!.isChecked = false
                settings_NotifSoundOffTheCurveCheckbox!!.isChecked = false
                settings_NotifSoundFunkYallCheckbox!!.isChecked = false
//                settings_NotifSoundKeyboardFunkyToneCheckbox!!.isChecked = false
                settings_NotifSoundUCantHoldNoGrooveCheckbox!!.isChecked = false
                settings_NotifSoundColdSweatCheckbox!!.isChecked = false

                settings_NotifsoundRelaxUploadSoundPath?.setText("")
                settings_NotifSoundAcousticGuitarCheckbox!!.isChecked = false
                settings_NotifSoundGravityCheckbox!!.isChecked = false
                settings_NotifSoundSlowDancingCheckbox!!.isChecked = false
                settings_NotifSoundScorpionThemeCheckbox!!.isChecked = false
                settings_NotifSoundFirstStepCheckbox!!.isChecked = false
                settings_NotifSoundRelaxToneCheckbox!!.isChecked = false

                if (notifFunkySoundIsBought) {

                    settings_NotificationMessagesAlarmSound =
                        MediaPlayer.create(this, R.raw.keyboard_funky_tone)
                    settings_NotificationMessagesAlarmSound!!.start()

                    val edit: SharedPreferences.Editor = sharedAlarmNotifTonePreferences.edit()
                    edit.putString("Alarm_Custom_Notif_Tone", null)
                    edit.apply()
                    edit.putInt("Alarm_Notif_Tone", R.raw.keyboard_funky_tone)
                    edit.apply()
                } else {
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
            }
        }

        settings_NotifSoundUCantHoldNoGrooveCheckbox!!.setOnClickListener {
            if (settings_NotificationMessagesAlarmSound != null) {
                settings_NotificationMessagesAlarmSound!!.stop()
            }

            if (settings_NotifSoundUCantHoldNoGrooveCheckbox!!.isChecked) {

                //settings_NotifNoSoundCheckbox!!.isChecked = false
                //settings_NotifSoundKnockinCheckbox!!.isChecked = false
                //settings_NotifSoundXyloCheckbox!!.isChecked = false

                settings_NotifSoundMoaninCheckbox!!.isChecked = false
                settings_NotifSoundBlueBossaCheckbox!!.isChecked = false
                settings_NotifSoundCaravanCheckbox!!.isChecked = false
                settings_NotifSoundDolphinDanceCheckbox!!.isChecked = false
                settings_NotifSoundAutumnLeavesCheckbox!!.isChecked = false
                settings_NotifSoundFreddieFreeloaderCheckbox!!.isChecked = false
                settings_NotifsoundJazzyUploadSoundPath?.setText("")

                settings_NotifsoundFunkyUploadSoundPath?.setText("")

                settings_NotifSoundSlapCheckbox!!.isChecked = false
                settings_NotifSoundOffTheCurveCheckbox!!.isChecked = false
                settings_NotifSoundFunkYallCheckbox!!.isChecked = false
                settings_NotifSoundKeyboardFunkyToneCheckbox!!.isChecked = false
//                settings_NotifSoundUCantHoldNoGrooveCheckbox!!.isChecked = false
                settings_NotifSoundColdSweatCheckbox!!.isChecked = false

                settings_NotifsoundRelaxUploadSoundPath?.setText("")
                settings_NotifSoundAcousticGuitarCheckbox!!.isChecked = false
                settings_NotifSoundGravityCheckbox!!.isChecked = false
                settings_NotifSoundSlowDancingCheckbox!!.isChecked = false
                settings_NotifSoundScorpionThemeCheckbox!!.isChecked = false
                settings_NotifSoundFirstStepCheckbox!!.isChecked = false
                settings_NotifSoundRelaxToneCheckbox!!.isChecked = false

                if (notifFunkySoundIsBought) {

                    settings_NotificationMessagesAlarmSound =
                        MediaPlayer.create(this, R.raw.u_cant_hold_no_groove)
                    settings_NotificationMessagesAlarmSound!!.start()

                    val edit: SharedPreferences.Editor = sharedAlarmNotifTonePreferences.edit()
                    edit.putString("Alarm_Custom_Notif_Tone", null)
                    edit.apply()
                    edit.putInt("Alarm_Notif_Tone", R.raw.u_cant_hold_no_groove)
                    edit.apply()
                } else {
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
            }
        }

        settings_NotifSoundColdSweatCheckbox!!.setOnClickListener {
            if (settings_NotificationMessagesAlarmSound != null) {
                settings_NotificationMessagesAlarmSound!!.stop()
            }

            if (settings_NotifSoundColdSweatCheckbox!!.isChecked) {

                //settings_NotifNoSoundCheckbox!!.isChecked = false
                //settings_NotifSoundKnockinCheckbox!!.isChecked = false
                //settings_NotifSoundXyloCheckbox!!.isChecked = false

                settings_NotifSoundMoaninCheckbox!!.isChecked = false
                settings_NotifSoundBlueBossaCheckbox!!.isChecked = false
                settings_NotifSoundCaravanCheckbox!!.isChecked = false
                settings_NotifSoundDolphinDanceCheckbox!!.isChecked = false
                settings_NotifSoundAutumnLeavesCheckbox!!.isChecked = false
                settings_NotifSoundFreddieFreeloaderCheckbox!!.isChecked = false
                settings_NotifsoundJazzyUploadSoundPath?.setText("")

                settings_NotifsoundFunkyUploadSoundPath?.setText("")

                settings_NotifSoundSlapCheckbox!!.isChecked = false
                settings_NotifSoundOffTheCurveCheckbox!!.isChecked = false
                settings_NotifSoundFunkYallCheckbox!!.isChecked = false
                settings_NotifSoundKeyboardFunkyToneCheckbox!!.isChecked = false
                settings_NotifSoundUCantHoldNoGrooveCheckbox!!.isChecked = false

                settings_NotifsoundRelaxUploadSoundPath?.setText("")
                settings_NotifSoundAcousticGuitarCheckbox!!.isChecked = false
                settings_NotifSoundGravityCheckbox!!.isChecked = false
                settings_NotifSoundSlowDancingCheckbox!!.isChecked = false
                settings_NotifSoundScorpionThemeCheckbox!!.isChecked = false
                settings_NotifSoundFirstStepCheckbox!!.isChecked = false
                settings_NotifSoundRelaxToneCheckbox!!.isChecked = false

                if (notifFunkySoundIsBought) {

                    settings_NotificationMessagesAlarmSound =
                        MediaPlayer.create(this, R.raw.cold_sweat)
                    settings_NotificationMessagesAlarmSound!!.start()

                    val edit: SharedPreferences.Editor = sharedAlarmNotifTonePreferences.edit()
                    edit.putString("Alarm_Custom_Notif_Tone", null)
                    edit.apply()
                    edit.putInt("Alarm_Notif_Tone", R.raw.cold_sweat)
                    edit.apply()
                } else {
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
            }
        }

        //endregion

        //region Relaxation

        settings_NotifSoundAcousticGuitarCheckbox!!.setOnClickListener {
            if (settings_NotificationMessagesAlarmSound != null) {
                settings_NotificationMessagesAlarmSound!!.stop()
            }

            if (settings_NotifSoundAcousticGuitarCheckbox!!.isChecked) {

                //settings_NotifNoSoundCheckbox!!.isChecked = false
                //settings_NotifSoundKnockinCheckbox!!.isChecked = false
                //settings_NotifSoundXyloCheckbox!!.isChecked = false

                settings_NotifSoundMoaninCheckbox!!.isChecked = false
                settings_NotifSoundBlueBossaCheckbox!!.isChecked = false
                settings_NotifSoundCaravanCheckbox!!.isChecked = false
                settings_NotifSoundDolphinDanceCheckbox!!.isChecked = false
                settings_NotifSoundAutumnLeavesCheckbox!!.isChecked = false
                settings_NotifSoundFreddieFreeloaderCheckbox!!.isChecked = false
                settings_NotifsoundJazzyUploadSoundPath?.setText("")

                settings_NotifsoundFunkyUploadSoundPath?.setText("")

                settings_NotifSoundSlapCheckbox!!.isChecked = false
                settings_NotifSoundOffTheCurveCheckbox!!.isChecked = false
                settings_NotifSoundFunkYallCheckbox!!.isChecked = false
                settings_NotifSoundKeyboardFunkyToneCheckbox!!.isChecked = false
                settings_NotifSoundUCantHoldNoGrooveCheckbox!!.isChecked = false
                settings_NotifSoundColdSweatCheckbox!!.isChecked = false

                settings_NotifsoundRelaxUploadSoundPath?.setText("")
//                settings_NotifSoundAcousticGuitarCheckbox!!.isChecked = false
                settings_NotifSoundGravityCheckbox!!.isChecked = false
                settings_NotifSoundSlowDancingCheckbox!!.isChecked = false
                settings_NotifSoundScorpionThemeCheckbox!!.isChecked = false
                settings_NotifSoundFirstStepCheckbox!!.isChecked = false
                settings_NotifSoundRelaxToneCheckbox!!.isChecked = false

                if (notifRelaxationSoundIsBought) {
                    settings_NotificationMessagesAlarmSound =
                        MediaPlayer.create(this, R.raw.beautiful_chords_progression)
                    settings_NotificationMessagesAlarmSound!!.start()

                    val edit: SharedPreferences.Editor = sharedAlarmNotifTonePreferences.edit()
                    edit.putString("Alarm_Custom_Notif_Tone", null)
                    edit.apply()
                    edit.putInt("Alarm_Notif_Tone", R.raw.beautiful_chords_progression)
                    edit.apply()
                } else {
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
            }
        }

        settings_NotifSoundGravityCheckbox!!.setOnClickListener {
            if (settings_NotificationMessagesAlarmSound != null) {
                settings_NotificationMessagesAlarmSound!!.stop()
            }

            if (settings_NotifSoundGravityCheckbox!!.isChecked) {

                //settings_NotifNoSoundCheckbox!!.isChecked = false
                //settings_NotifSoundKnockinCheckbox!!.isChecked = false
                //settings_NotifSoundXyloCheckbox!!.isChecked = false

                settings_NotifSoundMoaninCheckbox!!.isChecked = false
                settings_NotifSoundBlueBossaCheckbox!!.isChecked = false
                settings_NotifSoundCaravanCheckbox!!.isChecked = false
                settings_NotifSoundDolphinDanceCheckbox!!.isChecked = false
                settings_NotifSoundAutumnLeavesCheckbox!!.isChecked = false
                settings_NotifSoundFreddieFreeloaderCheckbox!!.isChecked = false
                settings_NotifsoundJazzyUploadSoundPath?.setText("")

                settings_NotifsoundFunkyUploadSoundPath?.setText("")

                settings_NotifSoundSlapCheckbox!!.isChecked = false
                settings_NotifSoundOffTheCurveCheckbox!!.isChecked = false
                settings_NotifSoundFunkYallCheckbox!!.isChecked = false
                settings_NotifSoundKeyboardFunkyToneCheckbox!!.isChecked = false
                settings_NotifSoundUCantHoldNoGrooveCheckbox!!.isChecked = false
                settings_NotifSoundColdSweatCheckbox!!.isChecked = false

                settings_NotifsoundRelaxUploadSoundPath?.setText("")
                settings_NotifSoundAcousticGuitarCheckbox!!.isChecked = false
//                settings_NotifSoundGravityCheckbox!!.isChecked = false
                settings_NotifSoundSlowDancingCheckbox!!.isChecked = false
                settings_NotifSoundScorpionThemeCheckbox!!.isChecked = false
                settings_NotifSoundFirstStepCheckbox!!.isChecked = false
                settings_NotifSoundRelaxToneCheckbox!!.isChecked = false

                if (notifRelaxationSoundIsBought) {
                    settings_NotificationMessagesAlarmSound =
                        MediaPlayer.create(this, R.raw.gravity)
                    settings_NotificationMessagesAlarmSound!!.start()

                    val edit: SharedPreferences.Editor = sharedAlarmNotifTonePreferences.edit()
                    edit.putString("Alarm_Custom_Notif_Tone", null)
                    edit.apply()
                    edit.putInt("Alarm_Notif_Tone", R.raw.gravity)
                    edit.apply()

                } else {
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
            }
        }

        settings_NotifSoundSlowDancingCheckbox!!.setOnClickListener {
            if (settings_NotificationMessagesAlarmSound != null) {
                settings_NotificationMessagesAlarmSound!!.stop()
            }

            if (settings_NotifSoundSlowDancingCheckbox!!.isChecked) {

                //settings_NotifNoSoundCheckbox!!.isChecked = false
                //settings_NotifSoundKnockinCheckbox!!.isChecked = false
                //settings_NotifSoundXyloCheckbox!!.isChecked = false

                settings_NotifSoundMoaninCheckbox!!.isChecked = false
                settings_NotifSoundBlueBossaCheckbox!!.isChecked = false
                settings_NotifSoundCaravanCheckbox!!.isChecked = false
                settings_NotifSoundDolphinDanceCheckbox!!.isChecked = false
                settings_NotifSoundAutumnLeavesCheckbox!!.isChecked = false
                settings_NotifSoundFreddieFreeloaderCheckbox!!.isChecked = false
                settings_NotifsoundJazzyUploadSoundPath?.setText("")

                settings_NotifsoundFunkyUploadSoundPath?.setText("")

                settings_NotifSoundSlapCheckbox!!.isChecked = false
                settings_NotifSoundOffTheCurveCheckbox!!.isChecked = false
                settings_NotifSoundFunkYallCheckbox!!.isChecked = false
                settings_NotifSoundKeyboardFunkyToneCheckbox!!.isChecked = false
                settings_NotifSoundUCantHoldNoGrooveCheckbox!!.isChecked = false
                settings_NotifSoundColdSweatCheckbox!!.isChecked = false

                settings_NotifsoundRelaxUploadSoundPath?.setText("")
                settings_NotifSoundAcousticGuitarCheckbox!!.isChecked = false
                settings_NotifSoundGravityCheckbox!!.isChecked = false
//                settings_NotifSoundSlowDancingCheckbox!!.isChecked = false
                settings_NotifSoundScorpionThemeCheckbox!!.isChecked = false
                settings_NotifSoundFirstStepCheckbox!!.isChecked = false
                settings_NotifSoundRelaxToneCheckbox!!.isChecked = false

                if (notifRelaxationSoundIsBought) {

                    settings_NotificationMessagesAlarmSound =
                        MediaPlayer.create(this, R.raw.slow_dancing)
                    settings_NotificationMessagesAlarmSound!!.start()

                    val edit: SharedPreferences.Editor = sharedAlarmNotifTonePreferences.edit()
                    edit.putString("Alarm_Custom_Notif_Tone", null)
                    edit.apply()
                    edit.putInt("Alarm_Notif_Tone", R.raw.slow_dancing)
                    edit.apply()
                } else {
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
            }
        }

        settings_NotifSoundScorpionThemeCheckbox!!.setOnClickListener {
            if (settings_NotificationMessagesAlarmSound != null) {
                settings_NotificationMessagesAlarmSound!!.stop()
            }

            if (settings_NotifSoundScorpionThemeCheckbox!!.isChecked) {

                //settings_NotifNoSoundCheckbox!!.isChecked = false
                //settings_NotifSoundKnockinCheckbox!!.isChecked = false
                //settings_NotifSoundXyloCheckbox!!.isChecked = false

                settings_NotifSoundMoaninCheckbox!!.isChecked = false
                settings_NotifSoundBlueBossaCheckbox!!.isChecked = false
                settings_NotifSoundCaravanCheckbox!!.isChecked = false
                settings_NotifSoundDolphinDanceCheckbox!!.isChecked = false
                settings_NotifSoundAutumnLeavesCheckbox!!.isChecked = false
                settings_NotifSoundFreddieFreeloaderCheckbox!!.isChecked = false
                settings_NotifsoundJazzyUploadSoundPath?.setText("")

                settings_NotifsoundFunkyUploadSoundPath?.setText("")

                settings_NotifSoundSlapCheckbox!!.isChecked = false
                settings_NotifSoundOffTheCurveCheckbox!!.isChecked = false
                settings_NotifSoundFunkYallCheckbox!!.isChecked = false
                settings_NotifSoundKeyboardFunkyToneCheckbox!!.isChecked = false
                settings_NotifSoundUCantHoldNoGrooveCheckbox!!.isChecked = false
                settings_NotifSoundColdSweatCheckbox!!.isChecked = false

                settings_NotifsoundRelaxUploadSoundPath?.setText("")
                settings_NotifSoundAcousticGuitarCheckbox!!.isChecked = false
                settings_NotifSoundGravityCheckbox!!.isChecked = false
                settings_NotifSoundSlowDancingCheckbox!!.isChecked = false
//                settings_NotifSoundScorpionThemeCheckbox!!.isChecked = false
                settings_NotifSoundFirstStepCheckbox!!.isChecked = false
                settings_NotifSoundRelaxToneCheckbox!!.isChecked = false

                if (notifRelaxationSoundIsBought) {

                    settings_NotificationMessagesAlarmSound =
                        MediaPlayer.create(this, R.raw.fade_to_black)
                    settings_NotificationMessagesAlarmSound!!.start()

                    val edit: SharedPreferences.Editor = sharedAlarmNotifTonePreferences.edit()
                    edit.putString("Alarm_Custom_Notif_Tone", null)
                    edit.apply()
                    edit.putInt("Alarm_Notif_Tone", R.raw.fade_to_black)
                    edit.apply()
                } else {
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
            }
        }

        settings_NotifSoundFirstStepCheckbox!!.setOnClickListener {
            if (settings_NotificationMessagesAlarmSound != null) {
                settings_NotificationMessagesAlarmSound!!.stop()
            }

            if (settings_NotifSoundFirstStepCheckbox!!.isChecked) {

                //settings_NotifNoSoundCheckbox!!.isChecked = false
                //settings_NotifSoundKnockinCheckbox!!.isChecked = false
                //settings_NotifSoundXyloCheckbox!!.isChecked = false

                settings_NotifSoundMoaninCheckbox!!.isChecked = false
                settings_NotifSoundBlueBossaCheckbox!!.isChecked = false
                settings_NotifSoundCaravanCheckbox!!.isChecked = false
                settings_NotifSoundDolphinDanceCheckbox!!.isChecked = false
                settings_NotifSoundAutumnLeavesCheckbox!!.isChecked = false
                settings_NotifSoundFreddieFreeloaderCheckbox!!.isChecked = false
                settings_NotifsoundJazzyUploadSoundPath?.setText("")

                settings_NotifsoundFunkyUploadSoundPath?.setText("")

                settings_NotifSoundSlapCheckbox!!.isChecked = false
                settings_NotifSoundOffTheCurveCheckbox!!.isChecked = false
                settings_NotifSoundFunkYallCheckbox!!.isChecked = false
                settings_NotifSoundKeyboardFunkyToneCheckbox!!.isChecked = false
                settings_NotifSoundUCantHoldNoGrooveCheckbox!!.isChecked = false
                settings_NotifSoundColdSweatCheckbox!!.isChecked = false

                settings_NotifsoundRelaxUploadSoundPath?.setText("")
                settings_NotifSoundAcousticGuitarCheckbox!!.isChecked = false
                settings_NotifSoundGravityCheckbox!!.isChecked = false
                settings_NotifSoundSlowDancingCheckbox!!.isChecked = false
                settings_NotifSoundScorpionThemeCheckbox!!.isChecked = false
//                settings_NotifSoundFirstStepCheckbox!!.isChecked = false
                settings_NotifSoundRelaxToneCheckbox!!.isChecked = false

                if (notifRelaxationSoundIsBought) {

                    settings_NotificationMessagesAlarmSound =
                        MediaPlayer.create(this, R.raw.interstellar_main_theme)
                    settings_NotificationMessagesAlarmSound!!.start()

                    val edit: SharedPreferences.Editor = sharedAlarmNotifTonePreferences.edit()
                    edit.putString("Alarm_Custom_Notif_Tone", null)
                    edit.apply()
                    edit.putInt("Alarm_Notif_Tone", R.raw.interstellar_main_theme)
                    edit.apply()
                } else {
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
            }
        }

        settings_NotifSoundRelaxToneCheckbox!!.setOnClickListener {
            if (settings_NotificationMessagesAlarmSound != null) {
                settings_NotificationMessagesAlarmSound!!.stop()
            }

            if (settings_NotifSoundRelaxToneCheckbox!!.isChecked) {

                settings_NotificationMessagesAlarmSound = MediaPlayer.create(this, R.raw.relax_sms)
                settings_NotificationMessagesAlarmSound!!.start()

                //settings_NotifNoSoundCheckbox!!.isChecked = false
                //settings_NotifSoundKnockinCheckbox!!.isChecked = false
                //settings_NotifSoundXyloCheckbox!!.isChecked = false

                settings_NotifSoundMoaninCheckbox!!.isChecked = false
                settings_NotifSoundBlueBossaCheckbox!!.isChecked = false
                settings_NotifSoundCaravanCheckbox!!.isChecked = false
                settings_NotifSoundDolphinDanceCheckbox!!.isChecked = false
                settings_NotifSoundAutumnLeavesCheckbox!!.isChecked = false
                settings_NotifSoundFreddieFreeloaderCheckbox!!.isChecked = false
                settings_NotifsoundJazzyUploadSoundPath?.setText("")

                settings_NotifsoundFunkyUploadSoundPath?.setText("")
                settings_NotifSoundSlapCheckbox!!.isChecked = false
                settings_NotifSoundOffTheCurveCheckbox!!.isChecked = false
                settings_NotifSoundFunkYallCheckbox!!.isChecked = false
                settings_NotifSoundKeyboardFunkyToneCheckbox!!.isChecked = false
                settings_NotifSoundUCantHoldNoGrooveCheckbox!!.isChecked = false
                settings_NotifSoundColdSweatCheckbox!!.isChecked = false

                settings_NotifsoundRelaxUploadSoundPath?.setText("")
                settings_NotifSoundAcousticGuitarCheckbox!!.isChecked = false
                settings_NotifSoundGravityCheckbox!!.isChecked = false
                settings_NotifSoundSlowDancingCheckbox!!.isChecked = false
                settings_NotifSoundScorpionThemeCheckbox!!.isChecked = false
                settings_NotifSoundFirstStepCheckbox!!.isChecked = false
//                settings_NotifSoundRelaxToneCheckbox!!.isChecked = false

                if (notifRelaxationSoundIsBought) {

                    settings_NotificationMessagesAlarmSound =
                        MediaPlayer.create(this, R.raw.relax_sms)
                    settings_NotificationMessagesAlarmSound!!.start()

                    val edit: SharedPreferences.Editor = sharedAlarmNotifTonePreferences.edit()
                    edit.putString("Alarm_Custom_Notif_Tone", null)
                    edit.apply()
                    edit.putInt("Alarm_Notif_Tone", R.raw.relax_sms)
                    edit.apply()
                } else {
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
            }
        }
        //endregion
        //endregion

    }


    //region ========================================== Functions ===========================================

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

    //End Permission
    //check runtime permission
    private fun checkRuntimePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                //permission denied
                val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                //show popup to request runtime permission
                requestPermissions(permissions, ManageNotificationActivity.PERMISSION_CODE)
            } else {
                getTones()
            }
        } else {
            getTones()
        }
    }

    //End
    //get personal Tones
    private fun getTones() {
        /*
        val currentTone: Uri = RingtoneManager.getActualDefaultRingtoneUri(this@EditContactVipSettingsActivity,
                RingtoneManager.TYPE_NOTIFICATION)
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Notification Tone")
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currentTone)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
        startActivityForResult(intent, 10)

         */
        val intent: Intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "audio/*"
        startActivityForResult(Intent.createChooser(intent, "Title"), 89)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode === 89 && resultCode === RESULT_OK) {
            if (data != null && data.getData() != null) {
                val audioFileUri: Uri = data.getData()!!
                // use uri to get path
                val path = audioFileUri.path
                settings_NotifsoundJazzyUploadSoundPath.setText("From :" + path)
                val sharedAlarmNotifTonePreferences: SharedPreferences =
                    getSharedPreferences("Alarm_Notif_Tone", Context.MODE_PRIVATE)
                val edit: SharedPreferences.Editor = sharedAlarmNotifTonePreferences.edit()
                edit.putString("Alarm_Custom_Notif_Tone", audioFileUri.toString())
                edit.apply()
            }
        }
    }
    /*

    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 10 && resultCode == RESULT_OK){
            filePath = data!!.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)!!

            settings_NotifsoundFunkyUploadSoundPath.setText("From :" + filePath!!.path)
            settings_NotifsoundJazzyUploadSoundPath.setText("From :" + filePath!!.path)
            settings_NotifsoundRelaxUploadSoundPath.setText("From :" + filePath!!.path)

            if (notifJazzySoundIsBought) {
                val sharedAlarmNotifTonePreferences: SharedPreferences = getSharedPreferences("Alarm_Notif_Tone", Context.MODE_PRIVATE)
                val edit: SharedPreferences.Editor = sharedAlarmNotifTonePreferences.edit()
                edit.putString("Alarm_Custom_Notif_Tone", filePath.toString())
                edit.apply()
            }

             else if (notifFunkySoundIsBought) {
                val sharedAlarmNotifTonePreferences: SharedPreferences = getSharedPreferences("Alarm_Notif_Tone", Context.MODE_PRIVATE)
                val edit: SharedPreferences.Editor = sharedAlarmNotifTonePreferences.edit()
                edit.putString("Alarm_Custom_Notif_Tone", filePath.toString())
                edit.apply()
            }else if (notifRelaxationSoundIsBought) {
                val sharedAlarmNotifTonePreferences: SharedPreferences = getSharedPreferences("Alarm_Notif_Tone", Context.MODE_PRIVATE)
                val edit: SharedPreferences.Editor = sharedAlarmNotifTonePreferences.edit()
                edit.putString("Alarm_Custom_Notif_Tone", filePath.toString())
                edit.apply()
            }else{
                MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                        .setTitle(getString(R.string.in_app_popup_tone_available_title))
                        .setMessage(getString(R.string.in_app_popup_tone_available_message))
                        .setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
                            goToPremiumActivity()
                        }
                        .setNegativeButton(R.string.alert_dialog_later) { _, _ ->
                            settings_NotifsoundJazzyUploadSoundPath.setText("")
                            settings_NotifsoundFunkyUploadSoundPath.setText("")
                            settings_NotifsoundRelaxUploadSoundPath.setText("")
                            refreshChecked()
                        }
                        .show()
            }

        }
    }
    */
    //EndTones

    fun refreshChecked() {

        if (settings_NotificationMessagesAlarmSound != null) {
            settings_NotificationMessagesAlarmSound!!.stop()
        }

        when (numberDefault) {
            1 -> {
                //settings_NotifNoSoundCheckbox!!.isChecked = false
                //settings_NotifSoundKnockinCheckbox!!.isChecked = false
                //settings_NotifSoundXyloCheckbox!!.isChecked = false

                settings_NotifSoundMoaninCheckbox!!.isChecked = false
                settings_NotifSoundBlueBossaCheckbox!!.isChecked = false
                settings_NotifSoundCaravanCheckbox!!.isChecked = false
                settings_NotifSoundDolphinDanceCheckbox!!.isChecked = false
                settings_NotifSoundAutumnLeavesCheckbox!!.isChecked = false
                settings_NotifSoundFreddieFreeloaderCheckbox!!.isChecked = false

                settings_NotifSoundSlapCheckbox!!.isChecked = false
                settings_NotifSoundOffTheCurveCheckbox!!.isChecked = false
                settings_NotifSoundFunkYallCheckbox!!.isChecked = false
                settings_NotifSoundKeyboardFunkyToneCheckbox!!.isChecked = false
                settings_NotifSoundUCantHoldNoGrooveCheckbox!!.isChecked = false
                settings_NotifSoundColdSweatCheckbox!!.isChecked = false

                settings_NotifSoundAcousticGuitarCheckbox!!.isChecked = false
                settings_NotifSoundGravityCheckbox!!.isChecked = false
                settings_NotifSoundSlowDancingCheckbox!!.isChecked = false
                settings_NotifSoundScorpionThemeCheckbox!!.isChecked = false
                settings_NotifSoundFirstStepCheckbox!!.isChecked = false
                settings_NotifSoundRelaxToneCheckbox!!.isChecked = false
            }

            R.raw.sms_ring -> {


                //settings_NotifNoSoundCheckbox!!.isChecked = false
                //settings_NotifSoundKnockinCheckbox!!.isChecked = false
                //settings_NotifSoundXyloCheckbox!!.isChecked = false

                settings_NotifSoundMoaninCheckbox!!.isChecked = false
                settings_NotifSoundBlueBossaCheckbox!!.isChecked = false
                settings_NotifSoundCaravanCheckbox!!.isChecked = false
                settings_NotifSoundDolphinDanceCheckbox!!.isChecked = false
                settings_NotifSoundAutumnLeavesCheckbox!!.isChecked = false
                settings_NotifSoundFreddieFreeloaderCheckbox!!.isChecked = false

                settings_NotifSoundSlapCheckbox!!.isChecked = false
                settings_NotifSoundOffTheCurveCheckbox!!.isChecked = false
                settings_NotifSoundFunkYallCheckbox!!.isChecked = false
                settings_NotifSoundKeyboardFunkyToneCheckbox!!.isChecked = false
                settings_NotifSoundUCantHoldNoGrooveCheckbox!!.isChecked = false
                settings_NotifSoundColdSweatCheckbox!!.isChecked = false

                settings_NotifSoundAcousticGuitarCheckbox!!.isChecked = false
                settings_NotifSoundGravityCheckbox!!.isChecked = false
                settings_NotifSoundSlowDancingCheckbox!!.isChecked = false
                settings_NotifSoundScorpionThemeCheckbox!!.isChecked = false
                settings_NotifSoundFirstStepCheckbox!!.isChecked = false
                settings_NotifSoundRelaxToneCheckbox!!.isChecked = false
            }
            R.raw.xylophone_tone -> {

                //settings_NotifNoSoundCheckbox!!.isChecked = false
                //settings_NotifSoundKnockinCheckbox!!.isChecked = false
                //settings_NotifSoundXyloCheckbox!!.isChecked = false

                settings_NotifSoundMoaninCheckbox!!.isChecked = false
                settings_NotifSoundBlueBossaCheckbox!!.isChecked = false
                settings_NotifSoundCaravanCheckbox!!.isChecked = false
                settings_NotifSoundDolphinDanceCheckbox!!.isChecked = false
                settings_NotifSoundAutumnLeavesCheckbox!!.isChecked = false
                settings_NotifSoundFreddieFreeloaderCheckbox!!.isChecked = false

                settings_NotifSoundSlapCheckbox!!.isChecked = false
                settings_NotifSoundOffTheCurveCheckbox!!.isChecked = false
                settings_NotifSoundFunkYallCheckbox!!.isChecked = false
                settings_NotifSoundKeyboardFunkyToneCheckbox!!.isChecked = false
                settings_NotifSoundUCantHoldNoGrooveCheckbox!!.isChecked = false
                settings_NotifSoundColdSweatCheckbox!!.isChecked = false

                settings_NotifSoundAcousticGuitarCheckbox!!.isChecked = false
                settings_NotifSoundGravityCheckbox!!.isChecked = false
                settings_NotifSoundSlowDancingCheckbox!!.isChecked = false
                settings_NotifSoundScorpionThemeCheckbox!!.isChecked = false
                settings_NotifSoundFirstStepCheckbox!!.isChecked = false
                settings_NotifSoundRelaxToneCheckbox!!.isChecked = false
            }
            R.raw.moanin_jazz -> {
                //settings_NotifNoSoundCheckbox!!.isChecked = false
                //settings_NotifSoundKnockinCheckbox!!.isChecked = false
                //settings_NotifSoundXyloCheckbox!!.isChecked = false

                settings_NotifSoundMoaninCheckbox!!.isChecked = true
                settings_NotifSoundBlueBossaCheckbox!!.isChecked = false
                settings_NotifSoundCaravanCheckbox!!.isChecked = false
                settings_NotifSoundDolphinDanceCheckbox!!.isChecked = false
                settings_NotifSoundAutumnLeavesCheckbox!!.isChecked = false
                settings_NotifSoundFreddieFreeloaderCheckbox!!.isChecked = false
                settings_NotifsoundJazzyUploadSoundPath?.setText("")


                settings_NotifSoundSlapCheckbox!!.isChecked = false
                settings_NotifSoundOffTheCurveCheckbox!!.isChecked = false
                settings_NotifSoundFunkYallCheckbox!!.isChecked = false
                settings_NotifSoundKeyboardFunkyToneCheckbox!!.isChecked = false
                settings_NotifSoundUCantHoldNoGrooveCheckbox!!.isChecked = false
                settings_NotifSoundColdSweatCheckbox!!.isChecked = false
                settings_NotifsoundFunkyUploadSoundPath?.setText("")

                settings_NotifSoundAcousticGuitarCheckbox!!.isChecked = false
                settings_NotifSoundGravityCheckbox!!.isChecked = false
                settings_NotifSoundSlowDancingCheckbox!!.isChecked = false
                settings_NotifSoundScorpionThemeCheckbox!!.isChecked = false
                settings_NotifSoundFirstStepCheckbox!!.isChecked = false
                settings_NotifSoundRelaxToneCheckbox!!.isChecked = false
                settings_NotifsoundRelaxUploadSoundPath.setText("")

            }
            R.raw.blue_bossa -> {
                //settings_NotifNoSoundCheckbox!!.isChecked = false
                //settings_NotifSoundKnockinCheckbox!!.isChecked = false
                //settings_NotifSoundXyloCheckbox!!.isChecked = false

                settings_NotifSoundMoaninCheckbox!!.isChecked = false
                settings_NotifSoundBlueBossaCheckbox!!.isChecked = true
                settings_NotifSoundCaravanCheckbox!!.isChecked = false
                settings_NotifSoundDolphinDanceCheckbox!!.isChecked = false
                settings_NotifSoundAutumnLeavesCheckbox!!.isChecked = false
                settings_NotifSoundFreddieFreeloaderCheckbox!!.isChecked = false
                settings_NotifsoundJazzyUploadSoundPath?.setText("")

                settings_NotifSoundSlapCheckbox!!.isChecked = false
                settings_NotifSoundOffTheCurveCheckbox!!.isChecked = false
                settings_NotifSoundFunkYallCheckbox!!.isChecked = false
                settings_NotifSoundKeyboardFunkyToneCheckbox!!.isChecked = false
                settings_NotifSoundUCantHoldNoGrooveCheckbox!!.isChecked = false
                settings_NotifSoundColdSweatCheckbox!!.isChecked = false
                settings_NotifsoundFunkyUploadSoundPath?.setText("")

                settings_NotifSoundAcousticGuitarCheckbox!!.isChecked = false
                settings_NotifSoundGravityCheckbox!!.isChecked = false
                settings_NotifSoundSlowDancingCheckbox!!.isChecked = false
                settings_NotifSoundScorpionThemeCheckbox!!.isChecked = false
                settings_NotifSoundFirstStepCheckbox!!.isChecked = false
                settings_NotifSoundRelaxToneCheckbox!!.isChecked = false
                settings_NotifsoundRelaxUploadSoundPath.setText("")
            }
            R.raw.caravan -> {
                //settings_NotifNoSoundCheckbox!!.isChecked = false
                //settings_NotifSoundKnockinCheckbox!!.isChecked = false
                //settings_NotifSoundXyloCheckbox!!.isChecked = false

                settings_NotifSoundMoaninCheckbox!!.isChecked = false
                settings_NotifSoundBlueBossaCheckbox!!.isChecked = false
                settings_NotifSoundCaravanCheckbox!!.isChecked = true
                settings_NotifSoundDolphinDanceCheckbox!!.isChecked = false
                settings_NotifSoundAutumnLeavesCheckbox!!.isChecked = false
                settings_NotifSoundFreddieFreeloaderCheckbox!!.isChecked = false
                settings_NotifsoundJazzyUploadSoundPath?.setText("")

                settings_NotifSoundSlapCheckbox!!.isChecked = false
                settings_NotifSoundOffTheCurveCheckbox!!.isChecked = false
                settings_NotifSoundFunkYallCheckbox!!.isChecked = false
                settings_NotifSoundKeyboardFunkyToneCheckbox!!.isChecked = false
                settings_NotifSoundUCantHoldNoGrooveCheckbox!!.isChecked = false
                settings_NotifSoundColdSweatCheckbox!!.isChecked = false
                settings_NotifsoundFunkyUploadSoundPath?.setText("")

                settings_NotifSoundAcousticGuitarCheckbox!!.isChecked = false
                settings_NotifSoundGravityCheckbox!!.isChecked = false
                settings_NotifSoundSlowDancingCheckbox!!.isChecked = false
                settings_NotifSoundScorpionThemeCheckbox!!.isChecked = false
                settings_NotifSoundFirstStepCheckbox!!.isChecked = false
                settings_NotifSoundRelaxToneCheckbox!!.isChecked = false
                settings_NotifsoundRelaxUploadSoundPath.setText("")
            }
            R.raw.dolphin_dance -> {
                //settings_NotifNoSoundCheckbox!!.isChecked = false
                //settings_NotifSoundKnockinCheckbox!!.isChecked = false
                //settings_NotifSoundXyloCheckbox!!.isChecked = false

                settings_NotifSoundMoaninCheckbox!!.isChecked = false
                settings_NotifSoundBlueBossaCheckbox!!.isChecked = false
                settings_NotifSoundCaravanCheckbox!!.isChecked = false
                settings_NotifSoundDolphinDanceCheckbox!!.isChecked = true
                settings_NotifSoundAutumnLeavesCheckbox!!.isChecked = false
                settings_NotifSoundFreddieFreeloaderCheckbox!!.isChecked = false
                settings_NotifsoundJazzyUploadSoundPath?.setText("")

                settings_NotifSoundSlapCheckbox!!.isChecked = false
                settings_NotifSoundOffTheCurveCheckbox!!.isChecked = false
                settings_NotifSoundFunkYallCheckbox!!.isChecked = false
                settings_NotifSoundKeyboardFunkyToneCheckbox!!.isChecked = false
                settings_NotifSoundUCantHoldNoGrooveCheckbox!!.isChecked = false
                settings_NotifSoundColdSweatCheckbox!!.isChecked = false
                settings_NotifsoundFunkyUploadSoundPath?.setText("")

                settings_NotifSoundAcousticGuitarCheckbox!!.isChecked = false
                settings_NotifSoundGravityCheckbox!!.isChecked = false
                settings_NotifSoundSlowDancingCheckbox!!.isChecked = false
                settings_NotifSoundScorpionThemeCheckbox!!.isChecked = false
                settings_NotifSoundFirstStepCheckbox!!.isChecked = false
                settings_NotifSoundRelaxToneCheckbox!!.isChecked = false
                settings_NotifsoundRelaxUploadSoundPath.setText("")
            }
            R.raw.autumn_leaves -> {
                //settings_NotifNoSoundCheckbox!!.isChecked = false
                //settings_NotifSoundKnockinCheckbox!!.isChecked = false
                //settings_NotifSoundXyloCheckbox!!.isChecked = false

                settings_NotifSoundMoaninCheckbox!!.isChecked = false
                settings_NotifSoundBlueBossaCheckbox!!.isChecked = false
                settings_NotifSoundCaravanCheckbox!!.isChecked = false
                settings_NotifSoundDolphinDanceCheckbox!!.isChecked = false
                settings_NotifSoundAutumnLeavesCheckbox!!.isChecked = true
                settings_NotifSoundFreddieFreeloaderCheckbox!!.isChecked = false
                settings_NotifsoundJazzyUploadSoundPath?.setText("")

                settings_NotifSoundSlapCheckbox!!.isChecked = false
                settings_NotifSoundOffTheCurveCheckbox!!.isChecked = false
                settings_NotifSoundFunkYallCheckbox!!.isChecked = false
                settings_NotifSoundKeyboardFunkyToneCheckbox!!.isChecked = false
                settings_NotifSoundUCantHoldNoGrooveCheckbox!!.isChecked = false
                settings_NotifSoundColdSweatCheckbox!!.isChecked = false
                settings_NotifsoundFunkyUploadSoundPath?.setText("")

                settings_NotifSoundAcousticGuitarCheckbox!!.isChecked = false
                settings_NotifSoundGravityCheckbox!!.isChecked = false
                settings_NotifSoundSlowDancingCheckbox!!.isChecked = false
                settings_NotifSoundScorpionThemeCheckbox!!.isChecked = false
                settings_NotifSoundFirstStepCheckbox!!.isChecked = false
                settings_NotifSoundRelaxToneCheckbox!!.isChecked = false
                settings_NotifsoundRelaxUploadSoundPath.setText("")
            }
            R.raw.freddie_freeloader -> {
                //settings_NotifNoSoundCheckbox!!.isChecked = false
                //settings_NotifSoundKnockinCheckbox!!.isChecked = false
                //settings_NotifSoundXyloCheckbox!!.isChecked = false

                settings_NotifSoundMoaninCheckbox!!.isChecked = false
                settings_NotifSoundBlueBossaCheckbox!!.isChecked = false
                settings_NotifSoundCaravanCheckbox!!.isChecked = false
                settings_NotifSoundDolphinDanceCheckbox!!.isChecked = false
                settings_NotifSoundAutumnLeavesCheckbox!!.isChecked = false
                settings_NotifSoundFreddieFreeloaderCheckbox!!.isChecked = true
                settings_NotifsoundJazzyUploadSoundPath?.setText("")

                settings_NotifSoundSlapCheckbox!!.isChecked = false
                settings_NotifSoundOffTheCurveCheckbox!!.isChecked = false
                settings_NotifSoundFunkYallCheckbox!!.isChecked = false
                settings_NotifSoundKeyboardFunkyToneCheckbox!!.isChecked = false
                settings_NotifSoundUCantHoldNoGrooveCheckbox!!.isChecked = false
                settings_NotifSoundColdSweatCheckbox!!.isChecked = false
                settings_NotifsoundFunkyUploadSoundPath?.setText("")

                settings_NotifSoundAcousticGuitarCheckbox!!.isChecked = false
                settings_NotifSoundGravityCheckbox!!.isChecked = false
                settings_NotifSoundSlowDancingCheckbox!!.isChecked = false
                settings_NotifSoundScorpionThemeCheckbox!!.isChecked = false
                settings_NotifSoundFirstStepCheckbox!!.isChecked = false
                settings_NotifSoundRelaxToneCheckbox!!.isChecked = false
                settings_NotifsoundRelaxUploadSoundPath.setText("")
            }
            R.raw.bass_slap -> {

                //settings_NotifNoSoundCheckbox!!.isChecked = false
                //settings_NotifSoundKnockinCheckbox!!.isChecked = false
                //settings_NotifSoundXyloCheckbox!!.isChecked = false

                settings_NotifSoundMoaninCheckbox!!.isChecked = false
                settings_NotifSoundBlueBossaCheckbox!!.isChecked = false
                settings_NotifSoundCaravanCheckbox!!.isChecked = false
                settings_NotifSoundDolphinDanceCheckbox!!.isChecked = false
                settings_NotifSoundAutumnLeavesCheckbox!!.isChecked = false
                settings_NotifSoundFreddieFreeloaderCheckbox!!.isChecked = false
                settings_NotifsoundJazzyUploadSoundPath?.setText("")

                settings_NotifSoundSlapCheckbox!!.isChecked = true
                settings_NotifSoundOffTheCurveCheckbox!!.isChecked = false
                settings_NotifSoundFunkYallCheckbox!!.isChecked = false
                settings_NotifSoundKeyboardFunkyToneCheckbox!!.isChecked = false
                settings_NotifSoundUCantHoldNoGrooveCheckbox!!.isChecked = false
                settings_NotifSoundColdSweatCheckbox!!.isChecked = false
                settings_NotifsoundFunkyUploadSoundPath?.setText("")

                settings_NotifSoundAcousticGuitarCheckbox!!.isChecked = false
                settings_NotifSoundGravityCheckbox!!.isChecked = false
                settings_NotifSoundSlowDancingCheckbox!!.isChecked = false
                settings_NotifSoundScorpionThemeCheckbox!!.isChecked = false
                settings_NotifSoundFirstStepCheckbox!!.isChecked = false
                settings_NotifSoundRelaxToneCheckbox!!.isChecked = false
                settings_NotifsoundRelaxUploadSoundPath.setText("")
            }
            R.raw.funk_yall -> {

                //settings_NotifNoSoundCheckbox!!.isChecked = false
                //settings_NotifSoundKnockinCheckbox!!.isChecked = false
                //settings_NotifSoundXyloCheckbox!!.isChecked = false

                settings_NotifSoundMoaninCheckbox!!.isChecked = false
                settings_NotifSoundBlueBossaCheckbox!!.isChecked = false
                settings_NotifSoundCaravanCheckbox!!.isChecked = false
                settings_NotifSoundDolphinDanceCheckbox!!.isChecked = false
                settings_NotifSoundAutumnLeavesCheckbox!!.isChecked = false
                settings_NotifSoundFreddieFreeloaderCheckbox!!.isChecked = false
                settings_NotifsoundJazzyUploadSoundPath?.setText("")

                settings_NotifSoundSlapCheckbox!!.isChecked = false
                settings_NotifSoundOffTheCurveCheckbox!!.isChecked = false
                settings_NotifSoundFunkYallCheckbox!!.isChecked = true
                settings_NotifSoundKeyboardFunkyToneCheckbox!!.isChecked = false
                settings_NotifSoundUCantHoldNoGrooveCheckbox!!.isChecked = false
                settings_NotifSoundColdSweatCheckbox!!.isChecked = false
                settings_NotifsoundFunkyUploadSoundPath?.setText("")

                settings_NotifSoundAcousticGuitarCheckbox!!.isChecked = false
                settings_NotifSoundGravityCheckbox!!.isChecked = false
                settings_NotifSoundSlowDancingCheckbox!!.isChecked = false
                settings_NotifSoundScorpionThemeCheckbox!!.isChecked = false
                settings_NotifSoundFirstStepCheckbox!!.isChecked = false
                settings_NotifSoundRelaxToneCheckbox!!.isChecked = false
                settings_NotifsoundRelaxUploadSoundPath.setText("")
            }
            R.raw.off_the_curve_groove -> {

                //settings_NotifNoSoundCheckbox!!.isChecked = false
                //settings_NotifSoundKnockinCheckbox!!.isChecked = false
                //settings_NotifSoundXyloCheckbox!!.isChecked = false

                settings_NotifSoundMoaninCheckbox!!.isChecked = false
                settings_NotifSoundBlueBossaCheckbox!!.isChecked = false
                settings_NotifSoundCaravanCheckbox!!.isChecked = false
                settings_NotifSoundDolphinDanceCheckbox!!.isChecked = false
                settings_NotifSoundAutumnLeavesCheckbox!!.isChecked = false
                settings_NotifSoundFreddieFreeloaderCheckbox!!.isChecked = false
                settings_NotifsoundJazzyUploadSoundPath?.setText("")

                settings_NotifSoundSlapCheckbox!!.isChecked = false
                settings_NotifSoundOffTheCurveCheckbox!!.isChecked = true
                settings_NotifSoundFunkYallCheckbox!!.isChecked = false
                settings_NotifSoundKeyboardFunkyToneCheckbox!!.isChecked = false
                settings_NotifSoundUCantHoldNoGrooveCheckbox!!.isChecked = false
                settings_NotifSoundColdSweatCheckbox!!.isChecked = false
                settings_NotifsoundFunkyUploadSoundPath?.setText("")

                settings_NotifSoundAcousticGuitarCheckbox!!.isChecked = false
                settings_NotifSoundGravityCheckbox!!.isChecked = false
                settings_NotifSoundSlowDancingCheckbox!!.isChecked = false
                settings_NotifSoundScorpionThemeCheckbox!!.isChecked = false
                settings_NotifSoundFirstStepCheckbox!!.isChecked = false
                settings_NotifSoundRelaxToneCheckbox!!.isChecked = false
                settings_NotifsoundRelaxUploadSoundPath.setText("")
            }
            R.raw.keyboard_funky_tone -> {

                //settings_NotifNoSoundCheckbox!!.isChecked = false
                //settings_NotifSoundKnockinCheckbox!!.isChecked = false
                //settings_NotifSoundXyloCheckbox!!.isChecked = false

                settings_NotifSoundMoaninCheckbox!!.isChecked = false
                settings_NotifSoundBlueBossaCheckbox!!.isChecked = false
                settings_NotifSoundCaravanCheckbox!!.isChecked = false
                settings_NotifSoundDolphinDanceCheckbox!!.isChecked = false
                settings_NotifSoundAutumnLeavesCheckbox!!.isChecked = false
                settings_NotifSoundFreddieFreeloaderCheckbox!!.isChecked = false
                settings_NotifsoundJazzyUploadSoundPath?.setText("")

                settings_NotifSoundSlapCheckbox!!.isChecked = false
                settings_NotifSoundOffTheCurveCheckbox!!.isChecked = false
                settings_NotifSoundFunkYallCheckbox!!.isChecked = false
                settings_NotifSoundKeyboardFunkyToneCheckbox!!.isChecked = true
                settings_NotifSoundUCantHoldNoGrooveCheckbox!!.isChecked = false
                settings_NotifSoundColdSweatCheckbox!!.isChecked = false
                settings_NotifsoundFunkyUploadSoundPath?.setText("")

                settings_NotifSoundAcousticGuitarCheckbox!!.isChecked = false
                settings_NotifSoundGravityCheckbox!!.isChecked = false
                settings_NotifSoundSlowDancingCheckbox!!.isChecked = false
                settings_NotifSoundScorpionThemeCheckbox!!.isChecked = false
                settings_NotifSoundFirstStepCheckbox!!.isChecked = false
                settings_NotifSoundRelaxToneCheckbox!!.isChecked = false
                settings_NotifsoundRelaxUploadSoundPath.setText("")
            }
            R.raw.u_cant_hold_no_groove -> {

                //settings_NotifNoSoundCheckbox!!.isChecked = false
                //settings_NotifSoundKnockinCheckbox!!.isChecked = false
                //settings_NotifSoundXyloCheckbox!!.isChecked = false

                settings_NotifSoundMoaninCheckbox!!.isChecked = false
                settings_NotifSoundBlueBossaCheckbox!!.isChecked = false
                settings_NotifSoundCaravanCheckbox!!.isChecked = false
                settings_NotifSoundDolphinDanceCheckbox!!.isChecked = false
                settings_NotifSoundAutumnLeavesCheckbox!!.isChecked = false
                settings_NotifSoundFreddieFreeloaderCheckbox!!.isChecked = false
                settings_NotifsoundJazzyUploadSoundPath?.setText("")

                settings_NotifSoundSlapCheckbox!!.isChecked = false
                settings_NotifSoundOffTheCurveCheckbox!!.isChecked = false
                settings_NotifSoundFunkYallCheckbox!!.isChecked = false
                settings_NotifSoundKeyboardFunkyToneCheckbox!!.isChecked = false
                settings_NotifSoundUCantHoldNoGrooveCheckbox!!.isChecked = true
                settings_NotifSoundColdSweatCheckbox!!.isChecked = false
                settings_NotifsoundFunkyUploadSoundPath?.setText("")

                settings_NotifSoundAcousticGuitarCheckbox!!.isChecked = false
                settings_NotifSoundGravityCheckbox!!.isChecked = false
                settings_NotifSoundSlowDancingCheckbox!!.isChecked = false
                settings_NotifSoundScorpionThemeCheckbox!!.isChecked = false
                settings_NotifSoundFirstStepCheckbox!!.isChecked = false
                settings_NotifSoundRelaxToneCheckbox!!.isChecked = false
                settings_NotifsoundRelaxUploadSoundPath.setText("")
            }
            R.raw.cold_sweat -> {

                //settings_NotifNoSoundCheckbox!!.isChecked = false
                //settings_NotifSoundKnockinCheckbox!!.isChecked = false
                //settings_NotifSoundXyloCheckbox!!.isChecked = false

                settings_NotifSoundMoaninCheckbox!!.isChecked = false
                settings_NotifSoundBlueBossaCheckbox!!.isChecked = false
                settings_NotifSoundCaravanCheckbox!!.isChecked = false
                settings_NotifSoundDolphinDanceCheckbox!!.isChecked = false
                settings_NotifSoundAutumnLeavesCheckbox!!.isChecked = false
                settings_NotifSoundFreddieFreeloaderCheckbox!!.isChecked = false
                settings_NotifsoundJazzyUploadSoundPath?.setText("")

                settings_NotifSoundSlapCheckbox!!.isChecked = false
                settings_NotifSoundOffTheCurveCheckbox!!.isChecked = false
                settings_NotifSoundFunkYallCheckbox!!.isChecked = false
                settings_NotifSoundKeyboardFunkyToneCheckbox!!.isChecked = false
                settings_NotifSoundUCantHoldNoGrooveCheckbox!!.isChecked = false
                settings_NotifSoundColdSweatCheckbox!!.isChecked = true
                settings_NotifsoundFunkyUploadSoundPath?.setText("")

                settings_NotifSoundAcousticGuitarCheckbox!!.isChecked = false
                settings_NotifSoundGravityCheckbox!!.isChecked = false
                settings_NotifSoundSlowDancingCheckbox!!.isChecked = false
                settings_NotifSoundScorpionThemeCheckbox!!.isChecked = false
                settings_NotifSoundFirstStepCheckbox!!.isChecked = false
                settings_NotifSoundRelaxToneCheckbox!!.isChecked = false
                settings_NotifsoundRelaxUploadSoundPath.setText("")
            }
            R.raw.beautiful_chords_progression -> {

                //settings_NotifNoSoundCheckbox!!.isChecked = false
                //settings_NotifSoundKnockinCheckbox!!.isChecked = false
                //settings_NotifSoundXyloCheckbox!!.isChecked = false

                settings_NotifSoundMoaninCheckbox!!.isChecked = false
                settings_NotifSoundBlueBossaCheckbox!!.isChecked = false
                settings_NotifSoundCaravanCheckbox!!.isChecked = false
                settings_NotifSoundDolphinDanceCheckbox!!.isChecked = false
                settings_NotifSoundAutumnLeavesCheckbox!!.isChecked = false
                settings_NotifSoundFreddieFreeloaderCheckbox!!.isChecked = false
                settings_NotifsoundJazzyUploadSoundPath?.setText("")

                settings_NotifSoundSlapCheckbox!!.isChecked = false
                settings_NotifSoundOffTheCurveCheckbox!!.isChecked = false
                settings_NotifSoundFunkYallCheckbox!!.isChecked = false
                settings_NotifSoundKeyboardFunkyToneCheckbox!!.isChecked = false
                settings_NotifSoundUCantHoldNoGrooveCheckbox!!.isChecked = false
                settings_NotifSoundColdSweatCheckbox!!.isChecked = false
                settings_NotifsoundFunkyUploadSoundPath?.setText("")

                settings_NotifSoundAcousticGuitarCheckbox!!.isChecked = true
                settings_NotifSoundGravityCheckbox!!.isChecked = false
                settings_NotifSoundSlowDancingCheckbox!!.isChecked = false
                settings_NotifSoundScorpionThemeCheckbox!!.isChecked = false
                settings_NotifSoundFirstStepCheckbox!!.isChecked = false
                settings_NotifSoundRelaxToneCheckbox!!.isChecked = false
                settings_NotifsoundRelaxUploadSoundPath.setText("")
            }
            R.raw.gravity -> {

                //settings_NotifNoSoundCheckbox!!.isChecked = false
                //settings_NotifSoundKnockinCheckbox!!.isChecked = false
                //settings_NotifSoundXyloCheckbox!!.isChecked = false

                settings_NotifSoundMoaninCheckbox!!.isChecked = false
                settings_NotifSoundBlueBossaCheckbox!!.isChecked = false
                settings_NotifSoundCaravanCheckbox!!.isChecked = false
                settings_NotifSoundDolphinDanceCheckbox!!.isChecked = false
                settings_NotifSoundAutumnLeavesCheckbox!!.isChecked = false
                settings_NotifSoundFreddieFreeloaderCheckbox!!.isChecked = false
                settings_NotifsoundJazzyUploadSoundPath?.setText("")

                settings_NotifSoundSlapCheckbox!!.isChecked = false
                settings_NotifSoundOffTheCurveCheckbox!!.isChecked = false
                settings_NotifSoundFunkYallCheckbox!!.isChecked = false
                settings_NotifSoundKeyboardFunkyToneCheckbox!!.isChecked = false
                settings_NotifSoundUCantHoldNoGrooveCheckbox!!.isChecked = false
                settings_NotifSoundColdSweatCheckbox!!.isChecked = false
                settings_NotifsoundFunkyUploadSoundPath?.setText("")

                settings_NotifSoundAcousticGuitarCheckbox!!.isChecked = false
                settings_NotifSoundGravityCheckbox!!.isChecked = true
                settings_NotifSoundSlowDancingCheckbox!!.isChecked = false
                settings_NotifSoundScorpionThemeCheckbox!!.isChecked = false
                settings_NotifSoundFirstStepCheckbox!!.isChecked = false
                settings_NotifSoundRelaxToneCheckbox!!.isChecked = false
                settings_NotifsoundRelaxUploadSoundPath.setText("")
            }
            R.raw.fade_to_black -> {

                //settings_NotifNoSoundCheckbox!!.isChecked = false
                //settings_NotifSoundKnockinCheckbox!!.isChecked = false
                //settings_NotifSoundXyloCheckbox!!.isChecked = false

                settings_NotifSoundMoaninCheckbox!!.isChecked = false
                settings_NotifSoundBlueBossaCheckbox!!.isChecked = false
                settings_NotifSoundCaravanCheckbox!!.isChecked = false
                settings_NotifSoundDolphinDanceCheckbox!!.isChecked = false
                settings_NotifSoundAutumnLeavesCheckbox!!.isChecked = false
                settings_NotifSoundFreddieFreeloaderCheckbox!!.isChecked = false
                settings_NotifsoundJazzyUploadSoundPath?.setText("")

                settings_NotifSoundSlapCheckbox!!.isChecked = false
                settings_NotifSoundOffTheCurveCheckbox!!.isChecked = false
                settings_NotifSoundFunkYallCheckbox!!.isChecked = false
                settings_NotifSoundKeyboardFunkyToneCheckbox!!.isChecked = false
                settings_NotifSoundUCantHoldNoGrooveCheckbox!!.isChecked = false
                settings_NotifSoundColdSweatCheckbox!!.isChecked = false
                settings_NotifsoundFunkyUploadSoundPath?.setText("")

                settings_NotifSoundAcousticGuitarCheckbox!!.isChecked = false
                settings_NotifSoundGravityCheckbox!!.isChecked = false
                settings_NotifSoundSlowDancingCheckbox!!.isChecked = false
                settings_NotifSoundScorpionThemeCheckbox!!.isChecked = true
                settings_NotifSoundFirstStepCheckbox!!.isChecked = false
                settings_NotifSoundRelaxToneCheckbox!!.isChecked = false
                settings_NotifsoundRelaxUploadSoundPath.setText("")
            }
            R.raw.slow_dancing -> {

                //settings_NotifNoSoundCheckbox!!.isChecked = false
                //settings_NotifSoundKnockinCheckbox!!.isChecked = false
                //settings_NotifSoundXyloCheckbox!!.isChecked = false

                settings_NotifSoundMoaninCheckbox!!.isChecked = false
                settings_NotifSoundBlueBossaCheckbox!!.isChecked = false
                settings_NotifSoundCaravanCheckbox!!.isChecked = false
                settings_NotifSoundDolphinDanceCheckbox!!.isChecked = false
                settings_NotifSoundAutumnLeavesCheckbox!!.isChecked = false
                settings_NotifSoundFreddieFreeloaderCheckbox!!.isChecked = false
                settings_NotifsoundJazzyUploadSoundPath?.setText("")

                settings_NotifSoundSlapCheckbox!!.isChecked = false
                settings_NotifSoundOffTheCurveCheckbox!!.isChecked = false
                settings_NotifSoundFunkYallCheckbox!!.isChecked = false
                settings_NotifSoundKeyboardFunkyToneCheckbox!!.isChecked = false
                settings_NotifSoundUCantHoldNoGrooveCheckbox!!.isChecked = false
                settings_NotifSoundColdSweatCheckbox!!.isChecked = false
                settings_NotifsoundFunkyUploadSoundPath?.setText("")

                settings_NotifSoundAcousticGuitarCheckbox!!.isChecked = false
                settings_NotifSoundGravityCheckbox!!.isChecked = false
                settings_NotifSoundSlowDancingCheckbox!!.isChecked = true
                settings_NotifSoundScorpionThemeCheckbox!!.isChecked = false
                settings_NotifSoundFirstStepCheckbox!!.isChecked = false
                settings_NotifSoundRelaxToneCheckbox!!.isChecked = false
                settings_NotifsoundRelaxUploadSoundPath.setText("")
            }
            R.raw.relax_sms -> {

                //settings_NotifNoSoundCheckbox!!.isChecked = false
                //settings_NotifSoundKnockinCheckbox!!.isChecked = false
                //settings_NotifSoundXyloCheckbox!!.isChecked = false

                settings_NotifSoundMoaninCheckbox!!.isChecked = false
                settings_NotifSoundBlueBossaCheckbox!!.isChecked = false
                settings_NotifSoundCaravanCheckbox!!.isChecked = false
                settings_NotifSoundDolphinDanceCheckbox!!.isChecked = false
                settings_NotifSoundAutumnLeavesCheckbox!!.isChecked = false
                settings_NotifSoundFreddieFreeloaderCheckbox!!.isChecked = false
                settings_NotifsoundJazzyUploadSoundPath?.setText("")

                settings_NotifSoundSlapCheckbox!!.isChecked = false
                settings_NotifSoundOffTheCurveCheckbox!!.isChecked = true
                settings_NotifSoundFunkYallCheckbox!!.isChecked = false
                settings_NotifSoundKeyboardFunkyToneCheckbox!!.isChecked = false
                settings_NotifSoundUCantHoldNoGrooveCheckbox!!.isChecked = false
                settings_NotifSoundColdSweatCheckbox!!.isChecked = false
                settings_NotifsoundFunkyUploadSoundPath?.setText("")

                settings_NotifSoundAcousticGuitarCheckbox!!.isChecked = false
                settings_NotifSoundGravityCheckbox!!.isChecked = false
                settings_NotifSoundSlowDancingCheckbox!!.isChecked = false
                settings_NotifSoundScorpionThemeCheckbox!!.isChecked = false
                settings_NotifSoundFirstStepCheckbox!!.isChecked = false
                settings_NotifSoundRelaxToneCheckbox!!.isChecked = true
                settings_NotifsoundRelaxUploadSoundPath.setText("")
            }
            R.raw.interstellar_main_theme -> {

                //settings_NotifNoSoundCheckbox!!.isChecked = false
                //settings_NotifSoundKnockinCheckbox!!.isChecked = false
                //settings_NotifSoundXyloCheckbox!!.isChecked = false

                settings_NotifSoundMoaninCheckbox!!.isChecked = false
                settings_NotifSoundBlueBossaCheckbox!!.isChecked = false
                settings_NotifSoundCaravanCheckbox!!.isChecked = false
                settings_NotifSoundDolphinDanceCheckbox!!.isChecked = false
                settings_NotifSoundAutumnLeavesCheckbox!!.isChecked = false
                settings_NotifSoundFreddieFreeloaderCheckbox!!.isChecked = false
                settings_NotifsoundJazzyUploadSoundPath?.setText("")

                settings_NotifSoundSlapCheckbox!!.isChecked = false
                settings_NotifSoundOffTheCurveCheckbox!!.isChecked = false
                settings_NotifSoundFunkYallCheckbox!!.isChecked = false
                settings_NotifSoundKeyboardFunkyToneCheckbox!!.isChecked = false
                settings_NotifSoundUCantHoldNoGrooveCheckbox!!.isChecked = false
                settings_NotifSoundColdSweatCheckbox!!.isChecked = false
                settings_NotifsoundFunkyUploadSoundPath?.setText("")

                settings_NotifSoundAcousticGuitarCheckbox!!.isChecked = false
                settings_NotifSoundGravityCheckbox!!.isChecked = false
                settings_NotifSoundSlowDancingCheckbox!!.isChecked = false
                settings_NotifSoundScorpionThemeCheckbox!!.isChecked = false
                settings_NotifSoundFirstStepCheckbox!!.isChecked = true
                settings_NotifSoundRelaxToneCheckbox!!.isChecked = false
                settings_NotifsoundRelaxUploadSoundPath.setText("")
            }
        }
    }

    fun ringToneLayoutClosed() {
        //region OpenClose

        //settings_NotifNoSoundCheckbox!!.isChecked = false
        //settings_NotifSoundKnockinCheckbox!!.isChecked = false
        //settings_NotifSoundXyloCheckbox!!.isChecked = false

        //settings_ChooseNotifDefaultSoundImageOpen!!.visibility = View.GONE
        //settings_ChooseNotifDefaultSoundImageClose!!.visibility = View.VISIBLE

        settings_NotifSoundMoaninLayout!!.visibility = View.GONE
        settings_NotifSoundBlueBossaLayout!!.visibility = View.GONE
        settings_NotifSoundCaravanLayout!!.visibility = View.GONE
        settings_NotifSoundDolphinDanceLayout!!.visibility = View.GONE
        settings_NotifSoundAutumnLeavesLayout!!.visibility = View.GONE
        settings_NotifSoundFreddieFreeloaderLayout!!.visibility = View.GONE

        settings_ChooseNotifJazzySoundImageOpen!!.visibility = View.GONE
        settings_ChooseNotifJazzySoundImageClose!!.visibility = View.VISIBLE

        settings_NotifSoundSlapLayout!!.visibility = View.GONE
        settings_NotifSoundOffTheCurveLayout!!.visibility = View.GONE
        settings_NotifSoundFunkYallLayout!!.visibility = View.GONE
        settings_NotifSoundKeyboardFunkyToneLayout!!.visibility = View.GONE
        settings_NotifSoundUCantHoldNoGrooveLayout!!.visibility = View.GONE
        settings_NotifSoundColdSweatLayout!!.visibility = View.GONE

        settings_ChooseNotifFunkySoundImageOpen!!.visibility = View.GONE
        settings_ChooseNotifFunkySoundImageClose!!.visibility = View.VISIBLE

        settings_NotifSoundAcousticGuitarLayout!!.visibility = View.GONE
        settings_NotifSoundGravityLayout!!.visibility = View.GONE
        settings_NotifSoundSlowDancingLayout!!.visibility = View.GONE
        settings_NotifSoundScorpionThemeLayout!!.visibility = View.GONE
        settings_NotifSoundFirstStepLayout!!.visibility = View.GONE
        settings_NotifSoundRelaxToneLayout!!.visibility = View.GONE

        settings_ChooseNotifRelaxationSoundImageOpen!!.visibility = View.GONE
        settings_ChooseNotifRelaxationSoundImageClose!!.visibility = View.VISIBLE

        //endregion
    }

    fun goToPremiumActivity() {
        startActivity(
            Intent(
                this@EditContactVipSettingsActivity,
                PremiumActivity::class.java
            ).putExtra("fromManageNotification", true)
        )
        finish()
    }

    override fun onRestart() {
        super.onRestart()
        if (editInAndroid) {
            if (fromGroupActivity) {
                startActivity(
                    Intent(
                        this@EditContactVipSettingsActivity,
                        GroupManagerActivity::class.java
                    ).putExtra("ContactId", edit_contact_id!!)
                )
                finish()
            } else {
                startActivity(
                    Intent(
                        this@EditContactVipSettingsActivity,
                        MainActivity::class.java
                    ).putExtra("ContactId", edit_contact_id!!).putExtra("position", position)
                )
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (editInGoogle) {
            if (fromGroupActivity) {
                startActivity(
                    Intent(
                        this@EditContactVipSettingsActivity,
                        GroupManagerActivity::class.java
                    ).putExtra("ContactId", edit_contact_id!!)
                )
                finish()
            } else {
                startActivity(
                    Intent(
                        this@EditContactVipSettingsActivity,
                        MainActivity::class.java
                    ).putExtra("ContactId", edit_contact_id!!).putExtra("position", position)
                )
                finish()
            }
        }
    }

    override fun onBackPressed() {
        if (isChanged || isFavoriteChanged != isFavorite) {
            MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                .setTitle(R.string.edit_contact_alert_dialog_cancel_title)
                .setMessage(R.string.edit_contact_alert_dialog_cancel_message)
                .setBackground(getDrawable(R.color.backgroundColor))
                .setPositiveButton(getString(R.string.alert_dialog_yes)) { _, _ ->
                    if (fromGroupActivity) {
                        startActivity(
                            Intent(
                                this@EditContactVipSettingsActivity,
                                GroupManagerActivity::class.java
                            )
                        )
                    } else {
                        startActivity(
                            Intent(
                                this@EditContactVipSettingsActivity,
                                MainActivity::class.java
                            ).putExtra("position", position)
                        )
                    }
                    finish()
                }
                .setNegativeButton(getString(R.string.alert_dialog_no)) { _, _ ->
                }
                .show()
        } else {
            if (fromGroupActivity) {
                startActivity(
                    Intent(
                        this@EditContactVipSettingsActivity,
                        GroupManagerActivity::class.java
                    )
                )
            } else {
                startActivity(
                    Intent(
                        this@EditContactVipSettingsActivity,
                        MainActivity::class.java
                    ).putExtra("position", position)
                )
            }
            finish()
        }
        hideKeyboard()
    }

    private fun hideKeyboard() {
        // Check if no view has focus:
        val view = this.currentFocus

        view?.let { v ->
            val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }

    private fun getPosItemSpinner(item: String, spinner: Spinner): Int {
        val tailleSpinner: Int = spinner.adapter.count
        //println("taille spinner" + tailleSpinner)
        for (x in 0 until tailleSpinner) {
            if (spinner.getItemAtPosition(x)
                    .toString() == NumberAndMailDB.convertStringToSpinnerString(item, this)
            ) {
                return x
            } else {
                println(
                    spinner.getItemAtPosition(x)
                        .toString() + "est diférent de " + NumberAndMailDB.convertStringToSpinnerString(
                        item,
                        this
                    )
                )
            }
        }
        return 0
    }

    //region ========================================== Favorites ===========================================

    /* private fun addToFavorite() {
         val contact = edit_contact_ContactsDatabase?.contactsDao()?.getContact(edit_contact_id!!)

         contact!!.setIsFavorite(edit_contact_ContactsDatabase)

         var counter = 0
         var alreadyExist = false

         while (counter < edit_contact_ContactsDatabase?.GroupsDao()!!.getAllGroupsByNameAZ().size) {
             if (edit_contact_ContactsDatabase?.GroupsDao()!!.getAllGroupsByNameAZ()[counter].groupDB!!.name == "Favorites") {
                 groupId = edit_contact_ContactsDatabase?.GroupsDao()!!.getAllGroupsByNameAZ()[counter].groupDB!!.id!!
                 alreadyExist = true
                 break
             }
             counter++
         }

         listContact.add(contact.contactDB)

         if (alreadyExist) {
             addContactToGroup(listContact, groupId)
         } else {
             createGroup(listContact, "Favorites")
         }
     }
 */

    /* private fun removeFromFavorite() {
        val contact = edit_contact_ContactsDatabase?.contactsDao()?.getContact(edit_contact_id!!)

        contact!!.setIsNotFavorite(edit_contact_ContactsDatabase)

        var counter = 0

        while (counter < edit_contact_ContactsDatabase?.GroupsDao()!!.getAllGroupsByNameAZ().size) {
            if (edit_contact_ContactsDatabase?.GroupsDao()!!.getAllGroupsByNameAZ()[counter].groupDB!!.name == "Favorites") {
                groupId = edit_contact_ContactsDatabase?.GroupsDao()!!.getAllGroupsByNameAZ()[counter].groupDB!!.id!!
                break
            }
            counter++
        }

        listContact.remove(contact.contactDB)

        removeContactFromGroup(edit_contact_id!!, groupId)

        counter = 0

        while (counter < edit_contact_ContactsDatabase?.GroupsDao()!!.getAllGroupsByNameAZ().size) {
            if (edit_contact_ContactsDatabase?.GroupsDao()!!.getAllGroupsByNameAZ()[counter].getListContact(this).isEmpty()) {
                edit_contact_ContactsDatabase?.GroupsDao()!!.deleteGroupById(edit_contact_ContactsDatabase?.GroupsDao()!!.getAllGroupsByNameAZ()[counter].groupDB!!.id!!.toInt())
                break
            }
            counter++
        }
    }
    */

    //endregion

    private fun getRealPathFromUri(context: Context, contentUri: Uri): String {
        var cursor: Cursor? = null
        try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = context.contentResolver.query(contentUri, proj, null, null, null)
            val columnIndex = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            return cursor.getString(columnIndex)
        } finally {
            cursor?.close()
        }
    }


    private fun exifToDegrees(exifOrientation: Int): Int {
        return when (exifOrientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    }

    private fun Bitmap.bitmapToBase64(): String {
        val baos = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.PNG, 100, baos)
        val imageBytes = baos.toByteArray()

        return Base64.encodeToString(imageBytes, Base64.DEFAULT)
    }

    private fun base64ToBitmap(base64: String): Bitmap {
        val imageBytes = Base64.decode(base64, 0)
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    //TODO: modifier l'alert dialog en ajoutant une vue pour le rendre joli.
    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(Build.VERSION_CODES.M)
    private fun overlayAlertDialog(): AlertDialog? {

        return MaterialAlertDialogBuilder(this, R.style.AlertDialog)
            .setTitle(R.string.alert_dialog_overlay_title)
            .setBackground(getDrawable(R.color.backgroundColor))
            .setMessage(this.resources.getString(R.string.alert_dialog_overlay_message))
            .setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
                val intentPermission = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intentPermission)
                val sharedPreferences =
                    getSharedPreferences("Knockin_preferences", Context.MODE_PRIVATE)
                val edit: SharedPreferences.Editor = sharedPreferences.edit()
                edit.putBoolean(
                    "popupNotif",
                    true
                )//quand la personne autorise l'affichage par dessus d'autre application nous l'enregistrons
                edit.putBoolean("serviceNotif", false)
                edit.apply()
            }
            .setNegativeButton(R.string.alert_dialog_no)
            { _, _ ->
                val sharedPreferences =
                    getSharedPreferences("Knockin_preferences", Context.MODE_PRIVATE)
                val edit: SharedPreferences.Editor = sharedPreferences.edit()
                edit.putBoolean(
                    "popupNotif",
                    false
                )//quand la personne autorise l'affichage par dessus d'autre application nous l'enregistrons
                edit.putBoolean("serviceNotif", true)
                edit.apply()
            }
            .show()
    }

    fun addContactIcone(bitmap: Bitmap) {
        //  add_new_contact_ImgString

        edit_contact_RoundedImageView!!.setImageBitmap(bitmap)
        edit_contact_imgString = bitmap.bitmapToBase64()
        edit_contact_imgStringChanged = true
    }


    //endregion
}