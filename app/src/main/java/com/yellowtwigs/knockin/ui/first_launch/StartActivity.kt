package com.yellowtwigs.knockin.ui.first_launch

import android.Manifest
import android.app.ActivityManager
import android.content.*
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Point
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.TypedValue
import android.view.View
import android.webkit.WebChromeClient
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.android.billingclient.api.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.ActivityStartActivityBinding
import com.yellowtwigs.knockin.ui.contacts.MainActivity
import com.yellowtwigs.knockin.model.data.*
import com.yellowtwigs.knockin.ui.contacts.ContactsViewModel
import com.yellowtwigs.knockin.ui.edit_contact.ContactDetailsViewModel
import com.yellowtwigs.knockin.ui.groups.GroupsViewModel
import com.yellowtwigs.knockin.ui.groups.LinkContactGroupViewModel
import com.yellowtwigs.knockin.utils.RandomDefaultImage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Activité qui nous permet d'importer nos contacts et accepter toutes les autorisations liées aux notifications appel et message
 * @author Florian Striebel, Kenzy Suon
 */
@AndroidEntryPoint
class StartActivity : AppCompatActivity(), PurchasesUpdatedListener {

    //region ========================================== Val or Var ==========================================

    private var billingClient: BillingClient? = null

    private lateinit var knockinPreferences: SharedPreferences
    private var funkyIsBought: SharedPreferences? = null
    private var jazzyIsBought: SharedPreferences? = null
    private var relaxIsBought: SharedPreferences? = null
    private var unlimitedContacts: SharedPreferences? = null

    private val contactsViewModel: ContactsViewModel by viewModels()
    private val contactDetailsViewModel: ContactDetailsViewModel by viewModels()
    private val groupsViewModel: GroupsViewModel by viewModels()
    private val linkContactGroupViewModel: LinkContactGroupViewModel by viewModels()

    private lateinit var binding: ActivityStartActivityBinding

    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStartActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT

        val listApp = getAppOnPhone()

        funkyIsBought = getSharedPreferences("Funky_Sound_Bought", Context.MODE_PRIVATE)
        jazzyIsBought = getSharedPreferences("Jazzy_Sound_Bought", Context.MODE_PRIVATE)
        relaxIsBought = getSharedPreferences("Relax_Sound_Bought", Context.MODE_PRIVATE)
        unlimitedContacts = getSharedPreferences("Contacts_Unlimited_Bought", Context.MODE_PRIVATE)
        knockinPreferences = getSharedPreferences("Knockin_preferences", Context.MODE_PRIVATE)

        setupBillingClient()

