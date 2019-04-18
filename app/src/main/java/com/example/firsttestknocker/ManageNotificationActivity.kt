package com.example.firsttestknocker

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Switch

class ManageNotificationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_notification)
        val sharedPreferences: SharedPreferences = getSharedPreferences("Knocker_preferences", Context.MODE_PRIVATE)
        val switchPopupNotif = this.findViewById<Switch>(R.id.switch_stop_popup)
        val switchservice =this.findViewById<Switch>(R.id.switch_stop_service)
        switchPopupNotif.setChecked(sharedPreferences.getBoolean("popupNotif",false))
        switchPopupNotif.setOnClickListener{
            val edit : SharedPreferences.Editor = sharedPreferences.edit()
            if(switchPopupNotif.isChecked){
                switchservice.setChecked(false)
                edit.remove("popupNotif")
                edit.remove("serviceNotif")
                edit.putBoolean("serviceNotif",false)
                edit.putBoolean("popupNotif",true)
                edit.commit()
                System.out.println("pop up true "+ sharedPreferences.getBoolean("popupNotif",false))
            }else{


                edit.remove("popupNotif")
                edit.putBoolean("popupNotif",false)
                edit.commit()
                System.out.println("pop up false"+ sharedPreferences.getBoolean("popupNotif",false))
            }

        }
        switchservice.setOnClickListener{
            val edit : SharedPreferences.Editor = sharedPreferences.edit()
            if(switchservice.isChecked){
                switchPopupNotif.setChecked(false)
                edit.remove("popupNotif")
                edit.remove("serviceNotif")
                edit.putBoolean("serviceNotif",true)
                edit.putBoolean("popupNotif",false)
                edit.commit()
                System.out.println("service economy true "+ sharedPreferences.getBoolean("serviceNotif",true))
            }else{

                edit.remove("serviceNotif")
                edit.putBoolean("serviceNotif",false)
                edit.commit()
                System.out.println("service economy false "+ sharedPreferences.getBoolean("serviceNotif",true))
            }

        }
    }
}