package com.example.knocker.controller.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION_CODES.M
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
import android.util.DisplayMetrics
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.example.knocker.*
import com.example.knocker.controller.*
import com.example.knocker.controller.activity.firstLaunch.FirstLaunchActivity
import com.example.knocker.model.*
import com.example.knocker.model.ModelDB.ContactDB
import com.example.knocker.model.ModelDB.ContactWithAllInformation
import java.util.regex.Pattern

/**
 * La Classe qui permet d'afficher la searchbar, les filtres, la gridview, les floatings buttons dans la page des contacts
 * @author Florian Striebel, Kenzy Suon, Ryan Granet
 */
class MainActivity: AppCompatActivity(),DrawerLayout.DrawerListener{



    //region ========================================== Var or Val ==========================================

    // Show on the Main Layout
    private var drawerLayout: DrawerLayout? = null

    private var main_GridView: GridView? = null
    private var main_Listview: ListView? = null

    private var my_knocker: RelativeLayout? = null

    private var main_FloatingButtonAdd: FloatingActionButton? = null
    private var main_FloatingButtonSync: FloatingActionButton? = null


    private var phone_call_ImageView: ImageView? = null
    private var sms_ImageView: ImageView? = null

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

    private var gestionnaireContacts: ContactList? = null
    private var gridViewAdapter:ContactGridViewAdapter?=null
    private var listViewAdapter:ContactListViewAdapter?=null
    private var main_layout: LinearLayout? = null

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
//            R.id.navigation_socials_networks -> {
//                startActivity(Intent(this@MainActivity, SocialsNetworksLinksActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
//                return@OnNavigationItemSelectedListener true
//            }
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT

        val sharedFirstLaunch = getSharedPreferences("FirstLaunch", Context.MODE_PRIVATE)
        if(sharedFirstLaunch.getBoolean("first_launch",true)){
            startActivity(Intent(this@MainActivity,FirstLaunchActivity::class.java))
            finish()
        }
        if(android.os.Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
           // window.navigationBarColor = getResources().getColor(R.color.whiteColor)
        }
        val isDelete = intent.getBooleanExtra("isDelete", false)
        if (isDelete) {
            Toast.makeText(this, "Vous venez de supprimer un contact !", Toast.LENGTH_LONG).show()
        }

      /*  if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), 1)
        }*/
        if (!isNotificationServiceEnabled()) {
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
        main_FloatingButtonAdd = findViewById(R.id.main_floating_button_open_id)

        main_BottomNavigationView = findViewById(R.id.navigation)

        main_BottomNavigationView!!.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        //main_BottomNavigationView!!.background= this.getDrawable(R.drawable.border_bottom_nav_view)
        // Search bar
        main_SearchBar = findViewById(R.id.main_search_bar)
        main_layout = findViewById(R.id.content_frame)
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
        drawerLayout!!.addDrawerListener(this)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        val headerView = navigationView.getHeaderView(0)
        val menu = navigationView.getMenu()
        val nav_sync_contact = menu.findItem(R.id.nav_sync_contact)
        nav_sync_contact.setVisible(true)

        //Sync contact
        nav_sync_contact.setOnMenuItemClickListener {
            gestionnaireContacts!!.getAllContacsInfoSync(contentResolver)//ContactSync.getAllContact(contentResolver)//TODO put this code into ContactList
            val sharedPreferences = applicationContext.getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
            val len = sharedPreferences.getInt("gridview", 4)
            /*  gridViewAdapter = ContactGridViewAdapter(applicationContext, gestionnaireContacts!!, len)
              main_GridView!!.adapter = gridViewAdapter
  */
            gridViewAdapter!!.setGestionnairecontact(gestionnaireContacts!!)
            gridViewAdapter!!.notifyDataSetChanged()
            drawerLayout!!.closeDrawers()
            true
        }
        main_layout!!.setOnTouchListener( object: View.OnTouchListener {
            override fun onTouch(v:View , event: MotionEvent): Boolean {
                val view = this@MainActivity.currentFocus
                val imm = this@MainActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                if (view != null) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0)
                }
                return true
            }
        })
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
        //region commentaire
