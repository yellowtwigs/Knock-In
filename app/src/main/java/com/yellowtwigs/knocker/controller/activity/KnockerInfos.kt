package com.yellowtwigs.knocker.controller.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.yellowtwigs.knocker.R

class KnockerInfos : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.knocker_infos)
    }

    override fun onBackPressed() {
        startActivity(Intent(this@KnockerInfos, MainActivity::class.java))
    }
}
