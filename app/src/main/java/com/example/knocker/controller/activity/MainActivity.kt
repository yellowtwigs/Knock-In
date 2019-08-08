package com.example.knocker.controller.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.*
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.core.app.ActivityCompat
import androidx.appcompat.widget.Toolbar
import android.text.TextUtils
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.view.*
import android.view.animation.AnimationUtils
import android.view.animation.TranslateAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.knocker.*
import com.example.knocker.controller.*
import com.example.knocker.controller.activity.firstLaunch.FirstLaunchActivity
import com.example.knocker.model.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder

import com.example.knocker.model.ModelDB.*
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.example.knocker.controller.activity.group.GroupActivity
import com.example.knocker.controller.activity.group.GroupManagerActivity
import kotlin.collections.ArrayList

/**
 * La Classe qui permet d'afficher la searchbar, les filtres, la gridview, les floatings buttons dans la page des contacts
 * @author Florian Striebel, Kenzy Suon, Ryan Granet
 */
@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class MainActivity : AppCompatActivity(), DrawerLayout.DrawerListener {

    //region ========================================== Var or Val ==========================================

    // Show on the Main Layout
    private var drawerLayout: DrawerLayout? = null

    private var main_GridView: GridView? = null
    private var main_RecyclerView: RecyclerView? = null

    private var main_FloatingButtonAdd: FloatingActionButton? = null
    private var main_FloatingButtonSend: FloatingActionButton? = null

    private var main_SMSButton: FloatingActionButton? = null
    private var main_MailButton: FloatingActionButton? = null

    internal var main_search_bar_value = ""
    private var main_filter = arrayListOf<String>()
    private var main_SearchBar: EditText? = null
    private var main_ToolbarLayout: ConstraintLayout? = null

    private var main_ToolbarMultiSelectModeLayout: RelativeLayout? = null
    private var main_ToolbarMultiSelectModeClose: AppCompatImageView? = null
    private var main_ToolbarMultiSelectModeTitle: TextView? = null
    private var main_ToolbarMultiSelectModeDelete: AppCompatImageView? = null
    private var main_ToolbarMultiSelectModeMenu: AppCompatImageView? = null

    // Database && Thread
    private var main_ContactsDatabase: ContactsRoomDatabase? = null
    private lateinit var main_mDbWorkerThread: DbWorkerThread

    private var main_BottomNavigationView: BottomNavigationView? = null

    private var gestionnaireContacts: ContactList? = null
    private var gridViewAdapter: ContactGridViewAdapter? = null
    private var recyclerViewAdapter: ContactRecyclerViewAdapter? = null
    private var main_layout: LinearLayout? = null
    private var main_loadingPanel: RelativeLayout? = null

    private var listOfItemSelected: ArrayList<ContactWithAllInformation> = ArrayList()

    private var firstClick: Boolean = true
    private var multiChannelMode: Boolean = false

    private val PERMISSION_CALL_RESULT = 1

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_contacts -> {
            }
            R.id.navigation_groups -> {
                startActivity(Intent(this@MainActivity, GroupActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifcations -> {
                startActivity(Intent(this@MainActivity, NotificationHistoryActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_phone_keyboard -> {
                startActivity(Intent(this@MainActivity, PhoneLogActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    //endregion

    /**
     * @param Bundle @type
     */

    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("ClickableViewAccessibility", "InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //region ======================================== Theme Dark ========================================

        val sharedThemePreferences = getSharedPreferences("Knocker_Theme", Context.MODE_PRIVATE)
        if (sharedThemePreferences.getBoolean("darkTheme", false)) {
            setTheme(R.style.AppThemeDark)
        } else {
            setTheme(R.style.AppTheme)
        }

        //endregion

        setContentView(R.layout.activity_main)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT

        val sharedFirstLaunch = getSharedPreferences("FirstLaunch", Context.MODE_PRIVATE)
        if (sharedFirstLaunch.getBoolean("first_launch", true)) {
            startActivity(Intent(this@MainActivity, FirstLaunchActivity::class.java))
            finish()
        }

        val decorView = window.decorView
//        val window = window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }

        val isDelete = intent.getBooleanExtra("isDelete", false)
        if (isDelete) {
            Toast.makeText(this, R.string.main_toast_delete_contact, Toast.LENGTH_LONG).show()
        }

        if (isNotificationServiceEnabled()) {
            toggleNotificationListenerService()
        }

        //region ====================================== Worker Thread =======================================

        // on init WorkerThread
        main_mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        main_mDbWorkerThread.start()

        //endregion

        //on get la base de données
        main_ContactsDatabase = ContactsRoomDatabase.getDatabase(this)

        //region ======================================= FindViewById =======================================

        main_FloatingButtonAdd = findViewById(R.id.main_floating_button_open_id)

        main_FloatingButtonSend = findViewById(R.id.main_floating_button_send_id)

        main_BottomNavigationView = findViewById(R.id.navigation)

        main_BottomNavigationView!!.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        main_SearchBar = findViewById(R.id.main_search_bar)
        main_layout = findViewById(R.id.content_frame)
        main_loadingPanel = findViewById(R.id.loadingPanel)

        main_MailButton = findViewById(R.id.main_gmail_button)
        main_SMSButton = findViewById(R.id.main_sms_button)

        main_ToolbarLayout = findViewById(R.id.main_toolbar_layout)

        main_ToolbarMultiSelectModeLayout = findViewById(R.id.main_toolbar_multi_select_mode_layout)
        main_ToolbarMultiSelectModeClose = findViewById(R.id.main_toolbar_multi_select_mode_close)
        main_ToolbarMultiSelectModeTitle = findViewById(R.id.main_toolbar_multi_select_mode_tv)
        main_ToolbarMultiSelectModeDelete = findViewById(R.id.main_toolbar_multi_select_mode_delete)
        main_ToolbarMultiSelectModeMenu = findViewById(R.id.main_toolbar_multi_select_mode_menu)

        //endregion

        //region ========================================== Toolbar =========================================

        val toolbar = findViewById<Toolbar>(R.id.main_toolbar)
        setSupportActionBar(toolbar)
        toolbar.overflowIcon = getDrawable(R.drawable.ic_toolbar_menu)
        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar.setHomeAsUpIndicator(R.drawable.ic_open_drawer)
        actionbar.title = ""
        actionbar.setBackgroundDrawable(ColorDrawable(Color.parseColor("#ffffff")))

        //endregion

        //region ======================================= DrawerLayout =======================================

        drawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout!!.addDrawerListener(this)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        val menu = navigationView.menu
        val navItem = menu.findItem(R.id.nav_home)
        navItem.isChecked = true
        val navSyncContact = menu.findItem(R.id.nav_sync_contact)
        navSyncContact.isVisible = true

        navigationView!!.menu.getItem(0).isChecked = true

        navigationView.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            drawerLayout!!.closeDrawers()

            when (menuItem.itemId) {
                R.id.nav_home -> startActivity(Intent(this@MainActivity, MainActivity::class.java))
                R.id.nav_groups -> startActivity(Intent(this@MainActivity, GroupManagerActivity::class.java))
                R.id.nav_informations -> startActivity(Intent(this@MainActivity, EditInformationsActivity::class.java))
                R.id.nav_messenger -> startActivity(Intent(this@MainActivity, MessengerActivity::class.java))
                R.id.nav_notif_config -> startActivity(Intent(this@MainActivity, ManageNotificationActivity::class.java))
                R.id.nav_settings -> startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                R.id.nav_manage_screen -> startActivity(Intent(this@MainActivity, ManageMyScreenActivity::class.java))
//                R.id.nav_data_access ->
                R.id.nav_knockons -> startActivity(Intent(this@MainActivity, ManageKnockonsActivity::class.java))
//                R.id.nav_statistics ->
                R.id.nav_help -> startActivity(Intent(this@MainActivity, HelpActivity::class.java))
            }

            val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
            drawer.closeDrawer(GravityCompat.START)
            true
        }

        //endregion

        //region ========================================= Runnable =========================================

        //affiche tout les contacts de la Database

        main_GridView = findViewById(R.id.main_grid_view_id)
        main_RecyclerView = findViewById(R.id.main_recycler_view_id)

        //region commentaire
//        val listParams = main_GridView!!.layoutParams
//        if (!hasMenuKey && !hasBackKey) {
//            listParams.height = height - main_BottomNavigationView!!.getMeasuredHeight() - resources.getDimensionPixelSize(getResources().getIdentifier("navigation_bar_height", "dimen", "android")) - getResources().getDimensionPixelSize(getResources().getIdentifier("status_bar_height", "dimen", "android"))
//        } else {
//            listParams.height = height - main_BottomNavigationView!!.getMeasuredHeight() - getResources().getDimensionPixelSize(getResources().getIdentifier("status_bar_height", "dimen", "android"))
//        }
//        main_ListView!!.layoutParams = listParams

        //endregion
        val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
        val len = sharedPreferences.getInt("gridview", 4)

        if (len <= 1) {
            main_GridView!!.visibility = View.GONE
            main_RecyclerView!!.visibility = View.VISIBLE
        } else {
            main_RecyclerView!!.visibility = View.GONE
            main_GridView!!.visibility = View.VISIBLE
        }

        main_GridView!!.numColumns = len // permet de changer
        gestionnaireContacts = ContactList(this.applicationContext)

        if (main_GridView != null) {
            if (sharedPreferences.getString("tri", "nom") == "nom") {
                gestionnaireContacts!!.sortContactByFirstNameAZ()
            } else if (sharedPreferences.getString("tri", "nom") == "priorite") {
                gestionnaireContacts!!.sortContactByPriority()
            } else {
                gestionnaireContacts!!.sortContactByGroup()
            }

            gridViewAdapter = ContactGridViewAdapter(this, gestionnaireContacts!!, len)

            main_GridView!!.adapter = gridViewAdapter
            val index = sharedPreferences.getInt("index", 0)
            val edit: SharedPreferences.Editor = sharedPreferences.edit()
            main_GridView!!.setSelection(index)
            edit.apply()

            main_GridView!!.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                if (main_GridView!!.adapter is SelectContactAdapter && !firstClick) {
                    val adapter = (main_GridView!!.adapter as SelectContactAdapter)
                    adapter.itemSelected(position)
                    adapter.notifyDataSetChanged()

                    if (listOfItemSelected.contains(gestionnaireContacts!!.contacts[position])) {
                        listOfItemSelected.remove(gestionnaireContacts!!.contacts[position])
                    } else {
                        listOfItemSelected.add(gestionnaireContacts!!.contacts[position])
                    }

                    verifiedContactsChannel(listOfItemSelected)

                    if (adapter.listContactSelect.size == 0) {
                        val pos = main_GridView!!.firstVisiblePosition
                        gridViewAdapter = ContactGridViewAdapter(this, gestionnaireContacts, len)
                        main_GridView!!.adapter = gridViewAdapter
                        main_FloatingButtonAdd!!.visibility = View.VISIBLE
                        main_FloatingButtonSend!!.visibility = View.GONE
                        main_SearchBar!!.visibility = View.VISIBLE
                        main_GridView!!.setSelection(pos)

                        Toast.makeText(this, R.string.main_toast_multi_select_deactived, Toast.LENGTH_SHORT).show()

                        main_MailButton!!.visibility = View.GONE
                        main_SMSButton!!.visibility = View.GONE

                        main_ToolbarMultiSelectModeLayout!!.visibility = View.GONE
                        main_ToolbarLayout!!.visibility = View.VISIBLE
                    }
                }
                firstClick = false
            }

            main_GridView!!.setOnScrollListener(object : AbsListView.OnScrollListener {
                var lastVisiblePos = main_GridView!!.firstVisiblePosition
                override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
                    if (gridViewAdapter != null) {
                        gridViewAdapter!!.closeMenu()
                    }
                }

                override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
                    if (gridViewAdapter != null) {
                        gridViewAdapter!!.closeMenu()
                    }
                    println("last visible pos" + lastVisiblePos + "first visible item " + firstVisibleItem + " visible item count" + visibleItemCount + " total item count " + totalItemCount)
                    if (lastVisiblePos < firstVisibleItem) {
                        if (main_FloatingButtonAdd!!.visibility == View.VISIBLE) {
                            val disparition = AnimationUtils.loadAnimation(baseContext, R.anim.disparition)
                            main_FloatingButtonAdd!!.startAnimation(disparition)
                            main_FloatingButtonAdd!!.visibility = View.GONE
                        }
                        lastVisiblePos = firstVisibleItem
                    } else if (lastVisiblePos > firstVisibleItem) {
                        if (main_FloatingButtonAdd!!.visibility == View.GONE) {
                            val apparition = AnimationUtils.loadAnimation(baseContext, R.anim.reapparrition)
                            main_FloatingButtonAdd!!.startAnimation(apparition)
                            main_FloatingButtonAdd!!.visibility = View.VISIBLE
                        }
                        lastVisiblePos = firstVisibleItem
                    }
                }
            })

        }

        if (main_RecyclerView != null) {
            recyclerViewAdapter = ContactRecyclerViewAdapter(this, gestionnaireContacts, len)
            main_RecyclerView!!.adapter = recyclerViewAdapter
            val index = sharedPreferences.getInt("index", 0)
            val edit: SharedPreferences.Editor = sharedPreferences.edit()
            main_RecyclerView!!.scrollToPosition(index)
            edit.putInt("index", 0)
            edit.apply()

            main_RecyclerView!!.layoutManager = LinearLayoutManager(this)
            main_RecyclerView!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    val totalItemCount = recyclerView.layoutManager!!.itemCount
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    println("dx " + dx + " dy" + dy)
                    if (dy > 10) {
                        if (main_FloatingButtonAdd!!.visibility == View.VISIBLE) {
                            val disparition = AnimationUtils.loadAnimation(baseContext, R.anim.disparition)
                            main_FloatingButtonAdd!!.startAnimation(disparition)
                            main_FloatingButtonAdd!!.visibility = View.GONE
                        }
                    } else if (dy < -10) {
                        if (main_FloatingButtonAdd!!.visibility == View.GONE) {
                            val apparition = AnimationUtils.loadAnimation(baseContext, R.anim.reapparrition)
                            main_FloatingButtonAdd!!.startAnimation(apparition)
                            main_FloatingButtonAdd!!.visibility = View.VISIBLE
                        }
                    }
                }
            })
        }

        //main_mDbWorkerThread.postTask(printContacts)

        //endregion

        //region ======================================== Listeners =========================================

        main_FloatingButtonSend!!.setOnClickListener {
            val intent = Intent(this@MainActivity, MultiChannelActivity::class.java)
            intent.putExtra("fromMainToMultiChannel", true)
            val iterator: IntIterator?
            val listOfIdContactSelected: ArrayList<Int> = ArrayList()

            if (len > 1) {
                val adapter: SelectContactAdapter = (main_GridView!!.adapter as SelectContactAdapter)
                iterator = (0 until adapter.listContactSelect.size).iterator()

                for (i in iterator) {
                    listOfIdContactSelected.add(adapter.listContactSelect[i].getContactId())
                }
                intent.putIntegerArrayListExtra("ListContactsSelected", listOfIdContactSelected)

                startActivity(intent)
                finish()
            } else {
                iterator = (0 until listOfItemSelected.size).iterator()

                for (i in iterator) {
                    listOfIdContactSelected.add(listOfItemSelected[i].getContactId())
                }
                intent.putIntegerArrayListExtra("ListContactsSelected", listOfIdContactSelected)

                refreshActivity()
                startActivity(intent)
                finish()
            }
        }

        //Sync contact
        navSyncContact.setOnMenuItemClickListener {
            drawerLayout!!.closeDrawers()
            main_GridView!!.visibility = View.GONE
            main_RecyclerView!!.visibility = View.GONE
            //check les permissions
            val sync = Runnable {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), 99)
                }
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                    //on affiche le loading
                    val displayLoading = Runnable {
                        main_loadingPanel!!.visibility = View.VISIBLE
                    }
                    runOnUiThread(displayLoading)
                    //on effectue la sync
                    gestionnaireContacts!!.getAllContacsInfoSync(contentResolver)
                    //on get tout les contact qui on été modifié lors de la last sync et on les stock dans une arrayList
                    val sharedPreferencesSync = getSharedPreferences("save_last_sync", Context.MODE_PRIVATE)
                    var index = 1
                    var stringSet = listOf<String>()
                    if (sharedPreferencesSync.getStringSet(index.toString(), null) != null)
                        stringSet = sharedPreferencesSync.getStringSet(index.toString(), null).sorted()
                    val changedContactList = arrayListOf<Pair<ContactDB, List<ContactDetailDB>>>()
                    while (sharedPreferencesSync.getStringSet(index.toString(), null) != null && stringSet.isNotEmpty()) {
                        stringSet = sharedPreferencesSync.getStringSet(index.toString(), null).sorted()
                        changedContactList.add(gestionnaireContacts!!.setToContactList(stringSet))
                        index++
                    }
                    //pour chaque contact changé on affiche une popup avec un choix ( garder la version Android ou Knocker
                    changedContactList.forEach { changedContact ->
                        MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                                .setTitle(R.string.main_edited_contact)
                                .setMessage(this.resources.getString(R.string.main_content_edited_contact) + " " + changedContact.first.firstName + " " + changedContact.first.lastName + this.resources.getString(R.string.main_content_edited_contact_2))
                                .setPositiveButton(R.string.main_knocker_edited_contact) { _, _ ->
                                    // on garde la version Knocker
                                }
                                .setNegativeButton(R.string.main_android_edited_contact) { _, _ ->

                                    val allId = gestionnaireContacts!!.sliceLastSync(sharedPreferences.getString("last_sync_2", "")!!)
                                    //on get les idAndroid et idKnocker du contact modifier
                                    allId.forEach {
                                        if (changedContact.first.id == it.first)
                                            changedContact.first.id = it.second
                                    }
                                    //grâce à l'id on update avec les details récupéré dans save_last_sync
                                    main_ContactsDatabase!!.contactsDao().updateContactByIdSync(changedContact.first.id!!, changedContact.first.firstName, changedContact.first.lastName)
                                    main_ContactsDatabase!!.contactDetailsDao().deleteAllDetailsOfContact(changedContact.first.id!!)
                                    changedContact.second.forEach {
                                        it.idContact = changedContact.first.id
                                        main_ContactsDatabase!!.contactDetailsDao().insert(it)
                                    }
                                    val displaySync = Runnable {
                                        //on update soit la grid ou soit la list view en fonction de celle séléctionné
                                        gestionnaireContacts!!.contacts.clear()
                                        val shareP = applicationContext.getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
                                        val executorService: ExecutorService = Executors.newFixedThreadPool(1)
                                        if (shareP.getString("tri", "") == "priorite") {
                                            val callDb = Callable { main_ContactsDatabase!!.contactsDao().sortContactByPriority20() }
                                            val result = executorService.submit(callDb)
                                            gestionnaireContacts!!.contacts.addAll(result.get())
                                        } else {
                                            val callDb = Callable { main_ContactsDatabase!!.contactsDao().sortContactByFirstNameAZ() }
                                            val result = executorService.submit(callDb)
                                            gestionnaireContacts!!.contacts.addAll(result.get())
                                        }
                                        gridViewAdapter!!.setGestionnairecontact(gestionnaireContacts!!)
                                        gridViewAdapter!!.notifyDataSetChanged()
                                    }
                                    runOnUiThread(displaySync)
                                }
                                .show()
                    }

                    index = 1
                    val edit: SharedPreferences.Editor = sharedPreferencesSync.edit()
                    while (sharedPreferencesSync.getStringSet(index.toString(), null) != null && stringSet.isNotEmpty()) {
                        stringSet = sharedPreferencesSync.getStringSet(index.toString(), null).sorted()
                        edit.remove(index.toString())
                        index++
                    }
                    edit.apply()
                    val sharedPrefe = applicationContext.getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
                    if (sharedPrefe.getString("tri", "") == "priorite")
                        gestionnaireContacts!!.sortContactByPriority()
                    else
                        gestionnaireContacts!!.sortContactByFirstNameAZ()

                    val displaySync = Runnable {
                        main_loadingPanel!!.visibility = View.GONE

                        if (len >= 3) {
                            main_GridView!!.visibility = View.VISIBLE
                            gridViewAdapter!!.setGestionnairecontact(gestionnaireContacts!!)
                            gridViewAdapter!!.notifyDataSetChanged()
                        } else {
                            main_RecyclerView!!.visibility = View.VISIBLE
                            recyclerViewAdapter!!.setGestionnaireContact(gestionnaireContacts!!)
                            recyclerViewAdapter!!.notifyDataSetChanged()
                        }
                        drawerLayout!!.closeDrawers()
                    }
                    runOnUiThread(displaySync)
                }
            }
            main_mDbWorkerThread.postTask(sync)
            true
        }

        main_layout!!.setOnTouchListener { _, _ ->
            val v = this@MainActivity.currentFocus
            val imm = this@MainActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (v != null) {
                imm.hideSoftInputFromWindow(v.windowToken, 0)
            }
            true
        }

        //création du listener de la searchbar
        main_SearchBar!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            //fonction appellé à chaque charactère tapé
            override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
                //ferme circular
                gridViewAdapter!!.closeMenu()
                //convertir en string le contenu de la searchbar
                main_search_bar_value = main_SearchBar!!.text.toString()
                //get le type d'affichage selectionné
                val sharedPref = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
                val length = sharedPref.getInt("gridview", 4)
                //on get la list des contacts en appliquant les filtres et la search bar
                val filteredList = gestionnaireContacts!!.getContactConcernByFilter(main_filter, main_search_bar_value)
                // on get la list des contacts en appliquant le tri
                val contactListDb = ContactList(this@MainActivity)
                if (sharedPref.getString("tri", "nom") == "nom") {
                    contactListDb.sortContactByFirstNameAZ()
                    contactListDb.contacts.retainAll(filteredList)
                } else {
                    contactListDb.sortContactByPriority()
                    contactListDb.contacts.retainAll(filteredList)
                }
                gestionnaireContacts!!.contacts.clear()
                gestionnaireContacts!!.contacts.addAll(contactListDb.contacts)
                //en fonction de l'affichage on update soit la grid soit la list view
                if (length > 1) {
                    gridViewAdapter = ContactGridViewAdapter(this@MainActivity, gestionnaireContacts, length)
                    main_GridView!!.adapter = gridViewAdapter
                } else {
                    recyclerViewAdapter = ContactRecyclerViewAdapter(this@MainActivity, gestionnaireContacts, length)
                    main_RecyclerView!!.adapter = recyclerViewAdapter
                    recyclerViewAdapter!!.notifyDataSetChanged()
                }
            }
        })

        main_FloatingButtonAdd!!.setOnClickListener {
            startActivity(Intent(this@MainActivity, AddNewContactActivity::class.java))
        }

        main_SMSButton!!.setOnClickListener {
            val iterator: IntIterator?
            val listOfPhoneNumberContactSelected: ArrayList<String> = ArrayList()

            if (len > 1) {
                val adapter: SelectContactAdapter = (main_GridView!!.adapter as SelectContactAdapter)

                iterator = (0 until adapter.listContactSelect.size).iterator()

                for (i in iterator) {
                    listOfPhoneNumberContactSelected.add(adapter.listContactSelect[i].getFirstPhoneNumber())
                }
            } else {
                iterator = (0 until listOfItemSelected.size).iterator()

                for (i in iterator) {
                    listOfPhoneNumberContactSelected.add(listOfItemSelected[i].getFirstPhoneNumber())
                }
            }
            monoChannelSmsClick(listOfPhoneNumberContactSelected)
            refreshActivity()
        }

        main_MailButton!!.setOnClickListener {
            val iterator: IntIterator?
            val listOfMailContactSelected: ArrayList<String> = ArrayList()

            if (len > 1) {
                val adapter: SelectContactAdapter = (main_GridView!!.adapter as SelectContactAdapter)
                iterator = (0 until adapter.listContactSelect.size).iterator()

                for (i in iterator) {
                    listOfMailContactSelected.add(adapter.listContactSelect[i].getFirstMail())
                }
            } else {
                iterator = (0 until listOfItemSelected.size).iterator()

                for (i in iterator) {
                    listOfMailContactSelected.add(listOfItemSelected[i].getFirstMail())
                }
            }
            monoChannelMailClick(listOfMailContactSelected)

            if (len >= 3) {
                gridViewAdapter = ContactGridViewAdapter(this@MainActivity, gestionnaireContacts, len)
                main_GridView!!.adapter = gridViewAdapter
                main_FloatingButtonAdd!!.visibility = View.VISIBLE
                main_FloatingButtonSend!!.visibility = View.GONE
                main_SearchBar!!.visibility = View.VISIBLE


                main_MailButton!!.visibility = View.GONE
                main_SMSButton!!.visibility = View.GONE
            } else {
                main_RecyclerView!!.adapter = ContactRecyclerViewAdapter(this, gestionnaireContacts, len)
                main_FloatingButtonAdd!!.visibility = View.VISIBLE
                main_FloatingButtonSend!!.visibility = View.GONE
                main_SearchBar!!.visibility = View.VISIBLE


                main_MailButton!!.visibility = View.GONE
                main_SMSButton!!.visibility = View.GONE
            }

            refreshActivity()
        }
        //endregion
    }
    //region ========================================== Functions ===========================================

    private fun refreshActivity() {
        val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
        val len = sharedPreferences.getInt("gridview", 4)
        if (len > 1) {
            gridViewAdapter = ContactGridViewAdapter(this@MainActivity, gestionnaireContacts, len)
            main_GridView!!.adapter = gridViewAdapter
        } else {
            recyclerViewAdapter = ContactRecyclerViewAdapter(this@MainActivity, gestionnaireContacts, len)
            main_RecyclerView!!.adapter = recyclerViewAdapter
        }

        main_FloatingButtonAdd!!.visibility = View.VISIBLE
        main_MailButton!!.visibility = View.GONE
        main_SMSButton!!.visibility = View.GONE
        main_FloatingButtonSend!!.visibility = View.GONE
        main_SearchBar!!.visibility = View.VISIBLE
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        val triNom = menu.findItem(R.id.tri_par_nom)
        val triLastName = menu.findItem(R.id.tri_par_lastname)
        val triPriority = menu.findItem(R.id.tri_par_priorite)
        val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
        val tri = sharedPreferences.getString("tri", "nom")
        when (tri) {
            "nom" -> triNom.isChecked = true
            "priorite" -> triPriority.isChecked = true
            else -> triLastName.isChecked = true
        }
        return true
    }


    //check les checkbox si elle ont été check apres une recherche
    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        super.onPrepareOptionsMenu(menu)
        val main_filter = intent.getStringArrayListExtra("Filter")
        if (main_filter != null && main_filter.contains("sms")) {
            menu?.findItem(R.id.sms_filter)?.isChecked = true
            intent.putStringArrayListExtra("Filter", main_filter)
        }
        if (main_filter != null && main_filter.contains("mail")) {
            menu?.findItem(R.id.mail_filter)?.isChecked = true
            intent.putStringArrayListExtra("Filter", main_filter)
        }
        return true
    }

    @SuppressLint("ShowToast")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        gridViewAdapter!!.closeMenu()
        when (item.itemId) {
            android.R.id.home -> {
                drawerLayout!!.openDrawer(GravityCompat.START)
                hideKeyboard()
                return true
            }
            R.id.item_help -> {
                //click sur le bouton help
                MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                        .setTitle(R.string.help)
                        .setMessage(this.resources.getString(R.string.help_main))
                        .setBackground(getDrawable(R.color.backgroundColor))
                        .show()
                return true
            }
            R.id.sms_filter -> {
                //clique sur la checkbox filtre SMS
                //on regarde si la checkbox est coché oui ou non
                if (item.isChecked) {
                    //on décoche la checkbox
                    item.isChecked = false
                    //on enleve sms de la list de filtre
                    main_filter.remove("sms")
                    /// duplicate
                    main_search_bar_value = main_SearchBar!!.text.toString()
                    //on récup la taille de la gridview dans la sharedpref
                    val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
                    val len = sharedPreferences.getInt("gridview", 4)
                    //on récup tout les contacts avec les filtres appliqués
                    val filteredContact = gestionnaireContacts!!.getContactConcernByFilter(main_filter, main_search_bar_value)
                    // on regarde le quel tri est activé pour l'appliquer
                    val contactListDb = ContactList(this)
                    if (sharedPreferences.getString("tri", "nom") == "nom") {
                        contactListDb.sortContactByFirstNameAZ()
                    } else if (sharedPreferences.getString("tri", "nom") == "priorite") {
                        contactListDb.sortContactByPriority()
                    } else {
                        contactListDb.sortContactByGroup()
                    }
                    //on garde uniquement les contact en commun avec les filtres et tris
                    contactListDb.contacts.retainAll(filteredContact)
                    gestionnaireContacts!!.contacts.clear()
                    gestionnaireContacts!!.contacts.addAll(contactListDb.contacts)
                    //on check si on est en grid ou list view pour savoir laquelle update
                    if (len > 1) {
                        gridViewAdapter = ContactGridViewAdapter(this@MainActivity, gestionnaireContacts, len)
                        main_GridView!!.adapter = gridViewAdapter
                    } else {
                        recyclerViewAdapter = ContactRecyclerViewAdapter(this@MainActivity, gestionnaireContacts, len)
                        main_RecyclerView!!.adapter = recyclerViewAdapter
                    }
                } else {
                    //on coche la checkbox
                    item.isChecked = true
                    //et on fais les memes étapes que plus haut |duplicata|
                    main_filter.add("sms")
                    main_search_bar_value = main_SearchBar!!.text.toString()

                    val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
                    val len = sharedPreferences.getInt("gridview", 4)
                    val filteredContact = gestionnaireContacts!!.getContactConcernByFilter(main_filter, main_search_bar_value)
                    if (sharedPreferences.getString("tri", "nom") == "nom") {
                        gestionnaireContacts!!.sortContactByFirstNameAZ()
                    } else if (sharedPreferences.getString("tri", "nom") == "priorite") {
                        gestionnaireContacts!!.sortContactByPriority()
                    } else {
                        gestionnaireContacts!!.sortContactByGroup()
                    }
                    gestionnaireContacts!!.contacts.retainAll(filteredContact)

                    if (len > 1) {
                        gridViewAdapter = ContactGridViewAdapter(this@MainActivity, gestionnaireContacts, len)
                        main_GridView!!.adapter = gridViewAdapter
                    } else {
                        recyclerViewAdapter = ContactRecyclerViewAdapter(this@MainActivity, gestionnaireContacts, len)
                        main_RecyclerView!!.adapter = recyclerViewAdapter
                        recyclerViewAdapter!!.notifyDataSetChanged()
                    }
                }
                return true
            }
            R.id.mail_filter -> {
                //clique sur la checkbox filtre MAIL
                //exactement comme pour le filtre SMS
                if (item.isChecked) {
                    item.isChecked = false
                    main_filter.remove("mail")
                    // duplicate
                    main_search_bar_value = main_SearchBar!!.text.toString()

                    val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
                    val len = sharedPreferences.getInt("gridview", 4)
                    val filteredContact = gestionnaireContacts!!.getContactConcernByFilter(main_filter, main_search_bar_value)
                    val contactListDb = ContactList(this)
                    if (sharedPreferences.getString("tri", "nom") == "nom") {
                        contactListDb.sortContactByFirstNameAZ()
                    } else if (sharedPreferences.getString("tri", "nom") == "priorite") {
                        contactListDb.sortContactByPriority()
                    } else {
                        contactListDb.sortContactByGroup()
                    }
                    contactListDb.contacts.retainAll(filteredContact)
                    gestionnaireContacts!!.contacts.clear()
                    gestionnaireContacts!!.contacts.addAll(contactListDb.contacts)
                    if (len > 1) {
                        gridViewAdapter = ContactGridViewAdapter(this@MainActivity, gestionnaireContacts, len)
                        main_GridView!!.adapter = gridViewAdapter
                    } else {
                        recyclerViewAdapter = ContactRecyclerViewAdapter(this@MainActivity, gestionnaireContacts, len)
                        main_RecyclerView!!.adapter = recyclerViewAdapter
                        recyclerViewAdapter!!.notifyDataSetChanged()
                    }
                } else {
                    item.isChecked = true
                    main_filter.add("mail")
                    // duplicate
                    main_search_bar_value = main_SearchBar!!.text.toString()
                    val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
                    val len = sharedPreferences.getInt("gridview", 4)
                    val filteredContact = gestionnaireContacts!!.getContactConcernByFilter(main_filter, main_search_bar_value)
                    if (sharedPreferences.getString("tri", "nom") == "nom") {
                        gestionnaireContacts!!.sortContactByFirstNameAZ()
                    } else if (sharedPreferences.getString("tri", "nom") == "priorite") {
                        gestionnaireContacts!!.sortContactByPriority()
                    } else {
                        gestionnaireContacts!!.sortContactByGroup()
                    }
                    gestionnaireContacts!!.contacts.retainAll(filteredContact)
                    if (len > 1) {
                        gridViewAdapter = ContactGridViewAdapter(this@MainActivity, gestionnaireContacts, len)
                        main_GridView!!.adapter = gridViewAdapter
                    } else {
                        recyclerViewAdapter = ContactRecyclerViewAdapter(this@MainActivity, gestionnaireContacts, len)

                        recyclerViewAdapter!!.notifyDataSetChanged()
                    }
                }
                return true
            }
            R.id.tri_par_nom -> {
                if (!item.isChecked) {
                    main_GridView!!.visibility = View.GONE
                    main_RecyclerView!!.visibility = View.GONE
                    main_loadingPanel!!.visibility = View.VISIBLE
                    val sortByName = Runnable {
                        gestionnaireContacts!!.sortContactByFirstNameAZ()
                        val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
                        val len = sharedPreferences.getInt("gridview", 4)
                        val edit: SharedPreferences.Editor = sharedPreferences.edit()
                        edit.putString("tri", "nom")
                        edit.apply()
                        runOnUiThread {
                            item.isChecked = true
                            if (len > 1) {
                                gridViewAdapter = ContactGridViewAdapter(this@MainActivity, gestionnaireContacts, len)
                                main_GridView!!.adapter = gridViewAdapter
                                main_GridView!!.visibility = View.VISIBLE
                            } else {
                                recyclerViewAdapter = ContactRecyclerViewAdapter(this@MainActivity, gestionnaireContacts, len)
                                main_RecyclerView!!.adapter = recyclerViewAdapter
                                recyclerViewAdapter!!.notifyDataSetChanged()
                                main_RecyclerView!!.visibility = View.VISIBLE
                            }
                            main_loadingPanel!!.visibility = View.GONE
                        }
                    }
                    main_mDbWorkerThread.postTask(sortByName)
                }
            }
            R.id.tri_par_priorite -> {
                if (!item.isChecked) {
                    main_GridView!!.visibility = View.GONE
                    main_RecyclerView!!.visibility = View.GONE
                    main_loadingPanel!!.visibility = View.VISIBLE
                    val sortByPriority = Runnable {
                        gestionnaireContacts!!.sortContactByPriority()
                        val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
                        val len = sharedPreferences.getInt("gridview", 4)
                        val edit: SharedPreferences.Editor = sharedPreferences.edit()
                        edit.putString("tri", "priorite")
                        edit.apply()
                        runOnUiThread {
                            item.isChecked = true
                            if (len > 1) {
                                gridViewAdapter = ContactGridViewAdapter(this@MainActivity, gestionnaireContacts, len)
                                main_GridView!!.adapter = gridViewAdapter
                                main_GridView!!.visibility = View.VISIBLE
                            } else {
                                recyclerViewAdapter = ContactRecyclerViewAdapter(this@MainActivity, gestionnaireContacts, len)
                                main_RecyclerView!!.adapter = recyclerViewAdapter
                                recyclerViewAdapter!!.notifyDataSetChanged()
                                main_RecyclerView!!.visibility = View.VISIBLE
                            }
                            main_loadingPanel!!.visibility = View.GONE
                        }
                    }
                    main_mDbWorkerThread.postTask(sortByPriority)
                }
            }
            R.id.tri_par_lastname -> {
                if (!item.isChecked) {
                    main_GridView!!.visibility = View.GONE
                    main_RecyclerView!!.visibility = View.GONE
                    main_loadingPanel!!.visibility = View.VISIBLE
                    val sortByLastname = Runnable {
                        gestionnaireContacts!!.sortContactByLastname()
                        val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
                        val len = sharedPreferences.getInt("gridview", 4)
                        val edit: SharedPreferences.Editor = sharedPreferences.edit()
                        edit.putString("tri", "lastname")
                        edit.apply()
                        runOnUiThread {
                            item.isChecked = true
                            if (len > 1) {
                                gridViewAdapter = ContactGridViewAdapter(this@MainActivity, gestionnaireContacts, len)
                                main_GridView!!.adapter = gridViewAdapter
                                main_GridView!!.visibility = View.VISIBLE
                            } else {
                                recyclerViewAdapter = ContactRecyclerViewAdapter(this@MainActivity, gestionnaireContacts, len)
                                main_RecyclerView!!.adapter = recyclerViewAdapter
                                recyclerViewAdapter!!.notifyDataSetChanged()
                                main_RecyclerView!!.visibility = View.VISIBLE
                            }
                            main_loadingPanel!!.visibility = View.GONE
                        }
                    }
                    main_mDbWorkerThread.postTask(sortByLastname)
                }
            }
        }
        return super.onOptionsItemSelected(item)
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
    }//TODO: enlever code duplicate

    private fun toggleNotificationListenerService() {
        val pm = packageManager
        val cmpName = ComponentName(this, NotificationListener::class.java)
        pm.setComponentEnabledSetting(cmpName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
        pm.setComponentEnabledSetting(cmpName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
    }

    private fun hideKeyboard() {
        // Check if no view has focus:
        val view = this.currentFocus

        view?.let { v ->
            val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }

    override fun onDrawerStateChanged(newState: Int) {

    }

    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
        gridViewAdapter!!.closeMenu()
    }

    override fun onDrawerClosed(drawerView: View) {
    }

    override fun onDrawerOpened(drawerView: View) {
        gridViewAdapter!!.closeMenu()
    }

    fun longGridItemClick(len: Int, position: Int, firstPosVis: Int) {
        main_GridView!!.setSelection(firstPosVis)
        //gridViewAdapter!!.closeMenu()
        val adapter = SelectContactAdapter(this, gestionnaireContacts, len, false)
        main_GridView!!.adapter = adapter
        adapter.itemSelected(position)
        adapter.notifyDataSetChanged()
        main_FloatingButtonAdd!!.visibility = View.GONE
        main_FloatingButtonSend!!.visibility = View.VISIBLE
        main_SearchBar!!.visibility = View.GONE
        firstClick = true

        if (listOfItemSelected.contains(gestionnaireContacts!!.contacts[position])) {
            listOfItemSelected.remove(gestionnaireContacts!!.contacts[position])
        } else {
            listOfItemSelected.add(gestionnaireContacts!!.contacts[position])
        }

        verifiedContactsChannel(listOfItemSelected)

        Toast.makeText(this, R.string.main_toast_multi_select_actived, Toast.LENGTH_SHORT).show()

        main_ToolbarMultiSelectModeLayout!!.visibility = View.VISIBLE
        main_ToolbarLayout!!.visibility = View.GONE
    }

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
            main_SMSButton!!.visibility = View.VISIBLE
            i++
        } else {
            println("false phoneNumber")
            main_SMSButton!!.visibility = View.GONE
        }
        if (allContactsHaveMail) {
            main_MailButton!!.visibility = View.VISIBLE
            val params: ViewGroup.MarginLayoutParams = main_MailButton!!.layoutParams as ViewGroup.MarginLayoutParams
            params.bottomMargin = margin * i
            main_MailButton!!.layoutParams = params
            println("height of floating mail" + main_MailButton!!.height)
            i++
        } else {
            println("false mail")
            main_MailButton!!.visibility = View.GONE
        }
    }

    fun longRecyclerItemClick(position: Int) {
        if (listOfItemSelected.contains(gestionnaireContacts!!.contacts[position])) {
            listOfItemSelected.remove(gestionnaireContacts!!.contacts[position])
            verifiedContactsChannel(listOfItemSelected)
        } else {
            listOfItemSelected.add(gestionnaireContacts!!.contacts[position])

            main_FloatingButtonAdd!!.visibility = View.GONE
            main_FloatingButtonSend!!.visibility = View.VISIBLE
            main_SearchBar!!.visibility = View.GONE

            verifiedContactsChannel(listOfItemSelected)
        }

        if (listOfItemSelected.size == 1 && firstClick) {
            Toast.makeText(this, R.string.main_toast_multi_select_actived, Toast.LENGTH_SHORT).show()
            main_FloatingButtonAdd!!.visibility = View.GONE
            main_FloatingButtonSend!!.visibility = View.VISIBLE
            main_SearchBar!!.visibility = View.GONE
            firstClick = false
            multiChannelMode = true
            main_ToolbarMultiSelectModeLayout!!.visibility = View.VISIBLE
            main_ToolbarLayout!!.visibility = View.INVISIBLE
        } else if (listOfItemSelected.size == 0) {
            Toast.makeText(this, R.string.main_toast_multi_select_deactived, Toast.LENGTH_SHORT).show()

            main_FloatingButtonAdd!!.visibility = View.VISIBLE
            main_FloatingButtonSend!!.visibility = View.GONE
            main_SearchBar!!.visibility = View.VISIBLE
            main_SMSButton!!.visibility = View.GONE
            main_MailButton!!.visibility = View.GONE

            main_ToolbarMultiSelectModeLayout!!.visibility = View.GONE
            main_ToolbarLayout!!.visibility = View.VISIBLE

            firstClick = true
        }
    }

    private fun monoChannelSmsClick(listOfPhoneNumber: ArrayList<String>) {

        var message = "smsto:" + listOfPhoneNumber[0]
        for (i in 0 until listOfPhoneNumber.size) {
            message += ";" + listOfPhoneNumber[i]
        }
        startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse(message)))
    }

    private fun monoChannelMailClick(listOfMail: ArrayList<String>) {
        val contact = listOfMail.toArray(arrayOfNulls<String>(listOfMail.size))
        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_EMAIL, contact)/*listOfMail.toArray(new String[listOfMail.size()]*/
        intent.data = Uri.parse("mailto:")
        intent.type = "message/rfc822"
        intent.putExtra(Intent.EXTRA_SUBJECT, "")
        intent.putExtra(Intent.EXTRA_TEXT, "")
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {

            PERMISSION_CALL_RESULT -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (main_GridView!!.visibility == View.VISIBLE) {
                        gridViewAdapter!!.callPhone(gridViewAdapter!!.phonePermission)
                    } else {
                        Toast.makeText(this, "Can't do anything until you permit me !", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            99 -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                println("permission accept")
                gestionnaireContacts!!.getAllContacsInfoSync(contentResolver)
                recreate()
            }
        }
    }

    //endregion
}