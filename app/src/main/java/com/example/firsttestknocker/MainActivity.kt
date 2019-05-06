package com.example.firsttestknocker

import android.Manifest
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Settings
import android.support.design.widget.CoordinatorLayout
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatDelegate
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import me.samthompson.bubbleactions.BubbleActions
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*

class MainActivity : AppCompatActivity() {

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
    private var main_CoordinationLayout:CoordinatorLayout? = null
    internal var isOpen = false
    internal var main_search_bar_value = ""
    private var main_filter = arrayListOf<String>()
    private var main_SearchBar: EditText? = null

    // Database && Thread
    private var main_ContactsDatabase: ContactsRoomDatabase? = null
    private lateinit var main_mDbWorkerThread: DbWorkerThread
    //private lateinit var mainContactsPriority: ContactsPriority

    override fun onCreate(savedInstanceState: Bundle?) {
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.DarkTheme)
        } else {
            setTheme(R.style.AppTheme)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), 1)
        }
        if (!isNotificationServiceEnabled) {
            //val alertDialog = buildNotificationServiceAlertDialog()
            // alertDialog.show()
            val sharedPreferences = getSharedPreferences("Knocker_preferences", Context.MODE_PRIVATE)
            val edit : SharedPreferences.Editor = sharedPreferences.edit()
            edit.putBoolean("popupNotif",false)//quand la personne autorise l'affichage par dessus d'autre application nous l'enregistrons
            edit.putBoolean("serviceNotif",true)
            edit.commit()
        }else{
            toggleNotificationListenerService()

        }

        // on init WorkerThread
        main_mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        main_mDbWorkerThread.start()

        //on get la base de données
        main_ContactsDatabase = ContactsRoomDatabase.getDatabase(this)

        // Floating Button
        main_FloatingButtonOpen = findViewById(R.id.main_floating_button_open_id)
        main_FloatingButtonAdd = findViewById(R.id.main_floating_button_add_id)
        main_FloatingButtonCompose = findViewById(R.id.main_floating_button_compose_id)
        main_FloatingButtonSync = findViewById(R.id.main_floating_button_sync_id)
        main_FloatingButtonOpenAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.fab_open)
        main_FloatingButtonCloseAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.fab_close)
        main_FloatingButtonClockWiserAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.rotate_clockwiser)
        main_FloatingButtonAntiClockWiserAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.rotate_anticlockwiser)

        // Search bar
        main_SearchBar = findViewById(R.id.main_search_bar)
        val main_search_bar = intent.getStringExtra("SearchBar")
        val main_filter_value = intent.getStringArrayListExtra("Filter")
        if (main_filter_value != null)
            main_filter = main_filter_value

        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar.setHomeAsUpIndicator(R.drawable.ic_open_drawer)

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
            } else if (id == R.id.nav_history){
                val loginIntent = Intent(this@MainActivity, NotificationHistoryActivity::class.java)
                startActivity(loginIntent)
            }

            val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
            drawer.closeDrawer(GravityCompat.START)
            true
        }

        //affiche tout les contacts de la Database
        val printContacts = Runnable {
            // Grid View
            main_GridView = findViewById(R.id.main_grid_view_id)
            //////////////
            val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
            val len = sharedPreferences.getInt("gridview",3)
            main_GridView!!.setNumColumns(len) // permet de changer
            var contactList: List<Contacts>?
            if(main_ContactsDatabase?.contactsDao()?.getAllContacts()!!.isEmpty()){
                contactList= null
                val contact= ListContact.loadJSONFromAsset(this)
                contactList= ListContact.construireListe(contact)
                println("contact list size"+ contactList.size)
            }else {
                if (main_search_bar == null || main_search_bar == "" && main_filter_value == null || main_search_bar == "" && main_filter_value.isEmpty() == true) {
                    contactList = main_ContactsDatabase?.contactsDao()?.getAllContacts()

                } else {
                    val contactFilterList: List<Contacts>? = getAllContactFilter(main_filter_value)
                    contactList = main_ContactsDatabase?.contactsDao()?.getContactByName(main_search_bar)
                    println(contactFilterList)
                    if (contactFilterList != null) {
                        contactList = contactList!!.intersect(contactFilterList).toList()
                    }
                }
             }
            if (main_GridView != null) {
                val contactAdapter = ContactAdapter(this, contactList, len)
                main_GridView!!.adapter = contactAdapter
                if (android.os.Build.VERSION.SDK_INT != 28) {
                    main_GridView!!.onItemLongClickListener = AdapterView.OnItemLongClickListener { _, _, position, _ ->
                        val item = main_GridView!!.getChildAt(position)
                        main_CoordinationLayout = findViewById<CoordinatorLayout>(R.id.main_coordinatorLayout)
                        val contact = main_GridView!!.getItemAtPosition(position) as Contacts
                        BubbleActions.on(main_CoordinationLayout)
                                .addAction("Messenger", R.drawable.ic_messenger_circle_menu,
                                        {
                                            ContactGesture.openMessenger("", this@MainActivity)
                                        }).addAction("SMS", R.drawable.sms, {
                                    val intent = ContactGesture.putContactIntent(contact, this@MainActivity, ComposeMessageActivity::class.java)
                                    startActivity(intent)
                                }).addAction("Gmail", R.drawable.gmail, {
                                    ContactGesture.openGmail(this@MainActivity)
                                }).addAction("whatsapp", R.drawable.ic_whatsapp_circle_menu, {
                                    ContactGesture.openWhatsapp(contact.phoneNumber, this@MainActivity)
                                }).addAction("edit", R.drawable.ic_edit_floating_button, {
                                    val intent = ContactGesture.putContactIntent(contact, this@MainActivity, EditContactActivity::class.java)
                                    startActivity(intent)
                                })
                                .show()
                        false
                    }
                }



                main_GridView!!.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                    if (isOpen == false) {
                        val o = main_GridView!!.getItemAtPosition(position)
                        val contact = o as Contacts

                        val intent =ContactGesture.putContactIntent(contact, this@MainActivity, ContactDetailsActivity::class.java)
                        startActivity(intent)
                    } else {
                        isOpen = false
                        onFloatingClickBack()
                    }

                }
                // Drag n Drop

            }
        }
        runOnUiThread(printContacts)
        //main_mDbWorkerThread.postTask(printContacts)

        main_FloatingButtonOpen!!.setOnClickListener {
            if (isOpen) {
                onFloatingClickBack()
                isOpen = false
            } else {
                onFloatingClick()
                isOpen = true
            }
        }

        main_FloatingButtonAdd!!.setOnClickListener {
            val loginIntent = Intent(this@MainActivity, AddNewContactActivity::class.java)
            startActivity(loginIntent)
        }

        main_FloatingButtonCompose!!.setOnClickListener {
            val loginIntent = Intent(this@MainActivity, ComposeMessageActivity::class.java)
            startActivity(loginIntent)
        }

        //bouton synchronisation des contacts du téléphone
        main_FloatingButtonSync!!.setOnClickListener(View.OnClickListener {
            //récupère tout les contacts du téléphone et les stock dans phoneContactsList et supprime les doublons
            val phoneContactsList = ContactSync.getAllContacsInfo(contentResolver)//ContactSync.getAllContact(contentResolver)

                //Ajoute tout les contacts dans la base de données en vérifiant si il existe pas avant

            val addAllContacts = Runnable {
                var isDuplicate = false
                val allcontacts = main_ContactsDatabase?.contactsDao()?.getAllContacts()
                //val priority = ContactsPriority.getPriorityWithName("Ryan Granet", "sms", allcontacts)
                //println("priorité === "+priority)
                phoneContactsList?.forEach { phoneContactList ->
                    allcontacts?.forEach { contactsDB ->
                        if (contactsDB.firstName == phoneContactList.firstName && contactsDB.lastName == phoneContactList.lastName)
                            isDuplicate = true
                    }
                    if (isDuplicate == false) {
                        main_ContactsDatabase?.contactsDao()?.insert(phoneContactList)
                    }
                }
                val intent = intent
                finish()
                startActivity(intent)
            }
            main_mDbWorkerThread.postTask(addAllContacts)
        })

        val isDelete = intent.getBooleanExtra("isDelete", false)
        if (isDelete) {
            Toast.makeText(this, "Vous venez de supprimer un contact !", Toast.LENGTH_LONG).show()
        }

        val threadVerifPopup = Runnable {
            if(Build.VERSION.SDK_INT>=23) {
                if (!Settings.canDrawOverlays(this)) {
                    if(ContactsPriority.checkPriority2(main_ContactsDatabase?.contactsDao()?.getAllContacts() )){
                        val alertDialog = OverlayAlertDialog()
                        alertDialog.show()
                        println("test overlay")
                    }
                }
            }
        }// comme nous faisons appel a la bdd nous lançons un thread
        main_mDbWorkerThread.postTask(threadVerifPopup)
    }

    // fonction qui filtre
    private fun getAllContactFilter(filterList: ArrayList<String>): List<Contacts>? {
        val allFilters: MutableList<List<Contacts>> = mutableListOf()
        var filter: List<Contacts>? = null

        if (filterList.contains("sms")) {
            filter = main_ContactsDatabase?.contactsDao()?.getContactWithPhoneNumber()
            if (filter != null && filter.isEmpty() == false)
                allFilters.add(filter)
        }
        if (filterList.contains("mail")) {
            filter = main_ContactsDatabase?.contactsDao()?.getContactWithMail()
            if (filter != null && filter.isEmpty() == false)
                allFilters.add(filter)
        } else
            return null
        var i = 0
        if (allFilters.size != 1) {
            while (i < allFilters.size - 1) {
                allFilters[i+1] = allFilters[i].intersect(allFilters[i+1]).toList()
                i++
            }
        } else
            return allFilters[0]
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
                main_SearchBar!!.visibility = View.VISIBLE
                main_search_bar_value = main_SearchBar!!.text.toString()
                intent.putExtra("SearchBar", main_search_bar_value)
                println(main_filter)
                intent.putStringArrayListExtra("Filter", main_filter)
                println(main_SearchBar!!.text.toString())
//                startActivity(intent)
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

//    private fun buildNotificationServiceAlertDialog(): AlertDialog {
//        val alertDialogBuilder = AlertDialog.Builder(this)
//        val inflater : LayoutInflater = this.getLayoutInflater()
//        val alertView: View = inflater.inflate(R.layout.alert_dialog_notification,null);
//        alertDialogBuilder.setView(alertView);
//        val alertDialog = alertDialogBuilder.create()
//        val tvNo= alertView.findViewById<TextView>(R.id.tv_alert_dialog)
//        tvNo.setOnClickListener {
//            alertDialog.cancel()
//        };
//        val btnYes = alertView.findViewById<Button>(R.id.button_alert_dialog)
//        btnYes.setOnClickListener{
//            startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
//            val intentFilter = IntentFilter()
//            intentFilter.addAction("com.example.firsttestknocker.notificationExemple")
//            alertDialog.cancel()
//        }
//        return alertDialog
//    }

    //TODO: modifier l'alert dialog en ajoutant une vue pour le rendre joli.
    private fun OverlayAlertDialog(): AlertDialog {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Knocker")
        alertDialogBuilder.setMessage("vous voulez vous autouriser knocker à afficher des notifications directement sur d'autre application")
        alertDialogBuilder.setPositiveButton("oui"
        ) { dialog, id ->
            val intentPermission = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivity(intentPermission)
            val sharedPreferences = getSharedPreferences("Knocker_preferences", Context.MODE_PRIVATE)
            val edit : SharedPreferences.Editor = sharedPreferences.edit()
            edit.putBoolean("popupNotif",true)//quand la personne autorise l'affichage par dessus d'autre application nous l'enregistrons
            edit.putBoolean("serviceNotif",false)
            edit.commit()

        }
        alertDialogBuilder.setNegativeButton("non"
        ) { dialog, id ->
            val sharedPreferences = getSharedPreferences("Knocker_preferences", Context.MODE_PRIVATE)
            val edit : SharedPreferences.Editor = sharedPreferences.edit()
            edit.putBoolean("popupNotif",false)//quand la personne autorise l'affichage par dessus d'autre application nous l'enregistrons
            edit.putBoolean("serviceNotif",true)
            edit.commit()
        }
        return alertDialogBuilder.create()
    }
    fun toggleNotificationListenerService(){
        val pm =getPackageManager()
        val cmpName = ComponentName(this,NotificationListener::class.java)
        pm.setComponentEnabledSetting(cmpName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
        pm.setComponentEnabledSetting(cmpName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
    }

}

