package com.yellowtwigs.knockin.ui.contacts.list

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Settings
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.view.*
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.model.ContactManager
import com.yellowtwigs.knockin.model.ContactsRoomDatabase
import com.yellowtwigs.knockin.model.DbWorkerThread
import com.yellowtwigs.knockin.model.data.*
import com.yellowtwigs.knockin.ui.CockpitActivity
import com.yellowtwigs.knockin.ui.HelpActivity
import com.yellowtwigs.knockin.ui.contacts.ContactGridViewAdapter
import com.yellowtwigs.knockin.ui.contacts.MultiChannelActivity
import com.yellowtwigs.knockin.ui.edit_contact.AddNewContactActivity
import com.yellowtwigs.knockin.ui.group.list.GroupManagerActivity
import com.yellowtwigs.knockin.ui.in_app.PremiumActivity
import com.yellowtwigs.knockin.ui.notifications.NotificationListener
import com.yellowtwigs.knockin.ui.notifications.history.NotificationHistoryActivity
import com.yellowtwigs.knockin.ui.settings.ManageMyScreenActivity
import com.yellowtwigs.knockin.ui.settings.ManageNotificationActivity
import com.yellowtwigs.knockin.ui.settings.SettingsActivity
import com.yellowtwigs.knockin.ui.teleworking.TeleworkingActivity
import com.yellowtwigs.knockin.utils.ContactGesture.callPhone
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Activite qui permet d'afficher et de gérer la homepage
 * @author Florian Striebel, Kenzy Suon, Ryan Granet
 */
