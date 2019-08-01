package com.example.knocker.controller.activity.firstLaunch

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import androidx.core.app.ActivityCompat
import com.example.knocker.R

class StartActivty : AppCompatActivity() {

    var start_activity_button_Autorize_superposition:Button?=null
    var start_activity_button_Permission:Button?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_activty)

        start_activity_button_Autorize_superposition = findViewById(R.id.start_activity_permissions_button)
        start_activity_button_Permission = findViewById(R.id.start_activity_permissions_button)

        start_activity_button_Autorize_superposition!!.setOnClickListener{
            val intentPermission = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivity(intentPermission)
        }
        start_activity_button_Permission!!.setOnClickListener{
            val arraylistPermission= ArrayList<String>()
            arraylistPermission.add(Manifest.permission.SEND_SMS)
            arraylistPermission.add(Manifest.permission.CALL_PHONE)
            ActivityCompat.requestPermissions(this,arraylistPermission.toArray(arrayOfNulls<String>(arraylistPermission.size)),2)
        }

    }
}
