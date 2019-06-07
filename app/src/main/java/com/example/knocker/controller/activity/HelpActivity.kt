package com.example.knocker.controller.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.knocker.R

/**
 * La Classe qui permet d'afficher les informations,la FAQ, le contact et les conditions de knocker
 * @author Kenzy Suon
 */
class HelpActivity : AppCompatActivity() {

    var help_activity_FAQ: ConstraintLayout? = null
    var help_activity_ContactUs: ConstraintLayout? = null
    var help_activity_Terms: ConstraintLayout? = null
    var help_activity_Infos: ConstraintLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        //region ========================================== Toolbar =========================================

        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar.setHomeAsUpIndicator(R.drawable.ic_left_arrow)

        //endregion

        //region ======================================= FindViewById =======================================

        help_activity_FAQ = findViewById(R.id.help_activity_q_and_r_id)
        help_activity_ContactUs = findViewById(R.id.help_activity_contact_us_id)
        help_activity_Terms = findViewById(R.id.help_activity_terms_id)
        help_activity_Infos = findViewById(R.id.help_activity_infos_id)

        //endregion

        //region ==================================== SetOnClickListener ====================================
        val onClick = View.OnClickListener {
            if(it.id==help_activity_FAQ!!.id){
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.yellowtwigs.com/forum?utm_campaign=65c6b065-06db-4383-9240-d877a178ea3b&utm_source=so")))
            }
            if(it.id==help_activity_ContactUs!!.id){
                val intent= Intent(Intent.ACTION_SEND)
                intent.setData(Uri.parse("mailto:"))
                intent.setType("text/html");
                intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("contacts@yellowtwigs.com"))
                intent.putExtra(Intent.EXTRA_SUBJECT, "")
                intent.putExtra(Intent.EXTRA_TEXT,"")
                println("intent "+ intent.extras.toString())
                startActivity(Intent.createChooser(intent,"contactez-nous"))
            }
        }
        help_activity_ContactUs!!.setOnClickListener(onClick)
        help_activity_Infos!!.setOnClickListener(onClick)
        help_activity_FAQ!!.setOnClickListener(onClick)

        //endregion
    }

    // Intent to return to the MainActivity
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                val loginIntent = Intent(this@HelpActivity, MainActivity::class.java)
                startActivity(loginIntent)
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}