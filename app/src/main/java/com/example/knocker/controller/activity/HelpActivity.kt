package com.example.knocker.controller.activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.knocker.R

/**
 * La Classe qui permet d'afficher les informations,la FAQ, le contact et les conditions de knocker
 * @author Kenzy Suon
 */
class HelpActivity : AppCompatActivity() {

    var help_activity_QandA: ConstraintLayout? = null
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

        help_activity_QandA = findViewById(R.id.help_activity_q_and_r_id)
        help_activity_ContactUs = findViewById(R.id.help_activity_contact_us_id)
        help_activity_Terms = findViewById(R.id.help_activity_terms_id)
        help_activity_Infos = findViewById(R.id.help_activity_infos_id)

        //endregion

        //region ==================================== SetOnClickListener ====================================

        help_activity_Infos!!.setOnClickListener {

        }

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
