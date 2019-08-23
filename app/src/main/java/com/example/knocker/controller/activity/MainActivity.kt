package com.example.knocker.controller.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
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
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.knocker.FirstLaunchActivity
import com.example.knocker.R
import com.example.knocker.controller.ContactGridViewAdapter
import com.example.knocker.controller.ContactRecyclerViewAdapter
import com.example.knocker.controller.NotificationListener
import com.example.knocker.controller.activity.firstLaunch.TutorialActivity
import com.example.knocker.controller.activity.group.GroupManagerActivity
import com.example.knocker.model.ContactManager
import com.example.knocker.model.ContactsRoomDatabase
import com.example.knocker.model.DbWorkerThread
import com.example.knocker.model.ModelDB.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.collections.ArrayList

/**
 * Activite qui permet d'afficher et de gérer la homepage
 * @author Florian Striebel, Kenzy Suon, Ryan Granet
 */
@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class MainActivity : AppCompatActivity(), DrawerLayout.DrawerListener {
    //Dans cette region on crée toutes les variables dont l'activité aura besoin
    //region ========================================== Var or Val ==========================================

    // Show on the Main Layout
    private var drawerLayout: DrawerLayout? = null

    private var main_GridView: GridView? = null
    private var main_RecyclerView: RecyclerView? = null

    private var main_FloatingButtonAdd: FloatingActionButton? = null
    private var main_FloatingButtonSend: FloatingActionButton? = null

    private var main_SMSButton: FloatingActionButton? = null
    private var main_MailButton: FloatingActionButton? = null
    private var main_groupButton: FloatingActionButton? = null

    internal var main_search_bar_value = ""
    private var main_filter = arrayListOf<String>()
    private var main_SearchBar: AppCompatEditText? = null

    private var main_ToolbarLayout: RelativeLayout? = null
    private var main_toolbar_Help: AppCompatImageView? = null

    private var main_ToolbarMultiSelectModeLayout: RelativeLayout? = null
    private var main_ToolbarMultiSelectModeClose: AppCompatImageView? = null
    private var main_ToolbarMultiSelectModeTitle: TextView? = null
    private var main_ToolbarMultiSelectModeDelete: AppCompatImageView? = null
    private var main_ToolbarMultiSelectModeMenu: AppCompatImageView? = null

    // Database && Thread
    private var main_ContactsDatabase: ContactsRoomDatabase? = null
    private lateinit var main_mDbWorkerThread: DbWorkerThread

    private var main_BottomNavigationView: BottomNavigationView? = null

    private var gestionnaireContacts: ContactManager? = null
    private var gridViewAdapter: ContactGridViewAdapter? = null
    private var recyclerViewAdapter: ContactRecyclerViewAdapter? = null
    private var main_layout: LinearLayout? = null
    private var main_loadingPanel: RelativeLayout? = null

    private var listOfItemSelected: ArrayList<ContactWithAllInformation> = ArrayList()

    private var firstClick: Boolean = true
    private var multiChannelMode: Boolean = false

    private val PERMISSION_CALL_RESULT = 1
    private val PERMISSION_READ_CONTACT = 99

    //On créé un listener pour la bottomNavigationBar pour changer d'activité lors d'un click
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_contacts -> {
            }
            R.id.navigation_groups -> {
                startActivity(Intent(this@MainActivity, GroupManagerActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
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
     * Méthode lancé par le système à chaque redémarage de l'activité
     * @param Bundle @type
     */

    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("ClickableViewAccessibility", "InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //region ======================================== Theme Dark ========================================
        //On affecte le Thème light ou le dark en fonction de ce que l'utilisateur à choisi
        //Ce thème est enregistré dans une sharedPreferences c'est un fichier android qui est sauvegardé par l'application
        val sharedThemePreferences = getSharedPreferences("Knocker_Theme", Context.MODE_PRIVATE)
        if (sharedThemePreferences.getBoolean("darkTheme", false)) {
            setTheme(R.style.AppThemeDark)
        } else {
            setTheme(R.style.AppTheme)
        }

        //endregion


        //region =====================================First Launch ==========================================
        //Si c'est la premiere fois que nous ouvrons l'application alors nous sommes envoyer dans les différents écrans d'installations
        val sharedFirstLaunch = getSharedPreferences("FirstLaunch", Context.MODE_PRIVATE)
        if (sharedFirstLaunch.getBoolean("first_launch", true)) {
            startActivity(Intent(this@MainActivity, FirstLaunchActivity::class.java))
            finish()
        }
        //endregion

        setContentView(R.layout.activity_main)//On affecte a notre activity son layout correspondant
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT

        //region ================================ BottomActionBar color light ===============================
        // (Bar android avec les boutons retour, home et application en cours)
        val decorView = window.decorView
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//Feature possible a partir de la version Oreo d'android
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }
        //endregion

        val isDelete = intent.getBooleanExtra("isDelete", false)
        if (isDelete) {
            Toast.makeText(this, R.string.main_toast_delete_contact, Toast.LENGTH_LONG).show()
        }
        //region =====================================Relancement du Service de Notification=========================================
        if (isNotificationServiceEnabled()) {
            toggleNotificationListenerService()
        }
        //endregion

        //region ====================================== Worker Thread =======================================

        // on init WorkerThread
        main_mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        main_mDbWorkerThread.start()

        //on get la base de données
        main_ContactsDatabase = ContactsRoomDatabase.getDatabase(this)

        //endregion

        //region ============================================setFavorite List ================================
        //Si l'activité précédente etait StartActivity alors on regarde dans les feoupes si il y en a un qui se nomme favoris
        //Si oui alors pour tous les contact de ce groupes alors nous les mettons en favoris
        val intent = intent
        var fromStartActivity = intent.getBooleanExtra("fromStartActivity", false)

        if (fromStartActivity) {
            var counter = 0

            while (counter < main_ContactsDatabase!!.GroupsDao().getAllGroupsByNameAZ().size) {
                if (main_ContactsDatabase!!.GroupsDao().getAllGroupsByNameAZ()[counter].groupDB!!.name == "Favorites") {
                    var secondCounter = 0
                    while (secondCounter < main_ContactsDatabase!!.GroupsDao().getAllGroupsByNameAZ()[counter].getListContact(this).size) {
                        main_ContactsDatabase!!.GroupsDao().getAllGroupsByNameAZ()[counter].getListContact(this)[secondCounter].setIsFavorite(main_ContactsDatabase)

                        secondCounter++
                    }
                    break
                }
                counter++
            }

            fromStartActivity = false
        }
        //endregion

        //region ======================================= FindViewById =======================================
        //Pour tous nos attributs qui sont des vues (TextView, listView , ConstraintLayout, ImageView etc) sur lesquelles notre activité agit nous les récupérons
        main_FloatingButtonAdd = findViewById(R.id.main_floating_button_open_id)

        main_FloatingButtonSend = findViewById(R.id.main_floating_button_send_id)

        main_BottomNavigationView = findViewById(R.id.navigation)

        main_BottomNavigationView!!.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        main_SearchBar = findViewById(R.id.main_toolbar_search)
        main_layout = findViewById(R.id.content_frame)
        main_loadingPanel = findViewById(R.id.loadingPanel)

        main_MailButton = findViewById(R.id.main_gmail_button)
        main_SMSButton = findViewById(R.id.main_sms_button)
        main_groupButton = findViewById(R.id.main_group_button)

        main_ToolbarLayout = findViewById(R.id.main_toolbar_layout)

        main_ToolbarMultiSelectModeLayout = findViewById(R.id.main_toolbar_multi_select_mode_layout)
        main_ToolbarMultiSelectModeClose = findViewById(R.id.main_toolbar_multi_select_mode_close)
        main_ToolbarMultiSelectModeTitle = findViewById(R.id.main_toolbar_multi_select_mode_tv)
        main_ToolbarMultiSelectModeDelete = findViewById(R.id.main_toolbar_multi_select_mode_delete)
        main_ToolbarMultiSelectModeMenu = findViewById(R.id.main_toolbar_multi_select_mode_menu)

        //endregion

        //region ========================================== Toolbar =========================================
        //La toolbar est la barre d'action en haut de l'écran elle est ici composé d'une searchbar mais ce n'est pas toujours le cas
        //Dans cette région nous ajoutons à la toolbar tous les menus et affichage qu'elle utiliseras
        main_ToolbarLayout = findViewById(R.id.main_toolbar_layout)
        val main_toolbar_OpenDrawer = findViewById<AppCompatImageView>(R.id.main_toolbar_open_drawer)
        main_toolbar_Help = findViewById(R.id.main_toolbar_help)

        val main_toolbar_Menu = findViewById<Toolbar>(R.id.main_toolbar_menu)
        setSupportActionBar(main_toolbar_Menu)
        main_toolbar_Menu.overflowIcon = getDrawable(R.drawable.ic_toolbar_menu)
        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(false)
        actionbar.title = ""

        //endregion

        //region ======================================= DrawerLayout =======================================
        //On récupère le drawer(menu latéral) pour affecter à chaque option de ce menu une action et modifier ces options d'affichage
        drawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout!!.addDrawerListener(this)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        val menu = navigationView.menu
        val navItem = menu.findItem(R.id.nav_home) //On récupère l'option correspondant à cette activité
        navItem.isChecked = true //Puis nous la mettons en surbrillance par rapport aux autres options
        val navSyncContact = menu.findItem(R.id.nav_sync_contact)
        navSyncContact.isVisible = true //Nous affichons dans cette activité la possibilité de synchroniser nos contacts

        //Lorsque l'utilisateur clique sur un des éléments du drawer nous le fermons puis ouvrons un nouvel activité
        navigationView.setNavigationItemSelectedListener { menuItem ->
         //   menuItem.isChecked = true
            drawerLayout!!.closeDrawers()
            when (menuItem.itemId) {
                R.id.nav_home -> startActivity(Intent(this@MainActivity, MainActivity::class.java))
                R.id.nav_informations -> startActivity(Intent(this@MainActivity, EditInformationsActivity::class.java))
                R.id.nav_messenger -> startActivity(Intent(this@MainActivity, MessengerActivity::class.java))
                R.id.nav_notif_config -> startActivity(Intent(this@MainActivity, ManageNotificationActivity::class.java))
                R.id.nav_settings -> startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                R.id.nav_manage_screen -> startActivity(Intent(this@MainActivity, ManageMyScreenActivity::class.java))
                R.id.nav_knockons -> startActivity(Intent(this@MainActivity, ManageKnockonsActivity::class.java))
                R.id.nav_help -> startActivity(Intent(this@MainActivity, HelpActivity::class.java))
            }

           /* val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
            drawer.closeDrawer(GravityCompat.START)*/
            true
        }

        //endregion

        //region ========================================= Runnable =========================================
        //affiche tout les contactList de la Database dans une RecyclerView ou dans une GridView

        main_GridView = findViewById(R.id.main_grid_view_id)
        main_RecyclerView = findViewById(R.id.main_recycler_view_id)
        val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
        val len = sharedPreferences.getInt("gridview", 4)

        //Vérification du mode d'affichage si inférieur à 1 alors l'affichage est sous forme de liste sinon sous forme de gridView
        if (len <= 1) {
            main_GridView!!.visibility = View.GONE
            main_RecyclerView!!.visibility = View.VISIBLE
        } else {
            main_RecyclerView!!.visibility = View.GONE
            main_GridView!!.visibility = View.VISIBLE
        }

        main_GridView!!.numColumns = len // permet de changer
        gestionnaireContacts = ContactManager(this.applicationContext)
        //region ==================================================== set ListContact ====================================
        //Selon le mode d'affichage set pour la list ou pour la grid les contact triés
        if (main_GridView!!.visibility != View.GONE) {
            when {//Verification du mode de trie des contact pour afficher le bon tri
                sharedPreferences.getString("tri", "nom") == "nom" -> gestionnaireContacts!!.sortContactByFirstNameAZ()
                sharedPreferences.getString("tri", "nom") == "priorite" -> gestionnaireContacts!!.sortContactByPriority()
                else -> gestionnaireContacts!!.sortContactByGroup()
            }
            gridViewAdapter = ContactGridViewAdapter(this, gestionnaireContacts!!, len)

            main_GridView!!.adapter = gridViewAdapter
            val index = sharedPreferences.getInt("index", 0)
            val edit: SharedPreferences.Editor = sharedPreferences.edit()
            main_GridView!!.setSelection(index)
            edit.apply()
            //La gridView va mettre en place un écouteur sur l'action Scroll nous avons alors défini un ensemble d'action à effectuer lorsque la gridView detecte ce scroll
            main_GridView!!.setOnScrollListener(object : AbsListView.OnScrollListener {
                var lastVisiblePos = main_GridView!!.firstVisiblePosition
                /**
                 * Méthode lancer par le listener lors du lancement ou de l'arret d'un scroll
                 * @param view [AbsListView]
                 * @param scrollState [Int]
                 */

                override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
                    if (gridViewAdapter != null) {
                        gridViewAdapter!!.closeMenu()
                    }
                }

                /**
                 * méthode appellé par la gridView lorsque il y a un scroll
                 * Nous affichons et masquons ici le bouton ajout de contact
                 * @param view [AbsListView]
                 * @param firstVisibleItem [Int]
                 * @param visibleItemCount [Int]
                 * @param totalItemCount [Int]
                 */
                override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
                  /* if (gridViewAdapter != null) {
                        gridViewAdapter!!.closeMenu()
                    }*/
                    println("last visible pos" + lastVisiblePos + "first visible item " + firstVisibleItem + " visible item count" + visibleItemCount + " total item count " + totalItemCount)
                    if (lastVisiblePos < firstVisibleItem) {
                        if (main_FloatingButtonAdd!!.visibility == View.VISIBLE) {
                            val disappear = AnimationUtils.loadAnimation(baseContext, R.anim.disappear)
                            main_FloatingButtonAdd!!.startAnimation(disappear)
                            main_FloatingButtonAdd!!.visibility = View.GONE
                        }
                        lastVisiblePos = firstVisibleItem
                    } else if (lastVisiblePos > firstVisibleItem) {
                        if (main_FloatingButtonAdd!!.visibility == View.GONE && !multiChannelMode) {
                            val apparition = AnimationUtils.loadAnimation(baseContext, R.anim.reapparrition)
                            main_FloatingButtonAdd!!.startAnimation(apparition)
                            main_FloatingButtonAdd!!.visibility = View.VISIBLE
                        }
                        lastVisiblePos = firstVisibleItem
                    }
                }
            })
        }

        if (main_RecyclerView!!.visibility != View.GONE) {
            recyclerViewAdapter = ContactRecyclerViewAdapter(this, gestionnaireContacts, len)
            main_RecyclerView!!.adapter = recyclerViewAdapter

            val index = sharedPreferences.getInt("index", 0)
            val edit: SharedPreferences.Editor = sharedPreferences.edit()
            main_RecyclerView!!.scrollToPosition(index)
            edit.putInt("index", 0)
            edit.apply()

            main_RecyclerView!!.layoutManager = LinearLayoutManager(this)
            main_RecyclerView!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    println("dx $dx dy$dy")
                    if (dy > 10) {
                        if (main_FloatingButtonAdd!!.visibility == View.VISIBLE) {
                            val disparition = AnimationUtils.loadAnimation(baseContext, R.anim.disappear)
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

        //endregion
        //endregion

        //zone qui nous permet de mettre en place les actions lors d'interaction de l'utilisateur
        //region ======================================== Listeners =========================================
        //Lors du click sur le bouton hamburger  dans la toolbar nous ouvrons le drawer
        main_toolbar_OpenDrawer.setOnClickListener {
            drawerLayout!!.openDrawer(GravityCompat.START)
            hideKeyboard()
        }

        main_toolbar_Help!!.setOnClickListener {
            val intentToTuto = Intent(this@MainActivity, TutorialActivity::class.java)
            intentToTuto.putExtra("fromMainActivity", true)
            startActivity(intentToTuto)
            finish()
        }

        main_FloatingButtonSend!!.setOnClickListener {
            val intentToMultiChannelActivity = Intent(this@MainActivity, MultiChannelActivity::class.java)
            intentToMultiChannelActivity.putExtra("fromMainToMultiChannel", true)
            val iterator: IntIterator?
            val listOfIdContactSelected: ArrayList<Int> = ArrayList()

            iterator = (0 until listOfItemSelected.size).iterator()

            for (i in iterator) {
                listOfIdContactSelected.add(listOfItemSelected[i].getContactId())
            }
            intentToMultiChannelActivity.putIntegerArrayListExtra("ListContactsSelected", listOfIdContactSelected)

            refreshActivity()
            startActivity(intentToMultiChannelActivity)
            finish()
        }

        main_ToolbarMultiSelectModeClose!!.setOnClickListener {
            listOfItemSelected.clear()
            switchMultiSelectToNormalMode()
            refreshActivity()

            Toast.makeText(this, R.string.main_toast_multi_select_deactived, Toast.LENGTH_SHORT).show()
        }

        main_ToolbarMultiSelectModeClose!!.setOnClickListener {
            listOfItemSelected.clear()
            switchMultiSelectToNormalMode()
            refreshActivity()

            Toast.makeText(this, R.string.main_toast_multi_select_deactived, Toast.LENGTH_SHORT).show()
        }

        main_ToolbarMultiSelectModeMenu!!.setOnClickListener {
            val popupMenu = PopupMenu(this, it)
            popupMenu.inflate(R.menu.toolbar_menu_main_multiselect_mode)

            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_main_toolbar_multiselect_all_select -> {
                    }
                }
                false
            }
            popupMenu.show();

        }

        main_ToolbarMultiSelectModeDelete!!.setOnClickListener {
            var supressWarning = " "
            if (listOfItemSelected.size > 1) {
                supressWarning = " :"
                for (contact in listOfItemSelected) {
                    val contactDb = contact.contactDB
                    supressWarning += "\n-" + contactDb!!.firstName + " " + contactDb.lastName
                }
            } else {
                val contact = listOfItemSelected.get(0).contactDB
                supressWarning += contact!!.firstName + " " + contact.lastName
            }
            MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                    .setTitle("Delete Contact")
                    .setMessage(String.format(resources.getString(R.string.main_delete_contact), supressWarning))
                    .setPositiveButton(R.string.edit_contact_validate) { _, _ ->
                        System.out.println("size of list " + listOfItemSelected.size + "-----")
                        listOfItemSelected.forEach {
                            main_ContactsDatabase!!.contactsDao().deleteContactById(it.contactDB!!.id!!)
                            System.out.println("contact " + it.contactDB!! + " son id " + it.contactDB!!.id!!)
                        }
                        listOfItemSelected.clear()
                        switchMultiSelectToNormalMode()
                        refreshActivity()
                        finish();
                        overridePendingTransition(0, 0);
                        startActivity(getIntent());
                        overridePendingTransition(0, 0);

                    }.setNegativeButton(R.string.delete_contact_from_group_cancel) { _, _ -> }.show()

        }

        //Sync contact
        navSyncContact.setOnMenuItemClickListener {
            fromStartActivity = true

            drawerLayout!!.closeDrawers()
            main_GridView!!.visibility = View.GONE
            main_RecyclerView!!.visibility = View.GONE
            //check les permissions
            val sync = Runnable {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), PERMISSION_READ_CONTACT)
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

                    //pour chaque contact changé on affiche une popup avec un choix "garder la version Android ou Knocker"
                    changedContactList.forEach { changedContact ->
                        MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                                .setTitle(R.string.main_edited_contact)
                                .setMessage(this.resources.getString(R.string.main_content_edited_contact) + " " + changedContact.first.firstName + " " + changedContact.first.lastName + this.resources.getString(R.string.main_content_edited_contact_2))
                                .setPositiveButton(R.string.app_name) { _, _ ->
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
                                        gestionnaireContacts!!.contactList.clear()
                                        val shareP = applicationContext.getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
                                        val executorService: ExecutorService = Executors.newFixedThreadPool(1)
                                        if (shareP.getString("tri", "") == "priorite") {
                                            val callDb = Callable { main_ContactsDatabase!!.contactsDao().sortContactByPriority20() }
                                            val result = executorService.submit(callDb)
                                            gestionnaireContacts!!.contactList.addAll(result.get())
                                        } else {
                                            val callDb = Callable { main_ContactsDatabase!!.contactsDao().sortContactByFirstNameAZ() }
                                            val result = executorService.submit(callDb)
                                            gestionnaireContacts!!.contactList.addAll(result.get())
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

            //fonction appelée à chaque charactère tapé
            override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
                //ferme circular
                if (gridViewAdapter!=null) {
                    gridViewAdapter!!.closeMenu()
                }
                //convertir en string le contenu de la searchbar
                main_search_bar_value = main_SearchBar!!.text.toString()
                //get le type d'affichage selectionné
                val sharedPref = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
                val length = sharedPref.getInt("gridview", 4)
                //on get la list des contactList en appliquant les filtres et la search bar
                val filteredList = gestionnaireContacts!!.getContactConcernByFilter(main_filter, main_search_bar_value)
                // on get la list des contactList en appliquant le tri
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
            iterator = (0 until listOfItemSelected.size).iterator()
            for (i in iterator) {
                listOfPhoneNumberContactSelected.add(listOfItemSelected[i].getFirstPhoneNumber())
            }
            monoChannelSmsClick(listOfPhoneNumberContactSelected)
            refreshActivity()
        }

        main_MailButton!!.setOnClickListener {
            val iterator: IntIterator?
            val listOfMailContactSelected: ArrayList<String> = ArrayList()
            iterator = (0 until listOfItemSelected.size).iterator()

            for (i in iterator) {
                listOfMailContactSelected.add(listOfItemSelected[i].getFirstMail())
            }
            monoChannelMailClick(listOfMailContactSelected)

            if (len >= 3) {
                gridViewAdapter = ContactGridViewAdapter(this@MainActivity, gestionnaireContacts, len)
                main_GridView!!.adapter = gridViewAdapter
                main_FloatingButtonAdd!!.visibility = View.VISIBLE
                main_FloatingButtonSend!!.visibility = View.GONE

                main_MailButton!!.visibility = View.GONE
                main_SMSButton!!.visibility = View.GONE
                main_groupButton!!.visibility = View.GONE
            } else {
                main_RecyclerView!!.adapter = ContactRecyclerViewAdapter(this, gestionnaireContacts, len)
                main_FloatingButtonAdd!!.visibility = View.VISIBLE
                main_FloatingButtonSend!!.visibility = View.GONE

                main_MailButton!!.visibility = View.GONE
                main_SMSButton!!.visibility = View.GONE
                main_groupButton!!.visibility = View.GONE
            }

            refreshActivity()
        }

        main_groupButton!!.setOnClickListener {
            val iterator: IntIterator?
            val listOfContactSelected: ArrayList<ContactWithAllInformation> = ArrayList()

            iterator = (0 until listOfItemSelected.size).iterator()

            for (i in iterator) {
                listOfContactSelected.add(listOfItemSelected[i])
                println("test contact:" + listOfItemSelected[i])
            }
            if (len >= 3) {
                gridViewAdapter = ContactGridViewAdapter(this, gestionnaireContacts, len)
                main_GridView!!.adapter = gridViewAdapter


                main_MailButton!!.visibility = View.GONE
                main_SMSButton!!.visibility = View.GONE
                main_groupButton!!.visibility = View.GONE
                main_FloatingButtonSend!!.visibility = View.GONE
            } else {
                main_RecyclerView!!.adapter = ContactRecyclerViewAdapter(this, gestionnaireContacts, len)


                main_MailButton!!.visibility = View.GONE
                main_SMSButton!!.visibility = View.GONE
                main_groupButton!!.visibility = View.GONE
                main_FloatingButtonSend!!.visibility = View.GONE
            }
            saveGroupMultiSelect(listOfContactSelected, len)
        }
        //endregion
    }

    //region ========================================== Functions ===========================================
    /**
     *  Les affichages du modemultiselect sont enlevé pour remettre un affichage comme à l'ouverture de l'activité
     */
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
        listOfItemSelected.clear()
        switchMultiSelectToNormalMode()
    }

    /**
     * Comme [MainActivity.onCreate] méthode lancé a chaque lancement de l'activité
     * On vérifie qu'elle est le dernier tri choisi par l'utilisateur et on l'affecte
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
        when (sharedPreferences.getString("tri", "favoris")) {
            "nom" -> triNom.isChecked = true
            "priorite" -> triPriority.isChecked = true
            "favoris" -> triFavorite.isChecked = true
            else -> triLastName.isChecked = true
        }
        return true
    }

    /**
     * Vérifie si les checkbox ont été check apres une recherche
     * @param item [MenuItem]
     * @return [Boolean]
     */
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

    /**
     * Méthode qui lors de la sélection d'un item du menu effectue les filtres et trie séléctionner dans le menu
     * @param item [MenuItem]
     * @return [Boolean]
     */
    @SuppressLint("ShowToast")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        gridViewAdapter!!.closeMenu()
        when (item.itemId) {
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
                    //on récup tout les contactList avec les filtres appliqués
                    val filteredContact = gestionnaireContacts!!.getContactConcernByFilter(main_filter, main_search_bar_value)
                    // on regarde le quel tri est activé pour l'appliquer
                    val contactListDb = ContactManager(this)
                    if (sharedPreferences.getString("tri", "nom") == "nom") {
                        contactListDb.sortContactByFirstNameAZ()
                    } else if (sharedPreferences.getString("tri", "nom") == "priorite") {
                        contactListDb.sortContactByPriority()
                    } else {
                        contactListDb.sortContactByGroup()
                    }
                    //on garde uniquement les contact en commun avec les filtres et tris
                    contactListDb.contactList.retainAll(filteredContact)
                    gestionnaireContacts!!.contactList.clear()
                    gestionnaireContacts!!.contactList.addAll(contactListDb.contactList)
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
                    gestionnaireContacts!!.contactList.retainAll(filteredContact)

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
                    val contactListDb = ContactManager(this)
                    if (sharedPreferences.getString("tri", "nom") == "nom") {
                        contactListDb.sortContactByFirstNameAZ()
                    } else if (sharedPreferences.getString("tri", "nom") == "priorite") {
                        contactListDb.sortContactByPriority()
                    } else {
                        contactListDb.sortContactByGroup()
                    }
                    contactListDb.contactList.retainAll(filteredContact)
                    gestionnaireContacts!!.contactList.clear()
                    gestionnaireContacts!!.contactList.addAll(contactListDb.contactList)
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
                    gestionnaireContacts!!.contactList.retainAll(filteredContact)
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
            R.id.tri_par_favoris -> {
                if (!item.isChecked) {
                    main_GridView!!.visibility = View.GONE
                    main_RecyclerView!!.visibility = View.GONE
                    main_loadingPanel!!.visibility = View.VISIBLE
                    val sortByFavorite = Runnable {
                        gestionnaireContacts!!.sortContactByFavorite()
                        val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
                        val len = sharedPreferences.getInt("gridview", 4)
                        val edit: SharedPreferences.Editor = sharedPreferences.edit()
                        edit.putString("tri", "favoris")
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
                    main_mDbWorkerThread.postTask(sortByFavorite)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Retourne si on a l'autorisation d'accès aux notification
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
        pm.setComponentEnabledSetting(cmpName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
        pm.setComponentEnabledSetting(cmpName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
    }

    /**
     *  ferme le keyboard
     */
    private fun hideKeyboard() {
        // Check if no view has focus:
        val view = this.currentFocus

        view?.let { v ->
            val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }
    //region ======================= Drawer Listener ========================
    //Cette region est composé d'un ensemble de méthode qui sont appellé lors d'action sur le drawer

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
        if (gridViewAdapter !=null){
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
        if (gridViewAdapter !=null){
            gridViewAdapter!!.closeMenu()
        }
    }
    //endregion
    /**
     *
     * @param [Int]
     */
    @SuppressLint("SetTextI18n")
    fun longGridItemClick(position: Int) {
        main_FloatingButtonAdd!!.visibility = View.GONE
        main_FloatingButtonSend!!.visibility = View.VISIBLE

        if (listOfItemSelected.contains(gestionnaireContacts!!.contactList[position])) {
            listOfItemSelected.remove(gestionnaireContacts!!.contactList[position])
        } else {
            listOfItemSelected.add(gestionnaireContacts!!.contactList[position])
        }

        verifiedContactsChannel(listOfItemSelected)

        if (gridViewAdapter!!.listOfItemSelected.size == 0) {
            val pos = main_GridView!!.firstVisiblePosition
            val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
            val len = sharedPreferences.getInt("gridview", 4)
            gridViewAdapter = ContactGridViewAdapter(this, gestionnaireContacts, len)
            main_GridView!!.adapter = gridViewAdapter
            main_GridView!!.setSelection(pos)

            Toast.makeText(this, R.string.main_toast_multi_select_deactived, Toast.LENGTH_SHORT).show()

            switchMultiSelectToNormalMode()
            main_groupButton!!.visibility = View.GONE
        } else if (listOfItemSelected.size == 1) {
            firstClick = false
            main_ToolbarMultiSelectModeLayout!!.visibility = View.VISIBLE
            main_ToolbarLayout!!.visibility = View.GONE
        }

        val i = listOfItemSelected.size

        if (listOfItemSelected.size == 1) {
            main_ToolbarMultiSelectModeTitle!!.text = i.toString() + " " + getString(R.string.main_toast_multi_select_mode_selected)
        } else if (listOfItemSelected.size > 1) {
            main_ToolbarMultiSelectModeTitle!!.text = i.toString() + " " + getString(R.string.main_toast_multi_select_mode_selected_more_than_one)
        }
    }

    /**
     * On regarde dans la liste ce qu'ont comme channel tous les contacts pour vérifier si ils ont un mail ou numéro
     * Si un des contact n'a pas un de ses channel le boutton correspondant au channel est masqué
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
        } else {
            println("false mail")
            main_MailButton!!.visibility = View.GONE
        }
        main_groupButton!!.visibility = View.VISIBLE
        val params: ViewGroup.MarginLayoutParams = main_groupButton!!.layoutParams as ViewGroup.MarginLayoutParams
        params.bottomMargin = margin * i
        main_groupButton!!.layoutParams = params

    }

    /**
     *
     * @param position [Int]
     */
    fun longRecyclerItemClick(position: Int) {
        if (listOfItemSelected.contains(gestionnaireContacts!!.contactList[position])) {
            listOfItemSelected.remove(gestionnaireContacts!!.contactList[position])
            verifiedContactsChannel(listOfItemSelected)
        } else {
            listOfItemSelected.add(gestionnaireContacts!!.contactList[position])

            main_FloatingButtonAdd!!.visibility = View.GONE
            main_FloatingButtonSend!!.visibility = View.VISIBLE

            verifiedContactsChannel(listOfItemSelected)
        }

        val i = listOfItemSelected.size

        if (listOfItemSelected.size == 1 && firstClick) {
            Toast.makeText(this, R.string.main_toast_multi_select_actived, Toast.LENGTH_SHORT).show()
            main_FloatingButtonAdd!!.visibility = View.GONE
            main_FloatingButtonSend!!.visibility = View.VISIBLE
            firstClick = false
            multiChannelMode = true
            main_ToolbarMultiSelectModeLayout!!.visibility = View.VISIBLE
            main_ToolbarLayout!!.visibility = View.GONE

        } else if (listOfItemSelected.size == 0) {
            Toast.makeText(this, R.string.main_toast_multi_select_deactived, Toast.LENGTH_SHORT).show()

            main_FloatingButtonAdd!!.visibility = View.VISIBLE
            main_FloatingButtonSend!!.visibility = View.GONE
            main_SMSButton!!.visibility = View.GONE
            main_MailButton!!.visibility = View.GONE
            main_groupButton!!.visibility = View.GONE

            main_ToolbarMultiSelectModeLayout!!.visibility = View.GONE
            main_ToolbarLayout!!.visibility = View.VISIBLE

            firstClick = true
        }

        if (listOfItemSelected.size == 1) {
            main_ToolbarMultiSelectModeTitle!!.text = i.toString() + " " + getString(R.string.main_toast_multi_select_mode_selected)
        } else if (listOfItemSelected.size > 1) {
            main_ToolbarMultiSelectModeTitle!!.text = i.toString() + " " + getString(R.string.main_toast_multi_select_mode_selected_more_than_one)
        }
    }
    /**
     * On ouvre l'application de SMS avec les contact saisit lors du multiselect
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
     * On ouvre l'application de mail choisi par l'utilisateur avec les contact saisit lors du multiselect
     * @param listOfMail [ArrayList<String>]
     */
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

    /**
     * Méthode appellé par le système lorsque l'utilisateur accepte ou refuse une permission
     * Lorsque l'utilisateur autorise de lire ses contacts nous synchronisons ses contacts android
     * Lorsque l'utilisateur autorise d'appeler avec Knocker alors nous Passons l'appel qu'il voulait passer avant d'accepter la permission
     * @param requestCode [Int]
     * @param permissions [Array<String>]
     * @param grantResults [IntArray]
     */
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
            PERMISSION_READ_CONTACT -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                println("permission accept")
                gestionnaireContacts!!.getAllContacsInfoSync(contentResolver)
                recreate()
            }
        }
    }

    /**
     * Nous affichons ici un alertDialog de création du groupe qui nous demande le nom du groupe est sa couleur
     * Une fois validé nous enregistrons le groupe et ses contacts
     * @param listContacts[ArrayList<ContactWithAllInformation>]
     * @param len [Int]
     */
    private fun saveGroupMultiSelect(listContacts: ArrayList<ContactWithAllInformation>, len: Int) {

        val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        @SuppressLint("InflateParams") val alertView = inflater.inflate(R.layout.alert_dialog_edit_group, null, true)

        val edit_group_name_EditText = alertView.findViewById<AppCompatEditText>(R.id.manager_group_edit_group_view_edit)
        val edit_group_name_AlertDialogTitle = alertView.findViewById<TextView>(R.id.manager_group_edit_group_alert_dialog_title)

        val edit_group_name_RedTag = alertView.findViewById<AppCompatImageView>(R.id.manager_group_edit_group_color_red)
        val edit_group_name_BlueTag = alertView.findViewById<AppCompatImageView>(R.id.manager_group_edit_group_color_blue)
        val edit_group_name_GreenTag = alertView.findViewById<AppCompatImageView>(R.id.manager_group_edit_group_color_green)
        val edit_group_name_YellowTag = alertView.findViewById<AppCompatImageView>(R.id.manager_group_edit_group_color_yellow)
        val edit_group_name_OrangeTag = alertView.findViewById<AppCompatImageView>(R.id.manager_group_edit_group_color_orange)
        val edit_group_name_PurpleTag = alertView.findViewById<AppCompatImageView>(R.id.manager_group_edit_group_color_purple)


        edit_group_name_AlertDialogTitle.setText(this.getString(R.string.main_alert_dialog_group_title)
                )

        edit_group_name_EditText.setHint(getString(R.string.group_name).format(main_ContactsDatabase!!.GroupsDao().getIdNeverUsed()))

        edit_group_name_RedTag.setImageResource(R.drawable.border_selected_image_view)
        var color=this.getColor(R.color.red_tag_group)
        edit_group_name_RedTag.setOnClickListener { v1 ->
            edit_group_name_RedTag.setImageResource(R.drawable.border_selected_image_view)
            edit_group_name_BlueTag.setImageResource(android.R.color.transparent)
            edit_group_name_GreenTag.setImageResource(android.R.color.transparent)
            edit_group_name_YellowTag.setImageResource(android.R.color.transparent)
            edit_group_name_OrangeTag.setImageResource(android.R.color.transparent)
            edit_group_name_PurpleTag.setImageResource(android.R.color.transparent)

            color = this.getColor(R.color.red_tag_group)
        }

        edit_group_name_BlueTag.setOnClickListener { v1 ->
            edit_group_name_BlueTag.setImageResource(R.drawable.border_selected_image_view)
            edit_group_name_RedTag.setImageResource(android.R.color.transparent)
            edit_group_name_GreenTag.setImageResource(android.R.color.transparent)
            edit_group_name_YellowTag.setImageResource(android.R.color.transparent)
            edit_group_name_OrangeTag.setImageResource(android.R.color.transparent)
            edit_group_name_PurpleTag.setImageResource(android.R.color.transparent)

            color = this.getColor(R.color.blue_tag_group)
        }

        edit_group_name_GreenTag.setOnClickListener { v1 ->
            edit_group_name_GreenTag.setImageResource(R.drawable.border_selected_image_view)
            edit_group_name_RedTag.setImageResource(android.R.color.transparent)
            edit_group_name_BlueTag.setImageResource(android.R.color.transparent)
            edit_group_name_YellowTag.setImageResource(android.R.color.transparent)
            edit_group_name_OrangeTag.setImageResource(android.R.color.transparent)
            edit_group_name_PurpleTag.setImageResource(android.R.color.transparent)

            color = this.getColor(R.color.green_tag_group)
        }

        edit_group_name_YellowTag.setOnClickListener { v1 ->
            edit_group_name_YellowTag.setImageResource(R.drawable.border_selected_image_view)
            edit_group_name_RedTag.setImageResource(android.R.color.transparent)
            edit_group_name_GreenTag.setImageResource(android.R.color.transparent)
            edit_group_name_BlueTag.setImageResource(android.R.color.transparent)
            edit_group_name_OrangeTag.setImageResource(android.R.color.transparent)
            edit_group_name_PurpleTag.setImageResource(android.R.color.transparent)

            color = this.getColor(R.color.yellow_tag_group)
        }

        edit_group_name_OrangeTag.setOnClickListener { v1 ->
            edit_group_name_OrangeTag.setImageResource(R.drawable.border_selected_image_view)
            edit_group_name_RedTag.setImageResource(android.R.color.transparent)
            edit_group_name_GreenTag.setImageResource(android.R.color.transparent)
            edit_group_name_YellowTag.setImageResource(android.R.color.transparent)
            edit_group_name_BlueTag.setImageResource(android.R.color.transparent)
            edit_group_name_PurpleTag.setImageResource(android.R.color.transparent)

            color = this.getColor(R.color.orange_tag_group)
        }

        edit_group_name_PurpleTag.setOnClickListener { v1 ->
            edit_group_name_PurpleTag.setImageResource(R.drawable.border_selected_image_view)
            edit_group_name_RedTag.setImageResource(android.R.color.transparent)
            edit_group_name_GreenTag.setImageResource(android.R.color.transparent)
            edit_group_name_YellowTag.setImageResource(android.R.color.transparent)
            edit_group_name_OrangeTag.setImageResource(android.R.color.transparent)
            edit_group_name_BlueTag.setImageResource(android.R.color.transparent)

            color = this.getColor(R.color.purple_tag_group)
        }

        MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                .setView(alertView)
                .setPositiveButton(R.string.alert_dialog_validate) { dialog, which ->
                  /*  println("Name : " + Objects.requireNonNull<Editable>(edit_group_name_EditText.text).toString())
                    println("Name : " + contactsDatabase.GroupsDao().getGroup(mSections.get(position).getIdGroup()!!.toInt()).name)
                    println("Color : $color")
*/                  var idGroup:Long=0
                    if (edit_group_name_EditText.text!!.toString() == "Favorites") {
                        Toast.makeText(this, "Vous ne pouvez pas donner favoris comme nom de groupe", Toast.LENGTH_LONG).show()
                    } else {

                        val nameGroup = if (edit_group_name_EditText.text.toString().isNotEmpty()) {
                            edit_group_name_EditText.text.toString()
                        } else {
                            edit_group_name_EditText.hint.toString()
                        }
                        val executorService: ExecutorService = Executors.newFixedThreadPool(1)
                        val callDb = Callable {
                            if (listContacts.size != 0) {
                                val group = GroupDB(null, nameGroup, "", -500138)
                                idGroup = main_ContactsDatabase?.GroupsDao()!!.insert(group)!!
                                for (contact in listContacts) {
                                    val link = LinkContactGroup(idGroup!!.toInt(), contact.getContactId())
                                    main_ContactsDatabase?.LinkContactGroupDao()!!.insert(link)
                                }
                            }
                        }
                        executorService.submit(callDb).get()!!
                        //                                            Toast.makeText(this, "Vous avez modifié le nom de votre groupe", Toast.LENGTH_LONG).show();
                    }

                    if (color == 0) {
                        val r = Random()
                        val n = r.nextInt(7)

                        when (n) {
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

                    main_ContactsDatabase!!.GroupsDao().updateGroupSectionColorById(idGroup.toInt(), color)
                    this.startActivity(Intent(this, GroupManagerActivity::class.java))
                }
                .setNegativeButton(R.string.alert_dialog_cancel) { dialog, which -> }
                .show()
        gridViewAdapter = ContactGridViewAdapter(this, gestionnaireContacts, len)
        main_GridView!!.adapter = gridViewAdapter
        /*main_FloatingButtonSend!!.visibility = View.GONE
          main_SearchBar!!.visibility = View.VISIBLE
          main_MailButton!!.visibility = View.GONE
          main_SMSButton!!.visibility = View.GONE
          main_groupButton!!.visibility = View.GONE*/
        switchMultiSelectToNormalMode()
        listOfItemSelected.clear()
    }

    /**
     *Passer de l'affichage multiselect au mode normal en changeant le laytout
     */
    private fun switchMultiSelectToNormalMode() {
        main_FloatingButtonSend!!.visibility = View.GONE
        main_ToolbarMultiSelectModeLayout!!.visibility = View.GONE
        main_ToolbarLayout!!.visibility = View.VISIBLE
        main_MailButton!!.visibility = View.GONE
        main_SMSButton!!.visibility = View.GONE
        main_groupButton!!.visibility = View.GONE
    }

    //endregion
}