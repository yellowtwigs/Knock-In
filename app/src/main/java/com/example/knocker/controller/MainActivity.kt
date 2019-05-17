package com.example.knocker.controller

import android.Manifest
import android.app.AlertDialog
import android.content.*
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import android.text.TextUtils
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import com.example.knocker.*
import com.example.knocker.model.*
import me.samthompson.bubbleactions.BubbleActions
import java.util.*

class MainActivity : AppCompatActivity() {

    //region ========================================== Var or Val ==========================================

    private var main_GridView: GridView? = null
    private var drawerLayout: DrawerLayout? = null
    private var main_FloatingButtonOpen: FloatingActionButton? = null
    private var main_FloatingButtonAdd: FloatingActionButton? = null
    private var main_FloatingButtonCompose: FloatingActionButton? = null
    private var main_FloatingButtonSync: FloatingActionButton? = null
    private var main_FloatingButtonOpenAnimation: Animation? = null
    private var main_FloatingButtonCloseAnimation: Animation? = null
    private var main_FloatingButtonClockWiserAnimation: Animation? = null
    private var main_FloatingButtonAntiClockWiserAnimation: Animation? = null
    private var main_CoordinationLayout: CoordinatorLayout? = null
    internal var main_FloatingButtonIsOpen = false
    internal var main_search_bar_value = ""
    private var main_filter = arrayListOf<String>()
    private var main_SearchBar: EditText? = null
    var scaleGestureDetectore: ScaleGestureDetector? = null

    // Database && Thread
    private var main_ContactsDatabase: ContactsRoomDatabase? = null
    private lateinit var main_mDbWorkerThread: DbWorkerThread

    private var main_BottomNavigationView: BottomNavigationView? = null
    private var phone_call_layout: ConstraintLayout? = null
    private var my_informations_layout: ConstraintLayout? = null

    //endregion

