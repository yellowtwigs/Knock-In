package com.example.firsttestknocker

import android.app.Dialog
import android.content.Intent
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.GridView
import android.widget.AdapterView
import android.widget.ImageView

import java.util.ArrayList

class MainActivity : AppCompatActivity() {

    private var gridView: GridView? = null
    private var drawerLayout: DrawerLayout? = null
    private var fab_open: FloatingActionButton? = null
    private var fab_add: FloatingActionButton? = null
    private val fab_close: FloatingActionButton? = null
    private var fab_compose: FloatingActionButton? = null
    private var FabOpen: Animation? = null
    private var FabClose: Animation? = null
    private var Fab_R_ClockWiser: Animation? = null
    private var Fab_R_anticlockwiser: Animation? = null
    internal var isOpen = false

    private val contactList: List<Contact>
        get() {
            val list = ArrayList<Contact>()
//            val Michel = Contact("Michel", "Ferachoglou", "06 51 74 09 03", R.drawable.michel)
//            val Jean_Luc = Contact("Jean Luc", "Paulin", "06 66 93 32 49", R.drawable.jl)
//            val Jean_Francois = Contact("Jean Francois", "Coudeyre", "07 78 03 65 54", R.drawable.jf)
//            val Ryan = Contact("Ryan", "Granet", "07 04 51 42 37", R.drawable.ryan)
//            val Florian = Contact("Florian", "Striebel", "06 96 32 09 28", R.drawable.img_avatar)
//
//            list.add(Michel)
//            list.add(Jean_Luc)
//            list.add(Jean_Francois)
//            list.add(Ryan)
//            list.add(Florian)
//
            return list
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Floating Button
        fab_open = findViewById(R.id.fab_open)
        fab_add = findViewById(R.id.fab_add)
        fab_compose = findViewById(R.id.fab_compose)
        FabOpen = AnimationUtils.loadAnimation(applicationContext, R.anim.fab_open)
        FabClose = AnimationUtils.loadAnimation(applicationContext, R.anim.fab_close)
        Fab_R_ClockWiser = AnimationUtils.loadAnimation(applicationContext, R.anim.rotate_clockwiser)
        Fab_R_anticlockwiser = AnimationUtils.loadAnimation(applicationContext, R.anim.rotate_anticlockwiser)

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

        // Grid View
        gridView = findViewById(R.id.myGridView1)
        val contactList = contactList

        if (gridView != null) {
            val contactAdapter = ContactAdapter(this, contactList)
            gridView!!.adapter = contactAdapter

            gridView!!.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                val o = gridView!!.getItemAtPosition(position)
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
            gridView!!.onItemLongClickListener = AdapterView.OnItemLongClickListener { parent, view, position, id ->
                //                finish();
                false
            }
        }

        fab_open!!.setOnClickListener {
            if (isOpen) {
                onFloatingClickBack()
                isOpen = false
            } else {
                onFloatingClick()
                isOpen = true
            }
        }

        fab_add!!.setOnClickListener {
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
        fab_add!!.startAnimation(FabClose)
        fab_compose!!.startAnimation(FabClose)
        fab_open!!.startAnimation(Fab_R_anticlockwiser)

        fab_add!!.isClickable = false
        fab_compose!!.isClickable = false
    }

    fun onFloatingClick() {
        fab_add!!.startAnimation(FabOpen)
        fab_compose!!.startAnimation(FabOpen)
        fab_open!!.startAnimation(Fab_R_ClockWiser)

        fab_add!!.isClickable = true
        fab_compose!!.isClickable = true
    }
}
