package com.yellowtwigs.knockin.controller.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import com.facebook.*
import com.yellowtwigs.knockin.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.facebook.login.widget.LoginButton
import com.facebook.login.LoginResult
import com.facebook.login.LoginManager
import com.facebook.ProfileTracker
import com.facebook.GraphResponse
import com.facebook.GraphRequest
import com.facebook.HttpMethod
import com.facebook.AccessToken
import com.google.android.youtube.player.internal.e
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


open class MessengerActivity : AppCompatActivity() {

    //region ========================================== Var or Val ==========================================

    private var messengerDrawerLayout: DrawerLayout? = null

    private var messenger_ComposeMessageFloatingButton: FloatingActionButton? = null

    private var messenger_RecyclerView: RecyclerView? = null

    private var facebook_LoginButton: LoginButton? = null
    private val EMAIL = "email"
    private var callbackManager: CallbackManager? = null

    private var profileTracker: ProfileTracker? = null

    private var isConnected: Boolean = false

    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //region ======================================== Theme Dark ========================================

        val sharedThemePreferences = getSharedPreferences("Knockin_Theme", Context.MODE_PRIVATE)
        if (sharedThemePreferences.getBoolean("darkTheme", false)) {
            setTheme(R.style.AppThemeDark)
        } else {
            setTheme(R.style.AppTheme)
        }

        //endregion

        setContentView(R.layout.activity_messenger)

        //region ======================================= FindViewById =======================================

        messenger_ComposeMessageFloatingButton = findViewById(R.id.messenger_compose_message)
        messenger_RecyclerView = findViewById(R.id.messenger_recycler_view)

        facebook_LoginButton = findViewById(R.id.facebook_login_button)
        facebook_LoginButton!!.setReadPermissions(listOf(EMAIL))

        //endregion

        //region ========================================= Toolbar ==========================================

        val toolbar = findViewById<Toolbar>(R.id.messenger_toolbar)
        setSupportActionBar(toolbar)
        toolbar.overflowIcon = getDrawable(R.drawable.ic_toolbar_menu)
        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar.setHomeAsUpIndicator(R.drawable.ic_open_drawer)
        actionbar.title = ""
        actionbar.setBackgroundDrawable(ColorDrawable(Color.parseColor("#ffffff")))

        //endregion

        //region ====================================== Drawer Layout =======================================

        messengerDrawerLayout = findViewById(R.id.messenger_drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        val menu = navigationView.menu
        val navItem = menu.findItem(R.id.nav_home)
        navItem.isChecked = true
        val navSyncContact = menu.findItem(R.id.nav_sync_contact)
        navSyncContact.isVisible = true

        navigationView.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            messengerDrawerLayout!!.closeDrawers()

            when (menuItem.itemId) {
                R.id.nav_home -> startActivity(Intent(this@MessengerActivity, MessengerActivity::class.java))
                R.id.nav_notif_config -> startActivity(Intent(this@MessengerActivity, ManageNotificationActivity::class.java))
                R.id.nav_settings -> startActivity(Intent(this@MessengerActivity, SettingsActivity::class.java))
                R.id.nav_manage_screen -> startActivity(Intent(this@MessengerActivity, ManageMyScreenActivity::class.java))
                R.id.nav_knockons -> startActivity(Intent(this@MessengerActivity, ManageKnockonsActivity::class.java))
                R.id.nav_help -> startActivity(Intent(this@MessengerActivity, HelpActivity::class.java))
            }

            val drawer = findViewById<DrawerLayout>(R.id.messenger_drawer_layout)
            drawer.closeDrawer(GravityCompat.START)
            true
        }

        //endregion

        //region ========================================= Adapter ==========================================

//        messenger_RecyclerView.adapter = MessengerRecyclerViewAdapter(this, )

        //endregion

        //region ======================================== Listeners =========================================

        messenger_ComposeMessageFloatingButton!!.setOnClickListener {
            startActivity(Intent(this@MessengerActivity, ComposeMessageActivity::class.java))
        }

        //endregion

        //region ========================================= Facebook =========================================

        FacebookSdk.sdkInitialize(this.applicationContext);
        callbackManager = CallbackManager.Factory.create()
        val accessToken = AccessToken.getCurrentAccessToken()
        val isLoggedIn = accessToken != null && !accessToken.isExpired
        LoginManager.getInstance().logInWithReadPermissions(this, listOf("public_profile"));

        /* make the API call */
        GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/{friend-list-id}",
                null,
                HttpMethod.GET,
                GraphRequest.Callback { /* handle the result */ }
        ).executeAsync()

        LoginManager.getInstance().registerCallback(callbackManager,
                object : FacebookCallback<LoginResult> {
                    override fun onSuccess(loginResult: LoginResult) {
//                        isConnected = true
                        Toast.makeText(this@MessengerActivity, "is connected", Toast.LENGTH_SHORT).show()
                    }

                    override fun onCancel() {
                        // App code
                    }

                    override fun onError(exception: FacebookException) {
                        // App code
                    }
                })

        val request = GraphRequest.newGraphPathRequest(
                accessToken, "/{user-id}/friends") {
            // Insert your code here
            val jsonArrayFriends = it.jsonArray
            val friendlistObject = it.jsonObject.getJSONObject("friendlist").getJSONArray("data")
            var cpt = 0

            while (cpt < jsonArrayFriends.length()) {
                if (jsonArrayFriends != null){
                    Toast.makeText(this@MessengerActivity, jsonArrayFriends[cpt].toString(), Toast.LENGTH_SHORT).show()
                }
                if (jsonArrayFriends != null){
                    Toast.makeText(this@MessengerActivity, friendlistObject[cpt].toString(), Toast.LENGTH_SHORT).show()
                }
                cpt++
            }
        }

        request.executeAsync()

        if (isConnected) {
            val token = AccessToken.getCurrentAccessToken()
            val graphRequest = GraphRequest.newMeRequest(token) { jsonObject, response ->
                try {
                    val jsonArrayFriends = jsonObject.getJSONObject("friendlist").getJSONArray("data")
                    val friendlistObject = jsonArrayFriends.getJSONObject(0)
                    val friendListID = friendlistObject.getString("id")
                    myNewGraphReq(friendListID)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }

            val param = Bundle()
            param.putString("fields", "friendlist")
            graphRequest.parameters = param
            graphRequest.executeAsync()
        }

        //endregion
    }

    //region ========================================== Functions ===========================================

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager!!.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        super.onDestroy()
        profileTracker!!.stopTracking()
    }

    private fun myNewGraphReq(friendlistId: String) {
        val graphPath = "/$friendlistId/members/";
        val token = AccessToken.getCurrentAccessToken();
        val request = GraphRequest(token, graphPath, null, HttpMethod.GET, GraphRequest.Callback() {
            val jsonObject = it.jsonObject;
            try {
                val arrayOfUsersInFriendList = jsonObject.getJSONArray("data");
                /* Do something with the user list */
                /* ex: get first user in list, "name" */
                val user = arrayOfUsersInFriendList.getJSONObject(0);
                val usersName = user.getString("name");
            } catch (e: JSONException) {
                e.printStackTrace();
            }
        });
        val param = Bundle();
        param.putString("fields", "name");
        request.parameters = param;
        request.executeAsync();
    }

    //endregion
}
