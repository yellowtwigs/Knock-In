package com.example.knocker.controller

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import com.example.knocker.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class PhoneLogActivity : AppCompatActivity() {

    private var main_BottomNavigationView: BottomNavigationView? = null
    private var phone_log_IncomingCallButton: FloatingActionButton? = null
    private var phone_log_PhoneNumber: AppCompatEditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_log)
        
        main_BottomNavigationView = findViewById(R.id.navigation)
        main_BottomNavigationView!!.menu.getItem(4).isChecked = true
        main_BottomNavigationView!!.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        phone_log_IncomingCallButton = findViewById(R.id.phone_log_incoming_call_button)
        phone_log_PhoneNumber = findViewById(R.id.phone_log_phone_number)

        phone_log_IncomingCallButton!!.setOnClickListener {
            if (!TextUtils.isEmpty(phone_log_PhoneNumber!!.text.toString())) {
                val dial = "tel:$phone_log_PhoneNumber!!.text.toString()"
                startActivity(Intent(Intent.ACTION_DIAL, Uri.parse(dial)))
            } else {
                Toast.makeText(this@PhoneLogActivity, "Enter a phone number", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_phone_book -> {
                startActivity(Intent(this@PhoneLogActivity, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_groups -> {
            }
            R.id.navigation_notifcations -> {
                startActivity(Intent(this@PhoneLogActivity, NotificationHistoryActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_socials_networks -> {
                startActivity(Intent(this@PhoneLogActivity, ChatActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_phone_keyboard -> {
                startActivity(Intent(this@PhoneLogActivity, PhoneLogActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }
}
