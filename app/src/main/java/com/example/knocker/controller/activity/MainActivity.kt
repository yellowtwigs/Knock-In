package com.example.knocker.controller.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.ColorStateList
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
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.appcompat.widget.Toolbar
import android.text.TextUtils
import android.text.TextWatcher
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.example.knocker.*
import com.example.knocker.controller.*
import com.example.knocker.model.*
import com.example.knocker.model.ModelDB.ContactWithAllInformation
import java.util.regex.Pattern

/**
 * La Classe qui permet d'afficher la searchbar, les filtres, la gridview, les floatings buttons dans la page des contacts
 * @author Florian Striebel, Kenzy Suon, Ryan Granet
 */
class MainActivity : AppCompatActivity() {


    //region ========================================== Var or Val ==========================================

    // Show on the Main Layout
    private var drawerLayout: DrawerLayout? = null

    private var main_GridView: GridView? = null
    private var main_Listview: ListView? = null

    private var my_knocker: RelativeLayout? = null

    private var main_FloatingButtonOpen: FloatingActionButton? = null
    private var main_FloatingButtonAdd: FloatingActionButton? = null
    private var main_FloatingButtonCompose: FloatingActionButton? = null
    private var main_FloatingButtonSync: FloatingActionButton? = null

    private var main_FloatingButtonOpenAnimation: Animation? = null
    private var main_FloatingButtonCloseAnimation: Animation? = null
    private var main_FloatingButtonClockWiserAnimation: Animation? = null
    private var main_FloatingButtonAntiClockWiserAnimation: Animation? = null

    private var phone_call_ImageView: ImageView? = null
    private var sms_ImageView: ImageView? = null

    private var main_CoordinationLayout: CoordinatorLayout? = null
    internal var main_FloatingButtonIsOpen = false
    internal var main_search_bar_value = ""
    private var main_filter = arrayListOf<String>()
    private var main_SearchBar: EditText? = null
    var scaleGestureDetectore: ScaleGestureDetector? = null

    private var gridViewAdapter: ContactGridViewAdapter?=null

    // Database && Thread
    private var main_ContactsDatabase: ContactsRoomDatabase? = null
    private lateinit var main_mDbWorkerThread: DbWorkerThread

    private var main_BottomNavigationView: BottomNavigationView? = null

    private var gestionnaireContacts: ContactList? = null

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
            R.id.navigation_socials_networks -> {
                startActivity(Intent(this@MainActivity, SocialsNetworksLinksActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
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
     *
     * @param Bundle @type
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT

        if(android.os.Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
            window.navigationBarColor = getResources().getColor(R.color.whiteColor)
        }
        val isDelete = intent.getBooleanExtra("isDelete", false)
        if (isDelete) {
            Toast.makeText(this, "Vous venez de supprimer un contact !", Toast.LENGTH_LONG).show()
        }

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
            edit.apply()
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
        actionbar.setBackgroundDrawable( ColorDrawable(Color.parseColor("#ffffff")))
        //endregion

        //region ======================================= DrawerLayout =======================================

        // Drawerlayout
        drawerLayout = findViewById(R.id.drawer_layout)

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        val headerView = navigationView.getHeaderView(0);
        my_knocker = headerView.findViewById(R.id.my_knocker)

        my_knocker!!.setOnClickListener {
        }
        navigationView.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            drawerLayout!!.closeDrawers()

            val id = menuItem.itemId

            if (id == R.id.nav_informations) {
                startActivity(Intent(this@MainActivity, EditInformationsActivity::class.java))
            } else if (id == R.id.nav_notif_config) {
                startActivity(Intent(this@MainActivity, ManageNotificationActivity::class.java))
            } else if (id == R.id.nav_screen_config) {
                startActivity(Intent(this@MainActivity, ManageMyScreenActivity::class.java))
            } else if (id == R.id.nav_data_access) {
            } else if (id == R.id.nav_knockons) {
                startActivity(Intent(this@MainActivity, ManageKnockonsActivity::class.java))
            } else if (id == R.id.nav_statistics) {
            } else if (id == R.id.nav_help) {
                startActivity(Intent(this@MainActivity, HelpActivity::class.java))
            }

            val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
            drawer.closeDrawer(GravityCompat.START)
            true
        }

        //endregion

        //region ========================================= Runnable =========================================

        //affiche tout les contacts de la Database

        // Grid View
        main_GridView = findViewById(R.id.main_grid_view_id)
        main_Listview = findViewById(R.id.main_list_view_id)
        ////////
        val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
        val len = sharedPreferences.getInt("gridview", 4)
        main_GridView!!.numColumns = len // permet de changer
        gestionnaireContacts = ContactList(this.applicationContext)

        println("contact db")

        if (main_GridView != null) {
            gridViewAdapter = ContactGridViewAdapter(this, gestionnaireContacts!!, len)

            main_GridView!!.adapter = gridViewAdapter
            var index = sharedPreferences.getInt("index", 0)
            val edit: SharedPreferences.Editor = sharedPreferences.edit()
            main_GridView!!.setSelection(index)
            edit.apply()

            main_GridView!!.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                if (!main_FloatingButtonIsOpen) {

                    //Save position in gridview
                    //val state = main_GridView!!.onSaveInstanceState()
                    index = main_GridView!!.getFirstVisiblePosition()
                    println("wheeeeeeee = " + index)

                    //val edit : SharedPreferences.Editor = sharedPreferences.edit()
                    edit.putInt("index", index)
                    edit.commit()
                    //
                    val contact = gestionnaireContacts!!.contacts.get(position)
                    /*                          val mail = contact.contactDetailList!!.get(1).contactDetails
                                              val phone = contact.contactDetailList!!.get(0).contactDetails
                  */
                    println(contact.contactDB!!.id.toString() + "\n contact detail list "
                            + contact.contactDetailList!! + "contact name" + contact.contactDB!!.firstName)

                    //val intent = Intent(this, EditContactActivity::class.java)
                    //intent.putExtra("ContactId", contact.getContactId())
                    // startActivity(intent)


                } else {
                    main_FloatingButtonIsOpen = false
                    onFloatingClickBack()
                }
            }
            main_GridView!!.setOnScrollListener(object : AbsListView.OnScrollListener {
                override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {

                }

                override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
                    if(gridViewAdapter!=null) {
                        if(gridViewAdapter!!.selectMenu!=null) {
                            gridViewAdapter!!.closeMenu()
                        }
                    }
                }
            })

            // Drag n Drop
        }

