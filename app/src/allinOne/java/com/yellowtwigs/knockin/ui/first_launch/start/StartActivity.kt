package com.yellowtwigs.knockin.ui.first_launch.start

import android.Manifest
import android.content.*
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.android.billingclient.api.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.ActivityStartActivityBinding
import com.yellowtwigs.knockin.ui.contacts.list.ContactsListActivity
import com.yellowtwigs.knockin.ui.first_launch.first_vip_selection.FirstVipSelectionActivity
import com.yellowtwigs.knockin.utils.EveryActivityUtils.checkIfGoEdition
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.abs

@AndroidEntryPoint
class StartActivity : AppCompatActivity(), PurchasesUpdatedListener {

    //region ========================================== Val or Var ==========================================

    private var currentPosition = 0
    private lateinit var binding: ActivityStartActivityBinding
    private val importContactsViewModel: ImportContactsViewModel by viewModels()

    private lateinit var importContactPreferences: SharedPreferences

    private var contactsAreImported = false

    private lateinit var sharedPreferences: SharedPreferences

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions[Manifest.permission.WRITE_CONTACTS] == true && permissions[Manifest.permission.READ_CONTACTS] == true) {
            CoroutineScope(Dispatchers.Main).launch {
                importContactsViewModel.syncAllContactsInDatabase(contentResolver)
            }

            val edit = importContactPreferences.edit()
            edit.putBoolean("Import_Contact", true)
            edit.apply()

            if (checkIfGoEdition(this@StartActivity)) {
                firstLaunchValidate()
                val edit = sharedPreferences.edit()
                edit.putBoolean("view", true)
                edit.apply()
                val intent = Intent(this@StartActivity, ContactsListActivity::class.java)
                intent.putExtra("fromStartActivity", true)
                startActivity(intent)
                finish()
            } else {
                checkRadioButton(binding.radioButton2.id)
                binding.viewPager.currentItem = 1
                contactsAreImported = true
            }

            readPhoneStateDialog()
        }
    }

    private val requestPermissionLauncher2 = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions[Manifest.permission.READ_PHONE_STATE] == true && permissions[Manifest.permission.CALL_PHONE] == true) {
        }
    }

    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStartActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT

        setupBillingClient()

        val calendar = Calendar.getInstance()
        val oneWeekSharedPreferences = getSharedPreferences("OneWeek", Context.MODE_PRIVATE)
        val edit = oneWeekSharedPreferences.edit()

        edit.putStringSet(
            "OneWeek", setOf(
                "DAY :${calendar.get(Calendar.DAY_OF_MONTH)}", // 29
                "MONTH :${calendar.get(Calendar.MONTH)}", // 11
                "YEAR :${calendar.get(Calendar.YEAR)}" // 2022
            )
        )
        edit.apply()
        sharedPreferences = getSharedPreferences("Knockin_preferences", Context.MODE_PRIVATE)
        val firstTime = getSharedPreferences("FirstTimeInTheApp", Context.MODE_PRIVATE)
        val editFirstTime = firstTime.edit()
        editFirstTime.putBoolean("FirstTimeInTheApp", true)
        editFirstTime.apply()

        importContactPreferences = getSharedPreferences("Import_Contact", Context.MODE_PRIVATE)
        contactsAreImported = importContactPreferences.getBoolean("Import_Contact", false)
        if (isNotificationServiceEnabled()) {
            val edit: SharedPreferences.Editor = sharedPreferences.edit()
            edit.putBoolean("serviceNotif", true)
            edit.apply()
        }
        if (Settings.canDrawOverlays(this)) {
            val edit: SharedPreferences.Editor = sharedPreferences.edit()
            edit.putBoolean("popupNotif", true)
            edit.apply()
        }

        if (checkIfGoEdition(this@StartActivity)) {
            MaterialAlertDialogBuilder(this, R.style.AlertDialog).setBackground(getDrawable(R.color.backgroundColor))
                .setMessage(getString(R.string.start_activity_go_edition_message))
                .setPositiveButton(R.string.start_activity_go_edition_positive_button) { _, _ ->
                }.show()
            setSliderContainerWithGoEdichie()

            binding.apply {
                activateButton.setOnClickListener {
                    when (currentPosition) {
                        0 -> {
                            if (!importContactPreferences.getBoolean("Import_Contact", false)) {
                                requestPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CONTACTS
                                    )
                                )
                            }
                        }
                    }
                }

                radioButton2.setOnClickListener {
                    binding.viewPager.currentItem = 0
                    checkRadioButton(radioButton2.id)
                }
                radioButton3.setOnClickListener {
                    binding.viewPager.currentItem = 1
                    checkRadioButton(radioButton3.id)
                }
            }
        } else {
            setSliderContainer()

            binding.apply {
                activateButton.setOnClickListener {
                    when (currentPosition) {
                        0 -> {
                            if (!importContactPreferences.getBoolean("Import_Contact", false)) {
                                requestPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.WRITE_CONTACTS,
                                        Manifest.permission.READ_CONTACTS,
                                    )
                                )
                            }
                        }

                        1 -> {
                            activateNotificationsClick()
                        }

                        2 -> {
                            openOverlaySettings()
                        }

                        3 -> {
                            if (checkIfGoEdition(this@StartActivity)) {
                                firstLaunchValidate()
                                val intent = Intent(this@StartActivity, ContactsListActivity::class.java)
                                intent.putExtra("fromStartActivity", true)
                                startActivity(intent)
                                finish()
                            }
                        }
                    }
                }
                next.setOnClickListener {
                    firstLaunchValidate()
                    startActivity(Intent(this@StartActivity, FirstVipSelectionActivity::class.java))
                    val edit = sharedPreferences.edit()
                    edit.putBoolean("view", true)
                    edit.apply()
                    finish()
                }
                skip.setOnClickListener {
                    firstLaunchValidate()
                    val edit = sharedPreferences.edit()
                    edit.putBoolean("view", true)
                    edit.apply()
                    val intent = Intent(this@StartActivity, ContactsListActivity::class.java)
                    intent.putExtra("fromStartActivity", true)
                    startActivity(intent)
                    finish()
                }

                radioButton1.setOnClickListener {
                    binding.viewPager.currentItem = 0
                    checkRadioButton(radioButton1.id)
                }
                radioButton2.setOnClickListener {
                    binding.viewPager.currentItem = 1
                    checkRadioButton(radioButton2.id)
                }
                radioButton3.setOnClickListener {
                    binding.viewPager.currentItem = 2
                    checkRadioButton(radioButton3.id)
                }
                radioButton4.setOnClickListener {
                    binding.viewPager.currentItem = 3
                    checkRadioButton(radioButton4.id)
                }
            }
        }
    }

    //region ========================================== Functions ==========================================
    private fun setSliderContainer() {
        val viewPager = findViewById<ViewPager2>(R.id.view_pager)
        val sliderItems = arrayListOf<SliderItem>()
        sliderItems.add(SliderItem(R.drawable.contacts_list))
        sliderItems.add(SliderItem(R.drawable.notif_history))
        sliderItems.add(SliderItem(R.drawable.vip_message))
        sliderItems.add(SliderItem(R.drawable.screen_lock_msg))

        val sliderAdapter = SliderAdapter(sliderItems)

        viewPager.apply {
            adapter = sliderAdapter
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 3
            getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER

            val compositePageTransformer = CompositePageTransformer()
            compositePageTransformer.addTransformer(MarginPageTransformer(30))
            compositePageTransformer.addTransformer { page, position ->
                val r = 1 - abs(position)
                page.scaleY = 0.85f + r * 0.25f
            }

            setPageTransformer(compositePageTransformer)

            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)

                    currentPosition = position

                    if (checkIfGoEdition(this@StartActivity)) {
                        currentPosition = 4
                    }

                    binding.apply {
                        when (currentPosition) {
                            0 -> {
                                title.text = getString(R.string.start_activity_import_title)
                                subtitle.text = getString(R.string.start_activity_import_subtitle)

                                checkRadioButton(radioButton1.id)

                                activateButton.visibility = View.VISIBLE
                                activateButtonIsClickable(!contactsAreImported)
                                activateButton.setText(R.string.start_activity_button_notification)
                                next.visibility = View.GONE
                                skip.visibility = View.GONE
                            }

                            1 -> {
                                title.text = getString(R.string.start_activity_notification_title)
                                subtitle.text = getString(R.string.start_activity_notification_subtitle)

                                checkRadioButton(radioButton2.id)

                                activateButton.visibility = View.VISIBLE
                                activateButtonIsClickable(!isNotificationServiceEnabled())
                                activateButton.setText(R.string.start_activity_button_notification)
                                next.visibility = View.GONE
                                skip.visibility = View.GONE
                            }

                            2 -> {
                                title.text = getString(R.string.superposition_title)
                                subtitle.text = getString(R.string.superposition_subtitle)

                                checkRadioButton(radioButton3.id)

                                activateButton.visibility = View.VISIBLE
                                activateButtonIsClickable(!Settings.canDrawOverlays(this@StartActivity))
                                activateButton.setText(R.string.start_activity_button_notification)
                                next.visibility = View.GONE
                                skip.visibility = View.GONE
                            }

                            3 -> {
                                if (checkIfGoEdition(this@StartActivity)) {
                                    radioButton1.visibility = View.GONE
                                    radioButton2.visibility = View.GONE
                                    radioButton3.visibility = View.GONE
                                    radioButton4.visibility = View.GONE

                                    title.text =
                                        "${getString(R.string.notif_adapter_show_message_button)} " + "${getString(R.string.left_drawer_home)}"

                                    subtitle.visibility = View.GONE
                                    activateButton.setText(R.string.start_activity_next)
                                } else {
                                    title.text = getString(R.string.notification_alert_dialog_title)
                                    subtitle.text = getString(R.string.notification_alert_dialog_message)

                                    checkRadioButton(radioButton4.id)

                                    activateButton.visibility = View.GONE
                                    next.visibility = View.VISIBLE
                                    skip.visibility = View.VISIBLE
                                }
                            }
                        }
                    }
                }
            })

            setCurrentItem(currentPosition, true)
        }
    }

    private fun setSliderContainerWithGoEdichie() {
        val viewPager = findViewById<ViewPager2>(R.id.view_pager)
        val sliderItems = arrayListOf<SliderItem>()
        sliderItems.add(SliderItem(R.drawable.contacts_list))

        val sliderAdapter = SliderAdapter(sliderItems)

        viewPager.apply {
            adapter = sliderAdapter
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 3
            getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER

            val compositePageTransformer = CompositePageTransformer()
            compositePageTransformer.addTransformer(MarginPageTransformer(30))
            compositePageTransformer.addTransformer { page, position ->
                val r = 1 - abs(position)
                page.scaleY = 0.85f + r * 0.25f
            }

            setPageTransformer(compositePageTransformer)

            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)

                    currentPosition = position

                    binding.apply {
                        radioButton1.visibility = View.GONE
                        radioButton2.visibility = View.GONE
                        radioButton3.visibility = View.GONE
                        radioButton4.visibility = View.GONE

                        when (currentPosition) {
                            0 -> {
                                title.text = getString(R.string.start_activity_import_title)
                                subtitle.text = getString(R.string.start_activity_import_subtitle)

                                checkRadioButton(radioButton2.id)

                                activateButton.visibility = View.VISIBLE
                                subtitle.visibility = View.VISIBLE
                                activateButtonIsClickable(!contactsAreImported)
                                activateButton.setText(R.string.start_activity_button_notification)
                                next.visibility = View.GONE
                                skip.visibility = View.GONE
                            }
                        }
                    }
                }
            })

            setCurrentItem(currentPosition, true)
        }
    }

    private fun activateButtonIsClickable(isClickable: Boolean) {
        binding.activateButton.isClickable = isClickable

        if (isClickable) {
            binding.activateButton.setBackgroundColor(
                ResourcesCompat.getColor(
                    resources, R.color.colorPrimary, null
                )
            )
        } else {
            binding.activateButton.setBackgroundColor(
                ResourcesCompat.getColor(
                    resources, R.color.greyColor, null
                )
            )
        }
    }

    private fun openOverlaySettings() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")
        )
        startActivity(intent)
    }

    private fun checkRadioButton(id: Int) {
        binding.radioButton1.isChecked = binding.radioButton1.id == id
        binding.radioButton2.isChecked = binding.radioButton2.id == id
        binding.radioButton3.isChecked = binding.radioButton3.id == id
        binding.radioButton4.isChecked = binding.radioButton4.id == id
    }

    private fun firstLaunchValidate() {
        val sharedFirstLaunch = getSharedPreferences("First_Launch", Context.MODE_PRIVATE)
        val edit = sharedFirstLaunch.edit()
        edit.putBoolean("First_Launch", true)
        edit.apply()
    }

    //region =========================================== BILLING ============================================

    private fun setupBillingClient() {
        sharedPreferencesConfiguration("Funky_Sound_Bought")
        sharedPreferencesConfiguration("Jazzy_Sound_Bought")
        sharedPreferencesConfiguration("Relax_Sound_Bought")
        sharedPreferencesConfiguration("Contacts_Unlimited_Bought")
        sharedPreferencesConfiguration("Apps_Support_Bought")
        sharedPreferencesConfiguration("Produits_Fake_Bought")
    }

    private fun sharedPreferencesConfiguration(sharedPreferencesName: String) {
        val private = Context.MODE_PRIVATE

        val sharedPreferences = getSharedPreferences(sharedPreferencesName, private)
        val edit = sharedPreferences.edit()
        edit.putBoolean(sharedPreferencesName, true)
        edit.apply()
    }

    //endregion

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_WRITE_READ_CONTACT) {
            if (ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.WRITE_CONTACTS
                ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.READ_CONTACTS
                ) == PackageManager.PERMISSION_GRANTED
            ) {

            }
        }
    }

    companion object {
        const val REQUEST_CODE_WRITE_READ_CONTACT = 1
        const val READ_CALL_LOG_PERMISSION_REQUEST = 2
    }

    override fun onBackPressed() {
    }

    private fun readPhoneStateDialog() {
        val alertDialog = MaterialAlertDialogBuilder(this, R.style.AlertDialog).setBackground(getDrawable(R.color.backgroundColor))
            .setTitle(getString(R.string.incoming_voice_calls_title)).setMessage(getString(R.string.incoming_voice_calls_message))
            .setPositiveButton(R.string.start_activity_go_edition_positive_button) { _, _ ->
                requestPermissionLauncher2.launch(arrayOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.CALL_PHONE))
            }.setNegativeButton(R.string.alert_dialog_no) { _, _ ->
            }

        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun activateNotificationsClick() {
        startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
        val intentFilter = IntentFilter()
        intentFilter.addAction("com.yellowtwigs.Knockin.notificationExemple")
    }

    private fun isNotificationServiceEnabled(): Boolean {
        val pkgName = packageName
        val str = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        if (!TextUtils.isEmpty(str)) {
            val names = str.split(":")
            for (i in names.indices) {
                val cn = ComponentName.unflattenFromString(names[i])
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.packageName)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    override fun onPurchasesUpdated(p0: BillingResult, p1: MutableList<Purchase>?) {
    }

    //endregion

    //region =========================================== Lifecycle ==========================================

    override fun onRestart() {
        super.onRestart()

        val sharedPreferences = getSharedPreferences("Knockin_preferences", Context.MODE_PRIVATE)
        Log.i("GetNotification", "isNotificationServiceEnabled() : ${isNotificationServiceEnabled()}")
        Log.i("GetNotification", "Settings.canDrawOverlays(this) : ${Settings.canDrawOverlays(this)}")
        if (isNotificationServiceEnabled()) {
            val edit: SharedPreferences.Editor = sharedPreferences.edit()
            edit.putBoolean("serviceNotif", true)
            edit.apply()
            binding.viewPager.currentItem = 2
        }
        if (Settings.canDrawOverlays(this)) {
            val edit: SharedPreferences.Editor = sharedPreferences.edit()
            edit.putBoolean("popupNotif", true)
            edit.apply()
            binding.viewPager.currentItem = 3
        }
    }

    //endregion
}