////        println("Status bar ???????????? = "+getResources().getDimensionPixelSize(getResources().getIdentifier("status_bar_height", "dimen", "android")))
////        var point: Point = Point(0,0)
////        val decorrView = window.decorView
////        decorrView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
////        println("action bar ???????????? = "+point.toString())
////        val ressulkt = windowManager.defaultDisplay.getRealSize(point)
////        println("action bar ???????????? = "+ point.toString())
////        println("Nav bar ??????????????? = "+resources.getDimensionPixelSize(getResources().getIdentifier("navigation_bar_height", "dimen", "android")))
//
////        val hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK)
////        val hasMenuKey = ViewConfiguration.get(this).hasPermanentMenuKey()
////        println("PRIIIIIIIINTTTTTTTTT = "+hasBackKey+"  "+hasMenuKey)
////        val displayMetrics = DisplayMetrics()
////        windowManager.defaultDisplay.getMetrics(displayMetrics)
////        var height = displayMetrics.heightPixels
////        main_BottomNavigationView!!.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
////        println("HEIGHT !!!! = "+main_BottomNavigationView!!.getMeasuredHeight()+" "+main_BottomNavigationView!!.width+ " TOTAL HEIGHT = "+height)
        //endregion

        main_GridView = findViewById(R.id.main_grid_view_id)
        //region commentaire
//        val gridParams = main_GridView!!.layoutParams
//        if (!hasMenuKey && !hasBackKey) {
//            gridParams.height = height - main_BottomNavigationView!!.getMeasuredHeight() - resources.getDimensionPixelSize(getResources().getIdentifier("navigation_bar_height", "dimen", "android")) - getResources().getDimensionPixelSize(getResources().getIdentifier("status_bar_height", "dimen", "android"))
//        } else {
 //           gridParams.height = height - main_BottomNavigationView!!.getMeasuredHeight() - getResources().getDimensionPixelSize(getResources().getIdentifier("status_bar_height", "dimen", "android"))
//        }
//        main_GridView!!.layoutParams = gridParams
       // main_GridView.height
        //endregion

        main_Listview = findViewById(R.id.main_list_view_id)

        //region commentaire
