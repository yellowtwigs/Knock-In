package com.yellowtwigs.knockin.controller.activity

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
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.controller.ContactGridViewAdapter
import com.yellowtwigs.knockin.controller.ContactRecyclerViewAdapter
import com.yellowtwigs.knockin.controller.NotificationListener
import com.yellowtwigs.knockin.controller.activity.firstLaunch.TutorialActivity
import com.yellowtwigs.knockin.controller.activity.group.GroupManagerActivity
import com.yellowtwigs.knockin.model.ContactManager
import com.yellowtwigs.knockin.model.ContactsRoomDatabase
import com.yellowtwigs.knockin.model.DbWorkerThread
import com.yellowtwigs.knockin.model.ModelDB.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.yellowtwigs.knockin.FirstLaunchActivity
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

    /**
     * Dans cette region on crée toutes les variables dont l'activité aura besoin
     * Val pour les valeurs constantes
     * Var pour les valeurs qui seront modifiés
     */
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
    private var main_LinearLayout: LinearLayout? = null
    private var main_loadingPanel: RelativeLayout? = null

    private var listOfItemSelected: ArrayList<ContactWithAllInformation> = ArrayList()

    private var firstClick: Boolean = true
    private var multiChannelMode: Boolean = false

    private val PERMISSION_CALL_RESULT = 1
    private val PERMISSION_READ_CONTACT = 99

    private var idGroup: Long = 0

    //On crée un listener pour la bottomNavigationBar pour changer d'activité lors d'un click
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
            R.id.navigation_cockpit -> {
                startActivity(Intent(this@MainActivity, CockpitActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
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

        //region ======================================= First Launch =======================================

        //Si c'est la premiere fois que nous ouvrons l'application, nous sommes redirigés vers les écrans d'installations
        val sharedFirstLaunch = getSharedPreferences("FirstLaunch", Context.MODE_PRIVATE)
        if (sharedFirstLaunch.getBoolean("first_launch", true)) {
            startActivity(Intent(this@MainActivity, FirstLaunchActivity::class.java))
            finish()
        }

        val importWhatsappSharedPreferences: SharedPreferences = getSharedPreferences("importWhatsappPreferences", Context.MODE_PRIVATE)
        val importWhatsapp = importWhatsappSharedPreferences.getBoolean("importWhatsappPreferences", false)

        //endregion

        //region ======================================== Theme Dark ========================================

        //On affecte le Thème light ou le dark en fonction de ce que l'utilisateur à choisi
        //Ce thème est enregistré dans une sharedPreferences c'est un fichier android qui est sauvegardé par l'application
        val sharedThemePreferences = getSharedPreferences("Knockin_Theme", Context.MODE_PRIVATE)
        if (sharedThemePreferences.getBoolean("darkTheme", false)) {
            setTheme(R.style.AppThemeDark)
        } else {
            setTheme(R.style.AppTheme)
        }

        //endregion

        //On affecte à notre activity son layout correspondant
        setContentView(R.layout.activity_main)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT

        // Si un contact est supprimé, un message s'affichera
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

        //region ====================================== Worker Thread =======================================

        // on init WorkerThread
        main_mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        main_mDbWorkerThread.start()

        //on get la base de données
        main_ContactsDatabase = ContactsRoomDatabase.getDatabase(this)

        //endregion

        //region ===================================== SetFavoriteList ======================================

        //Si l'activité précédente était StartActivity alors on regarde dans les groupes du carnet Android de l'utilisateur
        // S'il y en a un qui se nomme favoris ou favorites alors tous les contacts de ce groupe seront
        // considérés comme des favoris et seront placés dans le groupe Favoris de Knockin

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

            if (importWhatsapp) {
                importWhatsappContacts(main_ContactsDatabase!!.contactsDao().getAllContacts())
            }
        }

        //endregion

        //region ======================================= FindViewById =======================================

        //Pour tous nos attributs qui sont des vues (TextView, listView , ConstraintLayout, ImageView etc) sur lesquelles notre activité agit nous les récupérons

        main_FloatingButtonAddNewContact = this.findViewById(R.id.main_floating_button_add_new_contact)
        main_FloatingButtonMultiChannel = findViewById(R.id.main_floating_button_multichannel)
        main_FloatingButtonMail = findViewById(R.id.main_gmail_button)
        main_FloatingButtonSMS = findViewById(R.id.main_sms_button)
        main_FloatingButtonGroup = findViewById(R.id.main_group_button)

        bottomNavigationView = findViewById(R.id.navigation)
        bottomNavigationView!!.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        main_SearchBar = findViewById(R.id.main_toolbar_search)
        main_LinearLayout = findViewById(R.id.main_layout)
        main_loadingPanel = findViewById(R.id.main_loadingPanel)

        main_GridView = findViewById(R.id.main_grid_view_id)
        main_RecyclerView = findViewById(R.id.main_recycler_view_id)

        //endregion

        //region ========================================== Toolbar =========================================

        // La toolbar de base est la barre d'action en haut de l'écran, elle est composée d'une searchbar et de filtres
        // Lorsque l'utilisateur passe en mode Multiselect, une autre Toolbar apparait avec une poubelle pour supprimer les contacts
        // Et une croix permettant d'annuler le mode Multiselect

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
        actionbar.title = ""

        //endregion

        //region ======================================= DrawerLayout =======================================

        //On récupère le drawer(menu latéral) pour affecter à chaque option de ce menu une action et modifier ses options d'affichage
        mainDrawerLayout = findViewById(R.id.drawer_layout)
        mainDrawerLayout!!.addDrawerListener(this)

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        val menu = navigationView.menu

        //On récupère l'item correspondant à l'activité Home-Contacts
        val navItem = menu.findItem(R.id.nav_home)
        //Puis nous la mettons en surbrillance par rapport aux autres options
        navItem.isChecked = true

        //Nous affichons dans cette activité la possibilité de synchroniser nos contacts
        val navSyncContact = menu.findItem(R.id.nav_sync_contact)
        navSyncContact.isVisible = true

        //Lorsque l'utilisateur clique sur un des éléments du drawer nous le fermons puis ouvrons une nouvelle activité
        navigationView.setNavigationItemSelectedListener { menuItem ->
            //   menuItem.isChecked = true
            mainDrawerLayout!!.closeDrawers()
            when (menuItem.itemId) {
                R.id.nav_home -> startActivity(Intent(this@MainActivity, MainActivity::class.java))
                R.id.nav_informations -> startActivity(Intent(this@MainActivity, EditInformationsActivity::class.java))
                R.id.nav_messenger -> startActivity(Intent(this@MainActivity, MessengerActivity::class.java))
                R.id.nav_notif_config -> startActivity(Intent(this@MainActivity, ManageNotificationActivity::class.java))
                R.id.nav_settings -> startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                R.id.nav_in_app -> startActivity(Intent(this@MainActivity, PremiumActivity::class.java))
                R.id.nav_manage_screen -> startActivity(Intent(this@MainActivity, ManageMyScreenActivity::class.java))
                R.id.nav_knockons -> startActivity(Intent(this@MainActivity, ManageKnockonsActivity::class.java))
                R.id.nav_help -> startActivity(Intent(this@MainActivity, HelpActivity::class.java))
            }

            true
        }

        //endregion

        //region ========================================= Runnable =========================================

        //affiche tous les contactList de la Database dans une RecyclerView ou dans une GridView

        val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
        val len = sharedPreferences.getInt("gridview", 4)

        //Vérification du mode d'affichage si c'est 1 ou inférieur alors l'affichage est sous forme de liste
        // sinon il sera sous forme de gridView

        if (len <= 1) {
            main_GridView!!.visibility = View.GONE
            main_RecyclerView!!.visibility = View.VISIBLE
        } else {
            main_GridView!!.visibility = View.VISIBLE
            main_RecyclerView!!.visibility = View.GONE
        }

//        main_GridView!!.numColumns = len // permet de changer

        if (position != 0) {
            main_GridView!!.smoothScrollToPosition(position)
        }

        gestionnaireContacts = ContactManager(this.applicationContext)

        //region ===================================== set ListContact ======================================

        when {//Verification du mode de tri des contacts pour afficher le bon tri
            sharedPreferences.getString("tri", "nom") == "lastname" -> gestionnaireContacts!!.sortContactByLastname()
            sharedPreferences.getString("tri", "nom") == "nom" -> gestionnaireContacts!!.sortContactByFirstNameAZ()
            sharedPreferences.getString("tri", "nom") == "priorite" -> gestionnaireContacts!!.sortContactByPriority()
            sharedPreferences.getString("tri", "nom") == "favoris" -> gestionnaireContacts!!.sortContactByFavorite()
            else -> gestionnaireContacts!!.sortContactByFavorite()
        }
        //Selon le mode d'affichage set pour la list ou pour la grid les contacts triés
        if (main_GridView!!.visibility != View.GONE) {

            gridViewAdapter = ContactGridViewAdapter(this, gestionnaireContacts!!, len)
            main_GridView!!.adapter = gridViewAdapter
            main_GridView!!.layoutManager = GridLayoutManager(this, len)
            main_GridView!!.recycledViewPool.setMaxRecycledViews(0, 0)

            val index = sharedPreferences.getInt("index", 0)
            val edit: SharedPreferences.Editor = sharedPreferences.edit()
//            main_GridView!!.setSelection(index)
            edit.apply()

            // La gridView va mettre en place un écouteur sur l'action Scroll,
            // nous avons alors défini un ensemble d'action à effectuer lorsque la gridView détecte ce scroll
            main_GridView!!.setOnScrollChangeListener { _, _, _, _, _ ->
                if (gridViewAdapter != null) {
                    gridViewAdapter!!.closeMenu()
                }
            }

//            main_GridView!!.setOnScrollListener(object : AbsListView.OnScrollListener {
//                var lastVisiblePos = main_GridView!!.firstVisiblePosition
//                /**
//                 * Méthode lancer par le listener lors du lancement ou de l'arret d'un scroll
//                 * @param view [AbsListView]
//                 * @param scrollState [Int]
//                 */
//
//                override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
//                    if (gridViewAdapter != null) {
//                        gridViewAdapter!!.closeMenu()
//                    }
//                }
//
//                /**
//                 * Méthode appelée par la gridView lorsque il y a un scroll
//                 * Nous affichons et masquons ici le bouton ajout de contact
//                 * @param view [AbsListView]
//                 * @param firstVisibleItem [Int]
//                 * @param visibleItemCount [Int]
//                 * @param totalItemCount [Int]
//                 */
//                override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
//                    /* if (gridViewAdapter != null) {
//                          gridViewAdapter!!.closeMenu()
//                      }*/
//                    println("last visible pos" + lastVisiblePos + "first visible item " + firstVisibleItem + " visible item count" + visibleItemCount + " total item count " + totalItemCount)
//                    if (lastVisiblePos < firstVisibleItem) {
//                        if (main_FloatingButtonAddNewContact!!.visibility == View.VISIBLE) {
//                            val disappear = AnimationUtils.loadAnimation(baseContext, R.anim.disappear)
//                            main_FloatingButtonAddNewContact!!.startAnimation(disappear)
//                            main_FloatingButtonAddNewContact!!.visibility = View.GONE
//                        }
//                        lastVisiblePos = firstVisibleItem
//                    } else if (lastVisiblePos > firstVisibleItem) {
//                        if (main_FloatingButtonAddNewContact!!.visibility == View.GONE && !multiChannelMode) {
//                            val apparition = AnimationUtils.loadAnimation(baseContext, R.anim.reapparrition)
//                            main_FloatingButtonAddNewContact!!.startAnimation(apparition)
//                            main_FloatingButtonAddNewContact!!.visibility = View.VISIBLE
//                        }
//                        lastVisiblePos = firstVisibleItem
//                    }
//                }
//            })
        }

        if (main_RecyclerView!!.visibility != View.GONE) {

            recyclerViewAdapter = ContactRecyclerViewAdapter(this, gestionnaireContacts, len)
            main_RecyclerView!!.adapter = recyclerViewAdapter
            main_RecyclerView!!.layoutManager = LinearLayoutManager(this)
            main_RecyclerView!!.recycledViewPool.setMaxRecycledViews(0, 0)

            if (position == 0) {
                val index = sharedPreferences.getInt("index", 0)
                val edit: SharedPreferences.Editor = sharedPreferences.edit()
                main_RecyclerView!!.scrollToPosition(index)
                edit.putInt("index", 0)
                edit.apply()
            } else {
                main_RecyclerView!!.layoutManager!!.scrollToPosition(position)
            }

            main_RecyclerView!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    println("dx $dx dy$dy")
                    if (dy > 10) {
                        if (main_FloatingButtonAddNewContact!!.visibility == View.VISIBLE) {
                            val disparition = AnimationUtils.loadAnimation(baseContext, R.anim.disappear)
                            main_FloatingButtonAddNewContact!!.startAnimation(disparition)
                            main_FloatingButtonAddNewContact!!.visibility = View.GONE
                        }
                    } else if (dy < -10) {
                        if (main_FloatingButtonAddNewContact!!.visibility == View.GONE) {
                            val apparition = AnimationUtils.loadAnimation(baseContext, R.anim.reapparrition)
                            main_FloatingButtonAddNewContact!!.startAnimation(apparition)
                            main_FloatingButtonAddNewContact!!.visibility = View.VISIBLE
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

        //Sync contact
        navSyncContact.setOnMenuItemClickListener {
            fromStartActivity = true

            mainDrawerLayout!!.closeDrawers()
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
                        stringSet = sharedPreferencesSync.getStringSet(index.toString(), null)!!.sorted()
                    val changedContactList = arrayListOf<Pair<ContactDB, List<ContactDetailDB>>>()
                    while (sharedPreferencesSync.getStringSet(index.toString(), null) != null && stringSet.isNotEmpty()) {
                        stringSet = sharedPreferencesSync.getStringSet(index.toString(), null)!!.sorted()
                        changedContactList.add(gestionnaireContacts!!.setToContactList(stringSet))
                        index++
                    }

                    //pour chaque contact changé on affiche une popup avec un choix "garder la version Android ou Knockin"
                    changedContactList.forEach { changedContact ->
                        MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                                .setTitle(R.string.main_edited_contact)
                                .setMessage(this.resources.getString(R.string.main_content_edited_contact) + " " + changedContact.first.firstName + " " + changedContact.first.lastName + " " + this.resources.getString(R.string.main_content_edited_contact_2))
                                .setPositiveButton(R.string.app_name) { _, _ ->
                                    // on garde la version Knockin
                                }
                                .setNegativeButton(R.string.main_android_edited_contact) { _, _ ->

                                    val allId = gestionnaireContacts!!.sliceLastSync(sharedPreferences.getString("last_sync_2", "")!!)
                                    //on get les idAndroid et idKnockin du contact modifier
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

                                        if (gridViewAdapter != null) {
                                            gridViewAdapter!!.setGestionnaireContact(gestionnaireContacts!!)
                                            gridViewAdapter!!.notifyDataSetChanged()
                                        }

                                        if (recyclerViewAdapter != null) {
                                            recyclerViewAdapter!!.setGestionnaireContact(gestionnaireContacts!!)
                                            recyclerViewAdapter!!.notifyDataSetChanged()
                                        }
                                    }
                                    runOnUiThread(displaySync)
                                }
                                .show()
                    }

                    index = 1
                    val edit: SharedPreferences.Editor = sharedPreferencesSync.edit()
                    while (sharedPreferencesSync.getStringSet(index.toString(), null) != null && stringSet.isNotEmpty()) {
                        stringSet = sharedPreferencesSync.getStringSet(index.toString(), null)!!.sorted()
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
                            gridViewAdapter!!.setGestionnaireContact(gestionnaireContacts!!)
                            gridViewAdapter!!.notifyDataSetChanged()
                        } else {
                            main_RecyclerView!!.visibility = View.VISIBLE
                            recyclerViewAdapter!!.setGestionnaireContact(gestionnaireContacts!!)
                            recyclerViewAdapter!!.notifyDataSetChanged()
                        }

                        refreshActivity()
                        mainDrawerLayout!!.closeDrawers()
                    }
                    runOnUiThread(displaySync)
                }
            }
            main_mDbWorkerThread.postTask(sync)

            importWhatsappContacts(main_ContactsDatabase!!.contactsDao().getAllContacts())

            true
        }

        main_LinearLayout!!.setOnTouchListener { _, _ ->
            val v = this@MainActivity.currentFocus
            val imm = this@MainActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (v != null) {
                imm.hideSoftInputFromWindow(v.windowToken, 0)
            }
            true
        }

        // Lors du click sur le bouton hamburger  dans la toolbar, nous ouvrons le drawer layout
        main_toolbar_OpenDrawer!!.setOnClickListener {
            if (gridViewAdapter != null) {
                gridViewAdapter!!.closeMenu()
            }
            mainDrawerLayout!!.openDrawer(GravityCompat.START)
            hideKeyboard()
        }

        // Lors du click sur le bouton Help dans la toolbar, nous ouvrons le Tutorial
        main_toolbar_Help!!.setOnClickListener {
            startActivity(Intent(this@MainActivity, TutorialActivity::class.java).putExtra("fromMainActivity", true))
            finish()
        }

        // En mode Multiselect, le click sur la croix permet de fermer de ce mode
        main_ToolbarMultiSelectModeClose!!.setOnClickListener {
            Toast.makeText(this, R.string.main_toast_multi_select_deactived, Toast.LENGTH_SHORT).show()
            startActivity(Intent(this@MainActivity, MainActivity::class.java))
            finish()
        }

        // En mode Multiselect, le click sur le menu permettant de faire un Select All
        main_ToolbarMultiSelectModeMenu!!.setOnClickListener {
            val popupMenu = PopupMenu(this, it)
            popupMenu.inflate(R.menu.toolbar_menu_main_multiselect_mode)

            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_main_toolbar_multiselect_all_select -> {
                        listOfItemSelected.clear()
                        listOfItemSelected.addAll(gestionnaireContacts!!.contactList)
                        if (len > 1) {
                            gridViewAdapter!!.setListOfItemSelected(listOfItemSelected)
                            gridViewAdapter!!.notifyDataSetChanged()
                        } else {
                            recyclerViewAdapter!!.notifyDataSetChanged()
                            recyclerViewAdapter!!.setListOfItemSelected(listOfItemSelected)
                        }
                        main_ToolbarMultiSelectModeTitle!!.text = listOfItemSelected.size.toString() + " " + getString(R.string.main_toast_multi_select_mode_selected_more_than_one)

                    }
                }
                false
            }
            popupMenu.show();

        }

        // En mode Multiselect, le click sur la poubelle permettant de faire un Delete des contacts séléctionnés
        main_ToolbarMultiSelectModeDelete!!.setOnClickListener {
            var suppressWarning = " "
            var nbVIP = 0

            val sharedNumberOfContactsVIPPreferences: SharedPreferences = getSharedPreferences("nb_Contacts_VIP", Context.MODE_PRIVATE)
            val nb_Contacts_VIP = sharedNumberOfContactsVIPPreferences.getInt("nb_Contacts_VIP", 0)

            if (listOfItemSelected.size > 1) {
                suppressWarning = " :"
                for (contact in listOfItemSelected) {
                    val contactDb = contact.contactDB
                    suppressWarning += "\n- " + contactDb!!.firstName + " " + contactDb.lastName

                    if(contactDb.contactPriority == 2){
                        nbVIP++
                    }
                }
            } else {
                val contact = listOfItemSelected[0].contactDB
                suppressWarning += contact!!.firstName + " " + contact.lastName
            }
            MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                    .setTitle(getString(R.string.main_alert_dialog_delete_contact_title))
                    .setMessage(String.format(resources.getString(R.string.main_alert_dialog_delete_contact_message), suppressWarning))
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

        //création du listener de la searchbar
        main_SearchBar!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            //fonction appelée à chaque charactère tapé
            override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
                //ferme circular
                if (gridViewAdapter != null) {
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

        // Lors du click sur le Floating Button +, nous redirige vers la page d'Ajout d'un nouveau contact
        main_FloatingButtonAddNewContact!!.setOnClickListener {
            startActivity(Intent(this@MainActivity, AddNewContactActivity::class.java))
        }

        // En mode Multiselect, lors du click sur le Floating Button -> -> ->, nous redirige vers la page Multichannel
        main_FloatingButtonMultiChannel!!.setOnClickListener {
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

        // En mode Multiselect, lors du click sur le Floating Button SMS, nous redirige vers l'appli SMS de l'utilisateur avec les contacts sélectionnés
        main_FloatingButtonSMS!!.setOnClickListener {
            val iterator: IntIterator?
            val listOfPhoneNumberContactSelected: ArrayList<String> = ArrayList()
            iterator = (0 until listOfItemSelected.size).iterator()
            for (i in iterator) {
                listOfPhoneNumberContactSelected.add(listOfItemSelected[i].getFirstPhoneNumber())
            }
            monoChannelSmsClick(listOfPhoneNumberContactSelected)
//            refreshActivity()
        }

        // En mode Multiselect, lors du click sur le Floating Button Mail, nous redirige vers l'appli Mail de l'utilisateur avec les contacts sélectionnés
        main_FloatingButtonMail!!.setOnClickListener {
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
                main_FloatingButtonAddNewContact!!.visibility = View.VISIBLE
                main_FloatingButtonMultiChannel!!.visibility = View.GONE

                main_FloatingButtonMail!!.visibility = View.GONE
                main_FloatingButtonSMS!!.visibility = View.GONE
                main_FloatingButtonGroup!!.visibility = View.GONE
            } else {
                main_RecyclerView!!.adapter = ContactRecyclerViewAdapter(this, gestionnaireContacts, len)
                main_FloatingButtonAddNewContact!!.visibility = View.VISIBLE
                main_FloatingButtonMultiChannel!!.visibility = View.GONE

                main_FloatingButtonMail!!.visibility = View.GONE
                main_FloatingButtonSMS!!.visibility = View.GONE
                main_FloatingButtonGroup!!.visibility = View.GONE
            }

            refreshActivity()
        }

        // En mode Multiselect, lors du click sur le Floating Button Group, ouvre une Popup création de groupe
        main_FloatingButtonGroup!!.setOnClickListener {
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
                gestionnaireContacts!!.sortContactByPriority()
                val sharedDefaultTriPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
                val len = sharedDefaultTriPreferences.getInt("gridview", 4)
                val edit: SharedPreferences.Editor = sharedDefaultTriPreferences.edit()
                edit.putString("tri", "priorite")
                edit.apply()
                runOnUiThread {
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
            fromStartActivity = false
        }
    }

    //region ========================================== Functions ===========================================
    /**
     *  Les affichages du mode Multiselect sont enlevés pour remettre l'affichage initial
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
                    when {
                        sharedPreferences.getString("tri", "nom") == "prenom" -> contactListDb.sortContactByFirstNameAZ()
                        sharedPreferences.getString("tri", "nom") == "lastname" -> contactListDb.sortContactByLastname()
                        sharedPreferences.getString("tri", "nom") == "priorite" -> contactListDb.sortContactByPriority()
                        sharedPreferences.getString("tri", "nom") == "favoris" -> contactListDb.sortContactByFavorite()
                        else -> contactListDb.sortContactByGroup()
                    }
                    //on garde uniquement les contact en commun avec les filtres et tris
                    contactListDb.contactList.retainAll(filteredContact)
                    gestionnaireContacts!!.contactList.clear()
                    gestionnaireContacts!!.contactList.addAll(contactListDb.contactList)
                    //on check si on est en grid ou list view pour savoir laquelle update
                    if (len > 1) {
                        gridViewAdapter = ContactGridViewAdapter(this@MainActivity, gestionnaireContacts, len)
                        main_GridView!!.adapter = gridViewAdapter
                        gridViewAdapter!!.notifyDataSetChanged()
                    } else {
                        recyclerViewAdapter = ContactRecyclerViewAdapter(this@MainActivity, gestionnaireContacts, len)
                        main_RecyclerView!!.adapter = recyclerViewAdapter
                        recyclerViewAdapter!!.notifyDataSetChanged()
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
                    when {
                        sharedPreferences.getString("tri", "nom") == "prenom" -> gestionnaireContacts!!.sortContactByFirstNameAZ()
                        sharedPreferences.getString("tri", "nom") == "lastname" -> gestionnaireContacts!!.sortContactByLastname()
                        sharedPreferences.getString("tri", "nom") == "priorite" -> gestionnaireContacts!!.sortContactByPriority()
                        sharedPreferences.getString("tri", "nom") == "favoris" -> gestionnaireContacts!!.sortContactByFavorite()
                        else -> gestionnaireContacts!!.sortContactByGroup()
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
                    when {
                        sharedPreferences.getString("tri", "nom") == "prenom" -> contactListDb.sortContactByFirstNameAZ()
                        sharedPreferences.getString("tri", "nom") == "lastname" -> contactListDb.sortContactByLastname()
                        sharedPreferences.getString("tri", "nom") == "priorite" -> contactListDb.sortContactByPriority()
                        sharedPreferences.getString("tri", "nom") == "favoris" -> contactListDb.sortContactByFavorite()
                        else -> contactListDb.sortContactByGroup()
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
                    when {
                        sharedPreferences.getString("tri", "nom") == "prenom" -> gestionnaireContacts!!.sortContactByFirstNameAZ()
                        sharedPreferences.getString("tri", "nom") == "lastname" -> gestionnaireContacts!!.sortContactByLastname()
                        sharedPreferences.getString("tri", "nom") == "priorite" -> gestionnaireContacts!!.sortContactByPriority()
                        sharedPreferences.getString("tri", "nom") == "favoris" -> gestionnaireContacts!!.sortContactByFavorite()
                        else -> gestionnaireContacts!!.sortContactByGroup()
                    }
                    gestionnaireContacts!!.contactList.retainAll(filteredContact)
                    if (len > 1) {
                        gridViewAdapter = ContactGridViewAdapter(this@MainActivity, gestionnaireContacts, len)
                        main_GridView!!.adapter = gridViewAdapter
                    } else {
                        println("gestionnaire contacct  size ===================" + gestionnaireContacts!!.contactList.size)
                        recyclerViewAdapter = ContactRecyclerViewAdapter(this@MainActivity, gestionnaireContacts, len)
                        main_RecyclerView!!.adapter = recyclerViewAdapter
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
                        edit.putString("tri", "prenom")
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
        pm.setComponentEnabledSetting(cmpName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
        pm.setComponentEnabledSetting(cmpName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
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

//        if (gridViewAdapter!!.listOfItemSelected.size == 0) {
        if (listOfItemSelected.size == 0) {

//            val pos = main_GridView!!.firstVisiblePosition
            val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
            val len = sharedPreferences.getInt("gridview", 4)
            gridViewAdapter = ContactGridViewAdapter(this@MainActivity, gestionnaireContacts, len)
            main_GridView!!.layoutManager = GridLayoutManager(this, len)
            main_GridView!!.adapter = gridViewAdapter
//            main_GridView!!.setSe(pos)
//            main_GridView!!.setSelection(pos)

            Toast.makeText(this, R.string.main_toast_multi_select_deactived, Toast.LENGTH_SHORT).show()

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
            main_ToolbarMultiSelectModeTitle!!.text = i.toString() + " " + getString(R.string.main_toast_multi_select_mode_selected)
        } else if (listOfItemSelected.size > 1) {
            main_ToolbarMultiSelectModeTitle!!.text = i.toString() + " " + getString(R.string.main_toast_multi_select_mode_selected_more_than_one)
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
            Toast.makeText(this, R.string.main_toast_multi_select_actived, Toast.LENGTH_SHORT).show()
            main_FloatingButtonAddNewContact!!.visibility = View.GONE
            main_FloatingButtonMultiChannel!!.visibility = View.VISIBLE
            firstClick = false
            multiChannelMode = true
            main_ToolbarMultiSelectModeLayout!!.visibility = View.VISIBLE
            main_ToolbarLayout!!.visibility = View.GONE

        } else if (listOfItemSelected.size == 0) {
            Toast.makeText(this, R.string.main_toast_multi_select_deactived, Toast.LENGTH_SHORT).show()

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
            main_ToolbarMultiSelectModeTitle!!.text = i.toString() + " " + getString(R.string.main_toast_multi_select_mode_selected)
        } else if (listOfItemSelected.size > 1) {
            main_ToolbarMultiSelectModeTitle!!.text = i.toString() + " " + getString(R.string.main_toast_multi_select_mode_selected_more_than_one)
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
            val params: ViewGroup.MarginLayoutParams = main_FloatingButtonMail!!.layoutParams as ViewGroup.MarginLayoutParams
            params.bottomMargin = margin * i
            main_FloatingButtonMail!!.layoutParams = params
            println("height of floating mail" + main_FloatingButtonMail!!.height)
            i++
        } else {
            println("false mail")
            main_FloatingButtonMail!!.visibility = View.GONE
        }
        main_FloatingButtonGroup!!.visibility = View.VISIBLE
        val params: ViewGroup.MarginLayoutParams = main_FloatingButtonGroup!!.layoutParams as ViewGroup.MarginLayoutParams
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
        intent.putExtra(Intent.EXTRA_EMAIL, contact)/*listOfMail.toArray(new String[listOfMail.size()]*/
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
        @SuppressLint("InflateParams") val alertView = inflater.inflate(R.layout.alert_dialog_edit_group, null, true)

        val mainCreateGroupAlertDialogTitle = alertView.findViewById<TextView>(R.id.manager_group_edit_group_alert_dialog_title)
        val mainCreateGroupAlertDialogEditText = alertView.findViewById<AppCompatEditText>(R.id.manager_group_edit_group_view_edit)

        val mainCreateGroupAlertDialogRedTag = alertView.findViewById<AppCompatImageView>(R.id.manager_group_edit_group_color_red)
        val mainCreateGroupAlertDialogBlueTag = alertView.findViewById<AppCompatImageView>(R.id.manager_group_edit_group_color_blue)
        val mainCreateGroupAlertDialogGreenTag = alertView.findViewById<AppCompatImageView>(R.id.manager_group_edit_group_color_green)
        val mainCreateGroupAlertDialogYellowTag = alertView.findViewById<AppCompatImageView>(R.id.manager_group_edit_group_color_yellow)
        val mainCreateGroupAlertDialogOrangeTag = alertView.findViewById<AppCompatImageView>(R.id.manager_group_edit_group_color_orange)
        val mainCreateGroupAlertDialogPurpleTag = alertView.findViewById<AppCompatImageView>(R.id.manager_group_edit_group_color_purple)

        // Titre de la Popup
        mainCreateGroupAlertDialogTitle.text = this.getString(R.string.main_alert_dialog_group_title)

        // Text hint de l'EditText
        mainCreateGroupAlertDialogEditText.hint = getString(R.string.group_name).format(main_ContactsDatabase!!.GroupsDao().getIdNeverUsed())

        mainCreateGroupAlertDialogRedTag.setImageResource(R.drawable.border_selected_image_view)
        var color = this.getColor(R.color.red_tag_group)

        //region ================================= ClickListenerOnColorTag ==================================

        mainCreateGroupAlertDialogRedTag.setOnClickListener {
            mainCreateGroupAlertDialogRedTag.setImageResource(R.drawable.border_selected_image_view)
            mainCreateGroupAlertDialogBlueTag.setImageResource(android.R.color.transparent)
            mainCreateGroupAlertDialogGreenTag.setImageResource(android.R.color.transparent)
            mainCreateGroupAlertDialogYellowTag.setImageResource(android.R.color.transparent)
            mainCreateGroupAlertDialogOrangeTag.setImageResource(android.R.color.transparent)
            mainCreateGroupAlertDialogPurpleTag.setImageResource(android.R.color.transparent)

            color = this.getColor(R.color.red_tag_group)
        }

        mainCreateGroupAlertDialogBlueTag.setOnClickListener {
            mainCreateGroupAlertDialogBlueTag.setImageResource(R.drawable.border_selected_image_view)
            mainCreateGroupAlertDialogRedTag.setImageResource(android.R.color.transparent)
            mainCreateGroupAlertDialogGreenTag.setImageResource(android.R.color.transparent)
            mainCreateGroupAlertDialogYellowTag.setImageResource(android.R.color.transparent)
            mainCreateGroupAlertDialogOrangeTag.setImageResource(android.R.color.transparent)
            mainCreateGroupAlertDialogPurpleTag.setImageResource(android.R.color.transparent)

            color = this.getColor(R.color.blue_tag_group)
        }

        mainCreateGroupAlertDialogGreenTag.setOnClickListener {
            mainCreateGroupAlertDialogGreenTag.setImageResource(R.drawable.border_selected_image_view)
            mainCreateGroupAlertDialogRedTag.setImageResource(android.R.color.transparent)
            mainCreateGroupAlertDialogBlueTag.setImageResource(android.R.color.transparent)
            mainCreateGroupAlertDialogYellowTag.setImageResource(android.R.color.transparent)
            mainCreateGroupAlertDialogOrangeTag.setImageResource(android.R.color.transparent)
            mainCreateGroupAlertDialogPurpleTag.setImageResource(android.R.color.transparent)

            color = this.getColor(R.color.green_tag_group)
        }

        mainCreateGroupAlertDialogYellowTag.setOnClickListener {
            mainCreateGroupAlertDialogYellowTag.setImageResource(R.drawable.border_selected_image_view)
            mainCreateGroupAlertDialogRedTag.setImageResource(android.R.color.transparent)
            mainCreateGroupAlertDialogGreenTag.setImageResource(android.R.color.transparent)
            mainCreateGroupAlertDialogBlueTag.setImageResource(android.R.color.transparent)
            mainCreateGroupAlertDialogOrangeTag.setImageResource(android.R.color.transparent)
            mainCreateGroupAlertDialogPurpleTag.setImageResource(android.R.color.transparent)

            color = this.getColor(R.color.yellow_tag_group)
        }

        mainCreateGroupAlertDialogOrangeTag.setOnClickListener {
            mainCreateGroupAlertDialogOrangeTag.setImageResource(R.drawable.border_selected_image_view)
            mainCreateGroupAlertDialogRedTag.setImageResource(android.R.color.transparent)
            mainCreateGroupAlertDialogGreenTag.setImageResource(android.R.color.transparent)
            mainCreateGroupAlertDialogYellowTag.setImageResource(android.R.color.transparent)
            mainCreateGroupAlertDialogBlueTag.setImageResource(android.R.color.transparent)
            mainCreateGroupAlertDialogPurpleTag.setImageResource(android.R.color.transparent)

            color = this.getColor(R.color.orange_tag_group)
        }

        mainCreateGroupAlertDialogPurpleTag.setOnClickListener { _ ->
            mainCreateGroupAlertDialogPurpleTag.setImageResource(R.drawable.border_selected_image_view)
            mainCreateGroupAlertDialogRedTag.setImageResource(android.R.color.transparent)
            mainCreateGroupAlertDialogGreenTag.setImageResource(android.R.color.transparent)
            mainCreateGroupAlertDialogYellowTag.setImageResource(android.R.color.transparent)
            mainCreateGroupAlertDialogOrangeTag.setImageResource(android.R.color.transparent)
            mainCreateGroupAlertDialogBlueTag.setImageResource(android.R.color.transparent)

            color = this.getColor(R.color.purple_tag_group)
        }

        //endregion

        MaterialAlertDialogBuilder(this, R.style.AlertDialog)
                .setView(alertView)
                .setPositiveButton(R.string.alert_dialog_validate) { _, _ ->
                    if (mainCreateGroupAlertDialogEditText.text!!.toString() == "Favorites" || mainCreateGroupAlertDialogEditText.text!!.toString() == "Favorites") {
                        hideKeyboard()
                        Toast.makeText(this, getString(R.string.main_alert_dialog_group_favorites_already_exist), Toast.LENGTH_LONG).show()
                    } else {
                        val nameGroup = if (mainCreateGroupAlertDialogEditText.text.toString().isNotEmpty()) {
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
                        main_ContactsDatabase!!.GroupsDao().updateGroupSectionColorById(idGroup.toInt(), color)
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
     *Passer de l'affichage multiselect au mode normal en changeant le laytout
     */
    private fun switchMultiSelectToNormalMode() {
        main_FloatingButtonMultiChannel!!.visibility = View.GONE
        main_ToolbarMultiSelectModeLayout!!.visibility = View.GONE
        main_ToolbarLayout!!.visibility = View.VISIBLE
        main_FloatingButtonMail!!.visibility = View.GONE
        main_FloatingButtonSMS!!.visibility = View.GONE
        main_FloatingButtonGroup!!.visibility = View.GONE
    }


    private fun importWhatsappContacts(listContact: List<ContactDB>) {
        //This class provides applications access to the content model.
        val cr = contentResolver

        //RowContacts for filter Account Types
        val contactCursor = cr.query(
                ContactsContract.RawContacts.CONTENT_URI,
                arrayOf(
                        ContactsContract.RawContacts._ID,
                        ContactsContract.RawContacts.CONTACT_ID
                ),
                ContactsContract.RawContacts.ACCOUNT_TYPE + "= ?",
                arrayOf("com.whatsapp"),
                null)

        //ArrayList for Store Whatsapp Contact
        val myWhatsappContacts = ArrayList<String>()

        if (contactCursor != null) {
            if (contactCursor.count > 0) {
                if (contactCursor.moveToFirst()) {
                    do {
                        //whatsappContactId for get Number,Name,Id ect... from  ContactsContract.CommonDataKinds.Phone
                        val whatsappContactId = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.RawContacts.CONTACT_ID))

                        if (whatsappContactId != null) {
                            //Get Data from ContactsContract.CommonDataKinds.Phone of Specific CONTACT_ID
                            val whatsAppContactCursor = cr.query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                    arrayOf(
                                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                                            ContactsContract.CommonDataKinds.Phone.NUMBER,
                                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                                    ),
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                    arrayOf(whatsappContactId), null);

                            if (whatsAppContactCursor != null) {
                                whatsAppContactCursor.moveToFirst()
                                val name = whatsAppContactCursor.getString(whatsAppContactCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                                val number = whatsAppContactCursor.getString(whatsAppContactCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

                                whatsAppContactCursor.close()

                                //Add Number to ArrayList
                                myWhatsappContacts.add(number)

                                for (contact in listContact) {
                                    if (contact.firstName + " " + contact.lastName == name || contact.firstName == name || contact.lastName == name) {

                                        main_ContactsDatabase!!.contactsDao().getContact(contact.id!!).setHasWhatsapp(main_ContactsDatabase)

                                        val detail = ContactDetailDB(null, contact.id, number, "phone", "", 0)
                                        main_ContactsDatabase!!.contactDetailsDao().insert(detail)
                                    }
                                }

//                                showLog(TAG, " WhatsApp contact id  :  " + id);
//                                showLogI(TAG, " WhatsApp contact name :  " + name);
//                                showLogI(TAG, " WhatsApp contact number :  " + number);
                            }
                        }
                    } while (contactCursor.moveToNext())
                    contactCursor.close()
                }
            }
        }
    }

    //endregion
}