@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class MainActivity : AppCompatActivity(), DrawerLayout.DrawerListener {

    //region ========================================== Var or Val ==========================================

    // Show on the Main Layout
    private var mainDrawerLayout: DrawerLayout? = null
    private var bottomNavigationView: BottomNavigationView? = null

    // Gridview et Recycler avec leur adapter
    private var main_GridView: RecyclerView? = null
    private var gridViewAdapter: ContactGridViewAdapter? = null
    private var main_RecyclerView: RecyclerView? = null
    private var recyclerViewAdapter: ContactRecyclerViewAdapter? = null

    // Floating Button
    private var main_FloatingButtonAddNewContact: FloatingActionButton? = null
    private var main_FloatingButtonMultiChannel: FloatingActionButton? = null
    private var main_FloatingButtonSMS: FloatingActionButton? = null
    private var main_FloatingButtonMail: FloatingActionButton? = null
    private var main_FloatingButtonGroup: FloatingActionButton? = null

    internal var main_search_bar_value = ""
    private var main_filter = arrayListOf<String>()
    private var main_SearchBar: AppCompatEditText? = null

    // Custom Toolbar
    private var main_ToolbarLayout: RelativeLayout? = null
    private var main_ToolbarMultiSelectModeLayout: RelativeLayout? = null

    private var main_ToolbarMultiSelectModeClose: AppCompatImageView? = null
    private var main_toolbar_Help: AppCompatImageView? = null
    private var main_ToolbarMultiSelectModeTitle: TextView? = null
    private var main_ToolbarMultiSelectModeDelete: AppCompatImageView? = null
    private var main_ToolbarMultiSelectModeMenu: AppCompatImageView? = null
    private var main_toolbar_OpenDrawer: AppCompatImageView? = null

    // Database && Thread
    private var main_ContactsDatabase: ContactsRoomDatabase? = null
    private lateinit var main_mDbWorkerThread: DbWorkerThread

    private var gestionnaireContacts: ContactManager? = null
    private var main_constraintLayout: ConstraintLayout? = null
    private var main_loadingPanel: RelativeLayout? = null

    private var listOfItemSelected: ArrayList<ContactWithAllInformation> = ArrayList()

    private var firstClick: Boolean = true
    private var multiChannelMode: Boolean = false

    private val PERMISSION_CALL_RESULT = 1
    private val PERMISSION_READ_CONTACT = 99

    private var idGroup: Long = 0

    private var settings_left_drawer_ThemeSwitch: SwitchCompat? = null

    private lateinit var sharedFromSplashScreen: SharedPreferences
    private lateinit var sharedShowPopup: SharedPreferences

    private val mOnNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_contacts -> {
                }
                R.id.navigation_groups -> {
                    startActivity(
                        Intent(
                            this@MainActivity,
                            GroupManagerActivity::class.java
                        ).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    )
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_teleworking -> {
                    startActivity(
                        Intent(
                            this@MainActivity,
                            TeleworkingActivity::class.java
                        ).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    )
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_notifcations -> {
                    startActivity(
                        Intent(
                            this@MainActivity,
                            NotificationHistoryActivity::class.java
                        ).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    )
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_cockpit -> {
                    startActivity(
                        Intent(this@MainActivity, CockpitActivity::class.java).addFlags(
                            Intent.FLAG_ACTIVITY_NO_ANIMATION
                        )
                    )
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }

    //endregion

    /**
     * Méthode lancé par le système à chaque redémarage de l'activité
     * @param Bundle @type
     */
    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("ClickableViewAccessibility", "InflateParams")
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

        val importWhatsappSharedPreferences: SharedPreferences =
            getSharedPreferences("importWhatsappPreferences", Context.MODE_PRIVATE)
        val importWhatsapp =
            importWhatsappSharedPreferences.getBoolean("importWhatsappPreferences", false)

        setContentView()

        val isDelete = intent.getBooleanExtra("isDelete", false)
        if (isDelete) {
            Toast.makeText(this, R.string.main_toast_delete_contact, Toast.LENGTH_LONG).show()
        }

        val position = intent.getIntExtra("position", 0)

        //region ======================= Relancement du Service de Notification =============================

        if (isNotificationServiceEnabled()) {
            toggleNotificationListenerService()
        }

        //endregion

        //region ======================================== Mobile Ads ========================================

        sharedFromSplashScreen = getSharedPreferences("fromSplashScreen", Context.MODE_PRIVATE)
        sharedShowPopup = getSharedPreferences("sharedShowPopup", Context.MODE_PRIVATE)
        val fromSplashScreen = sharedFromSplashScreen.getBoolean("fromSplashScreen", false)
        val showPopup = sharedShowPopup.getBoolean("sharedShowPopup", true)

        if (showPopup) {
            if (fromSplashScreen) {
                customOptionVipPopupAd()
            }
        }

        //endregion

        //region ====================================== Worker Thread =======================================

        main_mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        main_mDbWorkerThread.start()

        main_ContactsDatabase = ContactsRoomDatabase.getDatabase(this)

        //endregion

        //region ===================================== SetFavoriteList ======================================

        val intent = intent
        var fromStartActivity = intent.getBooleanExtra("fromStartActivity", false)

        if (fromStartActivity) {
            var counter = 0

            while (counter < main_ContactsDatabase!!.GroupsDao().getAllGroupsByNameAZ().size) {
                if (main_ContactsDatabase!!.GroupsDao()
                        .getAllGroupsByNameAZ()[counter].groupDB!!.name == "Favorites"
                ) {
                    var secondCounter = 0
                    while (secondCounter < main_ContactsDatabase!!.GroupsDao()
                            .getAllGroupsByNameAZ()[counter].getListContact(this).size
                    ) {
                        main_ContactsDatabase!!.GroupsDao()
                            .getAllGroupsByNameAZ()[counter].getListContact(this)[secondCounter].setIsFavorite(
                            main_ContactsDatabase
                        )

                        secondCounter++
                    }
                    break
                }
                counter++
            }

            if (importWhatsapp) {
                main_ContactsDatabase?.contactsDao()?.getAllContacts()
                    ?.let { importWhatsappContacts(it) }
            }
        }

        //endregion

        //region ======================================= FindViewById =======================================

        //Pour tous nos attributs qui sont des vues (TextView, listView , ConstraintLayout, ImageView etc) sur lesquelles notre activité agit nous les récupérons

        main_FloatingButtonAddNewContact =
            this.findViewById(R.id.main_floating_button_add_new_contact)
        main_FloatingButtonMultiChannel = findViewById(R.id.main_floating_button_multichannel)
        main_FloatingButtonMail = findViewById(R.id.main_gmail_button)
        main_FloatingButtonSMS = findViewById(R.id.main_sms_button)
        main_FloatingButtonGroup = findViewById(R.id.main_group_button)

        bottomNavigationView = findViewById(R.id.navigation)
        bottomNavigationView!!.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        main_SearchBar = findViewById(R.id.main_toolbar_search)
        main_constraintLayout = findViewById(R.id.constraintLyout)
        main_loadingPanel = findViewById(R.id.main_loadingPanel)

        main_GridView = findViewById(R.id.main_grid_view_id)
        main_RecyclerView = findViewById(R.id.main_recycler_view_id)

        val main_SettingsLeftDrawerLayout =
            findViewById<RelativeLayout>(R.id.settings_left_drawer_layout)

        //region ================================ Call Popup from LeftDrawer ================================

        val sharedPreferencePopup = getSharedPreferences("Phone_call", Context.MODE_PRIVATE)
        val settings_CallPopupSwitch = findViewById<SwitchCompat>(R.id.settings_call_popup_switch)

        settings_left_drawer_ThemeSwitch = findViewById(R.id.settings_left_drawer_theme_switch)

        if (sharedThemePreferences.getBoolean("darkTheme", false)) {
            settings_left_drawer_ThemeSwitch?.isChecked = true
        }

        if (sharedPreferencePopup.getBoolean("popup", true)) {
            settings_CallPopupSwitch?.isChecked = true
        }
        //endregion

        //endregion

        //region ========================================== Toolbar =========================================

        main_ToolbarLayout = findViewById(R.id.main_toolbar_layout)
        main_toolbar_Help = findViewById(R.id.main_toolbar_help)
        main_toolbar_OpenDrawer = findViewById(R.id.main_toolbar_open_drawer)
        main_ToolbarLayout = findViewById(R.id.main_toolbar_layout)
        main_ToolbarMultiSelectModeLayout = findViewById(R.id.main_toolbar_multi_select_mode_layout)
        main_ToolbarMultiSelectModeClose = findViewById(R.id.main_toolbar_multi_select_mode_close)
        main_ToolbarMultiSelectModeTitle = findViewById(R.id.main_toolbar_multi_select_mode_tv)
        main_ToolbarMultiSelectModeDelete = findViewById(R.id.main_toolbar_multi_select_mode_delete)
        main_ToolbarMultiSelectModeMenu = findViewById(R.id.main_toolbar_multi_select_mode_menu)

        val main_toolbar_Menu = findViewById<Toolbar>(R.id.main_toolbar_menu)
        setSupportActionBar(main_toolbar_Menu)
        main_toolbar_Menu.overflowIcon = getDrawable(R.drawable.ic_toolbar_menu)
        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(false)
        actionbar.setDisplayShowTitleEnabled(false)

        //endregion

        //region ======================================= DrawerLayout =======================================

        mainDrawerLayout = findViewById(R.id.drawer_layout)
        mainDrawerLayout?.addDrawerListener(this)

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        val menu = navigationView.menu

        val navItem = menu.findItem(R.id.nav_home)
        navItem.isChecked = true

        val navSyncContact = menu.findItem(R.id.nav_sync_contact)
        navSyncContact.isVisible = true

        val navInviteFriend = menu.findItem(R.id.nav_invite_friend)
        navInviteFriend.isVisible = true

        navigationView.setNavigationItemSelectedListener { menuItem ->
            mainDrawerLayout?.closeDrawers()
            when (menuItem.itemId) {
                R.id.nav_home -> startActivity(Intent(this@MainActivity, MainActivity::class.java))
                R.id.nav_notif_config -> startActivity(
                    Intent(
                        this@MainActivity,
                        ManageNotificationActivity::class.java
                    )
                )
                R.id.navigation_teleworking -> startActivity(
                    Intent(
                        this@MainActivity,
                        TeleworkingActivity::class.java
                    )
                )
                R.id.nav_settings -> startActivity(
                    Intent(
                        this@MainActivity,
                        SettingsActivity::class.java
                    )
                )
//                R.id.nav_go_to_kin -> {
//                    val openURL = Intent(Intent.ACTION_VIEW)
//                    openURL.data = Uri.parse("https://play.google.com/store/apps/details?id=com.yellowtwigs.Knockin.notification")
//                    startActivity(openURL)
//                }
                R.id.nav_in_app -> startActivity(
                    Intent(
                        this@MainActivity,
                        PremiumActivity::class.java
                    )
                )
                R.id.nav_manage_screen -> startActivity(
                    Intent(
                        this@MainActivity,
                        ManageMyScreenActivity::class.java
                    )
                )
                R.id.nav_help -> startActivity(Intent(this@MainActivity, HelpActivity::class.java))
            }

            true
        }

        //endregion

        //region ========================================= Runnable =========================================

        val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
        val len = sharedPreferences.getInt("gridview", 1)

        if (len <= 1) {
            main_GridView?.visibility = View.GONE
            main_RecyclerView?.visibility = View.VISIBLE
        } else {
            main_GridView?.visibility = View.VISIBLE
            main_RecyclerView?.visibility = View.GONE
        }

        if (position != 0) {
            main_GridView?.smoothScrollToPosition(position)
        }

        gestionnaireContacts = ContactManager(this.applicationContext)

        //region ===================================== set ListContact ======================================

        when {
            sharedPreferences.getString(
                "tri",
                "nom"
            ) == "lastname" -> gestionnaireContacts!!.sortContactByLastname()
            sharedPreferences.getString(
                "tri",
                "nom"
            ) == "nom" -> gestionnaireContacts!!.sortContactByFirstNameAZ()
            sharedPreferences.getString(
                "tri",
                "nom"
            ) == "priorite" -> gestionnaireContacts!!.sortContactByPriority()
            sharedPreferences.getString(
                "tri",
                "nom"
            ) == "favoris" -> gestionnaireContacts!!.sortContactByFavorite()
            else -> gestionnaireContacts!!.sortContactByFavorite()
        }

        if (main_GridView?.visibility != View.GONE) {
            gridViewAdapter = ContactGridViewAdapter(this, gestionnaireContacts!!, len)
            main_GridView?.adapter = gridViewAdapter
            main_GridView?.layoutManager = GridLayoutManager(this, len)
            main_GridView?.recycledViewPool?.setMaxRecycledViews(0, 0)

            val edit: SharedPreferences.Editor = sharedPreferences.edit()
            edit.apply()

            main_GridView?.setOnScrollChangeListener { _, _, _, _, _ ->
                if (gridViewAdapter != null) {
                    gridViewAdapter?.closeMenu()
                }
            }
        }

        if (main_RecyclerView?.visibility != View.GONE) {
            recyclerViewAdapter = ContactRecyclerViewAdapter(this, gestionnaireContacts!!, len)
            main_RecyclerView?.adapter = recyclerViewAdapter
            main_RecyclerView?.layoutManager = LinearLayoutManager(this)
            main_RecyclerView?.recycledViewPool?.setMaxRecycledViews(0, 0)

            if (position == 0) {
                val index = sharedPreferences.getInt("index", 0)
                val edit: SharedPreferences.Editor = sharedPreferences.edit()
                main_RecyclerView?.scrollToPosition(index)
                edit.putInt("index", 0)
                edit.apply()
            } else {
                main_RecyclerView?.layoutManager?.scrollToPosition(position)
            }

            main_RecyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy > 10) {
                        if (main_FloatingButtonAddNewContact?.visibility == View.VISIBLE) {
                            val disparition =
                                AnimationUtils.loadAnimation(baseContext, R.anim.disappear)
                            main_FloatingButtonAddNewContact?.startAnimation(disparition)
                            main_FloatingButtonAddNewContact?.visibility = View.GONE
                        }
                    } else if (dy < -10) {
                        if (main_FloatingButtonAddNewContact?.visibility == View.GONE) {
                            val apparition =
                                AnimationUtils.loadAnimation(baseContext, R.anim.reapparrition)
                            main_FloatingButtonAddNewContact?.startAnimation(apparition)
                            main_FloatingButtonAddNewContact?.visibility = View.VISIBLE
                        }
                    }
                }
            })
        }

        //endregion

        //endregion

        /**
         *  Partie du code qui permet de mettre en place des actions liées aux interactions de l'utilisateur
         */
        //region ======================================== Listeners =========================================

        settings_CallPopupSwitch?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val sharedCallPopupPreferences: SharedPreferences =
                    getSharedPreferences("Phone_call", Context.MODE_PRIVATE)
                val edit: SharedPreferences.Editor = sharedCallPopupPreferences.edit()
                edit.putBoolean("popup", true)
                edit.apply()
            } else {
                val sharedCallPopupPreferences: SharedPreferences =
                    getSharedPreferences("Phone_call", Context.MODE_PRIVATE)
                val edit: SharedPreferences.Editor = sharedCallPopupPreferences.edit()
                edit.putBoolean("popup", false)
                edit.apply()
            }
        }
        settings_left_drawer_ThemeSwitch?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                setTheme(R.style.AppThemeDark)
