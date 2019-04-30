package com.example.firsttestknocker

import android.content.Context
import android.provider.ContactsContract
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream

object ListContact {

    fun loadJSONFromAsset(context: Context):String{
        var json = ""
        try{
            json= context.assets.open("premiers_contacts.json").bufferedReader().use{
                it.readText()
            }
        }catch (e :Exception){
            e.printStackTrace()
            return ""
        }
        return json
    }
    fun construireListe(contacts:String):List<Contacts>{
        var listContacts = mutableListOf<Contacts>()
        try {
                val jsArray:JSONArray = JSONArray(contacts)
                for(x in 0..(jsArray.length()-1)){
                    listContacts.add(getContactFromJSONObject(jsArray.getJSONObject(x)))
                }
            }catch (e:Exception){
            e.printStackTrace()
        }
        return listContacts
    }
    fun getContactFromJSONObject(json: JSONObject):Contacts{
        val firstName:String = json.getString("first_name")
        val lastName:String = json.getString("last_name")
        val phoneNumber:String = json.getString("phone_number")
        val mail:String=json.getString("mail")
        val profilPicture: Int = R.drawable.img_avatar
        var backgroundImage:Int = R.drawable.aquarius
        val contactPriority :Int= json.getInt("contact_priority")
        val profilPictureStr:String = json.getString("profile_picture_str")

        println("contact :"+ firstName +" "+ lastName)
        val contact:Contacts= Contacts(null,firstName,lastName,phoneNumber,mail,profilPicture,backgroundImage,contactPriority,profilPictureStr)
        return contact
    }
}