        if (main_Listview != null) {
            val contactAdapter = ContactListViewAdapter(this, gestionnaireContacts!!.contacts, len)
            main_Listview!!.adapter = contactAdapter
            var index = sharedPreferences.getInt("index", 0)
            println("okkkkkkk = " + index)
            val edit: SharedPreferences.Editor = sharedPreferences.edit()
            main_Listview!!.setSelection(index)
            edit.putInt("index", 0)
            edit.apply()

            var listview = layoutInflater.inflate(R.layout.list_contact_item_layout, null)

            phone_call_ImageView = listview.findViewById(R.id.phone_call_image_view)
            sms_ImageView = listview.findViewById(R.id.sms_image_view)


//                phone_call_ImageView!!.setOnClickListener {
//                    val intent = Intent(this@MainActivity, ComposeMessageActivity::class.java)
//                    intent.putExtra("ContactPhoneNumber", !!.text.toString())
//                    startActivity(intent)
//                }

            main_Listview!!.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                if (!main_FloatingButtonIsOpen) {

                    //Save position in gridview
                    index = main_Listview!!.firstVisiblePosition

                    //val edit : SharedPreferences.Editor = sharedPreferences.edit()
                    edit.putInt("index", index)
                    edit.commit()
                    //
                    val o = main_Listview!!.getItemAtPosition(position)
                    val contact = o as ContactWithAllInformation

                    val intent = Intent(this, EditContactActivity::class.java)
                    intent.putExtra("ContactId", contact.contactDB!!.id)
                    startActivity(intent)
                } else {
                    main_FloatingButtonIsOpen = false
                    onFloatingClickBack()
                }
            }
        }


        //main_mDbWorkerThread.postTask(printContacts)

        //endregion

        //region ==================================== SetOnClickListener ====================================

        main_SearchBar!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
                main_search_bar_value = main_SearchBar!!.text.toString()
                val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
                val len = sharedPreferences.getInt("gridview", 4)
                var filteredList = gestionnaireContacts!!.getContactConcernByFilter(main_filter, main_search_bar_value)
                gestionnaireContacts!!.contacts=filteredList
                gridViewAdapter = ContactGridViewAdapter(this@MainActivity, gestionnaireContacts, len)
                main_GridView!!.adapter = gridViewAdapter
            }
        })

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
            startActivity(Intent(this@MainActivity, AddNewContactActivity::class.java))
            main_FloatingButtonIsOpen = false
        }

        main_FloatingButtonCompose!!.setOnClickListener {
            val loginIntent = Intent(this@MainActivity, ComposeMessageActivity::class.java)
            startActivity(loginIntent)
            main_FloatingButtonIsOpen = false
        }

        //bouton synchronisation des contacts du téléphone
        main_FloatingButtonSync!!.setOnClickListener {
            //récupère tout les contacts du téléphone et les stock dans phoneContactsList et supprime les doublons
            gestionnaireContacts!!.getAllContacsInfoSync(contentResolver, main_GridView, this)//ContactSync.getAllContact(contentResolver)//TODO put this code into ContactList
            val sharedPreferences = applicationContext.getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
            val len = sharedPreferences.getInt("gridview", 4)
          /*  gridViewAdapter = ContactGridViewAdapter(applicationContext, gestionnaireContacts!!, len)
            main_GridView!!.adapter = gridViewAdapter
*/          gridViewAdapter!!.setGestionnairecontact(gestionnaireContacts!!);
            gridViewAdapter!!.notifyDataSetChanged();
            //Ajoute tout les contacts dans la base de données en vérifiant si il existe pas avant

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
        return true
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

    @SuppressLint("ShowToast")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                drawerLayout!!.openDrawer(GravityCompat.START)
                hideKeyboard()
                return true
            }
            R.id.item_click_change_format -> {
                if (main_GridView!!.visibility == View.VISIBLE) {
                    main_Listview!!.visibility = View.VISIBLE
                    main_GridView!!.visibility = View.GONE

                    val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)

                    main_Listview!!.startAnimation(slideUp)

                    Toast.makeText(this, "Mode Liste", Toast.LENGTH_SHORT).show()
                } else {
                    main_Listview!!.visibility = View.GONE
                    main_GridView!!.visibility = View.VISIBLE

                    val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)
                    main_GridView!!.startAnimation(slideUp)

                    Toast.makeText(this, "Mode Grille", Toast.LENGTH_SHORT).show()
                }


                return true
            }
            R.id.sms_filter -> {
                if (item.isChecked) {
                    item.setChecked(false)
                    main_filter.remove("sms")
                    // duplicate
                    main_search_bar_value = main_SearchBar!!.text.toString()

                    val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
                    val len = sharedPreferences.getInt("gridview", 4)
                    val filteredContact = gestionnaireContacts!!.getContactConcernByFilter(main_filter, main_search_bar_value)
                    gestionnaireContacts!!.contacts=filteredContact
                    gridViewAdapter= ContactGridViewAdapter(this@MainActivity, gestionnaireContacts, len)
                    main_GridView!!.adapter = gridViewAdapter
                    //
                } else {
                    item.setChecked(true)
                    main_filter.add("sms")
                    // duplicate
                    main_search_bar_value = main_SearchBar!!.text.toString()

                    val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
                    val len = sharedPreferences.getInt("gridview", 4)
                    val filteredContact = gestionnaireContacts!!.getContactConcernByFilter(main_filter, main_search_bar_value)
                    gestionnaireContacts!!.contacts=filteredContact
                    gridViewAdapter= ContactGridViewAdapter(this@MainActivity, gestionnaireContacts, len)
                    main_GridView!!.adapter = gridViewAdapter
                    //
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
                    gestionnaireContacts!!.contacts=filteredContact
                    gridViewAdapter= ContactGridViewAdapter(this@MainActivity, gestionnaireContacts, len)
                    main_GridView!!.adapter = gridViewAdapter
                    //
                } else {
                    item.setChecked(true)
                    main_filter.add("mail")
                    // duplicate
                    main_search_bar_value = main_SearchBar!!.text.toString()
                    val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
                    val len = sharedPreferences.getInt("gridview", 4)
                    val filteredContact = gestionnaireContacts!!.getContactConcernByFilter(main_filter, main_search_bar_value)
                    gestionnaireContacts!!.contacts=filteredContact
                    gridViewAdapter= ContactGridViewAdapter(this@MainActivity, gestionnaireContacts, len)
                    main_GridView!!.adapter = gridViewAdapter
                    //
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onFloatingClickBack() {
        main_FloatingButtonAdd!!.startAnimation(main_FloatingButtonCloseAnimation)
        main_FloatingButtonCompose!!.startAnimation(main_FloatingButtonCloseAnimation)
        main_FloatingButtonSync!!.startAnimation(main_FloatingButtonCloseAnimation)
        main_FloatingButtonOpen!!.startAnimation(main_FloatingButtonAntiClockWiserAnimation)

        main_FloatingButtonAdd!!.isClickable = false
        main_FloatingButtonCompose!!.isClickable = false
        main_FloatingButtonSync!!.isClickable = false
    }

    private fun onFloatingClick() {
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

    private fun toggleNotificationListenerService() {
        val pm = getPackageManager()
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

    @SuppressLint("ShowToast")
    private fun phoneCall(phoneNumberEntered: String) {
        if (!TextUtils.isEmpty(phoneNumberEntered)) {
            if (isValidPhone(phoneNumberEntered)) {
                val dial = "tel:$phoneNumberEntered"
                startActivity(Intent(Intent.ACTION_DIAL, Uri.parse(dial)))
            } else {
                Toast.makeText(this, "Enter a phone number valid", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Enter a phone number", Toast.LENGTH_SHORT).show()
        }
    }

    fun isValidPhone(phone: String): Boolean {
        val expression = "^(?:(?:\\+|00)33[\\s.-]{0,3}(?:\\(0\\)[\\s.-]{0,3})?|0)[1-9](?:(?:[\\s.-]?\\d{2}){4}|\\d{2}(?:[\\s.-]?\\d{3}){2})\$"
        val pattern = Pattern.compile(expression)
        val matcher = pattern.matcher(phone)
        return matcher.matches()
    }

//    if (main_Listview != null && gestionnaireContacts!!.contacts != null) {
//        val contactAdapter = ContactListViewAdapter(this, gestionnaireContacts!!.contacts, len)
//        main_Listview!!.adapter = contactAdapter
//        var index = sharedPreferences.getInt("index", 0)
//        println("okkkkkkk = " + index)a
//        val edit: SharedPreferences.Editor = sharedPreferences.edit()
//        main_Listview!!.setSelection(index)
//        edit.putInt("index", 0)
//        edit.apply()

    //endregion

}