    /**
     *
     * @param Bundle @type
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        //region ============================= Check For AppTheme or DarkTheme ==============================

        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.DarkTheme)
        } else {
            setTheme(R.style.AppTheme)
        }

        //endregion

        super.onCreate(savedInstanceState)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            setContentView(R.layout.activity_main)
        } else {
            setContentView(R.layout.activity_main)
        }

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), 1)
        }
        if (!isNotificationServiceEnabled) {
            //val alertDialog = buildNotificationServiceAlertDialog()
            // alertDialog.show()
            val sharedPreferences = getSharedPreferences("Knocker_preferences", Context.MODE_PRIVATE)
            val edit: SharedPreferences.Editor = sharedPreferences.edit()
            edit.putBoolean("popupNotif", false)//quand la personne autorise l'affichage par dessus d'autre application nous l'enregistrons
            edit.putBoolean("serviceNotif", true)
            edit.commit()
        } else {
            toggleNotificationListenerService()

        }

        // on init WorkerThread
        main_mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        main_mDbWorkerThread.start()

        //on get la base de données
        main_ContactsDatabase = ContactsRoomDatabase.getDatabase(this)

        //region ====================================== FindViewById() ======================================

        // Floating Button
        main_FloatingButtonOpen = findViewById(R.id.main_floating_button_open_id)
        main_FloatingButtonAdd = findViewById(R.id.main_floating_button_add_id)
        main_FloatingButtonCompose = findViewById(R.id.main_floating_button_compose_id)
        main_FloatingButtonSync = findViewById(R.id.main_floating_button_sync_id)
        main_FloatingButtonOpenAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.fab_open)
        main_FloatingButtonCloseAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.fab_close)
        main_FloatingButtonClockWiserAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.rotate_clockwiser)
        main_FloatingButtonAntiClockWiserAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.rotate_anticlockwiser)

        main_BottomNavigationView = findViewById(R.id.navigation)
        main_BottomNavigationView!!.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        phone_call_layout = findViewById(R.id.phone_call_layout_id)
        my_informations_layout = findViewById(R.id.my_informations_layout_id)

        // Search bar
        main_SearchBar = findViewById(R.id.main_search_bar)

        //endregion

        //region ========================================== Toolbar =========================================

        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar.setHomeAsUpIndicator(R.drawable.ic_open_drawer)

        //endregion

        //region ======================================= DrawerLayout =======================================

        // Drawerlayout
        drawerLayout = findViewById(R.id.drawer_layout)

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            drawerLayout!!.closeDrawers()

            val id = menuItem.itemId

            if (id == R.id.nav_home) {
                val loginIntent = Intent(this@MainActivity, MainActivity::class.java)
                startActivity(loginIntent)
                finish()
            } else if (id == R.id.nav_settings) {
                val loginIntent = Intent(this@MainActivity, SettingsActivity::class.java)
                startActivity(loginIntent)
            } else if (id == R.id.nav_chat) {
                val loginIntent = Intent(this@MainActivity, ChatActivity::class.java)
                startActivity(loginIntent)
            } else if (id == R.id.nav_history) {
                val loginIntent = Intent(this@MainActivity, NotificationHistoryActivity::class.java)
                startActivity(loginIntent)
            }

            val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
            drawer.closeDrawer(GravityCompat.START)
            true
        }

        //endregion

        //affiche tout les contacts de la Database
        val printContacts = Runnable {
            // Grid View
            main_GridView = findViewById(R.id.main_grid_view_id)
            ////////
            val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
            val len = sharedPreferences.getInt("gridview", 4)
            main_GridView!!.setNumColumns(len) // permet de changer

            println("OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO = " + GroupsUsefulFunctions.stringIdToList("1,2000,3"))
            var contactList: List<Contacts>?
            if (main_ContactsDatabase?.contactsDao()?.getAllContacts()!!.isEmpty()) {
                //contactList= null
                val contact = ListContact.loadJSONFromAsset(this)
                contactList = ListContact.buildList(contact)
                println("contact list size" + contactList.size)
            } else {
                contactList = main_ContactsDatabase?.contactsDao()?.sortContactByFirstNameAZ()
                println("list contact db " + contactList)
            }

            if (main_GridView != null && contactList != null) {
                val contactAdapter = ContactAdapter(this, contactList, len)
                main_GridView!!.adapter = contactAdapter
                var index = sharedPreferences.getInt("index", 0)
                println("okkkkkkk = " + index)
                val edit: SharedPreferences.Editor = sharedPreferences.edit()
                main_GridView!!.setSelection(index)
                edit.putInt("index", 0)
                edit.commit()

//                main_GridView!!.onItemLongClickListener = AdapterView.OnItemLongClickListener { _, _, position, _ ->
//                    main_CoordinationLayout = findViewById<CoordinatorLayout>(R.id.main_coordinatorLayout)
//                    val contact = main_GridView!!.getItemAtPosition(position) as Contacts
//                    BubbleActions.on(main_CoordinationLayout)
//                            .addAction("Messenger", R.drawable.ic_messenger_circle_menu,
//                                    {
//                                        ContactGesture.openMessenger("", this@MainActivity)
//                                    }).addAction("SMS", R.drawable.ic_sms, {
//                                val intent = ContactGesture.putContactIntent(contact, this@MainActivity, ComposeMessageActivity::class.java)
//                                startActivity(intent)
//                            }).addAction("Gmail", R.drawable.ic_gmail, {
//                                ContactGesture.openGmail(this@MainActivity)
//                            }).addAction("whatsapp", R.drawable.ic_whatsapp_circle_menu, {
//                                ContactGesture.openWhatsapp(contact.phoneNumber, this@MainActivity)
//                            }).addAction("edit", R.drawable.ic_edit_floating_button, {
//                                val intent = ContactGesture.putContactIntent(contact, this@MainActivity, EditContactActivity::class.java)
//                                startActivity(intent)
//                            })
//                            .show()
//                    false
//                }

                main_GridView!!.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                    if (main_FloatingButtonIsOpen == false) {
                        //Save position in gridview
                        //val state = main_GridView!!.onSaveInstanceState()
                        index = main_GridView!!.getFirstVisiblePosition()
                        println("wheeeeeeee = " + index)

                        //val edit : SharedPreferences.Editor = sharedPreferences.edit()
                        edit.putInt("index", index)
                        edit.commit()
                        //
                        val o = main_GridView!!.getItemAtPosition(position)
                        val contact = o as Contacts
                        var intent:Intent
                        if(main_ContactsDatabase?.contactsDao()?.getAllContacts()!!.isEmpty()) {
                             intent = ContactGesture.putContactIntent(contact," "," ", this@MainActivity, ContactDetailsActivity::class.java)
                        }else{

                            val mail = main_ContactsDatabase!!.contactDetailsDao().getMailById(contact.id)
                            val phone=main_ContactsDatabase!!.contactDetailsDao().getPhoneNumberById(contact.id)
                            println(contact.id.toString()+" phone and mail" +mail+ "   "+phone)
                             intent = ContactGesture.putContactIntent(contact, phone.contactDetails,mail.contactDetails,this@MainActivity, ContactDetailsActivity::class.java)
                        }
                        startActivity(intent)
                    } else {
                        main_FloatingButtonIsOpen = false
                        onFloatingClickBack()
                    }

                }
                // Drag n Drop

            }
        }
        runOnUiThread(printContacts)

        //main_mDbWorkerThread.postTask(printContacts)

        //region ==================================== SetOnClickListener ====================================

        main_FloatingButtonOpen!!.setOnClickListener {
            if (main_FloatingButtonIsOpen) {
                onFloatingClickBack()
                main_FloatingButtonIsOpen = false
            } else {
                onFloatingClick()
                main_FloatingButtonIsOpen = true
            }
        }

        main_FloatingButtonAdd!!.setOnClickListener {
            val loginIntent = Intent(this@MainActivity, AddNewContactActivity::class.java)
            startActivity(loginIntent)
            onFloatingClickBack()
            main_FloatingButtonIsOpen = false
        }

        main_FloatingButtonCompose!!.setOnClickListener {
            val loginIntent = Intent(this@MainActivity, ComposeMessageActivity::class.java)
            startActivity(loginIntent)
            onFloatingClickBack()
            main_FloatingButtonIsOpen = false
        }

        //bouton synchronisation des contacts du téléphone
        main_FloatingButtonSync!!.setOnClickListener(View.OnClickListener {
            //récupère tout les contacts du téléphone et les stock dans phoneContactsList et supprime les doublons
            val phoneContactsList = ContactSync.getAllContacsInfo(contentResolver)//ContactSync.getAllContact(contentResolver)

            //Ajoute tout les contacts dans la base de données en vérifiant si il existe pas avant

            val addAllContacts = Runnable {
                var isDuplicate: Boolean
                val allcontacts = main_ContactsDatabase?.contactsDao()?.sortContactByFirstNameAZ()
                //val priority = ContactsPriority.getPriorityWithName("Ryan Granet", "sms", allcontacts)
                //println("priorité === "+priority)
                phoneContactsList?.forEach { phoneContactList ->
                    isDuplicate = false
                    allcontacts?.forEach { contactsDB ->
                        //println("LOOOOOOOOOOOOP "+ contactsDB)
                        if (contactsDB.firstName == phoneContactList.firstName && contactsDB.lastName == phoneContactList.lastName)
                            isDuplicate = true
                        println("STATE = " + isDuplicate) //////////
                    }
                    if (isDuplicate == false) {
                        val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
                        val len = sharedPreferences.getInt("gridview", 3)
                        println("PLSSSSSSSSSSSSSS " + phoneContactList)
                        main_ContactsDatabase?.contactsDao()?.insert(phoneContactList)
                        val syncContact = main_ContactsDatabase?.contactsDao()?.getAllContacts()
                        //for(x in syncContact)
                        val contactAdapter = ContactAdapter(this, syncContact, len)
                        main_GridView!!.adapter = contactAdapter

                    }
                }
            }
            runOnUiThread(addAllContacts)
        })

        //endregion

        val isDelete = intent.getBooleanExtra("isDelete", false)
        if (isDelete) {
            Toast.makeText(this, "Vous venez de supprimer un contact !", Toast.LENGTH_LONG).show()
        }
        scaleGestureDetectore = ScaleGestureDetector(this,
                MyOnScaleGestureListener())
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleGestureDetectore?.onTouchEvent(event)
        return true
    }

    //
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

    //region ========================================== Functions ===========================================

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_phone_book -> {
                phone_call_layout!!.visibility = View.GONE
                main_GridView!!.visibility = View.VISIBLE
                main_FloatingButtonOpen!!.visibility = View.VISIBLE
                my_informations_layout!!.visibility = View.GONE
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_user -> {
                my_informations_layout!!.visibility = View.VISIBLE
                main_FloatingButtonOpen!!.visibility = View.GONE
                phone_call_layout!!.visibility = View.GONE
                main_GridView!!.visibility = View.GONE
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_phone_keyboard -> {
                phone_call_layout!!.visibility = View.VISIBLE
                main_FloatingButtonOpen!!.visibility = View.GONE
                main_GridView!!.visibility = View.GONE
                my_informations_layout!!.visibility = View.GONE
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    // fonction qui filtre
    private fun getAllContactFilter(filterList: ArrayList<String>): List<Contacts>? {
        val allFilters: MutableList<List<Contacts>> = mutableListOf()
        var filter: List<Contacts>?
        println(filterList)
        if (filterList.contains("sms")) {
            println("GOOOOOD")
            filter = main_ContactsDatabase?.contactsDao()?.getContactWithPhoneNumber()
            if (filter != null && filter.isEmpty() == false)
                allFilters.add(filter)
        }
        if (filterList.contains("mail")) {
            filter = main_ContactsDatabase?.contactsDao()?.getContactWithMail()
            println("FILTER = ! " + filter)
            if (filter != null && filter.isEmpty() == false)
                allFilters.add(filter)
        } else
            return null
        var i = 0
        println(allFilters.size.toString() + " CODYYYYYYYYYYYYYYYYYYYYY")
        if (allFilters.size > 1) {
            while (i < allFilters.size - 1) {
                allFilters[i + 1] = allFilters[i].intersect(allFilters[i + 1]).toList()
                i++
            }
        } else if (allFilters.size == 0) {
            return null
        } else
            return allFilters[0]
        println("all filter [i] = " + allFilters)
        return allFilters[i]
    }

    //check les checkbox si elle ont été check apres une recherche
    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        super.onPrepareOptionsMenu(menu)
        val main_filter = intent.getStringArrayListExtra("Filter")
        if (main_filter != null && main_filter.contains("sms")) {
            menu?.findItem(R.id.sms_filter)?.setChecked(true)
            intent.putStringArrayListExtra("Filter", main_filter)
        }
        if (main_filter != null && main_filter.contains("mail")) {
            menu?.findItem(R.id.mail_filter)?.setChecked(true)
            intent.putStringArrayListExtra("Filter", main_filter)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                drawerLayout!!.openDrawer(GravityCompat.START)
                return true
            }
            R.id.nav_search -> {
                if (main_SearchBar!!.visibility != View.VISIBLE) {
                    main_SearchBar!!.visibility = View.VISIBLE
                } else {
                    main_search_bar_value = main_SearchBar!!.text.toString()
                    println(main_filter)
                    println(main_SearchBar!!.text.toString())
                    //
                    val contactFilterList: List<Contacts>? = getAllContactFilter(main_filter) //main filter value [sms,mail]
                    var contactList = main_ContactsDatabase?.contactsDao()?.getContactByName(main_search_bar_value) //" test "
                    println(contactFilterList)
                    if (contactFilterList != null) {
                        contactList = contactList!!.intersect(contactFilterList).toList()
                    }
                    //
                    val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
                    val len = sharedPreferences.getInt("gridview", 3)
                    //val syncContact = main_ContactsDatabase?.contactsDao()?.getAllContacts()
                    println("LIIIIIIST = " + contactList)
                    val contactAdapter = ContactAdapter(this, contactList, len)
                    main_GridView!!.adapter = contactAdapter
                    //startActivity(intent)
                }
            }
            R.id.sms_filter -> {
                if (item.isChecked) {
                    item.setChecked(false)
                    main_filter.remove("sms")
                } else {
                    item.setChecked(true)
                    main_filter.add("sms")
                }
                return true
            }
            R.id.mail_filter -> {
                if (item.isChecked) {
                    item.setChecked(false)
                    main_filter.remove("mail")
                } else {
                    item.setChecked(true)
                    main_filter.add("mail")
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return true
    }

    fun onFloatingClickBack() {
        main_FloatingButtonAdd!!.startAnimation(main_FloatingButtonCloseAnimation)
        main_FloatingButtonCompose!!.startAnimation(main_FloatingButtonCloseAnimation)
        main_FloatingButtonSync!!.startAnimation(main_FloatingButtonCloseAnimation)
        main_FloatingButtonOpen!!.startAnimation(main_FloatingButtonAntiClockWiserAnimation)

        main_FloatingButtonAdd!!.isClickable = false
        main_FloatingButtonCompose!!.isClickable = false
        main_FloatingButtonSync!!.isClickable = false
    }

    fun onFloatingClick() {
        main_FloatingButtonAdd!!.startAnimation(main_FloatingButtonOpenAnimation)
        main_FloatingButtonCompose!!.startAnimation(main_FloatingButtonOpenAnimation)
        main_FloatingButtonSync!!.startAnimation(main_FloatingButtonOpenAnimation)
        main_FloatingButtonOpen!!.startAnimation(main_FloatingButtonClockWiserAnimation)

        main_FloatingButtonAdd!!.isClickable = true
        main_FloatingButtonCompose!!.isClickable = true
        main_FloatingButtonSync!!.isClickable = true
    }

    private val isNotificationServiceEnabled: Boolean
        get() {
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

    fun toggleNotificationListenerService() {
        val pm = getPackageManager()
        val cmpName = ComponentName(this, NotificationListener::class.java)
        pm.setComponentEnabledSetting(cmpName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
        pm.setComponentEnabledSetting(cmpName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
    }

    //endregion
}

