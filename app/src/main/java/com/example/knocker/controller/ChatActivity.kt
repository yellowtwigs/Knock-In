package com.example.knocker.controller

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate

import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Switch

import com.example.knocker.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class ChatActivity : AppCompatActivity() {

    private var messenger: ImageView? = null
    private var instagram: ImageView? = null
    private var main_BottomNavigationView: BottomNavigationView? = null


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        messenger = findViewById(R.id.messenger)
        instagram = findViewById(R.id.instagram)

        main_BottomNavigationView = findViewById(R.id.navigation)
        main_BottomNavigationView!!.menu.getItem(3).isChecked = true
        main_BottomNavigationView!!.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        messenger!!.setOnClickListener { gotToFacebookPage("") }

        instagram!!.setOnClickListener {
            gotToWhatsapp()
            gotToInstagramPage()
        }
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_phone_book -> {
                startActivity(Intent(this@ChatActivity, MainActivity::class.java))
                overridePendingTransition(0, 0);
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_groups -> {
            }
            R.id.navigation_notifcations -> {
                startActivity(Intent(this@ChatActivity, NotificationHistoryActivity::class.java))
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_socials_networks -> {
            }
            R.id.navigation_phone_keyboard -> {
                startActivity(Intent(this@ChatActivity, PhoneLogActivity::class.java))
                overridePendingTransition(0, 0);
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }


    private fun gotToFacebookPage(id: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.messenger.com/t/$id"))
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.messenger.com/t/$id"))
            startActivity(intent)
        }
    }

    private fun gotToWhatsapp() {
        val uri = Uri.parse("smsto: " + "12345")
        val i = Intent(Intent.ACTION_SENDTO, uri)
        i.setPackage("com.whatsapp")
        startActivity(i)
    }

    private fun gotToInstagramPage() {
        val uri = Uri.parse("http://instagram.com/_u/therock/")
        val likeIng = Intent(Intent.ACTION_VIEW, uri)

        likeIng.setPackage("com.instagram.android")

        try {
            startActivity(likeIng)
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://instagram.com/")))
        }

    }
}