        binding.apply {
            webView.apply {
                val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                var isWifiConn = false
                var isMobileConn = false
                connMgr.allNetworks.forEach { network ->
                    connMgr.getNetworkInfo(network)?.apply {
                        if (type == ConnectivityManager.TYPE_WIFI) {
                            isWifiConn = isWifiConn or isConnected
                        }
                        if (type == ConnectivityManager.TYPE_MOBILE) {
                            isMobileConn = isMobileConn or isConnected
                        }
                    }
                }
                val layoutSize = Point()
                val displayScreen = windowManager.defaultDisplay
                displayScreen.getRealSize(layoutSize)
                if (isWifiConn || isMobileConn) {
                    val sizeTest = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        150f,
                        resources.displayMetrics
                    )
                    webView.layoutParams.height = layoutSize.y - sizeTest.toInt()
                    webView.settings.loadWithOverviewMode = true
                    webView.settings.useWideViewPort = true
                    webView.settings.javaScriptEnabled = true
                    webView.webChromeClient = WebChromeClientCustomPoster()
                    //videoview.visibility = View.INVISIBLE

                    when (Resources.getSystem().configuration.locale.language) {
                        "fr" -> {
                            webView.visibility = View.VISIBLE
                            webView.loadUrl("https://www.yellowtwigs.com/france")
                        }
                        "de" -> {
                            println(importContactsButton!!.textSize)
                            importContactsButton!!.textSize = 10f
                            activateNotificationsButton!!.textSize = 10f
                            superpositionButton!!.textSize = 10f
                            permissionsButton!!.textSize = 10f
                            webView.visibility = View.VISIBLE
                            webView.loadUrl("https://www.yellowtwigs.com/germany")
                        }
                        "in" -> {
                            importContactsButton!!.textSize = 9f
                            activateNotificationsButton!!.textSize = 9f
                            superpositionButton!!.textSize = 9f
                            permissionsButton!!.textSize = 9f
                            webView.visibility = View.VISIBLE
                            webView.loadUrl("https://www.yellowtwigs.com/indonesia")
                        }
                        "vi" -> {
                            webView.visibility = View.VISIBLE
                            webView.loadUrl("https://www.yellowtwigs.com/vietnam")
                        }
                        "it" -> {
                            webView.visibility = View.VISIBLE
                            webView.loadUrl("https://www.yellowtwigs.com/italy")
                        }
                        "es" -> {
                            webView.visibility = View.VISIBLE
                            webView.loadUrl("https://www.yellowtwigs.com/spain")
                        }
                        "pt" -> {
                            webView.visibility = View.VISIBLE
                            webView.loadUrl("https://www.yellowtwigs.com/portugal")
                        }
                        "ar" -> {
                            webView.visibility = View.VISIBLE
                            webView.loadUrl("https://www.yellowtwigs.com/arabic")
                        }
                        "ru" -> {
                            importContactsButton!!.textSize = 7f
                            activateNotificationsButton!!.textSize = 7f
                            superpositionButton!!.textSize = 7f
                            permissionsButton!!.textSize = 7f
                            webView.visibility = View.VISIBLE
                            webView.loadUrl("https://www.yellowtwigs.com/russia")
                        }
                        "tr" -> {
                            webView.visibility = View.VISIBLE
                            webView.loadUrl("https://www.yellowtwigs.com/turkey")
                        }
                    }
                }
                if (visibility == View.GONE) {
                    val sizeTest = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        150f,
                        resources.displayMetrics
                    )
                    videoView.layoutParams.height = layoutSize.y - sizeTest.toInt()
                    visibility = View.INVISIBLE
                    videoView.visibility = View.VISIBLE
                    val uri =
                        Uri.parse("android.resource://" + packageName + "/" + R.raw.in_app_video_en)
                    videoView.setVideoURI(uri)
                    videoView.start()
                }
            }

            val mediaController = MediaController(this@StartActivity)
            videoView.setMediaController(mediaController)
            mediaController.setAnchorView(videoView)

            videoView.setOnCompletionListener {
                startActivityVideoLayout.visibility = View.INVISIBLE
                videoSkip.visibility = View.INVISIBLE
                startActivityLayout.visibility = View.VISIBLE
                videoView.stopPlayback()
            }

            if (checkIfGoEdition()) {
                activateNotificationsButton.visibility = View.GONE
                superpositionButton.visibility = View.GONE

                MaterialAlertDialogBuilder(this@StartActivity, R.style.AlertDialog)
                    .setBackground(getDrawable(R.color.backgroundColor))
                    .setMessage(getString(R.string.start_activity_go_edition_message))
                    .setPositiveButton(R.string.start_activity_go_edition_positive_button) { _, _ ->
                    }
                    .show()
            }
        }

        checkOverlay()
        checkNotifications()

        //region ======================================== Listeners =========================================

        binding.apply {
            videoSkip.setOnClickListener {
                webView.removeAllViews()
                webView.clearCache(true)
                webView.destroy()
                startActivityVideoLayout.visibility = View.INVISIBLE
                videoSkip.visibility = View.INVISIBLE
                startActivityLayout.visibility = View.VISIBLE
                videoView.stopPlayback()

                Toast.makeText(
                    this@StartActivity,
                    getString(R.string.start_activity_video_tutorial_skip_text),
                    Toast.LENGTH_LONG
                ).show()
            }

            importContactsButton.setOnClickListener {
                ActivityCompat.requestPermissions(
                    this@StartActivity,
                    arrayOf(Manifest.permission.READ_CONTACTS),
                    ImportContactsActivity.REQUEST_CODE_READ_CONTACT
                )
                importContactsButton.visibility = View.INVISIBLE
                importContactsLoading.visibility = View.VISIBLE

                if (listApp.contains("com.whatsapp")) {
                    getSharedPreferences("importWhatsapp", Context.MODE_PRIVATE).edit().apply {
                        putBoolean("importWhatsapp", true)
                        apply()
                    }
                }
            }

            activateNotificationsButton.setOnClickListener {
                activateNotificationsClick()

                activateNotificationsButton.visibility = View.INVISIBLE
                activateNotificationsLoading.visibility = View.VISIBLE

                CoroutineScope(Dispatchers.Main).launch {
                    activateNotificationsLoading.visibility = View.VISIBLE
                    delay(2000)
                    activateNotificationsLoading.visibility = View.INVISIBLE
                }
            }

            superpositionButton.setOnClickListener {
                verifiedOverlaySettings()
            }

            permissionsButton.setOnClickListener {
                val arraylistPermission = ArrayList<String>()
                arraylistPermission.add(Manifest.permission.SEND_SMS)
                arraylistPermission.add(Manifest.permission.CALL_PHONE)
                ActivityCompat.requestPermissions(
                    this@StartActivity,
                    arraylistPermission.toArray(arrayOfNulls<String>(arraylistPermission.size)),
                    REQUEST_CODE_SMS_AND_CALL
                )
                allIsCheckedGOEdition()
            }

            startActivityNext.setOnClickListener {
                if (!checkIfGoEdition()) {
                    buildMultiSelectAlertDialog()
                } else {
                    val intent = Intent(this@StartActivity, MainActivity::class.java)
                    intent.putExtra("fromStartActivity", true)
                    startActivity(intent)
                    finish()
                }
            }

            startActivitySkip.setOnClickListener {
                if (!checkIfGoEdition()) {
                    if (importContactsCheck.isVisible) {
                        buildMultiSelectAlertDialog()
                    } else {
                        buildLeaveAlertDialog()
                    }
                } else {
                    val intent = Intent(this@StartActivity, MainActivity::class.java)
                    intent.putExtra("fromStartActivity", true)
                    startActivity(intent)
                    finish()
                }
            }
        }

        //endregion
    }

    //region ========================================== Functions ==========================================

    private fun buildMultiSelectAlertDialog(): AlertDialog {
        return MaterialAlertDialogBuilder(this, R.style.AlertDialog)
            .setBackground(getDrawable(R.color.backgroundColor))
            .setTitle(getString(R.string.notification_alert_dialog_title))
            .setMessage(getString(R.string.notification_alert_dialog_message))
            .setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
                startActivity(Intent(this@StartActivity, MultiSelectActivity::class.java))
                knockinPreferences.edit().apply {
                    putBoolean("view", true)
                    apply()
                }
                closeContextMenu()
                finish()
            }
            .setNegativeButton(R.string.alert_dialog_later)
            { _, _ ->
                closeContextMenu()
                val intent = Intent(this@StartActivity, MainActivity::class.java)
                intent.putExtra("fromStartActivity", true)
                startActivity(intent)
                finish()
            }
            .show()
    }

    private fun buildLeaveAlertDialog(): AlertDialog {
        val message = if (binding.importContactsButton.visibility == View.VISIBLE) {
            getString(R.string.start_activity_skip_alert_dialog_message_importation)
        } else {
            getString(R.string.start_activity_skip_alert_dialog_message)
        }

        return MaterialAlertDialogBuilder(this, R.style.AlertDialog)
            .setBackground(getDrawable(R.color.backgroundColor))
            .setTitle(getString(R.string.start_activity_skip_alert_dialog_title))
            .setMessage(message)
            .setPositiveButton(R.string.start_activity_skip_alert_dialog_positive_button) { _, _ ->
                val intent = Intent(this@StartActivity, MainActivity::class.java)
                intent.putExtra("fromStartActivity", true)
                knockinPreferences.edit().apply {
                    putBoolean("view", true)
                    apply()
                }
                closeContextMenu()
                startActivity(intent)
            }
            .setNegativeButton(R.string.alert_dialog_cancel)
            { _, _ ->
                closeContextMenu()
            }
            .show()
    }

    private fun getAppOnPhone(): ArrayList<String> {
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        val resolveInfoList = packageManager.queryIntentActivities(intent, 0)
        val packageNameList = java.util.ArrayList<String>()
        for (resolveInfo in resolveInfoList) {
            val activityInfo = resolveInfo.activityInfo
            packageNameList.add(activityInfo.applicationInfo.packageName)
        }
        return packageNameList
    }

    //region =========================================== Overlay ============================================

    private fun verifiedOverlaySettings() {
        CoroutineScope(Dispatchers.Main).launch {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S) {
                binding?.superpositionButton.visibility = View.INVISIBLE
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
                binding?.superpositionLoading.visibility = View.VISIBLE
            }
            delay(2000)
            binding?.superpositionLoading.visibility = View.INVISIBLE
        }
    }

    //endregion

    //region ========================================= Notifications ========================================

    private fun activateNotificationsClick() {
        startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
        val intentFilter = IntentFilter()
        intentFilter.addAction("com.yellowtwigs.Knockin.notificationExemple")
    }

    private fun isNotificationServiceEnabled(): Boolean {
        val str = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        if (!TextUtils.isEmpty(str)) {
            val names = str.split(":")
            for (i in names.indices) {
                val cn = ComponentName.unflattenFromString(names[i])
                if (cn != null) {
                    if (TextUtils.equals(packageName, cn.packageName)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    //endregion

    //region ============================================ Check =============================================

    private fun checkIfGoEdition(): Boolean {
        return (baseContext.getSystemService(ACTIVITY_SERVICE) as ActivityManager).isLowRamDevice
    }

    private fun allIsChecked() {
        if (binding.activateNotificationsCheck.visibility == View.VISIBLE &&
            binding.importContactsCheck.visibility == View.VISIBLE
        ) {
            binding.startActivityNext.visibility = View.VISIBLE
            binding.startActivitySkip.visibility = View.GONE
        }
    }

    private fun allIsCheckedGOEdition() {
        if (binding.importContactsCheck.visibility == View.VISIBLE &&
            binding.permissionsCheck.visibility == View.VISIBLE
        ) {
            binding.startActivityNext.visibility = View.VISIBLE
            binding.startActivitySkip.visibility = View.GONE
        }
    }

    private fun checkOverlay() {
        if (Settings.canDrawOverlays(this)) {
            binding.superpositionButton.visibility = View.INVISIBLE
            binding.superpositionLoading.visibility = View.INVISIBLE
            binding.superpositionCheck.visibility = View.VISIBLE
            knockinPreferences.edit().apply {
                putBoolean("popupNotif", true)
                apply()
            }
            allIsChecked()
        } else {
            binding.superpositionButton.visibility = View.VISIBLE
        }
    }

    private fun checkNotifications() {
        if (isNotificationServiceEnabled()) {
            binding.activateNotificationsButton.visibility = View.INVISIBLE
            binding.activateNotificationsLoading.visibility = View.INVISIBLE
            binding.activateNotificationsCheck.visibility = View.VISIBLE
            knockinPreferences.edit().apply {
                putBoolean("serviceNotif", true)
                putBoolean("popupNotif", true)
                apply()
            }
            allIsChecked()
        } else {
            binding.activateNotificationsCheck.visibility = View.INVISIBLE
            binding.activateNotificationsLoading.visibility = View.INVISIBLE
            binding.activateNotificationsButton.visibility = View.VISIBLE
        }
    }

    //endregion

    //region ========================================= BillingClient ========================================

    private fun setupBillingClient() {
        billingClient = BillingClient.newBuilder(this)
            .setListener(this)
            .enablePendingPurchases()
            .build()
        connectToGooglePlayBilling()
    }

    private fun connectToGooglePlayBilling() {
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    querySkuDetails()
                }
            }

            override fun onBillingServiceDisconnected() {
                connectToGooglePlayBilling()
            }
        })
    }

    private fun querySkuDetails() {
        val skuList = ArrayList<String>()
        skuList.add("contacts_vip_unlimited")
        skuList.add("notifications_vip_funk_theme")
        skuList.add("notifications_vip_jazz_theme")
        skuList.add("notifications_vip_relaxation_theme")

        val params = SkuDetailsParams.newBuilder()
            .setSkusList(skuList)
            .setType(BillingClient.SkuType.INAPP)

        billingClient?.querySkuDetailsAsync(
            params.build()
        ) { _, _ ->

            billingClient?.queryPurchasesAsync(
                BillingClient.SkuType.INAPP
            ) { _, listOfPurchases ->
                if (listOfPurchases.isNotEmpty()) {
                    for (purchase in listOfPurchases) {
                        when {
                            purchase.originalJson.contains("notifications_vip_funk_theme") -> {
                                val edit =
                                    funkyIsBought?.edit()
                                edit?.putBoolean("Funky_Sound_Bought", true)
                                edit?.apply()
                            }
                            purchase.originalJson.contains("notifications_vip_jazz_theme") -> {
                                val edit =
                                    jazzyIsBought?.edit()
                                edit?.putBoolean("Jazzy_Sound_Bought", true)
                                edit?.apply()
                            }
                            purchase.originalJson.contains("notifications_vip_relaxation_theme") -> {
                                val edit =
                                    relaxIsBought?.edit()
                                edit?.putBoolean("Relax_Sound_Bought", true)
                                edit?.apply()
                            }
                            purchase.originalJson.contains("contacts_vip_unlimited") -> {
                                val edit =
                                    unlimitedContacts?.edit()
                                edit?.putBoolean(
                                    "Contacts_Unlimited_Bought",
                                    true
                                )
                                edit?.apply()
                            }
                        }
                    }
                }
            }
        }
    }

    //endregion

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_READ_CONTACT) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.import_contacts_toast, Toast.LENGTH_LONG).show()
                getAllContactsInfoSync()

                val sharedPreferencesSync =
                    getSharedPreferences("save_last_sync", Context.MODE_PRIVATE)
                var index = 1
                var stringSet = listOf<String>()
                if (sharedPreferencesSync.getStringSet(index.toString(), null) != null)
                    stringSet =
                        sharedPreferencesSync.getStringSet(index.toString(), null)!!.sorted()
                arrayListOf<Pair<ContactDB, List<ContactDetailDB>>>()
                while (sharedPreferencesSync.getStringSet(
                        index.toString(),
                        null
                    ) != null && stringSet.isNotEmpty()
                ) {
                    stringSet =
                        sharedPreferencesSync.getStringSet(index.toString(), null)!!.sorted()
                    index++
                }

                CoroutineScope(Dispatchers.Main).launch {
                    binding.importContactsLoading.visibility = View.INVISIBLE
                    binding.importContactsCheck.visibility = View.VISIBLE
                    allIsChecked()
                    allIsCheckedGOEdition()
                }
            } else {
                binding.importContactsLoading.visibility = View.INVISIBLE
                binding.importContactsButton.visibility = View.VISIBLE
            }
        }

        if (ContextCompat.checkSelfPermission(
                this@StartActivity, Manifest.permission.CALL_PHONE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            binding.permissionsButton.visibility = View.INVISIBLE
            binding.permissionsLoading.visibility = View.VISIBLE
        }

        if (REQUEST_CODE_SMS_AND_CALL == requestCode) {
            binding.permissionsLoading.visibility = View.INVISIBLE
            binding.permissionsCheck.visibility = View.VISIBLE
        }
        allIsChecked()
        allIsCheckedGOEdition()
    }

    override fun onPurchasesUpdated(p0: BillingResult, p1: MutableList<Purchase>?) {}

    private fun getAllContactsInfoSync() {
        contactsViewModel.apply {
            val phoneStructName = getStructuredNameSync(contentResolver)
            val contactNumberAndPic = getPhoneNumberSync(contentResolver)
            val contactMail = getContactMailSync(contentResolver)
            val contactGroup = groupsViewModel.getContactGroupSync(contentResolver)
            val contactDetail = contactNumberAndPic.union(contactMail)

            createListContactsSync(phoneStructName, contactDetail.toList(), contactGroup)
        }
    }

    private fun createListContactsSync(
        phoneStructName: List<Pair<Int, Triple<String, String, String>>>?,
        contactNumberAndPic: List<Map<Int, Any>>,
        contactGroup: List<Triple<Int, String?, String?>>
    ) {
        val phoneContactsList = arrayListOf<ContactDB>()
        val lastId = arrayListOf<Int>()
        val sharedPreferencesSync = getSharedPreferences("save_last_sync", Context.MODE_PRIVATE)
        val edit = sharedPreferencesSync.edit()
        var contactLinksGroup: Pair<LinkContactGroup, GroupDB>
        val listLinkAndGroup = arrayListOf<Pair<LinkContactGroup, GroupDB>>()
        var lastSyncId = ""
        var lastSync = ""

        var modifiedContact = 0
        phoneStructName?.forEach { fullName ->
            val set = mutableSetOf<String>()
            contactNumberAndPic.forEach { numberPic ->
                val id = numberPic[1].toString().toInt()
                if (!lastId.contains(id)) {
                    val contactDetails =
                        contactDetailsViewModel.getDetailsById(id, contactNumberAndPic)
                    val contactGroups = groupsViewModel.getGroupsAndLinks(id, contactGroup)
                    if (fullName.first == numberPic[1]) {
                        lastId.add(id)
                        if (fullName.second.second == "") {
                            val contact = ContactDB(
                                null,
                                fullName.second.first,
                                fullName.second.third,
                                "",
                                RandomDefaultImage.randomDefaultImage(
                                    0,
                                    this@StartActivity,
                                    "Create"
                                ),
                                1,
                                numberPic[4].toString(),
                                0,
                                "",
                                0,
                                "",
                                0,
                                1,
                                ""
                            )
                            lastSync =
                                sharedPreferencesSync.getString("last_sync_2", "").toString()
                            if (!contactsViewModel.isDuplicateContacts(fullName, lastSync)) {
                                CoroutineScope(Dispatchers.IO).launch {
                                    contact.id = contactsViewModel.insertContact(contact)?.toInt()
                                }
                                lastSyncId += fullName.first.toString() + ":" + contact.id.toString() + "|"
                                for (details in contactDetails) {
                                    details.idContact = contact.id
                                }
                                for (groups in contactGroups) {
                                    val links =
                                        contact.id?.let { LinkContactGroup(0, it) }
                                    contactLinksGroup =
                                        Pair(links, groups) as Pair<LinkContactGroup, GroupDB>
                                    listLinkAndGroup.add(contactLinksGroup)
                                }
                                saveGroupsAndLinks(listLinkAndGroup)
                                listLinkAndGroup.clear()
                                contactsViewModel.insertDetails(contactDetails)
                            } else {
                                var positionInSet = 3
                                val contactWithAndroidId =
                                    contactsViewModel.getContactWithAndroidId(
                                        fullName.first,
                                        lastSync
                                    )
                                if (contactWithAndroidId != null) {
                                    set.add("0" + contactWithAndroidId.contactDB!!.id)
                                    set.add("1" + fullName.second.first)
                                    set += if (fullName.second.second == "")
                                        "2" + fullName.second.third
                                    else
                                        "2" + fullName.second.second + " " + fullName.second.third
                                    for (details in contactDetails) {
                                        val alldetail =
                                            details.type + ":" + details.content + ":" + details.tag
                                        set += positionInSet.toString() + alldetail
                                        positionInSet++
                                    }
                                    if (!isSameContact(
                                            contactWithAndroidId,
                                            fullName.second,
                                            contactDetails
                                        )
                                    ) {
                                        modifiedContact++
                                        edit.putStringSet(modifiedContact.toString(), set)
                                        edit.apply()
                                    }
                                } else {
                                    lastSync = contactsViewModel.deleteContactFromLastSync(
                                        lastSync,
                                        fullName.first
                                    )
                                    edit.putString("last_sync_2", lastSync)
                                    edit.apply()
                                    CoroutineScope(Dispatchers.IO).launch {
                                        contact.id =
                                            contactsViewModel.insertContact(contact)?.toInt()
                                        lastSyncId += fullName.first.toString() + ":" + contact.id.toString() + "|"
                                        for (details in contactDetails) {
                                            details.idContact = contact.id
                                        }
                                        for (groups in contactGroups) {
                                            val links =
                                                contact.id
                                                    ?.let { LinkContactGroup(0, it) }
                                            contactLinksGroup = Pair(
                                                links,
                                                groups
                                            ) as Pair<LinkContactGroup, GroupDB>
                                            listLinkAndGroup.add(contactLinksGroup)
                                        }
                                        saveGroupsAndLinks(listLinkAndGroup)
                                        listLinkAndGroup.clear()
                                        contactsViewModel.insertDetails(contactDetails)
                                    }
                                }
                            }
                            phoneContactsList.add(contact)
                        } else if (fullName.second.second != "") {
                            val contact = ContactDB(
                                null,
                                fullName.second.first,
                                fullName.second.second + " " + fullName.second.third,
                                "",
                                RandomDefaultImage.randomDefaultImage(
                                    0,
                                    this@StartActivity,
                                    "Create"
                                ),
                                1,
                                numberPic[4].toString(),
                                0,
                                "",
                                0,
                                "",
                                0,
                                1,
                                ""
                            )
                            phoneContactsList.add(contact)
                            contactsViewModel.allContacts.observe(this@StartActivity) { allContacts ->
                                if (!isDuplicate(allContacts, contact)) {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        contact.id =
                                            contactsViewModel.insertContact(contact)
                                                ?.toInt()
                                        for (details in contactDetails) {
                                            details.idContact = contact.id
                                        }
                                        contactsViewModel.insertDetails(contactDetails)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (lastSyncId != "") {
            if (lastSync != "")
                lastSyncId = lastSync + lastSyncId
            edit.putString("last_sync_2", lastSyncId)
            edit.apply()
//            }
            contactsViewModel.getContactAllInfo().observe(this@StartActivity) {

            }
        }
//            val syncContact = executorService.submit(callDb).get()?.asLiveData()?.value
//            if (syncContact != null) {
//                contactManager.contactList.addAll(syncContact)
//            }
//        }
    }

    private fun isDuplicate(contacts: List<ContactDB>?, phoneContactList: ContactDB): Boolean {
        if (contacts != null) {
            for (contact in contacts) {
                if (contact.firstName == phoneContactList.firstName && contact.lastName == phoneContactList.lastName)
                    return true
            }
        }
        return false
    }

    private fun isSameContact(
        contact: ContactWithAllInformation, fullname: Triple<String, String, String>,
        contactDetail: List<ContactDetailDB>
    ): Boolean {
        var isSame = true
        if (fullname.second != "") {
            if (contact.contactDB?.firstName != fullname.first || contact.contactDB?.lastName != fullname.second + " " + fullname.third) {
                return false
            }
        } else {
            if (contact.contactDB?.firstName != fullname.first || contact.contactDB?.lastName != fullname.third) {
                return false
            }
        }
        if (contact.contactDetailList!!.size != contactDetail.size) {
            return false
        }
        var alreadyCheck: Int
        contact.contactDetailList!!.forEach { Knockin ->
            alreadyCheck = 0
            contactDetail.forEach {
                if (alreadyCheck == 0 && (Knockin.type != it.type || Knockin.content != it.content || Knockin.tag != it.tag)) {
                    isSame = false
                } else {
                    alreadyCheck = 1
                    isSame = true
                }
            }
            if (!isSame)
                return false
        }
        return true
    }

    private fun saveGroupsAndLinks(listLinkAndGroup: List<Pair<LinkContactGroup, GroupDB>>) {
        listLinkAndGroup.forEach {
            groupsViewModel.getGroupWithName(it.second.name)
                .observe(this@StartActivity) { groupDb ->
                    CoroutineScope(Dispatchers.IO).launch {
                        if (groupDb != null) {
                            linkContactGroupViewModel.insert(
                                LinkContactGroup(groupDb.id.toInt(), it.first.idContact)
                            )
                        }
                    }
                }
        }
    }

    companion object {
        const val REQUEST_CODE_READ_CONTACT = 2
        const val REQUEST_CODE_SMS_AND_CALL = 5
    }

    //region =========================================== Lifecycle ==========================================

    override fun onRestart() {
        super.onRestart()

        checkNotifications()
        checkOverlay()
    }

    override fun onBackPressed() {
    }

    //endregion

    //endregion

    inner class WebChromeClientCustomPoster : WebChromeClient() {
        override fun getDefaultVideoPoster(): Bitmap? {
            return Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
        }
    }
}