//        val listParams = main_GridView!!.layoutParams
//        if (!hasMenuKey && !hasBackKey) {
//            listParams.height = height - main_BottomNavigationView!!.getMeasuredHeight() - resources.getDimensionPixelSize(getResources().getIdentifier("navigation_bar_height", "dimen", "android")) - getResources().getDimensionPixelSize(getResources().getIdentifier("status_bar_height", "dimen", "android"))
//        } else {
//            listParams.height = height - main_BottomNavigationView!!.getMeasuredHeight() - getResources().getDimensionPixelSize(getResources().getIdentifier("status_bar_height", "dimen", "android"))
//        }
//        main_Listview!!.layoutParams = listParams
        //endrgion
        val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
        val len = sharedPreferences.getInt("gridview", 4)
        if (len == 1) {
            main_GridView!!.visibility = View.GONE
            main_Listview!!.visibility = View.VISIBLE
        } else {
            main_Listview!!.visibility = View.GONE
            main_GridView!!.visibility = View.VISIBLE
        }
        main_GridView!!.numColumns = len // permet de changer
        gestionnaireContacts = ContactList(this.applicationContext)

        if (main_GridView != null) {
            if(sharedPreferences.getString("tri","nom").equals("nom")){
                gestionnaireContacts!!.sortContactByFirstNameAZ()
            }else{
                gestionnaireContacts!!.sortContactByPriority()
            }

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
                    //val edit : SharedPreferences.Editor = sharedPreferences.edit()
                    edit.putInt("index", index)
                    edit.commit()
                    //
                    val contact = gestionnaireContacts!!.contacts.get(position)
                    /*                          val mail = contact.contactDetailList!!.get(1).contactDetails
                                              val phone = contact.contactDetailList!!.get(0).contactDetails
                  */
                    //val intent = Intent(this, EditContactActivity::class.java)
                    //intent.putExtra("ContactId", contact.getContactId())
                    // startActivity(intent)


                } else {
                    main_FloatingButtonIsOpen = false
                }
            }
            main_GridView!!.setOnScrollListener(object : AbsListView.OnScrollListener {
                override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {

                }

                override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
                    if(gridViewAdapter!=null) {
                            gridViewAdapter!!.closeMenu()
                    }
                }
            })

            // Drag n Drop
        }

        if (main_Listview != null) {
            listViewAdapter = ContactListViewAdapter(this, gestionnaireContacts!!.contacts, len)
            main_Listview!!.adapter = listViewAdapter
            var index = sharedPreferences.getInt("index", 0)
            //println("okkkkkkk = " + index+"    +"+len)
            val edit: SharedPreferences.Editor = sharedPreferences.edit()
            main_Listview!!.setSelection(index)
            edit.putInt("index", 0)
            edit.apply()

            var listview = layoutInflater.inflate(R.layout.list_contact_item_layout, null)




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
                if(len>1){
                    gridViewAdapter = ContactGridViewAdapter(this@MainActivity, gestionnaireContacts, len)
                    main_GridView!!.adapter = gridViewAdapter
                }else{
                    listViewAdapter=ContactListViewAdapter(this@MainActivity,gestionnaireContacts!!.contacts,len)
                    main_Listview!!.adapter=listViewAdapter
                    listViewAdapter!!.notifyDataSetChanged()
                }
            }
        })

        main_FloatingButtonAdd!!.setOnClickListener {
            startActivity(Intent(this@MainActivity, AddNewContactActivity::class.java))
            main_FloatingButtonIsOpen = false
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
        if(tri.equals("nom")){
            triNom.setChecked(true)
        }else{
            triPrio.setChecked(true)
        }
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
            R.id.item_help -> {
                val alertDialogBuilder = android.app.AlertDialog.Builder(this)
                alertDialogBuilder.setMessage(this.resources.getString(R.string.help_homepage))
                alertDialogBuilder.show()
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
                    if (len>1){
                        gridViewAdapter= ContactGridViewAdapter(this@MainActivity, gestionnaireContacts, len)
                        main_GridView!!.adapter = gridViewAdapter
                    }else{
                        listViewAdapter= ContactListViewAdapter(this@MainActivity,gestionnaireContacts!!.contacts,len)
                        main_Listview!!.adapter=listViewAdapter
                    }
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
                    if (len>1){
                        gridViewAdapter= ContactGridViewAdapter(this@MainActivity, gestionnaireContacts, len)
                        main_GridView!!.adapter = gridViewAdapter
                    }else{
                        listViewAdapter=ContactListViewAdapter(this@MainActivity,gestionnaireContacts!!.contacts,len)
                        main_Listview!!.adapter=listViewAdapter
                        listViewAdapter!!.notifyDataSetChanged()
                    }
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
                    if (len>1){
                        gridViewAdapter= ContactGridViewAdapter(this@MainActivity, gestionnaireContacts, len)
                        main_GridView!!.adapter = gridViewAdapter
                    }else{
                        listViewAdapter=ContactListViewAdapter(this@MainActivity,gestionnaireContacts!!.contacts,len)
                        main_Listview!!.adapter=listViewAdapter
                        listViewAdapter!!.notifyDataSetChanged()
                    }
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
                    if (len>1){
                        gridViewAdapter= ContactGridViewAdapter(this@MainActivity, gestionnaireContacts, len)
                        main_GridView!!.adapter = gridViewAdapter
                    }else{
                        listViewAdapter=ContactListViewAdapter(this@MainActivity,gestionnaireContacts!!.contacts,len)
                        main_Listview!!.adapter=listViewAdapter

                        listViewAdapter!!.notifyDataSetChanged()
                    }
                    //
                }
                return true
            }
            R.id.tri_par_nom ->{
                if(!item.isChecked){
                    item.setChecked(true);
                    gestionnaireContacts!!.sortContactByFirstNameAZ()
                    val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
                    val len = sharedPreferences.getInt("gridview", 4)
                    if (len>1){
                        gridViewAdapter= ContactGridViewAdapter(this@MainActivity, gestionnaireContacts, len)
                        main_GridView!!.adapter = gridViewAdapter
                    }else{
                        listViewAdapter=ContactListViewAdapter(this@MainActivity,gestionnaireContacts!!.contacts,len)
                        main_Listview!!.adapter=listViewAdapter
                        listViewAdapter!!.notifyDataSetChanged()
                    }

                    val edit:SharedPreferences.Editor=sharedPreferences.edit()
                    edit.putString("tri","nom")
                    edit.commit()
                }
            }
            R.id.tri_par_priorite ->{
                if(!item.isChecked){
                    item.setChecked(true);
                    gestionnaireContacts!!.sortContactByPriority()
                    val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
                    val len = sharedPreferences.getInt("gridview", 4)
                    if (len>1){
                        gridViewAdapter= ContactGridViewAdapter(this@MainActivity, gestionnaireContacts, len)
                        main_GridView!!.adapter = gridViewAdapter
                    }else{
                        listViewAdapter=ContactListViewAdapter(this@MainActivity,gestionnaireContacts!!.contacts,len)
                        main_Listview!!.adapter=listViewAdapter
                        listViewAdapter!!.notifyDataSetChanged()
                    }

                    val edit:SharedPreferences.Editor=sharedPreferences.edit()
                    edit.putString("tri","priorite")
                    edit.commit()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun isNotificationServiceEnabled():Boolean {
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
                Toast.makeText(this, "Entrer un numéro valide", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Entrer un numéro de téléphone", Toast.LENGTH_SHORT).show()
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

}

