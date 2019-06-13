package com.example.knocker.controller.activity.firstLaunch

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.knocker.R
import com.example.knocker.model.ContactList

class SynchronyseContactActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_synchronyse_contact)
        val synchronyse:Button= findViewById(R.id.synchronyse_Button_accept)
        val notSynchronyse:Button = findViewById(R.id.synchronyse_Button_not_accept)
        synchronyse.setOnClickListener({
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), REQUEST_CODE_READ_CONTACT)
          //  startActivity(Intent(this@SynchronyseContactActivity,AcceptNotificationtActivity::class.java))
        })
        notSynchronyse.setOnClickListener({
            overlayAlertDialog().show()
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        println("test")
        if(requestCode == REQUEST_CODE_READ_CONTACT){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"contact synchroniser",Toast.LENGTH_LONG).show()
                ContactList(this).getAllContacsInfoSync(contentResolver)
                startActivity(Intent(this@SynchronyseContactActivity,AcceptNotificationtActivity::class.java))
                finish()
            }else{
                overlayAlertDialog().show()
            }
        }
    }

    private fun overlayAlertDialog(): android.app.AlertDialog {
        val alertDialogBuilder = android.app.AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Knocker")
        alertDialogBuilder.setMessage("Vous pourrez toujours synchroniser vos contact dans la bar de navigation latéral avec l'onglet \'\'Synchroniser vos contact\'\' ")
        alertDialogBuilder.setPositiveButton("ok"
        ) { _, _ ->
            startActivity(Intent(this@SynchronyseContactActivity,AcceptNotificationtActivity::class.java))
        }

        return alertDialogBuilder.create()
    }
    override fun onBackPressed(){

    }

    companion object{
        const val REQUEST_CODE_READ_CONTACT = 99
    }
}