//                main_constraintLayout!!.setBackgroundResource(R.drawable.dark_background)
//                main_constraintLayout!!.setBackgroundResource(R.drawable.galactic_background)
                val edit: SharedPreferences.Editor = sharedThemePreferences.edit()
                edit.putBoolean("darkTheme", true)
                edit.apply()
                startActivity(Intent(this, MainActivity::class.java))
            } else {

                setTheme(R.style.AppTheme)
//                main_constraintLayout!!.setBackgroundResource(R.drawable.mr_white_blur_background)
                val edit: SharedPreferences.Editor = sharedThemePreferences.edit()
                edit.putBoolean("darkTheme", false)
                edit.apply()
                startActivity(Intent(this, MainActivity::class.java))
            }
        }

        navInviteFriend.setOnMenuItemClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            val messageString =
                getResources().getString(R.string.invite_friend_text) + " \n" + getResources().getString(
                    R.string.location_on_playstore
                )
            intent.putExtra(Intent.EXTRA_TEXT, messageString)
            intent.type = "text/plain"
            val messageIntent = Intent.createChooser(intent, null)
            startActivity(messageIntent)
            true
        }

        navSyncContact.setOnMenuItemClickListener {
            fromStartActivity = true

            mainDrawerLayout?.closeDrawers()
            main_GridView?.visibility = View.GONE
            main_RecyclerView?.visibility = View.GONE
            //check les permissions
            val sync = Runnable {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_CONTACTS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_CONTACTS),
                        PERMISSION_READ_CONTACT
                    )
                }
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_CONTACTS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    val displayLoading = Runnable {
                        main_loadingPanel?.visibility = View.VISIBLE
                    }
                    runOnUiThread(displayLoading)

                    gestionnaireContacts?.getAllContactsInfoSync(contentResolver)

                    val sharedPreferencesSync =
                        getSharedPreferences("save_last_sync", Context.MODE_PRIVATE)
                    var index = 1
                    var stringSet = listOf<String>()
                    if (sharedPreferencesSync.getStringSet(index.toString(), null) != null)
                        stringSet =
                            sharedPreferencesSync.getStringSet(index.toString(), null)!!.sorted()
                    val changedContactList = arrayListOf<Pair<ContactDB, List<ContactDetailDB>>>()
                    while (sharedPreferencesSync.getStringSet(
                            index.toString(),
                            null
                        ) != null && stringSet.isNotEmpty()
                    ) {
                        stringSet =
                            sharedPreferencesSync.getStringSet(index.toString(), null)!!.sorted()
                        changedContactList.add(gestionnaireContacts!!.setToContactList(stringSet))
                        index++
                    }

                    index = 1
                    val edit: SharedPreferences.Editor = sharedPreferencesSync.edit()
                    while (sharedPreferencesSync.getStringSet(
                            index.toString(),
                            null
                        ) != null && stringSet.isNotEmpty()
                    ) {
                        stringSet =
                            sharedPreferencesSync.getStringSet(index.toString(), null)!!.sorted()
                        edit.remove(index.toString())
                        index++
                    }
                    edit.apply()
                    val sharedPrefe = applicationContext.getSharedPreferences(
                        "Gridview_column",
                        Context.MODE_PRIVATE
                    )
                    if (sharedPrefe.getString("tri", "") == "priorite")
                        gestionnaireContacts?.sortContactByPriority()
                    else
                        gestionnaireContacts?.sortContactByFirstNameAZ()

                    val displaySync = Runnable {
                        main_loadingPanel?.visibility = View.GONE

                        if (len >= 4) {
                            main_GridView?.visibility = View.VISIBLE
                            gridViewAdapter?.setContactManager(gestionnaireContacts!!)
                            gridViewAdapter?.notifyDataSetChanged()
                        } else {
                            main_RecyclerView?.visibility = View.VISIBLE
                            recyclerViewAdapter?.setGestionnaireContact(gestionnaireContacts!!)
                            recyclerViewAdapter?.notifyDataSetChanged()
                        }

                        refreshActivity()
                        mainDrawerLayout?.closeDrawers()
                        importWhatsappContacts(
                            main_ContactsDatabase?.contactsDao()?.getAllContacts()!!
                        )
                    }
                    runOnUiThread(displaySync)
                }
            }
            main_mDbWorkerThread.postTask(sync)

            true
        }

        main_constraintLayout?.setOnTouchListener { _, _ ->
            val v = this@MainActivity.currentFocus
            val imm =
                this@MainActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (v != null) {
                imm.hideSoftInputFromWindow(v.windowToken, 0)
            }
            true
        }

        main_toolbar_OpenDrawer?.setOnClickListener {
            if (gridViewAdapter != null) {
                gridViewAdapter?.closeMenu()
            }
            mainDrawerLayout?.openDrawer(GravityCompat.START)
            hideKeyboard()
        }

        main_toolbar_Help?.setOnClickListener {
            if (Resources.getSystem().configuration.locale.language == "fr") {
                val browserIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.yellowtwigs.com/aide-en-ligne-contacts")
                )
                startActivity(browserIntent)
            } else {
                val browserIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.yellowtwigs.com/help-contacts")
                )
                startActivity(browserIntent)
            }
        }

        main_ToolbarMultiSelectModeClose?.setOnClickListener {
            Toast.makeText(this, R.string.main_toast_multi_select_deactived, Toast.LENGTH_SHORT)
                .show()
            startActivity(Intent(this@MainActivity, MainActivity::class.java))
            finish()
        }

        main_ToolbarMultiSelectModeMenu?.setOnClickListener {
            val popupMenu = PopupMenu(this, it)
            popupMenu.inflate(R.menu.toolbar_menu_main_multiselect_mode)

            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_main_toolbar_multiselect_all_select -> {
                        listOfItemSelected.clear()
                        listOfItemSelected.addAll(gestionnaireContacts!!.contactList)
                        if (len > 1) {
                            gridViewAdapter?.listOfItemSelected = listOfItemSelected
                            gridViewAdapter?.notifyDataSetChanged()
                        } else {
                            recyclerViewAdapter?.notifyDataSetChanged()
                            recyclerViewAdapter?.listOfItemSelected = listOfItemSelected
                        }
                        main_ToolbarMultiSelectModeTitle?.text =
                            listOfItemSelected.size.toString() + " " + getString(R.string.main_toast_multi_select_mode_selected_more_than_one)

                    }
                }
                false
            }
            popupMenu.show()
        }

        main_ToolbarMultiSelectModeDelete?.setOnClickListener {
            var suppressWarning = " "
            var nbVIP = 0

            val sharedNumberOfContactsVIPPreferences: SharedPreferences =
                getSharedPreferences("nb_Contacts_VIP", Context.MODE_PRIVATE)
            val nb_Contacts_VIP = sharedNumberOfContactsVIPPreferences.getInt("nb_Contacts_VIP", 0)

            if (listOfItemSelected.size > 1) {
                suppressWarning = " :"
                for (contact in listOfItemSelected) {
                    val contactDb = contact.contactDB
                    suppressWarning += "\n- " + contactDb!!.firstName + " " + contactDb.lastName

                    if (contactDb.contactPriority == 2) {
                        nbVIP++
                    }
                }
            } else {
                val contact = listOfItemSelected[0].contactDB
                suppressWarning += contact!!.firstName + " " + contact.lastName
            }
            MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                .setTitle(getString(R.string.main_alert_dialog_delete_contact_title))
                .setMessage(
                    String.format(
                        resources.getString(R.string.main_alert_dialog_delete_contact_message),
                        suppressWarning
                    )
                )
                .setPositiveButton(R.string.edit_contact_validate) { _, _ ->

                    val nbVIPTT = nb_Contacts_VIP - nbVIP

                    val edit: SharedPreferences.Editor = sharedNumberOfContactsVIPPreferences.edit()
                    edit.putInt("nb_Contacts_VIP", nbVIPTT)
                    edit.apply()

                    listOfItemSelected.forEach {
                        main_ContactsDatabase!!.contactsDao().deleteContactById(it.contactDB!!.id!!)
                    }
                    listOfItemSelected.clear()
                    switchMultiSelectToNormalMode()
                    refreshActivity()
                    finish()
                    overridePendingTransition(0, 0)
                    startActivity(getIntent())
                    overridePendingTransition(0, 0)
                }
                .setNegativeButton(R.string.delete_contact_from_group_cancel) { _, _ -> }
                .show()

        }

        main_SearchBar?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                charSequence: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
                if (gridViewAdapter != null) {
                    gridViewAdapter!!.closeMenu()
                }

                main_search_bar_value = main_SearchBar!!.text.toString()

                val sharedPref = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
                val length = sharedPref.getInt("gridview", 1)

                val filteredList = gestionnaireContacts!!.getContactConcernByFilter(
                    main_filter,
                    main_search_bar_value
                )
                val contactListDb = ContactManager(this@MainActivity)

                if (sharedPref.getString("tri", "nom") == "nom") {
                    contactListDb.sortContactByFirstNameAZ()
                    contactListDb.contactList.retainAll(filteredList)
                } else {
                    contactListDb.sortContactByPriority()
                    contactListDb.contactList.retainAll(filteredList)
                }
                gestionnaireContacts!!.contactList.clear()
                gestionnaireContacts!!.contactList.addAll(contactListDb.contactList)

                //en fonction de l'affichage on update soit la grid soit la list view
                if (length > 1) {
                    gridViewAdapter =
                        ContactGridViewAdapter(this@MainActivity, gestionnaireContacts!!, length)
                    main_GridView!!.adapter = gridViewAdapter
                } else {
                    recyclerViewAdapter = ContactRecyclerViewAdapter(
                        this@MainActivity,
                        gestionnaireContacts!!,
                        length
                    )
                    main_RecyclerView!!.adapter = recyclerViewAdapter
                }
            }
        })

        main_FloatingButtonAddNewContact?.setOnClickListener {
            startActivity(Intent(this@MainActivity, AddNewContactActivity::class.java))
        }

        main_FloatingButtonMultiChannel?.setOnClickListener {
            val intentToMultiChannelActivity =
                Intent(this@MainActivity, MultiChannelActivity::class.java)
            intentToMultiChannelActivity.putExtra("fromMainToMultiChannel", true)
            val iterator: IntIterator?
            val listOfIdContactSelected: ArrayList<Int> = ArrayList()

            iterator = (0 until listOfItemSelected.size).iterator()

            for (i in iterator) {
                listOfIdContactSelected.add(listOfItemSelected[i].getContactId())
            }
            intentToMultiChannelActivity.putIntegerArrayListExtra(
                "ListContactsSelected",
                listOfIdContactSelected
            )

            refreshActivity()
            startActivity(intentToMultiChannelActivity)
            finish()
        }

        main_FloatingButtonSMS?.setOnClickListener {
            val iterator: IntIterator?
            val listOfPhoneNumberContactSelected: ArrayList<String> = ArrayList()
            iterator = (0 until listOfItemSelected.size).iterator()
            for (i in iterator) {
                listOfPhoneNumberContactSelected.add(listOfItemSelected[i].getFirstPhoneNumber())
            }
            monoChannelSmsClick(listOfPhoneNumberContactSelected)
        }

        main_FloatingButtonMail?.setOnClickListener {
            val iterator: IntIterator?
            val listOfMailContactSelected: ArrayList<String> = ArrayList()
            iterator = (0 until listOfItemSelected.size).iterator()

            for (i in iterator) {
                listOfMailContactSelected.add(listOfItemSelected[i].getFirstMail())
            }
            monoChannelMailClick(listOfMailContactSelected)

            if (len >= 3) {
                gridViewAdapter =
                    ContactGridViewAdapter(this@MainActivity, gestionnaireContacts!!, len)
                main_GridView?.adapter = gridViewAdapter
                main_FloatingButtonAddNewContact?.visibility = View.VISIBLE
                main_FloatingButtonMultiChannel?.visibility = View.GONE

                main_FloatingButtonMail?.visibility = View.GONE
                main_FloatingButtonSMS?.visibility = View.GONE
                main_FloatingButtonGroup?.visibility = View.GONE
            } else {
                main_RecyclerView?.adapter =
                    ContactRecyclerViewAdapter(this@MainActivity, gestionnaireContacts!!, len)
                main_FloatingButtonAddNewContact?.visibility = View.VISIBLE
                main_FloatingButtonMultiChannel?.visibility = View.GONE

                main_FloatingButtonMail?.visibility = View.GONE
                main_FloatingButtonSMS?.visibility = View.GONE
                main_FloatingButtonGroup?.visibility = View.GONE
            }

            refreshActivity()
        }

        main_FloatingButtonGroup?.setOnClickListener {
            val iterator: IntIterator?
            val listOfContactSelected: ArrayList<ContactWithAllInformation> = ArrayList()

            iterator = (0 until listOfItemSelected.size).iterator()

            for (i in iterator) {
                listOfContactSelected.add(listOfItemSelected[i])
            }

            createGroupMultiSelectClick(listOfContactSelected)
        }

        //endregion

        if (fromStartActivity) {
            val sortByPriority = Runnable {
                gestionnaireContacts?.sortContactByPriority()
                val sharedDefaultTriPreferences =
                    getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
                val len = sharedDefaultTriPreferences.getInt("gridview", 1)
                val edit: SharedPreferences.Editor = sharedDefaultTriPreferences.edit()
                edit.putString("tri", "priorite")
                edit.apply()

                runOnUiThread {
                    if (len > 1) {
                        gridViewAdapter =
                            ContactGridViewAdapter(this@MainActivity, gestionnaireContacts!!, len)
                        main_GridView?.adapter = gridViewAdapter
                        main_GridView?.visibility = View.VISIBLE
                    } else {
                        recyclerViewAdapter = ContactRecyclerViewAdapter(
                            this@MainActivity,
                            gestionnaireContacts!!,
                            len
                        )
                        main_RecyclerView?.adapter = recyclerViewAdapter
                        main_RecyclerView?.visibility = View.VISIBLE
                    }
                    main_loadingPanel?.visibility = View.GONE
                }
            }
            main_mDbWorkerThread.postTask(sortByPriority)
            fromStartActivity = false
        }
        val sharedpopup_shop = getSharedPreferences("popup_shop", Context.MODE_PRIVATE)
        val shared_timestamp = sharedpopup_shop.getLong("popup_timestamp", 0)
        val shared_popup_response = sharedpopup_shop.getBoolean("popup_response", true)
        val edit: SharedPreferences.Editor = sharedpopup_shop.edit()
        val actual_timestamp = System.currentTimeMillis() / 1000
        val popup_shop = MaterialAlertDialogBuilder(this, R.style.AlertDialog)
            .setPositiveButton(R.string.popup_shop_yes) { _, _ ->
                edit.putBoolean("popup_response", true)
                edit.apply()
                startActivity(Intent(this@MainActivity, PremiumActivity::class.java))
            }
            .setNeutralButton(R.string.popup_shop_never) { _, _ ->
                edit.putBoolean("popup_response", false)
                edit.apply()
            }
            .setNegativeButton(R.string.popup_shop_later) { _, _ ->
                edit.putBoolean("popup_response", true)
                edit.apply()
            }
        if (shared_timestamp == 0L) {
//            popup_shop.show()
            val timestamp =
                System.currentTimeMillis() / 1000 + 300
            edit.putLong("popup_timestamp", timestamp)
            edit.apply()
        } else {
            if (shared_timestamp < actual_timestamp) {
                if (shared_popup_response) {
//                    popup_shop.show()
                    val timestamp = System.currentTimeMillis() / 1000 + 604800
                    edit.putLong("popup_timestamp", timestamp)
                    edit.apply()
                }
            }
        }
    }

    //region ========================================== Functions ===========================================

    private fun setContentView() {
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val height = size.y

        when {
            height > 2500 -> setContentView(R.layout.activity_main)
            height in 1800..2499 -> setContentView(R.layout.activity_main)
            height in 1100..1799 -> setContentView(R.layout.activity_main_smaller_screen)
            height < 1099 -> setContentView(R.layout.activity_main_mini_screen)
        }
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
    }

    /**
     *  Les affichages du mode Multiselect sont enlevés pour remettre l'affichage initial
     */
    private fun refreshActivity() {
        val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
        val len = sharedPreferences.getInt("gridview", 1)
        if (len > 1) {
            gridViewAdapter = ContactGridViewAdapter(this@MainActivity, gestionnaireContacts!!, len)
            main_GridView?.adapter = gridViewAdapter
        } else {
            recyclerViewAdapter =
                ContactRecyclerViewAdapter(this@MainActivity, gestionnaireContacts!!, len)
            main_RecyclerView?.adapter = recyclerViewAdapter
        }
    }

    /**
     * Comme [MainActivity.onCreate] méthode lancée à chaque lancement de l'activité
     * On vérifie quel est le dernier tri choisi par l'utilisateur et on l'affecte
     * @param item [MenuItem]
     * @return [Boolean]
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.toolbar_menu_filter_main, menu)
        val triNom = menu.findItem(R.id.tri_par_nom)
        val triLastName = menu.findItem(R.id.tri_par_lastname)
        val triPriority = menu.findItem(R.id.tri_par_priorite)
        val triFavorite = menu.findItem(R.id.tri_par_favoris)
        val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
        when (sharedPreferences.getString("tri", "priorite")) {
            "nom" -> triNom.isChecked = true
            "priorite" -> triPriority.isChecked = true
            "favoris" -> triFavorite.isChecked = true
            else -> triLastName.isChecked = true
        }
        return true
    }

    /**
     * Vérifie si les checkbox ont été check après une recherche
     * @param item [MenuItem]
     * @return [Boolean]
     */
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)
        val main_filter = intent.getStringArrayListExtra("Filter")
        if (main_filter != null && main_filter.contains("sms")) {
            menu.findItem(R.id.sms_filter)?.isChecked = true
            intent.putStringArrayListExtra("Filter", main_filter)
        }
        if (main_filter != null && main_filter.contains("mail")) {
            menu.findItem(R.id.mail_filter)?.isChecked = true
            intent.putStringArrayListExtra("Filter", main_filter)
        }
        return true
    }

    /**
     * Méthode qui lors de la sélection d'un item du menu effectue les filtres et tri séléctionnés dans le menu
     * @param item [MenuItem]
     * @return [Boolean]
     */
    @SuppressLint("ShowToast")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (gridViewAdapter != null) {
            gridViewAdapter!!.closeMenu()
        }
        when (item.itemId) {
            R.id.sms_filter -> {
                if (item.isChecked) {
                    item.isChecked = false
                    main_filter.remove("sms")
                    sortBySharedPref(ContactManager(this), item.isChecked)
                } else {
                    item.isChecked = true
                    main_filter.add("sms")
                    sortBySharedPref(ContactManager(this), item.isChecked)
                }
                return true
            }
            R.id.mail_filter -> {
                if (item.isChecked) {
                    item.isChecked = false
                    main_filter.remove("mail")
                    sortBySharedPref(ContactManager(this), item.isChecked)
                } else {
                    item.isChecked = true
                    main_filter.add("mail")
                    sortBySharedPref(ContactManager(this), item.isChecked)
                }
                return true
            }
            R.id.tri_par_nom -> {
                if (!item.isChecked) {
                    sortBy("name", item)
                }
            }
            R.id.tri_par_lastname -> {
                if (!item.isChecked) {
                    sortBy("lastname", item)
                }
            }
            R.id.tri_par_priorite -> {
                if (!item.isChecked) {
                    sortBy("priority", item)
                }
            }
            R.id.tri_par_favoris -> {
                if (!item.isChecked) {
                    sortBy("favorite", item)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Retourne si on a l'autorisation d'accès aux notifications
     * @return [Boolean]
     */
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
    }//TODO: enlever code duplicate

    /**
     * Stop le service et le relance en disant que celui-ci ne doit pas être arreter
     */
    private fun toggleNotificationListenerService() {
        val pm = packageManager
        val cmpName = ComponentName(this, NotificationListener::class.java)
        pm.setComponentEnabledSetting(
            cmpName,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
        pm.setComponentEnabledSetting(
            cmpName,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    /**
     *  Ferme la keyboard
     */
    private fun hideKeyboard() {
        // Check if no view has focus:
        val view = this.currentFocus

        view?.let { v ->
            val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }

    /**
     *  Cette region est composée d'un ensemble de méthodes qui sont appelés lors d'action sur le drawer
     */
    //region ======================================== Drawer Listener =======================================

    /**
     *Lors du changement d'état nous ne faisons rien
     * @param newState [Int]
     */
    override fun onDrawerStateChanged(newState: Int) {
    }

    /**
     * Lors de l'ouvertur du drawer par le slide nous fermons le circularMenu
     * @param drawerView [View]
     * @param slideOffset [Float]
     */
    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
        if (gridViewAdapter != null) {
            gridViewAdapter!!.closeMenu()
        }
    }

    /**
     * Lorsque le drawer est fermer on ne fait rien
     * @param drawerView [View]
     */
    override fun onDrawerClosed(drawerView: View) {
    }

    /**
     * Lors de l'ouvertur du drawer nous fermons le circularMenu
     * @param drawerView [View]
     * @param slideOffset [Float]
     */
    override fun onDrawerOpened(drawerView: View) {
        if (gridViewAdapter != null) {
            gridViewAdapter!!.closeMenu()
        }
    }

    //endregion

    /**
     *  Cette region est composée d'un ensemble de méthodes qui sont liées au Multiselect
     */
    //region ====================================== Multi-MonoChannel =======================================

    /**
     * Fonction Item Click, appelée dans GridViewAdapter, liée au click sur un contact
     * Cela permet d'activer le mode Multiselect et ensuite de sélectionner d'autres contacts
     * @param position [Int]
     */
    @SuppressLint("SetTextI18n")
    fun gridMultiSelectItemClick(position: Int) {
        main_FloatingButtonAddNewContact!!.visibility = View.GONE
        main_FloatingButtonMultiChannel!!.visibility = View.VISIBLE

        if (listOfItemSelected.contains(gestionnaireContacts!!.contactList[position])) {
            listOfItemSelected.remove(gestionnaireContacts!!.contactList[position])
        } else {
            listOfItemSelected.add(gestionnaireContacts!!.contactList[position])
        }

        verifiedContactsChannel(listOfItemSelected)

        if (listOfItemSelected.size == 0) {
            val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
            val len = sharedPreferences.getInt("gridview", 4)
            gridViewAdapter = ContactGridViewAdapter(this@MainActivity, gestionnaireContacts!!, len)
            main_GridView!!.layoutManager = GridLayoutManager(this, len)
            main_GridView!!.adapter = gridViewAdapter

            Toast.makeText(this, R.string.main_toast_multi_select_deactived, Toast.LENGTH_SHORT)
                .show()

            main_FloatingButtonAddNewContact!!.visibility = View.VISIBLE

            switchMultiSelectToNormalMode()
            main_FloatingButtonGroup!!.visibility = View.GONE
        } else if (listOfItemSelected.size == 1) {
            firstClick = false
            main_ToolbarMultiSelectModeLayout!!.visibility = View.VISIBLE
            main_ToolbarLayout!!.visibility = View.GONE
        }

        val i = listOfItemSelected.size

        if (listOfItemSelected.size == 1) {
            main_ToolbarMultiSelectModeTitle!!.text =
                i.toString() + " " + getString(R.string.main_toast_multi_select_mode_selected)
        } else if (listOfItemSelected.size > 1) {
            main_ToolbarMultiSelectModeTitle!!.text =
                i.toString() + " " + getString(R.string.main_toast_multi_select_mode_selected_more_than_one)
        }
    }

    /**
     * Fonction Item Click, appelée dans GridViewAdapter, liée au long click sur un contact
     * Cela permet d'activer le mode Multiselect et ensuite de sélectionner d'autres contacts
     * @param position [Int]
     */
    fun recyclerMultiSelectItemClick(position: Int) {
        if (listOfItemSelected.contains(gestionnaireContacts!!.contactList[position])) {
            listOfItemSelected.remove(gestionnaireContacts!!.contactList[position])
            verifiedContactsChannel(listOfItemSelected)
        } else {
            listOfItemSelected.add(gestionnaireContacts!!.contactList[position])

            main_FloatingButtonAddNewContact!!.visibility = View.GONE
            main_FloatingButtonMultiChannel!!.visibility = View.VISIBLE

            verifiedContactsChannel(listOfItemSelected)
        }

        val i = listOfItemSelected.size

        if (listOfItemSelected.size == 1 && firstClick) {
            Toast.makeText(this, R.string.main_toast_multi_select_actived, Toast.LENGTH_SHORT)
                .show()
            main_FloatingButtonAddNewContact!!.visibility = View.GONE
            main_FloatingButtonMultiChannel!!.visibility = View.VISIBLE
            firstClick = false
            multiChannelMode = true
            main_ToolbarMultiSelectModeLayout!!.visibility = View.VISIBLE
            main_ToolbarLayout!!.visibility = View.GONE

        } else if (listOfItemSelected.size == 0) {
            Toast.makeText(this, R.string.main_toast_multi_select_deactived, Toast.LENGTH_SHORT)
                .show()

            main_FloatingButtonAddNewContact!!.visibility = View.VISIBLE
            main_FloatingButtonMultiChannel!!.visibility = View.GONE
            main_FloatingButtonSMS!!.visibility = View.GONE
            main_FloatingButtonMail!!.visibility = View.GONE
            main_FloatingButtonGroup!!.visibility = View.GONE

            main_ToolbarMultiSelectModeLayout!!.visibility = View.GONE
            main_ToolbarLayout!!.visibility = View.VISIBLE

            firstClick = true
        }

        if (listOfItemSelected.size == 1) {
            main_ToolbarMultiSelectModeTitle!!.text =
                i.toString() + " " + getString(R.string.main_toast_multi_select_mode_selected)
        } else if (listOfItemSelected.size > 1) {
            main_ToolbarMultiSelectModeTitle!!.text =
                i.toString() + " " + getString(R.string.main_toast_multi_select_mode_selected_more_than_one)
        }
    }

    /**
     * On regarde dans la liste des contacts sélectionnés lors du multiselect tous les channels (SMS, Mail) présents
     * Si tous les contacts possèdent un channel commun, alors le bouton correspondant à ce Channel sera visible
     * Sinon il sera masqué
     * @param listOfItemSelected [ArrayList<ContactWithAllInformation>]
     */
    private fun verifiedContactsChannel(listOfItemSelected: ArrayList<ContactWithAllInformation>) {
        val iterator = (0 until listOfItemSelected.size).iterator()
        var allContactsHaveMail = true
        var allContactsHavePhoneNumber = true

        for (i in iterator) {
            if (listOfItemSelected[i].getFirstMail() == "") {
                allContactsHaveMail = false
            }

            if (listOfItemSelected[i].getFirstPhoneNumber() == "") {
                allContactsHavePhoneNumber = false
            }
        }
        var i = 2
        val metrics = DisplayMetrics()
        this.windowManager.defaultDisplay.getMetrics(metrics)
        val margin = (0.5 * metrics.densityDpi).toInt()
        println("metric smartphone" + metrics.densityDpi)
        if (allContactsHavePhoneNumber) {
            main_FloatingButtonSMS!!.visibility = View.VISIBLE
            i++
        } else {
            println("false phoneNumber")
            main_FloatingButtonSMS!!.visibility = View.GONE
        }
        if (allContactsHaveMail) {
            main_FloatingButtonMail!!.visibility = View.VISIBLE
            val params: ViewGroup.MarginLayoutParams =
                main_FloatingButtonMail!!.layoutParams as ViewGroup.MarginLayoutParams
            params.bottomMargin = margin * i
            main_FloatingButtonMail!!.layoutParams = params
            println("height of floating mail" + main_FloatingButtonMail!!.height)
            i++
        } else {
            println("false mail")
            main_FloatingButtonMail!!.visibility = View.GONE
        }
        main_FloatingButtonGroup!!.visibility = View.VISIBLE
        val params: ViewGroup.MarginLayoutParams =
            main_FloatingButtonGroup!!.layoutParams as ViewGroup.MarginLayoutParams
        params.bottomMargin = margin * i
        main_FloatingButtonGroup!!.layoutParams = params

    }

    /**
     * On ouvre l'application de SMS avec tous les contacts saisis lors du multiselect
     * @param listOfPhoneNumber [ArrayList<String>]
     */
    private fun monoChannelSmsClick(listOfPhoneNumber: ArrayList<String>) {

        var message = "smsto:" + listOfPhoneNumber[0]
        for (i in 0 until listOfPhoneNumber.size) {
            message += ";" + listOfPhoneNumber[i]
        }
        startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse(message)))
    }

    /**
     * On ouvre l'application de mail choisi par l'utilisateur avec tous les contacts saisis lors du multiselect
     * @param listOfMail [ArrayList<String>]
     */
    private fun monoChannelMailClick(listOfMail: ArrayList<String>) {
        val contact = listOfMail.toArray(arrayOfNulls<String>(listOfMail.size))
        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(
            Intent.EXTRA_EMAIL,
            contact
        )/*listOfMail.toArray(new String[listOfMail.size()]*/
        intent.data = Uri.parse("mailto:")
        intent.type = "message/rfc822"
        intent.putExtra(Intent.EXTRA_SUBJECT, "")
        intent.putExtra(Intent.EXTRA_TEXT, "")
        startActivity(intent)
    }

    /**
     * Nous affichons ici une Popup de création de groupe qui nous demande d'entrer le nom du groupe et sa couleur
     * Une fois validé, nous enregistrons le groupe et ses contacts dans la BDD
     * @param listContacts[ArrayList<ContactWithAllInformation>]
     * @param len [Int]
     */
    private fun createGroupMultiSelectClick(listContacts: ArrayList<ContactWithAllInformation>) {

        val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        @SuppressLint("InflateParams") val alertView =
            inflater.inflate(R.layout.alert_dialog_edit_group, null, true)

        val mainCreateGroupAlertDialogTitle =
            alertView.findViewById<TextView>(R.id.manager_group_edit_group_alert_dialog_title)
        val mainCreateGroupAlertDialogEditText =
            alertView.findViewById<AppCompatEditText>(R.id.manager_group_edit_group_view_edit)

        val mainCreateGroupAlertDialogRedTag =
            alertView.findViewById<AppCompatImageView>(R.id.manager_group_edit_group_color_red)
        val mainCreateGroupAlertDialogBlueTag =
            alertView.findViewById<AppCompatImageView>(R.id.manager_group_edit_group_color_blue)
        val mainCreateGroupAlertDialogGreenTag =
            alertView.findViewById<AppCompatImageView>(R.id.manager_group_edit_group_color_green)
        val mainCreateGroupAlertDialogYellowTag =
            alertView.findViewById<AppCompatImageView>(R.id.manager_group_edit_group_color_yellow)
        val mainCreateGroupAlertDialogOrangeTag =
            alertView.findViewById<AppCompatImageView>(R.id.manager_group_edit_group_color_orange)
        val mainCreateGroupAlertDialogPurpleTag =
            alertView.findViewById<AppCompatImageView>(R.id.manager_group_edit_group_color_purple)

        // Titre de la Popup
        mainCreateGroupAlertDialogTitle.text =
            this.getString(R.string.main_alert_dialog_group_title)

        // Text hint de l'EditText
        mainCreateGroupAlertDialogEditText.hint = getString(R.string.group_name).format(
            main_ContactsDatabase!!.GroupsDao().getIdNeverUsed()
        )

        mainCreateGroupAlertDialogRedTag.setImageResource(R.drawable.border_selected_yellow)
        var color = ResourcesCompat.getColor(resources, R.color.red_tag_group, null)

        //region ================================= ClickListenerOnColorTag ==================================

        mainCreateGroupAlertDialogRedTag.setOnClickListener {
            mainCreateGroupAlertDialogRedTag.setImageResource(R.drawable.border_selected_yellow)
            mainCreateGroupAlertDialogBlueTag.setImageResource(android.R.color.transparent)
            mainCreateGroupAlertDialogGreenTag.setImageResource(android.R.color.transparent)
            mainCreateGroupAlertDialogYellowTag.setImageResource(android.R.color.transparent)
            mainCreateGroupAlertDialogOrangeTag.setImageResource(android.R.color.transparent)
            mainCreateGroupAlertDialogPurpleTag.setImageResource(android.R.color.transparent)

            color = ResourcesCompat.getColor(resources, R.color.red_tag_group, null)
        }

        mainCreateGroupAlertDialogBlueTag.setOnClickListener {
            mainCreateGroupAlertDialogBlueTag.setImageResource(R.drawable.border_selected_yellow)
            mainCreateGroupAlertDialogRedTag.setImageResource(android.R.color.transparent)
            mainCreateGroupAlertDialogGreenTag.setImageResource(android.R.color.transparent)
            mainCreateGroupAlertDialogYellowTag.setImageResource(android.R.color.transparent)
            mainCreateGroupAlertDialogOrangeTag.setImageResource(android.R.color.transparent)
            mainCreateGroupAlertDialogPurpleTag.setImageResource(android.R.color.transparent)

            color = ResourcesCompat.getColor(resources, R.color.blue_tag_group, null)
        }

        mainCreateGroupAlertDialogGreenTag.setOnClickListener {
            mainCreateGroupAlertDialogGreenTag.setImageResource(R.drawable.border_selected_yellow)
            mainCreateGroupAlertDialogRedTag.setImageResource(android.R.color.transparent)
            mainCreateGroupAlertDialogBlueTag.setImageResource(android.R.color.transparent)
            mainCreateGroupAlertDialogYellowTag.setImageResource(android.R.color.transparent)
            mainCreateGroupAlertDialogOrangeTag.setImageResource(android.R.color.transparent)
            mainCreateGroupAlertDialogPurpleTag.setImageResource(android.R.color.transparent)

            color = ResourcesCompat.getColor(resources, R.color.green_tag_group, null)
        }

        mainCreateGroupAlertDialogYellowTag.setOnClickListener {
            mainCreateGroupAlertDialogYellowTag.setImageResource(R.drawable.border_selected_yellow)
            mainCreateGroupAlertDialogRedTag.setImageResource(android.R.color.transparent)
            mainCreateGroupAlertDialogGreenTag.setImageResource(android.R.color.transparent)
            mainCreateGroupAlertDialogBlueTag.setImageResource(android.R.color.transparent)
            mainCreateGroupAlertDialogOrangeTag.setImageResource(android.R.color.transparent)
            mainCreateGroupAlertDialogPurpleTag.setImageResource(android.R.color.transparent)

            color = ResourcesCompat.getColor(resources, R.color.yellow_tag_group, null)
        }

        mainCreateGroupAlertDialogOrangeTag.setOnClickListener {
            mainCreateGroupAlertDialogOrangeTag.setImageResource(R.drawable.border_selected_yellow)
            mainCreateGroupAlertDialogRedTag.setImageResource(android.R.color.transparent)
            mainCreateGroupAlertDialogGreenTag.setImageResource(android.R.color.transparent)
            mainCreateGroupAlertDialogYellowTag.setImageResource(android.R.color.transparent)
            mainCreateGroupAlertDialogBlueTag.setImageResource(android.R.color.transparent)
            mainCreateGroupAlertDialogPurpleTag.setImageResource(android.R.color.transparent)

            color = ResourcesCompat.getColor(resources, R.color.orange_tag_group, null)
        }

        mainCreateGroupAlertDialogPurpleTag.setOnClickListener { _ ->
            mainCreateGroupAlertDialogPurpleTag.setImageResource(R.drawable.border_selected_yellow)
            mainCreateGroupAlertDialogRedTag.setImageResource(android.R.color.transparent)
            mainCreateGroupAlertDialogGreenTag.setImageResource(android.R.color.transparent)
            mainCreateGroupAlertDialogYellowTag.setImageResource(android.R.color.transparent)
            mainCreateGroupAlertDialogOrangeTag.setImageResource(android.R.color.transparent)
            mainCreateGroupAlertDialogBlueTag.setImageResource(android.R.color.transparent)

            color = ResourcesCompat.getColor(resources, R.color.purple_tag_group, null)
        }

        //endregion

        MaterialAlertDialogBuilder(this, R.style.AlertDialog)
            .setView(alertView)
            .setPositiveButton(R.string.alert_dialog_validate) { _, _ ->
                if (mainCreateGroupAlertDialogEditText.text!!.toString() == "Favorites" || mainCreateGroupAlertDialogEditText.text!!.toString() == "Favorites") {
                    hideKeyboard()
                    Toast.makeText(
                        this,
                        getString(R.string.main_alert_dialog_group_favorites_already_exist),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    val nameGroup =
                        if (mainCreateGroupAlertDialogEditText.text.toString().isNotEmpty()) {
                            mainCreateGroupAlertDialogEditText.text.toString()
                        } else {
                            mainCreateGroupAlertDialogEditText.hint.toString()
                        }
                    val executorService: ExecutorService = Executors.newFixedThreadPool(1)
                    val callDb = Callable {
                        if (listContacts.size != 0) {
                            val group = GroupDB(null, nameGroup, "", -500138)
                            idGroup = main_ContactsDatabase?.GroupsDao()!!.insert(group)!!
                            for (contact in listContacts) {
                                val link = LinkContactGroup(idGroup.toInt(), contact.getContactId())
                                main_ContactsDatabase?.LinkContactGroupDao()!!.insert(link)
                            }
                        }
                    }
                    executorService.submit(callDb).get()!!

                    if (color == 0) {
                        val r = Random()
                        when (r.nextInt(7)) {
                            0 -> {
                                color = this.getColor(R.color.red_tag_group)
                                color = this.getColor(R.color.blue_tag_group)
                                color = this.getColor(R.color.green_tag_group)
                                color = this.getColor(R.color.orange_tag_group)
                                color = this.getColor(R.color.yellow_tag_group)
                                color = this.getColor(R.color.purple_tag_group)
                                color = this.getColor(R.color.red_tag_group)
                                color = this.getColor(R.color.blue_tag_group)
                            }
                            1 -> {
                                color = this.getColor(R.color.blue_tag_group)
                                color = this.getColor(R.color.green_tag_group)
                                color = this.getColor(R.color.orange_tag_group)
                                color = this.getColor(R.color.yellow_tag_group)
                                color = this.getColor(R.color.purple_tag_group)
                                color = this.getColor(R.color.red_tag_group)
                                color = this.getColor(R.color.blue_tag_group)
                            }
                            2 -> {
                                color = this.getColor(R.color.green_tag_group)
                                color = this.getColor(R.color.orange_tag_group)
                                color = this.getColor(R.color.yellow_tag_group)
                                color = this.getColor(R.color.purple_tag_group)
                                color = this.getColor(R.color.red_tag_group)
                                color = this.getColor(R.color.blue_tag_group)
                            }
                            3 -> {
                                color = this.getColor(R.color.orange_tag_group)
                                color = this.getColor(R.color.yellow_tag_group)
                                color = this.getColor(R.color.purple_tag_group)
                                color = this.getColor(R.color.red_tag_group)
                                color = this.getColor(R.color.blue_tag_group)
                            }
                            4 -> {
                                color = this.getColor(R.color.yellow_tag_group)
                                color = this.getColor(R.color.purple_tag_group)
                                color = this.getColor(R.color.red_tag_group)
                                color = this.getColor(R.color.blue_tag_group)
                            }
                            5 -> {
                                color = this.getColor(R.color.purple_tag_group)
                                color = this.getColor(R.color.red_tag_group)
                                color = this.getColor(R.color.blue_tag_group)
                            }
                            6 -> {
                                color = this.getColor(R.color.red_tag_group)
                                color = this.getColor(R.color.blue_tag_group)
                            }
                            else -> color = this.getColor(R.color.blue_tag_group)
                        }
                    }
                    hideKeyboard()
                    main_ContactsDatabase!!.GroupsDao()
                        .updateGroupSectionColorById(idGroup.toInt(), color)
                    startActivity(Intent(this, GroupManagerActivity::class.java))
                }
            }
            .setNegativeButton(R.string.alert_dialog_cancel) { _, _ ->
                hideKeyboard()
            }
            .show()
    }

    override fun onRestart() {
        super.onRestart()
        hideKeyboard()
        startActivity(Intent(this@MainActivity, MainActivity::class.java))
    }

    //endregion

    /**
     * Méthode appelée par le système lorsque l'utilisateur accepte ou refuse une permission
     * Lorsque l'utilisateur autorise de lire ses contacts, nous synchronisons ses contacts Android
     * Lorsque l'utilisateur autorise d'appeler avec Knockin, alors nous passons l'appel qu'il voulait passer avant d'accepter la permission
     * @param requestCode [Int]
     * @param permissions [Array<String>]
     * @param grantResults [IntArray]
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {

            PERMISSION_CALL_RESULT -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (main_GridView?.visibility == View.VISIBLE) {
                        callPhone(gridViewAdapter?.phonePermission.toString(), this)
                    } else {
                        Toast.makeText(
                            this,
                            "Can't do anything until you permit me !",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            PERMISSION_READ_CONTACT -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                println("permission accept")
                gestionnaireContacts!!.getAllContactsInfoSync(contentResolver)
                recreate()
            }
        }
    }

    /**
     *Passer de l'affichage multiselect au mode normal en changeant le laytout
     */
    private fun switchMultiSelectToNormalMode() {
        main_FloatingButtonMultiChannel?.visibility = View.GONE
        main_ToolbarMultiSelectModeLayout?.visibility = View.GONE
        main_ToolbarLayout?.visibility = View.VISIBLE
        main_FloatingButtonMail?.visibility = View.GONE
        main_FloatingButtonSMS?.visibility = View.GONE
        main_FloatingButtonGroup?.visibility = View.GONE
    }

    private fun importWhatsappContacts(listContact: List<ContactDB>) {
        val cr = contentResolver
        val contactCursor = cr.query(
            ContactsContract.RawContacts.CONTENT_URI,
            arrayOf(
                ContactsContract.RawContacts._ID,
                ContactsContract.RawContacts.CONTACT_ID
            ),
            ContactsContract.RawContacts.ACCOUNT_TYPE + "= ?",
            arrayOf("com.whatsapp"),
            null
        )

        val myWhatsappContacts = ArrayList<String>()

        if (contactCursor != null) {
            if (contactCursor.count > 0) {
                if (contactCursor.moveToFirst()) {
                    do {
                        val whatsappContactId =
                            contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.RawContacts.CONTACT_ID))

                        if (whatsappContactId != null) {
                            val whatsAppContactCursor = cr.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                arrayOf(
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                                ),
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                arrayOf(whatsappContactId), null
                            );

                            if (whatsAppContactCursor != null) {
                                whatsAppContactCursor.moveToFirst()
                                val name = whatsAppContactCursor.getString(
                                    whatsAppContactCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                                )
                                val number = whatsAppContactCursor.getString(
                                    whatsAppContactCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                )

                                whatsAppContactCursor.close()

                                myWhatsappContacts.add(number)

                                for (contact in listContact) {
                                    if (contact.firstName + " " + contact.lastName == name || contact.firstName == name || contact.lastName == name) {

                                        main_ContactsDatabase!!.contactsDao()
                                            .getContact(contact.id!!)
                                            .setHasWhatsapp(main_ContactsDatabase)

                                        val detail = ContactDetailDB(
                                            null,
                                            contact.id,
                                            number,
                                            "phone",
                                            "",
                                            0
                                        )
                                        main_ContactsDatabase!!.contactDetailsDao().insert(detail)
                                    }
                                }
                            }
                        }
                    } while (contactCursor.moveToNext())
                    contactCursor.close()
                }
            }
        }
    }

    private fun sortBySharedPref(contactListDb: ContactManager, isChecked: Boolean) {
        main_search_bar_value = main_SearchBar!!.text.toString()
        val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
        val len = sharedPreferences.getInt("gridview", 4)
        val filteredContact =
            gestionnaireContacts!!.getContactConcernByFilter(main_filter, main_search_bar_value)
        when {
            sharedPreferences.getString(
                "tri",
                "nom"
            ) == "prenom" -> contactListDb.sortContactByFirstNameAZ()
            sharedPreferences.getString(
                "tri",
                "nom"
            ) == "lastname" -> contactListDb.sortContactByLastname()
            sharedPreferences.getString(
                "tri",
                "nom"
            ) == "priorite" -> contactListDb.sortContactByPriority()
            sharedPreferences.getString(
                "tri",
                "nom"
            ) == "favoris" -> contactListDb.sortContactByFavorite()
            else -> contactListDb.sortContactByGroup()
        }

        if (isChecked) {
            contactListDb.contactList.retainAll(filteredContact)
            gestionnaireContacts!!.contactList.clear()
            gestionnaireContacts!!.contactList.addAll(contactListDb.contactList)
        } else {
            gestionnaireContacts!!.contactList.retainAll(filteredContact)
        }

        if (len > 1) {
            gridViewAdapter = ContactGridViewAdapter(this@MainActivity, gestionnaireContacts!!, len)
            main_GridView!!.adapter = gridViewAdapter
        } else {
            recyclerViewAdapter =
                ContactRecyclerViewAdapter(this@MainActivity, gestionnaireContacts!!, len)
            main_RecyclerView!!.adapter = recyclerViewAdapter
        }
        refreshActivity()
    }

    private fun sortBy(keyword: String, item: MenuItem) {
        main_GridView?.visibility = View.GONE
        main_RecyclerView?.visibility = View.GONE
        main_loadingPanel?.visibility = View.VISIBLE

        val sortBy = Runnable {
            val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
            val len = sharedPreferences.getInt("gridview", 1)
            val edit: SharedPreferences.Editor = sharedPreferences.edit()

            when (keyword) {
                "name" -> {
                    gestionnaireContacts!!.sortContactByFirstNameAZ()
                    edit.putString("tri", "prenom")
                    edit.apply()
                }
                "lastname" -> {
                    gestionnaireContacts!!.sortContactByLastname()
                    edit.putString("tri", "lastname")
                    edit.apply()
                }
                "priority" -> {
                    gestionnaireContacts!!.sortContactByPriority()
                    edit.putString("tri", "priorite")
                    edit.apply()
                }
                "favorite" -> {
                    gestionnaireContacts!!.sortContactByFavorite()
                    edit.putString("tri", "favoris")
                    edit.apply()
                }
            }

            runOnUiThread {
                item.isChecked = true
                if (len > 1) {
                    gridViewAdapter =
                        ContactGridViewAdapter(this@MainActivity, gestionnaireContacts!!, len)
                    main_GridView!!.adapter = gridViewAdapter
                    main_GridView!!.visibility = View.VISIBLE
                } else {
                    recyclerViewAdapter =
                        ContactRecyclerViewAdapter(this@MainActivity, gestionnaireContacts!!, len)
                    main_RecyclerView!!.adapter = recyclerViewAdapter
                    main_RecyclerView!!.visibility = View.VISIBLE
                }
                main_loadingPanel!!.visibility = View.GONE
            }
        }
        main_mDbWorkerThread.postTask(sortBy)
        refreshActivity()
    }

    private fun customOptionVipPopupAd() {
        val edit = sharedFromSplashScreen.edit()
        edit.putBoolean("fromSplashScreen", false)
        edit.apply()

        MaterialAlertDialogBuilder(this, R.style.AlertDialog)
            .setView(R.layout.custom_alert_dialog_vip_notif_ad)
            .setPositiveButton(R.string.show_popup_positive_button) { alertDialog, _ ->
                val edit1 = sharedShowPopup.edit()
                edit1.putBoolean("sharedShowPopup", false)
                edit1.apply()
                alertDialog.dismiss()
                alertDialog.cancel()
            }.setNegativeButton(R.string.alert_dialog_close) { alertDialog, _ ->
                alertDialog.dismiss()
                alertDialog.cancel()
            }
            .show()
    }

//endregion
}