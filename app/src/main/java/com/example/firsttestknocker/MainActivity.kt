package com.example.firsttestknocker

import android.content.Intent
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.GridView
import android.widget.AdapterView

import java.util.ArrayList

class MainActivity : AppCompatActivity() {

    private var main_GridView: GridView? = null
    private var drawerLayout: DrawerLayout? = null
    private var main_FloatingButtonOpen: FloatingActionButton? = null
    private var main_FloatingButtonAdd: FloatingActionButton? = null
    private var main_FloatingButtonCompose: FloatingActionButton? = null
    private var main_FloatingButtonOpenAnimation: Animation? = null
    private var main_FloatingButtonCloseAnimation: Animation? = null
    private var main_FloatingButtonClockWiserAnimation: Animation? = null
    private var main_FloatingButtonAntiClockWiserAnimation: Animation? = null
    internal var isOpen = false
    // Database && Thread
    private var mDb: ContactsRoomDatabase? = null
    private lateinit var mDbWorkerThread: DbWorkerThread

    private val contactList: List<Contact>
        get() {
            val list = ArrayList<Contact>()
//            val Michel = Contact("Michel", "Ferachoglou", "06 51 74 09 03", R.drawable.michel, R.drawable.aquarius)
//            val Jean_Luc = Contact("Jean Luc", "Paulin", "06 66 93 32 49", R.drawable.jl, R.drawable.aquarius)
//            val Jean_Francois = Contact("Jean Francois", "Coudeyre", "07 78 03 65 54", R.drawable.jf, R.drawable.aquarius)
//            val Ryan = Contact("Ryan", "Granet", "07 04 51 42 37", R.drawable.ryan, R.drawable.aquarius)
//            val Florian = Contact("Florian", "Striebel", "06 96 32 09 28", R.drawable.img_avatar, R.drawable.aquarius)
//
//            list.add(Michel)
//            list.add(Jean_Luc)
//            list.add(Jean_Francois)
//            list.add(Ryan)
//            list.add(Florian)
            println("end contact")

            return list
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // on init WorkerThread
        mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        mDbWorkerThread.start()

        //on get la base de données
        mDb = ContactsRoomDatabase.getDatabase(this)

        // Floating Button
        main_FloatingButtonOpen = findViewById(R.id.main_floating_button_open_id)
        main_FloatingButtonAdd = findViewById(R.id.main_floating_button_add_id)
        main_FloatingButtonCompose = findViewById(R.id.main_floating_button_compose_id)
        main_FloatingButtonOpenAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.fab_open)
        main_FloatingButtonCloseAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.fab_close)
        main_FloatingButtonClockWiserAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.rotate_clockwiser)
        main_FloatingButtonAntiClockWiserAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.rotate_anticlockwiser)

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
            }

            val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
            drawer.closeDrawer(GravityCompat.START)
            true
        }

        val printContacts = Runnable {
            // Grid View
            main_GridView = findViewById(R.id.main_grid_view_id)
            val contactList = mDb?.contactsDao()?.getAllContacts() //contactList

            if (main_GridView != null) {
                val contactAdapter = ContactAdapter(this, contactList)
                main_GridView!!.adapter = contactAdapter

                main_GridView!!.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                    val o = main_GridView!!.getItemAtPosition(position)
                    val contact = o as Contact

                    val intent = Intent(this@MainActivity, ContactDetailsActivity::class.java)
                    intent.putExtra("ContactFirstName", contact.contactFirstName)
                    intent.putExtra("ContactLastName", contact.contactLastName)
                    intent.putExtra("ContactPhoneNumber", contact.contactPhoneNumber)
                    intent.putExtra("ContactImage", contact.contactImage)
                    startActivity(intent)
                    //                finish();
                }

                // Drag n Drop
                main_GridView!!.onItemLongClickListener = AdapterView.OnItemLongClickListener { _, _, _, _ ->
                    //                finish();
                    false
                }
            }
        }
        mDbWorkerThread.postTask(printContacts)

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
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                drawerLayout!!.openDrawer(GravityCompat.START)
                return true
            }
            R.id.nav_category -> return true
            R.id.nav_filter -> return true
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
        main_FloatingButtonOpen!!.startAnimation(main_FloatingButtonAntiClockWiserAnimation)

        main_FloatingButtonAdd!!.isClickable = false
        main_FloatingButtonCompose!!.isClickable = false
    }

    fun onFloatingClick() {
        main_FloatingButtonAdd!!.startAnimation(main_FloatingButtonOpenAnimation)
        main_FloatingButtonCompose!!.startAnimation(main_FloatingButtonOpenAnimation)
        main_FloatingButtonOpen!!.startAnimation(main_FloatingButtonClockWiserAnimation)

        main_FloatingButtonAdd!!.isClickable = true
        main_FloatingButtonCompose!!.isClickable = true
    }
}
