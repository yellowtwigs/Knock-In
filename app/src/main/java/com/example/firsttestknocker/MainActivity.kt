package com.example.firsttestknocker

import android.Manifest
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
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
import android.support.v4.app.ActivityCompat
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*

import java.util.ArrayList

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
    internal var isOpen = false
    //<check Kenzy
    internal var main_search_bar_value = ""
    private var main_filter: MutableList<String> = mutableListOf<String>()
    private var main_SearchBar: EditText? = null
    //check Kenzy>

    // Database && Thread
    private var main_ContactsDatabase: ContactsRoomDatabase? = null
    private lateinit var main_mDbWorkerThread: DbWorkerThread

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), 1)
        }
        if (!isNotificationServiceEnabled) {

            val alertDialog = buildNotificationServiceAlertDialog()
            alertDialog.show()
        }
        if(Build.VERSION.SDK_INT>=23) {
            if (!Settings.canDrawOverlays(this)) {
                val alertDialog = OverlayAlertDialog()
                alertDialog.show()
            }
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

        // Search bar //<check Kenzy
        main_SearchBar = findViewById(R.id.main_search_bar)
        var main_search_bar = intent.getStringExtra("SearchBar")
        //check Kenzy>

        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar.setHomeAsUpIndicator(R.drawable.ic_open_drawer)

        ///TEST
        var first = arrayListOf<String>()
        val second = arrayListOf<String>()
        val third = arrayListOf<String>()
        first.add("okk")
        first.add("error")
        first.add("foo")
        second.add("end")
        second.add("bar")
        second.add("error")
        second.add("okk")
        third.add("oddy")
        third.add("choose")
        third.add("ok")
        third.add("error")
//        second.forEach {
//            first.add(it)
//        }

        println(third.intersect(first.intersect(second)))
        ///ENDTEST

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
            }
                else if (id == R.id.nav_chat) {
                    val loginIntent = Intent(this@MainActivity, ChatActivity::class.java)
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
            var contactList: List<Contacts>?

            println("teeeeest = " + main_search_bar)
            if (main_search_bar == null) {
                contactList = main_ContactsDatabase?.contactsDao()?.getAllContacts() //contactList
            } else {
                contactList = main_ContactsDatabase?.contactsDao()?.getContactByName(main_search_bar)
            }

            if (main_GridView != null) {
                val contactAdapter = ContactAdapter(this, contactList)
                main_GridView!!.adapter = contactAdapter

                main_GridView!!.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                    val o = main_GridView!!.getItemAtPosition(position)
                    val contact = o as Contacts

                    val intent = Intent(this@MainActivity, ContactDetailsActivity::class.java)
                    intent.putExtra("ContactFirstName", contact.firstName)
                    intent.putExtra("ContactLastName", contact.lastName)
                    intent.putExtra("ContactPhoneNumber", contact.phoneNumber)
                    intent.putExtra("ContactMail", contact.mail)
                    intent.putExtra("ContactImage", contact.profilePicture)
                    intent.putExtra("ContactId", contact.id)

                    startActivity(intent)
                }

                // Drag n Drop
                main_GridView!!.onItemLongClickListener = AdapterView.OnItemLongClickListener { _, _, _, _ ->
                    false
                }
            }
        }
        main_mDbWorkerThread.postTask(printContacts)

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
            finish()
        }

        //bouton synchronisation des contacts du téléphone
        main_FloatingButtonSync!!.setOnClickListener(View.OnClickListener {

            //création de la pop up de confirmation de synchro
            val builder = AlertDialog.Builder(this)
            builder.setTitle("SYNCHRONISATION DE VOS CONTACTS")
            builder.setMessage("Voulez vous synchroniser les contacts de votre téléphone avec Knoker ?")
            builder.setPositiveButton("OUI") { _, _ ->
                //récupère tout les contacts du téléphone et les stock dans phoneContactsList et supprime les doublons
                val phonecontact = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC")
                val phoneContactsList = arrayListOf<Contacts>()
                while (phonecontact.moveToNext()) {
                    val fullName = phonecontact?.getString(phonecontact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                    val phoneNumber = phonecontact?.getString(phonecontact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    if (phoneContactsList.isEmpty()) {
                        var lastName = ""
                        if (fullName!!.contains(' '))
                            lastName = fullName.substringAfter(' ')
                        val contactData = Contacts(null, fullName.substringBefore(' '), lastName, phoneNumber!!, "", R.drawable.ryan, R.drawable.aquarius)
                        phoneContactsList.add(contactData)
                    } else if (!isDuplicate(fullName!!, phoneContactsList)) {
                        var lastName = ""
                        if (fullName.contains(' '))
                            lastName = fullName.substringAfter(' ')
                        val contactData = Contacts(null, fullName.substringBefore(' '), lastName, phoneNumber!!, "", R.drawable.ryan, R.drawable.aquarius)
                        phoneContactsList.add(contactData)
                    }
                }
                phonecontact?.close()

                //Ajoute tout les contacts dans la base de données en vérifiant si il existe pas avant
                val addAllContacts = Runnable {
                    var isDuplicate = false
                    val allcontacts = main_ContactsDatabase?.contactsDao()?.getAllContacts()
                    phoneContactsList.forEach { phoneContactList ->
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
            }
            builder.setNegativeButton("NON") { _, _ ->
                //retour à la liste de contacts
            }
            val dialog: AlertDialog = builder.create()
            dialog.show()
        })

        val isDelete = intent.getBooleanExtra("isDelete", false)
        if (isDelete) {
            Toast.makeText(this, "Vous venez de supprimer un contact !", Toast.LENGTH_LONG).show()
        }
    }

    //compare le contact données avec tous ceux de la Database
    private fun isDuplicate(contact: String, contactsList: List<Contacts>): Boolean {
        contactsList.forEach {
            if (it.lastName == "" && it.firstName == contact || it.firstName + " " + it.lastName == contact)
                return true
        }
        return false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        println("Start = " + main_filter)
        when (item.itemId) {
            android.R.id.home -> {
                drawerLayout!!.openDrawer(GravityCompat.START)
                return true
            }
            //<check Kenzy
            R.id.nav_search -> {
                main_search_bar_value = main_SearchBar!!.text.toString()
                intent.putExtra("SearchBar", main_search_bar_value)
                println(main_SearchBar!!.text.toString())
                startActivity(intent)
            }
            //check Kenzy>
            R.id.sms_filter -> {
                if (item.isChecked) {
                    item.setChecked(false)
                    main_filter.remove("sms")
                    println("OffIIIII = " + main_filter)
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
        println("End = " + main_filter)
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
        }
    private fun buildNotificationServiceAlertDialog(): AlertDialog {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Knocker")
        alertDialogBuilder.setMessage("vous voulez vous autouriser knocker à acceder a vos notifications")
        alertDialogBuilder.setPositiveButton("yes"
        ) { dialog, id ->
            startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
            val intentFilter = IntentFilter()
            intentFilter.addAction("com.example.testnotifiacation.notificationExemple")
            //registerReceiver(sbr, intentFilter);
            if (isNotificationServiceEnabled) {
            }
        }
        alertDialogBuilder.setNegativeButton("no"
        ) { dialog, id -> }
        return alertDialogBuilder.create()
    }

    private fun OverlayAlertDialog(): AlertDialog {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Knocker")
        alertDialogBuilder.setMessage("vous voulez vous autouriser knocker à afficher des notifications dirrectement sur d'autre application")
        alertDialogBuilder.setPositiveButton("oui"
        ) { dialog, id ->
            val intentPermission = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivity(intentPermission)
        }
        alertDialogBuilder.setNegativeButton("no"
        ) { dialog, id -> }
        return alertDialogBuilder.create()
    }
}
