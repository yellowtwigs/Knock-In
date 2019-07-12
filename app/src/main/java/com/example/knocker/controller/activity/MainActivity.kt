package com.example.knocker.controller.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.*
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import android.util.Base64
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.knocker.*
import com.example.knocker.controller.*
import com.example.knocker.controller.ContactRecyclerViewAdapter.ContactViewHolder
import com.example.knocker.controller.activity.firstLaunch.FirstLaunchActivity
import com.example.knocker.model.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder

import com.example.knocker.controller.activity.firstLaunch.SelectContactAdapter
import com.example.knocker.model.ModelDB.ContactDB
import com.example.knocker.model.ModelDB.ContactDetailDB
import kotlinx.android.synthetic.main.content_main.*
import com.example.knocker.model.ModelDB.ContactWithAllInformation
import java.util.*
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
    private var main_ListView: ListView? = null
    private var main_RecyclerView: RecyclerView? = null

    private var main_FloatingButtonAdd: FloatingActionButton? = null
    private var main_FloatingButtonSend: FloatingActionButton? = null

    private var main_WhatsappButton: AppCompatButton? = null
    private var main_SMSButton: AppCompatButton? = null
    private var main_MailButton: AppCompatButton? = null

    internal var main_search_bar_value = ""
    private var main_filter = arrayListOf<String>()
    private var main_SearchBar: EditText? = null
    var scaleGestureDetectore: ScaleGestureDetector? = null

    // Database && Thread
    private var main_ContactsDatabase: ContactsRoomDatabase? = null
    private lateinit var main_mDbWorkerThread: DbWorkerThread

    private var main_BottomNavigationView: BottomNavigationView? = null

    private var gestionnaireContacts: ContactList? = null
    private var gridViewAdapter: ContactGridViewAdapter? = null
    private var listViewAdapter: ContactListViewAdapter? = null
    private var recyclerViewAdapter: ContactRecyclerViewAdapter? = null
    private var main_layout: LinearLayout? = null

    private var listOfItemSelected: ArrayList<ContactWithAllInformation> = ArrayList<ContactWithAllInformation>()

    private var firstClick: Boolean = true

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_phone_book -> {
            }
            R.id.navigation_groups -> {
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
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedThemePreferences = getSharedPreferences("Knocker_Theme", Context.MODE_PRIVATE)
        if (sharedThemePreferences.getBoolean("darkTheme", false)) {
            setTheme(R.style.AppThemeDark)
        } else {
            setTheme(R.style.AppTheme)
        }
        setContentView(R.layout.activity_main)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT

        val sharedFirstLaunch = getSharedPreferences("FirstLaunch", Context.MODE_PRIVATE)
        if (sharedFirstLaunch.getBoolean("first_launch", true)) {
            startActivity(Intent(this@MainActivity, FirstLaunchActivity::class.java))
            finish()
        }
        val decorView = window.decorView
        val window = window
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
        //main_BottomNavigationView!!.background= this.getDrawable(R.drawable.border_bottom_nav_view)

        // Search bar
        main_SearchBar = findViewById(R.id.main_search_bar)
        main_layout = findViewById(R.id.content_frame)

        main_WhatsappButton = findViewById(R.id.main_whatsapp_button)
        main_MailButton = findViewById(R.id.main_gmail_button)
        main_SMSButton = findViewById(R.id.main_sms_button)

        //endregion

        //region ========================================== Toolbar =========================================

        val toolbar = findViewById<Toolbar>(R.id.main_toolbar)
        setSupportActionBar(toolbar)
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
        val nav_item = menu.findItem(R.id.nav_address_book)
        nav_item.isChecked = true
        val nav_sync_contact = menu.findItem(R.id.nav_sync_contact)
        nav_sync_contact.isVisible = true

        navigationView!!.menu.getItem(0).isChecked = true

        navigationView.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            drawerLayout!!.closeDrawers()

            when (menuItem.itemId) {
                R.id.nav_address_book -> {
                    startActivity(Intent(this@MainActivity, MainActivity::class.java))
                }
                R.id.nav_informations -> startActivity(Intent(this@MainActivity, EditInformationsActivity::class.java))
                R.id.nav_notif_config -> startActivity(Intent(this@MainActivity, ManageNotificationActivity::class.java))
                R.id.nav_screen_config -> startActivity(Intent(this@MainActivity, ManageMyScreenActivity::class.java))
                R.id.nav_data_access -> {
                }
                R.id.nav_knockons -> startActivity(Intent(this@MainActivity, ManageKnockonsActivity::class.java))
                R.id.nav_statistics -> {
                }
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
        main_ListView = findViewById(R.id.main_list_view_id)
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
            } else {
                gestionnaireContacts!!.sortContactByPriority()
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
                        main_GridView!!.adapter = ContactGridViewAdapter(this, gestionnaireContacts, len)
                        main_FloatingButtonAdd!!.visibility = View.VISIBLE
                        main_FloatingButtonSend!!.visibility = View.GONE
                        main_SearchBar!!.visibility = View.VISIBLE

                        Toast.makeText(this, R.string.main_toast_multi_select_deactived, Toast.LENGTH_SHORT).show()

                        main_MailButton!!.visibility = View.GONE
                        main_WhatsappButton!!.visibility = View.GONE
                        main_SMSButton!!.visibility = View.GONE
                    }
                }
                firstClick = false
            }

            main_GridView!!.setOnScrollListener(object : AbsListView.OnScrollListener {
                override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {

                }

                override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
                    if (gridViewAdapter != null) {
                        gridViewAdapter!!.closeMenu()
                    }
                }
            })
        }

        if (main_ListView != null) {
            listViewAdapter = ContactListViewAdapter(this, gestionnaireContacts!!.contacts, len)
            main_ListView!!.adapter = listViewAdapter
            val index = sharedPreferences.getInt("index", 0)
            val edit: SharedPreferences.Editor = sharedPreferences.edit()
            main_ListView!!.setSelection(index)
            edit.putInt("index", 0)
            edit.apply()

            main_ListView!!.setOnItemClickListener { _, _, position, id ->
                if (main_ListView!!.adapter is SelectContactAdapter && !firstClick) {
                    val adapter = (main_ListView!!.adapter as SelectContactAdapter)
                    adapter.itemSelected(position)
                    if (adapter.listContactSelect.size == 0) {
                        main_GridView!!.adapter = ContactGridViewAdapter(this, gestionnaireContacts, len)
                        main_FloatingButtonAdd!!.visibility = View.VISIBLE
                        main_FloatingButtonSend!!.visibility = View.GONE
                        main_SearchBar!!.visibility = View.VISIBLE

                        Toast.makeText(this, R.string.main_toast_multi_select_deactived, Toast.LENGTH_SHORT).show()

                        main_MailButton!!.visibility = View.GONE
                        main_WhatsappButton!!.visibility = View.GONE
                        main_SMSButton!!.visibility = View.GONE
                    }
                }
                firstClick = false
            }
        }

        if (main_RecyclerView != null) {
            recyclerViewAdapter = ContactRecyclerViewAdapter(this, gestionnaireContacts!!.contacts, len, false)
            main_RecyclerView!!.adapter = recyclerViewAdapter
            val index = sharedPreferences.getInt("index", 0)
            val edit: SharedPreferences.Editor = sharedPreferences.edit()
            main_RecyclerView!!.scrollToPosition(index)
            edit.putInt("index", 0)
            edit.apply()

            main_RecyclerView!!.layoutManager = LinearLayoutManager(this)

            main_RecyclerView!!.setOnClickListener {
                if (main_RecyclerView!!.adapter is SelectContactAdapter && !firstClick) {
                    val adapter = ContactRecyclerViewAdapter(this, gestionnaireContacts!!.contacts, len, false)
                    main_RecyclerView!!.adapter = adapter
//                    adapter.itemSelected(adapter.)

                    adapter.notifyDataSetChanged()
                    if (adapter.listContactSelect.size == 0) {
                        main_RecyclerView!!.adapter = ContactRecyclerViewAdapter(this, gestionnaireContacts!!.contacts, len, false)
                        main_FloatingButtonAdd!!.visibility = View.VISIBLE
                        main_FloatingButtonSend!!.visibility = View.GONE
                        main_SearchBar!!.visibility = View.VISIBLE

                        Toast.makeText(this, R.string.main_toast_multi_select_deactived, Toast.LENGTH_SHORT).show()

                        main_MailButton!!.visibility = View.GONE
                        main_WhatsappButton!!.visibility = View.GONE
                        main_SMSButton!!.visibility = View.GONE
                    }
                }
                firstClick = false
            }
        }

        //main_mDbWorkerThread.postTask(printContacts)

        //endregion

        //region ======================================== Listeners =========================================

        main_FloatingButtonSend!!.setOnClickListener {
            val intent = Intent(this@MainActivity, MultiChannelActivity::class.java)
            var iterator: IntIterator?
            val listOfIdContactSelected: ArrayList<Int> = ArrayList()

            if (len > 1) {
                val adapter: SelectContactAdapter = (main_GridView!!.adapter as SelectContactAdapter)
                iterator = (0 until adapter.listContactSelect.size).iterator()

                for (i in iterator) {
                    listOfIdContactSelected.add(adapter.listContactSelect[i].getContactId())
                }
                intent.putIntegerArrayListExtra("ListContactsSelected", listOfIdContactSelected)

                startActivity(intent)
            } else {
//                val adapter: ContactRecyclerViewAdapter = main_RecyclerView!!.adapter as ContactRecyclerViewAdapter
//                iterator = (0 until adapter.listContactSelect.size).iterator()
//
//                for (i in iterator) {
//                    listOfIdContactSelected.add(adapter.listContactSelect[i].getContactId())
//                }
//                intent.putIntegerArrayListExtra("ListContactsSelected", listOfIdContactSelected)
//
//                startActivity(intent)
            }
        }

        //Sync contact
        nav_sync_contact.setOnMenuItemClickListener {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), 1)
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                gestionnaireContacts!!.getAllContacsInfoSync(contentResolver)
                val sharedPreferencesSync = getSharedPreferences("save_last_sync", Context.MODE_PRIVATE)
                var index = 1
                var stringSet = listOf<String>()
                if (sharedPreferencesSync.getStringSet(index.toString(), null) != null)
                    stringSet = sharedPreferencesSync.getStringSet(index.toString(), null).sorted()
                val changedContactList = arrayListOf<Pair<ContactDB, List<ContactDetailDB>>>()
                while (sharedPreferencesSync.getStringSet(index.toString(), null) != null && !stringSet.isEmpty()) {
                    stringSet = sharedPreferencesSync.getStringSet(index.toString(), null).sorted()
                    changedContactList.add(gestionnaireContacts!!.setToContactList(stringSet))
                    index++
                }
                changedContactList.forEach {changedContact ->
                    MaterialAlertDialogBuilder(this)
                            .setTitle("Contact modifié")
                            .setMessage("Le Contact "+changedContact.first.firstName+" "+changedContact.first.lastName+" a été changer, garder la version du carnet d'address d'Android ou de Knocker ?")
                            .setPositiveButton("Knocker",{dialog, which ->
                            })
                            .setNegativeButton("Android", {dialog, which ->
                                val allId = gestionnaireContacts!!.sliceLastSync(sharedPreferences.getString("last_sync", "")!!)
                                allId.forEach {
                                    if (changedContact.first.id == it.first)
                                        changedContact.first.id = it.second
                                }
                                main_ContactsDatabase!!.contactsDao().updateContactByIdSync(changedContact.first.id!!,changedContact.first.firstName, changedContact.first.lastName)
                                main_ContactsDatabase!!.contactDetailsDao().deleteAllDetailsOfContact(changedContact.first.id!!)
                                changedContact.second.forEach {
                                    it.idContact = changedContact.first.id
                                    main_ContactsDatabase!!.contactDetailsDao().insert(it)
                                }
                            })
                            .show()
                }
                //gestionnaireContacts!!.
                //ContactSync.getAllContact(contentResolver)//TODO put this code into ContactList
                //val len = sharedPreferences.getInt("gridview", 4)
                /*  gridViewAdapter = ContactGridViewAdapter(applicationContext, gestionnaireContacts!!, len)
              main_GridView!!.adapter = gridViewAdapter
  */
                index = 1
                val edit: SharedPreferences.Editor = sharedPreferencesSync.edit()
                while (sharedPreferencesSync.getStringSet(index.toString(), null) != null && !stringSet.isEmpty()) {
                    stringSet = sharedPreferencesSync.getStringSet(index.toString(), null).sorted()
                    edit.remove(index.toString())
                    index++
                }
                edit.apply()
                val sharedPreferences = applicationContext.getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
                if (sharedPreferences.getString("tri", "") == "priorite")
                    gestionnaireContacts!!.sortContactByPriority()
                else
                    gestionnaireContacts!!.sortContactByFirstNameAZ()
                gridViewAdapter!!.setGestionnairecontact(gestionnaireContacts!!)
                gridViewAdapter!!.notifyDataSetChanged()
                drawerLayout!!.closeDrawers()
            }
            true
        }

        main_layout!!.setOnTouchListener { v, event ->
            val view = this@MainActivity.currentFocus
            val imm = this@MainActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (view != null) {
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
            true
        }

        main_SearchBar!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
                gridViewAdapter!!.closeMenu()
                main_search_bar_value = main_SearchBar!!.text.toString()
                val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
                val len = sharedPreferences.getInt("gridview", 4)
                var filteredList = gestionnaireContacts!!.getContactConcernByFilter(main_filter, main_search_bar_value)
                val contactListDb = ContactList(this@MainActivity)
                if (sharedPreferences.getString("tri", "nom") == "nom") {
                    contactListDb.sortContactByFirstNameAZ()
                    contactListDb.contacts.retainAll(filteredList)
                } else {
                    contactListDb.sortContactByPriority()
                    contactListDb.contacts.retainAll(filteredList)
                }
                gestionnaireContacts!!.contacts.clear()
                gestionnaireContacts!!.contacts.addAll(contactListDb.contacts)
                if (len > 1) {
                    gridViewAdapter = ContactGridViewAdapter(this@MainActivity, gestionnaireContacts, len)
                    main_GridView!!.adapter = gridViewAdapter
                } else {

                    listViewAdapter = ContactListViewAdapter(this@MainActivity, gestionnaireContacts!!.contacts, len)
                    main_ListView!!.adapter = listViewAdapter
                    listViewAdapter!!.notifyDataSetChanged()
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
                    listOfPhoneNumberContactSelected.add(adapter.listContactSelect[i].getPhoneNumber())
                }
            } else {
//                val adapter: ContactRecyclerViewAdapter = main_RecyclerView!!.adapter as ContactRecyclerViewAdapter
//
//                iterator = (0 until adapter.listContactSelect.size).iterator()
//
//                for (i in iterator) {
//                    listOfPhoneNumberContactSelected.add(adapter.listContactSelect[i].getPhoneNumber())
//                }
            }
            monoChannelSmsClick(listOfPhoneNumberContactSelected)
        }

        main_MailButton!!.setOnClickListener {
            val adapter: SelectContactAdapter = (main_GridView!!.adapter as SelectContactAdapter)
            val iterator = (0 until adapter.listContactSelect.size).iterator()
            val listOfPhoneNumberContactSelected: ArrayList<String> = ArrayList()

            for (i in iterator) {
                listOfPhoneNumberContactSelected.add(adapter.listContactSelect[i].getFirstMail())
            }

            monoChannelMailClick(listOfPhoneNumberContactSelected)
        }

        //endregion

        scaleGestureDetectore = ScaleGestureDetector(this,
                MyOnScaleGestureListener())
    }

    //region ========================================== Functions ===========================================

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleGestureDetectore?.onTouchEvent(event)
        return true
    }

    inner class MyOnScaleGestureListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scaleFactor = detector.scaleFactor
            if (scaleFactor > 1) {
                println("Zooming Out" + scaleFactor)
            } else {
                println("Zooming In" + scaleFactor)
            }
            return true
        }

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            println("begin")
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            println("end")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        val triNom = menu.findItem(R.id.tri_par_nom)
        val triPrio = menu.findItem(R.id.tri_par_priorite)
        val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
        val tri = sharedPreferences.getString("tri", "nom")
        if (tri == "nom") {
            triNom.isChecked = true
        } else {
            triPrio.isChecked = true
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
                MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.help)
                        .setMessage(this.resources.getString(R.string.help_main))
                        .show()
                return true
            }
            R.id.sms_filter -> {
                if (item.isChecked) {
                    item.isChecked = false
                    main_filter.remove("sms")
                    // duplicate
                    main_search_bar_value = main_SearchBar!!.text.toString()

                    val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
                    val len = sharedPreferences.getInt("gridview", 4)
                    val filteredContact = gestionnaireContacts!!.getContactConcernByFilter(main_filter, main_search_bar_value)
                    val contactListDb = ContactList(this)
                    if (sharedPreferences.getString("tri", "nom") == "nom") {
                        contactListDb.sortContactByFirstNameAZ()
                        contactListDb.contacts.retainAll(filteredContact)
                    } else {
                        contactListDb.sortContactByPriority()
                        contactListDb.contacts.retainAll(filteredContact)
                    }
                    gestionnaireContacts!!.contacts.clear()
                    gestionnaireContacts!!.contacts.addAll(contactListDb.contacts)
                    if (len > 1) {
                        gridViewAdapter = ContactGridViewAdapter(this@MainActivity, gestionnaireContacts, len)
                        main_GridView!!.adapter = gridViewAdapter
                    } else {
                        listViewAdapter = ContactListViewAdapter(this@MainActivity, gestionnaireContacts!!.contacts, len)
                        main_ListView!!.adapter = listViewAdapter
                    }
                } else {
                    item.isChecked = true
                    main_filter.add("sms")
                    // duplicate
                    main_search_bar_value = main_SearchBar!!.text.toString()

                    val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
                    val len = sharedPreferences.getInt("gridview", 4)
                    val filteredContact = gestionnaireContacts!!.getContactConcernByFilter(main_filter, main_search_bar_value)
                    if (sharedPreferences.getString("tri", "nom") == "nom") {
                        gestionnaireContacts!!.sortContactByFirstNameAZ()
                    } else {
                        gestionnaireContacts!!.sortContactByPriority()
                    }

                    gestionnaireContacts!!.contacts.retainAll(filteredContact)

                    if (len > 1) {
                        gridViewAdapter = ContactGridViewAdapter(this@MainActivity, gestionnaireContacts, len)
                        main_GridView!!.adapter = gridViewAdapter
                    } else {
                        listViewAdapter = ContactListViewAdapter(this@MainActivity, gestionnaireContacts!!.contacts, len)
                        main_ListView!!.adapter = listViewAdapter
                        listViewAdapter!!.notifyDataSetChanged()
                    }
                }
                return true
            }
            R.id.mail_filter -> {
                if (item.isChecked) {
                    item.setChecked(false)
                    main_filter.remove("mail")
                    // duplicate
                    main_search_bar_value = main_SearchBar!!.text.toString()

                    val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
                    val len = sharedPreferences.getInt("gridview", 4)
                    val filteredContact = gestionnaireContacts!!.getContactConcernByFilter(main_filter, main_search_bar_value)
                    val contactListDb = ContactList(this)
                    if (sharedPreferences.getString("tri", "nom") == "nom") {
                        contactListDb.sortContactByFirstNameAZ()
                        contactListDb.contacts.retainAll(filteredContact)
                    } else {
                        contactListDb.sortContactByPriority()
                        contactListDb.contacts.retainAll(filteredContact)
                    }
                    gestionnaireContacts!!.contacts.clear()
                    gestionnaireContacts!!.contacts.addAll(contactListDb.contacts)
                    if (len > 1) {
                        gridViewAdapter = ContactGridViewAdapter(this@MainActivity, gestionnaireContacts, len)
                        main_GridView!!.adapter = gridViewAdapter
                    } else {
                        listViewAdapter = ContactListViewAdapter(this@MainActivity, gestionnaireContacts!!.contacts, len)
                        main_ListView!!.adapter = listViewAdapter
                        listViewAdapter!!.notifyDataSetChanged()
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
                    } else {
                        gestionnaireContacts!!.sortContactByPriority()
                    }
                    gestionnaireContacts!!.contacts.retainAll(filteredContact)
                    if (len > 1) {
                        gridViewAdapter = ContactGridViewAdapter(this@MainActivity, gestionnaireContacts, len)
                        main_GridView!!.adapter = gridViewAdapter
                    } else {
                        listViewAdapter = ContactListViewAdapter(this@MainActivity, gestionnaireContacts!!.contacts, len)
                        main_ListView!!.adapter = listViewAdapter

                        listViewAdapter!!.notifyDataSetChanged()
                    }
                }
                return true
            }
            R.id.tri_par_nom -> {
                if (!item.isChecked) {
                    item.isChecked = true
                    gestionnaireContacts!!.sortContactByFirstNameAZ()
                    val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
                    val len = sharedPreferences.getInt("gridview", 4)
                    if (len > 1) {
                        gridViewAdapter = ContactGridViewAdapter(this@MainActivity, gestionnaireContacts, len)
                        main_GridView!!.adapter = gridViewAdapter
                    } else {
                        listViewAdapter = ContactListViewAdapter(this@MainActivity, gestionnaireContacts!!.contacts, len)
                        main_ListView!!.adapter = listViewAdapter
                        listViewAdapter!!.notifyDataSetChanged()
                    }
                    val edit: SharedPreferences.Editor = sharedPreferences.edit()
                    edit.putString("tri", "nom")
                    edit.apply()
                }
            }
            R.id.tri_par_priorite -> {
                if (!item.isChecked) {
                    item.setChecked(true);
                    gestionnaireContacts!!.sortContactByPriority()
                    val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
                    val len = sharedPreferences.getInt("gridview", 4)
                    if (len > 1) {
                        gridViewAdapter = ContactGridViewAdapter(this@MainActivity, gestionnaireContacts, len)
                        main_GridView!!.adapter = gridViewAdapter
                    } else {
                        listViewAdapter = ContactListViewAdapter(this@MainActivity, gestionnaireContacts!!.contacts, len)
                        main_ListView!!.adapter = listViewAdapter
                        listViewAdapter!!.notifyDataSetChanged()
                    }

                    val edit: SharedPreferences.Editor = sharedPreferences.edit()
                    edit.putString("tri", "priorite")
                    edit.apply()
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

    fun longGridItemClick(len: Int, position: Int) {
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
    }

    private fun verifiedContactsChannel(listOfItemSelected: ArrayList<ContactWithAllInformation>) {
        val iterator = (0 until listOfItemSelected.size).iterator()
        var allContactsHaveMail = true
        var allContactsHavePhoneNumber = true

        for (i in iterator) {
            if (listOfItemSelected[i].getFirstMail() == "") {
                allContactsHaveMail = false
            }

            if (listOfItemSelected[i].getPhoneNumber() == "") {
                allContactsHavePhoneNumber = false
            }
        }

        if (allContactsHaveMail) {
            main_MailButton!!.visibility = View.VISIBLE
        } else {
            main_MailButton!!.visibility = View.GONE
        }

        if (allContactsHavePhoneNumber) {
            main_SMSButton!!.visibility = View.VISIBLE
        } else {
            main_SMSButton!!.visibility = View.GONE
        }

        if (appIsInstalled() && allContactsHavePhoneNumber) {
            main_WhatsappButton!!.visibility = View.VISIBLE
        } else {
            main_WhatsappButton!!.visibility = View.GONE
        }
    }

    fun longRecyclerItemClick(position: Int, view: View, contactViewHolder: ContactViewHolder) {
        var holder = contactViewHolder

        holder.contactRoundedImageView = view.findViewById(R.id.list_contact_item_contactRoundedImageView)
        holder.contactFirstNameView = view.findViewById(R.id.list_contact_item_contactFirstName)

        view.tag = holder

        val contact = gestionnaireContacts!!.contacts[position].contactDB

        holder.contactFirstNameView.text = contact!!.firstName

        if (gestionnaireContacts!!.contacts.contains(gestionnaireContacts!!.contacts[position])) {
            if (contact.profilePicture64 != "") {
                val bitmap = base64ToBitmap(contact.profilePicture64)
                holder.contactRoundedImageView.setImageBitmap(bitmap)
            } else {
                holder.contactRoundedImageView.setImageResource(randomDefaultImage(contact.profilePicture, "Get")) //////////////
            }
        } else {
            holder.contactRoundedImageView.setImageResource(R.drawable.ic_contact_selected)
            Toast.makeText(this, R.string.main_toast_multi_select_actived, Toast.LENGTH_SHORT).show()
        }

        main_FloatingButtonAdd!!.visibility = View.GONE
        main_FloatingButtonSend!!.visibility = View.VISIBLE
        main_SearchBar!!.visibility = View.GONE
        firstClick = false

        if (gestionnaireContacts!!.contacts[position].getFirstMail() != "") {
            main_MailButton!!.visibility = View.VISIBLE
        }

        if (appIsInstalled() && gestionnaireContacts!!.contacts[position].getPhoneNumber() != "") {
            main_WhatsappButton!!.visibility = View.VISIBLE
        }

        if (gestionnaireContacts!!.contacts[position].getPhoneNumber() != "") {
            main_SMSButton!!.visibility = View.VISIBLE
        }
    }

//    fun recyclerItemClick(len: Int, position: Int) {
////        if (main_RecyclerView!!.adapter is SelectContactAdapter && !firstClick) {
//        val adapter = (main_RecyclerView!!.adapter as SelectContactAdapter)
//        adapter.itemSelected(position)
//        if (adapter.listContactSelect.size == 0) {
//            main_RecyclerView!!.adapter = ContactRecyclerViewAdapter(this, gestionnaireContacts!!.contacts, len)
//            main_FloatingButtonAdd!!.visibility = View.VISIBLE
//            main_FloatingButtonSend!!.visibility = View.GONE
//            main_SearchBar!!.visibility = View.VISIBLE
//
//            Toast.makeText(this, R.string.main_toast_multi_select_deactived, Toast.LENGTH_SHORT).show()
//        }
////        }
//        firstClick = false
//    }

    private fun appIsInstalled(): Boolean {
        val pm = this.packageManager
        return try {
            pm.getApplicationInfo("com.whatsapp", 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun monoChannelSmsClick(listOfPhoneNumber: ArrayList<String>) {

        var message = "smsto:" + listOfPhoneNumber[0]
        for (i in 1 until listOfPhoneNumber.size) {
            message += ";" + listOfPhoneNumber[i]
        }
        startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse(message)))
    }

    private fun base64ToBitmap(base64: String): Bitmap {

        val decodedString = Base64.decode(base64, Base64.DEFAULT)
        val options = BitmapFactory.Options()
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size, options)
    }

    private fun randomDefaultImage(avatarId: Int, createOrGet: String): Int {
        if (createOrGet == "Create") {
            return Random().nextInt(7)
        } else if (createOrGet == "Get") {
            return when (avatarId) {
                0 -> R.drawable.ic_user_purple
                1 -> R.drawable.ic_user_blue
                2 -> R.drawable.ic_user_knocker
                3 -> R.drawable.ic_user_green
                4 -> R.drawable.ic_user_om
                5 -> R.drawable.ic_user_orange
                6 -> R.drawable.ic_user_pink
                else -> R.drawable.ic_user_blue
            }
        }
        return -1
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

    //endregion
}